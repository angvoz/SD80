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

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager_TBD;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsExtensionManager_TBD;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsSerializeProjectTests extends TestCase {
	// Should match id of extension point defined in plugin.xml
	private static final String EXTENSION_PROVIDER_ID = "org.eclipse.cdt.core.tests.language.settings.base.provider.subclass";
	private static final String EXTENSION_SERIALIZABLE_PROVIDER_ID = "org.eclipse.cdt.core.tests.custom.serializable.language.settings.provider";

	private static final String CFG_ID = "test.configuration.id.0";
	private static final String CFG_ID_2 = "test.configuration.id.2";
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_2 = "test.provider.2.id";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";
	private static final String PROVIDER_NAME_2 = "test.provider.2.name";
	private static final String PROVIDER_ID_WSP = "test.provider.workspace.id";
	private static final String PROVIDER_NAME_WSP = "test.provider.workspace.name";
	private static final String ELEM_TEST = "test";

	private static CoreModel coreModel = CoreModel.getDefault();

	class MockConfigurationDescription extends CProjectDescriptionTestHelper.DummyCConfigurationDescription {
		List<ILanguageSettingsProvider> providers;
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
	class MockProjectDescription extends CProjectDescriptionTestHelper.DummyCProjectDescription {
		ICConfigurationDescription[] cfgDescriptions;

		public MockProjectDescription(ICConfigurationDescription[] cfgDescriptions) {
			this.cfgDescriptions = cfgDescriptions;
		}

		public MockProjectDescription(ICConfigurationDescription cfgDescription) {
			this.cfgDescriptions = new ICConfigurationDescription[] { cfgDescription };
		}

		@Override
		public ICConfigurationDescription[] getConfigurations() {
			return cfgDescriptions;

		}

		@Override
		public ICConfigurationDescription getConfigurationById(String id) {
			for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
				if (cfgDescription.getId().equals(id))
					return cfgDescription;
			}
			return null;
		}
	}

	private class MockProvider extends LanguageSettingsSerializable {
		public MockProvider(String id, String name) {
			super(id, name);
		}
	}


	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsSerializeProjectTests(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
//		ResourceHelper.cleanUp();
		LanguageSettingsManager_TBD.setUserDefinedProviders(null);
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(LanguageSettingsSerializeProjectTests.class);
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
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project);
		assertNotNull(projectDescription);
		assertEquals(1, projectDescription.getConfigurations().length);
		// configuration description
		ICConfigurationDescription[] cfgDescriptions = projectDescription.getConfigurations();
		assertNotNull(cfgDescriptions);
		return cfgDescriptions;
	}

	private ICConfigurationDescription getFirstConfigurationDescription(IProject project) {
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertNotNull(cfgDescription);

		return cfgDescription;
	}

	/**
	 */
	public void testWorkspacePersistence_ModifiedExtensionProvider() throws Exception {
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));

		{
			// ID should not be in the list of workspace providers
			List<String> oldAvailableIds = Arrays.asList(LanguageSettingsManager_TBD.getProviderAvailableIds());
			assertTrue(oldAvailableIds.contains(EXTENSION_SERIALIZABLE_PROVIDER_ID));
			// get the provider
			LanguageSettingsSerializable provider = (LanguageSettingsSerializable) LanguageSettingsManager_TBD.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			assertNotNull(provider);
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, provider.getId());
			// add entries
			provider.setSettingEntries(null, null, null, original);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());

			// serialize language settings of user defined providers (on workspace level)
			LanguageSettingsExtensionManager_TBD.serializeLanguageSettingsWorkspace();
			// clear the provider
			provider.setSettingEntries(null, null, null, null);
		}

		{
			// doublecheck it's clean
			ILanguageSettingsProvider provider = LanguageSettingsManager_TBD.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertNull(retrieved);
		}
		{
			// re-load and check language settings of the provider
			LanguageSettingsExtensionManager_TBD.loadLanguageSettingsWorkspace();

			ILanguageSettingsProvider provider = LanguageSettingsManager_TBD.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, provider.getId());
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
	}

	/**
	 */
	public void testWorkspacePersistence_AddProvider() throws Exception {
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));

		{
			// ID should not be in the list of workspace providers
			List<String> oldAvailableIds = Arrays.asList(LanguageSettingsManager_TBD.getProviderAvailableIds());
			assertFalse(oldAvailableIds.contains(PROVIDER_0));
			// create a provider
			LanguageSettingsSerializable mockProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			mockProvider.setSettingEntries(null, null, null, original);

			// assign provider to workspace
			LanguageSettingsManager_TBD.setUserDefinedProviders(new ILanguageSettingsProvider[] {mockProvider});
			String[] retrievedIds = LanguageSettingsManager_TBD.getProviderAvailableIds();
			// user defined providers are always before extension providers
			assertEquals(PROVIDER_0, retrievedIds[0]);

			// serialize language settings of user defined providers (on workspace level)
			LanguageSettingsExtensionManager_TBD.serializeLanguageSettingsWorkspace();
			// clear the provider
			mockProvider.setSettingEntries(null, null, null, null);
			ILanguageSettingsProvider provider = LanguageSettingsManager_TBD.getWorkspaceProvider(PROVIDER_0);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertNull(retrieved);
		}
		{
			// re-load and check language settings of the provider
			LanguageSettingsExtensionManager_TBD.loadLanguageSettingsWorkspace();

			ILanguageSettingsProvider provider = LanguageSettingsManager_TBD.getWorkspaceProvider(PROVIDER_0);
			assertEquals(PROVIDER_0, provider.getId());
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
	}

	/**
	 */
	public void testWorkspacePersistence_OverrideExtensionProvider() throws Exception {
		MockProvider mockProvider;
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));

		{
			// ID should be in the list of workspace providers
			List<String> oldAvailableIds = Arrays.asList(LanguageSettingsManager_TBD.getProviderAvailableIds());
			assertTrue(oldAvailableIds.contains(EXTENSION_PROVIDER_ID));
			ILanguageSettingsProvider oldProvider = LanguageSettingsManager_TBD.getWorkspaceProvider(EXTENSION_PROVIDER_ID);
			assertTrue(oldProvider instanceof LanguageSettingsBaseProvider);
		}

		{
			// create a new provider
			mockProvider = new MockProvider(EXTENSION_PROVIDER_ID, PROVIDER_NAME_0);
			mockProvider.setSettingEntries(null, null, null, original);

			// assign provider to workspace
			LanguageSettingsManager_TBD.setUserDefinedProviders(new ILanguageSettingsProvider[] {mockProvider});
			String[] retrievedIds = LanguageSettingsManager_TBD.getProviderAvailableIds();
			// user defined providers are always before extension providers
			assertEquals(EXTENSION_PROVIDER_ID, retrievedIds[0]);
			// doublecheck it got there
			ILanguageSettingsProvider provider = LanguageSettingsManager_TBD.getWorkspaceProvider(EXTENSION_PROVIDER_ID);
			assertTrue(provider instanceof MockProvider);
		}

		{
			// serialize language settings of user defined providers (on workspace level)
			LanguageSettingsExtensionManager_TBD.serializeLanguageSettingsWorkspace();
			// clear the provider
			mockProvider.setSettingEntries(null, null, null, null);
			ILanguageSettingsProvider provider = LanguageSettingsManager_TBD.getWorkspaceProvider(EXTENSION_PROVIDER_ID);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertNull(retrieved);
		}
		{
			// re-load and check language settings of the provider
			LanguageSettingsExtensionManager_TBD.loadLanguageSettingsWorkspace();

			ILanguageSettingsProvider provider = LanguageSettingsManager_TBD.getWorkspaceProvider(EXTENSION_PROVIDER_ID);
			assertEquals(EXTENSION_PROVIDER_ID, provider.getId());
			assertEquals(PROVIDER_NAME_0, provider.getName());
			assertTrue(provider instanceof MockProvider);
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
	}

	/**
	 */
	public void testProjectPersistence_SerializableProviderDOM() throws Exception {
		Element rootElement = null;

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));

		{
			// create a provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			LanguageSettingsSerializable serializableProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			serializableProvider.setSettingEntries(null, null, null, original);

			ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(serializableProvider);
			cfgDescription.setLanguageSettingProviders(providers);

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsExtensionManager_TBD.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager_TBD.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertTrue(provider instanceof LanguageSettingsSerializable);

			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
	}

	/**
	 */
	public void testProjectPersistence_TwoConfigurationsDOM() throws Exception {
		Element rootElement = null;

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("path2", 0));

		{
			// create a project description with 2 configuration descriptions
			MockProjectDescription mockPrjDescription = new MockProjectDescription(
					new MockConfigurationDescription[] {
							new MockConfigurationDescription(CFG_ID),
							new MockConfigurationDescription(CFG_ID_2),
						});
			{
				ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
				assertNotNull(cfgDescriptions);
				assertEquals(2, cfgDescriptions.length);
				{
					// populate configuration 1 with provider
					ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
					assertNotNull(cfgDescription1);
					assertEquals(CFG_ID, cfgDescription1.getId());
					LanguageSettingsSerializable provider1 = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
					provider1.setSettingEntries(null, null, null, original);
					ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
					providers.add(provider1);
					cfgDescription1.setLanguageSettingProviders(providers);
				}
				{
					// populate configuration 2 with provider
					ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
					assertNotNull(cfgDescription2);
					assertEquals(CFG_ID_2, cfgDescription2.getId());
					LanguageSettingsSerializable provider2 = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
					provider2.setSettingEntries(null, null, null, original2);
					ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
					providers.add(provider2);
					cfgDescription2.setLanguageSettingProviders(providers);
				}
			}

			{
				// doublecheck both configuration descriptions
				ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
				assertNotNull(cfgDescriptions);
				assertEquals(2, cfgDescriptions.length);
				{
					// doublecheck configuration 1
					ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
					assertNotNull(cfgDescription1);
					List<ILanguageSettingsProvider> providers = cfgDescription1.getLanguageSettingProviders();
					assertNotNull(providers);
					assertEquals(1, providers.size());
					ILanguageSettingsProvider provider = providers.get(0);
					assertNotNull(provider);
					List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
					assertEquals(original.get(0), retrieved.get(0));
					assertEquals(original.size(), retrieved.size());
				}
				{
					// doublecheck configuration 2
					ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
					assertNotNull(cfgDescription2);
					List<ILanguageSettingsProvider> providers = cfgDescription2.getLanguageSettingProviders();
					assertNotNull(providers);
					assertEquals(1, providers.size());
					ILanguageSettingsProvider provider = providers.get(0);
					assertNotNull(provider);
					List<ICLanguageSettingEntry> retrieved2 = provider.getSettingEntries(null, null, null);
					assertEquals(original2.get(0), retrieved2.get(0));
					assertEquals(original2.size(), retrieved2.size());
				}
			}

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsExtensionManager_TBD.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-create a project description and re-load language settings for each configuration
			MockProjectDescription mockPrjDescription = new MockProjectDescription(
					new MockConfigurationDescription[] {
							new MockConfigurationDescription(CFG_ID),
							new MockConfigurationDescription(CFG_ID_2),
						});
			// load
			LanguageSettingsExtensionManager_TBD.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(2, cfgDescriptions.length);
			{
				// check configuration 1
				ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
				assertNotNull(cfgDescription1);
				List<ILanguageSettingsProvider> providers = cfgDescription1.getLanguageSettingProviders();
				assertNotNull(providers);
				assertEquals(1, providers.size());
				ILanguageSettingsProvider provider = providers.get(0);
				assertNotNull(provider);
				List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
				assertEquals(original.get(0), retrieved.get(0));
				assertEquals(original.size(), retrieved.size());
			}
			{
				// check configuration 2
				ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
				assertNotNull(cfgDescription2);
				List<ILanguageSettingsProvider> providers = cfgDescription2.getLanguageSettingProviders();
				assertNotNull(providers);
				assertEquals(1, providers.size());
				ILanguageSettingsProvider provider = providers.get(0);
				assertNotNull(provider);
				List<ICLanguageSettingEntry> retrieved2 = provider.getSettingEntries(null, null, null);
				assertEquals(original2.get(0), retrieved2.get(0));
				assertEquals(original2.size(), retrieved2.size());
			}
		}
	}

	/**
	 */
	public void testProjectPersistence_SubclassedSerializableProviderDOM() throws Exception {
		Element rootElement = null;

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));

		{
			// create a provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			LanguageSettingsSerializable serializableProvider = new TestClassSerializableLanguageSettingsProvider(PROVIDER_0, PROVIDER_NAME_0);
			serializableProvider.setSettingEntries(null, null, null, original);

			ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(serializableProvider);
			cfgDescription.setLanguageSettingProviders(providers);

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsExtensionManager_TBD.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager_TBD.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertTrue(provider instanceof TestClassSerializableLanguageSettingsProvider);

			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
	}

	/**
	 */
	public void testProjectPersistence_ReferenceExtensionProviderDOM() throws Exception {
		Element rootElement = null;

		// provider of other type (not LanguageSettingsSerializable) defined as an extension
		ILanguageSettingsProvider providerExt = LanguageSettingsExtensionManager_TBD.getWorkspaceProvider(EXTENSION_PROVIDER_ID);

		{
			// create cfg description
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			// populate with provider defined as extension
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerExt);
			cfgDescription.setLanguageSettingProviders(providers);


			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsExtensionManager_TBD.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager_TBD.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			// and check the newly loaded provider which should be same as extension one
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertNotNull(provider);
			assertSame(providerExt, provider);
		}
	}

	/**
	 */
	public void testProjectPersistence_ReferenceWorkspaceProviderDOM() throws Exception {
		Element rootElement = null;

		// provider set on workspace level overriding an extension
		String idExt = EXTENSION_PROVIDER_ID;
		ILanguageSettingsProvider providerExt = LanguageSettingsExtensionManager_TBD.getWorkspaceProvider(idExt);
		ILanguageSettingsProvider providerWsp = new MockProvider(idExt, PROVIDER_NAME_0);
		{
			LanguageSettingsManager_TBD.setUserDefinedProviders(new ILanguageSettingsProvider[] { providerWsp });
			ILanguageSettingsProvider provider = LanguageSettingsManager_TBD.getWorkspaceProvider(idExt);
			assertNotSame(providerExt, provider);
			assertSame(providerWsp, provider);
		}
		{
			// create cfg description
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			// populate with provider defined as extension
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerWsp);
			cfgDescription.setLanguageSettingProviders(providers);


			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsExtensionManager_TBD.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager_TBD.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			// and check the newly loaded provider which should be same as the workspace one
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertNotNull(provider);
			assertSame(providerWsp, provider);
		}
	}

	/**
	 */
	public void testProjectPersistence_OverrideExtensionProviderDOM() throws Exception {
		Element rootElement = null;

		// provider set on workspace level overriding an extension
		String idExt = EXTENSION_PROVIDER_ID;
		LanguageSettingsExtensionManager_TBD.getWorkspaceProvider(idExt);
		{
			// create cfg description
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			// populate with provider overriding the extension (must be SerializableLanguageSettingsProvider or a class from another extension)
			ILanguageSettingsProvider providerOverride = new TestClassSerializableLanguageSettingsProvider(idExt, PROVIDER_NAME_0);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerOverride);
			cfgDescription.setLanguageSettingProviders(providers);


			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsExtensionManager_TBD.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager_TBD.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			// check the newly loaded provider
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertNotNull(provider);
			assertTrue(provider instanceof TestClassSerializableLanguageSettingsProvider);
			assertEquals(idExt, provider.getId());
			assertEquals(PROVIDER_NAME_0, provider.getName());
		}
	}


	/**
	 */
	public void testProjectPersistence_OverrideWorkspaceProviderDOM() throws Exception {
		Element rootElement = null;

		// define provider set on workspace level overriding an extension
		String idExt = EXTENSION_PROVIDER_ID;
		ILanguageSettingsProvider providerExt = LanguageSettingsExtensionManager_TBD.getWorkspaceProvider(idExt);
		ILanguageSettingsProvider providerWsp = new LanguageSettingsSerializable(idExt, PROVIDER_NAME_WSP);
		{
			LanguageSettingsManager_TBD.setUserDefinedProviders(new ILanguageSettingsProvider[] { providerWsp });
			ILanguageSettingsProvider provider = LanguageSettingsManager_TBD.getWorkspaceProvider(idExt);
			assertNotSame(providerExt, provider);
			assertSame(providerWsp, provider);
		}
		{
			// create cfg description
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			// populate with provider overriding both workspace provider and extension
			ILanguageSettingsProvider providerOverride = new TestClassSerializableLanguageSettingsProvider(idExt, PROVIDER_NAME_0);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerOverride);
			cfgDescription.setLanguageSettingProviders(providers);

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsExtensionManager_TBD.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager_TBD.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			// check the newly loaded provider
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertNotNull(provider);
			assertTrue(provider instanceof TestClassSerializableLanguageSettingsProvider);
			assertEquals(idExt, provider.getId());
			assertEquals(PROVIDER_NAME_0, provider.getName());
		}
	}

	/**
	 */
	public void testProjectPersistence_MixedProvidersDOM() throws Exception {
		Element rootElement = null;

		List<ICLanguageSettingEntry> original_41 = new ArrayList<ICLanguageSettingEntry>();
		original_41.add(new CIncludePathEntry("path0", 0));

		List<ICLanguageSettingEntry> original_42 = new ArrayList<ICLanguageSettingEntry>();
		original_42.add(new CIncludePathEntry("path2", 0));

		ILanguageSettingsProvider providerExt;
		ILanguageSettingsProvider providerWsp;
		{
			// Define providers a bunch
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			{
				ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
				ICConfigurationDescription cfgDescription = cfgDescriptions[0];
				assertNotNull(cfgDescription);

				// 1. Provider reference to extension from plugin.xml
				providerExt = LanguageSettingsExtensionManager_TBD.getWorkspaceProvider(EXTENSION_PROVIDER_ID);

				// 2. Provider reference to provider defined in the workspace
				providerWsp = new LanguageSettingsSerializable(PROVIDER_ID_WSP, PROVIDER_NAME_WSP);
				LanguageSettingsManager_TBD.setUserDefinedProviders(new ILanguageSettingsProvider[] { providerWsp });

				// 3. TODO Provider reference to provider defined in the project

				// 4. Providers defined in a configuration
				// 4.1
				LanguageSettingsSerializable mockProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
				mockProvider.setSettingEntries(null, null, null, original_41);
				// 4.2
				LanguageSettingsSerializable mockProvider2 = new TestClassSerializableLanguageSettingsProvider(PROVIDER_2, PROVIDER_NAME_2);
				mockProvider2.setSettingEntries(null, null, null, original_42);

				ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(providerExt);
				providers.add(providerWsp);
				providers.add(mockProvider);
				providers.add(mockProvider2);
				cfgDescription.setLanguageSettingProviders(providers);
			}

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsExtensionManager_TBD.serializeLanguageSettings(rootElement, mockPrjDescription);
			XmlUtil.toString(doc);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager_TBD.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];

			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			// 1. Provider reference to extension from plugin.xml
			ILanguageSettingsProvider provider0 = providers.get(0);
			assertSame(provider0, providerExt);
			// 2. Provider reference to provider defined in the workspace
			ILanguageSettingsProvider provider1 = providers.get(1);
			assertSame(provider1, providerWsp);

			// 3. TODO Provider reference to provider defined in the project

			// 4. Providers defined in a configuration
			// 4.1
			{
				ILanguageSettingsProvider provider2 = providers.get(2);
				assertTrue(provider2 instanceof LanguageSettingsSerializable);
				List<ICLanguageSettingEntry> retrieved = provider2.getSettingEntries(null, null, null);
				assertEquals(original_41.get(0), retrieved.get(0));
				assertEquals(original_41.size(), retrieved.size());
			}
			// 4.2
			{
				ILanguageSettingsProvider provider3 = providers.get(3);
				assertTrue(provider3 instanceof TestClassSerializableLanguageSettingsProvider);
				List<ICLanguageSettingEntry> retrieved = provider3.getSettingEntries(null, null, null);
				assertEquals(original_42.get(0), retrieved.get(0));
				assertEquals(original_42.size(), retrieved.size());
			}
			assertEquals(4, providers.size());
		}
	}

	/**
	 */
	public void testProjectPersistence_RealProject() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		String xmlStorageFileLocation;
		String xmlOutOfTheWay;

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));

		{
			// get project descriptions
			ICProjectDescription writableProjDescription = coreModel.getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];

			// create a provider
			LanguageSettingsSerializable mockProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			mockProvider.setSettingEntries(cfgDescription, null, null, original);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(mockProvider);
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			coreModel.setProjectDescription(project, writableProjDescription);
			IFile xmlStorageFile = project.getFile(".settings/language.settings.xml");
			assertTrue(xmlStorageFile.exists());
			xmlStorageFileLocation = xmlStorageFile.getLocation().toOSString();
		}
		{
			coreModel.getProjectDescription(project);
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertTrue(provider instanceof LanguageSettingsSerializable);
			assertEquals(PROVIDER_0, provider.getId());
			assertEquals(PROVIDER_NAME_0, provider.getName());

			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(cfgDescription, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
		{
			// Move storage out of the way
			java.io.File xmlFile = new java.io.File(xmlStorageFileLocation);
			xmlOutOfTheWay = xmlStorageFileLocation+".out-of-the-way";
			java.io.File xmlFileOut = new java.io.File(xmlOutOfTheWay);
			xmlFile.renameTo(xmlFileOut);
			assertFalse(xmlFile.exists());
			assertTrue(xmlFileOut.exists());
		}

		{
			// clear configuration
			ICProjectDescription writableProjDescription = coreModel.getProjectDescription(project);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			cfgDescription.setLanguageSettingProviders(new ArrayList<ILanguageSettingsProvider>());
			coreModel.setProjectDescription(project, writableProjDescription);
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, providers.size());
		}
		{
			// re-check if it really took it
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, providers.size());
		}
		{
			// close the project
			project.close(null);
		}
		{
			// open to double-check the data is not kept in some other kind of cache
			project.open(null);
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, providers.size());
			// and close
			project.close(null);
		}

		{
			// Move storage back
			java.io.File xmlFile = new java.io.File(xmlStorageFileLocation);
			xmlFile.delete();
			assertFalse("File "+xmlFile+ " still exist", xmlFile.exists());
			java.io.File xmlFileOut = new java.io.File(xmlOutOfTheWay);
			xmlFileOut.renameTo(xmlFile);
			assertTrue("File "+xmlFile+ " does not exist", xmlFile.exists());
			assertFalse("File "+xmlFileOut+ " still exist", xmlFileOut.exists());
		}

		{
			// Remove project from internal cache
			CProjectDescriptionManager.getInstance().projectClosedRemove(project);
			// open project and check if providers are loaded
			project.open(null);
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			ILanguageSettingsProvider loadedProvider = providers.get(0);
			assertTrue(loadedProvider instanceof LanguageSettingsSerializable);
			assertEquals(PROVIDER_0, loadedProvider.getId());
			assertEquals(PROVIDER_NAME_0, loadedProvider.getName());

			List<ICLanguageSettingEntry> retrieved = loadedProvider.getSettingEntries(cfgDescription, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
	}


}
