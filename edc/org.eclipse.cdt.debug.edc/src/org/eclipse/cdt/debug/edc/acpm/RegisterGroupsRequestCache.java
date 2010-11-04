package org.eclipse.cdt.debug.edc.acpm;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;

/**
 * An ACPM cache for
 * {@link IRegisters#getRegisters(IDMContext, DataRequestMonitor)}
 * 
 * @since 2.0
 */
public class RegisterGroupsRequestCache extends AbstractRegisterRequestCache<IRegisterGroupDMContext[]> {

	public RegisterGroupsRequestCache(IRegisters service, IDMContext ctx) {
        super(service, ctx);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.concurrent.RequestCache#retrieve(org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	protected void retrieve(DataRequestMonitor<IRegisterGroupDMContext[]> rm) {
		((IRegisters)fService).getRegisterGroups(fCtx, rm);
	}
}
