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

package org.eclipse.cdt.debug.edc.agent.gdbserver;

import java.io.PrintStream;

import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.IPacketListener;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.Packet;

/**
 * A utility to log any gdbserver packet traffic.
 * 
 * @author LWang
 * 
 */
public class PacketLogger implements IPacketListener {

	private String fOutFlag = "-> ";
	private String fInFlag = "<- ";

	private PrintStream fOut;

	/**
	 * @param s
	 *            - the output stream to which to log packets.
	 */
	public PacketLogger(PrintStream s) {
		fOut = s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.IPacketListener
	 * #
	 * onPacketReceived(org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol
	 * .Packet)
	 */
	public void onPacketReceived(Packet p) {
		fOut.println("(Thread:" + Thread.currentThread().getName() + ")" + fInFlag + p.getDescription() + ": "
				+ p.getData());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.IPacketListener
	 * #
	 * onPacketSent(org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.
	 * Packet, java.lang.Exception)
	 */
	public void onPacketSent(Packet p, Exception err) {
		if (err != null)
			fOut.println("\tError : " + err.getLocalizedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.IPacketListener
	 * #
	 * onPacketToBeSent(org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol
	 * .Packet)
	 */
	public void onPacketToBeSent(Packet p) {
		fOut.println("(Thread:" + Thread.currentThread().getName() + ")" + fOutFlag + p.getDescription() + ": "
				+ p.getData());
	}
}
