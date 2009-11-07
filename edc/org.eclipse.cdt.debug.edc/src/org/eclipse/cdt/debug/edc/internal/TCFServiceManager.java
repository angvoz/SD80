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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.ITCFAgentDescriptor;
import org.eclipse.cdt.debug.edc.ITCFServiceManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
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

public class TCFServiceManager implements ITCFServiceManager, ILocator.LocatorListener {

	private boolean initialized = false;
	private List<String> hostIPAddresses;
	private List<ITCFAgentDescriptor> tcfAgentDescriptors;

	// <name, peer> map.
	private final Map<String, IPeer> knownPeers = Collections.synchronizedMap(new HashMap<String, IPeer>());

	public TCFServiceManager() {
	}

	private void initialize() throws CoreException {
		// load extensions
		tcfAgentDescriptors = new ArrayList<ITCFAgentDescriptor>();

		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EDCDebugger.PLUGIN_ID
				+ ".tcfAgentDescriptor"); //$NON-NLS-1$
		IExtension[] extensions = extensionPoint.getExtensions();

		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			IConfigurationElement element = elements[0];

			boolean failed = false;
			try {
				Object extObject = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (extObject instanceof ITCFAgentDescriptor) {
					tcfAgentDescriptors.add((ITCFAgentDescriptor) extObject);
				} else {
					failed = true;
				}
			} catch (CoreException e) {
				failed = true;
			}

