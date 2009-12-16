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
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;

public class Snapshots extends AbstractEDCService {

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
	public void eventDispatched(ISuspendedDMEvent e) {
		if (!this.isSnapshot()) {
			String controlSetting = Album.getSnapshotCreationControl();
			if (e.getReason() != StateChangeReason.SHAREDLIB
					&& (controlSetting.equals("suspend") || controlSetting.equals("breakpoints"))) {
				if (controlSetting.equals("suspend") || e.getReason() != StateChangeReason.BREAKPOINT) {
					Album.createSnapshotForSession(getSession());
				}
			}

		}
	}
}
