/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.index.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for the indexer tests
 */
public class IndexTests extends TestSuite {

	public static Test suite() {
		TestSuite suite = new IndexTests();
		suite.addTest(IndexListenerTest.suite());
		suite.addTest(IndexSearchTest.suite());
		return suite;
	}
	
}
