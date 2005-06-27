/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author jcamelon
 *
 */
public class DOMGCCParserExtensionTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite= new TestSuite(DOMGCCParserExtensionTestSuite.class.getName());
//		suite.addTestSuite( GCCScannerExtensionsTest.class );
//		suite.addTestSuite( GCCQuickParseExtensionsTest.class );
//		suite.addTestSuite( GCCCompleteParseExtensionsTest.class );
		suite.addTestSuite( DOMGCCSelectionParseExtensionsTest.class);
		return suite;
	}
	
}
