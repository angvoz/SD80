package org.eclipse.cdt.debug.edc.internal.services.dsf;

import org.eclipse.cdt.debug.edc.services.IEDCService;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * A dummy DSF service used for testing.
 */
public interface INoop extends IEDCService {
	/** Simply sticks a boolean in the given RM */
	void noop(IDMContext whatever, DataRequestMonitor<Boolean> rm);
	
	/**
	 * Simply sticks a boolean in the given RM after 10 seconds have elapsed.
	 * The sleep is not done on the DSF executor thread, of course
	 */
	void longNoop(IDMContext whatever, DataRequestMonitor<Boolean> rm);

	/**
	 * This service simply loops for [duration] seconds, asking for the service
	 * tracker every second and trying to use it to get a service. It's used in
	 * a shutdown test to validate that threads in the EDC thread pool are given
	 * a chance to complete before the DSF session moves forward with its
	 * shutdown. If session shutdown didn't wait, then this service would end up
	 * encountering either a null pointer exception or an exception due to the
	 * attempted use of a disposed tracker.
	 * 
	 * @param duration
	 *            the number of seconds to loop
	 * @param rm
	 *            not used but included for consistency
	 */
	void longNoopUsingServiceTracker(int duration, RequestMonitor rm);
}