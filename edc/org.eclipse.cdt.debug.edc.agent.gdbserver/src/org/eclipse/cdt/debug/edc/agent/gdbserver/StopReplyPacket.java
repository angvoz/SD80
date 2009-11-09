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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.edc.tcf.extension.AgentUtils;

/**
 * Stop reply packet in GDB remote protocol.<br>
 * 
 * @see <a
 *      href="http://sources.redhat.com/gdb/current/onlinedocs/gdb_34.html#SEC724">Stop
 *      Reply Packets</a>
 */
public class StopReplyPacket {

	public enum StopReason {
		STOP_BREAKPOINT, STOP_WATCHPOINT, STOP_EXCEPTION, STOP_REPLAY_END,
	};

	/**
	 * (id, value) pairs for changed registers reported in the stop reply.
	 */
	public class ChangedRegister {
		private int registerNo;
		private String registerValue;

		public ChangedRegister(int registerNo, String registerValue) {
			this.registerNo = registerNo;
			this.registerValue = registerValue;
		}

		public int getRegisterNo() {
			return registerNo;
		}

		public String getRegistValue() {
			return registerValue;
		}
	}

	final static public int TYPE_UNKOWN = 0;
	final static public int TYPE_STOP = 1;
	final static public int TYPE_EXITED = 2;
	final static public int TYPE_TERMINATED = 3;
	final static public int TYPE_DEBUGGEE_OUTPUT = 4;

	private int type = TYPE_UNKOWN;
	private int signalNumber;
	private List<ChangedRegister> changedRegisters = new ArrayList<ChangedRegister>();
	private int processID;
	private int threadID;
	private StopReason stopReason = StopReason.STOP_BREAKPOINT;
	private String stopReasonMessage;
	private boolean libraryChanged = false; // loaded libraries have changed.
	private long watchpointAddress;

	private int exitStatus;
	private String debuggeeOutput;
	private boolean replayEndAtBeginning;

	/**
	 * Parse the reply string from gdbserver and construct an StopReplyPacket
	 * object.
	 * 
	 * @param reply
	 *            reply string from gdbserver.
	 */
	public StopReplyPacket(String reply) {
		StringTokenizer tk;

		char typeChar = reply.charAt(0);

		switch (typeChar) {
		case 'S':
		case 'T':
			// Example #1: stop-on-breakpoint
			// T0505:e8c08bbf;04:d0c08bbf;08:d3830408;thread:5abb;
			// Example #2: stop-on-exception
			// T0805:c8e6ebbf;04:c0e6ebbf;08:b7830408;thread:70eb;
			//
			type = TYPE_STOP;
			signalNumber = Integer.parseInt(reply.substring(1, 3), 16);

			if (signalNumber == 0x05) { // TRAP
				// default to breakpoint. May change to "watchpoint" later.
				stopReason = StopReason.STOP_BREAKPOINT;
				stopReasonMessage = "Breakpoint";
			} else {
				stopReason = StopReason.STOP_EXCEPTION;
				stopReasonMessage = UnixSignal.getSignalString(signalNumber);
			}

			if (reply.length() > 3) {
				// now parse the "n:r" pairs
				// The `n:r' pairs can carry values of important registers and
				// other information directly in the stop reply packet, reducing
				// round-trip latency.
				//
				tk = new StringTokenizer(reply.substring(3), ":;");

				while (tk.hasMoreTokens()) {
					String n = tk.nextToken();
					if (!tk.hasMoreTokens()) { // invalid reply. should not
						// happen ?
						break;
					}
					String r = tk.nextToken();

					if (Character.digit(n.charAt(0), 16) != -1) { // is hex
						// digit
						int regno = Integer.valueOf(n, 16);
						changedRegisters.add(new ChangedRegister(regno, r));
					} else if (n.equals("thread")) {
						threadID = Integer.valueOf(r, 16); // "r" is in
						// big-endian
					} else if (n.equals("watch") || n.equals("rwatch") || n.equals("awatch")) {
						assert signalNumber == 0x05;
						stopReason = StopReason.STOP_WATCHPOINT;
						watchpointAddress = Long.valueOf(r, 16);
						stopReasonMessage = "Watchpoint at " + r;
					} else if (n.equals("library")) {
						// The packet indicates that the loaded libraries have
						// changed. Debugger should use `qXfer:libraries:read'
						// to
						// fetch a new list of loaded libraries. r is ignored.
						libraryChanged = true;
					} else if (n.equals("replaylog")) {
						// The packet indicates that the target cannot continue
						// replaying logged execution events, because it has
						// reached the end (or the beginning when executing
						// backward) of the log. The value of r will be either
						// `begin' or `end'.
						stopReason = StopReason.STOP_REPLAY_END;
						replayEndAtBeginning = r.equals("begin");
					} else {
						// ignore unknown.
					}
				}
			}

			break;

		case 'W':
			// `W AA'
			// `W AA ; process:pid'
		case 'X':
			// `X AA'
			// `X AA ; process:pid'
			if (typeChar == 'W') {
				type = TYPE_EXITED;
				exitStatus = Integer.parseInt(reply.substring(1, 3), 16);
			} else {
				type = TYPE_TERMINATED;
				signalNumber = Integer.parseInt(reply.substring(1, 3), 16);
			}

			if (reply.length() > 3) {
				int lastColon = reply.lastIndexOf(':');
				if (lastColon != -1) {
					processID = Integer.valueOf(reply.substring(lastColon + 1), 16);
				}
			}
			break;

		case 'O':
			type = TYPE_DEBUGGEE_OUTPUT;
			debuggeeOutput = new String(AgentUtils.hexStringToByteArray(reply.substring(1)));
			break;

		case 'F': // system-call request
			assert false : "System call request is not supported.";
			break;

		default:
			break;
		}
	}

	public int getType() {
		return type;
	}

	public int getSignalNumber() {
		return signalNumber;
	}

	public List<ChangedRegister> getChangedRegisters() {
		return changedRegisters;
	}

	public int getProcessID() {
		return processID;
	}

	public int getThreadID() {
		return threadID;
	}

	public StopReason getStopReason() {
		return stopReason;
	}

	public String getStopReasonMessage() {
		return stopReasonMessage;
	}

	public boolean isLibraryChanged() {
		return libraryChanged;
	}

	public long getWatchpointAddress() {
		return watchpointAddress;
	}

	public int getExitStatus() {
		return exitStatus;
	}

	public String getDebuggeeOutput() {
		return debuggeeOutput;
	}

	public boolean isReplayEndAtBeginning() {
		return replayEndAtBeginning;
	}
}
