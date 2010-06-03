/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui.launch;

import org.eclipse.cdt.debug.edc.internal.ui.EDCDebugUI;

/**
 * Debugger tab for Attach launch configuration type.
 */
public class AttachDebuggerTab extends AbstractDebuggerTab {
	private static final String TAB_ID = EDCDebugUI.PLUGIN_ID + ".launch.attachDebuggerTab";
	private static final String HELP_ID = EDCDebugUI.PLUGIN_ID + ".launch_attachDebuggerTab";

	public AttachDebuggerTab() {
		super(true);
	}

	@Override
	protected String getHelpID() {
		return HELP_ID;
	}

	@Override
	protected String getTabID() {
		return TAB_ID;
	}
}
