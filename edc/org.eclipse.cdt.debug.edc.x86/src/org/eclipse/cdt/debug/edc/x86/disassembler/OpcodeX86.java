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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.x86.disassembler.InstructionParserX86.ModRM;

/**
 * Opcode map of x86, and some constants.
 * 
 */
@SuppressWarnings("serial")
public class OpcodeX86 {
	private String name;
	private boolean needModRM;
	private String[] operandDescriptors;

	public OpcodeX86(String name, boolean needModRM, String[] operandDescriptors) {
		this.needModRM = needModRM;
		this.name = name;
		this.operandDescriptors = operandDescriptors;
	}

	public String getName() {
		return name;
	}

	public boolean needModRM() {
		return needModRM;
	}

	public String[] getOperandDescriptors() {
		return operandDescriptors;
	}

	@Override
	public String toString() {
		return name + " " + Arrays.toString(operandDescriptors) + "\nNeed ModRM : " + needModRM;
	}

	/*
	 * ======================================================================
	 * Following are static constants and maps.
	 * ======================================================================
	 */

	/**
	 * This is my special "extension" to operand type encoding. An operand type
	 * string like "*Ev" means the operand type is "Ev" and is address of a
	 * pointer.
	 */
	public static final char INDIRECT_INDICATOR = '*';

	/**
	 * My indicator at end of a instruction name indicating requirement of
	 * "size" suffix like b/w/l to the instruction name. "TARGET" means the size
	 * is derived from target operand (the first operand in Intel syntax).
	 */
	public static final String SIZE_FLAG_TARGET = "T";
	/**
	 * My indicator at end of a instruction name indicating requirement of
	 * "size" suffix like b/w/l to the instruction name. "SOURCE" means the size
	 * is derived from source operand (the second operand in Intel syntax).
	 */
	public static final String SIZE_FLAG_SOURCE = "S";

	// prefixes
	//
	static final int
	// Group 1
			PREFIX_LOCK = 0xF0,
			PREFIX_REPNZ = 0xF2, // aka REPNE
			PREFIX_REPZ = 0xF3, // aka REPE

			// Group 2
			// Segment override
			PREFIX_SEGOVR_CS = 0x2E, PREFIX_SEGOVR_SS = 0x36, PREFIX_SEGOVR_DS = 0x3E,
			PREFIX_SEGOVR_ES = 0x26,
			PREFIX_SEGOVR_FS = 0x64, PREFIX_SEGOVR_GS = 0x65,
			// Branch hint
			PREFIX_BRANCH_NOT_TAKEN = 0x2E, PREFIX_BRANCH_TAKEN = 0x3E,

			// Group 3
			PREFIX_OPERAND_SIZE_OVERRIDE = 0x66,

			// Group 4
			PREFIX_ADDRESS_SIZE_OVERRIDE = 0x67;

	/**
	 * All prefixes except REX ones.
	 */
	static List<Integer> sAllPrefixes = new ArrayList<Integer>() {
		{
			add(PREFIX_LOCK);
			add(PREFIX_REPNZ);
			add(PREFIX_REPZ);

			// Group 2
			// Segment override
			add(PREFIX_SEGOVR_CS);
			add(PREFIX_SEGOVR_SS);
			add(PREFIX_SEGOVR_DS);
			add(PREFIX_SEGOVR_ES);
			add(PREFIX_SEGOVR_FS);
			add(PREFIX_SEGOVR_GS);
			// Branch hint
			add(PREFIX_BRANCH_NOT_TAKEN);
			add(PREFIX_BRANCH_TAKEN);

			// Group 3
			add(PREFIX_OPERAND_SIZE_OVERRIDE);

			// Group 4
			add(PREFIX_ADDRESS_SIZE_OVERRIDE);
		}
	};

	/**
	 * prefixes that apply to operand. (vs.
	 * {@link OpcodeX86#sPrefixesForInstruction}).
	 */
	static List<Integer> sPrefixesForOperand = new ArrayList<Integer>() {
		{
			add(PREFIX_SEGOVR_CS);
			add(PREFIX_SEGOVR_SS);
			add(PREFIX_SEGOVR_DS);
			add(PREFIX_SEGOVR_ES);
			add(PREFIX_SEGOVR_FS);
			add(PREFIX_SEGOVR_GS);
		}
	};

	/**
	 * prefixes that apply to instruction. (vs.
	 * {@link OpcodeX86#sPrefixesForOperand}).
	 */
	static List<Integer> sPrefixesForInstruction = new ArrayList<Integer>() {
		{
			add(PREFIX_LOCK);
			add(PREFIX_REPNZ);
			add(PREFIX_REPZ);
		}
	};

	// Operand descriptors
	// Note:
	// All letters below are from Intel manual.
	// Only the lower-case "i" below is added by me to indicate "indirect".
	//
	static final String[] OD_AL_iDX = { "AL", "*DX" }, OD_AL_Ib = { "AL", "Ib" }, OD_AL_Ob = { "AL", "Ob" },
			OD_AL_Xb = { "AL", "Xb" }, OD_AL_Yb = { "AL", "Yb" }, OD_Ap = { "Ap" }, OD_AX = { "AX" }, OD_Cd_Rd = {
					"Cd", "Rd" }, OD_CS = { "CS" }, OD_Dd_Rd = { "Dd", "Rd" }, OD_DS = { "DS" }, OD_iDX_AL = { "*DX",
					"AL" }, OD_iDX_eAX = { "*DX", "eAX" }, OD_iDX_Xb = { INDIRECT_INDICATOR + "DX", "Xb" },
			OD_iDX_Xz = { INDIRECT_INDICATOR + "DX", "Xz" }, OD_iEp = { INDIRECT_INDICATOR + "Ep" },
			OD_iEv = { INDIRECT_INDICATOR + "Ev" }, OD_Eb_CL = { "Eb", "CL" }, OD_Eb_Gb = { "Eb", "Gb" }, OD_Eb_Ib = {
					"Eb", "Ib" }, OD_Eb = { "Eb" }, OD_ES = { "ES" }, OD_Ev = { "Ev" }, OD_Ew = { "Ew" }, OD_Ev_CL = {
					"Ev", "CL" }, OD_Ev_Gv = { "Ev", "Gv" }, OD_Ev_Gv_Ib = { "Ev", "Gv", "Ib" }, OD_Ev_Gv_CL = { "Ev",
					"Gv", "CL" }, OD_Ev_Ib = { "Ev", "Ib" }, OD_Ev_Iz = { "Ev", "Iz" }, OD_Ev_Sw = { "Ev", "Sw" },
			OD_Ew_Gw = { "Ew", "Gw" }, OD_eAX = { "eAX" }, OD_eAX_iDX = { "eAX", "*DX" }, OD_eAX_Ib = { "eAX", "Ib" },
			OD_eCX = { "eCX" }, OD_eDX = { "eDX" }, OD_eBX = { "eBX" }, OD_eSP = { "eSP" },
			OD_eBP = { "eBP" },
			OD_eSI = { "eSI" },
			OD_eDI = { "eDI" },
			OD_FS = { "FS" },
			OD_Gb_Eb = { "Gb", "Eb" },
			OD_GS = { "GS" },
			OD_Gv_Eb = { "Gv", "Eb" },
			OD_Gv_Ev = { "Gv", "Ev" },
			OD_Gv_Ev_Ib = { "Gv", "Ev", "Ib" },
			OD_Gv_Ev_Iz = { "Gv", "Ev", "Iz" },
			OD_Gv_Ew = { "Gv", "Ew" },
			OD_Gv_M = { "Gv", "M" },
			OD_Gv_Ma = OD_Gv_M, // "a" does not affect disassembler.
			OD_Gv_Mp = { "Gv", "Mp" },
			OD_Gv_Mv = { "Gv", "Mv" },
			OD_Gz_Mp = { "Gz", "Mp" },
			OD_Ib = { "Ib" },
			OD_Ib_AL = { "Ib", "AL" },
			OD_Ib_eAX = { "Ib", "eAX" },
			OD_Iw = { "Iw" },
			OD_Iw_Ib = { "Iw", "Ib" },
			OD_Iz = { "Iz" },
			OD_Jb = { "Jb" },
			OD_Jz = { "Jz" },
			OD_Ms = { "Ms" },
			OD_Mv_Gv = { "Mv", "Gv" },
			OD_m14byte = { "m14byte" }, // my special
			OD_Ob_AL = { "Ob", "AL" },
			OD_Ov_rAX = { "Ov", "rAX" },
			OD_Rd_Cd = { "Rd", "Cd" },
			OD_Rd_Dd = { "Rd", "Dd" },
			OD_RvMw = OD_Ev, // Rv/Mw == Ev ?
			OD_rAX = { "rAX" }, OD_rAX_Iz = { "rAX", "Iz" }, OD_rAX_rAX = { "rAX", "rAX" },
			OD_rAX_Ov = { "rAX", "Ov" }, OD_rAX_Xv = { "rAX", "Xv" }, OD_rCX = { "rCX" },
			OD_rCX_rAX = { "rCX", "rAX" }, OD_rDX = { "rDX" }, OD_rBX = { "rBX" }, OD_rSP = { "rSP" },
			OD_rBP = { "rBP" }, OD_rSI = { "rSI" }, OD_rDI = { "rDI" }, OD_rDX_rAX = { "rDX", "rAX" }, OD_rBX_rAX = {
					"rBX", "rAX" }, OD_rSP_rAX = { "rSP", "rAX" }, OD_rBP_rAX = { "rBP", "rAX" }, OD_rSI_rAX = { "rSI",
					"rAX" }, OD_rDI_rAX = { "rDI", "rAX" }, OD_ST0 = { "ST(0)" }, OD_ST1 = { "ST(1)" },
			OD_ST2 = { "ST(2)" }, OD_ST3 = { "ST(3)" }, OD_ST4 = { "ST(4)" }, OD_ST5 = { "ST(5)" },
			OD_ST6 = { "ST(6)" }, OD_ST7 = { "ST(7)" }, OD_ST_ST0 = { "ST", "ST(0)" }, OD_ST_ST1 = { "ST", "ST(1)" },
			OD_ST_ST2 = { "ST", "ST(2)" }, OD_ST_ST3 = { "ST", "ST(3)" }, OD_ST_ST4 = { "ST", "ST(4)" }, OD_ST_ST5 = {
					"ST", "ST(5)" }, OD_ST_ST6 = { "ST", "ST(6)" }, OD_ST_ST7 = { "ST", "ST(7)" }, OD_ST0_ST = {
					"ST(0)", "ST" }, OD_ST1_ST = { "ST(1)", "ST" }, OD_ST2_ST = { "ST(2)", "ST" }, OD_ST3_ST = {
					"ST(3)", "ST" }, OD_ST4_ST = { "ST(4)", "ST" }, OD_ST5_ST = { "ST(5)", "ST" }, OD_ST6_ST = {
					"ST(6)", "ST" }, OD_ST7_ST = { "ST(7)", "ST" }, OD_SS = { "SS" }, OD_Sw_Ew = { "Sw", "Ew" },
			OD_Vss_Wss = { "Vss", "Wss" }, OD_Wss_Vss = { "Wss", "Vss" }, OD_Xb_Yb = { "Xb", "Yb" }, OD_Xv_Yv = { "Xv",
					"Yv" }, OD_Yb_iDX = { "Yb", "*DX" }, // indirect DX
			OD_Yb_AL = { "Yb", "AL" }, OD_Yv_rAX = { "Yv", "rAX" }, OD_Yz_iDX = { "Yz", "*DX" }, OD_NO_OPERAND = null;

	/**
	 * Representation of invalid opcode.
	 */
	public static final OpcodeX86 sInvalidOpcode = new OpcodeX86("<Invalid>", false, OD_NO_OPERAND);

	/**
	 * Just to indicate more info is needed to determine the opcode.
	 */
	public static final OpcodeX86 sVaringOpcode = new OpcodeX86("<Varing>", false, OD_NO_OPERAND);

