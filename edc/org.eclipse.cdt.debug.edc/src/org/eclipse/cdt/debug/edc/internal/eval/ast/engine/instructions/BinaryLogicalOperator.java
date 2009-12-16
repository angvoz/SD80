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
 * Binary logical operator, such as "<"
 */
public abstract class BinaryLogicalOperator extends CompoundInstruction {

	/**
	 * Constructor for a binary logical operator, such as "<"
	 * 
	 * @param resultId - for assignment, variable ID of the result 
	 * @param isAssignmentOperator - whether the result is assigned
	 * @param start - instruction start
	 */
	protected BinaryLogicalOperator(int resultId, boolean isAssignmentOperator, int start) {
		super(start);
	}

	/**
	 * Resolve a binary logical operator, such as "<"
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

		int promotedType = getBinaryPromotionType(right, left);

		// let others convert the type to the string "bool", "_Bool", etc.
		this.setValueType(ASTEvaluationEngine.UNKNOWN_TYPE);

		switch (promotedType) {
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
			pushNewValue(getBigIntegerResult(GetValue.getBigIntegerValue(left), GetValue.getBigIntegerValue(right)));
			break;
		}
	}

	/**
	 * Get boolean result of applying a binary logical operation to two ints
	 * 
	 * @param leftOperand
	 *            - left int operand
	 * @param rightOperand
	 *            - right int operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract boolean getIntResult(int leftOperand, int rightOperand) throws CoreException;

	/**
	 * Get boolean result of applying a binary logical operation to two longs
	 * 
	 * @param leftOperand
	 *            - left long operand
	 * @param rightOperand
	 *            - right long operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract boolean getLongResult(long leftOperand, long rightOperand) throws CoreException;

	/**
	 * Get boolean result of applying a binary logical operation to two
	 * BigIntegers
	 * 
	 * @param leftOperand
	 *            - left BigInteger operand
	 * @param rightOperand
	 *            - right BigInteger operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract boolean getBigIntegerResult(BigInteger leftOperand, BigInteger rightOperand)
			throws CoreException;

	/**
	 * Get boolean result of applying a binary logical operation to two floats
	 * 
	 * @param leftOperand
	 *            - left float operand
	 * @param rightOperand
	 *            - right float operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 */
	protected abstract boolean getFloatResult(float leftOperand, float rightOperand);

	/**
	 * Get boolean result of applying a binary logical operation to two doubles
	 * 
	 * @param leftOperand
	 *            - left double operand
	 * @param rightOperand
	 *            - right double operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 */
	protected abstract boolean getDoubleResult(double leftOperand, double rightOperand);

	/**
	 * Get boolean result of applying a binary logical operation to two booleans
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
	 * Get boolean result of applying a binary logical operation to two strings
	 * 
	 * @param leftOperand
	 *            - left string operand
	 * @param rightOperand
	 *            - right string operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract boolean getStringResult(String leftOperand, String rightOperand) throws CoreException;

}
