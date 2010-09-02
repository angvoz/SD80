/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nokia - Initial API and implementation.  Mar, 2010
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.agent.gdbserver.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbserverAgent;
import org.eclipse.cdt.debug.edc.agent.gdbserver.RegisterCache;
import org.eclipse.cdt.debug.edc.agent.gdbserver.RegisterInfoX86InGDB;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.Packet;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.ResponseHandler;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentException;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentUtils;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.ContextInAgent;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.ContextManager;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.IRegisterInfo;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.RegisterGroupInAgent;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.RegisterInAgent;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.ThreadInAgent;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.IRegisterInfo.RegisterGroupInfo;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.IRegisterInfo.RegisterInfo;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IRegisters;

/**
 * IRegisters service. 
 */
public class RegistersService implements IRegisters {

	private final IChannel fChannel;

	private class CommandServer implements IChannel.ICommandServer {

		public void command(IToken token, String name, byte[] data) {
			try {
				command(token, name, JSON.parseSequence(data));
			} catch (Throwable x) {
				fChannel.terminate(x);
			}
		}

		private void command(IToken token, String name, Object[] args) throws Exception {

			String methodName = "_" + name;

			Method handler = null;
			try {
				handler = this.getClass().getMethod(methodName, new Class[] { IToken.class, String.class, Object[].class });
			} catch (SecurityException e) {
				// should not happen
				return;
			} catch (NoSuchMethodException e) {
				// Command not supported yet.
				fChannel.rejectCommand(token);
				return;
			}

			try {
				handler.invoke(this, new Object[] { token, name, args });
			} catch (IllegalArgumentException e) {
				// should not happen
			} catch (IllegalAccessException e) {
				// should not happen
			} catch (InvocationTargetException e) {
				throw (Exception)e.getCause();
			}
		}

		@SuppressWarnings("unused")	// implicitly used
		public void _getChildren(IToken token, String cmdName, Object[] args) throws Exception {
			if (args.length != 1) {
				fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
				return;
			}

			String[] result = new String[0];
			
			String contextID = (String)args[0];
			ContextInAgent context = ContextManager.findDebuggedContext(contextID);
			if (context == null) {
				fChannel.sendResult(token, JSON.toJSONSequence(new Object[] {
						AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER, "Invalid context id: " + contextID), 
						result}));
				return;
			}
			
			List<String> children = context.getChildren();
			
			if (context instanceof ThreadInAgent) {
				// Currently it's assumed thread only has register group
				// contexts as children.
				// And we hook up the register children to a thread only 
				// when requested. This way we don't bother adding registers
				// for a thread that user does not care about.
				//  ..................02/11/10
				if (children.size() == 0) {
					// Add register contexts for the thread when accessed.
					children = addRegisterContextsForThread(contextID);
				}
			}
			else {
				// for RegisterGroupInAgent, children are registers.
				// for Register context, possible children are bit-fields.
			}

			result = children.toArray(new String[children.size()]);
			
			fChannel.sendResult(token, JSON.toJSONSequence(new Object[] {null,	result}));
		}

		/**
		 * Add register group & register contexts for the give thread context.
		 * 
		 * @param threadContextID
		 * @return IDs of register group contexts supported. 
		 */
		private List<String> addRegisterContextsForThread(String threadContextID) {
			
			// Get static register info first. 
			List<RegisterGroupInfo> rgInfoList = RegisterInfoX86InGDB.getInstance().getRegisterGroupInfo();
			
			List<String> result = new ArrayList<String>(rgInfoList.size());
			
			// Now add thread-specific register contexts.
			//
			for (IRegisterInfo.RegisterGroupInfo rg : rgInfoList) {
				Map<String, Object> props = rg.getProperties();
				
				// This will be added as child context of the thread.
				RegisterGroupInAgent rgContext = new RegisterGroupInAgent(
					(String)props.get(IRegisters.PROP_NAME), threadContextID, props);
				
				String rgContextID = rgContext.getID(); 
				result.add(rgContextID);
				
				// Now add register contexts under the register group context
				//
				RegisterInfo[] regs = rg.getRegisters();
				for (RegisterInfo r : regs) {
					props = r.getProperties();
					new RegisterInAgent((String)props.get(IRegisters.PROP_NAME), rgContextID, props);
				}
			}
			return result; 
		}

