/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.scannerconfig;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class MBSLanguageSettingsProvider implements ILanguageSettingsProvider {
	private static final String fId = "org.eclipse.cdt.managedbuilder.core.LanguageSettingsProvider"; //$NON-NLS-1$
	private static final String fName = "CDT MBS Setting Entries"; //$NON-NLS-1$

	public MBSLanguageSettingsProvider() {
	}

	/**
	 * @return id of extension
	 */
	public String getId() {
		return fId;
	}

	/**
	 * @return name of extension
	 */
	public String getName() {
		return fName;
	}

	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		IPath projectPath = rc.getProjectRelativePath();
		ICResourceDescription rcDescription = cfgDescription.getResourceDescription(projectPath, false);
		
		List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>();
		for (ICLanguageSetting languageSetting : getLanguageSettings(rcDescription)) {
			if (languageSetting!=null) {
				String id = languageSetting.getLanguageId();
				if (id!=null && id.equals(languageId)) {
					int kindsBits = languageSetting.getSupportedEntryKinds();
					for (int kind=1;kind<=kindsBits;kind<<=1) {
						if ((kindsBits & kind) != 0) {
							list.addAll(languageSetting.getSettingEntriesList(kind));
						}
					}
				}
			}
		}
		return list;
	}
	
	private ICLanguageSetting[] getLanguageSettings(ICResourceDescription rcDescription) {
		ICLanguageSetting[] array = null;
		switch (rcDescription.getType()) {
		case ICSettingBase.SETTING_PROJECT:
		case ICSettingBase.SETTING_CONFIGURATION:
		case ICSettingBase.SETTING_FOLDER:
			ICFolderDescription foDes = (ICFolderDescription)rcDescription;
			array = foDes.getLanguageSettings();
			break;
		case ICSettingBase.SETTING_FILE:
			ICFileDescription fiDes = (ICFileDescription)rcDescription;
			ICLanguageSetting ls = fiDes.getLanguageSetting();
			if (ls!=null) {
				array = new ICLanguageSetting[] { ls };
			}
		}
		if (array==null) {
			array = new ICLanguageSetting[0];
		}
		return array;
	}

}
