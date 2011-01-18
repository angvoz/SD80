package org.eclipse.cdt.debug.edc.internal.services.dsf;

import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.Status;

/**
 * Implementation of the no-op service used for testing
 *
 */
public class Noop extends AbstractEDCService implements INoop {

	public Noop(DsfSession session) {
		super(session, new String[] {INoop.class.getName()});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.INoop#noop(org.eclipse.cdt.dsf.datamodel.IDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void noop(IDMContext ctx, DataRequestMonitor<Boolean> rm) {
		rm.setData(true);
		rm.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.INoop#longNoop(org.eclipse.cdt.dsf.datamodel.IDMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void longNoop(IDMContext whatever, final DataRequestMonitor<Boolean> rm) {
		new Thread() {
			public void run() {
				try {
					for (int i = 0; i < 100; i++) {
						Thread.sleep(100);
						if (rm.isCanceled()) {
							rm.setStatus(Status.CANCEL_STATUS);
							rm.done();
							return;
						}
					}
				} catch (InterruptedException e) {}
				rm.setData(true);
				rm.done();
			}
		}.start();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.INoop#longNoopUsingServiceTracker(int, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	public void longNoopUsingServiceTracker(int duration, RequestMonitor rm) {
		for (int i = 0; i < duration; i++) {
			// ask for any service; our own is fine
			getService(INoop.class);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
}
