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

import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.junit.Test;

public class ExpressionsAggregatesAndEnums extends BaseExpressionTest {

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

		checkExprNoError("lstruct");
		checkExpr("49 ('1')", "lstruct.achar");
		checkExpr("2 ('\\002')", "lstruct.auchar");
		checkExpr("51 ('3')", "lstruct.aschar");
		checkExpr("4", "lstruct.ashort");
		checkExpr("5", "lstruct.aushort");
		checkExpr("6", "lstruct.asshort");
		checkExpr("7", "lstruct.aint");
		checkExpr("8", "lstruct.auint");
		checkExpr("9", "lstruct.asint");
		checkExpr("10", "lstruct.along");
		checkExpr("11", "lstruct.aulong");
		checkExpr("12", "lstruct.aslong");
		checkExpr("13", "lstruct.aulonglong");
		checkExpr("14", "lstruct.aslonglong");
		checkExpr("15.0", "lstruct.afloat");
		checkExpr("16.0", "lstruct.adouble");

		// logical operations
		// ==
		checkExpr("true", "lstruct.achar == 49");
		checkExpr("true", "lstruct.ashort == 4");
		checkExpr("true", "lstruct.aint == 7");
		checkExpr("true", "10 == lstruct.along");
		checkExpr("true", "lstruct.aslonglong == 14");
		checkExpr("true", "lstruct.afloat == lstruct.afloat"); // for precision
		checkExpr("true", "lstruct.adouble == lstruct.adouble"); // for precision
		checkExpr("false", "lstruct.achar == lstruct.auchar");
		checkExpr("false", "lstruct.ashort == lstruct.aushort");
		checkExpr("false", "lstruct.aint == lstruct.asint");
		checkExpr("false", "lstruct.along == lstruct.aulong");
		checkExpr("false", "lstruct.aulonglong == lstruct.aslonglong");
		checkExpr("false", "lstruct.afloat == lstruct.adouble");
		// !=
		checkExpr("false", "lstruct.achar != 49");
		checkExpr("false", "lstruct.ashort != 4");
		checkExpr("false", "7 != lstruct.aint");
		checkExpr("false", "lstruct.along != 10");
		checkExpr("false", "lstruct.aslonglong != 14");
		checkExpr("false", "15.0F != lstruct.afloat");
		checkExpr("false", "lstruct.adouble != 16.0");
		checkExpr("true", "lstruct.achar != lstruct.aschar");
		checkExpr("true", "lstruct.ashort != lstruct.aushort");
		checkExpr("true", "lstruct.aint != lstruct.asint");
		checkExpr("true", "lstruct.along != lstruct.aulong");
		checkExpr("true", "lstruct.aulonglong != lstruct.aslonglong");
		checkExpr("true", "lstruct.afloat != lstruct.adouble");
		// >=
		checkExpr("true", "lstruct.achar >= 49");
		checkExpr("true", "lstruct.ashort >= 4");
		checkExpr("true", "lstruct.aint >= 7");
		checkExpr("true", "lstruct.along >= 10");
		checkExpr("true", "lstruct.aslonglong >= 14");
		checkExpr("true", "lstruct.afloat >= 15.0F");
		checkExpr("true", "lstruct.adouble >= 16.0");
		checkExpr("false", "lstruct.achar >= lstruct.aschar");
		checkExpr("false", "lstruct.ashort >= lstruct.aushort");
		checkExpr("false", "lstruct.aint >= lstruct.asint");
		checkExpr("false", "lstruct.along >= lstruct.aulong");
		checkExpr("false", "lstruct.aulonglong >= lstruct.aslonglong");
		checkExpr("false", "lstruct.afloat >= lstruct.adouble");
		// >
		checkExpr("false", "lstruct.achar > 49");
		checkExpr("false", "lstruct.ashort > 4");
		checkExpr("false", "lstruct.aint > 7");
		checkExpr("false", "lstruct.along > 10");
		checkExpr("false", "lstruct.aslonglong > 14");
		checkExpr("false", "lstruct.afloat > 15.0F");
		checkExpr("false", "lstruct.adouble > 16.0");
		checkExpr("false", "lstruct.achar > lstruct.aschar");
		checkExpr("false", "lstruct.ashort > lstruct.aushort");
		checkExpr("false", "lstruct.aint > lstruct.asint");
		checkExpr("false", "lstruct.along > lstruct.aulong");
		checkExpr("false", "lstruct.aulonglong > lstruct.aslonglong");
		checkExpr("false", "lstruct.afloat > lstruct.adouble");
		// <=
		checkExpr("true", "lstruct.achar <= 49");
		checkExpr("true", "lstruct.ashort <= 4");
		checkExpr("true", "lstruct.aint <= 7");
		checkExpr("true", "lstruct.along <= 10");
		checkExpr("true", "lstruct.aslonglong <= 14");
		checkExpr("true", "lstruct.afloat <= 15.0F");
		checkExpr("true", "lstruct.adouble <= 16.0");
		checkExpr("true", "lstruct.achar <= lstruct.aschar");
		checkExpr("true", "lstruct.ashort <= lstruct.aushort");
		checkExpr("true", "lstruct.aint <= lstruct.asint");
		checkExpr("true", "lstruct.along <= lstruct.aulong");
		checkExpr("true", "lstruct.aulonglong <= lstruct.aslonglong");
		checkExpr("true", "lstruct.afloat <= lstruct.adouble");
		// <
		checkExpr("false", "lstruct.achar < 49");
		checkExpr("false", "lstruct.ashort < 4");
		checkExpr("false", "lstruct.aint < 7");
		checkExpr("false", "lstruct.along < 10");
		checkExpr("false", "lstruct.aslonglong < 14");
		checkExpr("false", "lstruct.afloat < 15.0F");
		checkExpr("false", "lstruct.adouble < 16.0");
		checkExpr("true", "lstruct.achar < lstruct.aschar");
		checkExpr("true", "lstruct.ashort < lstruct.aushort");
		checkExpr("true", "lstruct.aint < lstruct.asint");
		checkExpr("true", "lstruct.along < lstruct.aulong");
		checkExpr("true", "lstruct.aulonglong < lstruct.aslonglong");
		checkExpr("true", "lstruct.afloat < lstruct.adouble");
		// &&
		checkExpr("true", "lstruct.achar && 49");
		checkExpr("true", "lstruct.ashort && 4");
		checkExpr("true", "lstruct.aint && 7");
		checkExpr("true", "lstruct.along && 10");
		checkExpr("true", "lstruct.aslonglong && 14");
		checkExpr("true", "lstruct.afloat && 15.0F");
		checkExpr("true", "lstruct.adouble && 16.0");
		checkExpr("true", "lstruct.achar || lstruct.aschar");
		checkExpr("true", "lstruct.ashort || lstruct.aushort");
		checkExpr("true", "lstruct.aint || lstruct.asint");
		checkExpr("true", "lstruct.along || lstruct.aulong");
		checkExpr("true", "lstruct.aulonglong || lstruct.aslonglong");
		checkExpr("true", "lstruct.afloat || lstruct.adouble");
		// ||
		checkExpr("true", "lstruct.achar || 49");
		checkExpr("true", "lstruct.ashort || 4");
		checkExpr("true", "lstruct.aint || 7");
		checkExpr("true", "lstruct.along || 10");
		checkExpr("true", "lstruct.aslonglong || 14");
		checkExpr("true", "lstruct.afloat || 15.0F");
		checkExpr("true", "lstruct.adouble || 16.0");
		checkExpr("true", "lstruct.achar || lstruct.aschar");
		checkExpr("true", "lstruct.ashort || lstruct.aushort");
		checkExpr("true", "lstruct.aint || lstruct.asint");
		checkExpr("true", "lstruct.along || lstruct.aulong");
		checkExpr("true", "lstruct.aulonglong || lstruct.aslonglong");
		checkExpr("true", "lstruct.afloat || lstruct.adouble");

