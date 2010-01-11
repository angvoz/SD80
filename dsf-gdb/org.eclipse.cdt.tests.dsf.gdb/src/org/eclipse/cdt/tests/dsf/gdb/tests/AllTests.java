/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is meant to be empty.  It enables us to define
 * the annotations which list all the different JUnit suites we
 * want to run.  When creating a new suite class, it should be
 * added to the list below.
 * 
 * This suite runs the tests with the current version of GDB that is on the PATH
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	LaunchUtilsTest.class,
	MIRegistersTest.class,
	MIRunControlTest.class,
	MIExpressionsTest.class,
	MIMemoryTest.class,
	MIBreakpointsTest.class,
	MIDisassemblyTest.class,
	GDBProcessesTest.class
	/* Add your suite class here */
})

public class AllTests {}