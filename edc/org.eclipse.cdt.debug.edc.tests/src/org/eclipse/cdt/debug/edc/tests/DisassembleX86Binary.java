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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler.IDisassemblerOptions;
import org.eclipse.cdt.debug.edc.x86.disassembler.DisassemblerX86;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.Section;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Disassemble any specified x86 binary file.
 */
public class DisassembleX86Binary {

	static Map<String, Object> sOptions = null;
	static DisassemblerX86 sDisassembler;
	static String[] sSectionsToDisassemble;
	static boolean sOutputOnlyAsm; // no address, raw bytes, nor extra text.
	private static boolean sAddSectionTitle;

	private String fOutputFileName = "DisassemblerOutput.txt";

	private String[] fDataFileNames;

	private PrintStream fOutputFileStream = null;

	@BeforeClass
	public static void beforeClass() {
		sDisassembler = new DisassemblerX86();

		sSectionsToDisassemble = new String[] { ".init", ".plt", ".text", ".fini" };

		sOutputOnlyAsm = true;
		sAddSectionTitle = false;
	}

	@Before
	public void setUp() throws Exception {
		
		fDataFileNames = getDataFileNames();

		if (fOutputFileStream == null) {
			File outputFile = File.createTempFile(fOutputFileName, null);
			fOutputFileStream = new PrintStream(new FileOutputStream(outputFile, false /*
																						 * append?
																						 */));
		}
	}

	@After
	public void tearDown() throws Exception {
		fOutputFileStream.close();
	}

	@Test
	public void testDisassembleFiles() throws IOException {
		
		/*
		 * set up common disassembler options.
		 */
		sOptions = new HashMap<String, Object>();
		if (!sOutputOnlyAsm) {
			sOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_ADDRESS, true);
			sOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_BYTES, true);
		}

		for (String fname : fDataFileNames) {
			File f = new File(fname);
			if (f == null || !f.exists())
				Assert.fail("Data file not found: " + f.getAbsolutePath());

			Elf elf = null;
			try {
				elf = new Elf(fname);
			} catch (IOException e) {
				System.out.println("File is not ELF file: " + fname);
				continue;
			}

			System.out.println("disassembling " + fname);

			for (String secName : sSectionsToDisassemble) {
				Section sec = elf.getSectionByName(secName);
				Assert.assertNotNull(sec);

				if (sAddSectionTitle)
					fOutputFileStream.println("\nDisassembly of section " + secName + ":\n");

				// Currently we just run the disassembler and catch any error
				// or exception it throws. As to correctness of the output,
				// let's use an x86 assembler (e.g. gnu as) to do that.

				// disassemble(sec.sh_addr, sec.mapSectionData(), null);
				disassemble1by1(sec.sh_addr, sec.mapSectionData());
			}
		}
	}

	// Get data files to test
	//
	private String[] getDataFileNames() throws Exception {
		String[] names = null;

		ArrayList<String> list = new ArrayList<String>(10);

		// To add individual data files...
		//
		// list.add("/bin/ls");
		// list.add("/usr/bin/gdb");

		// To add files under one folder...
		//
		// All all binary files under resources folder in the project.
//		String res_folder = EDCTestPlugin.projectRelativePath("resources/symbolFiles");
//		list.addAll(Arrays.asList(TestUtils.getFileFullNamesByExtension(res_folder, "")));

		list.add(EDCTestPlugin.projectRelativePath("resources/SymbolFiles/ls"));
		list.add(EDCTestPlugin.projectRelativePath("resources/SymbolFiles/vi"));
		
		names = new String[list.size()];
		list.toArray(names);

		return names;
	}

	private void disassemble1by1(IAddress addr, ByteBuffer codeBuffer) {
		IAddress address = addr;
		IDisassembledInstruction inst = null;
		int instSize = 0;

		while (codeBuffer.hasRemaining()) {
			try {
				inst = sDisassembler.disassembleOneInstruction(address, codeBuffer, sOptions);
				instSize += inst.getSize();

				fOutputFileStream.println(inst.getMnemonics());

				// next instruction address
				// Note at this point the current position of code buffer should
				// point to the next instruction.
				address = address.add(inst.getSize());

			} catch (CoreException e) {
				Assert.fail("Fail to disassemble instruction at " + address.toHexAddressString() + "\nCause: "
						+ e.getLocalizedMessage());
			}
		}

		Assert.assertEquals(codeBuffer.capacity(), instSize);
	}

}
