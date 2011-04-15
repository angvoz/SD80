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
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrd__imm,		0xE1C100D1),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrd__lit,		0xE1CF00D1),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrd__reg,		0xE18100D1),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrh__imm,		0xE1D100B1),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrh__lit,		0xE1DF00B1),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrh__reg,		0xE19100B1),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrsb__imm,		0xE1D100D1),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrsb__lit,		0xE1DF00D1),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrsb__reg,		0xE19100D1),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrsh__imm,		0xE1D100F1),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrsh__lit,		0xE1DF00F1),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrsh__reg,		0xE19100F1),
		this.new OpcodeTest(OpcodeARM.Index.arm_strd__imm,		0xE14F00F0),
		this.new OpcodeTest(OpcodeARM.Index.arm_strd__reg,		0xE10F00F0),
		this.new OpcodeTest(OpcodeARM.Index.arm_strh__imm,		0xE14F00B0),
		this.new OpcodeTest(OpcodeARM.Index.arm_strh__reg,		0xE10F00B0),
		this.new OpcodeTest(OpcodeARM.Index.arm_adc__imm,		0xE2A10002),
		this.new OpcodeTest(OpcodeARM.Index.arm_adc__reg,		0xE0A10002),
		this.new OpcodeTest(OpcodeARM.Index.arm_adc__rsr,		0xE0A10012),
		this.new OpcodeTest(OpcodeARM.Index.arm_add__imm,		0xE2810001),
		this.new OpcodeTest(OpcodeARM.Index.arm_add__reg,		0xE0810001),
		this.new OpcodeTest(OpcodeARM.Index.arm_add__rsr,		0xE0810011),
//		this.new OpcodeTest(OpcodeARM.Index.arm_add__sp_imm,	0xE28D0001),
//		this.new OpcodeTest(OpcodeARM.Index.arm_add__sp_reg,	0xE08D0001),
		this.new OpcodeTest(OpcodeARM.Index.arm_adr__higher,	0xE28F0001),
		this.new OpcodeTest(OpcodeARM.Index.arm_adr__lower,		0xE24F0001),
		this.new OpcodeTest(OpcodeARM.Index.arm_and__imm,		0xE2010002),
		this.new OpcodeTest(OpcodeARM.Index.arm_and__reg,		0xE0010002),
		this.new OpcodeTest(OpcodeARM.Index.arm_and__rsr,		0xE0010012),
		this.new OpcodeTest(OpcodeARM.Index.arm_blx__imm,		0xFAFFFFFE),
		this.new OpcodeTest(OpcodeARM.Index.arm_blx__reg,		0xE12FFF30),
		this.new OpcodeTest(OpcodeARM.Index.arm_b,				0xEAFFFFFE),
		this.new OpcodeTest(OpcodeARM.Index.arm_bl,				0xEBFFFFFE),
		this.new OpcodeTest(OpcodeARM.Index.arm_bic__imm,		0xE3C10002),
		this.new OpcodeTest(OpcodeARM.Index.arm_bic__reg,		0xE1C10002),
		this.new OpcodeTest(OpcodeARM.Index.arm_bic__rsr,		0xE1C10012),
		this.new OpcodeTest(OpcodeARM.Index.arm_bkpt,			0xE1200170),
		this.new OpcodeTest(OpcodeARM.Index.arm_bx,				0xE12FFF10),
		this.new OpcodeTest(OpcodeARM.Index.arm_cdp,			0xEE110002),
		this.new OpcodeTest(OpcodeARM.Index.arm_cdp2,			0xFE110002),
		this.new OpcodeTest(OpcodeARM.Index.arm_clz,			0xE16F0F11),
		this.new OpcodeTest(OpcodeARM.Index.arm_cmn__imm,		0xE3700000),
		this.new OpcodeTest(OpcodeARM.Index.arm_cmn__reg,		0xE1700000),
		this.new OpcodeTest(OpcodeARM.Index.arm_cmn__rsr,		0xE1700010),
		this.new OpcodeTest(OpcodeARM.Index.arm_cmp__imm,		0xE3500001),
		this.new OpcodeTest(OpcodeARM.Index.arm_cmp__reg,		0xE1500001),
		this.new OpcodeTest(OpcodeARM.Index.arm_cmp__rsr,		0xE1500011),
		this.new OpcodeTest(OpcodeARM.Index.arm_eor__imm,		0xE2210010),
		this.new OpcodeTest(OpcodeARM.Index.arm_eor__reg,		0xE0210000),
		this.new OpcodeTest(OpcodeARM.Index.arm_eor__rsr,		0xE0210010),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldm,			0xE8900006),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldm__exc_ret,	0xE8F08006),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldm__user_reg,	0xE8D00006),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldr__lit,		0xE51F0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldr__imm,		0xE51E0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldr__reg,		0xE61F0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrb__lit,		0xE55F0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrb__imm,		0xE55E0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrb__reg,		0xE65F0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrbt__imm,		0xE4F10001),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrbt__reg,		0xE6F10001),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrt__imm,		0xE4B10001),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldrt__reg,		0xE6B10001),
		this.new OpcodeTest(OpcodeARM.Index.arm_mcr,			0xEE221013),
		this.new OpcodeTest(OpcodeARM.Index.arm_mcr2,			0xFE221013),
		this.new OpcodeTest(OpcodeARM.Index.arm_mcrr,			0xEC421013),
		this.new OpcodeTest(OpcodeARM.Index.arm_mcrr2,			0xFC421013),
		this.new OpcodeTest(OpcodeARM.Index.arm_mla,			0xE0203291),
		this.new OpcodeTest(OpcodeARM.Index.arm_mov__imm,		0xE3A00010),
		this.new OpcodeTest(OpcodeARM.Index.arm_mov__reg,		0xE1A00000),
		this.new OpcodeTest(OpcodeARM.Index.arm_movw,			0xE3000010),
		this.new OpcodeTest(OpcodeARM.Index.arm_mrc,			0xEE321013),
		this.new OpcodeTest(OpcodeARM.Index.arm_mrc2,			0xFE321013),
		this.new OpcodeTest(OpcodeARM.Index.arm_mrrc,			0xEC521013),
		this.new OpcodeTest(OpcodeARM.Index.arm_mrrc2,			0xFC521013),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldc__imm,		0xED910000),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldc2__imm,		0xFD910000),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldc__lit,		0xED9F0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_ldc2__lit,		0xFD9F0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_mrs,			0xE10F0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_msr__imm,		0xE328F001),
		this.new OpcodeTest(OpcodeARM.Index.arm_msr__reg,		0xE128F000),
		this.new OpcodeTest(OpcodeARM.Index.arm_msr__sys_imm,	0xE368F001),
		this.new OpcodeTest(OpcodeARM.Index.arm_msr__sys_reg,	0xE168F000),
		this.new OpcodeTest(OpcodeARM.Index.arm_mul,			0xE0000291),
		this.new OpcodeTest(OpcodeARM.Index.arm_mvn__imm,		0xE3E00001),
		this.new OpcodeTest(OpcodeARM.Index.arm_mvn__reg,		0xE1E00001),
		this.new OpcodeTest(OpcodeARM.Index.arm_mvn__rsr,		0xE1E00011),
		this.new OpcodeTest(OpcodeARM.Index.arm_orr__imm,		0xE3810002),
		this.new OpcodeTest(OpcodeARM.Index.arm_orr__reg,		0xE1810002),
		this.new OpcodeTest(OpcodeARM.Index.arm_orr__rsr,		0xE1810012),
		this.new OpcodeTest(OpcodeARM.Index.arm_pld__lit,		0xF5DFF001),
		this.new OpcodeTest(OpcodeARM.Index.arm_pld__imm,		0xF5D0F001),
		this.new OpcodeTest(OpcodeARM.Index.arm_pld__reg,		0xF7D0F001),
		this.new OpcodeTest(OpcodeARM.Index.arm_qadd,			0xE1020051),
		this.new OpcodeTest(OpcodeARM.Index.arm_qdadd,			0xE1420051),
		this.new OpcodeTest(OpcodeARM.Index.arm_qdsub,			0xE1620051),
		this.new OpcodeTest(OpcodeARM.Index.arm_qsub,			0xE1220051),
		this.new OpcodeTest(OpcodeARM.Index.arm_rsb__imm,		0xE2610002),
		this.new OpcodeTest(OpcodeARM.Index.arm_rsb__reg,		0xE0610002),
		this.new OpcodeTest(OpcodeARM.Index.arm_rsb__rsr,		0xE0610012),
		this.new OpcodeTest(OpcodeARM.Index.arm_rsc__imm,		0xE2E10002),
		this.new OpcodeTest(OpcodeARM.Index.arm_rsc__reg,		0xE0E10002),
		this.new OpcodeTest(OpcodeARM.Index.arm_rsc__rsr,		0xE0E10012),
		this.new OpcodeTest(OpcodeARM.Index.arm_sbc__imm,		0xE2C10002),
		this.new OpcodeTest(OpcodeARM.Index.arm_sbc__reg,		0xE0C10002),
		this.new OpcodeTest(OpcodeARM.Index.arm_sbc__rsr,		0xE0C10012),
		this.new OpcodeTest(OpcodeARM.Index.arm_smla,			0xE1003281),
		this.new OpcodeTest(OpcodeARM.Index.arm_smlal,			0xE0E10392),
		this.new OpcodeTest(OpcodeARM.Index.arm_smlalxy,		0xE1410382),
		this.new OpcodeTest(OpcodeARM.Index.arm_smlaw,			0xE1203281),
		this.new OpcodeTest(OpcodeARM.Index.arm_smul,			0xE1600281),
		this.new OpcodeTest(OpcodeARM.Index.arm_smull,			0xE0C10392),
		this.new OpcodeTest(OpcodeARM.Index.arm_smulw,			0xE12002A1),
		this.new OpcodeTest(OpcodeARM.Index.arm_stc,			0xED811000),
		this.new OpcodeTest(OpcodeARM.Index.arm_stc2,			0xFD811000),
		this.new OpcodeTest(OpcodeARM.Index.arm_stm__regs,		0xE8A00006),
		this.new OpcodeTest(OpcodeARM.Index.arm_stm__usr_regs,	0xE8C00006),
		this.new OpcodeTest(OpcodeARM.Index.arm_str__imm,		0xE50F0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_str__reg,		0xE70F0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_strb__imm,		0xE54F0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_strb__reg,		0xE74F0000),
		this.new OpcodeTest(OpcodeARM.Index.arm_strbt__imm,		0xE4E10001),
		this.new OpcodeTest(OpcodeARM.Index.arm_strbt__reg,		0xE6E10001),
		this.new OpcodeTest(OpcodeARM.Index.arm_strt__imm,		0xE4A10001),
		this.new OpcodeTest(OpcodeARM.Index.arm_strt__reg,		0xE6A10001),
		this.new OpcodeTest(OpcodeARM.Index.arm_sub__imm,		0xE2410002),
		this.new OpcodeTest(OpcodeARM.Index.arm_sub__reg,		0xE0410002),
		this.new OpcodeTest(OpcodeARM.Index.arm_sub__rsr,		0xE0410012),
		this.new OpcodeTest(OpcodeARM.Index.arm_svc,			0xEF000000),
		this.new OpcodeTest(OpcodeARM.Index.arm_swp,			0xE1020091),
		this.new OpcodeTest(OpcodeARM.Index.arm_teq__imm,		0xE3300001),
		this.new OpcodeTest(OpcodeARM.Index.arm_teq__reg,		0xE1300001),
		this.new OpcodeTest(OpcodeARM.Index.arm_tst__imm,		0xE3100001),
		this.new OpcodeTest(OpcodeARM.Index.arm_tst__reg,		0xE1100001),
		this.new OpcodeTest(OpcodeARM.Index.arm_umlal,			0xE0A10392),
		this.new OpcodeTest(OpcodeARM.Index.arm_umull,			0xE0810392),
		// TODO : add test cases for ARMv6 instructions
		// VFP instructions