		// arithmetic operations
		// &
		checkExpr("49", "lstruct.achar & 49");
		checkExpr("4", "lstruct.ashort & 4");
		checkExpr("7", "lstruct.aint & 7");
		checkExpr("10", "lstruct.along & 10");
		checkExpr("14", "lstruct.aslonglong & 14");
		checkExpr("0.0", "lstruct.afloat & 15.0F");
		checkExpr("0.0", "lstruct.adouble & 16.0");
		// |
		checkExpr("49", "lstruct.achar | 0");
		checkExpr("4", "lstruct.ashort | 0");
		checkExpr("7", "lstruct.aint | 0");
		checkExpr("10", "lstruct.along | 0");
		checkExpr("14", "lstruct.aslonglong | 0");
		checkExpr("0.0", "lstruct.afloat | 0.0F");
		checkExpr("0.0", "lstruct.adouble | 0.0");
		// ^
		checkExpr("49", "lstruct.achar ^ 0");
		checkExpr("4", "lstruct.ashort ^ 0");
		checkExpr("7", "lstruct.aint ^ 0");
		checkExpr("10", "lstruct.along ^ 0");
		checkExpr("14", "lstruct.aslonglong ^ 0");
		checkExpr("0.0", "lstruct.afloat ^ 0.0F");
		checkExpr("0.0", "lstruct.adouble ^ 0.0");
		// +
		checkExpr("50", "lstruct.achar + 1");
		checkExpr("5", "lstruct.ashort + 1");
		checkExpr("8", "lstruct.aint + 1");
		checkExpr("11", "lstruct.along + 1");
		checkExpr("15", "lstruct.aslonglong + 1");
		checkExpr("16.0", "lstruct.afloat + 1.0F");
		checkExpr("17.0", "lstruct.adouble + 1.0");
		// -
		checkExpr("48", "lstruct.achar - 1");
		checkExpr("3", "lstruct.ashort - 1");
		checkExpr("6", "lstruct.aint - 1");
		checkExpr("9", "lstruct.along - 1");
		checkExpr("13", "lstruct.aslonglong - 1");
		checkExpr("14.0", "lstruct.afloat - 1.0F");
		checkExpr("15.0", "lstruct.adouble - 1.0");
		// *
		checkExpr("98", "lstruct.achar * 2");
		checkExpr("8", "lstruct.ashort * 2");
		checkExpr("14", "lstruct.aint * 2");
		checkExpr("20", "lstruct.along * 2");
		checkExpr("28", "lstruct.aslonglong * 2");
		checkExpr("30.0", "lstruct.afloat * 2.0F");
		checkExpr("32.0", "lstruct.adouble * 2.0");
		// /
		checkExpr("24", "lstruct.achar / 2");
		checkExpr("2", "lstruct.ashort / 2");
		checkExpr("3", "lstruct.aint / 2");
		checkExpr("5", "lstruct.along / 2");
		checkExpr("7", "lstruct.aslonglong / 2");
		checkExpr("7.5", "lstruct.afloat / 2.0F");
		checkExpr("8.0", "lstruct.adouble / 2.0");
		// %
		checkExpr("1", "lstruct.achar % 2");
		checkExpr("0", "lstruct.ashort % 2");
		checkExpr("1", "lstruct.aint % 2");
		checkExpr("0", "lstruct.along % 2");
		checkExpr("0", "lstruct.aslonglong % 2");
		checkExpr("1.0", "lstruct.afloat % 2.0F");
		checkExpr("0.0", "lstruct.adouble % 2.0");
		// <<
		checkExpr("98", "lstruct.achar << 1");
		checkExpr("8", "lstruct.ashort << 1");
		checkExpr("14", "lstruct.aint << 1");
		checkExpr("20", "lstruct.along << 1");
		checkExpr("28", "lstruct.aslonglong << 1");
		checkExpr("0.0", "lstruct.afloat << 1");
		checkExpr("0.0", "lstruct.adouble << 1");
		// >>
		checkExpr("24", "lstruct.achar >> 1");
		checkExpr("2", "lstruct.ashort >> 1");
		checkExpr("3", "lstruct.aint >> 1");
		checkExpr("5", "lstruct.along >> 1");
		checkExpr("7", "lstruct.aslonglong >> 1");
		checkExpr("0.0", "lstruct.afloat >> 1");
		checkExpr("0.0", "lstruct.adouble >> 1");

