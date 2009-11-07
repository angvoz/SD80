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
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;

import java.math.BigInteger;

import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvaluationEngine;
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
		Object right = popValue();
		Object left = popValue();

		if (right == null || left == null)
			return;

		if (right instanceof InvalidExpression) {
			push(right);
			return;
		}

		if (left instanceof InvalidExpression) {
			push(left);
			return;
		}

		right = convertForPromotion(right);
		left = convertForPromotion(left);

		// let others convert the type to the string "bool", "int", etc.
		this.setValueType(ASTEvaluationEngine.UNKNOWN_TYPE);

		int resultType = getBinaryPromotionType(right, left);

		switch (resultType) {
		case T_String:
			pushNewValue(getStringResult(GetValue.getStringValue(left), GetValue.getStringValue(right)));
			break;
		case T_double:
			pushNewValue(getDoubleResult(GetValue.getDoubleValue(left), GetValue.getDoubleValue(right)));
			break;
		case T_float:
			pushNewValue(getFloatResult(GetValue.getFloatValue(left), GetValue.getFloatValue(right)));
			break;
		case T_long:
			pushNewValue(getLongResult(GetValue.getLongValue(left), GetValue.getLongValue(right)));
			break;
		case T_int:
			pushNewValue(getIntResult(GetValue.getIntValue(left), GetValue.getIntValue(right)));
			break;
		case T_boolean:
			pushNewValue(getBooleanResult(GetValue.getBooleanValue(left), GetValue.getBooleanValue(right)));
			break;
		case T_BigInt:
			// TODO: get the length of a long long rather than using hard-coded
			// 8
			pushNewValue(getBigIntegerResult(GetValue.getBigIntegerValue(left), GetValue.getBigIntegerValue(right), 8));
			break;
		}
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
	 * Get string result of applying a binary operation to two strings
	 * 
	 * @param leftOperand
	 *            - left string operand
	 * @param rightOperand
	 *            - right string operand
	 * @return string result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract String getStringResult(String leftOperand, String rightOperand) throws CoreException;

}
