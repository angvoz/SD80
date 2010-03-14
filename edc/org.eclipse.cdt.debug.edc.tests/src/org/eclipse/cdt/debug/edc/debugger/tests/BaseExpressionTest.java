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

import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions.ExpressionDMC;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.TypeEngine;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.junit.Assert;
import org.junit.Before;

/**
 * 
 */
public abstract class BaseExpressionTest extends SimpleDebuggerTest {

	private TypeEngine typeEngine;
	protected IType boolType;
	protected IType charType;
	protected IType wcharType;
	protected IType shortType;
	protected IType intType;
	protected IType longType;
	protected IType floatType;
	protected IType doubleType;
	protected IType longDoubleType;

	/**
	 * 
	 */
	public BaseExpressionTest() {
		super();
	}

	@Before
	public void gatherTypes() {
		typeEngine = ((StackFrameDMC)frame).getTypeEngine();
		
		charType = typeEngine.getCharacterType(typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_CHAR));
		wcharType = typeEngine.getCharacterType(typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_WCHAR_T));
		
		boolType = typeEngine.getBasicType(ICPPBasicType.t_bool, 0, typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_BOOL));
		
		shortType = typeEngine.getBasicType(ICPPBasicType.t_int, ICPPBasicType.IS_SIGNED + ICPPBasicType.IS_SHORT, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_SHORT));
		intType = typeEngine.getBasicType(ICPPBasicType.t_int, ICPPBasicType.IS_SIGNED, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_INT));
		longType = typeEngine.getBasicType(ICPPBasicType.t_int, ICPPBasicType.IS_SIGNED + ICPPBasicType.IS_LONG, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_LONG));
		floatType = typeEngine.getBasicType(ICPPBasicType.t_float, 0, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_FLOAT));
		doubleType = typeEngine.getBasicType(ICPPBasicType.t_double, 0, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_DOUBLE));
		longDoubleType = typeEngine.getBasicType(ICPPBasicType.t_double, ICPPBasicType.IS_LONG, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_LONG_DOUBLE));
	}

	/**
	 * Check that an expression evaluates without an error.
	 * @param result
	 * @param expr
	 * @param type the type to check (either IType or String), or <code>null</code>
	 * @throws Exception
	 */

	protected void checkExprNoError(String expr)
			throws Exception {
		ExpressionDMC exprVal = TestUtils.getExpressionDMC(session, frame, expr);
		if (exprVal.getEvaluationError() != null)
			Assert.fail(expr + " got error " + exprVal.getEvaluationError().getMessage());
	}

	
	/**
	 * Check that an expression evaluates to the given string and type, with no errors.
	 * If the type is a string, look for a substring match (e.g., to account for
	 * differences in compilers wrt. class/struct and template argument formatting).
	 * If the type is IType, compare the types with #equals first, and if not matching, then
	 * because we know we have gaps in #equals/#hashCode support, check the #getTypeName() of the type.
	 * @param result
	 * @param expr
	 * @param type the type to check (either IType or String), or <code>null</code>
	 * @throws Exception
	 */
	protected void checkExpr(Object type, String result, String expr)
			throws Exception {
		ExpressionDMC exprVal = TestUtils.getExpressionDMC(session, frame, expr);
		if (exprVal.getEvaluationError() != null)
			Assert.fail(expr + " got error " + exprVal.getEvaluationError().getMessage());

		String formatted = TestUtils.getFormattedExpressionValue(session, frame, exprVal);
		if (!result.equals(formatted))
			Assert.assertEquals(result, formatted);	// the test is duplicated this way to allow breakpoint
		if (type != null) {
			if (type instanceof IType) {
				if (type.equals(exprVal.getEvaluatedType()))
					return;
				String typeName = typeEngine.getTypeName((IType) type);
				String exprTypeName = typeEngine.getTypeName(exprVal.getEvaluatedType());
				if (!typeName.equals(exprTypeName))
					Assert.assertEquals(exprTypeName, typeName, exprTypeName);	// the test is duplicated this way to allow breakpoint
			} else if (type instanceof String) {
				String typeName = (String) type;
				String exprTypeName = typeEngine.getTypeName(exprVal.getEvaluatedType());
				if (!exprTypeName.contains(typeName))
					Assert.assertTrue(exprTypeName, exprTypeName.contains(typeName));	// the test is duplicated this way to allow breakpoint
				
			} else {
				throw new IllegalStateException("Unexpected type to check: " + type);
			}
		}
		
	}

	/**
	 * Check that an expression evaluates to the given string, with no errors.
	 * @param result
	 * @param expr
	 * @throws Exception
	 */
	protected void checkExpr(String result, String expr) throws Exception {
		checkExpr(null, result, expr);
	}

	/**
	 * Check that an expression evaluates with an error.
	 * @param expr
	 * @throws Exception
	 */

	protected void checkExprError(String expr) throws Exception {
		ExpressionDMC exprVal = TestUtils.getExpressionDMC(session, frame, expr);
		if (exprVal.getEvaluationError() != null)
			return;
		String formatted = TestUtils.getFormattedExpressionValue(session, frame, exprVal);
		Assert.fail("should not have parsed: " + expr + " but got: " + formatted);
	}

	/**
	 * Check that an expression evaluates with the given error message.
	 * @param message
	 * @param expr
	 * @throws Exception
	 */
	protected void checkExprError(String message, String expr) throws Exception {
		ExpressionDMC exprVal = TestUtils.getExpressionDMC(session, frame, expr);
		if (exprVal.getEvaluationError() != null) {
			Assert.assertEquals(message, exprVal.getEvaluationError().getMessage());
			return;
		}
		String formatted = TestUtils.getFormattedExpressionValue(session, frame, exprVal);
		Assert.fail("should not have parsed: " + expr + " but got: " + formatted);
	}

}