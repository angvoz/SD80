/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui.launch;

public class RegularDebuggerTab extends AbstractDebuggerTab {
	private static final String TAB_ID = "org.eclipse.cdt.debug.edc.ui.launch.regularDebuggerTab";
	private static final String HELP_ID = "org.eclipse.cdt.debug.edc.ui.launch_regularDebuggerTab";

	public RegularDebuggerTab() {
		super(false);
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
