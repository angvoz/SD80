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

package org.eclipse.cdt.debug.edc.internal;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.utils.Addr64;

/**
 * Representing the address to which control-change instruction (e.g. a jump
 * instruction or subroutine call instruction) may jump to. <br>
 * Contrast to: the address of the instruction right after this instruction in
 * memory.<br>
 * <br>
 * Object of such class is supposed to be produced by disassembler and consumed
 * by debugger for tasks such as stepping and stack crawling.<br>
 * <br>
 * For some jump or call instructions, the jump-to-address is an absolute
 * immediate address encoded in the instruction, while for some instructions it
 * is an indirect address whose value has to be computed by reading certain
 * registers or memory when control is at the instruction. The computation
 * detail is target processor dependent.
 */
public class JumpToAddress {
	public static final String EXPRESSION_RETURN_NEAR = "ret-near";
	public static final String EXPRESSION_RETURN_FAR = "ret-far";

	private final IAddress address;
	private final String expression;
	private final boolean isSoleDestination;
	private final boolean isSubroutineAddress;

	public JumpToAddress(IAddress address, boolean isSoleDestination, boolean isSubroutineAddress) {
		this.address = address;
		this.expression = null;
		this.isSoleDestination = isSoleDestination;
		this.isSubroutineAddress = isSubroutineAddress;
	}

	public JumpToAddress(long address, boolean isSoleDestination, boolean isSubroutineAddress) {
		this(new Addr64(Long.toString(address)), isSoleDestination, isSubroutineAddress);
	}

	public JumpToAddress(String expression, boolean isSoleDestination, boolean isSubroutineAddress) {
		this.address = null;
		this.expression = expression;
		this.isSoleDestination = isSoleDestination;
		this.isSubroutineAddress = isSubroutineAddress;
	}

	/**
	 * Whether this address is the sole destination address after executing the
	 * current instruction. E.g. it's true for unconditional jump and subroutine
	 * call, but false for condition jump.
	 * 
	 * @return
	 */
	public boolean isSoleDestination() {
		return isSoleDestination;
	}

	/**
	 * Whether the jump-to address is a subroutine address (namely whether the
	 * current instruction is a subroutine call instruction.
	 * 
	 * @return
	 */
	public boolean isSubroutineAddress() {
		return isSubroutineAddress;
	}

	/**
	 * Is the address an immediate value (no calculation is needed) ?
	 * 
	 * @return
	 */
	public boolean isImmediate() {
		return address != null;
	}

	/**
	 * Get the address.
	 * 
	 * @return IAddress object for immediate address, or an expression string
	 *         indicating how to calculate the actual address.
	 */
	public Object getValue() {
		if (address != null)
			return address;
		else {
			assert expression != null;
			return expression;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + (isSoleDestination ? 1231 : 1237);
		result = prime * result + (isSubroutineAddress ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JumpToAddress other = (JumpToAddress) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (isSoleDestination != other.isSoleDestination)
			return false;
		if (isSubroutineAddress != other.isSubroutineAddress)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JumpToAddress [address=" + address + ", expression=" + expression + ", isSoleDestination="
				+ isSoleDestination + ", isSubroutineAddress=" + isSubroutineAddress + "]";
	}
}
