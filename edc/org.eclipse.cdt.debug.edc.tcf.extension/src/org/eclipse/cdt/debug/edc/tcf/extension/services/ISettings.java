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

	/**
	 * Get the supported setting ids.
	 * @since 2.0
	 */
	IToken getIds(DoneGetSettingIds done);

	/**
	 * Client callback interface for getIds().
	 * @since 2.0
	 */
	interface DoneGetSettingIds {
		/**
		 * Called when setting id retrieval is done.
		 */
		void doneGetSettingIds(IToken token, Exception error, String[] ids);
	}

	/**
	 * Set values for one or more setting ids.  An agent should accept
	 * unknown ids without error but may issue errors when values are 
	 * not of the expected type.
	 * @since 2.0
	 * @param ids array of setting ids
	 * @param values array of values, parallel in structure to ids
	 */
	IToken setValues(String context, String[] ids, Object[] values, DoneSetSettingValues done);

	/**
	 * Client callback interface for setValues().
	 * @since 2.0
	 */
	interface DoneSetSettingValues {
		/**
		 * Called when settings are sent and acknowledged.
		 */
		void doneSetSettingValues(IToken token, Exception error);
	}
}
