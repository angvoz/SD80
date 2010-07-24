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
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescription;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescriptionCache;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
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
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";
	private static final String ELEM_LANGUAGE_SETTINGS = "languageSettings";

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
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("path2", 0));
		
		Element elementProvider = null;
		LanguageSettingsSerializable mockProvider = null;
		{
			// create a provider
			mockProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			mockProvider.setSettingEntries(null, null, null, original);
			mockProvider.setSettingEntries(cfgDescription, null, null, original2);
		}
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
			
			List<ICLanguageSettingEntry> retrieved2 = loadedProvider.getSettingEntries(cfgDescription, null, null);
			assertEquals(original2.get(0), retrieved2.get(0));
			assertEquals(original2.size(), retrieved2.size());
		}
	}
	
	/**
	 */
	public void testSerializeProject() throws Exception {
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
	
	/**
	 */
	public void testSetProjectDescription() throws Exception {
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
	
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		String xmlStorageFileLocation;
		String xmlOutOfTheWay;
		
		{
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project, true);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
	
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof CConfigurationDescription);
	
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(cfgDescription, null, null, original);
			provider.setSettingEntries(cfgDescription, FILE_0, LANG_ID, original);
			
			{
				// add mock serializable provider
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(provider);
				LanguageSettingsManager.setProviders(cfgDescription, providers);
			}
	
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
			
			{
				// check that provider is retrievable
				List<ILanguageSettingsProvider> providers = LanguageSettingsManager.getProviders(cfgDescription);
				assertEquals(1, providers.size());
				ILanguageSettingsProvider provider0 = providers.get(0);
				assertEquals(provider.getId(), provider0.getId());
				// and the settings are there
				List<ICLanguageSettingEntry> settingEntries = provider0.getSettingEntries(cfgDescription, null, null);
				assertEquals(original.get(0), settingEntries.get(0));
			}
			
			{
				// serialize
				CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
				IFile xmlStorageFile = project.getFile("language.settings.xml");
				assertTrue(xmlStorageFile.exists());
				xmlStorageFileLocation = xmlStorageFile.getLocation().toOSString();
	
				// close the project
				project.close(null);
				// Remove from internal cache
				CProjectDescriptionManager.getInstance().projectClosedRemove(project);
			}
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
		
		// No storage
		{
			// open the project
			project.open(null);
			
			ICProjectDescription readonlyProjDescription = CoreModel.getDefault().getProjectDescription(project, false);
			ICConfigurationDescription[] cfgDescriptions = readonlyProjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof CConfigurationDescriptionCache);
			
			// check that there is no entries
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
			
			// close again
			project.close(null);
			// Remove from internal cache
			CProjectDescriptionManager.getInstance().projectClosedRemove(project);
		}
		
		{
			// Move storage back
			java.io.File xmlFile = new java.io.File(xmlStorageFileLocation);
			java.io.File xmlFileOut = new java.io.File(xmlOutOfTheWay);
			xmlFileOut.renameTo(xmlFile);
			assertTrue(xmlFile.exists());
			assertFalse(xmlFileOut.exists());
		}
	
		// Storage is back
		{
			// open the project
			project.open(null);
			
			// AG: FIXME
			ICProjectDescription readonlyProjDescription = CProjectDescriptionManager.getInstance()
				.getProjectDescription(project);
			ICConfigurationDescription[] cfgDescriptions = readonlyProjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof CConfigurationDescription);
			
			{
				// check 1st entry
				List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
					.getSettingEntries(cfgDescription, PROVIDER_0, null, null);
				assertEquals(original.get(0), retrieved.get(0));
				assertEquals(original.size(), retrieved.size());
			}
			{
				// check 2nd entry
				List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
					.getSettingEntries(cfgDescription, PROVIDER_0, FILE_0, LANG_ID);
				assertEquals(original.get(0), retrieved.get(0));
				assertEquals(original.size(), retrieved.size());
			}
			
			project.close(null);
		}
	
		fail("UNDER CONSTRUCTION");
	}

	/**
	 */
	public void testPersistProject() throws Exception {
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		
		// Writing
		{
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project, true);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
	
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof CConfigurationDescription);
			
	
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			provider.setSettingEntries(cfgDescription, null, null, original);
			provider.setSettingEntries(cfgDescription, FILE_0, LANG_ID, original);
			
			{
				// add mock serializable provider
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(provider);
				LanguageSettingsManager.setProviders(cfgDescription, providers);
			}
	
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
			
			{
				// check that provider is retrievable
				List<ILanguageSettingsProvider> providers = LanguageSettingsManager.getProviders(cfgDescription);
				assertEquals(1, providers.size());
				ILanguageSettingsProvider provider0 = providers.get(0);
				assertEquals(provider.getId(), provider0.getId());
				// and the settings are there
				List<ICLanguageSettingEntry> settingEntries = provider0.getSettingEntries(cfgDescription, null, null);
				assertEquals(original.get(0), settingEntries.get(0));
			}
	
			// apply providers to the project configuration and serialize
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
				
		}
		
		// Reading read-only description
		{
			ICProjectDescription readonlyProjDescription = CoreModel.getDefault().getProjectDescription(project, false);
			
			ICConfigurationDescription[] cfgDescriptions = readonlyProjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof CConfigurationDescriptionCache);
			
			{
				// check 1st entry
				List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
					.getSettingEntries(cfgDescription, PROVIDER_0, null, null);
				assertEquals(original.get(0), retrieved.get(0));
				assertEquals(original.size(), retrieved.size());
			}
			{
				// check 2nd entry
				List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
					.getSettingEntries(cfgDescription, PROVIDER_0, FILE_0, LANG_ID);
				assertEquals(original.get(0), retrieved.get(0));
				assertEquals(original.size(), retrieved.size());
			}
		}
			
		// Reading
		{
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project, true);
			
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof CConfigurationDescription);
			
			// clear provider
			List<ILanguageSettingsProvider> providers = LanguageSettingsManager.getProviders(cfgDescription);
			assertEquals(1, providers.size());
			assertTrue(providers.get(0) instanceof LanguageSettingsSerializable);
			
			LanguageSettingsSerializable provider = (LanguageSettingsSerializable)providers.get(0);
			provider.setSettingEntries(cfgDescription, null, null, null);
			provider.setSettingEntries(cfgDescription, FILE_0, LANG_ID, null);
			{
				// check 1 that provider is clear
				List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
					.getSettingEntries(cfgDescription, PROVIDER_0, null, null);
				assertEquals(0, retrieved.size());
			}
			{
				// check 2 that provider is clear
				List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
					.getSettingEntries(cfgDescription, PROVIDER_0, FILE_0, LANG_ID);
				assertEquals(0, retrieved.size());
			}
	
			// re-load from file-storage
			LanguageSettingsManager.loadLanguageSettings(writableProjDescription);
			{
				// check 1st entry
				List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
					.getSettingEntries(cfgDescription, PROVIDER_0, null, null);
				assertEquals(original.get(0), retrieved.get(0));
				assertEquals(original.size(), retrieved.size());
			}
			{
				// check 2nd entry
				List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager
					.getSettingEntries(cfgDescription, PROVIDER_0, FILE_0, LANG_ID);
				assertEquals(original.get(0), retrieved.get(0));
				assertEquals(original.size(), retrieved.size());
			}
			
		}
		
		// serialize over existing file (no exception expected)
		LanguageSettingsManager.serializeLanguageSettings(CoreModel.getDefault().getProjectDescription(project));
		
		
		fail("UNDER CONSTRUCTION");
	}
	

}
