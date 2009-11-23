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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.LanguageSettingsPersistentProvider;
import org.eclipse.core.resources.IProject;

public abstract class AbstractBuiltinSpecsDetector extends LanguageSettingsPersistentProvider implements
		ILanguageSettingsOutputScanner {

	private ICConfigurationDescription cfgDescription = null;
	private IProject project;
	private String languageId;
	private String command;

	protected List<ICLanguageSettingEntry> settingEntries = null;

	public void setCommand(String command) {
		this.command = command;
	}
	
	public String getCommand() {
		return command;
	}
	
	public void setLanguage(String id) {
		languageId = id;
	}

	public String getLanguage() {
		return languageId;
	}
	
	public void startup(ICConfigurationDescription cfgDescription) {
		this.cfgDescription = cfgDescription;
		this.project = cfgDescription != null ? cfgDescription.getProjectDescription().getProject() : null;
		this.settingEntries = new ArrayList<ICLanguageSettingEntry>();
		setSettingEntries(cfgDescription, project, languageId, settingEntries);
	}

	/**
	 * This method is expected to populate this.settingEntries with specific values
	 * parsed from supplied lines.
	 */
	public abstract boolean processLine(String line);
	
	public void shutdown() {
		setSettingEntries(cfgDescription, project, languageId, settingEntries);
	}

}
