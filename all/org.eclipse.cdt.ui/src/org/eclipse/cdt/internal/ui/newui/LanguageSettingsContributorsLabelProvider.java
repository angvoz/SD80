package org.eclipse.cdt.internal.ui.newui;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;

import org.eclipse.cdt.internal.ui.CPluginImages;

public class LanguageSettingsContributorsLabelProvider extends LabelProvider /*implements IFontProvider, ITableLabelProvider , IColorProvider */ {
		public LanguageSettingsContributorsLabelProvider() {
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

			if (element instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider provider = (ILanguageSettingsProvider)element;
				if (provider.getId().equals("org.eclipse.cdt.ui.user.LanguageSettingsProvider")) {
					if (LanguageSettingsManager.isWorkspaceProvider(provider)) {
						imageKey = CPluginImages.IMG_OBJS_USER;
					} else {
						imageKey = CPluginImages.IMG_OBJS_USER_ME;
					}
				} else if (provider.getId().equals("org.eclipse.cdt.managedbuilder.core.LanguageSettingsProvider")) {
					imageKey = CPluginImages.IMG_OBJS_MBS;
				} else {
					imageKey = CPluginImages.IMG_OBJS_LANG_SETTINGS_PROVIDER;
				}
				
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

			String imageKey = getImageKey(element, columnIndex);
			if (imageKey!=null) {
				String overlayKey = getOverlayKey(element, columnIndex);
				if (overlayKey!=null) {
					return CPluginImages.getOverlaidImage(imageKey, overlayKey, IDecoration.TOP_RIGHT);
				}
				return CPluginImages.get(imageKey);
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
				return ((ILanguageSettingsProvider)element).getName();
			} else if (element instanceof ICLanguageSettingEntry) {
				ICLanguageSettingEntry le = (ICLanguageSettingEntry) element;
				if (columnIndex == 0) {
					String s = le.getName();
					if (le.getKind() == ICSettingEntry.MACRO) {
						s = s+'='+le.getValue();
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
				return AllLanguageSettingEntriesTab.EMPTY_STR;
			}
			return (columnIndex == 0) ? element.toString() : AllLanguageSettingEntriesTab.EMPTY_STR;
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