package org.eclipse.cdt.debug.edc.acpm;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;

/**
 * An ACPM cache for {@link IRegisters#getRegisters(IDMContext, DataRequestMonitor) 
 * @since 2.0
 */
public class GetRegistersRequestCache extends AbstractRegisterRequestCache<IRegisterDMContext[]> {

	public GetRegistersRequestCache(IRegisters service, IDMContext ctx) {
        super(service, ctx);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.concurrent.RequestCache#retrieve(org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	protected void retrieve(DataRequestMonitor<IRegisterDMContext[]> rm) {
		((IRegisters)fService).getRegisters(fCtx, rm);
	}
}
