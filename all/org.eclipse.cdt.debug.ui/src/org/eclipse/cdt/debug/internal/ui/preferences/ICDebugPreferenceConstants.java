/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * Constants defining the keys to be used for accessing preferences
 * inside the debug ui plugin's preference bundle.
 *
 * In descriptions (of the keys) below describe the preference 
 * stored at the given key. The type indicates type of the stored preferences
 *
 * The preference store is loaded by the plugin (CDebugUIPlugin).
 * @see CDebugUIPlugin.initializeDefaultPreferences(IPreferenceStore) - for initialization of the store
 * 
 * @since Jul 23, 2002
 */
public interface ICDebugPreferenceConstants
{
	/**
	 * Boolean preference controlling whether the debugger shows 
	 * full paths. When <code>true</code> the debugger
	 * will show full paths in newly opened views.
	 */
	public static final String PREF_SHOW_FULL_PATHS = ICDebugUIConstants.PLUGIN_ID + "cDebug.show_full_paths";

	/**
	 * The RGB for the color to be used to indicate changed registers
	 */
	public static final String CHANGED_REGISTER_RGB = "Changed.Register.RGB"; //$NON-NLS-1$
	
	/**
	 * The default values for the memory view parameters. 
	 */
	public static final String DEFAULT_MEMORY_PADDING_CHAR = ".";
	public static final FontData DEFAULT_MEMORY_FONT = Display.getDefault().getSystemFont().getFontData()[0];

	public static final RGB DEFAULT_MEMORY_FOREGROUND_RGB = Display.getCurrent().getSystemColor( SWT.COLOR_LIST_FOREGROUND ).getRGB();
	public static final RGB DEFAULT_MEMORY_BACKGROUND_RGB = Display.getCurrent().getSystemColor( SWT.COLOR_LIST_BACKGROUND ).getRGB();
	public static final RGB DEFAULT_MEMORY_ADDRESS_RGB = Display.getCurrent().getSystemColor( SWT.COLOR_DARK_GRAY ).getRGB();
	public static final RGB DEFAULT_MEMORY_CHANGED_RGB = Display.getCurrent().getSystemColor( SWT.COLOR_RED ).getRGB();
	public static final RGB DEFAULT_MEMORY_DIRTY_RGB = Display.getCurrent().getSystemColor( SWT.COLOR_BLUE ).getRGB();

	public static final String PREF_MEMORY_NUMBER_OF_BYTES = "Memory.NumberOfBytes";
	public static final String PREF_MEMORY_SIZE = "Memory.Size";
	public static final String PREF_MEMORY_FORMAT = "Memory.Format";
	public static final String PREF_MEMORY_BYTES_PER_ROW = "Memory.BytesPerRow";
	public static final String PREF_MEMORY_PADDING_CHAR = "Memory.PaddingChar";

	/**
	 * The RGB for the memory text foreground color
	 */
	public static final String MEMORY_FOREGROUND_RGB = "Memory.Foreground.RGB"; //$NON-NLS-1$

	/**
	 * The RGB for the memory text background color
	 */
	public static final String MEMORY_BACKGROUND_RGB = "Memory.background.RGB"; //$NON-NLS-1$

	/**
	 * The RGB for the color to be used to indicate address values in the memory view
	 */
	public static final String MEMORY_ADDRESS_RGB = "Memory.Address.RGB"; //$NON-NLS-1$

	/**
	 * The RGB for the color to be used to indicate changed values in the memory view
	 */
	public static final String MEMORY_CHANGED_RGB = "Memory.Changed.RGB"; //$NON-NLS-1$

	/**
	 * The RGB for the color to be used to indicate dirty values in the memory view
	 */
	public static final String MEMORY_DIRTY_RGB = "Memory.Dirty.RGB"; //$NON-NLS-1$

	/**
	 * The name of the font to use for the memory view
	 */
	public static final String MEMORY_FONT = "Memory.font"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether primitive types
	 * types display hexidecimal values.
	 */
	public static final String PREF_SHOW_HEX_VALUES = ICDebugUIConstants.PLUGIN_ID + "cDebug.showHexValues"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether primitive types
	 * types display char values.
	 */
	public static final String PREF_SHOW_CHAR_VALUES = ICDebugUIConstants.PLUGIN_ID + "cDebug.showCharValues"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether the memory view shows ASCII characters. 
	 * When <code>true</code> the memory view will show ASCII characters by default.
	 */
	public static final String PREF_MEMORY_SHOW_ASCII = ICDebugUIConstants.PLUGIN_ID + "Memory.show_ascii"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether the memory view will be refreshed 
	 * every time when the execution of program stops. 
	 * When <code>true</code> the 'Auto-Refresh' option will be checked.
	 */
	public static final String PREF_MEMORY_AUTO_REFRESH = ICDebugUIConstants.PLUGIN_ID + "Memory.auto_refresh"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether the shared libraries view will be
	 * refreshed every time when the execution of program stops. When
	 * <code>true</code> the 'Auto-Refresh' option will be checked.
	 */
	public static final String PREF_SHARED_LIBRARIES_AUTO_REFRESH = ICDebugUIConstants.PLUGIN_ID + "SharedLibraries.auto_refresh"; //$NON-NLS-1$
}
