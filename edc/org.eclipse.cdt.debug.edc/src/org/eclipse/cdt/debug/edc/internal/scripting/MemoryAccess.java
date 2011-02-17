/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.internal.scripting;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.IMemoryAccess;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Memory;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.MemoryByte;

public class MemoryAccess implements IMemoryAccess {

	public MemoryByte[] getMemoryValues(final DsfSession session,
			final IEDCExecutionDMC exe_dmc,
			final String memoryAddress, final int length) throws Exception {
		Query<MemoryByte[]> query = new Query<MemoryByte[]>() {
			@Override
			protected void execute(final DataRequestMonitor<MemoryByte[]> drm) {
				DsfServicesTracker servicesTracker = DOMUtils.getDsfServicesTracker(session);
				final Memory memoryService = servicesTracker.getService(Memory.class);
				if (memoryService != null) {
					final IMemoryDMContext mem_dmc
					  = DMContexts.getAncestorOfType(exe_dmc, IMemoryDMContext.class);
					final IAddress start = new Addr64(memoryAddress);
					memoryService.getMemory(mem_dmc, start, 0, 1, length, drm);
				}
			}
		};

		session.getExecutor().execute(query);

		return query.get();

	}

	public boolean changeMemoryValue(final DsfSession session,
			final IEDCExecutionDMC exe_dmc,
			final String memoryAddress, final byte[] newMemoryValue)
			throws Exception {
		Query<IStatus> query = new Query<IStatus>() {
			@Override
			protected void execute(final DataRequestMonitor<IStatus> drm) {
				DsfServicesTracker servicesTracker = DOMUtils.getDsfServicesTracker(session);
				final Memory memoryService = servicesTracker.getService(Memory.class);
				if (memoryService != null) {
					final IMemoryDMContext mem_dmc
					  = DMContexts.getAncestorOfType(exe_dmc, IMemoryDMContext.class);
					final IAddress start = new Addr64(memoryAddress);
					memoryService.setMemory(mem_dmc, start, 0, 1, newMemoryValue.length, newMemoryValue, drm);
				}
			}
		};

		session.getExecutor().execute(query);

		return IStatus.OK == query.get().getSeverity();
	}
}
