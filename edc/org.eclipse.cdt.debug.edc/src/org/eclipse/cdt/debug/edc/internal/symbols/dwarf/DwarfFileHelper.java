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
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.edc.internal.HostOS;
import org.eclipse.cdt.debug.edc.internal.PathUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * An instance of this class per DwarfDebugInfoProvider assists in
 * canonicalizing filepaths (from DW_AT_comp_dir and DW_AT_stmt_list)
 */
public class DwarfFileHelper {

	private boolean DEBUG = false;
	private final IPath symbolFile;
	private Map<String, IPath> compDirAndNameMap;
	
	private int hits;
	private long nextDump;
	
	public DwarfFileHelper(IPath symbolFile) {
		this.symbolFile = symbolFile;
		this.compDirAndNameMap = new HashMap<String, IPath>();
	}
	
	protected String getKey(String compDir, String name) {
		return compDir + "/" + name;
	}
	
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
	public IPath normalizeFilePath(String compDir, String name) {
		if (name == null || name.length() == 0)
			return null;

		// don't count the entry "<internal>" from GCCE compiler
		if (name.charAt(0) == '<')
			return null;

		String key = getKey(compDir, name);
		
		IPath path = compDirAndNameMap.get(key);
		if (path == null) {
			path = normalizePath(compDir, name);
			compDirAndNameMap.put(key, path);
		} else {
			hits++;
		}
		if (DEBUG && System.currentTimeMillis() > nextDump) {
			System.out.println("DwarfFileHelper entries: " + compDirAndNameMap.size() + "; hits: " + hits);
			nextDump = System.currentTimeMillis() + 1000;
		}
		return path;
	}

	/**
	 * Engine for taking a DW_AT_comp_dir and a filename from DWARF
	 * and making sure it's canonical and sensible on the host.
	 * @param compDir
	 * @param name
	 * @return IPath, never <code>null</code>
	 */
	private IPath normalizePath(String compDir, String name) {

		String fullName = name;

		IPath path = PathUtils.createPath(name);

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
		
		// TODO: we need to put the EPOCROOT as a prefix, really, in this case.
		if (HostOS.IS_WIN32 && path.isAbsolute() && path.getDevice() == null) {
			IPath dirPa = new Path(compDir);   // on Win32, don't need PathUtils#createPath
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
				if (exeWinVolume != null && exeWinVolume.length() > 0) {
					path = path.setDevice(exeWinVolume);
				}
			}
		}

		// If drive is missing, use current directory (TODO: don't!) and 
		// canonicalize the path for any match on the filesystem.
		//
		if (path.isAbsolute() && (HostOS.IS_WIN32 || path.getDevice() == null)) {
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
	public IPath fixUpPath(String path) {
		/*
		 * translate cygwin drive path like /cygdrive/c/system/main.c
		 * //G/System/main.cpp
		 */
		boolean isCygwin = false;
		int deleteTill = 0;
		
		// These paths may appear in Cygwin-compiled code, so check on any host
		if (path.length() > 12 && path.startsWith("/cygdrive/") && ('/' == path.charAt(11))) { //$NON-NLS-1$
			isCygwin = true;
			deleteTill = 10;
		}

		// These paths may appear in Cygwin-compiled code, so check on any host
		if (path.length() > 4 && path.startsWith("//") && ('/' == path.charAt(3))) { //$NON-NLS-1$
			isCygwin = true;
			deleteTill = 2;
		}

		// New-style Cygwin is different and has neither prefix.  
		// But this check only makes sense on a Windows host, since
		// it may be a valid Unix-host path.
		//
		//	/C/sources/foo.c --> c:\sources\foo.c
		if (HostOS.IS_WIN32 && path.length() > 3 
				&& path.charAt(0) == '/'
				&& Character.isLetter(path.charAt(1))
				&& path.charAt(2) == '/') {
			isCygwin = true;
			deleteTill = 1;
		}
		
		if (isCygwin) {
			StringBuilder buf = new StringBuilder(path);
			buf.delete(0, deleteTill);
			buf.insert(1, ':');
			path = buf.toString();
		}

		// convert to path on runtime platform
		//
		return PathUtils.createPath(path);
	}

	/**
	 * 
	 */
	public void dispose() {
		compDirAndNameMap.clear();
	}

}
