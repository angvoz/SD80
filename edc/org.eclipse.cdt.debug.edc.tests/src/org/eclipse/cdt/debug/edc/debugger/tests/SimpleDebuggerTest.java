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

import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public abstract class SimpleDebuggerTest {

	protected DsfSession session;
	protected ExecutionDMC threadDMC;
	protected IFrameDMContext frame;
	protected Expressions expressionsService;
	protected EDCLaunch launch;

	/** Get the id the launch configuation type used by the snapshot, when it
	 * might not be available at runtime.  If non-<code>null</code>, your test should
	 * check launch != <code>null</code> before continuing.
	 * @return launchConfigurationType id, or null if the snapshot uses a standard CDT EDC launcher */
	protected String getRequiredLaunchConfigurationType() {
		return null;
	}
	
	/** Get the id (class name) the TCF agent launcher used by the snapshot, when it
	 * might not be available at runtime.  If non-<code>null</code>, your test should
	 * check launch != <code>null</code> before continuing.
	 * @return org.eclipse.debug.edc.tcfAgentLauncher id, or null if the snapshot uses a host CDT EDC launcher */
	protected String getRequiredTCFAgentLauncher() {
		return null;
	}
	
	@Before
	public void launchAndWaitForSuspendedContext() throws Exception {
		String reqdLauncher = getRequiredLaunchConfigurationType();
		if (reqdLauncher != null) {
			if (!TestUtils.hasLaunchConfiguationType(reqdLauncher)) {
				return;
			}
		}
		reqdLauncher = getRequiredTCFAgentLauncher();
		if (reqdLauncher != null) {
			if (!TestUtils.hasTCFAgentLauncher(reqdLauncher)) {
				return;
			}
		}
		TestUtils.disableDebugPerspectiveSwitchPrompt();
		launch = TestUtils.createLaunchForAlbum(getAlbumName());
		Assert.assertNotNull(launch);
		session = TestUtils.waitForSession(launch);
		Assert.assertNotNull(session);
		IEDCExecutionDMC executionDMC = TestUtils.waitForExecutionDMC(session);
		Assert.assertNotNull(executionDMC);
		threadDMC = TestUtils.waitForSuspendedThread(session);
		Assert.assertNotNull(threadDMC);
		frame = TestUtils.waitForStackFrame(session, threadDMC);
	}
	
	@After
	public void shutdown() {
		TestUtils.shutdownDebugSession(launch, session);
		session = null;
	}
	
	public void openSnapshotAndWaitForSuspendedContext(int index) throws Exception {
		launch.getAlbum().openSnapshot(index);
		session = TestUtils.waitForSession(launch);
		Assert.assertNotNull(session);
		IEDCExecutionDMC executionDMC = TestUtils.waitForExecutionDMC(session);
		Assert.assertNotNull(executionDMC);
		threadDMC = TestUtils.waitForSuspendedThread(session);
		Assert.assertNotNull(threadDMC);
		frame = TestUtils.waitForStackFrame(session, threadDMC);
	}

	abstract public String getAlbumName();

	public String getExpressionValue(String expression) throws Exception {
		return TestUtils.getExpressionValue(session, frame, expression);
	}
}
