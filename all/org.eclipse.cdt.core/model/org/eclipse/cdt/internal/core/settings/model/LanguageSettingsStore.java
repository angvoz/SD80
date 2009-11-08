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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsResourceDescriptor;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * LanguageSettingsStore serves for storing lists of language settings ICLanguageSettingEntry
 * (org.eclipse.cdt.core analog for compiler options). It is represented internally as TODO .
 *
 * Each provider can keep heterogeneous ordered list of settings where there is no distinction
 * between different kinds such as include paths or macros made. Entries with duplicate names
 * are also allowed.
 *
 */
public class LanguageSettingsStore {
	private static final String ROOT_ELEM = "languageSettings";
	private static final String ATTR_PROJECT_NAME = "projectName";

	private static final String ELEM_PROVIDER = "provider";
	private static final String ATTR_ID = "id";


	private static final String ELEM_RESOURCE_DESCRIPTOR = "descriptor";
	private static final String ATTR_CONFIGURATION = "configuration";
	private static final String ATTR_LANGUAGE = "language";
	private static final String ATTR_PROJECT_PATH = "projectPath";

	private static final String ELEM_SETTING_ENTRY = "settingEntry";
	private static final String ATTR_KIND = "kind";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_VALUE = "value";
	private static final String ATTR_FLAGS = "flags";

	// TODO
	private static final String ATTR_PATH = "path";
	private static final String ATTR_URI = "uri";

	private static final String ATTR_BUILTIN = "builtIn";
	private static final String ATTR_ENABLEMENT = "enablement";

	// store Map<ProviderId, Map<Resource, List<SettingEntry>>>
	private Map<String, Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>>>
		fStorage = new HashMap<String, Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>>>();

	private IFile file;

	public LanguageSettingsStore(IFile file) {
		this.file = file;
	}

