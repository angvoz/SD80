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

import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsUnderConstructionTests extends TestCase {
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
	public LanguageSettingsUnderConstructionTests(String name) {
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
		return new TestSuite(LanguageSettingsUnderConstructionTests.class);
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
	 * Test serialization of user defined providers.
	 *
	 * @throws Exception...
	 */
	public void testSerializeUserDefinedProviderWorkspace() throws Exception {
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

}
