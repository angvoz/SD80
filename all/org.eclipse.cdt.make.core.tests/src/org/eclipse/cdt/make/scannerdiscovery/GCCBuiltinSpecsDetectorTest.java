/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
 package org.eclipse.cdt.make.scannerdiscovery;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCBuiltinSpecsDetector;
import org.eclipse.core.resources.IProject;

public class GCCBuiltinSpecsDetectorTest extends TestCase {
	private static final String LANGUAGE_ID = "language.id";

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
	}

	private ICConfigurationDescription[] getConfigurationDescriptions(IProject project) {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project);
		assertNotNull(projectDescription);
		assertEquals(1, projectDescription.getConfigurations().length);
		// configuration description
		ICConfigurationDescription[] cfgDescriptions = projectDescription.getConfigurations();
		return cfgDescriptions;
	}


	public void testAbstractBuiltinSpecsDetector_Nulls() throws Exception {
		AbstractBuiltinSpecsDetector detector = new AbstractBuiltinSpecsDetector() {
			@Override
			public boolean processLine(String line) {
				return true;
			}

		};
		detector.startup(null, null);
		detector.processLine(null);
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertNull(entries);
	}

	public void testAbstractBuiltinSpecsDetector_Basic() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		AbstractBuiltinSpecsDetector detector = new AbstractBuiltinSpecsDetector() {
			@Override
			public boolean processLine(String line) {
				// pretending that we parsed the line
				detectedSettingEntries.add(new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY));
				return true;
			}

		};
		detector.startup(cfgDescription, LANGUAGE_ID);
		detector.processLine("#define MACRO VALUE");
		detector.shutdown();

		List<ICLanguageSettingEntry> noentries = detector.getSettingEntries(null, null, null);
		assertNull(noentries);

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(cfgDescription, project, LANGUAGE_ID);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testAbstractBuiltinSpecsDetector_StartupShutdown() throws Exception {
		// Define mock detector
		AbstractBuiltinSpecsDetector detector = new AbstractBuiltinSpecsDetector() {
			@Override
			public boolean processLine(String line) {
				detectedSettingEntries.add(new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY));
				return true;
			}

		};

		// Test startup/shutdown on running with each build
		detector.setRunOnce(false);
		assertEquals(false, detector.isRunOnce());

		detector.startup(null, null);
		{
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
			assertNull(entries);
		}
		detector.processLine("#define MACRO VALUE");
		detector.shutdown();
		{
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
			assertEquals(1, entries.size());
		}

		detector.startup(null, null);
		{
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
			assertNull(entries);
		}
		detector.processLine("#define MACRO VALUE");
		detector.shutdown();
		{
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
			assertEquals(1, entries.size());
		}

		// Test startup on running once
		detector.setRunOnce(true);
		assertEquals(true, detector.isRunOnce());

		detector.startup(null, null);
		{
			// Should not clear entries
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
			assertEquals(1, entries.size());
		}
		detector.shutdown();
		{
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
			assertEquals(1, entries.size());
		}
	}

	public void testGCCBuiltinSpecsDetector_NoValue() throws Exception {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.startup(null, null);
		detector.processLine("#define MACRO");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", null, ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_NoArgs() throws Exception {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.startup(null, null);
		detector.processLine("#define MACRO VALUE");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_Const() throws Exception {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.startup(null, null);
		detector.processLine("#define MACRO (3)");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "(3)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_EmptyArgList() throws Exception {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.startup(null, null);
		detector.processLine("#define MACRO() VALUE");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO()", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_ParamUnused() throws Exception {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.startup(null, null);
		detector.processLine("#define MACRO(X) VALUE");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO(X)", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_ParamSpace() throws Exception {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.startup(null, null);
		detector.processLine("#define MACRO(P1, P2) VALUE(P1, P2)");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO(P1, P2)", "VALUE(P1, P2)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_ArgsNoValue() throws Exception {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.startup(null, null);
		detector.processLine("#define MACRO(P1, P2) ");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO(P1, P2)", null, ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_Includes() throws Exception {
		// Create model project and folders to test
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProject(projectName);
		ResourceHelper.createFolder(project, "/incorrect/include1");
		ResourceHelper.createFolder(project, "/local/include");
		ResourceHelper.createFolder(project, "/usr/include");
		ResourceHelper.createFolder(project, "/usr/include2");
		ResourceHelper.createFolder(project, "/incorrect/include2");
		String loc = project.getLocation().toString();

		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.startup(null, null);

		detector.processLine(loc+"/incorrect/include1");
		detector.processLine("#include \"...\" search starts here:");
		detector.processLine(" "+loc+"/local/include");
		detector.processLine("#include <...> search starts here:");
		detector.processLine(" "+loc+"/usr/include");
		detector.processLine(" "+loc+"/usr/include/../include2");
		detector.processLine(" "+loc+"/missing/folder");
		detector.processLine("End of search list.");
		detector.processLine(loc+"/incorrect/include2");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected0 = new CIncludePathEntry(loc+"/local/include", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY | ICSettingEntry.LOCAL);
		ICLanguageSettingEntry expected1 = new CIncludePathEntry(loc+"/usr/include", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		ICLanguageSettingEntry expected2 = new CIncludePathEntry(loc+"/usr/include2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		ICLanguageSettingEntry expected3 = new CIncludePathEntry(loc+"/missing/folder", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected0, entries.get(0));
		assertEquals(expected1, entries.get(1));
		assertEquals(expected2, entries.get(2));
		assertEquals(expected3, entries.get(3));
		assertEquals(4, entries.size());
	}

}
