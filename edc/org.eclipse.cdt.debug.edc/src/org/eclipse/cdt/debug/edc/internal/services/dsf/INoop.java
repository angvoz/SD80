package org.eclipse.cdt.debug.edc.internal.services.dsf;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * A dummy DSF service used for testing.
 */
public interface INoop extends IDsfService {
	/** Simply sticks a boolean in the given RM */
	void noop(IDMContext whatever, DataRequestMonitor<Boolean> rm);
	
	/**
	 * Simply sticks a boolean in the given RM after 10 seconds have elapsed.
	 * The sleep is not done on the DSF executor thread, of course
	 */
	void longNoop(IDMContext whatever, DataRequestMonitor<Boolean> rm);
}