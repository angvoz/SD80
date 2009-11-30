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

package org.eclipse.cdt.make.core.scannerconfig;

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.LanguageSettingsPersistentProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public abstract class AbstractBuildCommandParser extends LanguageSettingsPersistentProvider implements
		ILanguageSettingsOutputScanner {

	private ICConfigurationDescription currentCfgDescription = null;
	private IProject currentProject;
	

	public void startup(ICConfigurationDescription cfgDescription) throws CoreException {
		currentCfgDescription = cfgDescription;
		currentProject = cfgDescription != null ? cfgDescription.getProjectDescription().getProject() : null;
	}

	public ICConfigurationDescription getConfigurationDescription() {
		return currentCfgDescription;
	}
	
	public IProject getProject() {
		return currentProject;
	}
	
	/**
	 * This method is expected to populate this.settingEntries with specific values
	 * parsed from supplied lines.
	 */
	public abstract boolean processLine(String line);
	
	public void shutdown() {
	}

	protected void setSettingEntries(List<ICLanguageSettingEntry> entries, String fileName) {
		IProject project = getProject();
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		IResource rc = project.findMember(fileName);
		if (rc!=null) {
			ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(rc.getProjectRelativePath(), true);
			String languageId = ls.getLanguageId();
			setSettingEntries(cfgDescription, rc, languageId, entries);
		}
	}


}
