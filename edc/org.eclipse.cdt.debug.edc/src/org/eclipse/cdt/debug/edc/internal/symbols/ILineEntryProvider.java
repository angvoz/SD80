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

import java.util.Collection;

import org.eclipse.cdt.core.IAddress;

/**
 * Provides line table information
 */
public interface ILineEntryProvider {

	/**
	 * Get the list of line table entries for the entire compile unit
	 * 
	 * @return unmodifiable list of line entries, which may be empty
	 */
	Collection<ILineEntry> getLineEntries();

	/**
	 * Get the line table entry for the given link address
	 * 
	 * @param linkAddress
	 *            the link address
	 * @return the line table entry, or null if none found
	 */
	ILineEntry getLineEntryAtAddress(IAddress linkAddress);

	/**
	 * Get the list of line table entries that for the given sequence of line
	 * numbers. startLineNumber and endLineNumber can be the same if you're only
	 * interesting in one source line. Note that there can be multiple line
	 * entries for a single source line.
	 * 
	 * @param startLineNumber
	 *            the first line number
	 * @param endLineNumber
	 *            the last line number. if -1, the line entries for the
	 *            remainder of the file will be returned
	 * @return unmodifiable list of line entries, which may be empty
	 */
	Collection<ILineEntry> getLineEntriesForLines(int startLineNumber, int endLineNumber);

	/**
	 * Gets the next line table entry in the same scope by line number that also
	 * has a higher address (useful for source level stepping)
	 * 
	 * @param entry
	 *            the current entry
	 * @return the next entry, or null if none
	 */
	ILineEntry getNextLineEntry(ILineEntry entry);

}
