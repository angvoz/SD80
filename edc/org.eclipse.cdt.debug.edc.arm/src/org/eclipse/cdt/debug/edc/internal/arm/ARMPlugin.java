/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.arm;

import org.eclipse.cdt.debug.edc.MessageLogger;
import org.eclipse.cdt.debug.edc.internal.PersistentCache;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class ARMPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.edc.arm"; //$NON-NLS-1$

	// The shared instance
	private static ARMPlugin plugin;
	   
    private PersistentCache cache;

	/**
	 * The constructor
	 */
	public ARMPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
	public static ARMPlugin getDefault() {
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

	/**
	 * Utility method for creating a CoreException object with this EDC plugin
	 * ID.
	 * 
	 * @param msg
	 *            - error message.
	 * @param e
	 *            - cause exception, can be null.
	 * @return a {@link CoreException} object.
	 */
	public static CoreException newCoreException(String msg, Throwable t) {
		if ((msg == null || msg.length() == 0) && t instanceof CoreException)
			return new CoreException(((CoreException) t).getStatus());
		else
			return new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, msg, t));
	}

	/**
	 * Utility method for creating a CoreException object with this EDC plugin
	 * ID.
	 * 
	 * @param msg
	 *            - error message.
	 * @return a {@link CoreException} object.
	 */
	public static CoreException newCoreException(String msg) {
		return new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, msg));
	}

	/**
	 * Utility method for creating a DebugException object with this EDC plugin
	 * ID.
	 * 
	 * @param msg
	 *            - error message.
	 * @param e
	 *            - cause exception, can be null.
	 * @return a {@link DebugException} object.
	 */
	public static DebugException newDebugException(String msg, Throwable t) {
		if ((msg == null || msg.length() == 0) && t instanceof CoreException)
			return new DebugException(((CoreException) t).getStatus());
		else
			return new DebugException(new Status(IStatus.ERROR, PLUGIN_ID, msg, t));
	}

	/**
	 * Utility method for creating a DebugException object with this EDC plugin
	 * ID.
	 * 
	 * @param msg
	 *            - error message.
	 * @return a {@link DebugException} object.
	 */
	public static DebugException newDebugException(String msg) {
		return new DebugException(new Status(IStatus.ERROR, PLUGIN_ID, msg));
	}

	public PersistentCache getCache() {
		if (cache == null)
		{
			cache = new PersistentCache(getStateLocation().append("cached_data"));
		}
		return cache;
	}

}
