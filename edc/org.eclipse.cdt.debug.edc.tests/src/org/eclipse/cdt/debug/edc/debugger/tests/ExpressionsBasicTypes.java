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
import org.eclipse.cdt.debug.edc.internal.formatter.FormatExtensionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests of expression evaluation using basic types.
 * <p>
 * Note: if you want to determine the "real type" for an expression, it is convenient to
 * use a C++ compiler.  Make a program like this:
 * 
 * <pre>
	typeof(...your expression...) foo;  
   </pre>
   
   Build with your favorite compiler and debug info, disassemble to see what type the
   compiler picked.
 */
public class ExpressionsBasicTypes extends BaseExpressionTest {


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
	public void testExpressionsWithoutVariables() throws Exception {

		// expressions without operators
		// No booleans, which need logical operators (e.g. ==, !=, etc.)

		checkExpr(charType, "98 ('b')", "'b'"); // char
		checkExpr(wcharType, "0x3039 (L'\u3039')", "L'\\u3039'"); // wchar_t
		checkExpr(intType, "15", "15"); // int
		checkExpr(longType, "15", "15L"); // long
		checkExpr(floatType, "1.5678", "1.5678F"); // float
		checkExpr(doubleType, "234.567", "234.567"); // double
		checkExpr(longDoubleType, "234.567", "234.567L"); // double
		//checkExpr(doubleType, "234.567", "234.567"); // double

		// logical operations

		// ==
		checkExpr(boolType, "true", "'b' == 'b'"); // char
		checkExpr(boolType, "false", "'c' == 'b'"); // char
		checkExpr(boolType, "true", "5 == 5"); // int
		checkExpr(boolType, "false", "6 == 5"); // int
		checkExpr(boolType, "true", "5L == 5L"); // long
		checkExpr(boolType, "false", "6l == 5l"); // long
		checkExpr(boolType, "true", "5.5F == 5.5F"); // float
		checkExpr(boolType, "false", "6.5F == 5.5F"); // float
		checkExpr(boolType, "true", "5.5 == 5.5"); // double
		checkExpr(boolType, "false", "6.5 == 5.5"); // double
		checkExpr(boolType, "true", "(5 == 5) == (6 == 6)");// boolean
		checkExpr(boolType, "false", "(5 == 5) == (6 == 5)");// boolean
		// !=
		checkExpr(boolType, "false", "'b' != 'b'"); // char
		checkExpr(boolType, "true", "'c' != 'b'"); // char
		checkExpr(boolType, "false", "5 != 5"); // int
		checkExpr(boolType, "true", "6 != 5"); // int
		checkExpr(boolType, "false", "5l != 5l"); // long
		checkExpr(boolType, "true", "6L != 5L"); // long
		checkExpr(boolType, "false", "5.5F != 5.5F"); // float
		checkExpr(boolType, "true", "6.5F != 5.5F"); // float
		checkExpr(boolType, "false", "5.5 != 5.5"); // double
		checkExpr(boolType, "true", "6.5 != 5.5"); // double
		checkExpr(boolType, "false", "(5 == 5) != (6 == 6)");// boolean
		checkExpr(boolType, "true", "(5 == 5) != (6 == 5)");// boolean
		// >=
		checkExpr(boolType, "true", "'c' >= 'b'"); // char
		checkExpr(boolType, "false", "'b' >= 'c'"); // char
		checkExpr(boolType, "true", "6 >= 5"); // int
		checkExpr(boolType, "false", "5 >= 6"); // int
		checkExpr(boolType, "true", "6L >= 5L"); // long
		checkExpr(boolType, "false", "5L >= 6L"); // long
		checkExpr(boolType, "true", "6.5F >= 5.5F"); // float
		checkExpr(boolType, "false", "5.5F >= 6.5F"); // float
		checkExpr(boolType, "true", "6.5 >= 5.5"); // double
		checkExpr(boolType, "false", "5.5 >= 6.5"); // double
		checkExpr(boolType, "true", "(5 == 5) >= (6 == 5)");// boolean
		checkExpr(boolType, "true", "(5 == 5) >= (6 == 6)");// boolean
		// >
		checkExpr(boolType, "true", "'c' > 'b'"); // char
		checkExpr(boolType, "false", "'b' > 'c'"); // char
		checkExpr(boolType, "true", "6 > 5"); // int
		checkExpr(boolType, "false", "5 > 6"); // int
		checkExpr(boolType, "true", "6L > 5L"); // long
		checkExpr(boolType, "false", "5L > 6L"); // long
		checkExpr(boolType, "true", "6.5F > 5.5F"); // float
		checkExpr(boolType, "false", "5.5F > 6.5F"); // float
		checkExpr(boolType, "true", "6.5 > 5.5"); // double
		checkExpr(boolType, "false", "5.5 > 6.5"); // double
		checkExpr(boolType, "true", "(5 == 5) > (6 == 5)"); // boolean
		checkExpr(boolType, "false", "(5 == 5) > (6 == 6)"); // boolean
		// <=
		checkExpr(boolType, "false", "'c' <= 'b'"); // char
		checkExpr(boolType, "true", "'b' <= 'c'"); // char
		checkExpr(boolType, "false", "6 <= 5"); // int
		checkExpr(boolType, "true", "5 <= 6"); // int
		checkExpr(boolType, "false", "6L <= 5L"); // long
		checkExpr(boolType, "true", "5L <= 6L"); // long
		checkExpr(boolType, "false", "6.5F <= 5.5F"); // float
		checkExpr(boolType, "true", "5.5F <= 6.5F"); // float
		checkExpr(boolType, "false", "6.5 <= 5.5"); // double
		checkExpr(boolType, "true", "5.5 <= 6.5"); // double
		checkExpr(boolType, "false", "(5 == 5) <= (6 == 5)");// boolean
		checkExpr(boolType, "true", "(5 == 5) <= (6 == 6)");// boolean
		// <
		checkExpr(boolType, "false", "'c' < 'b'"); // char
		checkExpr(boolType, "true", "'b' < 'c'"); // char
		checkExpr(boolType, "false", "6 < 5"); // int
		checkExpr(boolType, "true", "5 < 6"); // int
		checkExpr(boolType, "false", "6L < 5L"); // long
		checkExpr(boolType, "true", "5L < 6L"); // long
		checkExpr(boolType, "false", "6.5F < 5.5F"); // float
		checkExpr(boolType, "true", "5.5F < 6.5F"); // float
		checkExpr(boolType, "false", "6.5 < 5.5"); // double
		checkExpr(boolType, "true", "5.5 < 6.5"); // double
		checkExpr(boolType, "false", "(5 == 5) < (6 == 5)"); // boolean
		checkExpr(boolType, "false", "(5 == 5) < (6 == 6)"); // boolean
		// &&
		checkExpr(boolType, "true", "'c' && 'b'"); // char
		checkExpr(boolType, "true", "6 && 5"); // int
		checkExpr(boolType, "false", "6 && 0"); // int
		checkExpr(boolType, "false", "0 && 6"); // int
		checkExpr(boolType, "true", "6L && 5L"); // long
		checkExpr(boolType, "true", "6.5F && 5.5F"); // float
		checkExpr(boolType, "false", "6.5F && 0"); // float
		checkExpr(boolType, "true", "6.5 && 5.5"); // double
		checkExpr(boolType, "false", "'\\0' && 5.5"); 
		checkExpr(boolType, "false", "(5 == 5) && (6 == 5)");// boolean
		checkExpr(boolType, "true", "(5 == 5) && (6 == 6)");// boolean
		// ||
		checkExpr(boolType, "true", "'c' || 'b'"); // char
		checkExpr(boolType, "true", "6 || 5"); // int
		checkExpr(boolType, "true", "6L || 5L"); // long
		checkExpr(boolType, "true", "6.5F || 5.5F"); // float
		checkExpr(boolType, "true", "6.5 || 5.5"); // double
		checkExpr(boolType, "false", "0 || 0L");
		checkExpr(boolType, "true", "(5 == 5) || (6 == 5)");// boolean
		checkExpr(boolType, "true", "(5 == 5) || (6 == 6)");// boolean
		checkExpr(boolType, "true", "(5 == 6) || (6 == 6)");// boolean

		// arithmetic operations

		// &
		checkExpr(intType, "98", "'c'&'b'"); // char
		checkExpr(intType, "4", "6&5"); // int
		checkExpr(longType, "4", "6L&5L"); // long
		checkExpr(floatType, "0.0", "6.5F&5.5F"); // float
		checkExpr(doubleType, "0.0", "6.5&5.5"); // double
		checkExpr(intType, "0", "(5 == 5)&(6 == 5)"); // boolean
		// |
		checkExpr(intType, "99", "'c' |'b'"); // char
		checkExpr(intType, "7", "6 |5"); // int
		checkExpr(longType, "7", "6L |5L"); // long
		checkExpr(floatType, "0.0", "6.5F |5.5F"); // float
		checkExpr(doubleType, "0.0", "6.5 |5.5"); // double
		checkExpr(intType, "1", "(5 == 5) |(6 == 5)"); // boolean
		// ^
		checkExpr(intType, "1", "'c'^ 'b'"); // char
		checkExpr(intType, "3", "6^ 5"); // int
		checkExpr(longType, "3", "6L^ 5L"); // long
		checkExpr(floatType, "0.0", "6.5F^ 5.5F"); // float
		checkExpr(doubleType, "0.0", "6.5^ 5.5"); // double
		checkExpr(intType, "1", "(5 == 5)| (6 == 5)"); // boolean
		// +
		checkExpr(intType, "197", "'c' + 'b'"); // char
		checkExpr(intType, "1", "'c' + -'b'"); // char
		checkExpr(intType, "11", "6 + 5"); // int
		checkExpr(intType, "11", "6- -5"); // int
		checkExpr(longType, "11", "6L + 5L"); // long
		checkExpr(longType, "11", "6L- -5L"); // long
		checkExpr(floatType, "12.0", "6.5F + 5.5F"); // float
		checkExpr(floatType, "12.0", "6.5F- -5.5F"); // float
		checkExpr(doubleType, "12.0", "6.5 + 5.5"); // double
		checkExpr(doubleType, "12.0", "6.5- -5.5"); // double
		checkExpr(intType, "1", "(5 == 5) + (6 == 5)"); // boolean
		// -
		checkExpr(intType, "1", "'c'-'b'"); // char
		checkExpr(intType, "1", " 6-5"); // int
		checkExpr(longType, "1", " 6L-5L"); // long
		checkExpr(floatType, "1.0", " 6.5F-5.5F"); // float
		checkExpr(doubleType, "1.0", " 6.5-5.5"); // double
		checkExpr(intType, "1", "(5 == 5) - (6 == 5)"); // boolean
		// *
		checkExpr(intType, "9702", "'c'*'b'"); // char
		checkExpr(intType, "30", "6*5"); // int
		checkExpr(longType, "30", "6L*5L"); // long
		checkExpr(floatType, "35.75", "6.5F*5.5F"); // float
		checkExpr(doubleType, "35.75", "6.5*5.5"); // double
		checkExpr(intType, "0", "(5 == 5) * (6 == 5)"); // boolean
		// /
		checkExpr(intType, "1", "'c' / 'b'"); // char
		checkExpr(intType, "1", "6 / 5"); // int
		checkExprError(ASTEvalMessages.DivideByZero, "6 / 0"); // int
		checkExpr(longType, "1", "6L / 5L"); // long
		checkExprError(ASTEvalMessages.DivideByZero, "6L / 0L"); // long
		checkExpr(floatType, "1.1818181", "6.5F / 5.5F"); // float
		checkExpr(doubleType, "1.1818181818181819", "6.5 / 5.5"); // double
		checkExprError(ASTEvalMessages.DivideByZero, "(5 == 5) / (6 == 5)"); // boolean
		checkExpr(intType, "0", "(6 == 5) / (6 == 6)"); // boolean
		checkExpr(intType, "1", "(5 == 5) / (6 == 6)"); // boolean
		// %
		checkExpr(intType, "1", "'c' % 'b'"); // char
		checkExpr(intType, "1", "6 % 5"); // int
		checkExprError(ASTEvalMessages.DivideByZero, "6 % 0"); // int
		checkExpr(longType, "1", "6L % 5L"); // long
		checkExprError(ASTEvalMessages.DivideByZero, "6L % 0L"); // long
		checkExpr(floatType, "1.0", "6.5F % 5.5F"); // float
		checkExpr(doubleType, "1.0", "6.5 % 5.5"); // double
		checkExprError(ASTEvalMessages.DivideByZero, "(5 == 5) % (6 == 5)"); // boolean
		// <<
		checkExpr(intType, "396", "'c' << 2"); // char
		checkExpr(intType, "192", "6 << 5"); // int
		checkExpr(longType, "192", "6L << 5L"); // long
		checkExpr(floatType, "0.0", "6.5F << 5.5F"); // float
		checkExpr(doubleType, "0.0", "6.5 << 5.5"); // double
		checkExpr(intType, "1", "(5 == 5) << (6 == 5)"); // boolean
		// >>
		checkExpr(intType, "12", "'c' >> 3"); // char
		checkExpr(intType, "1", "6 >> 2"); // int
		checkExpr(longType, "1", "6L >> 2L"); // long
		checkExpr(floatType, "0.0", "6.5F >> 5.5F"); // float
		checkExpr(doubleType, "0.0", "6.5 >> 5.5"); // double
		checkExpr(intType, "1", "(5 == 5) >> (6 == 5)"); // boolean
		// TODO: ->
		// TODO: .

		// unary operations

		// +
		checkExpr(intType, "99", "+'c'"); // char
		checkExpr(intType, "6", "+6"); // int
		checkExpr(longType, "6", "+6L"); // long
		checkExpr(floatType, "6.5", "+6.5F"); // float
		checkExpr(doubleType, "6.5", "+6.5"); // double
		checkExpr(intType, "1", "+(5 == 5)"); // boolean
		// -
		checkExpr(intType, "-99", "-'c'"); // char
		checkExpr(intType, "-6", "-6"); // int
		checkExpr(longType, "-6", "-6L"); // long
		checkExpr(floatType, "-6.5", "-6.5F"); // float
		checkExpr(doubleType, "-6.5", "-6.5"); // double
		checkExpr(intType, "-1", "-(5 == 5)"); // boolean
		// !
		checkExpr(boolType, "false", "!'c'"); // char
		checkExpr(boolType, "false", "!6"); // int
		checkExpr(boolType, "false", "!6"); // long
		checkExpr(boolType, "false", "!6.5"); // float
		checkExpr(boolType, "false", "!6.5"); // double
		checkExpr(boolType, "false", "!(5 == 5)"); // boolean
		checkExpr(boolType, "true", "!(5 == 6)"); // boolean
		// ~
		checkExpr(intType, "-100", "~'c'"); // ~'c'
		checkExpr(intType, "-7", "~6"); // int
		checkExpr(longType, "-7", "~6L"); // long
		checkExpr(floatType, "0.0", "~6.5F"); // float
		checkExpr(doubleType, "0.0", "~6.5"); // double
		checkExpr(intType, "-2", "~(5 == 5)"); // boolean
		checkExpr(intType, "-1", "~(5 == 6)"); // boolean
		// TODO: *
		// TODO: &

		// precedence
		checkExpr(intType, "783", "6 + 'a' * 8 + 14 / 7 - 4 % 3"); // char
		// &
		// int
		checkExpr(intType, "377", "(6 + 'a') * (8 + 14) / (7 - 4 % 3)"); // char
		// &
		// int
		checkExpr(longType, "55", "6L + 6l * 8L + 14l / 7L - 4l % 3L"); // long
		checkExpr(longType, "28", "6L + 6l * (8L + 14l) / (7L - 4l % 3L)"); // long
		checkExpr(floatType, "55.366665", "6.5F + 6.F * 8.5F + 14.F / 7.5F - 4.F"); // float
		checkExpr(floatType, "80.35714", "(6.5F + 6.F) * (8.5F + 14.F) / (7.5F - 4.F)"); // float
		checkExpr(doubleType, "55.36666666666667", "6.5 + 6 * 8.5 + 14 / 7.5 - 4"); // double
		checkExpr(doubleType, "80.35714285714286", "(6.5 + 6) * (8.5 + 14) / (7.5 - 4)"); // double
		checkExpr(boolType, "false", "(5 == 6) && ((6 == 5) || (5 == 5))"); // boolean
		checkExpr(boolType, "true", "((5 == 6) && (6 == 5)) || (5 == 5)"); // boolean
	}

