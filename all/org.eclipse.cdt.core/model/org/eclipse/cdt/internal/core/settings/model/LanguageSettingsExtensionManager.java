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

package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingsContributor;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class LanguageSettingsExtensionManager {
	private static final String PREFERENCE_CONTRIBUTOR_DEFAULT_IDS = "lang.settings.contributor.default.ids"; //$NON-NLS-1$
	private static final String NONE = ""; //$NON-NLS-1$
	public static final char CONTRIBUTOR_DELIMITER = ';';
	/**
	 * Name of the extension point for contributing language settings
	 */
	public final static String CONTRIBUTOR_EXTENSION_ID = "LanguageSettingsContributor"; //$NON-NLS-1$
	private static final String ELEM_CONTRIBUTOR = "contributor"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$

	private static final String ELEM_LANGUAGE = "language"; //$NON-NLS-1$

	private static final String ELEM_ENTRY = "entry"; //$NON-NLS-1$
	private static final String ELEM_FLAG = "flag"; //$NON-NLS-1$
	private static final String ATTR_KIND = "kind"; //$NON-NLS-1$
	private static final String ATTR_VALUE = "value"; //$NON-NLS-1$

	private static final LinkedHashMap<String, ICLanguageSettingsContributor> fExtensionContributors = new LinkedHashMap<String, ICLanguageSettingsContributor>();
	private static final LinkedHashMap<String, ICLanguageSettingsContributor> fAvailableContributors = new LinkedHashMap<String, ICLanguageSettingsContributor>();
	private static LinkedHashMap<String, ICLanguageSettingsContributor> fUserDefinedContributors = null;
	private static List<String> fDefaultContributorIds = null;

	static {
//		loadUserDefinedContributors();
		loadDefaultContributorIds();
		loadContributorExtensions();
	}


	/**
	 * Load workspace default contributor IDs to be used if no contributors specified.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	synchronized public static void loadDefaultContributorIds() {
		fDefaultContributorIds = null;
		IEclipsePreferences preferences = new InstanceScope().getNode(CCorePlugin.PLUGIN_ID);
		String ids = preferences.get(PREFERENCE_CONTRIBUTOR_DEFAULT_IDS, NONE);
		if (ids.equals(NONE)) {
			return;
		}

		fDefaultContributorIds = Arrays.asList(ids.split(String.valueOf(CONTRIBUTOR_DELIMITER)));
	}


	/**
	 * Load contributor contributed extensions.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	synchronized public static void loadContributorExtensions() {

		List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();
		loadContributorExtensions(Platform.getExtensionRegistry(), contributors);

		fExtensionContributors.clear();
		for (ICLanguageSettingsContributor contributor : contributors) {
			fExtensionContributors.put(contributor.getId(), contributor);
		}

		recalculateAvailableContributors();
	}

	/**
	 * Populate the list of available contributors where workspace level user defined parsers
	 * overwrite contributed through contributor extension point.
	 */
	private static void recalculateAvailableContributors() {
		fAvailableContributors.clear();
		if (fUserDefinedContributors!=null) {
			fAvailableContributors.putAll(fUserDefinedContributors);
		}
		for (ICLanguageSettingsContributor contributor : fExtensionContributors.values()) {
			String id = contributor.getId();
			if (!fAvailableContributors.containsKey(id)) {
				fAvailableContributors.put(id, contributor);
			}
		}
	}


	/**
	 * Load contributed extensions from extension registry.
	 *
	 * @param registry - extension registry
	 * @param contributors - resulting set of contributors
	 */
	private static void loadContributorExtensions(IExtensionRegistry registry, List<ICLanguageSettingsContributor> contributors) {
		contributors.clear();
		IExtensionPoint extension = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID, CONTRIBUTOR_EXTENSION_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				try {
					String extensionID = ext.getUniqueIdentifier();
					String extensionName = ext.getLabel();
					ICLanguageSettingsContributor contributor = null;
					for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
						if (cfgEl.getName().equals(ELEM_CONTRIBUTOR)) {
							contributor = createExecutableContributor(cfgEl);
							contributors.add(contributor);
						}
					}
				} catch (Exception e) {
					CCorePlugin.log("Cannot load LanguageSettingsContributor extension " + ext.getUniqueIdentifier(), e); //$NON-NLS-1$
				}
			}
		}
	}

	private static String determineAttributeValue(IConfigurationElement ce, String attr) {
		String value = ce.getAttribute(attr);
		return value!=null ? value : ""; //$NON-NLS-1$
	}

	/**
	 * Creates empty non-configured contributor as executable extension from extension point definition.
	 * If "class" attribute is empty TODO is created.
	 *
	 * @param initialId - nominal ID of contributor
	 * @param initialName - nominal name of contributor
	 * @param ce - configuration element with contributor definition
	 * @return new non-configured contributor
	 * @throws CoreException in case of failure
	 */
	private static ICLanguageSettingsContributor createExecutableContributor(IConfigurationElement ce) throws CoreException {
		String conClass = ce.getAttribute(ATTR_CLASS);
		String conId = determineAttributeValue(ce, ATTR_ID);
		String conName = determineAttributeValue(ce, ATTR_NAME);
		List<String> languages = null;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();

		for (IConfigurationElement ceLang : ce.getChildren(ELEM_LANGUAGE)) {
			String langId = determineAttributeValue(ceLang, ATTR_ID);
			if (langId.trim().length()>0) {
				if (languages==null) {
					languages = new ArrayList<String>();
					languages.add(langId);
				}
			}
		}

		for (IConfigurationElement ceEntry : ce.getChildren(ELEM_ENTRY)) {
			try {
				int entryKind = LanguageSettingEntriesSerializer.stringToKind(determineAttributeValue(ceEntry, ATTR_KIND));
				String entryName = determineAttributeValue(ceEntry, ATTR_NAME);
				String entryValue = determineAttributeValue(ceEntry, ATTR_VALUE);

				int flags = 0;
				for (IConfigurationElement ceFlags : ceEntry.getChildren(ELEM_FLAG)) {
					int bitFlag = LanguageSettingEntriesSerializer.composeFlags(determineAttributeValue(ceFlags, ATTR_VALUE));
					flags |= bitFlag;
				}

				ICLanguageSettingEntry entry = (ICLanguageSettingEntry) CDataUtil.createEntry(
						entryKind, entryName, entryValue, null, flags);
				entries.add(entry);

			} catch (Exception e) {
				CCorePlugin.log("Error creating language settings entry ", e); //$NON-NLS-1$
			}
		}

		ICLanguageSettingsContributor contributor = null;
		if (conClass!=null && !conClass.equals(LanguageSettingsDefaultContributor.class.getCanonicalName())) {
			contributor = (ICLanguageSettingsContributor)ce.createExecutableExtension(ATTR_CLASS);
		}
		if (contributor==null) {
			contributor = new LanguageSettingsDefaultContributor(conId, conName, languages, entries);
		}
		return contributor;
	}

	/**
	 * Creates empty non-configured contributor from extension point definition looking at "class" attribute.
	 * ID and name of contributor are assigned from first extension point encountered.
	 *
	 * @param className - full qualified class name of contributor.
	 * @param registry - extension registry
	 * @return new non-configured contributor
	 */
	private static ICLanguageSettingsContributor createContributorCarcass(String className, IExtensionRegistry registry) {
		if (className==null || className.length()==0 /*|| className.equals(RegexContributor.class.getName())*/) {
//			return new LanguageSettingsCoreContributor();
			return null;
		}

		try {
			IExtensionPoint extension = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID, CCorePlugin.ERROR_PARSER_SIMPLE_ID);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension ext : extensions) {
					String extensionID = ext.getUniqueIdentifier();
					String oldStyleId = extensionID;
					String oldStyleName = ext.getLabel();
					for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
						if (cfgEl.getName().equals(ELEM_CONTRIBUTOR) && className.equals(cfgEl.getAttribute(ATTR_CLASS))) {
							return createExecutableContributor(cfgEl);
						}
					}
				}
			}
		} catch (Exception e) {
			CCorePlugin.log("Error creating language settings contributor.", e); //$NON-NLS-1$
		}
		return null;
	}


	/**
	 * FIXME - to clone
	 * @param id - ID of contributor
	 * @return cloned copy of contributor. Note that {@link ContributorNamedWrapper} returns
	 * shallow copy with the same instance of underlying contributor.
	 */
	public static ICLanguageSettingsContributor getContributor(String id) {
		ICLanguageSettingsContributor contributor = fAvailableContributors.get(id);

//		try {
//			if (contributor instanceof RegexContributor) {
//				return (RegexContributor) ((RegexContributor)contributor).clone();
//			} else if (contributor instanceof ContributorNamedWrapper) {
//				return (ContributorNamedWrapper) ((ContributorNamedWrapper)contributor).clone();
//			}
//		} catch (CloneNotSupportedException e) {
//			CCorePlugin.log(e);
//		}
		return contributor;
	}

	/**
	 * @param ids - array of contributor IDs
	 * @return contributor IDs delimited with contributor delimiter ";"
	 * @since 5.2
	 */
	public static String toDelimitedString(String[] ids) {
		String result=""; //$NON-NLS-1$
		for (String id : ids) {
			if (result.length()==0) {
				result = id;
			} else {
				result += CONTRIBUTOR_DELIMITER + id;
			}
		}
		return result;
	}

	/**
	 * Save the list of default contributors in preferences.
	 *
	 * @throws BackingStoreException in case of problem storing
	 */
	public static void serializeDefaultContributorIds() throws BackingStoreException {
		IEclipsePreferences preferences = new InstanceScope().getNode(CCorePlugin.PLUGIN_ID);
		String ids = NONE;
		if (fDefaultContributorIds!=null) {
			ids = toDelimitedString(fDefaultContributorIds.toArray(new String[0]));
		}

		preferences.put(PREFERENCE_CONTRIBUTOR_DEFAULT_IDS, ids);
		preferences.flush();
	}

	/**
	 * Set and store in workspace area user defined contributors.
	 *
	 * @param contributors - array of user defined contributors
	 * @throws CoreException in case of problems
	 */
	public static void setUserDefinedContributors(ICLanguageSettingsContributor[] contributors) throws CoreException {
		setUserDefinedContributorsInternal(contributors);
//		serializeUserDefinedContributors();
	}

	/**
	 * Internal method to set user defined contributors in memory.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * Use {@link #setUserDefinedContributors(ICLanguageSettingsContributor[])}.
	 *
	 * @param contributors - array of user defined contributors
	 */
	public static void setUserDefinedContributorsInternal(ICLanguageSettingsContributor[] contributors) {
		if (contributors==null) {
			fUserDefinedContributors = null;
		} else {
			fUserDefinedContributors= new LinkedHashMap<String, ICLanguageSettingsContributor>();
			// set customized list
			for (ICLanguageSettingsContributor contributor : contributors) {
				fUserDefinedContributors.put(contributor.getId(), contributor);
			}
		}
		recalculateAvailableContributors();
	}

	/**
	 * @return available contributors IDs which include contributed through extension + user defined ones
	 * from workspace
	 */
	public static String[] getContributorAvailableIds() {
		return fAvailableContributors.keySet().toArray(new String[0]);
	}

	/**
	 * @return IDs of language settings contributors of LanguageSettingContributor extension point.
	 */
	public static String[] getContributorExtensionIds() {
		return fExtensionContributors.keySet().toArray(new String[0]);
	}


	/**
	 * Set and store default contributors IDs to be used if contributor list is empty.
	 *
	 * @param ids - default contributors IDs
	 * @throws BackingStoreException in case of problem with storing
	 */
	public static void setDefaultContributorIds(String[] ids) throws BackingStoreException {
		setDefaultContributorIdsInternal(ids);
//		serializeDefaultContributorIds();
	}

	/**
	 * Set default contributors IDs in internal list.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * Use {@link #setDefaultContributorIds(String[])}.
	 *
	 * @param ids - default contributors IDs
	 */
	public static void setDefaultContributorIdsInternal(String[] ids) {
		if (ids==null) {
			fDefaultContributorIds = null;
		} else {
			fDefaultContributorIds = new ArrayList<String>(Arrays.asList(ids));
		}
	}

	/**
	 * @return default contributors IDs to be used if contributor list is empty.
	 */
	public static String[] getDefaultContributorIds() {
		if (fDefaultContributorIds==null) {
			return fAvailableContributors.keySet().toArray(new String[0]);
		}
		return fDefaultContributorIds.toArray(new String[0]);
	}


}
