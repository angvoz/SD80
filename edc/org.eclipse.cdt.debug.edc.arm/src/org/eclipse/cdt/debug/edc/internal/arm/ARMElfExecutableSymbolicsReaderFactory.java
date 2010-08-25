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
package org.eclipse.cdt.debug.edc.internal.arm;

import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.debug.edc.internal.symbols.elf.BufferedRandomReadAccessFile;
import org.eclipse.cdt.debug.edc.internal.symbols.elf.Elf;
import org.eclipse.cdt.debug.edc.internal.symbols.files.ExecutableSymbolicsReaderFactory;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSymbolicsReader;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSymbolicsReaderFactory;
import org.eclipse.cdt.utils.elf.Elf.ELFhdr;
import org.eclipse.core.runtime.IPath;

/**
 * Factory for creating readers of symbolics in executables.
 */
public class ARMElfExecutableSymbolicsReaderFactory implements IExecutableSymbolicsReaderFactory {
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSymbolicsReaderFactory#getConfidence(org.eclipse.core.runtime.IPath)
	 */
	public int getConfidence(IPath binaryFile) {
		Elf elfFile = getARMElfFile(binaryFile);
		if (elfFile == null) {
			// treat the symbol file as an executable, if existing
			IPath symbolFilePath = ExecutableSymbolicsReaderFactory.findSymbolicsFile(binaryFile);
			if (symbolFilePath != null) {
				elfFile = getARMElfFile(symbolFilePath);
			}
		}
		
		return elfFile != null ? IExecutableSymbolicsReaderFactory.HIGH_CONFIDENCE :
			IExecutableSymbolicsReaderFactory.NO_CONFIDENCE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.files.IExecutableSymbolicsReaderFactory#createExecutableSymbolicsReader(org.eclipse.core.runtime.IPath)
	 */
	public IExecutableSymbolicsReader createExecutableSymbolicsReader(
			IPath binaryFile) {

		IExecutableSymbolicsReader reader = detectExecutable(binaryFile);
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
			Elf elfFile = getARMElfFile(binaryFile);
			if (elfFile != null) {
				return new ARMElfExecutableSymbolicsReader(binaryFile, elfFile);
			}
		} catch (IOException e) {
			// this class elides actual I/O errors with format errors; ignore
		}
		return null;
	}
	
	private Elf getARMElfFile(IPath binaryFile) {
		try {
			// quickly check the endianness (Elf repeats this)
			FileInputStream fis = new FileInputStream(binaryFile.toOSString());
			byte[] e_ident = new byte[16];
			fis.read(e_ident);
			if (e_ident[ELFhdr.EI_MAG0] != 0x7f || e_ident[ELFhdr.EI_MAG1] != 'E' || e_ident[ELFhdr.EI_MAG2] != 'L'
					|| e_ident[ELFhdr.EI_MAG3] != 'F')
				throw new IOException(CCorePlugin.getResourceString("Util.exception.notELF")); //$NON-NLS-1$
			
			boolean isle = (e_ident[ELFhdr.EI_DATA] == ELFhdr.ELFDATA2LSB);
			
			// If this constructor succeeds, it's ELF
			Elf elf = new Elf(new BufferedRandomReadAccessFile(binaryFile.toOSString(), isle), 
					binaryFile.toOSString(), 0);
			
			if (elf.getAttributes().getCPU().compareToIgnoreCase("arm") == 0) {
				return elf;
			}
		} catch (IOException e) {
			// this class elides actual I/O errors with format errors; ignore
		}

		return null;
	}

}
