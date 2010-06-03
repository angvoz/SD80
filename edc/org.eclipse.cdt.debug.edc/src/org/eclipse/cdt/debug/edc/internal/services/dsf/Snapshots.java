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
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.debug.edc.services.ISnapshots;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;

public class Snapshots extends AbstractEDCService implements ISnapshots {

	public Snapshots(DsfSession session) {
		super(session, new String[] { Snapshots.class.getName() });
		session.addServiceEventListener(this, null);
	}

	@Override
	public void shutdown(RequestMonitor rm) {
		getSession().removeServiceEventListener(this);
		super.shutdown(rm);
	}
	
	@DsfServiceEventHandler
	public void eventDispatched(final ISuspendedDMEvent e) {
		if (!this.isSnapshot()) {
			final String controlSetting = Album.getSnapshotCreationControl();
			if (!controlSetting.equals(Album.CREATE_MANUAL)){
								if (e.getReason() != StateChangeReason.SHAREDLIB
						&& (controlSetting.equals(Album.CREATE_WHEN_STOPPED) || controlSetting.equals(Album.CREATE_AT_BEAKPOINTS))) {
					if (controlSetting.equals(Album.CREATE_WHEN_STOPPED) || 
											e.getReason() == StateChangeReason.BREAKPOINT) {
						Album.captureSnapshotForSession(getSession(), e.getDMContext());
									}
								}
			}
		}
	}
}
