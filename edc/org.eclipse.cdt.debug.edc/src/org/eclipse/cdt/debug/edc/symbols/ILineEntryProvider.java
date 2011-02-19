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
package org.eclipse.cdt.debug.edc.symbols;

import java.util.Collection;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.runtime.IPath;

/**
 * Provides line table lookup support.
 */
public interface ILineEntryProvider {

	/**
	 * Get the line table entry for the given link address
	 * 
	 * @param linkAddress
	 *            the link address
	 * @return the line table entry, or null if none found
	 */
	ILineEntry getLineEntryAtAddress(IAddress linkAddress);

	/**
	 * Get the list of line table entries for the given sequence of line
	 * numbers. startLineNumber and endLineNumber can be the same if you're only
	 * interested in one source line. 
	 * <p>
	 * Note that there can be multiple line entries for a single source line,
	 * due to multiple statements per line, template expansion, static functions
	 * in headers, etc. 
	 * 
	 * @param file
	 *            the file to examine (source or header); as a full path.
	 * @param startLineNumber
	 *            the first line number
	 * @param endLineNumber
	 *            the last line number. if -1, the line entries for the
	 *            remainder of the file will be returned
	 * @return unmodifiable list of line entries, which may be empty
	 */
	Collection<ILineEntry> getLineEntriesForLines(IPath file, int startLineNumber, int endLineNumber);

	/**
	 * Gets the next line table entry in the same scope by line number that also
	 * has a higher address (useful for source level stepping)
	 * 
	 * @param entry
	 *            the current entry
	 * @param collapseInlineFunctions
	 * 			  treat inline code as though it were a function to be stepped over
	 * @return the next entry, or null if none
	 * @since 2.0
	 */
	ILineEntry getNextLineEntry(ILineEntry entry, boolean collapseInlineFunctions);

	/**
	 * Gets the previous line table entry in the same scope by line number that also
	 * has a lower address (useful for source level stepping)
	 * 
	 * @param entry
	 *            the current entry
	 * @param collapseInlineFunctions
	 * 			  treat inline code as though it were a function to be stepped over
	 * @return the next entry, or null if none
	 * @since 2.0
	 */
	ILineEntry getPreviousLineEntry(ILineEntry entry, boolean collapseInlineFunctions);

	/**
	 * Gets the line entry for the given link address within a given function.
	 * (Useful for inline stepping and stack-crawl function name determination.)
	 * 
	 * @param linkAddress
	 * @param parentFunction
	 * @return
	 * @since 2.0
	 */
	public ILineEntry getLineEntryInFunction(IAddress linkAddress, IFunctionScope parentFunction);
}
