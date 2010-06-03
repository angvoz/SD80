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

import org.eclipse.cdt.core.IAddress;

/**
 * Interface representing a symbol from the symbol table
 */
public interface ISymbol extends Comparable<Object> {

	/**
	 * Get the symbol name
	 * 
	 * @return the symbol name
	 */
	String getName();

	/**
	 * Get the address of the symbol
	 * 
	 * @return the symbol address
	 */
	IAddress getAddress();

	/**
	 * Get the size of the symbol
	 * 
	 * @return the symbol size
	 */
	long getSize();

}
