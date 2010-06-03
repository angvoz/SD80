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

package org.eclipse.cdt.debug.edc.tests;

import java.util.Collection;
import java.util.Map;

import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tm.tcf.services.IDiagnostics;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IDiagnostics.DoneEcho;
import org.eclipse.tm.tcf.services.ILocator.LocatorListener;
import org.eclipse.tm.tcf.services.IProcesses.ProcessContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A unit test for TCF debugger agent (an agent providing common TCF debugger
 * services). This is supposed to run manually (as a JUnit plugin test) as it
 * requires interaction with you. So don't add this in any nightly auto test
 * suite.
 * 
 * With a TCF agent that supports auto-discovery, you can start the agent before
 * or after the test is started. And the test should be able to detect and
 * connect to the agent and display relevant information.
 * 
 * ...................... LWang. April, 2009
 * 
 */
public class ManualDebuggerAgentTest {

	boolean exitTest = false;

	class Peer extends AbstractPeer {
		public Peer(Map<String, String> attrs) {
			super(attrs);
		}
	}

	@BeforeClass
	static public void setup() {
		Protocol.invokeLater(new Runnable() {
			public void run() {
				System.out.println("!!!! TCF is ready.");
			}
		});
	}

	@AfterClass
	static public void tearDown() {
		Protocol.invokeLater(new Runnable() {
			public void run() {
				System.out.println("!!!! Test done.");
			}
		});
	}

	@Test
	public void testTCF() {
		Protocol.invokeLater(new Runnable() {
			public void run() {
				ILocator loc = Protocol.getLocator();

				Map<String, IPeer> peers = loc.getPeers();

				IPeer theAgent = null;

				for (IPeer p : peers.values()) {
					if (isTheAgent(p)) {
						theAgent = p;
						break;
					}
				}

				if (theAgent == null) {
					// wait till the agent is started
					System.out.println("Please start the TCF agent you want to test. Waiting ...");

					loc.addListener(new LocatorListener() {

						public void peerRemoved(String id) {
							System.out.println("----->>> Peer removed:" + id + "\n");
						}

						public void peerHeartBeat(String id) {
							// System.out.println("----->>> Peer heartbeat:" +
							// id +"\n");
						}

						public void peerChanged(IPeer peer) {
							System.out.println("----->>> Peer changed:" + peer.getName() + "\n");
						}

						public void peerAdded(IPeer peer) {
							System.out.println("----->>> New Peer Discovered:");
							if (isTheAgent(peer))
								examinePeer(peer);
						}
					});
				}
			}
		});

		// Idle. any newly started agent should be detected.
		//
		while (!exitTest)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	/**
	 * Check if the peer is the one we want to test. Modify this to match your
	 * desired agent when needed.
	 * 
	 * @param p
	 * @return
	 */
	private boolean isTheAgent(IPeer p) {
		return p.getName().toLowerCase().contains("gdbserver") && !p.getID().contains("127.0.0.1");
	}

	private void examinePeer(IPeer p) {
		System.out.println("----------- Peer ------------------");
		System.out.println("\tID: " + p.getID());
		System.out.println("\tName: " + p.getName());
		System.out.println("\tTransport Name: " + p.getTransportName());

		final IChannel channel = p.openChannel();

		channel.addChannelListener(new IChannelListener() {

			public void onChannelOpened() {
				if (channel.getState() != IChannel.STATE_OPEN)
					return;

				System.out.println("\nChannel opened: " + channel.getLocalPeer().getID() + " <---> "
						+ channel.getRemotePeer().getID() + "(" + channel.getRemotePeer().getName() + ")");

				Collection<String> rservices = channel.getRemoteServices();

				System.out.println("\tRemote services: ");
				for (String sname : rservices) {
					System.out.println("\t\t" + sname);

					final IService is = channel.getRemoteService(sname);

					Protocol.invokeLater(new Runnable() {

						public void run() {
							examineService(is);
						}
					});
				}
			}

			public void onChannelClosed(Throwable error) {
				System.out.println("Channel to this peer closed: " + channel.getRemotePeer().getID());
				((AbstractPeer) channel.getRemotePeer()).dispose();

				// Shutdown the test
				//
				if (Protocol.getOpenChannels().length <= 1)
					exitTest = true;
			}

			public void congestionLevel(int level) {
				System.out.println("Congestion level is: " + level);
			}
		});
	}

	private void examineService(IService service) {
		if (service instanceof IDiagnostics) {
			IDiagnostics serv = (IDiagnostics) service;
			serv.echo("how are you", new DoneEcho() {
				public void doneEcho(IToken token, Throwable error, String s) {
					Assert.assertNull(error);
					Assert.assertEquals("how are you", s);
					System.out.println("IDiagnostics service: [echo] test OK !");
				}
			});
		} else if (service instanceof IProcesses) {
			IProcesses serv = (IProcesses) service;

			// Try to get all processes the agent resides.
			serv.getChildren(null, false, new IProcesses.DoneGetChildren() {

				public void doneGetChildren(IToken token, Exception error, String[] contextIds) {
					if (error == null) {
						System.out.println("===> Process list from IProcesses service:");
						for (String pid : contextIds)
							System.out.print("\t" + pid);
						System.out.println("\nIProcesses service: [get-process-list] test OK !");
					} else
						System.out.println("\nIProcesses service: [get-process-list] failed ! Error: "
								+ error.getMessage());
				}
			});

			serv.start("", "/mydisk/myprog/cpp/gtkhello/helloworld", new String[] { "" }, null, false,
					new IProcesses.DoneStart() {

						public void doneStart(IToken token, Exception error, ProcessContext process) {
							if (error != null) {
								System.out.println("Fail to start program. Error from agent: \n" + error.getMessage());
							} else {
								System.out.println("Newly started process: " + process.getID());
								System.out.println("IProcesses service: [start] test ok.\n");
							}
						}

					});
		}

	}
}
