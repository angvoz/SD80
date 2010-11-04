package org.eclipse.cdt.debug.edc.acpm;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;

/**
 * An ACPM cache for {@link IFormattedValues#getFormattedExpressionValue(FormattedValueDMContext, DataRequestMonitor)} 
 * @since 2.0
 */
public class FormatedExpressionValueRequestCache extends AbstractFormattedValuesRequestCache<FormattedValueDMData> {

	public FormatedExpressionValueRequestCache(IFormattedValues service, FormattedValueDMContext ctx) {
        super(service, ctx);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.concurrent.RequestCache#retrieve(org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	protected void retrieve(final DataRequestMonitor<FormattedValueDMData> rm) {

		((IFormattedValues)fService).getFormattedExpressionValue((FormattedValueDMContext)fCtx, new DataRequestMonitor<FormattedValueDMData>(fService.getExecutor(), rm) {
			public void handleSuccess() {
				rm.setData(getData());
				rm.done();
			}
		});
	}
}
