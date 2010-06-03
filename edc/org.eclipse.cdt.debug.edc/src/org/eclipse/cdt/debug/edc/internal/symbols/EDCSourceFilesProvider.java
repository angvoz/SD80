/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols;

import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ISourceFilesProvider;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.core.runtime.IProgressMonitor;

public class EDCSourceFilesProvider implements ISourceFilesProvider {

	public String[] getSourceFiles(Executable executable, IProgressMonitor monitor) {

		try {
			// get cached reader
			IEDCSymbolReader reader = Symbols.getSymbolReader(executable.getPath());
			if (reader != null) {
				// note: don't dispose reader here
				return reader.getSourceFiles(monitor);
			}
		} catch (Exception e) {
		}

		return new String[0];
	}

	public int getPriority(Executable executable) {
		// this forces us to be called before the DE source files provider
		return ISourceFilesProvider.HIGH_PRIORITY + 10;
	}
}
