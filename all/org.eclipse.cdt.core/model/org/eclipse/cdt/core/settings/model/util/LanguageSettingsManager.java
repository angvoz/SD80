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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.LanguageSettingsPersistentProvider;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescription;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescriptionCache;
import org.eclipse.cdt.internal.core.settings.model.LanguageSettingsExtensionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.prefs.BackingStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * TODO
 * This layer of language settings in TODO
 *
 * Duplicate entries are filtered where only first entry is preserved.
 *
 */
public class LanguageSettingsManager {
	public static final String PROVIDER_UNKNOWN = "org.eclipse.cdt.projectmodel.4.0.0";
	public static final String PROVIDER_UI_USER = "org.eclipse.cdt.ui.user";
	public static final char PROVIDER_DELIMITER = LanguageSettingsExtensionManager.PROVIDER_DELIMITER;

	private static final String ROOT_ELEM = "languageSettings";
	private static final String ATTR_PROJECT_NAME = "project";


	/**
	 * Never returns {@code null} although individual providers return {@code null} if
	 * no settings defined.
	 */
	public static List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, String providerId, IResource rc, String languageId) {
		Assert.isNotNull(cfgDescription);

		ILanguageSettingsProvider provider = getProvider(cfgDescription, providerId);
		if (provider!=null) {
			List<ICLanguageSettingEntry> list = provider.getSettingEntries(cfgDescription, rc, languageId);
			if (list!=null) {
				return new ArrayList<ICLanguageSettingEntry>(list);
			}
		}

		if (rc!=null) {
			IResource parentFolder = rc.getParent();
			if (parentFolder!=null) {
				return getSettingEntries(cfgDescription, providerId, parentFolder, languageId);
			}
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

	// FIXME: consider removing, semantics are not well defined
	@Deprecated
	public static List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription,
			String providerId, IResource rc, String languageId, int kind) {
		ILanguageSettingsProvider provider = getProvider(cfgDescription, providerId);
		if (provider==null) {
			return new ArrayList<ICLanguageSettingEntry>(0);
		}
		List<ICLanguageSettingEntry> list = getSettingEntries(cfgDescription, providerId, rc, languageId);
		if (list==null) {
			return new ArrayList<ICLanguageSettingEntry>(0);
		}
		
		ArrayList<ICLanguageSettingEntry> newList = new ArrayList<ICLanguageSettingEntry>(list.size());
		for (ICLanguageSettingEntry entry : list) {
			if (entry!=null && entry.getKind()==kind && !containsEntry(newList, entry.getName())) {
				newList.add(entry);
			}
		}
		return newList;
	}

	public static List<ICLanguageSettingEntry> getSettingEntriesReconciled(ICConfigurationDescription cfgDescription, IResource rc, String languageId, int kind) {
		List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>();
		for (ILanguageSettingsProvider provider: getProviders(cfgDescription)) {
			for (ICLanguageSettingEntry entry : getSettingEntries(cfgDescription, provider.getId(), rc, languageId, kind)) {
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

	private static ICLanguageSetting[] getLanguageIds(ICResourceDescription rcDescription) {
		if (rcDescription instanceof ICFileDescription) {
			ICFileDescription fileDescription = (ICFileDescription)rcDescription;
			return new ICLanguageSetting[] {fileDescription.getLanguageSetting()};
		} else if (rcDescription instanceof ICFolderDescription) {
			ICFolderDescription folderDescription = (ICFolderDescription)rcDescription;
			return folderDescription.getLanguageSettings();
		}

		return null;
	}
	
	public static boolean isCustomizedResource(ICConfigurationDescription cfgDescription, IResource rc) {
		for (ILanguageSettingsProvider provider: getProviders(cfgDescription)) {
			ICResourceDescription rcDescription = cfgDescription.getResourceDescription(rc.getProjectRelativePath(), false);
			for (ICLanguageSetting languageSetting : getLanguageIds(rcDescription)) {
				String languageId = languageSetting.getLanguageId();
				List<ICLanguageSettingEntry> list = provider.getSettingEntries(cfgDescription, rc, languageId);
				if (list!=null) {
					return true;
				}
			}
		}
		
		return false;
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

	public static void load(ICConfigurationDescription cfgDescription) {
		IProject project = cfgDescription.getProjectDescription().getProject();
		IFile file = project.getFile("language.settings.xml");
		if (file.exists() && file.isAccessible()) {
			Document doc = null;
			try {
				doc = loadXML(file);
			} catch (Exception e) {
				CCorePlugin.log("Can't load preferences from file "+file.getLocation(), e); //$NON-NLS-1$
			}
			
			if (doc!=null) {
				Element rootElement = doc.getDocumentElement();
				
				for (ILanguageSettingsProvider provider : LanguageSettingsManager.getProviders(cfgDescription)) {
					if (provider instanceof LanguageSettingsPersistentProvider) {
						((LanguageSettingsPersistentProvider) provider).load(rootElement);
					}
				}
			}
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

	public static void serialize(ICConfigurationDescription cfgDescription) throws CoreException {
		try {
			IProject project = cfgDescription.getProjectDescription().getProject();
			
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			Element rootElement = doc.createElement(ROOT_ELEM);
			rootElement.setAttribute(ATTR_PROJECT_NAME, project.getName());
			doc.appendChild(rootElement);

			for (ILanguageSettingsProvider provider : getProviders(cfgDescription)) {
				if (provider instanceof LanguageSettingsPersistentProvider) {
					((LanguageSettingsPersistentProvider) provider).serialize(rootElement);
				}
			}
			InputStream input = new ByteArrayInputStream(toByteArray(doc));
			
			IFile file = project.getFile("language.settings.xml");
			if (file.exists()) {
				file.setContents(input, IResource.FORCE, null);
			} else {
				file.create(input, IResource.FORCE, null);
			}

		} catch (Exception e) {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.getResourceString("Internal error while trying to serialize language settings"), e);
			throw new CoreException(s);
		}
	}

	/**
	 * Set and store in workspace area user defined providers.
	 *
	 * @param providers - array of user defined providers
	 * @throws CoreException in case of problems
	 */
	public static void setUserDefinedProviders(ILanguageSettingsProvider[] providers) throws CoreException {
		LanguageSettingsExtensionManager.setUserDefinedProviders(providers);
	}

	/**
	 * @return available providers IDs which include contributed through extension + user defined ones
	 * from workspace
	 */
	public static String[] getProviderAvailableIds() {
		return LanguageSettingsExtensionManager.getProviderAvailableIds();
	}

	/**
	 * @return IDs of language settings providers of LanguageSettingProvider extension point.
	 */
	public static String[] getProviderExtensionIds() {
		return LanguageSettingsExtensionManager.getProviderExtensionIds();
	}

	/**
	 * TODO
	 */
	public static ILanguageSettingsProvider getProvider(String id) {
		return LanguageSettingsExtensionManager.getProvider(id);
	}

	/**
	 * Set and store default providers IDs to be used if provider list is empty.
	 *
	 * @param ids - default providers IDs
	 * @throws BackingStoreException in case of problem with storing
	 */
	public static void setDefaultProviderIds(String[] ids) throws BackingStoreException {
		LanguageSettingsExtensionManager.setDefaultProviderIds(ids);
	}

	/**
	 * @return default providers IDs to be used if provider list is empty.
	 */
	public static String[] getDefaultProviderIds() {
		return LanguageSettingsExtensionManager.getDefaultProviderIds();
	}

	/**
	 * This usage is discouraged TODO .
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void setProviders(ICConfigurationDescription cfgDescription, List<ILanguageSettingsProvider> providers) {
		if (cfgDescription instanceof CConfigurationDescription) {
			((CConfigurationDescription)cfgDescription).setLanguageSettingProviders(providers);
		} else if (cfgDescription instanceof CConfigurationDescriptionCache) {
				((CConfigurationDescriptionCache)cfgDescription).setLanguageSettingProviders(providers);
		} else if (cfgDescription!=null) {
			String className = cfgDescription.getClass().getName();
			CCorePlugin.log("Error setting ICLanguageSettingsProvider for unsupported configuration description type " + className); //$NON-NLS-1$
		}
	}

	/**
	 * This usage is discouraged TODO .
	 * @ ??? noreference This method is not intended to be referenced by clients.
	 */
	public static List<ILanguageSettingsProvider> getProviders(ICConfigurationDescription cfgDescription) {
		if (cfgDescription instanceof CConfigurationDescription) {
			return ((CConfigurationDescription)cfgDescription).getLanguageSettingProviders();
		} else if (cfgDescription instanceof CConfigurationDescriptionCache) {
			return ((CConfigurationDescriptionCache)cfgDescription).getLanguageSettingProviders();
		} else if (cfgDescription!=null) {
			String className = cfgDescription.getClass().getName();
			CCorePlugin.log("Error getting ICLanguageSettingsProvider for unsupported configuration description type " + className); //$NON-NLS-1$
		}
		return new ArrayList<ILanguageSettingsProvider>();
	}

	/**
	 */
	public static void setProviderIds(ICConfigurationDescription cfgDescription, List<String> ids) {
		if (cfgDescription instanceof CConfigurationDescription) {
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(ids.size());
			for (String id : ids) {
				ILanguageSettingsProvider provider = getProvider(id);
				if (provider!=null) {
					providers.add(provider);
				}
			}
			((CConfigurationDescription)cfgDescription).setLanguageSettingProviders(providers);
		} else if (cfgDescription instanceof CConfigurationDescriptionCache) {
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(ids.size());
			for (String id : ids) {
				ILanguageSettingsProvider provider = getProvider(id);
				if (provider!=null) {
					providers.add(provider);
				}
			}
			((CConfigurationDescriptionCache)cfgDescription).setLanguageSettingProviders(providers);
		} else if (cfgDescription!=null) {
			String className = cfgDescription.getClass().getName();
			CCorePlugin.log("Error setting ICLanguageSettingsProvider for unsupported configuration description type " + className); //$NON-NLS-1$
		}
	}
	
	/**
	 */
	public static List<String> getProviderIds(ICConfigurationDescription cfgDescription) {
		List<String> ids = new ArrayList<String>();
		if (cfgDescription instanceof CConfigurationDescription) {
			for (ILanguageSettingsProvider provider : ((CConfigurationDescription)cfgDescription).getLanguageSettingProviders()) {
				ids.add(provider.getId());
			}
		} else if (cfgDescription instanceof CConfigurationDescriptionCache) {
			for (ILanguageSettingsProvider provider : ((CConfigurationDescriptionCache)cfgDescription).getLanguageSettingProviders()) {
				ids.add(provider.getId());
			}
		} else if (cfgDescription!=null) {
			String className = cfgDescription.getClass().getName();
			CCorePlugin.log("Error getting ICLanguageSettingsProvider for unsupported configuration description type " + className); //$NON-NLS-1$
		}
		return ids;
	}
	
	/**
	 * TODO
	 */
	private static ILanguageSettingsProvider getProvider(ICConfigurationDescription cfgDescription, String id) {
		for (ILanguageSettingsProvider provider : getProviders(cfgDescription)) {
			if (provider.getId().equals(id)) {
				return provider;
			}
		}
		return null;
	}

	public static void serializeWorkspaceProviders() throws CoreException {
		LanguageSettingsExtensionManager.serializeLanguageSettings();
	}


}
