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
import org.eclipse.core.resources.IResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class LanguageSettingsPersistentProvider extends LanguageSettingsBaseProvider {
	private static final String ELEM_PROVIDER = "provider";
	private static final String ATTR_ID = "id";

	private static final String ELEM_CONFIGURATION = "configuration";
	private static final String ELEM_LANGUAGE = "language";
	private static final String ELEM_RESOURCE = "resource";
	private static final String ATTR_URI = "uri";

	private static final String ELEM_SETTING_ENTRY = "settingEntry";
	private static final String ATTR_KIND = "kind";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_VALUE = "value";
	private static final String ATTR_FLAGS = "flags";
	
	
	private Map<String, // cfgDescriptionId
				Map<String, // languageId
					Map<URI, // resource URI
						List<ICLanguageSettingEntry>>>> fStorage = new HashMap<String, Map<String, Map<URI, List<ICLanguageSettingEntry>>>>();
	
	public LanguageSettingsPersistentProvider() {
		super();
	}
	
	public LanguageSettingsPersistentProvider(String id, String name) {
		super(id, name);
	}


	private void setSettingEntries(String cfgId, URI rcUri, String languageId, List<ICLanguageSettingEntry> entries) {
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
	}
	
	public void setSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId, List<ICLanguageSettingEntry> entries) {
		String cfgId = cfgDescription!=null ? cfgDescription.getId() : null;
		URI rcUri = rc!=null ? rc.getLocationURI() : null;
		if (entries==null) {
			entries = new ArrayList<ICLanguageSettingEntry>(0);
		}
		setSettingEntries(cfgId, rcUri, languageId, entries);
	}
	
	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription,
			IResource rc, String languageId) {
		
		List<ICLanguageSettingEntry> entries = super.getSettingEntries(cfgDescription, rc, languageId);
			
		String cfgId = cfgDescription!=null ? cfgDescription.getId() : null;
		Map<String, Map<URI, List<ICLanguageSettingEntry>>> cfgMap = fStorage.get(cfgId);
		if (cfgMap!=null) {
			Map<URI, List<ICLanguageSettingEntry>> langMap = cfgMap.get(languageId);
			if (langMap!=null) {
				URI rcUri = rc!=null ? rc.getLocationURI() : null;
				entries.addAll(langMap.get(rcUri));
			}
		}
		return entries;
	}

	private void serializeSettingEntries(Element parentElement, List<ICLanguageSettingEntry> settingEntries) {
		Document doc = parentElement.getOwnerDocument();
		for (ICLanguageSettingEntry entry : settingEntries) {
			Element elementSettingEntry = doc.createElement(ELEM_SETTING_ENTRY);
			elementSettingEntry.setAttribute(ATTR_KIND, LanguageSettingEntriesSerializer.kindToString(entry.getKind()));
			elementSettingEntry.setAttribute(ATTR_NAME, entry.getName());
			elementSettingEntry.setAttribute(ATTR_FLAGS, LanguageSettingEntriesSerializer.composeFlagsString(entry.getFlags()));
			switch(entry.getKind()) {
			case ICLanguageSettingEntry.MACRO:
				elementSettingEntry.setAttribute(ATTR_VALUE, entry.getValue());
				break;
//						case ICLanguageSettingEntry.LIBRARY_FILE:
			// TODO: sourceAttachment fields need to be covered
//							break;
			}
			parentElement.appendChild(elementSettingEntry);
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
	public void serialize(Element parentElement) {
		Document doc = parentElement.getOwnerDocument();
		
		Element elementProvider = doc.createElement(ELEM_PROVIDER);
		elementProvider.setAttribute(ATTR_ID, getId());
		parentElement.appendChild(elementProvider);
		
		for (Entry<String, Map<String, Map<URI, List<ICLanguageSettingEntry>>>> entryCfg : fStorage.entrySet()) {
			Element elementConfiguration = doc.createElement(ELEM_CONFIGURATION);
			String cfgId = entryCfg.getKey();
			if (cfgId==null) {
				cfgId = ""; //$NON-NLS-1$
			}
			elementConfiguration.setAttribute(ATTR_ID, cfgId);
			elementProvider.appendChild(elementConfiguration);
			for (Entry<String, Map<URI, List<ICLanguageSettingEntry>>> entryLang : entryCfg.getValue().entrySet()) {
				Element elementLanguage = doc.createElement(ELEM_LANGUAGE);
				String langId = entryLang.getKey();
				if (langId==null) {
					langId = ""; //$NON-NLS-1$
				}
				elementLanguage.setAttribute(ATTR_ID, langId);
				elementConfiguration.appendChild(elementLanguage);
				for (Entry<URI, List<ICLanguageSettingEntry>> entryRc : entryLang.getValue().entrySet()) {
					Element elementRc = doc.createElement(ELEM_RESOURCE);
					URI rcUri = entryRc.getKey();
					String rcUriString = rcUri!=null ? rcUri.toString() : ""; //$NON-NLS-1$
					elementRc.setAttribute(ATTR_URI, rcUriString);
					elementLanguage.appendChild(elementRc);
					
					serializeSettingEntries(elementRc, entryRc.getValue());
				}
			}
		}
	}

	/**
	 * @param node
	 * @return node value or {@code null}
	 */
	private static String determineNodeValue(Node node) {
		return node!=null ? node.getNodeValue() : null;
	}


	private ICLanguageSettingEntry loadSettingEntry(Node parentElement) {
		NamedNodeMap settingAttributes = parentElement.getAttributes();
		String settingKind = determineNodeValue(settingAttributes.getNamedItem(ATTR_KIND));
		String settingName = determineNodeValue(settingAttributes.getNamedItem(ATTR_NAME));
		String settingFlags = determineNodeValue(settingAttributes.getNamedItem(ATTR_FLAGS));

		ICLanguageSettingEntry entry = null;
		switch (LanguageSettingEntriesSerializer.stringToKind(settingKind)) {
		case ICSettingEntry.INCLUDE_PATH:
			entry = new CIncludePathEntry(settingName, LanguageSettingEntriesSerializer.composeFlags(settingFlags));
			break;
		case ICSettingEntry.INCLUDE_FILE:
			entry = new CIncludeFileEntry(settingName, LanguageSettingEntriesSerializer.composeFlags(settingFlags));
			break;
		case ICSettingEntry.MACRO:
			String settingValue = determineNodeValue(settingAttributes.getNamedItem(ATTR_VALUE));
			entry = new CMacroEntry(settingName, settingValue, LanguageSettingEntriesSerializer.composeFlags(settingFlags));
			break;
		case ICSettingEntry.MACRO_FILE:
			entry = new CMacroFileEntry(settingName, LanguageSettingEntriesSerializer.composeFlags(settingFlags));
			break;
		case ICSettingEntry.LIBRARY_PATH:
			entry = new CLibraryPathEntry(settingName, LanguageSettingEntriesSerializer.composeFlags(settingFlags));
			break;
		case ICSettingEntry.LIBRARY_FILE:
			entry = new CLibraryFileEntry(settingName, LanguageSettingEntriesSerializer.composeFlags(settingFlags));
			break;
		}
		return entry;
	}


	// provider/configuration/language/resource/settingEntry
	public void load(Element parentElement) {
		fStorage.clear();

		if (parentElement!=null) {
			NodeList providerNodes = parentElement.getElementsByTagName(ELEM_PROVIDER);
			
			for (int iprovider=0;iprovider<providerNodes.getLength();iprovider++) {
				Node providerNode = providerNodes.item(iprovider);
				if(providerNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				NamedNodeMap providerAttributes = providerNode.getAttributes();
				String providerId = determineNodeValue(providerAttributes.getNamedItem(ATTR_ID));
				if (!providerId.equals(this.getId())) {
					continue;
				}

				NodeList cfgNodes = providerNode.getChildNodes();
				for (int icfg=0;icfg<cfgNodes.getLength();icfg++) {
					Node cfgNode = cfgNodes.item(icfg);
					if(cfgNode.getNodeType() != Node.ELEMENT_NODE || ! ELEM_CONFIGURATION.equals(cfgNode.getNodeName()))
						continue;

					NamedNodeMap cfgAttributes = cfgNode.getAttributes();
					String cfgId = determineNodeValue(cfgAttributes.getNamedItem(ATTR_ID));
					if (cfgId.length()==0) {
						cfgId=null;
					}
					
					NodeList langNodes = cfgNode.getChildNodes();
					for (int ilang=0;ilang<langNodes.getLength();ilang++) {
						Node langNode = langNodes.item(ilang);
						if(langNode.getNodeType() != Node.ELEMENT_NODE || ! ELEM_LANGUAGE.equals(langNode.getNodeName()))
							continue;

						NamedNodeMap langAttributes = langNode.getAttributes();
						String langId = determineNodeValue(langAttributes.getNamedItem(ATTR_ID));
						if (langId.length()==0) {
							langId=null;
						}
						
						NodeList rcNodes = langNode.getChildNodes();
						for (int irc=0;irc<rcNodes.getLength();irc++) {
							Node rcNode = rcNodes.item(irc);
							if(rcNode.getNodeType() != Node.ELEMENT_NODE || ! ELEM_RESOURCE.equals(rcNode.getNodeName()))
								continue;
							
							NamedNodeMap rcAttributes = rcNode.getAttributes();
							String rcUriString = determineNodeValue(rcAttributes.getNamedItem(ATTR_URI));
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
								if(settingEntryNode.getNodeType() != Node.ELEMENT_NODE || ! ELEM_SETTING_ENTRY.equals(settingEntryNode.getNodeName()))
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





}
