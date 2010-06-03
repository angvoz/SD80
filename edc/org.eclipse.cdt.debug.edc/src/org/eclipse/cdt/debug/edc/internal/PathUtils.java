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
import java.io.FilenameFilter;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * These utilities handle some common portability issues when dealing with
 * (absolute) paths which may be in a format intended for another operating system.  
 * It also handles shortcomings in the org.eclipse.core.runtime.Path
 * implementation, which is not able to construct a meaningful path from
 * a Win32 path outside of Windows.
 */
public class PathUtils {

	/**
	 * Convert a variable constructed blindly for a Win32 environment into
	 * Unix-like syntax.  This is typically used for PATH or lists
	 * of paths where ';' is the entry separator and '\' is the 
	 * path component separator.
	 * <p>
	 * NOTE: we assume that the entries in the
	 * path list are already legal Unix paths, but just with the
	 * wrong slash.
	 * @param env
	 * @return converted string
	 */
	public static String convertPathListToUnix(String env) {
		if (env == null) return null;
		env = env.replaceAll(";", ":");  // entry separators
		env = env.replaceAll("\\\\", "/");  // path separators
		return env;
	}

	/**
	 * Convert a path constructed blindly for a Win32 environment into
	 * Unix-like syntax.  <p>
	 * NOTE: we assume that the path is already a legal Unix path, 
	 * but just with the wrong slash.
	 * @param file
	 * @return converted string
	 */
	public static String convertPathToUnix(String file) {
		if (file == null) return null;
		// handle Windows slashes and canonicalize
		file = file.replaceAll("\\\\", "/");
		return file;
	}

	/**
	 * Convert a path which may be in Windows or Unix format to Windows format.
	 * NOTE: we assume that the path is already a legal path, 
	 * but just with the wrong slash.
	 * @param file
	 * @return converted string
	 */
	public static String convertPathToWindows(String file) {
		if (file == null) return null;
		file = file.replaceAll("/", "\\\\");
		return file;
	}

	/**
	 * Convert a path which may be in Windows or Unix format to Windows format.
	 * NOTE: we assume that the path is already a legal path, 
	 * but just with the wrong slash.
	 * @param file
	 * @return converted string
	 */
	public static String convertPathToWindows(IPath path) {
		return convertPathToWindows(path.toPortableString());
	}

	/**
	 * Convert a path which may be in the opposite slash format to the local slash format.
	 * NOTE: we assume that the path is already a legal path, 
	 * but just with the wrong slash.
	 * @param file
	 * @return converted string
	 */
	public static String convertPathToNative(String path) {
		if (path == null) return null;
		if (HostOS.IS_UNIX)
			return path.replaceAll("\\\\", "/");
		else
			return path.replaceAll("/", "\\\\");
	}

	/**
	 * Create an IPath from a string which may be a Win32 path. <p>
	 * <p>
	 * ("new Path(...)" won't work in Unix when using a Win32 path: the backslash
	 * separator and the device notation are completely munged.)
	 * @param path
	 * @return converted string
	 */
	public static IPath createPath(String path) {
		if (path == null) return null;
		if (path.contains("\\")) {
			// handle Windows slashes and canonicalize
			path = path.replaceAll("\\\\", "/");
		}
		
		// also check for device or UNC
		int idx = path.indexOf(":");
		if (idx > 0) {
			String device = path.substring(0, idx + 1);
			path = path.substring(idx + 1);
			return new Path(path).setDevice(device);
		} 
		else {
			// Cygwin or UNC path
			if (path.startsWith("//")) {
				String network;
				idx = path.indexOf("/", 2);
				if (idx > 0) {
					network = path.substring(0, idx);
					path = path.substring(idx);
				} else {
					network = path;
					path = "";
				}
				return new Path(network, path).makeUNC(true);
			}
		}		
		
		// fallthrough
		return new Path(path);
	}

	/**
	 * Get the PATH entries from the given path environment value or the
	 * system environment.
	 * @param pathValue the expected PATH/Path value, or <code>null</code> for the system value
	 * @return array of IPath, never <code>null</code>
	 */
	public static IPath[] getPathEntries(String pathValue) {
		String pathVar = null;
		if (pathValue != null) {
			pathVar = pathValue;
		} else {
			if (HostOS.IS_WIN32) {
				// canonical name, plus fallback below
				pathVar = System.getenv("Path"); //$NON-NLS-1$
			}
			if (pathVar == null) {
				pathVar = System.getenv("PATH"); //$NON-NLS-1$
			}
		}
		
		if (pathVar == null)
			pathVar = "";
		
		String pathSeparator = System.getProperty("path.separator");
		String[] pathEntries = pathVar.split(pathSeparator);
		IPath[] paths = new IPath[pathEntries.length];
		for (int i = 0; i < pathEntries.length; i++) {
			paths[i] = new Path(pathEntries[i]);
		}
		return paths;
	}

	/**
	 * If the filesystem is case sensitive, locate the file on the filesystem 
	 * on the given path, by ignoring case sensitivity differences.  
	 * This is needed on case-preserving but not case-insensitive filesystems.
	 * @param path 
	 * @return path pointing to existing file (possibly with different case in components) or
	 * original path if there is no match
	 */
	public static IPath findExistingPathIfCaseSensitive(IPath path) {
		// case is insensitive already
		if (HostOS.IS_WIN32)
			return path;
		
		if (path == null || !path.isAbsolute())
			return path;
		
		File pathFile = path.toFile();
		if (pathFile.exists()) {
			try {
				return new Path(pathFile.getCanonicalPath());
			} catch (IOException e) {
				// should not happen
				return path;
			}
		}
			

		// start with the assumption that the path is mostly correct except for the
		// last N segments.
		IPath goodPath = Path.ROOT;
		if (path.getDevice() != null)
			goodPath = goodPath.setDevice(path.getDevice());
		
		// if bad drive or no root (?!), just skip
		if (!goodPath.toFile().exists())
			return path;
		
		for (int seg = path.segmentCount(); seg > 0; seg--) {	
			final IPath prefix = path.uptoSegment(seg - 1);

			if (prefix.toFile().exists()) {
				goodPath = prefix;
				break;
			}
		}
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(goodPath.addTrailingSeparator().toOSString());
		
		boolean failedLookup = false;
		
		for (int seg = goodPath.segmentCount(); seg < path.segmentCount(); seg++) {
			final String segment = path.segment(seg);
			
			final String[] matches = { segment };
			
			if (!failedLookup) {
				File dir = new File(builder.toString());
				if (!new File(dir, matches[0]).exists()) {
					// component has wrong case; find the first one matching case-insensitively
					String[] names = dir.list(new FilenameFilter() {
						
						public boolean accept(File dir, String name) {
							if (name.equalsIgnoreCase(segment)) {
								matches[0] = name;
								return true;
							}
							return false;
						}
					});
					
					if (names.length == 0) {
						// no matches!  the rest of the path won't match either
						failedLookup = true;
					}
				}
			}
			builder.append(matches[0]);
			builder.append('/');
		}
		
		if (!path.hasTrailingSeparator() && builder.length() > 0 && builder.charAt(builder.length() - 1) == '/') {
			builder.setLength(builder.length() - 1);
		}
		return new Path(builder.toString());
	}

	public static boolean isCaseSensitive() {
		// Is the underlying file system case sensitive?
		// This can actually be complex to determine and can even vary by volume
		// but this is an OK general test for now.
		if (HostOS.IS_UNIX)
			return true;
		return false;
	}
}
