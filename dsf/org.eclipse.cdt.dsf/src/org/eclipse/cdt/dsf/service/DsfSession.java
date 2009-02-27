/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Filter;

/**
 * Class to manage DSF sessions.  A DSF session is a way to 
 * associate a set of DSF services that are running simultaneously and 
 * are interacting with each other to provide a complete set of functionality.
 * <p>
 * Properties of a session are following:
 * <br>1. Each session is associated with a single DSF executor, although there
 * could be multiple sessions using the same executor.
 * <br>2. Each session has a unique String identifier, which has to be used by
 * the services belonging to this session when registering with OSGI services.
 * <br>3. Each session has its set of service event listeners.
 * <br>4. Start and end of each session is announced by events, which are always
 * sent on that session's executor dispatch thread.      
 * 
 * @see org.eclipse.cdt.dsf.concurrent.DsfExecutor
 * 
 * @since 1.0
 */
@ConfinedToDsfExecutor("getExecutor") 
public class DsfSession 
{    
    /** 
     * Listener for session started events.  This listener is always going to be
     * called in the dispatch thread of the session's executor.  
     */
    public static interface SessionStartedListener {
        /** 
         * Called when a new session is started.  It is always called in the 
         * dispatch thread of the new session.
         */
        public void sessionStarted(DsfSession session);
    }

    /** 
     * Listener for session ended events.  This listener is always going to be
     * called in the dispatch thread of the session's executor. 
     */
    public static interface SessionEndedListener {
        /** 
         * Called when a session is ended.  It is always called in the 
         * dispatch thread of the session.
         */
        public void sessionEnded(DsfSession session);
    }

    private static int fgSessionIdCounter = 0; 
    private static Set<DsfSession> fgActiveSessions = Collections.synchronizedSet(new HashSet<DsfSession>());
    private static List<SessionStartedListener> fSessionStartedListeners = Collections.synchronizedList(new ArrayList<SessionStartedListener>());
    private static List<SessionEndedListener> fSessionEndedListeners = Collections.synchronizedList(new ArrayList<SessionEndedListener>());
    
    /** Returns true if given session is currently active */
    public static boolean isSessionActive(String sessionId) {
        return getSession(sessionId) != null;
    }
    
    /** Returns a session instance for given session identifier */
    @ThreadSafe
    public static DsfSession getSession(String sessionId) {
        synchronized(fgActiveSessions) {
            for (DsfSession session : fgActiveSessions) {
                if (session.getId().equals(sessionId)) {
                    return session;
                }
            } 
        }
        return null;
    }
    
    /** 
     * Registers a listener for session started events.
     * Can be called on any thread. 
     */
    @ThreadSafe
    public static void addSessionStartedListener(SessionStartedListener listener) {
        assert !fSessionStartedListeners.contains(listener);
        fSessionStartedListeners.add(listener);
    }
    
    /** 
     * Un-registers a listener for session started events. 
     * Can be called on any thread. 
     */ 
    @ThreadSafe
    public static void removeSessionStartedListener(SessionStartedListener listener) {
        assert fSessionStartedListeners.contains(listener);
        fSessionStartedListeners.remove(listener);
    }
    
    /** 
     * Registers a listener for session ended events. 
     * Can be called on any thread. 
     */ 
    @ThreadSafe
    public static void addSessionEndedListener(SessionEndedListener listener) {
        assert !fSessionEndedListeners.contains(listener);
        fSessionEndedListeners.add(listener);
    }

    /** 
     * Un-registers a listener for session ended events. 
     * Can be called on any thread. 
     */ 
    @ThreadSafe
    public static void removeSessionEndedListener(SessionEndedListener listener) {
        assert fSessionEndedListeners.contains(listener);
        fSessionEndedListeners.remove(listener);
    }

    /**
     * Starts and returns a new session instance.  This method can be called on any
     * thread, but the session-started listeners will be called using the session's 
     * executor.
     * @param executor The DSF executor to use for this session.
     * @param ownerId ID (plugin ID preferably) of the owner of this session
     * @return instance object of the new session
     */
    @ThreadSafe
    public static DsfSession startSession(DsfExecutor executor, String ownerId) {
        synchronized(fgActiveSessions) {
            final DsfSession newSession = new DsfSession(executor, ownerId, Integer.toString(fgSessionIdCounter++));
            fgActiveSessions.add(newSession);
            executor.submit( new DsfRunnable() { public void run() {
                SessionStartedListener[] listeners = fSessionStartedListeners.toArray(
                    new SessionStartedListener[fSessionStartedListeners.size()]);
                for (int i = 0; i < listeners.length; i++) {
                    listeners[i].sessionStarted(newSession);
                }
            }});
            return newSession;
        }
    }

