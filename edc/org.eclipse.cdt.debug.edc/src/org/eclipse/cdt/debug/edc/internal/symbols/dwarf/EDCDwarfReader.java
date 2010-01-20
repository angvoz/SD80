/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.IEDCTraceOptions;
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
import org.eclipse.cdt.debug.edc.internal.symbols.IBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.internal.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ISection;
import org.eclipse.cdt.debug.edc.internal.symbols.ISymbol;
import org.eclipse.cdt.debug.edc.internal.symbols.IVariable;
import org.eclipse.cdt.debug.edc.internal.symbols.InheritanceType;
import org.eclipse.cdt.debug.edc.internal.symbols.LexicalBlockScope;
import org.eclipse.cdt.debug.edc.internal.symbols.PointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.ReferenceType;
import org.eclipse.cdt.debug.edc.internal.symbols.Scope;
import org.eclipse.cdt.debug.edc.internal.symbols.Section;
import org.eclipse.cdt.debug.edc.internal.symbols.StructType;
import org.eclipse.cdt.debug.edc.internal.symbols.Symbol;
import org.eclipse.cdt.debug.edc.internal.symbols.Type;
import org.eclipse.cdt.debug.edc.internal.symbols.TypedefType;
import org.eclipse.cdt.debug.edc.internal.symbols.UnionType;
import org.eclipse.cdt.debug.edc.internal.symbols.VolatileType;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.cdt.utils.ERandomAccessFile;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.cdt.utils.coff.Coff.SectionHeader;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.PHdr;
import org.eclipse.core.runtime.IPath;

public class EDCDwarfReader extends Scope implements IEDCSymbolReader, IModuleScope {

	// TODO 64-bit Dwarf currently unsupported

	/* Section names. */
	final static String DWARF_DEBUG_INFO = ".debug_info"; //$NON-NLS-1$
	final static String DWARF_DEBUG_ABBREV = ".debug_abbrev"; //$NON-NLS-1$
	final static String DWARF_DEBUG_ARANGES = ".debug_aranges"; //$NON-NLS-1$
	final static String DWARF_DEBUG_LINE = ".debug_line"; //$NON-NLS-1$
	final static String DWARF_DEBUG_FRAME = ".debug_frame"; //$NON-NLS-1$
	final static String DWARF_EH_FRAME = ".eh_frame"; //$NON-NLS-1$
	final static String DWARF_DEBUG_LOC = ".debug_loc"; //$NON-NLS-1$
	final static String DWARF_DEBUG_PUBNAMES = ".debug_pubnames"; //$NON-NLS-1$
	final static String DWARF_DEBUG_STR = ".debug_str"; //$NON-NLS-1$
	final static String DWARF_DEBUG_FUNCNAMES = ".debug_funcnames"; //$NON-NLS-1$
	final static String DWARF_DEBUG_TYPENAMES = ".debug_typenames"; //$NON-NLS-1$
	final static String DWARF_DEBUG_VARNAMES = ".debug_varnames"; //$NON-NLS-1$
	final static String DWARF_DEBUG_WEAKNAMES = ".debug_weaknames"; //$NON-NLS-1$
	final static String DWARF_DEBUG_MACINFO = ".debug_macinfo"; //$NON-NLS-1$

	// only map the sections we need to save space - add here as needed. note
	// that DWARF_DEBUG_INFO is handled as a special case since it can be very
	// large and we don't want to map it into memory
	final static String[] MINIMAL_DWARF_SCNNAMES = { DWARF_DEBUG_ABBREV, DWARF_DEBUG_LINE, DWARF_DEBUG_LOC,
			DWARF_DEBUG_STR };

	private class CompositeNest {
		private final long siblingOffset;
		private final ICompositeType type;

		CompositeNest(long siblingOffset, ICompositeType type) {
			this.siblingOffset = siblingOffset;
			this.type = type;
		}

		public long getSiblingOffset() {
			return this.siblingOffset;
		}

		public ICompositeType getType() {
			return this.type;
		}
	}

	private class DebugInfoSectionInfo {

		long fileOffset;
		long sectionSize;

		DebugInfoSectionInfo(long fileOffset, long sectionSize) {
			this.fileOffset = fileOffset;
			this.sectionSize = sectionSize;
		}
	}

