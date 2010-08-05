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

package org.eclipse.cdt.core.settings.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.core.resources.IResource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class LanguageSettingsSerializable extends LanguageSettingsBaseProvider implements Cloneable {
	public static final String ELEM_PROVIDER = "provider";
	private static final String ATTR_ID = "id";

	private static final String ELEM_CONFIGURATION = "configuration";
	private static final String ELEM_LANGUAGE = "language";
	private static final String ELEM_RESOURCE = "resource";
	private static final String ATTR_URI = "uri";

	private static final String ELEM_ENTRY = "entry";
	private static final String ATTR_KIND = "kind";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_VALUE = "value";

	private static final String ELEM_FLAG = "flag";
	
	
	private Map<String, // cfgDescriptionId
				Map<String, // languageId
					Map<URI, // resource URI
						List<ICLanguageSettingEntry>>>> fStorage = new HashMap<String, Map<String, Map<URI, List<ICLanguageSettingEntry>>>>();
	
	public LanguageSettingsSerializable() {
		super();
	}
	
	public LanguageSettingsSerializable(String id, String name) {
		super(id, name);
	}

	public LanguageSettingsSerializable(Element elementProvider) {
		load(elementProvider);
	}
	
	// TODO: look for refactoring this method
	private void setSettingEntriesInternal(String cfgId, URI rcUri, String languageId, List<ICLanguageSettingEntry> entries) {
		if (entries!=null) {
			Map<String, Map<URI, List<ICLanguageSettingEntry>>> cfgMap = fStorage.get(cfgId);
			if (cfgMap==null) {
				cfgMap = new HashMap<String, Map<URI, List<ICLanguageSettingEntry>>>();
				fStorage.put(cfgId, cfgMap);
			}
			Map<URI, List<ICLanguageSettingEntry>> langMap = cfgMap.get(languageId);
			if (langMap==null) {
				langMap = new HashMap<URI, List<ICLanguageSettingEntry>>();
				cfgMap.put(languageId, langMap);
			}
			langMap.put(rcUri, entries);
		} else {
			// do not keep nulls in the tables
			Map<String, Map<URI, List<ICLanguageSettingEntry>>> cfgMap = fStorage.get(cfgId);
			if (cfgMap!=null) {
				Map<URI, List<ICLanguageSettingEntry>> langMap = cfgMap.get(languageId);
				if (langMap!=null) {
					langMap.remove(rcUri);
					if (langMap.size()==0) {
						cfgMap.remove(languageId);
					}
				}
				if (cfgMap.size()==0) {
					fStorage.remove(cfgId);
				}
			}
		}
	}
	
	public void setSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId, List<ICLanguageSettingEntry> entries) {
		String cfgId = cfgDescription!=null ? cfgDescription.getId() : null;
		URI rcUri = rc!=null ? rc.getLocationURI() : null;
		setSettingEntriesInternal(cfgId, rcUri, languageId, entries);
	}
	
	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription,
			IResource rc, String languageId) {
		
		String cfgId = cfgDescription!=null ? cfgDescription.getId() : null;
		Map<String, Map<URI, List<ICLanguageSettingEntry>>> cfgMap = fStorage.get(cfgId);
		if (cfgMap!=null) {
			Map<URI, List<ICLanguageSettingEntry>> langMap = cfgMap.get(languageId);
			if (langMap!=null) {
				URI rcUri = rc!=null ? rc.getLocationURI() : null;
				List<ICLanguageSettingEntry> entries = langMap.get(rcUri);
				return entries;
			}
		}
		
		return null;
	}

	/*
	<provider id="provider.id">
		<configuration id="cfg.id">
			<language id="lang.id">
				<resource uri="file://">
					<settingEntry flags="" kind="includePath" name="path"/>
				</resource>
			</language>
		</configuration>
	</provider>
	*/
	// provider/configuration/language/resource/settingEntry
	public Element serialize(Element parentElement) {
		Element elementProvider = XmlUtil.appendElement(parentElement, ELEM_PROVIDER, new String[] {
				ATTR_ID, getId(),
				ATTR_NAME, getName(),
				ATTR_CLASS, getClass().getCanonicalName(),
			});
		
		for (Entry<String, Map<String, Map<URI, List<ICLanguageSettingEntry>>>> entryCfg : fStorage.entrySet()) {
			serializeConfiguration(elementProvider, entryCfg);
		}
		return elementProvider;
	}

	private void serializeConfiguration(Element parentElement, Entry<String, Map<String, Map<URI, List<ICLanguageSettingEntry>>>> entryCfg) {
		String cfgId = entryCfg.getKey();
		if (cfgId!=null) {
			Element elementConfiguration = XmlUtil.appendElement(parentElement, ELEM_CONFIGURATION, new String[] {ATTR_ID, cfgId});
			parentElement = elementConfiguration;
		}
		for (Entry<String, Map<URI, List<ICLanguageSettingEntry>>> entryLang : entryCfg.getValue().entrySet()) {
			serializeLanguage(parentElement, entryLang);
		}
	}

	private void serializeLanguage(Element parentElement, Entry<String, Map<URI, List<ICLanguageSettingEntry>>> entryLang) {
		String langId = entryLang.getKey();
		if (langId!=null) {
			Element elementLanguage = XmlUtil.appendElement(parentElement, ELEM_LANGUAGE, new String[] {ATTR_ID, langId});
			parentElement = elementLanguage;
		}
		for (Entry<URI, List<ICLanguageSettingEntry>> entryRc : entryLang.getValue().entrySet()) {
			serializeResource(parentElement, entryRc);
		}
	}

	private void serializeResource(Element parentElement, Entry<URI, List<ICLanguageSettingEntry>> entryRc) {
		URI rcUri = entryRc.getKey();
		String rcUriString = rcUri!=null ? rcUri.toString() : null;
		
		if (rcUriString!=null) {
			Element elementRc = XmlUtil.appendElement(parentElement, ELEM_RESOURCE, new String[] {ATTR_URI, rcUriString});
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
			case ICLanguageSettingEntry.MACRO:
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
	
	
	// provider/configuration/language/resource/settingEntry
	public void load(Element providerNode) {
		fStorage.clear();
	
		if (providerNode!=null) {
			String providerId = XmlUtil.determineAttributeValue(providerNode, ATTR_ID);
			String providerName = XmlUtil.determineAttributeValue(providerNode, ATTR_NAME);
			this.setId(providerId);
			this.setName(providerName);

			List<ICLanguageSettingEntry> settings = new ArrayList<ICLanguageSettingEntry>();
			NodeList nodes = providerNode.getChildNodes();
			for (int i=0;i<nodes.getLength();i++) {
				Node elementNode = nodes.item(i);
				if(elementNode.getNodeType() != Node.ELEMENT_NODE)
					continue;
				
				if (ELEM_CONFIGURATION.equals(elementNode.getNodeName())) {
					loadConfigurationElement(elementNode);
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
				setSettingEntriesInternal(null, null, null, settings);
			}
		}
	}

	private void loadConfigurationElement(Node parentNode) {
		String cfgId = XmlUtil.determineAttributeValue(parentNode, ATTR_ID);
		if (cfgId.length()==0) {
			cfgId=null;
		}
		
		List<ICLanguageSettingEntry> settings = new ArrayList<ICLanguageSettingEntry>();
		NodeList nodes = parentNode.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			Node elementNode = nodes.item(i);
			if (elementNode.getNodeType()!=Node.ELEMENT_NODE)
				continue;
			if (ELEM_LANGUAGE.equals(elementNode.getNodeName())) {
				loadLanguageElement(elementNode, cfgId);
			} else if (ELEM_RESOURCE.equals(elementNode.getNodeName())) {
				loadResourceElement(elementNode, cfgId, null);
			} else if (ELEM_ENTRY.equals(elementNode.getNodeName())) {
				ICLanguageSettingEntry entry = loadSettingEntry(elementNode);
				if (entry!=null) {
					settings.add(entry);
				}
			}
		}
		// set settings
		if (settings.size()>0) {
			setSettingEntriesInternal(cfgId, null, null, settings);
		}
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
			setSettingEntriesInternal(cfgId, null, langId, settings);
		}
	}

	private void loadResourceElement(Node parentNode, String cfgId, String langId) {
		String rcUriString = XmlUtil.determineAttributeValue(parentNode, ATTR_URI);
		URI rcUri = null;
		if (rcUriString.length()>0) {
			try {
				rcUri = new URI(rcUriString);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}

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
			setSettingEntriesInternal(cfgId, rcUri, langId, settings);
		}
	}

	private Map<String, Map<String, Map<URI, List<ICLanguageSettingEntry>>>> cloneStorage() {
		Map<String, // cfgDescriptionId
			Map<String, // languageId
				Map<URI, // resource URI
					List<ICLanguageSettingEntry>>>> storageClone = new HashMap<String, Map<String, Map<URI, List<ICLanguageSettingEntry>>>>();
		Set<Entry<String, Map<String, Map<URI, List<ICLanguageSettingEntry>>>>> entrySetCfg = fStorage.entrySet();
		for (Entry<String, Map<String, Map<URI, List<ICLanguageSettingEntry>>>> entryCfg : entrySetCfg) {
			String cfgDescriptionId = entryCfg.getKey();
			Map<String, Map<URI, List<ICLanguageSettingEntry>>> mapLang = entryCfg.getValue();
			Map<String, Map<URI, List<ICLanguageSettingEntry>>> mapLangClone = new HashMap<String, Map<URI, List<ICLanguageSettingEntry>>>();
			Set<Entry<String, Map<URI, List<ICLanguageSettingEntry>>>> entrySetLang = mapLang.entrySet();
			for (Entry<String, Map<URI, List<ICLanguageSettingEntry>>> entryLang : entrySetLang) {
				String langId = entryLang.getKey();
				Map<URI, List<ICLanguageSettingEntry>> mapRc = entryLang.getValue();
				Map<URI, List<ICLanguageSettingEntry>> mapRcClone = new HashMap<URI, List<ICLanguageSettingEntry>>();
				Set<Entry<URI, List<ICLanguageSettingEntry>>> entrySetRc = mapRc.entrySet();
				for (Entry<URI, List<ICLanguageSettingEntry>> entryRc : entrySetRc) {
					URI rcURI = entryRc.getKey();
					List<ICLanguageSettingEntry> lsEntries = entryRc.getValue();
					List<ICLanguageSettingEntry> lsEntriesClone = new ArrayList<ICLanguageSettingEntry>(lsEntries);
					mapRcClone.put(rcURI, lsEntriesClone);
				}
				mapLangClone.put(langId, mapRcClone);
			}
			storageClone.put(cfgDescriptionId, mapLangClone);
		}
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof LanguageSettingsSerializable) {
			LanguageSettingsSerializable that = (LanguageSettingsSerializable)o;
			// FIXME? Or it is OK, the tests pass?
			return this.fStorage.equals(that.fStorage);
		}
		return false;

	}

	
}