		/**
		 * Handler for getContext command.
		 * 
		 * @param token
		 * @param cmdName
		 * @param args
		 * @throws Exception
		 */
		@SuppressWarnings("unused")	// implicitly used
		public void _getContext(IToken token, String cmdName, Object[] args) throws Exception {
			if (args.length != 1) {
				fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
				return;
			}

			String contextID = (String)args[0];
			ContextInAgent context = ContextManager.findDebuggedContext(contextID);
			
			fChannel.sendResult(token, JSON.toJSONSequence(new Object[] {
					context == null ? AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER, "Invalid context id: " + contextID) : null, 
					context == null ? null : context.getProperties()}));
		}

		@SuppressWarnings("unused")	// implicitly used
		public void _get(IToken token, String cmdName, Object[] args) throws Exception {
			if (args.length != 1) {
				fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
				return;
			}

			String contextID = (String)args[0];
			ContextInAgent context = ContextManager.findDebuggedContext(contextID);

			if (context == null || ! (context instanceof RegisterInAgent)) {
				fChannel.sendResult(token, JSON.toJSONSequence(new Object[] {
						AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER, "Invalid register context id: " + contextID), 
						""}));
				return;
			}

			int rID = (Integer)context.getProperties().get(RegisterInfoX86InGDB.PROP_GDB_REGISTER_ID);
			
			final int[] regIDs = new int[] { rID };

			try {
				final RegisterCache cache = GdbserverAgent.getInstance().getGdbRemoteProtocol().getRegisterCache(
						contextID);

				boolean allCached = true;

				for (int r : regIDs) {
					if (!cache.isRegisterCached(r)) {
						allCached = false;
						break;
					}
				}

				if (allCached) {
					readRegisters(cache, regIDs, token);
				} else {
					GdbserverAgent.getInstance().getGdbRemoteProtocol().readGeneralRegisters(
							new ResponseHandler(fChannel, token) {
								@Override
								public void handle(Packet p) {

									try {
										cache.updateCache(p);

										readRegisters(cache, regIDs, fTcfCmdToken);

									} catch (AgentException e) {
										sendError(e);
									} catch (IOException e) {
										sendError(e);
									}
								}
							});
				}
			} catch (AgentException e) {
				fChannel.sendResult(token, JSON.toJSONSequence(new Object[] {
						AgentUtils.jsonErr(e.getLocalizedMessage()), null}));
			}
		}

		@SuppressWarnings("unused")	// implicitly used
		public void _set(IToken token, String cmdName, Object[] args) throws Exception {
			if (args.length != 2) {
				fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
				return;
			}

			String contextID = (String)args[0];
			ContextInAgent context = ContextManager.findDebuggedContext(contextID);

			if (context == null || ! (context instanceof RegisterInAgent)) {
				fChannel.sendResult(token, JSON.toJSONSequence(new Object[] {
						AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER, "Invalid register context id: " + contextID), 
						""}));
				return;
			}

			Integer regID = (Integer)context.getProperties().get(RegisterInfoX86InGDB.PROP_GDB_REGISTER_ID);
			byte[] val = JSON.toByteArray(args[1]);	// big-endian

			final int[] regIDs = new int[] {regID};
			final String[] regValues = new String[] {AgentUtils.byteArrayToHexString(val)};

			// Change current thread of gdb. TBD. 
			// String threadCxtID = getThreadContextID(contextID);
			
			try {
				final RegisterCache cache = GdbserverAgent.getInstance().getGdbRemoteProtocol().getRegisterCache(
						contextID);

				if (cache.isCacheValid()) {
					writeRegistersInBatch(cache, regIDs, regValues, token);
				} else {
					// Update cache first by reading
					//
					GdbserverAgent.getInstance().getGdbRemoteProtocol().readGeneralRegisters(
							new ResponseHandler(fChannel, token) {
								@Override
								public void handle(Packet p) {

									try {
										cache.updateCache(p);

										writeRegistersInBatch(cache, regIDs, regValues, fTcfCmdToken);

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
		}


		/**
		 * With GDB protocol "G" command, we have to write whole group of registers in order to change 
		 * one register value.
		 *
		 * @param cache
		 * @param regIDs
		 * @param regValues	 value in big-endian hex string.
		 * @param tcfCmdToken
		 * @throws IOException
		 * @throws AgentException
		 */
		private void writeRegistersInBatch(RegisterCache cache, int[] regIDs, String[] regValues, IToken tcfCmdToken)	throws IOException, AgentException 
		{
			try {
				for (int i = 0; i < regIDs.length; i++)
					cache.cacheRegister(regIDs[i], RegisterCache.Swap4(regValues[i])); // cache expects little-endian
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

		@SuppressWarnings("unused")	// implicitly used
		public void _getm(IToken token, String cmdName, Object[] args) throws Exception {
			fChannel.rejectCommand(token);
		}

		@SuppressWarnings("unused")	// implicitly used
		public void _setm(IToken token, String cmdName, Object[] args) throws Exception {
			fChannel.rejectCommand(token);
		}

		@SuppressWarnings({ "unused", "unchecked" })	// implicitly used
		public void _search(IToken token, String cmdName, Object[] args) throws Exception {
			if (args.length != 2) {
				fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
				return;
			}

			String contextID = (String)args[0];
			ContextInAgent context = ContextManager.findDebuggedContext(contextID);

			if (context == null || ! (context instanceof RegisterInAgent)) {
				fChannel.sendResult(token, JSON.toJSONSequence(new Object[] {
						AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER, "Invalid context id: " + contextID), 
						""}));
				return;
			}

			Map<String, Object> filter = (Map<String, Object>) args[1];
			
			String propName = (String)filter.get("Name");
			Object propValue = filter.get("EqualValue");
			List<List<String>> paths = searchForContext(context, propName, propValue);

			fChannel.sendResult(token, JSON.toJSONSequence(new Object[] { paths }));
		}

		/**
		 * Get thread context ID for the given context, usually child or
		 * grandchild context of a thread such as register.
		 * 
		 * @param contextID
		 * @return owning thread context id.
		 */
//		private String getThreadContextID(String contextID) {
//			String ret = null;
//			while (true){
//				ContextInAgent context = ContextManager.findDebuggedContext(contextID);
//				if (context == null)
//					break;
//				if (context instanceof ThreadInAgent) {
//					ret = contextID;
//					break;
//				}
//				contextID = context.getParentID();
//			};
//			
//			return ret;
//		}
// unused; remove?

		private List<List<String>> searchForContext(ContextInAgent startContext, String propName, Object propValue) {
			List<List<String>> result = new ArrayList<List<String>>();
			
			List<String> matchingContexts = new ArrayList<String>();
			for (String childID : startContext.getChildren()) {
				matchingContexts.addAll(doSearchForContexts(childID, propName, propValue));
			}

			for (String cid : matchingContexts) {
				List<String> path = getPathToContext(startContext.getID(), cid);
				result.add(path);
			}
			
			return result;
		}

		/**
		 * Get path from startContext (exclusive) to the context.
		 * @param startConextID
		 * @param contextID
		 * @return
		 */
		private List<String> getPathToContext(String startConextID, String contextID) {
			List<String> result = new ArrayList<String>();
			
			String id = contextID;
			result.add(0, id);

			while (true) {
				id = ContextManager.findDebuggedContext(id).getParentID();
				if (id.equals(startConextID))
					break;

				result.add(0, id);
			}
			
			return result;
		}

		private List<String> doSearchForContexts(String startConextID, String propName, Object propValue) {
			List<String> result = new ArrayList<String>();
			
			ContextInAgent cxt = ContextManager.findDebuggedContext(startConextID);
			if (cxt.getProperties().get(propName).equals(propValue))
				result.add(startConextID);
			
			for (String id : cxt.getChildren())
				// Recursive call
				result.addAll(doSearchForContexts(id, propName, propValue));
			
			return result;
		}

		private void readRegisters(RegisterCache cache, int[] regIDs, IToken token) throws IOException {
			byte[] result; 

			try {
				if (regIDs.length == 1)
					result = cache.getRegisterValueAsBytes(regIDs[0]);
				else {
					ByteArrayOutputStream bos = new ByteArrayOutputStream(regIDs.length * 4); // minimum size
					for (int i : regIDs) {
						byte[] bs = cache.getRegisterValueAsBytes(i);
						bos.write(bs);
					}
					
					result = bos.toByteArray();
				}
			} catch (AgentException e) {
				fChannel.sendResult(token, JSON.toJSONSequence(new Object[] {
						AgentUtils.jsonErr(e.getLocalizedMessage()), null}));
				return;
			}

			try {
				// The JSON.Binary object will be converted to Base64-encoded byte array
				// in JSON.toJSONSequence.
				fChannel.sendResult(token, JSON.toJSONSequence(new Object[] { null, new JSON.Binary(result, 0, result.length) }));
			} catch (IOException e) {
				fChannel.sendResult(token, JSON.toJSONSequence(new Object[] {
						AgentUtils.jsonErr(e.getLocalizedMessage()), null}));
			}
		}

	}

	public RegistersService(IChannel channel) {
		this.fChannel = channel;
		channel.addCommandServer(this, new CommandServer());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.IRegisters#addListener(org.eclipse.tm.tcf.services.IRegisters.RegistersListener)
	 */
	public void addListener(RegistersListener listener) {
		// stub to please compiler. Do nothing.

	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.IRegisters#getChildren(java.lang.String, org.eclipse.tm.tcf.services.IRegisters.DoneGetChildren)
	 */
	public IToken getChildren(String parentContextId, DoneGetChildren done) {
		// stub to please compiler. Do nothing.
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.IRegisters#getContext(java.lang.String, org.eclipse.tm.tcf.services.IRegisters.DoneGetContext)
	 */
	public IToken getContext(String id, DoneGetContext done) {
		// stub to please compiler. Do nothing.
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.IRegisters#getm(org.eclipse.tm.tcf.services.IRegisters.Location[], org.eclipse.tm.tcf.services.IRegisters.DoneGet)
	 */
	public IToken getm(Location[] locs, DoneGet done) {
		// stub to please compiler. Do nothing.
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.IRegisters#removeListener(org.eclipse.tm.tcf.services.IRegisters.RegistersListener)
	 */
	public void removeListener(RegistersListener listener) {
		// stub to please compiler. Do nothing.

	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.IRegisters#setm(org.eclipse.tm.tcf.services.IRegisters.Location[], byte[], org.eclipse.tm.tcf.services.IRegisters.DoneSet)
	 */
	public IToken setm(Location[] locs, byte[] value, DoneSet done) {
		// stub to please compiler. Do nothing.
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IService#getName()
	 */
	public String getName() {
		return IRegisters.NAME;
	}

}
