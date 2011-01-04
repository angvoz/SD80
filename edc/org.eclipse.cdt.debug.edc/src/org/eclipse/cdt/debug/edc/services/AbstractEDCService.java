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
package org.eclipse.cdt.debug.edc.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.osgi.framework.BundleContext;

/**
 * This is abstract DSF service with some APIs specific to EDC.
 */
public abstract class AbstractEDCService extends AbstractDsfService implements IEDCService {

	private final String[] classNames;
	private ITargetEnvironment targetEnvironmentService = null;
	private final boolean snapshot;
    private EDCServicesTracker fEDCTracker;

	public AbstractEDCService(DsfSession session, String[] classNames) {
		super(session);
		this.classNames = classNames;
		this.snapshot = Album.isSnapshotSession(session.getId());
	}

	public boolean isSnapshot() {
		return snapshot;
	}

	/**
	 * @since 2.0
	 */
	public EDCServicesTracker getEDCServicesTracker() {
		return fEDCTracker;
	}

    /**
	 * @since 2.0
	 */
    public <V> V getService(Class<V> serviceClass) {
    	if (IEDCService.class.isAssignableFrom(serviceClass))
    		return fEDCTracker.getService(serviceClass);
        return getServicesTracker().getService(serviceClass);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#initialize(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		fEDCTracker = new EDCServicesTracker(getBundleContext(), getSession().getId());
		super.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
			@Override
			public void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	@Override
	public void shutdown(RequestMonitor rm) {
		fEDCTracker.dispose();
		fEDCTracker = null;
		super.shutdown(rm);
	}

	protected void doInitialize(RequestMonitor requestMonitor) {
		register(classNames, new Hashtable<String, String>());

		if (targetEnvironmentService == null)
			targetEnvironmentService = getServicesTracker().getService(ITargetEnvironment.class);

		requestMonitor.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#getBundleContext()
	 */
	@Override
	protected BundleContext getBundleContext() {
		return EDCDebugger.getBundleContext();
	}

	public ITargetEnvironment getTargetEnvironmentService() {
		return targetEnvironmentService;
	}

	/**
	 * Add implicit service class names
	 * 
	 * @param classNames
	 *            the class names our derivative gave us. Must not be null, but
	 *            can be empty.
	 * @param implicitClassNames
	 *            the implicit class names that should be specified. Must not be
	 *            null, but can be empty.
	 * @return a new collection of class names that is [classNames] plus any
	 *         missing implicit ones. The implicit ones will appear first in the
	 *         list
	 */
	/*package*/ static String[] massageClassNames(String[] classNames, String[] implicitClassNames) {
		List<String> newClassNames = new ArrayList<String>(Arrays.asList(implicitClassNames));
		for (String className : classNames) {
			if (!newClassNames.contains(className)) {
				newClassNames.add(className);
			}
		}
		return newClassNames.toArray(new String[newClassNames.size()]);
	}
}
