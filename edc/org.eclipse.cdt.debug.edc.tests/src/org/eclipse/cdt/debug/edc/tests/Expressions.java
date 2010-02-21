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
package org.eclipse.cdt.debug.edc.tests;

import junit.framework.Assert;

import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.debugger.tests.SimpleDebuggerTest;
import org.eclipse.cdt.debug.edc.internal.IEDCTraceOptions;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.InstructionSequence;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.Interpreter;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.InvalidExpression;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.VariableWithValue;
import org.eclipse.osgi.framework.debug.FrameworkDebugOptions;
import org.junit.Test;

@SuppressWarnings("restriction")
public class Expressions extends SimpleDebuggerTest {

	ASTEvaluationEngine engine = new ASTEvaluationEngine();

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.debugger.tests.SimpleDebuggerTest#getAlbumName()
	 */
	@Override
	public String getAlbumName() {
		return "NewDWARF_BlackFlagMinGW.dsa";
	}
	
	@Test
	public void testExpressions() {
		FrameworkDebugOptions.getDefault().setDebugEnabled(true);
		FrameworkDebugOptions.getDefault().setOption(EDCDebugger.PLUGIN_ID + IEDCTraceOptions.EXPRESSION_PARSE_TRACE,
				"true");

		resolveWithoutVariables();
		resolveWithSimpleVariables();
	}

