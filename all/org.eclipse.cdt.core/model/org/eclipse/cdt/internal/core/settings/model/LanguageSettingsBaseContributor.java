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

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingsContributor;
import org.eclipse.core.resources.IResource;

public class LanguageSettingsBaseContributor extends AbstractExecutableExtensionBase implements ICLanguageSettingsContributor {
	private List<String> languages;
	private List<ICLanguageSettingEntry> entries;

	/**
	 * TODO
	 * languages can be null: in that case all languages qualify.
	 * entries cannot be null
	 */
	public LanguageSettingsBaseContributor(String id, String name, List<String> languages, List<ICLanguageSettingEntry> entries) {
		super(id, name);
		this.languages = languages!=null ? new ArrayList<String>(languages) : null;
		this.entries = new ArrayList<ICLanguageSettingEntry>(entries);
	}

	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		if (languages==null) {
			return new ArrayList<ICLanguageSettingEntry>(entries);
		}
		for (String lang : languages) {
			if (lang.equals(languageId)) {
				return new ArrayList<ICLanguageSettingEntry>(entries);
			}
		}
		return new ArrayList<ICLanguageSettingEntry>();
	}

}
