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
import java.util.Arrays;
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsSerializableTests extends TestCase {
	// Should match id of extension point defined in plugin.xml
	private static final String EXTENSION_PROVIDER_ID = "org.eclipse.cdt.core.tests.language.settings.base.provider.subclass";

	private static final IFile FILE_0 = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/project/path0"));
	private static final String LANG_ID = "test.lang.id";
	private static final String CFG_ID = "test.configuration.id";
	private static final String PROVIDER_NULL = "test.provider.null.id";
	private static final String PROVIDER_0 = "test.provider.0.id";
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
		LanguageSettingsSerializable mockProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
		// test setter and getter
		mockProvider.setSettingEntries(null, FILE_0, LANG_ID, original);
		List<ICLanguageSettingEntry> retrieved = mockProvider.getSettingEntries(null, FILE_0, LANG_ID);
		assertEquals(original.get(0), retrieved.get(0));
		assertEquals(original.size(), retrieved.size());
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
		LanguageSettingsExtensionManager.serializeLanguageSettings();
		LanguageSettingsExtensionManager.loadLanguageSettingsWorkspace();
		
		// test passes if no exception was thrown
	}

	/**
	 */
	public void testEmptyProvider() throws Exception {
		{
			// create null provider
			LanguageSettingsSerializable providerNull = new LanguageSettingsSerializable(PROVIDER_NULL, PROVIDER_NAME_NULL);
			assertNull(providerNull.getSettingEntries(null, null, null));
			// set and get null entries
			providerNull.setSettingEntries(null, null, null, null);
			assertNull(providerNull.getSettingEntries(null, null, null));

			// assign provider to workspace
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] {providerNull});
			String[] retrievedIds = LanguageSettingsManager.getProviderAvailableIds();
			// user defined providers are always before extension providers
			assertEquals(PROVIDER_NULL, retrievedIds[0]);
		}
		{
			// serialize language settings of user defined providers (on workspace level)
			LanguageSettingsExtensionManager.serializeLanguageSettings();
			LanguageSettingsExtensionManager.loadLanguageSettingsWorkspace();
		}
		
		{
			// read language settings of the provider
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(PROVIDER_NULL);
			assertEquals(PROVIDER_NULL, provider.getId());
			assertNull(provider.getSettingEntries(null, null, null));
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
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] {provider});
			assertNotNull(LanguageSettingsManager.getWorkspaceProvider(PROVIDER_0));
			
			Document doc = XmlUtil.newDocument();
			Element rootElement = doc.createElement(ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_0, provider.getId());
			
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
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			
			Document doc = XmlUtil.newDocument();
			Element rootElement = doc.createElement(ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_0, provider.getId());
			
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
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			
			Document doc = XmlUtil.newDocument();
			Element rootElement = doc.createElement(ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_0, provider.getId());
			
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
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			
			Document doc = XmlUtil.newDocument();
			Element rootElement = doc.createElement(ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_0, provider.getId());
			
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
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			
			Document doc = XmlUtil.newDocument();
			Element rootElement = doc.createElement(ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_0, provider.getId());
			
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
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			
			Document doc = XmlUtil.newDocument();
			Element rootElement = doc.createElement(ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_0, provider.getId());
			
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
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			
			Document doc = XmlUtil.newDocument();
			Element rootElement = doc.createElement(ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_0, provider.getId());
			
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.get(1), retrieved.get(1));
			assertEquals(original.get(2), retrieved.get(2));
			assertEquals(original.size(), retrieved.size());
		}
	}
	
	/**
	 */
	public void testTwoConfigurations() throws Exception {
		ICConfigurationDescription mockCfgDescription = new CProjectDescriptionTestHelper.DummyCConfigurationDescription(CFG_ID);
		Element elementProvider = null;

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("path2", 0));
		
		{
			// create a provider
			LanguageSettingsSerializable mockProvider = null;
			mockProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			mockProvider.setSettingEntries(null, null, null, original);
			mockProvider.setSettingEntries(mockCfgDescription, null, null, original2);
			
			// serialize language settings to DOM
			Document doc = XmlUtil.newDocument();
			Element rootElement = doc.createElement(ELEM_TEST);
			elementProvider = mockProvider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable loadedProvider = new LanguageSettingsSerializable(elementProvider);
			
			List<ICLanguageSettingEntry> retrieved = loadedProvider.getSettingEntries(null, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
			
			List<ICLanguageSettingEntry> retrieved2 = loadedProvider.getSettingEntries(mockCfgDescription, null, null);
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
		LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);

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
	

}
