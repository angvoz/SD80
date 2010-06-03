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
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.core.runtime.CoreException;

/*
 * Unary logical operator, such as "!"
 */
public abstract class UnaryLogicalOperator extends CompoundInstruction {

	/**
	 * Constructor for a unary logical operator, such as "!"
	 * 
	 * @param start
	 *            - instruction start
	 */
	public UnaryLogicalOperator(int start) {
		super(start);
	}

	/**
	 * Constructor for a unary logical operator, such as "!"
	 * 
	 * @param resultId
	 *            - for assignment, variable ID of the result
	 * @param isAssignmentOperator
	 *            - whether the result is assigned
	 * @param start
	 *            - instruction start
	 */
	public UnaryLogicalOperator(int resultId, boolean isAssignmentOperator, int start) {
		super(start);
	}

	/**
	 * Resolve a unary logical expression
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() throws CoreException {
		OperandValue operand = popValue();

		operand = convertForPromotion(operand);

		// change chars/shorts to int, etc.
		int resultType = getJavaBinaryPromotionType(operand, operand);
		IType type = fInterpreter.getTypeEngine().getBooleanType(1);
		
		switch (resultType) {
		case T_String:
			pushNewValue(type, getStringResult(GetValue.getStringValue(operand)));
			break;
		case T_double:
			pushNewValue(type, getDoubleResult(GetValue.getDoubleValue(operand)));
			break;
		case T_float:
			pushNewValue(type, getFloatResult(GetValue.getFloatValue(operand)));
			break;
		case T_long:
			pushNewValue(type, getLongResult(GetValue.getLongValue(operand)));
			break;
		case T_int:
			pushNewValue(type, getIntResult(GetValue.getIntValue(operand)));
			break;
		case T_boolean:
			pushNewValue(type, getBooleanResult(GetValue.getBooleanValue(operand)));
			break;
		case T_BigInt:
			pushNewValue(type, getBigIntegerResult(GetValue.getBigIntegerValue(operand)));
			break;
		default:
			throw EDCDebugger.newCoreException(ASTEvalMessages.UnhandledTypeCode + resultType);

		}
	}

	/**
	 * Get boolean result of applying a unary logical operation to an int
	 * 
	 * @param operand
	 *            - int operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract boolean getIntResult(int operand) throws CoreException;

	/**
	 * Get boolean result of applying a unary logical operation to a long
	 * 
	 * @param operand
	 *            - long operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract boolean getLongResult(long operand) throws CoreException;

	/**
	 * Get boolean result of applying a unary logical operation to a BigInteger
	 * 
	 * @param operand
	 *            - BigInteger operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract boolean getBigIntegerResult(BigInteger operand) throws CoreException;

	/**
	 * Get boolean result of applying a unary logical operation to a float
	 * 
	 * @param operand
	 *            - float operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 */
	protected abstract boolean getFloatResult(float operand);

	/**
	 * Get boolean result of applying a unary logical operation to a double
	 * 
	 * @param operand
	 *            - double operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 */
	protected abstract boolean getDoubleResult(double operand);

	/**
	 * Get boolean result of applying a unary logical operation to a boolean
	 * 
	 * @param operand
	 *            - boolean operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 */
	protected abstract boolean getBooleanResult(boolean operand);

	/**
	 * Get boolean result of applying a unary logical operation to a string
	 * 
	 * @param operand
	 *            - string operand
	 * @return boolean result of the operation if possible, or an
	 *         operation-specific default
	 * @throws CoreException
	 */
	protected abstract boolean getStringResult(String operand) throws CoreException;

}
