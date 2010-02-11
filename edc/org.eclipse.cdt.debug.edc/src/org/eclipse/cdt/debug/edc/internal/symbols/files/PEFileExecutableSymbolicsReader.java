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

import org.eclipse.cdt.debug.edc.internal.symbols.ISection;
import org.eclipse.cdt.debug.edc.internal.symbols.Section;
import org.eclipse.cdt.debug.edc.internal.symbols.Symbol;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.cdt.utils.coff.Coff.SectionHeader;
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
		
		// may be null
		debugReader = DebugInfoProviderFactory.createFor(this);
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
		for (org.eclipse.cdt.utils.coff.Coff.Symbol symbol : peFile.getSymbols()) {
			symbols.add(new Symbol(symbol.toString(), new Addr32(symbol.n_value), 1));
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
}
