package org.eclipse.cdt.debug.edc.acpm;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * EDC ACPM cache objects must implement this interface in order to be
 * manageable by an ICacheManager. Some cache manager implementations need to
 * attach meta data to the cache objects in order to track them and decide which
 * ones should be discarded first.
 * 
 * @since 2.0
 */
public interface ICacheEntry {
	/**
	 * Called by a cache manager to attach meta data for tracking purposes
	 * 
	 * @param data
	 *            the meta data
	 */
	void setMetaData(Object data);

	/**
	 * Called by a cache manager to retrieve the data it has previously attached
	 * via {@link #setMetaData(Object)}
	 * 
	 * @return the meta data
	 */
	Object getMetaData();

	/**
	 * Called by a cache manager when it discards a cache object in order to
	 * make room for more. The implementation should clear any data it has been
	 * storing from its source.
	 */
	void dispose();

	/**
	 * Returns the RM the cache object passes to its derivative's retrieval
	 * method. Cache objects that are containers for other cache objects can
	 * return null. They are responsible for managing the lifetime of their own
	 * cache objects. We manage the lifetime of the collection as a whole.
	 */
	RequestMonitor getRequestMonitor();

	
	/**
	 * Returns the primary context the cache object is associated with
	 */
	IDMContext getContext();
}

