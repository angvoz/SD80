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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.debug.edc.agent.gdbserver.CommandLineArguments;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocolX86;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbserverAgent;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.Packet;
import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.ResponseHandler;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentException;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentUtils;
import org.eclipse.cdt.debug.edc.tcf.extension.ProtocolConstants.IModuleProperty;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.ContextManager;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.ProcessInAgent;
import org.eclipse.cdt.debug.edc.tcf.extension.transport.ITransportChannel;
import org.eclipse.cdt.debug.edc.tcf.extension.transport.TransportChannelTCP;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRunControl;

/**
 * @author LWang
 * 
 */
public class ProcessesService implements IProcesses {

	private final IChannel fChannel;
	private Process fGdbserverProcess = null;

	// These record parameters for launch-process debug.
	private String file;

	private String directory, commandLine[];
	private Map<String, String> environment;

	// These record parameters for attach debug.
	private int processIDToAttach;

	/**
	 * Current (also the only one before multi-process debug is supported)
	 * process under debug. The ID is internal ID, not ID in OS.
	 */
	static private String currentProcessID = null;

	private class CommandServer implements IChannel.ICommandServer {

		public void command(IToken token, String name, byte[] data) {
			try {
				command(token, name, JSON.parseSequence(data));
			} catch (Throwable x) {
				fChannel.terminate(x);
			}
		}

		private void command(IToken token, String name, Object[] args) throws IOException {
			System.out.println("ProcessesService Command:" + name);

			if (name.equals("start")) {
				if (args.length != 5) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}
				directory = (String) args[0];
				file = (String) args[1];
				commandLine = AgentUtils.toStringArray(args[2]);
				environment = AgentUtils.toEnvMap(args[3]);
				boolean attach = ((Boolean) args[4]).booleanValue();

				if (attach) {
					processIDToAttach = Integer.valueOf(directory);
				}
				startDebug(token, attach);

			} else if (name.equals("attach")) {
				if (args.length != 1) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}
				String contextID = (String) args[0];

				// With Linux, we currently get process ID from host
				// (as process lister is implemented on host).
				processIDToAttach = Integer.valueOf(contextID);

				startDebug(token, true);

			} else if (name.equals("signal")) {
				if (args.length != 2) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}
				String contextID = (String) args[0];
				Integer s = (Integer) args[1];

				if (s.intValue() == 9)
					terminate(contextID);

