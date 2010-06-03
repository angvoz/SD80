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
package org.eclipse.cdt.debug.edc.internal.snapshot;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.launch.AbstractFinalLaunchSequence;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class SnapshotLaunchSequence extends AbstractFinalLaunchSequence {

	private final Step[] steps = new Step[] {

	trackerStep,

	new Step() {

		@Override
		public void execute(final RequestMonitor requestMonitor) {
			try {
				int snapIndex = launch.getAlbum().getCurrentSnapshotIndex();
				launch.getAlbum().openSnapshot(snapIndex);
				requestMonitor.done();
			} catch (Exception e) {
				requestMonitor.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID,
						"Failed to open snapshot. Reason: " + e.getLocalizedMessage()));
			}
		}

	}

	};

	public SnapshotLaunchSequence(DsfExecutor executor, EDCLaunch launch, IProgressMonitor pm) {
		super(executor, launch, pm, "Configuring Snapshot Launch", "Aborting configuring Snapshot Launch");
	}

	@Override
	public Step[] getSteps() {
		return steps;
	}

	@Override
	protected void specifyRequiredPeer() {
		// No TCF agent needed.
	}

}
