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

import java.util.Collection;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.runtime.IPath;

/**
 * Interface representing a compile unit scope. A compile unit is a source or
 * header file which contains functions.
 */
public interface ICompileUnitScope extends IScope {

	/**
	 * Get the path to the compile unit file (source/header)
	 * 
	 * @return the file path as defined in the symbolics
	 */
	IPath getFilePath();

	/**
	 * Get the function at the given link address
	 * 
	 * @param linkAddress
	 *            the link address
	 * @return the function, or null if none found
	 */
	IFunctionScope getFunctionAtAddress(IAddress linkAddress);
	

	/**
	 * Get all the top-level functions in the compilation unit.
	 * (This ensures the CU is parsed, unlike {@link #getChildren()}.)
	 * @return the list of functions
	 * @return an unmodifiable list of functions, maybe empty
	 */
	Collection<IFunctionScope> getFunctions();

	/**
	 * Get the list of line table entries for the entire compile unit.
	 * These may represent contributions for multiple files (e.g.,
	 * sources and #included headers).
	 * 
	 * @return unmodifiable list of line entries, which may be empty
	 */
	Collection<ILineEntry> getLineEntries();
}
