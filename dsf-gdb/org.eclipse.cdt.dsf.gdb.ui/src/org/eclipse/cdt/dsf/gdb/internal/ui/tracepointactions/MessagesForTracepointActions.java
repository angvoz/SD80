/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepointactions;

import org.eclipse.osgi.util.NLS;

/**
 * Preference strings.
 * @since 2.1
 */
class MessagesForTracepointActions extends NLS {
	private static final String BUNDLE_NAME= "org.eclipse.cdt.dsf.gdb.internal.ui.tracepointactions.messages"; //$NON-NLS-1$

	public static String TracepointActions_Actions_for_this_tracepoint;
	public static String TracepointActions_Available_actions;
	public static String TracepointActions_Name;
	public static String TracepointActions_Type;
	public static String TracepointActions_Summary;
	public static String TracepointActions_Attach;
	public static String TracepointActions_New;
	public static String TracepointActions_Edit;
	public static String TracepointActions_Delete;
	public static String TracepointActions_Remove;
	public static String TracepointActions_Up;
	public static String TracepointActions_Down;
	public static String TracepointActions_Preferences_Actions_Available;
	public static String TracepointActions_Step_Count;
	public static String TracepointActions_ActionDialog_New;
	public static String TracepointActions_ActionDialog_Name;
	public static String TracepointActions_ActionDialog_Type;
	public static String TracepointActions_Collect_Label;
	public static String TracepointActions_Evaluate_Label;
	public static String TracepointActions_WhileStepping_Sub_Actions;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, MessagesForTracepointActions.class);
	}

	private MessagesForTracepointActions() {
	}
}
