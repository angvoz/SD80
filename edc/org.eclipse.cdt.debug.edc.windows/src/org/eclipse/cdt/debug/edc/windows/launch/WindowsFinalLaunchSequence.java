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
package org.eclipse.cdt.debug.edc.windows.launch;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.ITCFAgentLauncher;
import org.eclipse.cdt.debug.edc.internal.TCFServiceManager;
import org.eclipse.cdt.debug.edc.launch.AbstractFinalLaunchSequence;
import org.eclipse.cdt.debug.edc.launch.ChooseProcessItem;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ILogging;
import org.eclipse.cdt.debug.edc.ui.console.AbstractLoggingConsoleFactory;
import org.eclipse.cdt.debug.edc.windows.RestartCommand;
import org.eclipse.cdt.debug.edc.windows.WindowsDebugger;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.services.IRunControl;

public class WindowsFinalLaunchSequence extends AbstractFinalLaunchSequence {

	// logging
	protected Step initLoggingStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {
			try {
				IPeer agent = getTCFAgent(ILogging.NAME, agentAttributes);
				TCFServiceManager tcfServiceManager = (TCFServiceManager) EDCDebugger.getDefault().getServiceManager();
				final IChannel channel = tcfServiceManager.getChannelForPeer(agent);
				AbstractLoggingConsoleFactory.openConsole(WindowsProgramOutputConsoleFactory.CONSOLE_TYPE,
						WindowsProgramOutputConsoleFactory.CONSOLE_TITLE, WindowsProgramOutputConsoleFactory.LOG_ID,
						channel, true);
			} catch (CoreException e) {
				WindowsDebugger.getMessageLogger().log(e.getStatus()); // log
																		// and
																		// move
																		// on
			} finally {
				requestMonitor.done();
			}
		}
	};

	// experimental "Restart" command support.
	//
	protected Step initRestartStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {

			IPeer agent;
			try {
				agent = getTCFAgent(IRunControl.NAME, agentAttributes);
			} catch (CoreException e1) {
				requestMonitor.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID,
						"Fail to initialize for Restart support. Reason: " + e1.getLocalizedMessage()));
				requestMonitor.done();
				return;
			}

			TCFServiceManager tcfServiceManager = (TCFServiceManager) EDCDebugger.getDefault().getServiceManager();
			IChannel channel = tcfServiceManager.getChannelForPeer(agent);

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
		steps.add(initRunControlStep);
		steps.add(initLoggingStep);
		steps.add(initRestartStep);
		steps.add(initRegistersServiceStep);
		steps.add(initMemoryServiceStep);
		steps.add(doAttach ? attachStep : launchStep);
		steps.add(cleanupStep);
	}

	@Override
	protected ChooseProcessItem chooseProcess(ChooseProcessItem[] processes, String defaultSelection)
			throws CoreException {
		return WindowsDebugger.chooseProcess(processes);
	}

	@Override
	protected void specifyRequiredAgent() {
		agentAttributes.put(ITCFAgentLauncher.DEBUG_SUPPORT, "Win32 Debug API");
	}
}
