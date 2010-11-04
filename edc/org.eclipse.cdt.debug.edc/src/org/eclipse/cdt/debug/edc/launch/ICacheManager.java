package org.eclipse.cdt.debug.edc.launch;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.acpm.AvailableFormatsRequestCache;
import org.eclipse.cdt.debug.edc.acpm.FormatedExpressionValueRequestCache;
import org.eclipse.cdt.debug.edc.acpm.MemoryRangeCache;
import org.eclipse.cdt.debug.edc.acpm.RegistersByNameRequestCache;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;

/**
 * Interface for an ACPM cache manager used within EDC. The cache manager is
 * responsible for dishing out cache objects and managing their lifetimes.
 * <p>
 * In theory, every method of every available DSF service could be given an ACPM
 * front end, making this a massive interface. In practice, though, ACPM is used
 * selectively for situations where coding using asynchronous APIS becomes
 * unwieldy. This interface will attempt to provide access to the service
 * methods likely to be used in those situations. This interface will grow in
 * time and as such is marked @noimplement and @noextend so the expansion can be
 * done without breaking backward compatibility.
 * 
 * TODO: extensibility; allow EDC adopters to integrate custom services
 * 
 * @since 2.0
 * @noimplement
 * @noextend
 */
@ConfinedToDsfExecutor("")	// any executor
public interface ICacheManager {

	/** See {@link IFormattedValues#getFormattedExpressionValue(FormattedValueDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)} */
	public FormatedExpressionValueRequestCache getFormattedExpressionValue(IFormattedValues service, FormattedValueDMContext dmc);
	
	/** See {@link IFormattedValues#getAvailableFormats(IFormattedDataDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)} */
	public AvailableFormatsRequestCache getAvailableFormats(IFormattedValues service, IFormattedDataDMContext dmc);
	
	/** Returns the collection of all available registers for the given context, indexed by name */
	public RegistersByNameRequestCache getRegistersByName(IDMContext dmc);

	/**
	 * Returns a range cache that can be used to get any range of memory
	 * relative to [address].
	 * {@link IMemory#getMemory(IMemoryDMContext, IAddress, long, int, int, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)}
	 */
    public MemoryRangeCache getMemory(IMemoryDMContext dmc, IAddress address, int wordSize);

	/**
	 * ACPM transactions that use this cache manager interface are required to
	 * start the transaction by calling this method, and then calling
	 * {@link #endTransaction(boolean)} at the end of the attempt (successful or
	 * not). The manager relies on this for tracking cache objects used in the
	 * current transaction (only one can run at any one time since ACPM
	 * transactions run on the DSF thread). That information is used in managing
	 * the lifetime of cache objects.
	 * 
	 * <pre>
	 * class MyTransaction<Boolean> {
	 *     protected Boolean process() throws InvalidCacheException, CoreException {
	 *         boolean invalidCache = false;
	 *         ICacheManager cacheMgr = ...
	 *         cacheMgr.beginTransaction();
	 *         try {
	 *             Boolean result = false;
	 *             // ... transaction logic
	 *             return result
	 *          }
	 *          catch (InvalidCacheException exc) {
	 *              invalidCache = true;
	 *              throw exc;
	 *          }
	 *          finally {
	 *              cacheMgr.endTransaction(invalidCache);
	 *          }
	 *      }
	 * }
	 * </pre>
	 * 
	 */
	public void beginTransaction();

	/**
	 * See {@link #beginTransaction()}
	 * 
	 * @param failedInvalidCache
	 *            indicates whether the transaction failed due to an invalid
	 *            cache exception. One that fails in that way will run again
	 *            once the required invalid cache objects become valid; it's an
	 *            "ongoing" transaction. It's imperative for the cache manager
	 *            to not discard cache objects involved in ongoing transactions.
	 */
	public void endTransaction(boolean failedInvalidCache);

	/**
	 * Tells the cache manager to discard all cache objects.
	 */
	public void purgeAll();
}
