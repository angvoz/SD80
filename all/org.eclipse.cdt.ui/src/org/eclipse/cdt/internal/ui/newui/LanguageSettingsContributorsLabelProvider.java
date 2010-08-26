package org.eclipse.cdt.internal.ui.newui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;

import org.eclipse.cdt.internal.ui.CPluginImages;

public class LanguageSettingsContributorsLabelProvider extends LabelProvider /*implements IFontProvider, ITableLabelProvider , IColorProvider */ {
	private static final String LANGUAGE_SETTINGS_PROVIDER_UI = "LanguageSettingsProviderUI"; //$NON-NLS-1$
	private static final String ELEM_PROVIDER_UI = "providerUI"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_ICON = "icon"; //$NON-NLS-1$
	private static final String ATTR_PAGE = "page"; //$NON-NLS-1$

	static private Map<URL, Image> loadedIcons = null;
	static private Map<String, Image> fImage = null;

	public LanguageSettingsContributorsLabelProvider() {
	}

	private Image getIcon(IConfigurationElement config) {
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

	private void loadImages() {
		if (loadedIcons==null) loadedIcons = new HashMap<URL, Image>();
		if (fImage==null) fImage = new HashMap<String, Image>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extension = registry.getExtensionPoint(CUIPlugin.PLUGIN_ID, LANGUAGE_SETTINGS_PROVIDER_UI);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				try {
					String extensionID = ext.getUniqueIdentifier();
					for (IConfigurationElement cfgEl : ext.getConfigurationElements()) {
						if (cfgEl.getName().equals(ELEM_PROVIDER_UI)) {
							String id = cfgEl.getAttribute(ATTR_ID);
							String strIcon = cfgEl.getAttribute(ATTR_ICON);
							String strPage = cfgEl.getAttribute(ATTR_PAGE);
							Image image =getIcon(cfgEl);
							fImage.put(id, image);
						}
					}
				} catch (Exception e) {
					CUIPlugin.log("Cannot load LanguageSettingsProviderUI extension " + ext.getUniqueIdentifier(), e); //$NON-NLS-1$
				}
			}
		}

	}

	@Override
	public Image getImage(Object element) {
		return getColumnImage(element, 0);
	}

	protected String getOverlayKey(Object element, int columnIndex) {
		return null;
	}

	protected String getImageKey(Object element, int columnIndex) {
		String imageKey = null;

		ILanguageSettingsProvider provider = (ILanguageSettingsProvider) element;
		if (provider.getId().equals("org.eclipse.cdt.ui.user.LanguageSettingsProvider")) {
			if (LanguageSettingsManager.isWorkspaceProvider(provider)) {
				imageKey = CPluginImages.IMG_OBJS_USER;
			} else {
				imageKey = CPluginImages.IMG_OBJS_USER_ME;
			}
		} else if (provider.getId().equals("org.eclipse.cdt.managedbuilder.core.LanguageSettingsProvider")) {
			imageKey = CPluginImages.IMG_OBJS_MBS;
		}

		return imageKey;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex > 0)
			return null;

		if (element instanceof ICLanguageSettingEntry) {
			ICLanguageSettingEntry le = (ICLanguageSettingEntry) element;
			int kind = le.getKind();
			boolean isWorkspacePath = (le.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0;
			boolean isProjectRelative = isWorkspacePath && !le.getName().startsWith("/");
			return LanguageSettingsEntryImages.getImage(kind, le.getFlags(), isProjectRelative);
		}

		if (element instanceof ILanguageSettingsProvider) {
			ILanguageSettingsProvider provider = (ILanguageSettingsProvider)element;
			String imageKey = getImageKey(element, columnIndex);
			if (imageKey==null) {
				if (loadedIcons==null || fImage==null) {
					loadedIcons = new HashMap<URL, Image>();
					fImage = new HashMap<String, Image>();
					loadImages();
				}

				Image image = fImage.get(provider.getId());
				if (image!=null) {
					return image;
				}

				imageKey = CPluginImages.IMG_OBJS_LANG_SETTINGS_PROVIDER;
			}
			if (imageKey != null) {
				String overlayKey = getOverlayKey(element, columnIndex);
				if (overlayKey != null) {
					return CPluginImages.getOverlaidImage(imageKey, overlayKey, IDecoration.TOP_RIGHT);
				}
				return CPluginImages.get(imageKey);
			}
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		return getColumnText(element, 0);
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Object[]) {
			return "OOPS";
		}
		if (element instanceof ILanguageSettingsProvider) {
			return ((ILanguageSettingsProvider) element).getName();
		} else if (element instanceof ICLanguageSettingEntry) {
			ICLanguageSettingEntry le = (ICLanguageSettingEntry) element;
			if (columnIndex == 0) {
				String s = le.getName();
				if (le.getKind() == ICSettingEntry.MACRO) {
					s = s + '=' + le.getValue();
				}
//					if (exported.contains(resolve(le)))
//						s = s + Messages.AbstractLangsListTab_3;
				return s;
			}
			if (le.getKind() == ICSettingEntry.MACRO) {
				switch (columnIndex) {
				case 1:
					return le.getValue();
				}
			}
			return AbstractCPropertyTab.EMPTY_STR;
		}
		return (columnIndex == 0) ? element.toString() : AbstractCPropertyTab.EMPTY_STR;
	}

	public Font getFont(Object element) {
		if (!(element instanceof ICLanguageSettingEntry))
			return null;
		ICLanguageSettingEntry le = (ICLanguageSettingEntry) element;
		if (le.isBuiltIn())
			return null; // built in
		if (le.isReadOnly()) // read only
			return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
		// normal
		return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
	}
}