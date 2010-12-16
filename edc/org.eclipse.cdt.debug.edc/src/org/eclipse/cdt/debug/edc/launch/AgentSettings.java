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

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.cdt.debug.edc.tcf.extension.services.ISettings;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.util.TCFTask;

/**
 * @noimplement
 * @noextend
 */
public class AgentSettings {

	private final ISettings settingsService;

	/**
	 * @since 2.0
	 */
	public AgentSettings(ISettings settingsService) {
		this.settingsService = settingsService;
	}

	public ISettings getSettingsService() {
		return settingsService;
	}

	/**
	 * @since 2.0
	 */
	public String[] getSettingIds() throws IOException {
		TCFTask<String[]> task = new TCFTask<String[]>() {

			public void run() {

				settingsService.getIds(new ISettings.DoneGetSettingIds() {

					public void doneGetSettingIds(IToken token, Exception error, String[] ids) {
						if (error != null) {
							error(error);
						} else {
							if (ids.length > 0)
								System.out.println(Arrays.deepToString(ids));
							done(ids);
						}
					}
				});
			}
			
		};
		return task.getIO();
	}

	/**
	 * @since 2.0
	 */
	public void setSettings(final String context, final String[] ids, final Object[] values) throws IOException {
		
		TCFTask<Object> task = new TCFTask<Object>() {

			public void run() {

				settingsService.setValues(context, ids, values, 
				new ISettings.DoneSetSettingValues() {

					public void doneSetSettingValues(IToken token, Exception error) {
						if (error != null)
							error(error);
						else
							done(null);
					}
				});
			}
			
		};
		task.getIO();
	}

}
