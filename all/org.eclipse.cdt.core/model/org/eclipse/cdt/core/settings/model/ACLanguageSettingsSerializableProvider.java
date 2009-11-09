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

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;


public abstract class ACLanguageSettingsSerializableProvider extends AbstractExecutableExtensionBase implements ICLanguageSettingsProvider {

	public ACLanguageSettingsSerializableProvider(String id, String name) {
		super(id, name);
	}

	public void toXML() {
	}

	public void fromXML() {
	}

}