	private void resolveWithoutVariables() {
		// expressions without operators
		// No booleans, which need logical operators (e.g. ==, !=, etc.)

		Assert.assertEquals('b', resolve("'b'")); // char
		Assert.assertEquals(Long.valueOf(15), resolve("15")); // int (returns Long)
		Assert.assertEquals(Long.valueOf(15), resolve("15L")); // long
		Assert.assertEquals(Float.valueOf((float) 1.5678), resolve("1.5678F")); // float
		Assert.assertEquals(234.567, resolve("234.567")); // double
		Assert.assertEquals("\"hi\"", resolve("\"hi\"")); // string

		// logical operations

		// ==
		Assert.assertEquals(true, resolve("'b' == 'b'")); // char
		Assert.assertEquals(false, resolve("'c' == 'b'")); // char
		Assert.assertEquals(true, resolve("5 == 5")); // int
		Assert.assertEquals(false, resolve("6 == 5")); // int
		Assert.assertEquals(true, resolve("5L == 5L")); // long
		Assert.assertEquals(false, resolve("6l == 5l")); // long
		Assert.assertEquals(true, resolve("5.5F == 5.5F")); // float
		Assert.assertEquals(false, resolve("6.5F == 5.5F")); // float
		Assert.assertEquals(true, resolve("5.5 == 5.5")); // double
		Assert.assertEquals(false, resolve("6.5 == 5.5")); // double
		Assert.assertEquals(true, resolve("\"hi\" == \"hi\"")); // string
		Assert.assertEquals(false, resolve("\"hi\" == \"bye\"")); // string
		Assert.assertEquals(true, resolve("(5 == 5) == (6 == 6)"));// boolean
		Assert.assertEquals(false, resolve("(5 == 5) == (6 == 5)"));// boolean
		// !=
		Assert.assertEquals(false, resolve("'b' != 'b'")); // char
		Assert.assertEquals(true, resolve("'c' != 'b'")); // char
		Assert.assertEquals(false, resolve("5 != 5")); // int
		Assert.assertEquals(true, resolve("6 != 5")); // int
		Assert.assertEquals(false, resolve("5l != 5l")); // long
		Assert.assertEquals(true, resolve("6L != 5L")); // long
		Assert.assertEquals(false, resolve("5.5F != 5.5F")); // float
		Assert.assertEquals(true, resolve("6.5F != 5.5F")); // float
		Assert.assertEquals(false, resolve("5.5 != 5.5")); // double
		Assert.assertEquals(true, resolve("6.5 != 5.5")); // double
		Assert.assertEquals(false, resolve("\"hi\" != \"hi\"")); // string
		Assert.assertEquals(true, resolve("\"hi\" != \"bye\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) != (6 == 6)"));// boolean
		Assert.assertEquals(true, resolve("(5 == 5) != (6 == 5)"));// boolean
		// >=
		Assert.assertEquals(true, resolve("'c' >= 'b'")); // char
		Assert.assertEquals(false, resolve("'b' >= 'c'")); // char
		Assert.assertEquals(true, resolve("6 >= 5")); // int
		Assert.assertEquals(false, resolve("5 >= 6")); // int
		Assert.assertEquals(true, resolve("6L >= 5L")); // long
		Assert.assertEquals(false, resolve("5L >= 6L")); // long
		Assert.assertEquals(true, resolve("6.5F >= 5.5F")); // float
		Assert.assertEquals(false, resolve("5.5F >= 6.5F")); // float
		Assert.assertEquals(true, resolve("6.5 >= 5.5")); // double
		Assert.assertEquals(false, resolve("5.5 >= 6.5")); // double
		Assert.assertEquals(true, resolve("\"hi\" >= \"bye\"")); // string
		Assert.assertEquals(false, resolve("\"bye\" >= \"hi\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) >= (6 == 5)"));// boolean
		Assert.assertEquals(false, resolve("(5 == 5) >= (6 == 6)"));// boolean
		// >
		Assert.assertEquals(true, resolve("'c' > 'b'")); // char
		Assert.assertEquals(false, resolve("'b' > 'c'")); // char
		Assert.assertEquals(true, resolve("6 > 5")); // int
		Assert.assertEquals(false, resolve("5 > 6")); // int
		Assert.assertEquals(true, resolve("6L > 5L")); // long
		Assert.assertEquals(false, resolve("5L > 6L")); // long
		Assert.assertEquals(true, resolve("6.5F > 5.5F")); // float
		Assert.assertEquals(false, resolve("5.5F > 6.5F")); // float
		Assert.assertEquals(true, resolve("6.5 > 5.5")); // double
		Assert.assertEquals(false, resolve("5.5 > 6.5")); // double
		Assert.assertEquals(true, resolve("\"hi\" > \"bye\"")); // string
		Assert.assertEquals(false, resolve("\"bye\" > \"hi\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) > (6 == 5)")); // boolean
		Assert.assertEquals(false, resolve("(5 == 5) > (6 == 6)")); // boolean
		// <=
		Assert.assertEquals(false, resolve("'c' <= 'b'")); // char
		Assert.assertEquals(true, resolve("'b' <= 'c'")); // char
		Assert.assertEquals(false, resolve("6 <= 5")); // int
		Assert.assertEquals(true, resolve("5 <= 6")); // int
		Assert.assertEquals(false, resolve("6L <= 5L")); // long
		Assert.assertEquals(true, resolve("5L <= 6L")); // long
		Assert.assertEquals(false, resolve("6.5F <= 5.5F")); // float
		Assert.assertEquals(true, resolve("5.5F <= 6.5F")); // float
		Assert.assertEquals(false, resolve("6.5 <= 5.5")); // double
		Assert.assertEquals(true, resolve("5.5 <= 6.5")); // double
		Assert.assertEquals(false, resolve("\"hi\" <= \"bye\"")); // string
		Assert.assertEquals(true, resolve("\"bye\" <= \"hi\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) <= (6 == 5)"));// boolean
		Assert.assertEquals(false, resolve("(5 == 5) <= (6 == 6)"));// boolean
		// <
		Assert.assertEquals(false, resolve("'c' < 'b'")); // char
		Assert.assertEquals(true, resolve("'b' < 'c'")); // char
		Assert.assertEquals(false, resolve("6 < 5")); // int
		Assert.assertEquals(true, resolve("5 < 6")); // int
		Assert.assertEquals(false, resolve("6L < 5L")); // long
		Assert.assertEquals(true, resolve("5L < 6L")); // long
		Assert.assertEquals(false, resolve("6.5F < 5.5F")); // float
		Assert.assertEquals(true, resolve("5.5F < 6.5F")); // float
		Assert.assertEquals(false, resolve("6.5 < 5.5")); // double
		Assert.assertEquals(true, resolve("5.5 < 6.5")); // double
		Assert.assertEquals(false, resolve("\"hi\" < \"bye\"")); // string
		Assert.assertEquals(true, resolve("\"bye\" < \"hi\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) < (6 == 5)")); // boolean
		Assert.assertEquals(false, resolve("(5 == 5) < (6 == 6)")); // boolean
		// &&
		Assert.assertEquals(false, resolve("'c' && 'b'")); // char
		Assert.assertEquals(false, resolve("6 && 5")); // int
		Assert.assertEquals(false, resolve("6L && 5L")); // long
		Assert.assertEquals(false, resolve("6.5F && 5.5F")); // float
		Assert.assertEquals(false, resolve("6.5 && 5.5")); // double
		Assert.assertEquals(false, resolve("\"hi\" && \"bye\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) && (6 == 5)"));// boolean
		Assert.assertEquals(true, resolve("(5 == 5) && (6 == 6)"));// boolean
		// ||
		Assert.assertEquals(false, resolve("'c' || 'b'")); // char
		Assert.assertEquals(false, resolve("6 || 5")); // int
		Assert.assertEquals(false, resolve("6L || 5L")); // long
		Assert.assertEquals(false, resolve("6.5F || 5.5F")); // float
		Assert.assertEquals(false, resolve("6.5 || 5.5")); // double
		Assert.assertEquals(false, resolve("\"hi\" || \"bye\"")); // string
		Assert.assertEquals(true, resolve("(5 == 5) || (6 == 5)"));// boolean
		Assert.assertEquals(true, resolve("(5 == 5) || (6 == 6)"));// boolean
		Assert.assertEquals(true, resolve("(5 == 6) || (6 == 6)"));// boolean

		// arithmetic operations

		// &
		Assert.assertEquals(98, resolve("'c'&'b'")); // char
		Assert.assertEquals((long) 4, resolve("6&5")); // int
		Assert.assertEquals((long) 4, resolve("6L&5L")); // long
		Assert.assertEquals((float) 0, resolve("6.5F&5.5F")); // float
		Assert.assertEquals((double) 0, resolve("6.5&5.5")); // double
		Assert.assertEquals(null, resolve("\"hi\"&\"bye\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5)&(6 == 5)")); // boolean
		// |
		Assert.assertEquals(99, resolve("'c' |'b'")); // char
		Assert.assertEquals((long) 7, resolve("6 |5")); // int
		Assert.assertEquals((long) 7, resolve("6L |5L")); // long
		Assert.assertEquals((float) 0, resolve("6.5F |5.5F")); // float
		Assert.assertEquals((double) 0, resolve("6.5 |5.5")); // double
		Assert.assertEquals(null, resolve("\"hi\" |\"bye\"")); // string
		Assert.assertEquals(true, resolve("(5 == 5) |(6 == 5)")); // boolean
		// ^
		Assert.assertEquals(1, resolve("'c'^ 'b'")); // char
		Assert.assertEquals((long) 3, resolve("6^ 5")); // int
		Assert.assertEquals((long) 3, resolve("6L^ 5L")); // long
		Assert.assertEquals((float) 0, resolve("6.5F^ 5.5F")); // float
		Assert.assertEquals((double) 0, resolve("6.5^ 5.5")); // double
		Assert.assertEquals(null, resolve("\"hi\"^ \"bye\"")); // string
		Assert.assertEquals(true, resolve("(5 == 5)| (6 == 5)")); // boolean
		// +
		Assert.assertEquals(197, resolve("'c' + 'b'")); // char
		Assert.assertEquals(1, resolve("'c' + -'b'")); // char
		Assert.assertEquals((long) 11, resolve("6 + 5")); // int
		Assert.assertEquals((long) 11, resolve("6- -5")); // int
		Assert.assertEquals((long) 11, resolve("6L + 5L")); // long
		Assert.assertEquals((long) 11, resolve("6L- -5L")); // long
		Assert.assertEquals((float) 12.0, resolve("6.5F + 5.5F")); // float
		Assert.assertEquals((float) 12.0, resolve("6.5F- -5.5F")); // float
		Assert.assertEquals(12.0, resolve("6.5 + 5.5")); // double
		Assert.assertEquals(12.0, resolve("6.5- -5.5")); // double
		Assert.assertEquals("\"hibye\"", resolve("\"hi\" + \"bye\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) + (6 == 5)"));// boolean
		// -
		Assert.assertEquals(1, resolve("'c'-'b'")); // char
		Assert.assertEquals(Long.valueOf(1), resolve(" 6-5")); // int
		Assert.assertEquals(Long.valueOf(1), resolve(" 6L-5L")); // long
		Assert.assertEquals(Float.valueOf((float)1.0), resolve(" 6.5F-5.5F")); // float
		Assert.assertEquals(Double.valueOf((double)1.0), resolve(" 6.5-5.5")); // double
		Assert.assertEquals(null, resolve(" \"hi\"-\"bye\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) - (6 == 5)")); // boolean
		// *
		Assert.assertEquals(9702, resolve("'c'*'b'")); // char
		Assert.assertEquals(Long.valueOf(30), resolve("6*5")); // int
		Assert.assertEquals(Long.valueOf(30), resolve("6L*5L")); // long
		Assert.assertEquals(Float.valueOf((float)35.75), resolve("6.5F*5.5F")); // float
		Assert.assertEquals(Double.valueOf((double)35.75), resolve("6.5*5.5")); // double
		Assert.assertEquals(null, resolve("\"hi\"*\"bye\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) * (6 == 5)")); // boolean
		// /
		Assert.assertEquals(1, resolve("'c' / 'b'")); // char
		Assert.assertEquals(Long.valueOf(1), resolve("6 / 5")); // int
		Assert.assertEquals(ASTEvalMessages.DivideByZero, resolve("6 / 0")); // int
		Assert.assertEquals(Long.valueOf(1), resolve("6L / 5L")); // long
		Assert.assertEquals(ASTEvalMessages.DivideByZero, resolve("6L / 0L")); // long
		Assert.assertEquals(Float.valueOf((float)1.1818181), resolve("6.5F / 5.5F")); // float
		Assert.assertEquals(Double.valueOf((double)1.1818181818181819), resolve("6.5 / 5.5")); // double
		Assert.assertEquals(null, resolve("\"hi\" / \"bye\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) / (6 == 5)")); // boolean
		// %
		Assert.assertEquals(1, resolve("'c' % 'b'")); // char
		Assert.assertEquals(Long.valueOf(1), resolve("6 % 5")); // int
		Assert.assertEquals(ASTEvalMessages.DivideByZero, resolve("6 % 0")); // int
		Assert.assertEquals(Long.valueOf(1), resolve("6L % 5L")); // long
		Assert.assertEquals(ASTEvalMessages.DivideByZero, resolve("6L % 0L")); // long
		Assert.assertEquals(Float.valueOf((float)0.0), resolve("6.5F % 5.5F")); // float
		Assert.assertEquals(ASTEvalMessages.ASTInstructionCompiler_InvalidNumber, resolve("6F")); // float
		Assert.assertEquals(Double.valueOf((double)0.0), resolve("6.5 % 5.5")); // double
		Assert.assertEquals(null, resolve("\"hi\" % \"bye\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) % (6 == 5)")); // boolean
		// <<
		Assert.assertEquals(Long.valueOf(396), resolve("'c' << 2")); // char
		Assert.assertEquals(Long.valueOf(192), resolve("6 << 5")); // int
		Assert.assertEquals(Long.valueOf(192), resolve("6L << 5L")); // long
		Assert.assertEquals(Float.valueOf((float)0), resolve("6.5F << 5.5F")); // float
		Assert.assertEquals(Double.valueOf((double)0), resolve("6.5 << 5.5")); // double
		Assert.assertEquals(null, resolve("\"hi\" << \"bye\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) << (6 == 5)"));// boolean
		// >>
		Assert.assertEquals(Long.valueOf(12), resolve("'c' >> 3")); // char
		Assert.assertEquals(Long.valueOf(1), resolve("6 >> 2")); // int
		Assert.assertEquals(Long.valueOf(1), resolve("6L >> 2L")); // long
		Assert.assertEquals(Float.valueOf((float)0), resolve("6.5f >> 5.5F")); // float
		Assert.assertEquals(Double.valueOf((double)0), resolve("6.5 >> 5.5")); // double
		Assert.assertEquals(null, resolve("\"hi\" >> \"bye\"")); // string
		Assert.assertEquals(false, resolve("(5 == 5) >> (6 == 5)"));// boolean
		// TODO: ->
		// TODO: .

		// unary operations

		// +
		Assert.assertEquals(Integer.valueOf(99), resolve("+'c'")); // char
		Assert.assertEquals(Long.valueOf(6), resolve("+6")); // int
		Assert.assertEquals(Long.valueOf(6), resolve("+6L")); // long
		Assert.assertEquals(Float.valueOf((float)6.5), resolve("+6.5F")); // float
		Assert.assertEquals(Double.valueOf((double)6.5), resolve("+6.5")); // double
		Assert.assertEquals(null, resolve("+\"hi\"")); // string
		Assert.assertEquals(false, resolve("+(5 == 5)")); // boolean
		// -
		Assert.assertEquals(-99, resolve("-'c'")); // char
		Assert.assertEquals(Long.valueOf(-6), resolve("-6")); // int
		Assert.assertEquals(Long.valueOf(-6), resolve("-6L")); // long
		Assert.assertEquals(Float.valueOf((float)-6.5), resolve("-6.5F")); // float
		Assert.assertEquals(Double.valueOf((double)-6.5), resolve("-6.5")); // double
		Assert.assertEquals(null, resolve("-\"hi\"")); // string
		Assert.assertEquals(false, resolve("-(5 == 5)")); // boolean
		// !
		Assert.assertEquals(false, resolve("!'c'")); // char
		Assert.assertEquals(false, resolve("!6")); // int
		Assert.assertEquals(false, resolve("!6L")); // long
		Assert.assertEquals(false, resolve("!6.5F")); // float
		Assert.assertEquals(false, resolve("!6.5")); // double
		Assert.assertEquals(false, resolve("!\"hi\"")); // string
		Assert.assertEquals(false, resolve("!(5 == 5)")); // boolean
		Assert.assertEquals(true, resolve("!(5 == 6)")); // boolean
		// ~
		Assert.assertEquals(-100, resolve("~'c'")); // ~'c'
		Assert.assertEquals(Long.valueOf(-7), resolve("~6")); // int
		Assert.assertEquals(Long.valueOf(-7), resolve("~6L")); // long
		Assert.assertEquals(Float.valueOf((float)0), resolve("~6.5F")); // float
		Assert.assertEquals(Double.valueOf((double)0), resolve("~6.5")); // double
		Assert.assertEquals(null, resolve("~\"hi\"")); // string
		Assert.assertEquals(false, resolve("~(5 == 5)")); // boolean
		// TODO: *
		// TODO: &

		// precedence
		Assert.assertEquals(Long.valueOf(783), resolve("6 + 'a' * 8 + 14 / 7 - 4 % 3")); // char
		// &
		// int
		Assert.assertEquals(Long.valueOf(377), resolve("(6 + 'a') * (8 + 14) / (7 - 4 % 3)")); // char
		// &
		// int
		Assert.assertEquals(Long.valueOf(55), resolve("6L + 6l * 8L + 14l / 7L - 4l % 3L")); // long
		Assert.assertEquals(Long.valueOf(28), resolve("6L + 6l * (8L + 14l) / (7L - 4l % 3L)")); // long
		Assert.assertEquals(Float.valueOf((float)55.366665), resolve("6.5F + 6.F * 8.5F + 14.0F / 7.5F - 4.F")); // float
		Assert.assertEquals(Float.valueOf((float)80.35714), resolve("(6.5F + 6.0F) * (8.5F + 14.F) / (7.5F - 4.F)")); // float
		Assert.assertEquals(Double.valueOf((double)55.36666666666667), resolve("6.5 + 6 * 8.5 + 14 / 7.5 - 4")); // double
		Assert.assertEquals(Double.valueOf((double)80.35714285714286), resolve("(6.5 + 6) * (8.5 + 14) / (7.5 - 4)")); // double
		Assert.assertEquals(false, resolve("(5 == 6) && ((6 == 5) || (5 == 5))")); // boolean
		Assert.assertEquals(true, resolve("((5 == 6) && (6 == 5)) || (5 == 5)")); // boolean
	}

