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

import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.core.runtime.CoreException;

/*
 * Push a long on the instruction stack
 */
public class PushLongOrBigInteger extends SimpleInstruction {

	// if true, suffix had 'u'
	private boolean isUnsigned;
	// if true, suffix had 'l' (but not 'll')
	private boolean isLong;
	// if true, suffix had 'll' (but not 'l')
	private boolean isLongLong;
	
	// if true, the original number was a hex or octal string
	private boolean isHexOrOctal;
	
	// if not null, a big integer 
	private BigInteger fBigInteger;
	// otherwise, this is the value
	private long fLong;
	private IType type;

	/**
	 * Constructor for pushing a long on the stack
	 * 
	 * @param value
	 *            - long value
	 */
	public PushLongOrBigInteger(long value) {
		isLong = true;
		fLong = value;
	}

	/**
	 * Constructor for pushing a long on the stack
	 * 
	 * @param value
	 *            - string version of a long
	 * @throws NumberFormatException
	 */
	public PushLongOrBigInteger(String value) throws NumberFormatException {
		parseLongOrBigInteger(value);
	}

	/**
	 */
	public long getLong() {
		return fLong;
	}
	
	/**
	 * Execute pushing a long on the stack
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() {
		Number number = fBigInteger;
		if (number == null)
			number = fLong;
		
		// TODO: Handle 8-byte long and >8-byte long long

		int intSize  = fInterpreter.getTypeEngine().getTypeSize(TypeUtils.BASIC_TYPE_INT);
		int longSize = fInterpreter.getTypeEngine().getTypeSize(TypeUtils.BASIC_TYPE_LONG);
		int longLongSize = fInterpreter.getTypeEngine().getTypeSize(TypeUtils.BASIC_TYPE_LONG_LONG);

		// promote constant type based on the size of C/C++ int, long, and long long types.
		// this cannot be done at parse time because the type engine is not known then
		correctIntegerType(intSize * 8, longSize * 8, longLongSize * 8);

		if (type == null) {
			int flags = isLongLong ? ICPPBasicType.IS_LONG_LONG : isLong ?  ICPPBasicType.IS_LONG : 0;
			
			if (isUnsigned)
				flags |= ICPPBasicType.IS_UNSIGNED;
			else
				flags |= ICPPBasicType.IS_SIGNED;
	
			int size;
			if (isLongLong)
				size = longLongSize;
			else if (isLong)
				size = longSize;
			else
				size = intSize;
			
			type = fInterpreter.getTypeEngine().getBasicType(ICPPBasicType.t_int, flags, size);
		}
		
		pushNewValue(type, number);
	}

	/**
	 *	Fix type of constant based on C++ promotion rules, which depend on the size of
	 *  int, long, and long long types.
	 *
	 * @param intBitSize
	 * @param longBitSize
	 * @param longLongBitSize
	 */
	private void correctIntegerType(int intBitSize, int longBitSize, int longLongBitSize) {
		if (    fBigInteger == null
				// check if it fits in signed long (but maybe not an unsigned int)
			&& (   (fLong >= 0 && (fLong >> (intBitSize - 1)) == 0)
				|| (fLong <  0 && (fLong >> (intBitSize - 1)) < -1)))
				return;

		// at execute() time, isUnsigned means explicit unsigned, isLong means explicit long,
		// and isLongLong means explicit long long
		if (fBigInteger == null) {
			// it fits in a 64-bit signed Java long
			if (isUnsigned) {
				// explicit unsigned decimal, hex, or octal constant
				if (isLong) {
					// explicit unsigned long may change to unsigned long long
					if ((fLong >> longBitSize) > 0) {
						isLong = false;
						isLongLong = true;
					}
				} else if (!isLongLong) {
					// not declared long long - may be int, long, or long long
					if ((fLong >> (intBitSize - 1)) <= 1) {
						// fits in an unsigned int
					} else if ((fLong >> (longBitSize - 1)) <= 1) {
						// fits in an unsigned long
						isLong = true;
					} else {
						// fits in an unsigned longLong
						isLongLong = true;
					}
				}
			} else {
				// signed decimal, hex, or octal constant
				if (!isHexOrOctal) {
					// decimal constant
					if (!isLong && !isLongLong){
						// signed decimal constant, not declared long or long long
						if (   (fLong >= 0 && (fLong >> (longBitSize - 1)) == 0)
							|| (fLong <  0 && (fLong >> (longBitSize - 1)) < -1)) {
							// fits in signed long
							isLong = true;
						} else {
							// fits in signed long long
							isLongLong = true;
						}
					} else if (isLong) {
						// explicit long may change to long long
						if (!(   (fLong >= 0 && (fLong >> (longLongBitSize - 1)) == 0)
							  || (fLong <  0 && (fLong >> (longLongBitSize - 1)) < -1))) {
							isLong = false;
							isLongLong = true;
						}
					}
				} else {
					// hex or octal constant
					if (!isLong && !isLongLong){
						// signed hex or octal constant, not declared long or long long
						// may be int, unsigned int, long, unsigned long, long long, or unsigned long long
						if (fLong >> (intBitSize - 1) == 1) {
							// fits in unsigned int
							isUnsigned = true;
						} else if (   (fLong >= 0 && (fLong >> (longBitSize - 1)) == 0)
								   || (fLong <  0 && (fLong >> (longBitSize - 1)) < -1)) {
							// fits in signed long
							isLong = true;
						} else if (fLong >> (longBitSize - 1) == 1) {
							// fits in unsigned long
							isLong = true;
							isUnsigned = true; 
						} else if (   (fLong >= 0 && (fLong >> (longLongBitSize - 1)) == 0)
								   || (fLong <  0 && (fLong >> (longLongBitSize - 1)) < -1)) {
							// fits in signed long long
							isLongLong = true;
						} else {
							// fits in unsigned long long
							isLongLong = true;
							isUnsigned = true;
						}
					} else if (isLong) {
						// explicit long hex or octal may change to unsigned long, long long, or unsigned long long
						if (   (fLong >= 0 && (fLong >> (longBitSize - 1)) == 0)
							|| (fLong <  0 && (fLong >> (longBitSize - 1)) < -1)) {
							// fits in signed long
						} else if (fLong >> (longBitSize - 1) == 1) {
							// fits in unsigned long
							isUnsigned = true; 
						} else if (   (fLong >= 0 && (fLong >> (longLongBitSize - 1)) == 0)
								   || (fLong <  0 && (fLong >> (longLongBitSize - 1)) < -1)) {
							// fits in signed long long
							isLong = false;
							isLongLong = true;
						} else {
							// fits in unsigned long long
							isLong = false;
							isLongLong = true;
							isUnsigned = true;
						}
					} else {
						// explicit long long may change to unsigned long long
						if (fLong >> (longLongBitSize - 1) > 0)
							isUnsigned = true;
					}
				}
			}
		} else {
			// BigInteger, which does not fit in a Java long
			isLongLong = true;
			isLong = false;
			if (!isUnsigned) {
				// signed decimal, hex, or octal long long constant
				if (isHexOrOctal) {
					// for hex or octal constant, long long may change to unsigned long long
					if (fBigInteger.bitLength() > (longLongBitSize - 1))
						isUnsigned = true;
				}
			}
		}
	}

