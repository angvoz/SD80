/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui.actions;

import org.eclipse.cdt.debug.edc.internal.ui.EDCDebugUI;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfCommandRunnable;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

public class EDCDisconnectCommand implements IDisconnectHandler {
    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    
    public EDCDisconnectCommand(DsfSession session) {
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(EDCDebugUI.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }

    public void canExecute(final IEnabledStateRequest request) {
        if (request.getElements().length != 1) {
            request.setEnabled(false);
            request.done();
            return;
        }

        fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) { 
            @Override public void doExecute() {
                IProcessDMContext processDmc = DMContexts.getAncestorOfType(getContext(), IProcessDMContext.class);
                IProcesses procService = getProcessService();

                if (procService != null) {
                	procService.canDetachDebuggerFromProcess(
                			processDmc,
                			new DataRequestMonitor<Boolean>(fExecutor, null) {
                				@Override
                				protected void handleCompleted() {
                					request.setEnabled(isSuccess() && getData());
                					request.done();
                				}
                			});
                } else {
                	request.setEnabled(false);
					request.done();
       			}
            }
        });
	}

	public boolean execute(final IDebugCommandRequest request) {
        if (request.getElements().length != 1) {
            request.done();
            return false;
        }

    	fExecutor.submit(new DsfCommandRunnable(fTracker, request.getElements()[0], request) { 
            @Override public void doExecute() {
                IProcessDMContext processDMC = DMContexts.getAncestorOfType(getContext(), IProcessDMContext.class);
                IProcesses procService = getProcessService();

                if (procService != null) {
                	procService.detachDebuggerFromProcess(processDMC, new RequestMonitor(fExecutor, null));
                }
            }
        });
		return false;
	}    
}
