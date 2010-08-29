package org.eclipse.cdt.internal.ui.newui;

import java.net.MalformedURLException;
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
	// Subdirectory (under the package containing this class) where 16 color images are
	private static URL fgIconBaseURL;

	static {
		try {
			fgIconBaseURL= new URL(CUIPlugin.getDefault().getBundle().getEntry("/"), "icons/" ); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MalformedURLException e) {
			CUIPlugin.log(e);
		}
	}

	private static final String NAME_PREFIX = CUIPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	private static final String T_OBJ= "obj16/"; //$NON-NLS-1$
	private static final String T_ETOOL= "etool16/"; //$NON-NLS-1$

	public static final String IMG_OBJS_INCLUDES_FOLDER = NAME_PREFIX + "hfolder_obj.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_OBJS_INCLUDES_FOLDER= createManaged(T_OBJ, IMG_OBJS_INCLUDES_FOLDER);

	public static final String IMG_OBJS_INCLUDES_FOLDER_SYSTEM = NAME_PREFIX + "fldr_sys_obj.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_OBJS_INCLUDES_FOLDER_SYSTEM  = createManaged(T_OBJ, IMG_OBJS_INCLUDES_FOLDER_SYSTEM);

	public static final String IMG_OBJS_INCLUDES_FOLDER_PROJECT = NAME_PREFIX + "hproject.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_OBJS_INCLUDES_FOLDER_PROJECT= createManaged(T_OBJ, IMG_OBJS_INCLUDES_FOLDER_PROJECT);

	public static final String IMG_OBJS_INCLUDES_FOLDER_WORKSPACE = NAME_PREFIX + "wsp_includefolder.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_OBJS_INCLUDES_FOLDER_WORKSPACE= createManaged(T_OBJ, IMG_OBJS_INCLUDES_FOLDER_WORKSPACE);

	public static final String IMG_OBJS_LANG_SETTINGS_PROVIDER = NAME_PREFIX + "ls_entries_provider.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_OBJS_LANG_SETTINGS_PROVIDER= createManaged(T_OBJ, IMG_OBJS_LANG_SETTINGS_PROVIDER);

	public static final String IMG_OBJS_TUNIT_HEADER= NAME_PREFIX + "h_file_obj.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_OBJS_TUNIT_HEADER= createManaged(T_OBJ, IMG_OBJS_TUNIT_HEADER);

	public static final String IMG_OBJS_MACRO= NAME_PREFIX + "define_obj.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_OBJS_MACRO= createManaged(T_OBJ, IMG_OBJS_MACRO);

	public static final String IMG_OBJS_MACROS_FILE= NAME_PREFIX + "macros_file.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_OBJS_MACROS_FILE= createManaged(T_OBJ, IMG_OBJS_MACROS_FILE);

	public static final String IMG_OBJS_LIBRARY_FOLDER=  NAME_PREFIX + "fldr_lib_obj.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	private static final ImageDescriptor DESC_OBJS_LIBRARY_FOLDER= createManaged(T_OBJ, IMG_OBJS_LIBRARY_FOLDER);

	public static final String IMG_OBJS_LIBRARY= NAME_PREFIX + "lib_obj.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_OBJS_LIBRARY= createManaged(T_OBJ, IMG_OBJS_LIBRARY);

	public static final String IMG_OBJS_UNKNOWN_TYPE= NAME_PREFIX + "unknown_type_obj.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_OBJS_UNKNOWN_TYPE= createManaged(T_OBJ, IMG_OBJS_UNKNOWN_TYPE);

	public static final String IMG_OBJS_CDT_TESTING = NAME_PREFIX + "flask.png"; //$NON-NLS-1$
    @SuppressWarnings("unused")
	private static final ImageDescriptor DESC_OBJS_CDT_TESTING= createManaged(T_OBJ, IMG_OBJS_CDT_TESTING);

	public static final String IMG_OBJS_PROJECT=NAME_PREFIX + "prj_obj.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_OBJS_PROJECT = createManaged(T_ETOOL, IMG_OBJS_PROJECT);

	public static final String IMG_FILESYSTEM= NAME_PREFIX + "filesyst.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_FILESYSTEM = createManaged(T_OBJ, IMG_FILESYSTEM);
	public static final String IMG_WORKSPACE = NAME_PREFIX + "workspace.gif"; //$NON-NLS-1$
	private static final ImageDescriptor DESC_WORKSPACE  = createManaged(T_OBJ, IMG_WORKSPACE);

	private static ImageDescriptor createManaged(String prefix, String name) {
		return createManaged(imageRegistry, prefix, name);
	}

	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer= new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			CUIPlugin.log(e);
			return null;
		}
	}

	private static ImageDescriptor createManaged(ImageRegistry registry, String prefix, String name) {
		ImageDescriptor result= ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
		registry.put(name, result);
		return result;
	}

	public static Image get(String key) {
		return imageRegistry.get(key);
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
			if (overlayKeys.length<i && overlayKeys[i]!=null) {
				overlayKey=overlayKeys[i];
			}
			suffix=suffix+'.'+overlayKey;
		}
		if (!suffix.equals(".....")) { //$NON-NLS-1$
			suffix=""; //$NON-NLS-1$
		}
		String compositeKey=baseKey+suffix;

		Image result = imageRegistry.get(compositeKey);
		if (result==null) {
			result = imageRegistry.get(baseKey);
			ImageDescriptor[] overlayDescriptors = new ImageDescriptor[5];
			for (int i=0;i<4;i++) {
				String key = overlayKeys[i];
				if (key!=null) {
					ImageDescriptor overlayDescriptor = imageRegistry.getDescriptor(key);
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