		// unary operations
		// +
		checkExpr(intType, "49", "+lstruct.achar");
		checkExpr(intType, "4", "+lstruct.ashort");
		checkExpr(intType, "7", "+lstruct.aint");
		checkExpr(longType, "10", "+lstruct.along");
		checkExpr("14", "+lstruct.aslonglong");
		checkExpr(floatType, "15.0", "+lstruct.afloat");
		checkExpr(doubleType, "16.0", "+lstruct.adouble");
		// -
		checkExpr(intType, "-49", "-lstruct.achar");
		checkExpr("-4", "-lstruct.ashort");
		checkExpr("-7", "-lstruct.aint");
		checkExpr("-10", "-lstruct.along");
		checkExpr("-14", "-lstruct.aslonglong");
		checkExpr("-15.0", "-lstruct.afloat");
		checkExpr("-16.0", "-lstruct.adouble");
		// !
		checkExpr("false", "!lstruct.achar");
		checkExpr("false", "!lstruct.ashort");
		checkExpr("false", "!lstruct.aint");
		checkExpr("false", "!lstruct.along");
		checkExpr("false", "!lstruct.aslonglong");
		checkExpr("false", "!lstruct.afloat");
		checkExpr("false", "!lstruct.adouble");
		// ~
		checkExpr("-5", "~lstruct.ashort");
		checkExpr("-8", "~lstruct.aint");
		checkExpr("-11", "~lstruct.along");
		checkExpr("-15", "~lstruct.aslonglong");
		checkExpr("0.0", "~lstruct.afloat");
		checkExpr("0.0", "~lstruct.adouble");
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

