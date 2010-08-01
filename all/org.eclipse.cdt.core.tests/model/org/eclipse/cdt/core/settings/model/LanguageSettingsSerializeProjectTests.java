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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsExtensionManager;
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
	
	private static final String CFG_ID = "test.configuration.id";
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_2 = "test.provider.2.id";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";
	private static final String PROVIDER_NAME_2 = "test.provider.2.name";
	private static final String PROVIDER_ID_WSP = "test.provider.workspace.id";
	private static final String PROVIDER_NAME_WSP = "test.provider.workspace.name";
	private static final String ELEM_TEST = "test";

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
		
//		public MockProjectDescription(ICConfigurationDescription[] cfgDescriptions) {
//			this.cfgDescriptions = cfgDescriptions;
//		}

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
		LanguageSettingsManager.setUserDefinedProviders(null);
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
		CoreModel coreModel = CoreModel.getDefault();
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
	public void testSerializableProviderDOM() throws Exception {
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
	public void testConfiguration_SerializeDOM() throws Exception {
		Element rootElement = null;
		
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("path2", 0));

		{
			// create a provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			{
				ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
				assertNotNull(cfgDescriptions);
				assertEquals(1, cfgDescriptions.length);
				ICConfigurationDescription cfgDescription = cfgDescriptions[0];
				assertNotNull(cfgDescription);
				
				LanguageSettingsSerializable serializableProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
				serializableProvider.setSettingEntries(null, null, null, original);
				serializableProvider.setSettingEntries(cfgDescription, null, null, original2);
				
				ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(serializableProvider);
				cfgDescription.setLanguageSettingProviders(providers);
			}
			
			// doublecheck the mock description
			{
				ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
				assertNotNull(cfgDescriptions);
				assertEquals(1, cfgDescriptions.length);
				ICConfigurationDescription cfgDescription = cfgDescriptions[0];
				assertNotNull(cfgDescription);
				
				List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
				assertNotNull(providers);
				assertEquals(1, providers.size());
				ILanguageSettingsProvider provider = providers.get(0);
				assertNotNull(provider);
				
				List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
				assertEquals(original.get(0), retrieved.get(0));
				assertEquals(original.size(), retrieved.size());
				
				List<ICLanguageSettingEntry> retrieved2 = provider.getSettingEntries(cfgDescription, null, null);
				assertEquals(original2.get(0), retrieved2.get(0));
				assertEquals(original2.size(), retrieved2.size());
			}

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsExtensionManager.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager.loadLanguageSettings(rootElement, mockPrjDescription);
			
			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertNotNull(provider);
			
			List<ICLanguageSettingEntry> retrieved = provider.getSettingEntries(null, null, null);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
			
			List<ICLanguageSettingEntry> retrieved2 = provider.getSettingEntries(cfgDescription, null, null);
			assertEquals(original2.get(0), retrieved2.get(0));
			assertEquals(original2.size(), retrieved2.size());
		}
	}
	
	/**
	 */
	public void testConfiguration_SerializableProviderDOM() throws Exception {
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
			LanguageSettingsExtensionManager.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager.loadLanguageSettings(rootElement, mockPrjDescription);
			
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
	public void testConfiguration_SubclassedSerializableProviderDOM() throws Exception {
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
			LanguageSettingsExtensionManager.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager.loadLanguageSettings(rootElement, mockPrjDescription);
			
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
	public void testConfiguration_ReferenceExtensionProviderDOM() throws Exception {
		Element rootElement = null;
		
		// provider of other type (not LanguageSettingsSerializable) defined as an extension
		ILanguageSettingsProvider providerExt = LanguageSettingsExtensionManager.getWorkspaceProvider(EXTENSION_PROVIDER_ID);
		
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
			LanguageSettingsExtensionManager.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager.loadLanguageSettings(rootElement, mockPrjDescription);
			
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
	public void testConfiguration_ReferenceWorkspaceProviderDOM() throws Exception {
		Element rootElement = null;
		
		// provider set on workspace level overriding an extension
		String idExt = EXTENSION_PROVIDER_ID;
		ILanguageSettingsProvider providerExt = LanguageSettingsExtensionManager.getWorkspaceProvider(idExt);
		ILanguageSettingsProvider providerWsp = new MockProvider(idExt, PROVIDER_NAME_0);
		{
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] { providerWsp });
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(idExt);
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
			LanguageSettingsExtensionManager.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager.loadLanguageSettings(rootElement, mockPrjDescription);
			
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
	public void testConfiguration_OverrideExtensionProviderDOM() throws Exception {
		Element rootElement = null;
		
		// provider set on workspace level overriding an extension
		String idExt = EXTENSION_PROVIDER_ID;
		LanguageSettingsExtensionManager.getWorkspaceProvider(idExt);
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
			LanguageSettingsExtensionManager.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager.loadLanguageSettings(rootElement, mockPrjDescription);
			
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
	public void testConfiguration_OverrideWorkspaceProviderDOM() throws Exception {
		Element rootElement = null;
		
		// define provider set on workspace level overriding an extension
		String idExt = EXTENSION_PROVIDER_ID;
		ILanguageSettingsProvider providerExt = LanguageSettingsExtensionManager.getWorkspaceProvider(idExt);
		ILanguageSettingsProvider providerWsp = new LanguageSettingsSerializable(idExt, PROVIDER_NAME_WSP);
		{
			LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] { providerWsp });
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(idExt);
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
			LanguageSettingsExtensionManager.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager.loadLanguageSettings(rootElement, mockPrjDescription);
			
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
	public void testConfiguration_SerializeDifferentProvidersDOM() throws Exception {
		Element rootElement = null;
		
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("path2", 0));
		
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
				providerExt = LanguageSettingsExtensionManager.getWorkspaceProvider(EXTENSION_PROVIDER_ID);
				
				// 2. Provider reference to provider defined in the workspace
				providerWsp = new LanguageSettingsSerializable(PROVIDER_ID_WSP, PROVIDER_NAME_WSP);
				LanguageSettingsManager.setUserDefinedProviders(new ILanguageSettingsProvider[] { providerWsp });

				// 3. TODO Provider reference to provider defined in the project

				// 4. Providers defined in a configuration
				// 4.1
				LanguageSettingsSerializable mockProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
				mockProvider.setSettingEntries(null, null, null, original);
				mockProvider.setSettingEntries(cfgDescription, null, null, original2);
				// 4.2
				LanguageSettingsSerializable mockProvider2 = new TestClassSerializableLanguageSettingsProvider(PROVIDER_2, PROVIDER_NAME_2);
				mockProvider2.setSettingEntries(null, null, null, original);
				mockProvider2.setSettingEntries(cfgDescription, null, null, original2);
				
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
			LanguageSettingsExtensionManager.serializeLanguageSettings(rootElement, mockPrjDescription);
			XmlUtil.toString(doc);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsExtensionManager.loadLanguageSettings(rootElement, mockPrjDescription);
			
			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			ILanguageSettingsProvider provider0 = providers.get(0);
			assertSame(provider0, providerExt);
			ILanguageSettingsProvider provider1 = providers.get(1);
			assertSame(provider1, providerWsp);
			ILanguageSettingsProvider provider2 = providers.get(2);
			assertTrue(provider2 instanceof LanguageSettingsSerializable);
			ILanguageSettingsProvider provider3 = providers.get(3);
			assertTrue(provider3 instanceof TestClassSerializableLanguageSettingsProvider);
			assertEquals(4, providers.size());
			
			{
				List<ICLanguageSettingEntry> retrieved = provider2.getSettingEntries(null, null, null);
				assertEquals(original.get(0), retrieved.get(0));
				assertEquals(original.size(), retrieved.size());
				
				List<ICLanguageSettingEntry> retrieved2 = provider2.getSettingEntries(cfgDescription, null, null);
				assertEquals(original2.get(0), retrieved2.get(0));
				assertEquals(original2.size(), retrieved2.size());
			}
			{
				List<ICLanguageSettingEntry> retrieved = provider3.getSettingEntries(null, null, null);
				assertEquals(original.get(0), retrieved.get(0));
				assertEquals(original.size(), retrieved.size());
				
				List<ICLanguageSettingEntry> retrieved2 = provider3.getSettingEntries(cfgDescription, null, null);
				assertEquals(original2.get(0), retrieved2.get(0));
				assertEquals(original2.size(), retrieved2.size());
			}
		}
	}
	
	/**
	 */
	public void testSerializeRealProject() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		String xmlStorageFileLocation;
		String xmlOutOfTheWay;

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
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
			CoreModel.getDefault()
				.setProjectDescription(project, writableProjDescription);
			IFile xmlStorageFile = project.getFile("language.settings.xml");
			assertTrue(xmlStorageFile.exists());
			xmlStorageFileLocation = xmlStorageFile.getLocation().toOSString();
		}
		{
			CoreModel.getDefault().getProjectDescription(project);
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
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			cfgDescription.setLanguageSettingProviders(new ArrayList<ILanguageSettingsProvider>());
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
		}
		{
			// close the project
			project.close(null);
		}
		{
			// open to double-check the data is not kept in some other kind of cache
			project.open(null);
			CoreModel.getDefault().getProjectDescription(project);
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, providers.size());
			// and close
			project.close(null);
		}
		
		{
			// Move storage back
			java.io.File xmlFile = new java.io.File(xmlStorageFileLocation);
			java.io.File xmlFileOut = new java.io.File(xmlOutOfTheWay);
			xmlFileOut.renameTo(xmlFile);
			assertTrue(xmlFile.exists());
			assertFalse(xmlFileOut.exists());
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
