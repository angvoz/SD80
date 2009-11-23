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

import org.eclipse.cdt.core.ICConsoleParser;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsProvider;
import org.eclipse.core.resources.IResource;


// TODO: YAGNI?
public interface ILanguageSettingsOutputScanner extends ILanguageSettingsProvider, ICConsoleParser {
	
	// Inherited from ICConsoleParser
	public void startup(ICConfigurationDescription cfgDescription);
	public boolean processLine(String line);
	public void shutdown();
	
	// Inherited from ICLanguageSettingsProvider
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId);
}
