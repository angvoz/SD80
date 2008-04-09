/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettinsDeltaCalculator.ExtSettingMapKey;

public class CExternalSettingsHolder extends CExternalSettingsContainer {
	private Map fSettingsMap;
	static final String ELEMENT_EXT_SETTINGS_CONTAINER = "externalSettings"; //$NON-NLS-1$
	static final CExternalSetting[] EMPTY_EXT_SETTINGS_ARRAY = new CExternalSetting[0];
	
	private boolean fIsModified;

	CExternalSettingsHolder(){
		
	}

	CExternalSettingsHolder(ICStorageElement element){
		ICStorageElement children[] = element.getChildren();
		List externalSettingList = null;
		for(int i = 0; i < children.length; i++){
			ICStorageElement child = children[i];
			String name = child.getName();
			
			if(CExternalSettingSerializer.ELEMENT_SETTING_INFO.equals(name)){
				if(externalSettingList == null)
					externalSettingList = new ArrayList();
				
				CExternalSetting setting = CExternalSettingSerializer.load(child);
				externalSettingList.add(setting);
			}
		}
		
		if(externalSettingList != null && externalSettingList.size() != 0){
			for(int i = 0; i < externalSettingList.size(); i++){
				CExternalSetting setting = (CExternalSetting)externalSettingList.get(i);
				createExternalSetting(setting.getCompatibleLanguageIds(),
						setting.getCompatibleContentTypeIds(),
						setting.getCompatibleExtensions(), 
						setting.getEntries());
			}
		}
	}

	CExternalSettingsHolder(CExternalSettingsHolder base){
		if(base.fSettingsMap != null)
			fSettingsMap = new HashMap(base.fSettingsMap);
	}

	@Override
	public CExternalSetting[] getExternalSettings(){
		if(fSettingsMap != null)
			return (CExternalSetting[])fSettingsMap.values().toArray(new CExternalSetting[fSettingsMap.size()]);
		return EMPTY_EXT_SETTINGS_ARRAY;
	}

	void setExternallSettings(CExternalSetting[] settings){
		setExternalSettings(settings, false);
	}

	void setExternalSettings(CExternalSetting[] settings, boolean add){
		if(!add)
			removeExternalSettings();

		if(settings != null){
			for(int i = 0; i < settings.length; i++){
				CExternalSetting setting = settings[i];
				createExternalSetting(setting.getCompatibleLanguageIds(),
						setting.getCompatibleContentTypeIds(),
						setting.getCompatibleExtensions(),
						setting.getEntries());
			}
		}
		fIsModified = true;
	}
	
	void addExternalSettings(CExternalSetting[] settings){
		setExternalSettings(settings, true);
	}

	public CExternalSetting createExternalSetting(String[] languageIDs,
			String[] contentTypeIDs, String[] extensions,
			ICSettingEntry[] entries) {
		return createExternalSetting(new CExternalSetting(languageIDs, contentTypeIDs, extensions, entries));
	}

	private CExternalSetting createExternalSetting(CExternalSetting setting){
		ExtSettingMapKey key = new ExtSettingMapKey(setting);
		CExternalSetting newSetting;
		if(fSettingsMap != null){
			newSetting = (CExternalSetting)fSettingsMap.get(key);
			if(newSetting == null){
				newSetting = new CExternalSetting(setting);
			} else {
				newSetting = new CExternalSetting(newSetting, setting.getEntries());
			}
			
			fSettingsMap.put(key, newSetting);
		} else {
			newSetting = new CExternalSetting(setting);
			fSettingsMap = new HashMap();
			fSettingsMap.put(key, newSetting);
		}
		fIsModified = true;
		return newSetting;
	}

	public void removeExternalSetting(CExternalSetting setting) {
		if(fSettingsMap != null){
			
			ExtSettingMapKey key = new ExtSettingMapKey(setting);
			CExternalSetting settingToRemove = (CExternalSetting)fSettingsMap.get(key);
			if(setting.equals(settingToRemove)){
				fSettingsMap.remove(key);
				fIsModified = true;
			}
		}
	}

	public void removeExternalSettings() {
		if(fSettingsMap != null){
			fSettingsMap.clear();
			fSettingsMap = null;
			fIsModified = true;
		}
	}
	
	public void serialize(ICStorageElement el){
		if(fSettingsMap != null && fSettingsMap.size() != 0){
			for(Iterator iter = fSettingsMap.values().iterator(); iter.hasNext();){
				CExternalSetting setting = (CExternalSetting)iter.next();
				ICStorageElement child = el.createChild(CExternalSettingSerializer.ELEMENT_SETTING_INFO);
				CExternalSettingSerializer.store(setting, child);
			}
		}
	}
	
	public boolean isModified(){
		return fIsModified;
	}
}
