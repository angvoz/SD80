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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingPathEntry;
import org.eclipse.cdt.core.settings.model.ICMacroEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
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
	private ICProjectDescription fProjDes;
	private ICConfigurationDescription fCfgDes;
	private Map<String, IScannerInfo> fIdToLanguageSettingsMap = Collections.synchronizedMap(new HashMap<String, IScannerInfo>());
	private String fCurrentFileDescriptionId;
	private IScannerInfo fCurrentFileScannerInfo;
	private static final ScannerInfo INEXISTENT_SCANNER_INFO = new ScannerInfo();
	private boolean fInited;

	DescriptionScannerInfoProvider(IProject project){
		fProject = project;

		CProjectDescriptionManager.getInstance().addCProjectDescriptionListener(this, CProjectDescriptionEvent.APPLIED | CProjectDescriptionEvent.LOADED);
	}

	private void updateProjCfgInfo(ICProjectDescription des){
		fInited = true;
		fProjDes = des;
		if(fProjDes != null){
			fCfgDes = des.getDefaultSettingConfiguration();
		}

		fIdToLanguageSettingsMap.clear();
		fCurrentFileDescriptionId = null;
		fCurrentFileScannerInfo = null;
	}

	public IProject getProject(){
		return fProject;
	}

	public IScannerInfo getScannerInformation(IResource resource) {
		if(!fInited)
			updateProjCfgInfo(CProjectDescriptionManager.getInstance().getProjectDescription(fProject, false));

		if(fCfgDes == null)
			return INEXISTENT_SCANNER_INFO;

		ICLanguageSetting setting = null;
		ICResourceDescription rcDes = null;
		if(resource.getType() != IResource.PROJECT){
			IPath rcPath = resource.getProjectRelativePath();
			rcDes = fCfgDes.getResourceDescription(rcPath, false);

			if(rcDes.getType() == ICSettingBase.SETTING_FILE){
				setting = ((ICFileDescription)rcDes).getLanguageSetting();
			} else {
				if(resource.getType() == IResource.FILE)
					setting = ((ICFolderDescription)rcDes).getLanguageSettingForFile(rcPath.lastSegment());
				else {
					ICLanguageSetting settings[] = ((ICFolderDescription)rcDes).getLanguageSettings();
					if(settings.length > 0){
						setting = settings[0];
					}
				}
			}
		}
		return getScannerInfo(resource, rcDes, setting);
	}

	private IScannerInfo getScannerInfo(IResource rc, ICResourceDescription rcDes, ICLanguageSetting ls){
		String mapKey = ls != null ? ls.getId() : null;
//		if(ls == null)
//			return INEXISTENT_SCANNER_INFO;
		boolean useMap = rcDes == null || rcDes.getType() == ICSettingBase.SETTING_FOLDER;

		IScannerInfo info;
		if(useMap)
			info = fIdToLanguageSettingsMap.get(mapKey);
		else {
			if(fCurrentFileScannerInfo != null && rcDes != null){
				if(rcDes.getId().equals(fCurrentFileDescriptionId))
					info = fCurrentFileScannerInfo;
				else {
					info = null;
					fCurrentFileScannerInfo = null;
					fCurrentFileDescriptionId = null;
				}
			} else {
				info = null;
			}
		}
		if(info == null){
			info = createScannerInfo(rc, ls);
			if(useMap)
				fIdToLanguageSettingsMap.put(mapKey, info);
			else if (rcDes != null){
				fCurrentFileScannerInfo = info;
				fCurrentFileDescriptionId = rcDes.getId();
			}
		}
		return info;
	}

	private IScannerInfo createScannerInfo(IResource rc, ICLanguageSetting ls){
		List<ICLanguageSettingEntry> incsList = new ArrayList<ICLanguageSettingEntry>();
		List<ICLanguageSettingEntry> incFilesList = new ArrayList<ICLanguageSettingEntry>();
		List<ICLanguageSettingEntry> macroFilesList = new ArrayList<ICLanguageSettingEntry>();
		List<ICLanguageSettingEntry> macroList = new ArrayList<ICLanguageSettingEntry>();
		
		ICLanguageSetting[] languageSettings = null;
		if(ls == null) {
			ICFolderDescription foDes = fCfgDes.getRootFolderDescription();
			languageSettings = foDes.getLanguageSettings();
		} else {
			languageSettings = new ICLanguageSetting[] {ls};
		}

		for (ICLanguageSetting languageSetting : languageSettings) {
			String languageId = languageSetting.getLanguageId();
			ICConfigurationDescription cfgDescription = languageSetting.getConfiguration();
			incsList.addAll(LanguageSettingsManager.getSettingEntriesReconciled(cfgDescription, rc, languageId, ICSettingEntry.INCLUDE_PATH));
			incFilesList.addAll(LanguageSettingsManager.getSettingEntriesReconciled(cfgDescription, rc, languageId, ICSettingEntry.INCLUDE_FILE));
			macroFilesList.addAll(LanguageSettingsManager.getSettingEntriesReconciled(cfgDescription, rc, languageId, ICSettingEntry.MACRO_FILE));
			macroList.addAll(LanguageSettingsManager.getSettingEntriesReconciled(cfgDescription, rc, languageId, ICSettingEntry.MACRO));
		}
		
		String incs[] = getValues(incsList);
		String incFiles[] = getValues(incFilesList);
		String macroFiles[] = getValues(macroFilesList);
		Map<String, String> macrosMap = getMacroValues(macroList);
		
		return new ExtendedScannerInfo(macrosMap, incs, macroFiles, incFiles);
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
