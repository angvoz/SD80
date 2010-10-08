package org.eclipse.cdt.internal.ui.newui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;

public class LanguageSettingsProviderAssociation {
	public static final String LANGUAGE_SETTINGS_PROVIDER_UI = "LanguageSettingsProviderAssociation"; //$NON-NLS-1$

	private static final String ELEM_ID_ASSOCIATION = "id-association"; //$NON-NLS-1$
	private static final String ELEM_CLASS_ASSOCIATION = "class-association"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_ICON = "icon"; //$NON-NLS-1$
	private static final String ATTR_PAGE = "page"; //$NON-NLS-1$

	static private Map<URL, Image> loadedIcons = null;
	static private Map<String, URL> fImagesUrlById = null;
	static private Map<String, URL> fImagesByClass = null;
	static private List<String> fRegirestedIds = null;
	static private List<String> fRegisteredClasses = null;

	private static void loadExtensions() {
		if (loadedIcons!=null) {
			return;
		}
		if (loadedIcons==null) loadedIcons = new HashMap<URL, Image>();
		if (fImagesUrlById==null) fImagesUrlById = new HashMap<String, URL>();
		if (fImagesByClass==null) fImagesByClass = new HashMap<String, URL>();
		if (fRegirestedIds==null) fRegirestedIds = new ArrayList<String>();
		if (fRegisteredClasses==null) fRegisteredClasses = new ArrayList<String>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extension = registry.getExtensionPoint(CUIPlugin.PLUGIN_ID, LanguageSettingsProviderAssociation.LANGUAGE_SETTINGS_PROVIDER_UI);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				@SuppressWarnings("unused")
				String extensionID = ext.getUniqueIdentifier();
				for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
					if (cfgEl.getName().equals(ELEM_ID_ASSOCIATION)) {
						String id = cfgEl.getAttribute(ATTR_ID);
						Image image =getIcon(cfgEl);
						URL url = getIconUrl(cfgEl);
						fImagesUrlById.put(id, url);
						fRegirestedIds.add(id);
					} else if (cfgEl.getName().equals(ELEM_CLASS_ASSOCIATION)) {
						String className = cfgEl.getAttribute(ATTR_CLASS);
						Image image =getIcon(cfgEl);
						URL url = getIconUrl(cfgEl);
						fImagesByClass.put(className, url);
						String pageClass = cfgEl.getAttribute(ATTR_PAGE);
						if (pageClass!=null && pageClass.trim().length()>0) {
							fRegisteredClasses.add(className);
						}
					}
				}
			}
		}

	}

	private static Image getIcon(IConfigurationElement config) {
		ImageDescriptor idesc = null;
		URL url = null;
		try {
			String iconName = config.getAttribute(ATTR_ICON);
			if (iconName != null) {
				URL pluginInstallUrl = Platform.getBundle(config.getDeclaringExtension().getContributor().getName()).getEntry("/"); //$NON-NLS-1$
				url = new URL(pluginInstallUrl, iconName);
				if (loadedIcons.containsKey(url))
					return loadedIcons.get(url);
				idesc = ImageDescriptor.createFromURL(url);
			}
		} catch (MalformedURLException exception) {}
		if (idesc == null)
			return null;
		Image img = idesc.createImage();
		loadedIcons.put(url, img);
		return img;
	}

	private static URL getIconUrl(IConfigurationElement config) {
		ImageDescriptor idesc = null;
		URL url = null;
		try {
			String iconName = config.getAttribute(ATTR_ICON);
			if (iconName != null) {
				URL pluginInstallUrl = Platform.getBundle(config.getDeclaringExtension().getContributor().getName()).getEntry("/"); //$NON-NLS-1$
				url = new URL(pluginInstallUrl, iconName);
				if (loadedIcons.containsKey(url))
					return url;
				idesc = ImageDescriptor.createFromURL(url);
			}
		} catch (MalformedURLException exception) {}
		if (idesc == null)
			return null;
		Image img = idesc.createImage();
		loadedIcons.put(url, img);
		return url;
	}

	public static URL getImageUrl(String id) {
		if (fImagesUrlById==null) {
			loadExtensions();
		}
		return fImagesUrlById.get(id);
	}

	private static ICOptionPage createOptionsPageById(String providerId) {
		if (fRegirestedIds==null) {
			loadExtensions();
		}
		if (fRegirestedIds.contains(providerId)) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extension = registry.getExtensionPoint(CUIPlugin.PLUGIN_ID, LanguageSettingsProviderAssociation.LANGUAGE_SETTINGS_PROVIDER_UI);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension ext : extensions) {
					try {
						String extensionID = ext.getUniqueIdentifier();
						for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
							if (cfgEl.getName().equals(ELEM_ID_ASSOCIATION)) {
								String id = cfgEl.getAttribute(ATTR_ID);
								if (providerId.equals(id)) {
									String pageClass = cfgEl.getAttribute(ATTR_PAGE);
									if (pageClass!=null && pageClass.trim().length()>0) {
										ICOptionPage page = (ICOptionPage) cfgEl.createExecutableExtension(ATTR_PAGE);
										return page;
									}
								}
							}
						}
					} catch (Exception e) {
						CUIPlugin.log("Cannot load LanguageSettingsProviderAssociation extension " + ext.getUniqueIdentifier(), e); //$NON-NLS-1$
					}
				}
			}
		}
		return null;
	}

	private static ICOptionPage createOptionsPageByClass(String providerClassName) {
		if (fRegisteredClasses==null) {
			loadExtensions();
		}
		if (fRegisteredClasses.contains(providerClassName)) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extension = registry.getExtensionPoint(CUIPlugin.PLUGIN_ID, LanguageSettingsProviderAssociation.LANGUAGE_SETTINGS_PROVIDER_UI);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension ext : extensions) {
					try {
						@SuppressWarnings("unused")
						String extensionID = ext.getUniqueIdentifier();
						for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
							if (cfgEl.getName().equals(ELEM_CLASS_ASSOCIATION)) {
								String className = cfgEl.getAttribute(ATTR_CLASS);
								if (providerClassName.equals(className)) {
									String pageClass = cfgEl.getAttribute(ATTR_PAGE);
									if (pageClass!=null && pageClass.trim().length()>0) {
										ICOptionPage page = (ICOptionPage) cfgEl.createExecutableExtension(ATTR_PAGE);
										return page;
									}
								}
							}
						}
					} catch (Exception e) {
						CUIPlugin.log("Cannot load LanguageSettingsProviderAssociation extension " + ext.getUniqueIdentifier(), e); //$NON-NLS-1$
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns Language Settings Provider image registered for closest superclass.
	 *
	 * @param clazz - class to find Language Settings Provider image.
	 * @return image or {@code null}
	 */
	public static URL getImage(Class<? extends ILanguageSettingsProvider> clazz) {
		for (Class<?> c=clazz;c!=null;c=c.getSuperclass()) {
			String className = c.getCanonicalName();
			Set<Entry<String, URL>> entrySet = fImagesByClass.entrySet();
			for (Entry<String, URL> entry : entrySet) {
				if (entry.getKey().equals(className)) {
					return entry.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Returns Language Settings Provider image registered for closest superclass.
	 * @param provider TODO
	 * @param id TODO
	 * @param clazz - class to find Language Settings Provider image.
	 * @return image or {@code null}
	 */
	public static ICOptionPage createOptionsPage(ILanguageSettingsProvider provider) {
		String id = provider.getId();
		ICOptionPage optionsPage = createOptionsPageById(id);
		if (optionsPage!=null) {
			return optionsPage;
		}

		Class<? extends ILanguageSettingsProvider> clazz = provider.getClass();
		for (Class<?> c=clazz;c!=null;c=c.getSuperclass()) {
			String className = c.getCanonicalName();
			if (fRegisteredClasses.contains(className)) {
				optionsPage = createOptionsPageByClass(className);
				return optionsPage;
			}
		}
		return null;
	}


}
