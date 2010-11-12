package org.eclipse.cdt.internal.ui.newui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.settings.model.ACPathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;

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

	public static Image getImage(ICLanguageSettingEntry entry) {
		int kind = entry.getKind();
		boolean isWorkspacePath = (entry.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0;
		String path = entry.getName();
		boolean isProjectRelative = isWorkspacePath && !path.startsWith("/");
		int flags = entry.getFlags();
		String imageKey = getImageKey(kind, flags, isProjectRelative);
		if (imageKey!=null) {
			if (entry instanceof ACPathEntry) {
				String overlayKey=null;
				IStatus status = getStatus(entry);
				switch (status.getSeverity()) {
				case IStatus.ERROR:
					overlayKey = CDTSharedImages.IMG_OVR_ERROR;
					break;
				case IStatus.WARNING:
					overlayKey = CDTSharedImages.IMG_OVR_WARNING;
					break;
				case IStatus.INFO:
					overlayKey = CDTSharedImages.IMG_OVR_WARNING;
					break;
				}
				return CDTSharedImages.getImageOverlaid(imageKey, overlayKey, IDecoration.BOTTOM_LEFT);
//				boolean exists = isLocationOk((ACPathEntry) entry);
//				if (!exists) {
//					return CDTSharedImages.getImageOverlaid(imageKey, severity, IDecoration.BOTTOM_LEFT);
//				}
			}
			return CDTSharedImages.getImage(imageKey);
		}
		return null;
	}

	private static boolean isLocationOk(ACPathEntry entry) {
		boolean exists = true;
		boolean resolved = (entry.getFlags() & ICSettingEntry.RESOLVED) == ICSettingEntry.RESOLVED;
		boolean isWorkspacePath_FIXME = (entry.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0;
		if (isWorkspacePath_FIXME) {
			// TODO: Hmm, MBS supplies unresolved entries having location=null
			if (resolved) {
				IPath location = entry.getLocation();
				exists = location!=null && location.toFile().exists();
			} else {
				// AG: this does not work
//						exists = false;
			}
		} else {
			String pathname = entry.getName();
			java.io.File file = new java.io.File(pathname);
			exists = file.exists();
		}
		return exists;
	}
	
	public static IStatus getStatus(ICLanguageSettingEntry entry) {
		if (entry instanceof ACPathEntry) {
			ACPathEntry acEntry = (ACPathEntry)entry;
			IPath path = new Path(acEntry.getName());
			if (!path.isAbsolute()) {
				String msg = "Using relative paths is not recommended. This can cause unexpected side-effects.";
				return new Status(IStatus.INFO, CUIPlugin.PLUGIN_ID, msg);
			}
			if (!isLocationOk(acEntry)) {
				String msg;
				if (acEntry.isFile())
					msg = "The selected file does not exist.";
				else
					msg = "The selected folder does not exist.";
				return new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, msg);
			}
				
		}
		return Status.OK_STATUS;
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
