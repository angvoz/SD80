/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Gvozdev
 *******************************************************************************/

package org.eclipse.cdt.errorparsers.xlc.tests;

import junit.framework.Assert;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.errorparsers.xlc.XlcErrorParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/*
 * Helper tester class to be able to test XlcErrorParser which extends AbstractErrorParser.
 */

public class XlcErrorParserTester {
	static private int counter=0;
	IProject fTempProject = ResourcesPlugin.getWorkspace().getRoot().getProject("XlcErrorParserTester.temp." + counter++);

	XlcErrorParserTester() {
		try {
			fTempProject.create(null);
		} catch (CoreException e) {
			e.printStackTrace();
			Assert.fail("Exception creating temporary project "+fTempProject.getName()+": "+e);
		}
	}

	private String fileName;
	private int lineNumber;
	private int severity;
	private String message;

	/*
	 * Dummy class implementing IMarkerGenerator lets get through testing
	 * without NPE.
	 */
	private class MockMarkerGenerator implements IMarkerGenerator {

		public void addMarker(IResource file, int lineNumber, String errorDesc,
				int severity, String errorVar) {
			// dummy
		}

		public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
			// dummy
		}

	}

	/*
	 * Class MockErrorParserManager replaces ErrorParserManager
	 * with the purpose to be able to inquire how the line was parsed.
	 * fileName, lineNumber, message and severity are populated
	 * to be accessed from the test cases.
	 * Relying on internal implementation of ErrorPattern.RecordError()
	 * to provide necessary data via generateExternalMarker() call
	 */
	private class MockErrorParserManager extends ErrorParserManager {

		private MockErrorParserManager() {
			super(fTempProject, new MockMarkerGenerator());
		}

		/*
		 * A stub function just to return non-null IFile.
		 * Necessary to trick ErrorPattern.RecordError() to generate markers.
		 */
		@Override
		public IFile findFileName(String fileName) {
			return fTempProject.getFile(fileName);
		}

		/**
		 * Called by ErrorPattern.RecordError() for external problem markers
		 */
		@Override
		public void generateExternalMarker(IResource file, int lineNumb, String desc, int sev, String varName, IPath externalPath) {
			if (file!=null) {
				fileName = file.getName();
			} else {
				fileName="";
			}
			lineNumber = lineNumb;
			message = desc;
			severity = sev;
		}
	}

	/**
	 * Main method called by individual error parser tests.
	 * @param line one xlC error message
	 * @return
	 */
	boolean parseLine(String line) {
		XlcErrorParser errorParser = new XlcErrorParser();

		MockErrorParserManager epManager = new MockErrorParserManager();
		return errorParser.processLine(line, epManager);
	}

	String getFileName() {
		return fileName;
	}

	int getLineNumber() {
		return lineNumber;
	}

	int getSeverity() {
		return severity;
	}

	String getMessage() {
		return message;
	}
}
