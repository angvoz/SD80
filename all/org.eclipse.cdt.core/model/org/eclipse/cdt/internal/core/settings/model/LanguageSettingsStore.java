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

package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsResourceDescriptor;


/**
 * 
 */
public class LanguageSettingsStore {
	// store Map<Resource, Map<ProviderId, List<SettingEntry>>>
	private static Map<LanguageSettingsResourceDescriptor, Map<String, List<ICLanguageSettingEntry>>>
		fStorage = new HashMap<LanguageSettingsResourceDescriptor, Map<String, List<ICLanguageSettingEntry>>>();
	
	/**
	 * Get copy of the list
	 */
	public static List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId) {
		Map<String, List<ICLanguageSettingEntry>> map = fStorage.get(descriptor);
		if (map!=null) {
			List<ICLanguageSettingEntry> list = map.get(providerId);
			if (list!=null) {
				return new ArrayList<ICLanguageSettingEntry>(list);
			}
		}
		return new ArrayList<ICLanguageSettingEntry>();
	}
	
	/**
	 * 
	 */
	public static List<ICLanguageSettingEntry> getSettingEntriesFiltered(LanguageSettingsResourceDescriptor descriptor, String providerId, int kind) {
		ArrayList<ICLanguageSettingEntry> filtered = new ArrayList<ICLanguageSettingEntry>();
		Map<String, List<ICLanguageSettingEntry>> map = fStorage.get(descriptor);
		if (map!=null) {
			List<ICLanguageSettingEntry> list = map.get(providerId);
			if (list!=null) {
				for (ICLanguageSettingEntry entry : list) {
					if (entry.getKind()==kind) {
						filtered.add(entry);
					}
				}
			}
		}
		return filtered;
	}
	
	/**
	 * This will replace old settings if existed with the new ones.
	 */
	public static void setSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId, List<ICLanguageSettingEntry> settings) {
		Map<String, List<ICLanguageSettingEntry>> map = fStorage.get(descriptor);
		if (map==null) {
			map = new HashMap<String, List<ICLanguageSettingEntry>>();
			fStorage.put(descriptor, map);
		}
		map.put(providerId, new ArrayList<ICLanguageSettingEntry>(settings));
	}
	
	/**
	 * This will add new settings to the end.
	 */
	public static void addSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId, List<ICLanguageSettingEntry> settings) {
		Map<String, List<ICLanguageSettingEntry>> map = fStorage.get(descriptor);
		if (map==null) {
			map = new HashMap<String, List<ICLanguageSettingEntry>>();
			fStorage.put(descriptor, map);
		}
		List<ICLanguageSettingEntry> list = map.get(providerId);
		if (list==null) {
			list = new ArrayList<ICLanguageSettingEntry>(settings);
		} else {
			list.addAll(settings);
		}
		map.put(providerId, list);
	}
	
	public static void removeSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId) {
		Map<String, List<ICLanguageSettingEntry>> map = fStorage.get(descriptor);
		if (map!=null) {
			map.remove(providerId);
		}
		if (map==null || map.size()==0) {
			fStorage.remove(descriptor);
		}
	}
	
	public static void clear() {
		fStorage.clear();
	}
	
	
	
	
	
	
	/**
	 * TODO
	 * 
	 * @param projectName - project name. Use {@code null} to load workspace level settings.
	 */
	public static void load(String projectName) {
		
	}
	
	public static void serialize() {
		
	}

	
}
