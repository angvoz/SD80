/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointHit;
import org.eclipse.cdt.debug.mi.core.cdi.EndSteppingRange;
import org.eclipse.cdt.debug.mi.core.cdi.ErrorInfo;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryEvent;
import org.eclipse.cdt.debug.mi.core.cdi.SignalReceived;
import org.eclipse.cdt.debug.mi.core.cdi.WatchpointScope;
import org.eclipse.cdt.debug.mi.core.cdi.WatchpointTrigger;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointHitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIErrorEvent;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIFunctionFinishedEvent;
import org.eclipse.cdt.debug.mi.core.event.MILocationReachedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibEvent;
import org.eclipse.cdt.debug.mi.core.event.MISignalEvent;
import org.eclipse.cdt.debug.mi.core.event.MISteppingRangeEvent;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointScopeEvent;
import org.eclipse.cdt.debug.mi.core.event.MIWatchpointTriggerEvent;

/**
 *
 */
public class SuspendedEvent implements ICDISuspendedEvent {

	MIEvent event;
	Session session;

	public SuspendedEvent(Session s, MIEvent e) {
		session = s;
		event = e;
	}

	public ICDISessionObject getReason() {
		if (event instanceof MIBreakpointHitEvent) {
			return new BreakpointHit(session, (MIBreakpointHitEvent)event);
		} else if (event instanceof MIWatchpointTriggerEvent) {
			return new WatchpointTrigger(session, (MIWatchpointTriggerEvent)event);
		} else if (event instanceof MIWatchpointScopeEvent) {
			return new WatchpointScope(session, (MIWatchpointScopeEvent)event);
		} else if (event instanceof MISteppingRangeEvent) {
			return new EndSteppingRange(session);
		} else if (event instanceof MISignalEvent) {
			return new SignalReceived(session, (MISignalEvent)event);
		} else if (event instanceof MILocationReachedEvent) {
			return new EndSteppingRange(session);
		} else if (event instanceof MIFunctionFinishedEvent) {
			return new EndSteppingRange(session);
		} else if (event instanceof MIErrorEvent) {
			return new ErrorInfo(session, (MIErrorEvent)event);
		} else if (event instanceof MISharedLibEvent) {
			return new SharedLibraryEvent(session);
		}
		return session;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		ICDITarget target = session.getCurrentTarget();
		// We can send the target as the Source.  CDI
		// Will assume that all threads are supended for this.
		// This is true for gdb when it suspend the inferior
		// all threads are suspended.
		return target;
	}
}
