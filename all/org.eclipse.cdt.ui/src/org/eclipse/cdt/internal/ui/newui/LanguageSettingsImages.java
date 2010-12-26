/*******************************************************************************
 * Copyright (c) 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.newui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.settings.model.ACPathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICPathEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Helper class to provide unified images for {@link ICLanguageSettingEntry}.
 */
public class LanguageSettingsImages {
	/**
	 * Returns image for the given entry from internally managed repository including
	 * necessary overlays.  Note that the images are managed by internal registry in
	 * {@link CDTSharedImages} so they must not be disposed.
	 * This method is shortcut for {@link #getImage(ICLanguageSettingEntry, IProject)}
	 * when no project is available.
	 *
	 * @param entry - language settings entry to get an image for.
	 * @return the image for the entry with appropriate overlays. Returns default
	 *   image if image descriptor is missing as specified by {@link CDTSharedImages#getImage(String)}
	 */
	public static Image getImage(ICLanguageSettingEntry entry) {
		return getImage(entry, null);
	}

	/**
	 * Returns image for the given entry from internally managed repository including
	 * necessary overlays. Note that the images are managed by internal registry in
	 * {@link CDTSharedImages} so they must not be disposed.
	 *
	 * @param entry - language settings entry to get an image for.
	 * @param project - pass project to test if the path entry points to a resource
	 *    in this project. That lets to put "project" metaphor on the image.
	 *    Pass {@code null} if no project is available or "project" overlay is desired.
	 *    This parameter is meaningful for path entries only.
	 * @return the image for the entry with appropriate overlays. Returns default
	 *   image if image descriptor is missing as specified by {@link CDTSharedImages#getImage(String)}
	 */
	public static Image getImage(ICLanguageSettingEntry entry, IProject project) {
		boolean useProjectRelativeOverlay = false;
		boolean isWorkspacePath = false;
		boolean isBuiltin = entry.isBuiltIn();

		if (project!=null && (entry instanceof ICPathEntry)) {
			ICPathEntry pathEntry = (ICPathEntry)entry;
			isWorkspacePath = pathEntry.isValueWorkspacePath();
			if (isWorkspacePath) {
				IResource rc = ResourcesPlugin.getWorkspace().getRoot().findMember(pathEntry.getFullPath());
				useProjectRelativeOverlay = (rc!=null) && project.equals(rc.getProject());
			}
		}

		int kind = entry.getKind();
		String imageKey = getImageKey(kind, useProjectRelativeOverlay, isWorkspacePath, isBuiltin);
		if (imageKey!=null) {
			if (entry instanceof ICPathEntry) {
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
			}
			return CDTSharedImages.getImage(imageKey);
		}
		return null;
	}

	/**
	 * Checking if the entry points to existing or accessible location.
	 * This method should not be called
	 */
	private static boolean isLocationOk(ACPathEntry entry) {
		boolean exists = true;
		if (entry.isValueWorkspacePath()) {
			IPath path = entry.getFullPath();
			IResource rc = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			exists = rc!=null && rc.isAccessible();
		} else {
			IPath path = new Path(entry.getName());
			java.io.File file = path.toFile();
			exists = file.exists();
		}
		return exists;
	}

	/**
	 * Defines status object for the status message line.
	 *
	 * @param entry - the entry to check status on.
	 * @return a status object defining severity and message.
	 */
	public static IStatus getStatus(ICLanguageSettingEntry entry) {
		if (entry instanceof ACPathEntry) {
			// have to trust paths which contain variables
			if (entry.getName().contains("${")) //$NON-NLS-1$
				return Status.OK_STATUS;

			ACPathEntry acEntry = (ACPathEntry)entry;
			IPath path = new Path(acEntry.getName());
			if (path.isAbsolute()) {
				if (!isLocationOk(acEntry)) {
					String msg;
					if (acEntry.isFile())
						msg = Messages.LanguageSettingsImages_FileDoesNotExist;
					else
						msg = Messages.LanguageSettingsImages_FolderDoesNotExist;
					return new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, msg);
				}
			} else {
				String msg = Messages.LanguageSettingsImages_RelativePathsAmbiguous;
				return new Status(IStatus.INFO, CUIPlugin.PLUGIN_ID, msg);
			}

		}
		return Status.OK_STATUS;
	}

	/**
	 * @return the base key for the image.
	 */
	private static String getImageKey(int kind, boolean isProjectRelative, boolean isWorkspacePath, boolean isBuiltin) {
		String imageKey = null;

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
