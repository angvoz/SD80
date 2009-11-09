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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbserverAgent;
import org.eclipse.cdt.debug.edc.agent.gdbserver.RegisterCache;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.Packet;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.ResponseHandler;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentException;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentUtils;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ISimpleRegisters;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;

/**
 * @author LWang
 * 
 */
public class SimpleRegistersService implements ISimpleRegisters {

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
			System.out.println("SimpleRegistersService Command:" + name);

			if (name.equals("get")) {
				if (args.length != 2) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}
				final String contextID = (String) args[0];
				final String[] regNames = AgentUtils.toStringArray(args[1]);

				try {
					final RegisterCache cache = GdbserverAgent.getInstance().getGdbRemoteProtocol().getRegisterCache(
							contextID);

					boolean allCached = true;

					for (String r : regNames) {
						if (!cache.isRegisterCached(r)) {
							allCached = false;
							break;
						}
					}

					if (allCached) {
						readRegisters(cache, regNames, token);
					} else {
						GdbserverAgent.getInstance().getGdbRemoteProtocol().readGeneralRegisters(
								new ResponseHandler(fChannel, token) {
									@Override
									public void handle(Packet p) {

										try {
											cache.updateCache(p);

											readRegisters(cache, regNames, fTcfCmdToken);

										} catch (AgentException e) {
											sendError(e);
										} catch (IOException e) {
											sendError(e);
										}
									}
								});
					}
				} catch (AgentException e) {
					fChannel.sendResult(token, AgentUtils.jsonErr(e.getLocalizedMessage()));
				}
			} else if (name.equals("set")) {
				if (args.length != 3) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}
				final String contextID = (String) args[0];
				final String[] regNames = AgentUtils.toStringArray(args[1]);
				final String[] regValues = AgentUtils.toStringArray(args[2]);
				// we only support one register write as there is no rule on
				// encoding of the values, e.g. how many bytes does one value
				// have ?
				assert (regValues.length == 1);
				if (regValues.length != 1) {
					fChannel.sendResult(token, AgentUtils.jsonErr("Only writing one register at a time is allowed."));
					return;
				}

				// Assume register size is 4-bytes
				if (regValues[0].length() < 8) {
					regValues[0] = pad8(regValues[0]);
				}

				if (regNames.length != regValues.length) {
					fChannel
							.sendResult(
									token,
									AgentUtils
											.jsonErr("Illegal Argument: size of reg name array is different from size of the value array."));
					return;
				}

				try {
					final RegisterCache cache = GdbserverAgent.getInstance().getGdbRemoteProtocol().getRegisterCache(
							contextID);

					if (cache.isCacheValid()) {
						writeRegistersInBatch(cache, regNames, regValues, token);
					} else {
						// Update cache first by reading
						//
						GdbserverAgent.getInstance().getGdbRemoteProtocol().readGeneralRegisters(
								new ResponseHandler(fChannel, token) {
									@Override
									public void handle(Packet p) {

										try {
											cache.updateCache(p);

											writeRegistersInBatch(cache, regNames, regValues, fTcfCmdToken);

										} catch (AgentException e) {
											sendError(e);
										} catch (IOException e) {
											sendError(e);
										}
									}
								});
					}
				} catch (AgentException e) {
					fChannel.sendResult(token, AgentUtils.jsonErr(e.getLocalizedMessage()));
				}
			} else
				fChannel.rejectCommand(token);
		}

	}

	public SimpleRegistersService(IChannel channel) {
		this.fChannel = channel;
		channel.addCommandServer(this, new CommandServer());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.debug.edc.services.tcf.ISimpleRegisters#get(java.lang
	 * .String, java.lang.String[],
	 * org.eclipse.cdt.debug.edc.services.tcf.ISimpleRegisters.DoneGet)
	 */
	public IToken get(String executionContextID, String[] registerIDs, DoneGet done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.debug.edc.services.tcf.ISimpleRegisters#set(java.lang
	 * .String, java.lang.String[], java.lang.String[],
	 * org.eclipse.cdt.debug.edc.services.tcf.ISimpleRegisters.DoneSet)
	 */
	public IToken set(String executionContextID, String[] registerIDs, String[] registerValues, DoneSet done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.protocol.IService#getName()
	 */
	public String getName() {
		return ISimpleRegisters.NAME;
	}

	private void writeRegistersInBatch(RegisterCache cache, String[] regNames, String[] regValues, IToken tcfCmdToken)
			throws IOException, AgentException {
		try {
			for (int i = 0; i < regNames.length; i++)
				cache.cacheRegister(regNames[i], RegisterCache.Swap4(regValues[i]));
		} catch (AgentException e) {
			fChannel.sendResult(tcfCmdToken, AgentUtils.jsonErr(e.getLocalizedMessage()));
			return;
		}

		GdbserverAgent.getInstance().getGdbRemoteProtocol().writeGeneralRegisters(cache.toString(),
				new ResponseHandler(fChannel, tcfCmdToken) {
					@Override
					public void handle(Packet p) {

						try {
							if (p.getData().equals("OK"))
								fTcfChannel.sendResult(fTcfCmdToken, JSON.toJSONSequence(new Object[] { null }));
							else
								sendError("Fail to write registers.");
						} catch (IOException e) {
							sendError(e);
						}
					}
				});
	}

	private void readRegisters(RegisterCache cache, String[] regNames, IToken token) throws IOException {
		final List<String> vals = new ArrayList<String>();

		try {
			for (String r : regNames) {
				String v = cache.getRegisterValue(r);
				vals.add(v);
			}

		} catch (AgentException e) {
			fChannel.sendResult(token, AgentUtils.jsonErr(e.getLocalizedMessage()));
			return;
		}

		try {
			fChannel.sendResult(token, JSON.toJSONSequence(new Object[] { null, // error
					vals }));
		} catch (IOException e) {
			fChannel.sendResult(token, AgentUtils.jsonErr(e.getLocalizedMessage()));
		}
	}

	private static String pad8(String val) {
		StringBuffer ret = new StringBuffer();
		for (int i = 0; i < 8 - val.length(); i++)
			ret.append("0");

		ret.append(val);
		return ret.toString();
	}

}