		checkExprNoError("larray");
		checkExprNoError("&larray");
		
		checkExpr("0", "larray[0]");
		checkExpr("1", "larray[1]");
		checkExpr("2", "larray[2]");
		checkExpr("37", "larray[37]");
		checkExpr("38", "larray[38]");
		checkExpr("39", "larray[39]");

		// logical operations
		// ==
		checkExpr("true", "larray[0] == 0");
		checkExpr("false", "larray[0] == 1");
		checkExpr("true", "39 == larray[39]");
		checkExpr("false", "larray[39] == 40");
		checkExpr("false", "larray[0] == larray[39]");
		// !=
		checkExpr("true", "larray[0] != 1");
		checkExpr("false", "larray[0] != 0");
		checkExpr("true", "40 != larray[39]");
		checkExpr("false", "larray[39] != 39");
		checkExpr("true", "larray[0] != larray[39]");
		// >=
		checkExpr("true", "larray[0] >= 0");
		checkExpr("false", "larray[0] >= 41");
		checkExpr("true", "larray[39] >= 1");
		checkExpr("false", "38 >= larray[39]");
		checkExpr("false", "larray[0] >= larray[39]");
		// >
		checkExpr("true", "larray[0] > -1");
		checkExpr("false", "larray[0] > 40");
		checkExpr("true", "larray[39] > 0");
		checkExpr("false", "38 > larray[39]");
		checkExpr("false", "larray[0] > larray[39]");
		// <=
		checkExpr("true", "larray[0] <= 40");
		checkExpr("false", "larray[0] <= -1");
		checkExpr("true", "38 <= larray[39]");
		checkExpr("false", "larray[39] <= 0");
		checkExpr("true", "larray[0] <= larray[39]");
		// <
		checkExpr("true", "larray[0] < 41");
		checkExpr("false", "larray[0] < -1");
		checkExpr("true", "larray[39] < 40");
		checkExpr("false", "larray[39] < 1");
		checkExpr("true", "larray[0] < larray[39]");
		// &&
		checkExpr("false", "larray[0] && 40");
		checkExpr("true", "1 && larray[39]");
		checkExpr("false", "larray[0] && larray[39]");
		// ||
		checkExpr("true", "larray[0] || 40");
		checkExpr("true", "larray[39] || 1");
		checkExpr("true", "larray[0] || larray[39]");

