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

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IJumpToAddress;

/**
 * Class describing an instruction output from a disassembler.
 */
public class DisassembledInstruction implements IDisassembledInstruction {
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
	private IJumpToAddress jumpToAddress;

	public DisassembledInstruction() {
		address = null;
		size = 0;
		mnemonics = null;
		jumpToAddress = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.disassembler.IDisassembledInstruction#getAddress()
	 */
	public IAddress getAddress() {
		return address;
	}

	public void setAddress(IAddress address) {
		this.address = address;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.disassembler.IDisassembledInstruction#isValid()
	 */
	public boolean isValid() {
		return size > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.disassembler.IDisassembledInstruction#getSize()
	 */
	public int getSize() {
		return size;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.disassembler.IDisassembledInstruction#getMnemonics()
	 */
	public String getMnemonics() {
		return mnemonics;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.disassembler.IDisassembledInstruction#getJumpToAddress()
	 */
	public IJumpToAddress getJumpToAddress() {
		return jumpToAddress;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setMnemonics(String mnemonics) {
		this.mnemonics = mnemonics;
	}

	public void setJumpToAddress(IJumpToAddress jta) {
		this.jumpToAddress = jta;
	}

	@Override
	public String toString() {
		return "(length: " + size + ")  " + Integer.toHexString(address.getValue().intValue()) + ":  " + mnemonics
				+ (jumpToAddress != null ? "  [BranchAddress: " + jumpToAddress.toString() + "]" : "");
	}
}
