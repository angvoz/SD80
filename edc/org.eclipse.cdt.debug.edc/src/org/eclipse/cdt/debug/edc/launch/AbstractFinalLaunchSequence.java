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
package org.eclipse.cdt.debug.edc.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.TCFServiceManager;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Breakpoints;
import org.eclipse.cdt.debug.edc.internal.services.dsf.IDSFServiceUsingTCF;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Memory;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Registers;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ISimpleRegisters;
import org.eclipse.cdt.debug.edc.tcf.extension.services.SimpleRegistersProxy;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IProcesses.DoneCommand;
import org.eclipse.tm.tcf.services.IProcesses.DoneGetChildren;
import org.eclipse.tm.tcf.services.IProcesses.DoneGetContext;
import org.eclipse.tm.tcf.services.IProcesses.ProcessContext;
import org.eclipse.tm.tcf.util.TCFTask;

public abstract class AbstractFinalLaunchSequence extends Sequence {

	protected EDCLaunch launch;
	protected DsfServicesTracker tracker;
	protected List<Step> steps = new ArrayList<Step>();
	private IPeer tcfAgent = null;

	/**
	 * Attributes that the debugger requires the TCF agent to match.
	 */
	protected final Map<String, String> agentAttributes = new HashMap<String, String>();

	/**********************************************
	 * Common steps
	 **********************************************/

