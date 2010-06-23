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
 package org.eclipse.cdt.build.core.scannerconfig.tests;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCBuiltinSpecsDetector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class GCCBuiltinSpecsDetectorTest extends TestCase {
	private static final String LANGUAGE_ID = "language.id";
	
	// These should match id and name of extension point defined in plugin.xml
	private static final String PROVIDER_ID_EXT = "org.eclipse.cdt.managedbuilder.core.tests.TestClassBuiltinSpecsDetector";
	private static final String LANG_ID_EXT = "org.eclipse.cdt.core.tests.language.id";

	@Override
	protected void setUp() throws Exception {
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

	
	public void testAbstractBuiltinSpecsDetector_Nulls() throws CoreException {
		AbstractBuiltinSpecsDetector detector = new AbstractBuiltinSpecsDetector() {
			@Override
			public boolean processLine(String line) {
				return true;
			}
			
		};
		detector.setLanguage(null);
		detector.startup(null);
		detector.processLine(null);
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(0, entries.size());
	}
	
	public void testAbstractBuiltinSpecsDetector_Basic() throws CoreException {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());
		
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		AbstractBuiltinSpecsDetector detector = new AbstractBuiltinSpecsDetector() {
			@Override
			public boolean processLine(String line) {
				settingEntries.add(new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN));
				return true;
			}
			
		};
		detector.setLanguage(LANGUAGE_ID);
		detector.startup(cfgDescription);
		detector.processLine("#define MACRO VALUE");
		detector.shutdown();
		
		List<ICLanguageSettingEntry> noentries = detector.getSettingEntries(null, null, null);
		assertEquals(0, noentries.size());
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(cfgDescription, cproject.getProject(), LANGUAGE_ID);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN);
		assertEquals(expected, entries.get(0));
	}
	
	public void testAbstractBuiltinSpecsDetector_StartupShutdown() throws CoreException {
		AbstractBuiltinSpecsDetector detector = new AbstractBuiltinSpecsDetector() {
			@Override
			public boolean processLine(String line) {
				settingEntries.add(new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN));
				return true;
			}
			
		};
		detector.setLanguage(null);
		detector.startup(null);
		{
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
			assertEquals(0, entries.size());
		}
		detector.processLine("#define MACRO VALUE");
		detector.shutdown();
		{
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
			assertEquals(1, entries.size());
		}
		
		detector.startup(null);
		{
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
			assertEquals(0, entries.size());
		}
		detector.processLine("#define MACRO VALUE");
		detector.shutdown();
		{
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
			assertEquals(1, entries.size());
		}
		
	}

	public void testGCCBuiltinSpecsDetector_NoValue() {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.setLanguage(null);
		detector.startup(null);
		detector.processLine("#define MACRO");
		detector.shutdown();
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", null, ICSettingEntry.BUILTIN);
		assertEquals(expected, entries.get(0));
	}
	
	public void testGCCBuiltinSpecsDetector_NoArgs() {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.setLanguage(null);
		detector.startup(null);
		detector.processLine("#define MACRO VALUE");
		detector.shutdown();
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_Const() {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.setLanguage(null);
		detector.startup(null);
		detector.processLine("#define MACRO (3)");
		detector.shutdown();
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "(3)", ICSettingEntry.BUILTIN);
		assertEquals(expected, entries.get(0));
	}
	
	public void testGCCBuiltinSpecsDetector_EmptyArgList() {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.setLanguage(null);
		detector.startup(null);
		detector.processLine("#define MACRO() VALUE");
		detector.shutdown();
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO()", "VALUE", ICSettingEntry.BUILTIN);
		assertEquals(expected, entries.get(0));
	}
	
	public void testGCCBuiltinSpecsDetector_ParamUnused() {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.setLanguage(null);
		detector.startup(null);
		detector.processLine("#define MACRO(X) VALUE");
		detector.shutdown();
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO(X)", "VALUE", ICSettingEntry.BUILTIN);
		assertEquals(expected, entries.get(0));
	}
	
	public void testGCCBuiltinSpecsDetector_ParamSpace() {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.setLanguage(null);
		detector.startup(null);
		detector.processLine("#define MACRO(P1, P2) VALUE(P1, P2)");
		detector.shutdown();
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO(P1, P2)", "VALUE(P1, P2)", ICSettingEntry.BUILTIN);
		assertEquals(expected, entries.get(0));
	}
	
	public void testGCCBuiltinSpecsDetector_ArgsNoValue() {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.setLanguage(null);
		detector.startup(null);
		detector.processLine("#define MACRO(P1, P2) ");
		detector.shutdown();
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO(P1, P2)", null, ICSettingEntry.BUILTIN);
		assertEquals(expected, entries.get(0));
	}
	
	public void testGCCBuiltinSpecsDetector_Includes() {
		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.setLanguage(null);
		detector.startup(null);

		detector.processLine("/incorrect/include1");
		detector.processLine("#include \"...\" search starts here:");
		detector.processLine(" /local/include");
		detector.processLine("#include <...> search starts here:");
		detector.processLine(" /usr/include");
		detector.processLine(" /usr/include2");
		detector.processLine("End of search list.");
		detector.processLine("/incorrect/include2");
		detector.shutdown();
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected0 = new CIncludePathEntry("/local/include", ICSettingEntry.BUILTIN|ICSettingEntry.LOCAL);
		ICLanguageSettingEntry expected1 = new CIncludePathEntry("/usr/include", ICSettingEntry.BUILTIN);
		ICLanguageSettingEntry expected2 = new CIncludePathEntry("/usr/include2", ICSettingEntry.BUILTIN);
		assertEquals(expected0, entries.get(0));
		assertEquals(expected1, entries.get(1));
		assertEquals(expected2, entries.get(2));
		assertEquals(3, entries.size());
	}
	
	public void testExtension() throws Exception {
		// get test plugin extension non-default provider
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getProvider(PROVIDER_ID_EXT);
		assertNotNull(providerExt);
		assertTrue(providerExt instanceof TestClassBuiltinSpecsDetector);
		
		AbstractBuiltinSpecsDetector detector = (AbstractBuiltinSpecsDetector)providerExt;
		detector.startup(null);
		detector.processLine(null);
		detector.shutdown();
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CIncludePathEntry("/test/path/", ICSettingEntry.BUILTIN);
		assertEquals(expected, entries.get(0));
		assertEquals(1, entries.size());

	}

}
