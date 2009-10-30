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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ACLanguageSettingsSerializableContributor;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingsContributor;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsExtensionManager;
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

//	private final LanguageSettingsStore fStore;
	// null project means settings apply to all projects in the workspace
	// note that project settings can be added but not removed (only cleared)
//	private static final Map<IProject, LanguageSettingsStore> globalStoreMap = new HashMap<IProject, LanguageSettingsStore>();

	private static final List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();

	public static List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor, String contributorId) {
		ICLanguageSettingsContributor contributor = getContributor(contributorId);
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

			return getSettingEntries(parentDescriptor, contributorId);
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

	public static List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor,
			String contributorId, int kind) {
		ICLanguageSettingsContributor contributor = getContributor(contributorId);
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

//	/**
//	 * Note: old settings are discarded.
//	 */
//	public void setSettingEntries(LanguageSettingsResourceDescriptor descriptor, String contributorId, List<ICLanguageSettingEntry> entries) {
//		fStore.setSettingEntries(descriptor, contributorId, entries);
//	}
//
//	/**
//	 * Settings added to the end.
//	 */
//	public void addSettingEntries(LanguageSettingsResourceDescriptor descriptor, String contributorId, List<ICLanguageSettingEntry> entries) {
//		fStore.addSettingEntries(descriptor, contributorId, entries);
//	}
//
//	public void removeSettingEntries(LanguageSettingsResourceDescriptor descriptor, String contributorId) {
//		fStore.removeSettingEntries(descriptor, contributorId);
//	}

//	public List<String> getProviders(LanguageSettingsResourceDescriptor descriptor) {
//		Comparator<String> comparator = new Comparator<String>() {
//			// TODO: priority will be taken from extension point
//			private int getProviderPriority(String contributorId) {
//				if (PROVIDER_UI_USER.equals(contributorId)) {
//					return 1;
//				}
//				if (PROVIDER_UNKNOWN.equals(contributorId)) {
//					return 100;
//				}
//				return 666;
//			}
//
//			public int compare(String contributor1, String contributor2) {
//				return getProviderPriority(contributor1) - getProviderPriority(contributor2);
//			}
//		};
//		List<String> contributors = fStore.getProviders();
//		Collections.sort(contributors, comparator);
//		return contributors;
//	}

	public static List<ICLanguageSettingsContributor> getAllContributors() {
		ArrayList<ICLanguageSettingsContributor> list = new ArrayList<ICLanguageSettingsContributor>(contributors);
		list.addAll(LanguageSettingsExtensionManager.getAllContributorsFIXME());
		return list;
	}

	public static ICLanguageSettingsContributor getContributor(String id) {
//		for (ICLanguageSettingsContributor contributor : contributors) {
//			if (contributor.getId().equals(id))
//				return contributor;
//		}
		return LanguageSettingsExtensionManager.getContributor(id);
	}

	public static List<ICLanguageSettingEntry> getSettingEntriesReconciled(LanguageSettingsResourceDescriptor descriptor, int kind) {
		List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>();
		for (ICLanguageSettingsContributor contributor: getAllContributors()) {
			for (ICLanguageSettingEntry entry : getSettingEntries(descriptor, contributor.getId(), kind)) {
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

	public static void removeContributor(String id) {
		for (ICLanguageSettingsContributor contributor : contributors) {
			if (contributor.getId().equals(id)) {
				contributors.remove(contributor);
				return;
			}
		}
	}

	public static void addContributor(ICLanguageSettingsContributor contributor) {
		contributors.add(contributor);
		Collections.sort(contributors, new Comparator<ICLanguageSettingsContributor>() {
			public int compare(ICLanguageSettingsContributor c0, ICLanguageSettingsContributor c1) {
				return c0.getRank() - c1.getRank();
			}
		});
	}

	public static void load() {
		for (ICLanguageSettingsContributor contributor : contributors) {
			if (contributor instanceof ACLanguageSettingsSerializableContributor) {
				((ACLanguageSettingsSerializableContributor) contributor).fromXML();
			}
		}
	}

	public static void serialize() {
		for (ICLanguageSettingsContributor contributor : contributors) {
			if (contributor instanceof ACLanguageSettingsSerializableContributor) {
				((ACLanguageSettingsSerializableContributor) contributor).toXML();
			}
		}
	}

	/**
	 * Set and store in workspace area user defined contributors.
	 *
	 * @param contributors - array of user defined contributors
	 * @throws CoreException in case of problems
	 * @since 5.2
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
	 * Set and store default contributors IDs to be used if contributor list is empty.
	 *
	 * @param ids - default contributors IDs
	 * @throws BackingStoreException in case of problem with storing
	 * @since 5.2
	 */
	public static void setDefaultContributorIds(String[] ids) throws BackingStoreException {
		LanguageSettingsExtensionManager.setDefaultContributorIds(ids);
	}

	/**
	 * @return default contributors IDs to be used if contributor list is empty.
	 * @since 5.2
	 */
	public static String[] getDefaultContributorIds() {
		return LanguageSettingsExtensionManager.getDefaultContributorIds();
	}


}
