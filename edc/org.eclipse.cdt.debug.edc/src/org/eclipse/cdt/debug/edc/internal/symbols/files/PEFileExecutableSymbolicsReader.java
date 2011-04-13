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
import java.io.RandomAccessFile;
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
import org.eclipse.cdt.utils.coff.PE.NTOptionalHeader;
import org.eclipse.core.runtime.IPath;

/**
 * This class handles PE-COFF files for the purpose of supporting symbolics. 
 */
public class PEFileExecutableSymbolicsReader extends BaseExecutableSymbolicsReader {
	
	static public final String CODEVIEW_SECTION_NAME = "CodeView_Data";
	/** .CRT is another initialized data section utilized by the Microsoft C/C++ run-time libraries 
	 * @since 5.2
	 */
	public final static String _CRT = ".CRT"; //$NON-NLS-1$
	
	protected boolean isLE;
	protected Map<Integer, ISection> sectionsByPEID = new HashMap<Integer, ISection>();

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
		SectionHeader rDataHeader = null;
		int peSectionID = 0;

		for (SectionHeader s : secHeaders) {
			peSectionID++;
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
			else if (sectionName.equals(SectionHeader._DATA) || sectionName.equals(_CRT))
				name = ISection.NAME_DATA;
			else if (sectionName.equals(".rdata")) // add this name in SectionHeader ?
			{
				name = ISection.NAME_RODATA;
				rDataHeader = s;
			}
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
			Section newSection = new Section(id++, size, new Addr64(Long.toString(imageBase + s.s_vaddr)), props);
			sections.add(newSection);
			sectionsByPEID.put(peSectionID, newSection);
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
		
		if (rDataHeader != null)
			checkForCodeView(peFile, rDataHeader, imageBase, id);

		// now sort it by address for faster lookups
		Collections.sort(symbols);
	}

	private void checkForCodeView(PE peFile, SectionHeader rDataHeader, long imageBase, int id) throws IOException { //$NON-NLS-1$
		// figure out the file offset of the debug directory
		// entries
		final int IMAGE_DIRECTORY_ENTRY_DEBUG = 6;
		final int DEBUGDIRSZ = 28;
		NTOptionalHeader ntHeader = peFile.getNTOptionalHeader();
		if (ntHeader == null
				|| ntHeader.NumberOfRvaAndSizes < IMAGE_DIRECTORY_ENTRY_DEBUG)
			return;

		int debugDir = ntHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_DEBUG].VirtualAddress;
		if (debugDir == 0)
			return;

		int debugFormats = ntHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_DEBUG].Size / 28;
		if (debugFormats == 0)
			return;

		int offsetInto_rdata = debugDir
				- rDataHeader.s_vaddr;
		int fileOffset = rDataHeader.s_scnptr
				+ offsetInto_rdata;
		RandomAccessFile accessFile = new RandomAccessFile(
				binaryFile.toOSString(), "r");

		// loop through the debug directories looking for
		// CodeView (type 2)
		for (int j = 0; j < debugFormats; j++) {
			PE.IMAGE_DEBUG_DIRECTORY dir = new PE.IMAGE_DEBUG_DIRECTORY(
					accessFile, fileOffset);

			if ((dir.Type == 2) && (dir.SizeOfData > 0)) {
				// CodeView found, seek to actual data
				int debugBase = dir.PointerToRawData;
				accessFile.seek(debugBase);

				// sanity check. the first four bytes of the
				// CodeView
				// data should be "NB11"
				String s2 = accessFile.readLine();
				if (s2.startsWith("NB11")) { //$NON-NLS-1$
					// Attribute att = peFile.getAttribute();
					long start = debugBase;
					long size = accessFile.length() - start;
					
					String name = CODEVIEW_SECTION_NAME;
					IExecutableSection exeSection = new ExecutableSection(sectionMapper, name, 
							new SectionInfo(start, size));
					executableSections.put(name, exeSection);
				}
			}
			fileOffset += DEBUGDIRSZ;
		}
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
	
	public ISection getSectionByPEID(int peID)
	{
		return sectionsByPEID.get(peID);
	}

}
