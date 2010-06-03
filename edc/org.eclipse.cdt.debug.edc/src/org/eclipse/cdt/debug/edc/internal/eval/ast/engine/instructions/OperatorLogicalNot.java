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
 * Unary logical NOT operation "!"
 */
public class OperatorLogicalNot extends UnaryLogicalOperator {

	/**
	 * Constructor for a unary logical NOT operation "!"
	 * 
	 * @param start
	 *            - instruction start
	 */
	public OperatorLogicalNot(int start) {
		super(start);
	}

	/**
	 * Get boolean result of applying logical NOT "!" to an int
	 * 
	 * @param operand
	 *            - int operand
	 * @return <code>false</code>
	 * @throws CoreException
	 */
	@Override
	protected boolean getIntResult(int leftOperand) throws CoreException {
		// as bool
		return leftOperand == 0;
	}

	/**
	 * Get boolean result of applying logical NOT "!" to a long
	 * 
	 * @param operand
	 *            - long operand
	 * @return <code>false</code>
	 * @throws CoreException
	 */
	@Override
	protected boolean getLongResult(long leftOperand) throws CoreException {
		// as bool
		return leftOperand == 0;
	}

	/**
	 * Get boolean result of applying logical NOT "!" to a BigInteger
	 * 
	 * @param operand
	 *            - BigInteger operand
	 * @return <code>false</code>
	 * @throws CoreException
	 */
	@Override
	protected boolean getBigIntegerResult(BigInteger leftOperand) throws CoreException {
		// as bool
		return leftOperand.signum() == 0;
	}

	/**
	 * Get boolean result of applying logical NOT "!" to a float
	 * 
	 * @param operand
	 *            - float operand
	 * @return <code>false</code>
	 */
	@Override
	protected boolean getFloatResult(float leftOperand) {
		// as bool
		return leftOperand == 0;
	}

	/**
	 * Get boolean result of applying logical NOT "!" to a double
	 * 
	 * @param operand
	 *            - double operand
	 * @return <code>false</code>
	 */
	@Override
	protected boolean getDoubleResult(double leftOperand) {
		// as bool
		return leftOperand == 0;
	}

	/**
	 * Get boolean result of applying logical NOT "!" to a boolean
	 * 
	 * @param operand
	 *            - boolean operand
	 * @return !<code>operand</code>
	 */
	@Override
	protected boolean getBooleanResult(boolean leftOperand) {
		return !leftOperand;
	}

	/**
	 * Get boolean result of applying logical NOT "!" to a string
	 * 
	 * @param operand
	 *            - string operand
	 * @return <code>false</code>
	 * @throws CoreException
	 */
	@Override
	protected boolean getStringResult(String leftOperand) throws CoreException {
		// not of address
		return false;
	}

}
