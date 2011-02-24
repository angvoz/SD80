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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
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

	/**
	 * EDC services can use this method to execute blocking <i>thread-safe</i>
	 * code without blocking the DSF thread. The runnable is exercised on a
	 * separate thread obtained from a thread pool. This mechanism was
	 * introduced to allow an EDC service's logic to avoid bogging down the DSF
	 * thread in cases where it cannot reasonably avoid making a blocking call.
	 * One example is when a TCF service has to be invoked synchronously (the
	 * calling thread waits for the request monitor to complete). Such things
	 * should not be done on the DSF thread since that thread is meant to be
	 * readily available to handle a queue of requests, much like a UI thread
	 * is.
	 * 
	 * In the event of an uncaught exception in the given code, the request
	 * monitor is automatically completed and given an error status.
	 * 
	 * @throws RejectedExecutionException
	 *             if the thread pool has been overwhelmed and given code cannot
	 *             be scheduled to run
	 * 
	 * @since 2.0
	 */
	protected void asyncExec(Runnable runnable, RequestMonitor rm) {
		try {
			ExecutorService executor = EDCLaunch.getThreadPool(getSession().getId());
			if (executor.isShutdown())
			{
				rm.setStatus(new Status(Status.ERROR, EDCDebugger.PLUGIN_ID, "Session has been shutdown.", null));
				rm.done();
			}
			else
				executor.execute(new SafeRunner(runnable, rm));
		}
		catch (RejectedExecutionException exc) {
			// See EDCLaunch.newThreadPool()
			String msg = Messages.AbstractEDCService_0;
			EDCDebugger.getMessageLogger().log(IStatus.WARNING, msg, exc); 
			rm.setStatus(new Status(Status.ERROR, EDCDebugger.PLUGIN_ID, msg, exc));
			rm.done();
			throw exc;
		}
	}

	/**
	 * A safe runner used by {@link AbstractEDCService#asyncExec(Runnable)} to
	 * ensure an uncaught exception does not leave the request monitor hanging.
	 */
	private class SafeRunner implements Runnable {
		private Runnable fCode;
		private RequestMonitor fRm;

		SafeRunner(Runnable code, RequestMonitor rm) {
			fCode = code;
			fRm = rm;
			Assert.isNotNull(code);
		}

		public void run() {
			try {
				fCode.run();
			} catch (Exception e) {
				handleException(fCode, e);
			} catch (LinkageError e) {
				handleException(fCode, e);
			} catch (AssertionError e) {
				handleException(fCode, e);
			}
		}

		private void handleException(Runnable code, Throwable e) {
			IStatus status;
			if (!(e instanceof OperationCanceledException)) {
				// try to obtain the correct plug-in id for the bundle providing the safe runnable 
				if (e instanceof CoreException) {
					status = new MultiStatus(EDCDebugger.PLUGIN_ID, -1, Messages.AbstractEDCService_1, e);
					((MultiStatus)status).merge(((CoreException) e).getStatus());
				} else {
					status = new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, -1, Messages.AbstractEDCService_2, e);
				}
				EDCDebugger.getMessageLogger().log(status);
			}
			else {
				status = new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, -1, Messages.AbstractEDCService_3, e);
			}
			if (fRm != null) {
				fRm.setStatus(status);
				fRm.done();
			}
		}
	}
}
