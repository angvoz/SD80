/*******************************************************************************
 * Copyright (c) 2009 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.settings.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsExtensionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsSerializableTests extends TestCase {
	private static final String CFG_ID = "test.configuration.id";
	private static final String CFG_ID_1 = "test.configuration.id.1";
	private static final String CFG_ID_2 = "test.configuration.id.2";
	private static final ICConfigurationDescription MOCK_CFG = new CProjectDescriptionTestHelper.DummyCConfigurationDescription(CFG_ID);
	private static final IResource MOCK_RC = ResourcesPlugin.getWorkspace().getRoot();
	private static final String LANG_ID = "test.lang.id";
	private static final String LANG_ID_1 = "test.lang.id.1";
	private static final String LANG_ID_2 = "test.lang.id.2";
	private static final String PROVIDER_NULL = "test.provider.null.id";
	private static final String PROVIDER_1 = "test.provider.1.id";
	private static final String PROVIDER_2 = "test.provider.2.id";
	private static final String PROVIDER_NAME_NULL = "test.provider.null.name";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";

	private static final String ELEM_TEST = "test";

	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsSerializableTests(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
//		ResourceHelper.cleanUp();
		LanguageSettingsManager.setUserDefinedProviders(null);
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(LanguageSettingsSerializableTests.class);
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
	 */
	public void testProvider() throws Exception {
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));

		// create a provider
		LanguageSettingsSerializable mockProvider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
		// test isEmpty()
		assertTrue(mockProvider.isEmpty());
		// test setter and getter
		mockProvider.setSettingEntries(null, MOCK_RC, LANG_ID, original);
		List<ICLanguageSettingEntry> retrieved = mockProvider.getSettingEntries(null, MOCK_RC, LANG_ID);
		assertEquals(original.get(0), retrieved.get(0));
		assertEquals(original.size(), retrieved.size());
		assertFalse(mockProvider.isEmpty());
		// test clear()
		mockProvider.clear();
		assertTrue(mockProvider.isEmpty());
	}

	/**
	 */
	public void testNoProviders() throws Exception {
		// nullify user defined providers
		LanguageSettingsExtensionManager.setUserDefinedProvidersInternal(null);
		String[] allIds = LanguageSettingsManager.getProviderAvailableIds();
		String[] extensionIds = LanguageSettingsManager.getProviderExtensionIds();
		assertEquals(allIds.length, extensionIds.length);

		// serialize language settings of user defined providers (on workspace level)
		LanguageSettingsExtensionManager.serializeLanguageSettingsWorkspace();
		LanguageSettingsExtensionManager.loadLanguageSettingsWorkspace();

		// test passes if no exception was thrown
	}

	/**
	 */
	public void testEmptyProvider() throws Exception {
		Element elementProvider;
		{
			// create null provider
			LanguageSettingsSerializable providerNull = new LanguageSettingsSerializable(PROVIDER_NULL, PROVIDER_NAME_NULL);
			assertNull(providerNull.getSettingEntries(null, null, null));
			// set and get null entries
			providerNull.setSettingEntries(null, null, null, null);
			assertNull(providerNull.getSettingEntries(null, null, null));

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = providerNull.serialize(rootElement);
			String xmlString = XmlUtil.toString(doc);
			assertTrue(xmlString.contains(PROVIDER_NULL));
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_NULL, provider.getId());
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertNull(retrieved);
		}
	}

	/**
	 */
	public void testNullConfiguration() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(null, MOCK_RC, LANG_ID, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that "configuration" element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			String xmlTag = "configuration"; // LanguageSettingsSerializable.ELEM_CONFIGURATION;
			assertFalse(xmlString.contains(xmlTag));
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, MOCK_RC, LANG_ID);
			assertEquals(original.get(0), retrieved.get(0));
		}
	}

	/**
	 */
	public void testNullLanguage() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(MOCK_CFG, MOCK_RC, null, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that "language" element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			String xmlTag = "language"; // LanguageSettingsSerializable.ELEM_LANGUAGE;
			assertFalse(xmlString.contains(xmlTag));
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(MOCK_CFG, MOCK_RC, null);
			assertEquals(original.get(0), retrieved.get(0));
		}
	}


	/**
	 */
	public void testNullResource() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(MOCK_CFG, null, LANG_ID, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that "resource" element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			String xmlTag = "resource"; // LanguageSettingsSerializable.ELEM_RESOURCE;
			assertFalse(xmlString.contains(xmlTag));
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(MOCK_CFG, null, LANG_ID);
			assertEquals(original.get(0), retrieved.get(0));
		}
	}

	/**
	 */
	public void testNullConfigurationLanguage() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(null, MOCK_RC, null, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("configuration")); // LanguageSettingsSerializable.ELEM_CONFIGURATION;
			assertFalse(xmlString.contains("language")); // LanguageSettingsSerializable.ELEM_LANGUAGE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, MOCK_RC, null);
			assertEquals(original.get(0), retrieved.get(0));
		}
	}

	/**
	 */
	public void testNullConfigurationResource() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, LANG_ID, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("configuration")); // LanguageSettingsSerializable.ELEM_CONFIGURATION;
			assertFalse(xmlString.contains("resource")); // LanguageSettingsSerializable.ELEM_RESOURCE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, LANG_ID);
			assertEquals(original.get(0), retrieved.get(0));
		}
	}

	/**
	 */
	public void testNullLanguageResource() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(MOCK_CFG, null, null, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("language")); // LanguageSettingsSerializable.ELEM_LANGUAGE;
			assertFalse(xmlString.contains("resource")); // LanguageSettingsSerializable.ELEM_RESOURCE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(MOCK_CFG, null, null);
			assertEquals(original.get(0), retrieved.get(0));
		}
	}

	/**
	 */
	public void testNullConfigurationLanguageResourceFlag() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		int flag = 0;
		original.add(new CIncludePathEntry("path0", flag));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("configuration")); // LanguageSettingsSerializable.ELEM_CONFIGURATION;
			assertFalse(xmlString.contains("language")); // LanguageSettingsSerializable.ELEM_LANGUAGE;
			assertFalse(xmlString.contains("resource")); // LanguageSettingsSerializable.ELEM_RESOURCE;
			assertFalse(xmlString.contains("flag")); // LanguageSettingsSerializable.ELEM_FLAG;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertEquals(original.get(0), retrieved.get(0));
		}
	}

	/**
	 */
	public void testCIncludePathEntry() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			ICLanguageSettingEntry entry = retrieved.get(0);
			assertTrue(entry instanceof CIncludePathEntry);

			CIncludePathEntry includePathEntry = (CIncludePathEntry)entry;
			assertEquals(original.get(0).getName(), includePathEntry.getName());
			assertEquals(original.get(0).getValue(), includePathEntry.getValue());
			assertEquals(original.get(0).getKind(), includePathEntry.getKind());
			assertEquals(original.get(0).getFlags(), includePathEntry.getFlags());
			assertEquals(original.get(0), includePathEntry);
		}
	}

	/**
	 */
	public void testCIncludeFileEntry() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludeFileEntry("name", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			ICLanguageSettingEntry entry = retrieved.get(0);
			assertTrue(entry instanceof CIncludeFileEntry);
			CIncludeFileEntry includeFileEntry = (CIncludeFileEntry)entry;
			assertEquals(original.get(0).getName(), includeFileEntry.getName());
			assertEquals(original.get(0).getValue(), includeFileEntry.getValue());
			assertEquals(original.get(0).getKind(), includeFileEntry.getKind());
			assertEquals(original.get(0).getFlags(), includeFileEntry.getFlags());
			assertEquals(original.get(0), includeFileEntry);
		}
	}

	/**
	 */
	public void testCMacroEntry() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CMacroEntry("MACRO0", "value0",1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			ICLanguageSettingEntry entry = retrieved.get(0);
			assertTrue(entry instanceof CMacroEntry);
			CMacroEntry macroEntry = (CMacroEntry)entry;
			assertEquals(original.get(0).getName(), macroEntry.getName());
			assertEquals(original.get(0).getValue(), macroEntry.getValue());
			assertEquals(original.get(0).getKind(), macroEntry.getKind());
			assertEquals(original.get(0).getFlags(), macroEntry.getFlags());
			assertEquals(original.get(0), macroEntry);
		}
	}

	/**
	 */
	public void testCMacroFileEntry() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CMacroFileEntry("name", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			ICLanguageSettingEntry entry = retrieved.get(0);
			assertTrue(entry instanceof CMacroFileEntry);
			CMacroFileEntry macroFileEntry = (CMacroFileEntry)entry;
			assertEquals(original.get(0).getName(), macroFileEntry.getName());
			assertEquals(original.get(0).getValue(), macroFileEntry.getValue());
			assertEquals(original.get(0).getKind(), macroFileEntry.getKind());
			assertEquals(original.get(0).getFlags(), macroFileEntry.getFlags());
			assertEquals(original.get(0), macroFileEntry);
		}
	}

	/**
	 */
	public void testCLibraryPathEntry() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CLibraryPathEntry("name", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			ICLanguageSettingEntry entry = retrieved.get(0);
			assertTrue(entry instanceof CLibraryPathEntry);
			CLibraryPathEntry libraryPathEntry = (CLibraryPathEntry)entry;
			assertEquals(original.get(0).getName(), libraryPathEntry.getName());
			assertEquals(original.get(0).getValue(), libraryPathEntry.getValue());
			assertEquals(original.get(0).getKind(), libraryPathEntry.getKind());
			assertEquals(original.get(0).getFlags(), libraryPathEntry.getFlags());
			assertEquals(original.get(0), libraryPathEntry);
		}
	}

	/**
	 */
	public void testCLibraryFileEntry() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CLibraryFileEntry("name", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			ICLanguageSettingEntry entry = retrieved.get(0);
			assertTrue(entry instanceof CLibraryFileEntry);
			CLibraryFileEntry libraryFileEntry = (CLibraryFileEntry)entry;
			assertEquals(original.get(0).getName(), libraryFileEntry.getName());
			assertEquals(original.get(0).getValue(), libraryFileEntry.getValue());
			assertEquals(original.get(0).getKind(), libraryFileEntry.getKind());
			assertEquals(original.get(0).getFlags(), libraryFileEntry.getFlags());
			assertEquals(original.get(0), libraryFileEntry);
		}
	}

	/**
	 */
	public void testMixedSettingEntries() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CMacroEntry("MACRO0", "value0",1));
		original.add(new CIncludePathEntry("path0", 1));
		original.add(new CIncludePathEntry("path1", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.get(1), retrieved.get(1));
			assertEquals(original.get(2), retrieved.get(2));
			assertEquals(original.size(), retrieved.size());
		}
	}

	/**
	 */
	public void testConfigurationAndNull() throws Exception {
		ICConfigurationDescription mockCfgDescription = new CProjectDescriptionTestHelper.DummyCConfigurationDescription(CFG_ID);
		Element elementProvider = null;

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("path2", 0));

		{
			// create a provider
			LanguageSettingsSerializable mockProvider = null;
			mockProvider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			mockProvider.setSettingEntries(null, null, null, original);
			mockProvider.setSettingEntries(mockCfgDescription, null, null, original2);

			// serialize language settings to DOM
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = mockProvider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable loadedProvider = new LanguageSettingsSerializable(elementProvider);

			List<ICLanguageSettingEntry> retrieved = loadedProvider.getSettingEntries(null, null, null);
			if (retrieved==null) {
				String xml = XmlUtil.toString(elementProvider.getOwnerDocument());
				/*
				// This occasionally fails with following xml:
				<?xml version="1.0" encoding="UTF-8"?>
				<test>
					<provider class="org.eclipse.cdt.core.settings.model.LanguageSettingsSerializable" id="test.provider.0.id" name="test.provider.0.name">
						<configuration id="test.configuration.id">
							<entry kind="includePath" name="path2"/>
							<entry kind="includePath" name="path0"/>
						</configuration>
					</provider>
				</test>
				 */
				fail(xml);
			}
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());

			List<ICLanguageSettingEntry> retrieved2 = loadedProvider.getSettingEntries(mockCfgDescription, null, null);
			assertEquals(original2.get(0), retrieved2.get(0));
			assertEquals(original2.size(), retrieved2.size());
