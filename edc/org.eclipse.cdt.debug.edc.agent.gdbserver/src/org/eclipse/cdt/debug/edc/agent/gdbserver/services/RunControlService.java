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

package org.eclipse.cdt.debug.edc.agent.gdbserver.services;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbserverAgent;
import org.eclipse.cdt.debug.edc.agent.gdbserver.RegisterCache;
import org.eclipse.cdt.debug.edc.agent.gdbserver.StopReplyPacket;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.Packet;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.ResponseHandler;
import org.eclipse.cdt.debug.edc.agent.gdbserver.StopReplyPacket.ChangedRegister;
import org.eclipse.cdt.debug.edc.agent.gdbserver.StopReplyPacket.StopReason;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentException;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentUtils;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.ContextInAgent;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.ContextManager;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.ThreadInAgent;
import org.eclipse.tm.tcf.core.AbstractChannel;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRunControl;

/**
 * TCF RunControl service in debugger agent.
 * 
 */
public class RunControlService implements IRunControl {

	private final IChannel fChannel;

	private class CommandServer implements IChannel.ICommandServer {

		public void command(IToken token, String name, byte[] data) {
			try {
				command(token, name, JSON.parseSequence(data));
			} catch (Throwable x) {
				fChannel.terminate(x);
			}
		}

		private void command(IToken token, String name, Object[] args) throws IOException {
			System.out.println("RunControlService Command:" + name);

			if (name.equals("getContext")) {
				if (args.length != 1) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}

				String contextID = (String) args[0];
				getContext(token, contextID);
			} else if (name.equals("resume")) {
				// See
				// org.eclipse.tm.internal.tcf.services.remote.RunControlProxy.RunContext.resume(int
				// mode, int count, DoneCommand done)
				if (args.length != 3) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}

				// this should be thread contextID.
				final String contextID = (String) args[0];
				int mode = ((Integer) args[1]).intValue();
				int count = ((Integer) args[2]).intValue();

				resume(token, contextID, mode, count);
			} else if (name.equals("suspend")) {
				if (args.length != 1) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}

				String contextID = (String) args[0];

