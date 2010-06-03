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

import org.eclipse.cdt.debug.edc.internal.arm.disassembler.OpcodeARM;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for ARM disassembler opcode map.
 */
public class TestOpcodeARM extends Assert {
	
	private class OpcodeTest {

		private OpcodeARM.Index index;
		private int opcode;

		public OpcodeTest(OpcodeARM.Index index, int opcode) {
			this.index = index;
			this.opcode = opcode;
		}

		public OpcodeARM.Index getIndex() {
			return index;
		}

		public int getOpcode() {
			return opcode;
		}
	}

	/**
	 * Reference ARM opcode table.
	 */
	private OpcodeTest arm_test_opcode_table[] = {
			this.new OpcodeTest(OpcodeARM.Index.arm_ldrd, 0xE1C100D1),
			this.new OpcodeTest(OpcodeARM.Index.arm_ldrh, 0xE1D100B1),
			this.new OpcodeTest(OpcodeARM.Index.arm_ldrsb, 0xE1D100D1),
			this.new OpcodeTest(OpcodeARM.Index.arm_ldrsh, 0xE1D100F1),
			this.new OpcodeTest(OpcodeARM.Index.arm_strd, 0xE14F00F0),
			this.new OpcodeTest(OpcodeARM.Index.arm_strh, 0xE14F00B0),
			this.new OpcodeTest(OpcodeARM.Index.arm_adc, 0xE0A10002),
			this.new OpcodeTest(OpcodeARM.Index.arm_add, 0xE2810001),
			this.new OpcodeTest(OpcodeARM.Index.arm_and, 0xE0010002),
			this.new OpcodeTest(OpcodeARM.Index.arm_blx1, 0xFAFFFFFE),
			this.new OpcodeTest(OpcodeARM.Index.arm_b, 0xEAFFFFFE),
			this.new OpcodeTest(OpcodeARM.Index.arm_bl, 0xEBFFFFFE),
			this.new OpcodeTest(OpcodeARM.Index.arm_bic, 0xE1C10002),
			this.new OpcodeTest(OpcodeARM.Index.arm_bkpt, 0xE1200170),
			this.new OpcodeTest(OpcodeARM.Index.arm_blx2, 0xE12FFF30),
			this.new OpcodeTest(OpcodeARM.Index.arm_bx, 0xE12FFF10),
			this.new OpcodeTest(OpcodeARM.Index.arm_cdp, 0xEE110002),
			this.new OpcodeTest(OpcodeARM.Index.arm_cdp2, 0xFE110002),
			this.new OpcodeTest(OpcodeARM.Index.arm_clz, 0xE16F0F11),
			this.new OpcodeTest(OpcodeARM.Index.arm_cmn, 0xE3700000),
			this.new OpcodeTest(OpcodeARM.Index.arm_cmp, 0xE1500001),
			this.new OpcodeTest(OpcodeARM.Index.arm_eor, 0xE2210010),
			this.new OpcodeTest(OpcodeARM.Index.arm_ldm1, 0xE8900006),
			this.new OpcodeTest(OpcodeARM.Index.arm_ldm2, 0xE8D00006),
			this.new OpcodeTest(OpcodeARM.Index.arm_ldm3, 0xE8F00006),
			this.new OpcodeTest(OpcodeARM.Index.arm_ldr, 0xE51F0000),
			this.new OpcodeTest(OpcodeARM.Index.arm_ldrb, 0xE55F0000),
			this.new OpcodeTest(OpcodeARM.Index.arm_ldrbt, 0xE4F10001),
			this.new OpcodeTest(OpcodeARM.Index.arm_ldrt, 0xE4B10001),
			this.new OpcodeTest(OpcodeARM.Index.arm_mcr, 0xEE221013),
			this.new OpcodeTest(OpcodeARM.Index.arm_mcr2, 0xFE221013),
			this.new OpcodeTest(OpcodeARM.Index.arm_mcrr, 0xEC421013),
			this.new OpcodeTest(OpcodeARM.Index.arm_mcrr2, 0xFC421013),
			this.new OpcodeTest(OpcodeARM.Index.arm_mla, 0xE0203291),
			this.new OpcodeTest(OpcodeARM.Index.arm_mov, 0xE3A00010),
			this.new OpcodeTest(OpcodeARM.Index.arm_mrc, 0xEE321013),
			this.new OpcodeTest(OpcodeARM.Index.arm_mrc2, 0xFE321013),
			this.new OpcodeTest(OpcodeARM.Index.arm_mrrc, 0xEC521013),
			this.new OpcodeTest(OpcodeARM.Index.arm_mrrc2, 0xFC521013),
			this.new OpcodeTest(OpcodeARM.Index.arm_ldc, 0xED910000),
			this.new OpcodeTest(OpcodeARM.Index.arm_ldc2, 0xFD910000),
			this.new OpcodeTest(OpcodeARM.Index.arm_mrs, 0xE10F0000),
			this.new OpcodeTest(OpcodeARM.Index.arm_msr, 0xE329F001),
			this.new OpcodeTest(OpcodeARM.Index.arm_msr2, 0xE129F000),
			this.new OpcodeTest(OpcodeARM.Index.arm_mul, 0xE0000291),
			this.new OpcodeTest(OpcodeARM.Index.arm_mvn, 0xE1E00001),
			this.new OpcodeTest(OpcodeARM.Index.arm_orr, 0xE1810002),
			this.new OpcodeTest(OpcodeARM.Index.arm_pld, 0xF5D0F001),
			this.new OpcodeTest(OpcodeARM.Index.arm_qadd, 0xE1020051),
			this.new OpcodeTest(OpcodeARM.Index.arm_qdadd, 0xE1420051),
			this.new OpcodeTest(OpcodeARM.Index.arm_qdsub, 0xE1620051),
			this.new OpcodeTest(OpcodeARM.Index.arm_qsub, 0xE1220051),
			this.new OpcodeTest(OpcodeARM.Index.arm_rsb, 0xE0610002),
			this.new OpcodeTest(OpcodeARM.Index.arm_rsc, 0xE0E10002),
			this.new OpcodeTest(OpcodeARM.Index.arm_sbc, 0xE0C10002),
			this.new OpcodeTest(OpcodeARM.Index.arm_smla, 0xE1003281),
			this.new OpcodeTest(OpcodeARM.Index.arm_smlal, 0xE0E10392),
			this.new OpcodeTest(OpcodeARM.Index.arm_smlalxy, 0xE1410382),
			this.new OpcodeTest(OpcodeARM.Index.arm_smlaw, 0xE1203281),
			this.new OpcodeTest(OpcodeARM.Index.arm_smul, 0xE1600281),
			this.new OpcodeTest(OpcodeARM.Index.arm_smull, 0xE0C10392),
			this.new OpcodeTest(OpcodeARM.Index.arm_smulw, 0xE12002A1),
			this.new OpcodeTest(OpcodeARM.Index.arm_stc, 0xED811000),
			this.new OpcodeTest(OpcodeARM.Index.arm_stc2, 0xFD811000),
			this.new OpcodeTest(OpcodeARM.Index.arm_stm1, 0xE8A00006),
			this.new OpcodeTest(OpcodeARM.Index.arm_stm2, 0xE8C00006),
			this.new OpcodeTest(OpcodeARM.Index.arm_str, 0xE50F0000),
			this.new OpcodeTest(OpcodeARM.Index.arm_strb, 0xE54F0000),
			this.new OpcodeTest(OpcodeARM.Index.arm_strbt, 0xE4E10001),
			this.new OpcodeTest(OpcodeARM.Index.arm_strt, 0xE4A10001),
			this.new OpcodeTest(OpcodeARM.Index.arm_sub, 0xE0410002),
			this.new OpcodeTest(OpcodeARM.Index.arm_swi, 0xEF000000),
			this.new OpcodeTest(OpcodeARM.Index.arm_swp, 0xE1020091),
			this.new OpcodeTest(OpcodeARM.Index.arm_swpb, 0xE1420091),
			this.new OpcodeTest(OpcodeARM.Index.arm_teq, 0xE1300001),
			this.new OpcodeTest(OpcodeARM.Index.arm_tst, 0xE1100001),
			this.new OpcodeTest(OpcodeARM.Index.arm_umlal, 0xE0A10392),
			this.new OpcodeTest(OpcodeARM.Index.arm_umull, 0xE0810392),
			// TODO : add test cases for ARMv6 instructions
			// VFP instructions
			this.new OpcodeTest(OpcodeARM.Index.arm_fabsd, 0xEEB00BC1),
			this.new OpcodeTest(OpcodeARM.Index.arm_fabss, 0xEEB00AE0),
			this.new OpcodeTest(OpcodeARM.Index.arm_faddd, 0xEE310B02),
			this.new OpcodeTest(OpcodeARM.Index.arm_fadds, 0xEE300A81),
			this.new OpcodeTest(OpcodeARM.Index.arm_fcmpd, 0xEEB40B41),
			this.new OpcodeTest(OpcodeARM.Index.arm_fcmps, 0xEEB40A60),
			this.new OpcodeTest(OpcodeARM.Index.arm_fcmped, 0xEEB40BC1),
			this.new OpcodeTest(OpcodeARM.Index.arm_fcmpes, 0xEEB40AE0),
			this.new OpcodeTest(OpcodeARM.Index.arm_fcmpezd, 0xEEB50BC0),
			this.new OpcodeTest(OpcodeARM.Index.arm_fcmpezs, 0xEEB50AC0),
			this.new OpcodeTest(OpcodeARM.Index.arm_fcmpzd, 0xEEB50B40),
			this.new OpcodeTest(OpcodeARM.Index.arm_fcmpzs, 0xEEB50A40),
			this.new OpcodeTest(OpcodeARM.Index.arm_fcpyd, 0xEEB00B41),
			this.new OpcodeTest(OpcodeARM.Index.arm_fcpys, 0xEEB00A60),
			this.new OpcodeTest(OpcodeARM.Index.arm_fcvtds, 0xEEB70AE0),
			this.new OpcodeTest(OpcodeARM.Index.arm_fcvtsd, 0xEEB70BC1),
			this.new OpcodeTest(OpcodeARM.Index.arm_fdivd, 0xEE810B02),
			this.new OpcodeTest(OpcodeARM.Index.arm_fdivs, 0xEE800A81),
			this.new OpcodeTest(OpcodeARM.Index.arm_fldd, 0xED910B01),
			this.new OpcodeTest(OpcodeARM.Index.arm_flds, 0xED910A01),
			this.new OpcodeTest(OpcodeARM.Index.arm_fldmd, 0xEC901B06),
			this.new OpcodeTest(OpcodeARM.Index.arm_fldms, 0xECD00A03),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmacd, 0xEE010B02),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmacs, 0xEE000A81),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmdhr, 0xEE201B10),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmdlr, 0xEE001B10),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmdrr, 0xEC421B10),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmrdh, 0xEE310B10),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmrdl, 0xEE110B10),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmrrd, 0xEC510B12),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmrrs, 0xEC510A11),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmrs, 0xEE100A90),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmstat, 0x0EF1FA10),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmrx, 0xEEF10A10),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmscd, 0xEE110B02),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmscs, 0xEE100A81),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmsr, 0xEE001A10),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmsrr, 0xEC432A10),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmuld, 0xEE210B02),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmuls, 0xEE200A81),
			this.new OpcodeTest(OpcodeARM.Index.arm_fmxr, 0xEEE01A10),
			this.new OpcodeTest(OpcodeARM.Index.arm_fnegd, 0xEEB10B41),
			this.new OpcodeTest(OpcodeARM.Index.arm_fnegs, 0xEEB10A60),
			this.new OpcodeTest(OpcodeARM.Index.arm_fnmacd, 0xEE010B42),
			this.new OpcodeTest(OpcodeARM.Index.arm_fnmacs, 0xEE000AC1),
			this.new OpcodeTest(OpcodeARM.Index.arm_fnmscd, 0xEE110B42),
			this.new OpcodeTest(OpcodeARM.Index.arm_fnmscs, 0xEE100AC1),
			this.new OpcodeTest(OpcodeARM.Index.arm_fnmuld, 0xEE210B42),
			this.new OpcodeTest(OpcodeARM.Index.arm_fnmuls, 0xEE200AC1),
			this.new OpcodeTest(OpcodeARM.Index.arm_fsitod, 0xEEB80BE0),
			this.new OpcodeTest(OpcodeARM.Index.arm_fsitos, 0xEEB80AE0),
			this.new OpcodeTest(OpcodeARM.Index.arm_fsqrtd, 0xEEB10BC1),
			this.new OpcodeTest(OpcodeARM.Index.arm_fsqrts, 0xEEB10AE0),
			this.new OpcodeTest(OpcodeARM.Index.arm_fstd, 0xED010B01),
			this.new OpcodeTest(OpcodeARM.Index.arm_fsts, 0xED010A01),
			this.new OpcodeTest(OpcodeARM.Index.arm_fstmd, 0xEC801B06),
			this.new OpcodeTest(OpcodeARM.Index.arm_fstms, 0xECC00A03),
			this.new OpcodeTest(OpcodeARM.Index.arm_fsubd, 0xEE310B42),
			this.new OpcodeTest(OpcodeARM.Index.arm_fsubs, 0xEE300AC1),
			this.new OpcodeTest(OpcodeARM.Index.arm_ftosid, 0xEEBD0B41),
			this.new OpcodeTest(OpcodeARM.Index.arm_ftosis, 0xEEBD0A60),
			this.new OpcodeTest(OpcodeARM.Index.arm_ftouid, 0xEEBC0B41),
			this.new OpcodeTest(OpcodeARM.Index.arm_ftouis, 0xEEBC0A60),
			this.new OpcodeTest(OpcodeARM.Index.arm_fuitod, 0xEEB80B60),
			this.new OpcodeTest(OpcodeARM.Index.arm_fuitos, 0xEEB80A60), };

	/**
	 * Reference Thumb opcode table.
	 */
	private OpcodeTest thumb_test_opcode_table[] = { this.new OpcodeTest(OpcodeARM.Index.thumb_mov2, 0x1C08),
			this.new OpcodeTest(OpcodeARM.Index.thumb_swi, 0xDF00),
			this.new OpcodeTest(OpcodeARM.Index.thumb_adc, 0x4148),
			this.new OpcodeTest(OpcodeARM.Index.thumb_add1, 0x1D08),
			this.new OpcodeTest(OpcodeARM.Index.thumb_add2, 0x3004),
			this.new OpcodeTest(OpcodeARM.Index.thumb_add3, 0x1888),
			this.new OpcodeTest(OpcodeARM.Index.thumb_add4, 0x4448),
			this.new OpcodeTest(OpcodeARM.Index.thumb_add5, 0xA001),
			this.new OpcodeTest(OpcodeARM.Index.thumb_add6, 0xA801),
			this.new OpcodeTest(OpcodeARM.Index.thumb_add7, 0xB001),
			this.new OpcodeTest(OpcodeARM.Index.thumb_and, 0x4008),
			this.new OpcodeTest(OpcodeARM.Index.thumb_asr1, 0x1088),
			this.new OpcodeTest(OpcodeARM.Index.thumb_asr2, 0x4108),
			this.new OpcodeTest(OpcodeARM.Index.thumb_b1, 0xD0FE),
			this.new OpcodeTest(OpcodeARM.Index.thumb_b2, 0xE7FE),
			this.new OpcodeTest(OpcodeARM.Index.thumb_bic, 0x4388),
			this.new OpcodeTest(OpcodeARM.Index.thumb_bkpt, 0xBE01),
			this.new OpcodeTest(OpcodeARM.Index.thumb_blx1, 0xF7FF),
			this.new OpcodeTest(OpcodeARM.Index.thumb_blx2, 0x4780),
			this.new OpcodeTest(OpcodeARM.Index.thumb_bx, 0x4700),
			this.new OpcodeTest(OpcodeARM.Index.thumb_cmn, 0x42C8),
			this.new OpcodeTest(OpcodeARM.Index.thumb_cmp1, 0x2801),
			this.new OpcodeTest(OpcodeARM.Index.thumb_cmp2, 0x4288),
			this.new OpcodeTest(OpcodeARM.Index.thumb_cmp3, 0x4548),
			this.new OpcodeTest(OpcodeARM.Index.thumb_cps, 0xB667),
			this.new OpcodeTest(OpcodeARM.Index.thumb_cpy, 0x4648),
			this.new OpcodeTest(OpcodeARM.Index.thumb_eor, 0x4048),
			this.new OpcodeTest(OpcodeARM.Index.thumb_ldmia, 0xC80E),
			this.new OpcodeTest(OpcodeARM.Index.thumb_ldr1, 0x6848),
			this.new OpcodeTest(OpcodeARM.Index.thumb_ldr2, 0x5888),
			this.new OpcodeTest(OpcodeARM.Index.thumb_ldr3, 0x4801),
			this.new OpcodeTest(OpcodeARM.Index.thumb_ldr4, 0x9801),
			this.new OpcodeTest(OpcodeARM.Index.thumb_ldrb1, 0x7888),
			this.new OpcodeTest(OpcodeARM.Index.thumb_ldrb2, 0x5C88),
			this.new OpcodeTest(OpcodeARM.Index.thumb_ldrh1, 0x8848),
			this.new OpcodeTest(OpcodeARM.Index.thumb_ldrh2, 0x5A88),
			this.new OpcodeTest(OpcodeARM.Index.thumb_ldrsb, 0x5688),
			this.new OpcodeTest(OpcodeARM.Index.thumb_ldrsh, 0x5E88),
			this.new OpcodeTest(OpcodeARM.Index.thumb_lsl1, 0x0088),
			this.new OpcodeTest(OpcodeARM.Index.thumb_lsl2, 0x4088),
			this.new OpcodeTest(OpcodeARM.Index.thumb_lsr1, 0x0888),
			this.new OpcodeTest(OpcodeARM.Index.thumb_lsr2, 0x40C8),
			this.new OpcodeTest(OpcodeARM.Index.thumb_mov1, 0x2001),
			this.new OpcodeTest(OpcodeARM.Index.thumb_mul, 0x4348),
			this.new OpcodeTest(OpcodeARM.Index.thumb_mvn, 0x43C8),
			this.new OpcodeTest(OpcodeARM.Index.thumb_neg, 0x4248),
			this.new OpcodeTest(OpcodeARM.Index.thumb_orr, 0x4308),
			this.new OpcodeTest(OpcodeARM.Index.thumb_pop, 0xBC0E),
			this.new OpcodeTest(OpcodeARM.Index.thumb_push, 0xB40E),
			this.new OpcodeTest(OpcodeARM.Index.thumb_rev, 0xBA08),
			this.new OpcodeTest(OpcodeARM.Index.thumb_rev16, 0xBA48),
			this.new OpcodeTest(OpcodeARM.Index.thumb_revsh, 0xBAC8),
			this.new OpcodeTest(OpcodeARM.Index.thumb_ror, 0x41C8),
			this.new OpcodeTest(OpcodeARM.Index.thumb_sbc, 0x4188),
			this.new OpcodeTest(OpcodeARM.Index.thumb_setend, 0xB658),
			this.new OpcodeTest(OpcodeARM.Index.thumb_stmia, 0xC00E),
			this.new OpcodeTest(OpcodeARM.Index.thumb_str1, 0x6048),
			this.new OpcodeTest(OpcodeARM.Index.thumb_str2, 0x5088),
			this.new OpcodeTest(OpcodeARM.Index.thumb_str3, 0x9001),
			this.new OpcodeTest(OpcodeARM.Index.thumb_strb1, 0x7088),
			this.new OpcodeTest(OpcodeARM.Index.thumb_strb2, 0x5488),
			this.new OpcodeTest(OpcodeARM.Index.thumb_strh1, 0x8048),
			this.new OpcodeTest(OpcodeARM.Index.thumb_strh2, 0x5288),
			this.new OpcodeTest(OpcodeARM.Index.thumb_sub1, 0x1F08),
			this.new OpcodeTest(OpcodeARM.Index.thumb_sub2, 0x3804),
			this.new OpcodeTest(OpcodeARM.Index.thumb_sub3, 0x1A88),
			this.new OpcodeTest(OpcodeARM.Index.thumb_sub4, 0xB081),
			this.new OpcodeTest(OpcodeARM.Index.thumb_sxtb, 0xB248),
			this.new OpcodeTest(OpcodeARM.Index.thumb_sxth, 0xB208),
			this.new OpcodeTest(OpcodeARM.Index.thumb_tst, 0x4208),
			this.new OpcodeTest(OpcodeARM.Index.thumb_uxtb, 0xB2C8),
			this.new OpcodeTest(OpcodeARM.Index.thumb_uxth, 0xB288), };

	private OpcodeARM opcode_arm;

	@Before
	public void setUp() throws Exception {
		opcode_arm = new OpcodeARM(OpcodeARM.Index.arm_bx, "bx", "xxxx000100101111111111110001xxxx");
	}

	@After
	public void tearDown() throws Exception {
		opcode_arm = null;
	}

	@Test
	public void testOpcodeARM() {
		for (OpcodeARM opcode : OpcodeARM.arm_opcode_table) {
			String mnemonic = opcode.getMnemonic();
			assertTrue(mnemonic != null);
			assertTrue(mnemonic.length() > 0);
			String pattern = opcode.getOpcodePattern();
			assertTrue(pattern != null);
			assertTrue(pattern.length() > 0);
		}
	}

	@Test
	public void testGetMnemonic() {
		String mnemonic = opcode_arm.getMnemonic();
		assertTrue(mnemonic != null);
		assertTrue(mnemonic.equals("bx"));
	}

	@Test
	public void testGetOpcodeMask() {
		String pattern = opcode_arm.getOpcodePattern();
		int mask = getMask(pattern);
		assertTrue(mask == opcode_arm.getOpcodeMask());
	}

	@Test
	public void testGetOpcodePattern() {
		String pattern = opcode_arm.getOpcodePattern();
		assertTrue(pattern != null);
		assertTrue(pattern.equals("xxxx000100101111111111110001xxxx"));
	}

	@Test
	public void testGetOpcodeResult() {
		String pattern = opcode_arm.getOpcodePattern();
		int result = getResult(pattern);
		assertTrue(result == opcode_arm.getOpcodeResult());
	}

	@Test
	public void testARMOpcodeTable() {
		assertTrue(OpcodeARM.arm_opcode_table.length >= arm_test_opcode_table.length);
		for (OpcodeTest opcodeTest : arm_test_opcode_table) {
			OpcodeARM opcodeARM = null;
			for (OpcodeARM element : OpcodeARM.arm_opcode_table) {
				if (element.getIndex().equals(opcodeTest.getIndex())) {
					opcodeARM = element;
				}
			}
			assertNotNull(opcodeARM);
			int result = opcodeTest.getOpcode() & opcodeARM.getOpcodeMask();
			assertTrue(result == opcodeARM.getOpcodeResult());
		}
	}

	@Test
	public void testThumbOpcodeTable() {
		assertTrue(OpcodeARM.thumb_opcode_table.length >= thumb_test_opcode_table.length);
		for (OpcodeTest opcodeTest : thumb_test_opcode_table) {
			OpcodeARM opcodeThumb = null;
			for (OpcodeARM element : OpcodeARM.thumb_opcode_table) {
				if (element.getIndex().equals(opcodeTest.getIndex())) {
					opcodeThumb = element;
				}
			}
			assertNotNull(opcodeThumb);
			int result = opcodeTest.getOpcode() & opcodeThumb.getOpcodeMask();
			assertTrue(result == opcodeThumb.getOpcodeResult());
		}
	}

	private int getMask(String pattern) {
		char[] sig = pattern.toCharArray();
		int size = pattern.length();
		int mask = 0;
		for (int i = 0; i < size; i++) {
			mask = mask << 1;
			switch (sig[i]) {
			case '0':
				mask |= 1;
				break;

			case '1':
				mask |= 1;
				break;

			default:
				break;
			}
		}
		return mask;
	}

	private int getResult(String pattern) {
		char[] sig = pattern.toCharArray();
		int size = pattern.length();
		int result = 0;
		for (int i = 0; i < size; i++) {
			result = result << 1;
			switch (sig[i]) {
			case '0':
				result |= 0;
				break;

			case '1':
				result |= 1;
				break;

			default:
				break;
			}
		}
		return result;
	}

}
