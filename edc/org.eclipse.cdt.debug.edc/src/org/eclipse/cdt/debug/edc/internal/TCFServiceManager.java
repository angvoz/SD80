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
package org.eclipse.cdt.debug.edc.internal;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.edc.ITCFAgentLauncher;
import org.eclipse.cdt.debug.edc.ITCFServiceManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.tcf.util.TCFTask;

/**
 * Utility class that provides access to TCF agents and services. It abstracts
 * out the details of which agent provides the services, launching the agent if
 * necessary, etc.
 */
public class TCFServiceManager implements ITCFServiceManager  {

	/**
	 * The IP addresses of the local machine. Typically, there's at least two
	 * (the loopback address is one of them), but there can be more if there are
	 * multiple network adapters (physical or virtual).
	 * 
	 * <p>
	 * TODO: if you look at the TCF Java reference implementation, it updates
	 * its list every so often, as a system's network configuration can change
	 * during the life of a process. We should probably do that, too, though
	 * it's clearly an edge case.
	 */
	private static List<String> localIPAddresses;
	
	private List<ITCFAgentLauncher> tcfAgentLaunchers;

	private static final String EXTENSION_POINT_NAME = "tcfAgentLauncher";

	private List<ITCFAgentLauncher> launchedtcfAgentLaunchers;

	static {
		// record local host IP addresses
		try {
			localIPAddresses = getLocalIPAddresses();
		} catch (CoreException e) {
			EDCDebugger.getMessageLogger().logError("Problem getting local IP addresses", e); //$NON-NLS-1$
		}
	}

	public TCFServiceManager() {
		// load TCFAgentLauncher extensions
		tcfAgentLaunchers = new ArrayList<ITCFAgentLauncher>();
		launchedtcfAgentLaunchers = new ArrayList<ITCFAgentLauncher>();

		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EDCDebugger.PLUGIN_ID, EXTENSION_POINT_NAME);
		IExtension[] extensions = extensionPoint.getExtensions();

		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			IConfigurationElement element = elements[0];

			boolean failed = false;
			CoreException exc = null;
			try {
				Object extObject = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (extObject instanceof ITCFAgentLauncher) {
					tcfAgentLaunchers.add((ITCFAgentLauncher) extObject);
				} else {
					failed = true;
				}
			} catch (CoreException e) {
				failed = true;
				exc = e;
			}

