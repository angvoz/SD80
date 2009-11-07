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

package org.eclipse.cdt.debug.edc.windows.launch;

import org.eclipse.cdt.debug.edc.ui.console.AbstractLoggingConsoleFactory;

public class WindowsProgramOutputConsoleFactory extends AbstractLoggingConsoleFactory {

	static final String CONSOLE_TYPE = "WindowsProgramOutputConsoleFactory";
	static final String LOG_ID = "org.eclipse.cdt.debug.edc.windows.launch.ConsoleLogger";;
	static final String CONSOLE_TITLE = "Windows Program Output Console";

	@Override
	protected String getConsoleType() {
		return CONSOLE_TYPE;
	}

	@Override
	protected String getLogId() {
		return LOG_ID;
	}

	@Override
	protected String getConsoleTitle() {
		return CONSOLE_TITLE;
	}
}
