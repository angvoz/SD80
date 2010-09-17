/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
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

/**
 * This listener on {@link ITCFServiceManager} allows a client to track the
 * state of peers and channels managed by EDC.
 * @since 2.0
 */
public interface ITCFConnectionListener {

	/** 
	 * Called when ITCFServiceManager opens a channel on a peer.
	 * This is called on the TCF dispatch thread. 
	 * */
	void peerChannelOpened(IPeer peer, IChannel channel);
	
	/** 
	 * Called when a channel was closed for a peer.
	 * This is called on the TCF dispatch thread. 
	 */
	void peerChannelClosed(IPeer peer, IChannel channel, Throwable error);
}
