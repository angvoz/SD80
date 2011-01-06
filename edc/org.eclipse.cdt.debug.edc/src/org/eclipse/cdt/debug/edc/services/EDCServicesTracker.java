/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * Convenience class to help track DSF services that a given
 * client needs to use.  This class is based on the DsfServicesTracker
 * but is designed to be thread safe so clients can use it to get
 * a service reference from any thread. This is important for EDC
 * services because they are not restricted to the Dsf thread.
 * 
 * @since 2.0
 */
public class EDCServicesTracker {
    
    private static String getServiceFilter(String sessionId) {
        return ("(" + IDsfService.PROP_SESSION_ID + "=" + sessionId + ")").intern();   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    }

    final private static class ServiceKey 
    {
        private final String fClassName;
        private final String fFilter;
        private final int fHashCode;
        private final String fHashString;
        
        
        public ServiceKey(Class<?> clazz, String filter) {
            fClassName = clazz != null ? clazz.getName() : null;
            fFilter = filter;
            fHashString =  'C' + (fClassName == null ? "" : fClassName) + //$NON-NLS-1$
        				   'F' + (fFilter == null ? "" : fFilter); //$NON-NLS-1$
            fHashCode = fHashString.hashCode();
        }
        
        @Override
        public boolean equals(Object other) {
        	// hashcodes are not guaranteed to be unique, but objects that are equal must have the same hashcode
        	// thus we can optimize by first comparing hashcodes
            return other instanceof ServiceKey &&
            	((((ServiceKey)other).fHashCode == this.fHashCode) && (((ServiceKey)other).fHashString.equals(this.fHashString))); 
        }
        
        @Override
        public int hashCode() {
        	return fHashCode;
        }
    }
    
    private final String fSessionId;    
    private volatile boolean fDisposed = false;
    private final BundleContext fBundleContext;

    private final Map<ServiceKey,ServiceReference> fServiceReferences = Collections.synchronizedMap(new HashMap<ServiceKey,ServiceReference>());
    private final Map<ServiceReference,Object> fServices = Collections.synchronizedMap(new HashMap<ServiceReference,Object>());
    private final String fServiceFilter;

    private final ServiceListener fListner = new ServiceListener() {
        public void serviceChanged(final ServiceEvent event) {
            // Only listen to unregister events.
            if (event.getType() != ServiceEvent.UNREGISTERING) {
                return;
            }
            
            // If session is not active anymore, just exit.  The tracker should 
            // soon be disposed.
            DsfSession session = DsfSession.getSession(fSessionId);
            if (session == null) {
                return;
            }
            
            handleUnregisterEvent(event);
        }
    };
    
    private void handleUnregisterEvent(ServiceEvent event) {
    	synchronized (fServiceReferences)
    	{
            for (Iterator<Map.Entry<ServiceKey, ServiceReference>> itr = fServiceReferences.entrySet().iterator(); itr.hasNext();) {
                Map.Entry<ServiceKey, ServiceReference> entry = itr.next();
                if ( entry.getValue().equals(event.getServiceReference()) ) {
                    itr.remove();
                }
            }
            if (fServices.remove(event.getServiceReference()) != null) {
                fBundleContext.ungetService(event.getServiceReference());
            }
    	}
    }
    
    /** 
     * Only constructor.
     * @param bundleContext Context of the plugin that the client lives in. 
     * @param sessionId The DSF session that this tracker will be used for. 
     */
    public EDCServicesTracker(BundleContext bundleContext, String sessionId) {
        fSessionId = sessionId;
        fBundleContext = bundleContext;
        fServiceFilter = getServiceFilter(sessionId); 
        try {
            fBundleContext.addServiceListener(fListner, fServiceFilter);
        } catch (InvalidSyntaxException e) {
            assert false : "Invalid session ID syntax"; //$NON-NLS-1$
        }
    }
    
    /**
     * Retrieves a service reference for given service class and optional filter.  
     * Filter should be used if there are multiple instances of the desired service
     * running within the same session. 
     * @param serviceClass class of the desired service
     * @param custom filter to use when searching for the service, this filter will 
     * be used instead of the standard filter so it should also specify the desired 
     * session-ID 
     * @return OSGI service reference object to the desired service, null if not found
     */
    @SuppressWarnings("rawtypes")
    public ServiceReference getServiceReference(Class serviceClass, String filter) {
        if (fDisposed) {
            return null;
        }
        
        // If the session is not active, all of its services are gone.
        DsfSession session = DsfSession.getSession(fSessionId);
        if (session == null) {
            return null;
        }
        
        ServiceKey key = new ServiceKey(serviceClass, filter != null ? filter : fServiceFilter);
        if (fServiceReferences.containsKey(key)) {
            return fServiceReferences.get(key);
        }
        
        try {
            ServiceReference[] references = fBundleContext.getServiceReferences(key.fClassName, key.fFilter);
            assert references == null || references.length <= 1;
            if (references == null || references.length == 0) {
                return null;
            } else {
                fServiceReferences.put(key, references[0]);
                return references[0];
            }
        } catch(InvalidSyntaxException e) {
            assert false : "Invalid session ID syntax"; //$NON-NLS-1$
        } catch(IllegalStateException e) {
            // Can occur when plugin is shutting down.
        }
        return null;
    }
    
    /**
     * Convenience class to retrieve a service based on class name only.
     * @param serviceClass class of the desired service
     * @return instance of the desired service, null if not found
     */
    public <V> V getService(Class<V> serviceClass) {
        return getService(serviceClass, null);
    }
    
    /** 
     * Retrieves the service given service class and optional filter.
     * Filter should be used if there are multiple instances of the desired service
     * running within the same session. 
     * @param serviceClass class of the desired service
     * @param custom filter to use when searching for the service, this filter will 
     * be used instead of the standard filter so it should also specify the desired 
     * session-ID 
     * @return instance of the desired service, null if not found
     */
    @SuppressWarnings("unchecked")
    public <V> V getService(Class<V> serviceClass, String filter) {
        ServiceReference serviceRef = getServiceReference(serviceClass, filter);
        if (serviceRef == null) {
            return null;
        } else {
            if (fServices.containsKey(serviceRef)) {
                return (V)fServices.get(serviceRef);
            } else {
                V service = (V)fBundleContext.getService(serviceRef);
                fServices.put(serviceRef, service);
                return service;
            }
        }
    }
    
    /**
     * Un-gets all the references held by this tracker.  Must be called
     * to avoid leaking OSGI service references.
     */
    public void dispose() {
        assert !fDisposed;
        fDisposed = true;
        doDispose();
    }

    private void doDispose() {
    	synchronized (fServices)
    	{
            try {
                fBundleContext.removeServiceListener(fListner);
                for (Iterator<ServiceReference> itr = fServices.keySet().iterator(); itr.hasNext();) {
                    fBundleContext.ungetService(itr.next());
                }
            } catch (IllegalStateException e) {
                // May be thrown during shutdown (bug 293049).
            }
    	}
        fServices.clear();
        fServiceReferences.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        assert fDisposed;
        super.finalize();
    }
}
