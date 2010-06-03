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
		
		if (type == null) {
			int flags = isLongLong ? ICPPBasicType.IS_LONG_LONG : isLong ?  ICPPBasicType.IS_LONG : 0;
			
			if (isUnsigned)
				flags |= ICPPBasicType.IS_UNSIGNED;
			else
				flags |= ICPPBasicType.IS_SIGNED;
	
			int size;
			if (isLongLong)
				size = fInterpreter.getTypeEngine().getTypeSize(TypeUtils.BASIC_TYPE_LONG_LONG);
			else if (isLong)
				size = fInterpreter.getTypeEngine().getTypeSize(TypeUtils.BASIC_TYPE_LONG);
			else
				size = fInterpreter.getTypeEngine().getTypeSize(TypeUtils.BASIC_TYPE_INT);
			
			type = fInterpreter.getTypeEngine().getBasicType(ICPPBasicType.t_int, flags, size);
		}
		
		pushNewValue(type, number);
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
			try {
				fLong = Long.valueOf(val.substring(2), 16);
			} catch (NumberFormatException nfe) {
				fBigInteger = new BigInteger(val.substring(2), 16);
				fBigInteger.and(Instruction.Mask8Bytes);
				return;
			}
			length -= 2;
		} else if (length > 1 && val.startsWith("0")) { //$NON-NLS-1$

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
