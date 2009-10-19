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

import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsResourceDescriptor;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Tests for LanguageSettingsStore and LanguageSettingsManager
 */
public class LanguageSettingsStoreTests extends BaseTestCase {
	private static final String CONFIGURATION_ID = "cfg.id";
	private static final IPath WORKSPACE_PATH = new Path("/workspacePath");
	private static final String LANG_ID = "lang.id";
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_1 = "test.provider.1.id";
	private static final String PROVIDER_2 = "test.provider.2.id";
	private static final String HIGH_PRIORITY_PROVIDER = LanguageSettingsManager.PROVIDER_UI_USER;
	private static final String LOW_PRIORITY_PROVIDER = LanguageSettingsManager.PROVIDER_UNKNOWN;

	private static final LanguageSettingsResourceDescriptor RC_DESCRIPTOR = new LanguageSettingsResourceDescriptor(CONFIGURATION_ID, WORKSPACE_PATH, LANG_ID);

	public static TestSuite suite() {
		return suite(LanguageSettingsStoreTests.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		LanguageSettingsManager.clear();
	}

	/**
	 */
	public void testEmptyStore() throws Exception {
		LanguageSettingsManager manager = new LanguageSettingsManager(null);

		// retrieve the entries for provider-1
		{
			List<ICLanguageSettingEntry> retrieved = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_0);
			assertNotNull(retrieved);
			assertEquals(0, retrieved.size());
		}

		// retrieve entries by kind
		{
			List<ICLanguageSettingEntry> includes = manager.getSettingEntries(
					RC_DESCRIPTOR, PROVIDER_0, ICSettingEntry.INCLUDE_PATH);
			assertNotNull(includes);
			assertEquals(0, includes.size());
		}

		// get providers
		{
			List<String> providers = manager.getProviders(RC_DESCRIPTOR);
			assertNotNull(providers);
			assertEquals(0, providers.size());
		}
	}

	/**
	 */
	public void testClear() throws Exception {
		LanguageSettingsManager manager = new LanguageSettingsManager(null);

		{
			// add something
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(new CIncludePathEntry("value1", 1));
			manager.setSettingEntries(RC_DESCRIPTOR, PROVIDER_0, entries);
			List<ICLanguageSettingEntry> retrieved = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_0);
			assertEquals(1, retrieved.size());
		}

		{
			// clear
			LanguageSettingsManager.clear();

			// Check instantiated earlier manager
			List<ICLanguageSettingEntry> retrieved = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_0);
			assertEquals(0, retrieved.size());

