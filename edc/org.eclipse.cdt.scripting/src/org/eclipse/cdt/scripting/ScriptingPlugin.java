/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.scripting;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.cdt.internal.scripting.RPCBridge;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.ui.IStartup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ScriptingPlugin extends Plugin implements IStartup {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.scripting";

	// The shared instance
	private static ScriptingPlugin plugin;

	private static BundleContext bundleContext;

	public final static String HELP_CONTEXT_ID = "scripting_help_context"; //$NON-NLS-1$

	public final static String SCRIPTING_ENABLED = "Scripting.Scripting_Enabled"; //$NON-NLS-1$
	public final static String PORT_NUMBER = "Scripting.Port_Number"; //$NON-NLS-1$

	/**
	 * The constructor
	 */
	public ScriptingPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleContext = context;
		startServelet();
		readExtensions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static ScriptingPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the plugin's bundle context,
	 */
	public static BundleContext getBundleContext() {
		return bundleContext;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(String message, Throwable t) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, message, t));
	}

	private void startServelet() {
		
		// Configure log4j
		org.apache.log4j.Logger.getLogger("org.jabsorb.JSONSerializer").setLevel(org.apache.log4j.Level.OFF);
		// Uncomment this next line if you want logging to the console
		// org.apache.log4j.Logger.getLogger("org.jabsorb.JSONSerializer").addAppender(new ConsoleAppender(new SimpleLayout()));

		new Thread("Server Sr") {
			@Override
			public void run() {
				try {
					boolean enabled = new InstanceScope().getNode(ScriptingPlugin.PLUGIN_ID).getBoolean(SCRIPTING_ENABLED, false);

					if (enabled) {
						int portNumber = new InstanceScope().getNode(ScriptingPlugin.PLUGIN_ID).getInt(PORT_NUMBER, 5660);
						Bundle bundle = Platform.getBundle("org.eclipse.equinox.http.registry");

						if (bundle.getState() == Bundle.RESOLVED) {
							bundle.start(Bundle.START_TRANSIENT);
						}
						final Dictionary<String, Integer> d = new Hashtable<String, Integer>();
						d.put("http.port", portNumber); //$NON-NLS-1$
						Logger.getLogger("org.mortbay").setLevel(Level.WARNING); //$NON-NLS-1$	

						JettyConfigurator.startServer("jsonrpc", d);
					}
				} catch (Throwable t) {
					ScriptingPlugin.log(null, t);
				}
			}

		}.start();
	}

	private void readExtensions() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(PLUGIN_ID + ".scriptableFeature");
		for (IConfigurationElement element : elements) {
			RPCBridge.instance().addExtension(element);
		}

	}

	public static int newPendingActivityId() {
		int id = Activities.nextActivityId++;
		synchronized (Activities.pendingActivityIds) {
			Activities.pendingActivityIds.add(id);
		}
		return id;
	}

	public static void setActivityDone(int id) {
		synchronized (Activities.pendingActivityIds) {
			Activities.pendingActivityIds.remove(id);
		}
	}

	public void earlyStartup() {
	}

}
