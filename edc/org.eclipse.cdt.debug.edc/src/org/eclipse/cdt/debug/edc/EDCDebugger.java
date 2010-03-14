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
package org.eclipse.cdt.debug.edc;

import org.eclipse.cdt.debug.edc.internal.TCFServiceManager;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ILogging;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ISettings;
import org.eclipse.cdt.debug.edc.tcf.extension.services.LoggingProxy;
import org.eclipse.cdt.debug.edc.tcf.extension.services.SettingsProxy;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.framework.debug.FrameworkDebugOptions;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.protocol.Protocol.ChannelOpenListener;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */

@SuppressWarnings("restriction")
public class EDCDebugger extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.edc"; //$NON-NLS-1$

	// The shared instance
	private static EDCDebugger plugin;

	private DebugTrace trace;

    private ITCFServiceManager tcfServiceManager;
	
	private IPreferenceStore preferenceStore;
	
	/**
	 * The constructor
	 */
	public EDCDebugger() {
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
		installChannelListener();
	}

	private void installChannelListener() {

		Protocol.invokeLater(new Runnable() {

			public void run() {
				Protocol.addChannelOpenListener(new ChannelOpenListener() {

					public void onChannelOpen(IChannel channel) {
						// logging service
						if (channel.getRemoteService(ILogging.NAME) != null)
							channel.setServiceProxy(ILogging.class, new LoggingProxy(channel));
						// settings service
						if (channel.getRemoteService(ISettings.NAME) != null)
							channel.setServiceProxy(ISettings.class, new SettingsProxy(channel));
						//
					}
				});
			};
		});
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
		if (tcfServiceManager != null)
			((TCFServiceManager) tcfServiceManager).shutdown();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static EDCDebugger getDefault() {
		return plugin;
	}

	public static BundleContext getBundleContext() {
		return getDefault().getBundle().getBundleContext();
	}

	public DebugTrace getTrace() {
		if (trace == null)
			trace = FrameworkDebugOptions.getDefault().newDebugTrace(getBundle().getSymbolicName());
		return trace;
	}

	public ITCFServiceManager getServiceManager() {
		if (tcfServiceManager == null) {
			tcfServiceManager = new TCFServiceManager();
		}
		return tcfServiceManager;
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

	   public IPreferenceStore getPreferenceStore() {
	        if (preferenceStore == null) {
	            preferenceStore = new ScopedPreferenceStore(new InstanceScope(),getBundle().getSymbolicName());

	        }
	        return preferenceStore;
	    }

	public static IStatus dsfRequestFailedStatus(String message, Throwable exception) {
		return new Status(IStatus.ERROR, PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, message, exception);
	}
}
