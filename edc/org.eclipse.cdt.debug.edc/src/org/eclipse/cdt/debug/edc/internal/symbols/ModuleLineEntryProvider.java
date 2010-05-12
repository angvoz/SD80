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
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.core.IAddress;
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
	
	static class FileLineEntryProvider implements ILineEntryProvider {


		protected List<ILineEntry> lineEntries = new ArrayList<ILineEntry>();

		// use TreeMap so line number keys are sorted in ascending order
		protected TreeMap<Integer, List<ILineEntry>> lineEntriesByLine = new TreeMap<Integer, List<ILineEntry>>();
		
		private IPath filePath;

		private final ICompileUnitScope compileUnitScope;

		private boolean sorted;

		public FileLineEntryProvider(ICompileUnitScope compileUnitScope, IPath path) {
			this.compileUnitScope = compileUnitScope;
			this.filePath = path;
			this.sorted = true;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			// note: peeking into lowAddress to avoid dynamically parsing stuff while viewing #toString() 
			return filePath + " at " + ((CompileUnitScope)compileUnitScope).lowAddress  + ": " + lineEntries.size() + " entries"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		/**
		 * @param entry
		 */
		public void addLineEntry(ILineEntry entry) {
			//System.out.println("Adding " + entry + " for " + compileUnitScope);
			lineEntries.add(entry);

			List<ILineEntry> currentMappings = lineEntriesByLine.get(entry.getLineNumber());
			if (currentMappings == null) {
				currentMappings = new ArrayList<ILineEntry>();
			}
			currentMappings.add(entry);
			lineEntriesByLine.put(entry.getLineNumber(), currentMappings);		
			
			sorted = false;
		}
		
		public ILineEntry getLineEntryAtAddress(IAddress linkAddress) {
			if (!sorted) {
				// sort by start address for faster lookup by address
				Collections.sort(lineEntries);
				sorted = true;
			}
			
			int insertion = Collections.binarySearch(lineEntries, linkAddress);
			if (insertion >= 0) {
				return lineEntries.get(insertion);
			}

			if (insertion == -1) {
				return null;
			}

			insertion = -insertion - 1;

			ILineEntry entry = lineEntries.get(insertion - 1);
			if (linkAddress.compareTo(entry.getHighAddress()) < 0) {
				return entry;
			}

			return null;
		}

		public Collection<ILineEntry> getLineEntriesForLines(IPath path, int startLineNumber, int endLineNumber) {
			// FIXME: ugly drive letter stuff
			if (!filePath.setDevice(null).equals(path.setDevice(null)) )
				return Collections.emptyList();
			
			List<ILineEntry> entries = new ArrayList<ILineEntry>();

			List<ILineEntry> startMappings = lineEntriesByLine.get(startLineNumber);
			if (startMappings != null) {
				if (startLineNumber == endLineNumber) {
					entries.addAll(startMappings);
				} else if (endLineNumber == -1) {
					// return the entries for the rest of the file
					entries = lineEntries.subList(lineEntries.indexOf(startMappings.get(0)), lineEntries.size());
				} else {
					List<ILineEntry> endMappings = lineEntriesByLine.get(endLineNumber);
					if (endMappings != null) {
						entries = lineEntries.subList(lineEntries.indexOf(startMappings.get(0)), lineEntries
								.indexOf(endMappings.get(endMappings.size() - 1)) + 1);
					} else {
						// no mapping for end line #. just go to the end of the file
						entries = lineEntries.subList(lineEntries.indexOf(startMappings.get(0)), lineEntries.size());
					}
				}
			}

			return Collections.unmodifiableCollection(entries);
		}

		public ILineEntry getNextLineEntry(ILineEntry lineEntry) {
			if (lineEntry.getFilePath().equals(filePath)) {
				SortedMap<Integer, List<ILineEntry>> subMap = lineEntriesByLine.tailMap(lineEntry.getLineNumber() + 1);
				if (!subMap.isEmpty()) {
					IFunctionScope function = ignoreInlineFunctions(compileUnitScope.getFunctionAtAddress(lineEntry.getLowAddress()));
					if (function == null) {
						return null;
					}
					for (ILineEntry nextEntry : subMap.get(subMap.firstKey())) {
						// return the entry at the next line if it's in the
						// same function and has a higher address
						IFunctionScope nextFunction = ignoreInlineFunctions(compileUnitScope.getFunctionAtAddress(nextEntry.getLowAddress()));
						if (function.equals(nextFunction)) {
							if (nextEntry.getLowAddress().compareTo(lineEntry.getHighAddress()) >= 0) {
								return nextEntry;
							}
						}
					}
				}
			}
			return null;
		}

		private IFunctionScope ignoreInlineFunctions(IFunctionScope function) {
			if (function == null)
				return null;
			
			while (function.getParent() instanceof IFunctionScope)
			{
				function = (IFunctionScope) function.getParent();
			}			
			return function;
		}

	}
	
	// CUs we've already considered
	private Set<ICompileUnitScope> parsedCUs = new HashSet<ICompileUnitScope>();
	// mapping to find info for a given path
	private Map<IPath, List<FileLineEntryProvider>> pathToLineEntryMap = new HashMap<IPath, List<FileLineEntryProvider>>();
	// all known providers
	private List<FileLineEntryProvider> fileProviders = new ArrayList<FileLineEntryProvider>();
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
				FileLineEntryProvider provider = getFileLineProviderForCU(cu,
						declFile);
				
				int declLine = func.getDeclLine();
				int declColumn = func.getDeclColumn();
				
				// is there already an entry at this line?
				Collection<ILineEntry> curEntries = provider.getLineEntriesForLines(declFile, declLine, declLine);
				if (curEntries.isEmpty()) {
					// no, add one, and make it range from our start to the first actual line
					
					LineEntry entry = new LineEntry(declFile, declLine, declColumn, func.getLowAddress(), 
							func.getLowAddress());
					provider.addLineEntry(entry);
				}
			}
		}	
	}



	private FileLineEntryProvider getFileLineProviderForCU(
			ICompileUnitScope cu, IPath declFile) {
		List<FileLineEntryProvider> providers = pathToLineEntryMap.get(declFile);
		if (providers != null) {
			for (FileLineEntryProvider p : providers) {
				if (p.compileUnitScope.equals(cu)) {
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
		List<FileLineEntryProvider> fileEntries = pathToLineEntryMap.get(path);
		if (fileEntries == null) {
			fileEntries = new ArrayList<FileLineEntryProvider>();
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
		
		// FIXME: drive letter nastiness
		for (Map.Entry<IPath, List<FileLineEntryProvider>> entry : pathToLineEntryMap.entrySet()) {
			if (entry.getKey().setDevice(null).equals(sourceFile.setDevice(null))) {
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
		List<FileLineEntryProvider> matches = pathToLineEntryMap.get(file);
		if (matches == null)
			return Collections.emptyList();
		
		List<ILineEntry> ret = null;
		for (FileLineEntryProvider provider : matches) {
			Collection<ILineEntry> entries = provider.getLineEntriesForLines(file, startLineNumber, endLineNumber);
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
			fileProviderArray = (FileLineEntryProvider[]) fileProviders.toArray(new FileLineEntryProvider[fileProviders.size()]);
		}
		for (FileLineEntryProvider provider : fileProviderArray) {
			// Narrow down the search to avoid iterating potentially hundreds of duplicates of the same file 
			// (e.g. for stl_vector.h, expanded N times for N std::vector<T> uses).
			// (Don't use #getScopeAtAddress() since this preparses too much.)
			if (provider.compileUnitScope.getLowAddress().compareTo(linkAddress) <= 0
					&& provider.compileUnitScope.getHighAddress().compareTo(linkAddress) > 0) {
				ILineEntry entry = provider.getLineEntryAtAddress(linkAddress);
				if (entry != null)
					return entry;
			}
		}
		return null;
	}



	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.ILineEntryProvider#getNextLineEntry(org.eclipse.cdt.debug.edc.internal.symbols.ILineEntry)
	 */
	public ILineEntry getNextLineEntry(ILineEntry entry) {
		List<FileLineEntryProvider> matches = pathToLineEntryMap.get(entry.getFilePath());
		if (matches == null)
			return null;
		
		// possible concurrent access if we read new files while searching
		FileLineEntryProvider[] matchArray = (FileLineEntryProvider[]) matches
				.toArray(new FileLineEntryProvider[matches.size()]);
		
		for (FileLineEntryProvider provider : matchArray) {
			// Narrow down the search to avoid iterating potentially hundreds of duplicates of the same file 
			// (e.g. for stl_vector.h, expanded N times for N std::vector<T> uses).
			// (Don't use #getScopeAtAddress() since this preparses too much.).
			if (provider.compileUnitScope.getLowAddress().compareTo(entry.getLowAddress()) <= 0
					&& provider.compileUnitScope.getHighAddress().compareTo(entry.getHighAddress()) > 0) {
				ILineEntry next = provider.getNextLineEntry(entry);
				if (next != null)
					return next;
			}
		}
		return null;
	}
}
