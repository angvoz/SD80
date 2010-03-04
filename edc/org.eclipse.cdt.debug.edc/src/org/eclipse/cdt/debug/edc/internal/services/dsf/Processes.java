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
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.util.Map;

import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.tcf.extension.ProtocolConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;

public class Processes extends AbstractEDCService implements IProcesses, IEventListener {

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

	public void detachDebuggerFromProcess(IDMContext dmc, RequestMonitor requestMonitor) {
		requestMonitor.done();
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

}
