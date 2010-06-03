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
package org.eclipse.cdt.debug.edc.windows;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.DebugException;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.services.IProcesses;

/**
 * This is work underway.
 */
public class RestartCommand implements IRestart {
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;
	private final EDCLaunch fLaunch;
	private IChannel fChannel;

	public RestartCommand(DsfSession session, EDCLaunch launch, IChannel channel) {
		fExecutor = session.getExecutor();
		fLaunch = launch;
		fChannel = channel;
		fTracker = new DsfServicesTracker(EDCDebugger.getDefault().getBundle().getBundleContext(), session.getId());
	}

	public void dispose() {
		fTracker.dispose();
	}

	// Run control may not be available after a connection is terminated and
	// shut down.
	public boolean canRestart() {
		return true;
	}

	public void restart() throws DebugException {
		Query<Object> restartQuery = new Query<Object>() {
			@Override
			protected void execute(final DataRequestMonitor<Object> rm) {
				final IProcesses tcfProcesses = (IProcesses) fChannel.getRemoteService(IProcesses.NAME);
				final RunControl dsfRC = (RunControl) fTracker.getService(IRunControl.class);

				if (tcfProcesses != null && dsfRC != null) {

					// XXX: hack with predefined contextID.
					dsfRC.getRootDMC().terminate(new RequestMonitor(fExecutor, rm) {
						@Override
						protected void handleSuccess() {
							// Note that requestMonitor is passed down, so don't
							// call requestMonitor.done() here !
							WindowsDebugger.getDefault().launchProcess(fLaunch, tcfProcesses, rm);
						}
					});
				} else {
					rm.done();
				}
			}
		};

		fExecutor.execute(restartQuery);
		try {
			restartQuery.get();
		} catch (InterruptedException e1) {
			// TODO Is there some reason this is not being handled?
		} catch (ExecutionException e1) {
			// TODO Is there some reason this is not being handled?
		}
	}
}
