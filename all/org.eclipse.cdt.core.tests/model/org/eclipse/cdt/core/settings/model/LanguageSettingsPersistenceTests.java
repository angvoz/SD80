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

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescription;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsExtensionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsPersistenceTests extends TestCase {
	private static final IFile FILE_0 = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/project/path0"));
	private static final String LANG_ID = "test.lang.id";
	private static final String PROVIDER_NULL = "test.provider.null.id";
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_NAME_NULL = "test.provider.null.name";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";

	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsPersistenceTests(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
		LanguageSettingsManager.setUserDefinedProviders(null);
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(LanguageSettingsPersistenceTests.class);
	}

	/**
	 * main function of the class.
	 *
	 * @param args - arguments
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
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

	/**
	 */
	public void testProvider() throws Exception {
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		
		// create a provider
		LanguageSettingsPersistentProvider mockProvider = new LanguageSettingsPersistentProvider(PROVIDER_0, PROVIDER_NAME_0);
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
		LanguageSettingsExtensionManager.loadLanguageSettings();
		
		// test passes if no exception was thrown
	}

	/**
	 */
	public void testEmptyProvider() throws Exception {
		{
			// create null provider
			LanguageSettingsPersistentProvider providerNull = new LanguageSettingsPersistentProvider(PROVIDER_NULL, PROVIDER_NAME_NULL);
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
			LanguageSettingsExtensionManager.loadLanguageSettings();
		}
		
		{
			// read language settings of the provider
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_NULL);
			assertEquals(PROVIDER_NULL, provider.getId());
			assertNull(provider.getSettingEntries(null, null, null));
		}
	}
	
	/**
	 */
	public void testBasicPersistence() throws Exception {
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		
		LanguageSettingsPersistentProvider mockProvider = null;
		{
			// create a provider
			mockProvider = new LanguageSettingsPersistentProvider(PROVIDER_0, PROVIDER_NAME_0);
			mockProvider.setSettingEntries(null, null, null, original);
			
			// assign provider to workspace
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] {mockProvider});
			String[] retrievedIds = LanguageSettingsManager.getProviderAvailableIds();
			// user defined providers are always before extension providers
			assertEquals(PROVIDER_0, retrievedIds[0]);
		}
		{
			// serialize language settings of user defined providers (on workspace level)
			LanguageSettingsExtensionManager.serializeLanguageSettings();
			// clear the provider
			mockProvider.setSettingEntries(null, null, null, null);
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertNull(retrieved);
		}
		{
			// re-load and check language settings of the provider
			LanguageSettingsExtensionManager.loadLanguageSettings();
			
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
			assertEquals(PROVIDER_0, provider.getId());
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
	}
	
	/**
	 */
	public void testCIncludePathEntry() throws Exception {
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsPersistentProvider provider = new LanguageSettingsPersistentProvider(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] {provider});
			assertNotNull(LanguageSettingsManager.getProvider(PROVIDER_0));
			LanguageSettingsExtensionManager.serializeLanguageSettings();
			// clear the provider
			provider.setSettingEntries(null, null, null, null);
		}
		{
			// doublecheck that provider is unloaded
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertNull(retrieved);
		}
		{
			// re-load and check language settings of the provider
			LanguageSettingsExtensionManager.loadLanguageSettings();
			
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
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
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludeFileEntry("name", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsPersistentProvider provider = new LanguageSettingsPersistentProvider(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] {provider});
			assertNotNull(LanguageSettingsManager.getProvider(PROVIDER_0));
			LanguageSettingsExtensionManager.serializeLanguageSettings();
			// clear the provider
			provider.setSettingEntries(null, null, null, null);
		}
		{
			// doublecheck that provider is unloaded
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertNull(retrieved);
		}
		{
			// re-load and check language settings of the provider
			LanguageSettingsExtensionManager.loadLanguageSettings();
			
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
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
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CMacroEntry("MACRO0", "value0",1));
		{
			// create a provider and serialize its settings
			LanguageSettingsPersistentProvider provider = new LanguageSettingsPersistentProvider(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] {provider});
			assertNotNull(LanguageSettingsManager.getProvider(PROVIDER_0));
			LanguageSettingsExtensionManager.serializeLanguageSettings();
			// clear the provider
			provider.setSettingEntries(null, null, null, null);
		}
		{
			// doublecheck that provider is unloaded
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertNull(retrieved);
		}
		{
			// re-load and check language settings of the provider
			LanguageSettingsExtensionManager.loadLanguageSettings();
			
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
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
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CMacroFileEntry("name", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsPersistentProvider provider = new LanguageSettingsPersistentProvider(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] {provider});
			assertNotNull(LanguageSettingsManager.getProvider(PROVIDER_0));
			LanguageSettingsExtensionManager.serializeLanguageSettings();
			// clear the provider
			provider.setSettingEntries(null, null, null, null);
		}
		{
			// doublecheck that provider is unloaded
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertNull(retrieved);
		}
		{
			// re-load and check language settings of the provider
			LanguageSettingsExtensionManager.loadLanguageSettings();
			
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
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
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CLibraryPathEntry("name", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsPersistentProvider provider = new LanguageSettingsPersistentProvider(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] {provider});
			assertNotNull(LanguageSettingsManager.getProvider(PROVIDER_0));
			LanguageSettingsExtensionManager.serializeLanguageSettings();
			// clear the provider
			provider.setSettingEntries(null, null, null, null);
		}
		{
			// doublecheck that provider is unloaded
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertNull(retrieved);
		}
		{
			// re-load and check language settings of the provider
			LanguageSettingsExtensionManager.loadLanguageSettings();
			
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
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
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CLibraryFileEntry("name", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsPersistentProvider provider = new LanguageSettingsPersistentProvider(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] {provider});
			assertNotNull(LanguageSettingsManager.getProvider(PROVIDER_0));
			LanguageSettingsExtensionManager.serializeLanguageSettings();
			// clear the provider
			provider.setSettingEntries(null, null, null, null);
		}
		{
			// doublecheck that provider is unloaded
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertNull(retrieved);
		}
		{
			// re-load and check language settings of the provider
			LanguageSettingsExtensionManager.loadLanguageSettings();
			
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
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
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CMacroEntry("MACRO0", "value0",1));
		original.add(new CIncludePathEntry("path0", 1));
		original.add(new CIncludePathEntry("path1", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsPersistentProvider provider = new LanguageSettingsPersistentProvider(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(null, null, null, original);
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] {provider});
			assertNotNull(LanguageSettingsManager.getProvider(PROVIDER_0));
			LanguageSettingsExtensionManager.serializeLanguageSettings();
			// clear the provider
			provider.setSettingEntries(null, null, null, null);
		}
		{
			// doublecheck that provider is unloaded
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertNull(retrieved);
		}
		{
			// re-load and check language settings of the provider
			LanguageSettingsExtensionManager.loadLanguageSettings();
			
			ILanguageSettingsProvider provider = LanguageSettingsManager.getProvider(PROVIDER_0);
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
	public void testConfigurationProvider() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));

		LanguageSettingsPersistentProvider provider = new LanguageSettingsPersistentProvider(PROVIDER_0, PROVIDER_NAME_0);
		provider.setSettingEntries(cfgDescription, null, null, original);
		provider.setSettingEntries(cfgDescription, FILE_0, LANG_ID, original);

		// add mock serializable provider
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		LanguageSettingsManager.setProviders(cfgDescription, providers);

		{
			// 1st double-check that provider returns proper data
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntries(cfgDescription, PROVIDER_0, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}

		{
			// 2nd double-check that provider returns proper data
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntries(cfgDescription, PROVIDER_0, FILE_0, LANG_ID);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
		
		// serialize
		LanguageSettingsManager.serialize(cfgDescription);

		// clear provider
		provider.setSettingEntries(cfgDescription, null, null, null);
		provider.setSettingEntries(cfgDescription, FILE_0, LANG_ID, null);
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntries(cfgDescription, PROVIDER_0, null, null);
			assertEquals(0, retrieved.size());
		}
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntries(cfgDescription, PROVIDER_0, FILE_0, LANG_ID);
			assertEquals(0, retrieved.size());
		}

		// re-load
		LanguageSettingsManager.load(cfgDescription);
		{
			// 1st
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntries(cfgDescription, PROVIDER_0, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
		{
			// 2nd
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntries(cfgDescription, PROVIDER_0, FILE_0, LANG_ID);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
		
		// serialize over existing file (no exception expected)
		LanguageSettingsManager.serialize(cfgDescription);
	}

	/**
	 */
	public void testParentFolder() throws Exception {
		// Create model project and accompanied descriptions
		ICProject cproject = CProjectHelper.createNewStileCProject(getName(), IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		// Create resources
		IProject project = cproject.getProject();
		final IFolder parentFolder = ResourceHelper.createFolder(project, "/ParentFolder/");
		assertNotNull(parentFolder);
		final IFile emptySettingsPath = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/empty");
		assertNotNull(emptySettingsPath);

		// Create provider
		LanguageSettingsPersistentProvider provider = new LanguageSettingsPersistentProvider(PROVIDER_0, PROVIDER_NAME_0);

		// store the entries in parent folder
		final List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		provider.setSettingEntries(cfgDescription, parentFolder, LANG_ID, original);
		provider.setSettingEntries(cfgDescription, emptySettingsPath, LANG_ID, new ArrayList<ICLanguageSettingEntry>());

		{
			// retrieve entries for a parent folder itself
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(cfgDescription, parentFolder, LANG_ID);
			assertEquals(original,retrieved);
			assertEquals(original.size(), retrieved.size());
		}

		{
			// retrieve entries for a derived resource (in a subfolder)
			IFile derived = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/resource");
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(cfgDescription, derived, LANG_ID);
			// NOT taken from parent folder
			assertEquals(null,retrieved);
		}
		
		{
			// retrieve entries for not related resource
			IFile notRelated = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/AnotherFolder/Subfolder/resource"));
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(cfgDescription, notRelated, LANG_ID);
			assertEquals(null,retrieved);
		}

		{
			// test distinction between no settings and empty settings
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(cfgDescription, emptySettingsPath, LANG_ID);
			// NOT taken from parent folder and not null
			assertEquals(0, retrieved.size());
		}
	}

}
