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
package org.eclipse.cdt.debug.edc.tests;

import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.tests.TestUtils.Condition;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.junit.Assert;

public abstract class AbstractLaunchTest extends Assert {

	protected DsfSession waitForSession(final EDCLaunch launch) throws InterruptedException {
		final DsfSession sessionHolder[] = { null };
		TestUtils.wait(new Condition() {
			public boolean isConditionValid() {
				DsfSession session = launch.getSession();
				if (session == null)
					return false;

				sessionHolder[0] = session;
				return true;
			}
		});
		return sessionHolder[0];
	}

	protected ExecutionDMC waitForExecutionDMC(final DsfSession session) throws Exception {
		final ExecutionDMC contextHolder[] = { null };
		TestUtils.waitOnExecutorThread(session, new Condition() {
			public boolean isConditionValid() {
				DsfServicesTracker servicesTracker = getDsfServicesTracker(session);
				RunControl runControlService = servicesTracker.getService(RunControl.class);
				if (runControlService == null)
					return false;
				ExecutionDMC rootDMC = runControlService.getRootDMC();
				if (rootDMC == null)
					return false;
				ExecutionDMC[] processes = rootDMC.getChildren();
				if (processes.length == 0)
					return false;

				contextHolder[0] = processes[0];
				return true;
			}

		});
		return contextHolder[0];
	}

	protected DsfServicesTracker getDsfServicesTracker(final DsfSession session) {
		return new DsfServicesTracker(EDCTestPlugin.getBundleContext(), session.getId());
	}
}