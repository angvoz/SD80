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
import org.eclipse.core.runtime.CoreException;

public abstract class AbstractBuiltinSpecsDetector extends LanguageSettingsPersistentProvider implements
		ILanguageSettingsOutputScanner {

	private ICConfigurationDescription cfgDescription = null;
	private IProject project;
	private String currentLanguageId;
	private String command;

	protected List<ICLanguageSettingEntry> detectedSettingEntries = null;

	public void setCommand(String command) {
		this.command = command;
	}
	
	public String getCommand() {
		return command;
	}
	
	public void setCurrentLanguage(String id) {
		currentLanguageId = id;
	}

	public String getCurrentLanguage() {
		return currentLanguageId;
	}
	
	public void startup(ICConfigurationDescription cfgDescription) throws CoreException {
		this.cfgDescription = cfgDescription;
		this.project = cfgDescription != null ? cfgDescription.getProjectDescription().getProject() : null;
		this.detectedSettingEntries = new ArrayList<ICLanguageSettingEntry>();
		setSettingEntries(cfgDescription, project, currentLanguageId, detectedSettingEntries);
	}

	/**
	 * This method is expected to populate this.settingEntries with specific values
	 * parsed from supplied lines.
	 */
	public abstract boolean processLine(String line);
	
	public void shutdown() {
		setSettingEntries(cfgDescription, project, currentLanguageId, detectedSettingEntries);
	}

}
