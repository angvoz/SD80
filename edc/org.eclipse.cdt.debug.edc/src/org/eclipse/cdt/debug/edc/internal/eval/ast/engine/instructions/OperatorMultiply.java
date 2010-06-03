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

import org.eclipse.core.runtime.CoreException;

/*
 * Binary multiply operation "*"
 */
public class OperatorMultiply extends BinaryOperator {

	/**
	 * Constructor for a binary multiply operation "*"
	 * 
	 * @param start
	 *            - instruction start
	 */
	public OperatorMultiply(int start) {
		this(0, false, start);
	}

	/**
	 * Constructor for a binary multiply operation "*"
	 * 
	 * @param resultId
	 *            - for assignment, variable ID of the result
	 * @param isAssignmentOperator
	 *            - whether the result is assigned
	 * @param start
	 *            - instruction start
	 */
	protected OperatorMultiply(int resultId, boolean isAssignmentOperator, int start) {
		super(resultId, isAssignmentOperator, start);
	}

	/**
	 * Get int result of applying binary multiply "*" to two ints
	 * 
	 * @param leftOperand
	 *            - left int operand
	 * @param rightOperand
	 *            - right int operand
	 * @return <code>leftOperand</code> * <code>rightOperand</code>
	 * @throws CoreException
	 */
	@Override
	protected int getIntResult(int leftOperand, int rightOperand) throws CoreException {
		return leftOperand * rightOperand;
	}

	/**
	 * Get long result of applying binary multiply "*" to two longs
	 * 
	 * @param leftOperand
	 *            - left long operand
	 * @param rightOperand
	 *            - right long operand
	 * @return <code>leftOperand</code> * <code>rightOperand</code>
	 * @throws CoreException
	 */
	@Override
	protected long getLongResult(long leftOperand, long rightOperand) throws CoreException {
		return leftOperand * rightOperand;
	}

	/**
	 * Get BigInteger result of applying binary multiply "*" to two BigIntegers
	 * 
	 * @param leftOperand
	 *            - left BigInteger operand
	 * @param rightOperand
	 *            - right BigInteger operand
	 * @param length
	 *            - length in bytes of result
	 * @return <code>leftOperand</code> * <code>rightOperand</code>, truncated
	 *         to 8 bytes
	 * @throws CoreException
	 */
	@Override
	protected BigInteger getBigIntegerResult(BigInteger leftOperand, BigInteger rightOperand, int length)
			throws CoreException {
		return leftOperand.multiply(rightOperand).and(Instruction.Mask8Bytes);
	}

	/**
	 * Get float result of applying binary multiply "*" to two floats
	 * 
	 * @param leftOperand
	 *            - left float operand
	 * @param rightOperand
	 *            - right float operand
	 * @return <code>leftOperand</code> * <code>rightOperand</code>
	 */
	@Override
	protected float getFloatResult(float leftOperand, float rightOperand) {
		return leftOperand * rightOperand;
	}

	/**
	 * Get double result of applying binary multiply "*" to two doubles
	 * 
	 * @param leftOperand
	 *            - left double operand
	 * @param rightOperand
	 *            - right double operand
	 * @return <code>leftOperand</code> * <code>rightOperand</code>
	 */
	@Override
	protected double getDoubleResult(double leftOperand, double rightOperand) {
		return leftOperand * rightOperand;
	}

	/**
	 * Get boolean result of applying binary multiply "*" to two booleans
	 * 
	 * @param leftOperand
	 *            - left boolean operand
	 * @param rightOperand
	 *            - right boolean operand
	 * @return <code>false</code>
	 */
	@Override
	protected boolean getBooleanResult(boolean leftOperand, boolean rightOperand) {
		return false;
	}
}
