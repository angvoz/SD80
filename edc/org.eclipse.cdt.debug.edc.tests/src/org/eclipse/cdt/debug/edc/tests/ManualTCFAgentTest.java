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

package org.eclipse.cdt.debug.edc.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.examples.javaagent.remote.CalculatorServiceProxy;
import org.eclipse.cdt.debug.edc.examples.javaagent.remote.ICalculator;
import org.eclipse.cdt.debug.edc.tcf.extension.SimpleEventQueue;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ISimpleRegisters;
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
 * Manually run this test as a standalone Java application, or as a JUnit plugin test 
 * (but don't add this in any auto test suite.)
 * 
 * After you start the test, it will sit there until you terminate it.
 * 
 * With a TCF agent that supports auto-discovery, you can start the agent before
 * or after the test is started. And the test should be able to detect and
 * connect to the agent and display relevant information.
 * 
 * With a TCF agent that does not support auto-discovery, you need to add a line
 * in testSpecifiedAgents() and you should start the agent before the test
 * starts.
 * 
 * ...................... LWang. Mar, April, 2009
 * 
 */
public class ManualTCFAgentTest {

	public static void main(String[] args) {
		SimpleEventQueue queue = new SimpleEventQueue();
		Protocol.setEventQueue(queue);
		setup();
		try {
			new ManualTCFAgentTest().testTCF();
		} finally {
			tearDown();
		}
	}
	
	boolean exitTest = false;

	class Peer extends AbstractPeer {
		public Peer(Map<String, String> attrs) {
			super(attrs);
		}
	}

	static private List<Peer> sPeers = new ArrayList<Peer>();

	@BeforeClass
	static public void setup() {
		Protocol.invokeLater(new Runnable() {
			public void run() {
				System.out.println("!!!! TCF is ready.");
			}
		});

		// See comment of the method for more.
		CalculatorServiceProxy.registerProxyForClient();
	}

	@AfterClass
	static public void tearDown() {
		Protocol.invokeLater(new Runnable() {
			public void run() {
				for (Peer p : sPeers)
					p.dispose();

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

				for (IPeer p : peers.values())
					examinePeer(p);

				loc.addListener(new LocatorListener() {

					public void peerRemoved(String id) {
						System.out.println("----->>> Peer removed:" + id + "\n");
					}

					public void peerHeartBeat(String id) {
						System.out.println("----->>> Peer heartbeat:" + id + "\n");
					}

					public void peerChanged(IPeer peer) {
						System.out.println("----->>> Peer changed:" + peer.getName() + "\n");
					}

					public void peerAdded(IPeer peer) {
						System.out.println("----->>> New Peer Discovered:");
						examinePeer(peer);
					}
				});
			}
		});

		Protocol.invokeLater(new Runnable() {
			public void run() {
				testSpecifiedAgents();
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

	private void testSpecifiedAgents() {
		// If we want to connect to a specified agent (not auto-discovered)...

		// createPeerForRemoteAgent("192.168.131.1", 1534); // reference agent
		// running on machine 192.168.131.1.

		// createPeerForRemoteAgent("127.0.0.1", 15341); // an agent on local
		// host
	}

/*	private Peer createPeerForRemoteAgent(String addr, int port) {
		for (Peer p : sPeers) {
			if (addr.equals(p.getAttributes().get(IPeer.ATTR_IP_HOST))
					&& port == Integer.parseInt(p.getAttributes().get(IPeer.ATTR_IP_PORT)))
				return p;
		}
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(IPeer.ATTR_ID, "TCP:" + addr + ":" + port);
		attrs.put(IPeer.ATTR_NAME, "AnyRemoteAgent");
		attrs.put(IPeer.ATTR_OS_NAME, System.getProperty("os.name"));
		attrs.put(IPeer.ATTR_TRANSPORT_NAME, "TCP");
		attrs.put(IPeer.ATTR_IP_HOST, addr);
		attrs.put(IPeer.ATTR_IP_PORT, Integer.toString(port));
		attrs.put(IPeer.ATTR_PROXY, "");
		Peer p = new Peer(attrs);
		sPeers.add(p);
		return p;
	}
*/
	private void examinePeer(IPeer p) {
		System.out.println("----------- Peer ------------------");
		System.out.println("\tID: " + p.getID());
		System.out.println("\tName: " + p.getName());
		System.out.println("\tTransport Name: " + p.getTransportName());

		if (p.getID().equals("TCFLocal")) // skip the default local one.
			return;

		final IChannel channel = p.openChannel();

		channel.addChannelListener(new IChannelListener() {

			public void onChannelOpened() {
				if (channel.getState() != IChannel.STATE_OPEN)
					return;

				System.out.println("\nChannel opened: " + channel.getLocalPeer().getID() + " <---> "
						+ channel.getRemotePeer().getID() + "(" + channel.getRemotePeer().getName() + ")");

				Collection<String> lservices = channel.getLocalServices();

				System.out.println("\tLocal services: ");
				for (String s : lservices)
					System.out.println("\t\t" + s);

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

				channel.removeChannelListener(this);

				// Shutdown the test
				//
				// if (Protocol.getOpenChannels().length <= 1)
				// the channel between localPeer & localPeer won't close until
				// Eclipse exits.
				// exitTest = true;
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
		} else if (service instanceof ISimpleRegisters) {
			ISimpleRegisters serv = (ISimpleRegisters) service;
			serv.get(null, new String[] { "eas" }, new ISimpleRegisters.DoneGet() {

				public void doneGet(IToken token, Exception error, String[] values) {
					if (error != null) {
						System.out.println("ISimpleRegisters service: [get] test failed. Error from agent: \n"
								+ error.getMessage());
					} else {
						System.out.println("ISimpleRegisters service: [get] test ok.\n");
					}
				}
			});
		} else if (service instanceof IProcesses) {
			IProcesses serv = (IProcesses) service;

			// Try to get all processes from the agent.
			serv.getChildren(null, false, new IProcesses.DoneGetChildren() {

				public void doneGetChildren(IToken token, Exception error, String[] contextIds) {
					if (error == null) {
						System.out.println("===> Process list from IProcesses service:");
						for (String pid : contextIds)
							System.out.print("\t" + pid);
						System.out.println("\nIProcesses service: [get all processes] test OK !");
					} else
						System.out.println("\nIProcesses service: [get all processes] failed ! Error from agent: "
								+ error.getMessage());
				}
			});

			// Try to get all attached processes
			serv.getChildren(null, true, new IProcesses.DoneGetChildren() {

				public void doneGetChildren(IToken token, Exception error, String[] contextIds) {
					if (error == null) {
						System.out.println("===> Attached Process list from IProcesses service:");
						for (String pid : contextIds)
							System.out.print("\t" + pid);
						System.out.println("\nIProcesses service: [get attached process] test OK !");
					} else
						System.out.println("\nIProcesses service: [get attached process] failed ! Error from agent: "
								+ error.getMessage());
				}
			});

			serv.start("", "/mydisk/myprog/cpp/gtkhello/helloworld", new String[] { "" }, null, false,
					new IProcesses.DoneStart() {

						public void doneStart(IToken token, Exception error, ProcessContext process) {
							if (error != null) {
								System.out.println("IProcesses service: [start] test  failed. Error from agent: \n"
										+ error.getMessage());
							} else {
								System.out.println("IProcesses service: [start] test ok.  New process id: "
										+ process.getID());
							}
						}

					});
		} else if (service instanceof ICalculator) {
			ICalculator serv = (ICalculator) service;
			serv.increment(111, new ICalculator.DoneIncrement() {
				public void done(IToken token, Throwable error, int i) {
					Assert.assertNull(error);
					Assert.assertEquals(112, i);
					System.out.println("ICalculator service: [increment] test OK !");
				}
			});
		}

	}

}
