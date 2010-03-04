package org.eclipse.cdt.debug.edc.disassembler;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IJumpToAddress;
import org.eclipse.cdt.debug.edc.JumpToAddress;

public interface IDisassembledInstruction {

	public IAddress getAddress();

	public boolean isValid();

	public int getSize();

	public String getMnemonics();

	/**
	 * Get {@link JumpToAddress} of the instruction. Return null for non
	 * control-change instruction.
	 * 
	 * @return
	 */
	public IJumpToAddress getJumpToAddress();

}