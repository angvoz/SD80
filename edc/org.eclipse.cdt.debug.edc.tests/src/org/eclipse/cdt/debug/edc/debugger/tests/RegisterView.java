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

import junit.framework.Assert;

import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ThreadExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.debug.edc.services.Registers.RegisterDMC;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.debug.edc.tests.TestUtils.Condition;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.CompositeDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

public class RegisterView extends BaseLaunchTest {

	private EDCLaunch launch;
	private DsfSession session;

	@Test
	public void testRegisterView() throws Exception {
		TestUtils.showDebugPerspective();	
		launch = createLaunch();
		assertNotNull(launch);
		session = TestUtils.waitForSession(launch);
		assertNotNull(session);
		final ExecutionDMC executionDMC = TestUtils.waitForSuspendedThread(session);
		assertNotNull(executionDMC);
		Thread.sleep(10 * 1000);

		final Registers regService = TestUtils.getService(session, Registers.class);
		final IRegisterGroupDMContext regGroupDMC = waitForRegisterGroup((ThreadExecutionDMC) executionDMC, regService);
		final IRegisterDMContext[] regDMCs = waitForRegisterDMCs(executionDMC, regGroupDMC, regService);

		testRegisterWrites(regService, regDMCs);
	}

	private void testRegisterWrites(final Registers regService, final IRegisterDMContext[] regDMCs)
			throws Exception {
		for (IRegisterDMContext regContext : regDMCs) {
			final RegisterDMC regDMC = (RegisterDMC) regContext;
			regService.writeRegister(regDMC, "0000000d", "NATURAL.Format");
			Assert.assertEquals("d", regService.getRegisterValueAsHexString(regDMC));
		}
	}

	private IRegisterDMContext[] waitForRegisterDMCs(final ExecutionDMC executionDMC,
			final IRegisterGroupDMContext regGroupDMC, final Registers regService) throws Exception {
		final IRegisterDMContext contextsHolder[][] = { null };
		TestUtils.wait(new Condition() {
			public boolean isConditionValid() {
				CompositeDMContext compositeDMC = new CompositeDMContext(new IDMContext[] { executionDMC, regGroupDMC });
				regService.getRegisters(compositeDMC, new DataRequestMonitor<IRegisterDMContext[]>(regService
						.getExecutor(), null) {
					@Override
					protected void handleSuccess() {
						contextsHolder[0] = getData();
					}
				});
				if (contextsHolder[0] != null)
					return true;

				return false;
			}
		});
		return contextsHolder[0];
	}

	private IRegisterGroupDMContext waitForRegisterGroup(final ThreadExecutionDMC threadExeDMC,
			final Registers regService) throws Exception {
		final IRegisterGroupDMContext contextHolder[] = { null };
		TestUtils.wait(new Condition() {
			public boolean isConditionValid() {
				try {
					IRegisterGroupDMContext[] regGroups = regService.getGroupsForContext(threadExeDMC);
					if (regGroups.length > 0) {
						contextHolder[0] = regGroups[0];
						return true;
					}
				} catch (CoreException e) {
					e.printStackTrace();
					return false;
				}

				return false;
			}
		});
		return contextHolder[0];
	}
}
