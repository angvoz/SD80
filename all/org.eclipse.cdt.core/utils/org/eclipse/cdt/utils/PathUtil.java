/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class PathUtil {
	
	public static boolean isWindowsFileSystem() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return (os != null && os.startsWith("Win")); //$NON-NLS-1$
	}
	
	public static IWorkspaceRoot getWorkspaceRoot() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace != null) {
			return workspace.getRoot();
		}
		return null;
	}
	
	public static IPath getCanonicalPath(IPath fullPath) {
		if (!fullPath.isAbsolute())
			return fullPath;
		
	    File file = fullPath.toFile();
		try {
			String canonPath = file.getCanonicalPath();
			IPath canonicalPath = new Path(canonPath);
			if (fullPath.getDevice() == null)
				canonicalPath = canonicalPath.setDevice(null);
			return canonicalPath;
		} catch (IOException ex) {
		}
		return fullPath;
	}

	public static IPath getWorkspaceRelativePath(IPath fullPath) {
		IWorkspaceRoot workspaceRoot = getWorkspaceRoot();
		if (workspaceRoot != null) {
			IPath workspaceLocation = workspaceRoot.getLocation();
			if (workspaceLocation != null && isPrefix(workspaceLocation, fullPath)) {
				int segments = matchingFirstSegments(fullPath, workspaceLocation);
				IPath relPath = fullPath.setDevice(null).removeFirstSegments(segments);
				return new Path("").addTrailingSeparator().append(relPath); //$NON-NLS-1$
			}
		}
		return fullPath;
	}
	
	public static IPath getProjectRelativePath(IPath fullPath, IProject project) {
		IPath projectPath = project.getFullPath();
		if (isPrefix(projectPath, fullPath)) {
			return fullPath.removeFirstSegments(projectPath.segmentCount());
		}
		projectPath = project.getLocation();
		if (isPrefix(projectPath, fullPath)) {
			return fullPath.removeFirstSegments(projectPath.segmentCount());
		}
		return getWorkspaceRelativePath(fullPath);
	}

	public static IPath getWorkspaceRelativePath(String fullPath) {
		return getWorkspaceRelativePath(new Path(fullPath));
	}

	public static IPath getRawLocation(IPath wsRelativePath) {
		IWorkspaceRoot workspaceRoot = getWorkspaceRoot();
		if (workspaceRoot != null && wsRelativePath != null) {
			IPath workspaceLocation = workspaceRoot.getLocation();
			if (workspaceLocation != null && !isPrefix(workspaceLocation, wsRelativePath)) {
				return workspaceLocation.append(wsRelativePath);
			}
		}
		return wsRelativePath;
	}

    public static IPath makeRelativePath(IPath path, IPath relativeTo) {
        int segments = matchingFirstSegments(relativeTo, path);
        if (segments > 0) {
            IPath prefix = relativeTo.removeFirstSegments(segments);
            IPath suffix = path.removeFirstSegments(segments);
            IPath relativePath = new Path(""); //$NON-NLS-1$
            for (int i = 0; i < prefix.segmentCount(); ++i) {
                relativePath = relativePath.append(".." + IPath.SEPARATOR); //$NON-NLS-1$
            }
            return relativePath.append(suffix);
        }
        return null;
    }

    public static IPath makeRelativePathToProjectIncludes(IPath fullPath, IProject project) {
        IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
        if (provider != null) {
            IScannerInfo info = provider.getScannerInformation(project);
            if (info != null) {
                return makeRelativePathToIncludes(fullPath, info.getIncludePaths());
            }
        }
        return null;
    }
    
    public static IPath makeRelativePathToIncludes(IPath fullPath, String[] includePaths) {
        IPath relativePath = null;
        int mostSegments = 0;
        for (int i = 0; i < includePaths.length; ++i) {
            IPath includePath = new Path(includePaths[i]);
            if (isPrefix(includePath, fullPath)) {
                int segments = includePath.segmentCount();
                if (segments > mostSegments) {
                    relativePath = fullPath.removeFirstSegments(segments).setDevice(null);
                    mostSegments = segments;
                }
            }
        }
        return relativePath;
    }

    public static IProject getEnclosingProject(IPath fullPath) {
		IWorkspaceRoot root = getWorkspaceRoot();
		if (root != null) {
			IPath path = getWorkspaceRelativePath(fullPath);
			while (path.segmentCount() > 0) {
				IResource res = root.findMember(path);
				if (res != null)
				    return res.getProject();

				path = path.removeLastSegments(1);
			}
		}
		return null;
    }
    
    public static IPath getValidEnclosingFolder(IPath fullPath) {
		IWorkspaceRoot root = getWorkspaceRoot();
		if (root != null) {
			IPath path = getWorkspaceRelativePath(fullPath);
			while (path.segmentCount() > 0) {
				IResource res = root.findMember(path);
				if (res != null && res.exists() && (res.getType() == IResource.PROJECT || res.getType() == IResource.FOLDER))
				    return path;

				path = path.removeLastSegments(1);
			}
		}
		return null;
	}

    /**
	 * Checks whether path1 is the same as path2.
	 * @return <code>true</code> if path1 is the same as path2, and <code>false</code> otherwise
     * 
     * Similar to IPath.equals(Object obj), but takes case sensitivity of the file system
     * into account.
     * @since 5.1
     */
	public boolean equal(IPath path1, IPath path2) {
		// Check leading separators
		if (path1.isAbsolute() != path2.isAbsolute() || path1.isUNC() != path2.isUNC()) {
			return false;
		}
		int i = path1.segmentCount();
		// Check segment count
		if (i != path2.segmentCount())
			return false;
		// Check segments in reverse order - later segments more likely to differ
		while (--i >= 0) {
			if (!path1.segment(i).equals(path2.segment(i)))
				return false;
		}
		// Check device last (least likely to differ)
		if (path1.getDevice() == null) {
			return path2.getDevice() == null;
		} else {
			return path1.getDevice().equalsIgnoreCase(path2.getDevice());
		}
	}

    /**
	 * Checks whether path1 is a prefix of path2. To be a prefix, path1's segments
	 * must appear in path1 in the same order, and their device ids must match.
	 * <p>
	 * An empty path is a prefix of all paths with the same device; a root path is a prefix of 
	 * all absolute paths with the same device.
	 * </p>
	 * @return <code>true</code> if path1 is a prefix of path2, and <code>false</code> otherwise
     * 
     * Similar to IPath.isPrefixOf(IPath anotherPath), but takes case sensitivity of the file system
     * into account. 
     * @since 5.1
     */
	public static boolean isPrefix(IPath path1, IPath path2) {
		if (path1.getDevice() == null) {
			if (path2.getDevice() != null) {
				return false;
			}
		} else {
			if (!path1.getDevice().equalsIgnoreCase(path2.getDevice())) {
				return false;
			}
		}
		if (path1.isEmpty() || (path1.isRoot() && path2.isAbsolute())) {
			return true;
		}
		int len1 = path1.segmentCount();
		if (len1 > path2.segmentCount()) {
			return false;
		}
		boolean caseSensitive = !isWindowsFileSystem();
		for (int i = 0; i < len1; i++) {
			if (!(caseSensitive ?
					path1.segment(i).equals(path2.segment(i)) :
					path1.segment(i).equalsIgnoreCase(path2.segment(i)))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the number of segments which match in path1 and path2
	 * (device ids are ignored), comparing in increasing segment number order.
	 *
	 * @return the number of matching segments

	 * Similar to IPath.matchingFirstSegments(IPath anotherPath), but takes case sensitivity
	 * of the file system into account.
     * @since 5.1
	 */
	public static int matchingFirstSegments(IPath path1, IPath path2) {
		Assert.isNotNull(path1);
		Assert.isNotNull(path2);
		int len1 = path1.segmentCount();
		int len2 = path2.segmentCount();
		int max = Math.min(len1, len2);
		int count = 0;
		boolean caseSensitive = !isWindowsFileSystem();
		for (int i = 0; i < max; i++) {
			if (!(caseSensitive ?
					path1.segment(i).equals(path2.segment(i)) :
					path1.segment(i).equalsIgnoreCase(path2.segment(i)))) {
				return count;
			}
			count++;
		}
		return count;
	}
}
