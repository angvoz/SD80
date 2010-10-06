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

package org.eclipse.cdt.internal.core.language.settings.providers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.settings.model.LanguageSettingsSerializable;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LanguageSettingsExtensionManager_TBD {
	private static final String STORAGE_WORKSPACE_LANGUAGE_SETTINGS = "language.settings.xml"; //$NON-NLS-1$
	private static final String SETTINGS_FOLDER_NAME = ".settings/"; //$NON-NLS-1$
	private static final String STORAGE_PROJECT_LANGUAGE_SETTINGS = "language.settings.xml"; //$NON-NLS-1$
	private static final String PREFERENCE_PROVIDER_DEFAULT_IDS = "lang.settings.provider.default.ids"; //$NON-NLS-1$
	private static final String NONE = ""; //$NON-NLS-1$
	public static final char PROVIDER_DELIMITER = ';';
	/**
	 * Name of the extension point for contributing language settings
	 */
	public final static String PROVIDER_EXTENSION_FULL_ID = "org.eclipse.cdt.core.LanguageSettingsProvider"; //$NON-NLS-1$
	public final static String PROVIDER_EXTENSION_SIMPLE_ID = "LanguageSettingsProvider"; //$NON-NLS-1$
	private static final String MBS_LANGUAGE_SETTINGS_PROVIDER = "org.eclipse.cdt.managedbuilder.core.LanguageSettingsProvider";

	private static final String ELEM_PLUGIN = "plugin"; //$NON-NLS-1$
	private static final String ELEM_EXTENSION = "extension"; //$NON-NLS-1$
	private static final String ATTR_POINT = "point"; //$NON-NLS-1$

	private static final String ELEM_PROJECT = "project"; //$NON-NLS-1$
	private static final String ELEM_CONFIGURATION = "configuration"; //$NON-NLS-1$

	private static final String ELEM_PROVIDER = "provider"; //$NON-NLS-1$
	private static final String ELEM_PROVIDER_REFERENCE = "provider-reference"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_PARAMETER = "parameter"; //$NON-NLS-1$

	private static final String ELEM_LANGUAGE_SCOPE = "language-scope"; //$NON-NLS-1$

	private static final String ELEM_ENTRY = "entry"; //$NON-NLS-1$
	private static final String ELEM_FLAG = "flag"; //$NON-NLS-1$
	private static final String ATTR_KIND = "kind"; //$NON-NLS-1$
	private static final String ATTR_VALUE = "value"; //$NON-NLS-1$



	private static final LinkedHashMap<String, ILanguageSettingsProvider> fExtensionProviders = new LinkedHashMap<String, ILanguageSettingsProvider>();
	private static final LinkedHashMap<String, ILanguageSettingsProvider> fAvailableProviders = new LinkedHashMap<String, ILanguageSettingsProvider>();
	private static LinkedHashMap<String, ILanguageSettingsProvider> fUserDefinedProviders = null;
	private static List<String> fDefaultProviderIds = null;

	private static Object serializingLock = new Object();

	static {
		loadDefaultProviderIds();
		loadProviderExtensions();
		try {
			loadLanguageSettingsWorkspace();
		} catch (CoreException e) {
			CCorePlugin.log("Error loading workspace language settings providers", e); //$NON-NLS-1$
		}
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
		Set<ILanguageSettingsProvider> sortedProviders = new TreeSet<ILanguageSettingsProvider>(
				new Comparator<ILanguageSettingsProvider>() {
					public int compare(ILanguageSettingsProvider contr1, ILanguageSettingsProvider contr2) {
						return contr1.getName().compareTo(contr2.getName());
					}
				}
		);

		loadProviderExtensions(Platform.getExtensionRegistry(), sortedProviders);

		fExtensionProviders.clear();
		for (ILanguageSettingsProvider provider : sortedProviders) {
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
		for (ILanguageSettingsProvider provider : fExtensionProviders.values()) {
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
	private static void loadProviderExtensions(IExtensionRegistry registry, Set<ILanguageSettingsProvider> providers) {
		providers.clear();
		IExtensionPoint extension = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID, PROVIDER_EXTENSION_SIMPLE_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				try {
					String extensionID = ext.getUniqueIdentifier();
					String extensionName = ext.getLabel();
					ILanguageSettingsProvider provider = null;
					for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
						if (cfgEl.getName().equals(ELEM_PROVIDER)) {
							provider = createExecutableExtension(cfgEl);
							configureExecutableProvider(provider, cfgEl);
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
		// FIXME: should return null if no value?
		return value!=null ? value : ""; //$NON-NLS-1$
	}

	/**
	 * Creates empty non-configured provider as executable extension from extension point definition.
	 * If "class" attribute is empty {@link LanguageSettingsBaseProvider} is created.
	 *
	 * @param ce - configuration element with provider definition
	 * @return new non-configured provider
	 * @throws CoreException in case of failure
	 */
	private static ILanguageSettingsProvider createExecutableExtension(IConfigurationElement ce) throws CoreException {
		String ceClass = ce.getAttribute(ATTR_CLASS);
		ILanguageSettingsProvider provider = null;
		if (ceClass==null || ceClass.trim().length()==0 || ceClass.equals(LanguageSettingsBaseProvider.class.getCanonicalName())) {
			provider = new LanguageSettingsBaseProvider();
		} else {
			provider = (ILanguageSettingsProvider)ce.createExecutableExtension(ATTR_CLASS);
		}

		return provider;
	}


	private static void configureExecutableProvider(ILanguageSettingsProvider provider,
			IConfigurationElement ce) {
		String ceId = determineAttributeValue(ce, ATTR_ID);
		String ceName = determineAttributeValue(ce, ATTR_NAME);
		String ceParameter = determineAttributeValue(ce, ATTR_PARAMETER);
		List<String> languages = null;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();

		for (IConfigurationElement ceLang : ce.getChildren(ELEM_LANGUAGE_SCOPE)) {
			String langId = determineAttributeValue(ceLang, ATTR_ID);
			if (langId.trim().length()>0) {
				if (languages==null) {
					languages = new ArrayList<String>();
				}
				languages.add(langId);
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

		if (provider instanceof LanguageSettingsBaseProvider) {
			((LanguageSettingsBaseProvider) provider).configureProvider(ceId, ceName, languages, entries, ceParameter);
		} else if (provider instanceof AbstractExecutableExtensionBase) {
			((AbstractExecutableExtensionBase) provider).setId(ceId);
			((AbstractExecutableExtensionBase) provider).setName(ceName);
		}
	}

	/**
	 * Creates empty non-configured provider from extension point definition looking at "class" attribute.
	 * ID and name of provider are assigned from first extension point encountered.
	 *
	 * @param className - full qualified class name of provider.
	 * @param registry - extension registry
	 * @return new non-configured provider
	 */
	private static ILanguageSettingsProvider createProviderCarcass(String className, IExtensionRegistry registry) {
		if (className==null || className.length()==0) {
			return new LanguageSettingsBaseProvider();
		}
		if (className.equals(LanguageSettingsSerializable.class.getName())) {
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable();
			return provider;
		}

		try {
			IExtensionPoint extension = registry.getExtensionPoint(CCorePlugin.PLUGIN_ID, PROVIDER_EXTENSION_SIMPLE_ID);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension ext : extensions) {
					@SuppressWarnings("unused")
					String extensionID = ext.getUniqueIdentifier();
					for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
						if (cfgEl.getName().equals(ELEM_PROVIDER) && className.equals(cfgEl.getAttribute(ATTR_CLASS))) {
							ILanguageSettingsProvider provider = createExecutableExtension(cfgEl);
							if (provider instanceof AbstractExecutableExtensionBase) {
								String ceId = determineAttributeValue(cfgEl, ATTR_ID);
								String ceName = determineAttributeValue(cfgEl, ATTR_NAME);
								((AbstractExecutableExtensionBase) provider).setId(ceId);
								((AbstractExecutableExtensionBase) provider).setName(ceName);
							}
							return provider;
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
	 * Get Language Settings Provider defined in the workspace. That includes user-defined
	 * providers and after that providers defined as extensions via
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} extension point.
	 * That returns actual object, any modifications will affect any configuration
	 * referring to the provider.
	 *
	 * @param id - ID of provider to find.
	 * @return the provider or {@code null} if provider is not defined.
	 */
	public static ILanguageSettingsProvider getWorkspaceProvider(String id) {
		ILanguageSettingsProvider provider = fAvailableProviders.get(id);
		return provider;
	}

	/**
	 * Checks if the provider is defined on the workspace level. See {@link #getWorkspaceProvider(String)}.
	 *
	 * @param provider - provider to check.
	 * @return {@code true} if the given provider is workspace provider, {@code false} otherwise.
	 */
	public static boolean isWorkspaceProvider(ILanguageSettingsProvider provider) {
		return provider==getWorkspaceProvider(provider.getId());
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
	public static void setUserDefinedProviders(ILanguageSettingsProvider[] providers) throws CoreException {
		setUserDefinedProvidersInternal(providers);
//		serializeUserDefinedProviders();
	}

	/**
	 * Internal method to set user defined providers in memory.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * Use {@link #setUserDefinedProviders(ILanguageSettingsProvider[])}.
	 *
	 * @param providers - array of user defined providers. If {@code null}
	 * is passed user defined providers are cleared.
	 */
	public static void setUserDefinedProvidersInternal(ILanguageSettingsProvider[] providers) {
		if (providers==null) {
			fUserDefinedProviders = null;
		} else {
			fUserDefinedProviders= new LinkedHashMap<String, ILanguageSettingsProvider>();
			// set customized list
			for (ILanguageSettingsProvider provider : providers) {
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
	private static URI getStoreLocation(String store) {
		IPath location = CCorePlugin.getDefault().getStateLocation().append(store);
		URI uri = URIUtil.toURI(location);
		return uri;
	}

	public static void serializeLanguageSettingsWorkspace() throws CoreException {
		URI uriLocation = getStoreLocation(STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
		List<LanguageSettingsSerializable> serializableExtensionProviders = new ArrayList<LanguageSettingsSerializable>();
		for (ILanguageSettingsProvider provider : fExtensionProviders.values()) {
			if (provider instanceof LanguageSettingsSerializable) {
				// serialize only modified ones
				LanguageSettingsSerializable ser = (LanguageSettingsSerializable)provider;
				if (!ser.isEmpty()) {
					serializableExtensionProviders.add(ser);
				}
			}
		}
		if (fUserDefinedProviders!=null) {
			for (ILanguageSettingsProvider provider : fUserDefinedProviders.values()) {
				// serialize all user defined providers
				if (provider instanceof LanguageSettingsSerializable) {
					LanguageSettingsSerializable ser = (LanguageSettingsSerializable)provider;
					serializableExtensionProviders.add(ser);
				}
			}
		}
		try {
			if (serializableExtensionProviders.isEmpty()) {
				java.io.File file = new java.io.File(uriLocation);
				synchronized (serializingLock) {
					file.delete();
				}
				return;
			}

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_PLUGIN);
			Element elementExtension = XmlUtil.appendElement(rootElement, ELEM_EXTENSION, new String[] {ATTR_POINT, PROVIDER_EXTENSION_FULL_ID});

			for (LanguageSettingsSerializable provider : serializableExtensionProviders) {
				provider.serialize(elementExtension);
			}

			synchronized (serializingLock) {
				XmlUtil.serializeXml(doc, uriLocation);
			}

		} catch (Exception e) {
			CCorePlugin.log("Internal error while trying to serialize language settings", e); //$NON-NLS-1$
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Internal error while trying to serialize language settings", e);
			throw new CoreException(s);
		}
	}


	public static void loadLanguageSettingsWorkspace() throws CoreException {
		URI uriLocation = getStoreLocation(STORAGE_WORKSPACE_LANGUAGE_SETTINGS);

		Document doc = null;
		try {
			synchronized (serializingLock) {
				doc = XmlUtil.loadXML(uriLocation);
			}
		} catch (Exception e) {
			CCorePlugin.log("Can't load preferences from file "+uriLocation, e); //$NON-NLS-1$
		}

		if (doc!=null) {
			Element rootElement = doc.getDocumentElement();
			NodeList providerNodes = rootElement.getElementsByTagName(LanguageSettingsSerializable.ELEM_PROVIDER);

			for (int i=0;i<providerNodes.getLength();i++) {
				Element providerNode = (Element)providerNodes.item(i);
				String providerId = XmlUtil.determineAttributeValue(providerNode, ATTR_ID);
				if (providerId!=null) {
					ILanguageSettingsProvider provider = null;
					if (fUserDefinedProviders!=null) {
						provider = fUserDefinedProviders.get(providerId);
					}
					if (provider==null) {
						provider = getWorkspaceProvider(providerId);
					}
					if (provider instanceof LanguageSettingsSerializable) {
						((LanguageSettingsSerializable)provider).load(providerNode);
					}
				}
			}
		}
	}

	public static void serializeLanguageSettings(Element parentElement, ICProjectDescription prjDescription) throws CoreException {
		ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
		for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
			Element elementConfiguration = XmlUtil.appendElement(parentElement, ELEM_CONFIGURATION, new String[] {
					ATTR_ID, cfgDescription.getId(),
					ATTR_NAME, cfgDescription.getName(),
				});
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			if (providers.size()>0) {
				Element elementExtension = XmlUtil.appendElement(elementConfiguration, ELEM_EXTENSION, new String[] {
						ATTR_POINT, PROVIDER_EXTENSION_FULL_ID});
				for (ILanguageSettingsProvider provider : providers) {
					if (isWorkspaceProvider(provider)) {
						// Element elementProviderReference =
						XmlUtil.appendElement(elementExtension, ELEM_PROVIDER_REFERENCE, new String[] {
								ATTR_ID, provider.getId()});
						continue;
					}
					if (provider instanceof LanguageSettingsSerializable) {
						((LanguageSettingsSerializable) provider).serialize(elementExtension);
					} else {
						// Element elementProvider =
						XmlUtil.appendElement(elementExtension, ELEM_PROVIDER, new String[] {
								ATTR_ID, provider.getId(),
								ATTR_NAME, provider.getName(),
								ATTR_CLASS, provider.getClass().getCanonicalName(),
							});
					}
				}
			}
		}
	}

	private static IFile getStorage(IProject project) throws CoreException {
		IFolder folder = project.getFolder(SETTINGS_FOLDER_NAME);
		if (!folder.exists()) {
			folder.create(true, true, null);
		}
		IFile storage = folder.getFile(STORAGE_PROJECT_LANGUAGE_SETTINGS);
		return storage;
	}

	public static void serializeLanguageSettings(ICProjectDescription prjDescription) throws CoreException {
		IProject project = prjDescription.getProject();
		try {
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_PROJECT);
			serializeLanguageSettings(rootElement, prjDescription);

			IFile file = getStorage(project);
			synchronized (serializingLock){
				XmlUtil.serializeXml(doc, file);
			}

		} catch (Exception e) {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.getResourceString("Internal error while trying to serialize language settings"), e);
			CCorePlugin.log(s);
			throw new CoreException(s);
		}
	}

	public static void loadLanguageSettings(Element parentElement, ICProjectDescription prjDescription) {
		/*
		<project>
			<configuration id="cfg.id">
				<extension point="org.eclipse.cdt.core.LanguageSettingsProvider">
					<provider .../>
					<provider-reference id="..."/>
				</extension>
			</configuration>
		</project>
		 */
		NodeList configurationNodes = parentElement.getChildNodes();
		for (int ic=0;ic<configurationNodes.getLength();ic++) {
			Node cfgNode = configurationNodes.item(ic);
			if (!(cfgNode instanceof Element && cfgNode.getNodeName().equals(ELEM_CONFIGURATION)) )
				continue;
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			String cfgId = XmlUtil.determineAttributeValue(cfgNode, ATTR_ID);
			@SuppressWarnings("unused")
			String cfgName = XmlUtil.determineAttributeValue(cfgNode, ATTR_NAME);

			NodeList extensionAndReferenceNodes = cfgNode.getChildNodes();
			for (int ie=0;ie<extensionAndReferenceNodes.getLength();ie++) {
				Node extNode = extensionAndReferenceNodes.item(ie);
				if (!(extNode instanceof Element))
					continue;

				if (extNode.getNodeName().equals(ELEM_EXTENSION)) {
					NodeList providerNodes = extNode.getChildNodes();

					for (int i=0;i<providerNodes.getLength();i++) {
						Node providerNode = providerNodes.item(i);
						if (!(providerNode instanceof Element))
							continue;

						ILanguageSettingsProvider provider=null;
						if (providerNode.getNodeName().equals(ELEM_PROVIDER_REFERENCE)) {
							String providerId = XmlUtil.determineAttributeValue(providerNode, ATTR_ID);
							provider = getWorkspaceProvider(providerId);
						} else if (providerNode.getNodeName().equals(ELEM_PROVIDER)) {
							String providerClass = XmlUtil.determineAttributeValue(providerNode, ATTR_CLASS);
							if (providerClass!=null) {
								provider = createProviderCarcass(providerClass, Platform.getExtensionRegistry());
								if (provider instanceof LanguageSettingsSerializable) {
									((LanguageSettingsSerializable)provider).load((Element) providerNode);
								}
							}
						}
						if (provider!=null) {
							providers.add(provider);
						}
					}
				}
			}

			ICConfigurationDescription cfgDescription = prjDescription.getConfigurationById(cfgId);
			if (cfgDescription!=null)
				cfgDescription.setLanguageSettingProviders(providers);
		}
	}

	public static void loadLanguageSettings(ICProjectDescription prjDescription) {
		IProject project = prjDescription.getProject();
		IFile file = project.getFile(SETTINGS_FOLDER_NAME+STORAGE_PROJECT_LANGUAGE_SETTINGS);
		// AG: FIXME not sure about that one
		// Causes java.lang.IllegalArgumentException: Attempted to beginRule: P/cdt312, does not match outer scope rule: org.eclipse.cdt.internal.ui.text.c.hover.CSourceHover$SingletonRule@6f34fb
		try {
			file.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (CoreException e) {
			// ignore failure
		}
		if (file.exists() && file.isAccessible()) {
			Document doc = null;
			try {
				synchronized (serializingLock) {
					doc = XmlUtil.loadXML(file);
				}
				Element rootElement = doc.getDocumentElement(); // <project/>
				loadLanguageSettings(rootElement, prjDescription);
			} catch (Exception e) {
				CCorePlugin.log("Can't load preferences from file "+file.getLocation(), e); //$NON-NLS-1$
			}

			if (doc!=null) {
			}

		} else {
			// Already existing legacy projects
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
				if (cfgDescription!=null) {
					List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(2);
					ILanguageSettingsProvider userProvider = getWorkspaceProvider(MBS_LANGUAGE_SETTINGS_PROVIDER);
					providers.add(userProvider);
					cfgDescription.setLanguageSettingProviders(providers);
				}
			}

		}
	}

	// AG FIXME
	/**
	 * @param msg
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void logInfo(String msg) {
		Exception e = new Exception(msg);
		IStatus status = new Status(IStatus.INFO, CCorePlugin.PLUGIN_ID, msg, e);
		CCorePlugin.log(status);
	}

	// AG FIXME
	/**
	 * @param msg
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void logWarning(String msg) {
		Exception e = new Exception(msg);
		IStatus status = new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, msg, e);
		CCorePlugin.log(status);
	}

	// AG FIXME
	/**
	 * @param msg
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void logError(String msg) {
		Exception e = new Exception(msg);
		IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, msg, e);
		CCorePlugin.log(status);
	}

}
