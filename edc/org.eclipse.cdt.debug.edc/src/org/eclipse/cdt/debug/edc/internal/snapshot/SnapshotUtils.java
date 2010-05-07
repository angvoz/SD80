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

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.launch.IEDCLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SnapshotUtils extends PlatformObject {

	/**
	 * Constants for XML element names and attributes
	 */
	public static final String PROPERTIES = "properties"; //$NON-NLS-1$
	private static final String KEY = "key"; //$NON-NLS-1$
	private static final String VALUE = "value"; //$NON-NLS-1$
	private static final String SET_ENTRY = "setEntry"; //$NON-NLS-1$
	private static final String MAP_ENTRY = "mapEntry"; //$NON-NLS-1$
	private static final String LIST_ENTRY = "listEntry"; //$NON-NLS-1$
	private static final String SET_ATTRIBUTE = "setAttribute"; //$NON-NLS-1$
	private static final String MAP_ATTRIBUTE = "mapAttribute"; //$NON-NLS-1$
	private static final String LIST_ATTRIBUTE = "listAttribute"; //$NON-NLS-1$
	private static final String BOOLEAN_ATTRIBUTE = "booleanAttribute"; //$NON-NLS-1$
	private static final String INT_ATTRIBUTE = "intAttribute"; //$NON-NLS-1$
	private static final String LONG_ATTRIBUTE = "longAttribute"; //$NON-NLS-1$
	private static final String BIG_INTEGER_ATTRIBUTE = "bigIntegerAttribute"; //$NON-NLS-1$
	private static final String STRING_ATTRIBUTE = "stringAttribute"; //$NON-NLS-1$

	@SuppressWarnings("unchecked")
	static public Element makeXMLFromProperties(Document doc, Map<String, Object> properties) {

		Element rootElement = doc.createElement(PROPERTIES);
		Iterator<String> keys = properties.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key != null) {
				Object value = properties.get(key);
				if (value == null) {
					continue;
				}
				Element element = null;
				String valueString = null;
				if (value instanceof String) {
					valueString = (String) value;
					element = createKeyValueElement(doc, STRING_ATTRIBUTE, key, valueString);
				} else if (value instanceof Integer) {
					valueString = ((Integer) value).toString();
					element = createKeyValueElement(doc, INT_ATTRIBUTE, key, valueString);
				} else if (value instanceof Long) {
					valueString = ((Long) value).toString();
					element = createKeyValueElement(doc, LONG_ATTRIBUTE, key, valueString);
				} else if (value instanceof BigInteger) {
					valueString = ((BigInteger) value).toString();
					element = createKeyValueElement(doc, BIG_INTEGER_ATTRIBUTE, key, valueString);
				} else if (value instanceof Boolean) {
					valueString = ((Boolean) value).toString();
					element = createKeyValueElement(doc, BOOLEAN_ATTRIBUTE, key, valueString);
				} else if (value instanceof List<?>) {
					element = createListElement(doc, LIST_ATTRIBUTE, key, (List<Object>) value);
				} else if (value instanceof Map<?,?>) {
					element = createMapElement(doc, MAP_ATTRIBUTE, key, (Map<Object, Object>) value);
				} else if (value instanceof Set<?>) {
					element = createSetElement(doc, SET_ATTRIBUTE, key, (Set<Object>) value);
				} else {
					EDCDebugger.getMessageLogger().logError("Unsupported data type " + value.getClass(), null);
					continue;
				}
				rootElement.appendChild(element);
			}
		}
		return rootElement;
	}

	/**
	 * Helper method that creates a 'key value' element of the specified type
	 * with the specified attribute values.
	 */
	static protected Element createKeyValueElement(Document doc, String elementType, String key, String value) {
		Element element = doc.createElement(elementType);
		element.setAttribute(KEY, key);
		element.setAttribute(VALUE, value);
		return element;
	}

	/**
	 * Creates a new <code>Element</code> for the specified
	 * <code>java.util.List</code>
	 * 
	 * @param doc
	 *            the doc to add the element to
	 * @param elementType
	 *            the type of the element
	 * @param setKey
	 *            the key for the element
	 * @param list
	 *            the list to fill the new element with
	 * @return the new element
	 */
	static protected Element createListElement(Document doc, String elementType, String listKey, List<Object> list) {
		Element listElement = doc.createElement(elementType);
		listElement.setAttribute(KEY, listKey);
		Iterator<Object> iterator = list.iterator();
		while (iterator.hasNext()) {
			String value = (String) iterator.next();
			Element element = doc.createElement(LIST_ENTRY);
			element.setAttribute(VALUE, value);
			listElement.appendChild(element);
		}
		return listElement;
	}

	/**
	 * Creates a new <code>Element</code> for the specified
	 * <code>java.util.Set</code>
	 * 
	 * @param doc
	 *            the doc to add the element to
	 * @param elementType
	 *            the type of the element
	 * @param setKey
	 *            the key for the element
	 * @param set
	 *            the set to fill the new element with
	 * @return the new element
	 * 
	 * @since 3.3
	 */
	static protected Element createSetElement(Document doc, String elementType, String setKey, Set<Object> set) {
		Element setElement = doc.createElement(elementType);
		setElement.setAttribute(KEY, setKey);
		Element element = null;
		for (Object object : set) {
			element = doc.createElement(SET_ENTRY);
			element.setAttribute(VALUE, (String) object);
			setElement.appendChild(element);
		}
		return setElement;
	}

	/**
	 * Creates a new <code>Element</code> for the specified
	 * <code>java.util.Map</code>
	 * <p>
	 * NOTE: this creates a <String, String> map from your map -- your ISnapshot#loadSnapshot() implementation
	 * must recover the right types.
	 * @param doc
	 *            the doc to add the element to
	 * @param elementType
	 *            the type of the element
	 * @param setKey
	 *            the key for the element
	 * @param map
	 *            the map to fill the new element with
	 * @return the new element
	 * 
	 */
	static protected Element createMapElement(Document doc, String elementType, String mapKey, Map<Object, Object> map) {
		Element mapElement = doc.createElement(elementType);
		mapElement.setAttribute(KEY, mapKey);
		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			String key = entry.getKey().toString();
			String value = entry.getValue() != null ? entry.getValue().toString() : null;
			Element element = doc.createElement(MAP_ENTRY);
			element.setAttribute(KEY, key);
			element.setAttribute(VALUE, value);
			mapElement.appendChild(element);
		}
		return mapElement;
	}

	/**
	 * Initializes the mapping of attributes from the XML file
	 * 
	 * @param root
	 *            the root node from the XML document
	 * @throws CoreException
	 */
	static public void initializeFromXML(Element root, Map<String, Object> properties) throws CoreException {
		if (root.getNodeName().equalsIgnoreCase(PROPERTIES)) {
			NodeList list = root.getChildNodes();
			Node node = null;
			Element element = null;
			String nodeName = null;
			for (int i = 0; i < list.getLength(); ++i) {
				node = list.item(i);
				short nodeType = node.getNodeType();
				if (nodeType == Node.ELEMENT_NODE) {
					element = (Element) node;
					nodeName = element.getNodeName();
					try {
						if (nodeName.equalsIgnoreCase(STRING_ATTRIBUTE)) {
							setStringAttribute(element, properties);
						} else if (nodeName.equalsIgnoreCase(INT_ATTRIBUTE)) {
							setIntegerAttribute(element, properties);
						} else if (nodeName.equalsIgnoreCase(LONG_ATTRIBUTE)) {
							setLongAttribute(element, properties);
						} else if (nodeName.equalsIgnoreCase(BIG_INTEGER_ATTRIBUTE)) {
							setBigIntegerAttribute(element, properties);
						} else if (nodeName.equalsIgnoreCase(BOOLEAN_ATTRIBUTE)) {
							setBooleanAttribute(element, properties);
						} else if (nodeName.equalsIgnoreCase(LIST_ATTRIBUTE)) {
							setListAttribute(element, properties);
						} else if (nodeName.equalsIgnoreCase(MAP_ATTRIBUTE)) {
							setMapAttribute(element, properties);
						} else if (nodeName.equalsIgnoreCase(SET_ATTRIBUTE)) {
							setSetAttribute(element, properties);
						} else {
							EDCDebugger.getMessageLogger().logError("Unsupported element: " + nodeName, null);
						}
					} catch (Exception e) {
						// Some integers are longs and so will fail when adding 
						// their properties to the launch configuration. LaunchConfigurationInfo 
						// does not support any number but Integer (no BigInteger or Long)
						// See org.eclipse.debug.internal.core.LaunchConfigurationInfo#getAsXML().
						EDCDebugger.getMessageLogger().logError("Skipping snapshot element.",  e);
					} finally {
						// continue on to the next element
					}
				}
			}
		}

	}

	/**
	 * Loads a <code>String</code> from the specified element into the local
	 * attribute mapping
	 * 
	 * @param element
	 *            the element to load from
	 * @throws CoreException
	 */
	static protected void setStringAttribute(Element element, Map<String, Object> properties) throws CoreException {
		properties.put(element.getAttribute(KEY), element.getAttribute(VALUE));
	}

	/**
	 * Loads an <code>Integer</code> from the specified element into the local
	 * attribute mapping
	 * 
	 * @param element
	 *            the element to load from
	 * @throws CoreException
	 */
	static protected void setIntegerAttribute(Element element, Map<String, Object> properties) throws CoreException {
		properties.put(element.getAttribute(KEY), new Integer(element.getAttribute(VALUE)));
	}

	/**
	 * Loads an <code>Long</code> from the specified element into the local
	 * attribute mapping
	 * 
	 * @param element
	 *            the element to load from
	 * @throws CoreException
	 */
	static protected void setLongAttribute(Element element, Map<String, Object> properties) throws CoreException {
		properties.put(element.getAttribute(KEY), new Long(element.getAttribute(VALUE)));
	}

	/**
	 * Loads an <code>BigInteger</code> from the specified element into the local
	 * attribute mapping
	 * 
	 * @param element
	 *            the element to load from
	 * @throws CoreException
	 */
	static protected void setBigIntegerAttribute(Element element, Map<String, Object> properties) throws CoreException {
		properties.put(element.getAttribute(KEY), new BigInteger(element.getAttribute(VALUE)));
	}
	
	/**
	 * Loads a <code>Boolean</code> from the specified element into the local
	 * attribute mapping
	 * 
	 * @param element
	 *            the element to load from
	 * @throws CoreException
	 */
	static protected void setBooleanAttribute(Element element, Map<String, Object> properties) throws CoreException {
		properties.put(element.getAttribute(KEY), Boolean.valueOf(element.getAttribute(VALUE)));
	}

	/**
	 * Reads a <code>List</code> attribute from the specified XML node and loads
	 * it into the mapping of attributes
	 * 
	 * @param element
	 *            the element to read the list attribute from
	 * @throws CoreException
	 *             if the element has an invalid format
	 */
	static protected void setListAttribute(Element element, Map<String, Object> properties) throws CoreException {
		String listKey = element.getAttribute(KEY);
		NodeList nodeList = element.getChildNodes();
		int entryCount = nodeList.getLength();
		List<Object> list = new ArrayList<Object>(entryCount);
		Node node = null;
		Element selement = null;
		for (int i = 0; i < entryCount; i++) {
			node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				selement = (Element) node;
				if (selement.getNodeName().equalsIgnoreCase(LIST_ENTRY)) {
					String value = selement.getAttribute(VALUE);
					list.add(value);
				}
			}
		}
		properties.put(listKey, list);
	}

	/**
	 * Reads a <code>Set</code> attribute from the specified XML node and loads
	 * it into the mapping of attributes
	 * 
	 * @param element
	 *            the element to read the set attribute from
	 * @throws CoreException
	 *             if the element has an invalid format
	 * 
	 * @since 3.3
	 */
	static protected void setSetAttribute(Element element, Map<String, Object> properties) throws CoreException {
		String setKey = element.getAttribute(KEY);
		NodeList nodeList = element.getChildNodes();
		int entryCount = nodeList.getLength();
		Set<Object> set = new HashSet<Object>(entryCount);
		Node node = null;
		Element selement = null;
		for (int i = 0; i < entryCount; i++) {
			node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				selement = (Element) node;
				if (selement.getNodeName().equalsIgnoreCase(SET_ENTRY)) {
					set.add(element.getAttribute(VALUE));
				}
			}
		}
		properties.put(setKey, set);
	}

	/**
	 * Reads a <code>Map</code> attribute from the specified XML node and loads
	 * it into the mapping of attributes
	 * <p>
	 * NOTE: this creates a <String, String> map -- your ISnapshot#loadSnapshot() implementation
	 * must recover the right types.
	 * 
	 * @param element
	 *            the element to read the map attribute from
	 * @throws CoreException
	 *             if the element has an invalid format
	 */
	static protected void setMapAttribute(Element element, Map<String, Object> properties) throws CoreException {
		String mapKey = element.getAttribute(KEY);
		NodeList nodeList = element.getChildNodes();
		int entryCount = nodeList.getLength();
		Map<Object, Object> map = new HashMap<Object, Object>(entryCount);
		Node node = null;
		Element selement = null;
		for (int i = 0; i < entryCount; i++) {
			node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				selement = (Element) node;
				if (selement.getNodeName().equalsIgnoreCase(MAP_ENTRY)) {
					map.put(selement.getAttribute(KEY), selement.getAttribute(VALUE));
				}
			}
		}
		properties.put(mapKey, map);
	}
	
	static public IProject getSnapshotsProject() {

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
	
	// TODO: This was taken from SnapshotLaunchDelegate. Need to refactor properly to make this common....
	/**
	 * Load an album and launch the session without creating a Snapshot launch configuration. 
	 * Only creates the launch configuration type specified in the album data.
	 */
	static public boolean launchAlbumSession(Album album){
		IPath albumPath = album.getLocation();

		try {
			if (!album.isLoaded()){
				album.loadAlbum(false);
			}
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			return false;
		} catch (SAXException e1) {
			e1.printStackTrace();
			return false;
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchType = lm.getLaunchConfigurationType(album.getLaunchTypeID());
		if (launchType == null) {
			// Can't launch TODO: Need error or exception
			return false;
		}
		ILaunchConfiguration proxyLaunchConfig = findExistingLaunchForAlbum(album);
		if (proxyLaunchConfig == null) {
			String lcName = lm.generateLaunchConfigurationName(album.getDisplayName());
			ILaunchConfigurationWorkingCopy proxyLaunchConfigWC = null;
			try {
				proxyLaunchConfigWC = launchType.newInstance(null, lcName);
				proxyLaunchConfigWC.setAttributes(album.getLaunchProperties());
				proxyLaunchConfigWC.setAttribute(IEDCLaunchConfigurationConstants.ATTR_ALBUM_FILE, albumPath.toOSString());
				proxyLaunchConfig = proxyLaunchConfigWC.doSave();
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		final ILaunchConfiguration finalProxyLC = proxyLaunchConfig;
		Job launchJob = new Job("Launching " + albumPath.toFile().getName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					finalProxyLC.launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor(), false, true);
				} catch (CoreException e) {
					EDCDebugger.getMessageLogger().logError(null, e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		launchJob.schedule();
		return false;
	}
	
	static public ILaunchConfiguration findExistingLaunchForAlbum(IAlbum album) {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchType = lm.getLaunchConfigurationType(album.getLaunchTypeID());
		if (launchType == null){
			return null;
		}
		
		try {
			ILaunchConfiguration[] configurations = lm.getLaunchConfigurations(launchType);
			for (ILaunchConfiguration configuration : configurations) {
				if (album.getLocation().toOSString().equals(configuration.getAttribute(
						IEDCLaunchConfigurationConstants.ATTR_ALBUM_FILE, "")))
					return configuration;
			}
		} catch (CoreException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
		return null;
	}
	
	/**
	 * Taken from org.eclipse.cdt.debug.ui.breakpointactions#SoundAction
	 * @param soundFile
	 */
	static public void playSoundFile(final File soundFile) {

		class SoundPlayer extends Thread {

			public void run() {
				AudioInputStream soundStream;
				try {
					soundStream = AudioSystem.getAudioInputStream(soundFile);
					AudioFormat audioFormat = soundStream.getFormat();
					DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
					SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
					byte[] soundBuffer = new byte[5000];
					sourceDataLine.open(audioFormat);
					sourceDataLine.start();
					int dataCount = 0;

					while ((dataCount = soundStream.read(soundBuffer, 0, soundBuffer.length)) != -1) {
						if (dataCount > 0) {
							sourceDataLine.write(soundBuffer, 0, dataCount);
						}
					}
					sourceDataLine.drain();
					sourceDataLine.close();

				} 
				// Don't report any exceptions, some VMs may not play the sound
				catch (UnsupportedAudioFileException e) {
				} catch (IOException e) {
				} catch (IllegalArgumentException e) {
				} catch (LineUnavailableException e) {
				} finally {
					
				}

			}

		}
		
		if (soundFile.exists()) {
			new SoundPlayer().start();
		}
	}


}