    /**
     * Terminates the given session.  This method can be also called on any
     * thread, but the session-ended listeners will be called using the session's 
     * executor.
     * @param session session to terminate
     */
    @ThreadSafe
    public static void endSession(final DsfSession session) {
        synchronized(fgActiveSessions) {
            if (!fgActiveSessions.contains(session)) {
                throw new IllegalArgumentException();
            }
            fgActiveSessions.remove(session);
            session.getExecutor().submit( new DsfRunnable() { public void run() {
                SessionEndedListener[] listeners = fSessionEndedListeners.toArray(
                    new SessionEndedListener[fSessionEndedListeners.size()]);
                for (int i = 0; i < listeners.length; i++) {
                    listeners[i].sessionEnded(session);
                }
            }});
        }
    }

    private static class ListenerEntry {
        Object fListener;
        Filter fFilter;

        ListenerEntry(Object listener, Filter filter) {
            fListener = listener;
            fFilter = filter;
        }
        
        @Override
        public boolean equals(Object other) {
            return other instanceof ListenerEntry && fListener.equals(((ListenerEntry)other).fListener);
        }
        
        @Override
        public int hashCode() { return fListener.hashCode(); }
    }

    /** ID (plugin ID preferably) of the owner of this session */
    private String fOwnerId;
    
    /** Session ID of this session. */
    private String fId;
    
    /** Dispatch-thread executor for this session */
    private DsfExecutor fExecutor;
    
    /** Service start-up counter for this session */ 
    private int fServiceInstanceCounter;
    
    /** Map of registered event listeners. */
    private Map<ListenerEntry,Method[]> fListeners = new HashMap<ListenerEntry,Method[]>();
    
    /** 
     * Map of registered adapters, for implementing the 
     * IModelContext.getAdapter() method.
     * @see org.eclipse.cdt.dsf.datamodel.AbstractDMContext#getAdapter 
     */
    @SuppressWarnings("unchecked") 
    private Map<Class,Object> fAdapters = Collections.synchronizedMap(new HashMap<Class,Object>());

    /** Returns the owner ID of this session */
    public String getOwnerId() { return fOwnerId; }    
    
    public boolean isActive() { return DsfSession.isSessionActive(fId); }
    
    /** Returns the ID of this session */
    public String getId() { return fId; }
    
    /** Returns the DSF executor of this session */
    public DsfExecutor getExecutor() { return fExecutor; }
 
    /**
     * Adds a new listener for service events in this session.
     * @param listener the listener that will receive service events
     * @param filter optional filter to restrict the services that the 
     * listener will receive events from 
     */
    public void addServiceEventListener(Object listener, Filter filter) {
        ListenerEntry entry = new ListenerEntry(listener, filter);
        assert !fListeners.containsKey(entry);
        fListeners.put(entry, getEventHandlerMethods(listener));
    }
    
    /**
     * Removes the given listener.
     * @param listener listener to remove
     */
    public void removeServiceEventListener(Object listener) {
        ListenerEntry entry = new ListenerEntry(listener, null);
        assert fListeners.containsKey(entry);
        fListeners.remove(entry);
    }

    /**
     * Retrieves and increments the startup counter for services in this session.
     * DSF services should retrieve this counter when they are initialized, 
     * and should return it through IService.getStartupNumber().  This number is then
     * used to prioritize service events.
     * @return current startup counter value
     */
    public int getAndIncrementServiceStartupCounter() { return fServiceInstanceCounter++; }
    
    /**
     * Dispatches the given event to service event listeners.  The event is submitted to 
     * the executor to be dispatched.
     * @param event to be sent out
     * @param serviceProperties properties of the service requesting the event to be dispatched
     */
    @ThreadSafe
    @SuppressWarnings("unchecked")
    public void dispatchEvent(final Object event, final Dictionary serviceProperties) {
        getExecutor().submit(new DsfRunnable() { 
            public void run() { doDispatchEvent(event, serviceProperties);}
            @Override
            public String toString() { return "Event: " + event + ", from service " + serviceProperties; }  //$NON-NLS-1$ //$NON-NLS-2$
            });
    }
    
    /**
     * Registers a IModelContext adapter of given type.
     * @param adapterType class type to register the adapter for
     * @param adapter adapter instance to register
     * @see org.eclipse.dsdp.model.AbstractDMContext#getAdapter
     */
    @ThreadSafe
    @SuppressWarnings("unchecked") 
    public void registerModelAdapter(Class adapterType, Object adapter) {
        fAdapters.put(adapterType, adapter);
    }
    
    /**
     * Un-registers a IModelContext adapter of given type.
     * @param adapterType adapter type to unregister
     * @see org.eclipse.dsdp.model.AbstractDMContext#getAdapter
     */
    @ThreadSafe
    @SuppressWarnings("unchecked")
    public void unregisterModelAdapter(Class adapterType) {
        fAdapters.remove(adapterType);
    }
    
