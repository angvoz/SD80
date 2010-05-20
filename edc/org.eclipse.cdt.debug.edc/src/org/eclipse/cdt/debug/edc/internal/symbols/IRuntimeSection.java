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
package org.eclipse.cdt.debug.edc.internal.symbols;

import org.eclipse.cdt.core.IAddress;

public interface IRuntimeSection extends ISection {

	static final String PROPERTY_RUNTIME_ADDRESS = "runtime_address"; //$NON-NLS-1$

	/**
	 * Get the base runtime address of the section
	 * 
	 * @return the base runtime address
	 */
	IAddress getRuntimeAddress();

	/**
	 * Relocates the section to the given runtime base address
	 * 
	 * @param runtimeAddress
	 *            the relocated base address of the section
	 */
	void relocate(IAddress runtimeAddress);

}
