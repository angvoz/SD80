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
package org.eclipse.cdt.debug.edc.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.debug.edc.debugger.tests.Launching;
import org.eclipse.cdt.debug.edc.debugger.tests.Terminating;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { Expressions.class })
public class AllEDCTestsNotWorking {
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Broken Tests for org.eclipse.cdt.debug.edc.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(Launching.class);  // UI thread blocking issue, won't run from ANT
		suite.addTestSuite(Terminating.class);  // UI thread blocking issue, won't run from ANT
		//$JUnit-END$
		return suite;
	}

}
