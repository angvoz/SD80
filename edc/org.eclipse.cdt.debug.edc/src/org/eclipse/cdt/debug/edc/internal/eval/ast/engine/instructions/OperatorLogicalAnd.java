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
 * Binary logical AND operation "&&"
 */
public class OperatorLogicalAnd extends BinaryLogicalOperator {

	/**
	 * Constructor for a binary logical AND operation "&&"
	 * 
	 * @param start
	 *            - instruction start
	 */
	public OperatorLogicalAnd(int start) {
		this(0, false, start);
	}

	/**
	 * Constructor for a binary logical AND operation "&&"
	 * 
	 * @param resultId
	 *            - for assignment, variable ID of the result
	 * @param isAssignmentOperator
	 *            - whether the result is assigned
	 * @param start
	 *            - instruction start
	 */
	protected OperatorLogicalAnd(int resultId, boolean isAssignmentOperator, int start) {
		super(resultId, isAssignmentOperator, start);
	}

	/**
	 * Get boolean result of comparing two ints with logical AND "&&"
	 * 
	 * @param leftOperand
	 *            - left int operand
	 * @param rightOperand
	 *            - right int operand
	 * @return <code>false</code>
	 * @throws CoreException
	 */
	@Override
	protected boolean getIntResult(int leftOperand, int rightOperand) throws CoreException {
		// as bools
		return (leftOperand != 0) && (rightOperand != 0);
	}

	/**
	 * Get boolean result of comparing two longs with logical AND "&&"
	 * 
	 * @param leftOperand
	 *            - left long operand
	 * @param rightOperand
	 *            - right long operand
	 * @return <code>false</code>
	 * @throws CoreException
	 */
	@Override
	protected boolean getLongResult(long leftOperand, long rightOperand) throws CoreException {
		// as bools
		return (leftOperand != 0) && (rightOperand != 0);
	}

	/**
	 * Get boolean result of comparing two BigIntegers with logical AND "&&"
	 * 
	 * @param leftOperand
	 *            - left BigInteger operand
	 * @param rightOperand
	 *            - right BigInteger operand
	 * @return <code>false</code>
	 * @throws CoreException
	 */
	@Override
	protected boolean getBigIntegerResult(BigInteger leftOperand, BigInteger rightOperand) throws CoreException {
		// as bools
		return (leftOperand.signum() != 0) && (rightOperand.signum() != 0);
	}

	/**
	 * Get boolean result of comparing two floats with logical AND "&&"
	 * 
	 * @param leftOperand
	 *            - left float operand
	 * @param rightOperand
	 *            - right float operand
	 * @return <code>false</code>
	 */
	@Override
	protected boolean getFloatResult(float leftOperand, float rightOperand) {
		// as bools
		return (leftOperand != 0) && (rightOperand != 0);
	}

	/**
	 * Get boolean result of comparing two doubles with logical AND "&&"
	 * 
	 * @param leftOperand
	 *            - left double operand
	 * @param rightOperand
	 *            - right double operand
	 * @return <code>false</code>
	 */
	@Override
	protected boolean getDoubleResult(double leftOperand, double rightOperand) {
		// as bools
		return (leftOperand != 0) && (rightOperand != 0);
	}

	/**
	 * Get boolean result of comparing two booleans with logical AND "&&"
	 * 
	 * @param leftOperand
	 *            - left boolean operand
	 * @param rightOperand
	 *            - right boolean operand
	 * @return true if <code>leftOperand</code> && <code>rightOperand</code> is
	 *         true, and false otherwise
	 */
	@Override
	protected boolean getBooleanResult(boolean leftOperand, boolean rightOperand) {
		return leftOperand && rightOperand;
	}

	/**
	 * Get boolean result of comparing two strings with logical AND "&&"
	 * 
	 * @param leftOperand
	 *            - left string operand
	 * @param rightOperand
	 *            - right string operand
	 * @return <code>false</code>
	 * @throws CoreException
	 */
	@Override
	protected boolean getStringResult(String leftOperand, String rightOperand) throws CoreException {
		return true;
	}

}
