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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.symbols.ISection;
import org.eclipse.cdt.debug.edc.internal.symbols.ISymbol;
import org.eclipse.core.runtime.IPath;

/**
 * Base implementation of a symbolics reader.  Subclasses populae sections and symbols
 * on construction. 
 */
public abstract class BaseExecutableSymbolicsReader implements IExecutableSymbolicsReader {

	protected final IPath binaryFile;
	
	protected Map<String, IExecutableSection> executableSections = new HashMap<String, IExecutableSection>();
	protected List<ISection> sections = new ArrayList<ISection>();
	protected List<ISymbol> symbols = new ArrayList<ISymbol>();
	protected IAddress exeBaseAddress;
	protected long modificationDate;
	protected IDebugInfoProvider debugReader;
	protected ISectionMapper sectionMapper;
	
	/**
	 * 
	 */
	public BaseExecutableSymbolicsReader(IPath binaryFile) {
		this.binaryFile = binaryFile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.exe.IExecutableSymbolicsReader#dispose()
	 */
	public void dispose() {
		if (sectionMapper != null) {
			sectionMapper.dispose();
			sectionMapper = null;
		}
		sections.clear();
		symbols.clear();
	}
	
	public IPath getSymbolFile() {
		return binaryFile;
	}

	public Collection<IExecutableSection> getExecutableSections() {
		return Collections.unmodifiableCollection(executableSections.values());
	}

	public IExecutableSection findExecutableSection(String sectionName) {
		return executableSections.get(sectionName);
	}

	public Collection<ISection> getSections() {
		return Collections.unmodifiableCollection(sections);
	}

	public Collection<ISymbol> getSymbols() {
		return Collections.unmodifiableCollection(symbols);
	}

	public ISymbol getSymbolAtAddress(IAddress linkAddress) {
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

	public IAddress getBaseLinkAddress() {
		return exeBaseAddress;
	}

	public long getModificationDate() {
		return modificationDate;
	}
}
