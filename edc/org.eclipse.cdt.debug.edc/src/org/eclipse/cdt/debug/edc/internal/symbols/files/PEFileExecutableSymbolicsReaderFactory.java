/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols.files;

import java.io.IOException;

import org.eclipse.cdt.debug.edc.symbols.IExecutableSymbolicsReader;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSymbolicsReaderFactory;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.core.runtime.IPath;

/**
 * Factory for creating readers of symbolics in executables.
 */
public class PEFileExecutableSymbolicsReaderFactory implements IExecutableSymbolicsReaderFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSymbolicsReaderFactory#getConfidence(org.eclipse.core.runtime.IPath)
	 */
	public int getConfidence(IPath binaryFile) {
		try {
			// If this constructor succeeds, it's PE
			@SuppressWarnings("unused")
			PE peFile = new PE(binaryFile.toOSString());
			return IExecutableSymbolicsReaderFactory.NORMAL_CONFIDENCE;
		} catch (IOException e) {
			// this class elides actual I/O errors with format errors; ignore
		}
		return IExecutableSymbolicsReaderFactory.NO_CONFIDENCE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSymbolicsReaderFactory#createExecutableSymbolicsReader(org.eclipse.core.runtime.IPath)
	 */
	public IExecutableSymbolicsReader createExecutableSymbolicsReader(
			IPath binaryFile) {
		
		try {
			// If this constructor succeeds, it's PE
			PE peFile = new PE(binaryFile.toOSString());
			return new PEFileExecutableSymbolicsReader(binaryFile, peFile);
		} catch (IOException e) {
			// this class elides actual I/O errors with format errors; ignore
		}
		return null;
	}

}
