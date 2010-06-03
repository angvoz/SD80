/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc;

public interface IJumpToAddress {

	/**
	 * Whether this address is the sole destination address after executing the
	 * current instruction. E.g. it's true for unconditional jump and subroutine
	 * call, but false for condition jump.
	 * 
	 * @return
	 */
	public boolean isSoleDestination();

	/**
	 * Whether the jump-to address is a subroutine address (namely whether the
	 * current instruction is a subroutine call instruction.
	 * 
	 * @return
	 */
	public boolean isSubroutineAddress();

	/**
	 * Is the address an immediate value (no calculation is needed) ?
	 * 
	 * @return
	 */
	public boolean isImmediate();

	/**
	 * Get the address.
	 * 
	 * @return IAddress object for immediate address, or an expression string
	 *         indicating how to calculate the actual address.
	 */
	public Object getValue();

}