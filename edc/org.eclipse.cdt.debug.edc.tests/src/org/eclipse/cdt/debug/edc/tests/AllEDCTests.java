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

import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsBasicTypes;
import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsAggregatesAndEnums;
import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsInheritance;
import org.eclipse.cdt.debug.edc.debugger.tests.ExpressionsInvalidExpressions;
import org.eclipse.cdt.debug.edc.debugger.tests.SnapshotTests;
import org.eclipse.cdt.debug.edc.debugger.tests.Variables;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {  ExpressionsBasicTypes.class,
						ExpressionsAggregatesAndEnums.class,
						ExpressionsInheritance.class,
						ExpressionsInvalidExpressions.class,
						SymbolReader.class,
						TestDwarfReader.class,
						TestSourceToAddressMapping.class,
						TestMemoryStreamBuffer.class,
						TestFileStreamBuffer.class,
						TestByteBufferStreamBuffer.class,
						TestUnmanglerEABI.class,
						TestExecutableReader.class,
						DisassembleARMBinary.class,
						DisassembleX86Binary.class,
						TestDisassemblerARM.class,
						TestDisassemblerX86.class,
						TestOpcodeARM.class,
						Variables.class,
						SnapshotTests.class,
						SnapshotMetaDataTests.class })

public class AllEDCTests {
}
