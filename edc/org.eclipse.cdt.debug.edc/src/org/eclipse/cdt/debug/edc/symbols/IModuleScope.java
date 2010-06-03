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
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.services.IFrameRegisterProvider;
import org.eclipse.core.runtime.IPath;

/**
 * Interface representing a module scope. A module is a symbol file which
 * contains compile units.
 */
public interface IModuleScope extends IScope {

	/**
	 * Get the module described
	 */
	IPath getSymbolFile();
	
	/**
	 * Get the compile unit at the given link address
	 * 
	 * @param address
	 *            the link address
	 * @return the compile unit at the address, or null if none
	 */
	ICompileUnitScope getCompileUnitForAddress(IAddress linkAddress);

	/**
	 * Get the compile unit(s) for the given file
	 * 
	 * @param filePath
	 *            the file path as defined in the symbolics
	 * @return the compile unit for this file, possibly empty
	 */
	List<ICompileUnitScope> getCompileUnitsForFile(IPath filePath);
	
	/**
	 * Get all functions with the given name in any scope in the module
	 * 
	 * @param name
	 *            the function name, or <code>null</code> for all functions
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
	 * Get all the types declared in the module.
	 * <p>
	 * This does not load types on demand; each IType instance may be a proxy for a
	 * type loaded once methods are called on it.  Thus, do not use "instanceof" to
	 * check the type.
	 * @return unmodifiable list of types, which may be empty.
	 */
	Collection<IType> getTypes();
	
	/**
	 * Get the line entry provider for the module.  This aggregates the
	 * information for any source or header file referenced in the module.
	 * @return {@link IModuleLineEntryProvider}, never <code>null</code> 
	 */
	IModuleLineEntryProvider getModuleLineEntryProvider();
	
	/**
	 * Get a provider that allows access to registers stored in other stack frames.
	 * @return {@link IFrameRegisterProvider} or <code>null</code>
	 */
	IFrameRegisterProvider getFrameRegisterProvider();

}
