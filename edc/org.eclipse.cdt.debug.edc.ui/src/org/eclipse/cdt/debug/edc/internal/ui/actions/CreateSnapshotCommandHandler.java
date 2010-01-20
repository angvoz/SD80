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
import org.eclipse.cdt.debug.edc.internal.services.dsf.Snapshots;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Stack;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.contexts.DebugContextEvent;

public class CreateSnapshotCommandHandler extends AbstractSnapshotCommandHandler {

	public CreateSnapshotCommandHandler() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {

		final DsfSession session = DsfSession.getSession(getSelectionExecutionDMC().getSessionId());

		DsfRunnable runner = new DsfRunnable() {
			public void run() {
				final DsfServicesTracker tracker = new DsfServicesTracker(EDCDebugger.getBundleContext(),
						getSelectionExecutionDMC().getSessionId());
				final Stack stackService = tracker.getService(Stack.class);
				stackService.getTopFrame(getSelectionExecutionDMC(), new DataRequestMonitor<IFrameDMContext>(
						stackService.getExecutor(), null) {

					@Override
					protected void handleCompleted() {
						StackFrameDMC topFrame = (StackFrameDMC) getData();
						final Snapshots snapshotsService = tracker.getService(Snapshots.class);
						Album.createSnapshotForSession(session, snapshotsService
								.createSnapshotNameFromStackFrameDMC(topFrame));
					}
				}

				);
			}
		};

		session.getExecutor().execute(runner);

		return null;
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		super.debugContextChanged(event);
		setBaseEnabled(isEnabled() && !isSnapshotSession());
	}

}
