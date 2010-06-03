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

import org.eclipse.cdt.debug.edc.internal.formatter.FormatExtensionManager;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.CastInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests of expression evaluation using cast operators.
 * 
 * Additional checks for code where variables are in registers.
 */
public class ExpressionsCasting2 extends BaseExpressionTest {
	private static final String YOU_SHOULD_BE_SEEING_THIS_TEXT = "\"You should be seeing this text!\"";

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.debugger.tests.SimpleDebuggerTest#getRequiredLaunchConfigurationType()
	 */
	@Override
	protected String getRequiredLaunchConfigurationType() {
		return "com.nokia.cdt.debug.launch.systemTRKLaunch";
	}
	

	boolean formatterSetting;
	
	@Before
	public void turnOnFormatter() {
		formatterSetting = FormatExtensionManager.instance().isEnabled();
		FormatExtensionManager.instance().setEnabled(true);
	}
	@After
	public void restoreFormatter() {
		FormatExtensionManager.instance().setEnabled(formatterSetting);
	}
	
	
	@Test
	public void testCastingRegisters() throws Exception {
		if (launch == null) return;
		openSnapshotAndWaitForSuspendedContext(1);
		
		// these variables are in registers
		checkCastedExpr(null, YOU_SHOULD_BE_SEEING_THIS_TEXT, "aArg2", new CastInfo("TPtr16*"));
		checkCastedExpr(null, YOU_SHOULD_BE_SEEING_THIS_TEXT, "aArg3", new CastInfo("TPtr16*"));
		checkCastedExpr(null, YOU_SHOULD_BE_SEEING_THIS_TEXT, "aArg3", new CastInfo("TPtr16&"));
		
		// cast value in register directly to float, don't complain about "& of register"
		checkCastedExpr(null, "5.962985E-39", "aArg2", new CastInfo("float"));
	}
	
	@Test
	public void testCastingArraysInRegisters() throws Exception {
		if (launch == null) return;
		openSnapshotAndWaitForSuspendedContext(1);
		
		// these variables are in registers, don't complain about "& of register"
		CastInfo arrayCast = new CastInfo("TPtr16*", 0, 2);
		checkCastedChildExpr(null, YOU_SHOULD_BE_SEEING_THIS_TEXT, "aArg3", arrayCast, "aArg3[0]");

	}
	
	@Override
	public String getAlbumName() {
		return "RegisterFrameTestsBlackFlagRVCT.dsa";
	}

}
