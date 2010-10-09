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

package org.eclipse.cdt.core.language.settings.providers;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.CModelMock;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsManagerTests extends TestCase {
	// Should match id of extension point defined in plugin.xml
	private static final String PROVIDER_ID_EXT = "org.eclipse.cdt.core.tests.language.settings.base.provider";

	private static final IFile FILE_0 = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/project/path0"));
	private static final String CFG_ID = "test.configuration.id";
	private static final String LANG_ID = "test.lang.id";
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_1 = "test.provider.1.id";
	private static final String PROVIDER_2 = "test.provider.2.id";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";
	private static final String PROVIDER_NAME_1 = "test.provider.1.name";
	private static final String PROVIDER_NAME_2 = "test.provider.2.name";

	class MockConfigurationDescription extends CModelMock.DummyCConfigurationDescription {
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		public MockConfigurationDescription(String id) {
			super(id);
		}

		@Override
		public void setLanguageSettingProviders(List<ILanguageSettingsProvider> providers) {
			this.providers = new ArrayList<ILanguageSettingsProvider>(providers);
		}

		@Override
		public List<ILanguageSettingsProvider> getLanguageSettingProviders() {
			return providers;
		}
	}

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
	public LanguageSettingsManagerTests(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
		LanguageSettingsManager_TBD.setUserDefinedProviders(null);
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(LanguageSettingsManagerTests.class);
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
	 * Test setting/retrieval of providers and their IDs.
	 *
	 * @throws Exception...
	 */
	public void testWorkspaceProviders() throws Exception {
		// Common variables
		List<ILanguageSettingsProvider> availableProviders = LanguageSettingsManager.getWorkspaceProviders();
		assertNotNull(availableProviders);
		assertTrue(availableProviders.size()>0);

		ILanguageSettingsProvider firstProvider = availableProviders.get(0);
		assertNotNull(firstProvider);
		String firstId = firstProvider.getId();
		String firstName = firstProvider.getName();

		// Define mock providers
		ILanguageSettingsProvider mockProvider1 = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, null);
		String firstNewName = firstName + " new";
		ILanguageSettingsProvider mockProvider2 = new MockProvider(firstId, firstNewName, null);

		// Preconditions
		{
			
			List<ILanguageSettingsProvider> workspaceProviders = LanguageSettingsManager.getWorkspaceProviders();
			assertFalse(workspaceProviders.contains(mockProvider1));
			assertFalse(workspaceProviders.contains(mockProvider2));

			assertNull(LanguageSettingsManager.getWorkspaceProvider(PROVIDER_1));
			assertFalse(LanguageSettingsManager.isWorkspaceProvider(mockProvider1));

			ILanguageSettingsProvider provider2 = LanguageSettingsManager.getWorkspaceProvider(firstId);
			assertNotNull(provider2);
			assertEquals(firstProvider, provider2);
			assertTrue(workspaceProviders.contains(provider2));
		}

		// set available providers
		{
			LanguageSettingsManager_TBD.setUserDefinedProviders(new ILanguageSettingsProvider[] {
					// add brand new one
					mockProvider1,
					// override extension with another one
					mockProvider2,
			});
			
			List<ILanguageSettingsProvider> workspaceProviders = LanguageSettingsManager.getWorkspaceProviders();
			assertTrue(workspaceProviders.contains(mockProvider1));
			assertTrue(workspaceProviders.contains(mockProvider2));

			ILanguageSettingsProvider retrieved1 = LanguageSettingsManager.getWorkspaceProvider(PROVIDER_1);
			assertNotNull(retrieved1);
			assertEquals(PROVIDER_NAME_1, retrieved1.getName());
			assertEquals(mockProvider1, retrieved1);
			assertTrue(LanguageSettingsManager.isWorkspaceProvider(mockProvider1));

			ILanguageSettingsProvider retrieved2 = LanguageSettingsManager.getWorkspaceProvider(firstId);
			assertNotNull(retrieved2);
			assertEquals(firstNewName, retrieved2.getName());
			assertEquals(mockProvider2, retrieved2);
		}
		// reset available providers
		{
			LanguageSettingsManager_TBD.setUserDefinedProviders(null);
			
			List<ILanguageSettingsProvider> workspaceProviders = LanguageSettingsManager.getWorkspaceProviders();
			assertFalse(workspaceProviders.contains(mockProvider1));
			assertFalse(workspaceProviders.contains(mockProvider2));

			assertNull(LanguageSettingsManager.getWorkspaceProvider(PROVIDER_1));

			ILanguageSettingsProvider provider2 = LanguageSettingsManager.getWorkspaceProvider(firstId);
			assertNotNull(provider2);
			assertEquals(firstProvider, provider2);
			assertTrue(workspaceProviders.contains(provider2));
		}
	}

	/**
	 * Test setting/retrieval of user defined providers.
	 *
	 * @throws Exception...
	 */
	public void testUserDefinedProviders() throws Exception {
		// reset providers
		LanguageSettingsManager_TBD.setUserDefinedProviders(null);
		int size = LanguageSettingsManager.getWorkspaceProviders().size();
		assertTrue(size > 0);
		assertNull(LanguageSettingsManager.getWorkspaceProvider(PROVIDER_0));
		
		LanguageSettingsManager_TBD.setUserDefinedProviders(new ILanguageSettingsProvider[] {
				new MockProvider(PROVIDER_0, PROVIDER_NAME_0, null),
		});
		assertNotNull(LanguageSettingsManager.getWorkspaceProvider(PROVIDER_0));

		List<ILanguageSettingsProvider> allProviders = LanguageSettingsManager.getWorkspaceProviders();
		assertEquals(size+1, allProviders.size());
		assertEquals(PROVIDER_0, allProviders.get(0).getId());
	}

	/**
	 */
	public void testConfigurationDescription_NullProvider() throws Exception {
		ICConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);
		// set rough provider returning null with getSettingEntries()
		ILanguageSettingsProvider providerNull = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, null);
		{
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerNull);
			cfgDescription.setLanguageSettingProviders(providers);
		}

		// use provider returning null
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(providerNull, cfgDescription, FILE_0, LANG_ID);
			assertNotNull(retrieved);
			assertEquals(0, retrieved.size());
		}
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, 0);
			assertNotNull(retrieved);
			assertEquals(0, retrieved.size());
		}

		// set rough provider returning null in getSettingEntries() array
		ILanguageSettingsProvider providerNull_2 = new MockProvider(PROVIDER_2, PROVIDER_NAME_2,
			new ArrayList<ICLanguageSettingEntry>() {
				{ // init via static initializer
					add(null);
				}
			});

		{
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerNull);
			cfgDescription.setLanguageSettingProviders(providers);
		}

		// use provider returning null as item in array
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(providerNull_2, cfgDescription, FILE_0, LANG_ID);
			assertNotNull(retrieved);
			assertEquals(1, retrieved.size());
		}
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, 0);
			assertNotNull(retrieved);
			assertEquals(0, retrieved.size());
		}
	}

	/**
	 * TODO .
	 *
	 * @throws Exception...
	 */
	public void testConfigurationDescription_Use() throws Exception {
		final ICConfigurationDescription modelCfgDescription = new MockConfigurationDescription(CFG_ID);

		final List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));

		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		ILanguageSettingsProvider providerYes = new MockProvider(PROVIDER_0, PROVIDER_NAME_0, null)  {
			@Override
			public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
				if (cfgDescription.getId().equals(modelCfgDescription.getId())) {
					return original;
				}
				return null;
			}

		};
		providers.add(providerYes);
		ILanguageSettingsProvider providerNo = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, null)  {
			@Override
			public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
				if (cfgDescription.getId().equals(modelCfgDescription.getId())) {
					return null;
				}
				return original;
			}

		};
		providers.add(providerNo);
		modelCfgDescription.setLanguageSettingProviders(providers);

		{
			// retrieve the entries for model configuration description
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(providerYes, modelCfgDescription, FILE_0, LANG_ID);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}

		{
			// retrieve the entries for different configuration description
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(providerNo, modelCfgDescription, FILE_0, LANG_ID);
			assertEquals(0, retrieved.size());
		}
	}

	/**
	 * TODO .
	 *
	 * @throws Exception...
	 */
	public void testConfigurationDescription_Providers() throws Exception {
		final ICConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);

		// set providers
		ILanguageSettingsProvider provider1 = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, null);
		ILanguageSettingsProvider provider2 = new MockProvider(PROVIDER_2, PROVIDER_NAME_2, null);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider1);
		providers.add(provider2);
		cfgDescription.setLanguageSettingProviders(providers);

		{
			// get providers
			List<ILanguageSettingsProvider> retrieved = cfgDescription.getLanguageSettingProviders();
			assertEquals(provider1, retrieved.get(0));
			assertEquals(provider2, retrieved.get(1));
			assertEquals(providers.size(), retrieved.size());
		}
	}

	/**
	 */
	public void testConfigurationDescription_Basic() throws Exception {
		final ICConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);

		// create couple of providers
		final List<ICLanguageSettingEntry> original1 = new ArrayList<ICLanguageSettingEntry>();
		original1.add(new CIncludePathEntry("value1", 1));
		original1.add(new CIncludePathEntry("value2", 2));

		final List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("value1", 1));
		original2.add(new CIncludePathEntry("value2", 2));
		original2.add(new CIncludePathEntry("value3", 2));

		ILanguageSettingsProvider provider1 = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, original1);
		ILanguageSettingsProvider provider2 = new MockProvider(PROVIDER_2, PROVIDER_NAME_2, original2);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider1);
		providers.add(provider2);
		cfgDescription.setLanguageSettingProviders(providers);

		{
			// get list of providers
			List<ILanguageSettingsProvider> all = cfgDescription.getLanguageSettingProviders();
			assertTrue(all.contains(provider1));
			assertTrue(all.contains(provider2));
			assertTrue(all.size()>=2);
		}

		{
			// retrieve the entries for provider-1
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(provider1, cfgDescription, FILE_0, LANG_ID);

			assertNotSame(original1, retrieved);
			assertEquals(original1.size(), retrieved.size());
			ICLanguageSettingEntry[] originalArray = original1.toArray(new ICLanguageSettingEntry[0]);
			ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
			for (int i=0;i<original1.size();i++) {
				assertEquals(originalArray[i], retrievedArray[i]);
			}
		}

		{
			// retrieve the entries for provider-2
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(provider2, cfgDescription, FILE_0, LANG_ID);

			assertNotSame(original2, retrieved);
			assertEquals(original2.size(), retrieved.size());
			ICLanguageSettingEntry[] originalArray = original2.toArray(new ICLanguageSettingEntry[0]);
			ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
			for (int i=0;i<original2.size();i++) {
				assertEquals(originalArray[i], retrievedArray[i]);
			}
		}

		// TODO: for some other provider?
