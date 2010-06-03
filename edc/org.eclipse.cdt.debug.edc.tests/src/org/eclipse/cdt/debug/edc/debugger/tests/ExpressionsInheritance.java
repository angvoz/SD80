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

import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.junit.Assert;
import org.junit.Test;

public class ExpressionsInheritance extends SimpleDebuggerTest {

	IAlbum album;

	/*
	 * Note: This assumes you are at a breakpoint where the following are true:
	 * 
	 * class Person has fields _age(20), _height(72), _weight(150) class
	 * Employee inherits from Person and has field _salary(no default)
	 * 
	 * Jane is a default Person Frank is an Employee with _salary = 2000.0 and
	 * otherwise a default Person Jessie is an Employee with _salary = 4000.0,
	 * _age = 15, _height = 73, _weight = 123 John is an Employee wit		Assert.assertEquals("0", getExpressionValue("larray[1] & 1"));
h _salary =
	 * 5000.0 and otherwise a default Person
	 */
	@Test
	public void testExpressionsSimpleInheritance() throws Exception {
		Assert.assertEquals("20", getExpressionValue("Jane._age"));
		Assert.assertEquals("72", getExpressionValue("Jane._height"));
		Assert.assertEquals("150", getExpressionValue("Jane._weight"));
		Assert.assertEquals("2000.0", getExpressionValue("Frank._salary"));
		Assert.assertEquals("20", getExpressionValue("Frank._age"));
		Assert.assertEquals("72", getExpressionValue("Frank._height"));
		Assert.assertEquals("150", getExpressionValue("Frank._weight"));
		Assert.assertEquals("4000.0", getExpressionValue("Jessie._salary"));
		Assert.assertEquals("15", getExpressionValue("Jessie._age"));
		Assert.assertEquals("73", getExpressionValue("Jessie._height"));
		Assert.assertEquals("123", getExpressionValue("Jessie._weight"));
		Assert.assertEquals("5000.0", getExpressionValue("John._salary"));
		Assert.assertEquals("20", getExpressionValue("John._age"));
		Assert.assertEquals("72", getExpressionValue("John._height"));
		Assert.assertEquals("150", getExpressionValue("John._weight"));
	}

	// TODO
	@Test
	public void testExpressionsMultipleInheritance() throws Exception {
		// go to next snapshot in the album
		openSnapshotAndWaitForSuspendedContext(1);
		Assert.assertTrue(true);
	}

	@Override
	public String getAlbumName() {
		return "BlackFlagInheritance.dsa";
	}

}
