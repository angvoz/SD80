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

import org.eclipse.core.resources.IResource;

public class LanguageSettingsResourceDescriptor {
	private IResource rc;
	private String langId;

	public LanguageSettingsResourceDescriptor(IResource rc, String langId) {
		this.rc = rc;
		this.langId = langId;
	}

	public IResource getResource() {
		return rc;
	}
	
	public String getLangId() {
		return langId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((langId == null) ? 0 : langId.hashCode());
		result = prime * result + ((rc == null) ? 0 : rc.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LanguageSettingsResourceDescriptor)) return false;
		LanguageSettingsResourceDescriptor that = (LanguageSettingsResourceDescriptor)o;
		return this.rc.equals(that.rc)
			&& this.langId.equals(that.langId)
		;
	}

}
