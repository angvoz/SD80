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
package org.eclipse.cdt.debug.edc.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.ITCFAgentLauncher;
import org.eclipse.cdt.debug.edc.ITCFServiceManager;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.TCFServiceManager;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Breakpoints;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Memory;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Processes;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.services.IDSFServiceUsingTCF;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.debug.edc.tcf.extension.ProtocolConstants;
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
import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IProcesses.DoneCommand;
import org.eclipse.tm.tcf.services.IProcesses.DoneGetChildren;
import org.eclipse.tm.tcf.services.IProcesses.DoneGetContext;
import org.eclipse.tm.tcf.services.IProcesses.ProcessContext;
import org.eclipse.tm.tcf.services.IRegisters;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.util.TCFTask;

public abstract class AbstractFinalLaunchSequence extends Sequence {

	protected EDCLaunch launch;
	protected DsfServicesTracker tracker;
	protected List<Step> steps = new ArrayList<Step>();

	/**
	 * The single TCF peer associated with this session. Do not reference this
	 * field explicitly except to set it. Use {@link #getTCFPeer()}
	 */
	private IPeer tcfPeer;
	
	private boolean usingRemotePeers;
	private boolean isLocallyAllocatedPeer;

	/**
	 * Attributes that the debugger requires the TCF peer to match. Derivatives
	 * populate this when we call {@link #specifyRequiredPeer()}
	 */
	protected final Map<String, String> peerAttributes = new HashMap<String, String>();

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

