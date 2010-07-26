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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsSerializeProjectTests extends TestCase {
	private static final IFile FILE_0 = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/project/path0"));
	private static final String LANG_ID = "test.lang.id";
	private static final String CFG_ID = "test.configuration.id";
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";
	private static final String ELEM_LANGUAGE_SETTINGS = "languageSettings";

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
		public void addConfiguration(ICConfigurationDescription cfgDescription) {
			cfgDescriptions = new ICConfigurationDescription[] {cfgDescription};
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
	public void testSerializeProviderDOM() throws Exception {
		ICConfigurationDescription mockCfgDescription = new CProjectDescriptionTestHelper.DummyCConfigurationDescription(CFG_ID);
		Element elementProvider = null;

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("path2", 0));
		
		// create a provider
		LanguageSettingsSerializable mockProvider = null;
		mockProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
		mockProvider.setSettingEntries(null, null, null, original);
		mockProvider.setSettingEntries(mockCfgDescription, null, null, original2);
		{
			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			Element rootElement = doc.createElement(ELEM_LANGUAGE_SETTINGS);
			// serialize language settings to DOM
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
	public void testSerializeProjectDOM() throws Exception {
		Element rootElement = null;
		
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("path2", 0));

		
		{
			// create a provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription();
			{
				MockConfigurationDescription mockCfgDescription = new MockConfigurationDescription(CFG_ID);
				mockPrjDescription.addConfiguration(mockCfgDescription);
				
				ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
				assertNotNull(cfgDescriptions);
				assertEquals(1, cfgDescriptions.length);
				ICConfigurationDescription cfgDescription = cfgDescriptions[0];
				assertNotNull(cfgDescription);
				
				LanguageSettingsSerializable mockProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
				mockProvider.setSettingEntries(null, null, null, original);
				mockProvider.setSettingEntries(mockCfgDescription, null, null, original2);
				
				ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(mockProvider);
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
			rootElement = XmlUtil.appendElement(doc, ELEM_LANGUAGE_SETTINGS);
			// serialize language settings to the DOM
			LanguageSettingsExtensionManager.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription();
			mockPrjDescription.addConfiguration(new MockConfigurationDescription(CFG_ID));
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
