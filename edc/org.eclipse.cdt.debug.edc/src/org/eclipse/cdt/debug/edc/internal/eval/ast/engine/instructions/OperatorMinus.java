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
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.core.runtime.CoreException;

/*
 * Binary minus operation "-"
 */
public class OperatorMinus extends BinaryOperator {

	/**
	 * Constructor for a binary minus operation "-"
	 * 
	 * @param start
	 *            - instruction start
	 */
	public OperatorMinus(int start) {
		this(0, false, start);
	}

	/**
	 * Constructor for a binary minus operation "-"
	 * 
	 * @param resultId
	 *            - for assignment, variable ID of the result
	 * @param isAssignmentOperator
	 *            - whether the result is assigned
	 * @param start
	 *            - instruction start
	 */
	protected OperatorMinus(int resultId, boolean isAssignmentOperator, int start) {
		super(resultId, isAssignmentOperator, start);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.BinaryOperator#customHandleOperation(org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperandValue, org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions.OperandValue)
	 */
	@Override
	protected boolean customHandleOperation(Interpreter fInterpreter,
			OperandValue left, OperandValue right) throws CoreException {
		IType rightType = null;
		IType leftType = null;

		boolean isLeftPointer = false;
		leftType = TypeUtils.getStrippedType(left.getValueType());
		isLeftPointer = leftType instanceof IPointerType || leftType instanceof IArrayType;

		boolean isRightPointer = false;
		rightType = TypeUtils.getStrippedType(right.getValueType());
		isRightPointer = rightType instanceof IPointerType || rightType instanceof IArrayType;

		// no pointer operands
		if (!isLeftPointer && !isRightPointer) {
			return false;
		}

		// only pointer is on the right
		if (!isLeftPointer && isRightPointer) {
			throw EDCDebugger.newCoreException(ASTEvalMessages.OperatorMinus_NonPtrMinusPtr);
		}

		// ignore strings
		if (getValueType(left) == T_String && getValueType(right) == T_String)
			return false;
		
		BigInteger bigIntAddress = left.getBigIntValue();
		
		BigInteger aggregateSize;
		if (leftType instanceof IPointerType)
			aggregateSize = BigInteger.valueOf(leftType.getByteSize());
		else
			aggregateSize = BigInteger.valueOf(TypeUtils.getStrippedType(leftType.getType())
					.getByteSize());

		if (!isRightPointer) {
			right = convertForPromotion(right);
		}

		BigInteger subtractAmount = right.getBigIntValue();

		if (!isRightPointer)
			subtractAmount = subtractAmount.multiply(aggregateSize);

		bigIntAddress = bigIntAddress.subtract(subtractAmount);

		// if both are pointers, subtract, then divide by size of what's pointed
		// to
		if (isRightPointer && aggregateSize.longValue() != 0)
			bigIntAddress = bigIntAddress.divide(aggregateSize);

		if (isRightPointer)
			pushNewValue(left.getValueType(), bigIntAddress);
		else
			pushNewValue(fInterpreter.getTypeEngine().getPointerSizeType(), bigIntAddress);
		
		return true;
	}

	/**
	 * Get int result of applying binary minus "-" to two ints
	 * 
	 * @param leftOperand
	 *            - left int operand
	 * @param rightOperand
	 *            - right int operand
	 * @return <code>leftOperand</code> - <code>rightOperand</code>
	 * @throws CoreException
	 */
	@Override
	protected int getIntResult(int leftOperand, int rightOperand) throws CoreException {
		return leftOperand - rightOperand;
	}

	/**
	 * Get long result of applying binary minus "-" to two longs
	 * 
	 * @param leftOperand
	 *            - left long operand
	 * @param rightOperand
	 *            - right long operand
	 * @return <code>leftOperand</code> - <code>rightOperand</code>
	 * @throws CoreException
	 */
	@Override
	protected long getLongResult(long leftOperand, long rightOperand) throws CoreException {
		return leftOperand - rightOperand;
	}

	/**
	 * Get BigInteger result of applying binary minus "-" to two BigIntegers
	 * 
	 * @param leftOperand
	 *            - left BigInteger operand
	 * @param rightOperand
	 *            - right BigInteger operand
	 * @param length
	 *            - length in bytes of result
	 * @return <code>leftOperand</code> - <code>rightOperand</code>, truncated
	 *         to 8 bytes
	 * @throws CoreException
	 */
	@Override
	protected BigInteger getBigIntegerResult(BigInteger leftOperand, BigInteger rightOperand, int length)
			throws CoreException {
		return leftOperand.subtract(rightOperand).and(Instruction.Mask8Bytes);
	}

	/**
	 * Get float result of applying binary minus "-" to two floats
	 * 
	 * @param leftOperand
	 *            - left float operand
	 * @param rightOperand
	 *            - right float operand
	 * @return <code>leftOperand</code> - <code>rightOperand</code>
	 */
	@Override
	protected float getFloatResult(float leftOperand, float rightOperand) {
		return leftOperand - rightOperand;
	}

	/**
	 * Get double result of applying binary minus "-" to two doubles
	 * 
	 * @param leftOperand
	 *            - left double operand
	 * @param rightOperand
	 *            - right double operand
	 * @return <code>leftOperand</code> - <code>rightOperand</code>
	 */
	@Override
	protected double getDoubleResult(double leftOperand, double rightOperand) {
		return leftOperand - rightOperand;
	}

	/**
	 * Get boolean result of applying binary minus "-" to two booleans
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