			// Check newly instantiated manager
			LanguageSettingsManager manager1 = new LanguageSettingsManager(null);
			retrieved = manager1.getSettingEntries(RC_DESCRIPTOR, PROVIDER_0);
			assertEquals(0, retrieved.size());
		}

		// TODO: clear project

	}

	/**
	 */
	public void testLanguageSettingsStore() throws Exception {
		LanguageSettingsStore store = new LanguageSettingsStore();

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("value1", 1));
		original.add(new CIncludePathEntry("value2", 2));

		// store and retrieve the entries
		store.setSettingEntries(RC_DESCRIPTOR, PROVIDER_0, original);
		List<ICLanguageSettingEntry> retrieved = store.getSettingEntries(RC_DESCRIPTOR, PROVIDER_0);

		assertNotSame(original, retrieved);
		assertEquals(original.size(), retrieved.size());
		ICLanguageSettingEntry[] originalArray = original.toArray(new ICLanguageSettingEntry[0]);
		ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
		for (int i=0;i<original.size();i++) {
			assertEquals(originalArray[i], retrievedArray[i]);
		}
	}

	/**
	 */
	public void testLanguageSettingsManagerBasic() throws Exception {
		LanguageSettingsManager manager = new LanguageSettingsManager(null);

		List<ICLanguageSettingEntry> original1 = new ArrayList<ICLanguageSettingEntry>();
		original1.add(new CIncludePathEntry("value1", 1));
		original1.add(new CIncludePathEntry("value2", 2));

		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("value1", 1));
		original2.add(new CIncludePathEntry("value2", 2));
		original2.add(new CIncludePathEntry("value3", 2));

		// store the entries
		manager.setSettingEntries(RC_DESCRIPTOR, PROVIDER_1, original1);
		manager.setSettingEntries(RC_DESCRIPTOR, PROVIDER_2, original2);

		// retrieve the entries for provider-1
		{
			List<ICLanguageSettingEntry> retrieved = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_1);

			assertNotSame(original1, retrieved);
			assertEquals(original1.size(), retrieved.size());
			ICLanguageSettingEntry[] originalArray = original1.toArray(new ICLanguageSettingEntry[0]);
			ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
			for (int i=0;i<original1.size();i++) {
				assertEquals(originalArray[i], retrievedArray[i]);
			}
		}

		// retrieve the entries for provider-2
		{
			List<ICLanguageSettingEntry> retrieved = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_2);

			assertNotSame(original2, retrieved);
			assertEquals(original2.size(), retrieved.size());
			ICLanguageSettingEntry[] originalArray = original2.toArray(new ICLanguageSettingEntry[0]);
			ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
			for (int i=0;i<original2.size();i++) {
				assertEquals(originalArray[i], retrievedArray[i]);
			}
		}

		// get list of providers
		{
			List<String> providers = manager.getProviders(RC_DESCRIPTOR);
			assertTrue(providers.contains(PROVIDER_1));
			assertTrue(providers.contains(PROVIDER_2));
			assertEquals(2, providers.size());
		}

		// reset the entries for provider-1
		{
			original1.clear();
			original1.add(new CIncludePathEntry("value10", 10));
			original1.add(new CIncludePathEntry("value20", 20));

			manager.setSettingEntries(RC_DESCRIPTOR, PROVIDER_1, original1);
			List<ICLanguageSettingEntry> retrieved = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_1);

			assertNotSame(original1, retrieved);
			assertEquals(original1.size(), retrieved.size());
			ICLanguageSettingEntry[] originalArray = original1.toArray(new ICLanguageSettingEntry[0]);
			ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
			for (int i=0;i<original1.size();i++) {
				assertEquals(originalArray[i], retrievedArray[i]);
			}
		}

		// clear settings for provider-1
		{
			manager.removeSettingEntries(RC_DESCRIPTOR, PROVIDER_1);
			List<ICLanguageSettingEntry> retrieved_1 = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_1);
			assertEquals(0, retrieved_1.size());

			List<ICLanguageSettingEntry> retrieved_2 = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_2);
			assertEquals(original2.size(), retrieved_2.size());
		}

		// add entries to the end
		{
			List<ICLanguageSettingEntry> original2a = new ArrayList<ICLanguageSettingEntry>();
			original2a.add(new CIncludePathEntry("value4a", 1));
			original2a.add(new CIncludePathEntry("value5a", 2));

			manager.addSettingEntries(RC_DESCRIPTOR, PROVIDER_2, original2a);
			List<ICLanguageSettingEntry> retrieved = manager.getSettingEntries(RC_DESCRIPTOR, PROVIDER_2);

			assertEquals(original2.size()+original2a.size(), retrieved.size());

			ICLanguageSettingEntry[] originalArray = original2.toArray(new ICLanguageSettingEntry[0]);
			ICLanguageSettingEntry[] originalAddedArray = original2a.toArray(new ICLanguageSettingEntry[0]);
			ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
			for (int i=0;i<original2.size();i++) {
				assertEquals(originalArray[i], retrievedArray[i]);
			}
			for (int i=0;i<original2a.size();i++) {
				assertEquals(originalAddedArray[i], retrievedArray[i+original2.size()]);
			}
		}
	}

	/**
	 */
	public void testLanguageSettingsManagerGlobal() throws Exception {
		// store the entries with manager1
		LanguageSettingsManager manager1 = new LanguageSettingsManager(null);
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		manager1.setSettingEntries(RC_DESCRIPTOR, PROVIDER_0, original);

		// retrieve the entries with manager2
		LanguageSettingsManager manager2 = new LanguageSettingsManager(null);
		List<ICLanguageSettingEntry> retrieved = manager2.getSettingEntries(RC_DESCRIPTOR, PROVIDER_0);

		assertEquals(original.get(0), retrieved.get(0));
	}

	/**
	 */
	public void testLanguageSettingsManagerFiltered() throws Exception {
		LanguageSettingsManager manager = new LanguageSettingsManager(null);

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		original.add(new CMacroEntry("MACRO0", "value0",0));
		original.add(new CIncludePathEntry("path1", 0));
		original.add(new CMacroEntry("MACRO1", "value1",0));
		original.add(new CIncludePathEntry("path2", 0));

		// store the entries
		manager.setSettingEntries(RC_DESCRIPTOR, PROVIDER_0, original);

		// retrieve entries by kind
		{
			List<ICLanguageSettingEntry> includes = manager
				.getSettingEntries(RC_DESCRIPTOR, PROVIDER_0, ICSettingEntry.INCLUDE_PATH);
			assertEquals(3, includes.size());
			assertEquals(new CIncludePathEntry("path0", 0),includes.get(0));
			assertEquals(new CIncludePathEntry("path1", 0),includes.get(1));
			assertEquals(new CIncludePathEntry("path2", 0),includes.get(2));

			List<ICLanguageSettingEntry> macros = manager
				.getSettingEntries(RC_DESCRIPTOR, PROVIDER_0, ICSettingEntry.MACRO);
			assertEquals(2, macros.size());
			assertEquals(new CMacroEntry("MACRO0", "value0",0), macros.get(0));
			assertEquals(new CMacroEntry("MACRO1", "value1",0), macros.get(1));
		}

	}

	/**
	 */
	public void testLanguageSettingsManagerFilteredConflicting() throws Exception {
		LanguageSettingsManager manager = new LanguageSettingsManager(null);

		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path", ICSettingEntry.BUILTIN));
		original.add(new CIncludePathEntry("path", ICSettingEntry.DISABLED));
		original.add(new CIncludePathEntry("path", 0));

		// store the entries
		manager.setSettingEntries(RC_DESCRIPTOR, PROVIDER_0, original);

		// retrieve entries by kind, only first entry is returned
		{
			List<ICLanguageSettingEntry> includes = manager
				.getSettingEntries(RC_DESCRIPTOR, PROVIDER_0, ICSettingEntry.INCLUDE_PATH);
			assertEquals(1, includes.size());
			assertEquals(original.get(0),includes.get(0));
		}

	}

	/**
	 * TODO: revisit the test after priorities are built in the new extension point
	 */
	public void testLanguageSettingsManagerReconciledProviders() throws Exception {
		LanguageSettingsManager manager = new LanguageSettingsManager(null);

		// store the entries
		List<ICLanguageSettingEntry> originalLow = new ArrayList<ICLanguageSettingEntry>();
		originalLow.add(new CIncludePathEntry("path0", ICSettingEntry.BUILTIN));
		originalLow.add(new CIncludePathEntry("path1", ICSettingEntry.DISABLED));
		originalLow.add(new CIncludePathEntry("path2", 0));
		originalLow.add(new CIncludePathEntry("path3", 0));
		manager.setSettingEntries(RC_DESCRIPTOR, LOW_PRIORITY_PROVIDER, originalLow);

		// store the entries
		List<ICLanguageSettingEntry> originalHigh = new ArrayList<ICLanguageSettingEntry>();
		originalHigh.add(new CIncludePathEntry("path0", ICSettingEntry.RESOLVED));
		originalHigh.add(new CIncludePathEntry("path1", 0));
		originalHigh.add(new CIncludePathEntry("path2", ICSettingEntry.DISABLED));
		manager.setSettingEntries(RC_DESCRIPTOR, HIGH_PRIORITY_PROVIDER, originalHigh);


		// retrieve entries by kind
		{
			List<ICLanguageSettingEntry> includes = manager
				.getSettingEntriesReconciled(RC_DESCRIPTOR, ICSettingEntry.INCLUDE_PATH);
			// path0 is taked from higher priority provider
			assertEquals(originalHigh.get(0),includes.get(0));
			// path1 disablement by lower priority provider is ignored
			assertEquals(originalHigh.get(1),includes.get(1));
			// path2 is removed because of DISABLED flag of high priority provider
			// path3 gets there from low priority provider
			assertEquals(originalLow.get(3),includes.get(2));
			assertEquals(3, includes.size());
		}

	}

}
