/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of the License "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/

package org.eclipse.cdt.debug.edc.internal.symbols.newdwarf;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.IEDCTraceOptions;
import org.eclipse.cdt.debug.edc.internal.IStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.MemoryStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.PathUtils;
import org.eclipse.cdt.debug.edc.internal.symbols.ArrayBoundType;
import org.eclipse.cdt.debug.edc.internal.symbols.ArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.CPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ClassType;
import org.eclipse.cdt.debug.edc.internal.symbols.ConstType;
import org.eclipse.cdt.debug.edc.internal.symbols.Enumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.Enumerator;
import org.eclipse.cdt.debug.edc.internal.symbols.FieldType;
import org.eclipse.cdt.debug.edc.internal.symbols.FunctionScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.IBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.internal.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.IScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ISection;
import org.eclipse.cdt.debug.edc.internal.symbols.IType;
import org.eclipse.cdt.debug.edc.internal.symbols.IVariable;
import org.eclipse.cdt.debug.edc.internal.symbols.InheritanceType;
import org.eclipse.cdt.debug.edc.internal.symbols.LexicalBlockScope;
import org.eclipse.cdt.debug.edc.internal.symbols.LineEntry;
import org.eclipse.cdt.debug.edc.internal.symbols.PointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.ReferenceType;
import org.eclipse.cdt.debug.edc.internal.symbols.Scope;
import org.eclipse.cdt.debug.edc.internal.symbols.StructType;
import org.eclipse.cdt.debug.edc.internal.symbols.SubroutineType;
import org.eclipse.cdt.debug.edc.internal.symbols.TypedefType;
import org.eclipse.cdt.debug.edc.internal.symbols.UnionType;
import org.eclipse.cdt.debug.edc.internal.symbols.VolatileType;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSection;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSymbolicsReader;
import org.eclipse.cdt.debug.edc.internal.symbols.newdwarf.DwarfDebugInfoProvider.AbbreviationEntry;
import org.eclipse.cdt.debug.edc.internal.symbols.newdwarf.DwarfDebugInfoProvider.Attribute;
import org.eclipse.cdt.debug.edc.internal.symbols.newdwarf.DwarfDebugInfoProvider.AttributeList;
import org.eclipse.cdt.debug.edc.internal.symbols.newdwarf.DwarfDebugInfoProvider.AttributeValue;
import org.eclipse.cdt.debug.edc.internal.symbols.newdwarf.DwarfDebugInfoProvider.CompilationUnitHeader;
import org.eclipse.cdt.debug.edc.internal.symbols.newdwarf.DwarfDebugInfoProvider.ForwardTypeReference;
import org.eclipse.cdt.debug.edc.internal.symbols.newdwarf.DwarfDebugInfoProvider.PublicNameInfo;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.core.runtime.IPath;

/**
 * Handle restartable parsing of Dwarf information from a single module.  
 * This class may be instantiated multiple times to parse specific subsets 
 * of the Dwarf data.  The {@link DwarfDebugInfoProvider}
 * holds the global state of everything parsed so far.
 */
public class DwarfInfoReader {

	static public boolean DEBUG = false;
	
	// TODO 64-bit Dwarf currently unsupported

	/* Section names. */
	public final static String DWARF_DEBUG_INFO      = ".debug_info"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_RANGES   = ".debug_ranges"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_ABBREV    = ".debug_abbrev"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_ARANGES   = ".debug_aranges"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_LINE      = ".debug_line"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_FRAME     = ".debug_frame"; //$NON-NLS-1$
	public final static String DWARF_EH_FRAME        = ".eh_frame"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_LOC       = ".debug_loc"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_PUBNAMES  = ".debug_pubnames"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_STR       = ".debug_str"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_FUNCNAMES = ".debug_funcnames"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_TYPENAMES = ".debug_typenames"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_VARNAMES  = ".debug_varnames"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_WEAKNAMES = ".debug_weaknames"; //$NON-NLS-1$
	public final static String DWARF_DEBUG_MACINFO   = ".debug_macinfo"; //$NON-NLS-1$

	private Map<Long, List<LocationEntry>> locationEntriesByOffset = new HashMap<Long, List<LocationEntry>>();

	// the target for all the reading
	private DwarfDebugInfoProvider provider;
	
	private IExecutableSymbolicsReader exeReader;
	private DwarfModuleScope moduleScope;
	private IPath symbolFilePath;
	
	private IExecutableSection debugInfoSection;
	private IExecutableSection publicNamesSection;
	private CompilationUnitHeader currentCUHeader;
	
	private Map<IType, IType> typeToParentMap;
	private IType currentParentType;
	//private ForwardDwarfDefinition currentParent;
	private Scope currentParentScope;
	private DwarfCompileUnit currentCompileUnitScope;

	private DwarfFileHelper fileHelper;

	private RangeList codeRanges;
	
	/**
	 * Create a reader for the provider.  This constructor and any methods 
	 * on this reader class will incrementally update the provider.
	 * @param provider
	 */
	public DwarfInfoReader(DwarfDebugInfoProvider provider) {
		this.provider = provider;
		exeReader = provider.getExecutableSymbolicsReader();
		symbolFilePath = provider.getSymbolFile();
		fileHelper = provider.fileHelper;
		moduleScope = (DwarfModuleScope) provider.getModuleScope();
		debugInfoSection = exeReader.findExecutableSection(DWARF_DEBUG_INFO);
		publicNamesSection = exeReader.findExecutableSection(DWARF_DEBUG_PUBNAMES);
		
		codeRanges = getCodeRanges();
	}

	/**
	 * @return
	 */
	private RangeList getCodeRanges() {
		RangeList codeRanges = new RangeList();
		for (ISection section : exeReader.getSections()) {
			if (section.getProperties().get(ISection.PROPERTY_NAME).equals(ISection.NAME_TEXT)) {
				long start = section.getLinkAddress().getValue().longValue();
				long size = section.getSize();
				codeRanges.addRange(start, start + size);
			}
		}
		return codeRanges;
	}

	protected IStreamBuffer getDwarfSection(String sectionName) {
		// the exe reader and section already handle caching this
		IStreamBuffer buffer = null;
		IExecutableSection section = exeReader.findExecutableSection(sectionName);
		if (section != null) {
			buffer = section.getBuffer();
		}
		return buffer;
	}


