/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class DwarfHelper {

	private static final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win"); //$NON-NLS-1$

	/**
	 * This is to combine the given compDir and file name from Dwarf to form a
	 * file path. Some processing is done including <br>
	 * -- extra path delimiter are removed. <br>
	 * -- "a/b/" and "../c/d" are combined to "/a/c/d". <br>
	 * -- change path delimiter to native, namely on Windows "/" => "\\" and the
	 * opposite on unix/linux. -- special cygwin paths like "/cygdrive/c/f.c"
	 * are converted to standard path like "c:/f.c" <br>
	 * 
	 * @param compDir
	 *            compDir from dwarf data.
	 * @param name
	 *            file name from dwarf data with full or partial or no path.
	 * @return most complete file path that we can get from Dwarf data, which
	 *         may still be a partial path. Note letter case of the names
	 *         remains unchanged. Empty string is returned if the given name is
	 *         not invalid file name, e.g. <internal>.
	 * 
	 */
	public static IPath normalizeFilePath(String compDir, String name, IPath symbolFile) {
		// TODO make this faster
		if (name == null || name.length() == 0)
			return null;

		// don't count the entry "<internal>" from GCCE compiler
		if (name.charAt(0) == '<')
			return null;

		String fullName = name;

		IPath path = new Path(name);

		// Combine dir & name if needed.
		if (!path.isAbsolute() && compDir.length() > 0) {
			fullName = compDir;
			if (!compDir.endsWith(File.separator))
				fullName += File.separatorChar;
			fullName += name;
		}

		// some fix-up like cygwin style path conversion.
		path = fixUpPath(fullName);

		// For win32 only.
		// On Windows, there are cases where the source file itself has the full
		// path except the drive letter.
		if (isWindows && path.isAbsolute() && path.getDevice() == null) {
			IPath dirPa = new Path(compDir);
			// Try to get drive letter from comp_dir.
			if (dirPa.getDevice() != null)
				path = path.setDevice(dirPa.getDevice());
			else {
				// No drive from Dwarf data, which is also possible with RVCT or
				// GCCE compilers for ARM. A practically good solution is to
				// assume
				// drive of the exe file as the drive. Though it's not good in
				// theory, it does not hurt when the assumption is wrong, as
				// user still
				// has the option to locate the file manually...03/15/07
				String exeWinVolume = symbolFile.getDevice();
				if (exeWinVolume.length() > 0) {
					path = path.setDevice(exeWinVolume);
				}
			}
		}

		if (path.isAbsolute()) {
			try {
				path = new Path(path.toFile().getCanonicalPath());
			} catch (IOException e) {
			}
		}

		return path;
	}

	/**
	 * Convert cygwin path and change path delimiter to native.
	 * 
	 * @param path
	 * @return
	 */
	public static IPath fixUpPath(String path) {
		// TODO make this faster

		/*
		 * translate cygwin drive path like /cygdrive/c/system/main.c
		 * //G/System/main.cpp
		 */
		boolean isCygwin = false;
		int deleteTill = 0;
		if (path.length() > 12 && path.startsWith("/cygdrive/") && ('/' == path.charAt(11))) { //$NON-NLS-1$
			isCygwin = true;
			deleteTill = 10;
		}

		if (path.length() > 4 && path.startsWith("//") && ('/' == path.charAt(3))) { //$NON-NLS-1$
			isCygwin = true;
			deleteTill = 2;
		}

		if (isCygwin) {
			StringBuffer buf = new StringBuffer(path);
			buf.delete(0, deleteTill);
			buf.insert(1, ':');
			path = buf.toString();
		}

		// convert to path on runtime platform
		//
		if (isWindows)
			path = path.replaceAll("/", "\\\\"); //$NON-NLS-1$//$NON-NLS-2$
		else
			path = path.replaceAll("\\\\", "/");

		return new Path(path);
	}

	/**
	 * Read a null-ended string from the given "data" stream. data : IN, byte
	 * buffer
	 */
	public static String readString(ByteBuffer data) {
		// TODO make this faster
		String str;

		StringBuffer sb = new StringBuffer();
		while (data.hasRemaining()) {
			byte c = data.get();
			if (c == 0) {
				break;
			}
			sb.append((char) c);
		}

		str = sb.toString();
		return str;
	}

}
