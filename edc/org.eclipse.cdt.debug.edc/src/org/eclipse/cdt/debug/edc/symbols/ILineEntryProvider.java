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
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.runtime.IPath;

/**
 * Provides line table lookup support.
 */
public interface ILineEntryProvider {

	/**
	 * A source line and address(es) mapped to it. The address here may be
	 * runtime address or link address, depending on context in which the
	 * objects are used.
	 * 
	 * @since 2.0
	 */
	public interface ILineAddresses {
		public int getLineNumber();		// line number
		public IAddress[] getAddress();	// addresses mapped to the line 
	}

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

	/**
	 * Given a source line (let's call it anchor), find the line(s) closest to
	 * the anchor in the neighborhood (including the anchor itself) that has
	 * machine code. <br>
	 * <br>
	 * The search is done in the context of this line provider which is usually
	 * a compile unit (CU) or a module (one executable file).<br>
	 * <br>
	 * In the context of a CU, only one code line will be returned. If the
	 * closest line above the anchor and the closest line below the anchor have
	 * the same distance from the anchor, the one below will be selected.<br>
	 * <br>
	 * In the context of a module, more than one code lines may be found. For
	 * instance, one source line of an inline function in a header may have code
	 * in one CU but not in another where a neighboring code line may be found.
	 * 
	 * @param sourceFile
	 *            the file that contains the source lines in question.
	 * @param anchorLine
	 *            line number of the anchor source line.
	 * @param neighborLimit
	 *            specify the limit of the neighborhood: up to this number of
	 *            lines above the anchor and up to this number of lines below
	 *            the anchor will be checked if needed. But the check will never
	 *            go beyond the source file. When the limit is zero, no neighbor
	 *            lines will be checked. If the limit has value of -1, it means
	 *            the actual limit is the source file.
	 * 
	 * @return List of {@link ILineAddresses} objects containing link addresses.
	 *         Empty list if no code line is found.
	 * 
	 * @since 2.0
	 */
	List<ILineAddresses> findClosestLineWithCode(IPath sourceFile, int anchorLine,
			int neighborLimit);
}
