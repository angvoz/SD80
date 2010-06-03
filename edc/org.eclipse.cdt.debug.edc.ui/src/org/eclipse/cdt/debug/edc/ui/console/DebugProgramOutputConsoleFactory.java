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

package org.eclipse.cdt.debug.edc.ui.console;

public class DebugProgramOutputConsoleFactory extends
		AbstractLoggingConsoleFactory {

	public static final String CONSOLE_TYPE = "DebugProgramOutputConsoleFactory";
	public static final String LOG_ID = "ProgramOutputConsoleLogger";
	public static final String CONSOLE_TITLE = "Program Output Console";

	@Override
	protected String getConsoleTitle() {
		return CONSOLE_TITLE;
	}

	@Override
	protected String getConsoleType() {
		return CONSOLE_TYPE;
	}

	@Override
	protected String getLogId() {
		return LOG_ID;
	}

}
