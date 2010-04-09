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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.edc.internal.PathUtils;
import org.eclipse.cdt.debug.edc.internal.services.dsf.LineEntryMapper;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.symbols.IModuleLineEntryProvider;
import org.eclipse.cdt.dsf.debug.service.IModules.AddressRange;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

/**
 * Test use of the IEDCSymbolReader to map source file/line to addresses.
 */
public class TestSourceToAddressMapping extends BaseDwarfTestCase {
	protected static final String[] symFilesToTest = {
		"SimpleCpp_rvct_22.sym",
		"SimpleCpp_rvct_40.sym",
		"SimpleCpp_gcce_432.sym",
		"SimpleCpp_gcc_x86.exe",
		
		"BlackFlagMinGW.exe",
		"BlackFlag_gcce.sym",
		"BlackFlag_linuxgcc.exe",
		"BlackFlag_rvct.sym",
		
		"HelloWorld_rvct_2_2.exe.sym",
		"HelloWorld_rvct_4_0.exe.sym",
	};

	/** Bag of data for testing sym files.  The key is 'symFile' and other
	 * elements are used by specific tests.
	 */
	protected static class TestInfo {
		IPath symFile;
		Map<LineInfo, List<String>> lineToFunctionMap = new LinkedHashMap<LineInfo, List<String>>();
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
	
	protected static class LineInfo {
		public LineInfo(IPath srcFile, int line) {
			sourceFile = srcFile;
			this.line = line;
		}
		IPath sourceFile;
		int line;
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return sourceFile.lastSegment() +":" + line;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + line;
			result = prime * result
					+ ((sourceFile == null) ? 0 : sourceFile.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LineInfo other = (LineInfo) obj;
			if (line != other.line)
				return false;
			if (sourceFile == null) {
				if (other.sourceFile != null)
					return false;
			} else if (!sourceFile.equals(other.sourceFile))
				return false;
			return true;
		}
		
		
	}
	
	protected static void registerFunctionMapping(String symFileRx, String srcFile, int line, String function) {
		for (TestInfo info : testInfos.values()) {
			if (info.symFile.lastSegment().matches(symFileRx)) {
				IPath srcFilePath = new Path(srcFile);
				LineInfo l = new LineInfo(srcFilePath, line);
				List<String> funcs = info.lineToFunctionMap.get(l);
				if (funcs == null) {
					funcs = new ArrayList<String>();
					info.lineToFunctionMap.put(l, funcs);
				}
				funcs.add(function);
			}
		}
	}
	
	static {
		registerFunctionMapping("SimpleCpp.*", "src/SimpleCpp.cpp", 19, "doit");	// decl line
		registerFunctionMapping("SimpleCpp.*", "src/SimpleCpp.cpp", 25, "doit");	// return line
		registerFunctionMapping("SimpleCpp.*", "src/SimpleCpp.cpp", 25, "length");	// inline site
		
		registerFunctionMapping("SimpleCpp.*", "inc/Templates.h", 14, "List");	// decl line
	
		// templates are defined in one place but expanded a lot, especially as inlined functions
		registerFunctionMapping("SimpleCpp_gcce_432.sym", "inc/Templates.h", 15, "List");	// open brace
		registerFunctionMapping("SimpleCpp_rvct_22.sym", "inc/Templates.h", 15, "List");	// open brace
		registerFunctionMapping("SimpleCpp_rvct_40.sym", "inc/Templates.h", 15, "List");	// open brace
		registerFunctionMapping("SimpleCpp_gcce_432.sym", "inc/Templates.h", 15, "makelist");	// when inlined
		registerFunctionMapping("SimpleCpp_rvct_22.sym", "inc/Templates.h", 15, "makelist");	// when inlined
		registerFunctionMapping("SimpleCpp_rvct_490.sym", "inc/Templates.h", 15, "makelist");	// when inlined
		
		// GCC 4.x moves the code to the brace
		registerFunctionMapping("SimpleCpp_gcc_x86.sym", "inc/Templates.h", 16, "List");	// open brace
		registerFunctionMapping("SimpleCpp_gcc_x86.sym", "inc/Templates.h", 16, "makelist");	// when inlined
		
		registerFunctionMapping("SimpleCpp.*", "inc/Templates.h", 17, "length");	// decl line
		registerFunctionMapping("SimpleCpp.*", "inc/Templates.h", 18, "length");	// return line
		registerFunctionMapping("SimpleCpp.*", "inc/Templates.h", 18, "doit");	// inline site
		registerFunctionMapping("SimpleCpp.*", "inc/Templates.h", 21, "operator[]");	// decl line
		registerFunctionMapping("SimpleCpp.*", "inc/Templates.h", 21, "makelist");	// inlined here
		registerFunctionMapping("SimpleCpp.*", "inc/Templates.h", 30, "add");	// decl line
		registerFunctionMapping("SimpleCpp.*", "inc/Templates.h", 43, "add");	// last code line
		
		// this one is tricky: the header may reference the subprogram, but
		// the content is placed in the cpp file.
		registerFunctionMapping("SimpleCpp.*", "inc/mycode.h", 11, "myfunc");	// decl line
		registerFunctionMapping("SimpleCpp.*", "inc/mycode.h", 14, "myfunc");	// assignment
		registerFunctionMapping("SimpleCpp.*", "inc/mycode.h", 16, "myfunc");	// final }
	}
	
	@Test
	public void testLineLookupRVCT22() throws Exception {
		doTestLineLookup(lookupInfo("SimpleCpp_rvct_22.sym"));
	}

