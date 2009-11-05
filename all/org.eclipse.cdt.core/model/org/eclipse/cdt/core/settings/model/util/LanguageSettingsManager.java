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

package org.eclipse.cdt.core.settings.model.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingsContributor;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescription;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescriptionCache;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsExtensionManager;
import org.eclipse.core.runtime.Assert;
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
public class LanguageSettingsManager {
	public static final String CONTRIBUTOR_UNKNOWN = "org.eclipse.cdt.projectmodel.4.0.0";
	public static final String CONTRIBUTOR_UI_USER = "org.eclipse.cdt.ui.user";
	public static final char CONTRIBUTOR_DELIMITER = LanguageSettingsExtensionManager.CONTRIBUTOR_DELIMITER;
	
	public static List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, LanguageSettingsResourceDescriptor descriptor, String contributorId) {
		Assert.isNotNull(cfgDescription);

		ICLanguageSettingsContributor contributor = getContributor(cfgDescription, contributorId);
		if (contributor!=null) {
			List<ICLanguageSettingEntry> list = contributor.getSettingEntries(descriptor);
			if (list!=null) {
				return new ArrayList<ICLanguageSettingEntry>(list);
			}
		}

		IPath path = descriptor.getWorkspacePath();
		if (!path.isRoot() && !path.isEmpty()) {
			IPath parentPath = path.removeLastSegments(1);
			LanguageSettingsResourceDescriptor parentDescriptor = new LanguageSettingsResourceDescriptor(
					descriptor.getConfigurationId(), parentPath, descriptor.getLangId());

			return getSettingEntries(cfgDescription, parentDescriptor, contributorId);
		}
		return new ArrayList<ICLanguageSettingEntry>(0);
	}

	private static boolean containsEntry(List<ICLanguageSettingEntry> list, String name) {
		for (ICLanguageSettingEntry entry : list) {
			if (entry.getName().equals(name)) {
				return true;
			}
		}

		return false;
	}

	public static List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDecription,
			LanguageSettingsResourceDescriptor descriptor, String contributorId, int kind) {
		ICLanguageSettingsContributor contributor = getContributor(cfgDecription, contributorId);
		if (contributor==null) {
			return new ArrayList<ICLanguageSettingEntry>(0);
		}
		List<ICLanguageSettingEntry> list = contributor.getSettingEntries(descriptor);
		ArrayList<ICLanguageSettingEntry> newList = new ArrayList<ICLanguageSettingEntry>(list.size());
		for (ICLanguageSettingEntry entry : list) {
			if (entry.getKind()==kind && !containsEntry(newList, entry.getName())) {
				newList.add(entry);
			}
		}
		return newList;
	}

	public static List<ICLanguageSettingEntry> getSettingEntriesReconciled(ICConfigurationDescription cfgDescription, LanguageSettingsResourceDescriptor descriptor, int kind) {
		List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>();
		for (ICLanguageSettingsContributor contributor: getContributors(cfgDescription)) {
			for (ICLanguageSettingEntry entry : getSettingEntries(cfgDescription, descriptor, contributor.getId(), kind)) {
				if (!containsEntry(list, entry.getName())) {
					list.add(entry);
				}
			}
		}

		Iterator<ICLanguageSettingEntry> iter = list.iterator();
		while (iter.hasNext()) {
			ICLanguageSettingEntry entry = iter.next();
			if ((entry.getFlags() & ICSettingEntry.UNDEFINED)==ICSettingEntry.UNDEFINED) {
				iter.remove();
			}
		}
		return list;
	}

	public static void load() {
//		for (ICLanguageSettingsContributor contributor : contributors) {
//			if (contributor instanceof ACLanguageSettingsSerializableContributor) {
//				((ACLanguageSettingsSerializableContributor) contributor).fromXML();
//			}
//		}
	}

	public static void serialize() {
//		for (ICLanguageSettingsContributor contributor : contributors) {
//			if (contributor instanceof ACLanguageSettingsSerializableContributor) {
//				((ACLanguageSettingsSerializableContributor) contributor).toXML();
//			}
//		}
	}

	/**
	 * Set and store in workspace area user defined contributors.
	 *
	 * @param contributors - array of user defined contributors
	 * @throws CoreException in case of problems
	 */
	public static void setUserDefinedContributors(ICLanguageSettingsContributor[] contributors) throws CoreException {
		LanguageSettingsExtensionManager.setUserDefinedContributors(contributors);
	}

	/**
	 * @return available contributors IDs which include contributed through extension + user defined ones
	 * from workspace
	 */
	public static String[] getContributorAvailableIds() {
		return LanguageSettingsExtensionManager.getContributorAvailableIds();
	}

	/**
	 * @return IDs of language settings contributors of LanguageSettingContributor extension point.
	 */
	public static String[] getContributorExtensionIds() {
		return LanguageSettingsExtensionManager.getContributorExtensionIds();
	}

	/**
	 * TODO
	 */
	public static ICLanguageSettingsContributor getContributor(String id) {
		return LanguageSettingsExtensionManager.getContributor(id);
	}

	/**
	 * Set and store default contributors IDs to be used if contributor list is empty.
	 *
	 * @param ids - default contributors IDs
	 * @throws BackingStoreException in case of problem with storing
	 */
	public static void setDefaultContributorIds(String[] ids) throws BackingStoreException {
		LanguageSettingsExtensionManager.setDefaultContributorIds(ids);
	}

	/**
	 * @return default contributors IDs to be used if contributor list is empty.
	 */
	public static String[] getDefaultContributorIds() {
		return LanguageSettingsExtensionManager.getDefaultContributorIds();
	}

	/**
	 * This usage is discouraged TODO .
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void setContributors(ICConfigurationDescription cfgDescription,
			List<ICLanguageSettingsContributor> contributors) {
		if (cfgDescription instanceof CConfigurationDescription) {
			((CConfigurationDescription)cfgDescription).setLanguageSettingContributors(contributors);
		} else if (cfgDescription instanceof CConfigurationDescriptionCache) {
				((CConfigurationDescriptionCache)cfgDescription).setLanguageSettingContributors(contributors);
		} else if (cfgDescription!=null) {
			String className = cfgDescription.getClass().getName();
			CCorePlugin.log("Error setting ICLanguageSettingsContributor for unsupported configuration description type " + className); //$NON-NLS-1$
		}
	}

	/**
	 * This usage is discouraged TODO .
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static List<ICLanguageSettingsContributor> getContributors(ICConfigurationDescription cfgDescription) {
		if (cfgDescription instanceof CConfigurationDescription) {
			return ((CConfigurationDescription)cfgDescription).getLanguageSettingContributors();
		} else if (cfgDescription instanceof CConfigurationDescriptionCache) {
			return ((CConfigurationDescriptionCache)cfgDescription).getLanguageSettingContributors();
		} else if (cfgDescription!=null) {
			String className = cfgDescription.getClass().getName();
			CCorePlugin.log("Error getting ICLanguageSettingsContributor for unsupported configuration description type " + className); //$NON-NLS-1$
		}
		return new ArrayList<ICLanguageSettingsContributor>();
	}

	/**
	 */
	public static void setContributorIds(ICConfigurationDescription cfgDescription, List<String> ids) {
		if (cfgDescription instanceof CConfigurationDescription) {
			List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>(ids.size());
			for (String id : ids) {
				ICLanguageSettingsContributor contributor = getContributor(id);
				if (contributor!=null) {
					contributors.add(contributor);
				}
			}
			((CConfigurationDescription)cfgDescription).setLanguageSettingContributors(contributors);
		} else if (cfgDescription instanceof CConfigurationDescriptionCache) {
			List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>(ids.size());
			for (String id : ids) {
				ICLanguageSettingsContributor contributor = getContributor(id);
				if (contributor!=null) {
					contributors.add(contributor);
				}
			}
			((CConfigurationDescriptionCache)cfgDescription).setLanguageSettingContributors(contributors);
		} else if (cfgDescription!=null) {
			String className = cfgDescription.getClass().getName();
			CCorePlugin.log("Error setting ICLanguageSettingsContributor for unsupported configuration description type " + className); //$NON-NLS-1$
		}
	}
	
	/**
	 */
	public static List<String> getContributorIds(ICConfigurationDescription cfgDescription) {
		List<String> ids = new ArrayList<String>();
		if (cfgDescription instanceof CConfigurationDescription) {
			for (ICLanguageSettingsContributor contributor : ((CConfigurationDescription)cfgDescription).getLanguageSettingContributors()) {
				ids.add(contributor.getId());
			}
		} else if (cfgDescription instanceof CConfigurationDescriptionCache) {
			for (ICLanguageSettingsContributor contributor : ((CConfigurationDescriptionCache)cfgDescription).getLanguageSettingContributors()) {
				ids.add(contributor.getId());
			}
		} else if (cfgDescription!=null) {
			String className = cfgDescription.getClass().getName();
			CCorePlugin.log("Error getting ICLanguageSettingsContributor for unsupported configuration description type " + className); //$NON-NLS-1$
		}
		return ids;
	}
	
	/**
	 * TODO
	 */
	private static ICLanguageSettingsContributor getContributor(ICConfigurationDescription cfgDescription, String id) {
		for (ICLanguageSettingsContributor contributor : getContributors(cfgDescription)) {
			if (contributor.getId().equals(id)) {
				return contributor;
			}
		}
		return null;
	}


}
