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
import org.eclipse.cdt.debug.edc.internal.PathUtils;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfCompileUnit;
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
		protected List<ILineEntry> cuEntries   = null;

		// use TreeMap so line number keys are sorted in ascending order
		protected TreeMap<Integer, List<ILineEntry>> lineEntriesByLine = new TreeMap<Integer, List<ILineEntry>>();
		protected TreeMap<IAddress, ILineEntry> lineEntriesByAddress = new TreeMap<IAddress, ILineEntry>();

		private IPath filePath;

		private final ICompileUnitScope compileUnitScope;

		private boolean sorted;

		public FileLineEntryProvider(ICompileUnitScope compileUnitScope, IPath path) {
			this.compileUnitScope = compileUnitScope;
			this.filePath = path;
			this.sorted = true;
		}

		protected void setCULineEntries(Collection<ILineEntry> entries) {
			cuEntries = new ArrayList<ILineEntry>(entries);			
		}

		protected List<ILineEntry> getCULineEntries() {
			if (cuEntries == null)
				setCULineEntries(compileUnitScope.getLineEntries());
			return cuEntries;
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
				lineEntriesByLine.put(entry.getLineNumber(), currentMappings);		
			}
			currentMappings.add(entry);

			ILineEntry currentByAddress = lineEntriesByAddress.get(entry.getLowAddress());
			
			if (   currentByAddress == null
				|| entry.getHighAddress().compareTo(currentByAddress.getHighAddress()) > 0) {
				lineEntriesByAddress.put(entry.getLowAddress(), entry);
			}

			sorted = false;
		}

		public ILineEntry getLineEntryAtAddress(IAddress linkAddress) {
			// NOTE: lineEntries can, and does, have multiple entries with the same low address
			if (!sorted) {
				// sort by start address for faster lookup by address
				Collections.sort(lineEntries);
				sorted = true;
			}
			int insertion = getLineEntryInsertionForAddress(linkAddress, lineEntries);
			if (-1 != insertion)
				return lineEntries.get(insertion);
			return null;
		}

		private int getLineEntryInsertionForAddress(IAddress linkAddress,
				final List<? extends ILineEntry> entriesToSearch) {

			int insertion = Collections.binarySearch(entriesToSearch, linkAddress);
			ILineEntry newEntry;
			int newInsertion;

			if (insertion >= 0) {
				// line entry's low address exactly matches linkAddress, but if the line
				// entry has an empty address range, see if a previous or subsequent
				// line entry with the same start address has a nonempty range
 				ILineEntry entry = entriesToSearch.get(insertion);
				
				if (entry.getHighAddress().compareTo(entry.getLowAddress()) != 0)
					return insertion;

				if (insertion > 0) {
					newInsertion = insertion - 1;
					newEntry = entriesToSearch.get(newInsertion);
					while (newEntry.getLowAddress().compareTo(entry.getLowAddress()) == 0) {
						if (newEntry.getHighAddress().compareTo(newEntry.getLowAddress()) != 0) {
							return newInsertion;
						}
						if (--newInsertion < 0)
							break;
						newEntry = entriesToSearch.get(newInsertion);
					}
				}

				if (insertion < entriesToSearch.size() - 1) {
					newInsertion = insertion + 1;
					newEntry = entriesToSearch.get(newInsertion);
					while (newEntry.getLowAddress().compareTo(entry.getLowAddress()) == 0) {
						if (newEntry.getHighAddress().compareTo(newEntry.getLowAddress()) != 0) {
							return newInsertion;
						}
						if (++newInsertion == entriesToSearch.size())
							break;
						newEntry = lineEntries.get(newInsertion);
					}
				}
				
				return insertion;
			}

			if (insertion == -1) {
				return -1;
			}

			// after a failed binary search, link address is > low address of -insertion-2
			// so see if a previous entry with the same low address has a nonempty range
			// that includes linkAddress
			insertion = -insertion - 2;

			ILineEntry entry = entriesToSearch.get(insertion);

			// low address of entry at insertion cannot match linkAddress
			if (insertion > 0 && entry.getHighAddress().compareTo(entry.getLowAddress()) == 0) {
				newInsertion = insertion - 1;
				newEntry = entriesToSearch.get(newInsertion);
				while (newEntry.getLowAddress().compareTo(entry.getLowAddress()) == 0) {
					if (newEntry.getHighAddress().compareTo(newEntry.getLowAddress()) != 0) {
						if (linkAddress.compareTo(newEntry.getHighAddress()) < 0)
							return newInsertion;
					}
					if (--newInsertion < 0)
						break;
					newEntry = entriesToSearch.get(newInsertion);
				}
			}
			
			if (linkAddress.compareTo(entry.getHighAddress()) < 0)
				return insertion;

			return -1;
		}

		public Collection<ILineEntry> getLineEntriesForLines(IPath path, int startLineNumber, int endLineNumber) {
			// FIXME: ugly drive letter stuff
			if (!filePath.setDevice(null).equals(path.setDevice(null)) )
			{
				if (!PathUtils.isCaseSensitive() && filePath.toOSString().compareToIgnoreCase(path.toOSString()) != 0)
					return Collections.emptyList();			
			}

			int lntSize = lineEntries.size();
			// Note: this may not be the last line:
			//	  lineEntries.get(lntSize-1).getLineNumber();
			// as I've seen line table like this for a source file
			// (illustrated by line #s):
			//     7, 8, 25, 26, 12, 14
			// where line (7, 8) forms one function, (25,26) one function,
			// and (12, 14) one function.
			int endLine = (endLineNumber != -1) ? endLineNumber : 
							lineEntriesByLine.lastKey();

			List<ILineEntry> entries = new ArrayList<ILineEntry>(), startMappings;
			
			/* in case the caller has requested something other than a single line,
			 * make certain this doesn't fail if the the caller passes a
			 * startLineNumber that doesn't have a direct mapping in the LNT
			 */
			for (; null == (startMappings = lineEntriesByLine.get(startLineNumber))
				   && startLineNumber < endLine
				 ; ++startLineNumber) {}

			if (startMappings != null) {
				if (startLineNumber == endLineNumber) {
					entries.addAll(startMappings);
				} else if (endLineNumber == -1) {
					// return the entries for the rest of the file
					entries = lineEntries.subList(lineEntries.indexOf(startMappings.get(0)), lntSize);
				} else {
					List<ILineEntry> endMappings = lineEntriesByLine.get(endLineNumber);
					if (endMappings != null) {
						entries = lineEntries.subList(lineEntries.indexOf(startMappings.get(0)), lineEntries
								.indexOf(endMappings.get(endMappings.size() - 1)) + 1);
					} else {
						// no mapping for end line #. just go to the end of the file
						entries = lineEntries.subList(lineEntries.indexOf(startMappings.get(0)), lntSize);
					}
				}
			}

			return Collections.unmodifiableCollection(entries);
		}

		public ILineEntry getNextLineEntry(ILineEntry entry, boolean collapseInlineFunctions) {
			if (entry == null || isLastEntryInCU(entry))
				return null;
			IFunctionScope entryFn = compileUnitScope.getFunctionAtAddress(entry.getLowAddress());
			IFunctionScope container = ignoreInlineFunctions(entryFn);
			if (container == null)	// relies on ignoreInlineFunctions() to return null if func==null
				return null;

			do {	// loop is for continue to retry the next entry in same function

				// check if there's even a need to do further operations
				IAddress desiredAddr = entry.getHighAddress();
				if (desiredAddr.compareTo(container.getHighAddress()) > 0)
					return null;
	
				SortedMap<IAddress, ILineEntry> tailAddrs
				  = desiredAddr.equals(entry.getLowAddress())	// can be equal due to DWARF generation bug
						? null : lineEntriesByAddress.tailMap(desiredAddr);
	
				// no other lines in this provider; try the CU line entries
				if (tailAddrs == null || tailAddrs.isEmpty()) {
					// the following case is that we are at the first line of an inline
					// that would otherwise be the last line of a function.
					if (collapseInlineFunctions && entryFn != container
							&& entry.getLowAddress().equals(entryFn.getLowAddress())
							&& entryFn.getParent().equals(container))
						return getDifferentLineEntryInCU(container, entry, entryFn.getHighAddress(),
														  collapseInlineFunctions);
					else
						return getDifferentLineEntryInCU(entryFn, entry, desiredAddr,
														  collapseInlineFunctions);
				}

				IAddress foundAddr = tailAddrs.firstKey();
				ILineEntry next = tailAddrs.get(foundAddr);
				IFunctionScope foundFn = compileUnitScope.getFunctionAtAddress(foundAddr);
	
				// [1] if the function of our the current instr ptr entry and
				// the function at the found addr are identical, then take it!!
				// (i.e. we lucked out!!  all lines in the gap 
				// in other providers are nested inlines.)
				if (entryFn.equals(foundFn)) {
					// ... ok, well, line number must be different, too.
					if (next.getLineNumber() != entry.getLineNumber())
						return next;
					entry = next;		// just pretend this was the entry ...
					continue;			// ... and try again
	
				// [2]if the foundAddr is immediately after this entry ... 
				} else if (foundAddr.equals(desiredAddr) && foundFn != null) {		// [2]
	
					/// ... and it is
					// [2a] an inline parent of this inline
					// [2b] an inline and a direct sibling (i.e. not a cousin) of this inline
					// then take it!!
					if (entryFn.getParent().equals(foundFn)							// [2a]
							|| entryFn.getParent().equals(foundFn.getParent())) {	// [2b]
						return tailAddrs.get(foundAddr);
	
					// ... or if it is
					// [2c] the first line of an inline whose next line in the
					//      lineEntriesByLine table is identical to next
					// then we'll call that good enough
					// (you may be reading this if you have an inline function
					// in the same file as the parent function invoking it and
					// nothing in between; step over is probably broken for you.)
					} else if (foundFn.getParent().equals(entryFn)
							&& areEntriesAdjacentLines(entry, next)) {
						return next;
					}
	
				// similar to [2a] & [2b], even if foundAddr shows a gap
				// [3] if the current location is an inline, and entry
				//     adjacent to the one passed is identical to next
				// again, call it good enough for now.
				} else if (collapseInlineFunctions
						&& (entryFn.getParent().equals(foundFn)
							|| entryFn.getParent().equals(container))
						&& areEntriesAdjacentLines(entry, next)) {
					return next;
				}
	
	
				// getting here means 1 (or both) of 2 things (both slightly irrelevant)
				//
				// either:
				// a] entryFn is an ancestor of foundFn
				// b] there's a gap between the desiredAddr & the foundAddr
				//
				// in either case, the next entry will be found in 
				// this.compileUnitScope.lineEntries .  so get it from there.
				return getDifferentLineEntryInCU(entryFn, entry, desiredAddr,
												 collapseInlineFunctions);

			} while (true);
		}

		/**
		 * @param entry
		 * @param next
		 */
		private boolean areEntriesAdjacentLines(ILineEntry entry, ILineEntry next) {
			if (entry.getLineNumber() == next.getLineNumber())
				return false;
			SortedMap<Integer, List<ILineEntry>> tailLines
			  = lineEntriesByLine.tailMap(entry.getLineNumber());
			if (tailLines != null) {
				List<ILineEntry> entries = tailLines.get(entry.getLineNumber());
				if (entries != null) {
					int entryIdx = entries.indexOf(entry);
					if (-1 != entryIdx) {
						if (entry.equals(entries.get(0))
								&& ++entryIdx == entries.size()) {
							entries = tailLines.get(next.getLineNumber());
							if (entries != null && next.equals(entries.get(0))) {
								return true;
			}	}	}	}	}

			return false;
		}

		/**
		 * @param entryFn
		 * @param desiredAddr
		 * @return
		 */
		private ILineEntry getDifferentLineEntryInCU(IFunctionScope entryFn,
				ILineEntry origEntry, IAddress desiredAddr,
				boolean collapseInlineFunctions) {
			if (compileUnitScope instanceof DwarfCompileUnit) {	// known to be a sorted list
				if (getCULineEntries().isEmpty())
					return null;
				int insertion = getLineEntryInsertionForAddress(desiredAddr, cuEntries);
				if (-1 != insertion)
					for (; insertion < cuEntries.size(); ++insertion) {
						ILineEntry next = cuEntries.get(insertion);
						if (isGoodEntry(next, entryFn, origEntry, collapseInlineFunctions))
							return next;
				}
			} else {
				boolean firstFound = false;
				for (ILineEntry next : getCULineEntries()) {
					if (!firstFound && desiredAddr.compareTo(next.getLowAddress()) < 0)
						continue;
					else
						firstFound = true;

					if (isGoodEntry(next, entryFn, origEntry, collapseInlineFunctions))
						return next;
				}
			}


			// by deduction, if we don't hit any of those 3 cases and exhaust all
			// entries in the compuleUnitScope, then every entry after the current
			// one is some sort of inline nested to the current function (possibly
			// even several different inlines nested separately, possibly even with
			// code from the original function at the original line number).
			// 
			// in simple terms, it means the step-over that led here
			// will turn into a step out

			return null;
		}

		private boolean isGoodEntry(ILineEntry e, IFunctionScope origFn,
				ILineEntry origEntry, boolean collapseInlineFunctions) {
			IFunctionScope nextFn
			  = compileUnitScope.getFunctionAtAddress(e.getLowAddress());

			if (origFn.equals(nextFn) && e.getLineNumber() != origEntry.getLineNumber())
				return true;	// case [1] described in caller getNextLineEntry()

			else if (!collapseInlineFunctions || !isAncestorFunction(nextFn, origFn))
				return true;	// case [2] or [3] described in caller getNextLineEntry()

			return false;
		}

		private boolean isFirstEntryInCU(ILineEntry e) throws IllegalArgumentException {
			if (e == null)
				throw new IllegalArgumentException("isFirstEntryInCU() called with null");
			int cuEntriesSize = getCULineEntries().size();
			return (cuEntriesSize > 0 && e.equals(cuEntries.get(0)));			
		}

		private boolean isLastEntryInCU(ILineEntry e) throws IllegalArgumentException {
			if (e == null)
				throw new IllegalArgumentException("isFirstEntryInCU() called with null");
			int cuEntriesSize = getCULineEntries().size();
			return (cuEntriesSize > 0 && e.equals(cuEntries.get(cuEntriesSize-1)));
		}

		public ILineEntry getPreviousLineEntry(ILineEntry entry, boolean collapseInlineFunctions) {
			if (entry == null || isFirstEntryInCU(entry))
				return null;
			IAddress entryAddr = entry.getLowAddress();
			IFunctionScope func = compileUnitScope.getFunctionAtAddress(entryAddr);
			IFunctionScope container = ignoreInlineFunctions(func);
			if (container == null)	// relies on ignoreInlineFunctions() to return null if func==null
				return null;
			SortedMap<IAddress, ILineEntry> headAddrs = lineEntriesByAddress.headMap(entryAddr);
			if (headAddrs.isEmpty())
				return null;

			if (!collapseInlineFunctions)
				return getPreviousLineEntryByAddress(entry, container.getLowAddress(), headAddrs);

			IFunctionScope prevFunc = compileUnitScope.getFunctionAtAddress(entryAddr);
			IFunctionScope prevContainer = ignoreInlineFunctions(prevFunc);
			if (prevContainer == null || !prevContainer.equals(container)) {
				return null;	// relies on ignoreInlineFunctions() to return null if nextFunc==null
			}

			boolean inline = !func.equals(container);
			boolean prevInline = !prevFunc.equals(prevContainer);
			if (inline && prevInline) {
				ILineEntry testPrev = headAddrs.get(headAddrs.lastKey());

				// take the first head in tailAddrs if the function containing entry is
				// [1] identical to the function containing the lastKey of headAddrs
				//     (i.e. in the same inline; skips nested inlines in other providers)
				// [2] an inline parent of this the previous inline
				//     && the top addr of testPrev is immediately bottom addr of entry
				// [3] an inline and a sibling of this previous inline
				//     && the top addr of testPrev is immediately bottom addr of entry
				if (func.equals(prevFunc)											// [1]
					|| (testPrev.getHighAddress().equals(entryAddr)
						&& (prevFunc.getParent().equals(func)						// [2]
							|| prevFunc.getParent().equals(func.getParent())))) {	// [3]
					return testPrev;
				}

				if (!filePath.equals(compileUnitScope.getFilePath()))
					// fall out and force reliance on the provider mapped from the
					// compileUnitScope's filePath (i.e. the parent to these inlines)
					return null; 
			}

			SortedMap<Integer, List<ILineEntry>> headLines
			  = inline
			  		? lineEntriesByLine.headMap(headAddrs.get(headAddrs.lastKey()).getLineNumber()+1)
			  		: lineEntriesByLine.headMap(entry.getLineNumber());

			while (!headLines.isEmpty()) {
				List<ILineEntry> entries = headLines.get(headLines.lastKey());
				for (int i = entries.size()-1; i >= 0; --i) {
					ILineEntry prev = entries.get(i);
					if (!prev.equals(entry)
							&& prev.getHighAddress().compareTo(entryAddr) <= 0
							&& prev.getLowAddress().compareTo(container.getLowAddress()) >= 0
							&& prev.getLineNumber() != entry.getLineNumber()) {
						return prev;
					}
				}
				headLines = headLines.headMap(headLines.lastKey());
			}
			return null;
		}

		/**
		 * @param entry
		 * @param bottom
		 * @param addrEntries
		 * @return
		 */
		private ILineEntry getPreviousLineEntryByAddress(ILineEntry entry, IAddress bottom,
				SortedMap<IAddress, ILineEntry> addrEntries) {
			while (!addrEntries.isEmpty()) {
				ILineEntry prev = addrEntries.get(addrEntries.lastKey());
				if (prev == null || prev.getLowAddress().compareTo(bottom) < 0)
					break;
				if (prev.getLineNumber() != entry.getLineNumber())
					return prev;
				addrEntries = addrEntries.headMap(prev.getLowAddress());
			}
			return null;
		}

		public ILineEntry getLineEntryInFunction(IAddress linkAddress, IFunctionScope parentFunction) {
			
			// get all line entries with low address <= linkAddress
			SortedMap<IAddress, ILineEntry> subMap = lineEntriesByAddress.headMap(linkAddress.add(1));

			if (subMap.isEmpty())
			{
				// if no line entries have a low address <= linkAddress, but the address is
				// definitely in the function, use the first entry
				if (parentFunction.getLowAddress().compareTo(linkAddress) >= 0
						&& parentFunction.getHighAddress().compareTo(linkAddress) < 0)
					return lineEntriesByAddress.values().iterator().next();
				return null;
			}

			// look for an entry that includes linkAddress; if linkAddress is in the gap between
			// two lineEntriesByAddress entries, assume the gap is due to inlined functions' code
			ILineEntry entry = subMap.get(subMap.lastKey());
			
			if (   entry.getHighAddress().compareTo(linkAddress) >= 0
				|| subMap.size() < lineEntriesByAddress.size()) {
				return entry;
			}

			return null;
		}

		/**
		 * ONLY call when caller has 'collapseInlineFunctions == true' and the caller
		 * has failed to get the address in any other way.
		 * 
		 * @param cuScope the scope in which to search for the entry of interest
		 * @param entry the entry in hand, for which the preceding entry is desired
		 * @return a line-entry whose end address is exactly first address of the passed entry
		 */
		protected ILineEntry getPreviousLineEntryInCU(ILineEntry entry) {
			IAddress desiredEndAddress = entry.getLowAddress();

			for (ILineEntry testEntry : getCULineEntries()) {
				if (!desiredEndAddress.equals(testEntry.getHighAddress()))
					continue;
				IFunctionScope entryFn
				  = compileUnitScope.getFunctionAtAddress(entry.getLowAddress());
				IScope entryParent = entryFn.getParent();

				IFunctionScope prevFn
				  = compileUnitScope.getFunctionAtAddress(testEntry.getLowAddress());
				if (isInlinedFunction(entryFn)
					&& (isAncestorFunction(entryFn, prevFn)
						|| entryParent != null && entryParent.equals(prevFn.getParent()))) {
					return testEntry;
				}
				if (isAncestorFunction(prevFn, entryFn)) {
					// TODO: add logic to get entry at start of testEntry
					// something like:
					//	dp {
					//		desiredEndAddress = testEntry.getLowAddress();
					//		testEntry = getPreviousLineEntryInCU(cuScope, testEntry, null);
					//		get back to where the function for testEntry is a sibling or
					//		ancestor of entryFn
					//	} while (testEntry != null)
					// 
					// but this is mostly here to help with step-out while standing
					// on first instruction of inline coinciding with source, so fix later
				}
				break;	// got the address of interest; just wasn't useful as we wanted	
			}
			return null;
		}

	}

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
			if (provider.compileUnitScope.getLowAddress().compareTo(linkAddress) <= 0
					&& provider.compileUnitScope.getHighAddress().compareTo(linkAddress) > 0) {
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

	
	private static boolean isInlinedFunction(IFunctionScope function) {
		return function != null && function.getParent() instanceof IFunctionScope;
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
			ICompileUnitScope cuScope = provider.compileUnitScope;
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

	/**
	 * @param c the child to compare
	 * @param x the function we are seeking to call an ancestor
	 * @return true if there's a function in this linkage where <b>x</b> is an ancestor of <b>c</b>
	 */
	private static boolean isAncestorFunction(IFunctionScope c, IFunctionScope x) {
		for (IScope p = c.getParent(); p != null; p = p.getParent())
			if (p.equals(x))
				return true;
		return false;
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
				ICompileUnitScope cuScope = provider.compileUnitScope;
				// Narrow down the search to avoid iterating potentially hundreds of duplicates of the same file 
				// (e.g. for stl_vector.h, expanded N times for N std::vector<T> uses).
				// (Don't use #getScopeAtAddress() since this preparses too much.).
				//
				// 
				if (cuScope.getLowAddress().compareTo(entryLowAddr) <= 0
						// NOTE: high addrs for both scope & line entries are inclusive: thus >= 0
						&& cuScope.getHighAddress().compareTo(entryHighAddr) >= 0) {
					if (!inlineEstablished) {
						entryIsInline = isInlinedFunction(cuScope.getFunctionAtAddress(entryLowAddr));
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
	
	private static IFunctionScope ignoreInlineFunctions(IFunctionScope function) {
		if (function == null)
			return null;
		
		while (function.getParent() instanceof IFunctionScope) {
			function = (IFunctionScope) function.getParent();
		}			
		return function;
	}
	
}
