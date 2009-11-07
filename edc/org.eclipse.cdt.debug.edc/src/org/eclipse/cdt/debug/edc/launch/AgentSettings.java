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
package org.eclipse.cdt.debug.edc.launch;

import java.util.Arrays;

import org.eclipse.cdt.debug.edc.tcf.extension.services.ISettings;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;

public class AgentSettings {

	private final ISettings settingsService;
	private final EDCLaunch launch;

	public AgentSettings(ISettings settingsService, EDCLaunch launch) {
		this.settingsService = settingsService;
		this.launch = launch;
	}

	public ISettings getSettingsService() {
		return settingsService;
	}

	public EDCLaunch getLaunch() {
		return launch;
	}

	public void sendSettingsToAgent() {
		Protocol.invokeAndWait(new Runnable() {

			public void run() {

				settingsService.getSupportedSettings(new ISettings.DoneGetSettingValues() {

					public void doneGetSettingValues(IToken token, Exception error, String[] ids) {
						if (ids.length > 0)
							System.out.println(Arrays.deepToString(ids));
					}
				});
			}
		});
	}

}
