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
package org.eclipse.cdt.debug.edc.internal;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


/**
 * Utilities used for portability between hosts.
 */
public class HostOS {
	/** Is the host Windows? */
	public static boolean IS_WIN32 = File.separatorChar == '\\';
	/** Is the host some Unix variant? */
	public static boolean IS_UNIX = File.separatorChar == '/';
	/** Executable file extension */
	public static final String EXE_EXT = IS_WIN32 ? ".exe" : "";
	
	/**
	 * Ensure that the executable name mentioned is canonical for the machine.
	 * This only affects Windows, currently, ensuring that an ".exe" is attached.
	 * @param executablePath
	 * @return updated path
	 */
	public static String canonicalizeExecutableName(String executable) {
		if (IS_WIN32) {
			IPath executablePath = new Path(executable);
			String ext = executablePath.getFileExtension();
			if (ext == null) {
				executable += EXE_EXT;
			}
		}
		return executable;
	}

	/**
	 * Scan the PATH variable and see if the given binary is visible on
	 * the PATH that will be used at runtime (with the default environment and overrides).
	 * @param pathValue the expected Path 
	 * @param program
	 * @return IPath if program is on PATH, else <code>null</code>
	 */
	public static IPath findProgramOnPath(String program, String pathValue) {
		
		// be sure proper path/extension are present
		program = HostOS.canonicalizeExecutableName(program);
		
		IPath path = null;
		
		IPath[] pathEntries = PathUtils.getPathEntries(pathValue);
		for (IPath pathEntry : pathEntries) {
			IPath testPath = pathEntry.append(program);
			if (testPath.toFile().exists()) {
				path = testPath;
				break;
			}
		}
		
		return path;
	}
}
