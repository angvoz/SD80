/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.disassembler;

import java.nio.ByteBuffer;

import org.eclipse.cdt.core.IAddress;

public class AssemblyFormatter {

	private static final int ADDRESS_COLUMN_SIZE = 12;
	private static final int CODE_BYTE_COLUMN_SIZE = 32;
	protected static final int INST_NAME_COLUMN_SIZE = 10;
	public static final char OPEN = '(';
	public static final char CLOSE = ')';
	public static final char SEPARATOR = ',';
	public static final char SCALE = ',';
	public static final char PREFIX_IMMEDIATE = '$';
	public static final char PREFIX_REGISTER = '%';

	public static String toHexString(IAddress addr, boolean show0x) {
		// this gives "800000"
		String ret = Integer.toHexString(addr.getValue().intValue());
		if (show0x)
			ret = "0x" + ret;
	
		return ret;
	}

	/**
	 * get hexString representation of a byte data. <br>
	 * E.g. 0x12 => "12" or "0x12" <br>
	 * 2 = > "02" or "0x02".
	 * 
	 * @param b
	 * @param show0x
	 * @return
	 */
	public static String toHexString(byte b, boolean show0x) {
		String s = Integer.toHexString(b & 0xff);
		if (s.length() == 1)
			// padding
			s = "0" + s;
		if (show0x)
			s = "0x" + s;
	
		return s;
	}

	public static String toHexString(long i, boolean show0x) {
		String ret = Long.toHexString(i);
		if (show0x)
			ret = "0x" + ret;
	
		return ret;
	}
	public static String toHexString(int i, boolean show0x) {
		String ret = Integer.toHexString(i);
		if (show0x)
			ret = "0x" + ret;
	
		return ret;
	}

	public static String toHexString(short i, boolean show0x) {
		String ret = Integer.toHexString(i & 0xffff);
		if (show0x)
			ret = "0x" + ret;
	
		return ret;
	}

	public static String formatFarPointer(short segment, int offset) {
		return "$" + toHexString(segment, true) + ",$" + toHexString(offset, true);
	}

	/**
	 * Format address for displaying in format column.
	 */
	public static String formatForAddressColumn(IAddress addr) {
		String addrStr = toHexString(addr, false);
	
		// Padding space at the end.
		StringBuffer buf = new StringBuffer(addrStr).append(':');
		for (int i = 0; i < ADDRESS_COLUMN_SIZE - addrStr.length(); i++)
			buf.append(' ');
	
		return buf.toString();
	}

	/**
	 * Format bytes for displaying in byte column.
	 * 
	 * @param codeBuffer
	 *            - code byte buffer.
	 * @param startPosition
	 *            - position of the first byte in the code buffer.
	 * @param length
	 *            - number of bytes to display.
	 * @return
	 */
	public static String formatForByteColumn(ByteBuffer codeBuffer, int startPosition,
			int length) {
				StringBuffer tmp = new StringBuffer();
			
				codeBuffer.position(startPosition);
				for (int i = 0; i < length; i++) {
					byte b = codeBuffer.get();
					tmp.append(toHexString(b, false)).append(' ');
				}
			
				int cnt = tmp.length();
			
				// padding
				for (int i = 0; i < CODE_BYTE_COLUMN_SIZE - cnt; i++)
					tmp.append(' ');
			
				return tmp.toString();
			}

	public static String formatForCode(IAddress addr) {
		return toHexString(addr, true);
	}

	public static String formatImmediate(long imm) {
		/* Not signed. */
		return PREFIX_IMMEDIATE + toHexString(imm, true);
	}

	public static String formatImmediate(int imm) {
		/* Not signed. */
		return PREFIX_IMMEDIATE + toHexString(imm, true);
	}
	
	public static String formatDisplacement(int displacement) {
		if (displacement < 0)
			return "-" + toHexString(-displacement, true); // negative hex
		else
			return toHexString(displacement, true); // signed hex
	}

	public static String formatOffset(int offset) {
		// Offset is never negative.
		return toHexString(offset, true); // signed hex
	}

	public static String formatRegister(String regName, boolean fIndirectAddressing) {
		String ret = "";
		if (fIndirectAddressing)
			ret += OPEN;
	
		ret += PREFIX_REGISTER + regName;
	
		if (fIndirectAddressing)
			ret += CLOSE;
	
		return ret;
	}

	public static String instructionNameSizeSuffix(int size) {
		return size == 8 ? "b" : (size == 16 ? "w" : "l");
	}

	public AssemblyFormatter() {
		super();
	}

}