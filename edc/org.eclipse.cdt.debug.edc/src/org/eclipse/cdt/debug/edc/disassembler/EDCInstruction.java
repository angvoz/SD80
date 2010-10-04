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

import org.eclipse.cdt.dsf.debug.service.AbstractInstruction;
import org.eclipse.cdt.dsf.debug.service.IInstruction;

public class EDCInstruction extends AbstractInstruction {

	private final IDisassembledInstruction instruction;
	private String functionName;
	private int offset;

	public EDCInstruction(IDisassembledInstruction instruction) {
		this.instruction = instruction;
		functionName = null;
		offset = 0;
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

	/** @since 2.0 */
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.dsf.debug.service.IInstruction#getFuntionName()
	 */
	public String getFuntionName() {
		return functionName;
	}
	/**
	 * not a true override; the name is "misspelled" in {@link IInstruction}
	 * @since 2.0
	 * @see org.eclipse.cdt.dsf.debug.service.IInstruction#getFuntionName()
	 */
	public String getFunctionName() {
		return functionName;
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

	/** @since 2.0 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.dsf.debug.service.IInstruction#getOffset()
	 */
	public long getOffset() {
		return offset;
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

	/**
	 * needed for DisassemblyBackendDsf#insertDisassembly() ;
	 * for HEAD see the first reference; for CDT_7_0, see the second reference
	 * @see org.eclipse.cdt.dsf.debug.service.IInstruction#getSize()
	 * @see org.eclipse.cdt.dsf.debug.internal.provisional.service.IInstructionWithSize#getSize()
	 * @since 2.0
	 */
	public Integer getSize() {
		return (Integer)instruction.getSize();
	}
	
	@Override
	public String toString() {
		return instruction.toString();
	}
}