//		// reset the entries for provider-1
//		{
//			original1.clear();
//			original1.add(new CIncludePathEntry("value10", 10));
//			original1.add(new CIncludePathEntry("value20", 20));
//
//			manager.setSettingEntries(RC_DESCRIPTOR, PROVIDER_1, original1);
//			List<ICLanguageSettingEntry> retrieved = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_1);
//
//			assertNotSame(original1, retrieved);
//			assertEquals(original1.size(), retrieved.size());
//			ICLanguageSettingEntry[] originalArray = original1.toArray(new ICLanguageSettingEntry[0]);
//			ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
//			for (int i=0;i<original1.size();i++) {
//				assertEquals(originalArray[i], retrievedArray[i]);
//			}
//		}
//
//		// clear settings for provider-1
//		{
//			manager.removeSettingEntries(RC_DESCRIPTOR, PROVIDER_1);
//			List<ICLanguageSettingEntry> retrieved_1 = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_1);
//			assertEquals(0, retrieved_1.size());
//
//			List<ICLanguageSettingEntry> retrieved_2 = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_2);
//			assertEquals(original2.size(), retrieved_2.size());
//		}
//
//		// add entries to the end
//		{
//			List<ICLanguageSettingEntry> original2a = new ArrayList<ICLanguageSettingEntry>();
//			original2a.add(new CIncludePathEntry("value4a", 1));
//			original2a.add(new CIncludePathEntry("value5a", 2));
//
//			manager.addSettingEntries(RC_DESCRIPTOR, PROVIDER_2, original2a);
//			List<ICLanguageSettingEntry> retrieved = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_2);
//
//			assertEquals(original2.size()+original2a.size(), retrieved.size());
//
//			ICLanguageSettingEntry[] originalArray = original2.toArray(new ICLanguageSettingEntry[0]);
//			ICLanguageSettingEntry[] originalAddedArray = original2a.toArray(new ICLanguageSettingEntry[0]);
//			ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
//			for (int i=0;i<original2.size();i++) {
//				assertEquals(originalArray[i], retrievedArray[i]);
//			}
//			for (int i=0;i<original2a.size();i++) {
//				assertEquals(originalAddedArray[i], retrievedArray[i+original2.size()]);
//			}
//		}
	}

	/**
	 */
	public void testConfigurationDescription_GetByKind() throws Exception {
		final ICConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);

		// contribute the entries
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		original.add(new CMacroEntry("MACRO0", "value0",0));
		original.add(new CIncludePathEntry("path1", 0));
		original.add(new CMacroEntry("MACRO1", "value1",0));
		original.add(new CIncludePathEntry("path2", 0));

		ILanguageSettingsProvider provider0 = new MockProvider(PROVIDER_0, PROVIDER_NAME_0, original);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider0);
		cfgDescription.setLanguageSettingProviders(providers);

		{
			// retrieve entries by kind
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager
				.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
			assertEquals(3, includes.size());
			assertEquals(new CIncludePathEntry("path0", 0),includes.get(0));
			assertEquals(new CIncludePathEntry("path1", 0),includes.get(1));
			assertEquals(new CIncludePathEntry("path2", 0),includes.get(2));

			List<ICLanguageSettingEntry> macros = LanguageSettingsManager
				.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.MACRO);
			assertEquals(2, macros.size());
			assertEquals(new CMacroEntry("MACRO0", "value0",0), macros.get(0));
			assertEquals(new CMacroEntry("MACRO1", "value1",0), macros.get(1));
		}

	}

	/**
	 */
	public void testConfigurationDescription_GetByKindConflicting() throws Exception {
		final ICConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);

		// contribute the entries
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path", ICSettingEntry.BUILTIN));
		original.add(new CIncludePathEntry("path", ICSettingEntry.UNDEFINED));
		original.add(new CIncludePathEntry("path", 0));

		ILanguageSettingsProvider provider0 = new MockProvider(PROVIDER_0, PROVIDER_NAME_0, original);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider0);
		cfgDescription.setLanguageSettingProviders(providers);

		{
			// retrieve entries by kind, only first entry is returned
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager
				.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
			assertEquals(1, includes.size());
			assertEquals(original.get(0),includes.get(0));
		}

	}

	/**
	 */
	public void testConfigurationDescription_ConflictingProviders() throws Exception {
		final ICConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);

		// contribute the entries
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();

		// contribute the higher ranked entries
		List<ICLanguageSettingEntry> originalHigh = new ArrayList<ICLanguageSettingEntry>();
		originalHigh.add(new CIncludePathEntry("path0", ICSettingEntry.RESOLVED));
		originalHigh.add(new CIncludePathEntry("path1", 0));
		originalHigh.add(new CIncludePathEntry("path2", ICSettingEntry.UNDEFINED));
		ILanguageSettingsProvider highRankProvider = new MockProvider(PROVIDER_2, PROVIDER_NAME_2, originalHigh);
		providers.add(highRankProvider);

		// contribute the lower ranked entries
		List<ICLanguageSettingEntry> originalLow = new ArrayList<ICLanguageSettingEntry>();
		originalLow.add(new CIncludePathEntry("path0", ICSettingEntry.BUILTIN));
		originalLow.add(new CIncludePathEntry("path1", ICSettingEntry.UNDEFINED));
		originalLow.add(new CIncludePathEntry("path2", 0));
		originalLow.add(new CIncludePathEntry("path3", 0));
		ILanguageSettingsProvider lowRankProvider = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, originalLow);
		providers.add(lowRankProvider);

		cfgDescription.setLanguageSettingProviders(providers);

		// retrieve entries by kind
		{
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager
				.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
			// path0 is taken from higher priority provider
			assertEquals(originalHigh.get(0),includes.get(0));
			// path1 disablement by lower priority provider is ignored
			assertEquals(originalHigh.get(1),includes.get(1));
			// path2 is removed because of DISABLED flag of high priority provider
			// path3 gets there from low priority provider
			assertEquals(originalLow.get(3),includes.get(2));
			assertEquals(3, includes.size());
		}

	}

	/**
	 */
	public void testConfigurationDescription_ParentFolder() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		final IFolder parentFolder = ResourceHelper.createFolder(project, "/ParentFolder/");
		assertNotNull(parentFolder);
		final IFile emptySettingsPath = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/empty");
		assertNotNull(emptySettingsPath);

		// store the entries in parent folder
		final List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_0, PROVIDER_NAME_0, null)  {
			@Override
			public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
				if (rc.equals(parentFolder)) {
					return original;
				}
				if (rc.equals(emptySettingsPath)) {
					return new ArrayList<ICLanguageSettingEntry>(0);
				}
				return null;
			}

		};
		providers.add(provider);
		cfgDescription.setLanguageSettingProviders(providers);

		{
			// retrieve entries for a derived resource (in a subfolder)
			IFile derived = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/resource");
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntriesUpResourceTree(provider, cfgDescription, derived, LANG_ID);
			// taken from parent folder
			assertEquals(original.get(0),retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}

		{
			// retrieve entries for not related resource
			IFile notRelated = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/AnotherFolder/Subfolder/resource"));
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntriesUpResourceTree(provider, cfgDescription, notRelated, LANG_ID);
			assertEquals(0, retrieved.size());
		}

		{
			// test distinction between no settings and empty settings
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntriesUpResourceTree(provider, cfgDescription, emptySettingsPath, LANG_ID);
			// NOT taken from parent folder
			assertEquals(0, retrieved.size());
		}
	}

	/**
	 */
	public void testConfigurationDescription_ProviderIds() throws Exception {
		final ICConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);
		ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(PROVIDER_ID_EXT);

		{
			// ensure no test provider is set yet
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, providers.size());
		}

		{
			// set test provider
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			assertNotNull(workspaceProvider);
			providers.add(workspaceProvider);
			cfgDescription.setLanguageSettingProviders(providers);
		}

		{
			// check that test provider got there
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(providers.get(0), workspaceProvider);
		}
	}

	/**
	 * TODO
	 */
	public void testConfigurationDescription_UniqueProviders() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project, true);

		ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		// attempt to add duplicate providers
		MockProvider dupe1 = new MockProvider(PROVIDER_0, PROVIDER_NAME_1, null);
		MockProvider dupe2 = new MockProvider(PROVIDER_0, PROVIDER_NAME_2, null);

		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(dupe1);
		providers.add(dupe2);

		try {
			cfgDescription.setLanguageSettingProviders(providers);
			fail("cfgDescription.setLanguageSettingProviders() should not accept duplicate providers");
		} catch (Exception e) {
			// Exception is welcome here
		}

	}

	/**
	 * TODO
	 */
	public void testConfigurationDescription_SerializeProviders() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project, true);

		ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(PROVIDER_ID_EXT);
		assertNotNull(workspaceProvider);
		{
			// ensure no test provider is set yet
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, providers.size());
		}
		{
			// set test provider
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(workspaceProvider);
			cfgDescription.setLanguageSettingProviders(providers);
		}
		{
			// check that test provider got there
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(workspaceProvider, providers.get(0));
		}

		{
			// serialize
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
			// close and reopen the project
			project.close(null);
			project.open(null);
		}

		{
			// check that test provider got loaded
			ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project);
			ICConfigurationDescription[] loadedCfgDescriptions = prjDescription.getConfigurations();
			ICConfigurationDescription loadedCfgDescription = loadedCfgDescriptions[0];
			assertTrue(cfgDescription instanceof CConfigurationDescription);

			List<ILanguageSettingsProvider> loadedProviders = loadedCfgDescription.getLanguageSettingProviders();
			assertEquals(workspaceProvider, loadedProviders.get(0));
		}

	}

	/**
	 */
	public void testBaseProvider() throws Exception {
		final List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<String> languages = new ArrayList<String>(2) {
			{
				add("bogus.language.id");
				add(LANG_ID);
			}
		};

		// add default provider
		LanguageSettingsBaseProvider provider = new LanguageSettingsBaseProvider(
				PROVIDER_0, PROVIDER_NAME_0, languages, original);

		{
			// attempt to get entries for wrong language
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, FILE_0, "wrong.lang.id");
			assertNull(retrieved);
		}

		{
			// retrieve the entries
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, FILE_0, LANG_ID);
			assertEquals(original.get(0), retrieved.get(0));
			List<String> retrievedLanguageIds = provider.getLanguageIds();
			for (String languageId: languages) {
				assertTrue(retrievedLanguageIds.contains(languageId));
			}
			assertEquals(languages.size(), retrievedLanguageIds.size());
		}

	}

}
