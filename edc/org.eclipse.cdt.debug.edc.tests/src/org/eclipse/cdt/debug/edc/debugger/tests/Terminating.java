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

import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.Test;

public class Terminating extends BaseLaunchTest {
	
	@Test
	public void testTerminating() throws Exception {
		TestUtils.showDebugPerspective();
		EDCLaunch launch = createLaunch();
		assertNotNull(launch);
		DsfSession session = waitForSession(launch);
		waitForExecutionDMC(session);
		assertTrue(launch.canTerminate());
		assertFalse(launch.isTerminated());
		TestUtils.terminateLaunch(launch);
		assertFalse(launch.canTerminate());
		assertTrue(launch.isTerminated());
	}

	@Test
	public void testSnapshotTermainating() throws Exception {
		EDCLaunch launch = TestUtils.createLaunchForAlbum("BlackFlag.dsa");
		assertNotNull(launch);
		if (launch.isSnapshotLaunch()) {
			DsfSession session = waitForSession(launch);
			waitForExecutionDMC(session);
			TestUtils.terminateLaunch(launch);
			ILaunchConfiguration lc = SnapshotUtils.findExistingLaunchForAlbum(launch.getAlbum());
			assertNull(lc);
		}
	}
}
