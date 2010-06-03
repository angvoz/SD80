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

import java.math.BigInteger;

import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IMixedInstruction;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.junit.Test;

public class DisassemblyView extends BaseLaunchTest {

	/*
	 * NOTE: This is the source line and its corresponding code addresses in the
	 * test case program specified by EXEPATH environemnt variable.
	 * 
	 * The value below is for BlackFlagWascana.exe I built using Cygwin. Change
	 * them accordingly if you are using different binary file.
	 * 
	 * TODO: make it more flexible.
	 */
	static final String sSrcFile = "dbg_breakpoints.cpp";
	static final int sLineNumber = 82; // line in above source file.
	static final int sStartAddress = 0x4024ac, sEndAddress = 0x4024d2;

	@Test
	public void testDisassemblyView() throws Exception {
		EDCLaunch launch = createLaunch();
		assertNotNull(launch);
		DsfSession session = waitForSession(launch);
		assertNotNull(session);
		ExecutionDMC executionDMC = waitForExecutionDMC(session);
		assertNotNull(executionDMC);
		final IDisassemblyDMContext disassemblyDMC = DMContexts.getAncestorOfType(executionDMC,
				IDisassemblyDMContext.class);
		assertNotNull(disassemblyDMC);
		DsfServicesTracker servicesTracker = getDsfServicesTracker(session);
		final IDisassembly service = servicesTracker.getService(IDisassembly.class);

		// get assembly code by source file & line number
		//
		Query<IMixedInstruction[]> query1 = new Query<IMixedInstruction[]>() {
			@Override
			protected void execute(final DataRequestMonitor<IMixedInstruction[]> drm) {
				service.getMixedInstructions(disassemblyDMC, sSrcFile, sLineNumber, 2, drm);
			}
		};
		session.getExecutor().execute(query1);

		IMixedInstruction[] result1 = query1.get();

		for (IMixedInstruction mi : result1)
			System.out.println(mi);

		// get assembly code by runtime address (same as logical address on
		// Windows & Linux)
		//
		Query<IMixedInstruction[]> query2 = new Query<IMixedInstruction[]>() {
			@Override
			protected void execute(final DataRequestMonitor<IMixedInstruction[]> drm) {
				service.getMixedInstructions(disassemblyDMC, new BigInteger(Integer.toString(sStartAddress)),
						new BigInteger(Integer.toString(sEndAddress)), drm);
			}
		};
		session.getExecutor().execute(query2);

		IMixedInstruction[] result2 = query2.get();

		for (IMixedInstruction mi : result2)
			System.out.println(mi);

		assertEquals(result1[0].getLineNumber(), result2[0].getLineNumber());
		assertEquals(result1[0].getInstructions().length, result2[0].getInstructions().length);

		for (int i = 0; i < result1[0].getInstructions().length; i++)
			assertTrue(TestUtils.stringCompare(result1[0].getInstructions()[i].toString(),
					result2[0].getInstructions()[i].toString(), true, // ignoreCase,
					true, // ignoreWhite,
					false // ignore0x
					));
	}
}
