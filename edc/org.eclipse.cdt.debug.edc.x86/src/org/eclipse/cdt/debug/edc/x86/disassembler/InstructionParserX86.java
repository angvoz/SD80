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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IJumpToAddress;
import org.eclipse.cdt.debug.edc.JumpToAddress;
import org.eclipse.cdt.debug.edc.disassembler.DisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler.IDisassemblerOptions;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.x86.disassembler.DisassemblerX86.IDisassemblerOptionsX86;
import org.eclipse.core.runtime.CoreException;

/**
 * Single instruction disassembler for x86.
 */
public class InstructionParserX86 {

	class ModRM {
		public int mod, reg, rm;
	};

	class SIB {
		public int scale, index, base;
	};

	public static final int ADDRESS_MODE_16BIT = 1, ADDRESS_MODE_32BIT = 2, ADDRESS_MODE_64BIT = 4;

	private final List<Integer> prefixes = new ArrayList<Integer>();
	private final List<Integer> prefixesUsed = new ArrayList<Integer>();

	private final List<Byte> opcode = new ArrayList<Byte>();
	private ModRM modRM;
	private SIB sib;
	private String[] operandStrings;

	private int addressMode = ADDRESS_MODE_32BIT;
	private Map<String, Object> disassemblerOptions = null;

	/**
	 * address of the first byte of the instruction
	 */
	private final IAddress address;

	/**
	 * raw data of the instruction.
	 */
	private final ByteBuffer codeBuffer;

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
	 * true but the result is null, the given byte buffer does not contain valid
	 * instruction.
	 */
	private boolean parsed = false;

	private boolean indirectAddressing;

	private boolean isMemoryOperand;

	private int operandSize;

	/**
	 * suffix for instruction name to indicate operand size. e.g. 'b', 'w', and
	 * 'l'.
	 */
	private String nameSizeSuffix;
	/**
	 * Some instruction use source operand (the second one in Intel syntax) to
	 * determine operand size, some use target operand (the first one).
	 */
	private boolean honorSrcSize;

	private IAddress jumpToAddr;

	/**
	 * prepare to disassemble the instruction at the current position of the
	 * given byte buffer.
	 */
	public InstructionParserX86(IAddress addr, ByteBuffer codeBuffer) {
		this.address = addr;
		this.codeBuffer = codeBuffer;
		this.startPosition = codeBuffer.position();
	}

	private void initialize() {
		// reset position
		codeBuffer.position(startPosition);

		prefixes.clear();
		prefixesUsed.clear();
		opcode.clear();

		nameSizeSuffix = null; // we'll know this after parsing the operands.
		honorSrcSize = false; // use target operand by default.

		modRM = null;
		sib = null;
		operandStrings = null;

		jumpToAddr = null;

		result = new DisassembledInstruction(); // start new
	}