	/** 
	 * Parse top-level debugging information about compilation units and globally
	 * visible objects, but do not expand or gather data about other objects in
 	 * compilation units.  
	 */
	public void parseInitial() {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, "Initial parse for " + symbolFilePath);
		parseCUDebugInfo();
		parsePublicNames();
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, "Finished initial parse");
	}
	
	/**
	 * Parse all computation units for addresses
	 */
	public void parseForAddresses() {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, "Address parse for " + symbolFilePath);
		for (DwarfCompileUnit compileUnit : provider.compileUnits) {
			parseCompilationUnitForAddresses(compileUnit);
		}
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, "Finished address parse");
		
		moduleScope.fixupRanges(Addr32.ZERO);

		if (DEBUG) {
			dumpSymbols();
		}

	}
	
	/**
	 * Parse names in the .debug_pubnames section
	 */
	private void parsePublicNames() {

		if (publicNamesSection == null || debugInfoSection == null) { // no public names and/or debug info 
			return;
		}

		IStreamBuffer bufferPublicNames = publicNamesSection.getBuffer();
		if (bufferPublicNames == null)
			return;

		IStreamBuffer bufferDebuginfo = debugInfoSection.getBuffer();
		if (bufferDebuginfo == null)
			return;

		long fileIndex = 0;
		long fileEndIndex = bufferPublicNames.capacity();

		// parse all the sets in the .debug_pubnames section
		while (fileIndex < fileEndIndex) {
			fileIndex = parsePublicNamesSet(bufferPublicNames, fileIndex, bufferDebuginfo);
		}
	}
	
	/**
	 * Parse one set of global objects and functions
	 */
	private long parsePublicNamesSet(IStreamBuffer bufferPublicNames, long fileIndex, IStreamBuffer bufferDebugInfo) {
		bufferPublicNames.position(fileIndex);

		// get the set's data length
		int setLength = bufferPublicNames.getInt();

		// get the entire set

		IStreamBuffer dataPublicNames = bufferPublicNames.wrapSubsection(setLength);

		// get header info for set of public names
		// skip over Dwarf version
		dataPublicNames.position(2);
		int debugInfoOffset = dataPublicNames.getInt();
		int debugInfoLength = dataPublicNames.getInt();

		try {
			// read the entire compile unit
			bufferDebugInfo.position(debugInfoOffset);

			IStreamBuffer dataInfoBytes = bufferDebugInfo.wrapSubsection(debugInfoLength);
			
			CompilationUnitHeader header = provider.debugOffsetsToCompileUnits.get(Long.valueOf(debugInfoOffset));
			
			// get stored abbrev table, or read and parse an abbrev table
			Map<Long, AbbreviationEntry> abbrevs = parseDebugAbbreviation(header.abbreviationOffset);

			// read names and corresponding types
			// ignore the 4 bytes of 0 at the end of the set
			while (dataPublicNames.remaining() > 4) {
				// read the object offset and name
				int objectOffset = dataPublicNames.getInt();
				String name = readString(dataPublicNames);
				
				// read the type of object
				dataInfoBytes.position(objectOffset);
				long code = read_unsigned_leb128(dataInfoBytes);
				AbbreviationEntry entry = abbrevs.get(new Long(code));
				
				if (entry != null) {
					String baseName = name;
					int baseStart = name.lastIndexOf("::");
					if (baseStart != -1)
						baseName = name.substring(baseStart + 2);

					if (entry.tag == DwarfConstants.DW_TAG_variable) {
						List<PublicNameInfo> variables = provider.publicVariables.get(name);
						if (variables == null) {
							variables = new ArrayList<PublicNameInfo>();
						}
						variables.add(new PublicNameInfo(name, header, entry.tag));
						provider.publicVariables.put(baseName, variables);
					} else if (entry.tag == DwarfConstants.DW_TAG_subprogram) {
						List<PublicNameInfo> functions = provider.publicFunctions.get(name);
						if (functions == null) {
							functions = new ArrayList<PublicNameInfo>();
						}
						functions.add(new PublicNameInfo(name, header, entry.tag));
						provider.publicFunctions.put(baseName, functions);
						
					}
				}
			}
		} catch (IOException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}

		return fileIndex + setLength + 4;
	}

	/**
	 * Parse all computation units for types
	 */
	public void parseForTypes() {
		for (DwarfCompileUnit compileUnit : provider.compileUnits) {
			parseCompilationUnitForTypes(compileUnit);
		}
	}
	/**
	 * Parse compilation unit headers and top-level info in the .debug_info section
	 */
	private void parseCUDebugInfo() {

		if (debugInfoSection == null) {	// no dwarf data.
			return;
		}
		
		// if we haven't built the referenced files list from a quick parse yet,
		// flag it here so we can build the file list as we parse.
		if (provider.referencedFiles.isEmpty()) {
			provider.buildReferencedFilesList = true;
		}

		IStreamBuffer buffer = debugInfoSection.getBuffer();
		if (buffer != null) {
			long fileIndex = 0;
			long fileEndIndex = buffer.capacity();
			
			while (fileIndex < fileEndIndex) {
				fileIndex = parseCompilationUnitShallow(buffer, fileIndex);
			}
		}
		provider.compileUnits.trimToSize();
		provider.buildReferencedFilesList = false;
	}

	/**
	 * Parse the header and top-level attributes of one compilation unit 
	 * @return offset of next compilation unit
	 */
	public long parseCompilationUnitShallow(IStreamBuffer buffer, long fileIndex) {
		buffer.position(fileIndex);

		currentCUHeader = new CompilationUnitHeader();

		// read the length of the compile unit from the file
		currentCUHeader.length = buffer.getInt();

		// now read the whole compile unit into memory. note that we're
		// reading the whole section including the size that we already
		// read because other code will use the offset of the buffer as
		// the offset of the section to store things by offset (types,
		// function declarations, etc).
		buffer.position(fileIndex);
		
		IStreamBuffer data = buffer.wrapSubsection(currentCUHeader.length + 4);

		// skip over the length since we already know it
		data.position(4);

		currentCUHeader.version = data.getShort();
		currentCUHeader.abbreviationOffset = data.getInt();
		currentCUHeader.addressSize = data.get();
		currentCUHeader.debugInfoOffset = (int) fileIndex;

		try {
			// get stored abbrev table, or read and parse an abbrev table
			Map<Long, AbbreviationEntry> abbrevs = parseDebugAbbreviation(currentCUHeader.abbreviationOffset);
			
			// read only the compile unit's attribute list, ignoring its children
			long code = read_unsigned_leb128(data);
			AbbreviationEntry entry = abbrevs.get(Long.valueOf(code));
			
			AttributeList attributeList = new AttributeList(entry, data, currentCUHeader.addressSize, getDebugStrings());
			processCompileUnit(currentCUHeader, entry.hasChildren, attributeList);
			
		} catch (IOException e) {
			EDCDebugger.getMessageLogger().logError("Failed to parse debug info from section " 
					+ debugInfoSection.getName() + " in file " + symbolFilePath, e);
		}
		
		// skip past the compile unit. note that the
		// currentCUHeader.length does not include
		// the size of the unit length itself
		fileIndex += currentCUHeader.length + 4;		
		
		return fileIndex;
	}
	
	/**
	 * Given compilation unit, parse to get variables and all children that have address ranges.
	 * 
	 * @param childrenposition
	 */
	public void parseCompilationUnitForAddresses(DwarfCompileUnit compileUnit) {
		if (compileUnit.isParsedForAddresses())
			return;
		
		compileUnit.setParsedForAddresses(true);
		
		CompilationUnitHeader header = compileUnit.header;

		if (header == null)
			return;

		trace(IEDCTraceOptions.SYMBOL_READER_TRACE, "Address parse for " + Integer.toHexString(header.debugInfoOffset) + " : " + header.scope.getFilePath());
		
		IStreamBuffer buffer = debugInfoSection.getBuffer();
		
		if (buffer == null)
			return;
		
		int fileIndex = header.debugInfoOffset;
		
		// read the compile unit debug info into memory
		buffer.position(fileIndex);

		IStreamBuffer data = buffer.wrapSubsection(header.length + 4);

		// skip over the header, since we've already read it
		data.position(11); // unit length + version + abbrev table offset + address size
		
		currentCompileUnitScope = compileUnit;
		currentParentScope = compileUnit;
		registerScope(header.debugInfoOffset, compileUnit);
		currentCUHeader = header;

		try {
			// get stored abbrev table, or read and parse an abbrev table
			Map<Long, AbbreviationEntry> abbrevs = parseDebugAbbreviation(header.abbreviationOffset);

			parseForAddresses(data, abbrevs, header, new Stack<Long>());
		} catch (IOException e) {
			EDCDebugger.getMessageLogger().logError("Failed to parse debug info from section " 
					+ debugInfoSection.getName() + " in file " + symbolFilePath, e);
		}
	}

	public void parseCompilationUnitForTypes(DwarfCompileUnit compileUnit) {
		if (compileUnit.isParsedForTypes())
			return;
		
		compileUnit.setParsedForTypes(true);
		
		CompilationUnitHeader header = compileUnit.header;

		if (header == null)
			return;

		trace(IEDCTraceOptions.SYMBOL_READER_TRACE, "Type parse of " + Integer.toHexString(header.debugInfoOffset) + " : " + header.scope.getFilePath());
		
		IStreamBuffer buffer = debugInfoSection.getBuffer();
		
		if (buffer == null)
			return;
		
		int fileIndex = header.debugInfoOffset;
		
		// read the compile unit debug info into memory
		buffer.position(fileIndex);

		IStreamBuffer data = buffer.wrapSubsection(header.length + 4);

		// skip over the header, since we've already read it
		data.position(11); // unit length + version + abbrev table offset + address size
		
		currentCompileUnitScope = compileUnit;
		currentParentScope = compileUnit;
		registerScope(header.debugInfoOffset, compileUnit);
		currentCUHeader = header;

		try {
			// get stored abbrev table, or read and parse an abbrev table
			Map<Long, AbbreviationEntry> abbrevs = parseDebugAbbreviation(header.abbreviationOffset);

			parseForTypes(data, abbrevs, header,
						new Stack<Long>());
		} catch (IOException e) {
			EDCDebugger.getMessageLogger().logError("Failed to parse type debug info from section " 
					+ debugInfoSection.getName() + " in file " + symbolFilePath, e);
		}
	}
	
	/**
	 * Does a quick parse of the .debug_info section just to get a list of
	 * referenced files from the compile units.
	 */
	public void quickParseDebugInfo() {

		if (debugInfoSection == null) {	// no dwarf data.
			return;
		}
		
		// get the compile units out of the .debug_info section
		IStreamBuffer buffer = debugInfoSection.getBuffer();
		if (buffer == null) 
			return;
		
		try {

			long fileIndex = 0;
			long fileEndIndex = buffer.capacity();

			buffer.position(0);
			while (fileIndex < fileEndIndex) {
				buffer.position(fileIndex);
				
				int unit_length = buffer.getInt();

				buffer.position(fileIndex + 4);

				IStreamBuffer data = buffer.wrapSubsection(unit_length);
				
				data.position(2); // skip version
				int debug_abbrev_offset = data.getInt();
				byte address_size = data.get();

				// get the abbreviation entry for the compile unit
				// find the offset to the
				Map<Long, AbbreviationEntry> abbrevs = parseDebugAbbreviation(debug_abbrev_offset);

				long code = read_unsigned_leb128(data);
				AbbreviationEntry entry = abbrevs.get(Long.valueOf(code));
				AttributeList attributeList = new AttributeList(entry, data, address_size, getDebugStrings());

				// get comp_dir and name and figure out the path
				String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
				String compDir = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_comp_dir);

				IPath filePath = fileHelper.normalizeFilePath(compDir, name);
				provider.referencedFiles.add(filePath.toOSString());

				// do a quick parse of the line table to get any other
				// referenced files
				int stmtList = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_stmt_list);
				quickParseLineInfo(stmtList, compDir);

				// skip past the compile unit. note that the unit_length does
				// not include the size of the unit length itself
				fileIndex += unit_length + 4;
			}

		} catch (IOException e) {
			EDCDebugger.getMessageLogger().logError("Failed to parse source files from section " 
					+ debugInfoSection.getName() + " in file " + symbolFilePath, e);
		}
	}

	/**
	 * Get the .debug_strings section.
	 * @return ByteBuffer or <code>null</code>
	 */
	private IStreamBuffer getDebugStrings() {
		IStreamBuffer data = getDwarfSection(DWARF_DEBUG_STR);
		return data;
	}

	/**
	 * Does a quick parse of the .debug_line section just to get a list of
	 * referenced files from the line table.
	 */
	private void quickParseLineInfo(int lineTableOffset, String compileUnitDirectory) {
		IPath compileUnitDirectoryPath = PathUtils.createPath(compileUnitDirectory);
		try {
			// do a quick parse of the line table just to get referenced files
			IStreamBuffer data = getDwarfSection(DWARF_DEBUG_LINE);
			if (data != null) {
				data.position(lineTableOffset);

				/*
				 * Skip past the rest the bits of the header that we don't care
				 * about unit_length - 4 bytes version - 2 bytes header_length -
				 * 4 bytes minimum_instruction_length - 1 byte default_is_stmt -
				 * 1 byte line_base - 1 byte line_range - 1 byte
				 */
				data.position(data.position() + 14);

				// we need to get this value so we can skip over
				// standard_opcode_lengths
				int opcode_base = data.get() & 0xff;
				data.position(data.position() + opcode_base - 1);

				// include_directories
				ArrayList<String> dirList = new ArrayList<String>();

				// add the compilation directory of the CU as the first
				// directory
				dirList.add(compileUnitDirectory);

				while (true) {
					String str = readString(data);
					if (str.length() == 0)
						break;

					// if the directory is relative, append it to the CU dir
					IPath dir = PathUtils.createPath(str);
					if (!dir.isAbsolute() && dir.getDevice() == null) {
						dir = compileUnitDirectoryPath.append(str);
					}
					dirList.add(dir.toString());
				}

				while (true) {
					String fileName = readString(data);
					if (fileName.length() == 0) // no more file entry
						break;

					// dir index
					long leb128 = DwarfInfoReader.read_unsigned_leb128(data);

					IPath fullPath = fileHelper.normalizeFilePath(dirList.get((int) leb128), fileName);
					if (fullPath != null) {
						provider.referencedFiles.add(fullPath.toOSString());
					}

					// skip the modification time and file size
					leb128 = read_unsigned_leb128(data);
					leb128 = read_unsigned_leb128(data);
				}
			}
		} catch (Exception e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
	}


	/**
	 * Parse the line table for a given compile unit
	 * @param attributes
	 * @param fileList list for file entries
	 * @return new array of ILineEntry
	 */
	public Collection<ILineEntry> parseLineTable(IScope scope, AttributeList attributes, List<IPath> fileList) {
		List<ILineEntry> lineEntries = new ArrayList<ILineEntry>();
		try {
			IStreamBuffer data = getDwarfSection(DWARF_DEBUG_LINE);
			int stmtList = attributes.getAttributeValueAsInt(DwarfConstants.DW_AT_stmt_list);
			if (data != null && stmtList >= 0) {
				data.position(stmtList);

				/*
				 * Read line table header:
				 * 
				 * total_length: 4 bytes (excluding itself) version: 2 prologue
				 * length: 4 minimum_instruction_len: 1 default_is_stmt: 1
				 * line_base: 1 line_range: 1 opcode_base: 1
				 * standard_opcode_lengths: (value of opcode_base)
				 */

				// Remember the CU line tables we've parsed.
				int length = data.getInt() + 4;

				// Skip the following till "opcode_base"
				@SuppressWarnings("unused")
				int version = data.getShort();
				@SuppressWarnings("unused")
				int prologue_length = data.getInt();
				int minimum_instruction_length = data.get() & 0xff;
				boolean is_stmt = data.get() > 0;
				int line_base = data.get();  // signed
				int line_range = data.get() & 0xff;

				int opcode_base = data.get() & 0xff;
				byte[] opcodes = new byte[opcode_base - 1];
				data.get(opcodes);

				// Read in directories.
				//
				ArrayList<String> dirList = new ArrayList<String>();

				// Put the compilation directory of the CU as the first dir
				String compDir = attributes.getAttributeValueAsString(DwarfConstants.DW_AT_comp_dir);
				dirList.add(compDir);

				IPath compDirPath = PathUtils.createPath(compDir);
				
				String str, fileName;

				while (true) {
					str = readString(data);
					if (str.length() == 0)
						break;
					// If the directory is relative, append it to the CU dir
					IPath dir = PathUtils.createPath(str);
					if (!dir.isAbsolute() && dir.getDevice() == null) {
						dir = compDirPath.append(str);
					}
					dirList.add(dir.toString());
				}

				// Read file names
				//
				long leb128;
				while (true) {
					fileName = readString(data);
					if (fileName.length() == 0) // no more file entry
						break;

					// dir index
					leb128 = read_unsigned_leb128(data);

					IPath fullPath = fileHelper.normalizeFilePath(dirList.get((int) leb128), fileName);
					if (fullPath != null) {
						fileList.add(fullPath);
					}

					// Skip the followings
					//
					// modification time
					leb128 = read_unsigned_leb128(data);

					// file size in bytes
					leb128 = read_unsigned_leb128(data);
				}

				int info_line = 1;
				long info_address = 0;
				int info_flags = 0;
				long info_file = 1;
				int info_column = 0;
				@SuppressWarnings("unused")
				long info_ISA = 0;

				long lineInfoEnd = stmtList + length;
				while (data.position() < lineInfoEnd) {
					byte opcodeB = data.get();
					int opcode = 0xFF & opcodeB;

					if (opcode >= opcode_base) {
						info_line += (((opcode - opcode_base) % line_range) + line_base);
						info_address += (opcode - opcode_base) / line_range * minimum_instruction_length;
						if (is_stmt) {
							lineEntries.add(new LineEntry(fileList.get((int) info_file - 1), info_line, info_column,
									new Addr32(info_address), null));
						}
						info_flags &= ~(DwarfConstants.LINE_BasicBlock | DwarfConstants.LINE_PrologueEnd | DwarfConstants.LINE_EpilogueBegin);
					} else if (opcode == 0) {
						long op_size = read_unsigned_leb128(data);
						long op_pos = data.position();
						int code = data.get() & 0xff;
						switch (code) {
						case DwarfConstants.DW_LNE_define_file: {
							fileName = readString(data);
							long dir = read_unsigned_leb128(data);
							@SuppressWarnings("unused")
							long modTime = read_unsigned_leb128(data);
							@SuppressWarnings("unused")
							long fileSize = read_unsigned_leb128(data);
							IPath fullPath = fileHelper.normalizeFilePath(dirList.get((int) dir), fileName);
							if (fullPath != null) {
								fileList.add(fullPath);
							}
							break;
						}
						case DwarfConstants.DW_LNE_end_sequence:
							info_flags |= DwarfConstants.LINE_EndSequence;

							if (lineEntries.size() > 0) {
								// this just marks the end of a line number
								// program sequence. use
								// its address to set the high address of the
								// last line entry
								lineEntries.get(lineEntries.size() - 1).setHighAddress(new Addr32(info_address));
							}

							// it also resets the state machine
							info_file = 1;
							info_line = 1;
							info_address = 0;
							info_flags = 0;
							info_column = 0;
							info_ISA = 0;
							break;

						case DwarfConstants.DW_LNE_set_address:
							info_address = data.getInt();
							break;
						default:
							data.position((int) (data.position() + op_size - 1));
							break;
						}
						assert (data.position() == op_pos + op_size);
					} else {
						switch (opcode) {
						case DwarfConstants.DW_LNS_copy:
							if (is_stmt) {
								lineEntries.add(new LineEntry(fileList.get((int) info_file - 1), info_line,
										info_column, new Addr32(info_address), null));
							}
							info_flags &= ~(DwarfConstants.LINE_BasicBlock | DwarfConstants.LINE_PrologueEnd | DwarfConstants.LINE_EpilogueBegin);
							break;
						case DwarfConstants.DW_LNS_advance_pc:
							info_address += read_unsigned_leb128(data) * minimum_instruction_length;
							break;
						case DwarfConstants.DW_LNS_advance_line:
							info_line += read_signed_leb128(data);
							break;
						case DwarfConstants.DW_LNS_set_file:
							info_file = read_unsigned_leb128(data);
							break;
						case DwarfConstants.DW_LNS_set_column:
							info_column = (int) read_unsigned_leb128(data);
							break;
						case DwarfConstants.DW_LNS_negate_stmt:
							is_stmt = !is_stmt;
							break;
						case DwarfConstants.DW_LNS_set_basic_block:
							info_flags |= DwarfConstants.LINE_BasicBlock;
							break;
						case DwarfConstants.DW_LNS_const_add_pc:
							info_address += (255 - opcode_base) / line_range * minimum_instruction_length;
							break;
						case DwarfConstants.DW_LNS_fixed_advance_pc:
							info_address += data.getShort();
							break;
						case DwarfConstants.DW_LNS_set_prologue_end:
							info_flags |= DwarfConstants.LINE_PrologueEnd;
							break;
						case DwarfConstants.DW_LNS_set_epilogue_begin:
							info_flags |= DwarfConstants.LINE_EpilogueBegin;
							break;
						case DwarfConstants.DW_LNS_set_isa:
							info_ISA = read_unsigned_leb128(data);
							break;
						default:
							break;
						}
					}
				}
			}
		} catch (IOException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}

		// sort by start address
		Collections.sort(lineEntries);

		// fill in the end addresses as needed
		ILineEntry previousEntry = null;
		for (ILineEntry line : lineEntries) {
			if (previousEntry != null && previousEntry.getHighAddress() == null) {
				previousEntry.setHighAddress(line.getLowAddress());
			}

			previousEntry = line;
		}

		// the last line entry
		if (previousEntry != null && previousEntry.getHighAddress() == null) {
			previousEntry.setHighAddress(scope.getHighAddress());
		}

		return lineEntries;
	}

	private void parseForAddresses(IStreamBuffer in, Map<Long, AbbreviationEntry> abbrevs, CompilationUnitHeader header,
			Stack<Long> nestingStack)
			throws IOException {

		trace(IEDCTraceOptions.SYMBOL_READER_TRACE, "Address parse of " + header.scope.getName() + " @ " + Long.toHexString(header.debugInfoOffset));
		
		while (in.remaining() > 0) {
			long offset = in.position() + currentCUHeader.debugInfoOffset;
			long code = read_unsigned_leb128(in);
			AbbreviationEntry entry = abbrevs.get(new Long(code));

			if (entry != null) {
				if (isDebugInfoEntryWithAddressRange(entry.tag)) {
					processDebugInfoEntry(offset, entry, 
							new AttributeList(entry, in, header.addressSize, getDebugStrings()), 
							header);
				} else {
					// skip the attributes we're not reading...
					AttributeList.skipAttributes(entry, in, header.addressSize);
				}
				
				if (entry.hasChildren) {
					nestingStack.push(offset);
				}
			} else {
				if (code == 0) {
					if (nestingStack.isEmpty()) {
						// FIXME
						currentParentScope = null;
					} else {
						long nesting = nestingStack.pop();
						Scope parent = provider.scopesByOffset.get(nesting);
						if (parent != null)
							currentParentScope = (Scope) parent.getParent();
					}
				}
			}
		}
	}


	private void parseForTypes(IStreamBuffer in, Map<Long, AbbreviationEntry> abbrevs, CompilationUnitHeader header,
			Stack<Long> nestingStack)
			throws IOException {

		trace(IEDCTraceOptions.SYMBOL_READER_TRACE, "Parsing types for " + header.scope.getName() + " @ " + Long.toHexString(header.debugInfoOffset));
		
		Stack<IType> typeStack = new Stack<IType>();
		typeToParentMap = new HashMap<IType, IType>();
		
		currentParentScope = currentCompileUnitScope;
		
		while (in.remaining() > 0) {
			long offset = in.position() + currentCUHeader.debugInfoOffset;
			long code = read_unsigned_leb128(in);
			AbbreviationEntry entry = abbrevs.get(new Long(code));

			if (entry != null) {
				if (isForwardTypeTag(entry.tag) || isForwardTypeChildTag(entry.tag)) {
					
					processDebugInfoEntry(offset, entry, 
							new AttributeList(entry, in, header.addressSize, getDebugStrings()), 
							header);
					
				} else {
					switch (entry.tag) {
					case DwarfConstants.DW_TAG_subprogram:
					case DwarfConstants.DW_TAG_inlined_subroutine:
					case DwarfConstants.DW_TAG_variable:
					case DwarfConstants.DW_TAG_lexical_block: {
						Scope scope = provider.scopesByOffset.get(offset);  // may be null
						if (scope != null)
							currentParentScope = scope;
						break;
					}
					}
					
					// skip the attributes we're not reading...
					AttributeList.skipAttributes(entry, in, header.addressSize);
					
				}
				
				if (entry.hasChildren) {
					nestingStack.push(offset);
					typeStack.push(currentParentType);
					//typeMap.put(currentParentType, (int) offset);
				}
			} else {
				if (code == 0) {
					if (nestingStack.isEmpty()) {
						// FIXME
						typeStack.clear();
						currentParentType = null;
						currentParentScope = null;
					} else {
						long nesting = nestingStack.pop();
						currentParentType = typeStack.pop();
						Scope defScope = provider.scopesByOffset.get(nesting);
						if (defScope != null)
							currentParentScope = (Scope) defScope.getParent();
					}
				}
			}
		}
	}

	/**
	 * Tell if a tag will be parsed on-demand to generate an IType, and will
	 * be accessible via provider.getType() or provider.readType().
	 * <p>
	 * Note: DW_TAG_member is usually considered a child of struct/class/etc., but
	 * a static class variable contains a reference to it, so we must be able to
	 * locate it.
	 * @param tag
	 * @return true if type is parsed and should have a ForwardDwarfDefinition
	 */
	private boolean isForwardTypeTag(short tag) {
		switch (tag) {
		case DwarfConstants.DW_TAG_array_type:
		case DwarfConstants.DW_TAG_class_type:
		case DwarfConstants.DW_TAG_enumeration_type:
		case DwarfConstants.DW_TAG_member:
		case DwarfConstants.DW_TAG_pointer_type:
		case DwarfConstants.DW_TAG_reference_type:
		case DwarfConstants.DW_TAG_structure_type:
		case DwarfConstants.DW_TAG_subroutine_type:
		case DwarfConstants.DW_TAG_typedef:
		case DwarfConstants.DW_TAG_union_type:
		//case DwarfConstants.DW_TAG_unspecified_parameters:
		case DwarfConstants.DW_TAG_inheritance:
		//case DwarfConstants.DW_TAG_ptr_to_member_type:
		//case DwarfConstants.DW_TAG_with_stmt:
		case DwarfConstants.DW_TAG_base_type:
		//case DwarfConstants.DW_TAG_catch_block:
		case DwarfConstants.DW_TAG_const_type:
		//case DwarfConstants.DW_TAG_enumerator:
		//case DwarfConstants.DW_TAG_file_type:
		//case DwarfConstants.DW_TAG_friend:
		//case DwarfConstants.DW_TAG_template_type_param:
		//case DwarfConstants.DW_TAG_template_value_param:
		//case DwarfConstants.DW_TAG_thrown_type:
		//case DwarfConstants.DW_TAG_try_block:
		case DwarfConstants.DW_TAG_volatile_type:
		case DwarfConstants.DW_TAG_subrange_type:
			return true;
		}
		return false;
	}
	

	/**
	 * Tell if a tag is a parsed child of an IType.  This should not be explicitly
	 * referenced in provider.typesByOffset or .forwardDwarfDefinitions but as
	 * children of other ForwardDwarfDefinitions parsed on demand.
	 *<p>
	 * Note: DW_TAG_member is usually considered a child of struct/class/etc., but
	 * a static class variable contains a reference to it, so we must be able to
	 * locate it.  Thus, it is not listed here.
	 * @param tag
	 * @return true if component is parsed and a child of a forward definition
	 */
	private boolean isForwardTypeChildTag(short tag) {
		switch (tag) {
		//case DwarfConstants.DW_TAG_unspecified_parameters:
		case DwarfConstants.DW_TAG_inheritance:
		case DwarfConstants.DW_TAG_enumerator:
		case DwarfConstants.DW_TAG_member:
		case DwarfConstants.DW_TAG_subrange_type:
		//case DwarfConstants.DW_TAG_friend:
		//case DwarfConstants.DW_TAG_template_type_param:
		//case DwarfConstants.DW_TAG_template_value_param:
		//case DwarfConstants.DW_TAG_thrown_type:
			return true;
		}
		return false;
	}
	/**
	 * Parse a type from the debug information.  The TypeReference is
	 * presumed to have been generated by {@link #parseDebugInfoEntriesWithForwardTypes(ByteBuffer, Map, CompilationUnitHeader)}
	 * @param offset
	 * @param ref
	 */
	/*
	public IType parseType(long offset, ForwardDwarfDefinition ref) {
		// restore the parsing state
		switchToCompilationUnit(ref.header);
		
		return parseDwarfInner(offset, ref);
	}

	private void switchToCompilationUnit(CompilationUnitHeader header) {
		currentCUHeader = header;
		currentCompileUnitScope = header.scope;

		if (currentCompileUnitScope == null)
			throw new IllegalStateException();
		
		currentParentScope = currentCompileUnitScope;
	}
*/
	/**
	 * Parse a DWARF tag in isolation.
	 * @param offset
	 * @param ref
	 * @return IType, if the tag represented a type, else <code>null</code>
	 */
	/*
	private IType parseDwarfInner(long offset, ForwardDwarfDefinition ref) {
		ByteBuffer in = debugInfoSection.getBuffer();
		in.position(ref.attributeOffset);
		AttributeList attributeList = new AttributeList(ref.entry, in,
				ref.header.addressSize, getDebugStrings()); 
		
		currentParent = ref.parent;
		currentParentScope = ref.parentScope;
		processDebugInfoEntry(offset, ref.entry, attributeList, ref.header);
		
		IType type = null;
		if (isForwardTypeTag(ref.entry.tag)) {
			type = provider.typesByOffset.get(offset);
			if (type == null)
				throw new IllegalStateException();
			
			// store references to any dereferenced types (again, lazily fetched)
			if (type.getProperties() != null) {
				AttributeValue typeAttribute = (AttributeValue) type.getProperties().get(
														Short.valueOf(DwarfConstants.DW_AT_type));
				if (typeAttribute != null) {
					// get the offset into the .debug_info section
					long debugInfoOffset = ((Number) typeAttribute.value).longValue();
					if (typeAttribute.actualForm == DwarfConstants.DW_FORM_ref_addr) {
						// this is already relative to the .debug_info section
					} else {
						CompilationUnitHeader typeHeader = (CompilationUnitHeader) type.getProperties().get(DwarfInfoReader.CU_HEADER);
						debugInfoOffset += typeHeader.debugInfoOffset;
						
						// adjust the current scope too
						switchToCompilationUnit(typeHeader);
					}
	
					IType subType = provider.typesByOffset.get(debugInfoOffset);
					if (subType == null) {
						ForwardDwarfDefinition fwdType = provider.forwardDwarfDefinitions.get(debugInfoOffset);
						if (fwdType != null)
							subType = new ForwardTypeReference(provider, debugInfoOffset);
					}
					// may still be null here if it's a tag we don't recognize yet
					type.setType(subType);
				}
			}
		}
		
		if (ref.entry.hasChildren) {
			for (ForwardDwarfDefinition fwdDef : ref.childEntries) {
				parseDwarfInner(fwdDef.offset, fwdDef);
			}
		}
		
		return type;
	}
	*/
	
	/**
	 * Fully parse any debug info entry.
	 * @param offset
	 * @param entry
	 * @param attributeList
	 * @param header
	 * @param compositeNesting
	 */
	private void processDebugInfoEntry(long offset, AbbreviationEntry entry, AttributeList attributeList,
			CompilationUnitHeader header) {
		//System.out.println("Handling " + entry.tag + " at " + Long.toHexString(offset));
		short tag = entry.tag;

		// We are only interested in certain tags.
		switch (tag) {
		case DwarfConstants.DW_TAG_array_type:
			processArrayType(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_class_type:
			processClassType(offset, attributeList, header);
			break;
		case DwarfConstants.DW_TAG_enumeration_type:
			processEnumType(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_formal_parameter:
			processVariable(offset, attributeList, true);
			break;
		case DwarfConstants.DW_TAG_lexical_block:
			processLexicalBlock(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_member:
			processField(offset, attributeList, header);
			break;
		case DwarfConstants.DW_TAG_pointer_type:
			processPointerType(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_reference_type:
			processReferenceType(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_structure_type:
			processStructType(offset, attributeList, header);
			break;
		case DwarfConstants.DW_TAG_subroutine_type:
			processSubroutineType(offset, attributeList, header);
			break;
		case DwarfConstants.DW_TAG_typedef:
			processTypeDef(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_union_type:
			processUnionType(offset, attributeList, header);
			break;
		case DwarfConstants.DW_TAG_unspecified_parameters:
			break;
		case DwarfConstants.DW_TAG_inheritance:
			processInheritance(offset, attributeList, header);
			break;
		case DwarfConstants.DW_TAG_ptr_to_member_type:
			break;
		case DwarfConstants.DW_TAG_with_stmt:
			break;
		case DwarfConstants.DW_TAG_base_type:
			processBasicType(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_catch_block:
			break;
		case DwarfConstants.DW_TAG_const_type:
			processConstType(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_enumerator:
			processEnumerator(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_file_type:
			break;
		case DwarfConstants.DW_TAG_friend:
			break;
		case DwarfConstants.DW_TAG_subprogram:
			processSubProgram(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_inlined_subroutine:
			processInlinedSubroutine(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_template_type_param:
			break;
		case DwarfConstants.DW_TAG_template_value_param:
			break;
		case DwarfConstants.DW_TAG_thrown_type:
			break;
		case DwarfConstants.DW_TAG_try_block:
			break;
		case DwarfConstants.DW_TAG_variable:
			processVariable(offset, attributeList, false);
			break;
		case DwarfConstants.DW_TAG_volatile_type:
			processVolatileType(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_subrange_type:
			processArrayBoundType(offset, attributeList);
			break;
		}
	}	
	
	/**
	 * Tell whether a tag has or may have content with an address range
	 * 
	 * Note: tag DW_TAG_compile_unit was parsed in the initial parse
	 * 
	 * @param tag
	 * @return
	 */
	private boolean isDebugInfoEntryWithAddressRange(short tag) {
		switch (tag) {
		// tags allowed to have both DW_AT_low_pc and DW_AT_high_pc or DW_at_ranges
		case DwarfConstants.DW_TAG_catch_block:
		case DwarfConstants.DW_TAG_inlined_subroutine:
		case DwarfConstants.DW_TAG_lexical_block:
		case DwarfConstants.DW_TAG_module:
		case DwarfConstants.DW_TAG_partial_unit:
		case DwarfConstants.DW_TAG_subprogram:
		case DwarfConstants.DW_TAG_try_block:
		case DwarfConstants.DW_TAG_with_stmt:
			return true;
		// TODO: take this out of here?
		case DwarfConstants.DW_TAG_variable:
		case DwarfConstants.DW_TAG_formal_parameter:
			return true;
		}
		return false;
	}
	
	static Long readAddress(IStreamBuffer in, int addressSize) throws IOException {
		long value = 0;

		switch (addressSize) {
		case 2:
			value = in.getShort();
			break;
		case 4:
			value = in.getInt();
			break;
		case 8:
			value = in.getLong();
			break;
		default:
			// ????
		}
		return new Long(value);
	}


	/* unsigned */
	static long read_unsigned_leb128(IStreamBuffer in) throws IOException {
		/* unsigned */
		long result = 0;
		int shift = 0;
		byte b;

		while (true) {
			b = in.get();
			if (!in.hasRemaining())
				break; // throw new IOException("no more data");
			result |= ((long) (b & 0x7f) << shift);
			if ((b & 0x80) == 0) {
				break;
			}
			shift += 7;
		}
		return result;
	}

	/* signed */
	public static long read_signed_leb128(IStreamBuffer in) throws IOException {
		/* unsigned */
		long result = 0;
		int shift = 0;
		int size = 32;
		byte b;

		while (true) {
			if (!in.hasRemaining())
				throw new IOException(CCorePlugin.getResourceString("Util.exception.noData")); //$NON-NLS-1$
			b = in.get();
			result |= ((long) (b & 0x7f) << shift);
			shift += 7;
			if ((b & 0x80) == 0) {
				break;
			}
		}
		if ((shift < size) && (b & 0x40) != 0) {
			result |= -(1 << shift);
		}
		return result;
	}


	/**
	 * Read a null-ended string from the given "data" stream. data : IN, byte
	 * buffer
	 */
	public static String readString(IStreamBuffer data) {
		String str;

		StringBuilder sb = new StringBuilder();
		while (data.hasRemaining()) {
			byte c = data.get();
			if (c == 0) {
				break;
			}
			sb.append((char) c);
		}

		str = sb.toString();
		return str;
	}

	private List<LocationEntry> getLocationRecord(long offset) {
		// first check the cache
		List<LocationEntry> entries = locationEntriesByOffset.get(offset);
		if (entries == null) {
			// not found so try to get the entries from the offset
			entries = new ArrayList<LocationEntry>();

			try {
				IStreamBuffer data = getDwarfSection(DWARF_DEBUG_LOC);
				if (data != null) {
					long base = currentCUHeader.scope.getLowAddress().getValue().longValue();
					data.position(offset);
					while (data.hasRemaining()) {

						long lowPC = readAddress(data, currentCUHeader.addressSize);
						long highPC = readAddress(data, currentCUHeader.addressSize);

						if (lowPC == 0 && highPC == 0) {
							// end of list entry
							break;
						} else {
							// location list entry
							int numOpCodes = data.getShort();
							byte[] bytes = new byte[numOpCodes];
							data.get(bytes);
							LocationEntry entry = new LocationEntry(lowPC + base, highPC + base, bytes);
							entries.add(entry);
						}
					}

					locationEntriesByOffset.put(offset, entries);
				}
			} catch (Exception e) {
				EDCDebugger.getMessageLogger().logError(null, e);
			}
		}

		return entries;
	}


	private Map<Long, AbbreviationEntry> parseDebugAbbreviation(int abbreviationOffset) throws IOException {
		Integer key = Integer.valueOf(abbreviationOffset);
		Map<Long, AbbreviationEntry> abbrevs = provider.abbreviationMaps.get(key);
		if (abbrevs == null) {
			abbrevs = new HashMap<Long, AbbreviationEntry>();
			provider.abbreviationMaps.put(key, abbrevs);
			IStreamBuffer data = getDwarfSection(DWARF_DEBUG_ABBREV);
			if (data != null) {
				data.position(abbreviationOffset);
				while (data.remaining() > 0) {
					long code = read_unsigned_leb128(data);
					if (code == 0) {
						break;
					}
					short tag = (short) read_unsigned_leb128(data);
					boolean hasChildren = data.get() == DwarfConstants.DW_CHILDREN_yes;
					AbbreviationEntry entry = new AbbreviationEntry(code, tag, hasChildren);

					// attributes
					short name = 0;
					byte form = 0;
					do {
						name = (short) read_unsigned_leb128(data);
						form = (byte) read_unsigned_leb128(data);
						if (name != 0) {
							entry.attributes.add(new Attribute(name, form));
						}
					} while (name != 0 && form != 0);
					entry.attributes.trimToSize();

					abbrevs.put(Long.valueOf(code), entry);
				}
			}
		}
		return abbrevs;
	}


	private void registerType(long offset, IType type) {
		Long typeKey = offset;
		//if (provider.typesByOffset.containsKey(typeKey))
		//	throw new IllegalStateException();
		provider.typesByOffset.put(typeKey, type);
		
		typeToParentMap.put(type, currentParentType);
		currentParentType = type;
		if (DEBUG) {
			if (type != null) {
				System.out.print("Read type " + type.getName());
				while (type.getType() != null) {
					type = type.getType();
					System.out.print(" " + type.getName());
				}
				System.out.println();
			}
		}
	}

	private void registerScope(long offset, Scope scope) {
		provider.scopesByOffset.put(offset, scope);
		currentParentScope = scope;
	}

	/**
	 * Read a range list referenced from a code scope.
	 * @param offset
	 * @param base the specified DW_AT_low_pc value (or 0)
	 * @return a new RangeList
	 */
	public RangeList readRangeList(int offset, AttributeValue baseValue) {
		IStreamBuffer data = getDwarfSection(DWARF_DEBUG_RANGES);
		if (data == null) {
			return null;
		}
		
		try {
			data.position(offset);
		
			/*
			 * Read range list entry:
			 * 
			 * start: DW_FORM_addr
			 * end: DW_FORM_addr
			 * 
			 * When start == all ones, it is a base address selection entry,
			 * and end is the base address.  The base address does not need to
			 * be specified, and is the compialtion unit's base address by default.
			 * 
			 * When start == end == 0, this is the end of the list.
			 */

			RangeList list = new RangeList();

			long base = 0;
			long start = data.getInt();
			long end = data.getInt();
			
			if (start == -1) {
				base = end;
				
				start = data.getInt();
				end = data.getInt();
			} else if (baseValue != null) {
				base = ((Number) baseValue.value).longValue();
			} else if (currentCompileUnitScope != null && !currentCompileUnitScope.hasEmptyRange()) {
				base = currentCompileUnitScope.getLowAddress().getValue().longValue();
			}
			do {
				if (start == 0 && end == 0) {
					break;
				} else if (start != end) {
					// ignore bogus entries: GCC-E sometimes generates these buggily (for artifical non-inlined functions)
					if (base + start >= codeRanges.getLowAddress()) {
						list.addRange(base + start, base + end);
					}
				}
				start = data.getInt();
				end = data.getInt();
				
			} while (true);
			
			return list;
			
		} catch (BufferUnderflowException e) {
			EDCDebugger.getMessageLogger().logError("Failed to read ranges", e);
			return null;
		}
	}

	
	
	/**
	 * Set up the address range for a scope by using its DW_AT_low_pc/DW_AT_high_pc
	 * or DW_AT_ranges attributes, or DW_AT_stmt_list in a pinch
	 * @param attributeList
	 * @param scope
	 */
	private void setupAddresses(AttributeList attributeList, Scope scope) {
		
		// get the high and low pc from the attributes list
		AttributeValue value;
		value = attributeList.getAttribute(DwarfConstants.DW_AT_high_pc);
		if (value != null) {
			IAddress low = new Addr32(attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_low_pc));
			IAddress high = new Addr32(attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_high_pc));
			if (low.compareTo(high) > 0) {
				// wow, RVCT, you're neat... I think you mean, point to the high PC of the parent
				if (scope.getParent() != null && scope.getParent().getHighAddress() != null) {
					high = scope.getParent().getHighAddress();
					// may still be bogus, check again next
				} 
				
				if (low.compareTo(high) > 0) {
					IAddress t = high;
					high = low;
					low = t;
				}
			}
			scope.setLowAddress(low);
			scope.setHighAddress(high);
			return;
		}
		
		// look for a range
		value = attributeList.getAttribute(DwarfConstants.DW_AT_ranges);
		if (value != null) {
			AttributeValue baseValue = attributeList.getAttribute(DwarfConstants.DW_AT_low_pc);
			RangeList ranges = readRangeList(((Number)value.value).intValue(), baseValue);
			if (ranges != null) {
				scope.setRangeList(ranges);
				return;
			}
		}
		
		// in a CU, GCC-E may have only generated this, so we need to dig into the line table
		if (scope instanceof ICompileUnitScope) {
			value = attributeList.getAttribute(DwarfConstants.DW_AT_stmt_list);
			if (value != null) {
				RangeList ranges = new RangeList();
				for (ILineEntry entry : ((ICompileUnitScope) scope).getLineEntries()) {
					// ignore (for now) entries that seem far out of range
					if (entry.getLowAddress().getValue().longValue() >= codeRanges.getLowAddress()) {
						ranges.addRange(entry.getLowAddress().getValue().longValue(),
								entry.getHighAddress().getValue().longValue());
					}
				}
				scope.setRangeList(ranges);
				return;
			}
		}
		
		// no code, apparently
		scope.setLowAddress(new Addr32(0));
		scope.setHighAddress(new Addr32(0));
	}

	/**
	 * Process a compile unit
	 * 
	 * @param attributeList
	 * @param childrenPosition
	 */
	private void processCompileUnit(CompilationUnitHeader header, boolean hasChildren, AttributeList attributeList) {
		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		String compDir = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_comp_dir);
		//System.out.println("processing compile unit: " + Integer.toHexString(header.debugInfoOffset) + ": " + name);

		IPath filePath = fileHelper.normalizeFilePath(compDir, name);
		
		currentCompileUnitScope = new DwarfCompileUnit(provider, moduleScope, filePath, null, null, header, hasChildren, attributeList);
		header.scope = currentCompileUnitScope;
		currentParentScope = currentCompileUnitScope;
		
		setupAddresses(attributeList, currentCompileUnitScope);

		// some compilers (RVCT) may generate multiple compile units for the
		// same file.
		List<ICompileUnitScope> matchingCompileUnits = provider.compileUnitsPerFile.get(filePath);
		
		if (matchingCompileUnits == null) {
			// first one. create it now.
			matchingCompileUnits = new ArrayList<ICompileUnitScope>();
		}
		
		matchingCompileUnits.add(currentCompileUnitScope);
		provider.compileUnitsPerFile.put(filePath, matchingCompileUnits);
		provider.compileUnits.add(currentCompileUnitScope);
		moduleScope.addChild(currentCompileUnitScope);
		
		provider.registerCompileUnitHeader(currentCUHeader.debugInfoOffset, currentCUHeader);
		
		if (provider.buildReferencedFilesList) {
			provider.referencedFiles.add(filePath.toOSString());

			// do a quick parse of the line table to get any other referenced files.
			// note that even the full parse doesn't parse the line table information.
			// that is calculated (and then cached) on demand
			int stmtList = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_stmt_list);
			quickParseLineInfo(stmtList, compDir);
		}
		
		// remove unused attributes
		attributeList.attributeMap.remove(DwarfConstants.DW_AT_name);
		//attributeList.attributeMap.remove(DwarfConstants.DW_AT_comp_dir); // needed later
		attributeList.attributeMap.remove(DwarfConstants.DW_AT_low_pc);
		attributeList.attributeMap.remove(DwarfConstants.DW_AT_high_pc);
		attributeList.attributeMap.remove(DwarfConstants.DW_AT_ranges);
	}

	private void processLexicalBlock(long offset, AttributeList attributeList) {
		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);

		if (!attributeList.hasCodeRangeAttributes()) {
			// ignore any that don't have a valid range
			return;
		}
		
		LexicalBlockScope lb = new LexicalBlockScope(name, currentParentScope, null, null);
		setupAddresses(attributeList, lb);

		currentParentScope.addChild(lb);
		registerScope(offset, lb);
	}

	static class DereferencedAttributes {
		public CompilationUnitHeader header;
		public AttributeList attributeList;
		
		public DereferencedAttributes(CompilationUnitHeader header,
				AttributeList attributeList) {
			this.header = header;
			this.attributeList = attributeList;
		}
		
	}

	/**
	 * DW_AT_abstract_origin and DW_AT_specification can refer to types in other
	 * compilation units. This will dynamically parse that CU if needed in order
	 * to get the attributes for the type.
	 * 
	 * @param debugInfoOffset
	 * @return AttributeList or <code>null</code> (should not happen)
	 */
	private DereferencedAttributes getDereferencedAttributes(AttributeList attributeList, short tag) {
		CompilationUnitHeader providingCU = currentCUHeader;
		AttributeValue derefLocation = attributeList.getAttribute(tag);
		if (derefLocation == null)
			return null;
		
		// get the offset into the .debug_info section
		long debugInfoOffset = ((Number) derefLocation.value).longValue();
		if (derefLocation.actualForm == DwarfConstants.DW_FORM_ref_addr) {
			// this is already relative to the .debug_info section
		} else {
			// relative to the CU 
			debugInfoOffset += providingCU.debugInfoOffset;
		}
		
		AttributeList attributes = provider.functionsByOffset.get(debugInfoOffset);
		if (attributes == null) {
			// dereferenced function does not exist yet
			providingCU = provider.scanCompilationHeader(debugInfoOffset);
			attributes = provider.functionsByOffset.get(debugInfoOffset);
		} else {
			providingCU = provider.fetchCompileUnitHeader(debugInfoOffset);
			if (providingCU == null) {
				assert(false);
				return null;
			}
		}
		if (attributes == null)
			return null;
		
		return new DereferencedAttributes(providingCU, attributes);
	}

	private void processSubProgram(long offset, AttributeList attributeList) {
		// if it's a declaration just add to the offsets map for later lookup
		if (attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_declaration) > 0) {
			provider.functionsByOffset.put(offset, attributeList);
			return;
		}

		// functions with no high/low pc aren't real functions. just treat them
		// as declarations as they will be pointed to by abstract_origin from
		// another sub program tag
		if (!attributeList.hasCodeRangeAttributes()) {
			provider.functionsByOffset.put(offset, attributeList);
			return;
		}

		CompilationUnitHeader otherCU = null;

		boolean isArtifical = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_artificial) > 0;

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		if (name.length() == 0) {
			// no name. see if we can get it from a declaration
			DereferencedAttributes deref = getDereferencedAttributes(attributeList, DwarfConstants.DW_AT_abstract_origin); 
			if (deref != null) {
				// this should either have a name or point to another
				// declaration
				otherCU = deref.header;
				AttributeList attributes = deref.attributeList;
				name = attributes.getAttributeValueAsString(DwarfConstants.DW_AT_name);
				isArtifical |= attributes.getAttributeValueAsInt(DwarfConstants. DW_AT_artificial) > 0;
				if (name.length() == 0) {
					deref = getDereferencedAttributes(attributes, DwarfConstants.DW_AT_specification); 
					if (deref != null) {
						// this should either have a name or point to another
						// declaration
						otherCU = deref.header;
						attributes = deref.attributeList;
						name = attributes.getAttributeValueAsString(DwarfConstants.DW_AT_name);
						isArtifical |= attributes.getAttributeValueAsInt(DwarfConstants. DW_AT_artificial) > 0;
					}
				}
			}
		}
		if (name.length() == 0) {
			DereferencedAttributes deref = getDereferencedAttributes(attributeList, DwarfConstants.DW_AT_specification); 
			if (deref != null) {
				// this should either have a name or point to another
				// declaration
				otherCU = deref.header;
				AttributeList attributes = deref.attributeList;
				name = attributes.getAttributeValueAsString(DwarfConstants.DW_AT_name);
			}
		}

		if (name.length() == 0) {
			// the name should either be an attribute of the compile unit, or be
			// in the declaration which according to the spec will always be
			// before its definition in the Dwarf.
			return;
		}

		DwarfFunctionScope function = new DwarfFunctionScope(name, currentCompileUnitScope, null, null, null);
		setupAddresses(attributeList, function);
		
		registerScope(offset, function);
		
		AttributeValue frameBaseAttribute = attributeList.getAttribute(DwarfConstants.DW_AT_frame_base);
		ILocationProvider locationProvider = getLocationProvider(frameBaseAttribute);
		function.setLocationProvider(locationProvider);
		
		// Note: we may still have cases where DW_AT_low_pc and/or DW_AT_high_pc are 0x0
		// (some "ignored inlined" functions in GCC).  We want to keep track of their scope
		// (though not store them in the CU), because child tag parses expect to find a parent into 
		// which to write their formal parameters and locals.
		if (!function.getLowAddress().isZero() && !function.getHighAddress().isZero() && !isArtifical) {
			// find the declaration location
			int declLine = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_decl_line);
			int declColumn = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_decl_column);
			int declFileNum = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_decl_file);
			
			if (otherCU != null)
				function.setDeclFile(otherCU.scope.getFileEntry(declFileNum));
			else
				function.setDeclFileNum(declFileNum);
			function.setDeclLine(declLine);
			function.setDeclColumn(declColumn);
			
			currentCompileUnitScope.addChild(function);
			
			// keep track of all functions by name for faster lookup
			List<IFunctionScope> functions = provider.functionsByName.get(name);
			if (functions == null) {
				functions = new ArrayList<IFunctionScope>();
				provider.functionsByName.put(name, functions);
			}
			functions.add(function);
		}
	}

	/** 
	 * Get the already-parsed or forward reference to a type from a DW_AT_type attribute, if present
	 * @param attributeMap the map of Long, AttributeValue from AttributeList or Object, Object from Type
	 * @return offset to referenced type or 0 if no type attribute 
	 */
	private IType getTypeOrReference(AttributeList attributeList, CompilationUnitHeader header) {
		AttributeValue typeAttribute = attributeList.getAttribute(DwarfConstants.DW_AT_type);
		if (typeAttribute == null)
			return null;
		return getTypeOrReference(typeAttribute, header);
	}
	/** 
	 * Get the already-parsed or forward reference to a type from a DW_AT_type attribute, if present
	 * @param attributeMap the map of Long, AttributeValue from AttributeList or Object, Object from Type
	 * @return offset to referenced type or 0 if no type attribute 
	 */
	private IType getTypeOrReference(AttributeValue typeAttribute, CompilationUnitHeader header) {
		if (typeAttribute == null)
			return null;
		
		// get the offset into the .debug_info section
		long debugInfoOffset = ((Number) typeAttribute.value).longValue();
		if (typeAttribute.actualForm == DwarfConstants.DW_FORM_ref_addr) {
			// this is already relative to the .debug_info section
		} else {
			debugInfoOffset += header.debugInfoOffset;
		}
		
		IType type = provider.typesByOffset.get(debugInfoOffset);
		if (type == null) {
			type = new ForwardTypeReference(provider, debugInfoOffset);
		}
		return type;
	}
	 
	private void processInlinedSubroutine(long offset, AttributeList attributeList) {
		// functions with no high/low pc aren't real (probably an error)
		if (!attributeList.hasCodeRangeAttributes()) {
			return;
		}

		DereferencedAttributes deref = getDereferencedAttributes(attributeList, DwarfConstants.DW_AT_abstract_origin);
		if (deref == null) {
			if (attributeList.getAttribute(DwarfConstants.DW_AT_abstract_origin) != null) {
				// TODO: GCC-E can reference forward tags (!) so we need to handle these another way
			} else {
				assert(false);
			}
			return;
		}
		
		CompilationUnitHeader otherCU = deref.header;
		AttributeList origAttributes = deref.attributeList;
		
		// this should either have a name or point to another
		// declaration
		String name = origAttributes.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		if (name.length() == 0) {
			deref = getDereferencedAttributes(origAttributes, DwarfConstants.DW_AT_specification);
			if (deref != null) {
				// this should either have a name or point to another
				// declaration
				//otherCU = deref.header;
				AttributeList declarationAttributes = deref.attributeList;
				name = declarationAttributes.getAttributeValueAsString(DwarfConstants.DW_AT_name);
			}
		}

		if (name.length() == 0) {
			// the name should either be an attribute of the compile unit, or be
			// in the declaration which according to the spec will always be
			// before its definition in the Dwarf.
			return;
		}

		// find the declaration location
		int declLine = origAttributes.getAttributeValueAsInt(DwarfConstants.DW_AT_decl_line);
		int declColumn = origAttributes.getAttributeValueAsInt(DwarfConstants.DW_AT_decl_column);
		int declFileNum = origAttributes.getAttributeValueAsInt(DwarfConstants.DW_AT_decl_file);
		
		if (declFileNum == 0) {
			assert(false);
			return;
		}
		
		DwarfFunctionScope function = new DwarfFunctionScope(name, currentParentScope, null, null, null);
		setupAddresses(attributeList, function);
		
		function.setDeclFile(otherCU.scope.getFileEntry(declFileNum));
		function.setDeclLine(declLine);
		function.setDeclColumn(declColumn);
		
		currentParentScope.addChild(function);
		
		registerScope(offset, function);
		
		// keep track of all functions by name for faster lookup
		List<IFunctionScope> functions = provider.functionsByName.get(name);
		if (functions == null) {
			functions = new ArrayList<IFunctionScope>();
			provider.functionsByName.put(name, functions);
		}
		functions.add(function);
	}

	private void processSubroutineType(long offset, AttributeList attributeList, CompilationUnitHeader header) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		SubroutineType type = new SubroutineType(currentParentScope, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		registerType(offset, type);
		
		// TODO: associate parameters with this type in child tag parse
		
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}
	
	private void processClassType(long offset, AttributeList attributeList, CompilationUnitHeader header) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);
		
		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);

		ClassType type = new ClassType(name, currentParentScope, byteSize, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}
	
	private void processStructType(long offset, AttributeList attributeList, CompilationUnitHeader header) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);

		StructType type = new StructType(name, currentParentScope, byteSize, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	private void processUnionType(long offset, AttributeList attributeList, CompilationUnitHeader header) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);

		UnionType type = new UnionType(name, currentParentScope, byteSize, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	private void processInheritance(long offset, AttributeList attributeList, CompilationUnitHeader header) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		ICompositeType compositeType = null;
		compositeType = getCompositeParent();
		
		// The allowed attributes are DW_AT_type, DW_AT_data_member_location,
		// and DW_AT_accessibility
		long fieldsOffset = 0;
		byte[] offsetBlock = attributeList.getAttributeValueAsBytes(DwarfConstants.DW_AT_data_member_location);
		// unsigned LEB128 encoding
		if (offsetBlock.length > 0 && offsetBlock[0] == DwarfConstants.DW_OP_plus_uconst) {
			for (int i = 1, shift = 0; i < offsetBlock.length; i++) {
				fieldsOffset += (offsetBlock[i] & 0x7f) << shift;
				shift += 7;
			}
		}

		// default accessibility is private
		int accessibility = ICompositeType.ACCESS_PRIVATE;
		if (attributeList.getAttribute(DwarfConstants.DW_AT_accessibility) != null) {
			accessibility = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_accessibility);
			
			if (accessibility == DwarfConstants.DW_ACCESS_public)
				accessibility = ICompositeType.ACCESS_PUBLIC;
			else if (accessibility == DwarfConstants.DW_ACCESS_private)
				accessibility = ICompositeType.ACCESS_PRIVATE;
			else
				accessibility = ICompositeType.ACCESS_PROTECTED;
		}
		
		InheritanceType type = new InheritanceType(currentParentScope, accessibility, fieldsOffset, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		
		// add the member to the deepest nested (last added) compositeNesting
		// member
		if (compositeType != null)
			compositeType.addInheritance(type);
		
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	/*
	// remove composites that are out of scope and add a composite that is now
	// is scope
	private void adjustCompositeNesting(long offset, long siblingOffset, ICompositeType type,
			CompilationUnitHeader header, ArrayList<CompositeNest> compositeNesting) {

		// siblingOffset will be 0 when the composite is being used, but not
		// being defined. E.g., when we are getting the type of a pointer such
		// as "class foo *pFoo"
		if (siblingOffset != 0) {
			// if needed, remove composites whose definitions are finished
			while ((compositeNesting.size() > 0)
					&& (compositeNesting.get(0).getSiblingOffset() + header.debugInfoOffset <= offset))
				compositeNesting.remove(0);

			// add this structure to the start of the composite list
			compositeNesting.add(0, new CompositeNest(siblingOffset, type));
		}
	}
	 */
	private void processField(long offset, AttributeList attributeList, CompilationUnitHeader header) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		
		// GCC-E has fields like "_vptr.BaseClass" which will be a problem for us 
		// (since '.' is an operator); rename these here
		name = name.replace('.', '$');
		
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);
		int bitSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_bit_size);
		int bitOffset = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_bit_offset);

		long fieldOffset = 0;
		byte[] offsetBlock = attributeList.getAttributeValueAsBytes(DwarfConstants.DW_AT_data_member_location);
		// unsigned LEB128 encoding
		if (offsetBlock.length > 0 && offsetBlock[0] == DwarfConstants.DW_OP_plus_uconst) {
			for (int i = 1, shift = 0; i < offsetBlock.length; i++) {
				fieldOffset += (offsetBlock[i] & 0x7f) << shift;
				shift += 7;
			}
		}

		ICompositeType compositeType = null;
		
		compositeType = getCompositeParent();

		// default accessibility depends on the composite type -
		// public for a struct or union, private for a class
		int accessibility = ICompositeType.ACCESS_PUBLIC;
		if (attributeList.getAttribute(DwarfConstants.DW_AT_accessibility) != null) {
			accessibility = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_accessibility);
			
			if (accessibility == DwarfConstants.DW_ACCESS_public)
				accessibility = ICompositeType.ACCESS_PUBLIC;
			else if (accessibility == DwarfConstants.DW_ACCESS_private)
				accessibility = ICompositeType.ACCESS_PRIVATE;
			else
				accessibility = ICompositeType.ACCESS_PROTECTED;
		} else if (compositeType != null && compositeType instanceof ClassType)
			accessibility = ICompositeType.ACCESS_PRIVATE;

		FieldType type = new FieldType(name, currentParentScope, compositeType, fieldOffset, bitSize, bitOffset,
				byteSize, accessibility, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		// add the member to the deepest nested (last added) compositeNesting
		// member
		if (compositeType != null)
			compositeType.addField(type);
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	/*
	private ICompositeType getCompositeParent() {
		ICompositeType compositeType = null;
		ForwardDwarfDefinition def = currentParent;
		while (def != null) {
			IType type = provider.readType(def.offset);
			if (type instanceof ICompositeType) { 
				compositeType = (ICompositeType) type;
				break;
			} 
			def = def.parent;
		}
		return compositeType;
	}*/
	
	private ICompositeType getCompositeParent() {
		IType parent = currentParentType;
		while (parent != null) {
			if (parent instanceof ICompositeType)
				return ((ICompositeType) parent);
			parent = typeToParentMap.get(parent);
		}
		return null;
	}

	private void processArrayType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);

		ArrayType type = new ArrayType(name, currentParentScope, byteSize, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	private IArrayType getArrayParent() {
		IType parent = currentParentType;
		while (parent != null) {
			if (parent instanceof IArrayType)
				return ((IArrayType) parent);
			parent = typeToParentMap.get(parent);
		}
		return null;
	}

	private void processArrayBoundType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		long arrayBound = 0;
		if (attributeList.getAttribute(DwarfConstants.DW_AT_upper_bound) != null)
			arrayBound = attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_upper_bound) + 1;

		ArrayBoundType type = new ArrayBoundType(currentParentScope, arrayBound);

		IArrayType array = getArrayParent();
		if (array == null)
			throw new IllegalStateException();
		((IArrayType) array).addBound(type);

		registerType(offset, type);
		
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	private void processReferenceType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);

		ReferenceType type = new ReferenceType(name, currentParentScope, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	private void processPointerType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);

		PointerType type = new PointerType(name, currentParentScope, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	private void processConstType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		ConstType type = new ConstType(currentParentScope, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	private void processVolatileType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		VolatileType type = new VolatileType(currentParentScope, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	private void processEnumType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);

		Enumeration type = new Enumeration(name, currentParentScope, byteSize, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	private Enumeration getEnumerationParent() {
		IType parent = currentParentType;
		while (parent != null) {
			if (parent instanceof Enumeration)
				return ((Enumeration) parent);
			parent = typeToParentMap.get(parent);
		}
		return null;
	}
	
	private void processEnumerator(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		long value = attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_const_value);

		Enumerator enumerator = new Enumerator(name, value);
		
		Enumeration enumeration = getEnumerationParent();
		if (enumeration == null)
			throw new IllegalStateException();
		((Enumeration) enumeration).addEnumerator(enumerator);
		((Scope)((Enumeration) enumeration).getScope()).addEnumerator(enumerator);

		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, enumerator);
	}

	private void processTypeDef(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);

		TypedefType type = new TypedefType(name, currentParentScope, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	private void processBasicType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, offset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);

		int baseType = IBasicType.t_unspecified;
		int qualifierBits = 0;
		int encoding = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_encoding);

		switch (encoding) {
		case DwarfConstants.DW_ATE_boolean:
			baseType = ICPPBasicType.t_bool;
			break;
		case DwarfConstants.DW_ATE_float:
			if (name.contains("float")) {
				baseType = IBasicType.t_float;
			} else if (name.contains("long double")) {
				baseType = IBasicType.t_double;
				qualifierBits |= ICPPBasicType.IS_LONG;
			} else if (name.contains("double")) {
				baseType = IBasicType.t_double;
			}
			break;
		case DwarfConstants.DW_ATE_signed:
			baseType = IBasicType.t_int;
			qualifierBits |= ICPPBasicType.IS_SIGNED;
			if (name.contains("short")) {
				qualifierBits |= ICPPBasicType.IS_SHORT;
			} else if (name.contains("long long")) {
				qualifierBits |= ICPPBasicType.IS_LONG_LONG;
			} else if (name.contains("long")) {
				qualifierBits |= ICPPBasicType.IS_LONG;
			}
			break;
		case DwarfConstants.DW_ATE_signed_char:
			baseType = IBasicType.t_char;
			qualifierBits |= ICPPBasicType.IS_SIGNED;
			break;
		case DwarfConstants.DW_ATE_unsigned:
			baseType = IBasicType.t_int;
			qualifierBits |= ICPPBasicType.IS_UNSIGNED;
			if (name.contains("short")) {
				qualifierBits |= ICPPBasicType.IS_SHORT;
			} else if (name.contains("long long")) {
				qualifierBits |= ICPPBasicType.IS_LONG_LONG;
			} else if (name.contains("long")) {
				qualifierBits |= ICPPBasicType.IS_LONG;
			}
			break;
		case DwarfConstants.DW_ATE_unsigned_char:
			baseType = IBasicType.t_char;
			qualifierBits |= ICPPBasicType.IS_UNSIGNED;
			break;
		case DwarfConstants.DW_ATE_complex_float:
			qualifierBits |= ICPPBasicType.IS_COMPLEX;
			if (name.contains("float")) {
				baseType = IBasicType.t_float;
			} else if (name.contains("long double")) {
				baseType = IBasicType.t_double;
				qualifierBits |= ICPPBasicType.IS_LONG;
			} else if (name.contains("double")) {
				baseType = IBasicType.t_double;
			}
			break;
		case DwarfConstants.DW_ATE_imaginary_float:
			qualifierBits |= ICPPBasicType.IS_IMAGINARY;
			if (name.contains("float")) {
				baseType = IBasicType.t_float;
			} else if (name.contains("long double")) {
				baseType = IBasicType.t_double;
				qualifierBits |= ICPPBasicType.IS_LONG;
			} else if (name.contains("double")) {
				baseType = IBasicType.t_double;
			}
			break;
		case DwarfConstants.DW_ATE_void:
			baseType = IBasicType.t_void;
			break;
		case DwarfConstants.DW_ATE_address:
		case DwarfConstants.DW_ATE_packed_decimal:
		case DwarfConstants.DW_ATE_numeric_string:
		case DwarfConstants.DW_ATE_edited:
		case DwarfConstants.DW_ATE_signed_fixed:
		case DwarfConstants.DW_ATE_unsigned_fixed:
		case DwarfConstants.DW_ATE_decimal_float:
		default:
			break;
		}

		CPPBasicType type = new CPPBasicType(name, currentParentScope, baseType, qualifierBits, byteSize, null);
		type.setType(getTypeOrReference(attributeList, currentCUHeader));
		registerType(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, type);
	}

	private void processVariable(long offset, AttributeList attributeList, boolean isParameter) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, attributeList);

		AttributeValue locationAttribute = attributeList.getAttribute(DwarfConstants.DW_AT_location);
		ILocationProvider locationProvider = getLocationProvider(locationAttribute);
		if (locationProvider == null) {
			// No location means either this is a placeholder (in a subprogram declaration) 
			// or it may have been optimized out.  See section
			// 2.6 of the dwarf3 spec. for now we're ignoring it but we may be able
			// to show it in the view with some special decoration to indicate that
			// it's been optimized out
			
			// assume it is a forward reference if we're inside a function (formal_parameter or local)...
			provider.functionsByOffset.put(offset, attributeList);
			return;
		}
		// variables can have abstract origins with most of their contents
		CompilationUnitHeader otherCU = currentCUHeader;
		AttributeList otherAttributes = attributeList;
		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		if (name.length() == 0) {
			// no name. see if we can get it from the origin
			DereferencedAttributes deref = getDereferencedAttributes(attributeList, DwarfConstants.DW_AT_abstract_origin); 
			if (deref != null) {
				// this should either have a name or point to another
				// declaration
				otherCU = deref.header;
				otherAttributes = deref.attributeList;
				name = otherAttributes.getAttributeValueAsString(DwarfConstants.DW_AT_name);
			}
		}
		
		IType type = getTypeOrReference(otherAttributes.getAttribute(DwarfConstants.DW_AT_type), otherCU);
		if (type != null) {
			boolean global = (otherAttributes.getAttributeValueAsInt(DwarfConstants.DW_AT_external) == 1);

			long startScope = attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_start_scope);
			
			DwarfVariable variable = new DwarfVariable(name, 
							global ? moduleScope : currentParentScope, 
							locationProvider,
							type);

			variable.setStartScope(startScope);
			
			if (isParameter) {
				if (currentParentScope instanceof FunctionScope) {
					((FunctionScope) currentParentScope).addParameter(variable);
				} else {
					assert (false);
				}
			} else {
				if (global) {
					// add global variables to the module scope 
					moduleScope.addVariable(variable);
					// AND to the CU scope
					if (currentCompileUnitScope != null) {
						currentCompileUnitScope.addVariable(variable);
					}
				} else {
					// the parent scope could be compile unit, function or
					// lexical block
					currentParentScope.addVariable(variable);
				}

				// keep track of all variables by name for faster lookup
				List<IVariable> variables = provider.variablesByName.get(name);
				if (variables == null) {
					variables = new ArrayList<IVariable>();
					provider.variablesByName.put(name, variables);
				}
				variables.add(variable);
			}

			traceExit(IEDCTraceOptions.SYMBOL_READER_VERBOSE_TRACE, variable);
		}
	}


	/**
	 * Dereference a DW_AT_type attribute, if present
	 * @param attributeMap the map of Long, AttributeValue from AttributeList or Object, Object from Type
	 * @return offset to referenced type or 0 if no type attribute 
	 */
	/*
	private long getTypeReference(Map<Short,Object> attributeMap, CompilationUnitHeader header) {
		AttributeValue typeAttribute = (AttributeValue) attributeMap.get(Short.valueOf(DwarfConstants.DW_AT_type));
		if (typeAttribute != null) {
			// get the offset into the .debug_info section
			long debugInfoOffset = ((Number) typeAttribute.value).longValue();
			if (typeAttribute.actualForm == DwarfConstants.DW_FORM_ref_addr) {
				// this is already relative to the .debug_info section
			} else {
				if (header == null) {
					header = (CompilationUnitHeader) attributeMap.get(CU_HEADER);
					if (header == null) 
						throw new IllegalStateException();
				}
				debugInfoOffset += header.debugInfoOffset;
			}
			return debugInfoOffset;
		}
		return 0;
	}
	 */
	private ILocationProvider getLocationProvider(AttributeValue locationValue) {
		if (locationValue != null) {
			if (locationValue.actualForm == DwarfConstants.DW_FORM_data4) {
				// location list
				List<LocationEntry> entryList = getLocationRecord(((Integer) locationValue.value).longValue());
				return new LocationList(entryList.toArray(new LocationEntry[entryList.size()]),
						exeReader.getByteOrder(),
						currentCUHeader.addressSize, currentParentScope);
			} else if (locationValue.actualForm == DwarfConstants.DW_FORM_block
					|| locationValue.actualForm == DwarfConstants.DW_FORM_block1
					|| locationValue.actualForm == DwarfConstants.DW_FORM_block2
					|| locationValue.actualForm == DwarfConstants.DW_FORM_block4) {
				// location expression
				IStreamBuffer locationData = new MemoryStreamBuffer((byte[]) locationValue.value, exeReader.getByteOrder());
				return new LocationExpression(locationData, 
						currentCUHeader.addressSize,
						currentParentScope);
			} else {
				// should not happen according to the spec
				assert (false);
			}
		}

		return null;
	}

	private void dumpSymbols() {
		if (DEBUG) {
			System.out.println("Module - " + toString());
			System.out.println("	Variables - " + moduleScope.getVariables().size());
			System.out.println("	Compile units - " + moduleScope.getChildren().size());
			System.out.println();

			for (IScope cu : moduleScope.getChildren()) {
				System.out.println("	Compile unit - " + cu.toString());
				System.out.println("		Variables - " + cu.getVariables().size());
				System.out.println("		Functions - " + cu.getChildren().size());
				System.out.println();

				for (IScope func : cu.getChildren()) {
					System.out.println("		Function - " + func.toString());
					System.out.println("			Variables - " + func.getVariables().size());
					System.out.println("			Parameters - " + ((IFunctionScope) func).getParameters().size());
					System.out.println("			Lexical blocks - " + func.getChildren().size());
					System.out.println();

					// not accurate: can contain IFunctionScope too!
					for (IScope block : func.getChildren()) {
						System.out.println("			Lexical block - " + block.toString());
						System.out.println("				Variables - " + block.getVariables().size());
						System.out.println();
					}
				}
			}
		}
	}

	private void trace(final String option, final Object methodArgument) {
		if (EDCDebugger.getDefault() != null) {
			EDCDebugger.getDefault().getTrace().trace(option, methodArgument.toString());
		}
	}

	private void traceEntry(final String option, final Object methodArgument) {
		if (EDCDebugger.getDefault() != null) {
			EDCDebugger.getDefault().getTrace().traceEntry(option, methodArgument);
		}
	}

	private void traceExit(final String option, final Object methodArgument) {
		if (EDCDebugger.getDefault() != null) {
			EDCDebugger.getDefault().getTrace().traceExit(option, methodArgument);
		}
	}

}
