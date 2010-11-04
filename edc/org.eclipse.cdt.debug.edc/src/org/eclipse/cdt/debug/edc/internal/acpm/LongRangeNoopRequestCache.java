
package org.eclipse.cdt.debug.edc.internal.acpm;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.edc.acpm.BaseRangeCache;
import org.eclipse.cdt.debug.edc.internal.services.dsf.INoop;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * @since 2.0
 */
public class LongRangeNoopRequestCache extends BaseRangeCache<Boolean> {

	public LongRangeNoopRequestCache(IDsfService service, IDMContext ctx) {
		super(service, ctx);
	}

	@Override
	protected void retrieve(long offset, final int count,
			final DataRequestMonitor<List<Boolean>> rm) {
		((INoop)fService).longNoop(getContext(), new DataRequestMonitor<Boolean>(fService.getExecutor(), rm) {
			public void handleSuccess() {
				List<Boolean> result = new ArrayList<Boolean>();
				for (int i = 0; i < count; i++) {
					result.add(new Boolean(true));
				}
				rm.setData(result);
				rm.done();
			}
		});
	}
}
