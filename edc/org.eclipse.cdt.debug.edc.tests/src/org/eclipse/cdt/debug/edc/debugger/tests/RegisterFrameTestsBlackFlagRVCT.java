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
import org.junit.Test;

/**
 * Test that we can recover expressions from stack frames other than the TOS
 * 
 * (actual case in bug 304040)
 * 
 * Handle broken DWARF frame register format in RVCT
 */
public class RegisterFrameTestsBlackFlagRVCT extends SimpleDebuggerTest {
	/**
	 * 
	 */
	private static final String YOU_SHOULD_BE_SEEING_THIS_TEXT = "\"You should be seeing this text!\"";
	boolean formatterSetting;
	
	public void setFormatter(boolean enable) {
		formatterSetting = FormatExtensionManager.instance().isEnabled();
		FormatExtensionManager.instance().setEnabled(enable);
	}
	public void restoreFormatter() {
		FormatExtensionManager.instance().setEnabled(formatterSetting);
	}

	/** account for patchy decimal vs. hex outputs */
	private void assertNumbersEquals(String exp, String value) {
		try {
			long expl, valuel;
			if (exp.startsWith("0x"))
				expl = Long.valueOf(exp.substring(2), 16);
			else
				expl = Long.valueOf(exp);
			if (value.startsWith("0x"))
				valuel = Long.valueOf(value.substring(2), 16);
			else
				valuel = Long.valueOf(value);
			assertEquals(value, expl, valuel);
		} catch (NumberFormatException e) {
			// fail naturally
			assertEquals(exp, value);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.debugger.tests.SimpleDebuggerTest#getRequiredLaunchConfigurationType()
	 */
	@Override
	protected String getRequiredLaunchConfigurationType() {
		return "com.nokia.cdt.debug.launch.systemTRKLaunch";
	}
	
	protected void doTestStringFormatting() throws Exception {
		try {
			setFormatter(true);
			assertEquals(YOU_SHOULD_BE_SEEING_THIS_TEXT, getExpressionValue("cstr8"));
			assertEquals(YOU_SHOULD_BE_SEEING_THIS_TEXT, getExpressionValue("cstr16"));
			assertEquals(YOU_SHOULD_BE_SEEING_THIS_TEXT, getExpressionValue("cstr"));
			assertEquals(YOU_SHOULD_BE_SEEING_THIS_TEXT, getExpressionValue("ptrC8"));
			assertEquals(YOU_SHOULD_BE_SEEING_THIS_TEXT, getExpressionValue("ptrC16"));
			assertEquals(YOU_SHOULD_BE_SEEING_THIS_TEXT, getExpressionValue("ptrC"));
			assertEquals(YOU_SHOULD_BE_SEEING_THIS_TEXT, getExpressionValue("ptr8"));
			assertEquals(YOU_SHOULD_BE_SEEING_THIS_TEXT, getExpressionValue("ptr16"));
			assertEquals(YOU_SHOULD_BE_SEEING_THIS_TEXT, getExpressionValue("ptr8p"));
			assertEquals(YOU_SHOULD_BE_SEEING_THIS_TEXT, getExpressionValue("ptr16p"));
			assertEquals(YOU_SHOULD_BE_SEEING_THIS_TEXT, getExpressionValue("ptrp"));
			
		} finally {
			restoreFormatter();
		}
	}
	
	@Test
	public void testBaseFunction() throws Exception {
		if (launch == null) return;
		doTestStringFormatting();
	}
	@Test
	public void testShowConstArguments1() throws Exception {
		if (launch == null) return;
		openSnapshotAndWaitForSuspendedContext(1);
		frame = TestUtils.waitForStackFrame(session, threadDMC, 1);
		doTestStringFormatting();
	}
	@Test
	public void testShowConstArguments2() throws Exception {
		if (launch == null) return;
		openSnapshotAndWaitForSuspendedContext(2);
		frame = TestUtils.waitForStackFrame(session, threadDMC, 1);
		doTestStringFormatting();
		
	}
	@Test
	public void testShowConstArguments3() throws Exception {
		if (launch == null) return;
		openSnapshotAndWaitForSuspendedContext(3);
		frame = TestUtils.waitForStackFrame(session, threadDMC, 1);
		doTestStringFormatting();
	}
	
	@Test
	public void testShowConstArguments4() throws Exception {
		if (launch == null) return;
		openSnapshotAndWaitForSuspendedContext(4);
		frame = TestUtils.waitForStackFrame(session, threadDMC, 1);
		doTestStringFormatting();
	}
	
	@Test
	public void testShowConstArguments5() throws Exception {
		if (launch == null) return;
		openSnapshotAndWaitForSuspendedContext(4);

		try {
			setFormatter(false);
			assertNumbersEquals("31", getExpressionValue("length"));
			assertNumbersEquals("31", getExpressionValue("length2"));
			assertNumbersEquals("31", getExpressionValue("length3"));
			assertNumbersEquals("31", getExpressionValue("length4"));
		} finally {
			restoreFormatter();
		}
	}
	
	@Test
	public void testShowE32Main() throws Exception {
		if (launch == null) return;
		openSnapshotAndWaitForSuspendedContext(4);
		frame = TestUtils.waitForStackFrame(session, threadDMC, 5);
		try {
			setFormatter(false);
			assertNumbersEquals("0", getExpressionValue("error"));
		} finally {
			restoreFormatter();
		}
	}
	
	@Override
	public String getAlbumName() {
		return "RegisterFrameTestsBlackFlagRVCT.dsa";
	}

}
