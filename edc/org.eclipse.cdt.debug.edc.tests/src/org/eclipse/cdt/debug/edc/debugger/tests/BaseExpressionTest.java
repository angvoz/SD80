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

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.TypeEngine;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.CastInfo;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.ICastedExpressionDMContext;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;
import org.junit.Before;

/**
 * 
 */
public abstract class BaseExpressionTest extends SimpleDebuggerTest {

	private TypeEngine typeEngine;
	protected IType boolType;
	protected IType charType;
	protected IType signedCharType;
	protected IType wcharType;
	protected IType shortType;
	protected IType unsignedShortType;
	protected IType intType;
	protected IType unsignedIntType;
	protected IType longType;
	protected IType unsignedLongType;
	protected IType longLongType;
	protected IType unsignedLongLongType;
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
		if (frame == null)
			return;
		typeEngine = ((StackFrameDMC)frame).getTypeEngine();
		
		charType = typeEngine.getCharacterType(typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_CHAR));
		wcharType = typeEngine.getCharacterType(typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_WCHAR_T));

		signedCharType = typeEngine.getBasicType(ICPPBasicType.t_char, ICPPBasicType.IS_SIGNED + ICPPBasicType.IS_SHORT, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_CHAR_SIGNED));

		boolType = typeEngine.getBasicType(ICPPBasicType.t_bool, 0, typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_BOOL));
		
		shortType = typeEngine.getBasicType(ICPPBasicType.t_int, ICPPBasicType.IS_SIGNED + ICPPBasicType.IS_SHORT, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_SHORT));
		unsignedShortType = typeEngine.getBasicType(ICPPBasicType.t_int, ICPPBasicType.IS_UNSIGNED + ICPPBasicType.IS_SHORT, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_SHORT_UNSIGNED));
		intType = typeEngine.getBasicType(ICPPBasicType.t_int, ICPPBasicType.IS_SIGNED, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_INT));
		unsignedIntType = typeEngine.getBasicType(ICPPBasicType.t_int, ICPPBasicType.IS_UNSIGNED, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_INT_UNSIGNED));
		longType = typeEngine.getBasicType(ICPPBasicType.t_int, ICPPBasicType.IS_SIGNED + ICPPBasicType.IS_LONG, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_LONG));
		unsignedLongType = typeEngine.getBasicType(ICPPBasicType.t_int, ICPPBasicType.IS_UNSIGNED + ICPPBasicType.IS_LONG, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_LONG_UNSIGNED));
		longLongType = typeEngine.getBasicType(ICPPBasicType.t_int, ICPPBasicType.IS_SIGNED + ICPPBasicType.IS_LONG_LONG, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_LONG_LONG));
		unsignedLongLongType = typeEngine.getBasicType(ICPPBasicType.t_int, ICPPBasicType.IS_UNSIGNED + ICPPBasicType.IS_LONG_LONG, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_LONG_LONG_UNSIGNED));
		floatType = typeEngine.getBasicType(ICPPBasicType.t_float, 0, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_FLOAT));
		doubleType = typeEngine.getBasicType(ICPPBasicType.t_double, 0, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_DOUBLE));
		longDoubleType = typeEngine.getBasicType(ICPPBasicType.t_double, ICPPBasicType.IS_LONG, 
				typeEngine.getTypeSize(TypeUtils.BASIC_TYPE_LONG_DOUBLE));
	}

	/**
	 * Check that an expression evaluates without an error.
	 * @param k9View
	 * @param expr
	 * @param type the type to check (either IType or String), or <code>null</code>
	 * @throws Exception
	 */

	protected void checkExprNoError(String expr)
			throws Exception {
		IEDCExpression exprVal = TestUtils.getExpressionDMC(session, frame, expr);
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
		IEDCExpression exprVal = TestUtils.getExpressionDMC(session, frame, expr);
		if (exprVal.getEvaluationError() != null)
			Assert.fail(expr + " got error " + exprVal.getEvaluationError().getMessage());

		doCheckExprValue(type, result, exprVal);
		
	}

	/**
	 * Get a casted expression.
	 * @param expr the expression to cast
	 * @param castInfo the casting details
	 * @return evaluated expression or <code>null</code>
	 * @throws CoreException for any error
	 */
	protected IEDCExpression getCastedExpr(String expr, CastInfo castInfo)
			throws Exception {
		IExpressionDMContext exprDMC = TestUtils.getExpressionDMC(session, frame, expr);
		if (((IEDCExpression) exprDMC).getEvaluationError() != null)
			throw new CoreException(((IEDCExpression) exprDMC).getEvaluationError());
		
		ICastedExpressionDMContext castedExprVal = TestUtils.getCastedExpressionDMC(session, frame, exprDMC, castInfo);
		
		return (IEDCExpression) castedExprVal;
	}

	/**
	 * Check that a casted expression provides the expected value.
	 * @param type the type to check (either IType or String), or <code>null</code>
	 * @param result
	 * @param expr the expression to cast
	 * @param castInfo the casting details
	 * @throws CoreException for any error
	 * @throws Exception
	 */

	protected void checkCastedExpr(Object type, String result, String expr, CastInfo castInfo) throws Exception {
		IEDCExpression exprVal = getCastedExpr(expr, castInfo);
		
		if (exprVal.getEvaluationError() != null)
			throw new CoreException(exprVal.getEvaluationError());
	
		doCheckExprValue(type, result, exprVal);
		
	}
	/**
	 * Get a child expression from a casted expression.
	 * @param expr the expression to cast
	 * @param castInfo the casting details
	 * @param childExpr the name of the child to fetch
	 * @return evaluated child expression or <code>null</code>
	 * @throws CoreException for any error
	 */
	protected IEDCExpression getCastedChildExpr(String expr, CastInfo castInfo, String childExpr)
			throws Exception {
		IExpressionDMContext exprDMC = TestUtils.getExpressionDMC(session, frame, expr);
		if (((IEDCExpression) exprDMC).getEvaluationError() != null)
			throw new CoreException(((IEDCExpression) exprDMC).getEvaluationError());
		
		ICastedExpressionDMContext castedExprVal = TestUtils.getCastedExpressionDMC(session, frame, exprDMC, castInfo);
		
		IExpressionDMContext[] childDMCs = TestUtils.getSubExpressionDMCs(session, frame, castedExprVal);
		
		for (IExpressionDMContext childDMC : childDMCs) {
			
			if (!(childDMC instanceof IEDCExpression))
				continue;
			if (((IEDCExpression)childDMC).getName().equals(childExpr)) {
				IEDCExpression exprVal = (IEDCExpression) childDMC;
				/* ignore, just evaluate */ TestUtils.getFormattedExpressionValue(session, frame, exprVal);
				
				return exprVal;
			}
		}
		return null;
	}

	/**
	 * Check that a casted expression provides the expected kind of children.
	 * @param type the type to check (either IType or String), or <code>null</code>
	 * @param result
	 * @param expr the expression to cast
	 * @param castInfo the casting details
	 * @param childExpr the name of the child to fetch 
	 * @throws CoreException for any error
	 * @throws Exception
	 */
	protected void checkCastedChildExpr(Object type, String result, String expr, CastInfo castInfo, String childExpr)
			throws CoreException, Exception {
		IEDCExpression exprVal = getCastedChildExpr(expr, castInfo, childExpr);
		if (exprVal == null)
			Assert.fail("Did not find child " + childExpr);
	
		if (exprVal.getEvaluationError() != null)
			throw new CoreException(exprVal.getEvaluationError());
	
		doCheckExprValue(type, result, exprVal);
	}

	protected void checkCastedChildExprFail(String expr, CastInfo castInfo, String childExpr) {
		IEDCExpression exprVal;
		try {
			exprVal = getCastedChildExpr(expr, castInfo, childExpr);
		} catch (Exception e) {
			// fine
			return;
		}
		if (exprVal == null)
			return;
	
		if (exprVal.getEvaluationError() != null)
			return;
	
		Assert.fail("expected failure to cast and find " + childExpr);
	}
	/**
	 * @param type
	 * @param result
	 * @param exprVal
	 * @throws Exception
	 * @throws ExecutionException
	 */
	private void doCheckExprValue(Object type, String result,
			IEDCExpression exprVal) throws Exception, ExecutionException {
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
		IEDCExpression exprVal = TestUtils.getExpressionDMC(session, frame, expr);
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
		IEDCExpression exprVal = TestUtils.getExpressionDMC(session, frame, expr);
		if (exprVal.getEvaluationError() != null) {
			Assert.assertEquals(message, exprVal.getEvaluationError().getMessage());
			return;
		}
		String formatted = TestUtils.getFormattedExpressionValue(session, frame, exprVal);
		Assert.fail("should not have parsed: " + expr + " but got: " + formatted);
	}

}