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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;

/**
 * This interface provides access to TCF services. It abstracts out the details
 * of which agent provides the services, launching the agent if necessary, etc.
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
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
	
	/**
	 * Find an open channel or create a new one and return it.  This blocks,
	 * and should not be called from the TCF dispatch thread.
	 * @param peer the peer
	 * @return the channel used to communicate to the specified peer, in open state
	 * @since 2.0
	 */
	public IChannel findOrCreateChannelForPeer(IPeer peer) throws CoreException;
	
	/** 
	 * Add a listener. Duplicate listeners are ignored. 
	 * @param listener 
	 * @since 2.0 
	 */
	public void addConnectionListener(ITCFConnectionListener listener);
	/** 
	 * Remove a listener. Nonexistent listeners are ignored.
	 * @param listener 
	 * @since 2.0 
	 */
	public void removeConnectionListener(ITCFConnectionListener listener);
}
