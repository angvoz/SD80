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

import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ThreadExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.debug.edc.services.Registers.RegisterDMC;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.debug.edc.tests.TestUtils.Condition;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.CompositeDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IBitFieldDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.junit.Test;

public class RegisterView extends BaseLaunchTest {

	@Test
	public void testRegisterView() throws Exception {
		final EDCLaunch launch = createLaunch();
		assertNotNull(launch);
		final DsfSession session = waitForSession(launch);
		assertNotNull(session);
		final ExecutionDMC executionDMC = waitForExecutionDMC(session);
		assertNotNull(executionDMC);
		final ThreadExecutionDMC threadExeDMC = waitForThreadExeDMC(session, executionDMC);
		final Registers regService = getDsfServicesTracker(session).getService(Registers.class);
		final IRegisterGroupDMContext regGroupDMC = waitForRegisterGroup(threadExeDMC, regService);
		final IRegisterDMContext[] regDMCs = waitForRegisterDMCs(executionDMC, regGroupDMC, regService);

		testRegisterWrites(regService, regDMCs);
	}

	private void testRegisterWrites(final Registers regService, final IRegisterDMContext[] regDMCs)
			throws InterruptedException {
		TestUtils.wait(new Condition() {
			public boolean isConditionValid() {
				for (IRegisterDMContext regContext : regDMCs) {
					final RegisterDMC regDMC = (RegisterDMC) regContext;
					regService.writeRegister(regDMC, "0000000d", "NATURAL.Format",
							new DataRequestMonitor<IBitFieldDMData>(regService.getExecutor(), null));
					String regValue = regService.getRegisterValue(regDMC);
					if (!regValue.toLowerCase().equals("0000000d"))
						return false;
				}

				return true;
			}
		});
	}

	private IRegisterDMContext[] waitForRegisterDMCs(final ExecutionDMC executionDMC,
			final IRegisterGroupDMContext regGroupDMC, final Registers regService) throws InterruptedException {
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
			final Registers regService) throws InterruptedException {
		final IRegisterGroupDMContext contextHolder[] = { null };
		TestUtils.wait(new Condition() {
			public boolean isConditionValid() {
				IRegisterGroupDMContext[] regGroups = regService.getGroupsForContext(threadExeDMC);
				if (regGroups.length > 0) {
					contextHolder[0] = regGroups[0];
					return true;
				}

				return false;
			}
		});
		return contextHolder[0];
	}

	private ThreadExecutionDMC waitForThreadExeDMC(final DsfSession session, final ExecutionDMC executionDMC)
			throws InterruptedException {
		final ThreadExecutionDMC contextHolder[] = { null };
		TestUtils.wait(new Condition() {
			public boolean isConditionValid() {
				for (IEDCExecutionDMC context : executionDMC.getChildren()) {
					if (context instanceof ThreadExecutionDMC) {
						contextHolder[0] = (ThreadExecutionDMC) context;
						return true;
					}
				}

				return false;
			}

		});
		return contextHolder[0];
	}
}
