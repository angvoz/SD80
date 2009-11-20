/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import com.ibm.icu.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class UIMessages {
	// Bundle ID
	private static final String BUNDLE_ID = "org.eclipse.cdt.ui.newui.PluginResources"; //$NON-NLS-1$
	//Resource bundle.
	private static ResourceBundle resourceBundle;

	static {
		try {
			resourceBundle = ResourceBundle.getBundle(BUNDLE_ID);
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getString(key), new String[] { arg });
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getString(key), args);
	}

	public static String getString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NullPointerException e) {
			return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private UIMessages() {
		// No constructor
	}

}
