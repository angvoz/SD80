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

import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;

public abstract class Instruction {

	protected static BigInteger Mask8Bytes = new BigInteger("ffffffffffffffff", 16); //$NON-NLS-1$

	private Interpreter fInterpreter;

	/**
	 * Get instruction size
	 * 
	 * return instruction size
	 */
	public abstract int getSize();

	/**
	 * Set instruction's interpreter
	 * 
	 * @param interpreter
	 */
	public void setInterpreter(Interpreter interpreter) {
		fInterpreter = interpreter;
	}

	/**
	 * Set instruction's last value
	 * 
	 * @param value
	 */
	public void setLastValue(Object value) {
		fInterpreter.setLastValue(value);
	}

	/**
	 * Set instruction's location
	 * 
	 * @param value
	 */
	public void setValueLocation(Object value) {
		fInterpreter.setValueLocation(value);
	}

	/**
	 * Set instruction's type
	 * 
	 * @param value
	 */
	public void setValueType(Object value) {
		fInterpreter.setValueType(value);
	}

	/**
	 * Stop the instruction interpreter
	 */
	public void stop() {
		fInterpreter.stop();
	}

	/**
	 * Execute the instruction
	 * 
	 * @throws CoreException
	 */
	public abstract void execute() throws CoreException;

	/**
	 * Get the instruction's context
	 * 
	 * @return instruction interpreter context
	 */
	protected Object getContext() {
		return fInterpreter.getContext();
	}

	/**
	 * Jump to an instruction interpreter offset
	 * 
	 * @param offset
	 *            - interpreter offset
	 */
	protected void jump(int offset) {
		fInterpreter.jump(offset);
	}

	/**
	 * Push an object on the instruction stack
	 * 
	 * @param object
	 */
	protected void push(Object object) {
		fInterpreter.push(object);
	}

	/**
	 * Pop an object off the instruction stack
	 * 
	 * @return object on the top of the stack
	 */
	protected Object pop() {
		return fInterpreter.pop();
	}

	/**
	 * Pop a value from the instruction stack
	 * 
	 * @return current top of stack, if the stack is not empty, or
	 *         <code>null</code> otherwise
	 */
	protected Object popValue() {
		if (!fInterpreter.isEmpty())
			return fInterpreter.pop();
		else
			return null;
	}

	/**
	 * Push a boolean on the instruction stack
	 * 
	 * @param value
	 *            - boolean value
	 */
	protected void pushNewValue(boolean value) {
		fInterpreter.push(new Boolean(value));
	}

	/**
	 * Push a byte on the instruction stack
	 * 
	 * @param value
	 *            - byte value
	 */
	protected void pushNewValue(byte value) {
		fInterpreter.push(new Integer(value));
	}

	/**
	 * Push a short on the instruction stack
	 * 
	 * @param value
	 *            - short value
	 */
	protected void pushNewValue(short value) {
		fInterpreter.push(new Integer(value));
	}

	/**
	 * Push an int on the instruction stack
	 * 
	 * @param value
	 *            - int value
	 */
	protected void pushNewValue(int value) {
		fInterpreter.push(new Integer(value));
	}

	/**
	 * Push a long on the instruction stack
	 * 
	 * @param value
	 *            - long value
	 */
	protected void pushNewValue(long value) {
		fInterpreter.push(new Long(value));
	}

	/**
	 * Push a BigInteger on the instruction stack
	 * 
	 * @param value
	 *            - BigInteger value
	 */
	protected void pushNewValue(BigInteger value) {
		fInterpreter.push(value);
	}

	/**
	 * Push a char on the instruction stack
	 * 
	 * @param value
	 *            - char value
	 */
	protected void pushNewValue(char value) {
		fInterpreter.push(new Character(value));
	}

	/**
	 * Push a float on the instruction stack
	 * 
	 * @param value
	 *            - float value
	 */
	protected void pushNewValue(float value) {
		fInterpreter.push(new Float(value));
	}

	/**
	 * Push a double on the instruction stack
	 * 
	 * @param value
	 *            - double value
	 */
	protected void pushNewValue(double value) {
		fInterpreter.push(new Double(value));
	}

	/**
	 * Push a string on the instruction stack
	 * 
	 * @param value
	 *            - string value
	 */
	protected void pushNewValue(String value) {
		fInterpreter.push(value);
	}

	/**
	 * Push null on the instruction stack
	 */
	protected void pushNullValue() {
		fInterpreter.push(null);
	}

	/**
	 * Convert operands to types expected by getBinaryPromotionType() (e.g.,
	 * VariableWithValue to its underlying Long)
	 * 
	 * @param operand
	 *            - original operand
	 * @return result operand type
	 */
	protected Object convertForPromotion(Object operand) {
		if (operand instanceof VariableWithValue) {
			VariableWithValue variableWithValue = (VariableWithValue) operand;
			if (TypeUtils.isAggregateType(variableWithValue.getVariable().getType())) {
				operand = variableWithValue.getValueLocation();
				if (operand instanceof Addr64)
					operand = ((Addr64) operand).getValue();
			} else {
				operand = variableWithValue.getValue();
			}
		}

		if ((operand instanceof BigInteger) && (((BigInteger) operand).bitCount() < 64))
			operand = ((BigInteger) operand).longValue();

		return operand;
	}

