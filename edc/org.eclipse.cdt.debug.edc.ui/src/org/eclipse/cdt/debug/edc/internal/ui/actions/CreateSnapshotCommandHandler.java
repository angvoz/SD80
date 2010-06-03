/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui.actions;

import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.contexts.DebugContextEvent;

public class CreateSnapshotCommandHandler extends AbstractSnapshotCommandHandler {

	public CreateSnapshotCommandHandler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {

		final DsfSession session = DsfSession
				.getSession(getSelectionExecutionDMC().getSessionId());
		Album.captureSnapshotForSession(session, getSelectionExecutionDMC());
		return null;
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		super.debugContextChanged(event);
		setBaseEnabled(isEnabled() && !isSnapshotSession());
	}

}