	private class CompilationUnitHeader {
		int length;
		short version;
		int abbreviationOffset;
		byte addressSize;
		long debugInfoOffset;

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Length: " + length).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("Version: " + version).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("Abbreviation: " + abbreviationOffset).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("Address size: " + addressSize).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			return sb.toString();
		}
	}

	private class AbbreviationEntry {
		/* unsigned */
		long tag;
		List<Attribute> attributes;

		AbbreviationEntry(long c, long t, byte h) {
			tag = t;
			attributes = new ArrayList<Attribute>();
		}
	}

	private class Attribute {
		/* unsigned */
		long name;
		/* unsigned */
		long form;

		Attribute(long n, long f) {
			name = n;
			form = f;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("name: " + Long.toHexString(name)); //$NON-NLS-1$
			sb.append(" value: " + Long.toHexString(form)); //$NON-NLS-1$
			return sb.toString();
		}
	}

	private class AttributeValue {
		Attribute attribute;
		Object value;

		// for indirect form, this is the actual form
		long actualForm;

		AttributeValue(Attribute a, ByteBuffer in, byte addressSize) {
			attribute = a;
			actualForm = attribute.form;

			try {
				value = readAttribute(in, addressSize);
			} catch (IOException e) {
				EDCDebugger.getMessageLogger().logError(null, e);
			}
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(attribute.toString()).append(' ');
			if (value != null) {
				Class<? extends Object> clazz = value.getClass();
				if (clazz.isArray()) {
					int len = Array.getLength(value);
					sb.append(len).append(' ');
					sb.append(clazz.getComponentType().toString());
					sb.append(':');
					for (int i = 0; i < len; i++) {
						byte b = Array.getByte(value, i);
						sb.append(' ').append(Integer.toHexString(b));
					}
				} else {
					if (value instanceof Number) {
						Number n = (Number) value;
						sb.append(Long.toHexString(n.longValue()));
					} else if (value instanceof String) {
						sb.append(value);
					} else {
						sb.append(value);
					}
				}
			}
			return sb.toString();
		}

		private Object readAttribute(ByteBuffer in, byte addressSize) throws IOException {
			Object obj = null;
			switch ((int) actualForm) {
			case DwarfConstants.DW_FORM_addr:
			case DwarfConstants.DW_FORM_ref_addr:
				obj = readAddress(in, addressSize);
				break;

			case DwarfConstants.DW_FORM_block: {
				int size = (int) read_unsigned_leb128(in);
				byte[] bytes = new byte[size];
				in.get(bytes);
				obj = bytes;
			}
				break;

			case DwarfConstants.DW_FORM_block1: {
				int size = in.get();
				byte[] bytes = new byte[size];
				in.get(bytes);
				obj = bytes;
			}
				break;

			case DwarfConstants.DW_FORM_block2: {
				int size = read_2_bytes(in);
				byte[] bytes = new byte[size];
				in.get(bytes);
				obj = bytes;
			}
				break;

			case DwarfConstants.DW_FORM_block4: {
				int size = read_4_bytes(in);
				byte[] bytes = new byte[size];
				in.get(bytes);
				obj = bytes;
			}
				break;

			case DwarfConstants.DW_FORM_data1:
				obj = new Byte(in.get());
				break;

			case DwarfConstants.DW_FORM_data2:
				obj = new Short(read_2_bytes(in));
				break;

			case DwarfConstants.DW_FORM_data4:
				obj = new Integer(read_4_bytes(in));
				break;

			case DwarfConstants.DW_FORM_data8:
				obj = new Long(read_8_bytes(in));
				break;

			case DwarfConstants.DW_FORM_sdata:
				obj = new Long(read_signed_leb128(in));
				break;

			case DwarfConstants.DW_FORM_udata:
				obj = new Long(read_unsigned_leb128(in));
				break;

			case DwarfConstants.DW_FORM_string: {
				int c;
				StringBuffer sb = new StringBuffer();
				while ((c = in.get()) != -1) {
					if (c == 0) {
						break;
					}
					sb.append((char) c);
				}
				obj = sb.toString();
			}
				break;

			case DwarfConstants.DW_FORM_flag:
				obj = new Byte(in.get());
				break;

			case DwarfConstants.DW_FORM_strp: {
				int offset = read_4_bytes(in);
				ByteBuffer data = getDwarfSection(DWARF_DEBUG_STR);
				if (data == null) {
					obj = new String();
				} else if (offset < 0 || offset > data.capacity()) {
					obj = new String();
				} else {
					StringBuffer sb = new StringBuffer();
					data.position(offset);
					while (data.hasRemaining()) {
						byte c = data.get();
						if (c == 0) {
							break;
						}
						sb.append((char) c);
					}
					obj = sb.toString();
				}
			}
				break;

			case DwarfConstants.DW_FORM_ref1:
				obj = new Byte(in.get());
				break;

			case DwarfConstants.DW_FORM_ref2:
				obj = new Short(read_2_bytes(in));
				break;

			case DwarfConstants.DW_FORM_ref4:
				obj = new Integer(read_4_bytes(in));
				break;

			case DwarfConstants.DW_FORM_ref8:
				obj = new Long(read_8_bytes(in));
				break;

			case DwarfConstants.DW_FORM_ref_udata:
				obj = new Long(read_unsigned_leb128(in));
				break;

			case DwarfConstants.DW_FORM_indirect: {
				actualForm = read_unsigned_leb128(in);
				return readAttribute(in, addressSize);
			}

			default:
				assert (false);
				break;
			}

			return obj;
		}
	}

	class AttributeList {

		Map<Long, AttributeValue> attributeMap;

		AttributeList(AbbreviationEntry entry, ByteBuffer in, byte addressSize) {

			int len = entry.attributes.size();
			attributeMap = new HashMap<Long, AttributeValue>(len);
			for (int i = 0; i < len; i++) {
				Attribute attr = entry.attributes.get(i);
				attributeMap.put(attr.name, new AttributeValue(attr, in, addressSize));
			}

		}

		public Collection<AttributeValue> values() {
			return attributeMap.values();
		}

		public long getAttributeValueAsLong(long attributeName) {
			AttributeValue attr = attributeMap.get(attributeName);
			if (attr != null) {
				Number number = ((Number) attr.value);
				if (number != null)
					return number.longValue();
			}
			return 0;
		}

		public int getAttributeValueAsInt(long attributeName) {
			AttributeValue attr = attributeMap.get(attributeName);
			if (attr != null) {
				Number number = ((Number) attr.value);
				if (number != null)
					return number.intValue();
			}
			return 0;
		}

		public String getAttributeValueAsString(long attributeName) {
			AttributeValue attr = attributeMap.get(attributeName);
			if (attr != null) {
				String result = (String) attr.value;
				if (result != null)
					return result;
			}
			return "";
		}

		public byte[] getAttributeValueAsBytes(long attributeName) {
			AttributeValue attr = attributeMap.get(attributeName);
			if (attr != null) {
				byte[] result = (byte[]) attr.value;
				if (result != null)
					return result;
			}
			return new byte[0];
		}

		public AttributeValue getAttribute(long attributeName) {
			return attributeMap.get(attributeName);
		}
	}

	private List<ISection> sections = new ArrayList<ISection>();
	private Map<String, ByteBuffer> dwarfSections = new HashMap<String, ByteBuffer>();
	private List<ISymbol> symbols = new ArrayList<ISymbol>();
	private Map<IPath, ICompileUnitScope> compileUnits = new HashMap<IPath, ICompileUnitScope>();
	private Map<Integer, Map<Long, AbbreviationEntry>> abbreviationMaps = new HashMap<Integer, Map<Long, AbbreviationEntry>>();
	private Map<Long, List<LocationEntry>> locationEntriesByOffset = new HashMap<Long, List<LocationEntry>>();

	// function and type declarations can be referenced by offsets relative to
	// the compile unit or to the entire .debug_info section. therefore we keep
	// maps by .debug_info offset, and for compile unit relative offsets, we
	// just add the compile unit offset into the .debug_info section.
	protected Map<Long, AttributeList> functionsByOffset = new HashMap<Long, AttributeList>();
	protected Map<Long, Type> typesByOffset = new HashMap<Long, Type>();

	// these are just for faster lookups
	protected Map<String, List<IFunctionScope>> functionsByName = new HashMap<String, List<IFunctionScope>>();
	protected Map<String, List<IVariable>> variablesByName = new HashMap<String, List<IVariable>>();

	private IPath symbolFilePath;
	private boolean isLE;
	private IAddress exeBaseAddress;
	private long modificationDate;
	private DebugInfoSectionInfo debugInfoSectionInfo;
	private CompilationUnitHeader currentCUHeader;

	private Set<String> referencedFiles = new HashSet<String>();
	private boolean buildReferencedFilesList = false;

	private Scope currentParentScope;
	private DwarfCompileUnit currentCompileUnitScope;
	private FunctionScope currentFunctionScope;
	private ArrayType currentArrayType;
	private Enumeration currentEnumType;

	private boolean isParsed = false;

	private boolean DEBUG = false;

	// TODO for temporary use
	private static long CU_DEBUG_INFO_OFFSET = 0x5555;

	public EDCDwarfReader(IPath binaryFile) throws IOException {
		super("", null, null, null);

		// Check to see if there is a sym file we should use for the symbols
		//
		// Note: there may be for "foo.exe" --> "foo.exe.sym" or "foo.sym"
		//
		IPath symFile = binaryFile.removeFileExtension().addFileExtension("sym");
		if (symFile.toFile().exists()) {
			symbolFilePath = symFile;
		} else {
			symFile = binaryFile.addFileExtension("sym");
			if (symFile.toFile().exists()) {
				symbolFilePath = symFile;
			} else {
				symbolFilePath = binaryFile;
			}
		}

		this.name = symbolFilePath.lastSegment();

		modificationDate = symbolFilePath.toFile().lastModified();

		recordSections();

		// TODO should we start parsing in a background job? ensure parsed would
		// then block until the job has completed
	}

	protected ByteBuffer getDwarfSection(String sectionName) {
		return dwarfSections.get(sectionName);
	}

	/**
	 * Determine the executable format and record the sections.
	 * 
	 * @throws IOException
	 *             if not ELF or PE, or not Dwarf
	 */
	private void recordSections() throws IOException {
		// start from zero so that we can use it as index to the array list
		// for quick access.
		int id = 0;
		Map<String, Object> props;

		// First check if it's an Elf file.
		try {
			// If this constructor succeeds, it's Elf
			Elf elfFile = new Elf(symbolFilePath.toOSString());

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

					sections.add(new Section(id++, s.p_memsz, s.p_vaddr, props));
				}
			}

			mapElfSections(elfFile);

			// load the symbol table
			elfFile.loadSymbols();
			for (org.eclipse.cdt.utils.elf.Elf.Symbol symbol : elfFile.getSymtabSymbols()) {
				String name = symbol.toString();
				if (name.length() > 0) {
					symbols.add(new Symbol(symbol.toString(), symbol.st_value, symbol.st_size));
				}
			}

			// now sort it by address for faster lookups
			Collections.sort(symbols);

			elfFile.dispose();
		} catch (IOException e) {
			// Check if it's PECOFF file
			try {
				// If this constructor succeeds, it's PE
				PE peFile = new PE(symbolFilePath.toOSString());

				SectionHeader[] secHeaders = peFile.getSectionHeaders();
				long imageBase = peFile.getNTOptionalHeader().ImageBase & 0xffffffffL;

				for (SectionHeader s : secHeaders) {
					String name;
					int i;
					for (i = 0; i < s.s_name.length; i++)
						if (s.s_name[i] == 0)
							break;

					String sectionName = new String(s.s_name, 0, i);
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

				mapPESections(peFile);

				// load the symbol table
				for (org.eclipse.cdt.utils.coff.Coff.Symbol symbol : peFile.getSymbols()) {
					symbols.add(new Symbol(symbol.toString(), new Addr32(symbol.n_value), 1));
				}

				// now sort it by address for faster lookups
				Collections.sort(symbols);

				peFile.dispose();
			} catch (IOException e2) {
				throw new IOException(symbolFilePath.toOSString() + " is not an ELF or PE file");
			}
		}

		if (debugInfoSectionInfo == null) {
			// No, we need to keep the section data and symbol table data for the module
			// even if there is no dwarf data.
//			throw new IOException(symbolFilePath.toOSString() + " is not a Dwarf file");
		}
	}

	private void mapElfSections(Elf exe) throws IOException {
		Elf.ELFhdr header = exe.getELFhdr();
		isLE = header.e_ident[Elf.ELFhdr.EI_DATA] == Elf.ELFhdr.ELFDATA2LSB;
		exeBaseAddress = getExeSegment(exe).p_vaddr;
		modificationDate = new File(exe.getFilename()).lastModified();

		Elf.Section[] sections = exe.getSections();
		for (org.eclipse.cdt.utils.elf.Elf.Section section : sections) {
			String name = section.toString();
			for (String element : MINIMAL_DWARF_SCNNAMES) {
				if (name.equals(element)) {
					try {
						// try to load the section into memory because it will
						// be faster
						dwarfSections.put(element, ByteBuffer.wrap(section.loadSectionData()));
					} catch (Throwable e) {
						// if that fails then try to map it into memory. this is
						// a little slower  but should work unless the section
						// is really large. note even for huge (500MB) sym files,
						// the only section that's ever too big for memory mapping
						// is the .debug_info section which we handle
						// differently anyway
						try {
							dwarfSections.put(element, section.mapSectionData());
						} catch (Throwable e2) {
							EDCDebugger.getMessageLogger().logError(null, e2);
						}
					}

					break;
				}
			}

			if (name.equals(DWARF_DEBUG_INFO)) {
				debugInfoSectionInfo = new DebugInfoSectionInfo(section.sh_offset, section.sh_size);
			}
		}
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

	private void mapPESections(PE exe) throws IOException {
		isLE = true;
		exeBaseAddress = new Addr32(exe.getNTOptionalHeader().ImageBase);
		modificationDate = new File(exe.getFilename()).lastModified();

		for (SectionHeader section : exe.getSectionHeaders()) {
			String name = new String(section.s_name).trim();
			if (name.startsWith("/")) //$NON-NLS-1$
			{
				int stringTableOffset = Integer.parseInt(name.substring(1));
				name = exe.getStringTableEntry(stringTableOffset);
			}
			for (String element : MINIMAL_DWARF_SCNNAMES) {
				if (name.equals(element)) {
					try {
						dwarfSections.put(element, section.mapSectionData());
					} catch (Exception e) {
						EDCDebugger.getMessageLogger().logError(null, e);
					}

					break;
				}
			}

			if (name.equals(DWARF_DEBUG_INFO)) {
				debugInfoSectionInfo = new DebugInfoSectionInfo(section.s_scnptr, section.s_paddr);
			}
		}

	}

	private void ensureParsed() {
		if (!isParsed) {
			parse();
		}
	}

	int read_4_bytes(ByteBuffer in) throws IOException {
		try {
			byte[] bytes = new byte[4];
			in.get(bytes);
			return read_4_bytes(bytes);
		} catch (Exception e) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.missingBytes")); //$NON-NLS-1$
		}
	}

	// FIXME:This is wrong, it's signed.
	int read_4_bytes(byte[] bytes) throws IndexOutOfBoundsException {
		if (isLE) {
			return (((bytes[3] & 0xff) << 24) | ((bytes[2] & 0xff) << 16) | ((bytes[1] & 0xff) << 8) | (bytes[0] & 0xff));
		}
		return (((bytes[0] & 0xff) << 24) | ((bytes[1] & 0xff) << 16) | ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff));
	}

	long read_8_bytes(ByteBuffer in) throws IOException {
		try {
			byte[] bytes = new byte[8];
			in.get(bytes);
			return read_8_bytes(bytes);
		} catch (Exception e) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.missingBytes")); //$NON-NLS-1$
		}
	}

	// FIXME:This is wrong, for unsigned.
	long read_8_bytes(byte[] bytes) throws IndexOutOfBoundsException {

		if (isLE) {
			return (((bytes[7] & 0xff) << 56) | ((bytes[6] & 0xff) << 48) | ((bytes[5] & 0xff) << 40)
					| ((bytes[4] & 0xff) << 32) | ((bytes[3] & 0xff) << 24) | ((bytes[2] & 0xff) << 16)
					| ((bytes[1] & 0xff) << 8) | (bytes[0] & 0xff));
		}

		return (((bytes[0] & 0xff) << 56) | ((bytes[1] & 0xff) << 48) | ((bytes[2] & 0xff) << 40)
				| ((bytes[3] & 0xff) << 32) | ((bytes[4] & 0xff) << 24) | ((bytes[5] & 0xff) << 16)
				| ((bytes[6] & 0xff) << 8) | (bytes[7] & 0xff));
	}

	short read_2_bytes(ByteBuffer in) throws IOException {
		try {
			byte[] bytes = new byte[2];
			in.get(bytes);
			return read_2_bytes(bytes);
		} catch (Exception e) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.missingBytes")); //$NON-NLS-1$
		}
	}

	short read_2_bytes(byte[] bytes) throws IndexOutOfBoundsException {
		if (isLE) {
			return (short) (((bytes[1] & 0xff) << 8) + (bytes[0] & 0xff));
		}
		return (short) (((bytes[0] & 0xff) << 8) + (bytes[1] & 0xff));
	}

	/* unsigned */
	long read_unsigned_leb128(ByteBuffer in) throws IOException {
		/* unsigned */
		long result = 0;
		int shift = 0;
		short b;

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

	/* unsigned */
	public long read_signed_leb128(ByteBuffer in) throws IOException {
		/* unsigned */
		long result = 0;
		int shift = 0;
		int size = 32;
		short b;

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

	private void parseDebugInfo() {

		// this does a full parse of the dwarf data. if we haven't built the
		// referenced files list from a quick parse yet, flag it here so we
		// can build the file list as we parse.
		if (referencedFiles.isEmpty()) {
			buildReferencedFilesList = true;
		}

		if (debugInfoSectionInfo == null) {	// no dwarf data.
			isParsed = true;
			return;
		}
		
		ERandomAccessFile symFile = null;

		try {
			// get the compile units out of the .debug_info section
			symFile = new ERandomAccessFile(symbolFilePath.toOSString(), "r"); //$NON-NLS-1$

			long fileIndex = debugInfoSectionInfo.fileOffset;
			symFile.seek(fileIndex);

			long fileEndIndex = debugInfoSectionInfo.fileOffset + debugInfoSectionInfo.sectionSize;

			while (fileIndex < fileEndIndex) {
				currentCUHeader = new CompilationUnitHeader();

				// read the length of the compile unit from the file
				byte[] lengthBytes = new byte[4];
				symFile.read(lengthBytes);
				currentCUHeader.length = read_4_bytes(ByteBuffer.wrap(lengthBytes));

				// now read the whole compile unit into memory. note that we're
				// reading the whole section including the size that we already
				// read because other code will use the offset of the buffer as
				// the offset of the section to store things by offset (types,
				// function declarations, etc).
				byte[] bytes = new byte[currentCUHeader.length + 4];
				symFile.seek(fileIndex);
				symFile.read(bytes);

				ByteBuffer data = ByteBuffer.wrap(bytes);

				// skip over the length since we already know it
				data.position(4);

				currentCUHeader.version = read_2_bytes(data);
				currentCUHeader.abbreviationOffset = read_4_bytes(data);
				currentCUHeader.addressSize = data.get();
				currentCUHeader.debugInfoOffset = fileIndex - debugInfoSectionInfo.fileOffset;

				// read the abbrev section.
				Map<Long, AbbreviationEntry> abbrevs = parseDebugAbbreviation(currentCUHeader.abbreviationOffset);
				parseDebugInfoEntry(data, abbrevs, currentCUHeader);

				// skip past the compile unit. note that the
				// currentCUHeader.length does not include
				// the size of the unit length itself
				fileIndex += currentCUHeader.length + 4;
				symFile.seek(fileIndex);
			}

			isParsed = true;

		} catch (IOException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		} finally {
			if (symFile != null) {
				try {
					symFile.close();
				} catch (IOException e) {
					EDCDebugger.getMessageLogger().logError(null, e);
				}
			}

			buildReferencedFilesList = false;
		}
	}

	/**
	 * Does a quick parse of the .debug_info section just to get a list of
	 * referenced files from the compile units.
	 */
	private void quickParseDebugInfo() {

		ERandomAccessFile symFile = null;

		if (debugInfoSectionInfo == null) {	// no dwarf data.
			return;
		}
		
		try {
			// get the compile units out of the .debug_info section
			symFile = new ERandomAccessFile(symbolFilePath.toOSString(), "r"); //$NON-NLS-1$

			long fileIndex = debugInfoSectionInfo.fileOffset;
			symFile.seek(fileIndex);

			long fileEndIndex = debugInfoSectionInfo.fileOffset + debugInfoSectionInfo.sectionSize;

			while (fileIndex < fileEndIndex) {
				byte[] lengthBytes = new byte[4];
				symFile.read(lengthBytes);
				int unit_length = read_4_bytes(ByteBuffer.wrap(lengthBytes));

				byte[] bytes = new byte[unit_length];
				symFile.seek(fileIndex + 4);
				symFile.read(bytes);

				ByteBuffer data = ByteBuffer.wrap(bytes);

				data.position(data.position() + 2); // skip version
				int debug_abbrev_offset = read_4_bytes(data);
				byte address_size = data.get();

				// get the abbreviation entry for the compile unit
				// find the offset to the
				Map<Long, AbbreviationEntry> abbrevs = parseDebugAbbreviation(debug_abbrev_offset);

				long code = read_unsigned_leb128(data);
				AbbreviationEntry entry = abbrevs.get(new Long(code));
				AttributeList attributes = new AttributeList(entry, data, address_size);

				// get comp_dir and name and figure out the path
				String name = attributes.getAttributeValueAsString(DwarfConstants.DW_AT_name);
				String compDir = attributes.getAttributeValueAsString(DwarfConstants.DW_AT_comp_dir);

				IPath filePath = DwarfHelper.normalizeFilePath(compDir, name, symbolFilePath);
				referencedFiles.add(filePath.toOSString());

				// do a quick parse of the line table to get any other
				// referenced files
				int stmtList = attributes.getAttributeValueAsInt(DwarfConstants.DW_AT_stmt_list);
				quickParseLineInfo(stmtList, compDir);

				// skip past the compile unit. note that the unit_length does
				// not include the size of the unit length itself
				fileIndex += unit_length + 4;
				symFile.seek(fileIndex);
			}

		} catch (Exception e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		} finally {
			if (symFile != null) {
				try {
					symFile.close();
				} catch (IOException e) {
					EDCDebugger.getMessageLogger().logError(null, e);
				}
			}
		}
	}

	/**
	 * Does a quick parse of the .debug_line section just to get a list of
	 * referenced files from the line table.
	 */
	private void quickParseLineInfo(int lineTableOffset, String compileUnitDirectory) {
		IPath compileUnitDirectoryPath = PathUtils.createPath(compileUnitDirectory);
		try {
			// do a quick parse of the line table just to get referenced files
			ByteBuffer data = getDwarfSection(DWARF_DEBUG_LINE);
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
				int opcode_base = data.get();
				data.position(data.position() + opcode_base - 1);

				// include_directories
				ArrayList<String> dirList = new ArrayList<String>();

				// add the compilation directory of the CU as the first
				// directory
				dirList.add(compileUnitDirectory);

				while (true) {
					String str = DwarfHelper.readString(data);
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
					String fileName = DwarfHelper.readString(data);
					if (fileName.length() == 0) // no more file entry
						break;

					// dir index
					long leb128 = read_unsigned_leb128(data);

					IPath fullPath = DwarfHelper.normalizeFilePath(dirList.get((int) leb128), fileName, symbolFilePath);
					if (fullPath != null) {
						referencedFiles.add(fullPath.toOSString());
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

	private Map<Long, AbbreviationEntry> parseDebugAbbreviation(int abbreviationOffset) throws IOException {
		Integer key = new Integer(abbreviationOffset);
		Map<Long, AbbreviationEntry> abbrevs = abbreviationMaps.get(key);
		if (abbrevs == null) {
			abbrevs = new HashMap<Long, AbbreviationEntry>();
			abbreviationMaps.put(key, abbrevs);
			ByteBuffer data = getDwarfSection(DWARF_DEBUG_ABBREV);
			if (data != null) {
				data.position(abbreviationOffset);
				while (data.remaining() > 0) {
					long code = read_unsigned_leb128(data);
					if (code == 0) {
						break;
					}
					long tag = read_unsigned_leb128(data);
					byte hasChildren = data.get();
					AbbreviationEntry entry = new AbbreviationEntry(code, tag, hasChildren);

					// attributes
					long name = 0;
					long form = 0;
					do {
						name = read_unsigned_leb128(data);
						form = read_unsigned_leb128(data);
						if (name != 0) {
							entry.attributes.add(new Attribute(name, form));
						}
					} while (name != 0 && form != 0);

					abbrevs.put(new Long(code), entry);
				}
			}
		}
		return abbrevs;
	}

	private void parseDebugInfoEntry(ByteBuffer in, Map<Long, AbbreviationEntry> abbrevs, CompilationUnitHeader header)
			throws IOException {

		// FIFO nesting of structures found reading the DWARF info - used to
		// tell which type a member is part of
		ArrayList<CompositeNest> compositeNesting = new ArrayList<CompositeNest>();

		while (in.remaining() > 0) {
			long offset = in.position() + currentCUHeader.debugInfoOffset;
			long code = read_unsigned_leb128(in);
			AbbreviationEntry entry = abbrevs.get(new Long(code));

			if (entry != null) {
				processDebugInfoEntry(offset, entry, new AttributeList(entry, in, header.addressSize), header,
						compositeNesting);
			}
		}
	}

	private void processDebugInfoEntry(long offset, AbbreviationEntry entry, AttributeList attributeList,
			CompilationUnitHeader header, ArrayList<CompositeNest> compositeNesting) {
		int tag = (int) entry.tag;

		// We are only interested in certain tags.
		switch (tag) {
		case DwarfConstants.DW_TAG_array_type:
			processArrayType(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_class_type:
			processClassType(offset, attributeList, header, compositeNesting);
			break;
		case DwarfConstants.DW_TAG_enumeration_type:
			processEnumType(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_formal_parameter:
			processVariable(attributeList, true);
			break;
		case DwarfConstants.DW_TAG_lexical_block:
			processLexicalBlock(attributeList);
			break;
		case DwarfConstants.DW_TAG_member:
			processField(offset, attributeList, header, compositeNesting);
			break;
		case DwarfConstants.DW_TAG_pointer_type:
			processPointerType(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_reference_type:
			processReferenceType(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_compile_unit:
			processCompileUnit(attributeList);
			break;
		case DwarfConstants.DW_TAG_structure_type:
			processStructType(offset, attributeList, header, compositeNesting);
			break;
		case DwarfConstants.DW_TAG_subroutine_type:
			break;
		case DwarfConstants.DW_TAG_typedef:
			processTypeDef(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_union_type:
			processUnionType(offset, attributeList, header, compositeNesting);
			break;
		case DwarfConstants.DW_TAG_unspecified_parameters:
			break;
		case DwarfConstants.DW_TAG_inheritance:
			processInheritance(offset, attributeList, header, compositeNesting);
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
		case DwarfConstants.DW_TAG_template_type_param:
			break;
		case DwarfConstants.DW_TAG_template_value_param:
			break;
		case DwarfConstants.DW_TAG_thrown_type:
			break;
		case DwarfConstants.DW_TAG_try_block:
			break;
		case DwarfConstants.DW_TAG_variable:
			processVariable(attributeList, false);
			break;
		case DwarfConstants.DW_TAG_volatile_type:
			processVolatileType(offset, attributeList);
			break;
		case DwarfConstants.DW_TAG_subrange_type:
			processArrayBoundType(offset, attributeList, currentArrayType);
			break;
		}
	}

	public Long readAddress(ByteBuffer in, int addressSize) throws IOException {
		long value = 0;

		switch (addressSize) {
		case 2:
			value = read_2_bytes(in);
			break;
		case 4:
			value = read_4_bytes(in);
			break;
		case 8:
			value = read_8_bytes(in);
			break;
		default:
			// ????
		}
		return new Long(value);
	}

	private List<LocationEntry> getLocationRecord(long offset) {
		// first check the cache
		List<LocationEntry> entries = locationEntriesByOffset.get(offset);
		if (entries == null) {
			// not found so try to get the entries from the offset
			entries = new ArrayList<LocationEntry>();

			try {
				ByteBuffer data = getDwarfSection(DWARF_DEBUG_LOC);
				if (data != null) {
					data.position((int) offset);
					while (data.hasRemaining()) {

						long lowPC = readAddress(data, currentCUHeader.addressSize);
						long highPC = readAddress(data, currentCUHeader.addressSize);

						if (lowPC == 0 && highPC == 0) {
							// end of list entry
							break;
						} else {
							// location list entry
							int numOpCodes = read_2_bytes(data);
							byte[] bytes = new byte[numOpCodes];
							data.get(bytes);
							LocationEntry entry = new LocationEntry(lowPC, highPC, bytes);
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

	private void processCompileUnit(AttributeList attributeList) {
		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		String compDir = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_comp_dir);

		IAddress low = new Addr32(attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_low_pc));
		IAddress high = new Addr32(attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_high_pc));

		IPath filePath = DwarfHelper.normalizeFilePath(compDir, name, getSymbolFile());

		// some compilers (RVCT) may generate multiple compile units for the
		// same file. in such cases, they typically have one main compile unit
		// that has address range, while the others just have other type
		// declarations, etc. since keeping multiple compile units for the
		// same file makes lookup by filename/path difficult, we'll merge the
		// contents of each duplicate compile entry into one
		currentCompileUnitScope = (DwarfCompileUnit) compileUnits.get(filePath);
		if (currentCompileUnitScope == null) {
			// first one. create it now.
			currentCompileUnitScope = new DwarfCompileUnit(this, filePath, low, high, attributeList);
			compileUnits.put(currentCompileUnitScope.getFilePath(), currentCompileUnitScope);
			addChild(currentCompileUnitScope);
		}

		currentParentScope = currentCompileUnitScope;

		if (currentCompileUnitScope.getLowAddress().isZero() && currentCompileUnitScope.getLowAddress().isZero()) {
			// the existing entry does not yet have an address range. set the
			// range of this compile unit and also the attributes since those
			// will be used to fetch the line table data. we want the line
			// table from the same compile unit that has address ranges.
			currentCompileUnitScope.setLowAddress(low);
			currentCompileUnitScope.setHighAddress(high);
			currentCompileUnitScope.setAttributes(attributeList);
		}

		if (buildReferencedFilesList) {
			referencedFiles.add(currentCompileUnitScope.getFilePath().toOSString());

			// do a quick parse of the line table to get any other referenced
			// files note that even the full parse doesn't parse the line table
			// information. that is calculated (and then cached) on demand
			int stmtList = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_stmt_list);
			quickParseLineInfo(stmtList, compDir);
		}
	}

	private void processLexicalBlock(AttributeList attributeList) {
		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);

		// get the high and low pc from the attributes list
		IAddress low = new Addr32(attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_low_pc));
		IAddress high = new Addr32(attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_high_pc));

		if (low.isZero() && high.isZero()) {
			// ignore any that don't have a valid range
			return;
		}

		LexicalBlockScope lb = new LexicalBlockScope(name, currentParentScope, low, high);

		currentParentScope.addChild(lb);
		currentParentScope = lb;
	}

	private void processSubProgram(long offset, AttributeList attributeList) {
		// if it's a declaration just add to the offsets map for later lookup
		if (attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_declaration) > 0) {
			functionsByOffset.put(offset, attributeList);
			return;
		}

		// get the high and low pc from the attributes list
		IAddress low = new Addr32(attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_low_pc));
		IAddress high = new Addr32(attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_high_pc));

		// functions with no high/low pc aren't real functions. just treat them
		// as declarations as they will be pointed to by abstract_origin from
		// another sub program tag
		if (low.isZero() && high.isZero()) {
			functionsByOffset.put(offset, attributeList);
			return;
		}

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		if (name.length() == 0) {
			// no name. see if we can get it from a declaration
			AttributeValue abstract_origin = attributeList.getAttribute(DwarfConstants.DW_AT_abstract_origin);
			if (abstract_origin != null) {
				// get the offset into the .debug_info section
				long debugInfoOffset = ((Number) abstract_origin.value).longValue();
				if (abstract_origin.actualForm == DwarfConstants.DW_FORM_ref_addr) {
					// this is already relative to the .debug_info section
				} else {
					debugInfoOffset += currentCUHeader.debugInfoOffset;
				}

				AttributeList attributes = functionsByOffset.get(debugInfoOffset);
				;
				if (attributes != null) {
					// this should either have a name or point to another
					// declaration
					name = attributes.getAttributeValueAsString(DwarfConstants.DW_AT_name);
					if (name.length() == 0) {
						AttributeValue specification = attributes.getAttribute(DwarfConstants.DW_AT_specification);
						if (specification != null) {
							// get the offset into the .debug_info section
							debugInfoOffset = ((Number) specification.value).longValue();
							if (specification.actualForm == DwarfConstants.DW_FORM_ref_addr) {
								// this is already relative to the .debug_info
								// section
							} else {
								debugInfoOffset += currentCUHeader.debugInfoOffset;
							}

							AttributeList declarationAttributes = functionsByOffset.get(debugInfoOffset);
							;
							if (declarationAttributes != null) {
								name = declarationAttributes.getAttributeValueAsString(DwarfConstants.DW_AT_name);
							}
						}
					}
				}
			} else {
				AttributeValue specification = attributeList.getAttribute(DwarfConstants.DW_AT_specification);
				if (specification != null) {
					// get the offset into the .debug_info section
					long debugInfoOffset = ((Number) specification.value).longValue();
					if (specification.actualForm == DwarfConstants.DW_FORM_ref_addr) {
						// this is already relative to the .debug_info section
					} else {
						debugInfoOffset += currentCUHeader.debugInfoOffset;
					}

					AttributeList attributes = functionsByOffset.get(debugInfoOffset);
					;
					if (attributes != null) {
						name = attributes.getAttributeValueAsString(DwarfConstants.DW_AT_name);
					}
				}
			}
		}

		if (name.length() == 0) {
			// the name should either be an attribute of the compile unit, or be
			// in the declaration which according to the spec will always be
			// before its definition in the Dwarf.
			return;
		}

		AttributeValue frameBaseAttribute = attributeList.getAttribute(DwarfConstants.DW_AT_frame_base);
		ILocationProvider locationProvider = getLocationProvider(frameBaseAttribute);

		currentFunctionScope = new FunctionScope(name, currentCompileUnitScope, low, high, locationProvider);
		currentParentScope = currentFunctionScope;
		currentCompileUnitScope.addChild(currentFunctionScope);

		// keep track of all functions by name for faster lookup
		List<IFunctionScope> functions = functionsByName.get(name);
		if (functions == null) {
			functions = new ArrayList<IFunctionScope>();
		}
		functions.add(currentFunctionScope);
		functionsByName.put(name, functions);
	}

	private void processClassType(long offset, AttributeList attributeList, CompilationUnitHeader header,
			ArrayList<CompositeNest> compositeNesting) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);
		long siblingOffset = attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_sibling);

		ClassType type = new ClassType(name, currentParentScope, byteSize, properties);
		adjustCompositeNesting(offset, siblingOffset, type, header, compositeNesting);
		typesByOffset.put(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processStructType(long offset, AttributeList attributeList, CompilationUnitHeader header,
			ArrayList<CompositeNest> compositeNesting) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);
		long siblingOffset = attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_sibling);

		StructType type = new StructType(name, currentParentScope, byteSize, properties);
		adjustCompositeNesting(offset, siblingOffset, type, header, compositeNesting);
		typesByOffset.put(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processUnionType(long offset, AttributeList attributeList, CompilationUnitHeader header,
			ArrayList<CompositeNest> compositeNesting) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);
		long siblingOffset = attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_sibling);

		UnionType type = new UnionType(name, currentParentScope, byteSize, properties);
		adjustCompositeNesting(offset, siblingOffset, type, header, compositeNesting);
		typesByOffset.put(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processInheritance(long offset, AttributeList attributeList, CompilationUnitHeader header,
			ArrayList<CompositeNest> compositeNesting) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);
		
		// if needed, remove structures whose definitions are finished
		while ((compositeNesting.size() > 0)
				&& (compositeNesting.get(0).getSiblingOffset() + header.debugInfoOffset <= offset))
			compositeNesting.remove(0);

		ICompositeType compositeType = null;

		// find the deepest nested (last added) compositeNesting member
		if (compositeNesting.size() > 0) {
			compositeType = (compositeNesting.get(0).getType());
		}

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
		
		InheritanceType type = new InheritanceType(currentParentScope, accessibility, fieldsOffset, properties);

		// add the member to the deepest nested (last added) compositeNesting
		// member
		if (compositeType != null)
			compositeType.addInheritance(type);
		typesByOffset.put(offset, type);
		
		
		typesByOffset.put(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

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

	private void processField(long offset, AttributeList attributeList, CompilationUnitHeader header,
			ArrayList<CompositeNest> compositeNesting) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
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

		// if needed, remove structures whose definitions are finished
		while ((compositeNesting.size() > 0)
				&& (compositeNesting.get(0).getSiblingOffset() + header.debugInfoOffset <= offset))
			compositeNesting.remove(0);

		ICompositeType compositeType = null;

		// find the deepest nested (last added) compositeNesting member
		if (compositeNesting.size() > 0) {
			compositeType = (compositeNesting.get(0).getType());
		}

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
				byteSize, accessibility, properties);

		// add the member to the deepest nested (last added) compositeNesting
		// member
		if (compositeType != null)
			compositeType.addField(type);
		typesByOffset.put(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processArrayType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);

		ArrayType type = new ArrayType(name, currentParentScope, byteSize, properties);
		typesByOffset.put(offset, type);
		currentArrayType = type;
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processArrayBoundType(long offset, AttributeList attributeList, ArrayType arrayParent) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		long arrayBound = 0;
		if (attributeList.getAttribute(DwarfConstants.DW_AT_upper_bound) != null)
			arrayBound = attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_upper_bound) + 1;

		ArrayBoundType type = new ArrayBoundType(currentParentScope, arrayBound);
		typesByOffset.put(offset, type);
		arrayParent.addBound(type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processReferenceType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);

		ReferenceType type = new ReferenceType(name, currentParentScope, properties);
		typesByOffset.put(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processPointerType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);

		PointerType type = new PointerType(name, currentParentScope, byteSize, properties);
		typesByOffset.put(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processConstType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

		ConstType type = new ConstType(currentParentScope, properties);
		typesByOffset.put(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processVolatileType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

		VolatileType type = new VolatileType(currentParentScope, properties);
		typesByOffset.put(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processEnumType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		int byteSize = attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_byte_size);

		Enumeration type = new Enumeration(name, currentParentScope, byteSize, properties);
		typesByOffset.put(offset, type);
		currentEnumType = type;
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processEnumerator(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);
		long value = attributeList.getAttributeValueAsLong(DwarfConstants.DW_AT_const_value);

		Enumerator enumerator = new Enumerator(name, value);
		currentParentScope.addEnumerator(enumerator);
		currentEnumType.addEnumerator(enumerator);

		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, enumerator);
	}

	private void processTypeDef(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);

		TypedefType type = new TypedefType(name, currentParentScope, properties);
		typesByOffset.put(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processBasicType(long offset, AttributeList attributeList) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, offset);

		HashMap<Object, Object> properties = new HashMap<Object, Object>(attributeList.attributeMap.size());
		properties.putAll(attributeList.attributeMap);
		properties.put(CU_DEBUG_INFO_OFFSET, currentCUHeader.debugInfoOffset);

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
			} else if (this.name.contains("long")) {
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
			} else if (this.name.contains("long")) {
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

		CPPBasicType type = new CPPBasicType(name, currentParentScope, baseType, qualifierBits, byteSize, properties);
		typesByOffset.put(offset, type);
		traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, type);
	}

	private void processVariable(AttributeList attributeList, boolean isParameter) {
		traceEntry(IEDCTraceOptions.SYMBOL_READER_TRACE, attributeList);

		String name = attributeList.getAttributeValueAsString(DwarfConstants.DW_AT_name);

		AttributeValue locationAttribute = attributeList.getAttribute(DwarfConstants.DW_AT_location);
		ILocationProvider locationProvider = getLocationProvider(locationAttribute);
		if (locationProvider == null) {
			// TODO no location - I believe this means it was in the source but
			// did not make it into the object code (optimized out?). see section
			// 2.6 of the dwarf3 spec. for now we're ignoring it but we may be able
			// to show it in the view with some special decoration to indicate that
			// it's been optimized out
			return;
		}

		AttributeValue typeAttribute = attributeList.getAttribute(DwarfConstants.DW_AT_type);
		if (typeAttribute != null) {
			// get the offset into the .debug_info section
			long debugInfoOffset = ((Number) typeAttribute.value).longValue();
			if (typeAttribute.actualForm == DwarfConstants.DW_FORM_ref_addr) {
				// this is already relative to the .debug_info section
			} else {
				debugInfoOffset += currentCUHeader.debugInfoOffset;
			}

			boolean global = (attributeList.getAttributeValueAsInt(DwarfConstants.DW_AT_external) == 1);

			DwarfVariable variable = new DwarfVariable(name, global ? this : currentParentScope, locationProvider,
					this, debugInfoOffset);

			if (isParameter) {
				if (currentFunctionScope != null) {
					currentFunctionScope.addParameter(variable);
				} else {
					assert (false);
				}
			} else {
				if (global) {
					// add global variables to the module scope
					addVariable(variable);
				} else {
					// the parent scope could be compile unit, function or
					// lexical block
					currentParentScope.addVariable(variable);
				}

				// keep track of all variables by name for faster lookup
				List<IVariable> variables = variablesByName.get(name);
				if (variables == null) {
					variables = new ArrayList<IVariable>();
				}
				variables.add(variable);
				variablesByName.put(name, variables);
			}

			traceExit(IEDCTraceOptions.SYMBOL_READER_TRACE, variable);
		}
	}

	private void parse() {
		parseDebugInfo();

		// fix up the type tree. this is a temporary solution until we move to
		// the symbolics-on-demand model. basically because some types may refer
		// to other types later in the symbolics which we haven't parsed yet, we
		// go through all of the types once we're done parsing and fix up the
		// references.
		for (Type type : typesByOffset.values()) {
			if (type.getProperties() != null) {
				AttributeValue typeAttribute = (AttributeValue) type.getProperties().get(
						Long.valueOf(DwarfConstants.DW_AT_type));
				if (typeAttribute != null) {
					// get the offset into the .debug_info section
					long debugInfoOffset = ((Number) typeAttribute.value).longValue();
					if (typeAttribute.actualForm == DwarfConstants.DW_FORM_ref_addr) {
						// this is already relative to the .debug_info section
					} else {
						long cuDebugInfoOffset = (Long) type.getProperties().get(CU_DEBUG_INFO_OFFSET);
						debugInfoOffset += cuDebugInfoOffset;
					}

					type.setType(typesByOffset.get(debugInfoOffset));
				}
			}
		}

		// fix up scope addresses in case compiler doesn't generate them.
		for (IScope cu : children) {
			((DwarfCompileUnit) cu).fixupScopes();
		}

		IAddress newLowAddress = new Addr64(BigInteger.valueOf(0xFFFFFFFFL));
		IAddress newHighAddress = new Addr64(BigInteger.valueOf(0));
		;

		// now fix up the module scope
		for (IScope cu : children) {
			if (cu.getLowAddress().compareTo(newLowAddress) < 0) {
				newLowAddress = cu.getLowAddress();
			}

			if (cu.getHighAddress().compareTo(newHighAddress) > 0) {
				newHighAddress = cu.getHighAddress();
			}
		}

		lowAddress = newLowAddress;
		highAddress = newHighAddress;

		if (DEBUG) {
			dumpSymbols();
		}
	}

	private ILocationProvider getLocationProvider(AttributeValue locationValue) {
		if (locationValue != null) {
			if (locationValue.actualForm == DwarfConstants.DW_FORM_data4) {
				// location list
				List<LocationEntry> entryList = getLocationRecord(((Integer) locationValue.value).longValue());
				return new LocationList(this, entryList.toArray(new LocationEntry[entryList.size()]),
						currentCUHeader.addressSize, currentParentScope);
			} else if (locationValue.actualForm == DwarfConstants.DW_FORM_block
					|| locationValue.actualForm == DwarfConstants.DW_FORM_block1
					|| locationValue.actualForm == DwarfConstants.DW_FORM_block2
					|| locationValue.actualForm == DwarfConstants.DW_FORM_block4) {
				// location expression
				return new LocationExpression(this, (byte[]) locationValue.value, currentCUHeader.addressSize,
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
			System.out.println("	Variables - " + getVariables().size());
			System.out.println("	Compile units - " + getChildren().size());
			System.out.println();

			for (IScope cu : getChildren()) {
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

					for (IScope block : func.getChildren()) {
						System.out.println("			Lexical block - " + block.toString());
						System.out.println("				Variables - " + block.getVariables().size());
						System.out.println();
					}
				}
			}
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

	public IPath getSymbolFile() {
		return symbolFilePath;
	}

	public Collection<ISection> getSections() {
		// this is set when the reader is created so we don't need to check
		// if we're parsed the dwarf for this
		return Collections.unmodifiableCollection(sections);
	}

	public IAddress getBaseLinkAddress() {
		// this is set when the reader is created so we don't need to check
		// if we're parsed the dwarf for this
		return exeBaseAddress;
	}

	public long getModificationDate() {
		// this is set when the reader is created so we don't need to check
		// if we're parsed the dwarf for this
		return modificationDate;
	}

	public Collection<ISymbol> getSymbols() {
		// this is set when the reader is created so we don't need to check
		// if we're parsed the dwarf for this
		return Collections.unmodifiableCollection(symbols);
	}

	public ISymbol getSymbolAtAddress(IAddress linkAddress) {
		// this is set when the reader is created so we don't need to check
		// if we're parsed the dwarf for this
		int insertion = Collections.binarySearch(symbols, linkAddress);
		if (insertion >= 0) {
			return symbols.get(insertion);
		}

		if (insertion == -1) {
			return null;
		}

		insertion = -insertion - 1;

		ISymbol symbol = symbols.get(insertion - 1);
		if (linkAddress.compareTo(symbol.getAddress().add(symbol.getSize())) < 0) {
			return symbol;
		} else {
			// symbol address may have the last bit set to indicate ARM's thumb
			// mode, in which case the address is really addr - 1
			symbol = symbols.get(insertion);
			if (symbol.getAddress().compareTo(linkAddress.add(1)) == 0) {
				return symbol;
			}
		}

		return null;
	}

	public Collection<IFunctionScope> getFunctionsByName(String name) {
		ensureParsed();
		List<IFunctionScope> result = functionsByName.get(name);
		if (result == null)
			return new ArrayList<IFunctionScope>(0);
		return Collections.unmodifiableCollection(result);
	}

	public Collection<IVariable> getVariablesByName(String name) {
		ensureParsed();
		List<IVariable> result = variablesByName.get(name);
		if (result == null)
			return new ArrayList<IVariable>(0);
		return Collections.unmodifiableCollection(result);
	}

	public void shutDown() {
		System.gc();
		System.runFinalization();
	}

	public String[] getSourceFiles() {
		if (referencedFiles.isEmpty()) {
			quickParseDebugInfo();
		}

		return referencedFiles.toArray(new String[referencedFiles.size()]);
	}

	public ICompileUnitScope getCompileUnitForAddress(IAddress linkAddress) {
		ensureParsed();

		IScope scope = getScopeAtAddress(linkAddress);
		while (scope != null && !(scope instanceof ICompileUnitScope)) {
			scope = scope.getParent();
		}

		return (ICompileUnitScope) scope;
	}

	public ICompileUnitScope getCompileUnitForFile(IPath filePath) {
		ensureParsed();

		return compileUnits.get(filePath);
	}

	@Override
	public Collection<IScope> getChildren() {
		ensureParsed();

		return super.getChildren();
	}

	@Override
	public Collection<IVariable> getVariables() {
		ensureParsed();

		return super.getVariables();
	}

	@Override
	public Collection<IEnumerator> getEnumerators() {
		ensureParsed();

		return super.getEnumerators();
	}

	@Override
	public IScope getScopeAtAddress(IAddress linkAddress) {
		ensureParsed();

		return super.getScopeAtAddress(linkAddress);
	}

	public DebugInformationType getRecognizedDebugInformationType() {
		return DebugInformationType.DWARF;
	}

	public boolean hasRecognizedDebugInformation() {
		ensureParsed();
		return debugInfoSectionInfo != null;
	}
}
