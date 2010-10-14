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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.settings.model.ACPathEntry;
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

		List<ICLanguageSettingEntry> includePathEntries = LanguageSettingsExtensionManager.getSettingEntriesByKind(cfgDescription,
				resource, languageId, ICSettingEntry.INCLUDE_PATH, false);
		String[] includePaths = convertToLocations(includePathEntries, cfgDescription);

		List<ICLanguageSettingEntry> includePathLocalEntries = LanguageSettingsExtensionManager.getSettingEntriesByKind(cfgDescription,
				resource, languageId, ICSettingEntry.INCLUDE_PATH, true);
		String[] includePathsLocal = convertToLocations(includePathLocalEntries, cfgDescription);
		
		List<ICLanguageSettingEntry> includeFileEntries = LanguageSettingsExtensionManager.getSettingEntriesByKind(cfgDescription,
				resource, languageId, ICSettingEntry.INCLUDE_FILE);
		String[] includeFiles = convertToLocations(includeFileEntries, cfgDescription);

		List<ICLanguageSettingEntry> macroFileEntries = LanguageSettingsExtensionManager.getSettingEntriesByKind(cfgDescription,
				resource, languageId, ICSettingEntry.MACRO_FILE);
		String[] macroFiles = convertToLocations(macroFileEntries, cfgDescription);

		List<ICLanguageSettingEntry> macroEntries = LanguageSettingsExtensionManager.getSettingEntriesByKind(cfgDescription,
				resource, languageId, ICSettingEntry.MACRO);
		Map<String, String> definedMacros = new HashMap<String, String>();
		for (ICLanguageSettingEntry entry : macroEntries) {
			ICMacroEntry macroEntry = (ICMacroEntry)entry;
			String name = macroEntry.getName();
			String value = macroEntry.getValue();
			definedMacros.put(name, value);
		}

		return new ExtendedScannerInfo(definedMacros, includePaths, macroFiles, includeFiles, includePathsLocal);
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

	private IPath expandVariables(IPath path, ICConfigurationDescription cfgDescription) {
		ICdtVariableManager varManager = CCorePlugin.getDefault().getCdtVariableManager();
		String pathStr = path.toString();
		try {
			pathStr = varManager.resolveValue(pathStr, "", null, cfgDescription); //$NON-NLS-1$
		} catch (CdtVariableException e) {
			// Swallow exceptions but also log them
			CCorePlugin.log(e);
		}
		IPath resolvedLoc = new Path(pathStr);
		return resolvedLoc;
	}

	/**
	 * Get build working directory for the provided configuration. Returns
	 * project location if none defined.
	 */
	private static IPath getBuildCWD(ICConfigurationDescription cfgDescription) {
		IPath buildCWD = cfgDescription.getBuildSetting().getBuilderCWD();
		if (buildCWD==null) {
			IProject project = cfgDescription.getProjectDescription().getProject();
			buildCWD = project.getLocation();
		}
		buildCWD = buildCWD.addTrailingSeparator();
		return buildCWD;
	}

	/**
	 * Resolve location to file system location in a configuration context.
	 * Resolving includes replacing build/environment variables with values, making relative path absolute etc.
	 * 
	 * @param location - location to resolve. If relative, it is taken to be rooted in build working directory.
	 * @param cfgDescription - the configuration context.
	 * @return resolved file system location.
	 */
	private static String resolveEntry(String location, ICConfigurationDescription cfgDescription) {
		// Substitute build/environment variables
		ICdtVariableManager varManager = CCorePlugin.getDefault().getCdtVariableManager();
		try {
			location = varManager.resolveValue(location, "", null, cfgDescription); //$NON-NLS-1$
		} catch (CdtVariableException e) {
			// Swallow exceptions but also log them
			CCorePlugin.log(e);
		}
		// use OS file separators (i.e. '\' on Windows)
		if (java.io.File.separatorChar != '/') {
			location = location.replace('/', java.io.File.separatorChar);
		}
	
		// note that we avoid using org.eclipse.core.runtime.Path for manipulations being careful
		// to preserve "../" segments and not let collapsing them which is not correct for symbolic links.
		Path locPath = new Path(location);
		if (locPath.isAbsolute() && locPath.getDevice() == null) {
			// prepend device (C:) for Windows
			IPath buildCWD = getBuildCWD(cfgDescription);
			location = buildCWD.getDevice() + location;
		}
		if (!locPath.isAbsolute()) {
			// consider relative path to be from build working directory
			IPath buildCWD = getBuildCWD(cfgDescription);
			location = buildCWD.toOSString() + locPath;
		}
		return location;
	}

	/**
	 * Convert the path entries to absolute file system locations represented as String array.
	 * Resolve the entries which are not resolved.
	 * 
	 * @param pathEntries - language settings path entries.
	 * @param cfgDescription - configuration description for resolving entries.
	 * @return array of the locations.
	 */
	private String[] convertToLocations(List<ICLanguageSettingEntry> pathEntries, ICConfigurationDescription cfgDescription){
		List<String> locations = new ArrayList<String>(pathEntries.size());
		for (ICLanguageSettingEntry entry : pathEntries) {
			ICLanguageSettingPathEntry entryPath = (ICLanguageSettingPathEntry)entry;
			if (((ACPathEntry)entryPath).isValueWorkspacePath()) {
				IPath loc = entryPath.getLocation();
				if (loc!=null) {
					locations.add(loc.toOSString());
				}
			} else {
				String locStr = entryPath.getName();
				if (entryPath.isResolved()) {
					locations.add(locStr);
				} else {
					locStr = resolveEntry(locStr, cfgDescription);
					if (locStr!=null) {
						locations.add(locStr);
						// add relative paths again for indexer to resolve from source file location
						IPath unresolvedPath = entryPath.getLocation();
						if (!unresolvedPath.isAbsolute()) {
							IPath expandedPath = expandVariables(unresolvedPath, cfgDescription);
							if (!expandedPath.isAbsolute()) {
								locations.add(expandedPath.toOSString());
							}
						}
					}
				}
			}
		}

		return locations.toArray(new String[locations.size()]);
	}

	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		// Handled by ScannerInfoProviderProxy for the moment
	}

	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		// Handled by ScannerInfoProviderProxy for the moment
	}

}
