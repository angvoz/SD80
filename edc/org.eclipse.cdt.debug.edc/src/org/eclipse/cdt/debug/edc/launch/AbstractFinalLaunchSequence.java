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

	/**
	 * Whether this launcher will ignore TCF agents on other machines. Set at
	 * construction.
	 */
	final private boolean useLocalAgentOnly;
	
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
		public String getTaskName() {
			return "Find or launch TCF peer";
		}

		@Override
		public void execute(final RequestMonitor requestMonitor) {
			findPeer(requestMonitor);
			requestMonitor.done();
		}
	};

	protected Step initRunControlStep = new Step() {

		@Override
		public String getTaskName() {
			return "Init RunControl service";
		}

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
		public String getTaskName() {
			return "Init Registers service";
		}

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
		public String getTaskName() {
			return "Init Memory service";
		}

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
		public String getTaskName() {
			return "Init Breakpoints service";
		}

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
		public String getTaskName() {
			return "Init Processes service";
		}

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
		public String getTaskName() {
			return "Launch target";
		}

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
		public String getTaskName() {
			return "Attach";
		}

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

	/**
	 * Looks for a peer that matches the criteria set by the subclass, and puts
	 * it in {@link #tcfPeer} if successful. If more than one peer fits the
	 * bill, the subclass is given the chance to choose via
	 * {@link #selectPeer(IPeer[])}
	 * 
	 * @param requestMonitor
	 */
	protected void findPeer(RequestMonitor requestMonitor) {
		try {
			// We already found it. No-op
			if (getTCFPeer() != null) {
				return;
			}
			
			// See if subclass wants an explicit peer.
			if ((tcfPeer = selectExplicitPeer()) != null) {
				return;
			}
			
			// See if any already running (and discovered) peers fit the bill
			TCFServiceManager tcfServiceManager = (TCFServiceManager) EDCDebugger.getDefault().getServiceManager();
			IPeer[] runningPeers = tcfServiceManager.getRunningPeers(IRunControl.NAME, peerAttributes, useLocalAgentOnly);
			if (runningPeers.length > 0) {
				int index = selectPeer(runningPeers);
				if (index >= 0 && index < runningPeers.length) {
					tcfPeer = runningPeers[index];
					return;
				}
			}

			// Invoke any registered agent-launchers which could make the
			// desired peer available.
			List<IPeer> launchedPeers = new ArrayList<IPeer>();
			ITCFAgentLauncher[] agentLaunchers = tcfServiceManager.findSuitableAgentLaunchers(IRunControl.NAME, peerAttributes, useLocalAgentOnly);
			for (ITCFAgentLauncher agentLauncher : agentLaunchers) {
				IPeer peer = tcfServiceManager.launchAgent(agentLauncher, peerAttributes);	// this could take a little while...
				if (peer != null) {
					launchedPeers.add(peer);
				}
			}
			if (launchedPeers.size() > 0) {
				int index = selectPeer(launchedPeers.toArray(new IPeer[launchedPeers.size()]));
				if (index >= 0 && index < launchedPeers.size()) {
					tcfPeer = launchedPeers.get(index);
					return;
				}
			}
			
			requestMonitor.setStatus(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), "Could not find a suitable TCF peer", null));
		} catch (CoreException e) {
			requestMonitor.setStatus(e.getStatus());				
		}
	}

	/**
	 * Subclass may override this to select a peer that is not
	 * otherwise discovered by the Locator service.
	 * <p>
	 * If this returns non-<code>null</code>, this peer will be
	 * used instead of querying the locator service and using 
	 * {@link #selectPeer(IPeer[])}.  
	 * <p>
	 * By default, this returns <code>null</code>.
	 * @return an IPeer or <code>null</code>
	 * @throws CoreException if unable to select the desired explicit peer
	 * @since 2.0
	 */
	protected IPeer selectExplicitPeer() throws CoreException {
		return null;
	}

	/**
	 * Subclass should override this to select a peer from the array of peers
	 * which match the required minimum set of attributes specified by
	 * {@link #specifyRequiredPeer()}.  The default behavior is to use the first candidate.
	 * 
	 * <p>
	 * This methods represents a way for a specific launcher to do runtime peer
	 * selection. Choosing the right peer might require opening a channel to it
	 * to determine more detailed capabilities than what's available via its
	 * attributes. Or perhaps some additional decision making is required, not
	 * requiring a channel--e.g., more closely examining one of the peer
	 * attributes. Either way, {@link #specifyRequiredPeer()} provides the
	 * <i>minimum</i> set of attributes a peer should have to be considered for
	 * the launch. This method is used to provide further and final pruning of
	 * any candidates.
	 * 
	 * @param peers
	 *            the candidates
	 * @return the index of the peer to use; if the returned value is outside
	 *         the range of candidates, then none of the peers are used.
	 * @since 2.0
	 */
	public int selectPeer(IPeer[] peers) {
		assert peers.length > 0;
		return 0;
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
			if (service == null)
				throw EDCDebugger.newCoreException("Required service \"" + tcfServiceName + "\" is not available.");
			
			dsfService.tcfServiceReady(service);
		} catch (CoreException e1) {
			if (e1.getStatus().matches(IStatus.CANCEL))
				rm.cancel();
			rm.setStatus(e1.getStatus());
		}
	}

	/**
	 * @since 2.0
	 */
	public AbstractFinalLaunchSequence(DsfExecutor executor, EDCLaunch launch, IProgressMonitor pm, boolean useLocalAgentOnly) {
		this(executor, launch, pm, "Configuring Debugger", "Aborting configuring debugger", useLocalAgentOnly);
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
	 * @param useLocalAgentOnly
	 *            whether to ignore peers from remote hosts
	 * @since 2.0
	 */
	public AbstractFinalLaunchSequence(DsfExecutor executor, EDCLaunch launch, IProgressMonitor pm,
			String sequenceName, String abortName, boolean useLocalAgentOnly) {
		super(executor, pm, sequenceName, abortName);
		this.launch = launch;
		this.useLocalAgentOnly = useLocalAgentOnly;
		specifyRequiredPeer();
	}

	@Override
	public Step[] getSteps() {
		return steps.toArray(new Step[steps.size()]);
	}

	/**
	 * This method is invoked by the the step
	 * {@link AbstractFinalLaunchSequence#launchStep} to launch the thing to be
	 * debugged. The base implementation knows how to carry out the launch via a
	 * TCF IProcesses service. When dealing with an agent that doesn't provide
	 * that service (typically the case for embedded bareboard debugging), the
	 * subclass must override this method to inject its custom launch logic.
	 * 
	 * @param launch
	 *            the launch object
	 * @param ps
	 *            the TCF processes service; operation will fail if null
	 * @param requestMonitor
	 *            monitor to invoke when processing is done
	 */
	protected void launchProcess(final ILaunch launch, final IProcesses ps, final RequestMonitor requestMonitor) {
		try {
			ILaunchConfiguration cfg = launch.getLaunchConfiguration();

			// Get absolute program path.
			ICProject cproject = LaunchUtils.getCProject(cfg);
			// This works even if cproject is null.
			IPath program = LaunchUtils.verifyProgramPath(cfg, cproject); 
			final String file = program.toOSString();

			final String workingDirectory = LaunchUtils.getWorkingDirectoryPath(cfg);
			final String[] args = LaunchUtils.getProgramArgumentsArray(cfg);
			final Map<String, String> env = LaunchUtils.getEnvironmentVariables(cfg);
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
					ps.start(workingDirectory, file, args, vars, attach, new IProcesses.DoneStart() {

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
			int pid = launch.getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, -1);

			String preTargetedID = Integer.toString(pid);
			
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

			int selectedIndex = 0;
			if (pid == -1)
			{
				// 3) bring up dialog to choose which process
				ChooseProcessItem selected = chooseProcess(items, "");
				for (selectedIndex = 0; selectedIndex < numProcesses; selectedIndex++) {
					if (selected.processID.equals(items[selectedIndex].processID))
						break;
				}
			}
			else
			{
				for (int i = 0; i < contexts.length; i++) {
					String procID = (String) contexts[i].getProperties().get(ProtocolConstants.PROP_OS_ID);
					if (procID.equals(preTargetedID))
					{
						selectedIndex = i;
						break;
					}
				}

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
	 * all the services needed by a session. The peer is that from
	 * {@link #selectExplicitPeer()} or chosen from the Locator service 
	 * using attributes from {@link #specifyRequiredPeer()} and filtered
	 * by {@link #selectPeer(IPeer[])}.
	 * <p>
	 * 
	 * @return the peer. Will return null if called before the
	 *         {@link #initFindPeerStep} step has executed.
	 * @since 2.0 a subclass can no longer override this method if it wants to explicitly provide the
	 * peer: override {@link #selectExplicitPeer()} instead.
	 */
	protected final IPeer getTCFPeer() {
		return tcfPeer;
	}

	/**
	 * Returns whether this launcher will ignore TCF agents on other machines.
	 * 
	 * @since 2.0
	 */
	public boolean getUseLocalAgentOnly() {
		return useLocalAgentOnly;
	}
}
