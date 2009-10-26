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
import java.util.List;

import org.eclipse.cdt.core.settings.model.ACLanguageSettingsContributor;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsResourceDescriptor;

public class LanguageSettingsDefaultContributor extends ACLanguageSettingsContributor {
	private List<String> languages;
	private List<ICLanguageSettingEntry> entries;

	/**
	 * TODO
	 * languages can be null: in that case all languages qualify.
	 * entries cannot be null
	 */
	public LanguageSettingsDefaultContributor(String id, String name, int rank, List<String> languages, List<ICLanguageSettingEntry> entries) {
		super(id, name, rank);
		this.languages = languages!=null ? new ArrayList<String>(languages) : null;
		this.entries = new ArrayList<ICLanguageSettingEntry>(entries);
	}

	public List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor) {
		if (languages==null) {
			return new ArrayList<ICLanguageSettingEntry>(entries);
		}
		for (String lang : languages) {
			if (lang.equals(descriptor.getLangId())) {
				return new ArrayList<ICLanguageSettingEntry>(entries);
			}
		}
		return new ArrayList<ICLanguageSettingEntry>();
	}

	/**
	 * Used only for debugging purposes.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getId();
	}
}