	/**
	 * Test strings, treated as temporary char arrays. 
	 * These don't really make much sense in C/C++ but the support is there...
	 * @throws Exception
	 */
	@Test
	public void testStringExpressionsWithoutVariables() throws Exception {

		// expressions without operators
		// No booleans, which need logical operators (e.g. ==, !=, etc.)

		checkExpr(null, "\"hi\"", "\"hi\""); // string

		// logical operations

		// ==
		checkExpr(boolType, "true", "\"hi\" == \"hi\""); // string
		checkExpr(boolType, "false", "\"hi\" == \"bye\""); // string
		// !=
		checkExpr(boolType, "false", "\"hi\" != \"hi\""); // string
		checkExpr(boolType, "true", "\"hi\" != \"bye\""); // string
		// >=
		checkExpr(boolType, "true", "\"hi\" >= \"bye\""); // string
		checkExpr(boolType, "false", "\"bye\" >= \"hi\""); // string
		// >
		checkExpr(boolType, "true", "\"hi\" > \"bye\""); // string
		checkExpr(boolType, "false", "\"bye\" > \"hi\""); // string
		// <=
		checkExpr(boolType, "false", "\"hi\" <= \"bye\""); // string
		checkExpr(boolType, "true", "\"bye\" <= \"hi\""); // string
		// <
		checkExpr(boolType, "false", "\"hi\" < \"bye\""); // string
		checkExpr(boolType, "true", "\"bye\" < \"hi\""); // string
		// &&
		checkExpr(boolType, "true", "\"hi\" && \"bye\""); // string
		// ||
		checkExpr(boolType, "true", "\"hi\" || \"bye\""); // string

		// arithmetic operations

		// &
		checkExprError("\"hi\"&\"bye\""); // string
		// |
		checkExprError("\"hi\" |\"bye\""); // string
		// ^
		checkExprError("\"hi\"^ \"bye\""); // string
		// +
		checkExpr(null, "\"hibye\"", "\"hi\" + \"bye\"");// string
		// -
		checkExprError(" \"hi\"-\"bye\""); // string
		// *
		checkExprError("\"hi\"*\"bye\""); // string
		// /
		checkExprError("\"hi\" / \"bye\""); // string
		// %
		checkExprError("\"hi\" % \"bye\""); // string
		// <<
		checkExprError("\"hi\" << \"bye\""); // string
		// >>
		checkExprError("\"hi\" >> \"bye\""); // string
		// TODO: ->
		// TODO: .

		// unary operations

		// +
		checkExprError("+\"hi\""); // string
		// -
		checkExprError("-\"hi\""); // string
		// !
		checkExpr(boolType, "false", "!\"hi\""); // string
		// ~
		checkExprError("~\"hi\""); // string
		// TODO: *
		// TODO: &
	}

