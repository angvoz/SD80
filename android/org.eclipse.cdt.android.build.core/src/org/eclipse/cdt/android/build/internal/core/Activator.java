/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.android.build.internal.core;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends Plugin {

	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	public static Activator getDefault() {
		return plugin;
	}
	
	public static String getId() {
		return plugin.getBundle().getSymbolicName();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> clazz) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference ref = context.getServiceReference(clazz.getName());
		return (ref != null) ? (T)context.getService(ref) : null;
	}
	
	public static Bundle getBundle(String id) {
		for (Bundle bundle : plugin.getBundle().getBundleContext().getBundles()) {
			if (bundle.getSymbolicName().equals(id)) {
				return bundle;
			}
		}
		return null;
	}
	
	public static IStatus newStatus(Exception e) {
		return new Status(IStatus.ERROR, getId(), e.getMessage(), e);
	}
	
	public static void log(Exception e) {
		plugin.getLog().log(newStatus(e));
	}
	
	public static URL findFile(IPath path) {
		return FileLocator.find(plugin.getBundle(), path, null);
	}

}
