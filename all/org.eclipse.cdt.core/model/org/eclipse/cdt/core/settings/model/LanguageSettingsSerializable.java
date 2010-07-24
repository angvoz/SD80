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

import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class LanguageSettingsSerializable extends LanguageSettingsBaseProvider {
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
	private void setSettingEntries(String cfgId, URI rcUri, String languageId, List<ICLanguageSettingEntry> entries) {
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
		setSettingEntries(cfgId, rcUri, languageId, entries);
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
			Element elementFlag = XmlUtil.appendElement(elementSettingEntry, ELEM_FLAG, new String[] {
					ATTR_VALUE, LanguageSettingEntriesSerializer.composeFlagsString(entry.getFlags())
				});
		}
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
			String cfgId = entryCfg.getKey();
			if (cfgId==null) {
				cfgId = ""; //$NON-NLS-1$
			}
			Element elementConfiguration = XmlUtil.appendElement(elementProvider, ELEM_CONFIGURATION, new String[] {ATTR_ID, cfgId});
			for (Entry<String, Map<URI, List<ICLanguageSettingEntry>>> entryLang : entryCfg.getValue().entrySet()) {
				String langId = entryLang.getKey();
				if (langId==null) {
					langId = ""; //$NON-NLS-1$
				}
				Element elementLanguage = XmlUtil.appendElement(elementConfiguration, ELEM_LANGUAGE, new String[] {ATTR_ID, langId});
				for (Entry<URI, List<ICLanguageSettingEntry>> entryRc : entryLang.getValue().entrySet()) {
					URI rcUri = entryRc.getKey();
					String rcUriString = rcUri!=null ? rcUri.toString() : ""; //$NON-NLS-1$
					
					Element elementRc = XmlUtil.appendElement(elementLanguage, ELEM_RESOURCE, new String[] {ATTR_URI, rcUriString});
					serializeSettingEntries(elementRc, entryRc.getValue());
				}
			}
		}
		return elementProvider;
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

			NodeList cfgNodes = providerNode.getChildNodes();
			for (int icfg=0;icfg<cfgNodes.getLength();icfg++) {
				Node cfgNode = cfgNodes.item(icfg);
				if(cfgNode.getNodeType() != Node.ELEMENT_NODE || ! ELEM_CONFIGURATION.equals(cfgNode.getNodeName()))
					continue;

				String cfgId = XmlUtil.determineAttributeValue(cfgNode, ATTR_ID);
				if (cfgId.length()==0) {
					cfgId=null;
				}
				
				NodeList langNodes = cfgNode.getChildNodes();
				for (int ilang=0;ilang<langNodes.getLength();ilang++) {
					Node langNode = langNodes.item(ilang);
					if(langNode.getNodeType() != Node.ELEMENT_NODE || ! ELEM_LANGUAGE.equals(langNode.getNodeName()))
						continue;

					String langId = XmlUtil.determineAttributeValue(langNode, ATTR_ID);
					if (langId.length()==0) {
						langId=null;
					}
					
					NodeList rcNodes = langNode.getChildNodes();
					for (int irc=0;irc<rcNodes.getLength();irc++) {
						Node rcNode = rcNodes.item(irc);
						if(rcNode.getNodeType() != Node.ELEMENT_NODE || ! ELEM_RESOURCE.equals(rcNode.getNodeName()))
							continue;
						
						String rcUriString = XmlUtil.determineAttributeValue(rcNode, ATTR_URI);
						URI rcUri = null;
						if (rcUriString.length()>0) {
							try {
								rcUri = new URI(rcUriString);
							} catch (URISyntaxException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								continue;
							}
						}

						NodeList settingEntryNodes = rcNode.getChildNodes();
						List<ICLanguageSettingEntry> settings = new ArrayList<ICLanguageSettingEntry>();
						for (int ientry=0;ientry<settingEntryNodes.getLength();ientry++) {
							Node settingEntryNode = settingEntryNodes.item(ientry);
							if(settingEntryNode.getNodeType() != Node.ELEMENT_NODE || ! ELEM_ENTRY.equals(settingEntryNode.getNodeName()))
								continue;
	
							ICLanguageSettingEntry entry = loadSettingEntry(settingEntryNode);
							if (entry!=null) {
								settings.add(entry);
							}
						}
						
						// set settings
						setSettingEntries(cfgId, rcUri, langId, settings);
					}
				}
			}
		}
	
	}

}
