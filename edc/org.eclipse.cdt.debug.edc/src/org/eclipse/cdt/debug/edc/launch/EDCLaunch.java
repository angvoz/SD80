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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.PathUtils;
import org.eclipse.cdt.debug.edc.internal.launch.ShutdownSequence;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Processes;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * The only object in the model that implements the traditional interfaces.
 */
@ThreadSafe
public class EDCLaunch extends Launch {
	private DefaultDsfExecutor executor;
	private final DsfSession session;
	private DsfServicesTracker tracker;
	private boolean initialized = false;
	private boolean shutDown = false;

	private DsfMemoryBlockRetrieval memRetrieval;
	private IDsfDebugServicesFactory serviceFactory;
	private final String debugModelID;
	private Album album;
	private boolean snapshotSupportInitialized;
	private boolean isFirstLaunch = true;
	private ILaunchConfiguration activeLaunchConfiguration;
	private List<ILaunchConfiguration> affiliatedLaunchConfigurations = Collections.synchronizedList(new ArrayList<ILaunchConfiguration>());

	private static final Map<EDCLaunch, List<IChannel>> launchChannels = Collections
			.synchronizedMap(new HashMap<EDCLaunch, List<IChannel>>());

	private static final Map<String, EDCLaunch> launchSessions = Collections
			.synchronizedMap(new HashMap<String, EDCLaunch>());

	public EDCLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator, String ownerID) {
		super(launchConfiguration, mode, locator);

		debugModelID = ownerID;

		// Create the dispatch queue to be used by debugger control and services
		// that belong to this launch
		final DefaultDsfExecutor dsfExecutor = new DefaultDsfExecutor(ownerID);
		dsfExecutor.prestartCoreThread();
		executor = dsfExecutor;
		session = DsfSession.startSession(executor, ownerID);
		launchSessions.put(session.getId(), this);
	}

	public static EDCLaunch getLaunchForSession(String sessionID) {
		return launchSessions.get(sessionID);
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
		// are terminated.
		if (e.getDMContext() instanceof RootExecutionDMC)
			shutdownSession(new RequestMonitor(ImmediateExecutor.getInstance(), null));
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
		return shutDown;
	}

	@Override
	public void terminate() throws DebugException {
        getDsfExecutor().execute(new Runnable() {
            public void run() {
                RunControl runControlService = tracker.getService(RunControl.class);
                if (runControlService != null)
                	runControlService.terminateAllContexts(null);
            }
        });
	}

	// ITerminate
	// /////////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////////
	// IDisconnect
	@Override
	public boolean canDisconnect() {
		return canTerminate();
	}

	@Override
	public boolean isDisconnected() {
		return isTerminated();
	}

	@Override
	public void disconnect() throws DebugException {
        getDsfExecutor().execute(new Runnable() {
            public void run() {
                Processes procService = tracker.getService(Processes.class);
                if (procService != null)
                	procService.detachDebuggerFromSession(null);
            }
        });
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
		if (shutDown) {
			rm.done();
			return;
		}
		shutDown = true;

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

				rm.setStatus(getStatus());
				rm.done();
			}
		});
		executor.execute(shutdownSeq);
		if (isSnapshotLaunch()) {
			try {
				// delete launch configuration
				ILaunchConfiguration lc = SnapshotUtils.findExistingLaunchForAlbum(album);
				if (lc != null){
					lc.delete();
				}
			} catch (Throwable e) {
				EDCDebugger.getMessageLogger().logError(null, e);
			}
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
				if (exePath.length() > 0) {
					IPath exeDirectory = new Path(exePath).removeLastSegments(1);
					ISourceContainer[] containers = new ISourceContainer[] { new DirectorySourceContainer(exeDirectory,
							false) };
					director.setSourceContainers(containers);
				}
			}
		} catch (CoreException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
		return director;
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
	public ILaunchConfiguration getActiveLaunchConfiguration() {
		if (activeLaunchConfiguration == null)
			activeLaunchConfiguration = getLaunchConfiguration();
		return activeLaunchConfiguration;
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
		String memento = getActiveLaunchConfiguration().getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String) null);
		if (memento == null) {
			locator.initializeDefaults(getActiveLaunchConfiguration());
		} else {
			locator.initializeFromMemento(memento, getActiveLaunchConfiguration());
		}
		return locator;
	}

}
