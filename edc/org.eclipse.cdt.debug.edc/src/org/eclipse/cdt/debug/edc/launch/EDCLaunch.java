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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.sourcelookup.AbsolutePathSourceContainer;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.ExecutablesSourceContainer;
import org.eclipse.cdt.debug.edc.internal.PathUtils;
import org.eclipse.cdt.debug.edc.internal.launch.ShutdownSequence;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Processes;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExitedEvent;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.RootExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.model.DsfLaunch;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlockRetrieval;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.debug.sourcelookup.DsfSourceLookupDirector;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * The only object in the model that implements the traditional interfaces.
 */
@ThreadSafe
abstract public class EDCLaunch extends DsfLaunch {
	private DefaultDsfExecutor executor;
	private final DsfSession session;
	private DsfServicesTracker tracker;
	private boolean initialized;
	private boolean shutDown;
	private boolean isLaunching;
	private boolean isTerminating;

	private DsfMemoryBlockRetrieval memRetrieval;
	private IDsfDebugServicesFactory serviceFactory;
	private final String debugModelID;
	private String description;
	private Album album;
	private boolean snapshotSupportInitialized;
	private boolean isFirstLaunch = true;
	private ILaunchConfiguration activeLaunchConfiguration;
	private List<ILaunchConfiguration> affiliatedLaunchConfigurations = Collections.synchronizedList(new ArrayList<ILaunchConfiguration>());
	private boolean isTerminatedThanDisconnected = false;
	private boolean shuttingDown;

	private static final Map<EDCLaunch, List<IChannel>> launchChannels = Collections
			.synchronizedMap(new HashMap<EDCLaunch, List<IChannel>>());

	private static final Map<String, EDCLaunch> launchSessions = Collections
			.synchronizedMap(new HashMap<String, EDCLaunch>());

	/**
	 * Every EDC (DSF) session has a thread pool in which to delegate blocking
	 * code to. See AbstractEDCService.asyncExec()
	 */
	private static final Map<String, ExecutorService> threadPools = Collections
		.synchronizedMap(new HashMap<String, ExecutorService>());

