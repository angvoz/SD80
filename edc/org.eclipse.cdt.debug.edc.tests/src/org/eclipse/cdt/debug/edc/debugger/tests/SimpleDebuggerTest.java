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
package org.eclipse.cdt.debug.edc.debugger.tests;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.DebugException;
import org.junit.Before;

public abstract class SimpleDebuggerTest extends TestCase {

	protected DsfSession session;
	protected ExecutionDMC threadDMC;
	protected IFrameDMContext frame;
	protected Expressions expressionsService;
	protected EDCLaunch launch;

	@Before
	public void launchAndWaitForSuspendedContext() throws Exception {
		TestUtils.disableDebugPerspectiveSwitchPrompt();
		if (launch != null) {
			try {
				launch.terminate();
			} catch (DebugException de) {
			}
			launch = null;
		}
		if (session != null) {
			DsfSession.endSession(session);
			session = null;
		}
		launch = TestUtils.createLaunchForAlbum(getAlbumName());
		Assert.assertNotNull(launch);
		session = TestUtils.waitForSession(launch);
		Assert.assertNotNull(session);
		ExecutionDMC executionDMC = TestUtils.waitForExecutionDMC(session);
		Assert.assertNotNull(executionDMC);
		threadDMC = TestUtils.waitForSuspendedThread(session);
		Assert.assertNotNull(threadDMC);
		frame = TestUtils.waitForStackFrame(session, threadDMC);
	}
	
	public void openSnapshotAndWaitForSuspendedContext(int index) throws Exception {
		launch.getAlbum().openSnapshot(index);
		session = TestUtils.waitForSession(launch);
		Assert.assertNotNull(session);
		ExecutionDMC executionDMC = TestUtils.waitForExecutionDMC(session);
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
