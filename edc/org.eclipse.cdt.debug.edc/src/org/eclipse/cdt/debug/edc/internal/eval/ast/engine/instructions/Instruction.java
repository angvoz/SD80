/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
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
import java.text.MessageFormat;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.core.runtime.CoreException;

public abstract class Instruction {

	protected static BigInteger Mask8Bytes = new BigInteger("ffffffffffffffff", 16); //$NON-NLS-1$

	protected Interpreter fInterpreter;

	/**
	 * Get instruction size
	 * 
	 * return instruction size
	 */
	public abstract int getSize();

	/**
	 * Set instruction's fInterpreter
	 * 
	 * @param interpreter
	 */
	public void setInterpreter(Interpreter interpreter) {
		fInterpreter = interpreter;
	}

	/**
	 * Stop the instruction fInterpreter
	 */
	public void stop() {
		fInterpreter.stop();
	}

	/**
	 * Execute the instruction
	 * @throws CoreException
	 */
	public abstract void execute() throws CoreException;

	/**
	 * Get the instruction's context
	 * 
	 * @return instruction fInterpreter context
	 */
	protected IDMContext getContext() {
		return fInterpreter.getContext();
	}

	/**
	 * Jump to an instruction fInterpreter offset
	 * 
	 * @param offset
	 *            - fInterpreter offset
	 */
	protected void jump(int offset) {
		fInterpreter.jump(offset);
	}

	/**
	 * Push an object on the instruction stack
	 * 
	 * @param op
	 */
	protected OperandValue push(OperandValue op) {
		fInterpreter.push(op);
		return op;
	}

	/**
	 * Pop an object off the instruction stack
	 * 
	 * @return object on the top of the stack
	 */
	protected OperandValue pop() {
		return fInterpreter.pop();
	}

	/**
	 * Pop a value from the instruction stack
	 * 
	 * @return current top of stack, if the stack is not empty, or
	 *         <code>null</code> otherwise
	 * @throws CoreException 
	 */
	protected OperandValue popValue() throws CoreException {
		OperandValue value = null;
		if (!fInterpreter.isEmpty())
			value = fInterpreter.pop();
		if (value == null)
			throw EDCDebugger.newCoreException(ASTEvalMessages.Instruction_EmptyStack);
		return value;
	}

	/**
	 * Push a boolean on the instruction stack
	 * 
	 * @param value
	 *            - boolean value
	 */
	protected OperandValue pushNewValue(IType type, boolean value) {
		OperandValue op = new OperandValue(value ? 1 : 0, type);
		fInterpreter.push(op);
		return op;
	}
	

	/**
	 * Push a number on the instruction stack
	 * 
	 * @param value
	 *            - number value
	 */
	protected OperandValue pushNewValue(IType type, Number value) {
		OperandValue op = new OperandValue(value, type);
		fInterpreter.push(op);
		return op;
	}

	/**
	 * Push a string on the instruction stack
	 * 
	 * @param value
	 *            - string value
	 */
	protected OperandValue pushNewValue(IType type, String value) {
		OperandValue op = new OperandValue(value, type);
		fInterpreter.push(op);
		return op;
	}

	/**
	 * Convert operands to types expected by getBinaryPromotionType() (e.g.,
	 * VariableWithValue to its underlying Long)
	 * 
	 * @param operand
	 *            - original operand
	 * @return result operand type
	 * @throws CoreException if value cannot be fetched
	 */
	protected OperandValue convertForPromotion(OperandValue operand) throws CoreException {
		IType type = getBasicType(operand.getValueType());
		if (type instanceof ICompositeType) {
			throw EDCDebugger.newCoreException(ASTEvalMessages.Instruction_CannotUseCompositeType);
		}
		if (type instanceof IArrayType && operand.getValueLocation() != null) {
			// take address as value
			return new OperandValue(operand.getValueLocationAddress(), fInterpreter.getTypeEngine().getPointerSizeType());
		}
		return operand;
	}