//		this.new OpcodeTest(OpcodeARM.Index.arm_fabsd,			0xEEB00BC1),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fabss,			0xEEB00AE0),
//		this.new OpcodeTest(OpcodeARM.Index.arm_faddd,			0xEE310B02),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fadds,			0xEE300A81),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fcmpd,			0xEEB40B41),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fcmps,			0xEEB40A60),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fcmped,			0xEEB40BC1),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fcmpes,			0xEEB40AE0),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fcmpezd,		0xEEB50BC0),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fcmpezs,		0xEEB50AC0),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fcmpzd,			0xEEB50B40),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fcmpzs,			0xEEB50A40),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fcpyd,			0xEEB00B41),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fcpys,			0xEEB00A60),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fcvtds,			0xEEB70AE0),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fcvtsd,			0xEEB70BC1),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fdivd,			0xEE810B02),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fdivs,			0xEE800A81),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fldd,			0xED910B01),
//		this.new OpcodeTest(OpcodeARM.Index.arm_flds,			0xED910A01),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fldmd,			0xEC901B06),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fldms,			0xECD00A03),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmacd,			0xEE010B02),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmacs,			0xEE000A81),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmdhr,			0xEE201B10),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmdlr,			0xEE001B10),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmdrr,			0xEC421B10),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmrdh,			0xEE310B10),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmrdl,			0xEE110B10),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmrrd,			0xEC510B12),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmrrs,			0xEC510A11),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmrs,			0xEE100A90),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmstat,			0x0EF1FA10),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmrx,			0xEEF10A10),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmscd,			0xEE110B02),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmscs,			0xEE100A81),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmsr,			0xEE001A10),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmsrr,			0xEC432A10),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmuld,			0xEE210B02),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmuls,			0xEE200A81),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fmxr,			0xEEE01A10),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fnegd,			0xEEB10B41),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fnegs,			0xEEB10A60),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fnmacd,			0xEE010B42),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fnmacs,			0xEE000AC1),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fnmscd,			0xEE110B42),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fnmscs,			0xEE100AC1),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fnmuld,			0xEE210B42),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fnmuls,			0xEE200AC1),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fsitod,			0xEEB80BE0),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fsitos,			0xEEB80AE0),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fsqrtd,			0xEEB10BC1),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fsqrts,			0xEEB10AE0),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fstd,			0xED010B01),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fsts,			0xED010A01),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fstmd,			0xEC801B06),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fstms,			0xECC00A03),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fsubd,			0xEE310B42),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fsubs,			0xEE300AC1),
//		this.new OpcodeTest(OpcodeARM.Index.arm_ftosid,			0xEEBD0B41),
//		this.new OpcodeTest(OpcodeARM.Index.arm_ftosis,			0xEEBD0A60),
//		this.new OpcodeTest(OpcodeARM.Index.arm_ftouid,			0xEEBC0B41),
//		this.new OpcodeTest(OpcodeARM.Index.arm_ftouis,			0xEEBC0A60),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fuitod,			0xEEB80B60),
//		this.new OpcodeTest(OpcodeARM.Index.arm_fuitos,			0xEEB80A60),
		};

	/**
	 * Reference Thumb opcode table.
	 */
	private OpcodeTest thumb_test_opcode_table[] = {
		this.new OpcodeTest(OpcodeARM.Index.thumb_svc,				0xDF00),
		this.new OpcodeTest(OpcodeARM.Index.thumb_adc,				0x4148),
		this.new OpcodeTest(OpcodeARM.Index.thumb_add__reg_imm,		0x1D08),
		this.new OpcodeTest(OpcodeARM.Index.thumb_add__imm, 		0x3004),
		this.new OpcodeTest(OpcodeARM.Index.thumb_add__reg_reg,		0x1888),
		this.new OpcodeTest(OpcodeARM.Index.thumb_add__reg,			0x4448),
		this.new OpcodeTest(OpcodeARM.Index.thumb_add__sp_imm,		0xA801),
		this.new OpcodeTest(OpcodeARM.Index.thumb_add__imm_to_sp,	0xB001),
		this.new OpcodeTest(OpcodeARM.Index.thumb_and,				0x4008),
		this.new OpcodeTest(OpcodeARM.Index.thumb_asr__imm,			0x1088),
		this.new OpcodeTest(OpcodeARM.Index.thumb_asr__reg,			0x4108),
		this.new OpcodeTest(OpcodeARM.Index.thumb_b_1,				0xD0FE),
		this.new OpcodeTest(OpcodeARM.Index.thumb_b_2,				0xE7FE),
		this.new OpcodeTest(OpcodeARM.Index.thumb_bic,				0x4388),
		this.new OpcodeTest(OpcodeARM.Index.thumb_bkpt,				0xBE01),
		this.new OpcodeTest(OpcodeARM.Index.thumb_blx,				0x4780),
		this.new OpcodeTest(OpcodeARM.Index.thumb_undefined,		0xDE01),
		this.new OpcodeTest(OpcodeARM.Index.thumb_bx,				0x4700),
		this.new OpcodeTest(OpcodeARM.Index.thumb_cmn,				0x42C8),
		this.new OpcodeTest(OpcodeARM.Index.thumb_cmp__imm,			0x2801),
		this.new OpcodeTest(OpcodeARM.Index.thumb_cmp__reg,			0x4288),
		this.new OpcodeTest(OpcodeARM.Index.thumb_cmp__reg_hi,		0x4548),
		this.new OpcodeTest(OpcodeARM.Index.thumb_cps,				0xB667),
		this.new OpcodeTest(OpcodeARM.Index.thumb_eor,				0x4048),
		this.new OpcodeTest(OpcodeARM.Index.thumb_ldm,				0xC80E),
		this.new OpcodeTest(OpcodeARM.Index.thumb_ldr__imm,			0x6848),
		this.new OpcodeTest(OpcodeARM.Index.thumb_ldr__imm_sp,		0x9888),
		this.new OpcodeTest(OpcodeARM.Index.thumb_ldr__lit,			0x4801),
		this.new OpcodeTest(OpcodeARM.Index.thumb_ldr__reg,			0x5801),
		this.new OpcodeTest(OpcodeARM.Index.thumb_ldrb__imm,		0x7888),
		this.new OpcodeTest(OpcodeARM.Index.thumb_ldrb__reg,		0x5C88),
		this.new OpcodeTest(OpcodeARM.Index.thumb_ldrh__imm,		0x8848),
		this.new OpcodeTest(OpcodeARM.Index.thumb_ldrh__reg,		0x5A88),
		this.new OpcodeTest(OpcodeARM.Index.thumb_ldrsb,			0x5688),
		this.new OpcodeTest(OpcodeARM.Index.thumb_ldrsh,			0x5E88),
		this.new OpcodeTest(OpcodeARM.Index.thumb_lsl__imm,			0x0088),
		this.new OpcodeTest(OpcodeARM.Index.thumb_lsl__reg,			0x4088),
		this.new OpcodeTest(OpcodeARM.Index.thumb_lsr__imm,			0x0888),
		this.new OpcodeTest(OpcodeARM.Index.thumb_lsr__reg,			0x40C8),
		this.new OpcodeTest(OpcodeARM.Index.thumb_mov__reg,			0x4648),
		this.new OpcodeTest(OpcodeARM.Index.thumb_mov__imm,			0x2001),
		this.new OpcodeTest(OpcodeARM.Index.thumb_movs,				0x0001),
		this.new OpcodeTest(OpcodeARM.Index.thumb_mul,				0x4348),
		this.new OpcodeTest(OpcodeARM.Index.thumb_mvn,				0x43C8),
		this.new OpcodeTest(OpcodeARM.Index.thumb_orr,				0x4308),
		this.new OpcodeTest(OpcodeARM.Index.thumb_pop,				0xBC0E),
		this.new OpcodeTest(OpcodeARM.Index.thumb_push,				0xB40E),
		this.new OpcodeTest(OpcodeARM.Index.thumb_rev,				0xBA08),
		this.new OpcodeTest(OpcodeARM.Index.thumb_rev16,			0xBA48),
		this.new OpcodeTest(OpcodeARM.Index.thumb_revsh,			0xBAC8),
		this.new OpcodeTest(OpcodeARM.Index.thumb_ror,				0x41C8),
		this.new OpcodeTest(OpcodeARM.Index.thumb_rsb,				0x4248),
		this.new OpcodeTest(OpcodeARM.Index.thumb_sbc,				0x4188),
		this.new OpcodeTest(OpcodeARM.Index.thumb_setend,			0xB658),
		this.new OpcodeTest(OpcodeARM.Index.thumb_stm,				0xC00E),
		this.new OpcodeTest(OpcodeARM.Index.thumb_str__imm,			0x6048),
		this.new OpcodeTest(OpcodeARM.Index.thumb_str__imm_sp,		0x9088),
		this.new OpcodeTest(OpcodeARM.Index.thumb_str__reg,			0x5001),
		this.new OpcodeTest(OpcodeARM.Index.thumb_strb__imm,		0x7088),
		this.new OpcodeTest(OpcodeARM.Index.thumb_strb__reg,		0x5488),
		this.new OpcodeTest(OpcodeARM.Index.thumb_strh__imm,		0x8048),
		this.new OpcodeTest(OpcodeARM.Index.thumb_strh__reg,		0x5288),
		this.new OpcodeTest(OpcodeARM.Index.thumb_sub__reg_imm,		0x1F08),
		this.new OpcodeTest(OpcodeARM.Index.thumb_sub__imm,			0x3804),
		this.new OpcodeTest(OpcodeARM.Index.thumb_sub__reg_reg,		0x1A88),
		this.new OpcodeTest(OpcodeARM.Index.thumb_sub__imm_from_sp,	0xB081),
		this.new OpcodeTest(OpcodeARM.Index.thumb_sxtb,				0xB248),
		this.new OpcodeTest(OpcodeARM.Index.thumb_sxth,				0xB208),
		this.new OpcodeTest(OpcodeARM.Index.thumb_tst,				0x4208),
		this.new OpcodeTest(OpcodeARM.Index.thumb_uxtb,				0xB2C8),
		this.new OpcodeTest(OpcodeARM.Index.thumb_uxth,				0xB288),
	};                                                              	

	/**
	 * Reference Thumb opcode table.
	 */
	private OpcodeTest thumb2_test_opcode_table[] = {
			/// getting the following error when compiling this table with
			/// no elements commented out:
			///
			///	"The code of constructor TestOpcodeARM() is exceeding the 65535 bytes limit"
			///
			/// ... so, until this can be broken into separate test classes,
			/// the following are commented out
			/// 1) all instruction sequences that, while strictly legal,
			///    may result in what the reference manual deems
			///    UNPREDICTABLE results (with the exception of the
			///    first few examples immediately below).
			/// 2) all but the first and last instructions sequences of ranges
			///    greater than 0xFFF1F200 for which there are other instruction sequences
			///    corresponding to the same mnemonic enumeral from OpcodeARM.java

		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE8000000), // ____ ____ __x_ ::.: ::.. .... ...x xxxx UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE80DBFFF), // ____ ____ __x_ ::.: ::.. .... ...x xxxx UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE80DC000), // ____ ____ __x_ ::.: ::.. .... ...x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE80DC01F), // ____ ____ __x_ ::.: ::.. .... ...x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE80DC020), // ____ ____ __x_ ::.: ::.. .... ...x xxxx UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE80FFFFF), // ____ ____ __x_ ::.: ::.. .... ...x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8100000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE810BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE810C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE810C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE811FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8110000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE811BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE811C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE811C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE811FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8120000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE812BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE812C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE812C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE812FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8130000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE813BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE813C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE813C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE813FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8140000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE814BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE814C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE814C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE814FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8150000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE815BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE815C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE815C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE815FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8160000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE816BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE816C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE816C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE816FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8170000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE817BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE817C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE817C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE817FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8180000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE818BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE818C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE818C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE818FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8190000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE819BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE819C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE819C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE819FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81A0000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81ABFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81AC000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81AC001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81AFFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81B0000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81BBFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81BC000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81BC001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81BFFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81C0000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81CBFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81CC000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81CC001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81CFFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81D0000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81DBFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81DC000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81DC001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81DFFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81E0000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81EBFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81EC000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81EC001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81EFFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81F0000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81FBFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81FC000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81FC001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE81FFFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE8200000), // ____ ____ __x_ ::.: ::.. .... ...x xxxx UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE82DBFFF), // ____ ____ __x_ ::.: ::.. .... ...x xxxx UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE82DC000), // ____ ____ __x_ ::.: ::.. .... ...x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE82DC01F), // ____ ____ __x_ ::.: ::.. .... ...x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE82DC020), // ____ ____ __x_ ::.: ::.. .... ...x xxxx UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE82FFFFF), // ____ ____ __x_ ::.: ::.. .... ...x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8300000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE830BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE830C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE830C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE831FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8310000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE831BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE831C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE831C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE831FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8320000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE832BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE832C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE832C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE832FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8330000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE833BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE833C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE833C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE833FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8340000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE834BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE834C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE834C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE834FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8350000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE835BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE835C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE835C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE835FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8360000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE836BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE836C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE836C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE836FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8370000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE837BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE837C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE837C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE837FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8380000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE838BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE838C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE838C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE838FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE8390000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE839BFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE839C000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE839C001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE839FFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83A0000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83ABFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83AC000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83AC001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83AFFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83B0000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83BBFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83BC000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83BC001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83BFFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83C0000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83CBFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83CC000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83CC001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83CFFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83D0000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83DBFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83DC000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83DC001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83DFFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83E0000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83EBFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83EC000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83EC001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83EFFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83F0000), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83FBFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83FC000), // ____ ____ __x_ xxxx ::.. .... .... ....
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83FC001), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE83FFFFF), // ____ ____ __x_ xxxx ::.. .... .... ....	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strex,				0xE8400000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strex,				0xE84FFFFF), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldrd__imm,			0xE8500000), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldrex,				0xE8500F00), // ____ ____ ____ xxxx xxxx :::: xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldrd__lit,			0xE85F0000), // ____ ___x x_._ ____ xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE8600000), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE86FFFFF), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_stm,					0xE8800000), // ____ ____ __x_ xxxx .x.x xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldm,					0xE8900000), // ____ ____ __x_ xxxx xx.x xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_push__regs,			0xE8AD0000), // ____ ____ ____ ____ .x.x xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pop__regs,			0xE8BD0000), // ____ ____ ____ ____ xx.x xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE8C00000), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE8CFFFFF), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strexx,				0xE8C00070), // ____ ____ ____ xxxx xxxx xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strexx,				0xE8C00F40), // ____ ____ ____ xxxx xxxx :::: ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strexx,				0xE8C00F50), // ____ ____ ____ xxxx xxxx :::: ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldrexx,				0xE8D0005F), // ____ ____ ____ xxxx xxxx :::: ____ ::::
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldrexx,				0xE8D0007F), // ____ ____ ____ xxxx xxxx xxxx ____ ::::
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldrexx,				0xE8D00F4F), // ____ ____ ____ xxxx xxxx :::: ____ ::::
		this.new OpcodeTest(OpcodeARM.Index.thumb2_tb,					0xE8D0F000), // ____ ____ ____ xxxx :::: .... ___x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE8E00000), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE8EFFFFF), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_stmdb,				0xE9000000), // ____ ____ __x_ xxxx .x.x xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldmdb,				0xE9100000), // ____ ____ __x_ xxxx xx.x xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE9400000), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE94FFFFF), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE9600000), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE96FFFFF), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_srs,					0xE98DC000), // ____ ____ __x_ ::.: ::.. .... ...x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rfe,					0xE990C000), // ____ ____ __x_ xxxx ::.. .... .... ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE9C00000), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE9CFFFFF), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE9E00000), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_strd,				0xE9EFFFFF), // ____ ___x x_x_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_and__reg,			0xEA000000), // ____ ____ ___x xxxx .xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_tst__reg,			0xEA100F00), // ____ ____ ____ xxxx .xxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_bic__reg,			0xEA200000), // ____ ____ ___x xxxx .xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_orr__reg,			0xEA400000), // ____ ____ ___x xxxx .xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_lsl__imm,			0xEA4F0000), // ____ ____ ___x ____ .xxx xxxx xx__ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mov__reg,			0xEA4F0000), // ____ ____ ___x ____ .___ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_lsr__imm,			0xEA4F0010), // ____ ____ ___x ____ .xxx xxxx xx__ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_asr__imm,			0xEA4F0020), // ____ ____ ___x ____ .xxx xxxx xx__ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ror__imm,			0xEA4F0030), // ____ ____ ___x ____ .xxx xxxx xx__ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rrx,					0xEA4F0030), // ____ ____ ___x ____ .___ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_orn__reg,			0xEA600000), // ____ ____ ___x xxxx .xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mvn__reg,			0xEA6F0000), // ____ ____ ___x ____ .xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_eor__reg,			0xEA800000), // ____ ____ ___x xxxx .xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_teq__reg,			0xEA900F00), // ____ ____ ____ xxxx .xxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pkh,					0xEAC00000), // ____ ____ ____ xxxx .xxx xxxx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_add__reg,			0xEB000000), // ____ ____ ___x xxxx .xxx xxxx xxxx xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_add__sp_reg,			0xEB0D0000), // ____ ____ ___x ____ _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_add__reg,			0xEB0D0000), // ____ ____ ___x ____ _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_cmn__reg,			0xEB100F00), // ____ ____ ____ xxxx .xxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_adc__reg,			0xEB400000), // ____ ____ ___x xxxx .xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sbc__reg,			0xEB600000), // ____ ____ ___x xxxx .xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sub__reg,			0xEBA00000), // ____ ____ ___x xxxx .xxx xxxx xxxx xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_sub__sp_reg,			0xEBAD0000), // ____ ____ ___x ____ .xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sub__reg,			0xEBAD0000), // ____ ____ ___x ____ .xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_cmp__reg,			0xEBB00F00), // ____ ____ ____ xxxx .xxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rsb__reg,			0xEBC00000), // ____ ____ ___x xxxx .xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_stc,					0xEC000000), // ____ ___x xxx_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vstm__32,			0xEC000A00), // ____ ___x xxx_ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vstm__64,			0xEC000B00), // ____ ___x xxx_ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vldm__32,			0xEC100A00), // ____ ___x xxx_ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vldm__64,			0xEC100B00), // ____ ___x xxx_ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldc,					0xEC100000), // ____ ___x xxx_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldc,					0xEC1F0000), // ____ ___x xxx_ ____ xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mcrr,				0xEC400000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov_8,				0xEC400A10), // ____ ____ ___x xxxx xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov_9,				0xEC400B10), // ____ ____ ___x xxxx xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mrrc,				0xEC500000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpop,				0xECBD0A00), // ____ ____ _x__ ____ xxxx ___x xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpop,				0xECBD0B00), // ____ ____ _x__ ____ xxxx ___x xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vstr__32,			0xED000A00), // ____ ____ xx__ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vstr__64,			0xED000B00), // ____ ____ xx__ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vldr__32,			0xED100A00), // ____ ____ xx__ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vldr__64,			0xED100B00), // ____ ____ xx__ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpush,				0xED2D0A00), // ____ ____ _x__ ____ xxxx ___X xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpush,				0xED2D0B00), // ____ ____ _x__ ____ xxxx ___x xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_cdp,					0xEE000000), // ____ ____ xxxx xxxx xxxx xxxx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mcr,					0xEE000010), // ____ ____ xxx_ xxxx xxxx xxxx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vml__fp,				0xEE000A00), // ____ ____ _x__ xxxx xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov_7,				0xEE000A10), // ____ ____ ___x xxxx xxxx ____ x.._ ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov_5,				0xEE000B10), // ____ ____ _xx_ xxxx xxxx ____ xxx_ ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mrc,					0xEE100010), // ____ ____ xxx_ xxxx xxxx xxxx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vnml,				0xEE100A40), // ____ ____ _x__ xxxx xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov_6,				0xEE100B10), // ____ ____ xxx_ xxxx xxxx ____ xxx_ ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmul__fp_2,			0xEE200A00), // ____ ____ _x__ xxxx xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vnmul,				0xEE200A40), // ____ ____ _x__ xxxx xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vadd__fp_f,			0xEE300A00), // ____ ____ _x__ xxxx xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vsub__fp_f,			0xEE300A40), // ____ ____ _x__ xxxx xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdiv,				0xEE800A00), // ____ ____ _x__ xxxx xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__reg,			0xEE800B10), // ____ ____ _x__ xxxx xxxx ____ x_x_ ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov__imm,			0xEEB00A00), // ____ ____ _x__ xxxx xxxx ___x ._._ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov__reg_f,			0xEEB00A40), // ____ ____ _x__ ____ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs__f,				0xEEB00AC0), // ____ ____ _x__ ____ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg__f,				0xEEB10A40), // ____ ____ _x__ ____ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vsqrt,				0xEEB10AC0), // ____ ____ _x__ ____ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_reg,		0xEEB20A40), // ____ ____ _x__ ___x xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcmp__reg,			0xEEB40A40), // ____ ____ _x__ ____ xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcmp__to_0,			0xEEB50A40), // ____ ____ _x__ ____ xxxx ___x x_._ ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__dp_sp,			0xEEB70AC0), // ____ ____ _x__ ____ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__fp_i_reg,		0xEEB80A40), // ____ ____ _x__ _xxx xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__fp_fix_reg,	0xEEBA0A40), // ____ ____ _x__ _x_x xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmsr,				0xEEE00A10), // ____ ____ ____ xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmsr,				0xEEE10A10), // ____ ____ ____ xxxx xxxx ____ _.._ ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmrs,				0xEEF00A10), // ____ ____ ____ xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmrs,				0xEEF10A10), // ____ ____ ____ xxxx xxxx ____ _.._ ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vhadd_vhsub,			0xEF000000), // ___x ____ _xxx xxxx xxxx __x_ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vand,				0xEF000110), // ____ ____ _x__ xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqadd,				0xEF000010), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrhadd,				0xEF000100), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqsub,				0xEF000210), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__reg_int,		0xEF000300), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__reg_int,		0xEF000310), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshl__reg,			0xEF000400), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqshl__reg,			0xEF000410), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrshl,				0xEF000500), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqrshl,				0xEF000510), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmax_vmin__int,		0xEF000600), // ___x ____ _xxx xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabd__int,			0xEF000700), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vaba,				0xEF000710), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vadd__int,			0xEF000800), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtst,				0xEF000810), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vml__int,			0xEF000900), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmul_1,				0xEF000910), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpmax_vpmin__int,	0xEF000A00), // ___x ____ _xxx xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqdmulh__vec,		0xEF000B00), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadd__int,			0xEF000B10), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vadd__f32,			0xEF000D00), // ____ ____ _x_x xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vml__f32,			0xEF000D10), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__reg_f32,		0xEF000E00), // ____ ____ _x_x xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmax_vmin__fp,		0xEF000F00), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrecps,				0xEF000F10), // ____ ____ _x_x xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vbic__reg,			0xEF100110), // ____ ____ _x__ xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov__reg,			0xEF200110), // ____ ____ _x__ xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vorr__reg,			0xEF200110), // ____ ____ _x__ xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vsub__f32,			0xEF200D00), // ____ ____ _x_x xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrsqrts,				0xEF200F10), // ____ ____ _x_x xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vorn,				0xEF300110), // ____ ____ _x__ xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vaddl_vaddw,			0xEF800000), // ___x ____ _xxx xxxx xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov_vbitwise,		0xEF800010), // ___x ____ _x__ _xxx xxxx xxxx _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov_vbitwise,		0xEF800010), // ___x ____ _x__ _xxx xxxx xxxx _x__ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshr,				0xEF800010), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov_vbitwise,		0xEF800030), // ___x ____ _x__ 0xxx xxxx xxxx _x__ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov_vbitwise,		0xEF800030), // ___x ____ _x__ _xxx xxxx xxxx _x__ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vml__scalar,			0xEF800040), // ___x ____ _xxx xxxx xxxx _xxx x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vsra,				0xEF800110), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vsubl_vsubw,			0xEF800200), // ___x ____ _xxx xxxx xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrshr,				0xEF800210), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vml__scalar,			0xEF800240), // ___x ____ _xxx xxxx xxxx _xx_ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrsra,				0xEF800310), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqdml__scalar,		0xEF800340), // ____ ____ _xxx xxxx xxxx 0x__ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vaddhn,				0xEF800400), // ____ ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabal,				0xEF800500), // ___x ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshl__imm,			0xEF800510), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vsubhn,				0xEF800600), // ____ ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqshl__imm,			0xEF800610), // ___x ____ _xxx xxxx xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabdl,				0xEF800700), // ___x ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vml__int_long,		0xEF800800), // ___x ____ _xxx xxxx xxxx __x_ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqshr,				0xEF800810), // ___x ____ _xxx xxxx xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshrn,				0xEF800810), // ____ ____ _xxx xxxx xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmul__scalar,		0xEF800840), // ___x ____ _xxx xxxx xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqrshr,				0xEF800850), // ___x ____ _xxx xxxx xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrshrn,				0xEF800850), // ____ ____ _xxx xxxx xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqdml__vec,			0xEF800900), // ____ ____ _xxx xxxx xxxx __x_ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovl,				0xEF800A10), // ___x ____ _xxx x___ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__various,		0xEF800A10), // ___x ____ _xxx xxxx xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmul__scalar,		0xEF800A40), // ___x ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqdmull__scalar,		0xEF800B40), // ____ ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmull,				0xEF800C00), // ___x ____ _xxx xxxx xxxx __x_ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqdmulh__scalar,		0xEF800C40), // ___x ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqdmull__vec,		0xEF800D00), // ____ ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqrdmulh__scalar,	0xEF800D40), // ___x ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__fp_fix_vec,	0xEF800E10), // ___x ____ _xxx xxxx xxxx ___x _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vext,				0xEFB00000), // ____ ____ _x__ xxxx xxxx xxxx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_and__imm,			0xF0000000), // ____ _x__ ___x xxxx _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_b__cond,				0xF0008000), // ____ _xxx xxxx xxxx __x_ xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_b__uncond,			0xF0009000), // ____ _xxx xxxx xxxx __x_ xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_blx,					0xF000C000), // ____ _xxx xxxx xxxx __x_ xxxx xxxx xxx_
		this.new OpcodeTest(OpcodeARM.Index.thumb2_bl,					0xF000D000), // ____ _xxx xxxx xxxx __x_ xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_tst__imm,			0xF0100F00), // ____ _x__ ____ xxxx _xxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_bic__imm,			0xF0200000), // ____ _x__ ___x xxxx _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_orr__imm,			0xF0400000), // ____ _x__ ___x xxxx _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mov__imm,			0xF04F0000), // ____ _x__ ___x ____ _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_orn__imm,			0xF0600000), // ____ _x__ ___x xxxx _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mvn__imm,			0xF06F0000), // ____ _x__ ___x ____ _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_eor__imm,			0xF0800000), // ____ _x__ ___x xxxx _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_teq__imm,			0xF0900F00), // ____ _x__ ____ xxxx _xxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_add__imm,			0xF1000000), // ____ _x__ ___x xxxx _xxx xxxx xxxx xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_add__sp_imm,			0xF10D0000), // ____ _x__ ___x ____ _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_add__imm,			0xF10D0000), // special case for SP
		this.new OpcodeTest(OpcodeARM.Index.thumb2_cmn__imm,			0xF1100F00), // ____ _x__ ____ xxxx _xxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_adc__imm,			0xF1400000), // ____ _x__ ___x xxxx _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sbc__imm,			0xF1600000), // ____ _x__ ___x xxxx _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sub__imm,			0xF1A00000), // ____ _x__ ___x xxxx 0xxx xxxx xxxx xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_sub__sp_imm,			0xF1AD0000), // ____ _x__ ___x ____ 0xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sub__imm,			0xF1AD0000), // special case for SP
		this.new OpcodeTest(OpcodeARM.Index.thumb2_cmp__imm,			0xF1B00F00), // ____ _x__ ____ xxxx _xxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_rsb__imm,			0xF1C00000), // ____ _x__ ___x xxxx _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_addw,				0xF2000000), // ____ _x__ ____ xxxx _xxx xxxx xxxx xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_addw__sp_imm,		0xF20D0000), // ____ _x__ ____ ____ _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_addw,				0xF20D0000), // special case for SP
		this.new OpcodeTest(OpcodeARM.Index.thumb2_adr__add,			0xF20F0000), // ____ _x__ ____ ____ _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_movx,				0xF2400000), // ____ _x__ ____ xxxx _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_subw,				0xF2A00000), // ____ _x__ ____ xxxx 0xxx xxxx xxxx xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_subw__sp_imm,		0xF2AD0000), // ____ _x__ ____ ____ 0xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_subw,				0xF2AD0000), // special case for SP
		this.new OpcodeTest(OpcodeARM.Index.thumb2_adr__sub,			0xF2AF0000), // ____ _x__ ____ ____ _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_movx,				0xF2C00000), // ____ _x__ ____ xxxx _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF300001F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF300003F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF300005F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF300007F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000080), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF300009F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30000A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30000BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30000C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30000DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30000E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30000FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000F00), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000F1F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000F20), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000F3F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000F40), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000F5F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000F60), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000F7F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000F80), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000F9F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000FA0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000FBF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000FC0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000FDF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000FE0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3000FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF300701F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF300703F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF300705F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF300707F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007080), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF300709F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30070A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30070BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30070C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30070DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30070E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30070FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007F00), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007F1F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007F20), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007F3F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007F40), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007F5F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007F60), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007F7F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007F80), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007F9F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007FA0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007FBF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007FC0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007FDF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007FE0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3007FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F001F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F003F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F005F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F007F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0080), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F009F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F00A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F00BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F00C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F00DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F00E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F00FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0F00), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0F1F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0F20), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0F3F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0F40), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0F5F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0F60), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0F7F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0F80), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0F9F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0FA0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0FBF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0FC0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0FDF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0FE0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F0FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F701F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F703F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F705F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F707F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7080), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F709F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F70A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F70BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F70C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F70DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F70E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F70FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7F00), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7F1F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7F20), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7F3F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7F40), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7F5F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7F60), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7F7F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7F80), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7F9F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7FA0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7FBF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7FC0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7FDF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7FE0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF30F7FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF3200000), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF320000F), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200010), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320001F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF3200020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF320003F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320005F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF3200060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF320007F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200080), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320009F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32000A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32000BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32000C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32000DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32000E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32000FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF3200100), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF320010F), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200110), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320011F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF3200120), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF320013F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200140), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320015F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF3200160), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF320017F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200180), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320019F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32001A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32001BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32001C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32001DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32001E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32001FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF3200F00), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF3200F0F), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200F10), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200F1F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF3200F20), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF3200F3F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200F40), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200F5F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF3200F60), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF3200F7F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200F80), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200F9F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF3200FA0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF3200FBF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200FC0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3200FDF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF3200FE0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF3200FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3201000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320101F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3201020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320103F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3201040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320105F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3201060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320107F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3201000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320101F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3201020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320103F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3201040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320105F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3201060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320107F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3201080), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320109F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32010A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32010BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32010C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32010DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32010E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32010FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3207000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320701F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3207020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320703F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3207040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320705F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3207060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320707F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3207000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320701F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3207020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320703F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3207040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320705F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3207060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320707F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF3207080), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF320709F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32070A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32070BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32070C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32070DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32070E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32070FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF32F0000), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF32F000F), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0010), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F001F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F0020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F003F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F005F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F0060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F007F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0080), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F009F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F00A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F00BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F00C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F00DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F00E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F00FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF32F0100), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF32F010F), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0110), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F011F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F0120), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F013F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0140), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F015F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F0160), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F017F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0180), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F019F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F01A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F01BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F01C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F01DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F01E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F01FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF32F0F00), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF32F0F0F), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0F10), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0F1F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F0F20), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F0F3F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0F40), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0F5F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F0F60), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F0F7F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0F80), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0F9F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F0FA0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F0FBF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0FC0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F0FDF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F0FE0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF32F0FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F1000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F101F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F1020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F103F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F1040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F105F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F1060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F107F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F1000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F101F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F1020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F103F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F1040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F105F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F1060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F107F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F1080), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F109F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F10A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F10BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F10C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F10DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F10E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F10FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F00), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F1F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F20), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F3F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F40), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F5F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F60), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F7F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F00), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F1F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F20), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F3F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F40), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F5F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F60), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F7F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F80), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7F9F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7FA0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7FBF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7FC0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7FDF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7FE0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF32F7FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_bfx,					0xF3400000), // ____ _.__ ____ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_bfi,					0xF3600000), // ____ _.__ ____ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_bfc,					0xF36F0000), // ____ _.__ ____ ____ _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_usat,				0xF3800000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_msr,					0xF3808000), // ____ ____ ____ xxxx __._ xx__ .... ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_msr,					0xF3808000), // ____ ____ ___x xxxx __._ xxxx .... ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_usat16,				0xF3A00000), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_yield,				0xF3A08001), // ____ ____ ____ xxxx __x_ x___ ____ ____
		this.new OpcodeTest(OpcodeARM.Index.thumb2_wfe,					0xF3A08002), // ____ ____ ____ xxxx __x_ x___ ____ ____
		this.new OpcodeTest(OpcodeARM.Index.thumb2_wfi,					0xF3A08003), // ____ ____ ____ xxxx __x_ x___ ____ ____
		this.new OpcodeTest(OpcodeARM.Index.thumb2_nop,					0xF3AF8000), // ____ ____ ____ :::: __._ .___ ____ ____
		this.new OpcodeTest(OpcodeARM.Index.thumb2_cps,					0xF3AF8001), // ____ ____ ____ :::: __._ .xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_cps,					0xF3AF8003), // ____ ____ ____ :::: __._ .xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sev,					0xF3AF8004), // ____ ____ ____ :::: __._ .___ ____ ____
		this.new OpcodeTest(OpcodeARM.Index.thumb2_cps,					0xF3AF80EF), // ____ ____ ____ :::: __._ .xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_cps,					0xF3AF87FF), // ____ ____ ____ :::: __._ .xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_dbg,					0xF3AF80F0), // ____ ____ ____ :::: __._ .___ ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_cps,					0xF3AF80F1), // ____ ____ ____ :::: __._ .xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_cps,					0xF3AF87FF), // ____ ____ ____ :::: __._ .xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_clrex,				0xF3BF8F2F), // ____ ____ ____ :::: __._ :::: ____ ::::
		this.new OpcodeTest(OpcodeARM.Index.thumb2_dsb,					0xF3BF8F40), // ____ ____ ____ :::: __._ :::: ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_dmb,					0xF3BF8F50), // ____ ____ ____ :::: __._ :::: ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_isb,					0xF3BF8F60), // ____ ____ ____ :::: __._ :::: ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_bfx,					0xF3C00000), // ____ _.__ ____ xxxx _xxx xxxx xx.x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_bxj,					0xF3C08F00), // ____ ____ ____ xxxx __._ :::: .... ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_subs,				0xF3D08000), // ____ ____ ____ xxxx __x_ xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mrs,					0xF3EF8000), // ____ ____ ____ :::: __._ xxxx .... ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mrs,					0xF3EF8000), // ____ ____ ___x :::: __._ xxxx .... ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_and__imm,			0xF4000000), // ____ _x__ ___x xxxx _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_add__imm,			0xF5000000), // ____ _x__ ___x xxxx _xxx xxxx xxxx xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_add__sp_imm,			0xF50D0000), // ____ _x__ ___x ____ _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_add__imm,			0xF50D0000), // special case for SP
		this.new OpcodeTest(OpcodeARM.Index.thumb2_adc__imm,			0xF5400000), // ____ _x__ ___x xxxx _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_addw,				0xF6000000), // ____ _x__ ____ xxxx _xxx xxxx xxxx xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_addw__sp_imm,		0xF60D0000), // ____ _x__ ____ ____ _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_addw,				0xF60D0000), // special case for SP
		this.new OpcodeTest(OpcodeARM.Index.thumb2_adr__add,			0xF60F0000), // ____ _x__ ____ ____ _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_adr__sub,			0xF6AF0000), // ____ _x__ ____ ____ _xxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7000000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7007FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF70F0000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF70F7FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF7200000), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF720000F), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200010), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF720001F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF7200020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF720003F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF720005F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF7200060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF720007F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200080), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF720009F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72000A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72000BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72000C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72000DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72000E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72000FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF7200100), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF720010F), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200110), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF720011F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF7200120), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF720013F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200140), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF720015F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF7200160), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF720017F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200180), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF720019F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72001A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72001BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72001C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72001DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72001E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72001FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF7200F00), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF7200F0F), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200F10), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200F1F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF7200F20), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF7200F3F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200F40), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200F5F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF7200F60), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF7200F7F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200F80), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200F9F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF7200FA0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF7200FBF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200FC0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7200FDF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF7200FE0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF7200FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7201000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF7207FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF72F0000), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF72F000F), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0010), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F001F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F0020), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F003F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0040), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F005F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F0060), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F007F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0080), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F009F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F00A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F00BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F00C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F00DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F00E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F00FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF72F0100), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF72F010F), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0110), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F011F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F0120), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F013F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0140), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F015F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F0160), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F017F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0180), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F019F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F01A0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F01BF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F01C0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F01DF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F01E0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F01FF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF72F0F00), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat16,				0xF72F0F0F), // ____ _.__ ____ xxxx ____ xxxx __.. xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0F10), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0F1F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F0F20), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F0F3F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0F40), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0F5F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F0F60), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F0F7F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0F80), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0F9F), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F0FA0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F0FBF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0FC0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F0FDF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F0FE0), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//	///	this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat|_stat16,		0xF72F0FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE/AMBIGUOUS
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F1000), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ssat,				0xF72F7FFF), // ____ _.__ __x_ xxxx _xxx xxxx xx.x xxxx	UNPREDICTABLE
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smc,					0xF7F08000), // ____ ____ ____ xxxx ____ .... .... ....
		this.new OpcodeTest(OpcodeARM.Index.thumb2_str,					0xF8000000), // ____ ____ ____ xxxx xxxx ____ __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_str,					0xF8000800), // ____ ____ ____ xxxx xxxx _xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_str,					0xF8000E00), // ____ ____ ____ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF8100000), // ____ ____ ____ xxxx xxxx ____ __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF8100800), // ____ ____ ____ xxxx xxxx _xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF8100E00), // ____ ____ ____ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pld,					0xF810F000), // ____ ____ __x_ xxxx ____ ____ __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pld,					0xF810F0FF), // ____ ____ __x_ xxxx ____ ____ __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pld,					0xF810FC00), // ____ ____ __x_ xxxx ____ ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pld,					0xF810FCFF), // ____ ____ __x_ xxxx ____ ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF81F0000), // ____ ____ x___ ____ xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pld,					0xF81FF000), // ____ ____ x_._ ____ ____ xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_str,					0xF8200000), // ____ ____ ____ xxxx xxxx ____ __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_str,					0xF8200800), // ____ ____ ____ xxxx xxxx _xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_str,					0xF8200E00), // ____ ____ ____ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF8300000), // ____ ____ ____ xxxx xxxx ____ __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF8300800), // ____ ____ ____ xxxx xxxx _xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF8300E00), // ____ ____ ____ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF83F0000), // ____ ____ x___ ____ xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_str,					0xF8400000), // ____ ____ ____ xxxx xxxx ____ __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_str,					0xF8400800), // ____ ____ ____ xxxx xxxx _xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_str,					0xF8400E00), // ____ ____ ____ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_push__reg,			0xF84D0D04), // ____ ____ ____ ____ xxxx ____ ____ ____
		this.new OpcodeTest(OpcodeARM.Index.thumb2_push__reg,			0xF84DFD04), // ____ ____ ____ ____ xxxx ____ ____ ____
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF8500000), // ____ ____ ____ xxxx xxxx ____ __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF8500800), // ____ ____ ____ xxxx xxxx _xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF8500E00), // ____ ____ ____ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pop__reg,			0xF85D0B04), // ____ ____ ____ ____ xxxx ____ ____ ____
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF85F0000), // ____ ____ x___ ____ xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_str,					0xF8800000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF8900000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pld,					0xF890F000), // ____ ____ __x_ xxxx ____ xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pld,					0xF890FFFF), // ____ ____ __x_ xxxx ____ xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_str,					0xF8A00000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF8B00000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_str,					0xF8C00000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF8D00000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vst__multi,			0xF9000000), // ____ ____ _x__ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF9100000), // ____ ____ ____ xxxx xxxx ____ __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF9100800), // ____ ____ ____ xxxx xxxx _xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF9100E00), // ____ ____ ____ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pli,					0xF910F000), // ____ ____ ____ xxxx ____ ____ __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pli,					0xF910FC00), // ____ ____ ____ xxxx ____ ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pli,					0xF910FCFF), // ____ ____ ____ xxxx ____ ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF91F0000), // ____ ____ x___ ____ xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pli,					0xF91FF000), // ____ ____ x___ ____ ____ xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pli,					0xF91FFFFF), // ____ ____ x___ ____ ____ xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vld__multi,			0xF9200000), // ____ ____ _x__ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF9300000), // ____ ____ ____ xxxx xxxx ____ __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF9300800), // ____ ____ ____ xxxx xxxx _xxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF9300E00), // ____ ____ ____ xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF93F0000), // ____ ____ x___ ____ xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vst__xlane,			0xF9800000), // ____ ____ _x__ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vst__xlane,			0xF9800100), // ____ ____ _x__ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vst__xlane,			0xF9800200), // ____ ____ _x__ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vst__xlane,			0xF9800300), // ____ ____ _x__ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF9900000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_pli,					0xF990F000), // ____ ____ ____ xxxx ____ xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vld__xlane,			0xF9A00000), // ____ ____ _x__ xxxx xxxx XXXX xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vld__xlane,			0xF9A00100), // ____ ____ _x__ xxxx xxxx XXXX xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vld__xlane,			0xF9A00200), // ____ ____ _x__ xxxx xxxx XXXX xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vld__xlane,			0xF9A00300), // ____ ____ _x__ xxxx xxxx XXXX xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vld__xlane,			0xF9A00C00), // ____ ____ _x__ xxxx xxxx XXXX xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vld__xlane,			0xF9A00D00), // ____ ____ _x__ xxxx xxxx XXXX xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vld__xlane,			0xF9A00E00), // ____ ____ _x__ xxxx xxxx XXXX xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vld__xlane,			0xF9A00F00), // ____ ____ _x__ xxxx xxxx XXXX xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldr,					0xF9B00000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_lsl__reg,			0xFA00F000), // ____ ____ ___x xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sxtah,				0xFA00F080), // ____ ____ ____ xxxx ____ xxxx _.xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sxth,				0xFA0FF080), // ____ ____ ____ ____ ____ xxxx _.xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_uxtah,				0xFA10F080), // ____ ____ ____ xxxx ____ xxxx _.xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_uxth,				0xFA1FF080), // ____ ____ ____ ____ ____ xxxx _xxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_lsr__reg,			0xFA20F000), // ____ ____ ___x xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sxtab16,				0xFA20F080), // ____ ____ ____ xxxx ____ xxxx _.xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sxtb16,				0xFA2FF080), // ____ ____ ____ ____ ____ xxxx _.xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_uxtab16,				0xFA30F080), // ____ ____ ____ xxxx ____ xxxx _.xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_uxtb16,				0xFA3FF080), // ____ ____ ____ ____ ____ xxxx _.xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_asr__reg,			0xFA40F000), // ____ ____ ___x xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sxtab,				0xFA40F080), // ____ ____ ____ xxxx ____ xxxx _.xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sxtb,				0xFA4FF080), // ____ ____ ____ ____ ____ xxxx _.xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_uxtab,				0xFA50F080), // ____ ____ ____ xxxx ____ xxxx _.xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_uxtb,				0xFA5FF080), // ____ ____ ____ ____ ____ xxxx _.xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ror__reg,			0xFA60F000), // ____ ____ ___x xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFA80F000), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFA80F010), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFA80F020), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFA80F040), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFA80F050), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFA80F060), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_qadd,				0xFA80F090), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_qsub,				0xFA80F0A0), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_qsub,				0xFA80F0B0), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_qadd,				0xFA8BF080), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFA90F000), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFA90F010), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFA90F020), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFA90F040), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFA90F050), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFA90F060), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_reverse,				0xFA90F080), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_reverse,				0xFA90F090), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_reverse,				0xFA90F0A0), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_reverse,				0xFA90F0B0), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sel,					0xFAA0F080), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_clz,					0xFAB0F080), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAC0F000), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAC0F010), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAC0F020), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAC0F040), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAC0F050), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAC0F060), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAD0F000), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAD0F010), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAD0F020), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAD0F040), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAD0F050), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAD0F060), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAE0F000), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAE0F010), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAE0F020), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAE0F040), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAE0F050), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2__r_dnm_math,			0xFAE0F060), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ml,					0xFB000000), // ____ ____ ____ xxxx xxxx xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ml,					0xFB000010), // ____ ____ ____ xxxx xxxx xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mul,					0xFB00F000), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smla,				0xFB100000), // ____ ____ ____ xxxx xxxx xxxx __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smul,				0xFB10F000), // ____ ____ ____ xxxx ____ xxxx __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smlad,				0xFB200000), // ____ ____ ____ xxxx xxxx xxxx ___x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smuad,				0xFB20F000), // ____ ____ ____ xxxx ____ xxxx ___x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smlaw,				0xFB300000), // ____ ____ ____ xxxx xxxx xxxx ___x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smulw,				0xFB30F000), // ____ ____ ____ xxxx ____ xxxx ___x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smlsd,				0xFB400000), // ____ ____ ____ xxxx xxxx xxxx ___x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smusd,				0xFB40F000), // ____ ____ ____ xxxx ____ xxxx ___x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smmla,				0xFB500000), // ____ ____ ____ xxxx xxxx xxxx ___x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smmul,				0xFB50F000), // ____ ____ ____ xxxx ____ xxxx ___x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smmls,				0xFB600000), // ____ ____ ____ xxxx xxxx xxxx ___x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_usada8,				0xFB700000), // ____ ____ ____ xxxx xxxx xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_usad8,				0xFB70F000), // ____ ____ ____ xxxx ____ xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smull,				0xFB800000), // ____ ____ ____ xxxx xxxx xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_sdiv,				0xFB90F0F0), // ____ ____ ____ xxxx :::: xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_umull,				0xFBA00000), // ____ ____ ____ xxxx xxxx xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_udiv,				0xFBB000F0), // ____ ____ ____ xxxx :::: xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smlal,				0xFBC00000), // ____ ____ ____ xxxx xxxx xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smlal,				0xFBC00080), // ____ ____ ____ xxxx xxxx xxxx __xx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smlald,				0xFBC000C0), // ____ ____ ____ xxxx xxxx xxxx ___x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_smlsld,				0xFBD000C0), // ____ ____ ____ xxxx xxxx xxxx ___x xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_umlal,				0xFBE00000), // ____ ____ ____ xxxx xxxx xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_umaal,				0xFBE00060), // ____ ____ ____ xxxx xxxx xxxx ____ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_stc,					0xFC000000), // ____ ___x xxx_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldc,					0xFC100000), // ____ ___x xxx_ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_ldc,					0xFC1F0000), // ____ ___x xxx_ ____ xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mcrr,				0xFC400000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mrrc,				0xFC500000), // ____ ____ ____ xxxx xxxx xxxx xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_cdp2,				0xFE000000), // ____ ____ xxxx xxxx xxxx xxxx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mcr,					0xFE000010), // ____ ____ xxx_ xxxx xxxx xxxx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_mrc,					0xFE100010), // ____ ____ xxx_ xxxx xxxx xxxx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vhadd_vhsub,			0xFF000000), // ___x ____ _xxx xxxx xxxx __x_ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqadd,				0xFF000010), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrhadd,				0xFF000100), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vbif_vbit_vbsl_veor,	0xFF000110), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vbif_vbit_vbsl_veor,	0xFF000110), // ____ ____ _x__ xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqsub,				0xFF000210), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__reg_int,		0xFF000300), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__reg_int,		0xFF000310), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshl__reg,			0xFF000400), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqshl__reg,			0xFF000410), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrshl,				0xFF000500), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqrshl,				0xFF000510), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmax_vmin__int,		0xFF000600), // ___x ____ _xxx xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabd__int,			0xFF000700), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vaba,				0xFF000710), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vsub__int,			0xFF000800), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__reg_int,		0xFF000810), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vml__int,			0xFF000900), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmul_1,				0xFF000910), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpmax_vpmin__int,	0xFF000A00), // ___x ____ _xxx xxxx xxxx ____ xxxx xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqrdmulh__vec,		0xFF000B00), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadd__f32,			0xFF000D00), // ____ ____ _x_x xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmul__f32,			0xFF000D10), // ____ ____ _x_x xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__reg_f32,		0xFF000E00), // ____ ____ _x_x xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vacge_vacgt,			0xFF000E10), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpmax_vpmin__fp,		0xFF000F00), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabd__f32,			0xFF200D00), // ____ ____ _x_x xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__reg_f32,		0xFF200E00), // ____ ____ _x_x xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vaddl_vaddw,			0xFF800000), // ___x ____ _xxx xxxx xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov_vbitwise,		0xFF800010), // ___x ____ _x__ _xxx xxxx xxxx _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshr,				0xFF800010), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov_vbitwise,		0xFF800030), // ___x ____ _x__ 0xxx xxxx xxxx _x__ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmov_vbitwise,		0xFF800030), // ___x ____ _x__ _xxx xxxx xxxx _x__ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vml__scalar,			0xFF800040), // ___x ____ _xxx xxxx xxxx _xxx x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vsra,				0xFF800110), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vsubl_vsubw,			0xFF800200), // ___x ____ _xxx xxxx xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrshr,				0xFF800210), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vml__scalar,			0xFF800240), // ___x ____ _xxx xxxx xxxx _xx_ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrsra,				0xFF800310), // ___x ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vraddhn,				0xFF800400), // ____ ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vsri,				0xFF800410), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabal,				0xFF800500), // ___x ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vsli,				0xFF800510), // ____ ____ _xxx xxxx xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrsubhn,				0xFF800600), // ____ ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqshl__imm,			0xFF800610), // ___x ____ _xxx xxxx xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabdl,				0xFF800700), // ___x ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vml__int_long,		0xFF800800), // ___x ____ _xxx xxxx xxxx __x_ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqshr,				0xFF800810), // ___x ____ _xxx xxxx xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmul__scalar,		0xFF800840), // ___x ____ _xxx xxxx xxxx ___x x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqrshr,				0xFF800850), // ___x ____ _xxx xxxx xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovl,				0xFF800A10), // ___x ____ _xxx x___ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__various,		0xFF800A10), // ___x ____ _xxx xxxx xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmul__scalar,		0xFF800A40), // ___x ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmull,				0xFF800C00), // ___x ____ _xxx xxxx xxxx __x_ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqdmulh__scalar,		0xFF800C40), // ___x ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqrdmulh__scalar,	0xFF800D40), // ___x ____ _xxx xxxx xxxx ____ x_x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__fp_fix_vec,	0xFF800E10), // ___x ____ _xxx xxxx xxxx ___x _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB00000), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB0000F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB00020), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB0002F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB00040), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB0004F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB00060), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB0006F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB00200), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB0020F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB00220), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB0022F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB00240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB0024F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB00260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB0026F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB00400), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB0040F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB00420), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB0042F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB00440), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB0044F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB00460), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB0046F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB00480), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB0048F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB004A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB004AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB004C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB004CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB004E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB004EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB00500), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB0050F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB00520), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB0052F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB00540), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB0054F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB00560), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB0056F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB00580), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB0058F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB005A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB005AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB005C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB005CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB005E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB005EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB00600), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB0060F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB00620), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB0062F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB00640), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB0064F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB00660), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB0066F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB00700), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB0070F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB00720), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB0072F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB00740), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB0074F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB00760), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB0076F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB00780), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB0078F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB007A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB007AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB007C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB007CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB007E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB007EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0080F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0082F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0084F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0086F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0090F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0092F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0094F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0096F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00A00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00A0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00A20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00A2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00A40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00A4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00A60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00A6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00B00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00B0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00B20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00B2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00B40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00B4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00B60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB00B6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB00C00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB00C0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB00C20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB00C2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB00C40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB00C4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB00C60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB00C6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB0F000), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB0F00F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB0F020), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB0F02F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB0F040), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB0F04F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB0F060), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFB0F06F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB0F200), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB0F20F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB0F220), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB0F22F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB0F240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB0F24F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB0F260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFB0F26F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB0F400), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB0F40F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB0F420), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB0F42F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB0F440), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB0F44F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB0F460), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFB0F46F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB0F480), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB0F48F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB0F4A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB0F4AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB0F4C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB0F4CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB0F4E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFB0F4EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB0F500), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB0F50F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB0F520), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB0F52F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB0F540), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB0F54F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB0F560), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFB0F56F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB0F580), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB0F58F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB0F5A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB0F5AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB0F5C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB0F5CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB0F5E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFB0F5EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB0F600), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB0F60F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB0F620), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB0F62F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB0F640), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB0F64F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB0F660), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFB0F66F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB0F700), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB0F70F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB0F720), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB0F72F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB0F740), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB0F74F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB0F760), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFB0F76F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB0F780), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB0F78F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB0F7A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB0F7AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB0F7C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB0F7CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB0F7E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFB0F7EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB0FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB0FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB0FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB0FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB0FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB0FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB0FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB0FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB0FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB10000), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1000F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB10020), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1002F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB10040), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1004F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB10060), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1006F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB10080), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1008F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB100A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB100AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB100C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB100CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB100E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB100EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB10100), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1010F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB10120), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1012F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB10140), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1014F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB10160), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1016F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB10180), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1018F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB101A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB101AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB101C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB101CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB101E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB101EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB10200), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1020F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB10220), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1022F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB10240), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1024F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB10260), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1026F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB10300), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1030F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB10320), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1032F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB10340), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1034F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB10360), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1036F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB10380), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1038F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB103A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB103AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB103C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB103CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB103E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB103EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB10400), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1040F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB10420), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1042F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB10440), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1044F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB10460), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1046F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB10480), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1048F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB104A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB104AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB104C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB104CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB104E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB104EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB10500), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1050F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB10520), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1052F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB10540), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1054F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB10560), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1056F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB10580), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1058F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB105A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB105AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB105C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB105CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB105E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB105EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB10600), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1060F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB10620), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1062F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB10640), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1064F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB10660), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1066F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB10700), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1070F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB10720), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1072F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB10740), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1074F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB10760), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1076F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB10780), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1078F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB107A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB107AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB107C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB107CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB107E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB107EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F000), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F00F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F020), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F02F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F040), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F04F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F060), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F06F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F080), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F08F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F0A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F0AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F0C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F0CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F0E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F0EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F100), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F10F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F120), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F12F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F140), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F14F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F160), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F16F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F180), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F18F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F1A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F1AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F1C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F1CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F1E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F1EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F200), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F20F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F220), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F22F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F240), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F24F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F260), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F26F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F300), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F30F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F320), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F32F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F340), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F34F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F360), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F36F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F380), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F38F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F3A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F3AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F3C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F3CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F3E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F3EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F400), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F40F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F420), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F42F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F440), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F44F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F460), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFB1F46F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F480), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F48F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F4A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F4AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F4C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F4CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F4E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFB1F4EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F500), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F50F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F520), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F52F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F540), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F54F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F560), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFB1F56F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F580), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F58F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F5A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F5AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F5C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F5CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F5E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFB1F5EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F600), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F60F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F620), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F62F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F640), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F64F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F660), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFB1F66F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F700), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F70F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F720), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F72F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F740), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F74F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F760), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFB1F76F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F780), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F78F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F7A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F7AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F7C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F7CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F7E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFB1F7EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB1FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB1FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB1FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB1FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB1FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB1FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB1FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB1FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB1FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB20000), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB2000F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB20020), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB2002F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB20040), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB2004F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB20060), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB2006F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB20080), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB2008F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB200A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB200AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB200C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB200CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB200E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB200EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB20100), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB2010F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB20120), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB2012F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB20140), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB2014F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB20160), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB2016F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB20180), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB2018F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB201A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB201AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB201C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB201CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB201E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB201EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFB20200), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFB2020F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFB20220), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFB2022F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB20240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2024F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB20260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2026F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB20280), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2028F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB202A0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB202AF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB202C0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB202CF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB202E0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB202EF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFB20300), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFB2030F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFB20320), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFB2032F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB20600), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB2060F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB20620), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB2062F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB20700), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB2070F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB20720), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB2072F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB2F000), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB2F00F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB2F020), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB2F02F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB2F040), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB2F04F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB2F060), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB2F06F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB2F080), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB2F08F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB2F0A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB2F0AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB2F0C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB2F0CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB2F0E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB2F0EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB2F100), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB2F10F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB2F120), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB2F12F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB2F140), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB2F14F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB2F160), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB2F16F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB2F180), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB2F18F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB2F1A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB2F1AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB2F1C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB2F1CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB2F1E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB2F1EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFB2F200), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFB2F20F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFB2F220), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFB2F22F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2F240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2F24F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2F260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2F26F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2F280), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2F28F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2F2A0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2F2AF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2F2C0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2F2CF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2F2E0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB2F2EF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFB2F300), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFB2F30F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFB2F320), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFB2F32F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB2F600), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB2F60F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB2F620), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB2F62F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB2F700), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB2F70F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB2F720), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB2F72F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB2FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB2FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB2FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB2FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB2FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB2FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB2FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB2FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB2FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrecpe,				0xFFB30400), // ____ ____ _x__ xx__ xxxx ___x _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrsqrte,				0xFFB30480), // ____ ____ _x__ xx__ xxxx ___x _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__fp_i_vec,		0xFFB30600), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB3FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB3FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB3FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB3FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB3FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB3FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB3FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB3FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB3FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB4FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB4FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB4FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB4FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB4FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB4FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB4FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB4FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB4FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB5FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB5FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB5FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB5FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB5FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB5FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB5FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB5FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB5FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB60000), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB6000F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB60020), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB6002F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB60040), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB6004F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB60060), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFB6006F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB60080), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB6008F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB600A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB600AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB600C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB600CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB600E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFB600EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB60100), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB6010F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB60120), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB6012F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB60140), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB6014F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB60160), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFB6016F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB60180), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB6018F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB601A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB601AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB601C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB601CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB601E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFB601EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFB60200), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFB6020F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFB60220), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFB6022F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB60240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB6024F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB60260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB6026F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB60280), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB6028F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB602A0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB602AF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB602C0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB602CF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB602E0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFB602EF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFB60300), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFB6030F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFB60320), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFB6032F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB60600), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB6060F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB60620), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB6062F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB60700), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB6070F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB60720), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFB6072F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB6FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB6FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB6FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB6FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB6FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB6FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB6FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB6FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB6FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB7FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB7FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB7FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB7FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB7FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB7FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB7FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB7FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB7FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB8FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB8FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB8FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB8FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB8FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB8FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB8FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB8FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB8FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFB9FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB9FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB9FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB9FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB9FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB9FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB9FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB9FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFB9FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBA0000), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBA000F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBA0020), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBA002F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBA0040), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBA004F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBA0060), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBA006F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBA0080), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBA008F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBA00A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBA00AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBA00C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBA00CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBA00E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBA00EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBA0100), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBA010F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBA0120), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBA012F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBA0140), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBA014F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBA0160), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBA016F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBA0180), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBA018F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBA01A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBA01AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBA01C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBA01CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBA01E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBA01EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFBA0200), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFBA020F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFBA0220), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFBA022F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBA0240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBA024F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBA0260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBA026F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBA0280), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBA028F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBA02A0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBA02AF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBA02C0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBA02CF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBA02E0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBA02EF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFBA0300), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFBA030F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFBA0320), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFBA032F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBA0600), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBA060F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBA0620), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBA062F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBA0700), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBA070F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBA0720), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBA072F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAF96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBAFB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBAFC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBAFC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBAFC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBAFC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBAFC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBAFC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBAFC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBAFC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBF96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBBFB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBBFC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBBFC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBBFC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBBFC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBBFC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBBFC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBBFC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBBFC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCF96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBCFB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBCFC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBCFC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBCFC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBCFC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBCFC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBCFC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBCFC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBCFC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDF96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBDFB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBDFC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBDFC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBDFC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBDFC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBDFC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBDFC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBDFC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBDFC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBE0000), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBE000F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBE0020), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBE002F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBE0040), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBE004F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBE0060), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFBE006F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBE0080), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBE008F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBE00A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBE00AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBE00C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBE00CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBE00E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFBE00EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBE0100), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBE010F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBE0120), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBE012F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBE0140), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBE014F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBE0160), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFBE016F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBE0180), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBE018F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBE01A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBE01AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBE01C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBE01CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBE01E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFBE01EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFBE0200), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFBE020F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFBE0220), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFBE022F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBE0240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBE024F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBE0260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBE026F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBE0280), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBE028F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBE02A0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBE02AF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBE02C0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBE02CF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBE02E0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFBE02EF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFBE0300), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFBE030F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFBE0320), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFBE032F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBE0600), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBE060F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBE0620), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBE062F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBE0700), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBE070F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBE0720), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFBE072F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEF96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBEFB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBEFC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBEFC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBEFC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBEFC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBEFC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBEFC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBEFC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBEFC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFF96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFBFFB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBFFC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBFFC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBFFC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBFFC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBFFC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBFFC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBFFC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFBFFC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF00000), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF0000F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF00020), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF0002F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF00040), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF0004F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF00060), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF0006F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF00200), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF0020F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF00220), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF0022F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF00240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF0024F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF00260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF0026F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF00400), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF0040F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF00420), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF0042F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF00440), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF0044F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF00460), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF0046F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF00480), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF0048F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF004A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF004AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF004C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF004CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF004E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF004EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF00500), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF0050F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF00520), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF0052F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF00540), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF0054F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF00560), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF0056F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF00580), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF0058F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF005A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF005AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF005C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF005CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF005E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF005EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF00600), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF0060F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF00620), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF0062F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF00640), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF0064F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF00660), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF0066F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF00700), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF0070F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF00720), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF0072F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF00740), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF0074F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF00760), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF0076F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF00780), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF0078F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF007A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF007AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF007C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF007CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF007E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF007EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0080F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0082F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0084F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0086F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0090F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0092F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0094F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0096F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00A00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00A0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00A20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00A2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00A40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00A4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00A60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00A6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00B00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00B0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00B20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00B2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00B40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00B4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00B60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF00B6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF00C00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF00C0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF00C20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF00C2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF00C40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF00C4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF00C60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF00C6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF0F000), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF0F00F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF0F020), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF0F02F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF0F040), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF0F04F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF0F060), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrev,				0xFFF0F06F), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF0F200), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF0F20F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF0F220), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF0F22F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF0F240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF0F24F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF0F260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpaddl,				0xFFF0F26F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF0F400), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF0F40F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF0F420), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF0F42F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF0F440), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF0F44F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF0F460), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcls,				0xFFF0F46F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF0F480), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF0F48F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF0F4A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF0F4AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF0F4C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF0F4CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF0F4E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclz,				0xFFF0F4EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF0F500), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF0F50F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF0F520), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF0F52F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF0F540), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF0F54F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF0F560), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcnt,				0xFFF0F56F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF0F580), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF0F58F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF0F5A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF0F5AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF0F5C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF0F5CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF0F5E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmvn,				0xFFF0F5EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF0F600), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF0F60F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF0F620), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF0F62F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF0F640), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF0F64F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF0F660), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vpadal,				0xFFF0F66F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF0F700), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF0F70F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF0F720), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF0F72F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF0F740), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF0F74F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF0F760), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqabs,				0xFFF0F76F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF0F780), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF0F78F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF0F7A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF0F7AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF0F7C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF0F7CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF0F7E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqneg,				0xFFF0F7EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF0FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF0FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF0FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF0FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF0FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF0FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF0FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF0FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF0FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF10000), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1000F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF10020), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1002F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF10040), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1004F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF10060), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1006F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF10080), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1008F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF100A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF100AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF100C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF100CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF100E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF100EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF10100), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1010F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF10120), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1012F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF10140), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1014F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF10160), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1016F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF10180), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1018F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF101A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF101AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF101C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF101CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF101E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF101EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF10200), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1020F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF10220), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1022F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF10240), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1024F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF10260), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1026F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF10300), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1030F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF10320), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1032F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF10340), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1034F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF10360), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1036F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF10380), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1038F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF103A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF103AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF103C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF103CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF103E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF103EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF10400), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1040F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF10420), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1042F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF10440), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1044F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF10460), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1046F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF10480), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1048F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF104A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF104AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF104C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF104CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF104E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF104EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF10500), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1050F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF10520), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1052F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF10540), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1054F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF10560), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1056F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF10580), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1058F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF105A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF105AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF105C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF105CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF105E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF105EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF10600), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1060F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF10620), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1062F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF10640), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1064F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF10660), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1066F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF10700), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1070F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF10720), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1072F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF10740), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1074F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF10760), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1076F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF10780), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1078F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF107A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF107AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF107C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF107CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF107E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF107EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F000), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F00F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F020), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F02F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F040), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F04F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F060), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F06F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F080), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F08F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F0A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F0AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F0C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F0CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F0E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F0EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F100), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F10F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F120), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F12F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F140), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F14F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F160), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F16F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F180), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F18F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F1A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F1AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F1C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F1CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F1E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F1EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F200), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F20F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F220), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F22F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F240), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F24F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F260), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F26F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F300), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F30F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F320), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F32F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F340), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F34F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F360), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F36F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F380), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F38F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F3A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F3AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F3C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F3CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F3E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F3EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F400), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F40F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F420), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F42F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F440), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F44F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F460), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcgt__imm0,			0xFFF1F46F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F480), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F48F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F4A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F4AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F4C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F4CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F4E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcge__imm0,			0xFFF1F4EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F500), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F50F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F520), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F52F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F540), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F54F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F560), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vceq__imm0,			0xFFF1F56F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F580), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F58F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F5A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F5AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F5C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F5CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F5E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcle,				0xFFF1F5EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F600), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F60F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F620), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F62F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F640), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F64F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F660), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vclt,				0xFFF1F66F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F700), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F70F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F720), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F72F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F740), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F74F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F760), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vabs,				0xFFF1F76F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F780), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F78F), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F7A0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F7AF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F7C0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F7CF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F7E0), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vneg,				0xFFF1F7EF), // ____ ____ _x__ xx__ xxxx _x__ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF1FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF1FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF1FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF1FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF1FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF1FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF1FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF1FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF1FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF20000), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF2000F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF20020), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF2002F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF20040), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF2004F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF20060), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF2006F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF20080), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF2008F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF200A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF200AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF200C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF200CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF200E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF200EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF20100), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF2010F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF20120), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF2012F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF20140), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF2014F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF20160), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF2016F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF20180), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF2018F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF201A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF201AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF201C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF201CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF201E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF201EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFF20200), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFF2020F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFF20220), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFF2022F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF20240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2024F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF20260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2026F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF20280), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2028F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF202A0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF202AF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF202C0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF202CF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF202E0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF202EF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFF20300), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFF2030F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFF20320), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFF2032F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF20600), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF2060F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF20620), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF2062F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF20700), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF2070F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF20720), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF2072F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF2F000), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF2F00F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF2F020), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF2F02F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF2F040), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF2F04F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF2F060), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF2F06F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF2F080), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF2F08F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF2F0A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF2F0AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF2F0C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF2F0CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF2F0E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF2F0EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF2F100), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF2F10F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF2F120), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF2F12F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF2F140), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF2F14F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF2F160), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF2F16F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF2F180), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF2F18F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF2F1A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF2F1AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF2F1C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF2F1CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF2F1E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF2F1EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFF2F200), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFF2F20F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFF2F220), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFF2F22F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2F240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2F24F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2F260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2F26F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2F280), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2F28F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2F2A0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2F2AF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2F2C0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2F2CF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2F2E0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF2F2EF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFF2F300), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFF2F30F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFF2F320), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFF2F32F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF2F600), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt_6,				0xFFF2F60F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt_6,				0xFFF2F620), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt_6,				0xFFF2F62F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt_6,				0xFFF2F700), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt_6,				0xFFF2F70F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt_6,				0xFFF2F720), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF2F72F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrecpe,				0xFFF30400), // ____ ____ _x__ xx__ xxxx ___x _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vrsqrte,				0xFFF30480), // ____ ____ _x__ xx__ xxxx ___x _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__fp_i_vec,		0xFFF30600), // ____ ____ _x__ xx__ xxxx ___x xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF60000), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF6000F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF60020), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF6002F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF60040), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF6004F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF60060), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFF6006F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF60080), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF6008F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF600A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF600AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF600C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF600CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF600E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFF600EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF60100), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF6010F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF60120), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF6012F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF60140), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF6014F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF60160), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFF6016F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF60180), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF6018F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF601A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF601AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF601C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF601CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF601E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFF601EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFF60200), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFF6020F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFF60220), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFF6022F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF60240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF6024F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF60260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF6026F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF60280), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF6028F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF602A0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF602AF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF602C0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF602CF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF602E0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFF602EF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFF60300), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFF6030F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFF60320), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFF6032F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF60600), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF6060F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF60620), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF6062F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF60700), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF6070F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF60720), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFF6072F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF6FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF6FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF6FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF6FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF6FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF6FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF6FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF6FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF6FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF7FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF7FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF7FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF7FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF7FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF7FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF7FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF7FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF7FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF8FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF8FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF8FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF8FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF8FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF8FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF8FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF8FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF8FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9F96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFF9FB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF9FC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF9FC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF9FC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF9FC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF9FC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF9FC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF9FC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFF9FC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFA0000), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFA000F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFA0020), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFA002F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFA0040), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFA004F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFA0060), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFA006F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFA0080), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFA008F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFA00A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFA00AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFA00C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFA00CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFA00E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFA00EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFA0100), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFA010F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFA0120), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFA012F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFA0140), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFA014F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFA0160), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFA016F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFA0180), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFA018F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFA01A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFA01AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFA01C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFA01CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFA01E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFA01EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFFA0200), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFFA020F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFFA0220), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFFA022F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFA0240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFA024F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFA0260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFA026F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFA0280), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFA028F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFA02A0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFA02AF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFA02C0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFA02CF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFA02E0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFA02EF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFFA0300), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFFA030F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFFA0320), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFFA032F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFA0600), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFA060F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFA0620), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFA062F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFA0700), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFA070F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFA0720), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFA072F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAF96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFAFB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFAFC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFAFC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFAFC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFAFC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFAFC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFAFC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFAFC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFAFC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBF96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFBFB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFBFC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFBFC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFBFC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFBFC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFBFC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFBFC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFBFC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFBFC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCF96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFCFB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFCFC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFCFC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFCFC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFCFC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFCFC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFCFC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFCFC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFCFC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDF96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFDFB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFDFC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFDFC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFDFC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFDFC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFDFC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFDFC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFDFC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFDFC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFE0000), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFE000F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFE0020), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFE002F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFE0040), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFE004F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFE0060), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vswp,				0xFFFE006F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFE0080), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFE008F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFE00A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFE00AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFE00C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFE00CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFE00E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtrn,				0xFFFE00EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFE0100), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFE010F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFE0120), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFE012F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFE0140), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFE014F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFE0160), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vuzp,				0xFFFE016F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFE0180), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFE018F), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFE01A0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFE01AF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFE01C0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFE01CF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFE01E0), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vzip,				0xFFFE01EF), // ____ ____ _x__ xx__ xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFFE0200), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFFE020F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFFE0220), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vmovn,				0xFFFE022F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFE0240), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFE024F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFE0260), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFE026F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFE0280), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFE028F), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFE02A0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFE02AF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFE02C0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFE02CF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFE02E0), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vqmov,				0xFFFE02EF), // ____ ____ _x__ xx__ xxxx ____ xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFFE0300), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFFE030F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFFE0320), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vshll__max,			0xFFFE032F), // ____ ____ _x__ xx__ xxxx ____ __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFE0600), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFE060F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFE0620), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFE062F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFE0700), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFE070F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFE0720), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vcvt__hp_sp_vec,		0xFFFE072F), // ____ ____ _x__ xx__ xxxx ___x __x_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEF96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFEFB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFEFC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFEFC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFEFC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFEFC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFEFC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFEFC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFEFC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFEFC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF800), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF80F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF820), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF82F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF840), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF84F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF860), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF86F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF900), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF90F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF920), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF92F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF940), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF94F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF960), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFF96F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFA00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFA0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFA20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFA2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFA40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFA4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFA60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFA6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFB00), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFB0F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFB20), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFB2F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFB40), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFB4F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFB60), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vtb,					0xFFFFFB6F), // ____ ____ _x__ xxxx xxxx __xx xxx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFFFC00), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFFFC0F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFFFC20), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFFFC2F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFFFC40), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFFFC4F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
//		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFFFC60), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
		this.new OpcodeTest(OpcodeARM.Index.thumb2_vdup__scalar,		0xFFFFFC6F), // ____ ____ _x__ xxxx xxxx ____ _xx_ xxxx
	};

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
			// a couple of cases have been deliberately set to null to start
			if (!opcode.getIndex().equals(OpcodeARM.Index.arm__r_dnm_math)) {
				String mnemonic = opcode.getMnemonic();
				assertTrue(mnemonic != null);
				assertTrue(mnemonic.length() > 0);
			}
			String pattern = opcode.getOpcodePattern();
			assertTrue(pattern != null);
			assertTrue(pattern.length() > 0);
		}
	}

	@Test
	public void testOpcodeThumb() {
		for (OpcodeARM opcode : OpcodeARM.thumb_opcode_table) {
			String mnemonic = opcode.getMnemonic();
			assertTrue(mnemonic != null);
			assertTrue(mnemonic.length() > 0);
			String pattern = opcode.getOpcodePattern();
			assertTrue(pattern != null);
			assertTrue(pattern.length() > 0);
		}
	}

	@Test
	public void testOpcodeThumb2() {
		for (OpcodeARM opcode : OpcodeARM.thumb2_opcode_table) {
			// a couple of cases have been deliberately set to null to start
			if (!(opcode.getIndex().equals(OpcodeARM.Index.thumb2__r_dnm_math)
				  || opcode.getIndex().equals(OpcodeARM.Index.thumb2_vmov_vbitwise))) {
				String mnemonic = opcode.getMnemonic();
				assertTrue(mnemonic != null);
				assertTrue(mnemonic.length() > 0);
			}
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
					break;
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
					break;
				}
			}
			assertNotNull(opcodeThumb);
			int result = opcodeTest.getOpcode() & opcodeThumb.getOpcodeMask();
			assertEquals(opcodeTest.getOpcode()+"", opcodeThumb.getOpcodeResult(), result);
		}
	}


	@Test
	public void testThumb2OpcodeTable() {
		for (OpcodeTest opcodeTest : thumb2_test_opcode_table) {
			OpcodeARM opcodeThumb2 = null;
			for (OpcodeARM element : OpcodeARM.thumb2_opcode_table) {
				if (element.getIndex().equals(opcodeTest.getIndex())) {
					opcodeThumb2 = element;
					break;
				}
			}
			assertNotNull(opcodeThumb2);
			int result = opcodeTest.getOpcode() & opcodeThumb2.getOpcodeMask();
			assertEquals(opcodeTest.getOpcode()+"", opcodeThumb2.getOpcodeResult(), result);
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
