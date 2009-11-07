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
package org.eclipse.cdt.debug.edc.internal.snapshot;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.ZipFileUtil;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.core.LaunchManager;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The Album class represents a series of snapshots that record moments in a
 * debug session. An Album manages the collection of snapshots, common resources
 * such as source files, persistence, and association with debug sessions.
 * 
 * An Album is usually created during a debug session, saved at the conclusion
 * of the session, and reopened by a launch delegate for a new snapshot debug
 * session.
 * 
 * When an Album is saved it's data and resources are archived in a snapshot
 * file in a default location. When reopened the contents are expanded into a
 * temporary directory and used to recreate the debug session.
 */
public class Album extends PlatformObject {

	// XML element names
	public static final String SNAPSHOT = "snapshot";
	private static final String ALBUM = "album";
	private static final String LAUNCH = "launch";
	private static final String RESOURCES = "resources";
	private static final String FILE = "file";
	private static final String INFO = "info";

	private static final String ALBUM_DATA = "album.xml";

	private static final String ALBUM_VERSION = "100";

	private Document document;
	private Element albumRootElement;

	private final List<Snapshot> snapshots = new ArrayList<Snapshot>();
	private String sessionID = "";
	private IPath albumRootDirectory;
	private boolean launchConfigSaved;
	private String launchType;
	private HashMap<String, Object> launchProperties;
	private String launchName;
	private String name;
	private boolean loaded;
	private final Set<IPath> files = new HashSet<IPath>();

	private int currentSnapshotIndex;
	private IPath location;
	private boolean resourceListSaved;
	private boolean albumInfoSaved;

	private static Map<String, Album> albumsBySessionID = Collections.synchronizedMap(new HashMap<String, Album>());
	private static Map<IPath, Album> albumsByLocation = Collections.synchronizedMap(new HashMap<IPath, Album>());

	private static boolean sessionEndedListenerAdded;
	private static SessionEndedListener sessionEndedListener = new SessionEndedListener() {

		public void sessionEnded(DsfSession session) {
			synchronized (albumsBySessionID) {
				Album album = albumsBySessionID.get(session.getId());
				if (album != null) {
					album.saveAlbum(new NullProgressMonitor());
					albumsBySessionID.remove(album);
				}
			}
		}
	};

	public interface IAlbumArchiveEntry {

		public String createEntryName(File file);

	}

	public Album() {
		super();
		try {
			document = DebugPlugin.newDocument();
			albumRootElement = document.createElement(ALBUM);
			document.appendChild(albumRootElement);
			if (!sessionEndedListenerAdded)
				DsfSession.addSessionEndedListener(sessionEndedListener);
			sessionEndedListenerAdded = true;
		} catch (CoreException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}

	}

