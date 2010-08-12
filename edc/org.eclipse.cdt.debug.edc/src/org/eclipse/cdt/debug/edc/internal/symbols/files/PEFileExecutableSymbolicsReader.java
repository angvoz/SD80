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

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.symbols.ISection;
import org.eclipse.cdt.debug.edc.internal.symbols.Section;
import org.eclipse.cdt.debug.edc.internal.symbols.Symbol;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSection;
import org.eclipse.cdt.debug.edc.symbols.ISymbol;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.cdt.utils.coff.Coff.SectionHeader;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.core.runtime.IPath;

/**
 * This class handles PE-COFF files for the purpose of supporting symbolics. 
 */
public class PEFileExecutableSymbolicsReader extends BaseExecutableSymbolicsReader {
	protected boolean isLE;

	public PEFileExecutableSymbolicsReader(IPath binaryFile, PE peFile) throws IOException {
		super(binaryFile);
		isLE = true;
		exeBaseAddress = new Addr32(peFile.getNTOptionalHeader().ImageBase);
		modificationDate = binaryFile.toFile().lastModified();
		
		sectionMapper = new SectionMapper(binaryFile, isLE);
		
		recordSections(peFile);
		
		// TODO: better selection.
		boolean isWin32 = false, isEABI = false;
		for (ISymbol symbol : symbols) {
			String symname = symbol.getName();
			if (symname.startsWith("__Z") && symname.endsWith("v")) {
				isWin32 = true;
				isEABI = true;
				break;
			} else if (symname.startsWith("_Z") && symname.endsWith("v")) {
				isEABI = true;
				break;
			} else if (symname.contains("@") && symname.contains("?")) {
				isWin32 = true;
				break;
			}
		}
		if (isWin32 && isEABI)
			unmangler = new UnmanglerWin32EABI();
		else if (isEABI)
			unmangler = new UnmanglerEABI();
		else
			unmangler = new UnmanglerWin32();
			
		
	}
	
	/**
	 * Determine the executable format and record the sections.
	 * @param peFile 
	 * 
	 * @throws IOException if file reading fails
	 */
	private void recordSections(PE peFile) throws IOException {
		// start from zero so that we can use it as index to the array list
		// for quick access.
		int id = 0;
		Map<String, Object> props;

		SectionHeader[] secHeaders = peFile.getSectionHeaders();
		long imageBase = peFile.getNTOptionalHeader().ImageBase & 0xffffffffL;

		for (SectionHeader s : secHeaders) {
			String name = new String(s.s_name).trim();
			if (name.startsWith("/")) //$NON-NLS-1$
			{
				int stringTableOffset = Integer.parseInt(name.substring(1));
				name = peFile.getStringTableEntry(stringTableOffset);
			}

			// Remember how to map this section
			if (executableSections.containsKey(name))
				throw new IllegalStateException("duplicate section " + name);
			IExecutableSection exeSection = new ExecutableSection(sectionMapper, name, 
					new SectionInfo(s.s_scnptr, s.s_paddr));
			executableSections.put(name, exeSection);
			
			String sectionName = name;
			// Convert the name to our unified name.
			if (sectionName.equals(SectionHeader._TEXT))
				name = ISection.NAME_TEXT;
			else if (sectionName.equals(SectionHeader._DATA))
				name = ISection.NAME_DATA;
			else if (sectionName.equals(".rdata")) // add this name in SectionHeader ?
				name = ISection.NAME_RODATA;
			else if (sectionName.equals(SectionHeader._BSS))
				name = ISection.NAME_BSS;
			else { // ignore other section.
				continue;
			}

			// Well, PE is a _modified_ version of COFF, where
			// section.s_paddr of COFF becomes "VirtualSize"
			// (memory size of the section) in PE, while s_size
			// is raw data size (file size of the section).
			long size = s.s_paddr; // not s_size !

			props = new HashMap<String, Object>();
			props.put(ISection.PROPERTY_NAME, name);

			// Note the s_vaddr is relative to image base.
			// For Section we need absolute address.
			sections.add(new Section(id++, size, new Addr64(Long.toString(imageBase + s.s_vaddr)), props));
		}

		// load the symbol table
		//
		/*
		 * Note this "rawSymbols" array contains both standard and auxiliary
		 * symbol records. It's assumed symbols in the array are in the same
		 * order they appear in the symbol table section, no sorting of any kind
		 * is done.
		 * 
		 * Actually auxiliary symbols should not be treated the same as standard
		 * symbols by Coff and PE in CDT core. But fixing that would break API,
		 * which is not allowed for CDT 7.0 at this time......... 04/07/10
		 */
		org.eclipse.cdt.utils.coff.Coff.Symbol[] rawSymbols = peFile.getSymbols();
		
		for (int i=0; i < rawSymbols.length; i++) {
			org.eclipse.cdt.utils.coff.Coff.Symbol symbol = rawSymbols[i];

			if (!(symbol.n_type == 0)) // Change to Coff.isNoSymbol for CDT 8.0.
			{
				String symName = symbol.getName(peFile.getStringTable());
				symbols.add(new Symbol(symName, new Addr32(symbol.n_value), 1));
			}

			// skip auxiliary symbol record(s) if any as otherwise they may
			// give us bogus match in any symbol table lookup.
			if (symbol.n_numaux > 0) {
				i += symbol.n_numaux; 
			}
		}

		// now sort it by address for faster lookups
		Collections.sort(symbols);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSymbolicsReader#getByteOrder()
	 */
	public ByteOrder getByteOrder() {
		return isLE ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.exe.IExecutableSymbolicsReader#getSymbolAtAddress()
	 */
	@Override
	public ISymbol getSymbolAtAddress(IAddress linkAddress) {
		int insertion = Collections.binarySearch(symbols, linkAddress);
		if (insertion >= 0) {
			return symbols.get(insertion++);
		}
	
		if (insertion == -1) {
			return null;
		}
	
		insertion = -insertion - 1;

		if (insertion == symbols.size()) {
			return null;
		}

		return symbols.get(insertion - 1);
	}

}
