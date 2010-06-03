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
package org.eclipse.cdt.debug.edc.launch;

import java.text.MessageFormat;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.launch.ServicesLaunchSequence;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotLaunchSequence;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.sourcelookup.DsfSourceLookupDirector;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ISourceLocator;

abstract public class EDCLaunchDelegate extends AbstractCLaunchDelegate2 {

	public EDCLaunchDelegate() {
		super(false);
	}

	public EDCLaunchDelegate(boolean requireCProject) {
		super(requireCProject);
	}

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		org.eclipse.cdt.launch.LaunchUtils.enableActivity("org.eclipse.cdt.debug.edc.ui.edcActivity", true); //$NON-NLS-1$
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			launchDebugger(configuration, launch, monitor);
		} else {
			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					MessageFormat.format("Launch mode ''{0}'' is not (yet) implemented", mode), null));
		}

	}

	private void launchDebugger(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		monitor.beginTask("Launching debugger session", 10);
		if (monitor.isCanceled()) {
			return;
		}

		try {
			launchDebugSession(config, launch, monitor);
		} finally {
			monitor.done();
		}
	}

	private ISourceLocator getSourceLocator(ILaunchConfiguration configuration, DsfSession session)
			throws CoreException {
		DsfSourceLookupDirector locator = new DsfSourceLookupDirector(session);
		String memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String) null);
		if (memento == null) {
			locator.initializeDefaults(configuration);
		} else {
			locator.initializeFromMemento(memento, configuration);
		}
		return locator;
	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		// Need to configure the source locator before creating the launch
		// because once the launch is created and added to launch manager,
		// the adapters will be created for the whole session, including
		// the source lookup adapter.

		EDCLaunch launch = new EDCLaunch(configuration, mode, null, getDebugModelID());
		launch.initialize();
		launch.setSourceLocator(getSourceLocator(configuration, launch.getSession()));
		return launch;
	}

	abstract public String getDebugModelID();

	abstract protected Sequence getLiveLaunchSequence(DsfExecutor executor, EDCLaunch launch, IProgressMonitor pm);

	protected Sequence getSnapshotLaunchSequence(DsfExecutor executor, EDCLaunch launch, IProgressMonitor pm) {
		return new SnapshotLaunchSequence(executor, launch, pm);
	};

	protected Sequence getFinalLaunchSequence(DsfExecutor executor, EDCLaunch launch, IProgressMonitor pm) {
		if (launch.isSnapshotLaunch())
			return getSnapshotLaunchSequence(executor, launch, pm);
		else
			return getLiveLaunchSequence(executor, launch, pm);
	};

	private void launchDebugSession(final ILaunchConfiguration config, ILaunch l, IProgressMonitor monitor)
			throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}

		final EDCLaunch launch = (EDCLaunch) l;

		monitor.worked(1);

		launch.setServiceFactory(newServiceFactory());

		launch.initializeSnapshotSupport();

		// Create and invoke the launch sequence to create the debug control and
		// services
		IProgressMonitor subMon1 = new SubProgressMonitor(monitor, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		final ServicesLaunchSequence servicesLaunchSequence = new ServicesLaunchSequence(launch.getSession(), launch,
				subMon1);

		launch.getSession().getExecutor().execute(servicesLaunchSequence);
		try {
			servicesLaunchSequence.get();
		} catch (InterruptedException e1) {
			throw new DebugException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Interrupted Exception in dispatch thread.\n" + e1.getLocalizedMessage(), e1)); //$NON-NLS-1$
		} catch (ExecutionException e1) {
			throw new DebugException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error in services launch sequence.", e1.getCause())); //$NON-NLS-1$
		}

		if (monitor.isCanceled())
			return;

		// The initializeControl method should be called after the
		// ICommandControlService
		// be initialized in the ServicesLaunchSequence above. This is because
		// it is that
		// service that will trigger the launch cleanup (if we need it during
		// this launch)
		// through an ICommandControlShutdownDMEvent
		launch.initializeMemoryRetrieval();

		monitor.worked(1);

		// Create and invoke the final launch sequence to setup the debugger
		IProgressMonitor subMon2 = new SubProgressMonitor(monitor, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		final Sequence finalLaunchSequence = getFinalLaunchSequence(launch.getSession().getExecutor(), launch, subMon2);

		launch.getSession().getExecutor().execute(finalLaunchSequence);
		boolean succeed = false;
		try {
			finalLaunchSequence.get();
			succeed = true;
		} catch (InterruptedException e1) {
			throw new DebugException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, DebugException.INTERNAL_ERROR,
					"Interrupted Exception in dispatch thread.\n" + e1.getLocalizedMessage(), e1)); //$NON-NLS-1$
		} catch (CancellationException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (ExecutionException e1) {
			Throwable cause = e1.getCause();
			if (cause instanceof CoreException) {
				IStatus s = ((CoreException) cause).getStatus();
				if (s.getSeverity() == IStatus.CANCEL)
					throw (CoreException) cause;
			}

			throw new DebugException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Error in final launch sequence.", e1.getCause())); //$NON-NLS-1$
		} finally {
			if (!succeed) {
				Query<Object> launchShutdownQuery = new Query<Object>() {
					@Override
					protected void execute(DataRequestMonitor<Object> rm) {
						launch.shutdownSession(rm);
					}
				};

				launch.getSession().getExecutor().execute(launchShutdownQuery);

				// Wait for the shutdown to finish. The Query.get() method is a
				// synchronous call which blocks until the query completes.
				try {
					launchShutdownQuery.get();
				} catch (InterruptedException e) {
					throw new DebugException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID,
							DebugException.INTERNAL_ERROR,
							"InterruptedException while shutting down debugger launch " + launch, e)); //$NON-NLS-1$ 
				} catch (ExecutionException e) {
					throw new DebugException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID,
							DebugException.REQUEST_FAILED, "Error in shutting down debugger launch " + launch, e)); //$NON-NLS-1$
				}
			}
		}
	}

	abstract protected IDsfDebugServicesFactory newServiceFactory();
}
