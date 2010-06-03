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

import org.eclipse.cdt.dsf.debug.service.IInstruction;
import org.eclipse.cdt.dsf.debug.service.IMixedInstruction;

public class EDCMixedInstruction implements IMixedInstruction {

	private final String fileName;
	private final int lineNumber;
	private final IInstruction[] instructions;

	public EDCMixedInstruction(String fileName, int lineNumber, IInstruction[] instructions) {
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.instructions = instructions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.dsf.debug.service.IMixedInstruction#getFileName()
	 */
	public String getFileName() {
		return fileName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.dsf.debug.service.IMixedInstruction#getInstructions()
	 */
	public IInstruction[] getInstructions() {
		return instructions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.dsf.debug.service.IMixedInstruction#getLineNumber()
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("Source File: ").append(fileName).append("\n");
		buf.append(" line #").append(lineNumber).append(":\n");
		for (IInstruction i : instructions)
			buf.append("\t").append(i).append("\n");

		return buf.toString();
	}

}
