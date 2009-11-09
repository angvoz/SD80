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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.debug.edc.tcf.extension.AgentException;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentUtils;
import org.eclipse.cdt.debug.edc.tcf.extension.transport.ITransportChannel;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * Handles communication with gdbserver using GDB Serial Remote Protocol.
 * <p>
 * 
 * @See <a href="http://sources.redhat.com/gdb/current/onlinedocs/gdb_34.html">
 *      Specification of the GDB Remote Protocol</a>
 */
public abstract class GdbRemoteProtocol {

	public enum PacketType {
		UNKNOWN, COMMAND, RESPONSE, NOTIFICATION, ACKNOWLEDGEMENT
	}

	public class Packet {
		static final String ACK_PLUS = "+";
		static final String ACK_MINUS = "-";

		private PacketType type;
		private String data; // raw data of the packet
		private String desc; // descriptive text of the packet

		/**
		 * @param desc
		 * @param data
		 */
		public Packet(PacketType type, String desc, String data) {
			this.type = type;
			this.desc = desc;
			this.data = data;
		}

		public Packet() {
		}

		public PacketType getType() {
			return type;
		}

		public String getDescription() {
			return desc;
		}

		public String getData() {
			return data;
		}
	}

	public interface IPacketListener {
		/**
		 * called right before a packet is sent from host to target.
		 * 
		 * @param p
		 *            raw packet data with no framing.
		 */
		public void onPacketToBeSent(Packet p);

		/**
		 * called when a packet is sent from host to target.
		 * 
		 * @param p
		 *            raw packet data with no framing.
		 * @param err
		 *            null if the sending is successful, else something wrong.
		 */
		public void onPacketSent(Packet p, Exception err);

		/**
		 * Called when a packet from target is received by host.
		 * 
		 * @param p
		 *            raw packet data with no framing.
		 */
		public void onPacketReceived(Packet p);
	}

	/**
	 * Callback handler to handle response from gdbserver to our command.
	 * Subclass must implement handle().
	 */
	public static abstract class ResponseHandler {
		protected IChannel fTcfChannel;
		protected IToken fTcfCmdToken;

		/**
		 * @param tcfChannel
		 *            - TCF channel to report the response to TCF client, if
		 *            needed. Can be null.
		 * @param tcfCmdToken
		 *            - TCF command token if the command is sent on the request
		 *            of TCF. Can be null.
		 */
		public ResponseHandler(IChannel tcfChannel, IToken tcfCmdToken) {
			fTcfChannel = tcfChannel;
			fTcfCmdToken = tcfCmdToken;
		}

		/**
		 * A utility method.
		 * 
		 * @param obj_array
		 */
		protected void sendResult(final Object[] obj_array) {
			assert (fTcfChannel != null && fTcfCmdToken != null);
			if (fTcfChannel == null || fTcfCmdToken == null)
				return;

			Protocol.invokeLater(new Runnable() {
				public void run() {
					try {
						fTcfChannel.sendResult(fTcfCmdToken, JSON.toJSONSequence(obj_array));
					} catch (IOException e) {
						fTcfChannel.terminate(e);
					}
				}
			});
		}

		protected void sendError(String err_msg) {
			sendResult(new Object[] { AgentUtils.makeErrorReport(0, err_msg) });
		}

		protected void sendError(Exception e) {
			sendError(e.getLocalizedMessage());
		}

		/**
		 * Process the response, which may (not required) interpret the response
		 * and report back to TCF client. <br>
		 * 
		 * @param response
		 */
		public abstract void handle(Packet response);
	}

	/**
	 * Command to send to gdbserver.
	 */
	public static class Command {
		public Packet packet;
		public ResponseHandler rh; // this can be null if the command does not

		// expect a response

		public Command(Packet p, ResponseHandler rh) {
			packet = p;
			this.rh = rh;
		}
	}

	private ITransportChannel fTransportChannel;
	private static List<IPacketListener> fPacketListeners = new ArrayList<IPacketListener>();

	private LinkedList<Command> fPendingCmdQueue = new LinkedList<Command>();

	private Thread fCommunicationThread;

	public static void addPacketListener(IPacketListener pl) {
		fPacketListeners.add(pl);
	}

	public static void removePacketListener(IPacketListener pl) {
		fPacketListeners.remove(pl);
	}

