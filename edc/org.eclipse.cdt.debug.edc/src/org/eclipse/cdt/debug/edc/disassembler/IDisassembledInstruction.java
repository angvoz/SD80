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