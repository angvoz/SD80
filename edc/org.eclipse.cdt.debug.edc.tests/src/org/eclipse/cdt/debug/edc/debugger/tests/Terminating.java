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
import org.eclipse.cdt.debug.edc.tests.TestUtils.Condition;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.Test;

public class Terminating extends BaseLaunchTest {

	protected void waitForLaunchTerminated(final EDCLaunch launch) throws InterruptedException {
		TestUtils.wait(new Condition() {
			public boolean isConditionValid() {
				return launch.isTerminated();
			}
		});
	}
	
	@Test
	public void testTerminating() throws Exception {
		EDCLaunch launch = createLaunch();
		assertNotNull(launch);
		DsfSession session = waitForSession(launch);
		waitForExecutionDMC(session);
		assertTrue(launch.canTerminate());
		assertFalse(launch.isTerminated());
		launch.terminate();
		waitForLaunchTerminated(launch);
		assertFalse(launch.canTerminate());
		assertTrue(launch.isTerminated());
	}

	@Test
	public void testSnapshotTermainating() throws Exception {
		TestUtils.disableDebugPerspectiveSwitchPrompt();
		EDCLaunch launch = TestUtils.createLaunchForAlbum("BlackFlag.dsa");
		assertNotNull(launch);
		if (launch.isSnapshotLaunch()) {
			DsfSession session = waitForSession(launch);
			waitForExecutionDMC(session);
			launch.terminate();
			waitForLaunchTerminated(launch);
			ILaunchConfiguration lc = SnapshotUtils.findExistingLaunchForAlbum(launch.getAlbum());
			assertNull(lc);
		}
	}
}
