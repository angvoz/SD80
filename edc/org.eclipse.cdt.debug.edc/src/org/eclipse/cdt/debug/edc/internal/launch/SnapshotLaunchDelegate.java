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
package org.eclipse.cdt.debug.edc.internal.launch;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.launch.EDCLaunchDelegate;
import org.eclipse.cdt.debug.edc.launch.IEDCLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

public class SnapshotLaunchDelegate extends EDCLaunchDelegate {

	public final static String SNAPSHOT_DEBUG_MODEL_ID = "org.eclipse.cdt.debug.edc.internal.snapshot"; //$NON-NLS-1$
	private Album album;
	private ILaunchConfiguration proxyLaunchConfig;

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		String albumLocation = configuration.getAttribute(IEDCLaunchConfigurationConstants.ATTR_ALBUM_FILE, "");
		IPath albumPath = new Path(albumLocation);
		album = Album.getAlbumByLocation(albumPath);
		if (album == null || !album.isLoaded()) {
			if (album == null) {
				album = new Album();
			}
			album.setLocation(albumPath);
			try {
				album.loadAlbum(false);
			} catch (Exception e) {
				EDCDebugger.getMessageLogger().logError(null, e);
				return false;
			}
		}
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchType = lm.getLaunchConfigurationType(album.getLaunchTypeID());
		if (launchType == null) {
			// Can't launch TODO: Need error or exception
			return false;
		}
		proxyLaunchConfig = findExistingLaunchForAlbum(albumLocation);
		if (proxyLaunchConfig == null) {
			String lcName = lm.generateLaunchConfigurationName(album.getDisplayName());
			ILaunchConfigurationWorkingCopy proxyLaunchConfigWC = launchType.newInstance(null, lcName);

			proxyLaunchConfigWC.setAttributes(album.getLaunchProperties());
			proxyLaunchConfigWC.setAttribute(IEDCLaunchConfigurationConstants.ATTR_ALBUM_FILE, configuration
					.getAttribute(IEDCLaunchConfigurationConstants.ATTR_ALBUM_FILE, ""));
			proxyLaunchConfig = proxyLaunchConfigWC.doSave();
		}

		Job launchJob = new Job("Launching " + configuration.getName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					proxyLaunchConfig.launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor(), false, true);
				} catch (CoreException e) {
					EDCDebugger.getMessageLogger().logError(null, e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		launchJob.schedule();
		return false;
	}

	private ILaunchConfiguration findExistingLaunchForAlbum(String albumLocation) {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchType = lm.getLaunchConfigurationType(album.getLaunchTypeID());

		try {
			ILaunchConfiguration[] configurations = lm.getLaunchConfigurations(launchType);
			for (ILaunchConfiguration configuration : configurations) {
				if (albumLocation.equals(configuration.getAttribute(IEDCLaunchConfigurationConstants.ATTR_ALBUM_FILE,
						"")))
					return configuration;
			}
		} catch (CoreException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
		return null;
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		proxyLaunchConfig.launch(mode, new NullProgressMonitor(), false, true);
	}

	@Override
	public String getDebugModelID() {
		return SNAPSHOT_DEBUG_MODEL_ID;
	}

	@Override
	protected Sequence getLiveLaunchSequence(DsfExecutor executor, EDCLaunch launch, IProgressMonitor pm) {
		return null;
	}

	@Override
	protected IDsfDebugServicesFactory newServiceFactory() {
		return null;
	}

	@Override
	protected String getPluginID() {
		return EDCDebugger.getUniqueIdentifier();
	}

}
