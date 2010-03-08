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
package org.eclipse.cdt.debug.edc.symbols;

import java.math.BigInteger;

import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface representing a variable value location.
 */
public interface IVariableLocation {

	/**
	 * Read the item's value
	 * @param bytes number of bytes to fetch 
	 * @return value, never <code>null</code>
	 * @throws CoreException on error
	 */
	BigInteger readValue(int bytes) throws CoreException;

	/**
	 * Create another location at the given offset from this location.
	 * @param offset
	 * @return {@link IVariableLocation}
	 */
	IVariableLocation addOffset(long offset);

	/**
	 * Get the name for the location, to show in the UI
	 * @param servicesTracker
	 * @return name
	 */
	String getLocationName(DsfServicesTracker servicesTracker);
}
