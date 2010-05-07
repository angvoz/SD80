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
 * Binary add operation "+"
 */
public class OperatorPlus extends BinaryOperator {

	/**
	 * Constructor for a binary add operation "+"
	 * 
	 * @param start
	 *            - instruction start
	 */
	public OperatorPlus(int start) {
		this(0, false, start);
	}

	/**
	 * Constructor for a binary add operation "+"
	 * 
	 * @param resultId
	 *            - for assignment, variable ID of the result
	 * @param isAssignmentOperator
	 *            - whether the result is assigned
	 * @param start
	 *            - instruction start
	 */
	protected OperatorPlus(int resultId, boolean isAssignmentOperator, int start) {
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

		// zero pointer operands
		if (!isLeftPointer && !isRightPointer) {
			return false;
		}

		// two pointer operands
		if (isLeftPointer && isRightPointer) {
			// allow for strings...
			if (getValueType(left) == T_String && getValueType(right) == T_String)
				return false;
			throw EDCDebugger.newCoreException(ASTEvalMessages.OperatorPlus_PtrPlusPtr);
		}

		// get the non-pointer on the left
		if (isRightPointer) {
			OperandValue temp = right;
			right = left;
			left = temp;
			temp = right;
			right = left;
			left = temp;
			IType tempType = rightType;
			rightType = leftType;
			leftType = tempType;
		}

		// convert the left address to BigInteger
		BigInteger bigIntAddress = left.getBigIntValue();

		BigInteger aggregateSize = BigInteger.ZERO;
		if (leftType instanceof IPointerType)
			aggregateSize = BigInteger.valueOf(leftType.getByteSize());
		else
			aggregateSize = BigInteger.valueOf(TypeUtils.getStrippedType(leftType.getType())
					.getByteSize());
		BigInteger addAmount = aggregateSize;

		right = convertForPromotion(right);
		
		addAmount = addAmount.multiply(right.getBigIntValue());		

		bigIntAddress = bigIntAddress.add(addAmount);

		pushNewValue(left.getValueType(), bigIntAddress);

		return true;
	}

	/**
	 * Get int result of applying binary plus "+" to two ints
	 * 
	 * @param leftOperand
	 *            - left int operand
	 * @param rightOperand
	 *            - right int operand
	 * @return <code>leftOperand</code> + <code>rightOperand</code>
	 * @throws CoreException
	 */
	@Override
	protected int getIntResult(int leftOperand, int rightOperand) throws CoreException {
		return leftOperand + rightOperand;
	}

	/**
	 * Get long result of applying binary plus "+" to two longs
	 * 
	 * @param leftOperand
	 *            - left long operand
	 * @param rightOperand
	 *            - right long operand
	 * @return <code>leftOperand</code> + <code>rightOperand</code>
	 * @throws CoreException
	 */
	@Override
	protected long getLongResult(long leftOperand, long rightOperand) throws CoreException {
		return leftOperand + rightOperand;
	}

	/**
	 * Get BigInteger result of applying binary plus "+" to two BigIntegers
	 * 
	 * @param leftOperand
	 *            - BigInteger long operand
	 * @param rightOperand
	 *            - BigInteger long operand
	 * @param length
	 *            - length in bytes of result
	 * @return <code>leftOperand</code> + <code>rightOperand</code>, truncated
	 *         to 8 bytes
	 * @throws CoreException
	 */
	@Override
	protected BigInteger getBigIntegerResult(BigInteger leftOperand, BigInteger rightOperand, int length)
			throws CoreException {
		return leftOperand.add(rightOperand).and(Instruction.Mask8Bytes);
	}

	/**
	 * Get float result of applying binary plus "+" to two floats
	 * 
	 * @param leftOperand
	 *            - left float operand
	 * @param rightOperand
	 *            - right float operand
	 * @return <code>leftOperand</code> + <code>rightOperand</code>
	 */
	@Override
	protected float getFloatResult(float leftOperand, float rightOperand) {
		return leftOperand + rightOperand;
	}

	/**
	 * Get double result of applying binary plus "+" to two doubles
	 * 
	 * @param leftOperand
	 *            - left double operand
	 * @param rightOperand
	 *            - right double operand
	 * @return <code>leftOperand</code> + <code>rightOperand</code>
	 */
	@Override
	protected double getDoubleResult(double leftOperand, double rightOperand) {
		return leftOperand + rightOperand;
	}

	/**
	 * Get boolean result of applying binary plus "+" to two booleans
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

	/**
	 * Get string result of applying binary plus "+" to two strings
	 * 
	 * @param leftOperand
	 *            - left string operand
	 * @param rightOperand
	 *            - right string operand
	 * @return <code>leftOperand</code> + <code>rightOperand</code>
	 * @throws CoreException
	 */
	@Override
	protected String getStringResult(String leftOperand, String rightOperand) throws CoreException {
		return leftOperand + rightOperand;
	}

}