    /** 
     * Retrieves an adapter for given type for IModelContext.
     * @param adapterType adapter type to look fors
     * @return adapter object for given type, null if none is registered with the session
     * @see org.eclipse.dsdp.model.AbstractDMContext#getAdapter
     */
    @ThreadSafe
    @SuppressWarnings("unchecked") 
    public Object getModelAdapter(Class adapterType) {
        return fAdapters.get(adapterType);
    }
    
    @Override
    @ThreadSafe
    public boolean equals(Object other) {
        return other instanceof DsfSession && fId.equals(((DsfSession)other).fId);
    }
    
    @Override
    @ThreadSafe
    public int hashCode() { return fId.hashCode(); }

    @SuppressWarnings("unchecked") 
    private void doDispatchEvent(Object event, Dictionary serviceProperties) {
        // Build a list of listeners;
        SortedMap<ListenerEntry,List<Method>> listeners = new TreeMap<ListenerEntry,List<Method>>(new Comparator<ListenerEntry>() {
                public int compare(ListenerEntry o1, ListenerEntry o2) {
                    if (o1.fListener == o2.fListener) {
                        return 0;
                    } if (o1.fListener instanceof IDsfService && !(o2.fListener instanceof IDsfService)) {
                        return Integer.MIN_VALUE;
                    } else if (o2.fListener instanceof IDsfService && !(o1.fListener instanceof IDsfService)) {
                        return Integer.MAX_VALUE;
                    } else if ( (o1.fListener instanceof IDsfService) && (o2.fListener instanceof IDsfService) ) {
                        return ((IDsfService)o1.fListener).getStartupNumber() - ((IDsfService)o2.fListener).getStartupNumber();
                    }
                    return 1;
                }
                
                @Override
                public boolean equals(Object obj) {
                    return obj == this;
                }
            });

        // Build a list of listeners and methods that are registered for this event class.
        Class<?> eventClass = event.getClass();
        for (Map.Entry<ListenerEntry,Method[]> entry : fListeners.entrySet()) {
            if (entry.getKey().fFilter != null && !entry.getKey().fFilter.match(serviceProperties)) {
                // Dispatching service doesn't match the listener's filter, skip it.
                continue;
            }
            Method[] allMethods = entry.getValue();
            List<Method> matchingMethods = new ArrayList<Method>();
            for (Method method : allMethods) {
            	assert method.getParameterTypes().length > 0 : eventClass.getName() + "." + method.getName() //$NON-NLS-1$
            		+ " signature contains zero parameters"; //$NON-NLS-1$
    	    	if ( method.getParameterTypes()[0].isAssignableFrom(eventClass) ) {
                    matchingMethods.add(method);
                }
            }
            if (!matchingMethods.isEmpty()) {
                listeners.put(entry.getKey(), matchingMethods);
            }
        }
        
        // Call the listeners
        for (Map.Entry<ListenerEntry,List<Method>> entry : listeners.entrySet()) {
            for (Method method : entry.getValue()) {
                try {
                    method.invoke(entry.getKey().fListener, new Object[] { event } );
                }
                catch (IllegalAccessException e) {
                    DsfPlugin.getDefault().getLog().log(new Status(
                        IStatus.ERROR, DsfPlugin.PLUGIN_ID, -1, "Security exception when calling a service event handler method", e)); //$NON-NLS-1$
                    assert false : "IServiceEventListener.ServiceHandlerMethod method not accessible, is listener declared public?"; //$NON-NLS-1$
                }
                catch (InvocationTargetException e) {
                    DsfPlugin.getDefault().getLog().log(new Status(
                        IStatus.ERROR, DsfPlugin.PLUGIN_ID, -1, "Invocation exception when calling a service event handler method", e)); //$NON-NLS-1$
                    assert false : "Exception thrown by a IServiceEventListener.ServiceHandlerMethod method"; //$NON-NLS-1$
                }
            }
        }
    }

    private Method[] getEventHandlerMethods(Object listener) 
    {
        List<Method> retVal = new ArrayList<Method>();
        try {
            Method[] methods = listener.getClass().getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(DsfServiceEventHandler.class)) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length > 2) {
                        throw new IllegalArgumentException("ServiceEventHandler method has incorrect number of parameters"); //$NON-NLS-1$
                    } 
                    retVal.add(method);
                }
            }
        } catch(SecurityException e) {
            throw new IllegalArgumentException("No permission to access ServiceEventHandler method"); //$NON-NLS-1$
        }
        
        if (retVal.isEmpty()) {
            throw new IllegalArgumentException("No methods marked with @ServiceEventHandler in listener, is listener declared public?"); //$NON-NLS-1$
        }
        return retVal.toArray(new Method[retVal.size()]);
    }
    
    /**
     * Class to be instanciated only using startSession()
     */
    @ThreadSafe
    private DsfSession(DsfExecutor executor, String ownerId, String id) {
        fId = id;
        fOwnerId = ownerId;
        fExecutor = executor;
    }
    
}