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
			session.addServiceEventListener(this, null);
		}

		public String getSessionID() {
			return sessionID;
		}

		@DsfServiceEventHandler
		public void eventDispatched(ISuspendedDMEvent e) {
			Map<String,String> eventProps = new HashMap<String,String>();
			eventProps.put("session", getSessionID());
			eventProps.put("event", "contextSuspended");
			eventProps.put("context", ((IEDCDMContext)e.getDMContext()).getID());
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
	
	public Map<String, Object> listenForEvents(String sessionID, String clientID) throws InterruptedException{
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("sessionID", sessionID);
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
