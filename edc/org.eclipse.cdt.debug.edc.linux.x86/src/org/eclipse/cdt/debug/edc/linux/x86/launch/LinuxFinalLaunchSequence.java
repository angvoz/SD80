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
package org.eclipse.cdt.debug.edc.linux.x86.launch;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.IEDCConstants;
import org.eclipse.cdt.debug.edc.launch.AbstractFinalLaunchSequence;
import org.eclipse.cdt.debug.edc.launch.ChooseProcessItem;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.linux.x86.LinuxDebugger;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IProcesses;

public class LinuxFinalLaunchSequence extends AbstractFinalLaunchSequence {

	public LinuxFinalLaunchSequence(DsfExecutor executor, EDCLaunch launch, IProgressMonitor pm) {
		super(executor, launch, pm, "Configuring Linux Debugger", "Aborting configuring Linux debugger");

		boolean doAttach;

		ILaunchConfiguration config = launch.getLaunchConfiguration();
		doAttach = false;
		try {
			String debugMode = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
				doAttach = true;
			}
		} catch (CoreException e) {
		}

		steps.add(trackerStep);
		steps.add(initFindPeerStep);
		steps.add(initRunControlStep);
		steps.add(initRegistersServiceStep);
		steps.add(initMemoryServiceStep);
		steps.add(initProcessesServiceStep);
		steps.add(doAttach ? attachStep : launchStep);
		steps.add(cleanupStep);
	}

	@Override
	protected ChooseProcessItem chooseProcess(ChooseProcessItem[] processes, String defaultSelection)
			throws CoreException {
		return LinuxDebugger.chooseProcess(processes);
	}

	@Override
	protected void specifyRequiredPeer() {
		peerAttributes.put(IEDCConstants.PEER_ATTR_DEBUG_SUPPORT, "GDB Remote Protocol");
	}

	/*
	 * Special override for linux: 1. Get process list using CDT process lister
	 * instead of from gdbserver agent that has no process lister yet. 2. Use
	 * ProcessesService.start(... attach=true...) API instead of
	 * Context.attach() to perform the attach. TODO: get rid of this after
	 * gdbserver agent supports process lister ............ 10/26/09
	 */
	@Override
	public void attachProcess(EDCLaunch launch, final IProcesses tcfProcService, final RequestMonitor requestMonitor) {

		try {
			// 1) get process list
			IProcessInfo[] processes = getProcessList();
			int numProcesses = processes.length;
			// 2) get contexts for each ID
			ChooseProcessItem[] items = null;

			items = new ChooseProcessItem[numProcesses];

			for (int i = 0; i < numProcesses; i++) {
				items[i] = new ChooseProcessItem(Integer.toString(processes[i].getPid()), processes[i].getName());
			}

			// 3) bring up dialog to choose which process
			ChooseProcessItem selected = chooseProcess(items, "");
			int selectedIndex = 0;
			for (selectedIndex = 0; selectedIndex < numProcesses; selectedIndex++) {
				if (selected.processID.equals(items[selectedIndex].processID))
					break;
			}

			final IProcessInfo proc = processes[selectedIndex];

			// 4) attach
			Protocol.invokeLater(new Runnable() {
				public void run() {
					tcfProcService.start(Integer.toString(proc.getPid()), proc.getName(), new String[] { "" }, null,
							true, new IProcesses.DoneStart() {
								public void doneStart(IToken token, Exception error, IProcesses.ProcessContext process) {
									if (error != null) {
										requestMonitor.setStatus(new Status(IStatus.ERROR, LinuxDebugger
												.getUniqueIdentifier(), error.getLocalizedMessage(), error));
									}

									requestMonitor.done();
								}
							});
				}
			});

		} catch (CoreException e) {
			requestMonitor.setStatus(new Status(IStatus.ERROR, LinuxDebugger.getUniqueIdentifier(), e.getMessage()));
			requestMonitor.done();
			return;
		}
	}

	/**
	 * Get process list on local host using CDT process lister.
	 * 
	 * @return
	 */
	private IProcessInfo[] getProcessList() {
		IProcessList processList = null;
		try {
			processList = CCorePlugin.getDefault().getProcessList();
		} catch (CoreException exc) {
			// ignored on purpose
		}
		if (processList != null)
			return processList.getProcessList();

		return null;
	}
}
