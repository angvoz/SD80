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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.PathUtils;
import org.eclipse.cdt.debug.edc.internal.ZipFileUtils;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
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
@SuppressWarnings("restriction")
public class Album extends PlatformObject {

	// XML element names
	public static final String SNAPSHOT = "snapshot";
	private static final String ALBUM = "album";
	private static final String LAUNCH = "launch";
	private static final String RESOURCES = "resources";
	private static final String FILE = "file";
	private static final String INFO = "info";

	public static final String METADATA = "snapshotMetaData";
	public static final String SNAPSHOT_LIST = "snapshots";

	private static final String ALBUM_DATA = "album.xml";

	private static final String ALBUM_VERSION = "100";

	private static String[] DSA_FILE_EXTENSIONS = new String[] {"dsa"};

	// Preferences
	private static final String CREATION_CONTROL = "creation_control";

	private static final String CAMERA_CLICK_WAV = "/sounds/camera_click.wav";
	private static final String SNAPSHOT_VIEW_ID = "org.eclipse.cdt.debug.edc.ui.views.SnapshotView";
	
	private Document document;
	private Element albumRootElement;

	private final List<Snapshot> snapshotList = new ArrayList<Snapshot>();
	private String sessionID = "";
	private String recordingSessionID = "";
	private IPath albumRootDirectory;
	private boolean launchConfigSaved;
	private String launchType;
	private HashMap<String, Object> launchProperties;
	private String launchName;
	private String name;
	private boolean loaded;
	private boolean metaDataLoaded;
	private final Set<IPath> files = new HashSet<IPath>();

	private int currentSnapshotIndex;
	private IPath location;
	private boolean resourceListSaved;
	private boolean metadataSaved;
	private boolean albumInfoSaved;
	private String displayName;

    /**
     * Listener for state changes on albums
     */
	protected static List<ISnapshotAlbumStateListener> listeners = new ArrayList<ISnapshotAlbumStateListener>();

	private static Map<String, Album> albumsBySessionID = Collections.synchronizedMap(new HashMap<String, Album>());
	private static Map<String, Album> albumsRecordingBySessionID = Collections.synchronizedMap(new HashMap<String, Album>());	
	private static Map<IPath, Album> albumsByLocation = Collections.synchronizedMap(new HashMap<IPath, Album>());
	private static String snapshotCreationControl;

