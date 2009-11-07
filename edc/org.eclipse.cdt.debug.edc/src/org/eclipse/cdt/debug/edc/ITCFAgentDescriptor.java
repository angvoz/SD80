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
package org.eclipse.cdt.debug.edc;

import java.util.List;
import java.util.Map;

/**
 * Provides information about a TCF agent and provides a way to launch it if
 * necessary.
 */
public interface ITCFAgentDescriptor {

	// Some agent attributes
	public static final String DEBUG_SUPPORT = "DebugSupport";

	/**
	 * Gets the user friendly name of this agent
	 * 
	 * @return the name of this agent
	 */
	String getName();

	/**
	 * Get the list of service names this agent implements
	 * 
	 * @return list of service names, may be empty
	 */
	List<String> getServiceNames();

	/**
	 * Get the attributes of this agent
	 * 
	 * @return the agent attributes, which may be empty
	 */
	Map<String, String> getAttributes();

	/**
	 * Launches the agent if it's not already running
	 * 
	 * @throws Exception
	 *             on any error.
	 */
	void launch() throws Exception;

}
