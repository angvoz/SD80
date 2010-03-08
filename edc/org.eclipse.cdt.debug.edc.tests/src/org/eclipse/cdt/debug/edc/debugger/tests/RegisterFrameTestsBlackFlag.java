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
 */
public class RegisterFrameTestsBlackFlag extends SimpleDebuggerTest {
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

	@Test
	public void testLength() throws Exception {
		frame = TestUtils.waitForStackFrame(session, threadDMC, 0);
		
		try {
			setFormatter(false);
			assertNumbersEquals("0x7907aee0", getExpressionValue("this"));
		} finally {
			restoreFormatter();
		}
	}
	@Test
	public void testShowConstArguments() throws Exception {
		frame = TestUtils.waitForStackFrame(session, threadDMC, 1);
		try {
			// note: formatter was half-showing decimal and hex at this time
			setFormatter(false);
			assertNumbersEquals("0x40ee38", getExpressionValue("aArg1"));
			assertNumbersEquals("0x40ee38", getExpressionValue("aArg2"));
			assertNumbersEquals("0x40ee38", getExpressionValue("aArg3"));
			assertNumbersEquals("0x40ee38", getExpressionValue("aArg4"));
			assertEquals("31", getExpressionValue("length"));
			assertEquals("31", getExpressionValue("length2"));
			assertEquals("31", getExpressionValue("length3"));
			assertEquals("31", getExpressionValue("length4"));
		} finally {
			restoreFormatter();
		}
	}
	@Test
	public void testShowTPtr() throws Exception {
		frame = TestUtils.waitForStackFrame(session, threadDMC, 2);
		try {
			setFormatter(true);
			assertEquals("\"You should be seeing this text!\"", getExpressionValue("cstr8"));
			assertEquals("\"You should be seeing this text!\"", getExpressionValue("cstr16"));
			assertEquals("\"You should be seeing this text!\"", getExpressionValue("cstr"));
			assertEquals("\"You should be seeing this text!\"", getExpressionValue("ptrC8"));
			assertEquals("\"You should be seeing this text!\"", getExpressionValue("ptrC16"));
			assertEquals("\"You should be seeing this text!\"", getExpressionValue("ptrC"));
			assertEquals("\"You should be seeing this text!\"", getExpressionValue("ptr8"));
			assertEquals("\"You should be seeing this text!\"", getExpressionValue("ptr16"));
			assertEquals("\"You should be seeing this text!\"", getExpressionValue("ptr8p"));
			assertEquals("\"You should be seeing this text!\"", getExpressionValue("ptr16p"));
			assertEquals("\"You should be seeing this text!\"", getExpressionValue("ptrp"));
			
		} finally {
			restoreFormatter();
		}
	}
	
	@Override
	public String getAlbumName() {
		return "RegisterFrameTestsBlackFlag.dsa";
	}

}
