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
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.ZipFileUtils;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.snapshot.ISnapshotContributor;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.core.LaunchManager;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

@SuppressWarnings("restriction")
public class Snapshot extends PlatformObject {
	
	// XML elements
	public static final String SNAPSHOT = "snapshot";
	
	public static final String SNAPSHOT_FILENAME_PREFIX = "snapshot_";
	
	private Document document;
	private Element snapshotRootElement;
	private DsfSession session;
	private Album album;
	private String snapshotFileName;
	private String snapshotDisplayName;

	private String creationDate;

	private String snapshotDescription;
	
	// Reference location information: when a snapshot is created
	// we record the location in the most recently suspended stack frame.
	// This is then used to create a default name for the snapshot and
	// is displayed in the snapshot view.
	// Of course, when debugging multiple contexts this does not
	// provide a complete description of the snapshot.
	
	private String referenceLocationSourceFile = "";
	private long referenceLocationLineNumber;
	
	/*
	 * Create a snapshot for reading
	 */
	public Snapshot(Album album){
		try {
			this.album = album;
			document = DebugPlugin.newDocument();
			snapshotRootElement = document.createElement(SNAPSHOT);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a snapshot with prep for writing to file.
	 * @param album
	 * @param recentStackFrame - 
	 */
	public Snapshot(Album album, DsfSession session, StackFrameDMC recentStackFrame){
		try {
			assert session != null;
			
			this.album = album;
			this.session = session;
			document = DebugPlugin.newDocument();
			snapshotRootElement = document.createElement(SNAPSHOT);
			document.appendChild(snapshotRootElement);
			
			if (recentStackFrame == null){
				snapshotDisplayName = snapshotFileName;
			} else {
				snapshotDisplayName = createSnapshotNameFromStackFrameDMC(recentStackFrame);
			}
			snapshotFileName = SNAPSHOT_FILENAME_PREFIX + System.currentTimeMillis() + ".xml";
			creationDate = new Date(System.currentTimeMillis()).toString();
			
			if (recentStackFrame != null){
				File f = new File(recentStackFrame.getSourceFile());
				if (f != null){
					setReferenceLocationSourceFile(f.getName());
				} 
				setReferenceLocationLineNumber(recentStackFrame.getLineNumber());
			}
			
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(Document.class))
			return document;

		return super.getAdapter(adapter);
	}
	
	private static String getServiceFilter(String sessionId) {
		return ("(" + IDsfService.PROP_SESSION_ID + "=" + sessionId + ")").intern(); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

	public void open(DsfSession session) {
		ServiceReference[] references;
		BufferedInputStream stream = null;
		try {
			
			stream = ZipFileUtils.openFile(album.getLocation().toFile(), snapshotFileName, new String[] {"dsa"} );
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			
			document = parser.parse(new InputSource(stream));
			NodeList snapNode = document.getElementsByTagName(SNAPSHOT);
			Element snapShotE = (Element)snapNode.item(0);
			
			references = EDCDebugger.getBundleContext().getServiceReferences(ISnapshotContributor.class.getName(),
					getServiceFilter(session.getId()));
			for (ServiceReference serviceReference : references) {
				ISnapshotContributor sc = (ISnapshotContributor) EDCDebugger.getBundleContext().getService(
						serviceReference);
				sc.loadSnapshot(snapShotE);
			}
			
		} catch (Exception e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		} finally {
			ZipFileUtils.unmount();
		}
	}
	
	public void writeSnapshotData(IProgressMonitor monitor){
		try {
			ServiceReference[] references = EDCDebugger.getBundleContext().getServiceReferences(
					ISnapshotContributor.class.getName(), getServiceFilter(session.getId()));
			for (ServiceReference serviceReference : references) {
				if (monitor.isCanceled())
					break;
				ISnapshotContributor sc = (ISnapshotContributor) EDCDebugger.getBundleContext().getService(
						serviceReference);
				Element serviceElement = sc.takeShapshot(album, document, monitor);
				if (serviceElement != null)
					snapshotRootElement.appendChild(serviceElement);
			}
		} catch (InvalidSyntaxException e) {
			EDCDebugger.getMessageLogger().logError("Invalid session ID syntax", e); //$NON-NLS-1$
		} catch (IllegalStateException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
	}
	
	public String getSnapshotFileName(){
		return snapshotFileName;
	}
	
	public void setSnapshotFileName(String snapshotFileName){
		this.snapshotFileName = snapshotFileName;
	}
	
	public void saveSnapshot(ZipOutputStream zipOut) throws TransformerException, IOException {
		String xml = LaunchManager.serializeDocument(document);
		zipOut.write(xml.getBytes("UTF8")); //$NON-NLS-1$
		zipOut.closeEntry();
	}
	
	public String getCreationDate(){
		return creationDate;
	}
	
	public void setCreationDate(String date){
		this.creationDate = date;
	}

	/**
	 * Set the display text for the snapshot
	 * @param snapshotDisplayName
	 */
	public void setSnapshotDisplayName(String snapshotDisplayName) {
		this.snapshotDisplayName = snapshotDisplayName;
	}

	/**
	 * Get the display name of the snapshot. If there is no display name, the XML file containing
	 * the snapshot data in the DSA archive will be used.
	 * @return
	 */
	public String getSnapshotDisplayName() {
		if (snapshotDisplayName == null || snapshotDisplayName.length() == 0){
			snapshotDisplayName = snapshotFileName;
		}
		return snapshotDisplayName;
	}
	
	/**
	 * Additional arbitrary notes to describe a particular snapshot
	 * @return
	 */
	public String getSnapshotDescription() {
		if (snapshotDescription == null){
			snapshotDescription = "";
		}
		return snapshotDescription;
	}
	
	/**
	 * Set the snapshot description text.
	 * @param descr
	 */
	public void setSnapshotDescription(String descr) {
		snapshotDescription = descr;
	}
	
	/**
	 * Get the album this snapshot belongs to
	 * @return
	 */
	public Album getAlbum(){
		return album;
	}
	
	/**
	 * Creates the snapshot name from a stack frame dmc.
	 * 
	 * @param frameDMC the frame dmc
	 * 
	 * @return the snapshot name
	 */
	public String createSnapshotNameFromStackFrameDMC(StackFrameDMC stackFrame)
	{
		assert stackFrame != null;
		StringBuilder name = new StringBuilder();
		if (stackFrame.getFunctionName() != null && stackFrame.getFunctionName().length() != 0) {
			name.append(stackFrame.getFunctionName());
			name.append("() : "); //$NON-NLS-1$
			name.append(stackFrame.getLineNumber());
		} else if (stackFrame.getModuleName() != null && stackFrame.getModuleName().length() != 0) {
			name.append(stackFrame.getModuleName());
		} else if (stackFrame.getIPAddress() != null) {
			name.append(stackFrame.getIPAddress().toHexAddressString());
		}

		return name.toString();	
	}

	public void setReferenceLocationSourceFile(String referenceLocationSourceFile) {
		assert referenceLocationSourceFile != null;
		this.referenceLocationSourceFile = referenceLocationSourceFile;
	}

	public String getReferenceLocationSourceFile() {
		assert referenceLocationSourceFile != null;
		return referenceLocationSourceFile;
	}

	public void setReferenceLocationLineNumber(long referenceLocationLineNumber) {
		this.referenceLocationLineNumber = referenceLocationLineNumber;
	}

	public long getReferenceLocationLineNumber() {
		return referenceLocationLineNumber;
	}
	
}