	/**
	 * One-byte opcode map
	 */
	public static Map<Integer, OpcodeX86> sOpcodeMap_OneByte = new HashMap<Integer, OpcodeX86>() {
		{
			put(0x00, new OpcodeX86("add", true, OD_Eb_Gb));
			put(0x01, new OpcodeX86("add", true, OD_Ev_Gv));
			put(0x02, new OpcodeX86("add", true, OD_Gb_Eb));
			put(0x03, new OpcodeX86("add", true, OD_Gv_Ev));
			put(0x04, new OpcodeX86("add", false, OD_AL_Ib));
			put(0x05, new OpcodeX86("add", false, OD_rAX_Iz));
			put(0x06, new OpcodeX86("push", false, OD_ES));
			put(0x07, new OpcodeX86("pop", false, OD_ES));
			put(0x08, new OpcodeX86("or", true, OD_Eb_Gb));
			put(0x09, new OpcodeX86("or", true, OD_Ev_Gv));
			put(0x0a, new OpcodeX86("or", true, OD_Gb_Eb));
			put(0x0b, new OpcodeX86("or", true, OD_Gv_Ev));
			put(0x0c, new OpcodeX86("or", false, OD_AL_Ib));
			put(0x0d, new OpcodeX86("or", false, OD_rAX_Iz));
			put(0x0e, new OpcodeX86("push", false, OD_CS));
			// 0x0f: two-byte escape

			put(0x10, new OpcodeX86("adc", true, OD_Eb_Gb));
			put(0x11, new OpcodeX86("adc", true, OD_Ev_Gv));
			put(0x12, new OpcodeX86("adc", true, OD_Gb_Eb));
			put(0x13, new OpcodeX86("adc", true, OD_Gv_Ev));
			put(0x14, new OpcodeX86("adc", false, OD_AL_Ib));
			put(0x15, new OpcodeX86("adc", false, OD_rAX_Iz));
			put(0x16, new OpcodeX86("push", false, OD_SS));
			put(0x17, new OpcodeX86("pop", false, OD_SS));
			put(0x18, new OpcodeX86("sbb", true, OD_Eb_Gb));
			put(0x19, new OpcodeX86("sbb", true, OD_Ev_Gv));
			put(0x1a, new OpcodeX86("sbb", true, OD_Gb_Eb));
			put(0x1b, new OpcodeX86("sbb", true, OD_Gv_Ev));
			put(0x1c, new OpcodeX86("sbb", false, OD_AL_Ib));
			put(0x1d, new OpcodeX86("sbb", false, OD_rAX_Iz));
			put(0x1e, new OpcodeX86("push", false, OD_DS));
			put(0x1f, new OpcodeX86("pop", false, OD_DS));

			put(0x20, new OpcodeX86("and", true, OD_Eb_Gb));
			put(0x21, new OpcodeX86("and", true, OD_Ev_Gv));
			put(0x22, new OpcodeX86("and", true, OD_Gb_Eb));
			put(0x23, new OpcodeX86("and", true, OD_Gv_Ev));
			put(0x24, new OpcodeX86("and", false, OD_AL_Ib));
			put(0x25, new OpcodeX86("and", false, OD_rAX_Iz));
			// 0x26: prefix: seg=ES
			put(0x27, new OpcodeX86("daa", false, OD_NO_OPERAND));
			put(0x28, new OpcodeX86("sub", true, OD_Eb_Gb));
			put(0x29, new OpcodeX86("sub", true, OD_Ev_Gv));
			put(0x2a, new OpcodeX86("sub", true, OD_Gb_Eb));
			put(0x2b, new OpcodeX86("sub", true, OD_Gv_Ev));
			put(0x2c, new OpcodeX86("sub", false, OD_AL_Ib));
			put(0x2d, new OpcodeX86("sub", false, OD_rAX_Iz));
			// 0x2e: prefix: seg=CS
			put(0x2f, new OpcodeX86("das", false, OD_NO_OPERAND));

			put(0x30, new OpcodeX86("xor", true, OD_Eb_Gb));
			put(0x31, new OpcodeX86("xor", true, OD_Ev_Gv));
			put(0x32, new OpcodeX86("xor", true, OD_Gb_Eb));
			put(0x33, new OpcodeX86("xor", true, OD_Gv_Ev));
			put(0x34, new OpcodeX86("xor", false, OD_AL_Ib));
			put(0x35, new OpcodeX86("xor", false, OD_rAX_Iz));
			// 0x36: prefix: seg=SS
			put(0x37, new OpcodeX86("aaa", false, null));
			put(0x38, new OpcodeX86("cmp", true, OD_Eb_Gb));
			put(0x39, new OpcodeX86("cmp", true, OD_Ev_Gv));
			put(0x3a, new OpcodeX86("cmp", true, OD_Gb_Eb));
			put(0x3b, new OpcodeX86("cmp", true, OD_Gv_Ev));
			put(0x3c, new OpcodeX86("cmp", false, OD_AL_Ib));
			put(0x3d, new OpcodeX86("cmp", false, OD_rAX_Iz));
			// 0x3e: prefix: seg=DS
			put(0x3f, new OpcodeX86("aas", false, OD_NO_OPERAND));

			put(0x40, new OpcodeX86("inc", false, OD_eAX));
			put(0x41, new OpcodeX86("inc", false, OD_eCX));
			put(0x42, new OpcodeX86("inc", false, OD_eDX));
			put(0x43, new OpcodeX86("inc", false, OD_eBX));
			put(0x44, new OpcodeX86("inc", false, OD_eSP));
			put(0x45, new OpcodeX86("inc", false, OD_eBP));
			put(0x46, new OpcodeX86("inc", false, OD_eSI));
			put(0x47, new OpcodeX86("inc", false, OD_eDI));
			put(0x48, new OpcodeX86("dec", false, OD_eAX));
			put(0x49, new OpcodeX86("dec", false, OD_eCX));
			put(0x4a, new OpcodeX86("dec", false, OD_eDX));
			put(0x4b, new OpcodeX86("dec", false, OD_eBX));
			put(0x4c, new OpcodeX86("dec", false, OD_eSP));
			put(0x4d, new OpcodeX86("dec", false, OD_eBP));
			put(0x4e, new OpcodeX86("dec", false, OD_eSI));
			put(0x4f, new OpcodeX86("dec", false, OD_eDI));

			put(0x50, new OpcodeX86("push", false, OD_rAX));
			put(0x51, new OpcodeX86("push", false, OD_rCX));
			put(0x52, new OpcodeX86("push", false, OD_rDX));
			put(0x53, new OpcodeX86("push", false, OD_rBX));
			put(0x54, new OpcodeX86("push", false, OD_rSP));
			put(0x55, new OpcodeX86("push", false, OD_rBP));
			put(0x56, new OpcodeX86("push", false, OD_rSI));
			put(0x57, new OpcodeX86("push", false, OD_rDI));
			put(0x58, new OpcodeX86("pop", false, OD_rAX));
			put(0x59, new OpcodeX86("pop", false, OD_rCX));
			put(0x5a, new OpcodeX86("pop", false, OD_rDX));
			put(0x5b, new OpcodeX86("pop", false, OD_rBX));
			put(0x5c, new OpcodeX86("pop", false, OD_rSP));
			put(0x5d, new OpcodeX86("pop", false, OD_rBP));
			put(0x5e, new OpcodeX86("pop", false, OD_rSI));
			put(0x5f, new OpcodeX86("pop", false, OD_rDI));

			put(0x60, new OpcodeX86("pusha", false, OD_NO_OPERAND));
			put(0x61, new OpcodeX86("popa", false, OD_NO_OPERAND));
			put(0x62, new OpcodeX86("bound", true, OD_Gv_Ma));
			AssemblyFormatterX86.sInstructionsNotReverseOperand.add("bound");

			put(0x63, new OpcodeX86("arpl", true, OD_Ew_Gw));
			// 0x64: prefix: seg=FS
			// 0x65: prefix: seg=GS
			// 0x66: prefix: operand size
			// 0x67: prefix: address size
			put(0x68, new OpcodeX86("push", false, OD_Iz));
			put(0x69, new OpcodeX86("imul", true, OD_Gv_Ev_Iz));
			put(0x6a, new OpcodeX86("push", false, OD_Ib));
			put(0x6b, new OpcodeX86("imul", true, OD_Gv_Ev_Ib));
			put(0x6c, new OpcodeX86("ins" + SIZE_FLAG_TARGET, false, OD_Yb_iDX));
			put(0x6d, new OpcodeX86("ins" + SIZE_FLAG_TARGET, false, OD_Yz_iDX));
			put(0x6e, new OpcodeX86("outs" + SIZE_FLAG_SOURCE, false, OD_iDX_Xb));
			put(0x6f, new OpcodeX86("outs" + SIZE_FLAG_SOURCE, false, OD_iDX_Xz));

			put(0x70, new OpcodeX86("jo", false, OD_Jb));
			put(0x71, new OpcodeX86("jno", false, OD_Jb));
			put(0x72, new OpcodeX86("jb", false, OD_Jb));
			put(0x73, new OpcodeX86("jae", false, OD_Jb));
			put(0x74, new OpcodeX86("je", false, OD_Jb));
			put(0x75, new OpcodeX86("jne", false, OD_Jb));
			put(0x76, new OpcodeX86("jbe", false, OD_Jb));
			put(0x77, new OpcodeX86("ja", false, OD_Jb));
			put(0x78, new OpcodeX86("js", false, OD_Jb));
			put(0x79, new OpcodeX86("jns", false, OD_Jb));
			put(0x7a, new OpcodeX86("jp", false, OD_Jb));
			put(0x7b, new OpcodeX86("jnp", false, OD_Jb));
			put(0x7c, new OpcodeX86("jl", false, OD_Jb));
			put(0x7d, new OpcodeX86("jge", false, OD_Jb));
			put(0x7e, new OpcodeX86("jle", false, OD_Jb));
			put(0x7f, new OpcodeX86("jg", false, OD_Jb));

			// 0x80-0x83: opcode extension group 1
			put(0x84, new OpcodeX86("test", true, OD_Eb_Gb));
			put(0x85, new OpcodeX86("test", true, OD_Ev_Gv));
			put(0x86, new OpcodeX86("xchg", true, OD_Eb_Gb));
			put(0x87, new OpcodeX86("xchg", true, OD_Ev_Gv));
			put(0x88, new OpcodeX86("mov", true, OD_Eb_Gb));
			put(0x89, new OpcodeX86("mov", true, OD_Ev_Gv));
			put(0x8a, new OpcodeX86("mov", true, OD_Gb_Eb));
			put(0x8b, new OpcodeX86("mov", true, OD_Gv_Ev));
			put(0x8c, new OpcodeX86("mov", true, OD_Ev_Sw));
			put(0x8d, new OpcodeX86("lea", true, OD_Gv_M));
			put(0x8e, new OpcodeX86("mov", true, OD_Sw_Ew));
			// 0x8f: extension group 1A. see sOpcodeMap_OneByteExtension.

			put(0x90, new OpcodeX86("nop", false, OD_NO_OPERAND));
			put(0x91, new OpcodeX86("xchg", false, OD_rCX_rAX));
			put(0x92, new OpcodeX86("xchg", false, OD_rDX_rAX));
			put(0x93, new OpcodeX86("xchg", false, OD_rBX_rAX));
			put(0x94, new OpcodeX86("xchg", false, OD_rSP_rAX));
			put(0x95, new OpcodeX86("xchg", false, OD_rBP_rAX));
			put(0x96, new OpcodeX86("xchg", false, OD_rSI_rAX));
			put(0x97, new OpcodeX86("xchg", false, OD_rDI_rAX));
			// aka "cwde". TODO: consider "cbtw" variant when needed.
			put(0x98, new OpcodeX86("cwtl", false, OD_NO_OPERAND));
			// aka "cdw". TODO: consider "cwd" when needed.
			put(0x99, new OpcodeX86("cltd", false, OD_NO_OPERAND));
			put(0x9a, new OpcodeX86("call", false, OD_Ap));
			put(0x9b, new OpcodeX86("fwait", false, OD_NO_OPERAND));
			put(0x9c, new OpcodeX86("pushf", false, OD_NO_OPERAND));
			put(0x9d, new OpcodeX86("popf", false, OD_NO_OPERAND));
			put(0x9e, new OpcodeX86("sahf", false, OD_NO_OPERAND));
			put(0x9f, new OpcodeX86("lahf", false, OD_NO_OPERAND));

			put(0xa0, new OpcodeX86("mov", false, OD_AL_Ob));
			put(0xa1, new OpcodeX86("mov", false, OD_rAX_Ov));
			put(0xa2, new OpcodeX86("mov", false, OD_Ob_AL));
			put(0xa3, new OpcodeX86("mov", false, OD_Ov_rAX));
			put(0xa4, new OpcodeX86("movs" + SIZE_FLAG_TARGET, false, OD_Xb_Yb));
			put(0xa5, new OpcodeX86("movs" + SIZE_FLAG_TARGET, false, OD_Xv_Yv));
			AssemblyFormatterX86.sInstructionsNotReverseOperand.add("movs" + SIZE_FLAG_TARGET);

			put(0xa6, new OpcodeX86("cmps" + SIZE_FLAG_TARGET, false, OD_Xb_Yb));
			put(0xa7, new OpcodeX86("cmps" + SIZE_FLAG_TARGET, false, OD_Xv_Yv));
			put(0xa8, new OpcodeX86("test", false, OD_AL_Ib));
			put(0xa9, new OpcodeX86("test", false, OD_rAX_Iz));
			put(0xaa, new OpcodeX86("stos" + SIZE_FLAG_TARGET, false, OD_Yb_AL));
			put(0xab, new OpcodeX86("stos" + SIZE_FLAG_TARGET, false, OD_Yv_rAX));
			put(0xac, new OpcodeX86("lods" + SIZE_FLAG_TARGET, false, OD_AL_Xb));
			put(0xad, new OpcodeX86("lods" + SIZE_FLAG_TARGET, false, OD_rAX_Xv));
			put(0xae, new OpcodeX86("scas" + SIZE_FLAG_TARGET, false, OD_AL_Yb));
			put(0xaf, new OpcodeX86("scas" + SIZE_FLAG_TARGET, false, OD_rAX_Xv));

			put(0xb0, new OpcodeX86("mov", false, OD_AL_Ib));
			put(0xb1, new OpcodeX86("mov", false, new String[] { "CL", "Ib" }));
			put(0xb2, new OpcodeX86("mov", false, new String[] { "DL", "Ib" }));
			put(0xb3, new OpcodeX86("mov", false, new String[] { "BL", "Ib" }));
			put(0xb4, new OpcodeX86("mov", false, new String[] { "AH", "Ib" }));
			put(0xb5, new OpcodeX86("mov", false, new String[] { "CH", "Ib" }));
			put(0xb6, new OpcodeX86("mov", false, new String[] { "DH", "Ib" }));
			put(0xb7, new OpcodeX86("mov", false, new String[] { "BH", "Ib" }));
			put(0xb8, new OpcodeX86("mov", false, new String[] { "rAX", "Iv" }));
			put(0xb9, new OpcodeX86("mov", false, new String[] { "rCX", "Iv" }));
			put(0xba, new OpcodeX86("mov", false, new String[] { "rDX", "Iv" }));
			put(0xbb, new OpcodeX86("mov", false, new String[] { "rBX", "Iv" }));
			put(0xbc, new OpcodeX86("mov", false, new String[] { "rSP", "Iv" }));
			put(0xbd, new OpcodeX86("mov", false, new String[] { "rBP", "Iv" }));
			put(0xbe, new OpcodeX86("mov", false, new String[] { "rSI", "Iv" }));
			put(0xbf, new OpcodeX86("mov", false, new String[] { "rDI", "Iv" }));

			// 0xc0-0xc1: see extension group 2
			put(0xc2, new OpcodeX86("ret", false, OD_Iw));
			put(0xc3, new OpcodeX86("ret", false, OD_NO_OPERAND));
			put(0xc4, new OpcodeX86("les", true, OD_Gz_Mp));
			put(0xc5, new OpcodeX86("lds", true, OD_Gz_Mp));
			// 0xc6-0xc7: see extension group 11
			put(0xc8, new OpcodeX86("enter", false, OD_Iw_Ib));
			AssemblyFormatterX86.sInstructionsNotReverseOperand.add("enter");

			put(0xc9, new OpcodeX86("leave", false, OD_NO_OPERAND));
			put(0xca, new OpcodeX86("lret", false, OD_Iw)); // Intel syntax:
			// "retf"
			put(0xcb, new OpcodeX86("lret", false, OD_NO_OPERAND));
			put(0xcc, new OpcodeX86("int3", false, OD_NO_OPERAND));
			put(0xcd, new OpcodeX86("int", false, OD_Ib));
			put(0xce, new OpcodeX86("into", false, OD_NO_OPERAND));
			put(0xcf, new OpcodeX86("iret", false, OD_NO_OPERAND));

			// 0xd0-0xd3: see extension group 2
			put(0xd4, new OpcodeX86("aam", false, OD_Ib));
			put(0xd5, new OpcodeX86("aad", false, OD_Ib));
			put(0xd6, sInvalidOpcode);
			put(0xd7, new OpcodeX86("xlat", false, OD_NO_OPERAND));
			// 0xd8-0xdf: escape to coprocessor instructions

			put(0xe0, new OpcodeX86("loopne", false, OD_Jb));
			put(0xe1, new OpcodeX86("loope", false, OD_Jb));
			put(0xe2, new OpcodeX86("loop", false, OD_Jb));
			put(0xe3, new OpcodeX86("jcxz", false, OD_Jb));
			put(0xe4, new OpcodeX86("in", false, OD_AL_Ib));
			put(0xe5, new OpcodeX86("in", false, OD_eAX_Ib));
			put(0xe6, new OpcodeX86("out", false, OD_Ib_AL));
			put(0xe7, new OpcodeX86("out", false, OD_Ib_eAX));

			put(0xe8, new OpcodeX86("call", false, OD_Jz));
			put(0xe9, new OpcodeX86("jmp", false, OD_Jz));
			put(0xea, new OpcodeX86("ljmp", false, OD_Ap));
			put(0xeb, new OpcodeX86("jmp", false, OD_Jb));
			put(0xec, new OpcodeX86("in", false, OD_AL_iDX));
			put(0xed, new OpcodeX86("in", false, OD_eAX_iDX));
			put(0xee, new OpcodeX86("out", false, OD_iDX_AL));
			put(0xef, new OpcodeX86("out", false, OD_iDX_eAX));

			// 0xf0: prefix: LOCK
			put(0xf1, sInvalidOpcode);
			// 0xf2: prefix : REPNE
			// 0xf3: prefix : REP/REPE
			put(0xf4, new OpcodeX86("hlt", false, OD_NO_OPERAND));
			put(0xf5, new OpcodeX86("cmc", false, OD_NO_OPERAND));
			// 0xf6-0xf7: extension group 3
			put(0xf8, new OpcodeX86("clc", false, OD_NO_OPERAND));
			put(0xf9, new OpcodeX86("stc", false, OD_NO_OPERAND));
			put(0xfa, new OpcodeX86("cli", false, OD_NO_OPERAND));
			put(0xfb, new OpcodeX86("sti", false, OD_NO_OPERAND));
			put(0xfc, new OpcodeX86("cld", false, OD_NO_OPERAND));
			put(0xfd, new OpcodeX86("std", false, OD_NO_OPERAND));
			// 0xfe: extension group 4
			// 0xff: extension group 5

		}
	};

