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
import org.eclipse.cdt.core.settings.model.LanguageSettingsSerializable;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsExtensionManager_TBD;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.osgi.service.prefs.BackingStoreException;

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
	public static final char PROVIDER_DELIMITER = LanguageSettingsExtensionManager_TBD.PROVIDER_DELIMITER;

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

	/**
	 * @return available providers IDs which include contributed through extension + user defined ones
	 * from workspace
	 */
	public static String[] getProviderAvailableIds() {
		return LanguageSettingsExtensionManager_TBD.getProviderAvailableIds();
	}

	/**
	 * @return IDs of language settings providers of LanguageSettingProvider extension point.
	 */
	public static String[] getProviderExtensionIds() {
		return LanguageSettingsExtensionManager_TBD.getProviderExtensionIds();
	}

	/**
	 * Get Language Settings Provider defined in the workspace. That includes user-defined
	 * providers and after that providers defined as extensions via
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} extension point.
	 * That returns actual object, any modifications will affect any configuration
	 * referring to the provider.
	 *
	 * @param id - ID of provider to find.
	 * @return the provider or {@code null} if provider is not defined.
	 */
	public static ILanguageSettingsProvider getWorkspaceProvider(String id) {
		return LanguageSettingsExtensionManager_TBD.getWorkspaceProvider(id);
	}

	/**
	 * Checks if the provider is defined on the workspace level. See {@link #getWorkspaceProvider(String)}.
	 *
	 * @param provider - provider to check.
	 * @return {@code true} if the given provider is workspace provider, {@code false} otherwise.
	 */
	public static boolean isWorkspaceProvider(ILanguageSettingsProvider provider) {
		return LanguageSettingsExtensionManager_TBD.isWorkspaceProvider(provider);
	}

	public static void serializeWorkspaceProviders() throws CoreException {
		LanguageSettingsExtensionManager_TBD.serializeLanguageSettingsWorkspace();
	}

	/**
	 * Set and store in workspace area user defined providers.
	 *
	 * @param providers - array of user defined providers
	 * @throws CoreException in case of problems
	 */
	public static void setUserDefinedProviders(ILanguageSettingsProvider[] providers) throws CoreException {
		LanguageSettingsExtensionManager_TBD.setUserDefinedProviders(providers);
	}

	/**
	 * Set and store default providers IDs to be used if provider list is empty.
	 *
	 * @param ids - default providers IDs
	 * @throws BackingStoreException in case of problem with storing
	 */
	@Deprecated
	public static void setDefaultProviderIds(String[] ids) throws BackingStoreException {
		LanguageSettingsExtensionManager_TBD.setDefaultProviderIds(ids);
	}

	/**
	 * @return default providers IDs to be used if provider list is empty.
	 */
	@Deprecated
	public static String[] getDefaultProviderIds() {
		return LanguageSettingsExtensionManager_TBD.getDefaultProviderIds();
	}

	/**
	 * TODO .
	 */
	@Deprecated
	public static void setProviderIds(ICConfigurationDescription cfgDescription, List<String> ids) {
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(ids.size());
		for (String id : ids) {
			ILanguageSettingsProvider provider = getWorkspaceProvider(id);
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
