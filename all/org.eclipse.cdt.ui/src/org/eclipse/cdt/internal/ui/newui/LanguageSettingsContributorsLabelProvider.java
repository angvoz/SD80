package org.eclipse.cdt.internal.ui.newui;

import java.net.URL;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;

public class LanguageSettingsContributorsLabelProvider extends LabelProvider /*implements IFontProvider, ITableLabelProvider , IColorProvider */ {
	private static final String TEST_PLUGIN_ID = "org.eclipse.cdt.core.tests";

	protected String getBaseKey(ILanguageSettingsProvider provider) {
		String imageKey = null;
		// try id-association
		URL url = LanguageSettingsProviderAssociation.getImageUrl(provider.getId());
		// try class-association
		if (url==null) {
			url = LanguageSettingsProviderAssociation.getImage(provider.getClass());
		}
		if (url!=null) {
			imageKey = url.toString();
		} else {
			imageKey = CDTSharedImages.IMG_OBJS_LANG_SETTINGS_PROVIDER;
		}
		if (provider.getId().startsWith(TEST_PLUGIN_ID)) {
			imageKey = CDTSharedImages.IMG_OBJS_CDT_TESTING;
		}
		return imageKey;
	}

	protected String[] getOverlayKeys(ILanguageSettingsProvider provider) {
		String[] overlayKeys = new String[5];
		if (LanguageSettingsManager.isWorkspaceProvider(provider)) {
//			overlayKeys[IDecoration.TOP_LEFT] = LanguageSettingsImages.IMG_OVR_GLOBAL;
//			overlayKeys[IDecoration.TOP_LEFT] = LanguageSettingsImages.IMG_OVR_REFERENCE;
//			overlayKeys[IDecoration.TOP_RIGHT] = LanguageSettingsImages.IMG_OVR_PARENT;
//			overlayKeys[IDecoration.BOTTOM_RIGHT] = LanguageSettingsImages.IMG_OVR_LINK;
		} else {
//			overlayKeys[IDecoration.TOP_LEFT] = LanguageSettingsImages.IMG_OVR_CONFIGURATION;
//			overlayKeys[IDecoration.TOP_LEFT] = LanguageSettingsImages.IMG_OVR_INDEXED;
//			overlayKeys[IDecoration.TOP_LEFT] = LanguageSettingsImages.IMG_OVR_CONTEXT;
			
//			overlayKeys[IDecoration.TOP_LEFT] = LanguageSettingsImages.IMG_OVR_PROJECT;
		}
		return overlayKeys;
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof ICLanguageSettingEntry) {
			ICLanguageSettingEntry le = (ICLanguageSettingEntry) element;
			return LanguageSettingsImages.getImage(le);
		}

		if (element instanceof ILanguageSettingsProvider) {
			ILanguageSettingsProvider provider = (ILanguageSettingsProvider)element;
			String imageKey = getBaseKey(provider);
			String[] overlayKeys = getOverlayKeys(provider);
			return CDTSharedImages.getImageOverlaid(imageKey, overlayKeys);
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
			return null;
		if (le.isReadOnly())
			return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
		
		return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
	}
}