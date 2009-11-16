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

import org.eclipse.core.runtime.CoreException;

/*
 * Push a long on the instruction stack
 */
public class PushLongOrBigInteger extends SimpleInstruction {

	private boolean isLong;
	private long fLong;
	private BigInteger fBigInteger;

	/**
	 * Constructor for pushing a long on the stack
	 * 
	 * @param value
	 *            - long value
	 */
	public PushLongOrBigInteger(long value) {
		fLong = value;
		isLong = true;
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
	 * Execute pushing a long on the stack
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() {
		if (isLong)
			pushNewValue(fLong);
		else
			pushNewValue(fBigInteger);
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
	 * @return string converted to a long, or 0 if it cannot be converted
	 * @throws NumberFormatException
	 */
	public void parseLongOrBigInteger(String value) throws NumberFormatException {
		boolean suffixUnsigned = false;

		String val = value.toLowerCase();
		int length = value.length();

		if (val.endsWith("ull") || val.endsWith("llu")) { //$NON-NLS-1$ //$NON-NLS-2$
			suffixUnsigned = true;
			val = val.substring(0, val.length() - 3);
			length -= 3;
		} else if (val.endsWith("ll")) { //$NON-NLS-1$
			val = val.substring(0, val.length() - 2);
			length -= 2;
		} else if (val.endsWith("ul") || val.endsWith("lu")) { //$NON-NLS-1$ //$NON-NLS-2$
			suffixUnsigned = true;
			val = val.substring(0, val.length() - 2);
			length -= 2;
		} else if (val.endsWith("l")) { //$NON-NLS-1$
			val = val.substring(0, val.length() - 1);
			length -= 1;
		} else if (val.endsWith("u")) { //$NON-NLS-1$
			suffixUnsigned = true;
			val = val.substring(0, val.length() - 1);
			length -= 1;
		}

		// if conversion to BigInteger fails, the string is invalid
		// if after BigInteger conversion, Long conversion fails, the value is
		// too large or small
		if (val.startsWith("0x")) { //$NON-NLS-1$
			fBigInteger = new BigInteger(val.substring(2), 16);

			try {
				fLong = Long.valueOf(val.substring(2), 16);
			} catch (NumberFormatException nfe) {
				fBigInteger.and(Instruction.Mask8Bytes);
				return;
			}
			length -= 2;
		} else if (length > 1 && val.startsWith("0")) { //$NON-NLS-1$
			fBigInteger = new BigInteger(val.substring(1), 8);

			try {
				fLong = Long.valueOf(val.substring(1), 8);
			} catch (NumberFormatException nfe) {
				fBigInteger.and(Instruction.Mask8Bytes);
				return;
			}
			length -= 1;
		} else {
			fBigInteger = new BigInteger(val);

			try {
				fLong = Long.valueOf(val);
			} catch (NumberFormatException nfe) {
				fBigInteger.and(Instruction.Mask8Bytes);
				return;
			}
		}

		if (length == 0) {
			fLong = 0;
			return;
		}

		if (suffixUnsigned) {
			if (fBigInteger.bitLength() < 64) {
				// unsigned will fit in a Java long
				fLong = fBigInteger.longValue();
				return;
			}

			// keep it a BigInteger
			// TODO: Allow bigger than 8 bytes
			fBigInteger.and(Instruction.Mask8Bytes);
			return;
		}
	}

}