		// arithmetic operations
		// &
		checkExpr("1", "larray[39] & 1");
		checkExpr("0", "larray[0] & larray[39]");
		// |
		checkExpr("0", "larray[0] | 0");
		checkExpr("39", "0 | larray[39]");
		checkExpr("39", "larray[1] | larray[38]");
		// ^
		checkExpr("0", "larray[0] ^ 0");
		checkExpr("39", "larray[39] ^ 0");
		checkExpr("38", "larray[1] ^ larray[39]");
		// +
		checkExpr("1", "larray[0] + 1");
		checkExpr("40", "1 + larray[39]");
		checkExpr("39", "larray[0] + larray[39]");
		// -
		checkExpr("-1", "larray[0] - 1");
		checkExpr("38", "larray[39] - 1");
		checkExpr("-39", "larray[0] - larray[39]");
		// *
		checkExpr("2", "larray[1] * 2");
		checkExpr("78", "2 * larray[39]");
		checkExpr("39", "larray[1] * larray[39]");
		// /
		checkExpr("0", "larray[0] / 2");
		checkExpr("19", "larray[39] / 2");
		checkExpr("39", "larray[39] / larray[1]");
		// %
		checkExpr("0", "larray[0] % 2");
		checkExpr("1", "larray[39] % 2");
		checkExpr("1", "larray[1] % larray[39]");
		// <<
		checkExpr("0", "larray[0] << 1");
		checkExpr("78", "larray[39] << 1");
		checkExpr("16", "larray[2] << larray[3]");
		// >>
		checkExpr("0", "larray[0] >> 1");
		checkExpr("19", "larray[39] >> 1");
		checkExpr("4", "larray[39] >> larray[3]");

		// unary operations
		// +
		checkExpr("0", "+larray[0]");
		checkExpr("39", "+larray[39]");
		// -
		checkExpr("0", "-larray[0]");
		checkExpr("-39", "-larray[39]");
		// !
		checkExpr("true", "!larray[0]");
		checkExpr("false", "!larray[39]");
		// ~
		checkExpr("-2", "~larray[1]");
		checkExpr("-40", "~larray[39]");
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

		checkExprNoError("lunion");
		checkExprNoError("&lunion");
		checkExpr("2", "lunion.x");
		checkExpr("2", "lunion.y");

		// logical operations
		// ==
		checkExpr("true", "lunion.x == 2");
		checkExpr("false", "lunion.x == 1");
		checkExpr("true", "lunion.y == 2");
		checkExpr("false", "lunion.y == 1");
		checkExpr("true", "lunion.x == lunion.y");
		// !=
		checkExpr("false", "lunion.x != 2");
		checkExpr("true", "lunion.x != 1");
		checkExpr("false", "lunion.y != 2");
		checkExpr("true", "lunion.y != 1");
		checkExpr("false", "lunion.x != lunion.y");
		// >=
		checkExpr("true", "lunion.x >= 2");
		checkExpr("false", "lunion.x >= 3");
		checkExpr("true", "lunion.y >= 2");
		checkExpr("true", "2 >= lunion.y");
		checkExpr("false", "lunion.y >= 3");
		checkExpr("true", "lunion.x >= lunion.y");
		// >
		checkExpr("false", "lunion.x > 2");
		checkExpr("true", "lunion.x > 1");
		checkExpr("false", "lunion.y > 2");
		checkExpr("true", "lunion.y > 1");
		checkExpr("false", "lunion.x > lunion.y");
		// <=
		checkExpr("true", "lunion.x <= 2");
		checkExpr("false", "lunion.x <= 1");
		checkExpr("true", "lunion.y <= 2");
		checkExpr("false", "lunion.y <= 1");
		checkExpr("true", "lunion.x <= lunion.y");
		// <
		checkExpr("false", "lunion.x < 2");
		checkExpr("true", "lunion.x < 3");
		checkExpr("false", "lunion.y < 2");
		checkExpr("true", "lunion.y < 3");
		checkExpr("false", "lunion.x < lunion.y");
		// &&
		checkExpr("true", "lunion.x && 2");
		checkExpr("true", "1 && lunion.x");
		checkExpr("false", "lunion.x && 0");
		checkExpr("false", "0 && lunion.x");
		checkExpr("true", "lunion.y && 2");
		checkExpr("true", "1 && lunion.y");
		checkExpr("true", "lunion.x && lunion.y");
		// ||
		checkExpr("true", "lunion.x || 2");
		checkExpr("true", "lunion.x || 1");
		checkExpr("true", "lunion.y || 2");
		checkExpr("true", "lunion.y || 1");
		checkExpr("true", "lunion.x || lunion.y");

