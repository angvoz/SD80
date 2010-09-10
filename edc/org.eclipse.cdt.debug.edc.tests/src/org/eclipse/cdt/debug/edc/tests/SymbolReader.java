/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.debug.edc.internal.HostOS;
import org.eclipse.cdt.debug.edc.internal.PathUtils;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfFileHelper;
import org.eclipse.cdt.debug.edc.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.symbols.ILineEntryProvider;
import org.eclipse.cdt.debug.edc.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IVariable;
import org.eclipse.core.runtime.IPath;
import org.junit.Assert;
import org.junit.Test;

/**
 * Fast check of symbol reader.
 */
public class SymbolReader {
	

	@Test
	public void testFileNameNormalization() {

		IPath path = EDCTestPlugin.getDefault().getPluginFilePath("/resources/SymbolFiles/BlackFlagMinGW.exe");
		
		DwarfFileHelper fileHelper = new DwarfFileHelper(path);
		
		// ignore compDir
		String nname = fileHelper.normalizeFilePath("/dir", "/absolute").toOSString();
		if (HostOS.IS_WIN32)
			// a drive letter from exe file has been prepended, like
			// c:\\absolute
			Assert.assertEquals("\\absolute", nname.substring(2));
		else
			Assert.assertEquals("/absolute", nname);

		// concatenate and remove extra delimiter
		nname = fileHelper.normalizeFilePath("/dir//", "a///B/////c\\\\d").toOSString();
		if (HostOS.IS_WIN32)
			// a drive letter from exe file may have been prepended.
			Assert.assertTrue(nname.endsWith("\\dir\\a\\B\\c\\d"));
		else
			Assert.assertEquals("/dir/a/B/c/d", nname);

		// remove "..", dir without ending separator
		nname = fileHelper.normalizeFilePath("/1/2/3", "..//4//5").toOSString();
		if (HostOS.IS_WIN32)
			Assert.assertTrue(nname.endsWith("\\1\\2\\4\\5"));
		else
			Assert.assertEquals("/1/2/4/5", nname);

		// unify delimiter, dir with ending separator, remove "..".
		nname = fileHelper.normalizeFilePath("\\1/2\\3\\", "..\\4//5").toOSString();
		if (HostOS.IS_WIN32)
			Assert.assertTrue(nname.endsWith("\\1\\2\\4\\5"));
		else
			Assert.assertEquals("/1/2/4/5", nname);

		// Cygwin path #1 conversion.
		nname = fileHelper.normalizeFilePath("/dir/", "/cygdrive/c/a/b").toOSString();
		if (HostOS.IS_WIN32)
			Assert.assertEquals("C:\\a\\b", nname);
		else
			Assert.assertEquals("c:/a/b", nname);

		// Cygwin path #2 conversion.
		nname = fileHelper.normalizeFilePath("/dir/", "//c/a/b").toOSString();
		if (HostOS.IS_WIN32)
			Assert.assertEquals("C:\\a\\b", nname);
		else
			Assert.assertEquals("c:/a/b", nname);
		
		// Cygwin path #3 conversion.
		nname = fileHelper.normalizeFilePath("/dir/", "/D/a/b").toOSString();
		if (HostOS.IS_WIN32)
			Assert.assertEquals("D:\\a\\b", nname);
		else
			Assert.assertEquals("/D/a/b", nname);
	}


	@Test
	public void testQuickParse() {
		// test the quick parsing by just getting a list of the source files
		IEDCSymbolReader reader = Symbols.getSymbolReader(EDCTestPlugin.getDefault().getPluginFilePath(
				"/resources/SymbolFiles/BlackFlagMinGW.exe"));
		Assert.assertEquals(121, reader.getSourceFiles().length);

		reader = Symbols.getSymbolReader(EDCTestPlugin.getDefault().getPluginFilePath(
				"/resources/SymbolFiles/BlackFlag_linuxgcc.exe"));
		Assert.assertEquals(139, reader.getSourceFiles().length);

		reader = Symbols.getSymbolReader(EDCTestPlugin.getDefault().getPluginFilePath(
				"/resources/SymbolFiles/BlackFlag_gcce.sym"));
		Assert.assertEquals(108, reader.getSourceFiles().length);

		reader = Symbols.getSymbolReader(EDCTestPlugin.getDefault().getPluginFilePath(
				"/resources/SymbolFiles/BlackFlag_rvct.sym"));
		
		// TODO: the fixups for paths without drives only happens on Win32,
		// but it seems to introduce duplicates, not actually resolvng
		// all the driveless paths...
		int expectedCount = HostOS.IS_WIN32 ? 207 : 172;
		Assert.assertEquals(expectedCount, reader.getSourceFiles().length);
	}

