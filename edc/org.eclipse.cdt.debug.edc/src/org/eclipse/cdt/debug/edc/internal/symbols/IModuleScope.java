/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.runtime.IPath;

/**
 * Interface representing a module scope. A module is a symbol file which
 * contains compile units.
 */
public interface IModuleScope extends IScope {

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
	 * @return the compile unit for this file, or null if none
	 */
	ICompileUnitScope getCompileUnitForFile(IPath filePath);
}
