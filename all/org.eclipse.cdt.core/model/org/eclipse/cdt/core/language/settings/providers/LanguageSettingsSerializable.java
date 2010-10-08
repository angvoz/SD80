/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.core.resources.IResource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class LanguageSettingsSerializable extends LanguageSettingsBaseProvider implements Cloneable {
	public static final String ELEM_PROVIDER = "provider";
	private static final String ATTR_ID = "id";

	private static final String ELEM_LANGUAGE_SCOPE = "language-scope";
	private static final String ELEM_LANGUAGE = "language";
	private static final String ELEM_RESOURCE = "resource";
	private static final String ATTR_PROJECT_PATH = "project-relative-path";

	private static final String ELEM_ENTRY = "entry";
	private static final String ATTR_KIND = "kind";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_PARAMETER = "parameter";
	private static final String ATTR_VALUE = "value";

	private static final String ELEM_FLAG = "flag";


	private Map<String, // languageId
				Map<String, // resource project path
					List<ICLanguageSettingEntry>>> fStorage = new HashMap<String, Map<String, List<ICLanguageSettingEntry>>>();

	public LanguageSettingsSerializable() {
		super();
	}

	public LanguageSettingsSerializable(String id, String name) {
		super(id, name);
	}

	public LanguageSettingsSerializable(Element elementProvider) {
		load(elementProvider);
	}

	/**
	 * @return {@code true} if the provider does not keep any settings yet or {@code false} if there are some.
	 */
	public boolean isEmpty() {
		return fStorage.isEmpty();
	}

	public void setLanguageIds(List <String> languages) {
		this.languages = new ArrayList<String>(languages);
	}

	public void setCustomParameter(String customParameter) {
		this.customParameter = customParameter;
	}

	public void clear() {
		fStorage.clear();
	}

	// TODO: look for refactoring this method
	private void setSettingEntriesInternal(String rcProjectPath, String languageId, List<ICLanguageSettingEntry> entries) {
		if (entries!=null) {
			Map<String, List<ICLanguageSettingEntry>> langMap = fStorage.get(languageId);
			if (langMap==null) {
				langMap = new HashMap<String, List<ICLanguageSettingEntry>>();
				fStorage.put(languageId, langMap);
			}
			langMap.put(rcProjectPath, entries);
		} else {
			// do not keep nulls in the tables
			Map<String, List<ICLanguageSettingEntry>> langMap = fStorage.get(languageId);
			if (langMap!=null) {
				langMap.remove(rcProjectPath);
				if (langMap.size()==0) {
					fStorage.remove(languageId);
				}
			}
		}
	}

	public void setSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId, List<ICLanguageSettingEntry> entries) {
		String cfgId = cfgDescription!=null ? cfgDescription.getId() : null;
		String rcProjectPath = rc!=null ? rc.getProjectRelativePath().toString() : null;
		setSettingEntriesInternal(rcProjectPath, languageId, entries);
	}

	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		Map<String, List<ICLanguageSettingEntry>> langMap = fStorage.get(languageId);
		if (langMap!=null) {
			String rcProjectPath = rc!=null ? rc.getProjectRelativePath().toString() : null;
			List<ICLanguageSettingEntry> entries = langMap.get(rcProjectPath);
			return entries;
		}

		return null;
	}

	/*
	<provider id="provider.id" ...>
		<language-scope id="lang.id"/>
		<language id="lang.id">
			<resource project-relative-path="/">
				<entry flags="" kind="includePath" name="path"/>
			</resource>
		</language>
	</provider>
	*/
	// provider/configuration/language/resource/entry
	public Element serialize(Element parentElement) {
		Element elementProvider = XmlUtil.appendElement(parentElement, ELEM_PROVIDER, new String[] {
				ATTR_ID, getId(),
				ATTR_NAME, getName(),
				ATTR_CLASS, getClass().getCanonicalName(),
				ATTR_PARAMETER, getCustomParameter(),
			});

		if (languages!=null) {
			for (String langId : languages) {
				XmlUtil.appendElement(elementProvider, ELEM_LANGUAGE_SCOPE, new String[] {ATTR_ID, langId});
			}
		}
		for (Entry<String, Map<String, List<ICLanguageSettingEntry>>> entryLang : fStorage.entrySet()) {
			serializeLanguage(elementProvider, entryLang);
		}
		return elementProvider;
	}

	private void serializeLanguage(Element parentElement, Entry<String, Map<String, List<ICLanguageSettingEntry>>> entryLang) {
		String langId = entryLang.getKey();
		if (langId!=null) {
			Element elementLanguage = XmlUtil.appendElement(parentElement, ELEM_LANGUAGE, new String[] {ATTR_ID, langId});
			parentElement = elementLanguage;
		}
		for (Entry<String, List<ICLanguageSettingEntry>> entryRc : entryLang.getValue().entrySet()) {
			serializeResource(parentElement, entryRc);
		}
	}

	private void serializeResource(Element parentElement, Entry<String, List<ICLanguageSettingEntry>> entryRc) {
		String rcProjectPath = entryRc.getKey();
		if (rcProjectPath!=null) {
			Element elementRc = XmlUtil.appendElement(parentElement, ELEM_RESOURCE, new String[] {ATTR_PROJECT_PATH, rcProjectPath});
			parentElement = elementRc;
		}
		serializeSettingEntries(parentElement, entryRc.getValue());
	}

	private void serializeSettingEntries(Element parentElement, List<ICLanguageSettingEntry> settingEntries) {
		for (ICLanguageSettingEntry entry : settingEntries) {
			Element elementSettingEntry = XmlUtil.appendElement(parentElement, ELEM_ENTRY, new String[] {
					ATTR_KIND, LanguageSettingEntriesSerializer.kindToString(entry.getKind()),
					ATTR_NAME, entry.getName(),
				});
			switch(entry.getKind()) {
			case ICSettingEntry.MACRO:
				elementSettingEntry.setAttribute(ATTR_VALUE, entry.getValue());
				break;
//						case ICLanguageSettingEntry.LIBRARY_FILE:
			// TODO: sourceAttachment fields need to be covered
//							break;
			}
			int flags = entry.getFlags();
			if (flags!=0) {
				// Element elementFlag =
				XmlUtil.appendElement(elementSettingEntry, ELEM_FLAG, new String[] {
						ATTR_VALUE, LanguageSettingEntriesSerializer.composeFlagsString(entry.getFlags())
					});
			}
		}
	}

	private ICLanguageSettingEntry loadSettingEntry(Node parentElement) {
		String settingKind = XmlUtil.determineAttributeValue(parentElement, ATTR_KIND);
		String settingName = XmlUtil.determineAttributeValue(parentElement, ATTR_NAME);

		NodeList flagNodes = parentElement.getChildNodes();
		int flags = 0;
		for (int i=0;i<flagNodes.getLength();i++) {
			Node flagNode = flagNodes.item(i);
			if(flagNode.getNodeType() != Node.ELEMENT_NODE || !ELEM_FLAG.equals(flagNode.getNodeName()))
				continue;

			String settingFlags = XmlUtil.determineAttributeValue(flagNode, ATTR_VALUE);
			int bitFlag = LanguageSettingEntriesSerializer.composeFlags(settingFlags);
			flags |= bitFlag;

		}

		ICLanguageSettingEntry entry = null;
		switch (LanguageSettingEntriesSerializer.stringToKind(settingKind)) {
		case ICSettingEntry.INCLUDE_PATH:
			entry = new CIncludePathEntry(settingName, flags);
			break;
		case ICSettingEntry.INCLUDE_FILE:
			entry = new CIncludeFileEntry(settingName, flags);
			break;
		case ICSettingEntry.MACRO:
			String settingValue = XmlUtil.determineAttributeValue(parentElement, ATTR_VALUE);
			entry = new CMacroEntry(settingName, settingValue, flags);
			break;
		case ICSettingEntry.MACRO_FILE:
			entry = new CMacroFileEntry(settingName, flags);
			break;
		case ICSettingEntry.LIBRARY_PATH:
			entry = new CLibraryPathEntry(settingName, flags);
			break;
		case ICSettingEntry.LIBRARY_FILE:
			entry = new CLibraryFileEntry(settingName, flags);
			break;
		}
		return entry;
	}


	// provider/configuration/language/resource/entry
	public void load(Element providerNode) {
		fStorage.clear();
		languages = null;

		if (providerNode!=null) {
			String providerId = XmlUtil.determineAttributeValue(providerNode, ATTR_ID);
			String providerName = XmlUtil.determineAttributeValue(providerNode, ATTR_NAME);
			String providerParameter = XmlUtil.determineAttributeValue(providerNode, ATTR_PARAMETER);
			this.setId(providerId);
			this.setName(providerName);
			this.setCustomParameter(providerParameter);

			List<ICLanguageSettingEntry> settings = new ArrayList<ICLanguageSettingEntry>();
			NodeList nodes = providerNode.getChildNodes();
			for (int i=0;i<nodes.getLength();i++) {
				Node elementNode = nodes.item(i);
				if(elementNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				if (ELEM_LANGUAGE_SCOPE.equals(elementNode.getNodeName())) {
					loadLanguageScopeElement(elementNode);
				} else if (ELEM_LANGUAGE.equals(elementNode.getNodeName())) {
					loadLanguageElement(elementNode, null);
				} else if (ELEM_RESOURCE.equals(elementNode.getNodeName())) {
					loadResourceElement(elementNode, null, null);
				} else if (ELEM_ENTRY.equals(elementNode.getNodeName())) {
					ICLanguageSettingEntry entry = loadSettingEntry(elementNode);
					if (entry!=null) {
						settings.add(entry);
					}
				}
			}
			// set settings
			if (settings.size()>0) {
				setSettingEntriesInternal(null, null, settings);
			}
		}
	}

	private void loadLanguageScopeElement(Node parentNode) {
		if (languages==null) {
			languages = new ArrayList<String>();
		}
		String id = XmlUtil.determineAttributeValue(parentNode, ATTR_ID);
		languages.add(id);

	}

	private void loadLanguageElement(Node parentNode, String cfgId) {
		String langId = XmlUtil.determineAttributeValue(parentNode, ATTR_ID);
		if (langId.length()==0) {
			langId=null;
		}

		List<ICLanguageSettingEntry> settings = new ArrayList<ICLanguageSettingEntry>();
		NodeList nodes = parentNode.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			Node elementNode = nodes.item(i);
			if(elementNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (ELEM_RESOURCE.equals(elementNode.getNodeName())) {
				loadResourceElement(elementNode, cfgId, langId);
			} else if (ELEM_ENTRY.equals(elementNode.getNodeName())) {
				ICLanguageSettingEntry entry = loadSettingEntry(elementNode);
				if (entry!=null) {
					settings.add(entry);
				}
			}
		}
		// set settings
		if (settings.size()>0) {
			setSettingEntriesInternal(null, langId, settings);
		}
	}

	private void loadResourceElement(Node parentNode, String cfgId, String langId) {
		String rcProjectPath = XmlUtil.determineAttributeValue(parentNode, ATTR_PROJECT_PATH);

		List<ICLanguageSettingEntry> settings = new ArrayList<ICLanguageSettingEntry>();
		NodeList nodes = parentNode.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			Node elementNode = nodes.item(i);
			if(elementNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (ELEM_ENTRY.equals(elementNode.getNodeName())) {
				ICLanguageSettingEntry entry = loadSettingEntry(elementNode);
				if (entry!=null) {
					settings.add(entry);
				}
			}
		}

		// set settings
		if (settings.size()>0) {
			setSettingEntriesInternal(rcProjectPath, langId, settings);
		}
	}

	private Map<String, Map<String, List<ICLanguageSettingEntry>>> cloneStorage() {
		Map<String, // languageId
			Map<String, // resource String
				List<ICLanguageSettingEntry>>> storageClone = new HashMap<String, Map<String, List<ICLanguageSettingEntry>>>();
//		Set<Entry<String, Map<String, Map<String, List<ICLanguageSettingEntry>>>>> entrySetCfg = fStorage.entrySet();
//		for (Entry<String, Map<String, Map<String, List<ICLanguageSettingEntry>>>> entryCfg : entrySetCfg) {
//			String cfgDescriptionId = entryCfg.getKey();
//			Map<String, Map<String, List<ICLanguageSettingEntry>>> mapLang = entryCfg.getValue();
//			Map<String, Map<String, List<ICLanguageSettingEntry>>> mapLangClone = new HashMap<String, Map<String, List<ICLanguageSettingEntry>>>();
			Set<Entry<String, Map<String, List<ICLanguageSettingEntry>>>> entrySetLang = fStorage.entrySet();
			for (Entry<String, Map<String, List<ICLanguageSettingEntry>>> entryLang : entrySetLang) {
				String langId = entryLang.getKey();
				Map<String, List<ICLanguageSettingEntry>> mapRc = entryLang.getValue();
				Map<String, List<ICLanguageSettingEntry>> mapRcClone = new HashMap<String, List<ICLanguageSettingEntry>>();
				Set<Entry<String, List<ICLanguageSettingEntry>>> entrySetRc = mapRc.entrySet();
				for (Entry<String, List<ICLanguageSettingEntry>> entryRc : entrySetRc) {
					String rcProjectPath = entryRc.getKey();
					List<ICLanguageSettingEntry> lsEntries = entryRc.getValue();
					List<ICLanguageSettingEntry> lsEntriesClone = new ArrayList<ICLanguageSettingEntry>(lsEntries);
					mapRcClone.put(rcProjectPath, lsEntriesClone);
				}
//				mapLangClone.put(langId, mapRcClone);
				storageClone.put(langId, mapRcClone);
			}
//		}
		return storageClone;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public LanguageSettingsSerializable clone() throws CloneNotSupportedException {
		LanguageSettingsSerializable clone = (LanguageSettingsSerializable)super.clone();
		clone.fStorage = cloneStorage();
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((languages == null) ? 0 : languages.hashCode());
		result = prime * result + ((customParameter == null) ? 0 : customParameter.hashCode());
		result = prime * result + ((fStorage == null) ? 0 : fStorage.hashCode());
		result = prime * result + getClass().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LanguageSettingsSerializable other = (LanguageSettingsSerializable) obj;

		String id = getId();
		String otherId = other.getId();
		if (id == null) {
			if (otherId != null)
				return false;
		} else if (!id.equals(otherId))
			return false;

		String name = getName();
		String otherName = other.getName();
		if (name == null) {
			if (otherName != null)
				return false;
		} else if (!name.equals(otherName))
			return false;

		if (languages == null) {
			if (other.languages != null)
				return false;
		} else if (!languages.equals(other.languages))
			return false;

		if (customParameter == null) {
			if (other.customParameter != null)
				return false;
		} else if (!customParameter.equals(other.customParameter))
			return false;

		if (fStorage == null) {
			if (other.fStorage != null)
				return false;
		} else if (!fStorage.equals(other.fStorage))
			return false;
		return true;
	}
}
