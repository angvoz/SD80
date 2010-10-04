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
package org.eclipse.cdt.debug.edc.internal.arm;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.disassembler.EDCInstructionFunctionInfo;

public class RangeAndMode {

	private IAddress startAddress;
	private IAddress endAddress;

	private boolean thumbMode;
	private boolean hasSymbols;

	private EDCInstructionFunctionInfo wholeFunctionInfo;

	public RangeAndMode(IAddress start, IAddress end, boolean thumb, boolean symbols) {
		startAddress = start;
		endAddress = end;
		thumbMode = thumb;
		hasSymbols = symbols;
		wholeFunctionInfo = null;
	}

	public void setThumbMode(boolean thumbMode) {
		this.thumbMode = thumbMode;
	}

	public boolean isThumbMode() {
		return thumbMode;
	}

	public void setHasSymbols(boolean hasSymbols) {
		this.hasSymbols = hasSymbols;
	}

	public boolean hasSymbols() {
		return hasSymbols;
	}

	public void setStartAddress(IAddress startAddress) {
		this.startAddress = startAddress;
	}

	public IAddress getStartAddress() {
		return startAddress;
	}

	public void setEndAddress(IAddress endAddress) {
		this.endAddress = endAddress;
	}

	public IAddress getEndAddress() {
		return endAddress;
	}

	public EDCInstructionFunctionInfo getFunctionInfo() {
		return wholeFunctionInfo;
	}

	public void setFunctionInfo(EDCInstructionFunctionInfo wholeFunctionInfo) {
		this.wholeFunctionInfo = wholeFunctionInfo;
	}

	@Override
	public String toString() {
		return "start = " + (startAddress != null ? startAddress.toHexAddressString() : "null")
			 + ", end = " + (endAddress   != null ? endAddress.toHexAddressString()   : "null")
			 + (thumbMode ? ", thumb" : "") + (hasSymbols ? ", symbols" : ", no symbols");
	}

}