		// arithmetic operations
		// &
		checkExpr("2", "lunion.x & 2");
		checkExpr("0", "lunion.x & 1");
		checkExpr("2", "lunion.y & 2");
		checkExpr("0", "lunion.y & 1");
		checkExpr("2", "lunion.x & lunion.y");
		// |
		checkExpr("2", "lunion.x | 2");
		checkExpr("3", "lunion.x | 1");
		checkExpr("2", "lunion.y | 2");
		checkExpr("3", "lunion.y | 1");
		checkExpr("2", "lunion.x | lunion.y");
		// ^
		checkExpr("0", "lunion.x ^ 2");
		checkExpr("3", "lunion.x ^ 1");
		checkExpr("0", "lunion.y ^ 2");
		checkExpr("3", "lunion.y ^ 1");
		checkExpr("0", "lunion.x ^ lunion.y");
		// +
		checkExpr("4", "lunion.x + 2");
		checkExpr("3", "lunion.x + 1");
		checkExpr("4", "lunion.y + 2");
		checkExpr("3", "lunion.y + 1");
		checkExpr("4", "lunion.x + lunion.y");
		// -
		checkExpr("0", "lunion.x - 2");
		checkExpr("1", "lunion.x - 1");
		checkExpr("0", "lunion.y - 2");
		checkExpr("1", "lunion.y - 1");
		checkExpr("0", "lunion.x - lunion.y");
		// *
		checkExpr("4", "lunion.x * 2");
		checkExpr("2", "lunion.x * 1");
		checkExpr("4", "lunion.y * 2");
		checkExpr("2", "lunion.y * 1");
		checkExpr("4", "lunion.x * lunion.y");
		// /
		checkExpr("1", "lunion.x / 2");
		checkExpr("2", "lunion.x / 1");
		checkExpr("1", "lunion.y / 2");
		checkExpr("2", "lunion.y / 1");
		checkExpr("1", "lunion.x / lunion.y");
		// %
		checkExpr("0", "lunion.x % 2");
		checkExpr("0", "lunion.x % 1");
		checkExpr("0", "lunion.y % 2");
		checkExpr("0", "lunion.y % 1");
		checkExpr("0", "lunion.x % lunion.y");
		// <<
		checkExpr("8", "lunion.x << 2");
		checkExpr("4", "lunion.x << 1");
		checkExpr("8", "lunion.y << 2");
		checkExpr("4", "lunion.y << 1");
		checkExpr("8", "lunion.x << lunion.y");
		// >>
		checkExpr("0", "lunion.x >> 2");
		checkExpr("1", "lunion.x >> 1");
		checkExpr("0", "lunion.y >> 2");
		checkExpr("1", "lunion.y >> 1");
		checkExpr("0", "lunion.x >> lunion.y");

		// unary operations
		// +
		checkExpr("2", "+lunion.x");
		checkExpr("2", "+lunion.y");
		// -
		checkExpr("-2", "-lunion.x");
		checkExpr("-2", "-lunion.y");
		// !
		checkExpr("false", "!lunion.x");
		checkExpr("false", "!lunion.y");
		checkExpr("true", "!!lunion.y");
		// ~
		checkExpr("-3", "~lunion.x");
		checkExpr("-3", "~lunion.y");
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

		checkExprNoError("lbitfield");
		checkExpr("1", "lbitfield.x");
		checkExpr("2", "lbitfield.y");
		checkExpr("3", "lbitfield.z");
		checkExpr("26", "lbitfield.w");

