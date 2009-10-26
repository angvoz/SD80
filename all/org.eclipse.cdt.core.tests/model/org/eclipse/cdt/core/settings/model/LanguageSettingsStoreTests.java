/*******************************************************************************
 * Copyright (c) 2009, 2009 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.internal.errorparsers.tests.ResourceHelper;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsResourceDescriptor;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsDefaultContributor;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Tests for LanguageSettingsStore and LanguageSettingsManager
 */
public class LanguageSettingsStoreTests extends BaseTestCase {
	private static final String CONFIGURATION_ID = "cfg.id";
	private static final IPath PATH_0 = new Path("/path0");
	private static final String LANG_ID = "test.lang.id";
	/* as defined in test extension point */
	private static final String CONTRIBUTOR_ID_EXT = "org.eclipse.cdt.core.tests.language.settings.contributor";
	private static final String LANG_ID_EXT = "org.eclipse.cdt.core.tests.language.id";
	private static final String CONTRIBUTOR_0 = "test.contributor.0.id";
	private static final String CONTRIBUTOR_1 = "test.contributor.1.id";
	private static final String CONTRIBUTOR_2 = "test.contributor.2.id";
	private static final String CONTRIBUTOR_NAME = "test.contributor.name";

	private static final LanguageSettingsResourceDescriptor RC_DESCRIPTOR = new LanguageSettingsResourceDescriptor(CONFIGURATION_ID, PATH_0, LANG_ID);
	private static final LanguageSettingsResourceDescriptor RC_DESCRIPTOR_EXT = new LanguageSettingsResourceDescriptor(CONFIGURATION_ID, PATH_0, LANG_ID_EXT);

	private class MockContributor extends ACLanguageSettingsContributor {
		private final List<ICLanguageSettingEntry> entries;

		public MockContributor(String id, int rank, List<ICLanguageSettingEntry> entries) {
			super(id, CONTRIBUTOR_NAME, rank);
			this.entries = entries;
		}

