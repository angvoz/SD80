package org.eclipse.cdt.internal.core.language.settings.providers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager_TBD;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsWorkspaceProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LanguageSettingsProvidersSerializer {

	private static final String STORAGE_WORKSPACE_LANGUAGE_SETTINGS = "language.settings.xml"; //$NON-NLS-1$
	private static final String SETTINGS_FOLDER_NAME = ".settings/"; //$NON-NLS-1$
	private static final String STORAGE_PROJECT_LANGUAGE_SETTINGS = "language.settings.xml"; //$NON-NLS-1$
	public static final char PROVIDER_DELIMITER = ';';
	private static final String MBS_LANGUAGE_SETTINGS_PROVIDER = "org.eclipse.cdt.managedbuilder.core.LanguageSettingsProvider";
	private static final String ELEM_PLUGIN = "plugin"; //$NON-NLS-1$
	private static final String ELEM_EXTENSION = "extension"; //$NON-NLS-1$
	private static final String ATTR_POINT = "point"; //$NON-NLS-1$
	private static final String ELEM_PROJECT = "project"; //$NON-NLS-1$
	private static final String ELEM_CONFIGURATION = "configuration"; //$NON-NLS-1$
	private static final String ELEM_PROVIDER_REFERENCE = "provider-reference"; //$NON-NLS-1$
	/** Cache of globally available providers to be consumed by calling clients */
	private static final LinkedHashMap<String, ILanguageSettingsProvider> rawGlobalWorkspaceProviders = new LinkedHashMap<String, ILanguageSettingsProvider>();
	/** Global user-defined providers matching persistent storage */
	private static LinkedHashMap<String, ILanguageSettingsProvider> fUserDefinedProviders = null;
	private static Object serializingLock = new Object();
	
	static {
		try {
			loadLanguageSettingsWorkspace();
		} catch (Throwable e) {
			CCorePlugin.log("Error loading workspace language settings providers", e); //$NON-NLS-1$
		} finally {
		}
	}

	/**
	 * Populate the list of available providers where workspace level user defined parsers
	 * overwrite contributed through provider extension point.
	 */
	private static void recalculateAvailableProviders() {
		rawGlobalWorkspaceProviders.clear();
		if (fUserDefinedProviders!=null) {
			rawGlobalWorkspaceProviders.putAll(fUserDefinedProviders);
		}
		for (ILanguageSettingsProvider provider : LanguageSettingsExtensionManager.getExtensionProviders()) {
			String id = provider.getId();
			if (!rawGlobalWorkspaceProviders.containsKey(id)) {
				if (provider instanceof ILanguageSettingsEditableProvider) {
					try {
						provider = LanguageSettingsExtensionManager.getExtensionProviderCopy(id);
					} catch (CloneNotSupportedException e) {
						IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Not able to clone provider " + provider.getClass());
						CCorePlugin.log(new CoreException(status));
					}
				}
				rawGlobalWorkspaceProviders.put(id, provider);
			}
		}
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
		 * Set and store in workspace area user defined providers.
		 *
		 * @param providers - array of user defined providers
		 * @throws CoreException in case of problems
		 */
		public static void setUserDefinedProviders(List<ILanguageSettingsProvider> providers) throws CoreException {
			setUserDefinedProvidersInternal(providers);
			serializeLanguageSettingsWorkspace();
		}

	/**
	 * Internal method to set user defined providers in memory.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * Use {@link #setUserDefinedProviders(List)}.
	 *
	 * @param providers - list of user defined providers. If {@code null}
	 *    is passed user defined providers are cleared.
	 */
	public static void setUserDefinedProvidersInternal(List<ILanguageSettingsProvider> providers) {
		if (providers==null) {
			fUserDefinedProviders = null;
		} else {
			fUserDefinedProviders= new LinkedHashMap<String, ILanguageSettingsProvider>();
			// set customized list
			for (ILanguageSettingsProvider provider : providers) {
				if (isWorkspaceProvider(provider)) {
					provider = getRawWorkspaceProvider(provider.getId());
				}
				if (!LanguageSettingsExtensionManager.equalsExtensionProvider(provider)) {
					fUserDefinedProviders.put(provider.getId(), provider);
				}
			}
		}
		recalculateAvailableProviders();
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
		for (ILanguageSettingsProvider provider : rawGlobalWorkspaceProviders.values()) {
			if (provider instanceof LanguageSettingsSerializable) {
				// TODO - serialize only modified ones
				LanguageSettingsSerializable ser = (LanguageSettingsSerializable)provider;
				serializableExtensionProviders.add(ser);
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
			Element elementExtension = XmlUtil.appendElement(rootElement, ELEM_EXTENSION, new String[] {ATTR_POINT, LanguageSettingsExtensionManager.PROVIDER_EXTENSION_FULL_ID});
	
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
		fUserDefinedProviders = null;
		
		URI uriLocation = getStoreLocation(STORAGE_WORKSPACE_LANGUAGE_SETTINGS);
	
		Document doc = null;
		try {
			synchronized (serializingLock) {
				doc = XmlUtil.loadXml(uriLocation);
			}
		} catch (Exception e) {
			CCorePlugin.log("Can't load preferences from file "+uriLocation, e); //$NON-NLS-1$
		}
	
		if (doc!=null) {
			Element rootElement = doc.getDocumentElement();
			NodeList providerNodes = rootElement.getElementsByTagName(LanguageSettingsSerializable.ELEM_PROVIDER);
	
			List<String> userDefinedProvidersIds = new ArrayList<String>(); 
			for (int i=0;i<providerNodes.getLength();i++) {
				Node providerNode = providerNodes.item(i);
				String providerId = XmlUtil.determineAttributeValue(providerNode, LanguageSettingsExtensionManager.ATTR_ID);
				if (userDefinedProvidersIds.contains(providerId)) {
					String msg = "Ignored illegally persisted duplicate language settings provider id=" + providerId;
					CCorePlugin.log(new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, msg, new Exception()));
					continue;
				}
				userDefinedProvidersIds.add(providerId);
				
				ILanguageSettingsProvider provider = loadWorkspaceProvider(providerNode);
				if (provider!=null) {
					if (fUserDefinedProviders==null)
						fUserDefinedProviders= new LinkedHashMap<String, ILanguageSettingsProvider>();
					
					if (!LanguageSettingsExtensionManager.equalsExtensionProvider(provider)) {
						fUserDefinedProviders.put(provider.getId(), provider);
					}
				}
			}
		}
		recalculateAvailableProviders();
	}

	public static void serializeLanguageSettings(Element parentElement, ICProjectDescription prjDescription) throws CoreException {
		ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
		for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
			Element elementConfiguration = XmlUtil.appendElement(parentElement, ELEM_CONFIGURATION, new String[] {
					LanguageSettingsExtensionManager.ATTR_ID, cfgDescription.getId(),
					LanguageSettingsExtensionManager.ATTR_NAME, cfgDescription.getName(),
				});
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			if (providers.size()>0) {
				Element elementExtension = XmlUtil.appendElement(elementConfiguration, ELEM_EXTENSION, new String[] {
						ATTR_POINT, LanguageSettingsExtensionManager.PROVIDER_EXTENSION_FULL_ID});
				for (ILanguageSettingsProvider provider : providers) {
					if (isWorkspaceProvider(provider)) {
						// Element elementProviderReference =
						XmlUtil.appendElement(elementExtension, ELEM_PROVIDER_REFERENCE, new String[] {
								LanguageSettingsExtensionManager.ATTR_ID, provider.getId()});
						continue;
					}
					if (provider instanceof LanguageSettingsSerializable) {
						((LanguageSettingsSerializable) provider).serialize(elementExtension);
					} else {
						// Element elementProvider =
						XmlUtil.appendElement(elementExtension, LanguageSettingsExtensionManager.ELEM_PROVIDER, new String[] {
								LanguageSettingsExtensionManager.ATTR_ID, provider.getId(),
								LanguageSettingsExtensionManager.ATTR_NAME, provider.getName(),
								LanguageSettingsExtensionManager.ATTR_CLASS, provider.getClass().getCanonicalName(),
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
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Internal error while trying to serialize language settings", e);
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
			String cfgId = XmlUtil.determineAttributeValue(cfgNode, LanguageSettingsExtensionManager.ATTR_ID);
			@SuppressWarnings("unused")
			String cfgName = XmlUtil.determineAttributeValue(cfgNode, LanguageSettingsExtensionManager.ATTR_NAME);
	
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
							String providerId = XmlUtil.determineAttributeValue(providerNode, LanguageSettingsExtensionManager.ATTR_ID);
							provider = getWorkspaceProvider(providerId);
						} else if (providerNode.getNodeName().equals(LanguageSettingsExtensionManager.ELEM_PROVIDER)) {
							provider = loadConfigurationProvider(providerNode);
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

	private static ILanguageSettingsProvider loadWorkspaceProvider(Node providerNode) {
		String providerId = XmlUtil.determineAttributeValue(providerNode, LanguageSettingsExtensionManager.ATTR_ID);
		// try less expensive shallow copy first
		ILanguageSettingsProvider provider = LanguageSettingsExtensionManager.getExtensionProviderShallow(providerId);
		boolean isLoadable = (provider instanceof LanguageSettingsSerializable) && (provider instanceof ILanguageSettingsEditableProvider);
		if (!isLoadable) {
			provider = LanguageSettingsExtensionManager.getExtensionProvider(providerId);
		}
		
		String attrClass = XmlUtil.determineAttributeValue(providerNode, LanguageSettingsExtensionManager.ATTR_CLASS);
		if (provider!=null && !provider.getClass().getName().equals(attrClass) ) {
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Types mismatch while loading workspace provider. id=" + providerId
					+ ", extension class=" + provider.getClass().getName() + ", being loaded class=" + attrClass);
			CCorePlugin.log(new CoreException(status));
			return provider;
		}
		
		if (provider==null)
			provider = LanguageSettingsExtensionManager.getProviderInstance(attrClass);
		
		if (provider instanceof LanguageSettingsSerializable)
			((LanguageSettingsSerializable)provider).load((Element) providerNode);
		
		return provider;
	}

	private static ILanguageSettingsProvider loadConfigurationProvider(Node providerNode) {
		String attrClass = XmlUtil.determineAttributeValue(providerNode, LanguageSettingsExtensionManager.ATTR_CLASS);
		ILanguageSettingsProvider provider = LanguageSettingsExtensionManager.getProviderInstance(attrClass);
		
		if (provider instanceof LanguageSettingsSerializable)
			((LanguageSettingsSerializable)provider).load((Element) providerNode);

		return provider;
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
					doc = XmlUtil.loadXml(file);
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

	/**
	 * FIXME Get Language Settings Provider defined in the workspace. That includes user-defined
	 * providers and after that providers defined as extensions via
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} extension point.
	 * That returns actual object, any modifications will affect any configuration
	 * referring to the provider.
	 *
	 * @param id - ID of provider to find.
	 * @return the provider or {@code null} if provider is not defined.
	 */
	public static ILanguageSettingsProvider getWorkspaceProvider(String id) {
		return new LanguageSettingsWorkspaceProvider(id);
	}

	/**
	 * Note that it is not legal to add workspace provider to a configuration
	 * directly, use {@link #getWorkspaceProvider(String)} wrapper.
	 * 
	 * @param id
	 * @return
	 */
	public static ILanguageSettingsProvider getRawWorkspaceProvider(String id) {
		return rawGlobalWorkspaceProviders.get(id);
	}

	/**
	 * TODO
	 * @return ordered set of providers defined in the workspace which include contributed through extension + user defined ones
	 * 
	 */
	public static List<ILanguageSettingsProvider> getWorkspaceProviders() {
		ArrayList<ILanguageSettingsProvider> workspaceProviders = new ArrayList<ILanguageSettingsProvider>();
		for (ILanguageSettingsProvider rawProvider : rawGlobalWorkspaceProviders.values()) {
			workspaceProviders.add(new LanguageSettingsWorkspaceProvider(rawProvider.getId()));
		}
		return workspaceProviders;
	}

	/**
	 * Checks if the provider is defined on the workspace level.
	 *
	 * @param provider - provider to check.
	 * @return {@code true} if the given provider is workspace provider, {@code false} otherwise.
	 * 
	 */
	public static boolean isWorkspaceProvider(ILanguageSettingsProvider provider) {
		return provider instanceof LanguageSettingsWorkspaceProvider;
	}
}