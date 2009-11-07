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

/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.tcf.extension;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.core.ChannelTCP;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * ServerTCP is a TCP server that is listening for incoming connection requests
 * and creates TCF communication channels over TCP sockets for such requests.
 * 
 * Clients may create objects of this class to become a TCF server.
 */
public class ServerTCP extends ServerSocket {

	private static class ServerPeer extends AbstractPeer {
		ServerPeer(Map<String, String> attrs) {
			super(attrs);
		}
	}

	private static class RemotePeer extends AbstractPeer {
		RemotePeer(Map<String, String> attrs) {
			super(attrs);
		}
	}

	private final String name;
	private List<ServerPeer> peers;
	private Thread thread;

	public ServerTCP(String name, int port) throws IOException {
		super(port);
		this.name = name;
		peers = new ArrayList<ServerPeer>();
		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		while (e.hasMoreElements()) {
			NetworkInterface f = e.nextElement();
			Enumeration<InetAddress> n = f.getInetAddresses();
			while (n.hasMoreElements()) {
				InetAddress addr = n.nextElement();
				if (shouldCreatePeerOnAddress(addr))
					getLocalPeer(addr.getHostAddress());
			}
		}
		thread = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						final Socket socket = accept();
						Protocol.invokeLater(new Runnable() {
							public void run() {
								try {
									new ChannelTCP(getLocalPeer(socket), getRemotePeer(socket), socket);
								} catch (final Throwable x) {
									Protocol.log("TCF Server: failed to create a channel", x);
								}
							}
						});
					} catch (final Throwable x) {
						Protocol.invokeLater(new Runnable() {
							public void run() {
								Protocol.log("TCF Server thread aborted", x);
							}
						});
						break;
					}
				}
			}
		};
		thread.setName(name);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Check if we want to create a server peer on the given local address. Note
	 * a network host may have several net addresses. By default we create a
	 * peer on each of them. This method is to allow subclass to do certain
	 * filtering, for instance not to create peer on loopback address.
	 * 
	 * @param localAddr
	 * @return
	 */
	protected boolean shouldCreatePeerOnAddress(InetAddress localAddr) {
		return true;
	}

	/**
	 * This is to allow subclass to specify customized attributes for creating
	 * new server peer.
	 * 
	 * @return
	 */
	protected Map<String, String> getCustomPeerAttributes() {
		Map<String, String> attrs = new HashMap<String, String>();
		return attrs;
	}

	private ServerPeer getLocalPeer(String addr) {
		for (ServerPeer p : peers) {
			if (addr.equals(p.getAttributes().get(IPeer.ATTR_IP_HOST)))
				return p;
		}
		// Standard attributes
		//
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(IPeer.ATTR_ID, "TCP:" + addr + ":" + getLocalPort());
		attrs.put(IPeer.ATTR_NAME, name);
		attrs.put(IPeer.ATTR_OS_NAME, System.getProperty("os.name"));
		attrs.put(IPeer.ATTR_TRANSPORT_NAME, "TCP");
		attrs.put(IPeer.ATTR_IP_HOST, addr);
		attrs.put(IPeer.ATTR_IP_PORT, Integer.toString(getLocalPort()));
		attrs.put(IPeer.ATTR_PROXY, "");

		// Custom attributes
		//
		attrs.putAll(getCustomPeerAttributes());

		ServerPeer p = new ServerPeer(attrs);
		peers.add(p);
		return p;
	}

	private IPeer getLocalPeer(Socket socket) {
		return getLocalPeer(socket.getLocalAddress().getHostAddress());
	}

	private IPeer getRemotePeer(Socket socket) {
		String addr = socket.getInetAddress().getHostAddress();
		for (IPeer p : Protocol.getLocator().getPeers().values()) {
			if (addr.equals(p.getAttributes().get(IPeer.ATTR_IP_HOST)))
				return p;
		}
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(IPeer.ATTR_ID, "TCP:" + addr + ":");
		attrs.put(IPeer.ATTR_TRANSPORT_NAME, "TCP");
		attrs.put(IPeer.ATTR_IP_HOST, addr);
		return new RemotePeer(attrs);
	}

	@Override
	public void close() throws IOException {
		if (peers != null) {
			for (ServerPeer s : peers)
				s.dispose();
			peers = null;
		}
		super.close();
		if (thread != null) {
			try {
				thread.join();
				thread = null;
			} catch (InterruptedException e) {
				throw new InterruptedIOException();
			}
		}
	}
}
