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
package org.eclipse.cdt.debug.edc.tests;

import java.io.File;
import java.net.URL;

import org.eclipse.cdt.debug.edc.MessageLogger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EDCTestPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.edc.tests";

	// The shared instance
	private static EDCTestPlugin plugin;

	private static BundleContext bundleContext;

	private Path pluginPath;

	/**
	 * The constructor
	 */
	public EDCTestPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		URL pluginURL = FileLocator.find(context.getBundle(), new Path(""), null); //$NON-NLS-1$
		pluginURL = FileLocator.resolve(pluginURL);
		String pluginFilePath = pluginURL.getFile();
		pluginPath = new Path(pluginFilePath);

		bundleContext = context;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the plugin's bundle context,
	 */
	public static BundleContext getBundleContext() {
		return bundleContext;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static EDCTestPlugin getDefault() {
		return plugin;
	}

	public IPath getPluginFilePath(String inPluginPath) {
		return pluginPath.append(inPluginPath);
	}

	/**
	 * Find a file relative to the project. Works if running in the workbench or
	 * standalone.
	 * 
	 * @param file
	 *            the relative path (from the project) to the file
	 * @return File
	 */
	public static String projectRelativePath(String file) throws Exception {
		File f;
		if (!Platform.isRunning()) {
			// get file relative to CWD (i.e. this project)
			f = new File(file);
			f = f.getCanonicalFile();
		} else {
			// get file relative to running plugin (still this project)
			f = new File(getDefault().getPluginFilePath(file).toOSString());
		}

		return f.getAbsolutePath();
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

	public static void logError(String message, Throwable t) {
		getMessageLogger().log(IStatus.ERROR, message, t);
	}

}
