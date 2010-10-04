/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
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
import org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.symbols.IScope;


/**
 * information for an instruction, line, or block that can be used
 * when constructing EDCInstruction objects to collect and pass to
 * DisassemblyBackendDsf for display in the disassembly view.
 * @author kirk.beitz@nokia.com
 * @since 2.0
 */
public class EDCInstructionFunctionInfo {
	private final String functionName;
	private final IAddress functionStartAddress;

	/**
	 * simple constructor
	 * @param name function for the block this object represents
	 * @param address beginning of the function
	 */
	public EDCInstructionFunctionInfo(String name, IAddress address) {
		functionName = name;
		functionStartAddress = address;
	}

	/**
	 * constructor for a block whose function may not be known
	 * @param module container of the item for which to find function/address info
	 * @param line source location from LNT for which to find function/address info
	 */
	public EDCInstructionFunctionInfo(IEDCModuleDMContext module, ILineEntry line) {
		String name = null;
		IAddress start = null;
		IEDCSymbolReader reader = module.getSymbolReader();
		if (reader != null) {
			IScope scope = reader.getModuleScope().getScopeAtAddress(line.getLowAddress());
			while (scope != null && !(scope instanceof IFunctionScope)) {
				scope = scope.getParent();
			}

			if (scope != null) {
				name = scope.getName();
				start = ((IFunctionScope)scope).getLowAddress();
			}
		}
		functionName = name;
		functionStartAddress = start;
	}

	/**
	 * @return function name for block, null if never established
	 */
	public String getFunctionName() {
		return functionName;
	}

	/**
	 * @return starting address of function, null if never established
	 */
	public IAddress getFunctionStartAddress() {
		return functionStartAddress;
	}
}
