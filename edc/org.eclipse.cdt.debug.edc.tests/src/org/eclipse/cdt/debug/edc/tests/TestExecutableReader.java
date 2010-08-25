/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of the License "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/

package org.eclipse.cdt.debug.edc.tests;


import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.ISymbol;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.core.runtime.IPath;
import org.junit.Test;

/**
 * Note: several tests are implied by TestDwarfReader
 */
public class TestExecutableReader extends BaseDwarfTestCase {
	protected static final String[] symFilesToTest = {
		"BlackFlagMinGW.exe",
		"BlackFlag_gcce.sym",
		"BlackFlag_linuxgcc.exe",
		"BlackFlag_rvct.sym",
		"BlackFlag_gcce_343.sym",
		"HelloWorld_rvct_2_2.exe.sym",
		"HelloWorld_rvct_4_0.exe.sym",
		"SimpleCpp_rvct_22.sym",
		"SimpleCpp_rvct_40.sym",
		"SimpleCpp_gcce_432.sym",
		"SimpleCpp_gcc_x86.exe",
	};

	/** Bag of data for testing sym files.  The key is 'symFile' and other
	 * elements are used by specific tests.
	 */
	protected static class TestInfo {
		IPath symFile;
		IPath exeFile;
		Map<String, SymLookupInfo> symToFuncAddrMap = new HashMap<String,SymLookupInfo>();
	}

	protected  static Map<String, TestInfo> testInfos = new LinkedHashMap<String, TestInfo>();
	
	static {
		for (String sym : symFilesToTest) {
			TestInfo info = new TestInfo();
			info.symFile = getFile(sym);
			testInfos.put(sym, info);
		}
	}
	
	protected  static TestInfo lookupInfo(String sym) {
		return testInfos.get(sym);
	}
	
	protected  static void setExe(String sym, String exe) {
		TestInfo info = lookupInfo(sym);
		if (info != null)
			info.exeFile = getFile(exe);
	}
	
	
	static class SymLookupInfo {
		boolean isMangled;
		int addr;
		@Override
		public String toString() {
			return "mangled="+isMangled+"; addr="+Integer.toHexString(addr);
		}
	}
	
	private static void addSymbolAddr(String sym, String symbol, boolean isMangled, int addr) {
		TestInfo info = lookupInfo(sym);
		if (info != null) {
			SymLookupInfo si = new SymLookupInfo();
			si.isMangled = isMangled;
			si.addr = addr;
			info.symToFuncAddrMap.put(symbol, si);
		}
	}
	static {
		addSymbolAddr("BlackFlagMinGW.exe", "_mainCRTStartup", true, 0x401130);
		addSymbolAddr("BlackFlagMinGW.exe", "mainCRTStartup", false, 0x401130);
		addSymbolAddr("BlackFlagMinGW.exe", "_main", true, 0x40663d);
		addSymbolAddr("BlackFlagMinGW.exe", "_main", false, 0x40663d);
		addSymbolAddr("BlackFlagMinGW.exe", "main", false, 0x40663d);
		addSymbolAddr("BlackFlag_gcce.sym", "_Z7E32Mainv", true, 0x80a0);
		addSymbolAddr("BlackFlag_gcce.sym", "E32Main", false, 0x80a0);
		addSymbolAddr("BlackFlag_gcce.sym", "::E32Main", false, 0x80a0);
		addSymbolAddr("BlackFlag_gcce.sym", "E32Main()", false, 0x80a0);
		addSymbolAddr("BlackFlag_linuxgcc.exe", "main", false, 0x8048880);
		addSymbolAddr("BlackFlag_rvct.sym", "_Z7E32Mainv", true, 0x849c);
		addSymbolAddr("BlackFlag_rvct.sym", "E32Main", false, 0x849c);
		addSymbolAddr("BlackFlag_rvct.sym", "E32Main()", false, 0x849c);
		addSymbolAddr("BlackFlag_rvct.sym", "::E32Main()", false, 0x849c);
		addSymbolAddr("BlackFlag_rvct.sym", "::E32Main", false, 0x849c);
		addSymbolAddr("HelloWorld_rvct_2_2.exe.sym", "_E32Startup", true, 0x8000);
		addSymbolAddr("HelloWorld_rvct_2_2.exe.sym", "_E32Startup", false, 0x8000);
		addSymbolAddr("HelloWorld_rvct_2_2.exe.sym", "::_E32Startup", false, 0x8000);
		addSymbolAddr("HelloWorld_rvct_4_0.exe.sym", "_Z7E32Mainv", true, 0x82ea);
		addSymbolAddr("HelloWorld_rvct_4_0.exe.sym", "E32Main", false, 0x82ea);
		addSymbolAddr("HelloWorld_rvct_4_0.exe.sym", "::E32Main()", false, 0x82ea);
	}

	
	@Test
	public void testLookupOfSymbol() throws Exception {
		for (TestInfo info : testInfos.values()) {
			if (info.symToFuncAddrMap == null) continue;
			IEDCSymbolReader reader = Symbols.getSymbolReader(info.symFile);
			for (Map.Entry<String, SymLookupInfo> entry : info.symToFuncAddrMap.entrySet()) {
				SymLookupInfo si = entry.getValue();
				String label = info.symFile.lastSegment() + ":" + entry.getKey() + ":" + si;
				doLookupSymbol(reader, entry, si, label);
			}
		}
	}

	/**
	 * @param reader
	 * @param entry
	 * @param si
	 * @param label
	 */
	private void doLookupSymbol(IEDCSymbolReader reader,
			Map.Entry<String, SymLookupInfo> entry, SymLookupInfo si,
			String label) {
		Collection<ISymbol> symbols;
		if (!si.isMangled) {
			// should find after unmangling
			symbols = reader.findUnmangledSymbols(entry.getKey());
			assertEquals(label, 1, symbols.size());
		} else {
			// should match mangled symbol
			symbols = reader.findSymbols(entry.getKey());
			assertEquals(label, 1, symbols.size());
			
		}
		assertEquals(label, new Addr32(si.addr),
				symbols.iterator().next().getAddress());
	}
}
