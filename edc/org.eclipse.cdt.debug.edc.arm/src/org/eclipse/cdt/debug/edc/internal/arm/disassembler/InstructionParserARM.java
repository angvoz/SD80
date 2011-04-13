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

package org.eclipse.cdt.debug.edc.internal.arm.disassembler;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.JumpToAddress;
import org.eclipse.cdt.debug.edc.disassembler.AssemblyFormatter;
import org.eclipse.cdt.debug.edc.disassembler.CodeBufferUnderflowException;
import org.eclipse.cdt.debug.edc.disassembler.DisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler.IDisassemblerOptions;
import org.eclipse.cdt.debug.edc.internal.arm.ARMPlugin;
import org.eclipse.cdt.debug.edc.internal.arm.disassembler.DisassemblerARM.IDisassemblerOptionsARM;
import org.eclipse.core.runtime.CoreException;

/**
 * Single instruction disassembler for ARM.
 */
public class InstructionParserARM {

	public static final int DISASSEMBLER_MODE_ARM = 1;
	public static final int DISASSEMBLER_MODE_THUMB = 2;
	public static final int BIG_ENDIAN_MODE = 1;
	public static final int LITTLE_ENDIAN_MODE = 2;

	public static final int ARMv4	= 400;
	public static final int ARMv4T	= 401;
	public static final int ARMv5	= 500;
	public static final int ARMv6T	= 601;
	public static final int ARMv6	= 600;
	public static final int ARMv6T2	= 602;
	public static final int ARMv6K	= 640;
	public static final int ARMv7	= 700;

	private static final char TAB = '\t';

	private List<Integer> prefixes = new ArrayList<Integer>();
	private List<Integer> prefixesUsed = new ArrayList<Integer>();

	private int endianMode = LITTLE_ENDIAN_MODE;
	private int disassemblerMode = DISASSEMBLER_MODE_THUMB;
	private int versionMode = ARMv7;
	private Map<String, Object> disassemblerOptions = null;

	/**
	 * address of the first byte of the instruction
	 */
	private IAddress address;

	/**
	 * raw data of the instruction.
	 */
	private ByteBuffer codeBuffer;

	/**
	 * start value of position (read pointer) of the code ByteBuffer.
	 */
	final private int startPosition;

	/**
	 * result of disassembling the instruction, an
	 * {@link DisassembledInstruction} object.
	 */
	private DisassembledInstruction result = null;

	private boolean isSoleDestination;
	private boolean isSubroutineAddress;
	private IAddress jumpToAddr;
	private String addrExpression;

	/**
	 * prepare to disassemble the instruction at the current position of the
	 * given byte buffer.
	 */
	public InstructionParserARM(IAddress addr, ByteBuffer codeBuffer) {
		this.address = addr;
		this.codeBuffer = codeBuffer;
		this.startPosition = codeBuffer.position();
	}

	private void initialize(Map<String, Object> options) {
		prefixes.clear();
		prefixesUsed.clear();
		isSoleDestination = false;
		isSubroutineAddress = false;
		jumpToAddr = null;
		addrExpression = null;
		result = new DisassembledInstruction(); // start new

		// Make sure the code buffer is in big-endian
		if (codeBuffer.order() == ByteOrder.LITTLE_ENDIAN)
			codeBuffer.order(ByteOrder.BIG_ENDIAN);

		disassemblerOptions = options;

		Object mode = options.get(IDisassemblerOptionsARM.ENDIAN_MODE);
		endianMode = (mode != null) ? ((Integer) mode).intValue() : LITTLE_ENDIAN_MODE;

		mode = options.get(IDisassemblerOptionsARM.DISASSEMBLER_MODE);
		disassemblerMode
		  = (mode != null) ? ((Integer) mode).intValue() : DISASSEMBLER_MODE_THUMB;

		mode = options.get(IDisassemblerOptionsARM.VERSION_MODE);
		versionMode
		  = (mode != null) ? ((Integer) mode).intValue() : ARMv7;
	}

	/**
	 * Disassemble the given byte buffer to get one instruction. This method can
	 * be called more than once to disassemble the same instruction with
	 * different options.
	 *
	 * @param options
	 *            - disassembler options
	 * @return output of disassembling the instruction, an
	 *         {@link DisassembledInstruction} object.
	 * @throws CoreException
	 */
	public DisassembledInstruction disassemble(Map<String, Object> options)
      throws CoreException {
		initialize(options);

		String mnemonics = null;
		CoreException err = null;

		try {
			if (disassemblerMode == DISASSEMBLER_MODE_ARM) {
				mnemonics = parseARMOpcode();
			} else {
				mnemonics = parseThumbOpcode();
			}
		} catch (BufferUnderflowException e) {
			throw new CodeBufferUnderflowException(e);
		}

		// Now we are done with parsing.
		// Fill in result.
		//
		int instSize = codeBuffer.position() - startPosition;
		result.setSize(instSize);
		StringBuffer asmOutput = new StringBuffer();

		// Note we want to show "address" and "bytes" in
		// error message when error/exception occurs.

		if (checkBooleanOption(IDisassemblerOptions.MNEMONICS_SHOW_ADDRESS) || err != null) {
			asmOutput.append(AssemblyFormatter.formatForAddressColumn(address));
		}

		if (checkBooleanOption(IDisassemblerOptions.MNEMONICS_SHOW_BYTES) || err != null) {
			int currPos = codeBuffer.position();
			asmOutput.append(AssemblyFormatter.formatForByteColumn(codeBuffer, startPosition, instSize));
			codeBuffer.position(currPos);
		}

		if (err != null) {
			// create informative error message
			String msg = "Fail to disassemble this instruction (address + code-bytes): " + asmOutput.toString();
			msg += "\nCause: " + err.getMessage();
			throw ARMPlugin.newCoreException(msg);
		} else {
			// Now fill in output.
			result.setAddress(address);

			fillInJumpToAddress();

			// Append the instruction proper
			asmOutput.append(mnemonics);

			result.setMnemonics(asmOutput.toString());
		}

		return result;
	}

	private boolean checkBooleanOption(String option) {
		if (!disassemblerOptions.containsKey(option))
			return false;

		Boolean value = (Boolean) disassemblerOptions.get(option);
		return value.booleanValue();
	}

	private void fillInJumpToAddress() {
		JumpToAddress jta = null;
		if (jumpToAddr != null) {
			jta = new JumpToAddress(jumpToAddr, isSoleDestination, isSubroutineAddress);
		} else if (addrExpression != null) {
			jta = new JumpToAddress(addrExpression, isSoleDestination, isSubroutineAddress);
		}
		result.setJumpToAddress(jta);
	}

	/**
	 * Disassemble a 32-bit ARM instruction
	 * Reference manual citations (e.g., "A8.6.16") refer to sections in the ARM Architecture
	 * Reference Manual ARMv7-A and ARMv7-R Edition with errata markup
	 * @return disassembled instruction
	 */
	private String parseARMOpcode() {
		byte b0, b1, b2, b3;
		if (endianMode == BIG_ENDIAN_MODE) {
			b0 = codeBuffer.get();
			b1 = codeBuffer.get();
			b2 = codeBuffer.get();
			b3 = codeBuffer.get();
		} else {
			b3 = codeBuffer.get();
			b2 = codeBuffer.get();
			b1 = codeBuffer.get();
			b0 = codeBuffer.get();
		}
		int opcode
		  = ((b0 & 0xff) << 24) + ((b1 & 0xff) << 16) + ((b2 & 0xff) << 8) + ((b3 & 0xff));

		OpcodeARM.Index opcodeIndex = OpcodeARM.Index.invalid;
		String mnemonic = "";

		for (OpcodeARM armOpcode : OpcodeARM.arm_opcode_table) {
			int result = opcode & armOpcode.getOpcodeMask();
			if (result == armOpcode.getOpcodeResult()) {
				opcodeIndex = armOpcode.getIndex();
				mnemonic = armOpcode.getMnemonic();
				break;
			}
		}

		String instruction = "";
		String condString = "";

		String tempStr = "";
		long offset;
		int startReg;	// first reg in range
		int imm;		// immediate
		int reg;		// register
		int q;			// VFP bit to decide whether to use q or d registers
		int fld1;		// arbitrary instruction field

		switch (opcodeIndex) {
		case arm_b:					// A8.6.16 B
									// b<c> <label>
		case arm_bl:				// A8.6.23 BL, BLX (immediate)
									// bl<c> <label>
			offset = getBranchOffset(opcode);
			condString = getArmCondition(opcode);
			isSoleDestination = (condString.length() == 0); // true if unconditional b or bl
			isSubroutineAddress = (opcodeIndex == OpcodeARM.Index.arm_bl); // only bl is a subroutine call
			jumpToAddr = address.add(offset); // immediate address known
			instruction = mnemonic + condString + "\t" + jumpToAddr.toHexAddressString();
			// No pc check: not applicable
			break;

		case arm_blx__imm:			// A8.6.23 BL, BLX (immediate)
									// blx <label>
			offset = getBranchOffset(opcode) | ((opcode >> 23) & 2);
			isSoleDestination = true; // no condition
			isSubroutineAddress = true;
			jumpToAddr = address.add(offset); // immediate address known
			instruction = mnemonic + "\t" + jumpToAddr.toHexAddressString();
			// No pc check: not applicable
			break;

		case arm_blx__reg:			// A8.6.24 BLX (register)
									// blx<c> <Rm>
			condString = getArmCondition(opcode);
			tempStr = getR_0(opcode);
			instruction = mnemonic + condString + "\t" + tempStr;
			isSoleDestination = (condString.length() == 0); // true if unconditional blx
			isSubroutineAddress = true;
			addrExpression = tempStr; // branches to the address in Rm register
			// No pc check: not applicable
			break;

		case arm_bx:				// A8.6.25 BX
									// bx<c> Rm
		case arm_bxj:				// A8.6.26 BXJ
									// bxj<c> <Rm>
			condString = getArmCondition(opcode);
			tempStr = getR_0(opcode);
			instruction = mnemonic + condString + "\t" + tempStr;
			isSoleDestination = (condString.length() == 0); // true if unconditional bx
			isSubroutineAddress = false;
			addrExpression = tempStr; // branches to the address in Rm register
			// No pc check: not applicable
			break;

		case arm_adc__imm:			// A8.6.1 ADC (immediate)
									// adc{s}<c> <Rd>,<Rn>,#<const>
		case arm_adc__reg:			// A8.6.2 ADC (register)
									// adc{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		case arm_add__imm:			// A8.6.5 ADD (immediate, ARM)
									// add{s}<c> <Rd>,<Rn>,#<const>
		case arm_add__reg:			// A8.6.6 ADD (register)
									// add{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		case arm_and__imm:			// A8.6.11 AND (immediate)
									// and{s}<c> <Rd>,<Rn>,#<const>
		case arm_and__reg:			// A8.6.12 AND (register)
									// and{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		case arm_bic__imm:			// A8.6.19 BIC (immediate)
									// bic{s}<c> <Rd>,<Rn>,#<const>
		case arm_bic__reg:			// A8.6.20 BIC (register)
									// bic{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		case arm_eor__imm:			// A8.6.44 EOR (immediate)
									// eor{s}<c> <Rd>,<Rn>,#<const>
		case arm_eor__reg:			// A8.6.45 EOR (register)
									// eor{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		case arm_orr__imm:			// A8.6.113 ORR (immediate)
									// orr{s}<c> <Rd>,<Rn>,#<const>
		case arm_orr__reg:			// A8.6.114 ORR (register)
									// orr{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		case arm_rsb__imm:			// A8.6.142 RSB (immediate)
									// rsb{s}<c> <Rd>,<Rn>,#<const>
		case arm_rsb__reg:			// A8.6.143 RSB (register)
									// rsb{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		case arm_rsc__imm:			// A8.6.145 RSC (immediate)
									// rsc{s}<c> <Rd>,<Rn>,#<const>
		case arm_rsc__reg:			// A8.6.146 RSC (register)		
									// rsc{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		case arm_sbc__imm:			// A8.6.151 SBC (immediate)
									// sbc{s}<c> <Rd>,<Rn>,#<const>
		case arm_sbc__reg:			// A8.6.152 SBC (register)
									// sbc{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		case arm_sub__imm:			// A8.6.212 SUB (immediate, ARM)
									// sub{s}<c> <Rd>,<Rn>,#<const>
		case arm_sub__reg:			// A8.6.213 SUB (register)
									// sub{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
			condString = getArmCondition(opcode);
			tempStr = getR_12(opcode);
			instruction = mnemonic + getS(opcode) + condString + "\t"
					+ tempStr + "," + getR_16(opcode) + "," + getShifterOperand(opcode);

			if (tempStr.equals("pc"))
				setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			break;

		case arm_adc__rsr:			// A8.6.3 ADC (register-shifted register)
									// adc{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		case arm_add__rsr:			// A8.6.7 ADD (register-shifted register)
									// add{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		case arm_and__rsr:			// A8.6.13 AND (register-shifted register)
									// and{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		case arm_bic__rsr:			// A8.6.21 BIC (register-shifted register)
									// bic{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		case arm_eor__rsr:			// A8.6.46 EOR (register-shifted register)
									// eor{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		case arm_orr__rsr:			// A8.6.115 ORR (register-shifted register)
									// orr{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		case arm_rsb__rsr:			// A8.6.144 RSB (register-shifted register)
									// rsb{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		case arm_rsc__rsr:			// A8.6.147 RSC (register-shifted register)
									// rsc{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		case arm_sbc__rsr:			// A8.6.153 SBC (register-shifted register)
									// sbc{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		case arm_sub__rsr:			// A8.6.214 SUB (register-shifted register)
									// sub{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
			instruction = mnemonic + getS(opcode) + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getR_16(opcode) + "," + getShifterOperand(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_adr__higher:		// A8.6.10 ADR
									// adr<c> <Rd>,<label>
									// add<c> <Rd>,pc,#imm12	Alternate form
		case arm_adr__lower:		// A8.6.10 ADR
									// adr<c> <Rd>,<label>
									// sub<c> <Rd>,pc,#imm12	Alternate form
			condString = getArmCondition(opcode);
			tempStr = getR_12(opcode);
			imm = opcode & 0xfff;
			instruction = mnemonic + condString + "\t" + tempStr + ",pc,#" + getHexValue(imm);

			if (tempStr.equals("pc"))
				setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			break;

		case arm_asr__imm:			// A8.6.14 ASR (immediate)
									// asr{s}<c> <Rd>,<Rm>,#<imm>
		case arm_lsl__imm:			// A8.6.88 LSL (immediate)
									// lsl{s}<c> <Rd>,<Rm>,#<imm5>
		case arm_lsr__imm:			// A8.6.90 LSR (immediate)
									// lsr{s}<c> <Rd>,<Rm>,#<imm>
		case arm_ror__imm:			// A8.6.139 ROR (immediate)
									// ror{s}<c> <Rd>,<Rm>,#<imm>
			condString = getArmCondition(opcode);
			tempStr = getR_12(opcode);
			imm = (opcode >> 7) & 0x1f;
			if (imm == 0)
				imm = 32;
			instruction = mnemonic + getS(opcode) + condString + "\t"
					+ tempStr + "," + getR_0(opcode) + ",#" + imm;

			if (tempStr.equals("pc"))
				setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			break;

		case arm_asr__reg:			// A8.6.15 ASR (register)
									// asr{s}<c> <Rd>,<Rn>,<Rm>
		case arm_lsl__reg:			// A8.6.89 LSL (register)
									// lsl{s}<c> <Rd>,<Rn>,<Rm>
		case arm_lsr__reg:			// A8.6.91 LSR (register)
									// lsr{s}<c> <Rd>,<Rn>,<Rm>
		case arm_ror__reg:			// A8.6.140 ROR (register)
									// ror{s}<c> <Rd>,<Rn>,<Rm>
			instruction = mnemonic + getS(opcode) + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if Rd, Rn, or Rm is PC, the instruction is unpredictable
			break;

		case arm_bfc:				// A8.6.17 BFC
									// bfc<c> <Rd>,#<lsb>,#<width>
			{
				int lsb = (opcode >>  7) & 0x1f;
				int msb = (opcode >> 16) & 0x1f;
				int width = msb - lsb + 1;
				if (msb < lsb)
					width = 0;
				instruction = mnemonic + getArmCondition(opcode) + "\t"
						+ getR_12(opcode) + ",#" + lsb + ",#" + width;
			}
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_bfi:				// A8.6.18 BFI
									// bfi<c> <Rd>,<Rn>,#<lsb>,#<width>
			{
				int lsb = (opcode >>  7) & 0x1f;
				int msb = (opcode >> 16) & 0x1f;
				int width = msb - lsb + 1;
				if (msb < lsb)
					width = 0;
				instruction = mnemonic + getArmCondition(opcode) + "\t"
						+ getR_12(opcode) + "," + getR_0(opcode) + ",#" + lsb + ",#" + width;
			}
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_bkpt:				// A8.6.22 BKPT
									// bkpt #<imm16>
			instruction = mnemonic + "\t" + "#" + getHexValue((((opcode >> 4) & 0xfff0) | (opcode & 0xf)));
			// No pc check: not applicable
			break;

		case arm_cdp:				// A8.6.28 CDP, CDP2
									// cdp<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>
			mnemonic += getArmCondition(opcode);
		case arm_cdp2:				// A8.6.28 CDP, CDP2
									// cdp2<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>
			instruction = mnemonic + "\t" + getCo_cdp_operands(opcode);
			// No pc check: not applicable
			break;

		case arm_clrex:				// A8.6.30 CLREX
									// clrex
			if (ARMv6K > versionMode) {
				instruction = IDisassembler.INVALID_OPCODE;
				break;
			}
			instruction = mnemonic;
			// No pc check: not applicable
			break;

		case arm_clz:				// A8.6.31 CLZ
									// clz<c> <Rd>,<Rm>
		case arm_rbit:				// A8.6.134 RBIT
									// rbit<c> <Rd>,<Rm>
		case arm_rev:				// A8.6.135 REV
									// rev<c> <Rd>,<Rm>
		case arm_rev16:				// A8.6.136 REV16
									// rev16<c> <Rd>,<Rm>
		case arm_revsh:				// A8.6.137 REVSH
									// revsh<c> <Rd>,<Rm>
			instruction = mnemonic + getArmCondition(opcode) + "\t" + getR_12(opcode) + "," + getR_0(opcode);
			// No pc check: if Rd or Rm is PC, the instruction is unpredictable
			break;

		case arm_cmn__imm:			// A8.6.32 CMN (immediate)
									// cmn<c> <Rn>,#<const>
		case arm_cmp__imm:			// A8.6.35 CMP (immediate)
									// cmp<c> <Rn>,#<const>
		case arm_teq__imm:			// A8.6.227 TEQ (immediate)
									// teq<c> <Rn>,#<const>
		case arm_tst__imm:			// A8.6.230 TST (immediate)
									// tst<c> <Rn>,#<const>
			imm = opcode & 0xfff;
			instruction = mnemonic + getArmCondition(opcode) + "\t" + getR_16(opcode) + "," + getShifterOperand(opcode);
			// No pc check: no registers changed
			break;

		case arm_cmn__reg:			// A8.6.33 CMN (register)
									// cmn<c> <Rn>,<Rm>{,<shift>}
		case arm_cmn__rsr:			// A8.6.34 CMN (register-shifted register)
									// cmn<c> <Rn>,<Rm>,<type> <Rs>
		case arm_cmp__reg:			// A8.6.36 CMP (register)
									// cmp<c> <Rn>,<Rm>{,<shift>}
		case arm_cmp__rsr:			// A8.6.37 CMP (register-shifted register)
									// cmp<c> <Rn>,<Rm>,<type> <Rs>
		case arm_teq__reg:			// A8.6.228 TEQ (register)
									// teq<c> <Rn>,<Rm>{,<shift>}
		case arm_teq__rsr:			// A8.6.229 TEQ (register-shifted register)
									// teq<c> <Rn>,<Rm>,<type> <Rs>
		case arm_tst__reg:			// A8.6.231 TST (register)
									// tst<c> <Rn>,<Rm>{,<shift>}
		case arm_tst__rsr:			// A8.6.232 TST (register-shifted register)
									// tst<c> <Rn>,<Rm>,<type> <Rs>
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getShifterOperand(opcode);
			// No pc check: no registers changed
			break;

		case arm_cps:				// B6.1.1 CPS
									// cps #<mode>
									// cps<effect> <iflags>{,#<mode>}
			instruction = mnemonic + getCo_cps_instruction(opcode, false);
			// No pc check: not applicable
			break;

		case arm_dbg:				// A8.6.40 DBG
									// dbg<c> #<option>
			if (ARMv6T2 > versionMode)
				instruction = IDisassembler.INVALID_OPCODE;
			else if (ARMv7 > versionMode)
				instruction = "nop" + getArmCondition(opcode);
			else
				instruction = mnemonic + getArmCondition(opcode) + "\t" + "#" + (opcode & 0xf);
			// No pc check: no registers changed
			break;

		case arm_dmb:				// A8.6.41 DMB
									// dmb #<option>
		case arm_dsb:				// A8.6.42 DSB
									// dsb #<option>
			if (ARMv7 > versionMode)
				instruction = IDisassembler.INVALID_OPCODE;
			else
				instruction = mnemonic + "\t" + getDataBarrierOption(opcode);
			// No pc check: no registers changed
			break;

		case arm_isb:				// A8.6.49 ISB
									// isb #<option>
			if (ARMv7 > versionMode)
				instruction = IDisassembler.INVALID_OPCODE;
			else
				instruction = mnemonic + "\t" + getInstructionBarrierOption(opcode);
			// No pc check: no registers changed
			break;

		case arm_ldc__imm:			// A8.6.51 LDC, LDC2 (immediate)
									// ldc{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}
									// ldc{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>
									// ldc{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		case arm_ldc__lit:			// A8.6.52 LDC, LDC2 (literal)
									// ldc{l}<c> <coproc>,<CRd>,<label>
									// ldc{l}<c> <coproc>,<CRd>,[pc,#-0] Special case
									// ldc{l}<c> <coproc>,<CRd>,[pc],<option>
		case arm_ldc2__imm:			// A8.6.51 LDC, LDC2 (immediate)
									// ldc2{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}
									// ldc2{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>
									// ldc2{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		case arm_ldc2__lit:			// A8.6.52 LDC, LDC2 (literal)
									// ldc2{l}<c> <coproc>,<CRd>,<label>
									// ldc2{l}<c> <coproc>,<CRd>,[pc,#-0] Special case
									// ldc2{l}<c> <coproc>,<CRd>,[pc],<option>
		case arm_stc:				// A8.6.188 STC, STC2
									// stc{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}
									// stc{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>
									// stc{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		case arm_stc2:				// A8.6.188 STC, STC2
									// stc2{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}
									// stc2{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>
									// stc2{l}<c> <coproc>,<CRd>,[<Rn>],<option>
			instruction = mnemonic + getL(opcode) + getArmCondition(opcode) + "\t"
					+ getCoprocessor(opcode) + "," + getCR_12(opcode) + "," + getAddrModeImm8(opcode);
			// No pc check: not applicable
			break;

		case arm_ldr__imm:			// A8.6.58 LDR (immediate, ARM)
									// ldr<c> <Rt>,[<Rn>{,#+/-<imm12>}]
									// ldr<c> <Rt>,[<Rn>],#+/-<imm12>
									// ldr<c> <Rt>,[<Rn>,#+/-<imm12>]!
		case arm_strt__imm:			// A8.6.210 STRT
									// strt<c> <Rt>, [<Rn>] {, +/-<imm12>}
			condString = getArmCondition(opcode);
			tempStr = getR_12(opcode);
			instruction = mnemonic + condString + "\t" + tempStr +  "," + getAddrMode2(opcode, 24);

			if (tempStr.equals("pc"))
				setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			break;

		case arm_ldr__lit:			// A8.6.59 LDR (literal)
									// ldr<c> <Rt>,<label>
									// ldr<c> <Rt>,[pc,#+/-<imm>]	Alternative form
			condString = getArmCondition(opcode);
			tempStr = getR_12(opcode);
			imm = opcode & 0xfff;
			instruction = mnemonic + condString + "\t"
					+ tempStr + "," + getAddrModePCImm(opcode, imm);
			if (tempStr.equals("pc"))
				setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			break;

		case arm_ldrb__imm:			// A8.6.62 LDRB (immediate, ARM)
									// ldrb<c> <Rt>,[<Rn>{,#+/-<imm12>}]
									// ldrb<c> <Rt>,[<Rn>],#+/-<imm12>
									// ldrb<c> <Rt>,[<Rn>,#+/-<imm12>]!
		case arm_ldrbt__imm:		// A8.6.65 LDRBT
									// ldrbt<c> <Rt>,[<Rn>],#+/-<imm12>
		case arm_ldrt__imm:			// A8.6.65 LDRBT
									// ldrt<c> <Rt>, [<Rn>] {, #+/-<imm12>}
		case arm_str__imm:			// A8.6.194 STR (immediate, ARM)
									// str<c> <Rt>,[<Rn>{,#+/-<imm12>}]
									// str<c> <Rt>,[<Rn>],#+/-<imm12>
									// str<c> <Rt>,[<Rn>,#+/-<imm12>]!
		case arm_strb__imm:			// A8.6.197 STRB (immediate, ARM)
									// strb<c> <Rt>,[<Rn>{,#+/-<imm12>}]
									// strb<c> <Rt>,[<Rn>],#+/-<imm12>
									// strb<c> <Rt>,[<Rn>,#+/-<imm12>]!
			instruction = mnemonic + getArmCondition(opcode) + "\t" + getR_12(opcode) + "," + getAddrMode2(opcode, 24);
			// No pc check: for non-str, if Rt is PC, the instruction is unpredictable;
			//              for str, the destination is memory - not a register
			break;

		case arm_ldrb__lit:			// A8.6.63 LDRB (literal)
									// ldrb<c> <Rt>,<label>
									// ldrb<c> <Rt>,[pc,#+/-<imm>]	Alternative form
			imm = opcode & 0xfff;
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getAddrModePCImm(opcode, imm);
			// No pc check: if Rt is PC, the instruction is unpredictable;
			break;

		case arm_ldrd__imm:			// A8.6.66 LDRD (immediate)
									// ldrd<c> <Rt>,<Rt2>,[<Rn>{,#+/-<imm8>}]
									// ldrd<c> <Rt>,<Rt2>,[<Rn>],#+/-<imm8>
									// ldrd<c> <Rt>,<Rt2>,[<Rn>,#+/-<imm8>]!
			startReg = (opcode >> 12) & 0xf;
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getRegName(startReg) + "," + getRegName(startReg + 1)	+ ","
					+ getAddrModeSplitImm8(opcode);
			// No pc check: if Rt is odd or is LR (register 14), the instruction is unpredictable
			break;

		case arm_ldrd__lit:			// A8.6.67 LDRD (literal)
									// ldrd<c> <Rt>,<Rt2>,<label>
									// ldrd<c> <Rt>,<Rt2>,[pc,#+/-<imm>]	Alternative form
			imm = ((opcode >> 4) & 0xf0) | (opcode & 0xf);
			startReg = (opcode >> 12) & 0xf;
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getRegName(startReg) + "," + getRegName(startReg + 1) + ","
					+ getAddrModePCImm(opcode, imm);
			// No pc check: if Rt is odd or is LR (register 14), the instruction is unpredictable
			break;

		case arm_ldrh__imm:			// A8.6.75 LDRH (literal)
									// ldrh<c> <Rt>,[<Rn>{,#+/-<imm8>}]
									// ldrh<c> <Rt>,[<Rn>],#+/-<imm8>
									// ldrh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		case arm_ldrsb__imm:		// A8.6.81 LDRSBT
									// ldrsb<c> <Rt>,[<Rn>{,#+/-<imm8>}]
									// ldrsb<c> <Rt>,[<Rn>],#+/-<imm8>
									// ldrsb<c> <Rt>,[<Rn>,#+/-<imm8>]!
		case arm_ldrsh__imm:		// A8.6.82 LDRSH (immediate)
									// ldrsh<c> <Rt>,[<Rn>{,#+/-<imm8>}]
									// ldrsh<c> <Rt>,[<Rn>],#+/-<imm8>
									// ldrsh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		case arm_strh__imm:			// A8.6.207 STRH (immediate, ARM)
									// strh<c> <Rt>,[<Rn>{,#+/-<imm8>}]
									// strh<c> <Rt>,[<Rn>],#+/-<imm8>
									// strh<c> <Rt>,[<Rn>,#+/-<imm8>]!
			instruction = mnemonic + getArmCondition(opcode) + "\t" + getR_12(opcode)
					+ "," + getAddrModeSplitImm8(opcode);
			// No pc check: if Rt is PC, the instruction is unpredictable
			break;

		case arm_ldrh__lit:			// A8.6.75 LDRH (literal)
									// ldrh<c> <Rt>,<label>
									// ldrh<c> <Rt>,[pc,#+/-<imm>]	Alternative form
		case arm_ldrsb__lit:		// A8.6.79 LDRSB (literal)
									// ldrsb<c> <Rt>,<label>
									// ldrsb<c> <Rt>,[pc,#+/-<imm>]	Alternative form
		case arm_ldrsh__lit:		// A8.6.83 LDRSH (literal)
									// ldrsh<c> <Rt>,<label>
									// ldrsh<c> <Rt>,[pc,#+/-<imm>]	Alternative form
			imm = ((opcode >> 4) & 0xf0) | (opcode & 0xf);
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getAddrModePCImm(opcode, imm);
			// No pc check: if Rt is PC, the instruction is unpredictable
			break;

		case arm_ldm:				// A8.6.53 LDM / LDMIA / LDMFD
									// ldm<c> <Rn>{!},<registers>
		case arm_ldmda:				// A8.6.54 LDMDA / LDMFA
									// ldmda<c> <Rn>{!},<registers>
		case arm_ldmdb:				// A8.6.55 LDMDB / LDMEA
									// ldmdb<c> <Rn>{!},<registers>
		case arm_ldmib:				// A8.6.56 LDMIB / LDMED
									// ldmib<c> <Rn>{!},<registers>
		case arm_stm__regs:			// A8.6.189 STM / STMIA / STMEA
									// stm<c> <Rn>{!},<registers>
		case arm_stmda:				// A8.6.190 STMDA / STMED
									// stmda<c> <Rn>{!},<registers>
		case arm_stmdb:				// A8.6.191 STMDB / STMFD
									// stmdb<c> <Rn>{!},<registers>
		case arm_stmib:				// A8.6.192 STMIB / STMFA
									// stmib<c> <Rn>{!},<registers>
			condString = getArmCondition(opcode);
			tempStr = getRegList(opcode);
			instruction = mnemonic + condString + "\t"
					+ getR_16(opcode) + getW(opcode) + "," + tempStr;

			if (tempStr.contains("pc"))
				setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			// Note: having PC (register 15) in the list is deprecated
			break;

		case arm_ldm__exc_ret:		// B6.1.2 LDM (exception return)
									// ldm{<amode>}<c> <Rn>{!},<registers_with_pc>^
			condString = getArmCondition(opcode);
			tempStr = getRegList(opcode);
			instruction = mnemonic + getAddrMode(opcode) + condString + "\t"
					+ getR_16(opcode) + getW(opcode) + "," + tempStr + "^";

			if (tempStr.contains("pc"))
				setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			break;

		case arm_ldm__user_reg:		// B6.1.3 LDM (user registers)
									// ldm{<amode>}<c> <Rn>,<registers_without_pc>^
		case arm_stm__usr_regs:		// stm{amode}<c> <Rn>,<registers>^
									// stm{amode}<c> <Rn>,<registers>^
			instruction = mnemonic + getAddrMode(opcode) + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getRegList(opcode) + "^";
			// No pc check: not applicable
			break;

		case arm_ldr__reg:			// A8.6.60 LDR (register)
									// ldr<c> <Rt>,[<Rn>,+/-<Rm>{, <shift>}]{!}
									// ldr<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
			condString = getArmCondition(opcode);
			tempStr = getR_12(opcode);
			instruction = mnemonic + condString + "\t"
					+ tempStr + "," + getAddrMode2(opcode, 24);

			if (tempStr.equals("pc"))
				setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			break;

		case arm_ldrb__reg:			// A8.6.64 LDRB (register)
									// ldrb<c> <Rt>,[<Rn>,+/-<Rm>{, <shift>}]{!}
									// ldrb<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		case arm_ldrbt__reg:		// A8.6.65 LDRBT
									// ldrbt<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		case arm_ldrt__reg:			// A8.6.86 LDRT
									// ldrt<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		case arm_strbt__reg:		// A8.6.199 STRBT
									// strbt<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		case arm_strt__reg:			// A8.6.210 STRT
									// strt<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getAddrMode2(opcode, 24);
			// No pc check: for non-str, if Rt is PC, the instruction is unpredictable
			//              for str, the destination is memory - not a register
			break;

		case arm_ldrd__reg:			// A8.6.68 LDRD (register)
									// ldrd<c> <Rt>,<Rt2>,[<Rn>,+/-<Rm>]{!}
									// ldrd<c> <Rt>,<Rt2>,[<Rn>],+/-<Rm>
		case arm_strd__reg:			// A8.6.201 STRD (register)
									// strd<c> <Rt>,<Rt2>,[<Rn>,+/-<Rm>]{!}
									// strd<c> <Rt>,<Rt2>,[<Rn>],+/-<Rm>
			startReg = (opcode >> 12) & 0xf;
			instruction = mnemonic + getArmCondition(opcode) + "\t"	+ getRegName(startReg)
					+ "," + getRegName(startReg + 1) + "," + getAddrModeSplitImm8(opcode);
			// No pc check: if Rt is odd or is LR (register 14), the instruction is unpredictable
			break;

		case arm_ldrex:				// A8.6.69 LDREX
									// ldrex<c> <Rt>,[<Rn>]
		case arm_ldrexb:			// A8.6.70 LDREXB
									// ldrexb<c> <Rt>, [<Rn>]
		case arm_ldrexh:			// A8.6.72 LDREXH
									// ldrexh<c> <Rt>, [<Rn>]
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + ",[" + getR_16(opcode) + "]";
			// No pc check: if Rt is PC, the instruction is unpredictable
			break;

		case arm_ldrexd:			// A8.6.71 LDREXD
									// ldrexd<c> <Rt>,<Rt2>,[<Rn>]
			startReg = (opcode >> 12) & 0xf;
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getRegName(startReg) + "," + getRegName(startReg + 1) + ",[" + getR_16(opcode) + "]";
			// No pc check: if Rt is odd or is LR (register 14), the instruction is unpredictable
			break;

		case arm_ldrh__reg:			// A8.6.76 LDRH (register)
									// ldrh<c> <Rt>,[<Rn>,+/-<Rm>]{!}
									// ldrh<c> <Rt>,[<Rn>],+/-<Rm>
		case arm_ldrsb__reg:		// A8.6.80 LDRSB (register)
									// ldrsb<c> <Rt>,[<Rn>,+/-<Rm>]{!}
									// ldrsb<c> <Rt>,[<Rn>],+/-<Rm>
		case arm_ldrsh__reg:		// A8.6.85 LDRSHT
									// ldrsh<c> <Rt>,[<Rn>,+/-<Rm>]{!}
									// ldrsh<c> <Rt>,[<Rn>],+/-<Rm>
		case arm_strh__reg:			// A8.6.208 STRH (register)
									// strh<c> <Rt>,[<Rn>,+/-<Rm>]{!}
									// strh<c> <Rt>,[<Rn>],+/-<Rm>
			instruction = mnemonic + getArmCondition(opcode) + "\t"	+ getR_12(opcode)
					+ "," + getAddrModeSplitImm8(opcode);
			// No pc check: for non-str, if Rt is PC, the instruction is unpredictable;
			//              for str, the destination is memory - not a register
			break;

		case arm_ldrht__imm:		// A8.6.77 LDRHT
									// ldrht<c> <Rt>, [<Rn>] {, #+/-<imm8>}
		case arm_ldrsbt__imm:		// A8.6.81 LDRSBT
									// ldrsbt<c> <Rt>, [<Rn>] {, #+/-<imm8>}
		case arm_ldrsht__imm:		// A8.6.85 LDRSHT
									// ldrsht<c> <Rt>, [<Rn>] {, #+/-<imm8>}
		case arm_strht__imm:		// A8.6.209 STRHT
									// strht<c> <Rt>, [<Rn>] {, #+/-<imm8>}
			{
				int offsetHi = (opcode >> 4) & 0xf0;
				int offsetLo = opcode & 0xf;
				offset = offsetHi | offsetLo;
				instruction = mnemonic + getArmCondition(opcode) + "\t"
						+ getR_12(opcode) + ",[" + getR_16(opcode) + "]";
				if (offset != 0)
					instruction += ",#" + ((isBitEnabled(opcode, 23)) ? "" : "-") + getHexValue(offset);
			}
			// No pc check: for non-str, if Rt is PC, the instruction is unpredictable;
			//              for str, the destination is memory - not a register
			break;

		case arm_ldrht__reg:		// A8.6.77 LDRHT
									// ldrht<c> <Rt>, [<Rn>], +/-<Rm>
		case arm_ldrsbt__reg:		// A8.6.81 LDRSBT
									// ldrsbt<c> <Rt>, [<Rn>], +/-<Rm>
		case arm_ldrsht__reg:		// A8.6.85 LDRSHT
									// ldrsht<c> <Rt>, [<Rn>], +/-<Rm>
		case arm_strht__reg:		// A8.6.209 STRHT
									// strht<c> <Rt>, [<Rn>], +/-<Rm>
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + ",[" + getR_16(opcode)
					+ (isBitEnabled(opcode, 23) ? "]," : "],-") + getR_0(opcode);
			// No pc check: for non-str, if Rt is PC, the instruction is unpredictable;
			//              for str, the destination is memory - not a register
			break;

		case arm_mcr:				// A8.6.92 MCR, MCR2
									// mcr<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
			mnemonic += getArmCondition(opcode);
// no break!
		case arm_mcr2:				// A8.6.92 MCR, MCR2
									// mcr2<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
			instruction = mnemonic + getCo_mcr_operands(opcode);
			// No pc check: not applicable
			break;

		case arm_mcrr:				// A8.6.93 MCRR, MCRR2
									// mcrr<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
		case arm_mrrc:				// A8.6.101 MRRC, MRRC2
									// mrrc<c> <coproc>,<opc>,<Rt>,<Rt2>,<CRm>
			mnemonic += getArmCondition(opcode);
// no break!
		case arm_mcrr2:				// A8.6.93 MCRR, MCRR2
									// mcrr2<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
		case arm_mrrc2:				// A8.6.101 MRRC, MRRC2
									// mrrc2<c> <coproc>,<opc>,<Rt>,<Rt2>,<CRm>
			instruction = mnemonic + getCo_mrr_operands(opcode);
			// No pc check: not applicable
			break;

		case arm_mla:				// A8.6.94 MLA
									// mla{s}<c> <Rd>,<Rn>,<Rm>,<Ra>
			instruction = mnemonic + getS(opcode) + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode) + "," + getR_12(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_mls:				// A8.6.95 MLS
									// mls<c> <Rd>,<Rn>,<Rm>,<Ra>
		case arm_usada8:			// A8.6.254 USADA8
									// usada8<c> <Rd>,<Rn>,<Rm>,<Ra>
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode) + "," + getR_12(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_mov__imm:			// A8.6.96 MOV (immediate)
									// mov{s}<c> <Rd>,#<const>
		case arm_mvn__imm:			// A8.6.106 MVN (immediate)
									// mvn{s}<c> <Rd>,#<const>
			condString = getArmCondition(opcode);
			tempStr = getR_12(opcode);
			imm = opcode & 0xfff;
			instruction = mnemonic + getS(opcode) + condString + "\t" + tempStr + "," + getShifterOperand(opcode);

			if (tempStr.equals("pc"))
				setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			break;

		case arm_mov__reg:			// A8.6.97 MOV (register)
									// mov{s}<c> <Rd>,<Rm>
		case arm_rrx:				// A8.6.141 RRX
									// rrx{s}<c> <Rd>,<Rm>
			condString = getArmCondition(opcode);
			tempStr = getR_12(opcode);
			instruction = mnemonic + getS(opcode) + condString + "\t" + tempStr + "," + getR_0(opcode);

			if (tempStr.equals("pc"))
				setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			break;

		case arm_movw:				// A8.6.96 MOV (immediate)
									// movw<c> <Rd>,#<imm16>
		case arm_movt:				// A8.6.99 MOVT
									// movt<c> <Rd>,#<imm16>
			imm = ((opcode >> 4) & 0xf000) | (opcode & 0xfff);
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + ",#" + getHexValue(imm);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_mrc:				// A8.6.100 MRC, MRC2
									// mrc<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
			mnemonic += getArmCondition(opcode);
// no break!
		case arm_mrc2:				// A8.6.100 MRC, MRC2
									// mrc2<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
			instruction = mnemonic + getCo_mrc_operands(opcode);
			// No pc check: not applicable
			break;

		case arm_mrs:				// A8.6.102 MRS
									// mrs<c> <Rd>,<spec_reg>
			instruction = mnemonic + getArmCondition(opcode) + "\t" + getR_12(opcode)
				+ "," + getStatusReg(opcode, 22);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_msr__imm:			// A8.6.103 MSR (immediate)
									// msr<c> <spec_reg>,#<const>
		case arm_msr__reg:			// A8.6.104 MSR (register)
									// msr<c> <spec_reg>,<Rn>
			// mask<1> field (CPSR f field; APSR nzcvq bits)
			// mask<0> field (CPSR s field; APSR g bit)
			instruction = mnemonic + getArmCondition(opcode) + "\tcpsr_"
					+ (isBitEnabled(opcode, 19) ? "f" : "")
					+ (isBitEnabled(opcode, 18) ? "s" : "") + ",";
			if (isBitEnabled(opcode, 25))
				instruction += getShifterOperand(opcode);
			else
				instruction += getR_0(opcode);
			// No pc check: not applicable
			break;

		case arm_msr__sys_imm:		// B6.1.6 MSR (immediate)
									// msr<c> <spec_reg>,#<const>
		case arm_msr__sys_reg:		// B6.1.7 MSR (register)
									// msr<c> <spec_reg>,<Rn>
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getStatusReg(opcode, 22) + getStatusRegFields(opcode, 16);
			if (isBitEnabled(opcode, 25))
				instruction += "," + getShifterOperand(opcode);
			else
				instruction += "," + getR_0(opcode);
			break;

		case arm_mul:				// A8.6.105 MUL
									// mul{s}<c> <Rd>,<Rn>,<Rm>
			instruction = mnemonic + getS(opcode) + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if Rd, Rn, or Rm is PC, the instruction is unpredictable
			break;

		case arm_mvn__reg:			// A8.6.107 MVN (register)
									// mvn{s}<c> <Rd>,<Rm>{,<shift>}
			condString = getArmCondition(opcode);
			tempStr = getR_12(opcode);
			instruction = mnemonic + getS(opcode) + condString + "\t"
					+ tempStr + "," + getShifterOperand(opcode);

			if (tempStr.equals("pc"))
				setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			break;

		case arm_mvn__rsr:			// A8.6.108 MVN (register-shifted register)
									// mvn{s}<c> <Rd>,<Rm>,<type> <Rs>
			instruction = mnemonic + getS(opcode) + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getShifterOperand(opcode);
			// No pc check: if Rn, Rm, or Rs is PC, the instruction is unpredictable
			break;

		case arm_nop:				// A8.6.110 NOP
									// nop<c>
			if (ARMv6T2 > versionMode)
				instruction = IDisassembler.INVALID_OPCODE;
			else
				instruction = mnemonic + getArmCondition(opcode);
			// No pc check: not applicable
			break;

		case arm_pkh:				// A8.6.116 PKH
									// pkhbt<c> <Rd>,<Rn>,<Rm>{,lsl #<imm>}
									// pkhtb<c> <Rd>,<Rn>,<Rm>{,asr #<imm>}
			imm = (opcode >> 7) & 0x1f;
			// based on tb field (bit 6)
			if (isBitEnabled(opcode, 6)) {
				if (imm == 0)
					imm = 32;
				instruction = mnemonic + "tb" + getArmCondition(opcode) + "\t"
						+ getR_12(opcode) + "," + getR_16(opcode) + "," + getR_0(opcode)
						+ ",asr #" + imm;
			} else {
				instruction = mnemonic + "bt" + getArmCondition(opcode) + "\t"
						+ getR_12(opcode) + "," + getR_16(opcode) + "," + getR_0(opcode);
				if (imm != 0)
					instruction += ",lsl #" + imm;
			}
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_pld__lit:			// A8.6.118 PLD (literal)
									// pld <label>
									// pld [pc,#+/-<imm>]	Alternative form
			imm = opcode & 0xfff;
			instruction = mnemonic + "\t" + getAddrModePCImm(opcode, imm);
			// No pc check: not applicable
			break;

		case arm_pld__imm:			// A8.6.117 PLD, PLDW (immediate)
									// pld{w} [<Rn>,#+/-<imm12>]
			instruction = mnemonic + (isBitEnabled(opcode, 22) ? "\t" : "w\t") + getAddrMode2(opcode, 24);
			// No pc check: not applicable
			break;

		case arm_pld__reg:			// A8.6.119 PLD, PLDW (register)
									// pld{w}<c> [<Rn>,+/-<Rm>{, <shift>}]
			mnemonic += (isBitEnabled(opcode, 22) ? "\t" : "w\t") + getArmCondition(opcode);
			instruction = mnemonic + "\t" + getAddrMode2(opcode, 24);
			// No pc check: not applicable
			break;

		case arm_pli__imm_lit:		// A8.6.120 PLI (immediate, literal)
									// pli [<Rn>,#+/-<imm12>]
									// pli <label>
									// pli [pc,#+/-<imm>]	Alternative form
			imm = opcode & 0xfff;
			instruction = mnemonic + "\t"
					+ (getR_16(opcode).equals("pc")
						? getAddrModePCImm(opcode, imm)
						: getAddrMode2(opcode, 20)); // picked bit 20 because it is 1
			// No pc check: not applicable
			break;

		case arm_pli__reg:			// A8.6.121 PLI (register)
									// pli [<Rn>,+/-<Rm>{, <shift>}]
			instruction = mnemonic + "\t" + getAddrMode2(opcode, 20); // picked bit 20 because it is 1
			// No pc check: not applicable
			break;

		case arm_pop__regs:			// A8.6.122 POP
									// pop<c> <registers> <registers> has more than one register
			condString = getArmCondition(opcode);
			tempStr = getRegList(opcode);
			instruction = mnemonic + condString + "\t" + tempStr;

			if (tempStr.contains("pc"))
				setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			break;

		case arm_pop__reg:			// A8.6.122 POP
									// pop<c> <registers> <registers> has one register, <Rt>
			condString = getArmCondition(opcode);
			tempStr = getR_12(opcode);
			instruction = mnemonic + condString + "\t{" + tempStr + "}";
			
			if (tempStr.equals("pc"))
			setDefaultPCJumpProperties(condString.length() == 0); // true if unconditional
			break;

		case arm_push__reg:			// A8.6.123 PUSH
									// push<c> <registers> <registers> has one register, <Rt>
			instruction = mnemonic + getArmCondition(opcode) + "\t{" + getR_12(opcode) + "}";
			// No pc check: not applicable
			break;

		case arm_push__regs:		// A8.6.123 PUSH
									// push<c> <registers> <registers> has more than one register
			instruction = mnemonic + getArmCondition(opcode) + "\t" + getRegList(opcode);
			// No pc check: not applicable
			break;

		case arm_qadd:				// A8.6.124 QADD
									// qadd<c> <Rd>,<Rm>,<Rn>
		case arm_qdadd:				// A8.6.128 QDADD
									// qdadd<c> <Rd>,<Rm>,<Rn>
		case arm_qdsub:				// A8.6.129 QDSUB
									// qdsub<c> <Rd>,<Rm>,<Rn>
		case arm_qsub:				// A8.6.131 QSUB
									// qsub<c> <Rd>,<Rm>,<Rn>
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getR_0(opcode) + ","	+ getR_16(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

//		case arm_qadd16:			// A8.6.125 QADD16
//									// qadd16<c> <Rd>,<Rn>,<Rm>
//		case arm_qadd8:				// A8.6.126 QADD8
//									// qadd8<c> <Rd>,<Rn>,<Rm>
//		case arm_qasx:				// A8.6.127 QASX
//									// qasx<c> <Rd>,<Rn>,<Rm>
//		case arm_qsax:				// A8.6.130 QSAX
//									// qsax<c> <Rd>,<Rn>,<Rm>
//		case arm_qsub16:			// A8.6.132 QSUB16
//									// qsub16<c> <Rd>,<Rn>,<Rm>
//		case arm_qsub8:				// A8.6.133 QSUB8
//									// qsub8<c> <Rd>,<Rn>,<Rm>
//		case arm_sadd16:			// A8.6.148 SADD16
//									// sadd16<c> <Rd>,<Rn>,<Rm>
//		case arm_sadd8:				// A8.6.149 SADD8
//									// sadd8<c> <Rd>,<Rn>,<Rm>
//		case arm_sasx:				// A8.6.150 SASX
//									// sasx<c> <Rd>,<Rn>,<Rm>
//		case arm_shadd16:			// A8.6.159 SHADD16
//									// shadd16<c> <Rd>,<Rn>,<Rm>
//		case arm_shadd8:			// A8.6.160 SHADD8
//									// shadd8<c> <Rd>,<Rn>,<Rm>
//		case arm_shasx:				// A8.6.161 SHASX
//									// shasx<c> <Rd>,<Rn>,<Rm>
//		case arm_shsax:				// A8.6.162 SHSAX
//									// shsax<c> <Rd>,<Rn>,<Rm>
//		case arm_shsub16:			// A8.6.163 SHSUB16
//									// shsub16<c> <Rd>,<Rn>,<Rm>
//		case arm_shsub8:			// A8.6.164 SHSUB8
//									// shsub8<c> <Rd>,<Rn>,<Rm>
//		case arm_ssax:				// A8.6.185 SSAX
//									// ssax<c> <Rd>,<Rn>,<Rm>
//		case arm_ssub16:			// A8.6.186 SSUB16
//									// ssub16<c> <Rd>,<Rn>,<Rm>
//		case arm_ssub8:				// A8.6.187 SSUB8
//									// ssub8<c> <Rd>,<Rn>,<Rm>
//		case arm_uadd16:			// A8.6.233 UADD16
//									// uadd16<c> <Rd>,<Rn>,<Rm>
//		case arm_uadd8:				// A8.6.234 UADD8
//									// uadd8<c> <Rd>,<Rn>,<Rm>
//		case arm_uasx:				// A8.6.235 UASX
//									// uasx<c> <Rd>,<Rn>,<Rm>
//		case arm_uhadd16:			// A8.6.238 UHADD16
//									// uhadd16<c> <Rd>,<Rn>,<Rm>
//		case arm_uhadd8:			// A8.6.239 UHADD8
//									// uhadd8<c> <Rd>,<Rn>,<Rm>
//		case arm_uhasx:				// A8.6.240 UHASX
//									// uhasx<c> <Rd>,<Rn>,<Rm>
//		case arm_uhsax:				// A8.6.241 UHSAX
//									// uhsax<c> <Rd>,<Rn>,<Rm>
//		case arm_uhsub16:			// A8.6.242 UHSUB16
//									// uhsub16<c> <Rd>,<Rn>,<Rm>
//		case arm_uhsub8:			// A8.6.243 UHSUB8
//									// uhsub8<c> <Rd>,<Rn>,<Rm>
//		case arm_uqadd16:			// A8.6.247 UQADD16
//									// uqadd16<c> <Rd>,<Rn>,<Rm>
//		case arm_uqadd8:			// A8.6.248 UQADD8
//									// uqadd8<c> <Rd>,<Rn>,<Rm>
//		case arm_uqasx:				// A8.6.249 UQASX
//									// uqasx<c> <Rd>,<Rn>,<Rm>
//		case arm_uqsax:				// A8.6.250 UQSAX
//									// uqsax<c> <Rd>,<Rn>,<Rm>
//		case arm_uqsub16:			// A8.6.251 UQSUB16
//									// uqsub16<c> <Rd>,<Rn>,<Rm>
//		case arm_uqsub8:			// A8.6.252 UQSUB8
//									// uqsub8<c> <Rd>,<Rn>,<Rm>
//		case arm_usax:				// A8.6.257 USAX
//									// usax<c> <Rd>,<Rn>,<Rm>
//		case arm_usub16:			// A8.6.258 USUB16
//									// usub16<c> <Rd>,<Rn>,<Rm>
//		case arm_usub8:				// A8.6.259 USUB8
//									// usub8<c> <Rd>,<Rn>,<Rm>
		case arm__r_dnm_math:
			switch (opcode >> 20 & 7) {
				case 1:	mnemonic = "s";		break;
				case 2:	mnemonic = "q";		break;
				case 3:	mnemonic = "sh";	break;
				case 5:	mnemonic = "u";		break;
				case 6:	mnemonic = "uq";	break;
				case 7:	mnemonic = "uh";	break;
				default:	mnemonic = "";
			}
			switch (opcode >> 5 & 7) {
				case 0:	mnemonic += "add16";	break;
				case 1:	mnemonic += "asx";		break;
				case 2:	mnemonic += "sax";		break;
				case 3:	mnemonic += "sub16";	break;
				case 4: mnemonic += "add8";		break;
				case 7: mnemonic += "sub8";		break;
			}
// no break!
		case arm_sel:
			// sel<c> <Rd>,<Rn>,<Rm>
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getR_16(opcode) + "," + getR_0(opcode);
			// No pc check: if Rd, Rn, or Rm is PC, the instruction is unpredictable
			break;

		case arm_rfe:				// B6.1.8 RFE
									// rfe{<amode>} <Rn>{!}
			mnemonic += getAddrMode(opcode);
			instruction = mnemonic + "\t" + getR_16(opcode) + getW(opcode);
			// loads the PC from memory
			// Is this right?
			setDefaultPCJumpProperties(true); // true if unconditional
			break;

		case arm_sbfx:				// A8.6.154 SBFX
									// sbfx<c> <Rd>,<Rn>,#<lsb>,#<width>
		case arm_ubfx:				// A8.6.236 UBFX
									// ubfx<c> <Rd>,<Rn>,#<lsb>,#<width>
			{
				int lsb = (opcode >> 7) & 0x1f;
				int width = ((opcode >> 16) & 0x1f) + 1;
				instruction = mnemonic + getArmCondition(opcode) + "\t"
						+ getR_12(opcode) + "," + getR_0(opcode) + ",#" + lsb + ",#" +width;
			}
			// No pc check: for non-str, if Rd is PC, the instruction is unpredictable;
			//              for str, the destination is memory - not a register
			break;

		case arm_setend:			// A8.6.157 SETEND
									// setend <endian_specifier> Cannot be conditional
			instruction = mnemonic + "\t";
			if ((opcode & (1 << 9)) == 0)
				instruction = instruction + "le";
			else
				instruction = instruction + "be";
			break;

		case arm_sev:				// A8.6.158 SEV
									// sev<c>
		case arm_wfe:				// A8.6.411 WFE
									// wfe<c>
		case arm_wfi:				// A8.6.412 WFI
									// wfi<c>
		case arm_yield:				// A8.6.413 YIELD
									// yield<c>
			if (ARMv6T2 > versionMode)
				instruction = IDisassembler.INVALID_OPCODE;
			else if (ARMv6K > versionMode)
				instruction = "nop" + getArmCondition(opcode);
			else
				instruction = mnemonic + getArmCondition(opcode);
			// No pc check: not applicable
			break;

		case arm_smc:				// B6.1.9 SMC (previously SMI)
									// smc<c> #<imm4>
			instruction = mnemonic + getArmCondition(opcode) + "\t#" + getHexValue((opcode & 0xf));
			// No pc check: not applicable
			break;

		case arm_smla:				// A8.6.166 SMLABB, SMLABT, SMLATB, SMLATT
									// smla<x><y><c> <Rd>,<Rn>,<Rm>,<Ra>
			instruction = mnemonic + getBorT(opcode, 5) + getBorT(opcode, 6) + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode) + "," + getR_12(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_smlad:				// A8.6.167 SMLAD
									// smlad{x}<c> <Rd>,<Rn>,<Rm>,<Ra>
		case arm_smlsd:				// A8.6.172 SMLSD
									// smlsd{x}<c> <Rd>,<Rn>,<Rm>,<Ra>
			instruction = mnemonic + getX(opcode, 5) + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode) + "," + getR_12(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_smlal:				// A8.6.168 SMLAL
									// smlal{s}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
			instruction = mnemonic + getS(opcode) + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + ","	+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if RdLo or RdHi is PC, the instruction is unpredictable
			break;

		case arm_smlalxy:			// A8.6.169 SMLALBB, SMLALBT, SMLALTB, SMLALTT
									// smlal<x><y><c> <RdLo>,<RdHi>,<Rn>,<Rm>
			instruction = mnemonic + getBorT(opcode, 5) + getBorT(opcode, 6) + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + ","	+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if RdLo or RdHi is PC, the instruction is unpredictable
			break;

		case arm_smlald:			// A8.6.170 SMLALD
									// smlald{x}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		case arm_smlsld:			// A8.6.173 SMLSLD
									// smlsld{x}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
			instruction = mnemonic + getX(opcode, 5) + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + ","	+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_smlaw:				// A8.6.171 SMLAWB, SMLAWT
									// smlaw<y><c> <Rd>,<Rn>,<Rm>,<Ra>
			instruction = mnemonic + getBorT(opcode, 6) + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode) + "," + getR_12(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_smmla:				// A8.6.174 SMMLA
									// smmla{r}<c> <Rd>,<Rn>,<Rm>,<Ra>
		case arm_smmls:				// A8.6.175 SMMLS
									// smmls{r}<c> <Rd>,<Rn>,<Rm>,<Ra>
			instruction = mnemonic + getR(opcode, 5) + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode) + "," + getR_12(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_smmul:				// A8.6.176 SMMUL
									// smmul{r}<c> <Rd>,<Rn>,<Rm>
			instruction = mnemonic + getR(opcode, 5) + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_smuad:				// A8.6.177 SMUAD
									// smuad{x}<c> <Rd>,<Rn>,<Rm>
		case arm_smusd:				// A8.6.181 SMUSD
									// smusd{x}<c> <Rd>,<Rn>,<Rm>
			instruction = mnemonic + getX(opcode, 5) + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_smul:				// A8.6.178 SMULBB, SMULBT, SMULTB, SMULTT
									// smul<x><y><c> <Rd>,<Rn>,<Rm>
			instruction = mnemonic + getBorT(opcode, 5) + getBorT(opcode, 6) + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_smull:				// A8.6.179 SMULL
									// smull{s}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
			instruction = mnemonic + getS(opcode) + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + ","	+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if RdLo or RdHi is PC, the instruction is unpredictable
			break;

		case arm_smulw:				// A8.6.180 SMULWB, SMULWT
									// smulw<y><c> <Rd>,<Rn>,<Rm>
			instruction = mnemonic + getBorT(opcode, 6) + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_srs:				// B6.1.10 SRS
									// srs{<amode>} sp{!},#<mode>
			instruction = mnemonic + getAddrMode(opcode) + "\t"
					+ "sp" + getW(opcode) + ",#" + getHexValue((opcode & 0x1f));
			break;

		case arm_ssat:				// A8.6.183 SSAT
									// ssat<c> <Rd>,#<imm>,<Rn>{,<shift>}
		case arm_usat:				// A8.6.255 USAT
									// usat<c> <Rd>,#<imm5>,<Rn>{,<shift>}
			imm = ((opcode >> 16) & 0x1f);
			if ((opcode & (1 << 22)) == 0)
				imm++;
				
			if (((opcode >> 6) & 0x3f) != 0) {
				int shiftCnt = (opcode >> 7) & 0x1f;
				if ((opcode & (1 << 6)) == 0)
					tempStr = ",lsl #" + shiftCnt;
				else {
					if (shiftCnt == 0)
						shiftCnt = 32;
					tempStr = ",asr #" + shiftCnt;
				}
			}
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + ",#" + imm + "," + getR_0(opcode) + tempStr;
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_ssat16:			// A8.6.184 SSAT16
									// ssat16<c> <Rd>,#<imm>,<Rn>
			imm = ((opcode >> 16) & 0xf) + 1;
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + ",#" + imm + "," + getR_0(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_str__reg:			// A8.6.195 STR (register)
									// str<c> <Rt>,[<Rn>,+/-<Rm>{, <shift>}]{!}
									// str<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		case arm_strb__reg:			// A8.6.198 STRB (register)
									// strb<c> <Rt>,[<Rn>,+/-<Rm>{, <shift>}]{!}
									// strb<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		case arm_strbt__imm:		// A8.6.199 STRBT
									// strbt<c> <Rt>,[<Rn>],#+/-<imm12>
			mnemonic += getArmCondition(opcode);
			instruction = mnemonic + "\t" + getR_12(opcode) +  "," + getAddrMode2(opcode, 24);
			// No pc check: if Rt is PC, the instruction is unpredictable;
			break;

		case arm_strd__imm:			// A8.6.200 STRD (immediate)
									// strd<c> <Rt>,<Rt2>,[<Rn>{,#+/-<imm8>}]
									// strd<c> <Rt>,<Rt2>,[<Rn>],#+/-<imm8>
									// strd<c> <Rt>,<Rt2>,[<Rn>,#+/-<imm8>]!
			reg = (opcode >> 12) & 0xf;
			instruction = mnemonic + getArmCondition(opcode) + "\t"	+ getRegName(reg)
					+ "," + getRegName(reg + 1) + "," + getAddrModeSplitImm8(opcode);
			// No pc check: if Rt is odd or is LR (register 14), the instruction is unpredictable
			break;

		case arm_strex:				// A8.6.202 STREX
									// strex<c> <Rd>,<Rt>,[<Rn>]
		case arm_strexb:			// A8.6.203 STREXB
									// strexb<c> <Rd>,<Rt>,[<Rn>]
		case arm_strexh:			// A8.6.205 STREXH
									// strexh<c> <Rd>,<Rt>,[<Rn>]
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getR_0(opcode) + ",[" + getR_16(opcode) + "]";
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_strexd:			// A8.6.204 STREXD
									// strexd<c> <Rd>,<Rt>,<Rt2>,[<Rn>]
			startReg = opcode & 0xf;
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getRegName(startReg) + ","
					+ getRegName(startReg + 1) + ",[" + getR_16(opcode) + "]";
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_svc:				// A8.6.218 SVC (previously SWI)
									// svc<c> #<imm24>
			instruction = mnemonic + getArmCondition(opcode) + "\t" + getImmediate24(opcode);
			// No pc check: the destination is memory - not a register
			break;

		case arm_swp:				// A8.6.219 SWP, SWPB
									// swp{b}<c> <Rt>,<Rt2>,[<Rn>]
			instruction = mnemonic + getB(opcode) + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getR_0(opcode) + ",[" + getR_16(opcode) + "]";
			// No pc check: if Rt or Rt2 is PC, the instruction is unpredictable
			break;

		case arm_sxtab:				// A8.6.220 SXTAB
									// sxtab<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		case arm_sxtab16:			// A8.6.221 SXTAB16
									// sxtab16<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		case arm_sxtah:				// A8.6.222 SXTAH
									// sxtah<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		case arm_uxtab:				// A8.6.260 UXTAB
									// uxtab<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		case arm_uxtab16:			// A8.6.261 UXTAB16
									// uxtab16<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		case arm_uxtah:				// A8.6.262 UXTAH
									// uxtah<c> <Rd>,<Rn>,<Rm>{,<rotation>}
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getR_16(opcode) + "," + getR_0(opcode)
					+ getRotationOperand(opcode, 10);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_sxtb:				// A8.6.223 SXTB
									// sxtb<c> <Rd>,<Rm>{,<rotation>}
		case arm_sxtb16:			// A8.6.224 SXTB16
									// sxtb16<c> <Rd>,<Rm>{,<rotation>}
		case arm_sxth:				// A8.6.225 SXTH
									// sxth<c> <Rd>,<Rm>{,<rotation>}
		case arm_uxtb:				// A8.6.263 UXTB
									// uxtb<c> <Rd>,<Rm>{,<rotation>}
		case arm_uxtb16:			// A8.6.264 UXTB16
									// uxtb16<c> <Rd>,<Rm>{,<rotation>}
		case arm_uxth:				// A8.6.265 UXTH
									// uxth<c> <Rd>,<Rm>{,<rotation>}
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getR_0(opcode)
					+ getRotationOperand(opcode, 10);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_umaal:				// A8.6.244 UMAAL
									// umaal<c> <RdLo>,<RdHi>,<Rn>,<Rm>
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + ","	+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if RdLo or RdHi is PC, the instruction is unpredictable
			break;

		case arm_umlal:				// A8.6.245 UMLAL
									// umlal{s}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		case arm_umull:				// A8.6.246 UMULL
									// umull{s}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
			instruction = mnemonic + getS(opcode) + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + ","	+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if RdLo or RdHi is PC, the instruction is unpredictable
			break;

		case arm_usad8:				// A8.6.253 USAD8
									// usad8<c> <Rd>,<Rn>,<Rm>
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_16(opcode) + "," + getR_0(opcode) + "," + getR_8(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_usat16:			// A8.6.256 USAT16
									// usat16<c> <Rd>,#<imm4>,<Rn>
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + ",#" + ((opcode >> 16) & 0xf) + "," + getR_0(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case arm_undefined:
			instruction = mnemonic;
			// No pc check: not applicable
			break;

			// VFP instructions

		case arm_vhadd_vhsub:		// A8.6.306 VHADD, VHSUB
									// vhadd<c> <Qd>, <Qn>, <Qm>
									// vhadd<c> <Dd>, <Dn>, <Dm>
									// vhsub<c> <Qd>, <Qn>, <Qm>
									// vhsub<c> <Dd>, <Dn>, <Dm>
			mnemonic += isBitEnabled(opcode, 9) ? "sub" : "add";
		case arm_vaba:				// A8.6.266 VABA, VABAL
									// vaba<c>.<dt> <Qd>, <Qn>, <Qm>
									// vaba<c>.<dt> <Dd>, <Dn>, <Dm>
		case arm_vabd__int:			// A8.6.267 VABD, VABDL (integer)
									// vabd<c>.<dt> <Qd>, <Qn>, <Qm>
									// vabd<c>.<dt> <Dd>, <Dn>, <Dm>
		case arm_vcge__reg_int:		// A8.6.282 VCGE (register)
									// vceq<c>.<dt> <Qd>, <Qn>, <Qm>
									// vceq<c>.<dt> <Dd>, <Dn>, <Dm>
		case arm_vcgt__reg_int:		// A8.6.284 VCGT (register)
									// vcgt<c>.<dt> <Qd>, <Qn>, <Qm>
									// vcgt<c>.<dt> <Dd>, <Dn>, <Dm>
		case arm_vqadd:				// A8.6.357 VQADD
									// vqadd<c>.<dt> <Qd>,<Qn>,<Qm>
									// vqadd<c>.<dt> <Dd>,<Dn>,<Dm>
		case arm_vrhadd:			// A8.6.374 VRHADD
									// vrhadd<c> <Qd>, <Qn>, <Qm>
									// vrhadd<c> <Dd>, <Dn>, <Dm>
		case arm_vqsub:				// A8.6.369 VQSUB
									// vqsub<c>.<type><size> <Qd>, <Qn>, <Qm>
									// vqsub<c>.<type><size> <Dd>, <Dn>, <Dm>
			instruction = mnemonic + getVFPSorUDataType(opcode, 24) + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vqdml__vec:		// A8.6.358 VQDMLAL, VQDMLSL
									// vqd<op><c>.<dt> <Qd>,<Dn>,<Dm>
									// bit24 == 0, so can use getVFPSorUDataType
			mnemonic += isBitEnabled(opcode, 9) ? "sl" : "al";
			// can use getVFPSorUDataType() because bit 24 is always '0'
// no break!
		case arm_vabal:				// A8.6.266 VABA, VABAL
									// vabal<c>.<dt> <Qd>,<Dn>,<Dm>
		case arm_vabdl:				// A8.6.267 VABD, VABDL (integer)
									// vabdl<c>.<dt> <Qd>,<Dn>,<Dm>
		case arm_vqdmull__vec:		// A8.6.360 VQDMULL
									// vqdmull<c>.<dt> <Qd>,<Dn>,<Dm>
									// bit24 == 0, so can use getVFPSorUDataType
			instruction = mnemonic + getVFPSorUDataType(opcode, 24)
					+ TAB + getVFPQdDnDmRegs(opcode);
			// No pc check: not applicable
			break;
			
		case arm_vabd__f32:			// A8.6.268 VABD (floating-point)
									// vabd<c>.f32 <Qd>, <Qn>, <Qm>
									// vabd<c>.f32 <Dd>, <Dn>, <Dm>
		case arm_vadd__f32:			// A8.6.272 VADD (floating-point)
									// vadd<c>.f32 <Qd>, <Qn>, <Qm>
									// vadd<c>.f32 <Dd>, <Dn>, <Dm>
		case arm_vceq__reg_f32:		// A8.6.280 VCEQ (register)
									// vceq<c>.f32 <Qd>, <Qn>, <Qm>
									// vceq<c>.f32 <Dd>, <Dn>, <Dm>
		case arm_vcge__reg_f32:		// A8.6.282 VCGE (register)
									// vcge<c>.f32 <Qd>, <Qn>, <Qm>
									// vcge<c>.f32 <Dd>, <Dn>, <Dm>
		case arm_vcgt__reg_f32:		// A8.6.284 VCGT (register)
									// vcgt<c>.f32 <Qd>, <Qn>, <Qm>
									// vcgt<c>.f32 <Dd>, <Dn>, <Dm>
		case arm_vmul_f32:			// A8.6.338 VMUL (floating-point)
									// vmul<c>.f32 <Qd>, <Qn>, <Qm>
									// vmul<c>.f32 <Dd>, <Dn>, <Dm>
		case arm_vpadd__f32:		// A8.6.350 VPADD (floating-point)
									// vpadd<c>.f32 <Dd>, <Dn>, <Dm>
		case arm_vrecps:			// A8.6.372 VRECPS
									// vrecps<c>.f32 <Qd>, <Qn>, <Qm>
									// vrecps<c>.f32 <Dd>, <Dn>, <Dm>
		case arm_vrsqrts:			// A8.6.379 VRSQRTS
									// vrsqrts<c>.f32 <Qd>, <Qn>, <Qm>
									// vrsqrts<c>.f32 <Dd>, <Dn>, <Dm>
		case arm_vsub__f32:			// A8.6.402 VSUB (floating-point)
									// vsub<c>.f32 <Qd>, <Qn>, <Qm>
									// vsub<c>.f32 <Dd>, <Dn>, <Dm>
			instruction = mnemonic + ".f32\t" + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vabs:				// A8.6.269 VABS
									// vabs<c>.<dt> <Qd>, <Qm>
									// vabs<c>.<dt> <Dd>, <Dm>
		case arm_vneg:				// A8.6.342 VNEG
									// vneg<c>.<dt> <Qd>, <Qm>
									// vneg<c>.<dt> <Dd>, <Dm>
			instruction = mnemonic + getVFPSorUorFQorDdmOperands(opcode, 10, 4); // chose bit 4 because it is 0
			// No pc check: not applicable
			break;

		case arm_vabs__f:			// A8.6.269 VABS
									// vabs<c>.f64 <Dd>, <Dm>
									// vabs<c>.f32 <Sd>, <Sm>
		case arm_vmov__reg_f:		// A8.6.327 VMOV (register)
									// vmov<c>.f64 <Dd>, <Dm>
									// vmov<c>.f32 <Sd>, <Sm>
		case arm_vneg__f:			// A8.6.342 VNEG
									// vneg<c>.f64 <Dd>, <Dm>
									// vneg<c>.f32 <Sd>, <Sm>
		case arm_vsqrt:				// A8.6.388 VSQRT
									// vsqrt<c>.f64 <Dd>, <Dm>
									// vsqrt<c>.f32 <Sd>, <Sm>
			instruction = mnemonic + getArmCondition(opcode) + getVFPSzF64F32dmOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vacge_vacgt:		// A8.6.270 VACGE, VACGT, VACLE, VACLT
									// vacge<c>.f32 <Qd>, <Qn>, <Qm>
									// vacge<c>.f32 <Dd>, <Dn>, <Dm>
									// vacgt<c>.f32 <Qd>, <Qn>, <Qm>
									// vacgt<c>.f32 <Dd>, <Dn>, <Dm>
			instruction = mnemonic + getVFP_vacge_vacgt(opcode);
			// No pc check: not applicable
			break;

		case arm_vadd__int:			// A8.6.271 VADD (integer)
									// vadd<c>.<dt> <Qd>, <Qn>, <Qm>
									// vadd<c>.<dt> <Dd>, <Dn>, <Dm>
		case arm_vceq__reg_int:		// A8.6.280 VCEQ (register)
									// vceq<c>.<dt> <Qd>, <Qn>, <Qm>
									// vceq<c>.<dt> <Dd>, <Dn>, <Dm>
		case arm_vpadd__int:		// A8.6.349 VPADD (integer)
									// vpadd<c>.<dt> <Dd>, <Dn>, <Dm>
		case arm_vsub__int:			// A8.6.401 VSUB (integer)
									// vsub<c>.<dt> <Qd>, <Qn>, <Qm>
									// vsub<c>.<dt> <Dd>, <Dn>, <Dm>
			instruction = mnemonic + getVFPIDataTypeQorDdnmOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vnml:				// A8.6.343 VNMLA, VNMLS, VNMUL
									// vnmla<c>.f64 <Dd>, <Dn>, <Dm>
									// vnmla<c>.f32 <Sd>, <Sn>, <Sm>
									// vnmls<c>.f64 <Dd>, <Dn>, <Dm>
									// vnmls<c>.f32 <Sd>, <Sn>, <Sm>
			mnemonic += isBitEnabled(opcode, 6) ? 'a' : 's';
// no break!
		case arm_vadd__fp_f:		// A8.6.272 VADD (floating-point)
									// vadd<c>.f64 <Dd>, <Dn>, <Dm>
									// vadd<c>.f32 <Sd>, <Sn>, <Sm>
		case arm_vdiv:				// A8.6.301 VDIV
									// vdiv<c>.f64 <Dd>, <Dn>, <Dm>
									// vdiv<c>.f32 <Sd>, <Sn>, <Sm>
		case arm_vmul__fp_2:		// A8.6.338 VMUL (floating-point)
									// vmul<c>.f64 <Dd>, <Dn>, <Dm>
									// vmul<c>.f32 <Sd>, <Sn>, <Sm>
		case arm_vnmul:				// A8.6.343 VNMLA, VNMLS, VNMUL
									// vnmul<c>.f64 <Dd>, <Dn>, <Dm>
									// vnmul<c>.f32 <Sd>, <Sn>, <Sm>
		case arm_vsub__fp_f:		// A8.6.402 VSUB (floating-point)
									// vsub<c>.f64 <Dd>, <Dn>, <Dm>
									// vsub<c>.f32 <Sd>, <Sn>, <Sm>
			instruction = mnemonic + getArmCondition(opcode) + getVFPSzF64F32dnmOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vaddhn:			// A8.6.273 VADDHN
									// vaddhn<c>.<dt> <Dd>, <Qn>, <Qm>
		case arm_vraddhn:			// A8.6.370 VRADDHN
									// vraddhn<c>.<dt> <Dd>, <Qn>, <Qm>
		case arm_vrsubhn:			// A8.6.381 VRSUBHN
									// vrsubhn<c>.<dt> <Dd>, <Qn>, <Qm>
		case arm_vsubhn:			// A8.6.403 VSUBHN
									// vsubhn<c>.<dt> <Dd>, <Qn>, <Qm>
			instruction = mnemonic + getVFPIDataType2DdQnDmOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vaddl_vaddw:		// A8.6.274 VADDL, VADDW
									// vaddl<c>.<dt> <Qd>, <Dn>, <Dm>
									// vaddw<c>.<dt> <Qd>, <Qn>, <Dm>
		case arm_vsubl_vsubw:		// A8.6.404 VSUBL, VSUBW
									// vsubl<c>.<dt> <Qd>, <Dn>, <Dm>
									// vsubw<c>.<dt> {<Qd>,} <Qn>, <Dm>
			instruction = mnemonic + getVFP_vXXXl_vXXXw(opcode, 24);
			// No pc check: not applicable
			break;

		case arm_vbif_vbit_vbsl_veor:	// A8.6.279 VBIF, VBIT, VBSL
									// vbif<c> <Qd>, <Qn>, <Qm>
									// vbif<c> <Dd>, <Dn>, <Dm>
									// vbit<c> <Qd>, <Qn>, <Qm>
									// vbit<c> <Dd>, <Dn>, <Dm>
									// vbsl<c> <Qd>, <Qn>, <Qm>
									// vbsl<c> <Dd>, <Dn>, <Dm>
									// A8.6.304 VEOR
									// veor<c> <Qd>, <Qn>, <Qm>
									// veor<c> <Dd>, <Dn>, <Dm>
			mnemonic = getVFP_vbif_vbit_vbsl_veor_mnemonic(opcode);
// no break!
		case arm_vand:				// A8.6.276 VAND (register)
									// vand<c> <Qd>, <Qn>, <Qm>
									// vand<c> <Dd>, <Dn>, <Dm>
		case arm_vbic:				// A8.6.278 VBIC (register)
									// vbic<c> <Qd>, <Qn>, <Qm>
									// vbic<c> <Dd>, <Dn>, <Dm>
		case arm_vorn:				// A8.6.345 VORN (register)
									// vorn<c> <Qd>, <Qn>, <Qm>
									// vorn<c> <Dd>, <Dn>, <Dm>
		case arm_vorr:				// A8.6.347 VORR (register)
									// vorr<c> <Qd>, <Qn>, <Qm>
									// vorr<c> <Dd>, <Dn>, <Dm>
			instruction = mnemonic + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vmov_vbitwise:		// A8.6.277 VBIC (immediate)
									// vbic<c>.<dt> <Qd>, #<imm>
									// vbic<c>.<dt> <Dd>, #<imm>
									// A8.6.326 VMOV (immediate)
									// vmov<c>.<dt> <Qd>, #<imm>
									// vmov<c>.<dt> <Dd>, #<imm>
									// A8.6.340 VMVN (immediate)
									// vmvn<c>.<dt> <Qd>, #<imm>
									// vmvn<c>.<dt> <Dd>, #<imm>
									// A8.6.346 VORR (immediate)
									// vorr<c>.<dt> <Qd>, #<imm>
									// vorr<c>.<dt> <Dd>, #<imm>
			instruction = getVFP_vmov_vbitwise_instruction(opcode, 24);
			// No pc check: not applicable
			break;

		case arm_vceq__imm0:		// A8.6.281 VCEQ (immediate #0)
									// vceq<c>.<dt> <Qd>, <Qm>, #0
									// vceq<c>.<dt> <Dd>, <Dm>, #0
			instruction = mnemonic + getVFPIorFQorDdmOperands(opcode, 10) + ",#0";
			// No pc check: not applicable
			break;

		case arm_vcge__imm0:		// A8.6.283 VCGE (immediate #0)
									// vcge<c>.<dt> <Qd>, <Qm>, #0
									// vcge<c>.<dt> <Dd>, <Dm>, #0
		case arm_vcgt__imm0:		// A8.6.285 VCGT (immediate #0)
									// vcgt<c>.<dt> <Qd>, <Qm>, #0
									// vcgt<c>.<dt> <Dd>, <Dm>, #0
		case arm_vcle:				// A8.6.287 VCLE (immediate #0)
									// vcle<c>.<dt> <Qd>, <Qm>, #0
									// vcle<c>.<dt> <Dd>, <Dm>, #0
		case arm_vclt:				// A8.6.290 VCLT (immediate #0)
									// vclt<c>.<dt> <Qd>, <Qm>, #0
									// vclt<c>.<dt> <Dd>, <Dm>, #0
			instruction = mnemonic + getVFPSorUorFQorDdmOperands(opcode, 10, 11) + ",#0"; // chose bit 11 because it is 0
			// No pc check: not applicable
			break;

		case arm_vcls:				// A8.6.288 VCLS
									// vcls<c>.<dt> <Qd>, <Qm>
									// vcls<c>.<dt> <Dd>, <Dm>
		case arm_vqabs:				// A8.6.356 VQABS
									// vqabs<c>.<dt> <Qd>,<Qm>
									// vqabs<c>.<dt> <Dd>,<Dm>
		case arm_vqneg:				// A8.6.362 VQNEG
									// vqneg<c>.<dt> <Qd>,<Qm>
									// vqneg<c>.<dt> <Dd>,<Dm>
			instruction = mnemonic + getVFPSorUorFQorDdmOperands(opcode, 4, 11); // chose bit 11 because it is 0
			// No pc check: not applicable
			break;

		case arm_vclz:				// A8.6.291 VCLZ
									// vclz<c>.<dt> <Qd>, <Qm>
									// vclz<c>.<dt> <Dd>, <Dm>
			instruction =  mnemonic + getVFPIorFQorDdmOperands(opcode, 11); // chose bit 11 because it is 0 
			// No pc check: not applicable
			break;

		case arm_vcmp__reg:			// A8.6.292 VCMP, VCMPE
									// vcmp{e}<c>.f64 <Dd>, <Dm>
									// vcmp{e}<c>.f32 <Sd>, <Sm>
			instruction = mnemonic + getE(opcode) + getArmCondition(opcode)
					+ getVFPSzF64F32dmOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vcmp__to_0:		// A8.6.292 VCMP, VCMPE
									// vcmp{e}<c>.f64 <Dd>, #0.0
									// vcmp{e}<c>.f32 <Sd>, #0.0
			instruction = mnemonic + getE(opcode) + getArmCondition(opcode)
					+ getVFP_vcmpTo0Operands(opcode);
			// No pc check: not applicable
			break;

		case arm_vcnt:				// A8.6.293 VCNT
									// vcnt<c>.8 <Qd>, <Qm>
									// vcnt<c>.8 <Dd>, <Dm>
			mnemonic += ".8";
		case arm_vmvn:				// A8.6.341 VMVN (register)
									// vmvn<c> <Qd>, <Qm>
									// vmvn<c> <Dd>, <Dm>
		case arm_vmov__reg:			// A8.6.327 VMOV (register)
									// vmov<c> <Qd>, <Qm>
									// vmov<c> <Dd>, <Dm>
		case arm_vswp:				// A8.6.405 VSWP
									// vswp<c> <Qd>, <Qm>
									// vswp<c> <Dd>, <Dm>
			instruction = mnemonic + getVFPQorDdmRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vcvt__fp_i_vec:	// A8.6.294 VCVT (between floating-point and integer, Advanced SIMD)
									// vcvt<c>.<Td>.<Tm> <Qd>, <Qm>
									// vcvt<c>.<Td>.<Tm> <Dd>, <Dm>
			instruction = mnemonic + getVFP_vcvtFpIVecOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vcvt__fp_i_reg:	// A8.6.295 VCVT, VCVTR (between floating-point and integer, VFP)
									// vcvt{r}<c>.s32.f64 <Sd>, <Dm>
									// vcvt{r}<c>.s32.f32 <Sd>, <Sm>
									// vcvt{r}<c>.u32.f64 <Sd>, <Dm>
									// vcvt{r}<c>.u32.f32 <Sd>, <Sm>
									// vcvt<c>.f64.<Tm> <Dd>, <Sm>
									// vcvt<c>.f32.<Tm> <Sd>, <Sm>
			if (isBitEnabled(opcode, 18) && !isBitEnabled(opcode, 7))
				mnemonic += "r";
			instruction = mnemonic + getArmCondition(opcode) + getVFP_vcvtFpIRegOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vcvt__fp_fix_vec:	// A8.6.296 VCVT (between floating-point and fixed-point, Advanced SIMD)
									// vcvt<c>.<Td>.<Tm> <Qd>, <Qm>, #<fbits>
									// vcvt<c>.<Td>.<Tm> <Dd>, <Dm>, #<fbits>
			instruction = mnemonic + getVFP_vcvtFpFixVecOperands(opcode, 24);
			// No pc check: not applicable
			break;

		case arm_vcvt__fp_fix_reg:	// A8.6.297 VCVT (between floating-point and fixed-point, VFP)
									// vcvt<c>.<Td>.f64 <Dd>, <Dd>, #<fbits>
									// vcvt<c>.<Td>.f32 <Sd>, <Sd>, #<fbits>
									// vcvt<c>.f64.<Td> <Dd>, <Dd>, #<fbits>
									// vcvt<c>.f32.<Td> <Sd>, <Sd>, #<fbits>
			instruction = mnemonic + getArmCondition(opcode) + getVFP_vcvtFpFixRegOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vcvt__dp_sp:		// A8.6.298 VCVT (between double-precision and single-precision)
									// vcvt<c>.f64.f32 <Dd>, <Sm>
									// vcvt<c>.f32.f64 <Sd>, <Dm>
			instruction = mnemonic + getArmCondition(opcode) + getVFP_vcvtDpSpOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vcvt__hp_sp_vec:	// A8.6.299 VCVT (between half-precision and single-precision, Advanced SIMD)
									// vcvt<c>.f32.f16 <Qd>, <Dm>
									// vcvt<c>.f16.f32 <Dd>, <Qm>
			instruction = mnemonic + getVFP_vcvtHpSpVecOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vcvt__hp_sp_reg:	// A8.6.300 VCVTB, VCVTT (between half-precision and single-precision, VFP)
									// vcvt<y><c>.f32.f16 <Sd>, <Sm>
									// vcvt<y><c>.f16.f32 <Sd>, <Sm>
			mnemonic += isBitEnabled(opcode, 7) ? "t" : "b";
			instruction = mnemonic + getArmCondition(opcode) + getVFP_vcvtHpSpRegOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vdup__scalar:		// A8.6.302 VDUP (scalar)
									// vdup<c>.<size> <Qd>, <Dm[x]>
									// vdup<c>.<size> <Dd>, <Dm[x]>
			instruction = mnemonic + getVFP_vdupScalarOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vdup__reg:			// A8.6.303 VDUP (ARM core register)
									// vdup<c>.<size> <Qd>, <Rt>
									// vdup<c>.<size> <Dd>, <Rt>
			mnemonic += getArmCondition(opcode);
			instruction = mnemonic + getVFP_vdupRegOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vext:				// A8.6.305 VEXT
									// vext<c>.8 <Qd>, <Qn>, <Qm>, #<imm>
									// vext<c>.8 <Dd>, <Dn>, <Dm>, #<imm>
			instruction = mnemonic + ".8" + getVFPQorDdnmRegs(opcode)
					+ ",#" + (opcode >> 8 & 0xf);
			// No pc check: not applicable
			break;

		case arm_vld__multi:		// A8.6.307 VLD1 (multiple single elements)
									// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}
									// vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
									// A8.6.310 VLD2 (multiple 2-element structures)
									// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}
									// vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
									// A8.6.313 VLD3 (multiple 3-element structures)
									// vld3<c>.<size> <list>, [<Rn>{@<align>}]{!}
									// vld3<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
									// A8.6.316 VLD4 (multiple 4-element structures)
									// vld4<c>.<size> <list>, [<Rn>{@<align>}]{!}
									// vld4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		case arm_vst__multi:		// A8.6.391 VST1 (multiple single elements)
									// vst1<c>.<size> <list>, [<Rn>{@<align>}]{!}
									// vst1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
									// A8.6.393 VST2 (multiple 2-element structures)
									// vst2<c>.<size> <list>, [<Rn>{@<align>}]{!}
									// vst2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
									// A8.6.395 VST3 (multiple 3-element structures)
									// vst3<c>.<size> <list>, [<Rn>{@<align>}]{!}
									// vst3<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
									// A8.6.397 VST4 (multiple 4-element structures)
									// vst4<c>.<size> <list>, [<Rn>{@<align>}]{!}
									// vst4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
			instruction = mnemonic + getVFP_vXX_multi(opcode);
			// No pc check: not applicable
			break;

		case arm_vld__xlane:		// A8.6.308 VLD1 (single element to one lane)
									// vld1<c>.<size> <list>, [<Rn>{@<align>}}]{!}
									// vld1<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
									// A8.6.309 VLD1 (single element to all lanes)
									// vld1<c>.<size> <list>, [<Rn>{@<align>}}]{!}
									// vld1<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
									// A8.6.311 VLD2 (single 2-element structure to one lane)
									// vld2<c>.<size> <list>, [<Rn>{@<align>}}]{!}
									// vld2<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
									// A8.6.312 VLD2 (single 2-element structure to all lanes)
									// vld2<c>.<size> <list>, [<Rn>{@<align>}}]{!}
									// vld2<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
									// A8.6.314 VLD3 (single 3-element structure to one lane)
									// vld3<c>.<size> <list>, [<Rn>{@<align>}}]{!}
									// vld3<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
									// A8.6.315 VLD3 (single 3-element structure to all lanes)
									// vld3<c>.<size> <list>, [<Rn>{@<align>}}]{!}
									// vld3<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
									// A8.6.317 VLD4 (single 4-element structure to one lane)
									// vld4<c>.<size> <list>, [<Rn>{@<align>}}]{!}
									// vld4<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
									// A8.6.318 VLD4 (single 4-element structure to all lanes)
									// vld4<c>.<size> <list>, [<Rn>{@<align>}}]{!}
									// vld4<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
		case arm_vst__xlane:		// A8.6.392 VST1 (single element from one lane)
									// vst1<c>.<size> <list>, [<Rn>{@<align>}}]{!}
									// vst1<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
									// A8.6.394 VST2 (single 2-element structure from one lane)
									// vst2<c>.<size> <list>, [<Rn>{@<align>}}]{!}
									// vst2<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
									// A8.6.396 VST3 (single 3-element structure from one lane)
									// vst3<c>.<size> <list>, [<Rn>{@<align>}}]{!}
									// vst3<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
									// A8.6.398 VST4 (single 4-element structure from one lane)
									// vst4<c>.<size> <list>, [<Rn>{@<align>}}]{!}
									// vst4<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
			instruction = mnemonic + getVFP_vXX_Xlane(opcode);
			// No pc check: not applicable
			break;

		case arm_vldm__64:			// A8.6.319 VLDM
									// vldm{mode}<c> <Rn>{!}, <list> <list> is consecutive 64-bit registers
		case arm_vldm__32:			// A8.6.319 VLDM
									// vldm{mode}<c> <Rn>{!}, <list> <list> is consecutive 32-bit registers
		case arm_vstm__64:			// A8.6.399 VSTM
									// vstm{mode}<c> <Rn>{!}, <list> <list> is consecutive 64-bit registers
		case arm_vstm__32:			// A8.6.399 VSTM
									// vstm{mode}<c> <Rn>{!}, <list> <list> is consecutive 32-bit registers
			instruction = mnemonic + getVFPIncDec(opcode) + getArmCondition(opcode)
						  + getVFP_vXXm(opcode);
			// No pc check: not applicable
			break;

		case arm_vldr__64:			// A8.6.320 VLDR
									// vldr<c> <Dd>, [<Rn>{, #+/-<imm>}]
									// vldr<c> <Dd>, <label>
									// vldr<c> <Dd>, [pc,#+/-<imm>]	Alternative form
		case arm_vldr__32:			// A8.6.320 VLDR
									// vldr<c> <Sd>, [<Rn>{, #+/-<imm>}]
									// vldr<c> <Sd>, <label>
									// vldr<c> <Sd>, [pc,#+/-<imm>]	Alternative form
		case arm_vstr__64:			// A8.6.400 VSTR
									// vstr<c> <Dd>, [<Rn>{, #+/-<imm>}]
		case arm_vstr__32:			// A8.6.400 VSTR
									// vstr<c> <Sd>, [<Rn>{, #+/-<imm>}]
			instruction = mnemonic + getArmCondition(opcode) + getVFP_vXXr(opcode);
			// No pc check: not applicable
			break;

		case arm_vmax_vmin__int:	// A8.6.321 VMAX, VMIN (integer)
									// vmax<c>.<dt> <Qd>, <Qn>, <Qm>
									// vmax<c>.<dt> <Dd>, <Dn>, <Dm>
									// vmin<c>.<dt> <Qd>, <Qn>, <Qm>
									// vmin<c>.<dt> <Dd>, <Dn>, <Dm>
		case arm_vpmax_vpmin__int:	// A8.6.352 VPMAX, VPMIN (integer)
									// vp<op><c>.<dt> <Dd>, <Dn>, <Dm>
									// (this works despite no Q version because Q==1 is UNDEFEIND)
			instruction = mnemonic + (isBitEnabled(opcode, 4) ? "min" : "max")
						  + getVFPSorUDataType(opcode, 24) + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vmax_vmin__fp:		// A8.6.322 VMAX, VMIN (floating-point)
									// vmax<c>.f32 <Qd>, <Qn>, <Qm>
									// vmax<c>.f32 <Dd>, <Dn>, <Dm>
									// vmin<c>.f32 <Qd>, <Qn>, <Qm>
									// vmin<c>.f32 <Dd>, <Dn>, <Dm>
		case arm_vpmax_vpmin__fp:	// A8.6.353 VPMAX, VPMIN (floating-point)
									// vp<op><c>.f32 <Dd>, <Dn>, <Dm>
			instruction = mnemonic + (isBitEnabled(opcode, 21) ? "min.f32" : "max.f32")
						  + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vml__int:			// A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
									// v<op><c>.<dt> <Qd>, <Qn>, <Qm>
									// v<op><c>.<dt> <Dd>, <Dn>, <Dm>
			mnemonic += isBitEnabled(opcode, 24) ? 's' : 'a';
			instruction = mnemonic + getVFPIDataType(opcode, 20) + TAB + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vml__int_long:		// A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
									// v<op>l<c>.<dt> <Qd>, <Dn>, <Dm>
			mnemonic += isBitEnabled(opcode, 9) ? "sl" : "al";
			instruction = mnemonic + getVFPSorUDataType(opcode, 24)
					+ TAB + getVFPQdDnDmRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vml__f32:			// A8.6.324 VMLA, VMLS (floating-point)
									// v<op><c>.f32 <Qd>, <Qn>, <Qm>
									// v<op><c>.f32 <Dd>, <Dn>, <Dm>
			mnemonic += isBitEnabled(opcode, 21) ? "s.f32" : "a.f32";
			instruction = mnemonic + TAB + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vml__fp:			// A8.6.324 VMLA, VMLS (floating-point)
									// v<op><c>.f64 <Dd>, <Dn>, <Dm>
									// v<op><c>.f32 <Sd>, <Sn>, <Sm>
			mnemonic += isBitEnabled(opcode, 6) ? 's' : 'a' + getArmCondition(opcode);
			instruction = mnemonic + getVFPSzF64F32dnmOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vml__scalar:		// A8.6.325 VMLA, VMLAL, VMLS, VMLSL (by scalar)
									// v<op><c>.<dt> <Qd>, <Qn>, <Dm[x]>
									// v<op><c>.<dt> <Dd>, <Dn>, <Dm[x]>
									// v<op>l<c>.<dt> <Qd>, <Dn>, <Dm[x]>
			mnemonic += isBitEnabled(opcode, 10) ? 's' : 'a';
		case arm_vmul__scalar:		// A8.6.339 VMUL, VMULL (by scalar)
									// vmul<c>.<dt> <Qd>, <Qn>, <Dm[x]>
									// vmul<c>.<dt> <Dd>, <Dn>, <Dm[x]>
		case arm_vqdmull__scalar:	// A8.6.360 VQDMULL
									// vqdmull<c>.<dt> <Qd>,<Dn>,<Dm[x]>
									// bit 9 == 1, so getVFP_vmXXScalar() works
			instruction = mnemonic + getVFP_vmXXScalar(opcode, 24);
			// No pc check: not applicable
			break;

		case arm_vmov__imm:			// A8.6.326 VMOV (immediate)
									// vmov<c>.f64 <Dd>, #<imm>
									// vmov<c>.f32 <Sd>, #<imm>
			mnemonic += getArmCondition(opcode) + getVFPSzF64F32Type(getBit(opcode, 8)); 
			imm = (opcode >> 16 & 0xf) << 4 | opcode & 0xf;
			instruction = mnemonic + TAB + getVFPDorSReg(opcode, getBit(opcode, 8), 12, 22)
						  + ",#" + getHexValue(imm);
			// No pc check: not applicable
			break;

		case arm_vmov_5:			// A8.6.328 VMOV (ARM core register to scalar)
									// vmov<c>.<size> <Dd[x]>, <Rt>
			mnemonic += getArmCondition(opcode);
			instruction = mnemonic + getVFP_vmovArmCoreRegToScalar(opcode);
			// No pc check: not applicable
			break;

		case arm_vmov_6:			// A8.6.329 VMOV (scalar to ARM core register)
									// vmov<c>.<dt> <Rt>, <Dn[x]>
			mnemonic += getArmCondition(opcode);
			instruction = mnemonic + getVFP_vmovScalarToArmCoreReg(opcode);
			// No pc check: not applicable
			break;

		case arm_vmov_7:			// A8.6.330 VMOV (between ARM core register and
									//							single-precision register)
									// vmov<c> <Sn>, <Rt>
									// vmov<c> <Rt>, <Sn>
			mnemonic += getArmCondition(opcode);
			instruction = mnemonic + getVFP_vmovBetweenArmCoreAndSinglePrecReg(opcode);
			// No pc check: not applicable
			break;

		case arm_vmov_8:			// A8.6.331 VMOV (between two ARM core registers and
									//							two single-precision registers)
									// vmov<c> <Sm>, <Sm1>, <Rt>, <Rt2>
									// vmov<c> <Rt>, <Rt2>, <Sm>, <Sm1>
			mnemonic += getArmCondition(opcode);
			instruction = mnemonic + getVFP_vmovBetween2ArmCoreAndSinglePrecRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vmov_9:			// A8.6.332 VMOV (between two ARM core registers and
									//							a doubleword extension register)
									// vmov<c> <Dm>, <Rt>, <Rt2>
									// vmov<c> <Rt>, <Rt2>, <Dm>
			mnemonic += getArmCondition(opcode);
			instruction = mnemonic + getVFP_vmovBetween2ArmCoreAnd1DoublewordExtensionRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vmovl:				// A8.6.333 VMOVL
									// vmovl<c>.<dt> <Qd>, <Dm>
		case arm_vshll__various:	// A8.6.384 VSHLL
									// vshll<c>.<type><size> <Qd>, <Dm>, #<imm> (0 < <imm> < <size>)
			instruction = mnemonic + getVFP_vmovl_vshll_operands(opcode, 24);
			// No pc check: not applicable
			break;

		case arm_vmovn:				// A8.6.334 VMOVN
									// vmovn<c>.<dt> <Dd>, <Qm>
			fld1 = (opcode >> 18) & 3;	// size field

			tempStr = ".i16";
			if (fld1 == 1)
				tempStr = ".i32";
			else if (fld1 == 2)
				tempStr = ".i64";

			instruction = mnemonic + tempStr + "\t"
					+ getVFPQorDReg(opcode, 0, 12, 22) + "," +getVFPQorDReg(opcode, 1, 0, 5);
			// No pc check: not applicable
			break;

		case arm_vmrs:				// A8.6.335 VMRS
									// vmrs<c> <Rt>, fpscr
									// B6.1.14 VMRS
									// vmrs<c> <Rt>,<spec_reg>
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getR_12(opcode) + "," + getVFPSpecialReg(opcode);
			// No pc check: not applicable
			break;

		case arm_vmsr:				// A8.6.336 VMSR
									// vmsr<c> fpscr, <Rt>
									// B6.1.15 VMSR
									// vmsr<c> <spec_reg>,<Rt>
			instruction = mnemonic + getArmCondition(opcode) + "\t"
					+ getVFPSpecialReg(opcode) + "," + getR_12(opcode);
			// No pc check: not applicable
			break;

		case arm_vmul_1:			// A8.6.337 VMUL, VMULL (integer and polynomial)
									// vmul<c>.<dt> <Qd>, <Qn>, <Qm>
									// vmul<c>.<dt> <Dd>, <Dn>, <Dm>
			// 1 1 1 1 0 0 1 op_24_24 0 D_22_22 size_21_20 Vn_19_16 Vd_15_12 1 0 0 1 N_7_7 Q_6_6 M_5_5 1 Vm_3_0
			mnemonic += (isBitEnabled(opcode, 24) ? ".p" : ".i") + getVFPDataTypeSize(opcode, 20);
			instruction = mnemonic + TAB + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vmull:				// A8.6.337 VMUL, VMULL (integer and polynomial)
									// vmull<c>.<dt> <Qd>, <Dn>, <Dm>
			mnemonic += isBitEnabled(opcode, 9) ? getVFPPDataType(opcode, 20) : getVFPSorUDataType(opcode, 24);
			instruction = mnemonic + TAB + getVFPQdDnDmRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vpadal:			// A8.6.348 VPADAL
									// vpadal<c>.<dt> <Qd>, <Qm>
									// vpadal<c>.<dt> <Dd>, <Dm>
		case arm_vpaddl:			// A8.6.351 VPADDL
									// vpaddl<c>.<dt> <Qd>, <Qm>
									// vpaddl<c>.<dt> <Dd>, <Dm>
			instruction = mnemonic + getVFPSorUorFQorDdmOperands(opcode, 4, 7); // chose bit 4 because it is 0
			// No pc check: not applicable
			break;

		case arm_vpop:				// A8.6.354 VPOP
									// vpop<c> <list>	(<list> is consecutive 64-bit registers)
									// vpop<c> <list>	(<list> is consecutive 32-bit registers)
		case arm_vpush:				// A8.6.355 VPUSH
									// vpush<c> <list> (<list> is consecutive 64-bit registers)
									// vpush<c> <list> (<list> is consecutive 32-bit registers)
			instruction = mnemonic + getArmCondition(opcode) + getVFP_vpop_vpush_operands(opcode);
			// No pc check: not applicable
			break;

		case arm_vqdml__scalar:		// A8.6.358 VQDMLAL, VQDMLSL
									// vqd<op><c>.<dt> <Qd>,<Dn>,<Dm[x]>
			mnemonic += isBitEnabled(opcode, 10) ? "sl.s" : "al.s";
			instruction = mnemonic + getVFPScalarOperands(opcode, 1, 0);
			// No pc check: not applicable
			break;

		case arm_vqdmulh__vec:		// A8.6.359 VQDMULH
									// vqdmulh<c>.<dt> <Qd>,<Qn>,<Qm>
									// vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm>
		case arm_vqrdmulh__vec:		// A8.6.363 VQRDMULH
									// vqrdmulh<c>.<dt> <Qd>,<Qn>,<Qm>
									// vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm>
			mnemonic += isBitEnabled(opcode, 20) ? ".s16" : ".s32";
			instruction = mnemonic + TAB + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vqdmulh__scalar:	// A8.6.359 VQDMULH
									// vqdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>
									// vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>
		case arm_vqrdmulh__scalar:	// A8.6.363 VQRDMULH
									// vqrdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>
									// vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>
			q = getBit(opcode, 24);
			instruction = mnemonic + ".s" + getVFPScalarOperands(opcode, q, q);
			// No pc check: not applicable
			break;

		case arm_vqmov:				// A8.6.361 VQMOVN, VQMOVUN
									// vqmov{u}n<c>.<type><size> <Dd>,<Qm>
			instruction = mnemonic + getVFP_vqmov_instruction(opcode);
			// No pc check: not applicable
			break;

		case arm_vqrshr:			// A8.6.365 VQRSHRN, VQRSHRUN
			// vqrshr{u}n<c>.<type><size> <Dd>,<Qm>,#<imm>
		case arm_vqshr:
			// vqshr{u}n<c>.<type><size> <Dd>,<Qm>,#<imm>
			instruction = mnemonic + getVFP_vqXshr_instruction(opcode, 24);
			// No pc check: not applicable
			break;

		case arm_vqshl__imm:		// A8.6.367 VQSHL, VQSHLU (immediate)
									// vqshl{u}<c>.<type><size> <Qd>,<Qm>,#<imm>
									// vqshl{u}<c>.<type><size> <Dd>,<Dm>,#<imm>
			instruction = mnemonic + getVFP_vqshl_instruction(opcode, 24);
			// No pc check: not applicable
			break;

		case arm_vqrshl:			// A8.6.364 VQRSHL
									// vqrshl<c>.<type><size> <Qd>,<Qm>,<Qn>
									// vqrshl<c>.<type><size> <Dd>,<Dm>,<Dn>
		case arm_vqshl__reg:		// A8.6.366 VQSHL (register)
									// vqshl<c>.<type><size> <Qd>,<Qm>,<Qn>
									// vqshl<c>.<type><size> <Dd>,<Dm>,<Dn>
		case arm_vrshl:				// A8.6.375 VRSHL
									// vrshl<c>.<type><size> <Qd>,<Qm>,<Qn>
									// vrshl<c>.<type><size> <Dd>,<Dm>,<Dn>
		case arm_vshl__reg:			// A8.6.383 VSHL (register)
									// vshl<c>.<type><size>       <Qd>,<Qm>,<Qn>
									// vshl<c>.<type><size>	     <Dd>,<Dm>,<Dn>
			instruction = mnemonic + getVFPSorUDataType(opcode, 24) + getVFPQorDdmnRegs(opcode);
			// No pc check: not applicable
			break;

		case arm_vrecpe:			// A8.6.371 VRECPE
									// vrecpe<c>.<dt> <Qd>, <Qm>
									// vrecpe<c>.<dt> <Dd>, <Dm>
		case arm_vrsqrte:			// A8.6.378 VRSQRTE
									// vrsqrte<c>.<dt> <Qd>, <Qm>
									// vrsqrte<c>.<dt> <Dd>, <Dm>
			instruction = mnemonic + getVFPSorUorFQorDdmOperands(opcode, 8, 10);
			// No pc check: not applicable
			break;

		case arm_vrev:				// A8.6.373 VREV16, VREV32, VREV64
									// vrev<n><c>.<size> <Qd>,<Qm>
									// vrev<n><c>.<size> <Dd>,<Dm>
			instruction = mnemonic + getVFP_vrev_instruction(opcode);
			// No pc check: not applicable
			break;

		case arm_vrshr:				// A8.6.376 VRSHR
									// vrshr<c>.<type><size> <Qd>, <Qm>, #<imm>
									// vrshr<c>.<type><size> <Dd>, <Dm>, #<imm>
		case arm_vrsra:				// A8.6.380 VRSRA
									// vrsra<c>.<type><size> <Qd>, <Qm>, #<imm>
									// vrsra<c>.<type><size> <Dd>, <Dm>, #<imm>
		case arm_vshr:				// A8.6.385 VSHR
									// vshr<c>.<type><size> <Qd>, <Qm>, #<imm>
									// vshr<c>.<type><size> <Dd>, <Dm>, #<imm>
		case arm_vsra:				// A8.6.389 VSRA
									// vsra<c>.<type><size> <Qd>, <Qm>, #<imm>
									// vsra<c>.<type><size> <Dd>, <Dm>, #<imm>
			mnemonic += isBitEnabled(opcode, 24) ? ".u" : ".s";
// no break
		case arm_vsri:				// A8.6.390 VSRI
									// vsri<c>.<size> <Qd>, <Qm>, #<imm>
									// vsri<c>.<size> <Dd>, <Dm>, #<imm>
			instruction = mnemonic + getVFP_vXrX_instruction(opcode, true);
			// No pc check: not applicable
			break;

		case arm_vrshrn:			// A8.6.377 VRSHRN
									// vrshrn<c>.i<size> <Dd>, <Qm>, #<imm>
		case arm_vshrn:				// A8.6.386 VSHRN
									// vshrn<c>.i<size> <Dd>, <Qm>, #<imm>
			instruction = mnemonic + getVFP_vXshrn_instruction(opcode);
			// No pc check: not applicable
			break;

		case arm_vshl__imm:			// A8.6.382 VSHL (immediate)
									// vshl<c>.i<size> <Qd>, <Qm>, #<imm>
									// vshl<c>.i<size> <Dd>, <Dm>, #<imm>
		case arm_vsli:				// A8.6.387 VSLI
									// vsli<c>.<size> <Qd>, <Qm>, #<imm>
									// vsli<c>.<size> <Dd>, <Dm>, #<imm>
			instruction = mnemonic + getVFP_vXrX_instruction(opcode, false);
			// No pc check: not applicable
			break;

		case arm_vshll__max:		// A8.6.384 VSHLL
									// vshll<c>.<type><size> <Qd>, <Dm>, #<imm> (<imm> == <size>)
			mnemonic += getVFPIDataType3(opcode, 18);
			instruction = mnemonic + TAB + getVFPQorDReg(opcode, 1, 12, 22)
					+ ',' + getVFPQorDReg(opcode, 0, 0, 5) + ",#" + (8 << (opcode >> 18 & 3));
			// No pc check: not applicable
			break;

		case arm_vtb:				// A8.6.406 VTBL, VTBX
									// v<op><c>.8 <Dd>, <list>, <Dm>
			instruction = mnemonic + getVFP_vtb_instruction(opcode);
			// No pc check: not applicable
			break;

		case arm_vtrn:				// A8.6.407 VTRN
									// vtrn<c>.<size> <Qd>, <Qm>
									// vtrn<c>.<size> <Dd>, <Dm>
		case arm_vuzp:				// A8.6.409 VUZP
									// vuzp<c>.<size> <Qd>, <Qm>
									// vuzp<c>.<size> <Dd>, <Dm>
		case arm_vzip:				// A8.6.410 VZIP
									// vzip<c>.<size> <Qd>, <Qm>
									// vzip<c>.<size> <Dd>, <Dm>
			instruction = mnemonic + getVFPSzQorDdmOperands(opcode);
			// No pc check: not applicable
			break;

		case arm_vtst:				// A8.6.408 VTST
									// vtst<c>.<size> <Qd>, <Qn>, <Qm>
									// vtst<c>.<size> <Dd>, <Dn>, <Dm>
			instruction = mnemonic + getVFPSzQorDdnmOperands(opcode);
			// No pc check: not applicable
			break;

		default:
			instruction = IDisassembler.INVALID_OPCODE;
			break;
		}
		return instruction;
	}


	/**
	 * Disassemble a 16-bit Thumb instruction
	 * Reference manual citations (e.g., "A8.6.2") refer to sections in the ARM Architecture
	 * Reference Manual ARMv7-A and ARMv7-R Edition with errata markup
	 * @return disassembled instruction
	 */
	private String parseThumbOpcode() throws BufferUnderflowException {
		byte b0, b1;
		if (endianMode == BIG_ENDIAN_MODE && (codeBuffer.remaining() > 1)) {
			b0 = codeBuffer.get();
			b1 = codeBuffer.get();
		} else {
			b1 = codeBuffer.get();
			b0 = codeBuffer.get();
		}
		int opcode = (b0 & 0xff) << 8 | b1 & 0xff;
		if (0xf0 == (b0 & 0xf0) || 0xe8 == (b0 & 0xe8)) {
			if (endianMode == BIG_ENDIAN_MODE && (codeBuffer.remaining() > 1)) {
				b0 = codeBuffer.get();
				b1 = codeBuffer.get();
			} else {
				b1 = codeBuffer.get();
				b0 = codeBuffer.get();
			}
			opcode = opcode << 16 | (b0 & 0xff) << 8 | (b1 & 0xff);
			return parseThumb2Opcode(opcode);
		}

		OpcodeARM.Index opcodeIndex = OpcodeARM.Index.invalid;
		String mnemonic = "";

		for (OpcodeARM thumbOpcode : OpcodeARM.thumb_opcode_table) {
			int result = opcode & thumbOpcode.getOpcodeMask();
			if (result == thumbOpcode.getOpcodeResult()) {
				opcodeIndex = thumbOpcode.getIndex();
				mnemonic = thumbOpcode.getMnemonic();
				break;
			}
		}

		String instruction = "";
		String regList = "";
		String regOp = "";
		int offset;
		int bit;
		switch (opcodeIndex) {
		case thumb_adc:				// A8.6.2 ADC (register)
									// adcs <Rdn>,<Rm> Outside IT block.
									// adc<c> <Rdn>,<Rm> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: Rdn cannot be PC
			break;

		case thumb_add__imm:		// A8.6.4 ADD (immediate, Thumb)
									// adds <Rdn>,#<imm8> Outside IT block.
									// add<c> <Rdn>,#<imm8> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 8) + "," + getThumbImmediate8(opcode, 1);
			// No pc check: Rdn cannot be PC
			break;

		case thumb_add__imm_to_sp:	// A8.6.8 ADD (SP plus immediate)
									// add<c> sp,sp,#<imm>
			instruction = mnemonic + "\tsp,sp," + getThumbImmediate7(opcode, 4);
			// No pc check: not applicable
			break;

		case thumb_add__reg:		// A8.6.6 ADD (register)
									// add<c> <Rdn>,<Rm> If <Rdn> is PC, must be outside or last in IT block.
			regOp = getThumbRegHigh(opcode, 0, 7);
			instruction = mnemonic + "\t" + regOp + "," + getThumbRegHigh(opcode, 3, 6);

			if (regOp.equals("pc"))
				setDefaultPCJumpProperties(true);
			break;

		case thumb_add__reg_imm:	// A8.6.4 ADD (immediate, Thumb)
									// adds <Rd>,<Rn>,#<imm3> Outside IT block.
									// add<c> <Rd>,<Rn>,#<imm3> Inside IT block.
			instruction = mnemonic + "s\t"
					+ getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + "," + getThumbImmediate3(opcode);
			// No pc check: Rd cannot be PC
			break;

		case thumb_add__reg_reg:	// A8.6.6 ADD (register)
									// adds <Rd>,<Rn>,<Rm> Outside IT block.
									// add<c> <Rd>,<Rn>,<Rm> Inside IT block.
			instruction = mnemonic + "s\t"
					+ getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + "," + getThumbReg(opcode, 6);
			// No pc check: Rd cannot be PC
			break;

		case thumb_add__sp_imm:		// A8.6.8 ADD (SP plus immediate)
									// add<c> <Rd>,sp,#<imm>
			instruction = mnemonic + "\t"
					+ getThumbReg(opcode, 8) + ",sp," + getThumbImmediate8(opcode, 4);
			// No pc check: Rd cannot be PC
			break;

		case thumb_adr:				// A8.6.10 ADR
									// adr<c> <Rd>,<label>
									// add <Rd>,pc,imm8		Alternative form
			instruction = mnemonic + "\t"
					+ getThumbReg(opcode, 8) + ",pc," + getThumbImmediate8(opcode, 4);
			// No pc check: Rd cannot be PC
			break;

		case thumb_and:				// A8.6.12 AND (register)
									// ands <Rdn>,<Rm> Outside IT block.
									// and<c> <Rdn>,<Rm> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: Rdn cannot be PC
			break;

		case thumb_asr__imm:		// A8.6.14 ASR (immediate)
									// asrs <Rd>,<Rm>,#<imm> Outside IT block.
									// asr<c> <Rd>,<Rm>,#<imm> Inside IT block.
			instruction = mnemonic + "s\t"
					+ getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + "," + getThumbImmediate5(opcode, 1);
			// No pc check: Rd cannot be PC
			break;

		case thumb_asr__reg:		// A8.6.15 ASR (register)
									// asrs <Rdn>,<Rm> Outside IT block.
									// asr<c> <Rdn>,<Rm> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: Rdn cannot be PC
			break;

		case thumb_b_1:				// A8.6.16 B
									// b<c> <label> Not permitted in IT block.
			offset = getThumbBranchOffset8(opcode);
			String condString = getThumbCondition(opcode);
			isSoleDestination = condString.length() == 0; // true if unconditional
			isSubroutineAddress = false;
			jumpToAddr = address.add(offset);
			instruction = mnemonic + condString + "\t" + jumpToAddr.toHexAddressString();
			break;

		case thumb_b_2:				// A8.6.16 B
									// b<c> <label> Outside or last in IT block
			offset = getThumbBranchOffset11(opcode);
			isSoleDestination = true;
			isSubroutineAddress = false;
			jumpToAddr = address.add(offset);
			instruction = mnemonic + "\t" + jumpToAddr.toHexAddressString();
			break;

		case thumb_bic:				// A8.6.20 BIC (register)
									// bics <Rdn>,<Rm> Outside IT block.
									// bic<c> <Rdn>,<Rm> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: Rdn cannot be PC
			break;

		case thumb_bkpt:			// A8.6.22 BKPT
									// bkpt #<imm8>
		case thumb_svc:				// A8.6.218 SVC (previously SWI)
									// svc<c> #<imm8>
			instruction = mnemonic + "\t" + getThumbImmediate8(opcode, 1);
			// No pc check: not applicable
			break;

		case thumb_blx:				// A8.6.24 BLX (register)
									// blx<c> <Rm> Outside or last in IT block
			instruction = mnemonic + "\t" + getThumbReg(opcode, 3);
			isSoleDestination = true;
			isSubroutineAddress = true;
			addrExpression = getThumbReg(opcode, 3);
			break;

		case thumb_bx:				// A8.6.25 BX
									// bx<c> <Rm> Outside or last in IT block
			instruction = mnemonic + "\t" + getThumbRegHigh(opcode, 3,6);
			isSoleDestination = true;
			isSubroutineAddress = false;
			addrExpression = getThumbRegHigh(opcode, 3, 6);
			break;

		case thumb_cbnz_cbz:		// A8.6.27 CBNZ, CBZ
									// cb{n}z <Rn>,<label> Not permitted in IT block.
			offset = ((opcode >> 3) & 0x1f) * 2;
			String addN = ((opcode & (1 << 11)) != 0) ? "n" : "";
			isSoleDestination = true;
			isSubroutineAddress = false;
			jumpToAddr = address.add(offset);
			instruction = mnemonic + addN + "z\t"
					+ getThumbReg(opcode, 0) + "," + jumpToAddr.toHexAddressString();
			break;

		case thumb_cmn:				// A8.6.33 CMN (register)
									// cmn<c> <Rn>,<Rm>
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: Rd cannot be PC
			break;

		case thumb_cmp__imm:		// A8.6.35 CMP (immediate)
									// cmp<c> <Rn>,#<imm8>
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + "," + getThumbImmediate8(opcode, 1);
			// No pc check: not applicable
			break;

		case thumb_cmp__reg:		// A8.6.36 CMP (register)
									// cmp<c> <Rn>,<Rm> <Rn> and <Rm> both from R0-R7
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: not applicable
			break;

		case thumb_cmp__reg_hi:		// A8.6.36 CMP (register)
									// cmp<c> <Rn>,<Rm> <Rn> and <Rm> not both from R0-R7
			instruction = mnemonic + "\t" + getThumbRegHigh(opcode, 0, 7) + "," + getThumbRegHigh(opcode, 3, 6);
			// No pc check: not applicable
			break;

		case thumb_cps:				// B6.1.1 CPS
									// cps<effect> <iflags> Not permitted in IT block.
			instruction = mnemonic + getThumbEffect(opcode) + "\t" + getThumbIFlags(opcode);
			// No pc check: not applicable
			break;

		case thumb_eor:				// A8.6.45 EOR (register)
									// eors <Rdn>,<Rm> Outside IT block.
									// eor<c> <Rdn>,<Rm> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: Rd cannot be PC
			break;

		case thumb_it:				// A8.6.50 IT
									// it{x{y{z}}} <firstcond> Not permitted in IT block
			int mask = opcode & 0xf;
			int cond = (opcode >> 4) & 0xf;
			int mask3 = (mask >> 3) & 1;
			int mask2 = (mask >> 2) & 1;
			int mask1 = (mask >> 1) & 1;
			int mask0 = mask & 1;
			int cond0 = cond & 1;
			String xyz = "";
			if ((mask3 == cond0) && ((mask & 7) == 4))
				xyz = "t";
			else if ((mask3 != cond0) && ((mask & 7) == 4))
				xyz = "e";
			else if ((mask3 == cond0) && (mask2 == cond0) && ((mask & 3) == 2))
				xyz = "tt";
			else if ((mask3 != cond0) && (mask2 == cond0) && ((mask & 3) == 2))
				xyz = "et";
			else if ((mask3 == cond0) && (mask2 != cond0) && ((mask & 3) == 2))
				xyz = "te";
			else if ((mask3 != cond0) && (mask2 != cond0) && ((mask & 3) == 2))
				xyz = "ee";
			else if ((mask3 == cond0) && (mask2 == cond0) && (mask1 == cond0) && (mask0 == 1))
				xyz = "ttt";
			else if ((mask3 != cond0) && (mask2 == cond0) && (mask1 == cond0) && (mask0 == 1))
				xyz = "ett";
			else if ((mask3 == cond0) && (mask2 != cond0) && (mask1 == cond0) && (mask0 == 1))
				xyz = "tet";
			else if ((mask3 != cond0) && (mask2 != cond0) && (mask1 == cond0) && (mask0 == 1))
				xyz = "eet";
			else if ((mask3 == cond0) && (mask2 == cond0) && (mask1 != cond0) && (mask0 == 1))
				xyz = "tte";
			else if ((mask3 != cond0) && (mask2 == cond0) && (mask1 != cond0) && (mask0 == 1))
				xyz = "ete";
			else if ((mask3 == cond0) && (mask2 != cond0) && (mask1 != cond0) && (mask0 == 1))
				xyz = "tee";
			else if ((mask3 != cond0) && (mask2 != cond0) && (mask1 != cond0) && (mask0 == 1))
				xyz = "eee";
			instruction = mnemonic + xyz + "\t" + getCondition(cond);
			// No pc check: not applicable
			break;

		case thumb_ldm:				// A8.6.53 LDM / LDMIA / LDMFD
									// ldm<c> <Rn>!,<registers> <Rn> not included in <registers>
									// ldm<c> <Rn>,<registers> <Rn> included in <registers>
			regOp = getThumbReg(opcode, 8);
			regList = getThumbRegList(opcode, null);
			String addExclaim = regList.contains(regOp)? "" : "!";
			instruction = mnemonic + "\t" + regOp + addExclaim + "," + regList;
			// No pc check: Rn cannot be PC and regList cannot contain PC
			break;

		case thumb_ldr__imm:		// A8.6.57 LDR (immediate, Thumb)
									// ldr<c> <Rt>, [<Rn>{,#<imm>}]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3);
			if (((opcode >> 6) & 0x1f) != 0)
				instruction += "," + getThumbImmediate5(opcode, 4);
			instruction += "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_ldr__imm_sp:		// A8.6.57 LDR (immediate, Thumb)
									// ldr<c> <Rt>,[sp{,#<imm>}]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + ",[sp";
			if ((opcode & 0xff) != 0)
				instruction += "," + getThumbImmediate8(opcode, 4);
			instruction += "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_ldr__lit:		// A8.6.59 LDR (literal)
									// ldr<c> <Rt>,<label>
									// ldr<c> <Rt>,[pc, #<imm>] Alternative form
			{
				long imm = (opcode & 0xff) * 4;
				String addr = Long.toHexString((address.getValue().longValue() & 0xfffffffc) + ((opcode & 0xff) * 4));
				int addrLen = addr.length();
				if (addrLen > 8)
					addr = addr.substring(addrLen - 8);

				instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + ",[pc,#" + getHexValue(imm) + "] ; 0x" + addr;
			}
			// No pc check: Rt cannot be PC
			break;

		case thumb_ldr__reg:		// A8.6.60 LDR (register)
									// ldr<c> <Rt>,[<Rn>,<Rm>]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_ldrb__imm:		// A8.6.61 LDRB (immediate, Thumb)
									// ldrb<c> <Rt>,[<Rn>{,#<imm5>}]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3);
			if (((opcode >> 6) & 0x1f) != 0)
				instruction += "," + getThumbImmediate5(opcode, 1);
			instruction += "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_ldrb__reg:		// A8.6.64 LDRB (register)
									// ldrb<c> <Rt>,[<Rn>,<Rm>]
		case thumb_ldrh__reg:		// A8.6.73 LDRH (immediate, Thumb)
									// ldrh<c> <Rt>,[<Rn>,<Rm>]
		case thumb_ldrsb:			// A8.6.80 LDRSB (register)
									// ldrsb<c> <Rt>,[<Rn>,<Rm>]
		case thumb_ldrsh:			// A8.6.84 LDRSH (register)
									// ldrsh<c> <Rt>,[<Rn>,<Rm>]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_ldrh__imm:		// A8.6.73 LDRH (immediate, Thumb)
									// ldrh<c> <Rt>,[<Rn>{,#<imm>}]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3);
			if (((opcode >> 6) & 0x1f) != 0)
				instruction += "," + getThumbImmediate5(opcode, 2);
			instruction += "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_lsl__imm:		// A8.6.88 LSL (immediate)
									// lsls <Rd>,<Rm>,#<imm5> Outside IT block.
									// lsl<c> <Rd>,<Rm>,#<imm5> Inside IT block.
		case thumb_lsr__imm:		// A8.6.90 LSR (immediate)
									// lsrs <Rd>,<Rm>,#<imm> Outside IT block.
									// lsr<c> <Rd>,<Rm>,#<imm> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3)
					+ ",#" + ((opcode >> 6) & 0x1f);
			// No pc check: Rt cannot be PC
			break;

		case thumb_lsl__reg:		// A8.6.89 LSL (register)
									// lsls <Rdn>,<Rm> Outside IT block.
									// lsl<c> <Rdn>,<Rm> Inside IT block.
		case thumb_lsr__reg:		// A8.6.91 LSR (register)
									// lsrs <Rdn>,<Rm> Outside IT block.
									// lsr<c> <Rdn>,<Rm> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: Rt cannot be PC
			break;

		case thumb_mov__imm:		// A8.6.96 MOV (immediate)
									// movs <Rd>,#<imm8> Outside IT block.
									// mov<c> <Rd>,#<imm8> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 8) + "," + getThumbImmediate8(opcode, 1);
			// No pc check: Rt cannot be PC
			break;

		case thumb_mov__reg:		// A8.6.97 MOV (register)
									// mov<c> <Rd>,<Rm> If <Rd> is PC, must be outside or
									//					last in IT block.
			regOp = getThumbRegHigh(opcode, 0, 7);
			instruction = mnemonic + "\t" + regOp + "," + getThumbRegHigh(opcode, 3, 6);
			if (regOp.equals("pc")) {
				isSoleDestination = true;
				isSubroutineAddress = false;
				addrExpression = getThumbRegHigh(opcode, 3, 6);
			}
			break;

		case thumb_movs:			// A8.6.97 MOV (register)
									// movs <Rd>,<Rm> Not permitted in IT block
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: Rd cannot be PC
			break;

		case thumb_mul:				// A8.6.105 MUL
									// muls <Rdm>,<Rn>,<Rdm> Outside IT block.
									// mul<c> <Rdm>,<Rn>,<Rdm> Inside IT block.
			instruction = mnemonic + "s\t"
					+ getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + "," + getThumbReg(opcode, 0);
			// No pc check: Rdm cannot be PC
			break;

		case thumb_mvn:				// A8.6.107 MVN (register)
									// mvns <Rd>,<Rm> Outside IT block.
									// mvn<c> <Rd>,<Rm> Inside IT block.
		case thumb_orr:				// A8.6.114 ORR (register)
									// orrs <Rdn>,<Rm> Outside IT block.
									// orr<c> <Rdn>,<Rm> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: Rd or Rdn cannot be PC
			break;

		case thumb_nop:				// A8.6.110 NOP
									// nop<c>
		case thumb_sev:				// A8.6.158 SEV
									// sev<c>
		case thumb_wfe:				// A8.6.411 WFE
									// wfe<c>
		case thumb_wfi:				// A8.6.412 WFI
									// wfi<c>
		case thumb_yield:			// A8.6.413 YIELD
									// yield<c>
			instruction = mnemonic;
			// No pc check: not applicable
			break;

		case thumb_pop:				// A8.6.122 POP
									// pop<c> <registers>
			bit = (opcode >> 8) & 1;
			if (bit == 1) {
				// This is an unconditional jump.
				regList = getThumbRegList(opcode, "pc");

				setDefaultPCJumpProperties(true);
			} else {
				regList = getThumbRegList(opcode, null);
			}
			instruction = mnemonic + "\t" + regList;
			break;

		case thumb_push:			// A8.6.123 PUSH
									// push<c> <registers>
			bit = (opcode >> 8) & 1;
			if (bit == 1) {
				regList = getThumbRegList(opcode, "lr");
			} else {
				regList = getThumbRegList(opcode, null);
			}
			instruction = mnemonic + "\t" + regList;
			// No pc check: not applicable
			break;

		case thumb_rev:				// A8.6.135 REV
			// rev<c> <Rd>,<Rm>
		case thumb_rev16:			// A8.6.136 REV16
			// rev16<c> <Rd>,<Rm>
		case thumb_revsh:			// A8.6.137 REVSH
			// revsh<c> <Rd>,<Rm>
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: Rd cannot be PC
			break;

		case thumb_ror:				// A8.6.140 ROR (register)
									// rors <Rdn>,<Rm> Outside IT block.
									// ror<c> <Rdn>,<Rm> Inside IT block.
		case thumb_sbc:				// A8.6.152 SBC (register)
									// sbcs <Rdn>,<Rm> Outside IT block.
									// sbc<c> <Rdn>,<Rm> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: Rd or Rdn cannot be PC
			break;

		case thumb_rsb:				// A8.6.142 RSB (immediate)
									// rsbs <Rd>,<Rn>,#0 Outside IT block.
									// rsb<c> <Rd>,<Rn>,#0 Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + ",#0";
			// No pc check: Rd or Rdn cannot be PC
			break;

		case thumb_setend:			// A8.6.157 SETEND
									// setend <endian_specifier> Not permitted in IT block
			String endian = (((opcode >> 3) & 1) == 1) ? "be" : "le";
			instruction = mnemonic + "\t" + endian;
			// No pc check: not applicable
			break;

		case thumb_stm:				// A8.6.189 STM / STMIA / STMEA
									// stm<c> <Rn>!,<registers>
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + "!," + getThumbRegList(opcode, null);
			// No pc check: not applicable
			break;

		case thumb_str__imm:		// A8.6.193 STR (immediate, Thumb)
									// str<c> <Rt>, [<Rn>{,#<imm>}]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3);
			if (((opcode >> 6) & 0x1f) != 0)
				instruction += "," + getThumbImmediate5(opcode, 4);
			instruction += "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_str__imm_sp:		// A8.6.193 STR (immediate, Thumb)
									// str<c> <Rt>,[sp,#<imm>]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + ",[sp," + getThumbImmediate8(opcode, 4) + "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_str__reg:		// A8.6.195 STR (register)
									// str<c> <Rt>,[<Rn>,<Rm>]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_strb__imm:		// A8.6.196 STRB (immediate, Thumb)
									// strb<c> <Rt>,[<Rn>,#<imm5>]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3);
			if (((opcode >> 6) & 0x1f) != 0)
				instruction += "," + getThumbImmediate5(opcode, 1);
			instruction += "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_strb__reg:		// A8.6.198 STRB (register)
									// strb<c> <Rt>,[<Rn>,<Rm>]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_strh__imm:		// A8.6.206 STRH (immediate, Thumb)
									// strh<c> <Rt>,[<Rn>{,#<imm>}]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3);
			if (((opcode >> 6) & 0x1f) != 0)
				instruction += "," + getThumbImmediate5(opcode, 2);
			instruction += "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_strh__reg:		// A8.6.208 STRH (register)
									// strh<c> <Rt>,[<Rn>,<Rm>]
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			// No pc check: Rt cannot be PC
			break;

		case thumb_sub__imm:		// A8.6.211 SUB (immediate, Thumb)
									// subs <Rdn>,#<imm8> Outside IT block.
									// sub<c> <Rdn>,#<imm8> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 8) + "," + getThumbImmediate8(opcode, 1);
			// No pc check: Rdn cannot be PC
			break;

		case thumb_sub__imm_from_sp:	// A8.6.215 SUB (SP minus immediate)
									// sub<c> sp,sp,#<imm>
			instruction = mnemonic + "\tsp,sp," + getThumbImmediate7(opcode, 4);
			// No pc check: not applicable
			break;

		case thumb_sub__reg_imm:	// A8.6.211 SUB (immediate, Thumb)
									// subs <Rd>,<Rn>,#<imm3> Outside IT block.
									// sub<c> <Rd>,<Rn>,#<imm3> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + ","
					+ getThumbImmediate3(opcode);
			// No pc check: Rd cannot be PC
			break;

		case thumb_sub__reg_reg:	// A8.6.213 SUB (register)
									// subs <Rd>,<Rn>,<Rm> Outside IT block.
									// sub<c> <Rd>,<Rn>,<Rm> Inside IT block.
			instruction = mnemonic + "s\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6);
			// No pc check: Rd cannot be PC
			break;

		case thumb_sxtb:			// A8.6.223 SXTB
									// sxtb<c> <Rd>,<Rm>
		case thumb_sxth:			// A8.6.225 SXTH
									// sxth<c> <Rd>,<Rm>
		case thumb_tst:				// A8.6.231 TST (register)
									// tst<c> <Rn>,<Rm>
		case thumb_uxtb:			// A8.6.263 UXTB
									// uxtb<c> <Rd>,<Rm>
		case thumb_uxth:			// A8.6.265 UXTH
									// uxth<c> <Rd>,<Rm>
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			// No pc check: Rd cannot be PC
			break;

		case thumb_undefined:
			instruction = mnemonic;
			// No pc check: not applicable
			break;

		default:
			instruction = IDisassembler.INVALID_OPCODE;
			break;
		}
		return instruction;
	}


	/**
	 * Disassemble a 32-bit Thumb instruction
	 * Reference manual citations (e.g., "A8.6.4") refer to sections in the ARM Architecture
	 * Reference Manual ARMv7-A and ARMv7-R Edition with errata markup
	 * @param opcode instruction to parse 
	 * @return disassembled instruction
	 */
	private String parseThumb2Opcode(int opcode) throws BufferUnderflowException {
		OpcodeARM.Index opcodeIndex = OpcodeARM.Index.invalid;
		String mnemonic = "";

		for (OpcodeARM thumb2Opcode : OpcodeARM.thumb2_opcode_table) {
			int result = opcode & thumb2Opcode.getOpcodeMask();
			if (result == thumb2Opcode.getOpcodeResult()) {
				opcodeIndex = thumb2Opcode.getIndex();
				if (ARMv6 > versionMode
					&& opcodeIndex != OpcodeARM.Index.thumb2_bl
					&& opcodeIndex != OpcodeARM.Index.thumb2_blx) {
					return IDisassembler.INVALID_OPCODE;
				}

				mnemonic = thumb2Opcode.getMnemonic();
				break;
			}
		}

		String instruction = "";
		String regDest = "";
		int offset;
		boolean checkPC = false;
		switch (opcodeIndex) {

		case thumb2_add__imm:	// A8.6.4 ADD (immediate)		// add{s}<c>.w <Rd>,<Rn>,#<const>
		case thumb2_rsb__imm:	// A8.6.142 RSB (immediate)		// rsb{s}<c>.w <Rd>,<Rn>,#<const>
		case thumb2_sub__imm:	// A8.6.211 SUB (immediate)		// sub{s}<c>.w <Rd>,<Rn>,#<const>
			instruction = ".w";
// no break!
		case thumb2_adc__imm:	// A8.6.1 ADC (immediate)		// adc{s}<c> <Rd>,<Rn>,#<const>
		case thumb2_and__imm:	// A8.6.11 AND (immediate)		// and{s}<c> <Rd>,<Rn>,#<const>
		case thumb2_bic__imm:	// A8.6.19 BIC (immediate)		// bic{s}<c> <Rd>,<Rn>,#<const>
		case thumb2_eor__imm:	// A8.6.44 EOR (immediate)		// eor{s}<c> <Rd>,<Rn>,#<const>
		case thumb2_orn__imm:	// A8.6.111 ORN (immediate)		// orn{s}<c> <Rd>,<Rn>,#<const>
		case thumb2_orr__imm:	// A8.6.113 ORR (immediate)		// orr{s}<c> <Rd>,<Rn>,#<const>
		case thumb2_sbc__imm:	// A8.6.151 SBC (immediate)		// sbc{s}<c> <Rd>,<Rn>,#<const>
			// . . . . . i_1_10_10 . . . . . S_1_4_4 Rn_1_3_0 . imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
			mnemonic += getS(opcode) + instruction;
			instruction = mnemonic + TAB + getR_8(opcode) + ',' + getR_16(opcode)
						  + ",#" + getThumb2ExpandImm12(opcode);
			// No pc check: if Rd is PC, the instruction translates to another enumeral or is unpredictable
			break;

		case thumb2_addw:	// A8.6.4 ADD (immediate, Thumb)	// addw<c> <Rd>,<Rn>,#<imm12>
		case thumb2_subw:	// A8.6.211 SUB (immediate, Thumb)	// subw<c> <Rd>,<Rn>,#<imm12>
			// . . . . . i_1_10_10 . . . . . . Rn_1_3_0 . imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
			instruction = mnemonic + TAB + getR_8(opcode) + ',' + getR_16(opcode)
						  + ",#" + getHexValue(getThumb2RawImm12(opcode));
			// No pc check: if Rd is PC, the instruction translates to another enumeral or is unpredictable
			break;

		case thumb2_adc__reg:	// A8.6.2 ADC (register)		// adc{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		case thumb2_add__reg:	// A8.6.6 ADD (register)		// add{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		case thumb2_and__reg:	// A8.6.12 AND (register)		// and{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		case thumb2_bic__reg:	// A8.6.20 BIC (register)		// bic{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		case thumb2_eor__reg:	// A8.6.45 EOR (register)		// eor{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		case thumb2_orr__reg:	// A8.6.114 ORR (register)		// orr{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		case thumb2_sbc__reg:	// A8.6.152 SBC (register)		// sbc{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		case thumb2_sub__reg:	// A8.6.213 SUB (register)		// sub{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
			instruction = ".w";
// no break!
		case thumb2_orn__reg:	// A8.6.112 ORN (register)		// orn{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		case thumb2_rsb__reg:	// A8.6.143 RSB (register)		// rsb{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
			// . . . . . . . . . . . S_1_4_4 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
			mnemonic += getS(opcode) + instruction;
			instruction = mnemonic + TAB + getR_8(opcode) + ',' + getR_16(opcode)
						  + ',' + getR_0(opcode) + getThumb2ShiftMode(opcode, 4);
			// No pc check: if Rd is PC, the instruction translates to another enumeral or is unpredictable
			break;

		case thumb2_mov__imm:	// A8.6.96 MOV (immediate)		// mov{s}<c>.w <Rd>,#<const>
			instruction = ".w";
// no break!
		case thumb2_mvn__imm:	// A8.6.106 MVN (immediate)		// mvn{s}<c> <Rd>,#<const>
			// 1 1 1 1 0 i_1_10_10 0 0 0 1 1 S_1_4_4 1 1 1 1 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
			mnemonic += getS(opcode) + instruction;
			instruction = mnemonic + TAB + getR_8(opcode)+ ",#" + getThumb2ExpandImm12(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case thumb2_mvn__reg:	// A8.6.107 MVN (register)		// mvn{s}<c>.w <Rd>,<Rm>{,<shift>}
			// . . . . . . . . . . . S_1_4_4 . . . . (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
			instruction = mnemonic + getS(opcode) + ".w\t" + getR_8(opcode) + ','
						  + getR_0(opcode) + getThumb2ShiftMode(opcode, 4);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case thumb2_adr__sub:		// A8.6.10 ADR	// adr<c>.w <Rd>,<label>
												// add<c> <Rd>,pc,#imm12	Alternate form
		case thumb2_adr__add:		// A8.6.10 ADR	// adr<c>.w <Rd>,<label>
												// sub<c> <Rd>,pc,#imm12	Alternate form
			instruction = mnemonic + TAB + getR_8(opcode) + ",pc,#"
						  + getHexValue(getThumb2RawImm12(opcode));
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case thumb2_asr__imm:	// A8.6.14 ASR (immediate)		// asr{s}<c>.w <Rd>,<Rm>,#<imm>
		case thumb2_lsl__imm:	// A8.6.88 LSL (immediate)		// lsl{s}<c>.w <Rd>,<Rm>,#<imm>
		case thumb2_lsr__imm:	// A8.6.90 LSR (immediate)		// lsr{s}<c>.w <Rd>,<Rm>,#<imm>
			instruction = ".w";
		case thumb2_ror__imm:	// A8.6.139 ROR (immediate)		// ror{s}<c> <Rd>,<Rm>,#<imm>
			// . . . . . . . . . . . S_1_4_4 . . . . (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 . . Rm_0_3_0
			mnemonic += getS(opcode) + instruction;
			instruction = mnemonic + TAB + getR_8(opcode) + ',' + getR_0(opcode)
						  + ",#" + getThumb2ShiftValue(opcode, opcode >> 4 & 3);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case thumb2_asr__reg:	// A8.6.15 ASR (register)		// asr{s}<c>.w <Rd>,<Rn>,<Rm>
		case thumb2_lsl__reg:	// A8.6.89 LSL (register)		// lsl{s}<c>.w <Rd>,<Rm>,<Rm>
		case thumb2_lsr__reg:	// A8.6.91 LSR (register)		// lsr{s}<c>.w <Rd>,<Rn>,<Rm>
		case thumb2_ror__reg:	// A8.6.140 ROR (register)		// ror{s}<c>.w <Rd>,<Rn>,<Rm>
			// . . . . . . . . . . . S_1_4_4 Rn_1_3_0 . . . . Rd_0_11_8 . . . . Rm_0_3_0
			mnemonic += getS(opcode) + ".w\t";
			instruction = mnemonic + getR_8(opcode) + ',' + getR_16(opcode) + ',' + getR_0(opcode);
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;

		case thumb2_b__cond:	// A8.6.16 B			// b<c>.w <label> Not permitted in IT block.
			// 1 1 1 1 0 S_1_10_10 cond_1_9_6 imm6_1_5_0 1 0 J1_0_13_13 0 J2_0_11_11 imm11_0_10_0
			instruction = getThumb2Condition(opcode);
			setDefaultPCJumpProperties(instruction.length() == 0); // true if unconditional b
			offset = getThumb2_condB_Offset(opcode);
			jumpToAddr = address.add(offset);
			instruction = mnemonic + instruction + ".w\t" + jumpToAddr.toHexAddressString();
			break;

		case thumb2_b__uncond:	// A8.6.16 B				// b<c>.w <label> Outside or last in IT block
			if (ARMv6 > versionMode) {
				instruction = IDisassembler.INVALID_OPCODE;
				break;
			}
			// otherwise, no break!
		case thumb2_bl:			// A8.6.23 BL (immediate)	// bl<c> <label> Outside or last in IT block
			// 1 1 1 1 0 S_1_10_10 imm10_1_9_0 1 1 J1_0_13_13 1 J2_0_11_11 imm11_0_10_0
			// . . . . . S_1_10_10 imm10_1_9_0 . . J1_0_13_13 . J2_0_11_11 imm11_0_10_0
		case thumb2_blx:		// A8.6.23 BLX (immediate)	// blx<c> <label> Outside or last in IT block
			// 1 1 1 1 0 S_1_10_10 imm10H_1_9_0 1 1 J1_0_13_13 0 J2_0_11_11 imm10L_0_10_1 h_0_1_0

			// if you are reading this and confused that the above 3 are "the same",
			// my apologies.  the ref manual used to state Encoding T2 bit 0
			// was always 0, and now has a late annotation that bit 0 is
			// supposed to be H, and that blx (immediate) is UNDEFINED for h==1.
			// since h must be 0, and SignExtend() for the 2 cases are:
			//		imm32 = SignExtend(S:I1:I2:imm10:imm11:'0', 32);	// Encoding T1
			//  	imm32 = SignExtend(S:I1:I2:imm10H:imm10L:'00', 32)	// Encoding T2
			// the conclusion is that pretending to get imm11_0_10_0
			// for the blx (immediate) T2 case is the same since the
			// end '0' will match the left 0 in the '00' for T2.
			// thus it's okay to use the same code for both cases.

			{ // first, disallow this conditionally based on ref-manual rules if pre-ARMv6

				boolean j1 = isBitEnabled(opcode, 13), j2 = isBitEnabled(opcode, 11);
				if (!(j1 & j2) && ARMv6 > versionMode) {
					instruction = IDisassembler.INVALID_OPCODE;
					break;
				}
	
				offset = getThumb2_uncondB_Offset(opcode, j1, j2);
			}

			jumpToAddr = address.add(offset); // immediate address known
			instruction = mnemonic + TAB + jumpToAddr.toHexAddressString();
			isSoleDestination = true;
			isSubroutineAddress = (opcodeIndex != OpcodeARM.Index.thumb2_b__uncond);
			break;

		case thumb2_bfi:		// A8.6.18 BFI		// bfi<c> <Rd>,<Rn>,#<lsb>,#<width>
			// 1 1 1 1 0 (0) 1 1 0 1 1 0 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 (0) msb_0_4_0
			instruction = "," + getR_16(opcode);
// no break!
		case thumb2_bfc:		// A8.6.17 BFC		// bfc<c> <Rd>,#<lsb>,#<width>
			// 1 1 1 1 0 (0) 1 1 0 1 1 0 1 1 1 1 0 imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 (0) msb_0_4_0
		{
			int lsb = opcode >> 10 & 0x1c | opcode >> 6 & 3;
			int width = (opcode & 0x1f) - lsb + 1;
			instruction = mnemonic + TAB + getR_8(opcode) + instruction + ",#" + lsb + ",#" + width;
			// No pc check: if Rd is PC, the instruction is unpredictable
			break;
		}

		case thumb2_bfx:		// A8.6.154 SBFX	// sbfx<c> <Rd>,<Rn>,#<lsb>,#<width>
								// A8.6.236 UBFX	// ubfx<c> <Rd>,<Rn>,#<lsb>,#<width>
			// . . . . . (0) . . U_1_7_7 . . . Rn_1_3_0 . imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 (0) widthm1
			mnemonic = (isBitEnabled(opcode, 23) ? "u" : "s") + mnemonic;
			instruction = mnemonic + TAB + getR_8(opcode) + ',' + getR_16(opcode)
				  + ",#" + getThumb2ShiftValue(opcode, 0) + ",#" + ((opcode & 0x1f) + 1);
			// No pc check: PC at Rd is UNPREDICTABLE
			break;

		case thumb2_bxj:		// A8.6.26 BXJ		// bxj<c> <Rm> Outside or last in IT block
			// 1 1 1 1 0 0 1 1 1 1 0 0 Rm_1_3_0 1 0 (0) 0 (1)(1)(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)
			instruction = mnemonic + TAB + getR_16(opcode);
			setDefaultPCJumpProperties(true);
			break;

		case thumb2_clrex:		// A8.6.30 CLREX	// clrex<c>
			if (ARMv7 > versionMode) {
				instruction = IDisassembler.INVALID_OPCODE;
				break;
			}
			// else no break!
			// 1 1 1 1 0 0 1 1 1 0 1 1 (1)(1)(1) (1) 1 0 (0) 0 (1)(1)(1)(1) 0 0 1 0 (1)(1)(1)(1)
		case thumb2_sev:		// A8.6.158 SEV		// sev<c>.w
		case thumb2_wfe:		// A8.6.411 WFE		// wfe<c>.w
		case thumb2_wfi:		// A8.6.412 WFI		// wfi<c>.w
		case thumb2_yield:		// A8.6.413 YIELD	// yield<c>.w
			// . . . . . . . . . . . . (1)(1)(1) (1) . . (0) . (0) . . . . . . . . . . .
			if (ARMv6T2 == versionMode) {
				mnemonic = "nop.w";
			}
			// else no break!
		case thumb2_nop:
		case thumb2_undefined:
			if (ARMv6K == versionMode) {
				instruction = IDisassembler.INVALID_OPCODE;
			} else if (ARMv6T2 <= versionMode) {
				instruction = mnemonic;
			}
			// No pc check: no registers changed
			break;

		case thumb2_reverse:	// A8.6.134 RBIT	// rbit<c> <Rd>,<Rm>
								// A8.6.135 REV		// rev<c>.w <Rd>,<Rm>
								// A8.6.136 REV16	// rev16<c>.w <Rd>,<Rm>
								// A8.6.137 REVSH	// revsh<c>.w <Rd>,<Rm>
			switch (opcode >> 4 & 3) {
				case 0:		mnemonic += "ev.w";		break;
				case 1:		mnemonic += "ev16.w";	break;
				case 2:		mnemonic += "bit";		break;
				case 3:		mnemonic += "evsh.w";	break;
			}
// no break!
		case thumb2_clz:		// A8.6.31 CLZ		// clz<c> <Rd>,<Rm>
			// . . . . . . . . . . . . Rm_1_3_0 . . . . Rd_0_11_8 . . . . Rm_0_3_0
			regDest = getR_8(opcode);
			instruction = mnemonic + TAB + regDest + ',' + getR_0(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE
			break;

		case thumb2_cmn__imm:	// A8.6.32 CMN (immediate)	// cmn<c> <Rn>,#<const>
		case thumb2_cmp__imm:	// A8.6.35 CMP (immediate)	// cmp<c>.w <Rn>,#<const>
		case thumb2_teq__imm:	// A8.6.227 TEQ (immediate)	// teq<c> <Rn>,#<const>
		case thumb2_tst__imm:	// A8.6.230 TST (immediate)	// tst<c> <Rn>,#<const>
			// . . . . . i_1_10_10 . . . . . . Rn_1_3_0 . imm3_0_14_12 . . . . imm8_0_7_0
			instruction = mnemonic + TAB + getR_16(opcode)
						  + ",#" + getThumb2ExpandImm12(opcode);
			// No pc check: no registers changed
			break;

		case thumb2_cmn__reg:	// A8.6.33 CMN (register)	// cmn<c> <Rn>,<Rm>{,<shift>}
		case thumb2_cmp__reg:	// A8.6.36 CMP (register)	// cmp<c>.w <Rn>,<Rm> {,<shift>}
		case thumb2_teq__reg:	// A8.6.228 TEQ (register)	// teq<c> <Rn>,<Rm>{,<shift>}
		case thumb2_tst__reg:	// A8.6.231 TST (register)	// tst<c> <Rn>,<Rm>{,<shift>}
			// . . . . . . . . . . . . Rn_1_3_0 (0) imm3_0_14_12 . . . . imm2_0_7_6 type_0_5_4 Rm_0_3_0
			instruction = mnemonic + instruction + TAB + getR_16(opcode) + ','
						  + getR_0(opcode) + getThumb2ShiftMode(opcode, 4);
			// No pc check: no registers changed
			break;

		case thumb2_dbg:		// A8.6.40 DBG			// dbg<c> #<option>
			// . . . . . . . . . . . . (1)(1)(1) (1) . . (0) . (1)(1)(1)(1) . . . . option_0_3_0
			if (ARMv6T2 == versionMode) {
				instruction = "nop.w";
			} else if (ARMv7 > versionMode) {
				instruction = IDisassembler.INVALID_OPCODE;
			} else {
				instruction = mnemonic + "\t#" + (opcode & 0xf);
			}
			// No pc check: no registers changed
			break;

		case thumb2_dmb:		// A8.6.41 DMB			// dmb<c> #<option>
		case thumb2_dsb:		// A8.6.42 DSB			// dsb<c> #<option>
			if (ARMv7 > versionMode)
				instruction = IDisassembler.INVALID_OPCODE;
			else
				instruction = mnemonic + TAB + getDataBarrierOption(opcode);
			// No pc check: no registers changed
			break;

		case thumb2_enterx_leavex:	// A9.3.1 ENTERX, LEAVEX
			// enterx  Not permitted in IT block.	leavex  Not permitted in IT block.
			// 1 1 1 1 0 0 1 1 1 0 1 1 (1)(1)(1) (1) 1 0 (0) 0 (1)(1)(1)(1) 0 0 0 J_0_4_4 (1)(1)(1)(1)
			// . . . . . . . . . . . . (1)(1)(1) (1) . . (0) . (1)(1)(1)(1) . . . J_0_4_4 (1)(1)(1)(1)
			instruction = isBitEnabled(opcode, 4) ? mnemonic : "leavex";
			break;

		case thumb2_isb:		// A8.6.49 ISB			// isb<c> #<option>
			// . . . . . . . . . . . . (1)(1)(1) (1) . . (0) . (1)(1)(1)(1) . . . . option_0_3_0
			if (ARMv7 > versionMode)
				instruction = IDisassembler.INVALID_OPCODE;
			else
				instruction = mnemonic + TAB + getInstructionBarrierOption(opcode);
			// No pc check: no registers changed
			break;

		case thumb2_ldm:		// A8.6.53 LDM / LDMIA / LDMFD		// ldm<c>.w <Rn>{!},<registers>
		case thumb2_ldmdb:		// A8.6.55 LDMDB / LDMEA			// ldmdb<c> <Rn>{!},<registers>
			// . . . . . . . . . . W_1_5_5 . Rn_1_3_0 P_0_15_15 M_0_14_14 (0) register_list_0_12_0
			regDest = getRegList(opcode);
			instruction = mnemonic + TAB + getR_16(opcode) + getW(opcode) + ',' + regDest;
			if (regDest.contains("pc"))
				setDefaultPCJumpProperties(true);
			break;

		case thumb2_ldr:
			if (isBitEnabled(opcode, 22))
				checkPC = true;	// most other cases will end up in thumb2_pld or thumb2_pli
		case thumb2_str:
			regDest = getR_12(opcode);
			{
				String rn = getR_16(opcode);
				boolean isPC = rn.equals("pc");
				boolean isReg = 0 == (opcode >> 6 & 0x3f);
				if (isBitEnabled(opcode, 22)) {
					// A8.6.57 LDR (immediate, Thumb)	// ldr<c>.w
					// A8.6.59 LDR (literal)			// ldr.w
					// A8.6.60 LDR (register)			// ldr<c>.w
					// A8.6.193 STR (immediate, Thumb)	// str<c>.w
					// A8.6.195 STR (register)			// str<c>.w
					if (isPC || isBitEnabled(opcode, 23) || isReg)
						mnemonic += ".w";
					else if (0xe == (opcode >> 8 & 0xf))
						mnemonic += 't';
						
				} else if (isBitEnabled(opcode, 24)) {
					// A8.6.78 LDRSB (immediate)
					// A8.6.82 LDRSH (immediate)
					// A8.6.80 LDRSB (register)	// ldrsb<c>.w
					// A8.6.84 LDRSH (register)	// ldrsh<c>.w
					mnemonic += isBitEnabled(opcode, 21) ? "sh" : "sb";
					if (!isPC) {
						if (0xe == (opcode >> 8 & 0xf))
							mnemonic += 't';
						else if (!isBitEnabled(opcode, 23) && isReg)
							mnemonic += ".w";
					}
				} else {
					// A8.6.61 LDRB (immediate, Thumb)	// ldrb<c>.w
					// A8.6.63 LDRB (literal)			// ldrb
					// A8.6.64 LDRB (register)			// ldrb<c>.w
					// A8.6.75 LDRH (literal)			// ldrh
					// A8.6.73 LDRH (immediate, Thumb)	// ldrh<c>.w
					// A8.6.76 LDRH (register)			// ldrh<c>.w
					// A8.6.196 STRB (immediate, Thumb)	// strb<c>.w
					// A8.6.198 STRB (register)			// strb<c>.w
					// A8.6.206 STRH (immediate, Thumb)	// strh<c>.w
					// A8.6.208 STRH (register)			// strh<c>.w
					mnemonic += isBitEnabled(opcode, 21) ? "h" : "b";
					if (!isPC) {
						if (isBitEnabled(opcode, 23) || isReg)
							mnemonic += ".w";
						else if (0xe == (opcode >> 8 & 0xf))
							mnemonic += 't';
					}
				}
				instruction = mnemonic + TAB + regDest + ',';
				if (isPC) {
					// A8.6.59 LDR (literal)	// ldr.w <Rt>,[pc,#+/-<imm>]	Alternative form
					// A8.6.63 LDRB (literal)	// ldrb <Rt>,[pc,#+/-<imm>]		Alternative form
					// A8.6.75 LDRH (literal)	// ldrh <Rt>,[pc,#+/-<imm>]		Alternative form
					// A8.6.79 LDRSB (literal)	// ldrsb <Rt>,[pc,#+/-<imm>]	Alternative form
					// A8.6.83 LDRSH (literal)	// ldrsh <Rt>,[pc,#+/-<imm>]	Alternative form

					// appends addr as UAL comment
					instruction += getAddrModePCImm(opcode, opcode & 0xfff);
				} else if (isBitEnabled(opcode, 23)) {
					// A8.6.57 LDR (immediate, Thumb)	// ldr<c>.w <Rt>,[<Rn>{,#<imm12>}]
					// A8.6.61 LDRB (immediate, Thumb)	// ldrb<c>.w <Rt>,[<Rn>{,#<imm12>}]
					// A8.6.73 LDRH (immediate, Thumb)	// ldrh<c>.w <Rt>,[<Rn>{,#<imm12>}]
					// A8.6.78 LDRSB (immediate)		// ldrsb<c> <Rt>,[<Rn>{,#<imm12>}]
					// A8.6.82 LDRSH (immediate)		// ldrsh<c> <Rt>,[<Rn>,#<imm12>]
					// A8.6.193 STR (immediate, Thumb)	// str<c>.w <Rt>,[<Rn>{,#<imm12>}]
					// A8.6.196 STRB (immediate, Thumb)	// strb<c>.w <Rt>,[<Rn>{,#<imm12>}]
					// A8.6.206 STRH (immediate, Thumb)	// strh<c>.w <Rt>,[<Rn>{,#<imm12>}]
					instruction += '[' + rn;
					offset = opcode & 0xfff;
					if (offset != 0)
						instruction += ",#" + getHexValue(offset);
					instruction += ']';
				} else if (isReg) {
					// A8.6.60 LDR (register)	// ldr<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
					// A8.6.64 LDRB (register)	// ldrb<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
					// A8.6.76 LDRH (register)	// ldrh<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
					// A8.6.80 LDRSB (register)	// ldrsb<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
					// A8.6.84 LDRSH (register)	// ldrsh<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
					// A8.6.195 STR (register)	// str<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
					// A8.6.198 STRB (register)	// strb<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
					// A8.6.208 STRH (register)	// strh<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
					instruction += getThumb2AddrModeRegImm(opcode);
				} else {
					// A8.6.57 LDR (immediate, Thumb)
					// A8.6.61 LDRB (immediate, Thumb)
					// A8.6.73 LDRH (immediate, Thumb)
					// A8.6.78 LDRSB (immediate)
					// A8.6.82 LDRSH (immediate)
					// A8.6.193 STR (immediate, Thumb)
					// A8.6.196 STRB (immediate, Thumb)					
					// A8.6.206 STRH (immediate, Thumb)	
					//			<mnemonic><c> <Rt>,[<Rn>,#-<imm8>]
					//			<mnemonic> <Rt>,[<Rn>],#+/-<imm8>
					//			<mnemonic> <Rt>,[<Rn>,#+/-<imm8>]!
					
					instruction += getThumb2AddrModeImm8(opcode, 10, 9, 8, 0);
				}
			}
			if (checkPC && regDest.equals("pc"))
				setDefaultPCJumpProperties(true);
			break;

		case thumb2_ldrex:		// A8.6.69 LDREX			// ldrex<c> <Rt>,[<Rn>{,#<imm>}]
			// . . . . . . . . . . . . Rn_1_3_0 Rt_0_15_12 . . . . imm8_0_7_0
			offset = opcode & 0xff;
			instruction = mnemonic + TAB + getR_12(opcode) + ",[" + getR_16(opcode);
			if (offset != 0)
				instruction +=	",#" + getHexValue(offset << 2);
			instruction += ']';
			// No pc check: PC at Rt location will generate different instruction or UNPREDICTABLE
			break;

		case thumb2_ldrd__imm:	// A8.6.66 LDRD (immediate)	// ldrd<c> <Rt>,<Rt2>,[<Rn>{,#+/-<imm>}]
															// ldrd <Rt>,<Rt2>,[<Rn>],#+/-<imm>
															// ldrd <Rt>,<Rt2>,[<Rn>,#+/-<imm>]!
		case thumb2_strd:		// A8.6.200 STRD (immediate)// strd<c> <Rt>,<Rt2>,[<Rn>{,#+/-<imm>}]
															// strd <Rt>,<Rt2>,[<Rn>],#+/-<imm>
															// strd <Rt>,<Rt2>,[<Rn>,#+/-<imm>]!
			// . . . . . . . P_1_8_8 U_1_7_7 . W_1_5_5 . Rn_1_3_0 Rt_0_15_12 Rt2_0_11_8 imm8_0_7_0
			instruction = mnemonic + TAB + getR_12(opcode) + ',' + getR_8(opcode)
						  + ',' + getThumb2AddrModeImm8(opcode, 24, 23, 21, 2);
			// No pc check: PC in <Rt,Rt2> is UNPREDICTABLE
			break;

		case thumb2_ldrd__lit:	// A8.6.67 LDRD (literal)	// ldrd<c> <Rt>,<Rt2>,<label>	ldrd <Rt>,<Rt2>,[pc,#-0] Special case
			// 1 1 1 0 1 0 0 P_1_8_8 U_1_7_7 1 (0) 1 1 1 1 1 Rt_0_15_12 Rt2_0_11_8 imm8_0_7_0
			offset = opcode & 0xff;
			instruction = mnemonic + TAB + getR_12(opcode) + ',' + getR_8(opcode)
						  + ",[pc,#" + ((isBitEnabled(opcode, 23) && offset != 0) ? "" : "-")
						  + getHexValue(offset<<2) + ']';
			// No pc check: PC at Rt is UNPREDICTABLE
			break;

		case thumb2_ldrexx:
			// A8.6.70 LDREXB			// ldrexb<c> <Rt>, [<Rn>]
			// A8.6.71 LDREXD			// ldrexd<c> <Rt>,<Rt2>,[<Rn>]
			// A8.6.72 LDREXH			// ldrexh<c> <Rt>, [<Rn>]
			// . . . . . . . . . . . . Rn_1_3_0 Rt_0_15_12 (1)(1)(1)(1) . . . . (1)(1)(1)(1)
			if (isBitEnabled(opcode, 5)) {
				mnemonic += 'd';
				instruction = "," + getR_8(opcode);
			} else {
				mnemonic += isBitEnabled(opcode, 4) ? 'h' : 'b';
			}
			instruction = mnemonic + TAB + getR_12(opcode) + instruction
					  + ",[" + getR_16(opcode) + ']';
			// No pc check: PC at Rt is UNPREDICTABLE
			break;

		case thumb2_ml:			// A8.6.94 MLA				// mla<c> <Rd>,<Rn>,<Rm>,<Ra>
			mnemonic += isBitEnabled(opcode, 4) ? 's' : 'a';
		case thumb2_usada8:		// A8.6.254 USADA8			// usada8<c> <Rd>,<Rn>,<Rm>,<Ra>
			// . . . . . . . . . . . . Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 . . . . Rm_0_3_0
			instruction = mnemonic + TAB + getR_8(opcode) + ',' + getR_16(opcode)
						  + ',' + getR_0(opcode) + ',' + getR_12(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE
			break;

		case thumb2_mov__reg:	// A8.6.97 MOV (register)		// mov{s}<c>.w <Rd>,<Rm>
			instruction = ".w";
			checkPC = true;
// no break!
		case thumb2_rrx:		// A8.6.141 RRX					// rrx{s}<c> <Rd>,<Rm>
			// . . . . . . . . . . . S_1_4_4 . . . . (0) . . . Rd_0_11_8 . . . . Rm_0_3_0
			regDest = getR_8(opcode);
			instruction = mnemonic + getS(opcode) + instruction + TAB + regDest + "," + getR_0(opcode);
			if (checkPC && regDest.equals("pc"))
				setDefaultPCJumpProperties(true);
			break;

		case thumb2_movx:		// A8.6.96 MOV (immediate)		// movw<c> <Rd>,#<imm16>
								// A8.6.99 MOVT					// movt<c> <Rd>,#<imm16>
			// . . . . . i_1_10_10 . . . . . . imm4_1_3_0 . imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
			mnemonic += isBitEnabled(opcode, 23) ? 't' : 'w';
			instruction = mnemonic + TAB + getR_8(opcode) + ",#" + getThumb2ImmForMovX(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE
			break;

		case thumb2_mrs:
			// A8.6.102 MRS
			// mrs<c> <Rd>,<spec_reg>
			// 1 1 1 1 0 0 1 1 1 1 1 0 (1)(1)(1) (1) 1 0 (0) 0 Rd_0_11_8 (0)(0)(0)(0)(0)(0)(0)(0)
			// B6.1.5 MRS
			// mrs <Rd>,<spec_reg>
			// 1 1 1 1 0 0 1 1 1 1 1 R_1_4_4 (1)(1)(1) (1) 1 0 (0) 0 Rd_0_11_8 (0)(0)(0)(0)(0)(0)(0)(0)
			instruction = mnemonic + TAB + getR_8(opcode)
						  + ',' + (isBitEnabled(opcode, 20) ? 's' : 'c') + "psr";
			// No pc check: PC at Rd is UNPREDICTABLE
			break;

		case thumb2_msr:
			// B6.1.7 MSR (register)
			// msr <spec_reg>,<Rn>
			// 1 1 1 1 0 0 1 1 1 0 0 R_1_4_4 Rn_1_3_0 1 0 (0) 0 mask_0_11_8 (0)(0)(0)(0)(0)(0)(0)(0)
			instruction = mnemonic + TAB + getStatusReg(opcode, 20)
						  + getStatusRegFields(opcode, 8) + ',' + getR_16(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE
			break;

		case thumb2__r_dnm_math:
			// A8.6.125 QADD16	// qadd16<c>  <Rd>,<Rn>,<Rm>
			// A8.6.126 QADD8	// qadd8<c>   <Rd>,<Rn>,<Rm>
			// A8.6.127 QASX	// qasx<c>    <Rd>,<Rn>,<Rm>
			// A8.6.130 QSAX	// qsax<c>    <Rd>,<Rn>,<Rm>
			// A8.6.132 QSUB16	// qsub16<c>  <Rd>,<Rn>,<Rm>
			// A8.6.133 QSUB8	// qsub8<c>   <Rd>,<Rn>,<Rm>
			// A8.6.148 SADD16	// sadd16<c>  <Rd>,<Rn>,<Rm>
			// A8.6.149 SADD8	// sadd8<c>   <Rd>,<Rn>,<Rm>
			// A8.6.150 SASX	// sasx<c>    <Rd>,<Rn>,<Rm>
			// A8.6.159 SHADD16	// shadd16<c> <Rd>,<Rn>,<Rm>
			// A8.6.160 SHADD8	// shadd8<c>  <Rd>,<Rn>,<Rm>
			// A8.6.161 SHASX	// shasx<c>   <Rd>,<Rn>,<Rm>
			// A8.6.162 SHSAX	// shsax<c>   <Rd>,<Rn>,<Rm>
			// A8.6.163 SHSUB16	// shsub16<c> <Rd>,<Rn>,<Rm>
			// A8.6.164 SHSUB8	// shsub8<c>  <Rd>,<Rn>,<Rm>
			// A8.6.185 SSAX	// ssax<c>    <Rd>,<Rn>,<Rm>
			// A8.6.186 SSUB16	// ssub16<c>  <Rd>,<Rn>,<Rm>
			// A8.6.187 SSUB8	// ssub8<c>   <Rd>,<Rn>,<Rm>
			// A8.6.233 UADD16	// uadd16<c>  <Rd>,<Rn>,<Rm>
			// A8.6.234 UADD8	// uadd8<c>   <Rd>,<Rn>,<Rm>
			// A8.6.235 UASX	// uasx<c>    <Rd>,<Rn>,<Rm>
			// A8.6.238 UHADD16	// uhadd16<c> <Rd>,<Rn>,<Rm>
			// A8.6.239 UHADD8	// uhadd8<c>  <Rd>,<Rn>,<Rm>
			// A8.6.240 UHASX	// uhasx<c>   <Rd>,<Rn>,<Rm>
			// A8.6.241 UHSAX	// uhsax<c>   <Rd>,<Rn>,<Rm>
			// A8.6.242 UHSUB16	// uhsub16<c> <Rd>,<Rn>,<Rm>
			// A8.6.243 UHSUB8	// uhsub8<c>  <Rd>,<Rn>,<Rm>
			// A8.6.247 UQADD16	// uqadd16<c> <Rd>,<Rn>,<Rm>
			// A8.6.248 UQADD8	// uqadd8<c>  <Rd>,<Rn>,<Rm>
			// A8.6.249 UQASX	// uqasx<c>   <Rd>,<Rn>,<Rm>
			// A8.6.250 UQSAX	// uqsax<c>   <Rd>,<Rn>,<Rm>
			// A8.6.251 UQSUB16	// uqsub16<c> <Rd>,<Rn>,<Rm>
			// A8.6.252 UQSUB8	// uqsub8<c>  <Rd>,<Rn>,<Rm>
			// A8.6.257 USAX	// usax<c>    <Rd>,<Rn>,<Rm>
			// A8.6.258 USUB16	// usub16<c>  <Rd>,<Rn>,<Rm>
			// A8.6.259 USUB8	// usub8<c>   <Rd>,<Rn>,<Rm>
			if (1 == (opcode >> 4 & 7))
				mnemonic = "";
			else
				mnemonic = isBitEnabled(opcode, 6) ? "u" : "s";
			if (isBitEnabled(opcode, 4))
				mnemonic += "q";
			else if (isBitEnabled (opcode, 5))
				mnemonic += "h";
			switch (opcode >> 20 & 7) {
				case 0:	mnemonic += "add8";		break;
				case 1: mnemonic += "add16";	break;
				case 2: mnemonic += "asx";		break;
				case 4:	mnemonic += "sub8";		break;
				case 5:	mnemonic += "sub16";	break;
				case 6:	mnemonic += "sax";		break;
			}
// no break!
		case thumb2_mul:		// A8.6.105 MUL		// mul<c> <Rd>,<Rn>,<Rm>
		case thumb2_sel:		// A8.6.156 SEL		// sel<c> <Rd>,<Rn>,<Rm>
		case thumb2_usad8:		// A8.6.253 USAD8	// usad8<c> <Rd>,<Rn>,<Rm>
			// . . . . . . . . . . . . Rn_1_3_0 . . . . Rd_0_11_8 . . . . Rm_0_3_0
			
			instruction = mnemonic + TAB + getR_8(opcode) + ',' + getR_16(opcode) + ',' + getR_0(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE
			break;

		case thumb2_sdiv:		// A8.6.155 SDIV	// sdiv<c> <Rd>,<Rn>,<Rm>
		case thumb2_udiv:		// A8.6.237 UDIV	// udiv<c> <Rd>,<Rn>,<Rm>
			// . . . . . . . . . . . . Rn_1_3_0 (1) (1)(1)(1) Rd_0_11_8 . . . . Rm_0_3_0
			if (ARMv7 > versionMode) {
				instruction = IDisassembler.INVALID_OPCODE;
				break;
			}

			instruction = mnemonic + TAB + getR_8(opcode) + ',' + getR_16(opcode) + ',' + getR_0(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE
			break;

		case thumb2_qadd:		// A8.6.124 QADD	// qadd<c> <Rd>,<Rm>,<Rn>
								// A8.6.128 QDADD	// qdadd<c> <Rd>,<Rm>,<Rn>
		case thumb2_qsub:		// A8.6.129 QDSUB	// qdsub<c> <Rd>,<Rm>,<Rn>
								// A8.6.131 QSUB	// qsub<c> <Rd>,<Rm>,<Rn>
			mnemonic = (isBitEnabled(opcode, 4) ? "qd" : "q") + mnemonic;
			instruction = mnemonic + TAB + getR_8(opcode) + ',' + getR_0(opcode) + ',' + getR_16(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE
			break;
			
		case thumb2_pkh:		// A8.6.116 PKH			// pkhbt<c> <Rd>,<Rn>,<Rm>{,lsl #<imm>}
														// pkhtb <Rd>,<Rn>,<Rm>{,asr #<imm>}
			// 1 1 1 0 1 0 1 0 1 1 0 0 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 tb_0_5_5 0 Rm_0_3_0
			mnemonic += isBitEnabled(opcode, 5) ? "tb\t" : "bt\t";
			instruction = mnemonic + getR_8(opcode) + ',' + getR_16(opcode) + ','
						  + getR_0(opcode) + getThumb2ShiftMode(opcode, 4);
			// No pc check: PC at Rd is UNPREDICTABLE
			break;

		case thumb2_pld:
		case thumb2_pli:
			if ((opcode >> 16 & 0xf) == 15) {	
				// A8.6.118 PLD (literal)				// pld<c> <label>	pld [pc,#-0] Special case
				// 1 1 1 1 1 0 0 0 U_1_7_7 0 (0) 1 1 1 1 1 1 1 1 1 imm12_0_11_0
				// A8.6.120 PLI (immediate, literal)	// pli<c> <label>	pli [pc,#-0] Special case
				// 1 1 1 1 1 0 0 1 U_1_7_7 0 0 1 1 1 1 1 1 1 1 1 imm12_0_11_0
				instruction = mnemonic + TAB + getAddrModePCImm(opcode, opcode & 0xfff);
			} else if (isBitEnabled(opcode, 23) || isBitEnabled(opcode, 11)){
				String imm;
				if (isBitEnabled(opcode, 23)) {
					// A8.6.117 PLD, PLDW (immediate)			// pld{w}<c> [<Rn>,#<imm12>]
					// 1 1 1 1 1 0 0 0 1 0 W_1_5_5 1 Rn_1_3_0 1 1 1 1 imm12_0_11_0
					// A8.6.120 PLI (immediate, literal)		// pli<c> [<Rn>,#<imm12>]
					// 1 1 1 1 1 0 0 1 1 0 0 1 Rn_1_3_0 1 1 1 1 imm12_0_11_0
					imm = getImmediate12(opcode);
				} else { 
					// A8.6.117 PLD, PLDW (immediate)			// pld{w}<c> [<Rn>,#-<imm8>]
					// 1 1 1 1 1 0 0 0 0 0 W_1_5_5 1 Rn_1_3_0 1 1 1 1 1 1 0 0 imm8_0_7_0
					// A8.6.120 PLI (immediate, literal)		// pli<c> [<Rn>,#-<imm8>]
					// 1 1 1 1 1 0 0 1 0 0 0 1 Rn_1_3_0 1 1 1 1 1 1 0 0 imm8_0_7_0
					imm = "#-" + getHexValue(opcode & 0xff);
				}
				instruction = mnemonic + (isBitEnabled(opcode, 21) ? "w\t[" : "\t[")
							  + getR_16(opcode) + ',' + imm + ']';
			} else {
				// A8.6.119 PLD, PLDW (register)			// pld{w}<c> [<Rn>,<Rm>{,lsl #<imm2>}]
				// 1 1 1 1 1 0 0 0 0 0 W_1_5_5 1 Rn_1_3_0 1 1 1 1 0 0 0 0 0 0 imm2_0_5_4 Rm_0_3_0
				// A8.6.121 PLI (register)					// pli<c> [<Rn>,<Rm>{,lsl #<imm2>}]
				// 1 1 1 1 1 0 0 1 0 0 0 1 Rn_1_3_0 1 1 1 1 0 0 0 0 0 0 imm2_0_5_4 Rm_0_3_0
				instruction = mnemonic + (isBitEnabled(opcode, 21) ? "w\t" : "\t")
							  + getThumb2AddrModeRegImm(opcode);
			}
			// No pc check: PC at Rn is handled above as literal
			break;

		case thumb2_pop__regs:	// A8.6.122 POP		// pop<c>.w <registers> <registers> contains more than one register
			// 1 1 1 0 1 0 0 0 1 0 1 1 1 1 0 1 P_0_15_15 M_0_14_14 (0) register_list_0_12_0
			checkPC = true;
// no break!
		case thumb2_push__regs:	// A8.6.123 PUSH	// push<c>.w <registers> <registers> contains more than one register
			// 1 1 1 0 1 0 0 0 1 0 1 0 1 1 0 1 (0) M_0_14_14 (0) register_list_0_12_0
			regDest = getRegList(opcode);
			instruction = mnemonic + TAB + regDest;
			if (checkPC && regDest.contains("pc"))
				setDefaultPCJumpProperties(true);
			break;

		case thumb2_pop__reg:	// A8.6.122 POP		// pop<c>.w <registers> <registers> contains one register, <Rt>
			checkPC = true;
// no break!
		case thumb2_push__reg:	// A8.6.123 PUSH	// push<c>.w <registers> <registers> contains one register, <Rt>
			regDest = getR_12(opcode);
			instruction = mnemonic + TAB + '{' + regDest + '}';
			if (regDest.equals("pc"))
				setDefaultPCJumpProperties(true);
			break;

		case thumb2_rfe:		// B6.1.8 RFE		// rfe{ia}<c> <Rn>{!} Outside or last in IT block
													// rfedb <Rn>{!} Outside or last in IT block
			// . . . . . . . . . . W_1_5_5 . Rn_1_3_0 (1) (1)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)
			instruction = mnemonic
						  + (isBitEnabled(opcode, 24) /* && isBitEnabled(opcode, 23) */ ? "ia\t" : "db\t")
						  + getR_16(opcode) + getW(opcode);
			setDefaultPCJumpProperties(true);
			break;

		case thumb2_smc:		// B6.1.9 SMC (previously SMI)		// smc #<imm4>
			// 1 1 1 1 0 1 1 1 1 1 1 1 imm4_1_3_0 1 0 0 0 (0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)
			instruction = mnemonic + "\t#" + getHexValue(opcode >> 16 & 0xf);
			break;

		case thumb2_smla:		// A8.6.166 SMLABB, SMLABT, SMLATB, SMLATT		// smla<x><y><c> <Rd>,<Rn>,<Rm>,<Ra>
			// 1 1 1 1 1 0 1 1 0 0 0 1 Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 0 0 N_0_5_5 M_0_4_4 Rm_0_3_0
			instruction = mnemonic + getBorT(opcode, 5) + getBorT(opcode, 4) + TAB + getR_8(opcode)
						  + ',' + getR_16(opcode) + ',' + getR_0(opcode) + ',' + getR_12(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE					 			 
			break;

		case thumb2_smlad:		// A8.6.167 SMLAD		// smlad{x}<c> <Rd>,<Rn>,<Rm>,<Ra>
		case thumb2_smlsd:		// A8.6.172 SMLSD		// smlsd{x}<c> <Rd>,<Rn>,<Rm>,<Ra>
			// . . . . . . . . . . . . Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 . . . M_0_4_4 Rm_0_3_0
			instruction = mnemonic + getX(opcode, 4) + TAB + getR_8(opcode)
						  + ',' + getR_16(opcode) + ',' + getR_0(opcode) + ',' + getR_12(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE					 			 
			break;

		case thumb2_smlal:		// A8.6.168 SMLAL		// smlal<c> <RdLo>,<RdHi>,<Rn>,<Rm>
								// A8.6.169 SMLALBB, SMLALBT, SMLALTB, SMLALTT
														// smlal<x><y><c> <RdLo>,<RdHi>,<Rn>,<Rm>
			// 1 1 1 1 1 0 1 1 1 1 0 0 Rn_1_3_0 RdLo_0_15_12 RdHi_0_11_8 0 0 0 0 Rm_0_3_0
			// 1 1 1 1 1 0 1 1 1 1 0 0 Rn_1_3_0 RdLo_0_15_12 RdHi_0_11_8 1 0 N_0_5_5 M_0_4_4 Rm_0_3_0
			if (isBitEnabled(opcode, 7))
				mnemonic += getBorT(opcode, 5) + getBorT(opcode, 4);
// no break!
		case thumb2_smull:		// A8.6.179 SMULL		// smull<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		case thumb2_umaal:		// A8.6.244 UMAAL		// umaal<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		case thumb2_umlal:		// A8.6.245 UMLAL		// umlal<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		case thumb2_umull:		// A8.6.246 UMULL		// umull<c> <RdLo>,<RdHi>,<Rn>,<Rm>
			// . . . . . . . . . . . . Rn_1_3_0 RdLo_0_15_12 RdHi_0_11_8 . . . . Rm_0_3_0
			instruction = mnemonic + TAB + getR_12(opcode) + ',' + getR_8(opcode)
						  + ',' + getR_16(opcode) + ',' + getR_0(opcode);
			// No pc check: PC at RdLo or RdHi or Rn or Rm is UNPREDICTABLE					 			 
			break;

		case thumb2_smlald:		// A8.6.170 SMLALD		// smlald{x}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		case thumb2_smlsld:		// A8.6.173 SMLSLD		// smlsld{x}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
			// . . . . . . . . . . . . Rn_1_3_0 RdLo_0_15_12 RdHi_0_11_8 . . . M_0_4_4 Rm_0_3_0
			instruction = mnemonic + getX(opcode, 4) + TAB + getR_12(opcode)
						  + ',' + getR_8(opcode) + ',' + getR_16(opcode) + ',' + getR_0(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE					 			 
			break;

		case thumb2_smlaw:		// A8.6.171 SMLAWB, SMLAWT	// smlaw<y><c> <Rd>,<Rn>,<Rm>,<Ra>
			// 1 1 1 1 1 0 1 1 0 0 1 1 Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 0 0 0 M_0_4_4 Rm_0_3_0
			instruction = mnemonic + getBorT(opcode, 4) + TAB + getR_8(opcode)
						  + ',' + getR_16(opcode) + ',' + getR_0(opcode) + ',' + getR_12(opcode);
			break;

		case thumb2_smmla:		// A8.6.174 SMMLA		// smmla{r}<c> <Rd>,<Rn>,<Rm>,<Ra>
		case thumb2_smmls:		// A8.6.175 SMMLS		// smmls{r}<c> <Rd>,<Rn>,<Rm>,<Ra>
			// . . . . . . . . . . . . Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 . . . R_0_4_4 Rm_0_3_0
			instruction = ',' + getR_12(opcode);
// no break!
		case thumb2_smmul:		// A8.6.176 SMMUL		// smmul{r}<c> <Rd>,<Rn>,<Rm>
			// 1 1 1 1 1 0 1 1 0 1 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 R_0_4_4 Rm_0_3_0
			instruction = mnemonic + getR(opcode, 4) + TAB + getR_8(opcode)
						  + ',' + getR_16(opcode) + ',' + getR_0(opcode)
						  + instruction;
			// No pc check: PC at Rd is UNPREDICTABLE					 			 
			break;

		case thumb2_smuad:		// A8.6.177 SMUAD		// smuad{x}<c> <Rd>,<Rn>,<Rm>
		case thumb2_smusd:		// A8.6.181 SMUSD		// smusd{x}<c> <Rd>,<Rn>,<Rm>
			// . . . . . . . . . . . . Rn_1_3_0 . . . . Rd_0_11_8 . . . M_0_4_4 Rm_0_3_0
			instruction = mnemonic + getX(opcode, 4) + TAB + getR_8(opcode)
			  			  + ',' + getR_16(opcode) + ',' + getR_0(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE					 			 
			break;

		case thumb2_smul:		// A8.6.178 SMULBB, SMULBT, SMULTB, SMULTT
														// smul<x><y><c> <Rd>,<Rn>,<Rm>
			// 1 1 1 1 1 0 1 1 0 0 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 N_0_5_5 M_0_4_4 Rm_0_3_0
			instruction = mnemonic + getBorT(opcode, 5) + getBorT(opcode, 4) + TAB + getR_8(opcode)
						  + ',' + getR_16(opcode) + ',' + getR_0(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE					 			 
			break;

		case thumb2_smulw:		// A8.6.180 SMULWB, SMULWT	// smulw<y><c> <Rd>,<Rn>,<Rm>
			// 1 1 1 1 1 0 1 1 0 0 1 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 M_0_4_4 Rm_0_3_0
			instruction = mnemonic + getBorT(opcode, 4) + TAB + getR_8(opcode)
						  + ',' + getR_16(opcode) + ',' + getR_0(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE					 			 
			break;

		case thumb2_srs:		// B6.1.10 SRS				// srsdb sp{!},#<mode>
			// 1 1 1 0 1 0 0 0 0 0 W_1_5_5 0 (1)(1)(0) (1) (1) (1)(0)(0)(0)(0)(0)(0)(0)(0)(0) mode_0_4_0
															// srs{ia} sp{!},#<mode>
			// 1 1 1 0 1 0 0 1 1 0 W_1_5_5 0 (1)(1)(0) (1) (1) (1)(0)(0)(0)(0)(0)(0)(0)(0)(0) mode_0_4_0
			// . . . . . . . . . . W_1_5_5 . (1)(1)(0) (1) (1) (1)(0)(0)(0)(0)(0)(0)(0)(0)(0) mode_0_4_0
			instruction = mnemonic
						  + (isBitEnabled(opcode, 24) /* && isBitEnabled(opcode, 23) */ ? "ia" : "db")
						  + "\tsp" + getW(opcode) + ",#" + getHexValue(opcode & 0x1f);
			break;

		case thumb2_ssat:		// A8.6.183 SSAT			// ssat<c> <Rd>,#<imm>,<Rn>{,<shift>}
		case thumb2_usat:		// A8.6.256 USAT16			// usat16<c> <Rd>,#<imm4>,<Rn>
			// 1 1 1 1 0 (0) 1 1 0 0 sh_1_5_5 0 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 (0) sat_imm_0_4_0
			offset = (opcode & 0x1f) + 1 - getBit(opcode, 23);
			instruction = mnemonic + TAB + getR_8(opcode) + ",#" + offset + ',' + getR_16(opcode)
						  + getThumb2ShiftMode(opcode, 20);
			// No pc check: PC at Rd is UNPREDICTABLE					 			 
			break;

		case thumb2_ssat16:		// A8.6.184 SSAT16			// ssat16<c> <Rd>,#<imm>,<Rn>
		case thumb2_usat16:		// A8.6.256 USAT16			// usat16<c> <Rd>,#<imm4>,<Rn>
			// . . . . . (0) . . . . . . Rn_1_3_0 . . . . Rd_0_11_8 . . (0)(0) sat_imm_0_3_0
			offset = (opcode & 0xf) + 1 - getBit(opcode, 23);
			instruction = mnemonic + TAB + getR_8(opcode) + ",#" + offset + ',' + getR_16(opcode);
			// No pc check: PC at Rd is UNPREDICTABLE					 			 
			break;

		case thumb2_stm:		// A8.6.189 STM / STMIA / STMEA	// stm<c>.w <Rn>{!},<registers>
		case thumb2_stmdb:		// A8.6.191 STMDB / STMFD		// stmdb<c> <Rn>{!},<registers>
			// . . . . . . . . . . W_1_5_5 . Rn_1_3_0 (0) M_0_14_14 (0) register_list_0_12_0
			instruction = mnemonic + TAB + getR_16(opcode) + getW(opcode)
						  + ',' + getRegList(opcode);
			// No pc check: PC is not eligible for writeback
			break;

		case thumb2_strex:		// A8.6.202 STREX		// strex<c> <Rd>,<Rt>,[<Rn>{,#<imm>}]
			// 1 1 1 0 1 0 0 0 0 1 0 0 Rn_1_3_0 Rt_0_15_12 Rd_0_11_8 imm8_0_7_0
			offset = opcode & 0xff;
			instruction = mnemonic + TAB + getR_8(opcode) + ',' + getR_12(opcode) + ",[" + getR_16(opcode);
			if (offset != 0)
				instruction += ",#" + getHexValue(offset << 2);
			instruction += ']';
			// No pc check: PC at Rd is UNPREDICTABLE					 			 
			break;

		case thumb2_strexx:		// A8.6.203 STREXB		// strexb<c> <Rd>,<Rt>,[<Rn>]
								// A8.6.205 STREXH		// strexh<c> <Rd>,<Rt>,[<Rn>]
								// A8.6.204 STREXD		// strexd<c> <Rd>,<Rt>,<Rt2>,[<Rn>]
			// . . . . . . . . . . . . Rn_1_3_0 Rt_0_15_12 (1)(1)(1)(1) . . . . Rd_0_3_0
			if (isBitEnabled(opcode, 5)) {
				mnemonic += 'd';
				instruction = "," + getR_8(opcode);
			} else {
				mnemonic += isBitEnabled(opcode, 4) ? 'h' : 'b';
			}
			instruction = mnemonic + TAB + getR_0(opcode) + ',' + getR_12(opcode)
						  + instruction + ",[" + getR_16(opcode) + ']';
			// No pc check: PC at Rt is UNPREDICTABLE
			break;

		case thumb2_subs:		// B6.1.13 SUBS PC, LR and related instructions
										// subs pc,lr,#<imm8> Outside or last in IT block
			// 1 1 1 1 0 0 1 1 1 1 0 1 (1)(1)(1) (0) 1 0 (0) 0 (1)(1)(1)(1) imm8_0_7_0
			instruction = mnemonic + "\tpc,lr,#" + getHexValue(opcode & 0xff);
			setDefaultPCJumpProperties(true);
			break;

		case thumb2_sxtab:		// A8.6.220 SXTAB		// sxtab<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		case thumb2_sxtab16:	// A8.6.221 SXTAB16		// sxtab16<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		case thumb2_sxtah:		// A8.6.222 SXTAH		// sxtah<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		case thumb2_uxtab:		// A8.6.260 UXTAB		// uxtab<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		case thumb2_uxtab16:	// A8.6.261 UXTAB16		// uxtab16<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		case thumb2_uxtah:		// A8.6.262 UXTAH		// uxtah<c> <Rd>,<Rn>,<Rm>{,<rotation>}
			// . . . . . . . . . . . . Rn_1_3_0 . . . . Rd_0_11_8 . (0) rotate_0_5_4 Rm_0_3_0
			instruction = mnemonic + TAB + getR_8(opcode) + ',' + getR_16(opcode)
						  + ',' + getR_0(opcode) + getRotationOperand(opcode, 4);
			// No pc check: PC at Rd is UNPREDICTABLE					 			 
			break;

		case thumb2_sxtb:		// A8.6.223 SXTB		// sxtb<c>.w <Rd>,<Rm>{,<rotation>}
		case thumb2_sxtb16:		// A8.6.224 SXTB16		// sxtb16<c> <Rd>,<Rm>{,<rotation>}
		case thumb2_sxth:		// A8.6.225 SXTH		// sxth<c>.w <Rd>,<Rm>{,<rotation>}
		case thumb2_uxtb:		// A8.6.263 UXTB		// uxtb<c>.w <Rd>,<Rm>{,<rotation>}
		case thumb2_uxtb16:		// A8.6.264 UXTB16		// uxtb16<c> <Rd>,<Rm>{,<rotation>}
		case thumb2_uxth:		// A8.6.265 UXTH		// uxth<c>.w <Rd>,<Rm>{,<rotation>}
			// . . . . . . . . . . . . . . . . . . . . Rd_0_11_8 . (0) rotate_0_5_4 Rm_0_3_0
			instruction = mnemonic + TAB + getR_8(opcode) + ',' + getR_0(opcode)
						  + getRotationOperand(opcode, 4);
			// No pc check: PC at Rd is UNPREDICTABLE					 			 
			break;

		case thumb2_tb:			// A8.6.226 TBB, TBH	// tbb<c> [<Rn>,<Rm>] Outside or last in IT block
														// tbh [<Rn>,<Rm>,LSL #1] Outside or last in IT block
			// 1 1 1 0 1 0 0 0 1 1 0 1 Rn_1_3_0 (1) (1)(1)(1)(0)(0)(0)(0) 0 0 0 H_0_4_4 Rm_0_3_0
			mnemonic += isBitEnabled(opcode, 4) ? 'h' : 'b';
			instruction = mnemonic + TAB + '[' + getR_16(opcode) + ',' + getR_0(opcode)
						  + (isBitEnabled(opcode, 4) ? ",lsl #1" : "") + ']';
			setDefaultPCJumpProperties(false);
			break;




			

			// VFP instructions

		case thumb2_vhadd_vhsub:
			// vhadd<c> <Qd>, <Qn>, <Qm>
			// vhadd<c> <Dd>, <Dn>, <Dm>
			// vhsub<c> <Qd>, <Qn>, <Qm>
			// vhsub<c> <Dd>, <Dn>, <Dm>
			mnemonic += isBitEnabled(opcode, 9) ? "sub" : "add";
		case thumb2_vaba:
			// vaba<c>.<dt> <Qd>, <Qn>, <Qm>
			// vaba<c>.<dt> <Dd>, <Dn>, <Dm>
		case thumb2_vabd__int:
			// vabd<c>.<dt> <Qd>, <Qn>, <Qm>
			// vabd<c>.<dt> <Dd>, <Dn>, <Dm>
		case thumb2_vcge__reg_int:
			// vceq<c>.<dt> <Qd>, <Qn>, <Qm>
			// vceq<c>.<dt> <Dd>, <Dn>, <Dm>
		case thumb2_vcgt__reg_int:
			// vcgt<c>.<dt> <Qd>, <Qn>, <Qm>
			// vcgt<c>.<dt> <Dd>, <Dn>, <Dm>
		case thumb2_vqadd:
			// vqadd<c>.<dt> <Qd>,<Qn>,<Qm>
			// vqadd<c>.<dt> <Dd>,<Dn>,<Dm>
		case thumb2_vrhadd:
			// vrhadd<c> <Qd>, <Qn>, <Qm>
			// vrhadd<c> <Dd>, <Dn>, <Dm>
		case thumb2_vqsub:		// A8.6.369 VQSUB
			// vqsub<c>.<type><size>  <Qd>,<Qn>,<Qm>
			// vqsub<c>.<type><size>  <Dd>,<Dn>,<Dm>
			instruction = mnemonic + getVFPSorUDataType(opcode, 28) + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vqdml__vec:
			// vqd<op><c>.<dt> <Qd>,<Dn>,<Dm>
			mnemonic += isBitEnabled(opcode, 9) ? "sl" : "al";
			// can use getVFPSorUDataType() because bit 24 is always '0'
// no break!
		case thumb2_vabal:
			// vabal<c>.<dt> <Qd>, <Dn>, <Dm>
		case thumb2_vabdl:
			// vabdl<c>.<dt> <Qd>, <Dn>, <Dm>
		case thumb2_vqdmull__vec:
			// vqdmull<c>.<dt> <Qd>,<Dn>,<Dm>
			instruction = mnemonic + getVFPSorUDataType(opcode, 28)
						  + TAB + getVFPQdDnDmRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vabd__f32:
			// vabd<c>.f32 <Qd>, <Qn>, <Qm>
			// vabd<c>.f32 <Dd>, <Dn>, <Dm>
		case thumb2_vadd__f32:
			// vadd<c>.f32 <Qd>, <Qn>, <Qm>
			// vadd<c>.f32 <Dd>, <Dn>, <Dm>
		case thumb2_vceq__reg_f32:
			// vceq<c>.f32 <Qd>, <Qn>, <Qm>
			// vceq<c>.f32 <Dd>, <Dn>, <Dm>
		case thumb2_vcge__reg_f32:
			// vcge<c>.f32 <Qd>, <Qn>, <Qm>
			// vcge<c>.f32 <Dd>, <Dn>, <Dm>
		case thumb2_vcgt__reg_f32:
			// vcgt<c>.f32 <Qd>, <Qn>, <Qm>
			// vcgt<c>.f32 <Dd>, <Dn>, <Dm>
		case thumb2_vmul__f32:
			// vmul<c>.f32 <Qd>, <Qn>, <Qm>
			// vmul<c>.f32 <Dd>, <Dn>, <Dm>
		case thumb2_vpadd__f32:
			// vpadd<c>.f32 <Dd>, <Dn>, <Dm>
		case thumb2_vrecps:
			// vrecps<c>.f32 <Qd>, <Qn>, <Qm>
			// vrecps<c>.f32 <Dd>, <Dn>, <Dm>
		case thumb2_vrsqrts:
			// vrsqrts<c>.f32 <Qd>, <Qn>, <Qm>
			// vrsqrts<c>.f32 <Dd>, <Dn>, <Dm>
		case thumb2_vsub__f32:
			// vsub<c>.f32 <Qd>, <Qn>, <Qm>
			// vsub<c>.f32 <Dd>, <Dn>, <Dm>
			instruction = mnemonic + ".f32\t" + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vabs:
			// vabs<c>.<dt> <Qd>, <Qm>
			// vabs<c>.<dt> <Dd>, <Dm>
		case thumb2_vneg:
			// vneg<c>.<dt> <Qd>, <Qm>
			// vneg<c>.<dt> <Dd>, <Dm>
			instruction = mnemonic + getVFPSorUorFQorDdmOperands(opcode, 10, 4); // chose bit 11 because it is 0
			// No pc check: not applicable
			break;

		case thumb2_vabs__f:
			// vabs<c>.f64 <Dd>, <Dm>
			// vabs<c>.f32 <Sd>, <Sm>
		case thumb2_vmov__reg_f:
			// vmov<c>.f64 <Dd>, <Dm>
			// vmov<c>.f32 <Sd>, <Sm>
		case thumb2_vneg__f:
			// vneg<c>.f64 <Dd>, <Dm>
			// vneg<c>.f32 <Sd>, <Sm>
		case thumb2_vsqrt:
			// vsqrt<c>.f64 <Dd>, <Dm>
			// vsqrt<c>.f32 <Sd>, <Sm>
			instruction = mnemonic + getVFPSzF64F32dmOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vacge_vacgt:
			// vacge<c>.f32 <Qd>, <Qn>, <Qm>
			// vacge<c>.f32 <Dd>, <Dn>, <Dm>
			// vacgt<c>.f32 <Qd>, <Qn>, <Qm>
			// vacgt<c>.f32 <Dd>, <Dn>, <Dm>
			instruction = mnemonic + getVFP_vacge_vacgt(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vadd__int:
			// vadd<c>.<dt> <Qd>, <Qn>, <Qm>
			// vadd<c>.<dt> <Dd>, <Dn>, <Dm>
		case thumb2_vceq__reg_int:
			// vceq<c>.<dt> <Qd>, <Qn>, <Qm>
			// vceq<c>.<dt> <Dd>, <Dn>, <Dm>
		case thumb2_vpadd__int:
			// vpadd<c>.<dt> <Dd>, <Dn>, <Dm>
		case thumb2_vsub__int:
			// vsub<c>.<dt> <Qd>, <Qn>, <Qm>
			// vsub<c>.<dt> <Dd>, <Dn>, <Dm>
			instruction = mnemonic + getVFPIDataTypeQorDdnmOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vnml:
			// vnmla<c>.f64 <Dd>, <Dn>, <Dm>
			// vnmla<c>.f32 <Sd>, <Sn>, <Sm>
			// vnmls<c>.f64 <Dd>, <Dn>, <Dm>
			// vnmls<c>.f32 <Sd>, <Sn>, <Sm>
			mnemonic += isBitEnabled(opcode, 6) ? 'a' : 's';
// no break!
		case thumb2_vadd__fp_f:
			// vadd<c>.f64 <Dd>, <Dn>, <Dm>
			// vadd<c>.f32 <Sd>, <Sn>, <Sm>
		case thumb2_vdiv:
			// vdiv<c>.f64 <Dd>, <Dn>, <Dm>
			// vdiv<c>.f32 <Sd>, <Sn>, <Sm>
		case thumb2_vmul__fp_2:
			// vmul<c>.f64 <Dd>, <Dn>, <Dm>
			// vmul<c>.f32 <Sd>, <Sn>, <Sm>
		case thumb2_vnmul:
			// vnmul<c>.f64 <Dd>, <Dn>, <Dm>
			// vnmul<c>.f32 <Sd>, <Sn>, <Sm>
		case thumb2_vsub__fp_f:
			// vsub<c>.f64 <Dd>, <Dn>, <Dm>
			// vsub<c>.f32 <Sd>, <Sn>, <Sm>
			instruction = mnemonic + getVFPSzF64F32dnmOperands(opcode);
			break;

		case thumb2_vaddhn:
			// vaddhn<c>.<dt> <Dd>, <Qn>, <Qm>
		case thumb2_vraddhn:
			// vraddhn<c>.<dt> <Dd>, <Qn>, <Qm>
		case thumb2_vrsubhn:
			// vrsubhn<c>.<dt> <Dd>, <Qn>, <Qm>
		case thumb2_vsubhn:
			// vsubhn<c>.<dt> <Dd>, <Qn>, <Qm>
			instruction = mnemonic + getVFPIDataType2DdQnDmOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vaddl_vaddw:
			// vaddl<c>.<dt> <Qd>, <Dn>, <Dm>
			// vaddw<c>.<dt> <Qd>, <Qn>, <Dm>
		case thumb2_vsubl_vsubw:
			// vsubl<c>.<dt> <Qd>, <Dn>, <Dm>
			// vsubw<c>.<dt> {<Qd>,} <Qn>, <Dm>
			instruction = mnemonic + getVFP_vXXXl_vXXXw(opcode, 28);
			break;

		case thumb2_vbif_vbit_vbsl_veor:
			// vbif<c> <Qd>, <Qn>, <Qm>
			// vbif<c> <Dd>, <Dn>, <Dm>
			// vbit<c> <Qd>, <Qn>, <Qm>
			// vbit<c> <Dd>, <Dn>, <Dm>
			// vbsl<c> <Qd>, <Qn>, <Qm>
			// vbsl<c> <Dd>, <Dn>, <Dm>
			// veor<c> <Qd>, <Qn>, <Qm>
			// veor<c> <Dd>, <Dn>, <Dm>
			mnemonic = getVFP_vbif_vbit_vbsl_veor_mnemonic(opcode);
// no break!
		case thumb2_vand:
			// vand<c> <Qd>, <Qn>, <Qm>
			// vand<c> <Dd>, <Dn>, <Dm>
		case thumb2_vbic__reg:
			// vbic<c> <Qd>, <Qn>, <Qm>
			// vbic<c> <Dd>, <Dn>, <Dm>
		case thumb2_vorn:
			// vorn<c> <Qd>, <Qn>, <Qm>
			// vorn<c> <Dd>, <Dn>, <Dm>
		case thumb2_vorr__reg:
			// vorr<c> <Qd>, <Qn>, <Qm>
			// vorr<c> <Dd>, <Dn>, <Dm>
			instruction = mnemonic + getVFPQorDdnmRegs(opcode);
			break;

		case thumb2_vmov_vbitwise:
			// vbic<c>.<dt> <Qd>, #<imm>
			// vbic<c>.<dt> <Dd>, #<imm>
			// vmov<c>.<dt> <Qd>, #<imm>
			// vmov<c>.<dt> <Dd>, #<imm>
			// vmvn<c>.<dt> <Qd>, #<imm>
			// vmvn<c>.<dt> <Dd>, #<imm>
			// vorr<c>.<dt> <Qd>, #<imm>
			// vorr<c>.<dt> <Dd>, #<imm>
			instruction = getVFP_vmov_vbitwise_instruction(opcode, 28);
			break;

		case thumb2_vceq__imm0:	// A8.6.281 VCEQ (immediate #0)
			// vceq<c>.<dt> <Qd>, <Qm>, #0
			// vceq<c>.<dt> <Dd>, <Dm>, #0
			// vceq<c>.<dt> <Qd>, <Qm>, #0
			// vceq<c>.<dt> <Dd>, <Dm>, #0
			instruction = mnemonic + getVFPIorFQorDdmOperands(opcode, 10) + ",#0";
			// No pc check: not applicable
			break;

		case thumb2_vcge__imm0:
			// vcge<c>.<dt> <Qd>, <Qm>, #0
			// vcge<c>.<dt> <Dd>, <Dm>, #0
		case thumb2_vcgt__imm0:
			// vcgt<c>.<dt> <Qd>, <Qm>, #0
			// vcgt<c>.<dt> <Dd>, <Dm>, #0
		case thumb2_vcle:
			// vcle<c>.<dt> <Qd>, <Qm>, #0
			// vcle<c>.<dt> <Dd>, <Dm>, #0
		case thumb2_vclt:
			// vclt<c>.<dt> <Qd>, <Qm>, #0
			// vclt<c>.<dt> <Dd>, <Dm>, #0
			instruction = mnemonic + getVFPSorUorFQorDdmOperands(opcode, 10, 11) + ",#0"; // chose bit 11 because it is 0
			// No pc check: not applicable
			break;

		case thumb2_vcls:
			// vcls<c>.<dt> <Qd>, <Qm>
			// vcls<c>.<dt> <Dd>, <Dm>
		case thumb2_vqabs:
			// vqabs<c>.<dt> <Qd>,<Qm>
			// vqabs<c>.<dt> <Dd>,<Dm>
		case thumb2_vqneg:
			// vqneg<c>.<dt> <Qd>,<Qm>
			// vqneg<c>.<dt> <Dd>,<Dm>
			instruction = mnemonic + getVFPSorUorFQorDdmOperands(opcode, 4, 11); // chose bit 11 because it is 0
			// No pc check: not applicable
			break;

		case thumb2_vclz:
			// vclz<c>.<dt> <Qd>, <Qm>
			// vclz<c>.<dt> <Dd>, <Dm>
			instruction =  mnemonic + getVFPIorFQorDdmOperands(opcode, 11); // chose bit 11 because it is 0 
			// No pc check: not applicable
			break;

		case thumb2_vcmp__reg:
			// vcmp{e}<c>.f64 <Dd>, <Dm>
			// vcmp{e}<c>.f32 <Sd>, <Sm>
			instruction = mnemonic + getE(opcode) + getVFPSzF64F32dmOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vcmp__to_0:
			// vcmp{e}<c>.f64 <Dd>, #0.0
			// vcmp{e}<c>.f32 <Sd>, #0.0
			instruction = mnemonic + getE(opcode) + getVFP_vcmpTo0Operands(opcode);
			break;

		case thumb2_vcnt:
			// vcnt<c>.8 <Qd>, <Qm>
			// vcnt<c>.8 <Dd>, <Dm>
			mnemonic += ".8";
		case thumb2_vmvn:
			// vmvn<c> <Qd>, <Qm>
			// vmvn<c> <Dd>, <Dm>
		case thumb2_vmov__reg:
			// vmov<c> <Qd>, <Qm>	vmov<c> <Dd>, <Dm>
		case thumb2_vswp:
			// vswp<c> <Qd>, <Qm>
			// vswp<c> <Dd>, <Dm>
			instruction = mnemonic + getVFPQorDdmRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vcvt__fp_i_vec:
			// vcvt<c>.<Td>.<Tm> <Qd>, <Qm>
			// vcvt<c>.<Td>.<Tm> <Dd>, <Dm>
			instruction = mnemonic + getVFP_vcvtFpIVecOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vcvt__fp_i_reg:
			// vcvt{r}<c>.s32.f64 <Sd>, <Dm>
			// vcvt{r}<c>.s32.f32 <Sd>, <Sm>
			// vcvt{r}<c>.u32.f64 <Sd>, <Dm>
			// vcvt{r}<c>.u32.f32 <Sd>, <Sm>
			// vcvt<c>.f64.<Tm> <Dd>, <Sm>
			// vcvt<c>.f32.<Tm> <Sd>, <Sm>
			if (isBitEnabled(opcode, 18) && !isBitEnabled(opcode, 7))
				mnemonic += "r";
			instruction = mnemonic + getVFP_vcvtFpIRegOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vcvt__fp_fix_vec:
			// vcvt<c>.<Td>.<Tm> <Qd>, <Qm>, #<fbits>
			// vcvt<c>.<Td>.<Tm> <Dd>, <Dm>, #<fbits>
			instruction = mnemonic + getVFP_vcvtFpFixVecOperands(opcode, 28);
			// No pc check: not applicable
			break;

		case thumb2_vcvt__fp_fix_reg:
			// vcvt<c>.<Td>.f64 <Dd>, <Dd>, #<fbits>
			// vcvt<c>.<Td>.f32 <Sd>, <Sd>, #<fbits>
			// vcvt<c>.f64.<Td> <Dd>, <Dd>, #<fbits>
			// vcvt<c>.f32.<Td> <Sd>, <Sd>, #<fbits>
			instruction = mnemonic + getVFP_vcvtFpFixRegOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vcvt__dp_sp:
			// vcvt<c>.f64.f32 <Dd>, <Sm>
			// vcvt<c>.f32.f64 <Sd>, <Dm>
			instruction = mnemonic + getVFP_vcvtDpSpOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vcvt__hp_sp_vec:
			// vcvt<c>.f32.f16 <Qd>, <Dm>
			// vcvt<c>.f16.f32 <Dd>, <Qm>
			instruction = mnemonic + getVFP_vcvtHpSpVecOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vcvt__hp_sp_reg:
			// vcvt<y><c>.f32.f16 <Sd>, <Sm>
			// vcvt<y><c>.f16.f32 <Sd>, <Sm>
			mnemonic += (isBitEnabled(opcode, 7) ? "t" : "b");
			instruction = mnemonic + getVFP_vcvtHpSpRegOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vdup__scalar:
			// vdup<c>.<size> <Qd>, <Dm[x]>
			// vdup<c>.<size> <Dd>, <Dm[x]>
			instruction = mnemonic + getVFP_vdupScalarOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vdup__reg:
			// vdup<c>.<size> <Qd>, <Rt>
			// vdup<c>.<size> <Dd>, <Rt>
			instruction = mnemonic + getVFP_vdupRegOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vext:
			// vext<c>.8 <Qd>, <Qn>, <Qm>, #<imm>
			// vext<c>.8 <Dd>, <Dn>, <Dm>, #<imm>
			instruction = mnemonic + getVFPQorDdnmRegs(opcode)
						  + ",#" + (opcode >> 8 & 0xf);
			// No pc check: not applicable
			break;

		case thumb2_vld__multi:
			// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}
			// vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
			// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}
			// vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
			// vld3<c>.<size> <list>, [<Rn>{@<align>}]{!}
			// vld3<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
			// vld4<c>.<size> <list>, [<Rn>{@<align>}]{!}
			// vld4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		case thumb2_vst__multi:
			// vst1<c>.<size> <list>, [<Rn>{@<align>}]{!}
			// vst1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
			// vst2<c>.<size> <list>, [<Rn>{@<align>}]{!}
			// vst2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
			// vst3<c>.<size> <list>, [<Rn>{@<align>}]{!}
			// vst3<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
			// vst4<c>.<size> <list>, [<Rn>{@<align>}]{!}
			// vst4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
			instruction = mnemonic + getVFP_vXX_multi(opcode);
			break;

		case thumb2_vld__xlane:
			// vld1<c>.<size> <list>, [<Rn>{@<align>}}]{!}
			// vld1<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
			// vld1<c>.<size> <list>, [<Rn>{@<align>}}]{!}
			// vld1<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
			// vld2<c>.<size> <list>, [<Rn>{@<align>}}]{!}
			// vld2<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
			// vld2<c>.<size> <list>, [<Rn>{@<align>}}]{!}
			// vld2<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
			// vld3<c>.<size> <list>, [<Rn>{@<align>}}]{!}
			// vld3<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
			// vld3<c>.<size> <list>, [<Rn>{@<align>}}]{!}
			// vld3<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
			// vld4<c>.<size> <list>, [<Rn>{@<align>}}]{!}
			// vld4<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
			// vld4<c>.<size> <list>, [<Rn>{@<align>}}]{!}
			// vld4<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
		case thumb2_vst__xlane:
			// vst1<c>.<size> <list>, [<Rn>{@<align>}}]{!}
			// vst1<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
			// vst2<c>.<size> <list>, [<Rn>{@<align>}}]{!}
			// vst2<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
			// vst3<c>.<size> <list>, [<Rn>{@<align>}}]{!}
			// vst3<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
			// vst4<c>.<size> <list>, [<Rn>{@<align>}}]{!}
			// vst4<c>.<size> <list>, [<Rn>{@<align>}}], <Rm>
			instruction = mnemonic + getVFP_vXX_Xlane(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vldm__64:
			// vldm{mode}<c> <Rn>{!},<list>	(<list> is consecutive 64-bit registers)
		case thumb2_vldm__32:
			// vldm{mode}<c> <Rn>{!},<list>	(<list> is consecutive 64-bit registers)
		case thumb2_vstm__64:
			// vstm{mode}<c> <Rn>{!},<list>	(<list> is consecutive 64-bit registers)
		case thumb2_vstm__32:
			// vstm{mode}<c> <Rn>{!},<list>	(<list> is consecutive 64-bit registers)
			instruction = mnemonic + getVFPIncDec(opcode) + getVFP_vXXm(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vldr__64:
			// vldr<c> <Dd>, [<Rn>{,#+/-<imm>}]
			// vldr<c> <Dd>, <label>
			// vldr<c> <Dd>, [pc,#-0] Special case
		case thumb2_vldr__32:
			// vldr<c> <Sd>, [<Rn>{,#+/-<imm>}]
			// vldr<c> <Sd>, <label>
			// vldr<c> <Sd>, [pc,#-0] Special case
		case thumb2_vstr__64:
			// vstr<c> <Dd>, [<Rn>{,#+/-<imm>}]
		case thumb2_vstr__32:
			// vstr<c> <Sd>, [<Rn>{,#+/-<imm>}]
			instruction = mnemonic + getVFP_vXXr(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vmax_vmin__int:
			// A8.6.321 VMAX, VMIN (integer)
			// vmax<c>.<dt> <Qd>,<Qn>,<Qm>	vmax<c>.<dt> <Dd>,<Dn>,<Dm>	vmin<c>.<dt> <Qd>,<Qn>,<Qm>	vmin<c>.<dt> <Dd>,<Dn>,<Dm>
		case thumb2_vpmax_vpmin__int:
			// A8.6.352 VPMAX, VPMIN (integer)
			// vp<op><c>.<dt> <Dd>, <Dn>, <Dm>
			// (this works despite no Q version because Q==1 is UNDEFEIND)
			// . . . U_1_12_12 . . . . . D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 . . . . N_0_7_7 Q_0_6_6 M_0_5_5 op_0_4_4 Vm_0_3_0
			instruction = mnemonic + (isBitEnabled(opcode, 4) ? "min" : "max")
						  + getVFPSorUDataType(opcode, 28) + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vmax_vmin__fp:
			// A8.6.322 VMAX, VMIN (floating-point)
			// vmax<c>.f32 <Qd>,<Qn>,<Qm>	vmax<c>.f32 <Dd>,<Dn>,<Dm>	vmin<c>.f32 <Qd>,<Qn>,<Qm>	vmin<c>.f32 <Dd>,<Dn>,<Dm>
		case thumb2_vpmax_vpmin__fp:
			// A8.6.353 VPMAX, VPMIN (floating-point)
			// vp<op><c>.f32 <Dd>,<Dn>,<Dm>
			// . . . . . . . . . D_1_6_6 op_1_5_5 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 . . . . N_0_7_7 Q_0_6_6 M_0_5_5 . Vm_0_3_0
			instruction = mnemonic + (isBitEnabled(opcode, 21) ? "min.f32" : "max.f32")
						  + getVFPQorDdnmRegs(opcode);
			break;

		case thumb2_vml__int:
			// A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
			// v<op><c>.<dt> <Qd>,<Qn>,<Qm>	v<op><c>.<dt> <Dd>,<Dn>,<Dm>
			// 1 1 1 op_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
			mnemonic += isBitEnabled(opcode, 28) ? 's' : 'a';
			instruction = mnemonic + getVFPIDataType(opcode, 20) + TAB + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vml__int_long:
			// A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
			// v<op>l<c>.<dt> <Qd>,<Dn>,<Dm>
			// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 op_0_9_9 0 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
			mnemonic += isBitEnabled(opcode, 9) ? "sl" : "al";
			instruction = mnemonic + getVFPSorUDataType(opcode, 28)
						  + TAB + getVFPQdDnDmRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vml__f32:
			// A8.6.324 VMLA, VMLS (floating-point)
			// v<op><c>.f32 <Qd>,<Qn>,<Qm>	v<op><c>.f32 <Dd>,<Dn>,<Dm>
			mnemonic += isBitEnabled(opcode, 21) ? "s.f32" : "a.f32";
			instruction = mnemonic + TAB + getVFPQorDdnmRegs(opcode);
			break;

		case thumb2_vml__fp:
			// A8.6.324 VMLA, VMLS (floating-point)
			// v<op><c>.f64 <Dd>,<Dn>,<Dm>	v<op><c>.f32 <Sd>,<Sn>,<Sm>
			// 1 1 1 0 1 1 1 0 0 D_1_6_6 0 0 Vn_1_3_0 Vd_0_15_12 1 0 1 sz_0_8_8 N_0_7_7 op_0_6_6 M_0_5_5 0 Vm_0_3_0
			mnemonic += isBitEnabled(opcode, 6) ? 's' : 'a';
			instruction = mnemonic + getVFPSzF64F32dnmOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vml__scalar:
			// v<op><c>.<dt> <Qd>,<Qn>,<Dm[x]>	v<op><c>.<dt> <Dd>,<Dn>,<Dm[x]>
			// v<op>l<c>.<dt> <Qd>,<Dn>,<Dm[x]>
			mnemonic += isBitEnabled(opcode, 10) ? 's' : 'a';
		case thumb2_vmul__scalar:
			// vmul<c>.<dt>  <Qd>,<Qn>,<Dm[x]>	vmul<c>.<dt>  <Dd>,<Dn>,<Dm[x]>
			// vmull<c>.<dt>  <Qd>,<Dn>,<Dm[x]>
		case thumb2_vqdmull__scalar:	// bit9 == 1, so getVFP_vmXXScalar() works
			// vqdmull<c>.<dt> <Qd>,<Dn>,<Dm[x]>
			instruction = mnemonic + getVFP_vmXXScalar(opcode, 28);
			// No pc check: not applicable
			break;

		case thumb2_vmov__imm:
			// A8.6.326 VMOV (immediate)
			// vmov<c>.f64 <Dd>,#<imm>	vmov<c>.f32 <Sd>,#<imm>
			// 1 1 1 0 1 1 1 0 1 D_1_6_6 1 1 imm4H_1_3_0 Vd_0_15_12 1 0 1 sz_0_8_8 (0) 0 (0) 0 imm4L_0_3_0
			mnemonic += getVFPSzF64F32Type(getBit(opcode, 8));
			instruction = mnemonic + TAB + getVFPDorSReg(opcode, getBit(opcode, 8), 12, 22)
						  + ",#" + getHexValue((opcode >> 16 & 0xf) << 4 | opcode & 0xf);
			// No pc check: not applicable
			break;

		case thumb2_vmov_5:
			// vmov<c>.<size> <Dd[x]>,<Rt>
			instruction = mnemonic + getVFP_vmovArmCoreRegToScalar(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vmov_6:
			// vmov<c>.<dt> <Rt>,<Dn[x]>
			instruction = mnemonic + getVFP_vmovScalarToArmCoreReg(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vmov_7:
			// vmov<c> <Sn>,<Rt>	vmov<c> <Rt>,<Sn>
			instruction = mnemonic + getVFP_vmovBetweenArmCoreAndSinglePrecReg(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vmov_8:
			// vmov<c> <Sm>,<Sm1>,<Rt>,<Rt2>	vmov<c> <Rt>,<Rt2>,<Sm>,<Sm1>
			instruction = mnemonic + getVFP_vmovBetween2ArmCoreAndSinglePrecRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vmov_9:
			// vmov<c> <Dm>,<Rt>,<Rt2>	vmov<c> <Rt>,<Rt2>,<Dm>
			instruction = mnemonic + getVFP_vmovBetween2ArmCoreAnd1DoublewordExtensionRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vmovl:
			// vmovl<c>.<dt> <Qd>, <Dm>
		case thumb2_vshll__various:
			// vshll<c>.<type><size> <Qd>,<Dm>,#<imm> (0 < <imm> < <size>)
			instruction = mnemonic + getVFP_vmovl_vshll_operands(opcode, 28);
			// No pc check: not applicable
			break;

		case thumb2_vmovn:
			// A8.6.334 VMOVN
			// vmovn<c>.<dt> <Dd>,<Qm>
			// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 0 Vd_0_15_12 0 0 1 0 0 0 M_0_5_5 0 Vm_0_3_0
			mnemonic += getVFPIDataType2(opcode, 18);
			instruction = mnemonic + TAB + getVFPQorDReg(opcode, 0, 12, 22)
						  + ',' + getVFPQorDReg(opcode, 1, 0, 5);
			// No pc check: not applicable
			break;

		case thumb2_vmrs:
			// A8.6.335 VMRS
			// vmrs<c> <Rt>,fpscr
			// 1 1 1 0 1 1 1 0 1 1 1 1 0 0 0 1 Rt_0_15_12 1 0 1 0 0 (0)(0) 1 (0)(0)(0)(0)
			instruction = mnemonic + TAB + getR_12(opcode) + "," + getVFPSpecialReg(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vmsr:
			// A8.6.336 VMSR
			// vmsr<c> fpscr,<Rt>
			// 1 1 1 0 1 1 1 0 1 1 1 0 0 0 0 1 Rt_0_15_12 1 0 1 0 0 (0)(0) 1 (0)(0)(0)(0)
			instruction = mnemonic + TAB + getVFPSpecialReg(opcode) + "," + getR_12(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vmul_1:
			// A8.6.337 VMUL, VMULL (integer and polynomial)
			// vmul<c>.<dt> <Qd>,<Qn>,<Qm>	vmul<c>.<dt> <Dd>,<Dn>,<Dm>
			// 1 1 1 op_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
			mnemonic += (isBitEnabled(opcode, 28) ? ".p" : ".i") + getVFPDataTypeSize(opcode, 20);
			instruction = mnemonic + TAB + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vmull:
			// A8.6.337 VMUL, VMULL (integer and polynomial)
			// vmull<c>.<dt> <Qd>, <Dn>, <Dm>
			// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 1 op_9_9 0 N_7_7 0 M_5_5 0 Vm_3_0
			mnemonic += isBitEnabled(opcode, 9) ? getVFPPDataType(opcode, 20) : getVFPSorUDataType(opcode, 28);
			instruction = mnemonic + TAB + getVFPQdDnDmRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vpadal:
			// vpadal<c>.<dt> <Qd>, <Qm>
			// vpadal<c>.<dt> <Dd>, <Dm>
		case thumb2_vpaddl:
			// vpaddl<c>.<dt> <Qd>, <Qm>
			// vpaddl<c>.<dt> <Dd>, <Dm>
			instruction = mnemonic + getVFPSorUorFQorDdmOperands(opcode, 4, 7); // chose bit 4 because it is 0
			// No pc check: not applicable
			break;

		case thumb2_vpop:
			// vpop <list> <list> is consecutive 64-bit registers
			// vpop <list> <list> is consecutive 32-bit registers
		case thumb2_vpush:
			// vpush <list> <list> is consecutive 64-bit registers
			// vpush <list> <list> is consecutive 32-bit registers
			instruction = mnemonic + getVFP_vpop_vpush_operands(opcode);
			break;

		case thumb2_vqdml__scalar:
			// A8.6.358 VQDMLAL, VQDMLSL
			// vqd<op><c>.<dt> <Qd>,<Dn>,<Dm[x]>
			// 1 1 1 0 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 op_0_10_10 1 1 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
			mnemonic += isBitEnabled(opcode, 10) ? "sl.s" : "al.s";
			instruction = mnemonic + getVFPScalarOperands(opcode, 1, 0);
			break;

		case thumb2_vqdmulh__vec:
			// vqdmulh<c>.<dt> <Qd>,<Qn>,<Qm>	vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm>
		case thumb2_vqrdmulh__vec:
			// vqrdmulh<c>.<dt> <Qd>,<Qn>,<Qm>	vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm>
			mnemonic += isBitEnabled(opcode, 20) ? ".s16" : ".s32";
			instruction = mnemonic + TAB + getVFPQorDdnmRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vqdmulh__scalar:
			// vqdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>	vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>
		case thumb2_vqrdmulh__scalar:
			// vqrdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>	vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>
			{
				int q = getBit(opcode, 28);
				instruction = mnemonic + ".s" + getVFPScalarOperands(opcode, q, q);
			}
			// No pc check: not applicable
			break;

		case thumb2_vqmov:
			// vqmov{u}n<c>.<type><size> <Dd>,<Qm>
			instruction = mnemonic + getVFP_vqmov_instruction(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vqrshl:		// A8.6.364 VQRSHL				
			// vqrshl<c>.<type><size> <Qd>,<Qm>,<Qn>	
			// vqrshl<c>.<type><size> <Dd>,<Dm>,<Dn>
		case thumb2_vqshl__reg:	// A8.6.366 VQSHL (register)
			// vqshl<c>.<type><size>  <Qd>,<Qm>,<Qn>
			// vqshl<c>.<type><size>  <Dd>,<Dm>,<Dn>
		case thumb2_vrshl:		// A8.6.375 VRSHL
			// vrshl<c>.<type><size>  <Qd>,<Qm>,<Qn>
			// vrshl<c>.<type><size>  <Dd>,<Dm>,<Dn>
		case thumb2_vshl__reg:	// A8.6.383 VSHL (register)
			// vshl<c>.<type><size>        <Qd>,<Qm>,<Qn>
			// vshl<c>.<type><size>        <Dd>,<Dm>,<Dn>
			instruction = mnemonic + getVFPSorUDataType(opcode, 28) + getVFPQorDdmnRegs(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vqrshr:
			// vqrshr{u}n<c>.<type><size> <Dd>,<Qm>,#<imm>
		case thumb2_vqshr:
			// vqshr{u}n<c>.<type><size> <Dd>,<Qm>,#<imm>
			instruction = mnemonic + getVFP_vqXshr_instruction(opcode, 28);
			// No pc check: not applicable
			break;

		case thumb2_vqshl__imm:
			// vqshl{u}<c>.<type><size> <Qd>,<Qm>,#<imm>	vqshl{u}<c>.<type><size> <Dd>,<Dm>,#<imm>
			instruction = mnemonic + getVFP_vqshl_instruction(opcode, 28);
			// No pc check: not applicable
			break;

		case thumb2_vrecpe:
			// vrecpe<c>.<dt> <Qd>, <Qm>
			// vrecpe<c>.<dt> <Dd>, <Dm>
		case thumb2_vrsqrte:
			// vrsqrte<c>.<dt> <Qd>, <Qm>
			// vrsqrte<c>.<dt> <Dd>, <Dm>
			instruction = mnemonic + getVFPSorUorFQorDdmOperands(opcode, 8, 10);
			// No pc check: not applicable
			break;

		case thumb2_vrev:
			// vrev<n><c>.<size> <Qd>, <Qm>	vrev<n><c>.<size> <Dd>, <Dm>
			instruction = mnemonic + getVFP_vrev_instruction(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vrshr:
			// vrshr<c>.<type><size> <Qd>, <Qm>, #<imm>	vrshr<c>.<type><size> <Dd>, <Dm>, #<imm>
		case thumb2_vrsra:
			// vrsra<c>.<type><size> <Qd>,<Qm>,#<imm>	vrsra<c>.<type><size> <Dd>,<Dm>,#<imm>
		case thumb2_vshr:
			// vshr<c>.<type><size> <Qd>,<Qm>,#<imm>	vshr<c>.<type><size> <Dd>,<Dm>,#<imm>
		case thumb2_vsra:
			// vsra<c>.<type><size> <Qd>,<Qm>,#<imm>	vsra<c>.<type><size> <Dd>,<Dm>,#<imm>
			mnemonic += (isBitEnabled(opcode, 28) ? ".u" : ".s");
// no break
		case thumb2_vsri:
			// vsri<c>.<size> <Qd>,<Qm>,#<imm>	vsri<c>.<size> <Dd>,<Dm>,#<imm>
			instruction = mnemonic + getVFP_vXrX_instruction(opcode, true);
			// No pc check: not applicable
			break;
			
		case thumb2_vshl__imm:
			// vshl<c>.i<size> <Qd>,<Qm>,#<imm>	vshl<c>.i<size> <Dd>,<Dm>,#<imm>
		case thumb2_vsli:
			// vsli<c>.<size> <Qd>,<Qm>, #<imm>	vsli<c>.<size> <Dd>,<Dm>,#<imm>
			instruction = mnemonic + getVFP_vXrX_instruction(opcode, false);
			// No pc check: not applicable
			break;

		case thumb2_vrshrn:
			// vrshrn<c>.i<size> <Dd>,<Qm>,#<imm>
		case thumb2_vshrn:
			// vshrn<c>.i<size>  <Dd>,<Qm>,#<imm>
			instruction = mnemonic + getVFP_vXshrn_instruction(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vshll__max:
			// A8.6.384 VSHLL
			// vshll<c>.<type><size> <Qd>, <Dm>, #<imm> (<imm> == <size>)
			// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 0 Vd_0_15_12 0 0 1 1 0 0 M_0_5_5 0 Vm_0_3_0
			mnemonic += getVFPIDataType3(opcode, 18);
			instruction = mnemonic + TAB + getVFPQorDReg(opcode, 1, 12, 22)
					+ ',' + getVFPQorDReg(opcode, 0, 0, 5) + ",#" + (8 << (opcode >> 18 & 3));
			// No pc check: not applicable
			break;

		case thumb2_vtb:
			// v<op><c>.8 <Dd>, <list>, <Dm>
			instruction = mnemonic + getVFP_vtb_instruction(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vtrn:
		case thumb2_vuzp:
		case thumb2_vzip:
			instruction = mnemonic + getVFPSzQorDdmOperands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_vtst:
			instruction = mnemonic + getVFPSzQorDdnmOperands(opcode);
			// No pc check: not applicable
			break;




			// CoProcessor instructions

		case thumb2_cdp:
			// cdp<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>
		case thumb2_cdp2:
			// cdp2<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>
			instruction = mnemonic + "\t" + getCo_cdp_operands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_cps:
			// cps<effect>.w <iflags>{,#<mode>} Not permitted in IT block.	cps #<mode> Not permitted in IT block.
			instruction = mnemonic + getCo_cps_instruction(opcode, true);
			// No pc check: not applicable
			break;

		case thumb2_ldc:	// A8.6.51 LDC, LDC2 (immediate)	// ldc{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}
																// ldc{l} <coproc>,<CRd>,[<Rn>],#+/-<imm>
																// ldc{l} <coproc>,<CRd>,[<Rn>],<option>
							// A8.6.51 LDC, LDC2 (immediate)	// ldc2{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}
																// ldc2{l} <coproc>,<CRd>,[<Rn>],#+/-<imm>
																// ldc2{l} <coproc>,<CRd>,[<Rn>],<option>
		case thumb2_stc:	// A8.6.188 STC, STC2				// stc{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}
																// stc{l} <coproc>,<CRd>,[<Rn>],#+/-<imm>
																// stc{l} <coproc>,<CRd>,[<Rn>],<option>
							// A8.6.188 STC, STC2				// stc2{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}
																// stc2{l} <coproc>,<CRd>,[<Rn>],#+/-<imm>
																// stc2{l} <coproc>,<CRd>,[<Rn>],<option>
			// . . . . . . . P_1_8_8 U_1_7_7 D_1_6_6 W_1_5_5 . Rn_1_3_0 CRd_0_15_12 coproc_0_11_8 imm8_0_7_0
			if (isBitEnabled(opcode, 28))
				mnemonic += '2';
			instruction = mnemonic + (isBitEnabled(opcode, 22) ? "l\t" : "\t") + getCoprocessor(opcode)
						  + ',' + getCR_12(opcode) + ',' + getAddrModeImm8(opcode);
			break;

		case thumb2_mcr:
			// mcr<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
			// mcr2<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
			if (isBitEnabled(opcode, 28))
				mnemonic += '2';
			instruction = mnemonic + getCo_mcr_operands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_mcrr:
			// mcrr<c>  <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
			// mcrr2<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
		case thumb2_mrrc:
			// mrrc<c>  <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
			// mrrc2<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
			if (isBitEnabled(opcode, 28))
				mnemonic += '2';
			instruction = mnemonic + getCo_mrr_operands(opcode);
			// No pc check: not applicable
			break;

		case thumb2_mrc:
			// mrc<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
			// mrc2<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
			if (isBitEnabled(opcode, 28))
				mnemonic += '2';
			instruction = mnemonic + getCo_mrc_operands(opcode);
			// No pc check: not applicable
			break;

		default:
			instruction = IDisassembler.INVALID_OPCODE;
			break;
		}
		return instruction;
	}











	private String getAddrMode(int opcode) {
		int amode = (opcode >> 23) & 0x3;
		if (amode == 0)
			return "da";
		else if (amode == 2)
			return "db";
		else if (amode == 1)
			return "ia";
		else
			return "ib";
	}

	private String getAddrMode2(int opcode, int pPos) {
		int regOffset = (opcode >> 25) & 1;
		int scaled = (opcode >> 5) & 3;
		int shiftValue = (opcode >> 7) & 0x1f;
		int shiftMode = (opcode >> 5) & 3;
		int offset = opcode & 0xfff;
		String operands = "[" + getR_16(opcode);
		String sign = isBitEnabled(opcode, 23) ? "" : "-";
		if (isBitEnabled(opcode, pPos)) {
			if (regOffset == 0) { // immediate offset
				if (offset != 0)
					operands += ",#" + sign + getHexValue(offset);
			} else {
				operands += "," + sign + getR_0(opcode);
				if (scaled != 0 || shiftValue != 0) // scaled register offset 
					operands += getAddrMode2ScaledRegOffset(opcode, shiftValue, shiftMode);
			}
			operands += isBitEnabled(opcode, 21) ? "]!" : "]";
		} else { // post-indexed
			if (regOffset == 0) { // immediate offset
				operands += "]";
				if (offset != 0)
					operands +=	",#" + sign + getHexValue(offset);
			} else {
				operands += "]," + sign + getR_0(opcode);
				if (scaled != 0 || shiftValue != 0) // scaled register offset 
					operands += getAddrMode2ScaledRegOffset(opcode, shiftValue, shiftMode);
			}
		}
		return operands;
	}

	private String getAddrMode2ScaledRegOffset(int opcode, int shiftValue, int shiftMode) {
		String ops = "," + getShiftMode(opcode);
		if (shiftValue == 0 && (shiftMode == 1 || shiftMode == 2)) // lsr & asr encode shift 32 as shift 0
			shiftValue = 32;
		if (shiftValue != 0)
			ops += "#" + shiftValue;
		return ops;
	}

	private String getAddrModeImm8(int opcode) {
		int p = (opcode >> 24) & 1;
		int u = (opcode >> 23) & 1;
		int w = (opcode >> 21) & 1;
		int offset = (opcode & 0xff) * 4;
		String operands = "";
		String sign = (u == 1) ? "" : "-";
		if (p == 1) {
			operands = "[" + getR_16(opcode);
			if (offset != 0 || u == 0)
				operands += ",#" + sign + getHexValue(offset);
			operands += "]";
			if (w == 1) { // pre-indexed
				operands += "!";
			}
		} else {
			if (w == 1) { // post-indexed
				operands = "[" + getR_16(opcode) + "],#" + sign + getHexValue(offset);
			} else { // unindexed
				operands = "[" + getR_16(opcode) + "]";
				if (u == 1)
					operands += ",{" + (offset/4) + "}";
				else
					operands += ",#" + getHexValue(offset);
			}
		}
		return operands;
	}

	private String getAddrModePCImm(int opcode, int imm) {
		boolean p = isBitEnabled(opcode, 23);
		String addr = Long.toHexString((address.getValue().longValue() & 0xfffffffc)
					  + (p ? imm : -imm));
		int addrLen = addr.length();
		if (addrLen > 8)
			addr = addr.substring(addrLen - 8);
		return "[pc,#" + (p ? "" : "-") + getHexValue(imm) + "] ; 0x" + addr;
	}

	private String getAddrModeSplitImm8(int opcode) {
		boolean immOffset = isBitEnabled(opcode, 22);
		int offsetHi = opcode >> 4 & 0xf0;
		int offsetLo = opcode & 0xf;
		int offset = offsetHi | offsetLo;
		String operands = "[" + getR_16(opcode);
		String sign = (isBitEnabled(opcode, 23)) ? "" : "-";
		if (isBitEnabled(opcode, 24)) {
			if (immOffset) { // immediate offset
				if (offset != 0)
					operands += ",#" + sign + getHexValue(offset);
			} else { // register offset
				operands += "," + sign + getR_0(opcode);
			}
			operands += isBitEnabled(opcode, 21) ? "]!" : "]";
		} else { // post-indexed
			if (immOffset) { // immediate offset
				operands += "],#" + sign + getHexValue(offset);
			} else { // register offset
				operands += "]," + sign + getR_0(opcode);
			}
		}
		return operands;
	}

	private String getArmCondition(int opcode) {
		return getCondition(opcode >> 28 & 0xf);
	}

	private int getBit(int opcode, int bit) {
		return 1 & (opcode >> bit);
	}

	private int getBranchOffset(int opcode) {
		int offset = ((opcode << 8) >> 6) + 8;
		return offset;
	}
	
	private String getCondition(int condition) {
		switch (condition) {
		case 0:		return "eq";
		case 1:		return "ne";
		case 2:		return "cs";
		case 3:		return "cc";
		case 4:		return "mi";
		case 5:		return "pl";
		case 6:		return "vs";
		case 7:		return "vc";
		case 8:		return "hi";
		case 9:		return "ls";
		case 10:	return "ge";
		case 11:	return "lt";
		case 12:	return "gt";
		case 13:	return "le";

		case 14:	// always (unconditional)

		case 15:
		default:
			return "";
		}
	}

	private String getCoprocessor(int opcode) {
		int cproc = (opcode >> 8) & 0xf;
		return ("p" + cproc);
	}

	private String getCR_12(int opcode) {
		int cReg = (opcode >> 12) & 0xf;
		return ("c" + cReg);
	}

	private String getCR_0(int opcode) {
		int cReg = opcode & 0xf;
		return ("c" + cReg);
	}

	private String getCR_16(int opcode) {
		int reg = (opcode >> 16) & 0xf;
		return ("c" + reg);
	}

	private String getDataBarrierOption(int opcode) {
		int option = opcode & 0xf;
		switch (option) {
			case  2:	return "oshst";
			case  3:	return "osh";
			case  6:	return "nshst";
			case  7:	return "nsh";
			case 10:	return "ishst";
			case 11:	return "ish";
			case 14:	return "st";
			case 15:	return "sy";

			default:
				return "#" + option;
		}
	}

	private String getHexValue(int value) {
		return "0x" + Integer.toHexString(value);
	}

	private String getHexValue(long value) {
		return "0x" + Long.toHexString(value);
	}

	private String getImmediate24(int opcode) {
		int imm24 = opcode & 0xffffff;
		return "#" + getHexValue(imm24);
	}

	private String getImmediate12(int opcode) {
		int imm12 = opcode & 0xfff;
		return "#" + getHexValue(imm12);
	}

	private String getInstructionBarrierOption(int opcode) {
		int option = opcode & 0xf;
		switch (option) {
		case 15:	return "sy";
		default:	return "#" + option;
		}
	}

	// get 4-bit register number from opcode bits 0-3
	private String getR_0(int opcode) {
		int reg = opcode & 0xf;
		return getRegName(reg);
	}

	// get 4-bit register number from opcode bits 8-11
	private String getR_8(int opcode) {
		int reg = (opcode >>  8) & 0xf;
		return getRegName(reg);
	}

	// get 4-bit register number from opcode bits 12-15
	private String getR_12(int opcode) {
		int reg = (opcode >> 12) & 0xf;
		return getRegName(reg);
	}

	// get 4-bit register number from opcode bits 16-19
	private String getR_16(int opcode) {
		int reg = (opcode >> 16) & 0xf;
		return getRegName(reg);
	}

	private String getRegList(int opcode) {
		String r0 = ((opcode & 1) == 1) ? "r0," : "";
		String r1 = (((opcode >> 1) & 1) == 1) ? "r1," : "";
		String r2 = (((opcode >> 2) & 1) == 1) ? "r2," : "";
		String r3 = (((opcode >> 3) & 1) == 1) ? "r3," : "";
		String r4 = (((opcode >> 4) & 1) == 1) ? "r4," : "";
		String r5 = (((opcode >> 5) & 1) == 1) ? "r5," : "";
		String r6 = (((opcode >> 6) & 1) == 1) ? "r6," : "";
		String r7 = (((opcode >> 7) & 1) == 1) ? "r7," : "";
		String r8 = (((opcode >> 8) & 1) == 1) ? "r8," : "";
		String r9 = (((opcode >> 9) & 1) == 1) ? "r9," : "";
		String r10 = (((opcode >> 10) & 1) == 1) ? "r10," : "";
		String r11 = (((opcode >> 11) & 1) == 1) ? "r11," : "";
		String r12 = (((opcode >> 12) & 1) == 1) ? "r12," : "";
		String sp = (((opcode >> 13) & 1) == 1) ? "sp," : "";
		String lr = (((opcode >> 14) & 1) == 1) ? "lr," : "";
		String pc = (((opcode >> 15) & 1) == 1) ? "pc," : "";
		String regList = "{" + r0 + r1 + r2 + r3 + r4 + r5 + r6 + r7 + r8 + r9 + r10 + r11 + r12 + sp + lr + pc + "}";
		regList = regList.replace(",}", "}");
		return regList;
	}

	private String getRegName(int reg) {
		switch (reg) {
		case 13:
			return "sp";
		case 14:
			return "lr";
		case 15:
			return "pc";
		default:
			return ("r" + reg);
		}
	}

	private String getRotationOperand(int opcode, int bit) {
		int rotation = ((opcode >> (bit-3)) & 0x18); 
		return (rotation != 0) ? ",ror #" + rotation : "";		
	}

	private String getShifterOperand(int opcode) {
		int shift, immval;
		int bit25 = (opcode >> 25) & 1;
		if (bit25 == 1) { // bit set - we have immediate shifter operand
			shift = (opcode & 0x0f00) >> 7; // rotate_imm*2
			immval = (opcode & 0xff);
			immval = (immval >> shift) | (immval << (32 - shift));
			return "#" + getHexValue(immval);
		}

		if ((opcode & 0xff0) == 0) // got Rm
			return getR_0(opcode);

		// else shifted
		immval = (opcode >> 7) & 0x1f;
		if ((opcode & 0x70) == 0x0) {// LSL #imm
			return getR_0(opcode) + ",lsl #" + immval;
		} else if ((opcode & 0xf0) == 0x10) {// LSL Rs
			return getR_0(opcode) + ",lsl " + getR_8(opcode);
		} else if ((opcode & 0x70) == 0x20) {// LSR #imm
			if (immval == 0)
				immval = 32;
			return getR_0(opcode) + ",lsr #" + immval;
		} else if ((opcode & 0xf0) == 0x30) {// LSR Rs
			return getR_0(opcode) + ",lsr " + getR_8(opcode);
		} else if ((opcode & 0x70) == 0x40) {// ASR #imm
			if (immval == 0)
				immval = 32;
			return getR_0(opcode) + ",asr #" + immval;
		} else if ((opcode & 0xf0) == 0x50) {// ASR Rs
			return getR_0(opcode) + ",asr " + getR_8(opcode);
		} else if ((opcode & 0xff0) == 0x60) {// RRX
			return getR_0(opcode) + ",rrx";
		} else if ((opcode & 0x70) == 0x60) {// ROR #imm
			return getR_0(opcode) + ",ror #" + immval;
		} else if ((opcode & 0xf0) == 0x70) {// ROR Rs
			return getR_0(opcode) + ",ror " + getR_8(opcode);
		} else {
			return ",<invalid shift operand>";
		}
	}

	private String getShiftMode(int opcode) {
		int shiftMode = (opcode >> 5) & 3;
		int shiftValue = (opcode >> 7) & 0x1f;
		switch (shiftMode) {
		case 0:
			return "lsl";
		case 1:
			return "lsr";
		case 2:
			return "asr";
		case 3:
			if (shiftValue == 0) {
				return "rrx";
			} else {
				return "ror";
			}
		default:
			return "";
		}
	}

	private String getStatusReg(int opcode, int rPos) {
		return (isBitEnabled(opcode, rPos) ? 's' : 'c') + "psr";
	}

	private String getStatusRegFields(int opcode, int mask_lsb) {
		String fields = "_";
		boolean c = isBitEnabled(opcode, mask_lsb);
		boolean x = isBitEnabled(opcode, mask_lsb+1);
		boolean s = isBitEnabled(opcode, mask_lsb+2);
		boolean f = isBitEnabled(opcode, mask_lsb+3);
		if (c) {
			fields += "c";
		}
		if (x) {
			fields += "x";
		}
		if (f) {
			fields += "f";
		}
		if (s) {
			fields += "s";
		}
		return fields;
	}

	/**
	 * Arm--- ... opc1_1_7_4 CRn_1_3_0 CRd___15_12 coproc___11_8 opc2___7_5 . CRm___3_0<br>
	 * Thumb2 ... opc1_1_7_4 CRn_1_3_0 CRd_0_15_12 coproc_0_11_8 opc2_0_7_5 . CRm_0_3_0
	 * @param opcode
	 * @return String containing all cdp/cdp2 instr operands
	 * <p><listing>
	 * A8.6.28 CDP, CDP2
	 *	{@literal cdp<c>  <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>}
	 *	{@literal cdp2<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>}
	 */
	private String getCo_cdp_operands(int opcode) {
		int cpOpc1 = (opcode >> 20) & 0xf;
		int cpOpc2 = (opcode >> 5) & 7;
		return TAB
			+ getCoprocessor(opcode) + ',' + getHexValue(cpOpc1) + ',' + getCR_12(opcode)
			+ ',' + getCR_16(opcode) + ',' + getCR_0(opcode) + ',' + getHexValue(cpOpc2);

	}

	/**
	 * Arm--- ... imod_19_18  M_17_17 ...A___7_7 I___6_6 F___5_5 mode_0_4_0<br>
	 * Thumb2 ... ... imod_0_10_9 M_0_8_8 A_0_7_7 I_0_6_6 F_0_5_5 mode_0_4_0
	 * @param opcode
	 * @param thumb2 boolean: pass true if thumb2 instr (false if ARM)
	 * @return String containing cps mnemonic postfix + TAB + operands
	 * <p><listing>
	 * B6.1.1 CPS Not permitted in IT block
	 *	cps {@literal <effect>.w <iflags>}{,#{@literal <mode>}}.
	 *	cps {@literal #<mode>}
	 */
	private String getCo_cps_instruction(int opcode, boolean thumb2) {
		int armAdj = thumb2 ? 0 : 9;
		int imod = opcode >> 9+armAdj & 3;
		boolean changeMode = isBitEnabled(opcode, 8+armAdj);
		int mode = opcode & 0x1f;

		String ops;

		// treat unpredictable ((imod == 0 && !changeMode) || imod == 1) as a change mode to 0
		if (imod == 0 || imod == 1) {
			if (!changeMode || mode == 0)
				ops = TAB + "#0";
			else
				ops = TAB + "#" + mode;
		} else {
			ops = "i" + (imod == 2 ? 'e' : 'd') + TAB;
			armAdj = thumb2 ? 0 : 1;
			if (isBitEnabled(opcode, 7 + armAdj))
				ops += "a";
			if (isBitEnabled(opcode, 6 + armAdj))
				ops += "i";
			if (isBitEnabled(opcode, 5 + armAdj))
				ops += "f";
			if (changeMode)
				ops += ",#" + mode;
		}
		return ops;
	}

	/**
	 * Arm--- ... opc1_23_21 . CRn_19_16 Rt___15_12 coproc___11_8 opc2___7_5 . CRm___3_0<br>
	 * Thumb2 ... opc1_1_7_5 . CRn_1_3_0 Rt_0_15_12 coproc_0_11_8 opc2_0_7_5 . CRm_0_3_0
	 * @param opcode
	 * @return String containing + TAB + mcr/mcr2 operands
	 * <p><listing>
	 * A8.6.92 MCR, MCR2
	 *	mcr{@literal <c>  <coproc>,<opc1>,<Rt>,<CRn>,<CRm>}{,{@literal <opc2>}}
	 *	mcr2{@literal <c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>}{,{@literal <opc2>}}
	 */
	private String getCo_mcr_operands(int opcode) {
		int cpOpc1 = opcode >> 21 & 7;
		int cpOpc2 = opcode >> 5  & 7;
		return TAB
			+ getCoprocessor(opcode) + ',' + getHexValue(cpOpc1)
			+ ',' + getR_12(opcode) + ',' + getCR_16(opcode) + ',' + getCR_0(opcode)
			+ (cpOpc2 != 0 ? "," + getHexValue(cpOpc2) : "");
	}

	/**
	 * Arm--- ... opc1_23_21 . CRn_19_16 Rt___15_12 coproc___11_8 opc2___7_5 . CRm___3_0<br>
	 * Thumb2 ... opc1_1_7_5 . CRn_1_3_0 Rt_0_15_12 coproc_0_11_8 opc2_0_7_5 . CRm_0_3_0
	 * @param opcode
	 * @return String containing + TAB + mrc/mrc2 operands
	 * <p><listing>
	 * A8.6.100 MRC, MRC2
	 *	mrc{@literal <c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>}{,{@literal <opc2>}}
	 *	mcr2{@literal <c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>}{,{@literal <opc2>}}
	 */
	private String getCo_mrc_operands(int opcode) {
		int cpOpc1 = opcode >> 21 & 7;
		int cpOpc2 = opcode >> 5  & 7;
		int rt = opcode >> 12 & 0xf;
		return TAB
			+ getCoprocessor(opcode) + ',' + getHexValue(cpOpc1)
			+ ',' + (rt == 15 ? "apsr_nzcv" : getRegName(rt))
			+ ',' + getCR_16(opcode) + ',' + getCR_0(opcode)
			+ (cpOpc2 != 0 ? "," + getHexValue(cpOpc2) : "");
	}

	/**
	 * Arm--- ... Rt2_19_16 Rt___15_12 coproc___11_8 opc1___7_4 CRm___3_0<br>
	 * Thumb2 ... Rt2_1_3_0 Rt_0_15_12 coproc_0_11_8 opc1_0_7_4 CRm_0_3_0
	 * @param opcode
	 * @return String containing + TAB + mcrr/mcrr2 operands
	 * <p><listing>
	 * A8.6.93 MCRR, MCRR2
	 *	{@literal mcrr<c>  <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>}
	 *	{@literal mcrr2<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>}
	 * A8.6.101 MRRC, MRRC2
	 *	{@literal mrrc<c>  <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>} 
	 *	{@literal mrrc2<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>}
	 */
	private String getCo_mrr_operands(int opcode) {
		int cpOpc = opcode >> 4 & 0xf;
		return TAB
			+ getCoprocessor(opcode) + ',' + getHexValue(cpOpc)
			+ ',' + getR_12(opcode) + ',' + getR_16(opcode) + ',' + getCR_0(opcode);
	}

	private int getThumbBranchOffset8(int opcode) {
		int offset = (byte)(opcode & 0xff);
	
		offset = (offset*2) + 4;
		return offset;
	}

	private int getThumbBranchOffset11(int opcode) {
		short offset = (short) ((opcode << 5) & 0xffff);
	
		return (offset / 16) + 4;
	}

	private String getThumbCondition(int opcode) {
		int condition = (opcode >> 8) & 0xf;
		return getCondition(condition);
	}

	private String getThumbEffect(int opcode) {
		int imod = (opcode >> 4) & 1;
		if (imod == 0) {
			return "ie";
		} else {
			return "id";
		}
	}

	private String getThumbIFlags(int opcode) {
		int a = (opcode >> 2) & 1;
		int i = (opcode >> 1) & 1;
		int f = (opcode & 1);
		String iflags = "";
		iflags += (a == 1) ? "a" : "";
		iflags += (i == 1) ? "i" : "";
		iflags += (f == 1) ? "f" : "";
		return iflags;
	}

	private String getThumbImmediate3(int opcode) {
		int imm = (opcode >> 6) & 7;
		return "#" + imm;
	}

	private String getThumbImmediate5(int opcode, int multiplier) {
		int imm = ((opcode >> 6) & 0x1f) * multiplier;
		return "#" + getHexValue(imm);
	}

	private String getThumbImmediate7(int opcode, int multiplier) {
		int imm = (opcode & 0x7f) * multiplier;
		return "#" + getHexValue(imm);
	}

	private String getThumbImmediate8(int opcode, int multiplier) {
		int imm = (opcode & 0xff) * multiplier;
		return "#" + getHexValue(imm);
	}

	private String getThumbReg(int opcode, int position) {
		int reg = (opcode >> position) & 7;
		return getRegName(reg);
	}

	private String getThumbRegHigh(int opcode, int position, int hBit) {
		int reg = (opcode >> position) & 7;
		int high = (opcode >> hBit) & 1;
		reg += (high == 1) ? 8 : 0;
		return getRegName(reg);
	}

	private String getThumbRegList(int opcode, String otherRegs) {
		String r0 = ((opcode & 1) == 1) ? "r0," : "";
		String r1 = (((opcode >> 1) & 1) == 1) ? "r1," : "";
		String r2 = (((opcode >> 2) & 1) == 1) ? "r2," : "";
		String r3 = (((opcode >> 3) & 1) == 1) ? "r3," : "";
		String r4 = (((opcode >> 4) & 1) == 1) ? "r4," : "";
		String r5 = (((opcode >> 5) & 1) == 1) ? "r5," : "";
		String r6 = (((opcode >> 6) & 1) == 1) ? "r6," : "";
		String r7 = (((opcode >> 7) & 1) == 1) ? "r7," : "";
		String regList = "{" + r0 + r1 + r2 + r3 + r4 + r5 + r6 + r7;
		if (otherRegs != null) {
			regList += otherRegs;
		}
		regList += "}";
		regList = regList.replace(",}", "}");
		return regList;
	}

	private String getThumb2AddrModeImm8(int opcode, int pBit, int uBit, int wBit, int shift) {
		int offset = (opcode & 0xff) << shift;
		String operands = '[' + getR_16(opcode);
		String sign = isBitEnabled(opcode, uBit) ? "" : "-";
		if (isBitEnabled(opcode, pBit)) {
			if (offset != 0) {
				operands += ",#" + sign + getHexValue(offset);
			}
			operands += isBitEnabled(opcode, wBit) ? "]!" : "]";
		} else {// post-indexed
			operands += ']';
			if (offset != 0) {
				operands += ",#" + sign + getHexValue(offset);
			}
		}
		return operands;
	}

	private String getThumb2AddrModeRegImm(int opcode) {
		String operands = '[' + getR_16(opcode) + ',' + getR_0(opcode);
		int imm2 = opcode >> 4 & 3;
		if (imm2 != 0) {
			operands += ",lsl #" + imm2;
		}
		operands += ']';
		return operands;
	}

	private int getThumb2_condB_Offset(int opcode) {
		int sSignExtended = signExtend(getBit(opcode, 27) << 20, 20, 32);
		int j1 = getBit(opcode, 13) << 18;
		int j2 = getBit(opcode, 11) << 19;
		return sSignExtended | j2 | j1 | opcode>>4&0x3f000 | (opcode & 0x7ff) << 1;
	}

	private int getThumb2_uncondB_Offset(int opcode, boolean j1, boolean j2) {
		int s = getBit(opcode, 27);
		int sSignExtended = signExtend(s << 24, 24, 32);
		boolean sb = s == 1;
		int i1 = (j1 ^ sb) ? 0 : 1<<22;
		int i2 = (j2 ^ sb) ? 0 : 1<<23;
		return sSignExtended | i2 | i1 | opcode >> 4 & 0X3ff000 | (opcode & 0x7ff) << 1;
	}

	private String getThumb2Condition(int opcode) {
		return getCondition((opcode >> 22) & 0xf);
	}

	private String getThumb2ExpandImm12(int opcode) {
		int imm12 = getThumb2RawImm12(opcode), imm32 = 0;
		if (0 == (imm12 >> 10 & 3)) {
			int imm8 = imm12 & 0xff;
			switch (imm12 >> 8 & 3) {
			case 2:
				imm32 = imm8 << 24 | imm8 << 8;
				break;

			case 3:
				imm32 = imm8 << 24 | imm8 << 8;
			case 1:
				imm32 |= imm8 << 16;
			case 0:
				imm32 |= imm8;
			}
		} else {
			int shift = imm12 >> 7 & 31;
			imm32 = 0x80 | imm12 & 0x7f;
			imm32 = imm32 >> shift | imm32 << 32-shift;
		}

		return getHexValue(imm32);
	}

	private String getThumb2ImmForMovX(int x) {
		return getHexValue((x & 0xf0000) >> 4 | getThumb2RawImm12(x));
	}

	private int getThumb2RawImm12(int x) {
		return (x & 0x04000000) >> 0xf | (x & 0x7000) >> 4 | x & 0xff;
	}

	private static final int
		LSL = 0, LSR = 1, ASR = 2, RRX_ROR = 3;

	private String getThumb2ShiftMode(int opcode, int typePos) {
		int type = opcode >> typePos & 3;
		int value = getThumb2ShiftValue(opcode, type);
		if (type == 0 && value == 0)
			return "";

		String shift = ",";
		switch (type) {
			case LSL:	shift += "lsl";		break;
			case LSR:	shift += "lsr";		break;
			case ASR:	shift += "asr";		break;
			case RRX_ROR:
				if (value == 0)
					return ",rrx";
				shift += "ror";
		}
		return shift + " #" + value; 
	}

	private int getThumb2ShiftValue(int opcode, int type) {
		int value = (opcode & 0x7000) >> 10 | opcode >> 6 & 3;
		switch (type) {
			case LSR:
			case ASR:
				if (0 == value)
					value = 32;
		}
		return value;
	}


	private String getVFPDorSRegList(int opcode, boolean isD) {
		int vd15_12 = opcode >> 12 & 0xf;
		int d22 = getBit(opcode, 22);
		int reg = isD ? d22 << 4 | vd15_12 : vd15_12 << 1 | d22;
		int count = opcode & 0xff;
		if (isD)
			count /= 2;

		String list = "{" + (isD ? 'd' : 's') + reg;

		if (count > 1)
			list += (isD ? "-d" : "-s") + (reg+count-1);
//		for (int i = 0; i < count; i++) {
//			if (count != 0)
//				list += ",";
//			list += (isD ? "d" : "s") + (reg + i);
//		}
		list += "}";
		return list;
	}

	private String getVFPDdQmRegs(int opcode) {
		return getVFPQorDReg(opcode, 0, 12, 22) + ',' + getVFPQorDReg(opcode, 1, 0, 5);
	}	

	private String getVFPIncDec(int opcode) {
		int mode = (opcode >> 23) & 3;
		switch (mode) {
		case 1:			return "ia";
		case 2:			return "db";
		default:
			return "";
		}
	}

	private int getVFPDataTypeSize (int opcode, int sizePos) {
		return 8 << (opcode >> sizePos & 3);
	}
	
	/**
	 * Return 6-bit immediate instruction size
	 * 
	 * @param opcode
	 * @param uPos bit indicating unsigned
	 * @param opPos
	 * @return .<type><size>
	 */
	private String getVFPImm6Size(int opcode) {
		int imm6 = opcode >> 16 & 0x3f;
		if (isBitEnabled(imm6, 5))
			return "64";
		else if (isBitEnabled(imm6, 4))
			return "32";
		else
			return "16";
	}

	/**
	 * Return vqshl{u} sign or unsigned type with bit size
	 * 
	 * @param opcode
	 * @param lBit
	 * @param uPos bit indicating unsigned
	 * @param opPos
	 * @return .<type><size>
	 */
	private String getVFPVqshlTypeSize(int opcode, int lBit, int uPos, int opPos) {
		return ((isBitEnabled(opcode, uPos) && isBitEnabled(opcode, opPos)) ? ".u" : ".s")
				+ getVFPLImm6Size(opcode, lBit);
	}

	private String getVFPIDataType(int opcode, int sizePos) {
		return ".i" + getVFPDataTypeSize(opcode, sizePos);
	}

	private String getVFPIDataType2(int opcode, int sizePos) {
		return getVFPIDataType((opcode >> sizePos & 3) + 1, 0);
	}

	private String getVFPIDataType3(int opcode, int sizePos) {
		return ".i" + (8 << (opcode >> sizePos & 3));
	}

	private String getVFPPDataType (int opcode, int sizePos) {
		return ".p" + getVFPDataTypeSize(opcode, sizePos);
	}

	// depending on d, separate register bit is either before or
	// after the rest of the register bits
	private String getVFPDorSReg(int opcode, int d, int regPos, int regBitPos) {
		if (d == 1)
			return "d" + ((((opcode >> regBitPos) & 1) << 4) | ((opcode >> regPos) & 0xf));
		else
			return "s" + ((((opcode >> regPos) & 0xf) << 1) | ((opcode >> regBitPos) & 1));
	}

	private int getVFPImm6SHRAdj(int opcode, int imm) {
		if (isBitEnabled(opcode, 21))
			imm = 32 - imm;
		else if (isBitEnabled(opcode, 20))
			imm = 16 - imm;
		else if (isBitEnabled(opcode, 19))
			imm = 8 - imm;
		return imm;
	}

	/**
	 * @return 0 when imm6=8|16|32 else value minus top-bit
	 */
	private int getVFPQImm6(int opcode) {
		int imm6 = opcode >> 16 & 0x3f;
		if (imm6 == 32 || imm6 == 16 || imm6 == 8) {
			imm6 = 0;
		} else if (isBitEnabled(imm6, 5)) {
			imm6 &= 0x1f;
		} else if (isBitEnabled(imm6, 4)) {
			imm6 &= 0x0f;
		} else { // assuming isBitEnabled(imm, 4)
			imm6 &= 0x07;
		}
		return imm6;
	}

	private int getVFPImm6Encoded(int opcode) {
		int imm6 = opcode >> 16 & 0x3f;
		if (isBitEnabled(imm6, 5)) {
			return 32 - (imm6 & 0x1f);
		} else if (isBitEnabled(imm6, 4)) {
			return 16 - (imm6 & 0xf);
		} else { // assuming isBitEnabled(imm6, 3)
			return 8 - (imm6 & 0x7);
		}
	}

	private int getVFPImm6(int opcode) {
		int imm6 = opcode >> 16 & 0x3f;
		if (isBitEnabled(imm6, 5)) {
			return imm6 & 0x1f;
		} else if (isBitEnabled(imm6, 4)) {
			return imm6 & 0xf;
		} else { // assuming isBitEnabled(imm6, 3)
			return imm6 & 0x7;
		}
	}

	private String getVFPLImm6Size(int opcode, int lBit) {
		if (lBit == 1)
			return "64";
		else {
			int imm6 = opcode >> 16 & 0x3f;
			if (isBitEnabled(imm6, 5))
				return "32";
			else if (isBitEnabled(imm6, 4))
				return "16";
			else
				return "8";
		}
	}

	private int getVFPQorDRegNum(int opcode, int regPos, int regMsbPos) {
		return (((opcode >> regMsbPos) & 1 ) << 4) | ((opcode >> regPos) & 0xf);
	}

	private String getVFPQorDReg(int opcode, int q, int regPos, int regMsbPos) {
		return (q == 1)
				? "q" + getVFPQorDRegNum(opcode, regPos, regMsbPos) / 2
				: "d" + getVFPQorDRegNum(opcode, regPos, regMsbPos);
	}

	private String getVFPQUNUorSType(int opcode, int uBit, int opBit) {
		int op = getBit(opcode, uBit) << 1 | getBit(opcode, opBit);
		return (1 == op ? "un" : "n") + (3 == op ? ".u" : ".s");
	}

	private String getVFPQUUorSType(int opcode, int l, int uPos, int opPos) {
		return ((isBitEnabled(opcode, uPos) && !isBitEnabled(opcode, opPos)) ? "u" : "")
				+ getVFPVqshlTypeSize(opcode, l, uPos, opPos);
	}


	private String  getVFPSize(int opCmode) {
		switch (opCmode) {
		case 14:
			return ".i8";
		case  8: case  9: case 10: case 11:
		case 24: case 25: case 26: case 27:
			return ".i16";
		default:
			return ".i32";
		case 30:
			return ".i64";
		case 15:
			return ".f32";
		case 31:
			return "";
		}
	}

	private String getVFPIorFDataType(int opcode, int sizePos, int bitIorF) {
		int size  = 8 << ((opcode >> sizePos) & 0x3);
		int f = (opcode >> bitIorF) & 1;
		return (f == 1 ? ".f" : ".i") + size; 
	}

	private String getVFPSorUDataType(int opcode, int uPos) {
		return (isBitEnabled(opcode, uPos) ? ".u" : ".s")
				+ getVFPDataTypeSize(opcode, 20);
	}

	private String getVFPSorUorFDataType(int opcode, int sizePos, int fPos, int uPos) {
		return (isBitEnabled(opcode, fPos) ? ".f" : (isBitEnabled(opcode, uPos) ? ".u" : ".s"))
				+ getVFPDataTypeSize(opcode, sizePos);
	}

	private String getVFPSpecialReg(int opcode) {
		switch (opcode >> 16 & 0xf) {
			case  0:	return "fpsid";
			case  1: 	return "fpscr";
			case  6: 	return "mvfr1";
			case  7: 	return "mvfr0";
			case  8:	return "fpexc";
			case  9:	return "fpinst";
			case 10:	return "fpinst2";
			default:	return "<unknown register>";
		}
	}

	private String getVFPTdTm(int opcode, int opPos, int sizePos) {
		switch ((opcode >> opPos & 3) << 2 | opcode >> sizePos & 3) {
			case 10:	return ".s32.f32";
			case 14:	return ".u32.f32";
			case 2:		return ".f32.s32";
			case 6:		return ".f32.u32";
		}
		return "";
	}

	private String getVFPTdTm2(int opcode, int opPos, int unsignedPos) {
		switch ((opcode >> opPos & 1) << 1 | opcode >> unsignedPos & 1) {
			case 0:		return ".f32.s32";
			case 1:		return ".f32.u32";
			case 2:		return ".s32.f32";
			case 3:		return ".u32.f32";
		}
		return "";
	}


	private String getVFPTdTm3(int opcode, int op2Pos, int opPos, int sz) {
		int opc2 = opcode >> op2Pos & 7;
		if (opc2 == 5)
			return sz == 1 ? ".s32.f64" : ".s32.f32";
		else if (opc2 == 4)
			return sz == 1 ? ".u32.f64" : ".u32.f32";
		else
			switch (sz << 1 | getBit(opcode, opPos)) {
				case 3:		return ".f64.s32";
				case 2:		return ".f64.u32";
				case 1:		return ".f32.s32";
				case 0:		return ".f32.u32";
			}
		return "";	// should never get here
	}

	private String getVFPVcvtType(int opcode) {
		boolean op = isBitEnabled(opcode, 18);
		boolean u  = isBitEnabled(opcode, 16);
		boolean sf = isBitEnabled(opcode,  8);
		boolean sx = isBitEnabled(opcode,  7);

		String td = u ? (sx ? ".u32" : ".u16") : (sx ? ".s32" : ".s16");
		String ts = sf ? ".f64" : ".f32";
		
		return op ? td + ts : ts + td;
	}

	private String getVFPVldVstSize(int size) {
		if (size == 0)
			return ".8";
		else if (size == 1)
			return ".16";
//		else if (size == 2)
			return ".32";
//		return "";
	}

	private String getVFPVldVstEnding(int opcode) {
		int Rm = opcode & 0xf;

		String suffix = "";
		if (Rm == 15)
			;
		else if (Rm == 13)
			suffix = "!";
		else
			suffix = "," + getRegName(Rm);
		
		return suffix;
	}

	/**
	 * ARM--- ... D_22_22 size_21_20 Vn_19_16 Vd___15_12 ... N___7_7 Q___6_6 M___5_5 0 Vm___3_0<br>
	 * Thumb2 ... D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 ... N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
	 * @param opcode
	 * @return String containing "i8", "i16", "i32" or "i64", + TAB + proper Q-or-D _d,_n,_m register operands
	 * <p><listing>
	 * A8.6.271 VADD (integer)
	 *	{@literal vadd<c>.<dt> <Qd>,<Qn>,<Qm>	vadd<c>.<dt>  <Dd>,<Dn>,<Dm>}
	 * A8.6.280 VCEQ (register)
	 *	{@literal vceq<c>.<dt> <Qd>,<Qn>,<Qm>	vceq<c>.<dt>  <Dd>,<Dn>,<Dm>}
	 * A8.6.349 VPADD (integer)
	 *	{@literal vpadd for q==1 UNDEFINED  	vpadd<c>.<dt> <Dd>,<Dn>,<Dm>}
	 * A8.6.401 VSUB (integer)
	 *	{@literal vsub<c>.<dt> <Qd>,<Qn>,<Qm>	vsub<c>.<dt>  <Dd>,<Dn>,<Dm>}
	 */
	private String getVFPIDataTypeQorDdnmOperands(int opcode) {
		return getVFPIDataType(opcode, 20) + getVFPQorDdnmRegs(opcode);
	}	
	
	/**
	 * ARM--- ... D_22_22 size_21_20 Vn_10_16 Vd___15_12 ... N___7_7 . M___5_5 . Vm___3_0<br>
	 * Thumb2 ... D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 ... N_0_7_7 . M_0_5_5 . Vm_0_3_0
	 * @param opcode
	 * @return String containing "i16", "i32" or "i64", + TAB + the approriate Dd,Qn,Qm register operands
	 * <p><listing>
	 * A8.6.273 VADDHN
	 *	{@literal vaddhn<c>.<dt>  <Dd>,<Qn>,<Qm>}
	 * A8.6.370 VRADDHN
	 *	{@literal vraddhn<c>.<dt> <Dd>,<Qn>,<Qm>}
	 * A8.6.381 VRSUBHN
	 *	{@literal vrsubhn<c>.<dt> <Dd>,<Qn>,<Qm>}
	 * A8.6.403 VSUBHN
	 *	{@literal vsubhn<c>.<dt>  <Dd>,<Qn>,<Qm>}
	 */
	private String getVFPIDataType2DdQnDmOperands(int opcode) {
		String ops = getVFPIDataType2(opcode, 20)
					 + TAB + getVFPQorDReg(opcode, 0, 12, 22)
					 + ',' + getVFPQorDReg(opcode, 1, 16, 7)
					 + ',' + getVFPQorDReg(opcode, 1, 0, 5);
		return ops;
	}	

	/**
	 * ARM--- ... D_22_22 size_21_20 Vn_19_16 Vd___15_12 ... N___7_7 . M___5_5 . Vm___3_0<br>
	 * Thumb2 ... D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 ... N_0_7_7 . M_0_5_5 . Vm_0_3_0
	 * @param opcode
	 * @return String containing a TAB + the approriate Qd,Dn,Dm register operands
	 * <p><listing>
	 * A8.6.266 VABA, VABAL
	 *	{@literal vabal<c>.<dt>   <Qd>,<Dn>,<Dm>}
	 * A8.6.267 VABD, VABDL (integer)
	 *	{@literal vabdl<c>.<dt>   <Qd>,<Dn>,<Dm>}
	 * A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
	 *	{@literal vmlal<c>.<dt>   <Qd>,<Dn>,<Dm>}
	 *	{@literal vmlsl<c>.<dt>   <Qd>,<Dn>,<Dm>}
	 * A8.6.337 VMUL, VMULL (integer and polynomial)
	 *	{@literal vmull<c>.<dt>   <Qd>,<Dn>,<Dm>}
	 * A8.6.358 VQDMLAL, VQDMLSL
	 *  {@literal vqd<op><c>.<dt> <Qd>,<Dn>,<Dm>}
	 * A8.6.360 VQDMULL
	 *	{@literal vqdmull<c>.<dt> <Qd>,<Dn>,<Dm>}
	 * 
	 */
	private String getVFPQdDnDmRegs(int opcode) {
		String regs = getVFPQorDReg(opcode, 1, 12, 22)
					  + ',' + getVFPQorDReg(opcode, 0, 16, 7)
					  + ',' + getVFPQorDReg(opcode, 0, 0, 5);
		return regs;
	}	
	
	/**
	 * ARM--- ...  D_22_22 ... Vd___15_12 ... Q___6_6 M___5_5 . Vm___3_0<br>
	 * Thumb2 ...  D_1_6_6 ... Vd_0_15_12 ... Q_0_6_6 M_0_5_5 . Vm_0_3_0
	 * @param opcode
	 * @return String containing a TAB + the approriate "q" or "d" _d,_m register operands
	 * <p><listing>
	 * A8.6.293 VCNT
	 *	{@literal vcnt<c>.8 <Qd>,<Qm>	vcnt<c>.8 <Dd>,<Dm>}
	 * A8.6.327 VMOV (register)
	 *	{@literal vmov<c>   <Qd>,<Qm>	vmov<c>   <Dd>,<Dm>}
	 * A8.6.341 VMVN (register)
	 *	{@literal vmvn<c>   <Qd>,<Qm>	vmvn<c>   <Dd>,<Dm>}
	 * A8.6.405 VSWP
	 *	{@literal vswp<c>   <Qd>,<Qm>	vswp<c>   <Dd>,<Dm>}
	 */
	private String getVFPQorDdmRegs(int opcode) {
		int q = getBit(opcode, 6);
		return TAB + getVFPQorDReg(opcode, q, 12, 22) + ',' + getVFPQorDReg(opcode, q, 0, 5);
	}	
	
	/**
	 * ARM--- ... D_22_22 ... Vn_19_16 Vd___15_12 ... N___7_7 Q___6_6 M___5_5 . Vm___3_0<br>
	 * Thumb2 ... D_1_6_6 ... Vn_1_3_0 Vd_0_15_12 ... N_0_7_7 Q_0_6_6 M_0_5_5 . Vm_0_3_0
	 * @param opcode
	 * @return String containing a TAB + proper Q-or-D _d,_n,_m register operands
	 * <p><listing>
	 * A8.6.364 VQRSHL
	 *	{@literal vqrshl<c>.<type><size> <Qd>,<Qm>,<Qn>	vqrshl<c>.<type><size> <Dd>,<Dm>,<Dn>}
	 * A8.6.366 VQSHL (register)
	 *	{@literal vqshl<c>.<type><size>  <Qd>,<Qm>,<Qn>	vqshl<c>.<type><size>  <Dd>,<Dm>,<Dn>}
	 * A8.6.375 VRSHL
	 *	{@literal vrshl<c>.<type><size>  <Qd>,<Qm>,<Qn>	vrshl<c>.<type><size>  <Dd>,<Dm>,<Dn>}
	 * A8.6.383 VSHL (register)
	 *	{@literal vshl<c>.i<size>        <Qd>,<Qm>,<Qn>	vshl<c>.i<size>        <Dd>,<Dm>,<Dn>}
	 */
	private String getVFPQorDdmnRegs(int opcode) {
		int q = getBit(opcode, 6);
		String regs = TAB + getVFPQorDReg(opcode, q, 12, 22)
					  + ',' + getVFPQorDReg(opcode, q, 0, 5)
					  + ',' + getVFPQorDReg(opcode, q, 16, 7);
		return regs;
	}

	/**
	 * ARM--- ... D_22_22 ... Vn_19_16 Vd___15_12 ... N___7_7 Q___6_6 M___5_5 . Vm___3_0<br>
	 * Thumb2 ... D_1_6_6 ... Vn_1_3_0 Vd_0_15_12 ... N_0_7_7 Q_0_6_6 M_0_5_5 . Vm_0_3_0
	 * @param opcode
	 * @return String containing a TAB + proper Q-or-D _d,_n,_m register operands
	 * <p><listing>
	 * A8.6.266 VABA, VABAL
	 *	{@literal vaba<c>.<dt>           <Qd>,<Qn>,<Qm>	vaba<c>.<dt>           <Dd>,<Dn>,<Dm>}
	 * A8.6.267 VABD, VABDL (integer)
	 *	{@literal vabd<c>.<dt>           <Qd>,<Qn>,<Qm>	vabd<c>.<dt>           <Dd>,<Dn>,<Dm>}
	 * A8.6.268 VABD (floating-point)
	 *	{@literal vabd<c>.f32            <Qd>,<Qn>,<Qm>	vabd<c>.f32            <Dd>,<Dn>,<Dm>}
	 * A8.6.282 VCGE (register)
	 *	{@literal vcge<c>.<dt>           <Qd>,<Qn>,<Qm>	vcge<c>.<dt>           <Dd>,<Dn>,<Dm>}
	 * A8.6.272 VADD (floating-point)
	 *	{@literal vadd<c>.f32            <Qd>,<Qn>,<Qm>	vadd<c>.f32            <Dd>,<Dn>,<Dm>}
	 * A8.6.276 VAND (register)
	 *	{@literal vand<c>                <Qd>,<Qn>,<Qm>	vand<c>                <Dd>,<Dn>,<Dm>}
	 * A8.6.278 VBIC (register)
	 *	{@literal vbic<c>                <Qd>,<Qn>,<Qm>	vbic<c>                <Dd>,<Dn>,<Dm>}
	 * A8.6.280 VCEQ (register)
	 *	{@literal vceq<c>.f32            <Qd>,<Qn>,<Qm>	vceq<c>.f32            <Dd>,<Dn>,<Dm>}
	 * A8.6.282 VCGE (register)
	 *	{@literal vcge<c>.f32            <Qd>,<Qn>,<Qm>	vcge<c>.f32            <Dd>,<Dn>,<Dm>}
	 * A8.6.284 VCGT (register)
	 *	{@literal vcgt<c>.f32            <Qd>,<Qn>,<Qm>	vcgt<c>.f32            <Dd>,<Dn>,<Dm>}
	 * A8.6.284 VCGT (register)
	 *	{@literal vcgt<c>.<dt>           <Qd>,<Qn>,<Qm>	vcgt<c>.<dt>           <Dd>,<Dn>,<Dm>}
	 * A8.6.304 VEOR
	 *	{@literal veor                   <Qd>,<Qn>,<Qm>	veor<c>                <Dd>,<Dn>,<Dm>}
	 * A8.6.324 VMLA, VMLS (floating point)
	 *	{@literal vml<op><c>.f32         <Qd>,<Qn>,<Qm>	vml<op><c>.f32         <Dd>,<Dn>,<Dm>}
	 * A8.6.337 VMUL, VMULL (integer and polynomial)
	 *	{@literal vmul<c>.<dt>           <Qd>,<Qn>,<Qm>	vmul<c>.<dt>           <Dd>,<Dn>,<Dm>}
	 * A8.6.338 VMUL (floating-point)
	 *	{@literal vmul<c>.f32            <Qd>,<Qn>,<Qm>	vmul<c>.f32            <Dd>,<Dn>,<Dm>}
	 * A8.6.345 VORN (register)
	 *	{@literal vorn                   <Qd>,<Qn>,<Qm>	vorn<c>                <Dd>,<Dn>,<Dm>}
	 * A8.6.347 VORR (register)
	 *	{@literal vorr<c>                <Qd>,<Qn>,<Qm>	vorr<c>                <Dd>,<Dn>,<Dm>}
	 * A8.6.350 VPADD (floating-point)
	 *	{@literal UNDEFINED if q=='1'               	vpadd<c>.f32           <Dd>,<Dn>,<Dm>}
	 * A8.6.357 VQADD
	 *	{@literal vqadd<c>.<dt>          <Qd>,<Qn>,<Qm>	vqadd<c>.<dt>          <Dd>,<Dn>,<Dm>}
	 * A8.6.359 VQDMULH
	 *	{@literal vqdmulh<c>.<dt>        <Qd>,<Qn>,<Qm>	vqadd<c>.<dt>          <Dd>,<Dn>,<Dm>}
	 * A8.6.363 VQRDMULH
	 *	{@literal vqrdmulh<c>.<dt>       <Qd>,<Qn>,<Qm>	vqadd<c>.<dt>          <Dd>,<Dn>,<Dm>}
	 * A8.6.369 VQSUB
	 *	{@literal vqsub<c>.<type><size>  <Qd>,<Qn>,<Qm>	vqsub<c>.<type><size>  <Dd>,<Dn>,<Dm>}
	 * A8.6.374 VRHADD
	 *	{@literal vrhadd<c>              <Qd>,<Qn>,<Qm>	vrhadd<c> 	           <Dd>,<Dn>,<Dm>}
	 * A8.6.372 VRECPS
	 *	{@literal vrecps<c>.f32          <Qd>,<Qn>,<Qm>	vrecps<c>.f32          <Dd>,<Dn>,<Dm>}
	 * A8.6.379 VRSQRTS
	 *	{@literal vrsqrts<c>.f32         <Qd>,<Qn>,<Qm>	vrsqrts<c>.f32         <Dd>,<Dn>,<Dm>}
	 * A8.6.402 VSUB (floating-point)
	 *	{@literal vsub<c>.f32            <Qd>,<Qn>,<Qm>	vsub<c>.f32            <Dd>,<Dn>,<Dm>}
	 */
	private String getVFPQorDdnmRegs(int opcode) {
		int q = getBit(opcode, 6);
		String regs = TAB + getVFPQorDReg(opcode, q, 12, 22)
					  + ',' + getVFPQorDReg(opcode, q, 16, 7)
					  + ',' + getVFPQorDReg(opcode, q, 0, 5);
		return regs;
	}

	/**
	 * ARM--- ... D_22_22 size_21_20 Vn_19_16 Vd___15_12 ... N___7_7 . M___5_5 . Vm___3_0<br>
	 * Thumb2 ... D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 ... N_0_7_7 . M_0_5_5 . Vm_0_3_0
	 * @param opcode
	 * @param dType whether to use Qd or Dd register type
	 * @param nType whether to use Qn or Dn register type
	 * @return String containing "16" or "32", + TAB + proper Q-or-D _d,_n,Dm[x] register operands
	 * <p><listing>
	 * A8.6.358 VQDMLAL, VQDMLSL
	 *	{@literal vqd<op><c>.<dt>  <Qd>,<Dn>,<Dm[x]>}
	 * A8.6.359 VQDMULH
	 *	{@literal vqdmulh<c>.<dt>  <Qd>,<Qn>,<Dm[x]>	vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>}
	 * A8.6.363 VQRDMULH
	 *	{@literal vqrdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>	vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>}
	 */
	private String getVFPScalarOperands(int opcode, int dType, int nType) {
		int index = getBit(opcode, 5);
		int mR = opcode & 0xf;
		int size = opcode >> 20 & 3;
		String postfix;
		if (size == 1) {
			postfix = "16";
			mR &= 7;	// probably not necessary
			index = index << 1 | getBit(opcode, 3);
		} else {	// size == 2 ; if size == 0 then UNDEFINED
			postfix = "32";
		}
		return postfix + TAB + getVFPQorDReg(opcode, dType, 12, 22)
				+ ',' + getVFPQorDReg(opcode, nType, 16, 7)
				+ ",d" + mR + '[' + index + ']';
	}
	
	/**
	 *  ARM--- ... D_22_22 . . size_19_18 . . Vd___15_12 ... Q___6_6 M___5_5 . Vm___3_0<br>
	 *  Thumb2 ... D_1_6_6 . . size_1_3_2 . . Vd_0_15_12 ... Q_0_6_6 M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @param bitIorF location of the f-flag designating float-or-integer
	 *  @param bitSorU location of the u-flag designating unsigned-or-signed integer
	 *  @return String containing the data-type + TAB + proper Q-or-D _d,_m reg operands
	 * <p><listing>
	 * A8.6.269 VABS
	 *	{@literal vabs<c>.<dt>    <Qd>,<Qm>	vabs<c>.<dt>    <Dd>,<Dm>}
	 * A8.6.288 VCLS
	 *	{@literal vcls<c>.<dt>    <Qd>,<Qm>	vcls<c>.<dt>    <Dd>,<Dm>}
	 * A8.6.342 VNEG
	 *	{@literal vneg<c>.<dt>    <Qd>,<Qm>	vneg<c>.<dt>    <Dd>,<Dm>}
	 * A8.6.348 VPADAL
	 *	{@literal vpadal<c>.<dt>  <Qd>,<Qm>	vpadal<c>.<dt>  <Dd>,<Dm>}
	 * A8.6.351 VPADDL
	 *	{@literal vpaddl<c>.<dt>  <Qd>,<Qm>	vpaddl<c>.<dt>  <Dd>,<Dm>}
	 * A8.6.356 VQABS
	 *	{@literal vqabs<c>.<dt>   <Qd>,<Qm>	vqabs<c>.<dt>   <Dd>,<Dm>}
	 * A8.6.362 VQNEG
	 *	{@literal vqneg<c>.<dt>   <Qd>,<Qm>	vqneg<c>.<dt>   <Dd>,<Dm>}
	 * A8.6.371 VRECPE
	 *	{@literal vrecpe<c>.<dt>  <Qd>,<Qm>	vrecpe<c>.<dt>  <Dd>,<Dm>}
	 * A8.6.378 VRSQRTE
	 *	{@literal vrsqrte<c>.<dt> <Qd>,<Qm>	vrsqrte<c>.<dt> <Dd>,<Dm>}
	 * A8.6.283 VCGE (immediate #0)
	 *	{@literal vcge<c>.<dt> <Qd>,<Qm>,#0	vcge<c>.<dt> <Dd>,<Dm>,#0}
	 * A8.6.285 VCGT (immediate #0)
	 *	{@literal vcgt<c>.<dt> <Qd>,<Qm>,#0	vcgt<c>.<dt> <Dd>,<Dm>,#0}
	 * A8.6.287 VCLE (immediate #0)
	 *	{@literal vcle<c>.<dt> <Qd>,<Qm>,#0	vcle<c>.<dt> <Dd>,<Dm>,#0}
	 * A8.6.290 VCLT (immediate #0)
	 *	{@literal vclt<c>.<dt> <Qd>,<Qm>,#0	vclt<c>.<dt> <Dd>,<Dm>,#0}
	 */
	private String getVFPSorUorFQorDdmOperands(int opcode, int bitIorF, int bitSorU) {
		return getVFPSorUorFDataType(opcode, 18, bitIorF, bitSorU) + TAB + getVFPQorDdmRegs(opcode);
	}	
	
	/**
	 *  ARM--- ... D_22_22 . . size_19_18 . . Vd___15_12 ... Q___6_6 M___5_5 . Vm___3_0<br>
	 *  Thumb2 ... D_1_6_6 . . size_1_3_2 . . Vd_0_15_12 ... Q_0_6_6 M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @param bitIorF location of the f-flag designating integer-or-float
	 *  @return String containing the data-type + TAB + proper Q-or-D _d,_m reg operands
	 * <p><listing>
	 * A8.6.291 VCLZ
	 *	{@literal vclz<c>.<dt> <Qd>,<Qm>	vclz<c>.<dt> <Dd>,<Dm>}
	 * A8.6.283 VCGE (immediate #0)
	 *	{@literal vcge<c>.<dt> <Qd>,<Qm>,#0	vcge<c>.<dt> <Dd>,<Dm>,#0}
	 * A8.6.285 VCGT (immediate #0)
	 *	{@literal vcgt<c>.<dt> <Qd>,<Qm>,#0	vcgt<c>.<dt> <Dd>,<Dm>,#0}
	 * A8.6.287 VCLE (immediate #0)
	 *	{@literal vcle<c>.<dt> <Qd>,<Qm>,#0	vcle<c>.<dt> <Dd>,<Dm>,#0}
	 * A8.6.290 VCLT (immediate #0)
	 *	{@literal vclt<c>.<dt> <Qd>,<Qm>,#0	vclt<c>.<dt> <Dd>,<Dm>,#0}
	 */
	private String getVFPIorFQorDdmOperands(int opcode, int bitIorF) {
		return getVFPIorFDataType(opcode, 18, bitIorF) + TAB + getVFPQorDdmRegs(opcode);
	}
	
	/**
	 * @param szbit bit containing the sz determining ".f64" or ".f32"
	 * @return String containing ".f64" or ".f32"
	 */
	private String getVFPSzF64F32Type(int szbit) {
		return szbit == 1 ? ".f64" : ".f32";
	}

	/**
 	 * ARM--- ... D_22_22 ... Vd___15_12 ... sz___8_8 . . M___5_5 . Vm___3_0<br>
 	 * Thumb2 ... D_1_6_6 ... Vd_0_15_12 ... sz_0_8_8 . . M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String containing ".f64" or ".f32" + TAB + proper D-or-S _d,_m reg operands
	 * <p><listing>
	 * A8.6.269 VABS
	 *	{@literal vabs<c>.f64    <Dd>,<Dm>	vabs<c>.f32    <Sd>,<Sm>}
	 * A8.6.327 VMOV (register)
	 *	{@literal vmov<c>.f64    <Dd>,<Dm>	vmov<c>.f32    <Sd>,<Sm>}
	 * A8.6.342 VNEG
	 *	{@literal vneg<c>.f64    <Dd>,<Dm>	vneg<c>.f32    <Sd>,<Sm>}
	 * A8.6.388 VSQRT
	 *	{@literal vsqrt<c>.f64   <Dd>,<Dm>	vsqrt<c>.f32   <Sd>,<Sm>}
	 * A8.6.292 VCMP, VCMPE
	 *	vcmp{e}{@literal <c>.f64 <Dd>,<Dm>}	vcmp{e}{@literal <c>.f32 <Sd>,<Sm>}
	 */
	private String getVFPSzF64F32dmOperands(int opcode) {
		int sz = getBit(opcode, 8);
		return getVFPSzF64F32Type(sz)
				 + TAB + getVFPDorSReg(opcode, sz, 12, 22)
				 + ',' + getVFPDorSReg(opcode, sz, 0, 5);
	}

	/**
 	 * ARM--- ... D_22_22 . . Vn_19_16 Vd___15_12 ... sz___8_8 N___7_7 . M___5_5 . Vm___3_0<br>
 	 * Thumb2 ... D_1_6_6 . . Vn_1_3_0 Vd_0_15_12 ... sz_0_8_8 N_0_7_7 . M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String containing ".f64" or ".f32" + TAB + proper D-or-S _d,_n,_m reg operands
	 * <p><listing>
	 * A8.6.343 VNMLA, VNMLS, VNMUL
	 *	{@literal vnmla<c>.f64   <Dd>,<Dn>,<Dm>	vnmla<c>.f32   <Sd>,<Sn>,<Sm>}
	 *	{@literal vnmls<c>.f64   <Dd>,<Dn>,<Dm>	vnmls<c>.f32   <Sd>,<Sn>,<Sm>}
	 * A8.6.272 VADD (floating-point)
	 *	{@literal vadd<c>.f64    <Dd>,<Dn>,<Dm>	vadd<c>.f32    <Sd>,<Sn>,<Sm>}
	 * A8.6.301 VDIV
	 *	{@literal vdiv<c>.f64    <Dd>,<Dn>,<Dm>	vdiv<c>.f32    <Sd>,<Sn>,<Sm>} 
	 * A8.6.324 VMLA, VMLS (floating-point)
	 *	{@literal vml<op><c>.f64 <Dd>,<Dn>,<Dm>	vml<op><c>.f32 <Sd>,<Sn>,<Sm>}
	 * A8.6.338 VMUL (floating-point)
	 *	{@literal vmul<c>.f64    <Dd>,<Dn>,<Dm>	vmul<c>.f32    <Sd>,<Sn>,<Sm>}
	 * A8.6.343 VNMLA, VNMLS, VNMUL
	 *	{@literal vnmul<c>.f64   <Dd>,<Dn>,<Dm>	vnmul<c>.f32   <Sd>,<Sn>,<Sm>}
	 * A8.6.402 VSUB (floating-point)
	 *	{@literal vsub<c>.f64    <Dd>,<Dn>,<Dm>	vsub<c>.f32    <Sd>,<Sn>,<Sm>} 
	 */
	private String getVFPSzF64F32dnmOperands(int opcode) {  
		int sz = getBit(opcode, 8);
		return getVFPSzF64F32Type(sz)
				 + TAB + getVFPDorSReg(opcode, sz, 12, 22)
				 + ',' + getVFPDorSReg(opcode, sz, 16, 7)
				 + ',' + getVFPDorSReg(opcode, sz, 0, 5);
	}

	/**
 	 * ARM--- ... D_22_22 . . sz_19_18 . . Vd___15_12 ... Q___6_6 M___5_5 . Vm___3_0<br>
 	 * Thumb2 ... D_1_6_6 . . sz_1_3_2 . . Vd_0_15_12 ... Q_0_6_6 M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String containing ".8", ".16" or ".32" + TAB + proper Q-or-D _d,_m reg operands
	 * <p><listing>
	 * A8.6.407 VTRN
	 *	{@literal vtrn<c>.<size> <Qd>,<Qm>	vtrn<c>.<size> <Dd>,<Dm>} 
	 * A8.6.409 VUZP
	 *	{@literal vuzp<c>.<size> <Qd>,<Qm>	vuzp<c>.<size> <Dd>,<Dm>}
	 * A8.6.410 VZIP
	 *	{@literal vzip<c>.<size> <Qd>,<Qm>	vzip<c>.<size> <Dd>,<Dm>}
	 */
	private String getVFPSzQorDdmOperands(int opcode) {
		return "." + getVFPDataTypeSize(opcode, 18) + getVFPQorDdmRegs(opcode);
	}

	/**
 	 * ARM--- ... D_22_22 sz_21_20 Vn_19_16 Vd___15_12 ... N___7_7 Q___6_6 M___5_5 . Vm___3_0<br>
 	 * Thumb2 ... D_1_6_6 sz_1_5_4 Vn_1_3_0 Vd_0_15_12 ... N_0_7_7 Q_0_6_6 M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String containing ".8" ".16" or ".32" + TAB + proper Q-or-D _d,_n,_m reg operands
	 *  <p><listing>
	 * A8.6.408 VTST
	 *	{@literal vtst<c>.<size> <Qd>,<Qn>,<Qm>	vtst<c>.<size> <Dd>,<Dn>,<Dm>} 
	 */
	private String getVFPSzQorDdnmOperands(int opcode) {
		return "." + getVFPDataTypeSize(opcode, 20) + getVFPQorDdnmRegs(opcode);
	}

	/**
	 * ARM--- ... D_22_22 op_21_21 sz_20_20 Vn_19_16 Vd___15_12 ... N___7_7 Q___6_6 M___5_5 1 Vm___3_0<br>
	 * Thumb2 ... D_1_6_6 op_1_5_5 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 ... N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
	 * @param opcode
	 * @return "ge" or "gt" + "f32" + TAB + proper Q-or-D _d,_n,_m register operands
	 * <p><listing>
	 * A8.6.270 VACGE, VACGT, VACLE, VACLT
	 *	{@literal vacge<c>.f32 <Qd>,<Qn>,<Qm>	vacge<c>.f32 <Dd>,<Dn>,<Dm>}
	 *	{@literal vacgt<c>.f32 <Qd>,<Qn>,<Qm>	vacgt<c>.f32 <Dd>,<Dn>,<Dm>}  
	 */
	private String getVFP_vacge_vacgt(int opcode) {
		return (isBitEnabled(opcode, 21) ? "gt.f32" : "ge.f32")
				+ getVFPQorDdnmRegs(opcode);
	}

	/**
	 *	ARM--- ... D_22_22 op_21_20 Vn_19_16 Vd___15_12 ... N___7_7 Q___6_6 M___5_5 1 Vm___3_0<br>
	 *	Thumb2 ... D_1_6_6 op_1_5_4 Vn_1_3_0 Vd_0_15_12 ... N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
	 * <p><listing>
	 *  @param opcode
	 *  @return String containing the full mnemonic for this opcode
	 */
	private String getVFP_vbif_vbit_vbsl_veor_mnemonic(int opcode) {
		switch ((opcode >> 20) & 3) {
			case 0:	return "veor";
			case 1: return "vbsl";
			case 2:	return "vbit";
			case 3:	return "vbif";
		}
		return "v";	// should never get here
	}

	/**
	 * ARM--- ... D_22_22 ... Vd___15_12 ... sz___8_8 E___7_7 ...<br> 
	 * Thumb2 ... D_1_6_6 ... Vd_0_15_12 ... sz_0_8_8 E_0_7_7 ... 
	 *  @param opcode
	 *  @return String containing ".f32" or ".f64" + TAB + proper D-or-S _d reg operand + ",0.0"
	 * <p><listing>
	 * A8.6.292 VCMP, VCMPE
	 *	vcmp{e}{@literal <c>.f64 <Dd>},#0.0	vcmp{e}{@literal <c>.f32 <Sd>},#0.0
	 */
	private String getVFP_vcmpTo0Operands(int opcode) {
		int sz = getBit(opcode, 8);
		return getVFPSzF64F32Type(sz)
				+ TAB + getVFPDorSReg(opcode, sz, 12, 22) + ",#0.0";
	}

	/**
	 * ARM--- ... D_22_22 ... Vd___15_12 ... sz___8_8 . . M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 ... Vd_0_15_12 ... sz_0_8_8 . . M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String containing proper mnemonic postfix + TAB + proper D-or-S _d,_m reg operands
	 * <p><listing>
	 * A8.6.298 VCVT (between double-precision and single-precision)
	 *	{@literal vcvt<c>.f64.f32 <Dd>,<Sm>}
	 *	{@literal vcvt<c>.f32.f64 <Sd>,<Dm>}
	 */
	private String getVFP_vcvtDpSpOperands(int opcode) {
		int sz = getBit(opcode, 8);
		return (sz == 1 ? ".f32.f64" : ".f64.f32")
				+ TAB + getVFPDorSReg(opcode, 1-sz, 12, 22)
				+ ',' + getVFPDorSReg(opcode, sz, 0, 5);
	}

	/**
	 * ARM--- ... D_22_22 ... op_18_18 . U_16_16 Vd___15_12 ... sf___8_8 sx___7_7 . i___5_5 . imm4___3_0<br> 
	 * Thumb2 ... D_1_6_6 ... op_1_2_2 . U_1_0_0 Vd_0_15_12 ... sf_0_8_8 sx_0_7_7 . i_0_5_5 . imm4_0_3_0 
	 *  @param opcode
	 *  @return String containing proper mnemonic postfix + TAB + proper D-or-S _d,_d reg operands
	 * <p><listing>
	 * A8.6.297 VCVT (between floating-point and fixed-point, VFP)
	 *	{@literal vcvt<c>.<Td>.f64 <Dd>,<Dd>,#<fbits>}
	 *	{@literal vcvt<c>.<Td>.f32 <Sd>,<Sd>,#<fbits>}
	 *	{@literal vcvt<c>.f64.<Td> <Dd>,<Dd>,#<fbits>}
	 *	{@literal vcvt<c>.f32.<Td> <Sd>,<Sd>,#<fbits>}
	 */
	private String getVFP_vcvtFpFixRegOperands(int opcode) {
		String dreg = getVFPDorSReg(opcode, getBit(opcode, 8), 12, 22);
		int imm = ((opcode & 0xF) << 1) | getBit(opcode, 5);
		if (isBitEnabled(opcode, 7))
			imm = 32 - imm;
		else
			imm = 16 - imm;
		return getVFPVcvtType(opcode) + TAB + dreg + ',' + dreg
				+ ",#" + imm;
	}

	/**
	 * ARM--- ... D_22_22 imm6_21_16 Vd___15_12 ... op___8_8 0 Q___6_6 M___5_5 1 Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 imm6_1_5_0 Vd_0_15_12 ... op_0_8_8 0 Q_0_6_6 M_0_5_5 1 Vm_0_3_0 
	 *  @param opcode
	 *  @param uBit bit in the opcode containing the u (unsigned) bit
	 *  @return String containing proper mnemonic postfix + TAB + proper Q-or-D _d,_m reg operands
	 * <p><listing>
	 * A8.6.296 VCVT (between floating-point and fixed-point, Advanced SIMD)
	 *	{@literal vcvt<c>.<Td>.<Tm>	<Qd>,<Qm>,#<fbits>}
	 *	{@literal vcvt<c>.<Td>.<Tm>	<Dd>,<Dm>,#<fbits>}
	 */
	private String getVFP_vcvtFpFixVecOperands(int opcode, int uBit) { 
		return getVFPTdTm2(opcode, 8, uBit) + getVFPQorDdmRegs(opcode)
				+ ",#" + (opcode >> 16 & 0x3f);
	}

	/**
	 * ARM--- ... D_22_22 . . size_19_18 1 1 Vd___15_12 ... op___8_7 Q___6_6 M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 . . size_1_3_2 1 1 Vd_0_15_12 ... op_0_8_7 Q_0_6_6 M_0_5_5 . Vm_0_3_0 
	 *  @param opcode
	 *  @return String containing proper Td.Tm combo + TAB + proper Q-or-D _d,_m reg operands
	 * <p><listing>
	 * A8.6.294 VCVT (between floating-point and integer, Advanced SIMD)
	 *	{@literal vcvt<c>.<Td>.<Tm>	<Qd>,<Qm>}
	 *	{@literal vcvt<c>.<Td>.<Tm>	<Dd>,<Dm>}
	 */
	private String getVFP_vcvtFpIVecOperands(int opcode) {
		return getVFPTdTm(opcode, 7, 18) + getVFPQorDdmRegs(opcode);
	}

	/**
	 * ARM--- ... D_22_22 ... opc2_18_16 Vd___15_12 ... sz___8_8 op___7_7 . M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 ... opc2_1_2_0 Vd_0_15_12 ... sz_0_8_8 op_0_7_7 . M_0_5_5 . Vm_0_3_0 
	 *  @param opcode
	 *  @return String containing proper mnemonic postfix + TAB + proper D-or-S _d,_m reg operands
	 * <p><listing>
	 * A8.6.295 VCVT, VCVTR (between floating-point and integer, VFP)
	 *	vcvt{r}{@literal <c>.s32.f64 <Sd>,<Dm>}
	 *	vcvt{r}{@literal <c>.s32.f32 <Sd>,<Sm>}
	 *	vcvt{r}{@literal <c>.u32.f64 <Sd>,<Dm>}
	 *	vcvt{r}{@literal <c>.u32.f32 <Sd>,<Sm>}
	 *	{@literal vcvt<c>.f64.<Tm>   <Dd>,<Sm>}
	 *	{@literal vcvt<c>.f32.<Tm>   <Sd>,<Sm>}
	 */
	private String getVFP_vcvtFpIRegOperands(int opcode) {
		int sz = getBit(opcode, 8);
		String ops = getVFPTdTm3(opcode, 16, 7, sz);
		int dD = (((opcode & 0x70000) == 0) && (sz == 1)) ? 1 : 0;
		int mD = (((opcode & 0x70000) == 0) && (sz == 1)) ? 0 : sz;
		ops += TAB + getVFPDorSReg(opcode, dD, 12, 22) + ',' + getVFPDorSReg(opcode, mD, 0, 5);
		return ops;
	}

	/**
	 * ARM--- ... D_22_22 ... op_16_16 Vd___15_12 ... T___7_7 . M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 ... op_1_0_0 Vd_0_15_12 ... T_0_7_7 . M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String containing proper mnemonic postfix + TAB + proper Sd,Sm reg operands
	 * <p><listing>
	 * A8.6.300 VCVTB, VCVTT (between half-precision and single-precision, VFP)
	 *	{@literal vcvt<y><c>.f32.f16 <Sd>, <Sm>}
	 *	{@literal vcvt<y><c>.f16.f32 <Sd>, <Sm>}
	 */
	private String getVFP_vcvtHpSpRegOperands(int opcode) {
		return (isBitEnabled(opcode, 16) ? ".f16.f32": ".f32.f16")
				+ TAB + getVFPDorSReg(opcode, 0, 12, 22)
				+ ',' + getVFPDorSReg(opcode, 0, 0, 5);
	}

	/**
	 * ARM--- ... D_22_22 . . size_19_18 . . Vd___15_12 ... op___8_8 . . M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 . . size_1_3_2 ... Vd_0_15_12 . . op_0_8_8 . . M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String containing proper mnemonic postfix + TAB + proper Q-or-D _d,_m reg operands
	 * <p><listing>
	 * A8.6.299 VCVT (between half-precision and single-precision, Advanced SIMD)
	 *	{@literal vcvt<c>.f32.f16 <Qd>, <Dm>}
	 *	{@literal vcvt<c>.f16.f32 <Dd>, <Qm>}
	 */
	private String getVFP_vcvtHpSpVecOperands(int opcode) {
		int sz = getBit(opcode, 8);
		return (sz == 1 ? ".f32.f16" : ".f16.f32")
				+ TAB + getVFPQorDReg(opcode, sz, 12, 22)
				+ ',' + getVFPQorDReg(opcode, 1-sz, 0, 5);
	}

	/**
	 * ARM--- ... b_22_22 Q_21_21 . Vd_19_16 Rt___15_12 ... D___7_7 . e___5_5 ...<br> 
	 * Thumb2 ... b_1_6_6 Q_1_5_5 . Vd_1_3_0 Rt_0_15_12 ... D_0_7_7 . e_0_5_5 ...
	 *  @param opcode
	 *  @return String containing proper mnemonic postfix + TAB + proper Q-or-D _d,Rt reg operands
	 * <p><listing>
	 * A8.6.303 VDUP (ARM core register)
	 *	{@literal vdup<c>.<size> <Qd>, <Rt>}
	 *	{@literal vdup<c>.<size> <Dd>, <Rt>}
	 */
	private String getVFP_vdupRegOperands(int opcode) {
		int be = getBit(opcode, 22) << 1 | getBit(opcode, 5);
		String postfix;
		switch (be) {
		case 0:		postfix = ".32";	break;
		case 1:		postfix = ".16";	break;
		case 2:		postfix = ".8";		break;
		default:	postfix = ".UNDEFINED";
		}
		return postfix + TAB + getVFPQorDReg(opcode, getBit(opcode, 21), 16, 7) + "," + getR_12(opcode);
	}

	/**
	 * ARM--- ... D_22_22 . . imm4_19_16 Vd___15_12 ... Q___6_6 M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 . . imm4_1_3_0 Vd_0_15_12 ... Q_0_6_6 M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String containing proper mnemonic postfix + TAB + proper Sd,Sm reg operands
	 * <p><listing>
	 * A8.6.302 VDUP (scalar)
	 *	{@literal vdup<c>.<size> <Qd>, <Dm[x]>}
	 *	{@literal vdup<c>.<size> <Dd>, <Dm[x]>}
	 */
	private String getVFP_vdupScalarOperands(int opcode) {
		int x, q = getBit(opcode, 6), imm = (opcode >> 16) & 0xf;
		String postfix;
		if ((imm & 1) != 0) {
			postfix = ".8";
			x = imm >> 1;
		} else if ((imm & 2) != 0) {
			postfix = ".16";
			x = imm >> 2;	
		} else {
			postfix = ".32";
			x = imm >> 3;
		}
		return postfix
				+ TAB + getVFPQorDReg(opcode, q, 12, 22)
				+ ',' + getVFPQorDReg(opcode, 0, 0, 5)
				+ '[' + x + ']';
	}

	/**
	 * ARM--- ... D_22_22 size_21_20 Vn_19_16 Vd___15_12 0 op___10_10 L___9_9 . N___7_7 1 M___5_5 0 Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 op_0_10_10 L_0_9_9 . N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
	 *  @param opcode
	 *  @param quBit shift position of q-bit/u-bit in instruction
	 *  @return String containing mnemonic postfix + TAB + proper Q-or-D _d,_n,<Dm[x]> reg operands
	 * <p><listing>
	 * A8.6.325 VMLA, VMLAL, VMLS, VMLSL (by scalar)
	 *	{@literal v<op><c>.<dt>  <Qd>,<Qn>,<Dm[x]>	v<op><c>.<dt> <Dd>,<Dn>,<Dm[x]>}
	 *	{@literal v<op>l<c>.<dt> <Qd>,<Dn>,<Dm[x]>}
	 * A8.6.339 VMUL, VMULL (by scalar)
	 *	{@literal vmul<c>.<dt>   <Qd>,<Qn>,<Dm[x]>	vmul<c>.<dt> <Dd>,<Dn>,<Dm[x]>}
	 *	{@literal vmull<c>.<dt>  <Qd>,<Dn>,<Dm[x]>}
	 * A8.6.360 VQDMULL
	 *	{@literal vqdmull<c>.<dt> <Qd>,<Dn>,<Dm[x]>}
	 */
	private String getVFP_vmXXScalar(int opcode, int quBit) {
		int dR, nR, qu = getBit(opcode, quBit);
		String postfix;

		if (isBitEnabled(opcode, 9)) {
			postfix = "l." + ((qu == 0) ? 's' : 'u');
			dR = 1;
			nR = 0;
		} else {
			postfix = "." + (isBitEnabled(opcode, 8) ? 'f' : 'i');
			dR = nR = qu;
		}

		return postfix + getVFPScalarOperands(opcode, dR, nR);
	}

	/**
	 * ARM--- ... opc1_22_21 0 Vd_19_16 Rt___15_12 ... D___7_7 opc2___6_5<br>
	 * Thumb2 ... opc1_1_6_5 0 Vd_1_3_0 Rt_0_15_12 ... D_0_7_7 opc2_0_6_5
	 *  @param opcode
	 *  @return String containing mnenomic type postfix + TAB + proper Dd[x],Rt reg operands
	 * A8.6.328 VMOV (ARM core register to scalar)
	 *	{@literal vmov<c>.<size> <Dd[x]>, <Rt>}
	 */
	private String getVFP_vmovArmCoreRegToScalar(int opcode) {
		int opc1 = opcode >> 21 & 3;
		int opc2 = opcode >>  5 & 3;
		int index = 0;
		String ops = ".";

		if (isBitEnabled(opc1, 1)) {
			ops += '8';
			index = getBit(opc1, 0) << 2 | opc2;
		} else if (isBitEnabled(opc2, 0)) {
			ops += "16";
			index = getBit(opc1, 0) << 1 | getBit(opc2, 1);
		} else if (opc2 == 0) {
			ops += "32";
			index = getBit(opc1, 0);
		}

		ops += TAB + getVFPDorSReg(opcode, 1, 16, 7)
			+  '[' + index + "]," + getR_12(opcode);

		return ops;
	}

	/**
	 * ARM--- ... op_20_20 Vn_19_16 Rt___15_12 ... N___7_7<br>
	 * Thumb2 ... op_1_4_4 Vn_1_3_0 Rt_0_15_12 ... N_0_7_7
	 *  @param opcode
	 *  @return String TAB + proper Sn,Rt or Rt,Sn operands
	 * A8.6.330 VMOV (between ARM core register and single-precision register)
	 *	{@literal vmov<c> <Sn>, <Rt>	vmov<c> <Rt>, <Sn>}
	 */
	private String getVFP_vmovBetweenArmCoreAndSinglePrecReg(int opcode) {
		String sn = getVFPDorSReg(opcode, 0, 16, 7);
		String rt = getR_12(opcode);
		return TAB + (isBitEnabled(opcode, 20) ? rt + ',' + sn : sn + ',' + rt);
	}

	/**
	 * ARM--- ... op_20_20 Rt2_19_16 Rt___15_12 ... M___5_5 . Vm___3_0<br>
	 * Thumb2 ... op_1_4_4 Rt2_1_3_0 Rt_0_15_12 ... M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String TAB + proper Dm,Rt,Rt2 or Rt,Rt2,Dm reg operands
	 * <p><listing>
	 * A8.6.332 VMOV (between two ARM core registers and a doubleword extension register)
	 *	{@literal vmov<c> <Dm>,<Rt>,<Rt2>	vmov<c> <Rt>,<Rt2>,<Dm>}
	 */
	private String getVFP_vmovBetween2ArmCoreAnd1DoublewordExtensionRegs(int opcode) {
		String dm = getVFPDorSReg(opcode, 1, 0, 5);
		String armCoreRegs = getR_12(opcode) + ',' + getR_16(opcode);
		if (isBitEnabled(opcode, 20))
			return TAB + armCoreRegs + ',' + dm;
		else
			return TAB + dm + ',' + armCoreRegs;
	}

	/**
	 * ARM--- ... op_20_20 Rt2_19_16 Rt___15_12 ... M___5_5 . Vm___3_0<br>
	 * Thumb2 ... op_1_4_4 Rt2_1_3_0 Rt_0_15_12 ... M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String TAB + proper Sn,Sn+1,Rt,Rt2 or Rt,Rt2,Sn,Sn+1 reg operands
	 * <p><listing>
	 * A8.6.331 VMOV (between two ARM core registers and two single-precision registers)
	 *	{@literal vmov<c> <Sm>,<Sm1>,<Rt>,<Rt2>	vmov<c> <Rt>,<Rt2>,<Sm>,<Sm1>}
	 */
	private String getVFP_vmovBetween2ArmCoreAndSinglePrecRegs(int opcode) {
		int vm = (opcode & 0xf) << 1 | getBit(opcode, 5);
		String armCoreRegs = getR_12(opcode) + ',' + getR_16(opcode);
		String singPrecRegs = "s" + vm + ",s" + (vm + 1);
		if (isBitEnabled(opcode, 20))
			return TAB + armCoreRegs + ',' + singPrecRegs;
		else
			return TAB + singPrecRegs + ',' + armCoreRegs;
	}

	/**
	 * ARM--- ... opc1_22_21 0 Vd_19_16 Rt___15_12 ... D___7_7 opc2___6_5<br>
	 * Thumb2 ... opc1_1_6_5 0 Vd_1_3_0 Rt_0_15_12 ... D_0_7_7 opc2_0_6_5
	 *  @param opcode
	 *  @return String containing mnenomic type postfix + TAB + proper Dd[x],Rt reg operands
	 * <p><listing>
	 * A8.6.329 VMOV (scalar to ARM core register)
	 *  {@literal vmov<c>.<dt> <Rt>, <Dn[x]>}
	 */
	private String getVFP_vmovScalarToArmCoreReg(int opcode) {
		int opc1 = opcode >> 21 & 3;
		int opc2 = opcode >> 5 & 3;

		boolean u = isBitEnabled(opcode, 23);

		String ops = ".";

		int index = 0;
		if (isBitEnabled(opc1, 1)) {
			ops += u ? "u8" : "s8";
			index = getBit(opc1, 0) << 2 | opc2;
		} else if (isBitEnabled(opc2, 0)) {
			ops += u ? "u16" : "s16";
			index = getBit(opc1, 0) << 1 | getBit(opc2, 1);
		} else if (opc2 == 0) {
			ops += "32";
			index = getBit(opc1, 0);
		}
		ops += TAB + getR_12(opcode) + ',' + getVFPDorSReg(opcode, 1, 16, 7)
			+  '[' + index + ']';

		return ops;
	}

	private String getVFP_vmov_vbitwise_mnemonic(int opcode) {
		String mnemonic = "";
		// concatenate bit 5 op field with bits 8-11 cmode field
		int opCmode = ((opcode >> 1) & 0x10) | (opcode >> 8) & 0xf;
		
		// find the instruction mnemonic
		switch (opCmode) {
		case 0: case 2: case 4: case 6:
		case 8: case 10:
		case 12: case 13: case 14: case 15:
		case 30:
			mnemonic = "vmov";
			break;
		case  1: case  3: case  5: case  7:
		case  9: case 11:
			mnemonic = "vorr";
			break;
		case 16: case 18: case 20: case 22:
		case 24: case 26:
		case 28: case 29:
			mnemonic = "vmvn";
			break;
		case 17: case 19: case 21: case 23:
		case 25: case 27:
			mnemonic = "vbic";
			break;
		default:
			break;
		}
		
		return mnemonic + getVFPSize(opCmode);
	}

	/**
	 * ARM--- ... D_22_22 ... imm3_18_16 Vd___15_12 cmode___11_8 . Q___6_6 . . imm4___3_0<br> 
	 * Thumb2 ... D_1_6_6 ... imm3_1_2_0 Vd_0_15_12 cmode_0_11_8 . Q_0_6_6 . . imm4_0_3_0 
	 *  @param opcode
	 *  @param topIBit location of the I bit to place at the top of the imm8 to be constructed 
	 *  @return String containing the full mnemonic + TAB + proper Q-or-D _d,_m reg operands
	 * <p><listing>
	 * A8.6.277 VBIC (immediate)
	 *	{@literal vbic<c>.<dt> <Qd>,#<imm>	vbic<c>.<dt> <Dd>,#<imm>}
	 * A8.6.326 VMOV (immediate)
	 *	{@literal vmov<c>.<dt> <Qd>,#<imm>	vmov<c>.<dt> <Dd>,#<imm>}
	 * A8.6.340 VMVN (immediate)
	 *	{@literal vmvn<c>.<dt> <Qd>,#<imm>	vmvn<c>.<dt> <Dd>,#<imm>}
	 * A8.6.346 VORR (immediate)
	 *	{@literal vorr<c>.<dt> <Qd>,#<imm>	vorr<c>.<dt> <Dd>,#<imm>}
	 */
	private String getVFP_vmov_vbitwise_instruction(int opcode, int topIBit) {
		int imm = getBit(opcode, topIBit) << 7 | ((opcode >> 12) & 0x70) | (opcode & 0xf);
		return getVFP_vmov_vbitwise_mnemonic(opcode)
				+ TAB + getVFPQorDReg(opcode, getBit(opcode, 6), 12, 22)
				+ ",#" + getHexValue(imm);
	}

	/**
	 * ARM--- ... D_22_22 imm6_21_16 Vd___15_12 ... M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 imm6_1_5_0 Vd_0_15_12 ... M_0_5_5 . Vm_0_3_0 
	 *  @param opcode
	 *  @param uBit shift position of u-bit in instruction
	 *  @return String containing mnemonic data type post-fix + TAB + proper Q-or-D _d,_m reg operands
	 *  + imm value for vshll instruction
	 * <p><listing>
	 * A8.6.333 VMOVL
	 *	{@literal vmovl<c>.<dt> <Qd>,<Dm>}
	 * A8.6.384 VSHLL
	 *	{@literal vshll<c>.<type><size> <Qd>,<Dm>,#<imm> (0 < <imm> < <size>)}
	 */
	private String getVFP_vmovl_vshll_operands(int opcode, int uBit) {
		int imm6 = getVFPQImm6(opcode);
		return getVFPSorUDataType(opcode-(imm6<<16), uBit)
				+ TAB + getVFPQorDReg(opcode, 1, 12, 22)
				+ ',' + getVFPQorDReg(opcode, 0, 0, 5)
				+ (0 == imm6 ? "" : ",#" + imm6);
	}

	/**
	 * ARM--- ... D_22_22 ... Vd___15_12 ... imm___3_0<br> 
	 * Thumb2 ... D_1_6_6 ... Vd_0_15_12 ... imm_0_3_0 
	 *  @param opcode
	 *  @param uBit shift position of u-bit in instruction
	 *  @return String containing mnemonic data size post-fix + TAB + proper D-or-S <list> reg operands
	 * <p><listing>
	 * A8.6.354 VPOP
	 *	{@literal vpop  <list>	(<list> is consecutive 64-bit registers)}
	 *	{@literal vpop  <list>	(<list> is consecutive 32-bit registers)}
	 * A8.6.355 VPUSH
	 *	{@literal vpush <list>	(<list> is consecutive 64-bit registers)}
	 *	{@literal vpush <list>	(<list> is consecutive 32-bit registers)}
	 */
	private String getVFP_vpop_vpush_operands(int opcode) {
		boolean is64 = isBitEnabled(opcode, 8);
		return TAB + getVFPDorSRegList(opcode, is64);
	}

	/**
	 * ARM--- ... D_22_22 . . size_19_18 . . Vd___15_12 ... op___7_6 M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 . . size_1_3_2 . . Vd_0_15_12 ... op_0_7_6 M_0_5_4 . Vm_0_3_0 
	 * @param opcode
	 * @return String containing mnemonic size postfix + TAB + proper Dd,Qm reg operands 
	 * <p><listing>
	 * A8.6.361 VQMOVN
	 *	vld1{@literal vqmov{u}n<c>.<type><size> <Dd>, <Qm>}
	 */
	private String getVFP_vqmov_instruction(int opcode) {
		return getVFPQUNUorSType(opcode, 7, 6) + getVFPDataTypeSize((opcode >> 18 & 3)+1, 0)
				+ TAB + getVFPDdQmRegs(opcode);
	}

	/**
	 * ARM--- ... D_22_22 imm6_21_16 Vd_15_12 1 ... op___8_8 . . M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 imm6_1_5_0 Vd_0_15_12 ... op_0_8_8 . . M_0_5_5 . Vm_0_3_0 
	 *  @param opcode
	 *  @param uBit shift position of u-bit in instruction
	 *  @return String containing mnemonic postfix + TAB + proper Dd,Qm,#imm operands
	 * <p><listing>
	 * A8.6.367 VQSHL, VQSHLU (immediate)
	 *	vqshl{u}{@literal <c>.<type><size> <Qd>,<Qm>,#<imm>}	vqshl{u}{@literal <c>.<type><size> <Dd>,<Dm>,#<imm>}
	 */
	private String getVFP_vqshl_instruction(int opcode, int uBit) {
		int l = getBit(opcode, 7);
		int imm = l == 1 ? opcode >> 16 & 0x3f : getVFPQImm6(opcode);
		String typeSize = getVFPQUUorSType(opcode, l, uBit, 8);  
		return typeSize + TAB + getVFPQorDdmRegs(opcode) + ",#" + imm;
	}

	/**
	 * ARM--- ... D_22_22 imm6_21_16 Vd_15_12 1 ... op___8_8 . . M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 imm6_1_5_0 Vd_0_15_12 ... op_0_8_8 . . M_0_5_5 . Vm_0_3_0 
	 *  @param opcode
	 *  @return String containing mnemonic postfix + TAB + proper Dd,Qm,#imm operands
	 * <p><listing>
	 * A8.6.373 VREV16, VREV32, VREV64
	 * vrev<n><c>.<size> <Qd>, <Qm>	vrev<n><c>.<size> <Dd>, <Dm>
	 */
	private String getVFP_vrev_instruction(int opcode) {
		int op = opcode >> 7 & 3;
		int size = getVFPDataTypeSize(opcode, 18);
		return (64 >> op) + "." + size + TAB + getVFPQorDdmRegs(opcode);
	}

	/**
	 * ARM--- ... D_22_22 imm6_21_16 Vd_15_12 1 ... op___8_8 . . M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 imm6_1_5_0 Vd_0_15_12 ... op_0_8_8 . . M_0_5_5 . Vm_0_3_0 
	 *  @param opcode
	 *  @param uPos shift position of u-bit in instruction
	 *  @return String containing mnemonic postfix + TAB + proper Dd,Qm,#imm operands
	 * <p><listing>
	 * A8.6.365 VQRSHRN, VQRSHRUN
	 *	vqrshr{u}n{@literal <c>.<type><size> <Dd>,<Qm>,#<imm>}
	 * A8.6.368 VQSHRN, VQSHRUN
	 *	vqshr{u}{@literal n<c>.<type><size>  <Dd>,<Qm>,#<imm>}
	 */
	private String getVFP_vqXshr_instruction(int opcode, int uPos) {
		int imm = getVFPQImm6(opcode);
		String typeSize = ((isBitEnabled(opcode, uPos) && isBitEnabled(opcode, 8)) ? ".u" : ".s")
				+ getVFPImm6Size(opcode);
		return ((isBitEnabled(opcode, uPos) && !isBitEnabled(opcode, 8)) ? "un" : "n") + typeSize + TAB
				+ getVFPDdQmRegs(opcode) + ",#" + getVFPImm6SHRAdj(opcode, imm);
	}

	/**
	 * ARM--- ... D_1_6_6 imm6_1_5_0 Vd___15_12 ... L___7_7 Q___6_6 M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 imm6_1_5_0 Vd_0_15_12 ... L_0_7_7 Q_0_6_6 M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String containing TAB + proper Dd,{Dn1-Dnn},Dm reg operands
	 * <p><listing>
	 * A8.6.406 VTBL, VTBX
	 *	{@literal v<op><c>.8 <Dd>,<list>,<Dm>}
	 */
	private String getVFP_vtb_instruction(int opcode) {
		int reg = getBit(opcode, 7) << 4 | opcode	>> 16 & 0xf;
		int cnt = opcode >> 8 & 3;
		String ops = (isBitEnabled(opcode, 6) ? "x" : "l") + ".8\t"
					 + getVFPQorDReg(opcode, 0, 12, 22) + ",{d" + reg;
		for (int i = 1; i < cnt + 1; ++i) {
			ops += ",d" + (reg + i);
		}
		ops += "}," + getVFPQorDReg(opcode, 0, 0, 5);
		
		return ops;
	}

	/**
	 * ARM--- ... D_1_6_6 imm6_1_5_0 Vd___15_12 ... L___7_7 Q___6_6 M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 imm6_1_5_0 Vd_0_15_12 ... L_0_7_7 Q_0_6_6 M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @param encoded whether immediate is in the opcode or must be derived 
	 *  @return String containing mnemonic postfix + TAB + proper Q-or-D _d,_m,#imm operands
	 * <p><listing>
	 * A8.6.376 VRSHR
	 *	{@literal vrshr<c>.<type><size> <Qd>,<Qm>,#<imm>	vrshr<c>.<type><size> <Dd>,<Dm>,#<imm>}
	 * A8.6.380 VRSRA
	 *	{@literal vrsra<c>.<type><size> <Qd>,<Qm>,#<imm>	vrsra<c>.<type><size> <Dd>,<Dm>,#<imm>}
	 * A8.6.382 VSHL (immediate)
	 *	{@literal vshl<c>.i<size>       <Qd>,<Qm>,#<imm>	vshl<c>.i<size>       <Dd>,<Dm>,#<imm>}
	 * A8.6.385 VSHR
	 *	{@literal vshr<c>.<type><size>  <Qd>,<Qm>,#<imm>	vshr<c>.<type><size>  <Dd>,<Dm>,#<imm>}
	 * A8.6.387 VSLI
	 *	{@literal vsli<c>.<size>        <Qd>,<Qm>,#<imm>	vsli<c>.<size>        <Dd>,<Dm>,#<imm>}
	 * A8.6.389 VSRA
	 *	{@literal vsra<c>.<type><size>  <Qd>,<Qm>,#<imm>	vsra<c>.<type><size>  <Dd>,<Dm>,#<imm>}
	 * A8.6.390 VSRI
	 *	{@literal vsri<c>.<size>        <Qd>,<Qm>,#<imm>	vsri<c>.<size>        <Dd>,<Dm>,#<imm>}
	 */
	private String getVFP_vXrX_instruction(int opcode, boolean encoded) {
		int l = opcode >> 7 & 1;
		int imm = l == 1
					? (encoded ? 64 - (opcode >> 16 & 0x3f) : opcode >> 16 & 0x3f)
					: (encoded ? getVFPImm6Encoded(opcode) : getVFPImm6(opcode));
		return getVFPLImm6Size(opcode, l) + getVFPQorDdmRegs(opcode) + ",#" + imm;
	}

	/**
	 * ARM--- ... D_1_6_6 imm6_1_5_0 Vd___15_12 ... L___7_7 Q___6_6 M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 imm6_1_5_0 Vd_0_15_12 ... L_0_7_7 Q_0_6_6 M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String containing mnemonic postfix + TAB + proper Q-or-D _d,_m,#imm operands
	 * <p><listing>
	 * A8.6.387 VSLI
	 *	{@literal vsli<c>.<size>        <Qd>,<Qm>,#<imm>	vsli<c>.<size>        <Dd>,<Dm>,#<imm>}
	 * A8.6.390 VSRI
	 *	{@literal vsri<c>.<size>        <Qd>,<Qm>,#<imm>	vsri<c>.<size>        <Dd>,<Dm>,#<imm>}
	 */
//	private String getVFP_vXrX_instruction(int opcode) {
//		boolean encoded = isBitEnabled()
//		int l = opcode >> 7 & 1;
//		int imm = l == 1
//					? (encoded ? 64 - (opcode >> 16 & 0x3f) : opcode >> 16 & 0x3f)
//					: (encoded ? getVFPImm6Encoded(opcode) : getVFPImm6(opcode));
//		return getVFPLImm6Size(opcode, l) + getVFPQorDdmRegs(opcode) + ",#" + imm;
//	}

	/**
	 * ARM--- ... D_1_6_6 imm6_1_5_0 Vd___15_12 ... M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 imm6_1_5_0 Vd_0_15_12 ... M_0_5_5 . Vm_0_3_0
	 *  @param opcode
	 *  @return String containing mnemonic size postfix + TAB + proper Dd,Qm,#imm operands
	 * <p><listing>
	 * A8.6.377 VRSHRN
	 *	{@literal vrshrn<c>.i<size> <Dd>,<Qm>,#<imm>}
	 * A8.6.386 VSHRN
	 *	{@literal vshrn<c>.i<size>  <Dd>,<Qm>,#<imm>}
	 */
	private String getVFP_vXshrn_instruction(int opcode) {
		int imm = getVFPQImm6(opcode);
		String size = ".i" + getVFPImm6Size(opcode);
		return size + TAB + getVFPDdQmRegs(opcode) + ",#" + getVFPImm6SHRAdj(opcode, imm);
	}

	/**
	 * ARM--- ... D_22_22 . . Rn_19_16 Vd___15_12 type___11_8 size___7_6 align___5_4 Rm___3_0<br> 
	 * Thumb2 ... D_1_6_6 . . Rn_1_3_0 Vd_0_15_12 type_0_11_8 size_0_7_6 align_0_5_4 Rm_0_3_0 
	 * @param opcode
	 * @return String containing 
	 * <p><listing>
	 * A8.6.308 VLD1 (single element to one lane)
	 *	vld1{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vld1{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.309 VLD1 (single element to all lanes)
	 *	vld1{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vld1{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.311 VLD2 (single 2-element structure to one lane)
	 *	vld2{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vld2{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.312 VLD2 (single 2-element structure to all lanes)
	 *	vld2{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vld2{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.314 VLD3 (single 3-element structure to one lane)
	 *	vld3{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vld3{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.315 VLD3 (single 3-element structure to all lanes)
	 *	vld3{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vld3{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.317 VLD4 (single 4-element structure to one lane)
	 *	vld4{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vld4{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.318 VLD4 (single 4-element structure to all lanes)
	 *	vld4{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vld4{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.392 VST1 (single element from one lane)
	 *	vst1{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vst1{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.394 VST2 (single 2-element structure from one lane)
	 *	vst2{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vst2{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.396 VST3 (single 3-element structure from one lane)
	 *	vst3{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vst3{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.398 VST4 (single 4-element structure from one lane)
	 *	vst4{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vst4{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 */	
	private String getVFP_vXX_Xlane(int opcode) {
		// careful examination of the bit patterns in the
		// reference manual shows bits 8 & 9 determine
		// whether "vld1", "vld2", "vld3", "vld4"
		// or "vst1", "vst"2, "vst3" or "vst4"
		int ver = (opcode >> 8 & 3) + 1;

		// bits 10 & 11 have double use: 0 - 2 means size for 1 lane, but 3 means all lanes
		int sz = (opcode >> 10 & 3);	// size (if not 3) for "one-lane" versions
		
		boolean allLanes = 3 == sz;
		if (allLanes)
			sz = (opcode >> 6 & 3);	// "all-lanes" size

		int spacing = 1;			// spacing: 1 == single, 2 == double
		int listMembers = ver;		// members in register list

		// vector and index used in all versions
		int vecReg = getBit(opcode, 22) << 4 | (opcode >> 12 & 0xf);
		String index = "[";

		// optional alignment string
		String alignStr = "";

		// initialize postfix version+size and first reg operand (index below)
		String ops = ver + getVFPVldVstSize(sz)	+ TAB + "{d" + vecReg;

		// determine differences for "all-lanes" versions

		if (allLanes) {
			boolean align = isBitEnabled(opcode, 4); 
			spacing = getBit(opcode, 5) + 1;
			
			index += "]";

			// figure out the list member count, spacing and alignment string
			if (ver == 1) {
				// vst1 "all-lanes" list can have 1 or 2 elemnts
				listMembers = spacing;
				spacing = 1;
				if (align && sz == 1)		alignStr = "@16";
				else if (align && sz == 2)	alignStr = "@32";
			} else if (ver == 2) {
				if (align && sz == 0)		alignStr = "@16";
				else if (align && sz == 1)	alignStr = "@32";
				else if (align && sz == 2)	alignStr = "@64";
			} else if (ver == 4) {
				if (align && sz == 0)						alignStr = "@32";
				else if (align && ((sz == 1) || (sz == 2)))	alignStr = "@64";
				else if (align && sz == 3)					alignStr = "@128";
			}
		} else {
			//  indexAlign bits for "one-lane" versions
			int ia = opcode >> 4 & 0xf;

			// index is "[x]", where x are these bits
			index += (ia >> (sz+1)) + "]";

			if (ver > 1) {
				spacing = sz == 0 ? 1 : getBit(ia, sz) + 1;
			}
			
			listMembers = ver;

			// figure out the list member count, spacing and alignment string
			if (ver == 1) {
				if (sz == 1 && (ia & 3) == 1)		alignStr = "@16";
				else if (sz == 2 && (ia & 7) == 3)	alignStr = "@32";
			} else if (ver == 2) {
				if (sz == 0 && (ia & 1) == 1)		alignStr = "@16";
				else if (sz == 1 && (ia & 1) == 1)	alignStr = "@32";
				else if (sz == 2 && (ia & 3) == 1)	alignStr = "@64";
			} else if (ver == 4) {
				if (sz == 0 && (ia & 1) == 1)		alignStr = "@32";
				else if (sz == 1 && (ia & 1) == 1)	alignStr = "@64";
				else if (sz == 2) {
					if ((ia & 3) == 1)				alignStr = "@64";
					else if ((ia & 3) == 2)			alignStr = "@128";
				}
			}
		}

		// operand generation
		ops += index;	// index determined based on lanes type

		// get each of the vreg args for each of the versions, spaced properly
		for (int i = 1; i < listMembers; ++ i) {
			ops += ",d" + (vecReg + spacing * i) + index;
		}

		// complete the generation with the Rn reg
		// + optional alignment string + the standard ending
		ops += "},[" + getR_16(opcode) + alignStr + ']' + getVFPVldVstEnding(opcode);
		return ops;
	}

	/**
	 * ARM--- ... D_22_22 . . Rn_19_16 Vd___15_12 type___11_8 size___7_6 align___5_4 Rm___3_0<br> 
	 * Thumb2 ... D_1_6_6 . . Rn_1_3_0 Vd_0_15_12 type_0_11_8 size_0_7_6 align_0_5_4 Rm_0_3_0 
	 * @param opcode
	 * @return String containing mnemonic c.size postfix + TAB + all operands
	 * <p><listing>
	 * A8.6.307 VLD1 (multiple single elements)
	 *	vld1{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vld1{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.310 VLD2 (multiple 2-element structures)
	 *	vld2{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vld2{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.313 VLD3 (multiple 3-element structures)
	 *	vld3{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vld3{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.316 VLD4 (multiple 4-element structures)
	 *	vld4{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vld4{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.391 VSY1 (multiple single elements)
	 *	vst1{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vst1{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.393 VST2 (multiple 2-element structures)
	 *	vst2{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vst2{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.395 VST3 (multiple 3-element structures)
	 *	vst3{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vst3{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 * A8.6.397 VST4 (multiple 4-element structures)
	 *	vst4{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}]{!}
	 *	vst4{@literal <c>.<size> <list>,[<Rn>}{{@literal @<align>}}],{@literal <Rm>}
	 */	
	private String getVFP_vXX_multi(int opcode) {
		int vecReg = (getBit(opcode, 22) << 4) | (opcode >> 12 & 0xf);
		int type = (opcode >> 8) & 0xf;
		int align = (opcode >> 4) & 3;
		int size = 8 << ((opcode >> 6) & 3);

		String suffix = "1";
		String regList;

		switch (type) {
		case 7:
			suffix = "1";
			regList = "{d" + vecReg + "}";
			break;
		case 10:
			suffix = "1";
			if (vecReg + 1 > 31)
				regList = "<register > d31>";
			else
				regList = "{d" + vecReg + ",d" + (vecReg + 1) + "}";
			break;
		case 6:
			suffix = "1";
			if (vecReg + 2 > 31)
				regList = "<register > d31>";
			else
				regList = "{d" + vecReg + ",d" + (vecReg + 1)
						+ ",d" + (vecReg + 2) + "}";
			break;
		case 2:
			suffix = "1";
			if (vecReg + 3 > 31)
				regList = "<register > d31>";
			else
				regList = "{d" + vecReg + ",d" + (vecReg + 1)
						+ ",d" + (vecReg + 2) + ",d" + (vecReg + 3) + "}";
			break;
		case 8:
			suffix = "2";
			if (vecReg + 1 > 31)
				regList = "<register > d31>";
			else
				regList = "{d" + vecReg + ",d" + (vecReg + 1) + "}";
			break;
		case 9:
			suffix = "2";
			if (vecReg + 2 > 31)
				regList = "<register > d31>";
			else
				regList = "{d" + vecReg + ",d" + (vecReg + 2) + "}";
			break;
		case 3:
			suffix = "2";
			if (vecReg + 3 > 31)
				regList = "<register > d31>";
			else
				regList = "{d" + vecReg + ",d" + (vecReg + 1)
						+ ",d" + (vecReg + 2) + ",d" + (vecReg + 3) + "}";
			break;
		case 4:
			suffix = "3";
			if (vecReg + 2 > 31)
				regList = "<register > d31>";
			else
				regList = "{d" + vecReg + ",d" + (vecReg + 1)
					+ ",d" + (vecReg + 2) + "}";
			break;
		case 5:
			suffix = "3";
			if (vecReg + 4 > 31)
				regList = "<register > d31>";
			else
				regList = "{d" + vecReg + ",d" + (vecReg + 2)
						+ ",d" + (vecReg + 4) + "}";
			break;
		case 0:
			suffix = "4";
			if (vecReg + 3 > 31)
				regList = "<register > d31>";
			else
				regList = "{d" + vecReg + ",d" + (vecReg + 1)
						+ ",d" + (vecReg + 2) + ",d" + (vecReg + 3) + "}";
			break;
		case 1:
			suffix = "4";
			if (vecReg + 6 > 31)
				regList = "<register > d31>";
			else
				regList = "{d" + vecReg + ",d" + (vecReg + 2)
				+ ",d" + (vecReg + 4) + ",d" + (vecReg + 6) + "}";
			break;
		default:
			regList = "";
		}

		String alignStr = "";
		if (align == 1)
			alignStr = "@64";
		else if (align == 2)
			alignStr = "@128";
		else if (align == 3)
			alignStr = "@256";

		return suffix + "." + size + TAB + regList
				+ ",[" + getR_16(opcode) + alignStr + "]"
				+ getVFPVldVstEnding(opcode);
	}

	/**
	 * ARM--- ... D_22_22 . . Rn_19_16 Vd___15_12 type___11_8 size___7_6 align___5_4 Rm___3_0<br> 
	 * Thumb2 ... D_1_6_6 . . Rn_1_3_0 Vd_0_15_12 type_0_11_8 size_0_7_6 align_0_5_4 Rm_0_3_0 
	 * @param opcode
	 * @return String containing mnemonic post-coniditon postfix + TAB + all operands
	 * <p><listing>
	 * A8.6.319 VLDM
	 *	vldm{mode}{@literal <c> <Rn>}{!}{@literal <list>}
	 * A8.6.399 VSTM
	 *	vstm{mode}{@literal <c> <Rn>}{!}{@literal <list>}
	 */	
	private String getVFP_vXXm(int opcode) {
		boolean is64 = isBitEnabled(opcode, 8);
		return TAB + getR_16(opcode) + getW(opcode)
				+ ',' + getVFPDorSRegList(opcode, is64);
	}

	/**
	 * ARM--- ... U_23_23 D_22_22 0 1 Rn_19_16 Vd___15_12 ... imm8___7_0<br> 
	 * Thumb2 ... U_1_7_7 D_1_6_6 . . Rn_1_3_0 Vd_0_15_12 ... imm8_0_7_0 
	 * @param opcode
	 * @return String containing post-condition mnemonic prefix
	 * 			+ TAB + proper D-or-S reg & imm8 addressing mode operands
	 * <p><listing>
	 * A8.6.320 VLDR
	 *	vldr{@literal <c> <Dd>, [<Rn>}{, #+/-{@literal <imm>}}]
	 *	vldr{@literal <c> <Dd>, <label>}
	 *	vldr{@literal <c> <Dd>, [pc,#-0] Special case}
	 * A8.6.320 VLDR
	 *	vldr{@literal <c> <Sd>, [<Rn>}{, #+/-{@literal <imm>}}]
	 *	vldr{@literal <c> <Sd>, <label>}
	 *	vldr{@literal <c> <Sd>, [pc,#-0] Special case}
	 * A8.6.400 VSTR
	 *	vldr{@literal <c> <Dd>, [<Rn>}{, #+/-{@literal <imm>}}]
	 * A8.6.400 VSTR
	 *	vldr{@literal <c> <Sd>, [<Rn>}{, #+/-{@literal <imm>}}]
	 */	
	private String getVFP_vXXr(int opcode) {
		int ds = getBit(opcode, 8);
		return (ds == 1 ? ".64" : ".32")
				+ TAB + getVFPDorSReg(opcode, ds, 12, 22)
				+ ',' + getAddrModeImm8(opcode);
	}

	/**
	 * ARM--- ... D_22_22 sz_21_20 Vn_19_16 Vd___15_12 ... op___8_8 N___7_7 . M___5_5 . Vm___3_0<br> 
	 * Thumb2 ... D_1_6_6 sz_1_5_4 Vn_1_3_0 Vd_0_15_12 ... op_0_8_8 N_0_7_7 . M_0_5_5 . Vm_0_3_0 
	 * @param opcode
	 * @param uBit shift position of u-bit in instruction
	 * @return "w" or "l" + type mnemonic postfix, a TAB,
	 *  and then proper Q-or-D reg operands for the instruction
	 * <p><listing>
	 * A8.6.274 VADDL, VADDW
	 *	{@literal vaddl<c>.<dt> <Qd>,<Dn>,<Dm>	vaddw<c>.<dt>} {{@literal <Qd>}},{@literal <Qn>,<Dm>}
	 * A8.6.404 VSUBL, VSUBW
	 *	{@literal vsubl<c>.<dt> <Qd>,<Dn>,<Dm>	vsubw<c>.<dt>} {{@literal <Qd>}},{@literal <Qn>,<Dm>}
	 */	
	private String getVFP_vXXXl_vXXXw(int opcode, int uBit) {
		String ops;
		if (isBitEnabled(opcode, 8)) {
			ops = 'w' + getVFPSorUDataType(opcode, uBit) + TAB;
			if (getVFPQorDRegNum(opcode, 12, 22) != getVFPQorDRegNum(opcode, 0, 5))
				  ops += getVFPQorDReg(opcode, 1, 12, 22) + ',';
			ops += getVFPQorDReg(opcode, 1, 16, 7);
		}
		else
			ops = 'l' + getVFPSorUDataType(opcode, uBit)
				  + TAB + getVFPQorDReg(opcode, 1, 12, 22)
				  + ',' + getVFPQorDReg(opcode, 0, 16, 7);
		return ops + ',' + getVFPQorDReg(opcode, 0, 0, 5);
	}

	private String getB(int opcode) {
		return isBitEnabled(opcode, 22) ? "b" : "";
	}

	private String getBorT(int opcode, int bitPos) {
		return isBitEnabled(opcode, bitPos) ? "t" : "b";
	}

	private String getE(int opcode) {
		return isBitEnabled(opcode, 7) ? "e" : "";
	}

	private String getL(int opcode) {
		return isBitEnabled(opcode, 22) ? "l" : "";
	}

	private String getR(int opcode, int bitPos) {
		return isBitEnabled(opcode, bitPos) ? "r" : "";
	}
	
	private String getS(int opcode) {
		return isBitEnabled(opcode, 20) ? "s" : "";
	}

	private String getW(int opcode) {
		return isBitEnabled(opcode, 21) ? "!" : "";
	}
	
	private String getX(int opcode, int bitPos) {
		return isBitEnabled(opcode, bitPos) ? "x" : "";
	}

	private boolean isBitEnabled(int opcode, int bit) {
		return 0 != (1 & (opcode >> bit)) ;
	}

	private void setDefaultPCJumpProperties(boolean soleDestination) {
		isSoleDestination = soleDestination;
		isSubroutineAddress = false;

		// Though it's something like establishing the PC with an address,
		// we just fake it as using Link Register, which is fine in practice.
		addrExpression = JumpToAddress.EXPRESSION_LR;
	}

	private int signExtend(int value, int leftmostBit, int size) {
		if (size >= leftmostBit)
			return value;
		int leftmostValue = value & (1 << leftmostBit);
		int extended = 0;
		for (;++leftmostBit < size ;) {
			extended = (extended << 1) + leftmostValue;
		}
		return extended | leftmostValue;
	}

}
