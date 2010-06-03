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

import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.junit.Test;

public class Launching extends BaseLaunchTest {

	@Test
	public void testLaunching() throws Exception {
		EDCLaunch launch = createLaunch();
		assertNotNull(launch);
		DsfSession session = waitForSession(launch);
		assertNotNull(session);
		IEDCExecutionDMC executionDMC = waitForExecutionDMC(session);
		assertNotNull(executionDMC);
	}

}
