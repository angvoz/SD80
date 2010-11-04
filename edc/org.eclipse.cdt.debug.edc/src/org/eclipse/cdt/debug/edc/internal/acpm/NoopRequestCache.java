package org.eclipse.cdt.debug.edc.internal.acpm;

import org.eclipse.cdt.debug.edc.acpm.BaseRequestCache;
import org.eclipse.cdt.debug.edc.internal.services.dsf.INoop;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * @since 2.0
 */
public class NoopRequestCache extends BaseRequestCache<Boolean> {

	public NoopRequestCache(INoop service, IDMContext ctx) {
        super(service, ctx);
    }

	protected void retrieve(final DataRequestMonitor< Boolean > rm) {
		((INoop)fService).noop(getContext(), new DataRequestMonitor<Boolean>(fService.getExecutor(), rm) {
			public void handleSuccess() {
				rm.setData(getData());
				rm.done();
			}
		});
	}
}
