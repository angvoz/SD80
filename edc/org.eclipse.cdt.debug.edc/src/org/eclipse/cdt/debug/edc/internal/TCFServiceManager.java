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
package org.eclipse.cdt.debug.edc.internal;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.ITCFAgentLauncher;
import org.eclipse.cdt.debug.edc.ITCFServiceManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Utility class that provides access to TCF agents and services. It abstracts
 * out the details of which agent provides the services, launching the agent if
 * necessary, etc.
 */
public class TCFServiceManager implements ITCFServiceManager  {

	/**
	 * The stringified IP addresses of the local machine. Populated by
	 * {@link #initialize()}. Typically "127.0.0.1" and the actual IP (plus
	 * others if multiple NICs).
	 */
	private List<String> localIPAddresses;
	
	private List<ITCFAgentLauncher> tcfAgentLaunchers;

	private static final String EXTENSION_POINT_NAME = "tcfAgentLauncher";

	private boolean initialized = false;
	private List<ITCFAgentLauncher> launchedtcfAgentLaunchers;

	public TCFServiceManager() {
	}

	private void initialize() throws CoreException {
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

		// record local host IP addresses
		localIPAddresses = getLocalIPAddresses();

		initialized = true;
	}

	private boolean matchesAllAttributes(Map<String, String> attributes, Map<String, String> attributesToMatch) {
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
	 * Find a TCF peer that matches the given attributes and offers the given
	 * service. Running agents on LAN will be searched. If no running agent
	 * meets the need, local registered agents will be searched. When multiple
	 * candidates are found, user will be prompted to select one. If the
	 * selected one is a non-started agent, it will be launched.<br>
	 * But if JUnit test is running, only local agent will be searched and no
	 * prompt dialog will be presented.
	 * 
	 * @param serviceName
	 * @param attributesToMatch
	 * @return a running agent
	 * @throws CoreException
	 *             on any error.
	 */
	public IPeer getPeer(final String serviceName, final Map<String, String> attributesToMatch) throws CoreException {

		if (!initialized) {
			initialize();
		}

		final boolean unitTestRunning = GeneralUtils.isJUnitRunning();

		// first find running agents with matching attributes
		//
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
						if (unitTestRunning) {
							// for junit, only look at local peer
							if (isLocalPeer(p))
								runningCandidates1.add(p);
						} else
							runningCandidates1.add(p);
					}
				}
			}
		});

		final List<IPeer> runningCandidates2 = new ArrayList<IPeer>();
		final List<String> runningCandidateLabels = new ArrayList<String>();
		final List<String> runningLocalAgentPorts = new ArrayList<String>();
		
		final boolean[] localAgentFound = { false };

		// Now search the running candidates for the one that offers the
		// required service.
		//
		for (final IPeer peer : runningCandidates1) {

			// wait up to 3 seconds for the asynchronous task.
			TCFTask<Object> task = new TCFTask<Object>(3000) {
				public void run() {
					final boolean isLocalAgent = isLocalPeer(peer);

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
					
					final String peerLabel = peer.getName() + " (" + peer.getID() + ") "
							+ (isLocalAgent ? "(local running)" : "(remote running)");

					IChannel ch = getChannelForPeer(peer);
					if (ch != null) {
						assert (ch.getState() == IChannel.STATE_OPEN);
						if (null != ch.getRemoteService(serviceName)) {
							runningCandidates2.add(peer);
							if (isLocalAgent)
								localAgentFound[0] = true;
							runningCandidateLabels.add(peerLabel);
						}
						done(this);
					} else {
						final IChannel channel = peer.openChannel();

						IChannel.IChannelListener listener = new IChannel.IChannelListener() {
							public void onChannelOpened() {
								channel.removeChannelListener(this);

								if (null != channel.getRemoteService(serviceName)) {
									runningCandidates2.add(peer);
									if (isLocalAgent)
										localAgentFound[0] = true;
									runningCandidateLabels.add(peerLabel);
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
						((AbstractPeer) peer).dispose();
					}
				});
			}
		}

		IPeer finalCandidate = null;

		boolean useRegisteredAgents = false;
		boolean promptUser = false;

		final ArrayList<String> options = new ArrayList<String>();

		/*
		 * We need to offer registered agents as candidates if 1) no running
		 * agent meets our need, or 2) none of the running agents matched is
		 * local
		 * 
		 * Namely if there is a local running agent that meets our need, don't
		 * bother checking registered agent.
		 */
		if (runningCandidates2.size() == 0) {
			// no running agents found. Check registered agents.
			useRegisteredAgents = true;
		}
		// Single local agent meeting our requirement, use it and
		// don't bother prompting user.
		// Or for junit test, just use the first candidate.
		else if (runningCandidates2.size() == 1 && localAgentFound[0] || unitTestRunning) {
			finalCandidate = runningCandidates2.get(0);
			return finalCandidate;
		} else if (runningCandidates2.size() > 1 || runningCandidates2.size() == 1 && !localAgentFound[0]) {

			// Prompt user if we
			// 1) find two or more candidates or
			// 2) find only one candidate but it's remote agent.
			//
			promptUser = true;

			options.addAll(runningCandidateLabels);

			// If none of the running agents found is local, make
			// registered agents available as options for user to choose.
			//
			if (!localAgentFound[0])
				useRegisteredAgents = true;
		}

		List<String> registeredPeerLabels = new ArrayList<String>();
		List<ITCFAgentLauncher> registeredAgents = new ArrayList<ITCFAgentLauncher>();

		// Find registered agents that meets our need and which can be launched.
		//
		if (useRegisteredAgents) {
			for (ITCFAgentLauncher descriptor : tcfAgentLaunchers) {
				if (descriptor.getServiceNames().contains(serviceName)
						&& matchesAllAttributes(descriptor.getPeerAttributes(), attributesToMatch)
						&& descriptor.isLaunchable()) {
					registeredPeerLabels.add(descriptor.getPeerName() + " (local registered non-started)");
					registeredAgents.add(descriptor);

					// For unit test, just use the fist candidate found.
					if (unitTestRunning)
						break;
				}
			}

			options.addAll(registeredPeerLabels);
			
			// We wanted to prompt the user because nothing was running
			// locally and a registered agent might have provided more agents.
			// But even if only one is found, still prompt the user, otherwise
			// there's a chance of running a remote agent from the wrong place.
			//
			/*
			if (options.size() == 1) {
				promptUser = false;
			}
			*/
		}

		final String selection[] = { null };
		final boolean[] userCanceled = { false };

		/*
		 * We need to prompt user to choose if 1) two or more options (including
		 * running ones and registered ones) available, or 2) only one remote
		 * running agent is available
		 */
		if (options.size() == 0) // no matching agent
			throw EDCDebugger.newCoreException(MessageFormat.format(
					"No peer was found that provides service [{0}] and has attributes {1}", serviceName,
					attributesToMatch));
		if (options.size() == 1 && !promptUser)
			selection[0] = options.get(0);
		else {
			// Prompt is required or more than one options available
			runInUIThread(new Runnable() {
				public void run() {
					ElementListSelectionDialog dialog = new ElementListSelectionDialog(null, new LabelProvider());
					dialog.setElements(options.toArray());
					dialog.setTitle("Select TCF Peer");
					dialog.setMessage("Select the TCF peer you want the debugger to use:");
					if (dialog.open() == Window.OK) {
						selection[0] = (String) dialog.getFirstResult();
					} else { // user canceled
						userCanceled[0] = true;
					}
				}
			});
			if (userCanceled[0])
				throw new CoreException(Status.CANCEL_STATUS);
		}

		int i = runningCandidateLabels.indexOf(selection[0]);
		if (i >= 0)
			finalCandidate = runningCandidates2.get(i);
		else {
			// User selected a registered agent
			// We launch the agent.
			i = registeredPeerLabels.indexOf(selection[0]);
			ITCFAgentLauncher descriptor = registeredAgents.get(i);
			finalCandidate = launchAgent(descriptor);
		}

		return finalCandidate;
	}

	private void runInUIThread(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			Display.getDefault().syncExec(runnable);
		}
	}

	protected boolean isLocalPeer(IPeer peer) {
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

				ArrayList<IChannel> allChannels = new ArrayList<IChannel>();

				allChannels.addAll(Arrays.asList(Protocol.getOpenChannels()));

				for (IChannel iChannel : allChannels) {
					IPeer remotePeer = iChannel.getRemotePeer();
					if (remotePeer.getName().equals(peerName) && remotePeer.getID().equals(peerID)) {
						ret[0] = iChannel;
						return;
					}
				}
			}
		});

		return ret[0];
	}

	/**
	 * Gets the service from the given TCF agent.
	 * 
	 * @param peer
	 *            TCF agent.
	 * @param serviceName
	 *            the name of the service
	 * @throws CoreException on error
	 */
	public IService getPeerService(final IPeer peer, final String serviceName) throws CoreException {

		final WaitForResult<IService> waitForService = new WaitForResult<IService>();

		final IChannel channel = getOpenChannel(peer);

		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				try {
					IService service = channel.getRemoteService(serviceName);
					waitForService.setData(service);
				} catch (Exception e) {
					waitForService.handleException(e);
				}
			}
		});

		try {
			return waitForService.get();
		} catch (Exception e) {
			throw EDCDebugger.newCoreException("Fail to get TCF service [" + serviceName + "] from peer.", e);
		}
	}

private IPeer launchAgent(final ITCFAgentLauncher descriptor) throws CoreException {
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

				if (peer.getName().equals(descriptor.getPeerName())) {
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

	private List<String> getLocalIPAddresses() throws CoreException {
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
