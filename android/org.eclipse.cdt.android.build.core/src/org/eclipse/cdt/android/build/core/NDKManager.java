package org.eclipse.cdt.android.build.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.android.build.internal.core.Activator;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class NDKManager {

	private static final String NDK_LOCATION = "ndkLocation";
	private static final String EMPTY = "";
	private static String ndkLocation;
	
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
	
	public static void addNativeSupport(final IProject project, final String libraryName, IProgressMonitor monitor) 
			throws CoreException, IOException {

		// Launch our template to set up the project contents
		TemplateCore template = TemplateEngine.getDefault().getTemplateById("AddNDKSupport");
		Map<String, String> valueStore = template.getValueStore();
		valueStore.put("projectName", project.getName());
		template.executeTemplateProcesses(monitor, false);
		
		// Generate the Android.mk and initial source file
		// TODO add this to the template
		Map<String, String> map = new HashMap<String, String>();
		map.put("lib", libraryName);

		IFile makefile = project.getFile(new Path("jni/Android.mk"));
		if (!makefile.exists()) {
			URL mkURL = Activator.find(new Path("templates/Android.mk"));
			String contents = readFile(mkURL);
			contents = contents.replaceAll("''lib''", libraryName);
			
			InputStream contentsIn = new ByteArrayInputStream(contents.getBytes());
			makefile.create(contentsIn, true, monitor);

			// Copy over initial source file
			// TODO we should allow C or C++ files
			IFile srcFile = project.getFile(new Path("jni/" + libraryName + ".cpp"));
			if (!srcFile.exists()) {
				URL srcURL = Activator.find(new Path("templates/jni.cpp"));
				InputStream srcIn = srcURL.openStream();

				srcFile.create(srcIn, true, monitor);
			}

			// refresh project resources
			project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 10));
		}
	}
	
	private static String readFile(URL url) throws IOException {
		char[] chars = new char[4092];
		InputStreamReader contentsReader = new InputStreamReader(url.openStream());
		StringBuffer buffer = new StringBuffer();
		while (true) {
			int n = contentsReader.read(chars);
			if (n == -1)
				break;
			buffer.append(chars, 0, n);
		}
		contentsReader.close();
		return buffer.toString();
	}
}
