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
package org.eclipse.cdt.debug.edc.internal.ui.actions;

import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.contexts.DebugContextEvent;

public class NextSnapshotCommandHandler extends AbstractSnapshotCommandHandler {

	public NextSnapshotCommandHandler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		final DsfSession session = DsfSession.getSession(getSelectionExecutionDMC().getSessionId());

		session.getExecutor().execute(new DsfRunnable() {
			public void run() {
				if (isSnapshotSession()) {
					try {
						getAlbumContext().openNextSnapshot();
					} catch (Exception e) {
						EDCDebugger.getMessageLogger().logError(null, e);
					}
				}
			}
		});

		return null;
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		super.debugContextChanged(event);
		
		Album album = getAlbumContext();
		if (album != null)
		{
			int snapshotIndex = album.getCurrentSnapshotIndex();
			int numSnapshots = album.getSnapshots().size();
			// TODO: Rather than disable should we wrap when at the end?
			setBaseEnabled(isEnabled() && isSnapshotSession() && snapshotIndex < numSnapshots-1);
		}
		else
			setBaseEnabled(false);
	}

}