		public List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor) {
			return entries;
		}
	}

	public static TestSuite suite() {
		return suite(LanguageSettingsStoreTests.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		for (ICLanguageSettingsContributor contributor : LanguageSettingsManager.getAllContributors()) {
			LanguageSettingsManager.removeContributor(contributor.getId());
		}
//		ResourceHelper.cleanUp();
	}

	/**
	 */
	public void testEmptyStore() throws Exception {
		// retrieve the entries for provider-1
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0);
			assertNotNull(retrieved);
			assertEquals(0, retrieved.size());
		}

		// retrieve entries by kind
		{
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager.getSettingEntries(
					RC_DESCRIPTOR, CONTRIBUTOR_0, ICSettingEntry.INCLUDE_PATH);
			assertNotNull(includes);
			assertEquals(0, includes.size());
		}

		// get contributors
		{
			List<ICLanguageSettingsContributor> contributors = LanguageSettingsManager.getAllContributors();
			assertNotNull(contributors);
			assertEquals(0, contributors.size());
		}
	}

	/**
	 */
	public void testAddRemoveContributor() throws Exception {
		ICLanguageSettingsContributor contributor = new MockContributor(CONTRIBUTOR_0, 10, null);
		{
			// add a contributor
			LanguageSettingsManager.addContributor(contributor);
			List<ICLanguageSettingsContributor> contributors = LanguageSettingsManager.getAllContributors();
			assertEquals(1, contributors.size());
		}

		{
			// remove contributor
			LanguageSettingsManager.removeContributor(CONTRIBUTOR_0);

			List<ICLanguageSettingsContributor> contributors = LanguageSettingsManager.getAllContributors();
			assertEquals(0, contributors.size());
		}
	}

	/**
	 */
	public void testLanguageSettingsStore_Basic() throws Exception {
		LanguageSettingsStore store = new LanguageSettingsStore(null);

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("value1", 1));
		original.add(new CIncludePathEntry("value2", 2));

		// store and retrieve the entries
		store.setSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0, original);
		List<ICLanguageSettingEntry> retrieved = store.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0);

		assertNotSame(original, retrieved);
		assertEquals(original.size(), retrieved.size());
		ICLanguageSettingEntry[] originalArray = original.toArray(new ICLanguageSettingEntry[0]);
		ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
		for (int i=0;i<original.size();i++) {
			assertEquals(originalArray[i], retrievedArray[i]);
		}

		// check providers
		List<String> providers = store.getProviders();
		assertEquals(CONTRIBUTOR_0, providers.get(0));
		assertEquals(1, providers.size());

		// check descriptors
		List<LanguageSettingsResourceDescriptor> descriptors = store.getDescriptors(CONTRIBUTOR_0);
		assertEquals(RC_DESCRIPTOR, descriptors.get(0));
		assertEquals(1, descriptors.size());
	}

	/**
	 */
	public void testLanguageSettingsStore_LoadEmpty() throws Exception {
		IProject project = ResourceHelper.createCDTProject(getName());
		IFile settingsFile = project.getFile("lang-settings.xml");
		{
			// no file available
			assertFalse(settingsFile.exists());

			// load
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.load();
			assertEquals(0, store.getProviders().size());
			// no file is created
			assertFalse(settingsFile.exists());
		}

	}

	/**
	 */
	public void testLanguageSettingsStore_Serialize_CIncludePathEntry() throws Exception {
		IProject project = ResourceHelper.createCDTProject(getName());
		IFile settingsFile = project.getFile("lang-settings.xml");

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 1));

		{
			// serialize
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.setSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0, original);
			store.serialize();
			assertTrue(settingsFile.exists());
		}
		{
			// load
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.load();

			List<String> providers = store.getProviders();
			assertEquals(CONTRIBUTOR_0, providers.get(0));
			assertEquals(1, providers.size());

			List<LanguageSettingsResourceDescriptor> descriptors = store.getDescriptors(CONTRIBUTOR_0);
			assertEquals(RC_DESCRIPTOR, descriptors.get(0));
			assertEquals(1, descriptors.size());

			List<ICLanguageSettingEntry> entries = store.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0);
			ICLanguageSettingEntry entry = entries.get(0);
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
	public void testLanguageSettingsStore_Serialize_CIncludeFileEntry() throws Exception {
		IProject project = ResourceHelper.createCDTProject(getName());
		IFile settingsFile = project.getFile("lang-settings.xml");

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludeFileEntry("name", 1));

		{
			// serialize
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.setSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0, original);
			store.serialize();
			assertTrue(settingsFile.exists());
		}
		{
			// load
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.load();

			List<String> providers = store.getProviders();
			assertEquals(CONTRIBUTOR_0, providers.get(0));
			assertEquals(1, providers.size());

			List<LanguageSettingsResourceDescriptor> descriptors = store.getDescriptors(CONTRIBUTOR_0);
			assertEquals(RC_DESCRIPTOR, descriptors.get(0));
			assertEquals(1, descriptors.size());

			List<ICLanguageSettingEntry> entries = store.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0);
			ICLanguageSettingEntry entry = entries.get(0);
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
	public void testLanguageSettingsStore_Serialize_CMacroEntry() throws Exception {
		IProject project = ResourceHelper.createCDTProject(getName());
		IFile settingsFile = project.getFile("lang-settings.xml");

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CMacroEntry("MACRO0", "value0",1));

		{
			// serialize
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.setSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0, original);
			store.serialize();
			assertTrue(settingsFile.exists());
		}
		{
			// load
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.load();

			List<String> providers = store.getProviders();
			assertEquals(CONTRIBUTOR_0, providers.get(0));
			assertEquals(1, providers.size());

			List<LanguageSettingsResourceDescriptor> descriptors = store.getDescriptors(CONTRIBUTOR_0);
			assertEquals(RC_DESCRIPTOR, descriptors.get(0));
			assertEquals(1, descriptors.size());

			List<ICLanguageSettingEntry> entries = store.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0);
			ICLanguageSettingEntry entry = entries.get(0);
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
	public void testLanguageSettingsStore_Serialize_CMacroFileEntry() throws Exception {
		IProject project = ResourceHelper.createCDTProject(getName());
		IFile settingsFile = project.getFile("lang-settings.xml");

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CMacroFileEntry("name", 1));

		{
			// serialize
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.setSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0, original);
			store.serialize();
			assertTrue(settingsFile.exists());
		}
		{
			// load
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.load();

			List<String> providers = store.getProviders();
			assertEquals(CONTRIBUTOR_0, providers.get(0));
			assertEquals(1, providers.size());

			List<LanguageSettingsResourceDescriptor> descriptors = store.getDescriptors(CONTRIBUTOR_0);
			assertEquals(RC_DESCRIPTOR, descriptors.get(0));
			assertEquals(1, descriptors.size());

			List<ICLanguageSettingEntry> entries = store.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0);
			ICLanguageSettingEntry entry = entries.get(0);
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
	public void testLanguageSettingsStore_Serialize_CLibraryPathEntry() throws Exception {
		IProject project = ResourceHelper.createCDTProject(getName());
		IFile settingsFile = project.getFile("lang-settings.xml");

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CLibraryPathEntry("name", 1));

		{
			// serialize
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.setSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0, original);
			store.serialize();
			assertTrue(settingsFile.exists());
		}
		{
			// load
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.load();

			List<String> providers = store.getProviders();
			assertEquals(CONTRIBUTOR_0, providers.get(0));
			assertEquals(1, providers.size());

			List<LanguageSettingsResourceDescriptor> descriptors = store.getDescriptors(CONTRIBUTOR_0);
			assertEquals(RC_DESCRIPTOR, descriptors.get(0));
			assertEquals(1, descriptors.size());

			List<ICLanguageSettingEntry> entries = store.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0);
			ICLanguageSettingEntry entry = entries.get(0);
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
	public void testLanguageSettingsStore_Serialize_CLibraryFileEntry() throws Exception {
		IProject project = ResourceHelper.createCDTProject(getName());
		IFile settingsFile = project.getFile("lang-settings.xml");

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CLibraryFileEntry("name", 1));
		// TODO:
