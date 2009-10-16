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

package org.eclipse.cdt.core.settings.model.util;

import org.eclipse.core.runtime.IPath;

public class LanguageSettingsResourceDescriptor {
	private String configurationId;
	private IPath workspacePath;
	private String langId;
	
	public LanguageSettingsResourceDescriptor(String configurationId, IPath workspacePath, String langId) {
		this.configurationId = configurationId;
		this.workspacePath = workspacePath;
		this.langId = langId;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configurationId == null) ? 0 : configurationId.hashCode());
		result = prime * result + ((langId == null) ? 0 : langId.hashCode());
		result = prime * result + ((workspacePath == null) ? 0 : workspacePath.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LanguageSettingsResourceDescriptor)) return false;
		LanguageSettingsResourceDescriptor that = (LanguageSettingsResourceDescriptor)o;
		return this.configurationId.equals(that.configurationId)
			&& this.workspacePath.equals(that.workspacePath)
			&& this.langId.equals(that.langId)
		;
	}
	
}
