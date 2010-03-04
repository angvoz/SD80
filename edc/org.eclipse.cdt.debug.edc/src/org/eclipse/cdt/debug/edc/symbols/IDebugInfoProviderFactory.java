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

import org.eclipse.core.runtime.IPath;

/**
 * This factory creates debug info providers for files.
 */
public interface IDebugInfoProviderFactory {
	String EXTENSION_ID = "org.eclipse.cdt.debug.edc.debugInfoProviderFactory"; //$NON-NLS-1$

	/**
	 * Create a debug info provider for the given executable reader.  This can
	 * fetch information from the executable itself or reference an associated
	 * symbolics file (e.g. *.sym or *.dbg).
	 * @param binaryFile the host-side path of the binary
	 * @param reader the reader for the executable, or <code>null</code>
	 * @return a new {@link IDebugInfoProvider} or <code>null</code> if unsupported 
	 */
	IDebugInfoProvider createDebugInfoProvider(IPath binaryFile, IExecutableSymbolicsReader reader);
}