	/**
	 * Get copy of the list
	 */
	public List<ICLanguageSettingEntry> getSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId) {
		Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>> map = fStorage.get(providerId);
		if (map!=null) {
			List<ICLanguageSettingEntry> list = map.get(descriptor);
			if (list!=null) {
				return new ArrayList<ICLanguageSettingEntry>(list);
			}
		}

		IResource parent = descriptor.getResource();
		if (parent!=null) {
			LanguageSettingsResourceDescriptor parentDescriptor = new LanguageSettingsResourceDescriptor(parent,
					descriptor.getLangId());

			return getSettingEntries(parentDescriptor, providerId);
		}
		return new ArrayList<ICLanguageSettingEntry>();
	}


	/**
	 * This will replace old settings if existed with the new ones.
	 */
	public void setSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId, List<ICLanguageSettingEntry> settings) {
		Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>> map = fStorage.get(providerId);
		if (map==null) {
			map = new HashMap<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>>();
			fStorage.put(providerId, map);
		}
		map.put(descriptor, new ArrayList<ICLanguageSettingEntry>(settings));
	}

	/**
	 * This will add new settings to the end.
	 */
	public void addSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId, List<ICLanguageSettingEntry> settings) {
		Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>> map = fStorage.get(providerId);
		if (map==null) {
			map = new HashMap<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>>();
			fStorage.put(providerId, map);
		}
		List<ICLanguageSettingEntry> list = map.get(descriptor);
		if (list==null) {
			list = new ArrayList<ICLanguageSettingEntry>(settings);
		} else {
			list.addAll(settings);
		}
		map.put(descriptor, list);
	}

	/**
	 * Note that {@code removeSettingEntries} will remove empty descriptor from the list
	 * when no more entries are left meaning the settings are derived from parent folder.
	 * Use {@link #setSettingEntries(LanguageSettingsResourceDescriptor, String, List)}
	 * if empty list is desired.
	 *
	 * @param descriptor TODO
	 * @param providerId TODO
	 */
	public void removeSettingEntries(LanguageSettingsResourceDescriptor descriptor, String providerId) {
		Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>> map = fStorage.get(providerId);
		if (map!=null) {
			map.remove(descriptor);
		}
		if (map==null || map.size()==0) {
			fStorage.remove(providerId);
		}
	}

	public void clear() {
		fStorage.clear();
	}

	public List<String> getProviders() {
		return new ArrayList<String>(fStorage.keySet());
	}

	public List<LanguageSettingsResourceDescriptor> getDescriptors(String providerId) {
		Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>> map = fStorage.get(providerId);
		if (map!=null) {
			return new ArrayList<LanguageSettingsResourceDescriptor>(map.keySet());
		}
		return new ArrayList<LanguageSettingsResourceDescriptor>();
	}



	private Document toXML() throws CoreException {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			Element rootElement = doc.createElement(ROOT_ELEM);
			rootElement.setAttribute(ATTR_PROJECT_NAME, file.getProject().getName());
			doc.appendChild(rootElement);

			// Map<ProviderId, Map<Resource, List<SettingEntry>>>
			for (Entry<String, Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>>>
					providerMapEntry : fStorage.entrySet()) {
				String providerID = providerMapEntry.getKey();
				Map<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>> rcMap = providerMapEntry.getValue();

				Element elementProvider = doc.createElement(ELEM_PROVIDER);
				elementProvider.setAttribute(ATTR_ID, providerID);
				rootElement.appendChild(elementProvider);

				// Map<Resource, List<SettingEntry>>
				for (Entry<LanguageSettingsResourceDescriptor, List<ICLanguageSettingEntry>> rcMapEntry : rcMap.entrySet()) {
					LanguageSettingsResourceDescriptor rcDescriptor = rcMapEntry.getKey();
					List<ICLanguageSettingEntry> settingEntries = rcMapEntry.getValue();

					Element elementResourceDescriptor = doc.createElement(ELEM_RESOURCE_DESCRIPTOR);
					elementResourceDescriptor.setAttribute(ATTR_LANGUAGE, rcDescriptor.getLangId());
					elementResourceDescriptor.setAttribute(ATTR_PROJECT_PATH, rcDescriptor.getResource().getProjectRelativePath().toString());
					elementProvider.appendChild(elementResourceDescriptor);

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
						elementResourceDescriptor.appendChild(elementSettingEntry);
					}
				}
			}




			return doc;
		} catch (Exception e) {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.getResourceString("Internal error while trying to serialize language settings"), e);
			throw new CoreException(s);
		}
	}

	private static byte[] toByteArray(Document doc) throws CoreException {
		XmlUtil.prettyFormat(doc);

		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(stream);
			transformer.transform(source, result);

			return stream.toByteArray();
		} catch (Exception e) {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.getResourceString("Internal error while trying to serialize language settings"), e);
			throw new CoreException(s);
		}
	}


	public void serialize() throws CoreException {
		InputStream input = new ByteArrayInputStream(toByteArray(toXML()));
		file.create(input, IResource.FORCE, null);
	}


	private static Document loadXML(IFile xmlFile) throws CoreException {
		try {
			InputStream xmlStream = xmlFile.getContents();
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(xmlStream);
		} catch (Exception e) {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.getResourceString("Internal error while trying to load language settings"), e);
			throw new CoreException(s);
		}
	}

	/**
	 * @param node
	 * @return node value or {@code null}
	 */
	private static String determineNodeValue(Node node) {
		return node!=null ? node.getNodeValue() : null;
	}


	public void load() {
		if (!file.exists()) {
			return;
		}

		Document doc = null;

		try {
			doc = loadXML(file);
		} catch (Exception e) {
			CCorePlugin.log("Can't load preferences from file "+file.getLocation(), e); //$NON-NLS-1$
		}

		if (doc!=null) {
			NodeList providerNodes = doc.getElementsByTagName(ELEM_PROVIDER);
			for (int iprovider=0;iprovider<providerNodes.getLength();iprovider++) {
				Node providerNode = providerNodes.item(iprovider);
				if(providerNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				NamedNodeMap providerAttributes = providerNode.getAttributes();
				String providerId = determineNodeValue(providerAttributes.getNamedItem(ATTR_ID));

				NodeList descriptorNodes = providerNode.getChildNodes();
				for (int idescriptor=0;idescriptor<descriptorNodes.getLength();idescriptor++) {
					Node descriptorNode = descriptorNodes.item(idescriptor);
					if(descriptorNode.getNodeType() != Node.ELEMENT_NODE || ! ELEM_RESOURCE_DESCRIPTOR.equals(descriptorNode.getNodeName()))
						continue;

					NamedNodeMap descriptorAttributes = descriptorNode.getAttributes();
					String projectPath = determineNodeValue(descriptorAttributes.getNamedItem(ATTR_PROJECT_PATH));
					String languageId = determineNodeValue(descriptorAttributes.getNamedItem(ATTR_LANGUAGE));

					IProject project = file.getProject();
					IResource rc = project.findMember(projectPath);
					if (rc==null) {
						continue;
					}

					LanguageSettingsResourceDescriptor descriptor = new LanguageSettingsResourceDescriptor(
							rc, languageId);

					List<ICLanguageSettingEntry> settings = new ArrayList<ICLanguageSettingEntry>();
					NodeList settingEntryNodes = descriptorNode.getChildNodes();
					for (int ientry=0;ientry<settingEntryNodes.getLength();ientry++) {
						Node settingEntryNode = settingEntryNodes.item(ientry);
						if(settingEntryNode.getNodeType() != Node.ELEMENT_NODE || ! ELEM_SETTING_ENTRY.equals(settingEntryNode.getNodeName()))
							continue;

						NamedNodeMap settingAttributes = settingEntryNode.getAttributes();
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
						if (entry!=null) {
							settings.add(entry);
						}
					}
					if (settings.size()!=0) {
						setSettingEntries(descriptor, providerId, settings);
					}
				}
			}
		}


	}


}
