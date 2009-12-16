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

import org.eclipse.core.runtime.CoreException;

/*
 * Unary arithmetic operator, such as "~"
 */
public abstract class UnaryOperator extends CompoundInstruction {

	/**
	 * Constructor for a unary arithmetic operator, such as "~"
	 * 
	 * @param start
	 *            - instruction start
	 */
	public UnaryOperator(int start) {
		super(start);
	}

	/**
	 * Constructor for a unary arithmetic operator, such as "~"
	 * 
	 * @param resultId
	 *            - for assignment, variable ID of the result
	 * @param isAssignmentOperator
	 *            - whether the result is assigned
	 * @param start
	 *            - instruction start
	 */
	public UnaryOperator(int resultId, boolean isAssignmentOperator, int start) {
		super(start);
	}

	/**
	 * Resolve a unary arithmetic expression
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() throws CoreException {
		Object operand = popValue();

		if (operand == null)
			return;

		if (operand instanceof InvalidExpression) {
			push(operand);
			return;
		}

		operand = convertForPromotion(operand);

		// change chars/shorts to int, etc.
		int resultType = getBinaryPromotionType(operand, operand);

		switch (resultType) {
		case T_String:
			pushNewValue(getStringResult(GetValue.getStringValue(operand)));
			break;
		case T_double:
			pushNewValue(getDoubleResult(GetValue.getDoubleValue(operand)));
			break;
		case T_float:
			pushNewValue(getFloatResult(GetValue.getFloatValue(operand)));
			break;
		case T_long:
			pushNewValue(getLongResult(GetValue.getLongValue(operand)));
			break;
		case T_int:
			pushNewValue(getIntResult(GetValue.getIntValue(operand)));
			break;
		case T_boolean:
			pushNewValue(getBooleanResult(GetValue.getBooleanValue(operand)));
			break;
		}
	}

	/**
	 * Get int result of applying a unary operation to an int
	 * 
	 * @param operand
	 *            - int operand
	 * @return int result of the operation if possible, or an operation-specific
	 *         default
	 * @throws CoreException
	 */
	protected abstract int getIntResult(int operand) throws CoreException;

	/**
	 * Get long result of applying a unary operation to a long
	 * 
	 * @param operand
	 *            - long operand
	 * @return long result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract long getLongResult(long operand) throws CoreException;

	/**
	 * Get BigInteger result of applying a unary operation to a BigInteger
	 * 
	 * @param operand
	 *            - long operand
	 * @param length
	 *            - length in bytes of the result
	 * @return long result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract BigInteger getBigIntegerResult(BigInteger operand, int length) throws CoreException;

	/**
	 * Get float result of applying a unary operation to a float
	 * 
	 * @param operand
	 *            - float operand
	 * @return float result of the operation if possible, or an
	 *         operation-specific default
	 */
	protected abstract float getFloatResult(float operand);

	/**
	 * Get double result of applying a unary operation to a double
	 * 
	 * @param operand
	 *            - double operand
	 * @return double result of the operation if possible, or an
	 *         operation-specific default
	 */
	protected abstract double getDoubleResult(double operand);

	/**
	 * Get boolean result of applying a unary operation to a boolean
	 * 
	 * @param operand
	 *            - boolean operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 */
	protected abstract boolean getBooleanResult(boolean operand);

	/**
	 * Get string result of applying a unary operation to a string
	 * 
	 * @param operand
	 *            - string operand
	 * @return string result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract String getStringResult(String operand) throws CoreException;

}
