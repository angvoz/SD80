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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IJumpToAddress;
import org.eclipse.cdt.debug.edc.JumpToAddress;
import org.eclipse.cdt.debug.edc.disassembler.AssemblyFormatter;
import org.eclipse.cdt.debug.edc.disassembler.DisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembledInstruction;
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

	private List<Integer> prefixes = new ArrayList<Integer>();
	private List<Integer> prefixesUsed = new ArrayList<Integer>();

	private int disassemblerMode = DISASSEMBLER_MODE_THUMB;
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

	/**
	 * Whether the instruction has been parsed/disassembled. If this flag is
	 * true but the fResult is null, the given byte buffer does not contain
	 * valid instruction.
	 */
	private boolean parsed = false;

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

	private void initialize() {
		// reset position
		codeBuffer.position(startPosition);
		prefixes.clear();
		prefixesUsed.clear();
		isSoleDestination = false;
		isSubroutineAddress = false;
		jumpToAddr = null;
		addrExpression = null;
		result = new DisassembledInstruction(); // start new
	}

	public IDisassembledInstruction getResult() throws CoreException {
		if (!parsed) {
			// Default: ARM disassembler mode.
			Map<String, Object> options = new HashMap<String, Object>();
			options.put(IDisassemblerOptionsARM.DISASSEMBLER_MODE, DISASSEMBLER_MODE_ARM);
			disassemble(options);
		}
		return result;
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
	public DisassembledInstruction disassemble(Map<String, Object> options) throws CoreException {
		initialize();

		parsed = true;
		disassemblerOptions = options;

		// Make sure the code buffer is in big-endian
		if (codeBuffer.order() == ByteOrder.LITTLE_ENDIAN)
			codeBuffer.order(ByteOrder.BIG_ENDIAN);

		Object mode = options.get(IDisassemblerOptionsARM.ENDIAN_MODE);
		int endianMode = LITTLE_ENDIAN_MODE;
		if (mode != null) {
			endianMode = ((Integer) mode).intValue();
		}

		mode = options.get(IDisassemblerOptionsARM.DISASSEMBLER_MODE);
		if (mode != null)
			disassemblerMode = ((Integer) mode).intValue();
		else
			// Assume Thumb if couldn't get mode from symbolics
			disassemblerMode = 2;

		String mnemonics = null;
		CoreException err = null;

		byte b0, b1, b2, b3;
		int opcode = 0;
		int opcode2 = 0;
		try {
			if (disassemblerMode == DISASSEMBLER_MODE_ARM) {
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
				opcode += (b0 & 0xff) << 24;
				opcode += (b1 & 0xff) << 16;
				opcode += (b2 & 0xff) << 8;
				opcode += (b3 & 0xff);
				mnemonics = parseARMOpcode(opcode);
			} else {
				if (endianMode == BIG_ENDIAN_MODE) {
					b0 = codeBuffer.get();
					b1 = codeBuffer.get();
				} else {
					b1 = codeBuffer.get();
					b0 = codeBuffer.get();
				}
				opcode += (b0 & 0xff) << 8;
				opcode += (b1 & 0xff);
				// Thumb BL and BLX instructions consist of 2 16-bit Thumb
				// instructions
				if (isThumbBL(opcode)) {
					if (endianMode == BIG_ENDIAN_MODE) {
						b2 = codeBuffer.get();
						b3 = codeBuffer.get();
					} else {
						b3 = codeBuffer.get();
						b2 = codeBuffer.get();
					}
					opcode2 += (b2 & 0xff) << 8;
					opcode2 += (b3 & 0xff);
				}
				mnemonics = parseThumbOpcode(opcode, opcode2);
			}
		} catch (BufferUnderflowException e) {
			err = ARMPlugin.newCoreException("Error: end of code buffer reached.", e);
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
		IJumpToAddress jta = null;
		if (jumpToAddr != null) {
			jta = new JumpToAddress(jumpToAddr, isSoleDestination, isSubroutineAddress);
		} else if (addrExpression != null) {
			jta = new JumpToAddress(addrExpression, isSoleDestination, isSubroutineAddress);
		}
		result.setJumpToAddress(jta);
	}

	private String parseARMOpcode(int opcode) {
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
		int cpOpcode1, cpOpcode2 = 0;
		int variant = 0;
		int condition = (opcode >> 28) & 0xf;
		int p = (opcode >> 24) & 1;
		int w = (opcode >> 21) & 1;
		int z = (opcode >> 7) & 1;
		int offset;
		String shifterOperand;
		String temp = "";
		switch (opcodeIndex) {
		case arm_adc:
		case arm_umlal:
			variant = (opcode >> 4) & 0xf;
			if (variant == 9) {
				instruction = "umlal" + getCondition(opcode) + getS(opcode) + "\t" + getRdLo(opcode) + ","
						+ getRdHi(opcode) + "," + getRm(opcode) + "," + getRs(opcode);
			} else {
				instruction = "adc" + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + getRn(opcode)
						+ "," + getShifterOperand(opcode);
			}
			break;
		case arm_add:
		case arm_umull:
			variant = (opcode >> 4) & 0xf;
			if (variant == 9) {
				instruction = "umull" + getCondition(opcode) + getS(opcode) + "\t" + getRdLo(opcode) + ","
						+ getRdHi(opcode) + "," + getRm(opcode) + "," + getRs(opcode);
			} else {
				instruction = "add" + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + getRn(opcode)
						+ "," + getShifterOperand(opcode);
			}
			break;
		case arm_and:
		case arm_mul:
			variant = (opcode >> 4) & 0xf;
			if (variant == 9) {
				instruction = "mul" + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + getRm(opcode)
						+ "," + getRs(opcode);
			} else {
				instruction = "and" + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + getRn(opcode)
						+ "," + getShifterOperand(opcode);
			}
			break;
		case arm_b:
		case arm_bl:
			offset = getBranchOffset(opcode);
			condString = getCondition(opcode);
			if (condString.length() == 0) {
				condString = "\t";
			}
			instruction = mnemonic + condString + "\t" + getHexValue(offset);
			isSoleDestination = false;
			isSubroutineAddress = (opcodeIndex == OpcodeARM.Index.arm_bl);
			jumpToAddr = address.add(offset);
			break;
		case arm_bic:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getRn(opcode) + ","
					+ getShifterOperand(opcode);
			break;
		case arm_bkpt:
			instruction = mnemonic + "\t" + "#" + getHexValue((((opcode >> 4) & 0xfff0) | (opcode & 0xf)));
			break;
		case arm_blx1:
			offset = getBranchOffset(opcode) | ((opcode >> 23) & 2);
			instruction = mnemonic + "\t" + getHexValue(offset);
			isSoleDestination = true;
			isSubroutineAddress = true;
			jumpToAddr = address.add(offset);
			break;
		case arm_blx2:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRm(opcode);
			isSoleDestination = false;
			isSubroutineAddress = true;
			addrExpression = getRm(opcode);
			break;
		case arm_bx:
			condString = getCondition(opcode);
			if (condString.length() == 0) {
				condString = "\t";
			}
			instruction = mnemonic + condString + "\t" + getRm(opcode);
			isSoleDestination = false;
			isSubroutineAddress = false;
			addrExpression = getRm(opcode);
			break;
		case arm_cdp:
		case arm_cdp2:
			if (condition == 0xf) {
				mnemonic = "cdp2";
			} else {
				mnemonic = "cdp";
			}
			cpOpcode1 = (opcode >> 20) & 0xf;
			cpOpcode2 = (opcode >> 5) & 7;
			instruction = mnemonic + getCondition(opcode) + "\t" + getCoprocessor(opcode) + ","
					+ getHexValue(cpOpcode1) + "," + getCRd(opcode) + "," + getCRn(opcode) + "," + getCRm(opcode) + ","
					+ getHexValue(cpOpcode2);
			break;
		case arm_clz:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getRm(opcode);
			break;
		case arm_cmn:
		case arm_cmp:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRn(opcode) + "," + getShifterOperand(opcode);
			break;
		case arm_eor:
		case arm_mla:
			variant = (opcode >> 4) & 0xf;
			if (variant == 9) {
				instruction = "mla" + getCondition(opcode) + getS(opcode) + "\t" + getRn(opcode) + "," + getRm(opcode)
						+ "," + getRs(opcode) + "," + getRd(opcode);
			} else {
				instruction = "eor" + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + getRn(opcode)
						+ "," + getShifterOperand(opcode);
			}
			break;
		case arm_ldc:
		case arm_ldc2:
			if (condition == 0xf) {
				mnemonic = "ldc2";
			} else {
				mnemonic = "ldc";
			}
			instruction = mnemonic + getCondition(opcode) + getL(opcode) + "\t" + getCoprocessor(opcode) + ","
					+ getCRd(opcode) + "," + getAddrMode5(opcode);
			;
			break;
		case arm_ldm1:
		case arm_ldm2:
		case arm_ldm3:
			instruction = mnemonic + getCondition(opcode) + getAddrMode4(opcode) + "\t" + getRn(opcode) + getW(opcode)
					+ "," + getRegList(opcode) + getUserMode(opcode);
			break;
		case arm_ldr:
		case arm_ldrt:
			if (p == 0 && w == 1) {
				instruction = "ldr" + getCondition(opcode) + "t" + "\t" + getRd(opcode) + "," + getAddrMode2(opcode);
			} else {
				instruction = "ldr" + getCondition(opcode) + "\t" + getRd(opcode) + "," + getAddrMode2(opcode);
			}
			break;
		case arm_ldrb:
		case arm_ldrbt:
		case arm_pld:
			if (condition == 0xf) {
				instruction = "pld" + "\t" + getAddrMode2(opcode);
			} else {
				if (p == 0 && w == 1) {
					instruction = "ldr" + getCondition(opcode) + "bt" + "\t" + getRd(opcode) + ","
							+ getAddrMode2(opcode);
				} else {
					instruction = "ldr" + getCondition(opcode) + "b" + "\t" + getRd(opcode) + ","
							+ getAddrMode2(opcode);
				}
			}
			break;
		case arm_ldrd:
			instruction = "ldr" + getCondition(opcode) + "d" + "\t" + getRd(opcode) + "," + getAddrMode3(opcode);
			break;
		case arm_ldrh:
			instruction = "ldr" + getCondition(opcode) + "h" + "\t" + getRd(opcode) + "," + getAddrMode3(opcode);
			break;
		case arm_ldrsb:
			instruction = "ldr" + getCondition(opcode) + "sb" + "\t" + getRd(opcode) + "," + getAddrMode3(opcode);
			break;
		case arm_ldrsh:
			instruction = "ldr" + getCondition(opcode) + "sh" + "\t" + getRd(opcode) + "," + getAddrMode3(opcode);
			break;
		case arm_mcr:
		case arm_mcr2:
			if (condition == 0xf) {
				mnemonic = "mcr2";
			} else {
				mnemonic = "mcr";
			}
			cpOpcode1 = (opcode >> 21) & 7;
			cpOpcode2 = (opcode >> 5) & 7;
			instruction = mnemonic + getCondition(opcode) + "\t" + getCoprocessor(opcode) + ","
					+ getHexValue(cpOpcode1) + "," + getRd(opcode) + "," + getCRn(opcode) + "," + getCRm(opcode);
			if (cpOpcode2 != 0) {
				instruction += "," + getHexValue(cpOpcode2);
			}
			break;
		case arm_mcrr:
		case arm_mcrr2:
			if (condition == 0xf) {
				mnemonic = "mcrr2";
			} else {
				mnemonic = "mcrr";
			}
			cpOpcode1 = (opcode >> 4) & 0xf;
			instruction = mnemonic + getCondition(opcode) + "\t" + getCoprocessor(opcode) + ","
					+ getHexValue(cpOpcode1) + "," + getRd(opcode) + "," + getRn(opcode) + "," + getCRm(opcode);
			break;
		case arm_mov:
			shifterOperand = getShifterOperand(opcode);
			instruction = mnemonic + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + shifterOperand;
			if (getRd(opcode).equals("pc") && isRegister(shifterOperand)) {
				isSoleDestination = false;
				isSubroutineAddress = false;
				addrExpression = shifterOperand;
			}
			break;
		case arm_mrc:
		case arm_mrc2:
			if (condition == 0xf) {
				mnemonic = "mrc2";
			} else {
				mnemonic = "mrc";
			}
			cpOpcode1 = (opcode >> 21) & 7;
			cpOpcode2 = (opcode >> 5) & 7;
			instruction = mnemonic + getCondition(opcode) + "\t" + getCoprocessor(opcode) + ","
					+ getHexValue(cpOpcode1) + "," + getRd(opcode) + "," + getCRn(opcode) + "," + getCRm(opcode);
			if (cpOpcode2 != 0) {
				instruction += "," + getHexValue(cpOpcode2);
			}
			break;
		case arm_mrrc:
		case arm_mrrc2:
			if (condition == 0xf) {
				mnemonic = "mrrc2";
			} else {
				mnemonic = "mrrc";
			}
			cpOpcode1 = (opcode >> 4) & 0xf;
			instruction = mnemonic + getCondition(opcode) + "\t" + getCoprocessor(opcode) + ","
					+ getHexValue(cpOpcode1) + "," + getRd(opcode) + "," + getRn(opcode) + "," + getCRm(opcode);
			break;
		case arm_mrs:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getStatusReg(opcode);
			break;
		case arm_msr:
		case arm_msr2:
			variant = (opcode >> 25) & 1;
			if (variant == 1) {
				instruction = mnemonic + getCondition(opcode) + "\t" + getStatusReg(opcode)
						+ getStatusRegFields(opcode) + "," + getImmediate8(opcode);
			} else {
				instruction = mnemonic + getCondition(opcode) + "\t" + getStatusReg(opcode)
						+ getStatusRegFields(opcode) + "," + getRm(opcode);
			}
			break;
		case arm_mvn:
			instruction = mnemonic + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + ","
					+ getShifterOperand(opcode);
			break;
		case arm_orr:
			instruction = mnemonic + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + getRn(opcode)
					+ "," + getShifterOperand(opcode);
			break;
		case arm_qadd:
		case arm_qdadd:
		case arm_qdsub:
		case arm_qsub:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getRm(opcode) + ","
					+ getRn(opcode);
			break;
		case arm_rsb:
			instruction = mnemonic + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + getRn(opcode)
					+ "," + getShifterOperand(opcode);
			break;
		case arm_rsc:
		case arm_smlal:
			variant = (opcode >> 4) & 0xf;
			if (variant == 9) {
				instruction = "smlal" + getCondition(opcode) + getS(opcode) + "\t" + getRdLo(opcode) + ","
						+ getRdHi(opcode) + "," + getRm(opcode) + "," + getRs(opcode);
			} else {
				instruction = "rsc" + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + getRn(opcode)
						+ "," + getShifterOperand(opcode);
			}
			break;
		case arm_sbc:
		case arm_smull:
			variant = (opcode >> 4) & 0xf;
			if (variant == 9) {
				instruction = "smull" + getCondition(opcode) + getS(opcode) + "\t" + getRdLo(opcode) + ","
						+ getRdHi(opcode) + "," + getRm(opcode) + "," + getRs(opcode);
			} else {
				instruction = "sbc" + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + getRn(opcode)
						+ "," + getShifterOperand(opcode);
			}
			break;
		case arm_smla:
			instruction = mnemonic + getX(opcode) + getY(opcode) + getCondition(opcode) + "\t" + getRn(opcode) + ","
					+ getRm(opcode) + "," + getRs(opcode) + "," + getRd(opcode);
			break;
		case arm_smlalxy:
			instruction = mnemonic + getX(opcode) + getY(opcode) + getCondition(opcode) + "\t" + getRdLo(opcode) + ","
					+ getRdHi(opcode) + "," + getRm(opcode) + "," + getRs(opcode);
			break;
		case arm_smlaw:
			instruction = mnemonic + getY(opcode) + getCondition(opcode) + "\t" + getRn(opcode) + "," + getRm(opcode)
					+ "," + getRs(opcode) + "," + getRd(opcode);
			break;
		case arm_smul:
			instruction = mnemonic + getX(opcode) + getY(opcode) + getCondition(opcode) + "\t" + getRd(opcode) + ","
					+ getRm(opcode) + "," + getRs(opcode);
			break;
		case arm_smulw:
			instruction = mnemonic + getY(opcode) + getCondition(opcode) + "\t" + getRd(opcode) + "," + getRm(opcode)
					+ "," + getRs(opcode);
			break;
		case arm_stc:
		case arm_stc2:
			if (condition == 0xf) {
				mnemonic = "stc2";
			} else {
				mnemonic = "stc";
			}
			instruction = mnemonic + getCondition(opcode) + getN(opcode) + "\t" + getCoprocessor(opcode) + ","
					+ getCRd(opcode) + "," + getAddrMode5(opcode);
			break;
		case arm_stm1:
		case arm_stm2:
			instruction = mnemonic + getCondition(opcode) + getAddrMode4(opcode) + "\t" + getRn(opcode) + getW(opcode)
					+ "," + getRegList(opcode) + getUserMode(opcode);
			break;
		case arm_str:
		case arm_strt:
			if (p == 0 && w == 1) {
				instruction = "str" + getCondition(opcode) + "t" + "\t" + getRd(opcode) + "," + getAddrMode2(opcode);
			} else {
				instruction = "str" + getCondition(opcode) + "\t" + getRd(opcode) + "," + getAddrMode2(opcode);
			}
			break;
		case arm_strb:
		case arm_strbt:
			if (p == 0 && w == 1) {
				instruction = "str" + getCondition(opcode) + "bt" + "\t" + getRd(opcode) + "," + getAddrMode2(opcode);
			} else {
				instruction = "str" + getCondition(opcode) + "b" + "\t" + getRd(opcode) + "," + getAddrMode2(opcode);
			}
			break;
		case arm_strd:
			instruction = "str" + getCondition(opcode) + "d" + "\t" + getRd(opcode) + "," + getAddrMode3(opcode);
			break;
		case arm_strh:
			instruction = "str" + getCondition(opcode) + "h" + "\t" + getRd(opcode) + "," + getAddrMode3(opcode);
			break;
		case arm_sub:
			instruction = mnemonic + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + getRn(opcode)
					+ "," + getShifterOperand(opcode);
			break;
		case arm_swi:
			instruction = mnemonic + getCondition(opcode) + "\t" + getImmediate24(opcode);
			break;
		case arm_swp:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getRm(opcode) + ",["
					+ getRn(opcode) + "]";
			break;
		case arm_swpb:
			instruction = "swp" + getCondition(opcode) + "b" + "\t" + getRd(opcode) + "," + getRm(opcode) + ",["
					+ getRn(opcode) + "]";
			break;
		case arm_teq:
		case arm_tst:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRn(opcode) + "," + getShifterOperand(opcode);
			break;
		// ARMv6 instructions
		case arm_bxj:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRm(opcode);
			break;
		case arm_cps:
			instruction = mnemonic + "\t" + "#" + (opcode & 0x1f);
			break;
		case arm_cpsid:
		case arm_cpsie:
			if ((opcode & (1 << 8)) != 0)
				temp.concat("a");
			if ((opcode & (1 << 7)) != 0)
				temp.concat("i");
			if ((opcode & (1 << 6)) != 0)
				temp.concat("f");
			instruction = mnemonic + " " + temp + "\t" + "#" + (opcode & 0x1f);
			break;
		case arm_cpy:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getRm(opcode);
			break;
		case arm_pkhbt:
			if (((opcode >> 7) & 0x1f) != 0)
				variant = ((opcode >> 7) & 0x1f);
			instruction = mnemonic + "\t" + getRd(opcode) + "," + getRn(opcode) + "," + getRm(opcode) + "lsl #"
					+ variant;
			break;
		case arm_pkhtb:
			if (((opcode >> 7) & 0x1f) == 0)
				variant = 32;
			else
				variant = ((opcode >> 7) & 0x1f);
			instruction = mnemonic + "\t" + getRd(opcode) + "," + getRn(opcode) + "," + getRm(opcode) + "asr #"
					+ variant;
			break;
		case arm_rev:
		case arm_rev16:
		case arm_revsh:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getRm(opcode);
			break;
		case arm_rfeia:
		case arm_rfeib:
		case arm_rfeda:
		case arm_rfedb:
			instruction = mnemonic + "\t" + getRn(opcode);
			if ((opcode & (1 << 21)) != 0)
				instruction = instruction + "!";
			break;
		case arm_setend:
			instruction = mnemonic + "\t";
			if ((opcode & (1 << 9)) == 0)
				instruction = instruction + "le";
			else
				instruction = instruction + "be";
			break;
		case arm_smmul:
		case arm_smmulr:
			instruction = mnemonic + "\t" + getRn(opcode) + "," + getRm(opcode) + "," + getRs(opcode);
			break;
		case arm_srsia:
		case arm_srsib:
		case arm_srsda:
		case arm_srsdb:
			instruction = mnemonic + "\t" + "#" + (opcode & 0x1f);
			if ((opcode & (1 << 21)) != 0)
				instruction = instruction + "!";
			break;
		case arm_ssat:
		case arm_usat:
			if (((opcode >> 6) & 0x3f) != 0) {
				if ((opcode & (1 << 6)) == 0)
					temp = ",lsl #" + ((opcode >> 7) & 0x1f);
				else
					temp = ",asr #" + ((opcode >> 7) & 0x1f);

			}
			instruction = mnemonic + "\t" + getRd(opcode) + "," + "#" + ((opcode >> 16) & 0x1f) + "," + getRm(opcode)
					+ temp;
			break;
		case arm_ssat16:
			instruction = mnemonic + "\t" + getRd(opcode) + "," + "#" + (((opcode >> 16) & 0xf) + 1) + ","
					+ getRm(opcode);
			break;
		case arm_usat16:
			instruction = mnemonic + "\t" + getRd(opcode) + "," + "#" + ((opcode >> 16) & 0xf) + "," + getRm(opcode);
			break;
		case arm_sxtb:
		case arm_sxtb16:
		case arm_sxth:
			if (((opcode >> 10) & 0x3) != 0)
				temp = ",ror #" + (((opcode >> 10) & 0x3) * 8);
			instruction = mnemonic + "\t" + getRd(opcode) + "," + getRn(opcode) + "," + getRm(opcode) + temp;
			break;
		case arm_uxtb:
		case arm_uxtb16:
		case arm_uxth:
			if (((opcode >> 10) & 0x3) != 0)
				temp = ",ror #" + (((opcode >> 10) & 0x3) * 8);
			instruction = mnemonic + "\t" + getRd(opcode) + "," + getRm(opcode) + temp;
			break;
		case arm_qadd16:
		case arm_qadd8:
		case arm_qaddsubx:
		case arm_qsub16:
		case arm_qsub8:
		case arm_qsubaddx:
		case arm_sadd16:
		case arm_sadd8:
		case arm_saddsubx:
		case arm_sel:
		case arm_shadd16:
		case arm_shadd8:
		case arm_shaddsubx:
		case arm_shsub16:
		case arm_shsub8:
		case arm_shsubaddx:
			instruction = mnemonic + "\t" + getRd(opcode) + "," + getRn(opcode) + "," + getRm(opcode);
			break;
		case arm_smlald:
		case arm_smlaldx:
		case arm_smlsld:
		case arm_smlsldx:
			variant = (opcode >> 24) & 1;
			if (variant == 0) {
				instruction = mnemonic + getCondition(opcode) + getS(opcode) + "\t" + getRdLo(opcode) + ","
						+ getRdHi(opcode) + "," + getRm(opcode) + "," + getRs(opcode);
			} else {
				instruction = mnemonic + getX(opcode) + getY(opcode) + getCondition(opcode) + "\t" + getRdLo(opcode)
						+ "," + getRdHi(opcode) + "," + getRm(opcode) + "," + getRs(opcode);
			}
			break;
		case arm_smmls:
		case arm_smmlsr:
		case arm_smmla:
		case arm_smmlar:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getRm(opcode) + ","
					+ getRs(opcode) + "," + getRn(opcode);
			break;
		case arm_smuad:
		case arm_smuadx:
		case arm_smusd:
		case arm_smusdx:
		case arm_usad8:
			instruction = mnemonic + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + getRm(opcode)
					+ "," + getRs(opcode);
			break;
		case arm_ssub16:
		case arm_ssub8:
		case arm_ssubaddx:
		case arm_uadd16:
		case arm_uadd8:
		case arm_uaddsubx:
		case arm_uhadd16:
		case arm_uhadd8:
		case arm_uhaddsubx:
		case arm_uhsub16:
		case arm_uhsub8:
		case arm_uhsubaddx:
		case arm_uqadd16:
		case arm_uqadd8:
		case arm_uqaddsubx:
		case arm_uqsub16:
		case arm_uqsub8:
		case arm_uqsubaddx:
		case arm_usub16:
		case arm_usub8:
		case arm_usubaddx:
			instruction = mnemonic + "\t" + getRd(opcode) + "," + getRn(opcode) + "," + getRm(opcode);
			break;
		case arm_usada8:
		case arm_smlsd:
		case arm_smlsdx:
		case arm_smlad:
		case arm_smladx:
			instruction = mnemonic + getCondition(opcode) + getS(opcode) + "\t" + getRd(opcode) + "," + getRm(opcode)
					+ "," + getRs(opcode) + "," + getRn(opcode);
			break;
		case arm_uxtab:
		case arm_uxtab16:
		case arm_uxtah:
		case arm_sxtab:
		case arm_sxtab16:
		case arm_sxtah:
			if (((opcode >> 10) & 0x3) != 0)
				temp = ",ror #" + (((opcode >> 10) & 0x3) * 8);
			instruction = mnemonic + "\t" + getRd(opcode) + "," + getRn(opcode) + "," + getRm(opcode) + temp;
			break;
		// VFP instructions
		case arm_fabsd:
		case arm_fcmpd:
		case arm_fcmped:
		case arm_fcpyd:
		case arm_fnegd:
		case arm_fsqrtd:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPDReg(opcode, 12) + "," + getVFPDReg(opcode, 0);
			break;
		case arm_fabss:
		case arm_fcmps:
		case arm_fcmpes:
		case arm_fcpys:
		case arm_fnegs:
		case arm_fsitos:
		case arm_fsqrts:
		case arm_fuitos:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPSReg(opcode, 12, 22) + ","
					+ getVFPSReg(opcode, 0, 5);
			break;
		case arm_faddd:
		case arm_fdivd:
		case arm_fmacd:
		case arm_fmscd:
		case arm_fmuld:
		case arm_fnmacd:
		case arm_fnmscd:
		case arm_fnmuld:
		case arm_fsubd:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPDReg(opcode, 12) + ","
					+ getVFPDReg(opcode, 16) + "," + getVFPDReg(opcode, 0);
			break;
		case arm_fadds:
		case arm_fdivs:
		case arm_fmacs:
		case arm_fmscs:
		case arm_fmuls:
		case arm_fnmacs:
		case arm_fnmscs:
		case arm_fnmuls:
		case arm_fsubs:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPSReg(opcode, 12, 22) + ","
					+ getVFPSReg(opcode, 16, 7) + "," + getVFPSReg(opcode, 0, 5);
			break;
		case arm_fcmpezd:
		case arm_fcmpzd:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPDReg(opcode, 12);
			break;
		case arm_fcmpezs:
		case arm_fcmpzs:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPSReg(opcode, 12, 22);
			break;
		case arm_fcvtds:
		case arm_fsitod:
		case arm_fuitod:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPDReg(opcode, 12) + ","
					+ getVFPSReg(opcode, 0, 5);
			break;
		case arm_fcvtsd:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPSReg(opcode, 12, 22) + ","
					+ getVFPDReg(opcode, 0);
			break;
		case arm_fldd:
		case arm_fstd:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPDReg(opcode, 12) + "," + getAddrMode5(opcode);
			break;
		case arm_flds:
		case arm_fsts:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPSReg(opcode, 12, 22) + ","
					+ getAddrMode5(opcode);
			break;
		case arm_fldmd:
			instruction = "fldm" + getVFPAddrMode5(opcode) + "d\t" + getCondition(opcode) + getRn(opcode);
			instruction += (w == 1) ? "!" : "";
			instruction += "," + getVFPDRegList(opcode);
			break;
		case arm_fldms:
			instruction = "fldm" + getVFPAddrMode5(opcode) + "s\t" + getCondition(opcode) + getRn(opcode);
			instruction += (w == 1) ? "!" : "";
			instruction += "," + getVFPSRegList(opcode);
			break;
		case arm_fmdhr:
		case arm_fmdlr:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPDReg(opcode, 16) + "," + getRd(opcode);
			break;
		case arm_fmdrr:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPDReg(opcode, 0) + "," + getRd(opcode) + ","
					+ getRn(opcode);
			break;
		case arm_fmrdh:
		case arm_fmrdl:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getVFPDReg(opcode, 16);
			break;
		case arm_fmrrd:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getRn(opcode) + ","
					+ getVFPDReg(opcode, 0);
			break;
		case arm_fmrrs:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getRn(opcode) + ",{"
					+ getVFPSReg(opcode, 0, 5) + "," + getVFPSReg1(opcode, 0, 5) + "}";
			break;
		case arm_fmrs:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getVFPSReg(opcode, 16, 7);
			break;
		case arm_fmrx:
			instruction = mnemonic + getCondition(opcode) + "\t" + getRd(opcode) + "," + getVFPSysReg(opcode);
			break;
		case arm_fmsr:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPSReg(opcode, 16, 7) + "," + getRd(opcode);
			break;
		case arm_fmsrr:
			instruction = mnemonic + getCondition(opcode) + "\t{" + getVFPSReg(opcode, 0, 5) + ","
					+ getVFPSReg1(opcode, 0, 5) + "}," + getRd(opcode) + "," + getRn(opcode);
			break;
		case arm_fmstat:
			instruction = mnemonic + getCondition(opcode);
			break;
		case arm_fmxr:
			instruction = mnemonic + getCondition(opcode) + "\t" + getVFPSysReg(opcode) + "," + getRd(opcode);
			break;
		case arm_fstmd:
			instruction = "fstm" + getVFPAddrMode5(opcode) + "d" + getCondition(opcode) + getRn(opcode);
			instruction += (w == 1) ? "!" : "";
			instruction += "," + getVFPDRegList(opcode);
			break;
		case arm_fstms:
			instruction = "fstm" + getVFPAddrMode5(opcode) + "s" + getCondition(opcode) + getRn(opcode);
			instruction += (w == 1) ? "!" : "";
			instruction += "," + getVFPSRegList(opcode);
			break;
		case arm_ftosid:
			instruction = "ftosi";
			instruction += (z == 1) ? "z" : "";
			instruction += "d" + getCondition(opcode) + "\t" + getVFPSReg(opcode, 12, 22) + "," + getVFPDReg(opcode, 0);
			break;
		case arm_ftosis:
			instruction = "ftosi";
			instruction += (z == 1) ? "z" : "";
			instruction += "s" + getCondition(opcode) + "\t" + getVFPSReg(opcode, 12, 22) + ","
					+ getVFPSReg(opcode, 0, 5);
			break;
		case arm_ftouid:
			instruction = "ftoui";
			instruction += (z == 1) ? "z" : "";
			instruction += "d" + getCondition(opcode) + "\t" + getVFPSReg(opcode, 12, 22) + "," + getVFPDReg(opcode, 0);
			break;
		case arm_ftouis:
			instruction = "ftoui";
			instruction += (z == 1) ? "z" : "";
			instruction += "s" + getCondition(opcode) + "\t" + getVFPSReg(opcode, 12, 22) + ","
					+ getVFPSReg(opcode, 0, 5);
			break;
		default:
			instruction = "invalid opcode";
			break;
		}
		return instruction;
	}

	private String parseThumbOpcode(int opcode, int opcode2) {
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
		int offset;
		int bit;
		switch (opcodeIndex) {
		case thumb_adc:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		case thumb_add1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + ","
					+ getThumbImmediate3(opcode);
			break;
		case thumb_add2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + "," + getThumbImmediate8(opcode, 1);
			break;
		case thumb_add3:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6);
			break;
		case thumb_add4:
			instruction = mnemonic + "\t" + getThumbRegHigh(opcode, 0, 7) + "," + getThumbRegHigh(opcode, 3, 6);
			break;
		case thumb_add5:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + ",pc," + getThumbImmediate8(opcode, 4);
			break;
		case thumb_add6:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + ",sp," + getThumbImmediate8(opcode, 4);
			break;
		case thumb_add7:
			instruction = mnemonic + "\tsp," + getThumbImmediate7(opcode, 4);
			break;
		case thumb_and:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		case thumb_asr1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + ","
					+ getThumbImmediate5(opcode, 1);
			break;
		case thumb_asr2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		case thumb_b1:
			offset = getThumbBranchOffset8(opcode);
			String condString = getThumbCondition(opcode);
			if (condString.length() == 0) {
				condString = "\t";
			}
			instruction = mnemonic + condString + "\t" + getHexValue(offset);
			isSoleDestination = false;
			isSubroutineAddress = false;
			jumpToAddr = address.add(offset);
			break;
		case thumb_b2:
			offset = getThumbBranchOffset11(opcode);
			instruction = mnemonic + "\t\t" + getHexValue(offset);
			isSoleDestination = true;
			isSubroutineAddress = false;
			jumpToAddr = address.add(offset);
			break;
		case thumb_bic:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		case thumb_bkpt:
			instruction = mnemonic + "\t" + getThumbImmediate8(opcode, 1);
			break;
		case thumb_blx1:
			offset = getThumbBLOffset(opcode, opcode2);
			int h = (opcode2 >> 11) & 3;
			if (h == 3) {
				instruction = "bl" + "\t\t" + getHexValue(offset);
			} else {
				instruction = "blx" + "\t" + getHexValue(offset);
			}
			isSoleDestination = true;
			isSubroutineAddress = true;
			jumpToAddr = address.add(offset);
			break;
		case thumb_blx2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 3);
			isSoleDestination = true;
			isSubroutineAddress = true;
			addrExpression = getThumbReg(opcode, 3);
			break;
		case thumb_bx:
			instruction = mnemonic + "\t\t" + getThumbReg(opcode, 3);
			isSoleDestination = true;
			isSubroutineAddress = false;
			addrExpression = getThumbReg(opcode, 3);
			break;
		case thumb_cmn:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		case thumb_cmp1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + "," + getThumbImmediate8(opcode, 1);
			break;
		case thumb_cmp2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		case thumb_cmp3:
			instruction = mnemonic + "\t" + getThumbRegHigh(opcode, 0, 7) + "," + getThumbRegHigh(opcode, 3, 6);
			break;
		case thumb_cps:
			instruction = mnemonic + getThumbEffect(opcode) + "\t" + getThumbIFlags(opcode);
			break;
		case thumb_cpy:
			instruction = mnemonic + "\t" + getThumbRegHigh(opcode, 0, 7) + "," + getThumbRegHigh(opcode, 3, 6);
			if (getThumbRegHigh(opcode, 0, 7).equals("pc")) {
				isSoleDestination = true;
				isSubroutineAddress = false;
				addrExpression = getThumbRegHigh(opcode, 3, 6);
			}
			break;
		case thumb_eor:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		case thumb_ldmia:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + "!" + "," + getThumbRegList(opcode, null);
			break;
		case thumb_ldr1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbImmediate5(opcode, 4) + "]";
			break;
		case thumb_ldr2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			break;
		case thumb_ldr3:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + ",[pc," + getThumbImmediate8(opcode, 4) + "]";
			break;
		case thumb_ldr4:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + ",[sp," + getThumbImmediate8(opcode, 4) + "]";
			break;
		case thumb_ldrb1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbImmediate5(opcode, 1) + "]";
			break;
		case thumb_ldrb2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			break;
		case thumb_ldrh1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbImmediate5(opcode, 2) + "]";
			break;
		case thumb_ldrh2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			break;
		case thumb_ldrsb:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			break;
		case thumb_ldrsh:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			break;
		case thumb_lsl1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + ","
					+ getThumbImmediate5(opcode, 1);
			break;
		case thumb_lsl2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		case thumb_lsr1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + ","
					+ getThumbImmediate5(opcode, 1);
			break;
		case thumb_lsr2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		case thumb_mov1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + "," + getThumbImmediate8(opcode, 1);
			break;
		case thumb_mov2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		case thumb_mul:
		case thumb_mvn:
		case thumb_neg:
		case thumb_orr:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		case thumb_pop:
			bit = (opcode >> 8) & 1;
			if (bit == 1) {
				regList = getThumbRegList(opcode, "pc");
			} else {
				regList = getThumbRegList(opcode, null);
			}
			instruction = mnemonic + "\t" + regList;
			break;
		case thumb_push:
			bit = (opcode >> 8) & 1;
			if (bit == 1) {
				regList = getThumbRegList(opcode, "lr");
			} else {
				regList = getThumbRegList(opcode, null);
			}
			instruction = mnemonic + "\t" + regList;
			break;
		case thumb_rev:
		case thumb_rev16:
		case thumb_revsh:
		case thumb_ror:
		case thumb_sbc:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		case thumb_setend:
			String endian = (((opcode >> 3) & 1) == 1) ? "be" : "le";
			instruction = mnemonic + "\t" + endian;
			break;
		case thumb_stmia:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + "!" + "," + getThumbRegList(opcode, null);
			break;
		case thumb_str1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbImmediate5(opcode, 4) + "]";
			break;
		case thumb_str2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			break;
		case thumb_str3:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + ",[sp," + getThumbImmediate8(opcode, 4) + "]";
			break;
		case thumb_strb1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbImmediate5(opcode, 1) + "]";
			break;
		case thumb_strb2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			break;
		case thumb_strh1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbImmediate5(opcode, 2) + "]";
			break;
		case thumb_strh2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + ",[" + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6) + "]";
			break;
		case thumb_sub1:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + ","
					+ getThumbImmediate3(opcode);
			break;
		case thumb_sub2:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 8) + "," + getThumbImmediate8(opcode, 1);
			break;
		case thumb_sub3:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3) + ","
					+ getThumbReg(opcode, 6);
			break;
		case thumb_sub4:
			instruction = mnemonic + "\tsp," + getThumbImmediate7(opcode, 4);
			break;
		case thumb_swi:
			instruction = mnemonic + "\t" + getThumbImmediate8(opcode, 1);
			break;
		case thumb_sxtb:
		case thumb_sxth:
		case thumb_tst:
		case thumb_uxtb:
		case thumb_uxth:
			instruction = mnemonic + "\t" + getThumbReg(opcode, 0) + "," + getThumbReg(opcode, 3);
			break;
		default:
			instruction = "invalid opcode";
			break;
		}
		return instruction;
	}

	private boolean isRegister(String expression) {
		if ((expression.startsWith("r") && expression.length() < 4) || expression.equals("sp")
				|| expression.equals("lr") || expression.equals("pc")) {
			return true;
		}
		return false;
	}

	private boolean isThumbBL(int opcode) {
		int h = (opcode >> 12) & 0xf;
		if (h == 0xf) {
			return true;
		} else {
			return false;
		}
	}

	private String getAddrMode2(int opcode) {
		int p = (opcode >> 24) & 1;
		int u = (opcode >> 23) & 1;
		int w = (opcode >> 21) & 1;
		int regOffset = (opcode >> 25) & 1;
		int scaled = (opcode >> 5) & 0xff;
		int shiftValue = (opcode >> 7) & 0x1f;
		int offset = opcode & 0xfff;
		String operands = "";
		String sign = (u == 1) ? "" : "-";
		if (p == 1) {
			if (regOffset == 0) { // immediate offset
				operands = "[" + getRn(opcode) + ",#" + sign + getHexValue(offset) + "]";
				if (w == 1) { // pre-indexed
					operands += "!";
				}
			} else {
				if (scaled == 0) { // register offset
					operands = "[" + getRn(opcode) + "," + sign + getRm(opcode) + "]";
					if (w == 1) { // pre-indexed
						operands += "!";
					}
				} else { // scaled register offset
					operands = "[" + getRn(opcode) + "," + sign + getRm(opcode) + "," + getShiftMode(opcode);
					if (shiftValue != 0) {
						operands += "#" + getHexValue(shiftValue);
					}
					operands += "]";
					if (w == 1) { // pre-indexed
						operands += "!";
					}
				}
			}
		} else { // post-indexed
			if (regOffset == 0) { // immediate offset
				operands = "[" + getRn(opcode) + "],#" + sign + getHexValue(offset);
			} else {
				if (scaled == 0) { // register offset
					operands = "[" + getRn(opcode) + "]," + sign + getRm(opcode);
				} else { // scaled register offset
					operands = "[" + getRn(opcode) + "]," + sign + getRm(opcode) + "," + getShiftMode(opcode);
					if (shiftValue != 0) {
						operands += "#" + getHexValue(shiftValue);
					}
				}
			}
		}
		return operands;
	}

	private String getAddrMode3(int opcode) {
		int p = (opcode >> 24) & 1;
		int u = (opcode >> 23) & 1;
		int w = (opcode >> 21) & 1;
		int immOffset = (opcode >> 22) & 1;
		int offsetHi = (opcode >> 4) & 0xf0;
		int offsetLo = opcode & 0xf;
		int offset = offsetHi | offsetLo;
		String operands = "";
		String sign = (u == 1) ? "" : "-";
		if (p == 1) {
			if (immOffset == 1) { // immediate offset
				operands = "[" + getRn(opcode) + ",#" + sign + getHexValue(offset) + "]";
				if (w == 1) { // pre-indexed
					operands += "!";
				}
			} else { // register offset
				operands = "[" + getRn(opcode) + "," + sign + getRm(opcode) + "]";
				if (w == 1) { // pre-indexed
					operands += "!";
				}
			}
		} else { // post-indexed
			if (immOffset == 1) { // immediate offset
				operands = "[" + getRn(opcode) + "],#" + sign + getHexValue(offset);
			} else { // register offset
				operands = "[" + getRn(opcode) + "]," + sign + getRm(opcode);
			}
		}
		return operands;
	}

	private String getAddrMode4(int opcode) {
		int mode = (opcode >> 23) & 3;
		switch (mode) {
		case 0:
			return "da";
		case 1:
			return "ia";
		case 2:
			return "db";
		case 3:
			return "ib";
		default:
			return "";
		}
	}

	private String getAddrMode5(int opcode) {
		int p = (opcode >> 24) & 1;
		int u = (opcode >> 23) & 1;
		int w = (opcode >> 21) & 1;
		int offset = (opcode & 0xff) * 4;
		String operands = "";
		String sign = (u == 1) ? "" : "-";
		if (p == 1) {
			operands = "[" + getRn(opcode) + ",#" + sign + getHexValue(offset) + "]";
			if (w == 1) { // pre-indexed
				operands += "!";
			}
		} else {
			if (w == 1) { // post-indexed
				operands = "[" + getRn(opcode) + "],#" + sign + getHexValue(offset);
			} else { // unindexed
				operands = "[" + getRn(opcode) + "]";
				if (offset != 0) {
					operands += ",#" + getHexValue(offset);
				}
			}
		}
		return operands;
	}

	private int getBranchOffset(int opcode) {
		int offset = ((opcode << 8) >> 6) + 8;
		return offset;
	}

	private String getCondition(int opcode) {
		int condition = (opcode >> 28) & 0xf;
		switch (condition) {
		case 0:
			return "eq";
		case 1:
			return "ne";
		case 2:
			return "cs";
		case 3:
			return "cc";
		case 4:
			return "mi";
		case 5:
			return "pl";
		case 6:
			return "vs";
		case 7:
			return "vc";
		case 8:
			return "hi";
		case 9:
			return "ls";
		case 10:
			return "ge";
		case 11:
			return "lt";
		case 12:
			return "gt";
		case 13:
			return "le";
		case 14: // always (unconditional)
		case 15:
		default:
			return "";
		}
	}

	private String getCoprocessor(int opcode) {
		int cproc = (opcode >> 8) & 0xf;
		return ("p" + cproc);
	}

	private String getCRd(int opcode) {
		int cReg = (opcode >> 12) & 0xf;
		return ("cr" + cReg);
	}

	private String getCRm(int opcode) {
		int cReg = opcode & 0xf;
		return ("cr" + cReg);
	}

	private String getCRn(int opcode) {
		int reg = (opcode >> 16) & 0xf;
		return ("cr" + reg);
	}

	private String getHexValue(int value) {
		return "0x" + Integer.toHexString(value);
	}

	private String getImmediate24(int opcode) {
		int imm24 = opcode & 0xffffff;
		return "#" + getHexValue(imm24);
	}

	private String getImmediate8(int opcode) {
		int imm8 = opcode & 0xff;
		return "#" + getHexValue(imm8);
	}

	private String getN(int opcode) {
		int n = (opcode >> 22) & 1;
		if (n > 0) {
			return "l";
		} else {
			return "";
		}
	}

	private String getRd(int opcode) {
		int reg = (opcode >> 12) & 0xf;
		return getRegName(reg);
	}

	private String getRdHi(int opcode) {
		int reg = (opcode >> 16) & 0xf;
		return getRegName(reg);
	}

	private String getRdLo(int opcode) {
		int reg = (opcode >> 12) & 0xf;
		return getRegName(reg);
	}

	private String getRm(int opcode) {
		int reg = opcode & 0xf;
		return getRegName(reg);
	}

	private String getRn(int opcode) {
		int reg = (opcode >> 16) & 0xf;
		return getRegName(reg);
	}

	private String getRs(int opcode) {
		int reg = (opcode >> 8) & 0xf;
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

	private String getL(int opcode) {
		int l = (opcode >> 22) & 1;
		if (l > 0) {
			return "l";
		} else {
			return "";
		}
	}

	private String getS(int opcode) {
		int s = (opcode >> 20) & 1;
		if (s > 0) {
			return "s";
		} else {
			return "";
		}
	}

	private String getShifterOperand(int opcode) {
		int shift, immval;
		int bit25 = (opcode >> 25) & 1;
		if (bit25 == 1) { // bit set - we have immidiate shifter opperand
			shift = (opcode & 0x0f00) >> 7; // rotate_imm*2
			immval = (opcode & 0xff);
			immval = (immval >> shift) | (immval << (32 - shift));
			return "#" + getHexValue(immval);
		}

		if ((opcode & 0xff0) == 0) // got Rm
			return getRm(opcode);

		// else shifted
		immval = (opcode >> 7) & 0x1f;
		if ((opcode & 0x70) == 0x0) {// LSL #imm
			return getRm(opcode) + ",lsl #" + getHexValue(immval);
		} else if ((opcode & 0xf0) == 0x10) {// LSL Rs
			return getRm(opcode) + ",lsl " + getRs(opcode);
		} else if ((opcode & 0x70) == 0x20) {// LSR #imm
			return getRm(opcode) + ",lsr #" + getHexValue(immval);
		} else if ((opcode & 0xf0) == 0x30) {// LSR Rs
			return getRm(opcode) + ",lsr " + getRs(opcode);
		} else if ((opcode & 0x70) == 0x40) {// ASR #imm
			return getRm(opcode) + ",asr #" + getHexValue(immval);
		} else if ((opcode & 0xf0) == 0x50) {// ASR Rs
			return getRm(opcode) + ",asr " + getRs(opcode);
		} else if ((opcode & 0xff0) == 0x60) {// RRX
			return getRm(opcode) + ",rrx";
		} else if ((opcode & 0x70) == 0x60) {// ROR #imm
			return getRm(opcode) + ",ror #" + getHexValue(immval);
		} else if ((opcode & 0xf0) == 0x70) {// ROR Rs
			return getRm(opcode) + ",ror " + getRs(opcode);
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

	private String getStatusReg(int opcode) {
		int sr = (opcode >> 22) & 1;
		if (sr == 0) {
			return "cpsr";
		} else {
			return "spsr";
		}
	}

	private String getStatusRegFields(int opcode) {
		String fields = "_";
		int c = (opcode >> 16) & 1;
		int x = (opcode >> 17) & 1;
		int s = (opcode >> 18) & 1;
		int f = (opcode >> 19) & 1;
		if (c == 1) {
			fields += "c";
		}
		if (x == 1) {
			fields += "x";
		}
		if (s == 1) {
			fields += "s";
		}
		if (f == 1) {
			fields += "f";
		}
		return fields;
	}

	private int getThumbBranchOffset8(int opcode) {
		int offset = (opcode & 0xff);
		offset = ((offset << 25) >> 24) + 4;
		return offset;
	}

	private int getThumbBranchOffset11(int opcode) {
		int offset = (opcode & 0x7ff);
		offset = ((offset << 22) >> 21) + 4;
		return offset;
	}

	private String getThumbCondition(int opcode) {
		int condition = (opcode >> 8) & 0xf;
		switch (condition) {
		case 0:
			return "eq";
		case 1:
			return "ne";
		case 2:
			return "cs";
		case 3:
			return "cc";
		case 4:
			return "mi";
		case 5:
			return "pl";
		case 6:
			return "vs";
		case 7:
			return "vc";
		case 8:
			return "hi";
		case 9:
			return "ls";
		case 10:
			return "ge";
		case 11:
			return "lt";
		case 12:
			return "gt";
		case 13:
			return "le";
		case 14: // always (unconditional)
		case 15:
		default:
			return "";
		}
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
		int i = (opcode >> 2) & 1;
		int f = (opcode & 1);
		String iflags = "";
		iflags += (a == 1) ? "a" : "";
		iflags += (i == 1) ? "i" : "";
		iflags += (f == 1) ? "f" : "";
		return iflags;
	}

	private String getThumbImmediate3(int opcode) {
		int imm = (opcode >> 6) & 7;
		return "#" + getHexValue(imm);
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

	private int getThumbBLOffset(int opcode1, int opcode2) {
		int offset1 = opcode1 & 0x7ff;
		int offset2 = opcode2 & 0x7ff;
		int imm = ((offset1 << 21) >> 9) + (offset2 << 1) + 4;
		return imm;
	}

	private String getUserMode(int opcode) {
		int userMode = (opcode >> 22) & 1;
		if (userMode > 0) {
			return "^";
		} else {
			return "";
		}
	}

	private String getVFPAddrMode5(int opcode) {
		int mode = (opcode >> 23) & 3;
		switch (mode) {
		case 1:
			return "ia";
		case 2:
			return "db";
		default:
			return "";
		}
	}

	private String getVFPDReg(int opcode, int position) {
		int reg = (opcode >> position) & 0xf;
		return "d" + reg;
	}

	private String getVFPSReg(int opcode, int position, int lsb) {
		int reg = ((opcode >> position) & 0xf) << 1;
		reg |= (opcode >> lsb) & 1;
		return "s" + reg;
	}

	private String getVFPSReg1(int opcode, int position, int lsb) {
		int reg = ((opcode >> position) & 0xf) << 1;
		reg |= (opcode >> lsb) & 1;
		return "s" + (reg + 1);
	}

	private String getVFPSysReg(int opcode) {
		int reg = (opcode >> 16) & 0xf;
		switch (reg) {
		case 0:
			return "fpsid";
		case 1:
			return "fpscr";
		case 8:
			return "fpexc";
		default:
			return "sysReg" + getHexValue(reg);
		}
	}

	private String getVFPDRegList(int opcode) {
		int reg = (opcode >> 12) & 0xf;
		int count = (opcode & 0xff) >> 1;
		String regList = "{";
		for (int i = 0; i < count; i++) {
			regList += "d" + (reg + i) + ",";
		}
		regList += "}";
		regList = regList.replace(",}", "}");
		return regList;
	}

	private String getVFPSRegList(int opcode) {
		int reg = (opcode >> 11) & 0x1c;
		reg |= (opcode >> 22) & 1;
		int count = opcode & 0xff;
		String regList = "{";
		for (int i = 0; i < count; i++) {
			regList += "s" + (reg + i) + ",";
		}
		regList += "}";
		regList = regList.replace(",}", "}");
		return regList;
	}

	private String getW(int opcode) {
		int w = (opcode >> 21) & 1;
		if (w > 0) {
			return "!";
		} else {
			return "";
		}
	}

	private String getX(int opcode) {
		int x = (opcode >> 5) & 1;
		if (x == 0) {
			return "b";
		} else {
			return "t";
		}
	}

	private String getY(int opcode) {
		int y = (opcode >> 6) & 1;
		if (y == 0) {
			return "b";
		} else {
			return "t";
		}
	}

}
