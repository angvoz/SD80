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
package org.eclipse.cdt.debug.edc.linux.x86;

import org.eclipse.cdt.debug.edc.MessageLogger;
import org.eclipse.cdt.debug.edc.launch.ChooseProcessItem;
import org.eclipse.cdt.debug.edc.ui.ChooseProcessDialog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LinuxDebugger extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.edc.linux.x86";

	// The shared instance
	private static LinuxDebugger plugin;

	/**
	 * The constructor
	 */
	public LinuxDebugger() {
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
	}

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
	public static LinuxDebugger getDefault() {
		return plugin;
	}

	public static BundleContext getBundleContext() {
		return getDefault().getBundle().getBundleContext();
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

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	static public ChooseProcessItem chooseProcess(final ChooseProcessItem[] processes) throws CoreException {
		final ChooseProcessItem selectedProcessItem[] = { null };
		final boolean chooseProcessCanceled[] = { false };

		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				ChooseProcessDialog dialog = new ChooseProcessDialog(processes, "", Display.getDefault()
						.getActiveShell());
				int dialogResult = dialog.open();

				if (dialogResult == Window.OK) {
					selectedProcessItem[0] = dialog.getSelectedProcess();
				} else {
					chooseProcessCanceled[0] = true;
				}
			}

		});

		if (chooseProcessCanceled[0]) {
			String msg = "user canceled selection of process";
			IStatus status = new Status(IStatus.CANCEL, LinuxDebugger.PLUGIN_ID, msg);
			throw new CoreException(status);
		}
		return selectedProcessItem[0];
	}
}