	/**
	 * @param channel
	 */
	public GdbRemoteProtocol(ITransportChannel channel) {
		this.fTransportChannel = channel;

		/*
		 * This thread is supposed to handle all communication (sending /
		 * receiving packets) to gdbserver. Note that gdb remote protocol
		 * packets are not "numbered", namely you cannot tell by packet itself
		 * whether it's for which command. So we have to follow this workflow
		 * even for command like resume:
		 * 
		 * send command => get Ack => get response => send Ack => send next
		 * command
		 * 
		 * Even if non-stop mode is supported by gdbserver where a Notification
		 * may be sent at any time, this model should also work, I think, though
		 * we won't know until we can test it. See this for more:
		 * 
		 * http://sources.redhat.com/gdb/current/onlinedocs/gdb_34.html#SEC731
		 * 
		 * A new GdbRemoteProtoco object is created for each debug session, thus
		 * a new CommunicationThread. The thread should be interrupted on
		 * debugger termination.
		 */
		fCommunicationThread = new Thread("I/O Handler") {
			@Override
			public void run() {
				Command cmd;
				Packet p = new Packet();

				while (true) {
					synchronized (fPendingCmdQueue) {
						try {
							if (fPendingCmdQueue.isEmpty())
								fPendingCmdQueue.wait();
						} catch (InterruptedException e1) {
							// shutdown on debugger termination.
							// See where interrupt() is called for more.
							break;
						}

						cmd = fPendingCmdQueue.removeFirst();
					}

					try {
						sendPacket(cmd.packet);

						readPacket(p);

						if (p.getType() == PacketType.ACKNOWLEDGEMENT)
							readPacket(p);

						if (p.getType() != PacketType.ACKNOWLEDGEMENT) { // response
							// or
							// notification
							// Send ack.
							sendAcknowledgement();

							handleInput(p, cmd.rh);
						} else {
							// Unexpected ACK packet
							// Report to host debugger via TCF channel if
							// possible.
							if (cmd.rh != null)
								cmd.rh.sendError("Unexpected Acknowledgement received from gdbserver.");
						}
					} catch (AgentException e) {
						// gdbserver down or channel lost.
						break;
					}
				}
			}
		};

		fCommunicationThread.start();
	}

