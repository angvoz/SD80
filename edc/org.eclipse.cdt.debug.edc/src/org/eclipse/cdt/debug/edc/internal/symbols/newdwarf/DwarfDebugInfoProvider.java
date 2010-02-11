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
package org.eclipse.cdt.debug.edc.internal.symbols.newdwarf;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.IStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IForwardTypeReference;
import org.eclipse.cdt.debug.edc.internal.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IType;
import org.eclipse.cdt.debug.edc.internal.symbols.IVariable;
import org.eclipse.cdt.debug.edc.internal.symbols.Scope;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IDebugInfoProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSection;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSymbolicsReader;
import org.eclipse.core.runtime.IPath;

/**
 * This class handles the low-level aspects of reading DWARF data.
 * There exists one provider per symbol file.
 */
public class DwarfDebugInfoProvider implements IDebugInfoProvider {

	/**
	 * Tell if this executable appears to support DWARF.
	 * @param reader
	 * @return true if key DWARF sections are detected
	 */
	public static boolean isDebugInfoDetected(IExecutableSymbolicsReader reader) {
		for (IExecutableSection section : reader.getExecutableSections()) {
			if (section.getName().equals(DwarfInfoReader.DWARF_DEBUG_INFO)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This represents a forward type reference, which is a type
	 * that resolves itself when referenced.
	 */
	static class ForwardTypeReference implements IType, IForwardTypeReference {

		static final IType NULL_TYPE_ENTRY = new IType() {

			public int getByteSize() {
				return 0;
			}

			public String getName() {
				return "<<unhandled type>>";
			}

			public Map<Object, Object> getProperties() {
				return Collections.emptyMap();
			}

			public IScope getScope() {
				return null;
			}

			public IType getType() {
				return null;
			}

			public void setType(IType type) {
				throw new IllegalStateException();
			}
		};
		
		private final DwarfDebugInfoProvider provider;
		private IType type = null;

		private final long offset;
		
		public ForwardTypeReference(DwarfDebugInfoProvider provider, long offset) {
			this.provider = provider;
			this.offset = offset;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.symbols.newdwarf.IForwardTypeReference#getReferencedType()
		 */
		public IType getReferencedType() {
			if (type == null) {
				// to prevent recursion
				type = NULL_TYPE_ENTRY;
				type = provider.resolveTypeReference(this);
				if (type == null) {
					// FIXME
					type = NULL_TYPE_ENTRY;
				}
			}
			return type;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.symbols.IType#getByteSize()
		 */
		public int getByteSize() {
			return getReferencedType().getByteSize();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.symbols.IType#getName()
		 */
		public String getName() {
			return getReferencedType().getName();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.symbols.IType#getProperties()
		 */
		public Map<Object, Object> getProperties() {
			return getReferencedType().getProperties();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.symbols.IType#getScope()
		 */
		public IScope getScope() {
			return getReferencedType().getScope();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.symbols.IType#getType()
		 */
		public IType getType() {
			return getReferencedType().getType();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.symbols.IType#setType(org.eclipse.cdt.debug.edc.internal.symbols.IType)
		 */
		public void setType(IType type_) {
			getReferencedType().setType(type_);
		}
	}
	
	static public class CompilationUnitHeader {
		int length;
		short version;
		int abbreviationOffset;
		byte addressSize;
		int debugInfoOffset;
		DwarfCompileUnit scope;
		
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
	
	static class AbbreviationEntry {
		short tag;
		ArrayList<Attribute> attributes;
		boolean hasChildren;

		AbbreviationEntry(long code, short tag, boolean hasChildren) {
			// abbreviation code not stored
			this.tag = tag;
			this.hasChildren = hasChildren;
			attributes = new ArrayList<Attribute>();
		}
	}

	static class Attribute {
		short tag;
		byte form;

		Attribute(short tag, byte form) {
			this.tag = tag;
			this.form = form;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("tag: " + Long.toHexString(tag)); //$NON-NLS-1$
			sb.append(" form: " + Long.toHexString(form)); //$NON-NLS-1$
			return sb.toString();
		}
	}

	static class AttributeValue {
		Object value;

		// for indirect form, this is the actual form
		byte actualForm;

		AttributeValue(byte form, IStreamBuffer in, byte addressSize, IStreamBuffer debugStrings) {
			actualForm = form;

			try {
				value = readAttribute(in, addressSize, debugStrings);
			} catch (IOException e) {
				EDCDebugger.getMessageLogger().logError(null, e);
			}
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
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

		private Object readAttribute(IStreamBuffer in, byte addressSize, IStreamBuffer debugStrings) throws IOException {
			Object obj = null;
			switch (actualForm) {
			case DwarfConstants.DW_FORM_addr:
			case DwarfConstants.DW_FORM_ref_addr:
				obj = DwarfInfoReader.readAddress(in, addressSize);
				break;

			case DwarfConstants.DW_FORM_block: {
				int size = (int) DwarfInfoReader.read_unsigned_leb128(in);
				byte[] bytes = new byte[size];
				in.get(bytes);
				obj = bytes;
			}
				break;

			case DwarfConstants.DW_FORM_block1: {
				int size = in.get() & 0xff;
				byte[] bytes = new byte[size];
				in.get(bytes);
				obj = bytes;
			}
				break;

			case DwarfConstants.DW_FORM_block2: {
				int size = in.getShort();
				byte[] bytes = new byte[size];
				in.get(bytes);
				obj = bytes;
			}
				break;

			case DwarfConstants.DW_FORM_block4: {
				int size = in.getInt();
				byte[] bytes = new byte[size];
				in.get(bytes);
				obj = bytes;
			}
				break;

			case DwarfConstants.DW_FORM_data1:
				obj = new Byte(in.get());
				break;

			case DwarfConstants.DW_FORM_data2:
				obj = new Short(in.getShort());
				break;

			case DwarfConstants.DW_FORM_data4:
				obj = new Integer(in.getInt());
				break;

			case DwarfConstants.DW_FORM_data8:
				obj = new Long(in.getLong());
				break;

			case DwarfConstants.DW_FORM_sdata:
				obj = new Long(DwarfInfoReader.read_signed_leb128(in));
				break;

			case DwarfConstants.DW_FORM_udata:
				obj = new Long(DwarfInfoReader.read_unsigned_leb128(in));
				break;

			case DwarfConstants.DW_FORM_string: {
				int c;
				StringBuffer sb = new StringBuffer();
				while ((c = (in.get() & 0xff)) != -1) {
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
				int offset = in.getInt();
				if (debugStrings == null) {
					obj = new String();
				} else if (offset < 0 || offset > debugStrings.capacity()) {
					obj = new String();
				} else {
					debugStrings.position(offset);
					obj = DwarfInfoReader.readString(debugStrings);
				}
			}
				break;

			case DwarfConstants.DW_FORM_ref1:
				obj = new Integer(in.get() & 0xff);
				break;

			case DwarfConstants.DW_FORM_ref2:
				obj = new Integer(in.getShort() & 0xffff);
				break;

			case DwarfConstants.DW_FORM_ref4:
				obj = new Integer(in.getInt());
				break;

			case DwarfConstants.DW_FORM_ref8:
				obj = new Long(in.getLong());
				break;

			case DwarfConstants.DW_FORM_ref_udata:
				obj = new Long(DwarfInfoReader.read_unsigned_leb128(in));
				break;

			case DwarfConstants.DW_FORM_indirect: {
				actualForm = (byte) DwarfInfoReader.read_unsigned_leb128(in);
				return readAttribute(in, addressSize, debugStrings);
			}

			default:
				assert (false);
				break;
			}

			return obj;
		}

		/**
		 * @param attr
		 * @param in
		 * @param addressSize
		 * @param debugStrings
		 */
		public static void skipAttributeValue(short form, IStreamBuffer in,
				byte addressSize) throws IOException {
			switch ((int) form) {
			case DwarfConstants.DW_FORM_addr:
			case DwarfConstants.DW_FORM_ref_addr:
				in.position(in.position() + addressSize);
				break;

			case DwarfConstants.DW_FORM_block: {
				int size = (int) DwarfInfoReader.read_unsigned_leb128(in);
				in.position(in.position() + size);
			}
				break;

			case DwarfConstants.DW_FORM_block1: {
				int size = in.get() & 0xff;
				in.position(in.position() + size);
			}
				break;

			case DwarfConstants.DW_FORM_block2: {
				int size = in.getShort();
				in.position(in.position() + size);
			}
				break;

			case DwarfConstants.DW_FORM_block4: {
				int size = in.getInt();
				in.position(in.position() + size);
			}
				break;

			case DwarfConstants.DW_FORM_data1:
				in.position(in.position() + 1);
				break;

			case DwarfConstants.DW_FORM_data2:
				in.position(in.position() + 2);
				break;

			case DwarfConstants.DW_FORM_data4:
				in.position(in.position() + 4);
				break;

			case DwarfConstants.DW_FORM_data8:
				in.position(in.position() + 8);
				break;

			case DwarfConstants.DW_FORM_sdata:
				DwarfInfoReader.read_signed_leb128(in);
				break;

			case DwarfConstants.DW_FORM_udata:
				DwarfInfoReader.read_unsigned_leb128(in);
				break;

			case DwarfConstants.DW_FORM_string: {
				int c;
				while ((c = (in.get() & 0xff)) != -1) {
					if (c == 0) {
						break;
					}
				}
			}
				break;

			case DwarfConstants.DW_FORM_flag:
				in.position(in.position() + 1);
				break;

			case DwarfConstants.DW_FORM_strp:
				in.position(in.position() + 4);
				break;

			case DwarfConstants.DW_FORM_ref1:
				in.position(in.position() + 1);
				break;

			case DwarfConstants.DW_FORM_ref2:
				in.position(in.position() + 2);
				break;

			case DwarfConstants.DW_FORM_ref4:
				in.position(in.position() + 4);
				break;

			case DwarfConstants.DW_FORM_ref8:
				in.position(in.position() + 8);
				break;

			case DwarfConstants.DW_FORM_ref_udata:
				DwarfInfoReader.read_unsigned_leb128(in);
				break;

			case DwarfConstants.DW_FORM_indirect: {
				form = (short) DwarfInfoReader.read_unsigned_leb128(in);
				skipAttributeValue(form, in, addressSize);
				break;
			}

			default:
				assert (false);
				break;
			}
		}
	}

	static class AttributeList {

		Map<Short, AttributeValue> attributeMap;

		AttributeList(AbbreviationEntry entry, IStreamBuffer in, byte addressSize, IStreamBuffer debugStrings) {

			int len = entry.attributes.size();
			attributeMap = new HashMap<Short, AttributeValue>(len);
			for (int i = 0; i < len; i++) {
				Attribute attr = entry.attributes.get(i);
				attributeMap.put(Short.valueOf(attr.tag), new AttributeValue(attr.form, in, addressSize, debugStrings));
			}

		}

		public static void skipAttributes(AbbreviationEntry entry, IStreamBuffer in, byte addressSize) {

			int len = entry.attributes.size();
			for (int i = 0; i < len; i++) {
				Attribute attr = entry.attributes.get(i);
				try {
					AttributeValue.skipAttributeValue(attr.form, in, addressSize);
				} catch (IOException e) {
					EDCDebugger.getMessageLogger().logError(null, e);
					break;
				}
			}

		}	

		public long getAttributeValueAsLong(short attributeName) {
			AttributeValue attr = attributeMap.get(Short.valueOf(attributeName));
			if (attr != null) {
				Number number = ((Number) attr.value);
				if (number != null)
					return number.longValue();
			}
			return 0;
		}

		public int getAttributeValueAsInt(short attributeName) {
			AttributeValue attr = attributeMap.get(Short.valueOf(attributeName));
			if (attr != null) {
				Number number = ((Number) attr.value);
				if (number != null)
					return number.intValue();
			}
			return 0;
		}

		public String getAttributeValueAsString(short attributeName) {
			AttributeValue attr = attributeMap.get(Short.valueOf(attributeName));
			if (attr != null) {
				String result = (String) attr.value;
				if (result != null)
					return result;
			}
			return "";
		}

		public byte[] getAttributeValueAsBytes(short attributeName) {
			AttributeValue attr = attributeMap.get(Short.valueOf(attributeName));
			if (attr != null) {
				byte[] result = (byte[]) attr.value;
				if (result != null)
					return result;
			}
			return new byte[0];
		}

		public AttributeValue getAttribute(short attributeName) {
			return attributeMap.get(Short.valueOf(attributeName));
		}

		/**
		 * Tell whether the attributes do not have a code range.
		 * <p> 
		 * Note: a singular DW_AT_low_pc means an entry point
		 * <p>
		 * Also note: a compile unit can have code represented by DW_AT_stmt_list
		 * @return true if the attributes represent code
		 */
		public boolean hasCodeRangeAttributes() {
			return attributeMap.containsKey(DwarfConstants.DW_AT_high_pc)
			||  attributeMap.containsKey(DwarfConstants.DW_AT_ranges);
		}
	}
	
	static public class PublicNameInfo {
		public final String nameWithNameSpace;
		public final CompilationUnitHeader cuHeader;
		public final short tag;	// DW_TAG_xxx
		
		public PublicNameInfo(String nameWithNameSpace, CompilationUnitHeader cuHeader, short tag) {
			this.nameWithNameSpace = nameWithNameSpace;
			this.cuHeader = cuHeader;
			this.tag = tag;
		}
	}

	// list of compilation units per source file 
	protected HashMap<IPath, List<ICompileUnitScope>> compileUnitsPerFile = new HashMap<IPath, List<ICompileUnitScope>>();
	
	// list of compile units in .debug_info order
	protected ArrayList<DwarfCompileUnit> compileUnits = new ArrayList<DwarfCompileUnit>();

	// function and type declarations can be referenced by offsets relative to
	// the compile unit or to the entire .debug_info section. therefore we keep
	// maps by .debug_info offset, and for compile unit relative offsets, we
	// just add the compile unit offset into the .debug_info section.
	protected Map<Long, AttributeList> functionsByOffset = new HashMap<Long, AttributeList>();
	protected Map<Long, IType> typesByOffset = new HashMap<Long, IType>();
	// map of entities which created scopes
	protected Map<Long, Scope> scopesByOffset = new HashMap<Long, Scope>();
	
	// entry points for CUs in the .debug_info section, used to dynamically parse CUs as needed 
	protected TreeMap<Long, CompilationUnitHeader> debugOffsetsToCompileUnits = new TreeMap<Long, CompilationUnitHeader>();
	
	// forward references for tags we have not parsed yet.  (These will go into typesByOffset once handled)
	//Map<Long, ForwardDwarfDefinition> forwardDwarfDefinitions = new HashMap<Long, ForwardDwarfDefinition>();
	
	// these are just for faster lookups
	protected Map<String, List<IFunctionScope>> functionsByName = new HashMap<String, List<IFunctionScope>>();
	protected Map<String, List<IVariable>> variablesByName = new HashMap<String, List<IVariable>>();
	protected Map<String, List<PublicNameInfo>> publicFunctions = new HashMap<String, List<PublicNameInfo>>();
	protected Map<String, List<PublicNameInfo>> publicVariables = new HashMap<String, List<PublicNameInfo>>();
	

	// abbreviation tables (lists of abbrev entries), mapped by .debug_abbrev offset
	protected Map<Integer, Map<Long, AbbreviationEntry>> abbreviationMaps = new HashMap<Integer, Map<Long, AbbreviationEntry>>();

	
	protected Set<String> referencedFiles = new HashSet<String>();
	protected boolean buildReferencedFilesList = true;
	
	private IPath symbolFilePath;
	private boolean parsedInitially = false;
	private boolean parsedForVarsAndAddresses = false;
	private boolean parsedForTypes = false;
	
	private final IExecutableSymbolicsReader exeReader;
	private final DwarfModuleScope moduleScope;

	final DwarfFileHelper fileHelper;
	
	public DwarfDebugInfoProvider(IExecutableSymbolicsReader exeReader) {
		this.exeReader = exeReader;
		this.symbolFilePath = exeReader.getSymbolFile();
		this.moduleScope = new DwarfModuleScope(this);
		this.fileHelper = new DwarfFileHelper(symbolFilePath);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DWARF debug info provider for "+ symbolFilePath; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IDebugInfoProvider#dispose()
	 */
	public void dispose() {
		// help GC
		compileUnitsPerFile.clear();
		compileUnits.clear();
		typesByOffset.clear();
		scopesByOffset.clear();
		functionsByName.clear();
		variablesByName.clear();
		abbreviationMaps.clear();
		referencedFiles.clear();
		referencedFiles.clear();
		parsedInitially = false;
		parsedForTypes = false;
		parsedForVarsAndAddresses = false;
	}

	void ensureParsedInitially() {
		if (!parsedInitially) {
			DwarfInfoReader reader = new DwarfInfoReader(this);
			parsedInitially = true;
			reader.parseInitial();
		}
	}

	void ensureParsedForAddresses() {
		if (!parsedForVarsAndAddresses) {
			DwarfInfoReader reader = new DwarfInfoReader(this);
			if (!parsedInitially) {
				parsedInitially = true;
				reader.parseInitial();
			}
			parsedForVarsAndAddresses = true;
			reader.parseForAddresses();
		}
	}

	void ensureParsedForVariables() {
		if (!parsedForVarsAndAddresses) {
			DwarfInfoReader reader = new DwarfInfoReader(this);
			if (!parsedInitially) {
				parsedInitially = true;
				reader.parseInitial();
			}
			parsedForVarsAndAddresses = true;
			reader.parseForAddresses();
		}
	}

	void ensureParsedForTypes() {
		if (!parsedForTypes) {
			DwarfInfoReader reader = new DwarfInfoReader(this);
			if (!parsedInitially) {
				parsedInitially = true;
				reader.parseInitial();
			}
			parsedForTypes = true;
			reader.parseForTypes();
		}
	}

	public void setParsedInitially() {
		parsedInitially = true;
	}

	public void setParsedForAddresses() {
		parsedForVarsAndAddresses = true;
	}

	public IPath getSymbolFile() {
		return symbolFilePath;
	}

	public IModuleScope getModuleScope() {
		return moduleScope;
	}
	
	public IAddress getBaseLinkAddress() {
		return exeReader.getBaseLinkAddress();
	}

	public Collection<IFunctionScope> getFunctionsByName(String name) {
		List<IFunctionScope> result;

		ensureParsedInitially();
		
		String baseName = name;
		
		/*  use same semantics as before, where qualified name lookups would fail		
		// pubnames uses qualified names but is indexed by basename
		if (name != null) {
			int baseStart = name.lastIndexOf("::");
			if (baseStart != -1)
				baseName = name.substring(baseStart + 2);
		}
		*/
		
		// first, match against public function names
		if (publicFunctions.size() > 0) {
			if (name != null) {
				DwarfInfoReader reader = new DwarfInfoReader(this);
				List<PublicNameInfo> nameMatches = publicFunctions.get(baseName);

				if (nameMatches != null) {
					// parse the computation units that have matches
					if (nameMatches.size() == 1) { // quick usual case
						reader.parseCompilationUnitForAddresses(nameMatches.get(0).cuHeader.scope);
					} else {
						ArrayList<DwarfCompileUnit> cuList = new ArrayList<DwarfCompileUnit>(); 
	
						for (PublicNameInfo info : nameMatches) {
							if (!cuList.contains(info.cuHeader.scope)) {
								cuList.add(info.cuHeader.scope);
							}
						}
	
						for (DwarfCompileUnit cu : cuList) {
							reader.parseCompilationUnitForAddresses(cu);
						}
					}
				} else {
					// not a public name, so parse all computation units looking for functions
					ensureParsedForAddresses();
				}
			} else {
				// name is null, so parse all computation units looking for functions
				ensureParsedForAddresses();
			}
		} else {
			// no public names, so parse all computation units looking for functions
			ensureParsedForAddresses();
		}
		
		if (name != null) {
			result = functionsByName.get(baseName);
			if (result == null)
				return new ArrayList<IFunctionScope>(0);
		} else {
			result = new ArrayList<IFunctionScope>(functionsByName.size()); // at least this big
			for (List<IFunctionScope> functions : functionsByName.values())
				result.addAll(functions);
			((ArrayList<IFunctionScope>) result).trimToSize();
		}
		return Collections.unmodifiableCollection(result);
	}

	public Collection<IVariable> getVariablesByName(String name) {
		List<IVariable> result;

		ensureParsedInitially();
		
		// first, match against public variable names
		if (publicVariables.size() > 0) {
			if (name != null) {
				DwarfInfoReader reader = new DwarfInfoReader(this);
				List<PublicNameInfo> nameMatches = publicVariables.get(name);

				if (nameMatches != null) {
					// parse the computation units that have matches
					if (nameMatches.size() == 1) { // quick usual case
						reader.parseCompilationUnitForAddresses(nameMatches.get(0).cuHeader.scope);
					} else {
						ArrayList<DwarfCompileUnit> cuList = new ArrayList<DwarfCompileUnit>(); 
	
						for (PublicNameInfo info : nameMatches) {
							if (!cuList.contains(info.cuHeader.scope)) {
								cuList.add(info.cuHeader.scope);
							}
						}
	
						for (DwarfCompileUnit cu : cuList) {
							reader.parseCompilationUnitForAddresses(cu);
						}
					}
				} else {
					// not a public name, so parse all computation units looking for variables
					ensureParsedForVariables();
				}
			} else {
				// name is null, so parse all computation units looking for variables
				ensureParsedForVariables();
			}
		} else {
			// no public names, so parse all computation units looking for variables
			ensureParsedForVariables();
		}

		if (name != null) {
			result = variablesByName.get(name);
			if (result == null)
				return new ArrayList<IVariable>(0);
		} else {
			result = new ArrayList<IVariable>(variablesByName.size()); // at least this big
			for (List<IVariable> functions : variablesByName.values())
				result.addAll(functions);
		}
		return Collections.unmodifiableCollection(result);
	}

	/**
	 * @return the publicFunctions
	 */
	public Map<String, List<PublicNameInfo>> getPublicFunctions() {
		ensureParsedInitially();
		return publicFunctions;
	}
	/**
	 * @return the publicVariables
	 */
	public Map<String, List<PublicNameInfo>> getPublicVariables() {
		ensureParsedInitially();
		return publicVariables;
	}

	public ICompileUnitScope getCompileUnitForAddress(IAddress linkAddress) {
		ensureParsedForAddresses();
		
		IScope scope = moduleScope.getScopeAtAddress(linkAddress);
		while (scope != null && !(scope instanceof ICompileUnitScope)) {
			scope = scope.getParent();
		}

		return (ICompileUnitScope) scope;
	}

	public List<ICompileUnitScope> getCompileUnitsForFile(IPath filePath) {
		ensureParsedInitially();

		List<ICompileUnitScope> cuList = compileUnitsPerFile.get(filePath);
		
		if (cuList != null)
			return cuList;
		
		// FIXME: we need a looser check here: we add drive letters to all paths in Windows
		// even if there is not really a drive (see DwarfHelper).
		for (Map.Entry<IPath, List<ICompileUnitScope>> entry : compileUnitsPerFile.entrySet()) {
			if (entry.getKey().setDevice(null).equals(filePath.setDevice(null))) {
				return entry.getValue();
			}
		}
		
		return null;
	}

	public String[] getSourceFiles() {
		if (referencedFiles.isEmpty()) {
			DwarfInfoReader reader = new DwarfInfoReader(this);
			reader.quickParseDebugInfo();
		}

		return referencedFiles.toArray(new String[referencedFiles.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IDebugInfoProvider#getTypes()
	 */
	public Collection<IType> getTypes() {
		ensureParsedForTypes();
		
		ArrayList<IType> types = new ArrayList<IType>(typesByOffset.values());
		return types;
	}
	
	/////////////////
	
	// Lazy evaluation methods
	
	
	/**
	 * Fetch a type lazily.  Either we've already parsed the type, or we have
	 * a reference to it, or we can find its compilation unit and parse its types.  
	 * We do not fix up cross references until someone asks for
	 * it (e.g. from an IType or IVariable implementation).
	 */
	public IType readType(long offset_) {
		Long offset = Long.valueOf(offset_); 
		IType type = typesByOffset.get(offset);
		if (type == null) {
			// make sure we've parsed it
			CompilationUnitHeader header = fetchCompileUnitHeader(offset_);
			if (header != null) {
				DwarfInfoReader reader = new DwarfInfoReader(this);
				reader.parseCompilationUnitForTypes(header.scope);
				type = typesByOffset.get(offset);
				// may be unhandled currently
				if (type == null) { 
					EDCDebugger.getMessageLogger().logError("Not parsing type at " + Long.toHexString(offset_) + " in " + symbolFilePath, null);
				}
			} else {
				// may be unhandled currently
				EDCDebugger.getMessageLogger().logError("Cannot resolve compilation unit header for type at " + Long.toHexString(offset_) + " in " + symbolFilePath, null);
			}
		}
		return type;
	}
	

	/**
	 * Scan compilation unit header for its subprograms and addresses.
	 */
	public CompilationUnitHeader scanCompilationHeader(long offset_) {
		// make sure we've parsed it
		CompilationUnitHeader header = fetchCompileUnitHeader(offset_);
		if (header != null) {
			DwarfInfoReader reader = new DwarfInfoReader(this);
			reader.parseCompilationUnitForAddresses(header.scope);
		}
		return header;
	}
	
	
	/**
	 * Fetch a referenced type lazily.
	 * @param scope 
	 */
	IType resolveTypeReference(ForwardTypeReference ref) {
		IType type = typesByOffset.get(ref.offset);
		if (type == null) {
			type = readType(ref.offset);
		}
		return type;
	}
	/**
	 * @return
	 */
	public IExecutableSymbolicsReader getExecutableSymbolicsReader() {
		return exeReader;
	}

	/**
	 * Remember where a compilation unit header lives in the debug info.
	 * @param debugInfoOffset
	 * @param currentCUHeader
	 */
	public void registerCompileUnitHeader(int debugInfoOffset,
			CompilationUnitHeader currentCUHeader) {
		debugOffsetsToCompileUnits.put((long) debugInfoOffset, currentCUHeader);
	}
	
	/**
	 * Get a compilation unit header that contains the given offset.
	 * @param debugInfoOffset an offset which is on or after a compilation unit's debug offset
	 * @return {@link CompilationUnitHeader} containing the offset
	 */
	public CompilationUnitHeader fetchCompileUnitHeader(long debugInfoOffset) {
		CompilationUnitHeader match = debugOffsetsToCompileUnits.get(debugInfoOffset);
		if (match != null)
			return match;
		
		// it's inside one
		SortedMap<Long,CompilationUnitHeader> headMap = debugOffsetsToCompileUnits.headMap(debugInfoOffset);
		// urgh, sorted map... no easy way to get to the end
		for (CompilationUnitHeader header : headMap.values()) {
			match = header;
		}
		return match;
	}

}