	/**
	 * Map of opcode extensions for one-byte opcode. <br>
	 * 
	 * The top level map is keyed on opcode byte, while the inner-level map is
	 * keyed on value of the REG bits (bits 5,4,3) of ModR/M byte. <br>
	 * 
	 * @See 
	 *      "Table A-6 Opcode Extensions for One- and Two-byte Opcodes by Group Number"
	 *      on page A-21 of Intel Developer Manual Vol 2B.
	 */
	public static Map<Integer, Map<Integer, OpcodeX86>> sOpcodeMap_OneByteExtension = new HashMap<Integer, Map<Integer, OpcodeX86>>() {
		{

			// ------------------- Group 1 --------------------------------
			//

			put(0x80, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("add" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x01, new OpcodeX86("or" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x02, new OpcodeX86("adc" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x03, new OpcodeX86("sbb" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x04, new OpcodeX86("and" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x05, new OpcodeX86("sub" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x06, new OpcodeX86("xor" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x07, new OpcodeX86("cmp" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
				}
			});

			put(0x81, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("add" + SIZE_FLAG_TARGET, true, OD_Ev_Iz));
					put(0x01, new OpcodeX86("or" + SIZE_FLAG_TARGET, true, OD_Ev_Iz));
					put(0x02, new OpcodeX86("adc" + SIZE_FLAG_TARGET, true, OD_Ev_Iz));
					put(0x03, new OpcodeX86("sbb" + SIZE_FLAG_TARGET, true, OD_Ev_Iz));
					put(0x04, new OpcodeX86("and" + SIZE_FLAG_TARGET, true, OD_Ev_Iz));
					put(0x05, new OpcodeX86("sub" + SIZE_FLAG_TARGET, true, OD_Ev_Iz));
					put(0x06, new OpcodeX86("xor" + SIZE_FLAG_TARGET, true, OD_Ev_Iz));
					put(0x07, new OpcodeX86("cmp" + SIZE_FLAG_TARGET, true, OD_Ev_Iz));
				}
			});

			// Difference from 0x80 table above: these instructions are invalid
			// in 64-bit mode.
			// But currently we don't care about 64-bit. ....... 07/15/09
			put(0x82, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("add" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x01, new OpcodeX86("or" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x02, new OpcodeX86("adc" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x03, new OpcodeX86("sbb" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x04, new OpcodeX86("and" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x05, new OpcodeX86("sub" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x06, new OpcodeX86("xor" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x07, new OpcodeX86("cmp" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
				}
			});

			put(0x83, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("add" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x01, new OpcodeX86("or" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x02, new OpcodeX86("adc" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x03, new OpcodeX86("sbb" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x04, new OpcodeX86("and" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x05, new OpcodeX86("sub" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x06, new OpcodeX86("xor" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x07, new OpcodeX86("cmp" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
				}
			});

			// ------------------- Group 1A --------------------------------
			//

			put(0x8f, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("pop", true, OD_Ev));
					put(0x01, sInvalidOpcode);
					put(0x02, sInvalidOpcode);
					put(0x03, sInvalidOpcode);
					put(0x04, sInvalidOpcode);
					put(0x05, sInvalidOpcode);
					put(0x06, sInvalidOpcode);
					put(0x07, sInvalidOpcode);
				}
			});

			// ------------------- Group 2 --------------------------------
			//
			put(0xc0, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("rol" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x01, new OpcodeX86("ror" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x02, new OpcodeX86("rcl" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x03, new OpcodeX86("rcr" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x04, new OpcodeX86("shl" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x05, new OpcodeX86("shr" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x06, sInvalidOpcode);
					put(0x07, new OpcodeX86("sar" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
				}
			});

			put(0xc1, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("rol" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x01, new OpcodeX86("ror" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x02, new OpcodeX86("rcl" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x03, new OpcodeX86("rcr" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x04, new OpcodeX86("shl" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x05, new OpcodeX86("shr" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x06, sInvalidOpcode);
					put(0x07, new OpcodeX86("sar" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
				}
			});

			put(0xd0, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("rol", true, OD_Eb));
					put(0x01, new OpcodeX86("ror", true, OD_Eb));
					put(0x02, new OpcodeX86("rcl", true, OD_Eb));
					put(0x03, new OpcodeX86("rcr", true, OD_Eb));
					put(0x04, new OpcodeX86("shl", true, OD_Eb));
					put(0x05, new OpcodeX86("shr", true, OD_Eb));
					put(0x06, sInvalidOpcode);
					put(0x07, new OpcodeX86("sar", true, OD_Eb));
				}
			});

			put(0xd1, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("rol" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x01, new OpcodeX86("ror" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x02, new OpcodeX86("rcl" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x03, new OpcodeX86("rcr" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x04, new OpcodeX86("shl" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x05, new OpcodeX86("shr" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x06, sInvalidOpcode);
					put(0x07, new OpcodeX86("sar" + SIZE_FLAG_TARGET, true, OD_Ev));
				}
			});

			put(0xd2, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("rol", true, OD_Eb_CL));
					put(0x01, new OpcodeX86("ror", true, OD_Eb_CL));
					put(0x02, new OpcodeX86("rcl", true, OD_Eb_CL));
					put(0x03, new OpcodeX86("rcr", true, OD_Eb_CL));
					put(0x04, new OpcodeX86("shl", true, OD_Eb_CL));
					put(0x05, new OpcodeX86("shr", true, OD_Eb_CL));
					put(0x06, sInvalidOpcode);
					put(0x07, new OpcodeX86("sar", true, OD_Eb_CL));
				}
			});

			put(0xd3, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("rol", true, OD_Ev_CL));
					put(0x01, new OpcodeX86("ror", true, OD_Ev_CL));
					put(0x02, new OpcodeX86("rcl", true, OD_Ev_CL));
					put(0x03, new OpcodeX86("rcr", true, OD_Ev_CL));
					put(0x04, new OpcodeX86("shl", true, OD_Ev_CL));
					put(0x05, new OpcodeX86("shr", true, OD_Ev_CL));
					put(0x06, sInvalidOpcode);
					put(0x07, new OpcodeX86("sar", true, OD_Ev_CL));
				}
			});

			// ------------------- Group 3 --------------------------------
			//
			put(0xf6, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("test" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x01, sInvalidOpcode);
					put(0x02, new OpcodeX86("not" + SIZE_FLAG_TARGET, true, OD_Eb));
					put(0x03, new OpcodeX86("neg" + SIZE_FLAG_TARGET, true, OD_Eb));
					put(0x04, new OpcodeX86("mul" + SIZE_FLAG_TARGET, true, OD_Eb));
					put(0x05, new OpcodeX86("imul" + SIZE_FLAG_TARGET, true, OD_Eb));
					put(0x06, new OpcodeX86("div" + SIZE_FLAG_TARGET, true, OD_Eb));
					put(0x07, new OpcodeX86("idiv" + SIZE_FLAG_TARGET, true, OD_Eb));
				}
			});

			put(0xf7, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("test" + SIZE_FLAG_TARGET, true, OD_Ev_Iz));
					put(0x01, sInvalidOpcode);
					put(0x02, new OpcodeX86("not" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x03, new OpcodeX86("neg" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x04, new OpcodeX86("mul" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x05, new OpcodeX86("imul" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x06, new OpcodeX86("div" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x07, new OpcodeX86("idiv" + SIZE_FLAG_TARGET, true, OD_Ev));
				}
			});

			// ------------------- Group 4 --------------------------------
			//
			put(0xfe, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("incb", true, OD_Eb));
					put(0x01, new OpcodeX86("decb", true, OD_Eb));
					put(0x02, sInvalidOpcode);
					put(0x03, sInvalidOpcode);
					put(0x04, sInvalidOpcode);
					put(0x05, sInvalidOpcode);
					put(0x06, sInvalidOpcode);
					put(0x07, sInvalidOpcode);
				}
			});

			// ------------------- Group 5 --------------------------------
			//
			put(0xff, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("inc" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x01, new OpcodeX86("dec" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x02, new OpcodeX86("call", true, OD_iEv));
					put(0x03, new OpcodeX86("call", true, OD_iEp));
					put(0x04, new OpcodeX86("jmp", true, OD_iEv));
					put(0x05, new OpcodeX86("jmp", true, OD_iEp));
					put(0x06, new OpcodeX86("push" + SIZE_FLAG_TARGET, true, OD_Ev));
					put(0x07, sInvalidOpcode);
				}
			});

			// ------------------- Group 11 --------------------------------
			//
			put(0xc6, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("mov" + SIZE_FLAG_TARGET, true, OD_Eb_Ib));
					put(0x01, sInvalidOpcode);
					put(0x02, sInvalidOpcode);
					put(0x03, sInvalidOpcode);
					put(0x04, sInvalidOpcode);
					put(0x05, sInvalidOpcode);
					put(0x06, sInvalidOpcode);
					put(0x07, sInvalidOpcode);
				}
			});

			// TODO: differentiate Mod bits of ModRM.
			put(0xc7, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("mov" + SIZE_FLAG_TARGET, true, OD_Ev_Iz));
					put(0x01, sInvalidOpcode);
					put(0x02, sInvalidOpcode);
					put(0x03, sInvalidOpcode);
					put(0x04, sInvalidOpcode);
					put(0x05, sInvalidOpcode);
					put(0x06, sInvalidOpcode);
					put(0x07, sInvalidOpcode);
				}
			});

		}
	};

	/**
	 * One-byte opcode instructions with mandatory prefix. The map is keyed on
	 * prefix, while the inner map is keyed on byte of the opcode.
	 */
	public static Map<Integer, Map<Integer, OpcodeX86>> sOpcodeMap_OneByteWithPrefix = new HashMap<Integer, Map<Integer, OpcodeX86>>() {
		{
			// ------------------- 66 --------------------------------
			// Special hack: This is Intel-defined mandatory prefix. Instead
			// it's
			// my approach of accommodating this special case:
			// 66 90 xchg %ax,%ax
			// which is actually two-byte "nop" operation. .... 09/23/09
			//
			put(0x66, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x90, new OpcodeX86("xchg", false, OD_rAX_rAX));
				}
			});

			// ------------------- F3 --------------------------------
			//
			put(0xF3, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x90, new OpcodeX86("pause", false, OD_NO_OPERAND));
				}
			});
		}
	};

