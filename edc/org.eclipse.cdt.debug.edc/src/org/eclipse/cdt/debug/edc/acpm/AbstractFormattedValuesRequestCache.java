package org.eclipse.cdt.debug.edc.acpm;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;

/**
 * Base class for ACPM objects that cache information obtained from
 * {@link IFormattedValues}
 * 
 * @param <V>
 *            the specific type of information requested
 * @since 2.0
 */
public abstract class AbstractFormattedValuesRequestCache<V> extends BaseRequestCache<V> {

	/** Constructor */
	public AbstractFormattedValuesRequestCache(IFormattedValues service, IDMContext ctx) {
        super(service, ctx);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.acmp.BaseRequestCache#resumedEventHandler(org.eclipse.cdt.dsf.datamodel.IDMEvent)
	 */
	@DsfServiceEventHandler
	public void resumedEventHandler(IDMEvent<?> e) {
		// TODO: for now, we defer to the default behavior of losing confidence
		// in our data on *any* debug event. We should be more discriminating,
		// but this will be tricky from here since technically we're dealing
		// with a generic IFormattedValues object. Could be a register, an
		// expression, or something else. I don't know that we can cleanly
		// determine what events will invalidate out data.
		super.resumedEventHandler(e);
	}
}
