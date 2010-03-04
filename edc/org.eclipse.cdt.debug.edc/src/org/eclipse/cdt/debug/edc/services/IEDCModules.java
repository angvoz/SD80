package org.eclipse.cdt.debug.edc.services;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;

public interface IEDCModules {

	/**
	 * get module that contains the given runtime address.
	 * 
	 * @param symCtx
	 * @param instructionAddress
	 *            runtime absolute address.
	 * @return null if not found.
	 */
	public IEDCModuleDMContext getModuleByAddress(ISymbolDMContext symCtx,
			IAddress instructionAddress);

}