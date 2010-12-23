package org.eclipse.cdt.android.build.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.android.build.internal.core.Activator;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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

		// TODO make this a real CDT template so we can call from the new project wizard for
		// Java free native projects.
		
		// Create the source and output folders
		IFolder sourceFolder = project.getFolder("jni");
		if (!sourceFolder.exists())
			sourceFolder.create(true, true, monitor);
		IPathEntry sourceEntry = CoreModel.newSourceEntry(sourceFolder.getFullPath());
				
		IFolder libFolder = project.getFolder("libs");
		if (!libFolder.exists())
			libFolder.create(true, true, monitor);
		IPathEntry libEntry = CoreModel.newOutputEntry(libFolder.getFullPath());
		
		IFolder objFolder = project.getFolder("obj");
		if (!objFolder.exists())
			objFolder.create(true, true, monitor);
		IPathEntry objEntry = CoreModel.newOutputEntry(objFolder.getFullPath());
		
		// Set up the path entries for the source and output folders
		CoreModel model = CCorePlugin.getDefault().getCoreModel();
		ICProject cproject = model.create(project);
		IPathEntry[] pathEntries = cproject.getRawPathEntries();
		List<IPathEntry> newEntries = new ArrayList<IPathEntry>(pathEntries.length + 2);
		for (IPathEntry pathEntry : pathEntries) {
			// remove the old source and output entries
			if (pathEntry.getEntryKind() != IPathEntry.CDT_SOURCE
					&& pathEntry.getEntryKind() != IPathEntry.CDT_OUTPUT) {
				newEntries.add(pathEntry);
			}
		}
		newEntries.add(sourceEntry);
		newEntries.add(libEntry);
		newEntries.add(objEntry);
		cproject.setRawPathEntries(newEntries.toArray(new IPathEntry[newEntries.size()]), monitor);
				
		// Generate the Android.mk and initial source file
		// TODO make this a real CDT template
		Map<String, String> map = new HashMap<String, String>();
		map.put("lib", libraryName);

		IFile makefile = sourceFolder.getFile("Android.mk");
		if (!makefile.exists()) {
			URL mkURL = Activator.find(new Path("templates/Android.mk"));
			String contents = readFile(mkURL);
			contents = contents.replaceAll("''lib''", libraryName);
			
			InputStream contentsIn = new ByteArrayInputStream(contents.getBytes());
			makefile.create(contentsIn, true, monitor);
				
			// Copy over initial source file
			// TODO we should allow C or C++ files
			IFile srcFile = sourceFolder.getFile(libraryName + ".cpp");
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
