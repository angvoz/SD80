package org.eclipse.cdt.debug.edc.acpm;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;

/**
 * Base class for ACPM objects that cache information obtained from
 * {@link IRegisters}
 * 
 * @param <V>
 *            the specific type of information requested
 * @since 2.0            
 */
public abstract class AbstractRegisterRequestCache<V> extends BaseRequestCache<V> {

	/** Constructor */
	public AbstractRegisterRequestCache(IRegisters service, IDMContext ctx) {
        super(service, ctx);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.acmp.BaseRequestCache#resumedEventHandler(org.eclipse.cdt.dsf.datamodel.IDMEvent)
	 */
	@DsfServiceEventHandler
	public void resumedEventHandler(IDMEvent<?> e) {
		// By default, don't invalidate our data on any event. Our derivatives
		// store information *about* the registers, not the contents of
		// registers. The former typically does not change over the course
		// of a debug session
	}
}