	private static boolean sessionEndedListenerAdded;
	private static SessionEndedListener sessionEndedListener = new SessionEndedListener() {

		public void sessionEnded(DsfSession session) {
			Album album = albumsRecordingBySessionID.get(session.getId());
			if (album == null)
				album = albumsBySessionID.get(session.getId());
			if (album != null && session.getId().equals(album.getRecordingSessionID())) {
				album.saveResources(new NullProgressMonitor());
				album.setRecordingSessionID("");
			}
			synchronized (albumsRecordingBySessionID) {
			albumsRecordingBySessionID.remove(session.getId());
			}
			synchronized (albumsBySessionID) {
			albumsBySessionID.remove(session.getId());
			}

			if (album != null) {
				fireAlbumStateChanged(album);
				showSnapshotView();
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

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		if (displayName == null || displayName.length() == 0) {
			displayName = getName();
		}
		return displayName;
	}

	public String getSessionID() {
		sessionID = "";
		if (albumsBySessionID != null) {
			for (Map.Entry<String, Album> entry : albumsBySessionID.entrySet()){
				if (entry.getValue().location != null && entry.getValue().location.equals(getLocation())){
					sessionID = entry.getKey();
				}
			}
		}
		return sessionID;
	}
	
	public String getRecordingSessionID() {
		return recordingSessionID;
	}

	public void setRecordingSessionID(String sessionID) {
		this.recordingSessionID = sessionID;
		if (sessionID.length() > 0)
			albumsRecordingBySessionID.put(sessionID, this);
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
		if (sessionID.length() > 0)
			albumsBySessionID.put(sessionID, this);
	}
	
	/**
	 * Is the album currently open for recording
	 * @param sessionId
	 * @return true if the album is currently being recording by an active debug session
	 */
	public static boolean isSnapshotSession(String sessionId) {
		EDCLaunch launch = EDCLaunch.getLaunchForSession(sessionId);
		return launch != null && launch.isSnapshotLaunch();
	}
	
	public Snapshot createSnapshot(DsfSession session, String displayName) {
		configureAlbum();
		
		if (getLocation() == null || !getLocation().toFile().exists()){
				createEmptyAlbum(); 
		}
		
		Snapshot snapshot = new Snapshot(this, session, displayName);
		snapshot.writeSnapshotData();

		snapshotList.add(snapshot);
		saveAlbum(new NullProgressMonitor());
		
		return snapshot;
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

	private void saveSnapshotMetadata() {
		if (!metadataSaved || isRecording()) {
					
			if (metadataSaved){
				// If metatdata is saved, it must be a live debug session so
				// we need to add a new snapshot to the snapshot list
				NodeList snapMetaDataNode = document.getElementsByTagName(METADATA);
				document.getDocumentElement().removeChild(snapMetaDataNode.item(0));
			}
			
			Element metadataElement = document.createElement(METADATA);

			Element albumElement = document.createElement(ALBUM);
			albumElement.setAttribute("albumName", this.getDisplayName());
			metadataElement.appendChild(albumElement);

			Element snapshotsElement = document.createElement(SNAPSHOT_LIST);
			metadataElement.appendChild(snapshotsElement);

			for (Snapshot snap : snapshotList) {
				Element snapshotMetadataElement = document.createElement(SNAPSHOT);
				if (snap.getSnapshotDisplayName().length() == 0) {
					snapshotMetadataElement.setAttribute("displayName", snap.getSnapshotFileName());
				} else {
					snapshotMetadataElement.setAttribute("displayName", snap.getSnapshotDisplayName());
				}
				if (snap.getCreationDate() != null) {
					snapshotMetadataElement.setAttribute("date", snap.getCreationDate().toString());
				} else {
					snapshotMetadataElement.setAttribute("date", "unknown");
				}

				snapshotMetadataElement.setAttribute("description", snap.getSnapshotDescription());

				snapshotMetadataElement.setAttribute("fileName", snap.getSnapshotFileName());
				snapshotsElement.appendChild(snapshotMetadataElement);
			}
			albumRootElement.appendChild(metadataElement);
			metadataSaved = true;
		}
	}

	@SuppressWarnings("unchecked")
	private void saveLaunchConfiguration() {
		if (!launchConfigSaved) {
			EDCLaunch launch = EDCLaunch.getLaunchForSession(getRecordingSessionID());
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

	/**
	 * Create and write a full snapshot album from scratch
	 */
	private void saveAlbum(IProgressMonitor monitor) {

		IPath zipPath = getLocation();
		ZipOutputStream zipOut = null;
		try {
			zipOut = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));

			zipOut.putNextEntry(new ZipEntry(ALBUM_DATA));

			saveResourceList();
			saveSnapshotMetadata();

			String xml = LaunchManager.serializeDocument(document);
			zipOut.write(xml.getBytes("UTF8")); //$NON-NLS-1$
			zipOut.closeEntry();

			for (Snapshot snap : snapshotList) {
				zipOut.putNextEntry(new ZipEntry(snap.getSnapshotFileName()));
				snap.saveSnapshot(zipOut);
			}

		} catch (Exception e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		} finally {
			try {
				zipOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create and write a full snapshot album from scratch
	 */
	private void saveResources(IProgressMonitor monitor) {

		IPath zipPath = getLocation();
		ZipOutputStream zipOut = null;
		try {
			// TODO: Here's we're just rewriting the entire album again
			// Need to just add the resources alone using proper utils
			zipOut = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));

			for (IPath path : files) {

				IAlbumArchiveEntry entry = new IAlbumArchiveEntry() {

					public String createEntryName(File file) {
						StringBuffer entryPath = new StringBuffer();

						entryPath.append("Resources/");

						IPath filepath = new Path(file.getAbsolutePath());

						String deviceName = filepath.getDevice();
						if (deviceName != null) {
							// Remove the : from the end
							entryPath.append(deviceName.substring(0, deviceName.length() - 1));
							entryPath.append("/");
						}
						
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
			saveSnapshotMetadata();

			String xml = LaunchManager.serializeDocument(document);
			zipOut.write(xml.getBytes("UTF8")); //$NON-NLS-1$
			zipOut.closeEntry();

			for (Snapshot snap : snapshotList) {
				zipOut.putNextEntry(new ZipEntry(snap.getSnapshotFileName()));
				snap.saveSnapshot(zipOut);
			}

		} catch (Exception e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		} finally {
			try {
				zipOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		loadAlbum(false);
		DsfSession session = DsfSession.getSession(sessionID);
		if (session != null && snapshotList.size() >= index) {
			Snapshot snapshot = snapshotList.get(index);
			snapshot.open(session);
		}
		fireAlbumStateChanged(this);
		showSnapshotView();
	}

	/**
	 * Zero based index
	 * 
	 * @return current index of snapshot being played
	 */
	public int getCurrentSnapshotIndex() {
		return currentSnapshotIndex;
	}

	public void openNextSnapshot() throws Exception {
		int nextIndex = currentSnapshotIndex + 1;
		if (nextIndex >= snapshotList.size())
			nextIndex = 0;
		openSnapshot(nextIndex);
	}

	public void openPreviousSnapshot() throws Exception {
		int previousIndex = currentSnapshotIndex - 1;
		if (previousIndex < 0)
			previousIndex = snapshotList.size() - 1;
		openSnapshot(previousIndex);
	}

	public void loadAlbum(boolean force) throws ParserConfigurationException, SAXException, IOException {
		if (force)
			loaded = false;
		if (!loaded) {
			File albumFile = location.toFile();
			setName(albumFile.getName());
			
			if (!isRecording()){
				// not creating the snapshot, so must be snapshot play back	
				try {
					ZipFileUtils.unzipFiles(albumFile, getAlbumRootDirectory().toOSString(), new NullProgressMonitor());
				} catch (Exception e) {
					EDCDebugger.getMessageLogger().logError(null, e);
				}
			}
			
			BufferedInputStream stream = ZipFileUtils.openFile(albumFile, ALBUM_DATA, DSA_FILE_EXTENSIONS);
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			document = parser.parse(new InputSource(stream));

			loadAlbumInfo();
			loadLaunchConfiguration();
			loadResourceList();
			try {
				loadSnapshotMetadata();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				loaded = true;
				ZipFileUtils.unmount();
			}
		}
	}

	/**
	 * A lightwieght parse to get basic album info and what snapshots are
	 * available.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void loadAlbumMetada(boolean force) throws Exception {
		if (force)
			metaDataLoaded = false;
		if (!metaDataLoaded) {
			
			File albumFile = location.toFile();
			setDisplayName(albumFile.getName());

			BufferedInputStream stream = null;
			try {
				stream = ZipFileUtils.openFile(albumFile, ALBUM_DATA, DSA_FILE_EXTENSIONS);
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				parser.setErrorHandler(new DefaultHandler());
				document = parser.parse(new InputSource(stream));
				loadSnapshotMetadata();
				loadLaunchConfiguration(); // need to load launch config in case we need to delete it

			} catch (Exception e) {
				EDCDebugger.getMessageLogger().logError("Failed to load album: " + getName(), e);
			} finally {
				metaDataLoaded = true;
				ZipFileUtils.unmount();
			}
		}
	}

	private void loadAlbumInfo() {
		document.getElementsByTagName(INFO).item(0);
	}

	private void loadResourceList() {
		NodeList resources = document.getElementsByTagName(RESOURCES);
		NodeList elementFiles = ((Element) resources.item(0)).getElementsByTagName(FILE);
		int numFiles = elementFiles.getLength();
		for (int i = 0; i < numFiles; i++) {
			Element fileElement = (Element) elementFiles.item(i);
			String elementPath = fileElement.getAttribute("path");
			files.add(PathUtils.createPath(elementPath));		// for cross-created snapshot
		}
	}

	private void loadSnapshotMetadata() throws Exception {
		snapshotList.clear();
		NodeList snapMetaDataNode = document.getElementsByTagName(METADATA);

		if (snapMetaDataNode.getLength() == 0) {
			throw new Exception("Invalid or corrupted Album : " + getName());
		}
		NodeList albumNameElement = ((Element) snapMetaDataNode.item(0)).getElementsByTagName(ALBUM);
		Element albumElement = (Element) albumNameElement.item(0);
		String albumDisplayName = albumElement.getAttribute("albumName");

		setDisplayName(albumDisplayName);

		NodeList elementSnapshots = ((Element) snapMetaDataNode.item(0)).getElementsByTagName(SNAPSHOT);
		int numSnapshots = elementSnapshots.getLength();
		for (int i = 0; i < numSnapshots; i++) {
			Element snapshotElement = (Element) elementSnapshots.item(i);
			String elementDescription = snapshotElement.getAttribute("description");
			String elementDate = snapshotElement.getAttribute("date");
			String elementDispalyName = snapshotElement.getAttribute("displayName");
			String elementFileName = snapshotElement.getAttribute("fileName");

			Snapshot s = new Snapshot(this);
			s.setCreationDate(elementDate);
			s.setSnapshotFileName(elementFileName);
			s.setSnapshotDisplayName(elementDispalyName);
			s.setSnapshotDescription(elementDescription);
			snapshotList.add(s);
		}
	}

	private void loadLaunchConfiguration() {
		NodeList launchElements = document.getElementsByTagName(LAUNCH);
		Element launchElement = (Element) launchElements.item(0);
		if (launchElement == null){
			return;
		}
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

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(Document.class))
			return document;

		return super.getAdapter(adapter);
	}

	/**
	 * Get the location of the album contents, extracted to disk in the workspace.
	 * @return path to the extracted files
	 */
	public IPath getAlbumRootDirectory() {
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

	public static Album getRecordingForSession(String sessionId) {
		return albumsRecordingBySessionID.get(sessionId);
	}

	public void setLocation(IPath albumPath) {
		this.location = albumPath;
		albumsByLocation.put(albumPath, this);
	}

	public IPath getLocation() {
		return location;
	}

	public void configureSourceLookupDirector(CSourceLookupDirector director) {
		MappingSourceContainer sourceContainer = new MappingSourceContainer(getName());
		configureMappingSourceContainer(sourceContainer);
		ArrayList<ISourceContainer> containers = new ArrayList<ISourceContainer>(Arrays.asList(director
				.getSourceContainers()));
		containers.add(sourceContainer);

		DirectorySourceContainer directoryContainer = new DirectorySourceContainer(getResourcesDirectory(), true);
		containers.add(directoryContainer);

		director.setSourceContainers(containers.toArray(new ISourceContainer[containers.size()]));
	}

	protected IPath getResourcesDirectory()
	{
		return getAlbumRootDirectory().append("Resources");
	}
	
	public void configureMappingSourceContainer(MappingSourceContainer mappingContainer) {
		IPath albumRoot = getResourcesDirectory();
		String device = null;
		for (IPath iPath : files) {
			device = iPath.getDevice();
		}
		String deviceName = device;
		if (deviceName != null) {
			if (deviceName.endsWith(":"))
				deviceName = deviceName.substring(0, deviceName.length() - 1);
			albumRoot = albumRoot.append(deviceName);
		}
		MapEntrySourceContainer[] entries = new MapEntrySourceContainer[] { new MapEntrySourceContainer(
				device != null ? new Path(device) : Path.ROOT, albumRoot) };
		mappingContainer.addMapEntries(entries);
	}

	public static void setSnapshotCreationControl(String newSetting) {
		snapshotCreationControl = newSetting;
		new InstanceScope().getNode(EDCDebugger.PLUGIN_ID).put(CREATION_CONTROL, snapshotCreationControl);
	}

	public static String getSnapshotCreationControl() {
		if (snapshotCreationControl == null) {
			snapshotCreationControl = Platform.getPreferencesService().getString(EDCDebugger.PLUGIN_ID,
					CREATION_CONTROL, "manual", null);
		}
		return snapshotCreationControl;
	}

	public static DsfExecutor createSnapshotForSession(final DsfSession session, final String displayName) {
		
		DsfRunnable runner = new DsfRunnable() {
			public void run() {
					String sessionId = session.getId();
					Album album = Album.getRecordingForSession(sessionId);
					if (album == null) {
						album = new Album();
						album.setRecordingSessionID(sessionId);
					}
					playSnapshotSound();
					album.createSnapshot(session, displayName);
					fireAlbumStateChanged(album);
					showSnapshotView();
				}
		};
		
		session.getExecutor().execute(runner);
		
		return session.getExecutor();
	}
	
	protected static void playSnapshotSound() {
		Bundle bundle = Platform.getBundle(EDCDebugger.getUniqueIdentifier());
		if (bundle == null)
			return;
		
		URL url = null;
		try {
			url = FileLocator.toFileURL(bundle.getEntry(CAMERA_CLICK_WAV));
		} catch (IOException e) {
		} catch (RuntimeException e){
		}
		finally {
			if (url != null){
				File f = new File(url.getFile());
				SnapshotUtils.playSoundFile(f);
			}
		}
		
	}
	
	public List<Snapshot> getSnapshots() {
		if (snapshotList == null || snapshotList.size() == 0) {
			try {
				loadAlbumMetada(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return snapshotList;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public int getIndexOfSnapshot(Snapshot snap) {
		return snapshotList.indexOf(snap);
	}

	public void setCurrentSnapshotIndex(int index) {
		if (currentSnapshotIndex >= 0 && currentSnapshotIndex < snapshotList.size()) {
			currentSnapshotIndex = index;
		}
	}

	/**
	 * Update album.xml within the Album's .dsa file with new Snapshot data
	 * 
	 * @param albumName
	 *            - Name of album to display. Use null if value should not be
	 *            updated.
	 * @param snap
	 *            - Specific snapshot to update. Use null is snapshot should not
	 *            be updated.
	 */
	public void updateSnapshotMetaData(String albumName, Snapshot snap) {
		NodeList snapMetaDataNode = document.getElementsByTagName(METADATA);
		
		// try to update album display name
		if (albumName != null) {
			NodeList albumNameNode = ((Element) snapMetaDataNode.item(0)).getElementsByTagName(ALBUM);
			((Element) albumNameNode.item(0)).setAttribute("albumName", albumName);
		}

		// try to update snapshot data
		if (snap != null) {

			NodeList elementSnapshots = ((Element) snapMetaDataNode.item(0)).getElementsByTagName(SNAPSHOT);

			int numSnapshots = elementSnapshots.getLength();
			for (int i = 0; i < numSnapshots; i++) {
				Element currentSnapshotNode = (Element) elementSnapshots.item(i);
				String fileName = currentSnapshotNode.getAttribute("fileName");
				if (fileName.equals(snap.getSnapshotFileName())) {

					currentSnapshotNode.setAttribute("description", snap.getSnapshotDescription());
					currentSnapshotNode.setAttribute("displayName", snap.getSnapshotDisplayName());

					break;
				}
			}
		}

		saveAlbumData();

		// refresh all data
		try {
			loadAlbumMetada(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void saveAlbumData() {
		try {
			File tempFile = File.createTempFile("album", ".xml");
			File tempFile2 = new File(tempFile.getParent() + File.separator + ALBUM_DATA);
			tempFile.delete();
			if (!tempFile2.exists()) {
				tempFile2.delete();
			}
			tempFile2.createNewFile();
			saveAlbum(new Path(tempFile2.toString()));
			File[] fileList = { tempFile2 };
			ZipFileUtils.addFilesToZip(fileList, getLocation().toFile(), DSA_FILE_EXTENSIONS);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delete a given snapshot from an album. On delete, the album data will be
	 * reloaded.
	 * 
	 * @param snap
	 *            Snapshot to delete
	 */
	public void deleteSnapshot(Snapshot snap) {

		NodeList snapMetaDataNode = document.getElementsByTagName(METADATA);

		NodeList elementSnapshotList = ((Element) snapMetaDataNode.item(0)).getElementsByTagName(SNAPSHOT_LIST);
		NodeList elementSnapshots = ((Element) snapMetaDataNode.item(0)).getElementsByTagName(SNAPSHOT);

		int numSnapshots = elementSnapshots.getLength();
		for (int i = 0; i < numSnapshots; i++) {
			Element currentSnapshotNode = (Element) elementSnapshots.item(i);
			String fileName = currentSnapshotNode.getAttribute("fileName");
			if (fileName.equals(snap.getSnapshotFileName())) {
				elementSnapshotList.item(0).removeChild(currentSnapshotNode);
				break;
			}
		}

		snapshotList.remove(snap);

		saveAlbumData();

		// refresh all data
		try {
			loadAlbum(true);
			loadAlbumMetada(true);
		} catch (Exception e) {

		}

		ZipFileUtils.deleteFileFromZip(snap.getSnapshotFileName(), getLocation().toFile(), DSA_FILE_EXTENSIONS);
	}

	@Override
	public String toString() {
		return "Album [name=" + name + ", launchName=" + launchName + ", sessionID=" + sessionID + "]";
	}
	
	public IPath createEmptyAlbum() {
		IPath zipPath = SnapshotUtils.getSnapshotsProject().getLocation();
		zipPath = zipPath.append(getDefaultAlbumName());
		zipPath = zipPath.addFileExtension("dsa");
		boolean created =  ZipFileUtils.createNewZip(zipPath.toFile());
		
		if (created && zipPath.toFile().exists()){
			setLocation(zipPath);
		} else {
			return null;
		}
		
		return zipPath;
	}

	public boolean isRecording() {
		return recordingSessionID.length() > 0;
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void addSnapshotAlbumStateChangedListener(ISnapshotAlbumStateListener listener) {
		synchronized (listener) {
			listeners.add(listener);
		}	
		
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void removeSnapshotAlbumStateChangedListener(ISnapshotAlbumStateListener listener) {
		synchronized (listener) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void fireAlbumStateChanged(Album album) {
		for (ISnapshotAlbumStateListener l : listeners) {
			l.albumChanged(album);
		}
	}

	private static void showSnapshotView() {

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow workbenchWindow = workbench
						.getActiveWorkbenchWindow();
				if (workbenchWindow == null) {
					if (workbench.getWorkbenchWindowCount() == 0)
						return;
					workbenchWindow = workbench.getWorkbenchWindows()[0];
				}
				IWorkbenchPage page = workbenchWindow.getActivePage();
				if (page == null) {
					if (workbenchWindow.getPages().length == 0)
						return;
					page = workbenchWindow.getPages()[0];
				}
				
				try {
					if (page.findView(SNAPSHOT_VIEW_ID) == null){
						page.showView(SNAPSHOT_VIEW_ID);
					}
				} catch (PartInitException e) {
					return;
				}
			}
		});

	}

}
