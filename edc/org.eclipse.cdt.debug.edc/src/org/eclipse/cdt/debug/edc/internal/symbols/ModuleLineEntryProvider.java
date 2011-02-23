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

package org.eclipse.cdt.debug.edc.internal.symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.PathUtils;
import org.eclipse.cdt.debug.edc.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.symbols.ILineEntryProvider;
import org.eclipse.cdt.debug.edc.symbols.IModuleLineEntryProvider;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.core.runtime.IPath;

/**
 * This class holds a conglomeration of line entry data for an entire
 * module.  
 */
public class ModuleLineEntryProvider implements IModuleLineEntryProvider {
	
	/**
	 *	basically, a typedef of {@link ArrayList}&lt;{@link FileLineEntryProvider}&gt;
	 */
	private final class FileLineEntryProviders extends ArrayList<FileLineEntryProvider> {
		private static final long serialVersionUID = -2157263701372708990L;		
	}

	/**
	 *	basically, a typedef of {@link HashMap}&lt;{@link IPath},{@link FileLineEntryProvider}&gt;
	 */
	private final class PathToLineEntryMap extends HashMap<IPath, FileLineEntryProviders> {
		private static final long serialVersionUID = 7064789571684986782L;
	}

	// CUs we've already considered
	private Set<ICompileUnitScope> parsedCUs = new HashSet<ICompileUnitScope>();
	// mapping to find info for a given path
	private PathToLineEntryMap pathToLineEntryMap = new PathToLineEntryMap();
	// all known providers
	private FileLineEntryProviders fileProviders = new FileLineEntryProviders();
	// cached array of providers
	private FileLineEntryProvider[] fileProviderArray;

	public ModuleLineEntryProvider() {

	}

	
	/**
	 * Add the line entries from a compilation unit to the mapper.
	 * @param scope
	 */
	public void addCompileUnit(ICompileUnitScope cu) {
		if (parsedCUs.contains(cu))
			return;
		
		parsedCUs.add(cu);
		
		Collection<ILineEntry> lineEntries = cu.getLineEntries();
		
		if (lineEntries.size() > 0) {
			// files created for this compile unit scope (union of all CUs in this.lineEntryMap)
			// (kept because we visit the same file a lot in this function)
			Map<IPath, FileLineEntryProvider> fileProviders = new HashMap<IPath, FileLineEntryProvider>(4);
			
			// go through each entry and extract entries for each file.
			//
			// allocate one FileLineEntryProvider per CU
			//
			for (ILineEntry entry : lineEntries) {
				IPath path = entry.getFilePath();
				
				FileLineEntryProvider provider = fileProviders.get(path);
				if (provider == null) {
					// This will look for an existing one and create a 
					// new one if none exits.
					provider = getFileLineProviderForCU(cu,	path);
					provider.setCULineEntries(lineEntries);
					fileProviders.put(path, provider);
				}
	
				provider.addLineEntry(entry);
			}
		}
		
		// then, look for lines provided by decl file/line/column entries
		for (IScope child : cu.getChildren()) {
			addCompileUnitChild(cu, child);
		}
	}



	/**
	 * Add (or update) a compile unit child entry (function) by adding a
	 * line entry for its declaration location, which may differ from the
	 * first line inside the function to which the low PC refers. 
	 * @param cu
	 * @param child
	 */
	public void addCompileUnitChild(ICompileUnitScope cu, IScope child) {
		if (child instanceof IFunctionScope) {
			IFunctionScope func = (IFunctionScope) child;
			IPath declFile = func.getDeclFile();
			
			if (declFile != null) {
				// this is the slow path for dynamic parsing
				FileLineEntryProvider provider
				  = getFileLineProviderForCU(cu, declFile);
				
				int declLine = func.getDeclLine();
				int declColumn = func.getDeclColumn();
				
				// is there already an entry at this line?
				Collection<ILineEntry> curEntries
				  = provider.getLineEntriesForLines(declFile, declLine, declLine);
				if (curEntries.isEmpty()) {
					// no, add one, and make it range from our start to the first actual line
					
					LineEntry entry
					  = new LineEntry(declFile, declLine, declColumn,
									  func.getLowAddress(), func.getLowAddress());
					provider.addLineEntry(entry);
				}
			}
		}	
	}


