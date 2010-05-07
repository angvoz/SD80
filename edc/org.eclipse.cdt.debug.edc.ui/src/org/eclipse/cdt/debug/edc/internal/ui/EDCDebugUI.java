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
package org.eclipse.cdt.debug.edc.internal.ui;

import org.eclipse.cdt.debug.edc.MessageLogger;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.snapshot.ISnapshotAlbumEventListener;
import org.eclipse.cdt.debug.edc.internal.snapshot.Snapshot;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.ui.views.SnapshotView;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EDCDebugUI extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.edc.ui";

	// The shared instance
	private static EDCDebugUI plugin;

	private IPreferenceStore preferenceStore;

	/**
	 * The constructor
	 */
	public EDCDebugUI() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		startSnapshotViewSupport();
	}

	private void startSnapshotViewSupport() {
		
		Album.addSnapshotAlbumEventListener(new ISnapshotAlbumEventListener() {
			
			public void snapshotSessionEnded(Album album, DsfSession session) {}
			
			public void snapshotOpened(Snapshot snapshot) {}
			
			public void snapshotCreated(final Album album, final Snapshot snapshot,
					final DsfSession session, final StackFrameDMC stackFrame) {
				// Open the album view.
				UIJob openViewJob = new UIJob("Open Snapshot Album View"){

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						IWorkbench workbench = PlatformUI.getWorkbench();
						IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
						if (workbenchWindow != null) {
							IWorkbenchPage page = workbenchWindow.getActivePage();
							IViewPart snapshotView = page.findView(SnapshotView.SNAPSHOT_VIEW_ID);
							boolean firstTime = snapshotView == null;
							try {
								if (firstTime)
								{
									snapshotView = page.showView(SnapshotView.SNAPSHOT_VIEW_ID);
									((ISnapshotAlbumEventListener) snapshotView).snapshotCreated(album, snapshot, session, stackFrame);
								}
								else
								{
									if (!page.isPartVisible(snapshotView))
									{
										snapshotView = page.showView(SnapshotView.SNAPSHOT_VIEW_ID);
									}
								}
							} catch (PartInitException e) {
								EDCDebugUI.logError("", e);
							}
						}
						return Status.OK_STATUS;}
				};
				openViewJob.schedule();
			}});	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static EDCDebugUI getDefault() {
		return plugin;
	}

	public static MessageLogger getMessageLogger() {
		return new MessageLogger() {
			@Override
			public String getPluginID() {
				return PLUGIN_ID;
			}

			@Override
			public Plugin getPlugin() {
				return plugin;
			}
		};
	}

	public static BundleContext getBundleContext() {
		return getDefault().getBundle().getBundleContext();
	}

	public static IStatus newErrorStatus(int errCode, String message, Throwable exception) {
		return new Status(IStatus.ERROR, PLUGIN_ID, errCode, message, exception);
	}
	
	public static void logError(String message, Throwable t) {
		getMessageLogger().log(IStatus.ERROR, message, t);
	}

	public IPreferenceStore getPreferenceStore() {
		if (preferenceStore == null) {
			preferenceStore = new ScopedPreferenceStore(new InstanceScope(), EDCDebugger.PLUGIN_ID);

		}
		return preferenceStore;
	}
	
	public static IStatus dsfRequestFailedStatus(String message, Throwable exception) {
		return new Status(IStatus.ERROR, PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, message, exception);
	}

}
