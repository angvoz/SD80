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

import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Variables extends SimpleDebuggerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	/*
	Note: This assumes you are at a breakpoint at this point in a function:
	
	volatile int SizeOfInt = sizeof (int);
	#if SizeOfInt == 2
	volatile long int lint;
	#else
	volatile int lint;
	#endif
	volatile char lchar;
	volatile float lfloat;
	volatile double ldouble;
	volatile long llong;
	volatile UINT luint;
	volatile char *lstring;
	volatile char larray[8] = "testing";	
	   
	lint = 1024;			 
	lchar = 'a';			
	lfloat = 55.55f;		
	ldouble = 222.222;		

	llong = 123456789;
-->	luint = 256;			
	 */
	
	@Test
	public void testLocalVariables() throws Exception {
		launchAndWaitForSuspendedContext();
		
		Assert.assertEquals("4", TestUtils.getExpressionValue(session, frame, "SizeOfInt"));
		Assert.assertEquals("1024", TestUtils.getExpressionValue(session, frame, "lint"));
		Assert.assertEquals("'a'", TestUtils.getExpressionValue(session, frame, "lchar"));
		Assert.assertEquals("55.55", TestUtils.getExpressionValue(session, frame, "lfloat"));
		Assert.assertEquals("222.222", TestUtils.getExpressionValue(session, frame, "ldouble"));
		Assert.assertEquals("123456789", TestUtils.getExpressionValue(session, frame, "llong"));
		// assertEquals("0x46e034", getVariableValue("lstring"));
		// assertEquals("104", getVariableValue("*lstring"));
		// assertEquals("2293536", getVariableValue("larray"));
		// assertEquals("2293536", getVariableValue("&larray"));
		// assertEquals("116", getVariableValue("larray[0]"));
		// assertEquals("0", getVariableValue("larray[7]"));
	}

	@Override
	public String getAlbumName() {
		return "Variables.dsa";
	}
}