	private FileLineEntryProvider getFileLineProviderForCU(
			ICompileUnitScope cu, IPath declFile) {
		FileLineEntryProviders providers = pathToLineEntryMap.get(declFile);
		if (providers != null) {
			for (FileLineEntryProvider p : providers) {
				if (p.getCU().equals(cu)) {
					return p;
				}
			}
		}
		FileLineEntryProvider provider = new FileLineEntryProvider(cu, declFile);
		registerFileLineEntryProvider(declFile, provider);
		return provider;
	}



	private void registerFileLineEntryProvider(IPath path,
			FileLineEntryProvider provider) {
		FileLineEntryProviders fileEntries = pathToLineEntryMap.get(path);
		if (fileEntries == null) {
			fileEntries = new FileLineEntryProviders();
			pathToLineEntryMap.put(path, fileEntries);
		}
		fileEntries.add(provider);
		fileProviders.add(provider);
		fileProviderArray = null;
	}
	
	/**
	 * Get the line entry providers for the given source file.  
	 * @path sourceFile the absolute path to the source file
	 * @return the unmodifiable list of providers for the file, possibly empty.
	 */
	public Collection<ILineEntryProvider> getLineEntryProvidersForFile(IPath sourceFile) {
		List<? extends ILineEntryProvider> cus = pathToLineEntryMap.get(sourceFile);
		if (cus != null)
			return Collections.unmodifiableCollection(cus);
		
		for (Map.Entry<IPath, FileLineEntryProviders> entry : pathToLineEntryMap.entrySet()) {
			if (!PathUtils.isCaseSensitive() && entry.getKey().toOSString().compareToIgnoreCase(sourceFile.toOSString()) == 0) {
				cus = entry.getValue();
				pathToLineEntryMap.put(sourceFile, entry.getValue());
				return Collections.unmodifiableCollection(cus);
			}
		}
		
		for (Map.Entry<IPath, FileLineEntryProviders> entry : pathToLineEntryMap.entrySet()) {
			if (entry.getKey().equals(sourceFile)) {
				cus = entry.getValue();
				return Collections.unmodifiableCollection(cus);
			}
		}
		
		return Collections.emptyList();
	}



	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.ILineEntryProvider#getLineEntriesForLines(org.eclipse.core.runtime.IPath, int, int)
	 */
	public Collection<ILineEntry> getLineEntriesForLines(IPath file,
			int startLineNumber, int endLineNumber) {
		FileLineEntryProviders matches = pathToLineEntryMap.get(file);
		if (matches == null)
			return Collections.emptyList();
		
		List<ILineEntry> ret = null;
		for (FileLineEntryProvider provider : matches) {
			Collection<ILineEntry> entries
			  = provider.getLineEntriesForLines(file, startLineNumber, endLineNumber);
			if (!entries.isEmpty()) {
				if (ret == null)
					ret = new ArrayList<ILineEntry>(entries);
				else
					ret.addAll(entries);
			}
		}
		if (ret == null)
			return Collections.emptyList();
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.ILineEntryProvider#getLineEntryAtAddress(org.eclipse.cdt.core.IAddress)
	 */
	public ILineEntry getLineEntryAtAddress(IAddress linkAddress) {
		// scanning files can introduce new file providers; avoid ConcurrentModificationException
		if (fileProviderArray == null) {
			fileProviderArray = fileProviders.toArray(new FileLineEntryProvider[fileProviders.size()]);
		}
		for (FileLineEntryProvider provider : fileProviderArray) {
			// Narrow down the search to avoid iterating potentially hundreds
			// of duplicates of the same file 
			// (e.g. for stl_vector.h, expanded N times for N std::vector<T> uses).
			// (Don't use #getScopeAtAddress() since this preparses too much.)
			if (provider.getCU().getLowAddress().compareTo(linkAddress) <= 0
					&& provider.getCU().getHighAddress().compareTo(linkAddress) > 0) {
				ILineEntry entry = provider.getLineEntryAtAddress(linkAddress);
				if (entry != null

					/*	FIXME: sigh ...
					 *
					 *	yet another RVCT DWARF inlined LNT entry generation bug ...
					 *
					 *	we just can't have entry.highAddr == entry.lowAddr!
					 *
					 *	that just TOTALLY ruins the illusion of stepping.
					 *
					 *	see, if we pass back the address we're on,
					 *	no step-over range will get created,
					 *	and the debugger will just run ...
					 *		
					 *	... and that's just NotGood-TM .
					 */
						&& !entry.getLowAddress().equals(entry.getHighAddress())

						) {
					return entry;
				}
			}
		}
		return null;
	}


	public ILineEntry getLineEntryInFunction(IAddress linkAddress, IFunctionScope parentFunction) {
		ILineEntry functionEntry = getLineEntryAtAddress(parentFunction.getLowAddress());
		if (functionEntry == null)
			return null;
		Collection<ILineEntryProvider> parentProviders
		  = getLineEntryProvidersForFile(functionEntry.getFilePath());
		for (ILineEntryProvider iLineEntryProvider : parentProviders) {
			if (iLineEntryProvider instanceof FileLineEntryProvider) {
				ILineEntry entry
				  = ((FileLineEntryProvider)iLineEntryProvider).getLineEntryInFunction(linkAddress, parentFunction);
				if (entry != null)
					return entry;
			}
		}
		return null;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.ILineEntryProvider#getNextLineEntry(org.eclipse.cdt.debug.edc.internal.symbols.ILineEntry)
	 */
	public ILineEntry getNextLineEntry(final ILineEntry entry, final boolean collapseInlineFunctions) {
		if (entry == null)
			return null;

		final IAddress entryLowAddr = entry.getLowAddress();
		final IAddress entryHighAddr = entry.getHighAddress();
		final IPath entryPath = entry.getFilePath();
		FileLineEntryProviders matches = pathToLineEntryMap.get(entryPath);
		if (matches == null)
			return null;

		// avoid possible concurrent access if we read new files while searching
		FileLineEntryProvider[] matchArray = matches.toArray(new FileLineEntryProvider[matches.size()]);

		for (FileLineEntryProvider provider : matchArray) {
			ICompileUnitScope cuScope = provider.getCU();
			// Narrow down the search to avoid iterating potentially hundreds of duplicates of the same file 
			// (e.g. for stl_vector.h, expanded N times for N std::vector<T> uses).
			// (Don't use #getScopeAtAddress() since this preparses too much.).
			if (cuScope.getLowAddress().compareTo(entryLowAddr) <= 0
					// NOTE: high addrs for both scope & line entries are inclusive: thus >= 0
					&& cuScope.getHighAddress().compareTo(entryHighAddr) >= 0) {

				// provider.getNextLineEntry() returns null for only 1 reason:
				// 1) there are no more entries at all for the compileUnitScope
				//
				// so there's no need to continue looking in other providers
				return provider.getNextLineEntry(entry, collapseInlineFunctions);
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.ILineEntryProvider#getPreviousLineEntry
	 */
	public ILineEntry getPreviousLineEntry(ILineEntry entry,
			boolean collapseInlineFunctions) {
		if (entry == null)
			return null;

		FileLineEntryProviders matches = pathToLineEntryMap.get(entry.getFilePath());
		if (matches != null) {
			final IAddress entryLowAddr  = entry.getLowAddress();
			final IAddress entryHighAddr = entry.getHighAddress();
			boolean entryIsInline = false, inlineEstablished = false;

			// avoid possible concurrent access if we read new files while searching
			FileLineEntryProvider[] matchArray = matches.toArray(new FileLineEntryProvider[matches.size()]);

			for (FileLineEntryProvider provider : matchArray) {
				ICompileUnitScope cuScope = provider.getCU();
				// Narrow down the search to avoid iterating potentially hundreds of duplicates of the same file 
				// (e.g. for stl_vector.h, expanded N times for N std::vector<T> uses).
				// (Don't use #getScopeAtAddress() since this preparses too much.).
				//
				// 
				if (cuScope.getLowAddress().compareTo(entryLowAddr) <= 0
						// NOTE: high addrs for both scope & line entries are inclusive: thus >= 0
						&& cuScope.getHighAddress().compareTo(entryHighAddr) >= 0) {
					if (!inlineEstablished) {
						entryIsInline = FileLineEntryProvider.isInlinedFunction(cuScope.getFunctionAtAddress(entryLowAddr));
						inlineEstablished = true;
					}
					ILineEntry prev = provider.getPreviousLineEntry(entry, collapseInlineFunctions);
					if (prev == null && collapseInlineFunctions && entryIsInline) {
						// retry with the provider mapped from the compileUnitScope.filePath
						return provider.getPreviousLineEntryInCU(entry);
					}

					if (prev != null) {	// in case there's another provider
						return prev;
					}
				}
			}
		}
		return null;
	}
	
}
