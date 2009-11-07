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
package org.eclipse.cdt.debug.edc.internal.symbols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.runtime.IPath;

public abstract class CompileUnitScope extends Scope implements ICompileUnitScope {

	protected IPath filePath;

	protected List<ILineEntry> lineEntries = new ArrayList<ILineEntry>();

	// use TreeMap so line number keys are sorted in ascending order
	protected TreeMap<Integer, List<ILineEntry>> lineEntriesByLine = new TreeMap<Integer, List<ILineEntry>>();

	private boolean lineMappingsSet = false;

	public CompileUnitScope(IPath filePath, IModuleScope parent, IAddress lowAddress, IAddress highAddress) {
		super(filePath != null ? filePath.lastSegment() : "", lowAddress, highAddress, parent);

		this.filePath = filePath;
	}

	public IPath getFilePath() {
		return filePath;
	}

	public IFunctionScope getFunctionAtAddress(IAddress linkAddress) {
		IScope scope = getScopeAtAddress(linkAddress);
		while (scope != null && !(scope instanceof IFunctionScope)) {
			scope = scope.getParent();
		}

		return (IFunctionScope) scope;
	}

	public Collection<ILineEntry> getLineEntries() {
		if (!lineMappingsSet) {
			setLineMappings(parseLineTable());
		}
		return Collections.unmodifiableCollection(lineEntries);
	}

	public ILineEntry getLineEntryAtAddress(IAddress linkAddress) {
		if (!lineMappingsSet) {
			setLineMappings(parseLineTable());
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

	public Collection<ILineEntry> getLineEntriesForLines(int startLineNumber, int endLineNumber) {
		if (!lineMappingsSet) {
			setLineMappings(parseLineTable());
		}

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
		if (lineEntry.getFilePath().equals(getFilePath())) {
			SortedMap<Integer, List<ILineEntry>> subMap = lineEntriesByLine.tailMap(lineEntry.getLineNumber() + 1);
			if (!subMap.isEmpty()) {
				IFunctionScope function = getFunctionAtAddress(lineEntry.getLowAddress());

				for (ILineEntry nextEntry : subMap.get(subMap.firstKey())) {
					// return the entry at the next line if it's in the
					// same function and has a higher address
					if (function.equals(getFunctionAtAddress(nextEntry.getLowAddress()))) {
						if (nextEntry.getLowAddress().compareTo(lineEntry.getLowAddress()) > 0) {
							return nextEntry;
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Parse the line table data - to be implemented by debug format specific
	 * sub classes.
	 * 
	 * @return the list of line table entries (may be empty)
	 */
	protected abstract Collection<ILineEntry> parseLineTable();

	private void setLineMappings(Collection<ILineEntry> entries) {

		// create a map of entries by line for faster lookup
		for (ILineEntry entry : entries) {
			lineEntries.add(entry);

			// only do this for the main cu file
			if (entry.getFilePath().equals(getFilePath())) {
				List<ILineEntry> currentMappings = lineEntriesByLine.get(entry.getLineNumber());
				if (currentMappings == null) {
					currentMappings = new ArrayList<ILineEntry>();
				}
				currentMappings.add(entry);
				lineEntriesByLine.put(entry.getLineNumber(), currentMappings);
			}
		}

		// sort by start address for faster lookup by address
		Collections.sort(lineEntries);

		lineMappingsSet = true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CompileUnitScope [");
		builder.append("lowAddress=");
		builder.append(lowAddress);
		builder.append(", highAddress=");
		builder.append(highAddress);
		builder.append(", ");
		if (filePath != null) {
			builder.append("path=");
			builder.append(filePath.toOSString());
			builder.append(", ");
		}
		builder.append("]");
		return builder.toString();
	}
}