				fChannel.sendResult(token, JSON.toJSONSequence(new Object[] { null // error
						}));
			} else if (name.equals("getEnvironment")) {
				if (args.length != 0) {
					fChannel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
					return;
				}

				// TODO: add correct envs here.
				List<String> envs = new ArrayList<String>();
				envs.add("TEST=0");
				envs.add("TEST2");

				fChannel.sendResult(token, JSON.toJSONSequence(new Object[] { null, envs }));
			} else {
				fChannel.rejectCommand(token);
			}
		}

	}

	public ProcessesService(IChannel channel) {
		this.fChannel = channel;
		channel.addCommandServer(this, new CommandServer());
	}

	static public String getCurrentProcess() {
		return currentProcessID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IProcesses#addListener(org.eclipse.tm.tcf
	 * .services.IProcesses.ProcessesListener)
	 */
	public void addListener(ProcessesListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.services.IProcesses#getChildren(java.lang.String,
	 * boolean, org.eclipse.tm.tcf.services.IProcesses.DoneGetChildren)
	 */
	public IToken getChildren(String parentContextId, boolean attachedOnly, DoneGetChildren done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.services.IProcesses#getContext(java.lang.String,
	 * org.eclipse.tm.tcf.services.IProcesses.DoneGetContext)
	 */
	public IToken getContext(String id, DoneGetContext done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IProcesses#getEnvironment(org.eclipse.tm.
	 * tcf.services.IProcesses.DoneGetEnvironment)
	 */
	public IToken getEnvironment(DoneGetEnvironment done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IProcesses#getSignalList(java.lang.String,
	 * org.eclipse.tm.tcf.services.IProcesses.DoneGetSignalList)
	 */
	public IToken getSignalList(String contextId, DoneGetSignalList done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IProcesses#getSignalMask(java.lang.String,
	 * org.eclipse.tm.tcf.services.IProcesses.DoneGetSignalMask)
	 */
	public IToken getSignalMask(String contextId, DoneGetSignalMask done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IProcesses#removeListener(org.eclipse.tm.
	 * tcf.services.IProcesses.ProcessesListener)
	 */
	public void removeListener(ProcessesListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IProcesses#setSignalMask(java.lang.String,
	 * int, int, org.eclipse.tm.tcf.services.IProcesses.DoneCommand)
	 */
	public IToken setSignalMask(String contextId, int dontStop, int dontPass, DoneCommand done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.services.IProcesses#signal(java.lang.String,
	 * long, org.eclipse.tm.tcf.services.IProcesses.DoneCommand)
	 */
	public IToken signal(String contextId, long signal, DoneCommand done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.services.IProcesses#start(java.lang.String,
	 * java.lang.String, java.lang.String[], java.util.Map, boolean,
	 * org.eclipse.tm.tcf.services.IProcesses.DoneStart)
	 */
	public IToken start(String directory, String file, String[] commandLine, Map<String, String> environment,
			boolean attach, DoneStart done) {
		// TODO Auto-generated method stub
		return null;
	}

	private void startDebug(IToken token, boolean attach) throws IOException {

		initializeDebugSession();

		Exception err = null;
		int pid = 0;
		try {
			pid = launchGDBServer(attach);
		} catch (AgentException e) {
			err = e;
		}

		/*
		 * internal temp test
		 */
		try {
			TmpTest();
		} catch (AgentException e) {
			// ignore.
		}

		Map<String, Object> props = new HashMap<String, Object>();

		ProcessInAgent proc = new ProcessInAgent(pid, props, true);

		currentProcessID = proc.getID();

		List<Object> reply = new ArrayList<Object>();
		reply.add(err != null ? AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER, err.getMessage()) : null);
		// Don't do this only if we do "ProcessContext.attach" instead of
		// processesService.start(..., attach) ... 10/21/09
		// if (! attach)
		reply.add(proc.getProperties());

		fChannel.sendResult(token, JSON.toJSONSequence(reply.toArray()));

		if (err != null)
			return;

		// Now send a "contextAdded" event to debugger.
		//
		RunControlService rcService = (RunControlService) fChannel.getLocalService(IRunControl.NAME);
		if (err == null && rcService != null) {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			list.add(proc.getProperties());
			rcService.sendEvent("contextAdded", JSON.toJSONSequence(new Object[] { list }));

			// Hmm, we need this delay before sending "contextException"
			// event, otherwise the "process" node sometimes fail to appear on
			// the Debug
			// View.
			// TODO: Don't know why yet. Some timing issue in host
			// debugger ? DSF event handling ?
			// or something not happen in TCF event dispatcher ?
			// ..............06/03/09
			//
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			rcService.sendEvent("contextException", JSON
					.toJSONSequence(new Object[] { currentProcessID, "Just loaded" }));

			// Send module-load event for the main executable
			//
			props.clear();
			props.put("Name", file);
			props.put(IModuleProperty.PROP_FILE, file);
			/*
			 * For the main executable (not shared library), no relocation is
			 * done on Linux to run it. So we don't need to report any runtime
			 * addresses.
			 */
			// props.put(IModuleProperty.PROP_CODE_ADDRESS, 0x8048000);
			// props.put(IModuleProperty.PROP_CODE_SIZE, 0x2000000);

			rcService.sendEvent("contextSuspended", JSON.toJSONSequence(new Object[] { proc.getID(), null /*
																										 * pc,
																										 * ignore
																										 */,
					IRunControl.REASON_SHAREDLIB, /* use this reason */
					props }));
		}
	}

	private void initializeDebugSession() {
		ContextManager.clearContextCache();
	}

	private int launchGDBServer(boolean attach) throws AgentException {

		int port = 0;
		try {
			// Find a free TCP port first.
			port = AgentUtils.findFreePort();
		} catch (IOException e) {
			throw new AgentException("Failed to find a free TCP port for gdbserver.", e);
		}

		// start gdbserver
		List<String> cmdLine = new ArrayList<String>();
		cmdLine.add("gdbserver");
		if (attach) {
			cmdLine.add("--attach");
			cmdLine.add(":" + port);
			cmdLine.add("" + processIDToAttach);
		}
		else {
			cmdLine.add(":" + port);
			cmdLine.add(file);
			// entry 0 is the executable
			for (int i = 1; i < commandLine.length; i++) {
				cmdLine.add(commandLine[i]);
			}
			if (environment != null && !environment.isEmpty()) {
				cmdLine = CommandLineArguments.createFromCommandLine(
						CommandLineArguments.wrapStandardUnixShellCommandLine("/bin/sh", 
								cmdLine, environment, 
								(directory != null && directory.length() > 0 ? new File(directory) : null)));
			}
		}

		try {
			fGdbserverProcess = Runtime.getRuntime().exec((String[]) cmdLine.toArray(new String[cmdLine.size()]));
		} catch (IOException e1) {
			StringBuilder fullCmdLine = new StringBuilder();
			for (String arg : commandLine) {
				fullCmdLine.append(arg);
				fullCmdLine.append(' ');
			}
			throw new AgentException("Failed to launch gdbserver with command: " + fullCmdLine, e1);
		}

		// Wait some time for the process to start.
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
		}

		try {
			int code = fGdbserverProcess.exitValue();
			throw new AgentException("gdbserver is just stated but exited with code " + code);
		} catch (IllegalThreadStateException e) {
			// the process still running, good.
		}

		/*
		 * Try to get process ID from gdbserver output, which is like this:
		 * Process ./console_div0 created; pid = 10932 Listening on port 1234
		 * Note the output is to stderr.
		 */
		int pid;
		if (!attach) {
			BufferedReader in = new BufferedReader(new InputStreamReader(fGdbserverProcess.getErrorStream()));
			String line;
			try {
				line = in.readLine();
			} catch (IOException e1) {
				// should not happen
				throw new AgentException("Fail to read output of gdbserver.");
			}

			pid = getProcessID(line);
			if (pid < 0)
				throw new AgentException("Fail to get process ID from gdbserver output: " + line);
		} else
			pid = processIDToAttach;

		// Now set up communication with the gdbserver instance.
		//
		ITransportChannel tcpChannel = new TransportChannelTCP("localhost", port);
		try {
			tcpChannel.open();
		} catch (IOException e) {
			fGdbserverProcess.destroy();

			throw new AgentException("Failed to open communication channel to gdbserver.", e);
		}

		GdbserverAgent.getInstance().setGdbRemoteProtocol(new GdbRemoteProtocolX86(tcpChannel));

		return pid;
	}

	private void TmpTest() throws AgentException {
		ResponseHandler dumbRH = new ResponseHandler(fChannel, null) {
			@Override
			public void handle(Packet response) {
				// Currently we just log the output for testing purpose. Do
				// something meaningful
				// when needed.... 06/02/09
			}
		};

		GdbRemoteProtocol prot = GdbserverAgent.getInstance().getGdbRemoteProtocol();

		// Query features of gdbserver.
		//
		prot.queryStubFeatures(dumbRH);

		// Check libraries loaded.
		//
		prot.getLibraryList(dumbRH);

		// Check thread list.
		//
		prot.getThreads(dumbRH);

		prot.getMoreThreads(dumbRH);

		// This works but we need to decode the reply
		// and concatenate Ooutput replies ...10/21/09
		// prot.invokeRemoteCommand("ls", dumbRH);
	}

	/**
	 * Get process id from output string of gdbserver.
	 * 
	 * @param str
	 * @return
	 */
	private int getProcessID(String str) {
		int pid = -1;
		Pattern pattern = Pattern.compile(".*?pid = (\\d+)"); //$NON-NLS-1$

		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			String processID = matcher.group(1);
			pid = Integer.valueOf(processID);
		}

		return pid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.protocol.IService#getName()
	 */
	public String getName() {
		return NAME;
	}

	public void terminate(String contextID) {
		// TODO: terminate the "context" properly.
		//
		if (fGdbserverProcess != null) {
			fGdbserverProcess.destroy();
			fGdbserverProcess = null;
		}
	}
}
