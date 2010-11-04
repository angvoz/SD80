package org.eclipse.cdt.debug.edc.acpm;

import java.util.List;

import org.eclipse.cdt.dsf.concurrent.ImmediateInDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RangeCache;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.core.runtime.IStatus;

/**
 * Base EDC class for ACPM range-based caches. See {@link RangeCache}. 
 * @since 2.0
 */
public abstract class BaseRangeCache<V> extends RangeCache<V> implements ICacheEntry {

	/** See {@link #getService()} */
	protected final IDsfService fService;
	
	/** See {@link #getContext()} */
	protected final IDMContext fCtx;
	
	/** See {@link #getMetaData()} */
	private Object fMetaData;

	/**
	 * Constructor
	 * 
	 * @param service
	 *            The DSF service that will provide the data
	 * @param ctx
	 *            The primary context the data is for
	 */
	public BaseRangeCache(IDsfService service, IDMContext ctx) {
		super(new ImmediateInDsfExecutor(service.getExecutor()));
		fService = service;
		fCtx = ctx;
	}
	
	/** Returns the DSF service that provides the data */
	public IDsfService getService() {
		return fService;
	}

	/**
	 * Returns the primary context the data is for. Additional qualifications
	 * may be held by derivative classes
	 */
	public IDMContext getContext() {
		return fCtx;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.acmp.ICacheEntry#setMetaData(java.lang.Object)
	 */
	public void setMetaData(Object data) {
		fMetaData = data;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.acmp.ICacheEntry#getMetaData()
	 */
	public Object getMetaData() {
		return fMetaData;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.concurrent.AbstractCache#reset()
	 */
	protected void reset() {
		super.reset();

		// Once we're invalid, there's no point in listening for events; we
		// don't support going back to the valid state via a notification--too
		// much work. A fresh asynchronous query is the only way to get back to
		// the valid state
		fService.getSession().removeServiceEventListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.concurrent.RequestCache#set(java.lang.Object, org.eclipse.core.runtime.IStatus)
	 */
	protected void set(long offset, int count, List<V> data, IStatus status) {
		super.set(offset, count, data, status);
		
		// Now that we are valid, listen for debug events to find out when we
		// need to move ourselves to the invalid state
	    fService.getSession().addServiceEventListener(this, null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.acmp.BaseRequestCache#dispose()
	 */
	public void dispose() {
		reset();
	}
	
	@DsfServiceEventHandler
	public void resumedEventHandler(IDMEvent<?> e) {
		// We are extremely sensitive. *Any* debug event will cause us to lose
		// confidence in our data. We should be more discriminating and react to
		// specific events that we know may affect the data. E.g., there is no
		// need to invalidate ourselves if a breakpoint creation event occurs.
		reset();
	}

	public RequestMonitor getRequestMonitor() {
		return null; // TODO: how to handle this???
	}
}
