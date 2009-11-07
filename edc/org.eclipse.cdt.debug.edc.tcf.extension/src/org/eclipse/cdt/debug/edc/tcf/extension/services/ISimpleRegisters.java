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

public interface ISimpleRegisters extends IService {

	static final String NAME = "SimpleRegisters";

	IToken get(String executionContextID, String[] registerIDs, DoneGet done);

	IToken set(String executionContextID, String[] registerIDs, String[] registerValues, DoneSet done);

	/**
	 * 'get' command call back interface.
	 */
	interface DoneGet {

		void doneGet(IToken token, Exception error, String[] values);
	}

	/**
	 * 'set' command call back interface.
	 */
	interface DoneSet {

		void doneSet(IToken token, Exception error, String[] values);
	}

}
