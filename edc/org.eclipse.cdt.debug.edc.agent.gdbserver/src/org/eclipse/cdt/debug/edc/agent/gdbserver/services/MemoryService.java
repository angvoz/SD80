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

package org.eclipse.cdt.debug.edc.agent.gdbserver.services;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbserverAgent;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.Packet;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentException;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentUtils;
import org.eclipse.tm.tcf.core.Base64;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IMemory;

/**
 * @author LWang
 * 
 */
public class MemoryService implements IMemory {

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
			System.out.println("MemoryService Command:" + name);

			if (name.equals("getContext")) {
				if (args.length != 1) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}

				// The process.
				//
				String contextid = (String) args[0];

				// Now return the properties for the MemoryContext.
				// See:
				// org.eclipse.tm.internal.tcf.services.remote.MemoryProxy.MemContext.
				//

				String processID = contextid;
				Map<String, Object> props = new HashMap<String, Object>();
				props.put(IMemory.PROP_ID, contextid);
				props.put(IMemory.PROP_PARENT_ID, processID);
				props.put(IMemory.PROP_ADDRESS_SIZE, new Integer(4));
				ArrayList<String> access_types = new ArrayList<String>();
				access_types.add(ACCESS_INSTRUCTION);
				access_types.add(ACCESS_DATA);
				props.put(IMemory.PROP_ACCESS_TYPES, access_types);
				props.put(IMemory.PROP_BIG_ENDIAN, Boolean.FALSE);
				props.put(IMemory.NAME, "Process:" + contextid);
				// TODO: get real range ?
				props.put(IMemory.PROP_START_BOUND, new Long(0));
				props.put(IMemory.PROP_END_BOUND, new Long(0xffffffff));

				fChannel.sendResult(token, JSON.toJSONSequence(new Object[] { null, props }));
			} else if (name.equals("get")) {
				if (args.length != 5) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}

				//String contextid = (String) args[0];
				final Number addr = (Number) args[1];
				//Integer word_size = (Integer) args[2]; // what's this for ?
				Integer size = (Integer) args[3]; // number of bytes to read
				//Integer mode = (Integer) args[4]; // what's this ?

				try {
					GdbserverAgent.getInstance().getGdbRemoteProtocol().readMemory(addr.longValue(), size,
							new GdbRemoteProtocol.ResponseHandler(fChannel, token) {

								@Override
								public void handle(Packet response) {

									/*
									 * Note, when returning error packets, pass an empty string for the
									 * BASE64-encoded memory.  This is accepted by Base64#toByteArray
									 * and is never decoded to be longer than the request size.
									 */
									
									// Got error from gdbserver
									if (response.getData().startsWith("E")) {
										sendResult(new Object[] {
												"" /* ignore */,
												AgentUtils.makeErrorReport(1, MessageFormat.format(
														"gdbserver returns error on reading memory at address {0}",
														Long.toHexString(addr.longValue() & 0xffffffffL))), null });

										return;
									}

									String tcf_ret = null;
									try {
										// TCF requires memory data be Base64
										// encoded.
										//
										byte[] ba = AgentUtils.hexStringToByteArray(response.getData());
										char[] ca = Base64.toBase64(ba, 0, ba.length);
										tcf_ret = new String(ca);
									} catch (Exception e) {
										sendResult(new Object[] { "" /* ignore */,
												AgentUtils.makeErrorReport(1, e.getLocalizedMessage()), null });
										return;
									}

									// Send the result
									//
									// TODO: what are these objects (data &
									// ranges) for ?
									sendResult(new Object[] { tcf_ret, null, null });
								}
							});
				} catch (AgentException e) {
					fChannel.sendResult(token, JSON.toJSONSequence(new Object[] { AgentUtils.makeErrorReport(0, e
							.getLocalizedMessage()) }));
				}
			} else if (name.equals("set")) {
				if (args.length != 6) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}

				//String contextid = (String) args[0];
				final Number addr = (Number) args[1];
				//Integer word_size = (Integer) args[2]; // what's this for ?
				Integer size = (Integer) args[3]; // number of bytes to write
				//Integer mode = (Integer) args[4]; // what's this ?
				byte[] bytes = (byte[]) args[5];

				/*
				 * Since 20080713, no Base64 encoding is used any more. See
				 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=282840
				 * 
				 * String data = (String)args[5]; byte[] bytes = new byte[size];
				 * Base64.toByteArray(bytes, 0, size, data.toCharArray());
				 */

				try {

					GdbserverAgent.getInstance().getGdbRemoteProtocol().writeMemory(addr.longValue(), size, bytes,
							new GdbRemoteProtocol.ResponseHandler(fChannel, token) {

								@Override
								public void handle(Packet response) {

									// Got error from gdbserver
									if (response.getData().startsWith("E")) {
										sendResult(new Object[] {
												AgentUtils.makeErrorReport(1, MessageFormat.format(
														"gdbserver returns error on writing memory at address {0}",
														Long.toHexString(addr.longValue() & 0xffffffffL))), null });

										return;
									}

									// TODO: what are these objects (data &
									// ranges) for ?
									sendResult(new Object[] { null, null });
								}
							});
				} catch (AgentException e) {
					fChannel.sendResult(token, JSON.toJSONSequence(new Object[] {
							AgentUtils.makeErrorReport(0, e.getLocalizedMessage()), null }));
				}
			} else
				fChannel.rejectCommand(token);
		}

	}

	public MemoryService(IChannel channel) {
		this.fChannel = channel;
		channel.addCommandServer(this, new CommandServer());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IMemory#addListener(org.eclipse.tm.tcf.services
	 * .IMemory.MemoryListener)
	 */
	public void addListener(MemoryListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.services.IMemory#getChildren(java.lang.String,
	 * org.eclipse.tm.tcf.services.IMemory.DoneGetChildren)
	 */
	public IToken getChildren(String parentContextId, DoneGetChildren done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.services.IMemory#getContext(java.lang.String,
	 * org.eclipse.tm.tcf.services.IMemory.DoneGetContext)
	 */
	public IToken getContext(String id, DoneGetContext done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IMemory#removeListener(org.eclipse.tm.tcf
	 * .services.IMemory.MemoryListener)
	 */
	public void removeListener(MemoryListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.protocol.IService#getName()
	 */
	public String getName() {
		return IMemory.NAME;
	}

}