	/**
	 * Get result binary operation type given types of the left and right
	 * operands
	 * 
	 * @param left
	 *            - left operand
	 * @param right
	 *            - right operand
	 * @return result T_ type
	 */
	public static int getBinaryPromotionType(Object left, Object right) {
		int leftType = getValueType(left);
		int rightType = getValueType(right);
		return fTypeTable[leftType][rightType];
	}

	/**
	 * Get result binary operation type given types of the left and right
	 * operands
	 * 
	 * @param left
	 *            - left operand T_ type
	 * @param right
	 *            - right operand T_type
	 * @return result T_ type
	 */
	public static int getBinaryPromotionType(int left, int right) {
		return fTypeTable[left][right];
	}

	/**
	 * Get T_ type for a base Java type (e.g., Character or Boolean)
	 * 
	 * @param value
	 *            - base Java type object
	 * @return corresponding T_ type, or T_undefined if value is not a base type
	 */
	public static int getValueType(Object value) {
		if (value instanceof Integer)
			return T_int;
		if (value instanceof Short)
			return T_short;
		if (value instanceof Character)
			return T_char;
		if (value instanceof Byte)
			return T_byte;
		if (value instanceof Long)
			return T_long;
		if (value instanceof String)
			return T_String;
		if (value instanceof Boolean)
			return T_boolean;
		if (value instanceof Float)
			return T_float;
		if (value instanceof Double)
			return T_double;
		if (value instanceof BigInteger)
			return T_BigInt;
		return T_undefined;
	}

	static public final int T_undefined = 0;
	static public final int T_Object = 1;
	static public final int T_char = 2;
	static public final int T_byte = 3;
	static public final int T_short = 4;
	static public final int T_boolean = 5;
	static public final int T_void = 6;
	static public final int T_long = 7;
	static public final int T_double = 8;
	static public final int T_float = 9;
	static public final int T_int = 10;
	static public final int T_String = 11;
	static public final int T_null = 12;
	static public final int T_BigInt = 13;

	private static final int[][] fTypeTable = {
						//	undefined		object			char			byte			short			boolean			void
		/* undefined */	{	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined, 	T_undefined},
						//	undefined		object			char			byte			short			boolean			void
		/* object */	{	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_String,		T_undefined,	T_undefined },
						//	undefined		object			char			byte			short			boolean			void
		/* char */		{	T_undefined,	T_undefined,	T_int,			T_int,			T_int,			T_undefined,	T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_long,			T_double,		T_float,		T_int,			T_String,		T_undefined,	T_BigInt },
						//	undefined		object			char			byte			short			boolean			void
		/* byte */		{	T_undefined,	T_undefined,	T_int,			T_int,			T_int,			T_undefined,	T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_long,			T_double,		T_float,		T_int,			T_String,		T_undefined,	T_BigInt },
						//	undefined		object			char			byte			short			boolean			void
		/* short */		{	T_undefined,	T_undefined,	T_int,			T_int,			T_int,			T_undefined,	T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_long,			T_double,		T_float,		T_int,			T_String,		T_undefined,	T_BigInt },
						//	undefined		object			char			byte			short			boolean			void
		/* boolean */	{	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_boolean,		T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_String,		T_undefined,	T_undefined },
						//	undefined		object			char			byte			short			boolean			void
		/* void */		{	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined },
						//	undefined		object			char			byte			short			boolean			void
		/* long */		{	T_undefined,	T_undefined,	T_long,			T_long,			T_long,			T_undefined,	T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_long,			T_double,		T_float,		T_long,			T_String,		T_undefined,	T_BigInt },
						//	undefined		object			char			byte			short			boolean			void
		/* double */	{	T_undefined,	T_undefined,	T_double,		T_double,		T_double,		T_undefined,	T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_double,		T_double,		T_double,		T_double,		T_String,		T_undefined,	T_double },
						//	undefined		object			char			byte			short			boolean			void
		/* float */		{	T_undefined,	T_undefined,	T_float,		T_float,		T_float,		T_undefined,	T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_float,		T_double,		T_float,		T_float,		T_String,		T_undefined,	T_float },
						//	undefined		object			char			byte			short			boolean			void
		/* int */		{	T_undefined,	T_undefined,	T_int,			T_int,			T_int,			T_undefined,	T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_long,			T_double,		T_float,		T_int,			T_String,		T_undefined,	T_BigInt },
						//	undefined		object			char			byte			short			boolean			void
		/* String */	{	T_undefined,	T_String,		T_String,		T_String,		T_String,		T_String,		T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_String,		T_String,		T_String,		T_String,		T_String,		T_String,		T_undefined },
						//	undefined		object			char			byte			short			boolean			void
		/* null */		{	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_undefined,	T_undefined,	T_undefined,	T_undefined,	T_String,		T_undefined,	T_undefined },
						//	undefined		object			char			byte			short			boolean			void
		/* BigInteger */{	T_undefined,	T_undefined,	T_BigInt,		T_BigInt,		T_BigInt,		T_undefined,	T_undefined,
						//	long			double			float			int				String			null			BigInteger
							T_BigInt,		T_double,		T_float,		T_BigInt,		T_undefined,	T_undefined,	T_BigInt },		
		};

}
