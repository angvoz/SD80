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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler.IDisassemblerOptions;
import org.eclipse.cdt.debug.edc.internal.arm.disassembler.DisassemblerARM;
import org.eclipse.cdt.debug.edc.internal.arm.disassembler.InstructionParserARM;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.cdt.utils.elf.Elf.Section;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Disassemble any specified ARM binary file.
 */
public class DisassembleARMBinary {

	static Map<String, Object> sOptions = null;
	static DisassemblerARM sDisassembler;
	static String[] sSectionsToDisassemble;
	static boolean sOutputOnlyAsm; // no address, raw bytes, nor extra text.
	private static boolean sAddSectionTitle;

	private String fOutputFileName = "ARMDisassemblerOutput.txt";

	private String[] fDataFileNames;

	private PrintStream fOutputFileStream = null;
	
	@BeforeClass
	public static void beforeClass() {
		sDisassembler = new DisassemblerARM(null);

		sSectionsToDisassemble = new String[] { ".init", ".plt", ".text", ".fini" };

		sOutputOnlyAsm = false;
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

			if (fname.endsWith("thumb.o")) {
				sOptions.put(DisassemblerARM.IDisassemblerOptionsARM.DISASSEMBLER_MODE,
						InstructionParserARM.DISASSEMBLER_MODE_THUMB);
			} else {
				sOptions.put(DisassemblerARM.IDisassemblerOptionsARM.DISASSEMBLER_MODE,
						InstructionParserARM.DISASSEMBLER_MODE_ARM);
			}
			Elf elf = null;
			try {
				elf = new Elf(fname);
			} catch (IOException e) {
				System.out.println("File is not ELF file: " + fname);
				continue;
			}

			System.out.println("disassembling " + fname);
			fOutputFileStream.println("Start : " + fname);

			Attribute attrib = elf.getAttributes();
			if (attrib.isLittleEndian()) {
				sOptions.put(DisassemblerARM.IDisassemblerOptionsARM.ENDIAN_MODE,
						InstructionParserARM.LITTLE_ENDIAN_MODE);
			} else {
				sOptions.put(DisassemblerARM.IDisassemblerOptionsARM.ENDIAN_MODE, InstructionParserARM.BIG_ENDIAN_MODE);
			}
			for (String secName : sSectionsToDisassemble) {
				Section sec = elf.getSectionByName(secName);
				if (sec == null) {
					continue;
				}

				if (sAddSectionTitle)
					fOutputFileStream.println("\nDisassembly of section " + secName + ":\n");

				// Currently we just run the disassembler and catch any error
				// or exception it throws. As to correctness of the output,
				// let's use an ARM assembler (e.g. gnu as) to do that.

				// disassemble(sec.sh_addr, sec.mapSectionData(), null);
				disassemble1by1(sec.sh_addr, sec.mapSectionData());
			}

			fOutputFileStream.println("End : " + fname);
		}
	}

	// Get data files to test
	//
	private String[] getDataFileNames() throws Exception {
		String[] names = null;

		ArrayList<String> list = new ArrayList<String>(10);

		// To add individual data files...
		//
		// list.add("/mydisk/myprog/cpp/examples/div0");
		// list.add("/bin/ls");
		// list.add("/usr/bin/vi");
		// list.add("/usr/bin/nautilus");
		// list.add("/usr/bin/gdb");

		// To add files under one folder...
		//
		// All all binary files under resources folder in the project.
		String res_folder = EDCTestPlugin.projectRelativePath("resources/ARM");
		list.addAll(Arrays.asList(TestUtils.getFileFullNamesByExtension(res_folder, ".o")));

		// list.addAll(Arrays.asList(TestUtils.getFileFullNamesByExtension(
		// "/usr/bin", "")));

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
				inst = sDisassembler.disassembleOneInstruction(address, codeBuffer, sOptions, null);
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
