/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
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

		thumbOptions = new HashMap<String, Object>();
		thumbOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_ADDRESS, true);
		thumbOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_BYTES, true);
		thumbOptions.put(IDisassemblerOptionsARM.DISASSEMBLER_MODE, InstructionParserARM.DISASSEMBLER_MODE_THUMB);

		sDisassembler = new DisassemblerARM();
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * Test for ARM v5 instructions.
	 */
	@Test
	public void testArmv5Instructions() {
		
		System.out.println("\n===================== ARMv5 ========================\n");
		String[] insts = { "E0 A1 00 02", "adc	r0,r1,r2", "E2 81 00 01", "add	r0,r1,#0x1", "E0 01 00 02",
				"and	r0,r1,r2", "EA FF FF FE", "b		0x0", "EB FF FF FE", "bl		0x0", "E1 C1 00 02", "bic	r0,r1,r2",
				"E1 20 01 70", "bkpt	#0x10", "FA FF FF FE", "blx	0x0", "E1 2F FF 30", "blx	r0", "E1 2F FF 10",
				"bx		r0", "EE 11 00 02", "cdp	p0,0x1,cr0,cr1,cr2,0x0", "FE 11 00 02", "cdp2	p0,0x1,cr0,cr1,cr2,0x0",
				"E1 6F 0F 11", "clz	r0,r1", "E3 70 00 00", "cmn	r0,#0x0", "E1 50 00 01", "cmp	r0,r1", "E2 21 00 10",
				"eor	r0,r1,#0x10", "ED 91 00 00", "ldc	p0,cr0,[r1,#0x0]", "FD 91 00 00", "ldc2	p0,cr0,[r1,#0x0]",
				"E8 90 00 06", "ldmia	r0,{r1,r2}", "E8 D0 00 06", "ldmia	r0,{r1,r2}^", "E8 F0 00 06",
				"ldmia	r0!,{r1,r2}^", "E7 91 00 81", "ldr	r0,[r1,r1,lsl #0x1]", "E5 D1 00 01", "ldrb	r0,[r1,#0x1]",
				"E4 F1 00 01", "ldrbt	r0,[r1],#0x1", "E1 C1 00 D1", "ldrd	r0,[r1,#0x1]", "E1 D1 00 B1",
				"ldrh	r0,[r1,#0x1]", "E1 D1 00 D1", "ldrsb	r0,[r1,#0x1]", "E1 D1 00 F1", "ldrsh	r0,[r1,#0x1]",
				"E4 B1 00 01", "ldrt	r0,[r1],#0x1", "EE 22 10 13", "mcr	p0,0x1,r1,cr2,cr3", "FE 22 10 13",
				"mcr2	p0,0x1,r1,cr2,cr3", "EC 42 10 13", "mcrr	p0,0x1,r1,r2,cr3", "FC 42 10 13",
				"mcrr2	p0,0x1,r1,r2,cr3", "E0 20 32 91", "mla	r0,r1,r2,r3", "E3 A0 00 10", "mov	r0,#0x10",
				"EE 32 10 13", "mrc	p0,0x1,r1,cr2,cr3", "FE 32 10 13", "mrc2	p0,0x1,r1,cr2,cr3", "EC 52 10 13",
				"mrrc	p0,0x1,r1,r2,cr3", "FC 52 10 13", "mrrc2	p0,0x1,r1,r2,cr3", "E1 0F 00 00", "mrs	r0,cpsr",
				"E3 29 F0 01", "msr	cpsr_cf,#0x1", "E1 69 F0 00", "msr	spsr_cf,r0", "E0 00 02 91", "mul	r0,r1,r2",
				"E1 E0 00 01", "mvn	r0,r1", "E1 81 00 02", "orr	r0,r1,r2", "F5 D0 F0 01", "pld	[r0,#0x1]",
				"E1 02 00 51", "qadd	r0,r1,r2", "E1 42 00 51", "qdadd	r0,r1,r2", "E1 62 00 51", "qdsub	r0,r1,r2",
				"E1 22 00 51", "qsub	r0,r1,r2", "E0 61 00 02", "rsb	r0,r1,r2", "E0 E1 00 02", "rsc	r0,r1,r2",
				"E0 C1 00 02", "sbc	r0,r1,r2", "E1 00 32 81", "smlabb	r0,r1,r2,r3", "E0 E1 03 92", "smlal	r0,r1,r2,r3",
				"E1 41 03 82", "smlalbb	r0,r1,r2,r3", "E1 20 32 81", "smlawb	r0,r1,r2,r3", "E1 60 02 81",
				"smulbb	r0,r1,r2", "E0 C1 03 92", "smull	r0,r1,r2,r3", "E1 20 02 A1", "smulwb	r0,r1,r2", "ED 81 10 00",
				"stc	p0,cr1,[r1,#0x0]", "FD 81 10 00", "stc2	p0,cr1,[r1,#0x0]", "E8 A0 00 06", "stmia	r0!,{r1,r2}",
				"E8 C0 00 06", "stmia	r0,{r1,r2}^", "E4 81 00 01", "str	r0,[r1],#0x1", "E4 C1 00 01",
				"strb	r0,[r1],#0x1", "E4 E1 00 01", "strbt	r0,[r1],#0x1", "E0 C1 00 F1", "strd	r0,[r1],#0x1",
				"E0 C1 00 B1", "strh	r0,[r1],#0x1", "E4 A1 00 01", "strt	r0,[r1],#0x1", "E0 41 00 02", "sub	r0,r1,r2",
				"EF 00 00 10", "swi	#0x10", "E1 02 00 91", "swp	r0,r1,[r2]", "E1 42 00 91", "swpb	r0,r1,[r2]",
				"E1 30 00 01", "teq	r0,r1", "E1 10 00 01", "tst	r0,r1", "E0 A1 03 92", "umlal	r0,r1,r2,r3",
				"E0 81 03 92", "umull	r0,r1,r2,r3", };

		disassembleInstArray(insts, armOptions);
	}

	@Test
	public void testArmVFPInstructions() {
		
		System.out.println("\n====================== ARM VFP ======================\n");
		String[] insts = { "EE B0 0B C1", "fabsd	d0,d1", "EE B0 0A E0", "fabss	s0,s1", "EE 31 0B 02", "faddd	d0,d1,d2",
				"EE 30 0A 81", "fadds	s0,s1,s2", "EE B4 0B 41", "fcmpd	d0,d1", "EE B4 0A 60", "fcmps	s0,s1",
				"EE B4 0B C1", "fcmped	d0,d1", "EE B4 0A E0", "fcmpes	s0,s1", "EE B5 0B C0", "fcmpezd	d0",
				"EE B5 0A C0", "fcmpezs	s0", "EE B5 0B 40", "fcmpzd	d0", "EE B5 0A 40", "fcmpzs	s0", "EE B0 0B 41",
				"fcpyd	d0,d1", "EE B0 0A 60", "fcpys	s0,s1", "EE B7 0A E0", "fcvtds	d0,s1", "EE B7 0B C1",
				"fcvtsd	s0,d1", "EE 81 0B 02", "fdivd	d0,d1,d2", "EE 80 0A 81", "fdivs	s0,s1,s2", "ED 91 0B 01",
				"fldd	d0,[r1,#0x4]", "ED 91 0A 01", "flds	s0,[r1,#0x4]", "EC 90 1B 06", "fldmiad	r0,{d1,d2,d3}",
				"EC D0 0A 03", "fldmias	r0,{s1,s2,s3}", "EE 01 0B 02", "fmacd	d0,d1,d2", "EE 00 0A 81",
				"fmacs	s0,s1,s2", "EE 20 1B 10", "fmdhr	d0,r1", "EE 00 1B 10", "fmdlr	d0,r1", "EC 42 1B 10",
				"fmdrr	d0,r1,r2", "EE 31 0B 10", "fmrdh	r0,d1", "EE 11 0B 10", "fmrdl	r0,d1", "EC 51 0B 12",
				"fmrrd	r0,r1,d2", "EC 51 0A 11", "fmrrs	r0,r1,{s2,s3}", "EE 10 0A 90", "fmrs	r0,s1", "0E F1 FA 10",
				"fmstateq", "EE F1 0A 10", "fmrx	r0,fpscr", "EE 11 0B 02", "fmscd	d0,d1,d2", "EE 10 0A 81",
				"fmscs	s0,s1,s2", "EE 00 1A 10", "fmsr	s0,r1", "EC 43 2A 10", "fmsrr	{s0,s1},r2,r3", "EE 21 0B 02",
				"fmuld	d0,d1,d2", "EE 20 0A 81", "fmuls	s0,s1,s2", "EE E0 1A 10", "fmxr	fpsid,r1", "EE B1 0B 41",
				"fnegd	d0,d1", "EE B1 0A 60", "fnegs	s0,s1", "EE 01 0B 42", "fnmacd	d0,d1,d2", "EE 00 0A C1",
				"fnmacs	s0,s1,s2", "EE 11 0B 42", "fnmscd	d0,d1,d2", "EE 10 0A C1", "fnmscs	s0,s1,s2", "EE 21 0B 42",
				"fnmuld	d0,d1,d2", "EE 20 0A C1", "fnmuls	s0,s1,s2", "EE B8 0B E0", "fsitod	d0,s1", "EE B8 0A E0",
				"fsitos	s0,s1", "EE B1 0B C1", "fsqrtd	d0,d1", "EE B1 0A E0", "fsqrts	s0,s1", "ED 01 0B 01",
				"fstd	d0,[r1,#-0x4]", "ED 01 0A 01", "fsts	s0,[r1,#-0x4]", "EC 80 1B 06", "fstmiad	r0,{d1,d2,d3}",
				"EC C0 0A 03", "fstmias	r0,{s1,s2,s3}", "EE 31 0B 42", "fsubd	d0,d1,d2", "EE 30 0A C1",
				"fsubs	s0,s1,s2", "EE BD 0B 41", "ftosid	s0,d1", "EE BD 0A 60", "ftosis	s0,s1", "EE BC 0B 41",
				"ftouid	s0,d1", "EE BC 0A 60", "ftouis	s0,s1", "EE B8 0B 60", "fuitod	d0,s1", "EE B8 0A 60",
				"fuitos	s0,s1", };

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM condition code.
	 */
	@Test
	public void testArmConditionCode() {
		
		System.out.println("\n================ ARM Condition Code ================\n");
		String[] insts = { "00 A1 00 02", "adceq	r0,r1,r2", "10 A1 00 02", "adcne	r0,r1,r2", "20 A1 00 02",
				"adccs	r0,r1,r2", "30 A1 00 02", "adccc	r0,r1,r2", "40 A1 00 02", "adcmi	r0,r1,r2", "50 A1 00 02",
				"adcpl	r0,r1,r2", "60 A1 00 02", "adcvs	r0,r1,r2", "70 A1 00 02", "adcvc	r0,r1,r2", "80 A1 00 02",
				"adchi	r0,r1,r2", "90 A1 00 02", "adcls	r0,r1,r2", "A0 A1 00 02", "adcge	r0,r1,r2", "B0 A1 00 02",
				"adclt	r0,r1,r2", "C0 A1 00 02", "adcgt	r0,r1,r2", "D0 A1 00 02", "adcle	r0,r1,r2", "E0 A1 00 02",
				"adc	r0,r1,r2", };

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM addressing mode 1 (shifter operand).
	 */
	@Test
	public void testArmAddrMode1() {
		
		System.out.println("\n================== ARM Addr Mode 1 ==================\n");
		String[] insts = { "E2 81 00 11", "add	r0,r1,#0x11", "E0 81 00 02", "add	r0,r1,r2", "E0 81 08 82",
				"add	r0,r1,r2,lsl #0x11", "E0 81 03 12", "add	r0,r1,r2,lsl r3", "E0 81 08 A2",
				"add	r0,r1,r2,lsr #0x11", "E0 81 03 32", "add	r0,r1,r2,lsr r3", "E0 81 08 C2",
				"add	r0,r1,r2,asr #0x11", "E0 81 03 52", "add	r0,r1,r2,asr r3", "E0 81 08 E2",
				"add	r0,r1,r2,ror #0x11", "E0 81 03 72", "add	r0,r1,r2,ror r3", "E0 81 00 62", "add	r0,r1,r2,rrx", };

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM addressing mode 2.
	 */
	@Test
	public void testArmAddrMode2() {
		
		System.out.println("\n================== ARM Addr Mode 2 ==================\n");
		String[] insts = { "E5 91 00 11", "ldr	r0,[r1,#0x11]", "E5 11 00 11", "ldr	r0,[r1,#-0x11]", "E7 91 00 02",
				"ldr	r0,[r1,r2]", "E7 11 00 02", "ldr	r0,[r1,-r2]", "E7 91 08 82", "ldr	r0,[r1,r2,lsl #0x11]",
				"E7 91 08 A2", "ldr	r0,[r1,r2,lsr #0x11]", "E7 91 08 C2", "ldr	r0,[r1,r2,asr #0x11]", "E7 91 08 E2",
				"ldr	r0,[r1,r2,ror #0x11]", "E7 91 00 62", "ldr	r0,[r1,r2,rrx]", "E7 11 08 82",
				"ldr	r0,[r1,-r2,lsl #0x11]", "E7 11 08 A2", "ldr	r0,[r1,-r2,lsr #0x11]", "E7 11 08 C2",
				"ldr	r0,[r1,-r2,asr #0x11]", "E7 11 08 E2", "ldr	r0,[r1,-r2,ror #0x11]", "E7 11 00 62",
				"ldr	r0,[r1,-r2,rrx]", "E5 B1 00 11", "ldr	r0,[r1,#0x11]!", "E5 31 00 11", "ldr	r0,[r1,#-0x11]!",
				"E7 B1 00 02", "ldr	r0,[r1,r2]!", "E7 31 00 02", "ldr	r0,[r1,-r2]!", "E7 B1 08 82",
				"ldr	r0,[r1,r2,lsl #0x11]!", "E7 B1 08 A2", "ldr	r0,[r1,r2,lsr #0x11]!", "E7 B1 08 C2",
				"ldr	r0,[r1,r2,asr #0x11]!", "E7 B1 08 E2", "ldr	r0,[r1,r2,ror #0x11]!", "E7 B1 00 62",
				"ldr	r0,[r1,r2,rrx]!", "E7 31 08 82", "ldr	r0,[r1,-r2,lsl #0x11]!", "E7 31 08 A2",
				"ldr	r0,[r1,-r2,lsr #0x11]!", "E7 31 08 C2", "ldr	r0,[r1,-r2,asr #0x11]!", "E7 31 08 E2",
				"ldr	r0,[r1,-r2,ror #0x11]!", "E7 31 00 62", "ldr	r0,[r1,-r2,rrx]!", "E4 91 00 11",
				"ldr	r0,[r1],#0x11", "E4 11 00 11", "ldr	r0,[r1],#-0x11", "E6 91 00 02", "ldr	r0,[r1],r2",
				"E6 11 00 02", "ldr	r0,[r1],-r2", "E6 91 08 82", "ldr	r0,[r1],r2,lsl #0x11", "E6 91 08 A2",
				"ldr	r0,[r1],r2,lsr #0x11", "E6 91 08 C2", "ldr	r0,[r1],r2,asr #0x11", "E6 91 08 E2",
				"ldr	r0,[r1],r2,ror #0x11", "E6 91 00 62", "ldr	r0,[r1],r2,rrx", "E6 11 08 82",
				"ldr	r0,[r1],-r2,lsl #0x11", "E6 11 08 A2", "ldr	r0,[r1],-r2,lsr #0x11", "E6 11 08 C2",
				"ldr	r0,[r1],-r2,asr #0x11", "E6 11 08 E2", "ldr	r0,[r1],-r2,ror #0x11", "E6 11 00 62",
				"ldr	r0,[r1],-r2,rrx", };

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM addressing mode 3.
	 */
	@Test
	public void testArmAddrMode3() {
		
		System.out.println("\n================== ARM Addr Mode 3 ==================\n");
		String[] insts = { "E1 C1 01 B0", "strh	r0,[r1,#0x10]", "E1 41 01 B0", "strh	r0,[r1,#-0x10]", "E1 81 00 B2",
				"strh	r0,[r1,r2]", "E1 01 00 B2", "strh	r0,[r1,-r2]", "E1 E1 01 B0", "strh	r0,[r1,#0x10]!",
				"E1 61 01 B0", "strh	r0,[r1,#-0x10]!", "E1 A1 00 B2", "strh	r0,[r1,r2]!", "E1 21 00 B2",
				"strh	r0,[r1,-r2]!", "E0 C1 01 B0", "strh	r0,[r1],#0x10", "E0 41 01 B0", "strh	r0,[r1],#-0x10",
				"E0 81 00 B2", "strh	r0,[r1],r2", "E0 01 00 B2", "strh	r0,[r1],-r2", };

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM addressing mode 4.
	 */
	@Test
	public void testArmAddrMode4() {
		
		System.out.println("\n================== ARM Addr Mode 4 ==================\n");
		String[] insts = { "E8 90 00 06", "ldmia	r0,{r1,r2}", "E9 90 00 06", "ldmib	r0,{r1,r2}", "E8 10 00 06",
				"ldmda	r0,{r1,r2}", "E9 10 00 06", "ldmdb	r0,{r1,r2}", };

		disassembleInstArray(insts, armOptions);
	}

	/**
	 * Test for ARM addressing mode 5.
	 */
	@Test
	public void testArmAddrMode5() {
		
		System.out.println("\n================== ARM Addr Mode 5 ==================\n");
		String[] insts = { "ED 92 10 04", "ldc	p0,cr1,[r2,#0x10]", "ED 12 10 04", "ldc	p0,cr1,[r2,#-0x10]",
				"ED B2 10 04", "ldc	p0,cr1,[r2,#0x10]!", "ED 32 10 04", "ldc	p0,cr1,[r2,#-0x10]!", "EC B2 10 04",
				"ldc	p0,cr1,[r2],#0x10", "EC 32 10 04", "ldc	p0,cr1,[r2],#-0x10", "EC 92 10 00", "ldc	p0,cr1,[r2]", };

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
		disassembleInst(0x00000004, "ea ff ff fe", new JumpToAddress(0x00000004, false, false),
				"4:           ea ff ff fe                     b	0x0", armOptions);
		disassembleInst(0x00000004, "eb ff ff fe", new JumpToAddress(0x00000004, false, true),
				"4:           eb ff ff fe                     bl	0x0", armOptions);
		disassembleInst(0x00000004, "fa ff ff fe", new JumpToAddress(0x00000004, true, true),
				"4:           fa ff ff fe                     blx	0x0", armOptions);
		disassembleInst(0x00000004, "e1 2f ff 30", new JumpToAddress("r0", false, true),
				"4:           e1 2f ff 30                     blx	r0", armOptions);
		disassembleInst(0x00000004, "e1 2f ff 10", new JumpToAddress("r0", false, false),
				"4:           e1 2f ff 10                     bx	r0", armOptions);
		disassembleInst(0x00000004, "e1 a0 f0 0e", new JumpToAddress("lr", false, false),
				"4:           e1 a0 f0 0e                     mov	pc,lr", armOptions);
	}

	/**
	 * Test for Thumb instructions.
	 */
	@Test
	public void testThumbInstructions() {
		
		System.out.println("\n======================= Thumb =======================\n");
		String[] insts = { "41 48", "adc	r0,r1", "1D 08", "add	r0,r1,#0x4", "30 04", "add	r0,#0x4", "18 88",
				"add	r0,r1,r2", "44 48", "add	r0,r9", "A0 01", "add	r0,pc,#0x4", "A8 01", "add	r0,sp,#0x4", "B0 01",
				"add	sp,#0x4", "40 08", "and	r0,r1", "10 88", "asr	r0,r1,#0x2", "41 08", "asr	r0,r1", "D0 FE", "beq	0",
				"E7 FE", "b	0", "43 88", "bic	r0,r1", "BE 01", "bkpt	#0x1", "F7 FF FF FE", "bl	0", "F7 FF EF FE",
				"blx	0", "47 80", "blx	r0", "47 00", "bx	r0", "42 C8", "cmn	r0,r1", "28 01", "cmp	r0,#1", "42 88",
				"cmp	r0,r1", "45 48", "cmp	r0,r9", "B6 67", "cpsie	aif", "46 48", "cpy	r0,r9", "40 48", "eor	r0,r1",
				"C8 0E", "ldmia	r0!,{r1,r2,r3}", "68 48", "ldr	r0,[r1,#0x4]", "58 88", "ldr	r0,[r1,r2]", "48 01",
				"ldr	r0,[pc,#0x4]", "98 01", "ldr	r0,[sp,#0x4]", "78 88", "ldrb	r0,[r1,#0x2]", "5C 88",
				"ldrb	r0,[r1,r2]", "88 48", "ldrh	r0,[r1,#0x2]", "5A 88", "ldrh	r0,[r1,r2]", "56 88",
				"ldrsb	r0,[r1,r2]", "5E 88", "ldrsh	r0,[r1,r2]", "00 88", "lsl	r0,r1,#0x2", "40 88", "lsl	r0,r1",
				"08 88", "lsr	r0,r1,#0x2", "40 C8", "lsr	r0,r1", "20 01", "mov	r0,#1", "1C 08", "mov	r0,r1", "43 48",
				"mul	r0,r1", "43 C8", "mvn	r0,r1", "42 48", "neg	r0,r1", "43 08", "orr	r0,r1", "BC 0E",
				"pop	{r1,r2,r3}", "B4 0E", "push	{r1,r2,r3}", "BA 08", "rev	r0,r1", "BA 48", "rev16	r0,r1", "BA C8",
				"revsh	r0,r1", "41 C8", "ror	r0,r1", "41 88", "sbc	r0,r1", "B6 58", "setend	be", "C0 0E",
				"stmia	r0!,{r1,r2,r3}", "60 48", "str	r0,[r1,#0x4]", "50 88", "str	r0,[r1,r2]", "90 01",
				"str	r0,[sp,#0x4]", "70 88", "strb	r0,[r1,#0x2]", "54 88", "strb	r0,[r1,r2]", "80 48",
				"strh	r0,[r1,#0x2]", "52 88", "strh	r0,[r1,r2]", "1F 08", "sub	r0,r1,#0x4", "38 04", "sub	r0,#0x4",
				"1A 88", "sub	r0,r1,r2", "B0 81", "sub	sp,#0x4", "DF 00", "swi	#0x0", "B2 48", "sxtb	r0,r1", "B2 08",
				"sxth	r0,r1", "42 08", "tst	r0,r1", "B2 C8", "uxtb	r0,r1", "B2 88", "uxth	r0,r1", };

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
		disassembleInst(0x00000004, "d0 fe", new JumpToAddress(0x00000004, false, false),
				"4:           d0 fe                           beq	0x0", thumbOptions);
		disassembleInst(0x00000004, "e7 fe", new JumpToAddress(0x00000004, true, false),
				"4:           e7 fe                           b	0x0", thumbOptions);
		disassembleInst(0x00000004, "f7 ff ff fe", new JumpToAddress(0x00000004, true, true),
				"4:           f7 ff ff fe                     bl	0", thumbOptions);
		disassembleInst(0x00000004, "f7 ff ef fe", new JumpToAddress(0x00000004, true, true),
				"4:           f7 ff ef fe                     blx	0", thumbOptions);
		disassembleInst(0x00000004, "47 80", new JumpToAddress("r0", true, true),
				"4:           47 80                           blx	r0", thumbOptions);
		disassembleInst(0x00000004, "46 f7", new JumpToAddress("lr", true, false),
				"4:           46 f7                           cpy	pc,lr", thumbOptions);
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
