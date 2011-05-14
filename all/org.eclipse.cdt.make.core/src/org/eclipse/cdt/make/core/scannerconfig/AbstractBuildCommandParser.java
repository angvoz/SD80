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

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;

public abstract class AbstractBuildCommandParser extends LanguageSettingsSerializable implements
		ILanguageSettingsOutputScanner, IErrorParser {

	private static final String ATTR_EXPAND_RELATIVE_PATHS = "expand-relative-paths"; //$NON-NLS-1$
	
	private ICConfigurationDescription currentCfgDescription = null;
	private IProject currentProject;

	private boolean expandRelativePaths = true;

	/**
	 * @return the expandRelativePaths
	 */
	public boolean isExpandRelativePaths() {
		return expandRelativePaths;
	}

	/**
	 * @param expandRelativePaths the expandRelativePaths to set
	 */
	public void setExpandRelativePaths(boolean expandRelativePaths) {
		this.expandRelativePaths = expandRelativePaths;
	}


	public void startup(ICConfigurationDescription cfgDescription) throws CoreException {
		currentCfgDescription = cfgDescription;
		currentProject = cfgDescription != null ? cfgDescription.getProjectDescription().getProject() : null;
	}

	public ICConfigurationDescription getConfigurationDescription() {
		return currentCfgDescription;
	}

	public IProject getProject() {
		return currentProject;
	}

	public final boolean processLine(String line) {
		return processLine(line, null);
	}

	/**
	 * This method is expected to populate this.settingEntries with specific values
	 * parsed from supplied lines.
	 */
	public abstract boolean processLine(String line, ErrorParserManager epm);

	public void shutdown() {
	}

	protected void setSettingEntries(List<ICLanguageSettingEntry> entries, IResource rc) {
		IProject project = getProject();
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (rc!=null) {
			ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(rc.getProjectRelativePath(), true);
			String languageId = ls.getLanguageId();
			setSettingEntries(cfgDescription, rc, languageId, entries);
		}
	}

	@Override
	public Element serialize(Element parentElement) {
		Element elementProvider = super.serialize(parentElement);
		elementProvider.setAttribute(ATTR_EXPAND_RELATIVE_PATHS, Boolean.toString(expandRelativePaths));
		return elementProvider;
	}
	
	@Override
	public void load(Element providerNode) {
		super.load(providerNode);
		
		String expandRelativePathsValue = XmlUtil.determineAttributeValue(providerNode, ATTR_EXPAND_RELATIVE_PATHS);
		if (expandRelativePathsValue!=null)
			expandRelativePaths = Boolean.parseBoolean(expandRelativePathsValue);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (expandRelativePaths ? 1231 : 1237);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractBuildCommandParser other = (AbstractBuildCommandParser) obj;
		if (expandRelativePaths != other.expandRelativePaths)
			return false;
		return true;
	}
	

}
