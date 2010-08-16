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
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.util.Map;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.debug.edc.services.IDSFServiceUsingTCF;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.tcf.extension.ProtocolConstants;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IProcesses.ProcessContext;

public class Processes extends AbstractEDCService implements IProcesses, IEventListener, IDSFServiceUsingTCF {

	private org.eclipse.tm.tcf.services.IProcesses tcfProcessesService;
	
	/*
	 * The data of a corresponding thread or process.
	 */
	@Immutable
	protected static class ExecutionDMData implements IThreadDMData {
		String name = "unknown";
		String id = "unknown";

		public ExecutionDMData(ExecutionDMC dmc) {
			id = (String) dmc.getProperty(ProtocolConstants.PROP_OS_ID);
			name = (String) dmc.getProperty(IEDCDMContext.PROP_NAME);
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public boolean isDebuggerAttached() {
			return true;
		}
	}

	public Processes(DsfSession session) {
		super(session, new String[] { IProcesses.class.getName(), Processes.class.getName() });
	}

	@Override
	protected void doInitialize(RequestMonitor requestMonitor) {
		super.doInitialize(requestMonitor);
		getSession().addServiceEventListener(this, null);
	}

	public void attachDebuggerToProcess(IProcessDMContext procCtx, DataRequestMonitor<IDMContext> rm) {
		rm.done();
	}

	public void canDetachDebuggerFromProcess(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
		ExecutionDMC edcDMC = (ExecutionDMC) dmc;
		rm.setData(edcDMC.canDetach());
		rm.done();
	}

	public void canTerminate(IThreadDMContext thread, DataRequestMonitor<Boolean> rm) {
		ExecutionDMC executionDmc = (ExecutionDMC) thread;
		rm.setData(executionDmc.canTerminate());
		rm.done();
	}

	public void debugNewProcess(IDMContext dmc, String file, Map<String, Object> attributes,
			DataRequestMonitor<IDMContext> rm) {
		rm.done();
	}

	/**
	 * Detach debugger from all processes in the debug session.
	 * @param rm
	 */
	public void detachDebuggerFromSession(final RequestMonitor rm) {
		RunControl rcService = getServicesTracker().getService(RunControl.class);
		ExecutionDMC[] processes = rcService.getRootDMC().getChildren();
		
		CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), rm);
		
		crm.setDoneCount(processes.length);
		