	/**
	 * two-byte opcode instructions with mandatory prefix. The map is keyed on
	 * prefix, while the inner map is keyed on second byte of the opcode.
	 */
	public static Map<Integer, Map<Integer, OpcodeX86>> sOpcodeMap_TwoByteWithPrefix = new HashMap<Integer, Map<Integer, OpcodeX86>>() {
		{

			// ------------------- F3 --------------------------------
			//
			put(0xF3, new HashMap<Integer, OpcodeX86>() {
				{
					put(0x10, new OpcodeX86("movss", false, OD_Vss_Wss));
					put(0x11, new OpcodeX86("movss", false, OD_Wss_Vss));
					put(0xb8, new OpcodeX86("popcnt", true, OD_Gv_Ev));
				}
			});
		}
	};

	/**
	 * two-byte opcode map
	 */
	public static Map<Integer, OpcodeX86> sOpcodeMap_TwoByte = new HashMap<Integer, OpcodeX86>() {
		{
			// 0x00: extension group 6
			// 0x01: extension group 7
			put(0x02, new OpcodeX86("lar", true, OD_Gv_Ew));
			put(0x03, new OpcodeX86("lsl", true, OD_Gv_Ew));
			put(0x04, sInvalidOpcode);
			put(0x05, new OpcodeX86("syscall", false, OD_NO_OPERAND));
			put(0x06, new OpcodeX86("clts", false, OD_NO_OPERAND));
			put(0x07, new OpcodeX86("sysret", false, OD_NO_OPERAND));
			put(0x08, new OpcodeX86("invd", false, OD_NO_OPERAND));
			put(0x09, new OpcodeX86("wbinvd", false, OD_NO_OPERAND));
			put(0x0a, sInvalidOpcode);
			put(0x0b, sInvalidOpcode); // Illegal opcode. TODO: Special notation
			// needed ?
			put(0x0c, sInvalidOpcode);
			put(0x0d, new OpcodeX86("nop", true, OD_Ev));
			put(0x0e, sInvalidOpcode);
			put(0x0f, sInvalidOpcode);

			put(0x20, new OpcodeX86("mov", true, OD_Rd_Cd));
			put(0x21, new OpcodeX86("mov", true, OD_Rd_Dd));
			put(0x22, new OpcodeX86("mov", true, OD_Cd_Rd));
			put(0x23, new OpcodeX86("mov", true, OD_Dd_Rd));
			put(0x24, sInvalidOpcode);
			put(0x25, sInvalidOpcode);
			put(0x26, sInvalidOpcode);
			put(0x27, sInvalidOpcode);

			put(0x30, new OpcodeX86("wrmsr", false, OD_NO_OPERAND));
			put(0x31, new OpcodeX86("rdtsc", false, OD_NO_OPERAND));
			put(0x32, new OpcodeX86("rdmsr", false, OD_NO_OPERAND));
			put(0x33, new OpcodeX86("rdpmc", false, OD_NO_OPERAND));
			put(0x34, new OpcodeX86("sysenter", false, OD_NO_OPERAND));
			put(0x35, new OpcodeX86("sysexit", false, OD_NO_OPERAND));
			put(0x36, sInvalidOpcode);
			put(0x37, new OpcodeX86("getsec", false, OD_NO_OPERAND));
			// 0x38: 3-byte escape
			put(0x39, sInvalidOpcode);
			// 0x3a: 3-byte escape
			put(0x3b, sInvalidOpcode);
			put(0x3c, sInvalidOpcode);
			put(0x3d, sInvalidOpcode);
			put(0x3e, sInvalidOpcode);
			put(0x3f, sInvalidOpcode);

			put(0x40, new OpcodeX86("cmovo", true, OD_Gv_Ev));
			put(0x41, new OpcodeX86("cmovno", true, OD_Gv_Ev));
			put(0x42, new OpcodeX86("cmovb", true, OD_Gv_Ev));
			put(0x43, new OpcodeX86("cmovae", true, OD_Gv_Ev));
			put(0x44, new OpcodeX86("cmove", true, OD_Gv_Ev));
			put(0x45, new OpcodeX86("cmovne", true, OD_Gv_Ev));
			put(0x46, new OpcodeX86("cmovbe", true, OD_Gv_Ev));
			put(0x47, new OpcodeX86("cmovnbe", true, OD_Gv_Ev));
			put(0x48, new OpcodeX86("cmovs", true, OD_Gv_Ev));
			put(0x49, new OpcodeX86("cmovns", true, OD_Gv_Ev));
			put(0x4a, new OpcodeX86("cmovp", true, OD_Gv_Ev));
			put(0x4b, new OpcodeX86("cmovnp", true, OD_Gv_Ev));
			put(0x4c, new OpcodeX86("cmovnge", true, OD_Gv_Ev));
			put(0x4d, new OpcodeX86("cmovge", true, OD_Gv_Ev));
			put(0x4e, new OpcodeX86("cmovng", true, OD_Gv_Ev));
			put(0x4f, new OpcodeX86("cmovg", true, OD_Gv_Ev));

			put(0x80, new OpcodeX86("jo", false, OD_Jz));
			put(0x81, new OpcodeX86("jno", false, OD_Jz));
			put(0x82, new OpcodeX86("jb", false, OD_Jz));
			put(0x83, new OpcodeX86("jae", false, OD_Jz));
			put(0x84, new OpcodeX86("je", false, OD_Jz));
			put(0x85, new OpcodeX86("jne", false, OD_Jz));
			put(0x86, new OpcodeX86("jbe", false, OD_Jz));
			put(0x87, new OpcodeX86("ja", false, OD_Jz));
			put(0x88, new OpcodeX86("js", false, OD_Jz));
			put(0x89, new OpcodeX86("jns", false, OD_Jz));
			put(0x8a, new OpcodeX86("jp", false, OD_Jz));
			put(0x8b, new OpcodeX86("jnp", false, OD_Jz));
			put(0x8c, new OpcodeX86("jl", false, OD_Jz));
			put(0x8d, new OpcodeX86("jge", false, OD_Jz));
			put(0x8e, new OpcodeX86("jle", false, OD_Jz));
			put(0x8f, new OpcodeX86("jg", false, OD_Jz));

			put(0x90, new OpcodeX86("seto", true, OD_Eb));
			put(0x91, new OpcodeX86("setno", true, OD_Eb));
			put(0x92, new OpcodeX86("setb", true, OD_Eb));
			put(0x93, new OpcodeX86("setae", true, OD_Eb));
			put(0x94, new OpcodeX86("sete", true, OD_Eb));
			put(0x95, new OpcodeX86("setne", true, OD_Eb));
			put(0x96, new OpcodeX86("setbe", true, OD_Eb));
			put(0x97, new OpcodeX86("seta", true, OD_Eb));
			put(0x98, new OpcodeX86("sets", true, OD_Eb));
			put(0x99, new OpcodeX86("setns", true, OD_Eb));
			put(0x9a, new OpcodeX86("setp", true, OD_Eb));
			put(0x9b, new OpcodeX86("setnp", true, OD_Eb));
			put(0x9c, new OpcodeX86("setl", true, OD_Eb));
			put(0x9d, new OpcodeX86("setge", true, OD_Eb));
			put(0x9e, new OpcodeX86("setle", true, OD_Eb));
			put(0x9f, new OpcodeX86("setg", true, OD_Eb));

			put(0xa0, new OpcodeX86("push", false, OD_FS));
			put(0xa1, new OpcodeX86("pop", false, OD_FS));
			put(0xa2, new OpcodeX86("cpuid", false, OD_NO_OPERAND));
			put(0xa3, new OpcodeX86("bt", true, OD_Ev_Gv));
			put(0xa4, new OpcodeX86("shld", true, OD_Ev_Gv_Ib));
			put(0xa5, new OpcodeX86("shld", true, OD_Ev_Gv_CL));
			put(0xa6, sInvalidOpcode);
			put(0xa7, sInvalidOpcode);
			put(0xa8, new OpcodeX86("push", false, OD_GS));
			put(0xa9, new OpcodeX86("pop", false, OD_GS));
			put(0xaa, new OpcodeX86("rsm", false, OD_NO_OPERAND));
			put(0xab, new OpcodeX86("bts", true, OD_Ev_Gv));
			put(0xac, new OpcodeX86("shrd", true, OD_Ev_Gv_Ib));
			put(0xad, new OpcodeX86("shrd", true, OD_Ev_Gv_CL));
			// 0xae: extension group 15
			put(0xaf, new OpcodeX86("imul", true, OD_Gv_Ev));

			put(0xb0, new OpcodeX86("cmpxchg", true, OD_Eb_Gb));
			put(0xb1, new OpcodeX86("cmpxchg", true, OD_Ev_Gv));
			put(0xb2, new OpcodeX86("lss", true, OD_Gv_Mp));
			put(0xb3, new OpcodeX86("btr", true, OD_Ev_Gv));
			put(0xb4, new OpcodeX86("lfs", true, OD_Gv_Mp));
			put(0xb5, new OpcodeX86("lgs", true, OD_Gv_Mp));
			put(0xb6, new OpcodeX86("movzb" + SIZE_FLAG_TARGET, true, OD_Gv_Eb)); // Intel
			// "movzx".
			AssemblyFormatterX86.sInstructionsSuffixFromRegisterOperand.add("movzb" + SIZE_FLAG_TARGET);
			put(0xb7, new OpcodeX86("movzw" + SIZE_FLAG_TARGET, true, OD_Gv_Ew));
			AssemblyFormatterX86.sInstructionsSuffixFromRegisterOperand.add("movzw" + SIZE_FLAG_TARGET);
			put(0xb8, new OpcodeX86("jmpe", false, OD_NO_OPERAND));
			put(0xb9, sInvalidOpcode); // extension group 10
			// 0xba: extension group 8
			put(0xbb, new OpcodeX86("btc", true, OD_Ev_Gv));
			put(0xbc, new OpcodeX86("bsf", true, OD_Gv_Ev));
			put(0xbd, new OpcodeX86("bsr", true, OD_Gv_Ev));
			put(0xbe, new OpcodeX86("movsb" + SIZE_FLAG_TARGET, true, OD_Gv_Eb)); // Intel
			// "movsx".
			AssemblyFormatterX86.sInstructionsSuffixFromRegisterOperand.add("movsb" + SIZE_FLAG_TARGET);
			put(0xbf, new OpcodeX86("movsw" + SIZE_FLAG_TARGET, true, OD_Gv_Ew)); // Intel
			// "movsx"
			AssemblyFormatterX86.sInstructionsSuffixFromRegisterOperand.add("movsw" + SIZE_FLAG_TARGET);

			put(0xc0, new OpcodeX86("xadd", true, OD_Eb_Gb));
			put(0xc1, new OpcodeX86("xadd", true, OD_Ev_Gv));
			put(0xc8, new OpcodeX86("bswap", false, OD_eAX)); // "eAX" is fine
			// in lieu of
			// "EAX".
			put(0xc9, new OpcodeX86("bswap", false, OD_eCX));
			put(0xca, new OpcodeX86("bswap", false, OD_eDX));
			put(0xcb, new OpcodeX86("bswap", false, OD_eBX));
			put(0xcc, new OpcodeX86("bswap", false, OD_eSP));
			put(0xcd, new OpcodeX86("bswap", false, OD_eBP));
			put(0xce, new OpcodeX86("bswap", false, OD_eSI));
			put(0xcf, new OpcodeX86("bswap", false, OD_eDI));
		}
	};

