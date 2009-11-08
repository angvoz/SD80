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
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsBaseContributor;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsExtensionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * Test cases testing RegexContributor functionality
 */
public class LanguageSettingsManagerTests extends TestCase {
	// These should match id and name of extension point defined in plugin.xml
	private static final String DEFAULT_CONTRIBUTOR_ID_EXT = "org.eclipse.cdt.core.tests.default.language.settings.contributor";
	private static final String DEFAULT_CONTRIBUTOR_NAME_EXT = "Test Plugin Default Language Settings Contributor";
	private static final String CONTRIBUTOR_ID_EXT = "org.eclipse.cdt.core.tests.custom.language.settings.contributor";
	private static final String CONTRIBUTOR_NAME_EXT = "Test Plugin Language Settings Contributor";
	private static final String LANG_ID_EXT = "org.eclipse.cdt.core.tests.language.id";

	private static final String CONFIGURATION_ID = "cfg.id";
	private static final IFile FILE_0 = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/project/path0"));
	private static final String LANG_ID = "test.lang.id";
	private static final String CONTRIBUTOR_0 = "test.contributor.0.id";
	private static final String CONTRIBUTOR_1 = "test.contributor.1.id";
	private static final String CONTRIBUTOR_2 = "test.contributor.2.id";
	private static final String CONTRIBUTOR_NAME_0 = "test.contributor.0.name";
	private static final String CONTRIBUTOR_NAME_1 = "test.contributor.1.name";
	private static final String CONTRIBUTOR_NAME_2 = "test.contributor.2.name";

	private class MockContributor extends AbstractExecutableExtensionBase implements ICLanguageSettingsContributor {
		private final List<ICLanguageSettingEntry> entries;

