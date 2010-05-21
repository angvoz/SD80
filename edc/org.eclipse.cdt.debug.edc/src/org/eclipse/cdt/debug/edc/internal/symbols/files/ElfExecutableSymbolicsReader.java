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
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.symbols.ISection;
import org.eclipse.cdt.debug.edc.internal.symbols.Section;
import org.eclipse.cdt.debug.edc.internal.symbols.Symbol;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSection;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.PHdr;
import org.eclipse.core.runtime.IPath;

/**
 * This class handles reading ELF files for the purposes of detecting symbolics.  
 */
public class ElfExecutableSymbolicsReader extends BaseExecutableSymbolicsReader {
	protected boolean isLE;

	public ElfExecutableSymbolicsReader(IPath binaryFile, Elf elf) throws IOException {
		super(binaryFile);
		
		Elf.ELFhdr header = elf.getELFhdr();
		isLE = header.e_ident[Elf.ELFhdr.EI_DATA] == Elf.ELFhdr.ELFDATA2LSB;
		exeBaseAddress = getExeSegment(elf).p_vaddr;
		modificationDate = binaryFile.toFile().lastModified();
		
		sectionMapper = new SectionMapper(binaryFile, isLE);
		
		recordSections(elf);
		readSymbols(elf);
		
		// TODO: better selection.  We assume for now that all ELF targets we know about (ARM, Linux) use the same mangling.
		unmangler = new UnmanglerEABI();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ELF symbolics reader for " + binaryFile; //$NON-NLS-1$
	}
	
	/**
	 * Determine the executable format and record the sections.
	 * @param elfFile 
	 * 
	 * @throws IOException if file contents cannot be read
	 */
	private void recordSections(Elf elfFile) throws IOException {
		
		// start from zero so that we can use it as index to the array list
		// for quick access.
		int id = 0;
		Map<String, Object> props;

		// Use segments instead of sections in the Elf file.
		PHdr[] segments = elfFile.getPHdrs();

		for (PHdr s : segments) {
			if (s.p_type == PHdr.PT_LOAD) {
				props = new HashMap<String, Object>();

				if ((s.p_flags & PHdr.PF_X) != 0)
					props.put(ISection.PROPERTY_NAME, ISection.NAME_TEXT);
				else
					// There is no clear way to tell if a segment is
					// data or bss segment.
					props.put(ISection.PROPERTY_NAME, ISection.NAME_DATA);

				Section section = new Section(id++, s.p_memsz, s.p_vaddr, props);
				sections.add(section);
			}
		}
		
		// remember how to map the sections
		Elf.Section[] sections = elfFile.getSections();
		for (org.eclipse.cdt.utils.elf.Elf.Section section : sections) {
			String name = section.toString();
			
			if (name.length() > 0) {
				if (executableSections.containsKey(name))
					throw new IllegalStateException("duplicate section " + name);
				IExecutableSection exeSection = new ExecutableSection(sectionMapper, name, 
						new SectionInfo(section.sh_offset, section.sh_size));
				executableSections.put(name, exeSection);
			}
		}
	}
	
	private void readSymbols(Elf elfFile) throws IOException {
		// load the symbol table
		elfFile.loadSymbols();
		Set<IAddress> symbolAddressSet = new TreeSet<IAddress>();
		for (org.eclipse.cdt.utils.elf.Elf.Symbol symbol : elfFile.getSymtabSymbols()) {
			String name = symbol.toString();
			// Multiple symbol entries for the same address are generated.
			// Do not add duplicate symbols with 0 size to the list since it confuses
			// debugger
			if (name.length() > 0) {			
				if (symbol.st_size != 0 || !symbolAddressSet.contains(symbol.st_value)) {
					// need to get rid of labels with size 0
					if (symbol.st_size != 0 || !name.startsWith("|")) {
						Symbol sym = new Symbol(symbol.toString(), symbol.st_value, symbol.st_size);
						symbols.add(sym);
						symbolAddressSet.add(symbol.st_value);
					}
				}
			}
		}
		
		// now sort it by address for faster lookups
		Collections.sort(symbols);
	}
	
	/**
	 * Find the executable (text) segment of the elf file, assuming there is
	 * only one that segment.
	 * 
	 * @param elf
	 * @return exe segment header or null on error.
	 * @throws IOException
	 */
	private PHdr getExeSegment(Elf elf) throws IOException {
		PHdr[] segments = elf.getPHdrs();

		for (PHdr s : segments) {
			if (s.p_type == PHdr.PT_LOAD && ((s.p_flags & PHdr.PF_X) != 0))
				return s;
		}

		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSymbolicsReader#getByteOrder()
	 */
	public ByteOrder getByteOrder() {
		return isLE ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
	}
	
}
