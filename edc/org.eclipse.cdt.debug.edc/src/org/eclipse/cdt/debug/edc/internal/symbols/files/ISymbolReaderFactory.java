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

import org.eclipse.cdt.debug.edc.internal.symbols.IEDCSymbolReader;
import org.eclipse.core.runtime.IPath;

/**
 * This interface detects a specific executable format and yields a symbol
 * reader for it.
 */
public interface ISymbolReaderFactory {
	/**
	 * Create a symbol reader for the given executable.  The symbols do
	 * not have to live in the provided executable, but may be in associated files (e.g. exeFile + .sym).
	 * @param exeFile start point for searching
	 * @return a symbol reader, or <code>null</code> if not compatible
	 * @throws IOException if the reader fails to handle the file after detecting its type
	 */
	IEDCSymbolReader createSymbolReader(IPath exeFile) throws IOException;
}
