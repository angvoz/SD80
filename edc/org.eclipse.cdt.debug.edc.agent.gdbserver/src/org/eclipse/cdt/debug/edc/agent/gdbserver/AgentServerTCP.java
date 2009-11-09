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

package org.eclipse.cdt.debug.edc.agent.gdbserver;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.edc.tcf.extension.ServerTCP;

/**
 * This class is to customize local peer for our agent.
 * 
 * @author LWang
 * 
 */
public class AgentServerTCP extends ServerTCP {

	/**
	 * @param name
	 * @param port
	 * @throws IOException
	 */
	public AgentServerTCP(String name, int port) throws IOException {
		super(name, port);
	}

	@Override
	protected Map<String, String> getCustomPeerAttributes() {
		// Specify any custom/special attributes for the agent.
		//
		Map<String, String> attrs = new HashMap<String, String>();

		attrs.put("DebugSupport", GdbserverAgent.ATTR_DEBUG_SUPPPORT);

		return attrs;
	}

	@Override
	// Currently we use this to make sure only one TCF peer is created
	// for the agent on the host where the agent is running.
	//
	protected boolean shouldCreatePeerOnAddress(InetAddress localAddr) {
		if (localAddr.isLoopbackAddress())
			return false;

		// Don't support this yet.
		//
		if (localAddr instanceof Inet6Address)
			return false;
		return true;
	}
}
