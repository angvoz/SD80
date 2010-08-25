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
package org.eclipse.cdt.debug.edc.arm;

import org.eclipse.cdt.debug.edc.symbols.ISymbol;


/**
 * Interface representing a symbol from the ARM symbol table
 */
public interface IARMSymbol extends ISymbol {

	/**
	 * Is this symbol a Thumb address
	 * 
	 * @return true is Thumb, false if ARM
	 */
	boolean isThumbAddress();

}
