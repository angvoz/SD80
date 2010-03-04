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

import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.junit.Assert;
import org.junit.Test;

public class ExpressionsAggregatesAndEnums extends SimpleDebuggerTest {

	/*
	 *	Note: This assumes you are at a breakpoint where:
	 *	typedef struct {
	 *		char achar;
	 *		UCHAR auchar;
	 *		SCHAR aschar;
	 *		short ashort;
	 *		USHORT aushort;
	 *		SSHORT asshort;
	 *		int aint;
	 *		UINT auint;
	 *		SINT asint;
	 *		long along;
	 *		ULONG aulong;
	 *		SLONG aslong;
	 *		ULONGLONG aulonglong;
	 *		SLONGLONG aslonglong;
	 *		float afloat;
	 *		double adouble;
	 *	} struct_type;
	 *
	 *	struct_type lstruct;
	 *
	 *	lstruct.achar = '1';
	 *	lstruct.auchar = 2;
	 *	lstruct.aschar = '3';
	 *	lstruct.ashort = 4;
	 *	lstruct.aushort = 5;
	 *	lstruct.asshort = 6;
	 *	lstruct.aint = 7;
	 *	lstruct.auint = 8;
	 *	lstruct.asint = 9;
	 *	lstruct.along = 10;
	 *	lstruct.aulong = 11;
	 *	lstruct.aslong = 12;
	 *	lstruct.aulonglong = 13;
	 *	lstruct.aslonglong = 14;
	 *	lstruct.afloat = 15.0;
	 *	lstruct.adouble = 16.0;
	 */
	@Test
	public void testExpressionsWithStructs() throws Exception {
		openSnapshotAndWaitForSuspendedContext(0);

		Assert.assertTrue(getExpressionValue("lstruct") != "");
		Assert.assertEquals("'1'", getExpressionValue("lstruct.achar"));
		Assert.assertEquals("''", getExpressionValue("lstruct.auchar"));
		Assert.assertEquals("'3'", getExpressionValue("lstruct.aschar"));
		Assert.assertEquals("4", getExpressionValue("lstruct.ashort"));
		Assert.assertEquals("5", getExpressionValue("lstruct.aushort"));
		Assert.assertEquals("6", getExpressionValue("lstruct.asshort"));
		Assert.assertEquals("7", getExpressionValue("lstruct.aint"));
		Assert.assertEquals("8", getExpressionValue("lstruct.auint"));
		Assert.assertEquals("9", getExpressionValue("lstruct.asint"));
		Assert.assertEquals("10", getExpressionValue("lstruct.along"));
		Assert.assertEquals("11", getExpressionValue("lstruct.aulong"));
		Assert.assertEquals("12", getExpressionValue("lstruct.aslong"));
		Assert.assertEquals("13", getExpressionValue("lstruct.aulonglong"));
		Assert.assertEquals("14", getExpressionValue("lstruct.aslonglong"));
		Assert.assertEquals("15.0", getExpressionValue("lstruct.afloat"));
		Assert.assertEquals("16.0", getExpressionValue("lstruct.adouble"));

		// logical operations
		// ==
		Assert.assertEquals("true", getExpressionValue("lstruct.achar == 49"));
		Assert.assertEquals("true", getExpressionValue("lstruct.ashort == 4"));
		Assert.assertEquals("true", getExpressionValue("lstruct.aint == 7"));
		Assert.assertEquals("true", getExpressionValue("lstruct.along == 10"));
		Assert.assertEquals("true", getExpressionValue("lstruct.aslonglong == 14"));
		Assert.assertEquals("true", getExpressionValue("lstruct.afloat == 15.0F"));
		Assert.assertEquals("true", getExpressionValue("lstruct.adouble == 16.0"));
		Assert.assertEquals("false", getExpressionValue("lstruct.achar == lstruct.auchar"));
		Assert.assertEquals("false", getExpressionValue("lstruct.ashort == lstruct.aushort"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aint == lstruct.asint"));
		Assert.assertEquals("false", getExpressionValue("lstruct.along == lstruct.aulong"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aulonglong == lstruct.aslonglong"));
		Assert.assertEquals("false", getExpressionValue("lstruct.afloat == lstruct.adouble"));
		// !=
		Assert.assertEquals("false", getExpressionValue("lstruct.achar != 49"));
		Assert.assertEquals("false", getExpressionValue("lstruct.ashort != 4"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aint != 7"));
		Assert.assertEquals("false", getExpressionValue("lstruct.along != 10"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aslonglong != 14"));
		Assert.assertEquals("false", getExpressionValue("lstruct.afloat != 15.0F"));
		Assert.assertEquals("false", getExpressionValue("lstruct.adouble != 16.0"));
		Assert.assertEquals("true", getExpressionValue("lstruct.achar != lstruct.aschar"));
		Assert.assertEquals("true", getExpressionValue("lstruct.ashort != lstruct.aushort"));
		Assert.assertEquals("true", getExpressionValue("lstruct.aint != lstruct.asint"));
		Assert.assertEquals("true", getExpressionValue("lstruct.along != lstruct.aulong"));
		Assert.assertEquals("true", getExpressionValue("lstruct.aulonglong != lstruct.aslonglong"));
		Assert.assertEquals("true", getExpressionValue("lstruct.afloat != lstruct.adouble"));
		// >=
		Assert.assertEquals("true", getExpressionValue("lstruct.achar >= 49"));
		Assert.assertEquals("true", getExpressionValue("lstruct.ashort >= 4"));
		Assert.assertEquals("true", getExpressionValue("lstruct.aint >= 7"));
		Assert.assertEquals("true", getExpressionValue("lstruct.along >= 10"));
		Assert.assertEquals("true", getExpressionValue("lstruct.aslonglong >= 14"));
		Assert.assertEquals("true", getExpressionValue("lstruct.afloat >= 15.0F"));
		Assert.assertEquals("true", getExpressionValue("lstruct.adouble >= 16.0"));
		Assert.assertEquals("false", getExpressionValue("lstruct.achar >= lstruct.aschar"));
		Assert.assertEquals("false", getExpressionValue("lstruct.ashort >= lstruct.aushort"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aint >= lstruct.asint"));
		Assert.assertEquals("false", getExpressionValue("lstruct.along >= lstruct.aulong"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aulonglong >= lstruct.aslonglong"));
		Assert.assertEquals("false", getExpressionValue("lstruct.afloat >= lstruct.adouble"));
		// >
		Assert.assertEquals("false", getExpressionValue("lstruct.achar > 49"));
		Assert.assertEquals("false", getExpressionValue("lstruct.ashort > 4"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aint > 7"));
		Assert.assertEquals("false", getExpressionValue("lstruct.along > 10"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aslonglong > 14"));
		Assert.assertEquals("false", getExpressionValue("lstruct.afloat > 15.0F"));
		Assert.assertEquals("false", getExpressionValue("lstruct.adouble > 16.0"));
		Assert.assertEquals("false", getExpressionValue("lstruct.achar > lstruct.aschar"));
		Assert.assertEquals("false", getExpressionValue("lstruct.ashort > lstruct.aushort"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aint > lstruct.asint"));
		Assert.assertEquals("false", getExpressionValue("lstruct.along > lstruct.aulong"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aulonglong > lstruct.aslonglong"));
		Assert.assertEquals("false", getExpressionValue("lstruct.afloat > lstruct.adouble"));
		// <=
		Assert.assertEquals("true", getExpressionValue("lstruct.achar <= 49"));
		Assert.assertEquals("true", getExpressionValue("lstruct.ashort <= 4"));
		Assert.assertEquals("true", getExpressionValue("lstruct.aint <= 7"));
		Assert.assertEquals("true", getExpressionValue("lstruct.along <= 10"));
		Assert.assertEquals("true", getExpressionValue("lstruct.aslonglong <= 14"));
		Assert.assertEquals("true", getExpressionValue("lstruct.afloat <= 15.0F"));
		Assert.assertEquals("true", getExpressionValue("lstruct.adouble <= 16.0"));
		Assert.assertEquals("true", getExpressionValue("lstruct.achar <= lstruct.aschar"));
		Assert.assertEquals("true", getExpressionValue("lstruct.ashort <= lstruct.aushort"));
		Assert.assertEquals("true", getExpressionValue("lstruct.aint <= lstruct.asint"));
		Assert.assertEquals("true", getExpressionValue("lstruct.along <= lstruct.aulong"));
		Assert.assertEquals("true", getExpressionValue("lstruct.aulonglong <= lstruct.aslonglong"));
		Assert.assertEquals("true", getExpressionValue("lstruct.afloat <= lstruct.adouble"));
		// <
		Assert.assertEquals("false", getExpressionValue("lstruct.achar < 49"));
		Assert.assertEquals("false", getExpressionValue("lstruct.ashort < 4"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aint < 7"));
		Assert.assertEquals("false", getExpressionValue("lstruct.along < 10"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aslonglong < 14"));
		Assert.assertEquals("false", getExpressionValue("lstruct.afloat < 15.0F"));
		Assert.assertEquals("false", getExpressionValue("lstruct.adouble < 16.0"));
		Assert.assertEquals("true", getExpressionValue("lstruct.achar < lstruct.aschar"));
		Assert.assertEquals("true", getExpressionValue("lstruct.ashort < lstruct.aushort"));
		Assert.assertEquals("true", getExpressionValue("lstruct.aint < lstruct.asint"));
		Assert.assertEquals("true", getExpressionValue("lstruct.along < lstruct.aulong"));
		Assert.assertEquals("true", getExpressionValue("lstruct.aulonglong < lstruct.aslonglong"));
		Assert.assertEquals("true", getExpressionValue("lstruct.afloat < lstruct.adouble"));
		// &&
		Assert.assertEquals("false", getExpressionValue("lstruct.achar && 49"));
		Assert.assertEquals("false", getExpressionValue("lstruct.ashort && 4"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aint && 7"));
		Assert.assertEquals("false", getExpressionValue("lstruct.along && 10"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aslonglong && 14"));
		Assert.assertEquals("false", getExpressionValue("lstruct.afloat && 15.0F"));
		Assert.assertEquals("false", getExpressionValue("lstruct.adouble && 16.0"));
		Assert.assertEquals("false", getExpressionValue("lstruct.achar || lstruct.aschar"));
		Assert.assertEquals("false", getExpressionValue("lstruct.ashort || lstruct.aushort"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aint || lstruct.asint"));
		Assert.assertEquals("false", getExpressionValue("lstruct.along || lstruct.aulong"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aulonglong || lstruct.aslonglong"));
		Assert.assertEquals("false", getExpressionValue("lstruct.afloat || lstruct.adouble"));
		// ||
		Assert.assertEquals("false", getExpressionValue("lstruct.achar || 49"));
		Assert.assertEquals("false", getExpressionValue("lstruct.ashort || 4"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aint || 7"));
		Assert.assertEquals("false", getExpressionValue("lstruct.along || 10"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aslonglong || 14"));
		Assert.assertEquals("false", getExpressionValue("lstruct.afloat || 15.0F"));
		Assert.assertEquals("false", getExpressionValue("lstruct.adouble || 16.0"));
		Assert.assertEquals("false", getExpressionValue("lstruct.achar || lstruct.aschar"));
		Assert.assertEquals("false", getExpressionValue("lstruct.ashort || lstruct.aushort"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aint || lstruct.asint"));
		Assert.assertEquals("false", getExpressionValue("lstruct.along || lstruct.aulong"));
		Assert.assertEquals("false", getExpressionValue("lstruct.aulonglong || lstruct.aslonglong"));
		Assert.assertEquals("false", getExpressionValue("lstruct.afloat || lstruct.adouble"));

		// arithmetic operations
		// &
		Assert.assertEquals("49", getExpressionValue("lstruct.achar & 49"));
		Assert.assertEquals("4", getExpressionValue("lstruct.ashort & 4"));
		Assert.assertEquals("7", getExpressionValue("lstruct.aint & 7"));
		Assert.assertEquals("10", getExpressionValue("lstruct.along & 10"));
		Assert.assertEquals("14", getExpressionValue("lstruct.aslonglong & 14"));
		Assert.assertEquals("0.0", getExpressionValue("lstruct.afloat & 15.0F"));
		Assert.assertEquals("0.0", getExpressionValue("lstruct.adouble & 16.0"));
		// |
		Assert.assertEquals("49", getExpressionValue("lstruct.achar | 0"));
		Assert.assertEquals("4", getExpressionValue("lstruct.ashort | 0"));
		Assert.assertEquals("7", getExpressionValue("lstruct.aint | 0"));
		Assert.assertEquals("10", getExpressionValue("lstruct.along | 0"));
		Assert.assertEquals("14", getExpressionValue("lstruct.aslonglong | 0"));
		Assert.assertEquals("0.0", getExpressionValue("lstruct.afloat | 0.0F"));
		Assert.assertEquals("0.0", getExpressionValue("lstruct.adouble | 0.0"));
		// ^
		Assert.assertEquals("49", getExpressionValue("lstruct.achar ^ 0"));
		Assert.assertEquals("4", getExpressionValue("lstruct.ashort ^ 0"));
		Assert.assertEquals("7", getExpressionValue("lstruct.aint ^ 0"));
		Assert.assertEquals("10", getExpressionValue("lstruct.along ^ 0"));
		Assert.assertEquals("14", getExpressionValue("lstruct.aslonglong ^ 0"));
		Assert.assertEquals("0.0", getExpressionValue("lstruct.afloat ^ 0.0F"));
		Assert.assertEquals("0.0", getExpressionValue("lstruct.adouble ^ 0.0"));
		// +
		Assert.assertEquals("50", getExpressionValue("lstruct.achar + 1"));
		Assert.assertEquals("5", getExpressionValue("lstruct.ashort + 1"));
		Assert.assertEquals("8", getExpressionValue("lstruct.aint + 1"));
		Assert.assertEquals("11", getExpressionValue("lstruct.along + 1"));
		Assert.assertEquals("15", getExpressionValue("lstruct.aslonglong + 1"));
		Assert.assertEquals("16.0", getExpressionValue("lstruct.afloat + 1.0F"));
		Assert.assertEquals("17.0", getExpressionValue("lstruct.adouble + 1.0"));
		// -
		Assert.assertEquals("48", getExpressionValue("lstruct.achar - 1"));
		Assert.assertEquals("3", getExpressionValue("lstruct.ashort - 1"));
		Assert.assertEquals("6", getExpressionValue("lstruct.aint - 1"));
		Assert.assertEquals("9", getExpressionValue("lstruct.along - 1"));
		Assert.assertEquals("13", getExpressionValue("lstruct.aslonglong - 1"));
		Assert.assertEquals("14.0", getExpressionValue("lstruct.afloat - 1.0F"));
		Assert.assertEquals("15.0", getExpressionValue("lstruct.adouble - 1.0"));
		// *
		Assert.assertEquals("98", getExpressionValue("lstruct.achar * 2"));
		Assert.assertEquals("8", getExpressionValue("lstruct.ashort * 2"));
		Assert.assertEquals("14", getExpressionValue("lstruct.aint * 2"));
		Assert.assertEquals("20", getExpressionValue("lstruct.along * 2"));
		Assert.assertEquals("28", getExpressionValue("lstruct.aslonglong * 2"));
		Assert.assertEquals("30.0", getExpressionValue("lstruct.afloat * 2.0F"));
		Assert.assertEquals("32.0", getExpressionValue("lstruct.adouble * 2.0"));
		// /
		Assert.assertEquals("24", getExpressionValue("lstruct.achar / 2"));
		Assert.assertEquals("2", getExpressionValue("lstruct.ashort / 2"));
		Assert.assertEquals("3", getExpressionValue("lstruct.aint / 2"));
		Assert.assertEquals("5", getExpressionValue("lstruct.along / 2"));
		Assert.assertEquals("7", getExpressionValue("lstruct.aslonglong / 2"));
		Assert.assertEquals("7.5", getExpressionValue("lstruct.afloat / 2.0F"));
		Assert.assertEquals("8.0", getExpressionValue("lstruct.adouble / 2.0"));
		// %
		Assert.assertEquals("1", getExpressionValue("lstruct.achar % 2"));
		Assert.assertEquals("0", getExpressionValue("lstruct.ashort % 2"));
		Assert.assertEquals("1", getExpressionValue("lstruct.aint % 2"));
		Assert.assertEquals("0", getExpressionValue("lstruct.along % 2"));
		Assert.assertEquals("0", getExpressionValue("lstruct.aslonglong % 2"));
		Assert.assertEquals("0.0", getExpressionValue("lstruct.afloat % 2.0F"));
		Assert.assertEquals("0.0", getExpressionValue("lstruct.adouble % 2.0"));
		// <<
		Assert.assertEquals("98", getExpressionValue("lstruct.achar << 1"));
		Assert.assertEquals("8", getExpressionValue("lstruct.ashort << 1"));
		Assert.assertEquals("14", getExpressionValue("lstruct.aint << 1"));
		Assert.assertEquals("20", getExpressionValue("lstruct.along << 1"));
		Assert.assertEquals("28", getExpressionValue("lstruct.aslonglong << 1"));
		Assert.assertEquals("0.0", getExpressionValue("lstruct.afloat << 1"));
		Assert.assertEquals("0.0", getExpressionValue("lstruct.adouble << 1"));
		// >>
		Assert.assertEquals("24", getExpressionValue("lstruct.achar >> 1"));
		Assert.assertEquals("2", getExpressionValue("lstruct.ashort >> 1"));
		Assert.assertEquals("3", getExpressionValue("lstruct.aint >> 1"));
		Assert.assertEquals("5", getExpressionValue("lstruct.along >> 1"));
		Assert.assertEquals("7", getExpressionValue("lstruct.aslonglong >> 1"));
		Assert.assertEquals("0.0", getExpressionValue("lstruct.afloat >> 1"));
		Assert.assertEquals("0.0", getExpressionValue("lstruct.adouble >> 1"));

		// unary operations
		// +
		Assert.assertEquals("'1'", getExpressionValue("+lstruct.achar"));
		Assert.assertEquals("4", getExpressionValue("+lstruct.ashort"));
		Assert.assertEquals("7", getExpressionValue("+lstruct.aint"));
		Assert.assertEquals("10", getExpressionValue("+lstruct.along"));
		Assert.assertEquals("14", getExpressionValue("+lstruct.aslonglong"));
		Assert.assertEquals("15.0", getExpressionValue("+lstruct.afloat"));
		Assert.assertEquals("16.0", getExpressionValue("+lstruct.adouble"));
		// -
		Assert.assertEquals("-4", getExpressionValue("-lstruct.ashort"));
		Assert.assertEquals("-7", getExpressionValue("-lstruct.aint"));
		Assert.assertEquals("-10", getExpressionValue("-lstruct.along"));
		Assert.assertEquals("-14", getExpressionValue("-lstruct.aslonglong"));
		Assert.assertEquals("-15.0", getExpressionValue("-lstruct.afloat"));
		Assert.assertEquals("-16.0", getExpressionValue("-lstruct.adouble"));
		// !
		Assert.assertEquals("false", getExpressionValue("!lstruct.achar"));
		Assert.assertEquals("false", getExpressionValue("!lstruct.ashort"));
		Assert.assertEquals("false", getExpressionValue("!lstruct.aint"));
		Assert.assertEquals("false", getExpressionValue("!lstruct.along"));
		Assert.assertEquals("false", getExpressionValue("!lstruct.aslonglong"));
		Assert.assertEquals("false", getExpressionValue("!lstruct.afloat"));
		Assert.assertEquals("false", getExpressionValue("!lstruct.adouble"));
		// ~
		Assert.assertEquals("-5", getExpressionValue("~lstruct.ashort"));
		Assert.assertEquals("-8", getExpressionValue("~lstruct.aint"));
		Assert.assertEquals("-11", getExpressionValue("~lstruct.along"));
		Assert.assertEquals("-15", getExpressionValue("~lstruct.aslonglong"));
		Assert.assertEquals("0.0", getExpressionValue("~lstruct.afloat"));
		Assert.assertEquals("0.0", getExpressionValue("~lstruct.adouble"));
	}

	/*
	 * Note: This assumes you are at a breakpoint where:
	 * 
	 * int larray[40];
	 * 
	 * larray[0] = 40;
	 * larray[1] = 39;
	 * larray[2] = 38;
	 * ....
	 * larray[37] = 3;
	 * larray[38] = 2;
	 * larray[39] = 1;
	 */
	@Test
	public void testExpressionsWithArrays() throws Exception {
		openSnapshotAndWaitForSuspendedContext(1);

		Assert.assertTrue(getExpressionValue("larray") != "");
		Assert.assertTrue(getExpressionValue("&larray") != "");
		Assert.assertEquals("0", getExpressionValue("larray[0]"));
		Assert.assertEquals("1", getExpressionValue("larray[1]"));
		Assert.assertEquals("2", getExpressionValue("larray[2]"));
		Assert.assertEquals("37", getExpressionValue("larray[37]"));
		Assert.assertEquals("38", getExpressionValue("larray[38]"));
		Assert.assertEquals("39", getExpressionValue("larray[39]"));

		// logical operations
		// ==
		Assert.assertEquals("true", getExpressionValue("larray[0] == 0"));
		Assert.assertEquals("false", getExpressionValue("larray[0] == 1"));
		Assert.assertEquals("true", getExpressionValue("larray[39] == 39"));
		Assert.assertEquals("false", getExpressionValue("larray[39] == 40"));
		Assert.assertEquals("false", getExpressionValue("larray[0] == larray[39]"));
		// !=
		Assert.assertEquals("true", getExpressionValue("larray[0] != 1"));
		Assert.assertEquals("false", getExpressionValue("larray[0] != 0"));
		Assert.assertEquals("true", getExpressionValue("larray[39] != 40"));
		Assert.assertEquals("false", getExpressionValue("larray[39] != 39"));
		Assert.assertEquals("true", getExpressionValue("larray[0] != larray[39]"));
		// >=
		Assert.assertEquals("true", getExpressionValue("larray[0] >= 0"));
		Assert.assertEquals("false", getExpressionValue("larray[0] >= 41"));
		Assert.assertEquals("true", getExpressionValue("larray[39] >= 1"));
		Assert.assertEquals("false", getExpressionValue("larray[39] >= 41"));
		Assert.assertEquals("false", getExpressionValue("larray[0] >= larray[39]"));
		// >
		Assert.assertEquals("true", getExpressionValue("larray[0] > -1"));
		Assert.assertEquals("false", getExpressionValue("larray[0] > 40"));
		Assert.assertEquals("true", getExpressionValue("larray[39] > 0"));
		Assert.assertEquals("false", getExpressionValue("larray[39] > 40"));
		Assert.assertEquals("false", getExpressionValue("larray[0] > larray[39]"));
		// <=
		Assert.assertEquals("true", getExpressionValue("larray[0] <= 40"));
		Assert.assertEquals("false", getExpressionValue("larray[0] <= -1"));
		Assert.assertEquals("true", getExpressionValue("larray[39] <= 40"));
		Assert.assertEquals("false", getExpressionValue("larray[39] <= 0"));
		Assert.assertEquals("true", getExpressionValue("larray[0] <= larray[39]"));
		// <
		Assert.assertEquals("true", getExpressionValue("larray[0] < 41"));
		Assert.assertEquals("false", getExpressionValue("larray[0] < -1"));
		Assert.assertEquals("true", getExpressionValue("larray[39] < 40"));
		Assert.assertEquals("false", getExpressionValue("larray[39] < 1"));
		Assert.assertEquals("true", getExpressionValue("larray[0] < larray[39]"));
		// &&
		Assert.assertEquals("false", getExpressionValue("larray[0] && 40"));
		Assert.assertEquals("false", getExpressionValue("larray[39] && 1"));
		Assert.assertEquals("false", getExpressionValue("larray[0] && larray[39]"));
		// ||
		Assert.assertEquals("false", getExpressionValue("larray[0] || 40"));
		Assert.assertEquals("false", getExpressionValue("larray[39] || 1"));
		Assert.assertEquals("false", getExpressionValue("larray[0] || larray[39]"));

		// arithmetic operations
		// &
		Assert.assertEquals("1", getExpressionValue("larray[39] & 1"));
		Assert.assertEquals("0", getExpressionValue("larray[0] & larray[39]"));
		// |
		Assert.assertEquals("0", getExpressionValue("larray[0] | 0"));
		Assert.assertEquals("39", getExpressionValue("larray[39] | 0"));
		Assert.assertEquals("39", getExpressionValue("larray[1] | larray[38]"));
		// ^
		Assert.assertEquals("0", getExpressionValue("larray[0] ^ 0"));
		Assert.assertEquals("39", getExpressionValue("larray[39] ^ 0"));
		Assert.assertEquals("38", getExpressionValue("larray[1] ^ larray[39]"));
		// +
		Assert.assertEquals("1", getExpressionValue("larray[0] + 1"));
		Assert.assertEquals("40", getExpressionValue("larray[39] + 1"));
		Assert.assertEquals("39", getExpressionValue("larray[0] + larray[39]"));
		// -
		Assert.assertEquals("-1", getExpressionValue("larray[0] - 1"));
		Assert.assertEquals("38", getExpressionValue("larray[39] - 1"));
		Assert.assertEquals("-39", getExpressionValue("larray[0] - larray[39]"));
		// *
		Assert.assertEquals("2", getExpressionValue("larray[1] * 2"));
		Assert.assertEquals("78", getExpressionValue("larray[39] * 2"));
		Assert.assertEquals("39", getExpressionValue("larray[1] * larray[39]"));
		// /
		Assert.assertEquals("0", getExpressionValue("larray[0] / 2"));
		Assert.assertEquals("19", getExpressionValue("larray[39] / 2"));
		Assert.assertEquals("39", getExpressionValue("larray[39] / larray[1]"));
		// %
		Assert.assertEquals("0", getExpressionValue("larray[0] % 2"));
		Assert.assertEquals("1", getExpressionValue("larray[39] % 2"));
		Assert.assertEquals("1", getExpressionValue("larray[1] % larray[39]"));
		// <<
		Assert.assertEquals("0", getExpressionValue("larray[0] << 1"));
		Assert.assertEquals("78", getExpressionValue("larray[39] << 1"));
		Assert.assertEquals("16", getExpressionValue("larray[2] << larray[3]"));
		// >>
		Assert.assertEquals("0", getExpressionValue("larray[0] >> 1"));
		Assert.assertEquals("19", getExpressionValue("larray[39] >> 1"));
		Assert.assertEquals("4", getExpressionValue("larray[39] >> larray[3]"));

		// unary operations
		// +
		Assert.assertEquals("0", getExpressionValue("+larray[0]"));
		Assert.assertEquals("39", getExpressionValue("+larray[39]"));
		// -
		Assert.assertEquals("0", getExpressionValue("-larray[0]"));
		Assert.assertEquals("-39", getExpressionValue("-larray[39]"));
		// !
		Assert.assertEquals("false", getExpressionValue("!larray[0]"));
		Assert.assertEquals("false", getExpressionValue("!larray[39]"));
		// ~
		Assert.assertEquals("-2", getExpressionValue("~larray[1]"));
		Assert.assertEquals("-40", getExpressionValue("~larray[39]"));
	}

	/*
	 *	Note: This assumes you are at a breakpoint where:
	 *
	 *	typedef union {
	 *		volatile int x;
	 *		volatile long y;
	 *	} union_type;
	 *
	 *	union_type lunion;
	 *
	 *	lunion.x = 2;
	 *	lunion.y = 2;
	 */
	@Test
	public void testExpressionsWithUnions() throws Exception {
		openSnapshotAndWaitForSuspendedContext(2);

		Assert.assertTrue(getExpressionValue("lunion") != "");
		Assert.assertTrue(getExpressionValue("&lunion") != "");
		Assert.assertEquals("2", getExpressionValue("lunion.x"));
		Assert.assertEquals("2", getExpressionValue("lunion.y"));

		// logical operations
		// ==
		Assert.assertEquals("true", getExpressionValue("lunion.x == 2"));
		Assert.assertEquals("false", getExpressionValue("lunion.x == 1"));
		Assert.assertEquals("true", getExpressionValue("lunion.y == 2"));
		Assert.assertEquals("false", getExpressionValue("lunion.y == 1"));
		Assert.assertEquals("true", getExpressionValue("lunion.x == lunion.y"));
		// !=
		Assert.assertEquals("false", getExpressionValue("lunion.x != 2"));
		Assert.assertEquals("true", getExpressionValue("lunion.x != 1"));
		Assert.assertEquals("false", getExpressionValue("lunion.y != 2"));
		Assert.assertEquals("true", getExpressionValue("lunion.y != 1"));
		Assert.assertEquals("false", getExpressionValue("lunion.x != lunion.y"));
		// >=
		Assert.assertEquals("true", getExpressionValue("lunion.x >= 2"));
		Assert.assertEquals("false", getExpressionValue("lunion.x >= 3"));
		Assert.assertEquals("true", getExpressionValue("lunion.y >= 2"));
		Assert.assertEquals("false", getExpressionValue("lunion.y >= 3"));
		Assert.assertEquals("true", getExpressionValue("lunion.x >= lunion.y"));
		// >
		Assert.assertEquals("false", getExpressionValue("lunion.x > 2"));
		Assert.assertEquals("true", getExpressionValue("lunion.x > 1"));
		Assert.assertEquals("false", getExpressionValue("lunion.y > 2"));
		Assert.assertEquals("true", getExpressionValue("lunion.y > 1"));
		Assert.assertEquals("false", getExpressionValue("lunion.x > lunion.y"));
		// <=
		Assert.assertEquals("true", getExpressionValue("lunion.x <= 2"));
		Assert.assertEquals("false", getExpressionValue("lunion.x <= 1"));
		Assert.assertEquals("true", getExpressionValue("lunion.y <= 2"));
		Assert.assertEquals("false", getExpressionValue("lunion.y <= 1"));
		Assert.assertEquals("true", getExpressionValue("lunion.x <= lunion.y"));
		// <
		Assert.assertEquals("false", getExpressionValue("lunion.x < 2"));
		Assert.assertEquals("true", getExpressionValue("lunion.x < 3"));
		Assert.assertEquals("false", getExpressionValue("lunion.y < 2"));
		Assert.assertEquals("true", getExpressionValue("lunion.y < 3"));
		Assert.assertEquals("false", getExpressionValue("lunion.x < lunion.y"));
		// &&
		Assert.assertEquals("false", getExpressionValue("lunion.x && 2"));
		Assert.assertEquals("false", getExpressionValue("lunion.x && 1"));
		Assert.assertEquals("false", getExpressionValue("lunion.y && 2"));
		Assert.assertEquals("false", getExpressionValue("lunion.y && 1"));
		Assert.assertEquals("false", getExpressionValue("lunion.x && lunion.y"));
		// ||
		Assert.assertEquals("false", getExpressionValue("lunion.x || 2"));
		Assert.assertEquals("false", getExpressionValue("lunion.x || 1"));
		Assert.assertEquals("false", getExpressionValue("lunion.y || 2"));
		Assert.assertEquals("false", getExpressionValue("lunion.y || 1"));
		Assert.assertEquals("false", getExpressionValue("lunion.x || lunion.y"));

		// arithmetic operations
		// &
		Assert.assertEquals("2", getExpressionValue("lunion.x & 2"));
		Assert.assertEquals("0", getExpressionValue("lunion.x & 1"));
		Assert.assertEquals("2", getExpressionValue("lunion.y & 2"));
		Assert.assertEquals("0", getExpressionValue("lunion.y & 1"));
		Assert.assertEquals("2", getExpressionValue("lunion.x & lunion.y"));
		// |
		Assert.assertEquals("2", getExpressionValue("lunion.x | 2"));
		Assert.assertEquals("3", getExpressionValue("lunion.x | 1"));
		Assert.assertEquals("2", getExpressionValue("lunion.y | 2"));
		Assert.assertEquals("3", getExpressionValue("lunion.y | 1"));
		Assert.assertEquals("2", getExpressionValue("lunion.x | lunion.y"));
		// ^
		Assert.assertEquals("0", getExpressionValue("lunion.x ^ 2"));
		Assert.assertEquals("3", getExpressionValue("lunion.x ^ 1"));
		Assert.assertEquals("0", getExpressionValue("lunion.y ^ 2"));
		Assert.assertEquals("3", getExpressionValue("lunion.y ^ 1"));
		Assert.assertEquals("0", getExpressionValue("lunion.x ^ lunion.y"));
		// +
		Assert.assertEquals("4", getExpressionValue("lunion.x + 2"));
		Assert.assertEquals("3", getExpressionValue("lunion.x + 1"));
		Assert.assertEquals("4", getExpressionValue("lunion.y + 2"));
		Assert.assertEquals("3", getExpressionValue("lunion.y + 1"));
		Assert.assertEquals("4", getExpressionValue("lunion.x + lunion.y"));
		// -
		Assert.assertEquals("0", getExpressionValue("lunion.x - 2"));
		Assert.assertEquals("1", getExpressionValue("lunion.x - 1"));
		Assert.assertEquals("0", getExpressionValue("lunion.y - 2"));
		Assert.assertEquals("1", getExpressionValue("lunion.y - 1"));
		Assert.assertEquals("0", getExpressionValue("lunion.x - lunion.y"));
		// *
		Assert.assertEquals("4", getExpressionValue("lunion.x * 2"));
		Assert.assertEquals("2", getExpressionValue("lunion.x * 1"));
		Assert.assertEquals("4", getExpressionValue("lunion.y * 2"));
		Assert.assertEquals("2", getExpressionValue("lunion.y * 1"));
		Assert.assertEquals("4", getExpressionValue("lunion.x * lunion.y"));
		// /
		Assert.assertEquals("1", getExpressionValue("lunion.x / 2"));
		Assert.assertEquals("2", getExpressionValue("lunion.x / 1"));
		Assert.assertEquals("1", getExpressionValue("lunion.y / 2"));
		Assert.assertEquals("2", getExpressionValue("lunion.y / 1"));
		Assert.assertEquals("1", getExpressionValue("lunion.x / lunion.y"));
		// %
		Assert.assertEquals("0", getExpressionValue("lunion.x % 2"));
		Assert.assertEquals("0", getExpressionValue("lunion.x % 1"));
		Assert.assertEquals("0", getExpressionValue("lunion.y % 2"));
		Assert.assertEquals("0", getExpressionValue("lunion.y % 1"));
		Assert.assertEquals("0", getExpressionValue("lunion.x % lunion.y"));
		// <<
		Assert.assertEquals("8", getExpressionValue("lunion.x << 2"));
		Assert.assertEquals("4", getExpressionValue("lunion.x << 1"));
		Assert.assertEquals("8", getExpressionValue("lunion.y << 2"));
		Assert.assertEquals("4", getExpressionValue("lunion.y << 1"));
		Assert.assertEquals("8", getExpressionValue("lunion.x << lunion.y"));
		// >>
		Assert.assertEquals("0", getExpressionValue("lunion.x >> 2"));
		Assert.assertEquals("1", getExpressionValue("lunion.x >> 1"));
		Assert.assertEquals("0", getExpressionValue("lunion.y >> 2"));
		Assert.assertEquals("1", getExpressionValue("lunion.y >> 1"));
		Assert.assertEquals("0", getExpressionValue("lunion.x >> lunion.y"));

		// unary operations
		// +
		Assert.assertEquals("2", getExpressionValue("+lunion.x"));
		Assert.assertEquals("2", getExpressionValue("+lunion.y"));
		// -
		Assert.assertEquals("-2", getExpressionValue("-lunion.x"));
		Assert.assertEquals("-2", getExpressionValue("-lunion.y"));
		// !
		Assert.assertEquals("false", getExpressionValue("!lunion.x"));
		Assert.assertEquals("false", getExpressionValue("!lunion.y"));
		// ~
		Assert.assertEquals("-3", getExpressionValue("~lunion.x"));
		Assert.assertEquals("-3", getExpressionValue("~lunion.y"));
	}

	/*
	 *	Note: This assumes you are at a breakpoint where:
	 *
	 *	typedef struct {
	 *		volatile unsigned x:1;
	 *		volatile unsigned y:2;
	 *		volatile unsigned z:3;
	 *		volatile unsigned w:16;
	 *	} bitfield_type;
	 *
	 *	bitfield_type lbitfield;
	 *
	 *	lbitfield.x = 1;
	 *	lbitfield.y = 2;
	 *	lbitfield.z = 3;
	 *	lbitfield.w = 26;
	 */
	@Test
	public void testExpressionsWithBitfields() throws Exception {
		openSnapshotAndWaitForSuspendedContext(3);

		Assert.assertTrue(getExpressionValue("lbitfield") != "");
		Assert.assertTrue(getExpressionValue("&lbitfield") != "");
		Assert.assertEquals("1", getExpressionValue("lbitfield.x"));
		Assert.assertEquals("2", getExpressionValue("lbitfield.y"));
		Assert.assertEquals("3", getExpressionValue("lbitfield.z"));
		Assert.assertEquals("26", getExpressionValue("lbitfield.w"));

		// logical operations
		// ==
		Assert.assertEquals("true", getExpressionValue("lbitfield.x == 1"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x == 0"));
		Assert.assertEquals("true", getExpressionValue("lbitfield.y == 2"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.y == 0"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x == lbitfield.y"));
		// !=
		Assert.assertEquals("true", getExpressionValue("lbitfield.x != 0"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x != 1"));
		Assert.assertEquals("true", getExpressionValue("lbitfield.y != 0"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.y != 2"));
		Assert.assertEquals("true", getExpressionValue("lbitfield.x != lbitfield.y"));
		// >=
		Assert.assertEquals("true", getExpressionValue("lbitfield.x >= 1"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x >= 2"));
		Assert.assertEquals("true", getExpressionValue("lbitfield.y >= 2"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.y >= 3"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x >= lbitfield.y"));
		// >
		Assert.assertEquals("true", getExpressionValue("lbitfield.x > 0"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x > 1"));
		Assert.assertEquals("true", getExpressionValue("lbitfield.y > 1"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.y > 2"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x > lbitfield.y"));
		// <=
		Assert.assertEquals("true", getExpressionValue("lbitfield.x <= 1"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x <= 0"));
		Assert.assertEquals("true", getExpressionValue("lbitfield.y <= 2"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.y <= 1"));
		Assert.assertEquals("true", getExpressionValue("lbitfield.x <= lbitfield.y"));
		// <
		Assert.assertEquals("true", getExpressionValue("lbitfield.x < 2"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x < 1"));
		Assert.assertEquals("true", getExpressionValue("lbitfield.y < 3"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.y < 2"));
		Assert.assertEquals("true", getExpressionValue("lbitfield.x < lbitfield.y"));
		// &&
		Assert.assertEquals("false", getExpressionValue("lbitfield.x && 2"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x && 1"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.y && 3"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.y && 2"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x && lbitfield.y"));
		// ||
		Assert.assertEquals("false", getExpressionValue("lbitfield.x || 2"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x || 1"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.y || 3"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.y || 2"));
		Assert.assertEquals("false", getExpressionValue("lbitfield.x || lbitfield.y"));

		// arithmetic operations
		// &
		Assert.assertEquals("0", getExpressionValue("lbitfield.x & 0"));
		Assert.assertEquals("1", getExpressionValue("lbitfield.x & 1"));
		Assert.assertEquals("0", getExpressionValue("lbitfield.y & 0"));
		Assert.assertEquals("2", getExpressionValue("lbitfield.y & 2"));
		Assert.assertEquals("0", getExpressionValue("lbitfield.x & lbitfield.y"));
		// |
		Assert.assertEquals("1", getExpressionValue("lbitfield.x | 0"));
		Assert.assertEquals("1", getExpressionValue("lbitfield.x | 1"));
		Assert.assertEquals("2", getExpressionValue("lbitfield.y | 0"));
		Assert.assertEquals("2", getExpressionValue("lbitfield.y | 2"));
		Assert.assertEquals("3", getExpressionValue("lbitfield.x | lbitfield.y"));
		// ^
		Assert.assertEquals("1", getExpressionValue("lbitfield.x ^ 0"));
		Assert.assertEquals("0", getExpressionValue("lbitfield.x ^ 1"));
		Assert.assertEquals("2", getExpressionValue("lbitfield.y ^ 0"));
		Assert.assertEquals("0", getExpressionValue("lbitfield.y ^ 2"));
		Assert.assertEquals("3", getExpressionValue("lbitfield.x ^ lbitfield.y"));
		// +
		Assert.assertEquals("0", getExpressionValue("lbitfield.x + (-1)"));
		Assert.assertEquals("2", getExpressionValue("lbitfield.x + 1"));
		Assert.assertEquals("1", getExpressionValue("lbitfield.y + (-1)"));
		Assert.assertEquals("3", getExpressionValue("lbitfield.y + 1"));
		Assert.assertEquals("3", getExpressionValue("lbitfield.x + lbitfield.y"));
		// -
		Assert.assertEquals("2", getExpressionValue("lbitfield.x - (-1)"));
		Assert.assertEquals("0", getExpressionValue("lbitfield.x - 1"));
		Assert.assertEquals("3", getExpressionValue("lbitfield.y - (-1)"));
		Assert.assertEquals("1", getExpressionValue("lbitfield.y - 1"));
		Assert.assertEquals("-1", getExpressionValue("lbitfield.x - lbitfield.y"));
		// *
		Assert.assertEquals("0", getExpressionValue("lbitfield.x * 0"));
		Assert.assertEquals("1", getExpressionValue("lbitfield.x * 1"));
		Assert.assertEquals("0", getExpressionValue("lbitfield.y * 0"));
		Assert.assertEquals("4", getExpressionValue("lbitfield.y * 2"));
		Assert.assertEquals("2", getExpressionValue("lbitfield.x * lbitfield.y"));
		// /
		Assert.assertEquals(ASTEvalMessages.DivideByZero, getExpressionValue("lbitfield.x / 0"));
		Assert.assertEquals("1", getExpressionValue("lbitfield.x / 1"));
		Assert.assertEquals(ASTEvalMessages.DivideByZero, getExpressionValue("lbitfield.y / 0"));
		Assert.assertEquals("1", getExpressionValue("lbitfield.y / 2"));
		Assert.assertEquals("0", getExpressionValue("lbitfield.x / lbitfield.y"));
		// %
		Assert.assertEquals(ASTEvalMessages.DivideByZero, getExpressionValue("lbitfield.x % 0"));
		Assert.assertEquals("0", getExpressionValue("lbitfield.x % 1"));
		Assert.assertEquals(ASTEvalMessages.DivideByZero, getExpressionValue("lbitfield.y % 0"));
		Assert.assertEquals("0", getExpressionValue("lbitfield.y % 2"));
		Assert.assertEquals("1", getExpressionValue("lbitfield.x % lbitfield.y"));
		// <<
		Assert.assertEquals("1", getExpressionValue("lbitfield.x << 0"));
		Assert.assertEquals("2", getExpressionValue("lbitfield.x << 1"));
		Assert.assertEquals("2", getExpressionValue("lbitfield.y << 0"));
		Assert.assertEquals("4", getExpressionValue("lbitfield.y << 1"));
		Assert.assertEquals("4", getExpressionValue("lbitfield.x << lbitfield.y"));
		// >>
		Assert.assertEquals("1", getExpressionValue("lbitfield.x >> 0"));
		Assert.assertEquals("0", getExpressionValue("lbitfield.x >> 1"));
		Assert.assertEquals("2", getExpressionValue("lbitfield.y >> 0"));
		Assert.assertEquals("1", getExpressionValue("lbitfield.y >> 1"));
		Assert.assertEquals("0", getExpressionValue("lbitfield.x >> lbitfield.y"));

		// unary operations
		// +
		Assert.assertEquals("1", getExpressionValue("+lbitfield.x"));
		Assert.assertEquals("2", getExpressionValue("+lbitfield.y"));
		Assert.assertEquals("3", getExpressionValue("+lbitfield.z"));
		Assert.assertEquals("26", getExpressionValue("+lbitfield.w"));
		// -
		Assert.assertEquals("-1", getExpressionValue("-lbitfield.x"));
		Assert.assertEquals("-2", getExpressionValue("-lbitfield.y"));
		Assert.assertEquals("-3", getExpressionValue("-lbitfield.z"));
		Assert.assertEquals("-26", getExpressionValue("-lbitfield.w"));
		// !
		Assert.assertEquals("false", getExpressionValue("!lbitfield.x"));
		Assert.assertEquals("false", getExpressionValue("!lbitfield.y"));
		Assert.assertEquals("false", getExpressionValue("!lbitfield.z"));
		Assert.assertEquals("false", getExpressionValue("!lbitfield.w"));
		// ~
		Assert.assertEquals("-2", getExpressionValue("~lbitfield.x"));
		Assert.assertEquals("-3", getExpressionValue("~lbitfield.y"));
		Assert.assertEquals("-4", getExpressionValue("~lbitfield.z"));
		Assert.assertEquals("-27", getExpressionValue("~lbitfield.w"));
	}

	/*
	 *	Note: This assumes you are at a breakpoint where:
	 *
	 *	enum enum_type { zero, one, two, three, four };
	 *
	 *	enum enum_type lenum;
	 *
	 *	lenum = three;
	 */
	@Test
	public void testExpressionsWithEnums() throws Exception {
		openSnapshotAndWaitForSuspendedContext(4);

		Assert.assertTrue(getExpressionValue("lenum") != "");
		Assert.assertEquals("three [3]", getExpressionValue("lenum"));

		// logical operations
		// ==
		Assert.assertEquals("true", getExpressionValue("lenum == 3"));
		Assert.assertEquals("true", getExpressionValue("lenum == three"));
		Assert.assertEquals("false", getExpressionValue("lenum == 4"));
		Assert.assertEquals("false", getExpressionValue("lenum == four"));
		// !=
		Assert.assertEquals("true", getExpressionValue("lenum != 4"));
		Assert.assertEquals("true", getExpressionValue("lenum != four"));
		Assert.assertEquals("false", getExpressionValue("lenum != 3"));
		Assert.assertEquals("false", getExpressionValue("lenum != three"));
		// >=
		Assert.assertEquals("true", getExpressionValue("lenum >= 3"));
		Assert.assertEquals("true", getExpressionValue("lenum >= three"));
		Assert.assertEquals("false", getExpressionValue("lenum >= 5"));
		// >
		Assert.assertEquals("true", getExpressionValue("lenum > 2"));
		Assert.assertEquals("true", getExpressionValue("lenum > two"));
		Assert.assertEquals("false", getExpressionValue("lenum > 4"));
		Assert.assertEquals("false", getExpressionValue("lenum > four"));
		// <=
		Assert.assertEquals("true", getExpressionValue("lenum <= 3"));
		Assert.assertEquals("false", getExpressionValue("lenum <= 2"));
		// <
		Assert.assertEquals("true", getExpressionValue("lenum < 5"));
		Assert.assertEquals("false", getExpressionValue("lenum < 3"));
		// &&
		Assert.assertEquals("false", getExpressionValue("lenum && 4"));
		Assert.assertEquals("false", getExpressionValue("lenum && 1"));
		// ||
		Assert.assertEquals("false", getExpressionValue("lenum || 4"));
		Assert.assertEquals("false", getExpressionValue("lenum || 1"));

		// arithmetic operations
		// &
		Assert.assertEquals("0", getExpressionValue("lenum & 4"));
		Assert.assertEquals("3", getExpressionValue("lenum & 3"));
		// |
		Assert.assertEquals("7", getExpressionValue("lenum | 4"));
		Assert.assertEquals("3", getExpressionValue("lenum | 3"));
		// ^
		Assert.assertEquals("7", getExpressionValue("lenum ^ 4"));
		Assert.assertEquals("0", getExpressionValue("lenum ^ 3"));
		// +
		Assert.assertEquals("7", getExpressionValue("lenum + 4"));
		Assert.assertEquals("6", getExpressionValue("lenum + 3"));
		Assert.assertEquals("6", getExpressionValue("lenum + three"));
		// -
		Assert.assertEquals("-1", getExpressionValue("lenum - 4"));
		Assert.assertEquals("0", getExpressionValue("lenum - 3"));
		// *
		Assert.assertEquals("12", getExpressionValue("lenum * 4"));
		Assert.assertEquals("9", getExpressionValue("lenum * 3"));
		// /
		Assert.assertEquals("0", getExpressionValue("lenum / 4"));
		Assert.assertEquals("1", getExpressionValue("lenum / 3"));
		// %
		Assert.assertEquals("3", getExpressionValue("lenum % 4"));
		Assert.assertEquals("0", getExpressionValue("lenum % 3"));
		// <<
		Assert.assertEquals("12", getExpressionValue("lenum << 2"));
		Assert.assertEquals("24", getExpressionValue("lenum << three"));
		// >>
		Assert.assertEquals("0", getExpressionValue("lenum >> 4"));
		Assert.assertEquals("0", getExpressionValue("lenum >> 3"));

		// unary operations
		// +
		Assert.assertEquals("3", getExpressionValue("+lenum"));
		// -
		Assert.assertEquals("-3", getExpressionValue("-lenum"));
		// !
		Assert.assertEquals("false", getExpressionValue("!lenum"));
		// ~
		Assert.assertEquals("-4", getExpressionValue("~lenum"));
	}


	@Override
	public String getAlbumName() {
		return "ExpressionsAggregatesAndEnums.dsa";
	}

}
