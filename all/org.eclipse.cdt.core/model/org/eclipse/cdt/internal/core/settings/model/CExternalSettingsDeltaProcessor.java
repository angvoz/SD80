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
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.EntryContentsKey;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettinsDeltaCalculator.ExtSettingsDelta;
import org.eclipse.core.runtime.CoreException;

public class CExternalSettingsDeltaProcessor {
	static boolean applyDelta(ICConfigurationDescription des, ExtSettingsDelta deltas[]){
		return applyDelta(des, deltas, KindBasedStore.ORED_ALL_ENTRY_KINDS);
	}

	static boolean applyDelta(ICConfigurationDescription des, ExtSettingsDelta deltas[], int kindMask){
		ICResourceDescription rcDess[] = des.getResourceDescriptions();
		boolean changed = false;
		for(int i = 0; i < rcDess.length; i++){
			ICResourceDescription rcDes = rcDess[i];
			if(applyDelta(rcDes, deltas, kindMask))
				changed = true;
		}

		if((kindMask & ICSettingEntry.SOURCE_PATH) != 0){
			if(applySourceEntriesChange(des, deltas))
				changed = true;
		}
		if((kindMask & ICSettingEntry.OUTPUT_PATH) != 0){
			if(applyOutputEntriesChange(des, deltas))
				changed = true;
		}

		return changed;
	}
	
