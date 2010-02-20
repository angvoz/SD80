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

package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import org.eclipse.cdt.debug.edc.internal.symbols.files.ExecutableSymbolicsReaderFactory;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IDebugInfoProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IDebugInfoProviderFactory;
import org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSymbolicsReader;
import org.eclipse.core.runtime.IPath;

/**
 * 
 */
public class DwarfDebugInfoProviderFactory implements
		IDebugInfoProviderFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IDebugInfoProviderFactory#createDebugInfoProvider(org.eclipse.core.runtime.IPath)
	 */
	public IDebugInfoProvider createDebugInfoProvider(IPath binaryPath, IExecutableSymbolicsReader exeReader) {
		// DWARF info lives either in the executable itself or in an associated symbol file.
		if (exeReader != null) {
			if (exeReader.findExecutableSection(DwarfInfoReader.DWARF_DEBUG_INFO) != null) {
				return new DwarfDebugInfoProvider(exeReader);
			}
		}
		
		// else, look alongside for a *.sym or *.dbg file
		IPath symFile = ExecutableSymbolicsReaderFactory.findSymbolicsFile(binaryPath);
		if (symFile != null) {
			IExecutableSymbolicsReader symExeReader = ExecutableSymbolicsReaderFactory.createFor(symFile);
			if (symExeReader != null) {
				return new DwarfDebugInfoProvider(symExeReader);
			}
		}
		
		// no one has DWARF
		return null;
	}

}
