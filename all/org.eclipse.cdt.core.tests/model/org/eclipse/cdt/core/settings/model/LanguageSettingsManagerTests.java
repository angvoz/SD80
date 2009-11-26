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

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.internal.errorparsers.tests.ResourceHelper;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescription;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsExtensionManager;
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
	// These should match id and name of extension point defined in plugin.xml
	private static final String DEFAULT_PROVIDER_ID_EXT = "org.eclipse.cdt.core.tests.default.language.settings.provider";
	private static final String DEFAULT_PROVIDER_NAME_EXT = "Test Plugin Default Language Settings Provider";
	private static final String PROVIDER_ID_EXT = "org.eclipse.cdt.core.tests.custom.language.settings.provider";
	private static final String PROVIDER_NAME_EXT = "Test Plugin Language Settings Provider";
	private static final String LANG_ID_EXT = "org.eclipse.cdt.core.tests.language.id";
	private static final String BASE_PROVIDER_SUBCLASS_ID_EXT = "org.eclipse.cdt.core.tests.default.language.settings.provider.subclass";
	private static final String PERSISTENT_PROVIDER_SUBCLASS_ID_EXT = "org.eclipse.cdt.core.tests.persistent.language.settings.provider.subclass";

	private static final String CONFIGURATION_ID = "cfg.id";
	private static final IFile FILE_0 = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/project/path0"));
	private static final String LANG_ID = "test.lang.id";
	private static final String PROVIDER_NULL = "test.provider.null.id";
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_1 = "test.provider.1.id";
	private static final String PROVIDER_2 = "test.provider.2.id";
	private static final String PROVIDER_NAME_NULL = "test.provider.null.name";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";
	private static final String PROVIDER_NAME_1 = "test.provider.1.name";
	private static final String PROVIDER_NAME_2 = "test.provider.2.name";

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
//		fProject = ResourceHelper.createCDTProject(TEST_PROJECT_NAME);
//		assertNotNull(fProject);
//		errorList = new ArrayList<ProblemMarkerInfo>();
	}

	@Override
	protected void tearDown() throws Exception {
//		ResourceHelper.cleanUp();
//		fProject = null;

		LanguageSettingsManager.setUserDefinedProviders(null);
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
	 * @param ids - array of error parser IDs
	 * @return error parser IDs delimited with error parser delimiter ";"
	 * @since 5.2
	 */
	public static String toDelimitedString(String[] ids) {
		String result=""; //$NON-NLS-1$
		for (String id : ids) {
			if (result.length()==0) {
				result = id;
			} else {
				result += ";" + id;
			}
		}
		return result;
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
	 * Check that regular ICLanguageSettingsProvider extension defined in plugin.xml is accessible.
	 *
	 * @throws Exception...
	 */
	public void testExtension() throws Exception {
		{
			int pos = Arrays.binarySearch(LanguageSettingsManager.getProviderExtensionIds(), DEFAULT_PROVIDER_ID_EXT);
			assertTrue("extension " + DEFAULT_PROVIDER_ID_EXT + " not found", pos>=0);
		}
		{
			int pos = Arrays.binarySearch(LanguageSettingsManager.getProviderAvailableIds(), DEFAULT_PROVIDER_ID_EXT);
			assertTrue("extension " + DEFAULT_PROVIDER_ID_EXT + " not found", pos>=0);
		}

		// get test plugin extension provider
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getProvider(DEFAULT_PROVIDER_ID_EXT);
		assertNotNull(providerExt);

		assertTrue(providerExt instanceof LanguageSettingsBaseProvider);
		LanguageSettingsBaseProvider provider = (LanguageSettingsBaseProvider)providerExt;
		assertEquals(DEFAULT_PROVIDER_ID_EXT, provider.getId());
		assertEquals(DEFAULT_PROVIDER_NAME_EXT, provider.getName());

		// retrieve wrong language
		assertEquals(0, provider.getSettingEntries(null, FILE_0, LANG_ID).size());

		// benchmarks matching extension point definition
		final List<ICLanguageSettingEntry> entriesExt = new ArrayList<ICLanguageSettingEntry>();
		entriesExt.add(new CIncludePathEntry("/usr/include/",
				ICSettingEntry.BUILTIN
				| ICSettingEntry.READONLY
				| ICSettingEntry.LOCAL
				| ICSettingEntry.VALUE_WORKSPACE_PATH
				| ICSettingEntry.RESOLVED
				| ICSettingEntry.UNDEFINED
		));
		entriesExt.add(new CMacroEntry("TEST_DEFINE", "100", 0));
		entriesExt.add(new CIncludeFileEntry("/include/file.inc", 0));
		entriesExt.add(new CLibraryPathEntry("/usr/lib/", 0));
		entriesExt.add(new CLibraryFileEntry("libdomain.a", 0));
		entriesExt.add(new CMacroFileEntry("/macro/file.mac", 0));

		// retrieve entries from extension point
		List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, FILE_0, LANG_ID_EXT);
		for (int i=0;i<entriesExt.size();i++) {
			assertEquals("i="+i, entriesExt.get(i), retrieved.get(i));
		}
		assertEquals(entriesExt.size(), retrieved.size());
	}

	/**
	 * Check that subclassed LanguageSettingsBaseProvider extension defined in plugin.xml is accessible.
	 *
	 * @throws Exception...
	 */
	public void testExtensionBaseProviderSubclass() throws Exception {
		// get test plugin extension provider
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getProvider(BASE_PROVIDER_SUBCLASS_ID_EXT);
		assertNotNull(providerExt);
		
		assertTrue(providerExt instanceof TestClassLSBaseProvider);
		TestClassLSBaseProvider provider = (TestClassLSBaseProvider)providerExt;
		assertEquals(BASE_PROVIDER_SUBCLASS_ID_EXT, provider.getId());
		
		// benchmarks matching extension point definition
		final List<ICLanguageSettingEntry> entriesExt = new ArrayList<ICLanguageSettingEntry>();
		entriesExt.add(new CIncludePathEntry("/usr/include/", ICSettingEntry.BUILTIN));
		
		// retrieve entries from extension point
		List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, FILE_0, LANG_ID_EXT);
		for (int i=0;i<entriesExt.size();i++) {
			assertEquals("i="+i, entriesExt.get(i), retrieved.get(i));
		}
		assertEquals(entriesExt.size(), retrieved.size());
	}
	
	/**
	 * Check that subclassed LanguageSettingsBaseProvider extension defined in plugin.xml is accessible.
	 *
	 * @throws Exception...
	 */
	public void testExtensionPersistentProviderSubclass() throws Exception {
		// get test plugin extension provider
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getProvider(PERSISTENT_PROVIDER_SUBCLASS_ID_EXT);
		assertNotNull(providerExt);
		
		assertTrue(providerExt instanceof TestClassLSPersistentProvider);
		TestClassLSPersistentProvider provider = (TestClassLSPersistentProvider)providerExt;
		assertEquals(PERSISTENT_PROVIDER_SUBCLASS_ID_EXT, provider.getId());
		
		// benchmarks matching extension point definition
		final List<ICLanguageSettingEntry> entriesExt = new ArrayList<ICLanguageSettingEntry>();
		entriesExt.add(new CIncludePathEntry("/usr/include/", ICSettingEntry.BUILTIN));
		
		// retrieve entries from extension point
		List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, FILE_0, LANG_ID_EXT);
		for (int i=0;i<entriesExt.size();i++) {
			assertEquals("i="+i, entriesExt.get(i), retrieved.get(i));
		}
		assertEquals(entriesExt.size(), retrieved.size());
	}
	
	/**
	 * Make sure extensions contributed through extension point are sorted by name.
	 *
	 * @throws Exception...
	 */
	public void testExtensionsSorting() throws Exception {
		{
			String[] ids = LanguageSettingsManager.getProviderExtensionIds();
			String lastName="";
			// providers created from extensions are to be sorted by names
			for (String id : ids) {
				String name = LanguageSettingsManager.getProvider(id).getName();
				assertTrue(lastName.compareTo(name)<=0);
				lastName = name;
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
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getProvider(PROVIDER_ID_EXT);
		assertNotNull(providerExt);
		assertTrue(providerExt instanceof TestClassLanguageSettingsProvider);

		assertEquals(PROVIDER_ID_EXT, providerExt.getId());
		assertEquals(PROVIDER_NAME_EXT, providerExt.getName());
	}
	
	/**
	 * Test setting/retrieval of providers and their IDs.
	 *
	 * @throws Exception...
	 */
	public void testAvailableProviders() throws Exception {

		final String[] availableProviderIds = LanguageSettingsManager.getProviderAvailableIds();
		assertNotNull(availableProviderIds);
		assertTrue(availableProviderIds.length>0);
		final String firstId = LanguageSettingsManager.getProviderAvailableIds()[0];
		final ILanguageSettingsProvider firstProvider = LanguageSettingsManager.getProvider(firstId);
		assertNotNull(firstProvider);
		assertEquals(firstId, firstProvider.getId());
		final String firstName = firstProvider.getName();
		// Preconditions
		{
			List<String> all = Arrays.asList(LanguageSettingsManager.getProviderAvailableIds());
			assertEquals(false, all.contains(PROVIDER_0));
			assertEquals(true, all.contains(firstId));

			assertNull(LanguageSettingsManager.getProvider(PROVIDER_0));

			ILanguageSettingsProvider retrieved2 = LanguageSettingsManager.getProvider(firstId);
			assertNotNull(retrieved2);
			assertEquals(firstProvider, retrieved2);
		}

		// set available providers
		{
			ILanguageSettingsProvider dummy1 = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, null);
			final String firstNewName = firstName + " new";
			ILanguageSettingsProvider dummy2 = new MockProvider(firstId, firstNewName, null);
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] {
					// add brand new one
					dummy1,
					// override extension with another one
					dummy2,
			});
			List<String> all = Arrays.asList(LanguageSettingsManager.getProviderAvailableIds());
			assertEquals(true, all.contains(PROVIDER_1));
			assertEquals(true, all.contains(firstId));

			ILanguageSettingsProvider retrieved1 = LanguageSettingsManager.getProvider(PROVIDER_1);
			assertNotNull(retrieved1);
			assertEquals(PROVIDER_NAME_1, retrieved1.getName());
			assertEquals(dummy1, retrieved1);

			ILanguageSettingsProvider retrieved2 = LanguageSettingsManager.getProvider(firstId);
			assertNotNull(retrieved2);
			assertEquals(firstNewName, retrieved2.getName());
			assertEquals(dummy2, retrieved2);
		}
		// reset available providers
		{
			LanguageSettingsManager.setUserDefinedProviders(null);

			List<String> all = Arrays.asList(LanguageSettingsManager.getProviderAvailableIds());
			assertEquals(false, all.contains(PROVIDER_1));
			assertEquals(true, all.contains(firstId));

			assertNull(LanguageSettingsManager.getProvider(PROVIDER_1));

			ILanguageSettingsProvider retrieved2 = LanguageSettingsManager.getProvider(firstId);
			assertNotNull(retrieved2);
			assertEquals(firstProvider, retrieved2);
		}
	}

	/**
	 * Test setting/retrieval of user defined providers.
	 *
	 * @throws Exception...
	 */
	public void testUserDefinedProviders() throws Exception {
		// reset providers
		{
			LanguageSettingsManager.setUserDefinedProviders(null);
			String all = toDelimitedString(LanguageSettingsManager.getProviderAvailableIds());
			String extensions = toDelimitedString(LanguageSettingsManager.getProviderExtensionIds());
			assertEquals(all, extensions);
		}
		{
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] {
					new MockProvider(PROVIDER_0, PROVIDER_NAME_0, null),
			});
			String all = toDelimitedString(LanguageSettingsManager.getProviderAvailableIds());
			String extensions = toDelimitedString(LanguageSettingsManager.getProviderExtensionIds());
			assertFalse(all.equals(extensions));
		}
	}

	/**
	 * Test setting/retrieval of default provider IDs preferences.
	 *
	 * @throws Exception...
	 */
	public void testDefaultProviderIds() throws Exception {
		final String[] availableProviderIds = LanguageSettingsManager.getProviderAvailableIds();
		assertNotNull(availableProviderIds);
		final String[] initialDefaultProviderIds = LanguageSettingsManager.getDefaultProviderIds();

		// preconditions
		{
			String[] defaultProviderIds = LanguageSettingsManager.getDefaultProviderIds();
			assertNotNull(defaultProviderIds);
			assertEquals(toDelimitedString(availableProviderIds), toDelimitedString(defaultProviderIds));
		}
		// setDefaultProviderIds
		{
			String[] newDefaultProviderIds = {
					"org.eclipse.cdt.core.test.language.settings.provider0",
					"org.eclipse.cdt.core.test.language.settings.provider1",
					"org.eclipse.cdt.core.test.language.settings.provider2",
			};
			LanguageSettingsManager.setDefaultProviderIds(newDefaultProviderIds);
			String[] defaultProviderIds = LanguageSettingsManager.getDefaultProviderIds();
			assertNotNull(defaultProviderIds);
			assertEquals(toDelimitedString(newDefaultProviderIds), toDelimitedString(defaultProviderIds));
		}

		// reset
		{
			LanguageSettingsManager.setDefaultProviderIds(null);
			String[] defaultProviderIds = LanguageSettingsManager.getDefaultProviderIds();
			assertNotNull(defaultProviderIds);
			assertEquals(toDelimitedString(availableProviderIds), toDelimitedString(defaultProviderIds));
		}
	}

	/**
	 * Check that default provider IDs are stored properly.
	 *
	 * @throws Exception...
	 */
	public void testSerializeDefaultProviderIds() throws Exception {
		final String[] testingDefaultProviderIds = {
				"org.eclipse.cdt.core.test.language.settings.provider0",
				"org.eclipse.cdt.core.test.language.settings.provider1",
				"org.eclipse.cdt.core.test.language.settings.provider2",
		};
		final String TESTING_IDS = toDelimitedString(testingDefaultProviderIds);
		final String DEFAULT_IDS = toDelimitedString(LanguageSettingsManager.getDefaultProviderIds());

		{
			// setDefaultProviderIds
			LanguageSettingsExtensionManager.setDefaultProviderIdsInternal(testingDefaultProviderIds);

			String[] defaultProviderIds = LanguageSettingsManager.getDefaultProviderIds();
			assertNotNull(defaultProviderIds);
			assertEquals(TESTING_IDS, toDelimitedString(defaultProviderIds));

			// serialize them
			LanguageSettingsExtensionManager.serializeDefaultProviderIds();
		}

		{
			// Remove from internal list
			LanguageSettingsExtensionManager.setDefaultProviderIdsInternal(null);
			assertEquals(DEFAULT_IDS, toDelimitedString(LanguageSettingsManager.getDefaultProviderIds()));
		}

		{
			// Re-load from persistent storage and check it out
			LanguageSettingsExtensionManager.loadDefaultProviderIds();

			String[] defaultProviderIds = LanguageSettingsManager.getDefaultProviderIds();
			assertNotNull(defaultProviderIds);
			assertEquals(TESTING_IDS, toDelimitedString(defaultProviderIds));
		}

		{
			// Reset IDs and serialize
			LanguageSettingsExtensionManager.setDefaultProviderIdsInternal(null);
			LanguageSettingsExtensionManager.serializeDefaultProviderIds();

			// Check that default IDs are loaded
			LanguageSettingsExtensionManager.loadDefaultProviderIds();
			String[] defaultProviderIds = LanguageSettingsManager.getDefaultProviderIds();
			assertNotNull(defaultProviderIds);
			assertEquals(DEFAULT_IDS, toDelimitedString(defaultProviderIds));
		}
	}

	/**
	 * Test serialization of user defined providers.
	 *
	 * @throws Exception...
	 */
	public void testSerializeUserDefinedproviderWorkspace() throws Exception {
//		final String TESTING_ID = "org.eclipse.cdt.core.test.language.settings.provider";
//		final String TESTING_NAME = "A provider";
//
//		{
//			// Create provider
//			ICLanguageSettingsProvider provider = new GCCProvider();
//			// Add to available providers
//			LanguageSettingsExtensionManager.setUserDefinedProvidersInternal(
//					new ICLanguageSettingsProvider[] {provider});
//			assertNotNull(LanguageSettingsManager.getProvider(TESTING_ID));
//			assertEquals(TESTING_NAME, LanguageSettingsManager.getProvider(TESTING_ID).getName());
//			// Serialize in persistent storage
//			LanguageSettingsExtensionManager.serializeUserDefinedProviders();
//		}
//		{
//			// Remove from available providers
//			LanguageSettingsExtensionManager.setUserDefinedProvidersInternal(null);
//			assertNull(LanguageSettingsManager.getProviderCopy(TESTING_ID));
//		}
//
//		{
//			// Re-load from persistent storage and check it out
//			LanguageSettingsExtensionManager.loadUserDefinedProviders();
//			ICLanguageSettingsProvider provider = LanguageSettingsManager.getProviderCopy(TESTING_ID);
//			assertNotNull(provider);
//			assertEquals(TESTING_NAME, provider.getName());
//			assertTrue(provider instanceof ProviderNamedWrapper);
//			assertTrue(((ProviderNamedWrapper)provider).getProvider() instanceof GCCProvider);
//		}
//		{
//			// Remove from available providers as clean-up
//			LanguageSettingsExtensionManager.setUserDefinedProvidersInternal(null);
//			assertNull(LanguageSettingsManager.getProviderCopy(TESTING_ID));
//		}
		fail("UNDER CONSTRUCTION");
	}

	/**
	 * Make sure special characters are serialized properly.
	 *
	 * @throws Exception...
	 */
	public void testSerializeRegexProviderSpecialCharacters() throws Exception {
//
//		final String TESTING_ID = "org.eclipse.cdt.core.test.regexlanguage.settings.provider";
//		final String TESTING_NAME = "<>\"'\\& Error Parser";
//		final String TESTING_REGEX = "Pattern-<>\"'\\&";
//		final String ALL_IDS = toDelimitedString(LanguageSettingsManager.getProviderAvailableIds());
//		{
//			// Create provider with the same id as in eclipse registry
//			RegexProvider regexProvider = new RegexProvider(TESTING_ID, TESTING_NAME);
//			regexProvider.addPattern(new RegexErrorPattern(TESTING_REGEX,
//					"line-<>\"'\\&", "file-<>\"'\\&", "description-<>\"'\\&", null, IMarkerGenerator.SEVERITY_WARNING, false));
//
//			// Add to available providers
//			LanguageSettingsExtensionManager.setUserDefinedProvidersInternal(new ICLanguageSettingsProvider[] {regexProvider});
//			assertNotNull(LanguageSettingsManager.getProviderCopy(TESTING_ID));
//			// And serialize in persistent storage
//			LanguageSettingsExtensionManager.serializeUserDefinedProviders();
//		}
//
//		{
//			// Re-load from persistent storage and check it out
//			LanguageSettingsExtensionManager.loadUserDefinedProviders();
//			String all = toDelimitedString(LanguageSettingsManager.getProviderAvailableIds());
//			assertTrue(all.contains(TESTING_ID));
//
//			ICLanguageSettingsProvider provider = LanguageSettingsManager.getProviderCopy(TESTING_ID);
//			assertNotNull(provider);
//			assertTrue(provider instanceof RegexProvider);
//			RegexProvider regexProvider = (RegexProvider)provider;
//			assertEquals(TESTING_ID, regexProvider.getId());
//			assertEquals(TESTING_NAME, regexProvider.getName());
//
//			RegexErrorPattern[] errorPatterns = regexProvider.getPatterns();
//			assertEquals(1, errorPatterns.length);
//			assertEquals(TESTING_REGEX, errorPatterns[0].getPattern());
//		}
		fail("UNDER CONSTRUCTION");
	}

	/**
	 * Test retrieval of provider, clone() and equals().
	 *
	 * @throws Exception...
	 */
	public void testGetProviderCopy() throws Exception {
//		{
//			ICLanguageSettingsProvider clone1 = LanguageSettingsManager.getProviderCopy(REGEX_ERRORPARSER_ID);
//			ICLanguageSettingsProvider clone2 = LanguageSettingsManager.getProviderCopy(REGEX_ERRORPARSER_ID);
//			assertEquals(clone1, clone2);
//			assertNotSame(clone1, clone2);
//		}
//		{
//			ICLanguageSettingsProvider clone1 = LanguageSettingsManager.getProviderCopy(GCC_ERRORPARSER_ID);
//			ICLanguageSettingsProvider clone2 = LanguageSettingsManager.getProviderCopy(GCC_ERRORPARSER_ID);
//			assertEquals(clone1, clone2);
//			assertNotSame(clone1, clone2);
//
//			assertTrue(clone1 instanceof ProviderNamedWrapper);
//			assertTrue(clone2 instanceof ProviderNamedWrapper);
//			ICLanguageSettingsProvider gccClone1 = ((ProviderNamedWrapper)clone1).getProvider();
//			ICLanguageSettingsProvider gccClone2 = ((ProviderNamedWrapper)clone2).getProvider();
//			assertNotSame(clone1, clone2);
//		}
		fail("UNDER CONSTRUCTION");
	}

	/**
	 */
	public void testConfigurationDescription_NullProvider() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		{
			// set rough provider returning null with getSettingEntries()
			ILanguageSettingsProvider providerNull = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, null);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerNull);
			LanguageSettingsManager.setProviders(cfgDescription, providers);
		}

		// use provider returning null
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(
					cfgDescription, PROVIDER_1, FILE_0, LANG_ID);
			assertNotNull(retrieved);
			assertEquals(0, retrieved.size());
		}
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(
					cfgDescription, PROVIDER_1, FILE_0, LANG_ID, 0);
			assertNotNull(retrieved);
			assertEquals(0, retrieved.size());
		}
		
		{
			// set rough provider returning null in getSettingEntries() array
			ILanguageSettingsProvider providerNull = new MockProvider(PROVIDER_2, PROVIDER_NAME_2, 
					new ArrayList<ICLanguageSettingEntry>() {
						{
							add(null);
						}
					}
			);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerNull);
			LanguageSettingsManager.setProviders(cfgDescription, providers);
		}
		
		// use provider returning null as item in array
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(
					cfgDescription, PROVIDER_2, FILE_0, LANG_ID);
			assertNotNull(retrieved);
			assertEquals(1, retrieved.size());
		}
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(
					cfgDescription, PROVIDER_2, FILE_0, LANG_ID, 0);
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
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		final ICConfigurationDescription modelCfgDescription = cfgDescriptions[0];
		assertTrue(modelCfgDescription instanceof CConfigurationDescription);

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
		LanguageSettingsManager.setProviders(modelCfgDescription, providers);

		{
			// retrieve the entries for model configuration description
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(modelCfgDescription, PROVIDER_0, FILE_0, LANG_ID);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
		
		{
			// retrieve the entries for different configuration description
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(modelCfgDescription, PROVIDER_1, FILE_0, LANG_ID);
			assertEquals(0, retrieved.size());
		}
	}
	
	/**
	 * TODO .
	 *
	 * @throws Exception...
	 */
	public void testConfigurationDescription_Providers() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		// set providers
		ILanguageSettingsProvider provider1 = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, null);
		ILanguageSettingsProvider provider2 = new MockProvider(PROVIDER_2, PROVIDER_NAME_2, null);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider1);
		providers.add(provider2);
		LanguageSettingsManager.setProviders(cfgDescription, providers);

		{
			// get providers
			List<ILanguageSettingsProvider> retrieved = LanguageSettingsManager.getProviders(cfgDescription);
			assertEquals(provider1, retrieved.get(0));
			assertEquals(provider2, retrieved.get(1));
			assertEquals(providers.size(), retrieved.size());
		}
	}

	/**
	 */
	public void testConfigurationDescription_Basic() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

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
		LanguageSettingsManager.setProviders(cfgDescription, providers);

		{
			// get list of providers
			List<ILanguageSettingsProvider> all = LanguageSettingsManager.getProviders(cfgDescription);
			assertTrue(all.contains(provider1));
			assertTrue(all.contains(provider2));
			assertTrue(all.size()>=2);
		}

		{
			// retrieve the entries for provider-1
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, PROVIDER_1, FILE_0, LANG_ID);

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
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, PROVIDER_2, FILE_0, LANG_ID);

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
	public void testConfigurationDescription_Filtered() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

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
		LanguageSettingsManager.setProviders(cfgDescription, providers);

		{
			// retrieve entries by kind
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager
				.getSettingEntries(cfgDescription, PROVIDER_0, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
			assertEquals(3, includes.size());
			assertEquals(new CIncludePathEntry("path0", 0),includes.get(0));
			assertEquals(new CIncludePathEntry("path1", 0),includes.get(1));
			assertEquals(new CIncludePathEntry("path2", 0),includes.get(2));

			List<ICLanguageSettingEntry> macros = LanguageSettingsManager
				.getSettingEntries(cfgDescription, PROVIDER_0, FILE_0, LANG_ID, ICSettingEntry.MACRO);
			assertEquals(2, macros.size());
			assertEquals(new CMacroEntry("MACRO0", "value0",0), macros.get(0));
			assertEquals(new CMacroEntry("MACRO1", "value1",0), macros.get(1));
		}

	}

	/**
	 */
	public void testConfigurationDescription_FilteredConflicting() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		// contribute the entries
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path", ICSettingEntry.BUILTIN));
		original.add(new CIncludePathEntry("path", ICSettingEntry.UNDEFINED));
		original.add(new CIncludePathEntry("path", 0));

		ILanguageSettingsProvider provider0 = new MockProvider(PROVIDER_0, PROVIDER_NAME_0, original);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider0);
		LanguageSettingsManager.setProviders(cfgDescription, providers);

		{
			// retrieve entries by kind, only first entry is returned
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager
				.getSettingEntries(cfgDescription, PROVIDER_0, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
			assertEquals(1, includes.size());
			assertEquals(original.get(0),includes.get(0));
		}

	}

	/**
	 */
	public void testConfigurationDescription_ReconciledProviders() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

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

		LanguageSettingsManager.setProviders(cfgDescription, providers);

		// retrieve entries by kind
		{
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager
				.getSettingEntriesReconciled(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
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
		ICProject cproject = CProjectHelper.createNewStileCProject(getName(), IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		IProject project = cproject.getProject();
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
				IFolder pf = parentFolder;
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
		LanguageSettingsManager.setProviders(cfgDescription, providers);

		{
			// retrieve entries for a derived resource (in a subfolder)
			IFile derived = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/resource");
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, PROVIDER_0, derived, LANG_ID);
			// taken from parent folder
			assertEquals(original.get(0),retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}

		{
			// retrieve entries for not related resource
			IFile notRelated = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/AnotherFolder/Subfolder/resource"));
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, PROVIDER_0, notRelated, LANG_ID);
			assertEquals(0, retrieved.size());
		}

		{
			// test distinction between no settings and empty settings
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, PROVIDER_0, emptySettingsPath, LANG_ID);
			// NOT taken from parent folder
			assertEquals(0, retrieved.size());
		}
	}

	/**
	 */
	public void testConfigurationDescription_ProviderIds() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		{
			// ensure no test provider is set yet
			List<String> ids = LanguageSettingsManager.getProviderIds(cfgDescription);
			assertFalse(ids.contains(DEFAULT_PROVIDER_ID_EXT));
		}

		{
			// set test provider
			List<String> ids = new ArrayList<String>();
			ids.add(DEFAULT_PROVIDER_ID_EXT);
			LanguageSettingsManager.setProviderIds(cfgDescription, ids);
		}

		{
			// check that test provider got there
			List<String> ids = LanguageSettingsManager.getProviderIds(cfgDescription);
			assertTrue(ids.contains(DEFAULT_PROVIDER_ID_EXT));
		}
	}

	/**
	 */
	public void testConfigurationDescription_SerializeProviders() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);

		IProject project = cproject.getProject();
		ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project, true);

		ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		{
			// ensure no test provider is set yet
			List<String> ids = LanguageSettingsManager.getProviderIds(cfgDescription);
			assertFalse(ids.contains(DEFAULT_PROVIDER_ID_EXT));
		}
		{
			// set test provider
			List<String> ids = new ArrayList<String>();
			ids.add(DEFAULT_PROVIDER_ID_EXT);
			LanguageSettingsManager.setProviderIds(cfgDescription, ids);
		}
		{
			// check that test provider got there
			List<String> ids = LanguageSettingsManager.getProviderIds(cfgDescription);
			assertTrue(ids.contains(DEFAULT_PROVIDER_ID_EXT));
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
			ICConfigurationDescription[] loadedCfgDescriptions = getConfigurationDescriptions(cproject.getProject());
			ICConfigurationDescription loadedCfgDescription = loadedCfgDescriptions[0];
			assertTrue(cfgDescription instanceof CConfigurationDescription);

			List<String> ids = LanguageSettingsManager.getProviderIds(loadedCfgDescription);
			assertTrue(ids.contains(DEFAULT_PROVIDER_ID_EXT));
		}

	}

	/**
	 */
	public void testDefaultProvider() throws Exception {
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
			assertEquals(0, retrieved.size());
		}

		{
			// retrieve the entries
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, FILE_0, LANG_ID);
			assertEquals(original.get(0), retrieved.get(0));
		}

	}

}
