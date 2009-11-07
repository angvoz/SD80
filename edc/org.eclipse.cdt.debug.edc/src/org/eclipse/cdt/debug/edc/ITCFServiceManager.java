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

import java.util.Collection;
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IService;

/**
 * This interface provides access to TCF services. It abstracts out the details
 * of which agent provides the services, launching the agent if necessary, etc.
 */
public interface ITCFServiceManager {

	/**
	 * Gets the agents that implement the given service and also match desired
	 * set of attributes
	 * 
	 * @param serviceName
	 *            the name of the service
	 * @param attributesToMatch
	 *            the desired attributes of the service
	 * @return the list of agent identifiers (which may be empty). If there was
	 *         more than one match, the client should decide which one it wants
	 *         to use, and get the actual service by calling
	 *         {@link ITCFServiceManager#useService(String, String, IServiceCallback)}
	 * @throws Exception
	 */
	Collection<String> getAgents(String serviceName, Map<String, String> attributesToMatch) throws Exception;

	/**
	 * Gets the service from the unique service name (asynchronous)
	 * 
	 * @param agentId
	 *            the id of the agent implementing the service which can be
	 *            obtained from
	 *            {@link ITCFServiceManager#getAgents(String, Map)}
	 * @param serviceName
	 *            the name of the service
	 * @param callback
	 *            this will be called when the service is ready for use
	 * @throws throws a core exception if the agent cannot be found, or does not
	 *         implement the given service
	 * @throws Exception
	 */
	IService getAgentService(String agentId, String serviceName) throws Exception;

	/**
	 * Gets the channel used to talk to an agent. If no channel is open yet it
	 * will return null.
	 * 
	 * @param agentID
	 *            ID for the Agent
	 * @return the channel used by that service
	 */

	IChannel getAgentChannel(String agentID);

}
