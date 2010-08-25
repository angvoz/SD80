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

public interface IExecutableSymbolicsReaderFactory {

	String EXTENSION_ID = "org.eclipse.cdt.debug.edc.executableSymbolicsReaderFactory"; //$NON-NLS-1$

	/**
	 * @since 2.0
	 */
	static final int NO_CONFIDENCE = 0;
	/**
	 * @since 2.0
	 */
	static final int LOW_CONFIDENCE = 25;
	/**
	 * @since 2.0
	 */
	static final int NORMAL_CONFIDENCE = 50;
	/**
	 * @since 2.0
	 */
	static final int HIGH_CONFIDENCE = 75;

	/**
	 * Used to help determine if this factory can read the given binary file.  If so,
	 * the confidence returned is used to help determine which factory to use if more
	 * than one can read this file.
	 * @param binaryFile
	 * @return NO_CONFIDENCE if it cannot read the binary, otherwise one of LOW_CONFIDENCE,
	 * NORMAL_CONFIDENCE or HIGH_CONFIDENCE.
	 * @since 2.0
	 */
	int getConfidence(IPath binaryFile);

	/**
	 * Create an executable symbolics reader for the given binary file.
	 * @param binaryFile
	 * @return a new {@link IExecutableSymbolicsReader} or <code>null</code> if unsupported 
	 */
	IExecutableSymbolicsReader createExecutableSymbolicsReader(IPath binaryFile);
}