			if (failed) {
				EDCDebugger.getMessageLogger().logError(
						"Unable to load " + EXTENSION_POINT_NAME + " extension from " + extension.getContributor().getName(), exc);
			}
		}

	}

	/**
	 * Returns true if <i>all</i> the attributes in [attributesToMatch] appear
	 * identically in [attributes] (keys and respective values). Basically, is
	 * [attributesToMatch] a subset of [attributes]?
	 */
	public static boolean matchesAllAttributes(Map<String, String> attributes, Map<String, String> attributesToMatch) {
		if (attributesToMatch.isEmpty())
			return false;
		
		for (String key : attributesToMatch.keySet()) {
			if (!attributes.containsKey(key)) {
				return false;
			}
			if (!attributesToMatch.get(key).equals(attributes.get(key))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check if the given TCF peer is the LocalPeer defined in TCF. As that
	 * LocalPeer is not public, we check by its internal ID. It may not be
	 * forward compatible, but is there a better way ?
	 * 
	 * @param p
	 * @return
	 */
	public static boolean isInternalLocalPeer(IPeer p) {
		assert Protocol.isDispatchThread();
		return p.getID().equals("TCFLocal");
	}

	/**
	 * Find any registered TCF agent-launchers that will (should) produce a peer
	 * with the given attributes and that exposes the given service. The
	 * agent-launchers are registered through an EDC extension point.
	 * 
	 * @param serviceName
	 *            the required service
	 * @param attributesToMatch
	 *            the required peer attributes
	 * @return zero or more agent-launchers that fit the bill
	 */
	public ITCFAgentLauncher[] findSuitableAgentLaunchers(final String serviceName, final Map<String, String> attributesToMatch, boolean localAgentsOnly) {
		List<String> registeredPeerLabels = new ArrayList<String>();
		List<ITCFAgentLauncher> registeredAgents = new ArrayList<ITCFAgentLauncher>();

		// Find registered agents that meets our need and which can be launched.

		for (ITCFAgentLauncher descriptor : tcfAgentLaunchers) {
			if (descriptor.getServiceNames().contains(serviceName)
					&& matchesAllAttributes(descriptor.getPeerAttributes(), attributesToMatch)
					&& descriptor.isLaunchable()) {
				registeredPeerLabels.add(descriptor.getPeerName() + " (local registered non-started)");
				registeredAgents.add(descriptor);
			}
		}
		return registeredAgents.toArray(new ITCFAgentLauncher[registeredAgents.size()]);
	}
	
	public IPeer[] getRunningPeers(final String serviceName, final Map<String, String> attributesToMatch, final boolean localAgentsOnly) throws CoreException {
		// first find running peers with matching attributes
		final List<IPeer> runningCandidates1 = new ArrayList<IPeer>();

		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				// This collection is only changed in TCF dispatcher thread.
				// So don't worry about race condition.
				Collection<IPeer> peers = Protocol.getLocator().getPeers().values();

				for (IPeer p : peers) {
					// Don't bother with internal local peer.
					if (isInternalLocalPeer(p))
						continue;

					if (matchesAllAttributes(p.getAttributes(), attributesToMatch)) {
						runningCandidates1.add(p);
					}
				}
			}
		});
		
		// Now search the running candidates for the one that offers the
		// required service.

		final List<IPeer> runningCandidates2 = new ArrayList<IPeer>();
		final List<String> runningLocalAgentPorts = new ArrayList<String>();

		for (final IPeer peer : runningCandidates1) {

			// wait up to 3 seconds for the asynchronous task.
			TCFTask<Object> task = new TCFTask<Object>(3000) {
				public void run() {
					final boolean isLocalAgent = isInLocalAgent(peer);

					/*
					 * If host has multiple IP addresses (e.g. 127.0.0.1 &
					 * 192.168.0.5), a local agent instance may be running on
					 * each of the addresses (see AgentServerTCP for more) but
					 * listening on the same port. In such case, we don't want
					 * to ask user to choose between those for local debug (it's
					 * annoying). So we'll just use first of them for local
					 * debug. Also note that different types of agents should
					 * not listen to the same port.
					 */
					if (isLocalAgent) {
						String port = peer.getAttributes().get(IPeer.ATTR_IP_PORT);
						if (port != null) { // TCP/IP peer
							if (runningLocalAgentPorts.contains(port)) {
								// a local agent on the same port already exists (it 
								// must be of the same agent type), skip this one.
								done(this);
								return;
							}
							else
								runningLocalAgentPorts.add(port);
						}
					}

					IChannel ch = getChannelForPeer(peer);
					if (ch != null) {
						assert (ch.getState() == IChannel.STATE_OPEN);
						if (null != ch.getRemoteService(serviceName)) {
							// If the peer is on a local host, add it. If the
							// peer is on another host, then whether we add
							// it or not depends on the caller's wishes.
							if (isLocalAgent || !localAgentsOnly)
								runningCandidates2.add(peer);
						}
						done(this);
					} else {
						final IChannel channel = peer.openChannel();

						IChannel.IChannelListener listener = new IChannel.IChannelListener() {
							public void onChannelOpened() {
								channel.removeChannelListener(this);

								if (null != channel.getRemoteService(serviceName)) {
									// If the peer is on this machine, add it. If the
									// peer is on another machine, then whether we add
									// it or not depends on the caller's wishes.
									if (isLocalAgent || !localAgentsOnly)
										runningCandidates2.add(peer);
								}
								done(this); // argument is do-not-care
							}

							public void onChannelClosed(Throwable error) {
							}

							public void congestionLevel(int level) {
							}
						};

						channel.addChannelListener(listener);
					}
				}
			};

			try {
				task.get();
			} catch (Exception e) {
				// Failed to find nor open channel to the peer, it must be a
				// stale peer (a peer that dies but not removed from the TCF
				// framework. See
				// rg.eclipse.tm.internal.tcf.services.local.LocatorService.refresh_timer()).
				// Dispose it so that it won't get in the way
				// when we try to auto-launch the agent again.
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						try {
							((AbstractPeer) peer).dispose();
						} catch (AssertionError e) {
							// we were wrong; it is disposed
						}
					}
				});
			}
		}
		
		return runningCandidates2.toArray(new IPeer[runningCandidates2.size()]);
	}

	/**
	 * Determines whether the given peer is running in a local agent. We compare
	 * the IP address of the peer against the list of IP addresses for this
	 * machine (typically, there are at least two: the loopback address and the
	 * physical NIC).
	 */
	public static boolean isInLocalAgent(IPeer peer) {
		assert Protocol.isDispatchThread();
		String ipAddr = peer.getAttributes().get(IPeer.ATTR_IP_HOST);
		return (localIPAddresses.contains(ipAddr));
	}

	private IChannel getOpenChannel(final IPeer peer) throws CoreException {
		IChannel channel = null;

		// First check if there is existing open channel to the peer.
		//
		channel = getChannelForPeer(peer);
		if (channel != null)
			return channel;

		// Then try to open a channel to the peer.
		//
		/*
		 * Following will cause deadlock if called in TCF dispatcher thread as
		 * it will wait for an TCF even to finish in the dispatcher thread.
		 */
		assert (!Protocol.isDispatchThread());

		final WaitForResult<IChannel> waitForChannel = new WaitForResult<IChannel>();

		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				try {
					final IChannel newChannel = peer.openChannel();
					newChannel.addChannelListener(new IChannelListener() {
	
						public void onChannelOpened() {
							waitForChannel.setData(newChannel);
						}
	
						public void onChannelClosed(Throwable error) {
						}
	
						public void congestionLevel(int level) {
						}
					});
				}
				catch (Throwable exc) {
					waitForChannel.handleException(exc);
				}
			}
		});

		try {
			channel = waitForChannel.get();
		} catch (Exception e) {
			throw EDCDebugger.newCoreException("Time out getting open channel for peer.", e);
		}

		return channel;
	}

	/**
	 * Find existing open channel for the given peer.
	 * 
	 * @param peer
	 * @return null if not found.
	 */
	public IChannel getChannelForPeer(final IPeer peer) {

		final IChannel[] ret = { null };

		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				String peerName = peer.getName();
				String peerID = peer.getID();

				IChannel[] channels = Protocol.getOpenChannels();
				for (IChannel channel : channels) {
					IPeer remotePeer = channel.getRemotePeer();
					if (remotePeer.getName().equals(peerName) && remotePeer.getID().equals(peerID)) {
						ret[0] = channel;
						return;
					}
				}
			}
		});

		return ret[0];
	}

	/**
	 * Gets the service from the given TCF peer.
	 * 
	 * @param peer
	 *            TCF peer.
	 * @param serviceName
	 *            the name of the service
	 * @return IService if the peer offers that service, null otherwise.
	 * @throws CoreException on error
	 */
	public IService getPeerService(final IPeer peer, final String serviceName) throws CoreException {
		final WaitForResult<IService> waitForService = new WaitForResult<IService>();

		final IChannel channel = getOpenChannel(peer);

		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				try {
					IService service = channel.getRemoteService(serviceName);
					if (service == null) {
						// If the service is unavailable, set a dummy service
						// object so the delegating thread doesn't end up
						// pointlessly waiting
						service = new IService() {
							public String getName() {
								return null;
							}
						};
					}
					waitForService.setData(service);
				} catch (Exception e) {
					waitForService.handleException(e);
				}
			}
		});

		try {
			IService service = waitForService.get();
			// check for the dummy service object
			return (service.getName() == null) ? null : service;
			
		} catch (Exception e) {
			throw EDCDebugger.newCoreException("Fail to get TCF service [" + serviceName + "] from peer.", e);
		}
	}

	/**
	 * Invokes an agent-launcher and waits (a while) for an agent to be
	 * discovered that meets the given peer attributes
	 * 
	 * @param descriptor
	 * @return
	 * @throws CoreException
	 */
	public IPeer launchAgent(final ITCFAgentLauncher descriptor, final Map<String, String> peerAttrs) throws CoreException {
		final WaitForResult<IPeer> waitForPeer = new WaitForResult<IPeer>() {
		};

		final ILocator.LocatorListener listener = new ILocator.LocatorListener() {

			public void peerRemoved(String id) {
			}

			public void peerHeartBeat(String id) {
			}

			public void peerChanged(IPeer peer) {
			}

			public void peerAdded(IPeer peer) {
				if (matchesAllAttributes(peer.getAttributes(), peerAttrs)) {
					waitForPeer.setData(peer);
				}
			}
		};

		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				// register ourselves as a listener
				Protocol.getLocator().addListener(listener);
			}
		});

		// launch the agent

		IPeer launchedPeer = null;
		try {
			// Launch the agent (if it's not already running)
			try {
				descriptor.launch();
			} catch (Exception e) {
				throw EDCDebugger.newCoreException(MessageFormat.format("Failed to launch the TCF agent that hosts peer \"{0}\". Cause: {1}", 
						descriptor.getPeerName(), e.getLocalizedMessage()), e);
			}
			
			// Wait for the Locator listener we registered above to be notified
			// of the existence of the peer we're interested in
			try {
				launchedPeer = waitForPeer.get();
			} catch (Exception e) {
				if (e.getCause() instanceof TimeoutException) {
					throw EDCDebugger.newCoreException(MessageFormat.format("Timed out waiting for the launched TCF agent to make peer \"{0}\" available.", 
							descriptor.getPeerName()), null);
				}
				else {
					throw EDCDebugger.newCoreException(MessageFormat.format("Error waiting for the launched TCF agent to make peer \"{0}\" available. Cause: {1}", 
							descriptor.getPeerName(), e.getLocalizedMessage()), e);
				}
			}
			launchedtcfAgentLaunchers.add(descriptor);
		}
		finally {
			Protocol.invokeAndWait(new Runnable() {
				public void run() {
					// unregister our listener
					Protocol.getLocator().removeListener(listener);
				}
			});
		}
		

		return launchedPeer;
	}

	private static List<String> getLocalIPAddresses() throws CoreException {
		List<String> ret = new ArrayList<String>();

		Enumeration<NetworkInterface> e;
		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e1) {
			throw EDCDebugger.newCoreException("Host is required to connect to a network but it isn't.");
		}

		while (e.hasMoreElements()) {
			NetworkInterface f = e.nextElement();
			Enumeration<InetAddress> n = f.getInetAddresses();
			while (n.hasMoreElements()) {
				InetAddress addr = n.nextElement();
				ret.add(addr.getHostAddress());
			}
		}

		return ret;
	}

	/**
	 * Shutdown.
	 */
	public void shutdown() {
		// shutdown all agents that were launched by this manager
		for (ITCFAgentLauncher desc : launchedtcfAgentLaunchers) {
			try {
				desc.shutdown();
			} catch (Exception e) {
			}
		}
		launchedtcfAgentLaunchers.clear();
	}
}
