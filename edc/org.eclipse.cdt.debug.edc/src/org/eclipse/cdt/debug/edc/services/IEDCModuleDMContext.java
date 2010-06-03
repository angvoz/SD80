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

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMContext;

public interface IEDCModuleDMContext extends IModuleDMContext, IEDCDMContext {

	/**
	 * Convert link address to runtime address.
	 * 
	 * @param linkAddress
	 * @return null if the given link address is not in the module.
	 */
	public IAddress toRuntimeAddress(IAddress linkAddress);

	/**
	 * Convert runtime address to link address.
	 * 
	 * @param runtimeAddress
	 * @return null if the given runtime address is not in the module.
	 */
	public IAddress toLinkAddress(IAddress runtimeAddress);

	/**
	 * Gets the symbol reader used to read symbols for this module.
	 * 
	 * @return the symbol reader
	 */
	public IEDCSymbolReader getSymbolReader();

}