	/**
	 * Get result binary operation type given types of the left and right
	 * operands, according to Java rules
	 * 
	 * @param left
	 *            - left operand
	 * @param right
	 *            - right operand
	 * @return result T_ type
	 * @throws CoreException 
	 */
	public int getJavaBinaryPromotionType(OperandValue left, OperandValue right) throws CoreException {
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
	public int getBinaryPromotionType(int left, int right) {
		return fTypeTable[left][right];
	}

	/**
	 * Get T_ type for a base Java type (e.g., Character or Boolean).
	 * <p>
	 * This differs from the actual C type in that we pick the Java
	 * type that has the same size as the C type.  I.e., "char" of size 1
	 * becomes T_byte and "wchar_t" of size 4 becomes T_int.
	 * 
	 * @param value
	 *            - base Java type object
	 * @return corresponding T_ type, or T_undefined if value is not a base type
	 * @throws CoreException 
	 */
	public int getValueType(OperandValue op) throws CoreException {
		Number value = op.getValue();
		if (value == null) {
			if (op.getStringValue() != null)
				return T_String;
		}
		// respect Java types first, since C types can alias (e.g. int == long)
		if (value instanceof Integer)
			return T_int;
		if (value instanceof Short)
			return T_short;
		if (value instanceof Byte)
			return T_byte;
		if (value instanceof Long)
			return T_long;
		if (value instanceof Float)
			return T_float;
		if (value instanceof Double)
			return T_double;
		if (value instanceof BigInteger)
			return T_BigInt;
		
		return getJavaValueType(op.getValueType());
	}

	/**
	 * Get T_ type corresponding to the given C type.  This is used for promotion.
	 * Note: in this interpretation, long long maps to T_BigInt.
	 *  
	 * @param type the basic type
	 * @return corresponding T_ type, or T_undefined if value is not a base type
	 * @throws CoreException 
	 */
	public int getCValueType(OperandValue op) throws CoreException {
		if (op.getStringValue() != null) {
			return T_String;
		}
		
		IType type_ = getBasicType(op.getValueType());
		
		if (!(type_ instanceof ICPPBasicType))
			return T_undefined;
		
		ICPPBasicType type = (ICPPBasicType) type_;
		
		switch (type.getBaseType()) {
		case ICPPBasicType.t_bool:
			return T_boolean;
		case ICPPBasicType.t_char:
			return T_char;
		case ICPPBasicType.t_float:
			return T_float;
		case ICPPBasicType.t_double:
			if (type.isLong())
				assert(false); // TODO; need long double type
			return T_double;
		case ICPPBasicType.t_int:
			if (type.isLongLong())
				return T_BigInt;
			if (type.isLong())
				return T_long;
			if (type.isShort())
				return T_short;
			return T_int;
		case ICPPBasicType.t_unspecified:
			return T_undefined;
		case ICPPBasicType.t_void:
			return T_void;
		}
		return T_undefined;
	}

	/**
	 * Get result binary operation type given types of the left and right
	 * operands.  This uses the C rules for type promotion.  
	 * 
	 * @param left
	 *            - left type
	 * @param right
	 *            - right type
	 * @return result type or <code>null</code> if an undefined promotion
	 */
	public IType getBinaryPromotionType(OperandValue leftOp, OperandValue rightOp) throws CoreException {
		
		int leftType = getCValueType(leftOp);
		int rightType = getCValueType(rightOp);
		int promoted = getBinaryPromotionType(leftType, rightType);

		if (promoted == T_null || promoted == T_undefined || promoted == T_void)
			throw EDCDebugger.newCoreException(MessageFormat.format(ASTEvalMessages.Instruction_UnhandledTypeCombination,
					leftOp.getValueType().getName(), rightOp.getValueType().getName()));
		
		// promoted type loses qualifier bits and enum-ness
		if (leftType == promoted)
			return getBasicType(leftOp.getValueType());
		if (rightType == promoted)
			return getBasicType(rightOp.getValueType());
		
		// we're here because the promoted type is bigger than either of the
		// incoming types (e.g. short + float -> double)
		
		boolean isSigned = true;
		
		switch (promoted) {
		case T_char:
			return fInterpreter.getTypeEngine().getIntegerTypeFor(TypeUtils.BASIC_TYPE_CHAR, isSigned);
		case T_short:
			return fInterpreter.getTypeEngine().getIntegerTypeFor(TypeUtils.BASIC_TYPE_SHORT, isSigned);
		case T_int:
			return fInterpreter.getTypeEngine().getIntegerTypeFor(TypeUtils.BASIC_TYPE_INT, isSigned);
		case T_long:
			return fInterpreter.getTypeEngine().getIntegerTypeFor(TypeUtils.BASIC_TYPE_LONG, isSigned);
		case T_float:
			return fInterpreter.getTypeEngine().getFloatTypeOfSize(TypeUtils.BASIC_TYPE_FLOAT);
		case T_double:
			return fInterpreter.getTypeEngine().getFloatTypeOfSize(TypeUtils.BASIC_TYPE_DOUBLE);
		// TODO: long double
			
		case T_byte:	// should not happen
		case T_null:	// should not happen
		case T_Object:	// should not happen
		case T_String:	// should not happen
		default:
			assert(false);
			throw EDCDebugger.newCoreException(ASTEvalMessages.UnhandledTypeCode + promoted);
		}
	}
	
	/**
	 * Get the stripped down basic type that promotion rules work with.
	 * Ignore const/volatile/... qualifiers, demote enums to ints, etc.
	 * @param type
	 * @return adjusted type
	 */
	private IType getBasicType(IType type) {
		type = TypeUtils.getStrippedType(type);
		if (type instanceof IEnumeration) {
			// discover the appropriate integer to hold this
			int byteSize = ((IEnumeration) type).getByteSize();
			type = fInterpreter.getTypeEngine().getIntegerTypeOfSize(byteSize, true);
		}
		return type;
	}

	/**
	 * Get T_ type for a base Java type that can hold a given type
	 * (e.g., Character or Boolean).
	 * <p>
	 * This differs from the actual C type in that we pick the Java
	 * type that has a compatible size as the C type.  I.e., "char" of size 1
	 * becomes T_byte and "wchar_t" of size 4 becomes T_int.  (An actual
	 * target may have larger or smaller primitive types than Java.)
	 * <p> 
	 * Note: when we do unsigned math, we go up one type size in order to handle
	 * unsigned math properly.
	 * 
	 * @param value
	 *            - base Java type object
	 * @return corresponding T_ type, T_string, or T_undefined if value is not a base or string type
	 */
	public int getJavaValueType(IType type_) {
		type_ = getBasicType(type_);
		if (!(type_ instanceof ICPPBasicType)) {
			if (type_ instanceof IArrayType && type_.getType() instanceof ICPPBasicType
				&& ((((ICPPBasicType) type_.getType()).getBaseType() == ICPPBasicType.t_char
				|| ((ICPPBasicType) type_.getType()).getBaseType() == ICPPBasicType.t_wchar_t))) {
				return T_String;
			}
			return T_undefined;
		}
		
		ICPPBasicType type = (ICPPBasicType) type_;
		
		switch (type.getBaseType()) {
		case ICPPBasicType.t_bool:
			return T_boolean;
		case ICPPBasicType.t_char:
			return T_char;
		case ICPPBasicType.t_float:
			return T_float;
		case ICPPBasicType.t_double:
			if (type.isLong())
				assert(false); // TODO
			return T_double;
		case ICPPBasicType.t_unspecified:
			return T_undefined;
		case ICPPBasicType.t_void:
			return T_void;
		case ICPPBasicType.t_int:
			if (type.isLongLong()) {
				if (type.getByteSize() > 8 || (type.getByteSize() == 8 && type.isUnsigned()))
					return T_BigInt;	// java long cannot handle unsigned 8-byte math
				else
					return T_long;
			}
			if (type.isLong()) {
				if (type.getByteSize() > 8 || (type.getByteSize() == 8 && type.isUnsigned()))
					return T_BigInt;	// java long cannot handle unsigned 8-byte math
				else
					return T_long;
			}
			switch (type.getByteSize()) {
			case 1:
				if (type.isUnsigned())
					return T_short;
				else
					return T_byte;
			case 2:
				if (type.isUnsigned())
					return T_int;
				else
					return T_short;
			case 4:
				if (type.isUnsigned())
					return T_int;
				else
					return T_short;
			}
			if (type.isLong()) {
				if (type.isUnsigned() && type.getByteSize() > 8)
					return T_BigInt;
				else
					return T_long;
			}
			
			
		}
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
