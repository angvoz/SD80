/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.cdi.ExitInfo;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SignalExitInfo;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorSignalExitEvent;

/**
 */
public class ExitedEvent implements ICDIExitedEvent {

	MIEvent event;
	Session session;
	
	public ExitedEvent(Session s, MIInferiorExitEvent e) {
		session = s;
		event = e;
	}

	public ExitedEvent(Session s, MIInferiorSignalExitEvent e) {
		session = s;
		event = e;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent#getExitInfo()
	 */
	public ICDISessionObject getReason() {
		if (event instanceof MIInferiorExitEvent) {
			return new ExitInfo(session, (MIInferiorExitEvent)event);
		} else if (event instanceof MIInferiorSignalExitEvent) {
			return new SignalExitInfo(session, (MIInferiorSignalExitEvent)event);
		}
		return session;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return session.getCurrentTarget();
	}

}
