package org.eclipse.cdt.internal.ui.newui;

import java.net.URL;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;

public class LanguageSettingsContributorsLabelProvider extends LabelProvider /*implements IFontProvider, ITableLabelProvider , IColorProvider */ {

	@Override
	public Image getImage(Object element) {
		return getColumnImage(element, 0);
	}

	protected String[] getOverlayKeys(Object element, int columnIndex) {
		if (element instanceof ILanguageSettingsProvider) {
			String[] overlayKeys = new String[5];
			ILanguageSettingsProvider provider = (ILanguageSettingsProvider)element;
			if (LanguageSettingsManager.isWorkspaceProvider(provider)) {
//				overlayKeys[IDecoration.TOP_LEFT] = LanguageSettingsImages.IMG_OVR_GLOBAL;
				overlayKeys[IDecoration.BOTTOM_RIGHT] = LanguageSettingsImages.IMG_OVR_LINK;
			}
			return overlayKeys;
		}
		return null;
	}

	protected String getImageKey(Object element, int columnIndex) {
		String imageKey = null;

//		ILanguageSettingsProvider provider = (ILanguageSettingsProvider) element;
//		if (provider.getId().equals("org.eclipse.cdt.ui.user.LanguageSettingsProviderXXX")) {
//			if (LanguageSettingsManager.isWorkspaceProvider(provider)) {
//				imageKey = CPluginImages.IMG_OBJS_USER;
//			} else {
//				imageKey = CPluginImages.IMG_OBJS_USER_ME;
//			}
//		} else if (provider.getId().equals("org.eclipse.cdt.managedbuilder.core.LanguageSettingsProvider")) {
//			imageKey = CPluginImages.IMG_OBJS_MBS;
//		}

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
			return LanguageSettingsImages.getImage(kind, le.getFlags(), isProjectRelative);
		}

		if (element instanceof ILanguageSettingsProvider) {
			ILanguageSettingsProvider provider = (ILanguageSettingsProvider)element;
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
				imageKey = LanguageSettingsImages.IMG_OBJS_LANG_SETTINGS_PROVIDER;
			}
			final String TEST_PLUGIN_ID="org.eclipse.cdt.core.tests"; //$NON-NLS-1$
			if (provider.getId().startsWith(TEST_PLUGIN_ID)) {
				imageKey = LanguageSettingsImages.IMG_OBJS_CDT_TESTING;
			}

			String[] overlayKeys = getOverlayKeys(element, columnIndex);
			if (overlayKeys != null) {
				return LanguageSettingsImages.getOverlaidImage(imageKey, overlayKeys);
			}
			return LanguageSettingsImages.get(imageKey);
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