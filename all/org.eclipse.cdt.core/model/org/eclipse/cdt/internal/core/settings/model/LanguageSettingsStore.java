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
 * LanguageSettingsStore serves for storing lists of language settings ICLanguageSettingEntry
 * (org.eclipse.cdt.core analog for compiler options). It is represented internally as TODO .
 *
 * Each provider can keep heterogeneous ordered list of settings where there is no distinction
 * between different kinds such as include paths or macros made. Entries with duplicate names
 * are also allowed.
 *
 */
public class LanguageSettingsStore {
	// store Map<ProviderId, Map<Resource, List<SettingEntry>>>
	private Map<String, Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>>>
		fStorage = new HashMap<String, Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>>>();

	public LanguageSettingsStore() {
		// TODO
	}

	/**
	 * Get copy of the list
	 */
	public List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId) {
		Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>> map = fStorage.get(providerId);
		if (map!=null) {
			List<ICLanguageSettingEntry> list = map.get(descriptor);
			if (list!=null) {
				return new ArrayList<ICLanguageSettingEntry>(list);
			}
		}
		return new ArrayList<ICLanguageSettingEntry>();
	}


	/**
	 * This will replace old settings if existed with the new ones.
	 */
	public void setSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId, List<ICLanguageSettingEntry> settings) {
		Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>> map = fStorage.get(providerId);
		if (map==null) {
			map = new HashMap<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>>();
			fStorage.put(providerId, map);
		}
		map.put(descriptor, new ArrayList<ICLanguageSettingEntry>(settings));
	}

	/**
	 * This will add new settings to the end.
	 */
	public void addSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId, List<ICLanguageSettingEntry> settings) {
		Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>> map = fStorage.get(providerId);
		if (map==null) {
			map = new HashMap<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>>();
			fStorage.put(providerId, map);
		}
		List<ICLanguageSettingEntry> list = map.get(descriptor);
		if (list==null) {
			list = new ArrayList<ICLanguageSettingEntry>(settings);
		} else {
			list.addAll(settings);
		}
		map.put(descriptor, list);
	}

	public void removeSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId) {
		Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>> map = fStorage.get(providerId);
		if (map!=null) {
			map.remove(descriptor);
		}
		if (map==null || map.size()==0) {
			fStorage.remove(providerId);
		}
	}

	public void clear() {
		fStorage.clear();
	}

	public List<String> getProviders(LanguageSettingsResourceDescriptor descriptor) {
		return new ArrayList<String>(fStorage.keySet());
	}






	/**
	 * TODO
	 *
	 * @param projectName - project name. Use {@code null} to load workspace level settings.
	 */
	public void load(String projectName) {

	}

	public void serialize() {

	}


}
