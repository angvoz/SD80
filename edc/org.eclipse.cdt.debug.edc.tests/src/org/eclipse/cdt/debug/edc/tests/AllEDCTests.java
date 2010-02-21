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

import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsBasicTypes;
import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsAggregatesAndEnums;
import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsInheritance;
import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsInvalidExpressions;
import org.eclipse.cdt.debug.edc.debugger.tests.SnapshotTests;
import org.eclipse.cdt.debug.edc.debugger.tests.Variables;

//@RunWith(Suite.class)
//@Suite.SuiteClasses( { SymbolReader.class, DisassembleARMBinary.class, DisassembleX86Binary.class,
//		TestDisassemblerARM.class, TestDisassemblerX86.class, TestOpcodeARM.class, Variables.class, 
//		SnapshotTests.class, SnapshotMetaDataTests.class })

public class AllEDCTests {
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Tests for org.eclipse.cdt.debug.edc.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(ExpressionsBasicTypes.class);
		suite.addTestSuite(ExpressionsAggregatesAndEnums.class);
		suite.addTestSuite(ExpressionsInheritance.class);
		suite.addTestSuite(ExpressionsInvalidExpressions.class);
		suite.addTestSuite(SymbolReader.class);
		suite.addTestSuite(TestDwarfReader.class);
		suite.addTestSuite(TestSourceToAddressMapping.class);
		suite.addTestSuite(TestMemoryStreamBuffer.class);
		suite.addTestSuite(TestFileStreamBuffer.class);
		suite.addTestSuite(TestByteBufferStreamBuffer.class);
		
		suite.addTestSuite(DisassembleARMBinary.class);
		suite.addTestSuite(DisassembleX86Binary.class);
		suite.addTestSuite(TestDisassemblerARM.class);
		suite.addTestSuite(TestDisassemblerX86.class);
		
		suite.addTestSuite(TestOpcodeARM.class);
		suite.addTestSuite(Variables.class);
		suite.addTestSuite(SnapshotTests.class);
		suite.addTestSuite(SnapshotMetaDataTests.class);
		//$JUnit-END$
		return suite;
	}
}
