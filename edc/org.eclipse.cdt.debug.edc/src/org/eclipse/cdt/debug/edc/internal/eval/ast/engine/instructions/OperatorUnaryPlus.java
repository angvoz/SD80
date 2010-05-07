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

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.core.runtime.CoreException;

/*
 * Unary plus operation "+"
 */
public class OperatorUnaryPlus extends UnaryOperator {

	/**
	 * Constructor for unary plus operator "+"
	 * 
	 * @param start
	 */
	public OperatorUnaryPlus(int start) {
		super(start);
	}

	/**
	 * Constructor for a unary plus operation "+"
	 * 
	 * @param resultId
	 *            - for assignment, variable ID of the result
	 * @param isAssignmentOperator
	 *            - whether the result is assigned
	 * @param start
	 *            - instruction start
	 */
	public OperatorUnaryPlus(int resultId, boolean isAssignmentOperator, int start) {
		super(start);
	}

	/**
	 * Get int result of applying unary plus "+" to an int
	 * 
	 * @param operand
	 *            - int operand
	 * @return <code>operand</code>
	 * @throws CoreException
	 */
	@Override
	protected int getIntResult(int operand) throws CoreException {
		return operand;
	}

	/**
	 * Get long result of applying unary plus "+" to a long
	 * 
	 * @param operand
	 *            - long operand
	 * @return <code>operand</code>
	 * @throws CoreException
	 */
	@Override
	protected long getLongResult(long operand) throws CoreException {
		return operand;
	}

	/**
	 * Get BigInteger result of applying unary plus "+" to a BigInteger
	 * 
	 * @param operand
	 *            - BigInteger operand
	 * @param length
	 *            - length in bytes of result
	 * @return <code>operand</code>
	 * @throws CoreException
	 */
	@Override
	protected BigInteger getBigIntegerResult(BigInteger operand, int length) throws CoreException {
		return operand;
	}

	/**
	 * Get float result of applying unary plus "+" to a float
	 * 
	 * @param operand
	 *            - float operand
	 * @return <code>operand</code>
	 */
	@Override
	protected float getFloatResult(float operand) {
		return operand;
	}

	/**
	 * Get double result of applying unary plus "+" to a double
	 * 
	 * @param operand
	 *            - double operand
	 * @return <code>operand</code>
	 */
	@Override
	protected double getDoubleResult(double operand) {
		return operand;
	}

	/**
	 * Get boolean result of applying unary plus "+" to a boolean
	 * 
	 * @param operand
	 *            - boolean operand
	 * @return <code>false</code>
	 */
	@Override
	protected boolean getBooleanResult(boolean operand) {
		return false;
	}

	/**
	 * Get string result of applying unary plus "+" to a string
	 * 
	 * @param operand
	 *            - string operand
	 * @return <code>null</code>
	 * @throws CoreException
	 */
	@Override
	protected String getStringResult(String operand) throws CoreException {
		throw EDCDebugger.newCoreException(ASTEvalMessages.UnsupportedStringOperation);
	}

}
