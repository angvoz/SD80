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

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.core.resources.IResource;

public class LanguageSettingsBaseProvider extends AbstractExecutableExtensionBase implements ILanguageSettingsProvider {
	private List<String> languages = null;
	private List<ICLanguageSettingEntry> entries = null;

	public LanguageSettingsBaseProvider() {
	}

	/**
	 * TODO
	 */
	public LanguageSettingsBaseProvider(String id, String name) {
		super(id, name);
	}

	protected static List<ICLanguageSettingEntry> cloneList(List<ICLanguageSettingEntry> entries) {
		return entries!=null ? new ArrayList<ICLanguageSettingEntry>(entries) : null;
	}
	/**
	 * TODO
	 * languages can be null: in that case all languages qualify.
	 */
	public LanguageSettingsBaseProvider(String id, String name, List<String> languages, List<ICLanguageSettingEntry> entries) {
		super(id, name);
		this.languages = languages!=null ? new ArrayList<String>(languages) : null;
		this.entries = cloneList(entries);
	}
	
	public List<String> getLanguageIds() {
		return languages!=null ? new ArrayList<String>(languages) : null;
	}
	
	public void configureProvider(String id, String name, List<String> languages, List<ICLanguageSettingEntry> entries) {
		if (this.entries!=null)
			throw new UnsupportedOperationException("LanguageSettingsBaseProvider can be configured only once");
		
		setId(id);
		setName(name);
		this.languages = languages!=null ? new ArrayList<String>(languages) : null;
		this.entries = cloneList(entries);
	}
	
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		if (languages==null) {
			return cloneList(entries);
		}
		for (String lang : languages) {
			if (lang.equals(languageId)) {
				return cloneList(entries);
			}
		}
		return null;
	}

}
