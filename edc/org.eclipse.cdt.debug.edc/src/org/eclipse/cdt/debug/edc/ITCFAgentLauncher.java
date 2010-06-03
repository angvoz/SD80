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
package org.eclipse.cdt.debug.edc;

import java.util.List;
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IPeer;

/**
 * An implementation of this interface is provided by a tcgAgentLauncher
 * extension. It's a way to advertise a TCF agent that may not yet be running
 * and to provide a means to programatically launch it (or at least put up some
 * GUI that informs the user how to manually launch it, in the case of an agent
 * hosted remotely). Agents can advertise themselves once they are running via
 * TCF UDP Discovery, but we need a way for a debugger to discover and launch
 * agents that are not yet running. This interface assumes the agent hosts a
 * single peer. An agent that hosts multiple peers can be described using
 * multiple launchers with common launch logic.
 */
public interface ITCFAgentLauncher {

	/**
	 * Gets the user friendly name of the peer this agent hosts. Same as calling
	 * getAttributes().get(IPeer#ATTR_NAME)
	 * 
	 * @return the name of the peer
	 */
	String getPeerName();

	/**
	 * Get the names of the services the peer implements.
	 * 
	 * @return list of service names, may be empty
	 */
	List<String> getServiceNames();

	/**
	 * Get the attributes of the peer this agent hosts.
	 * 
	 * @return the peer's attributes; at least the {@link IPeer#ATTR_NAME} will
	 *         be present
	 */
	Map<String, String> getPeerAttributes();

	/**
	 * Tell whether the agent can be launched.  This is mainly used to
	 * avoid considering the launcher for situations where it will never
	 * work (e.g., wrong OS host).  {@link #launch()} can, of course,
	 * fail for other reasons.
	 * @return true if launching is possible.
	 */
	boolean isLaunchable();
	
	/**
	 * Launches the agent, if it's not already running
	 * 
	 * @throws Exception
	 *             on any error.
	 */
	void launch() throws Exception;
	
	
	/**
	 * Shuts down the agent if it was launched
	 * 
	 * @throws Exception
	 * 				on any error
	 */
	void shutdown() throws Exception;

}
