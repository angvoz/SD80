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
 */
public class ExpressionsCasting extends BaseExpressionTest {

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
	
	@Test
	public void testCastingWithoutVariables() throws Exception {
		
		checkExpr(signedCharType, "98 ('b')", "(signed char) 98");
		checkExpr(wcharType, "0x3039 (L'\u3039')", "(wchar_t) 0x3039");
		checkExpr(signedCharType, "1 ('\\001')", "(char)0x10001"); 
		checkExpr(shortType, "1", "(short)0x10001"); 
		checkExpr(shortType, "-1", "(short)(signed char)0xff"); 
		
		// cast of value is just a cast
		checkExpr(floatType, "1.42606336E9", "(float)0x55000000");
		
		// need reinterpret otherwise.  This works without having memory locations.
		checkExpr(floatType, "-128.0", "reinterpret_cast<float>(0xC3000000)"); 
	}
	
	@Test
	public void testCastPrecedence() throws Exception {
		// there was a problem here
		checkExpr(floatType, "0.5", "(float)1 / 2"); 
	}
	
	@Test
	public void testCastingClassesBasic() throws Exception {
		openSnapshotAndWaitForSuspendedContext(0);
		
		checkExpr(intType, "32", "((Der1*)der1)->b1");
		checkExpr(intType, "48", "((Der2*)der2)->b2");
		
	}

	@Test
	public void testCastingClassesInUI1() throws Exception {
		openSnapshotAndWaitForSuspendedContext(0);
		
		checkCastedChildExpr(null, "32", "der1", new CastInfo("Der1*"), "b1");
		checkCastedChildExpr(null, "48", "der2", new CastInfo("Der2 *"), "b2");
		
		// Nokia bug 10979: make sure we can cast primitive or struct types to array 
		CastInfo arrayCast = new CastInfo(0, 5);
		checkCastedChildExpr(null, "0x3d3ef8", "*b", arrayCast, "*b[0]");
		// be sure we calc the array element size properly too...
		checkCastedChildExpr(null, "0x3d3f00", "*b", arrayCast, "*b[1]");
	}
	
	@Test
	public void testCastingClassesInUI1b() throws Exception {
		openSnapshotAndWaitForSuspendedContext(0);
		
		// casting of nested items
		// (this is superfluous; the UI takes a different path)
		checkCastedExpr(null, "1.4E-45", "b->a", new CastInfo("float"));
	}
	
	@Test
	public void testCastingClassesInUI2() throws Exception {
		openSnapshotAndWaitForSuspendedContext(1);
		
		checkCastedChildExpr(null, "64", "der2", new CastInfo("DerDer *"), "c");
		checkCastedChildExpr(null, "0x3d2470", "der2", new CastInfo("IFaceDerived *"), "Der2");

	}
	@Test
	public void testCastingClassesInUI3() throws Exception {
		openSnapshotAndWaitForSuspendedContext(2);
		
		checkCastedChildExpr(null, "80", "der2", new CastInfo("IFaceDerived *"), "d");
		checkCastedChildExpr(null, "80", "der2", new CastInfo("struct IFaceDerived *"), "d");
		checkCastedChildExpr(null, "80", "der2", new CastInfo("class IFaceDerived *"), "d");
		checkCastedChildExpr(null, "80", "der2", new CastInfo("const class IFaceDerived *"), "d");
		checkCastedChildExpr(null, "80", "der2", new CastInfo("volatile struct IFaceDerived *"), "d");
		
	}
	@Test
	public void testCastingClassesInUI4() throws Exception {
		openSnapshotAndWaitForSuspendedContext(3);
		
		checkCastedChildExpr(null, "96", "iface1", new CastInfo("IFaceDerived *"), "d");
	}
	@Test
	public void testCastingClassesInUI5() throws Exception {
		openSnapshotAndWaitForSuspendedContext(4);
		
		checkCastedChildExpr(null, "112", "iface2", new CastInfo("struct IFaceDerived *"), "d");
		
	}
	@Test
	public void testCastingClassesInUI6() throws Exception {
		openSnapshotAndWaitForSuspendedContext(5);
		
		CastInfo arrayCast = new CastInfo(0, 5);
		checkCastedChildExpr(null, "104 ('h')", "lstring", arrayCast, "lstring[0]");
		checkCastedChildExpr(null, "101 ('e')", "lstring", arrayCast, "lstring[1]");
		checkCastedChildExpr(null, "108 ('l')", "lstring", arrayCast, "lstring[2]");
		checkCastedChildExpr(null, "108 ('l')", "lstring", arrayCast, "lstring[3]");
		checkCastedChildExpr(null, "111 ('o')", "lstring", arrayCast, "lstring[4]");
		
		// illegal index
		checkCastedChildExprFail("lstring", arrayCast, "lstring[5]");
		
		// Nokia bug 10979: make sure we can cast primitive types to array 
		checkCastedChildExpr(null, "2293560", "SizeOfInt", arrayCast, "SizeOfInt[1]");
	}
	@Override
	public String getAlbumName() {
		return "ExpressionsCasting.dsa";
	}

}
