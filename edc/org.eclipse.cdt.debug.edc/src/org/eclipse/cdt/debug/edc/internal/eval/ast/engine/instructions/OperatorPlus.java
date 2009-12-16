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

import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IType;
import org.eclipse.cdt.debug.edc.internal.symbols.TypeUtils;
import org.eclipse.cdt.utils.Addr64;
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

	/**
	 * Resolve a binary add operator "+"
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

		// let others convert the type to the string "bool", "int", etc.
		this.setValueType(ASTEvaluationEngine.UNKNOWN_TYPE);
		if (doPointerArithmetic(right, left))
			return;

		right = convertForPromotion(right);
		left = convertForPromotion(left);

		// let others convert the type to the string "bool", "int", etc.
		this.setValueType(ASTEvaluationEngine.UNKNOWN_TYPE);

		int resultType = getBinaryPromotionType(right, left);

		switch (resultType) {
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
			// TODO: get the length of a long long rather than using hard-coded
			// 8
			pushNewValue(getBigIntegerResult(GetValue.getBigIntegerValue(left), GetValue.getBigIntegerValue(right), 8));
			break;
		}
	}

	private boolean doPointerArithmetic(Object right, Object left) {
		VariableWithValue rightVar = null;
		IType rightType = null;
		VariableWithValue leftVar = null;
		IType leftType = null;

		boolean isLeftPointer = false;
		if (left instanceof VariableWithValue) {
			leftVar = (VariableWithValue) left;
			leftType = TypeUtils.getStrippedType(leftVar.getVariable().getType());
			isLeftPointer = leftType instanceof IPointerType || leftType instanceof IAggregate;
		}

		boolean isRightPointer = false;
		if (right instanceof VariableWithValue) {
			rightVar = (VariableWithValue) right;
			rightType = TypeUtils.getStrippedType(rightVar.getVariable().getType());
			isRightPointer = rightType instanceof IPointerType || rightType instanceof IAggregate;
		}

		// zero pointer operands
		if (!isLeftPointer && !isRightPointer) {
			return false;
		}

		// two pointer operands
		if (isLeftPointer && isRightPointer) {
			InvalidExpression invalidExpression = new InvalidExpression(ASTEvalMessages.OperatorPlus_PtrPlusPtr);
			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return true;
		}

		// get the non-pointer on the left
		if (isRightPointer) {
			Object temp = right;
			right = left;
			left = temp;
			temp = rightVar;
			rightVar = leftVar;
			leftVar = (VariableWithValue) temp;
			temp = rightType;
			rightType = leftType;
			leftType = (IType) temp;
		}

		Object address = leftVar.getValue();
		if (address instanceof Addr64)
			address = ((Addr64) address).getValue();

		// convert the left address to BigInteger
		String bigIntString = "0"; //$NON-NLS-1$
		if (!(address instanceof BigInteger)) {
			if (address instanceof Integer)
				bigIntString = Long.toString(((Integer) address).longValue());
			else if (address instanceof Short)
				bigIntString = Long.toString(((Short) address).longValue());
			else if (address instanceof Character)
				bigIntString = Long.toString(((Character) address));
			else if (address instanceof Byte)
				bigIntString = Long.toString(((Byte) address).longValue());
			else if (address instanceof Long)
				bigIntString = Long.toString(((Long) address).longValue());
			else
				return false;
			address = new BigInteger(bigIntString, 10);
		}

		BigInteger bigIntAddress = (BigInteger) address;
		BigInteger aggregateSize = BigInteger.ZERO;
		if (leftType instanceof IPointerType)
			aggregateSize = new BigInteger(Integer.toString(leftType.getByteSize()));
		else
			aggregateSize = new BigInteger(Integer.toString(TypeUtils.getStrippedType(leftType.getType())
					.getByteSize()));
		BigInteger addAmount = aggregateSize;

		right = convertForPromotion(right);
		bigIntString = "0"; //$NON-NLS-1$
		if (!(right instanceof BigInteger)) {
			if (right instanceof Integer)
				bigIntString = Long.toString(((Integer) right).longValue());
			else if (right instanceof Short)
				bigIntString = Long.toString(((Short) right).longValue());
			else if (right instanceof Character)
				bigIntString = Long.toString(((Character) right));
			else if (right instanceof Byte)
				bigIntString = Long.toString(((Byte) right).longValue());
			else if (right instanceof Long)
				bigIntString = Long.toString(((Long) right).longValue());
			else
				return false;
			addAmount = addAmount.multiply(new BigInteger(bigIntString, 10));
		} else
			addAmount = addAmount.multiply((BigInteger) right);

		bigIntAddress = bigIntAddress.add(addAmount);

		if (bigIntAddress.bitCount() < 64) {
			pushNewValue(bigIntAddress.longValue());
		} else {
			pushNewValue(bigIntAddress);
		}

		setValueLocation(new Long(0));
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
		return leftOperand.substring(0, leftOperand.length() - 1) + rightOperand.substring(1);
	}

}
