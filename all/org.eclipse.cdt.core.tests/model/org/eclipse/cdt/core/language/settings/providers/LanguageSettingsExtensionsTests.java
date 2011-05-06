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
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsExtensionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsExtensionsTests extends TestCase {
	// These should match corresponding entries defined in plugin.xml
	private static final String EXTENSION_BASE_PROVIDER_ID = "org.eclipse.cdt.core.tests.language.settings.base.provider";
	private static final String EXTENSION_BASE_PROVIDER_NAME = "Test Plugin Language Settings Base Provider";
	private static final String EXTENSION_BASE_PROVIDER_LANG_ID = "org.eclipse.cdt.core.tests.language.id";
	private static final String EXTENSION_BASE_PROVIDER_PARAMETER = "custom parameter";
	private static final String EXTENSION_CUSTOM_PROVIDER_ID = "org.eclipse.cdt.core.tests.custom.language.settings.provider";
	private static final String EXTENSION_CUSTOM_PROVIDER_NAME = "Test Plugin Language Settings Provider";
	private static final String EXTENSION_BASE_SUBCLASS_PROVIDER_ID = "org.eclipse.cdt.core.tests.language.settings.base.provider.subclass";
	private static final String EXTENSION_BASE_SUBCLASS_PROVIDER_PARAMETER = "custom parameter subclass";
	private static final String EXTENSION_SERIALIZABLE_PROVIDER_ID = "org.eclipse.cdt.core.tests.custom.serializable.language.settings.provider";
	private static final String EXTENSION_SERIALIZABLE_PROVIDER_NAME = "Test Plugin Serializable Language Settings Provider";
	private static final String EXTENSION_SERIALIZABLE_PROVIDER_PARAMETER = "";
	private static final String EXTENSION_EDITABLE_PROVIDER_ID = "org.eclipse.cdt.core.tests.custom.editable.language.settings.provider";

	// These are made up
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";
	private static final String LANG_ID = "test.lang.id";
	private static final IFile FILE_0 = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/project/path0"));

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
	 */
	public void testExtension() throws Exception {
		{
			List<ILanguageSettingsProvider> providers = LanguageSettingsManager.getWorkspaceProviders();
			List<String> ids = new ArrayList<String>();
			for (ILanguageSettingsProvider provider : providers) {
				ids.add(provider.getId());
			}
			assertTrue("extension " + EXTENSION_BASE_PROVIDER_ID + " not found", ids.contains(EXTENSION_BASE_PROVIDER_ID));
		}

		// get test plugin extension provider
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getRawWorkspaceProvider(EXTENSION_BASE_PROVIDER_ID);
		assertNotNull(providerExt);

		assertTrue(providerExt instanceof LanguageSettingsBaseProvider);
		LanguageSettingsBaseProvider provider = (LanguageSettingsBaseProvider)providerExt;
		assertEquals(EXTENSION_BASE_PROVIDER_ID, provider.getId());
		assertEquals(EXTENSION_BASE_PROVIDER_NAME, provider.getName());
		assertEquals(EXTENSION_BASE_PROVIDER_PARAMETER, provider.getCustomParameter());

		// attempt to get entries for wrong language
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
		List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, FILE_0, EXTENSION_BASE_PROVIDER_LANG_ID);
		for (int i=0;i<entriesExt.size();i++) {
			assertEquals("i="+i, entriesExt.get(i), actual.get(i));
		}
		assertEquals(entriesExt.size(), actual.size());
	}

	/**
	 * Check that subclassed LanguageSettingsBaseProvider extension defined in plugin.xml is accessible.
	 */
	public void testExtensionBaseProviderSubclass() throws Exception {
		// get test plugin extension provider
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getRawWorkspaceProvider(EXTENSION_BASE_SUBCLASS_PROVIDER_ID);
		assertNotNull(providerExt);

		assertTrue(providerExt instanceof MockLanguageSettingsBaseProvider);
		MockLanguageSettingsBaseProvider provider = (MockLanguageSettingsBaseProvider)providerExt;
		assertEquals(EXTENSION_BASE_SUBCLASS_PROVIDER_ID, provider.getId());
		assertEquals(EXTENSION_BASE_SUBCLASS_PROVIDER_PARAMETER, provider.getCustomParameter());

		// Test for null languages
		assertNull(provider.getLanguageScope());

		// benchmarks matching extension point definition
		List<ICLanguageSettingEntry> entriesExt = new ArrayList<ICLanguageSettingEntry>();
		entriesExt.add(new CIncludePathEntry("/usr/include/", ICSettingEntry.BUILTIN));

		// retrieve entries from extension point
		List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, FILE_0, LANG_ID);
		for (int i=0;i<entriesExt.size();i++) {
			assertEquals("i="+i, entriesExt.get(i), actual.get(i));
		}
		assertEquals(entriesExt.size(), actual.size());
	}

	/**
	 * Make sure extensions contributed through extension point are sorted by name.
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
	 */
	public void testExtensionsNameId() throws Exception {
		// get test plugin extension non-default provider
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getRawWorkspaceProvider(EXTENSION_CUSTOM_PROVIDER_ID);
		assertNotNull(providerExt);
		assertTrue(providerExt instanceof MockLanguageSettingsProvider);

		assertEquals(EXTENSION_CUSTOM_PROVIDER_ID, providerExt.getId());
		assertEquals(EXTENSION_CUSTOM_PROVIDER_NAME, providerExt.getName());
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
			List<String> actualLanguageIds = provider.getLanguageScope();
			for (String languageId: languages) {
				assertTrue(actualLanguageIds.contains(languageId));
			}
			assertEquals(languages.size(), actualLanguageIds.size());
		}
	}
	
	/**
	 * TODO
	 */
	public void testSerializableProvider() throws Exception {
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_SERIALIZABLE_PROVIDER_ID);
		assertNotNull(providerExt);
		assertTrue(providerExt instanceof LanguageSettingsSerializable);
		
		LanguageSettingsSerializable provider = (LanguageSettingsSerializable) providerExt;
		
		assertEquals(null, provider.getLanguageScope());
		assertEquals("", provider.getCustomParameter());
		
		List<ICLanguageSettingEntry> expected = new ArrayList<ICLanguageSettingEntry>();
		expected.add(new CMacroEntry("MACRO", "value", 0));
		assertEquals(expected, provider.getSettingEntries(null, null, null));
	}

	/**
	 * TODO
	 */
	public void testEditableProvider() throws Exception {
		// Non-editable providers cannot be copied so they are singletons
		{
			ILanguageSettingsProvider providerExt = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			assertNotNull(providerExt);
			assertTrue(providerExt instanceof LanguageSettingsSerializable);
			assertTrue(LanguageSettingsExtensionManager.equalsExtensionProvider(providerExt));
			
			ILanguageSettingsProvider providerExt2 = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			assertSame(providerExt, providerExt2);
			
			ILanguageSettingsProvider providerWsp = LanguageSettingsManager.getRawWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			assertSame(providerExt, providerWsp);
		}
		
		// Editable providers are retrieved by copy
		{
			ILanguageSettingsProvider providerExt = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(providerExt);
			assertTrue(providerExt instanceof ILanguageSettingsEditableProvider);
			assertTrue(LanguageSettingsExtensionManager.equalsExtensionProvider(providerExt));
			
			ILanguageSettingsProvider providerExt2 = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotSame(providerExt, providerExt2);
			assertEquals(providerExt, providerExt2);
			
			ILanguageSettingsProvider providerWsp = LanguageSettingsManager.getRawWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotSame(providerExt, providerWsp);
			assertEquals(providerExt, providerWsp);
			assertTrue(LanguageSettingsExtensionManager.equalsExtensionProvider(providerWsp));
		}
		
		// Test shallow copy
		{
			ILanguageSettingsProvider provider = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(provider);
			assertTrue(provider instanceof ILanguageSettingsEditableProvider);
			
			ILanguageSettingsProvider providerShallow = LanguageSettingsExtensionManager.getExtensionProviderShallow(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(providerShallow);
			assertTrue(providerShallow instanceof ILanguageSettingsEditableProvider);
			assertFalse(provider.equals(providerShallow));
			
			assertFalse(LanguageSettingsExtensionManager.equalsExtensionProvider(providerShallow));
			assertTrue(LanguageSettingsExtensionManager.equalsExtensionProviderShallow((ILanguageSettingsEditableProvider) providerShallow));
			
		}
	}
	
//	/**
//	 * LanguageSettingsBaseProvider is not allowed to be configured twice.
//	 */
//	public void testBaseProviderConfigure() throws Exception {
//		// create LanguageSettingsBaseProvider
//		LanguageSettingsBaseProvider provider = new LanguageSettingsBaseProvider();
//		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
//		entries.add(new CIncludePathEntry("/usr/include/", 0));
//		// configure it
//		provider.configureProvider("id", "name", null, entries, null);
//
//		try {
//			// attempt to configure it twice should fail
//			provider.configureProvider("id", "name", null, entries, null);
//			fail("LanguageSettingsBaseProvider is not allowed to be configured twice");
//		} catch (UnsupportedOperationException e) {
//		}
//	}

	/**
	 * TODO
	 */
	public void testReset() throws Exception {
		// get test plugin extension provider
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getRawWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
		assertNotNull(providerExt);

		assertTrue(providerExt instanceof LanguageSettingsSerializable);
		LanguageSettingsSerializable provider = (LanguageSettingsSerializable)providerExt;
		
		// doublecheck benchmarks
		{
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, provider.getId());
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_NAME, provider.getName());
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_PARAMETER, provider.getCustomParameter());
			
			// retrieve entries from extension point
			List<ICLanguageSettingEntry> expected = new ArrayList<ICLanguageSettingEntry>();
			expected.add(new CMacroEntry("MACRO", "value", 0));
			assertEquals(expected, provider.getSettingEntries(null, null, null));
		}
		
		// change provider
		String changedName = "Changed name";
		String changedParameter = "changedParameter";
		provider.setName(changedName);
		provider.setCustomParameter(changedParameter);
		List<ICLanguageSettingEntry> changedEntries = new ArrayList<ICLanguageSettingEntry>();
		changedEntries.add(new CIncludePathEntry("/added/entry/", 0));
		provider.setSettingEntries(null, null, null, changedEntries);
		// doublecheck changed
		{
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, provider.getId());
			assertEquals(changedName, provider.getName());
			assertEquals(changedParameter, provider.getCustomParameter());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertEquals(changedEntries, actual);
		}

		// reset provider
		LanguageSettingsExtensionManager.reset(provider);
		
		// should match original benchmarks
		{
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, provider.getId());
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_NAME, provider.getName());
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_PARAMETER, provider.getCustomParameter());
			
			// retrieve entries from extension point
			List<ICLanguageSettingEntry> expected = new ArrayList<ICLanguageSettingEntry>();
			expected.add(new CMacroEntry("MACRO", "value", 0));
			assertEquals(expected, provider.getSettingEntries(null, null, null));
		}
	}
}
