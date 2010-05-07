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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSymbolicsReader;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSymbolicsReaderFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * Factory for creating readers of symbolics in executables.
 */
public class ExecutableSymbolicsReaderFactory {
	
	private static final String SYM_EXTENSION = "sym";
	private static final String DBG_EXTENSION = "dbg";
	
	private static Map<String, IExecutableSymbolicsReaderFactory> providerMap = new HashMap<String, IExecutableSymbolicsReaderFactory>();
	
	static {
		initializeExtensions();
	}

	public static IExecutableSymbolicsReader createFor(IPath binaryFile) {
		for (Map.Entry<String, IExecutableSymbolicsReaderFactory> entry : providerMap.entrySet()) {
			String name = entry.getKey();
			IExecutableSymbolicsReaderFactory provider = entry.getValue();
			try {
				IExecutableSymbolicsReader reader = provider.createExecutableSymbolicsReader(binaryFile);
				if (reader != null)
					return reader;
			} catch (Throwable t) {
				EDCDebugger.getMessageLogger().logError("Executable reader " + name + " failed", t);
			}
		}
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
	
	protected static void initializeExtensions() {
		IConfigurationElement[] elements = 
			Platform.getExtensionRegistry().getConfigurationElementsFor(IExecutableSymbolicsReaderFactory.EXTENSION_ID);
		for (IConfigurationElement element : elements) {
			try {
				String name = element.getAttribute("name"); //$NON-NLS-1$
				IExecutableSymbolicsReaderFactory formatProvider = 
					(IExecutableSymbolicsReaderFactory) element.createExecutableExtension("class"); //$NON-NLS-1$
				providerMap.put(name, formatProvider);
			} catch (Exception e) {
				EDCDebugger.getMessageLogger().logError("Could not create executable symbolics provider extension", e);
			}
		}
	}
}
