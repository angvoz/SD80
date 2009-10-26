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
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.errorparsers.RegexErrorParser;
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

public class LanguageSettingsExtensionManager {
	/**
	 * Name of the extension point for contributing an error parser
	 */
	public final static String CONTRIBUTOR_EXTENSION_ID = "LanguageSettingsContributor"; //$NON-NLS-1$
	private static final String ELEM_CONTRIBUTOR = "contributor"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_RANK = "rank"; //$NON-NLS-1$

	private static final String ELEM_LANGUAGE = "language"; //$NON-NLS-1$

	private static final String ELEM_ENTRY = "entry"; //$NON-NLS-1$
	private static final String ELEM_FLAG = "flag"; //$NON-NLS-1$
	private static final String ATTR_KIND = "kind"; //$NON-NLS-1$
	private static final String ATTR_VALUE = "value"; //$NON-NLS-1$

	private static final LinkedHashMap<String, ICLanguageSettingsContributor> globalLanguageSettingContributors = new LinkedHashMap<String, ICLanguageSettingsContributor>();

	static {
		loadExtensions();
	}



	/**
	 * Load error parser contributed extensions.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	synchronized public static void loadExtensions() {

		List<ICLanguageSettingsContributor> contributors = new ArrayList<ICLanguageSettingsContributor>();
		loadExtensions(Platform.getExtensionRegistry(), contributors);

		globalLanguageSettingContributors.clear();
		for (ICLanguageSettingsContributor contributor : contributors) {
			globalLanguageSettingContributors.put(contributor.getId(), contributor);
		}
	}

	/**
	 * Load contributed extensions from extension registry.
	 *
	 * @param registry - extension registry
	 * @param contributors - resulting set of contributors
	 */
	private static void loadExtensions(IExtensionRegistry registry, List<ICLanguageSettingsContributor> contributors) {
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
	 * Creates empty non-configured error parser as executable extension from extension point definition.
	 * If "class" attribute is empty RegexErrorParser is created.
	 *
	 * @param initialId - nominal ID of error parser
	 * @param initialName - nominal name of error parser
	 * @param ce - configuration element with error parser definition
	 * @return new non-configured error parser
	 * @throws CoreException in case of failure
	 */
	private static ICLanguageSettingsContributor createExecutableContributor(IConfigurationElement ce) throws CoreException {
		String conClass = ce.getAttribute(ATTR_CLASS);
		String conId = determineAttributeValue(ce, ATTR_ID);
		String conName = determineAttributeValue(ce, ATTR_NAME);
		int conRank = Integer.parseInt(determineAttributeValue(ce, ATTR_RANK));
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
			contributor = new LanguageSettingsDefaultContributor(conId, conName, conRank, languages, entries);
		}
		return contributor;
	}

	/**
	 * Creates empty non-configured error parser from extension point definition looking at "class" attribute.
	 * ID and name of error parser are assigned from first extension point encountered.
	 *
	 * @param className - full qualified class name of error parser.
	 * @param registry - extension registry
	 * @return new non-configured error parser
	 */
	private static ICLanguageSettingsContributor createContributorCarcass(String className, IExtensionRegistry registry) {
		if (className==null || className.length()==0 || className.equals(RegexErrorParser.class.getName())) {
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
	 * @return IDs of language settings contributors of LanguageSettingContributor extension point.
	 */
	public static String[] getLanguageSettingsExtensionIds() {
		return globalLanguageSettingContributors.keySet().toArray(new String[0]);
	}

	public static ICLanguageSettingsContributor getContributor(String id) {
		return globalLanguageSettingContributors.get(id);
	}
}
