/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.BundleContext;

/**
 * ISourceLookup service implementation based on the CDT CSourceLookupDirector.
 * Supports multiple source lookup directors.
 */
public class CSourceLookup extends AbstractDsfService implements ISourceLookup {

	private final Map<ISourceLookupDMContext, List<CSourceLookupDirector>> directors = new HashMap<ISourceLookupDMContext, List<CSourceLookupDirector>>();

	public CSourceLookup(DsfSession session) {
		super(session);
	}

	@Override
	protected BundleContext getBundleContext() {
		return EDCDebugger.getBundleContext();
	}

	public void addSourceLookupDirector(ISourceLookupDMContext ctx, CSourceLookupDirector director) {
		List<CSourceLookupDirector> directorsInContext = directors.get(ctx);
		if (directorsInContext == null)
			directorsInContext = new ArrayList<CSourceLookupDirector>();
		directorsInContext.add(director);
		directors.put(ctx, directorsInContext);
	}

	public CSourceLookupDirector[] getSourceLookupDirectors(ISourceLookupDMContext ctx) {
		List<CSourceLookupDirector> directorList = directors.get(ctx);
		return directorList.toArray(new CSourceLookupDirector[directorList.size()]);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(final RequestMonitor requestMonitor) {
		// Register this service
		register(new String[] { CSourceLookup.class.getName(), ISourceLookup.class.getName() },
				new Hashtable<String, String>());

		requestMonitor.done();
	}

	@Override
	public void shutdown(final RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}

	public void getDebuggerPath(ISourceLookupDMContext sourceLookupCtx, Object source,
			final DataRequestMonitor<String> rm) {
		if (!(source instanceof String)) {
			// In future if needed other elements such as URIs could be
			// supported.
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED,
					"Only string source element is supported", null)); //$NON-NLS-1$);
			rm.done();
			return;
		}
		final String sourceString = (String) source;

		if (!directors.containsKey(sourceLookupCtx)) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
					"No source director configured for given context", null)); //$NON-NLS-1$);
			rm.done();
			return;
		}
		final CSourceLookupDirector[] director = getSourceLookupDirectors(sourceLookupCtx);

		new Job("Lookup Debugger Path") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				rm.setData(sourceString);

				for (CSourceLookupDirector cSourceLookupDirector : director) {
					IPath debuggerPath = cSourceLookupDirector.getCompilationPath(sourceString);
					if (debuggerPath != null) {
						rm.setData(debuggerPath.toString());
						break;
					}
				}			
				rm.done();

				return Status.OK_STATUS;
			}
		}.schedule();

	}

	public void getSource(ISourceLookupDMContext sourceLookupCtx, final String debuggerPath,
			final DataRequestMonitor<Object> rm) {
		if (!directors.containsKey(sourceLookupCtx)) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
					"No source director configured for given context", null)); //$NON-NLS-1$);
			rm.done();
			return;
		}
		final CSourceLookupDirector[] director = getSourceLookupDirectors(sourceLookupCtx);

		new Job("Lookup Source") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Object[] sources;
				try {
					for (CSourceLookupDirector cSourceLookupDirector : director) {
						sources = cSourceLookupDirector.findSourceElements(debuggerPath);
						if (sources == null || sources.length == 0) {
							rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID,
									IDsfStatusConstants.REQUEST_FAILED, "No sources found", null)); //$NON-NLS-1$);
						} else {
							rm.setData(sources[0]);
							break;
						}
					}
				} catch (CoreException e) {
					rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED,
							"Source lookup failed", e)); //$NON-NLS-1$);
				} finally {
					rm.done();
				}

				return Status.OK_STATUS;
			}
		}.schedule();
	}
}
