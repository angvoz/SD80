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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.launch.ServicesLaunchSequence;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotLaunchSequence;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

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
		
		monitor.beginTask("Launching...", 10);
		if (monitor.isCanceled()) {
			return;
		}

		try {
			final EDCLaunch edcLaunch = (EDCLaunch) launch;
			boolean forDebug = mode.equals(ILaunchManager.DEBUG_MODE);

			monitor.worked(1);
			
			if (edcLaunch.isFirstLaunch())
			{
				// First launch for this session, we need to create all of the services
				edcLaunch.setServiceFactory(newServiceFactory());

				if (forDebug) {
					edcLaunch.initializeSnapshotSupport();
				}
				// Create and invoke the launch sequence to create the debug control and
				// services
				IProgressMonitor subMon1 = new SubProgressMonitor(monitor, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
				final ServicesLaunchSequence servicesLaunchSequence = new ServicesLaunchSequence(edcLaunch.getSession(), edcLaunch,
						subMon1);

				edcLaunch.getSession().getExecutor().execute(servicesLaunchSequence);
				try {
					getOrCancelSequence(servicesLaunchSequence, subMon1);
				} catch (InterruptedException e1) {
					throw new DebugException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, DebugException.INTERNAL_ERROR,
							"Interrupted Exception in dispatch thread.\n" + e1.getLocalizedMessage(), e1)); //$NON-NLS-1$
				} catch (CancellationException e) {
					throw new CoreException(Status.CANCEL_STATUS);
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
				if (forDebug) {
					edcLaunch.initializeMemoryRetrieval();
				}

				monitor.worked(1);
				
			}


			// Create and invoke the final launch sequence to setup the debugger
			IProgressMonitor subMon2 = new SubProgressMonitor(monitor, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
			final Sequence finalLaunchSequence = getFinalLaunchSequence(edcLaunch.getSession().getExecutor(), edcLaunch, subMon2);

			edcLaunch.getSession().getExecutor().execute(finalLaunchSequence);
			boolean succeed = false;
			try {
				getOrCancelSequence(finalLaunchSequence, subMon2);
				succeed = true;
			} catch (InterruptedException e1) {
				IStatus exceptionStatus = new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, DebugException.INTERNAL_ERROR,
						"Interrupted Exception in dispatch thread.\n" + e1.getLocalizedMessage(), e1);
				if (edcLaunch.isFirstLaunch())
					throw new DebugException(exceptionStatus); //$NON-NLS-1$
				else
					EDCDebugger.getMessageLogger().log(exceptionStatus);
			} catch (CancellationException e) {
				if (edcLaunch.isFirstLaunch())
					throw new CoreException(Status.CANCEL_STATUS);
			} catch (ExecutionException e1) {
				Throwable cause = e1.getCause();
				if (cause instanceof CoreException) {
					IStatus s = ((CoreException) cause).getStatus();
					if (s.getSeverity() == IStatus.CANCEL && edcLaunch.isFirstLaunch())
						throw (CoreException) cause;
				}
				IStatus errorStatus = EDCDebugger.getMessageLogger().createStatus(IStatus.ERROR, null, e1.getCause());
				if (edcLaunch.isFirstLaunch())
					throw new DebugException(errorStatus);
				else
					EDCDebugger.getMessageLogger().log(errorStatus);
			} finally {
				if (!succeed && edcLaunch.isFirstLaunch()) {
					Query<Object> launchShutdownQuery = new Query<Object>() {
						@Override
						protected void execute(DataRequestMonitor<Object> rm) {
							edcLaunch.shutdownSession(rm);
						}
					};

					edcLaunch.getSession().getExecutor().execute(launchShutdownQuery);

					// Wait for the shutdown to finish. The Query.get() method is a
					// synchronous call which blocks until the query completes.
					try {
						// not cancellable
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
				
				if (!forDebug) {
					// just running, so go ahead and shutdown the session
					edcLaunch.shutdownSession(new RequestMonitor(ImmediateExecutor.getInstance(), null));
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Wait for a sequence to finish, periodically checking whether
	 * it has been cancelled. 
	 * @param sequence
	 * @param monitor
	 * @return the value of the sequence
	 * @throws ExecutionException 
	 * @throws InterruptedException
	 * @throws CancellationException  
	 */
	private Object getOrCancelSequence(Sequence sequence,
			IProgressMonitor monitor) throws InterruptedException, ExecutionException {
		while (!monitor.isCanceled()) {
			try {
				return sequence.get(1, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				// fine, keep looping
			}
		}
		// cancelled
		sequence.cancel(true);  /* flag is ignored */
		throw new CancellationException();
	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		// Need to configure the source locator before creating the launch
		// because once the launch is created and added to launch manager,
		// the adapters will be created for the whole session, including
		// the source lookup adapter.

		EDCLaunch launch = findExistingLaunch(configuration, mode);
		if (launch == null)
		{
			launch = createLaunch(configuration, mode);
			launch.initialize();
			launch.setSourceLocator(launch.createSourceLocator());
		}
		else
		{
			launch.addAffiliatedLaunchConfiguration(configuration);
			launch.setFirstLaunch(false);
		}

		launch.setActiveLaunchConfiguration(configuration);

		return launch;
	}

	/**
	 * @since 2.0
	 */
	abstract public EDCLaunch createLaunch(ILaunchConfiguration configuration,
			String mode);

	private EDCLaunch findExistingLaunch(ILaunchConfiguration configuration,
			String mode) {
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        List<ILaunch> launchList = Arrays.asList(manager.getLaunches());
        
        for (ILaunch iLaunch : launchList) {		
        	if (!iLaunch.isTerminated() && iLaunch instanceof EDCLaunch)
        	{
        		EDCLaunch edcLaunch = (EDCLaunch) iLaunch;
        		if (DsfSession.isSessionActive(edcLaunch.getSession().getId())
        				&& isSameTarget(edcLaunch, configuration, mode))
        			return edcLaunch;
         	}
		}
		return null;
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

	abstract protected IDsfDebugServicesFactory newServiceFactory();
	

	/**
	 * @param existingLaunch 
	 * @since 2.0
	 */
	abstract protected boolean isSameTarget(EDCLaunch existingLaunch, ILaunchConfiguration configuration, String mode);

}
