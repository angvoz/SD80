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
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * Interface to interpret the location of a variable at runtime.
 */
public interface ILocationProvider {

	/**
	 * Get the location of the variable for the given execution address
	 * 
	 * @param tracker
	 *            DSF services tracker to obtain needed services (registers,
	 *            memory, etc)
	 * @param stack
	 *            frame context to use for register based operations
	 * @param forLinkAddress
	 *            the links address for use if the variable can live at
	 *            different location depending on the execution context
	 * @return the variable location
	 */
	IVariableLocation getLocation(DsfServicesTracker tracker, IFrameDMContext context, IAddress forLinkAddress);

}
