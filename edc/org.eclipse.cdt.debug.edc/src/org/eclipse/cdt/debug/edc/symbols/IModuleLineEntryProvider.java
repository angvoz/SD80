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

package org.eclipse.cdt.debug.edc.symbols;

import java.util.Collection;

import org.eclipse.core.runtime.IPath;

/**
 * Added functionality for finding line information across a module.
 */
public interface IModuleLineEntryProvider extends ILineEntryProvider {
	/**
	 * Get the line entry providers for the given source file.  
	 * @path sourceFile the absolute path to the source file
	 * @return the unmodifiable list of providers for the file, possibly empty.
	 */
	Collection<ILineEntryProvider> getLineEntryProvidersForFile(IPath sourceFile);

	/**
	 * Check if the module uses the given source file.<br>
	 * Note that line table has more complete list of files (esp. headers) used
	 * in a compile unit than debug_info section of Dwarf.
	 * 
	 * @since 2.0
	 */
	boolean hasSourceFile(IPath sourceFile);
}
