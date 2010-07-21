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
import org.eclipse.cdt.debug.edc.symbols.IExecutableSection;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSymbolicsReader;
import org.eclipse.cdt.debug.edc.symbols.ISymbol;
import org.eclipse.cdt.debug.edc.symbols.IUnmangler;
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
	protected ISectionMapper sectionMapper;
	
	protected IUnmangler unmangler;

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
	
	public Collection<ISymbol> findSymbols(String name) {
		List<ISymbol> matchSymbols = new ArrayList<ISymbol>();
		
		// look for exact symbols
		for (ISymbol symbol : symbols) {
			String symName = symbol.getName();
			if (symName.equals(name)) {
				matchSymbols.add(symbol);
			}
		}
		if (!matchSymbols.isEmpty())
			return matchSymbols;
		
		// try for a decorated symbol if no match
		if (unmangler != null) {
			for (ISymbol symbol : symbols) {
				String symName = unmangler.undecorate(symbol.getName());
				if (symName.equals(name)) {
					matchSymbols.add(symbol);
				}
			}
		}
		
		return matchSymbols;
	}
	
	public Collection<ISymbol> findUnmangledSymbols(String name) {
		List<ISymbol> matchSymbols = new ArrayList<ISymbol>();

		if (unmangler != null) {
			name = unmangler.undecorate(name);
			
			String nameNoSpaces = name.replaceAll("\\s", "");
			
			// remove full qualifier
			if (nameNoSpaces.startsWith("::"))
				nameNoSpaces = nameNoSpaces.substring(2);
			
			boolean nameNoArguments = !nameNoSpaces.endsWith(")");
			
			// avoid unmangling a lot of irrelevant symbols by filtering out symbols not containing the base name
			String undecoratedBase = nameNoSpaces;
			int idx = undecoratedBase.lastIndexOf(':');
			if (idx >= 0)
				undecoratedBase = undecoratedBase.substring(idx+1);
			idx = undecoratedBase.indexOf('(');
			if (idx >= 0)
				undecoratedBase = undecoratedBase.substring(0, idx);
			
			for (ISymbol symbol : symbols) {
				String symName = symbol.getName();
				if (!symName.contains(undecoratedBase))
					continue;
				
				try {
					String unmangled = unmangler.unmangle(unmangler.undecorate(symName));
					if (unmangled != null) {
						String unmangledNoSpaces;
						// remove any 'const' which is in front of '(' for now
						unmangledNoSpaces = unmangled.replaceAll("\\bconst\\s*(?=\\()", "");
						unmangledNoSpaces = unmangledNoSpaces.replaceAll("\\s", "");
						
						// remove full qualifier
						if (unmangledNoSpaces.startsWith("::"))
							unmangledNoSpaces = unmangledNoSpaces.substring(2);
						
						if (nameNoSpaces.equals(unmangledNoSpaces)) {
							matchSymbols.add(symbol);
						} else if (nameNoArguments) {
							// try to match the name against a function
							idx = unmangledNoSpaces.lastIndexOf('(');
							if (idx >= 0) {
								String unmangledNoArguments = unmangledNoSpaces.substring(0, idx);
								if (unmangledNoArguments.equals(nameNoSpaces)) {
									matchSymbols.add(symbol);
								}
							}
						}
					}
				} catch (UnmanglingException e) {
					// nope
				}
			}
			if (!matchSymbols.isEmpty())
				return matchSymbols;
		}
		
		return matchSymbols;
	}

	public IUnmangler getUnmangler() {
		return unmangler;
	}
}
