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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.symbols.CompileUnitScope;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfDebugInfoProvider.AttributeList;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfDebugInfoProvider.CompilationUnitHeader;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IVariable;
import org.eclipse.core.runtime.IPath;

public class DwarfCompileUnit extends CompileUnitScope {

	protected DwarfDebugInfoProvider provider;
	protected AttributeList attributes;
	private List<IPath> fileList;
	private boolean rangesDirty;

	// computation unit header
	protected final CompilationUnitHeader header;
	
	// whether the computation unit has been parsed to find variables and children with address ranges
	protected boolean parsedForVarsAndAddresses = false;
	
	// whether the computation unit has been parsed to find types
	protected boolean parsedForTypes = false;

	
	public DwarfCompileUnit(DwarfDebugInfoProvider provider, IModuleScope parent, IPath filePath,
			IAddress lowAddress, IAddress highAddress, CompilationUnitHeader header, boolean hasChildren,
			AttributeList attributes) {
		super(filePath, parent, lowAddress, highAddress);

		this.provider = provider;
		this.attributes = attributes;
		this.header = header;
		
		// if there are no children, say the children have been parsed
		if (!hasChildren) {
			this.parsedForVarsAndAddresses = true;
			this.parsedForTypes = true;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + header.debugInfoOffset;
		result = prime * result + provider.getSymbolFile().hashCode();
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DwarfCompileUnit other = (DwarfCompileUnit) obj;
		if (header.debugInfoOffset != other.header.debugInfoOffset)
			return false;
		if (!provider.getSymbolFile().equals(other.provider.getSymbolFile()))
			return false;
		return true;
	}

	public AttributeList getAttributeList() {
		return attributes;
	}
	
	@Override
	protected Collection<ILineEntry> parseLineTable() {
		DwarfInfoReader reader = new DwarfInfoReader(provider);
		fileList = new ArrayList<IPath>();
		return reader.parseLineTable(this, attributes, fileList);
	}
	
	public void setLowAddress(IAddress address) {
		this.lowAddress = address;
	}

	public void setHighAddress(IAddress address) {
		this.highAddress = address;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Scope#getLowAddress()
	 */
	@Override
	public IAddress getLowAddress() {
		// the address is known in the compile unit tag;
		// if anything inside is outside that range, it's a bug.
		return super.getLowAddress();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Scope#getHighAddress()
	 */
	@Override
	public IAddress getHighAddress() {
		// the address is known in the compile unit tag;
		// if anything inside is outside that range, it's a bug.
		return super.getHighAddress();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.CompileUnitScope#getFunctionAtAddress(org.eclipse.cdt.core.IAddress)
	 */
	@Override
	public IFunctionScope getFunctionAtAddress(IAddress linkAddress) {
		if (rangesDirty) {
			fixupRanges();
		}
		ensureParsedForAddresses();
		return super.getFunctionAtAddress(linkAddress);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.CompileUnitScope#getFunctions()
	 */
	@Override
	public Collection<IFunctionScope> getFunctions() {
		ensureParsedForAddresses();
		return super.getFunctions();
	}
	
	/**
	 * For compilers that don't generate compile unit scopes, e.g. GCCE with
	 * dlls, this fixes up the low and high addresses of the compile unit based
	 * on the function scopes
	 */
	protected void fixupRanges() {
		
		// fix up scope addresses in case compiler doesn't generate them.
		if (hasEmptyRange() && parsedForVarsAndAddresses) {
			fixupRanges(provider.getBaseLinkAddress());
		}

		rangesDirty = false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Scope#getChildren()
	 */
	@Override
	public Collection<IScope> getChildren() {
		return super.getChildren();
	}
	
	public void setAttributes(AttributeList attributes) {
		this.attributes = attributes;
	}

	public boolean isParsedForAddresses() {
		return parsedForVarsAndAddresses;
	}

	public boolean isParsedForVariables() {
		return parsedForVarsAndAddresses;
	}

	public boolean isParsedForTypes() {
		return parsedForTypes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Scope#getVariables()
	 */
	@Override
	public Collection<IVariable> getVariables() {
		ensureParsedForVariables();
		return super.getVariables();
	}

	public void setParsedForAddresses(boolean parsedForAddresses) {
		this.parsedForVarsAndAddresses = parsedForAddresses;
	}

	public void setParsedForVariables(boolean parsedForVariables) {
		this.parsedForVarsAndAddresses = parsedForVariables;
	}

	public void setParsedForTypes(boolean parsedForTypes) {
		this.parsedForTypes = parsedForTypes;
	}

	private void ensureParsedForAddresses() {
		if (!parsedForVarsAndAddresses) {
			DwarfInfoReader reader = new DwarfInfoReader(provider);
			reader.parseCompilationUnitForAddresses(this);
		}
	}
	
	/**
	 * Get the file path for a file number
	 * @param declFileNum
	 * @return IPath for the file, or <code>null</code>
	 */
	public IPath getFileEntry(int declFileNum) {
		if (fileList == null)
			parseLineTable();
		if (declFileNum <= 0 || declFileNum > fileList.size())
			return null;
		return fileList.get(declFileNum - 1);
	}
	
	private void ensureParsedForVariables() {
		if (!parsedForVarsAndAddresses) {
			DwarfInfoReader reader = new DwarfInfoReader(provider);
			reader.parseCompilationUnitForAddresses(this);
		}
	}
	
	@Override
	public IScope getScopeAtAddress(IAddress linkAddress) {
		ensureParsedForAddresses();
		return super.getScopeAtAddress(linkAddress);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DwarfCompileUnit [");
		
		builder.append("SymFile=");
		builder.append(provider.getSymbolFile().lastSegment());
		
		builder.append(", SectionOffset=0x");
		builder.append(Integer.toHexString(header.debugInfoOffset));
		
		builder.append(", lowAddr=");
		builder.append(lowAddress != null ? lowAddress.toHexAddressString() : null);
		
		builder.append(", highAddr=");
		builder.append(highAddress != null ? highAddress.toHexAddressString() : null);
		if (filePath != null) {
			builder.append(", path=");
			builder.append(filePath.toOSString());
		}
		builder.append(", parsedForVarsAndAddresses=");
		builder.append(parsedForVarsAndAddresses);
		builder.append(", parsedForTypes=");
		builder.append(parsedForTypes);
		builder.append("]\n");
		return builder.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Scope#addChild(org.eclipse.cdt.debug.edc.internal.symbols.IScope)
	 */
	@Override
	public void addChild(IScope scope) {
		super.addChild(scope);
		
		// if we don't know our scope yet...
		if (hasEmptyRange()) {
			rangesDirty = true;
		} else {
			// the CU may have an incomplete idea of its scope; fit the new scope in
			mergeScopeRange(scope);
		}

		addLineInfoToParent(scope);
	}

}