	// Common first step for the sequence
	//
	protected final Step trackerStep = new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			tracker = new DsfServicesTracker(EDCDebugger.getDefault().getBundle().getBundleContext(), launch
					.getSession().getId());
			requestMonitor.done();
		}

		@Override
		public void rollBack(RequestMonitor requestMonitor) {
			if (tracker != null)
				tracker.dispose();
			tracker = null;
			requestMonitor.done();
		}
	};

	// Common last step for the sequence
	//
	protected final Step cleanupStep = new Step() {
		@Override
		public void execute(final RequestMonitor requestMonitor) {
			tracker.dispose();
			tracker = null;
			requestMonitor.done();
		}
	};

	protected Step initRunControlStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {

			RunControl runcontrol = tracker.getService(RunControl.class);

			findTCFServiceForDSFService(runcontrol, IRunControl.NAME, agentAttributes, requestMonitor);

			requestMonitor.done();
		}
	};

	/*
	 * Initialize SimpleRegisters service.
	 */
	protected Step initRegistersServiceStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {

			final Registers registers = tracker.getService(Registers.class);
			try {
				IPeer agent = getTCFAgent(ISimpleRegisters.NAME, agentAttributes);

				TCFServiceManager tcfServiceManager = (TCFServiceManager) EDCDebugger.getDefault().getServiceManager();
				final IChannel channel = tcfServiceManager.getChannelForPeer(agent);
				launch.usingTCFChannel(channel);

				Protocol.invokeLater(new Runnable() {
					public void run() {
						ISimpleRegisters simpleRegProxy = channel.getRemoteService(ISimpleRegisters.class);
						if (simpleRegProxy == null) {
							simpleRegProxy = new SimpleRegistersProxy(channel);
							channel.setServiceProxy(ISimpleRegisters.class, simpleRegProxy);
						}
						registers.tcfServiceReady(simpleRegProxy);
					}
				});
			} catch (CoreException e) {
				requestMonitor.setStatus(e.getStatus());
			}

			requestMonitor.done();
		}
	};

	/*
	 * Initialize the memory service
	 */
	protected Step initMemoryServiceStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {

			Memory memory = tracker.getService(Memory.class);

			findTCFServiceForDSFService(memory, IMemory.NAME, agentAttributes, requestMonitor);

			requestMonitor.done();
		}
	};

	/**
	 * init breakpoints service.
	 */
	protected Step initBreakpointsServiceStep = new Step() {

		@Override
		public void execute(RequestMonitor requestMonitor) {
			Breakpoints breakpoints = tracker.getService(Breakpoints.class);

			findTCFServiceForDSFService(breakpoints, org.eclipse.tm.tcf.services.IBreakpoints.NAME, agentAttributes,
					requestMonitor);

			requestMonitor.done();
		}

	};

	/*
	 * Launch the process
	 */
	protected Step launchStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {

			IService service;
			try {
				service = getTCFService(IProcesses.NAME, agentAttributes);
			} catch (CoreException e1) {
				requestMonitor.setStatus(e1.getStatus());
				requestMonitor.done();
				return;
			}

			// Note that requestMonitor is passed down, so don't call
			// requestMonitor.done() here !
			launchProcess(launch, (IProcesses) service, requestMonitor);
		}
	};

	/*
	 * Attach to a process
	 */
	protected Step attachStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {

			IService service;
			try {
				service = getTCFService(IProcesses.NAME, agentAttributes);
			} catch (CoreException e1) {
				requestMonitor.setStatus(e1.getStatus());
				requestMonitor.done();
				return;
			}

			// Note that requestMonitor is passed down, so don't call
			// requestMonitor.done() here !
			attachProcess(launch, (IProcesses) service, requestMonitor);
		}
	};

	protected IPeer getTCFAgent(String tcfServiceName, Map<String, String> tcfAgentAttrs) throws CoreException {
		if (tcfAgent == null) {
			TCFServiceManager tcfServiceManager = (TCFServiceManager) EDCDebugger.getDefault().getServiceManager();
			tcfAgent = tcfServiceManager.findAgent(tcfServiceName, tcfAgentAttrs);
		} else {
			// should we check if the previously found agent meets our need ?
			// No. Currently and in foreseeable future we only need to support
			// the case that one agent provides all required debug services.
		}
		return tcfAgent;
	}

	protected IService getTCFService(String tcfServiceName, Map<String, String> tcfAgentAttrs) throws CoreException {
		IPeer agent = getTCFAgent(tcfServiceName, tcfAgentAttrs);
		TCFServiceManager tcfServiceManager = (TCFServiceManager) EDCDebugger.getDefault().getServiceManager();
		return tcfServiceManager.getAgentService(agent, tcfServiceName);
	}

	/**
	 * Find the given TCF service and link it to the given DSF service. The TCF
	 * service will be used to carry out the DSF one.
	 * 
	 * <p>
	 * Note caller must call the "rm.done".
	 * 
	 * @param dsfService
	 * @param tcfServiceName
	 *            TCF service required
	 * @param tcfAgentAttrs
	 *            TCF agent attributes required to match.
	 * @param rm
	 */
	protected void findTCFServiceForDSFService(IDSFServiceUsingTCF dsfService, String tcfServiceName,
			Map<String, String> tcfAgentAttrs, RequestMonitor rm) {

		try {
			IService service = getTCFService(tcfServiceName, tcfAgentAttrs);
			dsfService.tcfServiceReady(service);
		} catch (CoreException e1) {
			if (e1.getStatus().matches(IStatus.CANCEL))
				rm.cancel();
			rm.setStatus(e1.getStatus());
		}
	}

	public AbstractFinalLaunchSequence(DsfExecutor executor, EDCLaunch launch, IProgressMonitor pm) {
		this(executor, launch, pm, "Configuring Debugger", "Aborting configuring debugger");
	}

	/**
	 * 
	 * @param executor
	 * @param launch
	 * @param pm
	 * @param sequenceName
	 *            name to display in the progress monitor when the sequence is
	 *            running.
	 * @param abortName
	 *            name to display in the progress monitor when the sequence is
	 *            aborting due to error.
	 */
	public AbstractFinalLaunchSequence(DsfExecutor executor, EDCLaunch launch, IProgressMonitor pm,
			String sequenceName, String abortName) {
		super(executor, pm, sequenceName, abortName);

		specifyRequiredAgent();

		this.launch = launch;
	}

	@Override
	public Step[] getSteps() {
		return steps.toArray(new Step[steps.size()]);
	}

	@SuppressWarnings("unchecked")
	protected void launchProcess(final ILaunch launch, final IProcesses ps, final RequestMonitor requestMonitor) {
		try {
			ILaunchConfiguration cfg = launch.getLaunchConfiguration();

			// Get absolute program path.
			ICProject cproject = LaunchUtils.getCProject(cfg);
			IPath program = LaunchUtils.verifyProgramPath(cfg, cproject); // works
																			// even
																			// if
																			// cproject
																			// is
																			// null.
			final String file = program.toOSString();

			final String workingDirectory = cfg.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					"");
			final String args = cfg.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
			final Map<String, String> env = cfg.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
			final boolean append = cfg.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
			final boolean attach = false;

			final IProcesses.DoneGetEnvironment done_env = new IProcesses.DoneGetEnvironment() {
				public void doneGetEnvironment(IToken token, Exception error, Map<String, String> def) {
					if (error != null) {
						requestMonitor.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), error
								.getLocalizedMessage(), error));
						requestMonitor.done();
						return;
					}
					Map<String, String> vars = new HashMap<String, String>();
					if (append)
						vars.putAll(def);
					if (env != null)
						vars.putAll(env);
					ps.start(workingDirectory, file, toArgsArray(file, args), vars, attach, new IProcesses.DoneStart() {

						public void doneStart(IToken token, Exception error, ProcessContext process) {
							if (error != null) {
								requestMonitor.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(),
										error.getLocalizedMessage(), error));
								requestMonitor.done();
								return;
							}

							requestMonitor.done();
						}
					});
				}

				private String[] toArgsArray(String file, String cmd) {
					// Create arguments list from a command line.
					int i = 0;
					int l = cmd.length();
					List<String> arr = new ArrayList<String>();
					arr.add(file);
					for (;;) {
						while (i < l && cmd.charAt(i) == ' ')
							i++;
						if (i >= l)
							break;
						String s = null;
						if (cmd.charAt(i) == '"') {
							i++;
							StringBuffer bf = new StringBuffer();
							while (i < l) {
								char ch = cmd.charAt(i++);
								if (ch == '"')
									break;
								if (ch == '\\' && i < l)
									ch = cmd.charAt(i++);
								bf.append(ch);
							}
							s = bf.toString();
						} else {
							int i0 = i;
							while (i < l && cmd.charAt(i) != ' ')
								i++;
							s = cmd.substring(i0, i);
						}
						arr.add(s);
					}
					return arr.toArray(new String[arr.size()]);
				}
			};

			if (append) {
				Protocol.invokeLater(new Runnable() {
					public void run() {
						ps.getEnvironment(done_env);
					}
				});
			} else {
				done_env.doneGetEnvironment(null, null, null);
			}
		} catch (Exception x) {
			requestMonitor.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), x
					.getLocalizedMessage(), x));
			requestMonitor.done();
		}
	}

	public void attachProcess(EDCLaunch launch, IProcesses service, RequestMonitor requestMonitor) {

		try {
			// 1) get process list from agent (getChildren)
			String[] processes = getProcessList(service);
			int numProcesses = processes.length;
			// 2) get contexts for each ID
			ChooseProcessItem[] items = null;
			ProcessContext[] contexts = null;

			items = new ChooseProcessItem[numProcesses];
			contexts = new ProcessContext[numProcesses];

			for (int i = 0; i < numProcesses; i++) {
				contexts[i] = getProcessContext(processes[i], service);
				String procID = (String) contexts[i].getProperties().get(IRunControl.PROP_PROCESS_ID);
				if (procID == null)
					procID = "unknown";
				ChooseProcessItem item = new ChooseProcessItem(procID, contexts[i].getName());
				items[i] = item;
			}

			// 3) bring up dialog to choose which process
			ChooseProcessItem selected = chooseProcess(items, "");
			int selectedIndex = 0;
			for (selectedIndex = 0; selectedIndex < numProcesses; selectedIndex++) {
				if (selected.getProcessID().equals(items[selectedIndex].getProcessID()))
					break;
			}

			// 4) attach
			doAttachTask(contexts[selectedIndex]);

		} catch (CoreException e) {
			requestMonitor.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), e.getMessage()));
			requestMonitor.done();
			return;
		}

		requestMonitor.done();
	}

	/**
	 * Call TCF to carry out the attach.
	 * 
	 * @param context
	 * @throws CoreException
	 */
	protected void doAttachTask(final ProcessContext context) throws CoreException {
		final TCFTask<Object> task = new TCFTask<Object>() {
			public void run() {
				context.attach(new DoneCommand() {
					public void doneCommand(IToken token, Exception error) {
						if (error == null)
							done(this);
						else
							error(error);
					}
				});
			}
		};
		try {
			task.getE();
		} catch (Error e) {
			String msg = "failed to attach to process: " + context.getID() + ", caused by: "
					+ e.getCause().getMessage();
			if (!task.isCancelled()) {
				EDCDebugger.getMessageLogger().logError(msg, e);
				throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), msg, e));
			} else {
				throw new CoreException(Status.CANCEL_STATUS);
			}
		}
	}

	protected ProcessContext getProcessContext(final String processID, final IProcesses service) throws CoreException {
		final TCFTask<IProcesses.ProcessContext> task = new TCFTask<IProcesses.ProcessContext>() {

			public void run() {
				service.getContext(processID, new DoneGetContext() {

					public void doneGetContext(IToken token, Exception error, ProcessContext context) {
						if (error == null)
							done(context);
						else
							error(error);
					}
				});
			}

		};
		try {
			return task.getE();
		} catch (Error e) {
			String msg = "failed to create context for process: " + processID + ", caused by: "
					+ e.getCause().getMessage();
			if (!task.isCancelled()) {
				EDCDebugger.getMessageLogger().logError(msg, e);
				throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), msg, e));
			} else {
				throw new CoreException(Status.CANCEL_STATUS);
			}
		}
	}

	/**
	 * Get list of running processes from the target.
	 * 
	 * @param service
	 *            TCF processes service.
	 * @return array of IDs which are internal IDs created by debugger for the
	 *         processes, not process ID in the target OS.
	 * @throws CoreException
	 */
	protected String[] getProcessList(final IProcesses service) throws CoreException {
		TCFTask<String[]> task = new TCFTask<String[]>() {

			public void run() {
				service.getChildren(null, false, new DoneGetChildren() {

					public void doneGetChildren(IToken token, Exception error, String[] contextIds) {
						if (error == null)
							done(contextIds);
						else
							error(error);
					}
				});
			}
		};

		String[] ids;
		try {
			ids = task.getE();
		} catch (Error e) {
			String msg = "failed to get process list from target,  caused by: " + e.getCause().getMessage();
			if (!task.isCancelled()) {
				EDCDebugger.getMessageLogger().logError(msg, e);
				throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), msg, e));
			} else {
				throw new CoreException(Status.CANCEL_STATUS);
			}
		}

		if (ids.length == 0) {
			String msg = "Failed to get running processes from target";
			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), msg, null));
		}
		return ids;
	}

	/**
	 * TODO: This requires UI dialog. We need to either 1. make this abstract so
	 * that each EDC-based debugger can do its own implementation (pending
	 * unification of attach code for TRK & Windows), or 2. provide a common
	 * dialog as IStatusHandler extension.
	 * 
	 * @param processes
	 * @param defaultSelection
	 * @return
	 * @throws CoreException
	 */
	protected ChooseProcessItem chooseProcess(final ChooseProcessItem[] processes, String defaultSelection)
			throws CoreException {
		// temp stub.
		return null;
	}

	/**
	 * Specify the attributes the debuggers requires from the underlying TCF
	 * agent. Only agents with those attributes will be chosen for the debugger.
	 */
	abstract protected void specifyRequiredAgent();
}
