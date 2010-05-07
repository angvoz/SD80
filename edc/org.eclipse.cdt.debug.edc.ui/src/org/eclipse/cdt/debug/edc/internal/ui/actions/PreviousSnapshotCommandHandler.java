/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui.actions;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.contexts.DebugContextEvent;

public class PreviousSnapshotCommandHandler extends AbstractSnapshotCommandHandler {

	public PreviousSnapshotCommandHandler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			if (isSnapshotSession()) {
				getAlbumContext().openPreviousSnapshot();
			}
			else
			{
				Album album = Album.getRecordingForSession(getSelectionExecutionDMC().getSessionId());
				album.setCurrentSnapshotIndex(album.getSnapshots().size() - 1);
				if (album.getSessionID().length() == 0)
					SnapshotUtils.launchAlbumSession(album);
				else
					album.openSnapshot(album.getSnapshots().size() - 1);
			}
		} catch (Exception e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
	
		return null;
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		super.debugContextChanged(event);
		IAlbum album = getAlbumContext();
		boolean enableit = false;
		if (album != null) {
			enableit = (isSnapshotSession() && album.getCurrentSnapshotIndex() > 0);
			if (!enableit) {
				IExecutionDMContext selectionDMC = getSelectionExecutionDMC();
				enableit = selectionDMC != null && ((Album.getRecordingForSession(getSelectionExecutionDMC().getSessionId()) != null));
			}
		}
		setBaseEnabled(isEnabled() && enableit);
	}

}
