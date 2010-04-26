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
package org.eclipse.cdt.debug.edc.symbols;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This interface handles fetching debug information from a particular 
 * debug information format.
 */
public interface IDebugInfoProvider {
	/**
	 * Dispose resources held by the provider 
	 */
	void dispose();

	/**
	 * Get the absolute host path to the symbolics
	 * @return IPath, never <code>null</code>
	 */
	IPath getSymbolFile();
	
	/**
	 * Get the corresponding executable used for symbolics.  This may be different from the
	 * running executable.
	 */
	IExecutableSymbolicsReader getExecutableSymbolicsReader();

	/**
	 * Get the module scope, which contains the unified set of global code
	 * and symbols (and, implicitly, the types they reference) accessible at runtime. 
	 */
	IModuleScope getModuleScope();

	/**
	 * Get the paths to all source files known for the module.
	 * @return non-<code>null</code> array of build-time paths
	 */
	String[] getSourceFiles(IProgressMonitor monitor);
	
	/**
	 * Get all functions with the given name in any scope in the module
	 * 
	 * @param name
	 *            the function name
	 * @return unmodifiable list of functions, which may be empty
	 */
	Collection<IFunctionScope> getFunctionsByName(String name);

	/**
	 * Get all variables with the given name in any scope in the module
	 * 
	 * @param name
	 *            the variable name, or <code>null</code> to get all variables
	 * @param globalsOnly
	 *            whether to assume local variables in scope have been recorded
     *            and only globally-visible variables remain to be found
	 * @return unmodifiable list of variables, which may be empty
	 */
	Collection<IVariable> getVariablesByName(String name, boolean globalsOnly);

	/**
	 * Get the compile unit at the given link address
	 * 
	 * @param address
	 *            the link address
	 * @return the compile unit at the address, or null if none
	 */
	ICompileUnitScope getCompileUnitForAddress(IAddress linkAddress);

	/**
	 * Get the compile unit for the given file
	 * 
	 * @param filePath
	 *            the file path as defined in the symbolics
	 * @return a list of the compile units for this file, or null if none
	 */
	List<ICompileUnitScope> getCompileUnitsForFile(IPath filePath);
	
	/**
	 * Get the type with the given name
	 * @param name
	 * 		canonical base type name (trimmed, multiple	spaces replaced
	 * 		by single spaces, no "*" chars)
	 * @return unmodifiable list of types, which may be empty
	 */
	Collection<IType> getTypesByName(String name);

	/**
	 * Get all the types declared in the module (which may
     * include forward definitions).
	 * @return unmodifiable list of types, which may be empty.
	 */
	Collection<IType> getTypes();
}
