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

package org.eclipse.cdt.debug.edc.tcf.extension;

/**
 * Checked exception used only within a TCF agent. One main purpose is to
 * support cascading of error messages along a call chain.
 * 
 * @author LWang
 * 
 */
public class AgentException extends Exception {

	private static final long serialVersionUID = 8073163639364456392L;

	private final String fMessage;

	/**
	 * @param fMessage
	 */
	public AgentException(String message) {
		super(message);
		fMessage = null;
	}

	/**
	 * @param cause
	 */
	public AgentException(Throwable cause) {
		super(cause);
		fMessage = null;
	}

	/**
	 * @param fMessage
	 * @param cause
	 */
	public AgentException(String message, Throwable cause) {
		super(message, cause);
		fMessage = (cause != null) ? message + "\n=>Underlying cause: " + cause.getMessage() : null;
	}

	@Override
	public String getMessage() {
		return (fMessage != null) ? fMessage : super.getMessage();
	}
}
