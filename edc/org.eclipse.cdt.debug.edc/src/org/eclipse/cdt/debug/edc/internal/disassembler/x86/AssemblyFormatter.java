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

package org.eclipse.cdt.debug.edc.internal.disassembler.x86;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;

/**
 * Class that handles format of assembly code for display. <br>
 * <p>
 * Currently We just support AT&T syntax, though it should be easy to support
 * Intel syntax with this class abstraction.
 * </p>
 * 
 * @see Refer http://en.wikipedia.org/wiki/X86_assembly_language
 * 
 */
@SuppressWarnings("serial")
public class AssemblyFormatter {

	private static final int ADDRESS_COLUMN_SIZE = 12, CODE_BYTE_COLUMN_SIZE = 32, INST_NAME_COLUMN_SIZE = 10;

	public static final char OPEN = '(';
	public static final char CLOSE = ')';
	public static final char SEPARATOR = ',';
	public static final char SCALE = ',';
	public static final char PREFIX_IMMEDIATE = '$';
	public static final char PREFIX_REGISTER = '%';

	/**
	 * Register names for different addressing size. Note we use AT&T syntax.
	 */
	public static Map<Integer, String[]> sGPRNames = new HashMap<Integer, String[]>() {
		{
			put(8, new String[] { "%al", "%cl", "%dl", "%bl", "%ah", "%ch", "%dh", "%bh" });

			put(16, new String[] { "%ax", "%cx", "%dx", "%bx", "%sp", "%bp", "%si", "%di", "%r8w", "%r9w", "%r10w",
					"%r11w", "%r12w", "%r13w", "%r14w", "%r15w" });

			put(32, new String[] { "%eax", "%ecx", "%edx", "%ebx", "%esp", "%ebp", "%esi", "%edi", "%r8d", "%r9d",
					"%r10d", "%r11d", "%r12d", "%r13d", "%r14d", "%r15d" });

			put(64, new String[] { "%rax", "%rcx", "%rdx", "%rbx", "%rsp", "%rbp", "%rsi", "%rdi", "%r8", "%r9",
					"%r10", "%r11", "%r12", "%r13", "%r14", "%r15" });
		}
	};

	/**
	 * Segment register names.
	 */
	public static String[] sSegmentRegisterNames = { "%es", "%cs", "%ss", "%ds", "%fs", "%gs" };

	/**
	 * Control register names.
	 */
	public static String[] sControlRegisterNames = { "%cr0", "%cr1", "%cr2", "%cr3", "%cr4" };

	/**
	 * Debug register names.
	 */
	public static String[] sDebugRegisterNames = { "%db0", "%db1", "%db2", "%db3", "%db4", "%db5", "%db6", "%db7" };

	/**
	 * Prefix string that's used in assembly mnemonics.
	 */
	public static Map<Integer, String> sPrefixStrings = new HashMap<Integer, String>() {
		{
			put(OpcodeX86.PREFIX_LOCK, "lock");
			put(OpcodeX86.PREFIX_REPZ, "rep"); // change to "repz" on the fly.
			put(OpcodeX86.PREFIX_REPNZ, "repnz");
			put(OpcodeX86.PREFIX_SEGOVR_CS, "%cs");
			put(OpcodeX86.PREFIX_SEGOVR_SS, "%ss");
			put(OpcodeX86.PREFIX_SEGOVR_DS, "%ds");
			put(OpcodeX86.PREFIX_SEGOVR_ES, "%es");
			put(OpcodeX86.PREFIX_SEGOVR_FS, "%gs");
			put(OpcodeX86.PREFIX_SEGOVR_GS, "%gs");
		}
	};

	/**
	 * Instructions whose operands should not be reversed in AT&T Syntax.
	 * Element are added to the list in the instruction definition in OpcodeX86.
	 * This is a very small list.
	 */
	public static ArrayList<String> sInstructionsNotReverseOperand = new ArrayList<String>();

	/**
	 * Instructions whose name's size suffix is determined by a register
	 * operand.<br>
	 * See comment at where the list is accessed for more.<br>
	 * Element are added to the list in the instruction definition in OpcodeX86.<br>
	 * This is a very small list.
	 */
	public static ArrayList<String> sInstructionsSuffixFromRegisterOperand = new ArrayList<String>();

