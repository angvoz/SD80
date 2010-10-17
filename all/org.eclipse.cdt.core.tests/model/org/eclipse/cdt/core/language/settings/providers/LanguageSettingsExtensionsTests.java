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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsExtensionsTests extends TestCase {
	// These should match corresponding entries defined in plugin.xml
	private static final String DEFAULT_PROVIDER_ID_EXT = "org.eclipse.cdt.core.tests.language.settings.base.provider";
	private static final String DEFAULT_PROVIDER_NAME_EXT = "Test Plugin Language Settings Base Provider";
	private static final String DEFAULT_PROVIDER_PARAMETER_EXT = "custom parameter";
	private static final String PROVIDER_ID_EXT = "org.eclipse.cdt.core.tests.custom.language.settings.provider";
	private static final String PROVIDER_NAME_EXT = "Test Plugin Language Settings Provider";
	private static final String PROVIDER_PARAMETER_EXT = "custom parameter subclass";
	private static final String LANG_ID_EXT = "org.eclipse.cdt.core.tests.language.id";
	private static final String BASE_PROVIDER_SUBCLASS_ID_EXT = "org.eclipse.cdt.core.tests.language.settings.base.provider.subclass";

	// These are made up
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";
	private static final IFile FILE_0 = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/project/path0"));
	private static final String LANG_ID = "test.lang.id";

	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsExtensionsTests(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(LanguageSettingsExtensionsTests.class);
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
	 * Check that regular ICLanguageSettingsProvider extension defined in plugin.xml is accessible.
	 *
	 * @throws Exception...
	 */
	public void testExtension() throws Exception {
		{
			List<ILanguageSettingsProvider> providers = LanguageSettingsManager.getWorkspaceProviders();
			List<String> ids = new ArrayList<String>();
			for (ILanguageSettingsProvider provider : providers) {
				ids.add(provider.getId());
			}
			assertTrue("extension " + DEFAULT_PROVIDER_ID_EXT + " not found", ids.contains(DEFAULT_PROVIDER_ID_EXT));
		}

		// get test plugin extension provider
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(DEFAULT_PROVIDER_ID_EXT);
		assertNotNull(providerExt);

		assertTrue(providerExt instanceof LanguageSettingsBaseProvider);
		LanguageSettingsBaseProvider provider = (LanguageSettingsBaseProvider)providerExt;
		assertEquals(DEFAULT_PROVIDER_ID_EXT, provider.getId());
		assertEquals(DEFAULT_PROVIDER_NAME_EXT, provider.getName());
		assertEquals(DEFAULT_PROVIDER_PARAMETER_EXT, provider.getCustomParameter());

		// retrieve wrong language
		assertNull(provider.getSettingEntries(null, FILE_0, LANG_ID));

		// benchmarks matching extension point definition
		List<ICLanguageSettingEntry> entriesExt = new ArrayList<ICLanguageSettingEntry>();
		entriesExt.add(new CIncludePathEntry("/usr/include/",
				ICSettingEntry.BUILTIN
				| ICSettingEntry.LOCAL
				| ICSettingEntry.RESOLVED
				| ICSettingEntry.VALUE_WORKSPACE_PATH
				| ICSettingEntry.UNDEFINED
		));
		entriesExt.add(new CMacroEntry("TEST_DEFINE", "100", 0));
		entriesExt.add(new CIncludeFileEntry("/include/file.inc", 0));
		entriesExt.add(new CLibraryPathEntry("/usr/lib/", 0));
		entriesExt.add(new CLibraryFileEntry("libdomain.a", 0));
		entriesExt.add(new CMacroFileEntry("/macro/file.mac", 0));

		// retrieve entries from extension point
		List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, FILE_0, LANG_ID_EXT);
		for (int i=0;i<entriesExt.size();i++) {
			assertEquals("i="+i, entriesExt.get(i), actual.get(i));
		}
		assertEquals(entriesExt.size(), actual.size());
	}

	/**
	 * Check that subclassed LanguageSettingsBaseProvider extension defined in plugin.xml is accessible.
	 *
	 * @throws Exception...
	 */
	public void testExtensionBaseProviderSubclass() throws Exception {
		// get test plugin extension provider
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(BASE_PROVIDER_SUBCLASS_ID_EXT);
		assertNotNull(providerExt);

		assertTrue(providerExt instanceof TestClassLanguageSettingsBaseProvider);
		TestClassLanguageSettingsBaseProvider provider = (TestClassLanguageSettingsBaseProvider)providerExt;
		assertEquals(BASE_PROVIDER_SUBCLASS_ID_EXT, provider.getId());
		assertEquals(PROVIDER_PARAMETER_EXT, provider.getCustomParameter());

		// Test for null languages
		assertNull(provider.getLanguageIds());

		// benchmarks matching extension point definition
		List<ICLanguageSettingEntry> entriesExt = new ArrayList<ICLanguageSettingEntry>();
		entriesExt.add(new CIncludePathEntry("/usr/include/", ICSettingEntry.BUILTIN));

		// retrieve entries from extension point
		List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, FILE_0, LANG_ID_EXT);
		for (int i=0;i<entriesExt.size();i++) {
			assertEquals("i="+i, entriesExt.get(i), actual.get(i));
		}
		assertEquals(entriesExt.size(), actual.size());
	}

	/**
	 * Make sure extensions contributed through extension point are sorted by name.
	 *
	 * @throws Exception...
	 */
	public void testExtensionsSorting() throws Exception {
		{
			List<ILanguageSettingsProvider> providers = LanguageSettingsManager.getWorkspaceProviders();
			String lastName="";
			for (ILanguageSettingsProvider provider : providers) {
				if (LanguageSettingsManager.isWorkspaceProvider(provider)) {
					String name = provider.getName();
					assertTrue(lastName.compareTo(name)<=0);
					lastName = name;
				}
			}
		}
	}

	/**
	 * Make sure extensions contributed through extension point created with proper ID/name.
	 *
	 * @throws Exception...
	 */
	public void testExtensionsNameId() throws Exception {
		// get test plugin extension non-default provider
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(PROVIDER_ID_EXT);
		assertNotNull(providerExt);
		assertTrue(providerExt instanceof TestClassLanguageSettingsProvider);

		assertEquals(PROVIDER_ID_EXT, providerExt.getId());
		assertEquals(PROVIDER_NAME_EXT, providerExt.getName());
	}

	/**
	 * Basic test for LanguageSettingsBaseProvider.
	 */
	public void testBaseProvider() throws Exception {
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		List<String> languages = new ArrayList<String>(2);
		languages.add("bogus.language.id");
		languages.add(LANG_ID);

		// add default provider
		LanguageSettingsBaseProvider provider = new LanguageSettingsBaseProvider(
				PROVIDER_0, PROVIDER_NAME_0, languages, entries);

		{
			// attempt to get entries for wrong language
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, FILE_0, "wrong.lang.id");
			assertNull(actual);
		}

		{
			// retrieve the entries
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, FILE_0, LANG_ID);
			assertEquals(entries.get(0), actual.get(0));
			assertNotSame(entries, actual);
			// retrieve languages
			List<String> actualLanguageIds = provider.getLanguageIds();
			for (String languageId: languages) {
				assertTrue(actualLanguageIds.contains(languageId));
			}
			assertEquals(languages.size(), actualLanguageIds.size());
		}
	}

	/**
	 * LanguageSettingsBaseProvider is not allowed to be configured twice.
	 *
	 * @throws Exception...
	 */
	public void testBaseProviderConfigure() throws Exception {
		// create LanguageSettingsBaseProvider
		LanguageSettingsBaseProvider provider = new LanguageSettingsBaseProvider();
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("/usr/include/", 0));
		// configure it
		provider.configureProvider("id", "name", null, entries, null);

		try {
			// attempt to configure it twice should fail
			provider.configureProvider("id", "name", null, entries, null);
			fail("LanguageSettingsBaseProvider is not allowed to be configured twice");
		} catch (UnsupportedOperationException e) {
		}
	}
}
