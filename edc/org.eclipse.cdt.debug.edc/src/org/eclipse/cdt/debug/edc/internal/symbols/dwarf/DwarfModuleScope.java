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

package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.symbols.ISection;
import org.eclipse.cdt.debug.edc.internal.symbols.ModuleLineEntryProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.Scope;
import org.eclipse.cdt.debug.edc.services.IFrameRegisterProvider;
import org.eclipse.cdt.debug.edc.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.IModuleLineEntryProvider;
import org.eclipse.cdt.debug.edc.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariable;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.core.runtime.IPath;

/**
 * This represents the high-level view of an executable module's symbolics.
 */
public class DwarfModuleScope extends Scope implements IModuleScope {

	private final DwarfDebugInfoProvider provider;
	private ModuleLineEntryProvider lineEntryMapper;

	public DwarfModuleScope(DwarfDebugInfoProvider provider) {
		super("", null, null, null);
		this.provider = provider;
		
		if (provider == null) {
			lowAddress = Addr32.ZERO;
			highAddress = Addr32.ZERO;
		} else {
			name = provider.getSymbolFile().lastSegment();
					
			// for now, use the code sections' ranges
			lowAddress = Addr32.MAX;
			highAddress = Addr32.ZERO;
			for (ISection section : provider.getExecutableSymbolicsReader().getSections()) {
				if (section.getProperties().get(ISection.PROPERTY_NAME).equals(ISection.NAME_DATA)) {
					if (section.getLinkAddress().compareTo(lowAddress) < 0)
						lowAddress = section.getLinkAddress();
					IAddress end = section.getLinkAddress().add(section.getSize());
					if (end.compareTo(highAddress) > 0)
						highAddress = end;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IModuleScope#getSymbolFile()
	 */
	public IPath getSymbolFile() {
		return provider.getSymbolFile();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IModuleScope#getCompileUnitForAddress(org.eclipse.cdt.core.IAddress)
	 */
	public ICompileUnitScope getCompileUnitForAddress(IAddress linkAddress) {
		if (provider != null)
			return provider.getCompileUnitForAddress(linkAddress);
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IModuleScope#getCompileUnitForFile(org.eclipse.core.runtime.IPath)
	 */
	public List<ICompileUnitScope> getCompileUnitsForFile(IPath filePath) {
		if (provider != null)
			return provider.getCompileUnitsForFile(filePath);
		else
			return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IModuleScope#getFunctionsByName(java.lang.String)
	 */
	public Collection<IFunctionScope> getFunctionsByName(String name) {
		if (provider != null)
			return provider.getFunctionsByName(name);
		else
			return Collections.emptyList();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IModuleScope#getVariablesByName(java.lang.String)
	 */
	public Collection<IVariable> getVariablesByName(String name) {
		if (provider != null) 
			return provider.getVariablesByName(name);
		else
			return Collections.emptyList();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Scope#getScopeAtAddress(org.eclipse.cdt.core.IAddress)
	 */
	@Override
	public IScope getScopeAtAddress(IAddress linkAddress) {
		ensureParsed();
		return super.getScopeAtAddress(linkAddress);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Scope#getEnumerators()
	 */
	@Override
	public Collection<IEnumerator> getEnumerators() {
		ensureParsed();
		return super.getEnumerators();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Scope#getChildren()
	 */
	@Override
	public Collection<IScope> getChildren() {
		ensureParsed();
		return super.getChildren();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Scope#getVariables()
	 */
	@Override
	public Collection<IVariable> getVariables() {
		ensureParsedForVariables();
		return super.getVariables();
	}
	
	private void ensureParsed() {
		if (provider != null) 
			provider.ensureParsedInitially();		
	}
	
	private void ensureParsedForVariables() {
		if (provider != null) 
			provider.ensureParsedForVariables();		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IModuleScope#getTypes()
	 */
	public Collection<IType> getTypes() {
		if (provider != null)
			return provider.getTypes();
		else
			return Collections.emptyList();
	}

	/**
	 * Fixup ranges after a full-module address parse.
	 */
	/*
	public void fixupRanges() {
		System.out.println("Fixing up ranges for " + getChildren().size() + " children");
		
		// fix up scope addresses in case compiler doesn't generate them.
		for (IScope cu : getChildren()) {
			((DwarfCompileUnit) cu).fixupRanges();
		}

		IAddress newLowAddress = new Addr64(BigInteger.valueOf(0xFFFFFFFFL));
		IAddress newHighAddress = new Addr64(BigInteger.valueOf(0));

		// now fix up the module scope
		for (IScope cu : getChildren()) {
			if (cu.getLowAddress().compareTo(newLowAddress) < 0) {
				newLowAddress = cu.getLowAddress();
			}

			if (cu.getHighAddress().compareTo(newHighAddress) > 0) {
				newHighAddress = cu.getHighAddress();
			}
		}

		this.lowAddress = newLowAddress;
		this.highAddress = newHighAddress;
	}
	*/
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IModuleScope#getModuleLineEntryProvider()
	 */
	public IModuleLineEntryProvider getModuleLineEntryProvider() {
		if (lineEntryMapper == null) {
			lineEntryMapper = new ModuleLineEntryProvider();

			// handle the currently parsed children 
			for (IScope scope : getChildren()) {
				if (scope instanceof ICompileUnitScope) {
					lineEntryMapper.addCompileUnit((ICompileUnitScope) scope);
				}
			}
		}
		return lineEntryMapper;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Scope#addChild(org.eclipse.cdt.debug.edc.internal.symbols.IScope)
	 */
	@Override
	public void addChild(IScope scope) {
		super.addChild(scope);
		
		// initial module scope range is a guess
		mergeScopeRange(scope);
		
		if (scope instanceof ICompileUnitScope && lineEntryMapper != null) {
			lineEntryMapper.addCompileUnit((ICompileUnitScope) scope);
		}
	}

	/** 
	 * Update info when a compile unit has been fully parsed.
	 * @param scope
	 */
	public void updateLineInfoForCU(ICompileUnitScope scope) {
		// be sure the decl entries for inlined functions are detected
		if (lineEntryMapper != null)
			lineEntryMapper.addCompileUnit(scope);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.symbols.IModuleScope#getFrameRegisterProvider()
	 */
	public IFrameRegisterProvider getFrameRegisterProvider() {
		if (provider != null)
			return provider.getFrameRegisterProvider();
		else
			return null;
	}
	
	/**
	 * Help garbage collection
	 */
	public void dispose() {
		lineEntryMapper = null;
		super.dispose();
	}
}

