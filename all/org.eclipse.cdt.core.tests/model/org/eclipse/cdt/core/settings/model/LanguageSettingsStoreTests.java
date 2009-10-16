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
	private static final String TEST_PROVIDER_0 = "test.provider.0.id";
	private static final String TEST_PROVIDER_1 = "test.provider.1.id";
	private static final String TEST_PROVIDER_2 = "test.provider.2.id";
	
	private static final LanguageSettingsResourceDescriptor TEST_RC_DESCRIPTOR = new LanguageSettingsResourceDescriptor(CONFIGURATION_ID, WORKSPACE_PATH, LANG_ID);

	public static TestSuite suite() {
		return suite(LanguageSettingsStoreTests.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		LanguageSettingsStore.clear();
	}

	/**
	 * @throws Exception
	 */
	public void testClear() throws Exception {
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("value1", 1));
		original.add(new CIncludePathEntry("value2", 2));

		// store and then clear the entries
		LanguageSettingsStore.setSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_0, original);
		List<ICLanguageSettingEntry> retrieved = LanguageSettingsStore.getSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_0);
		assertEquals(original.size(), retrieved.size());

		LanguageSettingsStore.clear();
		retrieved = LanguageSettingsStore.getSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_0);
		assertEquals(0, retrieved.size());
	}

	/**
	 * @throws Exception
	 */
	public void testLanguageSettingsStore() throws Exception {
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("value1", 1));
		original.add(new CIncludePathEntry("value2", 2));
		
		// store and retrieve the entries
		LanguageSettingsStore.setSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_1, original);
		List<ICLanguageSettingEntry> retrieved = LanguageSettingsStore.getSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_1);
		
		assertNotSame(original, retrieved);
		assertEquals(original.size(), retrieved.size());
		ICLanguageSettingEntry[] originalArray = original.toArray(new ICLanguageSettingEntry[0]);
		ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
		for (int i=0;i<original.size();i++) {
			assertEquals(originalArray[i], retrievedArray[i]);
		}
	}
	
	/**
	 * @throws Exception
	 */
	public void testLanguageSettingsManagerBasic() throws Exception {
		List<ICLanguageSettingEntry> original1 = new ArrayList<ICLanguageSettingEntry>();
		original1.add(new CIncludePathEntry("value1", 1));
		original1.add(new CIncludePathEntry("value2", 2));
		
		List<ICLanguageSettingEntry> original2 = new ArrayList<ICLanguageSettingEntry>();
		original2.add(new CIncludePathEntry("value1", 1));
		original2.add(new CIncludePathEntry("value2", 2));
		original2.add(new CIncludePathEntry("value3", 2));
		
		// store the entries
		LanguageSettingsManager.setSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_1, original1);
		LanguageSettingsManager.setSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_2, original2);

		// retrieve the entries for provider-1
		{
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_1);
			
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
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_2);
			
			assertNotSame(original2, retrieved);
			assertEquals(original2.size(), retrieved.size());
			ICLanguageSettingEntry[] originalArray = original2.toArray(new ICLanguageSettingEntry[0]);
			ICLanguageSettingEntry[] retrievedArray = retrieved.toArray(new ICLanguageSettingEntry[0]);
			for (int i=0;i<original2.size();i++) {
				assertEquals(originalArray[i], retrievedArray[i]);
			}
		}
		
		// reset the entries for provider-1
		{
			original1.clear();
			original1.add(new CIncludePathEntry("value10", 10));
			original1.add(new CIncludePathEntry("value20", 20));

			LanguageSettingsManager.setSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_1, original1);
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_1);
			
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
			LanguageSettingsManager.removeSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_1);
			List<ICLanguageSettingEntry> retrieved_1 = LanguageSettingsManager.getSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_1);
			assertEquals(0, retrieved_1.size());

			List<ICLanguageSettingEntry> retrieved_2 = LanguageSettingsManager.getSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_2);
			assertEquals(original2.size(), retrieved_2.size());
		}

		// add entries to the end
		{
			List<ICLanguageSettingEntry> original2a = new ArrayList<ICLanguageSettingEntry>();
			original2a.add(new CIncludePathEntry("value4a", 1));
			original2a.add(new CIncludePathEntry("value5a", 2));

			LanguageSettingsManager.addSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_2, original2a);
			List<ICLanguageSettingEntry> retrieved = LanguageSettingsManager.getSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_2);
			
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
	 * @throws Exception
	 */
	public void testLanguageSettingsManagerFiltered() throws Exception {
		List<ICLanguageSettingEntry> original = new ArrayList<ICLanguageSettingEntry>();
		original.add(new CIncludePathEntry("path0", 0));
		original.add(new CMacroEntry("MACRO0", "value0",0));
		original.add(new CIncludePathEntry("path1", 0));
		original.add(new CMacroEntry("MACRO1", "value1",0));
		original.add(new CIncludePathEntry("path2", 0));
		
		// store the entries
		LanguageSettingsManager.setSettingEntries(TEST_RC_DESCRIPTOR, TEST_PROVIDER_1, original);
		
		// retrieve entries by kind
		{
			List<ICLanguageSettingEntry> includes = LanguageSettingsManager.getSettingEntriesFiltered(
					TEST_RC_DESCRIPTOR, TEST_PROVIDER_1, ICSettingEntry.INCLUDE_PATH);
			assertEquals(3, includes.size());
			assertEquals(new CIncludePathEntry("path0", 0),includes.get(0));
			assertEquals(new CIncludePathEntry("path1", 0),includes.get(1));
			assertEquals(new CIncludePathEntry("path2", 0),includes.get(2));
			
			List<ICLanguageSettingEntry> macros = LanguageSettingsManager.getSettingEntriesFiltered(
					TEST_RC_DESCRIPTOR, TEST_PROVIDER_1, ICSettingEntry.MACRO);
			assertEquals(2, macros.size());
			assertEquals(new CMacroEntry("MACRO0", "value0",0), macros.get(0));
			assertEquals(new CMacroEntry("MACRO1", "value1",0), macros.get(1));
		}
		
	}
	
}