	/**
	 * EDC currently only supports the scenario where a single TCF peer provides
	 * all the services needed by a debug session (ILaunch). It must provide the
	 * run-control service at a minimum (although other services are probably
	 * required, too). This step should be executed immediately after
	 * trackerStep.
	 */
	protected Step initFindPeerStep = new Step() {
		@Override
		public void execute(final RequestMonitor requestMonitor) {
			findPeer(requestMonitor);
			requestMonitor.done();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.concurrent.Sequence.Step#rollBack(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
		 */
		@Override
		public void rollBack(RequestMonitor rm) {
			if (isLocallyAllocatedPeer) {
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						((AbstractPeer) tcfPeer).dispose();
					}
				});
			}
			super.rollBack(rm);
		}
	};

	protected Step initRunControlStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {
			assert getTCFPeer() != null : "initFindPeerStep must be run prior to this one";
			RunControl runcontrol = tracker.getService(RunControl.class);
			findTCFServiceForDSFService(runcontrol, IRunControl.NAME, requestMonitor);
			requestMonitor.done();
		}
	};

	/*
	 * Initialize Registers service.
	 */
	protected Step initRegistersServiceStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {
			IPeer peer = getTCFPeer();
			assert peer != null : "initFindPeerStep must be run prior to this one";

			final Registers registers = tracker.getService(Registers.class);
			
			ITCFServiceManager tcfServiceManager = EDCDebugger.getDefault().getServiceManager();
			final IChannel channel = tcfServiceManager.getChannelForPeer(peer);

			Protocol.invokeLater(new Runnable() {
				public void run() {
					// First check if IRegisters service is provided.
					// If not, look for ISimpleRegisters service.
					//
					IService regSvc = null;
					try {
						regSvc = getTCFService(IRegisters.NAME);
					} catch (CoreException e) {
						// ignore, look for SimpleRegisters service. 
						// Report error when SimpleRegisters service is discarded...02/16/10
					}

					if (regSvc != null) { // registers service ready
						registers.tcfServiceReady(regSvc);
					}
					else {	// look for ISimpleRegisters service.
						try {
							regSvc = getTCFService(ISimpleRegisters.NAME);
						} catch (CoreException e) {
						}
						if (regSvc == null) {
							requestMonitor.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "Fail to find Registers service in agent."));
						}
						else {
							ISimpleRegisters simpleRegProxy = channel.getRemoteService(ISimpleRegisters.class);
							if (simpleRegProxy == null) {
								simpleRegProxy = new SimpleRegistersProxy(channel);
								channel.setServiceProxy(ISimpleRegisters.class, simpleRegProxy);
							}
							registers.tcfServiceReady(simpleRegProxy);
						}
					}
					
					requestMonitor.done();
				}
			});
		}
	};

	/*
	 * Initialize the memory service
	 */
	protected Step initMemoryServiceStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {
			assert getTCFPeer() != null : "initFindPeerStep must be run prior to this one";
			Memory memory = tracker.getService(Memory.class);
			findTCFServiceForDSFService(memory, IMemory.NAME, requestMonitor);
			requestMonitor.done();
		}
	};

	/**
	 * init breakpoints service.
	 */
	protected Step initBreakpointsServiceStep = new Step() {

		@Override
		public void execute(RequestMonitor requestMonitor) {
			assert getTCFPeer() != null : "initFindPeerStep must be run prior to this one";
			Breakpoints breakpoints = tracker.getService(Breakpoints.class);
			findTCFServiceForDSFService(breakpoints,
					org.eclipse.tm.tcf.services.IBreakpoints.NAME,
					requestMonitor);

			requestMonitor.done();
		}

	};

	/*
	 * Initialize the processes service
	 */
	protected Step initProcessesServiceStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {
			assert getTCFPeer() != null : "initFindPeerStep must be run prior to this one";
			Processes p = tracker.getService(Processes.class);
			findTCFServiceForDSFService(p, IProcesses.NAME, requestMonitor);
			requestMonitor.done();
		}
	};

	/*
	 * Launch the process
	 */
	protected Step launchStep = new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {
			assert getTCFPeer() != null : "initFindPeerStep must be run prior to this one";
			IService service;
			try {
				service = getTCFService(IProcesses.NAME);
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
			assert getTCFPeer() != null : "initFindPeerStep must be run prior to this one";
			IService service;
			try {
				service = getTCFService(IProcesses.NAME);
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

	/**
	 * Get a particular service from the TCF peer associated with this launch
	 * 
	 * @param tcfServiceName
	 *            the name of the service
	 * @return the service or null if not available
	 * @throws CoreException
	 */
	protected IService getTCFService(String tcfServiceName) throws CoreException {
		TCFServiceManager tcfServiceManager = (TCFServiceManager) EDCDebugger.getDefault().getServiceManager();
		return tcfServiceManager.getPeerService(getTCFPeer(), tcfServiceName);
	}

	protected void findPeer(RequestMonitor requestMonitor) {
		try {
			TCFServiceManager tcfServiceManager = (TCFServiceManager) EDCDebugger.getDefault().getServiceManager();
			IPeer[] runningPeers = tcfServiceManager.getRunningPeers(IRunControl.NAME, peerAttributes, !isUsingRemotePeers());
			
			if (isUsingRemotePeers())
			{
				tcfPeer = selectPeer(runningPeers);
			}
			else
			{
				if (runningPeers.length == 0)
				{
					ITCFAgentLauncher[] registered = tcfServiceManager.getRegisteredAgents(IRunControl.NAME, peerAttributes);
					if (registered.length > 0)
					{
						tcfPeer = tcfServiceManager.launchAgent(registered[0]);
					}
				}
				else
					tcfPeer = runningPeers[0];
			}
			
			if (tcfPeer == null) {
				requestMonitor.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "Could not find a suitable TCF peer", null));
			}
		} catch (CoreException e) {
			requestMonitor.setStatus(e.getStatus());				
		}
	}

	/**
	 * Override to select a peer from the array of running peers, or create your
	 * own on the fly.  Set the {@link #isLocallyAllocatedPeer} boolean if you
	 * create your own and want it to be disposed automatically after the debug session ends.  
	 * @param runningPeers
	 * @return an IPeer or <code>null</code>
	 */
	public IPeer selectPeer(IPeer[] runningPeers) {
		return null;
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
	 * @param tcfPeerAttrs
	 *            TCF peer attributes required to match.
	 * @param rm
	 */
	protected void findTCFServiceForDSFService(IDSFServiceUsingTCF dsfService, String tcfServiceName,
			RequestMonitor rm) {

		try {
			IService service = getTCFService(tcfServiceName);
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
		specifyRequiredPeer();
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
			// even if cproject is null.
			final String file = program.toOSString();

			final String workingDirectory = LaunchUtils.getWorkingDirectoryPath(cfg);
			final String args = cfg.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
			final Map<String, String> env = cfg.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<?,?>) null);
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
				String procID = (String) contexts[i].getProperties().get(ProtocolConstants.PROP_OS_ID);
				if (procID == null)
					procID = "unknown";
				ChooseProcessItem item = new ChooseProcessItem(procID, contexts[i].getName());
				items[i] = item;
			}

			// 3) bring up dialog to choose which process
			ChooseProcessItem selected = chooseProcess(items, "");
			int selectedIndex = 0;
			for (selectedIndex = 0; selectedIndex < numProcesses; selectedIndex++) {
				if (selected.processID.equals(items[selectedIndex].processID))
					break;
			}

			// 4) attach
			doAttachTask(contexts[selectedIndex]);

		} catch (CoreException e) {
			if (e.getStatus().matches(IStatus.CANCEL))
				requestMonitor.cancel();
			else {
				requestMonitor.setStatus(e.getStatus());
			}
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
	 * Specify attributes the debugger requires from the underlying TCF peer.
	 * Only peers with those attributes will be considered for this debug
	 * session. This method is called by the abstract class during construction
	 * of the object. The implementation should add the required attributes to
	 * {@link #peerAttributes}
	 */
	abstract protected void specifyRequiredPeer();

	/**
	 * Return the single TCF peer associated with this debug session (ILaunch).
	 * EDC currently only supports the scenario where a single TCF peer provides
	 * all the services needed by a session. The peer is chosen based on
	 * {@link #specifyRequiredPeer()}
	 * 
	 * <p>
	 * A subclass can override this method if it wants to explicitly provide the
	 * peer. In that case it has no need to invoke {@link #initFindPeerStep}
	 * 
	 * @return the peer. Will return null if called before the
	 *         {@link #initFindPeerStep} step has executed.
	 */
	protected IPeer getTCFPeer() {
		return tcfPeer;
	}

	public boolean isUsingRemotePeers() {
		return usingRemotePeers;
	}

	public void setUsingRemotePeers(boolean usingRemotePeers) {
		this.usingRemotePeers = usingRemotePeers;
	}

	/**
	 * Tell whether the peer in {@link #tcfPeer} was created during the launch
	 * and should be disposed after the debug session ends
	 * @return
	 */
	public boolean isLocallyAllocatedPeer() {
		return isLocallyAllocatedPeer;
	}

	/**
	 * Tell whether the peer in {@link #tcfPeer} was created during the launch
	 * and should be disposed after the debug session ends
	 * @param isLocallyAllocatedPeer
	 */
	public void setLocallyAllocatedPeer(boolean isLocallyAllocatedPeer) {
		this.isLocallyAllocatedPeer = isLocallyAllocatedPeer;
	}

}
