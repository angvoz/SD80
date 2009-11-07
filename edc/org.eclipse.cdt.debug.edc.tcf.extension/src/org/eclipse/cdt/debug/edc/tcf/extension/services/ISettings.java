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
package org.eclipse.cdt.debug.edc.tcf.extension.services;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * Interface to allow access to settings used by this agent.
 * 
 */
public interface ISettings extends IService {

	static final String NAME = "Settings";

	IToken getSupportedSettings(DoneGetSettingValues done);

	/**
	 * Client call back interface for getSettingValue().
	 */
	interface DoneGetSettingValues {
		/**
		 * Called when setting value retrieval is done.
		 * 
		 * @param error
		 *            TODO
		 * @param value
		 *            – Current value of the setting.
		 */
		void doneGetSettingValues(IToken token, Exception error, String[] ids);
	}

	IToken setValues(String context, String[] ids, Object[] values);

}