	static boolean applySourceEntriesChange(ICConfigurationDescription cfgDes, ExtSettingsDelta[] deltas){
		ICSettingEntry[][] diff = CExternalSettinsDeltaCalculator.getAllEntries(deltas, ICSettingEntry.SOURCE_PATH);
		if(diff == null)
			return false;

		ICSourceEntry[] current = cfgDes.getSourceEntries();
		if(current.length == 1){
			ICSourceEntry cur = current[0];
			if(cur.getFullPath().segmentCount() == 1 && cur.getExclusionPatterns().length == 0){
				current = new ICSourceEntry[0];
			}
		}
		List newEntries = calculateUpdatedEntries(current, diff[0], diff[1]);
		if(newEntries != null){
			try {
				cfgDes.setSourceEntries((ICSourceEntry[])newEntries.toArray(new ICSourceEntry[newEntries.size()]));
			} catch (WriteAccessException e) {
				CCorePlugin.log(e);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
			return true;
		}
		return false;
	}

	static boolean applyOutputEntriesChange(ICConfigurationDescription cfgDes, ExtSettingsDelta[] deltas){
		ICSettingEntry[][] diff = CExternalSettinsDeltaCalculator.getAllEntries(deltas, ICSettingEntry.OUTPUT_PATH);
		if(diff == null)
			return false;

		ICBuildSetting bs = cfgDes.getBuildSetting();
		if(bs == null)
			return false;
		
		ICOutputEntry[] current = bs.getOutputDirectories(); 
		if(current.length == 1){
			ICOutputEntry cur = current[0];
			if(cur.getFullPath().segmentCount() == 1 && cur.getExclusionPatterns().length == 0){
				current = new ICOutputEntry[0];
			}
		}

		List newEntries = calculateUpdatedEntries(current, diff[0], diff[1]);
		if(newEntries != null){
			try {
				bs.setOutputDirectories((ICOutputEntry[])newEntries.toArray(new ICOutputEntry[newEntries.size()]));
			} catch (WriteAccessException e) {
				CCorePlugin.log(e);
			}
			return true;
		}
		return false;
	}

	static boolean applyDelta(ICResourceDescription rcDes, ExtSettingsDelta deltas[], int kindMask){
		if(rcDes.getType() == ICSettingBase.SETTING_FOLDER){
			return applyDelta((ICFolderDescription)rcDes, deltas, kindMask);
		} 
		return applyDelta((ICFileDescription)rcDes, deltas, kindMask);
	}
	
	static boolean applyDelta(ICFileDescription des, ExtSettingsDelta deltas[], int kindMask){
		ICLanguageSetting setting = des.getLanguageSetting();
		if(setting == null)
			return false;
		
		boolean changed = false;
		for(int i = 0; i < deltas.length; i++){
			if(isSettingCompatible(setting, deltas[i].fSetting)){
				if(applyDelta(setting, deltas[i], kindMask))
					changed = true;
			}
		}
		return changed;
	}

	static boolean applyDelta(ICFolderDescription des, ExtSettingsDelta deltas[], int kindMask){
		ICLanguageSetting settings[] = des.getLanguageSettings();
		if(settings == null || settings.length == 0)
			return false;
		
		ICLanguageSetting setting;
		boolean changed = false;
		for(int k = 0; k < settings.length; k++){
			setting = settings[k];
			if(applyDelta(setting, deltas, kindMask))
				changed = true;
		}
		return changed;
	}
	
	static boolean applyDelta(ICLanguageSetting setting, ExtSettingsDelta[] deltas, int kindMask){
		boolean changed = false;
		for(int i = 0; i < deltas.length; i++){
			if(isSettingCompatible(setting, deltas[i].fSetting)){
				if(applyDelta(setting, deltas[i], kindMask))
					changed = true;
			}
		}
		return changed;
	}

	static boolean applyDelta(ICLanguageSetting setting, ExtSettingsDelta delta, int kindMask){
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		int kind;
		ICLanguageSettingEntry entries[];
		ICSettingEntry diff[][];
		boolean changed = false;
		for(int i = 0; i < kinds.length; i++){
			kind = kinds[i];
			if((kind & kindMask) == 0)
				continue;
			
			diff = delta.getEntriesDelta(kind);
			if(diff == null)
				continue;
			
			entries = setting.getSettingEntries(kind);
			List list = calculateUpdatedEntries(entries, diff[0], diff[1]);
			
			if(list != null){
				setting.setSettingEntries(kind, list);
				changed = true;
			}
		}
		return changed;
	}
	
	private static List calculateUpdatedEntries(ICSettingEntry current[], ICSettingEntry added[], ICSettingEntry removed[]){
		LinkedHashMap map = new LinkedHashMap();
		boolean changed = false;
		if(added != null){
			CDataUtil.fillEntriesMapByContentsKey(map, added);
		}
		if(current != null){
			CDataUtil.fillEntriesMapByContentsKey(map, current);
			if(current.length != map.size()){
				changed = true;
			}
		} else {
			if(map.size() != 0){
				changed = true;
			}
		}
		if(removed != null){
			for(int i = 0; i < removed.length; i++){
				ICSettingEntry entry = removed[i];
				EntryContentsKey cKey = new EntryContentsKey(entry);
				ICSettingEntry cur = (ICSettingEntry)map.get(cKey);
				if(cur != null && !cur.isBuiltIn()){
					map.remove(cKey);
					changed = true;
				}
			}
		}
		return changed ? new ArrayList(map.values()) : null;
	}
	
	private static boolean isSettingCompatible(ICLanguageSetting setting, CExternalSetting provider){
		String ids[] = provider.getCompatibleLanguageIds();
		String id;
		if(ids != null && ids.length > 0){
			id = setting.getLanguageId();
			if(id != null){
				if(contains(ids, id))
					return true;
				return false;
			}
			return false;
		}
		
		ids = provider.getCompatibleContentTypeIds();
		if(ids != null && ids.length > 0){
			String[] cTypeIds = setting.getSourceContentTypeIds();
			if(cTypeIds.length != 0){
				for(int i = 0; i < cTypeIds.length; i++){
					id = cTypeIds[i];
					if(contains(ids, id))
						return true;
				}
				return false;
			}
			return false;
		}
		
		ids = provider.getCompatibleExtensions();
		if(ids != null && ids.length > 0){
			String [] srcIds = setting.getSourceExtensions();
			if(srcIds.length != 0){
				for(int i = 0; i < srcIds.length; i++){
					id = srcIds[i];
					if(contains(ids, id))
						return true;
				}
				return false;
			}
			return false;
		}
		return true;
	}
	
	private static boolean contains(Object array[], Object value){
		for(int i = 0; i < array.length; i++){
			if(array[i].equals(value))
				return true;
		}
		return false;
	}
}