		public MockContributor(String id, String name, List<ICLanguageSettingEntry> entries) {
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

		LanguageSettingsManager.setUserDefinedContributors(null);
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
	 * Check that regular ICLanguageSettingsContributor extension defined in plugin.xml is accessible.
	 *
	 * @throws Exception...
	 */
	public void testExtension() throws Exception {
		{
			int pos = Arrays.binarySearch(LanguageSettingsManager.getContributorExtensionIds(), DEFAULT_CONTRIBUTOR_ID_EXT);
			assertTrue("extension " + DEFAULT_CONTRIBUTOR_ID_EXT + " not found", pos>=0);
		}
		{
			int pos = Arrays.binarySearch(LanguageSettingsManager.getContributorAvailableIds(), DEFAULT_CONTRIBUTOR_ID_EXT);
			assertTrue("extension " + DEFAULT_CONTRIBUTOR_ID_EXT + " not found", pos>=0);
		}

		// get test plugin extension contributor
		ICLanguageSettingsContributor contributorExt = LanguageSettingsManager.getContributor(DEFAULT_CONTRIBUTOR_ID_EXT);
		assertNotNull(contributorExt);

		assertTrue(contributorExt instanceof LanguageSettingsBaseContributor);
		LanguageSettingsBaseContributor contributor = (LanguageSettingsBaseContributor)contributorExt;
		assertEquals(DEFAULT_CONTRIBUTOR_ID_EXT, contributor.getId());
		assertEquals(DEFAULT_CONTRIBUTOR_NAME_EXT, contributor.getName());

		// retrieve wrong language
		assertEquals(0, contributor.getSettingEntries(null, FILE_0, LANG_ID).size());

		// benchmarks matching extension point definition
		final List<ICLanguageSettingEntry> entriesExt = new ArrayList<ICLanguageSettingEntry>() {
			{
				add(new CIncludePathEntry("/usr/include/",
						ICSettingEntry.BUILTIN
						| ICSettingEntry.READONLY
						| ICSettingEntry.LOCAL
						| ICSettingEntry.VALUE_WORKSPACE_PATH
						| ICSettingEntry.RESOLVED
						| ICSettingEntry.UNDEFINED
				));
				add(new CMacroEntry("TEST_DEFINE", "100", 0));
				add(new CIncludeFileEntry("/include/file.inc", 0));
				add(new CLibraryPathEntry("/usr/lib/", 0));
				add(new CLibraryFileEntry("libdomain.a", 0));
				add(new CMacroFileEntry("/macro/file.mac", 0));
			}
		};

		// retrieve entries from extension point
		List<ICLanguageSettingEntry> retrieved = contributor.getSettingEntries(null, FILE_0, LANG_ID_EXT);
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
			String[] ids = LanguageSettingsManager.getContributorExtensionIds();
			String lastName="";
			// contributors created from extensions are to be sorted by names
			for (String id : ids) {
				String name = LanguageSettingsManager.getContributor(id).getName();
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
		// get test plugin extension non-default contributor
		ICLanguageSettingsContributor contributorExt = LanguageSettingsManager.getContributor(CONTRIBUTOR_ID_EXT);
		assertNotNull(contributorExt);
		assertTrue(contributorExt instanceof TestLanguageSettingsContributor);

		assertEquals(CONTRIBUTOR_ID_EXT, contributorExt.getId());
		assertEquals(CONTRIBUTOR_NAME_EXT, contributorExt.getName());
	}
	
	/**
	 * Test setting/retrieval of contributors and their IDs.
	 *
	 * @throws Exception...
	 */
	public void testAvailableContributors() throws Exception {

		final String[] availableContributorIds = LanguageSettingsManager.getContributorAvailableIds();
		assertNotNull(availableContributorIds);
		assertTrue(availableContributorIds.length>0);
		final String firstId = LanguageSettingsManager.getContributorAvailableIds()[0];
		final ICLanguageSettingsContributor firstContributor = LanguageSettingsManager.getContributor(firstId);
		assertNotNull(firstContributor);
		assertEquals(firstId, firstContributor.getId());
		final String firstName = firstContributor.getName();
		// Preconditions
		{
			List<String> all = Arrays.asList(LanguageSettingsManager.getContributorAvailableIds());
			assertEquals(false, all.contains(CONTRIBUTOR_0));
			assertEquals(true, all.contains(firstId));

			assertNull(LanguageSettingsManager.getContributor(CONTRIBUTOR_0));

			ICLanguageSettingsContributor retrieved2 = LanguageSettingsManager.getContributor(firstId);
			assertNotNull(retrieved2);
			assertEquals(firstContributor, retrieved2);
		}

		// set available contributors
		{
			ICLanguageSettingsContributor dummy1 = new MockContributor(CONTRIBUTOR_1, CONTRIBUTOR_NAME_1, null);
			final String firstNewName = firstName + " new";
			ICLanguageSettingsContributor dummy2 = new MockContributor(firstId, firstNewName, null);
			LanguageSettingsManager.setUserDefinedContributors(new ICLanguageSettingsContributor[] {
					// add brand new one
					dummy1,
					// override extension with another one
					dummy2,
			});
			List<String> all = Arrays.asList(LanguageSettingsManager.getContributorAvailableIds());
			assertEquals(true, all.contains(CONTRIBUTOR_1));
			assertEquals(true, all.contains(firstId));

			ICLanguageSettingsContributor retrieved1 = LanguageSettingsManager.getContributor(CONTRIBUTOR_1);
			assertNotNull(retrieved1);
			assertEquals(CONTRIBUTOR_NAME_1, retrieved1.getName());
			assertEquals(dummy1, retrieved1);

			ICLanguageSettingsContributor retrieved2 = LanguageSettingsManager.getContributor(firstId);
			assertNotNull(retrieved2);
			assertEquals(firstNewName, retrieved2.getName());
			assertEquals(dummy2, retrieved2);
		}
		// reset available contributors
		{
			LanguageSettingsManager.setUserDefinedContributors(null);

			List<String> all = Arrays.asList(LanguageSettingsManager.getContributorAvailableIds());
			assertEquals(false, all.contains(CONTRIBUTOR_1));
			assertEquals(true, all.contains(firstId));

			assertNull(LanguageSettingsManager.getContributor(CONTRIBUTOR_1));

			ICLanguageSettingsContributor retrieved2 = LanguageSettingsManager.getContributor(firstId);
			assertNotNull(retrieved2);
			assertEquals(firstContributor, retrieved2);
		}
	}

	/**
	 * Test setting/retrieval of user defined contributors.
	 *
	 * @throws Exception...
	 */
	public void testUserDefinedContributors() throws Exception {
		// reset contributors
		{
			LanguageSettingsManager.setUserDefinedContributors(null);
			String all = toDelimitedString(LanguageSettingsManager.getContributorAvailableIds());
			String extensions = toDelimitedString(LanguageSettingsManager.getContributorExtensionIds());
			assertEquals(all, extensions);
		}
		{
			LanguageSettingsManager.setUserDefinedContributors(new ICLanguageSettingsContributor[] {
					new MockContributor(CONTRIBUTOR_0, CONTRIBUTOR_NAME_0, null),
			});
			String all = toDelimitedString(LanguageSettingsManager.getContributorAvailableIds());
			String extensions = toDelimitedString(LanguageSettingsManager.getContributorExtensionIds());
			assertFalse(all.equals(extensions));
		}
	}

	/**
	 * Test setting/retrieval of default contributor IDs preferences.
	 *
	 * @throws Exception...
	 */
	public void testDefaultContributorIds() throws Exception {
		final String[] availableContributorIds = LanguageSettingsManager.getContributorAvailableIds();
		assertNotNull(availableContributorIds);
		final String[] initialDefaultContributorIds = LanguageSettingsManager.getDefaultContributorIds();

		// preconditions
		{
			String[] defaultContributorIds = LanguageSettingsManager.getDefaultContributorIds();
			assertNotNull(defaultContributorIds);
			assertEquals(toDelimitedString(availableContributorIds), toDelimitedString(defaultContributorIds));
		}
		// setDefaultContributorIds
		{
			String[] newDefaultContributorIds = {
					"org.eclipse.cdt.core.test.language.settings.contributor0",
					"org.eclipse.cdt.core.test.language.settings.contributor1",
					"org.eclipse.cdt.core.test.language.settings.contributor2",
			};
			LanguageSettingsManager.setDefaultContributorIds(newDefaultContributorIds);
			String[] defaultContributorIds = LanguageSettingsManager.getDefaultContributorIds();
			assertNotNull(defaultContributorIds);
			assertEquals(toDelimitedString(newDefaultContributorIds), toDelimitedString(defaultContributorIds));
		}

		// reset
		{
			LanguageSettingsManager.setDefaultContributorIds(null);
			String[] defaultContributorIds = LanguageSettingsManager.getDefaultContributorIds();
			assertNotNull(defaultContributorIds);
			assertEquals(toDelimitedString(availableContributorIds), toDelimitedString(defaultContributorIds));
		}
	}

	/**
	 * Check that default contributor IDs are stored properly.
	 *
	 * @throws Exception...
	 */
	public void testSerializeDefaultContributorIds() throws Exception {
		final String[] testingDefaultContributorIds = {
				"org.eclipse.cdt.core.test.language.settings.contributor0",
				"org.eclipse.cdt.core.test.language.settings.contributor1",
				"org.eclipse.cdt.core.test.language.settings.contributor2",
		};
		final String TESTING_IDS = toDelimitedString(testingDefaultContributorIds);
		final String DEFAULT_IDS = toDelimitedString(LanguageSettingsManager.getDefaultContributorIds());

		{
			// setDefaultContributorIds
			LanguageSettingsExtensionManager.setDefaultContributorIdsInternal(testingDefaultContributorIds);

			String[] defaultContributorIds = LanguageSettingsManager.getDefaultContributorIds();
			assertNotNull(defaultContributorIds);
			assertEquals(TESTING_IDS, toDelimitedString(defaultContributorIds));

			// serialize them
			LanguageSettingsExtensionManager.serializeDefaultContributorIds();
		}

		{
			// Remove from internal list
			LanguageSettingsExtensionManager.setDefaultContributorIdsInternal(null);
			assertEquals(DEFAULT_IDS, toDelimitedString(LanguageSettingsManager.getDefaultContributorIds()));
		}

		{
			// Re-load from persistent storage and check it out
			LanguageSettingsExtensionManager.loadDefaultContributorIds();

			String[] defaultContributorIds = LanguageSettingsManager.getDefaultContributorIds();
			assertNotNull(defaultContributorIds);
			assertEquals(TESTING_IDS, toDelimitedString(defaultContributorIds));
		}

		{
			// Reset IDs and serialize
			LanguageSettingsExtensionManager.setDefaultContributorIdsInternal(null);
			LanguageSettingsExtensionManager.serializeDefaultContributorIds();

			// Check that default IDs are loaded
			LanguageSettingsExtensionManager.loadDefaultContributorIds();
			String[] defaultContributorIds = LanguageSettingsManager.getDefaultContributorIds();
			assertNotNull(defaultContributorIds);
			assertEquals(DEFAULT_IDS, toDelimitedString(defaultContributorIds));
		}
	}

	/**
	 * Test serialization of user defined contributors.
	 *
	 * @throws Exception...
	 */
	public void testSerializeWorkspaceContributor() throws Exception {
//		final String TESTING_ID = "org.eclipse.cdt.core.test.language.settings.contributor";
//		final String TESTING_NAME = "A contributor";
//
//		{
//			// Create contributor
//			ICLanguageSettingsContributor contributor = new GCCContributor();
//			// Add to available contributors
//			LanguageSettingsExtensionManager.setUserDefinedContributorsInternal(
//					new ICLanguageSettingsContributor[] {contributor});
//			assertNotNull(LanguageSettingsManager.getContributor(TESTING_ID));
//			assertEquals(TESTING_NAME, LanguageSettingsManager.getContributor(TESTING_ID).getName());
//			// Serialize in persistent storage
//			LanguageSettingsExtensionManager.serializeUserDefinedContributors();
//		}
//		{
//			// Remove from available contributors
//			LanguageSettingsExtensionManager.setUserDefinedContributorsInternal(null);
//			assertNull(LanguageSettingsManager.getContributorCopy(TESTING_ID));
//		}
//
//		{
//			// Re-load from persistent storage and check it out
//			LanguageSettingsExtensionManager.loadUserDefinedContributors();
//			ICLanguageSettingsContributor contributor = LanguageSettingsManager.getContributorCopy(TESTING_ID);
//			assertNotNull(contributor);
//			assertEquals(TESTING_NAME, contributor.getName());
//			assertTrue(contributor instanceof ContributorNamedWrapper);
//			assertTrue(((ContributorNamedWrapper)contributor).getContributor() instanceof GCCContributor);
//		}
//		{
//			// Remove from available contributors as clean-up
//			LanguageSettingsExtensionManager.setUserDefinedContributorsInternal(null);
//			assertNull(LanguageSettingsManager.getContributorCopy(TESTING_ID));
//		}
		fail("UNDER CONSTRUCTION");
	}

	/**
	 * Make sure special characters are serialized properly.
	 *
	 * @throws Exception...
	 */
	public void testSerializeRegexContributorSpecialCharacters() throws Exception {
//
//		final String TESTING_ID = "org.eclipse.cdt.core.test.regexlanguage.settings.contributor";
//		final String TESTING_NAME = "<>\"'\\& Error Parser";
//		final String TESTING_REGEX = "Pattern-<>\"'\\&";
//		final String ALL_IDS = toDelimitedString(LanguageSettingsManager.getContributorAvailableIds());
//		{
//			// Create contributor with the same id as in eclipse registry
//			RegexContributor regexContributor = new RegexContributor(TESTING_ID, TESTING_NAME);
//			regexContributor.addPattern(new RegexErrorPattern(TESTING_REGEX,
//					"line-<>\"'\\&", "file-<>\"'\\&", "description-<>\"'\\&", null, IMarkerGenerator.SEVERITY_WARNING, false));
//
//			// Add to available contributors
//			LanguageSettingsExtensionManager.setUserDefinedContributorsInternal(new ICLanguageSettingsContributor[] {regexContributor});
//			assertNotNull(LanguageSettingsManager.getContributorCopy(TESTING_ID));
//			// And serialize in persistent storage
//			LanguageSettingsExtensionManager.serializeUserDefinedContributors();
//		}
//
//		{
//			// Re-load from persistent storage and check it out
//			LanguageSettingsExtensionManager.loadUserDefinedContributors();
//			String all = toDelimitedString(LanguageSettingsManager.getContributorAvailableIds());
//			assertTrue(all.contains(TESTING_ID));
//
//			ICLanguageSettingsContributor contributor = LanguageSettingsManager.getContributorCopy(TESTING_ID);
//			assertNotNull(contributor);
//			assertTrue(contributor instanceof RegexContributor);
//			RegexContributor regexContributor = (RegexContributor)contributor;
//			assertEquals(TESTING_ID, regexContributor.getId());
//			assertEquals(TESTING_NAME, regexContributor.getName());
//
//			RegexErrorPattern[] errorPatterns = regexContributor.getPatterns();
//			assertEquals(1, errorPatterns.length);
//			assertEquals(TESTING_REGEX, errorPatterns[0].getPattern());
//		}
		fail("UNDER CONSTRUCTION");
	}

	/**
	 * Test retrieval of contributor, clone() and equals().
	 *
	 * @throws Exception...
	 */
	public void testGetContributorCopy() throws Exception {
//		{
//			ICLanguageSettingsContributor clone1 = LanguageSettingsManager.getContributorCopy(REGEX_ERRORPARSER_ID);
//			ICLanguageSettingsContributor clone2 = LanguageSettingsManager.getContributorCopy(REGEX_ERRORPARSER_ID);
//			assertEquals(clone1, clone2);
//			assertNotSame(clone1, clone2);
//		}
//		{
//			ICLanguageSettingsContributor clone1 = LanguageSettingsManager.getContributorCopy(GCC_ERRORPARSER_ID);
//			ICLanguageSettingsContributor clone2 = LanguageSettingsManager.getContributorCopy(GCC_ERRORPARSER_ID);
//			assertEquals(clone1, clone2);
//			assertNotSame(clone1, clone2);
//
//			assertTrue(clone1 instanceof ContributorNamedWrapper);
//			assertTrue(clone2 instanceof ContributorNamedWrapper);
//			ICLanguageSettingsContributor gccClone1 = ((ContributorNamedWrapper)clone1).getContributor();
//			ICLanguageSettingsContributor gccClone2 = ((ContributorNamedWrapper)clone2).getContributor();
//			assertNotSame(clone1, clone2);
//		}
		fail("UNDER CONSTRUCTION");
	}

	/**
	 */
	public void testConfigurationDescription_NullContributor() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		{
			// set rough contributor returning null with getSettingEntries()
			ICLanguageSettingsContributor contributorNull = new MockContributor(CONTRIBUTOR_1, CONTRIBUTOR_NAME_1, null);
			List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();
			contributors.add(contributorNull);
			LanguageSettingsManager.setContributors(cfgDescription, contributors);
		}

		// use contributor returning null
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(
					cfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_1);
			assertNotNull(retrieved);
			assertEquals(0, retrieved.size());
		}
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(
					cfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_1, 0);
			assertNotNull(retrieved);
			assertEquals(0, retrieved.size());
		}
		
