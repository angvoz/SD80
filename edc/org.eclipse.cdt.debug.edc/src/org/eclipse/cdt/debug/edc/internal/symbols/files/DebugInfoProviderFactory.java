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
import org.eclipse.cdt.debug.edc.symbols.IDebugInfoProvider;
import org.eclipse.cdt.debug.edc.symbols.IDebugInfoProviderFactory;
import org.eclipse.cdt.debug.edc.symbols.IExecutableSymbolicsReader;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * Factory for creating debug symbolics providers from executables.
 */
public class DebugInfoProviderFactory {
	private static Map<String, IDebugInfoProviderFactory> providerMap = new HashMap<String, IDebugInfoProviderFactory>();
	
	static {
		initializeExtensions();
	}
	
	/**
	 * Create a debug info provider for the given binary (usually the executable being
	 * debugged).  It's up to a {@link IDebugInfoProviderFactory}
	 * implementation to determine how it maps a binary to a symbolics file. 
	 * @param binaryPath path to a host file 
	 * @param exeReader the reader for that file, or <code>null</code> 
	 * @return {@link IDebugInfoProvider} or <code>null</code> if nothing supports it
	 */
	public static IDebugInfoProvider createFor(IPath binaryPath, IExecutableSymbolicsReader exeReader) {
		for (Map.Entry<String, IDebugInfoProviderFactory> entry: providerMap.entrySet()) {
			String name = entry.getKey();
			IDebugInfoProviderFactory providerProvider = entry.getValue();
			try {
				IDebugInfoProvider provider = providerProvider.createDebugInfoProvider(binaryPath, exeReader);
				if (provider != null)
					return provider;
			} catch (Throwable t) {
				EDCDebugger.getMessageLogger().logError("Debug info reader " + name + " failed", t);
			}
		}
		return null;
	}
	
	protected static void initializeExtensions() {
		IConfigurationElement[] elements = 
			Platform.getExtensionRegistry().getConfigurationElementsFor(IDebugInfoProviderFactory.EXTENSION_ID);
		for (IConfigurationElement element : elements) {
			try {
				String name = element.getAttribute("name"); //$NON-NLS-1$
				IDebugInfoProviderFactory formatProvider = 
					(IDebugInfoProviderFactory) element.createExecutableExtension("class"); //$NON-NLS-1$
				providerMap.put(name, formatProvider);
			} catch (Exception e) {
				EDCDebugger.getMessageLogger().logError("Could not create executable symbolics provider extension", e);
			}
		}
	}
}