			if (failed) {
				EDCDebugger.getMessageLogger().logError(
						"Unable to load agentDescriptor extension from " + extension.getContributor().getName(), null);
			}
		}

		Protocol.invokeAndWait(new Runnable() {
			public void run() {

				// get the known peers from the locator service
				for (IPeer peer : Protocol.getLocator().getPeers().values()) {
					peerAdded(peer);
				}
				Protocol.getLocator().addListener(TCFServiceManager.this);
			}
		});

		// record local host IP addresses
		hostIPAddresses = getLocalIPAddresses();

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

	public Collection<String> getAgents(final String serviceName, final Map<String, String> attributesToMatch)
			throws Exception {
		if (!initialized) {
			initialize();
		}

		List<IStatus> statuses = new ArrayList<IStatus>();

		// first find running agents with matching attributes
		//
		final List<IPeer> runningCandidates = new ArrayList<IPeer>();
		new TCFTask<Object>() {
			public void run() {
				synchronized (knownPeers) {
					for (IPeer peer : knownPeers.values()) {
						// Don't bother with internal local peer.
						if (isInternalLocalPeer(peer))
							continue;

						Map<String, String> peerAttributes = peer.getAttributes();
						String host = peerAttributes.get(IPeer.ATTR_IP_HOST);

						if (host.equals("127.0.0.1") && matchesAllAttributes(peer.getAttributes(), attributesToMatch))
							runningCandidates.add(peer);
					}
				}

				done(this);
			}
		}.getE();

		final Set<String> agentNames = new HashSet<String>();

		// Now find the running candidates that offer the required service.
		//
		if (runningCandidates.size() > 0) {
			for (IPeer p : runningCandidates) {
				final IPeer peer = p;
				String peerName = new TCFTask<String>() {
					public void run() {
						done(peer.getName());
					}
				}.get();

				// wait up to 3 seconds for the asynchronous task.
				TCFTask<Object> task = new TCFTask<Object>(3000) {
					public void run() {
						IChannel ch = getChannelForPeer(peer);
						if (ch != null) {
							assert (ch.getState() == IChannel.STATE_OPEN);
							if (null != ch.getRemoteService(serviceName)) {
								agentNames.add(peer.getName());
								done(this);
							} else
								error(new Exception(MessageFormat.format(
										"Service [{0}] is not offered by the running agent [{1}].", serviceName, peer
												.getName())));
						} else {
							final IChannel channel = peer.openChannel();

							IChannel.IChannelListener listener = new IChannel.IChannelListener() {
								public void onChannelOpened() {
									if (null != channel.getRemoteService(serviceName))
										agentNames.add(peer.getName());

									channel.removeChannelListener(this);
									done(this); // the argument is do-not-care
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
					task.getE();
				} catch (Error er) {
					if (task.isCancelled()) {
						statuses.add(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, MessageFormat.format(
								"Timeout opening channel to running agent [{0}].", peerName)));
					} else
						statuses.add(new Status(IStatus.WARNING, EDCDebugger.PLUGIN_ID, MessageFormat.format(
								"Fail to find service [{0}] from running agent [{1}].", serviceName, peerName)));
				}
			}

			// If we failed to get service from matched running agents, report
			// the error.
			// In theory we should go on looking at those non-started agents,
			// but currently it's most likely a problem.
			// TODO: remove this when there are two agents with the same
			// attributes but offering different services........... 06/25/09
			if (statuses.size() == runningCandidates.size()) {
				assert agentNames.size() == 0;
				MultiStatus ms = new MultiStatus(EDCDebugger.PLUGIN_ID, 0, statuses
						.toArray(new IStatus[statuses.size()]), "Failed to get required service from running agents.",
						null);

				throw new Exception(ms.toString());
			}
		}

		// now check agent descriptors
		// only if there is no running agent meeting our needs.
		// TODO: get rid of the check after we have better way of selecting
		// agents...05/13/09
		if (agentNames.size() == 0)
			new TCFTask<Object>() {
				public void run() {
					for (ITCFAgentDescriptor descriptor : tcfAgentDescriptors) {
						if (descriptor.getServiceNames().contains(serviceName)) {
							if (matchesAllAttributes(descriptor.getAttributes(), attributesToMatch)) {
								agentNames.add(descriptor.getName());
							}
						}
					}
					done(this);
				}
			}.get();

		return agentNames;
	}

	/**
	 * Find a TCF agent that matches the given attributes and offers the given
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
	public IPeer findAgent(final String serviceName, final Map<String, String> attributesToMatch) throws CoreException {

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
							// for junit, only look at local agent
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

		final boolean[] localAgentFound = { false };

		// Now search the running candidates for the one that
		// offers the required service.
		//
		if (runningCandidates1.size() > 0) {
			for (IPeer p : runningCandidates1) {
				final IPeer peer = p;

				// wait up to 3 seconds for the asynchronous task.
				TCFTask<Object> task = new TCFTask<Object>(3000) {
					public void run() {
						final String peerLabel = peer.getName() + " (" + peer.getID() + ") "
								+ (isLocalPeer(peer) ? "(local running)" : "(remote running)");

						IChannel ch = getChannelForPeer(peer);
						if (ch != null) {
							assert (ch.getState() == IChannel.STATE_OPEN);
							if (null != ch.getRemoteService(serviceName)) {
								runningCandidates2.add(peer);
								if (peerLabel.contains("local"))
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
										if (peerLabel.contains("local"))
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

		List<String> registeredAgentLabels = new ArrayList<String>();
		List<ITCFAgentDescriptor> registeredAgents = new ArrayList<ITCFAgentDescriptor>();

		// find registered agents that meets our need.
		//
		if (useRegisteredAgents) {
			for (ITCFAgentDescriptor descriptor : tcfAgentDescriptors) {
				if (descriptor.getServiceNames().contains(serviceName)
						&& matchesAllAttributes(descriptor.getAttributes(), attributesToMatch)) {
					registeredAgentLabels.add(descriptor.getName() + " (local registered non-started)");
					registeredAgents.add(descriptor);

					// For unit test, just use the fist candidate found.
					if (unitTestRunning)
						break;
				}
			}

			options.addAll(registeredAgentLabels);
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
					"No agent is found that provides service [{0}] and has attributes {1}", serviceName,
					attributesToMatch));
		if (options.size() == 1 && !promptUser)
			selection[0] = options.get(0);
		else {
			// Prompt is required or more than one options available
			runInUIThread(new Runnable() {
				public void run() {
					ElementListSelectionDialog dialog = new ElementListSelectionDialog(null, new LabelProvider());
					dialog.setElements(options.toArray());
					dialog.setTitle("Select TCF Agent");
					dialog.setMessage("Please select the TCF agent you want debugger to use:");
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
			i = registeredAgentLabels.indexOf(selection[0]);
			ITCFAgentDescriptor descriptor = registeredAgents.get(i);
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
		return (hostIPAddresses.contains(ipAddr));
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
			throw EDCDebugger.newCoreException("Time out getting open channel for agent.", e);
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

	public void peerAdded(IPeer peer) {
		synchronized (knownPeers) {
			Map<String, String> peerAttributes = peer.getAttributes();
			String host = peerAttributes.get(IPeer.ATTR_IP_HOST);
			if (host != null && host.equals("127.0.0.1"))
				knownPeers.put(peer.getName(), peer);
		}
	}

	public void peerChanged(IPeer peer) {
	}

	public void peerHeartBeat(String id) {
	}

	// Note this method is ensured to be called in TCF dispatch thread.
	//
	public void peerRemoved(String id) {
		// "knownPeers" is keyed on name instead of id of agent.
		synchronized (knownPeers) {
			for (IPeer p : knownPeers.values())
				if (p.getID().equals(id)) {
					knownPeers.remove(p.getName());
					break;
				}
		}
	}

	public IService getAgentService(final String agentName, final String serviceName) throws Exception {
		// first check the running agents
		final IPeer peer = knownPeers.get(agentName);
		if (peer != null) {

			final WaitForResult<IService> waitForService = new WaitForResult<IService>();
			Protocol.invokeAndWait(new Runnable() {
				public void run() {
					try {
						IChannel channel = getOpenChannel(peer);
						IService service = channel.getRemoteService(serviceName);
						waitForService.setData(service);
					} catch (Exception e) {
						waitForService.handleException(e);
					}
				}
			});
			return waitForService.get();
		} else {
			// agent isn't running. see if there's an agent descriptor
			// by that name
			for (final ITCFAgentDescriptor descriptor : tcfAgentDescriptors) {
				if (descriptor.getName().equals(agentName)) {
					// double check that it implements the desired
					// service
					if (!descriptor.getServiceNames().contains(serviceName)) {
						return null;
					}

					final WaitForResult<IPeer> waitForPeer = new WaitForResult<IPeer>() {
					};

					final ILocator.LocatorListener locationListener = new ILocator.LocatorListener() {

						public void peerRemoved(String id) {
						}

						public void peerHeartBeat(String id) {
						}

						public void peerChanged(IPeer peer) {
						}

						public void peerAdded(IPeer peer) {

							if (peer.getName().equals(descriptor.getName())) {
								waitForPeer.setData(peer);
							}
						}
					};

					Protocol.invokeAndWait(new Runnable() {
						public void run() {
							// register ourselves as a listener
							Protocol.getLocator().addListener(locationListener);
						}
					});

					// launch the agent
					descriptor.launch();
					final IPeer launchedPeer = waitForPeer.get();

					final WaitForResult<IService> waitForService = new WaitForResult<IService>();

					Protocol.invokeAndWait(new Runnable() {
						public void run() {
							// unregister our listener
							Protocol.getLocator().removeListener(locationListener);
						}
					});

					if (launchedPeer != null) {
						try {
							final IChannel channel = getOpenChannel(launchedPeer);

							Protocol.invokeAndWait(new Runnable() {
								public void run() {
									// unregister our listener
									Protocol.getLocator().removeListener(locationListener);

									IService service = channel.getRemoteService(serviceName);
									waitForService.setData(service);
								}
							});
						} catch (Exception e) {
							waitForService.handleException(e);
						}
					}

					return waitForService.get();
				}
			}
		}
		return null;
	}

	public IService getAgentService(final IPeer peer, final String serviceName) throws CoreException {
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
			throw EDCDebugger.newCoreException("Fail to get TCF service [" + serviceName + "] from agent.", e);
		}
	}

	public IChannel getAgentChannel(String agentId) {
		IPeer peer = knownPeers.get(agentId);
		if (peer == null)
			return null;
		return getChannelForPeer(peer);
	}

	private IPeer launchAgent(final ITCFAgentDescriptor descriptor) throws CoreException {
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

				if (peer.getName().equals(descriptor.getName())) {
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

		IPeer launchedPeer;
		try {
			descriptor.launch();
			launchedPeer = waitForPeer.get();
		} catch (Exception e) {
			throw EDCDebugger.newCoreException(MessageFormat.format("Fail to launch agent {0}. Cause: {1}", descriptor
					.getName(), e.getLocalizedMessage()), e);
		} finally {
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
}