		{
			// set rough contributor returning null in getSettingEntries() array
			ICLanguageSettingsContributor contributorNull = new MockContributor(CONTRIBUTOR_2, CONTRIBUTOR_NAME_2, 
					new ArrayList<ICLanguageSettingEntry>() {
						{
							add(null);
						}
					}
			);
			List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();
			contributors.add(contributorNull);
			LanguageSettingsManager.setContributors(cfgDescription, contributors);
		}
		
		// use contributor returning null as item in array
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(
					cfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_2);
			assertNotNull(retrieved);
			assertEquals(1, retrieved.size());
		}
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(
					cfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_2, 0);
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

		List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();
		ICLanguageSettingsContributor contributorYes = new MockContributor(CONTRIBUTOR_0, CONTRIBUTOR_NAME_0, null)  {
			@Override
			public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
				if (cfgDescription.getId().equals(modelCfgDescription.getId())) {
					return original;
				}
				return null;
			}

		};
		contributors.add(contributorYes);
		ICLanguageSettingsContributor contributorNo = new MockContributor(CONTRIBUTOR_1, CONTRIBUTOR_NAME_1, null)  {
			@Override
			public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
				if (cfgDescription.getId().equals(modelCfgDescription.getId())) {
					return null;
				}
				return original;
			}
			
		};
		contributors.add(contributorNo);
		LanguageSettingsManager.setContributors(modelCfgDescription, contributors);

		{
			// retrieve the entries for model configuration description
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(modelCfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_0);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
		
		{
			// retrieve the entries for different configuration description
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(modelCfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_1);
			assertEquals(0, retrieved.size());
		}
	}
	
	/**
	 * TODO .
	 *
	 * @throws Exception...
	 */
	public void testConfigurationDescription_Contributors() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		// set contributors
		ICLanguageSettingsContributor contributor1 = new MockContributor(CONTRIBUTOR_1, CONTRIBUTOR_NAME_1, null);
		ICLanguageSettingsContributor contributor2 = new MockContributor(CONTRIBUTOR_2, CONTRIBUTOR_NAME_2, null);
		List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();
		contributors.add(contributor1);
		contributors.add(contributor2);
		LanguageSettingsManager.setContributors(cfgDescription, contributors);

		{
			// get contributors
			List<ICLanguageSettingsContributor> retrieved = LanguageSettingsManager.getContributors(cfgDescription);
			assertEquals(contributor1, retrieved.get(0));
			assertEquals(contributor2, retrieved.get(1));
			assertEquals(contributors.size(), retrieved.size());
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

		// create couple of contributors
		final List<ICLanguageSettingEntry> original1 = new ArrayList<ICLanguageSettingEntry>();
		original1.add(new CIncludePathEntry("value1", 1));
		original1.add(new CIncludePathEntry("value2", 2));

		final List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("value1", 1));
		original2.add(new CIncludePathEntry("value2", 2));
		original2.add(new CIncludePathEntry("value3", 2));

		ICLanguageSettingsContributor contributor1 = new MockContributor(CONTRIBUTOR_1, CONTRIBUTOR_NAME_1, original1);
		ICLanguageSettingsContributor contributor2 = new MockContributor(CONTRIBUTOR_2, CONTRIBUTOR_NAME_2, original2);
		List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();
		contributors.add(contributor1);
		contributors.add(contributor2);
		LanguageSettingsManager.setContributors(cfgDescription, contributors);

		{
			// get list of contributors
			List<ICLanguageSettingsContributor> all = LanguageSettingsManager.getContributors(cfgDescription);
			assertTrue(all.contains(contributor1));
			assertTrue(all.contains(contributor2));
			assertTrue(all.size()>=2);
		}

		{
			// retrieve the entries for contributor-1
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_1);

			assertNotSame(original1, retrieved);
			assertEquals(original1.size(), retrieved.size());
			ICLanguageSettingEntry[] originalArray = original1.toArray(new ICLanguageSettingEntry[0]);
			ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
			for (int i=0;i<original1.size();i++) {
				assertEquals(originalArray[i], retrievedArray[i]);
			}
		}

		{
			// retrieve the entries for contributor-2
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_2);

			assertNotSame(original2, retrieved);
			assertEquals(original2.size(), retrieved.size());
			ICLanguageSettingEntry[] originalArray = original2.toArray(new ICLanguageSettingEntry[0]);
			ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
			for (int i=0;i<original2.size();i++) {
				assertEquals(originalArray[i], retrievedArray[i]);
			}
		}

		// TODO: for some other contributor?
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
//			List<ICLanguageSettingEntry> retrieved_2 = manager.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_2);
//			assertEquals(original2.size(), retrieved_2.size());
//		}
//
//		// add entries to the end
//		{
//			List<ICLanguageSettingEntry> original2a = new ArrayList<ICLanguageSettingEntry>();
//			original2a.add(new CIncludePathEntry("value4a", 1));
//			original2a.add(new CIncludePathEntry("value5a", 2));
//
//			manager.addSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_2, original2a);
//			List<ICLanguageSettingEntry> retrieved = manager.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_2);
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

		ICLanguageSettingsContributor contributor0 = new MockContributor(CONTRIBUTOR_0, CONTRIBUTOR_NAME_0, original);
		List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();
		contributors.add(contributor0);
		LanguageSettingsManager.setContributors(cfgDescription, contributors);

		{
			// retrieve entries by kind
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager
				.getSettingEntries(cfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_0, ICSettingEntry.INCLUDE_PATH);
			assertEquals(3, includes.size());
			assertEquals(new CIncludePathEntry("path0", 0),includes.get(0));
			assertEquals(new CIncludePathEntry("path1", 0),includes.get(1));
			assertEquals(new CIncludePathEntry("path2", 0),includes.get(2));

			List<ICLanguageSettingEntry> macros = LanguageSettingsManager
				.getSettingEntries(cfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_0, ICSettingEntry.MACRO);
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

		ICLanguageSettingsContributor contributor0 = new MockContributor(CONTRIBUTOR_0, CONTRIBUTOR_NAME_0, original);
		List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();
		contributors.add(contributor0);
		LanguageSettingsManager.setContributors(cfgDescription, contributors);

		{
			// retrieve entries by kind, only first entry is returned
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager
				.getSettingEntries(cfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_0, ICSettingEntry.INCLUDE_PATH);
			assertEquals(1, includes.size());
			assertEquals(original.get(0),includes.get(0));
		}

	}

	/**
	 */
	public void testConfigurationDescription_ReconciledContributors() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		// contribute the entries
		List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();

		// contribute the higher ranked entries
		List<ICLanguageSettingEntry> originalHigh = new ArrayList<ICLanguageSettingEntry>();
		originalHigh.add(new CIncludePathEntry("path0", ICSettingEntry.RESOLVED));
		originalHigh.add(new CIncludePathEntry("path1", 0));
		originalHigh.add(new CIncludePathEntry("path2", ICSettingEntry.UNDEFINED));
		ICLanguageSettingsContributor highRankContributor = new MockContributor(CONTRIBUTOR_2, CONTRIBUTOR_NAME_2, originalHigh);
		contributors.add(highRankContributor);

		// contribute the lower ranked entries
		List<ICLanguageSettingEntry> originalLow = new ArrayList<ICLanguageSettingEntry>();
		originalLow.add(new CIncludePathEntry("path0", ICSettingEntry.BUILTIN));
		originalLow.add(new CIncludePathEntry("path1", ICSettingEntry.UNDEFINED));
		originalLow.add(new CIncludePathEntry("path2", 0));
		originalLow.add(new CIncludePathEntry("path3", 0));
		ICLanguageSettingsContributor lowRankContributor = new MockContributor(CONTRIBUTOR_1, CONTRIBUTOR_NAME_1, originalLow);
		contributors.add(lowRankContributor);

		LanguageSettingsManager.setContributors(cfgDescription, contributors);

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
		List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();
		ICLanguageSettingsContributor contributor = new MockContributor(CONTRIBUTOR_0, CONTRIBUTOR_NAME_0, null)  {
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
		contributors.add(contributor);
		LanguageSettingsManager.setContributors(cfgDescription, contributors);

		{
			// retrieve entries for a derived resource (in a subfolder)
			IFile derived = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/resource");
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, derived, LANG_ID, CONTRIBUTOR_0);
			// taken from parent folder
			assertEquals(original.get(0),retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}

		{
			// retrieve entries for not related resource
			IFile notRelated = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/AnotherFolder/Subfolder/resource"));
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, notRelated, LANG_ID, CONTRIBUTOR_0);
			assertEquals(0, retrieved.size());
		}

		{
			// test distinction between no settings and empty settings
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, emptySettingsPath, LANG_ID, CONTRIBUTOR_0);
			// NOT taken from parent folder
			assertEquals(0, retrieved.size());
		}
	}

	/**
	 */
	public void testConfigurationDescription_ContributorIds() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		{
			// ensure no test contributor is set yet
			List<String> ids = LanguageSettingsManager.getContributorIds(cfgDescription);
			assertFalse(ids.contains(DEFAULT_CONTRIBUTOR_ID_EXT));
		}

		{
			// set test contributor
			List<String> ids = new ArrayList<String>();
			ids.add(DEFAULT_CONTRIBUTOR_ID_EXT);
			LanguageSettingsManager.setContributorIds(cfgDescription, ids);
		}

		{
			// check that test contributor got there
			List<String> ids = LanguageSettingsManager.getContributorIds(cfgDescription);
			assertTrue(ids.contains(DEFAULT_CONTRIBUTOR_ID_EXT));
		}
	}

	/**
	 */
	public void testConfigurationDescription_SerializeContributors() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);

		IProject project = cproject.getProject();
		ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project, true);

		ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		{
			// ensure no test contributor is set yet
			List<String> ids = LanguageSettingsManager.getContributorIds(cfgDescription);
			assertFalse(ids.contains(DEFAULT_CONTRIBUTOR_ID_EXT));
		}
		{
			// set test contributor
			List<String> ids = new ArrayList<String>();
			ids.add(DEFAULT_CONTRIBUTOR_ID_EXT);
			LanguageSettingsManager.setContributorIds(cfgDescription, ids);
		}
		{
			// check that test contributor got there
			List<String> ids = LanguageSettingsManager.getContributorIds(cfgDescription);
			assertTrue(ids.contains(DEFAULT_CONTRIBUTOR_ID_EXT));
		}

		{
			// serialize
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
			// close and reopen the project
			project.close(null);
			project.open(null);
		}

		{
			// check that test contributor got loaded
			ICConfigurationDescription[] loadedCfgDescriptions = getConfigurationDescriptions(cproject.getProject());
			ICConfigurationDescription loadedCfgDescription = loadedCfgDescriptions[0];
			assertTrue(cfgDescription instanceof CConfigurationDescription);

			List<String> ids = LanguageSettingsManager.getContributorIds(loadedCfgDescription);
			assertTrue(ids.contains(DEFAULT_CONTRIBUTOR_ID_EXT));
		}

	}

	/**
	 */
	public void testConfigurationDescription_SerializeContributorData_Basic() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(cproject.getProject());

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));

		class ClearableMockContributor extends ACLanguageSettingsSerializableContributor {
			private List<ICLanguageSettingEntry> entries;
			public ClearableMockContributor(String id, String name, List<ICLanguageSettingEntry> entries) {
				super(id, name);
				this.entries = entries;
			}
			public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
				return entries;
			}
			public void clear() {
				entries = null;
			}
		}
		ClearableMockContributor contributor = new ClearableMockContributor(CONTRIBUTOR_0, CONTRIBUTOR_NAME_0, original);

		// add mock serializable contributor
		List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();
		contributors.add(contributor);
		LanguageSettingsManager.setContributors(cfgDescription, contributors);

		{
			// double-check that contributor returns proper data
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_0);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}

		// serialize
		LanguageSettingsManager.serialize();

		// clear contributor
		contributor.clear();
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_0);
			assertEquals(0, retrieved.size());
		}

		// re-load
		LanguageSettingsManager.load();
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(cfgDescription, FILE_0, LANG_ID, CONTRIBUTOR_0);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
	}

	/**
	 */
	public void testDefaultContributor() throws Exception {
		final List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<String> languages = new ArrayList<String>(2) {
			{
				add("bogus.language.id");
				add(LANG_ID);
			}
		};

		// add default contributor
		LanguageSettingsBaseContributor contributor = new LanguageSettingsBaseContributor(
				CONTRIBUTOR_0, CONTRIBUTOR_NAME_0, languages, original);

		{
			// attempt to get entries for wrong language
			List<ICLanguageSettingEntry> retrieved = contributor.getSettingEntries(null, FILE_0, "wrong.lang.id");
			assertEquals(0, retrieved.size());
		}

		{
			// retrieve the entries
			List<ICLanguageSettingEntry> retrieved = contributor.getSettingEntries(null, FILE_0, LANG_ID);
			assertEquals(original.get(0), retrieved.get(0));
		}

	}

}
