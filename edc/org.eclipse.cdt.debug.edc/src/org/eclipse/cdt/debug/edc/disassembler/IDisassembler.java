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

package org.eclipse.cdt.debug.edc.disassembler;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Generic interface for disassemblers.
 * 
 */
public interface IDisassembler {

	/**
	 * A known static value for assembly instruction parsers to use to
	 * indicate an instruction that cannot be parsed, and against which
	 * abstract/generic members of Disassembly services can compare.
	 * @since 2.0
	 */
	public static final String INVALID_OPCODE = "invalid opcode";
	
	/**
	 * Disassembler options that are common to all targets. Different targets
	 * may have its own disassembler options.
	 */
	public static interface IDisassemblerOptions {
		/*
		 * Option key names.
		 */
		public final static String GET_BRANCH_ADDRESS = "GetBranchAddress";
		public final static String GET_MNEMONICS = "GetMnemonics";

		// Following are sub-options when GetMnemonics is true.
		//
		/**
		 * Show address of the instruction in disassembler output.
		 */
		public final static String MNEMONICS_SHOW_ADDRESS = "ShowAddresses";
		/**
		 * Show original bytes of the instruction in disassembler output.
		 */
		public final static String MNEMONICS_SHOW_BYTES = "ShowBytes";
		/**
		 * Show symbol in the address in disassembler output.
		 */
		public final static String MNEMONICS_SHOW_SYMBOL = "ShowSymbol";

		/**
		 * Indicates that the address being disassembled is the PC
		 * @since 2.0
		 */
		public static final String ADDRESS_IS_PC = "AddressIsPC";
	}

	/**
	 * Disassemble one instruction at the beginning of the given byte array.
	 * 
	 * @param address
	 *            address of the code bytes
	 * @param code_bytes
	 *            memory bytes containing instructions.
	 * @param options
	 *            disassembler options.
	 * @param dmc
	 * 			  for context specific needs of the implementor (may be null)
	 * @return a {@link IDisassembledInstruction} object, null if no valid
	 *         instruction at the beginning of the code_bytes.
	 * @throws CoreException
	 * @since 2.0
	 */
	public IDisassembledInstruction disassembleOneInstruction(IAddress address, ByteBuffer code_bytes,
			Map<String, Object> options, IDisassemblyDMContext dmc) throws CoreException;

	/**
	 * Disassemble a block of memory.
	 * 
	 * @param start_address
	 *            address of the first byte in the "code_bytes"
	 * @param end_address
	 *            address of the byte after the last byte that is disassembled.
	 * @param code_bytes
	 *            memory bytes
	 * @param options
	 *            disassembler options.
	 * @param dmc
	 * 			  for context specific needs of the implementor (may be null)
	 * 
	 * @return a list of {@link IDisassembledInstruction} objects.
	 * @throws CoreException
	 * @since 2.0
	 */
	public List<IDisassembledInstruction> disassembleInstructions(IAddress start_address, IAddress end_address,
			ByteBuffer code_bytes, Map<String, Object> options, IDisassemblyDMContext dmc) throws CoreException;
}
