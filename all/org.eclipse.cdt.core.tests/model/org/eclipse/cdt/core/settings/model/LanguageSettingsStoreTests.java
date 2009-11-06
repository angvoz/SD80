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
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.internal.errorparsers.tests.ResourceHelper;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsResourceDescriptor;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
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

	public static TestSuite suite() {
		return suite(LanguageSettingsStoreTests.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
//		ResourceHelper.cleanUp();
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

}
