package org.eclipse.cdt.debug.edc.internal.acpm;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.acpm.AvailableFormatsRequestCache;
import org.eclipse.cdt.debug.edc.acpm.FormatedExpressionValueRequestCache;
import org.eclipse.cdt.debug.edc.acpm.ICacheEntry;
import org.eclipse.cdt.debug.edc.acpm.MemoryRangeCache;
import org.eclipse.cdt.debug.edc.acpm.RegistersByNameRequestCache;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.INoop;
import org.eclipse.cdt.debug.edc.launch.ICacheManager;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.omg.CORBA.FREE_MEM;

/**
 * An implementation of an ACPM cache manager that's based on keeping only a
 * limited number of cache objects around. The lifetime of a cache object is
 * affected by two factors. Firstly, a cache object that is part of an ongoing
 * transaction is never discarded (otherwise we risk preventing the transaction
 * from ever completing). Secondly, least recently accessed cache objects are
 * always discarded before more recently accessed ones. EDC transaction objects
 * must call ICacheManager.beginTransaction() and
 * ICacheManager.endTransaction(boolean) at the outset and end of its attempt to
 * run, respectively. See {@link ICacheManager#beginTransaction()}
 * <p>
 * This cache manager performs cleanup at the end of a transaction if the pool
 * of objects exceeds the limit by 20%. We will discard as many objects as
 * necessary to get back to the limit. However, because we never discard cache
 * objects participating in ongoing transactions, getting back to the limit may
 * not be achievable on any given cleanup attempt.
 */
@ConfinedToDsfExecutor("fDsfSession.getExecutor()")	// any executor
public class CacheManager implements ICacheManager, DsfSession.SessionEndedListener, RequestMonitor.ICanceledListener {

	/** The bookkeeping data we attach to each cache object. */
	class MetaData {
		/** Whether the object was used in the most recent transaction attempt */
		boolean usedInCurrentTransaction;

		/**
		 * Whether the object was part of any uncompleted transaction--i.e., one
		 * in which one or more cache objects were invalid (out of date), thus
		 * requiring another run. Once that transaction completes or is
		 * canceled, this is set to false.
		 */
		boolean inUse;
	}
	
	/** The cache objects used in the current transaction */
	private Set<ICacheEntry> fCachesUsedInThisTransaction = new HashSet<ICacheEntry>();

	/**
	 * A flat collection containing <b>all</b> the active cache objects. This
	 * collection acts as both a queue and a fast lookup hash. The former is
	 * required in order to maintain the cleanup order. Least recently used
	 * cache objects are discarded before more recently used ones. When a cache
	 * object is used, it is removed from the collection then re-added,
	 * effectively repositioning it to the end of the discard queue.
	 */
	private LinkedHashSet<ICacheEntry> fAllCaches = new LinkedHashSet<ICacheEntry>();

	/**
	 * This map allows us to quickly home in on the caches available for a given
	 * context. Each context basically has a bucket of caches. The contents of
	 * this collection must be identical to the contents of fAllCaches. 
	 */
	private Map<IDMContext, List<ICacheEntry>> fCachesByContext = new HashMap<IDMContext, List<ICacheEntry>>();

	/**
	 * We keep a map of RMs to cache objects so that when our RM cancel listener
	 * is called, we know which cache entry to discard
	 */
	private Map<RequestMonitor, ICacheEntry> fMonitorToCache = new HashMap<RequestMonitor, ICacheEntry>();

	/** The DSF session this cache manager serves */
	private final DsfSession fDsfSession;

	/**
	 * The maximum number of active cache objects. This is specified at
	 * construction. This is not a hard limit, but a goal.
	 */
	private final int fMaxCaches;

	/**
	 * Cleanup isn't cheap, so we minimize how often we do it by allowing a
	 * certain amount above the limit before kicking into cleanup, at which time
	 * we discard as many objects as possible to reach the limit. This
	 * represents the number of objects above the limit that will trigger
	 * cleanup.
	 */
	private final int fAllowedOverage; 
	
	/** Tracker we use to access DSF services */
	private DsfServicesTracker fTracker;

	/** True if a transaction is in progress */
	private boolean fInTransaction;
	
	@ThreadSafe
	/** Constructor that uses a default limit of 1000 cache objects */ 
	public CacheManager(DsfSession session) {
		this(session, 1000); 
	}

