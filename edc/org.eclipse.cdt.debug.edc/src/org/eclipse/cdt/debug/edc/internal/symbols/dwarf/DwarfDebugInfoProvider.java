/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.symbols.IForwardTypeReference;
import org.eclipse.cdt.debug.edc.internal.symbols.Scope;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfFrameRegisterProvider.CommonInformationEntry;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfFrameRegisterProvider.FrameDescriptionEntry;
import org.eclipse.cdt.debug.edc.services.IFrameRegisterProvider;
import org.eclipse.cdt.debug.edc.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.symbols.IDebugInfoProvider;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSymbolicsReader;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.symbols.IRangeList;
import org.eclipse.cdt.debug.edc.symbols.IRangeList.Entry;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class handles the low-level aspects of reading DWARF data.
 * There exists one provider per symbol file.
 */
public class DwarfDebugInfoProvider implements IDebugInfoProvider {
	
	/**
	 * This represents a forward type reference, which is a type
	 * that resolves itself when referenced.
	 */
	static public class ForwardTypeReference implements IType, IForwardTypeReference {

		static public final IType NULL_TYPE_ENTRY = new IType() {

			public int getByteSize() {
				return 0;
			}

			public String getName() {
				return DwarfMessages.DwarfDebugInfoProvider_UnhandledType;
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
			
			public void dispose() {
			}
		};
		
		private DwarfDebugInfoProvider provider;
		private IType type = null;

		private final long offset;
		
		public ForwardTypeReference(DwarfDebugInfoProvider provider, long offset) {
			this.provider = provider;
			this.offset = offset;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.symbols.dwarf.IForwardTypeReference#getReferencedType()
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
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.symbols.IType#dispose()
		 */
		public void dispose() {
			type = null;
			provider = null;
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
		private Object value;

		// for indirect form, this is the actual form
		private byte actualForm;

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
			switch (form) {
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
		
		/**
		 * Parse attributes and then skip to sibling, if any
		 * 
		 * @param names array to hold up to two names
		 * @param entry debug info entry
		 * @param in buffer stream of debug info
		 * @param addressSize 
		 * @param debugStrings
		 * @return DW_AT_name value, or null if there is no or invalid DW_AT_name attribute  
		 */
		public static void skipAttributesToSibling(AbbreviationEntry entry, IStreamBuffer in, byte addressSize) {
		
			long sibling = -1;

			// go through the attributes and throw away everything except the sibling
			int len = entry.attributes.size();
			for (int i = 0; i < len; i++) {
				Attribute attr = entry.attributes.get(i);
				try {
					if (attr.tag == DwarfConstants.DW_AT_sibling) {
						if (attr.form == DwarfConstants.DW_FORM_ref_udata) {
							sibling = DwarfInfoReader.read_unsigned_leb128(in);
						} else if (attr.form == DwarfConstants.DW_FORM_ref4) {
							sibling = in.getInt();
						} else {
							// TODO: allow other forms for sibling value
							AttributeValue.skipAttributeValue(attr.form, in, addressSize);
						}
					} else {
						AttributeValue.skipAttributeValue(attr.form, in, addressSize);
					}
				} catch (IOException e) {
					EDCDebugger.getMessageLogger().logError(null, e);
					break;
				}
			}

			if (sibling != -1)
				in.position(sibling);
		}


		public byte getActualForm() {
			return actualForm;
		}

		/**
		 * Get the value as a 64-bit signed long, sign-extending any shorter attribute
		 * @return value as signed long
		 */
		public long getValueAsSignedLong() {
			if (value instanceof Number) {
				return ((Number) value).longValue();
			}
			return 0;
		}
		
		/**
		 * Get the value as a 64-bit long.
		 * 
		 * A Byte, Short, or Integer is zero-extended.
		 * 
		 * @return value as long
		 */
		public long getValueAsLong() {
			if (value instanceof Byte) {
				return ((Byte) value).byteValue() & 0xff;
			}
			if (value instanceof Short) {
				return ((Short) value).shortValue() & 0xffff;
			}
			if (value instanceof Integer) {
				return ((Integer) value).intValue() & 0xffffffff;
			}
			// fallthrough
			if (value instanceof Number) {
				return ((Number) value).longValue();
			}
			return 0;
		}
		
		/**
		 * Get the value as a 32-bit int.
		 * 
		 * A Byte or Short is zero-extended.
		 * 
		 * @return value as int
		 */
		public int getValueAsInt() {
			if (value instanceof Byte) {
				return ((Byte) value).byteValue() & 0xff;
			}
			if (value instanceof Short) {
				return ((Short) value).shortValue() & 0xffff;
			}
			// fallthrough
			if (value instanceof Number) {
				return ((Number) value).intValue();
			}
			return 0;
		}
		
		/**
		 * Get the value as a string
		 * @return String or "" if not a string
		 */
		public String getValueAsString() {
			if (value != null)
				return value.toString();
			return null;
		}

		/**
		 * Get the byte array value (which is empty if this is not a byte array)
		 * @return array
		 */
		public byte[] getValueAsBytes() {
			if (value instanceof byte[])
				return (byte[]) value;
			return new byte[0];
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
				return attr.getValueAsLong();
			}
			return 0;
		}

		public int getAttributeValueAsInt(short attributeName) {
			AttributeValue attr = attributeMap.get(Short.valueOf(attributeName));
			if (attr != null) {
				return attr.getValueAsInt();
			}
			return 0;
		}


		public long getAttributeValueAsSignedLong(short attributeName) {
			AttributeValue attr = attributeMap.get(Short.valueOf(attributeName));
			if (attr != null) {
				return attr.getValueAsSignedLong();
			}
			return 0;
		}
		
		public String getAttributeValueAsString(short attributeName) {
			AttributeValue attr = attributeMap.get(Short.valueOf(attributeName));
			if (attr != null) {
				return attr.getValueAsString();
			}
			return ""; //$NON-NLS-1$
		}

		public byte[] getAttributeValueAsBytes(short attributeName) {
			AttributeValue attr = attributeMap.get(Short.valueOf(attributeName));
			if (attr != null) {
				return attr.getValueAsBytes();
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

	// for casting to a type, keep certain types by name
	protected Map<String, List<IType>> typesByName = new HashMap<String, List<IType>>();
	// for casting to a type, track whether the cast name includes an aggregate designator
	enum TypeAggregate { Class, Struct, Union, None };

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

	// mapping of PC range to frame description entries
	protected TreeMap<IRangeList.Entry, FrameDescriptionEntry> frameDescEntries = new TreeMap<IRangeList.Entry, FrameDescriptionEntry>();
	// mapping of CIE offsets to parsed common info entries
	protected Map<Long, CommonInformationEntry> commonInfoEntries = new HashMap<Long, CommonInformationEntry>();
	
	
	protected Set<String> referencedFiles = new HashSet<String>();
	protected boolean buildReferencedFilesList = true;
	
	private IPath symbolFilePath;
	private long symbolFileLastModified;
	private boolean parsedInitially = false;
	private boolean parsedForVarsAndAddresses = false;
	private boolean parsedForScopesAndAddresses = false;
	private boolean parsedForTypes = false;
	private boolean parsedForGlobalVars = false;
	
	private final IExecutableSymbolicsReader exeReader;
	private final DwarfModuleScope moduleScope;

	final DwarfFileHelper fileHelper;

	private IFrameRegisterProvider frameRegisterProvider;
	
	private static String SOURCE_FILES_CACHE = "_source_files"; //$NON-NLS-1$

	public DwarfDebugInfoProvider(IExecutableSymbolicsReader exeReader) {
		this.exeReader = exeReader;
		this.symbolFilePath = exeReader.getSymbolFile();
		this.symbolFileLastModified = symbolFilePath.toFile().lastModified();
		this.moduleScope = new DwarfModuleScope(this);
		this.fileHelper = new DwarfFileHelper(symbolFilePath);
		this.frameRegisterProvider = new DwarfFrameRegisterProvider(this);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return DwarfMessages.DwarfDebugInfoProvider_DwarfProviderFor + symbolFilePath;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IDebugInfoProvider#dispose()
	 */
	public void dispose() {
		// several views in DSF hold onto all our debug info, 
		// so go through and explicitly break links
		if (moduleScope != null) {
			moduleScope.dispose();
		}
		
		// help GC
		compileUnitsPerFile.clear();
		compileUnits.clear();
		functionsByOffset.clear();
		typesByOffset.clear();
		scopesByOffset.clear();
		debugOffsetsToCompileUnits.clear();
		functionsByName.clear();
		variablesByName.clear();
		publicFunctions.clear();
		publicVariables.clear();
		abbreviationMaps.clear();
		referencedFiles.clear();
		moduleScope.dispose();
		parsedInitially = false;
		parsedForTypes = false;
		parsedForVarsAndAddresses = false;
		parsedForScopesAndAddresses = false;
		
		fileHelper.dispose();
		frameRegisterProvider.dispose();
	}

	synchronized void ensureParsedInitially() {
		if (!parsedInitially) {
			DwarfInfoReader reader = new DwarfInfoReader(this);
			parsedInitially = true;
			reader.parseInitial();
		}
	}

	synchronized void ensureParsedForScopes() {
		if (!parsedForScopesAndAddresses) {
			DwarfInfoReader reader = new DwarfInfoReader(this);
			if (!parsedInitially) {
				parsedInitially = true;
				reader.parseInitial();
			}
			parsedForScopesAndAddresses = true;
			reader.parseForAddresses(false);
		}
	}

	synchronized void ensureParsedForVariables() {
		if (!parsedForVarsAndAddresses) {
			DwarfInfoReader reader = new DwarfInfoReader(this);
			if (!parsedInitially) {
				parsedInitially = true;
				reader.parseInitial();
			}
			parsedForVarsAndAddresses = true;
			reader.parseForAddresses(true);
		}
	}
	
	synchronized private void ensureParsedForGlobalVariables() {
		if (parsedForGlobalVars)
			return;
		parsedForGlobalVars = true;

		if (publicVariables.size() == 0)
			return;

		// determine compilation units containing globals
		HashSet<CompilationUnitHeader> cuWithGlobalsArray = new HashSet<CompilationUnitHeader>(publicVariables.size());
		for (List<PublicNameInfo> infoList : publicVariables.values()) {
			for (PublicNameInfo info : infoList) {
				cuWithGlobalsArray.add(info.cuHeader);
			}
		}

		// parse compilation units containing global variables
		DwarfInfoReader reader = new DwarfInfoReader(this);
		for (CompilationUnitHeader cuHeader : cuWithGlobalsArray)
			reader.parseCompilationUnitForAddresses(cuHeader.scope);
	}

	synchronized void ensureParsedForTypes() {
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
					// parse the compilation units that have matches
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
					// not a public name, so parse all compilation units looking for functions
					ensureParsedForScopes();
				}
			} else {
				// name is null, so parse all compilation units looking for functions
				ensureParsedForScopes();
			}
		} else {
			// no public names, so parse all compilation units looking for functions
			ensureParsedForScopes();
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

	public Collection<IVariable> getVariablesByName(String name, boolean globalsOnly) {
		List<IVariable> result;

		ensureParsedInitially();

		if (name == null) {
			if (publicVariables.size() > 0) {
				// name is null, so parse all compilation units looking for variables
				if (globalsOnly)
					ensureParsedForGlobalVariables();
				else
					ensureParsedForVariables();
			}
			
			result = new ArrayList<IVariable>(variablesByName.size()); // at least this big
			for (List<IVariable> variables : variablesByName.values())
				result.addAll(variables);

			return Collections.unmodifiableCollection(result);
		}

		String baseName = name;
		int baseNameStart = name.lastIndexOf("::"); //$NON-NLS-1$
		if (baseNameStart != -1)
			baseName = name.substring(baseNameStart + 2);

		// match against public variable names, which the initial parse populated
		if (publicVariables.size() > 0) {
			DwarfInfoReader reader = new DwarfInfoReader(this);
			List<PublicNameInfo> nameMatches = publicVariables.get(baseName);

			if (nameMatches != null) {
				// parse the compilation units that have matches
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
				// not a public name, so parse all compilation units looking for variables
				if (!globalsOnly)
					ensureParsedForVariables();
			}
		}

		result = variablesByName.get(name);

		// check against unqualified name because RVCT 2.x did not include namespace
		// info for globals that are inside namespaces
		if (result == null && baseNameStart != -1)
			result = variablesByName.get(baseName);
			
		if (result == null)
			return new ArrayList<IVariable>(0);

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
		ensureParsedForScopes();
		
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
		
		// FIXME: we need a looser check here: on Windows, we added drive letters to all
		// paths before populating compileUnitsPerFile, even if there is not really a
		// drive (see DwarFileHelper).
		for (Map.Entry<IPath, List<ICompileUnitScope>> entry : compileUnitsPerFile.entrySet()) {
			if (entry.getKey().setDevice(null).equals(filePath.setDevice(null))) {
				return entry.getValue();
			}
		}
		
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	synchronized public String[] getSourceFiles(IProgressMonitor monitor) {
		if (referencedFiles.isEmpty()) {
			// Check the persistent cache
			String cacheKey = getSymbolFile().toOSString() + SOURCE_FILES_CACHE;
			Set<String> cachedFiles = EDCDebugger.getDefault().getCache().getCachedData(cacheKey, Set.class, symbolFileLastModified);
			if (cachedFiles == null)
			{
				DwarfInfoReader reader = new DwarfInfoReader(this);
				reader.quickParseDebugInfo(monitor);
				assert referencedFiles.size() > 0;
				EDCDebugger.getDefault().getCache().putCachedData(cacheKey, new HashSet<String>(referencedFiles), symbolFileLastModified);
			}
			else
				referencedFiles = cachedFiles;
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
					// workaround for GCC-E 3.x bug where some, but not all, type offsets are off by 4
					// assume if you hit this null case that the problem may be the GCC-E bug
					type = typesByOffset.get(offset - 4);
					if (type == null)
						EDCDebugger.getMessageLogger().logError(DwarfMessages.DwarfDebugInfoProvider_NotParsingType1 + Long.toHexString(offset_) +
									DwarfMessages.DwarfDebugInfoProvider_NotParsingType2 + symbolFilePath, null);
				}
			} else {
				// may be unhandled currently
				EDCDebugger.getMessageLogger().logError(DwarfMessages.DwarfDebugInfoProvider_CannotResolveCompUnit1 + Long.toHexString(offset_) +
								DwarfMessages.DwarfDebugInfoProvider_CannotResolveCompUnit2 + symbolFilePath, null);
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

	/**
	 * Get the frame description entry for the given PC
	 * @param framePC
	 * @return FDE or <code>null</code>
	 */
	public FrameDescriptionEntry findFrameDescriptionEntry(IAddress framePC) {
		DwarfInfoReader reader = new DwarfInfoReader(this);
		if (frameDescEntries.isEmpty()) {
			reader.parseForFrameIndices();
		}
		
		long pc = framePC.getValue().longValue();
		SortedMap<Entry, FrameDescriptionEntry> tailMap = frameDescEntries.tailMap(new IRangeList.Entry(pc, pc));
		if (tailMap.isEmpty())
			return null;
		
		FrameDescriptionEntry entry = tailMap.values().iterator().next();
		if (entry.getCIE() == null) {
			CommonInformationEntry cie = null;
			if (!commonInfoEntries.containsKey(entry.ciePtr)) {
				try {
					cie = reader.parseCommonInfoEntry(entry.ciePtr, entry.addressSize, framePC);
				} catch (IOException e) {
					EDCDebugger.getMessageLogger().logError(DwarfMessages.DwarfDebugInfoProvider_FailedToReadCIE + entry.ciePtr, e);
				}
				commonInfoEntries.put(entry.ciePtr, cie);
			} else {
				cie = commonInfoEntries.get(entry.ciePtr);
			}
			entry.setCIE(cie);
		}
		
		return entry;
	}

	/**
	 * @return
	 */
	public IFrameRegisterProvider getFrameRegisterProvider() {
		return frameRegisterProvider;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.symbols.IDebugInfoProvider#getTypesByName(java.lang.String)
	 */
	public Collection<IType> getTypesByName(String name) {
		// is name has "struct", "class" or "union", search without that
		name = name.trim();
		
		String baseName = name;
		TypeAggregate aggregate = TypeAggregate.None;
		
		if (baseName.startsWith("class ")) { //$NON-NLS-1$
			aggregate = TypeAggregate.Class;
			baseName = baseName.replace("class ", ""); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (baseName.startsWith("struct ")) { //$NON-NLS-1$
			aggregate = TypeAggregate.Struct;
			baseName = baseName.replace("struct ", ""); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (baseName.startsWith("union ")) { //$NON-NLS-1$
			aggregate = TypeAggregate.Union;
			baseName = baseName.replace("union ", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		Collection<IType> types = typesByName.get(baseName);
		
		String templateName  = null;
		String templateName2 = null;

		if (types == null) {
			// if we didn't match and this is a template name,
			// remove extra spaces and composite type names 
			if (baseName.indexOf('<') != -1) {
				templateName = baseName;

				while (templateName.contains("  ")) //$NON-NLS-1$
					templateName = templateName.replaceAll("  ", " "); //$NON-NLS-1$ //$NON-NLS-2$
				templateName = templateName.replaceAll(", ", ","); //$NON-NLS-1$ //$NON-NLS-2$
				templateName = templateName.replaceAll("class ", ""); //$NON-NLS-1$ //$NON-NLS-2$
				templateName = templateName.replaceAll("struct ", ""); //$NON-NLS-1$ //$NON-NLS-2$
				templateName = templateName.replaceAll("union ", ""); //$NON-NLS-1$ //$NON-NLS-2$

				types = typesByName.get(templateName);

				// template name without "<...>", rather than with "<...>", might match 
				if (types == null) {
					templateName2 = templateName.substring(0, templateName.indexOf('<'));

					types = typesByName.get(templateName2);

					// screen out types whose template list does not match
					if (types != null) {
						ArrayList<IType> matchingTypes = null;
						for (Iterator<IType> it = types.iterator(); it.hasNext(); ) {
							IType nextType = it.next();
							String match = nextType.getName();
							// for templates, remove composite type names (e.g., "class")
							match = match.replaceAll("class ", ""); //$NON-NLS-1$ //$NON-NLS-2$
							match = match.replaceAll("struct ", ""); //$NON-NLS-1$ //$NON-NLS-2$
							match = match.replaceAll("union ", ""); //$NON-NLS-1$ //$NON-NLS-2$

							if (match.equals(templateName)) {
								if (matchingTypes == null)
									matchingTypes = new ArrayList<IType>(types.size());
								matchingTypes.add(nextType);
							}
						}
						types = matchingTypes; // may be null
					}
				}
			}

			if (types == null) {
				// Maybe we optimistically searched for relevant types;
				// if that fails, do the full parse of types now
				if (!parsedForTypes) {
					ensureParsedForTypes();
					types = getTypesByName(baseName);
					if (types != null)
						return types; // non-template return

					if (baseName.indexOf('<') != -1) {
						types = typesByName.get(templateName);
						if (types == null)
							types = typesByName.get(templateName2);
						else
							templateName2 = null; // did not match name without "<...>"
					}
				}
				
				if (types == null)
					return new ArrayList<IType>(0);
			}
		}

		// screen out types whose template list does not match
		if (templateName2 != null) {
			ArrayList<IType> matchingTypes = new ArrayList<IType>(types.size());
			for (Iterator<IType> it = types.iterator(); it.hasNext(); ) { // types can't be null
				IType nextType = it.next();
				String match = nextType.getName();
				// for templates, remove composite type names (e.g., "class")
				match = match.replaceAll("class ", ""); //$NON-NLS-1$ //$NON-NLS-2$
				match = match.replaceAll("struct ", ""); //$NON-NLS-1$ //$NON-NLS-2$
				match = match.replaceAll("union ", ""); //$NON-NLS-1$ //$NON-NLS-2$

				if (match.equals(templateName))
					matchingTypes.add(nextType);
			}
			types = matchingTypes;
		}
		
		// make sure that the aggregate type matches as well as the name
		if (aggregate == TypeAggregate.None)
			return Collections.unmodifiableCollection(types);
		
		Iterator<IType> itr = types.iterator();
		while (itr.hasNext()) {
			IType nextType = itr.next();
			if ((aggregate == TypeAggregate.Class  && !nextType.getName().contains("class ")) || //$NON-NLS-1$
				(aggregate == TypeAggregate.Struct && !nextType.getName().contains("struct ")) || //$NON-NLS-1$
			    (aggregate == TypeAggregate.Union  && !nextType.getName().contains("union "))) //$NON-NLS-1$
				types.remove(nextType);
		}

		if (types.isEmpty())
			return new ArrayList<IType>(0);
		
		return Collections.unmodifiableCollection(types);
	}

}
