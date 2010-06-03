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

package org.eclipse.cdt.debug.edc.agent.gdbserver;

import java.io.IOException;

import org.eclipse.cdt.debug.edc.agent.gdbserver.services.MemoryService;
import org.eclipse.cdt.debug.edc.agent.gdbserver.services.ProcessesService;
import org.eclipse.cdt.debug.edc.agent.gdbserver.services.RegistersService;
import org.eclipse.cdt.debug.edc.agent.gdbserver.services.RunControlService;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentException;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentUtils;
import org.eclipse.cdt.debug.edc.tcf.extension.SimpleEventQueue;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IServiceProvider;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * TCF debugger agent that talks to gdbserver.
 * 
 * @author LWang
 * 
 */
public class GdbserverAgent {

	public final static String NAME = "Gdbserver TCF Agent";
	public final static String ATTR_DEBUG_SUPPPORT = "GDB Remote Protocol";

	// Make it a singleton class.
	private static GdbserverAgent sInstance = null;

	private AgentServerTCP fServer = null;

	private GdbRemoteProtocol fGdbRemoteProtocol = null;

	private static IServiceProvider sServiceProvider = new IServiceProvider() {
		/*
		 * Tell framework that we are offering this service.
		 */
		public IService[] getLocalService(IChannel channel) {
			return new IService[] { 
					new ProcessesService(channel), 
					new RunControlService(channel),
					new RegistersService(channel), 
//					new SimpleRegistersService(channel), 
					new MemoryService(channel) };
		}

		public IService getServiceProxy(IChannel channel, String serviceName) {
			return null;
		}
	};

	/**
	 * Run the agent as a stand-alone Java application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SimpleEventQueue queue = new SimpleEventQueue();
			Protocol.setEventQueue(queue);

			getInstance().start();
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

	private GdbserverAgent() {
	}

	public static GdbserverAgent getInstance() {
		if (sInstance == null) {
			sInstance = new GdbserverAgent();
		}

		return sInstance;
	}

	/**
	 * Set GdbRemoteProtocol object associated with the agent.
	 * 
	 * @param grp
	 *            - can be null, meaning to shutdown GdbRemoteProtocol object of
	 *            the agent.
	 */
	public void setGdbRemoteProtocol(GdbRemoteProtocol grp) {
		if (fGdbRemoteProtocol != null)
			fGdbRemoteProtocol.dispose();

		fGdbRemoteProtocol = grp;
	}

	public GdbRemoteProtocol getGdbRemoteProtocol() throws AgentException {
		if (fGdbRemoteProtocol == null)
			throw new AgentException("Communication with gdbserver is not established yet.");

		return fGdbRemoteProtocol;
	}

	public void start() throws IOException {
		if (fServer == null) {
			Protocol.addServiceProvider(sServiceProvider);

			fServer = new AgentServerTCP(NAME, AgentUtils.findFreePort());

			// Log packets
			//
			GdbRemoteProtocol.addPacketListener(new PacketLogger(System.out));
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
