package org.eclipse.cdt.android.build.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.android.build.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static IStatus newStatus(Exception e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e);
	}

	public static void log(Exception e) {
		if (e instanceof CoreException)
			plugin.getLog().log(((CoreException)e).getStatus());
		else if (e instanceof InvocationTargetException) {
			Throwable e2 = ((InvocationTargetException)e).getTargetException();
			if (e2 instanceof CoreException)
				plugin.getLog().log(((CoreException)e).getStatus());
			else
				plugin.getLog().log(newStatus(e));
		} else
			plugin.getLog().log(newStatus(e));
	}

	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}
	
}