				suspend(token, contextID);
			} else if (name.equals("terminate")) {
				if (args.length != 1) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}

				String contextID = (String) args[0];

				terminate(token, contextID);
			} else {
				fChannel.rejectCommand(token);
			}
		}

	}

	public RunControlService(IChannel channel) {
		this.fChannel = channel;
		channel.addCommandServer(this, new CommandServer());
	}

	private void getContext(IToken token, String contextID) throws IOException {
		ContextInAgent context = ContextManager.findDebuggedContext(contextID);

		if (context == null)
			

		fChannel.sendResult(token, JSON.toJSONSequence(new Object[] { 
				context == null ? AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER, "Context ID is invalid: " + contextID) : null, 
				context == null ? null : context.getProperties()
		}));
	}

	private void resume(IToken token, final String contextID, int mode, int count) throws IOException {
		Object err = null;

		try {
			final GdbRemoteProtocol protocol = GdbserverAgent.getInstance().getGdbRemoteProtocol();

			// flush register cache
			protocol.getRegisterCache(contextID).invalidateCache();

			ResponseHandler stopReplyHandler = new ResponseHandler(fChannel, token) {
				@Override
				public void handle(Packet response) {
					String threadID = "unknown";

					StopReplyPacket reply = new StopReplyPacket(response.getData());

					switch (reply.getType()) {

					case StopReplyPacket.TYPE_STOP:

						Map<String, Object> props = new HashMap<String, Object>();

						String processID = ProcessesService.getCurrentProcess();
						threadID = ThreadInAgent.createInternalID(reply.getThreadID(), processID); // internal
																									// ID

						// cache register values reported in the packet.
						//
						List<ChangedRegister> regs = reply.getChangedRegisters();

						RegisterCache regCache = protocol.getRegisterCache(threadID);

						for (ChangedRegister r : regs)
							try {
								regCache.cacheRegister(r.getRegisterNo(), r.getRegistValue());
							} catch (AgentException e) {
								sendError(e.getLocalizedMessage());
							}

						RunControlService rcService = (RunControlService) fChannel.getLocalService(IRunControl.NAME);
						if (rcService != null) {
							try {
								ThreadInAgent thread = (ThreadInAgent) ContextManager.findDebuggedContext(threadID);
								if (thread == null) {
									// Add thread
									//
									thread = new ThreadInAgent(reply.getThreadID(), ProcessesService
											.getCurrentProcess(), props, true);

									List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
									list.add(thread.getProperties());
									rcService.sendEvent("contextAdded", JSON.toJSONSequence(new Object[] { list }));

									// Add some delay between contextAdded &
									// contextException
									// event for a new thread, otherwise host
									// debugger sometimes fail to
									// mark the thread as suspended. Not clear
									// why yet.
									try {
										Thread.sleep(600);
									} catch (InterruptedException e) {
									}
								}

								// Mark it as suspended.
								if (reply.getStopReason() == StopReason.STOP_EXCEPTION) {
									rcService.sendEvent("contextException", JSON.toJSONSequence(new Object[] {
											threadID, reply.getStopReasonMessage() }));
								} else if (reply.getStopReason() == StopReason.STOP_BREAKPOINT) {
									rcService.sendEvent("contextSuspended", JSON.toJSONSequence(new Object[] {
											threadID, null, /* PC, ignored. */
											IRunControl.REASON_BREAKPOINT, thread.getProperties() }));
								} else if (reply.getStopReason() == StopReason.STOP_WATCHPOINT) {
									rcService.sendEvent("contextSuspended", JSON.toJSONSequence(new Object[] {
											threadID, null, /* PC, ignored. */
											IRunControl.REASON_WATCHPOINT, thread.getProperties() }));
								}

							} catch (IOException e) {
								sendError(e.getLocalizedMessage());
							}
						}
						break;

					case StopReplyPacket.TYPE_TERMINATED:
					case StopReplyPacket.TYPE_EXITED:
						try {
							terminate(null, threadID);
						} catch (IOException e1) {
							// ignore
						}

						break;

					case StopReplyPacket.TYPE_DEBUGGEE_OUTPUT:
						assert false : "Unsupported StopReply.";
						break;

					}

				}
			};

			if (mode == RM_RESUME)
				protocol.resume(stopReplyHandler);
			else if (mode == RM_STEP_INTO) { // execute single instruction
				String threadID = null;
				ContextInAgent c = ContextManager.findDebuggedContext(contextID);
				if (c instanceof ThreadInAgent) {
					// We need hex string here.
					threadID = Long.toHexString(((ThreadInAgent) c).getThreadID());
				}

				if (threadID == null)
					throw new AgentException("Invalid context for single stepping: " + c);

				protocol.executeOneInstruction(threadID, stopReplyHandler);
			} else {
				err = AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER, MessageFormat.format(
						"Resume mode {0} is not supported.", mode));
			}
		} catch (AgentException e) {
			err = AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER, e.getLocalizedMessage());
		}

		fChannel.sendResult(token, JSON.toJSONSequence(new Object[] { err }));
	}

	private void terminate(IToken token, String contextID) throws IOException {
		Object err = null;

		// Terminating single thread is not supported.
		// And we don't support multi-process debug yet with current gdbserver
		// agent.
		// So this termination will just terminate the only process being
		// debugged.
		// Currently we just do brutal killing of the "gdbserver" process.

		IService s = fChannel.getLocalService(IProcesses.NAME);
		if (s != null && s instanceof ProcessesService)
			((ProcessesService) s).terminate(contextID);
		else
			err = AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER,
					"No ProcessesService in the agent to carry out the \"terminate\".");

		if (token != null)
			fChannel.sendResult(token, JSON.toJSONSequence(new Object[] { err }));

		// Notify host that all contexts are "removed".
		// Threads first, then the process.
		// 
		ContextInAgent process = ContextManager.findDebuggedContext(ProcessesService.getCurrentProcess());
		if (process == null) { // should not happen
			assert false : "Wow, where's the main process ?";
			sendEvent("contextRemoved", JSON.toJSONSequence(new Object[] { ContextManager.getDebuggedContexts() }));
		} else {
			// threads first
			if (process.getChildren().size() > 0)
				sendEvent("contextRemoved", JSON.toJSONSequence(new Object[] { process.getChildren().toArray() }));

			// the process
			sendEvent("contextRemoved", JSON.toJSONSequence(new Object[] { new String[] { process.getID() } }));
		}

		GdbserverAgent.getInstance().setGdbRemoteProtocol(null);
	}

	private void suspend(IToken token, String contextID) throws IOException {

		Object err = null;

		String cmd = "kill -2 " + contextID;

		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e1) {
			err = AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER, "Fail to send interrupt signal to "
					+ contextID);
		}

		/*
		 * This would only work for non-stop mode, which we don't support yet.
		 * try { final GdbRemoteProtocol protocol =
		 * GdbserverAgent.getInstance().getGdbRemoteProtocol();
		 * protocol.suspendOneThread(contextID, new ResponseHandler(fChannel,
		 * token) {
		 * 
		 * @Override public void handle(Packet response) {
		 * 
		 * } }); } catch (AgentException e) { err =
		 * AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER,
		 * e.getLocalizedMessage()); }
		 */

		fChannel.sendResult(token, JSON.toJSONSequence(new Object[] { err }));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IRunControl#addListener(org.eclipse.tm.tcf
	 * .services.IRunControl.RunControlListener)
	 */
	public void addListener(RunControlListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IRunControl#getChildren(java.lang.String,
	 * org.eclipse.tm.tcf.services.IRunControl.DoneGetChildren)
	 */
	public IToken getChildren(String parentContextId, DoneGetChildren done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.services.IRunControl#getContext(java.lang.String,
	 * org.eclipse.tm.tcf.services.IRunControl.DoneGetContext)
	 */
	public IToken getContext(String id, DoneGetContext done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IRunControl#removeListener(org.eclipse.tm
	 * .tcf.services.IRunControl.RunControlListener)
	 */
	public void removeListener(RunControlListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.protocol.IService#getName()
	 */
	public String getName() {
		return IRunControl.NAME;
	}

	public void sendEvent(String name, byte[] args) {
		((AbstractChannel) fChannel).sendEvent(this, name, args);
	}
}
