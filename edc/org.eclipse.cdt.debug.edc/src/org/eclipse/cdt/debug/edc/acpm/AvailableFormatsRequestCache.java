package org.eclipse.cdt.debug.edc.acpm;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;

/**
 * Cached result of {@link IFormattedValues#getAvailableFormats(IFormattedDataDMContext, DataRequestMonitor)} 
 * 
 * @param <V>
 *            the specific type of information requested
 * @since 2.0            
 */
public class AvailableFormatsRequestCache extends AbstractFormattedValuesRequestCache<String[]> {

	/** Constructor */
	public AvailableFormatsRequestCache(IFormattedValues service, IDMContext ctx) {
        super(service, ctx);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.concurrent.RequestCache#retrieve(org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	protected void retrieve(final DataRequestMonitor<String[]> rm) {
		((IFormattedValues)fService).getAvailableFormats((IFormattedDataDMContext)fCtx, new DataRequestMonitor<String[]>(fService.getExecutor(), rm) {
			public void handleSuccess() {
				rm.setData(getData());
				rm.done();
			}
		});
	}
}
