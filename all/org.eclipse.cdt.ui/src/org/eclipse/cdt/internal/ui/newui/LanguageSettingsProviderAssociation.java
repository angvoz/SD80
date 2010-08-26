package org.eclipse.cdt.internal.ui.newui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
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

import org.eclipse.cdt.core.settings.model.ILanguageSettingsProvider;
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
	static private Map<String, Image> fImagesById = null;
	static private Map<String, Image> fImagesByClass = null;
	static private Map<String, ICOptionPage> fPagesById = null;
	static private Map<String, ICOptionPage> fPagesByClass = null;

	private static void loadExtensions() {
		if (loadedIcons==null) loadedIcons = new HashMap<URL, Image>();
		if (fImagesById==null) fImagesById = new HashMap<String, Image>();
		if (fImagesByClass==null) fImagesByClass = new HashMap<String, Image>();
		if (fPagesById==null) fPagesById = new HashMap<String, ICOptionPage>();
		if (fPagesByClass==null) fPagesByClass = new HashMap<String, ICOptionPage>();

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
							Image image =getIcon(cfgEl);
							fImagesById.put(id, image);
							String pageClass = cfgEl.getAttribute(ATTR_PAGE);
							if (pageClass!=null && pageClass.trim().length()>0) {
								ICOptionPage page = (ICOptionPage) cfgEl.createExecutableExtension(ATTR_PAGE);
								fPagesById.put(id, page);
							}
						} else if (cfgEl.getName().equals(ELEM_CLASS_ASSOCIATION)) {
							String clazz = cfgEl.getAttribute(ATTR_CLASS);
							Image image =getIcon(cfgEl);
							fImagesByClass.put(clazz, image);
							String pageClass = cfgEl.getAttribute(ATTR_PAGE);
							if (pageClass!=null && pageClass.trim().length()>0) {
								ICOptionPage page = (ICOptionPage) cfgEl.createExecutableExtension(ATTR_PAGE);
								fPagesByClass.put(clazz, page);
							}
						}
					}
				} catch (Exception e) {
					CUIPlugin.log("Cannot load LanguageSettingsProviderAssociation extension " + ext.getUniqueIdentifier(), e); //$NON-NLS-1$
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

	public static Image getImage(String id) {
		if (fImagesById==null) {
			loadExtensions();
		}
		return fImagesById.get(id);
	}

	public static ICOptionPage getOptionsPage(String id) {
		if (fPagesById==null) {
			loadExtensions();
		}
		return fPagesById.get(id);
	}

	/**
	 * Returns Language Settings Provider image registered for closest superclass.
	 *
	 * @param clazz - class to find Language Settings Provider image.
	 * @return image or {@code null}
	 */
	public static Image getImage(Class<? extends ILanguageSettingsProvider> clazz) {
		for (Class<?> c=clazz;c!=null;c=c.getSuperclass()) {
			String className = c.getCanonicalName();
			Set<Entry<String, Image>> entrySet = fImagesByClass.entrySet();
			for (Entry<String, Image> entry : entrySet) {
				if (entry.getKey().equals(className)) {
					return entry.getValue();
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
	public static ICOptionPage getOptionsPage(Class<? extends ILanguageSettingsProvider> clazz) {
		for (Class<?> c=clazz;c!=null;c=c.getSuperclass()) {
			String className = c.getCanonicalName();
			Set<Entry<String, ICOptionPage>> entrySet = fPagesByClass.entrySet();
			for (Entry<String, ICOptionPage> entry : entrySet) {
				if (entry.getKey().equals(className)) {
					return entry.getValue();
				}
			}
		}
		return null;
	}


}
