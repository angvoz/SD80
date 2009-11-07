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

/*
 * Get the correct type of value from an object, converting if needed
 */
public class GetValue {

	/**
	 * Get the boolean value of an object
	 * 
	 * @param value
	 *            - possibly Boolean object
	 * @return boolean value of param, or false if param is not a Boolean object
	 */
	public static boolean getBooleanValue(Object value) {
		if (value instanceof Boolean)
			return (Boolean) value;
		return false;
	}

	/**
	 * Get the integer value of an object
	 * 
	 * @param value
	 *            - possibly Integer, Short, Byte, or Character object
	 * @return integer value of param, or 0 if param is not an integer object
	 */
	public static int getIntValue(Object value) {
		if (value instanceof Integer)
			return (Integer) value;
		if (value instanceof Short)
			return new Integer((Short) value);
		if (value instanceof Byte)
			return new Integer((Byte) value);
		if (value instanceof Character)
			return new Integer((Character) value);
		return 0;
	}

	/**
	 * Get the long value of an object
	 * 
	 * @param value
	 *            - possibly Long, Integer, Short, Byte, or Character object
	 * @return long value of param, or 0 if param is not an integral object
	 */
	public static long getLongValue(Object value) {
		if (value instanceof Long)
			return (Long) value;
		if (value instanceof Integer)
			return new Long((Integer) value);
		if (value instanceof Short)
			return new Long((Short) value);
		if (value instanceof Byte)
			return new Long((Byte) value);
		if (value instanceof Character)
			return new Long((Character) value);
		return 0;
	}

	/**
	 * Get the BigInteger value of an object
	 * 
	 * @param value
	 *            - possibly BigInteger, Long, Integer, Short, Byte, or
	 *            Character object
	 * @return BigInteger value of param, or 0 if param is not an integral
	 *         object
	 */
	public static BigInteger getBigIntegerValue(Object value) {
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
		if (value instanceof Character)
			return new BigInteger(new byte[] { (byte) Character.getNumericValue((Character) value) });
		return BigInteger.ZERO;
	}

	/**
	 * Get the float value of an object
	 * 
	 * @param value
	 *            - possibly Float or integral (e.g., Long) object
	 * @return float value of param, or 0 if param is not a Float or integral
	 *         object
	 */
	public static float getFloatValue(Object value) {
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
		if (value instanceof Character)
			return new Float((Character) value);
		if (value instanceof BigInteger)
			return new Float(((BigInteger) value).floatValue());
		return 0;
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
	public static double getDoubleValue(Object value) {
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
		if (value instanceof Character)
			return new Double((Character) value);
		if (value instanceof BigInteger)
			return new Double(((BigInteger) value).doubleValue());
		return 0;
	}

	/**
	 * Get the string value of an object
	 * 
	 * @param value
	 *            - String or Character object
	 * @return string value of String param, or quoted string for Character
	 *         param
	 */
	public static String getStringValue(Object value) {
		if (value instanceof Character)
			return "\"" + ((Character) value).charValue() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		return (String) value;
	}

}
