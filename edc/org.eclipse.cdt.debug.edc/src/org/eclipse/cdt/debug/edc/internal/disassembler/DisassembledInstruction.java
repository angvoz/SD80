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
package org.eclipse.cdt.debug.edc.internal.disassembler;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.JumpToAddress;

/**
 * Class describing an instruction output from a disassembler.
 */
public class DisassembledInstruction {
	// Address of the instruction
	private IAddress address;
	// size of instruction in 8-bit bytes
	private int size;
	// mnemonics, including instruction name & arguments. May include
	// address and raw bytes, depending on disassembler options.
	private String mnemonics;
	// jump-to-address for a control-change instruction (branch, call, ret,
	// etc.).
	// Null for the other instructions.
	private JumpToAddress jumpToAddress;

	public DisassembledInstruction() {
		address = null;
		size = 0;
		mnemonics = null;
		jumpToAddress = null;
	}

	public IAddress getAddress() {
		return address;
	}

	public void setAddress(IAddress address) {
		this.address = address;
	}

	public boolean isValid() {
		return size > 0;
	}

	public int getSize() {
		return size;
	}

	public String getMnemonics() {
		return mnemonics;
	}

	/**
	 * Get {@link JumpToAddress} of the instruction. Return null for non
	 * control-change instruction.
	 * 
	 * @return
	 */
	public JumpToAddress getJumpToAddress() {
		return jumpToAddress;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setMnemonics(String mnemonics) {
		this.mnemonics = mnemonics;
	}

	public void setJumpToAddress(JumpToAddress jta) {
		this.jumpToAddress = jta;
	}

	@Override
	public String toString() {
		return "(length: " + size + ")  " + Integer.toHexString(address.getValue().intValue()) + ":  " + mnemonics
				+ (jumpToAddress != null ? "  [BranchAddress: " + jumpToAddress.toString() + "]" : "");
	}
}