	/**
	 * Map of opcode extensions for two-byte opcode. <br>
	 * 
	 * The top level map is keyed on
	 * 
	 * <pre>
	 * [last opcode byte] &lt;&lt; 4 | [MOD bits (bits 7,6) of ModRM]
	 * </pre>
	 * 
	 * while the inner-level map is keyed on
	 * 
	 * <pre>
	 * [REG bits (bits 5,4,3) of ModR/M byte]
	 * </pre>
	 * 
	 * @See 
	 *      "Table A-6 Opcode Extensions for One- and Two-byte Opcodes by Group Number"
	 *      on page A-21 of Intel Developer Manual Vol 2B.
	 */
	public static Map<Integer, Map<Integer, OpcodeX86>> sOpcodeMap_TwoByteExtension = new HashMap<Integer, Map<Integer, OpcodeX86>>() {
		{

			// ------------------- Group 6 --------------------------------
			//
			Map<Integer, OpcodeX86> subMap_00 = new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("sldt", true, OD_RvMw));
					put(0x01, new OpcodeX86("str", true, OD_RvMw));
					put(0x02, new OpcodeX86("lldt", true, OD_Ew));
					put(0x03, new OpcodeX86("ltr", true, OD_Ew));
					put(0x04, new OpcodeX86("verr", true, OD_Ew));
					put(0x05, new OpcodeX86("verw", true, OD_Ew));
					put(0x06, sInvalidOpcode);
					put(0x07, sInvalidOpcode);
				}
			};

			put(0x0 << 4 | 0, subMap_00);
			put(0x0 << 4 | 1, subMap_00);
			put(0x0 << 4 | 2, subMap_00);
			put(0x0 << 4 | 3, subMap_00);

			// ------------------- Group 7 --------------------------------
			//
			Map<Integer, OpcodeX86> subMap_01_0_2 = new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, new OpcodeX86("sgdt", true, OD_Ms));
					put(0x01, new OpcodeX86("sidt", true, OD_Ms));
					put(0x02, new OpcodeX86("lgdt", true, OD_Ms));
					put(0x03, new OpcodeX86("lidt", true, OD_Ms));
					put(0x04, new OpcodeX86("smsw", true, OD_RvMw));
					put(0x05, sInvalidOpcode);
					put(0x06, new OpcodeX86("lmsw", true, OD_Ew));
				}
			};

			Map<Integer, OpcodeX86> subMap_01_3 = new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, sVaringOpcode); // see selectExtensionOpcodeByRM()
					// below.
					put(0x01, sVaringOpcode);
					put(0x02, sVaringOpcode);
					put(0x03, sInvalidOpcode);
					put(0x04, new OpcodeX86("smsw", true, OD_RvMw));
					put(0x05, sInvalidOpcode);
					put(0x06, new OpcodeX86("lmsw", true, OD_Ew));
					put(0x07, sVaringOpcode);
				}
			};

			put(0x01 << 4 | 0, subMap_01_0_2);
			put(0x01 << 4 | 1, subMap_01_0_2);
			put(0x01 << 4 | 2, subMap_01_0_2);
			put(0x01 << 4 | 3, subMap_01_3);

			// ------------------- Group 8 --------------------------------
			//
			Map<Integer, OpcodeX86> subMap_ba = new HashMap<Integer, OpcodeX86>() {
				{
					put(0x00, sInvalidOpcode);
					put(0x01, sInvalidOpcode);
					put(0x02, sInvalidOpcode);
					put(0x03, sInvalidOpcode);
					put(0x04, new OpcodeX86("bt" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x05, new OpcodeX86("bts" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x06, new OpcodeX86("btr" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
					put(0x07, new OpcodeX86("btc" + SIZE_FLAG_TARGET, true, OD_Ev_Ib));
				}
			};

			put(0xba << 4 | 0, subMap_ba);
			put(0xba << 4 | 1, subMap_ba);
			put(0xba << 4 | 2, subMap_ba);
			put(0xba << 4 | 3, subMap_ba);

		}
	};

	/**
	 * This is to select the opcode by RM bits of ModRM in two-bye opcode
	 * extension table.
	 * 
	 * @param opcodeID
	 * @param modrm
	 * @return
	 */
	static public OpcodeX86 selectExtensionOpcodeByRM(int opcodeID, ModRM modrm) {
		OpcodeX86 opc = null;

		if (opcodeID == 0x01 && modrm.mod == 3) {
			// special cases where RM bits are also part of opcode.
			//
			switch (modrm.reg) {
			case 0:
				switch (modrm.rm) {
				case 1:
					opc = new OpcodeX86("vmcall", true, OpcodeX86.OD_NO_OPERAND);
					break;
				case 2:
					opc = new OpcodeX86("vmlaunch", true, OpcodeX86.OD_NO_OPERAND);
					break;
				case 3:
					opc = new OpcodeX86("vmresume", true, OpcodeX86.OD_NO_OPERAND);
					break;
				case 4:
					opc = new OpcodeX86("vmxoff", true, OpcodeX86.OD_NO_OPERAND);
					break;
				default:
					opc = OpcodeX86.sInvalidOpcode;
				}
				break;
			case 1:
				switch (modrm.rm) {
				case 0:
					opc = new OpcodeX86("monitor", true, OpcodeX86.OD_NO_OPERAND);
					break;
				case 1:
					opc = new OpcodeX86("mwait", true, OpcodeX86.OD_NO_OPERAND);
					break;
				default:
					opc = OpcodeX86.sInvalidOpcode;
				}
				break;
			case 2:
				switch (modrm.rm) {
				case 0:
					opc = new OpcodeX86("xgetbv", true, OpcodeX86.OD_NO_OPERAND);
					break;
				case 1:
					opc = new OpcodeX86("xsetbv", true, OpcodeX86.OD_NO_OPERAND);
					break;
				default:
					opc = OpcodeX86.sInvalidOpcode;
				}
				break;
			case 7:
				switch (modrm.rm) {
				case 0:
					opc = new OpcodeX86("swapgs", true, OpcodeX86.OD_NO_OPERAND);
					break;
				case 1:
					opc = new OpcodeX86("rdtscp", true, OpcodeX86.OD_NO_OPERAND);
					break;
				default:
					opc = OpcodeX86.sInvalidOpcode;
				}
				break;

			default:
				assert false : "Should not get here.";
				break;
			}
		}

		return opc;
	}

	/**
	 * three-byte opcode map (first two bytes being "0f 38")
	 */
	public static Map<Integer, OpcodeX86> sOpcodeMap_ThreeByte_0F38 = new HashMap<Integer, OpcodeX86>() {
		{
			put(0xF0, new OpcodeX86("movbe", true, OD_Gv_Mv));
			put(0xF1, new OpcodeX86("movbe", true, OD_Mv_Gv));
		}
	};

	/**
	 * three-byte opcode map (first two bytes being "0f 3a")
	 */
	public static Map<Integer, OpcodeX86> sOpcodeMap_ThreeByte_0F3A = new HashMap<Integer, OpcodeX86>() {
		{
			;
		}
	};

	/**
	 * three-byte opcode instructions with mandatory prefix. The map is keyed on
	 * prefix, while the inner map is keyed on third byte of the opcode.
	 */
	public static Map<Integer, Map<Integer, OpcodeX86>> sOpcodeMap_ThreeByteWithPrefix = new HashMap<Integer, Map<Integer, OpcodeX86>>();

	/**
	 * <p>
	 * (from Appendix A.5 of Intel Manual vol.3B) <br>
	 * Opcode maps for coprocessor escape instruction opcodes (x87
	 * floating-point instruction opcodes) are in Table A-7 through Table A-22.
	 * These maps are grouped by the first byte of the opcode, from D8-DF. Each
	 * of these opcodes has a ModR/M byte. If the ModR/M byte is within the
	 * range of 00H-BFH, bits 3-5 of the ModR/M byte are used as an opcode
	 * extension, similar to the technique used for 1-and 2-byte opcodes (see
	 * A.4). If the ModR/M byte is outside the range of 00H through BFH, the
	 * entire ModR/M byte is used as an opcode extension.
	 * </p>
	 * <p>
	 * The map is keyed on escape code (0xDn), while the value are array of two
	 * maps:<br>
	 * 1. The first map contains opcodes for ModRM within 00-BF and is keyed on
	 * REG bits of ModRM;<br>
	 * 2. The second map contains opcodes for ModRM beyond BF and is keyed on
	 * ModRM value.
	 */
	public static Map<Integer, List<Map<Integer, OpcodeX86>>> sOpcodeMap_Escape = new HashMap<Integer, List<Map<Integer, OpcodeX86>>>() {
		{

			put(0xd8, new ArrayList<Map<Integer, OpcodeX86>>() {
				{
					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0x0, new OpcodeX86("fadds", true, OD_Ev));
							put(0x1, new OpcodeX86("fmuls", true, OD_Ev));
							put(0x2, new OpcodeX86("fcoms", true, OD_Ev));
							put(0x3, new OpcodeX86("fcomps", true, OD_Ev));
							put(0x4, new OpcodeX86("fsubs", true, OD_Ev));
							put(0x5, new OpcodeX86("fsubrs", true, OD_Ev));
							put(0x6, new OpcodeX86("fdivs", true, OD_Ev));
							put(0x7, new OpcodeX86("fdivrs", true, OD_Ev));
						}
					});

					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0xc0, new OpcodeX86("fadd", true, OD_ST_ST0));
							put(0xc1, new OpcodeX86("fadd", true, OD_ST_ST1));
							put(0xc2, new OpcodeX86("fadd", true, OD_ST_ST2));
							put(0xc3, new OpcodeX86("fadd", true, OD_ST_ST3));
							put(0xc4, new OpcodeX86("fadd", true, OD_ST_ST4));
							put(0xc5, new OpcodeX86("fadd", true, OD_ST_ST5));
							put(0xc6, new OpcodeX86("fadd", true, OD_ST_ST6));
							put(0xc7, new OpcodeX86("fadd", true, OD_ST_ST7));
							put(0xc8, new OpcodeX86("fmul", true, OD_ST_ST0));
							put(0xc9, new OpcodeX86("fmul", true, OD_ST_ST1));
							put(0xca, new OpcodeX86("fmul", true, OD_ST_ST2));
							put(0xcb, new OpcodeX86("fmul", true, OD_ST_ST3));
							put(0xcc, new OpcodeX86("fmul", true, OD_ST_ST4));
							put(0xcd, new OpcodeX86("fmul", true, OD_ST_ST5));
							put(0xce, new OpcodeX86("fmul", true, OD_ST_ST6));
							put(0xcf, new OpcodeX86("fmul", true, OD_ST_ST7));

							put(0xd0, new OpcodeX86("fcom", true, OD_ST_ST0));
							put(0xd1, new OpcodeX86("fcom", true, OD_ST_ST1));
							put(0xd2, new OpcodeX86("fcom", true, OD_ST_ST2));
							put(0xd3, new OpcodeX86("fcom", true, OD_ST_ST3));
							put(0xd4, new OpcodeX86("fcom", true, OD_ST_ST4));
							put(0xd5, new OpcodeX86("fcom", true, OD_ST_ST5));
							put(0xd6, new OpcodeX86("fcom", true, OD_ST_ST6));
							put(0xd7, new OpcodeX86("fcom", true, OD_ST_ST7));
							put(0xd8, new OpcodeX86("fcomp", true, OD_ST_ST0));
							put(0xd9, new OpcodeX86("fcomp", true, OD_ST_ST1));
							put(0xda, new OpcodeX86("fcomp", true, OD_ST_ST2));
							put(0xdb, new OpcodeX86("fcomp", true, OD_ST_ST3));
							put(0xdc, new OpcodeX86("fcomp", true, OD_ST_ST4));
							put(0xdd, new OpcodeX86("fcomp", true, OD_ST_ST5));
							put(0xde, new OpcodeX86("fcomp", true, OD_ST_ST6));
							put(0xdf, new OpcodeX86("fcomp", true, OD_ST_ST7));

							put(0xe0, new OpcodeX86("fsub", true, OD_ST_ST0));
							put(0xe1, new OpcodeX86("fsub", true, OD_ST_ST1));
							put(0xe2, new OpcodeX86("fsub", true, OD_ST_ST2));
							put(0xe3, new OpcodeX86("fsub", true, OD_ST_ST3));
							put(0xe4, new OpcodeX86("fsub", true, OD_ST_ST4));
							put(0xe5, new OpcodeX86("fsub", true, OD_ST_ST5));
							put(0xe6, new OpcodeX86("fsub", true, OD_ST_ST6));
							put(0xe7, new OpcodeX86("fsub", true, OD_ST_ST7));
							put(0xe8, new OpcodeX86("fsubr", true, OD_ST_ST0));
							put(0xe9, new OpcodeX86("fsubr", true, OD_ST_ST1));
							put(0xea, new OpcodeX86("fsubr", true, OD_ST_ST2));
							put(0xeb, new OpcodeX86("fsubr", true, OD_ST_ST3));
							put(0xec, new OpcodeX86("fsubr", true, OD_ST_ST4));
							put(0xed, new OpcodeX86("fsubr", true, OD_ST_ST5));
							put(0xee, new OpcodeX86("fsubr", true, OD_ST_ST6));
							put(0xef, new OpcodeX86("fsubr", true, OD_ST_ST7));

							put(0xf0, new OpcodeX86("fdiv", true, OD_ST_ST0));
							put(0xf1, new OpcodeX86("fdiv", true, OD_ST_ST1));
							put(0xf2, new OpcodeX86("fdiv", true, OD_ST_ST2));
							put(0xf3, new OpcodeX86("fdiv", true, OD_ST_ST3));
							put(0xf4, new OpcodeX86("fdiv", true, OD_ST_ST4));
							put(0xf5, new OpcodeX86("fdiv", true, OD_ST_ST5));
							put(0xf6, new OpcodeX86("fdiv", true, OD_ST_ST6));
							put(0xf7, new OpcodeX86("fdiv", true, OD_ST_ST7));
							put(0xf8, new OpcodeX86("fdivr", true, OD_ST_ST0));
							put(0xf9, new OpcodeX86("fdivr", true, OD_ST_ST1));
							put(0xfa, new OpcodeX86("fdivr", true, OD_ST_ST2));
							put(0xfb, new OpcodeX86("fdivr", true, OD_ST_ST3));
							put(0xfc, new OpcodeX86("fdivr", true, OD_ST_ST4));
							put(0xfd, new OpcodeX86("fdivr", true, OD_ST_ST5));
							put(0xfe, new OpcodeX86("fdivr", true, OD_ST_ST6));
							put(0xff, new OpcodeX86("fdivr", true, OD_ST_ST7));
						}
					});
				}
			});

			put(0xd9, new ArrayList<Map<Integer, OpcodeX86>>() {
				{
					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0x0, new OpcodeX86("flds", true, OD_Ev));
							put(0x1, sInvalidOpcode);
							put(0x2, new OpcodeX86("fsts", true, OD_Ev));
							put(0x3, new OpcodeX86("fstps", true, OD_Ev));
							put(0x4, new OpcodeX86("fldenv", true, OD_Ev));
							put(0x5, new OpcodeX86("fldcw", true, OD_Ev));
							/*
							 * Here's why "fstenv" has the leading "9b" (same
							 * for "fstcw"): 9b d9 35 00 00 01 00 fstenv 0x10000
							 * d9 35 00 00 01 00 fnstenv 0x10000
							 * 
							 * (See page 3-430 in Intel vol. 2A) The assembler
							 * issues two instructions for the FSTENV
							 * instruction (an FWAIT instruction followed by an
							 * FNSTENV instruction), and the processor executes
							 * each of these instructions separately.
							 */
							put(0x6, new OpcodeX86("fnstenv", true, OD_Ev));
							put(0x7, new OpcodeX86("fnstcw", true, OD_Ev));
						}
					});

					add(new HashMap<Integer, OpcodeX86>() {
						{
							// Intel manual requires OD_ST_ST0, but GNU
							// assembler not.
							put(0xc0, new OpcodeX86("fld", true, OD_ST0));
							put(0xc1, new OpcodeX86("fld", true, OD_ST1));
							put(0xc2, new OpcodeX86("fld", true, OD_ST2));
							put(0xc3, new OpcodeX86("fld", true, OD_ST3));
							put(0xc4, new OpcodeX86("fld", true, OD_ST4));
							put(0xc5, new OpcodeX86("fld", true, OD_ST5));
							put(0xc6, new OpcodeX86("fld", true, OD_ST6));
							put(0xc7, new OpcodeX86("fld", true, OD_ST7));
							put(0xc8, new OpcodeX86("fxch", true, OD_ST0));
							put(0xc9, new OpcodeX86("fxch", true, OD_ST1));
							put(0xca, new OpcodeX86("fxch", true, OD_ST2));
							put(0xcb, new OpcodeX86("fxch", true, OD_ST3));
							put(0xcc, new OpcodeX86("fxch", true, OD_ST4));
							put(0xcd, new OpcodeX86("fxch", true, OD_ST5));
							put(0xce, new OpcodeX86("fxch", true, OD_ST6));
							put(0xcf, new OpcodeX86("fxch", true, OD_ST7));

							put(0xd0, new OpcodeX86("fnop", true, OD_NO_OPERAND));
							put(0xd1, sInvalidOpcode);
							put(0xd2, sInvalidOpcode);
							put(0xd3, sInvalidOpcode);
							put(0xd4, sInvalidOpcode);
							put(0xd5, sInvalidOpcode);
							put(0xd6, sInvalidOpcode);
							put(0xd7, sInvalidOpcode);
							put(0xd8, sInvalidOpcode);
							put(0xd9, sInvalidOpcode);
							put(0xda, sInvalidOpcode);
							put(0xdb, sInvalidOpcode);
							put(0xdc, sInvalidOpcode);
							put(0xdd, sInvalidOpcode);
							put(0xde, sInvalidOpcode);
							put(0xdf, sInvalidOpcode);

							put(0xe0, new OpcodeX86("fchs", true, OD_NO_OPERAND));
							put(0xe1, new OpcodeX86("fabs", true, OD_NO_OPERAND));
							put(0xe2, sInvalidOpcode);
							put(0xe3, sInvalidOpcode);
							put(0xe4, new OpcodeX86("ftst", true, OD_NO_OPERAND));
							put(0xe5, new OpcodeX86("fxam", true, OD_NO_OPERAND));
							put(0xe6, sInvalidOpcode);
							put(0xe7, sInvalidOpcode);
							put(0xe8, new OpcodeX86("fld1", true, OD_NO_OPERAND));
							put(0xe9, new OpcodeX86("fldl2t", true, OD_NO_OPERAND));
							put(0xea, new OpcodeX86("fldl2e", true, OD_NO_OPERAND));
							put(0xeb, new OpcodeX86("fldpi", true, OD_NO_OPERAND));
							put(0xec, new OpcodeX86("fldlg2", true, OD_NO_OPERAND));
							put(0xed, new OpcodeX86("fldln2", true, OD_NO_OPERAND));
							put(0xee, new OpcodeX86("fldz", true, OD_NO_OPERAND));
							put(0xef, sInvalidOpcode);

							put(0xf0, new OpcodeX86("f2xm1", true, OD_NO_OPERAND));
							put(0xf1, new OpcodeX86("fyl2x", true, OD_NO_OPERAND));
							put(0xf2, new OpcodeX86("fptan", true, OD_NO_OPERAND));
							put(0xf3, new OpcodeX86("fpatan", true, OD_NO_OPERAND));
							put(0xf4, new OpcodeX86("fxtract", true, OD_NO_OPERAND));
							put(0xf5, new OpcodeX86("fprem1", true, OD_NO_OPERAND));
							put(0xf6, new OpcodeX86("fdecstp", true, OD_NO_OPERAND));
							put(0xf7, new OpcodeX86("fincstp", true, OD_NO_OPERAND));
							put(0xf8, new OpcodeX86("fprem", true, OD_NO_OPERAND));
							put(0xf9, new OpcodeX86("fyl2xp1", true, OD_NO_OPERAND));
							put(0xfa, new OpcodeX86("fsqrt", true, OD_NO_OPERAND));
							put(0xfb, new OpcodeX86("fsincos", true, OD_NO_OPERAND));
							put(0xfc, new OpcodeX86("frndint", true, OD_NO_OPERAND));
							put(0xfd, new OpcodeX86("fscale", true, OD_NO_OPERAND));
							put(0xfe, new OpcodeX86("fsin", true, OD_NO_OPERAND));
							put(0xff, new OpcodeX86("fcos", true, OD_NO_OPERAND));
						}
					});
				}
			});

			put(0xda, new ArrayList<Map<Integer, OpcodeX86>>() {
				{
					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0x0, new OpcodeX86("fiaddl", true, OD_Ev));
							put(0x1, new OpcodeX86("fimull", true, OD_Ev));
							put(0x2, new OpcodeX86("ficoml", true, OD_Ev));
							put(0x3, new OpcodeX86("ficompl", true, OD_Ev));
							put(0x4, new OpcodeX86("fisubl", true, OD_Ev));
							put(0x5, new OpcodeX86("fisubrl", true, OD_Ev));
							put(0x6, new OpcodeX86("fidivl", true, OD_Ev));
							put(0x7, new OpcodeX86("fidivrl", true, OD_Ev));
						}
					});

					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0xc0, new OpcodeX86("fcmovb", true, OD_ST_ST0));
							put(0xc1, new OpcodeX86("fcmovb", true, OD_ST_ST1));
							put(0xc2, new OpcodeX86("fcmovb", true, OD_ST_ST2));
							put(0xc3, new OpcodeX86("fcmovb", true, OD_ST_ST3));
							put(0xc4, new OpcodeX86("fcmovb", true, OD_ST_ST4));
							put(0xc5, new OpcodeX86("fcmovb", true, OD_ST_ST5));
							put(0xc6, new OpcodeX86("fcmovb", true, OD_ST_ST6));
							put(0xc7, new OpcodeX86("fcmovb", true, OD_ST_ST7));
							put(0xc8, new OpcodeX86("fcmove", true, OD_ST_ST0));
							put(0xc9, new OpcodeX86("fcmove", true, OD_ST_ST1));
							put(0xca, new OpcodeX86("fcmove", true, OD_ST_ST2));
							put(0xcb, new OpcodeX86("fcmove", true, OD_ST_ST3));
							put(0xcc, new OpcodeX86("fcmove", true, OD_ST_ST4));
							put(0xcd, new OpcodeX86("fcmove", true, OD_ST_ST5));
							put(0xce, new OpcodeX86("fcmove", true, OD_ST_ST6));
							put(0xcf, new OpcodeX86("fcmove", true, OD_ST_ST7));

							put(0xd0, new OpcodeX86("fcmovbe", true, OD_ST_ST0));
							put(0xd1, new OpcodeX86("fcmovbe", true, OD_ST_ST1));
							put(0xd2, new OpcodeX86("fcmovbe", true, OD_ST_ST2));
							put(0xd3, new OpcodeX86("fcmovbe", true, OD_ST_ST3));
							put(0xd4, new OpcodeX86("fcmovbe", true, OD_ST_ST4));
							put(0xd5, new OpcodeX86("fcmovbe", true, OD_ST_ST5));
							put(0xd6, new OpcodeX86("fcmovbe", true, OD_ST_ST6));
							put(0xd7, new OpcodeX86("fcmovbe", true, OD_ST_ST7));
							put(0xd8, new OpcodeX86("fcmovu", true, OD_ST_ST0));
							put(0xd9, new OpcodeX86("fcmovu", true, OD_ST_ST1));
							put(0xda, new OpcodeX86("fcmovu", true, OD_ST_ST2));
							put(0xdb, new OpcodeX86("fcmovu", true, OD_ST_ST3));
							put(0xdc, new OpcodeX86("fcmovu", true, OD_ST_ST4));
							put(0xdd, new OpcodeX86("fcmovu", true, OD_ST_ST5));
							put(0xde, new OpcodeX86("fcmovu", true, OD_ST_ST6));
							put(0xdf, new OpcodeX86("fcmovu", true, OD_ST_ST7));

							put(0xe0, sInvalidOpcode);
							put(0xe1, sInvalidOpcode);
							put(0xe2, sInvalidOpcode);
							put(0xe3, sInvalidOpcode);
							put(0xe4, sInvalidOpcode);
							put(0xe5, sInvalidOpcode);
							put(0xe6, sInvalidOpcode);
							put(0xe7, sInvalidOpcode);
							put(0xe8, sInvalidOpcode);
							put(0xe9, new OpcodeX86("fucompp", true, OD_NO_OPERAND));
							put(0xea, sInvalidOpcode);
							put(0xeb, sInvalidOpcode);
							put(0xec, sInvalidOpcode);
							put(0xed, sInvalidOpcode);
							put(0xee, sInvalidOpcode);
							put(0xef, sInvalidOpcode);

							put(0xf0, sInvalidOpcode);
							put(0xf1, sInvalidOpcode);
							put(0xf2, sInvalidOpcode);
							put(0xf3, sInvalidOpcode);
							put(0xf4, sInvalidOpcode);
							put(0xf5, sInvalidOpcode);
							put(0xf6, sInvalidOpcode);
							put(0xf7, sInvalidOpcode);
							put(0xf8, sInvalidOpcode);
							put(0xf9, sInvalidOpcode);
							put(0xfa, sInvalidOpcode);
							put(0xfb, sInvalidOpcode);
							put(0xfc, sInvalidOpcode);
							put(0xfd, sInvalidOpcode);
							put(0xfe, sInvalidOpcode);
							put(0xff, sInvalidOpcode);
						}
					});
				}
			});

			put(0xdb, new ArrayList<Map<Integer, OpcodeX86>>() {
				{
					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0x0, new OpcodeX86("fildl", true, OD_Ev));
							put(0x1, new OpcodeX86("fisttpl", true, OD_Ev));
							put(0x2, new OpcodeX86("fistl", true, OD_Ev));
							put(0x3, new OpcodeX86("fistpl", true, OD_Ev));
							put(0x4, sInvalidOpcode);
							put(0x5, new OpcodeX86("fldt", true, OD_Ev));
							put(0x6, sInvalidOpcode);
							put(0x7, new OpcodeX86("fstpt", true, OD_Ev));
						}
					});

					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0xc0, new OpcodeX86("fcmovnb", true, OD_ST_ST0));
							put(0xc1, new OpcodeX86("fcmovnb", true, OD_ST_ST1));
							put(0xc2, new OpcodeX86("fcmovnb", true, OD_ST_ST2));
							put(0xc3, new OpcodeX86("fcmovnb", true, OD_ST_ST3));
							put(0xc4, new OpcodeX86("fcmovnb", true, OD_ST_ST4));
							put(0xc5, new OpcodeX86("fcmovnb", true, OD_ST_ST5));
							put(0xc6, new OpcodeX86("fcmovnb", true, OD_ST_ST6));
							put(0xc7, new OpcodeX86("fcmovnb", true, OD_ST_ST7));
							put(0xc8, new OpcodeX86("fcmovne", true, OD_ST_ST0));
							put(0xc9, new OpcodeX86("fcmovne", true, OD_ST_ST1));
							put(0xca, new OpcodeX86("fcmovne", true, OD_ST_ST2));
							put(0xcb, new OpcodeX86("fcmovne", true, OD_ST_ST3));
							put(0xcc, new OpcodeX86("fcmovne", true, OD_ST_ST4));
							put(0xcd, new OpcodeX86("fcmovne", true, OD_ST_ST5));
							put(0xce, new OpcodeX86("fcmovne", true, OD_ST_ST6));
							put(0xcf, new OpcodeX86("fcmovne", true, OD_ST_ST7));

							put(0xd0, new OpcodeX86("fcmovnbe", true, OD_ST_ST0));
							put(0xd1, new OpcodeX86("fcmovnbe", true, OD_ST_ST1));
							put(0xd2, new OpcodeX86("fcmovnbe", true, OD_ST_ST2));
							put(0xd3, new OpcodeX86("fcmovnbe", true, OD_ST_ST3));
							put(0xd4, new OpcodeX86("fcmovnbe", true, OD_ST_ST4));
							put(0xd5, new OpcodeX86("fcmovnbe", true, OD_ST_ST5));
							put(0xd6, new OpcodeX86("fcmovnbe", true, OD_ST_ST6));
							put(0xd7, new OpcodeX86("fcmovnbe", true, OD_ST_ST7));
							put(0xd8, new OpcodeX86("fcmovnu", true, OD_ST_ST0));
							put(0xd9, new OpcodeX86("fcmovnu", true, OD_ST_ST1));
							put(0xda, new OpcodeX86("fcmovnu", true, OD_ST_ST2));
							put(0xdb, new OpcodeX86("fcmovnu", true, OD_ST_ST3));
							put(0xdc, new OpcodeX86("fcmovnu", true, OD_ST_ST4));
							put(0xdd, new OpcodeX86("fcmovnu", true, OD_ST_ST5));
							put(0xde, new OpcodeX86("fcmovnu", true, OD_ST_ST6));
							put(0xdf, new OpcodeX86("fcmovnu", true, OD_ST_ST7));

							put(0xe0, sInvalidOpcode);
							put(0xe1, sInvalidOpcode);
							put(0xe2, new OpcodeX86("fclex", true, OD_NO_OPERAND));
							put(0xe3, new OpcodeX86("finit", true, OD_NO_OPERAND));
							put(0xe4, sInvalidOpcode);
							put(0xe5, sInvalidOpcode);
							put(0xe6, sInvalidOpcode);
							put(0xe7, sInvalidOpcode);
							put(0xe8, new OpcodeX86("fucomi", true, OD_ST_ST0));
							put(0xe9, new OpcodeX86("fucomi", true, OD_ST_ST1));
							put(0xea, new OpcodeX86("fucomi", true, OD_ST_ST2));
							put(0xeb, new OpcodeX86("fucomi", true, OD_ST_ST3));
							put(0xec, new OpcodeX86("fucomi", true, OD_ST_ST4));
							put(0xed, new OpcodeX86("fucomi", true, OD_ST_ST5));
							put(0xee, new OpcodeX86("fucomi", true, OD_ST_ST6));
							put(0xef, new OpcodeX86("fucomi", true, OD_ST_ST7));

							put(0xf0, new OpcodeX86("fcomi", true, OD_ST_ST0));
							put(0xf1, new OpcodeX86("fcomi", true, OD_ST_ST1));
							put(0xf2, new OpcodeX86("fcomi", true, OD_ST_ST2));
							put(0xf3, new OpcodeX86("fcomi", true, OD_ST_ST3));
							put(0xf4, new OpcodeX86("fcomi", true, OD_ST_ST4));
							put(0xf5, new OpcodeX86("fcomi", true, OD_ST_ST5));
							put(0xf6, new OpcodeX86("fcomi", true, OD_ST_ST6));
							put(0xf7, new OpcodeX86("fcomi", true, OD_ST_ST7));
							put(0xf8, sInvalidOpcode);
							put(0xf9, sInvalidOpcode);
							put(0xfa, sInvalidOpcode);
							put(0xfb, sInvalidOpcode);
							put(0xfc, sInvalidOpcode);
							put(0xfd, sInvalidOpcode);
							put(0xfe, sInvalidOpcode);
							put(0xff, sInvalidOpcode);
						}
					});
				}
			});

			put(0xdc, new ArrayList<Map<Integer, OpcodeX86>>() {
				{
					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0x0, new OpcodeX86("faddl", true, OD_Ev));
							put(0x1, new OpcodeX86("fmull", true, OD_Ev));
							put(0x2, new OpcodeX86("fcoml", true, OD_Ev));
							put(0x3, new OpcodeX86("fcompl", true, OD_Ev));
							put(0x4, new OpcodeX86("fsubl", true, OD_Ev));
							put(0x5, new OpcodeX86("fsubrl", true, OD_Ev));
							put(0x6, new OpcodeX86("fdivl", true, OD_Ev));
							put(0x7, new OpcodeX86("fdivrl", true, OD_Ev));
						}
					});

					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0xc0, new OpcodeX86("fadd", true, OD_ST0_ST));
							put(0xc1, new OpcodeX86("fadd", true, OD_ST1_ST));
							put(0xc2, new OpcodeX86("fadd", true, OD_ST2_ST));
							put(0xc3, new OpcodeX86("fadd", true, OD_ST3_ST));
							put(0xc4, new OpcodeX86("fadd", true, OD_ST4_ST));
							put(0xc5, new OpcodeX86("fadd", true, OD_ST5_ST));
							put(0xc6, new OpcodeX86("fadd", true, OD_ST6_ST));
							put(0xc7, new OpcodeX86("fadd", true, OD_ST7_ST));
							put(0xc8, new OpcodeX86("fmul", true, OD_ST0_ST));
							put(0xc9, new OpcodeX86("fmul", true, OD_ST1_ST));
							put(0xca, new OpcodeX86("fmul", true, OD_ST2_ST));
							put(0xcb, new OpcodeX86("fmul", true, OD_ST3_ST));
							put(0xcc, new OpcodeX86("fmul", true, OD_ST4_ST));
							put(0xcd, new OpcodeX86("fmul", true, OD_ST5_ST));
							put(0xce, new OpcodeX86("fmul", true, OD_ST6_ST));
							put(0xcf, new OpcodeX86("fmul", true, OD_ST7_ST));

							put(0xd0, sInvalidOpcode);
							put(0xd1, sInvalidOpcode);
							put(0xd2, sInvalidOpcode);
							put(0xd3, sInvalidOpcode);
							put(0xd4, sInvalidOpcode);
							put(0xd5, sInvalidOpcode);
							put(0xd6, sInvalidOpcode);
							put(0xd7, sInvalidOpcode);
							put(0xd8, sInvalidOpcode);
							put(0xd9, sInvalidOpcode);
							put(0xda, sInvalidOpcode);
							put(0xdb, sInvalidOpcode);
							put(0xdc, sInvalidOpcode);
							put(0xdd, sInvalidOpcode);
							put(0xde, sInvalidOpcode);
							put(0xdf, sInvalidOpcode);

							put(0xe0, new OpcodeX86("fsubr", true, OD_ST0_ST));
							put(0xe1, new OpcodeX86("fsubr", true, OD_ST1_ST));
							put(0xe2, new OpcodeX86("fsubr", true, OD_ST2_ST));
							put(0xe3, new OpcodeX86("fsubr", true, OD_ST3_ST));
							put(0xe4, new OpcodeX86("fsubr", true, OD_ST4_ST));
							put(0xe5, new OpcodeX86("fsubr", true, OD_ST5_ST));
							put(0xe6, new OpcodeX86("fsubr", true, OD_ST6_ST));
							put(0xe7, new OpcodeX86("fsubr", true, OD_ST7_ST));
							put(0xe8, new OpcodeX86("fsub", true, OD_ST0_ST));
							put(0xe9, new OpcodeX86("fsub", true, OD_ST1_ST));
							put(0xea, new OpcodeX86("fsub", true, OD_ST2_ST));
							put(0xeb, new OpcodeX86("fsub", true, OD_ST3_ST));
							put(0xec, new OpcodeX86("fsub", true, OD_ST4_ST));
							put(0xed, new OpcodeX86("fsub", true, OD_ST5_ST));
							put(0xee, new OpcodeX86("fsub", true, OD_ST6_ST));
							put(0xef, new OpcodeX86("fsub", true, OD_ST7_ST));

							put(0xf0, new OpcodeX86("fdivr", true, OD_ST0_ST));
							put(0xf1, new OpcodeX86("fdivr", true, OD_ST1_ST));
							put(0xf2, new OpcodeX86("fdivr", true, OD_ST2_ST));
							put(0xf3, new OpcodeX86("fdivr", true, OD_ST3_ST));
							put(0xf4, new OpcodeX86("fdivr", true, OD_ST4_ST));
							put(0xf5, new OpcodeX86("fdivr", true, OD_ST5_ST));
							put(0xf6, new OpcodeX86("fdivr", true, OD_ST6_ST));
							put(0xf7, new OpcodeX86("fdivr", true, OD_ST7_ST));
							put(0xf8, new OpcodeX86("fdiv", true, OD_ST0_ST));
							put(0xf9, new OpcodeX86("fdiv", true, OD_ST1_ST));
							put(0xfa, new OpcodeX86("fdiv", true, OD_ST2_ST));
							put(0xfb, new OpcodeX86("fdiv", true, OD_ST3_ST));
							put(0xfc, new OpcodeX86("fdiv", true, OD_ST4_ST));
							put(0xfd, new OpcodeX86("fdiv", true, OD_ST5_ST));
							put(0xfe, new OpcodeX86("fdiv", true, OD_ST6_ST));
							put(0xff, new OpcodeX86("fdiv", true, OD_ST7_ST));
						}
					});
				}
			});

			put(0xdd, new ArrayList<Map<Integer, OpcodeX86>>() {
				{
					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0x0, new OpcodeX86("fldl", true, OD_Ev));
							put(0x1, new OpcodeX86("fisttpll", true, OD_Ev));
							put(0x2, new OpcodeX86("fstl", true, OD_Ev));
							put(0x3, new OpcodeX86("fstpl", true, OD_Ev));
							put(0x4, new OpcodeX86("frstor", true, OD_Ev));
							put(0x5, sInvalidOpcode);
							put(0x6, new OpcodeX86("fnsave", true, OD_Ev));
							put(0x7, new OpcodeX86("fnstsw", true, OD_Ev));
						}
					});

					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0xc0, new OpcodeX86("ffree", true, OD_ST0));
							put(0xc1, new OpcodeX86("ffree", true, OD_ST1));
							put(0xc2, new OpcodeX86("ffree", true, OD_ST2));
							put(0xc3, new OpcodeX86("ffree", true, OD_ST3));
							put(0xc4, new OpcodeX86("ffree", true, OD_ST4));
							put(0xc5, new OpcodeX86("ffree", true, OD_ST5));
							put(0xc6, new OpcodeX86("ffree", true, OD_ST6));
							put(0xc7, new OpcodeX86("ffree", true, OD_ST7));
							put(0xc8, sInvalidOpcode);
							put(0xc9, sInvalidOpcode);
							put(0xca, sInvalidOpcode);
							put(0xcb, sInvalidOpcode);
							put(0xcc, sInvalidOpcode);
							put(0xcd, sInvalidOpcode);
							put(0xce, sInvalidOpcode);
							put(0xcf, sInvalidOpcode);

							put(0xd0, new OpcodeX86("fst", true, OD_ST0));
							put(0xd1, new OpcodeX86("fst", true, OD_ST1));
							put(0xd2, new OpcodeX86("fst", true, OD_ST2));
							put(0xd3, new OpcodeX86("fst", true, OD_ST3));
							put(0xd4, new OpcodeX86("fst", true, OD_ST4));
							put(0xd5, new OpcodeX86("fst", true, OD_ST5));
							put(0xd6, new OpcodeX86("fst", true, OD_ST6));
							put(0xd7, new OpcodeX86("fst", true, OD_ST7));
							put(0xd8, new OpcodeX86("fstp", true, OD_ST0));
							put(0xd9, new OpcodeX86("fstp", true, OD_ST1));
							put(0xda, new OpcodeX86("fstp", true, OD_ST2));
							put(0xdb, new OpcodeX86("fstp", true, OD_ST3));
							put(0xdc, new OpcodeX86("fstp", true, OD_ST4));
							put(0xdd, new OpcodeX86("fstp", true, OD_ST5));
							put(0xde, new OpcodeX86("fstp", true, OD_ST6));
							put(0xdf, new OpcodeX86("fstp", true, OD_ST7));

							// OD_ST0_ST is required according to the Intel
							// manual,but GNU assembler does not like that.
							put(0xe0, new OpcodeX86("fucom", true, OD_ST0));
							put(0xe1, new OpcodeX86("fucom", true, OD_ST1));
							put(0xe2, new OpcodeX86("fucom", true, OD_ST2));
							put(0xe3, new OpcodeX86("fucom", true, OD_ST3));
							put(0xe4, new OpcodeX86("fucom", true, OD_ST4));
							put(0xe5, new OpcodeX86("fucom", true, OD_ST5));
							put(0xe6, new OpcodeX86("fucom", true, OD_ST6));
							put(0xe7, new OpcodeX86("fucom", true, OD_ST7));

							put(0xe8, new OpcodeX86("fucomp", true, OD_ST0));
							put(0xe9, new OpcodeX86("fucomp", true, OD_ST1));
							put(0xea, new OpcodeX86("fucomp", true, OD_ST2));
							put(0xeb, new OpcodeX86("fucomp", true, OD_ST3));
							put(0xec, new OpcodeX86("fucomp", true, OD_ST4));
							put(0xed, new OpcodeX86("fucomp", true, OD_ST5));
							put(0xee, new OpcodeX86("fucomp", true, OD_ST6));
							put(0xef, new OpcodeX86("fucomp", true, OD_ST7));

							put(0xf0, sInvalidOpcode);
							put(0xf1, sInvalidOpcode);
							put(0xf2, sInvalidOpcode);
							put(0xf3, sInvalidOpcode);
							put(0xf4, sInvalidOpcode);
							put(0xf5, sInvalidOpcode);
							put(0xf6, sInvalidOpcode);
							put(0xf7, sInvalidOpcode);
							put(0xf8, sInvalidOpcode);
							put(0xf9, sInvalidOpcode);
							put(0xfa, sInvalidOpcode);
							put(0xfb, sInvalidOpcode);
							put(0xfc, sInvalidOpcode);
							put(0xfd, sInvalidOpcode);
							put(0xfe, sInvalidOpcode);
							put(0xff, sInvalidOpcode);
						}
					});
				}
			});

			put(0xde, new ArrayList<Map<Integer, OpcodeX86>>() {
				{
					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0x0, new OpcodeX86("fiadd", true, OD_Ev));
							put(0x1, new OpcodeX86("fimul", true, OD_Ev));
							put(0x2, new OpcodeX86("ficom", true, OD_Ev));
							put(0x3, new OpcodeX86("ficomp", true, OD_Ev));
							put(0x4, new OpcodeX86("fisub", true, OD_Ev));
							put(0x5, new OpcodeX86("fisubr", true, OD_Ev));
							put(0x6, new OpcodeX86("fidiv", true, OD_Ev));
							put(0x7, new OpcodeX86("fidivr", true, OD_Ev));
						}
					});

					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0xc0, new OpcodeX86("faddp", true, OD_ST0_ST));
							put(0xc1, new OpcodeX86("faddp", true, OD_ST1_ST));
							put(0xc2, new OpcodeX86("faddp", true, OD_ST2_ST));
							put(0xc3, new OpcodeX86("faddp", true, OD_ST3_ST));
							put(0xc4, new OpcodeX86("faddp", true, OD_ST4_ST));
							put(0xc5, new OpcodeX86("faddp", true, OD_ST5_ST));
							put(0xc6, new OpcodeX86("faddp", true, OD_ST6_ST));
							put(0xc7, new OpcodeX86("faddp", true, OD_ST7_ST));
							put(0xc8, new OpcodeX86("fmulp", true, OD_ST0_ST));
							put(0xc9, new OpcodeX86("fmulp", true, OD_ST1_ST));
							put(0xca, new OpcodeX86("fmulp", true, OD_ST2_ST));
							put(0xcb, new OpcodeX86("fmulp", true, OD_ST3_ST));
							put(0xcc, new OpcodeX86("fmulp", true, OD_ST4_ST));
							put(0xcd, new OpcodeX86("fmulp", true, OD_ST5_ST));
							put(0xce, new OpcodeX86("fmulp", true, OD_ST6_ST));
							put(0xcf, new OpcodeX86("fmulp", true, OD_ST7_ST));

							put(0xd0, sInvalidOpcode);
							put(0xd1, sInvalidOpcode);
							put(0xd2, sInvalidOpcode);
							put(0xd3, sInvalidOpcode);
							put(0xd4, sInvalidOpcode);
							put(0xd5, sInvalidOpcode);
							put(0xd6, sInvalidOpcode);
							put(0xd7, sInvalidOpcode);
							put(0xd8, sInvalidOpcode);
							put(0xd9, new OpcodeX86("fcompp", true, OD_NO_OPERAND));
							put(0xda, sInvalidOpcode);
							put(0xdb, sInvalidOpcode);
							put(0xdc, sInvalidOpcode);
							put(0xdd, sInvalidOpcode);
							put(0xde, sInvalidOpcode);
							put(0xdf, sInvalidOpcode);

							put(0xe0, new OpcodeX86("fsubrp", true, OD_ST0_ST));
							put(0xe1, new OpcodeX86("fsubrp", true, OD_ST1_ST));
							put(0xe2, new OpcodeX86("fsubrp", true, OD_ST2_ST));
							put(0xe3, new OpcodeX86("fsubrp", true, OD_ST3_ST));
							put(0xe4, new OpcodeX86("fsubrp", true, OD_ST4_ST));
							put(0xe5, new OpcodeX86("fsubrp", true, OD_ST5_ST));
							put(0xe6, new OpcodeX86("fsubrp", true, OD_ST6_ST));
							put(0xe7, new OpcodeX86("fsubrp", true, OD_ST7_ST));
							put(0xe8, new OpcodeX86("fsubp", true, OD_ST0_ST));
							put(0xe9, new OpcodeX86("fsubp", true, OD_ST1_ST));
							put(0xea, new OpcodeX86("fsubp", true, OD_ST2_ST));
							put(0xeb, new OpcodeX86("fsubp", true, OD_ST3_ST));
							put(0xec, new OpcodeX86("fsubp", true, OD_ST4_ST));
							put(0xed, new OpcodeX86("fsubp", true, OD_ST5_ST));
							put(0xee, new OpcodeX86("fsubp", true, OD_ST6_ST));
							put(0xef, new OpcodeX86("fsubp", true, OD_ST7_ST));

							put(0xf0, new OpcodeX86("fdivrp", true, OD_ST0_ST));
							put(0xf1, new OpcodeX86("fdivrp", true, OD_ST1_ST));
							put(0xf2, new OpcodeX86("fdivrp", true, OD_ST2_ST));
							put(0xf3, new OpcodeX86("fdivrp", true, OD_ST3_ST));
							put(0xf4, new OpcodeX86("fdivrp", true, OD_ST4_ST));
							put(0xf5, new OpcodeX86("fdivrp", true, OD_ST5_ST));
							put(0xf6, new OpcodeX86("fdivrp", true, OD_ST6_ST));
							put(0xf7, new OpcodeX86("fdivrp", true, OD_ST7_ST));
							put(0xf8, new OpcodeX86("fdivp", true, OD_ST0_ST));
							put(0xf9, new OpcodeX86("fdivp", true, OD_ST1_ST));
							put(0xfa, new OpcodeX86("fdivp", true, OD_ST2_ST));
							put(0xfb, new OpcodeX86("fdivp", true, OD_ST3_ST));
							put(0xfc, new OpcodeX86("fdivp", true, OD_ST4_ST));
							put(0xfd, new OpcodeX86("fdivp", true, OD_ST5_ST));
							put(0xfe, new OpcodeX86("fdivp", true, OD_ST6_ST));
							put(0xff, new OpcodeX86("fdivp", true, OD_ST7_ST));
						}
					});
				}
			});

			put(0xdf, new ArrayList<Map<Integer, OpcodeX86>>() {
				{
					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0x0, new OpcodeX86("fild", true, OD_Ev));
							put(0x1, new OpcodeX86("fisttp", true, OD_Ev));
							put(0x2, new OpcodeX86("fist", true, OD_Ev));
							put(0x3, new OpcodeX86("fistp", true, OD_Ev));
							put(0x4, new OpcodeX86("fbld", true, OD_Ev));
							put(0x5, new OpcodeX86("fildll", true, OD_Ev));
							put(0x6, new OpcodeX86("fbstp", true, OD_Ev));
							put(0x7, new OpcodeX86("fistpll", true, OD_Ev));
						}
					});

					add(new HashMap<Integer, OpcodeX86>() {
						{
							put(0xc0, sInvalidOpcode);
							put(0xc1, sInvalidOpcode);
							put(0xc2, sInvalidOpcode);
							put(0xc3, sInvalidOpcode);
							put(0xc4, sInvalidOpcode);
							put(0xc5, sInvalidOpcode);
							put(0xc6, sInvalidOpcode);
							put(0xc7, sInvalidOpcode);
							put(0xc8, sInvalidOpcode);
							put(0xc9, sInvalidOpcode);
							put(0xca, sInvalidOpcode);
							put(0xcb, sInvalidOpcode);
							put(0xcc, sInvalidOpcode);
							put(0xcd, sInvalidOpcode);
							put(0xce, sInvalidOpcode);
							put(0xcf, sInvalidOpcode);

							put(0xd0, sInvalidOpcode);
							put(0xd1, sInvalidOpcode);
							put(0xd2, sInvalidOpcode);
							put(0xd3, sInvalidOpcode);
							put(0xd4, sInvalidOpcode);
							put(0xd5, sInvalidOpcode);
							put(0xd6, sInvalidOpcode);
							put(0xd7, sInvalidOpcode);
							put(0xd8, sInvalidOpcode);
							put(0xd9, sInvalidOpcode);
							put(0xda, sInvalidOpcode);
							put(0xdb, sInvalidOpcode);
							put(0xdc, sInvalidOpcode);
							put(0xdd, sInvalidOpcode);
							put(0xde, sInvalidOpcode);
							put(0xdf, sInvalidOpcode);

							put(0xe0, new OpcodeX86("fnstsw", true, OD_AX));
							put(0xe1, sInvalidOpcode);
							put(0xe2, sInvalidOpcode);
							put(0xe3, sInvalidOpcode);
							put(0xe4, sInvalidOpcode);
							put(0xe5, sInvalidOpcode);
							put(0xe6, sInvalidOpcode);
							put(0xe7, sInvalidOpcode);
							put(0xe8, new OpcodeX86("fucomip", true, OD_ST_ST0));
							put(0xe9, new OpcodeX86("fucomip", true, OD_ST_ST1));
							put(0xea, new OpcodeX86("fucomip", true, OD_ST_ST2));
							put(0xeb, new OpcodeX86("fucomip", true, OD_ST_ST3));
							put(0xec, new OpcodeX86("fucomip", true, OD_ST_ST4));
							put(0xed, new OpcodeX86("fucomip", true, OD_ST_ST5));
							put(0xee, new OpcodeX86("fucomip", true, OD_ST_ST6));
							put(0xef, new OpcodeX86("fucomip", true, OD_ST_ST7));

							put(0xf0, new OpcodeX86("fcomip", true, OD_ST_ST0));
							put(0xf1, new OpcodeX86("fcomip", true, OD_ST_ST1));
							put(0xf2, new OpcodeX86("fcomip", true, OD_ST_ST2));
							put(0xf3, new OpcodeX86("fcomip", true, OD_ST_ST3));
							put(0xf4, new OpcodeX86("fcomip", true, OD_ST_ST4));
							put(0xf5, new OpcodeX86("fcomip", true, OD_ST_ST5));
							put(0xf6, new OpcodeX86("fcomip", true, OD_ST_ST6));
							put(0xf7, new OpcodeX86("fcomip", true, OD_ST_ST7));
							put(0xf8, sInvalidOpcode);
							put(0xf9, sInvalidOpcode);
							put(0xfa, sInvalidOpcode);
							put(0xfb, sInvalidOpcode);
							put(0xfc, sInvalidOpcode);
							put(0xfd, sInvalidOpcode);
							put(0xfe, sInvalidOpcode);
							put(0xff, sInvalidOpcode);
						}
					});
				}
			});

		}
	};
}
