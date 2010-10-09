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

package org.eclipse.cdt.core.language.settings.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsExtensionManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * TODO
 * This layer of language settings in TODO
 *
 * Duplicate entries are filtered where only first entry is preserved.
 *
 */
public class LanguageSettingsManager_TBD {
	public static final String PROVIDER_UNKNOWN = "org.eclipse.cdt.projectmodel.4.0.0";
	public static final String PROVIDER_UI_USER = "org.eclipse.cdt.ui.user.LanguageSettingsProvider";
	public static final char PROVIDER_DELIMITER = LanguageSettingsExtensionManager.PROVIDER_DELIMITER;

	private static ICLanguageSetting[] getLanguageIds(ICResourceDescription rcDescription) {
		if (rcDescription instanceof ICFileDescription) {
			ICFileDescription fileDescription = (ICFileDescription)rcDescription;
			return new ICLanguageSetting[] {fileDescription.getLanguageSetting()};
		} else if (rcDescription instanceof ICFolderDescription) {
			ICFolderDescription folderDescription = (ICFolderDescription)rcDescription;
			return folderDescription.getLanguageSettings();
		}

		return null;
	}

	public static boolean isCustomizedResource(ICConfigurationDescription cfgDescription, IResource rc) {
		for (ILanguageSettingsProvider provider: cfgDescription.getLanguageSettingProviders()) {
			// FIXME
//			if (!LanguageSettingsManager.isWorkspaceProvider(provider)) {
			if (provider instanceof ILanguageSettingsEditableProvider || provider instanceof LanguageSettingsSerializable) {
				ICResourceDescription rcDescription = cfgDescription.getResourceDescription(rc.getProjectRelativePath(), false);
				for (ICLanguageSetting languageSetting : getLanguageIds(rcDescription)) {
					String languageId = languageSetting.getLanguageId();
					if (languageId!=null) {
						List<ICLanguageSettingEntry> list = provider.getSettingEntries(cfgDescription, rc, languageId);
						if (list!=null) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public static void serializeWorkspaceProviders() throws CoreException {
		LanguageSettingsExtensionManager.serializeLanguageSettingsWorkspace();
	}

	/**
	 * Set and store in workspace area user defined providers.
	 *
	 * @param providers - array of user defined providers
	 * @throws CoreException in case of problems
	 */
	public static void setUserDefinedProviders(ILanguageSettingsProvider[] providers) throws CoreException {
		LanguageSettingsExtensionManager.setUserDefinedProviders(providers);
	}

	/**
	 * TODO .
	 */
	@Deprecated
	public static void setProviderIds(ICConfigurationDescription cfgDescription, List<String> ids) {
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(ids.size());
		for (String id : ids) {
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(id);
			if (provider!=null) {
				providers.add(provider);
			}
		}
		cfgDescription.setLanguageSettingProviders(providers);
	}

	/**
	 * TODO: migrate test cases
	 */
	@Deprecated
	public static List<String> getProviderIds(ICConfigurationDescription cfgDescription) {
		List<String> ids = new ArrayList<String>();
		for (ILanguageSettingsProvider provider : cfgDescription.getLanguageSettingProviders()) {
			ids.add(provider.getId());
		}
		return ids;
	}

	// FIXME: get rid of using that in DescriptionScannerInfoProvider
	@Deprecated
	public static String[] getLanguageIds(ICConfigurationDescription cfgDescription, IResource resource) {
		ICResourceDescription rcDes = null;
		IPath rcPath = resource.getProjectRelativePath();
		if(resource.getType() == IResource.PROJECT){
			rcDes = cfgDescription.getRootFolderDescription();
		} else {
			rcDes = cfgDescription.getResourceDescription(rcPath, false);
		}

		if(rcDes.getType() == ICSettingBase.SETTING_FILE){
			ICLanguageSetting setting = ((ICFileDescription)rcDes).getLanguageSetting();
			return new String[] {setting.getLanguageId()};
		} else {
			if(resource.getType() == IResource.FILE) {
				ICLanguageSetting setting = ((ICFolderDescription)rcDes).getLanguageSettingForFile(rcPath.lastSegment());
				if (setting!=null) {
					// FIXME: there is a bug in in AbstractIndexerTask.parseFilesUpFront(). It should not parse C++ files for pure C project
					return new String[] {setting.getLanguageId()};
				}
			} else {
				ICLanguageSetting settings[] = ((ICFolderDescription)rcDes).getLanguageSettings();
				if(settings==null || settings.length==0){
					ICFolderDescription foDes = cfgDescription.getRootFolderDescription();
					settings = foDes.getLanguageSettings();
				}
				if(settings!=null){
					String[] ids = new String[settings.length];
					for (int i=0;i<settings.length;i++) {
						ids[i] = settings[i].getLanguageId();
					}
					return ids;
				}
			}
		}

		return null;
	}

}
