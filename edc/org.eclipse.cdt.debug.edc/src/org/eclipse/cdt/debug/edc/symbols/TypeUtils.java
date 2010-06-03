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
package org.eclipse.cdt.debug.edc.symbols;

import java.math.BigInteger;

import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IForwardTypeReference;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IQualifierType;
import org.eclipse.cdt.debug.edc.internal.symbols.IReferenceType;
import org.eclipse.cdt.debug.edc.internal.symbols.ITypedef;


/*
 * Various utility routines for Type objects
 */
public class TypeUtils {

	// type IDs for basic C and C++ types
	static public final int BASIC_TYPE_CHAR				 = 1;
	static public final int BASIC_TYPE_CHAR_UNSIGNED	 = 2;
	static public final int BASIC_TYPE_CHAR_SIGNED		 = 3;
	static public final int BASIC_TYPE_SHORT			 = 4;
	static public final int BASIC_TYPE_SHORT_UNSIGNED	 = 5;
	static public final int BASIC_TYPE_INT				 = 6;
	static public final int BASIC_TYPE_INT_UNSIGNED		 = 7;
	static public final int BASIC_TYPE_LONG				 = 8;
	static public final int BASIC_TYPE_LONG_UNSIGNED	 = 9;
	static public final int BASIC_TYPE_LONG_LONG		 = 10;
	static public final int BASIC_TYPE_LONG_LONG_UNSIGNED = 11;
	static public final int BASIC_TYPE_FLOAT			 = 12;
	static public final int BASIC_TYPE_FLOAT_COMPLEX	 = 13;
	static public final int BASIC_TYPE_DOUBLE			 = 14;
	static public final int BASIC_TYPE_DOUBLE_COMPLEX	 = 15;
	static public final int BASIC_TYPE_LONG_DOUBLE		 = 16;
	static public final int BASIC_TYPE_LONG_DOUBLE_COMPLEX = 17;
	static public final int BASIC_TYPE_BOOL				 = 18;
	static public final int BASIC_TYPE_BOOL_C9X		 	 = 19;
	static public final int BASIC_TYPE_WCHAR_T			 = 20;
	static public final int BASIC_TYPE_POINTER			 = 21; // not technically a basic type

	// is a type a pointer "*" type?
	public static boolean isPointerType(IType type) {
		return getStrippedType(type) instanceof IPointerType;
	}

	// is a type a reference "&" type?
	public static boolean isReferenceType(IType type) {
		return getStrippedType(type) instanceof IReferenceType;
	}

	// is a type an aggregate (composite or array) type?
	public static boolean isAggregateType(IType type) {
		return getStrippedType(type) instanceof IAggregate;
	}

	// is a type a composite (class, struct, or union) type?
	public static boolean isCompositeType(IType type) {
		return getStrippedType(type) instanceof ICompositeType;
	}

	// return the type with no typedefs, consts, or volatiles
	public static IType getStrippedType(IType type) {
		if (!(type instanceof IType))
			return null;

		if (type instanceof IForwardTypeReference)
			type = ((IForwardTypeReference) type).getReferencedType();
		
		while (type instanceof ITypedef || type instanceof IQualifierType) {
			type = ((IType) type).getType();
			
			if (type instanceof IForwardTypeReference)
				type = ((IForwardTypeReference) type).getReferencedType();
		}

		return (IType) type;
	}
	
	// return base type with no typedefs, consts, volatiles, pointer types, or array types
	public static IType getBaseType(Object type) {
		if (!(type instanceof IType))
			return null;

		if (type instanceof IForwardTypeReference)
			type = ((IForwardTypeReference) type).getReferencedType();
		
		while (type instanceof ITypedef || type instanceof IQualifierType 
				|| type instanceof IPointerType || type instanceof IArrayType) {
			type = ((IType) type).getType();
			
			if (type instanceof IForwardTypeReference)
				type = ((IForwardTypeReference) type).getReferencedType();
		}

		return (IType) type;
	}

	// return base type with no consts, volatiles, pointer types, or array types - but preserving typedefs
	public static IType getBaseTypePreservingTypedef(IType type) {
		if (!(type instanceof IType))
			return null;

		if (type instanceof IForwardTypeReference)
			type = ((IForwardTypeReference) type).getReferencedType();
		
		while (type instanceof IQualifierType 
				|| type instanceof IPointerType || type instanceof IArrayType) {
			type = ((IType) type).getType();
			
			if (type instanceof IForwardTypeReference)
				type = ((IForwardTypeReference) type).getReferencedType();
		}
		
		return (IType) type;
	}

	// shift, mask, and extend an extracted bit-field
	// NOTE: this may need to be endianness aware
	public static Number extractBitField(Number value, int byteSize, int bitSize, int bitOffset, boolean isSignedInt) {
		if (bitSize <= 0 || value == null
				|| (!(value instanceof Long) && !(value instanceof Integer) && !(value instanceof BigInteger))) {
			return value;
		}

		// TODO: Need to get default type sizes from the ITargetEnvironment
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
			int mask;
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
