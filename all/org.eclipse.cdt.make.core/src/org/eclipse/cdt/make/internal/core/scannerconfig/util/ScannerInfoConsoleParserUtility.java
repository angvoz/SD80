/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParserUtility;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Implements error reporting mechanism and file/path translation mechanism
 * Taken from ErrorParserManager and modified.
 * 
 * @author vhirsl
 */
public class ScannerInfoConsoleParserUtility implements IScannerInfoConsoleParserUtility {
	private IProject fProject;
	private IPath fBaseDirectory;
	private IMarkerGenerator fMarkerGenerator;
	private ArrayList fErrors;

	/*
	 * For tracking the location of files being compiled
	 */
	private Map fFilesInProject;
	private List fCollectedFiles;
	private List fNameConflicts;
	private Vector fDirectoryStack;
	
	public ScannerInfoConsoleParserUtility(IProject project, IPath workingDirectory, IMarkerGenerator markerGenerator) {
		fProject = project;
		fMarkerGenerator = markerGenerator;
		fBaseDirectory = fProject.getLocation();
		fErrors = new ArrayList();

		fFilesInProject = new HashMap();
		fCollectedFiles = new ArrayList();
		fNameConflicts = new ArrayList();
		fDirectoryStack = new Vector();

		collectFiles(fProject, fCollectedFiles);

		for (int i = 0; i < fCollectedFiles.size(); i++) {
			IFile curr = (IFile) fCollectedFiles.get(i);
			Object existing = fFilesInProject.put(curr.getName(), curr);
			if (existing != null) {
				fNameConflicts.add(curr.getName());
			}
		}
		if (workingDirectory != null) {
			pushDirectory(workingDirectory);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParserUtility#reportProblems()
	 */
	public boolean reportProblems() {
		boolean reset = false;
		for (Iterator iter = fErrors.iterator(); iter.hasNext(); ) {
			Problem problem = (Problem) iter.next();
			if (problem.severity == IMarkerGenerator.SEVERITY_ERROR_BUILD) {
				reset = true;
			}
			if (problem.file == null) {
				fMarkerGenerator.addMarker(
					fProject,
					problem.lineNumber,
					problem.description,
					problem.severity,
					problem.variableName);
			} else {
				fMarkerGenerator.addMarker(
					problem.file,
					problem.lineNumber,
					problem.description,
					problem.severity,
					problem.variableName);
			}
		}
		fErrors.clear();
		return reset;
	}

	protected class Problem {
		protected IResource file;
		protected int lineNumber;
		protected String description;
		protected int severity;
		protected String variableName;

		public Problem(IResource file, int lineNumber, String desciption, int severity, String variableName) {
			this.file = file;
			this.lineNumber = lineNumber;
			this.description = desciption;
			this.severity = severity;
			this.variableName = variableName;
		}
	}

	/**
	 * Called by the console line parsers to generate a problem marker.
	 */
	public void generateMarker(IResource file, int lineNumber, String desc, int severity, String varName) {
		Problem problem = new Problem(file, lineNumber, desc, severity, varName);
		fErrors.add(problem);
	}

	/**
	 * Called by the console line parsers to find a file with a given name.
	 * @param fileName
	 * @return IFile or null
	 */
	public IFile findFile(String fileName) {
		IFile file = findFilePath(fileName);
		if (file == null) {
			// Try the project's map.
			file = findFileName(fileName);
			if (file != null) {
				// If there is a conflict then try all files in the project.
				if (isConflictingName(fileName)) {
					file = null;
					// Create a problem marker
					generateMarker(fProject, -1, "Ambiguous file path: "+fileName,	//$NON-NLS-1$
							IMarkerGenerator.SEVERITY_ERROR_RESOURCE, null);				
				}
			}
		}
		return file;
	}
	
	/**
	 * @param filePath
	 * @return
	 */
	protected IFile findFilePath(String filePath) {
		IPath path = null;
		IPath fp = new Path(filePath);
		if (fp.isAbsolute()) {
			if (fBaseDirectory.isPrefixOf(fp)) {
				int segments = fBaseDirectory.matchingFirstSegments(fp);
				path = fp.removeFirstSegments(segments);
			} else {
				path = fp;
			}
		} else {
			path = (IPath) getWorkingDirectory().append(filePath);
		}

		IFile file = null;
		// The workspace may throw an IllegalArgumentException
		// Catch it and the parser should fallback to scan the entire project.
		try {
			file = findFileInWorkspace(path);
		} catch (Exception e) {
		}

		// We have to do another try, on Windows for cases like "TEST.C" vs "test.c"
		// We use the java.io.File canonical path.
		if (file == null || !file.exists()) {
			File f = path.toFile();
			try {
				String canon = f.getCanonicalPath();
				path = new Path(canon);
				file = findFileInWorkspace(path);
			} catch (IOException e1) {
			}
		}
		return (file != null && file.exists()) ? file : null;
	}

	/**
	 * @param fileName
	 * @return
	 */
	protected IFile findFileName(String fileName) {
		IPath path = new Path(fileName);
		return (IFile) fFilesInProject.get(path.lastSegment());
	}

	protected IFile findFileInWorkspace(IPath path) {
		IFile file = null;
		if (path.isAbsolute()) {
			IWorkspaceRoot root = fProject.getWorkspace().getRoot();
			file =  root.getFileForLocation(path);
			// It may be a link resource so we must check it also.
			if (file == null) {
				IFile[] files = root.findFilesForLocation(path);
				for (int i = 0; i < files.length; i++) {
					if (files[i].getProject().equals(fProject)) {
						file = files[i];
						break;
					}
				}
			}

		} else {
			file = fProject.getFile(path);
		}
		return file;
	}

	protected void collectFiles(IContainer parent, List result) {
		try {
			IResource[] resources = parent.members();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if (resource instanceof IFile) {
					result.add(resource);
				} else if (resource instanceof IContainer) {
					collectFiles((IContainer) resource, result);
				}
			}
		} catch (CoreException e) {
			MakeCorePlugin.log(e.getStatus());
		}
	}

	protected boolean isConflictingName(String fileName) {
		IPath path = new Path(fileName);
		return fNameConflicts.contains(path.lastSegment());
	}

	protected IPath getWorkingDirectory() {
		if (fDirectoryStack.size() != 0) {
			return (IPath) fDirectoryStack.lastElement();
		}
		// Fallback to the Project Location
		// FIXME: if the build did not start in the Project ?
		return fBaseDirectory;
	}

	protected void pushDirectory(IPath dir) {
		if (dir != null) {
			IPath pwd = null;
			if (fBaseDirectory.isPrefixOf(dir)) {
//				int segments = fBaseDirectory.matchingFirstSegments(dir);
				pwd = dir.removeFirstSegments(fBaseDirectory.segmentCount());
			} else {
				pwd = dir;
			}
			fDirectoryStack.addElement(pwd);
		}
	}

	protected IPath popDirectory() {
		int i = getDirectoryLevel();
		if (i != 0) {
			IPath dir = (IPath) fDirectoryStack.lastElement();
			fDirectoryStack.removeElementAt(i - 1);
			return dir;
		}
		return new Path("");	//$NON-NLS-1$
	}

	protected int getDirectoryLevel() {
		return fDirectoryStack.size();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParserUtility#changeMakeDirectory(java.lang.String, int, boolean)
	 */
	public void changeMakeDirectory(String dir, int dirLevel, boolean enterDir) {
    	if (enterDir) {
    		/* Sometimes make screws up the output, so
    		 * "leave" events can't be seen.  Double-check level
    		 * here.
    		 */
			for (int parseLevel = getDirectoryLevel(); dirLevel < parseLevel; parseLevel = getDirectoryLevel()) {
				popDirectory();
			}
    		pushDirectory(new Path(dir));
    	} else {
    		popDirectory();
    		/* Could check to see if they match */
    	}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParserUtility#translateRelativePaths(org.eclipse.core.resources.IFile, java.lang.String, java.util.List)
	 */
	public List translateRelativePaths(IFile file, String fileName, List includes) {
		List translatedIncludes = new ArrayList(includes.size());
		for (Iterator i = includes.iterator(); i.hasNext(); ) {
			String include = (String) i.next();
			IPath includePath = new Path(include);
			if (!includePath.isAbsolute()) {
				// check if it is a relative path
				if (include.startsWith("..") || include.startsWith(".")) {	//$NON-NLS-1$ //$NON-NLS-2$
					// First try the current working directory
					IPath pwd = getWorkingDirectory();
					if (!pwd.isAbsolute()) {
						pwd = fProject.getLocation().append(pwd);
					}
					IPath candidatePath = pwd.append(includePath);
					File dir = candidatePath.makeAbsolute().toFile();
					if (dir.exists()) {
						translatedIncludes.add(candidatePath.toString());
						break;
					}
					else {
						// try to deduce a current path from the fileName format
						if (file != null && fileName != null) {
							// TODO VMIR implement heuristics to translate relative path when the working directory is invalid
							// For now create a marker to identify prospective cases
							generateMarker(file.getProject(), -1, "Ambiguous include path: "+include, //$NON-NLS-1$
									IMarkerGenerator.SEVERITY_WARNING, fileName);				
						}
					}
				}
			}
			// TODO VMIR for now add unresolved paths as well
			translatedIncludes.add(include);
		}
		return translatedIncludes;
	}

}
