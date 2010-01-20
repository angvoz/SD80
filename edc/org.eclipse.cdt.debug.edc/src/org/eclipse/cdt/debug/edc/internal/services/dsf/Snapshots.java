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

import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.edc.internal.services.dsf.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
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

	/**
	 * Creates the snapshot name from a stack frame dmc.
	 * 
	 * @param frameDMC the frame dmc
	 * 
	 * @return the snapshot name
	 */
	public String createSnapshotNameFromStackFrameDMC(StackFrameDMC frameDMC)
	{
		assert frameDMC != null;
		StringBuilder name = new StringBuilder();
		if (frameDMC.getFunctionName() != null && frameDMC.getFunctionName().length() != 0) {
			name.append(frameDMC.getFunctionName());
			name.append("() : "); //$NON-NLS-1$
			name.append(frameDMC.getLineNumber());
		} else if (frameDMC.getModuleName() != null && frameDMC.getModuleName().length() != 0) {
			name.append(frameDMC.getModuleName());
		} else if (frameDMC.getIPAddress() != null) {
			name.append(frameDMC.getIPAddress().toHexAddressString());
		}

		return name.toString();	
	}
	
	@DsfServiceEventHandler
	public void eventDispatched(final ISuspendedDMEvent e) {
		if (!this.isSnapshot()) {
			final String controlSetting = Album.getSnapshotCreationControl();
			if (!controlSetting.equals("manual")){
				getSession().getExecutor().schedule(new Runnable() {
					public void run() {
						final Stack stackService = getServicesTracker().getService(Stack.class);
						stackService.getTopFrame(e.getDMContext(), new DataRequestMonitor<IFrameDMContext>(getExecutor(), null){

							@Override
							protected void handleCompleted() {
								final StackFrameDMC topFrame = (StackFrameDMC) getData();
								if (e.getReason() != StateChangeReason.SHAREDLIB
										&& (controlSetting.equals("suspend") || controlSetting.equals("breakpoints"))) {
									if (controlSetting.equals("suspend") || 
											e.getReason() == StateChangeReason.BREAKPOINT) {
										Album.createSnapshotForSession(getSession(), createSnapshotNameFromStackFrameDMC(topFrame));
									}
								}
							}
						});

					}}, 500L, TimeUnit.MILLISECONDS);							
			}
		}
	}
}
