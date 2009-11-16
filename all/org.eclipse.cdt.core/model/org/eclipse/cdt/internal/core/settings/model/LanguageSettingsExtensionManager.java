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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.LanguageSettingsPersistentProvider;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LanguageSettingsExtensionManager {
	private static final String STORAGE_WORKSPACE_LANGUAGE_SETTINGS = "language.settings.xml"; //$NON-NLS-1$
	private static final String PREFERENCE_PROVIDER_DEFAULT_IDS = "lang.settings.provider.default.ids"; //$NON-NLS-1$
	private static final String NONE = ""; //$NON-NLS-1$
	public static final char PROVIDER_DELIMITER = ';';
	/**
	 * Name of the extension point for contributing language settings
	 */
	public final static String PROVIDER_EXTENSION_ID = "LanguageSettingsProvider"; //$NON-NLS-1$
	private static final String ELEM_PROVIDER = "provider"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$

	private static final String ELEM_LANGUAGE = "language"; //$NON-NLS-1$

	private static final String ELEM_ENTRY = "entry"; //$NON-NLS-1$
	private static final String ELEM_FLAG = "flag"; //$NON-NLS-1$
	private static final String ATTR_KIND = "kind"; //$NON-NLS-1$
	private static final String ATTR_VALUE = "value"; //$NON-NLS-1$
	
	private static final String ELEM_LANGUAGE_SETTINGS = "languageSettings";


	private static final LinkedHashMap<String, ICLanguageSettingsProvider> fExtensionProviders = new LinkedHashMap<String, ICLanguageSettingsProvider>();
	private static final LinkedHashMap<String, ICLanguageSettingsProvider> fAvailableProviders = new LinkedHashMap<String, ICLanguageSettingsProvider>();
	private static LinkedHashMap<String, ICLanguageSettingsProvider> fUserDefinedProviders = null;
	private static List<String> fDefaultProviderIds = null;

	static {
//		loadUserDefinedProviders();
		loadDefaultProviderIds();
		loadProviderExtensions();
	}


	/**
	 * Load workspace default provider IDs to be used if no providers specified.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	synchronized public static void loadDefaultProviderIds() {
		fDefaultProviderIds = null;
		IEclipsePreferences preferences = new InstanceScope().getNode(CCorePlugin.PLUGIN_ID);
		String ids = preferences.get(PREFERENCE_PROVIDER_DEFAULT_IDS, NONE);
		if (ids.equals(NONE)) {
			return;
		}

		fDefaultProviderIds = Arrays.asList(ids.split(String.valueOf(PROVIDER_DELIMITER)));
	}


	/**
	 * Load provider contributed extensions.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	synchronized public static void loadProviderExtensions() {
		// sort by name - for the providers taken from platform extensions
		Set<ICLanguageSettingsProvider> sortedProviders = new TreeSet<ICLanguageSettingsProvider>(
				new Comparator<ICLanguageSettingsProvider>() {
					public int compare(ICLanguageSettingsProvider contr1, ICLanguageSettingsProvider contr2) {
						return contr1.getName().compareTo(contr2.getName());
					}
				}
		);

		loadProviderExtensions(Platform.getExtensionRegistry(), sortedProviders);

		fExtensionProviders.clear();
		for (ICLanguageSettingsProvider provider : sortedProviders) {
			fExtensionProviders.put(provider.getId(), provider);
		}

		recalculateAvailableProviders();
	}

	/**
	 * Populate the list of available providers where workspace level user defined parsers
	 * overwrite contributed through provider extension point.
	 */
	private static void recalculateAvailableProviders() {
		fAvailableProviders.clear();
		if (fUserDefinedProviders!=null) {
			fAvailableProviders.putAll(fUserDefinedProviders);
		}
		for (ICLanguageSettingsProvider provider : fExtensionProviders.values()) {
			String id = provider.getId();
			if (!fAvailableProviders.containsKey(id)) {
				fAvailableProviders.put(id, provider);
			}
		}
	}


	/**
	 * Load contributed extensions from extension registry.
	 *
	 * @param registry - extension registry
	 * @param providers - resulting set of providers
	 */
	private static void loadProviderExtensions(IExtensionRegistry registry, Set<ICLanguageSettingsProvider> providers) {
		providers.clear();
		IExtensionPoint extension = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID, PROVIDER_EXTENSION_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				try {
					String extensionID = ext.getUniqueIdentifier();
					String extensionName = ext.getLabel();
					ICLanguageSettingsProvider provider = null;
					for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
						if (cfgEl.getName().equals(ELEM_PROVIDER)) {
							provider = createExecutableProvider(cfgEl);
							providers.add(provider);
						}
					}
				} catch (Exception e) {
					CCorePlugin.log("Cannot load LanguageSettingsProvider extension " + ext.getUniqueIdentifier(), e); //$NON-NLS-1$
				}
			}
		}
	}

	private static String determineAttributeValue(IConfigurationElement ce, String attr) {
		String value = ce.getAttribute(attr);
		return value!=null ? value : ""; //$NON-NLS-1$
	}

	/**
	 * Creates empty non-configured provider as executable extension from extension point definition.
	 * If "class" attribute is empty TODO is created.
	 *
	 * @param initialId - nominal ID of provider
	 * @param initialName - nominal name of provider
	 * @param ce - configuration element with provider definition
	 * @return new non-configured provider
	 * @throws CoreException in case of failure
	 */
	private static ICLanguageSettingsProvider createExecutableProvider(IConfigurationElement ce) throws CoreException {
		String ceClass = ce.getAttribute(ATTR_CLASS);
		String ceId = determineAttributeValue(ce, ATTR_ID);
		String ceName = determineAttributeValue(ce, ATTR_NAME);
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

		ICLanguageSettingsProvider provider = null;
		if (ceClass!=null && !ceClass.equals(LanguageSettingsBaseProvider.class.getCanonicalName())) {
			Object base = ce.createExecutableExtension(ATTR_CLASS);
			if (base instanceof AbstractExecutableExtensionBase) {
				((AbstractExecutableExtensionBase) base).setId(ceId);
				((AbstractExecutableExtensionBase) base).setName(ceName);
			}
			provider = (ICLanguageSettingsProvider)base;
		}
		if (provider==null) {
			provider = new LanguageSettingsBaseProvider(ceId, ceName, languages, entries);
		}
		return provider;
	}

	/**
	 * Creates empty non-configured provider from extension point definition looking at "class" attribute.
	 * ID and name of provider are assigned from first extension point encountered.
	 *
	 * @param className - full qualified class name of provider.
	 * @param registry - extension registry
	 * @return new non-configured provider
	 */
	private static ICLanguageSettingsProvider createProviderCarcass(String className, IExtensionRegistry registry) {
		if (className==null || className.length()==0 /*|| className.equals(RegexProvider.class.getName())*/) {
//			return new LanguageSettingsCoreProvider();
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
						if (cfgEl.getName().equals(ELEM_PROVIDER) && className.equals(cfgEl.getAttribute(ATTR_CLASS))) {
							return createExecutableProvider(cfgEl);
						}
					}
				}
			}
		} catch (Exception e) {
			CCorePlugin.log("Error creating language settings provider.", e); //$NON-NLS-1$
		}
		return null;
	}


	/**
	 * FIXME - to clone
	 * @param id - ID of provider
	 * @return cloned copy of provider. Note that {@link ProviderNamedWrapper} returns
	 * shallow copy with the same instance of underlying provider.
	 */
	public static ICLanguageSettingsProvider getProvider(String id) {
		ICLanguageSettingsProvider provider = fAvailableProviders.get(id);

//		try {
//			if (provider instanceof RegexProvider) {
//				return (RegexProvider) ((RegexProvider)provider).clone();
//			} else if (provider instanceof ProviderNamedWrapper) {
//				return (ProviderNamedWrapper) ((ProviderNamedWrapper)provider).clone();
//			}
//		} catch (CloneNotSupportedException e) {
//			CCorePlugin.log(e);
//		}
		return provider;
	}

	/**
	 * @param ids - array of provider IDs
	 * @return provider IDs delimited with provider delimiter ";"
	 * @since 5.2
	 */
	public static String toDelimitedString(String[] ids) {
		String result=""; //$NON-NLS-1$
		for (String id : ids) {
			if (result.length()==0) {
				result = id;
			} else {
				result += PROVIDER_DELIMITER + id;
			}
		}
		return result;
	}

	/**
	 * Save the list of default providers in preferences.
	 *
	 * @throws BackingStoreException in case of problem storing
	 */
	public static void serializeDefaultProviderIds() throws BackingStoreException {
		IEclipsePreferences preferences = new InstanceScope().getNode(CCorePlugin.PLUGIN_ID);
		String ids = NONE;
		if (fDefaultProviderIds!=null) {
			ids = toDelimitedString(fDefaultProviderIds.toArray(new String[0]));
		}

		preferences.put(PREFERENCE_PROVIDER_DEFAULT_IDS, ids);
		preferences.flush();
	}

	/**
	 * Set and store in workspace area user defined providers.
	 *
	 * @param providers - array of user defined providers
	 * @throws CoreException in case of problems
	 */
	public static void setUserDefinedProviders(ICLanguageSettingsProvider[] providers) throws CoreException {
		setUserDefinedProvidersInternal(providers);
//		serializeUserDefinedProviders();
	}

	/**
	 * Internal method to set user defined providers in memory.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * Use {@link #setUserDefinedProviders(ICLanguageSettingsProvider[])}.
	 *
	 * @param providers - array of user defined providers. If {@code null}
	 * is passed user defined providers are cleared.
	 */
	public static void setUserDefinedProvidersInternal(ICLanguageSettingsProvider[] providers) {
		if (providers==null) {
			fUserDefinedProviders = null;
		} else {
			fUserDefinedProviders= new LinkedHashMap<String, ICLanguageSettingsProvider>();
			// set customized list
			for (ICLanguageSettingsProvider provider : providers) {
				fUserDefinedProviders.put(provider.getId(), provider);
			}
		}
		recalculateAvailableProviders();
	}

	/**
	 * @return available providers IDs which include contributed through extension + user defined ones
	 * from workspace
	 */
	public static String[] getProviderAvailableIds() {
		return fAvailableProviders.keySet().toArray(new String[0]);
	}

	/**
	 * @return IDs of language settings providers of LanguageSettingProvider extension point.
	 */
	public static String[] getProviderExtensionIds() {
		return fExtensionProviders.keySet().toArray(new String[0]);
	}


	/**
	 * Set and store default providers IDs to be used if provider list is empty.
	 *
	 * @param ids - default providers IDs
	 * @throws BackingStoreException in case of problem with storing
	 */
	public static void setDefaultProviderIds(String[] ids) throws BackingStoreException {
		setDefaultProviderIdsInternal(ids);
//		serializeDefaultProviderIds();
	}

	/**
	 * Set default providers IDs in internal list.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * Use {@link #setDefaultProviderIds(String[])}.
	 *
	 * @param ids - default providers IDs
	 */
	public static void setDefaultProviderIdsInternal(String[] ids) {
		if (ids==null) {
			fDefaultProviderIds = null;
		} else {
			fDefaultProviderIds = new ArrayList<String>(Arrays.asList(ids));
		}
	}

	/**
	 * @return default providers IDs to be used if provider list is empty.
	 */
	public static String[] getDefaultProviderIds() {
		if (fDefaultProviderIds==null) {
			return fAvailableProviders.keySet().toArray(new String[0]);
		}
		return fDefaultProviderIds.toArray(new String[0]);
	}


	/**
	 * TODO: refactor with ErrorParserManager
	 * 
	 * @param store - name of the store
	 * @return location of the store in the plug-in state area
	 */
	private static IPath getStoreLocation(String store) {
		return CCorePlugin.getDefault().getStateLocation().append(store);
	}

	/**
	 * TODO: refactor with ErrorParserManager
	 * Serialize XML Document in a file.
	 *
	 * @param doc - XML to serialize
	 * @param location - location of the file
	 * @throws IOException in case of problems with file I/O
	 * @throws TransformerException in case of problems with XML output
	 */
	synchronized private static void serializeXml(Document doc, IPath location) throws IOException, TransformerException {
		XmlUtil.prettyFormat(doc);

		java.io.File storeFile = location.toFile();
		if (!storeFile.exists()) {
			storeFile.createNewFile();
		}
		OutputStream fileStream = new FileOutputStream(storeFile);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");	//$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");	//$NON-NLS-1$

		XmlUtil.prettyFormat(doc);
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new FileOutputStream(storeFile));
		transformer.transform(source, result);

		fileStream.close();
	}

	// TODO: reconcile the name with ErrorParserExtensionManager
	public static void serializeLanguageSettings() throws CoreException {
		IPath fileLocation = getStoreLocation(STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
		try {
			if (fUserDefinedProviders==null) {
				fileLocation.toFile().delete();
				return;
			}
			
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			Element rootElement = doc.createElement(ELEM_LANGUAGE_SETTINGS);
			doc.appendChild(rootElement);

			for (ICLanguageSettingsProvider provider : fUserDefinedProviders.values()) {
				if (provider instanceof LanguageSettingsPersistentProvider) {
					((LanguageSettingsPersistentProvider) provider).serialize(rootElement);
				}
			}
			serializeXml(doc, fileLocation);

		} catch (Exception e) {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.getResourceString("Internal error while trying to serialize language settings"), e);
			throw new CoreException(s);
		}
	}

	private static Document loadXML(java.io.File xmlFile) throws CoreException {
		try {
			InputStream xmlStream = new FileInputStream(xmlFile);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(xmlStream);
		} catch (Exception e) {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.getResourceString("Internal error while trying to load language settings"), e);
			throw new CoreException(s);
		}
	}

	public static void loadLanguageSettings() throws CoreException {
		IPath fileLocation = getStoreLocation(STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
		java.io.File file = fileLocation.toFile();
		if (!file.exists()) {
			return;
		}

		Document doc = null;

		try {
			doc = loadXML(file);
		} catch (Exception e) {
			CCorePlugin.log("Can't load preferences from file "+file.getPath(), e); //$NON-NLS-1$
		}

		if (doc!=null) {
			Element rootElement = doc.getDocumentElement();
			for (ICLanguageSettingsProvider provider : fUserDefinedProviders.values()) {
				if (provider instanceof LanguageSettingsPersistentProvider) {
					((LanguageSettingsPersistentProvider) provider).load(rootElement);
				}
			}
		}
	}


}
