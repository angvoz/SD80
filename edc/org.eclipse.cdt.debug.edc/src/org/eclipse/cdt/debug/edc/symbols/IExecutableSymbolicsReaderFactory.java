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
 * 
 */
public interface IExecutableSymbolicsReaderFactory {
	String EXTENSION_ID = "org.eclipse.cdt.debug.edc.executableSymbolicsReaderFactory"; //$NON-NLS-1$

	/**
	 * Create an executable symbolics reader for the given binary file.
	 * @param binaryFile
	 * @return a new {@link IExecutableSymbolicsReader} or <code>null</code> if unsupported 
	 */
	IExecutableSymbolicsReader createExecutableSymbolicsReader(IPath binaryFile);
}