	@Test
	public void testFullParse() {
		// test the full parse
		IEDCSymbolReader reader = Symbols.getSymbolReader(EDCTestPlugin.getDefault().getPluginFilePath(
				"/resources/SymbolFiles/BlackFlagMinGW.exe"));
		reader.getModuleScope().getChildren();

		reader = Symbols.getSymbolReader(EDCTestPlugin.getDefault().getPluginFilePath(
				"/resources/SymbolFiles/BlackFlag_linuxgcc.exe"));
		reader.getModuleScope().getChildren();

		reader = Symbols.getSymbolReader(EDCTestPlugin.getDefault().getPluginFilePath(
				"/resources/SymbolFiles/BlackFlag_gcce.sym"));
		reader.getModuleScope().getChildren();

		reader = Symbols.getSymbolReader(EDCTestPlugin.getDefault().getPluginFilePath(
				"/resources/SymbolFiles/BlackFlag_rvct.sym"));
		reader.getModuleScope().getChildren();
	}

	@Test
	public void testBlackFlagAllCompilers() {
		IEDCSymbolReader reader = Symbols.getSymbolReader(EDCTestPlugin.getDefault().getPluginFilePath(
				"/resources/SymbolFiles/BlackFlag_gcce.sym"));
		doBlackFlagTest(reader);

		reader = Symbols.getSymbolReader(EDCTestPlugin.getDefault().getPluginFilePath(
				"/resources/SymbolFiles/BlackFlag_rvct.sym"));
		doBlackFlagTest(reader);

		reader = Symbols.getSymbolReader(EDCTestPlugin.getDefault().getPluginFilePath(
				"/resources/SymbolFiles/BlackFlagMinGW.exe"));
		doBlackFlagTest(reader);

		reader = Symbols.getSymbolReader(EDCTestPlugin.getDefault().getPluginFilePath(
				"/resources/SymbolFiles/BlackFlag_linuxgcc.exe"));
		doBlackFlagTest(reader);
	}