	public void dispose() {
		// Shutdown the communication thread.
		//
		if (fCommunicationThread != null)
			fCommunicationThread.interrupt();

		if (fTransportChannel != null)
			try {
				fTransportChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	protected void handleInput(Packet p, ResponseHandler rh) {

		switch (p.getType()) {
		case ACKNOWLEDGEMENT:
			assert (p.getData().equals(Packet.ACK_PLUS));
			break;

		case RESPONSE:
			if (rh != null)
				rh.handle(p);

			break;

		case NOTIFICATION:
			System.out.println("**** Oops, notification is not handled.");
			break;

		default:
			break;
		}
	}

	/*
	 * Note only one thread can be sending a packet at a time.
	 */
	protected synchronized void sendPacket(Packet p) throws AgentException {
		for (IPacketListener l : fPacketListeners) {
			l.onPacketToBeSent(p);
		}

		Exception err = null;
		try {
			if (p.type != PacketType.ACKNOWLEDGEMENT)
				fTransportChannel.put('$');

			int checksum = 0;
			byte[] bytes = p.data.getBytes();
			for (byte b : bytes) {
				fTransportChannel.put(b);
				checksum = (checksum + b) % 256;
			}

			if (p.type != PacketType.ACKNOWLEDGEMENT) {
				fTransportChannel.put('#');

				String scs = Integer.toHexString(checksum);
				/*
				 * must pad it to two-byte, otherwise command like
				 * $mb7ee7685,4#0a would be mistaken as $mb7ee7685,4#a and
				 * gdbserver would think it gets wrong checksum and wait for
				 * re-sending of the command, choking the whole debugger.
				 * .............. 06/18/09
				 */
				if (scs.length() == 1)
					scs = "0" + scs;
				assert (scs.length() == 2);
				fTransportChannel.put(scs.getBytes());
			}

			fTransportChannel.flush();
		} catch (IOException e) {
			err = e;
		} finally {
			for (IPacketListener l : fPacketListeners) {
				l.onPacketSent(p, err);
			}

			if (err != null)
				throw new AgentException("Error sending GDB remote protocol packet: " + p.desc, err);
		}
	}

	private void sendAcknowledgement() throws AgentException {
		Packet p = new Packet(PacketType.ACKNOWLEDGEMENT, "ACK", Packet.ACK_PLUS);
		sendPacket(p);
	}

	/*
	 * This will only be called in fCommunicationThread.
	 */
	private void readPacket(Packet p) throws AgentException {
		p.type = PacketType.UNKNOWN;

		try {
			char ch = (char) fTransportChannel.get();
			char previous = 0; // previous character

			switch (ch) {
			case '$': // response packet
			case '%': // notification
				if (ch == '$')
					p.type = PacketType.RESPONSE;
				else
					p.type = PacketType.NOTIFICATION;

				p.desc = p.type.toString();

				StringBuffer buf = new StringBuffer();
				int checksum = 0;

				while (true) {
					ch = (char) fTransportChannel.get();

					if (ch == '#') {
						// two checksum bytes.
						// TODO: Skip the check after verification...04/28/09
						//
						byte cs1 = (byte) fTransportChannel.get();
						byte cs2 = (byte) fTransportChannel.get();
						String str = new String(new byte[] { cs1, cs2 });
						int cs = Integer.parseInt(str, 16);

						int ncs = 0;
						for (int i = 0; i < buf.length(); i++)
							ncs = (ncs + buf.charAt(i)) % 256;

						checksum %= 256;
						assert (checksum == cs);
						break;
					} else {
						checksum += ch;

						/*
						 * Response data can be run-length encoded to save
						 * space. Run-length encoding replaces runs of identical
						 * characters with one instance of the repeated
						 * character, followed by a `*' and a repeat count. The
						 * repeat count is itself sent encoded, to avoid binary
						 * characters in data: a value of n is sent as n+29. For
						 * a repeat count greater or equal to 3, this produces a
						 * printable ASCII character, e.g. a space (ASCII code
						 * 32) for a repeat count of 3. (This is because
						 * run-length encoding starts to win for counts 3 or
						 * more.) Thus, for example, `0* ' is a run-length
						 * encoding of "0000": the space character after `*'
						 * means repeat the leading 0 32 - 29 = 3 more times.
						 */
						if (ch == '*') { // repeat of previous char
							assert (previous != 0);
							int repeatCount = fTransportChannel.get() - 29;
							for (int i = 0; i < repeatCount; i++) {
								buf.append(previous);
							}
						} else {
							buf.append(ch);
							previous = ch;
						}
					}
				}

				p.data = new String(buf);
				break;

			case '+':
			case '-':
				p.type = PacketType.ACKNOWLEDGEMENT;
				p.desc = "ACK";
				p.data = ch == '+' ? Packet.ACK_PLUS : Packet.ACK_MINUS;
				break;

			default:
				throw new AgentException("Unknown packet header: " + ch);
			}

		} catch (IOException e) {
			throw new AgentException("Error reading packet from target.", e);
		}

		// Notify listeners
		//
		for (IPacketListener l : fPacketListeners) {
			l.onPacketReceived(p);
		}
	}

	/**
	 * Register ResponseHandler (if any) for given command and then send the
	 * command.
	 * 
	 * @param p
	 *            Packet for a command.
	 * @param rh
	 *            ResponseHandler if the command incurs response. Can be null.
	 * @throws AgentException
	 */
	protected void sendCommand(Packet p, ResponseHandler rh) throws AgentException {
		synchronized (fPendingCmdQueue) {
			fPendingCmdQueue.add(new Command(p, rh));

			fPendingCmdQueue.notifyAll();
		}
	}

	public void resume(ResponseHandler rh) throws AgentException {
		Packet p = new Packet(PacketType.COMMAND, "Continue", "vCont;c");
		sendCommand(p, rh);
	}

	public void readRegister(int regNo, ResponseHandler rh) throws AgentException {
		Packet p = new Packet(PacketType.COMMAND, "Read register #" + regNo, "p" + regNo);
		sendCommand(p, rh);
	}

	public void readGeneralRegisters(ResponseHandler rh) throws AgentException {
		Packet p = new Packet(PacketType.COMMAND, "Read General Registers", "g");

		sendCommand(p, rh);
	}

	/**
	 * Execute one instruction on the given thread.
	 * 
	 * @param threadID
	 *            - hex string of the thread ID, e.g. "12a".
	 * @param rh
	 * @throws AgentException
	 */
	public void executeOneInstruction(String threadID, ResponseHandler rh) throws AgentException {
		Packet p = new Packet(PacketType.COMMAND, "Single-instruction Step", "vCont;s:" + threadID);
		sendCommand(p, rh);
	}

	/**
	 * Suspend a running thread. This is for non-stop mode only.
	 * 
	 * @param threadID
	 * @param rh
	 * @throws AgentException
	 */
	public void suspendOneThread(String threadID, ResponseHandler rh) throws AgentException {
		Packet p = new Packet(PacketType.COMMAND, "Suspend", "vCont;t:" + threadID);
		sendCommand(p, rh);
	}

	/**
	 * Write values to all general registers.
	 * 
	 * @param values
	 *            contain values of all GPRs in target processor. The encoding
	 *            is target specific.
	 * @param rh
	 * @throws AgentException
	 */
	public void writeGeneralRegisters(String values, ResponseHandler rh) throws AgentException {
		Packet p = new Packet(PacketType.COMMAND, "Write General Registers", "G" + values);

		sendCommand(p, rh);
	}

	public void readMemory(long addr, int byteCnt, ResponseHandler rh) throws AgentException {
		// Note we use java "long" (64-bit) to represent 32-bit unsigned long.
		String cmd = "m" + Long.toHexString(addr & 0xffffffffL) + "," + Integer.toHexString(byteCnt);
		Packet p = new Packet(PacketType.COMMAND, "Read Memory", cmd);

		sendCommand(p, rh);
	}

	public void writeMemory(long addr, int byteCnt, byte[] data, ResponseHandler rh) throws AgentException {
		// Note we use java "long" (64-bit) to represent 32-bit unsigned long.
		String cmd = "M" + Long.toHexString(addr & 0xffffffffL) + "," + Integer.toHexString(byteCnt) + ":"
				+ AgentUtils.byteArrayToHexString(data);
		Packet p = new Packet(PacketType.COMMAND, "Write Memory (M)", cmd);

		sendCommand(p, rh);
	}

	public void queryStubFeatures(ResponseHandler rh) throws AgentException {
		Packet p = new Packet(PacketType.COMMAND, "Query gdbserver features", "qSupported");

		sendCommand(p, rh);
	}

	/**
	 * Get list of loaded libraries.
	 * 
	 * @param rh
	 * @throws AgentException
	 */
	public void getLibraryList(ResponseHandler rh) throws AgentException {
		// But I only get empty list with Ubuntu desktop gdbserver. Why ??
		// llibrary-list>
		// </library-list>
		Packet p = new Packet(PacketType.COMMAND, "Get list of loaded libraries", "qXfer:libraries:read::0,1000");

		sendCommand(p, rh);
	}

	public void getThreads(ResponseHandler rh) throws AgentException {
		Packet p = new Packet(PacketType.COMMAND, "Get list of threads", "qfThreadInfo");

		sendCommand(p, rh);
	}

	/**
	 * This should be called after getThreadList(). See gdb remote protocol for
	 * more.
	 * 
	 * @param rh
	 * @throws AgentException
	 */
	public void getMoreThreads(ResponseHandler rh) throws AgentException {
		Packet p = new Packet(PacketType.COMMAND, "Get more threads", "qsThreadInfo");

		sendCommand(p, rh);
	}

	public void invokeRemoteCommand(String cmd, ResponseHandler rh) throws AgentException {
		String hexEncoded = AgentUtils.byteArrayToHexString(cmd.getBytes());
		Packet p = new Packet(PacketType.COMMAND, "Invoke remote command [" + cmd + "]", "qRcmd," + hexEncoded);

		sendCommand(p, rh);
	}

	/**
	 * Set a software breakpoint at given address with given size.<br>
	 * protocol command format: <br>
	 * $Z0,8048385,1#87
	 * 
	 * @param address
	 * @param size
	 *            - size in bytes of the software breakpoint. matters only for
	 *            targets like ARM ?
	 * @throws AgentException
	 */
	public void setSoftwareBreakpoint(long address, int byteCnt, ResponseHandler rh) throws AgentException {
		String cmd = "Z0" + "," + Long.toHexString(address) + "," + Integer.toHexString(byteCnt);
		Packet p = new Packet(PacketType.COMMAND, "Set software breakpoint at address", cmd);

		sendCommand(p, rh);
	}

	/**
	 * Set a hardware breakpoint at given address with given size.<br>
	 * protocol command format: <br>
	 * $Z1,8048385,1#87
	 * 
	 * @param address
	 * @param byteCnt
	 *            - size in bytes of the software breakpoint. matters only for
	 *            targets like ARM ?
	 * @param rh
	 *            - response handler
	 * @throws AgentException
	 */
	public void setHardwareBreakpoint(long address, int byteCnt, ResponseHandler rh) throws AgentException {
		String cmd = "Z1" + "," + Long.toHexString(address) + "," + Integer.toHexString(byteCnt);
		Packet p = new Packet(PacketType.COMMAND, "Set hardtware breakpoint at address", cmd);

		sendCommand(p, rh);
	}

	public abstract RegisterCache getRegisterCache(String contextID);

}