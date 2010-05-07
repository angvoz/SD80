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

/**
 * Get the correct type of value from an object, converting if needed.
 * <p>
 * All of these expect to be called with values no larger than their types (e.g. by {@link Instruction#convertForPromotion(Object)})
 * so we throw exceptions if not.
 */
public class GetValue {

	private static CoreException badType() {
		return EDCDebugger.newCoreException(ASTEvalMessages.GetValue_TypePromotionError);
	}
	
	/**
	 * Get the boolean value of an object
	 * 
	 * @param value
	 *            - possibly Boolean object
	 * @return boolean value of param, or false if param is not a Boolean object
	 */
	public static boolean getBooleanValue(OperandValue op) throws CoreException {
		Number value = op.getValue();
		if (value instanceof BigInteger)
			return ((BigInteger) value).signum() != 0 ? true : false;
		return value.longValue() != 0;
	}

	/**
	 * Get the integer value of an object
	 * 
	 * @param value
	 *            - possibly Integer, Short, or Byte object
	 * @return integer value of param, or 0 if param is not an integer object
	 */
	public static int getIntValue(OperandValue op) throws CoreException  {
		Number value = op.getValue();
		if (value instanceof Integer)
			return (Integer) value;
		if (value instanceof Short)
			return new Integer((Short) value);
		if (value instanceof Byte)
			return new Integer((Byte) value);
		throw badType();
	}

	/**
	 * Get the long value of an object
	 * 
	 * @param value value with Long, Integer, Short, or Byte value
	 * @return long value of param, or 0 if param is not an integral object
	 */
	public static long getLongValue(OperandValue op) throws CoreException  {
		Number value = op.getValue();
		if (value instanceof Long)
			return (Long) value;
		if (value instanceof Integer)
			return new Long((Integer) value);
		if (value instanceof Short)
			return new Long((Short) value);
		if (value instanceof Byte)
			return new Long((Byte) value);
		throw badType();
	}

	/**
	 * Get the BigInteger value of an object
	 * 
	 * @param value value with possibly BigInteger, Long, Integer, Short, Byte, or
	 *            Character object
	 * @return BigInteger value of param, or 0 if param is not an integral
	 *         object
	 */
	public static BigInteger getBigIntegerValue(OperandValue op) throws CoreException  {
		Number value = op.getValue();
		if (value instanceof BigInteger)
			return (BigInteger) value;
		if (value instanceof Long)
			return new BigInteger(((Long) value).toString());
		if (value instanceof Integer)
			return new BigInteger(((Integer) value).toString());
		if (value instanceof Short)
			return new BigInteger(((Short) value).toString());
		if (value instanceof Byte)
			return new BigInteger(new byte[] { (Byte) value });
		//if (value instanceof Character)
		//	return new BigInteger(new byte[] { (byte) Character.getNumericValue((Character) value) });
		throw badType();
	}

	/**
	 * Get the float value of an object
	 * 
	 * @param value with possibly Float or integral (e.g., Long) object
	 * @return float value of param, or 0 if param is not a Float or integral
	 *         object
	 */
	public static float getFloatValue(OperandValue op) throws CoreException  {
		Number value = op.getValue();
		if (value instanceof Float)
			return (Float) value;
		if (value instanceof Long)
			return new Float((Long) value);
		if (value instanceof Integer)
			return new Float((Integer) value);
		if (value instanceof Short)
			return new Float((Short) value);
		if (value instanceof Byte)
			return new Float((Byte) value);
		if (value instanceof BigInteger)
			return new Float(((BigInteger) value).floatValue());
		throw badType();
	}

	/**
	 * Get the double value of an object
	 * 
	 * @param value
	 *            - possibly float (e.g., Double) or integral (e.g., Long)
	 *            object
	 * @return double value of param, or 0 if param is not a float or integral
	 *         object
	 */
	public static double getDoubleValue(OperandValue op) throws CoreException  {
		Number value = op.getValue();
		if (value instanceof Double)
			return (Double) value;
		if (value instanceof Float)
			return new Double((Float) value);
		if (value instanceof Long)
			return new Double((Long) value);
		if (value instanceof Integer)
			return new Double((Integer) value);
		if (value instanceof Short)
			return new Double((Short) value);
		if (value instanceof Byte)
			return new Double((Byte) value);
		if (value instanceof BigInteger)
			return new Double(((BigInteger) value).doubleValue());
		throw badType();
	}

	/**
	 * Get the string value of an object
	 * 
	 * @param value
	 *            - String or Character object
	 * @return string value of String param, or quoted string for Character
	 *         param
	 */
	public static String getStringValue(OperandValue value) throws CoreException  {
		if (value.getStringValue() != null)
			return value.getStringValue();
		return "\"" + (char) (value.getValue().longValue()) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		//throw badType();
	}

}