//		public CLibraryFileEntry(String value,
//				int flags,
//				IPath sourceAttachmentPath,
//				IPath sourceAttachmentRootPath,
//				IPath sourceAttachmentPrefixMapping) {


		{
			// serialize
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.setSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0, original);
			store.serialize();
			assertTrue(settingsFile.exists());
		}
		{
			// load
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.load();

			List<String> providers = store.getProviders();
			assertEquals(CONTRIBUTOR_0, providers.get(0));
			assertEquals(1, providers.size());

			List<LanguageSettingsResourceDescriptor> descriptors = store.getDescriptors(CONTRIBUTOR_0);
			assertEquals(RC_DESCRIPTOR, descriptors.get(0));
			assertEquals(1, descriptors.size());

			List<ICLanguageSettingEntry> entries = store.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0);
			ICLanguageSettingEntry entry = entries.get(0);
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
	public void testLanguageSettingsStore_Serialize_Mixed() throws Exception {
		IProject project = ResourceHelper.createCDTProject(getName());
		IFile settingsFile = project.getFile("lang-settings.xml");
		assertFalse(settingsFile.exists());

		List<ICLanguageSettingEntry> original_1 = new ArrayList<ICLanguageSettingEntry>();
		original_1.add(new CMacroEntry("MACRO0", "value0",1));
		original_1.add(new CIncludePathEntry("path0", 1));
		original_1.add(new CIncludePathEntry("path1", 1));

		List<ICLanguageSettingEntry> original_2 = new ArrayList<ICLanguageSettingEntry>();
		original_2.add(new CIncludePathEntry("path0", 2));
		original_2.add(new CIncludePathEntry("path1", 2));
		original_2.add(new CMacroEntry("MACRO1", "value1",2));
		original_2.add(new CIncludePathEntry("path2", 2));
		{
			// Serialize settings
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.setSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_1, original_1);
			store.setSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_2, original_2);
			store.serialize();
			assertTrue(settingsFile.exists());
		}

		{
			// load settings
			LanguageSettingsStore store = new LanguageSettingsStore(settingsFile);
			store.load();

			List<ICLanguageSettingEntry> entries_1 = store.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_1);
			assertEquals(original_1.get(0), entries_1.get(0));
			assertEquals(original_1.get(1), entries_1.get(1));
			assertEquals(original_1.size(), entries_1.size());

			List<ICLanguageSettingEntry> entries_2 = store.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_2);
			assertEquals(original_2.get(0), entries_2.get(0));
			assertEquals(original_2.get(1), entries_2.get(1));
			assertEquals(original_2.size(), entries_2.size());
		}
	}

	/**
	 */
	public void testLanguageSettingsManager_NullContributor() throws Exception {
		ICLanguageSettingsContributor contributor0 = new MockContributor(CONTRIBUTOR_1, 10, null);
		LanguageSettingsManager.addContributor(contributor0);
		List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_1);
		assertNotNull(retrieved);
		assertEquals(0, retrieved.size());
	}

	/**
	 */
	public void testLanguageSettingsManager_Basic() throws Exception {
		final List<ICLanguageSettingEntry> original1 = new ArrayList<ICLanguageSettingEntry>();
		original1.add(new CIncludePathEntry("value1", 1));
		original1.add(new CIncludePathEntry("value2", 2));

		final List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("value1", 1));
		original2.add(new CIncludePathEntry("value2", 2));
		original2.add(new CIncludePathEntry("value3", 2));

		// create couple of contributors
		ICLanguageSettingsContributor contributor1 = new MockContributor(CONTRIBUTOR_1, 10, original1);
		ICLanguageSettingsContributor contributor2 = new MockContributor(CONTRIBUTOR_2, 10, original2);

		LanguageSettingsManager.addContributor(contributor1);
		LanguageSettingsManager.addContributor(contributor2);

		{
			// get list of contributors
			List<ICLanguageSettingsContributor> contributors = LanguageSettingsManager.getAllContributors();
			assertTrue(contributors.contains(contributor1));
			assertTrue(contributors.contains(contributor1));
			assertEquals(2, contributors.size());
		}

		{
			// retrieve the entries for contributor-1
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_1);

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
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_2);

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
	public void testLanguageSettingsManager_Filtered() throws Exception {
		// contribute the entries
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		original.add(new CMacroEntry("MACRO0", "value0",0));
		original.add(new CIncludePathEntry("path1", 0));
		original.add(new CMacroEntry("MACRO1", "value1",0));
		original.add(new CIncludePathEntry("path2", 0));
		ICLanguageSettingsContributor contributor = new MockContributor(CONTRIBUTOR_0, 10, original);
		LanguageSettingsManager.addContributor(contributor);

		{
			// retrieve entries by kind
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager
				.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0, ICSettingEntry.INCLUDE_PATH);
			assertEquals(3, includes.size());
			assertEquals(new CIncludePathEntry("path0", 0),includes.get(0));
			assertEquals(new CIncludePathEntry("path1", 0),includes.get(1));
			assertEquals(new CIncludePathEntry("path2", 0),includes.get(2));

			List<ICLanguageSettingEntry> macros = LanguageSettingsManager
				.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0, ICSettingEntry.MACRO);
			assertEquals(2, macros.size());
			assertEquals(new CMacroEntry("MACRO0", "value0",0), macros.get(0));
			assertEquals(new CMacroEntry("MACRO1", "value1",0), macros.get(1));
		}

	}

	/**
	 */
	public void testLanguageSettingsManager_FilteredConflicting() throws Exception {
		// contribute the entries
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path", ICSettingEntry.BUILTIN));
		original.add(new CIncludePathEntry("path", ICSettingEntry.UNDEFINED));
		original.add(new CIncludePathEntry("path", 0));
		ICLanguageSettingsContributor contributor = new MockContributor(CONTRIBUTOR_0, 10, original);
		LanguageSettingsManager.addContributor(contributor);

		{
			// retrieve entries by kind, only first entry is returned
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager
				.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0, ICSettingEntry.INCLUDE_PATH);
			assertEquals(1, includes.size());
			assertEquals(original.get(0),includes.get(0));
		}

	}

	/**
	 * TODO: revisit the test after priorities are built in the new extension point
	 */
	public void testLanguageSettingsManager_ReconciledContributors() throws Exception {
		// contribute the low ranked entries
		List<ICLanguageSettingEntry> originalLow = new ArrayList<ICLanguageSettingEntry>();
		originalLow.add(new CIncludePathEntry("path0", ICSettingEntry.BUILTIN));
		originalLow.add(new CIncludePathEntry("path1", ICSettingEntry.UNDEFINED));
		originalLow.add(new CIncludePathEntry("path2", 0));
		originalLow.add(new CIncludePathEntry("path3", 0));
		ICLanguageSettingsContributor lowRankContributor = new MockContributor(CONTRIBUTOR_1, 20, originalLow);
		LanguageSettingsManager.addContributor(lowRankContributor);

		// contribute the high ranked entries
		List<ICLanguageSettingEntry> originalHigh = new ArrayList<ICLanguageSettingEntry>();
		originalHigh.add(new CIncludePathEntry("path0", ICSettingEntry.RESOLVED));
		originalHigh.add(new CIncludePathEntry("path1", 0));
		originalHigh.add(new CIncludePathEntry("path2", ICSettingEntry.UNDEFINED));
		ICLanguageSettingsContributor highRankContributor = new MockContributor(CONTRIBUTOR_2, 10, originalHigh);
		LanguageSettingsManager.addContributor(highRankContributor);


		// retrieve entries by kind
		{
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager
				.getSettingEntriesReconciled(RC_DESCRIPTOR, ICSettingEntry.INCLUDE_PATH);
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
	public void testLanguageSettingsManager_ParentFolder() throws Exception {
		final IPath parentFolder = new Path("/ParentFolder/");
		final Path emptySettingsPath = new Path("/ParentFolder/Subfolder/empty");
		LanguageSettingsResourceDescriptor parentDescriptor = new LanguageSettingsResourceDescriptor(
				CONFIGURATION_ID, parentFolder, LANG_ID);

		// store the entries in parent folder
		final List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		ICLanguageSettingsContributor contributor = new ACLanguageSettingsContributor(CONTRIBUTOR_0, CONTRIBUTOR_NAME, 10) {

			public List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor) {
				if (descriptor.getWorkspacePath().equals(parentFolder)) {
					return original;
				}
				if (descriptor.getWorkspacePath().equals(emptySettingsPath)) {
					return new ArrayList<ICLanguageSettingEntry>(0);
				}
				return null;
			}

		};
		LanguageSettingsManager.addContributor(contributor);

		{
			// retrieve entries for a derived resource (in a subfolder)
			LanguageSettingsResourceDescriptor missingDescriptor = new LanguageSettingsResourceDescriptor(
					CONFIGURATION_ID, new Path("/ParentFolder/Subfolder/resource"), LANG_ID);
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(missingDescriptor, CONTRIBUTOR_0);
			// taken from parent folder
			assertEquals(original.get(0),retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}

		{
			// retrieve entries for not related resource
			LanguageSettingsResourceDescriptor missingDescriptor = new LanguageSettingsResourceDescriptor(
					CONFIGURATION_ID, new Path("/AnotherFolder/Subfolder/resource"), LANG_ID);
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(missingDescriptor, CONTRIBUTOR_0);
			assertEquals(0, retrieved.size());
		}

		{
			// test distinction between no settings and empty settings
			LanguageSettingsResourceDescriptor emptyDescriptor = new LanguageSettingsResourceDescriptor(
					CONFIGURATION_ID, emptySettingsPath, LANG_ID);
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(emptyDescriptor, CONTRIBUTOR_0);
			// NOT taken from parent folder
			assertEquals(0, retrieved.size());
		}
	}

	/**
	 */
	public void testLanguageSettingsCoreContributor() throws Exception {
		final List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		List<String> languages = new ArrayList<String>(2) {
			{
				add("bogus.language.id");
				add(RC_DESCRIPTOR.getLangId());
			}
		};
		LanguageSettingsDefaultContributor contributor = new LanguageSettingsDefaultContributor(
				CONTRIBUTOR_0, CONTRIBUTOR_NAME, 10, languages, original);

		{
			LanguageSettingsResourceDescriptor descriptor = new LanguageSettingsResourceDescriptor(
					CONFIGURATION_ID, PATH_0, "wrong.lang.id");
			List<ICLanguageSettingEntry> retrieved = contributor.getSettingEntries(descriptor);
			assertEquals(0, retrieved.size());
		}

		{
			List<ICLanguageSettingEntry> retrieved = contributor.getSettingEntries(RC_DESCRIPTOR);
			assertEquals(original.get(0), retrieved.get(0));
		}

	}

	/**
	 */
	public void testExtensionPoint() throws Exception {
		int pos = Arrays.binarySearch(LanguageSettingsManager.getLanguageSettingsExtensionIds(), CONTRIBUTOR_ID_EXT);
		assertTrue("extension " + CONTRIBUTOR_ID_EXT + " not found", pos>=0);

		ICLanguageSettingsContributor contributorExt = LanguageSettingsManager.getContributor(CONTRIBUTOR_ID_EXT);
		assertNotNull(contributorExt);

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
	 */
	public void testLanguageSettingsManager_Serialize_Basic() throws Exception {
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));

		class ClearableMockContributor extends ACLanguageSettingsSerializableContributor {
			private List<ICLanguageSettingEntry> entries;
			public ClearableMockContributor(String id, int rank, List<ICLanguageSettingEntry> entries) {
				super(id, CONTRIBUTOR_NAME, rank);
				this.entries = entries;
			}
			public List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor) {
				return entries;
			}
			public void clear() {
				entries = null;
			}
		}
		ClearableMockContributor contributor = new ClearableMockContributor(CONTRIBUTOR_0, 10, original);


		// add mock serializable contributor
		LanguageSettingsManager.addContributor(contributor);
		{
			// double-check that contributor returns proper data
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}

		// serialize
		LanguageSettingsManager.serialize();

		// clear contributor
		contributor.clear();
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0);
			assertEquals(0, retrieved.size());
		}

		// re-load
		LanguageSettingsManager.load();
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(RC_DESCRIPTOR, CONTRIBUTOR_0);
			assertEquals(original.get(0), retrieved.get(0));
			assertEquals(original.size(), retrieved.size());
		}
	}

}
