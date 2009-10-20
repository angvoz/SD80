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

package org.eclipse.cdt.core.settings.model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsStore;
import org.eclipse.core.resources.IProject;

/**
 * TODO
 * This layer of language settings in TODO
 *
 * Duplicate entries are filtered where only first entry is preserved.
 *
 */
public class LanguageSettingsManager {
	public static final String PROVIDER_UNKNOWN = "org.eclipse.cdt.projectmodel.4.0.0";
	public static final String PROVIDER_UI_USER = "org.eclipse.cdt.ui.user";

	private final IProject fProject;
	private final LanguageSettingsStore fStore;
	// null project means settings apply to any project in the workspace
	// note that project settings can be added but not removed (only cleared)
	private static final Map<IProject, LanguageSettingsStore> globalStoreMap = new HashMap<IProject, LanguageSettingsStore>();

	public LanguageSettingsManager(IProject project) {
		this.fProject = project;
		LanguageSettingsStore store = globalStoreMap.get(project);
		if (store==null) {
			store = new LanguageSettingsStore(null);
			globalStoreMap.put(project, store);
		}
		this.fStore = store;
	}

	public List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId) {
		return fStore.getSettingEntries(descriptor, providerId);
	}

	private static boolean containsEntry(List<ICLanguageSettingEntry> list, String name) {
		for (ICLanguageSettingEntry entry : list) {
			if (entry.getName().equals(name)) {
				return true;
			}
		}

		return false;
	}

	public List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId, int kind) {
		List<ICLanguageSettingEntry> list = fStore.getSettingEntries(descriptor, providerId);
		ArrayList<ICLanguageSettingEntry> newList = new ArrayList<ICLanguageSettingEntry>(list.size());
		for (ICLanguageSettingEntry entry : list) {
			if (entry.getKind()==kind && !containsEntry(newList, entry.getName())) {
				newList.add(entry);
			}
		}
		return newList;
	}

	/**
	 * Note: old settings are discarded.
	 */
	public void setSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId, List<ICLanguageSettingEntry> entries) {
		fStore.setSettingEntries(descriptor, providerId, entries);
	}

	/**
	 * Settings added to the end.
	 */
	public void addSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId, List<ICLanguageSettingEntry> entries) {
		fStore.addSettingEntries(descriptor, providerId, entries);
	}

	public void removeSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId) {
		fStore.removeSettingEntries(descriptor, providerId);
	}

	public List<String> getProviders(LanguageSettingsResourceDescriptor descriptor) {
		Comparator<String> comparator = new Comparator<String>() {
			// TODO: priority will be taken from extension point
			private int getProviderPriority(String providerId) {
				if (PROVIDER_UI_USER.equals(providerId)) {
					return 1;
				}
				if (PROVIDER_UNKNOWN.equals(providerId)) {
					return 100;
				}
				return 666;
			}

			public int compare(String provider1, String provider2) {
				return getProviderPriority(provider1) - getProviderPriority(provider2);
			}
		};
		List<String> providers = fStore.getProviders();
		Collections.sort(providers, comparator);
		return providers;
	}

	public List<ICLanguageSettingEntry> getSettingEntriesReconciled(LanguageSettingsResourceDescriptor descriptor, int kind) {
		List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>();
		for (String provider: getProviders(descriptor)) {
			for (ICLanguageSettingEntry entry : getSettingEntries(descriptor, provider, kind)) {
				if (!containsEntry(list, entry.getName())) {
					list.add(entry);
				}
			}
		}

		Iterator<ICLanguageSettingEntry> iter = list.iterator();
		while (iter.hasNext()) {
			ICLanguageSettingEntry entry = iter.next();
			if ((entry.getFlags() & ICSettingEntry.DISABLED)==ICSettingEntry.DISABLED) {
				iter.remove();
			}
		}
		return list;
	}

	public static void clear(IProject project) {
		// TODO: test case
//		globalStoreMap.get(project).clear();
	}

	public static void clear() {
		// Can't remove any store as it could be cached somewhere
		for (LanguageSettingsStore store : globalStoreMap.values()) {
			store.clear();
		}
	}

}
