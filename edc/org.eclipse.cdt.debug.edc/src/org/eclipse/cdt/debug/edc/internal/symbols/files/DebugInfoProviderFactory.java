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

import org.eclipse.cdt.debug.edc.internal.symbols.newdwarf.DwarfDebugInfoProvider;

/**
 * Factory for creating debug symbolics providers from executables.
 * 
 * TODO: add extensions for this
 */
public class DebugInfoProviderFactory {
	public static IDebugInfoProvider createFor(IExecutableSymbolicsReader reader) {
		if (DwarfDebugInfoProvider.isDebugInfoDetected(reader)) {
			return new DwarfDebugInfoProvider(reader);
		}
		
		// add other ones here until we have extensions...

		return null;
	}
}
