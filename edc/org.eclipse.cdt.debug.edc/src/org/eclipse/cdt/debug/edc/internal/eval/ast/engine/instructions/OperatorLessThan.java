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
 * Binary less than operation "<"
 */
public class OperatorLessThan extends BinaryLogicalOperator {

/**
	 * Constructor for a binary less than operation "<"
	 * 
	 * @param start - instruction start
	 */
	public OperatorLessThan(int start) {
		this(0, false, start);
	}

/**
	 * Constructor for a binary less than operation "<"
	 * 
	 * @param resultId - for assignment, variable ID of the result 
	 * @param isAssignmentOperator - whether the result is assigned
	 * @param start - instruction start
	 */
	protected OperatorLessThan(int resultId, boolean isAssignmentOperator, int start) {
		super(resultId, isAssignmentOperator, start);
	}

/**
	 * Get boolean result of comparing two ints with less than "<"
	 * 
	 * @param leftOperand - left int operand
	 * @param rightOperand - right int operand
	 * @return true if <code>leftOperand</code> < <code>rightOperand</code>, and false otherwise
	 * @throws CoreException
	 */
	@Override
	protected boolean getIntResult(int leftOperand, int rightOperand) throws CoreException {
		return leftOperand < rightOperand;
	}

/**
	 * Get boolean result of comparing two longs with less than "<"
	 * 
	 * @param leftOperand - left long operand
	 * @param rightOperand - right long operand
	 * @return true if <code>leftOperand</code> < <code>rightOperand</code>, and false otherwise
	 * @throws CoreException
	 */
	@Override
	protected boolean getLongResult(long leftOperand, long rightOperand) throws CoreException {
		return leftOperand < rightOperand;
	}

/**
	 * Get boolean result of comparing two BigIntegers with less than "<"
	 * 
	 * @param leftOperand - left BigInteger operand
	 * @param rightOperand - right BigInteger operand
	 * @return true if <code>leftOperand</code> < <code>rightOperand</code>, and false otherwise
	 * @throws CoreException
	 */
	@Override
	protected boolean getBigIntegerResult(BigInteger leftOperand, BigInteger rightOperand) throws CoreException {
		return leftOperand.compareTo(rightOperand) < 0;
	}

/**
	 * Get boolean result of comparing two floats with less than "<"
	 * 
	 * @param leftOperand - left float operand
	 * @param rightOperand - right float operand
	 * @return true if <code>leftOperand</code> < <code>rightOperand</code>, and false otherwise
	 */
	@Override
	protected boolean getFloatResult(float leftOperand, float rightOperand) {
		return leftOperand < rightOperand;
	}

/**
	 * Get boolean result of comparing two doubles with less than "<"
	 * 
	 * @param leftOperand - left double operand
	 * @param rightOperand - right double operand
	 * @return true if <code>leftOperand</code> < <code>rightOperand</code>, and false otherwise
	 */
	@Override
	protected boolean getDoubleResult(double leftOperand, double rightOperand) {
		return leftOperand < rightOperand;
	}

/**
	 * Get boolean result of comparing two longs with less than "<"
	 * 
	 * @param leftOperand - left long operand
	 * @param rightOperand - right long operand
	 * @return <code>false</code>
	 */
	@Override
	protected boolean getBooleanResult(boolean leftOperand, boolean rightOperand) {
		return false;
	}

/**
	 * Get boolean result of comparing two strings with less than "<"
	 * 
	 * @param leftOperand - left string operand
	 * @param rightOperand - right string operand
	 * @return true if <code>leftOperand</code> < <code>rightOperand</code>, and false otherwise
	 * @throws CoreException
	 */
	@Override
	protected boolean getStringResult(String leftOperand, String rightOperand) throws CoreException {
		return leftOperand.compareTo(rightOperand) < 0;
	}

}
