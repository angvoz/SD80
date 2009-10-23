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

/**
 * Abstract class to support most basic contributors.
 *
 */
public abstract class ACLanguageSettingsContributor implements ICLanguageSettingsContributor {
	private String id;
	private int rank;

	public ACLanguageSettingsContributor(String id, int rank) {
		this.id = id;
		this.rank = rank;
	}

	public String getId() {
		return id;
	}

	public int getRank() {
		return rank;
	}

}
