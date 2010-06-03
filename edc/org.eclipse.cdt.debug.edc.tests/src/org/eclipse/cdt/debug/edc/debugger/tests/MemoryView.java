/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.debugger.tests;

import java.util.ArrayList;
import java.util.Random;

import org.eclipse.cdt.debug.edc.internal.services.dsf.Memory;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.IEDCMemory;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.debug.edc.tests.TestUtils.Condition;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlockRetrieval;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.MemoryByte;
import org.junit.Test;

public class MemoryView extends BaseLaunchTest {

	@Test
	public void testMemoryView() throws Exception {
		final EDCLaunch launch = createLaunch();
		assertNotNull(launch);
		final Addr32 addr32 = new Addr32(0x405400);
		final DsfSession session = waitForSession(launch);
		assertNotNull(session);
		final DsfMemoryBlockRetrieval memoryBlock = launch.getMemoryBlockRetrieval();
		assertNotNull(memoryBlock);
		final ExecutionDMC executionDMC = waitForExecutionDMC(session);
		assertNotNull(executionDMC);
		DsfServicesTracker servicesTracker = getDsfServicesTracker(session);
		final Memory mem = servicesTracker.getService(Memory.class);
		// Write memory, read memory back and compare
		final byte[] data = new byte[1000];
		new Random().nextBytes(data);

		// Use a Query to synchronize the downstream calls
		Query<MemoryByte[]> query = new Query<MemoryByte[]>() {
			@Override
			protected void execute(final DataRequestMonitor<MemoryByte[]> drm) {
				mem.setMemory(executionDMC, addr32, 0, 1, 1000, data,
						new RequestMonitor(memoryBlock.getExecutor(), drm));
				drm.done();
			}
		};
		memoryBlock.getExecutor().execute(query);

		waitForMemoryValues(addr32, executionDMC, mem, data);
	}

	private void waitForMemoryValues(final Addr32 addr32, final ExecutionDMC executionDMC, final IEDCMemory mem,
			final byte[] data) throws InterruptedException {
		TestUtils.wait(new Condition() {
			public boolean isConditionValid() {
				ArrayList<MemoryByte> buf = new ArrayList<MemoryByte>();
				IStatus memGetStatus = mem.getMemory(executionDMC, addr32, buf, data.length, 1);

				if (memGetStatus.isOK()) {
					for (int i = 0; (i < buf.size()) && (i < data.length); i++)
						if (data[i] != buf.get(i).getValue())
							return false;
				} else
					return false;

				return true;
			}
		});
	}
}
