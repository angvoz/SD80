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

import java.util.List;

import org.eclipse.cdt.core.settings.model.util.LanguageSettingsResourceDescriptor;

public interface ICLanguageSettingsContributor {
	public String getId();
	public int getRank();
	public List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor);
}
