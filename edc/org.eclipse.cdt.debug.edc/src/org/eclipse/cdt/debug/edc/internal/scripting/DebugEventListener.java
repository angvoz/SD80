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
package org.eclipse.cdt.debug.edc.internal.scripting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionStartedListener;

public class DebugEventListener implements SessionStartedListener {

	private Map<String, List<Map<String,String>>> clientEvents = Collections.synchronizedMap(new HashMap<String, List<Map<String,String>>>());

	public class SessionListener {
		
		private String sessionID;

		public SessionListener(DsfSession session) {
			super();
			this.sessionID = session.getId();
			sessionListeners.put(sessionID, this);
			
			Map<String,String> eventProps = new HashMap<String,String>();
			eventProps.put("event", "sessionStarted");
			eventProps.put("id", session.getId());
			postEvent(eventProps);

			session.addServiceEventListener(this, null);
		}

		public String getSessionID() {
			return sessionID;
		}

		public void postEvent(Map<String,String> eventProps)
		{
			eventProps.put("session", getSessionID());
			synchronized (clientEvents)
			{
				Collection<List<Map<String, String>>> eventLists = clientEvents.values();
				for (List<Map<String, String>> list : eventLists) {
					synchronized (list)
					{
						list.add(eventProps);
						list.notifyAll();
					}
				}
			}
		}
		
		@DsfServiceEventHandler
		public void eventDispatched(ISuspendedDMEvent e) {
			Map<String,String> eventProps = new HashMap<String,String>();
			eventProps.put("event", "contextSuspended");
			eventProps.put("context", ((IEDCDMContext)e.getDMContext()).getID());
			postEvent(eventProps);
		}

	}

	private Map<String, SessionListener> sessionListeners = Collections.synchronizedMap(new HashMap<String,SessionListener>());
	
	static private DebugEventListener listener;
	
	public static DebugEventListener getListener() {
		if (listener == null)
			listener = new DebugEventListener();
		return listener;
	}

	public DebugEventListener() {
		super();
		DsfSession.addSessionStartedListener(this);
	}
	
	public void shutDown() {
		DsfSession.removeSessionStartedListener(this);
		listener = null;
	}

	public void sessionStarted(DsfSession session) {
		new SessionListener(session);
	}
	
	public Map<String, Object> listenForEvents(String clientID) throws InterruptedException{
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("clientID", clientID);
		List<Map<String,String>> events = null;
		synchronized (clientEvents)
		{
			events = clientEvents.get(clientID);
			if (events == null)
			{
				events = new ArrayList<Map<String,String>>();
				clientEvents.put(clientID, events);    
			}
		}
		synchronized (events)
		{
			while (events.size() == 0)
				events.wait();
			result.put("events", new ArrayList<Map<String,String>>(events));
			events.clear();
		}
		return result;
	}


}
