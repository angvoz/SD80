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

package org.eclipse.cdt.debug.edc.x86.disassembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.disassembler.AssemblyFormatter;


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
public class AssemblyFormatterX86 extends AssemblyFormatter {

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
	
	public static String formatPrefix(int p) {
		String ret = sPrefixStrings.get(p);
		if (ret == null)
			ret = "";
		return ret;
	}

}
