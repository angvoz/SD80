/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Nokia - Initial API and implementation. Feb 28, 2011
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.tests;

import java.util.List;

import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IModuleLineEntryProvider;
import org.eclipse.cdt.debug.edc.symbols.ILineEntryProvider.ILineAddresses;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test finding closest code line.
 * 
 * The executable and source files used in this test can be found at
 * {this_plugin}/resources/Project/inlineFunction/
 */
public class TestFindCodeLine extends BaseDwarfTestCase {

	private static String srcPath;
	private static IModuleLineEntryProvider line_table;

	@BeforeClass
	public static void beforeClass() {
		// This is where I built the program. Change this accordingly if the binary is rebuilt.
		srcPath = "C:/MyWorkspace/sources/austin_cvs/edc/org.eclipse.cdt.debug.edc.tests/resources/Projects/inlineFunction/src/";
		
		IPath binary = getFile("inlineFunction.exe");
		if (! binary.toFile().exists())
			fail("Cannot find data file: " + binary.toOSString());
		
		IEDCSymbolReader reader = Symbols.getSymbolReader(binary);
		
		line_table = reader.getModuleScope().getModuleLineEntryProvider();
	}
	
	@Test
	public void moduleUseSource() {
		String src = srcPath + "main.cpp";
		assertTrue("Source file not found in executable: " + src, line_table.hasSourceFile(new Path(src)));
		
		src = srcPath + "inline_func.h";
		assertTrue("Source file not found in executable: " + src, line_table.hasSourceFile(new Path(src))); // head file, not a CU
		
		// expected failure cases.
		assertFalse(line_table.hasSourceFile(new Path("main.cpp")));
		assertFalse(line_table.hasSourceFile(new Path("/abc/main.cpp")));
		assertFalse(line_table.hasSourceFile(new Path(srcPath + "non-exist.h")));
	}
	
	@Test
	public void findCodeLineInBasicCPPFile() {
		IPath src = new Path(srcPath + "main.cpp");
		
		List<ILineAddresses> code_lines;
		int anchor;

		// line 0: edge case
		anchor = 0;
		code_lines = line_table.findClosestLineWithCode(src, anchor, 0);
		assertEquals(0, code_lines.size());

		// line 31: beyond last line of the file. edge case
		anchor = 31;
		code_lines = line_table.findClosestLineWithCode(src, anchor, 0);
		assertEquals(0, code_lines.size());
		code_lines = line_table.findClosestLineWithCode(src, anchor, 5);
		assertEquals(1, code_lines.size());
		assertEquals(28, code_lines.get(0).getLineNumber());

		// line 19: a line with code
		anchor = 19;
		code_lines = line_table.findClosestLineWithCode(src, anchor, 0);
		assertEquals(1, code_lines.size());
		assertEquals(anchor, code_lines.get(0).getLineNumber());

		// line 21: a line without code (i.e. a blank line or comment line)
		//
		anchor = 21;
		code_lines = line_table.findClosestLineWithCode(src, anchor, 0);
		assertEquals(0, code_lines.size());
		// no code at next door neighbor either.
		code_lines = line_table.findClosestLineWithCode(src, anchor, 1);
		assertEquals(0, code_lines.size());
		// two doors down, we find code line
		code_lines = line_table.findClosestLineWithCode(src, anchor, 2);
		assertEquals(1, code_lines.size());
		assertEquals(anchor+2, code_lines.get(0).getLineNumber());
		
		// line 20: a line without code.
		//
		anchor = 20;
		code_lines = line_table.findClosestLineWithCode(src, anchor, 0);
		assertEquals(0, code_lines.size());
		// code line is next door above.
		code_lines = line_table.findClosestLineWithCode(src, anchor, 1);
		assertEquals(1, code_lines.size());
		assertEquals(anchor-1, code_lines.get(0).getLineNumber());

		// line 14: a line without code
		//
		anchor = 14;
		// no code line within 3 lines
		code_lines = line_table.findClosestLineWithCode(src, anchor, 3);
		assertEquals(0, code_lines.size());
		// there is code line if we search further.
		code_lines = line_table.findClosestLineWithCode(src, anchor, 5);
		assertEquals(1, code_lines.size());
		assertEquals(18, code_lines.get(0).getLineNumber());
	}

	@Test
	public void findCodeLineInBasicInlineFunction() {
		/*
		 * Inline function invoked twice in same cpp file.
		 */
		IPath src = new Path(srcPath + "BasicInline.cpp");
		
		List<ILineAddresses> code_lines;
		int anchor;
		
		// line 12: a line with code in an inline function.
		// Has two addresses mapped to two invocation points.
		anchor = 12;
		code_lines = line_table.findClosestLineWithCode(src, anchor, 0);
		assertEquals(1, code_lines.size());
		assertEquals(anchor, code_lines.get(0).getLineNumber());
		assertEquals(2, code_lines.get(0).getAddress().length);
	}

	@Test
	public void findCodeLineInInlineFunctionFromHeader() 
	{
		/*
		 * Two inline function in a header, each is invoked in
		 * different cpp file.
		 */
		IPath src = new Path(srcPath + "inline_func.h");
		
		List<ILineAddresses> code_lines;
		int anchor;
		
		// line 5: a line with code in an inline function
		// 
		// it has mapped address in compile unit InlineFromHeader_1.cpp, 
		anchor = 5;
		code_lines = line_table.findClosestLineWithCode(src, anchor, 0);
		assertEquals(1, code_lines.size());
		assertEquals(anchor, code_lines.get(0).getLineNumber());
		assertEquals(1, code_lines.get(0).getAddress().length);

		// it has no mapped address in compile unit InlineFromHeader_2.cpp,
		// but a code line can be found at a neighbor far below.
		int neighbor_limit = 8;
		code_lines = line_table.findClosestLineWithCode(src, anchor, neighbor_limit);
		assertEquals(2, code_lines.size());
		assertTrue(anchor == code_lines.get(0).getLineNumber() || // from InlineFromHeader_1.cpp
					11 == code_lines.get(0).getLineNumber()); // from InlineFromHeader_2.cpp
		assertTrue(anchor == code_lines.get(1).getLineNumber() || // from InlineFromHeader_1.cpp
				11 == code_lines.get(1).getLineNumber()); // from InlineFromHeader_2.cpp
	}
}
