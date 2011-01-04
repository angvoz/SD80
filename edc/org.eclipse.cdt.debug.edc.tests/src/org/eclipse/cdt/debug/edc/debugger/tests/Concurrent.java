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
package org.eclipse.cdt.debug.edc.debugger.tests;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.Stack;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.After;
import org.junit.Test;

public class Concurrent extends BaseLaunchTest {

	private EDCLaunch launch;
	private DsfSession session;

	@Test
	public void testStackTraces() throws Exception {
		TestUtils.showPerspective("org.eclipse.debug.ui.DebugPerspective");	
		launch = createLaunch();
		assertNotNull(launch);
		session = TestUtils.waitForSession(launch);
		assertNotNull(session);
		final ExecutionDMC executionDMC = TestUtils.waitForSuspendedThread(session);
		assertNotNull(executionDMC);
		Thread.sleep(10 * 1000);
		
		Runnable getFrames = new Runnable() {
			
			public void run() {
				Stack stackService = TestUtils.getService(session, Stack.class);
				try {
					IFrameDMContext[] frames = stackService.getFramesForDMC((ExecutionDMC) executionDMC, 0, IStack.ALL_FRAMES);
					System.out.println("Got frames: " + frames);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			
		};
		
		int testCount = 50;
		while (testCount-- > 0)
			EDCDebugger.execute(getFrames);

	}
	
	@After
	public void shutdown() {
		TestUtils.shutdownDebugSession(launch, session);
	}

	@Override
	protected void configureLaunchConfiguration(
			ILaunchConfigurationWorkingCopy configuration) {

		// Make sure it stop at main
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "main");
	}

}