		// logical operations
		// ==
		checkExpr("true", "lbitfield.x == 1");
		checkExpr("false", "lbitfield.x == 0");
		checkExpr("true", "lbitfield.y == 2");
		checkExpr("false", "lbitfield.y == 0");
		checkExpr("false", "lbitfield.x == lbitfield.y");
		// !=
		checkExpr("true", "lbitfield.x != 0");
		checkExpr("false", "lbitfield.x != 1");
		checkExpr("true", "lbitfield.y != 0");
		checkExpr("false", "lbitfield.y != 2");
		checkExpr("true", "lbitfield.x != lbitfield.y");
		// >=
		checkExpr("true", "lbitfield.x >= 1");
		checkExpr("false", "lbitfield.x >= 2");
		checkExpr("true", "lbitfield.y >= 2");
		checkExpr("false", "lbitfield.y >= 3");
		checkExpr("false", "lbitfield.x >= lbitfield.y");
		// >
		checkExpr("true", "lbitfield.x > 0");
		checkExpr("false", "lbitfield.x > 1");
		checkExpr("true", "lbitfield.y > 1");
		checkExpr("false", "lbitfield.y > 2");
		checkExpr("false", "lbitfield.x > lbitfield.y");
		// <=
		checkExpr("true", "lbitfield.x <= 1");
		checkExpr("false", "lbitfield.x <= 0");
		checkExpr("true", "lbitfield.y <= 2");
		checkExpr("false", "lbitfield.y <= 1");
		checkExpr("true", "lbitfield.x <= lbitfield.y");
		// <
		checkExpr("true", "lbitfield.x < 2");
		checkExpr("false", "lbitfield.x < 1");
		checkExpr("true", "lbitfield.y < 3");
		checkExpr("false", "lbitfield.y < 2");
		checkExpr("true", "lbitfield.x < lbitfield.y");
		// &&
		checkExpr("true", "lbitfield.x && 2");
		checkExpr("true", "lbitfield.x && 1");
		checkExpr("true", "lbitfield.y && 3");
		checkExpr("true", "lbitfield.y && 2");
		checkExpr("true", "lbitfield.x && lbitfield.y");
		// ||
		checkExpr("true", "lbitfield.x || 2");
		checkExpr("true", "lbitfield.x || 1");
		checkExpr("true", "lbitfield.y || 3");
		checkExpr("true", "lbitfield.y || 2");
		checkExpr("true", "lbitfield.x || lbitfield.y");

		// arithmetic operations
		// &
		checkExpr("0", "lbitfield.x & 0");
		checkExpr("1", "lbitfield.x & 1");
		checkExpr("0", "lbitfield.y & 0");
		checkExpr("2", "lbitfield.y & 2");
		checkExpr("0", "lbitfield.x & lbitfield.y");
		// |
		checkExpr("1", "lbitfield.x | 0");
		checkExpr("1", "lbitfield.x | 1");
		checkExpr("2", "lbitfield.y | 0");
		checkExpr("2", "lbitfield.y | 2");
		checkExpr("3", "lbitfield.x | lbitfield.y");
		// ^
		checkExpr("1", "lbitfield.x ^ 0");
		checkExpr("0", "lbitfield.x ^ 1");
		checkExpr("2", "lbitfield.y ^ 0");
		checkExpr("0", "lbitfield.y ^ 2");
		checkExpr("3", "lbitfield.x ^ lbitfield.y");
		// +
		checkExpr("0", "lbitfield.x + (-1)");
		checkExpr("2", "lbitfield.x + 1");
		checkExpr("1", "lbitfield.y + (-1)");
		checkExpr("3", "lbitfield.y + 1");
		checkExpr("3", "lbitfield.x + lbitfield.y");
		// -
		checkExpr("2", "lbitfield.x - (-1)");
		checkExpr("0", "lbitfield.x - 1");
		checkExpr("3", "lbitfield.y - (-1)");
		checkExpr("1", "lbitfield.y - 1");
		checkExpr("-1", "lbitfield.x - lbitfield.y");
		// *
		checkExpr("0", "lbitfield.x * 0");
		checkExpr("1", "lbitfield.x * 1");
		checkExpr("0", "lbitfield.y * 0");
		checkExpr("4", "lbitfield.y * 2");
		checkExpr("2", "lbitfield.x * lbitfield.y");
		// /
		checkExprError(ASTEvalMessages.DivideByZero, "lbitfield.x / 0");
		checkExpr("1", "lbitfield.x / 1");
		checkExprError(ASTEvalMessages.DivideByZero, "lbitfield.y / 0");
		checkExpr("1", "lbitfield.y / 2");
		checkExpr("0", "lbitfield.x / lbitfield.y");
		// %
		checkExprError(ASTEvalMessages.DivideByZero, "lbitfield.x % 0");
		checkExpr("0", "lbitfield.x % 1");
		checkExprError(ASTEvalMessages.DivideByZero, "lbitfield.y % 0");
		checkExpr("0", "lbitfield.y % 2");
		checkExpr("1", "lbitfield.x % lbitfield.y");
		// <<
		checkExpr("1", "lbitfield.x << 0");
		checkExpr("2", "lbitfield.x << 1");
		checkExpr("2", "lbitfield.y << 0");
		checkExpr("4", "lbitfield.y << 1");
		checkExpr("4", "lbitfield.x << lbitfield.y");
		// >>
		checkExpr("1", "lbitfield.x >> 0");
		checkExpr("0", "lbitfield.x >> 1");
		checkExpr("2", "lbitfield.y >> 0");
		checkExpr("1", "lbitfield.y >> 1");
		checkExpr("0", "lbitfield.x >> lbitfield.y");

