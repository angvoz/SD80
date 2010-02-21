/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
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
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.dsf.service.DsfSession;

public class Launching extends BaseLaunchTest {

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
	}

	public void testLaunching() throws Exception {
		EDCLaunch launch = createLaunch();
		assertNotNull(launch);
		DsfSession session = waitForSession(launch);
		assertNotNull(session);
		ExecutionDMC executionDMC = waitForExecutionDMC(session);
		assertNotNull(executionDMC);
	}

}
