/*******************************************************************************
 * Copyright (c) 2002, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.osgi.util.NLS;

public final class ConsoleMessages extends NLS {

	static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.buildconsole.ConsoleMessages";//$NON-NLS-1$

	private ConsoleMessages() {
		// Do not instantiate
	}

	public static String find_replace_action_label;
	public static String find_replace_action_tooltip;
	public static String find_replace_action_image;
	public static String find_replace_action_description;
	public static String BuildConsolePage__Copy_Ctrl_C_6;
	public static String BuildConsolePage_Copy_7;
	public static String BuildConsolePage_Select__All_Ctrl_A_12;
	public static String BuildConsolePage_Select_All;
	public static String ScrollLockAction_Scroll_Lock_1;

	static {
		NLS.initializeMessages(BUNDLE_NAME, ConsoleMessages.class);
	}
}