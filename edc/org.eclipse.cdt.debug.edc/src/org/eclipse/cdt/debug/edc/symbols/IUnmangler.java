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

import org.eclipse.cdt.debug.edc.internal.symbols.files.UnmanglingException;

/**
 * This interface supports unmangling symbols from executables
 * or debug information into the high-level language format.
 */
public interface IUnmangler {
	/**
	 * Undecorate a symbol which may have architecture or OS specific prefixes
	 * or suffixes independent of the actual mangling scheme underneath.
	 * @param symbol
	 * @return undecorated variant, possibly same as symbol
	 */
	String undecorate(String symbol);
	
	/**
	 * Tell if a symbol looks mangled
	 * @param symbol undecorated symbol
	 * @return true if it might be mangled, false if the symbol mangling is not recognized
	 * (or, it is the same as an extern "C" name)
	 */
	boolean isMangled(String symbol);
	
	/**
	 * Unmangle the symbol
	 * @param symbol undecorated symbol
	 * @return unmangled symbol 
	 * @throws UnmanglingException if symbol cannot be mangled 
	 */
	String unmangle(String symbol) throws UnmanglingException;
	
	/**
	 * Unmangle the symbol without args
	 * @param symbol
	 * @return the function name without args
	 * @throws UnmanglingException
	 * @since 2.0
	 */
	String unmangleWithoutArgs(String symbol) throws UnmanglingException;

}
