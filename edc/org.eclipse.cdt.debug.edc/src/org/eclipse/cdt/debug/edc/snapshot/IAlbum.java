/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.snapshot;

import java.util.HashMap;
import java.util.List;

import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.edc.internal.snapshot.Snapshot;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;

public interface IAlbum {

	public String getName();

	public String getDisplayName();

	public String getSessionID();

	public String getRecordingSessionID();

	public void openSnapshot(final int index);

	/**
	 * Zero based index
	 * 
	 * @return current index of snapshot being played
	 */
	public int getCurrentSnapshotIndex();

	public void openNextSnapshot() throws Exception;

	public void openPreviousSnapshot() throws Exception;

	/**
	 * Get the location of the album contents, extracted to disk in the workspace.
	 * @return path to the extracted files
	 */
	public IPath getAlbumRootDirectory();

	public String getLaunchTypeID();

	public HashMap<String, Object> getLaunchProperties();

	public String getLaunchName();

	public void playSnapshots(DsfSession session);

	public void addFile(IPath path);

	public IPath getLocation();

	public void configureSourceLookupDirector(ISourceLookupDirector director);

	public void configureMappingSourceContainer(
			MappingSourceContainer mappingContainer);

	public List<Snapshot> getSnapshots();

	public boolean isLoaded();

	public boolean isRecording();

}