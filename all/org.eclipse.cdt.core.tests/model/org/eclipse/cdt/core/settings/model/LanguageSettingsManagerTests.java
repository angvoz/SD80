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

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsResourceDescriptor;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescription;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsDefaultContributor;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsExtensionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Test cases testing RegexContributor functionality
 */
public class LanguageSettingsManagerTests extends TestCase {
	// These should match id and name of extension point defined in plugin.xml
	private static final String CONTRIBUTOR_ID_EXT = "org.eclipse.cdt.core.tests.language.settings.contributor";
	private static final String CONTRIBUTOR_NAME_EXT = "Test Plugin Language Settings Contributor";

	private static final String CONFIGURATION_ID = "cfg.id";
	private static final IPath PATH_0 = new Path("/path0");
	private static final String LANG_ID = "test.lang.id";
	/* as defined in test extension point */
	private static final String LANG_ID_EXT = "org.eclipse.cdt.core.tests.language.id";
	private static final String CONTRIBUTOR_0 = "test.contributor.0.id";
	private static final String CONTRIBUTOR_1 = "test.contributor.1.id";
	private static final String CONTRIBUTOR_2 = "test.contributor.2.id";
	private static final String CONTRIBUTOR_NAME_0 = "test.contributor.0.name";
	private static final String CONTRIBUTOR_NAME_1 = "test.contributor.1.name";
	private static final String CONTRIBUTOR_NAME_2 = "test.contributor.2.name";

	private static final LanguageSettingsResourceDescriptor RC_DESCRIPTOR = new LanguageSettingsResourceDescriptor(CONFIGURATION_ID, PATH_0, LANG_ID);
	private static final LanguageSettingsResourceDescriptor RC_DESCRIPTOR_EXT = new LanguageSettingsResourceDescriptor(CONFIGURATION_ID, PATH_0, LANG_ID_EXT);

	private class MockContributor extends ACLanguageSettingsContributor {
		private final List<ICLanguageSettingEntry> entries;

		public MockContributor(String id, String name, List<ICLanguageSettingEntry> entries) {
			super(id, name, 10);
			this.entries = entries;
		}

		public List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor) {
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

	/**
	 * Check that regular ICLanguageSettingsContributor extension defined in plugin.xml is accessible.
	 *
	 * @throws Exception...
	 */
	public void testExtension() throws Exception {
		int pos = Arrays.binarySearch(LanguageSettingsManager.getContributorExtensionIds(), CONTRIBUTOR_ID_EXT);
		assertTrue("extension " + CONTRIBUTOR_ID_EXT + " not found", pos>=0);

		// get test plugin extension contributor
		ICLanguageSettingsContributor contributorExt = LanguageSettingsManager.getContributor(CONTRIBUTOR_ID_EXT);
		assertNotNull(contributorExt);

		// check getAllContributors()
		List<ICLanguageSettingsContributor> allContributors = LanguageSettingsExtensionManager.getAllContributorsFIXME();
		assertTrue(allContributors.contains(contributorExt));
		List<ICLanguageSettingsContributor> allContributors2 = LanguageSettingsManager.getAllContributors();
		assertTrue(allContributors2.contains(contributorExt));

		assertTrue(contributorExt instanceof LanguageSettingsDefaultContributor);
		LanguageSettingsDefaultContributor contributor = (LanguageSettingsDefaultContributor)contributorExt;

		// retrieve wrong language
		assertEquals(0, contributor.getSettingEntries(RC_DESCRIPTOR).size());

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
		List<ICLanguageSettingEntry> retrieved = contributor.getSettingEntries(RC_DESCRIPTOR_EXT);
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
	public void testSerializeContributor() throws Exception {
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
	 * FIXME .
	 *
	 * @throws Exception...
	 */
	public void test_SEPARATOR() throws Exception {
	}

	/**
	 * TODO .
	 *
	 * @throws Exception...
	 */
	public void testConfigurationDescription() throws Exception {
		String projectName = getName();

		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();

		// Create model project and accompanied descriptions
		ICProject cproject = CProjectHelper.createNewStileCProject(projectName, IPDOMManager.ID_NO_INDEXER);
		IProject project = cproject.getProject();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project);
		assertNotNull(projectDescription);
		assertEquals(1, projectDescription.getConfigurations().length);
		// configuration description
		ICConfigurationDescription cfgDescription = projectDescription.getConfigurations()[0];
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
}
