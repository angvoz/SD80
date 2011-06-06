/*******************************************************************************
 * Copyright (c) 2007, 2009 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.build.core.scannerconfig.tests;

import org.eclipse.cdt.build.core.scannerconfig.tests.GCCBuiltinSpecsDetectorTest;

import junit.framework.TestSuite;

public class AllSD80Tests extends TestSuite {

	public static TestSuite suite() {
		return new AllSD80Tests();
	}

	public AllSD80Tests() {
		super(AllSD80Tests.class.getName());

		addTestSuite(GCCBuiltinSpecsDetectorTest.class);
	}
}
