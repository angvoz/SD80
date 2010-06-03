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
package org.eclipse.cdt.debug.edc.services;

import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;

public interface IEDCSymbols {
	/**
	 * Preference to show all variables that are defined as global by the current source file 
	 */
	public static final String SHOW_ALL_VARIABLES_ENABLED = "show_all_variables_enabled";

	/**
	 * Get the function at the given runtime address
	 * 
	 * @param context
	 *            the context
	 * @param runtimeAddress
	 *            the runtime address
	 * @return the function containing the given address, or null if none found
	 */
	public IFunctionScope getFunctionAtAddress(ISymbolDMContext context,
			IAddress runtimeAddress);

	/**
	 * Get the line entry at the given runtime address
	 * 
	 * @param context
	 *            the context
	 * @param runtimeAddress
	 *            the runtime address
	 * @return the line entry for the given address, or null if none found
	 */
	public ILineEntry getLineEntryForAddress(ISymbolDMContext context,
			IAddress runtimeAddress);

	/**
	 * <p>
	 * Get source line entries with code that are between the given start and
	 * end startAddress.
	 * <p>
	 * This method is created mainly for supporting disassembly service.
	 * 
	 * @param context
	 * @param start
	 *            start runtime address
	 * @param end
	 *            end runtime address (exclusive).
	 * @return list of source line entries which may or may not be in the same
	 *         source file (note that even one compile unit may have code from
	 *         different source files). It's empty if the start address has no
	 *         source line.
	 */
	public List<ILineEntry> getLineEntriesForAddressRange(
			ISymbolDMContext context, IAddress start, IAddress end);

}