	public EDCLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator, String ownerID) {
		super(launchConfiguration, mode, locator);

		debugModelID = ownerID;

		// Create the dispatch queue to be used by debugger control and services
		// that belong to this launch
		final DefaultDsfExecutor dsfExecutor = new DefaultDsfExecutor("DSF executor - " + ownerID); //$NON-NLS-1$
		dsfExecutor.prestartCoreThread();
		executor = dsfExecutor;
		session = DsfSession.startSession(executor, ownerID);
		launchSessions.put(session.getId(), this);
		
		threadPools.put(session.getId(), newThreadPool());
	}

	/**
	 * Obtains a new thread pool
	 */
	private ThreadPoolExecutor newThreadPool() {
		// Thread pools can be a tricky thing. The behaviors available for
		// ThreadPoolExecutor are particularly so. Grab a handful of
		// aspirin and read the class javadoc if you're up for it. Basically,
		// what we're going to do here is create a thread pool that hopes to
		// meet demand with three threads. It will use a queue to allow itself
		// to be backlogged by a maximum of 10,000 requests. If the requests
		// keep pouring in and the backlog limit is blown, the pool will
		// dispatch up to three additional threads (for a total of six) to try
		// to handle the load. If it still can't keep up after that, then it
		// throws up its hands and starts rejecting requests. We need to protect
		// ourselves from exhausting cpu/system resources (a million threads) or
		// memory (millions of backlogged requests). A coding or runtime mishap
		// could lead to either if we don't set limits on the thread pool. But
		// what are reasonable limits? The best we can do is set some values we
		// think will work, use checks to make us or the user aware when they
		// don't, and give the user a backdoor mechanism to tweak the values
		// until we can provide better default values.
		//
		// Also, keep in mind that the thread pool will try to trim down the
		// number of threads if and when it has had to resort to creating
		// additional ones to handle a heavy load. If a thread is idle for more
		// than ten seconds, it will be shut down.

		int coreThreadCount = getBackdoorValue("org.eclipse.cdt.edc.poolthread.coreThreadCount", 3);
		int maxThreadCount = getBackdoorValue("org.eclipse.cdt.edc.poolthread.maxThreadCount", coreThreadCount + 3);
		long idleLimit = getBackdoorValue("org.eclipse.cdt.edc.poolthread.idleLimit", 10);
		int queueLimit = getBackdoorValue("org.eclipse.cdt.edc.poolthread.queueLimit", 10000);
		
	    return new ThreadPoolExecutor(coreThreadCount, maxThreadCount,
	            idleLimit, TimeUnit.SECONDS,
	            new ArrayBlockingQueue<Runnable>(queueLimit));
	}

	/**
	 * Provide a backdoor mechanism for tweaking the thread pool parameters on
	 * the field. Hopefully this will never be needed, but better safe than
	 * sorry.
	 */
	private static int getBackdoorValue(String prop, int defaultVal) {
		String value = System.getProperty(prop);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			}
			catch (NumberFormatException exc){}
		}
		return defaultVal;
	}
	

	public static EDCLaunch getLaunchForSession(String sessionID) {
		return launchSessions.get(sessionID);
	}

	/**
	 * See {@link #threadPools}
	 * @since 2.0
	 */
	public static ExecutorService getThreadPool(String sessionID) {
		return threadPools.get(sessionID);
	}

	public DsfExecutor getDsfExecutor() {
		return executor;
	}

	public IDsfDebugServicesFactory getServiceFactory() {
		return serviceFactory;
	}

	public void initialize() {
		Runnable initRunnable = new DsfRunnable() {
			public void run() {
				tracker = new DsfServicesTracker(EDCDebugger.getDefault().getBundle().getBundleContext(), session
						.getId());
				session.addServiceEventListener(EDCLaunch.this, null);
				memRetrieval = null;
				initialized = true;
				fireChanged();
			}
		};

		// Invoke the execution code and block waiting for the result.
		try {
			executor.submit(initRunnable).get();
		} catch (Exception e) {
			EDCDebugger.getMessageLogger().log(
					new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
							"Error initializing launch", e)); //$NON-NLS-1$
		}
	}

	public void initializeMemoryRetrieval() throws CoreException {
		// Create a memory retrieval and register it with the session
		try {
			executor.submit(new Callable<Object>() {
				public Object call() throws CoreException {
					memRetrieval = new DsfMemoryBlockRetrieval(getDebugModelID(), getLaunchConfiguration(), session);
					session.registerModelAdapter(IMemoryBlockRetrieval.class, memRetrieval);
					return null;
				}
			}).get();
		} catch (InterruptedException e) {
			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, 0,
					"Interrupted while waiting for get process callable.", e)); //$NON-NLS-1$
		} catch (ExecutionException e) {
			throw (CoreException) e.getCause();
		} catch (RejectedExecutionException e) {
			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, 0,
					"Debugger shut down before launch was completed.", e)); //$NON-NLS-1$
		}
	}

	public DsfSession getSession() {
		return session;
	}

	public DsfMemoryBlockRetrieval getMemoryBlockRetrieval() {
		return memRetrieval;
	}

	public void setServiceFactory(IDsfDebugServicesFactory factory) {
		serviceFactory = factory;
	}

	// Event handler when a thread or a threadGroup exits
	@DsfServiceEventHandler
	public void eventDispatched(IExitedDMEvent e) {
		// Only shutdown the session if the RootDMC is exited, namely all processDMCs 
		// are terminated or disconnected.
		if (! (e instanceof ExitedEvent))
			return;
		
		// Synchronize here in case a launch delegate is trying to do more
		// launching at the same time.
		synchronized (this)
		{
			if (e.getDMContext() instanceof RootExecutionDMC && !isLaunching()) {
				// Don't terminate the launch if a delegate has already started
				// using it for new activity.
				// The ExitedEvent tells us whether the last context in the launch
				// is terminated or disconnected.
				setTerminating(true);
				isTerminatedThanDisconnected = ((ExitedEvent)e).isTerminatedThanDisconnected();
				shutdownSession(new RequestMonitor(ImmediateExecutor.getInstance(), null));
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// IServiceEventListener
	@DsfServiceEventHandler
	public void eventDispatched(ICommandControlShutdownDMEvent event) {
		shutdownSession(new RequestMonitor(ImmediateExecutor.getInstance(), null));
	}

	// /////////////////////////////////////////////////////////////////////////
	// ITerminate
	@Override
	public boolean canTerminate() {
		return initialized && !shutDown;
	}

	@Override
	public boolean isTerminated() {
		// This return value is irrelevant to whether the session
		// is terminated or disconnected.
		return shutDown;
	}

	@Override
	public void terminate() throws DebugException {
		// Step one, tell the run control service to terminate everything
		DsfExecutor dsfExecutor = getDsfExecutor();
		if (dsfExecutor != null) {
			setTerminating(true);
			getDsfExecutor().execute(new Runnable() {
				public void run() {
					RunControl runControlService = tracker
							.getService(RunControl.class);
					if (runControlService != null)
						runControlService.terminateAllContexts(null);
				}
			});
		}
	}

	// ITerminate
	// /////////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////////
	// IDisconnect
	@Override
	public boolean canDisconnect() {
		return !(snapshotSupportInitialized && isSnapshotLaunch()) && canTerminate();
	}

	@Override
	public boolean isDisconnected() {
		// Indicates whether the launch (session) is terminated 
		// by "disconnect" command.
		return isTerminated() && ! isTerminatedThanDisconnected;
	}

	@Override
	public void disconnect() throws DebugException {
		DsfExecutor dsfExecutor = getDsfExecutor();
		if (dsfExecutor != null) {
	        getDsfExecutor().execute(new Runnable() {
	            public void run() {
	                Processes procService = tracker.getService(Processes.class);
	                if (procService != null)
	                	procService.detachDebuggerFromSession(null);
	            }
	        });
		}
	}

	// IDisconnect
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Shuts down the services, the session and the executor associated with
	 * this launch.
	 * <p>
	 * Note: The argument request monitor to this method should NOT use the
	 * executor that belongs to this launch. By the time the shutdown is
	 * complete, this executor will not be dispatching anymore and the request
	 * monitor will never be invoked. Instead callers should use the
	 * {@link ImmediateExecutor}.
	 * </p>
	 * 
	 * @param rm
	 *            The request monitor invoked when the shutdown is complete.
	 */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void shutdownSession(final RequestMonitor rm) {		
		if (shutDown || shuttingDown || executor == null) {
			rm.done();
			return;
		}

		shuttingDown = true;

		// Shut down the thread pool, otherwise its core threads might hang
		// around indefinitely.
		ExecutorService pool = threadPools.get(session.getId());
		if (pool != null) {
			pool.shutdown();
			try {
				// We need to wait for the threads actively handling a task
				// to complete. If we proceed with the session shutdown before
				// that, the active threads are likely to encounter problem as
				// they try to operate within a defunct session. Don't wait
				// indefinitely, though. Note that this does not block the 
				// UI from showing the session as terminated. For the most part,
				// things on the surface should look like the debug session
				// ended. Obviously, cleanup will be pending.
				pool.awaitTermination(15, TimeUnit.SECONDS);
			} catch (InterruptedException exc) {
				EDCDebugger.getMessageLogger().logException(exc);
			}
		}

		Sequence shutdownSeq = new ShutdownSequence(getDsfExecutor(), session.getId(), new RequestMonitor(session
				.getExecutor(), rm) {
			@Override
			public void handleCompleted() {
				session.removeServiceEventListener(EDCLaunch.this);
				if (!isSuccess()) {
					EDCDebugger.getMessageLogger().log(
							new MultiStatus(EDCDebugger.PLUGIN_ID, -1, new IStatus[] { getStatus() },
									"Session shutdown failed", null)); //$NON-NLS-1$
				}
				// Last order of business, shutdown the dispatch queue.
				if (tracker != null)
					tracker.dispose();
				tracker = null;
				DsfSession.endSession(session);

				// DsfMemoryBlockRetrieval.saveMemoryBlocks();
				if (memRetrieval != null) {
					memRetrieval.saveMemoryBlocks();
				}

				// endSession takes a full dispatch to distribute the
				// session-ended event, finish step only after the dispatch.
				executor.shutdown();
				executor = null;
				fireTerminate();

				try {
					closeUnusedChannels();
				} catch (Throwable e) {
					EDCDebugger.getMessageLogger().logError(null, e);
				}

				shutDown = true;

				rm.setStatus(getStatus());
				rm.done();
			}
		});
		executor.execute(shutdownSeq);

		try {
			ILaunchConfiguration activeLaunchConfig = getLaunchConfiguration();

			if (activeLaunchConfig.getAttribute(IEDCLaunchConfigurationConstants.ATTR_IS_ONE_USE, false))
				activeLaunchConfig.delete();

			if (isSnapshotLaunch()) {
				// delete launch configuration
				ILaunchConfiguration lc = SnapshotUtils.findExistingLaunchForAlbum(album);
				if (lc != null){
					lc.delete();
				}
			}
		} catch (Throwable e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
	}

	protected void closeUnusedChannels() {
		synchronized (launchChannels) {
			List<IChannel> channelList = launchChannels.get(this);
			launchChannels.remove(this);
			if (channelList == null)
				return;

			Collection<List<IChannel>> remainingChannels = launchChannels.values();
			for (List<IChannel> list : remainingChannels) {
				channelList.removeAll(list);
			}
			for (final IChannel channel : channelList) {
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						channel.close();
					}
				});
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		// Must force adapters to be loaded.
		Platform.getAdapterManager().loadAdapter(this, adapter.getName());
		return super.getAdapter(adapter);
	}

	public String getDebugModelID() {
		return debugModelID;
	}

	public void usingTCFChannel(IChannel channel) {
		synchronized (launchChannels) {
			List<IChannel> channelList = launchChannels.get(this);
			if (channelList == null) {
				channelList = new ArrayList<IChannel>();
			}
			if (!channelList.contains(channel))
				channelList.add(channel);
			launchChannels.put(this, channelList);
		}
	}

	public IAlbum getAlbum() {
		return album;
	}

	public void setAlbum(IAlbum album) {
		this.album = (Album) album;
	}

	public boolean isSnapshotLaunch() {
		// this is not set in run mode
		if (ILaunchManager.DEBUG_MODE.equals(getLaunchMode()))
			assert snapshotSupportInitialized;
		return album != null;
	}

	public ISourceLocator getExecutableLocator() {
		CSourceLookupDirector director = new CSourceLookupDirector();
		director.initializeParticipants();
		try {
			if (isSnapshotLaunch()) {
				getAlbum().configureSourceLookupDirector(director);
			} else {
				String exePath = getLaunchConfiguration().getAttribute(
						ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "");
				director.setSourceContainers(createExecutableLocatorSourceContainers(exePath));
			}
		} catch (CoreException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
		return director;
	}

	/**
	 * @since 2.0
	 */
	protected ISourceContainer[] createExecutableLocatorSourceContainers(String exePath) {
		List<ISourceContainer> containers = new ArrayList<ISourceContainer>();
		containers.add(new AbsolutePathSourceContainer());
		if (exePath.length() > 0)
		{
			IPath exeDirectory = new Path(exePath).removeLastSegments(1);
			containers.add(new DirectorySourceContainer(exeDirectory, false));
		}
		containers.add(new ExecutablesSourceContainer());
		return containers.toArray(new ISourceContainer[containers.size()]);
	}

	public void initializeSnapshotSupport() {
		try {
			String albumFile = getLaunchConfiguration().getAttribute(IEDCLaunchConfigurationConstants.ATTR_ALBUM_FILE,
					"");
			IPath albumPath = PathUtils.createPath(albumFile);
			if (albumPath.toFile().exists()) {
				album = Album.getAlbumByLocation(albumPath);
				if (album == null) {
					album = new Album();
					album.setLocation(albumPath);
					album.loadAlbum(false);
				}
				album.setSessionID(session.getId());

				IAlbum album = Album.getAlbumBySession(session.getId());
				DsfSourceLookupDirector director = (DsfSourceLookupDirector) getSourceLocator();
				album.configureSourceLookupDirector(director);
			} else {

			}
		} catch (Exception e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
		
		snapshotSupportInitialized = true;
	}

	/**
	 * @since 2.0
	 */
	public ILaunchConfiguration[] getAffiliatedLaunchConfigurations() {
		return affiliatedLaunchConfigurations.toArray(new ILaunchConfiguration[affiliatedLaunchConfigurations.size()]);
	}

	/**
	 * @since 2.0
	 */
	public void addAffiliatedLaunchConfiguration(
			ILaunchConfiguration configuration) {
		affiliatedLaunchConfigurations.add(configuration);
	}

	/**
	 * @since 2.0
	 */
	public void setActiveLaunchConfiguration(ILaunchConfiguration activeLaunchConfiguration) {
		this.activeLaunchConfiguration = activeLaunchConfiguration;
	}

	/**
	 * @since 2.0
	 */
	public void setFirstLaunch(boolean isFirstLaunch) {
		this.isFirstLaunch = isFirstLaunch;
	}

	/**
	 * @since 2.0
	 */
	public boolean isFirstLaunch() {
		return isFirstLaunch;
	}

	/**
	 * @since 2.0
	 */
	public ISourceLocator createSourceLocator()
			throws CoreException {
		DsfSourceLookupDirector locator = new DsfSourceLookupDirector(session);
		String memento = getLaunchConfiguration().getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String) null);
		if (memento == null) {
			locator.initializeDefaults(getLaunchConfiguration());
		} else {
			locator.initializeFromMemento(memento, getLaunchConfiguration());
		}
		return locator;
	}

	/**
	 * @since 2.0
	 */
	public static EDCLaunch[] findLaunchesUsingPeer(final IPeer ipeer) {
		final List<EDCLaunch> results = new ArrayList<EDCLaunch>();
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		final List<ILaunch> launchList = Arrays.asList(manager.getLaunches());

		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				for (ILaunch iLaunch : launchList) {
					if (iLaunch instanceof EDCLaunch) {
						EDCLaunch edcLaunch = (EDCLaunch) iLaunch;
						List<IChannel> channels = launchChannels.get(edcLaunch);
						if (channels != null)
						{
							for (IChannel iChannel : channels) {
								if (iChannel.getRemotePeer().equals(ipeer)) {
									results.add(edcLaunch);
									break;
								}
							}
						}
					}
				}
			}
		});
		
		return results.toArray(new EDCLaunch[results.size()]);
	}

	/**
	 * @since 2.0
	 */
	@Override
	public ILaunchConfiguration getLaunchConfiguration() {
		if (activeLaunchConfiguration == null)
			activeLaunchConfiguration = super.getLaunchConfiguration();
		return activeLaunchConfiguration;
	}

	/**
	 * @since 2.0
	 */
	public String getCompilationPath(String filename) {
		// TODO Use the source lookup service
		IPath path = Path.EMPTY;
		if (Path.EMPTY.isValidPath(filename)) {
			filename = PathUtils.convertPathToNative(filename);
			ISourceLocator sl = getSourceLocator();
			if (sl instanceof CSourceLookupDirector) {
				path = ((CSourceLookupDirector) sl).getCompilationPath(filename);
			}
			if (path == null) {
				path = PathUtils.findExistingPathIfCaseSensitive(new Path(filename));
			}
		}
		return path.toOSString();
	}

	/**
	 * @since 2.0
	 */
	public String getStartupStopAtPoint() {
		String ret = null;
		try {
			if (getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
					false)) {
				ret = getLaunchConfiguration().getAttribute(
						ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
						ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);
			}
		} catch (CoreException e) {
			// ignore
		}

		return ret;
	}

	/**
	 * Set a short description for this launch.
	 * The default is the name of the initial launch
	 * configuration but usually somewhere in the
	 * initial launch sequence a description of the
	 * debug target can be gathered, either from the
	 * TCF peer attributes or from the connection
	 * settings.
	 * @since 2.0
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns a short description of the launch.
	 * Used as the label for the launch in the
	 * Debug view. 
	 * @since 2.0
	 */
	public String getDescription() {
		if (description == null)
			return getLaunchConfiguration().getName();
		return description;
	}

	/**
	 * Once a launch has been selected for use by a launch delegate
	 * the launching flag is set so clients can know the launch is
	 * in use. Once the launch process completes this launching flag
	 * will be reset.
	 * @since 2.0
	 */
	public void setLaunching(boolean isLaunching) {
		this.isLaunching = isLaunching;
	}

	/**
	 * Returns true if this launch being used by a delegate
	 * for new launch activity.
	 * @since 2.0
	 */
	public boolean isLaunching() {
		return isLaunching;
	}

	/**
	 * When the termination process begins this is called to flag
	 * the launch so clients can know it is shutting down.
	 * @since 2.0
	 */
	public void setTerminating(boolean isTerminating) {
		this.isTerminating = isTerminating;
	}

	/**
	 * Returns true if this launch is in the process of terminating.
	 * Termination is asynchronous and clients can call this to see
	 * if termination is in progress.
	 * @since 2.0
	 */
	public boolean isTerminating() {
		return isTerminating;
	}

}