	/**
	 * Table of special instructions that have only implicit operands but we
	 * want to display the operands in assembly output. The map is keyed on
	 * normal instruction name, while value is the predefined format for
	 * implicit operand.
	 */
	private static final Map<String, String> sSpecialFormInstructions = new HashMap<String, String>() {
		{
			put("xlat", "xlat   %ds:(%ebx)");
		}
	};

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
	public static String formatForByteColumn(ByteBuffer codeBuffer, int startPosition, int length) {
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

	public static String formatPrefix(int p) {
		String ret = sPrefixStrings.get(p);
		if (ret == null)
			ret = "";
		return ret;
	}

	/**
	 * Format memory addressing. <br>
	 * AT&T syntax: <i>*disp_as_signed_hex(baseRegName,indexRegName,scale) </i>
	 * 
	 * @param indirectAddressing
	 *            - is indirect addressing ?
	 * @param displacement
	 *            null means it does not matter.
	 * @param baseRegID
	 *            -1 means it does not matter.
	 * @param indexRegID
	 *            -1 means it does not matter.
	 * @param scale
	 *            "scale" bits of SIB byte, matters only when "indexRegID"
	 *            matters.
	 * @return
	 */
	public static String format(boolean indirectAddressing, Integer displacement, int baseRegID, int indexRegID,
			int scale) {
		StringBuffer sb = new StringBuffer();

		if (indirectAddressing)
			sb.append("*");

		if (displacement != null)
			sb.append(formatDisplacement(displacement));

		if (baseRegID != -1 || indexRegID != -1)
			sb.append(OPEN);

		if (baseRegID != -1)
			sb.append(sGPRNames.get(32)[baseRegID]);

		if (indexRegID != -1) {
			sb.append(SEPARATOR);
			sb.append(sGPRNames.get(32)[indexRegID]);

			if (scale >= 0) { // display it even if it's 0 (meaning x1)
				sb.append(SCALE);
				sb.append(1 << scale);
			}
		}

		if (baseRegID != -1 || indexRegID != -1)
			sb.append(CLOSE);

		return sb.toString();
	}

	/**
	 * Format the whole instruction with given name and operand list.
	 * 
	 * @param prefixes
	 * @param instructionName
	 * @param instructionNameSuffix
	 *            - can be null.
	 * @param operands
	 *            - string representation of all operands
	 * 
	 * @return
	 */
	public static String formatInstruction(List<Integer> prefixes, String instructionName,
			String instructionNameSuffix, String[] operands) {
		int i;

		if (instructionName == null)
			throw new IllegalArgumentException();

		StringBuffer sb = new StringBuffer();

		// ----------------- Format prefixes
		// ------------------------------------
		//
		for (int p : prefixes) {
			// Only such prefix should be here.
			assert OpcodeX86.sPrefixesForInstruction.contains(p);

			String pstr = formatPrefix(p);

			if (p == OpcodeX86.PREFIX_REPZ) {
				// Special adjustment
				if (instructionName.startsWith("cmps"))
					pstr = "repz";
			}

			sb.append(pstr).append(" ");
		}

		// ----------------- Format instruction names
		// --------------------------------
		//
		if (sSpecialFormInstructions.containsKey(instructionName)) {
			// use special predefined format
			assert operands == null;
			sb.append(sSpecialFormInstructions.get(instructionName));
		} else
			sb.append(instructionName);

		if (instructionName.endsWith(OpcodeX86.SIZE_FLAG_TARGET)
				|| instructionName.endsWith(OpcodeX86.SIZE_FLAG_SOURCE)) {
			// Size suffix required.
			// Replace the indicator with correct suffix.
			// assert instructionNameSuffix != null :
			// "size suffix required but not available.";
			sb.deleteCharAt(sb.length() - 1);
			if (instructionNameSuffix != null)
				sb.append(instructionNameSuffix);
		}

		// ----------------- Format operands
		// ------------------------------------
		//
		if (operands != null) {
			for (i = 0; i < INST_NAME_COLUMN_SIZE - instructionName.length(); i++)
				sb.append(' ');

			// AT&T Syntax, reverse operands unless otherwise indicated.
			boolean not_reverse = sInstructionsNotReverseOperand.contains(instructionName);

			if (not_reverse) {
				for (i = 0; i < operands.length; i++) {
					sb.append(operands[i]);

					if (i + 1 < operands.length)
						sb.append(SEPARATOR);
				}
			} else {
				for (i = operands.length - 1; i >= 0; i--) {
					sb.append(operands[i]);

					if (i > 0)
						sb.append(SEPARATOR);
				}
			}
		}

		return sb.toString();
	}

	/**
	 * For "Y" addressing method.
	 * 
	 * @param addressMode
	 */
	public static String registerPair_ESrDI(int addressMode) {
		if (addressMode == InstructionParserX86.ADDRESS_MODE_16BIT)
			return "%es:(%di)";
		else
			return "%es:(%edi)";
	}

	/**
	 * For "X" addressing method.
	 * 
	 * @param addressMode
	 */
	public static String registerPair_DSrSI(int addressMode) {
		if (addressMode == InstructionParserX86.ADDRESS_MODE_16BIT)
			return "%ds:(%si)";
		else
			return "%ds:(%esi)";
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
}
