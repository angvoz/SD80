package org.eclipse.cdt.internal.ui.newui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingPathEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.ui.CDTSharedImages;

public class LanguageSettingsImages {
	public static Image getImage(int kind, int flags, boolean isProjectRelative) {
		String imageKey = getImageKey(kind, flags, isProjectRelative);
		if (imageKey!=null) {
//			String overlayKey = getErrorOverlayKey(kind, flags, isProjectRelative);
//			if (overlayKey!=null) {
//				return getOverlaidImage(imageKey, overlayKey, IDecoration.BOTTOM_LEFT);
//			}
			return CDTSharedImages.getImage(imageKey);
		}
		return null;
	}

//	public static Image getImage(int kind, int flags, boolean isProjectRelative) {
	public static Image getImage(ICLanguageSettingEntry entry) {
		int kind = entry.getKind();
		boolean isWorkspacePath = (entry.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0;
		String path = entry.getName();
		boolean isProjectRelative = isWorkspacePath && !path.startsWith("/");
		int flags = entry.getFlags();
		String imageKey = getImageKey(kind, flags, isProjectRelative);
		if (imageKey!=null) {
			if (entry instanceof ICLanguageSettingPathEntry) {
				boolean exists = true;
				boolean resolved = (flags & ICSettingEntry.RESOLVED) ==  ICSettingEntry.RESOLVED;
				if (isWorkspacePath) {
					// TODO: Hmm, MBS supplies unresolved entries having location=null
					if (resolved) {
						IPath location = ((ICLanguageSettingPathEntry) entry).getLocation();
						exists = location!=null && location.toFile().exists();
					}
				} else {
					java.io.File file = new java.io.File(path);
					exists = file.exists();
				}
				if (!exists) {
					return CDTSharedImages.getImageOverlaid(imageKey, CDTSharedImages.IMG_OVR_WARNING, IDecoration.BOTTOM_LEFT);
				}
			}
			return CDTSharedImages.getImage(imageKey);
		}
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
					imageKey = CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER_PROJECT;
				else
					imageKey = CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER_WORKSPACE;
			else if (isBuiltin)
				imageKey = CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER_SYSTEM;
			else
				imageKey = CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER;
			break;
		case ICSettingEntry.INCLUDE_FILE:
			imageKey = CDTSharedImages.IMG_OBJS_TUNIT_HEADER;
			break;
		case ICSettingEntry.MACRO:
			imageKey = CDTSharedImages.IMG_OBJS_MACRO;
			break;
		case ICSettingEntry.MACRO_FILE:
			imageKey = CDTSharedImages.IMG_OBJS_MACROS_FILE;
			break;
		case ICSettingEntry.LIBRARY_PATH:
			imageKey = CDTSharedImages.IMG_OBJS_LIBRARY_FOLDER;
			break;
		case ICSettingEntry.LIBRARY_FILE:
			imageKey = CDTSharedImages.IMG_OBJS_LIBRARY;
			break;
		}
		if (imageKey==null)
			imageKey = CDTSharedImages.IMG_OBJS_UNKNOWN_TYPE;
		return imageKey;
	}

}
