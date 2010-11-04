package org.eclipse.cdt.debug.edc.acpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.services.DMContext;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;

/**
 * An ACPM cache of the collection of all available registers for the given
 * context, indexed by name. The name is provided by TCF via the property
 * {@link org.eclipse.tm.tcf.services.IRegisters#PROP_NAME}
 * 
 * @since 2.0
 */
public class RegistersByNameRequestCache extends AbstractRegisterRequestCache< Map<String, IRegisterDMContext> > {

	public RegistersByNameRequestCache(IRegisters service, IDMContext ctx) {
        super(service, ctx);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.concurrent.RequestCache#retrieve(org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	protected void retrieve(final DataRequestMonitor< Map<String, IRegisterDMContext> > rm) {
		final Map<String, IRegisterDMContext> registerMap = new HashMap<String, IRegisterDMContext>();
		((IRegisters)fService).getRegisterGroups(fCtx, new DataRequestMonitor<IRegisterGroupDMContext[]>(fService.getExecutor(), rm) {
			public void handleSuccess() {
				final List<IRegisterDMContext> registers = new ArrayList<IRegisterDMContext>();
				final IRegisterGroupDMContext[] groups = getData(); 
				final CountingRequestMonitor crm = new CountingRequestMonitor(fService.getExecutor(), rm) {
					public void handleSuccess() {
						for (IRegisterDMContext register : registers) {
							String regname = (String)((DMContext)register).getProperties().get(org.eclipse.tm.tcf.services.IRegisters.PROP_NAME);
							registerMap.put(regname, register);
						}
						rm.setData(registerMap);
						rm.done();
					}
				};
				crm.setDoneCount(groups.length);
				for (IRegisterGroupDMContext group : groups) {
					((IRegisters)fService).getRegisters(group, new DataRequestMonitor<IRegisterDMContext[]>(fService.getExecutor(), crm) {
						public void handleSuccess() {
							IRegisterDMContext[] regs = getData();
							for (IRegisterDMContext reg : regs) {
								registers.add(reg);
							}
							crm.done();
						}
					});
				}
			}
		});
	}
}
