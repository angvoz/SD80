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

import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.core.runtime.IPath;

/**
 * Factory for creating readers of symbolics in executables.
 */
public class ElfExecutableSymbolicsReaderFactory implements IExecutableSymbolicsReaderFactory {
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSymbolicsReaderFactory#createExecutableSymbolicsReader(org.eclipse.core.runtime.IPath)
	 */
	public IExecutableSymbolicsReader createExecutableSymbolicsReader(
			IPath binaryFile) {
		IExecutableSymbolicsReader reader = null;

		reader = detectExecutable(binaryFile);
		if (reader == null) {
			// treat the symbol file as an executable, if existing
			IPath symbolFilePath = ExecutableSymbolicsReaderFactory.findSymbolicsFile(binaryFile);
			if (symbolFilePath != null) {
				reader = detectExecutable(symbolFilePath);
			}
		}
		
		return reader;
	}
	
	private IExecutableSymbolicsReader detectExecutable(IPath binaryFile) {
		try {
			// If this constructor succeeds, it's ELF
			Elf elf = new Elf(binaryFile.toOSString());
			return new ElfExecutableSymbolicsReader(binaryFile, elf);
		} catch (IOException e) {
			// this class elides actual I/O errors with format errors; ignore
			return null;
		}
	}
}
