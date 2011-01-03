package org.eclipse.cdt.android.build.core;

import java.util.Map;

import org.eclipse.cdt.android.build.internal.core.Activator;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class NDKManager {

	private static final String NDK_LOCATION = "ndkLocation";
	private static final String EMPTY = "";
	private static String ndkLocation;
	
	public static final String LIBRARY_NAME = "libraryName";
	
	private static IEclipsePreferences getPrefs() {
		return new InstanceScope().getNode(Activator.getId());
	}
	
	public static String getNDKLocation() {
		if (ndkLocation == null) {
			ndkLocation = getPrefs().get(NDK_LOCATION, EMPTY);
		}
		return ndkLocation != EMPTY ? ndkLocation : EMPTY;
	}
	
	public static void setNDKLocation(String location) {
		IEclipsePreferences prefs = getPrefs();
		prefs.put(NDK_LOCATION, location);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}
	
	public static void addNativeSupport(final IProject project, Map<String, String> templateArgs, IProgressMonitor monitor) 
			throws CoreException {
		// Launch our template to set up the project contents
		TemplateCore template = TemplateEngine.getDefault().getTemplateById("AddNDKSupport");
		Map<String, String> valueStore = template.getValueStore();
		valueStore.put("projectName", project.getName());
		valueStore.putAll(templateArgs);
		template.executeTemplateProcesses(monitor, false);
		
		// refresh project resources
		project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 10));
	}
	
}