	@Test
	public void testLineLookupRVCT40() throws Exception {
		doTestLineLookup(lookupInfo("SimpleCpp_rvct_40.sym"));
	}
	
	@Test
	public void testLineLookupGCCE432() throws Exception {
		doTestLineLookup(lookupInfo("SimpleCpp_gcce_432.sym"));
	}
	
	@Test
	public void testLineLookupGCCLinux() throws Exception {
		doTestLineLookup(lookupInfo("SimpleCpp_gcc_x86.exe"));
	}

	/**
	 * @param reader
	 * @param sourceFile
	 * @return
	 */
	private IPath findSource(IEDCSymbolReader reader, IPath sourceFile) {
		for (String file : reader.getSourceFiles()) {
			IPath path = PathUtils.createPath(file);
			boolean matched = false;
			loop: for (int i = 0; i <sourceFile.segmentCount(); i++) {
				String pathSeg = path.segment(path.segmentCount() - i - 1);
				String sourceSeg = sourceFile.segment(sourceFile.segmentCount() - i - 1);
				if (pathSeg == null)
					break loop;
				if ( !pathSeg.equals(sourceSeg))
					break;
				matched = true;
			}
			if (matched)
				return path;
		}
		
		return sourceFile;
	}

	private void doTestLineLookup(TestInfo info) throws Exception {
		IEDCSymbolReader reader = Symbols.getSymbolReader(info.symFile);
		assertNotNull(reader);
		
		IModuleLineEntryProvider moduleLineEntryProvider = reader.getModuleScope().getModuleLineEntryProvider();
		assertNotNull(moduleLineEntryProvider);
		
		StringBuilder errors = new StringBuilder();
		for (Map.Entry<LineInfo, List<String>> entry : info.lineToFunctionMap.entrySet()) {
			try {
				checkLineMapping(info, reader, moduleLineEntryProvider, entry.getKey(), entry.getValue());
			} catch (AssertionError e) {
				errors.append(e.getMessage());
				errors.append('\n');
			}
		}
		if (errors.length() > 0)
			fail(errors.toString());
	}
	
	/**
	 * Check that we can find address information for the given line,
	 * and then map that to a real function.  These are two separate tasks.
	 * @param info
	 * @param reader
	 * @param moduleLineEntryProvider
	 * @param entry
	 */
	private void checkLineMapping(TestInfo info, IEDCSymbolReader reader,
			IModuleLineEntryProvider moduleLineEntryProvider,
			LineInfo lineinfo, List<String> funcNames) {
		String curinfo = info.symFile.lastSegment() + ":" + lineinfo.toString();
		
		// find the actual filename
		IPath lookupPath = findSource(reader, lineinfo.sourceFile);
		
		Collection<AddressRange> addresses = LineEntryMapper.getAddressRangesAtSource(
				moduleLineEntryProvider, 
				lookupPath, lineinfo.line);
		assertNotNull(addresses);
		assertTrue(curinfo, addresses.size() > 0);
		
		List<String> functionsFound = new ArrayList<String>();
		
		// make sure duplicates don't exist
		Set<AddressRange> foundRanges = new HashSet<AddressRange>();
		for (AddressRange range : addresses) {
			assertFalse(range+"", foundRanges.contains(range));
			foundRanges.add(range);

			checkFunction(reader, range, curinfo, functionsFound);
		}
		
		StringBuilder foundString = new StringBuilder();
		for (String func : functionsFound) {
			if (foundString.length() > 0)
				foundString.append(',');
			foundString.append(func);
		}
		StringBuilder wantedString = new StringBuilder();
		boolean found = false;
		for (String name : funcNames) {
			if (wantedString.length() > 0)
				wantedString.append(',');
			wantedString.append(name);
			if (functionsFound.contains(name)) {
				found = true;
			}
		}
		assertTrue("address range did not match anything expected at " + curinfo + ": wanted " + wantedString+" but got " + foundString, found);
	}

	/**
	 * Make sure the address maps to a function, and that the function is one listed in funcNames
	 * @param reader 
	 * @param range
	 * @param info 
	 * @param functionsFound 
	 */
	private void checkFunction(IEDCSymbolReader reader, AddressRange range, String info, List<String> functionsFound) {
		ICompileUnitScope cu = reader.getModuleScope().getCompileUnitForAddress(range.getStartAddress());
		// TODO: when we add line entries for _decl_file/_decl_line/etc., we may point to an
		// address currently outside the expected scope of a module (which is currently built only on
		// the functions it is known to contain).  Thus, cu may be null here in some cases.
		// The debugger doesn't care about the CU, though.
		if (cu != null) {
			assertNotNull(info, cu);
			
			IFunctionScope function = cu.getFunctionAtAddress(range.getStartAddress());
			assertNotNull(info, function);
			
			functionsFound.add(function.getName());
		}
	}
	
	/**
	 * Make sure we find multiple ranges for lines when multiple statements live there
	 * @throws Exception
	 */
	@Test
	public void testMultiRangeLines() throws Exception {
		TestInfo info = lookupInfo("SimpleCpp_gcce_432.sym");
		
		IEDCSymbolReader reader = Symbols.getSymbolReader(info.symFile);
		assertNotNull(reader);
		
		IModuleLineEntryProvider moduleLineEntryProvider = reader.getModuleScope().getModuleLineEntryProvider();
		assertNotNull(moduleLineEntryProvider);
		
		IPath lookupPath = findSource(reader, new Path("inc/Templates.h"));
		
		// "for", init, test for two template instances
		Collection<ILineEntry> entries = moduleLineEntryProvider.getLineEntriesForLines(lookupPath, 37, 37);
		assertEquals(4, entries.size());
	}
}
