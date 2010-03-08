/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.debugger.tests;

import static org.junit.Assert.*;

import org.eclipse.cdt.debug.edc.internal.formatter.FormatExtensionManager;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test that we can recover expressions from stack frames other than the TOS
 */
public class RegisterFrameTests extends SimpleDebuggerTest {
	boolean formatterSetting;
	
	@Before
	public void turnOffFormatter() {
		formatterSetting = FormatExtensionManager.instance().isEnabled();
		FormatExtensionManager.instance().setEnabled(false);
	}
	@After
	public void restoreFormatter() {
		FormatExtensionManager.instance().setEnabled(formatterSetting);
	}

	/*
	 * The "1" series has a breakpoint in a valid function at the top
	 */

	@Test
	public void testProcessFile1() throws Exception {
		frame = TestUtils.waitForStackFrame(session, threadDMC, 0);
		assertEquals("0x3d3ea5", getExpressionValue("filename"));
		assertEquals("8", getExpressionValue("lines"));
	}
	@Test
	public void testProcessArg1() throws Exception {
		frame = TestUtils.waitForStackFrame(session, threadDMC, 1);
		assertEquals("0x3d3ea5", getExpressionValue("arg"));
		assertEquals("5", getExpressionValue("errors"));
	}
	@Test
	public void testMain1() throws Exception {
		frame = TestUtils.waitForStackFrame(session, threadDMC, 2);
		assertEquals("4", getExpressionValue("argc"));
		assertEquals("0x3d3fd8", getExpressionValue("argv"));
		assertEquals("0x3d2c48", getExpressionValue("envp"));
	}


	/*
	 * The "2" series has a breakpoint in msvcrt.dll inside a function
	 * with no assembly.
	 * We need to be able to fetch EBP ($R5) from the preserved
	 * registers in order to reconcile the variables in the frames.
	 */
	
	@Test
	public void testProcessFile2() throws Exception {
		openSnapshotAndWaitForSuspendedContext(1);
		frame = TestUtils.waitForStackFrame(session, threadDMC, 1);
		assertEquals("0x3d3ea5", getExpressionValue("filename"));
		assertEquals("8", getExpressionValue("lines"));
		
		openSnapshotAndWaitForSuspendedContext(2);
		frame = TestUtils.waitForStackFrame(session, threadDMC, 1);
		assertEquals("0x3d3ea5", getExpressionValue("filename"));
		assertEquals("8", getExpressionValue("lines"));

	}
	@Test
	public void testProcessArg2() throws Exception {
		openSnapshotAndWaitForSuspendedContext(1);
		frame = TestUtils.waitForStackFrame(session, threadDMC, 2);
		assertEquals("0x3d3ea5", getExpressionValue("arg"));
		assertEquals("5", getExpressionValue("errors"));
		
		openSnapshotAndWaitForSuspendedContext(2);
		frame = TestUtils.waitForStackFrame(session, threadDMC, 2);
		assertEquals("0x3d3ea5", getExpressionValue("arg"));
		assertEquals("5", getExpressionValue("errors"));
	}
	@Test
	public void testMain2() throws Exception {
		openSnapshotAndWaitForSuspendedContext(1);
		frame = TestUtils.waitForStackFrame(session, threadDMC, 3);
		assertEquals("4", getExpressionValue("argc"));
		assertEquals("0x3d3fd8", getExpressionValue("argv"));
		assertEquals("0x3d2c48", getExpressionValue("envp"));
		
		openSnapshotAndWaitForSuspendedContext(2);
		frame = TestUtils.waitForStackFrame(session, threadDMC, 3);
		assertEquals("4", getExpressionValue("argc"));
		assertEquals("0x3d3fd8", getExpressionValue("argv"));
		assertEquals("0x3d2c48", getExpressionValue("envp"));
	}
	
	@Override
	public String getAlbumName() {
		return "RegisterFrameTests.dsa";
	}

}