	/**
	 * Show a long or BigInteger value as a string
	 * 
	 * @return string version of a long or BigInteger
	 */
	@Override
	public String toString() {
		if (isLong)
			return Long.toString(fLong);
		else
			return fBigInteger.toString();
	}

	/**
	 * Convert a string to a long or BigInteger
	 * 
	 * @param value
	 *            - string version of a long
	 * @throws NumberFormatException
	 */
	public void parseLongOrBigInteger(String value) throws NumberFormatException {
		isUnsigned = false;
		isHexOrOctal = false;

		String val = value.toLowerCase();
		int length = value.length();

		if (val.endsWith("ull") || val.endsWith("llu")) { //$NON-NLS-1$ //$NON-NLS-2$
			isUnsigned = true;
			isLongLong = true;
			isLong = false;
			val = val.substring(0, val.length() - 3);
			length -= 3;
		} else if (val.endsWith("ll")) { //$NON-NLS-1$
			isLong = false;
			isLongLong = true;
			val = val.substring(0, val.length() - 2);
			length -= 2;
		} else if (val.endsWith("ul") || val.endsWith("lu")) { //$NON-NLS-1$ //$NON-NLS-2$
			isUnsigned = true;
			isLong = true;
			val = val.substring(0, val.length() - 2);
			length -= 2;
		} else if (val.endsWith("l")) { //$NON-NLS-1$
			isLong = true;
			val = val.substring(0, val.length() - 1);
			length -= 1;
		} else if (val.endsWith("u")) { //$NON-NLS-1$
			isUnsigned = true;
			val = val.substring(0, val.length() - 1);
			length -= 1;
		}

		// if conversion to BigInteger fails, the string is invalid
		// if after BigInteger conversion, Long conversion fails, the value is
		// too large or small
		if (val.startsWith("0x")) { //$NON-NLS-1$
			isHexOrOctal = true;
			try {
				fLong = Long.valueOf(val.substring(2), 16);
			} catch (NumberFormatException nfe) {
				fBigInteger = new BigInteger(val.substring(2), 16);
				fBigInteger.and(Instruction.Mask8Bytes);
				return;
			}
			length -= 2;
		} else if (length > 1 && val.startsWith("0")) { //$NON-NLS-1$
			isHexOrOctal = true;

			try {
				fLong = Long.valueOf(val.substring(1), 8);
			} catch (NumberFormatException nfe) {
				fBigInteger = new BigInteger(val.substring(1), 8);
				fBigInteger.and(Instruction.Mask8Bytes);
				return;
			}
			length -= 1;
		} else {

			try {
				fLong = Long.valueOf(val);
			} catch (NumberFormatException nfe) {
				fBigInteger = new BigInteger(val);
				fBigInteger.and(Instruction.Mask8Bytes);
				return;
			}
		}

		if (length == 0) {
			fLong = 0;
			return;
		}

		if (isUnsigned) {
			if (fBigInteger != null) {
				if (fBigInteger.bitLength() < 64) {
					// unsigned will fit in a Java long
					fLong = fBigInteger.longValue();
					fBigInteger = null;
					return;
				}
				// keep it a BigInteger
				// TODO: Allow bigger than 8 bytes
				fBigInteger.and(Instruction.Mask8Bytes);
			}
		}
	}

}