//			String xml = XmlUtil.toString(elementProvider.getOwnerDocument());
			/*
			// The correct xml looks like that:
			<?xml version="1.0" encoding="UTF-8"?>
			<test>
				<provider class="org.eclipse.cdt.core.settings.model.LanguageSettingsSerializable" id="test.provider.0.id" name="test.provider.0.name">
					<entry kind="includePath" name="path0"/>
					<configuration id="test.configuration.id">
						<entry kind="includePath" name="path2"/>
					</configuration>
				</provider>
			</test>
			 */
		}
	}

	/**
	 */
	public void testTwoConfigurations() throws Exception {
		ICConfigurationDescription cfgDescription_1 = new CProjectDescriptionTestHelper.DummyCConfigurationDescription(CFG_ID_2);
		ICConfigurationDescription cfgDescription_2 = new CProjectDescriptionTestHelper.DummyCConfigurationDescription(CFG_ID_1);
		Element elementProvider = null;

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("path2", 0));

		{
			// create a provider
			LanguageSettingsSerializable mockProvider = null;
			mockProvider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			mockProvider.setSettingEntries(cfgDescription_1, null, null, original);
			mockProvider.setSettingEntries(cfgDescription_2, null, null, original2);

			// serialize language settings to DOM
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = mockProvider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable loadedProvider = new LanguageSettingsSerializable(elementProvider);

			List<ICLanguageSettingEntry> retrieved = loadedProvider.getSettingEntries(cfgDescription_1, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());

			List<ICLanguageSettingEntry> retrieved2 = loadedProvider.getSettingEntries(cfgDescription_2, null, null);
			assertEquals(original2.get(0), retrieved2.get(0));
			assertEquals(original2.size(), retrieved2.size());
		}
	}

	/**
	 */
	public void testTwoLanguages() throws Exception {
		ICConfigurationDescription cfgDescription = new CProjectDescriptionTestHelper.DummyCConfigurationDescription(CFG_ID);
		Element elementProvider = null;

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("path2", 0));

		{
			// create a provider
			LanguageSettingsSerializable mockProvider = null;
			mockProvider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			mockProvider.setSettingEntries(cfgDescription, null, LANG_ID_1, original);
			mockProvider.setSettingEntries(cfgDescription, null, LANG_ID_2, original2);

			// serialize language settings to DOM
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = mockProvider.serialize(rootElement);
			String xml = XmlUtil.toString(elementProvider.getOwnerDocument());
//			fail(xml); // for debugging
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable loadedProvider = new LanguageSettingsSerializable(elementProvider);

			List<ICLanguageSettingEntry> retrieved = loadedProvider.getSettingEntries(cfgDescription, null, LANG_ID_1);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());

			List<ICLanguageSettingEntry> retrieved2 = loadedProvider.getSettingEntries(cfgDescription, null, LANG_ID_2);
			assertEquals(original2.get(0), retrieved2.get(0));
			assertEquals(original2.size(), retrieved2.size());
		}
	}

	/**
	 */
	public void testTwoResources() throws Exception {
		ICConfigurationDescription cfgDescription = new CProjectDescriptionTestHelper.DummyCConfigurationDescription(CFG_ID);
		// Create resources
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		IFile rc1 = ResourceHelper.createFile(project, "rc1");
		assertNotNull(rc1);
		IFile rc2 = ResourceHelper.createFile(project, "rc2");
		assertNotNull(rc2);
		assertFalse(rc1.getFullPath().equals(rc2.getFullPath()));

		Element elementProvider = null;

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("path2", 0));

		{
			// create a provider
			LanguageSettingsSerializable mockProvider = null;
			mockProvider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			mockProvider.setSettingEntries(cfgDescription, rc1, null, original);
			mockProvider.setSettingEntries(cfgDescription, rc2, null, original2);

			// serialize language settings to DOM
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = mockProvider.serialize(rootElement);
			String xml = XmlUtil.toString(elementProvider.getOwnerDocument());
//			fail(xml); // for debugging
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable loadedProvider = new LanguageSettingsSerializable(elementProvider);

			List<ICLanguageSettingEntry> retrieved = loadedProvider.getSettingEntries(cfgDescription, rc1, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());

			List<ICLanguageSettingEntry> retrieved2 = loadedProvider.getSettingEntries(cfgDescription, rc2, null);
			assertEquals(original2.get(0), retrieved2.get(0));
			assertEquals(original2.size(), retrieved2.size());
		}
	}

	/**
	 */
	public void testParentFolder() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		ICConfigurationDescription mockCfgDescription = new CProjectDescriptionTestHelper.DummyCConfigurationDescription(CFG_ID);

		// Create resources
		final IFolder parentFolder = ResourceHelper.createFolder(project, "/ParentFolder/");
		assertNotNull(parentFolder);
		final IFile emptySettingsPath = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/empty");
		assertNotNull(emptySettingsPath);

		// Create provider
		LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);

		// store the entries in parent folder
		final List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		provider.setSettingEntries(mockCfgDescription, parentFolder, LANG_ID, original);
		provider.setSettingEntries(mockCfgDescription, emptySettingsPath, LANG_ID, new ArrayList<ICLanguageSettingEntry>());

		{
			// retrieve entries for a parent folder itself
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(mockCfgDescription, parentFolder, LANG_ID);
			assertEquals(original,retrieved);
			assertEquals(original.size(), retrieved.size());
		}

		{
			// retrieve entries for a derived resource (in a subfolder)
			IFile derived = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/resource");
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(mockCfgDescription, derived, LANG_ID);
			// NOT taken from parent folder
			assertEquals(null,retrieved);
		}

		{
			// retrieve entries for not related resource
			IFile notRelated = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/AnotherFolder/Subfolder/resource"));
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(mockCfgDescription, notRelated, LANG_ID);
			assertEquals(null,retrieved);
		}

		{
			// test distinction between no settings and empty settings
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(mockCfgDescription, emptySettingsPath, LANG_ID);
			// NOT taken from parent folder and not null
			assertEquals(0, retrieved.size());
		}
	}

	/**
	 */
	public void testEqualsAndClone() throws Exception {
		List<ICLanguageSettingEntry> original1 = new ArrayList<ICLanguageSettingEntry>();
		original1.add(new CMacroEntry("MACRO0", "value0",1));
		original1.add(new CIncludePathEntry("path0", 1));
		original1.add(new CIncludePathEntry("path1", 1));

		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("path0", 1));

		// create a model provider
		LanguageSettingsSerializable provider1 = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
		provider1.setSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID, original1);
		provider1.setSettingEntries(null, null, LANG_ID, original2);

		{
			// clone provider
			LanguageSettingsSerializable providerClone = provider1.clone();
			assertNotSame(provider1, providerClone);
			assertTrue(provider1.equals(providerClone));
			assertTrue(provider1.getClass()==providerClone.getClass());

			List<ICLanguageSettingEntry> retrieved1 = providerClone.getSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID);
			assertNotSame(original1, retrieved1);
			assertEquals(original1.get(0), retrieved1.get(0));
			assertEquals(original1.get(1), retrieved1.get(1));
			assertEquals(original1.get(2), retrieved1.get(2));
			assertEquals(original1.size(), retrieved1.size());

			List<ICLanguageSettingEntry> retrieved2 = providerClone.getSettingEntries(null, null, LANG_ID);
			assertNotSame(original2, retrieved2);
			assertEquals(original2.get(0), retrieved2.get(0));
			assertEquals(original2.size(), retrieved2.size());
		}

		{
			// create another provider with the same data
			LanguageSettingsSerializable provider2 = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_0);
			assertFalse(provider1.equals(provider2));

			provider2.setSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID, original1);
			assertFalse(provider1.equals(provider2));
			provider2.setSettingEntries(null, null, LANG_ID, original2);
			assertTrue(provider1.equals(provider2));
			assertTrue(provider1.hashCode()==provider2.hashCode());

			// check different ID
			provider2.setId(PROVIDER_2);
			assertFalse(provider1.equals(provider2));
			assertFalse(provider1.hashCode()==provider2.hashCode());
		}

		{
			// check that subclasses are not equal
			LanguageSettingsSerializable providerSub1 = new LanguageSettingsSerializable() {};
			LanguageSettingsSerializable providerSub2 = new LanguageSettingsSerializable() {};
			assertFalse(providerSub1.equals(providerSub2));
			assertFalse(providerSub1.hashCode()==providerSub2.hashCode());
		}
	}

}