		// unary operations
		// +
		checkExpr("1", "+lbitfield.x");
		checkExpr("2", "+lbitfield.y");
		checkExpr("3", "+lbitfield.z");
		checkExpr("26", "+lbitfield.w");
		// -
		checkExpr("-1", "-lbitfield.x");
		checkExpr("-2", "-lbitfield.y");
		checkExpr("-3", "-lbitfield.z");
		checkExpr("-26", "-lbitfield.w");
		// !
		checkExpr("false", "!lbitfield.x");
		checkExpr("false", "!lbitfield.y");
		checkExpr("false", "!lbitfield.z");
		checkExpr("false", "!lbitfield.w");
		checkExpr("true", "!!lbitfield.w");
		// ~
		checkExpr("-2", "~lbitfield.x");
		checkExpr("-3", "~lbitfield.y");
		checkExpr("-4", "~lbitfield.z");
		checkExpr("-27", "~lbitfield.w");
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

		checkExprNoError("lenum");
		checkExpr("three [3]", "lenum");

		// logical operations
		// ==
		checkExpr("true", "lenum == 3");
		checkExpr("true", "lenum == three");
		checkExpr("false", "lenum == 4");
		checkExpr("false", "lenum == four");
		// !=
		checkExpr("true", "lenum != 4");
		checkExpr("true", "lenum != four");
		checkExpr("false", "lenum != 3");
		checkExpr("false", "lenum != three");
		// >=
		checkExpr("true", "lenum >= 3");
		checkExpr("true", "lenum >= three");
		checkExpr("false", "lenum >= 5");
		// >
		checkExpr("true", "lenum > 2");
		checkExpr("true", "lenum > two");
		checkExpr("false", "lenum > 4");
		checkExpr("false", "lenum > four");
		// <=
		checkExpr("true", "lenum <= 3");
		checkExpr("false", "lenum <= 2");
		// <
		checkExpr("true", "lenum < 5");
		checkExpr("false", "lenum < 3");
		// &&
		checkExpr("true", "lenum && 4");
		checkExpr("true", "lenum && 1");
		// ||
		checkExpr("true", "lenum || 4");
		checkExpr("true", "lenum || 1");

		// arithmetic operations
		// &
		checkExpr("0", "lenum & 4");
		checkExpr("3", "lenum & 3");
		// |
		checkExpr("7", "lenum | 4");
		checkExpr("3", "lenum | 3");
		// ^
		checkExpr("7", "lenum ^ 4");
		checkExpr("0", "lenum ^ 3");
		// +
		checkExpr("7", "lenum + 4");
		checkExpr("6", "lenum + 3");
		checkExpr("6", "lenum + three");
		// -
		checkExpr("-1", "lenum - 4");
		checkExpr("0", "lenum - 3");
		// *
		checkExpr("12", "lenum * 4");
		checkExpr("9", "lenum * 3");
		// /
		checkExpr("0", "lenum / 4");
		checkExpr("1", "lenum / 3");
		// %
		checkExpr("3", "lenum % 4");
		checkExpr("0", "lenum % 3");
		// <<
		checkExpr("12", "lenum << 2");
		checkExpr("24", "lenum << three");
		// >>
		checkExpr("0", "lenum >> 4");
		checkExpr("0", "lenum >> 3");

		// unary operations
		// +
		checkExpr("3", "+lenum");
		// -
		checkExpr("-3", "-lenum");
		// !
		checkExpr("false", "!lenum");
		checkExpr("true", "!!lenum");
		// ~
		checkExpr("-4", "~lenum");
	}


	@Override
	public String getAlbumName() {
		return "ExpressionsAggregatesAndEnums.dsa";
	}

}
