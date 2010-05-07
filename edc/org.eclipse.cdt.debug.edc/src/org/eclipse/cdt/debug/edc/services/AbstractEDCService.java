/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.services;

import java.util.Hashtable;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.osgi.framework.BundleContext;

/**
 * This is abstract DSF service with some APIs specific to EDC.
 */
public abstract class AbstractEDCService extends AbstractDsfService {

	private final String[] classNames;
	private ITargetEnvironment targetEnvironmentService = null;
	private final boolean snapshot;

	public AbstractEDCService(DsfSession session, String[] classNames) {
		super(session);
		this.classNames = classNames;
		this.snapshot = Album.isSnapshotSession(session.getId());
	}

	public boolean isSnapshot() {
		return snapshot;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#initialize(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
			@Override
			public void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
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
}
