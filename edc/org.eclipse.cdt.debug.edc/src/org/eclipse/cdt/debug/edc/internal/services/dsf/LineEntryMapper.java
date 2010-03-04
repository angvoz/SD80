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

package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.EDCAddressRange;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.symbols.ILineEntryProvider;
import org.eclipse.cdt.debug.edc.symbols.IModuleLineEntryProvider;
import org.eclipse.cdt.dsf.debug.service.IModules.AddressRange;
import org.eclipse.core.runtime.IPath;

/**
 * Utilities for mapping source to addresses and vice versa.
 */
public class LineEntryMapper {
	
	/**
	 * Get the link-time address ranges starting at the given source location.  
	 * There may be more than one, e.g., in template expansions, static functions
	 * declared in headers, inlined functions, etc.  Or, of course, someone may
	 * declare multiple functions on one line.
	 * @path sourceFile the absolute path to the source file
	 * @param line the line number (1-based) 
	 * @return the unmodifiable list of link-time addresses which start at the given line, possibly empty.
	 */
	public static Collection<AddressRange> getAddressRangesAtSource(
			IModuleLineEntryProvider moduleLineEntryProvider,
			IPath sourceFile,
			int line) {
		Collection<? extends ILineEntryProvider> fileProviders = 
			moduleLineEntryProvider.getLineEntryProvidersForFile(sourceFile);
		if (fileProviders.isEmpty())
			return Collections.emptyList();
			
		int lastColumn = -1;
		IPath lastFile = null;
		
		List<EDCAddressRange> addrRanges = null;
		for (ILineEntryProvider fileProvider : fileProviders) {
			
			Collection<ILineEntry> entries = fileProvider.getLineEntriesForLines(sourceFile, line, line);
			
			if (addrRanges == null)
				addrRanges = new ArrayList<EDCAddressRange>();
			
			for (ILineEntry entry : entries) {
				/*
				 * when there is more than one line mapping for the source line,
				 * see if it makes sense to merge the line entries into the same
				 * address range, or keep different address ranges. examples of
				 * when this might happen are when there are multiple logical
				 * code segments for the same source line, but in different
				 * columns. in this case it makes sense to merge these into one
				 * address range. for templates and inline functions however,
				 * the column will be the same. for these cases it makes sense
				 * to keep the address ranges separate.
				 */
				boolean addNewRange = true;
				if (addrRanges.size() > 0 && entry.getFilePath().equals(lastFile) && lastColumn != entry.getColumnNumber()) {
					addNewRange = false;
				}
				
				if (addNewRange) {
					EDCAddressRange range = new EDCAddressRange(entry.getLowAddress(), entry.getHighAddress());
					addrRanges.add(range);
				} else {
					EDCAddressRange range = addrRanges.remove(addrRanges.size() - 1);
					range.setEndAddress(entry.getHighAddress());
					addrRanges.add(range);
				}
				
				lastColumn = entry.getColumnNumber();
				lastFile = entry.getFilePath();
			}
		}
		
		if (addrRanges == null)
			return Collections.emptyList();
		
		List<? extends AddressRange> returnRanges = addrRanges;
		return Collections.unmodifiableCollection(returnRanges);
	}

}
