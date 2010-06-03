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
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;

import java.math.BigInteger;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.core.runtime.CoreException;

/*
 * Binary arithmetic operator, such as "/"
 */
public abstract class BinaryOperator extends CompoundInstruction {

	/**
	 * Constructor for a binary arithmetic operator, such as "/"
	 * 
	 * @param resultId
	 *            - for assignment, variable ID of the result
	 * @param isAssignmentOperator
	 *            - whether the result is assigned
	 * @param start
	 *            - instruction start
	 */
	protected BinaryOperator(int resultId, boolean isAssignmentOperator, int start) {
		super(start);
	}

	/**
	 * Resolve a binary arithmetic operator, such as "/"
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() throws CoreException {
		OperandValue right = popValue();
		OperandValue left = popValue();

		right = convertForPromotion(right);
		left = convertForPromotion(left);

		if (customHandleOperation(fInterpreter, left, right))
			return;

		int resultType = getJavaBinaryPromotionType(right, left);
		IType type;
		if (resultType == T_String)
			type = left.getValueType();
		else
			type = getBinaryPromotionType(right, left);
		
		// non-logical operations on booleans are int results
		if ((type instanceof ICPPBasicType) && ((ICPPBasicType) type).getBaseType() == ICPPBasicType.t_bool) {
			type = fInterpreter.getTypeEngine().getIntegerTypeFor(TypeUtils.BASIC_TYPE_INT, true);
		}

		switch (resultType) {
		case T_String:
			pushNewValue(type, getStringResult(GetValue.getStringValue(left), GetValue.getStringValue(right)));
			break;
		case T_double:
			pushNewValue(type, getDoubleResult(GetValue.getDoubleValue(left), GetValue.getDoubleValue(right)));
			break;
		case T_float:
			pushNewValue(type, getFloatResult(GetValue.getFloatValue(left), GetValue.getFloatValue(right)));
			break;
		case T_long:
			pushNewValue(type, getLongResult(GetValue.getLongValue(left), GetValue.getLongValue(right)));
			break;
		case T_int:
			pushNewValue(type, getIntResult(GetValue.getIntValue(left), GetValue.getIntValue(right)));
			break;
		case T_boolean:
			pushNewValue(type, getBooleanResult(GetValue.getBooleanValue(left), GetValue.getBooleanValue(right)));
			break;
		case T_BigInt:
			pushNewValue(type, getBigIntegerResult(GetValue.getBigIntegerValue(left), GetValue.getBigIntegerValue(right), 8));
			break;
		default:
			throw EDCDebugger.newCoreException(ASTEvalMessages.UnhandledTypeCode + resultType);
		}
	}

	/**
	 * Handle type operation in a non-standard way
	 * @param fInterpreter
	 * @param left
	 * @param right
	 * @return true if handled
	 */
	protected boolean customHandleOperation(Interpreter fInterpreter, OperandValue left, OperandValue right) throws CoreException {
		return false;
	}

	/**
	 * Get int result of applying a binary operation to two ints
	 * 
	 * @param leftOperand
	 *            - left int operand
	 * @param rightOperand
	 *            - right int operand
	 * @return int result of the operation if possible, or an operation-specific
	 *         default
	 * @throws CoreException
	 */
	protected abstract int getIntResult(int leftOperand, int rightOperand) throws CoreException;

	/**
	 * Get long result of applying a binary operation to two longs
	 * 
	 * @param leftOperand
	 *            - left long operand
	 * @param rightOperand
	 *            - right long operand
	 * @return long result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract long getLongResult(long leftOperand, long rightOperand) throws CoreException;

	/**
	 * Get BigInteger result of applying a binary operation to two longs
	 * 
	 * @param leftOperand
	 *            - left BigInteger operand
	 * @param rightOperand
	 *            - right BigInteger operand
	 * @param length
	 *            - length in bytes of result
	 * @return BigInteger result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract BigInteger getBigIntegerResult(BigInteger leftOperand, BigInteger rightOperand, int length)
			throws CoreException;

	/**
	 * Get float result of applying a binary operation to two floats
	 * 
	 * @param leftOperand
	 *            - left float operand
	 * @param rightOperand
	 *            - right float operand
	 * @return float result of the operation if possible, or an
	 *         operation-specific default
	 */
	protected abstract float getFloatResult(float leftOperand, float rightOperand);

	/**
	 * Get double result of applying a binary operation to two doubles
	 * 
	 * @param leftOperand
	 *            - left double operand
	 * @param rightOperand
	 *            - right double operand
	 * @return double result of the operation if possible, or an
	 *         operation-specific default
	 */
	protected abstract double getDoubleResult(double leftOperand, double rightOperand);

	/**
	 * Get boolean result of applying a binary operation to two booleans
	 * 
	 * @param leftOperand
	 *            - left boolean operand
	 * @param rightOperand
	 *            - right boolean operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 */
	protected abstract boolean getBooleanResult(boolean leftOperand, boolean rightOperand);

	/**
	 * Get string result of applying a binary operation to two strings.
	 * Default implementation throws.
	 * 
	 * @param leftOperand
	 *            - left string operand
	 * @param rightOperand
	 *            - right string operand
	 * @return string result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected String getStringResult(String leftOperand, String rightOperand) throws CoreException {
		throw EDCDebugger.newCoreException(ASTEvalMessages.UnsupportedStringOperation);
	}

}