	private void doBlackFlagTest(IEDCSymbolReader reader) {
		Collection<IFunctionScope> functions = reader.getModuleScope().getFunctionsByName("dbg_stack_crawl");
		Assert.assertEquals(1, functions.size());

		// test function interface
		IFunctionScope function = functions.iterator().next();
		Assert.assertNotNull(function.getFrameBaseLocation());
		Assert.assertEquals("dbg_stack_crawl", function.getName());
		Assert.assertEquals(0, function.getParameters().size());
		Assert.assertEquals(1, function.getVariablesInTree().size());
		Assert.assertEquals(0, function.getEnumerators().size());
		Assert.assertFalse(function.getLowAddress().isZero());
		Assert.assertFalse(function.getHighAddress().isZero());

		// test variable interface
		IVariable variable = function.getVariablesInTree().iterator().next();
		Assert.assertTrue(variable.getName().equals("x"));
		Assert.assertNotNull(variable.getLocationProvider());
		Assert.assertNotNull(variable.getType());
		Assert.assertEquals("volatile", variable.getType().getName());
		Assert.assertNotNull(variable.getType().getType());
		Assert.assertEquals("int", variable.getType().getType().getName());
		Assert.assertNull(variable.getType().getType().getType());

		// test compile unit interface
		IScope parent = function.getParent();
		Assert.assertNotNull(parent);
		Assert.assertTrue(parent instanceof ICompileUnitScope);
		ICompileUnitScope cu = (ICompileUnitScope) parent;
		Assert.assertTrue(cu.getFilePath().lastSegment(), cu.getFilePath().lastSegment().equalsIgnoreCase("dbg_stack_crawl.cpp"));
		Assert.assertEquals(4, cu.getChildren().size());
		Assert.assertEquals(0, cu.getVariables().size());
		Assert.assertEquals(0, cu.getEnumerators().size());
		Assert.assertFalse(cu.getLowAddress().isZero());
		Assert.assertFalse(cu.getHighAddress().isZero());

		// test line entry interface
		IPath stackCrawlFilePath = null;
		List<IPath> matchingFiles = new ArrayList<IPath>();
		for (String src : reader.getSourceFiles() ) {
			if (src.endsWith("dbg_stack_crawl.cpp")) {
				matchingFiles.add(PathUtils.createPath(src));
			}
		}
		stackCrawlFilePath = matchingFiles.get(matchingFiles.size() - 1);
		Assert.assertNotNull(stackCrawlFilePath);
		Collection<ILineEntryProvider> lineEntryProviders = reader.getModuleScope().getModuleLineEntryProvider().getLineEntryProvidersForFile(
				stackCrawlFilePath);
		Assert.assertEquals(1, lineEntryProviders.size());
		ILineEntryProvider lep = lineEntryProviders.iterator().next();
		
		Collection<ILineEntry> lineEntries = lep.getLineEntriesForLines(stackCrawlFilePath, 31, 31);
		Assert.assertEquals(1, lineEntries.size());
		Assert.assertTrue(lineEntries.iterator().next().getFilePath().lastSegment(),
				lineEntries.iterator().next().getFilePath().lastSegment().equalsIgnoreCase(
				"dbg_stack_crawl.cpp"));
		Assert.assertEquals(31, lineEntries.iterator().next().getLineNumber());
		Assert.assertFalse(lineEntries.iterator().next().getLowAddress().isZero());
		Assert.assertFalse(lineEntries.iterator().next().getHighAddress().isZero());

		ILineEntry nextLine = lep.getNextLineEntry(lineEntries.iterator().next());
		Assert.assertNotNull(nextLine);
		Assert.assertEquals(33, nextLine.getLineNumber());
		Assert.assertFalse(nextLine.getLowAddress().isZero());
		Assert.assertFalse(nextLine.getHighAddress().isZero());

		nextLine = lep.getNextLineEntry(nextLine);
		Assert.assertNotNull(nextLine);
		Assert.assertEquals(34, nextLine.getLineNumber());

		// test past the end of the function
		nextLine = lep.getNextLineEntry(nextLine);
		Assert.assertNull(nextLine);

		// test bogus line
		Assert.assertTrue(lep.getLineEntriesForLines(stackCrawlFilePath, 4000, 4000).isEmpty());

		// test multiple lines
		Assert.assertEquals(2, lep.getLineEntriesForLines(stackCrawlFilePath, 33, 34).size());
		
		// these may have inlined subroutines on different compilers, so the count may differ
		int cnt = lep.getLineEntriesForLines(stackCrawlFilePath, 33, 50).size();
		Assert.assertTrue(cnt + "", cnt == 2 || cnt == 6);
		cnt = lep.getLineEntriesForLines(stackCrawlFilePath, 33, -1).size();
		Assert.assertTrue(cnt + "", cnt == 2 || cnt == 6);

		// test module scope interface
		parent = cu.getParent();
		Assert.assertNotNull(parent);
		Assert.assertTrue(parent instanceof IModuleScope);
		IModuleScope module = (IModuleScope) parent;
		Assert.assertFalse(module.getLowAddress().isZero());
		Assert.assertFalse(module.getHighAddress().isZero());

		Collection<IVariable> variables = reader.getModuleScope().getVariablesByName("gstring", false);
		Assert.assertEquals(1, variables.size());
		Assert.assertTrue(variables.iterator().next().getScope() instanceof IModuleScope);
		Assert.assertNotNull(variables.iterator().next().getLocationProvider());
		Assert.assertTrue(variables.iterator().next().getType() instanceof IPointerType);
	}

}
