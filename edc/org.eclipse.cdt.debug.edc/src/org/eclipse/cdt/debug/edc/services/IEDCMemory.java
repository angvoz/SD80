package org.eclipse.cdt.debug.edc.services;

import java.util.ArrayList;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.MemoryByte;

public interface IEDCMemory extends IMemory {

	public IStatus getMemory(IEDCExecutionDMC context, IAddress address,
			final ArrayList<MemoryByte> memBuffer, int count, int word_size);

}