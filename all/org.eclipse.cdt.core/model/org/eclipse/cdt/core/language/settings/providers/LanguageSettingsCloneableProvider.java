/*******************************************************************************
 * Copyright (c) 2010, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.core.settings.model.SettingsModelMessages;
import org.eclipse.core.resources.IResource;

public abstract class LanguageSettingsCloneableProvider extends LanguageSettingsBaseProvider implements Cloneable {

	private boolean isReadOnly = false;

	public LanguageSettingsCloneableProvider() {
		super();
	}

	public LanguageSettingsCloneableProvider(String id, String name) {
		super(id, name);
	}

	/**
	 * @return {@code true} if the provider is read-only or {@code false} otherwise.
	 */
	public boolean isReadOnly() {
		return isReadOnly;
	}
	
	/**
	 * TODO: not intended to be API
	 */
	public void makeReadOnly() {
		isReadOnly = true;
	}

	@Override
	public void setId(String id) {
		if (isReadOnly)
			// FIXME rename "LanguageSettingsSerializable_..."
			throw new UnsupportedOperationException(SettingsModelMessages.getString("LanguageSettingsSerializable_ReadOnlyAccessError")); //$NON-NLS-1$

		super.setId(id);
	}

	@Override
	public void setName(String name) {
		if (isReadOnly)
			throw new UnsupportedOperationException(SettingsModelMessages.getString("LanguageSettingsSerializable_ReadOnlyAccessError")); //$NON-NLS-1$

		super.setName(name);
	}
	
	@Override
	public void configureProvider(String id, String name, List<String> languages, List<ICLanguageSettingEntry> entries, String customParameter) {
		if (isReadOnly)
			throw new UnsupportedOperationException(SettingsModelMessages.getString("LanguageSettingsSerializable_ReadOnlyAccessError")); //$NON-NLS-1$

		super.configureProvider(id, name, languages, entries, customParameter);
	}

	public void setLanguageIds(List <String> languages) {
		if (isReadOnly)
			throw new UnsupportedOperationException(SettingsModelMessages.getString("LanguageSettingsSerializable_ReadOnlyAccessError")); //$NON-NLS-1$

		this.languageScope = new ArrayList<String>(languages);
	}

	public void setCustomParameter(String customParameter) {
		if (isReadOnly)
			throw new UnsupportedOperationException(SettingsModelMessages.getString("LanguageSettingsSerializable_ReadOnlyAccessError")); //$NON-NLS-1$

		this.customParameter = customParameter;
	}

	public abstract void setSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId, List<ICLanguageSettingEntry> entries);

	public abstract boolean isEmpty();
	public abstract void clear();

	public final LanguageSettingsCloneableProvider getReadable() {
		// immutable instance
		if (isReadOnly)
			return this;
		
		LanguageSettingsCloneableProvider clone;
		try {
			clone = (LanguageSettingsCloneableProvider) clone();
		} catch (CloneNotSupportedException e) {
			CCorePlugin.log("Error cloning language settings provider " + getId() + ", class " + getClass().getName(), e);
			return null;
		}
		clone.isReadOnly = true;
		return clone;
	}
	
	public final LanguageSettingsCloneableProvider getWritable() {
		LanguageSettingsCloneableProvider clone;
		try {
			clone = (LanguageSettingsCloneableProvider) clone();
		} catch (CloneNotSupportedException e) {
			CCorePlugin.log("Error cloning language settings provider " + getId() + ", class " + getClass().getName(), e);
			return null;
		}
		clone.isReadOnly = false;
		return clone;
	}

	// TODO: hashcode() and equals() ?
}
