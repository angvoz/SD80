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

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsStore;

public class LanguageSettingsManager {
	public static final String UNKNOWN_PROVIDER = "org.eclipse.cdt.projectmodel.4.0.0";
	
	public static List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId) {
		return LanguageSettingsStore.getSettingEntries(descriptor, providerId);
	}

	public static List<ICLanguageSettingEntry> getSettingEntriesFiltered(LanguageSettingsResourceDescriptor descriptor, String providerId, int kind) {
		return LanguageSettingsStore.getSettingEntriesFiltered(descriptor, providerId, kind);
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
	
}
