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
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsStore;

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

	public static List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId) {
		return LanguageSettingsStore.getSettingEntries(descriptor, providerId);
	}

	private static boolean containsEntry(List<ICLanguageSettingEntry> list, String name) {
		for (ICLanguageSettingEntry entry : list) {
			if (entry.getName().equals(name)) {
				return true;
			}
		}

		return false;
	}

	public static List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId, int kind) {
		List<ICLanguageSettingEntry> list = LanguageSettingsStore.getSettingEntries(descriptor, providerId);
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
	public static void setSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId, List<ICLanguageSettingEntry> entries) {
		LanguageSettingsStore.setSettingEntries(descriptor, providerId, entries);
	}

	/**
	 * Settings added to the end.
	 */
	public static void addSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId, List<ICLanguageSettingEntry> entries) {
		LanguageSettingsStore.addSettingEntries(descriptor, providerId, entries);
	}

	public static void removeSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId) {
		LanguageSettingsStore.removeSettingEntries(descriptor, providerId);
	}


	// TODO: priority will be taken from extension point
	private static int getProviderPriority(String providerId) {
		if (PROVIDER_UI_USER.equals(providerId)) {
			return 1;
		}
		if (PROVIDER_UNKNOWN.equals(providerId)) {
			return 100;
		}
		return 666;
	}

	public static List<String> getProviders(LanguageSettingsResourceDescriptor descriptor) {
		Comparator<String> comparator = new Comparator<String>() {
			public int compare(String provider1, String provider2) {
				return getProviderPriority(provider1) - getProviderPriority(provider2);
			}
		};
		List<String> providers = LanguageSettingsStore.getProviders(descriptor);
		Collections.sort(providers, comparator);
		return providers;
	}

	public static List<ICLanguageSettingEntry> getSettingEntriesReconciled(LanguageSettingsResourceDescriptor descriptor, int kind) {
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


}
