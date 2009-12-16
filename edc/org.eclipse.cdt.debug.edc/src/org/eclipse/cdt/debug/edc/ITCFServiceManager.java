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

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;

/*
/**
 * This interface provides access to TCF services. It abstracts out the details
 * of which agent provides the services, launching the agent if necessary, etc.
 */
public interface ITCFServiceManager {

	/**
	 * Gets the channel used to talk to a peer. If no channel is open yet it
	 * will return null.
	 * 
	 * @param peer
	 *            the peer
	 * @return the channel used to communicate to the specified peer
	 */

	public IChannel getChannelForPeer(IPeer peer);
}
