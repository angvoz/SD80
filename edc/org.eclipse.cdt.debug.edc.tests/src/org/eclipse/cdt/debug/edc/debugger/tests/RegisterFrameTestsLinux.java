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
 * Test that we can recover expressions from stack frames other than the TOS.
 * (using Linux/gdbserver binary)
 * <p>
 * This also tests some new DW_OP operands we weren't handling.
 */
public class RegisterFrameTestsLinux extends SimpleDebuggerTest {
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.debugger.tests.SimpleDebuggerTest#getRequiredLaunchConfigurationType()
	 */
	@Override
	protected String getRequiredLaunchConfigurationType() {
		return "org.eclipse.cdt.launch.applicationLaunchType";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.debugger.tests.SimpleDebuggerTest#getRequiredTCFAgentLauncher()
	 */
	@Override
	protected String getRequiredTCFAgentLauncher() {
		return "org.eclipse.cdt.debug.edc.linux.x86.GdbserverAgentDescriptor";
	}
	/*
	 * The "1" series has a breakpoint in a valid function at the top
	 */

	@Test
	public void testProcessFile1() throws Exception {
		if (launch == null) return;
		frame = TestUtils.waitForStackFrame(session, threadDMC, 0);
		assertEquals("0x9861008", getExpressionValue("f"));
		assertEquals("0xbfaf75fd", getExpressionValue("filename"));
		assertEquals("0", getExpressionValue("lines"));
	}
	@Test
	public void testProcessArg1() throws Exception {
		if (launch == null) return;
		frame = TestUtils.waitForStackFrame(session, threadDMC, 1);
		assertEquals("0xbfaf75fd", getExpressionValue("arg"));
		assertEquals("5", getExpressionValue("errors"));
	}
	@Test
	public void testMain1() throws Exception {
		if (launch == null) return;
		frame = TestUtils.waitForStackFrame(session, threadDMC, 2);
		assertEquals("1", getExpressionValue("argc"));
		assertEquals("0xbfaf7234", getExpressionValue("argv"));
		assertEquals("0xbfaf75fd", getExpressionValue("argv[0]"));
		assertEquals("0xbfaf723c", getExpressionValue("envp"));
	}

	@Override
	public String getAlbumName() {
		return "RegisterFrameTestsLinux.dsa";
	}

}
