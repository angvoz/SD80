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

import org.eclipse.cdt.debug.edc.debugger.tests.Concurrent;
import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsAggregatesAndEnums;
import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsBasicTypes;
import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsCasting;
import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsCasting2;
import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsInheritance;
import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsInvalidExpressions;
import org.eclipse.cdt.debug.edc.debugger.tests.OpaqueTypeResolving;
import org.eclipse.cdt.debug.edc.debugger.tests.RegisterFrameTests;
import org.eclipse.cdt.debug.edc.debugger.tests.RegisterFrameTestsBlackFlag;
import org.eclipse.cdt.debug.edc.debugger.tests.RegisterFrameTestsBlackFlagRVCT;
import org.eclipse.cdt.debug.edc.debugger.tests.RegisterFrameTestsLinux;
import org.eclipse.cdt.debug.edc.debugger.tests.RegisterView;
import org.eclipse.cdt.debug.edc.debugger.tests.SnapshotTests;
import org.eclipse.cdt.debug.edc.system.tests.K9SystemViewTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {  
	Concurrent.class,
	DisassembleARMBinary.class,
	DisassembleX86Binary.class,
	ExpressionsAggregatesAndEnums.class,
	ExpressionsBasicTypes.class,
	ExpressionsCasting.class,
	ExpressionsCasting2.class,
	ExpressionsInheritance.class,
	ExpressionsInvalidExpressions.class,
	K9SystemViewTest.class,
	OpaqueTypeResolving.class,
	RegisterFrameTests.class,
	RegisterFrameTestsBlackFlag.class,
	RegisterFrameTestsBlackFlagRVCT.class,
	RegisterFrameTestsLinux.class,
	RegisterView.class,
	SnapshotMetaDataTests.class, 
	SnapshotTests.class,
	SymbolReader.class,
	TestAgentUtils.class,
	TestByteBufferStreamBuffer.class,
	TestDwarfReader.class,
	TestDisassemblerARM.class,
	TestDisassemblerX86.class,
	TestExecutableReader.class,
	TestFileStreamBuffer.class,
	TestFindCodeLine.class,
	TestMemoryStreamBuffer.class,
	TestOpcodeARM.class,
	TestSourceToAddressMapping.class,
	TestUnmanglerEABI.class,
})

public class AllEDCTests {
}
