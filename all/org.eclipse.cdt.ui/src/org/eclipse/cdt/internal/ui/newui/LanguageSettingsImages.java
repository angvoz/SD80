package org.eclipse.cdt.internal.ui.newui;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.ui.CUIPlugin;

public class LanguageSettingsImages {
	// The images registry
	private static ImageRegistry imageRegistry = new ImageRegistry(CUIPlugin.getStandardDisplay());
	private static URL fBaseURL = CUIPlugin.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$

	public static final String IMG_OBJS_INCLUDES_FOLDER = "icons/obj16/hfolder_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDES_FOLDER_SYSTEM = "icons/obj16/fldr_sys_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDES_FOLDER_PROJECT = "icons/obj16/hproject.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDES_FOLDER_WORKSPACE = "icons/obj16/wsp_includefolder.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_LANG_SETTINGS_PROVIDER = "icons/obj16/ls_entries_provider.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT_HEADER= "icons/obj16/h_file_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_MACRO= "icons/obj16/define_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_MACROS_FILE= "icons/obj16/macros_file.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_LIBRARY_FOLDER=  "icons/obj16/fldr_lib_obj.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	public static final String IMG_OBJS_LIBRARY= "icons/obj16/lib_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_UNKNOWN_TYPE= "icons/obj16/unknown_type_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CDT_TESTING = "icons/obj16/flask.png"; //$NON-NLS-1$
	public static final String IMG_OBJS_PROJECT = "icons/etool16/prj_obj.gif"; //$NON-NLS-1$
	public static final String IMG_FILESYSTEM = "icons/obj16/filesyst.gif"; //$NON-NLS-1$
	public static final String IMG_WORKSPACE = "icons/obj16/workspace.gif"; //$NON-NLS-1$

	public static final String IMG_OVR_SETTING = "icons/ovr16/setting_nav.gif"; //$NON-NLS-1$
	public static final String IMG_OVR_GLOBAL = "icons/ovr16/global_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_OVR_CONFIGURATION = "icons/ovr16/cfg_ovr.gif"; //$NON-NLS-1$


	private static URL makeIconFileURL(String path) {
		try {
			return new URL(fBaseURL, path);
		} catch (Exception e) {
			CUIPlugin.log(e);
			return null;
		}
	}

	private static ImageDescriptor createManaged(ImageRegistry registry, String projectRelativePath) {
		URL url = makeIconFileURL(projectRelativePath);
		ImageDescriptor result= ImageDescriptor.createFromURL(url);
		String key = url.toString();
		registry.put(key, result);
		return result;
	}

	public static Image get(String key) {
		URL url = makeIconFileURL(key);
		key = url.toString();
		Image image = imageRegistry.get(key);
		if (image==null) {
			createManaged(imageRegistry, key);
			image = imageRegistry.get(key);
		}
		return image;
	}
	/**
	 * Retrieves an overlaid image descriptor from the repository of images.
	 * If there is no image one will be created.
	 *
     * The decoration overlay for the base image will use the array of
     * provided overlays. The indices of the array correspond to the values
     * of the 5 overlay constants defined on {@link IDecoration}
     * ({@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
     * {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
     * or {@link IDecoration#UNDERLAY})
     *
     * @param baseKey the base image key
     * @param overlayKeys the keys for the overlay images
     */
	public static Image getOverlaidImage(String baseKey, String[] overlayKeys) {
		String suffix=""; //$NON-NLS-1$
		for (int i=0;i<5;i++) {
			String overlayKey=""; //$NON-NLS-1$
			if (i<overlayKeys.length && overlayKeys[i]!=null) {
				overlayKey=overlayKeys[i];
			}
			suffix=suffix+'.'+overlayKey;
		}
		if (suffix.equals(".....")) { //$NON-NLS-1$
			Image result = get(baseKey);
			return result;
		}
		String compositeKey=baseKey+suffix;

		Image result = imageRegistry.get(compositeKey);
		if (result==null) {
			result = get(baseKey);
			ImageDescriptor[] overlayDescriptors = new ImageDescriptor[5];
			for (int i=0;i<4;i++) {
				String overlayKey = overlayKeys[i];
				if (overlayKey!=null) {
					Image overlay = get(overlayKey);
					URL url = makeIconFileURL(overlayKey);
					String urlKey = url.toString();
					ImageDescriptor overlayDescriptor = imageRegistry.getDescriptor(urlKey);
					if (overlayDescriptor==null) {
						overlayDescriptor = ImageDescriptor.getMissingImageDescriptor();
					}
					overlayDescriptors[i] = overlayDescriptor;
				}
			}
			ImageDescriptor compositeDescriptor = new DecorationOverlayIcon(result, overlayDescriptors);
			imageRegistry.put(compositeKey, compositeDescriptor);
			result = imageRegistry.get(compositeKey);
		}
		return result;
	}

	/**
	 * Retrieves an overlaid image descriptor from the repository of images.
	 * If there is no image one will be created.
	 *
	 * @param baseKey - key of the base image. Expected to be in repository.
	 * @param overlayKey - key of overlay image. Expected to be in repository as well.
	 * @param quadrant - location of overlay, one of those:
	 *        {@link IDecoration#TOP_LEFT},
	 *        {@link IDecoration#TOP_RIGHT},
	 *        {@link IDecoration#BOTTOM_LEFT},
	 *        {@link IDecoration#BOTTOM_RIGHT}
	 *
	 * @return image overlaid with smaller image in the specified quadrant.
	 */
	public static Image getOverlaidImage(String baseKey, String overlayKey, int quadrant) {
		String[] overlayKeys = new String[4];
		overlayKeys[quadrant]=overlayKey;
		return getOverlaidImage(baseKey, overlayKeys);
	}


	public static Image getImage(int kind, int flags, boolean isProjectRelative) {
		String imageKey = getImageKey(kind, flags, isProjectRelative);
		if (imageKey!=null) {
			String overlayKey = getOverlayKey(kind, flags, isProjectRelative);
			if (overlayKey!=null) {
				return getOverlaidImage(imageKey, overlayKey, IDecoration.TOP_RIGHT);
			}
			return get(imageKey);
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
					imageKey = IMG_OBJS_INCLUDES_FOLDER_PROJECT;
				else
					imageKey = IMG_OBJS_INCLUDES_FOLDER_WORKSPACE;
			else if (isBuiltin)
				imageKey = IMG_OBJS_INCLUDES_FOLDER_SYSTEM;
			else
				imageKey = IMG_OBJS_INCLUDES_FOLDER;
			break;
		case ICSettingEntry.INCLUDE_FILE:
			imageKey = IMG_OBJS_TUNIT_HEADER;
			break;
		case ICSettingEntry.MACRO:
			imageKey = IMG_OBJS_MACRO;
			break;
		case ICSettingEntry.MACRO_FILE:
			imageKey = IMG_OBJS_MACROS_FILE;
			break;
		case ICSettingEntry.LIBRARY_PATH:
			imageKey = IMG_OBJS_LIBRARY_FOLDER;
			break;
		case ICSettingEntry.LIBRARY_FILE:
			imageKey = IMG_OBJS_LIBRARY;
			break;
		}
		if (imageKey==null)
			imageKey = IMG_OBJS_UNKNOWN_TYPE;
		return imageKey;
	}

}
