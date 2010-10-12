/*******************************************************************************
 * Copyright (c) 2010, 2010 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.language.settings.providers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingPathEntry;
import org.eclipse.cdt.core.settings.model.ICMacroEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.internal.core.settings.model.SettingsModelMessages;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * Implementation of {@link IScannerInfoProvider} backed by the list of
 * language settings providers of "default settings configuration"
 * (see {@link ICProjectDescription#getDefaultSettingConfiguration()}).
 *
 */
public class LanguageSettingsScannerInfoProvider implements IScannerInfoProvider {
	private static final ExtendedScannerInfo DUMMY_SCANNER_INFO = new ExtendedScannerInfo();

	public ExtendedScannerInfo getScannerInformation(IResource resource) {
		IProject project = resource.getProject();
		if (project==null)
			return DUMMY_SCANNER_INFO;

		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		if (prjDescription==null)
			return DUMMY_SCANNER_INFO;

		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		if (cfgDescription==null)
			return DUMMY_SCANNER_INFO;

		String languageId = getLanguageId(cfgDescription, resource);
		if (languageId==null) {
			return DUMMY_SCANNER_INFO;
		}

		List<ICLanguageSettingEntry> includePathEntries = LanguageSettingsManager.getSettingEntriesByKind(cfgDescription, resource, languageId, ICSettingEntry.INCLUDE_PATH);
		String[] includePaths = convertToLocations(includePathEntries, project);

		List<ICLanguageSettingEntry> includeFileEntries = LanguageSettingsManager.getSettingEntriesByKind(cfgDescription, resource, languageId, ICSettingEntry.INCLUDE_FILE);
		String[] includeFiles = convertToLocations(includeFileEntries, project);

		List<ICLanguageSettingEntry> macroFileEntries = LanguageSettingsManager.getSettingEntriesByKind(cfgDescription, resource, languageId, ICSettingEntry.MACRO_FILE);
		String[] macroFiles = convertToLocations(macroFileEntries, project);

		List<ICLanguageSettingEntry> macroEntries = LanguageSettingsManager.getSettingEntriesByKind(cfgDescription, resource, languageId, ICSettingEntry.MACRO);
		Map<String, String> definedMacros = new HashMap<String, String>();
		for (ICLanguageSettingEntry entry : macroEntries) {
			ICMacroEntry macroEntry = (ICMacroEntry)entry;
			String name = macroEntry.getName();
			String value = macroEntry.getValue();
			definedMacros.put(name, value);
		}

		return new ExtendedScannerInfo(definedMacros, includePaths, macroFiles, includeFiles);
	}

	private String getLanguageId(ICConfigurationDescription cfgDescription, IResource resource) {
		String languageId = null;
		if (resource instanceof IFile) {
			languageId = getLanguageIdForFile(cfgDescription, resource);
		} else if (resource instanceof IContainer) { // IResource can be either IFile or IContainer
			languageId = getLanguageIdForFolder(cfgDescription, (IContainer) resource);
		}
		if (languageId==null) {
			String msg = NLS.bind(SettingsModelMessages.getString("LanguageSettingsScannerInfoProvider.UnableToDetermineLanguage"), resource.toString()); //$NON-NLS-1$
			CCorePlugin.log(new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, msg)));
		}
		return languageId;
	}

	private String getLanguageIdForFile(ICConfigurationDescription cfgDescription, IResource resource) {
		try {
			ILanguage language = LanguageManager.getInstance().getLanguageForFile((IFile) resource, cfgDescription);
			if (language!=null) {
				return language.getId();
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	private String getLanguageIdForFolder(ICConfigurationDescription cfgDescription, IContainer resource) {
		// TODO: That is wacky but that's how legacy stuff works
		String languageId = null;

		ICFolderDescription rcDes = null;
		if(resource.getType() == IResource.PROJECT){
			rcDes = cfgDescription.getRootFolderDescription();
		} else {
			IPath rcPath = resource.getProjectRelativePath();
			rcDes = (ICFolderDescription) cfgDescription.getResourceDescription(rcPath, false);
		}

		ICLanguageSetting settings[] = rcDes.getLanguageSettings();
		if(settings==null || settings.length==0){
			ICFolderDescription foDes = cfgDescription.getRootFolderDescription();
			settings = foDes.getLanguageSettings();
		}
		if(settings!=null && settings.length>0) {
			languageId = settings[0].getLanguageId();
		}

		return languageId;
	}

	// FIXME
	private String[] convertToLocations(List<ICLanguageSettingEntry> pathEntries, IProject project){
		String locations[] = new String[pathEntries.size()];
		int num = 0;
		for (ICLanguageSettingEntry entry : pathEntries) {
			ICLanguageSettingPathEntry pathEntry = (ICLanguageSettingPathEntry)entry;
			String pathStr = pathEntry.getValue();
			if(pathStr == null)
				continue;
			//TODO: obtain location from pathEntries when entries are resolved
			IPath path = new Path(pathStr);//pathEntry.getLocation();
			if(pathEntry.isValueWorkspacePath()){
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IResource rc = root.findMember(path);
				if(rc != null){
					path = rc.getLocation();
				}
			} else if (!path.isAbsolute()) {
				IPath projLocation = project != null ? project.getLocation() : null;
				if(projLocation != null)
					path = projLocation.append(path);
			}
			if(path != null)
				locations[num++] = path.toOSString();
		}

		if(num < pathEntries.size()){
			String tmp[] = new String[num];
			System.arraycopy(locations, 0, tmp, 0, num);
			locations = tmp;
		}

		return locations;
	}

	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		// Handled by ScannerInfoProviderProxy for the moment
	}

	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		// Handled by ScannerInfoProviderProxy for the moment
	}

}
