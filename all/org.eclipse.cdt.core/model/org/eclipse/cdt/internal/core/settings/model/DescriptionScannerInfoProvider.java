/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingPathEntry;
import org.eclipse.cdt.core.settings.model.ICMacroEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class DescriptionScannerInfoProvider implements IScannerInfoProvider, ICProjectDescriptionListener {
	private IProject fProject;
	private ICConfigurationDescription fCfgDes;
	private static final ScannerInfo INEXISTENT_SCANNER_INFO = new ScannerInfo();
	private boolean fInited;

	DescriptionScannerInfoProvider(IProject project){
		fProject = project;

		CProjectDescriptionManager.getInstance().addCProjectDescriptionListener(this, CProjectDescriptionEvent.APPLIED | CProjectDescriptionEvent.LOADED);
	}

	private void updateProjCfgInfo(ICProjectDescription des){
		if(des != null){
			fCfgDes = des.getDefaultSettingConfiguration();
			fInited = true;
		} else {
			fCfgDes = null;
			fInited = false;
		}
	}

	public IProject getProject(){
		return fProject;
	}

	public IScannerInfo getScannerInformation(IResource resource) {

		if(!fInited)
			updateProjCfgInfo(CProjectDescriptionManager.getInstance().getProjectDescription(fProject, false));

		if (fCfgDes==null) {
			CCorePlugin.log("Error getting scanner info: fCfgDes==null");
			return INEXISTENT_SCANNER_INFO;
		}
		
		String[] languageIds = LanguageSettingsManager.getLanguageIds(fCfgDes, resource);
		if (languageIds==null || languageIds.length==0) {
			CCorePlugin.log("Error getting ScannerInfo: Not able to retrieve language id for resource " + resource);
			return INEXISTENT_SCANNER_INFO;
		}
		
		// TODO: is there really a use case for resource being not IFile?
		String languageId = languageIds[0]; // Legacy logic

		List<ICLanguageSettingEntry> includePathEntries = LanguageSettingsManager.getSettingEntriesReconciled(fCfgDes, resource, languageId, ICSettingEntry.INCLUDE_PATH);
		List<ICLanguageSettingEntry> includePathSystemEntries = new ArrayList<ICLanguageSettingEntry>(includePathEntries.size());
		List<ICLanguageSettingEntry> includePathLocalEntries = new ArrayList<ICLanguageSettingEntry>(includePathEntries.size());
		for (ICLanguageSettingEntry entry : includePathEntries) {
			if ((entry.getFlags()&ICSettingEntry.LOCAL) == ICSettingEntry.LOCAL) {
				includePathLocalEntries.add(entry);
			} else {
				includePathSystemEntries.add(entry);
			}
		}
		
		String[] includePathsSystem = getValues(includePathSystemEntries);

		String[] includePathsLocal = new String[includePathLocalEntries.size()];
		for (int i=0; i<includePathLocalEntries.size();i++) {
			includePathsLocal[i] = includePathLocalEntries.get(i).getName();
		}
		
		List<ICLanguageSettingEntry> includeFileEntries = LanguageSettingsManager.getSettingEntriesReconciled(fCfgDes, resource, languageId, ICSettingEntry.INCLUDE_FILE);
		String[] includeFiles = getValues(includeFileEntries);
		
		List<ICLanguageSettingEntry> macroFileEntries = LanguageSettingsManager.getSettingEntriesReconciled(fCfgDes, resource, languageId, ICSettingEntry.MACRO_FILE);
		String[] macroFiles = getValues(macroFileEntries);
		
		List<ICLanguageSettingEntry> macroEntries = LanguageSettingsManager.getSettingEntriesReconciled(fCfgDes, resource, languageId, ICSettingEntry.MACRO);
		Map<String, String> definedSymbols = getMacroValues(macroEntries);
		
		return new ExtendedScannerInfo(definedSymbols, includePathsSystem, macroFiles, includeFiles, includePathsLocal);
	}

	private Map<String, String> getMacroValues(List<ICLanguageSettingEntry> macroEntries){
		Map<String, String> macrosMap = new HashMap<String, String>(macroEntries.size());
		String name;
		String value;

		for (ICLanguageSettingEntry entry : macroEntries) {
			ICMacroEntry macroEntry = (ICMacroEntry)entry;
			name = macroEntry.getName();
			value = macroEntry.getValue();
			macrosMap.put(name, value);
		}
		return macrosMap;
	}

	private String[] getValues(List<ICLanguageSettingEntry> pathEntries){
		String values[] = new String[pathEntries.size()];
		IPath path;
		int num = 0;
		for (ICLanguageSettingEntry entry : pathEntries) {
			ICLanguageSettingPathEntry pathEntry = (ICLanguageSettingPathEntry)entry;
			String p = pathEntry.getValue();
			if(p == null)
				continue;
			//TODO: obtain location from pathEntries when entries are resolved
			path = new Path(p);//p.getLocation();
			if(pathEntry.isValueWorkspacePath()){
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IResource rc = root.findMember(path);
				if(rc != null){
					path = rc.getLocation();
				}
			} else if (!path.isAbsolute()) {
				IPath projLocation = fProject != null ? fProject.getLocation() : null;
				if(projLocation != null)
					path = projLocation.append(path);
			}
			if(path != null)
				values[num++] = path.toOSString();
		}

		if(num < pathEntries.size()){
			String tmp[] = new String[num];
			System.arraycopy(values, 0, tmp, 0, num);
			values = tmp;
		}

		return values;
	}

	public void subscribe(IResource resource,
			IScannerInfoChangeListener listener) {
		// TODO Auto-generated method stub

	}

	public void unsubscribe(IResource resource,
			IScannerInfoChangeListener listener) {
		// TODO Auto-generated method stub

	}

	public void close(){
		CProjectDescriptionManager.getInstance().removeCProjectDescriptionListener(this);
	}

	public void handleEvent(CProjectDescriptionEvent event) {
		if(!event.getProject().equals(fProject))
			return;
		
		//TODO: check delta and notify listeners

		updateProjCfgInfo(event.getNewCProjectDescription());
	}

}
