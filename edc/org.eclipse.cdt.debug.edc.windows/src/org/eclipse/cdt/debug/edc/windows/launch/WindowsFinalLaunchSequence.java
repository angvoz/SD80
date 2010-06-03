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
package org.eclipse.cdt.debug.edc.windows.launch;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.edc.IEDCConstants;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.TCFServiceManager;
import org.eclipse.cdt.debug.edc.launch.AbstractFinalLaunchSequence;
import org.eclipse.cdt.debug.edc.launch.ChooseProcessItem;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.ui.console.AbstractLoggingConsoleFactory;
import org.eclipse.cdt.debug.edc.ui.console.DebugProgramOutputConsoleFactory;
import org.eclipse.cdt.debug.edc.windows.RestartCommand;
import org.eclipse.cdt.debug.edc.windows.WindowsDebugger;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.tm.tcf.protocol.IChannel;

public class WindowsFinalLaunchSequence extends AbstractFinalLaunchSequence {

	// logging
	protected Step initLoggingStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {
			TCFServiceManager tcfServiceManager = (TCFServiceManager) EDCDebugger.getDefault().getServiceManager();
			final IChannel channel = tcfServiceManager.getChannelForPeer(getTCFPeer());
			AbstractLoggingConsoleFactory.openConsole(DebugProgramOutputConsoleFactory.CONSOLE_TYPE,
					DebugProgramOutputConsoleFactory.CONSOLE_TITLE, DebugProgramOutputConsoleFactory.LOG_ID,
					channel, true);
			requestMonitor.done();
		}
	};

	// experimental "Restart" command support.
	//
	protected Step initRestartStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {

			TCFServiceManager tcfServiceManager = (TCFServiceManager) EDCDebugger.getDefault().getServiceManager();
			IChannel channel = tcfServiceManager.getChannelForPeer(getTCFPeer());

			launch.getSession().registerModelAdapter(IRestart.class,
					new RestartCommand(launch.getSession(), launch, channel));
			requestMonitor.done();
		}
	};

	public WindowsFinalLaunchSequence(DsfExecutor executor, EDCLaunch launch, IProgressMonitor pm) {
		super(executor, launch, pm, "Configuring Windows Debugger", "Aborting configuring Windows debugger");

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
		steps.add(initLoggingStep);
		steps.add(initRestartStep);
		steps.add(initRegistersServiceStep);
		steps.add(initMemoryServiceStep);
		steps.add(initProcessesServiceStep);
		steps.add(doAttach ? attachStep : launchStep);
		steps.add(cleanupStep);
	}

	@Override
	protected ChooseProcessItem chooseProcess(ChooseProcessItem[] processes, String defaultSelection)
			throws CoreException {
		return WindowsDebugger.chooseProcess(processes);
	}

	@Override
	protected void specifyRequiredPeer() {
		peerAttributes.put(IEDCConstants.PEER_ATTR_DEBUG_SUPPORT, "Win32 Debug API");
	}
}
