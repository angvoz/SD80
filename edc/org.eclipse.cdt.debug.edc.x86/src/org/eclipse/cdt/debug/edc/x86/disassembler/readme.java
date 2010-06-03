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

package org.eclipse.cdt.debug.edc.x86.disassembler;

/**
 * Status of the x86 disassembler
 * <p>
 * The disassembler is not meant to be a full fledged x86 disassembler. Its
 * initial purpose is to offer disassembly support for EDC debugger for x86
 * platform which will use it in areas like stepping and (perhaps) stack
 * crawling.
 * </p>
 * 
 * --------------------------------------------------<br>
 * <b>How to add new instruction</b><br>
 * --------------------------------------------------
 * 
 * <p>
 * The class {@link OpcodeX86} is supposed to contain definition of instructions
 * from opcode map in Intel developer manual Vol 3B. Add any new instruction
 * there. Then add a test case in
 * 
 * {@link org.eclipse.cdt.debug.edc.tests.TestDisassemblerX86}
 * 
 * <p>
 * New addressing method (things like "Ev", "Ap" etc) should be added in class
 * {@link InstructionParserX86}.
 * 
 * <p>
 * --------------------------------------------------<br>
 * <b>Status as of July 22 2009 </b><br>
 * --------------------------------------------------
 * 
 * <p>
 * 1. The disassembler only supports 32-bit address mode, no 16 or 64-bit
 * address mode, though any one who has interest can extend it.
 * 
 * <p>
 * 2. Only AT&T syntax is supported. See class AssemblyFormatter for more.
 * 
 * <p>
 * 3. All general-purpose instructions and FPU (x87) instructions are supported.
 * Instructions for MMX, SSE are not supported yet, though again any one
 * interested should be able to add new instruction support with ease.
 * 
 * <p>
 * 4. So far I've been able to disassemble "vi" and "gdb" binaries on 32-bit
 * Ubuntu Linux and feed the output to GNU assembler without getting any error.
 * <p>
 * --------------------------------------------------<br>
 * <b>References </b><br>
 * --------------------------------------------------
 * </p>
 * <ul>
 * <li><a href="http://www.intel.com/products/processor/manuals/">Intel 64 and
 * IA-32 Architectures Software Developer's Manuals</a></li>
 * <li><a href="http://en.wikipedia.org/wiki/X86_assembly_language">x86 assembly
 * language - Wikipedia</a></li>
 * <li><a href="http://siyobik.info/index.php?module=x86">x86 Instruction Set
 * Reference</a></li>
 * </ul>
 */
class readme {
	// this is a dummy class just for holding the readme info so that we can use
	// javadoc features such as hyperlinks to code or URL, spell checking and
	// format.
}