	public String getName() {
		if (name == null) {
			name = getDefaultAlbumName();
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
		if (sessionID.length() > 0)
			albumsBySessionID.put(sessionID, this);
	}

	public static boolean isSnapshotSession(String sessionId) {
		EDCLaunch launch = EDCLaunch.getLaunchForSession(sessionId);
		return launch != null && launch.isSnapshotLaunch();
	}

	private static String getServiceFilter(String sessionId) {
		return ("(" + IDsfService.PROP_SESSION_ID + "=" + sessionId + ")").intern(); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

	public Snapshot createSnapshot(DsfSession session) {

		configureAlbum();

		Snapshot result = null;
		try {
			Element snapshotRoot = document.createElement(SNAPSHOT);
			albumRootElement.appendChild(snapshotRoot);

			ServiceReference[] references = EDCDebugger.getBundleContext().getServiceReferences(
					ISnapshotContributor.class.getName(), getServiceFilter(session.getId()));
			for (ServiceReference serviceReference : references) {
				ISnapshotContributor sc = (ISnapshotContributor) EDCDebugger.getBundleContext().getService(
						serviceReference);
				Element serviceElement = sc.takeShapshot(this, document, new NullProgressMonitor());
				if (serviceElement != null)
					snapshotRoot.appendChild(serviceElement);
			}
			result = new Snapshot(snapshotRoot);
			snapshots.add(result);
		} catch (InvalidSyntaxException e) {
			EDCDebugger.getMessageLogger().logError("Invalid session ID syntax", e); //$NON-NLS-1$
		} catch (IllegalStateException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}

		return result;
	}

	private void configureAlbum() {
		saveAlbumInfo();
		saveLaunchConfiguration();
	}

	private void saveAlbumInfo() {
		if (!albumInfoSaved) {
			Element infoElement = document.createElement(INFO);
			infoElement.setAttribute("version", ALBUM_VERSION);
			Calendar calendar = Calendar.getInstance();
			infoElement.setAttribute("month", Integer.toString(calendar.get(Calendar.MONTH)));
			infoElement.setAttribute("day", Integer.toString(calendar.get(Calendar.DATE)));
			infoElement.setAttribute("year", Integer.toString(calendar.get(Calendar.YEAR)));
			infoElement.setAttribute("hour", Integer.toString(calendar.get(Calendar.HOUR)));
			infoElement.setAttribute("minute", Integer.toString(calendar.get(Calendar.MINUTE)));
			infoElement.setAttribute("second", Integer.toString(calendar.get(Calendar.SECOND)));

			Properties systemProps = System.getProperties();
			Map<String, Object> infoProps = new HashMap<String, Object>();
			Set<Object> systemKeys = systemProps.keySet();

			for (Object sysKey : systemKeys) {
				if (sysKey instanceof String)
					infoProps.put((String) sysKey, systemProps.get(sysKey));
			}
			Element propsElement = SnapshotUtils.makeXMLFromProperties(document, infoProps);
			infoElement.appendChild(propsElement);

			albumRootElement.appendChild(infoElement);
			albumInfoSaved = true;
		}
	}

	private void saveResourceList() {
		if (!resourceListSaved) {
			Element resourcesElement = document.createElement(RESOURCES);
			for (IPath filePath : files) {
				Element fileElement = document.createElement(FILE);
				fileElement.setAttribute("path", filePath.toOSString());
				resourcesElement.appendChild(fileElement);
			}
			albumRootElement.appendChild(resourcesElement);
			resourceListSaved = true;
		}
	}

	@SuppressWarnings("unchecked")
	private void saveLaunchConfiguration() {
		if (!launchConfigSaved) {
			EDCLaunch launch = EDCLaunch.getLaunchForSession(sessionID);
			try {
				Map<String, Object> map = launch.getLaunchConfiguration().getAttributes();
				Element launchElement = document.createElement(LAUNCH);
				launchType = launch.getLaunchConfiguration().getType().getIdentifier();
				launchName = launch.getLaunchConfiguration().getName();
				launchElement.setAttribute("type", launchType);
				launchElement.setAttribute("name", launchName);
				Element propsElement = SnapshotUtils.makeXMLFromProperties(document, map);
				launchElement.appendChild(propsElement);
				albumRootElement.appendChild(launchElement);
			} catch (CoreException e) {
				EDCDebugger.getMessageLogger().logError(null, e);
			}
			launchConfigSaved = true;
		}
	}

	private static void addZipEntry(ZipOutputStream zipOut, IAlbumArchiveEntry entry, File file)
			throws FileNotFoundException, IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File child : file.listFiles()) {
					addZipEntry(zipOut, entry, child);
				}
			} else {
				// Add ZIP entry to output stream.m
				String path = ""; //$NON-NLS-1$

				if (entry != null) {
					path = entry.createEntryName(file);
				} else {
					path = file.getName();
				}

				zipOut.putNextEntry(new ZipEntry(path));

				// Create a buffer for reading the files
				byte[] buf = new byte[1024];

				// Transfer bytes from the file to the ZIP file
				// and compress the files
				FileInputStream in = new FileInputStream(file);
				int len;
				while ((len = in.read(buf)) > 0) {
					zipOut.write(buf, 0, len);
				}

				// Complete the entry
				zipOut.closeEntry();
				in.close();
			}
		}
	}

	public IProject getSnapshotsProject() {

		final String SNAPSHOT_PROJECT_ID = "org.eclipse.cdt.debug.edc.snapshot"; //$NON-NLS-1$

		IProject snapshotsProject = null;
		// See if the default project exists
		String defaultProjectName = "Snapshots";
		ICProject cProject = CoreModel.getDefault().getCModel().getCProject(defaultProjectName);
		if (cProject.exists()) {
			snapshotsProject = cProject.getProject();
		} else {
			final String[] ignoreList = { ".project", //$NON-NLS-1$
					".cdtproject", //$NON-NLS-1$
					".cproject", //$NON-NLS-1$
					".cdtbuild", //$NON-NLS-1$
					".settings", //$NON-NLS-1$
			};

			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProject newProjectHandle = workspace.getRoot().getProject(defaultProjectName);

			int projectSuffix = 2;
			while (newProjectHandle.exists()) {
				newProjectHandle = workspace.getRoot().getProject(defaultProjectName + projectSuffix);
				projectSuffix++;
			}

			IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
			description.setLocation(null);
			IFileStore store;
			try {
				store = EFS.getStore(workspace.getRoot().getLocationURI());
				store = store.getChild(newProjectHandle.getName());
				for (String deleteName : ignoreList) {
					IFileStore projFile = store.getChild(deleteName);
					projFile.delete(EFS.NONE, new NullProgressMonitor());
				}
				IFileStore[] children = store.childStores(EFS.NONE, new NullProgressMonitor());
				for (IFileStore fileStore : children) {
					if (fileStore.fetchInfo().isDirectory())
						fileStore.delete(EFS.NONE, new NullProgressMonitor());
				}
				snapshotsProject = CCorePlugin.getDefault().createCProject(description, newProjectHandle, null,
						SNAPSHOT_PROJECT_ID);
			} catch (Exception e) {
				EDCDebugger.getMessageLogger().logError(null, e);
			}
		}
		return snapshotsProject;
	}

	@SuppressWarnings("restriction")
	private void saveAlbum(IProgressMonitor monitor) {

		IPath zipPath = getSnapshotsProject().getLocation();
		zipPath = zipPath.append(getDefaultAlbumName());
		zipPath = zipPath.addFileExtension("dsa");

		try {
			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));

			for (IPath path : files) {

				IAlbumArchiveEntry entry = new IAlbumArchiveEntry() {

					public String createEntryName(File file) {
						StringBuffer entryPath = new StringBuffer();

						entryPath.append("Resources/");

						IPath filepath = new Path(file.getAbsolutePath());

						String deviceName = filepath.getDevice();
						// Remove the : from the end
						entryPath.append(deviceName.substring(0, deviceName.length() - 1));
						entryPath.append("/");

						String[] segments = filepath.segments();
						int numSegments = segments.length - 1;

						for (int i = 0; i < numSegments; i++) {
							entryPath.append(segments[i]);
							entryPath.append("/");
						}
						entryPath.append(file.getName());
						return entryPath.toString();
					}
				};
				addZipEntry(zipOut, entry, path.toFile());
				if (monitor != null) {
					monitor.worked(1);
				}
			}
			zipOut.putNextEntry(new ZipEntry(ALBUM_DATA));

			saveResourceList();

			String xml = LaunchManager.serializeDocument(document);
			zipOut.write(xml.getBytes("UTF8")); //$NON-NLS-1$
			zipOut.closeEntry();

			zipOut.close();

		} catch (Exception e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
	}

	private String getDefaultAlbumName() {
		StringBuffer albumName = new StringBuffer();
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		if (hour < 10)
			albumName.append('0');
		albumName.append(hour);
		if (minute < 10)
			albumName.append('0');
		albumName.append(minute);
		if (second < 10)
			albumName.append('0');
		albumName.append(second);
		albumName.append('_');
		albumName.append(getLaunchName());
		return albumName.toString();
	}

	public void saveAlbum(IPath path) throws TransformerException, IOException {
		String xml = LaunchManager.serializeDocument(document);
		File file = path.toFile();
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream stream = new FileOutputStream(file);
		stream.write(xml.getBytes("UTF8")); //$NON-NLS-1$
		stream.close();
	}

	public void openSnapshot(int index) throws Exception {
		currentSnapshotIndex = index;
		loadAlbum();
		DsfSession session = DsfSession.getSession(sessionID);
		if (session != null && snapshots.size() >= index) {
			Snapshot snapshot = snapshots.get(index);
			snapshot.open(session);
		}
	}

	public int getCurrentSnapshotIndex() {
		return currentSnapshotIndex;
	}

	public void openNextSnapshot() throws Exception {
		int nextIndex = currentSnapshotIndex + 1;
		if (nextIndex >= snapshots.size())
			nextIndex = 0;
		openSnapshot(nextIndex);
	}

	public void openPreviousSnapshot() throws Exception {
		int previousIndex = currentSnapshotIndex - 1;
		if (previousIndex < 0)
			previousIndex = snapshots.size() - 1;
		openSnapshot(previousIndex);
	}

	public void loadAlbum() throws ParserConfigurationException, SAXException, IOException {
		if (!loaded) {
			File albumFile = location.toFile();
			setName(albumFile.getName());
			try {
				ZipFileUtil.unzipFiles(albumFile, getAlbumRootDirectory().toOSString(), new NullProgressMonitor());
			} catch (Exception e) {
				EDCDebugger.getMessageLogger().logError(null, e);
			}
			FileInputStream fileStream = new FileInputStream(getAlbumCatalog().toOSString());
			BufferedInputStream stream = new BufferedInputStream(fileStream);
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			document = parser.parse(new InputSource(stream));

			loadAlbumInfo();
			loadLaunchConfiguration();
			loadResourceList();

			NodeList snapshotElements = document.getElementsByTagName(SNAPSHOT);
			int numSnapshots = snapshotElements.getLength();
			for (int i = 0; i < numSnapshots; i++) {
				Element snapshotRoot = (Element) snapshotElements.item(i);
				snapshots.add(new Snapshot(snapshotRoot));
			}

			loaded = true;
		}
	}

	private void loadAlbumInfo() {
		Element infoElement = (Element) document.getElementsByTagName(INFO).item(0);

	}

	private void loadResourceList() {
		NodeList resources = document.getElementsByTagName(RESOURCES);
		NodeList elementFiles = ((Element) resources.item(0)).getElementsByTagName(FILE);
		int numFiles = elementFiles.getLength();
		for (int i = 0; i < numFiles; i++) {
			Element fileElement = (Element) elementFiles.item(i);
			String elementPath = fileElement.getAttribute("path");
			files.add(new Path(elementPath));
		}
	}

	private void loadLaunchConfiguration() {
		NodeList launchElements = document.getElementsByTagName(LAUNCH);
		Element launchElement = (Element) launchElements.item(0);
		launchType = launchElement.getAttribute("type");
		launchName = launchElement.getAttribute("name");

		Element propElement = (Element) launchElement.getElementsByTagName(SnapshotUtils.PROPERTIES).item(0);
		launchProperties = new HashMap<String, Object>();
		try {
			SnapshotUtils.initializeFromXML(propElement, launchProperties);
		} catch (Exception e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}

	}

	private IPath getAlbumCatalog() {
		return getAlbumRootDirectory().append(ALBUM_DATA);
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(Document.class))
			return document;

		return super.getAdapter(adapter);
	}

	private IPath getAlbumRootDirectory() {
		if (albumRootDirectory == null) {
			IPath path = EDCDebugger.getDefault().getStateLocation().append("SnapshotAlbums");
			String locationName = location.lastSegment();
			int extension = locationName.lastIndexOf(".");
			if (extension > 0) {
				locationName = locationName.substring(0, extension);
			}
			path = path.append(locationName);
			File dir = path.toFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			albumRootDirectory = path;
		}
		return albumRootDirectory;
	}

	public String getLaunchTypeID() {
		return launchType;
	}

	public HashMap<String, Object> getLaunchProperties() {
		return launchProperties;
	}

	public void setLaunchProperties(HashMap<String, Object> launchProperties) {
		this.launchProperties = launchProperties;
	}

	public String getLaunchName() {
		return launchName;
	}

	public void playSnapshots(DsfSession session) {
		// TODO Auto-generated method stub

	}

	public void addFile(IPath path) {
		files.add(path);
	}

	public static Album getAlbumByLocation(IPath path) {
		return albumsByLocation.get(path);
	}

	public static Album getAlbumBySession(String sessionId) {
		return albumsBySessionID.get(sessionId);
	}

	public void setLocation(IPath albumPath) {
		this.location = albumPath;
		albumsByLocation.put(albumPath, this);
	}

	public IPath getLocation() {
		return location;
	}

	@SuppressWarnings("restriction")
	public void configureMappingSourceContainer(MappingSourceContainer mappingContainer) {
		IPath albumRoot = getAlbumRootDirectory();
		String device = "";
		albumRoot = albumRoot.append("Resources");
		for (IPath iPath : files) {
			device = iPath.getDevice();
		}
		MapEntrySourceContainer[] entries = new MapEntrySourceContainer[] { new MapEntrySourceContainer(
				new Path(device), albumRoot) };
		mappingContainer.addMapEntries(entries);
	}
}
