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

package org.eclipse.cdt.debug.edc.examples.javaagent;

import java.io.IOException;
import java.util.LinkedList;

import org.eclipse.tm.tcf.core.ServerTCP;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IEventQueue;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IServiceProvider;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * This is a TCF agent in Java.
 * 
 * @author LWang
 * 
 */
public class TCFAgentInJava {

	// Make it a singleton class.
	private static TCFAgentInJava sInstance = null;

	private ServerTCP fServer = null;

	private static IServiceProvider sServiceProvider = new IServiceProvider() {
		/*
		 * Tell framework that we are offering this service.
		 */
		public IService[] getLocalService(IChannel channel) {
			return new IService[] { new CalculatorService(channel) };
		}

		public IService getServiceProxy(IChannel channel, String serviceName) {
			return null;
		}
	};

	private static class EventQueue extends Thread implements IEventQueue {

		private final LinkedList<Runnable> queue = new LinkedList<Runnable>();

		EventQueue() {
			setName("TCF Event Dispatch");
			start();
		}

		@Override
		public void run() {
			try {
				while (true) {
					Runnable r = null;
					synchronized (this) {
						while (queue.isEmpty())
							wait();
						r = queue.removeFirst();
					}
					try {
						r.run();
					} catch (Throwable x) {
						System.err.println("Error dispatching TCF event:");
						x.printStackTrace();
					}
				}
			} catch (Throwable x) {
				x.printStackTrace();
				System.exit(1);
			}
		}

		public synchronized int getCongestion() {
			int n = queue.size() - 100;
			if (n > 100)
				n = 100;
			return n;
		}

		public synchronized void invokeLater(Runnable runnable) {
			queue.add(runnable);
			notify();
		}

		public boolean isDispatchThread() {
			return Thread.currentThread() == this;
		}
	}

	/**
	 * Run the agent as a standalone Java application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Protocol.setEventQueue(new EventQueue());

			getInstance().start("Agent_Standalone");
		} catch (IOException e) {
			System.out.println("Fail to start the agent. IOException: " + e.getMessage());
			return;
		}

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private TCFAgentInJava() {
	}

	public static TCFAgentInJava getInstance() {
		if (sInstance == null) {
			sInstance = new TCFAgentInJava();
		}

		return sInstance;
	}

	public void start(String agentName) throws IOException {
		if (fServer == null) {
			Protocol.addServiceProvider(sServiceProvider);

			fServer = new ServerTCP(agentName, 15341);
		}
	}

	public void stop() throws IOException {
		if (fServer != null) {
			fServer.close();
			Protocol.removeServiceProvider(sServiceProvider);
		}

		fServer = null;
	}

	public boolean isRunning() {
		return fServer != null;
	}
}