	/*
	 * Note: This assumes you are at a breakpoint where the following are true:
	 * local int SizeOfInt = sizeof (int)
	 * local int lint = 1024
	 * local char lchar = 'a'(97)
	 * local float lfloat = 55.55
	 * local double ldouble = 222.222
	 * local long llong = 123456789
	 * local char larray[8] = "testing" (address = 0x22ff00)
	 */
	@Test
	public void testExpressionsWithVariables() throws Exception {

		// Expressions with variables, but without operators.
		// Types should be the original types of the variables.

		checkExpr("volatile int", "4", "SizeOfInt");
		checkExpr("volatile int", "1024", "lint");
		checkExpr("volatile char", "97 ('a')", "lchar");
		checkExpr("volatile float", "55.55", "lfloat");
		checkExpr("volatile double", "222.222", "ldouble");
		checkExpr("volatile long", "123456789", "llong");
		// custom formatting of character arrays is on by default and as a string now
//		checkExpr(intType, "0x22ff00", "larray");
		checkExpr("char[8]", "\"testing\"", "larray");

		// logical operations

		// ==
		checkExpr(boolType, "true", "lint == 1024"); // int
		checkExpr(boolType, "false", "lint == 2058"); // int
		checkExpr(boolType, "true", "lchar == 97"); // char
		checkExpr(boolType, "false", "lchar == 88"); // char
		checkExpr(boolType, "true", "lfloat == lfloat"); // float
		checkExpr(boolType, "true", "lfloat == lfloat"); // float (adjust for imprecision)
		checkExpr(boolType, "false", "lfloat == 66.66"); // float
		checkExpr(boolType, "true", "ldouble == ldouble"); // double (adjust for imprecision)
		checkExpr(boolType, "false", "ldouble == 111.111"); // double
		checkExpr(boolType, "true", "llong == 123456789"); // long
		checkExpr(boolType, "false", "llong == 987654321"); // long
		// !=
		checkExpr(boolType, "true", "lint != 2058"); // int
		checkExpr(boolType, "false", "lint != 1024"); // int
		checkExpr(boolType, "true", "lchar != 88"); // char
		checkExpr(boolType, "false", "lchar != 97"); // char
		checkExpr(boolType, "true", "lfloat != 66.66"); // float
		checkExpr(boolType, "false", "lfloat != lfloat"); // float (adjust for imprecision)
		checkExpr(boolType, "true", "ldouble != 111.111"); // double
		checkExpr(boolType, "false", "ldouble != ldouble"); // double(adjust for imprecision)
		checkExpr(boolType, "true", "llong != 987654321"); // long
		checkExpr(boolType, "false", "llong != 123456789"); // long
		// >=
		checkExpr(boolType, "true", "lint >= 1024"); // int
		checkExpr(boolType, "false", "lint >= 2058"); // int
		checkExpr(boolType, "true", "lchar >= 97"); // char
		checkExpr(boolType, "false", "lchar >= 99"); // char
		checkExpr(boolType, "true", "lfloat >= 55.54"); // float (adjust for imprecision)
		checkExpr(boolType, "false", "lfloat >= 66.66"); // float
		checkExpr(boolType, "true", "ldouble >= 222.221"); // double (adjust for imprecision)
		checkExpr(boolType, "false", "ldouble >= 333.333"); // double
		checkExpr(boolType, "true", "llong >= 123456789"); // long
		checkExpr(boolType, "false", "llong >= 987654321"); // long
		// >
		checkExpr(boolType, "true", "lint > 1023"); // int
		checkExpr(boolType, "false", "lint > 2058"); // int
		checkExpr(boolType, "true", "lchar > 96"); // char
		checkExpr(boolType, "false", "lchar > 99"); // char
		checkExpr(boolType, "true", "lfloat > 55.54"); // float
		checkExpr(boolType, "false", "lfloat > 66.66"); // float
		checkExpr(boolType, "true", "ldouble > 222.221"); // double
		checkExpr(boolType, "false", "ldouble > 333.333"); // double
		checkExpr(boolType, "true", "llong > 123456788"); // long
		checkExpr(boolType, "false", "llong > 987654321"); // long
		// <=
		checkExpr(boolType, "true", "lint <= 1024"); // int
		checkExpr(boolType, "false", "lint <= 999"); // int
		checkExpr(boolType, "true", "lchar <= 97"); // char
		checkExpr(boolType, "false", "lchar <= 88"); // char
		checkExpr(boolType, "true", "lfloat <= 55.55"); // float
		checkExpr(boolType, "false", "lfloat <= 44.44"); // float
		checkExpr(boolType, "true", "ldouble <= 222.222"); // double
		checkExpr(boolType, "false", "ldouble <= 111.111"); // double
		checkExpr(boolType, "true", "llong <= 123456789"); // long
		checkExpr(boolType, "false", "llong <= 100000000"); // long
		// <
		checkExpr(boolType, "true", "lint < 1025"); // int
		checkExpr(boolType, "false", "lint < 999"); // int
		checkExpr(boolType, "true", "lchar < 98"); // char
		checkExpr(boolType, "false", "lchar < 88"); // char
		checkExpr(boolType, "true", "lfloat < 55.56"); // float
		checkExpr(boolType, "false", "lfloat < 44.44"); // float
		checkExpr(boolType, "true", "ldouble < 222.223"); // double
		checkExpr(boolType, "false", "ldouble < 111.111"); // double
		checkExpr(boolType, "true", "llong < 123456790"); // long
		checkExpr(boolType, "false", "llong < 100000000"); // long
		// &&
		checkExpr(boolType, "true", "lint && 1024"); // int
		checkExpr(boolType, "false", "lint && 0"); // int
		checkExpr(boolType, "true", "lchar && 97"); // char
		checkExpr(boolType, "true", "lfloat && 55.55"); // float
		checkExpr(boolType, "false", "0 && lfloat"); // float
		checkExpr(boolType, "true", "ldouble && 222.222"); // double
		checkExpr(boolType, "true", "llong && 123456789"); // long
		// ||
		checkExpr(boolType, "true", "lint || 1024"); // int
		checkExpr(boolType, "true", "lchar || 97"); // char
		checkExpr(boolType, "true", "lfloat || 55.55"); // float
		checkExpr(boolType, "true", "ldouble || 222.222"); // double
		checkExpr(boolType, "true", "llong || 123456789"); // long

		// arithmetic operations

		// &
		checkExpr(intType, "0", "lint & 0"); // int
		checkExpr(intType, "0", "lchar & 0"); // char
		checkExpr(floatType, "0.0", "lfloat & 0.0F"); // float
		checkExpr(doubleType, "0.0", "ldouble & 0.0"); // double
		checkExpr(longType, "0", "llong & 0"); // long
		// |
		checkExpr(intType, "1024", "lint | 0"); // int
		checkExpr(intType, "97", "lchar | 0"); // char
		checkExpr(floatType, "0.0", "lfloat | 0.0F"); // float
		checkExpr(doubleType, "0.0", "ldouble | 0.0"); // double
		checkExpr(longType, "123456789", "llong | 0"); // long
		// ^
		checkExpr(intType, "1024", "lint ^ 0"); // int
		checkExpr(intType, "97", "lchar ^ 0"); // char
		checkExpr(floatType, "0.0", "lfloat ^ 0.0F"); // float
		checkExpr(doubleType, "0.0", "ldouble ^ 0.0"); // double
		checkExpr(longType, "123456789", "llong ^ 0"); // long
		// +
		checkExpr(intType, "1025", "lint + 1"); // int
		checkExpr(intType, "98", "lchar + 1"); // char
		checkExpr(floatType, "56.55", "lfloat + 1.0F"); // float
		checkExpr(doubleType, "223.222", "ldouble + 1.0"); // double
		checkExpr(longType, "123456790", "llong + 1"); // long
		// -
		checkExpr(intType, "1023", "lint - 1"); // int
		checkExpr(intType, "96", "lchar - 1"); // char
		checkExpr(floatType, "54.55", "lfloat - 1.0F"); // float
		checkExpr(doubleType, "221.222", "ldouble - 1.0"); // double
		checkExpr(longType, "123456788", "llong - 1"); // long
		// *
		checkExpr(intType, "2048", "lint * 2"); // int
		checkExpr(intType, "1048576", "lint * lint"); // int	// was a BUG -- treated as <type>* <var>
		checkExpr(intType, "194", "lchar * 2"); // char
		checkExpr(floatType, "111.1", "lfloat * 2.0F"); // float
		checkExpr(doubleType, "444.444", "ldouble * 2.0"); // double
		checkExpr(longType, "246913578", "llong * 2"); // long
		// /
		checkExpr(intType, "512", "lint / 2"); // int
		checkExpr(intType, "48", "lchar / 2"); // char
		checkExpr(floatType, "27.775", "lfloat / 2.0F"); // float
		checkExpr(doubleType, "111.111", "ldouble / 2.0"); // double
		checkExpr(longType, "61728394", "llong / 2"); // long
		// %
		checkExpr(intType, "0", "lint % 2"); // int
		checkExpr(intType, "1", "lchar % 2"); // char
		String val = getExpressionValue("lfloat % 55.0");
		Assert.assertTrue(val, "0.55".equals(val.substring(0, 4)) || "0.54".equals(val.substring(0, 4))); // float (and imprecise)
		val = getExpressionValue("ldouble % 222.0");
		Assert.assertTrue(val, "0.222".equals(val.substring(0, 5))); // double (and imprecise)
		checkExpr(longType, "1", "llong % 2"); // long
		// <<
		checkExpr(intType, "2048", "lint << 1"); // int
		checkExpr(intType, "194", "lchar << 1"); // char
		checkExpr(floatType, "0.0", "lfloat << 1.0F"); // float
		checkExpr(doubleType, "0.0", "ldouble << 1.0"); // double
		checkExpr(longType, "246913578", "llong << 1"); // long
		// >>
		checkExpr(intType, "512", "lint >> 1"); // int
		checkExpr(intType, "48", "lchar >> 1"); // char
		checkExpr(floatType, "0.0", "lfloat >> 1.0F"); // float
		checkExpr(doubleType, "0.0", "ldouble >> 1.0"); // double
		checkExpr(longType, "61728394", "llong >> 1"); // long

		// unary operations

		// +
		checkExpr(intType, "1024", "+lint"); // int
		checkExpr(intType, "97", "+lchar"); // char	(promoted to int)
		checkExpr(floatType, "55.55", "+lfloat"); // float
		checkExpr(doubleType, "222.222", "+ldouble"); // double
		checkExpr(longType, "123456789", "+llong"); // long
		// -
		checkExpr(intType, "-1024", "-lint"); // int
		checkExpr(intType, "-97", "-lchar"); // char (promoted to int)
		checkExpr(floatType, "-55.55", "-lfloat"); // float
		checkExpr(doubleType, "-222.222", "-ldouble"); // double
		checkExpr(longType, "-123456789", "-llong"); // long
		// !
		checkExpr(boolType, "false", "!lint"); // int
		checkExpr(boolType, "false", "!lchar"); // char
		checkExpr(boolType, "false", "!lfloat"); // float
		checkExpr(boolType, "false", "!ldouble"); // double
		checkExpr(boolType, "false", "!llong"); // long
		// ~
		checkExpr(intType, "-1025", "~lint"); // int
		checkExpr(intType, "-98", "~lchar"); // char
		checkExpr(floatType, "0.0", "~lfloat"); // float
		checkExpr(doubleType, "0.0", "~ldouble"); // double
		checkExpr(longType, "-123456790", "~llong"); // long
		// TODO: *
		// TODO: &
	}

	@Override
	public String getAlbumName() {
		return "ExpressionsBasic.dsa";
	}

}
