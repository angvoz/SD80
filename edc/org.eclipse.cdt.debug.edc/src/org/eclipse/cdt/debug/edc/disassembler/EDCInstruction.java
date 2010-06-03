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

import java.math.BigInteger;
import java.util.StringTokenizer;

import org.eclipse.cdt.dsf.debug.service.IInstruction;

public class EDCInstruction implements IInstruction {

	private final IDisassembledInstruction instruction;

	public EDCInstruction(IDisassembledInstruction instruction) {
		this.instruction = instruction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.dsf.debug.service.IInstruction#getAdress()
	 */
	public BigInteger getAdress() {
		return instruction.getAddress().getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.dsf.debug.service.IInstruction#getArgs()
	 */
	public String getArgs() {
		// It's assumed the disassembler does not put extras like address
		// and raw bytes in the output.
		String ret = null;
		String asm = instruction.getMnemonics();
		StringTokenizer tzer = new StringTokenizer(asm);
		if (tzer.countTokens() == 1) { // no arguments
			return ret;
		} else {
			tzer.nextToken(); // skip the instruction name
			ret = tzer.nextToken();
			while (tzer.hasMoreTokens())
				ret += " " + tzer.nextToken();
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.dsf.debug.service.IInstruction#getFuntionName()
	 */
	public String getFuntionName() {
		// TODO add later
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.dsf.debug.service.IInstruction#getInstruction()
	 */
	public String getInstruction() {
		// Hmm, this actually needs the whole instruction.
		return instruction.getMnemonics();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.dsf.debug.service.IInstruction#getOffset()
	 */
	public long getOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.dsf.debug.service.IInstruction#getOpcode()
	 */
	public String getOpcode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return instruction.toString();
	}
}
