/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal;

import java.io.File;
import java.util.Collection;

import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;

public class ExecutablesSourceContainer extends AbstractSourceContainer {

	public static final String TYPE_ID = EDCDebugger.getUniqueIdentifier() + ".containerType.executables";	 //$NON-NLS-1$

	public Object[] findSourceElements(String name) throws CoreException {
		IPath path = PathUtils.findExistingPathIfCaseSensitive(PathUtils.createPath(name));
		// Now looking for the file in executable view.
		//
		// Between the SDK and target, the exact directory and file capitalization may differ.
		//
		// Inject required initial slash so we can confidently use String#endsWith() without
		// matching, e.g. "/path/to/program.exe" with "ram.exe".
		//
		String slashAndLowerFileName = File.separator + path.lastSegment().toLowerCase();
		String absoluteLowerPath = path.makeAbsolute().toOSString().toLowerCase();
		
		// Note the 'wait=true' argument.  We can wait now that this job does not lock the UI.  
		Collection<Executable> executables = ExecutablesManager.getExecutablesManager().getExecutables(true);
		for (Executable e : executables) {
			String p = e.getPath().makeAbsolute().toOSString().toLowerCase();
			if (p.endsWith(absoluteLowerPath) || // stricter match first
				p.endsWith(slashAndLowerFileName)) // then only check by name
			{
				return new LocalFileStorage[] { new LocalFileStorage(e.getPath().toFile()) };
			}
		}
		return new Object[]{};
	}

	public String getName() {
		return "Executables";
	}

	public ISourceContainerType getType() {
		return getSourceContainerType( TYPE_ID );
	}

}
