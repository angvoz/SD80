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
package org.eclipse.cdt.debug.edc.symbols;

import org.eclipse.cdt.core.ISymbolReader;

/**
 * Top-level interface for getting symbolics information.  The executable
 * symbolics reader and IDebugInfoProvider (behind the scenes) work together
 * to provide the full information.  The symbolics
 * information includes debug information (e.g. DWARF, CODEVIEW) and symbol
 * table data. A binary file may only have symbol table without debug
 * information.
 */
public interface IEDCSymbolReader extends IExecutableSymbolicsReader, ISymbolReader {
	/**
	 * Call when the reader is no longer needed and should free up any resources.
	 */
	void shutDown();

	/**
	 * Check whether the symbol file has debug information recognized by this
	 * reader. The debug information here means data in the binary file that's
	 * specially for debugger, such as DWARF and CODEVIEW. The symbol table
	 * section data in many types of binary files is not considered debug
	 * information here.
	 * 
	 * @return true if the symbol reader has dedicated debug information
	 */
	boolean hasRecognizedDebugInformation();
	
	/**
	 * Get the module-level scope for the primary symbol file
	 * @return scope, never <code>null</code>
	 * @see #getSymbolFile()
	 */
	IModuleScope getModuleScope();
}
