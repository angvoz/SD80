/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.test;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.MapProblemPreference;
import org.eclipse.cdt.codan.core.param.RootProblemPreference;
import org.eclipse.cdt.codan.internal.core.model.CodanProblem;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Alena Laskavaia
 */
@SuppressWarnings("nls")
public class CheckerTestCase extends CodanTestCase {
	protected IMarker[] markers;

	public IMarker checkErrorLine(int i) {
		return checkErrorLine(currentFile, i);
	}

	public void checkErrorLines(Object... args) {
		for (Object i : args) {
			checkErrorLine((Integer) i);
		}
		assertEquals(args.length, markers.length);
	}

	public IMarker checkErrorLine(int i, String problemId) {
		return checkErrorLine(currentFile, i, problemId);
	}

	public IMarker checkErrorLine(File file, int expectedLine) {
		return checkErrorLine(file, expectedLine, null);
	}

	/**
	 * @param expectedLine
	 *        - line
	 * @return
	 */
	public IMarker checkErrorLine(File file, int expectedLine, String problemId) {
		assertTrue(markers != null);
		assertTrue("No problems found but should", markers.length > 0); //$NON-NLS-1$
		boolean found = false;
		Integer line = null;
		String mfile = null;
		IMarker m = null;
		for (int j = 0; j < markers.length; j++) {
			m = markers[j];
			line = getLine(m);
			mfile = m.getResource().getName();
			if (line.equals(expectedLine) && (problemId == null || problemId.equals(CodanProblemMarker.getProblemId(m)))) {
				found = true;
				if (file != null && !file.getName().equals(mfile))
					found = false;
				else
					break;
			}
		}
		assertEquals(Integer.valueOf(expectedLine), line);
		if (file != null)
			assertEquals(file.getName(), mfile);
		assertTrue(found);
		assertNotNull(m);
		return m;
	}

	/**
	 * @param line
	 * @param m
	 * @return
	 */
	public Integer getLine(IMarker m) {
		Integer line = null;
		try {
			line = (Integer) m.getAttribute(IMarker.LINE_NUMBER);
			if (line == null || line.equals(-1)) {
				Object pos = m.getAttribute(IMarker.CHAR_START);
				line = new Integer(pos2line(((Integer) pos).intValue()));
			}
		} catch (CoreException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		return line;
	}

	public void checkNoErrors() {
		if (markers == null || markers.length == 0) {
			// all good
		} else {
			IMarker m = markers[0];
			fail("Found " + markers.length + " errors but should not. First " + CodanProblemMarker.getProblemId(m) + " at line "
					+ getLine(m));
		}
	}

	/**
	 *
	 */
	public void runOnProject() {
		try {
			indexFiles();
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		runCodan();
	}

	public void loadCodeAndRun(String code) {
		loadcode(code);
		runCodan();
	}

	public void loadCodeAndRunCpp(String code) {
		loadcode(code, true);
		runCodan();
	}

	/**
	 *
	 */
	protected void runCodan() {
		CodanRuntime.getInstance().getBuilder().processResource(cproject.getProject(), new NullProgressMonitor());
		try {
			markers = cproject.getProject().findMarkers(IProblemReporter.GENERIC_CODE_ANALYSIS_MARKER_TYPE, true, 1);
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * @param problemId
	 * @param paramId
	 * @return
	 */
	protected IProblemPreference getPreference(String problemId, String paramId) {
		IProblem problem = CodanRuntime.getInstance().getCheckersRegistry().getResourceProfile(cproject.getResource())
				.findProblem(problemId);
		IProblemPreference pref = ((MapProblemPreference) problem.getPreference()).getChildDescriptor(paramId);
		return pref;
	}

	protected IProblemPreference setPreferenceValue(String problemId, String paramId, Object value) {
		IProblemPreference param = getPreference(problemId, paramId);
		param.setValue(value);
		return param;
	}

	/**
	 * @param string
	 * @param m
	 */
	public void assertMessageMatch(String pattern, IMarker m) {
		try {
			String attribute = (String) m.getAttribute(IMarker.MESSAGE);
			if (attribute.matches(pattern)) {
				fail("Expected " + attribute + " to match with /" + pattern //$NON-NLS-1$ //$NON-NLS-2$
						+ "/"); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	protected void enableProblems(String... ids) {
		IProblemProfile profile = CodanRuntime.getInstance().getCheckersRegistry().getWorkspaceProfile();
		IProblem[] problems = profile.getProblems();
		for (int i = 0; i < problems.length; i++) {
			IProblem p = problems[i];
			boolean enabled = false;
			for (int j = 0; j < ids.length; j++) {
				String pid = ids[j];
				if (p.getId().equals(pid)) {
					enabled = true;
					// Force the launch mode to FULL_BUILD to make sure we can test the problem even if by default it
					// is not set to run on FULL_BUILD
					IProblemPreference preference = p.getPreference();
					if (preference instanceof RootProblemPreference) {
						RootProblemPreference rootProblemPreference = (RootProblemPreference) preference;
						rootProblemPreference.getLaunchModePreference().enableInLaunchModes(CheckerLaunchMode.RUN_ON_FULL_BUILD);
					}
					break;
				}
			}
			((CodanProblem) p).setEnabled(enabled);
		}
		CodanRuntime.getInstance().getCheckersRegistry().updateProfile(cproject.getProject(), profile);
		return;
	}
}
