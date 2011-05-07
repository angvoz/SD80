/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.core.resources.IResource;

public class LanguageSettingsWorkspaceProvider implements ILanguageSettingsProvider {
	private String providerId;

	public LanguageSettingsWorkspaceProvider(String id) {
		providerId = id;
	}
	
	public String getId() {
		return providerId;
	}

	public String getName() {
		return getRawProvider().getName();
	}

	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		List<ICLanguageSettingEntry> entries = getRawProvider().getSettingEntries(cfgDescription, rc, languageId);
		return entries;
	}

	/**
	 * Do not cache the "raw" provider as workspace provider can be changed at any time. 
	 */
	private ILanguageSettingsProvider getRawProvider() {
		return LanguageSettingsProvidersSerializer.getRawWorkspaceProvider(providerId);
	}
	
	@Override
	public String toString() {
		return LanguageSettingsProvidersSerializer.getRawWorkspaceProvider(providerId).toString();
	}
}
