package org.eclipse.cdt.codan.checkers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CodanCheckersActivator extends Plugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.codan.checkers";
	// The shared instance
	private static CodanCheckersActivator plugin;

	/**
	 * The constructor
	 */
	public CodanCheckersActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
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
	public static CodanCheckersActivator getDefault() {
		return plugin;
	}

	/**
	 * @param e
	 */
	public static void log(Throwable e) {
		getDefault().getLog().log(getStatus(e));
	}

	public static void log(String message) {
		getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, message));
	}

	/**
	 * @param e
	 * @return
	 */
	public static IStatus getStatus(Throwable e) {
		return new Status(Status.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e);
	}
}
