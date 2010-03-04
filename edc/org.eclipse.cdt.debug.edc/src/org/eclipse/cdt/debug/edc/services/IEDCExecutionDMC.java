package org.eclipse.cdt.debug.edc.services;

import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;

public interface IEDCExecutionDMC extends IExecutionDMContext,IMemoryDMContext, IEDCDMContext {

	public boolean isSuspended();
	
	public ISymbolDMContext getSymbolDMContext();
	
}