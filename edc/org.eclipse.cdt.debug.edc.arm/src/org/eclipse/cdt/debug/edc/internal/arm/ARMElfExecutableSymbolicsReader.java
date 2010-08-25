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
package org.eclipse.cdt.debug.edc.internal.arm;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.symbols.elf.Elf;
import org.eclipse.cdt.debug.edc.internal.symbols.files.ElfExecutableSymbolicsReader;
import org.eclipse.core.runtime.IPath;

/**
 * This class handles reading ELF files for the purposes of detecting symbolics.  
 */
public class ARMElfExecutableSymbolicsReader extends ElfExecutableSymbolicsReader {
	protected boolean isLE;

	public ARMElfExecutableSymbolicsReader(IPath binaryFile, Elf elf) throws IOException {
		super(binaryFile, elf);
	}
	
	protected void readSymbols(Elf elfFile) throws IOException {
		// load the symbol table
		elfFile.loadSymbols();
		Set<IAddress> symbolAddressSet = new TreeSet<IAddress>();
		
		for (Elf.Symbol symbol : elfFile.getSymtabSymbols()) {
			String name = symbol.toString();
			// Multiple symbol entries for the same address are generated.
			// Do not add duplicate symbols with 0 size to the list since it confuses
			// debugger
			if (name.length() > 0) {			
				if (symbol.st_size != 0 || !symbolAddressSet.contains(symbol.st_value)) {
					// need to get rid of labels with size 0
					if (symbol.st_size != 0 || !name.startsWith("|")) {
						symbols.add(new ARMSymbol(symbol.toString(), symbol.st_value, symbol.st_size));
						symbolAddressSet.add(symbol.st_value);
					}
				}
			}
		}
		
		// now sort it by address for faster lookups
		Collections.sort(symbols);
	}
}
