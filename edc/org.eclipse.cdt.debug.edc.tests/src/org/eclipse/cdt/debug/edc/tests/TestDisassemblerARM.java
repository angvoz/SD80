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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IJumpToAddress;
import org.eclipse.cdt.debug.edc.JumpToAddress;
import org.eclipse.cdt.debug.edc.disassembler.CodeBufferUnderflowException;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler.IDisassemblerOptions;
import org.eclipse.cdt.debug.edc.internal.arm.disassembler.DisassemblerARM;
import org.eclipse.cdt.debug.edc.internal.arm.disassembler.DisassemblerARM.IDisassemblerOptionsARM;
import org.eclipse.cdt.debug.edc.internal.arm.disassembler.InstructionParserARM;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for ARM disassembler.
 */
public class TestDisassemblerARM {

	static Map<String, Object> armOptions = null;
	static Map<String, Object> thumbOptions = null;
	static DisassemblerARM sDisassembler;

	/**
	 * Set up.
	 */
	@BeforeClass
	public static void beforeClass() {
		/*
		 * set up common disassembler options.
		 */
		armOptions = new HashMap<String, Object>();
		armOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_ADDRESS, true);
		armOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_BYTES, true);
		armOptions.put(IDisassemblerOptionsARM.DISASSEMBLER_MODE, InstructionParserARM.DISASSEMBLER_MODE_ARM);
		armOptions.put(IDisassemblerOptionsARM.ENDIAN_MODE, InstructionParserARM.BIG_ENDIAN_MODE);


		thumbOptions = new HashMap<String, Object>();
		thumbOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_ADDRESS, true);
		thumbOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_BYTES, true);
		thumbOptions.put(IDisassemblerOptionsARM.DISASSEMBLER_MODE, InstructionParserARM.DISASSEMBLER_MODE_THUMB);
		thumbOptions.put(IDisassemblerOptionsARM.ENDIAN_MODE, InstructionParserARM.BIG_ENDIAN_MODE);


		sDisassembler = new DisassemblerARM(null);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test for non-VFP, 32-bit ARM v4*, v5T*, v6*, v7  instructions.
	 */
	@Test
	public void testArmInstructions() {

		System.out.println("\n===================== ARMv5 ========================\n");
		String[] insts = {
				"E7 F1 23 F4", "undefined",
				"E7 F0 00 10", "undefined",
				"02 AA 51 FE", "adceq	r5,r10,#0x8000003f",
				"00 A9 50 0A", "adceq	r5,r9,r10",
				"00 A9 56 5A", "adceq	r5,r9,r10,asr r6",
				"E0 A9 56 3A", "adc	r5,r9,r10,lsr r6",
				"02 BA 50 71", "adcseq	r5,r10,#0x71",
				"00 B9 50 0A", "adcseq	r5,r9,r10",
				"00 B9 56 1A", "adcseq	r5,r9,r10,lsl r6",
				"E0 B9 56 7A", "adcs	r5,r9,r10,ror r6",
				"02 8F 50 71", "addeq	r5,pc,#0x71",
				"02 8A 51 FE", "addeq	r5,r10,#0x8000003f",
				"00 89 50 0A", "addeq	r5,r9,r10",
				"00 89 50 CA", "addeq	r5,r9,r10,asr #1",
				"E0 89 56 5A", "add	r5,r9,r10,asr r6",
				"E0 89 50 8A", "add	r5,r9,r10,lsl #1",
				"E0 89 56 1A", "add	r5,r9,r10,lsl r6",
				"E0 89 50 2A", "add	r5,r9,r10,lsr #32",
				"E0 89 5F EA", "add	r5,r9,r10,ror #31",
				"E0 89 50 6A", "add	r5,r9,r10,rrx",
				"02 8D 51 FE", "addeq	r5,sp,#0x8000003f",
				"02 9A 50 71", "addseq	r5,r10,#0x71",
				"00 99 50 0A", "addseq	r5,r9,r10",
				"00 99 50 4A", "addseq	r5,r9,r10,asr #32",
				"E0 99 5F 8A", "adds	r5,r9,r10,lsl #31",
				"E0 99 50 AA", "adds	r5,r9,r10,lsr #1",
				"E0 99 56 3A", "adds	r5,r9,r10,lsr r6",
				"E0 99 50 EA", "adds	r5,r9,r10,ror #1",
				"E0 99 56 7A", "adds	r5,r9,r10,ror r6",
				"E0 99 50 6A", "adds	r5,r9,r10,rrx",
				"02 9D 50 71", "addseq	r5,sp,#0x71",
				"02 0A 50 71", "andeq	r5,r10,#0x71",
				"00 09 50 0A", "andeq	r5,r9,r10",
				"00 09 50 CA", "andeq	r5,r9,r10,asr #1",
				"E0 09 56 5A", "and	r5,r9,r10,asr r6",
				"E0 09 50 8A", "and	r5,r9,r10,lsl #1",
				"00 09 56 1A", "andeq	r5,r9,r10,lsl r6",
				"E0 09 50 2A", "and	r5,r9,r10,lsr #32",
				"E0 09 5F EA", "and	r5,r9,r10,ror #31",
				"E0 09 50 6A", "and	r5,r9,r10,rrx",
				"02 1A 51 FE", "andseq	r5,r10,#0x8000003f",
				"00 19 50 0A", "andseq	r5,r9,r10",
				"00 19 50 4A", "andseq	r5,r9,r10,asr #32",
				"E0 19 5F 8A", "ands	r5,r9,r10,lsl #31",
				"E0 19 50 AA", "ands	r5,r9,r10,lsr #1",
				"00 19 56 3A", "andseq	r5,r9,r10,lsr r6",
				"E0 19 50 EA", "ands	r5,r9,r10,ror #1",
				"E0 19 56 7A", "ands	r5,r9,r10,ror r6",
				"01 A0 5E C9", "asreq	r5,r9,#29",
				"01 A0 5A 59", "asreq	r5,r9,r10",
				"01 B0 5E C9", "asrseq	r5,r9,#29",
				"01 B0 5A 59", "asrseq	r5,r9,r10",
				"0A FF FF FE", "beq	0x00000000",
				"0A 00 00 1C", "beq	0x00000078",
				"0A FF FF 00", "beq	0xfffffc08",
				"07 DF 50 1F", "bfceq	r5,#0,#32",
				"E7 D9 50 9F", "bfc	r5,#1,#25",
				"E7 DF 5F 9F", "bfc	r5,#31,#1",
				"07 DF 50 1A", "bfieq	r5,r10,#0,#32",
				"E7 DF 5F 9A", "bfi	r5,r10,#31,#1",
				"E7 DC 53 9A", "bfi	r5,r10,#7,#22",
				"03 CA 50 71", "biceq	r5,r10,#0x71",
				"01 C9 50 CA", "biceq	r5,r9,r10,asr #1",
				"E1 C9 56 5A", "bic	r5,r9,r10,asr r6",
				"E1 C9 50 8A", "bic	r5,r9,r10,lsl #1",
				"E1 C9 56 1A", "bic	r5,r9,r10,lsl r6",
				"E1 C9 50 2A", "bic	r5,r9,r10,lsr #32",
				"E1 C9 5F EA", "bic	r5,r9,r10,ror #31",
				"E1 C9 50 6A", "bic	r5,r9,r10,rrx",
				"03 DA 51 FE", "bicseq	r5,r10,#0x8000003f",
				"01 D9 50 4A", "bicseq	r5,r9,r10,asr #32",
				"E1 D9 5F 8A", "bics	r5,r9,r10,lsl #31",
				"E1 D9 50 AA", "bics	r5,r9,r10,lsr #1",
				"01 D9 56 3A", "bicseq	r5,r9,r10,lsr r6",
				"E1 D9 50 EA", "bics	r5,r9,r10,ror #1",
				"E1 D9 56 7A", "bics	r5,r9,r10,ror r6",
				"E1 28 5A 77", "bkpt	#0x85a7",
				"0B FF FF FE", "bleq	0x00000000",
				"EB 00 00 1C", "bl	0x00000078",
				"EB FF FF 00", "bl	0xfffffc08",
				"FA FF FF FE", "blx	0x00000000",
				"01 2F FF 39", "blxeq	r9",
				"01 2F FF 16", "bxeq	r6",
				"01 2F FF 29", "bxjeq	r9",
				"0E C9 59 EA", "cdpeq	p9,0xc,c5,c9,c10,0x7",
				"FE 19 57 6A", "cdp2	p7,0x1,c5,c9,c10,0x3",
				"F5 7F F0 1F", "clrex",
				"01 6F 5F 19", "clzeq	r5,r9",
				"03 7A 00 71", "cmneq	r10,#0x71",
				"E3 7A 01 FE", "cmn	r10,#0x8000003f",
				"01 75 00 C9", "cmneq	r5,r9,asr #1",
				"E1 75 00 49", "cmn	r5,r9,asr #32",
				"01 75 06 59", "cmneq	r5,r9,asr r6",
				"E1 75 00 89", "cmn	r5,r9,lsl #1",
				"E1 75 0F 89", "cmn	r5,r9,lsl #31",
				"E1 75 06 19", "cmn	r5,r9,lsl r6",
				"E1 75 00 A9", "cmn	r5,r9,lsr #1",
				"E1 75 00 29", "cmn	r5,r9,lsr #32",
				"E1 75 06 39", "cmn	r5,r9,lsr r6",
				"E1 75 00 E9", "cmn	r5,r9,ror #1",
				"E1 75 0F E9", "cmn	r5,r9,ror #31",
				"E1 75 06 79", "cmn	r5,r9,ror r6",
				"E1 75 00 69", "cmn	r5,r9,rrx",
				"03 5A 00 71", "cmpeq	r10,#0x71",
				"E3 5A 01 FE", "cmp	r10,#0x8000003f",
				"01 55 00 C9", "cmpeq	r5,r9,asr #1",
				"E1 55 00 49", "cmp	r5,r9,asr #32",
				"E1 55 06 59", "cmp	r5,r9,asr r6",
				"E1 55 00 89", "cmp	r5,r9,lsl #1",
				"E1 55 0F 89", "cmp	r5,r9,lsl #31",
				"E1 55 06 19", "cmp	r5,r9,lsl r6",
				"E1 55 00 A9", "cmp	r5,r9,lsr #1",
				"E1 55 00 29", "cmp	r5,r9,lsr #32",
				"E1 55 06 39", "cmp	r5,r9,lsr r6",
				"E1 55 00 E9", "cmp	r5,r9,ror #1",
				"E1 55 0F E9", "cmp	r5,r9,ror #31",
				"E1 55 06 79", "cmp	r5,r9,ror r6",
				"E1 55 00 69", "cmp	r5,r9,rrx",
				"F1 02 00 00", "cps	#0",
				"F1 02 00 1F", "cps	#31",
				"F1 0C 01 40", "cpsid	af",
				"F1 0E 01 DF", "cpsid	aif,#31",
				"F1 0E 00 C1", "cpsid	if,#1",
				"F1 08 01 40", "cpsie	af",
				"F1 0A 01 DF", "cpsie	aif,#31",
				"F1 0A 00 C1", "cpsie	if,#1",
				"03 20 F0 FD", "dbgeq	#13",
				"F5 7F F0 50", "dmb	#0",
				"F5 7F F0 52", "dmb	oshst",
				"F5 7F F0 53", "dmb	osh",
				"F5 7F F0 56", "dmb	nshst",
				"F5 7F F0 57", "dmb	nsh",
				"F5 7F F0 5A", "dmb	ishst",
				"F5 7F F0 5B", "dmb	ish",
				"F5 7F F0 5E", "dmb	st",
				"F5 7F F0 5F", "dmb	sy",
				"F5 7F F0 42", "dsb	oshst",
				"F5 7F F0 43", "dsb	osh",
				"F5 7F F0 46", "dsb	nshst",
				"F5 7F F0 47", "dsb	nsh",
				"F5 7F F0 4A", "dsb	ishst",
				"F5 7F F0 4B", "dsb	ish",
				"F5 7F F0 4D", "dsb	#13",
				"F5 7F F0 4E", "dsb	st",
				"F5 7F F0 4F", "dsb	sy",
				"02 2A 50 71", "eoreq	r5,r10,#0x71",
				"00 29 50 CA", "eoreq	r5,r9,r10,asr #1",
				"00 29 56 5A", "eoreq	r5,r9,r10,asr r6",
				"E0 29 50 8A", "eor	r5,r9,r10,lsl #1",
				"E0 29 56 1A", "eor	r5,r9,r10,lsl r6",
				"E0 29 50 2A", "eor	r5,r9,r10,lsr #32",
				"E0 29 5F EA", "eor	r5,r9,r10,ror #31",
				"E0 29 50 6A", "eor	r5,r9,r10,rrx",
				"02 3A 51 FE", "eorseq	r5,r10,#0x8000003f",
				"00 39 50 4A", "eorseq	r5,r9,r10,asr #32",
				"E0 39 5F 8A", "eors	r5,r9,r10,lsl #31",
				"E0 39 50 AA", "eors	r5,r9,r10,lsr #1",
				"00 39 56 3A", "eorseq	r5,r9,r10,lsr r6",
				"E0 39 50 EA", "eors	r5,r9,r10,ror #1",
				"E0 39 56 7A", "eors	r5,r9,r10,ror r6",
				"F5 7F F0 60", "isb	#0",
				"F5 7F F0 6D", "isb	#13",
				"F5 7F F0 6F", "isb	sy",
				"0D 1F B9 00", "ldceq	p9,c11,[pc,#-0x0]",
				"ED 1F B9 00", "ldc	p9,c11,[pc,#-0x0]",
				"0D 1A B9 00", "ldceq	p9,c11,[r10,#-0x0]",
				"ED 3A B9 00", "ldc	p9,c11,[r10,#-0x0]!",
				"ED 1A B9 21", "ldc	p9,c11,[r10,#-0x84]",
				"ED 3A B9 21", "ldc	p9,c11,[r10,#-0x84]!",
				"ED 9A B9 21", "ldc	p9,c11,[r10,#0x84]",
				"ED BA B9 21", "ldc	p9,c11,[r10,#0x84]!",
				"EC 3A B9 00", "ldc	p9,c11,[r10],#-0x0",
				"0C 3A B9 21", "ldceq	p9,c11,[r10],#-0x84",
				"EC BA B9 21", "ldc	p9,c11,[r10],#0x84",
				"EC 9D B9 00", "ldc	p9,c11,[sp],{0}",
				"0C 9A B9 FF", "ldceq	p9,c11,[r10],{255}",
				"FD 1F B9 00", "ldc2	p9,c11,[pc,#-0x0]",
				"FD 1A B9 21", "ldc2	p9,c11,[r10,#-0x84]",
				"FD 3A B9 21", "ldc2	p9,c11,[r10,#-0x84]!",
				"FD 9A B9 21", "ldc2	p9,c11,[r10,#0x84]",
				"FD BA B9 21", "ldc2	p9,c11,[r10,#0x84]!",
				"FC 3A B9 21", "ldc2	p9,c11,[r10],#-0x84",
				"FC BA B9 21", "ldc2	p9,c11,[r10],#0x84",
				"FC 9A B9 FF", "ldc2	p9,c11,[r10],{255}",
				"FC 9D B9 00", "ldc2	p9,c11,[sp],{0}",
				"FD 5F B9 00", "ldc2l	p9,c11,[pc,#-0x0]",
				"FD 5A B9 21", "ldc2l	p9,c11,[r10,#-0x84]",
				"FD 7A B9 21", "ldc2l	p9,c11,[r10,#-0x84]!",
				"FD DA B9 21", "ldc2l	p9,c11,[r10,#0x84]",
				"FD FA B9 21", "ldc2l	p9,c11,[r10,#0x84]!",
				"FC 7A B9 21", "ldc2l	p9,c11,[r10],#-0x84",
				"FC FA B9 21", "ldc2l	p9,c11,[r10],#0x84",
				"FC DA B9 00", "ldc2l	p9,c11,[r10],{0}",
				"FC DD B9 FF", "ldc2l	p9,c11,[sp],{255}",
				"0D 5F B9 00", "ldcleq	p9,c11,[pc,#-0x0]",
				"ED 5F B9 00", "ldcl	p9,c11,[pc,#-0x0]",
				"ED 5A B9 00", "ldcl	p9,c11,[r10,#-0x0]",
				"ED 7A B9 00", "ldcl	p9,c11,[r10,#-0x0]!",
				"0D 5A B9 21", "ldcleq	p9,c11,[r10,#-0x84]",
				"ED 7A B9 21", "ldcl	p9,c11,[r10,#-0x84]!",
				"ED DA B9 21", "ldcl	p9,c11,[r10,#0x84]",
				"ED FA B9 21", "ldcl	p9,c11,[r10,#0x84]!",
				"0C 7A B9 00", "ldcleq	p9,c11,[r10],#-0x0",
				"EC 7A B9 21", "ldcl	p9,c11,[r10],#-0x84",
				"EC FA B9 21", "ldcl	p9,c11,[r10],#0x84",
				"0C DA B9 00", "ldcleq	p9,c11,[r10],{0}",
				"EC DD B9 FF", "ldcl	p9,c11,[sp],{255}",
				"E8 BA 42 40", "ldm	r10!,{r6,r9,lr}",
				"08 9A 82 40", "ldmeq	r10,{r6,r9,pc}",
				"08 7A C2 40", "ldmdaeq	r10!,{r6,r9,lr,pc}^",
				"E8 3A 42 40", "ldmda	r10!,{r6,r9,lr}",
				"E8 5A C2 40", "ldmda	r10,{r6,r9,lr,pc}^",
				"E8 5A 42 40", "ldmda	r10,{r6,r9,lr}^",
				"E8 1A 82 40", "ldmda	r10,{r6,r9,pc}",
				"09 7A C2 40", "ldmdbeq	r10!,{r6,r9,lr,pc}^",
				"E9 3A 42 40", "ldmdb	r10!,{r6,r9,lr}",
				"E9 5A C2 40", "ldmdb	r10,{r6,r9,lr,pc}^",
				"E9 5A 42 40", "ldmdb	r10,{r6,r9,lr}^",
				"E9 1A 82 40", "ldmdb	r10,{r6,r9,pc}",
				"08 D0 00 06", "ldmiaeq	r0,{r1,r2}^",
				"E8 D0 00 06", "ldmia	r0,{r1,r2}^",
				"E8 FA C2 40", "ldmia	r10!,{r6,r9,lr,pc}^",
				"E8 DA C2 40", "ldmia	r10,{r6,r9,lr,pc}^",
				"E8 DA 42 40", "ldmia	r10,{r6,r9,lr}^",
				"E8 D0 00 06", "ldmia	r0,{r1,r2}^",
				"E8 F0 00 06", "ldmia	r0,{r1,r2}^",
				"09 FA C2 40", "ldmibeq	r10!,{r6,r9,lr,pc}^",
				"E9 BA 42 40", "ldmib	r10!,{r6,r9,lr}",
				"E9 DA C2 40", "ldmib	r10,{r6,r9,lr,pc}^",
				"E9 DA 42 40", "ldmib	r10,{r6,r9,lr}^",
				"E9 9A 82 40", "ldmib	r10,{r6,r9,pc}",
				"05 1F 59 87", "ldreq	r5,[pc,#-0x987] ; 0xfffff679",
				"E5 9F 59 87", "ldr	r5,[pc,#0x987] ; 0x987",
				"E7 9A 50 C9", "ldr	r5,[r10,r9,asr #1]",
				"E7 BA 50 C9", "ldr	r5,[r10,r9,asr #1]!",
				"E7 9A 50 49", "ldr	r5,[r10,r9,asr #32]",
				"E7 BA 50 49", "ldr	r5,[r10,r9,asr #32]!",
				"E7 9A 50 89", "ldr	r5,[r10,r9,lsl #1]",
				"E7 BA 50 89", "ldr	r5,[r10,r9,lsl #1]!",
				"E7 9A 5F 89", "ldr	r5,[r10,r9,lsl #31]",
				"E7 BA 5F 89", "ldr	r5,[r10,r9,lsl #31]!",
				"E7 9A 50 A9", "ldr	r5,[r10,r9,lsr #1]",
				"E7 BA 50 A9", "ldr	r5,[r10,r9,lsr #1]!",
				"E7 9A 50 29", "ldr	r5,[r10,r9,lsr #32]",
				"E7 BA 50 29", "ldr	r5,[r10,r9,lsr #32]!",
				"E7 9A 50 E9", "ldr	r5,[r10,r9,ror #1]",
				"E7 BA 50 E9", "ldr	r5,[r10,r9,ror #1]!",
				"E7 9A 5F E9", "ldr	r5,[r10,r9,ror #31]",
				"E7 BA 5F E9", "ldr	r5,[r10,r9,ror #31]!",
				"E7 9A 50 69", "ldr	r5,[r10,r9,rrx]",
				"E7 BA 50 69", "ldr	r5,[r10,r9,rrx]!",
				"07 9A 50 09", "ldreq	r5,[r10,r9]",
				"E7 BA 50 09", "ldr	r5,[r10,r9]!",
				"07 1A 50 C9", "ldreq	r5,[r10,-r9,asr #1]",
				"E7 3A 50 C9", "ldr	r5,[r10,-r9,asr #1]!",
				"E7 1A 50 49", "ldr	r5,[r10,-r9,asr #32]",
				"E7 3A 50 49", "ldr	r5,[r10,-r9,asr #32]!",
				"E7 1A 50 89", "ldr	r5,[r10,-r9,lsl #1]",
				"E7 3A 50 89", "ldr	r5,[r10,-r9,lsl #1]!",
				"E7 1A 5F 89", "ldr	r5,[r10,-r9,lsl #31]",
				"E7 3A 5F 89", "ldr	r5,[r10,-r9,lsl #31]!",
				"E7 1A 50 A9", "ldr	r5,[r10,-r9,lsr #1]",
				"E7 3A 50 A9", "ldr	r5,[r10,-r9,lsr #1]!",
				"E7 1A 50 29", "ldr	r5,[r10,-r9,lsr #32]",
				"E7 3A 50 29", "ldr	r5,[r10,-r9,lsr #32]!",
				"E7 1A 50 E9", "ldr	r5,[r10,-r9,ror #1]",
				"E7 3A 50 E9", "ldr	r5,[r10,-r9,ror #1]!",
				"E7 1A 5F E9", "ldr	r5,[r10,-r9,ror #31]",
				"E7 3A 5F E9", "ldr	r5,[r10,-r9,ror #31]!",
				"E7 1A 50 69", "ldr	r5,[r10,-r9,rrx]",
				"E7 3A 50 69", "ldr	r5,[r10,-r9,rrx]!",
				"E7 1A 50 09", "ldr	r5,[r10,-r9]",
				"E7 3A 50 09", "ldr	r5,[r10,-r9]!",
				"05 1A 59 87", "ldreq	r5,[r10,#-0x987]",
				"E5 3A 59 87", "ldr	r5,[r10,#-0x987]!",
				"E5 9A 59 87", "ldr	r5,[r10,#0x987]",
				"E5 BA 59 87", "ldr	r5,[r10,#0x987]!",
				"E5 9A 50 00", "ldr	r5,[r10]",
				"E6 1A 50 09", "ldr	r5,[r10],-r9",
				"E6 9A 50 09", "ldr	r5,[r10],r9",
				"E6 9A 50 C9", "ldr	r5,[r10],r9,asr #1",
				"E6 9A 50 49", "ldr	r5,[r10],r9,asr #32",
				"E6 9A 50 89", "ldr	r5,[r10],r9,lsl #1",
				"E6 9A 5F 89", "ldr	r5,[r10],r9,lsl #31",
				"E6 9A 50 A9", "ldr	r5,[r10],r9,lsr #1",
				"E6 9A 50 29", "ldr	r5,[r10],r9,lsr #32",
				"E6 9A 50 E9", "ldr	r5,[r10],r9,ror #1",
				"E6 9A 5F E9", "ldr	r5,[r10],r9,ror #31",
				"E6 9A 50 69", "ldr	r5,[r10],r9,rrx",
				"06 1A 50 C9", "ldreq	r5,[r10],-r9,asr #1",
				"E6 1A 50 49", "ldr	r5,[r10],-r9,asr #32",
				"E6 1A 50 89", "ldr	r5,[r10],-r9,lsl #1",
				"E6 1A 5F 89", "ldr	r5,[r10],-r9,lsl #31",
				"E6 1A 50 A9", "ldr	r5,[r10],-r9,lsr #1",
				"E6 1A 50 29", "ldr	r5,[r10],-r9,lsr #32",
				"E6 1A 50 E9", "ldr	r5,[r10],-r9,ror #1",
				"E6 1A 5F E9", "ldr	r5,[r10],-r9,ror #31",
				"E6 1A 50 69", "ldr	r5,[r10],-r9,rrx",
				"04 1A 59 87", "ldreq	r5,[r10],#-0x987",
				"E4 9A 59 87", "ldr	r5,[r10],#0x987",
				"05 5F 59 87", "ldrbeq	r5,[pc,#-0x987] ; 0xfffff679",
				"E5 DF 59 87", "ldrb	r5,[pc,#0x987] ; 0x987",
				"E7 7A 50 09", "ldrb	r5,[r10,-r9]!",
				"05 5A 59 87", "ldrbeq	r5,[r10,#-0x987]",
				"E5 7A 59 87", "ldrb	r5,[r10,#-0x987]!",
				"E5 DA 59 87", "ldrb	r5,[r10,#0x987]",
				"E5 FA 59 87", "ldrb	r5,[r10,#0x987]!",
				"07 DA 50 C9", "ldrbeq	r5,[r10,r9,asr #1]",
				"E7 FA 50 C9", "ldrb	r5,[r10,r9,asr #1]!",
				"E7 DA 50 49", "ldrb	r5,[r10,r9,asr #32]",
				"E7 FA 50 49", "ldrb	r5,[r10,r9,asr #32]!",
				"E7 DA 50 89", "ldrb	r5,[r10,r9,lsl #1]",
				"E7 FA 50 89", "ldrb	r5,[r10,r9,lsl #1]!",
				"E7 DA 5F 89", "ldrb	r5,[r10,r9,lsl #31]",
				"E7 FA 5F 89", "ldrb	r5,[r10,r9,lsl #31]!",
				"E7 DA 50 A9", "ldrb	r5,[r10,r9,lsr #1]",
				"E7 FA 50 A9", "ldrb	r5,[r10,r9,lsr #1]!",
				"E7 DA 50 29", "ldrb	r5,[r10,r9,lsr #32]",
				"E7 FA 50 29", "ldrb	r5,[r10,r9,lsr #32]!",
				"E7 DA 50 E9", "ldrb	r5,[r10,r9,ror #1]",
				"E7 FA 50 E9", "ldrb	r5,[r10,r9,ror #1]!",
				"E7 DA 5F E9", "ldrb	r5,[r10,r9,ror #31]",
				"E7 FA 5F E9", "ldrb	r5,[r10,r9,ror #31]!",
				"E7 DA 50 69", "ldrb	r5,[r10,r9,rrx]",
				"E7 FA 50 69", "ldrb	r5,[r10,r9,rrx]!",
				"E7 DA 50 09", "ldrb	r5,[r10,r9]",
				"E7 FA 50 09", "ldrb	r5,[r10,r9]!",
				"07 5A 50 C9", "ldrbeq	r5,[r10,-r9,asr #1]",
				"E7 7A 50 C9", "ldrb	r5,[r10,-r9,asr #1]!",
				"E7 5A 50 49", "ldrb	r5,[r10,-r9,asr #32]",
				"E7 7A 50 49", "ldrb	r5,[r10,-r9,asr #32]!",
				"E7 5A 50 89", "ldrb	r5,[r10,-r9,lsl #1]",
				"E7 7A 50 89", "ldrb	r5,[r10,-r9,lsl #1]!",
				"E7 5A 5F 89", "ldrb	r5,[r10,-r9,lsl #31]",
				"E7 7A 5F 89", "ldrb	r5,[r10,-r9,lsl #31]!",
				"E7 5A 50 A9", "ldrb	r5,[r10,-r9,lsr #1]",
				"E7 7A 50 A9", "ldrb	r5,[r10,-r9,lsr #1]!",
				"E7 5A 50 29", "ldrb	r5,[r10,-r9,lsr #32]",
				"E7 7A 50 29", "ldrb	r5,[r10,-r9,lsr #32]!",
				"E7 5A 50 E9", "ldrb	r5,[r10,-r9,ror #1]",
				"E7 7A 50 E9", "ldrb	r5,[r10,-r9,ror #1]!",
				"E7 5A 5F E9", "ldrb	r5,[r10,-r9,ror #31]",
				"E7 7A 5F E9", "ldrb	r5,[r10,-r9,ror #31]!",
				"E7 5A 50 69", "ldrb	r5,[r10,-r9,rrx]",
				"E7 7A 50 69", "ldrb	r5,[r10,-r9,rrx]!",
				"E7 5A 50 09", "ldrb	r5,[r10,-r9]",
				"E5 DA 50 00", "ldrb	r5,[r10]",
				"E4 5A 59 87", "ldrb	r5,[r10],#-0x987",
				"E4 DA 59 87", "ldrb	r5,[r10],#0x987",
				"E6 DA 50 09", "ldrb	r5,[r10],r9",
				"E6 DA 50 C9", "ldrb	r5,[r10],r9,asr #1",
				"E6 DA 50 49", "ldrb	r5,[r10],r9,asr #32",
				"E6 DA 50 89", "ldrb	r5,[r10],r9,lsl #1",
				"E6 DA 5F 89", "ldrb	r5,[r10],r9,lsl #31",
				"E6 DA 50 A9", "ldrb	r5,[r10],r9,lsr #1",
				"E6 DA 50 29", "ldrb	r5,[r10],r9,lsr #32",
				"E6 DA 50 E9", "ldrb	r5,[r10],r9,ror #1",
				"E6 DA 5F E9", "ldrb	r5,[r10],r9,ror #31",
				"E6 DA 50 69", "ldrb	r5,[r10],r9,rrx",
				"E6 5A 50 09", "ldrb	r5,[r10],-r9",
				"E6 5A 50 C9", "ldrb	r5,[r10],-r9,asr #1",
				"E6 5A 50 49", "ldrb	r5,[r10],-r9,asr #32",
				"E6 5A 50 89", "ldrb	r5,[r10],-r9,lsl #1",
				"E6 5A 5F 89", "ldrb	r5,[r10],-r9,lsl #31",
				"E6 5A 50 A9", "ldrb	r5,[r10],-r9,lsr #1",
				"E6 5A 50 29", "ldrb	r5,[r10],-r9,lsr #32",
				"E6 5A 50 E9", "ldrb	r5,[r10],-r9,ror #1",
				"E6 5A 5F E9", "ldrb	r5,[r10],-r9,ror #31",
				"E6 5A 50 69", "ldrb	r5,[r10],-r9,rrx",
				"04 7A 59 87", "ldrbteq	r5,[r10],#-0x987",
				"E4 FA 59 87", "ldrbt	r5,[r10],#0x987",
				"E6 FA 50 09", "ldrbt	r5,[r10],r9",
				"E6 FA 50 C9", "ldrbt	r5,[r10],r9,asr #1",
				"E6 FA 50 49", "ldrbt	r5,[r10],r9,asr #32",
				"E6 FA 50 89", "ldrbt	r5,[r10],r9,lsl #1",
				"E6 FA 5F 89", "ldrbt	r5,[r10],r9,lsl #31",
				"E6 FA 50 A9", "ldrbt	r5,[r10],r9,lsr #1",
				"E6 FA 50 29", "ldrbt	r5,[r10],r9,lsr #32",
				"E6 FA 50 E9", "ldrbt	r5,[r10],r9,ror #1",
				"E6 FA 5F E9", "ldrbt	r5,[r10],r9,ror #31",
				"E6 FA 50 69", "ldrbt	r5,[r10],r9,rrx",
				"06 7A 50 09", "ldrbteq	r5,[r10],-r9",
				"06 7A 50 C9", "ldrbteq	r5,[r10],-r9,asr #1",
				"E6 7A 50 49", "ldrbt	r5,[r10],-r9,asr #32",
				"E6 7A 50 89", "ldrbt	r5,[r10],-r9,lsl #1",
				"E6 7A 5F 89", "ldrbt	r5,[r10],-r9,lsl #31",
				"E6 7A 50 A9", "ldrbt	r5,[r10],-r9,lsr #1",
				"E6 7A 50 29", "ldrbt	r5,[r10],-r9,lsr #32",
				"E6 7A 50 E9", "ldrbt	r5,[r10],-r9,ror #1",
				"E6 7A 5F E9", "ldrbt	r5,[r10],-r9,ror #31",
				"E6 7A 50 69", "ldrbt	r5,[r10],-r9,rrx",
				"01 4F 68 D7", "ldrdeq	r6,r7,[pc,#-0x87] ; 0xffffff79",
				"E1 CF 68 D7", "ldrd	r6,r7,[pc,#0x87] ; 0x87",
				"01 4A 68 D7", "ldrdeq	r6,r7,[r10,#-0x87]",
				"E1 6A 68 D7", "ldrd	r6,r7,[r10,#-0x87]!",
				"E1 CA 68 D7", "ldrd	r6,r7,[r10,#0x87]",
				"E1 EA 68 D7", "ldrd	r6,r7,[r10,#0x87]!",
				"E1 8A 60 D9", "ldrd	r6,r7,[r10,r9]",
				"E1 AA 60 D9", "ldrd	r6,r7,[r10,r9]!",
				"01 0A 60 D9", "ldrdeq	r6,r7,[r10,-r9]",
				"E1 2A 60 D9", "ldrd	r6,r7,[r10,-r9]!",
				"E1 CA 60 D0", "ldrd	r6,r7,[r10]",
				"00 4A 68 D7", "ldrdeq	r6,r7,[r10],#-0x87",
				"E0 CA 68 D7", "ldrd	r6,r7,[r10],#0x87",
				"E0 8A 60 D9", "ldrd	r6,r7,[r10],r9",
				"E0 0A 60 D9", "ldrd	r6,r7,[r10],-r9",
				"01 9A 5F 9F", "ldrexeq	r5,[r10]",
				"01 DA 5F 9F", "ldrexbeq	r5,[r10]",
				"01 BA 6F 9F", "ldrexdeq	r6,r7,[r10]",
				"01 FA 5F 9F", "ldrexheq	r5,[r10]",
				"01 5F 58 B7", "ldrheq	r5,[pc,#-0x87] ; 0xffffff79",
				"E1 DF 58 B7", "ldrh	r5,[pc,#0x87] ; 0x87",
				"01 5A 58 B7", "ldrheq	r5,[r10,#-0x87]",
				"E1 7A 58 B7", "ldrh	r5,[r10,#-0x87]!",
				"E1 DA 58 B7", "ldrh	r5,[r10,#0x87]",
				"E1 FA 58 B7", "ldrh	r5,[r10,#0x87]!",
				"E1 9A 50 B9", "ldrh	r5,[r10,r9]",
				"E1 BA 50 B9", "ldrh	r5,[r10,r9]!",
				"01 1A 50 B9", "ldrheq	r5,[r10,-r9]",
				"E1 3A 50 B9", "ldrh	r5,[r10,-r9]!",
				"E1 DA 50 B0", "ldrh	r5,[r10]",
				"E0 1A 50 B9", "ldrh	r5,[r10],-r9",
				"00 5A 58 B7", "ldrheq	r5,[r10],#-0x87",
				"E0 DA 58 B7", "ldrh	r5,[r10],#0x87",
				"E0 9A 50 B9", "ldrh	r5,[r10],r9",
				"E1 DA 50 B0", "ldrh	r5,[r10]",
				"00 FA 50 B0", "ldrhteq	r5,[r10]",
				"00 7A 58 B7", "ldrhteq	r5,[r10],#-0x87",
				"E0 FA 58 B7", "ldrht	r5,[r10],#0x87",
				"E0 BA 50 B9", "ldrht	r5,[r10],r9",
				"00 3A 50 B9", "ldrhteq	r5,[r10],-r9",
				"01 5F 58 D7", "ldrsbeq	r5,[pc,#-0x87] ; 0xffffff79",
				"E1 DF 58 D7", "ldrsb	r5,[pc,#0x87] ; 0x87",
				"01 5A 58 D7", "ldrsbeq	r5,[r10,#-0x87]",
				"E1 7A 58 D7", "ldrsb	r5,[r10,#-0x87]!",
				"E1 DA 58 D7", "ldrsb	r5,[r10,#0x87]",
				"E1 FA 58 D7", "ldrsb	r5,[r10,#0x87]!",
				"E1 9A 50 D9", "ldrsb	r5,[r10,r9]",
				"E1 BA 50 D9", "ldrsb	r5,[r10,r9]!",
				"01 1A 50 D9", "ldrsbeq	r5,[r10,-r9]",
				"E1 3A 50 D9", "ldrsb	r5,[r10,-r9]!",
				"E1 DA 50 D0", "ldrsb	r5,[r10]",
				"00 5A 58 D7", "ldrsbeq	r5,[r10],#-0x87",
				"E0 DA 58 D7", "ldrsb	r5,[r10],#0x87",
				"E0 9A 50 D9", "ldrsb	r5,[r10],r9",
				"00 1A 50 D9", "ldrsbeq	r5,[r10],-r9",
				"00 FA 50 D0", "ldrsbteq	r5,[r10]",
				"00 7A 58 D7", "ldrsbteq	r5,[r10],#-0x87",
				"E0 FA 58 D7", "ldrsbt	r5,[r10],#0x87",
				"E0 BA 50 D9", "ldrsbt	r5,[r10],r9",
				"00 3A 50 D9", "ldrsbteq	r5,[r10],-r9",
				"01 5F 58 F7", "ldrsheq	r5,[pc,#-0x87] ; 0xffffff79",
				"E1 DF 58 F7", "ldrsh	r5,[pc,#0x87] ; 0x87",
				"01 5A 58 F7", "ldrsheq	r5,[r10,#-0x87]",
				"E1 7A 58 F7", "ldrsh	r5,[r10,#-0x87]!",
				"E1 DA 58 F7", "ldrsh	r5,[r10,#0x87]",
				"E1 FA 58 F7", "ldrsh	r5,[r10,#0x87]!",
				"E1 9A 50 F9", "ldrsh	r5,[r10,r9]",
				"E1 BA 50 F9", "ldrsh	r5,[r10,r9]!",
				"01 1A 50 F9", "ldrsheq	r5,[r10,-r9]",
				"E1 3A 50 F9", "ldrsh	r5,[r10,-r9]!",
				"E1 DA 50 F0", "ldrsh	r5,[r10]",
				"00 5A 58 F7", "ldrsheq	r5,[r10],#-0x87",
				"E0 DA 58 F7", "ldrsh	r5,[r10],#0x87",
				"E0 9A 50 F9", "ldrsh	r5,[r10],r9",
				"E0 1A 50 F9", "ldrsh	r5,[r10],-r9",
				"00 FA 50 F0", "ldrshteq	r5,[r10]",
				"00 7A 58 F7", "ldrshteq	r5,[r10],#-0x87",
				"E0 FA 58 F7", "ldrsht	r5,[r10],#0x87",
				"E0 BA 50 F9", "ldrsht	r5,[r10],r9",
				"00 3A 50 F9", "ldrshteq	r5,[r10],-r9",
				"04 BA 50 00", "ldrteq	r5,[r10]",
				"04 3A 59 87", "ldrteq	r5,[r10],#-0x987",
				"E4 BA 59 87", "ldrt	r5,[r10],#0x987",
				"E6 BA 50 09", "ldrt	r5,[r10],r9",
				"E6 BA 50 C9", "ldrt	r5,[r10],r9,asr #1",
				"E6 BA 50 49", "ldrt	r5,[r10],r9,asr #32",
				"E6 BA 50 89", "ldrt	r5,[r10],r9,lsl #1",
				"E6 BA 5F 89", "ldrt	r5,[r10],r9,lsl #31",
				"E6 BA 50 A9", "ldrt	r5,[r10],r9,lsr #1",
				"E6 BA 50 29", "ldrt	r5,[r10],r9,lsr #32",
				"E6 BA 50 E9", "ldrt	r5,[r10],r9,ror #1",
				"E6 BA 5F E9", "ldrt	r5,[r10],r9,ror #31",
				"E6 BA 50 69", "ldrt	r5,[r10],r9,rrx",
				"06 3A 50 09", "ldrteq	r5,[r10],-r9",
				"06 3A 50 C9", "ldrteq	r5,[r10],-r9,asr #1",
				"E6 3A 50 49", "ldrt	r5,[r10],-r9,asr #32",
				"E6 3A 50 89", "ldrt	r5,[r10],-r9,lsl #1",
				"E6 3A 5F 89", "ldrt	r5,[r10],-r9,lsl #31",
				"E6 3A 50 A9", "ldrt	r5,[r10],-r9,lsr #1",
				"E6 3A 50 29", "ldrt	r5,[r10],-r9,lsr #32",
				"E6 3A 50 E9", "ldrt	r5,[r10],-r9,ror #1",
				"E6 3A 5F E9", "ldrt	r5,[r10],-r9,ror #31",
				"E6 3A 50 69", "ldrt	r5,[r10],-r9,rrx",
				"01 A0 5E 89", "lsleq	r5,r9,#29",
				"01 A0 5A 19", "lsleq	r5,r9,r10",
				"E1 B0 5E 89", "lsls	r5,r9,#29",
				"E1 B0 5A 19", "lsls	r5,r9,r10",
				"01 A0 5E A9", "lsreq	r5,r9,#29",
				"01 A0 5A 39", "lsreq	r5,r9,r10",
				"01 B0 5E A9", "lsrseq	r5,r9,#29",
				"01 B0 5A 39", "lsrseq	r5,r9,r10",
				"0E C9 59 FA", "mcreq	p9,0x6,r5,c9,c10,0x7",
				"FE C9 59 FA", "mcr2	p9,0x6,r5,c9,c10,0x7",
				"0C 46 59 C9", "mcrreq	p9,0xc,r5,r6,c9",
				"FC 46 59 C9", "mcrr2	p9,0xc,r5,r6,c9",
				"00 25 8A 99", "mlaeq	r5,r9,r10,r8",
				"00 35 8A 99", "mlaseq	r5,r9,r10,r8",
				"00 65 8A 99", "mlseq	r5,r9,r10,r8",
				"03 A0 50 71", "moveq	r5,#0x71",
				"01 A0 50 09", "moveq	r5,r9",
				"03 B0 51 FE", "movseq	r5,#0x8000003f",
				"01 B0 50 09", "movseq	r5,r9",
				"03 49 58 76", "movteq	r5,#0x9876",
				"03 09 58 76", "movweq	r5,#0x9876",
				"0E D5 F9 B9", "mrceq	p9,0x6,apsr_nzcv,c5,c9,0x5",
				"EE F5 59 99", "mrc	p9,0x7,r5,c5,c9,0x4",
				"FE 95 F9 F9", "mrc2	p9,0x4,apsr_nzcv,c5,c9,0x7",
				"FE B5 59 D9", "mrc2	p9,0x5,r5,c5,c9,0x6",
				"0C 56 59 F9", "mrrceq	p9,0xf,r5,r6,c9",
				"FC 56 59 39", "mrrc2	p9,0x3,r5,r6,c9",
				"01 0F 50 00", "mrseq	r5,cpsr",
				"E1 4F 50 00", "mrs	r5,spsr",
				"03 2F F1 FE", "msreq	cpsr_cxfs,#0x8000003f",
				"01 2F F0 0A", "msreq	cpsr_cxfs,r10",
				"E3 6F F1 FE", "msr	spsr_cxfs,#0x8000003f",
				"E1 6F F0 0A", "msr	spsr_cxfs,r10",
				"03 28 F0 71", "msreq	cpsr_f,#0x71",
				"01 28 F0 0A", "msreq	cpsr_f,r10",
				"E3 24 F1 FE", "msr	cpsr_s,#0x8000003f",
				"E1 24 F0 0A", "msr	cpsr_s,r10",
				"E3 2C F1 FE", "msr	cpsr_fs,#0x8000003f",
				"E1 2C F0 0A", "msr	cpsr_fs,r10",
				"00 05 0A 99", "muleq	r5,r9,r10",
				"00 15 0A 99", "mulseq	r5,r9,r10",
				"03 E0 50 71", "mvneq	r5,#0x71",
				"01 E0 50 C9", "mvneq	r5,r9,asr #1",
				"01 E0 56 59", "mvneq	r5,r9,asr r6",
				"E1 E0 50 89", "mvn	r5,r9,lsl #1",
				"E1 E0 56 19", "mvn	r5,r9,lsl r6",
				"E1 E0 50 29", "mvn	r5,r9,lsr #32",
				"E1 E0 5F E9", "mvn	r5,r9,ror #31",
				"E1 E0 50 69", "mvn	r5,r9,rrx",
				"03 F0 51 FE", "mvnseq	r5,#0x8000003f",
				"01 F0 50 49", "mvnseq	r5,r9,asr #32",
				"E1 F0 5F 89", "mvns	r5,r9,lsl #31",
				"E1 F0 50 A9", "mvns	r5,r9,lsr #1",
				"01 F0 56 39", "mvnseq	r5,r9,lsr r6",
				"E1 F0 50 E9", "mvns	r5,r9,ror #1",
				"E1 F0 56 79", "mvns	r5,r9,ror r6",
				"E3 20 F0 00", "nop",
				"03 20 F0 00", "nopeq",
				"03 8A 50 71", "orreq	r5,r10,#0x71",
				"01 89 50 CA", "orreq	r5,r9,r10,asr #1",
				"01 89 56 5A", "orreq	r5,r9,r10,asr r6",
				"E1 89 50 8A", "orr	r5,r9,r10,lsl #1",
				"E1 89 56 1A", "orr	r5,r9,r10,lsl r6",
				"E1 89 50 2A", "orr	r5,r9,r10,lsr #32",
				"E1 89 5F EA", "orr	r5,r9,r10,ror #31",
				"E1 89 50 6A", "orr	r5,r9,r10,rrx",
				"03 9A 51 FE", "orrseq	r5,r10,#0x8000003f",
				"01 99 50 4A", "orrseq	r5,r9,r10,asr #32",
				"E1 99 5F 8A", "orrs	r5,r9,r10,lsl #31",
				"E1 99 50 AA", "orrs	r5,r9,r10,lsr #1",
				"01 99 56 3A", "orrseq	r5,r9,r10,lsr r6",
				"E1 99 50 EA", "orrs	r5,r9,r10,ror #1",
				"E1 99 56 7A", "orrs	r5,r9,r10,ror r6",
				"06 89 50 1A", "pkhbteq	r5,r9,r10",
				"06 89 5E 9A", "pkhbteq	r5,r9,r10,lsl #29",
				"06 89 5E DA", "pkhtbeq	r5,r9,r10,asr #29",
				"F5 5F F9 87", "pld	[pc,#-0x987] ; 0xfffff679",
				"F5 DF F9 87", "pld	[pc,#0x987] ; 0x987",
				"F5 5A F9 87", "pld	[r10,#-0x987]",
				"F5 DA F9 87", "pld	[r10,#0x987]",
				"F7 DA F0 C9", "pld	[r10,r9,asr #1]",
				"F7 DA F0 49", "pld	[r10,r9,asr #32]",
				"F7 DA F0 89", "pld	[r10,r9,lsl #1]",
				"F7 DA FF 89", "pld	[r10,r9,lsl #31]",
				"F7 DA F0 A9", "pld	[r10,r9,lsr #1]",
				"F7 DA F0 29", "pld	[r10,r9,lsr #32]",
				"F7 DA F0 E9", "pld	[r10,r9,ror #1]",
				"F7 DA FF E9", "pld	[r10,r9,ror #31]",
				"F7 DA F0 69", "pld	[r10,r9,rrx]",
				"F7 DA F0 09", "pld	[r10,r9]",
				"F7 5A F0 C9", "pld	[r10,-r9,asr #1]",
				"F7 5A F0 49", "pld	[r10,-r9,asr #32]",
				"F7 5A F0 89", "pld	[r10,-r9,lsl #1]",
				"F7 5A FF 89", "pld	[r10,-r9,lsl #31]",
				"F7 5A F0 A9", "pld	[r10,-r9,lsr #1]",
				"F7 5A F0 29", "pld	[r10,-r9,lsr #32]",
				"F7 5A F0 E9", "pld	[r10,-r9,ror #1]",
				"F7 5A FF E9", "pld	[r10,-r9,ror #31]",
				"F7 5A F0 69", "pld	[r10,-r9,rrx]",
				"F7 5A F0 09", "pld	[r10,-r9]",
				"F5 1A F9 87", "pldw	[r10,#-0x987]",
				"F5 9A F9 87", "pldw	[r10,#0x987]",
				"F7 9A F0 C9", "pldw	[r10,r9,asr #1]",
				"F7 9A F0 49", "pldw	[r10,r9,asr #32]",
				"F7 9A F0 89", "pldw	[r10,r9,lsl #1]",
				"F7 9A FF 89", "pldw	[r10,r9,lsl #31]",
				"F7 9A F0 A9", "pldw	[r10,r9,lsr #1]",
				"F7 9A F0 29", "pldw	[r10,r9,lsr #32]",
				"F7 9A F0 E9", "pldw	[r10,r9,ror #1]",
				"F7 9A FF E9", "pldw	[r10,r9,ror #31]",
				"F7 9A F0 69", "pldw	[r10,r9,rrx]",
				"F7 9A F0 09", "pldw	[r10,r9]",
				"F7 1A F0 C9", "pldw	[r10,-r9,asr #1]",
				"F7 1A F0 49", "pldw	[r10,-r9,asr #32]",
				"F7 1A F0 89", "pldw	[r10,-r9,lsl #1]",
				"F7 1A FF 89", "pldw	[r10,-r9,lsl #31]",
				"F7 1A F0 A9", "pldw	[r10,-r9,lsr #1]",
				"F7 1A F0 29", "pldw	[r10,-r9,lsr #32]",
				"F7 1A F0 E9", "pldw	[r10,-r9,ror #1]",
				"F7 1A FF E9", "pldw	[r10,-r9,ror #31]",
				"F7 1A F0 69", "pldw	[r10,-r9,rrx]",
				"F7 1A F0 09", "pldw	[r10,-r9]",
				"F4 5F F9 87", "pli	[pc,#-0x987] ; 0xfffff679",
				"F4 DF F9 87", "pli	[pc,#0x987] ; 0x987",
				"F4 5A F9 87", "pli	[r10,#-0x987]",
				"F4 DA F9 87", "pli	[r10,#0x987]",
				"F6 DA F0 C9", "pli	[r10,r9,asr #1]",
				"F6 DA F0 49", "pli	[r10,r9,asr #32]",
				"F6 DA F0 89", "pli	[r10,r9,lsl #1]",
				"F6 DA FF 89", "pli	[r10,r9,lsl #31]",
				"F6 DA F0 A9", "pli	[r10,r9,lsr #1]",
				"F6 DA F0 29", "pli	[r10,r9,lsr #32]",
				"F6 DA F0 E9", "pli	[r10,r9,ror #1]",
				"F6 DA FF E9", "pli	[r10,r9,ror #31]",
				"F6 DA F0 69", "pli	[r10,r9,rrx]",
				"F6 DA F0 09", "pli	[r10,r9]",
				"04 9D E0 04", "popeq	{lr}",
				"F6 5A F0 C9", "pli	[r10,-r9,asr #1]",
				"F6 5A F0 49", "pli	[r10,-r9,asr #32]",
				"F6 5A F0 89", "pli	[r10,-r9,lsl #1]",
				"F6 5A FF 89", "pli	[r10,-r9,lsl #31]",
				"F6 5A F0 A9", "pli	[r10,-r9,lsr #1]",
				"F6 5A F0 29", "pli	[r10,-r9,lsr #32]",
				"F6 5A F0 E9", "pli	[r10,-r9,ror #1]",
				"F6 5A FF E9", "pli	[r10,-r9,ror #31]",
				"F6 5A F0 69", "pli	[r10,-r9,rrx]",
				"F6 5A F0 09", "pli	[r10,-r9]",
				"E8 BD 82 40", "pop	{r6,r9,pc}",
				"05 2D E0 04", "pusheq	{lr}",
				"E9 2D 12 40", "push	{r6,r9,r12}",
				"01 0A 50 59", "qaddeq	r5,r9,r10",
				"06 29 5F 1A", "qadd16eq	r5,r9,r10",
				"06 29 5F 9A", "qadd8eq	r5,r9,r10",
				"06 29 5F 3A", "qasxeq	r5,r9,r10",
				"01 4A 50 59", "qdaddeq	r5,r9,r10",
				"01 6A 50 59", "qdsubeq	r5,r9,r10",
				"06 29 5F 5A", "qsaxeq	r5,r9,r10",
				"01 2A 50 59", "qsubeq	r5,r9,r10",
				"06 29 5F 7A", "qsub16eq	r5,r9,r10",
				"06 29 5F FA", "qsub8eq	r5,r9,r10",
				"06 FF 5F 39", "rbiteq	r5,r9",
				"06 BF 5F 39", "reveq	r5,r9",
				"06 BF 5F B9", "rev16eq	r5,r9",
				"06 FF 5F B9", "revsheq	r5,r9",
				"F8 1A 0A 00", "rfeda	r10",
				"F8 3A 0A 00", "rfeda	r10!",
				"F9 1A 0A 00", "rfedb	r10",
				"F9 3A 0A 00", "rfedb	r10!",
				"F8 9A 0A 00", "rfeia	r10",
				"F8 BA 0A 00", "rfeia	r10!",
				"F9 9A 0A 00", "rfeib	r10",
				"F9 BA 0A 00", "rfeib	r10!",
				"01 A0 5E E9", "roreq	r5,r9,#29",
				"01 A0 5A 79", "roreq	r5,r9,r10",
				"E1 B0 5E E9", "rors	r5,r9,#29",
				"E1 B0 5A 79", "rors	r5,r9,r10",
				"01 A0 50 69", "rrxeq	r5,r9",
				"01 B0 50 69", "rrxseq	r5,r9",
				"02 6A 50 71", "rsbeq	r5,r10,#0x71",
				"00 69 50 CA", "rsbeq	r5,r9,r10,asr #1",
				"00 69 56 5A", "rsbeq	r5,r9,r10,asr r6",
				"E0 69 50 8A", "rsb	r5,r9,r10,lsl #1",
				"E0 69 56 1A", "rsb	r5,r9,r10,lsl r6",
				"E0 69 50 2A", "rsb	r5,r9,r10,lsr #32",
				"E0 69 5F EA", "rsb	r5,r9,r10,ror #31",
				"E0 69 50 6A", "rsb	r5,r9,r10,rrx",
				"02 7A 51 FE", "rsbseq	r5,r10,#0x8000003f",
				"00 79 50 4A", "rsbseq	r5,r9,r10,asr #32",
				"E0 79 5F 8A", "rsbs	r5,r9,r10,lsl #31",
				"E0 79 50 AA", "rsbs	r5,r9,r10,lsr #1",
				"00 79 56 3A", "rsbseq	r5,r9,r10,lsr r6",
				"E0 79 50 EA", "rsbs	r5,r9,r10,ror #1",
				"E0 79 56 7A", "rsbs	r5,r9,r10,ror r6",
				"02 EA 50 71", "rsceq	r5,r10,#0x71",
				"00 E9 50 CA", "rsceq	r5,r9,r10,asr #1",
				"00 E9 56 5A", "rsceq	r5,r9,r10,asr r6",
				"E0 E9 50 8A", "rsc	r5,r9,r10,lsl #1",
				"E0 E9 56 1A", "rsc	r5,r9,r10,lsl r6",
				"E0 E9 50 2A", "rsc	r5,r9,r10,lsr #32",
				"E0 E9 5F EA", "rsc	r5,r9,r10,ror #31",
				"E0 E9 50 6A", "rsc	r5,r9,r10,rrx",
				"02 FA 51 FE", "rscseq	r5,r10,#0x8000003f",
				"00 F9 50 4A", "rscseq	r5,r9,r10,asr #32",
				"E0 F9 5F 8A", "rscs	r5,r9,r10,lsl #31",
				"E0 F9 50 AA", "rscs	r5,r9,r10,lsr #1",
				"00 F9 56 3A", "rscseq	r5,r9,r10,lsr r6",
				"E0 F9 50 EA", "rscs	r5,r9,r10,ror #1",
				"E0 F9 56 7A", "rscs	r5,r9,r10,ror r6",
				"06 19 5F 1A", "sadd16eq	r5,r9,r10",
				"06 19 5F 9A", "sadd8eq	r5,r9,r10",
				"06 19 5F 3A", "sasxeq	r5,r9,r10",
				"02 CA 50 71", "sbceq	r5,r10,#0x71",
				"00 C9 50 CA", "sbceq	r5,r9,r10,asr #1",
				"00 C9 56 5A", "sbceq	r5,r9,r10,asr r6",
				"E0 C9 50 8A", "sbc	r5,r9,r10,lsl #1",
				"E0 C9 56 1A", "sbc	r5,r9,r10,lsl r6",
				"E0 C9 50 2A", "sbc	r5,r9,r10,lsr #32",
				"E0 C9 5F EA", "sbc	r5,r9,r10,ror #31",
				"E0 C9 50 6A", "sbc	r5,r9,r10,rrx",
				"02 DA 51 FE", "sbcseq	r5,r10,#0x8000003f",
				"00 D9 50 4A", "sbcseq	r5,r9,r10,asr #32",
				"E0 D9 5F 8A", "sbcs	r5,r9,r10,lsl #31",
				"E0 D9 50 AA", "sbcs	r5,r9,r10,lsr #1",
				"00 D9 56 3A", "sbcseq	r5,r9,r10,lsr r6",
				"E0 D9 50 EA", "sbcs	r5,r9,r10,ror #1",
				"E0 D9 56 7A", "sbcs	r5,r9,r10,ror r6",
				"07 BF 50 5A", "sbfxeq	r5,r10,#0,#32",
				"E7 A0 5F DA", "sbfx	r5,r10,#31,#1",
				"06 89 5F BA", "seleq	r5,r9,r10",
				"F1 01 02 00", "setend	be",
				"F1 01 00 00", "setend	le",
				"03 20 F0 04", "seveq",
				"06 39 5F 1A", "shadd16eq	r5,r9,r10",
				"06 39 5F 9A", "shadd8eq	r5,r9,r10",
				"06 39 5F 3A", "shasxeq	r5,r9,r10",
				"06 39 5F 5A", "shsaxeq	r5,r9,r10",
				"06 39 5F 7A", "shsub16eq	r5,r9,r10",
				"06 39 5F FA", "shsub8eq	r5,r9,r10",
				"01 60 00 7E", "smceq	#0xe",
				"01 05 8A 89", "smlabbeq	r5,r9,r10,r8",
				"E1 05 8A C9", "smlabt	r5,r9,r10,r8",
				"07 05 8A 19", "smladeq	r5,r9,r10,r8",
				"07 05 8A 39", "smladxeq	r5,r9,r10,r8",
				"00 E9 58 9A", "smlaleq	r5,r9,r10,r8",
				"00 F9 58 9A", "smlalseq	r5,r9,r10,r8",
				"01 49 58 8A", "smlalbbeq	r5,r9,r10,r8",
				"01 49 58 CA", "smlalbteq	r5,r9,r10,r8",
				"07 49 58 1A", "smlaldeq	r5,r9,r10,r8",
				"07 49 58 3A", "smlaldxeq	r5,r9,r10,r8",
				"01 49 58 AA", "smlaltbeq	r5,r9,r10,r8",
				"01 49 58 EA", "smlaltteq	r5,r9,r10,r8",
				"01 05 8A A9", "smlatbeq	r5,r9,r10,r8",
				"01 05 8A E9", "smlatteq	r5,r9,r10,r8",
				"01 25 8A 89", "smlawbeq	r5,r9,r10,r8",
				"01 25 8A C9", "smlawteq	r5,r9,r10,r8",
				"07 05 8A 59", "smlsdeq	r5,r9,r10,r8",
				"07 05 8A 79", "smlsdxeq	r5,r9,r10,r8",
				"07 49 58 5A", "smlsldeq	r5,r9,r10,r8",
				"07 49 58 7A", "smlsldxeq	r5,r9,r10,r8",
				"07 55 8A 19", "smmlaeq	r5,r9,r10,r8",
				"07 55 8A 39", "smmlareq	r5,r9,r10,r8",
				"07 55 8A D9", "smmlseq	r5,r9,r10,r8",
				"07 55 8A F9", "smmlsreq	r5,r9,r10,r8",
				"07 55 FA 19", "smmuleq	r5,r9,r10",
				"07 55 FA 39", "smmulreq	r5,r9,r10",
				"07 05 FA 19", "smuadeq	r5,r9,r10",
				"07 05 FA 39", "smuadxeq	r5,r9,r10",
				"01 65 0A 89", "smulbbeq	r5,r9,r10",
				"01 65 0A C9", "smulbteq	r5,r9,r10",
				"01 65 0A A9", "smultbeq	r5,r9,r10",
				"01 65 0A E9", "smultteq	r5,r9,r10",
				"00 C9 58 9A", "smulleq	r5,r9,r10,r8",
				"00 D9 58 9A", "smullseq	r5,r9,r10,r8",
				"01 25 0A A9", "smulwbeq	r5,r9,r10",
				"01 25 0A E9", "smulwteq	r5,r9,r10",
				"07 05 FA 59", "smusdeq	r5,r9,r10",
				"07 05 FA 79", "smusdxeq	r5,r9,r10",
				"F8 6D 05 13", "srsda	sp!,#0x13",
				"F8 4D 05 13", "srsda	sp,#0x13",
				"F9 6D 05 13", "srsdb	sp!,#0x13",
				"F9 4D 05 13", "srsdb	sp,#0x13",
				"F8 ED 05 13", "srsia	sp!,#0x13",
				"F8 CD 05 13", "srsia	sp,#0x13",
				"F9 ED 05 13", "srsib	sp!,#0x13",
				"F9 CD 05 13", "srsib	sp,#0x13",
				"06 BC 50 1A", "ssateq	r5,#29,r10",
				"06 BC 50 DA", "ssateq	r5,#29,r10,asr #1",
				"E6 BC 50 5A", "ssat	r5,#29,r10,asr #32",
				"E6 BC 50 9A", "ssat	r5,#29,r10,lsl #1",
				"E6 BC 5F 9A", "ssat	r5,#29,r10,lsl #31",
				"06 AE 5F 3A", "ssat16eq	r5,#15,r10",
				"06 19 5F 5A", "ssaxeq	r5,r9,r10",
				"06 19 5F 7A", "ssub16eq	r5,r9,r10",
				"06 19 5F FA", "ssub8eq	r5,r9,r10",
				"0D 0A B9 21", "stceq	p9,c11,[r10,#-0x84]",
				"ED 2A B9 21", "stc	p9,c11,[r10,#-0x84]!",
				"ED 8A B9 21", "stc	p9,c11,[r10,#0x84]",
				"ED AA B9 21", "stc	p9,c11,[r10,#0x84]!",
				"0C 2A B9 21", "stceq	p9,c11,[r10],#-0x84",
				"EC AA B9 21", "stc	p9,c11,[r10],#0x84",
				"0C 8A B9 00", "stceq	p9,c11,[r10],{0}",
				"EC 8A B9 FF", "stc	p9,c11,[r10],{255}",
				"FD 0A B9 21", "stc2	p9,c11,[r10,#-0x84]",
				"FD 2A B9 21", "stc2	p9,c11,[r10,#-0x84]!",
				"FD 8A B9 21", "stc2	p9,c11,[r10,#0x84]",
				"FD AA B9 21", "stc2	p9,c11,[r10,#0x84]!",
				"FC 2A B9 21", "stc2	p9,c11,[r10],#-0x84",
				"FC AA B9 21", "stc2	p9,c11,[r10],#0x84",
				"FC 8A B9 00", "stc2	p9,c11,[r10],{0}",
				"FC 8A B9 FF", "stc2	p9,c11,[r10],{255}",
				"FD 4A B9 21", "stc2l	p9,c11,[r10,#-0x84]",
				"FD 6A B9 21", "stc2l	p9,c11,[r10,#-0x84]!",
				"FD CA B9 21", "stc2l	p9,c11,[r10,#0x84]",
				"FD EA B9 21", "stc2l	p9,c11,[r10,#0x84]!",
				"FC 6A B9 21", "stc2l	p9,c11,[r10],#-0x84",
				"FC EA B9 21", "stc2l	p9,c11,[r10],#0x84",
				"FC CA B9 00", "stc2l	p9,c11,[r10],{0}",
				"FC CA B9 FF", "stc2l	p9,c11,[r10],{255}",
				"0D 4A B9 21", "stcleq	p9,c11,[r10,#-0x84]",
				"ED 6A B9 21", "stcl	p9,c11,[r10,#-0x84]!",
				"ED CA B9 21", "stcl	p9,c11,[r10,#0x84]",
				"ED EA B9 21", "stcl	p9,c11,[r10,#0x84]!",
				"0C 6A B9 21", "stcleq	p9,c11,[r10],#-0x84",
				"EC EA B9 21", "stcl	p9,c11,[r10],#0x84",
				"0C CA B9 00", "stcleq	p9,c11,[r10],{0}",
				"EC CA B9 FF", "stcl	p9,c11,[r10],{255}",
				"08 AA 42 40", "stmeq	r10!,{r6,r9,lr}",
				"E8 8A 42 40", "stm	r10,{r6,r9,lr}",
				"08 2A 42 40", "stmdaeq	r10!,{r6,r9,lr}",
				"E8 4A 64 20", "stmda	r10,{r5,r10,sp,lr}^",
				"E8 0A 42 40", "stmda	r10,{r6,r9,lr}",
				"09 2A 42 40", "stmdbeq	r10!,{r6,r9,lr}",
				"E9 4A 64 20", "stmdb	r10,{r5,r10,sp,lr}^",
				"E9 0A 42 40", "stmdb	r10,{r6,r9,lr}",
				"08 CA 64 20", "stmiaeq	r10,{r5,r10,sp,lr}^",
				"09 AA 42 40", "stmibeq	r10!,{r6,r9,lr}",
				"E9 CA 64 20", "stmib	r10,{r5,r10,sp,lr}^",
				"E9 8A 42 40", "stmib	r10,{r6,r9,lr}",
				"05 0A 59 87", "streq	r5,[r10,#-0x987]",
				"E5 2A 59 87", "str	r5,[r10,#-0x987]!",
				"E5 8A 59 87", "str	r5,[r10,#0x987]",
				"E5 AA 59 87", "str	r5,[r10,#0x987]!",
				"E7 8A 50 C9", "str	r5,[r10,r9,asr #1]",
				"E7 AA 50 C9", "str	r5,[r10,r9,asr #1]!",
				"E7 8A 50 49", "str	r5,[r10,r9,asr #32]",
				"E7 AA 50 49", "str	r5,[r10,r9,asr #32]!",
				"E7 8A 50 89", "str	r5,[r10,r9,lsl #1]",
				"E7 AA 50 89", "str	r5,[r10,r9,lsl #1]!",
				"E7 8A 5F 89", "str	r5,[r10,r9,lsl #31]",
				"E7 AA 5F 89", "str	r5,[r10,r9,lsl #31]!",
				"E7 8A 50 A9", "str	r5,[r10,r9,lsr #1]",
				"E7 AA 50 A9", "str	r5,[r10,r9,lsr #1]!",
				"E7 8A 50 29", "str	r5,[r10,r9,lsr #32]",
				"E7 AA 50 29", "str	r5,[r10,r9,lsr #32]!",
				"E7 8A 50 E9", "str	r5,[r10,r9,ror #1]",
				"E7 AA 50 E9", "str	r5,[r10,r9,ror #1]!",
				"E7 8A 5F E9", "str	r5,[r10,r9,ror #31]",
				"E7 AA 5F E9", "str	r5,[r10,r9,ror #31]!",
				"E7 8A 50 69", "str	r5,[r10,r9,rrx]",
				"E7 AA 50 69", "str	r5,[r10,r9,rrx]!",
				"E7 8A 50 09", "str	r5,[r10,r9]",
				"E7 AA 50 09", "str	r5,[r10,r9]!",
				"E5 8A 50 00", "str	r5,[r10]",
				"07 0A 50 C9", "streq	r5,[r10,-r9,asr #1]",
				"E7 2A 50 C9", "str	r5,[r10,-r9,asr #1]!",
				"E7 0A 50 49", "str	r5,[r10,-r9,asr #32]",
				"E7 2A 50 49", "str	r5,[r10,-r9,asr #32]!",
				"E7 0A 50 89", "str	r5,[r10,-r9,lsl #1]",
				"E7 2A 50 89", "str	r5,[r10,-r9,lsl #1]!",
				"E7 0A 5F 89", "str	r5,[r10,-r9,lsl #31]",
				"E7 2A 5F 89", "str	r5,[r10,-r9,lsl #31]!",
				"E7 0A 50 A9", "str	r5,[r10,-r9,lsr #1]",
				"E7 2A 50 A9", "str	r5,[r10,-r9,lsr #1]!",
				"E7 0A 50 29", "str	r5,[r10,-r9,lsr #32]",
				"E7 2A 50 29", "str	r5,[r10,-r9,lsr #32]!",
				"E7 0A 50 E9", "str	r5,[r10,-r9,ror #1]",
				"E7 2A 50 E9", "str	r5,[r10,-r9,ror #1]!",
				"E7 0A 5F E9", "str	r5,[r10,-r9,ror #31]",
				"E7 2A 5F E9", "str	r5,[r10,-r9,ror #31]!",
				"E7 0A 50 69", "str	r5,[r10,-r9,rrx]",
				"E7 2A 50 69", "str	r5,[r10,-r9,rrx]!",
				"07 0A 50 09", "streq	r5,[r10,-r9]",
				"E7 2A 50 09", "str	r5,[r10,-r9]!",
				"04 0A 59 87", "streq	r5,[r10],#-0x987",
				"E4 8A 59 87", "str	r5,[r10],#0x987",
				"E6 8A 50 09", "str	r5,[r10],r9",
				"E6 8A 50 C9", "str	r5,[r10],r9,asr #1",
				"E6 8A 50 49", "str	r5,[r10],r9,asr #32",
				"E6 8A 50 89", "str	r5,[r10],r9,lsl #1",
				"E6 8A 5F 89", "str	r5,[r10],r9,lsl #31",
				"E6 8A 50 A9", "str	r5,[r10],r9,lsr #1",
				"E6 8A 50 29", "str	r5,[r10],r9,lsr #32",
				"E6 8A 50 E9", "str	r5,[r10],r9,ror #1",
				"E6 8A 5F E9", "str	r5,[r10],r9,ror #31",
				"E6 8A 50 69", "str	r5,[r10],r9,rrx",
				"E6 0A 50 09", "str	r5,[r10],-r9",
				"06 0A 50 C9", "streq	r5,[r10],-r9,asr #1",
				"E6 0A 50 49", "str	r5,[r10],-r9,asr #32",
				"E6 0A 50 89", "str	r5,[r10],-r9,lsl #1",
				"E6 0A 5F 89", "str	r5,[r10],-r9,lsl #31",
				"E6 0A 50 A9", "str	r5,[r10],-r9,lsr #1",
				"E6 0A 50 29", "str	r5,[r10],-r9,lsr #32",
				"E6 0A 50 E9", "str	r5,[r10],-r9,ror #1",
				"E6 0A 5F E9", "str	r5,[r10],-r9,ror #31",
				"E6 0A 50 69", "str	r5,[r10],-r9,rrx",
				"05 4A 59 87", "strbeq	r5,[r10,#-0x987]",
				"E5 6A 59 87", "strb	r5,[r10,#-0x987]!",
				"E5 CA 59 87", "strb	r5,[r10,#0x987]",
				"E5 EA 59 87", "strb	r5,[r10,#0x987]!",
				"07 CA 50 C9", "strbeq	r5,[r10,r9,asr #1]",
				"E7 EA 50 C9", "strb	r5,[r10,r9,asr #1]!",
				"E7 CA 50 49", "strb	r5,[r10,r9,asr #32]",
				"E7 EA 50 49", "strb	r5,[r10,r9,asr #32]!",
				"E7 CA 50 89", "strb	r5,[r10,r9,lsl #1]",
				"E7 EA 50 89", "strb	r5,[r10,r9,lsl #1]!",
				"E7 CA 5F 89", "strb	r5,[r10,r9,lsl #31]",
				"E7 EA 5F 89", "strb	r5,[r10,r9,lsl #31]!",
				"E7 CA 50 A9", "strb	r5,[r10,r9,lsr #1]",
				"E7 EA 50 A9", "strb	r5,[r10,r9,lsr #1]!",
				"E7 CA 50 29", "strb	r5,[r10,r9,lsr #32]",
				"E7 EA 50 29", "strb	r5,[r10,r9,lsr #32]!",
				"E7 CA 50 E9", "strb	r5,[r10,r9,ror #1]",
				"E7 EA 50 E9", "strb	r5,[r10,r9,ror #1]!",
				"E7 CA 5F E9", "strb	r5,[r10,r9,ror #31]",
				"E7 EA 5F E9", "strb	r5,[r10,r9,ror #31]!",
				"E7 CA 50 69", "strb	r5,[r10,r9,rrx]",
				"E7 EA 50 69", "strb	r5,[r10,r9,rrx]!",
				"E7 CA 50 09", "strb	r5,[r10,r9]",
				"E7 EA 50 09", "strb	r5,[r10,r9]!",
				"E7 4A 50 C9", "strb	r5,[r10,-r9,asr #1]",
				"E7 6A 50 C9", "strb	r5,[r10,-r9,asr #1]!",
				"E7 4A 50 49", "strb	r5,[r10,-r9,asr #32]",
				"E7 6A 50 49", "strb	r5,[r10,-r9,asr #32]!",
				"E7 4A 50 89", "strb	r5,[r10,-r9,lsl #1]",
				"E7 6A 50 89", "strb	r5,[r10,-r9,lsl #1]!",
				"E7 4A 5F 89", "strb	r5,[r10,-r9,lsl #31]",
				"E7 6A 5F 89", "strb	r5,[r10,-r9,lsl #31]!",
				"E7 4A 50 A9", "strb	r5,[r10,-r9,lsr #1]",
				"E7 6A 50 A9", "strb	r5,[r10,-r9,lsr #1]!",
				"E7 4A 50 29", "strb	r5,[r10,-r9,lsr #32]",
				"E7 6A 50 29", "strb	r5,[r10,-r9,lsr #32]!",
				"E7 4A 50 E9", "strb	r5,[r10,-r9,ror #1]",
				"E7 6A 50 E9", "strb	r5,[r10,-r9,ror #1]!",
				"E7 4A 5F E9", "strb	r5,[r10,-r9,ror #31]",
				"E7 6A 5F E9", "strb	r5,[r10,-r9,ror #31]!",
				"E7 4A 50 69", "strb	r5,[r10,-r9,rrx]",
				"E7 6A 50 69", "strb	r5,[r10,-r9,rrx]!",
				"E7 4A 50 09", "strb	r5,[r10,-r9]",
				"E7 6A 50 09", "strb	r5,[r10,-r9]!",
				"05 CA 50 00", "strbeq	r5,[r10]",
				"04 4A 59 87", "strbeq	r5,[r10],#-0x987",
				"E4 CA 59 87", "strb	r5,[r10],#0x987",
				"06 CA 50 09", "strbeq	r5,[r10],r9",
				"06 CA 50 C9", "strbeq	r5,[r10],r9,asr #1",
				"E6 CA 50 49", "strb	r5,[r10],r9,asr #32",
				"E6 CA 50 89", "strb	r5,[r10],r9,lsl #1",
				"E6 CA 5F 89", "strb	r5,[r10],r9,lsl #31",
				"E6 CA 50 A9", "strb	r5,[r10],r9,lsr #1",
				"E6 CA 50 29", "strb	r5,[r10],r9,lsr #32",
				"E6 CA 50 E9", "strb	r5,[r10],r9,ror #1",
				"E6 CA 5F E9", "strb	r5,[r10],r9,ror #31",
				"E6 CA 50 69", "strb	r5,[r10],r9,rrx",
				"E6 4A 50 09", "strb	r5,[r10],-r9",
				"E6 4A 50 C9", "strb	r5,[r10],-r9,asr #1",
				"E6 4A 50 49", "strb	r5,[r10],-r9,asr #32",
				"E6 4A 50 89", "strb	r5,[r10],-r9,lsl #1",
				"E6 4A 5F 89", "strb	r5,[r10],-r9,lsl #31",
				"E6 4A 50 A9", "strb	r5,[r10],-r9,lsr #1",
				"E6 4A 50 29", "strb	r5,[r10],-r9,lsr #32",
				"E6 4A 50 E9", "strb	r5,[r10],-r9,ror #1",
				"E6 4A 5F E9", "strb	r5,[r10],-r9,ror #31",
				"E6 4A 50 69", "strb	r5,[r10],-r9,rrx",
				"04 6A 59 87", "strbteq	r5,[r10],#-0x987",
				"E4 EA 59 87", "strbt	r5,[r10],#0x987",
				"06 EA 50 09", "strbteq	r5,[r10],r9",
				"06 EA 50 C9", "strbteq	r5,[r10],r9,asr #1",
				"E6 EA 50 49", "strbt	r5,[r10],r9,asr #32",
				"E6 EA 50 89", "strbt	r5,[r10],r9,lsl #1",
				"E6 EA 5F 89", "strbt	r5,[r10],r9,lsl #31",
				"E6 EA 50 A9", "strbt	r5,[r10],r9,lsr #1",
				"E6 EA 50 29", "strbt	r5,[r10],r9,lsr #32",
				"E6 EA 50 E9", "strbt	r5,[r10],r9,ror #1",
				"E6 EA 5F E9", "strbt	r5,[r10],r9,ror #31",
				"E6 EA 50 69", "strbt	r5,[r10],r9,rrx",
				"E6 6A 50 09", "strbt	r5,[r10],-r9",
				"E6 6A 50 C9", "strbt	r5,[r10],-r9,asr #1",
				"E6 6A 50 49", "strbt	r5,[r10],-r9,asr #32",
				"E6 6A 50 89", "strbt	r5,[r10],-r9,lsl #1",
				"E6 6A 5F 89", "strbt	r5,[r10],-r9,lsl #31",
				"E6 6A 50 A9", "strbt	r5,[r10],-r9,lsr #1",
				"E6 6A 50 29", "strbt	r5,[r10],-r9,lsr #32",
				"E6 6A 50 E9", "strbt	r5,[r10],-r9,ror #1",
				"E6 6A 5F E9", "strbt	r5,[r10],-r9,ror #31",
				"E6 6A 50 69", "strbt	r5,[r10],-r9,rrx",
				"01 4A 68 F7", "strdeq	r6,r7,[r10,#-0x87]",
				"E1 6A 68 F7", "strd	r6,r7,[r10,#-0x87]!",
				"E1 CA 68 F7", "strd	r6,r7,[r10,#0x87]",
				"E1 EA 68 F7", "strd	r6,r7,[r10,#0x87]!",
				"01 8A 60 F9", "strdeq	r6,r7,[r10,r9]",
				"E1 AA 60 F9", "strd	r6,r7,[r10,r9]!",
				"E1 0A 60 F9", "strd	r6,r7,[r10,-r9]",
				"E1 2A 60 F9", "strd	r6,r7,[r10,-r9]!",
				"01 CA 60 F0", "strdeq	r6,r7,[r10]",
				"E1 CA 60 F0", "strd	r6,r7,[r10]",
				"00 4A 68 F7", "strdeq	r6,r7,[r10],#-0x87",
				"E0 CA 68 F7", "strd	r6,r7,[r10],#0x87",
				"00 8A 60 F9", "strdeq	r6,r7,[r10],r9",
				"E0 0A 60 F9", "strd	r6,r7,[r10],-r9",
				"01 8A 5F 99", "strexeq	r5,r9,[r10]",
				"01 CA 5F 99", "strexbeq	r5,r9,[r10]",
				"01 AA 9F 96", "strexdeq	r9,r6,r7,[r10]",
				"01 EA 5F 99", "strexheq	r5,r9,[r10]",
				"01 4A 58 B7", "strheq	r5,[r10,#-0x87]",
				"E1 6A 58 B7", "strh	r5,[r10,#-0x87]!",
				"E1 CA 58 B7", "strh	r5,[r10,#0x87]",
				"E1 EA 58 B7", "strh	r5,[r10,#0x87]!",
				"01 8A 50 B9", "strheq	r5,[r10,r9]",
				"E1 AA 50 B9", "strh	r5,[r10,r9]!",
				"01 0A 50 B9", "strheq	r5,[r10,-r9]",
				"E1 2A 50 B9", "strh	r5,[r10,-r9]!",
				"01 CA 50 B0", "strheq	r5,[r10]",
				"00 4A 58 B7", "strheq	r5,[r10],#-0x87",
				"E0 CA 58 B7", "strh	r5,[r10],#0x87",
				"00 8A 50 B9", "strheq	r5,[r10],r9",
				"E0 0A 50 B9", "strh	r5,[r10],-r9",
				"E0 EA 50 B0", "strht	r5,[r10]",
				"00 6A 58 B7", "strhteq	r5,[r10],#-0x87",
				"E0 EA 58 B7", "strht	r5,[r10],#0x87",
				"00 AA 50 B9", "strhteq	r5,[r10],r9",
				"E0 2A 50 B9", "strht	r5,[r10],-r9",
				"04 AA 50 00", "strteq	r5,[r10]",
				"04 2A 59 87", "strteq	r5,[r10],#-0x987",
				"E4 AA 59 87", "strt	r5,[r10],#0x987",
				"06 AA 50 09", "strteq	r5,[r10],r9",
				"06 AA 50 C9", "strteq	r5,[r10],r9,asr #1",
				"E6 AA 50 49", "strt	r5,[r10],r9,asr #32",
				"E6 AA 50 89", "strt	r5,[r10],r9,lsl #1",
				"E6 AA 5F 89", "strt	r5,[r10],r9,lsl #31",
				"E6 AA 50 A9", "strt	r5,[r10],r9,lsr #1",
				"E6 AA 50 29", "strt	r5,[r10],r9,lsr #32",
				"E6 AA 50 E9", "strt	r5,[r10],r9,ror #1",
				"E6 AA 5F E9", "strt	r5,[r10],r9,ror #31",
				"E6 AA 50 69", "strt	r5,[r10],r9,rrx",
				"E6 2A 50 09", "strt	r5,[r10],-r9",
				"E6 2A 50 C9", "strt	r5,[r10],-r9,asr #1",
				"E6 2A 50 49", "strt	r5,[r10],-r9,asr #32",
				"E6 2A 50 89", "strt	r5,[r10],-r9,lsl #1",
				"E6 2A 5F 89", "strt	r5,[r10],-r9,lsl #31",
				"E6 2A 50 A9", "strt	r5,[r10],-r9,lsr #1",
				"E6 2A 50 29", "strt	r5,[r10],-r9,lsr #32",
				"E6 2A 50 E9", "strt	r5,[r10],-r9,ror #1",
				"E6 2A 5F E9", "strt	r5,[r10],-r9,ror #31",
				"E6 2A 50 69", "strt	r5,[r10],-r9,rrx",
				"02 4F 50 00", "subeq	r5,pc,#0x0",
				"E2 4F 50 87", "sub	r5,pc,#0x87",
				"02 4A 50 71", "subeq	r5,r10,#0x71",
				"00 49 50 CA", "subeq	r5,r9,r10,asr #1",
				"00 49 56 5A", "subeq	r5,r9,r10,asr r6",
				"E0 49 50 8A", "sub	r5,r9,r10,lsl #1",
				"E0 49 56 1A", "sub	r5,r9,r10,lsl r6",
				"E0 49 50 2A", "sub	r5,r9,r10,lsr #32",
				"E0 49 5F EA", "sub	r5,r9,r10,ror #31",
				"E0 49 50 6A", "sub	r5,r9,r10,rrx",
				"02 5A 51 FE", "subseq	r5,r10,#0x8000003f",
				"00 59 50 4A", "subseq	r5,r9,r10,asr #32",
				"E0 59 5F 8A", "subs	r5,r9,r10,lsl #31",
				"E0 59 50 AA", "subs	r5,r9,r10,lsr #1",
				"00 59 56 3A", "subseq	r5,r9,r10,lsr r6",
				"E0 59 50 EA", "subs	r5,r9,r10,ror #1",
				"E0 59 56 7A", "subs	r5,r9,r10,ror r6",
				"0F AB CE F9", "svceq	#0xabcef9",
				"E1 0A 50 96", "swp	r5,r6,[r10]",
				"E1 4A 50 96", "swpb	r5,r6,[r10]",
				"06 A9 50 7A", "sxtabeq	r5,r9,r10",
				"06 A9 58 7A", "sxtabeq	r5,r9,r10,ror #16",
				"E6 A9 5C 7A", "sxtab	r5,r9,r10,ror #24",
				"E6 A9 54 7A", "sxtab	r5,r9,r10,ror #8",
				"06 89 50 7A", "sxtab16eq	r5,r9,r10",
				"06 89 58 7A", "sxtab16eq	r5,r9,r10,ror #16",
				"E6 89 5C 7A", "sxtab16	r5,r9,r10,ror #24",
				"E6 89 54 7A", "sxtab16	r5,r9,r10,ror #8",
				"06 B9 50 7A", "sxtaheq	r5,r9,r10",
				"06 B9 58 7A", "sxtaheq	r5,r9,r10,ror #16",
				"E6 B9 5C 7A", "sxtah	r5,r9,r10,ror #24",
				"E6 B9 54 7A", "sxtah	r5,r9,r10,ror #8",
				"06 AF 50 79", "sxtbeq	r5,r9",
				"06 AF 58 79", "sxtbeq	r5,r9,ror #16",
				"E6 AF 5C 79", "sxtb	r5,r9,ror #24",
				"E6 AF 54 79", "sxtb	r5,r9,ror #8",
				"06 8F 50 79", "sxtb16eq	r5,r9",
				"06 8F 58 79", "sxtb16eq	r5,r9,ror #16",
				"E6 8F 5C 79", "sxtb16	r5,r9,ror #24",
				"E6 8F 54 79", "sxtb16	r5,r9,ror #8",
				"06 BF 50 79", "sxtheq	r5,r9",
				"06 BF 58 79", "sxtheq	r5,r9,ror #16",
				"E6 BF 5C 79", "sxth	r5,r9,ror #24",
				"E6 BF 54 79", "sxth	r5,r9,ror #8",
				"03 3A 00 71", "teqeq	r10,#0x71",
				"E3 3A 01 FE", "teq	r10,#0x8000003f",
				"01 35 00 CA", "teqeq	r5,r10,asr #1",
				"E1 35 00 4A", "teq	r5,r10,asr #32",
				"01 35 06 5A", "teqeq	r5,r10,asr r6",
				"E1 35 00 8A", "teq	r5,r10,lsl #1",
				"E1 35 0F 8A", "teq	r5,r10,lsl #31",
				"E1 35 06 1A", "teq	r5,r10,lsl r6",
				"E1 35 00 AA", "teq	r5,r10,lsr #1",
				"E1 35 00 2A", "teq	r5,r10,lsr #32",
				"E1 35 06 3A", "teq	r5,r10,lsr r6",
				"E1 35 00 EA", "teq	r5,r10,ror #1",
				"E1 35 0F EA", "teq	r5,r10,ror #31",
				"E1 35 06 7A", "teq	r5,r10,ror r6",
				"E1 35 00 6A", "teq	r5,r10,rrx",
				"03 1A 00 71", "tsteq	r10,#0x71",
				"E3 1A 01 FE", "tst	r10,#0x8000003f",
				"01 15 00 CA", "tsteq	r5,r10,asr #1",
				"E1 15 00 4A", "tst	r5,r10,asr #32",
				"01 15 06 5A", "tsteq	r5,r10,asr r6",
				"E1 15 00 8A", "tst	r5,r10,lsl #1",
				"E1 15 0F 8A", "tst	r5,r10,lsl #31",
				"E1 15 06 1A", "tst	r5,r10,lsl r6",
				"E1 15 00 AA", "tst	r5,r10,lsr #1",
				"E1 15 00 2A", "tst	r5,r10,lsr #32",
				"E1 15 06 3A", "tst	r5,r10,lsr r6",
				"E1 15 00 EA", "tst	r5,r10,ror #1",
				"E1 15 0F EA", "tst	r5,r10,ror #31",
				"E1 15 06 7A", "tst	r5,r10,ror r6",
				"E1 15 00 6A", "tst	r5,r10,rrx",
				"06 59 5F 1A", "uadd16eq	r5,r9,r10",
				"06 59 5F 9A", "uadd8eq	r5,r9,r10",
				"06 59 5F 3A", "uasxeq	r5,r9,r10",
				"07 FF 50 5A", "ubfxeq	r5,r10,#0,#32",
				"E7 FF 50 5A", "ubfx	r5,r10,#0,#32",
				"E7 E0 5F DA", "ubfx	r5,r10,#31,#1",
				"06 79 5F 1A", "uhadd16eq	r5,r9,r10",
				"06 79 5F 9A", "uhadd8eq	r5,r9,r10",
				"06 79 5F 3A", "uhasxeq	r5,r9,r10",
				"06 79 5F 5A", "uhsaxeq	r5,r9,r10",
				"06 79 5F 7A", "uhsub16eq	r5,r9,r10",
				"06 79 5F FA", "uhsub8eq	r5,r9,r10",
				"00 49 58 9A", "umaaleq	r5,r9,r10,r8",
				"00 A9 58 9A", "umlaleq	r5,r9,r10,r8",
				"00 B9 58 9A", "umlalseq	r5,r9,r10,r8",
				"00 89 58 9A", "umulleq	r5,r9,r10,r8",
				"00 99 58 9A", "umullseq	r5,r9,r10,r8",
				"06 69 5F 1A", "uqadd16eq	r5,r9,r10",
				"06 69 5F 9A", "uqadd8eq	r5,r9,r10",
				"06 69 5F 3A", "uqasxeq	r5,r9,r10",
				"06 69 5F 5A", "uqsaxeq	r5,r9,r10",
				"06 69 5F 7A", "uqsub16eq	r5,r9,r10",
				"06 69 5F FA", "uqsub8eq	r5,r9,r10",
				"07 85 FA 19", "usad8eq	r5,r9,r10",
				"07 85 8A 19", "usada8eq	r5,r9,r10,r8",
				"06 FE 50 1A", "usateq	r5,#30,r10",
				"06 FE 50 DA", "usateq	r5,#30,r10,asr #1",
				"E6 FE 50 5A", "usat	r5,#30,r10,asr #32",
				"E6 FE 50 9A", "usat	r5,#30,r10,lsl #1",
				"E6 FE 5F 9A", "usat	r5,#30,r10,lsl #31",
				"06 EF 5F 3A", "usat16eq	r5,#15,r10",
				"06 59 5F 5A", "usaxeq	r5,r9,r10",
				"06 59 5F 7A", "usub16eq	r5,r9,r10",
				"06 59 5F FA", "usub8eq	r5,r9,r10",
				"06 E9 50 7A", "uxtabeq	r5,r9,r10",
				"06 E9 58 7A", "uxtabeq	r5,r9,r10,ror #16",
				"E6 E9 5C 7A", "uxtab	r5,r9,r10,ror #24",
				"E6 E9 54 7A", "uxtab	r5,r9,r10,ror #8",
				"06 C9 50 7A", "uxtab16eq	r5,r9,r10",
				"06 C9 58 7A", "uxtab16eq	r5,r9,r10,ror #16",
				"E6 C9 5C 7A", "uxtab16	r5,r9,r10,ror #24",
				"E6 C9 54 7A", "uxtab16	r5,r9,r10,ror #8",
				"06 F9 50 7A", "uxtaheq	r5,r9,r10",
				"06 F9 58 7A", "uxtaheq	r5,r9,r10,ror #16",
				"E6 F9 5C 7A", "uxtah	r5,r9,r10,ror #24",
				"E6 F9 54 7A", "uxtah	r5,r9,r10,ror #8",
				"06 EF 50 79", "uxtbeq	r5,r9",
				"06 EF 58 79", "uxtbeq	r5,r9,ror #16",
				"E6 EF 5C 79", "uxtb	r5,r9,ror #24",
				"E6 EF 54 79", "uxtb	r5,r9,ror #8",
				"06 CF 50 79", "uxtb16eq	r5,r9",
				"06 CF 58 79", "uxtb16eq	r5,r9,ror #16",
				"E6 CF 5C 79", "uxtb16	r5,r9,ror #24",
				"E6 CF 54 79", "uxtb16	r5,r9,ror #8",
				"06 FF 50 79", "uxtheq	r5,r9",
				"06 FF 58 79", "uxtheq	r5,r9,ror #16",
				"E6 FF 5C 79", "uxth	r5,r9,ror #24",
				"E6 FF 54 79", "uxth	r5,r9,ror #8",
				"03 20 F0 02", "wfeeq",
				"03 20 F0 03", "wfieq",
				"03 20 F0 01", "yieldeq",
		};

		disassembleInstArray(insts, armOptions);
	}

	@Test
	public void testArmVFPInstructions() {

		System.out.println("\n====================== ARM VFP ======================\n");
		String[] insts = {
				"F2 49 57 BA", "vaba.s8	d21,d25,d26",
				"F2 59 57 BA", "vaba.s16	d21,d25,d26",
				"F2 69 57 BA", "vaba.s32	d21,d25,d26",
				"F3 49 57 BA", "vaba.u8	d21,d25,d26",
				"F3 59 57 BA", "vaba.u16	d21,d25,d26",
				"F3 69 57 BA", "vaba.u32	d21,d25,d26",
				"F2 4C 67 FE", "vaba.s8	q11,q14,q15",
				"F2 5C 67 FE", "vaba.s16	q11,q14,q15",
				"F2 6C 67 FE", "vaba.s32	q11,q14,q15",
				"F3 4C 67 FE", "vaba.u8	q11,q14,q15",
				"F3 5C 67 FE", "vaba.u16	q11,q14,q15",
				"F3 6C 67 FE", "vaba.u32	q11,q14,q15",
				"F2 C9 65 AA", "vabal.s8	q11,d25,d26",
				"F2 D9 65 AA", "vabal.s16	q11,d25,d26",
				"F2 E9 65 AA", "vabal.s32	q11,d25,d26",
				"F3 C9 65 AA", "vabal.u8	q11,d25,d26",
				"F3 D9 65 AA", "vabal.u16	q11,d25,d26",
				"F3 E9 65 AA", "vabal.u32	q11,d25,d26",
				"F2 49 57 AA", "vabd.s8	d21,d25,d26",
				"F2 59 57 AA", "vabd.s16	d21,d25,d26",
				"F2 69 57 AA", "vabd.s32	d21,d25,d26",
				"F3 49 57 AA", "vabd.u8	d21,d25,d26",
				"F3 59 57 AA", "vabd.u16	d21,d25,d26",
				"F3 69 57 AA", "vabd.u32	d21,d25,d26",
				"F2 4C 67 EE", "vabd.s8	q11,q14,q15",
				"F2 5C 67 EE", "vabd.s16	q11,q14,q15",
				"F2 6C 67 EE", "vabd.s32	q11,q14,q15",
				"F3 4C 67 EE", "vabd.u8	q11,q14,q15",
				"F3 5C 67 EE", "vabd.u16	q11,q14,q15",
				"F3 6C 67 EE", "vabd.u32	q11,q14,q15",
				"F3 69 5D AA", "vabd.f32	d21,d25,d26",
				"F3 6C 6D EE", "vabd.f32	q11,q14,q15",
				"F2 C9 67 AA", "vabdl.s8	q11,d25,d26",
				"F2 D9 67 AA", "vabdl.s16	q11,d25,d26",
				"F2 E9 67 AA", "vabdl.s32	q11,d25,d26",
				"F3 C9 67 AA", "vabdl.u8	q11,d25,d26",
				"F3 D9 67 AA", "vabdl.u16	q11,d25,d26",
				"F3 E9 67 AA", "vabdl.u32	q11,d25,d26",
				"F3 F1 63 6E", "vabs.s8	q11,q15",
				"F3 F5 63 6E", "vabs.s16	q11,q15",
				"F3 F9 63 6E", "vabs.s32	q11,q15",
				"F3 F9 67 6E", "vabs.f32	q11,q15",
				"F3 F1 53 2A", "vabs.s8	d21,d26",
				"F3 F5 53 2A", "vabs.s16	d21,d26",
				"F3 F9 53 2A", "vabs.s32	d21,d26",
				"F3 F9 57 2A", "vabs.f32	d21,d26",
				"0E F0 5B EA", "vabseq.f64	d21,d26",
				"EE F0 AA CD", "vabs.f32	s21,s26",
				"EE F0 5B EA", "vabs.f64	d21,d26",
				"F3 49 5E BA", "vacge.f32	d21,d25,d26",
				"F3 4C 6E FE", "vacge.f32	q11,q14,q15",
				"F3 69 5E BA", "vacgt.f32	d21,d25,d26",
				"F3 6C 6E FE", "vacgt.f32	q11,q14,q15",
				"F2 49 58 AA", "vadd.i8	d21,d25,d26",
				"F2 59 58 AA", "vadd.i16	d21,d25,d26",
				"F2 69 58 AA", "vadd.i32	d21,d25,d26",
				"F2 79 58 AA", "vadd.i64	d21,d25,d26",
				"F2 4C 68 EE", "vadd.i8	q11,q14,q15",
				"F2 5C 68 EE", "vadd.i16	q11,q14,q15",
				"F2 6C 68 EE", "vadd.i32	q11,q14,q15",
				"F2 7C 68 EE", "vadd.i64	q11,q14,q15",
				"F2 49 5D AA", "vadd.f32	d21,d25,d26",
				"F2 4C 6D EE", "vadd.f32	q11,q14,q15",
				"EE 7C AA 8D", "vadd.f32	s21,s25,s26",
				"0E 79 5B AA", "vaddeq.f64	d21,d25,d26",
				"F2 CC 54 AE", "vaddhn.i16	d21,q14,q15",
				"F2 DC 54 AE", "vaddhn.i32	d21,q14,q15",
				"F2 EC 54 AE", "vaddhn.i64	d21,q14,q15",
				"F2 C9 60 AA", "vaddl.s8	q11,d25,d26",
				"F2 D9 60 AA", "vaddl.s16	q11,d25,d26",
				"F2 E9 60 AA", "vaddl.s32	q11,d25,d26",
				"F3 C9 60 AA", "vaddl.u8	q11,d25,d26",
				"F3 D9 60 AA", "vaddl.u16	q11,d25,d26",
				"F3 E9 60 AA", "vaddl.u32	q11,d25,d26",
				"F2 CC 61 AA", "vaddw.s8	q11,q14,d26",
				"F2 DC 61 AA", "vaddw.s16	q11,q14,d26",
				"F2 EC 61 AA", "vaddw.s32	q11,q14,d26",
				"F3 CC 61 AA", "vaddw.u8	q11,q14,d26",
				"F3 DC 61 AA", "vaddw.u16	q11,q14,d26",
				"F3 EC 61 AA", "vaddw.u32	q11,q14,d26",
				"F2 49 51 BA", "vand	d21,d25,d26",
				"F2 4C 61 FE", "vand	q11,q14,q15",
				"F2 59 51 BA", "vbic	d21,d25,d26",
				"F2 5C 61 FE", "vbic	q11,q14,q15",
				"F3 C0 59 39", "vbic.i16	d21,#0x89",
				"F3 C0 51 39", "vbic.i32	d21,#0x89",
				"F3 C0 69 79", "vbic.i16	q11,#0x89",
				"F3 C0 61 79", "vbic.i32	q11,#0x89",
				"F3 79 51 BA", "vbif	d21,d25,d26",
				"F3 7C 61 FE", "vbif	q11,q14,q15",
				"F3 69 51 BA", "vbit	d21,d25,d26",
				"F3 6C 61 FE", "vbit	q11,q14,q15",
				"F3 59 51 BA", "vbsl	d21,d25,d26",
				"F3 5C 61 FE", "vbsl	q11,q14,q15",
				"F3 49 58 BA", "vceq.i8	d21,d25,d26",
				"F3 59 58 BA", "vceq.i16	d21,d25,d26",
				"F3 69 58 BA", "vceq.i32	d21,d25,d26",
				"F3 F1 51 2A", "vceq.i8	d21,d26,#0",
				"F3 F5 51 2A", "vceq.i16	d21,d26,#0",
				"F3 F9 51 2A", "vceq.i32	d21,d26,#0",
				"F3 F9 55 2A", "vceq.f32	d21,d26,#0",
				"F3 4C 68 FE", "vceq.i8	q11,q14,q15",
				"F3 5C 68 FE", "vceq.i16	q11,q14,q15",
				"F3 6C 68 FE", "vceq.i32	q11,q14,q15",
				"F3 F1 61 6E", "vceq.i8	q11,q15,#0",
				"F3 F5 61 6E", "vceq.i16	q11,q15,#0",
				"F3 F9 61 6E", "vceq.i32	q11,q15,#0",
				"F3 F9 65 6E", "vceq.f32	q11,q15,#0",
				"F2 49 5E AA", "vceq.f32	d21,d25,d26",
				"F2 4C 6E EE", "vceq.f32	q11,q14,q15",
				"F2 49 53 BA", "vcge.s8	d21,d25,d26",
				"F2 59 53 BA", "vcge.s16	d21,d25,d26",
				"F2 69 53 BA", "vcge.s32	d21,d25,d26",
				"F3 49 53 BA", "vcge.u8	d21,d25,d26",
				"F3 59 53 BA", "vcge.u16	d21,d25,d26",
				"F3 69 53 BA", "vcge.u32	d21,d25,d26",
				"F3 F1 50 AA", "vcge.s8	d21,d26,#0",
				"F3 F5 50 AA", "vcge.s16	d21,d26,#0",
				"F3 F9 50 AA", "vcge.s32	d21,d26,#0",
				"F3 F9 54 AA", "vcge.f32	d21,d26,#0",
				"F2 4C 63 FE", "vcge.s8	q11,q14,q15",
				"F2 5C 63 FE", "vcge.s16	q11,q14,q15",
				"F2 6C 63 FE", "vcge.s32	q11,q14,q15",
				"F3 4C 63 FE", "vcge.u8	q11,q14,q15",
				"F3 5C 63 FE", "vcge.u16	q11,q14,q15",
				"F3 6C 63 FE", "vcge.u32	q11,q14,q15",
				"F3 F1 60 EE", "vcge.s8	q11,q15,#0",
				"F3 F5 60 EE", "vcge.s16	q11,q15,#0",
				"F3 F9 60 EE", "vcge.s32	q11,q15,#0",
				"F3 F9 64 EE", "vcge.f32	q11,q15,#0",
				"F3 49 5E AA", "vcge.f32	d21,d25,d26",
				"F3 4C 6E EE", "vcge.f32	q11,q14,q15",
				"F2 49 53 AA", "vcgt.s8	d21,d25,d26",
				"F2 59 53 AA", "vcgt.s16	d21,d25,d26",
				"F2 69 53 AA", "vcgt.s32	d21,d25,d26",
				"F3 49 53 AA", "vcgt.u8	d21,d25,d26",
				"F3 59 53 AA", "vcgt.u16	d21,d25,d26",
				"F3 69 53 AA", "vcgt.u32	d21,d25,d26",
				"F3 F1 50 2A", "vcgt.s8	d21,d26,#0",
				"F3 F5 50 2A", "vcgt.s16	d21,d26,#0",
				"F3 F9 50 2A", "vcgt.s32	d21,d26,#0",
				"F3 F9 54 2A", "vcgt.f32	d21,d26,#0",
				"F2 4C 63 EE", "vcgt.s8	q11,q14,q15",
				"F2 5C 63 EE", "vcgt.s16	q11,q14,q15",
				"F2 6C 63 EE", "vcgt.s32	q11,q14,q15",
				"F3 4C 63 EE", "vcgt.u8	q11,q14,q15",
				"F3 5C 63 EE", "vcgt.u16	q11,q14,q15",
				"F3 6C 63 EE", "vcgt.u32	q11,q14,q15",
				"F3 F1 60 6E", "vcgt.s8	q11,q15,#0",
				"F3 F5 60 6E", "vcgt.s16	q11,q15,#0",
				"F3 F9 60 6E", "vcgt.s32	q11,q15,#0",
				"F3 F9 64 6E", "vcgt.f32	q11,q15,#0",
				"F3 69 5E AA", "vcgt.f32	d21,d25,d26",
				"F3 6C 6E EE", "vcgt.f32	q11,q14,q15",
				"F3 F1 51 AA", "vcle.s8	d21,d26,#0",
				"F3 F5 51 AA", "vcle.s16	d21,d26,#0",
				"F3 F9 51 AA", "vcle.s32	d21,d26,#0",
				"F3 F9 55 AA", "vcle.f32	d21,d26,#0",
				"F3 F1 61 EE", "vcle.s8	q11,q15,#0",
				"F3 F5 61 EE", "vcle.s16	q11,q15,#0",
				"F3 F9 61 EE", "vcle.s32	q11,q15,#0",
				"F3 F9 65 EE", "vcle.f32	q11,q15,#0",
				"F3 F0 54 2A", "vcls.s8	d21,d26",
				"F3 F4 54 2A", "vcls.s16	d21,d26",
				"F3 F8 54 2A", "vcls.s32	d21,d26",
				"F3 F0 64 6E", "vcls.s8	q11,q15",
				"F3 F4 64 6E", "vcls.s16	q11,q15",
				"F3 F8 64 6E", "vcls.s32	q11,q15",
				"F3 F1 52 2A", "vclt.s8	d21,d26,#0",
				"F3 F5 52 2A", "vclt.s16	d21,d26,#0",
				"F3 F9 52 2A", "vclt.s32	d21,d26,#0",
				"F3 F9 56 2A", "vclt.f32	d21,d26,#0",
				"F3 F1 62 6E", "vclt.s8	q11,q15,#0",
				"F3 F5 62 6E", "vclt.s16	q11,q15,#0",
				"F3 F9 62 6E", "vclt.s32	q11,q15,#0",
				"F3 F9 66 6E", "vclt.f32	q11,q15,#0",
				"F3 F0 54 AA", "vclz.i8	d21,d26",
				"F3 F4 54 AA", "vclz.i16	d21,d26",
				"F3 F8 54 AA", "vclz.i32	d21,d26",
				"F3 F0 64 EE", "vclz.i8	q11,q15",
				"F3 F4 64 EE", "vclz.i16	q11,q15",
				"F3 F8 64 EE", "vclz.i32	q11,q15",
				"0E F5 AA 40", "vcmpeq.f32	s21,#0.0",
				"0E F4 AA 4D", "vcmpeq.f32	s21,s26",
				"EE F5 5B 40", "vcmp.f64	d21,#0.0",
				"EE F4 5B 6A", "vcmp.f64	d21,d26",
				"EE F5 AA C0", "vcmpe.f32	s21,#0.0",
				"EE F4 AA CD", "vcmpe.f32	s21,s26",
				"EE F5 5B C0", "vcmpe.f64	d21,#0.0",
				"EE F4 5B EA", "vcmpe.f64	d21,d26",
				"F3 F0 55 2A", "vcnt.8	d21,d26",
				"F3 F0 65 6E", "vcnt.8	q11,q15",
				"F3 FB 57 2A", "vcvt.s32.f32	d21,d26",
				"F3 FB 57 AA", "vcvt.u32.f32	d21,d26",
				"F3 FB 56 2A", "vcvt.f32.s32	d21,d26",
				"F3 FB 56 AA", "vcvt.f32.u32	d21,d26",
				"F2 E0 5F 3A", "vcvt.s32.f32	d21,d26,#32",
				"F3 E0 5F 3A", "vcvt.u32.f32	d21,d26,#32",
				"F2 E0 5E 3A", "vcvt.f32.s32	d21,d26,#32",
				"F3 E0 5E 3A", "vcvt.f32.u32	d21,d26,#32",
				"F3 FB 67 6E", "vcvt.s32.f32	q11,q15",
				"F3 FB 67 EE", "vcvt.u32.f32	q11,q15",
				"F3 FB 66 6E", "vcvt.f32.s32	q11,q15",
				"F3 FB 66 EE", "vcvt.f32.u32	q11,q15",
				"F2 E0 6F 7E", "vcvt.s32.f32	q11,q15,#32",
				"F3 E0 6F 7E", "vcvt.u32.f32	q11,q15,#32",
				"F2 E0 6E 7E", "vcvt.f32.s32	q11,q15,#32",
				"F3 E0 6E 7E", "vcvt.f32.u32	q11,q15,#32",
				"EE FA AA E8", "vcvt.f32.s32	s21,s21,#15",
				"EE FF AA 60", "vcvt.u16.f32	s21,s21,#15",
				"EE FE AA E2", "vcvt.s32.f32	s21,s21,#27",
				"EE FF AA E2", "vcvt.u32.f32	s21,s21,#27",
				"EE FE 5B 60", "vcvt.s16.f64	d21,d21,#15",
				"EE FF 5B 60", "vcvt.u16.f64	d21,d21,#15",
				"EE FE 5B E2", "vcvt.s32.f64	d21,d21,#27",
				"EE FF 5B E2", "vcvt.u32.f64	d21,d21,#27",
				"F3 F6 56 2E", "vcvt.f16.f32	d21,q15",
				"EE FA AA 60", "vcvt.f32.s16	s21,s21,#15",
				"EE FB AA 60", "vcvt.f32.u16	s21,s21,#15",
				"EE FA AA E2", "vcvt.f32.s32	s21,s21,#27",
				"EE FB AA E2", "vcvt.f32.u32	s21,s21,#27",
				"EE F8 AA CD", "vcvt.f32.s32	s21,s26",
				"EE F8 AA 4D", "vcvt.f32.u32	s21,s26",
				"F3 F6 67 2A", "vcvt.f32.f16	q11,d26",
				"EE F7 AB EA", "vcvt.f32.f64	s21,d26",
				"EE FA 5B 60", "vcvt.f64.s16	d21,d21,#15",
				"EE FB 5B 60", "vcvt.f64.u16	d21,d21,#15",
				"EE FA 5B E2", "vcvt.f64.s32	d21,d21,#27",
				"0E FB 5B E2", "vcvteq.f64.u32	d21,d21,#27",
				"EE F8 5B CD", "vcvt.f64.s32	d21,s26",
				"EE F8 5B 4D", "vcvt.f64.u32	d21,s26",
				"0E F7 5A CD", "vcvteq.f64.f32	d21,s26",
				"EE FD AA CD", "vcvt.s32.f32	s21,s26",
				"EE FD AB EA", "vcvt.s32.f64	s21,d26",
				"EE FC AA CD", "vcvt.u32.f32	s21,s26",
				"EE FC AB EA", "vcvt.u32.f64	s21,d26",
				"0E F3 AA 4D", "vcvtbeq.f16.f32	s21,s26",
				"EE F2 AA 4D", "vcvtb.f32.f16	s21,s26",
				"EE F3 AA CD", "vcvtt.f16.f32	s21,s26",
				"EE F2 AA CD", "vcvtt.f32.f16	s21,s26",
				"0E FD AA 4D", "vcvtreq.s32.f32	s21,s26",
				"EE FD AB 6A", "vcvtr.s32.f64	s21,d26",
				"EE FC AA 4D", "vcvtr.u32.f32	s21,s26",
				"EE FC AB 6A", "vcvtr.u32.f64	s21,d26",
				"0E CC AA 8D", "vdiveq.f32	s21,s25,s26",
				"EE C9 5B AA", "vdiv.f64	d21,d25,d26",
				"F3 F5 5C 26", "vdup.8	d21,d22[2]",
				"F3 FA 5C 26", "vdup.16	d21,d22[2]",
				"F3 FC 5C 26", "vdup.32	d21,d22[1]",
				"0E C5 5B 90", "vdupeq.8	d21,r5",
				"EE 85 5B B0", "vdup.16	d21,r5",
				"EE 85 5B 90", "vdup.32	d21,r5",
				"F3 F5 6C 66", "vdup.8	q11,d22[2]",
				"F3 FA 6C 66", "vdup.16	q11,d22[2]",
				"F3 FC 6C 66", "vdup.32	q11,d22[1]",
				"EE E6 5B 90", "vdup.8	q11,r5",
				"EE A6 5B B0", "vdup.16	q11,r5",
				"EE A6 5B 90", "vdup.32	q11,r5",
				"F3 49 51 BA", "veor	d21,d25,d26",
				"F3 4C 61 FE", "veor	q11,q14,q15",
				"F2 F9 55 AA", "vext.8	d21,d25,d26,#5",
				"F2 FC 6D EE", "vext.8	q11,q14,q15,#13",
				"F2 49 50 AA", "vhadd.s8	d21,d25,d26",
				"F2 59 50 AA", "vhadd.s16	d21,d25,d26",
				"F2 69 50 AA", "vhadd.s32	d21,d25,d26",
				"F3 49 50 AA", "vhadd.u8	d21,d25,d26",
				"F3 59 50 AA", "vhadd.u16	d21,d25,d26",
				"F3 69 50 AA", "vhadd.u32	d21,d25,d26",
				"F2 4C 60 EE", "vhadd.s8	q11,q14,q15",
				"F2 5C 60 EE", "vhadd.s16	q11,q14,q15",
				"F2 6C 60 EE", "vhadd.s32	q11,q14,q15",
				"F3 4C 60 EE", "vhadd.u8	q11,q14,q15",
				"F3 5C 60 EE", "vhadd.u16	q11,q14,q15",
				"F3 6C 60 EE", "vhadd.u32	q11,q14,q15",
				"F2 49 52 AA", "vhsub.s8	d21,d25,d26",
				"F2 59 52 AA", "vhsub.s16	d21,d25,d26",
				"F2 69 52 AA", "vhsub.s32	d21,d25,d26",
				"F3 49 52 AA", "vhsub.u8	d21,d25,d26",
				"F3 59 52 AA", "vhsub.u16	d21,d25,d26",
				"F3 69 52 AA", "vhsub.u32	d21,d25,d26",
				"F2 4C 62 EE", "vhsub.s8	q11,q14,q15",
				"F2 5C 62 EE", "vhsub.s16	q11,q14,q15",
				"F2 6C 62 EE", "vhsub.s32	q11,q14,q15",
				"F3 4C 62 EE", "vhsub.u8	q11,q14,q15",
				"F3 5C 62 EE", "vhsub.u16	q11,q14,q15",
				"F3 6C 62 EE", "vhsub.u32	q11,q14,q15",
				"F4 6A B7 0F", "vld1.8	{d27},[r10]",
				"F4 6A BA 0F", "vld1.8	{d27,d28},[r10]",
				"F4 6A B6 0F", "vld1.8	{d27,d28,d29},[r10]",
				"F4 6A B2 0F", "vld1.8	{d27,d28,d29,d30},[r10]",
				"F4 6A B7 4F", "vld1.16	{d27},[r10]",
				"F4 6A BA 4F", "vld1.16	{d27,d28},[r10]",
				"F4 6A B6 4F", "vld1.16	{d27,d28,d29},[r10]",
				"F4 6A B2 4F", "vld1.16	{d27,d28,d29,d30},[r10]",
				"F4 6A B7 8F", "vld1.32	{d27},[r10]",
				"F4 6A BA 8F", "vld1.32	{d27,d28},[r10]",
				"F4 6A B6 8F", "vld1.32	{d27,d28,d29},[r10]",
				"F4 6A B2 8F", "vld1.32	{d27,d28,d29,d30},[r10]",
				"F4 6A B7 CF", "vld1.64	{d27},[r10]",
				"F4 6A BA CF", "vld1.64	{d27,d28},[r10]",
				"F4 6A B6 CF", "vld1.64	{d27,d28,d29},[r10]",
				"F4 6A B2 CF", "vld1.64	{d27,d28,d29,d30},[r10]",
				"F4 6A B7 1F", "vld1.8	{d27},[r10@64]",
				"F4 6A BA 1F", "vld1.8	{d27,d28},[r10@64]",
				"F4 6A BA 2F", "vld1.8	{d27,d28},[r10@128]",
				"F4 6A B6 1F", "vld1.8	{d27,d28,d29},[r10@64]",
				"F4 6A B2 1F", "vld1.8	{d27,d28,d29,d30},[r10@64]",
				"F4 6A B2 2F", "vld1.8	{d27,d28,d29,d30},[r10@128]",
				"F4 6A B2 3F", "vld1.8	{d27,d28,d29,d30},[r10@256]",
				"F4 6A B7 5F", "vld1.16	{d27},[r10@64]",
				"F4 6A BA 5F", "vld1.16	{d27,d28},[r10@64]",
				"F4 6A BA 6F", "vld1.16	{d27,d28},[r10@128]",
				"F4 6A B6 5F", "vld1.16	{d27,d28,d29},[r10@64]",
				"F4 6A B2 5F", "vld1.16	{d27,d28,d29,d30},[r10@64]",
				"F4 6A B2 6F", "vld1.16	{d27,d28,d29,d30},[r10@128]",
				"F4 6A B2 7F", "vld1.16	{d27,d28,d29,d30},[r10@256]",
				"F4 6A B7 9F", "vld1.32	{d27},[r10@64]",
				"F4 6A BA 9F", "vld1.32	{d27,d28},[r10@64]",
				"F4 6A BA AF", "vld1.32	{d27,d28},[r10@128]",
				"F4 6A B6 9F", "vld1.32	{d27,d28,d29},[r10@64]",
				"F4 6A B2 9F", "vld1.32	{d27,d28,d29,d30},[r10@64]",
				"F4 6A B2 AF", "vld1.32	{d27,d28,d29,d30},[r10@128]",
				"F4 6A B2 BF", "vld1.32	{d27,d28,d29,d30},[r10@256]",
				"F4 6A B7 DF", "vld1.64	{d27},[r10@64]",
				"F4 6A BA DF", "vld1.64	{d27,d28},[r10@64]",
				"F4 6A BA EF", "vld1.64	{d27,d28},[r10@128]",
				"F4 6A B6 DF", "vld1.64	{d27,d28,d29},[r10@64]",
				"F4 6A B2 DF", "vld1.64	{d27,d28,d29,d30},[r10@64]",
				"F4 6A B2 EF", "vld1.64	{d27,d28,d29,d30},[r10@128]",
				"F4 6A B2 FF", "vld1.64	{d27,d28,d29,d30},[r10@256]",
				"F4 6A B7 0D", "vld1.8	{d27},[r10]!",
				"F4 6A BA 0D", "vld1.8	{d27,d28},[r10]!",
				"F4 6A B6 0D", "vld1.8	{d27,d28,d29},[r10]!",
				"F4 6A B2 0D", "vld1.8	{d27,d28,d29,d30},[r10]!",
				"F4 6A B7 4D", "vld1.16	{d27},[r10]!",
				"F4 6A BA 4D", "vld1.16	{d27,d28},[r10]!",
				"F4 6A B6 4D", "vld1.16	{d27,d28,d29},[r10]!",
				"F4 6A B2 4D", "vld1.16	{d27,d28,d29,d30},[r10]!",
				"F4 6A B7 8D", "vld1.32	{d27},[r10]!",
				"F4 6A BA 8D", "vld1.32	{d27,d28},[r10]!",
				"F4 6A B6 8D", "vld1.32	{d27,d28,d29},[r10]!",
				"F4 6A B2 8D", "vld1.32	{d27,d28,d29,d30},[r10]!",
				"F4 6A B7 CD", "vld1.64	{d27},[r10]!",
				"F4 6A BA CD", "vld1.64	{d27,d28},[r10]!",
				"F4 6A B6 CD", "vld1.64	{d27,d28,d29},[r10]!",
				"F4 6A B2 CD", "vld1.64	{d27,d28,d29,d30},[r10]!",
				"F4 6A B7 1D", "vld1.8	{d27},[r10@64]!",
				"F4 6A BA 1D", "vld1.8	{d27,d28},[r10@64]!",
				"F4 6A BA 2D", "vld1.8	{d27,d28},[r10@128]!",
				"F4 6A B6 1D", "vld1.8	{d27,d28,d29},[r10@64]!",
				"F4 6A B2 1D", "vld1.8	{d27,d28,d29,d30},[r10@64]!",
				"F4 6A B2 2D", "vld1.8	{d27,d28,d29,d30},[r10@128]!",
				"F4 6A B2 3D", "vld1.8	{d27,d28,d29,d30},[r10@256]!",
				"F4 6A B7 5D", "vld1.16	{d27},[r10@64]!",
				"F4 6A BA 5D", "vld1.16	{d27,d28},[r10@64]!",
				"F4 6A BA 6D", "vld1.16	{d27,d28},[r10@128]!",
				"F4 6A B6 5D", "vld1.16	{d27,d28,d29},[r10@64]!",
				"F4 6A B2 5D", "vld1.16	{d27,d28,d29,d30},[r10@64]!",
				"F4 6A B2 6D", "vld1.16	{d27,d28,d29,d30},[r10@128]!",
				"F4 6A B2 7D", "vld1.16	{d27,d28,d29,d30},[r10@256]!",
				"F4 6A B7 9D", "vld1.32	{d27},[r10@64]!",
				"F4 6A BA 9D", "vld1.32	{d27,d28},[r10@64]!",
				"F4 6A BA AD", "vld1.32	{d27,d28},[r10@128]!",
				"F4 6A B6 9D", "vld1.32	{d27,d28,d29},[r10@64]!",
				"F4 6A B2 9D", "vld1.32	{d27,d28,d29,d30},[r10@64]!",
				"F4 6A B2 AD", "vld1.32	{d27,d28,d29,d30},[r10@128]!",
				"F4 6A B2 BD", "vld1.32	{d27,d28,d29,d30},[r10@256]!",
				"F4 6A B7 DD", "vld1.64	{d27},[r10@64]!",
				"F4 6A BA DD", "vld1.64	{d27,d28},[r10@64]!",
				"F4 6A BA ED", "vld1.64	{d27,d28},[r10@128]!",
				"F4 6A B6 DD", "vld1.64	{d27,d28,d29},[r10@64]!",
				"F4 6A B2 DD", "vld1.64	{d27,d28,d29,d30},[r10@64]!",
				"F4 6A B2 ED", "vld1.64	{d27,d28,d29,d30},[r10@128]!",
				"F4 6A B2 FD", "vld1.64	{d27,d28,d29,d30},[r10@256]!",
				"F4 6A B7 09", "vld1.8	{d27},[r10],r9",
				"F4 6A BA 09", "vld1.8	{d27,d28},[r10],r9",
				"F4 6A B6 09", "vld1.8	{d27,d28,d29},[r10],r9",
				"F4 6A B2 09", "vld1.8	{d27,d28,d29,d30},[r10],r9",
				"F4 6A B7 49", "vld1.16	{d27},[r10],r9",
				"F4 6A BA 49", "vld1.16	{d27,d28},[r10],r9",
				"F4 6A B6 49", "vld1.16	{d27,d28,d29},[r10],r9",
				"F4 6A B2 49", "vld1.16	{d27,d28,d29,d30},[r10],r9",
				"F4 6A B7 89", "vld1.32	{d27},[r10],r9",
				"F4 6A BA 89", "vld1.32	{d27,d28},[r10],r9",
				"F4 6A B6 89", "vld1.32	{d27,d28,d29},[r10],r9",
				"F4 6A B2 89", "vld1.32	{d27,d28,d29,d30},[r10],r9",
				"F4 6A B7 C9", "vld1.64	{d27},[r10],r9",
				"F4 6A BA C9", "vld1.64	{d27,d28},[r10],r9",
				"F4 6A B6 C9", "vld1.64	{d27,d28,d29},[r10],r9",
				"F4 6A B2 C9", "vld1.64	{d27,d28,d29,d30},[r10],r9",
				"F4 6A B7 19", "vld1.8	{d27},[r10@64],r9",
				"F4 6A BA 19", "vld1.8	{d27,d28},[r10@64],r9",
				"F4 6A BA 29", "vld1.8	{d27,d28},[r10@128],r9",
				"F4 6A B6 19", "vld1.8	{d27,d28,d29},[r10@64],r9",
				"F4 6A B2 19", "vld1.8	{d27,d28,d29,d30},[r10@64],r9",
				"F4 6A B2 29", "vld1.8	{d27,d28,d29,d30},[r10@128],r9",
				"F4 6A B2 39", "vld1.8	{d27,d28,d29,d30},[r10@256],r9",
				"F4 6A B7 59", "vld1.16	{d27},[r10@64],r9",
				"F4 6A BA 59", "vld1.16	{d27,d28},[r10@64],r9",
				"F4 6A BA 69", "vld1.16	{d27,d28},[r10@128],r9",
				"F4 6A B6 59", "vld1.16	{d27,d28,d29},[r10@64],r9",
				"F4 6A B2 59", "vld1.16	{d27,d28,d29,d30},[r10@64],r9",
				"F4 6A B2 69", "vld1.16	{d27,d28,d29,d30},[r10@128],r9",
				"F4 6A B2 79", "vld1.16	{d27,d28,d29,d30},[r10@256],r9",
				"F4 6A B7 99", "vld1.32	{d27},[r10@64],r9",
				"F4 6A BA 99", "vld1.32	{d27,d28},[r10@64],r9",
				"F4 6A BA A9", "vld1.32	{d27,d28},[r10@128],r9",
				"F4 6A B6 99", "vld1.32	{d27,d28,d29},[r10@64],r9",
				"F4 6A B2 99", "vld1.32	{d27,d28,d29,d30},[r10@64],r9",
				"F4 6A B2 A9", "vld1.32	{d27,d28,d29,d30},[r10@128],r9",
				"F4 6A B2 B9", "vld1.32	{d27,d28,d29,d30},[r10@256],r9",
				"F4 6A B7 D9", "vld1.64	{d27},[r10@64],r9",
				"F4 6A BA D9", "vld1.64	{d27,d28},[r10@64],r9",
				"F4 6A BA E9", "vld1.64	{d27,d28},[r10@128],r9",
				"F4 6A B6 D9", "vld1.64	{d27,d28,d29},[r10@64],r9",
				"F4 6A B2 D9", "vld1.64	{d27,d28,d29,d30},[r10@64],r9",
				"F4 6A B2 E9", "vld1.64	{d27,d28,d29,d30},[r10@128],r9",
				"F4 6A B2 F9", "vld1.64	{d27,d28,d29,d30},[r10@256],r9",
				"F4 EA B0 2F", "vld1.8	{d27[1]},[r10]",
				"F4 EA B4 4F", "vld1.16	{d27[1]},[r10]",
				"F4 EA B8 8F", "vld1.32	{d27[1]},[r10]",
				"F4 EA B4 5F", "vld1.16	{d27[1]},[r10@16]",
				"F4 EA B8 BF", "vld1.32	{d27[1]},[r10@32]",
				"F4 EA B0 2D", "vld1.8	{d27[1]},[r10]!",
				"F4 EA B4 4D", "vld1.16	{d27[1]},[r10]!",
				"F4 EA B8 8D", "vld1.32	{d27[1]},[r10]!",
				"F4 EA B4 5D", "vld1.16	{d27[1]},[r10@16]!",
				"F4 EA B8 BD", "vld1.32	{d27[1]},[r10@32]!",
				"F4 EA B0 29", "vld1.8	{d27[1]},[r10],r9",
				"F4 EA B4 49", "vld1.16	{d27[1]},[r10],r9",
				"F4 EA B8 89", "vld1.32	{d27[1]},[r10],r9",
				"F4 EA B4 59", "vld1.16	{d27[1]},[r10@16],r9",
				"F4 EA B8 B9", "vld1.32	{d27[1]},[r10@32],r9",
				"F4 EA BC 0F", "vld1.8	{d27[]},[r10]",
				"F4 EA BC 2F", "vld1.8	{d27[],d28[]},[r10]",
				"F4 EA BC 4F", "vld1.16	{d27[]},[r10]",
				"F4 EA BC 6F", "vld1.16	{d27[],d28[]},[r10]",
				"F4 EA BC 8F", "vld1.32	{d27[]},[r10]",
				"F4 EA BC AF", "vld1.32	{d27[],d28[]},[r10]",
				"F4 EA BC 5F", "vld1.16	{d27[]},[r10@16]",
				"F4 EA BC 7F", "vld1.16	{d27[],d28[]},[r10@16]",
				"F4 EA BC 9F", "vld1.32	{d27[]},[r10@32]",
				"F4 EA BC BF", "vld1.32	{d27[],d28[]},[r10@32]",
				"F4 EA BC 0D", "vld1.8	{d27[]},[r10]!",
				"F4 EA BC 2D", "vld1.8	{d27[],d28[]},[r10]!",
				"F4 EA BC 4D", "vld1.16	{d27[]},[r10]!",
				"F4 EA BC 6D", "vld1.16	{d27[],d28[]},[r10]!",
				"F4 EA BC 8D", "vld1.32	{d27[]},[r10]!",
				"F4 EA BC AD", "vld1.32	{d27[],d28[]},[r10]!",
				"F4 EA BC 5D", "vld1.16	{d27[]},[r10@16]!",
				"F4 EA BC 7D", "vld1.16	{d27[],d28[]},[r10@16]!",
				"F4 EA BC 9D", "vld1.32	{d27[]},[r10@32]!",
				"F4 EA BC BD", "vld1.32	{d27[],d28[]},[r10@32]!",
				"F4 EA BC 09", "vld1.8	{d27[]},[r10],r9",
				"F4 EA BC 29", "vld1.8	{d27[],d28[]},[r10],r9",
				"F4 EA BC 49", "vld1.16	{d27[]},[r10],r9",
				"F4 EA BC 69", "vld1.16	{d27[],d28[]},[r10],r9",
				"F4 EA BC 89", "vld1.32	{d27[]},[r10],r9",
				"F4 EA BC A9", "vld1.32	{d27[],d28[]},[r10],r9",
				"F4 EA BC 59", "vld1.16	{d27[]},[r10@16],r9",
				"F4 EA BC 79", "vld1.16	{d27[],d28[]},[r10@16],r9",
				"F4 EA BC 99", "vld1.32	{d27[]},[r10@32],r9",
				"F4 EA BC B9", "vld1.32	{d27[],d28[]},[r10@32],r9",
				"F4 6A B8 0F", "vld2.8	{d27,d28},[r10]",
				"F4 6A B9 0F", "vld2.8	{d27,d29},[r10]",
				"F4 6A B3 0F", "vld2.8	{d27,d28,d29,d30},[r10]",
				"F4 6A B8 4F", "vld2.16	{d27,d28},[r10]",
				"F4 6A B9 4F", "vld2.16	{d27,d29},[r10]",
				"F4 6A B3 4F", "vld2.16	{d27,d28,d29,d30},[r10]",
				"F4 6A B8 8F", "vld2.32	{d27,d28},[r10]",
				"F4 6A B9 8F", "vld2.32	{d27,d29},[r10]",
				"F4 6A B3 8F", "vld2.32	{d27,d28,d29,d30},[r10]",
				"F4 6A B8 1F", "vld2.8	{d27,d28},[r10@64]",
				"F4 6A B8 2F", "vld2.8	{d27,d28},[r10@128]",
				"F4 6A B9 1F", "vld2.8	{d27,d29},[r10@64]",
				"F4 6A B9 2F", "vld2.8	{d27,d29},[r10@128]",
				"F4 6A B3 1F", "vld2.8	{d27,d28,d29,d30},[r10@64]",
				"F4 6A B3 2F", "vld2.8	{d27,d28,d29,d30},[r10@128]",
				"F4 6A B3 3F", "vld2.8	{d27,d28,d29,d30},[r10@256]",
				"F4 6A B8 5F", "vld2.16	{d27,d28},[r10@64]",
				"F4 6A B8 6F", "vld2.16	{d27,d28},[r10@128]",
				"F4 6A B9 5F", "vld2.16	{d27,d29},[r10@64]",
				"F4 6A B9 6F", "vld2.16	{d27,d29},[r10@128]",
				"F4 6A B3 5F", "vld2.16	{d27,d28,d29,d30},[r10@64]",
				"F4 6A B3 6F", "vld2.16	{d27,d28,d29,d30},[r10@128]",
				"F4 6A B3 7F", "vld2.16	{d27,d28,d29,d30},[r10@256]",
				"F4 6A B8 9F", "vld2.32	{d27,d28},[r10@64]",
				"F4 6A B8 AF", "vld2.32	{d27,d28},[r10@128]",
				"F4 6A B9 9F", "vld2.32	{d27,d29},[r10@64]",
				"F4 6A B9 AF", "vld2.32	{d27,d29},[r10@128]",
				"F4 6A B3 9F", "vld2.32	{d27,d28,d29,d30},[r10@64]",
				"F4 6A B3 AF", "vld2.32	{d27,d28,d29,d30},[r10@128]",
				"F4 6A B3 BF", "vld2.32	{d27,d28,d29,d30},[r10@256]",
				"F4 6A B8 0D", "vld2.8	{d27,d28},[r10]!",
				"F4 6A B9 0D", "vld2.8	{d27,d29},[r10]!",
				"F4 6A B3 0D", "vld2.8	{d27,d28,d29,d30},[r10]!",
				"F4 6A B8 4D", "vld2.16	{d27,d28},[r10]!",
				"F4 6A B9 4D", "vld2.16	{d27,d29},[r10]!",
				"F4 6A B3 4D", "vld2.16	{d27,d28,d29,d30},[r10]!",
				"F4 6A B8 8D", "vld2.32	{d27,d28},[r10]!",
				"F4 6A B9 8D", "vld2.32	{d27,d29},[r10]!",
				"F4 6A B3 8D", "vld2.32	{d27,d28,d29,d30},[r10]!",
				"F4 6A B8 1D", "vld2.8	{d27,d28},[r10@64]!",
				"F4 6A B8 2D", "vld2.8	{d27,d28},[r10@128]!",
				"F4 6A B9 1D", "vld2.8	{d27,d29},[r10@64]!",
				"F4 6A B9 2D", "vld2.8	{d27,d29},[r10@128]!",
				"F4 6A B3 1D", "vld2.8	{d27,d28,d29,d30},[r10@64]!",
				"F4 6A B3 2D", "vld2.8	{d27,d28,d29,d30},[r10@128]!",
				"F4 6A B3 3D", "vld2.8	{d27,d28,d29,d30},[r10@256]!",
				"F4 6A B8 5D", "vld2.16	{d27,d28},[r10@64]!",
				"F4 6A B8 6D", "vld2.16	{d27,d28},[r10@128]!",
				"F4 6A B9 5D", "vld2.16	{d27,d29},[r10@64]!",
				"F4 6A B9 6D", "vld2.16	{d27,d29},[r10@128]!",
				"F4 6A B3 5D", "vld2.16	{d27,d28,d29,d30},[r10@64]!",
				"F4 6A B3 6D", "vld2.16	{d27,d28,d29,d30},[r10@128]!",
				"F4 6A B3 7D", "vld2.16	{d27,d28,d29,d30},[r10@256]!",
				"F4 6A B8 9D", "vld2.32	{d27,d28},[r10@64]!",
				"F4 6A B8 AD", "vld2.32	{d27,d28},[r10@128]!",
				"F4 6A B9 9D", "vld2.32	{d27,d29},[r10@64]!",
				"F4 6A B9 AD", "vld2.32	{d27,d29},[r10@128]!",
				"F4 6A B3 9D", "vld2.32	{d27,d28,d29,d30},[r10@64]!",
				"F4 6A B3 AD", "vld2.32	{d27,d28,d29,d30},[r10@128]!",
				"F4 6A B3 BD", "vld2.32	{d27,d28,d29,d30},[r10@256]!",
				"F4 6A B8 09", "vld2.8	{d27,d28},[r10],r9",
				"F4 6A B9 09", "vld2.8	{d27,d29},[r10],r9",
				"F4 6A B3 09", "vld2.8	{d27,d28,d29,d30},[r10],r9",
				"F4 6A B8 49", "vld2.16	{d27,d28},[r10],r9",
				"F4 6A B9 49", "vld2.16	{d27,d29},[r10],r9",
				"F4 6A B3 49", "vld2.16	{d27,d28,d29,d30},[r10],r9",
				"F4 6A B8 89", "vld2.32	{d27,d28},[r10],r9",
				"F4 6A B9 89", "vld2.32	{d27,d29},[r10],r9",
				"F4 6A B3 89", "vld2.32	{d27,d28,d29,d30},[r10],r9",
				"F4 6A B8 19", "vld2.8	{d27,d28},[r10@64],r9",
				"F4 6A B8 29", "vld2.8	{d27,d28},[r10@128],r9",
				"F4 6A B9 19", "vld2.8	{d27,d29},[r10@64],r9",
				"F4 6A B9 29", "vld2.8	{d27,d29},[r10@128],r9",
				"F4 6A B3 19", "vld2.8	{d27,d28,d29,d30},[r10@64],r9",
				"F4 6A B3 29", "vld2.8	{d27,d28,d29,d30},[r10@128],r9",
				"F4 6A B3 39", "vld2.8	{d27,d28,d29,d30},[r10@256],r9",
				"F4 6A B8 59", "vld2.16	{d27,d28},[r10@64],r9",
				"F4 6A B8 69", "vld2.16	{d27,d28},[r10@128],r9",
				"F4 6A B9 59", "vld2.16	{d27,d29},[r10@64],r9",
				"F4 6A B9 69", "vld2.16	{d27,d29},[r10@128],r9",
				"F4 6A B3 59", "vld2.16	{d27,d28,d29,d30},[r10@64],r9",
				"F4 6A B3 69", "vld2.16	{d27,d28,d29,d30},[r10@128],r9",
				"F4 6A B3 79", "vld2.16	{d27,d28,d29,d30},[r10@256],r9",
				"F4 6A B8 99", "vld2.32	{d27,d28},[r10@64],r9",
				"F4 6A B8 A9", "vld2.32	{d27,d28},[r10@128],r9",
				"F4 6A B9 99", "vld2.32	{d27,d29},[r10@64],r9",
				"F4 6A B9 A9", "vld2.32	{d27,d29},[r10@128],r9",
				"F4 6A B3 99", "vld2.32	{d27,d28,d29,d30},[r10@64],r9",
				"F4 6A B3 A9", "vld2.32	{d27,d28,d29,d30},[r10@128],r9",
				"F4 6A B3 B9", "vld2.32	{d27,d28,d29,d30},[r10@256],r9",
				"F4 EA B1 2F", "vld2.8	{d27[1],d28[1]},[r10]",
				"F4 EA B5 4F", "vld2.16	{d27[1],d28[1]},[r10]",
				"F4 EA B5 6F", "vld2.16	{d27[1],d29[1]},[r10]",
				"F4 EA B9 8F", "vld2.32	{d27[1],d28[1]},[r10]",
				"F4 EA B9 CF", "vld2.32	{d27[1],d29[1]},[r10]",
				"F4 EA B1 3F", "vld2.8	{d27[1],d28[1]},[r10@16]",
				"F4 EA B5 5F", "vld2.16	{d27[1],d28[1]},[r10@32]",
				"F4 EA B5 7F", "vld2.16	{d27[1],d29[1]},[r10@32]",
				"F4 EA B9 9F", "vld2.32	{d27[1],d28[1]},[r10@64]",
				"F4 EA B9 DF", "vld2.32	{d27[1],d29[1]},[r10@64]",
				"F4 EA B1 2D", "vld2.8	{d27[1],d28[1]},[r10]!",
				"F4 EA B5 4D", "vld2.16	{d27[1],d28[1]},[r10]!",
				"F4 EA B5 6D", "vld2.16	{d27[1],d29[1]},[r10]!",
				"F4 EA B9 8D", "vld2.32	{d27[1],d28[1]},[r10]!",
				"F4 EA B9 CD", "vld2.32	{d27[1],d29[1]},[r10]!",
				"F4 EA B1 3D", "vld2.8	{d27[1],d28[1]},[r10@16]!",
				"F4 EA B5 5D", "vld2.16	{d27[1],d28[1]},[r10@32]!",
				"F4 EA B5 7D", "vld2.16	{d27[1],d29[1]},[r10@32]!",
				"F4 EA B9 9D", "vld2.32	{d27[1],d28[1]},[r10@64]!",
				"F4 EA B9 DD", "vld2.32	{d27[1],d29[1]},[r10@64]!",
				"F4 EA B1 29", "vld2.8	{d27[1],d28[1]},[r10],r9",
				"F4 EA B5 49", "vld2.16	{d27[1],d28[1]},[r10],r9",
				"F4 EA B5 69", "vld2.16	{d27[1],d29[1]},[r10],r9",
				"F4 EA B9 89", "vld2.32	{d27[1],d28[1]},[r10],r9",
				"F4 EA B9 C9", "vld2.32	{d27[1],d29[1]},[r10],r9",
				"F4 EA B1 39", "vld2.8	{d27[1],d28[1]},[r10@16],r9",
				"F4 EA B5 59", "vld2.16	{d27[1],d28[1]},[r10@32],r9",
				"F4 EA B5 79", "vld2.16	{d27[1],d29[1]},[r10@32],r9",
				"F4 EA B9 99", "vld2.32	{d27[1],d28[1]},[r10@64],r9",
				"F4 EA B9 D9", "vld2.32	{d27[1],d29[1]},[r10@64],r9",
				"F4 EA BD 0F", "vld2.8	{d27[],d28[]},[r10]",
				"F4 EA BD 2F", "vld2.8	{d27[],d29[]},[r10]",
				"F4 EA BD 4F", "vld2.16	{d27[],d28[]},[r10]",
				"F4 EA BD 6F", "vld2.16	{d27[],d29[]},[r10]",
				"F4 EA BD 8F", "vld2.32	{d27[],d28[]},[r10]",
				"F4 EA BD AF", "vld2.32	{d27[],d29[]},[r10]",
				"F4 EA BD 1F", "vld2.8	{d27[],d28[]},[r10@16]",
				"F4 EA BD 3F", "vld2.8	{d27[],d29[]},[r10@16]",
				"F4 EA BD 5F", "vld2.16	{d27[],d28[]},[r10@32]",
				"F4 EA BD 7F", "vld2.16	{d27[],d29[]},[r10@32]",
				"F4 EA BD 9F", "vld2.32	{d27[],d28[]},[r10@64]",
				"F4 EA BD BF", "vld2.32	{d27[],d29[]},[r10@64]",
				"F4 EA BD 0D", "vld2.8	{d27[],d28[]},[r10]!",
				"F4 EA BD 2D", "vld2.8	{d27[],d29[]},[r10]!",
				"F4 EA BD 4D", "vld2.16	{d27[],d28[]},[r10]!",
				"F4 EA BD 6D", "vld2.16	{d27[],d29[]},[r10]!",
				"F4 EA BD 8D", "vld2.32	{d27[],d28[]},[r10]!",
				"F4 EA BD AD", "vld2.32	{d27[],d29[]},[r10]!",
				"F4 EA BD 1D", "vld2.8	{d27[],d28[]},[r10@16]!",
				"F4 EA BD 3D", "vld2.8	{d27[],d29[]},[r10@16]!",
				"F4 EA BD 5D", "vld2.16	{d27[],d28[]},[r10@32]!",
				"F4 EA BD 7D", "vld2.16	{d27[],d29[]},[r10@32]!",
				"F4 EA BD 9D", "vld2.32	{d27[],d28[]},[r10@64]!",
				"F4 EA BD BD", "vld2.32	{d27[],d29[]},[r10@64]!",
				"F4 EA BD 09", "vld2.8	{d27[],d28[]},[r10],r9",
				"F4 EA BD 29", "vld2.8	{d27[],d29[]},[r10],r9",
				"F4 EA BD 49", "vld2.16	{d27[],d28[]},[r10],r9",
				"F4 EA BD 69", "vld2.16	{d27[],d29[]},[r10],r9",
				"F4 EA BD 89", "vld2.32	{d27[],d28[]},[r10],r9",
				"F4 EA BD A9", "vld2.32	{d27[],d29[]},[r10],r9",
				"F4 EA BD 19", "vld2.8	{d27[],d28[]},[r10@16],r9",
				"F4 EA BD 39", "vld2.8	{d27[],d29[]},[r10@16],r9",
				"F4 EA BD 59", "vld2.16	{d27[],d28[]},[r10@32],r9",
				"F4 EA BD 79", "vld2.16	{d27[],d29[]},[r10@32],r9",
				"F4 EA BD 99", "vld2.32	{d27[],d28[]},[r10@64],r9",
				"F4 EA BD B9", "vld2.32	{d27[],d29[]},[r10@64],r9",
				"F4 6A B4 0F", "vld3.8	{d27,d28,d29},[r10]",
				"F4 6A B5 0F", "vld3.8	{d27,d29,d31},[r10]",
				"F4 6A B4 4F", "vld3.16	{d27,d28,d29},[r10]",
				"F4 6A B5 4F", "vld3.16	{d27,d29,d31},[r10]",
				"F4 6A B4 8F", "vld3.32	{d27,d28,d29},[r10]",
				"F4 6A B5 8F", "vld3.32	{d27,d29,d31},[r10]",
				"F4 6A B4 1F", "vld3.8	{d27,d28,d29},[r10@64]",
				"F4 6A B5 1F", "vld3.8	{d27,d29,d31},[r10@64]",
				"F4 6A B4 5F", "vld3.16	{d27,d28,d29},[r10@64]",
				"F4 6A B5 5F", "vld3.16	{d27,d29,d31},[r10@64]",
				"F4 6A B4 9F", "vld3.32	{d27,d28,d29},[r10@64]",
				"F4 6A B5 9F", "vld3.32	{d27,d29,d31},[r10@64]",
				"F4 6A B4 0D", "vld3.8	{d27,d28,d29},[r10]!",
				"F4 6A B5 0D", "vld3.8	{d27,d29,d31},[r10]!",
				"F4 6A B4 4D", "vld3.16	{d27,d28,d29},[r10]!",
				"F4 6A B5 4D", "vld3.16	{d27,d29,d31},[r10]!",
				"F4 6A B4 8D", "vld3.32	{d27,d28,d29},[r10]!",
				"F4 6A B5 8D", "vld3.32	{d27,d29,d31},[r10]!",
				"F4 6A B4 1D", "vld3.8	{d27,d28,d29},[r10@64]!",
				"F4 6A B5 1D", "vld3.8	{d27,d29,d31},[r10@64]!",
				"F4 6A B4 5D", "vld3.16	{d27,d28,d29},[r10@64]!",
				"F4 6A B5 5D", "vld3.16	{d27,d29,d31},[r10@64]!",
				"F4 6A B4 9D", "vld3.32	{d27,d28,d29},[r10@64]!",
				"F4 6A B5 9D", "vld3.32	{d27,d29,d31},[r10@64]!",
				"F4 6A B4 09", "vld3.8	{d27,d28,d29},[r10],r9",
				"F4 6A B5 09", "vld3.8	{d27,d29,d31},[r10],r9",
				"F4 6A B4 49", "vld3.16	{d27,d28,d29},[r10],r9",
				"F4 6A B5 49", "vld3.16	{d27,d29,d31},[r10],r9",
				"F4 6A B4 89", "vld3.32	{d27,d28,d29},[r10],r9",
				"F4 6A B5 89", "vld3.32	{d27,d29,d31},[r10],r9",
				"F4 6A B4 19", "vld3.8	{d27,d28,d29},[r10@64],r9",
				"F4 6A B5 19", "vld3.8	{d27,d29,d31},[r10@64],r9",
				"F4 6A B4 59", "vld3.16	{d27,d28,d29},[r10@64],r9",
				"F4 6A B5 59", "vld3.16	{d27,d29,d31},[r10@64],r9",
				"F4 6A B4 99", "vld3.32	{d27,d28,d29},[r10@64],r9",
				"F4 6A B5 99", "vld3.32	{d27,d29,d31},[r10@64],r9",
				"F4 EA B2 2F", "vld3.8	{d27[1],d28[1],d29[1]},[r10]",
				"F4 EA B6 4F", "vld3.16	{d27[1],d28[1],d29[1]},[r10]",
				"F4 EA B6 6F", "vld3.16	{d27[1],d29[1],d31[1]},[r10]",
				"F4 EA BA 8F", "vld3.32	{d27[1],d28[1],d29[1]},[r10]",
				"F4 EA BA CF", "vld3.32	{d27[1],d29[1],d31[1]},[r10]",
				"F4 EA B2 2D", "vld3.8	{d27[1],d28[1],d29[1]},[r10]!",
				"F4 EA B6 4D", "vld3.16	{d27[1],d28[1],d29[1]},[r10]!",
				"F4 EA B6 6D", "vld3.16	{d27[1],d29[1],d31[1]},[r10]!",
				"F4 EA BA 8D", "vld3.32	{d27[1],d28[1],d29[1]},[r10]!",
				"F4 EA BA CD", "vld3.32	{d27[1],d29[1],d31[1]},[r10]!",
				"F4 EA B2 29", "vld3.8	{d27[1],d28[1],d29[1]},[r10],r9",
				"F4 EA B6 49", "vld3.16	{d27[1],d28[1],d29[1]},[r10],r9",
				"F4 EA B6 69", "vld3.16	{d27[1],d29[1],d31[1]},[r10],r9",
				"F4 EA BA 89", "vld3.32	{d27[1],d28[1],d29[1]},[r10],r9",
				"F4 EA BA C9", "vld3.32	{d27[1],d29[1],d31[1]},[r10],r9",
				"F4 EA BE 0F", "vld3.8	{d27[],d28[],d29[]},[r10]",
				"F4 EA BE 2F", "vld3.8	{d27[],d29[],d31[]},[r10]",
				"F4 EA BE 4F", "vld3.16	{d27[],d28[],d29[]},[r10]",
				"F4 EA BE 6F", "vld3.16	{d27[],d29[],d31[]},[r10]",
				"F4 EA BE 8F", "vld3.32	{d27[],d28[],d29[]},[r10]",
				"F4 EA BE AF", "vld3.32	{d27[],d29[],d31[]},[r10]",
				"F4 EA BE 0D", "vld3.8	{d27[],d28[],d29[]},[r10]!",
				"F4 EA BE 2D", "vld3.8	{d27[],d29[],d31[]},[r10]!",
				"F4 EA BE 4D", "vld3.16	{d27[],d28[],d29[]},[r10]!",
				"F4 EA BE 6D", "vld3.16	{d27[],d29[],d31[]},[r10]!",
				"F4 EA BE 8D", "vld3.32	{d27[],d28[],d29[]},[r10]!",
				"F4 EA BE AD", "vld3.32	{d27[],d29[],d31[]},[r10]!",
				"F4 EA BE 09", "vld3.8	{d27[],d28[],d29[]},[r10],r9",
				"F4 EA BE 29", "vld3.8	{d27[],d29[],d31[]},[r10],r9",
				"F4 EA BE 49", "vld3.16	{d27[],d28[],d29[]},[r10],r9",
				"F4 EA BE 69", "vld3.16	{d27[],d29[],d31[]},[r10],r9",
				"F4 EA BE 89", "vld3.32	{d27[],d28[],d29[]},[r10],r9",
				"F4 EA BE A9", "vld3.32	{d27[],d29[],d31[]},[r10],r9",
				"F4 6A B0 0F", "vld4.8	{d27,d28,d29,d30},[r10]",
				"F4 6A 91 0F", "vld4.8	{d25,d27,d29,d31},[r10]",
				"F4 6A B0 4F", "vld4.16	{d27,d28,d29,d30},[r10]",
				"F4 6A 91 4F", "vld4.16	{d25,d27,d29,d31},[r10]",
				"F4 6A B0 8F", "vld4.32	{d27,d28,d29,d30},[r10]",
				"F4 6A 91 8F", "vld4.32	{d25,d27,d29,d31},[r10]",
				"F4 6A B0 1F", "vld4.8	{d27,d28,d29,d30},[r10@64]",
				"F4 6A B0 2F", "vld4.8	{d27,d28,d29,d30},[r10@128]",
				"F4 6A B0 3F", "vld4.8	{d27,d28,d29,d30},[r10@256]",
				"F4 6A 91 1F", "vld4.8	{d25,d27,d29,d31},[r10@64]",
				"F4 6A 91 2F", "vld4.8	{d25,d27,d29,d31},[r10@128]",
				"F4 6A 91 3F", "vld4.8	{d25,d27,d29,d31},[r10@256]",
				"F4 6A B0 5F", "vld4.16	{d27,d28,d29,d30},[r10@64]",
				"F4 6A B0 6F", "vld4.16	{d27,d28,d29,d30},[r10@128]",
				"F4 6A B0 7F", "vld4.16	{d27,d28,d29,d30},[r10@256]",
				"F4 6A 91 5F", "vld4.16	{d25,d27,d29,d31},[r10@64]",
				"F4 6A 91 6F", "vld4.16	{d25,d27,d29,d31},[r10@128]",
				"F4 6A 91 7F", "vld4.16	{d25,d27,d29,d31},[r10@256]",
				"F4 6A B0 9F", "vld4.32	{d27,d28,d29,d30},[r10@64]",
				"F4 6A B0 AF", "vld4.32	{d27,d28,d29,d30},[r10@128]",
				"F4 6A B0 BF", "vld4.32	{d27,d28,d29,d30},[r10@256]",
				"F4 6A 91 9F", "vld4.32	{d25,d27,d29,d31},[r10@64]",
				"F4 6A 91 AF", "vld4.32	{d25,d27,d29,d31},[r10@128]",
				"F4 6A 91 BF", "vld4.32	{d25,d27,d29,d31},[r10@256]",
				"F4 6A B0 0D", "vld4.8	{d27,d28,d29,d30},[r10]!",
				"F4 6A 91 0D", "vld4.8	{d25,d27,d29,d31},[r10]!",
				"F4 6A B0 4D", "vld4.16	{d27,d28,d29,d30},[r10]!",
				"F4 6A 91 4D", "vld4.16	{d25,d27,d29,d31},[r10]!",
				"F4 6A B0 8D", "vld4.32	{d27,d28,d29,d30},[r10]!",
				"F4 6A 91 8D", "vld4.32	{d25,d27,d29,d31},[r10]!",
				"F4 6A B0 1D", "vld4.8	{d27,d28,d29,d30},[r10@64]!",
				"F4 6A B0 2D", "vld4.8	{d27,d28,d29,d30},[r10@128]!",
				"F4 6A B0 3D", "vld4.8	{d27,d28,d29,d30},[r10@256]!",
				"F4 6A 91 1D", "vld4.8	{d25,d27,d29,d31},[r10@64]!",
				"F4 6A 91 2D", "vld4.8	{d25,d27,d29,d31},[r10@128]!",
				"F4 6A 91 3D", "vld4.8	{d25,d27,d29,d31},[r10@256]!",
				"F4 6A B0 5D", "vld4.16	{d27,d28,d29,d30},[r10@64]!",
				"F4 6A B0 6D", "vld4.16	{d27,d28,d29,d30},[r10@128]!",
				"F4 6A B0 7D", "vld4.16	{d27,d28,d29,d30},[r10@256]!",
				"F4 6A 91 5D", "vld4.16	{d25,d27,d29,d31},[r10@64]!",
				"F4 6A 91 6D", "vld4.16	{d25,d27,d29,d31},[r10@128]!",
				"F4 6A 91 7D", "vld4.16	{d25,d27,d29,d31},[r10@256]!",
				"F4 6A B0 9D", "vld4.32	{d27,d28,d29,d30},[r10@64]!",
				"F4 6A B0 AD", "vld4.32	{d27,d28,d29,d30},[r10@128]!",
				"F4 6A B0 BD", "vld4.32	{d27,d28,d29,d30},[r10@256]!",
				"F4 6A 91 9D", "vld4.32	{d25,d27,d29,d31},[r10@64]!",
				"F4 6A 91 AD", "vld4.32	{d25,d27,d29,d31},[r10@128]!",
				"F4 6A 91 BD", "vld4.32	{d25,d27,d29,d31},[r10@256]!",
				"F4 6A B0 09", "vld4.8	{d27,d28,d29,d30},[r10],r9",
				"F4 6A 91 09", "vld4.8	{d25,d27,d29,d31},[r10],r9",
				"F4 6A B0 49", "vld4.16	{d27,d28,d29,d30},[r10],r9",
				"F4 6A 91 49", "vld4.16	{d25,d27,d29,d31},[r10],r9",
				"F4 6A B0 89", "vld4.32	{d27,d28,d29,d30},[r10],r9",
				"F4 6A 91 89", "vld4.32	{d25,d27,d29,d31},[r10],r9",
				"F4 6A B0 19", "vld4.8	{d27,d28,d29,d30},[r10@64],r9",
				"F4 6A B0 29", "vld4.8	{d27,d28,d29,d30},[r10@128],r9",
				"F4 6A B0 39", "vld4.8	{d27,d28,d29,d30},[r10@256],r9",
				"F4 6A 91 19", "vld4.8	{d25,d27,d29,d31},[r10@64],r9",
				"F4 6A 91 29", "vld4.8	{d25,d27,d29,d31},[r10@128],r9",
				"F4 6A 91 39", "vld4.8	{d25,d27,d29,d31},[r10@256],r9",
				"F4 6A B0 59", "vld4.16	{d27,d28,d29,d30},[r10@64],r9",
				"F4 6A B0 69", "vld4.16	{d27,d28,d29,d30},[r10@128],r9",
				"F4 6A B0 79", "vld4.16	{d27,d28,d29,d30},[r10@256],r9",
				"F4 6A 91 59", "vld4.16	{d25,d27,d29,d31},[r10@64],r9",
				"F4 6A 91 69", "vld4.16	{d25,d27,d29,d31},[r10@128],r9",
				"F4 6A 91 79", "vld4.16	{d25,d27,d29,d31},[r10@256],r9",
				"F4 6A B0 99", "vld4.32	{d27,d28,d29,d30},[r10@64],r9",
				"F4 6A B0 A9", "vld4.32	{d27,d28,d29,d30},[r10@128],r9",
				"F4 6A B0 B9", "vld4.32	{d27,d28,d29,d30},[r10@256],r9",
				"F4 6A 91 99", "vld4.32	{d25,d27,d29,d31},[r10@64],r9",
				"F4 6A 91 A9", "vld4.32	{d25,d27,d29,d31},[r10@128],r9",
				"F4 6A 91 B9", "vld4.32	{d25,d27,d29,d31},[r10@256],r9",
				"F4 EA B3 2F", "vld4.8	{d27[1],d28[1],d29[1],d30[1]},[r10]",
				"F4 EA B7 4F", "vld4.16	{d27[1],d28[1],d29[1],d30[1]},[r10]",
				"F4 EA 97 6F", "vld4.16	{d25[1],d27[1],d29[1],d31[1]},[r10]",
				"F4 EA BB 8F", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10]",
				"F4 EA 9B CF", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10]",
				"F4 EA B3 3F", "vld4.8	{d27[1],d28[1],d29[1],d30[1]},[r10@32]",
				"F4 EA B7 5F", "vld4.16	{d27[1],d28[1],d29[1],d30[1]},[r10@64]",
				"F4 EA 97 7F", "vld4.16	{d25[1],d27[1],d29[1],d31[1]},[r10@64]",
				"F4 EA BB 9F", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@64]",
				"F4 EA BB AF", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@128]",
				"F4 EA 9B DF", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@64]",
				"F4 EA 9B EF", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@128]",
				"F4 EA B3 2D", "vld4.8	{d27[1],d28[1],d29[1],d30[1]},[r10]!",
				"F4 EA B7 4D", "vld4.16	{d27[1],d28[1],d29[1],d30[1]},[r10]!",
				"F4 EA 97 6D", "vld4.16	{d25[1],d27[1],d29[1],d31[1]},[r10]!",
				"F4 EA BB 8D", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10]!",
				"F4 EA 9B CD", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10]!",
				"F4 EA B3 3D", "vld4.8	{d27[1],d28[1],d29[1],d30[1]},[r10@32]!",
				"F4 EA B7 5D", "vld4.16	{d27[1],d28[1],d29[1],d30[1]},[r10@64]!",
				"F4 EA 97 7D", "vld4.16	{d25[1],d27[1],d29[1],d31[1]},[r10@64]!",
				"F4 EA BB 9D", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@64]!",
				"F4 EA BB AD", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@128]!",
				"F4 EA 9B DD", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@64]!",
				"F4 EA 9B ED", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@128]!",
				"F4 EA B3 29", "vld4.8	{d27[1],d28[1],d29[1],d30[1]},[r10],r9",
				"F4 EA B7 49", "vld4.16	{d27[1],d28[1],d29[1],d30[1]},[r10],r9",
				"F4 EA 97 69", "vld4.16	{d25[1],d27[1],d29[1],d31[1]},[r10],r9",
				"F4 EA BB 89", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10],r9",
				"F4 EA 9B C9", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10],r9",
				"F4 EA B3 39", "vld4.8	{d27[1],d28[1],d29[1],d30[1]},[r10@32],r9",
				"F4 EA B7 59", "vld4.16	{d27[1],d28[1],d29[1],d30[1]},[r10@64],r9",
				"F4 EA 97 79", "vld4.16	{d25[1],d27[1],d29[1],d31[1]},[r10@64],r9",
				"F4 EA BB 99", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@64],r9",
				"F4 EA BB A9", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@128],r9",
				"F4 EA 9B D9", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@64],r9",
				"F4 EA 9B E9", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@128],r9",
				"F4 EA BF 0F", "vld4.8	{d27[],d28[],d29[],d30[]},[r10]",
				"F4 EA 9F 2F", "vld4.8	{d25[],d27[],d29[],d31[]},[r10]",
				"F4 EA BF 4F", "vld4.16	{d27[],d28[],d29[],d30[]},[r10]",
				"F4 EA 9F 6F", "vld4.16	{d25[],d27[],d29[],d31[]},[r10]",
				"F4 EA BF 8F", "vld4.32	{d27[],d28[],d29[],d30[]},[r10]",
				"F4 EA 9F AF", "vld4.32	{d25[],d27[],d29[],d31[]},[r10]",
				"F4 EA BF 1F", "vld4.8	{d27[],d28[],d29[],d30[]},[r10@32]",
				"F4 EA 9F 3F", "vld4.8	{d25[],d27[],d29[],d31[]},[r10@32]",
				"F4 EA BF 5F", "vld4.16	{d27[],d28[],d29[],d30[]},[r10@64]",
				"F4 EA 9F 7F", "vld4.16	{d25[],d27[],d29[],d31[]},[r10@64]",
				"F4 EA BF 9F", "vld4.32	{d27[],d28[],d29[],d30[]},[r10@64]",
				"F4 EA BF DF", "vld4.32	{d27[],d28[],d29[],d30[]},[r10@128]",
				"F4 EA 9F BF", "vld4.32	{d25[],d27[],d29[],d31[]},[r10@64]",
				"F4 EA 9F FF", "vld4.32	{d25[],d27[],d29[],d31[]},[r10@128]",
				"F4 EA BF 0D", "vld4.8	{d27[],d28[],d29[],d30[]},[r10]!",
				"F4 EA 9F 2D", "vld4.8	{d25[],d27[],d29[],d31[]},[r10]!",
				"F4 EA BF 4D", "vld4.16	{d27[],d28[],d29[],d30[]},[r10]!",
				"F4 EA 9F 6D", "vld4.16	{d25[],d27[],d29[],d31[]},[r10]!",
				"F4 EA BF 8D", "vld4.32	{d27[],d28[],d29[],d30[]},[r10]!",
				"F4 EA 9F AD", "vld4.32	{d25[],d27[],d29[],d31[]},[r10]!",
				"F4 EA BF 1D", "vld4.8	{d27[],d28[],d29[],d30[]},[r10@32]!",
				"F4 EA 9F 3D", "vld4.8	{d25[],d27[],d29[],d31[]},[r10@32]!",
				"F4 EA BF 5D", "vld4.16	{d27[],d28[],d29[],d30[]},[r10@64]!",
				"F4 EA 9F 7D", "vld4.16	{d25[],d27[],d29[],d31[]},[r10@64]!",
				"F4 EA BF 9D", "vld4.32	{d27[],d28[],d29[],d30[]},[r10@64]!",
				"F4 EA BF DD", "vld4.32	{d27[],d28[],d29[],d30[]},[r10@128]!",
				"F4 EA 9F BD", "vld4.32	{d25[],d27[],d29[],d31[]},[r10@64]!",
				"F4 EA 9F FD", "vld4.32	{d25[],d27[],d29[],d31[]},[r10@128]!",
				"F4 EA BF 09", "vld4.8	{d27[],d28[],d29[],d30[]},[r10],r9",
				"F4 EA 9F 29", "vld4.8	{d25[],d27[],d29[],d31[]},[r10],r9",
				"F4 EA BF 49", "vld4.16	{d27[],d28[],d29[],d30[]},[r10],r9",
				"F4 EA 9F 69", "vld4.16	{d25[],d27[],d29[],d31[]},[r10],r9",
				"F4 EA BF 89", "vld4.32	{d27[],d28[],d29[],d30[]},[r10],r9",
				"F4 EA 9F A9", "vld4.32	{d25[],d27[],d29[],d31[]},[r10],r9",
				"F4 EA BF 19", "vld4.8	{d27[],d28[],d29[],d30[]},[r10@32],r9",
				"F4 EA 9F 39", "vld4.8	{d25[],d27[],d29[],d31[]},[r10@32],r9",
				"F4 EA BF 59", "vld4.16	{d27[],d28[],d29[],d30[]},[r10@64],r9",
				"F4 EA 9F 79", "vld4.16	{d25[],d27[],d29[],d31[]},[r10@64],r9",
				"F4 EA BF 99", "vld4.32	{d27[],d28[],d29[],d30[]},[r10@64],r9",
				"F4 EA BF D9", "vld4.32	{d27[],d28[],d29[],d30[]},[r10@128],r9",
				"F4 EA 9F B9", "vld4.32	{d25[],d27[],d29[],d31[]},[r10@64],r9",
				"F4 EA 9F F9", "vld4.32	{d25[],d27[],d29[],d31[]},[r10@128],r9",
				"0C BA AA 03", "vldmiaeq	r10!,{s20-s22}",
				"ED 3A AA 03", "vldmdb	r10!,{s20-s22}",
				"EC FA 4B 06", "vldmia	r10!,{d20-d22}",
				"ED 7A 4B 06", "vldmdb	r10!,{d20-d22}",
				"EC 9A AA 03", "vldmia	r10,{s20-s22}",
				"0C DA 4B 06", "vldmiaeq	r10,{d20-d22}",
				"0D 5F 5B 21", "vldreq.64	d21,[pc,#-0x84]",
				"ED DF 5B 21", "vldr.64	d21,[pc,#0x84]",
				"ED DA 5B 00", "vldr.64	d21,[r10]",
				"ED 5A 5B 21", "vldr.64	d21,[r10,#-0x84]",
				"ED DA 5B 00", "vldr.64	d21,[r10]",
				"ED DA 5B 21", "vldr.64	d21,[r10,#0x84]",
				"0D 5F AA 21", "vldreq.32	s21,[pc,#-0x84]",
				"ED DF AA 21", "vldr.32	s21,[pc,#0x84]",
				"ED DA AA 00", "vldr.32	s21,[r10]",
				"ED 5A AA 21", "vldr.32	s21,[r10,#-0x84]",
				"ED DA AA 00", "vldr.32	s21,[r10]",
				"ED DA AA 21", "vldr.32	s21,[r10,#0x84]",
				"F2 49 56 AA", "vmax.s8	d21,d25,d26",
				"F2 59 56 AA", "vmax.s16	d21,d25,d26",
				"F2 69 56 AA", "vmax.s32	d21,d25,d26",
				"F3 49 56 AA", "vmax.u8	d21,d25,d26",
				"F3 59 56 AA", "vmax.u16	d21,d25,d26",
				"F3 69 56 AA", "vmax.u32	d21,d25,d26",
				"F2 4C 66 EE", "vmax.s8	q11,q14,q15",
				"F2 5C 66 EE", "vmax.s16	q11,q14,q15",
				"F2 6C 66 EE", "vmax.s32	q11,q14,q15",
				"F3 4C 66 EE", "vmax.u8	q11,q14,q15",
				"F3 5C 66 EE", "vmax.u16	q11,q14,q15",
				"F3 6C 66 EE", "vmax.u32	q11,q14,q15",
				"F2 49 5F AA", "vmax.f32	d21,d25,d26",
				"F2 4C 6F EE", "vmax.f32	q11,q14,q15",
				"F2 49 56 BA", "vmin.s8	d21,d25,d26",
				"F2 59 56 BA", "vmin.s16	d21,d25,d26",
				"F2 69 56 BA", "vmin.s32	d21,d25,d26",
				"F3 49 56 BA", "vmin.u8	d21,d25,d26",
				"F3 59 56 BA", "vmin.u16	d21,d25,d26",
				"F3 69 56 BA", "vmin.u32	d21,d25,d26",
				"F2 4C 66 FE", "vmin.s8	q11,q14,q15",
				"F2 5C 66 FE", "vmin.s16	q11,q14,q15",
				"F2 6C 66 FE", "vmin.s32	q11,q14,q15",
				"F3 4C 66 FE", "vmin.u8	q11,q14,q15",
				"F3 5C 66 FE", "vmin.u16	q11,q14,q15",
				"F3 6C 66 FE", "vmin.u32	q11,q14,q15",
				"F2 69 5F AA", "vmin.f32	d21,d25,d26",
				"F2 6C 6F EE", "vmin.f32	q11,q14,q15",
				"F2 D9 50 CF", "vmla.i16	d21,d25,d7[1]",
				"F2 E9 50 EF", "vmla.i32	d21,d25,d15[1]",
				"F2 E9 51 EF", "vmla.f32	d21,d25,d15[1]",
				"F2 49 59 AA", "vmla.i8	d21,d25,d26",
				"F2 59 59 AA", "vmla.i16	d21,d25,d26",
				"F2 69 59 AA", "vmla.i32	d21,d25,d26",
				"F3 DC 60 CF", "vmla.i16	q11,q14,d7[1]",
				"F3 EC 60 EF", "vmla.i32	q11,q14,d15[1]",
				"F2 4C 69 EE", "vmla.i8	q11,q14,q15",
				"F2 5C 69 EE", "vmla.i16	q11,q14,q15",
				"F2 6C 69 EE", "vmla.i32	q11,q14,q15",
				"F2 49 5D BA", "vmla.f32	d21,d25,d26",
				"F2 4C 6D FE", "vmla.f32	q11,q14,q15",
				"EE 4C AA 8D", "vmla.f32	s21,s25,s26",
				"0E 49 5B AA", "vmlaeq.f64	d21,d25,d26",
				"F2 D9 62 CF", "vmlal.s16	q11,d25,d7[1]",
				"F2 E9 62 EF", "vmlal.s32	q11,d25,d15[1]",
				"F3 D9 62 CF", "vmlal.u16	q11,d25,d7[1]",
				"F3 E9 62 EF", "vmlal.u32	q11,d25,d15[1]",
				"F2 C9 68 AA", "vmlal.s8	q11,d25,d26",
				"F2 D9 68 AA", "vmlal.s16	q11,d25,d26",
				"F2 E9 68 AA", "vmlal.s32	q11,d25,d26",
				"F3 C9 68 AA", "vmlal.u8	q11,d25,d26",
				"F3 D9 68 AA", "vmlal.u16	q11,d25,d26",
				"F3 E9 68 AA", "vmlal.u32	q11,d25,d26",
				"F2 D9 54 CF", "vmls.i16	d21,d25,d7[1]",
				"F2 E9 54 EF", "vmls.i32	d21,d25,d15[1]",
				"F2 E9 55 EF", "vmls.f32	d21,d25,d15[1]",
				"F3 49 59 AA", "vmls.i8	d21,d25,d26",
				"F3 59 59 AA", "vmls.i16	d21,d25,d26",
				"F3 69 59 AA", "vmls.i32	d21,d25,d26",
				"F3 DC 64 CF", "vmls.i16	q11,q14,d7[1]",
				"F3 EC 64 EF", "vmls.i32	q11,q14,d15[1]",
				"F3 EC 65 EF", "vmls.f32	q11,q14,d15[1]",
				"F3 4C 69 EE", "vmls.i8	q11,q14,q15",
				"F3 5C 69 EE", "vmls.i16	q11,q14,q15",
				"F3 6C 69 EE", "vmls.i32	q11,q14,q15",
				"F2 69 5D BA", "vmls.f32	d21,d25,d26",
				"F2 6C 6D FE", "vmls.f32	q11,q14,q15",
				"EE 4C AA CD", "vmls.f32	s21,s25,s26",
				"EE 49 5B EA", "vmls.f64	d21,d25,d26",
				"F2 D9 66 CF", "vmlsl.s16	q11,d25,d7[1]",
				"F2 E9 66 EF", "vmlsl.s32	q11,d25,d15[1]",
				"F3 D9 66 CF", "vmlsl.u16	q11,d25,d7[1]",
				"F3 E9 66 EF", "vmlsl.u32	q11,d25,d15[1]",
				"F2 C9 6A AA", "vmlsl.s8	q11,d25,d26",
				"F2 D9 6A AA", "vmlsl.s16	q11,d25,d26",
				"F2 E9 6A AA", "vmlsl.s32	q11,d25,d26",
				"F3 C9 6A AA", "vmlsl.u8	q11,d25,d26",
				"F3 D9 6A AA", "vmlsl.u16	q11,d25,d26",
				"F3 E9 6A AA", "vmlsl.u32	q11,d25,d26",
			    "F2 6A 51 BA", "vmov	d21,d26",
			    "F2 6E 61 FE", "vmov	q11,q15",
			    "0C 46 5B 3A", "vmoveq	d26,r5,r6",
				"EC 56 5B 3A", "vmov	r5,r6,d26",
				"0C 56 5A 1D", "vmoveq	r5,r6,s26,s27",
				"0E 1C 5A 90", "vmoveq	r5,s25",
				"EE 0C 5A 90", "vmov	s25,r5",
				"EE 0C 5A 90", "vmov	s25,r5",
				"EC 46 5A 1D", "vmov	s26,s27,r5,r6",
				"F3 C0 5E 19", "vmov.i8	d21,#0x89",
				"F3 C0 58 19", "vmov.i16	d21,#0x89",
				"F3 C0 50 19", "vmov.i32	d21,#0x89",
				"F2 C0 5E 30", "vmov.i64	d21,#0x0",
				"F3 C0 6E 59", "vmov.i8	q11,#0x89",
				"F3 C0 68 59", "vmov.i16	q11,#0x89",
				"F3 C0 60 59", "vmov.i32	q11,#0x89",
				"F2 C0 6E 70", "vmov.i64	q11,#0x0",
				"0E 5B 5B B0", "vmoveq.s8	r5,d27[1]",
				"EE 1B 5B F0", "vmov.s16	r5,d27[1]",
				"EE DB 5B B0", "vmov.u8	r5,d27[1]",
				"EE 9B 5B F0", "vmov.u16	r5,d27[1]",
				"EE 3B 5B 90", "vmov.32	r5,d27[1]",
				"0E 4B 5B B0", "vmoveq.8	d27[1],r5",
				"EE 0B 5B F0", "vmov.16	d27[1],r5",
				"EE 2B 5B 90", "vmov.32	d27[1],r5",
				"EE B7 BA 00", "vmov.f32	s22,#0x70",	// originally "vmov.f32 s22,#1.0"
				"EE F0 AA 4D", "vmov.f32	s21,s26",
				"0E F7 6B 00", "vmoveq.f64	d22,#0x70",	// originally "vmov.f64 d22,#1.0"
				"0E F0 5B 6A", "vmoveq.f64	d21,d26",
				"F2 C8 6A 3A", "vmovl.s8	q11,d26",
				"F2 D0 6A 3A", "vmovl.s16	q11,d26",
				"F2 E0 6A 3A", "vmovl.s32	q11,d26",
				"F3 C8 6A 3A", "vmovl.u8	q11,d26",
				"F3 D0 6A 3A", "vmovl.u16	q11,d26",
				"F3 E0 6A 3A", "vmovl.u32	q11,d26",
				"F3 F2 52 2E", "vmovn.i16	d21,q15",
				"F3 F6 52 2E", "vmovn.i32	d21,q15",
				"F3 FA 52 2E", "vmovn.i64	d21,q15",
				"0E F0 5A 10", "vmrseq	r5,fpsid",
				"0E F1 5A 10", "vmrseq	r5,fpscr",
				"EE F6 5A 10", "vmrs	r5,mvfr1",
				"EE F7 5A 10", "vmrs	r5,mvfr0",
				"EE F8 5A 10", "vmrs	r5,fpexc",
				"EE F9 5A 10", "vmrs	r5,fpinst",
				"EE FA 5A 10", "vmrs	r5,fpinst2",
				"0E E0 5A 10", "vmsreq	fpsid,r5",
				"0E E1 5A 10", "vmsreq	fpscr,r5",
				"EE E8 5A 10", "vmsr	fpexc,r5",
				"EE E9 5A 10", "vmsr	fpinst,r5",
				"EE EA 5A 10", "vmsr	fpinst2,r5",
				"F2 D9 58 CF", "vmul.i16	d21,d25,d7[1]",
				"F2 E9 58 EF", "vmul.i32	d21,d25,d15[1]",
				"F2 E9 59 EF", "vmul.f32	d21,d25,d15[1]",
				"F2 49 59 BA", "vmul.i8	d21,d25,d26",
				"F2 49 59 BA", "vmul.i8	d21,d25,d26",
				"F2 59 59 BA", "vmul.i16	d21,d25,d26",
				"F2 69 59 BA", "vmul.i32	d21,d25,d26",
				"F3 49 59 BA", "vmul.p8	d21,d25,d26",
				"F3 DC 68 CF", "vmul.i16	q11,q14,d7[1]",
				"F3 EC 68 EF", "vmul.i32	q11,q14,d15[1]",
				"F3 EC 69 EF", "vmul.f32	q11,q14,d15[1]",
				"F2 4C 69 FE", "vmul.i8	q11,q14,q15",
				"F2 5C 69 FE", "vmul.i16	q11,q14,q15",
				"F2 6C 69 FE", "vmul.i32	q11,q14,q15",
				"F3 4C 69 FE", "vmul.p8	q11,q14,q15",
				"F3 49 5D BA", "vmul.f32	d21,d25,d26",
				"F3 4C 6D FE", "vmul.f32	q11,q14,q15",
				"EE 6C AA 8D", "vmul.f32	s21,s25,s26",
				"0E 69 5B AA", "vmuleq.f64	d21,d25,d26",
				"F2 D9 6A CF", "vmull.s16	q11,d25,d7[1]",
				"F2 E9 6A EF", "vmull.s32	q11,d25,d15[1]",
				"F3 D9 6A CF", "vmull.u16	q11,d25,d7[1]",
				"F3 E9 6A EF", "vmull.u32	q11,d25,d15[1]",
				"F2 C9 6C AA", "vmull.s8	q11,d25,d26",
				"F2 D9 6C AA", "vmull.s16	q11,d25,d26",
				"F2 E9 6C AA", "vmull.s32	q11,d25,d26",
				"F3 C9 6C AA", "vmull.u8	q11,d25,d26",
				"F3 D9 6C AA", "vmull.u16	q11,d25,d26",
				"F3 E9 6C AA", "vmull.u32	q11,d25,d26",
				"F2 C9 6E AA", "vmull.p8	q11,d25,d26",
				"F3 F0 55 AA", "vmvn	d21,d26",
				"F3 F0 65 EE", "vmvn	q11,q15",
				"F3 C0 58 37", "vmvn.i16	d21,#0x87",
				"F3 C0 50 37", "vmvn.i32	d21,#0x87",
				"F3 C0 68 77", "vmvn.i16	q11,#0x87",
				"F3 C0 60 77", "vmvn.i32	q11,#0x87",
				"F3 F1 53 AA", "vneg.s8	d21,d26",
				"F3 F5 53 AA", "vneg.s16	d21,d26",
				"F3 F9 53 AA", "vneg.s32	d21,d26",
				"F3 F9 57 AA", "vneg.f32	d21,d26",
				"EE F1 5B 6A", "vneg.f64	d21,d26",
				"F3 F1 63 EE", "vneg.s8	q11,q15",
				"F3 F5 63 EE", "vneg.s16	q11,q15",
				"F3 F9 63 EE", "vneg.s32	q11,q15",
				"F3 F9 67 EE", "vneg.f32	q11,q15",
				"0E F1 AA 4D", "vnegeq.f32	s21,s26",
				"0E 5C AA CD", "vnmlaeq.f32	s21,s25,s26",
				"EE 59 5B EA", "vnmla.f64	d21,d25,d26",
				"EE 5C AA 8D", "vnmls.f32	s21,s25,s26",
				"EE 59 5B AA", "vnmls.f64	d21,d25,d26",
				"0E 6C AA CD", "vnmuleq.f32	s21,s25,s26",
				"EE 69 5B EA", "vnmul.f64	d21,d25,d26",
				"F2 79 51 BA", "vorn	d21,d25,d26",
				"F2 7C 61 FE", "vorn	q11,q14,q15",
				"F2 69 51 BA", "vorr	d21,d25,d26",
				"F2 6C 61 FE", "vorr	q11,q14,q15",
				"F3 C0 59 17", "vorr.i16	d21,#0x87",
				"F3 C0 51 17", "vorr.i32	d21,#0x87",
				"F3 C0 69 57", "vorr.i16	q11,#0x87",
				"F3 C0 61 57", "vorr.i32	q11,#0x87",
				"F3 F0 56 2A", "vpadal.s8	d21,d26",
				"F3 F4 56 2A", "vpadal.s16	d21,d26",
				"F3 F8 56 2A", "vpadal.s32	d21,d26",
				"F3 F0 56 AA", "vpadal.u8	d21,d26",
				"F3 F4 56 AA", "vpadal.u16	d21,d26",
				"F3 F8 56 AA", "vpadal.u32	d21,d26",
				"F3 F0 66 6E", "vpadal.s8	q11,q15",
				"F3 F4 66 6E", "vpadal.s16	q11,q15",
				"F3 F8 66 6E", "vpadal.s32	q11,q15",
				"F3 F0 66 EE", "vpadal.u8	q11,q15",
				"F3 F4 66 EE", "vpadal.u16	q11,q15",
				"F3 F8 66 EE", "vpadal.u32	q11,q15",
				"F2 49 5B BA", "vpadd.i8	d21,d25,d26",
				"F2 59 5B BA", "vpadd.i16	d21,d25,d26",
				"F2 69 5B BA", "vpadd.i32	d21,d25,d26",
				"F3 49 5D AA", "vpadd.f32	d21,d25,d26",
				"F3 F0 52 2A", "vpaddl.s8	d21,d26",
				"F3 F4 52 2A", "vpaddl.s16	d21,d26",
				"F3 F8 52 2A", "vpaddl.s32	d21,d26",
				"F3 F0 52 AA", "vpaddl.u8	d21,d26",
				"F3 F4 52 AA", "vpaddl.u16	d21,d26",
				"F3 F8 52 AA", "vpaddl.u32	d21,d26",
				"F3 F0 62 6E", "vpaddl.s8	q11,q15",
				"F3 F4 62 6E", "vpaddl.s16	q11,q15",
				"F3 F8 62 6E", "vpaddl.s32	q11,q15",
				"F3 F0 62 EE", "vpaddl.u8	q11,q15",
				"F3 F4 62 EE", "vpaddl.u16	q11,q15",
				"F3 F8 62 EE", "vpaddl.u32	q11,q15",
				"F2 49 5A AA", "vpmax.s8	d21,d25,d26",
				"F2 59 5A AA", "vpmax.s16	d21,d25,d26",
				"F2 69 5A AA", "vpmax.s32	d21,d25,d26",
				"F3 49 5A AA", "vpmax.u8	d21,d25,d26",
				"F3 59 5A AA", "vpmax.u16	d21,d25,d26",
				"F3 69 5A AA", "vpmax.u32	d21,d25,d26",
				"F3 49 5F AA", "vpmax.f32	d21,d25,d26",
				"F2 49 5A BA", "vpmin.s8	d21,d25,d26",
				"F2 59 5A BA", "vpmin.s16	d21,d25,d26",
				"F2 69 5A BA", "vpmin.s32	d21,d25,d26",
				"F3 49 5A BA", "vpmin.u8	d21,d25,d26",
				"F3 59 5A BA", "vpmin.u16	d21,d25,d26",
				"F3 69 5A BA", "vpmin.u32	d21,d25,d26",
				"F3 69 5F AA", "vpmin.f32	d21,d25,d26",
				"0C FD DA 02", "vpopeq	{s27-s28}",
				"0C FD BB 04", "vpopeq	{d27-d28}",
				"0D 6D DA 02", "vpusheq	{s27-s28}",
				"0D 6D BB 04", "vpusheq	{d27-d28}",
				"F3 F0 57 2A", "vqabs.s8	d21,d26",
				"F3 F4 57 2A", "vqabs.s16	d21,d26",
				"F3 F8 57 2A", "vqabs.s32	d21,d26",
				"F3 F0 67 6E", "vqabs.s8	q11,q15",
				"F3 F4 67 6E", "vqabs.s16	q11,q15",
				"F3 F8 67 6E", "vqabs.s32	q11,q15",
				"F2 49 50 BA", "vqadd.s8	d21,d25,d26",
				"F2 59 50 BA", "vqadd.s16	d21,d25,d26",
				"F2 69 50 BA", "vqadd.s32	d21,d25,d26",
				"F2 79 50 BA", "vqadd.s64	d21,d25,d26",
				"F3 49 50 BA", "vqadd.u8	d21,d25,d26",
				"F3 59 50 BA", "vqadd.u16	d21,d25,d26",
				"F3 69 50 BA", "vqadd.u32	d21,d25,d26",
				"F3 79 50 BA", "vqadd.u64	d21,d25,d26",
				"F2 4C 60 FE", "vqadd.s8	q11,q14,q15",
				"F2 5C 60 FE", "vqadd.s16	q11,q14,q15",
				"F2 6C 60 FE", "vqadd.s32	q11,q14,q15",
				"F2 7C 60 FE", "vqadd.s64	q11,q14,q15",
				"F3 4C 60 FE", "vqadd.u8	q11,q14,q15",
				"F3 5C 60 FE", "vqadd.u16	q11,q14,q15",
				"F3 6C 60 FE", "vqadd.u32	q11,q14,q15",
				"F3 7C 60 FE", "vqadd.u64	q11,q14,q15",
				"F2 D9 63 CF", "vqdmlal.s16	q11,d25,d7[1]",
				"F2 E9 63 EF", "vqdmlal.s32	q11,d25,d15[1]",
				"F2 D9 69 AA", "vqdmlal.s16	q11,d25,d26",
				"F2 E9 69 AA", "vqdmlal.s32	q11,d25,d26",
				"F2 D9 67 CF", "vqdmlsl.s16	q11,d25,d7[1]",
				"F2 E9 67 EF", "vqdmlsl.s32	q11,d25,d15[1]",
				"F2 D9 6B AA", "vqdmlsl.s16	q11,d25,d26",
				"F2 E9 6B AA", "vqdmlsl.s32	q11,d25,d26",
				"F2 D9 5C CA", "vqdmulh.s16	d21,d25,d2[1]",
				"F2 E9 5C EF", "vqdmulh.s32	d21,d25,d15[1]",
				"F2 59 5B AA", "vqdmulh.s16	d21,d25,d26",
				"F2 69 5B AA", "vqdmulh.s32	d21,d25,d26",
				"F3 DC 6C CA", "vqdmulh.s16	q11,q14,d2[1]",
				"F3 EC 6C EF", "vqdmulh.s32	q11,q14,d15[1]",
				"F2 5C 6B EE", "vqdmulh.s16	q11,q14,q15",
				"F2 6C 6B EE", "vqdmulh.s32	q11,q14,q15",
				"F2 D9 6B CA", "vqdmull.s16	q11,d25,d2[1]",
				"F2 E9 6B EF", "vqdmull.s32	q11,d25,d15[1]",
				"F2 D9 6D AA", "vqdmull.s16	q11,d25,d26",
				"F2 E9 6D AA", "vqdmull.s32	q11,d25,d26",
				"F2 D9 6D AA", "vqdmull.s16	q11,d25,d26",
				"F2 E9 6D AA", "vqdmull.s32	q11,d25,d26",
				"F3 F2 52 AE", "vqmovn.s16	d21,q15",
				"F3 F6 52 AE", "vqmovn.s32	d21,q15",
				"F3 FA 52 AE", "vqmovn.s64	d21,q15",
				"F3 F2 52 EE", "vqmovn.u16	d21,q15",
				"F3 F6 52 EE", "vqmovn.u32	d21,q15",
				"F3 FA 52 EE", "vqmovn.u64	d21,q15",
				"F3 F2 52 6E", "vqmovun.s16	d21,q15",
				"F3 F6 52 6E", "vqmovun.s32	d21,q15",
				"F3 FA 52 6E", "vqmovun.s64	d21,q15",
				"F3 F0 57 AA", "vqneg.s8	d21,d26",
				"F3 F4 57 AA", "vqneg.s16	d21,d26",
				"F3 F8 57 AA", "vqneg.s32	d21,d26",
				"F3 F0 67 EE", "vqneg.s8	q11,q15",
				"F3 F4 67 EE", "vqneg.s16	q11,q15",
				"F3 F8 67 EE", "vqneg.s32	q11,q15",
				"F2 D9 5D CF", "vqrdmulh.s16	d21,d25,d7[1]",
				"F2 E9 5D EF", "vqrdmulh.s32	d21,d25,d15[1]",
				"F3 59 5B AA", "vqrdmulh.s16	d21,d25,d26",
				"F3 69 5B AA", "vqrdmulh.s32	d21,d25,d26",
				"F3 DC 6D CF", "vqrdmulh.s16	q11,q14,d7[1]",
				"F3 EC 6D EF", "vqrdmulh.s32	q11,q14,d15[1]",
				"F3 5C 6B EE", "vqrdmulh.s16	q11,q14,q15",
				"F3 6C 6B EE", "vqrdmulh.s32	q11,q14,q15",
				"F2 49 55 BA", "vqrshl.s8	d21,d26,d25",
				"F2 59 55 BA", "vqrshl.s16	d21,d26,d25",
				"F2 69 55 BA", "vqrshl.s32	d21,d26,d25",
				"F2 79 55 BA", "vqrshl.s64	d21,d26,d25",
				"F3 49 55 BA", "vqrshl.u8	d21,d26,d25",
				"F3 59 55 BA", "vqrshl.u16	d21,d26,d25",
				"F3 69 55 BA", "vqrshl.u32	d21,d26,d25",
				"F3 79 55 BA", "vqrshl.u64	d21,d26,d25",
				"F2 4C 65 FE", "vqrshl.s8	q11,q15,q14",
				"F2 5C 65 FE", "vqrshl.s16	q11,q15,q14",
				"F2 6C 65 FE", "vqrshl.s32	q11,q15,q14",
				"F2 7C 65 FE", "vqrshl.s64	q11,q15,q14",
				"F3 4C 65 FE", "vqrshl.u8	q11,q15,q14",
				"F3 5C 65 FE", "vqrshl.u16	q11,q15,q14",
				"F3 6C 65 FE", "vqrshl.u32	q11,q15,q14",
				"F3 7C 65 FE", "vqrshl.u64	q11,q15,q14",
				"F2 CF 59 7E", "vqrshrn.s16	d21,q15,#1",
				"F3 CF 59 7E", "vqrshrn.u16	d21,q15,#1",
				"F2 CF 59 7E", "vqrshrn.s16	d21,q15,#1",
				"F2 C8 59 7E", "vqrshrn.s16	d21,q15,#8",
				"F3 CF 59 7E", "vqrshrn.u16	d21,q15,#1",
				"F3 C8 59 7E", "vqrshrn.u16	d21,q15,#8",
				"F2 DF 59 7E", "vqrshrn.s32	d21,q15,#1",
				"F2 D0 59 7E", "vqrshrn.s32	d21,q15,#16",
				"F3 DF 59 7E", "vqrshrn.u32	d21,q15,#1",
				"F3 D0 59 7E", "vqrshrn.u32	d21,q15,#16",
				"F2 FF 59 7E", "vqrshrn.s64	d21,q15,#1",
				"F2 E0 59 7E", "vqrshrn.s64	d21,q15,#32",
				"F3 FF 59 7E", "vqrshrn.u64	d21,q15,#1",
				"F3 E0 59 7E", "vqrshrn.u64	d21,q15,#32",
				"F3 CF 58 7E", "vqrshrun.s16	d21,q15,#1",
				"F3 C8 58 7E", "vqrshrun.s16	d21,q15,#8",
				"F3 DF 58 7E", "vqrshrun.s32	d21,q15,#1",
				"F3 D0 58 7E", "vqrshrun.s32	d21,q15,#16",
				"F3 FF 58 7E", "vqrshrun.s64	d21,q15,#1",
				"F3 E0 58 7E", "vqrshrun.s64	d21,q15,#32",
				"F2 C8 57 3A", "vqshl.s8	d21,d26,#0",
				"F2 CF 57 3A", "vqshl.s8	d21,d26,#7",
				"F3 C8 57 3A", "vqshl.u8	d21,d26,#0",
				"F3 CF 57 3A", "vqshl.u8	d21,d26,#7",
				"F2 D0 57 3A", "vqshl.s16	d21,d26,#0",
				"F2 DF 57 3A", "vqshl.s16	d21,d26,#15",
				"F3 D0 57 3A", "vqshl.u16	d21,d26,#0",
				"F3 DF 57 3A", "vqshl.u16	d21,d26,#15",
				"F2 E0 57 3A", "vqshl.s32	d21,d26,#0",
				"F2 FF 57 3A", "vqshl.s32	d21,d26,#31",
				"F3 E0 57 3A", "vqshl.u32	d21,d26,#0",
				"F3 FF 57 3A", "vqshl.u32	d21,d26,#31",
				"F2 C0 57 BA", "vqshl.s64	d21,d26,#0",
				"F2 FF 57 BA", "vqshl.s64	d21,d26,#63",
				"F3 C0 57 BA", "vqshl.u64	d21,d26,#0",
				"F3 FF 57 BA", "vqshl.u64	d21,d26,#63",
				"F2 49 54 BA", "vqshl.s8	d21,d26,d25",
				"F2 59 54 BA", "vqshl.s16	d21,d26,d25",
				"F2 69 54 BA", "vqshl.s32	d21,d26,d25",
				"F2 79 54 BA", "vqshl.s64	d21,d26,d25",
				"F3 49 54 BA", "vqshl.u8	d21,d26,d25",
				"F3 59 54 BA", "vqshl.u16	d21,d26,d25",
				"F3 69 54 BA", "vqshl.u32	d21,d26,d25",
				"F3 79 54 BA", "vqshl.u64	d21,d26,d25",
				"F2 C8 67 7E", "vqshl.s8	q11,q15,#0",
				"F2 CF 67 7E", "vqshl.s8	q11,q15,#7",
				"F3 C8 67 7E", "vqshl.u8	q11,q15,#0",
				"F3 CF 67 7E", "vqshl.u8	q11,q15,#7",
				"F2 D0 67 7E", "vqshl.s16	q11,q15,#0",
				"F2 DF 67 7E", "vqshl.s16	q11,q15,#15",
				"F3 D0 67 7E", "vqshl.u16	q11,q15,#0",
				"F3 DF 67 7E", "vqshl.u16	q11,q15,#15",
				"F2 E0 67 7E", "vqshl.s32	q11,q15,#0",
				"F2 FF 67 7E", "vqshl.s32	q11,q15,#31",
				"F3 E0 67 7E", "vqshl.u32	q11,q15,#0",
				"F3 FF 67 7E", "vqshl.u32	q11,q15,#31",
				"F2 C0 67 FE", "vqshl.s64	q11,q15,#0",
				"F2 FF 67 FE", "vqshl.s64	q11,q15,#63",
				"F3 C0 67 FE", "vqshl.u64	q11,q15,#0",
				"F3 FF 67 FE", "vqshl.u64	q11,q15,#63",
				"F2 4C 64 FE", "vqshl.s8	q11,q15,q14",
				"F2 5C 64 FE", "vqshl.s16	q11,q15,q14",
				"F2 6C 64 FE", "vqshl.s32	q11,q15,q14",
				"F2 7C 64 FE", "vqshl.s64	q11,q15,q14",
				"F3 4C 64 FE", "vqshl.u8	q11,q15,q14",
				"F3 5C 64 FE", "vqshl.u16	q11,q15,q14",
				"F3 6C 64 FE", "vqshl.u32	q11,q15,q14",
				"F3 7C 64 FE", "vqshl.u64	q11,q15,q14",
				"F3 C8 56 3A", "vqshlu.s8	d21,d26,#0",
				"F3 CF 56 3A", "vqshlu.s8	d21,d26,#7",
				"F3 D0 56 3A", "vqshlu.s16	d21,d26,#0",
				"F3 DF 56 3A", "vqshlu.s16	d21,d26,#15",
				"F3 E0 56 3A", "vqshlu.s32	d21,d26,#0",
				"F3 FF 56 3A", "vqshlu.s32	d21,d26,#31",
				"F3 C0 56 BA", "vqshlu.s64	d21,d26,#0",
				"F3 FF 56 BA", "vqshlu.s64	d21,d26,#63",
				"F3 C8 66 7E", "vqshlu.s8	q11,q15,#0",
				"F3 CF 66 7E", "vqshlu.s8	q11,q15,#7",
				"F3 D0 66 7E", "vqshlu.s16	q11,q15,#0",
				"F3 DF 66 7E", "vqshlu.s16	q11,q15,#15",
				"F3 E0 66 7E", "vqshlu.s32	q11,q15,#0",
				"F3 FF 66 7E", "vqshlu.s32	q11,q15,#31",
				"F3 C0 66 FE", "vqshlu.s64	q11,q15,#0",
				"F3 FF 66 FE", "vqshlu.s64	q11,q15,#63",
				"F2 CF 59 3E", "vqshrn.s16	d21,q15,#1",
				"F2 C8 59 3E", "vqshrn.s16	d21,q15,#8",
				"F3 CF 59 3E", "vqshrn.u16	d21,q15,#1",
				"F3 C8 59 3E", "vqshrn.u16	d21,q15,#8",
				"F2 DF 59 3E", "vqshrn.s32	d21,q15,#1",
				"F2 D0 59 3E", "vqshrn.s32	d21,q15,#16",
				"F3 DF 59 3E", "vqshrn.u32	d21,q15,#1",
				"F3 D0 59 3E", "vqshrn.u32	d21,q15,#16",
				"F2 FF 59 3E", "vqshrn.s64	d21,q15,#1",
				"F2 E0 59 3E", "vqshrn.s64	d21,q15,#32",
				"F3 FF 59 3E", "vqshrn.u64	d21,q15,#1",
				"F3 E0 59 3E", "vqshrn.u64	d21,q15,#32",
				"F3 CF 58 3E", "vqshrun.s16	d21,q15,#1",
				"F3 C8 58 3E", "vqshrun.s16	d21,q15,#8",
				"F3 DF 58 3E", "vqshrun.s32	d21,q15,#1",
				"F3 D0 58 3E", "vqshrun.s32	d21,q15,#16",
				"F3 FF 58 3E", "vqshrun.s64	d21,q15,#1",
				"F3 E0 58 3E", "vqshrun.s64	d21,q15,#32",
				"F2 49 52 BA", "vqsub.s8	d21,d25,d26",
				"F2 59 52 BA", "vqsub.s16	d21,d25,d26",
				"F2 69 52 BA", "vqsub.s32	d21,d25,d26",
				"F2 79 52 BA", "vqsub.s64	d21,d25,d26",
				"F3 49 52 BA", "vqsub.u8	d21,d25,d26",
				"F3 59 52 BA", "vqsub.u16	d21,d25,d26",
				"F3 69 52 BA", "vqsub.u32	d21,d25,d26",
				"F3 79 52 BA", "vqsub.u64	d21,d25,d26",
				"F2 4C 62 FE", "vqsub.s8	q11,q14,q15",
				"F2 5C 62 FE", "vqsub.s16	q11,q14,q15",
				"F2 6C 62 FE", "vqsub.s32	q11,q14,q15",
				"F2 7C 62 FE", "vqsub.s64	q11,q14,q15",
				"F3 4C 62 FE", "vqsub.u8	q11,q14,q15",
				"F3 5C 62 FE", "vqsub.u16	q11,q14,q15",
				"F3 6C 62 FE", "vqsub.u32	q11,q14,q15",
				"F3 7C 62 FE", "vqsub.u64	q11,q14,q15",
				"F3 CC 54 AE", "vraddhn.i16	d21,q14,q15",
				"F3 DC 54 AE", "vraddhn.i32	d21,q14,q15",
				"F3 EC 54 AE", "vraddhn.i64	d21,q14,q15",
				"F3 FB 54 2A", "vrecpe.u32	d21,d26",
				"F3 FB 55 2A", "vrecpe.f32	d21,d26",
				"F3 FB 64 6E", "vrecpe.u32	q11,q15",
				"F3 FB 65 6E", "vrecpe.f32	q11,q15",
				"F2 49 5F BA", "vrecps.f32	d21,d25,d26",
				"F2 4C 6F FE", "vrecps.f32	q11,q14,q15",
				"F3 F0 51 2A", "vrev16.8	d21,d26",
				"F3 F0 61 6E", "vrev16.8	q11,q15",
				"F3 F0 50 AA", "vrev32.8	d21,d26",
				"F3 F4 50 AA", "vrev32.16	d21,d26",
				"F3 F0 60 EE", "vrev32.8	q11,q15",
				"F3 F4 60 EE", "vrev32.16	q11,q15",
				"F3 F0 50 2A", "vrev64.8	d21,d26",
				"F3 F4 50 2A", "vrev64.16	d21,d26",
				"F3 F8 50 2A", "vrev64.32	d21,d26",
				"F3 F0 60 6E", "vrev64.8	q11,q15",
				"F3 F4 60 6E", "vrev64.16	q11,q15",
				"F3 F8 60 6E", "vrev64.32	q11,q15",
				"F2 49 51 AA", "vrhadd.s8	d21,d25,d26",
				"F2 59 51 AA", "vrhadd.s16	d21,d25,d26",
				"F2 69 51 AA", "vrhadd.s32	d21,d25,d26",
				"F3 49 51 AA", "vrhadd.u8	d21,d25,d26",
				"F3 59 51 AA", "vrhadd.u16	d21,d25,d26",
				"F3 69 51 AA", "vrhadd.u32	d21,d25,d26",
				"F2 4C 61 EE", "vrhadd.s8	q11,q14,q15",
				"F2 5C 61 EE", "vrhadd.s16	q11,q14,q15",
				"F2 6C 61 EE", "vrhadd.s32	q11,q14,q15",
				"F3 4C 61 EE", "vrhadd.u8	q11,q14,q15",
				"F3 5C 61 EE", "vrhadd.u16	q11,q14,q15",
				"F3 6C 61 EE", "vrhadd.u32	q11,q14,q15",
				"F2 49 55 AA", "vrshl.s8	d21,d26,d25",
				"F2 59 55 AA", "vrshl.s16	d21,d26,d25",
				"F2 69 55 AA", "vrshl.s32	d21,d26,d25",
				"F2 79 55 AA", "vrshl.s64	d21,d26,d25",
				"F3 49 55 AA", "vrshl.u8	d21,d26,d25",
				"F3 59 55 AA", "vrshl.u16	d21,d26,d25",
				"F3 69 55 AA", "vrshl.u32	d21,d26,d25",
				"F3 79 55 AA", "vrshl.u64	d21,d26,d25",
				"F2 4C 65 EE", "vrshl.s8	q11,q15,q14",
				"F2 5C 65 EE", "vrshl.s16	q11,q15,q14",
				"F2 6C 65 EE", "vrshl.s32	q11,q15,q14",
				"F2 7C 65 EE", "vrshl.s64	q11,q15,q14",
				"F3 4C 65 EE", "vrshl.u8	q11,q15,q14",
				"F3 5C 65 EE", "vrshl.u16	q11,q15,q14",
				"F3 6C 65 EE", "vrshl.u32	q11,q15,q14",
				"F3 7C 65 EE", "vrshl.u64	q11,q15,q14",
				"F2 CF 52 3A", "vrshr.s8	d21,d26,#1",
				"F2 C8 52 3A", "vrshr.s8	d21,d26,#8",
				"F3 CF 52 3A", "vrshr.u8	d21,d26,#1",
				"F3 C8 52 3A", "vrshr.u8	d21,d26,#8",
				"F2 DF 52 3A", "vrshr.s16	d21,d26,#1",
				"F2 D0 52 3A", "vrshr.s16	d21,d26,#16",
				"F3 DF 52 3A", "vrshr.u16	d21,d26,#1",
				"F3 D0 52 3A", "vrshr.u16	d21,d26,#16",
				"F2 FF 52 3A", "vrshr.s32	d21,d26,#1",
				"F2 E0 52 3A", "vrshr.s32	d21,d26,#32",
				"F3 FF 52 3A", "vrshr.u32	d21,d26,#1",
				"F3 E0 52 3A", "vrshr.u32	d21,d26,#32",
				"F2 FF 52 BA", "vrshr.s64	d21,d26,#1",
				"F2 C0 52 BA", "vrshr.s64	d21,d26,#64",
				"F3 FF 52 BA", "vrshr.u64	d21,d26,#1",
				"F3 C0 52 BA", "vrshr.u64	d21,d26,#64",
				"F2 CF 62 7E", "vrshr.s8	q11,q15,#1",
				"F2 C8 62 7E", "vrshr.s8	q11,q15,#8",
				"F3 CF 62 7E", "vrshr.u8	q11,q15,#1",
				"F3 C8 62 7E", "vrshr.u8	q11,q15,#8",
				"F2 DF 62 7E", "vrshr.s16	q11,q15,#1",
				"F2 D0 62 7E", "vrshr.s16	q11,q15,#16",
				"F3 DF 62 7E", "vrshr.u16	q11,q15,#1",
				"F3 D0 62 7E", "vrshr.u16	q11,q15,#16",
				"F2 FF 62 7E", "vrshr.s32	q11,q15,#1",
				"F2 E0 62 7E", "vrshr.s32	q11,q15,#32",
				"F3 FF 62 7E", "vrshr.u32	q11,q15,#1",
				"F3 E0 62 7E", "vrshr.u32	q11,q15,#32",
				"F2 FF 62 FE", "vrshr.s64	q11,q15,#1",
				"F2 C0 62 FE", "vrshr.s64	q11,q15,#64",
				"F3 FF 62 FE", "vrshr.u64	q11,q15,#1",
				"F3 C0 62 FE", "vrshr.u64	q11,q15,#64",
				"F2 CF 58 7E", "vrshrn.i16	d21,q15,#1",
				"F2 C8 58 7E", "vrshrn.i16	d21,q15,#8",
				"F2 DF 58 7E", "vrshrn.i32	d21,q15,#1",
				"F2 D0 58 7E", "vrshrn.i32	d21,q15,#16",
				"F2 FF 58 7E", "vrshrn.i64	d21,q15,#1",
				"F2 E0 58 7E", "vrshrn.i64	d21,q15,#32",
				"F3 FB 54 AA", "vrsqrte.u32	d21,d26",
				"F3 FB 55 AA", "vrsqrte.f32	d21,d26",
				"F3 FB 64 EE", "vrsqrte.u32	q11,q15",
				"F3 FB 65 EE", "vrsqrte.f32	q11,q15",
				"F2 69 5F BA", "vrsqrts.f32	d21,d25,d26",
				"F2 6C 6F FE", "vrsqrts.f32	q11,q14,q15",
				"F2 CF 53 3A", "vrsra.s8	d21,d26,#1",
				"F2 C8 53 3A", "vrsra.s8	d21,d26,#8",
				"F3 CF 53 3A", "vrsra.u8	d21,d26,#1",
				"F3 C8 53 3A", "vrsra.u8	d21,d26,#8",
				"F2 DF 53 3A", "vrsra.s16	d21,d26,#1",
				"F2 D0 53 3A", "vrsra.s16	d21,d26,#16",
				"F3 DF 53 3A", "vrsra.u16	d21,d26,#1",
				"F3 D0 53 3A", "vrsra.u16	d21,d26,#16",
				"F2 FF 53 3A", "vrsra.s32	d21,d26,#1",
				"F2 E0 53 3A", "vrsra.s32	d21,d26,#32",
				"F3 FF 53 3A", "vrsra.u32	d21,d26,#1",
				"F3 E0 53 3A", "vrsra.u32	d21,d26,#32",
				"F2 FF 53 BA", "vrsra.s64	d21,d26,#1",
				"F2 C0 53 BA", "vrsra.s64	d21,d26,#64",
				"F3 FF 53 BA", "vrsra.u64	d21,d26,#1",
				"F3 C0 53 BA", "vrsra.u64	d21,d26,#64",
				"F2 CF 63 7E", "vrsra.s8	q11,q15,#1",
				"F2 C8 63 7E", "vrsra.s8	q11,q15,#8",
				"F3 CF 63 7E", "vrsra.u8	q11,q15,#1",
				"F3 C8 63 7E", "vrsra.u8	q11,q15,#8",
				"F2 DF 63 7E", "vrsra.s16	q11,q15,#1",
				"F2 D0 63 7E", "vrsra.s16	q11,q15,#16",
				"F3 DF 63 7E", "vrsra.u16	q11,q15,#1",
				"F3 D0 63 7E", "vrsra.u16	q11,q15,#16",
				"F2 FF 63 7E", "vrsra.s32	q11,q15,#1",
				"F2 E0 63 7E", "vrsra.s32	q11,q15,#32",
				"F3 FF 63 7E", "vrsra.u32	q11,q15,#1",
				"F3 E0 63 7E", "vrsra.u32	q11,q15,#32",
				"F2 FF 63 FE", "vrsra.s64	q11,q15,#1",
				"F2 C0 63 FE", "vrsra.s64	q11,q15,#64",
				"F3 FF 63 FE", "vrsra.u64	q11,q15,#1",
				"F3 C0 63 FE", "vrsra.u64	q11,q15,#64",
				"F3 CC 56 AE", "vrsubhn.i16	d21,q14,q15",
				"F3 DC 56 AE", "vrsubhn.i32	d21,q14,q15",
				"F3 EC 56 AE", "vrsubhn.i64	d21,q14,q15",
				"F2 C8 55 3A", "vshl.i8	d21,d26,#0",
				"F2 CF 55 3A", "vshl.i8	d21,d26,#7",
				"F2 D0 55 3A", "vshl.i16	d21,d26,#0",
				"F2 DF 55 3A", "vshl.i16	d21,d26,#15",
				"F2 E0 55 3A", "vshl.i32	d21,d26,#0",
				"F2 FF 55 3A", "vshl.i32	d21,d26,#31",
				"F2 C0 55 BA", "vshl.i64	d21,d26,#0",
				"F2 FF 55 BA", "vshl.i64	d21,d26,#63",
				"F2 C8 65 7E", "vshl.i8	q11,q15,#0",
				"F2 CF 65 7E", "vshl.i8	q11,q15,#7",
				"F2 D0 65 7E", "vshl.i16	q11,q15,#0",
				"F2 DF 65 7E", "vshl.i16	q11,q15,#15",
				"F2 E0 65 7E", "vshl.i32	q11,q15,#0",
				"F2 FF 65 7E", "vshl.i32	q11,q15,#31",
				"F2 C0 65 FE", "vshl.i64	q11,q15,#0",
				"F2 FF 65 FE", "vshl.i64	q11,q15,#63",
				"F2 49 54 AA", "vshl.s8	d21,d26,d25",
				"F2 59 54 AA", "vshl.s16	d21,d26,d25",
				"F2 69 54 AA", "vshl.s32	d21,d26,d25",
				"F2 79 54 AA", "vshl.s64	d21,d26,d25",
				"F3 49 54 AA", "vshl.u8	d21,d26,d25",
				"F3 59 54 AA", "vshl.u16	d21,d26,d25",
				"F3 69 54 AA", "vshl.u32	d21,d26,d25",
				"F3 79 54 AA", "vshl.u64	d21,d26,d25",
				"F2 4C 64 EE", "vshl.s8	q11,q15,q14",
				"F2 5C 64 EE", "vshl.s16	q11,q15,q14",
				"F2 6C 64 EE", "vshl.s32	q11,q15,q14",
				"F2 7C 64 EE", "vshl.s64	q11,q15,q14",
				"F3 4C 64 EE", "vshl.u8	q11,q15,q14",
				"F3 5C 64 EE", "vshl.u16	q11,q15,q14",
				"F3 6C 64 EE", "vshl.u32	q11,q15,q14",
				"F3 7C 64 EE", "vshl.u64	q11,q15,q14",
				"F2 C9 6A 3A", "vshll.s8	q11,d26,#1",
				"F2 CF 6A 3A", "vshll.s8	q11,d26,#7",
				"F3 C9 6A 3A", "vshll.u8	q11,d26,#1",
				"F3 CF 6A 3A", "vshll.u8	q11,d26,#7",
				"F2 D1 6A 3A", "vshll.s16	q11,d26,#1",
				"F2 DF 6A 3A", "vshll.s16	q11,d26,#15",
				"F3 D1 6A 3A", "vshll.u16	q11,d26,#1",
				"F3 DF 6A 3A", "vshll.u16	q11,d26,#15",
				"F2 E1 6A 3A", "vshll.s32	q11,d26,#1",
				"F2 FF 6A 3A", "vshll.s32	q11,d26,#31",
				"F3 E1 6A 3A", "vshll.u32	q11,d26,#1",
				"F3 FF 6A 3A", "vshll.u32	q11,d26,#31",
				"F3 F2 63 2A", "vshll.i8	q11,d26,#8",
				"F3 F6 63 2A", "vshll.i16	q11,d26,#16",
				"F3 FA 63 2A", "vshll.i32	q11,d26,#32",
				"F2 CF 50 3A", "vshr.s8	d21,d26,#1",
				"F2 C8 50 3A", "vshr.s8	d21,d26,#8",
				"F3 CF 50 3A", "vshr.u8	d21,d26,#1",
				"F3 C8 50 3A", "vshr.u8	d21,d26,#8",
				"F2 DF 50 3A", "vshr.s16	d21,d26,#1",
				"F2 D0 50 3A", "vshr.s16	d21,d26,#16",
				"F3 DF 50 3A", "vshr.u16	d21,d26,#1",
				"F3 D0 50 3A", "vshr.u16	d21,d26,#16",
				"F2 FF 50 3A", "vshr.s32	d21,d26,#1",
				"F2 E0 50 3A", "vshr.s32	d21,d26,#32",
				"F3 FF 50 3A", "vshr.u32	d21,d26,#1",
				"F3 E0 50 3A", "vshr.u32	d21,d26,#32",
				"F2 FF 50 BA", "vshr.s64	d21,d26,#1",
				"F2 C0 50 BA", "vshr.s64	d21,d26,#64",
				"F3 FF 50 BA", "vshr.u64	d21,d26,#1",
				"F3 C0 50 BA", "vshr.u64	d21,d26,#64",
				"F2 CF 60 7E", "vshr.s8	q11,q15,#1",
				"F2 C8 60 7E", "vshr.s8	q11,q15,#8",
				"F3 CF 60 7E", "vshr.u8	q11,q15,#1",
				"F3 C8 60 7E", "vshr.u8	q11,q15,#8",
				"F2 DF 60 7E", "vshr.s16	q11,q15,#1",
				"F2 D0 60 7E", "vshr.s16	q11,q15,#16",
				"F3 DF 60 7E", "vshr.u16	q11,q15,#1",
				"F3 D0 60 7E", "vshr.u16	q11,q15,#16",
				"F2 FF 60 7E", "vshr.s32	q11,q15,#1",
				"F2 E0 60 7E", "vshr.s32	q11,q15,#32",
				"F3 FF 60 7E", "vshr.u32	q11,q15,#1",
				"F3 E0 60 7E", "vshr.u32	q11,q15,#32",
				"F2 FF 60 FE", "vshr.s64	q11,q15,#1",
				"F2 C0 60 FE", "vshr.s64	q11,q15,#64",
				"F3 FF 60 FE", "vshr.u64	q11,q15,#1",
				"F3 C0 60 FE", "vshr.u64	q11,q15,#64",
				"F2 CF 58 3E", "vshrn.i16	d21,q15,#1",
				"F2 C8 58 3E", "vshrn.i16	d21,q15,#8",
				"F2 DF 58 3E", "vshrn.i32	d21,q15,#1",
				"F2 D0 58 3E", "vshrn.i32	d21,q15,#16",
				"F2 FF 58 3E", "vshrn.i64	d21,q15,#1",
				"F2 E0 58 3E", "vshrn.i64	d21,q15,#32",
				"F3 C8 55 3A", "vsli.8	d21,d26,#0",
				"F3 CF 55 3A", "vsli.8	d21,d26,#7",
				"F3 D0 55 3A", "vsli.16	d21,d26,#0",
				"F3 DF 55 3A", "vsli.16	d21,d26,#15",
				"F3 E0 55 3A", "vsli.32	d21,d26,#0",
				"F3 FF 55 3A", "vsli.32	d21,d26,#31",
				"F3 C0 55 BA", "vsli.64	d21,d26,#0",
				"F3 FF 55 BA", "vsli.64	d21,d26,#63",
				"F3 C8 65 7E", "vsli.8	q11,q15,#0",
				"F3 CF 65 7E", "vsli.8	q11,q15,#7",
				"F3 D0 65 7E", "vsli.16	q11,q15,#0",
				"F3 DF 65 7E", "vsli.16	q11,q15,#15",
				"F3 E0 65 7E", "vsli.32	q11,q15,#0",
				"F3 FF 65 7E", "vsli.32	q11,q15,#31",
				"F3 C0 65 FE", "vsli.64	q11,q15,#0",
				"F3 FF 65 FE", "vsli.64	q11,q15,#63",
				"0E F1 AA CD", "vsqrteq.f32	s21,s26",
				"EE F1 5B EA", "vsqrt.f64	d21,d26",
				"F2 CF 51 3A", "vsra.s8	d21,d26,#1",
				"F2 C8 51 3A", "vsra.s8	d21,d26,#8",
				"F3 CF 51 3A", "vsra.u8	d21,d26,#1",
				"F3 C8 51 3A", "vsra.u8	d21,d26,#8",
				"F2 DF 51 3A", "vsra.s16	d21,d26,#1",
				"F2 D0 51 3A", "vsra.s16	d21,d26,#16",
				"F3 DF 51 3A", "vsra.u16	d21,d26,#1",
				"F3 D0 51 3A", "vsra.u16	d21,d26,#16",
				"F2 FF 51 3A", "vsra.s32	d21,d26,#1",
				"F2 E0 51 3A", "vsra.s32	d21,d26,#32",
				"F3 FF 51 3A", "vsra.u32	d21,d26,#1",
				"F3 E0 51 3A", "vsra.u32	d21,d26,#32",
				"F2 FF 51 BA", "vsra.s64	d21,d26,#1",
				"F2 C0 51 BA", "vsra.s64	d21,d26,#64",
				"F3 FF 51 BA", "vsra.u64	d21,d26,#1",
				"F3 C0 51 BA", "vsra.u64	d21,d26,#64",
				"F2 CF 61 7E", "vsra.s8	q11,q15,#1",
				"F2 C8 61 7E", "vsra.s8	q11,q15,#8",
				"F3 CF 61 7E", "vsra.u8	q11,q15,#1",
				"F3 C8 61 7E", "vsra.u8	q11,q15,#8",
				"F2 DF 61 7E", "vsra.s16	q11,q15,#1",
				"F2 D0 61 7E", "vsra.s16	q11,q15,#16",
				"F3 DF 61 7E", "vsra.u16	q11,q15,#1",
				"F3 D0 61 7E", "vsra.u16	q11,q15,#16",
				"F2 FF 61 7E", "vsra.s32	q11,q15,#1",
				"F2 E0 61 7E", "vsra.s32	q11,q15,#32",
				"F3 FF 61 7E", "vsra.u32	q11,q15,#1",
				"F3 E0 61 7E", "vsra.u32	q11,q15,#32",
				"F2 FF 61 FE", "vsra.s64	q11,q15,#1",
				"F2 C0 61 FE", "vsra.s64	q11,q15,#64",
				"F3 FF 61 FE", "vsra.u64	q11,q15,#1",
				"F3 C0 61 FE", "vsra.u64	q11,q15,#64",
				"F3 CF 54 3A", "vsri.8	d21,d26,#1",
				"F3 C8 54 3A", "vsri.8	d21,d26,#8",
				"F3 DF 54 3A", "vsri.16	d21,d26,#1",
				"F3 D0 54 3A", "vsri.16	d21,d26,#16",
				"F3 FF 54 3A", "vsri.32	d21,d26,#1",
				"F3 E0 54 3A", "vsri.32	d21,d26,#32",
				"F3 FF 54 BA", "vsri.64	d21,d26,#1",
				"F3 C0 54 BA", "vsri.64	d21,d26,#64",
				"F3 CF 64 7E", "vsri.8	q11,q15,#1",
				"F3 C8 64 7E", "vsri.8	q11,q15,#8",
				"F3 DF 64 7E", "vsri.16	q11,q15,#1",
				"F3 D0 64 7E", "vsri.16	q11,q15,#16",
				"F3 FF 64 7E", "vsri.32	q11,q15,#1",
				"F3 E0 64 7E", "vsri.32	q11,q15,#32",
				"F3 FF 64 FE", "vsri.64	q11,q15,#1",
				"F3 C0 64 FE", "vsri.64	q11,q15,#64",
				"F4 4A B7 0F", "vst1.8	{d27},[r10]",
				"F4 4A BA 0F", "vst1.8	{d27,d28},[r10]",
				"F4 4A B6 0F", "vst1.8	{d27,d28,d29},[r10]",
				"F4 4A B2 0F", "vst1.8	{d27,d28,d29,d30},[r10]",
				"F4 4A B7 4F", "vst1.16	{d27},[r10]",
				"F4 4A BA 4F", "vst1.16	{d27,d28},[r10]",
				"F4 4A B6 4F", "vst1.16	{d27,d28,d29},[r10]",
				"F4 4A B2 4F", "vst1.16	{d27,d28,d29,d30},[r10]",
				"F4 4A B7 8F", "vst1.32	{d27},[r10]",
				"F4 4A BA 8F", "vst1.32	{d27,d28},[r10]",
				"F4 4A B6 8F", "vst1.32	{d27,d28,d29},[r10]",
				"F4 4A B2 8F", "vst1.32	{d27,d28,d29,d30},[r10]",
				"F4 4A B7 CF", "vst1.64	{d27},[r10]",
				"F4 4A BA CF", "vst1.64	{d27,d28},[r10]",
				"F4 4A B6 CF", "vst1.64	{d27,d28,d29},[r10]",
				"F4 4A B2 CF", "vst1.64	{d27,d28,d29,d30},[r10]",
				"F4 4A B7 1F", "vst1.8	{d27},[r10@64]",
				"F4 4A BA 1F", "vst1.8	{d27,d28},[r10@64]",
				"F4 4A BA 2F", "vst1.8	{d27,d28},[r10@128]",
				"F4 4A B6 1F", "vst1.8	{d27,d28,d29},[r10@64]",
				"F4 4A B2 1F", "vst1.8	{d27,d28,d29,d30},[r10@64]",
				"F4 4A B2 2F", "vst1.8	{d27,d28,d29,d30},[r10@128]",
				"F4 4A B2 3F", "vst1.8	{d27,d28,d29,d30},[r10@256]",
				"F4 4A B7 5F", "vst1.16	{d27},[r10@64]",
				"F4 4A BA 5F", "vst1.16	{d27,d28},[r10@64]",
				"F4 4A BA 6F", "vst1.16	{d27,d28},[r10@128]",
				"F4 4A B6 5F", "vst1.16	{d27,d28,d29},[r10@64]",
				"F4 4A B2 5F", "vst1.16	{d27,d28,d29,d30},[r10@64]",
				"F4 4A B2 6F", "vst1.16	{d27,d28,d29,d30},[r10@128]",
				"F4 4A B2 7F", "vst1.16	{d27,d28,d29,d30},[r10@256]",
				"F4 4A B7 9F", "vst1.32	{d27},[r10@64]",
				"F4 4A BA 9F", "vst1.32	{d27,d28},[r10@64]",
				"F4 4A BA AF", "vst1.32	{d27,d28},[r10@128]",
				"F4 4A B6 9F", "vst1.32	{d27,d28,d29},[r10@64]",
				"F4 4A B2 9F", "vst1.32	{d27,d28,d29,d30},[r10@64]",
				"F4 4A B2 AF", "vst1.32	{d27,d28,d29,d30},[r10@128]",
				"F4 4A B2 BF", "vst1.32	{d27,d28,d29,d30},[r10@256]",
				"F4 4A B7 DF", "vst1.64	{d27},[r10@64]",
				"F4 4A BA DF", "vst1.64	{d27,d28},[r10@64]",
				"F4 4A BA EF", "vst1.64	{d27,d28},[r10@128]",
				"F4 4A B6 DF", "vst1.64	{d27,d28,d29},[r10@64]",
				"F4 4A B2 DF", "vst1.64	{d27,d28,d29,d30},[r10@64]",
				"F4 4A B2 EF", "vst1.64	{d27,d28,d29,d30},[r10@128]",
				"F4 4A B2 FF", "vst1.64	{d27,d28,d29,d30},[r10@256]",
				"F4 4A B7 0D", "vst1.8	{d27},[r10]!",
				"F4 4A BA 0D", "vst1.8	{d27,d28},[r10]!",
				"F4 4A B6 0D", "vst1.8	{d27,d28,d29},[r10]!",
				"F4 4A B2 0D", "vst1.8	{d27,d28,d29,d30},[r10]!",
				"F4 4A B7 4D", "vst1.16	{d27},[r10]!",
				"F4 4A BA 4D", "vst1.16	{d27,d28},[r10]!",
				"F4 4A B6 4D", "vst1.16	{d27,d28,d29},[r10]!",
				"F4 4A B2 4D", "vst1.16	{d27,d28,d29,d30},[r10]!",
				"F4 4A B7 8D", "vst1.32	{d27},[r10]!",
				"F4 4A BA 8D", "vst1.32	{d27,d28},[r10]!",
				"F4 4A B6 8D", "vst1.32	{d27,d28,d29},[r10]!",
				"F4 4A B2 8D", "vst1.32	{d27,d28,d29,d30},[r10]!",
				"F4 4A B7 CD", "vst1.64	{d27},[r10]!",
				"F4 4A BA CD", "vst1.64	{d27,d28},[r10]!",
				"F4 4A B6 CD", "vst1.64	{d27,d28,d29},[r10]!",
				"F4 4A B2 CD", "vst1.64	{d27,d28,d29,d30},[r10]!",
				"F4 4A B7 1D", "vst1.8	{d27},[r10@64]!",
				"F4 4A BA 1D", "vst1.8	{d27,d28},[r10@64]!",
				"F4 4A BA 2D", "vst1.8	{d27,d28},[r10@128]!",
				"F4 4A B6 1D", "vst1.8	{d27,d28,d29},[r10@64]!",
				"F4 4A B2 1D", "vst1.8	{d27,d28,d29,d30},[r10@64]!",
				"F4 4A B2 2D", "vst1.8	{d27,d28,d29,d30},[r10@128]!",
				"F4 4A B2 3D", "vst1.8	{d27,d28,d29,d30},[r10@256]!",
				"F4 4A B7 5D", "vst1.16	{d27},[r10@64]!",
				"F4 4A BA 5D", "vst1.16	{d27,d28},[r10@64]!",
				"F4 4A BA 6D", "vst1.16	{d27,d28},[r10@128]!",
				"F4 4A B6 5D", "vst1.16	{d27,d28,d29},[r10@64]!",
				"F4 4A B2 5D", "vst1.16	{d27,d28,d29,d30},[r10@64]!",
				"F4 4A B2 6D", "vst1.16	{d27,d28,d29,d30},[r10@128]!",
				"F4 4A B2 7D", "vst1.16	{d27,d28,d29,d30},[r10@256]!",
				"F4 4A B7 9D", "vst1.32	{d27},[r10@64]!",
				"F4 4A BA 9D", "vst1.32	{d27,d28},[r10@64]!",
				"F4 4A BA AD", "vst1.32	{d27,d28},[r10@128]!",
				"F4 4A B6 9D", "vst1.32	{d27,d28,d29},[r10@64]!",
				"F4 4A B2 9D", "vst1.32	{d27,d28,d29,d30},[r10@64]!",
				"F4 4A B2 AD", "vst1.32	{d27,d28,d29,d30},[r10@128]!",
				"F4 4A B2 BD", "vst1.32	{d27,d28,d29,d30},[r10@256]!",
				"F4 4A B7 DD", "vst1.64	{d27},[r10@64]!",
				"F4 4A BA DD", "vst1.64	{d27,d28},[r10@64]!",
				"F4 4A BA ED", "vst1.64	{d27,d28},[r10@128]!",
				"F4 4A B6 DD", "vst1.64	{d27,d28,d29},[r10@64]!",
				"F4 4A B2 DD", "vst1.64	{d27,d28,d29,d30},[r10@64]!",
				"F4 4A B2 ED", "vst1.64	{d27,d28,d29,d30},[r10@128]!",
				"F4 4A B2 FD", "vst1.64	{d27,d28,d29,d30},[r10@256]!",
				"F4 4A B7 09", "vst1.8	{d27},[r10],r9",
				"F4 4A BA 09", "vst1.8	{d27,d28},[r10],r9",
				"F4 4A B6 09", "vst1.8	{d27,d28,d29},[r10],r9",
				"F4 4A B2 09", "vst1.8	{d27,d28,d29,d30},[r10],r9",
				"F4 4A B7 49", "vst1.16	{d27},[r10],r9",
				"F4 4A BA 49", "vst1.16	{d27,d28},[r10],r9",
				"F4 4A B6 49", "vst1.16	{d27,d28,d29},[r10],r9",
				"F4 4A B2 49", "vst1.16	{d27,d28,d29,d30},[r10],r9",
				"F4 4A B7 89", "vst1.32	{d27},[r10],r9",
				"F4 4A BA 89", "vst1.32	{d27,d28},[r10],r9",
				"F4 4A B6 89", "vst1.32	{d27,d28,d29},[r10],r9",
				"F4 4A B2 89", "vst1.32	{d27,d28,d29,d30},[r10],r9",
				"F4 4A B7 C9", "vst1.64	{d27},[r10],r9",
				"F4 4A BA C9", "vst1.64	{d27,d28},[r10],r9",
				"F4 4A B6 C9", "vst1.64	{d27,d28,d29},[r10],r9",
				"F4 4A B2 C9", "vst1.64	{d27,d28,d29,d30},[r10],r9",
				"F4 4A B7 19", "vst1.8	{d27},[r10@64],r9",
				"F4 4A BA 19", "vst1.8	{d27,d28},[r10@64],r9",
				"F4 4A BA 29", "vst1.8	{d27,d28},[r10@128],r9",
				"F4 4A B6 19", "vst1.8	{d27,d28,d29},[r10@64],r9",
				"F4 4A B2 19", "vst1.8	{d27,d28,d29,d30},[r10@64],r9",
				"F4 4A B2 29", "vst1.8	{d27,d28,d29,d30},[r10@128],r9",
				"F4 4A B2 39", "vst1.8	{d27,d28,d29,d30},[r10@256],r9",
				"F4 4A B7 59", "vst1.16	{d27},[r10@64],r9",
				"F4 4A BA 59", "vst1.16	{d27,d28},[r10@64],r9",
				"F4 4A BA 69", "vst1.16	{d27,d28},[r10@128],r9",
				"F4 4A B6 59", "vst1.16	{d27,d28,d29},[r10@64],r9",
				"F4 4A B2 59", "vst1.16	{d27,d28,d29,d30},[r10@64],r9",
				"F4 4A B2 69", "vst1.16	{d27,d28,d29,d30},[r10@128],r9",
				"F4 4A B2 79", "vst1.16	{d27,d28,d29,d30},[r10@256],r9",
				"F4 4A B7 99", "vst1.32	{d27},[r10@64],r9",
				"F4 4A BA 99", "vst1.32	{d27,d28},[r10@64],r9",
				"F4 4A BA A9", "vst1.32	{d27,d28},[r10@128],r9",
				"F4 4A B6 99", "vst1.32	{d27,d28,d29},[r10@64],r9",
				"F4 4A B2 99", "vst1.32	{d27,d28,d29,d30},[r10@64],r9",
				"F4 4A B2 A9", "vst1.32	{d27,d28,d29,d30},[r10@128],r9",
				"F4 4A B2 B9", "vst1.32	{d27,d28,d29,d30},[r10@256],r9",
				"F4 4A B7 D9", "vst1.64	{d27},[r10@64],r9",
				"F4 4A BA D9", "vst1.64	{d27,d28},[r10@64],r9",
				"F4 4A BA E9", "vst1.64	{d27,d28},[r10@128],r9",
				"F4 4A B6 D9", "vst1.64	{d27,d28,d29},[r10@64],r9",
				"F4 4A B2 D9", "vst1.64	{d27,d28,d29,d30},[r10@64],r9",
				"F4 4A B2 E9", "vst1.64	{d27,d28,d29,d30},[r10@128],r9",
				"F4 4A B2 F9", "vst1.64	{d27,d28,d29,d30},[r10@256],r9",
				"F4 CA B0 2F", "vst1.8	{d27[1]},[r10]",
				"F4 CA B4 4F", "vst1.16	{d27[1]},[r10]",
				"F4 CA B8 8F", "vst1.32	{d27[1]},[r10]",
				"F4 CA B4 5F", "vst1.16	{d27[1]},[r10@16]",
				"F4 CA B8 BF", "vst1.32	{d27[1]},[r10@32]",
				"F4 CA B0 2D", "vst1.8	{d27[1]},[r10]!",
				"F4 CA B4 4D", "vst1.16	{d27[1]},[r10]!",
				"F4 CA B8 8D", "vst1.32	{d27[1]},[r10]!",
				"F4 CA B4 5D", "vst1.16	{d27[1]},[r10@16]!",
				"F4 CA B8 BD", "vst1.32	{d27[1]},[r10@32]!",
				"F4 CA B0 29", "vst1.8	{d27[1]},[r10],r9",
				"F4 CA B4 49", "vst1.16	{d27[1]},[r10],r9",
				"F4 CA B8 89", "vst1.32	{d27[1]},[r10],r9",
				"F4 CA B4 59", "vst1.16	{d27[1]},[r10@16],r9",
				"F4 CA B8 B9", "vst1.32	{d27[1]},[r10@32],r9",
				"F4 4A B8 0F", "vst2.8	{d27,d28},[r10]",
				"F4 4A B9 0F", "vst2.8	{d27,d29},[r10]",
				"F4 4A B3 0F", "vst2.8	{d27,d28,d29,d30},[r10]",
				"F4 4A B8 4F", "vst2.16	{d27,d28},[r10]",
				"F4 4A B9 4F", "vst2.16	{d27,d29},[r10]",
				"F4 4A B3 4F", "vst2.16	{d27,d28,d29,d30},[r10]",
				"F4 4A B8 1F", "vst2.8	{d27,d28},[r10@64]",
				"F4 4A B8 2F", "vst2.8	{d27,d28},[r10@128]",
				"F4 4A B9 1F", "vst2.8	{d27,d29},[r10@64]",
				"F4 4A B9 2F", "vst2.8	{d27,d29},[r10@128]",
				"F4 4A B3 1F", "vst2.8	{d27,d28,d29,d30},[r10@64]",
				"F4 4A B3 2F", "vst2.8	{d27,d28,d29,d30},[r10@128]",
				"F4 4A B3 3F", "vst2.8	{d27,d28,d29,d30},[r10@256]",
				"F4 4A B8 5F", "vst2.16	{d27,d28},[r10@64]",
				"F4 4A B8 6F", "vst2.16	{d27,d28},[r10@128]",
				"F4 4A B9 5F", "vst2.16	{d27,d29},[r10@64]",
				"F4 4A B9 6F", "vst2.16	{d27,d29},[r10@128]",
				"F4 4A B3 5F", "vst2.16	{d27,d28,d29,d30},[r10@64]",
				"F4 4A B3 6F", "vst2.16	{d27,d28,d29,d30},[r10@128]",
				"F4 4A B3 7F", "vst2.16	{d27,d28,d29,d30},[r10@256]",
				"F4 4A B8 0D", "vst2.8	{d27,d28},[r10]!",
				"F4 4A B9 0D", "vst2.8	{d27,d29},[r10]!",
				"F4 4A B3 0D", "vst2.8	{d27,d28,d29,d30},[r10]!",
				"F4 4A B8 4D", "vst2.16	{d27,d28},[r10]!",
				"F4 4A B9 4D", "vst2.16	{d27,d29},[r10]!",
				"F4 4A B3 4D", "vst2.16	{d27,d28,d29,d30},[r10]!",
				"F4 4A B8 1D", "vst2.8	{d27,d28},[r10@64]!",
				"F4 4A B8 2D", "vst2.8	{d27,d28},[r10@128]!",
				"F4 4A B9 1D", "vst2.8	{d27,d29},[r10@64]!",
				"F4 4A B9 2D", "vst2.8	{d27,d29},[r10@128]!",
				"F4 4A B3 1D", "vst2.8	{d27,d28,d29,d30},[r10@64]!",
				"F4 4A B3 2D", "vst2.8	{d27,d28,d29,d30},[r10@128]!",
				"F4 4A B3 3D", "vst2.8	{d27,d28,d29,d30},[r10@256]!",
				"F4 4A B8 5D", "vst2.16	{d27,d28},[r10@64]!",
				"F4 4A B8 6D", "vst2.16	{d27,d28},[r10@128]!",
				"F4 4A B9 5D", "vst2.16	{d27,d29},[r10@64]!",
				"F4 4A B9 6D", "vst2.16	{d27,d29},[r10@128]!",
				"F4 4A B3 5D", "vst2.16	{d27,d28,d29,d30},[r10@64]!",
				"F4 4A B3 6D", "vst2.16	{d27,d28,d29,d30},[r10@128]!",
				"F4 4A B3 7D", "vst2.16	{d27,d28,d29,d30},[r10@256]!",
				"F4 4A B8 09", "vst2.8	{d27,d28},[r10],r9",
				"F4 4A B9 09", "vst2.8	{d27,d29},[r10],r9",
				"F4 4A B3 09", "vst2.8	{d27,d28,d29,d30},[r10],r9",
				"F4 4A B8 49", "vst2.16	{d27,d28},[r10],r9",
				"F4 4A B9 49", "vst2.16	{d27,d29},[r10],r9",
				"F4 4A B3 49", "vst2.16	{d27,d28,d29,d30},[r10],r9",
				"F4 4A B8 19", "vst2.8	{d27,d28},[r10@64],r9",
				"F4 4A B8 29", "vst2.8	{d27,d28},[r10@128],r9",
				"F4 4A B9 19", "vst2.8	{d27,d29},[r10@64],r9",
				"F4 4A B9 29", "vst2.8	{d27,d29},[r10@128],r9",
				"F4 4A B3 19", "vst2.8	{d27,d28,d29,d30},[r10@64],r9",
				"F4 4A B3 29", "vst2.8	{d27,d28,d29,d30},[r10@128],r9",
				"F4 4A B3 39", "vst2.8	{d27,d28,d29,d30},[r10@256],r9",
				"F4 4A B8 59", "vst2.16	{d27,d28},[r10@64],r9",
				"F4 4A B8 69", "vst2.16	{d27,d28},[r10@128],r9",
				"F4 4A B9 59", "vst2.16	{d27,d29},[r10@64],r9",
				"F4 4A B9 69", "vst2.16	{d27,d29},[r10@128],r9",
				"F4 4A B3 59", "vst2.16	{d27,d28,d29,d30},[r10@64],r9",
				"F4 4A B3 69", "vst2.16	{d27,d28,d29,d30},[r10@128],r9",
				"F4 4A B3 79", "vst2.16	{d27,d28,d29,d30},[r10@256],r9",
				"F4 CA B1 2F", "vst2.8	{d27[1],d28[1]},[r10]",
				"F4 CA B5 4F", "vst2.16	{d27[1],d28[1]},[r10]",
				"F4 CA B5 6F", "vst2.16	{d27[1],d29[1]},[r10]",
				"F4 CA B9 8F", "vst2.32	{d27[1],d28[1]},[r10]",
				"F4 CA B9 CF", "vst2.32	{d27[1],d29[1]},[r10]",
				"F4 CA B1 3F", "vst2.8	{d27[1],d28[1]},[r10@16]",
				"F4 CA B5 5F", "vst2.16	{d27[1],d28[1]},[r10@32]",
				"F4 CA B5 7F", "vst2.16	{d27[1],d29[1]},[r10@32]",
				"F4 CA B9 9F", "vst2.32	{d27[1],d28[1]},[r10@64]",
				"F4 CA B9 DF", "vst2.32	{d27[1],d29[1]},[r10@64]",
				"F4 CA B1 2D", "vst2.8	{d27[1],d28[1]},[r10]!",
				"F4 CA B5 4D", "vst2.16	{d27[1],d28[1]},[r10]!",
				"F4 CA B5 6D", "vst2.16	{d27[1],d29[1]},[r10]!",
				"F4 CA B9 8D", "vst2.32	{d27[1],d28[1]},[r10]!",
				"F4 CA B9 CD", "vst2.32	{d27[1],d29[1]},[r10]!",
				"F4 CA B1 3D", "vst2.8	{d27[1],d28[1]},[r10@16]!",
				"F4 CA B5 5D", "vst2.16	{d27[1],d28[1]},[r10@32]!",
				"F4 CA B5 7D", "vst2.16	{d27[1],d29[1]},[r10@32]!",
				"F4 CA B9 9D", "vst2.32	{d27[1],d28[1]},[r10@64]!",
				"F4 CA B9 DD", "vst2.32	{d27[1],d29[1]},[r10@64]!",
				"F4 CA B1 29", "vst2.8	{d27[1],d28[1]},[r10],r9",
				"F4 CA B5 49", "vst2.16	{d27[1],d28[1]},[r10],r9",
				"F4 CA B5 69", "vst2.16	{d27[1],d29[1]},[r10],r9",
				"F4 CA B9 89", "vst2.32	{d27[1],d28[1]},[r10],r9",
				"F4 CA B9 C9", "vst2.32	{d27[1],d29[1]},[r10],r9",
				"F4 CA B1 39", "vst2.8	{d27[1],d28[1]},[r10@16],r9",
				"F4 CA B5 59", "vst2.16	{d27[1],d28[1]},[r10@32],r9",
				"F4 CA B5 79", "vst2.16	{d27[1],d29[1]},[r10@32],r9",
				"F4 CA B9 99", "vst2.32	{d27[1],d28[1]},[r10@64],r9",
				"F4 CA B9 D9", "vst2.32	{d27[1],d29[1]},[r10@64],r9",
				"F4 CA B2 2F", "vst3.8	{d27[1],d28[1],d29[1]},[r10]",
				"F4 CA B6 4F", "vst3.16	{d27[1],d28[1],d29[1]},[r10]",
				"F4 CA B6 6F", "vst3.16	{d27[1],d29[1],d31[1]},[r10]",
				"F4 CA BA 8F", "vst3.32	{d27[1],d28[1],d29[1]},[r10]",
				"F4 CA BA CF", "vst3.32	{d27[1],d29[1],d31[1]},[r10]",
				"F4 CA B2 2D", "vst3.8	{d27[1],d28[1],d29[1]},[r10]!",
				"F4 CA B6 4D", "vst3.16	{d27[1],d28[1],d29[1]},[r10]!",
				"F4 CA B6 6D", "vst3.16	{d27[1],d29[1],d31[1]},[r10]!",
				"F4 CA BA 8D", "vst3.32	{d27[1],d28[1],d29[1]},[r10]!",
				"F4 CA BA CD", "vst3.32	{d27[1],d29[1],d31[1]},[r10]!",
				"F4 CA B2 29", "vst3.8	{d27[1],d28[1],d29[1]},[r10],r9",
				"F4 CA B6 49", "vst3.16	{d27[1],d28[1],d29[1]},[r10],r9",
				"F4 CA B6 69", "vst3.16	{d27[1],d29[1],d31[1]},[r10],r9",
				"F4 CA BA 89", "vst3.32	{d27[1],d28[1],d29[1]},[r10],r9",
				"F4 CA BA C9", "vst3.32	{d27[1],d29[1],d31[1]},[r10],r9",
				"F4 CA B2 2F", "vst3.8	{d27[1],d28[1],d29[1]},[r10]",
				"F4 CA B6 4F", "vst3.16	{d27[1],d28[1],d29[1]},[r10]",
				"F4 CA B6 6F", "vst3.16	{d27[1],d29[1],d31[1]},[r10]",
				"F4 CA BA 8F", "vst3.32	{d27[1],d28[1],d29[1]},[r10]",
				"F4 CA BA CF", "vst3.32	{d27[1],d29[1],d31[1]},[r10]",
				"F4 CA B2 2D", "vst3.8	{d27[1],d28[1],d29[1]},[r10]!",
				"F4 CA B6 4D", "vst3.16	{d27[1],d28[1],d29[1]},[r10]!",
				"F4 CA B6 6D", "vst3.16	{d27[1],d29[1],d31[1]},[r10]!",
				"F4 CA BA 8D", "vst3.32	{d27[1],d28[1],d29[1]},[r10]!",
				"F4 CA BA CD", "vst3.32	{d27[1],d29[1],d31[1]},[r10]!",
				"F4 CA B2 29", "vst3.8	{d27[1],d28[1],d29[1]},[r10],r9",
				"F4 CA B6 49", "vst3.16	{d27[1],d28[1],d29[1]},[r10],r9",
				"F4 CA B6 69", "vst3.16	{d27[1],d29[1],d31[1]},[r10],r9",
				"F4 CA BA 89", "vst3.32	{d27[1],d28[1],d29[1]},[r10],r9",
				"F4 CA BA C9", "vst3.32	{d27[1],d29[1],d31[1]},[r10],r9",
				"F4 4A B0 0F", "vst4.8	{d27,d28,d29,d30},[r10]",
				"F4 4A 91 0F", "vst4.8	{d25,d27,d29,d31},[r10]",
				"F4 4A B0 4F", "vst4.16	{d27,d28,d29,d30},[r10]",
				"F4 4A 91 4F", "vst4.16	{d25,d27,d29,d31},[r10]",
				"F4 4A B0 8F", "vst4.32	{d27,d28,d29,d30},[r10]",
				"F4 4A 91 8F", "vst4.32	{d25,d27,d29,d31},[r10]",
				"F4 4A B0 1F", "vst4.8	{d27,d28,d29,d30},[r10@64]",
				"F4 4A B0 2F", "vst4.8	{d27,d28,d29,d30},[r10@128]",
				"F4 4A B0 3F", "vst4.8	{d27,d28,d29,d30},[r10@256]",
				"F4 4A 91 1F", "vst4.8	{d25,d27,d29,d31},[r10@64]",
				"F4 4A 91 2F", "vst4.8	{d25,d27,d29,d31},[r10@128]",
				"F4 4A 91 3F", "vst4.8	{d25,d27,d29,d31},[r10@256]",
				"F4 4A B0 5F", "vst4.16	{d27,d28,d29,d30},[r10@64]",
				"F4 4A B0 6F", "vst4.16	{d27,d28,d29,d30},[r10@128]",
				"F4 4A B0 7F", "vst4.16	{d27,d28,d29,d30},[r10@256]",
				"F4 4A 91 5F", "vst4.16	{d25,d27,d29,d31},[r10@64]",
				"F4 4A 91 6F", "vst4.16	{d25,d27,d29,d31},[r10@128]",
				"F4 4A 91 7F", "vst4.16	{d25,d27,d29,d31},[r10@256]",
				"F4 4A B0 9F", "vst4.32	{d27,d28,d29,d30},[r10@64]",
				"F4 4A B0 AF", "vst4.32	{d27,d28,d29,d30},[r10@128]",
				"F4 4A B0 BF", "vst4.32	{d27,d28,d29,d30},[r10@256]",
				"F4 4A 91 9F", "vst4.32	{d25,d27,d29,d31},[r10@64]",
				"F4 4A 91 AF", "vst4.32	{d25,d27,d29,d31},[r10@128]",
				"F4 4A 91 BF", "vst4.32	{d25,d27,d29,d31},[r10@256]",
				"F4 4A B0 0D", "vst4.8	{d27,d28,d29,d30},[r10]!",
				"F4 4A 91 0D", "vst4.8	{d25,d27,d29,d31},[r10]!",
				"F4 4A B0 4D", "vst4.16	{d27,d28,d29,d30},[r10]!",
				"F4 4A 91 4D", "vst4.16	{d25,d27,d29,d31},[r10]!",
				"F4 4A B0 8D", "vst4.32	{d27,d28,d29,d30},[r10]!",
				"F4 4A 91 8D", "vst4.32	{d25,d27,d29,d31},[r10]!",
				"F4 4A B0 1D", "vst4.8	{d27,d28,d29,d30},[r10@64]!",
				"F4 4A B0 2D", "vst4.8	{d27,d28,d29,d30},[r10@128]!",
				"F4 4A B0 3D", "vst4.8	{d27,d28,d29,d30},[r10@256]!",
				"F4 4A 91 1D", "vst4.8	{d25,d27,d29,d31},[r10@64]!",
				"F4 4A 91 2D", "vst4.8	{d25,d27,d29,d31},[r10@128]!",
				"F4 4A 91 3D", "vst4.8	{d25,d27,d29,d31},[r10@256]!",
				"F4 4A B0 5D", "vst4.16	{d27,d28,d29,d30},[r10@64]!",
				"F4 4A B0 6D", "vst4.16	{d27,d28,d29,d30},[r10@128]!",
				"F4 4A B0 7D", "vst4.16	{d27,d28,d29,d30},[r10@256]!",
				"F4 4A 91 5D", "vst4.16	{d25,d27,d29,d31},[r10@64]!",
				"F4 4A 91 6D", "vst4.16	{d25,d27,d29,d31},[r10@128]!",
				"F4 4A 91 7D", "vst4.16	{d25,d27,d29,d31},[r10@256]!",
				"F4 4A B0 9D", "vst4.32	{d27,d28,d29,d30},[r10@64]!",
				"F4 4A B0 AD", "vst4.32	{d27,d28,d29,d30},[r10@128]!",
				"F4 4A B0 BD", "vst4.32	{d27,d28,d29,d30},[r10@256]!",
				"F4 4A 91 9D", "vst4.32	{d25,d27,d29,d31},[r10@64]!",
				"F4 4A 91 AD", "vst4.32	{d25,d27,d29,d31},[r10@128]!",
				"F4 4A 91 BD", "vst4.32	{d25,d27,d29,d31},[r10@256]!",
				"F4 4A B0 09", "vst4.8	{d27,d28,d29,d30},[r10],r9",
				"F4 4A 91 09", "vst4.8	{d25,d27,d29,d31},[r10],r9",
				"F4 4A B0 49", "vst4.16	{d27,d28,d29,d30},[r10],r9",
				"F4 4A 91 49", "vst4.16	{d25,d27,d29,d31},[r10],r9",
				"F4 4A B0 89", "vst4.32	{d27,d28,d29,d30},[r10],r9",
				"F4 4A 91 89", "vst4.32	{d25,d27,d29,d31},[r10],r9",
				"F4 4A B0 19", "vst4.8	{d27,d28,d29,d30},[r10@64],r9",
				"F4 4A B0 29", "vst4.8	{d27,d28,d29,d30},[r10@128],r9",
				"F4 4A B0 39", "vst4.8	{d27,d28,d29,d30},[r10@256],r9",
				"F4 4A 91 19", "vst4.8	{d25,d27,d29,d31},[r10@64],r9",
				"F4 4A 91 29", "vst4.8	{d25,d27,d29,d31},[r10@128],r9",
				"F4 4A 91 39", "vst4.8	{d25,d27,d29,d31},[r10@256],r9",
				"F4 4A B0 59", "vst4.16	{d27,d28,d29,d30},[r10@64],r9",
				"F4 4A B0 69", "vst4.16	{d27,d28,d29,d30},[r10@128],r9",
				"F4 4A B0 79", "vst4.16	{d27,d28,d29,d30},[r10@256],r9",
				"F4 4A 91 59", "vst4.16	{d25,d27,d29,d31},[r10@64],r9",
				"F4 4A 91 69", "vst4.16	{d25,d27,d29,d31},[r10@128],r9",
				"F4 4A 91 79", "vst4.16	{d25,d27,d29,d31},[r10@256],r9",
				"F4 4A B0 99", "vst4.32	{d27,d28,d29,d30},[r10@64],r9",
				"F4 4A B0 A9", "vst4.32	{d27,d28,d29,d30},[r10@128],r9",
				"F4 4A B0 B9", "vst4.32	{d27,d28,d29,d30},[r10@256],r9",
				"F4 4A 91 99", "vst4.32	{d25,d27,d29,d31},[r10@64],r9",
				"F4 4A 91 A9", "vst4.32	{d25,d27,d29,d31},[r10@128],r9",
				"F4 4A 91 B9", "vst4.32	{d25,d27,d29,d31},[r10@256],r9",
				"F4 CA B3 2F", "vst4.8	{d27[1],d28[1],d29[1],d30[1]},[r10]",
				"F4 CA B7 4F", "vst4.16	{d27[1],d28[1],d29[1],d30[1]},[r10]",
				"F4 CA 97 6F", "vst4.16	{d25[1],d27[1],d29[1],d31[1]},[r10]",
				"F4 CA BB 8F", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10]",
				"F4 CA 9B CF", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10]",
				"F4 CA B3 3F", "vst4.8	{d27[1],d28[1],d29[1],d30[1]},[r10@32]",
				"F4 CA B7 5F", "vst4.16	{d27[1],d28[1],d29[1],d30[1]},[r10@64]",
				"F4 CA 97 7F", "vst4.16	{d25[1],d27[1],d29[1],d31[1]},[r10@64]",
				"F4 CA BB 9F", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@64]",
				"F4 CA BB AF", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@128]",
				"F4 CA 9B DF", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@64]",
				"F4 CA 9B EF", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@128]",
				"F4 CA B3 2D", "vst4.8	{d27[1],d28[1],d29[1],d30[1]},[r10]!",
				"F4 CA B7 4D", "vst4.16	{d27[1],d28[1],d29[1],d30[1]},[r10]!",
				"F4 CA 97 6D", "vst4.16	{d25[1],d27[1],d29[1],d31[1]},[r10]!",
				"F4 CA BB 8D", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10]!",
				"F4 CA 9B CD", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10]!",
				"F4 CA B3 3D", "vst4.8	{d27[1],d28[1],d29[1],d30[1]},[r10@32]!",
				"F4 CA B7 5D", "vst4.16	{d27[1],d28[1],d29[1],d30[1]},[r10@64]!",
				"F4 CA 97 7D", "vst4.16	{d25[1],d27[1],d29[1],d31[1]},[r10@64]!",
				"F4 CA BB 9D", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@64]!",
				"F4 CA BB AD", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@128]!",
				"F4 CA 9B DD", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@64]!",
				"F4 CA 9B ED", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@128]!",
				"F4 CA B3 29", "vst4.8	{d27[1],d28[1],d29[1],d30[1]},[r10],r9",
				"F4 CA B7 49", "vst4.16	{d27[1],d28[1],d29[1],d30[1]},[r10],r9",
				"F4 CA 97 69", "vst4.16	{d25[1],d27[1],d29[1],d31[1]},[r10],r9",
				"F4 CA BB 89", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10],r9",
				"F4 CA 9B C9", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10],r9",
				"F4 CA B3 39", "vst4.8	{d27[1],d28[1],d29[1],d30[1]},[r10@32],r9",
				"F4 CA B7 59", "vst4.16	{d27[1],d28[1],d29[1],d30[1]},[r10@64],r9",
				"F4 CA 97 79", "vst4.16	{d25[1],d27[1],d29[1],d31[1]},[r10@64],r9",
				"F4 CA BB 99", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@64],r9",
				"F4 CA BB A9", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@128],r9",
				"F4 CA 9B D9", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@64],r9",
				"F4 CA 9B E9", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@128],r9",
				"0C CA BB 04", "vstmiaeq	r10,{d27-d28}",
				"0C CA DA 02", "vstmiaeq	r10,{s27-s28}",
				"EC EA BB 04", "vstmia	r10!,{d27-d28}",
				"ED 6A BB 04", "vstmdb	r10!,{d27-d28}",
				"EC EA DA 02", "vstmia	r10!,{s27-s28}",
				"ED 6A DA 02", "vstmdb	r10!,{s27-s28}",
				"0D 4A 5B FF", "vstreq.64	d21,[r10,#-0x3fc]",
				"ED CA 5B FF", "vstr.64	d21,[r10,#0x3fc]",
				"ED CA 5B 00", "vstr.64	d21,[r10]",
				"0D 4A AA FF", "vstreq.32	s21,[r10,#-0x3fc]",
				"ED CA AA FF", "vstr.32	s21,[r10,#0x3fc]",
				"ED CA AA 00", "vstr.32	s21,[r10]",
				"F3 49 58 AA", "vsub.i8	d21,d25,d26",
				"F3 59 58 AA", "vsub.i16	d21,d25,d26",
				"F3 69 58 AA", "vsub.i32	d21,d25,d26",
				"F3 79 58 AA", "vsub.i64	d21,d25,d26",
				"F3 4C 68 EE", "vsub.i8	q11,q14,q15",
				"F3 5C 68 EE", "vsub.i16	q11,q14,q15",
				"F3 6C 68 EE", "vsub.i32	q11,q14,q15",
				"F3 7C 68 EE", "vsub.i64	q11,q14,q15",
				"F2 69 5D AA", "vsub.f32	d21,d25,d26",
				"F2 6C 6D EE", "vsub.f32	q11,q14,q15",
				"0E 7C AA CD", "vsubeq.f32	s21,s25,s26",
				"EE 79 5B EA", "vsub.f64	d21,d25,d26",
				"F2 CC 56 AE", "vsubhn.i16	d21,q14,q15",
				"F2 DC 56 AE", "vsubhn.i32	d21,q14,q15",
				"F2 EC 56 AE", "vsubhn.i64	d21,q14,q15",
				"F2 C9 62 AA", "vsubl.s8	q11,d25,d26",
				"F2 D9 62 AA", "vsubl.s16	q11,d25,d26",
				"F2 E9 62 AA", "vsubl.s32	q11,d25,d26",
				"F3 C9 62 AA", "vsubl.u8	q11,d25,d26",
				"F3 D9 62 AA", "vsubl.u16	q11,d25,d26",
				"F3 E9 62 AA", "vsubl.u32	q11,d25,d26",
				"F2 CC 63 AA", "vsubw.s8	q11,q14,d26",
				"F2 DC 63 AA", "vsubw.s16	q11,q14,d26",
				"F2 EC 63 AA", "vsubw.s32	q11,q14,d26",
				"F3 CC 63 AA", "vsubw.u8	q11,q14,d26",
				"F3 DC 63 AA", "vsubw.u16	q11,q14,d26",
				"F3 EC 63 AA", "vsubw.u32	q11,q14,d26",
				"F3 F2 50 2A", "vswp	d21,d26",
				"F3 F2 60 6E", "vswp	q11,q15",
				"F3 FB 58 AA", "vtbl.8	d21,{d27},d26",
				"F3 FB 59 AA", "vtbl.8	d21,{d27,d28},d26",
				"F3 FB 5A AA", "vtbl.8	d21,{d27,d28,d29},d26",
				"F3 FB 5B AA", "vtbl.8	d21,{d27,d28,d29,d30},d26",
				"F3 FB 58 EA", "vtbx.8	d21,{d27},d26",
				"F3 FB 59 EA", "vtbx.8	d21,{d27,d28},d26",
				"F3 FB 5A EA", "vtbx.8	d21,{d27,d28,d29},d26",
				"F3 FB 5B EA", "vtbx.8	d21,{d27,d28,d29,d30},d26",
				"F3 F2 50 AA", "vtrn.8	d21,d26",
				"F3 F6 50 AA", "vtrn.16	d21,d26",
				"F3 FA 50 AA", "vtrn.32	d21,d26",
				"F3 F2 60 EE", "vtrn.8	q11,q15",
				"F3 F6 60 EE", "vtrn.16	q11,q15",
				"F3 FA 60 EE", "vtrn.32	q11,q15",
				"F2 49 58 BA", "vtst.8	d21,d25,d26",
				"F2 59 58 BA", "vtst.16	d21,d25,d26",
				"F2 69 58 BA", "vtst.32	d21,d25,d26",
				"F2 4C 68 FE", "vtst.8	q11,q14,q15",
				"F2 5C 68 FE", "vtst.16	q11,q14,q15",
				"F2 6C 68 FE", "vtst.32	q11,q14,q15",
				"F3 F2 51 2A", "vuzp.8	d21,d26",
				"F3 F6 51 2A", "vuzp.16	d21,d26",
				"F3 F2 61 6E", "vuzp.8	q11,q15",
				"F3 F6 61 6E", "vuzp.16	q11,q15",
				"F3 FA 61 6E", "vuzp.32	q11,q15",
				"F3 F2 51 AA", "vzip.8	d21,d26",
				"F3 F6 51 AA", "vzip.16	d21,d26",
				"F3 F2 61 EE", "vzip.8	q11,q15",
				"F3 F6 61 EE", "vzip.16	q11,q15",
				"F3 FA 61 EE", "vzip.32	q11,q15",
			};

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM condition code.
	 */
	@Test
	public void testArmConditionCode() {

		System.out.println("\n================ ARM Condition Code ================\n");
		String[] insts = {
				"00 A1 00 02", "adceq	r0,r1,r2",
				"10 A1 00 02", "adcne	r0,r1,r2",
				"20 A1 00 02", "adccs	r0,r1,r2",
				"30 A1 00 02", "adccc	r0,r1,r2",
				"40 A1 00 02", "adcmi	r0,r1,r2",
				"50 A1 00 02", "adcpl	r0,r1,r2",
				"60 A1 00 02", "adcvs	r0,r1,r2",
				"70 A1 00 02", "adcvc	r0,r1,r2",
				"80 A1 00 02", "adchi	r0,r1,r2",
				"90 A1 00 02", "adcls	r0,r1,r2",
				"A0 A1 00 02", "adcge	r0,r1,r2",
				"B0 A1 00 02", "adclt	r0,r1,r2",
				"C0 A1 00 02", "adcgt	r0,r1,r2",
				"D0 A1 00 02", "adcle	r0,r1,r2",
				"E0 A1 00 02", "adc	r0,r1,r2", };

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM addressing mode 1 (shifter operand).
	 */
	@Test
	public void testArmAddrMode1() {

		System.out.println("\n================== ARM Addr Mode 1 ==================\n");
		String[] insts = {
				"E2 81 00 11", "add	r0,r1,#0x11",
				"E0 81 00 02", "add	r0,r1,r2",
				"E0 81 08 82", "add	r0,r1,r2,lsl #17",
				"E0 81 03 12", "add	r0,r1,r2,lsl r3",
				"E0 81 08 A2", "add	r0,r1,r2,lsr #17",
				"E0 81 03 32", "add	r0,r1,r2,lsr r3",
				"E0 81 08 C2", "add	r0,r1,r2,asr #17",
				"E0 81 03 52", "add	r0,r1,r2,asr r3",
				"E0 81 08 E2", "add	r0,r1,r2,ror #17",
				"E0 81 03 72", "add	r0,r1,r2,ror r3",
				"E0 81 00 62", "add	r0,r1,r2,rrx",
				};

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM addressing mode 2.
	 */
	@Test
	public void testArmAddrMode2() {

		System.out.println("\n================== ARM Addr Mode 2 ==================\n");
		String[] insts = {
				"E5 91 00 11", "ldr	r0,[r1,#0x11]",
				"E5 11 00 11", "ldr	r0,[r1,#-0x11]",
				"E7 91 00 02", "ldr	r0,[r1,r2]",
				"E7 11 00 02", "ldr	r0,[r1,-r2]",
				"E7 91 08 82", "ldr	r0,[r1,r2,lsl #17]",
				"E7 91 08 A2", "ldr	r0,[r1,r2,lsr #17]",
				"E7 91 08 C2", "ldr	r0,[r1,r2,asr #17]",
				"E7 91 08 E2", "ldr	r0,[r1,r2,ror #17]",
				"E7 91 00 62", "ldr	r0,[r1,r2,rrx]",
				"E7 11 08 82", "ldr	r0,[r1,-r2,lsl #17]",
				"E7 11 08 A2", "ldr	r0,[r1,-r2,lsr #17]",
				"E7 11 08 C2", "ldr	r0,[r1,-r2,asr #17]",
				"E7 11 08 E2", "ldr	r0,[r1,-r2,ror #17]",
				"E7 11 00 62", "ldr	r0,[r1,-r2,rrx]",
				"E5 B1 00 11", "ldr	r0,[r1,#0x11]!",
				"E5 31 00 11", "ldr	r0,[r1,#-0x11]!",
				"E7 B1 00 02", "ldr	r0,[r1,r2]!",
				"E7 31 00 02", "ldr	r0,[r1,-r2]!",
				"E7 B1 08 82", "ldr	r0,[r1,r2,lsl #17]!",
				"E7 B1 08 A2", "ldr	r0,[r1,r2,lsr #17]!",
				"E7 B1 08 C2", "ldr	r0,[r1,r2,asr #17]!",
				"E7 B1 08 E2", "ldr	r0,[r1,r2,ror #17]!",
				"E7 B1 00 62", "ldr	r0,[r1,r2,rrx]!",
				"E7 31 08 82", "ldr	r0,[r1,-r2,lsl #17]!",
				"E7 31 08 A2", "ldr	r0,[r1,-r2,lsr #17]!",
				"E7 31 08 C2", "ldr	r0,[r1,-r2,asr #17]!",
				"E7 31 08 E2", "ldr	r0,[r1,-r2,ror #17]!",
				"E7 31 00 62", "ldr	r0,[r1,-r2,rrx]!",
				"E4 91 00 11", "ldr	r0,[r1],#0x11",
				"E4 11 00 11", "ldr	r0,[r1],#-0x11",
				"E6 91 00 02", "ldr	r0,[r1],r2",
				"E6 11 00 02", "ldr	r0,[r1],-r2",
				"E6 91 08 82", "ldr	r0,[r1],r2,lsl #17",
				"E6 91 08 A2", "ldr	r0,[r1],r2,lsr #17",
				"E6 91 08 C2", "ldr	r0,[r1],r2,asr #17",
				"E6 91 08 E2", "ldr	r0,[r1],r2,ror #17",
				"E6 91 00 62", "ldr	r0,[r1],r2,rrx",
				"E6 11 08 82", "ldr	r0,[r1],-r2,lsl #17",
				"E6 11 08 A2", "ldr	r0,[r1],-r2,lsr #17",
				"E6 11 08 C2", "ldr	r0,[r1],-r2,asr #17",
				"E6 11 08 E2", "ldr	r0,[r1],-r2,ror #17",
				"E6 11 00 62", "ldr	r0,[r1],-r2,rrx",
				};

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM addressing mode 3.
	 */
	@Test
	public void testArmAddrMode3() {

		System.out.println("\n================== ARM Addr Mode 3 ==================\n");
		String[] insts = {
				"E1 C1 01 B0", "strh	r0,[r1,#0x10]",
				"E1 41 01 B0", "strh	r0,[r1,#-0x10]",
				"E1 81 00 B2", "strh	r0,[r1,r2]",
				"E1 01 00 B2", "strh	r0,[r1,-r2]",
				"E1 E1 01 B0", "strh	r0,[r1,#0x10]!",
				"E1 61 01 B0", "strh	r0,[r1,#-0x10]!",
				"E1 A1 00 B2", "strh	r0,[r1,r2]!",
				"E1 21 00 B2", "strh	r0,[r1,-r2]!",
				"E0 C1 01 B0", "strh	r0,[r1],#0x10",
				"E0 41 01 B0", "strh	r0,[r1],#-0x10",
				"E0 81 00 B2", "strh	r0,[r1],r2",
				"E0 01 00 B2", "strh	r0,[r1],-r2",
				};

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM addressing mode 4.
	 */
	@Test
	public void testArmAddrMode4() {

		System.out.println("\n================== ARM Addr Mode 4 ==================\n");
		String[] insts = {
				"E8 90 00 06", "ldm	r0,{r1,r2}",
				"E9 90 00 06", "ldmib	r0,{r1,r2}",
				"E8 10 00 06", "ldmda	r0,{r1,r2}",
				"E9 10 00 06", "ldmdb	r0,{r1,r2}", };

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM addressing mode 5.
	 */
	@Test
	public void testArmAddrMode5() {

		System.out.println("\n================== ARM Addr Mode 5 ==================\n");
		String[] insts = {
				"ED 92 10 04", "ldc	p0,c1,[r2,#0x10]",
				"ED 12 10 04", "ldc	p0,c1,[r2,#-0x10]",
				"ED B2 10 04", "ldc	p0,c1,[r2,#0x10]!",
				"ED 32 10 04", "ldc	p0,c1,[r2,#-0x10]!",
				"EC B2 10 04", "ldc	p0,c1,[r2],#0x10",
				"EC 32 10 04", "ldc	p0,c1,[r2],#-0x10",
				"EC 92 10 00", "ldc	p0,c1,[r2],{0}", };

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM branching instructions.
	 */
	@Test
	public void testArmBranches() {

		armOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_ADDRESS, true);
		armOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_BYTES, true);
		System.out.println("\n=================== ARM Branches ====================\n");
		disassembleInst(0x00000000, "0a ff ff fe", new JumpToAddress(0x00000000, false, false),
				"0:           0a ff ff fe                     beq		0x00000000", armOptions);
		disassembleInst(0x00000000, "ea ff ff fe", new JumpToAddress(0x00000000, true, false),
				"0:           ea ff ff fe                     b		0x00000000", armOptions);
		disassembleInst(0x00000000, "eb ff ff fe", new JumpToAddress(0x00000000, true, true),
				"0:           eb ff ff fe                     bl	0x00000000", armOptions);
		disassembleInst(0x00000000, "fa ff ff fe", new JumpToAddress(0x00000000, true, true),
				"0:           fa ff ff fe                     blx	0x00000000", armOptions);
		disassembleInst(0x00000000, "e1 2f ff 30", new JumpToAddress("r0", true, true),
				"0:           e1 2f ff 30                     blx	r0", armOptions);
		disassembleInst(0x00000000, "e1 2f ff 10", new JumpToAddress("r0", true, false),
				"0:           e1 2f ff 10                     bx	r0", armOptions);
		disassembleInst(0x00000000, "e1 a0 f0 0e", new JumpToAddress("lr", true, false),
				"0:           e1 a0 f0 0e                     mov	pc,lr", armOptions);
	}

	/**
	 * Test if ARM instruction parser raises CodeBufferUnderflow
	 */
	@Test
	public void testArmBufferUnderflow() {
		System.out.println("\n============= ARM CodeBufferUnderflow ===============\n");
		catchCodeBufferUnderflowException(0x0, "ea ff", armOptions);
	}

	/**
	 * Test for Thumb instructions.
	 */
	@Test
	public void testThumbInstructions() {

		System.out.println("\n======================= Thumb =======================\n");
		String[] insts = {
				"41 75", "adcs	r5,r6",
				"44 35", "add	r5,r6",
				"AD 1E", "add	r5,sp,#0x78",
				"B0 0E", "add	sp,sp,#0x38",
				"35 87", "adds	r5,#0x87",
				"1D F5", "adds	r5,r6,#7",
				"18 B5", "adds	r5,r6,r2",
				"A5 E1", "add	r5,pc,#0x384",
				"40 35", "ands	r5,r6",
				"41 35", "asrs	r5,r6",
				"17 F5", "asrs	r5,r6,#0x1f",
				"D1 FA", "bne	0xfffffff8",
				"E7 F9", "b	0xfffffff6",
				"43 B5", "bics	r5,r6",
				"BE 87", "bkpt	#0x87",
				"47 A8", "blx	r5",
				"47 28", "bx	r5",
				"B1 D5", "cbz	r5,0x00000034",
				"B9 CD", "cbnz	r5,0x00000032",
				"42 F5", "cmn	r5,r6",
				"2D 87", "cmp	r5,#0x87",
				"42 B5", "cmp	r5,r6",
				"42 B5", "cmp	r5,r6",
				"45 B1", "cmp	r9,r6",
				"B6 66", "cpsie	ai",
				"B6 73", "cpsid	if",
				"40 75", "eors	r5,r6",
				"BF 18", "it	ne",
				"BF 1C", "itt	ne",
				"BF 14", "ite	ne",
				"BF 1E", "ittt	ne",
				"BF 1A", "itte	ne",
				"BF 16", "itet	ne",
				"BF 12", "itee	ne",
				"BF 1F", "itttt	ne",
				"BF 1D", "ittte	ne",
				"BF 1B", "ittet	ne",
				"BF 19", "ittee	ne",
				"BF 17", "itett	ne",
				"BF 15", "itete	ne",
				"BF 13", "iteet	ne",
				"BF 11", "iteee	ne",
				"CD 81", "ldm	r5!,{r0,r7}",
				"CD A1", "ldm	r5,{r0,r5,r7}",
				"68 35", "ldr	r5,[r6]",
				"69 B5", "ldr	r5,[r6,#0x18]",
				"58 B5", "ldr	r5,[r6,r2]",
				"9D 00", "ldr	r5,[sp]",
				"9D 06", "ldr	r5,[sp,#0x18]",
				"4D 03", "ldr	r5,[pc,#0xc] ; 0xc",
				"5C B5", "ldrb	r5,[r6,r2]",
				"78 35", "ldrb	r5,[r6]",
				"7F F5", "ldrb	r5,[r6,#0x1f]",
				"5A B5", "ldrh	r5,[r6,r2]",
				"88 35", "ldrh	r5,[r6]",
				"8F 35", "ldrh	r5,[r6,#0x38]",
				"56 B5", "ldrsb	r5,[r6,r2]",
				"5E B5", "ldrsh	r5,[r6,r2]",
				"40 B5", "lsls	r5,r6",
				"06 F5", "lsls	r5,r6,#27",
				"40 F5", "lsrs	r5,r6",
				"0E F5", "lsrs	r5,r6,#27",
				"46 35", "mov	r5,r6",
				"25 87", "movs	r5,#0x87",
				"00 35", "movs	r5,r6",
				"43 7D", "muls	r5,r7,r5",
				"43 F5", "mvns	r5,r6",
				"BF 00", "nop",
				"43 35", "orrs	r5,r6",
				"BD 81", "pop	{r0,r7,pc}",
				"B4 81", "push	{r0,r7}",
				"BA 35", "rev	r5,r6",
				"BA 75", "rev16	r5,r6",
				"BA F5", "revsh	r5,r6",
				"41 F5", "rors	r5,r6",
				"42 75", "rsbs	r5,r6,#0",
				"41 B5", "sbcs	r5,r6",
				"B6 58", "setend	be",
				"B6 50", "setend	le",
				"BF 40", "sev",
				"C5 81", "stm	r5!,{r0,r7}",
				"60 35", "str	r5,[r6]",
				"67 B5", "str	r5,[r6,#0x78]",
				"50 B5", "str	r5,[r6,r2]",
				"95 60", "str	r5,[sp,#0x180]",
				"75 F5", "strb	r5,[r6,#0x17]",
				"54 B5", "strb	r5,[r6,r2]",
				"52 B5", "strh	r5,[r6,r2]",
				"80 35", "strh	r5,[r6]",
				"87 75", "strh	r5,[r6,#0x3a]",
				"B0 AE", "sub	sp,sp,#0xb8",
				"3D 87", "subs	r5,#0x87",
				"1F F5", "subs	r5,r6,#7",
				"1A B5", "subs	r5,r6,r2",
				"DF 87", "svc	#0x87",
				"B2 75", "sxtb	r5,r6",
				"B2 35", "sxth	r5,r6",
				"42 35", "tst	r5,r6",
				"B2 F5", "uxtb	r5,r6",
				"B2 B5", "uxth	r5,r6",
				"BF 20", "wfe",
				"BF 30", "wfi",
				"BF 10", "yield",
				"DE 80", "undefined",
		};

		disassembleInstArray(insts, thumbOptions);
	}

	/**
	 * Test for Thumb branching instructions.
	 */
	@Test
	public void testThumbBranches() {

		thumbOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_ADDRESS, true);
		thumbOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_BYTES, true);
		System.out.println("\n=================== Thumb Branches ==================\n");
		disassembleInst(0x00000000, "d0 fe", new JumpToAddress(0x00000000, false, false),
				"0:           d0 fe                           beq	0x00000000", thumbOptions);
		disassembleInst(0x00000000, "e7 fe", new JumpToAddress(0x00000000, true, false),
				"0:           e7 fe                           b		0x00000000", thumbOptions);
//		disassembleInst(0x00000000, "f7 ff ff fe", new JumpToAddress(0x00000000, true, true),
//				"0:           f7 ff ff fe                     bl	0x00000000", thumbOptions);
//		disassembleInst(0x00000000, "f7 ff ef fe", new JumpToAddress(0x00000000, true, true),
//				"0:           f7 ff ef fe                     blx	0x0000000000000000", thumbOptions);
		disassembleInst(0x00000000, "47 80", new JumpToAddress("r0", true, true),
				"0:           47 80                           blx	r0", thumbOptions);
		disassembleInst(0x00000000, "46 f7", new JumpToAddress("lr", true, false),
				"0:           46 f7                           mov	pc,lr", thumbOptions);
	}

	/**
	 * Test if thumb instruction parser raises CodeBufferUnderflow
	 */
	@Test
	public void testThumbBufferUnderflow() {
		System.out.println("\n============ Thumb CodeBufferUnderflow ==============\n");
		catchCodeBufferUnderflowException(0x0, "f7", thumbOptions);
	}


	/**
	 */
	@Test
	public void test32BitArmV6KInstructions() {

		System.out.println("\n================== ARMv6K Instructions ==================\n");

		String[] insts = {
				"F5 7F F0 1F", "clrex",
				"E3 20 F0 FD", "nop",	// dbg	#13
				"03 20 F0 FD", "nopeq",	// dbgeq	#13
				"E3 20 F0 00", "nop",	// nop
				"03 20 F0 00", "nopeq",	// nopeq
				"E3 20 F0 04", "sev",
				"03 20 F0 04", "seveq",
				"E3 20 F0 02", "wfe",
				"03 20 F0 02", "wfeeq",
				"E3 20 F0 03", "wfi",
				"03 20 F0 03", "wfieq",
				"E3 20 F0 01", "yield",
				"03 20 F0 01", "yieldeq",
		};

		Map<String, Object> options = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : armOptions.entrySet())
			options.put(entry.getKey(), entry.getValue());
		options.put(DisassemblerARM.IDisassemblerOptionsARM.VERSION_MODE, InstructionParserARM.ARMv6K);
		disassembleInstArray(insts, options);
	}

	/**
	 */
	@Test
	public void test32BitArmV6T2Instructions() {
		System.out.println("\n================== ARMv6T2 Hint Instructions ==================\n");

		String[] insts = {
				"F5 7F F0 1F", "invalid opcode",	// clrex
				"E3 20 F0 FD", "nop",               // dbg	#13
				"03 20 F0 FD", "nopeq",             // dbgeq	#13
				"E3 20 F0 00", "nop",               // nop
				"03 20 F0 00", "nopeq",             // nopeq
				"E3 20 F0 04", "nop",               // sev
				"03 20 F0 04", "nopeq",             // seveq
				"E3 20 F0 02", "nop",               // wfe
				"03 20 F0 02", "nopeq",             // wfeeq
				"E3 20 F0 03", "nop",               // wfi
				"03 20 F0 03", "nopeq",             // wfieq
				"E3 20 F0 01", "nop",               // yield
				"03 20 F0 01", "nopeq",             // yieldeq
		};

		Map<String, Object> options = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : armOptions.entrySet())
			options.put(entry.getKey(), entry.getValue());
		options.put(DisassemblerARM.IDisassemblerOptionsARM.VERSION_MODE, InstructionParserARM.ARMv6T2);
		disassembleInstArray(insts, options);
	}

	/**
	 */
	@Test
	public void test32BitArmV5Instructions() {

		System.out.println("\n================== ARMv5 Instructions (Invalid) ==================\n");

		String[] insts = {
				"F5 7F F0 1F", "invalid opcode",	// clrex
				"E3 20 F0 FD", "invalid opcode",    // dbg	#13
				"03 20 F0 FD", "invalid opcode",    // dbgeq	#13
				"E3 20 F0 00", "invalid opcode",    // nop
				"03 20 F0 00", "invalid opcode",    // nopeq
				"E3 20 F0 04", "invalid opcode",    // sev
				"03 20 F0 04", "invalid opcode",    // seveq
				"E3 20 F0 02", "invalid opcode",    // wfe
				"03 20 F0 02", "invalid opcode",    // wfeeq
				"E3 20 F0 03", "invalid opcode",    // wfi
				"03 20 F0 03", "invalid opcode",    // wfieq
				"E3 20 F0 01", "invalid opcode",    // yield
				"03 20 F0 01", "invalid opcode",    // yieldeq
		};

		Map<String, Object> options = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : armOptions.entrySet())
			options.put(entry.getKey(), entry.getValue());
		options.put(DisassemblerARM.IDisassemblerOptionsARM.VERSION_MODE, InstructionParserARM.ARMv5);
		disassembleInstArray(insts, options);
	}

	/**
	 * Test for non-VFP, 32-bit THumb2 v6*, v7  instructions.
	 */
	@Test
	public void test32BitThumb2Instructions() {

		System.out.println("\n===================== Thumb2 ========================\n");
		String[] insts = {
///			"E7 F1 23 F4", "undefined",
///			"E7 F0 00 10", "undefined",
			"F1 4A 05 71", "adc	r5,r10,#0x71",						// 1111 0x01 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 4B 06 F7", "adc	r6,r11,#0xf7",						// 1111 0x01 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 49 14 78", "adc	r4,r9,#0x780078",					// 1111 0x01 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 48 13 FC", "adc	r3,r8,#0xfc00fc",					// 1111 0x01 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 47 25 64", "adc	r5,r7,#0x64006400",					// 1111 0x01 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 46 25 E3", "adc	r5,r6,#0xe300e300",					// 1111 0x01 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 47 46 60", "adc	r6,r7,#0xe0000000",					// 1111 0x01 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 48 47 E0", "adc	r7,r8,#0x70000000",					// 1111 0x01 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F5 4A 05 60", "adc	r5,r10,#0xe00000",					// 1111 0x01 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F5 4A 45 60", "adc	r5,r10,#0xe000",					// 1111 0x01 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F5 4A 65 60", "adc	r5,r10,#0xe00",						// 1111 0x01 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"EB 49 05 0A", "adc.w	r5,r9,r10",						// 1110 1011 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.2	T2
			"EB 48 14 A9", "adc.w	r4,r8,r9,asr #6",				// 1110 1011 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.2	T2
			"EB 47 03 48", "adc.w	r3,r7,r8,lsl #1",				// 1110 1011 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.2	T2
			"EB 46 02 17", "adc.w	r2,r6,r7,lsr #32",				// 1110 1011 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.2	T2
			"EB 49 75 F8", "adc.w	r5,r9,r8,ror #31",				// 1110 1011 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.2	T2
			"EB 48 05 39", "adc.w	r5,r8,r9,rrx",					// 1110 1011 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.2	T2
			"F1 5A 05 71", "adcs	r5,r10,#0x71",					// 1111 0x01 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 5B 06 F7", "adcs	r6,r11,#0xf7",					// 1111 0x01 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 59 14 78", "adcs	r4,r9,#0x780078",				// 1111 0x01 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 58 13 FC", "adcs	r3,r8,#0xfc00fc",				// 1111 0x01 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 57 25 64", "adcs	r5,r7,#0x64006400",				// 1111 0x01 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 56 25 E3", "adcs	r5,r6,#0xe300e300",				// 1111 0x01 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 57 46 60", "adcs	r6,r7,#0xe0000000",				// 1111 0x01 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F1 58 47 E0", "adcs	r7,r8,#0x70000000",				// 1111 0x01 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F5 5A 05 60", "adcs	r5,r10,#0xe00000",				// 1111 0x01 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F5 5A 45 60", "adcs	r5,r10,#0xe000",				// 1111 0x01 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"F5 5A 65 60", "adcs	r5,r10,#0xe00",					// 1111 0x01 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.1	T1
			"EB 59 05 0A", "adcs.w	r5,r9,r10",						// 1110 1011 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.2	T2
			"EB 58 14 A9", "adcs.w	r4,r8,r9,asr #6",				// 1110 1011 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.2	T2
			"EB 57 03 48", "adcs.w	r3,r7,r8,lsl #1",				// 1110 1011 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.2	T2
			"EB 56 02 17", "adcs.w	r2,r6,r7,lsr #32",				// 1110 1011 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.2	T2
			"EB 59 75 F8", "adcs.w	r5,r9,r8,ror #31",				// 1110 1011 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.2	T2
			"EB 58 05 39", "adcs.w	r5,r8,r9,rrx",					// 1110 1011 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.2	T2
			"F2 0F 05 71", "add	r5,pc,#0x71",						// 1111 0x10 0000 1111 0xxx xxxx xxxx xxxx	// A8.6.10	T3
			"F2 0F 36 72", "add	r6,pc,#0x372",						// 1111 0x10 0000 1111 0xxx xxxx xxxx xxxx	// A8.6.10	T3
			"F6 0F 47 78", "add	r7,pc,#0xc78",						// 1111 0x10 0000 1111 0xxx xxxx xxxx xxxx	// A8.6.10	T3
			"F1 0A 05 71", "add.w	r5,r10,#0x71",					// 1111 0x01 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 0B 06 F7", "add.w	r6,r11,#0xf7",					// 1111 0x01 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 09 14 78", "add.w	r4,r9,#0x780078",				// 1111 0x01 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 08 13 FC", "add.w	r3,r8,#0xfc00fc",				// 1111 0x01 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 07 25 64", "add.w	r5,r7,#0x64006400",				// 1111 0x01 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 06 25 E3", "add.w	r5,r6,#0xe300e300",				// 1111 0x01 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 07 46 60", "add.w	r6,r7,#0xe0000000",				// 1111 0x01 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 08 47 E0", "add.w	r7,r8,#0x70000000",				// 1111 0x01 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F5 0A 05 60", "add.w	r5,r10,#0xe00000",				// 1111 0x01 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F5 0A 45 60", "add.w	r5,r10,#0xe000",				// 1111 0x01 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F5 0A 65 60", "add.w	r5,r10,#0xe00",					// 1111 0x01 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"EB 09 05 0A", "add.w	r5,r9,r10",						// 1110 1011 0000 xxxx .xxx xxxx xxxx xxxx	// A8.6.6	T3
			"EB 08 14 A9", "add.w	r4,r8,r9,asr #6",				// 1110 1011 0000 xxxx .xxx xxxx xxxx xxxx	// A8.6.6	T3
			"EB 07 03 48", "add.w	r3,r7,r8,lsl #1",				// 1110 1011 0000 xxxx .xxx xxxx xxxx xxxx	// A8.6.6	T3
			"EB 06 02 17", "add.w	r2,r6,r7,lsr #32",				// 1110 1011 0000 xxxx .xxx xxxx xxxx xxxx	// A8.6.6	T3
			"EB 09 75 F8", "add.w	r5,r9,r8,ror #31",				// 1110 1011 0000 xxxx .xxx xxxx xxxx xxxx	// A8.6.6	T3
			"EB 08 05 39", "add.w	r5,r8,r9,rrx",					// 1110 1011 0000 xxxx .xxx xxxx xxxx xxxx	// A8.6.6	T3
			"F1 0D 05 71", "add.w	r5,sp,#0x71",					// 1111 0x01 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 0D 06 F7", "add.w	r6,sp,#0xf7",					// 1111 0x01 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 0D 14 78", "add.w	r4,sp,#0x780078",				// 1111 0x01 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 0D 13 FC", "add.w	r3,sp,#0xfc00fc",				// 1111 0x01 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 0D 25 64", "add.w	r5,sp,#0x64006400",				// 1111 0x01 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 0D 25 E3", "add.w	r5,sp,#0xe300e300",				// 1111 0x01 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 0D 46 60", "add.w	r6,sp,#0xe0000000",				// 1111 0x01 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 0D 47 E0", "add.w	r7,sp,#0x70000000",				// 1111 0x01 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F5 0D 05 60", "add.w	r5,sp,#0xe00000",				// 1111 0x01 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F5 0D 45 60", "add.w	r5,sp,#0xe000",					// 1111 0x01 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F5 0D 65 60", "add.w	r5,sp,#0xe00",					// 1111 0x01 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"EB 0D 05 0A", "add.w	r5,sp,r10",						// 1110 1011 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.9	T3
			"EB 0D 14 A9", "add.w	r4,sp,r9,asr #6",				// 1110 1011 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.9	T3
			"EB 0D 03 48", "add.w	r3,sp,r8,lsl #1",				// 1110 1011 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.9	T3
			"EB 0D 02 17", "add.w	r2,sp,r7,lsr #32",				// 1110 1011 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.9	T3
			"EB 0D 75 F8", "add.w	r5,sp,r8,ror #31",				// 1110 1011 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.9	T3
			"EB 0D 05 39", "add.w	r5,sp,r9,rrx",					// 1110 1011 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.9	T3
			"F1 1A 05 71", "adds.w	r5,r10,#0x71",					// 1111 0x01 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 1B 06 F7", "adds.w	r6,r11,#0xf7",					// 1111 0x01 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 19 14 78", "adds.w	r4,r9,#0x780078",				// 1111 0x01 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 18 13 FC", "adds.w	r3,r8,#0xfc00fc",				// 1111 0x01 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 17 25 64", "adds.w	r5,r7,#0x64006400",				// 1111 0x01 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 16 25 E3", "adds.w	r5,r6,#0xe300e300",				// 1111 0x01 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 17 46 60", "adds.w	r6,r7,#0xe0000000",				// 1111 0x01 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F1 18 47 E0", "adds.w	r7,r8,#0x70000000",				// 1111 0x01 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F5 1A 05 60", "adds.w	r5,r10,#0xe00000",				// 1111 0x01 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F5 1A 45 60", "adds.w	r5,r10,#0xe000",				// 1111 0x01 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"F5 1A 65 60", "adds.w	r5,r10,#0xe00",					// 1111 0x01 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.4	T3
			"EB 19 05 0A", "adds.w	r5,r9,r10",						// 1110 1011 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.6	T3
			"EB 18 14 A9", "adds.w	r4,r8,r9,asr #6",				// 1110 1011 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.6	T3
			"EB 17 03 48", "adds.w	r3,r7,r8,lsl #1",				// 1110 1011 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.6	T3
			"EB 16 02 17", "adds.w	r2,r6,r7,lsr #32",				// 1110 1011 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.6	T3
			"EB 19 75 F8", "adds.w	r5,r9,r8,ror #31",				// 1110 1011 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.6	T3
			"EB 18 05 39", "adds.w	r5,r8,r9,rrx",					// 1110 1011 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.6	T3
			"F1 1D 05 71", "adds.w	r5,sp,#0x71",					// 1111 0x01 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 1D 06 F7", "adds.w	r6,sp,#0xf7",					// 1111 0x01 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 1D 14 78", "adds.w	r4,sp,#0x780078",				// 1111 0x01 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 1D 13 FC", "adds.w	r3,sp,#0xfc00fc",				// 1111 0x01 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 1D 25 64", "adds.w	r5,sp,#0x64006400",				// 1111 0x01 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 1D 25 E3", "adds.w	r5,sp,#0xe300e300",				// 1111 0x01 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 1D 46 60", "adds.w	r6,sp,#0xe0000000",				// 1111 0x01 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F1 1D 47 E0", "adds.w	r7,sp,#0x70000000",				// 1111 0x01 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F5 1D 05 60", "adds.w	r5,sp,#0xe00000",				// 1111 0x01 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F5 1D 45 60", "adds.w	r5,sp,#0xe000",					// 1111 0x01 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"F5 1D 65 60", "adds.w	r5,sp,#0xe00",					// 1111 0x01 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T3
			"EB 1D 05 0A", "adds.w	r5,sp,r10",						// 1110 1011 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.9	T3
			"EB 1D 14 A9", "adds.w	r4,sp,r9,asr #6",				// 1110 1011 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.9	T3
			"EB 1D 03 48", "adds.w	r3,sp,r8,lsl #1",				// 1110 1011 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.9	T3
			"EB 1D 02 17", "adds.w	r2,sp,r7,lsr #32",				// 1110 1011 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.9	T3
			"EB 1D 75 F8", "adds.w	r5,sp,r8,ror #31",				// 1110 1011 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.9	T3
			"EB 1D 05 39", "adds.w	r5,sp,r9,rrx",					// 1110 1011 0001 1101 0xxx xxxx xxxx xxxx	// A8.6.9	T3
			"F2 0D 05 71", "addw	r5,sp,#0x71",					// 1111 0x10 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T4
			"F2 0D 36 72", "addw	r6,sp,#0x372",					// 1111 0x10 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T4
			"F6 0D 47 78", "addw	r7,sp,#0xc78",					// 1111 0x10 0000 1101 0xxx xxxx xxxx xxxx	// A8.6.8	T4
			"F0 0A 05 71", "and	r5,r10,#0x71",						// 1111 0x00 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 0B 06 F7", "and	r6,r11,#0xf7",						// 1111 0x00 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 09 14 78", "and	r4,r9,#0x780078",					// 1111 0x00 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 08 13 FC", "and	r3,r8,#0xfc00fc",					// 1111 0x00 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 07 25 64", "and	r5,r7,#0x64006400",					// 1111 0x00 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 06 25 E3", "and	r5,r6,#0xe300e300",					// 1111 0x00 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 07 46 60", "and	r6,r7,#0xe0000000",					// 1111 0x00 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 08 47 E0", "and	r7,r8,#0x70000000",					// 1111 0x00 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F4 0A 05 60", "and	r5,r10,#0xe00000",					// 1111 0x00 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F4 0A 45 60", "and	r5,r10,#0xe000",					// 1111 0x00 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F4 0A 65 60", "and	r5,r10,#0xe00",						// 1111 0x00 0000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"EA 09 05 0A", "and.w	r5,r9,r10",						// 1110 1010 0000 xxxx .xxx xxxx xxxx xxxx	// A8.6.12	T2
			"EA 08 14 A9", "and.w	r4,r8,r9,asr #6",				// 1110 1010 0000 xxxx .xxx xxxx xxxx xxxx	// A8.6.12	T2
			"EA 07 03 48", "and.w	r3,r7,r8,lsl #1",				// 1110 1010 0000 xxxx .xxx xxxx xxxx xxxx	// A8.6.12	T2
			"EA 06 02 17", "and.w	r2,r6,r7,lsr #32",				// 1110 1010 0000 xxxx .xxx xxxx xxxx xxxx	// A8.6.12	T2
			"EA 09 75 F8", "and.w	r5,r9,r8,ror #31",				// 1110 1010 0000 xxxx .xxx xxxx xxxx xxxx	// A8.6.12	T2
			"EA 08 05 39", "and.w	r5,r8,r9,rrx",					// 1110 1010 0000 xxxx .xxx xxxx xxxx xxxx	// A8.6.12	T2
			"F0 1A 05 71", "ands	r5,r10,#0x71",					// 1111 0x00 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 1B 06 F7", "ands	r6,r11,#0xf7",					// 1111 0x00 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 19 14 78", "ands	r4,r9,#0x780078",				// 1111 0x00 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 18 13 FC", "ands	r3,r8,#0xfc00fc",				// 1111 0x00 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 17 25 64", "ands	r5,r7,#0x64006400",				// 1111 0x00 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 16 25 E3", "ands	r5,r6,#0xe300e300",				// 1111 0x00 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 17 46 60", "ands	r6,r7,#0xe0000000",				// 1111 0x00 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F0 18 47 E0", "ands	r7,r8,#0x70000000",				// 1111 0x00 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F4 1A 05 60", "ands	r5,r10,#0xe00000",				// 1111 0x00 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F4 1A 45 60", "ands	r5,r10,#0xe000",				// 1111 0x00 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"F4 1A 65 60", "ands	r5,r10,#0xe00",					// 1111 0x00 0001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.11	T1
			"EA 19 05 0A", "ands.w	r5,r9,r10",						// 1110 1010 0001 xxxx .xxx xxxx xxxx xxxx	// A8.6.12	T2
			"EA 18 14 A9", "ands.w	r4,r8,r9,asr #6",				// 1110 1010 0001 xxxx .xxx xxxx xxxx xxxx	// A8.6.12	T2
			"EA 17 03 48", "ands.w	r3,r7,r8,lsl #1",				// 1110 1010 0001 xxxx .xxx xxxx xxxx xxxx	// A8.6.12	T2
			"EA 16 02 17", "ands.w	r2,r6,r7,lsr #32",				// 1110 1010 0001 xxxx .xxx xxxx xxxx xxxx	// A8.6.12	T2
			"EA 19 75 F8", "ands.w	r5,r9,r8,ror #31",				// 1110 1010 0001 xxxx .xxx xxxx xxxx xxxx	// A8.6.12	T2
			"EA 18 05 39", "ands.w	r5,r8,r9,rrx",					// 1110 1010 0001 xxxx .xxx xxxx xxxx xxxx	// A8.6.12	T2
			"EA 4F 45 69", "asr.w	r5,r9,#17",						// 1110 1010 0100 1111 .xxx xxxx xx10 xxxx	// A8.6.14	T2
			"EA 4F 06 28", "asr.w	r6,r8,#32",						// 1110 1010 0100 1111 .xxx xxxx xx10 xxxx	// A8.6.14	T2
			"FA 49 F5 0A", "asr.w	r5,r9,r10",						// 1111 1010 0100 xxxx 1111 xxxx 0000 xxxx	// A8.6.15	T2
			"EA 5F 45 69", "asrs.w	r5,r9,#17",						// 1110 1010 0101 1111 .xxx xxxx xx10 xxxx	// A8.6.14	T2
			"EA 5F 06 28", "asrs.w	r6,r8,#32",						// 1110 1010 0101 1111 .xxx xxxx xx10 xxxx	// A8.6.14	T2
			"FA 59 F5 0A", "asrs.w	r5,r9,r10",						// 1111 1010 0101 xxxx 1111 xxxx 0000 xxxx	// A8.6.15	T2
			"F7 FF BF FE", "b.w	0x003ffffc",						// 1111 0xxx xxxx xxxx 10x1 xxxx xxxx xxxx	// A8.6.16	T4
			"F4 3F AF FE", "beq.w	0x000ffffc",					// 1111 0xxx xxxx xxxx 10x0 xxxx xxxx xxxx	// A8.6.16	T3
			"F3 6F 05 1F", "bfc	r5,#0,#32",							// 1111 0011 0110 1111 0xxx xxxx xx.x xxxx	// A8.6.17	T1
			"F3 6F 06 59", "bfc	r6,#1,#25",							// 1111 0011 0110 1111 0xxx xxxx xx.x xxxx	// A8.6.17	T1
			"F3 6F 77 DF", "bfc	r7,#31,#1",							// 1111 0011 0110 1111 0xxx xxxx xx.x xxxx	// A8.6.17	T1
			"F3 67 05 1F", "bfi	r5,r7,#0,#32",						// 1111 0011 0110 xxxx 0xxx xxxx xx.x xxxx	// A8.6.18	T1
			"F3 68 06 59", "bfi	r6,r8,#1,#25",						// 1111 0011 0110 xxxx 0xxx xxxx xx.x xxxx	// A8.6.18	T1
			"F3 69 77 DF", "bfi	r7,r9,#31,#1",						// 1111 0011 0110 xxxx 0xxx xxxx xx.x xxxx	// A8.6.18	T1
			"F0 2A 05 71", "bic	r5,r10,#0x71",						// 1111 0x00 0010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 2B 06 F7", "bic	r6,r11,#0xf7",						// 1111 0x00 0010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 29 14 78", "bic	r4,r9,#0x780078",					// 1111 0x00 0010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 28 13 FC", "bic	r3,r8,#0xfc00fc",					// 1111 0x00 0010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 27 25 64", "bic	r5,r7,#0x64006400",					// 1111 0x00 0010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 26 25 E3", "bic	r5,r6,#0xe300e300",					// 1111 0x00 0010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 27 46 60", "bic	r6,r7,#0xe0000000",					// 1111 0x00 0010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 28 47 E0", "bic	r7,r8,#0x70000000",					// 1111 0x00 0010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F4 2A 05 60", "bic	r5,r10,#0xe00000",					// 1111 0x00 0010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F4 2A 45 60", "bic	r5,r10,#0xe000",					// 1111 0x00 0010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F4 2A 65 60", "bic	r5,r10,#0xe00",						// 1111 0x00 0010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"EA 29 05 0A", "bic.w	r5,r9,r10",						// 1110 1010 001x xxxx 0xxx xxxx xxxx xxxx	// A8.6.20	T2
			"EA 28 14 A9", "bic.w	r4,r8,r9,asr #6",				// 1110 1010 001x xxxx 0xxx xxxx xxxx xxxx	// A8.6.20	T2
			"EA 27 03 48", "bic.w	r3,r7,r8,lsl #1",				// 1110 1010 001x xxxx 0xxx xxxx xxxx xxxx	// A8.6.20	T2
			"EA 26 02 17", "bic.w	r2,r6,r7,lsr #32",				// 1110 1010 001x xxxx 0xxx xxxx xxxx xxxx	// A8.6.20	T2
			"EA 29 75 F8", "bic.w	r5,r9,r8,ror #31",				// 1110 1010 001x xxxx 0xxx xxxx xxxx xxxx	// A8.6.20	T2
			"EA 28 05 39", "bic.w	r5,r8,r9,rrx",					// 1110 1010 001x xxxx 0xxx xxxx xxxx xxxx	// A8.6.20	T2
			"F0 3A 05 71", "bics	r5,r10,#0x71",					// 1111 0x00 0011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 3B 06 F7", "bics	r6,r11,#0xf7",					// 1111 0x00 0011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 39 14 78", "bics	r4,r9,#0x780078",				// 1111 0x00 0011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 38 13 FC", "bics	r3,r8,#0xfc00fc",				// 1111 0x00 0011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 37 25 64", "bics	r5,r7,#0x64006400",				// 1111 0x00 0011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 36 25 E3", "bics	r5,r6,#0xe300e300",				// 1111 0x00 0011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 37 46 60", "bics	r6,r7,#0xe0000000",				// 1111 0x00 0011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F0 38 47 E0", "bics	r7,r8,#0x70000000",				// 1111 0x00 0011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F4 3A 05 60", "bics	r5,r10,#0xe00000",				// 1111 0x00 0011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F4 3A 45 60", "bics	r5,r10,#0xe000",				// 1111 0x00 0011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"F4 3A 65 60", "bics	r5,r10,#0xe00",					// 1111 0x00 0011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.19	T1
			"EA 39 05 0A", "bics.w	r5,r9,r10",						// 1110 1010 0011 xxxx .xxx xxxx xxxx xxxx	// A8.6.20	T2
			"EA 38 14 A9", "bics.w	r4,r8,r9,asr #6",				// 1110 1010 0011 xxxx .xxx xxxx xxxx xxxx	// A8.6.20	T2
			"EA 37 03 48", "bics.w	r3,r7,r8,lsl #1",				// 1110 1010 0011 xxxx .xxx xxxx xxxx xxxx	// A8.6.20	T2
			"EA 36 02 17", "bics.w	r2,r6,r7,lsr #32",				// 1110 1010 0011 xxxx .xxx xxxx xxxx xxxx	// A8.6.20	T2
			"EA 39 75 F8", "bics.w	r5,r9,r8,ror #31",				// 1110 1010 0011 xxxx .xxx xxxx xxxx xxxx	// A8.6.20	T2
			"EA 38 05 39", "bics.w	r5,r8,r9,rrx",					// 1110 1010 0011 xxxx .xxx xxxx xxxx xxxx	// A8.6.20	T2
			"F7 FF FF FE", "bl	0x003ffffc",						// 1111 0xxx xxxx xxxx 11x1 xxxx xxxx xxxx	// A8.6.23	T1
			"F7 FF EF FE", "blx	0x003ffffc",						// 1111 0xxx xxxx xxxx 11x0 xxxx xxxx xxxx	// A8.6.23	T2
			"F4 00 C0 3C", "blx	0x00c00078",						// 1111 0xxx xxxx xxxx 11x0 xxxx xxxx xxxx	// A8.6.23	T2
			"F3 C9 AF 00", "bxj	r9",								// 1111 0011 1100 xxxx 10.0 :::: .... ....	// A8.6.26	T1
			"EE C9 59 EA", "cdp	p9,0xc,c5,c9,c10,0x7",				// 1110 1110 xxxx xxxx xxxx xxxx xxx0 xxxx	// A8.6.28	T1/A1
			"FE 19 57 6A", "cdp2	p7,0x1,c5,c9,c10,0x3",			// 1111 1110 xxxx xxxx xxxx xxxx xxx0 xxxx	// A8.6.28	T2/A2
			"F3 BF 8F 2F", "clrex",									// 1111 0011 1011 :::: 10.0 :::: 0010 ::::	// A8.6.30	T1
			"FA B9 F5 89", "clz	r5,r9",								// 1111 1010 1011 xxxx 1111 xxxx 1000 xxxx	// A8.6.31	T1
			"F1 15 0F 71", "cmn	r5,#0x71",							// 1111 0x01 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.32	T1
			"F1 16 0F F7", "cmn	r6,#0xf7",							// 1111 0x01 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.32	T1
			"F1 14 1F 78", "cmn	r4,#0x780078",						// 1111 0x01 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.32	T1
			"F1 13 1F FC", "cmn	r3,#0xfc00fc",						// 1111 0x01 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.32	T1
			"F1 15 2F 64", "cmn	r5,#0x64006400",					// 1111 0x01 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.32	T1
			"F1 15 2F E3", "cmn	r5,#0xe300e300",					// 1111 0x01 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.32	T1
			"F1 16 4F 60", "cmn	r6,#0xe0000000",					// 1111 0x01 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.32	T1
			"F1 17 4F E0", "cmn	r7,#0x70000000",					// 1111 0x01 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.32	T1
			"F5 15 0F 60", "cmn	r5,#0xe00000",						// 1111 0x01 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.32	T1
			"F5 15 4F 60", "cmn	r5,#0xe000",						// 1111 0x01 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.32	T1
			"F5 15 6F 60", "cmn	r5,#0xe00",							// 1111 0x01 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.32	T1
			"EB 15 0F 09", "cmn.w	r5,r9",							// 1110 1011 0001 xxxx .xxx 1111 xxxx xxxx	// A8.6.33	T2
			"EB 14 1F A8", "cmn.w	r4,r8,asr #6",					// 1110 1011 0001 xxxx .xxx 1111 xxxx xxxx	// A8.6.33	T2
			"EB 13 0F 47", "cmn.w	r3,r7,lsl #1",					// 1110 1011 0001 xxxx .xxx 1111 xxxx xxxx	// A8.6.33	T2
			"EB 12 0F 16", "cmn.w	r2,r6,lsr #32",					// 1110 1011 0001 xxxx .xxx 1111 xxxx xxxx	// A8.6.33	T2
			"EB 15 7F F9", "cmn.w	r5,r9,ror #31",					// 1110 1011 0001 xxxx .xxx 1111 xxxx xxxx	// A8.6.33	T2
			"EB 15 0F 38", "cmn.w	r5,r8,rrx",						// 1110 1011 0001 xxxx .xxx 1111 xxxx xxxx	// A8.6.33	T2
			"F1 B5 0F 71", "cmp.w	r5,#0x71",						// 1111 1x11 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.35	T2
			"F1 B6 0F F7", "cmp.w	r6,#0xf7",						// 1111 1x11 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.35	T2
			"F1 B4 1F 78", "cmp.w	r4,#0x780078",	    			// 1111 1x11 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.35	T2
			"F1 B3 1F FC", "cmp.w	r3,#0xfc00fc",	    			// 1111 1x11 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.35	T2
			"F1 B5 2F 64", "cmp.w	r5,#0x64006400",    			// 1111 1x11 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.35	T2
			"F1 B5 2F E3", "cmp.w	r5,#0xe300e300",    			// 1111 1x11 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.35	T2
            "F1 B6 4F 60", "cmp.w	r6,#0xe0000000",    			// 1111 1x11 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.35	T2
			"F1 B7 4F E0", "cmp.w	r7,#0x70000000",    			// 1111 1x11 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.35	T2
            "F5 B5 0F 60", "cmp.w	r5,#0xe00000",	    			// 1111 1x11 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.35	T2
            "F5 B5 4F 60", "cmp.w	r5,#0xe000",	    			// 1111 1x11 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.35	T2
			"F5 B5 6F 60", "cmp.w	r5,#0xe00",		    			// 1111 1x11 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.35	T2
			"EB B5 0F 09", "cmp.w	r5,r9",							// 1110 1011 1011 xxxx .xxx 1111 xxxx xxxx	// A8.6.36	T3
			"EB B4 1F A8", "cmp.w	r4,r8,asr #6",					// 1110 1011 1011 xxxx .xxx 1111 xxxx xxxx	// A8.6.36	T3
			"EB B3 0F 47", "cmp.w	r3,r7,lsl #1",					// 1110 1011 1011 xxxx .xxx 1111 xxxx xxxx	// A8.6.36	T3
			"EB B2 0F 16", "cmp.w	r2,r6,lsr #32",					// 1110 1011 1011 xxxx .xxx 1111 xxxx xxxx	// A8.6.36	T3
			"EB B5 7F F9", "cmp.w	r5,r9,ror #31",					// 1110 1011 1011 xxxx .xxx 1111 xxxx xxxx	// A8.6.36	T3
			"EB B5 0F 38", "cmp.w	r5,r8,rrx",						// 1110 1011 1011 xxxx .xxx 1111 xxxx xxxx	// A8.6.36	T3
		    "F3 AF 81 00", "cps	#0",
		    "F3 AF 81 1F", "cps	#31",
			"F3 AF 86 A0", "cpsid	af",							// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// B6.1.1	T2
			"F3 AF 87 FF", "cpsid	aif,#31",						// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// B6.1.1	T2
			"F3 AF 87 61", "cpsid	if,#1",							// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// B6.1.1	T2
			"F3 AF 84 A0", "cpsie	af",							// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// B6.1.1	T2
			"F3 AF 85 FF", "cpsie	aif,#31",						// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// B6.1.1	T2
			"F3 AF 85 61", "cpsie	if,#1",							// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// B6.1.1	T2
			"F3 AF 80 F0", "dbg	#0",								// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.40	T1
			"F3 AF 80 FD", "dbg	#13",								// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.40	T1
			"F3 BF 8F 50", "dmb	#0",								// 1111 0011 1011 :::: 10.0 :::: 0101 xxxx	// A8.6.41	T1
			"F3 BF 8F 52", "dmb	oshst", 							// 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
			"F3 BF 8F 53", "dmb	osh",   							// 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
			"F3 BF 8F 56", "dmb	nshst", 							// 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
			"F3 BF 8F 57", "dmb	nsh",   							// 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
			"F3 BF 8F 5A", "dmb	ishst", 							// 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
			"F3 BF 8F 5B", "dmb	ish",   							// 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
			"F3 BF 8F 5E", "dmb	st",    							// 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
			"F3 BF 8F 5F", "dmb	sy",    							// 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
			"F3 BF 8F 42", "dsb	oshst",								// 1111 0011 1011 :::: 10.0 :::: 0100 xxxx	// A8.6.42	T1
			"F3 BF 8F 43", "dsb	osh",   							// 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
			"F3 BF 8F 46", "dsb	nshst", 							// 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
			"F3 BF 8F 47", "dsb	nsh",   							// 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
			"F3 BF 8F 4A", "dsb	ishst", 							// 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
			"F3 BF 8F 4B", "dsb	ish",   							// 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
			"F3 BF 8F 4D", "dsb	#13",								// 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
			"F3 BF 8F 4E", "dsb	st",    							// 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
			"F3 BF 8F 4F", "dsb	sy",    							// 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
///			"F3 BF 8F 1F", "enterx",								// 1111 0011 1011 :::: 10.0 :::: 0001 ::::	// A9.3.1	T1
			"F0 8A 05 71", "eor	r5,r10,#0x71",						// 1111 0x00 1000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 8B 06 F7", "eor	r6,r11,#0xf7",						// 1111 0x00 1000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 89 14 78", "eor	r4,r9,#0x780078",					// 1111 0x00 1000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 88 13 FC", "eor	r3,r8,#0xfc00fc",					// 1111 0x00 1000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 87 25 64", "eor	r5,r7,#0x64006400",					// 1111 0x00 1000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 86 25 E3", "eor	r5,r6,#0xe300e300",					// 1111 0x00 1000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 87 46 60", "eor	r6,r7,#0xe0000000",					// 1111 0x00 1000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 88 47 E0", "eor	r7,r8,#0x70000000",					// 1111 0x00 1000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F4 8A 05 60", "eor	r5,r10,#0xe00000",					// 1111 0x00 1000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F4 8A 45 60", "eor	r5,r10,#0xe000",					// 1111 0x00 1000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F4 8A 65 60", "eor	r5,r10,#0xe00",						// 1111 0x00 1000 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"EA 89 05 0A", "eor.w	r5,r9,r10",						// 1110 1010 1000 xxxx .xxx xxxx xxxx xxxx	// A8.6.45	T2
			"EA 88 14 A9", "eor.w	r4,r8,r9,asr #6",				// 1110 1010 1000 xxxx .xxx xxxx xxxx xxxx	// A8.6.45	T2
			"EA 87 03 48", "eor.w	r3,r7,r8,lsl #1",				// 1110 1010 1000 xxxx .xxx xxxx xxxx xxxx	// A8.6.45	T2
			"EA 86 02 17", "eor.w	r2,r6,r7,lsr #32",				// 1110 1010 1000 xxxx .xxx xxxx xxxx xxxx	// A8.6.45	T2
			"EA 89 75 F8", "eor.w	r5,r9,r8,ror #31",				// 1110 1010 1000 xxxx .xxx xxxx xxxx xxxx	// A8.6.45	T2
			"EA 88 05 39", "eor.w	r5,r8,r9,rrx",					// 1110 1010 1000 xxxx .xxx xxxx xxxx xxxx	// A8.6.45	T2
			"F0 9A 05 71", "eors	r5,r10,#0x71",					// 1111 0x00 1001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 9B 06 F7", "eors	r6,r11,#0xf7",					// 1111 0x00 1001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 99 14 78", "eors	r4,r9,#0x780078",				// 1111 0x00 1001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 98 13 FC", "eors	r3,r8,#0xfc00fc",				// 1111 0x00 1001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 97 25 64", "eors	r5,r7,#0x64006400",				// 1111 0x00 1001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 96 25 E3", "eors	r5,r6,#0xe300e300",				// 1111 0x00 1001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 97 46 60", "eors	r6,r7,#0xe0000000",				// 1111 0x00 1001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F0 98 47 E0", "eors	r7,r8,#0x70000000",				// 1111 0x00 1001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F4 9A 05 60", "eors	r5,r10,#0xe00000",				// 1111 0x00 1001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F4 9A 45 60", "eors	r5,r10,#0xe000",				// 1111 0x00 1001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"F4 9A 65 60", "eors	r5,r10,#0xe00",					// 1111 0x00 1001 xxxx 0xxx xxxx xxxx xxxx	// A8.6.44	T1
			"EA 99 05 0A", "eors.w	r5,r9,r10",						// 1110 1010 1001 xxxx .xxx xxxx xxxx xxxx	// A8.6.45	T2
			"EA 98 14 A9", "eors.w	r4,r8,r9,asr #6",				// 1110 1010 1001 xxxx .xxx xxxx xxxx xxxx	// A8.6.45	T2
			"EA 97 03 48", "eors.w	r3,r7,r8,lsl #1",				// 1110 1010 1001 xxxx .xxx xxxx xxxx xxxx	// A8.6.45	T2
			"EA 96 02 17", "eors.w	r2,r6,r7,lsr #32",				// 1110 1010 1001 xxxx .xxx xxxx xxxx xxxx	// A8.6.45	T2
			"EA 99 75 F8", "eors.w	r5,r9,r8,ror #31",				// 1110 1010 1001 xxxx .xxx xxxx xxxx xxxx	// A8.6.45	T2
			"EA 98 05 39", "eors.w	r5,r8,r9,rrx",					// 1110 1010 1001 xxxx .xxx xxxx xxxx xxxx	// A8.6.45	T2
			"F3 BF 8F 60", "isb	#0",								// 1111 0011 1011 :::: 10.0 :::: 0110 xxxx  // A8.6.49  T1
			"F3 BF 8F 6d", "isb	#13",								// 1111 0011 1011 :::: 10.0 :::: 0110 xxxx  // A8.6.49  T1
			"F3 BF 8F 6F", "isb	sy",								// 1111 0011 1011 :::: 10.0 :::: 0110 xxxx  // A8.6.49  T1
			"ED 1A B9 00", "ldc	p9,c11,[r10,#-0x0]",				// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"ED 3A B9 00", "ldc	p9,c11,[r10,#-0x0]!",				// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"ED 1A B9 21", "ldc	p9,c11,[r10,#-0x84]",				// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"ED 3A B9 21", "ldc	p9,c11,[r10,#-0x84]!",				// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"ED 9A B9 21", "ldc	p9,c11,[r10,#0x84]",				// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"ED BA B9 21", "ldc	p9,c11,[r10,#0x84]!",				// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"EC 3A B9 00", "ldc	p9,c11,[r10],#-0x0",				// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"EC 3A B9 21", "ldc	p9,c11,[r10],#-0x84",				// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"EC BA B9 21", "ldc	p9,c11,[r10],#0x84",				// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"ED 1F B9 00", "ldc	p9,c11,[pc,#-0x0]",					// 1110 110x xxx1 1111 xxxx xxxx xxxx xxxx	// A8.6.52	T1
			"FD 1A B9 21", "ldc2	p9,c11,[r10,#-0x84]",			// 1111 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T2
			"FD 3A B9 21", "ldc2	p9,c11,[r10,#-0x84]!",			// 1111 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T2
			"FD 9A B9 21", "ldc2	p9,c11,[r10,#0x84]",			// 1111 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T2
			"FD BA B9 21", "ldc2	p9,c11,[r10,#0x84]!",			// 1111 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T2
			"FC 3A B9 21", "ldc2	p9,c11,[r10],#-0x84",			// 1111 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T2
			"FC BA B9 21", "ldc2	p9,c11,[r10],#0x84",			// 1111 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T2
			"FD 1F B9 00", "ldc2	p9,c11,[pc,#-0x0]",				// 1111 110x xxx1 1111 xxxx xxxx xxxx xxxx	// A8.6.52	T2
			"FD 5A B9 21", "ldc2l	p9,c11,[r10,#-0x84]",			// 1111 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T2
			"FD 7A B9 21", "ldc2l	p9,c11,[r10,#-0x84]!",			// 1111 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T2
			"FD DA B9 21", "ldc2l	p9,c11,[r10,#0x84]",			// 1111 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T2
			"FD FA B9 21", "ldc2l	p9,c11,[r10,#0x84]!",			// 1111 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T2
			"FC 7A B9 21", "ldc2l	p9,c11,[r10],#-0x84",			// 1111 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T2
			"FC FA B9 21", "ldc2l	p9,c11,[r10],#0x84",			// 1111 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T2
			"FD 5F B9 00", "ldc2l	p9,c11,[pc,#-0x0]",				// 1111 110x xxx1 1111 xxxx xxxx xxxx xxxx	// A8.6.52	T2
			"ED 5A B9 00", "ldcl	p9,c11,[r10,#-0x0]",			// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"ED 7A B9 00", "ldcl	p9,c11,[r10,#-0x0]!",			// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"ED 5A B9 21", "ldcl	p9,c11,[r10,#-0x84]",			// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"ED 7A B9 21", "ldcl	p9,c11,[r10,#-0x84]!",			// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"ED DA B9 21", "ldcl	p9,c11,[r10,#0x84]",			// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"ED FA B9 21", "ldcl	p9,c11,[r10,#0x84]!",			// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"EC 7A B9 00", "ldcl	p9,c11,[r10],#-0x0",			// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"EC 7A B9 21", "ldcl	p9,c11,[r10],#-0x84",			// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"EC FA B9 21", "ldcl	p9,c11,[r10],#0x84",			// 1110 110x xxx1 xxxx xxxx xxxx xxxx xxxx	// A8.6.51	T1
			"ED 5F B9 00", "ldcl	p9,c11,[pc,#-0x0]",				// 1111 110x xxx1 1111 xxxx xxxx xxxx xxxx	// A8.6.52	T2
			"E8 BA 42 40", "ldm.w	r10!,{r6,r9,lr}",				// 1110 1000 10x1 xxxx xx.x xxxx xxxx xxxx	// A8.6.53	T2
			"E8 9A 82 40", "ldm.w	r10,{r6,r9,pc}",				// 1110 1000 10x1 xxxx xx.x xxxx xxxx xxxx	// A8.6.53	T2
			"E9 3A 42 40", "ldmdb	r10!,{r6,r9,lr}",				// 1110 1001 00x1 xxxx xx.x xxxx xxxx xxxx	// A8.6.55	T1
			"E9 1A 82 40", "ldmdb	r10,{r6,r9,pc}",				// 1110 1001 00x1 xxxx xx.x xxxx xxxx xxxx	// A8.6.55	T1
			"F8 DA 50 00", "ldr.w	r5,[r10]",						// 1111 1000 1101 xxxx xxxx xxxx xxxx xxxx	// A8.6.57	T3
			"F8 D6 47 89", "ldr.w	r4,[r6,#0x789]",				// 1111 1000 1101 xxxx xxxx xxxx xxxx xxxx	// A8.6.57	T3
			"F8 5A 5C 80", "ldr	r5,[r10,#-0x80]",					// 1111 1000 0101 xxxx xxxx 1xxx xxxx xxxx	// A8.6.57	T4
			"F8 5A 5A 82", "ldr	r5,[r10],#0x82",					// 1111 1000 0101 xxxx xxxx 1xxx xxxx xxxx	// A8.6.57	T4
			"F8 5A 58 84", "ldr	r5,[r10],#-0x84",					// 1111 1000 0101 xxxx xxxx 1xxx xxxx xxxx	// A8.6.57	T4
			"F8 5A 5F 86", "ldr	r5,[r10,#0x86]!",					// 1111 1000 0101 xxxx xxxx 1xxx xxxx xxxx	// A8.6.57	T4
			"F8 5A 5D 88", "ldr	r5,[r10,#-0x88]!",					// 1111 1000 0101 xxxx xxxx 1xxx xxxx xxxx	// A8.6.57	T4
			"F8 DF 57 89", "ldr.w	r5,[pc,#0x789] ; 0x789",		// 1111 1000 x101 1111 xxxx xxxx xxxx xxxx	// A8.6.59	T2
			"F8 5F 69 87", "ldr.w	r6,[pc,#-0x987] ; 0xfffff679",	// 1111 1000 x101 1111 xxxx xxxx xxxx xxxx	// A8.6.59	T2
			"F8 5A 50 08", "ldr.w	r5,[r10,r8]",					// 1111 1000 0101 xxxx xxxx 0000 00xx xxxx	// A8.6.60	T2
			"F8 59 60 37", "ldr.w	r6,[r9,r7,lsl #3]",				// 1111 1000 0101 xxxx xxxx 0000 00xx xxxx	// A8.6.60	T2
			"F8 9A 50 00", "ldrb.w	r5,[r10]",						// 1111 1000 1001 xxxx xxxx xxxx xxxx xxxx	// A8.6.61	T3
			"F8 96 47 89", "ldrb.w	r4,[r6,#0x789]",				// 1111 1000 1001 xxxx xxxx xxxx xxxx xxxx	// A8.6.61	T3
			"F8 1A 5C 80", "ldrb	r5,[r10,#-0x80]",				// 1111 1000 0001 xxxx xxxx 1xxx xxxx xxxx	// A8.6.61	T4
			"F8 1A 5A 82", "ldrb	r5,[r10],#0x82",				// 1111 1000 0001 xxxx xxxx 1xxx xxxx xxxx	// A8.6.61	T4
			"F8 1A 58 84", "ldrb	r5,[r10],#-0x84",				// 1111 1000 0001 xxxx xxxx 1xxx xxxx xxxx	// A8.6.61	T4
			"F8 1A 5F 86", "ldrb	r5,[r10,#0x86]!",				// 1111 1000 0001 xxxx xxxx 1xxx xxxx xxxx	// A8.6.61	T4
			"F8 1A 5D 88", "ldrb	r5,[r10,#-0x88]!",				// 1111 1000 0001 xxxx xxxx 1xxx xxxx xxxx	// A8.6.61	T4
			"F8 9F 57 89", "ldrb	r5,[pc,#0x789] ; 0x789",		// 1111 1000 0001 1111 xxxx xxxx xxxx xxxx	// A8.6.63	T1
			"F8 1F 69 87", "ldrb	r6,[pc,#-0x987] ; 0xfffff679",	// 1111 1000 x001 1111 xxxx xxxx xxxx xxxx	// A8.6.63	T1
			"F8 1A 50 08", "ldrb.w	r5,[r10,r8]",					// 1111 1000 0001 xxxx xxxx 0000 00xx xxxx	// A8.6.64	T2
			"F8 19 60 37", "ldrb.w	r6,[r9,r7,lsl #3]",				// 1111 1000 0001 xxxx xxxx 0000 00xx xxxx	// A8.6.64	T2
			"F8 1A 5E 00", "ldrbt	r5,[r10]",						// 1111 1000 0001 xxxx xxxx 1110 xxxx xxxx	// A8.6.65	T1
			"F8 19 6E 84", "ldrbt	r6,[r9,#0x84]",					// 1111 1000 0001 xxxx xxxx 1110 xxxx xxxx	// A8.6.65	T1
			"E9 5A 67 84", "ldrd	r6,r7,[r10,#-0x210]",			// 1110 100p u1w1 xxxx xxxx xxxx xxxx xxxx	// A8.6.66	T1
			"E9 7A 67 85", "ldrd	r6,r7,[r10,#-0x214]!",			// 1110 100p u1w1 xxxx xxxx xxxx xxxx xxxx	// A8.6.66	T1
			"E9 DA 67 84", "ldrd	r6,r7,[r10,#0x210]",			// 1110 100p u1w1 xxxx xxxx xxxx xxxx xxxx	// A8.6.66	T1
			"E9 FA 67 85", "ldrd	r6,r7,[r10,#0x214]!",			// 1110 100p u1w1 xxxx xxxx xxxx xxxx xxxx	// A8.6.66	T1
			"E9 5A 67 00", "ldrd	r6,r7,[r10]",					// 1110 100p u1w1 xxxx xxxx xxxx xxxx xxxx	// A8.6.66	T1
			"E8 7A 67 84", "ldrd	r6,r7,[r10],#-0x210",			// 1110 100p u1w1 xxxx xxxx xxxx xxxx xxxx	// A8.6.66	T1
			"E8 FA 67 85", "ldrd	r6,r7,[r10],#0x214",			// 1110 100p u1w1 xxxx xxxx xxxx xxxx xxxx	// A8.6.66	T1
			"E9 5F 67 00", "ldrd	r6,r7,[pc,#-0x0]",				// 1110 100p u1w1 1111 xxxx xxxx xxxx xxxx	// A8.6.67	T1
			"E9 5F 67 84", "ldrd	r6,r7,[pc,#-0x210]",			// 1110 100x x1x1 1111 xxxx xxxx xxxx xxxx	// A8.6.67	T1
			"E9 DF 67 85", "ldrd	r6,r7,[pc,#0x214]",				// 1110 100x x1x1 1111 xxxx xxxx xxxx xxxx	// A8.6.67	T1
			"E8 5A 5F 00", "ldrex	r5,[r10]",						// 1110 1000 0101 xxxx xxxx :::: xxxx xxxx	// A8.6.69	T1
			"E8 59 6F 87", "ldrex	r6,[r9,#0x21c]",				// 1110 1000 0101 xxxx xxxx :::: xxxx xxxx	// A8.6.69	T1
			"E8 DA 5F 4F", "ldrexb 	r5,[r10]",						// 1110 1000 1101 xxxx xxxx :::: 0100 ::::	// A8.6.70	T1
			"E8 DA 56 7F", "ldrexd	r5,r6,[r10]",					// 1110 1000 1101 xxxx xxxx xxxx 0111 ::::	// A8.6.71	T1
			"E8 DA 5F 5F", "ldrexh	r5,[r10]",						// 1110 1000 1101 xxxx xxxx :::: 0101 ::::	// A8.6.72	T1
			"F8 BA 50 00", "ldrh.w	r5,[r10]",						// 1111 1000 1011 xxxx xxxx xxxx xxxx xxxx	// A8.6.73	T3
			"F8 B6 47 89", "ldrh.w	r4,[r6,#0x789]",				// 1111 1000 1011 xxxx xxxx xxxx xxxx xxxx	// A8.6.73	T3
			"F8 3A 5C 80", "ldrh	r5,[r10,#-0x80]",				// 1111 1000 0011 xxxx xxxx 1xxx xxxx xxxx	// A8.6.73	T4
			"F8 3A 5A 82", "ldrh	r5,[r10],#0x82",				// 1111 1000 0011 xxxx xxxx 1xxx xxxx xxxx	// A8.6.73	T4
			"F8 3A 58 84", "ldrh	r5,[r10],#-0x84",				// 1111 1000 0011 xxxx xxxx 1xxx xxxx xxxx	// A8.6.73	T4
			"F8 3A 5F 86", "ldrh	r5,[r10,#0x86]!",				// 1111 1000 0011 xxxx xxxx 1xxx xxxx xxxx	// A8.6.73	T4
			"F8 3A 5D 88", "ldrh	r5,[r10,#-0x88]!",				// 1111 1000 0011 xxxx xxxx 1xxx xxxx xxxx	// A8.6.73	T4
			"F8 BF 57 89", "ldrh	r5,[pc,#0x789] ; 0x789",		// 1111 1000 x011 1111 xxxx xxxx xxxx xxxx	// A8.6.75	T1
			"F8 3F 69 87", "ldrh	r6,[pc,#-0x987] ; 0xfffff679",	// 1111 1000 x011 1111 xxxx xxxx xxxx xxxx	// A8.6.75	T1
			"F8 3A 50 08", "ldrh.w	r5,[r10,r8]",					// 1111 1000 0011 xxxx xxxx 0000 00xx xxxx	// A8.6.76	T2
			"F8 39 60 37", "ldrh.w	r6,[r9,r7,lsl #3]",				// 1111 1000 0011 xxxx xxxx 0000 00xx xxxx	// A8.6.76	T2
			"F8 3A 5E 00", "ldrht	r5,[r10]",						// 1111 1000 0011 xxxx xxxx 1110 xxxx xxxx	// A8.6.77	T1
			"F8 39 6E 84", "ldrht	r6,[r9,#0x84]",					// 1111 1000 0011 xxxx xxxx 1110 xxxx xxxx	// A8.6.77	T1
			"F9 9A 50 00", "ldrsb	r5,[r10]",						// 1111 1001 1001 xxxx xxxx xxxx xxxx xxxx	// A8.6.78	T1
			"F9 96 47 89", "ldrsb	r4,[r6,#0x789]",				// 1111 1001 1001 xxxx xxxx xxxx xxxx xxxx	// A8.6.78	T1
			"F9 1A 5C 80", "ldrsb	r5,[r10,#-0x80]",				// 1111 1001 0001 xxxx xxxx 1xxx xxxx xxxx	// A8.6.78	T2
			"F9 1A 5A 82", "ldrsb	r5,[r10],#0x82",				// 1111 1001 0001 xxxx xxxx 1xxx xxxx xxxx	// A8.6.78	T2
			"F9 1A 58 84", "ldrsb	r5,[r10],#-0x84",				// 1111 1001 0001 xxxx xxxx 1xxx xxxx xxxx	// A8.6.78	T2
			"F9 1A 5F 86", "ldrsb	r5,[r10,#0x86]!",				// 1111 1001 0001 xxxx xxxx 1xxx xxxx xxxx	// A8.6.78	T2
			"F9 1A 5D 88", "ldrsb	r5,[r10,#-0x88]!",				// 1111 1001 0001 xxxx xxxx 1xxx xxxx xxxx	// A8.6.78	T2
			"F9 9F 57 89", "ldrsb	r5,[pc,#0x789] ; 0x789",		// 1111 1001 x001 1111 xxxx xxxx xxxx xxxx	// A8.6.79	T1
			"F9 1F 69 87", "ldrsb	r6,[pc,#-0x987] ; 0xfffff679",	// 1111 1001 x001 1111 xxxx xxxx xxxx xxxx	// A8.6.79	T1
			"F9 1A 50 08", "ldrsb.w	r5,[r10,r8]",					// 1111 1001 0001 xxxx xxxx 0000 00xx xxxx	// A8.6.80	T2
			"F9 19 60 37", "ldrsb.w	r6,[r9,r7,lsl #3]",				// 1111 1001 0001 xxxx xxxx 0000 00xx xxxx	// A8.6.80	T2
			"F9 1A 5E 00", "ldrsbt	r5,[r10]",						// 1111 1001 0001 xxxx xxxx 1110 xxxx xxxx	// A8.6.81	T1
			"F9 19 6E 84", "ldrsbt	r6,[r9,#0x84]",					// 1111 1001 0001 xxxx xxxx 1110 xxxx xxxx	// A8.6.81	T1
			"F9 BA 50 00", "ldrsh	r5,[r10]",						// 1111 1001 1011 xxxx xxxx xxxx xxxx xxxx	// A8.6.82	T1
			"F9 B6 47 89", "ldrsh	r4,[r6,#0x789]",				// 1111 1001 1011 xxxx xxxx xxxx xxxx xxxx	// A8.6.82	T1
			"F9 3A 5C 80", "ldrsh	r5,[r10,#-0x80]",				// 1111 1001 0011 xxxx xxxx 1xxx xxxx xxxx	// A8.6.82	T2
			"F9 3A 5A 82", "ldrsh	r5,[r10],#0x82",				// 1111 1001 0011 xxxx xxxx 1xxx xxxx xxxx	// A8.6.82	T2
			"F9 3A 58 84", "ldrsh	r5,[r10],#-0x84",				// 1111 1001 0011 xxxx xxxx 1xxx xxxx xxxx	// A8.6.82	T2
			"F9 3A 5F 86", "ldrsh	r5,[r10,#0x86]!",				// 1111 1001 0011 xxxx xxxx 1xxx xxxx xxxx	// A8.6.82	T2
			"F9 3A 5D 88", "ldrsh	r5,[r10,#-0x88]!",				// 1111 1001 0011 xxxx xxxx 1xxx xxxx xxxx	// A8.6.82	T2
			"F9 BF 57 89", "ldrsh	r5,[pc,#0x789] ; 0x789",		// 1111 1001 x011 1111 xxxx xxxx xxxx xxxx	// A8.6.83	T1
			"F9 3F 69 87", "ldrsh	r6,[pc,#-0x987] ; 0xfffff679",	// 1111 1001 x011 1111 xxxx xxxx xxxx xxxx	// A8.6.83	T1
			"F9 3A 50 08", "ldrsh.w	r5,[r10,r8]",					// 1111 1001 0011 xxxx xxxx 0000 00xx xxxx	// A8.6.84	T2
			"F9 39 60 37", "ldrsh.w	r6,[r9,r7,lsl #3]",				// 1111 1001 0011 xxxx xxxx 0000 00xx xxxx	// A8.6.84	T2
			"F9 3A 5E 00", "ldrsht	r5,[r10]",						// 1111 1001 0011 xxxx xxxx 1110 xxxx xxxx	// A8.6.85	T1
			"F9 36 4E 89", "ldrsht	r4,[r6,#0x89]",					// 1111 1001 0011 xxxx xxxx 1110 xxxx xxxx	// A8.6.85	T1
			"F8 5A 5E 00", "ldrt	r5,[r10]",						// 1111 1000 0101 xxxx xxxx 1110 xxxx xxxx	// A8.6.86	T3
			"F8 56 4E 89", "ldrt	r4,[r6,#0x89]",					// 1111 1000 0101 xxxx xxxx 1110 xxxx xxxx	// A8.6.86	T3
///			"F3 BF 8F 0F", "leavex",								// 1111 0011 1011 :::: 10.0 :::: 0000 ::::	// A9.3.1	T1
			"EA 4F 45 49", "lsl.w	r5,r9,#17",						// 1110 1010 0100 1111 .xxx xxxx xx00 xxxx	// A8.6.88	T2
			"EA 4F 06 48", "lsl.w	r6,r8,#1",						// 1110 1010 0100 1111 .xxx xxxx xx00 xxxx	// A8.6.88	T2
			"FA 09 F5 0A", "lsl.w	r5,r9,r10",						// 1111 1010 0000 xxxx 1111 xxxx 0000 xxxx	// A8.6.89	T2
			"EA 5F 45 49", "lsls.w	r5,r9,#17",						// 1110 1010 0101 1111 .xxx xxxx xx00 xxxx	// A8.6.89	T2
			"EA 5F 06 48", "lsls.w	r6,r8,#1",						// 1110 1010 0101 1111 .xxx xxxx xx00 xxxx	// A8.6.89	T2
			"FA 19 F5 0A", "lsls.w	r5,r9,r10",						// 1111 1010 0001 xxxx 1111 xxxx 0000 xxxx	// A8.6.89	T2
			"EA 4F 45 59", "lsr.w	r5,r9,#17",						// 1110 1010 0100 1111 .xxx xxxx xx01 xxxx	// A8.6.90	T2
			"EA 4F 06 18", "lsr.w	r6,r8,#32",						// 1110 1010 0100 1111 .xxx xxxx xx01 xxxx	// A8.6.90	T2
			"FA 29 F5 0A", "lsr.w	r5,r9,r10",						// 1111 1010 0010 xxxx 1111 xxxx 0000 xxxx	// A8.6.91	T2
			"EA 5F 45 59", "lsrs.w	r5,r9,#17",						// 1110 1010 0101 1111 .xxx xxxx xx01 xxxx	// A8.6.90	T2
			"EA 5F 06 18", "lsrs.w	r6,r8,#32",						// 1110 1010 0101 1111 .xxx xxxx xx01 xxxx	// A8.6.90	T2
			"FA 39 F5 0A", "lsrs.w	r5,r9,r10",						// 1111 1010 0011 xxxx 1111 xxxx 0000 xxxx	// A8.6.91	T2
			"EE C9 59 FA", "mcr	p9,0x6,r5,c9,c10,0x7",				// 1110 1110 xxx0 xxxx xxxx xxxx xxx1 xxxx	// A8.6.92	T1/A1
			"FE C9 59 FA", "mcr2	p9,0x6,r5,c9,c10,0x7",			// 1111 1110 xxx0 xxxx xxxx xxxx xxx1 xxxx	// A8.6.92	T2/A2
			"EC 46 59 C9", "mcrr	p9,0xc,r5,r6,c9",				// 1110 1100 0100 xxxx xxxx xxxx xxxx xxxx	// A8.6.93	T1/A1
			"FC 46 59 C9", "mcrr2	p9,0xc,r5,r6,c9",				// 1111 1100 0100 xxxx xxxx xxxx xxxx xxxx	// A8.6.93	T2/A2
			"FB 09 85 0A", "mla	r5,r9,r10,r8",						// 1111 1011 0000 xxxx xxxx xxxx 0000 xxxx	// A8.6.94	T1
			"FB 09 85 1A", "mls	r5,r9,r10,r8",						// 1111 1011 0000 xxxx xxxx xxxx 0001 xxxx	// A8.6.95	T1
			"F0 4F 05 71", "mov.w	r5,#0x71",						// 1111 0x00 010x 1111 0xxx xxxx xxxx xxxx	// A8.6.96	T2
			"EA 4F 05 0A", "mov.w	r5,r10",						// 1110 1010 010x 1111 .000 xxxx 0000 xxxx	// A8.6.97	T3
			"F0 5F 05 73", "movs.w	r5,#0x73",						// 1111 0x00 010x 1111 0xxx xxxx xxxx xxxx	// A8.6.96	T2
			"EA 5F 05 0A", "movs.w	r5,r10",						// 1110 1010 010x 1111 .000 xxxx 0000 xxxx	// A8.6.97	T3
			"F2 C0 25 87", "movt	r5,#0x287",						// 1111 0x10 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.99	T1
			"F6 C6 35 89", "movt	r5,#0x6b89",					// 1111 0x10 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.99	T1
			"F2 40 25 87", "movw	r5,#0x287",						// 1111 0x10 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.96	T3
			"F6 46 35 89", "movw	r5,#0x6b89",					// 1111 0x10 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.96	T3
			"EE D5 F9 B9", "mrc	p9,0x6,apsr_nzcv,c5,c9,0x5",		// 1110 1110 xxx1 xxxx xxxx xxxx xxx1 xxxx	// A8.6.100	T1/A1
			"EE F5 59 99", "mrc	p9,0x7,r5,c5,c9,0x4",				// 1110 1110 xxx1 xxxx xxxx xxxx xxx1 xxxx	// A8.6.100	T1/A1
			"FE 95 F9 F9", "mrc2	p9,0x4,apsr_nzcv,c5,c9,0x7",	// 1111 1110 xxx1 xxxx xxxx xxxx xxx1 xxxx	// A8.6.100	T2/A2
			"FE B5 59 D9", "mrc2	p9,0x5,r5,c5,c9,0x6",			// 1111 1110 xxx1 xxxx xxxx xxxx xxx1 xxxx	// A8.6.100	T2/A2
			"EC 56 59 F9", "mrrc	p9,0xf,r5,r6,c9",				// 1110 1100 0101 xxxx xxxx xxxx xxxx xxxx	// A8.6.101	T1/A1
			"FC 56 59 39", "mrrc2	p9,0x3,r5,r6,c9",				// 1111 1100 0101 xxxx xxxx xxxx xxxx xxxx	// A8.6.101	T2/A2
			"F3 EF 85 00", "mrs	r5,cpsr",							// 1111 0011 1110 :::: 10.0 xxxx .... ....	// A8.6.102	T1
			"F3 FF 85 00", "mrs	r5,spsr",							// 1111 0011 1110 :::: 10.0 xxxx .... ....	// B6.1.5	T1
			"F3 8A 88 00", "msr	cpsr_f,r10",						// 1111 0011 100x xxxx 10.0 xx00 .... ....	// A8.6.104	T1
			"F3 8A 84 00", "msr	cpsr_s,r10",						// 1111 0011 100x xxxx 10.0 xx00 .... ....	// A8.6.104	T1
			"F3 8A 8C 00", "msr	cpsr_fs,r10",						// 1111 0011 100x xxxx 10.0 xx00 .... ....	// A8.6.104	T1
			"F3 8A 8F 00", "msr	cpsr_cxfs,r10",						// 1111 0011 100x xxxx 10.0 xxxx .... ....	// B6.1.7	T1
			"F3 8A 8E 00", "msr	cpsr_xfs,r10",						// 1111 0011 100x xxxx 10.0 xxxx .... ....	// B6.1.7	T1
			"F3 8A 8D 00", "msr	cpsr_cfs,r10",						// 1111 0011 100x xxxx 10.0 xxxx .... ....	// B6.1.7	T1
			"F3 9A 8F 00", "msr	spsr_cxfs,r10",						// 1111 0011 100x xxxx 10.0 xxxx .... ....	// B6.1.7	T1
			"FB 09 F5 0A", "mul	r5,r9,r10",							// 1111 1011 0000 xxxx 1111 xxxx 0000 xxxx	// A8.6.105	T2
			"F0 6F 05 71", "mvn	r5,#0x71",							// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 6F 05 F7", "mvn	r5,#0xf7",							// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 6F 15 78", "mvn	r5,#0x780078",						// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 6F 15 FC", "mvn	r5,#0xfc00fc",						// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 6F 25 64", "mvn	r5,#0x64006400",					// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 6F 25 E3", "mvn	r5,#0xe300e300",					// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 6F 45 60", "mvn	r5,#0xe0000000",					// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 6F 45 E0", "mvn	r5,#0x70000000",					// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F4 6F 05 60", "mvn	r5,#0xe00000",						// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F4 6F 45 60", "mvn	r5,#0xe000",						// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F4 6F 65 60", "mvn	r5,#0xe00",							// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"EA 6F 05 09", "mvn.w	r5,r9",							// 1110 1010 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.107	T2
			"EA 6F 14 A8", "mvn.w	r4,r8,asr #6",					// 1110 1010 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.107	T2
			"EA 6F 03 47", "mvn.w	r3,r7,lsl #1",					// 1110 1010 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.107	T2
			"EA 6F 02 16", "mvn.w	r2,r6,lsr #32",					// 1110 1010 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.107	T2
			"EA 6F 75 F9", "mvn.w	r5,r9,ror #31",					// 1110 1010 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.107	T2
			"EA 6F 05 38", "mvn.w	r5,r8,rrx",						// 1110 1010 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.107	T2
			"F0 7F 05 71", "mvns	r5,#0x71",						// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 7F 05 F7", "mvns	r5,#0xf7",						// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 7F 15 78", "mvns	r5,#0x780078",					// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 7F 15 FC", "mvns	r5,#0xfc00fc",					// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 7F 25 64", "mvns	r5,#0x64006400",				// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 7F 25 E3", "mvns	r5,#0xe300e300",				// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 7F 45 60", "mvns	r5,#0xe0000000",				// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F0 7F 45 E0", "mvns	r5,#0x70000000",				// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F4 7F 05 60", "mvns	r5,#0xe00000",					// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F4 7F 45 60", "mvns	r5,#0xe000",					// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"F4 7F 65 60", "mvns	r5,#0xe00",						// 1111 0x00 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.106	T1
			"EA 7F 05 09", "mvns.w	r5,r9",							// 1110 1010 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.107	T2
			"EA 7F 14 A8", "mvns.w	r4,r8,asr #6",					// 1110 1010 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.107	T2
			"EA 7F 03 47", "mvns.w	r3,r7,lsl #1",					// 1110 1010 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.107	T2
			"EA 7F 02 16", "mvns.w	r2,r6,lsr #32",					// 1110 1010 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.107	T2
			"EA 7F 75 F9", "mvns.w	r5,r9,ror #31",					// 1110 1010 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.107	T2
			"EA 7F 05 38", "mvns.w	r5,r8,rrx",						// 1110 1010 011x 1111 0xxx xxxx xxxx xxxx	// A8.6.107	T2
			"F3 AF 80 00", "nop.w",									// 1111 0011 0110 :::: 10.0 .xxx xxxx xxxx	// B6.1.1	T2
			"F0 6A 05 71", "orn	r5,r10,#0x71",						// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 6B 06 F7", "orn	r6,r11,#0xf7",						// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 69 14 78", "orn	r4,r9,#0x780078",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 68 13 FC", "orn	r3,r8,#0xfc00fc",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 67 25 64", "orn	r5,r7,#0x64006400",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 66 25 E3", "orn	r5,r6,#0xe300e300",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 67 46 60", "orn	r6,r7,#0xe0000000",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 68 47 E0", "orn	r7,r8,#0x70000000",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F4 6A 05 60", "orn	r5,r10,#0xe00000",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F4 6A 45 60", "orn	r5,r10,#0xe000",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F4 6A 65 60", "orn	r5,r10,#0xe00",						// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"EA 69 05 0A", "orn	r5,r9,r10",							// 1110 1010 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.112	T2
			"EA 68 14 A9", "orn	r4,r8,r9,asr #6",					// 1110 1010 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.112	T2
			"EA 67 03 48", "orn	r3,r7,r8,lsl #1",					// 1110 1010 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.112	T2
			"EA 66 02 17", "orn	r2,r6,r7,lsr #32",					// 1110 1010 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.112	T2
			"EA 69 75 F8", "orn	r5,r9,r8,ror #31",					// 1110 1010 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.112	T2
			"EA 68 05 39", "orn	r5,r8,r9,rrx",						// 1110 1010 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.112	T2
			"F0 7A 05 71", "orns	r5,r10,#0x71",					// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 7B 06 F7", "orns	r6,r11,#0xf7",					// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 79 14 78", "orns	r4,r9,#0x780078",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 78 13 FC", "orns	r3,r8,#0xfc00fc",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 77 25 64", "orns	r5,r7,#0x64006400",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 76 25 E3", "orns	r5,r6,#0xe300e300",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 77 46 60", "orns	r6,r7,#0xe0000000",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F0 78 47 E0", "orns	r7,r8,#0x70000000",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F4 7A 05 60", "orns	r5,r10,#0xe00000",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F4 7A 45 60", "orns	r5,r10,#0xe000",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"F4 7A 65 60", "orns	r5,r10,#0xe00",					// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.111	T1
			"EA 79 05 0A", "orns	r5,r9,r10",						// 1110 1010 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.112	T2
			"EA 78 14 A9", "orns	r4,r8,r9,asr #6",				// 1110 1010 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.112	T2
			"EA 77 03 48", "orns	r3,r7,r8,lsl #1",				// 1110 1010 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.112	T2
			"EA 76 02 17", "orns	r2,r6,r7,lsr #32",				// 1110 1010 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.112	T2
			"EA 79 75 F8", "orns	r5,r9,r8,ror #31",				// 1110 1010 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.112	T2
			"EA 78 05 39", "orns	r5,r8,r9,rrx",					// 1110 1010 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.112	T2
			"F0 4A 05 71", "orr	r5,r10,#0x71",						// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 4B 06 F7", "orr	r6,r11,#0xf7",						// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 49 14 78", "orr	r4,r9,#0x780078",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 48 13 FC", "orr	r3,r8,#0xfc00fc",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 47 25 64", "orr	r5,r7,#0x64006400",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 46 25 E3", "orr	r5,r6,#0xe300e300",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 47 46 60", "orr	r6,r7,#0xe0000000",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 48 47 E0", "orr	r7,r8,#0x70000000",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F4 4A 05 60", "orr	r5,r10,#0xe00000",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F4 4A 45 60", "orr	r5,r10,#0xe000",					// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F4 4A 65 60", "orr	r5,r10,#0xe00",						// 1111 0x00 0100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"EA 49 05 0A", "orr.w	r5,r9,r10",						// 1110 1010 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.114	T2
			"EA 48 14 A9", "orr.w	r4,r8,r9,asr #6",				// 1110 1010 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.114	T2
			"EA 47 03 48", "orr.w	r3,r7,r8,lsl #1",				// 1110 1010 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.114	T2
			"EA 46 02 17", "orr.w	r2,r6,r7,lsr #32",				// 1110 1010 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.114	T2
			"EA 49 75 F8", "orr.w	r5,r9,r8,ror #31",				// 1110 1010 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.114	T2
			"EA 48 05 39", "orr.w	r5,r8,r9,rrx",					// 1110 1010 0100 xxxx .xxx xxxx xxxx xxxx	// A8.6.114	T2
			"F0 5A 05 71", "orrs	r5,r10,#0x71",					// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 5B 06 F7", "orrs	r6,r11,#0xf7",					// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 59 14 78", "orrs	r4,r9,#0x780078",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 58 13 FC", "orrs	r3,r8,#0xfc00fc",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 57 25 64", "orrs	r5,r7,#0x64006400",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 56 25 E3", "orrs	r5,r6,#0xe300e300",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 57 46 60", "orrs	r6,r7,#0xe0000000",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F0 58 47 E0", "orrs	r7,r8,#0x70000000",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F4 5A 05 60", "orrs	r5,r10,#0xe00000",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F4 5A 45 60", "orrs	r5,r10,#0xe000",				// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"F4 5A 65 60", "orrs	r5,r10,#0xe00",					// 1111 0x00 0101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.113	T1
			"EA 59 05 0A", "orrs.w	r5,r9,r10",						// 1110 1010 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.114	T2
			"EA 58 14 A9", "orrs.w	r4,r8,r9,asr #6",				// 1110 1010 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.114	T2
			"EA 57 03 48", "orrs.w	r3,r7,r8,lsl #1",				// 1110 1010 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.114	T2
			"EA 56 02 17", "orrs.w	r2,r6,r7,lsr #32",				// 1110 1010 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.114	T2
			"EA 59 75 F8", "orrs.w	r5,r9,r8,ror #31",				// 1110 1010 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.114	T2
			"EA 58 05 39", "orrs.w	r5,r8,r9,rrx",					// 1110 1010 0101 xxxx .xxx xxxx xxxx xxxx	// A8.6.114	T2
			"EA C9 05 0A", "pkhbt	r5,r9,r10",						// 1110 1010 1100 xxxx .xxx xxxx xxx0 xxxx	// A8.6.116	T1
			"EA C7 03 48", "pkhbt	r3,r7,r8,lsl #1",				// 1110 1010 1100 xxxx .xxx xxxx xxx0 xxxx	// A8.6.116	T1
			"EA C8 64 A9", "pkhtb	r4,r8,r9,asr #26",				// 1110 1010 1100 xxxx .xxx xxxx xxx0 xxxx	// A8.6.116	T1
			"F8 9A F9 87", "pld	[r10,#0x987]",						// 1111 1000 10x1 xxxx 1111 xxxx xxxx xxxx	// A8.6.117	T1
			"F8 1A FC 71", "pld	[r10,#-0x71]",						// 1111 1000 00x1 xxxx 1111 1100 xxxx xxxx	// A8.6.117	T2
			"F8 1F F9 87", "pld	[pc,#-0x987] ; 0xfffff679",			// 1111 1000 x0x1 1111 1111 xxxx xxxx xxxx	// A8.6.118	T1
			"F8 9F F9 87", "pld	[pc,#0x987] ; 0x987",				// 1111 1000 x0x1 1111 1111 xxxx xxxx xxxx	// A8.6.118	T1
			"F8 1A F0 19", "pld	[r10,r9,lsl #1]",					// 1111 1000 00x1 xxxx 1111 0000 00xx xxxx	// A8.6.119	T1
			"F8 1A F0 39", "pld	[r10,r9,lsl #3]",					// 1111 1000 00x1 xxxx 1111 0000 00xx xxxx	// A8.6.119	T1
			"F8 1A F0 09", "pld	[r10,r9]",							// 1111 1000 00x1 xxxx 1111 0000 00xx xxxx	// A8.6.119	T1
			"F8 BA F9 87", "pldw	[r10,#0x987]",					// 1111 1000 10x1 xxxx 1111 xxxx xxxx xxxx	// A8.6.117	T1
			"F8 3A FC 71", "pldw	[r10,#-0x71]",					// 1111 1000 00x1 xxxx 1111 1100 xxxx xxxx	// A8.6.117	T2
			"F8 3F F9 87", "pld	[pc,#-0x987] ; 0xfffff679",			// 1111 1000 x0x1 1111 1111 xxxx xxxx xxxx	// A8.6.118	T1
			"F8 BF F9 87", "pld	[pc,#0x987] ; 0x987",				// 1111 1000 x0x1 1111 1111 xxxx xxxx xxxx	// A8.6.118	T1
			"F8 3A F0 19", "pldw	[r10,r9,lsl #1]",				// 1111 1000 00x1 xxxx 1111 0000 00xx xxxx	// A8.6.119	T1
			"F8 3A F0 39", "pldw	[r10,r9,lsl #3]",				// 1111 1000 00x1 xxxx 1111 0000 00xx xxxx	// A8.6.119	T1
			"F8 3A F0 09", "pldw	[r10,r9]",						// 1111 1000 00x1 xxxx 1111 0000 00xx xxxx	// A8.6.119	T1
			"F9 9A F9 87", "pli	[r10,#0x987]",						// 1111 1000 1001 xxxx 1111 xxxx xxxx xxxx	// A8.6.120	T1
			"F9 1A FC 71", "pli	[r10,#-0x71]",						// 1111 1001 0001 xxxx 1111 1100 xxxx xxxx	// A8.6.120	T2
			"F9 1F F9 87", "pli	[pc,#-0x987] ; 0xfffff679",			// 1111 1001 x001 1111 1111 xxxx xxxx xxxx	// A8.6.120	T3
			"F9 9F F9 87", "pli	[pc,#0x987] ; 0x987",				// 1111 1001 x001 1111 1111 xxxx xxxx xxxx	// A8.6.120	T3
			"F9 1A F0 19", "pli	[r10,r9,lsl #1]",					// 1111 1001 0001 xxxx 1111 0000 00xx xxxx	// A8.6.121	T1
			"F9 1A F0 39", "pli	[r10,r9,lsl #3]",					// 1111 1001 0001 xxxx 1111 0000 00xx xxxx	// A8.6.121	T1
			"F9 1A F0 09", "pli	[r10,r9]",							// 1111 1001 0001 xxxx 1111 0000 00xx xxxx	// A8.6.121	T1
			"E8 BD 82 40", "pop.w	{r6,r9,pc}",					// 1110 1000 1011 1101 xx.x xxxx xxxx xxxx	// A8.6.122	T2
			"F8 5D EB 04", "pop.w	{lr}",							// 1111 1000 0101 1101 xxxx 1011 0000 0100	// A8.6.122 T3
			"E8 AD 12 40", "push.w	{r6,r9,r12}",					// 1110 1000 1010 1101 .x.x xxxx xxxx xxxx	// A8.6.123	T2
			"F8 4D ED 04", "push.w	{lr}",							// 1111 1000 0100 1101 xxxx 1101 0000 0100	// A8.6.123	T3
			"FA 89 F5 8A", "qadd	r5,r10,r9",						// 1111 1010 1000 xxxx 1111 xxxx 1000 xxxx	// A8.6.124	T1
			"FA 99 F5 1A", "qadd16	r5,r9,r10",						// 1111 1010 1001 xxxx 1111 xxxx 0001 xxxx	// A8.6.125	T1
			"FA 89 F5 1A", "qadd8	r5,r9,r10",						// 1111 1010 1000 xxxx 1111 xxxx 0001 xxxx  // A8.6.126	T1
			"FA A9 F5 1A", "qasx	r5,r9,r10",						// 1111 1010 1010 xxxx 1111 xxxx 0001 xxxx  // A8.6.127	T1
			"FA 89 F5 9A", "qdadd	r5,r10,r9",						// 1111 1010 1000 xxxx 1111 xxxx 1001 xxxx	// A8.6.128	T1
			"FA 89 F5 BA", "qdsub	r5,r10,r9",						// 1111 1010 1000 xxxx 1111 xxxx 1011 xxxx	// A8.6.129	T1
			"FA E9 F5 1A", "qsax	r5,r9,r10",						// 1111 1010 1110 xxxx 1111 xxxx 0001 xxxx  // A8.6.130	T1
			"FA 89 F5 AA", "qsub	r5,r10,r9",						// 1111 1010 1000 xxxx 1111 xxxx 101A xxxx	// A8.6.131	T1
			"FA D9 F5 1A", "qsub16	r5,r9,r10",						// 1111 1010 1101 xxxx 1111 xxxx 0001 xxxx	// A8.6.132	T1
			"FA C9 F5 1A", "qsub8	r5,r9,r10",						// 1111 1010 1100 xxxx 1111 xxxx 0001 xxxx  // A8.6.133	T1
			"FA 99 F5 A9", "rbit	r5,r9",							// 1111 1010 1001 xxxx 1111 xxxx 1010 xxxx	// A8.6.134	T1
			"FA 99 F5 89", "rev.w	r5,r9",							// 1111 1010 1001 xxxx 1111 xxxx 1000 xxxx	// A8.6.135	T1
			"FA 99 F5 99", "rev16.w	r5,r9",							// 1111 1010 1001 xxxx 1111 xxxx 1001 xxxx	// A8.6.136	T2
			"FA 99 F5 B9", "revsh.w	r5,r9",							// 1111 1010 1001 xxxx 1111 xxxx 1011 xxxx	// A8.6.137	T2
			"E8 1A C0 00", "rfedb	r10",							// 1110 1000 00x1 xxxx ::.. .... .... ....	// B6.1.8	T1
			"E8 3A C0 00", "rfedb	r10!",							// 1110 1000 00x1 xxxx ::.. .... .... ....	// B6.1.8	T1
			"E9 9A C0 00", "rfeia	r10",							// 1110 1001 10x1 xxxx ::.. .... .... ....	// B6.1.8	T1
			"E9 BA C0 00", "rfeia	r10!",							// 1110 1001 10x1 xxxx ::.. .... .... ....	// B6.1.8	T1
			"EA 4F 45 79", "ror	r5,r9,#17",							// 1110 1010 0100 1111 .xxx xxxx xx11 xxxx	// A8.6.139	T1
			"EA 4F 06 B8", "ror	r6,r8,#2",							// 1110 1010 0100 1111 .xxx xxxx xx11 xxxx	// A8.6.139	T1
			"FA 69 F5 0A", "ror.w	r5,r9,r10",						// 1111 1010 0110 xxxx 1111 xxxx 0000 xxxx	// A8.6.140	T2
			"EA 5F 45 79", "rors	r5,r9,#17",						// 1110 1010 0101 1111 .xxx xxxx xx11 xxxx	// A8.6.139	T1
			"EA 5F 06 B8", "rors	r6,r8,#2",						// 1110 1010 0101 1111 .xxx xxxx xx11 xxxx	// A8.6.139	T1
			"FA 79 F5 0A", "rors.w	r5,r9,r10",						// 1111 1010 0111 xxxx 1111 xxxx 0000 xxxx	// A8.6.140	T2
			"EA 4F 06 38", "rrx	r6,r8",								// 1110 1010 0100 1111 .000 xxxx 0011 xxxx	// A8.6.141	T1
			"EA 5F 06 38", "rrxs	r6,r8",							// 1110 1010 0100 1111 .000 xxxx 0011 xxxx	// A8.6.141	T1
			"F1 CA 05 71", "rsb.w	r5,r10,#0x71",					// 1111 0x01 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 CB 06 F7", "rsb.w	r6,r11,#0xf7",					// 1111 0x01 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 C9 14 78", "rsb.w	r4,r9,#0x780078",				// 1111 0x01 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 C8 13 FC", "rsb.w	r3,r8,#0xfc00fc",				// 1111 0x01 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 C7 25 64", "rsb.w	r5,r7,#0x64006400",				// 1111 0x01 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 C6 25 E3", "rsb.w	r5,r6,#0xe300e300",				// 1111 0x01 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 C7 46 60", "rsb.w	r6,r7,#0xe0000000",				// 1111 0x01 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 C8 47 E0", "rsb.w	r7,r8,#0x70000000",				// 1111 0x01 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F5 CA 05 60", "rsb.w	r5,r10,#0xe00000",				// 1111 0x01 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F5 CA 45 60", "rsb.w	r5,r10,#0xe000",				// 1111 0x01 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F5 CA 65 60", "rsb.w	r5,r10,#0xe00",					// 1111 0x01 1100 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"EB C9 05 0A", "rsb	r5,r9,r10",							// 1110 1011 1100 xxxx .xxx xxxx xxxx xxxx	// A8.6.143	T1
			"EB C8 14 A9", "rsb	r4,r8,r9,asr #6",					// 1110 1011 1100 xxxx .xxx xxxx xxxx xxxx	// A8.6.143	T1
			"EB C7 03 48", "rsb	r3,r7,r8,lsl #1",					// 1110 1011 1100 xxxx .xxx xxxx xxxx xxxx	// A8.6.143	T1
			"EB C6 02 17", "rsb	r2,r6,r7,lsr #32",					// 1110 1011 1100 xxxx .xxx xxxx xxxx xxxx	// A8.6.143	T1
			"EB C9 75 F8", "rsb	r5,r9,r8,ror #31",					// 1110 1011 1100 xxxx .xxx xxxx xxxx xxxx	// A8.6.143	T1
			"EB C8 05 39", "rsb	r5,r8,r9,rrx",						// 1110 1011 1100 xxxx .xxx xxxx xxxx xxxx	// A8.6.143	T1
			"F1 DA 05 71", "rsbs.w	r5,r10,#0x71",					// 1111 0x01 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 DB 06 F7", "rsbs.w	r6,r11,#0xf7",					// 1111 0x01 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 D9 14 78", "rsbs.w	r4,r9,#0x780078",				// 1111 0x01 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 D8 13 FC", "rsbs.w	r3,r8,#0xfc00fc",				// 1111 0x01 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 D7 25 64", "rsbs.w	r5,r7,#0x64006400",				// 1111 0x01 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 D6 25 E3", "rsbs.w	r5,r6,#0xe300e300",				// 1111 0x01 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 D7 46 60", "rsbs.w	r6,r7,#0xe0000000",				// 1111 0x01 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F1 D8 47 E0", "rsbs.w	r7,r8,#0x70000000",				// 1111 0x01 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F5 DA 05 60", "rsbs.w	r5,r10,#0xe00000",				// 1111 0x01 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F5 DA 45 60", "rsbs.w	r5,r10,#0xe000",				// 1111 0x01 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"F5 DA 65 60", "rsbs.w	r5,r10,#0xe00",					// 1111 0x01 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.142	T2
			"EB D9 05 0A", "rsbs	r5,r9,r10",						// 1110 1011 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.143	T1
			"EB D8 14 A9", "rsbs	r4,r8,r9,asr #6",				// 1110 1011 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.143	T1
			"EB D7 03 48", "rsbs	r3,r7,r8,lsl #1",				// 1110 1011 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.143	T1
			"EB D6 02 17", "rsbs	r2,r6,r7,lsr #32",				// 1110 1011 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.143	T1
			"EB D9 75 F8", "rsbs	r5,r9,r8,ror #31",				// 1110 1011 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.143	T1
			"EB D8 05 39", "rsbs	r5,r8,r9,rrx",					// 1110 1011 1101 xxxx 0xxx xxxx xxxx xxxx	// A8.6.143	T1
			"FA 99 F5 0A", "sadd16	r5,r9,r10",						// 1111 1010 1001 xxxx 1111 xxxx 0000 xxxx  // A8.6.148	T1
			"FA 89 F5 0A", "sadd8	r5,r9,r10",						// 1111 1010 1000 xxxx 1111 xxxx 0000 xxxx  // A8.6.149	T1
			"FA A9 F5 0A", "sasx	r5,r9,r10",						// 1111 1010 1010 xxxx 1111 xxxx 0000 xxxx  // A8.6.150	T1
			"F1 6A 05 71", "sbc	r5,r10,#0x71",						// 1111 0x01 0110 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 6B 06 F7", "sbc	r6,r11,#0xf7",						// 1111 0x01 0110 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 69 14 78", "sbc	r4,r9,#0x780078",					// 1111 0x01 0110 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 68 13 FC", "sbc	r3,r8,#0xfc00fc",					// 1111 0x01 0110 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 67 25 64", "sbc	r5,r7,#0x64006400",					// 1111 0x01 0110 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 66 25 E3", "sbc	r5,r6,#0xe300e300",					// 1111 0x01 0110 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 67 46 60", "sbc	r6,r7,#0xe0000000",					// 1111 0x01 0110 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 68 47 E0", "sbc	r7,r8,#0x70000000",					// 1111 0x01 0110 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F5 6A 05 60", "sbc	r5,r10,#0xe00000",					// 1111 0x01 0110 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F5 6A 45 60", "sbc	r5,r10,#0xe000",					// 1111 0x01 0110 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F5 6A 65 60", "sbc	r5,r10,#0xe00",						// 1111 0x01 0110 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"EB 69 05 0A", "sbc.w	r5,r9,r10",						// 1110 1011 0110 xxxx .xxx xxxx xxxx xxxx	// A8.6.152	T2
			"EB 68 14 A9", "sbc.w	r4,r8,r9,asr #6",				// 1110 1011 0110 xxxx .xxx xxxx xxxx xxxx	// A8.6.152	T2
			"EB 67 03 48", "sbc.w	r3,r7,r8,lsl #1",				// 1110 1011 0110 xxxx .xxx xxxx xxxx xxxx	// A8.6.152	T2
			"EB 66 02 17", "sbc.w	r2,r6,r7,lsr #32",				// 1110 1011 0110 xxxx .xxx xxxx xxxx xxxx	// A8.6.152	T2
			"EB 69 75 F8", "sbc.w	r5,r9,r8,ror #31",				// 1110 1011 0110 xxxx .xxx xxxx xxxx xxxx	// A8.6.152	T2
			"EB 68 05 39", "sbc.w	r5,r8,r9,rrx",					// 1110 1011 0110 xxxx .xxx xxxx xxxx xxxx	// A8.6.152	T2
			"F1 7A 05 71", "sbcs	r5,r10,#0x71",					// 1111 0x01 0111 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 7B 06 F7", "sbcs	r6,r11,#0xf7",					// 1111 0x01 0111 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 79 14 78", "sbcs	r4,r9,#0x780078",				// 1111 0x01 0111 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 78 13 FC", "sbcs	r3,r8,#0xfc00fc",				// 1111 0x01 0111 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 77 25 64", "sbcs	r5,r7,#0x64006400",				// 1111 0x01 0111 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 76 25 E3", "sbcs	r5,r6,#0xe300e300",				// 1111 0x01 0111 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 77 46 60", "sbcs	r6,r7,#0xe0000000",				// 1111 0x01 0111 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F1 78 47 E0", "sbcs	r7,r8,#0x70000000",				// 1111 0x01 0111 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F5 7A 05 60", "sbcs	r5,r10,#0xe00000",				// 1111 0x01 0111 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F5 7A 45 60", "sbcs	r5,r10,#0xe000",				// 1111 0x01 0111 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"F5 7A 65 60", "sbcs	r5,r10,#0xe00",					// 1111 0x01 0111 xxxx 0xxx xxxx xxxx xxxx	// A8.6.151	T1
			"EB 79 05 0A", "sbcs.w	r5,r9,r10",						// 1110 1011 0111 xxxx .xxx xxxx xxxx xxxx	// A8.6.152	T2
			"EB 78 14 A9", "sbcs.w	r4,r8,r9,asr #6",				// 1110 1011 0111 xxxx .xxx xxxx xxxx xxxx	// A8.6.152	T2
			"EB 77 03 48", "sbcs.w	r3,r7,r8,lsl #1",				// 1110 1011 0111 xxxx .xxx xxxx xxxx xxxx	// A8.6.152	T2
			"EB 76 02 17", "sbcs.w	r2,r6,r7,lsr #32",				// 1110 1011 0111 xxxx .xxx xxxx xxxx xxxx	// A8.6.152	T2
			"EB 79 75 F8", "sbcs.w	r5,r9,r8,ror #31",				// 1110 1011 0111 xxxx .xxx xxxx xxxx xxxx	// A8.6.152	T2
			"EB 78 05 39", "sbcs.w	r5,r8,r9,rrx",					// 1110 1011 0111 xxxx .xxx xxxx xxxx xxxx	// A8.6.152	T2
			"F3 47 05 1F", "sbfx	r5,r7,#0,#32",					// 1111 0.11 0100 xxxx 0xxx xxxx xx.x xxxx	// A8.6.154	T1
			"F3 48 06 59", "sbfx	r6,r8,#1,#26",					// 1111 0.11 0100 xxxx 0xxx xxxx xx.x xxxx	// A8.6.154	T1
			"FB 99 F5 FA", "sdiv	r5,r9,r10",						// 1111 1011 1001 xxxx :::: xxxx 1111 xxxx	// A8.6.155	T1
			"FA A9 F5 8A", "sel	r5,r9,r10",							// 1111 1010 1010 xxxx 1111 xxxx 1000 xxxx	// A8.6.156	T1
			"F3 AF 80 04", "sev.w",									// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.158	T1
			"FA 99 F5 2A", "shadd16	r5,r9,r10",						// 1111 1010 1001 xxxx 1111 xxxx 0010 xxxx  // A8.6.159	T1
			"FA 89 F5 2A", "shadd8	r5,r9,r10",						// 1111 1010 1000 xxxx 1111 xxxx 0010 xxxx  // A8.6.160	T1
			"FA A9 F5 2A", "shasx	r5,r9,r10",						// 1111 1010 1010 xxxx 1111 xxxx 0010 xxxx  // A8.6.161	T1
			"FA E9 F5 2A", "shsax	r5,r9,r10",						// 1111 1010 1110 xxxx 1111 xxxx 0010 xxxx  // A8.6.162	T1
			"FA D9 F5 2A", "shsub16	r5,r9,r10",						// 1111 1010 1101 xxxx 1111 xxxx 0010 xxxx  // A8.6.163	T1
			"FA C9 F5 2A", "shsub8	r5,r9,r10",						// 1111 1010 1100 xxxx 1111 xxxx 0010 xxxx  // A8.6.164	T1
			"F7 FE 80 00", "smc	#0xe",								// 1111 0111 1111 xxxx 1000 .... .... ....	// B6.1.9	T1
			"FB 19 85 0A", "smlabb	r5,r9,r10,r8",					// 1111 1011 0001 xxxx xxxx xxxx 00xx xxxx	// A8.6.166	T1
			"FB 19 85 1A", "smlabt	r5,r9,r10,r8",					// 1111 1011 0001 xxxx xxxx xxxx 00xx xxxx	// A8.6.166	T1
			"FB 19 85 2A", "smlatb	r5,r9,r10,r8",					// 1111 1011 0001 xxxx xxxx xxxx 00xx xxxx	// A8.6.166	T1
			"FB 19 85 3A", "smlatt	r5,r9,r10,r8",					// 1111 1011 0001 xxxx xxxx xxxx 00xx xxxx	// A8.6.166	T1
			"FB 29 85 0A", "smlad	r5,r9,r10,r8",					// 1111 1011 0010 xxxx xxxx xxxx 00xx xxxx	// A8.6.167	T1
			"FB 29 85 1A", "smladx	r5,r9,r10,r8",					// 1111 1011 0010 xxxx xxxx xxxx 00xx xxxx	// A8.6.167	T1
			"FB CA 56 09", "smlal	r5,r6,r10,r9",					// 1111 1011 1100 xxxx xxxx xxxx 0000 xxxx	// A8.6.168	T1
			"FB CA 56 89", "smlalbb	r5,r6,r10,r9",					// 1111 1011 1100 xxxx xxxx xxxx 10xx xxxx	// A8.6.169	T1
			"FB CA 56 99", "smlalbt	r5,r6,r10,r9",					// 1111 1011 1100 xxxx xxxx xxxx 10xx xxxx	// A8.6.169	T1
			"FB CA 56 A9", "smlaltb	r5,r6,r10,r9",					// 1111 1011 1100 xxxx xxxx xxxx 10xx xxxx	// A8.6.169	T1
			"FB CA 56 B9", "smlaltt	r5,r6,r10,r9",					// 1111 1011 1100 xxxx xxxx xxxx 10xx xxxx	// A8.6.169	T1
			"FB CA 56 C9", "smlald	r5,r6,r10,r9",					// 1111 1011 1100 xxxx xxxx xxxx 110x xxxx	// A8.6.170	T1
			"FB CA 56 D9", "smlaldx	r5,r6,r10,r9",					// 1111 1011 1100 xxxx xxxx xxxx 110x xxxx	// A8.6.170	T1
			"FB 39 85 0A", "smlawb	r5,r9,r10,r8",					// 1111 1011 0011 xxxx xxxx xxxx 000x xxxx	// A8.6.171	T1
			"FB 39 85 1A", "smlawt	r5,r9,r10,r8",					// 1111 1011 0011 xxxx xxxx xxxx 000x xxxx	// A8.6.171	T1
			"FB 49 85 0A", "smlsd	r5,r9,r10,r8",					// 1111 1011 0100 xxxx xxxx xxxx 000x xxxx	// A8.6.172	T1
			"FB 49 85 1A", "smlsdx	r5,r9,r10,r8",					// 1111 1011 0100 xxxx xxxx xxxx 000x xxxx	// A8.6.172	T1
			"FB DA 56 C9", "smlsld	r5,r6,r10,r9",					// 1111 1011 1101 xxxx xxxx xxxx 110x xxxx	// A8.6.173	T1
			"FB DA 56 D9", "smlsldx	r5,r6,r10,r9",					// 1111 1011 1101 xxxx xxxx xxxx 110x xxxx	// A8.6.173	T1
			"FB 59 85 0A", "smmla	r5,r9,r10,r8",					// 1111 1011 0101 xxxx xxxx xxxx 000x xxxx	// A8.6.174	T1
			"FB 59 85 1A", "smmlar	r5,r9,r10,r8",					// 1111 1011 0101 xxxx xxxx xxxx 000x xxxx	// A8.6.174	T1
			"FB 69 85 0A", "smmls	r5,r9,r10,r8",					// 1111 1011 0110 xxxx xxxx xxxx 000x xxxx	// A8.6.175	T1
			"FB 69 85 1A", "smmlsr	r5,r9,r10,r8",					// 1111 1011 0110 xxxx xxxx xxxx 000x xxxx	// A8.6.175	T1
			"FB 59 F5 0A", "smmul	r5,r9,r10",						// 1111 1011 0101 xxxx 1111 xxxx 000x xxxx	// A8.6.176	T1
			"FB 59 F5 1A", "smmulr	r5,r9,r10",						// 1111 1011 0101 xxxx 1111 xxxx 000x xxxx	// A8.6.176	T1
			"FB 29 F5 0A", "smuad	r5,r9,r10",						// 1111 1011 0010 xxxx 1111 xxxx 000x xxxx	// A8.6.177	T1
			"FB 29 F5 1A", "smuadx	r5,r9,r10",						// 1111 1011 0010 xxxx 1111 xxxx 000x xxxx	// A8.6.177	T1
			"FB 19 F5 0A", "smulbb	r5,r9,r10",						// 1111 1011 0001 xxxx 1111 xxxx 00xx xxxx	// A8.6.178	T1
			"FB 19 F5 1A", "smulbt	r5,r9,r10",						// 1111 1011 0001 xxxx 1111 xxxx 00xx xxxx	// A8.6.178	T1
			"FB 19 F5 2A", "smultb	r5,r9,r10",						// 1111 1011 0001 xxxx 1111 xxxx 00xx xxxx	// A8.6.178	T1
			"FB 19 F5 3A", "smultt	r5,r9,r10",						// 1111 1011 0001 xxxx 1111 xxxx 00xx xxxx	// A8.6.178	T1
			"FB 8A 56 09", "smull	r5,r6,r10,r9",					// 1111 1011 1000 xxxx xxxx xxxx 0000 xxxx	// A8.6.179	T1
			"FB 39 F5 0A", "smulwb	r5,r9,r10",						// 1111 1011 0011 xxxx 1111 xxxx 000x xxxx	// A8.6.180	T1
			"FB 39 F5 1A", "smulwt	r5,r9,r10",						// 1111 1011 0011 xxxx 1111 xxxx 000x xxxx	// A8.6.180	T1
			"FB 49 F5 0A", "smusd	r5,r9,r10",						// 1111 1011 0100 xxxx 1111 xxxx 000x xxxx	// A8.6.181	T1
			"FB 49 F5 1A", "smusdx	r5,r9,r10",						// 1111 1011 0100 xxxx 1111 xxxx 000x xxxx	// A8.6.181	T1
			"E8 2D C0 13", "srsdb	sp!,#0x13",						// 1110 1000 00x0 ::.: ::.. .... ...x xxxx	// B6.1.10	T1
			"E8 0D C0 13", "srsdb	sp,#0x13",						// 1110 1000 00x0 ::.: ::.. .... ...x xxxx	// B6.1.10	T1
			"E9 AD C0 13", "srsia	sp!,#0x13",						// 1110 1001 10x0 ::.: ::.. .... ...x xxxx	// B6.1.10	T2
			"E9 8D C0 13", "srsia	sp,#0x13",						// 1110 1001 10x0 ::.: ::.. .... ...x xxxx	// B6.1.10	T2
			"F3 0A 05 1C", "ssat	r5,#29,r10",					// 1111 0.11 00x0 xxxx 0xxx xxxx xx.x xxxx	// A8.6.183	T1
			"F3 2A 05 5C", "ssat	r5,#29,r10,asr #1",				// 1111 0.11 00x0 xxxx 0xxx xxxx xx.x xxxx	// A8.6.183	T1
			"F3 2A 75 9C", "ssat	r5,#29,r10,asr #30",			// 1111 0.11 00x0 xxxx 0xxx xxxx xx.x xxxx	// A8.6.183	T1
			"F3 0A 05 5C", "ssat	r5,#29,r10,lsl #1",				// 1111 0.11 00x0 xxxx 0xxx xxxx xx.x xxxx	// A8.6.183	T1
			"F3 0A 75 DC", "ssat	r5,#29,r10,lsl #31",			// 1111 0.11 00x0 xxxx 0xxx xxxx xx.x xxxx	// A8.6.183	T1
			"F3 2A 05 0C", "ssat16	r5,#13,r10",					// 1111 0.11 0010 xxxx 0000 xxxx 00.. xxxx	// A8.6.184	T1
			"FA E9 F5 0A", "ssax	r5,r9,r10",						// 1111 1010 1110 xxxx 1111 xxxx 0000 xxxx  // A8.6.185	T1
			"FA D9 F5 0A", "ssub16	r5,r9,r10",						// 1111 1010 1101 xxxx 1111 xxxx 0000 xxxx  // A8.6.186	T1
			"FA C9 F5 0A", "ssub8	r5,r9,r10",						// 1111 1010 1100 xxxx 1111 xxxx 0000 xxxx  // A8.6.187	T1
			"ED 0A B9 21", "stc	p9,c11,[r10,#-0x84]",				// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"ED 2A B9 21", "stc	p9,c11,[r10,#-0x84]!",				// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"ED 8A B9 21", "stc	p9,c11,[r10,#0x84]",				// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"ED AA B9 21", "stc	p9,c11,[r10,#0x84]!",				// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"EC 2A B9 21", "stc	p9,c11,[r10],#-0x84",				// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"EC AA B9 21", "stc	p9,c11,[r10],#0x84",				// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"EC 8A B9 00", "stc	p9,c11,[r10],{0}",					// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"EC 8A B9 FF", "stc	p9,c11,[r10],{255}",				// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"FD 0A B9 21", "stc2	p9,c11,[r10,#-0x84]",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FD 2A B9 21", "stc2	p9,c11,[r10,#-0x84]!",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FD 8A B9 21", "stc2	p9,c11,[r10,#0x84]",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FD AA B9 21", "stc2	p9,c11,[r10,#0x84]!",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FC 2A B9 21", "stc2	p9,c11,[r10],#-0x84",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FC AA B9 21", "stc2	p9,c11,[r10],#0x84",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FC 8A B9 00", "stc2	p9,c11,[r10],{0}",				// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FC 8A B9 FF", "stc2	p9,c11,[r10],{255}",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FD 4A B9 21", "stc2l	p9,c11,[r10,#-0x84]",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FD 6A B9 21", "stc2l	p9,c11,[r10,#-0x84]!",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FD CA B9 21", "stc2l	p9,c11,[r10,#0x84]",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FD EA B9 21", "stc2l	p9,c11,[r10,#0x84]!",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FC 6A B9 21", "stc2l	p9,c11,[r10],#-0x84",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FC EA B9 21", "stc2l	p9,c11,[r10],#0x84",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FC CA B9 00", "stc2l	p9,c11,[r10],{0}",				// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"FC CA B9 FF", "stc2l	p9,c11,[r10],{255}",			// 1111 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T2
			"ED 4A B9 21", "stcl	p9,c11,[r10,#-0x84]",			// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"ED 6A B9 21", "stcl	p9,c11,[r10,#-0x84]!",			// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"ED CA B9 21", "stcl	p9,c11,[r10,#0x84]",			// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"ED EA B9 21", "stcl	p9,c11,[r10,#0x84]!",			// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"EC 6A B9 21", "stcl	p9,c11,[r10],#-0x84",			// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"EC EA B9 21", "stcl	p9,c11,[r10],#0x84",			// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"EC CA B9 00", "stcl	p9,c11,[r10],{0}",				// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"EC CA B9 FF", "stcl	p9,c11,[r10],{255}",			// 1110 110x xxx0 xxxx xxxx xxxx xxxx xxxx	// A8.6.188 T1
			"E8 AA 42 40", "stm.w	r10!,{r6,r9,lr}",				// 1110 1000 10x0 xxxx xx.x xxxx xxxx xxxx	// A8.6.189	T2
			"E8 8A 42 40", "stm.w	r10,{r6,r9,lr}",				// 1110 1000 10x0 xxxx xx.x xxxx xxxx xxxx	// A8.6.189	T2
			"E9 2A 42 40", "stmdb	r10!,{r6,r9,lr}",				// 1110 1001 00x0 xxxx xx.x xxxx xxxx xxxx	// A8.6.191	T1
			"E9 0A 42 40", "stmdb	r10,{r6,r9,lr}",				// 1110 1001 00x0 xxxx xx.x xxxx xxxx xxxx	// A8.6.191	T1
			"F8 CA 50 00", "str.w	r5,[r10]",						// 1111 1000 1100 xxxx xxxx xxxx xxxx xxxx	// A8.6.193	T3
			"F8 C6 47 89", "str.w	r4,[r6,#0x789]",				// 1111 1000 1100 xxxx xxxx xxxx xxxx xxxx	// A8.6.193	T3
			"F8 4A 5C 80", "str	r5,[r10,#-0x80]",					// 1111 1000 0100 xxxx xxxx 1xxx xxxx xxxx	// A8.6.193	T4
			"F8 4A 5A 82", "str	r5,[r10],#0x82",					// 1111 1000 0100 xxxx xxxx 1xxx xxxx xxxx	// A8.6.193	T4
			"F8 4A 58 84", "str	r5,[r10],#-0x84",					// 1111 1000 0100 xxxx xxxx 1xxx xxxx xxxx	// A8.6.193	T4
			"F8 4A 5F 86", "str	r5,[r10,#0x86]!",					// 1111 1000 0100 xxxx xxxx 1xxx xxxx xxxx	// A8.6.193	T4
			"F8 4A 5D 88", "str	r5,[r10,#-0x88]!",					// 1111 1000 0100 xxxx xxxx 1xxx xxxx xxxx	// A8.6.193	T4
			"F8 4A 50 08", "str.w	r5,[r10,r8]",					// 1111 1000 0100 xxxx xxxx 0000 00xx xxxx	// A8.6.195	T2
			"F8 49 60 37", "str.w	r6,[r9,r7,lsl #3]",				// 1111 1000 0100 xxxx xxxx 0000 00xx xxxx	// A8.6.195	T2
			"F8 8A 50 00", "strb.w	r5,[r10]",						// 1111 1000 1000 xxxx xxxx xxxx xxxx xxxx	// A8.6.196	T2
			"F8 86 47 89", "strb.w	r4,[r6,#0x789]",				// 1111 1000 1000 xxxx xxxx xxxx xxxx xxxx	// A8.6.196	T2
			"F8 0A 5C 80", "strb	r5,[r10,#-0x80]",				// 1111 1000 0000 xxxx xxxx 1xxx xxxx xxxx	// A8.6.196	T3
			"F8 0A 5A 82", "strb	r5,[r10],#0x82",				// 1111 1000 0000 xxxx xxxx 1xxx xxxx xxxx	// A8.6.196	T3
			"F8 0A 58 84", "strb	r5,[r10],#-0x84",				// 1111 1000 0000 xxxx xxxx 1xxx xxxx xxxx	// A8.6.196	T3
			"F8 0A 5F 86", "strb	r5,[r10,#0x86]!",				// 1111 1000 0000 xxxx xxxx 1xxx xxxx xxxx	// A8.6.196	T3
			"F8 0A 5D 88", "strb	r5,[r10,#-0x88]!",				// 1111 1000 0000 xxxx xxxx 1xxx xxxx xxxx	// A8.6.196	T3
			"F8 0A 50 08", "strb.w	r5,[r10,r8]",					// 1111 1000 0000 xxxx xxxx 0000 00xx xxxx	// A8.6.198	T2
			"F8 09 60 37", "strb.w	r6,[r9,r7,lsl #3]",				// 1111 1000 0000 xxxx xxxx 0000 00xx xxxx	// A8.6.198	T2
			"F8 0A 5E 00", "strbt	r5,[r10]",						// 1111 1000 0000 xxxx xxxx 1110 xxxx xxxx	// A8.6.199	T1
			"F8 09 6E 84", "strbt	r6,[r9,#0x84]",					// 1111 1000 0000 xxxx xxxx 1110 xxxx xxxx	// A8.6.199	T1
			"E9 4A 67 84", "strd	r6,r7,[r10,#-0x210]",			// 1110 100x x1x0 xxxx xxxx xxxx xxxx xxxx	// A8.6.200	T1
			"E9 6A 67 85", "strd	r6,r7,[r10,#-0x214]!",			// 1110 100x x1x0 xxxx xxxx xxxx xxxx xxxx	// A8.6.200	T1
			"E9 CA 67 84", "strd	r6,r7,[r10,#0x210]",			// 1110 100x x1x0 xxxx xxxx xxxx xxxx xxxx	// A8.6.200	T1
			"E9 EA 67 85", "strd	r6,r7,[r10,#0x214]!",			// 1110 100x x1x0 xxxx xxxx xxxx xxxx xxxx	// A8.6.200	T1
			"E9 4A 67 00", "strd	r6,r7,[r10]",					// 1110 100x x1x0 xxxx xxxx xxxx xxxx xxxx	// A8.6.200	T1
			"E8 6A 67 84", "strd	r6,r7,[r10],#-0x210",			// 1110 100x x1x0 xxxx xxxx xxxx xxxx xxxx	// A8.6.200	T1
			"E8 EA 67 85", "strd	r6,r7,[r10],#0x214",			// 1110 100x x1x0 xxxx xxxx xxxx xxxx xxxx	// A8.6.200	T1
			"E8 4A 95 00", "strex	r5,r9,[r10]",					// 1110 1000 0101 xxxx xxxx :::: xxxx xxxx	// A8.6.202	T1
			"E8 49 86 87", "strex	r6,r8,[r9,#0x21c]",				// 1110 1000 0101 xxxx xxxx :::: xxxx xxxx	// A8.6.202	T1
			"E8 CA 9F 45", "strexb 	r5,r9,[r10]",					// 1110 1000 1100 xxxx xxxx :::: 0100 ::::	// A8.6.203	T1
			"E8 C8 67 74", "strexd	r4,r6,r7,[r8]",					// 1110 1000 1100 xxxx xxxx xxxx 0111 ::::	// A8.6.204	T1
			"E8 C9 8F 53", "strexh	r3,r8,[r9]",					// 1110 1000 1100 xxxx xxxx :::: 0101 ::::	// A8.6.205	T1
			"F8 AA 50 00", "strh.w	r5,[r10]",						// 1111 1000 1010 xxxx xxxx xxxx xxxx xxxx	// A8.6.206	T2
			"F8 A6 47 89", "strh.w	r4,[r6,#0x789]",				// 1111 1000 1010 xxxx xxxx xxxx xxxx xxxx	// A8.6.206	T2
			"F8 2A 5C 80", "strh	r5,[r10,#-0x80]",				// 1111 1000 0010 xxxx xxxx 1xxx xxxx xxxx	// A8.6.206	T3
			"F8 2A 5A 82", "strh	r5,[r10],#0x82",				// 1111 1000 0010 xxxx xxxx 1xxx xxxx xxxx	// A8.6.206	T3
			"F8 2A 58 84", "strh	r5,[r10],#-0x84",				// 1111 1000 0010 xxxx xxxx 1xxx xxxx xxxx	// A8.6.206	T3
			"F8 2A 5F 86", "strh	r5,[r10,#0x86]!",				// 1111 1000 0010 xxxx xxxx 1xxx xxxx xxxx	// A8.6.206	T3
			"F8 2A 5D 88", "strh	r5,[r10,#-0x88]!",				// 1111 1000 0010 xxxx xxxx 1xxx xxxx xxxx	// A8.6.206	T3
			"F8 2A 50 08", "strh.w	r5,[r10,r8]",					// 1111 1000 0010 xxxx xxxx 0000 00xx xxxx	// A8.6.208	T2
			"F8 29 60 37", "strh.w	r6,[r9,r7,lsl #3]",				// 1111 1000 0010 xxxx xxxx 0000 00xx xxxx	// A8.6.208	T2
			"F8 2A 5E 00", "strht	r5,[r10]",						// 1111 1000 0010 xxxx xxxx 1110 xxxx xxxx	// A8.6.209	T1
			"F8 29 6E 84", "strht	r6,[r9,#0x84]",					// 1111 1000 0010 xxxx xxxx 1110 xxxx xxxx	// A8.6.209	T1
			"F8 4A 5E 00", "strt	r5,[r10]",						// 1111 1000 0100 xxxx xxxx 1110 xxxx xxxx	// A8.6.210	T1
			"F8 46 4E 89", "strt	r4,[r6,#0x89]",					// 1111 1000 0100 xxxx xxxx 1110 xxxx xxxx	// A8.6.210	T1
			"F2 AF 05 71", "sub	r5,pc,#0x71",						// 1111 0x10 1010 1111 0xxx xxxx xxxx xxxx	// A8.6.10	T2
			"F2 AF 36 72", "sub	r6,pc,#0x372",						// 1111 0x10 1010 1111 0xxx xxxx xxxx xxxx	// A8.6.10	T2
			"F6 AF 47 78", "sub	r7,pc,#0xc78",						// 1111 0x10 1010 1111 0xxx xxxx xxxx xxxx	// A8.6.10	T2
			"F1 AA 05 71", "sub.w	r5,r10,#0x71",					// 1111 0x01 1010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 AB 06 F7", "sub.w	r6,r11,#0xf7",					// 1111 0x01 1010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 A9 14 78", "sub.w	r4,r9,#0x780078",				// 1111 0x01 1010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 A8 13 FC", "sub.w	r3,r8,#0xfc00fc",				// 1111 0x01 1010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 A7 25 64", "sub.w	r5,r7,#0x64006400",				// 1111 0x01 1010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 A6 25 E3", "sub.w	r5,r6,#0xe300e300",				// 1111 0x01 1010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 A7 46 60", "sub.w	r6,r7,#0xe0000000",				// 1111 0x01 1010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 A8 47 E0", "sub.w	r7,r8,#0x70000000",				// 1111 0x01 1010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F5 AA 05 60", "sub.w	r5,r10,#0xe00000",				// 1111 0x01 1010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F5 AA 45 60", "sub.w	r5,r10,#0xe000",				// 1111 0x01 1010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F5 AA 65 60", "sub.w	r5,r10,#0xe00",					// 1111 0x01 1010 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"EB A9 05 0A", "sub.w	r5,r9,r10",						// 1110 1011 1010 xxxx .xxx xxxx xxxx xxxx	// A8.6.213	T3
			"EB A8 14 A9", "sub.w	r4,r8,r9,asr #6",				// 1110 1011 1010 xxxx .xxx xxxx xxxx xxxx	// A8.6.213	T3
			"EB A7 03 48", "sub.w	r3,r7,r8,lsl #1",				// 1110 1011 1010 xxxx .xxx xxxx xxxx xxxx	// A8.6.213	T3
			"EB A6 02 17", "sub.w	r2,r6,r7,lsr #32",				// 1110 1011 1010 xxxx .xxx xxxx xxxx xxxx	// A8.6.213	T3
			"EB A9 75 F8", "sub.w	r5,r9,r8,ror #31",				// 1110 1011 1010 xxxx .xxx xxxx xxxx xxxx	// A8.6.213	T3
			"EB A8 05 39", "sub.w	r5,r8,r9,rrx",					// 1110 1011 1010 xxxx .xxx xxxx xxxx xxxx	// A8.6.213	T3
			"F1 AD 05 71", "sub.w	r5,sp,#0x71",					// 1111 0x01 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"F1 AD 06 F7", "sub.w	r6,sp,#0xf7",					// 1111 0x01 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"F1 AD 14 78", "sub.w	r4,sp,#0x780078",				// 1111 0x01 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"F1 AD 13 FC", "sub.w	r3,sp,#0xfc00fc",				// 1111 0x01 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"F1 AD 25 64", "sub.w	r5,sp,#0x64006400",				// 1111 0x01 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"F1 AD 25 E3", "sub.w	r5,sp,#0xe300e300",				// 1111 0x01 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"F1 AD 46 60", "sub.w	r6,sp,#0xe0000000",				// 1111 0x01 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"F1 AD 47 E0", "sub.w	r7,sp,#0x70000000",				// 1111 0x01 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"F5 AD 05 60", "sub.w	r5,sp,#0xe00000",				// 1111 0x01 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"F5 AD 45 60", "sub.w	r5,sp,#0xe000",					// 1111 0x01 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"F5 AD 65 60", "sub.w	r5,sp,#0xe00",					// 1111 0x01 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"EB AD 05 0A", "sub.w	r5,sp,r10",						// 1110 1011 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"EB AD 14 A9", "sub.w	r4,sp,r9,asr #6",				// 1110 1011 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"EB AD 03 48", "sub.w	r3,sp,r8,lsl #1",				// 1110 1011 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"EB AD 02 17", "sub.w	r2,sp,r7,lsr #32",				// 1110 1011 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"EB AD 75 F8", "sub.w	r5,sp,r8,ror #31",				// 1110 1011 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"EB AD 05 39", "sub.w	r5,sp,r9,rrx",					// 1110 1011 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.213	T3
			"F1 BA 05 71", "subs.w	r5,r10,#0x71",					// 1111 0x01 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 BB 06 F7", "subs.w	r6,r11,#0xf7",					// 1111 0x01 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 B9 14 78", "subs.w	r4,r9,#0x780078",				// 1111 0x01 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 B8 13 FC", "subs.w	r3,r8,#0xfc00fc",				// 1111 0x01 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 B7 25 64", "subs.w	r5,r7,#0x64006400",				// 1111 0x01 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 B6 25 E3", "subs.w	r5,r6,#0xe300e300",				// 1111 0x01 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 B7 46 60", "subs.w	r6,r7,#0xe0000000",				// 1111 0x01 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F1 B8 47 E0", "subs.w	r7,r8,#0x70000000",				// 1111 0x01 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F5 BA 05 60", "subs.w	r5,r10,#0xe00000",				// 1111 0x01 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F5 BA 45 60", "subs.w	r5,r10,#0xe000",				// 1111 0x01 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"F5 BA 65 60", "subs.w	r5,r10,#0xe00",					// 1111 0x01 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.211	T3
			"EB B9 05 0A", "subs.w	r5,r9,r10",						// 1110 1011 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.213	T2
			"EB B8 14 A9", "subs.w	r4,r8,r9,asr #6",				// 1110 1011 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.213	T2
			"EB B7 03 48", "subs.w	r3,r7,r8,lsl #1",				// 1110 1011 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.213	T2
			"EB B6 02 17", "subs.w	r2,r6,r7,lsr #32",				// 1110 1011 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.213	T2
			"EB B9 75 F8", "subs.w	r5,r9,r8,ror #31",				// 1110 1011 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.213	T2
			"EB B8 05 39", "subs.w	r5,r8,r9,rrx",					// 1110 1011 1011 xxxx 0xxx xxxx xxxx xxxx	// A8.6.213	T2
			"F1 BD 05 71", "subs.w	r5,sp,#0x71",					// 1111 0x01 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T2
			"F1 BD 06 F7", "subs.w	r6,sp,#0xf7",					// 1111 0x01 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T2
			"F1 BD 14 78", "subs.w	r4,sp,#0x780078",				// 1111 0x01 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T2
			"F1 BD 13 FC", "subs.w	r3,sp,#0xfc00fc",				// 1111 0x01 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T2
			"F1 BD 25 64", "subs.w	r5,sp,#0x64006400",				// 1111 0x01 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T2
			"F1 BD 25 E3", "subs.w	r5,sp,#0xe300e300",				// 1111 0x01 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T2
			"F1 BD 46 60", "subs.w	r6,sp,#0xe0000000",				// 1111 0x01 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T2
			"F1 BD 47 E0", "subs.w	r7,sp,#0x70000000",				// 1111 0x01 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T2
			"F5 BD 05 60", "subs.w	r5,sp,#0xe00000",				// 1111 0x01 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T2
			"F5 BD 45 60", "subs.w	r5,sp,#0xe000",					// 1111 0x01 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T2
			"F5 BD 65 60", "subs.w	r5,sp,#0xe00",					// 1111 0x01 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T2
			"EB BD 05 0A", "subs.w	r5,sp,r10",						// 1110 1011 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.216	T1
			"EB BD 14 A9", "subs.w	r4,sp,r9,asr #6",				// 1110 1011 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.216	T1
			"EB BD 03 48", "subs.w	r3,sp,r8,lsl #1",				// 1110 1011 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.216	T1
			"EB BD 02 17", "subs.w	r2,sp,r7,lsr #32",				// 1110 1011 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.216	T1
			"EB BD 75 F8", "subs.w	r5,sp,r8,ror #31",				// 1110 1011 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.216	T1
			"EB BD 05 39", "subs.w	r5,sp,r9,rrx",					// 1110 1011 1011 1101 0xxx xxxx xxxx xxxx	// A8.6.216	T1
			"F2 AD 05 71", "subw	r5,sp,#0x71",					// 1111 0x10 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T3
			"F2 AD 36 72", "subw	r6,sp,#0x372",					// 1111 0x10 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T3
			"F6 AD 47 78", "subw	r7,sp,#0xc78",					// 1111 0x10 1010 1101 0xxx xxxx xxxx xxxx	// A8.6.215	T3
			"FA 49 F5 8A", "sxtab	r5,r9,r10",						// 1111 1010 0100 xxxx 1111 xxxx 1.xx xxxx	// A8.6.220	T1
			"FA 49 F5 9A", "sxtab	r5,r9,r10,ror #8",				// 1111 1010 0100 xxxx 1111 xxxx 1.xx xxxx	// A8.6.220	T1
			"FA 49 F5 AA", "sxtab	r5,r9,r10,ror #16",				// 1111 1010 0100 xxxx 1111 xxxx 1.xx xxxx	// A8.6.220	T1
			"FA 49 F5 BA", "sxtab	r5,r9,r10,ror #24",				// 1111 1010 0100 xxxx 1111 xxxx 1.xx xxxx	// A8.6.220	T1
			"FA 29 F5 8A", "sxtab16	r5,r9,r10",						// 1111 1010 0010 xxxx 1111 xxxx 1.xx xxxx	// A8.6.221	T1
			"FA 29 F5 9A", "sxtab16	r5,r9,r10,ror #8",				// 1111 1010 0010 xxxx 1111 xxxx 1.xx xxxx	// A8.6.221	T1
			"FA 29 F5 AA", "sxtab16	r5,r9,r10,ror #16",				// 1111 1010 0010 xxxx 1111 xxxx 1.xx xxxx	// A8.6.221	T1
			"FA 29 F5 BA", "sxtab16	r5,r9,r10,ror #24",				// 1111 1010 0010 xxxx 1111 xxxx 1.xx xxxx	// A8.6.221	T1
			"FA 09 F5 8A", "sxtah	r5,r9,r10",						// 1111 1010 0000 xxxx 1111 xxxx 1.xx xxxx	// A8.6.222	T1
			"FA 09 F5 9A", "sxtah	r5,r9,r10,ror #8",				// 1111 1010 0000 xxxx 1111 xxxx 1.xx xxxx	// A8.6.222	T1
			"FA 09 F5 AA", "sxtah	r5,r9,r10,ror #16",				// 1111 1010 0000 xxxx 1111 xxxx 1.xx xxxx	// A8.6.222	T1
			"FA 09 F5 BA", "sxtah	r5,r9,r10,ror #24",				// 1111 1010 0000 xxxx 1111 xxxx 1.xx xxxx	// A8.6.222	T1
			"FA 4F F5 89", "sxtb.w	r5,r9",							// 1111 1010 0100 1111 1111 xxxx 1.xx xxxx	// A8.6.223	T2
			"FA 4F F5 99", "sxtb.w	r5,r9,ror #8",					// 1111 1010 0100 1111 1111 xxxx 1.xx xxxx	// A8.6.223	T2
			"FA 4F F5 A9", "sxtb.w	r5,r9,ror #16",					// 1111 1010 0100 1111 1111 xxxx 1.xx xxxx	// A8.6.223	T2
			"FA 4F F5 B9", "sxtb.w	r5,r9,ror #24",					// 1111 1010 0100 1111 1111 xxxx 1.xx xxxx	// A8.6.223	T2
			"FA 2F F5 89", "sxtb16	r5,r9",							// 1111 1010 0010 1111 1111 xxxx 1.xx xxxx	// A8.6.224	T1
			"FA 2F F5 99", "sxtb16	r5,r9,ror #8",					// 1111 1010 0010 1111 1111 xxxx 1.xx xxxx	// A8.6.224	T1
			"FA 2F F5 A9", "sxtb16	r5,r9,ror #16",					// 1111 1010 0010 1111 1111 xxxx 1.xx xxxx	// A8.6.224	T1
			"FA 2F F5 B9", "sxtb16	r5,r9,ror #24",					// 1111 1010 0010 1111 1111 xxxx 1.xx xxxx	// A8.6.224	T1
			"FA 0F F5 8A", "sxth.w	r5,r10",						// 1111 1010 0000 1111 1111 xxxx 1.xx xxxx	// A8.6.225	T1
			"FA 0F F5 9A", "sxth.w	r5,r10,ror #8",					// 1111 1010 0000 1111 1111 xxxx 1.xx xxxx	// A8.6.225	T1
			"FA 0F F5 AA", "sxth.w	r5,r10,ror #16",				// 1111 1010 0000 1111 1111 xxxx 1.xx xxxx	// A8.6.225	T1
			"FA 0F F5 BA", "sxth.w	r5,r10,ror #24",				// 1111 1010 0000 1111 1111 xxxx 1.xx xxxx	// A8.6.225	T1
			"E8 D5 F0 0A", "tbb	[r5,r10]",							// 1110 1000 1101 xxxx :::: .... 000x xxxx	// A8.6.226	T1
			"E8 D5 F0 1A", "tbh	[r5,r10,lsl #1]",					// 1110 1000 1101 xxxx :::: .... 000x xxxx	// A8.6.226	T1
			"F0 95 0F 71", "teq	r5,#0x71",							// 1111 0x00 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.227	T1
			"F0 95 0F F7", "teq	r5,#0xf7",							// 1111 0x00 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.227	T1
			"F0 95 1F 78", "teq	r5,#0x780078",						// 1111 0x00 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.227	T1
			"F0 95 1F FC", "teq	r5,#0xfc00fc",						// 1111 0x00 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.227	T1
			"F0 95 2F 64", "teq	r5,#0x64006400",					// 1111 0x00 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.227	T1
			"F0 95 2F E3", "teq	r5,#0xe300e300",					// 1111 0x00 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.227	T1
			"F0 95 4F 60", "teq	r5,#0xe0000000",					// 1111 0x00 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.227	T1
			"F0 95 4F E0", "teq	r5,#0x70000000",					// 1111 0x00 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.227	T1
			"F4 95 0F 60", "teq	r5,#0xe00000",						// 1111 0x00 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.227	T1
			"F4 95 4F 60", "teq	r5,#0xe000",						// 1111 0x00 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.227	T1
			"F4 95 6F 60", "teq	r5,#0xe00",							// 1111 0x00 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.227	T1
			"EA 95 0F 09", "teq	r5,r9",								// 1110 1010 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.228	T1
			"EA 94 1F A8", "teq	r4,r8,asr #6",						// 1110 1010 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.228	T1
			"EA 93 0F 47", "teq	r3,r7,lsl #1",						// 1110 1010 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.228	T1
			"EA 92 0F 16", "teq	r2,r6,lsr #32",						// 1110 1010 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.228	T1
			"EA 95 7F F9", "teq	r5,r9,ror #31",						// 1110 1010 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.228	T1
			"EA 95 0F 38", "teq	r5,r8,rrx",							// 1110 1010 1001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.230	T1
			"F0 15 0F 71", "tst	r5,#0x71",							// 1111 0x00 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.230	T1
			"F0 15 0F F7", "tst	r5,#0xf7",							// 1111 0x00 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.230	T1
			"F0 15 1F 78", "tst	r5,#0x780078",						// 1111 0x00 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.230	T1
			"F0 15 1F FC", "tst	r5,#0xfc00fc",						// 1111 0x00 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.230	T1
			"F0 15 2F 64", "tst	r5,#0x64006400",					// 1111 0x00 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.230	T1
			"F0 15 2F E3", "tst	r5,#0xe300e300",					// 1111 0x00 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.230	T1
			"F0 15 4F 60", "tst	r5,#0xe0000000",					// 1111 0x00 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.230	T1
			"F0 15 4F E0", "tst	r5,#0x70000000",					// 1111 0x00 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.230	T1
			"F4 15 0F 60", "tst	r5,#0xe00000",						// 1111 0x00 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.230	T1
			"F4 15 4F 60", "tst	r5,#0xe000",						// 1111 0x00 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.230	T1
			"F4 15 6F 60", "tst	r5,#0xe00",							// 1111 0x00 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.230	T1
			"EA 15 0F 09", "tst.w	r5,r9",							// 1110 1010 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.231	T1
			"EA 14 1F A8", "tst.w	r4,r8,asr #6",					// 1110 1010 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.231	T2
			"EA 13 0F 47", "tst.w	r3,r7,lsl #1",					// 1110 1010 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.231	T2
			"EA 12 0F 16", "tst.w	r2,r6,lsr #32",					// 1110 1010 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.231	T2
			"EA 15 7F F9", "tst.w	r5,r9,ror #31",					// 1110 1010 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.231	T2
			"EA 15 0F 38", "tst.w	r5,r8,rrx",						// 1110 1010 0001 xxxx 0xxx 1111 xxxx xxxx	// A8.6.231	T2
			"FA 99 F5 4A", "uadd16	r5,r9,r10",						// 1111 1010 1001 xxxx 1111 xxxx 0100 xxxx  // A8.6.233	T1
			"FA 89 F5 4A", "uadd8	r5,r9,r10",             		// 1111 1010 1000 xxxx 1111 xxxx 0100 xxxx  // A8.6.234	T1
			"FA A9 F5 4A", "uasx	r5,r9,r10",						// 1111 1010 1010 xxxx 1111 xxxx 0100 xxxx  // A8.6.235	T1
			"F3 C7 05 1F", "ubfx	r5,r7,#0,#32",					// 1111 0.11 1100 xxxx 0xxx xxxx xx.x xxxx	// A8.6.236	T1
			"F3 C8 06 59", "ubfx	r6,r8,#1,#26",					// 1111 0.11 1100 xxxx 0xxx xxxx xx.x xxxx	// A8.6.236	T1
			"FB B9 F5 FA", "udiv	r5,r9,r10",						// 1111 1011 1011 xxxx :::: xxxx 1111 xxxx	// A8.6.237	T1
			"FA 99 F5 6A", "uhadd16	r5,r9,r10",						// 1111 1010 1001 xxxx 1111 xxxx 0110 xxxx	// A8.6.238	T1
			"FA 89 F5 6A", "uhadd8	r5,r9,r10",						// 1111 1010 1000 xxxx 1111 xxxx 0110 xxxx  // A8.6.239	T1
			"FA A9 F5 6A", "uhasx	r5,r9,r10",						// 1111 1010 1010 xxxx 1111 xxxx 0110 xxxx  // A8.6.240	T1
			"FA E9 F5 6A", "uhsax	r5,r9,r10",						// 1111 1010 1110 xxxx 1111 xxxx 0110 xxxx  // A8.6.241	T1
			"FA D9 F5 6A", "uhsub16	r5,r9,r10",						// 1111 1010 1101 xxxx 1111 xxxx 0110 xxxx  // A8.6.242	T1
			"FA C9 F5 6A", "uhsub8	r5,r9,r10",						// 1111 1010 1100 xxxx 1111 xxxx 0110 xxxx  // A8.6.243	T1
			"FB EA 56 69", "umaal	r5,r6,r10,r9",					// 1111 1E10 1100 xxxx xxxx xxxx 0110 xxxx	// A8.6.244	T1
			"FB EA 56 09", "umlal	r5,r6,r10,r9",					// 1111 1E10 1100 xxxx xxxx xxxx 0000 xxxx	// A8.6.245	T1
			"FB AA 56 09", "umull	r5,r6,r10,r9",					// 1111 1011 1010 xxxx xxxx xxxx 0000 xxxx	// A8.6.246	T1
			"FA 99 F5 5A", "uqadd16	r5,r9,r10",						// 1111 1010 1001 xxxx 1111 xxxx 0101 xxxx  // A8.6.247	T1
			"FA 89 F5 5A", "uqadd8	r5,r9,r10",						// 1111 1010 1000 xxxx 1111 xxxx 0101 xxxx  // A8.6.248	T1
			"FA A9 F5 5A", "uqasx	r5,r9,r10",						// 1111 1010 1010 xxxx 1111 xxxx 0101 xxxx  // A8.6.249	T1
			"FA E9 F5 5A", "uqsax	r5,r9,r10",						// 1111 1010 1110 xxxx 1111 xxxx 0101 xxxx  // A8.6.250	T1
			"FA D9 F5 5A", "uqsub16	r5,r9,r10",						// 1111 1010 1101 xxxx 1111 xxxx 0101 xxxx  // A8.6.251	T1
			"FA C9 F5 5A", "uqsub8	r5,r9,r10",						// 1111 1010 1100 xxxx 1111 xxxx 0101 xxxx  // A8.6.252	T1
			"FB 79 F5 0A", "usad8	r5,r9,r10",						// 1111 1011 0111 xxxx 1111 xxxx 0000 xxxx	// A8.6.253	T1
			"FB 79 85 0A", "usada8	r5,r9,r10,r8",					// 1111 1011 0111 xxxx xxxx xxxx 0000 xxxx	// A8.6.253	T1
			"F3 8A 05 1C", "usat	r5,#28,r10",					// 1111 0.11 10x0 xxxx 0xxx xxxx xx.x xxxx	// A8.6.255	T1
			"F3 AA 05 5C", "usat	r5,#28,r10,asr #1",				// 1111 0.11 10x0 xxxx 0xxx xxxx xx.x xxxx	// A8.6.255	T1
			"F3 AA 75 9C", "usat	r5,#28,r10,asr #30",			// 1111 0.11 10x0 xxxx 0xxx xxxx xx.x xxxx	// A8.6.255	T1
			"F3 8A 05 5C", "usat	r5,#28,r10,lsl #1",				// 1111 0.11 10x0 xxxx 0xxx xxxx xx.x xxxx	// A8.6.255	T1
			"F3 8A 75 DC", "usat	r5,#28,r10,lsl #31",			// 1111 0.11 10x0 xxxx 0xxx xxxx xx.x xxxx	// A8.6.255	T1
			"F3 AA 05 0C", "usat16	r5,#12,r10",					// 1111 0.11 1010 xxxx 0000 xxxx 00.. xxxx	// A8.6.256	T1
			"FA E9 F5 4A", "usax	r5,r9,r10",						// 1111 1010 1110 xxxx 1111 xxxx 0100 xxxx  // A8.6.257	T1
			"FA D9 F5 4A", "usub16	r5,r9,r10",						// 1111 1010 1101 xxxx 1111 xxxx 0100 xxxx  // A8.6.258	T1
			"FA C9 F5 4A", "usub8	r5,r9,r10",						// 1111 1010 1100 xxxx 1111 xxxx 0100 xxxx  // A8.6.259	T1
			"FA 59 F5 8A", "uxtab	r5,r9,r10",						// 1111 1010 0101 xxxx 1111 xxxx 1.xx xxxx	// A8.6.220	T1
			"FA 59 F5 9A", "uxtab	r5,r9,r10,ror #8",				// 1111 1010 0101 xxxx 1111 xxxx 1.xx xxxx	// A8.6.220	T1
			"FA 59 F5 AA", "uxtab	r5,r9,r10,ror #16",				// 1111 1010 0101 xxxx 1111 xxxx 1.xx xxxx	// A8.6.220	T1
			"FA 59 F5 BA", "uxtab	r5,r9,r10,ror #24",				// 1111 1010 0101 xxxx 1111 xxxx 1.xx xxxx	// A8.6.220	T1
			"FA 39 F5 8A", "uxtab16	r5,r9,r10",						// 1111 1010 0011 xxxx 1111 xxxx 1.xx xxxx	// A8.6.221	T1
			"FA 39 F5 9A", "uxtab16	r5,r9,r10,ror #8",				// 1111 1010 0011 xxxx 1111 xxxx 1.xx xxxx	// A8.6.221	T1
			"FA 39 F5 AA", "uxtab16	r5,r9,r10,ror #16",				// 1111 1010 0011 xxxx 1111 xxxx 1.xx xxxx	// A8.6.221	T1
			"FA 39 F5 BA", "uxtab16	r5,r9,r10,ror #24",				// 1111 1010 0011 xxxx 1111 xxxx 1.xx xxxx	// A8.6.221	T1
			"FA 19 F5 8A", "uxtah	r5,r9,r10",						// 1111 1010 0001 xxxx 1111 xxxx 1.xx xxxx	// A8.6.222	T1
			"FA 19 F5 9A", "uxtah	r5,r9,r10,ror #8",				// 1111 1010 0001 xxxx 1111 xxxx 1.xx xxxx	// A8.6.222	T1
			"FA 19 F5 AA", "uxtah	r5,r9,r10,ror #16",				// 1111 1010 0001 xxxx 1111 xxxx 1.xx xxxx	// A8.6.222	T1
			"FA 19 F5 BA", "uxtah	r5,r9,r10,ror #24",				// 1111 1010 0001 xxxx 1111 xxxx 1.xx xxxx	// A8.6.222	T1
			"FA 5F F5 89", "uxtb.w	r5,r9",							// 1111 1010 0101 1111 1111 xxxx 1.xx xxxx	// A8.6.223	T2
			"FA 5F F5 99", "uxtb.w	r5,r9,ror #8",					// 1111 1010 0101 1111 1111 xxxx 1.xx xxxx	// A8.6.223	T2
			"FA 5F F5 A9", "uxtb.w	r5,r9,ror #16",					// 1111 1010 0101 1111 1111 xxxx 1.xx xxxx	// A8.6.223	T2
			"FA 5F F5 B9", "uxtb.w	r5,r9,ror #24",					// 1111 1010 0101 1111 1111 xxxx 1.xx xxxx	// A8.6.223	T2
			"FA 3F F5 89", "uxtb16	r5,r9",							// 1111 1010 0011 1111 1111 xxxx 1.xx xxxx	// A8.6.224	T1
			"FA 3F F5 99", "uxtb16	r5,r9,ror #8",					// 1111 1010 0011 1111 1111 xxxx 1.xx xxxx	// A8.6.224	T1
			"FA 3F F5 A9", "uxtb16	r5,r9,ror #16",					// 1111 1010 0011 1111 1111 xxxx 1.xx xxxx	// A8.6.224	T1
			"FA 3F F5 B9", "uxtb16	r5,r9,ror #24",					// 1111 1010 0011 1111 1111 xxxx 1.xx xxxx	// A8.6.224	T1
			"FA 1F F5 8A", "uxth.w	r5,r10",						// 1111 1010 0001 1111 1111 xxxx 1.xx xxxx	// A8.6.225	T1
			"FA 1F F5 9A", "uxth.w	r5,r10,ror #8",					// 1111 1010 0001 1111 1111 xxxx 1.xx xxxx	// A8.6.225	T1
			"FA 1F F5 AA", "uxth.w	r5,r10,ror #16",				// 1111 1010 0001 1111 1111 xxxx 1.xx xxxx	// A8.6.225	T1
			"FA 1F F5 BA", "uxth.w	r5,r10,ror #24",				// 1111 1010 0001 1111 1111 xxxx 1.xx xxxx	// A8.6.225	T1
			"F3 AF 80 02", "wfe.w",									// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.411
			"F3 AF 80 03", "wfi.w",									// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.412
			"F3 AF 80 01", "yield.w",								// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.413
		};

		disassembleInstArray(insts, thumbOptions);
	}

	@Test
	public void test32BitThumbVFPInstructions() {

		System.out.println("\n====================== ARM VFP ======================\n");
		String[] insts = {
				"EF 49 57 BA", "vaba.s8	d21,d25,d26",
				"EF 59 57 BA", "vaba.s16	d21,d25,d26",
				"EF 69 57 BA", "vaba.s32	d21,d25,d26",
				"FF 49 57 BA", "vaba.u8	d21,d25,d26",
				"FF 59 57 BA", "vaba.u16	d21,d25,d26",
				"FF 69 57 BA", "vaba.u32	d21,d25,d26",
				"EF 4C 67 FE", "vaba.s8	q11,q14,q15",
				"EF 5C 67 FE", "vaba.s16	q11,q14,q15",
				"EF 6C 67 FE", "vaba.s32	q11,q14,q15",
				"FF 4C 67 FE", "vaba.u8	q11,q14,q15",
				"FF 5C 67 FE", "vaba.u16	q11,q14,q15",
				"FF 6C 67 FE", "vaba.u32	q11,q14,q15",
				"EF C9 65 AA", "vabal.s8	q11,d25,d26",
				"EF D9 65 AA", "vabal.s16	q11,d25,d26",
				"EF E9 65 AA", "vabal.s32	q11,d25,d26",
				"FF C9 65 AA", "vabal.u8	q11,d25,d26",
				"FF D9 65 AA", "vabal.u16	q11,d25,d26",
				"FF E9 65 AA", "vabal.u32	q11,d25,d26",
				"EF 49 57 AA", "vabd.s8	d21,d25,d26",
				"EF 59 57 AA", "vabd.s16	d21,d25,d26",
				"EF 69 57 AA", "vabd.s32	d21,d25,d26",
				"FF 49 57 AA", "vabd.u8	d21,d25,d26",
				"FF 59 57 AA", "vabd.u16	d21,d25,d26",
				"FF 69 57 AA", "vabd.u32	d21,d25,d26",
				"EF 4C 67 EE", "vabd.s8	q11,q14,q15",
				"EF 5C 67 EE", "vabd.s16	q11,q14,q15",
				"EF 6C 67 EE", "vabd.s32	q11,q14,q15",
				"FF 4C 67 EE", "vabd.u8	q11,q14,q15",
				"FF 5C 67 EE", "vabd.u16	q11,q14,q15",
				"FF 6C 67 EE", "vabd.u32	q11,q14,q15",
				"FF 69 5D AA", "vabd.f32	d21,d25,d26",
				"FF 6C 6D EE", "vabd.f32	q11,q14,q15",
				"EF C9 67 AA", "vabdl.s8	q11,d25,d26",
				"EF D9 67 AA", "vabdl.s16	q11,d25,d26",
				"EF E9 67 AA", "vabdl.s32	q11,d25,d26",
				"FF C9 67 AA", "vabdl.u8	q11,d25,d26",
				"FF D9 67 AA", "vabdl.u16	q11,d25,d26",
				"FF E9 67 AA", "vabdl.u32	q11,d25,d26",
				"FF F1 63 6E", "vabs.s8	q11,q15",
				"FF F5 63 6E", "vabs.s16	q11,q15",
				"FF F9 63 6E", "vabs.s32	q11,q15",
				"FF F9 67 6E", "vabs.f32	q11,q15",
				"FF F1 53 2A", "vabs.s8	d21,d26",
				"FF F5 53 2A", "vabs.s16	d21,d26",
				"FF F9 53 2A", "vabs.s32	d21,d26",
				"FF F9 57 2A", "vabs.f32	d21,d26",
				"EE F0 5B EA", "vabs.f64	d21,d26",
				"EE F0 AA CD", "vabs.f32	s21,s26",
				"EE F0 5B EA", "vabs.f64	d21,d26",
				"FF 49 5E BA", "vacge.f32	d21,d25,d26",
				"FF 4C 6E FE", "vacge.f32	q11,q14,q15",
				"FF 69 5E BA", "vacgt.f32	d21,d25,d26",
				"FF 6C 6E FE", "vacgt.f32	q11,q14,q15",
				"EF 49 58 AA", "vadd.i8	d21,d25,d26",
				"EF 59 58 AA", "vadd.i16	d21,d25,d26",
				"EF 69 58 AA", "vadd.i32	d21,d25,d26",
				"EF 79 58 AA", "vadd.i64	d21,d25,d26",
				"EF 4C 68 EE", "vadd.i8	q11,q14,q15",
				"EF 5C 68 EE", "vadd.i16	q11,q14,q15",
				"EF 6C 68 EE", "vadd.i32	q11,q14,q15",
				"EF 7C 68 EE", "vadd.i64	q11,q14,q15",
				"EF 49 5D AA", "vadd.f32	d21,d25,d26",
				"EF 4C 6D EE", "vadd.f32	q11,q14,q15",
				"EE 7C AA 8D", "vadd.f32	s21,s25,s26",
				"EE 79 5B AA", "vadd.f64	d21,d25,d26",
				"EF CC 54 AE", "vaddhn.i16	d21,q14,q15",
				"EF DC 54 AE", "vaddhn.i32	d21,q14,q15",
				"EF EC 54 AE", "vaddhn.i64	d21,q14,q15",
				"EF C9 60 AA", "vaddl.s8	q11,d25,d26",
				"EF D9 60 AA", "vaddl.s16	q11,d25,d26",
				"EF E9 60 AA", "vaddl.s32	q11,d25,d26",
				"FF C9 60 AA", "vaddl.u8	q11,d25,d26",
				"FF D9 60 AA", "vaddl.u16	q11,d25,d26",
				"FF E9 60 AA", "vaddl.u32	q11,d25,d26",
				"EF CC 61 AA", "vaddw.s8	q11,q14,d26",
				"EF DC 61 AA", "vaddw.s16	q11,q14,d26",
				"EF EC 61 AA", "vaddw.s32	q11,q14,d26",
				"FF CC 61 AA", "vaddw.u8	q11,q14,d26",
				"FF DC 61 AA", "vaddw.u16	q11,q14,d26",
				"FF EC 61 AA", "vaddw.u32	q11,q14,d26",
				"EF 49 51 BA", "vand	d21,d25,d26",
				"EF 4C 61 FE", "vand	q11,q14,q15",
				"EF 59 51 BA", "vbic	d21,d25,d26",
				"EF 5C 61 FE", "vbic	q11,q14,q15",
				"FF C0 59 39", "vbic.i16	d21,#0x89",
				"FF C0 51 39", "vbic.i32	d21,#0x89",
				"FF C0 69 79", "vbic.i16	q11,#0x89",
				"FF C0 61 79", "vbic.i32	q11,#0x89",
				"FF 79 51 BA", "vbif	d21,d25,d26",
				"FF 7C 61 FE", "vbif	q11,q14,q15",
				"FF 69 51 BA", "vbit	d21,d25,d26",
				"FF 6C 61 FE", "vbit	q11,q14,q15",
				"FF 59 51 BA", "vbsl	d21,d25,d26",
				"FF 5C 61 FE", "vbsl	q11,q14,q15",
				"FF 49 58 BA", "vceq.i8	d21,d25,d26",
				"FF 59 58 BA", "vceq.i16	d21,d25,d26",
				"FF 69 58 BA", "vceq.i32	d21,d25,d26",
				"FF F1 51 2A", "vceq.i8	d21,d26,#0",
				"FF F5 51 2A", "vceq.i16	d21,d26,#0",
				"FF F9 51 2A", "vceq.i32	d21,d26,#0",
				"FF F9 55 2A", "vceq.f32	d21,d26,#0",
				"FF 4C 68 FE", "vceq.i8	q11,q14,q15",
				"FF 5C 68 FE", "vceq.i16	q11,q14,q15",
				"FF 6C 68 FE", "vceq.i32	q11,q14,q15",
				"FF F1 61 6E", "vceq.i8	q11,q15,#0",
				"FF F5 61 6E", "vceq.i16	q11,q15,#0",
				"FF F9 61 6E", "vceq.i32	q11,q15,#0",
				"FF F9 65 6E", "vceq.f32	q11,q15,#0",
				"EF 49 5E AA", "vceq.f32	d21,d25,d26",
				"EF 4C 6E EE", "vceq.f32	q11,q14,q15",
				"EF 49 53 BA", "vcge.s8	d21,d25,d26",
				"EF 59 53 BA", "vcge.s16	d21,d25,d26",
				"EF 69 53 BA", "vcge.s32	d21,d25,d26",
				"FF 49 53 BA", "vcge.u8	d21,d25,d26",
				"FF 59 53 BA", "vcge.u16	d21,d25,d26",
				"FF 69 53 BA", "vcge.u32	d21,d25,d26",
				"FF F1 50 AA", "vcge.s8	d21,d26,#0",
				"FF F5 50 AA", "vcge.s16	d21,d26,#0",
				"FF F9 50 AA", "vcge.s32	d21,d26,#0",
				"FF F9 54 AA", "vcge.f32	d21,d26,#0",
				"EF 4C 63 FE", "vcge.s8	q11,q14,q15",
				"EF 5C 63 FE", "vcge.s16	q11,q14,q15",
				"EF 6C 63 FE", "vcge.s32	q11,q14,q15",
				"FF 4C 63 FE", "vcge.u8	q11,q14,q15",
				"FF 5C 63 FE", "vcge.u16	q11,q14,q15",
				"FF 6C 63 FE", "vcge.u32	q11,q14,q15",
				"FF F1 60 EE", "vcge.s8	q11,q15,#0",
				"FF F5 60 EE", "vcge.s16	q11,q15,#0",
				"FF F9 60 EE", "vcge.s32	q11,q15,#0",
				"FF F9 64 EE", "vcge.f32	q11,q15,#0",
				"FF 49 5E AA", "vcge.f32	d21,d25,d26",
				"FF 4C 6E EE", "vcge.f32	q11,q14,q15",
				"EF 49 53 AA", "vcgt.s8	d21,d25,d26",
				"EF 59 53 AA", "vcgt.s16	d21,d25,d26",
				"EF 69 53 AA", "vcgt.s32	d21,d25,d26",
				"FF 49 53 AA", "vcgt.u8	d21,d25,d26",
				"FF 59 53 AA", "vcgt.u16	d21,d25,d26",
				"FF 69 53 AA", "vcgt.u32	d21,d25,d26",
				"FF F1 50 2A", "vcgt.s8	d21,d26,#0",
				"FF F5 50 2A", "vcgt.s16	d21,d26,#0",
				"FF F9 50 2A", "vcgt.s32	d21,d26,#0",
				"FF F9 54 2A", "vcgt.f32	d21,d26,#0",
				"EF 4C 63 EE", "vcgt.s8	q11,q14,q15",
				"EF 5C 63 EE", "vcgt.s16	q11,q14,q15",
				"EF 6C 63 EE", "vcgt.s32	q11,q14,q15",
				"FF 4C 63 EE", "vcgt.u8	q11,q14,q15",
				"FF 5C 63 EE", "vcgt.u16	q11,q14,q15",
				"FF 6C 63 EE", "vcgt.u32	q11,q14,q15",
				"FF F1 60 6E", "vcgt.s8	q11,q15,#0",
				"FF F5 60 6E", "vcgt.s16	q11,q15,#0",
				"FF F9 60 6E", "vcgt.s32	q11,q15,#0",
				"FF F9 64 6E", "vcgt.f32	q11,q15,#0",
				"FF 69 5E AA", "vcgt.f32	d21,d25,d26",
				"FF 6C 6E EE", "vcgt.f32	q11,q14,q15",
				"FF F1 51 AA", "vcle.s8	d21,d26,#0",
				"FF F5 51 AA", "vcle.s16	d21,d26,#0",
				"FF F9 51 AA", "vcle.s32	d21,d26,#0",
				"FF F9 55 AA", "vcle.f32	d21,d26,#0",
				"FF F1 61 EE", "vcle.s8	q11,q15,#0",
				"FF F5 61 EE", "vcle.s16	q11,q15,#0",
				"FF F9 61 EE", "vcle.s32	q11,q15,#0",
				"FF F9 65 EE", "vcle.f32	q11,q15,#0",
				"FF F0 54 2A", "vcls.s8	d21,d26",
				"FF F4 54 2A", "vcls.s16	d21,d26",
				"FF F8 54 2A", "vcls.s32	d21,d26",
				"FF F0 64 6E", "vcls.s8	q11,q15",
				"FF F4 64 6E", "vcls.s16	q11,q15",
				"FF F8 64 6E", "vcls.s32	q11,q15",
				"FF F1 52 2A", "vclt.s8	d21,d26,#0",
				"FF F5 52 2A", "vclt.s16	d21,d26,#0",
				"FF F9 52 2A", "vclt.s32	d21,d26,#0",
				"FF F9 56 2A", "vclt.f32	d21,d26,#0",
				"FF F1 62 6E", "vclt.s8	q11,q15,#0",
				"FF F5 62 6E", "vclt.s16	q11,q15,#0",
				"FF F9 62 6E", "vclt.s32	q11,q15,#0",
				"FF F9 66 6E", "vclt.f32	q11,q15,#0",
				"FF F0 54 AA", "vclz.i8	d21,d26",
				"FF F4 54 AA", "vclz.i16	d21,d26",
				"FF F8 54 AA", "vclz.i32	d21,d26",
				"FF F0 64 EE", "vclz.i8	q11,q15",
				"FF F4 64 EE", "vclz.i16	q11,q15",
				"FF F8 64 EE", "vclz.i32	q11,q15",
				"EE F5 AA 40", "vcmp.f32	s21,#0.0",
				"EE F4 AA 4D", "vcmp.f32	s21,s26",
				"EE F5 5B 40", "vcmp.f64	d21,#0.0",
				"EE F4 5B 6A", "vcmp.f64	d21,d26",
				"EE F5 AA C0", "vcmpe.f32	s21,#0.0",
				"EE F4 AA CD", "vcmpe.f32	s21,s26",
				"EE F5 5B C0", "vcmpe.f64	d21,#0.0",
				"EE F4 5B EA", "vcmpe.f64	d21,d26",
				"FF F0 55 2A", "vcnt.8	d21,d26",
				"FF F0 65 6E", "vcnt.8	q11,q15",
				"FF FB 57 2A", "vcvt.s32.f32	d21,d26",
				"FF FB 57 AA", "vcvt.u32.f32	d21,d26",
				"FF FB 56 2A", "vcvt.f32.s32	d21,d26",
				"FF FB 56 AA", "vcvt.f32.u32	d21,d26",
				"EF E0 5F 3A", "vcvt.s32.f32	d21,d26,#32",
				"FF E0 5F 3A", "vcvt.u32.f32	d21,d26,#32",
				"EF E0 5E 3A", "vcvt.f32.s32	d21,d26,#32",
				"FF E0 5E 3A", "vcvt.f32.u32	d21,d26,#32",
				"FF FB 67 6E", "vcvt.s32.f32	q11,q15",
				"FF FB 67 EE", "vcvt.u32.f32	q11,q15",
				"FF FB 66 6E", "vcvt.f32.s32	q11,q15",
				"FF FB 66 EE", "vcvt.f32.u32	q11,q15",
				"EF E0 6F 7E", "vcvt.s32.f32	q11,q15,#32",
				"FF E0 6F 7E", "vcvt.u32.f32	q11,q15,#32",
				"EF E0 6E 7E", "vcvt.f32.s32	q11,q15,#32",
				"FF E0 6E 7E", "vcvt.f32.u32	q11,q15,#32",
				"EE FA AA E8", "vcvt.f32.s32	s21,s21,#15",
				"EE FF AA 60", "vcvt.u16.f32	s21,s21,#15",
				"EE FE AA E2", "vcvt.s32.f32	s21,s21,#27",
				"EE FF AA E2", "vcvt.u32.f32	s21,s21,#27",
				"EE FE 5B 60", "vcvt.s16.f64	d21,d21,#15",
				"EE FF 5B 60", "vcvt.u16.f64	d21,d21,#15",
				"EE FE 5B E2", "vcvt.s32.f64	d21,d21,#27",
				"EE FF 5B E2", "vcvt.u32.f64	d21,d21,#27",
				"FF F6 56 2E", "vcvt.f16.f32	d21,q15",
				"EE FA AA 60", "vcvt.f32.s16	s21,s21,#15",
				"EE FB AA 60", "vcvt.f32.u16	s21,s21,#15",
				"EE FA AA E2", "vcvt.f32.s32	s21,s21,#27",
				"EE FB AA E2", "vcvt.f32.u32	s21,s21,#27",
				"EE F8 AA CD", "vcvt.f32.s32	s21,s26",
				"EE F8 AA 4D", "vcvt.f32.u32	s21,s26",
				"FF F6 67 2A", "vcvt.f32.f16	q11,d26",
				"EE F7 AB EA", "vcvt.f32.f64	s21,d26",
				"EE FA 5B 60", "vcvt.f64.s16	d21,d21,#15",
				"EE FB 5B 60", "vcvt.f64.u16	d21,d21,#15",
				"EE FA 5B E2", "vcvt.f64.s32	d21,d21,#27",
				"EE FB 5B E2", "vcvt.f64.u32	d21,d21,#27",
				"EE F8 5B CD", "vcvt.f64.s32	d21,s26",
				"EE F8 5B 4D", "vcvt.f64.u32	d21,s26",
				"EE F7 5A CD", "vcvt.f64.f32	d21,s26",
				"EE FD AA CD", "vcvt.s32.f32	s21,s26",
				"EE FD AB EA", "vcvt.s32.f64	s21,d26",
				"EE FC AA CD", "vcvt.u32.f32	s21,s26",
				"EE FC AB EA", "vcvt.u32.f64	s21,d26",
				"EE F3 AA 4D", "vcvtb.f16.f32	s21,s26",
				"EE F2 AA 4D", "vcvtb.f32.f16	s21,s26",
				"EE F3 AA CD", "vcvtt.f16.f32	s21,s26",
				"EE F2 AA CD", "vcvtt.f32.f16	s21,s26",
				"EE FD AA 4D", "vcvtr.s32.f32	s21,s26",
				"EE FD AB 6A", "vcvtr.s32.f64	s21,d26",
				"EE FC AA 4D", "vcvtr.u32.f32	s21,s26",
				"EE FC AB 6A", "vcvtr.u32.f64	s21,d26",
				"EE CC AA 8D", "vdiv.f32	s21,s25,s26",
				"EE C9 5B AA", "vdiv.f64	d21,d25,d26",
				"FF F5 5C 26", "vdup.8	d21,d22[2]",
				"FF FA 5C 26", "vdup.16	d21,d22[2]",
				"FF FC 5C 26", "vdup.32	d21,d22[1]",
				"EE C5 5B 90", "vdup.8	d21,r5",
				"EE 85 5B B0", "vdup.16	d21,r5",
				"EE 85 5B 90", "vdup.32	d21,r5",
				"FF F5 6C 66", "vdup.8	q11,d22[2]",
				"FF FA 6C 66", "vdup.16	q11,d22[2]",
				"FF FC 6C 66", "vdup.32	q11,d22[1]",
				"EE E6 5B 90", "vdup.8	q11,r5",
				"EE A6 5B B0", "vdup.16	q11,r5",
				"EE A6 5B 90", "vdup.32	q11,r5",
				"FF 49 51 BA", "veor	d21,d25,d26",
				"FF 4C 61 FE", "veor	q11,q14,q15",
				"EF F9 55 AA", "vext.8	d21,d25,d26,#5",
				"EF FC 6D EE", "vext.8	q11,q14,q15,#13",
				"EF 49 50 AA", "vhadd.s8	d21,d25,d26",
				"EF 59 50 AA", "vhadd.s16	d21,d25,d26",
				"EF 69 50 AA", "vhadd.s32	d21,d25,d26",
				"FF 49 50 AA", "vhadd.u8	d21,d25,d26",
				"FF 59 50 AA", "vhadd.u16	d21,d25,d26",
				"FF 69 50 AA", "vhadd.u32	d21,d25,d26",
				"EF 4C 60 EE", "vhadd.s8	q11,q14,q15",
				"EF 5C 60 EE", "vhadd.s16	q11,q14,q15",
				"EF 6C 60 EE", "vhadd.s32	q11,q14,q15",
				"FF 4C 60 EE", "vhadd.u8	q11,q14,q15",
				"FF 5C 60 EE", "vhadd.u16	q11,q14,q15",
				"FF 6C 60 EE", "vhadd.u32	q11,q14,q15",
				"EF 49 52 AA", "vhsub.s8	d21,d25,d26",
				"EF 59 52 AA", "vhsub.s16	d21,d25,d26",
				"EF 69 52 AA", "vhsub.s32	d21,d25,d26",
				"FF 49 52 AA", "vhsub.u8	d21,d25,d26",
				"FF 59 52 AA", "vhsub.u16	d21,d25,d26",
				"FF 69 52 AA", "vhsub.u32	d21,d25,d26",
				"EF 4C 62 EE", "vhsub.s8	q11,q14,q15",
				"EF 5C 62 EE", "vhsub.s16	q11,q14,q15",
				"EF 6C 62 EE", "vhsub.s32	q11,q14,q15",
				"FF 4C 62 EE", "vhsub.u8	q11,q14,q15",
				"FF 5C 62 EE", "vhsub.u16	q11,q14,q15",
				"FF 6C 62 EE", "vhsub.u32	q11,q14,q15",
				"F9 6A B7 0F", "vld1.8	{d27},[r10]",
				"F9 6A BA 0F", "vld1.8	{d27,d28},[r10]",
				"F9 6A B6 0F", "vld1.8	{d27,d28,d29},[r10]",
				"F9 6A B2 0F", "vld1.8	{d27,d28,d29,d30},[r10]",
				"F9 6A B7 4F", "vld1.16	{d27},[r10]",
				"F9 6A BA 4F", "vld1.16	{d27,d28},[r10]",
				"F9 6A B6 4F", "vld1.16	{d27,d28,d29},[r10]",
				"F9 6A B2 4F", "vld1.16	{d27,d28,d29,d30},[r10]",
				"F9 6A B7 8F", "vld1.32	{d27},[r10]",
				"F9 6A BA 8F", "vld1.32	{d27,d28},[r10]",
				"F9 6A B6 8F", "vld1.32	{d27,d28,d29},[r10]",
				"F9 6A B2 8F", "vld1.32	{d27,d28,d29,d30},[r10]",
				"F9 6A B7 CF", "vld1.64	{d27},[r10]",
				"F9 6A BA CF", "vld1.64	{d27,d28},[r10]",
				"F9 6A B6 CF", "vld1.64	{d27,d28,d29},[r10]",
				"F9 6A B2 CF", "vld1.64	{d27,d28,d29,d30},[r10]",
				"F9 6A B7 1F", "vld1.8	{d27},[r10@64]",
				"F9 6A BA 1F", "vld1.8	{d27,d28},[r10@64]",
				"F9 6A BA 2F", "vld1.8	{d27,d28},[r10@128]",
				"F9 6A B6 1F", "vld1.8	{d27,d28,d29},[r10@64]",
				"F9 6A B2 1F", "vld1.8	{d27,d28,d29,d30},[r10@64]",
				"F9 6A B2 2F", "vld1.8	{d27,d28,d29,d30},[r10@128]",
				"F9 6A B2 3F", "vld1.8	{d27,d28,d29,d30},[r10@256]",
				"F9 6A B7 5F", "vld1.16	{d27},[r10@64]",
				"F9 6A BA 5F", "vld1.16	{d27,d28},[r10@64]",
				"F9 6A BA 6F", "vld1.16	{d27,d28},[r10@128]",
				"F9 6A B6 5F", "vld1.16	{d27,d28,d29},[r10@64]",
				"F9 6A B2 5F", "vld1.16	{d27,d28,d29,d30},[r10@64]",
				"F9 6A B2 6F", "vld1.16	{d27,d28,d29,d30},[r10@128]",
				"F9 6A B2 7F", "vld1.16	{d27,d28,d29,d30},[r10@256]",
				"F9 6A B7 9F", "vld1.32	{d27},[r10@64]",
				"F9 6A BA 9F", "vld1.32	{d27,d28},[r10@64]",
				"F9 6A BA AF", "vld1.32	{d27,d28},[r10@128]",
				"F9 6A B6 9F", "vld1.32	{d27,d28,d29},[r10@64]",
				"F9 6A B2 9F", "vld1.32	{d27,d28,d29,d30},[r10@64]",
				"F9 6A B2 AF", "vld1.32	{d27,d28,d29,d30},[r10@128]",
				"F9 6A B2 BF", "vld1.32	{d27,d28,d29,d30},[r10@256]",
				"F9 6A B7 DF", "vld1.64	{d27},[r10@64]",
				"F9 6A BA DF", "vld1.64	{d27,d28},[r10@64]",
				"F9 6A BA EF", "vld1.64	{d27,d28},[r10@128]",
				"F9 6A B6 DF", "vld1.64	{d27,d28,d29},[r10@64]",
				"F9 6A B2 DF", "vld1.64	{d27,d28,d29,d30},[r10@64]",
				"F9 6A B2 EF", "vld1.64	{d27,d28,d29,d30},[r10@128]",
				"F9 6A B2 FF", "vld1.64	{d27,d28,d29,d30},[r10@256]",
				"F9 6A B7 0D", "vld1.8	{d27},[r10]!",
				"F9 6A BA 0D", "vld1.8	{d27,d28},[r10]!",
				"F9 6A B6 0D", "vld1.8	{d27,d28,d29},[r10]!",
				"F9 6A B2 0D", "vld1.8	{d27,d28,d29,d30},[r10]!",
				"F9 6A B7 4D", "vld1.16	{d27},[r10]!",
				"F9 6A BA 4D", "vld1.16	{d27,d28},[r10]!",
				"F9 6A B6 4D", "vld1.16	{d27,d28,d29},[r10]!",
				"F9 6A B2 4D", "vld1.16	{d27,d28,d29,d30},[r10]!",
				"F9 6A B7 8D", "vld1.32	{d27},[r10]!",
				"F9 6A BA 8D", "vld1.32	{d27,d28},[r10]!",
				"F9 6A B6 8D", "vld1.32	{d27,d28,d29},[r10]!",
				"F9 6A B2 8D", "vld1.32	{d27,d28,d29,d30},[r10]!",
				"F9 6A B7 CD", "vld1.64	{d27},[r10]!",
				"F9 6A BA CD", "vld1.64	{d27,d28},[r10]!",
				"F9 6A B6 CD", "vld1.64	{d27,d28,d29},[r10]!",
				"F9 6A B2 CD", "vld1.64	{d27,d28,d29,d30},[r10]!",
				"F9 6A B7 1D", "vld1.8	{d27},[r10@64]!",
				"F9 6A BA 1D", "vld1.8	{d27,d28},[r10@64]!",
				"F9 6A BA 2D", "vld1.8	{d27,d28},[r10@128]!",
				"F9 6A B6 1D", "vld1.8	{d27,d28,d29},[r10@64]!",
				"F9 6A B2 1D", "vld1.8	{d27,d28,d29,d30},[r10@64]!",
				"F9 6A B2 2D", "vld1.8	{d27,d28,d29,d30},[r10@128]!",
				"F9 6A B2 3D", "vld1.8	{d27,d28,d29,d30},[r10@256]!",
				"F9 6A B7 5D", "vld1.16	{d27},[r10@64]!",
				"F9 6A BA 5D", "vld1.16	{d27,d28},[r10@64]!",
				"F9 6A BA 6D", "vld1.16	{d27,d28},[r10@128]!",
				"F9 6A B6 5D", "vld1.16	{d27,d28,d29},[r10@64]!",
				"F9 6A B2 5D", "vld1.16	{d27,d28,d29,d30},[r10@64]!",
				"F9 6A B2 6D", "vld1.16	{d27,d28,d29,d30},[r10@128]!",
				"F9 6A B2 7D", "vld1.16	{d27,d28,d29,d30},[r10@256]!",
				"F9 6A B7 9D", "vld1.32	{d27},[r10@64]!",
				"F9 6A BA 9D", "vld1.32	{d27,d28},[r10@64]!",
				"F9 6A BA AD", "vld1.32	{d27,d28},[r10@128]!",
				"F9 6A B6 9D", "vld1.32	{d27,d28,d29},[r10@64]!",
				"F9 6A B2 9D", "vld1.32	{d27,d28,d29,d30},[r10@64]!",
				"F9 6A B2 AD", "vld1.32	{d27,d28,d29,d30},[r10@128]!",
				"F9 6A B2 BD", "vld1.32	{d27,d28,d29,d30},[r10@256]!",
				"F9 6A B7 DD", "vld1.64	{d27},[r10@64]!",
				"F9 6A BA DD", "vld1.64	{d27,d28},[r10@64]!",
				"F9 6A BA ED", "vld1.64	{d27,d28},[r10@128]!",
				"F9 6A B6 DD", "vld1.64	{d27,d28,d29},[r10@64]!",
				"F9 6A B2 DD", "vld1.64	{d27,d28,d29,d30},[r10@64]!",
				"F9 6A B2 ED", "vld1.64	{d27,d28,d29,d30},[r10@128]!",
				"F9 6A B2 FD", "vld1.64	{d27,d28,d29,d30},[r10@256]!",
				"F9 6A B7 09", "vld1.8	{d27},[r10],r9",
				"F9 6A BA 09", "vld1.8	{d27,d28},[r10],r9",
				"F9 6A B6 09", "vld1.8	{d27,d28,d29},[r10],r9",
				"F9 6A B2 09", "vld1.8	{d27,d28,d29,d30},[r10],r9",
				"F9 6A B7 49", "vld1.16	{d27},[r10],r9",
				"F9 6A BA 49", "vld1.16	{d27,d28},[r10],r9",
				"F9 6A B6 49", "vld1.16	{d27,d28,d29},[r10],r9",
				"F9 6A B2 49", "vld1.16	{d27,d28,d29,d30},[r10],r9",
				"F9 6A B7 89", "vld1.32	{d27},[r10],r9",
				"F9 6A BA 89", "vld1.32	{d27,d28},[r10],r9",
				"F9 6A B6 89", "vld1.32	{d27,d28,d29},[r10],r9",
				"F9 6A B2 89", "vld1.32	{d27,d28,d29,d30},[r10],r9",
				"F9 6A B7 C9", "vld1.64	{d27},[r10],r9",
				"F9 6A BA C9", "vld1.64	{d27,d28},[r10],r9",
				"F9 6A B6 C9", "vld1.64	{d27,d28,d29},[r10],r9",
				"F9 6A B2 C9", "vld1.64	{d27,d28,d29,d30},[r10],r9",
				"F9 6A B7 19", "vld1.8	{d27},[r10@64],r9",
				"F9 6A BA 19", "vld1.8	{d27,d28},[r10@64],r9",
				"F9 6A BA 29", "vld1.8	{d27,d28},[r10@128],r9",
				"F9 6A B6 19", "vld1.8	{d27,d28,d29},[r10@64],r9",
				"F9 6A B2 19", "vld1.8	{d27,d28,d29,d30},[r10@64],r9",
				"F9 6A B2 29", "vld1.8	{d27,d28,d29,d30},[r10@128],r9",
				"F9 6A B2 39", "vld1.8	{d27,d28,d29,d30},[r10@256],r9",
				"F9 6A B7 59", "vld1.16	{d27},[r10@64],r9",
				"F9 6A BA 59", "vld1.16	{d27,d28},[r10@64],r9",
				"F9 6A BA 69", "vld1.16	{d27,d28},[r10@128],r9",
				"F9 6A B6 59", "vld1.16	{d27,d28,d29},[r10@64],r9",
				"F9 6A B2 59", "vld1.16	{d27,d28,d29,d30},[r10@64],r9",
				"F9 6A B2 69", "vld1.16	{d27,d28,d29,d30},[r10@128],r9",
				"F9 6A B2 79", "vld1.16	{d27,d28,d29,d30},[r10@256],r9",
				"F9 6A B7 99", "vld1.32	{d27},[r10@64],r9",
				"F9 6A BA 99", "vld1.32	{d27,d28},[r10@64],r9",
				"F9 6A BA A9", "vld1.32	{d27,d28},[r10@128],r9",
				"F9 6A B6 99", "vld1.32	{d27,d28,d29},[r10@64],r9",
				"F9 6A B2 99", "vld1.32	{d27,d28,d29,d30},[r10@64],r9",
				"F9 6A B2 A9", "vld1.32	{d27,d28,d29,d30},[r10@128],r9",
				"F9 6A B2 B9", "vld1.32	{d27,d28,d29,d30},[r10@256],r9",
				"F9 6A B7 D9", "vld1.64	{d27},[r10@64],r9",
				"F9 6A BA D9", "vld1.64	{d27,d28},[r10@64],r9",
				"F9 6A BA E9", "vld1.64	{d27,d28},[r10@128],r9",
				"F9 6A B6 D9", "vld1.64	{d27,d28,d29},[r10@64],r9",
				"F9 6A B2 D9", "vld1.64	{d27,d28,d29,d30},[r10@64],r9",
				"F9 6A B2 E9", "vld1.64	{d27,d28,d29,d30},[r10@128],r9",
				"F9 6A B2 F9", "vld1.64	{d27,d28,d29,d30},[r10@256],r9",
				"F9 EA B0 2F", "vld1.8	{d27[1]},[r10]",
				"F9 EA B4 4F", "vld1.16	{d27[1]},[r10]",
				"F9 EA B8 8F", "vld1.32	{d27[1]},[r10]",
				"F9 EA B4 5F", "vld1.16	{d27[1]},[r10@16]",
				"F9 EA B8 BF", "vld1.32	{d27[1]},[r10@32]",
				"F9 EA B0 2D", "vld1.8	{d27[1]},[r10]!",
				"F9 EA B4 4D", "vld1.16	{d27[1]},[r10]!",
				"F9 EA B8 8D", "vld1.32	{d27[1]},[r10]!",
				"F9 EA B4 5D", "vld1.16	{d27[1]},[r10@16]!",
				"F9 EA B8 BD", "vld1.32	{d27[1]},[r10@32]!",
				"F9 EA B0 29", "vld1.8	{d27[1]},[r10],r9",
				"F9 EA B4 49", "vld1.16	{d27[1]},[r10],r9",
				"F9 EA B8 89", "vld1.32	{d27[1]},[r10],r9",
				"F9 EA B4 59", "vld1.16	{d27[1]},[r10@16],r9",
				"F9 EA B8 B9", "vld1.32	{d27[1]},[r10@32],r9",
				"F9 EA BC 0F", "vld1.8	{d27[]},[r10]",
				"F9 EA BC 2F", "vld1.8	{d27[],d28[]},[r10]",
				"F9 EA BC 4F", "vld1.16	{d27[]},[r10]",
				"F9 EA BC 6F", "vld1.16	{d27[],d28[]},[r10]",
				"F9 EA BC 8F", "vld1.32	{d27[]},[r10]",
				"F9 EA BC AF", "vld1.32	{d27[],d28[]},[r10]",
				"F9 EA BC 5F", "vld1.16	{d27[]},[r10@16]",
				"F9 EA BC 7F", "vld1.16	{d27[],d28[]},[r10@16]",
				"F9 EA BC 9F", "vld1.32	{d27[]},[r10@32]",
				"F9 EA BC BF", "vld1.32	{d27[],d28[]},[r10@32]",
				"F9 EA BC 0D", "vld1.8	{d27[]},[r10]!",
				"F9 EA BC 2D", "vld1.8	{d27[],d28[]},[r10]!",
				"F9 EA BC 4D", "vld1.16	{d27[]},[r10]!",
				"F9 EA BC 6D", "vld1.16	{d27[],d28[]},[r10]!",
				"F9 EA BC 8D", "vld1.32	{d27[]},[r10]!",
				"F9 EA BC AD", "vld1.32	{d27[],d28[]},[r10]!",
				"F9 EA BC 5D", "vld1.16	{d27[]},[r10@16]!",
				"F9 EA BC 7D", "vld1.16	{d27[],d28[]},[r10@16]!",
				"F9 EA BC 9D", "vld1.32	{d27[]},[r10@32]!",
				"F9 EA BC BD", "vld1.32	{d27[],d28[]},[r10@32]!",
				"F9 EA BC 09", "vld1.8	{d27[]},[r10],r9",
				"F9 EA BC 29", "vld1.8	{d27[],d28[]},[r10],r9",
				"F9 EA BC 49", "vld1.16	{d27[]},[r10],r9",
				"F9 EA BC 69", "vld1.16	{d27[],d28[]},[r10],r9",
				"F9 EA BC 89", "vld1.32	{d27[]},[r10],r9",
				"F9 EA BC A9", "vld1.32	{d27[],d28[]},[r10],r9",
				"F9 EA BC 59", "vld1.16	{d27[]},[r10@16],r9",
				"F9 EA BC 79", "vld1.16	{d27[],d28[]},[r10@16],r9",
				"F9 EA BC 99", "vld1.32	{d27[]},[r10@32],r9",
				"F9 EA BC B9", "vld1.32	{d27[],d28[]},[r10@32],r9",
				"F9 6A B8 0F", "vld2.8	{d27,d28},[r10]",
				"F9 6A B9 0F", "vld2.8	{d27,d29},[r10]",
				"F9 6A B3 0F", "vld2.8	{d27,d28,d29,d30},[r10]",
				"F9 6A B8 4F", "vld2.16	{d27,d28},[r10]",
				"F9 6A B9 4F", "vld2.16	{d27,d29},[r10]",
				"F9 6A B3 4F", "vld2.16	{d27,d28,d29,d30},[r10]",
				"F9 6A B8 8F", "vld2.32	{d27,d28},[r10]",
				"F9 6A B9 8F", "vld2.32	{d27,d29},[r10]",
				"F9 6A B3 8F", "vld2.32	{d27,d28,d29,d30},[r10]",
				"F9 6A B8 1F", "vld2.8	{d27,d28},[r10@64]",
				"F9 6A B8 2F", "vld2.8	{d27,d28},[r10@128]",
				"F9 6A B9 1F", "vld2.8	{d27,d29},[r10@64]",
				"F9 6A B9 2F", "vld2.8	{d27,d29},[r10@128]",
				"F9 6A B3 1F", "vld2.8	{d27,d28,d29,d30},[r10@64]",
				"F9 6A B3 2F", "vld2.8	{d27,d28,d29,d30},[r10@128]",
				"F9 6A B3 3F", "vld2.8	{d27,d28,d29,d30},[r10@256]",
				"F9 6A B8 5F", "vld2.16	{d27,d28},[r10@64]",
				"F9 6A B8 6F", "vld2.16	{d27,d28},[r10@128]",
				"F9 6A B9 5F", "vld2.16	{d27,d29},[r10@64]",
				"F9 6A B9 6F", "vld2.16	{d27,d29},[r10@128]",
				"F9 6A B3 5F", "vld2.16	{d27,d28,d29,d30},[r10@64]",
				"F9 6A B3 6F", "vld2.16	{d27,d28,d29,d30},[r10@128]",
				"F9 6A B3 7F", "vld2.16	{d27,d28,d29,d30},[r10@256]",
				"F9 6A B8 9F", "vld2.32	{d27,d28},[r10@64]",
				"F9 6A B8 AF", "vld2.32	{d27,d28},[r10@128]",
				"F9 6A B9 9F", "vld2.32	{d27,d29},[r10@64]",
				"F9 6A B9 AF", "vld2.32	{d27,d29},[r10@128]",
				"F9 6A B3 9F", "vld2.32	{d27,d28,d29,d30},[r10@64]",
				"F9 6A B3 AF", "vld2.32	{d27,d28,d29,d30},[r10@128]",
				"F9 6A B3 BF", "vld2.32	{d27,d28,d29,d30},[r10@256]",
				"F9 6A B8 0D", "vld2.8	{d27,d28},[r10]!",
				"F9 6A B9 0D", "vld2.8	{d27,d29},[r10]!",
				"F9 6A B3 0D", "vld2.8	{d27,d28,d29,d30},[r10]!",
				"F9 6A B8 4D", "vld2.16	{d27,d28},[r10]!",
				"F9 6A B9 4D", "vld2.16	{d27,d29},[r10]!",
				"F9 6A B3 4D", "vld2.16	{d27,d28,d29,d30},[r10]!",
				"F9 6A B8 8D", "vld2.32	{d27,d28},[r10]!",
				"F9 6A B9 8D", "vld2.32	{d27,d29},[r10]!",
				"F9 6A B3 8D", "vld2.32	{d27,d28,d29,d30},[r10]!",
				"F9 6A B8 1D", "vld2.8	{d27,d28},[r10@64]!",
				"F9 6A B8 2D", "vld2.8	{d27,d28},[r10@128]!",
				"F9 6A B9 1D", "vld2.8	{d27,d29},[r10@64]!",
				"F9 6A B9 2D", "vld2.8	{d27,d29},[r10@128]!",
				"F9 6A B3 1D", "vld2.8	{d27,d28,d29,d30},[r10@64]!",
				"F9 6A B3 2D", "vld2.8	{d27,d28,d29,d30},[r10@128]!",
				"F9 6A B3 3D", "vld2.8	{d27,d28,d29,d30},[r10@256]!",
				"F9 6A B8 5D", "vld2.16	{d27,d28},[r10@64]!",
				"F9 6A B8 6D", "vld2.16	{d27,d28},[r10@128]!",
				"F9 6A B9 5D", "vld2.16	{d27,d29},[r10@64]!",
				"F9 6A B9 6D", "vld2.16	{d27,d29},[r10@128]!",
				"F9 6A B3 5D", "vld2.16	{d27,d28,d29,d30},[r10@64]!",
				"F9 6A B3 6D", "vld2.16	{d27,d28,d29,d30},[r10@128]!",
				"F9 6A B3 7D", "vld2.16	{d27,d28,d29,d30},[r10@256]!",
				"F9 6A B8 9D", "vld2.32	{d27,d28},[r10@64]!",
				"F9 6A B8 AD", "vld2.32	{d27,d28},[r10@128]!",
				"F9 6A B9 9D", "vld2.32	{d27,d29},[r10@64]!",
				"F9 6A B9 AD", "vld2.32	{d27,d29},[r10@128]!",
				"F9 6A B3 9D", "vld2.32	{d27,d28,d29,d30},[r10@64]!",
				"F9 6A B3 AD", "vld2.32	{d27,d28,d29,d30},[r10@128]!",
				"F9 6A B3 BD", "vld2.32	{d27,d28,d29,d30},[r10@256]!",
				"F9 6A B8 09", "vld2.8	{d27,d28},[r10],r9",
				"F9 6A B9 09", "vld2.8	{d27,d29},[r10],r9",
				"F9 6A B3 09", "vld2.8	{d27,d28,d29,d30},[r10],r9",
				"F9 6A B8 49", "vld2.16	{d27,d28},[r10],r9",
				"F9 6A B9 49", "vld2.16	{d27,d29},[r10],r9",
				"F9 6A B3 49", "vld2.16	{d27,d28,d29,d30},[r10],r9",
				"F9 6A B8 89", "vld2.32	{d27,d28},[r10],r9",
				"F9 6A B9 89", "vld2.32	{d27,d29},[r10],r9",
				"F9 6A B3 89", "vld2.32	{d27,d28,d29,d30},[r10],r9",
				"F9 6A B8 19", "vld2.8	{d27,d28},[r10@64],r9",
				"F9 6A B8 29", "vld2.8	{d27,d28},[r10@128],r9",
				"F9 6A B9 19", "vld2.8	{d27,d29},[r10@64],r9",
				"F9 6A B9 29", "vld2.8	{d27,d29},[r10@128],r9",
				"F9 6A B3 19", "vld2.8	{d27,d28,d29,d30},[r10@64],r9",
				"F9 6A B3 29", "vld2.8	{d27,d28,d29,d30},[r10@128],r9",
				"F9 6A B3 39", "vld2.8	{d27,d28,d29,d30},[r10@256],r9",
				"F9 6A B8 59", "vld2.16	{d27,d28},[r10@64],r9",
				"F9 6A B8 69", "vld2.16	{d27,d28},[r10@128],r9",
				"F9 6A B9 59", "vld2.16	{d27,d29},[r10@64],r9",
				"F9 6A B9 69", "vld2.16	{d27,d29},[r10@128],r9",
				"F9 6A B3 59", "vld2.16	{d27,d28,d29,d30},[r10@64],r9",
				"F9 6A B3 69", "vld2.16	{d27,d28,d29,d30},[r10@128],r9",
				"F9 6A B3 79", "vld2.16	{d27,d28,d29,d30},[r10@256],r9",
				"F9 6A B8 99", "vld2.32	{d27,d28},[r10@64],r9",
				"F9 6A B8 A9", "vld2.32	{d27,d28},[r10@128],r9",
				"F9 6A B9 99", "vld2.32	{d27,d29},[r10@64],r9",
				"F9 6A B9 A9", "vld2.32	{d27,d29},[r10@128],r9",
				"F9 6A B3 99", "vld2.32	{d27,d28,d29,d30},[r10@64],r9",
				"F9 6A B3 A9", "vld2.32	{d27,d28,d29,d30},[r10@128],r9",
				"F9 6A B3 B9", "vld2.32	{d27,d28,d29,d30},[r10@256],r9",
				"F9 EA B1 2F", "vld2.8	{d27[1],d28[1]},[r10]",
				"F9 EA B5 4F", "vld2.16	{d27[1],d28[1]},[r10]",
				"F9 EA B5 6F", "vld2.16	{d27[1],d29[1]},[r10]",
				"F9 EA B9 8F", "vld2.32	{d27[1],d28[1]},[r10]",
				"F9 EA B9 CF", "vld2.32	{d27[1],d29[1]},[r10]",
				"F9 EA B1 3F", "vld2.8	{d27[1],d28[1]},[r10@16]",
				"F9 EA B5 5F", "vld2.16	{d27[1],d28[1]},[r10@32]",
				"F9 EA B5 7F", "vld2.16	{d27[1],d29[1]},[r10@32]",
				"F9 EA B9 9F", "vld2.32	{d27[1],d28[1]},[r10@64]",
				"F9 EA B9 DF", "vld2.32	{d27[1],d29[1]},[r10@64]",
				"F9 EA B1 2D", "vld2.8	{d27[1],d28[1]},[r10]!",
				"F9 EA B5 4D", "vld2.16	{d27[1],d28[1]},[r10]!",
				"F9 EA B5 6D", "vld2.16	{d27[1],d29[1]},[r10]!",
				"F9 EA B9 8D", "vld2.32	{d27[1],d28[1]},[r10]!",
				"F9 EA B9 CD", "vld2.32	{d27[1],d29[1]},[r10]!",
				"F9 EA B1 3D", "vld2.8	{d27[1],d28[1]},[r10@16]!",
				"F9 EA B5 5D", "vld2.16	{d27[1],d28[1]},[r10@32]!",
				"F9 EA B5 7D", "vld2.16	{d27[1],d29[1]},[r10@32]!",
				"F9 EA B9 9D", "vld2.32	{d27[1],d28[1]},[r10@64]!",
				"F9 EA B9 DD", "vld2.32	{d27[1],d29[1]},[r10@64]!",
				"F9 EA B1 29", "vld2.8	{d27[1],d28[1]},[r10],r9",
				"F9 EA B5 49", "vld2.16	{d27[1],d28[1]},[r10],r9",
				"F9 EA B5 69", "vld2.16	{d27[1],d29[1]},[r10],r9",
				"F9 EA B9 89", "vld2.32	{d27[1],d28[1]},[r10],r9",
				"F9 EA B9 C9", "vld2.32	{d27[1],d29[1]},[r10],r9",
				"F9 EA B1 39", "vld2.8	{d27[1],d28[1]},[r10@16],r9",
				"F9 EA B5 59", "vld2.16	{d27[1],d28[1]},[r10@32],r9",
				"F9 EA B5 79", "vld2.16	{d27[1],d29[1]},[r10@32],r9",
				"F9 EA B9 99", "vld2.32	{d27[1],d28[1]},[r10@64],r9",
				"F9 EA B9 D9", "vld2.32	{d27[1],d29[1]},[r10@64],r9",
				"F9 EA BD 0F", "vld2.8	{d27[],d28[]},[r10]",
				"F9 EA BD 2F", "vld2.8	{d27[],d29[]},[r10]",
				"F9 EA BD 4F", "vld2.16	{d27[],d28[]},[r10]",
				"F9 EA BD 6F", "vld2.16	{d27[],d29[]},[r10]",
				"F9 EA BD 8F", "vld2.32	{d27[],d28[]},[r10]",
				"F9 EA BD AF", "vld2.32	{d27[],d29[]},[r10]",
				"F9 EA BD 1F", "vld2.8	{d27[],d28[]},[r10@16]",
				"F9 EA BD 3F", "vld2.8	{d27[],d29[]},[r10@16]",
				"F9 EA BD 5F", "vld2.16	{d27[],d28[]},[r10@32]",
				"F9 EA BD 7F", "vld2.16	{d27[],d29[]},[r10@32]",
				"F9 EA BD 9F", "vld2.32	{d27[],d28[]},[r10@64]",
				"F9 EA BD BF", "vld2.32	{d27[],d29[]},[r10@64]",
				"F9 EA BD 0D", "vld2.8	{d27[],d28[]},[r10]!",
				"F9 EA BD 2D", "vld2.8	{d27[],d29[]},[r10]!",
				"F9 EA BD 4D", "vld2.16	{d27[],d28[]},[r10]!",
				"F9 EA BD 6D", "vld2.16	{d27[],d29[]},[r10]!",
				"F9 EA BD 8D", "vld2.32	{d27[],d28[]},[r10]!",
				"F9 EA BD AD", "vld2.32	{d27[],d29[]},[r10]!",
				"F9 EA BD 1D", "vld2.8	{d27[],d28[]},[r10@16]!",
				"F9 EA BD 3D", "vld2.8	{d27[],d29[]},[r10@16]!",
				"F9 EA BD 5D", "vld2.16	{d27[],d28[]},[r10@32]!",
				"F9 EA BD 7D", "vld2.16	{d27[],d29[]},[r10@32]!",
				"F9 EA BD 9D", "vld2.32	{d27[],d28[]},[r10@64]!",
				"F9 EA BD BD", "vld2.32	{d27[],d29[]},[r10@64]!",
				"F9 EA BD 09", "vld2.8	{d27[],d28[]},[r10],r9",
				"F9 EA BD 29", "vld2.8	{d27[],d29[]},[r10],r9",
				"F9 EA BD 49", "vld2.16	{d27[],d28[]},[r10],r9",
				"F9 EA BD 69", "vld2.16	{d27[],d29[]},[r10],r9",
				"F9 EA BD 89", "vld2.32	{d27[],d28[]},[r10],r9",
				"F9 EA BD A9", "vld2.32	{d27[],d29[]},[r10],r9",
				"F9 EA BD 19", "vld2.8	{d27[],d28[]},[r10@16],r9",
				"F9 EA BD 39", "vld2.8	{d27[],d29[]},[r10@16],r9",
				"F9 EA BD 59", "vld2.16	{d27[],d28[]},[r10@32],r9",
				"F9 EA BD 79", "vld2.16	{d27[],d29[]},[r10@32],r9",
				"F9 EA BD 99", "vld2.32	{d27[],d28[]},[r10@64],r9",
				"F9 EA BD B9", "vld2.32	{d27[],d29[]},[r10@64],r9",
				"F9 6A B4 0F", "vld3.8	{d27,d28,d29},[r10]",
				"F9 6A B5 0F", "vld3.8	{d27,d29,d31},[r10]",
				"F9 6A B4 4F", "vld3.16	{d27,d28,d29},[r10]",
				"F9 6A B5 4F", "vld3.16	{d27,d29,d31},[r10]",
				"F9 6A B4 8F", "vld3.32	{d27,d28,d29},[r10]",
				"F9 6A B5 8F", "vld3.32	{d27,d29,d31},[r10]",
				"F9 6A B4 1F", "vld3.8	{d27,d28,d29},[r10@64]",
				"F9 6A B5 1F", "vld3.8	{d27,d29,d31},[r10@64]",
				"F9 6A B4 5F", "vld3.16	{d27,d28,d29},[r10@64]",
				"F9 6A B5 5F", "vld3.16	{d27,d29,d31},[r10@64]",
				"F9 6A B4 9F", "vld3.32	{d27,d28,d29},[r10@64]",
				"F9 6A B5 9F", "vld3.32	{d27,d29,d31},[r10@64]",
				"F9 6A B4 0D", "vld3.8	{d27,d28,d29},[r10]!",
				"F9 6A B5 0D", "vld3.8	{d27,d29,d31},[r10]!",
				"F9 6A B4 4D", "vld3.16	{d27,d28,d29},[r10]!",
				"F9 6A B5 4D", "vld3.16	{d27,d29,d31},[r10]!",
				"F9 6A B4 8D", "vld3.32	{d27,d28,d29},[r10]!",
				"F9 6A B5 8D", "vld3.32	{d27,d29,d31},[r10]!",
				"F9 6A B4 1D", "vld3.8	{d27,d28,d29},[r10@64]!",
				"F9 6A B5 1D", "vld3.8	{d27,d29,d31},[r10@64]!",
				"F9 6A B4 5D", "vld3.16	{d27,d28,d29},[r10@64]!",
				"F9 6A B5 5D", "vld3.16	{d27,d29,d31},[r10@64]!",
				"F9 6A B4 9D", "vld3.32	{d27,d28,d29},[r10@64]!",
				"F9 6A B5 9D", "vld3.32	{d27,d29,d31},[r10@64]!",
				"F9 6A B4 09", "vld3.8	{d27,d28,d29},[r10],r9",
				"F9 6A B5 09", "vld3.8	{d27,d29,d31},[r10],r9",
				"F9 6A B4 49", "vld3.16	{d27,d28,d29},[r10],r9",
				"F9 6A B5 49", "vld3.16	{d27,d29,d31},[r10],r9",
				"F9 6A B4 89", "vld3.32	{d27,d28,d29},[r10],r9",
				"F9 6A B5 89", "vld3.32	{d27,d29,d31},[r10],r9",
				"F9 6A B4 19", "vld3.8	{d27,d28,d29},[r10@64],r9",
				"F9 6A B5 19", "vld3.8	{d27,d29,d31},[r10@64],r9",
				"F9 6A B4 59", "vld3.16	{d27,d28,d29},[r10@64],r9",
				"F9 6A B5 59", "vld3.16	{d27,d29,d31},[r10@64],r9",
				"F9 6A B4 99", "vld3.32	{d27,d28,d29},[r10@64],r9",
				"F9 6A B5 99", "vld3.32	{d27,d29,d31},[r10@64],r9",
				"F9 EA B2 2F", "vld3.8	{d27[1],d28[1],d29[1]},[r10]",
				"F9 EA B6 4F", "vld3.16	{d27[1],d28[1],d29[1]},[r10]",
				"F9 EA B6 6F", "vld3.16	{d27[1],d29[1],d31[1]},[r10]",
				"F9 EA BA 8F", "vld3.32	{d27[1],d28[1],d29[1]},[r10]",
				"F9 EA BA CF", "vld3.32	{d27[1],d29[1],d31[1]},[r10]",
				"F9 EA B2 2D", "vld3.8	{d27[1],d28[1],d29[1]},[r10]!",
				"F9 EA B6 4D", "vld3.16	{d27[1],d28[1],d29[1]},[r10]!",
				"F9 EA B6 6D", "vld3.16	{d27[1],d29[1],d31[1]},[r10]!",
				"F9 EA BA 8D", "vld3.32	{d27[1],d28[1],d29[1]},[r10]!",
				"F9 EA BA CD", "vld3.32	{d27[1],d29[1],d31[1]},[r10]!",
				"F9 EA B2 29", "vld3.8	{d27[1],d28[1],d29[1]},[r10],r9",
				"F9 EA B6 49", "vld3.16	{d27[1],d28[1],d29[1]},[r10],r9",
				"F9 EA B6 69", "vld3.16	{d27[1],d29[1],d31[1]},[r10],r9",
				"F9 EA BA 89", "vld3.32	{d27[1],d28[1],d29[1]},[r10],r9",
				"F9 EA BA C9", "vld3.32	{d27[1],d29[1],d31[1]},[r10],r9",
				"F9 EA BE 0F", "vld3.8	{d27[],d28[],d29[]},[r10]",
				"F9 EA BE 2F", "vld3.8	{d27[],d29[],d31[]},[r10]",
				"F9 EA BE 4F", "vld3.16	{d27[],d28[],d29[]},[r10]",
				"F9 EA BE 6F", "vld3.16	{d27[],d29[],d31[]},[r10]",
				"F9 EA BE 8F", "vld3.32	{d27[],d28[],d29[]},[r10]",
				"F9 EA BE AF", "vld3.32	{d27[],d29[],d31[]},[r10]",
				"F9 EA BE 0D", "vld3.8	{d27[],d28[],d29[]},[r10]!",
				"F9 EA BE 2D", "vld3.8	{d27[],d29[],d31[]},[r10]!",
				"F9 EA BE 4D", "vld3.16	{d27[],d28[],d29[]},[r10]!",
				"F9 EA BE 6D", "vld3.16	{d27[],d29[],d31[]},[r10]!",
				"F9 EA BE 8D", "vld3.32	{d27[],d28[],d29[]},[r10]!",
				"F9 EA BE AD", "vld3.32	{d27[],d29[],d31[]},[r10]!",
				"F9 EA BE 09", "vld3.8	{d27[],d28[],d29[]},[r10],r9",
				"F9 EA BE 29", "vld3.8	{d27[],d29[],d31[]},[r10],r9",
				"F9 EA BE 49", "vld3.16	{d27[],d28[],d29[]},[r10],r9",
				"F9 EA BE 69", "vld3.16	{d27[],d29[],d31[]},[r10],r9",
				"F9 EA BE 89", "vld3.32	{d27[],d28[],d29[]},[r10],r9",
				"F9 EA BE A9", "vld3.32	{d27[],d29[],d31[]},[r10],r9",
				"F9 6A B0 0F", "vld4.8	{d27,d28,d29,d30},[r10]",
				"F9 6A 91 0F", "vld4.8	{d25,d27,d29,d31},[r10]",
				"F9 6A B0 4F", "vld4.16	{d27,d28,d29,d30},[r10]",
				"F9 6A 91 4F", "vld4.16	{d25,d27,d29,d31},[r10]",
				"F9 6A B0 8F", "vld4.32	{d27,d28,d29,d30},[r10]",
				"F9 6A 91 8F", "vld4.32	{d25,d27,d29,d31},[r10]",
				"F9 6A B0 1F", "vld4.8	{d27,d28,d29,d30},[r10@64]",
				"F9 6A B0 2F", "vld4.8	{d27,d28,d29,d30},[r10@128]",
				"F9 6A B0 3F", "vld4.8	{d27,d28,d29,d30},[r10@256]",
				"F9 6A 91 1F", "vld4.8	{d25,d27,d29,d31},[r10@64]",
				"F9 6A 91 2F", "vld4.8	{d25,d27,d29,d31},[r10@128]",
				"F9 6A 91 3F", "vld4.8	{d25,d27,d29,d31},[r10@256]",
				"F9 6A B0 5F", "vld4.16	{d27,d28,d29,d30},[r10@64]",
				"F9 6A B0 6F", "vld4.16	{d27,d28,d29,d30},[r10@128]",
				"F9 6A B0 7F", "vld4.16	{d27,d28,d29,d30},[r10@256]",
				"F9 6A 91 5F", "vld4.16	{d25,d27,d29,d31},[r10@64]",
				"F9 6A 91 6F", "vld4.16	{d25,d27,d29,d31},[r10@128]",
				"F9 6A 91 7F", "vld4.16	{d25,d27,d29,d31},[r10@256]",
				"F9 6A B0 9F", "vld4.32	{d27,d28,d29,d30},[r10@64]",
				"F9 6A B0 AF", "vld4.32	{d27,d28,d29,d30},[r10@128]",
				"F9 6A B0 BF", "vld4.32	{d27,d28,d29,d30},[r10@256]",
				"F9 6A 91 9F", "vld4.32	{d25,d27,d29,d31},[r10@64]",
				"F9 6A 91 AF", "vld4.32	{d25,d27,d29,d31},[r10@128]",
				"F9 6A 91 BF", "vld4.32	{d25,d27,d29,d31},[r10@256]",
				"F9 6A B0 0D", "vld4.8	{d27,d28,d29,d30},[r10]!",
				"F9 6A 91 0D", "vld4.8	{d25,d27,d29,d31},[r10]!",
				"F9 6A B0 4D", "vld4.16	{d27,d28,d29,d30},[r10]!",
				"F9 6A 91 4D", "vld4.16	{d25,d27,d29,d31},[r10]!",
				"F9 6A B0 8D", "vld4.32	{d27,d28,d29,d30},[r10]!",
				"F9 6A 91 8D", "vld4.32	{d25,d27,d29,d31},[r10]!",
				"F9 6A B0 1D", "vld4.8	{d27,d28,d29,d30},[r10@64]!",
				"F9 6A B0 2D", "vld4.8	{d27,d28,d29,d30},[r10@128]!",
				"F9 6A B0 3D", "vld4.8	{d27,d28,d29,d30},[r10@256]!",
				"F9 6A 91 1D", "vld4.8	{d25,d27,d29,d31},[r10@64]!",
				"F9 6A 91 2D", "vld4.8	{d25,d27,d29,d31},[r10@128]!",
				"F9 6A 91 3D", "vld4.8	{d25,d27,d29,d31},[r10@256]!",
				"F9 6A B0 5D", "vld4.16	{d27,d28,d29,d30},[r10@64]!",
				"F9 6A B0 6D", "vld4.16	{d27,d28,d29,d30},[r10@128]!",
				"F9 6A B0 7D", "vld4.16	{d27,d28,d29,d30},[r10@256]!",
				"F9 6A 91 5D", "vld4.16	{d25,d27,d29,d31},[r10@64]!",
				"F9 6A 91 6D", "vld4.16	{d25,d27,d29,d31},[r10@128]!",
				"F9 6A 91 7D", "vld4.16	{d25,d27,d29,d31},[r10@256]!",
				"F9 6A B0 9D", "vld4.32	{d27,d28,d29,d30},[r10@64]!",
				"F9 6A B0 AD", "vld4.32	{d27,d28,d29,d30},[r10@128]!",
				"F9 6A B0 BD", "vld4.32	{d27,d28,d29,d30},[r10@256]!",
				"F9 6A 91 9D", "vld4.32	{d25,d27,d29,d31},[r10@64]!",
				"F9 6A 91 AD", "vld4.32	{d25,d27,d29,d31},[r10@128]!",
				"F9 6A 91 BD", "vld4.32	{d25,d27,d29,d31},[r10@256]!",
				"F9 6A B0 09", "vld4.8	{d27,d28,d29,d30},[r10],r9",
				"F9 6A 91 09", "vld4.8	{d25,d27,d29,d31},[r10],r9",
				"F9 6A B0 49", "vld4.16	{d27,d28,d29,d30},[r10],r9",
				"F9 6A 91 49", "vld4.16	{d25,d27,d29,d31},[r10],r9",
				"F9 6A B0 89", "vld4.32	{d27,d28,d29,d30},[r10],r9",
				"F9 6A 91 89", "vld4.32	{d25,d27,d29,d31},[r10],r9",
				"F9 6A B0 19", "vld4.8	{d27,d28,d29,d30},[r10@64],r9",
				"F9 6A B0 29", "vld4.8	{d27,d28,d29,d30},[r10@128],r9",
				"F9 6A B0 39", "vld4.8	{d27,d28,d29,d30},[r10@256],r9",
				"F9 6A 91 19", "vld4.8	{d25,d27,d29,d31},[r10@64],r9",
				"F9 6A 91 29", "vld4.8	{d25,d27,d29,d31},[r10@128],r9",
				"F9 6A 91 39", "vld4.8	{d25,d27,d29,d31},[r10@256],r9",
				"F9 6A B0 59", "vld4.16	{d27,d28,d29,d30},[r10@64],r9",
				"F9 6A B0 69", "vld4.16	{d27,d28,d29,d30},[r10@128],r9",
				"F9 6A B0 79", "vld4.16	{d27,d28,d29,d30},[r10@256],r9",
				"F9 6A 91 59", "vld4.16	{d25,d27,d29,d31},[r10@64],r9",
				"F9 6A 91 69", "vld4.16	{d25,d27,d29,d31},[r10@128],r9",
				"F9 6A 91 79", "vld4.16	{d25,d27,d29,d31},[r10@256],r9",
				"F9 6A B0 99", "vld4.32	{d27,d28,d29,d30},[r10@64],r9",
				"F9 6A B0 A9", "vld4.32	{d27,d28,d29,d30},[r10@128],r9",
				"F9 6A B0 B9", "vld4.32	{d27,d28,d29,d30},[r10@256],r9",
				"F9 6A 91 99", "vld4.32	{d25,d27,d29,d31},[r10@64],r9",
				"F9 6A 91 A9", "vld4.32	{d25,d27,d29,d31},[r10@128],r9",
				"F9 6A 91 B9", "vld4.32	{d25,d27,d29,d31},[r10@256],r9",
				"F9 EA B3 2F", "vld4.8	{d27[1],d28[1],d29[1],d30[1]},[r10]",
				"F9 EA B7 4F", "vld4.16	{d27[1],d28[1],d29[1],d30[1]},[r10]",
				"F9 EA 97 6F", "vld4.16	{d25[1],d27[1],d29[1],d31[1]},[r10]",
				"F9 EA BB 8F", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10]",
				"F9 EA 9B CF", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10]",
				"F9 EA B3 3F", "vld4.8	{d27[1],d28[1],d29[1],d30[1]},[r10@32]",
				"F9 EA B7 5F", "vld4.16	{d27[1],d28[1],d29[1],d30[1]},[r10@64]",
				"F9 EA 97 7F", "vld4.16	{d25[1],d27[1],d29[1],d31[1]},[r10@64]",
				"F9 EA BB 9F", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@64]",
				"F9 EA BB AF", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@128]",
				"F9 EA 9B DF", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@64]",
				"F9 EA 9B EF", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@128]",
				"F9 EA B3 2D", "vld4.8	{d27[1],d28[1],d29[1],d30[1]},[r10]!",
				"F9 EA B7 4D", "vld4.16	{d27[1],d28[1],d29[1],d30[1]},[r10]!",
				"F9 EA 97 6D", "vld4.16	{d25[1],d27[1],d29[1],d31[1]},[r10]!",
				"F9 EA BB 8D", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10]!",
				"F9 EA 9B CD", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10]!",
				"F9 EA B3 3D", "vld4.8	{d27[1],d28[1],d29[1],d30[1]},[r10@32]!",
				"F9 EA B7 5D", "vld4.16	{d27[1],d28[1],d29[1],d30[1]},[r10@64]!",
				"F9 EA 97 7D", "vld4.16	{d25[1],d27[1],d29[1],d31[1]},[r10@64]!",
				"F9 EA BB 9D", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@64]!",
				"F9 EA BB AD", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@128]!",
				"F9 EA 9B DD", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@64]!",
				"F9 EA 9B ED", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@128]!",
				"F9 EA B3 29", "vld4.8	{d27[1],d28[1],d29[1],d30[1]},[r10],r9",
				"F9 EA B7 49", "vld4.16	{d27[1],d28[1],d29[1],d30[1]},[r10],r9",
				"F9 EA 97 69", "vld4.16	{d25[1],d27[1],d29[1],d31[1]},[r10],r9",
				"F9 EA BB 89", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10],r9",
				"F9 EA 9B C9", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10],r9",
				"F9 EA B3 39", "vld4.8	{d27[1],d28[1],d29[1],d30[1]},[r10@32],r9",
				"F9 EA B7 59", "vld4.16	{d27[1],d28[1],d29[1],d30[1]},[r10@64],r9",
				"F9 EA 97 79", "vld4.16	{d25[1],d27[1],d29[1],d31[1]},[r10@64],r9",
				"F9 EA BB 99", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@64],r9",
				"F9 EA BB A9", "vld4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@128],r9",
				"F9 EA 9B D9", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@64],r9",
				"F9 EA 9B E9", "vld4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@128],r9",
				"F9 EA BF 0F", "vld4.8	{d27[],d28[],d29[],d30[]},[r10]",
				"F9 EA 9F 2F", "vld4.8	{d25[],d27[],d29[],d31[]},[r10]",
				"F9 EA BF 4F", "vld4.16	{d27[],d28[],d29[],d30[]},[r10]",
				"F9 EA 9F 6F", "vld4.16	{d25[],d27[],d29[],d31[]},[r10]",
				"F9 EA BF 8F", "vld4.32	{d27[],d28[],d29[],d30[]},[r10]",
				"F9 EA 9F AF", "vld4.32	{d25[],d27[],d29[],d31[]},[r10]",
				"F9 EA BF 1F", "vld4.8	{d27[],d28[],d29[],d30[]},[r10@32]",
				"F9 EA 9F 3F", "vld4.8	{d25[],d27[],d29[],d31[]},[r10@32]",
				"F9 EA BF 5F", "vld4.16	{d27[],d28[],d29[],d30[]},[r10@64]",
				"F9 EA 9F 7F", "vld4.16	{d25[],d27[],d29[],d31[]},[r10@64]",
				"F9 EA BF 9F", "vld4.32	{d27[],d28[],d29[],d30[]},[r10@64]",
				"F9 EA BF DF", "vld4.32	{d27[],d28[],d29[],d30[]},[r10@128]",
				"F9 EA 9F BF", "vld4.32	{d25[],d27[],d29[],d31[]},[r10@64]",
				"F9 EA 9F FF", "vld4.32	{d25[],d27[],d29[],d31[]},[r10@128]",
				"F9 EA BF 0D", "vld4.8	{d27[],d28[],d29[],d30[]},[r10]!",
				"F9 EA 9F 2D", "vld4.8	{d25[],d27[],d29[],d31[]},[r10]!",
				"F9 EA BF 4D", "vld4.16	{d27[],d28[],d29[],d30[]},[r10]!",
				"F9 EA 9F 6D", "vld4.16	{d25[],d27[],d29[],d31[]},[r10]!",
				"F9 EA BF 8D", "vld4.32	{d27[],d28[],d29[],d30[]},[r10]!",
				"F9 EA 9F AD", "vld4.32	{d25[],d27[],d29[],d31[]},[r10]!",
				"F9 EA BF 1D", "vld4.8	{d27[],d28[],d29[],d30[]},[r10@32]!",
				"F9 EA 9F 3D", "vld4.8	{d25[],d27[],d29[],d31[]},[r10@32]!",
				"F9 EA BF 5D", "vld4.16	{d27[],d28[],d29[],d30[]},[r10@64]!",
				"F9 EA 9F 7D", "vld4.16	{d25[],d27[],d29[],d31[]},[r10@64]!",
				"F9 EA BF 9D", "vld4.32	{d27[],d28[],d29[],d30[]},[r10@64]!",
				"F9 EA BF DD", "vld4.32	{d27[],d28[],d29[],d30[]},[r10@128]!",
				"F9 EA 9F BD", "vld4.32	{d25[],d27[],d29[],d31[]},[r10@64]!",
				"F9 EA 9F FD", "vld4.32	{d25[],d27[],d29[],d31[]},[r10@128]!",
				"F9 EA BF 09", "vld4.8	{d27[],d28[],d29[],d30[]},[r10],r9",
				"F9 EA 9F 29", "vld4.8	{d25[],d27[],d29[],d31[]},[r10],r9",
				"F9 EA BF 49", "vld4.16	{d27[],d28[],d29[],d30[]},[r10],r9",
				"F9 EA 9F 69", "vld4.16	{d25[],d27[],d29[],d31[]},[r10],r9",
				"F9 EA BF 89", "vld4.32	{d27[],d28[],d29[],d30[]},[r10],r9",
				"F9 EA 9F A9", "vld4.32	{d25[],d27[],d29[],d31[]},[r10],r9",
				"F9 EA BF 19", "vld4.8	{d27[],d28[],d29[],d30[]},[r10@32],r9",
				"F9 EA 9F 39", "vld4.8	{d25[],d27[],d29[],d31[]},[r10@32],r9",
				"F9 EA BF 59", "vld4.16	{d27[],d28[],d29[],d30[]},[r10@64],r9",
				"F9 EA 9F 79", "vld4.16	{d25[],d27[],d29[],d31[]},[r10@64],r9",
				"F9 EA BF 99", "vld4.32	{d27[],d28[],d29[],d30[]},[r10@64],r9",
				"F9 EA BF D9", "vld4.32	{d27[],d28[],d29[],d30[]},[r10@128],r9",
				"F9 EA 9F B9", "vld4.32	{d25[],d27[],d29[],d31[]},[r10@64],r9",
				"F9 EA 9F F9", "vld4.32	{d25[],d27[],d29[],d31[]},[r10@128],r9",
				"EC BA AA 03", "vldmia	r10!,{s20-s22}",
				"ED 3A AA 03", "vldmdb	r10!,{s20-s22}",
				"EC FA 4B 06", "vldmia	r10!,{d20-d22}",
				"ED 7A 4B 06", "vldmdb	r10!,{d20-d22}",
				"EC 9A AA 03", "vldmia	r10,{s20-s22}",
				"EC DA 4B 06", "vldmia	r10,{d20-d22}",
				"ED 5F 5B 21", "vldr.64	d21,[pc,#-0x84]",
				"ED DF 5B 21", "vldr.64	d21,[pc,#0x84]",
				"ED DA 5B 00", "vldr.64	d21,[r10]",
				"ED 5A 5B 21", "vldr.64	d21,[r10,#-0x84]",
				"ED DA 5B 00", "vldr.64	d21,[r10]",
				"ED DA 5B 21", "vldr.64	d21,[r10,#0x84]",
				"ED 5F AA 21", "vldr.32	s21,[pc,#-0x84]",
				"ED DF AA 21", "vldr.32	s21,[pc,#0x84]",
				"ED DA AA 00", "vldr.32	s21,[r10]",
				"ED 5A AA 21", "vldr.32	s21,[r10,#-0x84]",
				"ED DA AA 00", "vldr.32	s21,[r10]",
				"ED DA AA 21", "vldr.32	s21,[r10,#0x84]",
				"EF 49 56 AA", "vmax.s8	d21,d25,d26",
				"EF 59 56 AA", "vmax.s16	d21,d25,d26",
				"EF 69 56 AA", "vmax.s32	d21,d25,d26",
				"FF 49 56 AA", "vmax.u8	d21,d25,d26",
				"FF 59 56 AA", "vmax.u16	d21,d25,d26",
				"FF 69 56 AA", "vmax.u32	d21,d25,d26",
				"EF 4C 66 EE", "vmax.s8	q11,q14,q15",
				"EF 5C 66 EE", "vmax.s16	q11,q14,q15",
				"EF 6C 66 EE", "vmax.s32	q11,q14,q15",
				"FF 4C 66 EE", "vmax.u8	q11,q14,q15",
				"FF 5C 66 EE", "vmax.u16	q11,q14,q15",
				"FF 6C 66 EE", "vmax.u32	q11,q14,q15",
				"EF 49 5F AA", "vmax.f32	d21,d25,d26",
				"EF 4C 6F EE", "vmax.f32	q11,q14,q15",
				"EF 49 56 BA", "vmin.s8	d21,d25,d26",
				"EF 59 56 BA", "vmin.s16	d21,d25,d26",
				"EF 69 56 BA", "vmin.s32	d21,d25,d26",
				"FF 49 56 BA", "vmin.u8	d21,d25,d26",
				"FF 59 56 BA", "vmin.u16	d21,d25,d26",
				"FF 69 56 BA", "vmin.u32	d21,d25,d26",
				"EF 4C 66 FE", "vmin.s8	q11,q14,q15",
				"EF 5C 66 FE", "vmin.s16	q11,q14,q15",
				"EF 6C 66 FE", "vmin.s32	q11,q14,q15",
				"FF 4C 66 FE", "vmin.u8	q11,q14,q15",
				"FF 5C 66 FE", "vmin.u16	q11,q14,q15",
				"FF 6C 66 FE", "vmin.u32	q11,q14,q15",
				"EF 69 5F AA", "vmin.f32	d21,d25,d26",
				"EF 6C 6F EE", "vmin.f32	q11,q14,q15",
				"EF D9 50 CF", "vmla.i16	d21,d25,d7[1]",
				"EF E9 50 EF", "vmla.i32	d21,d25,d15[1]",
				"EF E9 51 EF", "vmla.f32	d21,d25,d15[1]",
				"EF 49 59 AA", "vmla.i8	d21,d25,d26",
				"EF 59 59 AA", "vmla.i16	d21,d25,d26",
				"EF 69 59 AA", "vmla.i32	d21,d25,d26",
				"FF DC 60 CF", "vmla.i16	q11,q14,d7[1]",
				"FF EC 60 EF", "vmla.i32	q11,q14,d15[1]",
				"EF 4C 69 EE", "vmla.i8	q11,q14,q15",
				"EF 5C 69 EE", "vmla.i16	q11,q14,q15",
				"EF 6C 69 EE", "vmla.i32	q11,q14,q15",
				"EF 49 5D BA", "vmla.f32	d21,d25,d26",
				"EF 4C 6D FE", "vmla.f32	q11,q14,q15",
				"EE 4C AA 8D", "vmla.f32	s21,s25,s26",
				"EE 49 5B AA", "vmla.f64	d21,d25,d26",
				"EF D9 62 CF", "vmlal.s16	q11,d25,d7[1]",
				"EF E9 62 EF", "vmlal.s32	q11,d25,d15[1]",
				"FF D9 62 CF", "vmlal.u16	q11,d25,d7[1]",
				"FF E9 62 EF", "vmlal.u32	q11,d25,d15[1]",
				"EF C9 68 AA", "vmlal.s8	q11,d25,d26",
				"EF D9 68 AA", "vmlal.s16	q11,d25,d26",
				"EF E9 68 AA", "vmlal.s32	q11,d25,d26",
				"FF C9 68 AA", "vmlal.u8	q11,d25,d26",
				"FF D9 68 AA", "vmlal.u16	q11,d25,d26",
				"FF E9 68 AA", "vmlal.u32	q11,d25,d26",
				"EF D9 54 CF", "vmls.i16	d21,d25,d7[1]",
				"EF E9 54 EF", "vmls.i32	d21,d25,d15[1]",
				"EF E9 55 EF", "vmls.f32	d21,d25,d15[1]",
				"FF 49 59 AA", "vmls.i8	d21,d25,d26",
				"FF 59 59 AA", "vmls.i16	d21,d25,d26",
				"FF 69 59 AA", "vmls.i32	d21,d25,d26",
				"FF DC 64 CF", "vmls.i16	q11,q14,d7[1]",
				"FF EC 64 EF", "vmls.i32	q11,q14,d15[1]",
				"FF EC 65 EF", "vmls.f32	q11,q14,d15[1]",
				"FF 4C 69 EE", "vmls.i8	q11,q14,q15",
				"FF 5C 69 EE", "vmls.i16	q11,q14,q15",
				"FF 6C 69 EE", "vmls.i32	q11,q14,q15",
				"EF 69 5D BA", "vmls.f32	d21,d25,d26",
				"EF 6C 6D FE", "vmls.f32	q11,q14,q15",
				"EE 4C AA CD", "vmls.f32	s21,s25,s26",
				"EE 49 5B EA", "vmls.f64	d21,d25,d26",
				"EF D9 66 CF", "vmlsl.s16	q11,d25,d7[1]",
				"EF E9 66 EF", "vmlsl.s32	q11,d25,d15[1]",
				"FF D9 66 CF", "vmlsl.u16	q11,d25,d7[1]",
				"FF E9 66 EF", "vmlsl.u32	q11,d25,d15[1]",
				"EF C9 6A AA", "vmlsl.s8	q11,d25,d26",
				"EF D9 6A AA", "vmlsl.s16	q11,d25,d26",
				"EF E9 6A AA", "vmlsl.s32	q11,d25,d26",
				"FF C9 6A AA", "vmlsl.u8	q11,d25,d26",
				"FF D9 6A AA", "vmlsl.u16	q11,d25,d26",
				"FF E9 6A AA", "vmlsl.u32	q11,d25,d26",
			    "EF 6A 51 BA", "vmov	d21,d26",
			    "EF 6E 61 FE", "vmov	q11,q15",
				"EC 46 5B 3A", "vmov	d26,r5,r6",
				"EC 56 5B 3A", "vmov	r5,r6,d26",
				"EC 56 5A 1D", "vmov	r5,r6,s26,s27",
				"EE 1C 5A 90", "vmov	r5,s25",
				"EE 0C 5A 90", "vmov	s25,r5",
				"EE 0C 5A 90", "vmov	s25,r5",
				"EC 46 5A 1D", "vmov	s26,s27,r5,r6",
				"FF C0 5E 19", "vmov.i8	d21,#0x89",
				"FF C0 58 19", "vmov.i16	d21,#0x89",
				"FF C0 50 19", "vmov.i32	d21,#0x89",
				"EF C0 5E 30", "vmov.i64	d21,#0x0",
				"FF C0 6E 59", "vmov.i8	q11,#0x89",
				"FF C0 68 59", "vmov.i16	q11,#0x89",
				"FF C0 60 59", "vmov.i32	q11,#0x89",
				"EF C0 6E 70", "vmov.i64	q11,#0x0",
				"EE 5B 5B B0", "vmov.s8	r5,d27[1]",
				"EE 1B 5B F0", "vmov.s16	r5,d27[1]",
				"EE DB 5B B0", "vmov.u8	r5,d27[1]",
				"EE 9B 5B F0", "vmov.u16	r5,d27[1]",
				"EE 3B 5B 90", "vmov.32	r5,d27[1]",
				"EE 4B 5B B0", "vmov.8	d27[1],r5",
				"EE 0B 5B F0", "vmov.16	d27[1],r5",
				"EE 2B 5B 90", "vmov.32	d27[1],r5",
				"EE B7 BA 00", "vmov.f32	s22,#0x70",	// originally "vmov.f32 s22,#1.0"
				"EE F0 AA 4D", "vmov.f32	s21,s26",
				"EE F7 6B 00", "vmov.f64	d22,#0x70",	// originally "vmov.f64 d22,#1.0"
				"EE F0 5B 6A", "vmov.f64	d21,d26",
				"EF C8 6A 3A", "vmovl.s8	q11,d26",
				"EF D0 6A 3A", "vmovl.s16	q11,d26",
				"EF E0 6A 3A", "vmovl.s32	q11,d26",
				"FF C8 6A 3A", "vmovl.u8	q11,d26",
				"FF D0 6A 3A", "vmovl.u16	q11,d26",
				"FF E0 6A 3A", "vmovl.u32	q11,d26",
				"FF F2 52 2E", "vmovn.i16	d21,q15",
				"FF F6 52 2E", "vmovn.i32	d21,q15",
				"FF FA 52 2E", "vmovn.i64	d21,q15",
				"EE F0 5A 10", "vmrs	r5,fpsid",
				"EE F1 5A 10", "vmrs	r5,fpscr",
				"EE F6 5A 10", "vmrs	r5,mvfr1",
				"EE F7 5A 10", "vmrs	r5,mvfr0",
				"EE F8 5A 10", "vmrs	r5,fpexc",
				"EE F9 5A 10", "vmrs	r5,fpinst",
				"EE FA 5A 10", "vmrs	r5,fpinst2",
				"EE E0 5A 10", "vmsr	fpsid,r5",
				"EE E1 5A 10", "vmsr	fpscr,r5",
				"EE E8 5A 10", "vmsr	fpexc,r5",
				"EE E9 5A 10", "vmsr	fpinst,r5",
				"EE EA 5A 10", "vmsr	fpinst2,r5",
				"EF D9 58 CF", "vmul.i16	d21,d25,d7[1]",
				"EF E9 58 EF", "vmul.i32	d21,d25,d15[1]",
				"EF E9 59 EF", "vmul.f32	d21,d25,d15[1]",
				"EF 49 59 BA", "vmul.i8	d21,d25,d26",
				"EF 49 59 BA", "vmul.i8	d21,d25,d26",
				"EF 59 59 BA", "vmul.i16	d21,d25,d26",
				"EF 69 59 BA", "vmul.i32	d21,d25,d26",
				"FF 49 59 BA", "vmul.p8	d21,d25,d26",
				"FF DC 68 CF", "vmul.i16	q11,q14,d7[1]",
				"FF EC 68 EF", "vmul.i32	q11,q14,d15[1]",
				"FF EC 69 EF", "vmul.f32	q11,q14,d15[1]",
				"EF 4C 69 FE", "vmul.i8	q11,q14,q15",
				"EF 5C 69 FE", "vmul.i16	q11,q14,q15",
				"EF 6C 69 FE", "vmul.i32	q11,q14,q15",
				"FF 4C 69 FE", "vmul.p8	q11,q14,q15",
				"FF 49 5D BA", "vmul.f32	d21,d25,d26",
				"FF 4C 6D FE", "vmul.f32	q11,q14,q15",
				"EE 6C AA 8D", "vmul.f32	s21,s25,s26",
				"EE 69 5B AA", "vmul.f64	d21,d25,d26",
				"EF D9 6A CF", "vmull.s16	q11,d25,d7[1]",
				"EF E9 6A EF", "vmull.s32	q11,d25,d15[1]",
				"FF D9 6A CF", "vmull.u16	q11,d25,d7[1]",
				"FF E9 6A EF", "vmull.u32	q11,d25,d15[1]",
				"EF C9 6C AA", "vmull.s8	q11,d25,d26",
				"EF D9 6C AA", "vmull.s16	q11,d25,d26",
				"EF E9 6C AA", "vmull.s32	q11,d25,d26",
				"FF C9 6C AA", "vmull.u8	q11,d25,d26",
				"FF D9 6C AA", "vmull.u16	q11,d25,d26",
				"FF E9 6C AA", "vmull.u32	q11,d25,d26",
				"EF C9 6E AA", "vmull.p8	q11,d25,d26",
				"FF F0 55 AA", "vmvn	d21,d26",
				"FF F0 65 EE", "vmvn	q11,q15",
				"FF C0 58 37", "vmvn.i16	d21,#0x87",
				"FF C0 50 37", "vmvn.i32	d21,#0x87",
				"FF C0 68 77", "vmvn.i16	q11,#0x87",
				"FF C0 60 77", "vmvn.i32	q11,#0x87",
				"FF F1 53 AA", "vneg.s8	d21,d26",
				"FF F5 53 AA", "vneg.s16	d21,d26",
				"FF F9 53 AA", "vneg.s32	d21,d26",
				"FF F9 57 AA", "vneg.f32	d21,d26",
				"EE F1 5B 6A", "vneg.f64	d21,d26",
				"FF F1 63 EE", "vneg.s8	q11,q15",
				"FF F5 63 EE", "vneg.s16	q11,q15",
				"FF F9 63 EE", "vneg.s32	q11,q15",
				"FF F9 67 EE", "vneg.f32	q11,q15",
				"EE F1 AA 4D", "vneg.f32	s21,s26",
				"EE 5C AA CD", "vnmla.f32	s21,s25,s26",
				"EE 59 5B EA", "vnmla.f64	d21,d25,d26",
				"EE 5C AA 8D", "vnmls.f32	s21,s25,s26",
				"EE 59 5B AA", "vnmls.f64	d21,d25,d26",
				"EE 6C AA CD", "vnmul.f32	s21,s25,s26",
				"EE 69 5B EA", "vnmul.f64	d21,d25,d26",
				"EF 79 51 BA", "vorn	d21,d25,d26",
				"EF 7C 61 FE", "vorn	q11,q14,q15",
				"EF 69 51 BA", "vorr	d21,d25,d26",
				"EF 6C 61 FE", "vorr	q11,q14,q15",
				"FF C0 59 17", "vorr.i16	d21,#0x87",
				"FF C0 51 17", "vorr.i32	d21,#0x87",
				"FF C0 69 57", "vorr.i16	q11,#0x87",
				"FF C0 61 57", "vorr.i32	q11,#0x87",
				"FF F0 56 2A", "vpadal.s8	d21,d26",
				"FF F4 56 2A", "vpadal.s16	d21,d26",
				"FF F8 56 2A", "vpadal.s32	d21,d26",
				"FF F0 56 AA", "vpadal.u8	d21,d26",
				"FF F4 56 AA", "vpadal.u16	d21,d26",
				"FF F8 56 AA", "vpadal.u32	d21,d26",
				"FF F0 66 6E", "vpadal.s8	q11,q15",
				"FF F4 66 6E", "vpadal.s16	q11,q15",
				"FF F8 66 6E", "vpadal.s32	q11,q15",
				"FF F0 66 EE", "vpadal.u8	q11,q15",
				"FF F4 66 EE", "vpadal.u16	q11,q15",
				"FF F8 66 EE", "vpadal.u32	q11,q15",
				"EF 49 5B BA", "vpadd.i8	d21,d25,d26",
				"EF 59 5B BA", "vpadd.i16	d21,d25,d26",
				"EF 69 5B BA", "vpadd.i32	d21,d25,d26",
				"FF 49 5D AA", "vpadd.f32	d21,d25,d26",
				"FF F0 52 2A", "vpaddl.s8	d21,d26",
				"FF F4 52 2A", "vpaddl.s16	d21,d26",
				"FF F8 52 2A", "vpaddl.s32	d21,d26",
				"FF F0 52 AA", "vpaddl.u8	d21,d26",
				"FF F4 52 AA", "vpaddl.u16	d21,d26",
				"FF F8 52 AA", "vpaddl.u32	d21,d26",
				"FF F0 62 6E", "vpaddl.s8	q11,q15",
				"FF F4 62 6E", "vpaddl.s16	q11,q15",
				"FF F8 62 6E", "vpaddl.s32	q11,q15",
				"FF F0 62 EE", "vpaddl.u8	q11,q15",
				"FF F4 62 EE", "vpaddl.u16	q11,q15",
				"FF F8 62 EE", "vpaddl.u32	q11,q15",
				"EF 49 5A AA", "vpmax.s8	d21,d25,d26",
				"EF 59 5A AA", "vpmax.s16	d21,d25,d26",
				"EF 69 5A AA", "vpmax.s32	d21,d25,d26",
				"FF 49 5A AA", "vpmax.u8	d21,d25,d26",
				"FF 59 5A AA", "vpmax.u16	d21,d25,d26",
				"FF 69 5A AA", "vpmax.u32	d21,d25,d26",
				"FF 49 5F AA", "vpmax.f32	d21,d25,d26",
				"EF 49 5A BA", "vpmin.s8	d21,d25,d26",
				"EF 59 5A BA", "vpmin.s16	d21,d25,d26",
				"EF 69 5A BA", "vpmin.s32	d21,d25,d26",
				"FF 49 5A BA", "vpmin.u8	d21,d25,d26",
				"FF 59 5A BA", "vpmin.u16	d21,d25,d26",
				"FF 69 5A BA", "vpmin.u32	d21,d25,d26",
				"FF 69 5F AA", "vpmin.f32	d21,d25,d26",
				"EC FD DA 02", "vpop	{s27-s28}",
				"EC FD BB 04", "vpop	{d27-d28}",
				"ED 6D DA 02", "vpush	{s27-s28}",
				"ED 6D BB 04", "vpush	{d27-d28}",
				"FF F0 57 2A", "vqabs.s8	d21,d26",
				"FF F4 57 2A", "vqabs.s16	d21,d26",
				"FF F8 57 2A", "vqabs.s32	d21,d26",
				"FF F0 67 6E", "vqabs.s8	q11,q15",
				"FF F4 67 6E", "vqabs.s16	q11,q15",
				"FF F8 67 6E", "vqabs.s32	q11,q15",
				"EF 49 50 BA", "vqadd.s8	d21,d25,d26",
				"EF 59 50 BA", "vqadd.s16	d21,d25,d26",
				"EF 69 50 BA", "vqadd.s32	d21,d25,d26",
				"EF 79 50 BA", "vqadd.s64	d21,d25,d26",
				"FF 49 50 BA", "vqadd.u8	d21,d25,d26",
				"FF 59 50 BA", "vqadd.u16	d21,d25,d26",
				"FF 69 50 BA", "vqadd.u32	d21,d25,d26",
				"FF 79 50 BA", "vqadd.u64	d21,d25,d26",
				"EF 4C 60 FE", "vqadd.s8	q11,q14,q15",
				"EF 5C 60 FE", "vqadd.s16	q11,q14,q15",
				"EF 6C 60 FE", "vqadd.s32	q11,q14,q15",
				"EF 7C 60 FE", "vqadd.s64	q11,q14,q15",
				"FF 4C 60 FE", "vqadd.u8	q11,q14,q15",
				"FF 5C 60 FE", "vqadd.u16	q11,q14,q15",
				"FF 6C 60 FE", "vqadd.u32	q11,q14,q15",
				"FF 7C 60 FE", "vqadd.u64	q11,q14,q15",
				"EF D9 63 CF", "vqdmlal.s16	q11,d25,d7[1]",
				"EF E9 63 EF", "vqdmlal.s32	q11,d25,d15[1]",
				"EF D9 69 AA", "vqdmlal.s16	q11,d25,d26",
				"EF E9 69 AA", "vqdmlal.s32	q11,d25,d26",
				"EF D9 67 CF", "vqdmlsl.s16	q11,d25,d7[1]",
				"EF E9 67 EF", "vqdmlsl.s32	q11,d25,d15[1]",
				"EF D9 6B AA", "vqdmlsl.s16	q11,d25,d26",
				"EF E9 6B AA", "vqdmlsl.s32	q11,d25,d26",
				"EF D9 5C CA", "vqdmulh.s16	d21,d25,d2[1]",
				"EF E9 5C EF", "vqdmulh.s32	d21,d25,d15[1]",
				"EF 59 5B AA", "vqdmulh.s16	d21,d25,d26",
				"EF 69 5B AA", "vqdmulh.s32	d21,d25,d26",
				"FF DC 6C CA", "vqdmulh.s16	q11,q14,d2[1]",
				"FF EC 6C EF", "vqdmulh.s32	q11,q14,d15[1]",
				"EF 5C 6B EE", "vqdmulh.s16	q11,q14,q15",
				"EF 6C 6B EE", "vqdmulh.s32	q11,q14,q15",
				"EF D9 6B CA", "vqdmull.s16	q11,d25,d2[1]",
				"EF E9 6B EF", "vqdmull.s32	q11,d25,d15[1]",
				"EF D9 6D AA", "vqdmull.s16	q11,d25,d26",
				"EF E9 6D AA", "vqdmull.s32	q11,d25,d26",
				"EF D9 6D AA", "vqdmull.s16	q11,d25,d26",
				"EF E9 6D AA", "vqdmull.s32	q11,d25,d26",
				"FF F2 52 AE", "vqmovn.s16	d21,q15",
				"FF F6 52 AE", "vqmovn.s32	d21,q15",
				"FF FA 52 AE", "vqmovn.s64	d21,q15",
				"FF F2 52 EE", "vqmovn.u16	d21,q15",
				"FF F6 52 EE", "vqmovn.u32	d21,q15",
				"FF FA 52 EE", "vqmovn.u64	d21,q15",
				"FF F2 52 6E", "vqmovun.s16	d21,q15",
				"FF F6 52 6E", "vqmovun.s32	d21,q15",
				"FF FA 52 6E", "vqmovun.s64	d21,q15",
				"FF F0 57 AA", "vqneg.s8	d21,d26",
				"FF F4 57 AA", "vqneg.s16	d21,d26",
				"FF F8 57 AA", "vqneg.s32	d21,d26",
				"FF F0 67 EE", "vqneg.s8	q11,q15",
				"FF F4 67 EE", "vqneg.s16	q11,q15",
				"FF F8 67 EE", "vqneg.s32	q11,q15",
				"EF D9 5D CF", "vqrdmulh.s16	d21,d25,d7[1]",
				"EF E9 5D EF", "vqrdmulh.s32	d21,d25,d15[1]",
				"FF 59 5B AA", "vqrdmulh.s16	d21,d25,d26",
				"FF 69 5B AA", "vqrdmulh.s32	d21,d25,d26",
				"FF DC 6D CF", "vqrdmulh.s16	q11,q14,d7[1]",
				"FF EC 6D EF", "vqrdmulh.s32	q11,q14,d15[1]",
				"FF 5C 6B EE", "vqrdmulh.s16	q11,q14,q15",
				"FF 6C 6B EE", "vqrdmulh.s32	q11,q14,q15",
				"EF 49 55 BA", "vqrshl.s8	d21,d26,d25",
				"EF 59 55 BA", "vqrshl.s16	d21,d26,d25",
				"EF 69 55 BA", "vqrshl.s32	d21,d26,d25",
				"EF 79 55 BA", "vqrshl.s64	d21,d26,d25",
				"FF 49 55 BA", "vqrshl.u8	d21,d26,d25",
				"FF 59 55 BA", "vqrshl.u16	d21,d26,d25",
				"FF 69 55 BA", "vqrshl.u32	d21,d26,d25",
				"FF 79 55 BA", "vqrshl.u64	d21,d26,d25",
				"EF 4C 65 FE", "vqrshl.s8	q11,q15,q14",
				"EF 5C 65 FE", "vqrshl.s16	q11,q15,q14",
				"EF 6C 65 FE", "vqrshl.s32	q11,q15,q14",
				"EF 7C 65 FE", "vqrshl.s64	q11,q15,q14",
				"FF 4C 65 FE", "vqrshl.u8	q11,q15,q14",
				"FF 5C 65 FE", "vqrshl.u16	q11,q15,q14",
				"FF 6C 65 FE", "vqrshl.u32	q11,q15,q14",
				"FF 7C 65 FE", "vqrshl.u64	q11,q15,q14",
				"EF CF 59 7E", "vqrshrn.s16	d21,q15,#1",
				"FF CF 59 7E", "vqrshrn.u16	d21,q15,#1",
				"EF CF 59 7E", "vqrshrn.s16	d21,q15,#1",
				"EF C8 59 7E", "vqrshrn.s16	d21,q15,#8",
				"FF CF 59 7E", "vqrshrn.u16	d21,q15,#1",
				"FF C8 59 7E", "vqrshrn.u16	d21,q15,#8",
				"EF DF 59 7E", "vqrshrn.s32	d21,q15,#1",
				"EF D0 59 7E", "vqrshrn.s32	d21,q15,#16",
				"FF DF 59 7E", "vqrshrn.u32	d21,q15,#1",
				"FF D0 59 7E", "vqrshrn.u32	d21,q15,#16",
				"EF FF 59 7E", "vqrshrn.s64	d21,q15,#1",
				"EF E0 59 7E", "vqrshrn.s64	d21,q15,#32",
				"FF FF 59 7E", "vqrshrn.u64	d21,q15,#1",
				"FF E0 59 7E", "vqrshrn.u64	d21,q15,#32",
				"FF CF 58 7E", "vqrshrun.s16	d21,q15,#1",
				"FF C8 58 7E", "vqrshrun.s16	d21,q15,#8",
				"FF DF 58 7E", "vqrshrun.s32	d21,q15,#1",
				"FF D0 58 7E", "vqrshrun.s32	d21,q15,#16",
				"FF FF 58 7E", "vqrshrun.s64	d21,q15,#1",
				"FF E0 58 7E", "vqrshrun.s64	d21,q15,#32",
				"EF C8 57 3A", "vqshl.s8	d21,d26,#0",
				"EF CF 57 3A", "vqshl.s8	d21,d26,#7",
				"FF C8 57 3A", "vqshl.u8	d21,d26,#0",
				"FF CF 57 3A", "vqshl.u8	d21,d26,#7",
				"EF D0 57 3A", "vqshl.s16	d21,d26,#0",
				"EF DF 57 3A", "vqshl.s16	d21,d26,#15",
				"FF D0 57 3A", "vqshl.u16	d21,d26,#0",
				"FF DF 57 3A", "vqshl.u16	d21,d26,#15",
				"EF E0 57 3A", "vqshl.s32	d21,d26,#0",
				"EF FF 57 3A", "vqshl.s32	d21,d26,#31",
				"FF E0 57 3A", "vqshl.u32	d21,d26,#0",
				"FF FF 57 3A", "vqshl.u32	d21,d26,#31",
				"EF C0 57 BA", "vqshl.s64	d21,d26,#0",
				"EF FF 57 BA", "vqshl.s64	d21,d26,#63",
				"FF C0 57 BA", "vqshl.u64	d21,d26,#0",
				"FF FF 57 BA", "vqshl.u64	d21,d26,#63",
				"EF 49 54 BA", "vqshl.s8	d21,d26,d25",
				"EF 59 54 BA", "vqshl.s16	d21,d26,d25",
				"EF 69 54 BA", "vqshl.s32	d21,d26,d25",
				"EF 79 54 BA", "vqshl.s64	d21,d26,d25",
				"FF 49 54 BA", "vqshl.u8	d21,d26,d25",
				"FF 59 54 BA", "vqshl.u16	d21,d26,d25",
				"FF 69 54 BA", "vqshl.u32	d21,d26,d25",
				"FF 79 54 BA", "vqshl.u64	d21,d26,d25",
				"EF C8 67 7E", "vqshl.s8	q11,q15,#0",
				"EF CF 67 7E", "vqshl.s8	q11,q15,#7",
				"FF C8 67 7E", "vqshl.u8	q11,q15,#0",
				"FF CF 67 7E", "vqshl.u8	q11,q15,#7",
				"EF D0 67 7E", "vqshl.s16	q11,q15,#0",
				"EF DF 67 7E", "vqshl.s16	q11,q15,#15",
				"FF D0 67 7E", "vqshl.u16	q11,q15,#0",
				"FF DF 67 7E", "vqshl.u16	q11,q15,#15",
				"EF E0 67 7E", "vqshl.s32	q11,q15,#0",
				"EF FF 67 7E", "vqshl.s32	q11,q15,#31",
				"FF E0 67 7E", "vqshl.u32	q11,q15,#0",
				"FF FF 67 7E", "vqshl.u32	q11,q15,#31",
				"EF C0 67 FE", "vqshl.s64	q11,q15,#0",
				"EF FF 67 FE", "vqshl.s64	q11,q15,#63",
				"FF C0 67 FE", "vqshl.u64	q11,q15,#0",
				"FF FF 67 FE", "vqshl.u64	q11,q15,#63",
				"EF 4C 64 FE", "vqshl.s8	q11,q15,q14",
				"EF 5C 64 FE", "vqshl.s16	q11,q15,q14",
				"EF 6C 64 FE", "vqshl.s32	q11,q15,q14",
				"EF 7C 64 FE", "vqshl.s64	q11,q15,q14",
				"FF 4C 64 FE", "vqshl.u8	q11,q15,q14",
				"FF 5C 64 FE", "vqshl.u16	q11,q15,q14",
				"FF 6C 64 FE", "vqshl.u32	q11,q15,q14",
				"FF 7C 64 FE", "vqshl.u64	q11,q15,q14",
				"FF C8 56 3A", "vqshlu.s8	d21,d26,#0",
				"FF CF 56 3A", "vqshlu.s8	d21,d26,#7",
				"FF D0 56 3A", "vqshlu.s16	d21,d26,#0",
				"FF DF 56 3A", "vqshlu.s16	d21,d26,#15",
				"FF E0 56 3A", "vqshlu.s32	d21,d26,#0",
				"FF FF 56 3A", "vqshlu.s32	d21,d26,#31",
				"FF C0 56 BA", "vqshlu.s64	d21,d26,#0",
				"FF FF 56 BA", "vqshlu.s64	d21,d26,#63",
				"FF C8 66 7E", "vqshlu.s8	q11,q15,#0",
				"FF CF 66 7E", "vqshlu.s8	q11,q15,#7",
				"FF D0 66 7E", "vqshlu.s16	q11,q15,#0",
				"FF DF 66 7E", "vqshlu.s16	q11,q15,#15",
				"FF E0 66 7E", "vqshlu.s32	q11,q15,#0",
				"FF FF 66 7E", "vqshlu.s32	q11,q15,#31",
				"FF C0 66 FE", "vqshlu.s64	q11,q15,#0",
				"FF FF 66 FE", "vqshlu.s64	q11,q15,#63",
				"EF CF 59 3E", "vqshrn.s16	d21,q15,#1",
				"EF C8 59 3E", "vqshrn.s16	d21,q15,#8",
				"FF CF 59 3E", "vqshrn.u16	d21,q15,#1",
				"FF C8 59 3E", "vqshrn.u16	d21,q15,#8",
				"EF DF 59 3E", "vqshrn.s32	d21,q15,#1",
				"EF D0 59 3E", "vqshrn.s32	d21,q15,#16",
				"FF DF 59 3E", "vqshrn.u32	d21,q15,#1",
				"FF D0 59 3E", "vqshrn.u32	d21,q15,#16",
				"EF FF 59 3E", "vqshrn.s64	d21,q15,#1",
				"EF E0 59 3E", "vqshrn.s64	d21,q15,#32",
				"FF FF 59 3E", "vqshrn.u64	d21,q15,#1",
				"FF E0 59 3E", "vqshrn.u64	d21,q15,#32",
				"FF CF 58 3E", "vqshrun.s16	d21,q15,#1",
				"FF C8 58 3E", "vqshrun.s16	d21,q15,#8",
				"FF DF 58 3E", "vqshrun.s32	d21,q15,#1",
				"FF D0 58 3E", "vqshrun.s32	d21,q15,#16",
				"FF FF 58 3E", "vqshrun.s64	d21,q15,#1",
				"FF E0 58 3E", "vqshrun.s64	d21,q15,#32",
				"EF 49 52 BA", "vqsub.s8	d21,d25,d26",
				"EF 59 52 BA", "vqsub.s16	d21,d25,d26",
				"EF 69 52 BA", "vqsub.s32	d21,d25,d26",
				"EF 79 52 BA", "vqsub.s64	d21,d25,d26",
				"FF 49 52 BA", "vqsub.u8	d21,d25,d26",
				"FF 59 52 BA", "vqsub.u16	d21,d25,d26",
				"FF 69 52 BA", "vqsub.u32	d21,d25,d26",
				"FF 79 52 BA", "vqsub.u64	d21,d25,d26",
				"EF 4C 62 FE", "vqsub.s8	q11,q14,q15",
				"EF 5C 62 FE", "vqsub.s16	q11,q14,q15",
				"EF 6C 62 FE", "vqsub.s32	q11,q14,q15",
				"EF 7C 62 FE", "vqsub.s64	q11,q14,q15",
				"FF 4C 62 FE", "vqsub.u8	q11,q14,q15",
				"FF 5C 62 FE", "vqsub.u16	q11,q14,q15",
				"FF 6C 62 FE", "vqsub.u32	q11,q14,q15",
				"FF 7C 62 FE", "vqsub.u64	q11,q14,q15",
				"FF CC 54 AE", "vraddhn.i16	d21,q14,q15",
				"FF DC 54 AE", "vraddhn.i32	d21,q14,q15",
				"FF EC 54 AE", "vraddhn.i64	d21,q14,q15",
				"FF FB 54 2A", "vrecpe.u32	d21,d26",
				"FF FB 55 2A", "vrecpe.f32	d21,d26",
				"FF FB 64 6E", "vrecpe.u32	q11,q15",
				"FF FB 65 6E", "vrecpe.f32	q11,q15",
				"EF 49 5F BA", "vrecps.f32	d21,d25,d26",
				"EF 4C 6F FE", "vrecps.f32	q11,q14,q15",
				"FF F0 51 2A", "vrev16.8	d21,d26",
				"FF F0 61 6E", "vrev16.8	q11,q15",
				"FF F0 50 AA", "vrev32.8	d21,d26",
				"FF F4 50 AA", "vrev32.16	d21,d26",
				"FF F0 60 EE", "vrev32.8	q11,q15",
				"FF F4 60 EE", "vrev32.16	q11,q15",
				"FF F0 50 2A", "vrev64.8	d21,d26",
				"FF F4 50 2A", "vrev64.16	d21,d26",
				"FF F8 50 2A", "vrev64.32	d21,d26",
				"FF F0 60 6E", "vrev64.8	q11,q15",
				"FF F4 60 6E", "vrev64.16	q11,q15",
				"FF F8 60 6E", "vrev64.32	q11,q15",
				"EF 49 51 AA", "vrhadd.s8	d21,d25,d26",
				"EF 59 51 AA", "vrhadd.s16	d21,d25,d26",
				"EF 69 51 AA", "vrhadd.s32	d21,d25,d26",
				"FF 49 51 AA", "vrhadd.u8	d21,d25,d26",
				"FF 59 51 AA", "vrhadd.u16	d21,d25,d26",
				"FF 69 51 AA", "vrhadd.u32	d21,d25,d26",
				"EF 4C 61 EE", "vrhadd.s8	q11,q14,q15",
				"EF 5C 61 EE", "vrhadd.s16	q11,q14,q15",
				"EF 6C 61 EE", "vrhadd.s32	q11,q14,q15",
				"FF 4C 61 EE", "vrhadd.u8	q11,q14,q15",
				"FF 5C 61 EE", "vrhadd.u16	q11,q14,q15",
				"FF 6C 61 EE", "vrhadd.u32	q11,q14,q15",
				"EF 49 55 AA", "vrshl.s8	d21,d26,d25",
				"EF 59 55 AA", "vrshl.s16	d21,d26,d25",
				"EF 69 55 AA", "vrshl.s32	d21,d26,d25",
				"EF 79 55 AA", "vrshl.s64	d21,d26,d25",
				"FF 49 55 AA", "vrshl.u8	d21,d26,d25",
				"FF 59 55 AA", "vrshl.u16	d21,d26,d25",
				"FF 69 55 AA", "vrshl.u32	d21,d26,d25",
				"FF 79 55 AA", "vrshl.u64	d21,d26,d25",
				"EF 4C 65 EE", "vrshl.s8	q11,q15,q14",
				"EF 5C 65 EE", "vrshl.s16	q11,q15,q14",
				"EF 6C 65 EE", "vrshl.s32	q11,q15,q14",
				"EF 7C 65 EE", "vrshl.s64	q11,q15,q14",
				"FF 4C 65 EE", "vrshl.u8	q11,q15,q14",
				"FF 5C 65 EE", "vrshl.u16	q11,q15,q14",
				"FF 6C 65 EE", "vrshl.u32	q11,q15,q14",
				"FF 7C 65 EE", "vrshl.u64	q11,q15,q14",
				"EF CF 52 3A", "vrshr.s8	d21,d26,#1",
				"EF C8 52 3A", "vrshr.s8	d21,d26,#8",
				"FF CF 52 3A", "vrshr.u8	d21,d26,#1",
				"FF C8 52 3A", "vrshr.u8	d21,d26,#8",
				"EF DF 52 3A", "vrshr.s16	d21,d26,#1",
				"EF D0 52 3A", "vrshr.s16	d21,d26,#16",
				"FF DF 52 3A", "vrshr.u16	d21,d26,#1",
				"FF D0 52 3A", "vrshr.u16	d21,d26,#16",
				"EF FF 52 3A", "vrshr.s32	d21,d26,#1",
				"EF E0 52 3A", "vrshr.s32	d21,d26,#32",
				"FF FF 52 3A", "vrshr.u32	d21,d26,#1",
				"FF E0 52 3A", "vrshr.u32	d21,d26,#32",
				"EF FF 52 BA", "vrshr.s64	d21,d26,#1",
				"EF C0 52 BA", "vrshr.s64	d21,d26,#64",
				"FF FF 52 BA", "vrshr.u64	d21,d26,#1",
				"FF C0 52 BA", "vrshr.u64	d21,d26,#64",
				"EF CF 62 7E", "vrshr.s8	q11,q15,#1",
				"EF C8 62 7E", "vrshr.s8	q11,q15,#8",
				"FF CF 62 7E", "vrshr.u8	q11,q15,#1",
				"FF C8 62 7E", "vrshr.u8	q11,q15,#8",
				"EF DF 62 7E", "vrshr.s16	q11,q15,#1",
				"EF D0 62 7E", "vrshr.s16	q11,q15,#16",
				"FF DF 62 7E", "vrshr.u16	q11,q15,#1",
				"FF D0 62 7E", "vrshr.u16	q11,q15,#16",
				"EF FF 62 7E", "vrshr.s32	q11,q15,#1",
				"EF E0 62 7E", "vrshr.s32	q11,q15,#32",
				"FF FF 62 7E", "vrshr.u32	q11,q15,#1",
				"FF E0 62 7E", "vrshr.u32	q11,q15,#32",
				"EF FF 62 FE", "vrshr.s64	q11,q15,#1",
				"EF C0 62 FE", "vrshr.s64	q11,q15,#64",
				"FF FF 62 FE", "vrshr.u64	q11,q15,#1",
				"FF C0 62 FE", "vrshr.u64	q11,q15,#64",
				"EF CF 58 7E", "vrshrn.i16	d21,q15,#1",
				"EF C8 58 7E", "vrshrn.i16	d21,q15,#8",
				"EF DF 58 7E", "vrshrn.i32	d21,q15,#1",
				"EF D0 58 7E", "vrshrn.i32	d21,q15,#16",
				"EF FF 58 7E", "vrshrn.i64	d21,q15,#1",
				"EF E0 58 7E", "vrshrn.i64	d21,q15,#32",
				"FF FB 54 AA", "vrsqrte.u32	d21,d26",
				"FF FB 55 AA", "vrsqrte.f32	d21,d26",
				"FF FB 64 EE", "vrsqrte.u32	q11,q15",
				"FF FB 65 EE", "vrsqrte.f32	q11,q15",
				"EF 69 5F BA", "vrsqrts.f32	d21,d25,d26",
				"EF 6C 6F FE", "vrsqrts.f32	q11,q14,q15",
				"EF CF 53 3A", "vrsra.s8	d21,d26,#1",
				"EF C8 53 3A", "vrsra.s8	d21,d26,#8",
				"FF CF 53 3A", "vrsra.u8	d21,d26,#1",
				"FF C8 53 3A", "vrsra.u8	d21,d26,#8",
				"EF DF 53 3A", "vrsra.s16	d21,d26,#1",
				"EF D0 53 3A", "vrsra.s16	d21,d26,#16",
				"FF DF 53 3A", "vrsra.u16	d21,d26,#1",
				"FF D0 53 3A", "vrsra.u16	d21,d26,#16",
				"EF FF 53 3A", "vrsra.s32	d21,d26,#1",
				"EF E0 53 3A", "vrsra.s32	d21,d26,#32",
				"FF FF 53 3A", "vrsra.u32	d21,d26,#1",
				"FF E0 53 3A", "vrsra.u32	d21,d26,#32",
				"EF FF 53 BA", "vrsra.s64	d21,d26,#1",
				"EF C0 53 BA", "vrsra.s64	d21,d26,#64",
				"FF FF 53 BA", "vrsra.u64	d21,d26,#1",
				"FF C0 53 BA", "vrsra.u64	d21,d26,#64",
				"EF CF 63 7E", "vrsra.s8	q11,q15,#1",
				"EF C8 63 7E", "vrsra.s8	q11,q15,#8",
				"FF CF 63 7E", "vrsra.u8	q11,q15,#1",
				"FF C8 63 7E", "vrsra.u8	q11,q15,#8",
				"EF DF 63 7E", "vrsra.s16	q11,q15,#1",
				"EF D0 63 7E", "vrsra.s16	q11,q15,#16",
				"FF DF 63 7E", "vrsra.u16	q11,q15,#1",
				"FF D0 63 7E", "vrsra.u16	q11,q15,#16",
				"EF FF 63 7E", "vrsra.s32	q11,q15,#1",
				"EF E0 63 7E", "vrsra.s32	q11,q15,#32",
				"FF FF 63 7E", "vrsra.u32	q11,q15,#1",
				"FF E0 63 7E", "vrsra.u32	q11,q15,#32",
				"EF FF 63 FE", "vrsra.s64	q11,q15,#1",
				"EF C0 63 FE", "vrsra.s64	q11,q15,#64",
				"FF FF 63 FE", "vrsra.u64	q11,q15,#1",
				"FF C0 63 FE", "vrsra.u64	q11,q15,#64",
				"FF CC 56 AE", "vrsubhn.i16	d21,q14,q15",
				"FF DC 56 AE", "vrsubhn.i32	d21,q14,q15",
				"FF EC 56 AE", "vrsubhn.i64	d21,q14,q15",
				"EF C8 55 3A", "vshl.i8	d21,d26,#0",
				"EF CF 55 3A", "vshl.i8	d21,d26,#7",
				"EF D0 55 3A", "vshl.i16	d21,d26,#0",
				"EF DF 55 3A", "vshl.i16	d21,d26,#15",
				"EF E0 55 3A", "vshl.i32	d21,d26,#0",
				"EF FF 55 3A", "vshl.i32	d21,d26,#31",
				"EF C0 55 BA", "vshl.i64	d21,d26,#0",
				"EF FF 55 BA", "vshl.i64	d21,d26,#63",
				"EF C8 65 7E", "vshl.i8	q11,q15,#0",
				"EF CF 65 7E", "vshl.i8	q11,q15,#7",
				"EF D0 65 7E", "vshl.i16	q11,q15,#0",
				"EF DF 65 7E", "vshl.i16	q11,q15,#15",
				"EF E0 65 7E", "vshl.i32	q11,q15,#0",
				"EF FF 65 7E", "vshl.i32	q11,q15,#31",
				"EF C0 65 FE", "vshl.i64	q11,q15,#0",
				"EF FF 65 FE", "vshl.i64	q11,q15,#63",
				"EF 49 54 AA", "vshl.s8	d21,d26,d25",
				"EF 59 54 AA", "vshl.s16	d21,d26,d25",
				"EF 69 54 AA", "vshl.s32	d21,d26,d25",
				"EF 79 54 AA", "vshl.s64	d21,d26,d25",
				"FF 49 54 AA", "vshl.u8	d21,d26,d25",
				"FF 59 54 AA", "vshl.u16	d21,d26,d25",
				"FF 69 54 AA", "vshl.u32	d21,d26,d25",
				"FF 79 54 AA", "vshl.u64	d21,d26,d25",
				"EF 4C 64 EE", "vshl.s8	q11,q15,q14",
				"EF 5C 64 EE", "vshl.s16	q11,q15,q14",
				"EF 6C 64 EE", "vshl.s32	q11,q15,q14",
				"EF 7C 64 EE", "vshl.s64	q11,q15,q14",
				"FF 4C 64 EE", "vshl.u8	q11,q15,q14",
				"FF 5C 64 EE", "vshl.u16	q11,q15,q14",
				"FF 6C 64 EE", "vshl.u32	q11,q15,q14",
				"FF 7C 64 EE", "vshl.u64	q11,q15,q14",
				"EF C9 6A 3A", "vshll.s8	q11,d26,#1",
				"EF CF 6A 3A", "vshll.s8	q11,d26,#7",
				"FF C9 6A 3A", "vshll.u8	q11,d26,#1",
				"FF CF 6A 3A", "vshll.u8	q11,d26,#7",
				"EF D1 6A 3A", "vshll.s16	q11,d26,#1",
				"EF DF 6A 3A", "vshll.s16	q11,d26,#15",
				"FF D1 6A 3A", "vshll.u16	q11,d26,#1",
				"FF DF 6A 3A", "vshll.u16	q11,d26,#15",
				"EF E1 6A 3A", "vshll.s32	q11,d26,#1",
				"EF FF 6A 3A", "vshll.s32	q11,d26,#31",
				"FF E1 6A 3A", "vshll.u32	q11,d26,#1",
				"FF FF 6A 3A", "vshll.u32	q11,d26,#31",
				"FF F2 63 2A", "vshll.i8	q11,d26,#8",
				"FF F6 63 2A", "vshll.i16	q11,d26,#16",
				"FF FA 63 2A", "vshll.i32	q11,d26,#32",
				"EF CF 50 3A", "vshr.s8	d21,d26,#1",
				"EF C8 50 3A", "vshr.s8	d21,d26,#8",
				"FF CF 50 3A", "vshr.u8	d21,d26,#1",
				"FF C8 50 3A", "vshr.u8	d21,d26,#8",
				"EF DF 50 3A", "vshr.s16	d21,d26,#1",
				"EF D0 50 3A", "vshr.s16	d21,d26,#16",
				"FF DF 50 3A", "vshr.u16	d21,d26,#1",
				"FF D0 50 3A", "vshr.u16	d21,d26,#16",
				"EF FF 50 3A", "vshr.s32	d21,d26,#1",
				"EF E0 50 3A", "vshr.s32	d21,d26,#32",
				"FF FF 50 3A", "vshr.u32	d21,d26,#1",
				"FF E0 50 3A", "vshr.u32	d21,d26,#32",
				"EF FF 50 BA", "vshr.s64	d21,d26,#1",
				"EF C0 50 BA", "vshr.s64	d21,d26,#64",
				"FF FF 50 BA", "vshr.u64	d21,d26,#1",
				"FF C0 50 BA", "vshr.u64	d21,d26,#64",
				"EF CF 60 7E", "vshr.s8	q11,q15,#1",
				"EF C8 60 7E", "vshr.s8	q11,q15,#8",
				"FF CF 60 7E", "vshr.u8	q11,q15,#1",
				"FF C8 60 7E", "vshr.u8	q11,q15,#8",
				"EF DF 60 7E", "vshr.s16	q11,q15,#1",
				"EF D0 60 7E", "vshr.s16	q11,q15,#16",
				"FF DF 60 7E", "vshr.u16	q11,q15,#1",
				"FF D0 60 7E", "vshr.u16	q11,q15,#16",
				"EF FF 60 7E", "vshr.s32	q11,q15,#1",
				"EF E0 60 7E", "vshr.s32	q11,q15,#32",
				"FF FF 60 7E", "vshr.u32	q11,q15,#1",
				"FF E0 60 7E", "vshr.u32	q11,q15,#32",
				"EF FF 60 FE", "vshr.s64	q11,q15,#1",
				"EF C0 60 FE", "vshr.s64	q11,q15,#64",
				"FF FF 60 FE", "vshr.u64	q11,q15,#1",
				"FF C0 60 FE", "vshr.u64	q11,q15,#64",
				"EF CF 58 3E", "vshrn.i16	d21,q15,#1",
				"EF C8 58 3E", "vshrn.i16	d21,q15,#8",
				"EF DF 58 3E", "vshrn.i32	d21,q15,#1",
				"EF D0 58 3E", "vshrn.i32	d21,q15,#16",
				"EF FF 58 3E", "vshrn.i64	d21,q15,#1",
				"EF E0 58 3E", "vshrn.i64	d21,q15,#32",
				"FF C8 55 3A", "vsli.8	d21,d26,#0",
				"FF CF 55 3A", "vsli.8	d21,d26,#7",
				"FF D0 55 3A", "vsli.16	d21,d26,#0",
				"FF DF 55 3A", "vsli.16	d21,d26,#15",
				"FF E0 55 3A", "vsli.32	d21,d26,#0",
				"FF FF 55 3A", "vsli.32	d21,d26,#31",
				"FF C0 55 BA", "vsli.64	d21,d26,#0",
				"FF FF 55 BA", "vsli.64	d21,d26,#63",
				"FF C8 65 7E", "vsli.8	q11,q15,#0",
				"FF CF 65 7E", "vsli.8	q11,q15,#7",
				"FF D0 65 7E", "vsli.16	q11,q15,#0",
				"FF DF 65 7E", "vsli.16	q11,q15,#15",
				"FF E0 65 7E", "vsli.32	q11,q15,#0",
				"FF FF 65 7E", "vsli.32	q11,q15,#31",
				"FF C0 65 FE", "vsli.64	q11,q15,#0",
				"FF FF 65 FE", "vsli.64	q11,q15,#63",
				"EE F1 AA CD", "vsqrt.f32	s21,s26",
				"EE F1 5B EA", "vsqrt.f64	d21,d26",
				"EF CF 51 3A", "vsra.s8	d21,d26,#1",
				"EF C8 51 3A", "vsra.s8	d21,d26,#8",
				"FF CF 51 3A", "vsra.u8	d21,d26,#1",
				"FF C8 51 3A", "vsra.u8	d21,d26,#8",
				"EF DF 51 3A", "vsra.s16	d21,d26,#1",
				"EF D0 51 3A", "vsra.s16	d21,d26,#16",
				"FF DF 51 3A", "vsra.u16	d21,d26,#1",
				"FF D0 51 3A", "vsra.u16	d21,d26,#16",
				"EF FF 51 3A", "vsra.s32	d21,d26,#1",
				"EF E0 51 3A", "vsra.s32	d21,d26,#32",
				"FF FF 51 3A", "vsra.u32	d21,d26,#1",
				"FF E0 51 3A", "vsra.u32	d21,d26,#32",
				"EF FF 51 BA", "vsra.s64	d21,d26,#1",
				"EF C0 51 BA", "vsra.s64	d21,d26,#64",
				"FF FF 51 BA", "vsra.u64	d21,d26,#1",
				"FF C0 51 BA", "vsra.u64	d21,d26,#64",
				"EF CF 61 7E", "vsra.s8	q11,q15,#1",
				"EF C8 61 7E", "vsra.s8	q11,q15,#8",
				"FF CF 61 7E", "vsra.u8	q11,q15,#1",
				"FF C8 61 7E", "vsra.u8	q11,q15,#8",
				"EF DF 61 7E", "vsra.s16	q11,q15,#1",
				"EF D0 61 7E", "vsra.s16	q11,q15,#16",
				"FF DF 61 7E", "vsra.u16	q11,q15,#1",
				"FF D0 61 7E", "vsra.u16	q11,q15,#16",
				"EF FF 61 7E", "vsra.s32	q11,q15,#1",
				"EF E0 61 7E", "vsra.s32	q11,q15,#32",
				"FF FF 61 7E", "vsra.u32	q11,q15,#1",
				"FF E0 61 7E", "vsra.u32	q11,q15,#32",
				"EF FF 61 FE", "vsra.s64	q11,q15,#1",
				"EF C0 61 FE", "vsra.s64	q11,q15,#64",
				"FF FF 61 FE", "vsra.u64	q11,q15,#1",
				"FF C0 61 FE", "vsra.u64	q11,q15,#64",
				"FF CF 54 3A", "vsri.8	d21,d26,#1",
				"FF C8 54 3A", "vsri.8	d21,d26,#8",
				"FF DF 54 3A", "vsri.16	d21,d26,#1",
				"FF D0 54 3A", "vsri.16	d21,d26,#16",
				"FF FF 54 3A", "vsri.32	d21,d26,#1",
				"FF E0 54 3A", "vsri.32	d21,d26,#32",
				"FF FF 54 BA", "vsri.64	d21,d26,#1",
				"FF C0 54 BA", "vsri.64	d21,d26,#64",
				"FF CF 64 7E", "vsri.8	q11,q15,#1",
				"FF C8 64 7E", "vsri.8	q11,q15,#8",
				"FF DF 64 7E", "vsri.16	q11,q15,#1",
				"FF D0 64 7E", "vsri.16	q11,q15,#16",
				"FF FF 64 7E", "vsri.32	q11,q15,#1",
				"FF E0 64 7E", "vsri.32	q11,q15,#32",
				"FF FF 64 FE", "vsri.64	q11,q15,#1",
				"FF C0 64 FE", "vsri.64	q11,q15,#64",
				"F9 4A B7 0F", "vst1.8	{d27},[r10]",
				"F9 4A BA 0F", "vst1.8	{d27,d28},[r10]",
				"F9 4A B6 0F", "vst1.8	{d27,d28,d29},[r10]",
				"F9 4A B2 0F", "vst1.8	{d27,d28,d29,d30},[r10]",
				"F9 4A B7 4F", "vst1.16	{d27},[r10]",
				"F9 4A BA 4F", "vst1.16	{d27,d28},[r10]",
				"F9 4A B6 4F", "vst1.16	{d27,d28,d29},[r10]",
				"F9 4A B2 4F", "vst1.16	{d27,d28,d29,d30},[r10]",
				"F9 4A B7 8F", "vst1.32	{d27},[r10]",
				"F9 4A BA 8F", "vst1.32	{d27,d28},[r10]",
				"F9 4A B6 8F", "vst1.32	{d27,d28,d29},[r10]",
				"F9 4A B2 8F", "vst1.32	{d27,d28,d29,d30},[r10]",
				"F9 4A B7 CF", "vst1.64	{d27},[r10]",
				"F9 4A BA CF", "vst1.64	{d27,d28},[r10]",
				"F9 4A B6 CF", "vst1.64	{d27,d28,d29},[r10]",
				"F9 4A B2 CF", "vst1.64	{d27,d28,d29,d30},[r10]",
				"F9 4A B7 1F", "vst1.8	{d27},[r10@64]",
				"F9 4A BA 1F", "vst1.8	{d27,d28},[r10@64]",
				"F9 4A BA 2F", "vst1.8	{d27,d28},[r10@128]",
				"F9 4A B6 1F", "vst1.8	{d27,d28,d29},[r10@64]",
				"F9 4A B2 1F", "vst1.8	{d27,d28,d29,d30},[r10@64]",
				"F9 4A B2 2F", "vst1.8	{d27,d28,d29,d30},[r10@128]",
				"F9 4A B2 3F", "vst1.8	{d27,d28,d29,d30},[r10@256]",
				"F9 4A B7 5F", "vst1.16	{d27},[r10@64]",
				"F9 4A BA 5F", "vst1.16	{d27,d28},[r10@64]",
				"F9 4A BA 6F", "vst1.16	{d27,d28},[r10@128]",
				"F9 4A B6 5F", "vst1.16	{d27,d28,d29},[r10@64]",
				"F9 4A B2 5F", "vst1.16	{d27,d28,d29,d30},[r10@64]",
				"F9 4A B2 6F", "vst1.16	{d27,d28,d29,d30},[r10@128]",
				"F9 4A B2 7F", "vst1.16	{d27,d28,d29,d30},[r10@256]",
				"F9 4A B7 9F", "vst1.32	{d27},[r10@64]",
				"F9 4A BA 9F", "vst1.32	{d27,d28},[r10@64]",
				"F9 4A BA AF", "vst1.32	{d27,d28},[r10@128]",
				"F9 4A B6 9F", "vst1.32	{d27,d28,d29},[r10@64]",
				"F9 4A B2 9F", "vst1.32	{d27,d28,d29,d30},[r10@64]",
				"F9 4A B2 AF", "vst1.32	{d27,d28,d29,d30},[r10@128]",
				"F9 4A B2 BF", "vst1.32	{d27,d28,d29,d30},[r10@256]",
				"F9 4A B7 DF", "vst1.64	{d27},[r10@64]",
				"F9 4A BA DF", "vst1.64	{d27,d28},[r10@64]",
				"F9 4A BA EF", "vst1.64	{d27,d28},[r10@128]",
				"F9 4A B6 DF", "vst1.64	{d27,d28,d29},[r10@64]",
				"F9 4A B2 DF", "vst1.64	{d27,d28,d29,d30},[r10@64]",
				"F9 4A B2 EF", "vst1.64	{d27,d28,d29,d30},[r10@128]",
				"F9 4A B2 FF", "vst1.64	{d27,d28,d29,d30},[r10@256]",
				"F9 4A B7 0D", "vst1.8	{d27},[r10]!",
				"F9 4A BA 0D", "vst1.8	{d27,d28},[r10]!",
				"F9 4A B6 0D", "vst1.8	{d27,d28,d29},[r10]!",
				"F9 4A B2 0D", "vst1.8	{d27,d28,d29,d30},[r10]!",
				"F9 4A B7 4D", "vst1.16	{d27},[r10]!",
				"F9 4A BA 4D", "vst1.16	{d27,d28},[r10]!",
				"F9 4A B6 4D", "vst1.16	{d27,d28,d29},[r10]!",
				"F9 4A B2 4D", "vst1.16	{d27,d28,d29,d30},[r10]!",
				"F9 4A B7 8D", "vst1.32	{d27},[r10]!",
				"F9 4A BA 8D", "vst1.32	{d27,d28},[r10]!",
				"F9 4A B6 8D", "vst1.32	{d27,d28,d29},[r10]!",
				"F9 4A B2 8D", "vst1.32	{d27,d28,d29,d30},[r10]!",
				"F9 4A B7 CD", "vst1.64	{d27},[r10]!",
				"F9 4A BA CD", "vst1.64	{d27,d28},[r10]!",
				"F9 4A B6 CD", "vst1.64	{d27,d28,d29},[r10]!",
				"F9 4A B2 CD", "vst1.64	{d27,d28,d29,d30},[r10]!",
				"F9 4A B7 1D", "vst1.8	{d27},[r10@64]!",
				"F9 4A BA 1D", "vst1.8	{d27,d28},[r10@64]!",
				"F9 4A BA 2D", "vst1.8	{d27,d28},[r10@128]!",
				"F9 4A B6 1D", "vst1.8	{d27,d28,d29},[r10@64]!",
				"F9 4A B2 1D", "vst1.8	{d27,d28,d29,d30},[r10@64]!",
				"F9 4A B2 2D", "vst1.8	{d27,d28,d29,d30},[r10@128]!",
				"F9 4A B2 3D", "vst1.8	{d27,d28,d29,d30},[r10@256]!",
				"F9 4A B7 5D", "vst1.16	{d27},[r10@64]!",
				"F9 4A BA 5D", "vst1.16	{d27,d28},[r10@64]!",
				"F9 4A BA 6D", "vst1.16	{d27,d28},[r10@128]!",
				"F9 4A B6 5D", "vst1.16	{d27,d28,d29},[r10@64]!",
				"F9 4A B2 5D", "vst1.16	{d27,d28,d29,d30},[r10@64]!",
				"F9 4A B2 6D", "vst1.16	{d27,d28,d29,d30},[r10@128]!",
				"F9 4A B2 7D", "vst1.16	{d27,d28,d29,d30},[r10@256]!",
				"F9 4A B7 9D", "vst1.32	{d27},[r10@64]!",
				"F9 4A BA 9D", "vst1.32	{d27,d28},[r10@64]!",
				"F9 4A BA AD", "vst1.32	{d27,d28},[r10@128]!",
				"F9 4A B6 9D", "vst1.32	{d27,d28,d29},[r10@64]!",
				"F9 4A B2 9D", "vst1.32	{d27,d28,d29,d30},[r10@64]!",
				"F9 4A B2 AD", "vst1.32	{d27,d28,d29,d30},[r10@128]!",
				"F9 4A B2 BD", "vst1.32	{d27,d28,d29,d30},[r10@256]!",
				"F9 4A B7 DD", "vst1.64	{d27},[r10@64]!",
				"F9 4A BA DD", "vst1.64	{d27,d28},[r10@64]!",
				"F9 4A BA ED", "vst1.64	{d27,d28},[r10@128]!",
				"F9 4A B6 DD", "vst1.64	{d27,d28,d29},[r10@64]!",
				"F9 4A B2 DD", "vst1.64	{d27,d28,d29,d30},[r10@64]!",
				"F9 4A B2 ED", "vst1.64	{d27,d28,d29,d30},[r10@128]!",
				"F9 4A B2 FD", "vst1.64	{d27,d28,d29,d30},[r10@256]!",
				"F9 4A B7 09", "vst1.8	{d27},[r10],r9",
				"F9 4A BA 09", "vst1.8	{d27,d28},[r10],r9",
				"F9 4A B6 09", "vst1.8	{d27,d28,d29},[r10],r9",
				"F9 4A B2 09", "vst1.8	{d27,d28,d29,d30},[r10],r9",
				"F9 4A B7 49", "vst1.16	{d27},[r10],r9",
				"F9 4A BA 49", "vst1.16	{d27,d28},[r10],r9",
				"F9 4A B6 49", "vst1.16	{d27,d28,d29},[r10],r9",
				"F9 4A B2 49", "vst1.16	{d27,d28,d29,d30},[r10],r9",
				"F9 4A B7 89", "vst1.32	{d27},[r10],r9",
				"F9 4A BA 89", "vst1.32	{d27,d28},[r10],r9",
				"F9 4A B6 89", "vst1.32	{d27,d28,d29},[r10],r9",
				"F9 4A B2 89", "vst1.32	{d27,d28,d29,d30},[r10],r9",
				"F9 4A B7 C9", "vst1.64	{d27},[r10],r9",
				"F9 4A BA C9", "vst1.64	{d27,d28},[r10],r9",
				"F9 4A B6 C9", "vst1.64	{d27,d28,d29},[r10],r9",
				"F9 4A B2 C9", "vst1.64	{d27,d28,d29,d30},[r10],r9",
				"F9 4A B7 19", "vst1.8	{d27},[r10@64],r9",
				"F9 4A BA 19", "vst1.8	{d27,d28},[r10@64],r9",
				"F9 4A BA 29", "vst1.8	{d27,d28},[r10@128],r9",
				"F9 4A B6 19", "vst1.8	{d27,d28,d29},[r10@64],r9",
				"F9 4A B2 19", "vst1.8	{d27,d28,d29,d30},[r10@64],r9",
				"F9 4A B2 29", "vst1.8	{d27,d28,d29,d30},[r10@128],r9",
				"F9 4A B2 39", "vst1.8	{d27,d28,d29,d30},[r10@256],r9",
				"F9 4A B7 59", "vst1.16	{d27},[r10@64],r9",
				"F9 4A BA 59", "vst1.16	{d27,d28},[r10@64],r9",
				"F9 4A BA 69", "vst1.16	{d27,d28},[r10@128],r9",
				"F9 4A B6 59", "vst1.16	{d27,d28,d29},[r10@64],r9",
				"F9 4A B2 59", "vst1.16	{d27,d28,d29,d30},[r10@64],r9",
				"F9 4A B2 69", "vst1.16	{d27,d28,d29,d30},[r10@128],r9",
				"F9 4A B2 79", "vst1.16	{d27,d28,d29,d30},[r10@256],r9",
				"F9 4A B7 99", "vst1.32	{d27},[r10@64],r9",
				"F9 4A BA 99", "vst1.32	{d27,d28},[r10@64],r9",
				"F9 4A BA A9", "vst1.32	{d27,d28},[r10@128],r9",
				"F9 4A B6 99", "vst1.32	{d27,d28,d29},[r10@64],r9",
				"F9 4A B2 99", "vst1.32	{d27,d28,d29,d30},[r10@64],r9",
				"F9 4A B2 A9", "vst1.32	{d27,d28,d29,d30},[r10@128],r9",
				"F9 4A B2 B9", "vst1.32	{d27,d28,d29,d30},[r10@256],r9",
				"F9 4A B7 D9", "vst1.64	{d27},[r10@64],r9",
				"F9 4A BA D9", "vst1.64	{d27,d28},[r10@64],r9",
				"F9 4A BA E9", "vst1.64	{d27,d28},[r10@128],r9",
				"F9 4A B6 D9", "vst1.64	{d27,d28,d29},[r10@64],r9",
				"F9 4A B2 D9", "vst1.64	{d27,d28,d29,d30},[r10@64],r9",
				"F9 4A B2 E9", "vst1.64	{d27,d28,d29,d30},[r10@128],r9",
				"F9 4A B2 F9", "vst1.64	{d27,d28,d29,d30},[r10@256],r9",
				"F9 CA B0 2F", "vst1.8	{d27[1]},[r10]",
				"F9 CA B4 4F", "vst1.16	{d27[1]},[r10]",
				"F9 CA B8 8F", "vst1.32	{d27[1]},[r10]",
				"F9 CA B4 5F", "vst1.16	{d27[1]},[r10@16]",
				"F9 CA B8 BF", "vst1.32	{d27[1]},[r10@32]",
				"F9 CA B0 2D", "vst1.8	{d27[1]},[r10]!",
				"F9 CA B4 4D", "vst1.16	{d27[1]},[r10]!",
				"F9 CA B8 8D", "vst1.32	{d27[1]},[r10]!",
				"F9 CA B4 5D", "vst1.16	{d27[1]},[r10@16]!",
				"F9 CA B8 BD", "vst1.32	{d27[1]},[r10@32]!",
				"F9 CA B0 29", "vst1.8	{d27[1]},[r10],r9",
				"F9 CA B4 49", "vst1.16	{d27[1]},[r10],r9",
				"F9 CA B8 89", "vst1.32	{d27[1]},[r10],r9",
				"F9 CA B4 59", "vst1.16	{d27[1]},[r10@16],r9",
				"F9 CA B8 B9", "vst1.32	{d27[1]},[r10@32],r9",
				"F9 4A B8 0F", "vst2.8	{d27,d28},[r10]",
				"F9 4A B9 0F", "vst2.8	{d27,d29},[r10]",
				"F9 4A B3 0F", "vst2.8	{d27,d28,d29,d30},[r10]",
				"F9 4A B8 4F", "vst2.16	{d27,d28},[r10]",
				"F9 4A B9 4F", "vst2.16	{d27,d29},[r10]",
				"F9 4A B3 4F", "vst2.16	{d27,d28,d29,d30},[r10]",
				"F9 4A B8 1F", "vst2.8	{d27,d28},[r10@64]",
				"F9 4A B8 2F", "vst2.8	{d27,d28},[r10@128]",
				"F9 4A B9 1F", "vst2.8	{d27,d29},[r10@64]",
				"F9 4A B9 2F", "vst2.8	{d27,d29},[r10@128]",
				"F9 4A B3 1F", "vst2.8	{d27,d28,d29,d30},[r10@64]",
				"F9 4A B3 2F", "vst2.8	{d27,d28,d29,d30},[r10@128]",
				"F9 4A B3 3F", "vst2.8	{d27,d28,d29,d30},[r10@256]",
				"F9 4A B8 5F", "vst2.16	{d27,d28},[r10@64]",
				"F9 4A B8 6F", "vst2.16	{d27,d28},[r10@128]",
				"F9 4A B9 5F", "vst2.16	{d27,d29},[r10@64]",
				"F9 4A B9 6F", "vst2.16	{d27,d29},[r10@128]",
				"F9 4A B3 5F", "vst2.16	{d27,d28,d29,d30},[r10@64]",
				"F9 4A B3 6F", "vst2.16	{d27,d28,d29,d30},[r10@128]",
				"F9 4A B3 7F", "vst2.16	{d27,d28,d29,d30},[r10@256]",
				"F9 4A B8 0D", "vst2.8	{d27,d28},[r10]!",
				"F9 4A B9 0D", "vst2.8	{d27,d29},[r10]!",
				"F9 4A B3 0D", "vst2.8	{d27,d28,d29,d30},[r10]!",
				"F9 4A B8 4D", "vst2.16	{d27,d28},[r10]!",
				"F9 4A B9 4D", "vst2.16	{d27,d29},[r10]!",
				"F9 4A B3 4D", "vst2.16	{d27,d28,d29,d30},[r10]!",
				"F9 4A B8 1D", "vst2.8	{d27,d28},[r10@64]!",
				"F9 4A B8 2D", "vst2.8	{d27,d28},[r10@128]!",
				"F9 4A B9 1D", "vst2.8	{d27,d29},[r10@64]!",
				"F9 4A B9 2D", "vst2.8	{d27,d29},[r10@128]!",
				"F9 4A B3 1D", "vst2.8	{d27,d28,d29,d30},[r10@64]!",
				"F9 4A B3 2D", "vst2.8	{d27,d28,d29,d30},[r10@128]!",
				"F9 4A B3 3D", "vst2.8	{d27,d28,d29,d30},[r10@256]!",
				"F9 4A B8 5D", "vst2.16	{d27,d28},[r10@64]!",
				"F9 4A B8 6D", "vst2.16	{d27,d28},[r10@128]!",
				"F9 4A B9 5D", "vst2.16	{d27,d29},[r10@64]!",
				"F9 4A B9 6D", "vst2.16	{d27,d29},[r10@128]!",
				"F9 4A B3 5D", "vst2.16	{d27,d28,d29,d30},[r10@64]!",
				"F9 4A B3 6D", "vst2.16	{d27,d28,d29,d30},[r10@128]!",
				"F9 4A B3 7D", "vst2.16	{d27,d28,d29,d30},[r10@256]!",
				"F9 4A B8 09", "vst2.8	{d27,d28},[r10],r9",
				"F9 4A B9 09", "vst2.8	{d27,d29},[r10],r9",
				"F9 4A B3 09", "vst2.8	{d27,d28,d29,d30},[r10],r9",
				"F9 4A B8 49", "vst2.16	{d27,d28},[r10],r9",
				"F9 4A B9 49", "vst2.16	{d27,d29},[r10],r9",
				"F9 4A B3 49", "vst2.16	{d27,d28,d29,d30},[r10],r9",
				"F9 4A B8 19", "vst2.8	{d27,d28},[r10@64],r9",
				"F9 4A B8 29", "vst2.8	{d27,d28},[r10@128],r9",
				"F9 4A B9 19", "vst2.8	{d27,d29},[r10@64],r9",
				"F9 4A B9 29", "vst2.8	{d27,d29},[r10@128],r9",
				"F9 4A B3 19", "vst2.8	{d27,d28,d29,d30},[r10@64],r9",
				"F9 4A B3 29", "vst2.8	{d27,d28,d29,d30},[r10@128],r9",
				"F9 4A B3 39", "vst2.8	{d27,d28,d29,d30},[r10@256],r9",
				"F9 4A B8 59", "vst2.16	{d27,d28},[r10@64],r9",
				"F9 4A B8 69", "vst2.16	{d27,d28},[r10@128],r9",
				"F9 4A B9 59", "vst2.16	{d27,d29},[r10@64],r9",
				"F9 4A B9 69", "vst2.16	{d27,d29},[r10@128],r9",
				"F9 4A B3 59", "vst2.16	{d27,d28,d29,d30},[r10@64],r9",
				"F9 4A B3 69", "vst2.16	{d27,d28,d29,d30},[r10@128],r9",
				"F9 4A B3 79", "vst2.16	{d27,d28,d29,d30},[r10@256],r9",
				"F9 CA B1 2F", "vst2.8	{d27[1],d28[1]},[r10]",
				"F9 CA B5 4F", "vst2.16	{d27[1],d28[1]},[r10]",
				"F9 CA B5 6F", "vst2.16	{d27[1],d29[1]},[r10]",
				"F9 CA B9 8F", "vst2.32	{d27[1],d28[1]},[r10]",
				"F9 CA B9 CF", "vst2.32	{d27[1],d29[1]},[r10]",
				"F9 CA B1 3F", "vst2.8	{d27[1],d28[1]},[r10@16]",
				"F9 CA B5 5F", "vst2.16	{d27[1],d28[1]},[r10@32]",
				"F9 CA B5 7F", "vst2.16	{d27[1],d29[1]},[r10@32]",
				"F9 CA B9 9F", "vst2.32	{d27[1],d28[1]},[r10@64]",
				"F9 CA B9 DF", "vst2.32	{d27[1],d29[1]},[r10@64]",
				"F9 CA B1 2D", "vst2.8	{d27[1],d28[1]},[r10]!",
				"F9 CA B5 4D", "vst2.16	{d27[1],d28[1]},[r10]!",
				"F9 CA B5 6D", "vst2.16	{d27[1],d29[1]},[r10]!",
				"F9 CA B9 8D", "vst2.32	{d27[1],d28[1]},[r10]!",
				"F9 CA B9 CD", "vst2.32	{d27[1],d29[1]},[r10]!",
				"F9 CA B1 3D", "vst2.8	{d27[1],d28[1]},[r10@16]!",
				"F9 CA B5 5D", "vst2.16	{d27[1],d28[1]},[r10@32]!",
				"F9 CA B5 7D", "vst2.16	{d27[1],d29[1]},[r10@32]!",
				"F9 CA B9 9D", "vst2.32	{d27[1],d28[1]},[r10@64]!",
				"F9 CA B9 DD", "vst2.32	{d27[1],d29[1]},[r10@64]!",
				"F9 CA B1 29", "vst2.8	{d27[1],d28[1]},[r10],r9",
				"F9 CA B5 49", "vst2.16	{d27[1],d28[1]},[r10],r9",
				"F9 CA B5 69", "vst2.16	{d27[1],d29[1]},[r10],r9",
				"F9 CA B9 89", "vst2.32	{d27[1],d28[1]},[r10],r9",
				"F9 CA B9 C9", "vst2.32	{d27[1],d29[1]},[r10],r9",
				"F9 CA B1 39", "vst2.8	{d27[1],d28[1]},[r10@16],r9",
				"F9 CA B5 59", "vst2.16	{d27[1],d28[1]},[r10@32],r9",
				"F9 CA B5 79", "vst2.16	{d27[1],d29[1]},[r10@32],r9",
				"F9 CA B9 99", "vst2.32	{d27[1],d28[1]},[r10@64],r9",
				"F9 CA B9 D9", "vst2.32	{d27[1],d29[1]},[r10@64],r9",
				"F9 CA B2 2F", "vst3.8	{d27[1],d28[1],d29[1]},[r10]",
				"F9 CA B6 4F", "vst3.16	{d27[1],d28[1],d29[1]},[r10]",
				"F9 CA B6 6F", "vst3.16	{d27[1],d29[1],d31[1]},[r10]",
				"F9 CA BA 8F", "vst3.32	{d27[1],d28[1],d29[1]},[r10]",
				"F9 CA BA CF", "vst3.32	{d27[1],d29[1],d31[1]},[r10]",
				"F9 CA B2 2D", "vst3.8	{d27[1],d28[1],d29[1]},[r10]!",
				"F9 CA B6 4D", "vst3.16	{d27[1],d28[1],d29[1]},[r10]!",
				"F9 CA B6 6D", "vst3.16	{d27[1],d29[1],d31[1]},[r10]!",
				"F9 CA BA 8D", "vst3.32	{d27[1],d28[1],d29[1]},[r10]!",
				"F9 CA BA CD", "vst3.32	{d27[1],d29[1],d31[1]},[r10]!",
				"F9 CA B2 29", "vst3.8	{d27[1],d28[1],d29[1]},[r10],r9",
				"F9 CA B6 49", "vst3.16	{d27[1],d28[1],d29[1]},[r10],r9",
				"F9 CA B6 69", "vst3.16	{d27[1],d29[1],d31[1]},[r10],r9",
				"F9 CA BA 89", "vst3.32	{d27[1],d28[1],d29[1]},[r10],r9",
				"F9 CA BA C9", "vst3.32	{d27[1],d29[1],d31[1]},[r10],r9",
				"F9 CA B2 2F", "vst3.8	{d27[1],d28[1],d29[1]},[r10]",
				"F9 CA B6 4F", "vst3.16	{d27[1],d28[1],d29[1]},[r10]",
				"F9 CA B6 6F", "vst3.16	{d27[1],d29[1],d31[1]},[r10]",
				"F9 CA BA 8F", "vst3.32	{d27[1],d28[1],d29[1]},[r10]",
				"F9 CA BA CF", "vst3.32	{d27[1],d29[1],d31[1]},[r10]",
				"F9 CA B2 2D", "vst3.8	{d27[1],d28[1],d29[1]},[r10]!",
				"F9 CA B6 4D", "vst3.16	{d27[1],d28[1],d29[1]},[r10]!",
				"F9 CA B6 6D", "vst3.16	{d27[1],d29[1],d31[1]},[r10]!",
				"F9 CA BA 8D", "vst3.32	{d27[1],d28[1],d29[1]},[r10]!",
				"F9 CA BA CD", "vst3.32	{d27[1],d29[1],d31[1]},[r10]!",
				"F9 CA B2 29", "vst3.8	{d27[1],d28[1],d29[1]},[r10],r9",
				"F9 CA B6 49", "vst3.16	{d27[1],d28[1],d29[1]},[r10],r9",
				"F9 CA B6 69", "vst3.16	{d27[1],d29[1],d31[1]},[r10],r9",
				"F9 CA BA 89", "vst3.32	{d27[1],d28[1],d29[1]},[r10],r9",
				"F9 CA BA C9", "vst3.32	{d27[1],d29[1],d31[1]},[r10],r9",
				"F9 4A B0 0F", "vst4.8	{d27,d28,d29,d30},[r10]",
				"F9 4A 91 0F", "vst4.8	{d25,d27,d29,d31},[r10]",
				"F9 4A B0 4F", "vst4.16	{d27,d28,d29,d30},[r10]",
				"F9 4A 91 4F", "vst4.16	{d25,d27,d29,d31},[r10]",
				"F9 4A B0 8F", "vst4.32	{d27,d28,d29,d30},[r10]",
				"F9 4A 91 8F", "vst4.32	{d25,d27,d29,d31},[r10]",
				"F9 4A B0 1F", "vst4.8	{d27,d28,d29,d30},[r10@64]",
				"F9 4A B0 2F", "vst4.8	{d27,d28,d29,d30},[r10@128]",
				"F9 4A B0 3F", "vst4.8	{d27,d28,d29,d30},[r10@256]",
				"F9 4A 91 1F", "vst4.8	{d25,d27,d29,d31},[r10@64]",
				"F9 4A 91 2F", "vst4.8	{d25,d27,d29,d31},[r10@128]",
				"F9 4A 91 3F", "vst4.8	{d25,d27,d29,d31},[r10@256]",
				"F9 4A B0 5F", "vst4.16	{d27,d28,d29,d30},[r10@64]",
				"F9 4A B0 6F", "vst4.16	{d27,d28,d29,d30},[r10@128]",
				"F9 4A B0 7F", "vst4.16	{d27,d28,d29,d30},[r10@256]",
				"F9 4A 91 5F", "vst4.16	{d25,d27,d29,d31},[r10@64]",
				"F9 4A 91 6F", "vst4.16	{d25,d27,d29,d31},[r10@128]",
				"F9 4A 91 7F", "vst4.16	{d25,d27,d29,d31},[r10@256]",
				"F9 4A B0 9F", "vst4.32	{d27,d28,d29,d30},[r10@64]",
				"F9 4A B0 AF", "vst4.32	{d27,d28,d29,d30},[r10@128]",
				"F9 4A B0 BF", "vst4.32	{d27,d28,d29,d30},[r10@256]",
				"F9 4A 91 9F", "vst4.32	{d25,d27,d29,d31},[r10@64]",
				"F9 4A 91 AF", "vst4.32	{d25,d27,d29,d31},[r10@128]",
				"F9 4A 91 BF", "vst4.32	{d25,d27,d29,d31},[r10@256]",
				"F9 4A B0 0D", "vst4.8	{d27,d28,d29,d30},[r10]!",
				"F9 4A 91 0D", "vst4.8	{d25,d27,d29,d31},[r10]!",
				"F9 4A B0 4D", "vst4.16	{d27,d28,d29,d30},[r10]!",
				"F9 4A 91 4D", "vst4.16	{d25,d27,d29,d31},[r10]!",
				"F9 4A B0 8D", "vst4.32	{d27,d28,d29,d30},[r10]!",
				"F9 4A 91 8D", "vst4.32	{d25,d27,d29,d31},[r10]!",
				"F9 4A B0 1D", "vst4.8	{d27,d28,d29,d30},[r10@64]!",
				"F9 4A B0 2D", "vst4.8	{d27,d28,d29,d30},[r10@128]!",
				"F9 4A B0 3D", "vst4.8	{d27,d28,d29,d30},[r10@256]!",
				"F9 4A 91 1D", "vst4.8	{d25,d27,d29,d31},[r10@64]!",
				"F9 4A 91 2D", "vst4.8	{d25,d27,d29,d31},[r10@128]!",
				"F9 4A 91 3D", "vst4.8	{d25,d27,d29,d31},[r10@256]!",
				"F9 4A B0 5D", "vst4.16	{d27,d28,d29,d30},[r10@64]!",
				"F9 4A B0 6D", "vst4.16	{d27,d28,d29,d30},[r10@128]!",
				"F9 4A B0 7D", "vst4.16	{d27,d28,d29,d30},[r10@256]!",
				"F9 4A 91 5D", "vst4.16	{d25,d27,d29,d31},[r10@64]!",
				"F9 4A 91 6D", "vst4.16	{d25,d27,d29,d31},[r10@128]!",
				"F9 4A 91 7D", "vst4.16	{d25,d27,d29,d31},[r10@256]!",
				"F9 4A B0 9D", "vst4.32	{d27,d28,d29,d30},[r10@64]!",
				"F9 4A B0 AD", "vst4.32	{d27,d28,d29,d30},[r10@128]!",
				"F9 4A B0 BD", "vst4.32	{d27,d28,d29,d30},[r10@256]!",
				"F9 4A 91 9D", "vst4.32	{d25,d27,d29,d31},[r10@64]!",
				"F9 4A 91 AD", "vst4.32	{d25,d27,d29,d31},[r10@128]!",
				"F9 4A 91 BD", "vst4.32	{d25,d27,d29,d31},[r10@256]!",
				"F9 4A B0 09", "vst4.8	{d27,d28,d29,d30},[r10],r9",
				"F9 4A 91 09", "vst4.8	{d25,d27,d29,d31},[r10],r9",
				"F9 4A B0 49", "vst4.16	{d27,d28,d29,d30},[r10],r9",
				"F9 4A 91 49", "vst4.16	{d25,d27,d29,d31},[r10],r9",
				"F9 4A B0 89", "vst4.32	{d27,d28,d29,d30},[r10],r9",
				"F9 4A 91 89", "vst4.32	{d25,d27,d29,d31},[r10],r9",
				"F9 4A B0 19", "vst4.8	{d27,d28,d29,d30},[r10@64],r9",
				"F9 4A B0 29", "vst4.8	{d27,d28,d29,d30},[r10@128],r9",
				"F9 4A B0 39", "vst4.8	{d27,d28,d29,d30},[r10@256],r9",
				"F9 4A 91 19", "vst4.8	{d25,d27,d29,d31},[r10@64],r9",
				"F9 4A 91 29", "vst4.8	{d25,d27,d29,d31},[r10@128],r9",
				"F9 4A 91 39", "vst4.8	{d25,d27,d29,d31},[r10@256],r9",
				"F9 4A B0 59", "vst4.16	{d27,d28,d29,d30},[r10@64],r9",
				"F9 4A B0 69", "vst4.16	{d27,d28,d29,d30},[r10@128],r9",
				"F9 4A B0 79", "vst4.16	{d27,d28,d29,d30},[r10@256],r9",
				"F9 4A 91 59", "vst4.16	{d25,d27,d29,d31},[r10@64],r9",
				"F9 4A 91 69", "vst4.16	{d25,d27,d29,d31},[r10@128],r9",
				"F9 4A 91 79", "vst4.16	{d25,d27,d29,d31},[r10@256],r9",
				"F9 4A B0 99", "vst4.32	{d27,d28,d29,d30},[r10@64],r9",
				"F9 4A B0 A9", "vst4.32	{d27,d28,d29,d30},[r10@128],r9",
				"F9 4A B0 B9", "vst4.32	{d27,d28,d29,d30},[r10@256],r9",
				"F9 4A 91 99", "vst4.32	{d25,d27,d29,d31},[r10@64],r9",
				"F9 4A 91 A9", "vst4.32	{d25,d27,d29,d31},[r10@128],r9",
				"F9 4A 91 B9", "vst4.32	{d25,d27,d29,d31},[r10@256],r9",
				"F9 CA B3 2F", "vst4.8	{d27[1],d28[1],d29[1],d30[1]},[r10]",
				"F9 CA B7 4F", "vst4.16	{d27[1],d28[1],d29[1],d30[1]},[r10]",
				"F9 CA 97 6F", "vst4.16	{d25[1],d27[1],d29[1],d31[1]},[r10]",
				"F9 CA BB 8F", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10]",
				"F9 CA 9B CF", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10]",
				"F9 CA B3 3F", "vst4.8	{d27[1],d28[1],d29[1],d30[1]},[r10@32]",
				"F9 CA B7 5F", "vst4.16	{d27[1],d28[1],d29[1],d30[1]},[r10@64]",
				"F9 CA 97 7F", "vst4.16	{d25[1],d27[1],d29[1],d31[1]},[r10@64]",
				"F9 CA BB 9F", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@64]",
				"F9 CA BB AF", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@128]",
				"F9 CA 9B DF", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@64]",
				"F9 CA 9B EF", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@128]",
				"F9 CA B3 2D", "vst4.8	{d27[1],d28[1],d29[1],d30[1]},[r10]!",
				"F9 CA B7 4D", "vst4.16	{d27[1],d28[1],d29[1],d30[1]},[r10]!",
				"F9 CA 97 6D", "vst4.16	{d25[1],d27[1],d29[1],d31[1]},[r10]!",
				"F9 CA BB 8D", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10]!",
				"F9 CA 9B CD", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10]!",
				"F9 CA B3 3D", "vst4.8	{d27[1],d28[1],d29[1],d30[1]},[r10@32]!",
				"F9 CA B7 5D", "vst4.16	{d27[1],d28[1],d29[1],d30[1]},[r10@64]!",
				"F9 CA 97 7D", "vst4.16	{d25[1],d27[1],d29[1],d31[1]},[r10@64]!",
				"F9 CA BB 9D", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@64]!",
				"F9 CA BB AD", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@128]!",
				"F9 CA 9B DD", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@64]!",
				"F9 CA 9B ED", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@128]!",
				"F9 CA B3 29", "vst4.8	{d27[1],d28[1],d29[1],d30[1]},[r10],r9",
				"F9 CA B7 49", "vst4.16	{d27[1],d28[1],d29[1],d30[1]},[r10],r9",
				"F9 CA 97 69", "vst4.16	{d25[1],d27[1],d29[1],d31[1]},[r10],r9",
				"F9 CA BB 89", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10],r9",
				"F9 CA 9B C9", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10],r9",
				"F9 CA B3 39", "vst4.8	{d27[1],d28[1],d29[1],d30[1]},[r10@32],r9",
				"F9 CA B7 59", "vst4.16	{d27[1],d28[1],d29[1],d30[1]},[r10@64],r9",
				"F9 CA 97 79", "vst4.16	{d25[1],d27[1],d29[1],d31[1]},[r10@64],r9",
				"F9 CA BB 99", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@64],r9",
				"F9 CA BB A9", "vst4.32	{d27[1],d28[1],d29[1],d30[1]},[r10@128],r9",
				"F9 CA 9B D9", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@64],r9",
				"F9 CA 9B E9", "vst4.32	{d25[1],d27[1],d29[1],d31[1]},[r10@128],r9",
				"EC CA BB 04", "vstmia	r10,{d27-d28}",
				"EC CA DA 02", "vstmia	r10,{s27-s28}",
				"EC EA BB 04", "vstmia	r10!,{d27-d28}",
				"ED 6A BB 04", "vstmdb	r10!,{d27-d28}",
				"EC EA DA 02", "vstmia	r10!,{s27-s28}",
				"ED 6A DA 02", "vstmdb	r10!,{s27-s28}",
				"ED 4A 5B FF", "vstr.64	d21,[r10,#-0x3fc]",
				"ED CA 5B FF", "vstr.64	d21,[r10,#0x3fc]",
				"ED CA 5B 00", "vstr.64	d21,[r10]",
				"ED 4A AA FF", "vstr.32	s21,[r10,#-0x3fc]",
				"ED CA AA FF", "vstr.32	s21,[r10,#0x3fc]",
				"ED CA AA 00", "vstr.32	s21,[r10]",
				"FF 49 58 AA", "vsub.i8	d21,d25,d26",
				"FF 59 58 AA", "vsub.i16	d21,d25,d26",
				"FF 69 58 AA", "vsub.i32	d21,d25,d26",
				"FF 79 58 AA", "vsub.i64	d21,d25,d26",
				"FF 4C 68 EE", "vsub.i8	q11,q14,q15",
				"FF 5C 68 EE", "vsub.i16	q11,q14,q15",
				"FF 6C 68 EE", "vsub.i32	q11,q14,q15",
				"FF 7C 68 EE", "vsub.i64	q11,q14,q15",
				"EF 69 5D AA", "vsub.f32	d21,d25,d26",
				"EF 6C 6D EE", "vsub.f32	q11,q14,q15",
				"EE 7C AA CD", "vsub.f32	s21,s25,s26",
				"EE 79 5B EA", "vsub.f64	d21,d25,d26",
				"EF CC 56 AE", "vsubhn.i16	d21,q14,q15",
				"EF DC 56 AE", "vsubhn.i32	d21,q14,q15",
				"EF EC 56 AE", "vsubhn.i64	d21,q14,q15",
				"EF C9 62 AA", "vsubl.s8	q11,d25,d26",
				"EF D9 62 AA", "vsubl.s16	q11,d25,d26",
				"EF E9 62 AA", "vsubl.s32	q11,d25,d26",
				"FF C9 62 AA", "vsubl.u8	q11,d25,d26",
				"FF D9 62 AA", "vsubl.u16	q11,d25,d26",
				"FF E9 62 AA", "vsubl.u32	q11,d25,d26",
				"EF CC 63 AA", "vsubw.s8	q11,q14,d26",
				"EF DC 63 AA", "vsubw.s16	q11,q14,d26",
				"EF EC 63 AA", "vsubw.s32	q11,q14,d26",
				"FF CC 63 AA", "vsubw.u8	q11,q14,d26",
				"FF DC 63 AA", "vsubw.u16	q11,q14,d26",
				"FF EC 63 AA", "vsubw.u32	q11,q14,d26",
				"FF F2 50 2A", "vswp	d21,d26",
				"FF F2 60 6E", "vswp	q11,q15",
				"FF FB 58 AA", "vtbl.8	d21,{d27},d26",
				"FF FB 59 AA", "vtbl.8	d21,{d27,d28},d26",
				"FF FB 5A AA", "vtbl.8	d21,{d27,d28,d29},d26",
				"FF FB 5B AA", "vtbl.8	d21,{d27,d28,d29,d30},d26",
				"FF FB 58 EA", "vtbx.8	d21,{d27},d26",
				"FF FB 59 EA", "vtbx.8	d21,{d27,d28},d26",
				"FF FB 5A EA", "vtbx.8	d21,{d27,d28,d29},d26",
				"FF FB 5B EA", "vtbx.8	d21,{d27,d28,d29,d30},d26",
				"FF F2 50 AA", "vtrn.8	d21,d26",
				"FF F6 50 AA", "vtrn.16	d21,d26",
				"FF FA 50 AA", "vtrn.32	d21,d26",
				"FF F2 60 EE", "vtrn.8	q11,q15",
				"FF F6 60 EE", "vtrn.16	q11,q15",
				"FF FA 60 EE", "vtrn.32	q11,q15",
				"EF 49 58 BA", "vtst.8	d21,d25,d26",
				"EF 59 58 BA", "vtst.16	d21,d25,d26",
				"EF 69 58 BA", "vtst.32	d21,d25,d26",
				"EF 4C 68 FE", "vtst.8	q11,q14,q15",
				"EF 5C 68 FE", "vtst.16	q11,q14,q15",
				"EF 6C 68 FE", "vtst.32	q11,q14,q15",
				"FF F2 51 2A", "vuzp.8	d21,d26",
				"FF F6 51 2A", "vuzp.16	d21,d26",
				"FF F2 61 6E", "vuzp.8	q11,q15",
				"FF F6 61 6E", "vuzp.16	q11,q15",
				"FF FA 61 6E", "vuzp.32	q11,q15",
				"FF F2 51 AA", "vzip.8	d21,d26",
				"FF F6 51 AA", "vzip.16	d21,d26",
				"FF F2 61 EE", "vzip.8	q11,q15",
				"FF F6 61 EE", "vzip.16	q11,q15",
				"FF FA 61 EE", "vzip.32	q11,q15",
			};

		disassembleInstArray(insts, thumbOptions);
	}


	/**
	 * Test for Thumb 32-bit Imm12
	 * see reference manual algorithm for ThumbExpandImm
	 */
	@Test
	public void testThumb2ExpandImm12() {

		System.out.println("\n================== Thumb2 Expand Imm12 Mode ==================\n");

		// A6.3.2 Modified Immediate constants in Thumb 32-bit instructions

		String[] insts = {
				"F1 0A 05 71", "add.w	r5,r10,#0x71",
				"F1 0B 06 F7", "add.w	r6,r11,#0xf7",
				"F1 09 14 78", "add.w	r4,r9,#0x780078",
				"F1 08 13 FC", "add.w	r3,r8,#0xfc00fc",
				"F1 07 25 64", "add.w	r5,r7,#0x64006400",
				"F1 06 25 E3", "add.w	r5,r6,#0xe300e300",
				"F1 07 46 60", "add.w	r6,r7,#0xe0000000",
				"F1 08 47 E0", "add.w	r7,r8,#0x70000000",
				"F5 0A 05 60", "add.w	r5,r10,#0xe00000",
				"F5 0A 45 60", "add.w	r5,r10,#0xe000",
				"F5 0A 65 60", "add.w	r5,r10,#0xe00",
				"F1 1A 05 71", "adds.w	r5,r10,#0x71",
				"F1 1B 06 F7", "adds.w	r6,r11,#0xf7",
				"F1 19 14 78", "adds.w	r4,r9,#0x780078",
				"F1 18 13 FC", "adds.w	r3,r8,#0xfc00fc",
				"F1 17 25 64", "adds.w	r5,r7,#0x64006400",
				"F1 16 25 E3", "adds.w	r5,r6,#0xe300e300",
				"F1 17 46 60", "adds.w	r6,r7,#0xe0000000",
				"F1 18 47 E0", "adds.w	r7,r8,#0x70000000",
				"F5 1A 05 60", "adds.w	r5,r10,#0xe00000",
				"F5 1A 45 60", "adds.w	r5,r10,#0xe000",
				"F5 1A 65 60", "adds.w	r5,r10,#0xe00",
				};

		disassembleInstArray(insts, thumbOptions);
	}

	/**
	 * Test for Thumb2 shifter operand).
	 */
	@Test
	public void testThumb2ShifterOperand() {

		System.out.println("\n================== Thumb2 Shifter Operand ==================\n");
		String[] insts = {
				"EB 09 05 0A", "add.w	r5,r9,r10",
				"EB 08 14 A9", "add.w	r4,r8,r9,asr #6",
				"EB 07 03 48", "add.w	r3,r7,r8,lsl #1",
				"EB 06 02 17", "add.w	r2,r6,r7,lsr #32",
				"EB 09 75 F8", "add.w	r5,r9,r8,ror #31",
				"EB 08 05 39", "add.w	r5,r8,r9,rrx",
				};
		disassembleInstArray(insts, thumbOptions);
	}

	/**
	 * check for instructions that will only get disassembled
	 * a certain way for ARMv6T2
	 */
	@Test
	public void testThumb2V6T2Instructions() {
		System.out.println("\n================== ARMv6T2 Hint Instructions ==================\n");

		String[] insts = {
				"F3 BF 8F 2F", "invalid opcode",				// 1111 0011 1011 :::: 10.0 :::: 0010 ::::	// A8.6.30	T1
				"F3 AF 80 F0", "nop.w",							// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.40	T1
				"F3 AF 80 FD", "nop.w",							// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.40	T1
				"F3 BF 8F 50", "invalid opcode",				// 1111 0011 1011 :::: 10.0 :::: 0101 xxxx	// A8.6.41	T1
				"F3 BF 8F 52", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
				"F3 BF 8F 53", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
				"F3 BF 8F 56", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
				"F3 BF 8F 57", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
				"F3 BF 8F 5A", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
				"F3 BF 8F 5B", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
				"F3 BF 8F 5E", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
				"F3 BF 8F 5F", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0101 xxxx  // A8.6.41  T1
				"F3 BF 8F 42", "invalid opcode",				// 1111 0011 1011 :::: 10.0 :::: 0100 xxxx	// A8.6.42	T1
				"F3 BF 8F 43", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
				"F3 BF 8F 46", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
				"F3 BF 8F 47", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
				"F3 BF 8F 4A", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
				"F3 BF 8F 4B", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
				"F3 BF 8F 4D", "invalid opcode",	            // 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
				"F3 BF 8F 4E", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
				"F3 BF 8F 4F", "invalid opcode",                // 1111 0011 1011 :::: 10.0 :::: 0100 xxxx  // A8.6.42  T1
				"F3 AF 80 04", "nop.w",							// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.158	T1
				"F3 AF 80 02", "nop.w",							// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.411 T1
				"F3 AF 80 03", "nop.w",							// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.412 T1
				"F3 AF 80 01", "nop.w",							// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.413 T1
		};

		Map<String, Object> options = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : thumbOptions.entrySet())
			options.put(entry.getKey(), entry.getValue());
		options.put(DisassemblerARM.IDisassemblerOptionsARM.VERSION_MODE, InstructionParserARM.ARMv6T2);
		disassembleInstArray(insts, options);
	}


	/**
	 * only BL & BLX with the J1 & J2 bits both set to 1 are allowed
	 * for thumb2; everything else should produce "invalid opcode"
	 */
	@Test
	public void testThumb2V4TInstructions() {

		System.out.println("\n================== Thumb2 V4T Instructions ==================\n");

		String[] insts = {
//				"Fx xx xx 0x", "bl	0x________",				// 1111 0xxx xxxx xxxx 11x1 xxxx xxxx xxxx	// A8.6.23	T1
//	ARM			"0B FF FF FE", "bleq	0x00000000",
//	ARM			"EB 00 00 1C", "bl	0x00000078",
//	ARM			"EB FF FF 00", "bl	0xfffffc08",
//
//				"Fx xx xx 0x", "blx	0x________",				// 1111 0xxx xxxx xxxx 11x0 xxxx xxxx xxxx	// A8.6.23	T2
//	ARM			"FA FF FF FE", "blx	0x00000000",
//	ARM			"01 2F FF 39", "blxeq	r9",
				"F3 BF 8F 2F", "invalid opcode",				// 1111 0011 1011 :::: 10.0 :::: 0010 ::::	// A8.6.30	T1
				"F3 AF 80 F0", "invalid opcode",				// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.40	T1
				"F3 AF 80 FD", "invalid opcode",				// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.40	T1
				"F3 AF 80 04", "invalid opcode",				// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.158	T1
				"F3 AF 80 02", "invalid opcode",				// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.411 T1
				"F3 AF 80 03", "invalid opcode",				// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.412 T1
				"F3 AF 80 01", "invalid opcode",				// 1111 0011 1010 :::: 10.0 .xxx xxxx xxxx	// A8.6.413 T1
		};

		Map<String, Object> options = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : thumbOptions.entrySet())
			options.put(entry.getKey(), entry.getValue());
		options.put(DisassemblerARM.IDisassemblerOptionsARM.VERSION_MODE, InstructionParserARM.ARMv4T);
		disassembleInstArray(insts, options);
	}

	/**
	 * Convert hex string into byte array.
	 */
	private static byte[] getByteArray(String byteHexString) {
		byteHexString = byteHexString.replaceAll("0x", "");
		StringTokenizer tn = new StringTokenizer(byteHexString);

		int cnt = tn.countTokens();
		byte[] ret = new byte[cnt];
		for (int i = 0; i < cnt; i++) {
			ret[i] = (byte) Integer.parseInt(tn.nextToken(), 16);
		}

		return ret;
	}

	/**
	 * Disassemble a single instruction and verify the output.
	 */
	private void disassembleInst(long address, String code, IJumpToAddress expectedJumpAddr, String expectedMnemonics,
			Map<String, Object> options) {
		if (options == null)
			options = armOptions;

		IAddress addr = new Addr32(address);
		ByteBuffer codeBuf = ByteBuffer.wrap(getByteArray(code));
		String msg;

		InstructionParserARM disa = new InstructionParserARM(addr, codeBuf);

		IDisassembledInstruction output = null;
		try {
			output = disa.disassemble(options);
		} catch (CoreException e) {
			Assert.fail(e.getLocalizedMessage());
		}

		Assert.assertEquals(codeBuf.capacity(), output.getSize());
		Assert.assertEquals(address, output.getAddress().getValue().longValue());

		if (expectedJumpAddr != null) {
			msg = "Address\n " + expectedJumpAddr + "\n not match expected\n " + output.getJumpToAddress();
			Assert.assertNotNull(output.getJumpToAddress());
			Assert.assertEquals(msg, expectedJumpAddr, output.getJumpToAddress());
		}

		if (expectedMnemonics != null) {
			msg = "Mnemonics\n " + output.getMnemonics() + "\n not match expected\n " + expectedMnemonics;
			Assert
					.assertTrue(msg, TestUtils.stringCompare(expectedMnemonics, output.getMnemonics(), false, true,
							true));
		}

		System.out.println(output.getMnemonics());
	}

	/**
	 * Disassemble a single instruction and verify the output.
	 */
	private void catchCodeBufferUnderflowException(long address, String code,
			Map<String, Object> options) {
		if (options == null)
			options = armOptions;

		IAddress addr = new Addr32(address);
		ByteBuffer codeBuf = ByteBuffer.wrap(getByteArray(code));

		InstructionParserARM disa = new InstructionParserARM(addr, codeBuf);

		try {
			disa.disassemble(options);
			Assert.fail("expected disa.disassemble() to throw CodeBufferUnderflowException");
		} catch (CodeBufferUnderflowException e) {
			System.out.println("properly caught CodeBufferUnderflowException");
		} catch (CoreException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}

	/**
	 * Disassemble an array of instructions and verify the output.
	 */
	private void disassembleInstArray(String[] insts, Map<String, Object> options) {
		if (insts.length % 2 != 0)
			throw new IllegalArgumentException();

		// Don't show address nor bytes
		options.put(IDisassemblerOptions.MNEMONICS_SHOW_ADDRESS, false);
		options.put(IDisassemblerOptions.MNEMONICS_SHOW_BYTES, false);

		int cnt = insts.length;

		for (int i = 0; i < cnt; i += 2) {
			disassembleInst(0 /* don't care */, insts[i], null /* don't care */, insts[i + 1], options);
		}
	}

}