	public IDisassembledInstruction getResult() throws CoreException {
		if (!parsed) {
			// Default: 32 bit address mode.
			Map<String, Object> options = new HashMap<String, Object>();
			options.put(IDisassemblerOptionsX86.ADDRESS_MODE, ADDRESS_MODE_32BIT);

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

		// Make sure the code buffer is in little-endian
		if (codeBuffer.order() == ByteOrder.BIG_ENDIAN)
			codeBuffer.order(ByteOrder.LITTLE_ENDIAN);

		Object mode = options.get(IDisassemblerOptionsX86.ADDRESS_MODE);
		if (mode != null)
			addressMode = ((Integer) mode).intValue();

		String mnemonics = null;

		CoreException err = null;

		try {
			getPrefix();

			byte b1 = codeBuffer.get();

			opcode.add(b1);

			if (b1 == 0x0F) {
				byte b2 = codeBuffer.get();
				opcode.add(b2);

				if (b2 == 0x38 || b2 == 0x3A) {
					byte b3 = codeBuffer.get();
					opcode.add(b3);
					mnemonics = parseOpcode(3, b3);
				} else {
					mnemonics = parseOpcode(2, b2);
				}
			} else
				mnemonics = parseOpcode(1, b1);

		} catch (BufferUnderflowException e) {
			err = EDCDebugger.newCoreException("Error: end of code buffer reached.", e);
		} catch (CoreException e) {
			err = e;
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
			asmOutput.append(AssemblyFormatterX86.formatForAddressColumn(address));
		}

		if (checkBooleanOption(IDisassemblerOptions.MNEMONICS_SHOW_BYTES) || err != null) {
			int currPos = codeBuffer.position();
			asmOutput.append(AssemblyFormatterX86.formatForByteColumn(codeBuffer, startPosition, instSize));
			codeBuffer.position(currPos);
		}

		if (err != null) {
			// create informative error message
			String msg = "Fail to disassemble this instruction (address + code-bytes): " + asmOutput.toString();
			msg += "\nCause: " + err.getMessage();
			throw EDCDebugger.newCoreException(msg);
		} else {
			// Now fill in output.
			//
			result.setAddress(address);

			fillInJumpToAddres();

			// Append the instruction proper
			asmOutput.append(mnemonics);

			result.setMnemonics(asmOutput.toString());
		}

		return result;
	}

	private void fillInJumpToAddres() {
		boolean isSoleDestination = false;
		boolean isSubroutineAddress = false;
		String addrExpression = null;

		IJumpToAddress jta = null;
		if (isConditonalJump(opcode)) {
			assert jumpToAddr != null;
			isSoleDestination = false;
		} else if (isAbsoluteJump(opcode)) {
			isSoleDestination = true;
			addrExpression = operandStrings[0];
		} else if (isCall(opcode)) {
			isSoleDestination = true;
			isSubroutineAddress = true;
			addrExpression = operandStrings[0];
		} else if (isNearReturn(opcode)) {
			isSoleDestination = true;
			isSubroutineAddress = false;
			addrExpression = JumpToAddress.EXPRESSION_RETURN_NEAR;
		} else if (isFarReturn(opcode)) {
			isSoleDestination = true;
			isSubroutineAddress = false;
			addrExpression = JumpToAddress.EXPRESSION_RETURN_FAR;
		} else if (isLoop(opcode)) {
			isSoleDestination = false;
			isSubroutineAddress = false;
			assert jumpToAddr != null;
		} else
			// non control-transfer instruction
			return;

		if (jumpToAddr != null)
			// Immediate address is available, use it.
			jta = new JumpToAddress(jumpToAddr, isSoleDestination, isSubroutineAddress);
		else {
			// Otherwise use the expression string.
			assert addrExpression != null;
			jta = new JumpToAddress(addrExpression, isSoleDestination, isSubroutineAddress);
		}

		result.setJumpToAddress(jta);
	}

	private boolean isLoop(List<Byte> opcode) {
		int code;
		if (opcode.size() == 1) { // one byte
			code = opcode.get(0) & 0xff;
			if (code >= 0xe0 && code <= 0xe2)
				return true;
		}

		return false;
	}

	private boolean isNearReturn(List<Byte> opcode) {
		int code;
		if (opcode.size() == 1) { // one byte
			code = opcode.get(0) & 0xff;
			if (code == 0xc2 || code == 0xc3)
				return true;
		}

		return false;
	}

	private boolean isFarReturn(List<Byte> opcode) {
		int code;
		if (opcode.size() == 1) { // one byte
			code = opcode.get(0) & 0xff;
			if (code == 0xca || code == 0xcb)
				return true;
		}

		return false;
	}

	private boolean isCall(List<Byte> opcode) {
		int code;
		if (opcode.size() == 1) { // one byte
			code = opcode.get(0) & 0xff;
			if (code == 0xe8 || code == 0x9a)
				return true;
			else if (code == 0xff) {
				assert modRM != null;
				if (modRM.reg == 2 || modRM.reg == 3)
					return true;
			}
		}

		return false;
	}

	private boolean isAbsoluteJump(List<Byte> opcode) {
		int code;
		if (opcode.size() == 1) { // one byte
			code = opcode.get(0) & 0xff;
			if (code >= 0xe9 && code <= 0xeb)
				return true;
			else if (code == 0xff) {
				assert modRM != null;
				if (modRM.reg == 4 || modRM.reg == 5)
					return true;
			}
		}

		return false;
	}

	private boolean isConditonalJump(List<Byte> opcode) {
		int code;
		if (opcode.size() == 1) { // one byte
			code = opcode.get(0) & 0xff;
			if (code >= 0x70 && code <= 0x7f)
				return true;
		} else if (opcode.size() == 2) {
			code = opcode.get(1) & 0xff;
			if (code >= 0x80 && code <= 0x8f)
				return true;
		}

		return false;
	}

	private boolean checkBooleanOption(String option) {
		if (!disassemblerOptions.containsKey(option))
			return false;

		Boolean value = (Boolean) disassemblerOptions.get(option);
		return value.booleanValue();
	}

	private String parseOpcode(int opcodeByteCnt, byte opcodeLastByte) throws CoreException {
		OpcodeX86 opc = null;
		int opcodeID = opcodeLastByte & 0xff; // convert to integer ID

		if (opcodeByteCnt == 1) {
			opc = lookupOneByteOpcode(opcodeID);
		} else if (opcodeByteCnt == 2) {
			opc = lookupTwoByteOpcode(opcodeID);
		} else if (opcodeByteCnt == 3) {
			opc = lookupThreeByteOpcode(opcodeID);
		}

		assert opc != null;

		if (opc.needModRM() && modRM == null) {
			modRM = getModRM(codeBuffer.get());
		}

		// Check which operand should be used to determine size suffix.
		if (opc.getName().endsWith(OpcodeX86.SIZE_FLAG_SOURCE))
			honorSrcSize = true;

		// Parse operands one by one.
		//
		String[] operandDescs = opc.getOperandDescriptors();
		if (operandDescs != null) {
			int opCnt = operandDescs.length;
			String opr;

			operandStrings = new String[opCnt];
			for (int i = 0; i < opCnt; i++) {
				// Default. These will be set in parsing the operand.
				isMemoryOperand = false;
				operandSize = 32;

				opr = parseOperand(operandDescs[i]);

				if (isMemoryOperand) {
					// check if there is segment-override prefix.
					String seg = getSegmentRegisterFromPrefix();
					if (seg != null)
						opr = seg + ":" + opr;
				}

				operandStrings[i] = opr;

				// determine the operand size suffix
				if (!honorSrcSize && i == 0 || // honor target, the first
						// operand
						honorSrcSize && i > 0) // honor source, the second
				// operand
				{
					// Don't set suffix if the determining operand is register
					// operand.
					// Example case: "imul OD_Ev":
					// f7 ea imul %edx
					// f7 ad 74 fb ff ff imull -0x48c(%ebp)
					//
					// Exception case:
					// The target register operand determines the size suffix.
					// "0f be c2", "movsbl %dl,%eax",
					// "66 0f be 82 00 b0 2c", "movsbw 0x82cb000(%edx),%ax",
					//
					if (isMemoryOperand
							|| AssemblyFormatterX86.sInstructionsSuffixFromRegisterOperand.contains(opc.getName()))
						nameSizeSuffix = AssemblyFormatterX86.instructionNameSizeSuffix(operandSize);
				}
			}
		}

		// Now printable output
		//
		ArrayList<Integer> instPrefixes = new ArrayList<Integer>(prefixes);
		instPrefixes.removeAll(prefixesUsed);
		return AssemblyFormatterX86.formatInstruction(instPrefixes, opc.getName(), nameSizeSuffix, operandStrings);
	}

	private OpcodeX86 lookupOneByteOpcode(int opcodeID) throws CoreException {
		OpcodeX86 opc;

		// Step 1: look up code with mandatory prefix
		//
		for (int p : prefixes) {
			if (OpcodeX86.sOpcodeMap_OneByteWithPrefix.containsKey(p)) {
				Map<Integer, OpcodeX86> submap = OpcodeX86.sOpcodeMap_OneByteWithPrefix.get(p);
				if (submap.containsKey(opcodeID)) {
					prefixesUsed.add(p);
					opc = submap.get(opcodeID);
					return opc;
				}
			}
		}

		// Step 2: now look up extended opcode.
		//
		if (OpcodeX86.sOpcodeMap_OneByteExtension.containsKey(opcodeID)) {
			// The opcode falls in the extension map,
			// namely "Reg" bits of ModR/M byte are extending the opcode.
			//
			modRM = getModRM(codeBuffer.get());

			opc = OpcodeX86.sOpcodeMap_OneByteExtension.get(opcodeID).get(modRM.reg);

			if (opc == null)
				throw EDCDebugger.newCoreException(MessageFormat.format(
						"Extension opcode \"0x{0} /{1}\" is not supported by the disassembler yet.", Integer
								.toHexString(opcodeID), modRM.reg));
			else
				return opc;
		}

		// Step 3: look up escape opcode
		//
		if (opcodeID >= 0xd8 && opcodeID <= 0xdf) {
			opc = lookupEscapeOpcode(opcodeID);
			if (opc != null)
				return opc;
		} else {
			// Step 4: look up regular opcode
			// 
			opc = OpcodeX86.sOpcodeMap_OneByte.get(opcodeID);
		}

		if (opc == null)
			throw EDCDebugger.newCoreException(MessageFormat.format(
					"Opcode \"0x{0}\" is not supported by the disassembler yet.", Integer.toHexString(opcodeID)));

		return opc;
	}

	private OpcodeX86 lookupEscapeOpcode(int opcodeID) throws CoreException {
		OpcodeX86 opc = null;
		if (opcodeID < 0xd8 || opcodeID > 0xdf)
			return null;

		if (modRM == null) // read in ModRM if not yet.
			modRM = getModRM(codeBuffer.get());

		int modRMByte = modRM.mod << 6 | modRM.reg << 3 | modRM.rm;

		int index = modRMByte <= 0xbf ? 0 : 1;
		int key = modRMByte <= 0xbf ? modRM.reg : modRMByte;

		if (!OpcodeX86.sOpcodeMap_Escape.containsKey(opcodeID))
			return null;

		Map<Integer, OpcodeX86> subMap = OpcodeX86.sOpcodeMap_Escape.get(opcodeID).get(index);
		opc = subMap.get(key);
		if (opc == null)
			throw EDCDebugger.newCoreException(MessageFormat.format(
					"Escape Opcode \"0x{0}\" with ModRM \"{1}\" is not supported by the disassembler yet.", Integer
							.toHexString(opcodeID), Integer.toHexString(modRMByte)));

		return opc;
	}

	private OpcodeX86 lookupTwoByteOpcode(int opcodeID) throws CoreException {
		OpcodeX86 opc;

		// Step 1: look up code with mandatory prefix
		//
		for (int p : prefixes) {
			if (OpcodeX86.sOpcodeMap_TwoByteWithPrefix.containsKey(p)) {
				Map<Integer, OpcodeX86> submap = OpcodeX86.sOpcodeMap_TwoByteWithPrefix.get(p);
				if (submap.containsKey(opcodeID)) {
					prefixesUsed.add(p);
					opc = submap.get(opcodeID);
					return opc;
				}
			}
		}

		// Step 2: now look up extended opcode.
		//
		int keyToExtensionMap = opcodeID << 4;
		if (OpcodeX86.sOpcodeMap_TwoByteExtension.containsKey(keyToExtensionMap)) {
			// The opcode falls in the extension map,
			// namely "Reg" bits of ModR/M byte are extending the opcode.
			//
			modRM = getModRM(codeBuffer.get());
			keyToExtensionMap |= modRM.mod; // real key

			opc = OpcodeX86.sOpcodeMap_TwoByteExtension.get(keyToExtensionMap).get(modRM.reg);

			if (opc == OpcodeX86.sVaringOpcode)
				opc = OpcodeX86.selectExtensionOpcodeByRM(opcodeID, modRM);

			if (opc == null)
				throw EDCDebugger.newCoreException(MessageFormat.format(
						"Extension opcode \"{0} {1} /{2}\" is not supported by the disassembler yet.", Integer
								.toHexString(opcode.get(0)), Integer.toHexString(opcodeID), modRM.reg));
			else
				return opc;
		}

		// Step 3: look up regular opcode
		// 
		opc = OpcodeX86.sOpcodeMap_TwoByte.get(opcodeID);

		if (opc == null)
			throw EDCDebugger.newCoreException(MessageFormat.format(
					"Two-byte opcode \"{0} {1}\" is not supported by the disassembler yet.", AssemblyFormatterX86
							.toHexString(opcode.get(0), false), AssemblyFormatterX86.toHexString(opcodeID, false)));

		return opc;
	}

	private OpcodeX86 lookupThreeByteOpcode(int opcodeID) throws CoreException {
		OpcodeX86 opc;

		// Step 1: look up code with mandatory prefix
		//
		for (int p : prefixes) {
			if (OpcodeX86.sOpcodeMap_ThreeByteWithPrefix.containsKey(p)) {
				Map<Integer, OpcodeX86> submap = OpcodeX86.sOpcodeMap_ThreeByteWithPrefix.get(p);
				if (submap.containsKey(opcodeID)) {
					prefixesUsed.add(p);
					opc = submap.get(opcodeID);
					return opc;
				}
			}
		}

		// Step 2: look up regular opcode for 0F38
		//
		if (opcode.get(1) == 0x38)
			opc = OpcodeX86.sOpcodeMap_ThreeByte_0F38.get(opcodeID);
		else {
			assert opcode.get(1) == 0x3a : "Invalid three byte opcode.";
			opc = OpcodeX86.sOpcodeMap_ThreeByte_0F3A.get(opcodeID);
		}

		if (opc == null)
			throw EDCDebugger.newCoreException(MessageFormat.format(
					"Three-byte opcode \"{0} {1}\" is not supported by the disassembler yet.", AssemblyFormatterX86
							.toHexString(opcode.get(0), false), AssemblyFormatterX86.toHexString(opcode.get(1), false),
					AssemblyFormatterX86.toHexString(opcodeID, false)));

		return opc;
	}

	private ModRM getModRM(byte b) {
		ModRM ret = new ModRM();
		ret.mod = (b >> 6) & 0x03;
		ret.reg = (b >> 3) & 0x07;
		ret.rm = b & 0x07;
		return ret;
	}

	private SIB getSIB(byte b) {
		SIB ret = new SIB();
		ret.scale = (b >> 6) & 0x03;
		ret.index = (b >> 3) & 0x07;
		ret.base = b & 0x07;
		return ret;
	}

	private String parseOperand(String operandDescriptor) throws CoreException {
		indirectAddressing = false; // default

		String ret = null;

		if (operandDescriptor.charAt(0) == OpcodeX86.INDIRECT_INDICATOR) {
			indirectAddressing = true;
			operandDescriptor = operandDescriptor.substring(1);
		}

		if (isRegisterID(operandDescriptor)) {
			// immediate register like: AL, eAX, rAX, rSP, etc.
			String reg = new String(operandDescriptor);

			if (addressMode == ADDRESS_MODE_32BIT) {
				if (prefixes.contains(OpcodeX86.PREFIX_OPERAND_SIZE_OVERRIDE)) { // 16-bit
					prefixesUsed.add(OpcodeX86.PREFIX_OPERAND_SIZE_OVERRIDE);

					if (reg.length() == 3)
						reg = reg.substring(1);
				} else
					reg = reg.replace('r', 'e'); // only consider 32-bit mode at
				// present.
			}

			ret = AssemblyFormatterX86.formatRegister(reg.toLowerCase(), indirectAddressing);
		} else {
			// addressing operand
			char addressingMethod = operandDescriptor.charAt(0);
			assert Character.isUpperCase(addressingMethod);

			char[] operandType = operandDescriptor.substring(1).toCharArray();

			ret = addressOperand(addressingMethod, operandType);
		}

		return ret;
	}

	/**
	 * Addressing dispatcher that calls corresponding addressing method.<br>
	 * Note the addressing method must be in this form:<br>
	 * <i>public String addressing_X(char addressingMethod char[] operandType)
	 * throws CoreException </i>
	 * 
	 * @param addressingMethod
	 *            - letter defined in section
	 *            "A.2.1 Codes for Addressing Method" in Intel manual Vol 3B.
	 * @param operandType
	 *            - letters defined in section "A.2.2 Codes for Operand Type"
	 *            and "A.2.3 Register Codes" in Intel Manual Vol 3B.
	 * @return String representation of the operand, e.g. "0x1(%ecx)",
	 *         "0x12345".
	 * @throws CoreException
	 */
	private String addressOperand(char addressingMethod, char[] operandType) throws CoreException {
		String methodName = "addressing_" + addressingMethod;

		Method handler = null;
		try {
			handler = this.getClass().getMethod(methodName, new Class[] { char[].class });
		} catch (SecurityException e) {
			// should not happen
			EDCDebugger.getMessageLogger().logError(null, e);
			return null;
		} catch (NoSuchMethodException e) {
			throw EDCDebugger.newCoreException(MessageFormat.format("Addressing method \"{0}\" is not supported yet.",
					addressingMethod));
		}

		try {
			String op = (String) handler.invoke(this, new Object[] { operandType });
			return op;
		} catch (IllegalArgumentException e) {
			// should not happen
			EDCDebugger.getMessageLogger().logError(null, e);
		} catch (IllegalAccessException e) {
			// should not happen
			EDCDebugger.getMessageLogger().logError(null, e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();

			cause.printStackTrace(); // TODO should we log this to the error
			// log?

			if (cause instanceof BufferUnderflowException)
				throw EDCDebugger.newCoreException("End of code buffer reached.", (Exception) cause);
			else if (cause instanceof CoreException)
				throw (CoreException) cause;
			else {
				String ex = cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName();
				throw EDCDebugger.newCoreException("Exception or error: " + ex);
			}
		}

		return null;
	}

	/**
	 * @param operandType
	 * @throws CoreException
	 */
	public String addressing_A(char[] operandType) throws CoreException {
		final char addressingMethod = 'A';
		int offset;
		String operand = null;

		switch (operandType[0]) {
		case 'p':
			if (addressMode == ADDRESS_MODE_16BIT) {
				offset = codeBuffer.getShort();
			} else {
				offset = codeBuffer.getInt();
			}

			short seg = codeBuffer.getShort();

			operand = AssemblyFormatterX86.formatFarPointer(seg, offset);
			break;

		default:
			throw EDCDebugger.newCoreException(MessageFormat.format(
					"Operand descriptor \"{0}{1}{2}\" is not supported yet.", addressingMethod, operandType[0],
					(operandType.length > 1 ? operandType[1] : "")));
		}

		return operand;
	}

	public String addressing_C(char[] operandType) throws CoreException {
		final char addressingMethod = 'C';

		return registerFromModRM_Reg(addressingMethod, operandType);
	}

	public String addressing_D(char[] operandType) throws CoreException {
		final char addressingMethod = 'D';

		return registerFromModRM_Reg(addressingMethod, operandType);
	}

	public String addressing_E(char[] operandType) throws CoreException {
		final char addressingMethod = 'E';

		if (modRM == null)
			throw EDCDebugger.newCoreException("ModRM required but not available.");

		/*
		 * see page 2-7 of Intel Developer's Manual Vol. 2A: Table 2-2: 32-Bit
		 * Addressing Forms with the ModR/M Byte
		 */
		if (modRM.mod == 3) { // registers
			return registerFromModRM_RM(addressingMethod, operandType);
		} else { // memory addressing
			isMemoryOperand = true;

			return memoryFromModRM(addressingMethod, operandType);
		}
	}

	public String addressing_G(char[] operandType) throws CoreException {
		final char addressingMethod = 'G';

		return registerFromModRM_Reg(addressingMethod, operandType);
	}

	public String addressing_I(char[] operandType) throws CoreException {
		final char addressingMethod = 'I';
		int imm;

		switch (operandType[0]) {
		case 'b':
			imm = codeBuffer.get();
			break;
		case 'w':
			imm = codeBuffer.getShort();
			break;
		case 'v':
		case 'z':
			if (addressMode == ADDRESS_MODE_16BIT) {
				imm = codeBuffer.getShort();
			} else {
				if (prefixes.contains(OpcodeX86.PREFIX_OPERAND_SIZE_OVERRIDE)) {
					prefixesUsed.add(OpcodeX86.PREFIX_OPERAND_SIZE_OVERRIDE);
					imm = codeBuffer.getShort();
				} else
					imm = codeBuffer.getInt();
			}
			break;
		default:
			throw EDCDebugger.newCoreException(MessageFormat.format(
					"Operand descriptor \"{0}{1}{2}\" is not supported yet.", addressingMethod, operandType[0],
					(operandType.length > 1 ? operandType[1] : "")));
		}

		return AssemblyFormatterX86.formatImmediate(imm);
	}

	public String addressing_J(char[] operandType) throws CoreException {
		final char addressingMethod = 'J';
		int offset;

		switch (operandType[0]) {
		case 'b':
			offset = codeBuffer.get();
			break;
		case 'z':
			if (addressMode == ADDRESS_MODE_16BIT) {
				offset = codeBuffer.getShort();
			} else {
				offset = codeBuffer.getInt();
			}
			break;
		default:
			throw EDCDebugger.newCoreException(MessageFormat.format(
					"Operand descriptor \"{0}{1}{2}\" is not supported yet.", addressingMethod, operandType[0],
					(operandType.length > 1 ? operandType[1] : "")));
		}

		// Offset needs be relative to the end of the instruction.
		offset += codeBuffer.position() - startPosition;

		jumpToAddr = address.add(offset);

		isMemoryOperand = true;

		return AssemblyFormatterX86.formatForCode(jumpToAddr);
	}

	public String addressing_M(char[] operandType) throws CoreException {
		final char addressingMethod = 'M';

		assert modRM != null;

		if (modRM.mod == 3) { // registers
			throw EDCDebugger.newCoreException("Invalid Opcode at " + address.toHexAddressString());
		} else { // memory addressing
			isMemoryOperand = true;
			return memoryFromModRM(addressingMethod, operandType);
		}
	}

	public String addressing_N(char[] operandType) throws CoreException {
		final char addressingMethod = 'N';

		return registerFromModRM_RM(addressingMethod, operandType);
	}

	public String addressing_O(char[] operandType) throws CoreException {
		int offset;

		// operandType ('b' or 'v') only indicate operand size.
		//
		if (addressMode == ADDRESS_MODE_16BIT) {
			offset = codeBuffer.getShort();
		} else {
			offset = codeBuffer.getInt();
		}

		isMemoryOperand = true;

		return AssemblyFormatterX86.formatOffset(offset);
	}

	public String addressing_R(char[] operandType) throws CoreException {
		final char addressingMethod = 'R';

		return registerFromModRM_RM(addressingMethod, operandType);
	}

	public String addressing_S(char[] operandType) throws CoreException {
		final char addressingMethod = 'S';

		return registerFromModRM_Reg(addressingMethod, operandType);
	}

	public String addressing_U(char[] operandType) throws CoreException {
		final char addressingMethod = 'U';

		return registerFromModRM_RM(addressingMethod, operandType);
	}

	public String addressing_V(char[] operandType) throws CoreException {
		final char addressingMethod = 'V';

		return registerFromModRM_Reg(addressingMethod, operandType);
	}

	public String addressing_X(char[] operandType) throws CoreException {
		final char addressingMethod = 'X';

		switch (operandType[0]) {
		case 'b':
			operandSize = 8;
			break;
		case 'v':
		case 'z':
			operandSize = 32;
			if (prefixes.contains(OpcodeX86.PREFIX_OPERAND_SIZE_OVERRIDE)) {
				prefixesUsed.add(OpcodeX86.PREFIX_OPERAND_SIZE_OVERRIDE);
				operandSize = 16;
			}
			break;
		default:
			throw EDCDebugger.newCoreException(MessageFormat.format(
					"Operand descriptor \"{0}{1}{2}\" is not supported yet.", addressingMethod, operandType[0],
					(operandType.length > 1 ? operandType[1] : "")));
		}

		isMemoryOperand = true;

		// setNameSizeSuffix(sizeSuffix);

		return AssemblyFormatterX86.registerPair_DSrSI(addressMode);
	}

	public String addressing_Y(char[] operandType) throws CoreException {
		final char addressingMethod = 'Y';

		switch (operandType[0]) {
		case 'b':
			operandSize = 8;
			break;
		case 'v':
		case 'z':
			operandSize = 32; // default to double-word
			if (prefixes.contains(OpcodeX86.PREFIX_OPERAND_SIZE_OVERRIDE)) {
				prefixesUsed.add(OpcodeX86.PREFIX_OPERAND_SIZE_OVERRIDE);
				operandSize = 16;
			}
			break;
		default:
			throw EDCDebugger.newCoreException(MessageFormat.format(
					"Operand descriptor \"{0}{1}{2}\" is not supported yet.", addressingMethod, operandType[0],
					(operandType.length > 1 ? operandType[1] : "")));
		}

		isMemoryOperand = true;

		return AssemblyFormatterX86.registerPair_ESrDI(addressMode);
	}

	/**
	 * Check if an operand descriptor is denoting a register.
	 * 
	 * @param operandDescriptor
	 *            things like "AL", "Ev", etc.
	 * @return
	 */
	private boolean isRegisterID(String operandDescriptor) {
		// If the second character is upper case, the descriptor is register ID.
		return operandDescriptor.length() > 1 && Character.isUpperCase(operandDescriptor.charAt(1));
	}

	private void getPrefix() {
		codeBuffer.mark(); // mark current position

		int b = codeBuffer.get() & 0xff;

		if (b >= 0x40 && b <= 0x4F) {
			if (addressMode == ADDRESS_MODE_64BIT) {
				// REX prefix.
				prefixes.add(b);
				getPrefix();
			} else
				codeBuffer.reset();
		} else if (OpcodeX86.sAllPrefixes.contains(b)) {
			prefixes.add(b);
			getPrefix();
		} else {
			codeBuffer.reset();
		}
	}

	private String memoryFromModRM(char addressingMethod, char[] operandType) throws CoreException {
		/*
		 * see page 2-7 of Intel Developer's Manual Vol. 2A: Table 2-2: 32-Bit
		 * Addressing Forms with the ModR/M Byte
		 */

		// Example (from objdump):
		// 80abe02: f6 44 50 01 08 testb $0x8,0x1(%eax,%edx,2)

		// First read SIB byte if needed.
		if (modRM.rm == 4) {
			// SIB follows, read in.
			byte b = codeBuffer.get();
			sib = getSIB(b);
		}

		// now read displacement if needed
		int displacement = 0;
		boolean useBase, useIndex;
		int baseRegID = 0, indexRegID = 0;

		boolean useDisplacement = true;

		if (modRM.mod == 1) { // 8-bit displacement, signed
			displacement = codeBuffer.get();
		} else if (modRM.mod == 2 || modRM.mod == 0 && modRM.rm == 5 || modRM.mod == 0 && modRM.rm == 4
				&& sib.base == 5) { // 32-bit displacement, signed
			displacement = codeBuffer.getInt();
		} else
			useDisplacement = false;

		if (modRM.rm == 4) {
			// SIB used.
			useIndex = (sib.index != 4);
			useBase = !(sib.base == 5 && modRM.mod == 0);
			indexRegID = sib.index;
			baseRegID = sib.base;
		} else {
			// no SIB involved.
			useIndex = false; // no index reg.
			useBase = !(modRM.rm == 5 && modRM.mod == 0);
			baseRegID = modRM.rm; // base reg denoted by "r/m" bits.
		}

		operandSize = 32;
		if (operandType.length > 0 && operandType[0] == 'b')
			operandSize = 8;
		else if (prefixes.contains(OpcodeX86.PREFIX_OPERAND_SIZE_OVERRIDE)) {
			prefixesUsed.add(OpcodeX86.PREFIX_OPERAND_SIZE_OVERRIDE);
			operandSize = 16;
		}

		// Now format the operand string.
		//
		if (!useBase)
			baseRegID = -1;
		if (!useIndex)
			indexRegID = -1;

		return AssemblyFormatterX86.format(indirectAddressing, useDisplacement ? displacement : null, baseRegID,
				indexRegID, sib != null ? sib.scale : 0);
	}

	/**
	 * Get register names.
	 */
	private String getRegisterName(int regID, char addressingMethod, char[] operandType) throws CoreException {

		String operand;
		int key;

		switch (operandType[0]) {
		case 'b':
			key = 8;
			break;
		case 'w':
			key = 16;
			break;
		case 'd':
			key = 32;
			break;
		case 'q':
			key = 64;
			break;
		case 'v':
		case 'z':
			if (addressMode == ADDRESS_MODE_16BIT)
				key = 16;
			else {
				key = 32;
				if (prefixes.contains(OpcodeX86.PREFIX_OPERAND_SIZE_OVERRIDE)) {
					prefixesUsed.add(OpcodeX86.PREFIX_OPERAND_SIZE_OVERRIDE);
					key = 16;
				}
			}
			break;
		default:
			throw EDCDebugger.newCoreException(MessageFormat.format(
					"Operand descriptor \"{0}{1}{2}\" is not supported yet.", addressingMethod, operandType[0],
					(operandType.length > 1 ? operandType[1] : "")));
		}

		operandSize = key;

		// String sizeSuffix = key==8 ? "b" : (key==16 ? "w" : "l");
		// setNameSizeSuffix(sizeSuffix);

		// print out
		//
		if (indirectAddressing)
			operand = "*";
		else
			operand = "";

		try {
			String[] names = null;
			switch (addressingMethod) {
			case 'G': // General register
			case 'E':
			case 'R':
				names = (AssemblyFormatterX86.sGPRNames.get(key));
				break;
			case 'C':
				names = AssemblyFormatterX86.sControlRegisterNames;
				break;
			case 'D':
				names = AssemblyFormatterX86.sDebugRegisterNames;
				break;
			case 'S':
				names = AssemblyFormatterX86.sSegmentRegisterNames;
				break;
			case 'N': // MMX registers
			case 'P':
				throw EDCDebugger.newCoreException("MMX registers are not supported by the disassembler yet.");
			case 'V': // XMM registers
			case 'U':
				throw EDCDebugger.newCoreException("XMM registers are not supported by the disassembler yet.");
			default:
				assert false : MessageFormat.format("unsupported addressing method \"{0}\" for getting register.",
						addressingMethod);
				break;
			}

			operand += names[regID];

		} catch (Exception e) { // out-of-bound, etc.
			e.printStackTrace(); // for debug
			throw EDCDebugger.newCoreException(MessageFormat.format("Invalid register ID {0} in instruction.", regID));
		}

		return operand;
	}

	/**
	 * Get register from REG bits of ModRM byte.
	 */
	private String registerFromModRM_Reg(char addressingMethod, char[] operandType) throws CoreException {
		return getRegisterName(modRM.reg, addressingMethod, operandType);
	}

	/**
	 * Get register from RM bits of ModRM byte.
	 */
	private String registerFromModRM_RM(char addressingMethod, char[] operandType) throws CoreException {
		return getRegisterName(modRM.rm, addressingMethod, operandType);
	}

	/**
	 * return null if not found.
	 * 
	 * @return
	 */
	private String getSegmentRegisterFromPrefix() {
		String seg = null;
		for (int p : prefixes) {
			if (OpcodeX86.sPrefixesForOperand.contains(p)) {
				prefixesUsed.add(p);
				seg = AssemblyFormatterX86.formatPrefix(p);
			}
		}

		return seg;
	}
}
