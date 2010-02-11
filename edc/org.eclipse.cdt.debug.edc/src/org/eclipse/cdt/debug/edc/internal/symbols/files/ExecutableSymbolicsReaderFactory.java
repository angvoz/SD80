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

import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.core.runtime.IPath;

/**
 * Factory for creating readers of symbolics in executables.
 * 
 * TODO: make extensions for this.
 */
public class ExecutableSymbolicsReaderFactory {
	private static final String SYM_EXTENSION = "sym";
	private static final String DBG_EXTENSION = "dbg";

	/**
	 * Create a reader for the symbolics of the given executable.
	 * @param binaryFile
	 * @return reader or <code>null</code>
	 */
	public static IExecutableSymbolicsReader createFor(IPath binaryFile) {
		IExecutableSymbolicsReader reader = null;
		
		IPath symbolFilePath = findSymbolicsFile(binaryFile);
		if (symbolFilePath != null)
			reader = detectExecutable(symbolFilePath);
		else
			reader = detectExecutable(binaryFile);
		
		return reader;
	}
	
	private static IExecutableSymbolicsReader detectExecutable(IPath binaryFile) {
		try {
			// If this constructor succeeds, it's ELF
			Elf elf = new Elf(binaryFile.toOSString());
			return new ElfExecutableSymbolicsReader(binaryFile, elf);
		} catch (IOException e) {
			// this class elides actual I/O errors with format errors; ignore
		}
		
		try {
			// If this constructor succeeds, it's PE
			PE peFile = new PE(binaryFile.toOSString());
			return new PEFileExecutableSymbolicsReader(binaryFile, peFile);
		} catch (IOException e) {
			// this class elides actual I/O errors with format errors; ignore
		}

		// add other ones here until we have extensions...
		
		return null;
	}

	/**
	 * Get a symbolics file which is associated with the given executable.
	 * @param binaryFile
	 * @return IPath or <code>null</code> if no candidate (or already looks like a sym file) 
	 */
	public static IPath findSymbolicsFile(IPath binaryFile) {

		// Check to see if there is a sym file we should use for the symbols
		//
		// Note: there may be for "foo.exe" --> "foo.exe.sym" or "foo.sym"
		//
		IPath symFile;
		symFile = binaryFile.removeFileExtension().addFileExtension(SYM_EXTENSION);
		if (symFile.toFile().exists()) 
			return symFile;
		symFile = binaryFile.removeFileExtension().addFileExtension(DBG_EXTENSION);
		if (symFile.toFile().exists()) 
			return symFile;
		
		symFile = binaryFile.addFileExtension(SYM_EXTENSION);
		if (symFile.toFile().exists())
			return symFile;
		symFile = binaryFile.addFileExtension(DBG_EXTENSION);
		if (symFile.toFile().exists())
			return symFile;
		
		return null;
	}
}