	@ThreadSafe
	/** Constructor that allows specification of cache object limit */ 
	public CacheManager(DsfSession session, int maxCaches) {
		fDsfSession = session;
		fTracker = new DsfServicesTracker(EDCDebugger.getBundleContext(), fDsfSession.getId());		
		fMaxCaches = maxCaches;
		fAllowedOverage = (int)(fMaxCaches * 0.20f);
		
		DsfSession.addSessionEndedListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener#sessionEnded(org.eclipse.cdt.dsf.service.DsfSession)
	 */
	public void sessionEnded(DsfSession session) {
		if (fDsfSession.equals(session)) {
			purgeAll();
			fTracker.dispose();
			fTracker = null;
			DsfSession.removeSessionEndedListener(this);
		}
	}

	/** Returns the collection of active cache objects for a given context */
	private List<ICacheEntry> getCacheListForContext(IDMContext dmc) {
		List<ICacheEntry> cacheList = fCachesByContext.get(dmc);
		if (cacheList == null) {
			cacheList = new ArrayList<ICacheEntry>();
			fCachesByContext.put(dmc, cacheList);			
		}
		return cacheList;
		
	}
	
	/**
	 * Called to note that an existing cache object is being dished out (to a
	 * transaction). We move the entry to the back of the (discard) list.
	 * 
	 * @param <T>
	 *            the type of cache object
	 * @param cache
	 *            the cache object
	 * @return [cache] casted as T
	 */
	@SuppressWarnings("unchecked")
	private <T extends ICacheEntry> T reuseCache(ICacheEntry cache) {
		fAllCaches.remove(cache);
		fAllCaches.add(cache);
		fCachesUsedInThisTransaction.add(cache);
		return (T)cache;
	}

	/**
	 * Called to note that a new cache object has been instantiated and is being
	 * dished out (to a transaction)
	 * 
	 * @param <T>
	 *            the type of cache object
	 * @param cache
	 *            the cache object
	 * @param cacheList
	 *            the live collection (not a copy) of caches for a given
	 *            context. The new cache will be added to it
	 * 
	 * @return [cache] casted as T
	 */
	private <T extends ICacheEntry> T newCache(T cache, List<ICacheEntry> cacheList) {
		cache.setMetaData(new MetaData());
		cacheList.add(cache);
		fAllCaches.add(cache);
		fCachesUsedInThisTransaction.add(cache);
		return cache;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.launch.ICacheManager#getFormattedExpressionValue(org.eclipse.cdt.dsf.debug.service.IFormattedValues, org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext)
	 */
	public FormatedExpressionValueRequestCache getFormattedExpressionValue(IFormattedValues service, FormattedValueDMContext dmc) {
		assert fDsfSession.getExecutor().isInExecutorThread();
		assert fInTransaction;

		List<ICacheEntry> cacheList = getCacheListForContext(dmc); 
		for (ICacheEntry cache : cacheList) {
			if (cache instanceof FormatedExpressionValueRequestCache && ((FormatedExpressionValueRequestCache)cache).getContext().equals(dmc)) {
				return reuseCache(cache);
			}
		}
		
		return newCache(new FormatedExpressionValueRequestCache(service, dmc), cacheList);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.launch.ICacheManager#getAvailableFormats(org.eclipse.cdt.dsf.debug.service.IFormattedValues, org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext)
	 */
	public AvailableFormatsRequestCache getAvailableFormats(IFormattedValues service, IFormattedDataDMContext dmc) {
		assert fDsfSession.getExecutor().isInExecutorThread();

		List<ICacheEntry> cacheList = getCacheListForContext(dmc); 
		for (ICacheEntry cache : cacheList) {
			if (cache instanceof FormatedExpressionValueRequestCache && ((FormatedExpressionValueRequestCache)cache).getContext().equals(dmc)) {
				return reuseCache(cache);
			}
		}
		
		for (ICacheEntry cache : cacheList) {
			if (cache instanceof AvailableFormatsRequestCache && ((AvailableFormatsRequestCache)cache).getContext().equals(dmc)) {
				return reuseCache(cache);
			}
		}
		
		return newCache(new AvailableFormatsRequestCache(service, dmc), cacheList);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.launch.ICacheManager#getRegistersByName(org.eclipse.cdt.dsf.datamodel.IDMContext)
	 */
	public RegistersByNameRequestCache getRegistersByName(IDMContext dmc) {
		assert fDsfSession.getExecutor().isInExecutorThread();
		assert fInTransaction;
		
		List<ICacheEntry> cacheList = getCacheListForContext(dmc); 
		for (ICacheEntry cache : cacheList) {
			if (cache instanceof RegistersByNameRequestCache && ((RegistersByNameRequestCache)cache).getContext().equals(dmc)) {
				return reuseCache(cache);
			}
		}
		
		IRegisters service = fTracker.getService(IRegisters.class);
		if (service != null) {
			return newCache(new RegistersByNameRequestCache(service, dmc), cacheList);
		}
		return null;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.edc.launch.ICacheManager#getMemory(org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext, org.eclipse.cdt.core.IAddress, int)
     */
    public MemoryRangeCache getMemory(IMemoryDMContext dmc, IAddress address, int wordSize) {
		assert fDsfSession.getExecutor().isInExecutorThread();
		assert fInTransaction;
		
		List<ICacheEntry> cacheList = getCacheListForContext(dmc); 
		for (ICacheEntry cache : cacheList) {
			if (cache instanceof MemoryRangeCache && ((MemoryRangeCache)cache).getContext().equals(dmc)) {
				return reuseCache(cache);
			}
		}
		
		IMemory service = fTracker.getService(IMemory.class);
		if (service != null) {
			return newCache(new MemoryRangeCache(service, dmc, address, wordSize), cacheList);
		}
		return null;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.launch.ICacheManager#beginTransaction()
	 */
	public void beginTransaction() {
		assert fCachesUsedInThisTransaction.size() == 0;
		fCachesUsedInThisTransaction.clear();	// just in case
		fInTransaction = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.launch.ICacheManager#endTransaction(boolean)
	 */
	public void endTransaction(boolean failedInvalidCache) {
		assert fDsfSession.getExecutor().isInExecutorThread();
		fInTransaction = false;
		if (failedInvalidCache == false) {
			System.out.println("The successful transaction took " + fCachesUsedInThisTransaction.size() + " caches.");
		}
		
		// Mark cache objects used in the transaction as 'in use' if the
		// transaction failed due to an invalid cache exception. If the 
		// transaction completed successfully or failed for any other reason,
		// then the transaction won't have another go and we can safely remove
		// any of the cache objects involved. Any 'in use' ones need to be
		// monitored for cancellation since the transaction could be canceled at
		// any time.
		for (ICacheEntry used : fCachesUsedInThisTransaction) {
			((MetaData)used.getMetaData()).inUse = failedInvalidCache;
			RequestMonitor rm = used.getRequestMonitor();
			if (rm != null) { 
				if (failedInvalidCache) {
					rm.addCancelListener(this);
					fMonitorToCache.put(rm, used);
				}
				else {
					rm.removeCancelListener(this);
					fMonitorToCache.remove(rm);
				}
			}
		}
		
		// See if our cache count has exceeded the limit and make room if so
		assert totalCacheCount() == fAllCaches.size();
		final int overage = fAllCaches.size() - fMaxCaches;
		if (overage > fAllowedOverage) {
			// First assemble a list of cache objects we want to remove. We'll
			// remove the objct from the flat list immediately, but removing 
			// them from the context-collection is trickier
			Set<ICacheEntry> removeThese = new HashSet<ICacheEntry>(overage);
			int discardCount = 0;
			Iterator<ICacheEntry> iter = fAllCaches.iterator();
			while (iter.hasNext() && (discardCount < overage)) {
				ICacheEntry cache = iter.next();
				MetaData mdata = (MetaData)cache.getMetaData();
				if (!mdata.inUse) {
					removeThese.add(cache);
					iter.remove(); // remove from the flat list now
					discardCount++;
				}
			}

			if (discardCount > 0) {
				System.out.println("Going to discard " + discardCount + " caches");
				List<ICacheEntry> removeForThisContext = new ArrayList<ICacheEntry>(fAllCaches.size() - fMaxCaches);
				Iterator<Entry<IDMContext, List<ICacheEntry>>> citer = fCachesByContext.entrySet().iterator();
				while (citer.hasNext()) {
					Entry<IDMContext, List<ICacheEntry>> entry = citer.next();
					removeForThisContext.clear();
					List<ICacheEntry> cachesForThisContext = entry.getValue();
					for (ICacheEntry cache : cachesForThisContext) {
						if (removeThese.contains(cache)) {
							System.out.println("Removing cache: " + cache);
							removeForThisContext.add(cache);
							cache.dispose();
						}
					}
					if (removeForThisContext.size() > 0) {
						cachesForThisContext.removeAll(removeForThisContext);
						if (cachesForThisContext.size() == 0) {
							citer.remove();
						}
					}
				}
			}
		}
		
		assert totalCacheCount() == fAllCaches.size();
		
		fCachesUsedInThisTransaction.clear();
	}

	/**
	 * We use this internally to validate that the number of cache objects in
	 * the hierarchical collection equals the number of elements in the flat
	 * collection. This returns the former.
	 * 
	 * @return
	 */
	private int totalCacheCount() {
		int count = 0;
		for (Map.Entry<IDMContext, List<ICacheEntry>> entry : fCachesByContext.entrySet()) {
			count += entry.getValue().size();
		}
		return count;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.launch.ICacheManager#purgeAll()
	 */
	public void purgeAll() {
		assert fDsfSession.getExecutor().isInExecutorThread();
		for (Map.Entry<IDMContext, List<ICacheEntry>> entry : fCachesByContext.entrySet()) {
			List<ICacheEntry> caches = entry.getValue();
			for (ICacheEntry cache : caches) {
				cache.dispose();
			}
			caches.clear();
		}
		fCachesByContext.clear();
		fAllCaches.clear();
	}
	
	public void dumpStats() {
		System.out.println("There are " + fAllCaches.size() + " total caches.");

		for (Map.Entry<IDMContext, List<ICacheEntry>> entry : fCachesByContext.entrySet()) {
			IDMContext dmc = entry.getKey();
			System.out.println("Context: " + dmc.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(dmc)));
			List<ICacheEntry> caches = entry.getValue();
			for (ICacheEntry cache : caches) {
				MetaData md = (MetaData)cache.getMetaData();
				Formatter formatter = new Formatter();
				formatter.format("%s [inUse=%b, usedInCurrentTransaction=%b]", 
						cache.toString(), md.inUse, md.usedInCurrentTransaction);
				System.out.println(formatter.toString());
			}
		}
	}

	/**
	 * Method use for testing. Keep in mind this is an internal class and the
	 * method is not part of ICacheManager
	 */
	public LongNoopRequestCache getLongNoop(IDMContext dmc) {
		assert fDsfSession.getExecutor().isInExecutorThread();
		assert fInTransaction;

		List<ICacheEntry> cacheList = getCacheListForContext(dmc); 
		for (ICacheEntry cache : cacheList) {
			if (cache instanceof LongNoopRequestCache && ((LongNoopRequestCache)cache).getContext().equals(dmc)) {
				return reuseCache(cache);
			}
		}

		INoop service = fTracker.getService(INoop.class);
		if (service != null) {
			return newCache(new LongNoopRequestCache(service, dmc), cacheList);
		}
		return null;
	}

	/**
	 * Method use for testing. Keep in mind this is an internal class and the
	 * method is not part of ICacheManager
	 */
	public LongRangeNoopRequestCache getLongRangeNoop(IDMContext dmc) {
		assert fDsfSession.getExecutor().isInExecutorThread();
		assert fInTransaction;

		List<ICacheEntry> cacheList = getCacheListForContext(dmc); 
		for (ICacheEntry cache : cacheList) {
			if (cache instanceof LongRangeNoopRequestCache && ((LongRangeNoopRequestCache)cache).getContext().equals(dmc)) {
				return reuseCache(cache);
			}
		}

		INoop service = fTracker.getService(INoop.class);
		if (service != null) {
			return newCache(new LongRangeNoopRequestCache(service, dmc), cacheList);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.concurrent.RequestMonitor.ICanceledListener#requestCanceled(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	public void requestCanceled(RequestMonitor rm) {
		assert fDsfSession.getExecutor().isInExecutorThread();
		
		// When a transaction is canceled, any pending updates of cache objects
		// may end up getting canceled as well. We need to find out when that
		// happens and remove those cache objects from our pool so that we don't
		// dish them out to any other transactions. A canceled cache object
		// currently ends up in the valid state with a canceled status. That
		// means it's dead in the water. Any transaction that attempts to use
		// them will outright fail. See 
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=310345#c47
		ICacheEntry cache = fMonitorToCache.get(rm);
		if (cache != null) {
			// remove the cache object from our flat collection... 
			fAllCaches.remove(cache);
			
			// ...and from our cache-grouped collection
			IDMContext dmc = cache.getContext();
			List<ICacheEntry> cacheList = getCacheListForContext(dmc);
			cacheList.remove(cache);
			
			cache.dispose();
			rm.removeCancelListener(this);
			
			// If discarding this cache object left us with no caches for the
			// context, then remove the context from the collection.
			if (cacheList.size() == 0) {
				fCachesByContext.remove(dmc);
			}
		}
		assert totalCacheCount() == fAllCaches.size();		
	}
}
