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

package org.eclipse.cdt.debug.edc.examples.javaagent.remote;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * @author LWang
 * 
 */
public interface ICalculator extends IService {
	/**
	 * This service name, as it appears on the wire - a TCF name of the service.
	 */
	public static final String NAME = "Calculator";

	/**
	 * @param s
	 *            - any string.
	 * @param done
	 *            - command result call back object.
	 * @return - pending command handle.
	 */
	IToken increment(int s, DoneIncrement done);

	/**
	 * Call back interface for 'increment' command.
	 */
	interface DoneIncrement {
		/**
		 * @param token
		 *            - command handle.
		 * @param error
		 *            - error object or null.
		 * @param i
		 *            - same string as the command argument.
		 */
		void done(IToken token, Throwable error, int i);
	}
}