		for (ExecutionDMC p : processes)
			detachDebuggerFromProcess(p, crm);
	}
	
	public void detachDebuggerFromProcess(final IDMContext exeDmc, final RequestMonitor rm) {
		/*
		 * 1. Remove all breakpoints for all modules in the process.
		 * 2. Resume the process.
		 * 3. Detach the process from agent and host debugger.
		 */
		
		// Make sure detach from the process, not just a thread.
		final IProcessDMContext dmc = DMContexts.getAncestorOfType(exeDmc, IProcessDMContext.class);

		final BreakpointsMediator2 bmService = getServicesTracker().getService(BreakpointsMediator2.class);
		if (bmService == null) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "Failed to get BreakpointsMediator2 service."));
			rm.done();
			return;
		}
		IModules modulesService = getServicesTracker().getService(IModules.class);
		if (modulesService == null) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "Failed to get Modules service."));
			rm.done();
			return;
		}

		ISymbolDMContext symCtx = DMContexts.getAncestorOfType(dmc, ISymbolDMContext.class);
		modulesService.getModules(symCtx, new DataRequestMonitor<IModules.IModuleDMContext[]>(getExecutor(), rm) {

			@Override
			protected void handleCompleted() {
				if (! isSuccess())
					super.handleCompleted();
				else {
					IModuleDMContext[] processModules = getData();
					
					CountingRequestMonitor bpRemovedCRM = new CountingRequestMonitor(getExecutor(), rm) {

						@Override
						protected void handleCompleted() {
							if (! isSuccess()) {
								super.handleCompleted();
								return;
							}
							
							// Now resume the process
							((ExecutionDMC)dmc).resume(new RequestMonitor(getExecutor(), rm){

								@Override
								protected void handleCompleted() {
									if (!isSuccess())
										super.handleCompleted();
									else {
										doDetachDebugger((ExecutionDMC)dmc, rm);
									}
								}
							});
						}
					}; 
					
					int bpTargetsDMCCnt = 0;
					for (IModuleDMContext m : processModules) {
						if (m instanceof IBreakpointsTargetDMContext) {
							// In EDC, each Module is a BpTargetsDMC.
							bpTargetsDMCCnt++;
							bmService.stopTrackingBreakpoints((IBreakpointsTargetDMContext)m, bpRemovedCRM);
						}
					}
					assert bpTargetsDMCCnt > 0;
					
					bpRemovedCRM.setDoneCount(bpTargetsDMCCnt);
				}
			}});
	}

	/**
	 * ask debugger to do final detach: forget the process.
	 * 
	 * @param dmc
	 * @param rm
	 */
	protected void doDetachDebugger(final ExecutionDMC dmc, final RequestMonitor rm) {
		// First detach agent so that the program won't die when the agent dies.
		// Then detach host debugger.
		//
		Protocol.invokeLater(new Runnable() {

			public void run() {
				tcfProcessesService.getContext(dmc.getID(), new org.eclipse.tm.tcf.services.IProcesses.DoneGetContext() {
					
					public void doneGetContext(IToken token, Exception error,
							ProcessContext context) {
						if (error != null) {
							rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "Fail to get TCF context for process: " + dmc.getID(), error));
							rm.done();
						}
						else {
							context.detach(new org.eclipse.tm.tcf.services.IProcesses.DoneCommand() {
								
								public void doneCommand(IToken token, Exception error) {
									if (error != null)
										rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "Fail to detach process \"" + dmc.getID() + "\" from TCF agent.", error));
									else {
										// everything ok, now detach from host debugger,
										// which will shutdown the debug session if the process
										// is the last one.
										dmc.detach();
									}
									rm.done();
								}
							});
						}
					}
				});
				
			}});
	}

	public void getDebuggingContext(IThreadDMContext dmc, DataRequestMonitor<IDMContext> rm) {
		rm.done();
	}

	public void getExecutionData(IThreadDMContext dmc, DataRequestMonitor<IThreadDMData> rm) {
		if (dmc instanceof IEDCExecutionDMC)
			rm.setData(new ExecutionDMData((ExecutionDMC) dmc));
		rm.done();
	}

	public void getProcessesBeingDebugged(IDMContext dmc, DataRequestMonitor<IDMContext[]> rm) {
		rm.setData(new IDMContext[0]);
		DsfServicesTracker tracker = getServicesTracker();
		if (tracker != null) {
			RunControl runcontrol = tracker.getService(RunControl.class);
			if (runcontrol != null) {
				IDMContext[] processes = runcontrol.getRootDMC().getChildren();
				rm.setData(processes);
			}
		}
		rm.done();
	}

	public void getRunningProcesses(IDMContext dmc, DataRequestMonitor<IProcessDMContext[]> rm) {
		rm.done();
	}

	public void isDebugNewProcessSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
		rm.done();
	}

	public void isDebuggerAttachSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
		rm.done();
	}

	public void isRunNewProcessSupported(IDMContext dmc, DataRequestMonitor<Boolean> rm) {
		rm.done();
	}

	public void runNewProcess(IDMContext dmc, String file, Map<String, Object> attributes,
			DataRequestMonitor<IProcessDMContext> rm) {
		rm.done();
	}

	public void terminate(IThreadDMContext thread, RequestMonitor requestMonitor) {
		ExecutionDMC executionDmc = (ExecutionDMC) thread;
		executionDmc.terminate(requestMonitor);
	}

	public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
		rm.done();
	}

	// Event handler when a thread or a threadGroup exits
	@DsfServiceEventHandler
	public void eventDispatched(IExitedDMEvent e) {
	}

	public void eventReceived(Object output) {
	}

	public void tcfServiceReady(IService service) {
		assert service instanceof org.eclipse.tm.tcf.services.IProcesses;
		tcfProcessesService = (org.eclipse.tm.tcf.services.IProcesses) service;
	}
}
