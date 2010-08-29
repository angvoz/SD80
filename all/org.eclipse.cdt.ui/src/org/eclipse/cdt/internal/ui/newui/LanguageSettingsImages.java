package org.eclipse.cdt.internal.ui.newui;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.settings.model.ICSettingEntry;

import org.eclipse.cdt.internal.ui.CPluginImages;

public class LanguageSettingsImages {

	public static Image getImage(int kind, int flags, boolean isProjectRelative) {
		String imageKey = getImageKey(kind, flags, isProjectRelative);
		if (imageKey!=null) {
			String overlayKey = getOverlayKey(kind, flags, isProjectRelative);
			if (overlayKey!=null) {
				return CPluginImages.getOverlaidImage(imageKey, overlayKey, IDecoration.TOP_RIGHT);
			}
			return CPluginImages.get(imageKey);
		}
		return null;
	}
	
	private static String getOverlayKey(Object element, int columnIndex, boolean isProjectRelative) {
		return null;
	}

	private static String getImageKey(int kind, int flag, boolean isProjectRelative) {
		String imageKey = null;

			boolean isWorkspacePath = (flag & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0;
			boolean isBuiltin = (flag & ICSettingEntry.BUILTIN) != 0;

			switch (kind) {
			case ICSettingEntry.INCLUDE_PATH:
				if (isWorkspacePath)
					if (isProjectRelative)
						imageKey = CPluginImages.IMG_OBJS_INCLUDES_FOLDER_PROJECT;
					else
						imageKey = CPluginImages.IMG_OBJS_INCLUDES_FOLDER_WORKSPACE;
				else if (isBuiltin)
					imageKey = CPluginImages.IMG_OBJS_INCLUDES_FOLDER_SYSTEM;
				else
					imageKey = CPluginImages.IMG_OBJS_INCLUDES_FOLDER;
				break;
			case ICSettingEntry.INCLUDE_FILE:
				imageKey = CPluginImages.IMG_OBJS_TUNIT_HEADER;
				break;
			case ICSettingEntry.MACRO:
				imageKey = CPluginImages.IMG_OBJS_MACRO;
				break;
			case ICSettingEntry.MACRO_FILE:
				imageKey = CPluginImages.IMG_OBJS_MACROS_FILE;
				break;
			case ICSettingEntry.LIBRARY_PATH:
				imageKey = CPluginImages.IMG_OBJS_LIBRARY_FOLDER;
				break;
			case ICSettingEntry.LIBRARY_FILE:
				imageKey = CPluginImages.IMG_OBJS_LIBRARY;
				break;
			}
			if (imageKey==null)
				imageKey = CPluginImages.IMG_OBJS_UNKNOWN_TYPE;
		return imageKey;
	}

}