	/*
	 * Note: This assumes you are at a breakpoint where the following are true:
	 * global char gchar = 49 global int gint = 4 global double gdouble = 2.2
	 * local long llong = 5 local float lfloat = 3.3
	 */
	private void resolveWithSimpleVariables() {
		// expressions without operators

		Assert.assertEquals(49, resolve("gchar")); // char

		// logical operations

		// ==
		Assert.assertEquals(false, resolve("66 == gchar")); // char
		Assert.assertEquals(true, resolve("gint == 4")); // int
		// !=
		Assert.assertEquals(false, resolve("llong != 5")); // long
		Assert.assertEquals(true, resolve("llong != 6")); // long
		// >=
		Assert.assertEquals(true, resolve("lfloat >= 3.2")); // float
		Assert.assertEquals(false, resolve("lfloat >= 3.4")); // float
		// >
		Assert.assertEquals(true, resolve("gdouble > 2.1")); // double
		Assert.assertEquals(false, resolve("gdouble > 100.0")); // double
		// <=
		Assert.assertEquals(true, resolve("llong <= 5")); // long
		Assert.assertEquals(false, resolve("llong <= 6")); // long
		// <
		Assert.assertEquals(true, resolve("lfloat < 3.4")); // float
		Assert.assertEquals(false, resolve("lfloat < 2.2")); // float
		// &&
		Assert.assertEquals(true, resolve("(gdouble > 2.1) && (5 = 5)")); // double
		Assert.assertEquals(false, resolve("(gdouble > 100.0) && (5 = 5)")); // double
		// ||
		Assert.assertEquals(false, resolve("(llong != 5) || (gdouble > 100.0)")); // long
		// &
		// double
		Assert.assertEquals(true, resolve("(llong != 6) || (gdouble > 100.0)")); // long
		// &
		// double

		// arithmetic operations

		// &
		Assert.assertEquals(Long.valueOf(5), resolve("llong & 7")); // long
		// |
		Assert.assertEquals(115, resolve("66 | gchar")); // char
		// ^
		Assert.assertEquals(Long.valueOf(1), resolve("gint ^ 5")); // int
		// +
		Assert.assertEquals(51.1, resolve("gchar + 2.1")); // char
		// -
		Assert.assertEquals(Double.valueOf((double)0), resolve("gdouble - gdouble")); // double
		// *
		Assert.assertEquals(Float.valueOf((float)13.2), resolve("gint * 3.3")); // int
		// /
		Assert.assertEquals(1, resolve("66 / gchar")); // char
		// %
		Assert.assertEquals(Long.valueOf(1), resolve("gint % 3")); // int
		// <<
		Assert.assertEquals(Long.valueOf(160), resolve("llong << 5")); // long
		// >>
		Assert.assertEquals(12, resolve("gchar >> 2")); // char
		// TODO: ->
		// TODO: .

		// unary operations

		// +
		Assert.assertEquals(Long.valueOf(4), resolve("+gint")); // int
		// -
		Assert.assertEquals(Long.valueOf(-5), resolve("-llong")); // long
		// !
		Assert.assertEquals(true, resolve("!(66 == lfloat)")); // float
		// ~
		Assert.assertEquals(Long.valueOf(-6), resolve("~llong")); // long
		// TODO: *
		// TODO: &
	}

	private Object resolve(String expression) {
		InstructionSequence comp = engine.getCompiledExpression(expression);
		Interpreter interpreter = engine.evaluateCompiledExpression(comp, this.frame);
		Object result = interpreter.getResult();
		
		if (result instanceof InvalidExpression){
			return ((InvalidExpression)result).getMessage();
		}
		
		if (result instanceof VariableWithValue) {
			result = ((VariableWithValue) result).getValue();
		}
		
		System.out.println(result);
		return result;
	}

}
