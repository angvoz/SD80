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
package org.eclipse.cdt.debug.edc.internal.symbols;

import java.math.BigInteger;

/*
 * Various utility routines for Type objects
 */
public class TypeUtils {

	// names to display and type IDs for basic C and C++ types
	static public final String DATA_NAME_CHAR = "char"; //$NON-NLS-1$
	static public final String DATA_NAME_CHAR_UNSIGNED = "unsigned char"; //$NON-NLS-1$
	static public final String DATA_NAME_CHAR_SIGNED = "signed char"; //$NON-NLS-1$
	static public final String DATA_NAME_SHORT = "short"; //$NON-NLS-1$
	static public final String DATA_NAME_SHORT_UNSIGNED = "unsigned short"; //$NON-NLS-1$
	static public final String DATA_NAME_INT = "int"; //$NON-NLS-1$
	static public final String DATA_NAME_INT_UNSIGNED = "unsigned int"; //$NON-NLS-1$
	static public final String DATA_NAME_LONG = "long"; //$NON-NLS-1$
	static public final String DATA_NAME_LONG_UNSIGNED = "unsigned long"; //$NON-NLS-1$
	static public final String DATA_NAME_LONG_LONG = "long long"; //$NON-NLS-1$
	static public final String DATA_NAME_LONG_LONG_UNSIGNED = "unsigned long long"; //$NON-NLS-1$
	static public final String DATA_NAME_FLOAT = "float"; //$NON-NLS-1$
	static public final String DATA_NAME_FLOAT_COMPLEX = "float _Complex"; //$NON-NLS-1$
	static public final String DATA_NAME_DOUBLE = "double"; //$NON-NLS-1$
	static public final String DATA_NAME_DOUBLE_COMPLEX = "double _Complex"; //$NON-NLS-1$
	static public final String DATA_NAME_LONG_DOUBLE = "long double"; //$NON-NLS-1$
	static public final String DATA_NAME_LONG_DOUBLE_COMPLEX = "long double _Complex"; //$NON-NLS-1$
	static public final String DATA_NAME_BOOL = "bool"; //$NON-NLS-1$
	static public final String DATA_NAME_BOOL_C9X = "_Bool"; //$NON-NLS-1$

	// is a type a pointer type?
	public static boolean isPointerType(Object type) {
		if (!(type instanceof IType))
			return false;

		while (type instanceof ITypedef || type instanceof IQualifierType)
			type = ((Type) type).getType();

		return type instanceof IPointerType;
	}

	// is a type an aggregate type?
	public static boolean isAggregateType(Object type) {
		if (!(type instanceof IType))
			return false;

		while (type instanceof ITypedef || type instanceof IQualifierType)
			type = ((Type) type).getType();

		return type instanceof IAggregate;
	}

	// is a type a structured type?
	public static boolean isCompositeType(Object type) {
		if (!(type instanceof IType))
			return false;

		while (type instanceof ITypedef || type instanceof IQualifierType)
			type = ((Type) type).getType();

		return type instanceof ICompositeType;
	}

	// is a type a base type?
	public static boolean isBaseType(Object type) {
		if (!(type instanceof IType))
			return false;

		while (type instanceof ITypedef || type instanceof IQualifierType)
			type = ((IType) type).getType();

		return type instanceof ICPPBasicType;
	}

	// return the unqualified, untypedef'ed type
	public static IType getUnqualifiedType(Object type) {
		if (!(type instanceof IType))
			return null;

		while (type instanceof ITypedef || type instanceof IQualifierType)
			type = ((IType) type).getType();

		return (IType) type;
	}

	public static Object extractBitField(Object value, int byteSize, int bitSize, int bitOffset, boolean isSignedInt) {
		if (bitSize <= 0 || value == null
				|| (!(value instanceof Long) && !(value instanceof Integer) && !(value instanceof BigInteger))) {
			return value;
		}

		// TODO: Need to get actual sizes from the target environment
		// This assumes long and long long are 64 bits, and int is 32 bits
		if (value instanceof Long) {
			long longValue = (Long) value;

			longValue >>= (byteSize * 8) - (bitOffset + bitSize);
			longValue &= (-1) >>> (64 - bitSize);

			if (isSignedInt) {
				if ((longValue & (1 << (bitSize - 1))) != 0) {
					longValue |= ((-1) >>> bitSize) << bitSize;
				}
			}
			return new Long(longValue);
		}

		if (value instanceof Integer) {
			int intValue = (Integer) value;

			intValue >>= (byteSize * 8) - (bitOffset + bitSize);
			intValue &= ((-1) >>> (32 - bitSize));

			if (isSignedInt) {
				if ((intValue & (1 << (bitSize - 1))) != 0) {
					intValue |= ((-1) >>> bitSize) << bitSize;
				}
			}
			return new Integer(intValue);
		}

		if (value instanceof BigInteger) {
			BigInteger bigValue = (BigInteger) value;
			bigValue = bigValue.shiftRight((byteSize * 8) - (bitOffset + bitSize));
			byte[] bytes = new byte[8];
			long mask;
			BigInteger bigMask;

			mask = ((-1) >>> (32 - bitSize));
			for (int i = 0; i < 8; i++) {
				bytes[i] = (byte) ((mask >>> ((7 - i) * 8)) & 0xff);
			}

			bigMask = new BigInteger(bytes);
			bigValue = bigValue.and(bigMask);

			if (isSignedInt) {
				// NOTE: for variable values, we use BigInteger ONLY for
				// unsigned numbers
				if (bigValue.testBit(bitSize - 1)) {
					mask = (((-1) >>> bitSize) << bitSize);
					for (int i = 0; i < 8; i++) {
						bytes[i] = (byte) ((mask >>> ((7 - i) * 8)) & 0xff);
					}

					bigMask = new BigInteger(bytes);
					bigValue = bigValue.or(bigMask);
				}
			}

			return bigValue;
		}

		return value;
	}

}
