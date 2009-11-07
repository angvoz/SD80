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
 * Interface representing a compile unit scope. A compile unit is a source or
 * header file which contains functions.
 */
public interface ICompileUnitScope extends IScope, ILineEntryProvider {

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

}
