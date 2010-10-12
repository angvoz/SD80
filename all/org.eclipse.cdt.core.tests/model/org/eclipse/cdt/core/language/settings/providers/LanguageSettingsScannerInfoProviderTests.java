/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsScannerInfoProvider;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsScannerInfoProviderTests extends TestCase {
	private static final IFile FAKE_FILE = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/project/path0"));
	private static final String PROVIDER_ID = "test.provider.id";
	private static final String PROVIDER_NAME = "test.provider.name";

	private class MockProvider extends AbstractExecutableExtensionBase implements ILanguageSettingsProvider {
		private final List<ICLanguageSettingEntry> entries;

		public MockProvider(String id, String name, List<ICLanguageSettingEntry> entries) {
			super(id, name);
			this.entries = entries;
		}

		public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
			return entries;
		}
	}



	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsScannerInfoProviderTests(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(LanguageSettingsScannerInfoProviderTests.class);
	}

	/**
	 * main function of the class.
	 *
	 * @param args - arguments
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * Test cases when some objects are null
	 *
	 * @throws Exception...
	 */
	public void testNulls() throws Exception {
		{
			// Handle project==null
			IResource root = ResourcesPlugin.getWorkspace().getRoot();
			assertNull(root.getProject());

			LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
			ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(root);
			assertEquals(0, info.getIncludePaths().length);
			assertEquals(0, info.getDefinedSymbols().size());
			assertEquals(0, info.getIncludeFiles().length);
			assertEquals(0, info.getMacroFiles().length);
			assertEquals(0, info.getLocalIncludePath().length);
		}

		{
			// Handle prjDescription==null
			IProject project = FAKE_FILE.getProject();
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			assertNull(prjDescription);

			LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
			ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(FAKE_FILE);
			assertEquals(0, info.getIncludePaths().length);
			assertEquals(0, info.getDefinedSymbols().size());
			assertEquals(0, info.getIncludeFiles().length);
			assertEquals(0, info.getMacroFiles().length);
			assertEquals(0, info.getLocalIncludePath().length);
		}

		{
			// Handle language==null
			LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
			IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
			IFile file = ResourceHelper.createFile(project, "file");

			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
			assertNotNull(cfgDescription);
			ILanguage language = LanguageManager.getInstance().getLanguageForFile(file, cfgDescription);
			assertNull(language);

			ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
			assertEquals(0, info.getIncludePaths().length);
			assertEquals(0, info.getDefinedSymbols().size());
			assertEquals(0, info.getIncludeFiles().length);
			assertEquals(0, info.getMacroFiles().length);
			assertEquals(0, info.getLocalIncludePath().length);
		}
	}

	/**
	 * Test empty scanner info
	 *
	 * @throws Exception...
	 */
	public void testEmpty() throws Exception {
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		IFile file = ResourceHelper.createFile(project, "file.c");

		// confirm that language==null
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		ILanguage language = LanguageManager.getInstance().getLanguageForFile(file, cfgDescription);
		assertNotNull(language);

		// test that the info is empty
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		assertEquals(0, info.getIncludePaths().length);
		assertEquals(0, info.getDefinedSymbols().size());
		assertEquals(0, info.getIncludeFiles().length);
		assertEquals(0, info.getMacroFiles().length);
		assertEquals(0, info.getLocalIncludePath().length);
	}
	/**
	 * Test regular cases
	 *
	 * @throws Exception...
	 */
	public void testRegular() throws Exception {
		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		IFile file = ResourceHelper.createFile(project, "file.c");

		// retrieve descriptions and language
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		ILanguage language = LanguageManager.getInstance().getLanguageForFile(file, cfgDescription);
		assertNotNull(language);

		// contribute the entries
		CIncludePathEntry includePathEntry = new CIncludePathEntry(new Path("/include-path").toOSString(), 0);
		CMacroEntry macroEntry = new CMacroEntry("MACRO", "value",0);
		CIncludeFileEntry includeFileEntry = new CIncludeFileEntry(new Path("/include-file").toOSString(), 0);
		CMacroFileEntry macroFileEntry = new CMacroFileEntry(new Path("/macro-file").toOSString(), 0);

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(includePathEntry);
		entries.add(macroEntry);
		entries.add(includeFileEntry);
		entries.add(macroFileEntry);

		// add provider to the configuration
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_ID, PROVIDER_NAME, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		cfgDescription.setLanguageSettingProviders(providers);

		// test that the scannerInfoProvider gets the entries
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		String[] actualIncludePaths = info.getIncludePaths();
		Map<String, String> actualDefinedSymbols = info.getDefinedSymbols();
		String[] actualIncludeFiles = info.getIncludeFiles();
		String[] actualMacroFiles = info.getMacroFiles();
		String[] actualLocalIncludePath = info.getLocalIncludePath();

		assertEquals(includePathEntry.getName(), actualIncludePaths[0]);
		assertEquals(1, actualIncludePaths.length);

		assertEquals(macroEntry.getValue(), actualDefinedSymbols.get(macroEntry.getName()));
		assertEquals(1, actualDefinedSymbols.size());

		assertEquals(includeFileEntry.getName(), actualIncludeFiles[0]);
		assertEquals(1, actualIncludeFiles.length);

		assertEquals(macroFileEntry.getName(), actualMacroFiles[0]);
		assertEquals(1, actualMacroFiles.length);

		assertEquals(0, actualLocalIncludePath.length);

	}
}
