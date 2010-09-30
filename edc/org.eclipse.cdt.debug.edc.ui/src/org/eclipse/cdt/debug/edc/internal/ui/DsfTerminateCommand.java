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
package org.eclipse.cdt.debug.edc.internal.ui;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfCommandRunnable;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.ITerminateHandler;

public class DsfTerminateCommand implements ITerminateHandler {
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	public DsfTerminateCommand(DsfSession session) {
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker(EDCDebugUI.getBundleContext(), session.getId());
	}

	public void dispose() {
		fTracker.dispose();
	}

	// Run control may not be avilable after a connection is terminated and shut
	// down.
	public void canExecute(final IEnabledStateRequest request) {
		if (request.getElements().length != 1 || !(request.getElements()[0] instanceof IDMVMContext)) {
			request.setEnabled(false);
			request.done();
			return;
		}

		// Javac doesn't like the cast to
		// "(AbstractDMVMLayoutNode<?>.DMVMContext)" need to use the
		// construct below and suppress warnings.
		IDMVMContext vmc = (IDMVMContext) request.getElements()[0];
		final IThreadDMContext dmc = DMContexts.getAncestorOfType(vmc.getDMContext(), IThreadDMContext.class);
		if (dmc == null) {
			request.setEnabled(false);
			request.done();
			return;
		}

		fExecutor.execute(new DsfRunnable() {
			public void run() {
				// Get the processes service and the exec context.
				IProcesses processesService = fTracker.getService(IProcesses.class);
				if (processesService == null || dmc == null) {
					// Context or service already invalid.
					request.setEnabled(false);
					request.done();
				} else {
					processesService.canTerminate(dmc, new DataRequestMonitor<Boolean>(ImmediateExecutor.getInstance(),
							null) {
						@Override
						protected void handleCompleted() {
							if (isSuccess()) {
								Boolean result = getData();
								assert result != null : "successful request should provide a result"; 
								request.setEnabled(result != null ? result : false);
							}
							else {
								request.setEnabled(false);
							}
							request.done();
						}
					});
				}
			}
		});

	}

	public boolean execute(final IDebugCommandRequest request) {
		if (request.getElements().length != 1) {
			request.done();
			return false;
		}

		IDMVMContext vmc = (IDMVMContext) request.getElements()[0];
		final IThreadDMContext dmc = DMContexts.getAncestorOfType(vmc.getDMContext(), IThreadDMContext.class);

		fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) {
			@Override
			public void doExecute() {
				IProcesses processesService = fTracker.getService(IProcesses.class);
				processesService.terminate(dmc, new RequestMonitor(fExecutor, null));
			}
		});
		return false;
	}

}
