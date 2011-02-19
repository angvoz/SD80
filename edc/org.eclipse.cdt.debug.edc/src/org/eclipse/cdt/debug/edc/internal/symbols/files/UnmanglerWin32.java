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

package org.eclipse.cdt.debug.edc.internal.symbols.files;

import org.eclipse.cdt.debug.edc.symbols.IUnmangler;

/**
 * Stub unmangler for Win32 symbols.  Big fat TODO
 */
public class UnmanglerWin32 implements IUnmangler {
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IUnmangler#undecorate(java.lang.String)
	 */
	public String undecorate(String symbol) {
		// remove standard underscore
		if (symbol.startsWith("_")) {
			symbol = symbol.substring(1);
		}
		// and DLL import prefix
		if (symbol.startsWith("_imp_")) {
			symbol = symbol.substring(5);
		}
		// and a stdcall/fastcall suffix
		int at = symbol.lastIndexOf('@');
		boolean isStdcall = false;
		if (at > 0) {
			try {
				Integer.parseInt(symbol.substring(at+1));
				isStdcall = true;
			} catch (NumberFormatException e) {
			}
		}
		if (isStdcall) {
			symbol = symbol.substring(0, at);
			
			// and a fastcall prefix
			if (symbol.startsWith("@")) 
				symbol = symbol.substring(1);
		}
		return symbol;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IUnmangler#isMangled(java.lang.String)
	 */
	public boolean isMangled(String symbol) {
		return symbol != null && symbol.startsWith("?");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IUnmangler#unmangleNameOnly(java.lang.String)
	 */
	public String unmangleWithoutArgs(String symbol) throws UnmanglingException {
		// big fat TODO
		return symbol;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IUnmangler#unmangle(java.lang.String)
	 */
	public String unmangle(String symbol) throws UnmanglingException {
		// big fat TODO
		return symbol;
	}
}
