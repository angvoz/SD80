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

import org.junit.Assert;
import org.junit.Test;

public class ExpressionsBasicTypes extends SimpleDebuggerTest {

	@Test
	public void testExpressionsWithoutVariables() throws Exception {

		// expressions without operators
		// No booleans, which need logical operators (e.g. ==, !=, etc.)

		Assert.assertEquals("b", getExpressionValue("'b'")); // char
		Assert.assertEquals("15", getExpressionValue("15")); // int
		Assert.assertEquals("15", getExpressionValue("15L")); // long
		Assert.assertEquals("1.5678", getExpressionValue("1.5678F")); // float
		Assert.assertEquals("234.567", getExpressionValue("234.567")); // double
		Assert.assertEquals("\"hi\"", getExpressionValue("\"hi\"")); // string

		// logical operations

		// ==
		Assert.assertEquals("true", getExpressionValue("'b' == 'b'")); // char
		Assert.assertEquals("false", getExpressionValue("'c' == 'b'")); // char
		Assert.assertEquals("true", getExpressionValue("5 == 5")); // int
		Assert.assertEquals("false", getExpressionValue("6 == 5")); // int
		Assert.assertEquals("true", getExpressionValue("5L == 5L")); // long
		Assert.assertEquals("false", getExpressionValue("6l == 5l")); // long
		Assert.assertEquals("true", getExpressionValue("5.5F == 5.5F")); // float
		Assert.assertEquals("false", getExpressionValue("6.5F == 5.5F")); // float
		Assert.assertEquals("true", getExpressionValue("5.5 == 5.5")); // double
		Assert.assertEquals("false", getExpressionValue("6.5 == 5.5")); // double
		Assert.assertEquals("true", getExpressionValue("\"hi\" == \"hi\"")); // string
		Assert.assertEquals("false", getExpressionValue("\"hi\" == \"bye\"")); // string
		Assert.assertEquals("true", getExpressionValue("(5 == 5) == (6 == 6)"));// boolean
		Assert.assertEquals("false", getExpressionValue("(5 == 5) == (6 == 5)"));// boolean
		// !=
		Assert.assertEquals("false", getExpressionValue("'b' != 'b'")); // char
		Assert.assertEquals("true", getExpressionValue("'c' != 'b'")); // char
		Assert.assertEquals("false", getExpressionValue("5 != 5")); // int
		Assert.assertEquals("true", getExpressionValue("6 != 5")); // int
		Assert.assertEquals("false", getExpressionValue("5l != 5l")); // long
		Assert.assertEquals("true", getExpressionValue("6L != 5L")); // long
		Assert.assertEquals("false", getExpressionValue("5.5F != 5.5F")); // float
		Assert.assertEquals("true", getExpressionValue("6.5F != 5.5F")); // float
		Assert.assertEquals("false", getExpressionValue("5.5 != 5.5")); // double
		Assert.assertEquals("true", getExpressionValue("6.5 != 5.5")); // double
		Assert.assertEquals("false", getExpressionValue("\"hi\" != \"hi\"")); // string
		Assert.assertEquals("true", getExpressionValue("\"hi\" != \"bye\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) != (6 == 6)"));// boolean
		Assert.assertEquals("true", getExpressionValue("(5 == 5) != (6 == 5)"));// boolean
		// >=
		Assert.assertEquals("true", getExpressionValue("'c' >= 'b'")); // char
		Assert.assertEquals("false", getExpressionValue("'b' >= 'c'")); // char
		Assert.assertEquals("true", getExpressionValue("6 >= 5")); // int
		Assert.assertEquals("false", getExpressionValue("5 >= 6")); // int
		Assert.assertEquals("true", getExpressionValue("6L >= 5L")); // long
		Assert.assertEquals("false", getExpressionValue("5L >= 6L")); // long
		Assert.assertEquals("true", getExpressionValue("6.5F >= 5.5F")); // float
		Assert.assertEquals("false", getExpressionValue("5.5F >= 6.5F")); // float
		Assert.assertEquals("true", getExpressionValue("6.5 >= 5.5")); // double
		Assert.assertEquals("false", getExpressionValue("5.5 >= 6.5")); // double
		Assert.assertEquals("true", getExpressionValue("\"hi\" >= \"bye\"")); // string
		Assert.assertEquals("false", getExpressionValue("\"bye\" >= \"hi\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) >= (6 == 5)"));// boolean
		Assert.assertEquals("false", getExpressionValue("(5 == 5) >= (6 == 6)"));// boolean
		// >
		Assert.assertEquals("true", getExpressionValue("'c' > 'b'")); // char
		Assert.assertEquals("false", getExpressionValue("'b' > 'c'")); // char
		Assert.assertEquals("true", getExpressionValue("6 > 5")); // int
		Assert.assertEquals("false", getExpressionValue("5 > 6")); // int
		Assert.assertEquals("true", getExpressionValue("6L > 5L")); // long
		Assert.assertEquals("false", getExpressionValue("5L > 6L")); // long
		Assert.assertEquals("true", getExpressionValue("6.5F > 5.5F")); // float
		Assert.assertEquals("false", getExpressionValue("5.5F > 6.5F")); // float
		Assert.assertEquals("true", getExpressionValue("6.5 > 5.5")); // double
		Assert.assertEquals("false", getExpressionValue("5.5 > 6.5")); // double
		Assert.assertEquals("true", getExpressionValue("\"hi\" > \"bye\"")); // string
		Assert.assertEquals("false", getExpressionValue("\"bye\" > \"hi\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) > (6 == 5)")); // boolean
		Assert.assertEquals("false", getExpressionValue("(5 == 5) > (6 == 6)")); // boolean
		// <=
		Assert.assertEquals("false", getExpressionValue("'c' <= 'b'")); // char
		Assert.assertEquals("true", getExpressionValue("'b' <= 'c'")); // char
		Assert.assertEquals("false", getExpressionValue("6 <= 5")); // int
		Assert.assertEquals("true", getExpressionValue("5 <= 6")); // int
		Assert.assertEquals("false", getExpressionValue("6L <= 5L")); // long
		Assert.assertEquals("true", getExpressionValue("5L <= 6L")); // long
		Assert.assertEquals("false", getExpressionValue("6.5F <= 5.5F")); // float
		Assert.assertEquals("true", getExpressionValue("5.5F <= 6.5F")); // float
		Assert.assertEquals("false", getExpressionValue("6.5 <= 5.5")); // double
		Assert.assertEquals("true", getExpressionValue("5.5 <= 6.5")); // double
		Assert.assertEquals("false", getExpressionValue("\"hi\" <= \"bye\"")); // string
		Assert.assertEquals("true", getExpressionValue("\"bye\" <= \"hi\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) <= (6 == 5)"));// boolean
		Assert.assertEquals("false", getExpressionValue("(5 == 5) <= (6 == 6)"));// boolean
		// <
		Assert.assertEquals("false", getExpressionValue("'c' < 'b'")); // char
		Assert.assertEquals("true", getExpressionValue("'b' < 'c'")); // char
		Assert.assertEquals("false", getExpressionValue("6 < 5")); // int
		Assert.assertEquals("true", getExpressionValue("5 < 6")); // int
		Assert.assertEquals("false", getExpressionValue("6L < 5L")); // long
		Assert.assertEquals("true", getExpressionValue("5L < 6L")); // long
		Assert.assertEquals("false", getExpressionValue("6.5F < 5.5F")); // float
		Assert.assertEquals("true", getExpressionValue("5.5F < 6.5F")); // float
		Assert.assertEquals("false", getExpressionValue("6.5 < 5.5")); // double
		Assert.assertEquals("true", getExpressionValue("5.5 < 6.5")); // double
		Assert.assertEquals("false", getExpressionValue("\"hi\" < \"bye\"")); // string
		Assert.assertEquals("true", getExpressionValue("\"bye\" < \"hi\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) < (6 == 5)")); // boolean
		Assert.assertEquals("false", getExpressionValue("(5 == 5) < (6 == 6)")); // boolean
		// &&
		Assert.assertEquals("false", getExpressionValue("'c' && 'b'")); // char
		Assert.assertEquals("false", getExpressionValue("6 && 5")); // int
		Assert.assertEquals("false", getExpressionValue("6L && 5L")); // long
		Assert.assertEquals("false", getExpressionValue("6.5F && 5.5F")); // float
		Assert.assertEquals("false", getExpressionValue("6.5 && 5.5")); // double
		Assert.assertEquals("false", getExpressionValue("\"hi\" && \"bye\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) && (6 == 5)"));// boolean
		Assert.assertEquals("true", getExpressionValue("(5 == 5) && (6 == 6)"));// boolean
		// ||
		Assert.assertEquals("false", getExpressionValue("'c' || 'b'")); // char
		Assert.assertEquals("false", getExpressionValue("6 || 5")); // int
		Assert.assertEquals("false", getExpressionValue("6L || 5L")); // long
		Assert.assertEquals("false", getExpressionValue("6.5F || 5.5F")); // float
		Assert.assertEquals("false", getExpressionValue("6.5 || 5.5")); // double
		Assert.assertEquals("false", getExpressionValue("\"hi\" || \"bye\"")); // string
		Assert.assertEquals("true", getExpressionValue("(5 == 5) || (6 == 5)"));// boolean
		Assert.assertEquals("true", getExpressionValue("(5 == 5) || (6 == 6)"));// boolean
		Assert.assertEquals("true", getExpressionValue("(5 == 6) || (6 == 6)"));// boolean

		// arithmetic operations

		// &
		Assert.assertEquals("98", getExpressionValue("'c'&'b'")); // char
		Assert.assertEquals("4", getExpressionValue("6&5")); // int
		Assert.assertEquals("4", getExpressionValue("6L&5L")); // long
		Assert.assertEquals("0.0", getExpressionValue("6.5F&5.5F")); // float
		Assert.assertEquals("0.0", getExpressionValue("6.5&5.5")); // double
		Assert.assertEquals("", getExpressionValue("\"hi\"&\"bye\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5)&(6 == 5)")); // boolean
		// |
		Assert.assertEquals("99", getExpressionValue("'c' |'b'")); // char
		Assert.assertEquals("7", getExpressionValue("6 |5")); // int
		Assert.assertEquals("7", getExpressionValue("6L |5L")); // long
		Assert.assertEquals("0.0", getExpressionValue("6.5F |5.5F")); // float
		Assert.assertEquals("0.0", getExpressionValue("6.5 |5.5")); // double
		Assert.assertEquals("", getExpressionValue("\"hi\" |\"bye\"")); // string
		Assert.assertEquals("true", getExpressionValue("(5 == 5) |(6 == 5)")); // boolean
		// ^
		Assert.assertEquals("1", getExpressionValue("'c'^ 'b'")); // char
		Assert.assertEquals("3", getExpressionValue("6^ 5")); // int
		Assert.assertEquals("3", getExpressionValue("6L^ 5L")); // long
		Assert.assertEquals("0.0", getExpressionValue("6.5F^ 5.5F")); // float
		Assert.assertEquals("0.0", getExpressionValue("6.5^ 5.5")); // double
		Assert.assertEquals("", getExpressionValue("\"hi\"^ \"bye\"")); // string
		Assert.assertEquals("true", getExpressionValue("(5 == 5)| (6 == 5)")); // boolean
		// +
		Assert.assertEquals("197", getExpressionValue("'c' + 'b'")); // char
		Assert.assertEquals("1", getExpressionValue("'c' + -'b'")); // char
		Assert.assertEquals("11", getExpressionValue("6 + 5")); // int
		Assert.assertEquals("11", getExpressionValue("6- -5")); // int
		Assert.assertEquals("11", getExpressionValue("6L + 5L")); // long
		Assert.assertEquals("11", getExpressionValue("6L- -5L")); // long
		Assert.assertEquals("12.0", getExpressionValue("6.5F + 5.5F")); // float
		Assert.assertEquals("12.0", getExpressionValue("6.5F- -5.5F")); // float
		Assert.assertEquals("12.0", getExpressionValue("6.5 + 5.5")); // double
		Assert.assertEquals("12.0", getExpressionValue("6.5- -5.5")); // double
		Assert.assertEquals("\"hibye\"", getExpressionValue("\"hi\" + \"bye\""));// string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) + (6 == 5)")); // boolean
		// -
		Assert.assertEquals("1", getExpressionValue("'c'-'b'")); // char
		Assert.assertEquals("1", getExpressionValue(" 6-5")); // int
		Assert.assertEquals("1", getExpressionValue(" 6L-5L")); // long
		Assert.assertEquals("1.0", getExpressionValue(" 6.5F-5.5F")); // float
		Assert.assertEquals("1.0", getExpressionValue(" 6.5-5.5")); // double
		Assert.assertEquals("", getExpressionValue(" \"hi\"-\"bye\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) - (6 == 5)")); // boolean
		// *
		Assert.assertEquals("9702", getExpressionValue("'c'*'b'")); // char
		Assert.assertEquals("30", getExpressionValue("6*5")); // int
		Assert.assertEquals("30", getExpressionValue("6L*5L")); // long
		Assert.assertEquals("35.75", getExpressionValue("6.5F*5.5F")); // float
		Assert.assertEquals("35.75", getExpressionValue("6.5*5.5")); // double
		Assert.assertEquals("", getExpressionValue("\"hi\"*\"bye\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) * (6 == 5)")); // boolean
		// /
		Assert.assertEquals("1", getExpressionValue("'c' / 'b'")); // char
		Assert.assertEquals("1", getExpressionValue("6 / 5")); // int
		// Assert.assertEquals("Error: " + ASTEvalMessages.DivideByZero,
		// getExpressionValue("6 / 0")); // int
		Assert.assertEquals("1", getExpressionValue("6L / 5L")); // long
		// Assert.assertEquals("Error: " + ASTEvalMessages.DivideByZero,
		// getExpressionValue("6L / 0L")); // long
		Assert.assertEquals("1.1818181", getExpressionValue("6.5F / 5.5F")); // float
		Assert.assertEquals("1.1818181818181819", getExpressionValue("6.5 / 5.5")); // double
		Assert.assertEquals("", getExpressionValue("\"hi\" / \"bye\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) / (6 == 5)")); // boolean
		// %
		Assert.assertEquals("1", getExpressionValue("'c' % 'b'")); // char
		Assert.assertEquals("1", getExpressionValue("6 % 5")); // int
		// Assert.assertEquals("Error: " + ASTEvalMessages.DivideByZero,
		// getExpressionValue("6 % 0")); // int
		Assert.assertEquals("1", getExpressionValue("6L % 5L")); // long
		// Assert.assertEquals("Error: " + ASTEvalMessages.DivideByZero,
		// getExpressionValue("6L % 0L")); // long
		Assert.assertEquals("0.0", getExpressionValue("6.5F % 5.5F")); // float
		Assert.assertEquals("0.0", getExpressionValue("6.5 % 5.5")); // double
		Assert.assertEquals("", getExpressionValue("\"hi\" % \"bye\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) % (6 == 5)")); // boolean
		// <<
		Assert.assertEquals("396", getExpressionValue("'c' << 2")); // char
		Assert.assertEquals("192", getExpressionValue("6 << 5")); // int
		Assert.assertEquals("192", getExpressionValue("6L << 5L")); // long
		Assert.assertEquals("0.0", getExpressionValue("6.5F << 5.5F")); // float
		Assert.assertEquals("0.0", getExpressionValue("6.5 << 5.5")); // double
		Assert.assertEquals("", getExpressionValue("\"hi\" << \"bye\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) << (6 == 5)")); // boolean
		// >>
		Assert.assertEquals("12", getExpressionValue("'c' >> 3")); // char
		Assert.assertEquals("1", getExpressionValue("6 >> 2")); // int
		Assert.assertEquals("1", getExpressionValue("6L >> 2L")); // long
		Assert.assertEquals("0.0", getExpressionValue("6.5F >> 5.5F")); // float
		Assert.assertEquals("0.0", getExpressionValue("6.5 >> 5.5")); // double
		Assert.assertEquals("", getExpressionValue("\"hi\" >> \"bye\"")); // string
		Assert.assertEquals("false", getExpressionValue("(5 == 5) >> (6 == 5)")); // boolean
		// TODO: ->
		// TODO: .

		// unary operations

		// +
		Assert.assertEquals("99", getExpressionValue("+'c'")); // char
		Assert.assertEquals("6", getExpressionValue("+6")); // int
		Assert.assertEquals("6", getExpressionValue("+6L")); // long
		Assert.assertEquals("6.5", getExpressionValue("+6.5F")); // float
		Assert.assertEquals("6.5", getExpressionValue("+6.5")); // double
		Assert.assertEquals("", getExpressionValue("+\"hi\"")); // string
		Assert.assertEquals("false", getExpressionValue("+(5 == 5)")); // boolean
		// -
		Assert.assertEquals("-99", getExpressionValue("-'c'")); // char
		Assert.assertEquals("-6", getExpressionValue("-6")); // int
		Assert.assertEquals("-6", getExpressionValue("-6L")); // long
		Assert.assertEquals("-6.5", getExpressionValue("-6.5F")); // float
		Assert.assertEquals("-6.5", getExpressionValue("-6.5")); // double
		Assert.assertEquals("", getExpressionValue("-\"hi\"")); // string
		Assert.assertEquals("false", getExpressionValue("-(5 == 5)")); // boolean
		// !
		Assert.assertEquals("false", getExpressionValue("!'c'")); // char
		Assert.assertEquals("false", getExpressionValue("!6")); // int
		Assert.assertEquals("false", getExpressionValue("!6L")); // long
		Assert.assertEquals("false", getExpressionValue("!6.5F")); // float
		Assert.assertEquals("false", getExpressionValue("!6.5")); // double
		Assert.assertEquals("false", getExpressionValue("!\"hi\"")); // string
		Assert.assertEquals("false", getExpressionValue("!(5 == 5)")); // boolean
		Assert.assertEquals("true", getExpressionValue("!(5 == 6)")); // boolean
		// ~
		Assert.assertEquals("-100", getExpressionValue("~'c'")); // ~'c'
		Assert.assertEquals("-7", getExpressionValue("~6")); // int
		Assert.assertEquals("-7", getExpressionValue("~6L")); // long
		Assert.assertEquals("0.0", getExpressionValue("~6.5F")); // float
		Assert.assertEquals("0.0", getExpressionValue("~6.5")); // double
		Assert.assertEquals("", getExpressionValue("~\"hi\"")); // string
		Assert.assertEquals("false", getExpressionValue("~(5 == 5)")); // boolean
		// TODO: *
		// TODO: &

		// precedence
		Assert.assertEquals("783", getExpressionValue("6 + 'a' * 8 + 14 / 7 - 4 % 3")); // char
		// &
		// int
		Assert.assertEquals("377", getExpressionValue("(6 + 'a') * (8 + 14) / (7 - 4 % 3)")); // char
		// &
		// int
		Assert.assertEquals("55", getExpressionValue("6L + 6l * 8L + 14l / 7L - 4l % 3L")); // long
		Assert.assertEquals("28", getExpressionValue("6L + 6l * (8L + 14l) / (7L - 4l % 3L)")); // long
		Assert.assertEquals("55.366665", getExpressionValue("6.5F + 6.F * 8.5F + 14.F / 7.5F - 4.F")); // float
		Assert.assertEquals("80.35714", getExpressionValue("(6.5F + 6.F) * (8.5F + 14.F) / (7.5F - 4.F)")); // float
		Assert.assertEquals("55.36666666666667", getExpressionValue("6.5 + 6 * 8.5 + 14 / 7.5 - 4")); // double
		Assert.assertEquals("80.35714285714286", getExpressionValue("(6.5 + 6) * (8.5 + 14) / (7.5 - 4)")); // double
		Assert.assertEquals("false", getExpressionValue("(5 == 6) && ((6 == 5) || (5 == 5))")); // boolean
		Assert.assertEquals("true", getExpressionValue("((5 == 6) && (6 == 5)) || (5 == 5)")); // boolean
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

		// expressions with variables, but without operators

		Assert.assertEquals("4", getExpressionValue("SizeOfInt"));
		Assert.assertEquals("1024", getExpressionValue("lint"));
		Assert.assertEquals("'a'", getExpressionValue("lchar"));
		Assert.assertEquals("55.55", getExpressionValue("lfloat"));
		Assert.assertEquals("222.222", getExpressionValue("ldouble"));
		Assert.assertEquals("123456789", getExpressionValue("llong"));
		// custom formatting of character arrays is on by default
//		Assert.assertEquals("0x22ff00", getExpressionValue("larray"));
		Assert.assertEquals("['t', 'e', 's', 't', 'i', 'n', 'g', '\\0']", getExpressionValue("larray"));

		// logical operations

		// ==
		Assert.assertEquals("true", getExpressionValue("lint == 1024")); // int
		Assert.assertEquals("false", getExpressionValue("lint == 2058")); // int
		Assert.assertEquals("true", getExpressionValue("lchar == 97")); // char
		Assert.assertEquals("false", getExpressionValue("lchar == 88")); // char
		Assert.assertEquals("true", getExpressionValue("lfloat == 55.55F")); // float
		Assert.assertEquals("false", getExpressionValue("lfloat == 66.66F")); // float
		Assert.assertEquals("true", getExpressionValue("ldouble == 222.222")); // double
		Assert.assertEquals("false", getExpressionValue("ldouble == 111.111")); // double
		Assert.assertEquals("true", getExpressionValue("llong == 123456789")); // long
		Assert.assertEquals("false", getExpressionValue("llong == 987654321")); // long
		// !=
		Assert.assertEquals("true", getExpressionValue("lint != 2058")); // int
		Assert.assertEquals("false", getExpressionValue("lint != 1024")); // int
		Assert.assertEquals("true", getExpressionValue("lchar != 88")); // char
		Assert.assertEquals("false", getExpressionValue("lchar != 97")); // char
		Assert.assertEquals("true", getExpressionValue("lfloat != 66.66F")); // float
		Assert.assertEquals("false", getExpressionValue("lfloat != 55.55F")); // float
		Assert.assertEquals("true", getExpressionValue("ldouble != 111.111")); // double
		Assert.assertEquals("false", getExpressionValue("ldouble != 222.222")); // double
		Assert.assertEquals("true", getExpressionValue("llong != 987654321")); // long
		Assert.assertEquals("false", getExpressionValue("llong != 123456789")); // long
		// >=
		Assert.assertEquals("true", getExpressionValue("lint >= 1024")); // int
		Assert.assertEquals("false", getExpressionValue("lint >= 2058")); // int
		Assert.assertEquals("true", getExpressionValue("lchar >= 97")); // char
		Assert.assertEquals("false", getExpressionValue("lchar >= 99")); // char
		Assert.assertEquals("true", getExpressionValue("lfloat >= 55.55F")); // float
		Assert.assertEquals("false", getExpressionValue("lfloat >= 66.66F")); // float
		Assert.assertEquals("true", getExpressionValue("ldouble >= 222.222")); // double
		Assert.assertEquals("false", getExpressionValue("ldouble >= 333.333")); // double
		Assert.assertEquals("true", getExpressionValue("llong >= 123456789")); // long
		Assert.assertEquals("false", getExpressionValue("llong >= 987654321")); // long
		// >
		Assert.assertEquals("true", getExpressionValue("lint > 1023")); // int
		Assert.assertEquals("false", getExpressionValue("lint > 2058")); // int
		Assert.assertEquals("true", getExpressionValue("lchar > 96")); // char
		Assert.assertEquals("false", getExpressionValue("lchar > 99")); // char
		Assert.assertEquals("true", getExpressionValue("lfloat > 55.54F")); // float
		Assert.assertEquals("false", getExpressionValue("lfloat > 66.66F")); // float
		Assert.assertEquals("true", getExpressionValue("ldouble > 222.221")); // double
		Assert.assertEquals("false", getExpressionValue("ldouble > 333.333")); // double
		Assert.assertEquals("true", getExpressionValue("llong > 123456788")); // long
		Assert.assertEquals("false", getExpressionValue("llong > 987654321")); // long
		// <=
		Assert.assertEquals("true", getExpressionValue("lint <= 1024")); // int
		Assert.assertEquals("false", getExpressionValue("lint <= 999")); // int
		Assert.assertEquals("true", getExpressionValue("lchar <= 97")); // char
		Assert.assertEquals("false", getExpressionValue("lchar <= 88")); // char
		Assert.assertEquals("true", getExpressionValue("lfloat <= 55.55F")); // float
		Assert.assertEquals("false", getExpressionValue("lfloat <= 44.44F")); // float
		Assert.assertEquals("true", getExpressionValue("ldouble <= 222.222")); // double
		Assert.assertEquals("false", getExpressionValue("ldouble <= 111.111")); // double
		Assert.assertEquals("true", getExpressionValue("llong <= 123456789")); // long
		Assert.assertEquals("false", getExpressionValue("llong <= 100000000")); // long
		// <
		Assert.assertEquals("true", getExpressionValue("lint < 1025")); // int
		Assert.assertEquals("false", getExpressionValue("lint < 999")); // int
		Assert.assertEquals("true", getExpressionValue("lchar < 98")); // char
		Assert.assertEquals("false", getExpressionValue("lchar < 88")); // char
		Assert.assertEquals("true", getExpressionValue("lfloat < 55.56F")); // float
		Assert.assertEquals("false", getExpressionValue("lfloat < 44.44F")); // float
		Assert.assertEquals("true", getExpressionValue("ldouble < 222.223")); // double
		Assert.assertEquals("false", getExpressionValue("ldouble < 111.111")); // double
		Assert.assertEquals("true", getExpressionValue("llong < 123456790")); // long
		Assert.assertEquals("false", getExpressionValue("llong < 100000000")); // long
		// &&
		Assert.assertEquals("false", getExpressionValue("lint && 1024")); // int
		Assert.assertEquals("false", getExpressionValue("lchar && 97")); // char
		Assert.assertEquals("false", getExpressionValue("lfloat && 55.55F")); // float
		Assert.assertEquals("false", getExpressionValue("ldouble && 222.222")); // double
		Assert.assertEquals("false", getExpressionValue("llong && 123456789")); // long
		// ||
		Assert.assertEquals("false", getExpressionValue("lint || 1024")); // int
		Assert.assertEquals("false", getExpressionValue("lchar || 97")); // char
		Assert.assertEquals("false", getExpressionValue("lfloat || 55.55F")); // float
		Assert.assertEquals("false", getExpressionValue("ldouble || 222.222")); // double
		Assert.assertEquals("false", getExpressionValue("llong || 123456789")); // long

		// arithmetic operations

		// &
		Assert.assertEquals("0", getExpressionValue("lint & 0")); // int
		Assert.assertEquals("0", getExpressionValue("lchar & 0")); // char
		Assert.assertEquals("0.0", getExpressionValue("lfloat & 0.0F")); // float
		Assert.assertEquals("0.0", getExpressionValue("ldouble & 0.0")); // double
		Assert.assertEquals("0", getExpressionValue("llong & 0")); // long
		// |
		Assert.assertEquals("1024", getExpressionValue("lint | 0")); // int
		Assert.assertEquals("97", getExpressionValue("lchar | 0")); // char
		Assert.assertEquals("0.0", getExpressionValue("lfloat | 0.0F")); // float
		Assert.assertEquals("0.0", getExpressionValue("ldouble | 0.0")); // double
		Assert.assertEquals("123456789", getExpressionValue("llong | 0")); // long
		// ^
		Assert.assertEquals("1024", getExpressionValue("lint ^ 0")); // int
		Assert.assertEquals("97", getExpressionValue("lchar ^ 0")); // char
		Assert.assertEquals("0.0", getExpressionValue("lfloat ^ 0.0F")); // float
		Assert.assertEquals("0.0", getExpressionValue("ldouble ^ 0.0")); // double
		Assert.assertEquals("123456789", getExpressionValue("llong ^ 0")); // long
		// +
		Assert.assertEquals("1025", getExpressionValue("lint + 1")); // int
		Assert.assertEquals("98", getExpressionValue("lchar + 1")); // char
		Assert.assertEquals("56.55", getExpressionValue("lfloat + 1.0F")); // float
		Assert.assertEquals("223.222", getExpressionValue("ldouble + 1.0")); // double
		Assert.assertEquals("123456790", getExpressionValue("llong + 1")); // long
		// -
		Assert.assertEquals("1023", getExpressionValue("lint - 1")); // int
		Assert.assertEquals("96", getExpressionValue("lchar - 1")); // char
		Assert.assertEquals("54.55", getExpressionValue("lfloat - 1.0F")); // float
		Assert.assertEquals("221.222", getExpressionValue("ldouble - 1.0")); // double
		Assert.assertEquals("123456788", getExpressionValue("llong - 1")); // long
		// *
		Assert.assertEquals("2048", getExpressionValue("lint * 2")); // int
		Assert.assertEquals("194", getExpressionValue("lchar * 2")); // char
		Assert.assertEquals("111.1", getExpressionValue("lfloat * 2.0F")); // float
		Assert.assertEquals("444.444", getExpressionValue("ldouble * 2.0")); // double
		Assert.assertEquals("246913578", getExpressionValue("llong * 2")); // long
		// /
		Assert.assertEquals("512", getExpressionValue("lint / 2")); // int
		Assert.assertEquals("48", getExpressionValue("lchar / 2")); // char
		Assert.assertEquals("27.775", getExpressionValue("lfloat / 2.0F")); // float
		Assert.assertEquals("111.111", getExpressionValue("ldouble / 2.0")); // double
		Assert.assertEquals("61728394", getExpressionValue("llong / 2")); // long
		// %
		Assert.assertEquals("0", getExpressionValue("lint % 2")); // int
		Assert.assertEquals("1", getExpressionValue("lchar % 2")); // char
		Assert.assertEquals("0.0", getExpressionValue("lfloat % 2.0")); // float
		Assert.assertEquals("0.0", getExpressionValue("ldouble % 2.0")); // double
		Assert.assertEquals("1", getExpressionValue("llong % 2")); // long
		// <<
		Assert.assertEquals("2048", getExpressionValue("lint << 1")); // int
		Assert.assertEquals("194", getExpressionValue("lchar << 1")); // char
		Assert.assertEquals("0.0", getExpressionValue("lfloat << 1.0F")); // float
		Assert.assertEquals("0.0", getExpressionValue("ldouble << 1.0")); // double
		Assert.assertEquals("246913578", getExpressionValue("llong << 1")); // long
		// >>
		Assert.assertEquals("512", getExpressionValue("lint >> 1")); // int
		Assert.assertEquals("48", getExpressionValue("lchar >> 1")); // char
		Assert.assertEquals("0.0", getExpressionValue("lfloat >> 1.0F")); // float
		Assert.assertEquals("0.0", getExpressionValue("ldouble >> 1.0")); // double
		Assert.assertEquals("61728394", getExpressionValue("llong >> 1")); // long

		// unary operations

		// +
		Assert.assertEquals("1024", getExpressionValue("+lint")); // int
		Assert.assertEquals("'a'", getExpressionValue("+lchar")); // char
		Assert.assertEquals("55.55", getExpressionValue("+lfloat")); // float
		Assert.assertEquals("222.222", getExpressionValue("+ldouble")); // double
		Assert.assertEquals("123456789", getExpressionValue("+llong")); // long
		// -
		Assert.assertEquals("-1024", getExpressionValue("-lint")); // int
		//Assert.assertEquals("-97", getExpressionValue("-lchar")); // char
		Assert.assertEquals("-55.55", getExpressionValue("-lfloat")); // float
		Assert.assertEquals("-222.222", getExpressionValue("-ldouble")); // double
		Assert.assertEquals("-123456789", getExpressionValue("-llong")); // long
		// !
		Assert.assertEquals("false", getExpressionValue("!lint")); // int
		Assert.assertEquals("false", getExpressionValue("!lchar")); // char
		Assert.assertEquals("false", getExpressionValue("!lfloat")); // float
		Assert.assertEquals("false", getExpressionValue("!ldouble")); // double
		Assert.assertEquals("false", getExpressionValue("!llong")); // long
		// ~
		Assert.assertEquals("-1025", getExpressionValue("~lint")); // int
		//Assert.assertEquals("-98", getExpressionValue("~lchar")); // char
		Assert.assertEquals("0.0", getExpressionValue("~lfloat")); // float
		Assert.assertEquals("0.0", getExpressionValue("~ldouble")); // double
		Assert.assertEquals("-123456790", getExpressionValue("~llong")); // long
		// TODO: *
		// TODO: &
	}

	@Override
	public String getAlbumName() {
		return "ExpressionsBasic.dsa";
	}

}
