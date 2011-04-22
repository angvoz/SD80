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

package org.eclipse.cdt.debug.edc.internal.arm.disassembler;

/**
 * ARM Opcode map.
 */
public class OpcodeARM {

	public enum Index {

		// 32-bit ARM instructions
		arm_adc__imm, arm_adc__reg, arm_adc__rsr,
		arm_add__imm, arm_add__reg, arm_add__rsr,
		arm_adr__higher, arm_adr__lower,
		arm_and__imm, arm_and__reg, arm_and__rsr,
		arm_asr__imm, arm_asr__reg,
		arm_b,
		arm_bfc,
		arm_bfi,
		arm_bic__imm, arm_bic__reg, arm_bic__rsr,
		arm_bkpt,
		arm_bl,
		arm_blx__imm, arm_blx__reg,
		arm_bx,
		arm_bxj,
		arm_cdp,
		arm_cdp2,
		arm_clrex,
		arm_clz,
		arm_cmn__imm, arm_cmn__reg, arm_cmn__rsr,
		arm_cmp__imm, arm_cmp__reg, arm_cmp__rsr,
		arm_cps,
		arm_dbg,
		arm_dmb,
		arm_dsb,
		arm_eor__imm, arm_eor__reg, arm_eor__rsr,
		arm_isb,
		arm_ldc__imm, arm_ldc__lit,
		arm_ldc2__imm, arm_ldc2__lit,
		arm_ldm, arm_ldm__exc_ret, arm_ldm__user_reg,
		arm_ldmda,
		arm_ldmdb,
		arm_ldmib,
		arm_ldr__imm, arm_ldr__lit, arm_ldr__reg,
		arm_ldrb__imm, arm_ldrb__lit, arm_ldrb__reg,
		arm_ldrbt__imm, arm_ldrbt__reg,
		arm_ldrd__imm, arm_ldrd__lit, arm_ldrd__reg,
		arm_ldrex,
		arm_ldrexb,
		arm_ldrexd,
		arm_ldrexh,
		arm_ldrh__imm, arm_ldrh__lit, arm_ldrh__reg,
		arm_ldrht__imm, arm_ldrht__reg,
		arm_ldrsb__imm, arm_ldrsb__lit, arm_ldrsb__reg,
		arm_ldrsbt__imm, arm_ldrsbt__reg,
		arm_ldrsh__imm, arm_ldrsh__lit, arm_ldrsh__reg,
		arm_ldrsht__imm, arm_ldrsht__reg,
		arm_ldrt__imm, arm_ldrt__reg,
		arm_lsl__imm, arm_lsl__reg,
		arm_lsr__imm, arm_lsr__reg,
		arm_mcr,
		arm_mcr2,
		arm_mcrr,
		arm_mcrr2,
		arm_mla,
		arm_mls,
		arm_mov__imm, arm_mov__reg,
		arm_movw,
		arm_movt,
		arm_mrc,
		arm_mrc2,
		arm_mrrc,
		arm_mrrc2,
		arm_mrs,
		arm_msr__imm, arm_msr__reg, arm_msr__sys_imm, arm_msr__sys_reg,
		arm_mul,
		arm_mvn__imm, arm_mvn__reg, arm_mvn__rsr,
		arm_nop,
		arm_orr__imm, arm_orr__reg, arm_orr__rsr,
		arm_pkh,
		arm_pld__imm, arm_pld__lit, arm_pld__reg,
		arm_pli__imm_lit, arm_pli__reg,
		arm_pop__regs, arm_pop__reg,
		arm_push__regs, arm_push__reg,
		arm_qadd,
		arm_qdadd,
		arm_qdsub,
		arm_qsub,
		arm__r_dnm_math,
		arm_rbit,
		arm_rev,
		arm_rev16,
		arm_revsh,
		arm_rfe,
		arm_ror__imm, arm_ror__reg,
		arm_rrx,
		arm_rsb__imm, arm_rsb__reg, arm_rsb__rsr,
		arm_rsc__imm, arm_rsc__reg, arm_rsc__rsr,
		arm_sbc__imm, arm_sbc__reg, arm_sbc__rsr,
		arm_sbfx,
		arm_sel,
		arm_setend,
		arm_sev,
		arm_smc,
		arm_smla,
		arm_smlad,
		arm_smlal,
		arm_smlalxy,
		arm_smlald,
		arm_smlaw,
		arm_smlsd,
		arm_smlsld,
		arm_smmla,
		arm_smmls,
		arm_smmul,
		arm_smuad,
		arm_smul,
		arm_smull,
		arm_smulw,
		arm_smusd,
		arm_srs,
		arm_ssat,
		arm_ssat16,
		arm_stc,
		arm_stc2,
		arm_stm__regs, arm_stm__usr_regs,
		arm_stmda,
		arm_stmdb,
		arm_stmib,
		arm_str__imm, arm_str__reg,
		arm_strb__imm, arm_strb__reg,
		arm_strbt__imm, arm_strbt__reg,
		arm_strd__imm, arm_strd__reg,
		arm_strex,
		arm_strexb,
		arm_strexd,
		arm_strexh,
		arm_strh__imm, arm_strh__reg,
		arm_strht__imm, arm_strht__reg,
		arm_strt__imm, arm_strt__reg,
		arm_sub__imm, arm_sub__reg, arm_sub__rsr,
		arm_svc,
		arm_swp,
		arm_sxtab,
		arm_sxtab16,
		arm_sxtah,
		arm_sxtb,
		arm_sxtb16,
		arm_sxth,
		arm_teq__imm, arm_teq__reg, arm_teq__rsr,
		arm_tst__imm, arm_tst__reg, arm_tst__rsr,
		arm_ubfx,
		arm_umaal,
		arm_umlal,
		arm_umull,
		arm_usad8,
		arm_usada8,
		arm_usat,
		arm_usat16,
		arm_uxtab,
		arm_uxtab16,
		arm_uxtah,
		arm_uxtb,
		arm_uxtb16,
		arm_uxth,
		arm_vaba,
		arm_vabal,
		arm_vabd__int, arm_vabd__f32,
		arm_vabdl,
		arm_vabs, arm_vabs__f,
		arm_vacge_vacgt,
		arm_vadd__int, arm_vadd__f32, arm_vadd__fp_f, arm_vaddl_vaddw,
		arm_vaddhn,
		arm_vand,
		arm_vbic,
		arm_vbif_vbit_vbsl_veor,
		arm_vceq__reg_int, arm_vceq__reg_f32, arm_vceq__imm0,
		arm_vcge__reg_int, arm_vcge__reg_f32, arm_vcge__imm0,
		arm_vcgt__reg_int, arm_vcgt__reg_f32, arm_vcgt__imm0,
		arm_vcle,
		arm_vcls,
		arm_vclt,
		arm_vclz,
		arm_vcmp__reg, arm_vcmp__to_0,
		arm_vcnt,
		arm_vcvt__fp_i_vec, arm_vcvt__fp_i_reg, arm_vcvt__fp_fix_vec,
		arm_vcvt__fp_fix_reg, arm_vcvt__dp_sp, arm_vcvt__hp_sp_vec, arm_vcvt__hp_sp_reg,
		arm_vdiv,
		arm_vdup__scalar, arm_vdup__reg,
		arm_vext,
		arm_vhadd_vhsub,
		arm_vld__multi,
		arm_vld__xlane,
		arm_vldm__64, arm_vldm__32,
		arm_vldr__64, arm_vldr__32,
		arm_vmax_vmin__int, arm_vmax_vmin__fp,
		arm_vml__int, arm_vml__int_long, arm_vml__f32, arm_vml__fp, arm_vml__scalar,
		arm_vmov_vbitwise, arm_vmov__imm, arm_vmov_vorr, arm_vmov__reg_f,
		arm_vmov_5, arm_vmov_6, arm_vmov_7, arm_vmov_8, arm_vmov_9,
		arm_vmovl,
		arm_vmovn,
		arm_vmrs,
		arm_vmsr,
		arm_vmul_1, arm_vmul_f32, arm_vmul__fp_2, arm_vmul__scalar,
		arm_vmull,
		arm_vmvn,
		arm_vneg, arm_vneg__f,
		arm_vnml,
		arm_vnmul,
		arm_vorn,
		arm_vpadal,
		arm_vpadd__int, arm_vpadd__f32,
		arm_vpaddl,
		arm_vpmax_vpmin__int, arm_vpmax_vpmin__fp,
		arm_vpop,
		arm_vpush,
		arm_vqabs,
		arm_vqadd,
		arm_vqdml__vec, arm_vqdml__scalar,
		arm_vqdmulh__vec, arm_vqdmulh__scalar,
		arm_vqdmull__vec, arm_vqdmull__scalar,
		arm_vqmov,
		arm_vqneg,
		arm_vqrdmulh__vec, arm_vqrdmulh__scalar,
		arm_vqrshl,
		arm_vqrshr,
		arm_vqshl__reg, arm_vqshl__imm,
		arm_vqshr,
		arm_vqsub,
		arm_vraddhn,
		arm_vrecpe,
		arm_vrecps,
		arm_vrev,
		arm_vrhadd,
		arm_vrshl,
		arm_vrshr,
		arm_vrshrn,
		arm_vrsqrte,
		arm_vrsqrts,
		arm_vrsra,
		arm_vrsubhn,
		arm_vshl__imm, arm_vshl__reg,
		arm_vshll__various, arm_vshll__max,
		arm_vshr,
		arm_vshrn,
		arm_vsli,
		arm_vsqrt,
		arm_vsra,
		arm_vsri,
		arm_vst__multi,
		arm_vst__xlane,
		arm_vstm__64, arm_vstm__32,
		arm_vstr__64, arm_vstr__32,
		arm_vsub__int, arm_vsub__f32, arm_vsub__fp_f, arm_vsubl_vsubw,
		arm_vsubhn,
		arm_vswp,
		arm_vtb,
		arm_vtrn,
		arm_vtst,
		arm_vuzp,
		arm_vzip,
		arm_wfe,
		arm_wfi,
		arm_yield,
		arm_undefined,

		// 16-bit Thumb instructions
		thumb_adc,
		thumb_add__imm, thumb_add__imm_to_sp, thumb_add__reg,
		thumb_add__reg_imm, thumb_add__reg_reg,
		thumb_add__sp_imm,
		thumb_adr,
		thumb_and,
		thumb_asr__imm, thumb_asr__reg,
		thumb_b_1, thumb_b_2,
		thumb_bic,
		thumb_bkpt,
		thumb_blx,
		thumb_bx,
		thumb_cbnz_cbz,
		thumb_cmn,
		thumb_cmp__imm, thumb_cmp__reg, thumb_cmp__reg_hi,
		thumb_cps,
		thumb_eor,
		thumb_it,
		thumb_ldm,
		thumb_ldr__imm, thumb_ldr__imm_sp, thumb_ldr__lit, thumb_ldr__reg,
		thumb_ldrb__imm, thumb_ldrb__reg,
		thumb_ldrh__imm, thumb_ldrh__reg,
		thumb_ldrsb,
		thumb_ldrsh,
		thumb_lsl__imm, thumb_lsl__reg,
		thumb_lsr__imm, thumb_lsr__reg,
		thumb_mov__imm, thumb_mov__reg,
		thumb_movs,
		thumb_mul,
		thumb_mvn,
		thumb_nop,
		thumb_orr,
		thumb_pop,
		thumb_push,
		thumb_rev,
		thumb_rev16,
		thumb_revsh,
		thumb_ror,
		thumb_rsb,
		thumb_sbc,
		thumb_setend,
		thumb_sev,
		thumb_stm,
		thumb_str__imm, thumb_str__imm_sp, thumb_str__reg,
		thumb_strb__imm, thumb_strb__reg,
		thumb_strh__imm, thumb_strh__reg,
		thumb_sub__imm, thumb_sub__imm_from_sp, thumb_sub__reg_imm, thumb_sub__reg_reg,
		thumb_svc,
		thumb_sxtb,
		thumb_sxth,
		thumb_tst,
		thumb_uxtb,
		thumb_uxth,
		thumb_wfe,
		thumb_wfi,
		thumb_yield,
		thumb_undefined,

		// 32-bit Thumb instructions
		thumb2_adc__imm, thumb2_adc__reg,
		thumb2_add__imm, thumb2_add__reg, /* thumb2_add__sp_imm, thumb2_add__sp_reg, */
		thumb2_addw, /* thumb2_addw__sp_imm, */
		thumb2_adr__sub, thumb2_adr__add,
		thumb2_and__imm, thumb2_and__reg,
		thumb2_asr__imm, thumb2_asr__reg,
		thumb2_b__cond, thumb2_b__uncond,
		thumb2_bfc,
		thumb2_bfi,
		thumb2_bfx,
		thumb2_bic__imm, thumb2_bic__reg,
		thumb2_bl,
		thumb2_blx,
		thumb2_bxj,
		thumb2_cdp,
		thumb2_cdp2,
		thumb2_clrex,
		thumb2_clz,
		thumb2_cmn__imm, thumb2_cmn__reg,
		thumb2_cmp__imm, thumb2_cmp__reg,
		thumb2_cps,
		thumb2_dbg,
		thumb2_dmb,
		thumb2_dsb,
		thumb2_eor__imm, thumb2_eor__reg,
		thumb2_enterx_leavex,
		thumb2_isb,
		thumb2_ldc,
		thumb2_ldm,
		thumb2_ldmdb,
		thumb2_ldr,
		thumb2_ldrd__imm, thumb2_ldrd__lit,
		thumb2_ldrex,
		thumb2_ldrexx,
		thumb2_lsl__imm, thumb2_lsl__reg,
		thumb2_lsr__imm, thumb2_lsr__reg,
		thumb2_mcr,
		thumb2_mcrr,
		thumb2_ml,
		thumb2_mov__imm, thumb2_mov__reg,
		thumb2_movx,
		thumb2_mrc,
		thumb2_mrrc,
		thumb2_mrs,
		thumb2_msr,
		thumb2_mul,
		thumb2_mvn__imm, thumb2_mvn__reg,
		thumb2_nop,
		thumb2_orn__imm, thumb2_orn__reg,
		thumb2_orr__imm, thumb2_orr__reg,
		thumb2_pkh,
		thumb2_pld,
		thumb2_pli,
		thumb2_pop__regs, thumb2_pop__reg,
		thumb2_push__regs, thumb2_push__reg,
		thumb2_qadd,
		thumb2_qsub,
		thumb2__r_dnm_math,
		thumb2_reverse,
		thumb2_rfe,
		thumb2_ror__imm, thumb2_ror__reg,
		thumb2_rrx,
		thumb2_rsb__imm, thumb2_rsb__reg,
		thumb2_sbc__imm, thumb2_sbc__reg,
		thumb2_sdiv,
		thumb2_sel,
		thumb2_sev,
		thumb2_smc,
		thumb2_smla,
		thumb2_smlad,
		thumb2_smlal,
		thumb2_smlald,
		thumb2_smlaw,
		thumb2_smlsd,
		thumb2_smlsld,
		thumb2_smmla,
		thumb2_smmls,
		thumb2_smmul,
		thumb2_smuad,
		thumb2_smul,
		thumb2_smull,
		thumb2_smulw,
		thumb2_smusd,
		thumb2_srs,
		thumb2_ssat,
		thumb2_ssat16,
		thumb2_stc,
		thumb2_stm,
		thumb2_stmdb,
		thumb2_str,
		thumb2_strd,
		thumb2_strex,
		thumb2_strexx,
		thumb2_sub__imm, thumb2_sub__reg,
		thumb2_subs,
		thumb2_subw,
		thumb2_sxtab,
		thumb2_sxtab16,
		thumb2_sxtah,
		thumb2_sxtb,
		thumb2_sxtb16,
		thumb2_sxth,
		thumb2_tb,
		thumb2_teq__imm, thumb2_teq__reg,
		thumb2_tst__imm, thumb2_tst__reg,
		thumb2_udiv,
		thumb2_umaal,
		thumb2_umlal,
		thumb2_umull,
		thumb2_usad8,
		thumb2_usada8,
		thumb2_usat,
		thumb2_usat16,
		thumb2_uxtab,
		thumb2_uxtab16,
		thumb2_uxtah,
		thumb2_uxtb,
		thumb2_uxtb16,
		thumb2_uxth,
		thumb2_vaba,
		thumb2_vabal,
		thumb2_vabd__int, thumb2_vabd__f32,
		thumb2_vabdl,
		thumb2_vabs, thumb2_vabs__f,
		thumb2_vacge_vacgt,
		thumb2_vadd__int, thumb2_vadd__f32, thumb2_vadd__fp_f, thumb2_vaddl_vaddw,
		thumb2_vaddhn,
		thumb2_vand,
		thumb2_vbic__reg,
		thumb2_vbif_vbit_vbsl_veor,
		thumb2_vceq__reg_int, thumb2_vceq__reg_f32, thumb2_vceq__imm0,
		thumb2_vcge__reg_int, thumb2_vcge__reg_f32, thumb2_vcge__imm0,
		thumb2_vcgt__reg_int, thumb2_vcgt__reg_f32, thumb2_vcgt__imm0,
		thumb2_vcle,
		thumb2_vcls,
		thumb2_vclt,
		thumb2_vclz,
		thumb2_vcmp__reg, thumb2_vcmp__to_0,
		thumb2_vcnt,
		thumb2_vcvt__fp_i_vec, thumb2_vcvt__fp_i_reg,
		thumb2_vcvt__fp_fix_vec, thumb2_vcvt__fp_fix_reg,
		thumb2_vcvt__dp_sp, thumb2_vcvt__hp_sp_vec, thumb2_vcvt__hp_sp_reg,
		thumb2_vdiv,
		thumb2_vdup__scalar, thumb2_vdup__reg,
		thumb2_vext,
		thumb2_vhadd_vhsub,
		thumb2_vld__multi,
		thumb2_vld__xlane,
		thumb2_vldm__64, thumb2_vldm__32,
		thumb2_vldr__64, thumb2_vldr__32,
		thumb2_vmax_vmin__int, thumb2_vmax_vmin__fp,
		thumb2_vml__int, thumb2_vml__int_long, thumb2_vml__f32,
		thumb2_vml__fp, thumb2_vml__scalar,
		thumb2_vmov_vbitwise, thumb2_vmov__imm, thumb2_vmov_vorr, thumb2_vmov__reg_f,
		thumb2_vmov_5, thumb2_vmov_6, thumb2_vmov_7, thumb2_vmov_8, thumb2_vmov_9,
		thumb2_vmovl,
		thumb2_vmovn,
		thumb2_vmrs,
		thumb2_vmsr,
		thumb2_vmul_1, thumb2_vmul__f32, thumb2_vmul__fp_2, thumb2_vmul__scalar,
		thumb2_vmull,
		thumb2_vmvn,
		thumb2_vneg, thumb2_vneg__f,
		thumb2_vnml,
		thumb2_vnmul,
		thumb2_vorn,
		thumb2_vpadal,
		thumb2_vpadd__int, thumb2_vpadd__f32,
		thumb2_vpaddl,
		thumb2_vpmax_vpmin__int, thumb2_vpmax_vpmin__fp,
		thumb2_vpop,
		thumb2_vpush,
		thumb2_vqabs,
		thumb2_vqadd,
		thumb2_vqdml__vec, thumb2_vqdml__scalar,
		thumb2_vqdmulh__vec, thumb2_vqdmulh__scalar,
		thumb2_vqdmull__vec, thumb2_vqdmull__scalar,
		thumb2_vqmov,
		thumb2_vqneg,
		thumb2_vqrdmulh__vec, thumb2_vqrdmulh__scalar,
		thumb2_vqrshl,
		thumb2_vqrshr,
		thumb2_vqshl__reg, thumb2_vqshl__imm,
		thumb2_vqshr,
		thumb2_vqsub,
		thumb2_vraddhn,
		thumb2_vrecpe,
		thumb2_vrecps,
		thumb2_vrev,
		thumb2_vrhadd,
		thumb2_vrshl,
		thumb2_vrshr,
		thumb2_vrshrn,
		thumb2_vrsqrte,
		thumb2_vrsqrts,
		thumb2_vrsra,
		thumb2_vrsubhn,
		thumb2_vshl__imm, thumb2_vshl__reg,
		thumb2_vshll__various, thumb2_vshll__max,
		thumb2_vshr,
		thumb2_vshrn,
		thumb2_vsli,
		thumb2_vsqrt,
		thumb2_vsra,
		thumb2_vsri,
		thumb2_vst__multi,
		thumb2_vst__xlane,
		thumb2_vstm__64, thumb2_vstm__32,
		thumb2_vstr__64, thumb2_vstr__32,
		thumb2_vsub__int, thumb2_vsub__f32, thumb2_vsub__fp_f, thumb2_vsubl_vsubw,
		thumb2_vsubhn,
		thumb2_vswp,
		thumb2_vtb,
		thumb2_vtrn,
		thumb2_vtst,
		thumb2_vuzp,
		thumb2_vzip,
		thumb2_wfe,
		thumb2_wfi,
		thumb2_yield,
		thumb2_undefined,

		// 16-bit ThumbEE instructions
		thumbEE_chka,
		thumbEE_hb,
		thumbEE_hblp,
		thumbEE_hbp,
		thumbEE_ldr_1,
		thumbEE_ldr_2,
		thumbEE_ldr_3,
		thumbEE_ldr_4,
		thumbEE_ldrh,
		thumbEE_ldrsh,
		thumbEE_str_1, thumbEE_str_2,
		thumbEE_strh,
		thumbEE_undefined,

		invalid
	};


	 // Reference manual citations (e.g., "A8.6.67") refer to sections in the ARM Architecture
	 // Reference Manual ARMv7-A and ARMv7-R Edition, Errata markup
	public static final OpcodeARM arm_opcode_table[] = {
		// A8.6.67 LDRD (literal)
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// ldrd<c> <Rt>,<Rt2>,<label>	ldrd<c> <Rt>,<Rt2>,[pc,#-0] Special case
		// Unpredictable if (1) is 0 or (0) is 1: xxxx000(1)x1(0)01111xxxxxxxx1101xxxx
		// must precede arm_ldrd__imm in table
		new OpcodeARM(Index.arm_ldrd__lit, "ldrd", "xxxx000xx1x01111xxxxxxxx1101xxxx"),
		// A8.6.66 LDRD (immediate)
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// ldrd<c> <Rt>,<Rt2>,[<Rn>{,#+/-<imm8>}]	ldrd<c> <Rt>,<Rt2>,[<Rn>],#+/-<imm8>	ldrd<c> <Rt>,<Rt2>,[<Rn>,#+/-<imm8>]!
		// must follow arm_ldrd__lit in table
		new OpcodeARM(Index.arm_ldrd__imm, "ldrd", "xxxx000xx1x0xxxxxxxxxxxx1101xxxx"),
		// A8.6.77 LDRHT
		// Encoding A1 ARMv6T2, ARMv7
		// ldrht<c> <Rt>, [<Rn>] {, #+/-<imm8>}
		// must precede arm_ldrh__imm in table
		new OpcodeARM(Index.arm_ldrht__imm, "ldrht", "xxxx0000x111xxxxxxxxxxxx1011xxxx"),
		// A8.6.75 LDRH (literal)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrh<c> <Rt>,<label>	ldrh<c> <Rt>,[pc,#-0] Special case
		// Unpredictable if (1) is 0 or (0) is 1: xxxx000(1)x1(0)11111xxxxxxxx1011xxxx
		// must precede arm_ldrh__imm in table
		new OpcodeARM(Index.arm_ldrh__lit, "ldrh", "xxxx000xx1x11111xxxxxxxx1011xxxx"),
		// A8.6.74 LDRH (immediate, ARM)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrh<c> <Rt>,[<Rn>{,#+/-<imm8>}]	ldrh<c> <Rt>,[<Rn>],#+/-<imm8>	ldrh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// must follow arm_ldrh__lit in table
		// must follow arm_ldrht__imm in table
		new OpcodeARM(Index.arm_ldrh__imm, "ldrh", "xxxx000xx1x1xxxxxxxxxxxx1011xxxx"),
		// A8.6.81 LDRSBT
		// Encoding A1 ARMv6T2, ARMv7
		// ldrsbt<c> <Rt>, [<Rn>] {, #+/-<imm8>}
		// must precede arm_ldrsb__imm in table
		new OpcodeARM(Index.arm_ldrsbt__imm, "ldrsbt", "xxxx0000x111xxxxxxxxxxxx1101xxxx"),
		// A8.6.79 LDRSB (literal)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrsb<c> <Rt>,<label>	ldrsb<c> <Rt>,[pc,#-0] Special case
		// Unpredictable if (1) is 0 or (0) is 1: xxxx000(1)x1(0)11111xxxxxxxx1101xxxx
		// must precede arm_ldrsb__imm in table
		new OpcodeARM(Index.arm_ldrsb__lit, "ldrsb", "xxxx000xx1x11111xxxxxxxx1101xxxx"),
		// A8.6.78 LDRSB (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrsb<c> <Rt>,[<Rn>{,#+/-<imm8>}]	ldrsb<c> <Rt>,[<Rn>],#+/-<imm8>	ldrsb<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// must follow arm_ldrsb__lit in table
		// must follow arm_ldrsbt__imm in table
		new OpcodeARM(Index.arm_ldrsb__imm, "ldrsb", "xxxx000xx1x1xxxxxxxxxxxx1101xxxx"),
		// A8.6.85 LDRSHT
		// Encoding A1 ARMv6T2, ARMv7
		// ldrsht<c> <Rt>, [<Rn>] {, #+/-<imm8>}
		// must precede arm_ldrsh__imm in table
		new OpcodeARM(Index.arm_ldrsht__imm, "ldrsht", "xxxx0000x111xxxxxxxxxxxx1111xxxx"),
		// A8.6.83 LDRSH (literal)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrsh<c> <Rt>,<label>	ldrsh<c> <Rt>,[pc,#-0] Special case
		// Unpredictable if (1) is 0 or (0) is 1: xxxx000(1)x1(0)11111xxxxxxxx1111xxxx
		// must precede arm_ldrsh__imm in table
		new OpcodeARM(Index.arm_ldrsh__lit, "ldrsh", "xxxx000xx1x11111xxxxxxxx1111xxxx"),
		// A8.6.82 LDRSH (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrsh<c> <Rt>,[<Rn>{,#+/-<imm8>}]	ldrsh<c> <Rt>,[<Rn>],#+/-<imm8>	ldrsh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// must follow arm_ldrsh__lit in table
		// must follow arm_ldrsht__imm in table
		new OpcodeARM(Index.arm_ldrsh__imm, "ldrsh", "xxxx000xx1x1xxxxxxxxxxxx1111xxxx"),
		// A8.6.200 STRD (immediate)
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// strd<c> <Rt>,<Rt2>,[<Rn>{,#+/-<imm8>}]	strd<c> <Rt>,<Rt2>,[<Rn>],#+/-<imm8>	strd<c> <Rt>,<Rt2>,[<Rn>,#+/-<imm8>]!
		new OpcodeARM(Index.arm_strd__imm, "strd", "xxxx000xx1x0xxxxxxxxxxxx1111xxxx"),
		// A8.6.209 STRHT
		// Encoding A1 ARMv6T2, ARMv7
		// strht<c> <Rt>, [<Rn>] {, #+/-<imm8>}
		// must precede arm_strh__imm in table
		new OpcodeARM(Index.arm_strht__imm, "strht", "xxxx0000x110xxxxxxxxxxxx1011xxxx"),
		// A8.6.207 STRH (immediate, ARM)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// strh<c> <Rt>,[<Rn>{,#+/-<imm8>}]	strh<c> <Rt>,[<Rn>],#+/-<imm8>	strh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// must follow arm_strht__imm in table
		new OpcodeARM(Index.arm_strh__imm, "strh", "xxxx000xx1x0xxxxxxxxxxxx1011xxxx"),
		// A8.6.1 ADC (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// adc{s}<c> <Rd>,<Rn>,#<const>
		new OpcodeARM(Index.arm_adc__imm, "adc", "xxxx0010101xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.2 ADC (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// adc{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		new OpcodeARM(Index.arm_adc__reg, "adc", "xxxx0000101xxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.3 ADC (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// adc{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		new OpcodeARM(Index.arm_adc__rsr, "adc", "xxxx0000101xxxxxxxxxxxxx0xx1xxxx"),
		// A8.6.10 ADR
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// adr<c> <Rd>,<label>	add<c> <Rd>,pc,#<const>	Alternative form
		// must precede arm_add__imm in table
		new OpcodeARM(Index.arm_adr__higher, "add", "xxxx001010001111xxxxxxxxxxxxxxxx"),
		// A8.6.5 ADD (immediate, ARM)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// add{s}<c> <Rd>,<Rn>,#<const>
		// A8.6.8 ADD (SP plus immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// add{s}<c> <Rd>,sp,#<const>
		//
		// must follow arm_adr__higher in table
		new OpcodeARM(Index.arm_add__imm, "add", "xxxx0010100xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.6 ADD (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// add{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// A8.6.9 ADD (SP plus register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// add{s}<c> <Rd>,sp,<Rm>{,<shift>}
		//
		new OpcodeARM(Index.arm_add__reg, "add", "xxxx0000100xxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.7 ADD (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// add{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		new OpcodeARM(Index.arm_add__rsr, "add", "xxxx0000100xxxxxxxxxxxxx0xx1xxxx"),
		// A8.6.11 AND (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// and{s}<c> <Rd>,<Rn>,#<const>
		new OpcodeARM(Index.arm_and__imm, "and", "xxxx0010000xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.12 AND (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// and{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		new OpcodeARM(Index.arm_and__reg, "and", "xxxx0000000xxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.13 AND (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// and{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		new OpcodeARM(Index.arm_and__rsr, "and", "xxxx0000000xxxxxxxxxxxxx0xx1xxxx"),
		// A8.6.14 ASR (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// asr{s}<c> <Rd>,<Rm>,#<imm>
		// Unpredictable if (0) is 1: xxxx0001101x(0)(0)(0)(0)xxxxxxxxx100xxxx
		new OpcodeARM(Index.arm_asr__imm, "asr", "xxxx0001101xxxxxxxxxxxxxx100xxxx"),
		// A8.6.15 ASR (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// asr{s}<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (0) is 1: xxxx0001101x(0)(0)(0)(0)xxxxxxxx0101xxxx
		new OpcodeARM(Index.arm_asr__reg, "asr", "xxxx0001101xxxxxxxxxxxxx0101xxxx"),
		// A8.6.23 BL, BLX (immediate)
		// Encoding A2 ARMv5T*, ARMv6*, ARMv7
		// blx <label>
		// must precede arm_b in table
		new OpcodeARM(Index.arm_blx__imm, "blx", "1111101xxxxxxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.16 B
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// b<c> <label>
		// must follow arm_blx__imm in table
		new OpcodeARM(Index.arm_b, "b", "xxxx1010xxxxxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.17 BFC
		// Encoding A1 ARMv6T2, ARMv7
		// bfc<c> <Rd>,#<lsb>,#<width>
		// must precede arm_bfi in table
		new OpcodeARM(Index.arm_bfc, "bfc", "xxxx0111110xxxxxxxxxxxxxx0011111"),
		// A8.6.18 BFI
		// Encoding A1 ARMv6T2, ARMv7
		// bfi<c> <Rd>,<Rn>,#<lsb>,#<width>
		// must follow arm_bfc in table
		new OpcodeARM(Index.arm_bfi, "bfi", "xxxx0111110xxxxxxxxxxxxxx001xxxx"),
		// A8.6.20 BIC (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// bic{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		new OpcodeARM(Index.arm_bic__reg, "bic", "xxxx0001110xxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.21 BIC (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// bic{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		new OpcodeARM(Index.arm_bic__rsr, "bic", "xxxx0001110xxxxxxxxxxxxx0xx1xxxx"),
		// A8.6.22 BKPT
		// Encoding A1 ARMv5T*, ARMv6*, ARMv7
		// bkpt #<imm16>
		new OpcodeARM(Index.arm_bkpt, "bkpt", "xxxx00010010xxxxxxxxxxxx0111xxxx"),
		// A8.6.23 BL, BLX (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// bl<c> <label>
		new OpcodeARM(Index.arm_bl, "bl", "xxxx1011xxxxxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.24 BLX (register)
		// Encoding A1 ARMv5T*, ARMv6*, ARMv7
		// blx<c> <Rm>
		// Unpredictable if (1) is 0: xxxx00010010(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)0011xxxx
		new OpcodeARM(Index.arm_blx__reg, "blx", "xxxx00010010xxxxxxxxxxxx0011xxxx"),
		// A8.6.25 BX
		// Encoding A1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// bx<c> Rm
		// Unpredictable if (1) is 0: xxxx00010010(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)0001xxxx
		new OpcodeARM(Index.arm_bx, "bx", "xxxx00010010xxxxxxxxxxxx0001xxxx"),
		// A8.6.26 BXJ
		// Encoding A1 ARMv5TEJ, ARMv6*, ARMv7
		// bxj<c> <Rm>
		// Unpredictable if (1) is 0: xxxx00010010(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)0010xxxx
		new OpcodeARM(Index.arm_bxj, "bxj", "xxxx00010010xxxxxxxxxxxx0010xxxx"),
		// A8.6.30 CLREX
		// Encoding A1 ARMv6K, ARMv7
		// clrex
		// Unpredictable if (1) is 0 or (0) is 1: 111101010111(1)(1)(1)(1)(1)(1)(1)(1)(0)(0)(0)(0)0001(1)(1)(1)(1)
		new OpcodeARM(Index.arm_clrex, "clrex", "111101010111xxxxxxxxxxxx0001xxxx"),
		// A8.6.31 CLZ
		// Encoding A1 ARMv5T*, ARMv6*, ARMv7
		// clz<c> <Rd>,<Rm>
		// Unpredictable if (1) is 0: xxxx00010110(1)(1)(1)(1)xxxx(1)(1)(1)(1)0001xxxx
		new OpcodeARM(Index.arm_clz, "clz", "xxxx00010110xxxxxxxxxxxx0001xxxx"),
		// A8.6.33 CMN (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// cmn<c> <Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: xxxx00010111xxxx(0)(0)(0)(0)xxxxxxx0xxxx
		new OpcodeARM(Index.arm_cmn__reg, "cmn", "xxxx00010111xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.34 CMN (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// cmn<c> <Rn>,<Rm>,<type> <Rs>
		// Unpredictable if (0) is 1: xxxx00010111xxxx(0)(0)(0)(0)xxxx0xx1xxxx
		new OpcodeARM(Index.arm_cmn__rsr, "cmn", "xxxx00010111xxxxxxxxxxxx0xx1xxxx"),
		// A8.6.36 CMP (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// cmp<c> <Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: xxxx00010101xxxx(0)(0)(0)(0)xxxxxxx0xxxx
		new OpcodeARM(Index.arm_cmp__reg, "cmp", "xxxx00010101xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.37 CMP (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// cmp<c> <Rn>,<Rm>,<type> <Rs>
		// Unpredictable if (0) is 1: xxxx00010101xxxx(0)(0)(0)(0)xxxx0xx1xxxx
		new OpcodeARM(Index.arm_cmp__rsr, "cmp", "xxxx00010101xxxxxxxxxxxx0xx1xxxx"),
		// A8.6.40 DBG
		// Encoding A1 ARMv7 (executes as NOP in ARMv6Kand ARMv6T2)
		// dbg<c> #<option>
		// Unpredictable if (1) is 0 or (0) is 1: xxxx001100100000(1)(1)(1)(1)(0)(0)(0)(0)1111xxxx
		new OpcodeARM(Index.arm_dbg, "dbg", "xxxx001100100000xxxxxxxx1111xxxx"),
		// A8.6.41 DMB
		// Encoding A1 ARMv7
		// dmb #<option>
		// Unpredictable if (1) is 0 or (0) is 1: 111101010111(1)(1)(1)(1)(1)(1)(1)(1)(0)(0)(0)(0)0101xxxx
		new OpcodeARM(Index.arm_dmb, "dmb", "111101010111xxxxxxxxxxxx0101xxxx"),
		// A8.6.42 DSB
		// Encoding A1 ARMv7
		// dsb #<option>
		// Unpredictable if (1) is 0 or (0) is 1: 111101010111(1)(1)(1)(1)(1)(1)(1)(1)(0)(0)(0)(0)0100xxxx
		new OpcodeARM(Index.arm_dsb, "dsb", "111101010111xxxxxxxxxxxx0100xxxx"),
		// A8.6.44 EOR (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// eor{s}<c> <Rd>,<Rn>,#<const>
		new OpcodeARM(Index.arm_eor__imm, "eor", "xxxx0010001xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.45 EOR (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// eor{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		new OpcodeARM(Index.arm_eor__reg, "eor", "xxxx0000001xxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.46 EOR (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// eor{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		new OpcodeARM(Index.arm_eor__rsr, "eor", "xxxx0000001xxxxxxxxxxxxx0xx1xxxx"),
		// A8.6.49 ISB
		// Encoding A1 ARMv7
		// isb #<option>
		// Unpredictable if (1) is 0 or (0) is 1: 111101010111(1)(1)(1)(1)(1)(1)(1)(1)(0)(0)(0)(0)0110xxxx
		new OpcodeARM(Index.arm_isb, "isb", "111101010111xxxxxxxxxxxx0110xxxx"),
		// B6.1.8 RFE
		// Encoding A1 ARMv6*, ARMv7
		// rfe{<amode>} <Rn>{!}
		// Unpredictable if (1) is 0 or (0) is 1: 1111100xx0x1xxxx(0)(0)(0)(0)(1)(0)(1)(0)(0)(0)(0)(0)(0)(0)(0)(0)
		// must precede arm_ldm__exc_ret in table
		new OpcodeARM(Index.arm_rfe, "rfe", "1111100xx0x1xxxxxxxxxxxxxxxxxxxx"),
		// B6.1.2 LDM (exception return)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldm{<amode>}<c> <Rn>{!},<registers_with_pc>^
		// must follow arm_rfe in table
		new OpcodeARM(Index.arm_ldm__exc_ret, "ldm", "xxxx100xx1x1xxxx1xxxxxxxxxxxxxxx"),
		// B6.1.3 LDM (user registers)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldm{<amode>}<c> <Rn>,<registers_without_pc>^
		// Unpredictable if (0) is 1: xxxx100xx1(0)1xxxx0xxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_ldm__user_reg, "ldm", "xxxx100xx1x1xxxx0xxxxxxxxxxxxxxx"),
		// A8.6.122 POP
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// pop<c> <registers> <registers> contains more than one register
		// must precede arm_ldm in table
		new OpcodeARM(Index.arm_pop__regs, "pop", "xxxx100010111101xxxxxxxxxxxxxxxx"),
		// A8.6.53 LDM / LDMIA / LDMFD
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldm<c> <Rn>{!},<registers>
		// must follow arm_pop__regs in table
		new OpcodeARM(Index.arm_ldm, "ldm", "xxxx100010x1xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.54 LDMDA / LDMFA
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldmda<c> <Rn>{!},<registers>
		new OpcodeARM(Index.arm_ldmda, "ldmda", "xxxx100000x1xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.55 LDMDB / LDMEA
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldmdb<c> <Rn>{!},<registers>
		new OpcodeARM(Index.arm_ldmdb, "ldmdb", "xxxx100100x1xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.56 LDMIB / LDMED
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldmib<c> <Rn>{!},<registers>
		new OpcodeARM(Index.arm_ldmib, "ldmib", "xxxx100110x1xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.122 POP
		// Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// pop<c> <registers> <registers> contains one register, <Rt>
		// must precede arm_ldr__imm in table
		new OpcodeARM(Index.arm_pop__reg, "pop", "xxxx010010011101xxxx000000000100"),
		// A8.6.118 PLD (literal)
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// pld <label>	pld [pc,#-0] Special case
		// Unpredictable if (1) is 0: 11110101x(1)011111(1)(1)(1)(1)xxxxxxxxxxxx
		// must precede arm_ldr__imm in table
		// must precede arm_pld__imm in table
		new OpcodeARM(Index.arm_pld__lit, "pld", "11110101xx011111xxxxxxxxxxxxxxxx"),
		// A8.6.117 PLD, PLDW (immediate)
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7 for PLD; ARMv7 with MP Extensions for PLDW
		// pld{w} [<Rn>,#+/-<imm12>]
		// Unpredictable if (1) is 0: 11110101xx01xxxx(1)(1)(1)(1)xxxxxxxxxxxx
		// must follow arm_pld__lit in table
		// must precede arm_ldr__imm in table
		// must precede arm_ldrbt__imm in table
		// must precede arm_ldrb__lit in table
		// must precede arm_ldrb__imm in table
		new OpcodeARM(Index.arm_pld__imm, "pld", "11110101xx01xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.120 PLI (immediate, literal)
		// Encoding A1 ARMv7
		// pli [<Rn>,#+/-<imm12>]	pli <label>	pli [pc,#-0] Special case
		// Unpredictable if (1) is 0: 11110100x101xxxx(1)(1)(1)(1)xxxxxxxxxxxx
		// must precede arm_ldr__imm in table
		// must precede arm_ldrbt__imm in table
		// must precede arm_ldrb__lit in table
		// must precede arm_ldrb__imm in table
		new OpcodeARM(Index.arm_pli__imm_lit, "pli", "11110100x101xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.86 LDRT
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrt<c> <Rt>, [<Rn>] {, #+/-<imm12>}
		// must precede arm_ldr__lit in table
		// must follow arm_pld__lit in table
		// must follow arm_pld__imm in table
		// must follow arm_pli__imm_lit in table
		new OpcodeARM(Index.arm_ldrt__imm, "ldrt", "xxxx0100x011xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.59 LDR (literal)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldr<c> <Rt>,<label>	ldr<c> <Rt>,[pc,#-0] Special case
		// Unpredictable if (1) is 0 or (0) is 1: xxxx010(1)x0(0)11111xxxxxxxxxxxxxxxx
		// must precede arm_ldr__imm in table
		// must follow arm_ldrt__imm in table
		// must follow arm_pld__lit in table
		// must follow arm_pld__imm in table
		// must follow arm_pli__imm_lit in table
		new OpcodeARM(Index.arm_ldr__lit, "ldr", "xxxx010xx0x11111xxxxxxxxxxxxxxxx"),
		// A8.6.58 LDR (immediate, ARM)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldr<c> <Rt>,[<Rn>{,#+/-<imm12>}]	ldr<c> <Rt>,[<Rn>],#+/-<imm12>	ldr<c> <Rt>,[<Rn>,#+/-<imm12>]!
		// must follow arm_ldr__lit in table
		// must follow arm_pop__reg in table
		// must follow arm_pld__lit in table
		// must follow arm_pld__imm in table
		// must follow arm_pli__imm_lit in table
		new OpcodeARM(Index.arm_ldr__imm, "ldr", "xxxx010xx0x1xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.86 LDRT
		// Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrt<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// must precede arm_ldr__reg in table
		new OpcodeARM(Index.arm_ldrt__reg, "ldrt", "xxxx0110x011xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.119 PLD, PLDW (register)
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7 for PLD; ARMv7 with MP Extensions for PLDW
		// pld{w}<c> [<Rn>,+/-<Rm>{, <shift>}]
		// Unpredictable if (1) is 0: 11110111xx01xxxx(1)(1)(1)(1)xxxxxxx0xxxx
		// must precede arm_ldrb__reg in table
		// must precede arm_ldrt__reg in table
		new OpcodeARM(Index.arm_pld__reg, "pld", "11110111xx01xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.121 PLI (register)
		// Encoding A1 ARMv7
		// pli [<Rn>,+/-<Rm>{, <shift>}]
		// Unpredictable if (1) is 0: 11110110x101xxxx(1)(1)(1)(1)xxxxxxx0xxxx
		// must precede arm_ldrb__reg in table
		// must precede arm_ldrt__reg in table
		new OpcodeARM(Index.arm_pli__reg, "pli", "11110110x101xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.60 LDR (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldr<c> <Rt>,[<Rn>,+/-<Rm>{, <shift>}]{!}	ldr<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// must follow arm_ldrt__reg in table
		// must follow arm_pld__reg in table
		// must follow arm_pli__reg in table
		new OpcodeARM(Index.arm_ldr__reg, "ldr", "xxxx011xx0x1xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.65 LDRBT
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrbt<c> <Rt>,[<Rn>],#+/-<imm12>
		// must precede arm_ldrb__imm in table
		// must follow arm_pld__imm in table
		new OpcodeARM(Index.arm_ldrbt__imm, "ldrbt", "xxxx0100x111xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.63 LDRB (literal)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrb<c> <Rt>,<label>	ldrb<c> <Rt>,[pc,#-0] Special case
		// Unpredictable if (1) is 0 or (0) is 1: xxxx010(1)x1(0)11111xxxxxxxxxxxxxxxx
		// must precede arm_ldrb__imm in table
		// must follow arm_pld__imm in table
		new OpcodeARM(Index.arm_ldrb__lit, "ldrb", "xxxx010xx1x11111xxxxxxxxxxxxxxxx"),
		// A8.6.62 LDRB (immediate, ARM)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrb<c> <Rt>,[<Rn>{,#+/-<imm12>}]	ldrb<c> <Rt>,[<Rn>],#+/-<imm12>	ldrb<c> <Rt>,[<Rn>,#+/-<imm12>]!
		// must follow arm_ldrb__lit in table
		// must follow arm_ldrbt__imm in table
		// must follow arm_pld__imm in table
		new OpcodeARM(Index.arm_ldrb__imm, "ldrb", "xxxx010xx1x1xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.65 LDRBT
		// Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrbt<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// must precede arm_ldrb__reg in table
		new OpcodeARM(Index.arm_ldrbt__reg, "ldrbt", "xxxx0110x111xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.64 LDRB (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrb<c> <Rt>,[<Rn>,+/-<Rm>{, <shift>}]{!}	ldrb<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// must follow arm_ldrbt__reg in table
		// must follow arm_pld__reg in table
		new OpcodeARM(Index.arm_ldrb__reg, "ldrb", "xxxx011xx1x1xxxxxxxxxxxxxxx0xxxx"),
		// B6.1.1 CPS
		// Encoding A1 ARMv6*, ARMv7
		// cps<effect> <iflags>{,#<mode>}	cps #<mode>
		// Unpredictable if (0) is 1: 111100010000xxx0(0)(0)(0)(0)(0)(0)(0)xxx0xxxxx
		// must precede arm_mrs in table
		// must precede arm_ldrd__reg in table
		new OpcodeARM(Index.arm_cps, "cps", "111100010000xxx0xxxxxxxxxx0xxxxx"),
		// A8.6.68 LDRD (register)
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// ldrd<c> <Rt>,<Rt2>,[<Rn>,+/-<Rm>]{!}	ldrd<c> <Rt>,<Rt2>,[<Rn>],+/-<Rm>
		// Unpredictable if (0) is 1: xxxx000xx0x0xxxxxxxx(0)(0)(0)(0)1101xxxx
		// must follow arm_cps in table
		new OpcodeARM(Index.arm_ldrd__reg, "ldrd", "xxxx000xx0x0xxxxxxxxxxxx1101xxxx"),
		// A8.6.69 LDREX
		// Encoding A1 ARMv6*, ARMv7
		// ldrex<c> <Rt>,[<Rn>]
		// Unpredictable if (1) is 0: xxxx00011001xxxxxxxx(1)(1)(1)(1)1001(1)(1)(1)(1)
		new OpcodeARM(Index.arm_ldrex, "ldrex", "xxxx00011001xxxxxxxxxxxx1001xxxx"),
		// A8.6.70 LDREXB
		// Encoding A1 ARMv6K, ARMv7
		// ldrexb<c> <Rt>, [<Rn>]
		// Unpredictable if (1) is 0: xxxx00011101xxxxxxxx(1)(1)(1)(1)1001(1)(1)(1)(1)
		new OpcodeARM(Index.arm_ldrexb, "ldrexb", "xxxx00011101xxxxxxxxxxxx1001xxxx"),
		// A8.6.71 LDREXD
		// Encoding A1 ARMv6K, ARMv7
		// ldrexd<c> <Rt>,<Rt2>,[<Rn>]
		// Unpredictable if (1) is 0: xxxx00011011xxxxxxxx(1)(1)(1)(1)1001(1)(1)(1)(1)
		new OpcodeARM(Index.arm_ldrexd, "ldrexd", "xxxx00011011xxxxxxxxxxxx1001xxxx"),
		// A8.6.72 LDREXH
		// Encoding A1 ARMv6K, ARMv7
		// ldrexh<c> <Rt>, [<Rn>]
		// Unpredictable if (1) is 0: xxxx00011111xxxxxxxx(1)(1)(1)(1)1001(1)(1)(1)(1)
		new OpcodeARM(Index.arm_ldrexh, "ldrexh", "xxxx00011111xxxxxxxxxxxx1001xxxx"),
		// A8.6.77 LDRHT
		// Encoding A2 ARMv6T2, ARMv7
		// ldrht<c> <Rt>, [<Rn>], +/-<Rm>
		// Unpredictable if (0) is 1: xxxx0000x011xxxxxxxx(0)(0)(0)(0)1011xxxx
		// must precede arm_ldrh__reg in table
		new OpcodeARM(Index.arm_ldrht__reg, "ldrht", "xxxx0000x011xxxxxxxxxxxx1011xxxx"),
		// A8.6.76 LDRH (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrh<c> <Rt>,[<Rn>,+/-<Rm>]{!}	ldrh<c> <Rt>,[<Rn>],+/-<Rm>
		// Unpredictable if (0) is 1: xxxx000xx0x1xxxxxxxx(0)(0)(0)(0)1011xxxx
		// must follow arm_ldrht__reg in table
		new OpcodeARM(Index.arm_ldrh__reg, "ldrh", "xxxx000xx0x1xxxxxxxxxxxx1011xxxx"),
		// A8.6.81 LDRSBT
		// Encoding A2 ARMv6T2, ARMv7
		// ldrsbt<c> <Rt>, [<Rn>], +/-<Rm>
		// Unpredictable if (0) is 1: xxxx0000x011xxxxxxxx(0)(0)(0)(0)1101xxxx
		// must precede arm_ldrsb__reg in table
		new OpcodeARM(Index.arm_ldrsbt__reg, "ldrsbt", "xxxx0000x011xxxxxxxxxxxx1101xxxx"),
		// A8.6.80 LDRSB (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrsb<c> <Rt>,[<Rn>,+/-<Rm>]{!}	ldrsb<c> <Rt>,[<Rn>],+/-<Rm>
		// Unpredictable if (0) is 1: xxxx000xx0x1xxxxxxxx(0)(0)(0)(0)1101xxxx
		// must follow arm_ldrsbt__reg in table
		new OpcodeARM(Index.arm_ldrsb__reg, "ldrsb", "xxxx000xx0x1xxxxxxxxxxxx1101xxxx"),
		// A8.6.85 LDRSHT
		// Encoding A2 ARMv6T2, ARMv7
		// ldrsht<c> <Rt>, [<Rn>], +/-<Rm>
		// Unpredictable if (0) is 1: xxxx0000x011xxxxxxxx(0)(0)(0)(0)1111xxxx
		// must precede arm_ldrsh__reg in table
		new OpcodeARM(Index.arm_ldrsht__reg, "ldrsht", "xxxx0000x011xxxxxxxxxxxx1111xxxx"),
		// A8.6.84 LDRSH (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldrsh<c> <Rt>,[<Rn>,+/-<Rm>]{!}	ldrsh<c> <Rt>,[<Rn>],+/-<Rm>
		// Unpredictable if (0) is 1: xxxx000xx0x1xxxxxxxx(0)(0)(0)(0)1111xxxx
		// must follow arm_ldrsht__reg in table
		new OpcodeARM(Index.arm_ldrsh__reg, "ldrsh", "xxxx000xx0x1xxxxxxxxxxxx1111xxxx"),
		// A8.6.89 LSL (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// lsl{s}<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (0) is 1: xxxx0001101x(0)(0)(0)(0)xxxxxxxx0001xxxx
		new OpcodeARM(Index.arm_lsl__reg, "lsl", "xxxx0001101xxxxxxxxxxxxx0001xxxx"),
		// A8.6.90 LSR (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// lsr{s}<c> <Rd>,<Rm>,#<imm>
		// Unpredictable if (0) is 1: xxxx0001101x(0)(0)(0)(0)xxxxxxxxx010xxxx
		new OpcodeARM(Index.arm_lsr__imm, "lsr", "xxxx0001101xxxxxxxxxxxxxx010xxxx"),
		// A8.6.91 LSR (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// lsr{s}<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (0) is 1: xxxx0001101x(0)(0)(0)(0)xxxxxxxx0011xxxx
		new OpcodeARM(Index.arm_lsr__reg, "lsr", "xxxx0001101xxxxxxxxxxxxx0011xxxx"),
		// A8.6.94 MLA
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// mla{s}<c> <Rd>,<Rn>,<Rm>,<Ra>
		new OpcodeARM(Index.arm_mla, "mla", "xxxx0000001xxxxxxxxxxxxx1001xxxx"),
		// A8.6.95 MLS
		// Encoding A1 ARMv6T2, ARMv7
		// mls<c> <Rd>,<Rn>,<Rm>,<Ra>
		new OpcodeARM(Index.arm_mls, "mls", "xxxx00000110xxxxxxxxxxxx1001xxxx"),
		// A8.6.96 MOV (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// mov{s}<c> <Rd>,#<const>
		// Unpredictable if (0) is 1: xxxx0011101x(0)(0)(0)(0)xxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_mov__imm, "mov", "xxxx0011101xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.97 MOV (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// mov{s}<c> <Rd>,<Rm>
		// Unpredictable if (0) is 1: xxxx0001101x(0)(0)(0)(0)xxxx00000000xxxx
		// must precede arm_lsl__imm in table
		new OpcodeARM(Index.arm_mov__reg, "mov", "xxxx0001101xxxxxxxxx00000000xxxx"),
		// A8.6.88 LSL (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// lsl{s}<c> <Rd>,<Rm>,#<imm>
		// Unpredictable if (0) is 1: xxxx0001101x(0)(0)(0)(0)xxxxxxxxx000xxxx
		// must follow arm_mov__reg in table
		new OpcodeARM(Index.arm_lsl__imm, "lsl", "xxxx0001101xxxxxxxxxxxxxx000xxxx"),
		// A8.6.96 MOV (immediate)
		// Encoding A2 ARMv6T2, ARMv7
		// movw<c> <Rd>,#<imm16>
		new OpcodeARM(Index.arm_movw, "movw", "xxxx00110000xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.157 SETEND
		// Encoding A1 ARMv6*, ARMv7
		// setend <endian_specifier> Cannot be conditional
		// Unpredictable if (0) is 1: 111100010000(0)(0)(0)1(0)(0)(0)(0)(0)(0)x(0)0000(0)(0)(0)(0)
		// must precede arm_mrs in table
		new OpcodeARM(Index.arm_setend, "setend", "111100010000xxx1xxxxxxxx0000xxxx"),
		// A8.6.102 MRS
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// mrs<c> <Rd>,<spec_reg>
		// Unpredictable if (1) is 0 or (0) is 1: xxxx00010000(1)(1)(1)(1)xxxx(0)(0)(0)(0)0000(0)(0)(0)(0)
		// B6.1.5 MRS
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// mrs<c> <Rd>,<spec_reg>
		// Unpredictable if (1) is 0 or (0) is 1: xxxx00010x00(1)(1)(1)(1)xxxx(0)(0)(0)(0)0000(0)(0)(0)(0)
		//
		// combined A8.6.102 MRS and B6.1.5 MRS, using the bit pattern of B6.1.5 MRS
		// must follow arm_cps in table
		// must follow arm_setend in table
		new OpcodeARM(Index.arm_mrs, "mrs", "xxxx00010x00xxxxxxxxxxxx0000xxxx"),
		// A8.6.158 SEV
		// Encoding A1 ARMv6K, ARMv7 (executes as NOP in ARMv6T2)
		// sev<c>
		// Unpredictable if (1) is 0 or (0) is 1: xxxx001100100000(1)(1)(1)(1)(0)(0)(0)(0)00000100
		// must precede arm_msr__imm in table
		new OpcodeARM(Index.arm_sev, "sev", "xxxx001100100000xxxxxxxx00000100"),
		// A8.6.110 NOP
		// Encoding A1 ARMv6K, ARMv6T2, ARMv7
		// nop<c>
		// Unpredictable if (1) is 0 or (0) is 1: xxxx001100100000(1)(1)(1)(1)(0)(0)(0)(0)00000000
		// must precede arm_msr__imm in table
		new OpcodeARM(Index.arm_nop, "nop", "xxxx001100100000xxxxxxxx00000000"),
		// A8.6.411 WFE
		// Encoding A1 ARMv6K, ARMv7 (executes as NOP in ARMv6T2)
		// wfe<c>
		// Unpredictable if (1) is 0 or (0) is 1: xxxx001100100000(1)(1)(1)(1)(0)(0)(0)(0)00000010
		// must precede arm_msr__imm in table
		new OpcodeARM(Index.arm_wfe, "wfe", "xxxx001100100000xxxxxxxx00000010"),
		// A8.6.412 WFI
		// Encoding A1 ARMv6K, ARMv7 (executes as NOP in ARMv6T2)
		// wfi<c>
		// Unpredictable if (1) is 0 or (0) is 1: xxxx001100100000(1)(1)(1)(1)(0)(0)(0)(0)00000011
		// must precede arm_msr__imm in table
		new OpcodeARM(Index.arm_wfi, "wfi", "xxxx001100100000xxxxxxxx00000011"),
		// A8.6.413 YIELD
		// Encoding A1 ARMv6K, ARMv7 (executes as NOP in ARMv6T2)
		// yield<c>
		// Unpredictable if (1) is 0 or (0) is 1: xxxx001100100000(1)(1)(1)(1)(0)(0)(0)(0)00000001
		// must precede arm_msr__imm in table
		new OpcodeARM(Index.arm_yield, "yield", "xxxx001100100000xxxxxxxx00000001"),
		// A8.6.104 MSR (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// msr<c> <spec_reg>,<Rn>
		// Unpredictable if (1) is 0 or (0) is 1: xxxx00010010xx00(1)(1)(1)(1)(0)(0)(0)(0)0000xxxx
		// must precede arm_msr__sys_reg in table
		new OpcodeARM(Index.arm_msr__reg, "msr", "xxxx00010010xx00xxxxxxxx0000xxxx"),
		// B6.1.7 MSR (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// msr<c> <spec_reg>,<Rn>
		// Unpredictable if (1) is 0 or (0) is 1: xxxx00010x10xxxx(1)(1)(1)(1)(0)(0)(0)(0)0000xxxx
		// must follow arm_msr__reg in table in table
		new OpcodeARM(Index.arm_msr__sys_reg, "msr", "xxxx00010x10xxxxxxxxxxxx0000xxxx"),
		// A8.6.105 MUL
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// mul{s}<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (0) is 1: xxxx0000000xxxxx(0)(0)(0)(0)xxxx1001xxxx
		new OpcodeARM(Index.arm_mul, "mul", "xxxx0000000xxxxxxxxxxxxx1001xxxx"),
		// A8.6.107 MVN (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// mvn{s}<c> <Rd>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: xxxx0001111x(0)(0)(0)(0)xxxxxxxxxxx0xxxx
		new OpcodeARM(Index.arm_mvn__reg, "mvn", "xxxx0001111xxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.108 MVN (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// mvn{s}<c> <Rd>,<Rm>,<type> <Rs>
		// Unpredictable if (0) is 1: xxxx0001111x(0)(0)(0)(0)xxxxxxxx0xx1xxxx
		new OpcodeARM(Index.arm_mvn__rsr, "mvn", "xxxx0001111xxxxxxxxxxxxx0xx1xxxx"),
		// A8.6.113 ORR (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// orr{s}<c> <Rd>,<Rn>,#<const>
		new OpcodeARM(Index.arm_orr__imm, "orr", "xxxx0011100xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.114 ORR (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// orr{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		new OpcodeARM(Index.arm_orr__reg, "orr", "xxxx0001100xxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.115 ORR (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// orr{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		new OpcodeARM(Index.arm_orr__rsr, "orr", "xxxx0001100xxxxxxxxxxxxx0xx1xxxx"),
		// A8.6.116 PKH
		// Encoding A1 ARMv6*, ARMv7
		// pkhbt<c> <Rd>,<Rn>,<Rm>{,lsl #<imm>}	pkhtb<c> <Rd>,<Rn>,<Rm>{,asr #<imm>}
		new OpcodeARM(Index.arm_pkh, "pkh", "xxxx01101000xxxxxxxxxxxxxx01xxxx"),
		// A8.6.124 QADD
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// qadd<c> <Rd>,<Rm>,<Rn>
		// Unpredictable if (0) is 1: xxxx00010000xxxxxxxx(0)(0)(0)(0)0101xxxx
		new OpcodeARM(Index.arm_qadd, "qadd", "xxxx00010000xxxxxxxxxxxx0101xxxx"),
		// A8.6.128 QDADD
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// qdadd<c> <Rd>,<Rm>,<Rn>
		// Unpredictable if (0) is 1: xxxx00010100xxxxxxxx(0)(0)(0)(0)0101xxxx
		new OpcodeARM(Index.arm_qdadd, "qdadd", "xxxx00010100xxxxxxxxxxxx0101xxxx"),
		// A8.6.129 QDSUB
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// qdsub<c> <Rd>,<Rm>,<Rn>
		// Unpredictable if (0) is 1: xxxx00010110xxxxxxxx(0)(0)(0)(0)0101xxxx
		new OpcodeARM(Index.arm_qdsub, "qdsub", "xxxx00010110xxxxxxxxxxxx0101xxxx"),
		// A8.6.131 QSUB
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// qsub<c> <Rd>,<Rm>,<Rn>
		// Unpredictable if (0) is 1: xxxx00010010xxxxxxxx(0)(0)(0)(0)0101xxxx
		new OpcodeARM(Index.arm_qsub, "qsub", "xxxx00010010xxxxxxxxxxxx0101xxxx"),
		// A8.6.125 QADD16
		// Encoding A1 ARMv6*, ARMv7
		// qadd16<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100010xxxxxxxx(1)(1)(1)(1)0001xxxx
		// A8.6.126 QADD8
		// Encoding A1 ARMv6*, ARMv7
		// qadd8<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100010xxxxxxxx(1)(1)(1)(1)1001xxxx
		// A8.6.127 QASX
		// Encoding A1 ARMv6*, ARMv7
		// qasx<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100010xxxxxxxx(1)(1)(1)(1)0011xxxx
		// A8.6.130 QSAX
		// Encoding A1 ARMv6*, ARMv7
		// qsax<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100010xxxxxxxx(1)(1)(1)(1)0101xxxx
		// A8.6.132 QSUB16
		// Encoding A1 ARMv6*, ARMv7
		// qsub16<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100010xxxxxxxx(1)(1)(1)(1)0111xxxx
		// A8.6.133 QSUB8
		// Encoding A1 ARMv6*, ARMv7
		// qsub8<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100010xxxxxxxx(1)(1)(1)(1)1111xxxx
		// A8.6.148 SADD16
		// Encoding A1 ARMv6*, ARMv7
		// sadd16<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100001xxxxxxxx(1)(1)(1)(1)0001xxxx
		// A8.6.149 SADD8
		// Encoding A1 ARMv6*, ARMv7
		// sadd8<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100001xxxxxxxx(1)(1)(1)(1)1001xxxx
		// A8.6.150 SASX
		// Encoding A1 ARMv6*, ARMv7
		// sasx<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100001xxxxxxxx(1)(1)(1)(1)0011xxxx
		// A8.6.159 SHADD16
		// Encoding A1 ARMv6*, ARMv7
		// shadd16<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100011xxxxxxxx(1)(1)(1)(1)0001xxxx
		// A8.6.160 SHADD8
		// Encoding A1 ARMv6*, ARMv7
		// shadd8<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100011xxxxxxxx(1)(1)(1)(1)1001xxxx
		// A8.6.161 SHASX
		// Encoding A1 ARMv6*, ARMv7
		// shasx<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100011xxxxxxxx(1)(1)(1)(1)0011xxxx
		// A8.6.162 SHSAX
		// Encoding A1 ARMv6*, ARMv7
		// shsax<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100011xxxxxxxx(1)(1)(1)(1)0101xxxx
		// A8.6.163 SHSUB16
		// Encoding A1 ARMv6*, ARMv7
		// shsub16<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100011xxxxxxxx(1)(1)(1)(1)0111xxxx
		// A8.6.164 SHSUB8
		// Encoding A1 ARMv6*, ARMv7
		// shsub8<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100011xxxxxxxx(1)(1)(1)(1)1111xxxx
		// A8.6.185 SSAX
		// Encoding A1 ARMv6*, ARMv7
		// ssax<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100001xxxxxxxx(1)(1)(1)(1)0101xxxx
		// A8.6.186 SSUB16
		// Encoding A1 ARMv6*, ARMv7
		// ssub16<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100001xxxxxxxx(1)(1)(1)(1)0111xxxx
		// A8.6.187 SSUB8
		// Encoding A1 ARMv6*, ARMv7
		// ssub8<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100001xxxxxxxx(1)(1)(1)(1)1111xxxx
		// A8.6.233 UADD16
		// Encoding A1 ARMv6*, ARMv7
		// uadd16<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100101xxxxxxxx(1)(1)(1)(1)0001xxxx
		// A8.6.234 UADD8
		// Encoding A1 ARMv6*, ARMv7
		// uadd8<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100101xxxxxxxx(1)(1)(1)(1)1001xxxx
		// A8.6.235 UASX
		// Encoding A1 ARMv6*, ARMv7
		// uasx<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100101xxxxxxxx(1)(1)(1)(1)0011xxxx
		// A8.6.238 UHADD16
		// Encoding A1 ARMv6*, ARMv7
		// uhadd16<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100111xxxxxxxx(1)(1)(1)(1)0001xxxx
		// A8.6.239 UHADD8
		// Encoding A1 ARMv6*, ARMv7
		// uhadd8<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100111xxxxxxxx(1)(1)(1)(1)1001xxxx
		// A8.6.240 UHASX
		// Encoding A1 ARMv6*, ARMv7
		// uhasx<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100111xxxxxxxx(1)(1)(1)(1)0011xxxx
		// A8.6.241 UHSAX
		// Encoding A1 ARMv6*, ARMv7
		// uhsax<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100111xxxxxxxx(1)(1)(1)(1)0101xxxx
		// A8.6.242 UHSUB16
		// Encoding A1 ARMv6*, ARMv7
		// uhsub16<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100111xxxxxxxx(1)(1)(1)(1)0111xxxx
		// A8.6.243 UHSUB8
		// Encoding A1 ARMv6*, ARMv7
		// uhsub8<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100111xxxxxxxx(1)(1)(1)(1)1111xxxx
		// A8.6.247 UQADD16
		// Encoding A1 ARMv6*, ARMv7
		// uqadd16<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100110xxxxxxxx(1)(1)(1)(1)0001xxxx
		// A8.6.248 UQADD8
		// Encoding A1 ARMv6*, ARMv7
		// uqadd8<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100110xxxxxxxx(1)(1)(1)(1)1001xxxx
		// A8.6.249 UQASX
		// Encoding A1 ARMv6*, ARMv7
		// uqasx<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100110xxxxxxxx(1)(1)(1)(1)0011xxxx
		// A8.6.250 UQSAX
		// Encoding A1 ARMv6*, ARMv7
		// uqsax<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100110xxxxxxxx(1)(1)(1)(1)0101xxxx
		// A8.6.251 UQSUB16
		// Encoding A1 ARMv6*, ARMv7
		// uqsub16<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100110xxxxxxxx(1)(1)(1)(1)0111xxxx
		// A8.6.252 UQSUB8
		// Encoding A1 ARMv6*, ARMv7
		// uqsub8<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100110xxxxxxxx(1)(1)(1)(1)1111xxxx
		// A8.6.257 USAX
		// Encoding A1 ARMv6*, ARMv7
		// usax<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100101xxxxxxxx(1)(1)(1)(1)0101xxxx
		// A8.6.258 USUB16
		// Encoding A1 ARMv6*, ARMv7
		// usub16<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100101xxxxxxxx(1)(1)(1)(1)0111xxxx
		// A8.6.259 USUB8
		// Encoding A1 ARMv6*, ARMv7
		// usub8<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01100101xxxxxxxx(1)(1)(1)(1)1111xxxx
		//
		// {s|u|}{h|q|}{{add|sub}{8|16}|asx|sax}<c> <Rd>,<Rn>,<Rm>
		// cond_31_28 0 1 1 0 0 hqsu_22_20 Rn_19_16 Rd_15_12 (1)(1)(1)(1) op_7_5 1 Rm_3_0
		// Unpredictable if (1) is 0: xxxx01100010xxxxxxxx(1)(1)(1)(1)0001xxxx
		new OpcodeARM(Index.arm__r_dnm_math, null, "xxxx01100xxxxxxxxxxxxxxxxxx1xxxx"),
		// A8.6.134 RBIT
		// Encoding A1 ARMv6T2, ARMv7
		// rbit<c> <Rd>,<Rm>
		// Unpredictable if (1) is 0: xxxx01101111(1)(1)(1)(1)xxxx(1)(1)(1)(1)0011xxxx
		new OpcodeARM(Index.arm_rbit, "rbit", "xxxx01101111xxxxxxxxxxxx0011xxxx"),
		// A8.6.135 REV
		// Encoding A1 ARMv6*, ARMv7
		// rev<c> <Rd>,<Rm>
		// Unpredictable if (1) is 0: xxxx01101011(1)(1)(1)(1)xxxx(1)(1)(1)(1)0011xxxx
		new OpcodeARM(Index.arm_rev, "rev", "xxxx01101011xxxxxxxxxxxx0011xxxx"),
		// A8.6.136 REV16
		// Encoding A1 ARMv6*, ARMv7
		// rev16<c> <Rd>,<Rm>
		// Unpredictable if (1) is 0: xxxx01101011(1)(1)(1)(1)xxxx(1)(1)(1)(1)1011xxxx
		new OpcodeARM(Index.arm_rev16, "rev16", "xxxx01101011xxxxxxxxxxxx1011xxxx"),
		// A8.6.137 REVSH
		// Encoding A1 ARMv6*, ARMv7
		// revsh<c> <Rd>,<Rm>
		// Unpredictable if (1) is 0: xxxx01101111(1)(1)(1)(1)xxxx(1)(1)(1)(1)1011xxxx
		new OpcodeARM(Index.arm_revsh, "revsh", "xxxx01101111xxxxxxxxxxxx1011xxxx"),
		// A8.6.141 RRX
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// rrx{s}<c> <Rd>,<Rm>
		// Unpredictable if (0) is 1: xxxx0001101x(0)(0)(0)(0)xxxx00000110xxxx
		// must precede arm_ror__imm in table
		new OpcodeARM(Index.arm_rrx, "rrx", "xxxx0001101xxxxxxxxx00000110xxxx"),
		// A8.6.139 ROR (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ror{s}<c> <Rd>,<Rm>,#<imm>
		// Unpredictable if (0) is 1: xxxx0001101x(0)(0)(0)(0)xxxxxxxxx110xxxx
		// must follow arm_rrx in table
		new OpcodeARM(Index.arm_ror__imm, "ror", "xxxx0001101xxxxxxxxxxxxxx110xxxx"),
		// A8.6.140 ROR (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ror{s}<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (0) is 1: xxxx0001101x(0)(0)(0)(0)xxxxxxxx0111xxxx
		new OpcodeARM(Index.arm_ror__reg, "ror", "xxxx0001101xxxxxxxxxxxxx0111xxxx"),
		// A8.6.143 RSB (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// rsb{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		new OpcodeARM(Index.arm_rsb__reg, "rsb", "xxxx0000011xxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.144 RSB (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// rsb{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		new OpcodeARM(Index.arm_rsb__rsr, "rsb", "xxxx0000011xxxxxxxxxxxxx0xx1xxxx"),
		// A8.6.146 RSC (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// rsc{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		new OpcodeARM(Index.arm_rsc__reg, "rsc", "xxxx0000111xxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.147 RSC (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// rsc{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		new OpcodeARM(Index.arm_rsc__rsr, "rsc", "xxxx0000111xxxxxxxxxxxxx0xx1xxxx"),
		// A8.6.152 SBC (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// sbc{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		new OpcodeARM(Index.arm_sbc__reg, "sbc", "xxxx0000110xxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.153 SBC (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// sbc{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		new OpcodeARM(Index.arm_sbc__rsr, "sbc", "xxxx0000110xxxxxxxxxxxxx0xx1xxxx"),
		// A8.6.154 SBFX
		// Encoding A1 ARMv6T2, ARMv7
		// sbfx<c> <Rd>,<Rn>,#<lsb>,#<width>
		new OpcodeARM(Index.arm_sbfx, "sbfx", "xxxx0111101xxxxxxxxxxxxxx101xxxx"),
		// A8.6.156 SEL
		// Encoding A1 ARMv6*, ARMv7
		// sel<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: xxxx01101000xxxxxxxx(1)(1)(1)(1)1011xxxx
		new OpcodeARM(Index.arm_sel, "sel", "xxxx01101000xxxxxxxxxxxx1011xxxx"),
		// A8.6.166 SMLABB, SMLABT, SMLATB, SMLATT
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// smla<x><y><c> <Rd>,<Rn>,<Rm>,<Ra>
		new OpcodeARM(Index.arm_smla, "smla", "xxxx00010000xxxxxxxxxxxx1xx0xxxx"),
		// A8.6.177 SMUAD
		// Encoding A1 ARMv6*, ARMv7
		// smuad{x}<c> <Rd>,<Rn>,<Rm>
		new OpcodeARM(Index.arm_smuad, "smuad", "xxxx01110000xxxx1111xxxx00x1xxxx"),
		// A8.6.167 SMLAD
		// Encoding A1 ARMv6*, ARMv7
		// smlad{x}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// must follow arm_smuad in table
		new OpcodeARM(Index.arm_smlad, "smlad", "xxxx01110000xxxxxxxxxxxx00x1xxxx"),
		// A8.6.168 SMLAL
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// smlal{s}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.arm_smlal, "smlal", "xxxx0000111xxxxxxxxxxxxx1001xxxx"),
		// A8.6.169 SMLALBB, SMLALBT, SMLALTB, SMLALTT
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// smlal<x><y><c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.arm_smlalxy, "smlal", "xxxx00010100xxxxxxxxxxxx1xx0xxxx"),
		// A8.6.170 SMLALD
		// Encoding A1 ARMv6*, ARMv7
		// smlald{x}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.arm_smlald, "smlald", "xxxx01110100xxxxxxxxxxxx00x1xxxx"),
		// A8.6.171 SMLAWB, SMLAWT
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// smlaw<y><c> <Rd>,<Rn>,<Rm>,<Ra>
		new OpcodeARM(Index.arm_smlaw, "smlaw", "xxxx00010010xxxxxxxxxxxx1x00xxxx"),
		// A8.6.181 SMUSD
		// Encoding A1 ARMv6*, ARMv7
		// smusd{x}<c> <Rd>,<Rn>,<Rm>
		// must precede arm_smlsd in table
		new OpcodeARM(Index.arm_smusd, "smusd", "xxxx01110000xxxx1111xxxx01x1xxxx"),
		// A8.6.172 SMLSD
		// Encoding A1 ARMv6*, ARMv7
		// smlsd{x}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// must follow arm_smusd in table
		new OpcodeARM(Index.arm_smlsd, "smlsd", "xxxx01110000xxxxxxxxxxxx01x1xxxx"),
		// A8.6.173 SMLSLD
		// Encoding A1 ARMv6*, ARMv7
		// smlsld{x}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.arm_smlsld, "smlsld", "xxxx01110100xxxxxxxxxxxx01x1xxxx"),
		// A8.6.176 SMMUL
		// Encoding A1 ARMv6*, ARMv7
		// smmul{r}<c> <Rd>,<Rn>,<Rm>
		// must precede arm_smmla in table
		new OpcodeARM(Index.arm_smmul, "smmul", "xxxx01110101xxxx1111xxxx00x1xxxx"),
		// A8.6.174 SMMLA
		// Encoding A1 ARMv6*, ARMv7
		// smmla{r}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// must follow arm_smmul in table
		new OpcodeARM(Index.arm_smmla, "smmla", "xxxx01110101xxxxxxxxxxxx00x1xxxx"),
		// A8.6.175 SMMLS
		// Encoding A1 ARMv6*, ARMv7
		// smmls{r}<c> <Rd>,<Rn>,<Rm>,<Ra>
		new OpcodeARM(Index.arm_smmls, "smmls", "xxxx01110101xxxxxxxxxxxx11x1xxxx"),
		// A8.6.178 SMULBB, SMULBT, SMULTB, SMULTT
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// smul<x><y><c> <Rd>,<Rn>,<Rm>
		new OpcodeARM(Index.arm_smul, "smul", "xxxx00010110xxxxxxxxxxxx1xx0xxxx"),
		// A8.6.179 SMULL
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// smull{s}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.arm_smull, "smull", "xxxx0000110xxxxxxxxxxxxx1001xxxx"),
		// A8.6.180 SMULWB, SMULWT
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// smulw<y><c> <Rd>,<Rn>,<Rm>
		new OpcodeARM(Index.arm_smulw, "smulw", "xxxx00010010xxxxxxxxxxxx1x10xxxx"),
		// A8.6.183 SSAT
		// Encoding A1 ARMv6*, ARMv7
		// ssat<c> <Rd>,#<imm>,<Rn>{,<shift>}
		new OpcodeARM(Index.arm_ssat, "ssat", "xxxx0110101xxxxxxxxxxxxxxx01xxxx"),
		// A8.6.184 SSAT16
		// Encoding A1 ARMv6*, ARMv7
		// ssat16<c> <Rd>,#<imm>,<Rn>
		// Unpredictable if (1) is 0: xxxx01101010xxxxxxxx(1)(1)(1)(1)0011xxxx
		new OpcodeARM(Index.arm_ssat16, "ssat16", "xxxx01101010xxxxxxxxxxxx0011xxxx"),
		// A8.6.189 STM / STMIA / STMEA
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// stm<c> <Rn>{!},<registers>
		// must precede arm_stm__usr_regs in table
		new OpcodeARM(Index.arm_stm__regs, "stm", "xxxx100010x0xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.190 STMDA / STMED
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// stmda<c> <Rn>{!},<registers>
		new OpcodeARM(Index.arm_stmda, "stmda", "xxxx100000x0xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.123 PUSH
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// push<c> <registers> <registers> contains more than one register
		// must precede arm_stmdb in table
		new OpcodeARM(Index.arm_push__regs, "push", "xxxx100100101101xxxxxxxxxxxxxxxx"),
		// A8.6.191 STMDB / STMFD
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// stmdb<c> <Rn>{!},<registers>
		// must follow arm_push__regs in table
		new OpcodeARM(Index.arm_stmdb, "stmdb", "xxxx100100x0xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.192 STMIB / STMFA
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// stmib<c> <Rn>{!},<registers>
		new OpcodeARM(Index.arm_stmib, "stmib", "xxxx100110x0xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.210 STRT
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// strt<c> <Rt>, [<Rn>] {, +/-<imm12>}
		// must precede arm_str__imm in table
		new OpcodeARM(Index.arm_strt__imm, "strt", "xxxx0100x010xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.123 PUSH
		// Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// push<c> <registers> <registers> contains one register, <Rt>
		// must precede arm_str__imm in table
		new OpcodeARM(Index.arm_push__reg, "push", "xxxx010100101101xxxx000000000100"),
		// A8.6.194 STR (immediate, ARM)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// str<c> <Rt>,[<Rn>{,#+/-<imm12>}]	str<c> <Rt>,[<Rn>],#+/-<imm12>	str<c> <Rt>,[<Rn>,#+/-<imm12>]!
		// must follow arm_strt__imm in table
		// must follow arm_push__reg in table
		new OpcodeARM(Index.arm_str__imm, "str", "xxxx010xx0x0xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.210 STRT
		// Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// strt<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// must precede arm_str__reg in table
		new OpcodeARM(Index.arm_strt__reg, "strt", "xxxx0110x010xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.195 STR (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// str<c> <Rt>,[<Rn>,+/-<Rm>{, <shift>}]{!}	str<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// must follow arm_strt__reg in table
		new OpcodeARM(Index.arm_str__reg, "str", "xxxx011xx0x0xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.199 STRBT
		// Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// strbt<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// must precede arm_strb__reg in table
		new OpcodeARM(Index.arm_strbt__reg, "strbt", "xxxx0110x110xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.198 STRB (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// strb<c> <Rt>,[<Rn>,+/-<Rm>{, <shift>}]{!}	strb<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// must follow arm_strbt__reg in table
		new OpcodeARM(Index.arm_strb__reg, "strb", "xxxx011xx1x0xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.201 STRD (register)
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// strd<c> <Rt>,<Rt2>,[<Rn>,+/-<Rm>]{!}	strd<c> <Rt>,<Rt2>,[<Rn>],+/-<Rm>
		// Unpredictable if (0) is 1: xxxx000xx0x0xxxxxxxx(0)(0)(0)(0)1111xxxx
		new OpcodeARM(Index.arm_strd__reg, "strd", "xxxx000xx0x0xxxxxxxxxxxx1111xxxx"),
		// A8.6.202 STREX
		// Encoding A1 ARMv6*, ARMv7
		// strex<c> <Rd>,<Rt>,[<Rn>]
		// Unpredictable if (1) is 0: xxxx00011000xxxxxxxx(1)(1)(1)(1)1001xxxx
		new OpcodeARM(Index.arm_strex, "strex", "xxxx00011000xxxxxxxxxxxx1001xxxx"),
		// A8.6.203 STREXB
		// Encoding A1 ARMv6K, ARMv7
		// strexb<c> <Rd>,<Rt>,[<Rn>]
		// Unpredictable if (1) is 0: xxxx00011100xxxxxxxx(1)(1)(1)(1)1001xxxx
		new OpcodeARM(Index.arm_strexb, "strexb", "xxxx00011100xxxxxxxxxxxx1001xxxx"),
		// A8.6.204 STREXD
		// Encoding A1 ARMv6K, ARMv7
		// strexd<c> <Rd>,<Rt>,<Rt2>,[<Rn>]
		// Unpredictable if (1) is 0: xxxx00011010xxxxxxxx(1)(1)(1)(1)1001xxxx
		new OpcodeARM(Index.arm_strexd, "strexd", "xxxx00011010xxxxxxxxxxxx1001xxxx"),
		// A8.6.205 STREXH
		// Encoding A1 ARMv6K, ARMv7
		// strexh<c> <Rd>,<Rt>,[<Rn>]
		// Unpredictable if (1) is 0: xxxx00011110xxxxxxxx(1)(1)(1)(1)1001xxxx
		new OpcodeARM(Index.arm_strexh, "strexh", "xxxx00011110xxxxxxxxxxxx1001xxxx"),
		// A8.6.209 STRHT
		// Encoding A2 ARMv6T2, ARMv7
		// strht<c> <Rt>, [<Rn>], +/-<Rm>
		// Unpredictable if (0) is 1: xxxx0000x010xxxxxxxx(0)(0)(0)(0)1011xxxx
		// must precede arm_strh__reg in table
		new OpcodeARM(Index.arm_strht__reg, "strht", "xxxx0000x010xxxxxxxxxxxx1011xxxx"),
		// A8.6.208 STRH (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// strh<c> <Rt>,[<Rn>,+/-<Rm>]{!}	strh<c> <Rt>,[<Rn>],+/-<Rm>
		// Unpredictable if (0) is 1: xxxx000xx0x0xxxxxxxx(0)(0)(0)(0)1011xxxx
		// must follow arm_strht__reg in table
		new OpcodeARM(Index.arm_strh__reg, "strh", "xxxx000xx0x0xxxxxxxxxxxx1011xxxx"),
		// A8.6.10 ADR
		// Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// adr<c> <Rd>,<label>	sub<c> <Rd>,pc,#<const>	Alternative form
		// must precede arm_sub__imm in table
		new OpcodeARM(Index.arm_adr__lower, "sub", "xxxx001001001111xxxxxxxxxxxxxxxx"),
		// A8.6.213 SUB (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// sub{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// A8.6.216 SUB (SP minus register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// sub{s}<c> <Rd>,sp,<Rm>{,<shift>}
		//
		new OpcodeARM(Index.arm_sub__reg, "sub", "xxxx0000010xxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.214 SUB (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// sub{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		new OpcodeARM(Index.arm_sub__rsr, "sub", "xxxx0000010xxxxxxxxxxxxx0xx1xxxx"),
		// A8.6.218 SVC (previously SWI)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// svc<c> #<imm24>
		new OpcodeARM(Index.arm_svc, "svc", "xxxx1111xxxxxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.219 SWP, SWPB
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6* (deprecated), ARMv7 (deprecated)
		// swp{b}<c> <Rt>,<Rt2>,[<Rn>]
		// Unpredictable if (0) is 1: xxxx00010x00xxxxxxxx(0)(0)(0)(0)1001xxxx
		new OpcodeARM(Index.arm_swp, "swp", "xxxx00010x00xxxxxxxxxxxx1001xxxx"),
		// A8.6.223 SXTB
		// Encoding A1 ARMv6*, ARMv7
		// sxtb<c> <Rd>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: xxxx011010101111xxxxxx(0)(0)0111xxxx
		// must precede arm_sxtab in table
		new OpcodeARM(Index.arm_sxtb, "sxtb", "xxxx011010101111xxxxxxxx0111xxxx"),
		// A8.6.220 SXTAB
		// Encoding A1 ARMv6*, ARMv7
		// sxtab<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: xxxx01101010xxxxxxxxxx(0)(0)0111xxxx
		// must follow arm_sxtb in table
		new OpcodeARM(Index.arm_sxtab, "sxtab", "xxxx01101010xxxxxxxxxxxx0111xxxx"),
		// A8.6.224 SXTB16
		// Encoding A1 ARMv6*, ARMv7
		// sxtb16<c> <Rd>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: xxxx011010001111xxxxxx(0)(0)0111xxxx
		// must precede arm_sxtab16 in table
		new OpcodeARM(Index.arm_sxtb16, "sxtb16", "xxxx011010001111xxxxxxxx0111xxxx"),
		// A8.6.221 SXTAB16
		// Encoding A1 ARMv6*, ARMv7
		// sxtab16<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: xxxx01101000xxxxxxxxxx(0)(0)0111xxxx
		// must follow arm_sxtb16 in table
		new OpcodeARM(Index.arm_sxtab16, "sxtab16", "xxxx01101000xxxxxxxxxxxx0111xxxx"),
		// A8.6.225 SXTH
		// Encoding A1 ARMv6*, ARMv7
		// sxth<c> <Rd>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: xxxx011010111111xxxxxx(0)(0)0111xxxx
		// must precede arm_sxtah in table
		new OpcodeARM(Index.arm_sxth, "sxth", "xxxx011010111111xxxxxxxx0111xxxx"),
		// A8.6.222 SXTAH
		// Encoding A1 ARMv6*, ARMv7
		// sxtah<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: xxxx01101011xxxxxxxxxx(0)(0)0111xxxx
		// must follow arm_sxth in table
		new OpcodeARM(Index.arm_sxtah, "sxtah", "xxxx01101011xxxxxxxxxxxx0111xxxx"),
		// A8.6.227 TEQ (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// teq<c> <Rn>,#<const>
		// Unpredictable if (0) is 1: xxxx00110011xxxx(0)(0)(0)(0)xxxxxxxxxxxx
		new OpcodeARM(Index.arm_teq__imm, "teq", "xxxx00110011xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.228 TEQ (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// teq<c> <Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: xxxx00010011xxxx(0)(0)(0)(0)xxxxxxx0xxxx
		new OpcodeARM(Index.arm_teq__reg, "teq", "xxxx00010011xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.229 TEQ (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// teq<c> <Rn>,<Rm>,<type> <Rs>
		// Unpredictable if (0) is 1: xxxx00010011xxxx(0)(0)(0)(0)xxxx0xx1xxxx
		new OpcodeARM(Index.arm_teq__rsr, "teq", "xxxx00010011xxxxxxxxxxxx0xx1xxxx"),
		// A8.6.230 TST (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// tst<c> <Rn>,#<const>
		// Unpredictable if (0) is 1: xxxx00110001xxxx(0)(0)(0)(0)xxxxxxxxxxxx
		new OpcodeARM(Index.arm_tst__imm, "tst", "xxxx00110001xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.231 TST (register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// tst<c> <Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: xxxx00010001xxxx(0)(0)(0)(0)xxxxxxx0xxxx
		new OpcodeARM(Index.arm_tst__reg, "tst", "xxxx00010001xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.232 TST (register-shifted register)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// tst<c> <Rn>,<Rm>,<type> <Rs>
		// Unpredictable if (0) is 1: xxxx00010001xxxx(0)(0)(0)(0)xxxx0xx1xxxx
		new OpcodeARM(Index.arm_tst__rsr, "tst", "xxxx00010001xxxxxxxxxxxx0xx1xxxx"),
		// A8.6.236 UBFX
		// Encoding A1 ARMv6T2, ARMv7
		// ubfx<c> <Rd>,<Rn>,#<lsb>,#<width>
		new OpcodeARM(Index.arm_ubfx, "ubfx", "xxxx0111111xxxxxxxxxxxxxx101xxxx"),
		// A8.6.244 UMAAL
		// Encoding A1 ARMv6*, ARMv7
		// umaal<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.arm_umaal, "umaal", "xxxx00000100xxxxxxxxxxxx1001xxxx"),
		// A8.6.245 UMLAL
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// umlal{s}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.arm_umlal, "umlal", "xxxx0000101xxxxxxxxxxxxx1001xxxx"),
		// A8.6.246 UMULL
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// umull{s}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.arm_umull, "umull", "xxxx0000100xxxxxxxxxxxxx1001xxxx"),
		// A8.6.253 USAD8
		// Encoding A1 ARMv6*, ARMv7
		// usad8<c> <Rd>,<Rn>,<Rm>
		// must precede arm_usada8 in table
		new OpcodeARM(Index.arm_usad8, "usad8", "xxxx01111000xxxx1111xxxx0001xxxx"),
		// A8.6.254 USADA8
		// Encoding A1 ARMv6*, ARMv7
		// usada8<c> <Rd>,<Rn>,<Rm>,<Ra>
		// must follow arm_usad8 in table
		new OpcodeARM(Index.arm_usada8, "usada8", "xxxx01111000xxxxxxxxxxxx0001xxxx"),
		// A8.6.255 USAT
		// Encoding A1 ARMv6*, ARMv7
		// usat<c> <Rd>,#<imm5>,<Rn>{,<shift>}
		new OpcodeARM(Index.arm_usat, "usat", "xxxx0110111xxxxxxxxxxxxxxx01xxxx"),
		// A8.6.256 USAT16
		// Encoding A1 ARMv6*, ARMv7
		// usat16<c> <Rd>,#<imm4>,<Rn>
		// Unpredictable if (1) is 0: xxxx01101110xxxxxxxx(1)(1)(1)(1)0011xxxx
		new OpcodeARM(Index.arm_usat16, "usat16", "xxxx01101110xxxxxxxxxxxx0011xxxx"),
		// A8.6.263 UXTB
		// Encoding A1 ARMv6*, ARMv7
		// uxtb<c> <Rd>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: xxxx011011101111xxxxxx(0)(0)0111xxxx
		// must precede arm_uxtab in table
		new OpcodeARM(Index.arm_uxtb, "uxtb", "xxxx011011101111xxxxxxxx0111xxxx"),
		// A8.6.260 UXTAB
		// Encoding A1 ARMv6*, ARMv7
		// uxtab<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: xxxx01101110xxxxxxxxxx(0)(0)0111xxxx
		// must follow arm_uxtb in table
		new OpcodeARM(Index.arm_uxtab, "uxtab", "xxxx01101110xxxxxxxxxxxx0111xxxx"),
		// A8.6.264 UXTB16
		// Encoding A1 ARMv6*, ARMv7
		// uxtb16<c> <Rd>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: xxxx011011001111xxxxxx(0)(0)0111xxxx
		// must precede arm_uxtab16 in table
		new OpcodeARM(Index.arm_uxtb16, "uxtb16", "xxxx011011001111xxxxxxxx0111xxxx"),
		// A8.6.261 UXTAB16
		// Encoding A1 ARMv6*, ARMv7
		// uxtab16<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: xxxx01101100xxxxxxxxxx(0)(0)0111xxxx
		// must follow arm_uxtb16 in table
		new OpcodeARM(Index.arm_uxtab16, "uxtab16", "xxxx01101100xxxxxxxxxxxx0111xxxx"),
		// A8.6.265 UXTH
		// Encoding A1 ARMv6*, ARMv7
		// uxth<c> <Rd>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: xxxx011011111111xxxxxx(0)(0)0111xxxx
		// must precede arm_uxtah in table
		new OpcodeARM(Index.arm_uxth, "uxth", "xxxx011011111111xxxxxxxx0111xxxx"),
		// A8.6.262 UXTAH
		// Encoding A1 ARMv6*, ARMv7
		// uxtah<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: xxxx01101111xxxxxxxxxx(0)(0)0111xxxx
		// must follow arm_uxth in table
		new OpcodeARM(Index.arm_uxtah, "uxtah", "xxxx01101111xxxxxxxxxxxx0111xxxx"),
		// B6.1.9 SMC (previously SMI)
		// Encoding A1 Security Extensions
		// smc<c> #<imm4>
		// Unpredictable if (0) is 1: xxxx00010110(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)0111xxxx
		new OpcodeARM(Index.arm_smc, "smc", "xxxx00010110xxxxxxxxxxxx0111xxxx"),
		// B6.1.10 SRS
		// Encoding A1 ARMv6*, ARMv7
		// srs{<amode>} sp{!},#<mode>
		// Unpredictable if (1) is 0 or (0) is 1: 1111100xx1x0(1)(1)(0)(1)(0)(0)(0)(0)(0)(1)(0)(1)(0)(0)(0)xxxxx
		new OpcodeARM(Index.arm_srs, "srs", "1111100xx1x0xxxxxxxxxxxxxxxxxxxx"),
		// B6.1.11 STM (user registers)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// stm{amode}<c> <Rn>,<registers>^
		// Unpredictable if (0) is 1: xxxx100xx1(0)0xxxxxxxxxxxxxxxxxxxx
		// must follow arm_stm__regs in table
		new OpcodeARM(Index.arm_stm__usr_regs, "stm", "xxxx100xx1x0xxxxxxxxxxxxxxxxxxxx"),

		// VFP and Advanced SIMD instructions

		// A8.6.266 VABA, VABAL
		// Encoding A1 / T1 Advanced SIMD
		// vaba<c>.<dt> <Qd>, <Qn>, <Qm>	vaba<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vaba, "vaba", "1111001x0xxxxxxxxxxx0111xxx1xxxx"),
		// A8.6.281 VCEQ (immediate #0)
		// Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vceq<c>.<dt> <Qd>, <Qm>, #0	vceq<c>.<dt> <Dd>, <Dm>, #0
		// must precede arm_vabal in table
		// must precede arm_vaddl_vaddw in table
		new OpcodeARM(Index.arm_vceq__imm0, "vceq", "111100111x11xx01xxxx0x010xx0xxxx"),
		// A8.6.287 VCLE (immediate #0)
		// Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vcle<c>.<dt> <Qd>, <Qm>, #0	vcle<c>.<dt> <Dd>, <Dm>, #0
		// must precede arm_vabal in table
		// must precede arm_vaddl_vaddw in table
		new OpcodeARM(Index.arm_vcle, "vcle", "111100111x11xx01xxxx0x011xx0xxxx"),
		// A8.6.293 VCNT
		// Encoding A1 / T1 Advanced SIMD
		// vcnt<c>.8 <Qd>, <Qm>	vcnt<c>.8 <Dd>, <Dm>
		// must precede arm_vabal in table
		new OpcodeARM(Index.arm_vcnt, "vcnt", "111100111x11xx00xxxx01010xx0xxxx"),
		// A8.6.267 VABD, VABDL (integer)
		// Encoding A1 / T1 Advanced SIMD
		// vabd<c>.<dt> <Qd>, <Qn>, <Qm>	vabd<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vabd__int, "vabd", "1111001x0xxxxxxxxxxx0111xxx0xxxx"),
		// A8.6.269 VABS
		// Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variant)
		// vabs<c>.<dt> <Qd>, <Qm>	vabs<c>.<dt> <Dd>, <Dm>
		// must precede arm_vabdl in table
		new OpcodeARM(Index.arm_vabs, "vabs", "111100111x11xx01xxxx0x110xx0xxxx"),
		// A8.6.294 VCVT (between floating-point and integer, Advanced SIMD)
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vcvt<c>.<Td>.<Tm> <Qd>, <Qm>	vcvt<c>.<Td>.<Tm> <Dd>, <Dm>
		// must precede arm_vabdl in table
		new OpcodeARM(Index.arm_vcvt__fp_i_vec, "vcvt", "111100111x11xx11xxxx011xxxx0xxxx"),
		// A8.6.299 VCVT (between half-precision and single-precision, Advanced SIMD)
		// Encoding A1 / T1 Advanced SIMD with half-precision extensions (UNDEFINED in integer-only variant)
		// vcvt<c>.f32.f16 <Qd>, <Dm>	vcvt<c>.f16.f32 <Dd>, <Qm>
		// must precede arm_vabdl in table
		new OpcodeARM(Index.arm_vcvt__hp_sp_vec, "vcvt", "111100111x11xx10xxxx011x00x0xxxx"),
		// A8.6.342 VNEG
		// Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vneg<c>.<dt> <Qd>, <Qm>	vneg<c>.<dt> <Dd>, <Dm>
		// must precede arm_vabdl in table
		// must precede arm_vmlal in table
		// must precede arm_vmlsl in table
		new OpcodeARM(Index.arm_vneg, "vneg", "111100111x11xx01xxxx0x111xx0xxxx"),
		// A8.6.356 VQABS
		// Encoding A1 / T1 Advanced SIMD
		// vqabs<c>.<dt> <Qd>,<Qm>	vqabs<c>.<dt> <Dd>,<Dm>
		// must precede arm_vabdl in table
		// must precede arm_vmlal in table
		new OpcodeARM(Index.arm_vqabs, "vqabs", "111100111x11xx00xxxx01110xx0xxxx"),
		// A8.6.362 VQNEG
		// Encoding A1 / T1 Advanced SIMD
		// vqneg<c>.<dt> <Qd>,<Qm>	vqneg<c>.<dt> <Dd>,<Dm>
		// must precede arm_vabdl in table
		// must precede arm_vmlsl in table
		new OpcodeARM(Index.arm_vqneg, "vqneg", "111100111x11xx00xxxx01111xx0xxxx"),
		// A8.6.267 VABD, VABDL (integer)
		// Encoding A2 Advanced SIMD
		// vabdl<c>.<dt> <Qd>, <Dn>, <Dm>
		// must follow arm_vabs in table
		// must follow arm_vcvt__fp_i_vec in table
		// must follow arm_vcvt__hp_sp_vec in table
		// must follow arm_vneg in table
		// must follow arm_vqabs in table
		// must follow arm_vqneg in table
		new OpcodeARM(Index.arm_vabdl, "vabdl", "1111001x1xxxxxxxxxxx0111x0x0xxxx"),
		// A8.6.268 VABD (floating-point)
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vabd<c>.f32 <Qd>, <Qn>, <Qm>	vabd<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vabd__f32, "vabd", "111100110x1xxxxxxxxx1101xxx0xxxx"),
		// A8.6.269 VABS
		// Encoding A2 / T2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vabs<c>.f64 <Dd>, <Dm>	vabs<c>.f32 <Sd>, <Sm>
		new OpcodeARM(Index.arm_vabs__f, "vabs", "xxxx11101x110000xxxx101x11x0xxxx"),
		// A8.6.270 VACGE, VACGT, VACLE, VACLT
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vacge<c>.f32 <Qd>, <Qn>, <Qm>	vacge<c>.f32 <Dd>, <Dn>, <Dm>
		// vacgt<c>.f32 <Qd>, <Qn>, <Qm>	vacgt<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vacge_vacgt, "vac", "111100110xxxxxxxxxxx1110xxx1xxxx"),
		// A8.6.271 VADD (integer)
		// Encoding A1 / T1 Advanced SIMD
		// vadd<c>.<dt> <Qd>, <Qn>, <Qm>	vadd<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vadd__int, "vadd", "111100100xxxxxxxxxxx1000xxx0xxxx"),
		// A8.6.272 VADD (floating-point)
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variants)
		// vadd<c>.f32 <Qd>, <Qn>, <Qm>	vadd<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vadd__f32, "vadd", "111100100x0xxxxxxxxx1101xxx0xxxx"),
		// A8.6.272 VADD (floating-point)
		// Encoding A2 / T2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vadd<c>.f64 <Dd>, <Dn>, <Dm>	vadd<c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.arm_vadd__fp_f, "vadd", "xxxx11100x11xxxxxxxx101xx0x0xxxx"),
		// A8.6.305 VEXT
		// Encoding A1 / T1 Advanced SIMD
		// vext<c>.8 <Qd>, <Qn>, <Qm>, #<imm>	vext<c>.8 <Dd>, <Dn>, <Dm>, #<imm>
		// must precede arm_vabal in table
		// must precede arm_vaddhn in table
		new OpcodeARM(Index.arm_vext, "vext", "111100101x11xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.273 VADDHN
		// Encoding A1 / T1 Advanced SIMD
		// vaddhn<c>.<dt> <Dd>, <Qn>, <Qm>
		// must follow arm_vext in table
		new OpcodeARM(Index.arm_vaddhn, "vaddhn", "111100101xxxxxxxxxxx0100x0x0xxxx"),
		// A8.6.283 VCGE (immediate #0)
		// Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vcge<c>.<dt> <Qd>, <Qm>, #0	vcge<c>.<dt> <Dd>, <Dm>, #0
		// must precede arm_vaddl_vaddw in table
		new OpcodeARM(Index.arm_vcge__imm0, "vcge", "111100111x11xx01xxxx0x001xx0xxxx"),
		// A8.6.285 VCGT (immediate #0)
		// Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vcgt<c>.<dt> <Qd>, <Qm>, #0	vcgt<c>.<dt> <Dd>, <Dm>, #0
		// must precede arm_vaddl_vaddw in table
		new OpcodeARM(Index.arm_vcgt__imm0, "vcgt", "111100111x11xx01xxxx0x000xx0xxxx"),
		// A8.6.373 VREV16, VREV32, VREV64
		// Encoding A1 / T1 Advanced SIMD
		// vrev<n><c>.<size> <Qd>, <Qm>	vrev<n><c>.<size> <Dd>, <Dm>
		// must precede arm_vaddl_vaddw in table
		new OpcodeARM(Index.arm_vrev, "vrev", "111100111x11xx00xxxx000xxxx0xxxx"),
		// A8.6.405 VSWP
		// Encoding A1 / T1 Advanced SIMD
		// vswp<c> <Qd>, <Qm>	vswp<c> <Dd>, <Dm>
		// must precede arm_vaddl_vaddw in table
		new OpcodeARM(Index.arm_vswp, "vswp", "111100111x11xx10xxxx00000xx0xxxx"),
		// A8.6.409 VUZP
		// Encoding A1 / T1 Advanced SIMD
		// vuzp<c>.<size> <Qd>, <Qm>	vuzp<c>.<size> <Dd>, <Dm>
		// must precede arm_vaddl_vaddw in table
		// must precede arm_vtrn in table
		new OpcodeARM(Index.arm_vuzp, "vuzp", "111100111x11xx10xxxx00010xx0xxxx"),
		// A8.6.410 VZIP
		// Encoding A1 / T1 Advanced SIMD
		// vzip<c>.<size> <Qd>, <Qm>	vzip<c>.<size> <Dd>, <Dm>
		// must precede arm_vaddl_vaddw in table
		// must precede arm_vtrn in table
		new OpcodeARM(Index.arm_vzip, "vzip", "111100111x11xx10xxxx00011xx0xxxx"),
		// A8.6.407 VTRN
		// Encoding A1 / T1 Advanced SIMD
		// vtrn<c>.<size> <Qd>, <Qm>	vtrn<c>.<size> <Dd>, <Dm>
		// must precede arm_vaddl_vaddw in table
		// must follow arm_vuzp in table
		// must follow arm_vzip in table
		new OpcodeARM(Index.arm_vtrn, "vtrn", "111100111x11xx10xxxx00001xx0xxxx"),
		// A8.6.274 VADDL, VADDW
		// Encoding A1 / T1 Advanced SIMD
		// vaddl<c>.<dt> <Qd>, <Dn>, <Dm>	vaddw<c>.<dt> <Qd>, <Qn>, <Dm>
		// must follow arm_vceq__imm0 in table
		// must follow arm_vcge__imm0 in table
		// must follow arm_vcgt__imm0 in table
		// must follow arm_vcle in table
		// must follow arm_vrev in table
		// must follow arm_vswp in table
		// must follow arm_vtrn in table
		// must follow arm_vuzp in table
		// must follow arm_vzip in table
		new OpcodeARM(Index.arm_vaddl_vaddw, "vadd", "1111001x1xxxxxxxxxxx000xx0x0xxxx"),
		// A8.6.276 VAND (register)
		// Encoding A1 / T1 Advanced SIMD
		// vand<c> <Qd>, <Qn>, <Qm>	vand<c> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vand, "vand", "111100100x00xxxxxxxx0001xxx1xxxx"),
		// A8.6.278 VBIC (register)
		// Encoding A1 / T1 Advanced SIMD
		// vbic<c> <Qd>, <Qn>, <Qm>	vbic<c> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vbic, "vbic", "111100100x01xxxxxxxx0001xxx1xxxx"),
		// A8.6.304 VEOR
		// Encoding A1 / T1 Advanced SIMD
		// veor<c> <Qd>, <Qn>, <Qm>	veor<c> <Dd>, <Dn>, <Dm>
		// A8.6.279 VBIF, VBIT, VBSL
		// Encoding A1 / T1 Advanced SIMD
		// vbif<c> <Qd>, <Qn>, <Qm>	vbif<c> <Dd>, <Dn>, <Dm>
		// vbit<c> <Qd>, <Qn>, <Qm>	vbit<c> <Dd>, <Dn>, <Dm>
		// vbsl<c> <Qd>, <Qn>, <Qm>	vbsl<c> <Dd>, <Dn>, <Dm>
		//
		new OpcodeARM(Index.arm_vbif_vbit_vbsl_veor, "v", "111100110xxxxxxxxxxx0001xxx1xxxx"),
		// A8.6.280 VCEQ (register)
		// Encoding A1 / T1 Advanced SIMD
		// vceq<c>.<dt> <Qd>, <Qn>, <Qm>	vceq<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vceq__reg_int, "vceq", "111100110xxxxxxxxxxx1000xxx1xxxx"),
		// A8.6.280 VCEQ (register)
		// Encoding A2 / T2 Advanced SIMD (UNDEFINED in integer-only variant)
		// vceq<c>.f32 <Qd>, <Qn>, <Qm>	vceq<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vceq__reg_f32, "vceq", "111100100x0xxxxxxxxx1110xxx0xxxx"),
		// A8.6.282 VCGE (register)
		// Encoding A1 / T1 Advanced SIMD
		// vcge<c>.<dt> <Qd>, <Qn>, <Qm>	vcge<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vcge__reg_int, "vcge", "1111001x0xxxxxxxxxxx0011xxx1xxxx"),
		// A8.6.282 VCGE (register)
		// Encoding A2 / T2 Advanced SIMD (UNDEFINED in integer-only variant)
		// vcge<c>.f32 <Qd>, <Qn>, <Qm>	vcge<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vcge__reg_f32, "vcge", "111100110x0xxxxxxxxx1110xxx0xxxx"),
		// A8.6.284 VCGT (register)
		// Encoding A1 / T1 Advanced SIMD
		// vcgt<c>.<dt> <Qd>, <Qn>, <Qm>	vcgt<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vcgt__reg_int, "vcgt", "1111001x0xxxxxxxxxxx0011xxx0xxxx"),
		// A8.6.284 VCGT (register)
		// Encoding A2 / T2 Advanced SIMD (UNDEFINED in integer-only variant)
		// vcgt<c>.f32 <Qd>, <Qn>, <Qm>	vcgt<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vcgt__reg_f32, "vcgt", "111100110x1xxxxxxxxx1110xxx0xxxx"),
		// A8.6.288 VCLS
		// Encoding A1 / T1 Advanced SIMD
		// vcls<c>.<dt> <Qd>, <Qm>	vcls<c>.<dt> <Dd>, <Dm>
		new OpcodeARM(Index.arm_vcls, "vcls", "111100111x11xx00xxxx01000xx0xxxx"),
		// A8.6.290 VCLT (immediate #0)
		// Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vclt<c>.<dt> <Qd>, <Qm>, #0	vclt<c>.<dt> <Dd>, <Dm>, #0
		new OpcodeARM(Index.arm_vclt, "vclt", "111100111x11xx01xxxx0x100xx0xxxx"),
		// A8.6.291 VCLZ
		// Encoding A1 / T1 Advanced SIMD
		// vclz<c>.<dt> <Qd>, <Qm>	vclz<c>.<dt> <Dd>, <Dm>
		new OpcodeARM(Index.arm_vclz, "vclz", "111100111x11xx00xxxx01001xx0xxxx"),
		// A8.6.292 VCMP, VCMPE
		// Encoding A1 / T1 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vcmp{e}<c>.f64 <Dd>, <Dm>	vcmp{e}<c>.f32 <Sd>, <Sm>
		new OpcodeARM(Index.arm_vcmp__reg, "vcmp", "xxxx11101x110100xxxx101xx1x0xxxx"),
		// A8.6.292 VCMP, VCMPE
		// Encoding A2 / T2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vcmp{e}<c>.f64 <Dd>, #0.0	vcmp{e}<c>.f32 <Sd>, #0.0
		// Unpredictable if (0) is 1: xxxx11101x110101xxxx101xx1(0)0(0)(0)(0)(0)
		new OpcodeARM(Index.arm_vcmp__to_0, "vcmp", "xxxx11101x110101xxxx101xx1x0xxxx"),
		// A8.6.297 VCVT (between floating-point and fixed-point, VFP)
		// Encoding A1 / T1 VFPv3 (sf = 1 UNDEFINED in single-precision only variants)
		// vcvt<c>.<Td>.f64 <Dd>, <Dd>, #<fbits>	vcvt<c>.<Td>.f32 <Sd>, <Sd>, #<fbits>
		// vcvt<c>.f64.<Td> <Dd>, <Dd>, #<fbits>	vcvt<c>.f32.<Td> <Sd>, <Sd>, #<fbits>
		// must precede arm_vcvt__fp_i_reg in table
		new OpcodeARM(Index.arm_vcvt__fp_fix_reg, "vcvt", "xxxx11101x111x1xxxxx101xx1x0xxxx"),
		// A8.6.295 VCVT, VCVTR (between floating-point and integer, VFP)
		// Encoding A1 / T1 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vcvt{r}<c>.s32.f64 <Sd>, <Dm>	vcvt{r}<c>.s32.f32 <Sd>, <Sm>	vcvt{r}<c>.u32.f64 <Sd>, <Dm>
		// vcvt{r}<c>.u32.f32 <Sd>, <Sm>	vcvt<c>.f64.<Tm> <Dd>, <Sm>	vcvt<c>.f32.<Tm> <Sd>, <Sm>
		// must follow arm_vcvt__fp_fix_reg in table
		new OpcodeARM(Index.arm_vcvt__fp_i_reg, "vcvt", "xxxx11101x111xxxxxxx101xx1x0xxxx"),
		// A8.6.277 VBIC (immediate)
		// Encoding A1 / T1 Advanced SIMD
		// vbic<c>.<dt> <Qd>, #<imm>	vbic<c>.<dt> <Dd>, #<imm>
		// A8.6.346 VORR (immediate)
		// Encoding A1 / T1 Advanced SIMD
		// vorr<c>.<dt> <Qd>, #<imm>	vorr<c>.<dt> <Dd>, #<imm>
		// A8.6.340 VMVN (immediate)
		// Encoding A1 / T1 Advanced SIMD
		// vmvn<c>.<dt> <Qd>, #<imm>	vmvn<c>.<dt> <Dd>, #<imm>
		// A8.6.326 VMOV (immediate)
		// Encoding A1 / T1 Advanced SIMD
		// vmov<c>.<dt> <Qd>, #<imm>	vmov<c>.<dt> <Dd>, #<imm>
		//
		// must precede arm_vcvt__fp_fix_vec in table
		new OpcodeARM(Index.arm_vmov_vbitwise, "_", "1111001x1x000xxxxxxxxxxx0xx1xxxx"),
		// A8.6.296 VCVT (between floating-point and fixed-point, Advanced SIMD)
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vcvt<c>.<Td>.<Tm> <Qd>, <Qm>, #<fbits>	vcvt<c>.<Td>.<Tm> <Dd>, <Dm>, #<fbits>
		// must follow arm_vmov_vbitwise in table
		new OpcodeARM(Index.arm_vcvt__fp_fix_vec, "vcvt", "1111001x1xxxxxxxxxxx111x0xx1xxxx"),
		// A8.6.298 VCVT (between double-precision and single-precision)
		// Encoding A1 / T1 VFPv2, VFPv3 (UNDEFINED in single-precision only variants)
		// vcvt<c>.f64.f32 <Dd>, <Sm>	vcvt<c>.f32.f64 <Sd>, <Dm>
		new OpcodeARM(Index.arm_vcvt__dp_sp, "vcvt", "xxxx11101x110111xxxx101x11x0xxxx"),
		// A8.6.300 VCVTB, VCVTT (between half-precision and single-precision, VFP)
		// Encoding A1 / T1 VFPv3 half-precision extensions
		// vcvt<y><c>.f32.f16 <Sd>, <Sm>	vcvt<y><c>.f16.f32 <Sd>, <Sm>
		new OpcodeARM(Index.arm_vcvt__hp_sp_reg, "vcvt", "xxxx11101x11001xxxxx1010x1x0xxxx"),
		// A8.6.301 VDIV
		// Encoding A1 / T1 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vdiv<c>.f64 <Dd>, <Dn>, <Dm>	vdiv<c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.arm_vdiv, "vdiv", "xxxx11101x00xxxxxxxx101xx0x0xxxx"),
		// A8.6.302 VDUP (scalar)
		// Encoding A1 / T1 Advanced SIMD
		// vdup<c>.<size> <Qd>, <Dm[x]>	vdup<c>.<size> <Dd>, <Dm[x]>
		new OpcodeARM(Index.arm_vdup__scalar, "vdup", "111100111x11xxxxxxxx11000xx0xxxx"),
		// A8.6.303 VDUP (ARM core register)
		// Encoding A1 / T1 Advanced SIMD
		// vdup<c>.<size> <Qd>, <Rt>	vdup<c>.<size> <Dd>, <Rt>
		// Unpredictable if (0) is 1: xxxx11101xx0xxxxxxxx1011x0x1(0)(0)(0)(0)
		new OpcodeARM(Index.arm_vdup__reg, "vdup", "xxxx11101xx0xxxxxxxx1011x0x1xxxx"),
		// A8.6.306 VHADD, VHSUB
		// Encoding A1 / T1 Advanced SIMD
		// vh<op><c> <Qd>, <Qn>, <Qm>	vh<op><c> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vhadd_vhsub, "vh", "1111001x0xxxxxxxxxxx00x0xxx0xxxx"),
		// A8.6.307 VLD1 (multiple single elements)
		// Encoding A1 / T1 Advanced SIMD
		// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.310 VLD2 (multiple 2-element structures)
		// Encoding A1 / T1 Advanced SIMD
		// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.313 VLD3 (multiple 3-element structures)
		// Encoding A1 / T1 Advanced SIMD
		// vld3<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld3<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.316 VLD4 (multiple 4-element structures)
		// Encoding A1 / T1 Advanced SIMD
		// vld4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		//
		new OpcodeARM(Index.arm_vld__multi, "vld", "111101000x10xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.308 VLD1 (single element to one lane)
		// Encoding A1 / T1 Advanced SIMD
		// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.309 VLD1 (single element to all lanes)
		// Encoding A1 / T1 Advanced SIMD
		// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.311 VLD2 (single 2-element structure to one lane)
		// Encoding A1 / T1 Advanced SIMD
		// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.312 VLD2 (single 2-element structure to all lanes)
		// Encoding A1 / T1 Advanced SIMD
		// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.314 VLD3 (single 3-element structure to one lane)
		// Encoding A1 / T1 Advanced SIMD
		// vld3<c>.<size> <list>, [<Rn>]{!}	vld3<c>.<size> <list>, [<Rn>], <Rm>
		// A8.6.315 VLD3 (single 3-element structure to all lanes)
		// Encoding A1 / T1 Advanced SIMD
		// vld3<c>.<size> <list>, [<Rn>]{!}	vld3<c>.<size> <list>, [<Rn>], <Rm>
		// A8.6.317 VLD4 (single 4-element structure to one lane)
		// Encoding A1 / T1 Advanced SIMD
		// vld4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.318 VLD4 (single 4-element structure to all lanes)
		// Encoding A1 / T1 Advanced SIMD
		// vld4<c>.<size> <list>, [<Rn>{ @<align>}]{!}	vld4<c>.<size> <list>, [<Rn>{ @<align>}], <Rm>
		//
		new OpcodeARM(Index.arm_vld__xlane, "vld", "111101001x10xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.320 VLDR
		// Encoding A1 / T1 VFPv2, VFPv3, Advanced SIMD
		// vldr<c> <Dd>, [<Rn>{, #+/-<imm>}]	vldr<c> <Dd>, <label>	vldr<c> <Dd>, [pc,#-0] Special case
		// must precede arm_vldm_1 in table
		new OpcodeARM(Index.arm_vldr__64, "vldr", "xxxx1101xx01xxxxxxxx1011xxxxxxxx"),
		// A8.6.332 VMOV (between two ARM core registers and a doubleword extension register)
		// Encoding A1 / T1 VFPv2, VFPv3, Advanced SIMD
		// vmov<c> <Dm>, <Rt>, <Rt2>	vmov<c> <Rt>, <Rt2>, <Dm>
		// must precede arm_vldm__64 in table
		new OpcodeARM(Index.arm_vmov_9, "vmov", "xxxx1100010xxxxxxxxx101100x1xxxx"),
		// A8.6.354 VPOP
		// Encoding A1 / T1 VFPv2, VFPv3, Advanced SIMD
		// vpop <list> <list> is consecutive 64-bit registers
		// A8.6.354 VPOP
		// Encoding A2 / T2 VFPv2, VFPv3
		// vpop <list> <list> is consecutive 32-bit registers
		// must precede arm_vldm_1 in table
		// must precede arm_vldm_2 in table
		// must precede arm_vldm__64 in table
		new OpcodeARM(Index.arm_vpop, "vpop", "xxxx11001x111101xxxx101xxxxxxxxx"),
		// A8.6.319 VLDM
		// Encoding A1 / T1 VFPv2, VFPv3, Advanced SIMD
		// vldm{mode}<c> <Rn>{!}, <list> <list> is consecutive 64-bit registers
		// must follow arm_vldr_1 in table
		// must follow arm_vmov_9 in table
		// must follow arm_vpop in table
		new OpcodeARM(Index.arm_vldm__64, "vldm", "xxxx110xxxx1xxxxxxxx1011xxxxxxxx"),
		// A8.6.331 VMOV (between two ARM core registers and two single-precision registers)
		// Encoding A1 / T1 VFPv2, VFPv3
		// vmov<c> <Sm>, <Sm1>, <Rt>, <Rt2>	vmov<c> <Rt>, <Rt2>, <Sm>, <Sm1>
		// must precede arm_vldr__32 in table
		new OpcodeARM(Index.arm_vmov_8, "vmov", "xxxx1100010xxxxxxxxx101000x1xxxx"),
		// A8.6.320 VLDR
		// Encoding A2 / T2 VFPv2, VFPv3
		// vldr<c> <Sd>, [<Rn>{, #+/-<imm>}]	vldr<c> <Sd>, <label>	vldr<c> <Sd>, [pc,#-0] Special case
		// must precede arm_vldm_2 in table
		// must follow arm_vmov_8 in table
		new OpcodeARM(Index.arm_vldr__32, "vldr", "xxxx1101xx01xxxxxxxx1010xxxxxxxx"),
		// A8.6.319 VLDM
		// Encoding A2 / T2 VFPv2, VFPv3
		// vldm{mode}<c> <Rn>{!}, <list> <list> is consecutive 32-bit registers
		// must follow arm_vldr_2 in table
		// must follow arm_vpop in table
		new OpcodeARM(Index.arm_vldm__32, "vldm", "xxxx110xxxx1xxxxxxxx1010xxxxxxxx"),
		// A8.6.321 VMAX, VMIN (integer)
		// Encoding A1 / T1 Advanced SIMD
		// vmax<c>.<dt> <Qd>, <Qn>, <Qm>	vmax<c>.<dt> <Dd>, <Dn>, <Dm>
		// vmin<c>.<dt> <Qd>, <Qn>, <Qm>	vmin<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vmax_vmin__int, "v", "1111001x0xxxxxxxxxxx0110xxxxxxxx"),
		// A8.6.322 VMAX, VMIN (floating-point)
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vmax<c>.f32 <Qd>, <Qn>, <Qm>	vmax<c>.f32 <Dd>, <Dn>, <Dm>
		// vmin<c>.f32 <Qd>, <Qn>, <Qm>	vmin<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vmax_vmin__fp, "v", "111100100xxxxxxxxxxx1111xxx0xxxx"),
		// A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
		// Encoding A1 / T1 Advanced SIMD
		// v<op><c>.<dt> <Qd>, <Qn>, <Qm>	v<op><c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vml__int, "vml", "1111001x0xxxxxxxxxxx1001xxx0xxxx"),
		// A8.6.406 VTBL, VTBX
		// Encoding A1 / T1 Advanced SIMD
		// v<op><c>.8 <Dd>, <list>, <Dm>
		// must precede arm_vml__int_long in table
		new OpcodeARM(Index.arm_vtb, "vtb", "111100111x11xxxxxxxx10xxxxx0xxxx"),
		// A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
		// Encoding A2 / T2 Advanced SIMD
		// v<op>l<c>.<dt> <Qd>, <Dn>, <Dm>
		// must follow arm_vtb in table
		new OpcodeARM(Index.arm_vml__int_long, "vml", "1111001x1xxxxxxxxxxx10x0x0x0xxxx"),
		// A8.6.324 VMLA, VMLS (floating-point)
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// v<op><c>.f32 <Qd>, <Qn>, <Qm>	v<op><c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vml__f32, "vml", "111100100xxxxxxxxxxx1101xxx1xxxx"),
		// A8.6.324 VMLA, VMLS (floating-point)
		// Encoding A2 / T2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// v<op><c>.f64 <Dd>, <Dn>, <Dm>	v<op><c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.arm_vml__fp, "vml", "xxxx11100x00xxxxxxxx101xxxx0xxxx"),
		// A8.6.341 VMVN (register)
		// Encoding A1 / T1 Advanced SIMD
		// vmvn<c> <Qd>, <Qm>	vmvn<c> <Dd>, <Dm>
		// must precede arm_vml__scalar in table
		new OpcodeARM(Index.arm_vmvn, "vmvn", "111100111x11xx00xxxx01011xx0xxxx"),
		// A8.6.348 VPADAL
		// Encoding A1 / T1 Advanced SIMD
		// vpadal<c>.<dt> <Qd>, <Qm>	vpadal<c>.<dt> <Dd>, <Dm>
		// must precede arm_vml__scalar in table
		new OpcodeARM(Index.arm_vpadal, "vpadal", "111100111x11xx00xxxx0110xxx0xxxx"),
		// A8.6.358 VQDMLAL, VQDMLSL
		// Encoding A2 / T2 Advanced SIMD
		// vqd<op><c>.<dt> <Qd>,<Dn>,<Dm[x]>
		// must precede arm_vml__scalar in table
		new OpcodeARM(Index.arm_vqdml__scalar, "vqdml", "111100101xxxxxxxxxxx0x11x1x0xxxx"),
		// A8.6.334 VMOVN
		// Encoding A1 / T1 Advanced SIMD
		// vmovn<c>.<dt> <Dd>, <Qm>
		// must precede arm_vqmov in table
		new OpcodeARM(Index.arm_vmovn, "vmovn", "111100111x11xx10xxxx001000x0xxxx"),
		// A8.6.351 VPADDL
		// Encoding A1 / T1 Advanced SIMD
		// vpaddl<c>.<dt> <Qd>, <Qm>	vpaddl<c>.<dt> <Dd>, <Dm>
		// must precede arm_vml__scalar in table
		new OpcodeARM(Index.arm_vpaddl, "vpaddl", "111100111x11xx00xxxx0010xxx0xxxx"),
		// A8.6.361 VQMOVN, VQMOVUN
		// Encoding A1 / T1 Advanced SIMD
		// vqmov{u}n<c>.<type><size> <Dd>, <Qm>
		// must follow arm_vmovn in table
		// must precede arm_vml__scalar in table
		new OpcodeARM(Index.arm_vqmov, "vqmov", "111100111x11xx10xxxx0010xxx0xxxx"),
		// A8.6.371 VRECPE
		// Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vrecpe<c>.<dt> <Qd>, <Qm>	vrecpe<c>.<dt> <Dd>, <Dm>
		// must precede arm_vml__scalar in table
		new OpcodeARM(Index.arm_vrecpe, "vrecpe", "111100111x11xx11xxxx010x0xx0xxxx"),
		// A8.6.378 VRSQRTE
		// Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vrsqrte<c>.<dt> <Qd>, <Qm>	vrsqrte<c>.<dt> <Dd>, <Dm>
		// must precede arm_vabal in table
		// must precede arm_vml__scalar in table
		new OpcodeARM(Index.arm_vrsqrte, "vrsqrte", "111100111x11xx11xxxx010x1xx0xxxx"),
		// A8.6.325 VMLA, VMLAL, VMLS, VMLSL (by scalar)
		// Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// v<op><c>.<dt> <Qd>, <Qn>, <Dm[x]>	v<op><c>.<dt> <Dd>, <Dn>, <Dm[x]>
		// A8.6.325 VMLA, VMLAL, VMLS, VMLSL (by scalar)
		// Encoding A2 / T2 Advanced SIMD
		// v<op>l<c>.<dt> <Qd>, <Dn>, <Dm[x]>
		// must follow arm_vmvn in table
		// must follow arm_vpadal in table
		// must follow arm_vpaddl in table
		// must follow arm_vqdml__scalar in table
		// must follow arm_vqmov in table
		// must follow arm_vrecpe in table
		// must follow arm_vrsqrte in table
		new OpcodeARM(Index.arm_vml__scalar, "vml", "1111001x1xxxxxxxxxxx0xxxx1x0xxxx"),
		// A8.6.326 VMOV (immediate)
		// Encoding A2 / T2 VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vmov<c>.f64 <Dd>, #<imm>	vmov<c>.f32 <Sd>, #<imm>
		// Unpredictable if (0) is 1: xxxx11101x11xxxxxxxx101x(0)0(0)0xxxx
		new OpcodeARM(Index.arm_vmov__imm, "vmov", "xxxx11101x11xxxxxxxx101xx0x0xxxx"),
		// A8.6.327 VMOV (register)
		// Encoding A1 / T1 Advanced SIMD
		// vmov<c> <Qd>, <Qm>	vmov<c> <Dd>, <Dm>
		// A8.6.347 VORR (register)
		// Encoding A1 / T1 Advanced SIMD
		// vorr<c> <Qd>, <Qn>, <Qm>	vorr<c> <Dd>, <Dn>, <Dm>
		//
		new OpcodeARM(Index.arm_vmov_vorr, "vmov", "111100100x10xxxxxxxx0001xxx1xxxx"),
		// A8.6.327 VMOV (register)
		// Encoding A2 / T2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vmov<c>.f64 <Dd>, <Dm>	vmov<c>.f32 <Sd>, <Sm>
		new OpcodeARM(Index.arm_vmov__reg_f, "vmov", "xxxx11101x110000xxxx101x01x0xxxx"),
		// A8.6.328 VMOV (ARM core register to scalar)
		// Encoding A1 / T1 VFPv2, VFPv3, Advanced SIMD for word version (opc1:opc2 == '00x00'); Advanced SIMD otherwise
		// vmov<c>.<size> <Dd[x]>, <Rt>
		// Unpredictable if (0) is 1: xxxx11100xx0xxxxxxxx1011xxx1(0)(0)(0)(0)
		new OpcodeARM(Index.arm_vmov_5, "vmov", "xxxx11100xx0xxxxxxxx1011xxx1xxxx"),
		// A8.6.329 VMOV (scalar to ARM core register)
		// Encoding A1 / T1 VFPv2, VFPv3, Advanced SIMD for word version (U:opc1:opc2 == '00x00'); Advanced SIMD otherwise
		// vmov<c>.<dt> <Rt>, <Dn[x]>
		// Unpredictable if (0) is 1: xxxx1110xxx1xxxxxxxx1011xxx1(0)(0)(0)(0)
		new OpcodeARM(Index.arm_vmov_6, "vmov", "xxxx1110xxx1xxxxxxxx1011xxx1xxxx"),
		// A8.6.330 VMOV (between ARM core register and single-precision register)
		// Encoding A1 / T1 VFPv2, VFPv3
		// vmov<c> <Sn>, <Rt>	vmov<c> <Sn>, <Rt>	vmov<c> <Rt>, <Sn>
		// Unpredictable if (0) is 1: xxxx1110000xxxxxxxxx1010x(0)(0)1(0)(0)(0)(0)
		new OpcodeARM(Index.arm_vmov_7, "vmov", "xxxx1110000xxxxxxxxx1010xxx1xxxx"),
		// A8.6.333 VMOVL
		// Encoding A1 / T1 Advanced SIMD
		// vmovl<c>.<dt> <Qd>, <Dm>
		// must precede arm_vshll_1 in table
		new OpcodeARM(Index.arm_vmovl, "vmovl", "1111001x1xxxx000xxxx101000x1xxxx"),
		// A8.6.384 VSHLL
		// Encoding A1 / T1 Advanced SIMD
		// vshll<c>.<type><size> <Qd>, <Dm>, #<imm> (0 < <imm> < <size>)
		// must follow arm_vmovl in table
		new OpcodeARM(Index.arm_vshll__various, "vshll", "1111001x1xxxxxxxxxxx101000x1xxxx"),
		// A8.6.335 VMRS
		// Encoding A1 / T1 VFPv2, VFPv3, Advanced SIMD
		// vmrs<c> <Rt>, fpscr
		// Unpredictable if (0) is 1: xxxx111011110001xxxx10100(0)(0)1(0)(0)(0)(0)
		// B6.1.14 VMRS
		// Encoding A1 / T1 VFPv2, VFPv3, Advanced SIMD
		// vmrs<c> <Rt>,<spec_reg>
		// Unpredictable if (0) is 1: xxxx11101111xxxxxxxx1010(0)(0)(0)1(0)(0)(0)(0)
		//
		new OpcodeARM(Index.arm_vmrs, "vmrs", "xxxx11101111xxxxxxxx1010xxx1xxxx"),
		// A8.6.336 VMSR
		// Encoding A1 / T1 VFPv2, VFPv3, Advanced SIMD
		// vmsr<c> fpscr, <Rt>
		// Unpredictable if (0) is 1: xxxx111011100001xxxx10100(0)(0)1(0)(0)(0)(0)
		// B6.1.15 VMSR
		// Encoding A1 / T2 VFPv2, VFPv3, Advanced SIMD
		// vmsr<c> <spec_reg>,<Rt>
		// Unpredictable if (0) is 1: xxxx11101110xxxxxxxx1010(0)(0)(0)1(0)(0)(0)(0)
		//
		new OpcodeARM(Index.arm_vmsr, "vmsr", "xxxx11101110xxxxxxxx1010xxx1xxxx"),
		// A8.6.337 VMUL, VMULL (integer and polynomial)
		// Encoding A1 / T1 Advanced SIMD
		// vmul<c>.<dt> <Qd>, <Qn>, <Qm>	vmul<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vmul_1, "vmul", "1111001x0xxxxxxxxxxx1001xxx1xxxx"),
		// A8.6.337 VMUL, VMULL (integer and polynomial)
		// Encoding A2 / T2 Advanced SIMD
		// vmull<c>.<dt> <Qd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vmull, "vmull", "1111001x1xxxxxxxxxxx11x0x0x0xxxx"),
		// A8.6.338 VMUL (floating-point)
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vmul<c>.f32 <Qd>, <Qn>, <Qm>	vmul<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vmul_f32, "vmul", "111100110x0xxxxxxxxx1101xxx1xxxx"),
		// A8.6.338 VMUL (floating-point)
		// Encoding A2 / T2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vmul<c>.f64 <Dd>, <Dn>, <Dm>	vmul<c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.arm_vmul__fp_2, "vmul", "xxxx11100x10xxxxxxxx101xx0x0xxxx"),
		// A8.6.360 VQDMULL
		// Encoding A2 / T2 Advanced SIMD
		// vqdmull<c>.<dt> <Qd>,<Dn>,<Dm[x]>
		// must precede arm_vmul__scalar in table
		new OpcodeARM(Index.arm_vqdmull__scalar, "vqdmul", "111100101xxxxxxxxxxx1011x1x0xxxx"),
		// A8.6.339 VMUL, VMULL (by scalar)
		// Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vmul<c>.<dt> <Qd>, <Qn>, <Dm[x]>	vmul<c>.<dt> <Dd>, <Dn>, <Dm[x]>
		// A8.6.339 VMUL, VMULL (by scalar)
		// Encoding A2 / T2 Advanced SIMD
		// vmull<c>.<dt> <Qd>, <Dn>, <Dm[x]>
		// must follow arm_vqdmull__scalar in table
		new OpcodeARM(Index.arm_vmul__scalar, "vmul", "1111001x1xxxxxxxxxxx10xxx1x0xxxx"),
		// A8.6.342 VNEG
		// Encoding A2 / T2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vneg<c>.f64 <Dd>, <Dm>	vneg<c>.f32 <Sd>, <Sm>
		new OpcodeARM(Index.arm_vneg__f, "vneg", "xxxx11101x110001xxxx101x01x0xxxx"),
		// A8.6.343 VNMLA, VNMLS, VNMUL
		// Encoding A1 / T1 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vnmla<c>.f64 <Dd>, <Dn>, <Dm>	vnmla<c>.f32 <Sd>, <Sn>, <Sm>
		// vnmls<c>.f64 <Dd>, <Dn>, <Dm>	vnmls<c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.arm_vnml, "vnml", "xxxx11100x01xxxxxxxx101xxxx0xxxx"),
		// A8.6.343 VNMLA, VNMLS, VNMUL
		// Encoding A2 / T2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vnmul<c>.f64 <Dd>, <Dn>, <Dm>	vnmul<c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.arm_vnmul, "vnmul", "xxxx11100x10xxxxxxxx101xx1x0xxxx"),
		// A8.6.345 VORN (register)
		// Encoding A1 / T1 Advanced SIMD
		// vorn<c> <Qd>, <Qn>, <Qm>	vorn<c> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vorn, "vorn", "111100100x11xxxxxxxx0001xxx1xxxx"),
		// A8.6.349 VPADD (integer)
		// Encoding A1 / T1 Advanced SIMD
		// vpadd<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vpadd__int, "vpadd", "111100100xxxxxxxxxxx1011xxx1xxxx"),
		// A8.6.350 VPADD (floating-point)
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vpadd<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vpadd__f32, "vpadd", "111100110x0xxxxxxxxx1101xxx0xxxx"),
		// A8.6.352 VPMAX, VPMIN (integer)
		// Encoding A1 / T1 Advanced SIMD
		// vp<op><c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vpmax_vpmin__int, "vp", "1111001x0xxxxxxxxxxx1010xxxxxxxx"),
		// A8.6.353 VPMAX, VPMIN (floating-point)
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vp<op><c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vpmax_vpmin__fp, "vp", "111100110xxxxxxxxxxx1111xxx0xxxx"),
		// A8.6.357 VQADD
		// Encoding A1 / T1 Advanced SIMD
		// vqadd<c>.<dt> <Qd>,<Qn>,<Qm>	vqadd<c>.<dt> <Dd>,<Dn>,<Dm>
		new OpcodeARM(Index.arm_vqadd, "vqadd", "1111001x0xxxxxxxxxxx0000xxx1xxxx"),
		// A8.6.358 VQDMLAL, VQDMLSL
		// Encoding A1 / T1 Advanced SIMD
		// vqd<op><c>.<dt> <Qd>,<Dn>,<Dm>
		new OpcodeARM(Index.arm_vqdml__vec, "vqdml", "111100101xxxxxxxxxxx10x1x0x0xxxx"),
		// A8.6.359 VQDMULH
		// Encoding A1 / T1 Advanced SIMD
		// vqdmulh<c>.<dt> <Qd>,<Qn>,<Qm>	vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm>
		new OpcodeARM(Index.arm_vqdmulh__vec, "vqdmulh", "111100100xxxxxxxxxxx1011xxx0xxxx"),
		// A8.6.359 VQDMULH
		// Encoding A2 / T2 Advanced SIMD
		// vqdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>	vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>
		new OpcodeARM(Index.arm_vqdmulh__scalar, "vqdmulh", "1111001x1xxxxxxxxxxx1100x1x0xxxx"),
		// A8.6.360 VQDMULL
		// Encoding A1 / T1 Advanced SIMD
		// vqdmull<c>.<dt> <Qd>,<Dn>,<Dm>	vqdmull<c>.<dt> <Qd>,<Dn>,<Dm>
		new OpcodeARM(Index.arm_vqdmull__vec, "vqdmull", "111100101xxxxxxxxxxx1101x0x0xxxx"),
		// A8.6.363 VQRDMULH
		// Encoding A1 / T1 Advanced SIMD
		// vqrdmulh<c>.<dt> <Qd>,<Qn>,<Qm>	vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm>
		new OpcodeARM(Index.arm_vqrdmulh__vec, "vqrdmulh", "111100110xxxxxxxxxxx1011xxx0xxxx"),
		// A8.6.363 VQRDMULH
		// Encoding A2 / T2 Advanced SIMD
		// vqrdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>	vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>
		new OpcodeARM(Index.arm_vqrdmulh__scalar, "vqrdmulh", "1111001x1xxxxxxxxxxx1101x1x0xxxx"),
		// A8.6.364 VQRSHL
		// Encoding A1 / T1 Advanced SIMD
		// vqrshl<c>.<type><size> <Qd>,<Qm>,<Qn>	vqrshl<c>.<type><size> <Dd>,<Dm>,<Dn>
		new OpcodeARM(Index.arm_vqrshl, "vqrshl", "1111001x0xxxxxxxxxxx0101xxx1xxxx"),
		// A8.6.377 VRSHRN
		// Encoding A1 / T1 Advanced SIMD
		// vrshrn<c>.i<size> <Dd>, <Qm>, #<imm>
		// must precede arm_vqrshr in table
		new OpcodeARM(Index.arm_vrshrn, "vrshrn", "111100101xxxxxxxxxxx100001x1xxxx"),
		// A8.6.365 VQRSHRN, VQRSHRUN
		// Encoding A1 / T1 Advanced SIMD
		// vqrshr{u}n<c>.<type><size> <Dd>,<Qm>,#<imm>
		// must follow arm_vrshrn in table
		new OpcodeARM(Index.arm_vqrshr, "vqrshr", "1111001x1xxxxxxxxxxx100x01x1xxxx"),
		// A8.6.366 VQSHL (register)
		// Encoding A1 / T1 Advanced SIMD
		// vqshl<c>.<type><size> <Qd>,<Qm>,<Qn>	vqshl<c>.<type><size> <Dd>,<Dm>,<Dn>
		new OpcodeARM(Index.arm_vqshl__reg, "vqshl", "1111001x0xxxxxxxxxxx0100xxx1xxxx"),
		// A8.6.367 VQSHL, VQSHLU (immediate)
		// Encoding A1 / T1 Advanced SIMD
		// vqshl{u}<c>.<type><size> <Qd>,<Qm>,#<imm>	vqshl{u}<c>.<type><size> <Dd>,<Dm>,#<imm>
		new OpcodeARM(Index.arm_vqshl__imm, "vqshl", "1111001x1xxxxxxxxxxx011xxxx1xxxx"),
		// A8.6.386 VSHRN
		// Encoding A1 / T1 Advanced SIMD
		// vshrn<c>.i<size> <Dd>, <Qm>, #<imm>
		// must precede arm_vqshr in table
		new OpcodeARM(Index.arm_vshrn, "vshrn", "111100101xxxxxxxxxxx100000x1xxxx"),
		// A8.6.368 VQSHRN, VQSHRUN
		// Encoding A1 / T1 Advanced SIMD
		// vqshr{u}n<c>.<type><size> <Dd>,<Qm>,#<imm>
		// must follow arm_vshrn in table
		new OpcodeARM(Index.arm_vqshr, "vqshr", "1111001x1xxxxxxxxxxx100x00x1xxxx"),
		// A8.6.369 VQSUB
		// Encoding A1 / T1 Advanced SIMD
		// vqsub<c>.<type><size> <Qd>, <Qn>, <Qm>	vqsub<c>.<type><size> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vqsub, "vqsub", "1111001x0xxxxxxxxxxx0010xxx1xxxx"),
		// A8.6.370 VRADDHN
		// Encoding A1 / T1 Advanced SIMD
		// vraddhn<c>.<dt> <Dd>, <Qn>, <Qm>
		new OpcodeARM(Index.arm_vraddhn, "vraddhn", "111100111xxxxxxxxxxx0100x0x0xxxx"),
		// A8.6.372 VRECPS
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vrecps<c>.f32 <Qd>, <Qn>, <Qm>	vrecps<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vrecps, "vrecps", "111100100x0xxxxxxxxx1111xxx1xxxx"),
		// A8.6.374 VRHADD
		// Encoding A1 / T1 Advanced SIMD
		// vrhadd<c> <Qd>, <Qn>, <Qm>	vrhadd<c> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vrhadd, "vrhadd", "1111001x0xxxxxxxxxxx0001xxx0xxxx"),
		// A8.6.375 VRSHL
		// Encoding A1 / T1 Advanced SIMD
		// vrshl<c>.<type><size> <Qd>, <Qm>, <Qn>	vrshl<c>.<type><size> <Dd>, <Dm>, <Dn>
		new OpcodeARM(Index.arm_vrshl, "vrshl", "1111001x0xxxxxxxxxxx0101xxx0xxxx"),
		// A8.6.376 VRSHR
		// Encoding A1 / T1 Advanced SIMD
		// vrshr<c>.<type><size> <Qd>, <Qm>, #<imm>	vrshr<c>.<type><size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.arm_vrshr, "vrshr", "1111001x1xxxxxxxxxxx0010xxx1xxxx"),
		// A8.6.266 VABA, VABAL
		// Encoding A2 / T2 Advanced SIMD
		// vabal<c>.<dt> <Qd>, <Dn>, <Dm>
		// must follow arm_vceq__imm0 in table
		// must follow arm_vcle in table
		// must follow arm_vcnt in table
		// must follow arm_vext in table
		// must follow arm_vrsqrte in table
		new OpcodeARM(Index.arm_vabal, "vabal", "1111001x1xxxxxxxxxxx0101x0x0xxxx"),
		// A8.6.379 VRSQRTS
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vrsqrts<c>.f32 <Qd>, <Qn>, <Qm>	vrsqrts<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vrsqrts, "vrsqrts", "111100100x1xxxxxxxxx1111xxx1xxxx"),
		// A8.6.380 VRSRA
		// Encoding A1 / T1 Advanced SIMD
		// vrsra<c>.<type><size> <Qd>, <Qm>, #<imm>	vrsra<c>.<type><size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.arm_vrsra, "vrsra", "1111001x1xxxxxxxxxxx0011xxx1xxxx"),
		// A8.6.381 VRSUBHN
		// Encoding A1 / T1 Advanced SIMD
		// vrsubhn<c>.<dt> <Dd>, <Qn>, <Qm>
		new OpcodeARM(Index.arm_vrsubhn, "vrsubhn", "111100111xxxxxxxxxxx0110x0x0xxxx"),
		// A8.6.382 VSHL (immediate)
		// Encoding A1 / T1 Advanced SIMD
		// vshl<c>.i<size> <Qd>, <Qm>, #<imm>	vshl<c>.i<size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.arm_vshl__imm, "vshl.i", "111100101xxxxxxxxxxx0101xxx1xxxx"),
		// A8.6.383 VSHL (register)
		// Encoding A1 / T1 Advanced SIMD
		// vshl<c>.i<size> <Qd>, <Qm>, <Qn>	vshl<c>.i<size> <Dd>, <Dm>, <Dn>
		new OpcodeARM(Index.arm_vshl__reg, "vshl", "1111001x0xxxxxxxxxxx0100xxx0xxxx"),
		// A8.6.384 VSHLL
		// Encoding A2 / T2 Advanced SIMD
		// vshll<c>.<type><size> <Qd>, <Dm>, #<imm> (<imm> == <size>)
		new OpcodeARM(Index.arm_vshll__max, "vshll", "111100111x11xx10xxxx001100x0xxxx"),
		// A8.6.385 VSHR
		// Encoding A1 / T1 Advanced SIMD
		// vshr<c>.<type><size> <Qd>, <Qm>, #<imm>	vshr<c>.<type><size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.arm_vshr, "vshr", "1111001x1xxxxxxxxxxx0000xxx1xxxx"),
		// A8.6.387 VSLI
		// Encoding A1 / T1 Advanced SIMD
		// vsli<c>.<size> <Qd>, <Qm>, #<imm>	vsli<c>.<size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.arm_vsli, "vsli.", "111100111xxxxxxxxxxx0101xxx1xxxx"),
		// A8.6.388 VSQRT
		// Encoding A1 / T1 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vsqrt<c>.f64 <Dd>, <Dm>	vsqrt<c>.f32 <Sd>, <Sm>
		new OpcodeARM(Index.arm_vsqrt, "vsqrt", "xxxx11101x110001xxxx101x11x0xxxx"),
		// A8.6.389 VSRA
		// Encoding A1 / T1 Advanced SIMD
		// vsra<c>.<type><size> <Qd>, <Qm>, #<imm>	vsra<c>.<type><size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.arm_vsra, "vsra", "1111001x1xxxxxxxxxxx0001xxx1xxxx"),
		// A8.6.390 VSRI
		// Encoding A1 / T1 Advanced SIMD
		// vsri<c>.<size> <Qd>, <Qm>, #<imm>	vsri<c>.<size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.arm_vsri, "vsri.", "111100111xxxxxxxxxxx0100xxx1xxxx"),
		// A8.6.391 VST1 (multiple single elements)
		// Encoding A1 / T1 Advanced SIMD
		// vst1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.393 VST2 (multiple 2-element structures)
		// Encoding A1 / T1 Advanced SIMD
		// vst2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.395 VST3 (multiple 3-element structures)
		// Encoding A1 / T1 Advanced SIMD
		// vst3<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst3<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.397 VST4 (multiple 4-element structures)
		// Encoding A1 / T1 Advanced SIMD
		// vst4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		new OpcodeARM(Index.arm_vst__multi, "vst", "111101000x00xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.392 VST1 (single element from one lane)
		// Encoding A1 / T1 Advanced SIMD
		// vst1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.394 VST2 (single 2-element structure from one lane)
		// Encoding A1 / T1 Advanced SIMD
		// vst2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.396 VST3 (single 3-element structure from one lane)
		// Encoding A1 / T1 Advanced SIMD
		// vst3<c>.<size> <list>, [<Rn>]{!}	vst3<c>.<size> <list>, [<Rn>], <Rm>
		// A8.6.398 VST4 (single 4-element structure from one lane)
		// Encoding A1 / T1 Advanced SIMD
		// vst4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		//
		new OpcodeARM(Index.arm_vst__xlane, "vst", "111101001x00xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.355 VPUSH
		// Encoding A1 / T1 VFPv2, VFPv3, Advanced SIMD
		// vpush<c> <list> <list> is consecutive 64-bit registers
		// A8.6.355 VPUSH
		// Encoding A2 / T2 VFPv2, VFPv3
		// vpush<c> <list> <list> is consecutive 32-bit registers
		// must precede arm_vstm_1 in table
		// must precede arm_vstm_2 in table
		new OpcodeARM(Index.arm_vpush, "vpush", "xxxx11010x101101xxxx101xxxxxxxxx"),
		// A8.6.400 VSTR
		// Encoding A1 / T1 VFPv2, VFPv3, Advanced SIMD
		// vstr<c> <Dd>, [<Rn>{, #+/-<imm>}]
		// must precede arm_vstm_1 in table
		new OpcodeARM(Index.arm_vstr__64, "vstr", "xxxx1101xx00xxxxxxxx1011xxxxxxxx"),
		// A8.6.399 VSTM
		// Encoding A1 / T1 Advanced SIMD
		// vstm{mode}<c> <Rn>{!}, <list> <list> is consecutive 64-bit registers
		// must follow arm_vpush in table
		// must follow arm_vstr_1 in table
		new OpcodeARM(Index.arm_vstm__64, "vstm", "xxxx110xxxx0xxxxxxxx1011xxxxxxxx"),
		// A8.6.400 VSTR
		// Encoding A2 / T2 VFPv2, VFPv3
		// vstr<c> <Sd>, [<Rn>{, #+/-<imm>}]
		// must precede arm_vstm_2 in table
		new OpcodeARM(Index.arm_vstr__32, "vstr", "xxxx1101xx00xxxxxxxx1010xxxxxxxx"),
		// A8.6.399 VSTM
		// Encoding A2 / T2 VFPv2, VFPv3
		// vstm{mode}<c> <Rn>{!}, <list> <list> is consecutive 32-bit registers
		// must follow arm_vpush in table
		// must follow arm_vstr_2 in table
		new OpcodeARM(Index.arm_vstm__32, "vstm", "xxxx110xxxx0xxxxxxxx1010xxxxxxxx"),
		// A8.6.401 VSUB (integer)
		// Encoding A1 / T1 Advanced SIMD
		// vsub<c>.<dt> <Qd>, <Qn>, <Qm>	vsub<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vsub__int, "vsub", "111100110xxxxxxxxxxx1000xxx0xxxx"),
		// A8.6.402 VSUB (floating-point)
		// Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vsub<c>.f32 <Qd>, <Qn>, <Qm>	vsub<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vsub__f32, "vsub", "111100100x1xxxxxxxxx1101xxx0xxxx"),
		// A8.6.402 VSUB (floating-point)
		// Encoding A2 / T2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vsub<c>.f64 <Dd>, <Dn>, <Dm>	vsub<c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.arm_vsub__fp_f, "vsub", "xxxx11100x11xxxxxxxx101xx1x0xxxx"),
		// A8.6.403 VSUBHN
		// Encoding A1 / T1 Advanced SIMD
		// vsubhn<c>.<dt> <Dd>, <Qn>, <Qm>
		new OpcodeARM(Index.arm_vsubhn, "vsubhn", "111100101xxxxxxxxxxx0110x0x0xxxx"),
		// A8.6.404 VSUBL, VSUBW
		// Encoding A1 / T1 Advanced SIMD
		// vsubl<c>.<dt> <Qd>, <Dn>, <Dm>	vsubw<c>.<dt> {<Qd>,} <Qn>, <Dm>
		new OpcodeARM(Index.arm_vsubl_vsubw, "vsub", "1111001x1xxxxxxxxxxx001xx0x0xxxx"),
		// A8.6.408 VTST
		// Encoding A1 / T1 Advanced SIMD
		// vtst<c>.<size> <Qd>, <Qn>, <Qm>	vtst<c>.<size> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.arm_vtst, "vtst", "111100100xxxxxxxxxxx1000xxx1xxxx"),

		// A8.6.19 BIC (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// bic{s}<c> <Rd>,<Rn>,#<const>
		new OpcodeARM(Index.arm_bic__imm, "bic", "xxxx0011110xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.32 CMN (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// cmn<c> <Rn>,#<const>
		// Unpredictable if (0) is 1: xxxx00110111xxxx(0)(0)(0)(0)xxxxxxxxxxxx
		new OpcodeARM(Index.arm_cmn__imm, "cmn", "xxxx00110111xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.35 CMP (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// cmp<c> <Rn>,#<const>
		// Unpredictable if (0) is 1: xxxx00110101xxxx(0)(0)(0)(0)xxxxxxxxxxxx
		new OpcodeARM(Index.arm_cmp__imm, "cmp", "xxxx00110101xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.99 MOVT
		// Encoding A1 ARMv6T2, ARMv7
		// movt<c> <Rd>,#<imm16>
		new OpcodeARM(Index.arm_movt, "movt", "xxxx00110100xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.103 MSR (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// msr<c> <spec_reg>,#<const>
		// Unpredictable if (1) is 0: xxxx00110010xx00(1)(1)(1)(1)xxxxxxxxxxxx
		// must precede arm_msr__sys_imm in table
		// must follow arm_nop in table
		// must follow arm_sev in table
		// must follow arm_wfe in table
		// must follow arm_wfi in table
		// must follow arm_yield in table
		new OpcodeARM(Index.arm_msr__imm, "msr", "xxxx00110010xx00xxxxxxxxxxxxxxxx"),
		// B6.1.6 MSR (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// msr<c> <spec_reg>,#<const>
		// Unpredictable if (1) is 0: xxxx00110x10xxxx(1)(1)(1)(1)xxxxxxxxxxxx
		// must follow arm_msr__imm in table in table
		new OpcodeARM(Index.arm_msr__sys_imm, "msr", "xxxx00110x10xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.106 MVN (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// mvn{s}<c> <Rd>,#<const>
		// Unpredictable if (0) is 1: xxxx0011111x(0)(0)(0)(0)xxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_mvn__imm, "mvn", "xxxx0011111xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.142 RSB (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// rsb{s}<c> <Rd>,<Rn>,#<const>
		new OpcodeARM(Index.arm_rsb__imm, "rsb", "xxxx0010011xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.145 RSC (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// rsc{s}<c> <Rd>,<Rn>,#<const>
		new OpcodeARM(Index.arm_rsc__imm, "rsc", "xxxx0010111xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.151 SBC (immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// sbc{s}<c> <Rd>,<Rn>,#<const>
		new OpcodeARM(Index.arm_sbc__imm, "sbc", "xxxx0010110xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.199 STRBT
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// strbt<c> <Rt>,[<Rn>],#+/-<imm12>
		// must precede arm_strb__imm in table
		new OpcodeARM(Index.arm_strbt__imm, "strbt", "xxxx0100x110xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.197 STRB (immediate, ARM)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// strb<c> <Rt>,[<Rn>{,#+/-<imm12>}]	strb<c> <Rt>,[<Rn>],#+/-<imm12>	strb<c> <Rt>,[<Rn>,#+/-<imm12>]!
		// must follow arm_strbt__imm in table
		new OpcodeARM(Index.arm_strb__imm, "strb", "xxxx010xx1x0xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.212 SUB (immediate, ARM)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// sub{s}<c> <Rd>,<Rn>,#<const>
		// A8.6.215 SUB (SP minus immediate)
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// sub{s}<c> <Rd>,sp,#<const>
		//
		// must follow arm_adr__lower in table
		new OpcodeARM(Index.arm_sub__imm, "sub", "xxxx0010010xxxxxxxxxxxxxxxxxxxxx"),

		// Coprocessor instructions

		// A8.6.28 CDP, CDP2
		// Encoding A2 / T2 ARMv5T*, ARMv6*, ARMv7
		// cdp2<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>
		// must precede arm_cdp in table
		new OpcodeARM(Index.arm_cdp2, "cdp2", "11111110xxxxxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.28 CDP, CDP2
		// Encoding A1 / T1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// cdp<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>
		// must follow arm_cdp2 in table
		new OpcodeARM(Index.arm_cdp, "cdp", "xxxx1110xxxxxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.92 MCR, MCR2
		// Encoding A2 / T2 ARMv5T*, ARMv6*, ARMv7
		// mcr2<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// must precede arm_mcr in table
		new OpcodeARM(Index.arm_mcr2, "mcr2", "11111110xxx0xxxxxxxxxxxxxxx1xxxx"),
		// A8.6.92 MCR, MCR2
		// Encoding A1 / T2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// mcr<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// must follow arm_mcr2 in table
		new OpcodeARM(Index.arm_mcr, "mcr", "xxxx1110xxx0xxxxxxxxxxxxxxx1xxxx"),
		// A8.6.100 MRC, MRC2
		// Encoding A2 / T2 ARMv5T*, ARMv6*, ARMv7
		// mrc2<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// must precede arm_mrc in table
		new OpcodeARM(Index.arm_mrc2, "mrc2", "11111110xxx1xxxxxxxxxxxxxxx1xxxx"),
		// A8.6.100 MRC, MRC2
		// Encoding A1 / T1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// mrc<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// must follow arm_mrc2 in table
		new OpcodeARM(Index.arm_mrc, "mrc", "xxxx1110xxx1xxxxxxxxxxxxxxx1xxxx"),
		// A8.6.101 MRRC, MRRC2
		// Encoding A2 / T1 ARMv6*, ARMv7
		// mrrc2<c> <coproc>,<opc>,<Rt>,<Rt2>,<CRm>
		// must precede arm_mrrc in table
		// must precede arm_ldc__lit in table
		// must precede arm_ldc__imm in table
		new OpcodeARM(Index.arm_mrrc2, "mrrc2", "111111000101xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.101 MRRC, MRRC2
		// Encoding A1 / T1 ARMv5TE*, ARMv6*, ARMv7
		// mrrc<c> <coproc>,<opc>,<Rt>,<Rt2>,<CRm>
		// must follow arm_mrrc2 in table
		// must precede arm_ldc__lit in table
		// must precede arm_ldc__imm in table
		new OpcodeARM(Index.arm_mrrc, "mrrc", "xxxx11000101xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.52 LDC, LDC2 (literal)
		// Encoding A2 / T2 ARMv5T*, ARMv6*, ARMv7
		// ldc2{l}<c> <coproc>,<CRd>,<label>	ldc2{l}<c> <coproc>,<CRd>,[pc,#-0]
		// Special case	ldc2{l}<c> <coproc>,<CRd>,[pc],<option>
		// must precede arm_ldc__imm in table
		// must follow arm_mrrc in table
		// must follow arm_mrrc2 in table
		new OpcodeARM(Index.arm_ldc2__lit, "ldc2", "1111110xxxx11111xxxxxxxxxxxxxxxx"),
		// A8.6.52 LDC, LDC2 (literal)
		// Encoding A1 / T1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldc{l}<c> <coproc>,<CRd>,<label>	ldc{l}<c> <coproc>,<CRd>,[pc,#-0]
		// Special case	ldc{l}<c> <coproc>,<CRd>,[pc],<option>
		// must follow arm_ldc2__lit in table
		// must precede arm_ldc2__imm in table
		new OpcodeARM(Index.arm_ldc__lit, "ldc", "xxxx110xxxx11111xxxxxxxxxxxxxxxx"),
		// A8.6.51 LDC, LDC2 (immediate)
		// Encoding A2 / T2 ARMv5T*, ARMv6*, ARMv7
		// ldc2{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	ldc2{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>
		// ldc2{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// must precede arm_ldc__lit in table
		new OpcodeARM(Index.arm_ldc2__imm, "ldc2", "1111110xxxx1xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.51 LDC, LDC2 (immediate)
		// Encoding A1 / T1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// ldc{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	ldc{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>
		// ldc{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// must follow arm_ldc2__imm in table
		// must follow arm_mrrc in table
		// must follow arm_mrrc2 in table
		new OpcodeARM(Index.arm_ldc__imm, "ldc", "xxxx110xxxx1xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.93 MCRR, MCRR2
		// Encoding A2 / T2 ARMv6*, ARMv7
		// mcrr2<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
		// must precede arm_mcrr in table
		// must precede arm_stc in table
		// must precede arm_stc2 in table
		new OpcodeARM(Index.arm_mcrr2, "mcrr2", "111111000100xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.93 MCRR, MCRR2
		// Encoding A1 / T1 ARMv5TE*, ARMv6*, ARMv7
		// mcrr<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
		// must follow arm_mcrr2 in table
		// must precede arm_stc in table
		// must precede arm_stc2 in table
		new OpcodeARM(Index.arm_mcrr, "mcrr", "xxxx11000100xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.188 STC, STC2
		// Encoding A2 / T2 ARMv5T*, ARMv6*, ARMv7
		// stc2{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	stc2{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>
		// stc2{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// must precede stc in table
		// must follow arm_ldc2__lit in table
		// must follow arm_mcrr in table
		// must follow arm_mcrr2 in table
		new OpcodeARM(Index.arm_stc2, "stc2", "1111110xxxx0xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.188 STC, STC2
		// Encoding A1 / T1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// stc{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	stc{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>
		// stc{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// must follow arm_ldc2__lit in table
		// must follow arm_stc2 in table
		// must follow arm_mcrr in table
		// must follow arm_mcrr2 in table
		new OpcodeARM(Index.arm_stc, "stc", "xxxx110xxxx0xxxxxxxxxxxxxxxxxxxx"),

		new OpcodeARM(Index.arm_undefined, "undefined", "111001111111xxxxxxxxxxxxxxx1xxxx"),
	};

	 // Reference manual citations (e.g., "A8.6.97") refer to sections in the ARM Architecture
	 // Reference Manual ARMv7-A and ARMv7-R Edition, Errata markup
	public static final OpcodeARM thumb_opcode_table[] = {

		// must precede thumb_b_1 in table
		new OpcodeARM(Index.thumb_undefined, "undefined", "11011110xxxxxxxx"),

		// A8.6.97 MOV (register)
		// Encoding T1 ARMv6*, ARMv7 if <Rd> and <Rm> both from R0-R7; ARMv4T, ARMv5T*, ARMv6*, ARMv7 otherwise
		// mov<c> <Rd>,<Rm> If <Rd> is the PC, must be outside or last in IT block.
		new OpcodeARM(Index.thumb_mov__reg, "mov", "01000110xxxxxxxx"),
		// A8.6.2 ADC (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// adcs <Rdn>,<Rm> Outside IT block.	adc<c> <Rdn>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_adc, "adc", "0100000101xxxxxx"),
		// A8.6.4 ADD (immediate, Thumb)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// adds <Rd>,<Rn>,#<imm3> Outside IT block.	add<c> <Rd>,<Rn>,#<imm3> Inside IT block.
		new OpcodeARM(Index.thumb_add__reg_imm, "add", "0001110xxxxxxxxx"),
		// A8.6.8 ADD (SP plus immediate)
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// add<c> sp,sp,#<imm>
		new OpcodeARM(Index.thumb_add__imm_to_sp, "add", "101100000xxxxxxx"),
		// A8.6.4 ADD (immediate, Thumb)
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// adds <Rdn>,#<imm8> Outside IT block.	add<c> <Rdn>,#<imm8> Inside IT block.
		new OpcodeARM(Index.thumb_add__imm, "add", "00110xxxxxxxxxxx"),
		// A8.6.6 ADD (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// adds <Rd>,<Rn>,<Rm> Outside IT block.	add<c> <Rd>,<Rn>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_add__reg_reg, "add", "0001100xxxxxxxxx"),
		// A8.6.6 ADD (register)
		// Encoding T2 ARMv6T2, ARMv7 if <Rdn> and <Rm> are both from R0-R7; ARMv4T, ARMv5T*, ARMv6*, ARMv7 otherwise
		// add<c> <Rdn>,<Rm> If <Rdn> is the PC, must be outside or last in IT block.
		// A8.6.9 ADD (SP plus register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// add<c> <Rdm>, sp, <Rdm>
		// A8.6.9 ADD (SP plus register)
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// add<c> sp,<Rm>
		//
		new OpcodeARM(Index.thumb_add__reg, "add", "01000100xxxxxxxx"),
		// A8.6.8 ADD (SP plus immediate)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// add<c> <Rd>,sp,#<imm>
		new OpcodeARM(Index.thumb_add__sp_imm, "add", "10101xxxxxxxxxxx"),
		// A8.6.10 ADR
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// adr<c> <Rd>,<label>	add <Rd>,pc,imm8		Alternative form
		new OpcodeARM(Index.thumb_adr, "add", "10100xxxxxxxxxxx"),
		// A8.6.12 AND (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// ands <Rdn>,<Rm> Outside IT block.	and<c> <Rdn>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_and, "and", "0100000000xxxxxx"),
		// A8.6.14 ASR (immediate)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// asrs <Rd>,<Rm>,#<imm> Outside IT block.	asr<c> <Rd>,<Rm>,#<imm> Inside IT block.
		new OpcodeARM(Index.thumb_asr__imm, "asr", "00010xxxxxxxxxxx"),
		// A8.6.15 ASR (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// asrs <Rdn>,<Rm> Outside IT block.	asr<c> <Rdn>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_asr__reg, "asr", "0100000100xxxxxx"),
		// A8.6.218 SVC (previously SWI)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// svc<c> #<imm8>
		// must precede thumb_b_1 in table
		new OpcodeARM(Index.thumb_svc, "svc", "11011111xxxxxxxx"),
		// A8.6.16 B
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// b<c> <label> Not permitted in IT block.
		// must follow thumb_b_1 in table
		new OpcodeARM(Index.thumb_b_1, "b", "1101xxxxxxxxxxxx"),
		// A8.6.16 B
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// b<c> <label> Outside or last in IT block
		new OpcodeARM(Index.thumb_b_2, "b", "11100xxxxxxxxxxx"),
		// A8.6.20 BIC (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// bics <Rdn>,<Rm> Outside IT block.	bic<c> <Rdn>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_bic, "bic", "0100001110xxxxxx"),
		// A8.6.22 BKPT
		// Encoding T1 ARMv5T*, ARMv6*, ARMv7
		// bkpt #<imm8>
		new OpcodeARM(Index.thumb_bkpt, "bkpt", "10111110xxxxxxxx"),
		// A8.6.24 BLX (register)
		// Encoding T1 ARMv5T*, ARMv6*, ARMv7
		// blx<c> <Rm> Outside or last in IT block
		// Unpredictable if (0) is 1: 010001111xxxx(0)(0)(0)
		new OpcodeARM(Index.thumb_blx, "blx", "010001111xxxxxxx"),
		// A8.6.25 BX
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// bx<c> <Rm> Outside or last in IT block
		// Unpredictable if (0) is 1: 010001110xxxx(0)(0)(0)
		new OpcodeARM(Index.thumb_bx, "bx", "010001110xxxxxxx"),
		// A8.6.27 CBNZ, CBZ
		// Encoding T1 ARMv6T2, ARMv7
		// cb{n}z <Rn>,<label> Not permitted in IT block.
		new OpcodeARM(Index.thumb_cbnz_cbz, "cb", "1011x0x1xxxxxxxx"),
		// A8.6.33 CMN (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// cmn<c> <Rn>,<Rm>
		new OpcodeARM(Index.thumb_cmn, "cmn", "0100001011xxxxxx"),
		// A8.6.35 CMP (immediate)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// cmp<c> <Rn>,#<imm8>
		new OpcodeARM(Index.thumb_cmp__imm, "cmp", "00101xxxxxxxxxxx"),
		// A8.6.36 CMP (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// cmp<c> <Rn>,<Rm> <Rn> and <Rm> both from R0-R7
		new OpcodeARM(Index.thumb_cmp__reg, "cmp", "0100001010xxxxxx"),
		// A8.6.36 CMP (register)
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// cmp<c> <Rn>,<Rm> <Rn> and <Rm> not both from R0-R7
		new OpcodeARM(Index.thumb_cmp__reg_hi, "cmp", "01000101xxxxxxxx"),
		// A8.6.45 EOR (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// eors <Rdn>,<Rm> Outside IT block.	eor<c> <Rdn>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_eor, "eor", "0100000001xxxxxx"),
		// A8.6.110 NOP
		// Encoding T1 ARMv6T2, ARMv7
		// nop<c>
		// must precede thumb_it in table
		new OpcodeARM(Index.thumb_nop, "nop", "1011111100000000"),
		// A8.6.158 SEV
		// Encoding T1 ARMv7 (executes as NOP in ARMv6T2)
		// sev<c>
		// must precede thumb_it in table
		new OpcodeARM(Index.thumb_sev, "sev", "1011111101000000"),
		// A8.6.411 WFE
		// Encoding T1 ARMv7 (executes as NOP in ARMv6T2)
		// wfe<c>
		// must precede thumb_it in table
		new OpcodeARM(Index.thumb_wfe, "wfe", "1011111100100000"),
		// A8.6.412 WFI
		// Encoding T1 ARMv7 (executes as NOP in ARMv6T2)
		// wfi<c>
		// must precede thumb_it in table
		new OpcodeARM(Index.thumb_wfi, "wfi", "1011111100110000"),
		// A8.6.413 YIELD
		// Encoding T1 ARMv7 (executes as NOP in ARMv6T2)
		// yield<c>
		// must precede thumb_it in table
		new OpcodeARM(Index.thumb_yield, "yield", "1011111100010000"),
		// A8.6.50 IT
		// Encoding T1 ARMv6T2, ARMv7
		// it{x{y{z}}} <firstcond> Not permitted in IT block
		// must follow thumb_nop in table
		// must follow thumb_sev in table
		// must follow thumb_wfe in table
		// must follow thumb_wfi in table
		// must follow thumb_yield in table
		new OpcodeARM(Index.thumb_it, "it", "10111111xxxxxxxx"),
		// A8.6.53 LDM / LDMIA / LDMFD
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7 (not in ThumbEE)
		// ldm<c> <Rn>!,<registers> <Rn> not included in <registers>	ldm<c> <Rn>,<registers> <Rn> included in <registers>
		new OpcodeARM(Index.thumb_ldm, "ldm", "11001xxxxxxxxxxx"),
		// A8.6.57 LDR (immediate, Thumb)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// ldr<c> <Rt>, [<Rn>{,#<imm>}]
		new OpcodeARM(Index.thumb_ldr__imm, "ldr", "01101xxxxxxxxxxx"),
		// A8.6.57 LDR (immediate, Thumb)
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// ldr<c> <Rt>,[sp{,#<imm>}]
		new OpcodeARM(Index.thumb_ldr__imm_sp, "ldr", "10011xxxxxxxxxxx"),
		// A8.6.59 LDR (literal)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// ldr<c> <Rt>,<label>
		new OpcodeARM(Index.thumb_ldr__lit, "ldr", "01001xxxxxxxxxxx"),
		// A8.6.60 LDR (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// ldr<c> <Rt>,[<Rn>,<Rm>]
		new OpcodeARM(Index.thumb_ldr__reg, "ldr", "0101100xxxxxxxxx"),
		// A8.6.61 LDRB (immediate, Thumb)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// ldrb<c> <Rt>,[<Rn>{,#<imm5>}]
		new OpcodeARM(Index.thumb_ldrb__imm, "ldrb", "01111xxxxxxxxxxx"),
		// A8.6.64 LDRB (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// ldrb<c> <Rt>,[<Rn>,<Rm>]
		new OpcodeARM(Index.thumb_ldrb__reg, "ldrb", "0101110xxxxxxxxx"),
		// A8.6.73 LDRH (immediate, Thumb)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// ldrh<c> <Rt>,[<Rn>{,#<imm>}]
		new OpcodeARM(Index.thumb_ldrh__imm, "ldrh", "10001xxxxxxxxxxx"),
		// A8.6.76 LDRH (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// ldrh<c> <Rt>,[<Rn>,<Rm>]
		new OpcodeARM(Index.thumb_ldrh__reg, "ldrh", "0101101xxxxxxxxx"),
		// A8.6.80 LDRSB (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// ldrsb<c> <Rt>,[<Rn>,<Rm>]
		new OpcodeARM(Index.thumb_ldrsb, "ldrsb", "0101011xxxxxxxxx"),
		// A8.6.84 LDRSH (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// ldrsh<c> <Rt>,[<Rn>,<Rm>]
		new OpcodeARM(Index.thumb_ldrsh, "ldrsh", "0101111xxxxxxxxx"),
		// A8.6.97 MOV (register)
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// movs <Rd>,<Rm> Not permitted in IT block
		// must precede thumb_lsl__imm in table
		new OpcodeARM(Index.thumb_movs, "movs", "0000000000xxxxxx"),
		// A8.6.88 LSL (immediate)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// lsls <Rd>,<Rm>,#<imm5> Outside IT block.	lsl<c> <Rd>,<Rm>,#<imm5> Inside IT block.
		// must follow thumb_movs in table
		new OpcodeARM(Index.thumb_lsl__imm, "lsl", "00000xxxxxxxxxxx"),
		// A8.6.89 LSL (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// lsls <Rdn>,<Rm> Outside IT block.	lsl<c> <Rdn>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_lsl__reg, "lsl", "0100000010xxxxxx"),
		// A8.6.90 LSR (immediate)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// lsrs <Rd>,<Rm>,#<imm> Outside IT block.	lsr<c> <Rd>,<Rm>,#<imm> Inside IT block.
		new OpcodeARM(Index.thumb_lsr__imm, "lsr", "00001xxxxxxxxxxx"),
		// A8.6.91 LSR (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// lsrs <Rdn>,<Rm> Outside IT block.	lsr<c> <Rdn>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_lsr__reg, "lsr", "0100000011xxxxxx"),
		// A8.6.96 MOV (immediate)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// movs <Rd>,#<imm8> Outside IT block.	mov<c> <Rd>,#<imm8> Inside IT block.
		new OpcodeARM(Index.thumb_mov__imm, "mov", "00100xxxxxxxxxxx"),
		// A8.6.105 MUL
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// muls <Rdm>,<Rn>,<Rdm> Outside IT block.	mul<c> <Rdm>,<Rn>,<Rdm> Inside IT block.
		new OpcodeARM(Index.thumb_mul, "mul", "0100001101xxxxxx"),
		// A8.6.107 MVN (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// mvns <Rd>,<Rm> Outside IT block.	mvn<c> <Rd>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_mvn, "mvn", "0100001111xxxxxx"),
		// A8.6.114 ORR (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// orrs <Rdn>,<Rm> Outside IT block.	orr<c> <Rdn>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_orr, "orr", "0100001100xxxxxx"),
		// A8.6.122 POP
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// pop<c> <registers>
		new OpcodeARM(Index.thumb_pop, "pop", "1011110xxxxxxxxx"),
		// A8.6.123 PUSH
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// push<c> <registers>
		new OpcodeARM(Index.thumb_push, "push", "1011010xxxxxxxxx"),
		// A8.6.135 REV
		// Encoding T1 ARMv6*, ARMv7
		// rev<c> <Rd>,<Rm>
		new OpcodeARM(Index.thumb_rev, "rev", "1011101000xxxxxx"),
		// A8.6.136 REV16
		// Encoding T1 ARMv6*, ARMv7
		// rev16<c> <Rd>,<Rm>
		new OpcodeARM(Index.thumb_rev16, "rev16", "1011101001xxxxxx"),
		// A8.6.137 REVSH
		// Encoding T1 ARMv6*, ARMv7
		// revsh<c> <Rd>,<Rm>
		new OpcodeARM(Index.thumb_revsh, "revsh", "1011101011xxxxxx"),
		// A8.6.140 ROR (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// rors <Rdn>,<Rm> Outside IT block.	ror<c> <Rdn>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_ror, "ror", "0100000111xxxxxx"),
		// A8.6.142 RSB (immediate)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// rsbs <Rd>,<Rn>,#0 Outside IT block.	rsb<c> <Rd>,<Rn>,#0 Inside IT block.
		new OpcodeARM(Index.thumb_rsb, "rsb", "0100001001xxxxxx"),
		// A8.6.152 SBC (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// sbcs <Rdn>,<Rm> Outside IT block.	sbc<c> <Rdn>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_sbc, "sbc", "0100000110xxxxxx"),
		// A8.6.157 SETEND
		// Encoding T1 ARMv6*, ARMv7
		// setend <endian_specifier> Not permitted in IT block
		// Unpredictable if (1) is 0 or (0) is 1: 10110110010(1)x(0)(0)(0)
		new OpcodeARM(Index.thumb_setend, "setend", "10110110010xxxxx"),
		// A8.6.189 STM / STMIA / STMEA
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7 (not in ThumbEE)
		// stm<c> <Rn>!,<registers>
		new OpcodeARM(Index.thumb_stm, "stm", "11000xxxxxxxxxxx"),
		// A8.6.193 STR (immediate, Thumb)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// str<c> <Rt>, [<Rn>{,#<imm>}]
		new OpcodeARM(Index.thumb_str__imm, "str", "01100xxxxxxxxxxx"),
		// A8.6.193 STR (immediate, Thumb)
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// str<c> <Rt>,[sp,#<imm>]
		new OpcodeARM(Index.thumb_str__imm_sp, "str", "10010xxxxxxxxxxx"),
		// A8.6.195 STR (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// str<c> <Rt>,[<Rn>,<Rm>]
		new OpcodeARM(Index.thumb_str__reg, "str", "0101000xxxxxxxxx"),
		// A8.6.196 STRB (immediate, Thumb)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// strb<c> <Rt>,[<Rn>,#<imm5>]
		new OpcodeARM(Index.thumb_strb__imm, "strb", "01110xxxxxxxxxxx"),
		// A8.6.198 STRB (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// strb<c> <Rt>,[<Rn>,<Rm>]
		new OpcodeARM(Index.thumb_strb__reg, "strb", "0101010xxxxxxxxx"),
		// A8.6.206 STRH (immediate, Thumb)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// strh<c> <Rt>,[<Rn>{,#<imm>}]
		new OpcodeARM(Index.thumb_strh__imm, "strh", "10000xxxxxxxxxxx"),
		// A8.6.208 STRH (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// strh<c> <Rt>,[<Rn>,<Rm>]
		new OpcodeARM(Index.thumb_strh__reg, "strh", "0101001xxxxxxxxx"),
		// A8.6.211 SUB (immediate, Thumb)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// subs <Rd>,<Rn>,#<imm3> Outside IT block.	sub<c> <Rd>,<Rn>,#<imm3> Inside IT block.
		new OpcodeARM(Index.thumb_sub__reg_imm, "sub", "0001111xxxxxxxxx"),
		// A8.6.211 SUB (immediate, Thumb)
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// subs <Rdn>,#<imm8> Outside IT block.	sub<c> <Rdn>,#<imm8> Inside IT block.
		new OpcodeARM(Index.thumb_sub__imm, "sub", "00111xxxxxxxxxxx"),
		// A8.6.213 SUB (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// subs <Rd>,<Rn>,<Rm> Outside IT block.	sub<c> <Rd>,<Rn>,<Rm> Inside IT block.
		new OpcodeARM(Index.thumb_sub__reg_reg, "sub", "0001101xxxxxxxxx"),
		// A8.6.215 SUB (SP minus immediate)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// sub<c> sp,sp,#<imm>
		new OpcodeARM(Index.thumb_sub__imm_from_sp, "sub", "101100001xxxxxxx"),
		// A8.6.223 SXTB
		// Encoding T1 ARMv6*, ARMv7
		// sxtb<c> <Rd>,<Rm>
		new OpcodeARM(Index.thumb_sxtb, "sxtb", "1011001001xxxxxx"),
		// A8.6.225 SXTH
		// Encoding T1 ARMv6*, ARMv7
		// sxth<c> <Rd>,<Rm>
		new OpcodeARM(Index.thumb_sxth, "sxth", "1011001000xxxxxx"),
		// A8.6.231 TST (register)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// tst<c> <Rn>,<Rm>
		new OpcodeARM(Index.thumb_tst, "tst", "0100001000xxxxxx"),
		// A8.6.263 UXTB
		// Encoding T1 ARMv6*, ARMv7
		// uxtb<c> <Rd>,<Rm>
		new OpcodeARM(Index.thumb_uxtb, "uxtb", "1011001011xxxxxx"),
		// A8.6.265 UXTH
		// Encoding T1 ARMv6*, ARMv7
		// uxth<c> <Rd>,<Rm>
		new OpcodeARM(Index.thumb_uxth, "uxth", "1011001010xxxxxx"),
		// B6.1.1 CPS
		// Encoding T1 ARMv6*, ARMv7
		// cps<effect> <iflags> Not permitted in IT block.
		// Unpredictable if (0) is 1: 10110110011x(0)xxx
		new OpcodeARM(Index.thumb_cps, "cps", "10110110011xxxxx"),
	};

	 // Reference manual citations (e.g., "A8.6.1") refer to sections in the ARM Architecture
	 // Reference Manual ARMv7-A and ARMv7-R Edition, Errata markup
	public static final OpcodeARM thumb2_opcode_table[] = {
		// A8.6.1 ADC (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// adc{s}<c> <Rd>,<Rn>,#<const>
		new OpcodeARM(Index.thumb2_adc__imm, "adc", "11110x01010xxxxx0xxxxxxxxxxxxxxx"),
		// A8.6.2 ADC (register)
		// Encoding T2 ARMv6T2, ARMv7
		// adc{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 11101011010xxxxx(0)xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_adc__reg, "adc", "11101011010xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.10 ADR
		// Encoding T3 ARMv6T2, ARMv7
		// adr<c>.w <Rd>,<label>
		// must precede thumb2_addw in table
		new OpcodeARM(Index.thumb2_adr__add, "add", "11110x10000011110xxxxxxxxxxxxxxx"),
		// A8.6.32 CMN (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// cmn<c> <Rn>,#<const>
		// must precede thumb2_add__imm in table
		// must precede thumb2_addw__imm in table
		new OpcodeARM(Index.thumb2_cmn__imm, "cmn", "11110x010001xxxx0xxx1111xxxxxxxx"),
		// A8.6.4 ADD (immediate, Thumb)
		// Encoding T4 ARMv6T2, ARMv7
		// addw<c> <Rd>,<Rn>,#<imm12>
		// A8.6.8 ADD (SP plus immediate)
		// Encoding T4 ARMv6T2, ARMv7
		// addw <Rd>,sp,#<imm12>
		//
		// must follow thumb2_adr__add in table
		// must follow thumb2_cmn__imm in table
		// must precede thumb2_add__imm in table
		new OpcodeARM(Index.thumb2_addw, "addw", "11110x100000xxxx0xxxxxxxxxxxxxxx"),
		// A8.6.4 ADD (immediate, Thumb)
		// Encoding T3 ARMv6T2, ARMv7
		// add{s}<c>.w <Rd>,<Rn>,#<const>
		// A8.6.8 ADD (SP plus immediate)
		// Encoding T3 ARMv6T2, ARMv7
		// add{s}.w <Rd>,sp,#<const>
		//
		// must follow thumb2_addw__imm in table
		// must follow thumb2_cmn__imm in table
		new OpcodeARM(Index.thumb2_add__imm, "add", "11110x01000xxxxx0xxxxxxxxxxxxxxx"),
		// A8.6.33 CMN (register)
		// Encoding T2 ARMv6T2, ARMv7
		// cmn<c>.w <Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 111010110001xxxx(0)xxx1111xxxxxxxx
		// must precede thumb2_add__reg in table
		new OpcodeARM(Index.thumb2_cmn__reg, "cmn.w", "111010110001xxxxxxxx1111xxxxxxxx"),
		// A8.6.6 ADD (register)
		// Encoding T3 ARMv6T2, ARMv7
		// add{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 11101011000xxxxx(0)xxxxxxxxxxxxxxx
		// A8.6.9 ADD (SP plus register)
		// Encoding T3 ARMv6T2, ARMv7
		// add{s}<c>.w <Rd>,sp,<Rm>{,<shift>}
		//
		// must follow thumb2_cmn__reg in table
		new OpcodeARM(Index.thumb2_add__reg, "add", "11101011000xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.10 ADR
		// Encoding T2 ARMv6T2, ARMv7
		// adr<c>.w <Rd>,<label>	sub <Rd>,pc,#0 Special case for subtraction of zero
		// must precede thumb2_subw in table
		new OpcodeARM(Index.thumb2_adr__sub, "sub", "11110x10101011110xxxxxxxxxxxxxxx"),
		// A8.6.230 TST (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// tst<c> <Rn>,#<const>
		// must precede thumb2_and__imm in table
		new OpcodeARM(Index.thumb2_tst__imm, "tst", "11110x000001xxxx0xxx1111xxxxxxxx"),
		// A8.6.11 AND (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// and{s}<c> <Rd>,<Rn>,#<const>
		// must follow thumb2_and__imm in table
		new OpcodeARM(Index.thumb2_and__imm, "and", "11110x00000xxxxx0xxxxxxxxxxxxxxx"),
		// A8.6.231 TST (register)
		// Encoding T2 ARMv6T2, ARMv7
		// tst<c>.w <Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 111010100001xxxx(0)xxx1111xxxxxxxx
		// must precede thumb2_and__reg in table
		new OpcodeARM(Index.thumb2_tst__reg, "tst.w", "111010100001xxxxxxxx1111xxxxxxxx"),
		// A8.6.12 AND (register)
		// Encoding T2 ARMv6T2, ARMv7
		// and{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 11101010000xxxxx(0)xxxxxxxxxxxxxxx
		// must follow thumb2_and__reg in table
		new OpcodeARM(Index.thumb2_and__reg, "and", "11101010000xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.14 ASR (immediate)
		// Encoding T2 ARMv6T2, ARMv7
		// asr{s}<c>.w <Rd>,<Rm>,#<imm>
		// Unpredictable if (0) is 1: 11101010010x1111(0)xxxxxxxxx10xxxx
		new OpcodeARM(Index.thumb2_asr__imm, "asr", "11101010010x1111xxxxxxxxxx10xxxx"),
		// A8.6.15 ASR (register)
		// Encoding T2 ARMv6T2, ARMv7
		// asr{s}<c>.w <Rd>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_asr__reg, "asr", "11111010010xxxxx1111xxxx0000xxxx"),
		// A8.6.26 BXJ
		// Encoding T1 ARMv6T2, ARMv7
		// bxj<c> <Rm> Outside or last in IT block
		// Unpredictable if (1) is 0 or (0) is 1: 111100111100xxxx10(0)0(1)(1)(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)
		// must precede thumb2_b__cond in table
		new OpcodeARM(Index.thumb2_bxj, "bxj", "111100111100xxxx10x0xxxxxxxxxxxx"),
		// A8.6.30 CLREX
		// Encoding T1 ARMv7
		// clrex<c>
		// Unpredictable if (1) is 0 or (0) is 1: 111100111011(1)(1)(1)(1)10(0)0(1)(1)(1)(1)0010(1)(1)(1)(1)
		// must precede thumb2_b__cond in table
		new OpcodeARM(Index.thumb2_clrex, "clrex", "111100111011xxxx10x0xxxx0010xxxx"),
		// A8.6.40 DBG
		// Encoding T1 ARMv7 (executes as NOP in ARMv6T2)
		// dbg<c> #<option>
		// Unpredictable if (1) is 0 or (0) is 1: 111100111010(1)(1)(1)(1)10(0)0(0)0001111xxxx
		// must precede thumb2_b__cond in table
		// must precede thumb2_cps in table
		new OpcodeARM(Index.thumb2_dbg, "dbg", "111100111010xxxx10x0x0001111xxxx"),
		// A8.6.110 NOP
		// Encoding T2 ARMv6T2, ARMv7
		// nop<c>.w
		// Unpredictable if (1) is 0 or (0) is 1: 111100111010(1)(1)(1)(1)10(0)0(0)00000000000
		// must precede thumb2_b__cond in table
		// must precede thumb2_cps in table
		new OpcodeARM(Index.thumb2_nop, "nop.w", "111100111010xxxx10x0x00000000000"),
		// A8.6.158 SEV
		// Encoding T2 ARMv7 (executes as NOP in ARMv6T2)
		// sev<c>.w
		// Unpredictable if (1) is 0 or (0) is 1: 111100111010(1)(1)(1)(1)10(0)0(0)00000000100
		// must precede thumb2_b__cond in table
		// must precede thumb2_cps in table
		new OpcodeARM(Index.thumb2_sev, "sev.w", "111100111010xxxx10x0x00000000100"),
		// B6.1.9 SMC (previously SMI)
		// Encoding T1 Security Extensions (not in ARMv6K)
		// smc<c> #<imm4>
		// Unpredictable if (0) is 1: 111101111111xxxx1000(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_smc, "smc", "111101111111xxxx1000xxxxxxxxxxxx"),
		// A8.6.411 WFE
		// Encoding T2 ARMv7 (executes as NOP in ARMv6T2)
		// wfe<c>.w
		// Unpredictable if (1) is 0 or (0) is 1: 111100111010(1)(1)(1)(1)10(0)0(0)00000000010
		// must precede thumbw_b__cond in table
		// must precede thumb2_cps in table
		new OpcodeARM(Index.thumb2_wfe, "wfe.w", "111100111010xxxx10x0x00000000010"),
		// A8.6.412 WFI
		// Encoding T2 ARMv7 (executes as NOP in ARMv6T2)
		// wfi<c>.w
		// Unpredictable if (1) is 0 or (0) is 1: 111100111010(1)(1)(1)(1)10(0)0(0)00000000011
		// must precede thumbw_b__cond in table
		// must precede thumb2_cps in table
		new OpcodeARM(Index.thumb2_wfi, "wfi.w", "111100111010xxxx10x0x00000000011"),
		// A8.6.413 YIELD
		// Encoding T2 ARMv7 (executes as NOP in ARMv6T2)
		// yield<c>.w
		// Unpredictable if (1) is 0 or (0) is 1: 111100111010(1)(1)(1)(1)10(0)0(0)00000000001
		// must precede thumbw_b__cond in table
		// must precede thumb2_cps in table
		new OpcodeARM(Index.thumb2_yield, "yield.w", "111100111010xxxx10x0x00000000001"),
		// B6.1.1 CPS
		// Encoding T2 ARMv6T2, ARMv7
		// cps<effect>.w <iflags>{,#<mode>} Not permitted in IT block.	cps #<mode> Not permitted in IT block.
		// Unpredictable if (1) is 0 or (0) is 1: 111100111010(1)(1)(1)(1)10(0)0(0)xxxxxxxxxxx
		// must follow thumb2_dbg in table
		// must follow thumb2_nop in table
		// must follow thumb2_sev in table
		// must follow thumb2_wfe in table
		// must follow thumb2_wfi in table
		// must follow thumb2_yield in table
		// must precede thumb2_b__cond in table
		new OpcodeARM(Index.thumb2_cps, "cps", "111100111010xxxx10x0xxxxxxxxxxxx"),
		// A8.6.41 DMB
		// Encoding T1 ARMv7
		// dmb<c> #<option>
		// Unpredictable if (1) is 0 or (0) is 1: 111100111011(1)(1)(1)(1)10(0)0(1)(1)(1)(1)0101xxxx
		// must precede thumb2_b__cond in table
		new OpcodeARM(Index.thumb2_dmb, "dmb", "111100111011xxxx10x0xxxx0101xxxx"),
		// A8.6.42 DSB
		// Encoding T1 ARMv7
		// dsb<c> #<option>
		// Unpredictable if (1) is 0 or (0) is 1: 111100111011(1)(1)(1)(1)10(0)0(1)(1)(1)(1)0100xxxx
		// must precede thumb2_b__cond in table
		new OpcodeARM(Index.thumb2_dsb, "dsb", "111100111011xxxx10x0xxxx0100xxxx"),
		// A8.6.49 ISB
		// Encoding T1 ARMv7
		// isb<c> #<option>
		// Unpredictable if (1) is 0 or (0) is 1: 111100111011(1)(1)(1)(1)10(0)0(1)(1)(1)(1)0110xxxx
		// must precede thumb2_b__cond in table
		new OpcodeARM(Index.thumb2_isb, "isb", "111100111011xxxx10x0xxxx0110xxxx"),
		// A8.6.102 MRS
		// Encoding T1 ARMv6T2, ARMv7
		// mrs<c> <Rd>,<spec_reg>
		// Unpredictable if (1) is 0 or (0) is 1: 111100111110(1)(1)(1)(1)10(0)0xxxx(0)(0)(0)(0)(0)(0)(0)(0)
		// B6.1.5 MRS
		// Encoding T1 ARMv6T2, ARMv7
		// mrs<c> <Rd>,<spec_reg>
		// Unpredictable if (1) is 0 or (0) is 1: 11110011111x(1)(1)(1)(1)10(0)0xxxx(0)(0)(0)(0)(0)(0)(0)(0)
		//
		// must precede thumb2_b__cond in table
		new OpcodeARM(Index.thumb2_mrs, "mrs", "11110011111xxxxx10x0xxxxxxxxxxxx"),
		// A8.6.104 MSR (register)
		// Encoding T1 ARMv6T2, ARMv7
		// msr<c> <spec_reg>,<Rn>
		// Unpredictable if (0) is 1: 111100111000xxxx10(0)0xx00(0)(0)(0)(0)(0)(0)(0)(0)
		// must precede thumb2_b__cond in table
		// B6.1.7 MSR (register)
		// Encoding T1 ARMv6T2, ARMv7
		// msr<c> <spec_reg>,<Rn>
		// Unpredictable if (0) is 1: 11110011100xxxxx10(0)0xxxx(0)(0)(0)(0)(0)(0)(0)(0)
		//
		// must precede thumb2_b__cond in table
		new OpcodeARM(Index.thumb2_msr, "msr", "11110011100xxxxx10x0xxxxxxxxxxxx"),
		// A8.6.16 B
		// Encoding T3 ARMv6T2, ARMv7
		// b<c>.w <label> Not permitted in IT block.
		// must follow thumb2_bxj in table
		// must follow thumb2_clrex in table
		// must follow thumb2_cps in table
		// must follow thumb2_dbg in table
		// must follow thumb2_dmb in table
		// must follow thumb2_dsb in table
		// must follow thumb2_isb in table
		// must follow thumb2_mrs in table
		// must follow thumb2_msr in table
		// must follow thumb2_nop in table
		// must follow thumb2_sev in table
		// must follow thumb2_wfe in table
		// must follow thumb2_wfi in table
		// must follow thumb2_yield in table
		new OpcodeARM(Index.thumb2_b__cond, "b", "11110xxxxxxxxxxx10x0xxxxxxxxxxxx"),
		// A8.6.16 B
		// Encoding T4 ARMv6T2, ARMv7
		// b<c>.w <label> Outside or last in IT block
		new OpcodeARM(Index.thumb2_b__uncond, "b.w", "11110xxxxxxxxxxx10x1xxxxxxxxxxxx"),
		// A8.6.17 BFC
		// Encoding T1 ARMv6T2, ARMv7
		// bfc<c> <Rd>,#<lsb>,#<width>
		// Unpredictable if (0) is 1: 11110(0)11011011110xxxxxxxxx(0)xxxxx
		// must precede thumb2_bfi in table
		new OpcodeARM(Index.thumb2_bfc, "bfc", "11110x11011011110xxxxxxxxxxxxxxx"),
		// A8.6.18 BFI
		// Encoding T1 ARMv6T2, ARMv7
		// bfi<c> <Rd>,<Rn>,#<lsb>,#<width>
		// Unpredictable if (0) is 1: 11110(0)110110xxxx0xxxxxxxxx(0)xxxxx
		// must follow thumb2_bfc in table
		new OpcodeARM(Index.thumb2_bfi, "bfi", "11110x110110xxxx0xxxxxxxxxxxxxxx"),
		// A8.6.154 SBFX
		// Encoding T1 ARMv6T2, ARMv7
		// sbfx<c> <Rd>,<Rn>,#<lsb>,#<width>
		// Unpredictable if (0) is 1: 11110(0)110100xxxx0xxxxxxxxx(0)xxxxx
		// A8.6.236 UBFX
		// Encoding T1 ARMv6T2, ARMv7
		// ubfx<c> <Rd>,<Rn>,#<lsb>,#<width>
		// Unpredictable if (0) is 1: 11110(0)111100xxxx0xxxxxxxxx(0)xxxxx
		//
		new OpcodeARM(Index.thumb2_bfx, "bfx", "11110x11x100xxxx0xxxxxxxxxxxxxxx"),
		// A8.6.19 BIC (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// bic{s}<c> <Rd>,<Rn>,#<const>
		new OpcodeARM(Index.thumb2_bic__imm, "bic", "11110x00001xxxxx0xxxxxxxxxxxxxxx"),
		// A8.6.20 BIC (register)
		// Encoding T2 ARMv6T2, ARMv7
		// bic{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 11101010001xxxxx(0)xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_bic__reg, "bic", "11101010001xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.23 BL, BLX (immediate)
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7 if J1 == J2 == 1; ARMv6T2, ARMv7 otherwise
		// bl<c> <label> Outside or last in IT block
		new OpcodeARM(Index.thumb2_bl, "bl", "11110xxxxxxxxxxx11x1xxxxxxxxxxxx"),
		// A8.6.23 BL, BLX (immediate)
		// Encoding T2 ARMv5T*, ARMv6*, ARMv7 if J1 == J2 == 1; ARMv6T2, ARMv7 otherwise
		// blx<c> <label> Outside or last in IT block
		new OpcodeARM(Index.thumb2_blx, "blx", "11110xxxxxxxxxxx11x0xxxxxxxxxxx0"),
		// A8.6.31 CLZ
		// Encoding T1 ARMv6T2, ARMv7
		// clz<c> <Rd>,<Rm>
		new OpcodeARM(Index.thumb2_clz, "clz", "111110101011xxxx1111xxxx1000xxxx"),
		// A8.6.35 CMP (immediate)
		// Encoding T2 ARMv6T2, ARMv7
		// cmp<c>.w <Rn>,#<const>
		// must precede thumb2_sub__imm in table
		// must precede thumb2_subw in table
		new OpcodeARM(Index.thumb2_cmp__imm, "cmp.w", "11110x011011xxxx0xxx1111xxxxxxxx"),
		// A8.6.36 CMP (register)
		// Encoding T3 ARMv6T2, ARMv7
		// cmp<c>.w <Rn>, <Rm> {,<shift>}
		// Unpredictable if (0) is 1: 111010111011xxxx(0)xxx1111xxxxxxxx
		// must precede thumb2_sub__reg in table
		// must precede thumb2_subw in table
		new OpcodeARM(Index.thumb2_cmp__reg, "cmp.w", "111010111011xxxxxxxx1111xxxxxxxx"),
		// A8.6.227 TEQ (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// teq<c> <Rn>,#<const>
		// must precede thumb2_eor__imm in table
		new OpcodeARM(Index.thumb2_teq__imm, "teq", "11110x001001xxxx0xxx1111xxxxxxxx"),
		// A8.6.44 EOR (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// eor{s}<c> <Rd>,<Rn>,#<const>
		// must follow thumb2_teq__imm in table
		new OpcodeARM(Index.thumb2_eor__imm, "eor", "11110x00100xxxxx0xxxxxxxxxxxxxxx"),
		// A8.6.228 TEQ (register)
		// Encoding T1 ARMv6T2, ARMv7
		// teq<c> <Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 111010101001xxxx(0)xxx1111xxxxxxxx
		// must precede thumb2_eor__reg in table
		new OpcodeARM(Index.thumb2_teq__reg, "teq", "111010101001xxxxxxxx1111xxxxxxxx"),
		// A8.6.45 EOR (register)
		// Encoding T2 ARMv6T2, ARMv7
		// eor{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 11101010100xxxxx(0)xxxxxxxxxxxxxxx
		// must follow thumb2_teq__reg in table
		new OpcodeARM(Index.thumb2_eor__reg, "eor", "11101010100xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.122 POP
		// Encoding T2 ARMv6T2, ARMv7
		// pop<c>.w <registers> <registers> contains more than one register
		// Unpredictable if (0) is 1: 1110100010111101xx(0)xxxxxxxxxxxxx
		// must precede thumb2_ldm in table
		new OpcodeARM(Index.thumb2_pop__regs, "pop.w", "1110100010111101xxxxxxxxxxxxxxxx"),
		// A8.6.53 LDM / LDMIA / LDMFD
		// Encoding T2 ARMv6T2, ARMv7
		// ldm<c>.w <Rn>{!},<registers>
		// Unpredictable if (0) is 1: 1110100010x1xxxxxx(0)xxxxxxxxxxxxx
		// must follow thumb2_pop__regs in table
		new OpcodeARM(Index.thumb2_ldm, "ldm.w", "1110100010x1xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.55 LDMDB / LDMEA
		// Encoding T1 ARMv6T2, ARMv7; if ony 1 register, instead assemble to LDR<c><q> <Rt>,[<Rn>,#-4]{!} instruction
		// ldmdb<c> <Rn>{!},<registers>
		// Unpredictable if (0) is 1: 1110100100x1xxxxxx(0)xxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_ldmdb, "ldmdb", "1110100100x1xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.117 PLD, PLDW (immediate)
		// Encoding T1 ARMv6T2, ARMv7 for PLD; ARMv7 with MP Extensions for PLDW
		// pld{w}<c> [<Rn>,#<imm12>]
		// A8.6.117 PLD, PLDW (immediate)
		// Encoding T2 ARMv6T2, ARMv7 for PLD; ARMv7 with MP Extensions for PLDW
		// pld{w}<c> [<Rn>,#-<imm8>]
		// A8.6.118 PLD (literal)
		// Encoding T1 ARMv6T2, ARMv7
		// pld<c> <label>	pld<c> [pc,#-0] Special case
		// Unpredictable if (0) is 1: 11111000x0(0)111111111xxxxxxxxxxxx
		// A8.6.119 PLD, PLDW (register)
		// Encoding T1 ARMv6T2, ARMv7 for PLD; ARMv7 with MP Extensions for PLDW
		// pld{w}<c> [<Rn>,<Rm>{,lsl #<imm2>}]
		//
		// must precede thumb2_ldr in table
		new OpcodeARM(Index.thumb2_pld, "pld", "11111000x0x1xxxx1111xxxxxxxxxxxx"),
		// A8.6.120 PLI (immediate, literal)
		// Encoding T1 ARMv7
		// pli<c> [<Rn>,#<imm12>]
		// A8.6.120 PLI (immediate, literal)
		// Encoding T2 ARMv7
		// pli<c> [<Rn>,#-<imm8>]
		// A8.6.120 PLI (immediate, literal)
		// Encoding T3 ARMv7
		// pli<c> <label>	pli<c> [pc,#-0] Special case
		// must precede thumb2_ldr__imm in table
		// A8.6.121 PLI (register)
		// Encoding T1 ARMv7
		// pli<c> [<Rn>,<Rm>{,lsl #<imm2>}]
		// must precede thumb2_ldr in table
		//
		new OpcodeARM(Index.thumb2_pli, "pli", "11111001x001xxxx1111xxxxxxxxxxxx"),
		// A8.6.122 POP
		// Encoding T3 ARMv6T2, ARMv7
		// pop<c>.w <registers> <registers> contains one register, <Rt>
		// must precede thumb2_ldr in table
		new OpcodeARM(Index.thumb2_pop__reg, "pop.w", "1111100001011101xxxx101100000100"),
		// A8.6.57 LDR (immediate, Thumb)
		// Encoding T3 ARMv6T2, ARMv7
		// ldr<c>.w <Rt>,[<Rn>{,#<imm12>}]
		// A8.6.57 LDR (immediate, Thumb)
		// Encoding T4 ARMv6T2, ARMv7
		// ldr<c> <Rt>,[<Rn>,#-<imm8>]	ldr<c> <Rt>,[<Rn>],#+/-<imm8>	ldr<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// A8.6.59 LDR (literal)
		// Encoding T2 ARMv6T2, ARMv7
		// ldr<c>.w <Rt>,<label>	ldr<c>.w <Rt>,[pc,#-0] Special case
		// A8.6.60 LDR (register)
		// Encoding T2 ARMv6T2, ARMv7
		// ldr<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// A8.6.61 LDRB (immediate, Thumb)
		// Encoding T2 ARMv6T2, ARMv7
		// ldrb<c>.w <Rt>,[<Rn>{,#<imm12>}]
		// A8.6.61 LDRB (immediate, Thumb)
		// Encoding T3 ARMv6T2, ARMv7
		// ldrb<c> <Rt>,[<Rn>,#-<imm8>]	ldrb<c> <Rt>,[<Rn>],#+/-<imm8>	ldrb<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// A8.6.63 LDRB (literal)
		// Encoding T1 ARMv6T2, ARMv7
		// ldrb<c> <Rt>,<label>	ldrb<c> <Rt>,[pc,#-0] Special case
		// A8.6.64 LDRB (register)
		// Encoding T2 ARMv6T2, ARMv7
		// ldrb<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// A8.6.65 LDRBT
		// Encoding T1 ARMv6T2, ARMv7
		// ldrbt<c> <Rt>,[<Rn>,#<imm8>]
		// A8.6.73 LDRH (immediate, Thumb)
		// Encoding T2 ARMv6T2, ARMv7
		// ldrh<c>.w <Rt>,[<Rn>{,#<imm12>}]
		// A8.6.73 LDRH (immediate, Thumb)
		// Encoding T3 ARMv6T2, ARMv7
		// ldrh<c> <Rt>,[<Rn>,#-<imm8>]	ldrh<c> <Rt>,[<Rn>],#+/-<imm8>	ldrh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// A8.6.75 LDRH (literal)
		// Encoding T1 ARMv6T2, ARMv7
		// ldrh<c> <Rt>,<label>	ldrh<c> <Rt>,[pc,#-0] Special case
		// A8.6.76 LDRH (register)
		// Encoding T2 ARMv6T2, ARMv7
		// ldrh<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// A8.6.77 LDRHT
		// Encoding T1 ARMv6T2, ARMv7
		// ldrht<c> <Rt>,[<Rn>,#<imm8>]
		// A8.6.78 LDRSB (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// ldrsb<c> <Rt>,[<Rn>,#<imm12>]
		// A8.6.78 LDRSB (immediate)
		// Encoding T2 ARMv6T2, ARMv7
		// ldrsb<c> <Rt>,[<Rn>,#-<imm8>]	ldrsb<c> <Rt>,[<Rn>],#+/-<imm8>	ldrsb<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// A8.6.79 LDRSB (literal)
		// Encoding T1 ARMv6T2, ARMv7
		// ldrsb<c> <Rt>,<label>	ldrsb<c> <Rt>,[pc,#-0] Special case
		// A8.6.80 LDRSB (register)
		// Encoding T2 ARMv6T2, ARMv7
		// ldrsb<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// A8.6.81 LDRSBT
		// Encoding T1 ARMv6T2, ARMv7
		// ldrsbt<c> <Rt>,[<Rn>,#<imm8>]
		// A8.6.82 LDRSH (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// ldrsh<c> <Rt>,[<Rn>,#<imm12>]
		// A8.6.82 LDRSH (immediate)
		// Encoding T2 ARMv6T2, ARMv7
		// ldrsh<c> <Rt>,[<Rn>,#-<imm8>]	ldrsh<c> <Rt>,[<Rn>],#+/-<imm8>	ldrsh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// A8.6.83 LDRSH (literal)
		// Encoding T1 ARMv6T2, ARMv7
		// ldrsh<c> <Rt>,<label>	ldrsh<c> <Rt>,[pc,#-0] Special case
		// A8.6.84 LDRSH (register)
		// Encoding T2 ARMv6T2, ARMv7
		// ldrsh<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// A8.6.85 LDRSHT
		// Encoding T1 ARMv6T2, ARMv7
		// ldrsht<c> <Rt>,[<Rn>,#<imm8>]
		// A8.6.86 LDRT
		// Encoding T1 ARMv6T2, ARMv7
		// ldrt<c> <Rt>,[<Rn>,#<imm8>]
		//
		// must follow thumb2_pld in table
		// must follow thumb2_pli in table
		// must follow thumb2_pop_reg in table
		// 1 1 1 1 1 0 0 s_1_8_8 u_1_7_7 op_1_6_6 bh_1_5_5 1 Rn_1_3_0 Rt_0_15_12 imm12_0_11_0
		new OpcodeARM(Index.thumb2_ldr, "ldr", "1111100xxxx1xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.67 LDRD (literal)
		// Encoding T1 ARMv6T2, ARMv7
		// ldrd<c> <Rt>,<Rt2>,<label>	ldrd<c> <Rt>,<Rt2>,[pc,#-0] Special case
		// Unpredictable if (0) is 1: 1110100xx1(0)11111xxxxxxxxxxxxxxxx
		// must precede thumb2_ldrex in table
		// must precede thumb2_ldrd__imm in table
		new OpcodeARM(Index.thumb2_ldrd__lit, "ldrd", "1110100xx1x11111xxxxxxxxxxxxxxxx"),
		// A8.6.69 LDREX
		// Encoding T1 ARMv6T2, ARMv7
		// ldrex<c> <Rt>,[<Rn>{,#<imm>}]
		// Unpredictable if (1) is 0: 111010000101xxxxxxxx(1)(1)(1)(1)xxxxxxxx
		// must follow thumb2_ldrd__lit in table
		// must precede thumb2_ldrd__imm in table and must encode (1)(1)(1)(1)
		new OpcodeARM(Index.thumb2_ldrex, "ldrex", "111010000101xxxxxxxx1111xxxxxxxx"),
		// A8.6.226 TBB, TBH
		// Encoding T1 ARMv6T2, ARMv7
		// tbb<c> [<Rn>,<Rm>] Outside or last in IT block	tbh<c> [<Rn>,<Rm>,LSL #1] Outside or last in IT block
		// Unpredictable if (1) is 0 or (0) is 1: 111010001101xxxx(1)(1)(1)(1)(0)(0)(0)(0)000xxxxx
		// must precede thumb2_ldrexx in table
		new OpcodeARM(Index.thumb2_tb, "tb", "111010001101xxxxxxxxxxxx000xxxxx"),
		// A8.6.70 LDREXB
		// Encoding T1 ARMv7
		// ldrexb<c> <Rt>, [<Rn>]
		// Unpredictable if (1) is 0: 111010001101xxxxxxxx(1)(1)(1)(1)0100(1)(1)(1)(1)
		// A8.6.71 LDREXD
		// Encoding T1 ARMv7
		// ldrexd<c> <Rt>,<Rt2>,[<Rn>]
		// Unpredictable if (1) is 0: 111010001101xxxxxxxxxxxx0111(1)(1)(1)(1)
		// A8.6.72 LDREXH
		// Encoding T1 ARMv7
		// ldrexh<c> <Rt>, [<Rn>]
		// Unpredictable if (1) is 0: 111010001101xxxxxxxx(1)(1)(1)(1)0101(1)(1)(1)(1)
		//
		// must follow thumb2_ldrd__lit in table
		// must follow thumb2_tb in table
		// must precede thumb2_ldrd__imm in table and encode Rt2/(1)(1)(1)(1)
		new OpcodeARM(Index.thumb2_ldrexx, "ldrex", "111010001101xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.66 LDRD (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// ldrd<c> <Rt>,<Rt2>,[<Rn>{,#+/-<imm>}]	ldrd<c> <Rt>,<Rt2>,[<Rn>],#+/-<imm>	ldrd<c> <Rt>,<Rt2>,[<Rn>,#+/-<imm>]!
		// must follow thumb2_ldrd__lit in table
		// must follow thumb2_ldrex in table
		new OpcodeARM(Index.thumb2_ldrd__imm, "ldrd", "1110100xx1x1xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.97 MOV (register)
		// Encoding T3 ARMv6T2, ARMv7
		// mov{s}<c>.w <Rd>,<Rm>
		// Unpredictable if (0) is 1: 11101010010x1111(0)000xxxx0000xxxx
		// must precede thumb2_lsl__imm in table
		// must precede thumb2_orr__reg in table
		new OpcodeARM(Index.thumb2_mov__reg, "mov", "11101010010x1111x000xxxx0000xxxx"),
		// A8.6.88 LSL (immediate)
		// Encoding T2 ARMv6T2, ARMv7
		// lsl{s}<c>.w <Rd>,<Rm>,#<imm5>
		// Unpredictable if (0) is 1: 11101010010x1111(0)xxxxxxxxx00xxxx
		// must follow thumb2_mov_reg in table
		new OpcodeARM(Index.thumb2_lsl__imm, "lsl", "11101010010x1111xxxxxxxxxx00xxxx"),
		// A8.6.89 LSL (register)
		// Encoding T2 ARMv6T2, ARMv7
		// lsl{s}<c>.w <Rd>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_lsl__reg, "lsl", "11111010000xxxxx1111xxxx0000xxxx"),
		// A8.6.90 LSR (immediate)
		// Encoding T2 ARMv6T2, ARMv7
		// lsr{s}<c>.w <Rd>,<Rm>,#<imm>
		// Unpredictable if (0) is 1: 11101010010x1111(0)xxxxxxxxx01xxxx
		new OpcodeARM(Index.thumb2_lsr__imm, "lsr", "11101010010x1111xxxxxxxxxx01xxxx"),
		// A8.6.91 LSR (register)
		// Encoding T2 ARMv6T2, ARMv7
		// lsr{s}<c>.w <Rd>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_lsr__reg, "lsr", "11111010001xxxxx1111xxxx0000xxxx"),
		// A8.6.105 MUL
		// Encoding T2 ARMv6T2, ARMv7
		// mul<c> <Rd>,<Rn>,<Rm>
		// must precede thumb2_ml in table
		new OpcodeARM(Index.thumb2_mul, "mul", "111110110000xxxx1111xxxx0000xxxx"),
		// A8.6.94 MLA
		// Encoding T1 ARMv6T2, ARMv7
		// mla<c> <Rd>,<Rn>,<Rm>,<Ra>
		// A8.6.95 MLS
		// Encoding T1 ARMv6T2, ARMv7
		// mls<c> <Rd>,<Rn>,<Rm>,<Ra>
		//
		// must follow thumb2_mul in table
		new OpcodeARM(Index.thumb2_ml, "ml", "111110110000xxxxxxxxxxxx000xxxxx"),
		// A8.6.96 MOV (immediate)
		// Encoding T2 ARMv6T2, ARMv7
		// mov{s}<c>.w <Rd>,#<const>
		// must precede thumb2_orr__imm in table
		new OpcodeARM(Index.thumb2_mov__imm, "mov", "11110x00010x11110xxxxxxxxxxxxxxx"),
		// A8.6.96 MOV (immediate)
		// Encoding T3 ARMv6T2, ARMv7
		// movw<c> <Rd>,#<imm16>
		// A8.6.99 MOVT
		// Encoding T1 ARMv6T2, ARMv7
		// movt<c> <Rd>,#<imm16>
		//
		new OpcodeARM(Index.thumb2_movx, "mov", "11110x10x100xxxx0xxxxxxxxxxxxxxx"),
		// A8.6.106 MVN (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// mvn{s}<c> <Rd>,#<const>
		// must precede thumb2_orn__imm in table
		new OpcodeARM(Index.thumb2_mvn__imm, "mvn", "11110x00011x11110xxxxxxxxxxxxxxx"),
		// A8.6.107 MVN (register)
		// Encoding T2 ARMv6T2, ARMv7
		// mvn{s}<c>.w <Rd>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 11101010011x1111(0)xxxxxxxxxxxxxxx
		// must precede thumb2_orn__reg in table
		new OpcodeARM(Index.thumb2_mvn__reg, "mvn", "11101010011x1111xxxxxxxxxxxxxxxx"),
		// A8.6.111 ORN (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// orn{s}<c> <Rd>,<Rn>,#<const>
		// must follow thumb2_mvn__imm in table
		new OpcodeARM(Index.thumb2_orn__imm, "orn", "11110x00011xxxxx0xxxxxxxxxxxxxxx"),
		// A8.6.112 ORN (register)
		// Encoding T1 ARMv6T2, ARMv7
		// orn{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 11101010011xxxxx(0)xxxxxxxxxxxxxxx
		// must follow thumb2_mvn__reg in table
		new OpcodeARM(Index.thumb2_orn__reg, "orn", "11101010011xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.113 ORR (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// orr{s}<c> <Rd>,<Rn>,#<const>
		// must follow thumb2_mov__imm in table
		new OpcodeARM(Index.thumb2_orr__imm, "orr", "11110x00010xxxxx0xxxxxxxxxxxxxxx"),
		// A8.6.141 RRX
		// Encoding T1 ARMv6T2, ARMv7
		// rrx{s}<c> <Rd>,<Rm>
		// Unpredictable if (0) is 1: 11101010010x1111(0)000xxxx0011xxxx
		// must precede thumb2_ror__imm in table
		// must precede thumb2_orr__reg in table
		new OpcodeARM(Index.thumb2_rrx, "rrx", "11101010010x1111x000xxxx0011xxxx"),
		// A8.6.139 ROR (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// ror{s}<c> <Rd>,<Rm>,#<imm>
		// Unpredictable if (0) is 1: 11101010010x1111(0)xxxxxxxxx11xxxx
		// must precede thumb2_orr__reg in table
		// must follow thumb2_rrx in table
		new OpcodeARM(Index.thumb2_ror__imm, "ror", "11101010010x1111xxxxxxxxxx11xxxx"),
		// A8.6.114 ORR (register)
		// Encoding T2 ARMv6T2, ARMv7
		// orr{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 11101010010xxxxx(0)xxxxxxxxxxxxxxx
		// must follow thumb2_mov__reg in table
		// must follow thumb2_ror__imm in table
		// must follow thumb2_rrx in table
		new OpcodeARM(Index.thumb2_orr__reg, "orr", "11101010010xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.116 PKH
		// Encoding T1 ARMv6T2, ARMv7
		// pkhbt<c> <Rd>,<Rn>,<Rm>{,lsl #<imm>}	pkhtb<c> <Rd>,<Rn>,<Rm>{,asr #<imm>}
		// Unpredictable if (0) is 1: 111010101100xxxx(0)xxxxxxxxxx0xxxx
		new OpcodeARM(Index.thumb2_pkh, "pkh", "111010101100xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.123 PUSH
		// Encoding T2 ARMv6T2, ARMv7
		// push<c>.w <registers> <registers> contains more than one register
		// Unpredictable if (0) is 1: 1110100010101101(0)x(0)xxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_push__regs, "push.w", "1110100010101101xxxxxxxxxxxxxxxx"),
		// A8.6.123 PUSH
		// Encoding T3 ARMv6T2, ARMv7
		// push<c>.w <registers> <registers> contains one register, <Rt>
		// must precede thumb2_str in table
		new OpcodeARM(Index.thumb2_push__reg, "push.w", "1111100001001101xxxx110100000100"),
		// A8.6.124 QADD
		// Encoding T1 ARMv6T2, ARMv7
		// qadd<c> <Rd>,<Rm>,<Rn>
		// A8.6.128 QDADD
		// Encoding T1 ARMv6T2, ARMv7
		// qdadd<c> <Rd>,<Rm>,<Rn>
		//
		new OpcodeARM(Index.thumb2_qadd, "add", "111110101000xxxx1111xxxx100xxxxx"),
		// A8.6.129 QDSUB
		// Encoding T1 ARMv6T2, ARMv7
		// qdsub<c> <Rd>,<Rm>,<Rn>
		// A8.6.131 QSUB
		// Encoding T1 ARMv6T2, ARMv7
		// qsub<c> <Rd>,<Rm>,<Rn>
		//
		new OpcodeARM(Index.thumb2_qsub, "sub", "111110101000xxxx1111xxxx101xxxxx"),
		// A8.6.125 QADD16
		// Encoding T1 ARMv6T2, ARMv7
		// qadd16<c> <Rd>,<Rn>,<Rm>
		// A8.6.126 QADD8
		// Encoding T1 ARMv6T2, ARMv7
		// qadd8<c> <Rd>,<Rn>,<Rm>
		// A8.6.127 QASX
		// Encoding T1 ARMv6T2, ARMv7
		// qasx<c> <Rd>,<Rn>,<Rm>
		// A8.6.130 QSAX
		// Encoding T1 ARMv6T2, ARMv7
		// qsax<c> <Rd>,<Rn>,<Rm>
		// A8.6.132 QSUB16
		// Encoding T1 ARMv6T2, ARMv7
		// qsub16<c> <Rd>,<Rn>,<Rm>
		// A8.6.133 QSUB8
		// Encoding T1 ARMv6T2, ARMv7
		// qsub8<c> <Rd>,<Rn>,<Rm>
		// A8.6.148 SADD16
		// Encoding T1 ARMv6T2, ARMv7
		// sadd16<c> <Rd>,<Rn>,<Rm>
		// A8.6.149 SADD8
		// Encoding T1 ARMv6T2, ARMv7
		// sadd8<c> <Rd>,<Rn>,<Rm>
		// A8.6.150 SASX
		// Encoding T1 ARMv6T2, ARMv7
		// sasx<c> <Rd>,<Rn>,<Rm>
		// A8.6.159 SHADD16
		// Encoding T1 ARMv6T2, ARMv7
		// shadd16<c> <Rd>,<Rn>,<Rm>
		// A8.6.160 SHADD8
		// Encoding T1 ARMv6T2, ARMv7
		// shadd8<c> <Rd>,<Rn>,<Rm>
		// A8.6.161 SHASX
		// Encoding T1 ARMv6T2, ARMv7
		// shasx<c> <Rd>,<Rn>,<Rm>
		// A8.6.162 SHSAX
		// Encoding T1 ARMv6T2, ARMv7
		// shsax<c> <Rd>,<Rn>,<Rm>
		// A8.6.163 SHSUB16
		// Encoding T1 ARMv6T2, ARMv7
		// shsub16<c> <Rd>,<Rn>,<Rm>
		// A8.6.164 SHSUB8
		// Encoding T1 ARMv6T2, ARMv7
		// shsub8<c> <Rd>,<Rn>,<Rm>
		// A8.6.185 SSAX
		// Encoding T1 ARMv6T2, ARMv7
		// ssax<c> <Rd>,<Rn>,<Rm>
		// A8.6.186 SSUB16
		// Encoding T1 ARMv6T2, ARMv7
		// ssub16<c> <Rd>,<Rn>,<Rm>
		// A8.6.187 SSUB8
		// Encoding T1 ARMv6T2, ARMv7
		// ssub8<c> <Rd>,<Rn>,<Rm>
		// A8.6.233 UADD16
		// Encoding T1 ARMv6T2, ARMv7
		// uadd16<c> <Rd>,<Rn>,<Rm>
		// A8.6.234 UADD8
		// Encoding T1 ARMv6T2, ARMv7
		// uadd8<c> <Rd>,<Rn>,<Rm>
		// A8.6.235 UASX
		// Encoding T1 ARMv6T2, ARMv7
		// uasx<c> <Rd>,<Rn>,<Rm>
		// A8.6.238 UHADD16
		// Encoding T1 ARMv6T2, ARMv7
		// uhadd16<c> <Rd>,<Rn>,<Rm>
		// A8.6.239 UHADD8
		// Encoding T1 ARMv6T2, ARMv7
		// uhadd8<c> <Rd>,<Rn>,<Rm>
		// A8.6.240 UHASX
		// Encoding T1 ARMv6T2, ARMv7
		// uhasx<c> <Rd>,<Rn>,<Rm>
		// A8.6.241 UHSAX
		// Encoding T1 ARMv6T2, ARMv7
		// uhsax<c> <Rd>,<Rn>,<Rm>
		// A8.6.242 UHSUB16
		// Encoding T1 ARMv6T2, ARMv7
		// uhsub16<c> <Rd>,<Rn>,<Rm>
		// A8.6.243 UHSUB8
		// Encoding T1 ARMv6T2, ARMv7
		// uhsub8<c> <Rd>,<Rn>,<Rm>
		// A8.6.247 UQADD16
		// Encoding T1 ARMv6T2, ARMv7
		// uqadd16<c> <Rd>,<Rn>,<Rm>
		// A8.6.248 UQADD8
		// Encoding T1 ARMv6T2, ARMv7
		// uqadd8<c> <Rd>,<Rn>,<Rm>
		// A8.6.249 UQASX
		// Encoding T1 ARMv6T2, ARMv7
		// uqasx<c> <Rd>,<Rn>,<Rm>
		// A8.6.250 UQSAX
		// Encoding T1 ARMv6T2, ARMv7
		// uqsax<c> <Rd>,<Rn>,<Rm>
		// A8.6.251 UQSUB16
		// Encoding T1 ARMv6T2, ARMv7
		// uqsub16<c> <Rd>,<Rn>,<Rm>
		// A8.6.252 UQSUB8
		// Encoding T1 ARMv6T2, ARMv7
		// uqsub8<c> <Rd>,<Rn>,<Rm>
		// A8.6.257 USAX
		// Encoding T1 ARMv6T2, ARMv7
		// usax<c> <Rd>,<Rn>,<Rm>
		// A8.6.258 USUB16
		// Encoding T1 ARMv6T2, ARMv7
		// usub16<c> <Rd>,<Rn>,<Rm>
		// A8.6.259 USUB8
		// Encoding T1 ARMv6T2, ARMv7
		// usub8<c> <Rd>,<Rn>,<Rm>
		//
		// {s|u|}{h|q|}{asx|sax|{add|sub}{8|16}<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 op_1_6_4 Rn_1_3_0 1 1 1 1 Rd_0_11_8 u_0_6_0 h_0_5_0 q_0_4_0 Rm_0_3_0
		new OpcodeARM(Index.thumb2__r_dnm_math, null, "111110101xxxxxxx1111xxxx0xxxxxxx"),
		// A8.6.134 RBIT
		// Encoding T1 ARMv6T2, ARMv7
		// rbit<c> <Rd>,<Rm>
		// A8.6.135 REV
		// Encoding T2 ARMv6T2, ARMv7
		// rev<c>.w <Rd>,<Rm>
		// A8.6.136 REV16
		// Encoding T2 ARMv6T2, ARMv7
		// rev16<c>.w <Rd>,<Rm>
		// A8.6.137 REVSH
		// Encoding T2 ARMv6T2, ARMv7
		// revsh<c>.w <Rd>,<Rm>
		//
		new OpcodeARM(Index.thumb2_reverse, "r", "111110101001xxxx1111xxxx10xxxxxx"),
		// A8.6.140 ROR (register)
		// Encoding T2 ARMv6T2, ARMv7
		// ror{s}<c>.w <Rd>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_ror__reg, "ror", "11111010011xxxxx1111xxxx0000xxxx"),
		// A8.6.142 RSB (immediate)
		// Encoding T2 ARMv6T2, ARMv7
		// rsb{s}<c>.w <Rd>,<Rn>,#<const>
		new OpcodeARM(Index.thumb2_rsb__imm, "rsb", "11110x01110xxxxx0xxxxxxxxxxxxxxx"),
		// A8.6.143 RSB (register)
		// Encoding T1 ARMv6T2, ARMv7
		// rsb{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 11101011110xxxxx(0)xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_rsb__reg, "rsb", "11101011110xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.151 SBC (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// sbc{s}<c> <Rd>,<Rn>,#<const>
		new OpcodeARM(Index.thumb2_sbc__imm, "sbc", "11110x01011xxxxx0xxxxxxxxxxxxxxx"),
		// A8.6.152 SBC (register)
		// Encoding T2 ARMv6T2, ARMv7
		// sbc{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 11101011011xxxxx(0)xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_sbc__reg, "sbc", "11101011011xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.155 SDIV
		// Encoding T1 ARMv7-R
		// sdiv<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: 111110111001xxxx(1)(1)(1)(1)xxxx1111xxxx
		new OpcodeARM(Index.thumb2_sdiv, "sdiv", "111110111001xxxxxxxxxxxx1111xxxx"),
		// A8.6.156 SEL
		// Encoding T1 ARMv6T2, ARMv7
		// sel<c> <Rd>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_sel, "sel", "111110101010xxxx1111xxxx1000xxxx"),
		// A8.6.178 SMULBB, SMULBT, SMULTB, SMULTT
		// Encoding T1 ARMv6T2, ARMv7
		// smul<x><y><c> <Rd>,<Rn>,<Rm>
		// must precede thumb2_smla in table
		new OpcodeARM(Index.thumb2_smul, "smul", "111110110001xxxx1111xxxx00xxxxxx"),
		// A8.6.166 SMLABB, SMLABT, SMLATB, SMLATT
		// Encoding T1 ARMv6T2, ARMv7
		// smla<x><y><c> <Rd>,<Rn>,<Rm>,<Ra>
		// must follow thumb2_smul in table
		new OpcodeARM(Index.thumb2_smla, "smla", "111110110001xxxxxxxxxxxx00xxxxxx"),
		// A8.6.177 SMUAD
		// Encoding T1 ARMv6T2, ARMv7
		// smuad{x}<c> <Rd>,<Rn>,<Rm>
		// must precede thumb2_smlad in table
		new OpcodeARM(Index.thumb2_smuad, "smuad", "111110110010xxxx1111xxxx000xxxxx"),
		// A8.6.167 SMLAD
		// Encoding T1 ARMv6T2, ARMv7
		// smlad{x}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// must follow thumb2_smuad in table
		new OpcodeARM(Index.thumb2_smlad, "smlad", "111110110010xxxxxxxxxxxx000xxxxx"),
		// A8.6.168 SMLAL
		// Encoding T1 ARMv6T2, ARMv7
		// smlal<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// A8.6.169 SMLALBB, SMLALBT, SMLALTB, SMLALTT
		// Encoding T1 ARMv6T2, ARMv7
		// smlal<x><y><c> <RdLo>,<RdHi>,<Rn>,<Rm>
		//
		new OpcodeARM(Index.thumb2_smlal, "smlal", "111110111100xxxxxxxxxxxxx0xxxxxx"),
		// A8.6.170 SMLALD
		// Encoding T1 ARMv6T2, ARMv7
		// smlald{x}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_smlald, "smlald", "111110111100xxxxxxxxxxxx110xxxxx"),
		// A8.6.180 SMULWB, SMULWT
		// Encoding T1 ARMv6T2, ARMv7
		// smulw<y><c> <Rd>,<Rn>,<Rm>
		// must precede thumb2_smlaw in table
		new OpcodeARM(Index.thumb2_smulw, "smulw", "111110110011xxxx1111xxxx000xxxxx"),
		// A8.6.171 SMLAWB, SMLAWT
		// Encoding T1 ARMv6T2, ARMv7
		// smlaw<y><c> <Rd>,<Rn>,<Rm>,<Ra>
		// must follow thumb2_smulw in table
		new OpcodeARM(Index.thumb2_smlaw, "smlaw", "111110110011xxxxxxxxxxxx000xxxxx"),
		// A8.6.181 SMUSD
		// Encoding T1 ARMv6T2, ARMv7
		// smusd{x}<c> <Rd>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_smusd, "smusd", "111110110100xxxx1111xxxx000xxxxx"),
		// A8.6.172 SMLSD
		// Encoding T1 ARMv6T2, ARMv7
		// smlsd{x}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// must follow thumb2_smusd in table
		new OpcodeARM(Index.thumb2_smlsd, "smlsd", "111110110100xxxxxxxxxxxx000xxxxx"),
		// A8.6.173 SMLSLD
		// Encoding T1 ARMv6T2, ARMv7
		// smlsld{x}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_smlsld, "smlsld", "111110111101xxxxxxxxxxxx110xxxxx"),
		// A8.6.176 SMMUL
		// Encoding T1 ARMv6T2, ARMv7
		// smmul{r}<c> <Rd>,<Rn>,<Rm>
		// must precede thumb2_smmla in table
		new OpcodeARM(Index.thumb2_smmul, "smmul", "111110110101xxxx1111xxxx000xxxxx"),
		// A8.6.174 SMMLA
		// Encoding T1 ARMv6T2, ARMv7
		// smmla{r}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// must follow thumb2_smmul in table
		new OpcodeARM(Index.thumb2_smmla, "smmla", "111110110101xxxxxxxxxxxx000xxxxx"),
		// A8.6.175 SMMLS
		// Encoding T1 ARMv6T2, ARMv7
		// smmls{r}<c> <Rd>,<Rn>,<Rm>,<Ra>
		new OpcodeARM(Index.thumb2_smmls, "smmls", "111110110110xxxxxxxxxxxx000xxxxx"),
		// A8.6.179 SMULL
		// Encoding T1 ARMv6T2, ARMv7
		// smull<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_smull, "smull", "111110111000xxxxxxxxxxxx0000xxxx"),
		// A8.6.184 SSAT16
		// Encoding T1 ARMv6T2, ARMv7
		// ssat16<c> <Rd>,#<imm>,<Rn>
		// Unpredictable if (0) is 1: 11110(0)110010xxxx0000xxxx00(0)(0)xxxx
		// must precede thumb2_ssat in table
		new OpcodeARM(Index.thumb2_ssat16, "ssat16", "11110x110010xxxx0000xxxx00xxxxxx"),
		// A8.6.183 SSAT
		// Encoding T1 ARMv6T2, ARMv7
		// ssat<c> <Rd>,#<imm>,<Rn>{,<shift>}
		// Unpredictable if (0) is 1: 11110(0)1100x0xxxx0xxxxxxxxx(0)xxxxx
		// must follow thumb2_ssat16 in table
		new OpcodeARM(Index.thumb2_ssat, "ssat", "11110x1100x0xxxx0xxxxxxxxxxxxxxx"),
		// A8.6.189 STM / STMIA / STMEA
		// Encoding T2 ARMv6T2, ARMv7
		// stm<c>.w <Rn>{!},<registers>
		// Unpredictable if (0) is 1: 1110100010x0xxxx(0)x(0)xxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_stm, "stm.w", "1110100010x0xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.191 STMDB / STMFD
		// Encoding T1 ARMv6T2, ARMv7
		// stmdb<c> <Rn>{!},<registers>
		// Unpredictable if (0) is 1: 1110100100x0xxxx(0)x(0)xxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_stmdb, "stmdb", "1110100100x0xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.307 VLD1 (multiple single elements)
		// Encoding T1 / A1 Advanced SIMD
		// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.310 VLD2 (multiple 2-element structures)
		// Encoding T1 / A1 Advanced SIMD
		// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.313 VLD3 (multiple 3-element structures)
		// Encoding T1 / A1 Advanced SIMD
		// vld3<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld3<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.316 VLD4 (multiple 4-element structures)
		// Encoding T1 / A1 Advanced SIMD
		// vld4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		//
		// must precede thumb2_str in table
		new OpcodeARM(Index.thumb2_vld__multi, "vld", "111110010x10xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.309 VLD1 (single element to all lanes)
		// Encoding T1 / A1 Advanced SIMD
		// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.308 VLD1 (single element to one lane)
		// Encoding T1 / A1 Advanced SIMD
		// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.312 VLD2 (single 2-element structure to all lanes)
		// Encoding T1 / A1 Advanced SIMD
		// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.311 VLD2 (single 2-element structure to one lane)
		// Encoding T1 / A1 Advanced SIMD
		// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.315 VLD3 (single 3-element structure to all lanes)
		// Encoding T1 / A1 Advanced SIMD
		// vld3<c>.<size> <list>, [<Rn>]{!}	vld3<c>.<size> <list>, [<Rn>], <Rm>
		// A8.6.314 VLD3 (single 3-element structure to one lane)
		// Encoding T1 / A1 Advanced SIMD
		// vld3<c>.<size> <list>, [<Rn>]{!}	vld3<c>.<size> <list>, [<Rn>], <Rm>
		// A8.6.318 VLD4 (single 4-element structure to all lanes)
		// Encoding T1 / A1 Advanced SIMD
		// vld4<c>.<size> <list>, [<Rn>{ @<align>}]{!}	vld4<c>.<size> <list>, [<Rn>{ @<align>}], <Rm>
		// A8.6.317 VLD4 (single 4-element structure to one lane)
		// Encoding T1 / A1 Advanced SIMD
		// vld4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		//
		// must precede thumb2_str in table
		new OpcodeARM(Index.thumb2_vld__xlane, "vld", "111110011x10xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.391 VST1 (multiple single elements)
		// Encoding T1 / A1 Advanced SIMD
		// vst1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.393 VST2 (multiple 2-element structures)
		// Encoding T1 / A1 Advanced SIMD
		// vst2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.395 VST3 (multiple 3-element structures)
		// Encoding T1 / A1 Advanced SIMD
		// vst3<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst3<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.397 VST4 (multiple 4-element structures)
		// Encoding T1 / A1 Advanced SIMD
		// vst4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		//
		// must precede thumb2_str in table
		new OpcodeARM(Index.thumb2_vst__multi, "vst", "111110010x00xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.392 VST1 (single element from one lane)
		// Encoding T1 / A1 Advanced SIMD
		// vst1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.394 VST2 (single 2-element structure from one lane)
		// Encoding T1 / A1 Advanced SIMD
		// vst2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// A8.6.396 VST3 (single 3-element structure from one lane)
		// Encoding T1 / A1 Advanced SIMD
		// vst3<c>.<size> <list>, [<Rn>]{!}	vst3<c>.<size> <list>, [<Rn>], <Rm>
		// A8.6.398 VST4 (single 4-element structure from one lane)
		// Encoding T1 / A1 Advanced SIMD
		// vst4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		//
		// must precede thumb2_str in table
		new OpcodeARM(Index.thumb2_vst__xlane, "vst", "111110011x00xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.193 STR (immediate, Thumb)
		// Encoding T3 ARMv6T2, ARMv7
		// str<c>.w <Rt>,[<Rn>,#<imm12>]
		// A8.6.193 STR (immediate, Thumb)
		// Encoding T4 ARMv6T2, ARMv7
		// str<c> <Rt>,[<Rn>,#-<imm8>]	str<c> <Rt>,[<Rn>],#+/-<imm8>	str<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// A8.6.195 STR (register)
		// Encoding T2 ARMv6T2, ARMv7
		// str<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// A8.6.196 STRB (immediate, Thumb)
		// Encoding T2 ARMv6T2, ARMv7
		// strb<c>.w <Rt>,[<Rn>,#<imm12>]
		// A8.6.196 STRB (immediate, Thumb)
		// Encoding T3 ARMv6T2, ARMv7
		// strb<c> <Rt>,[<Rn>,#-<imm8>]	strb<c> <Rt>,[<Rn>],#+/-<imm8>	strb<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// A8.6.198 STRB (register)
		// Encoding T2 ARMv6T2, ARMv7
		// strb<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// A8.6.199 STRBT
		// Encoding T1 ARMv6T2, ARMv7
		// strbt<c> <Rt>,[<Rn>,#<imm8>]
		// A8.6.206 STRH (immediate, Thumb)
		// Encoding T2 ARMv6T2, ARMv7
		// strh<c>.w <Rt>,[<Rn>{,#<imm12>}]
		// A8.6.206 STRH (immediate, Thumb)
		// Encoding T3 ARMv6T2, ARMv7
		// strh<c> <Rt>,[<Rn>,#-<imm8>]	strh<c> <Rt>,[<Rn>],#+/-<imm8>	strh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// A8.6.208 STRH (register)
		// Encoding T2 ARMv6T2, ARMv7
		// strh<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// A8.6.209 STRHT
		// Encoding T1 ARMv6T2, ARMv7
		// strht<c> <Rt>,[<Rn>,#<imm8>]
		// A8.6.210 STRT
		// Encoding T1 ARMv6T2, ARMv7
		// strt<c> <Rt>,[<Rn>,#<imm8>]
		//
		// must follow thumb2_push__reg in table
		// must follow thumb2_vld__multi in table
		// must follow thumb2_vld__xlane in table
		// must follow thumb2_vst__multi in table
		// must follow thumb2_vst__xlane in table
		new OpcodeARM(Index.thumb2_str, "str", "1111100xxxx0xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.202 STREX
		// Encoding T1 ARMv6T2, ARMv7
		// strex<c> <Rd>,<Rt>,[<Rn>{,#<imm>}]
		// must precede thumb2_strd in table
		new OpcodeARM(Index.thumb2_strex, "strex", "111010000100xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.203 STREXB
		// Encoding T1 ARMv7
		// strexb<c> <Rd>,<Rt>,[<Rn>]
		// Unpredictable if (1) is 0: 111010001100xxxxxxxx(1)(1)(1)(1)0100xxxx
		// must precede thumb2_strd in table
		// A8.6.204 STREXD
		// Encoding T1 ARMv7
		// strexd<c> <Rd>,<Rt>,<Rt2>,[<Rn>]
		// A8.6.205 STREXH
		// Encoding T1 ARMv7
		// strexh<c> <Rd>,<Rt>,[<Rn>]
		// Unpredictable if (1) is 0: 111010001100xxxxxxxx(1)(1)(1)(1)0101xxxx
		//
		// must precede thumb2_strd in table
		new OpcodeARM(Index.thumb2_strexx, "strex", "111010001100xxxxxxxxxxxx01xxxxxx"),
		// A8.6.200 STRD (immediate)
		// Encoding T1 ARMv6T2, ARMv7
		// strd<c> <Rt>,<Rt2>,[<Rn>{,#+/-<imm>}]	strd<c> <Rt>,<Rt2>,[<Rn>],#+/-<imm>
		// strd<c> <Rt>,<Rt2>,[<Rn>,#+/-<imm>]!
		// must follow thumb2_strex in table
		// must follow thumb2_strexx in table
		new OpcodeARM(Index.thumb2_strd, "strd", "1110100xx1x0xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.211 SUB (immediate, Thumb)
		// Encoding T4 ARMv6T2, ARMv7
		// subw<c> <Rd>,<Rn>,#<imm12>
		// A8.6.215 SUB (SP minus immediate)
		// Encoding T3 ARMv6T2, ARMv7
		// subw <Rd>,sp,#<imm12>
		//
		// must follow thumb2_adr__sub in table
		// must follow thumb2_cmp__imm in table
		new OpcodeARM(Index.thumb2_subw, "subw", "11110x101010xxxx0xxxxxxxxxxxxxxx"),
		// A8.6.211 SUB (immediate, Thumb)
		// Encoding T3 ARMv6T2, ARMv7
		// sub{s}<c>.w <Rd>,<Rn>,#<const>
		// A8.6.215 SUB (SP minus immediate)
		// Encoding T2 ARMv6T2, ARMv7
		// sub{s}.w <Rd>,sp,#<const>
		//
		// must follow thumb2_cmp__imm in table
		new OpcodeARM(Index.thumb2_sub__imm, "sub", "11110x01101xxxxx0xxxxxxxxxxxxxxx"),
		// A8.6.213 SUB (register)
		// Encoding T2 ARMv6T2, ARMv7
		// sub{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 11101011101xxxxx(0)xxxxxxxxxxxxxxx
		// A8.6.216 SUB (SP minus register)
		// Encoding T1 ARMv6T2, ARMv7
		// sub{s} <Rd>,sp,<Rm>{,<shift>}
		// Unpredictable if (0) is 1: 11101011101x1101(0)xxxxxxxxxxxxxxx
		//
		// must follow thumb2_cmp__reg in table
		new OpcodeARM(Index.thumb2_sub__reg, "sub", "11101011101xxxxxxxxxxxxxxxxxxxxx"),
		// A8.6.223 SXTB
		// Encoding T2 ARMv6T2, ARMv7
		// sxtb<c>.w <Rd>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: 11111010010011111111xxxx1(0)xxxxxx
		// must precede thumb2_sxtab in table
		new OpcodeARM(Index.thumb2_sxtb, "sxtb.w", "11111010010011111111xxxx1xxxxxxx"),
		// A8.6.220 SXTAB
		// Encoding T1 ARMv6T2, ARMv7
		// sxtab<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: 111110100100xxxx1111xxxx1(0)xxxxxx
		// must follow thumb2_sxtb in table
		new OpcodeARM(Index.thumb2_sxtab, "sxtab", "111110100100xxxx1111xxxx1xxxxxxx"),
		// A8.6.224 SXTB16
		// Encoding T1 ARMv6T2, ARMv7
		// sxtb16<c> <Rd>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: 11111010001011111111xxxx1(0)xxxxxx
		// must precede thumb2_sxtab16 in table
		new OpcodeARM(Index.thumb2_sxtb16, "sxtb16", "11111010001011111111xxxx1xxxxxxx"),
		// A8.6.221 SXTAB16
		// Encoding T1 ARMv6T2, ARMv7
		// sxtab16<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: 111110100010xxxx1111xxxx1(0)xxxxxx
		// must follow thumb2_sxtb16 in table
		new OpcodeARM(Index.thumb2_sxtab16, "sxtab16", "111110100010xxxx1111xxxx1xxxxxxx"),
		// A8.6.225 SXTH
		// Encoding T2 ARMv6T2, ARMv7
		// sxth<c>.w <Rd>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: 11111010000011111111xxxx1(0)xxxxxx
		// must precede thumb2_sxtah in table
		new OpcodeARM(Index.thumb2_sxth, "sxth.w", "11111010000011111111xxxx1xxxxxxx"),
		// A8.6.222 SXTAH
		// Encoding T1 ARMv6T2, ARMv7
		// sxtah<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: 111110100000xxxx1111xxxx1(0)xxxxxx
		// must follow thumb2_sxth in table
		new OpcodeARM(Index.thumb2_sxtah, "sxtah", "111110100000xxxx1111xxxx1xxxxxxx"),
		// A8.6.226 TBB, TBH
		// Encoding T1 ARMv6T2, ARMv7
		// tbb<c> [<Rn>,<Rm>] Outside or last in IT block	tbh<c> [<Rn>,<Rm>,LSL #1] Outside or last in IT block
		// Unpredictable if (1) is 0 or (0) is 1: 111010001101xxxx(1)(1)(1)(1)(0)(0)(0)(0)000xxxxx
		new OpcodeARM(Index.thumb2_tb, "tb", "111010001101xxxxxxxxxxxx000xxxxx"),
		// A8.6.237 UDIV
		// Encoding T1 ARMv7-R
		// udiv<c> <Rd>,<Rn>,<Rm>
		// Unpredictable if (1) is 0: 111110111011xxxx(1)(1)(1)(1)xxxx1111xxxx
		new OpcodeARM(Index.thumb2_udiv, "udiv", "111110111011xxxxxxxxxxxx1111xxxx"),
		// A8.6.244 UMAAL
		// Encoding T1 ARMv6T2, ARMv7
		// umaal<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_umaal, "umaal", "111110111110xxxxxxxxxxxx0110xxxx"),
		// A8.6.245 UMLAL
		// Encoding T1 ARMv6T2, ARMv7
		// umlal<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_umlal, "umlal", "111110111110xxxxxxxxxxxx0000xxxx"),
		// A8.6.246 UMULL
		// Encoding T1 ARMv6T2, ARMv7
		// umull<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_umull, "umull", "111110111010xxxxxxxxxxxx0000xxxx"),
		// A8.6.253 USAD8
		// Encoding T1 ARMv6T2, ARMv7
		// usad8<c> <Rd>,<Rn>,<Rm>
		new OpcodeARM(Index.thumb2_usad8, "usad8", "111110110111xxxx1111xxxx0000xxxx"),
		// A8.6.254 USADA8
		// Encoding T1 ARMv6T2, ARMv7
		// usada8<c> <Rd>,<Rn>,<Rm>,<Ra>
		new OpcodeARM(Index.thumb2_usada8, "usada8", "111110110111xxxxxxxxxxxx0000xxxx"),
		// A8.6.256 USAT16
		// Encoding T1 ARMv6T2, ARMv7
		// usat16<c> <Rd>,#<imm4>,<Rn>
		// Unpredictable if (0) is 1: 11110(0)111010xxxx0000xxxx00(0)(0)xxxx
		// must precede thumb2_usat in table
		new OpcodeARM(Index.thumb2_usat16, "usat16", "11110x111010xxxx0000xxxx00xxxxxx"),
		// A8.6.255 USAT
		// Encoding T1 ARMv6T2, ARMv7
		// usat<c> <Rd>,#<imm5>,<Rn>{,<shift>}
		// Unpredictable if (0) is 1: 11110(0)1110x0xxxx0xxxxxxxxx(0)xxxxx
		// must follow thumb2_usat16 in table
		new OpcodeARM(Index.thumb2_usat, "usat", "11110x1110x0xxxx0xxxxxxxxxxxxxxx"),
		// A8.6.263 UXTB
		// Encoding T2 ARMv6T2, ARMv7
		// uxtb<c>.w <Rd>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: 11111010010111111111xxxx1(0)xxxxxx
		// must precede thumb2_uxtab in table
		new OpcodeARM(Index.thumb2_uxtb, "uxtb.w", "11111010010111111111xxxx1xxxxxxx"),
		// A8.6.260 UXTAB
		// Encoding T1 ARMv6T2, ARMv7
		// uxtab<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: 111110100101xxxx1111xxxx1(0)xxxxxx
		// must follow thumb2_uxtb in table
		new OpcodeARM(Index.thumb2_uxtab, "uxtab", "111110100101xxxx1111xxxx1xxxxxxx"),
		// A8.6.264 UXTB16
		// Encoding T1 ARMv6T2, ARMv7
		// uxtb16<c> <Rd>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: 11111010001111111111xxxx1(0)xxxxxx
		// must precede thumb2_uxtab16 in table
		new OpcodeARM(Index.thumb2_uxtb16, "uxtb16", "11111010001111111111xxxx1xxxxxxx"),
		// A8.6.261 UXTAB16
		// Encoding T1 ARMv6T2, ARMv7
		// uxtab16<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: 111110100011xxxx1111xxxx1(0)xxxxxx
		// must follow thumb2_uxtb16 in table
		new OpcodeARM(Index.thumb2_uxtab16, "uxtab16", "111110100011xxxx1111xxxx1xxxxxxx"),
		// A8.6.265 UXTH
		// Encoding T2 ARMv6T2, ARMv7
		// uxth<c>.w <Rd>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: 11111010000111111111xxxx1(0)xxxxxx
		// must precede thumb2_uxtabh in table
		new OpcodeARM(Index.thumb2_uxth, "uxth.w", "11111010000111111111xxxx1xxxxxxx"),
		// A8.6.262 UXTAH
		// Encoding T1 ARMv6T2, ARMv7
		// uxtah<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// Unpredictable if (0) is 1: 111110100001xxxx1111xxxx1(0)xxxxxx
		// must follow thumb2_uxth in table
		new OpcodeARM(Index.thumb2_uxtah, "uxtah", "111110100001xxxx1111xxxx1xxxxxxx"),

		// VFP and Advanced SIMD instructions

		// A8.6.266 VABA, VABAL
		// Encoding T1 / A1 Advanced SIMD
		// vaba<c>.<dt> <Qd>, <Qn>, <Qm>	vaba<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vaba, "vaba", "111x11110xxxxxxxxxxx0111xxx1xxxx"),
		// A8.6.267 VABD, VABDL (integer)
		// Encoding T1 / A1 Advanced SIMD
		// vabd<c>.<dt> <Qd>, <Qn>, <Qm>	vabd<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vabd__int, "vabd", "111x11110xxxxxxxxxxx0111xxx0xxxx"),
		// A8.6.268 VABD (floating-point)
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vabd<c>.f32 <Qd>, <Qn>, <Qm>	vabd<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vabd__f32, "vabd", "111111110x1xxxxxxxxx1101xxx0xxxx"),
		// A8.6.269 VABS
		// Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variant)
		// vabs<c>.<dt> <Qd>, <Qm>	vabs<c>.<dt> <Dd>, <Dm>
		// must precede thumb2_vabdl in table
		new OpcodeARM(Index.thumb2_vabs, "vabs", "111111111x11xx01xxxx0x110xx0xxxx"),
		// A8.6.269 VABS
		// Encoding T2 / A2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vabs<c>.f64 <Dd>, <Dm>	vabs<c>.f32 <Sd>, <Sm>
		new OpcodeARM(Index.thumb2_vabs__f, "vabs", "111011101x110000xxxx101x11x0xxxx"),
		// A8.6.270 VACGE, VACGT, VACLE, VACLT
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vacge<c>.f32 <Qd>, <Qn>, <Qm>	vacge<c>.f32 <Dd>, <Dn>, <Dm>
		// vacgt<c>.f32 <Qd>, <Qn>, <Qm>	vacgt<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vacge_vacgt, "vac", "111111110xxxxxxxxxxx1110xxx1xxxx"),
		// A8.6.271 VADD (integer)
		// Encoding T1 / A1 Advanced SIMD
		// vadd<c>.<dt> <Qd>, <Qn>, <Qm>	vadd<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vadd__int, "vadd", "111011110xxxxxxxxxxx1000xxx0xxxx"),
		// A8.6.272 VADD (floating-point)
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variants)
		// vadd<c>.f32 <Qd>, <Qn>, <Qm>	vadd<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vadd__f32, "vadd", "111011110x0xxxxxxxxx1101xxx0xxxx"),
		// A8.6.272 VADD (floating-point)
		// Encoding T2 / A2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vadd<c>.f64 <Dd>, <Dn>, <Dm>	vadd<c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.thumb2_vadd__fp_f, "vadd", "111011100x11xxxxxxxx101xx0x0xxxx"),
		// A8.6.273 VADDHN
		// Encoding T1 / A1 Advanced SIMD
		// vaddhn<c>.<dt> <Dd>, <Qn>, <Qm>
		new OpcodeARM(Index.thumb2_vaddhn, "vaddhn", "111011111xxxxxxxxxxx0100x0x0xxxx"),
		// A8.6.276 VAND (register)
		// Encoding T1 / A1 Advanced SIMD
		// vand<c> <Qd>, <Qn>, <Qm>	vand<c> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vand, "vand", "111011110x00xxxxxxxx0001xxx1xxxx"),
		// A8.6.278 VBIC (register)
		// Encoding T1 / A1 Advanced SIMD
		// vbic<c> <Qd>, <Qn>, <Qm>	vbic<c> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vbic__reg, "vbic", "111011110x01xxxxxxxx0001xxx1xxxx"),
		// A8.6.304 VEOR - MUST PRECEDE VBIF/VBIT/VBSL
		// Encoding T1 / A1 Advanced SIMD
		// veor<c> <Qd>, <Qn>, <Qm>	veor<c> <Dd>, <Dn>, <Dm>
		// A8.6.279 VBIF, VBIT, VBSL
		// Encoding T1 / A1 Advanced SIMD
		// vbif<c> <Qd>, <Qn>, <Qm>	vbif<c> <Dd>, <Dn>, <Dm>	vbit<c> <Qd>, <Qn>, <Qm
		// vbit<c> <Dd>, <Dn>, <Dm>	vbsl<c> <Qd>, <Qn>, <Qm>	vbsl<c> <Dd>, <Dn>, <Dm>
		//
		new OpcodeARM(Index.thumb2_vbif_vbit_vbsl_veor, "v", "111111110xxxxxxxxxxx0001xxx1xxxx"),
		// A8.6.280 VCEQ (register)
		// Encoding T1 / A1 Advanced SIMD
		// vceq<c>.<dt> <Qd>, <Qn>, <Qm>	vceq<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vceq__reg_int, "vceq", "111111110xxxxxxxxxxx1000xxx1xxxx"),
		// A8.6.280 VCEQ (register)
		// Encoding T2 / A2 Advanced SIMD (UNDEFINED in integer-only variant)
		// vceq<c>.f32 <Qd>, <Qn>, <Qm>	vceq<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vceq__reg_f32, "vceq", "111011110x0xxxxxxxxx1110xxx0xxxx"),
		// A8.6.281 VCEQ (immediate #0)
		// Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vceq<c>.<dt> <Qd>, <Qm>, #0	vceq<c>.<dt> <Dd>, <Dm>, #0
		// must precede thumb2_vaddl_vaddw in table
		// must precede thumb2_vabal in table
		new OpcodeARM(Index.thumb2_vceq__imm0, "vceq", "111111111x11xx01xxxx0x010xx0xxxx"),
		// A8.6.282 VCGE (register)
		// Encoding T1 / A1 Advanced SIMD
		// vcge<c>.<dt> <Qd>, <Qn>, <Qm>	vcge<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vcge__reg_int, "vcge", "111x11110xxxxxxxxxxx0011xxx1xxxx"),
		// A8.6.282 VCGE (register)
		// Encoding T2 / A2 Advanced SIMD (UNDEFINED in integer-only variant)
		// vcge<c>.f32 <Qd>, <Qn>, <Qm>	vcge<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vcge__reg_f32, "vcge", "111111110x0xxxxxxxxx1110xxx0xxxx"),
		// A8.6.284 VCGT (register)
		// Encoding T1 / A1 Advanced SIMD
		// vcgt<c>.<dt> <Qd>, <Qn>, <Qm>	vcgt<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vcgt__reg_int, "vcgt", "111x11110xxxxxxxxxxx0011xxx0xxxx"),
		// A8.6.284 VCGT (register)
		// Encoding T2 / A2 Advanced SIMD (UNDEFINED in integer-only variant)
		// vcgt<c>.f32 <Qd>, <Qn>, <Qm>	vcgt<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vcgt__reg_f32, "vcgt", "111111110x1xxxxxxxxx1110xxx0xxxx"),
		// A8.6.283 VCGE (immediate #0)
		// Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vcge<c>.<dt> <Qd>, <Qm>, #0	vcge<c>.<dt> <Dd>, <Dm>, #0
		// must precede thumb2_vaddl_vaddw in table
		new OpcodeARM(Index.thumb2_vcge__imm0, "vcge", "111111111x11xx01xxxx0x001xx0xxxx"),
		// A8.6.285 VCGT (immediate #0)
		// Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vcgt<c>.<dt> <Qd>, <Qm>, #0	vcgt<c>.<dt> <Dd>, <Dm>, #0
		// must precede thumb2_vaddl_vaddw in table
		new OpcodeARM(Index.thumb2_vcgt__imm0, "vcgt", "111111111x11xx01xxxx0x000xx0xxxx"),
		// A8.6.287 VCLE (immediate #0)
		// Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vcle<c>.<dt> <Qd>, <Qm>, #0	vcle<c>.<dt> <Dd>, <Dm>, #0
		// must precede thumb2_vaddl_vaddw in table
		new OpcodeARM(Index.thumb2_vcle, "vcle", "111111111x11xx01xxxx0x011xx0xxxx"),
		// A8.6.288 VCLS
		// Encoding T1 / A1 Advanced SIMD
		// vcls<c>.<dt> <Qd>, <Qm>	vcls<c>.<dt> <Dd>, <Dm>
		new OpcodeARM(Index.thumb2_vcls, "vcls", "111111111x11xx00xxxx01000xx0xxxx"),
		// A8.6.290 VCLT (immediate #0)
		// Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vclt<c>.<dt> <Qd>, <Qm>, #0	vclt<c>.<dt> <Dd>, <Dm>, #0
		new OpcodeARM(Index.thumb2_vclt, "vclt", "111111111x11xx01xxxx0x100xx0xxxx"),
		// A8.6.291 VCLZ
		// Encoding T1 / A1 Advanced SIMD
		// vclz<c>.<dt> <Qd>, <Qm>	vclz<c>.<dt> <Dd>, <Dm>
		new OpcodeARM(Index.thumb2_vclz, "vclz", "111111111x11xx00xxxx01001xx0xxxx"),
		// A8.6.292 VCMP, VCMPE
		// Encoding T1 / A1 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vcmp{e}<c>.f64 <Dd>, <Dm>	vcmp{e}<c>.f32 <Sd>, <Sm>
		new OpcodeARM(Index.thumb2_vcmp__reg, "vcmp", "111011101x110100xxxx101xx1x0xxxx"),
		// A8.6.292 VCMP, VCMPE
		// Encoding T2 / A2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vcmp{e}<c>.f64 <Dd>, #0.0	vcmp{e}<c>.f32 <Sd>, #0.0
		// Unpredictable if (0) is 1: 111011101x110101xxxx101xx1(0)0(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_vcmp__to_0, "vcmp", "111011101x110101xxxx101xx1x0xxxx"),
		// A8.6.293 VCNT
		// Encoding T1 / A1 Advanced SIMD
		// vcnt<c>.8 <Qd>, <Qm>	vcnt<c>.8 <Dd>, <Dm>
		new OpcodeARM(Index.thumb2_vcnt, "vcnt", "111111111x11xx00xxxx01010xx0xxxx"),
		// A8.6.294 VCVT (between floating-point and integer, Advanced SIMD)
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vcvt<c>.<Td>.<Tm> <Qd>, <Qm>	vcvt<c>.<Td>.<Tm> <Dd>, <Dm>
		new OpcodeARM(Index.thumb2_vcvt__fp_i_vec, "vcvt", "111111111x11xx11xxxx011xxxx0xxxx"),
		// A8.6.297 VCVT (between floating-point and fixed-point, VFP)
		// Encoding T1 / A1 VFPv3 (sf = 1 UNDEFINED in single-precision only variants)
		// vcvt<c>.<Td>.f64 <Dd>, <Dd>, #<fbits>	vcvt<c>.<Td>.f32 <Sd>, <Sd>, #<fbits>
		// vcvt<c>.f64.<Td> <Dd>, <Dd>, #<fbits>	vcvt<c>.f32.<Td> <Sd>, <Sd>, #<fbits>
		// must precede thumb2_vcvt__fp_i_reg in table
		new OpcodeARM(Index.thumb2_vcvt__fp_fix_reg, "vcvt", "111011101x111x1xxxxx101xx1x0xxxx"),
		// A8.6.295 VCVT, VCVTR (between floating-point and integer, VFP)
		// Encoding T1 / A1 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vcvt{r}<c>.s32.f64 <Sd>, <Dm>	vcvt{r}<c>.s32.f32 <Sd>, <Sm>	vcvt{r}<c>.u32.f64 <Sd>, <Dm>
		// vcvt{r}<c>.u32.f32 <Sd>, <Sm>	vcvt<c>.f64.<Tm> <Dd>, <Sm>	vcvt<c>.f32.<Tm> <Sd>, <Sm>
		// must follow thumb2_vcvt__fp_fix_reg in table
		new OpcodeARM(Index.thumb2_vcvt__fp_i_reg, "vcvt", "111011101x111xxxxxxx101xx1x0xxxx"),
		// A8.6.277 VBIC (immediate)
		// Encoding T1 / A1 Advanced SIMD
		// vbic<c>.<dt> <Qd>, #<imm>	vbic<c>.<dt> <Dd>, #<imm>
		// A8.6.346 VORR (immediate)
		// Encoding T1 / A1 Advanced SIMD
		// vorr<c>.<dt> <Qd>, #<imm>	vorr<c>.<dt> <Dd>, #<imm>
		// A8.6.340 VMVN (immediate)
		// Encoding T1 / A1 Advanced SIMD
		// vmvn<c>.<dt> <Qd>, #<imm>	vmvn<c>.<dt> <Dd>, #<imm>
		// A8.6.326 VMOV (immediate)
		// Encoding T1 / A1 Advanced SIMD
		// vmov<c>.<dt> <Qd>, #<imm>	vmov<c>.<dt> <Dd>, #<imm>
		//
		// must precede thumb2_vcvt__fp_fix_vec in table
		new OpcodeARM(Index.thumb2_vmov_vbitwise, "_", "111x11111x000xxxxxxxxxxx0xx1xxxx"),
		// A8.6.296 VCVT (between floating-point and fixed-point, Advanced SIMD)
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vcvt<c>.<Td>.<Tm> <Qd>, <Qm>, #<fbits>	vcvt<c>.<Td>.<Tm> <Dd>, <Dm>, #<fbits>
		// must follow thumb2_vmov_vbitwise in table
		new OpcodeARM(Index.thumb2_vcvt__fp_fix_vec, "vcvt", "111x11111xxxxxxxxxxx111x0xx1xxxx"),
		// A8.6.298 VCVT (between double-precision and single-precision)
		// Encoding T1 / A1 VFPv2, VFPv3 (UNDEFINED in single-precision only variants)
		// vcvt<c>.f64.f32 <Dd>, <Sm>	vcvt<c>.f32.f64 <Sd>, <Dm>
		new OpcodeARM(Index.thumb2_vcvt__dp_sp, "vcvt", "111011101x110111xxxx101x11x0xxxx"),
		// A8.6.299 VCVT (between half-precision and single-precision, Advanced SIMD)
		// Encoding T1 / A1 Advanced SIMD with half-precision extensions (UNDEFINED in integer-only variant)
		// vcvt<c>.f32.f16 <Qd>, <Dm>	vcvt<c>.f16.f32 <Dd>, <Qm>
		new OpcodeARM(Index.thumb2_vcvt__hp_sp_vec, "vcvt", "111111111x11xx10xxxx011x00x0xxxx"),
		// A8.6.300 VCVTB, VCVTT (between half-precision and single-precision, VFP)
		// Encoding T1 / A1 VFPv3 half-precision extensions
		// vcvt<y><c>.f32.f16 <Sd>, <Sm>	vcvt<y><c>.f16.f32 <Sd>, <Sm>
		new OpcodeARM(Index.thumb2_vcvt__hp_sp_reg, "vcvt", "111011101x11001xxxxx1010x1x0xxxx"),
		// A8.6.301 VDIV
		// Encoding T1 / A1 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vdiv<c>.f64 <Dd>, <Dn>, <Dm>	vdiv<c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.thumb2_vdiv, "vdiv", "111011101x00xxxxxxxx101xx0x0xxxx"),
		// A8.6.302 VDUP (scalar)
		// Encoding T1 / A1 Advanced SIMD
		// vdup<c>.<size> <Qd>, <Dm[x]>	vdup<c>.<size> <Dd>, <Dm[x]>
		new OpcodeARM(Index.thumb2_vdup__scalar, "vdup", "111111111x11xxxxxxxx11000xx0xxxx"),
		// A8.6.303 VDUP (ARM core register)
		// Encoding T1 / A1 Advanced SIMD
		// vdup<c>.<size> <Qd>, <Rt>	vdup<c>.<size> <Dd>, <Rt>
		// Unpredictable if (0) is 1: 111011101xx0xxxxxxxx1011x0x1(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_vdup__reg, "vdup", "111011101xx0xxxxxxxx1011x0x1xxxx"),
		// A8.6.305 VEXT
		// Encoding T1 / A1 Advanced SIMD
		// vext<c>.8 <Qd>, <Qn>, <Qm>, #<imm>	vext<c>.8 <Dd>, <Dn>, <Dm>, #<imm>
		new OpcodeARM(Index.thumb2_vext, "vext.8", "111011111x11xxxxxxxxxxxxxxx0xxxx"),
		// A8.6.306 VHADD, VHSUB
		// Encoding T1 / A1 Advanced SIMD
		// vh<op><c> <Qd>, <Qn>, <Qm>	vh<op><c> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vhadd_vhsub, "vh", "111x11110xxxxxxxxxxx00x0xxx0xxxx"),

		// A8.6.320 VLDR
		// Encoding T1 / A1 VFPv2, VFPv3, Advanced SIMD
		// vldr<c> <Dd>, [<Rn>{, #+/-<imm>}]	vldr<c> <Dd>, <label>	vldr<c> <Dd>, [pc,#-0] Special case
		// must precede thumb2_vldm__64 in table
		new OpcodeARM(Index.thumb2_vldr__64, "vldr", "11101101xx01xxxxxxxx1011xxxxxxxx"),
		// A8.6.354 VPOP
		// Encoding T1 / A1 VFPv2, VFPv3, Advanced SIMD
		// vpop <list> <list> is consecutive 64-bit registers
		// A8.6.354 VPOP
		// Encoding T2 / A2 VFPv2, VFPv3
		// vpop <list> <list> is consecutive 32-bit registers
		// must precede thumb2_vldm__32 in table
		// must precede thumb2_vldm__64 in table
		new OpcodeARM(Index.thumb2_vpop, "vpop", "111011001x111101xxxx101xxxxxxxxx"),
		// A8.6.332 VMOV (between two ARM core registers and a doubleword extension register)
		// Encoding T1 / A1 VFPv2, VFPv3, Advanced SIMD
		// vmov<c> <Dm>, <Rt>, <Rt2>	vmov<c> <Rt>, <Rt2>, <Dm>
		// must precede thumb2_vldm_32 in table
		// must precede thumb2_vldm_64 in table
		new OpcodeARM(Index.thumb2_vmov_9, "vmov", "11101100010xxxxxxxxx101100x1xxxx"),
		// A8.6.319 VLDM
		// Encoding T1 / A1 VFPv2, VFPv3, Advanced SIMD
		// vldm{mode}<c> <Rn>{!}, <list> <list> is consecutive 64-bit registers
		// must follow thumb2_vldr_1 in table
		// must follow thumb2_vpop in table
		// must follow thumb2_vmov_9 in table
		new OpcodeARM(Index.thumb2_vldm__64, "vldm", "1110110xxxx1xxxxxxxx1011xxxxxxxx"),
		// A8.6.320 VLDR
		// Encoding T2 / A2 VFPv2, VFPv3
		// vldr<c> <Sd>, [<Rn>{, #+/-<imm>}]	vldr<c> <Sd>, <label>	vldr<c> <Sd>, [pc,#-0] Special case
		// must precede thumb2_vldm__32 in table
		new OpcodeARM(Index.thumb2_vldr__32, "vldr", "11101101xx01xxxxxxxx1010xxxxxxxx"),
		// A8.6.331 VMOV (between two ARM core registers and two single-precision registers)
		// Encoding T1 / A1 VFPv2, VFPv3
		// vmov<c> <Sm>, <Sm1>, <Rt>, <Rt2>	vmov<c> <Rt>, <Rt2>, <Sm>, <Sm1>
		// must precede thumb2_vldm__32 in table
		new OpcodeARM(Index.thumb2_vmov_8, "vmov", "11101100010xxxxxxxxx101000x1xxxx"),
		// A8.6.319 VLDM
		// Encoding T2 / A2 VFPv2, VFPv3
		// vldm{mode}<c> <Rn>{!}, <list> <list> is consecutive 32-bit registers
		// must follow thumb2_vldr__32 in table
		// must follow thumb2_vpop in table
		// must follow thumb2_vmov_8 in table
		new OpcodeARM(Index.thumb2_vldm__32, "vldm", "1110110xxxx1xxxxxxxx1010xxxxxxxx"),
		// A8.6.321 VMAX, VMIN (integer)
		// Encoding T1 / A1 Advanced SIMD
		// vmax<c>.<dt> <Qd>, <Qn>, <Qm>	vmax<c>.<dt> <Dd>, <Dn>, <Dm>
		// vmin<c>.<dt> <Qd>, <Qn>, <Qm>	vmin<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vmax_vmin__int, "v", "111x11110xxxxxxxxxxx0110xxxxxxxx"),
		// A8.6.322 VMAX, VMIN (floating-point)
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vmax<c>.f32 <Qd>, <Qn>, <Qm>	vmax<c>.f32 <Dd>, <Dn>, <Dm>
		// vmin<c>.f32 <Qd>, <Qn>, <Qm>	vmin<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vmax_vmin__fp, "v", "111011110xxxxxxxxxxx1111xxx0xxxx"),
		// A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
		// Encoding T1 / A1 Advanced SIMD
		// v<op><c>.<dt> <Qd>, <Qn>, <Qm>	v<op><c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vml__int, "vml", "111x11110xxxxxxxxxxx1001xxx0xxxx"),
		// A8.6.406 VTBL, VTBX
		// Encoding T1 / A1 Advanced SIMD
		// v<op><c>.8 <Dd>, <list>, <Dm>
		// must precede thumb2_vml__int_long in table
		new OpcodeARM(Index.thumb2_vtb, "vtb", "111111111x11xxxxxxxx10xxxxx0xxxx"),
		// A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
		// Encoding T2 / A2 Advanced SIMD
		// v<op>l<c>.<dt> <Qd>, <Dn>, <Dm>
		// must follow thumb2_vtb in table
		new OpcodeARM(Index.thumb2_vml__int_long, "vml", "111x11111xxxxxxxxxxx10x0x0x0xxxx"),
		// A8.6.324 VMLA, VMLS (floating-point)
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// v<op><c>.f32 <Qd>, <Qn>, <Qm>	v<op><c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vml__f32, "vml", "111011110xxxxxxxxxxx1101xxx1xxxx"),
		// A8.6.324 VMLA, VMLS (floating-point)
		// Encoding T2 / A2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// v<op><c>.f64 <Dd>, <Dn>, <Dm>	v<op><c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.thumb2_vml__fp, "vml", "111011100x00xxxxxxxx101xxxx0xxxx"),
		// A8.6.277 VBIC (immediate)
		// Encoding T1 / A1 Advanced SIMD
		// vbic<c>.<dt> <Qd>, #<imm>	vbic<c>.<dt> <Dd>, #<imm>
		// A8.6.326 VMOV (immediate)
		// Encoding T1 / A1 Advanced SIMD
		// vmov<c>.<dt> <Qd>, #<imm>	vmov<c>.<dt> <Dd>, #<imm>
		// A8.6.346 VORR (immediate)
		// Encoding T1 / A1 Advanced SIMD
		// vorr<c>.<dt> <Qd>, #<imm>	vorr<c>.<dt> <Dd>, #<imm>
		// A8.6.340 VMVN (immediate)
		// Encoding T1 / A1 Advanced SIMD
		// vmvn<c>.<dt> <Qd>, #<imm>	vmvn<c>.<dt> <Dd>, #<imm>
		//
		// must follow thumb2_vorr__imm in table
		new OpcodeARM(Index.thumb2_vmov_vbitwise, null, "111x11111x000xxxxxxxxxxx0xx1xxxx"),
		// A8.6.326 VMOV (immediate)
		// Encoding T2 / A2 VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vmov<c>.f64 <Dd>, #<imm>	vmov<c>.f32 <Sd>, #<imm>
		// Unpredictable if (0) is 1: 111011101x11xxxxxxxx101x(0)0(0)0xxxx
		new OpcodeARM(Index.thumb2_vmov__imm, "vmov", "111011101x11xxxxxxxx101xx0x0xxxx"),
		// A8.6.327 VMOV (register)
		// Encoding T1 / A1 Advanced SIMD
		// vmov<c> <Qd>, <Qm>	vmov<c> <Dd>, <Dm>
		// A8.6.347 VORR (register)
		// Encoding T1 / A1 Advanced SIMD
		// vorr<c> <Qd>, <Qn>, <Qm>	vorr<c> <Dd>, <Dn>, <Dm>
		//
		new OpcodeARM(Index.thumb2_vmov_vorr, "vmov", "111011110x10xxxxxxxx0001xxx1xxxx"),
		// A8.6.327 VMOV (register)
		// Encoding T2 / A2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vmov<c>.f64 <Dd>, <Dm>	vmov<c>.f32 <Sd>, <Sm>
		new OpcodeARM(Index.thumb2_vmov__reg_f, "vmov", "111011101x110000xxxx101x01x0xxxx"),
		// A8.6.328 VMOV (ARM core register to scalar)
		// Encoding T1 / A1 VFPv2, VFPv3, Advanced SIMD for word version (opc1:opc2 == '0x00')'; Advanced SIMD otherwise
		// vmov<c>.<size> <Dd[x]>, <Rt>
		// Unpredictable if (0) is 1: 111011100xx0xxxxxxxx1011xxx1(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_vmov_5, "vmov", "111011100xx0xxxxxxxx1011xxx1xxxx"),
		// A8.6.329 VMOV (scalar to ARM core register)
		// Encoding T1 / A1 VFPv2, VFPv3, Advanced SIMD for word version (U:opc1:opc2 == '00x00'); Advanced SIMD otherwise
		// vmov<c>.<dt> <Rt>, <Dn[x]>
		// Unpredictable if (0) is 1: 11101110xxx1xxxxxxxx1011xxx1(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_vmov_6, "vmov", "11101110xxx1xxxxxxxx1011xxx1xxxx"),
		// A8.6.330 VMOV (between ARM core register and single-precision register)
		// Encoding T1 / A1 VFPv2, VFPv3
		// vmov<c> <Sn>, <Rt>	vmov<c> <Rt>, <Sn>
		// Unpredictable if (0) is 1: 11101110000xxxxxxxxx1010x(0)(0)1(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_vmov_7, "vmov", "11101110000xxxxxxxxx1010xxx1xxxx"),
		// A8.6.333 VMOVL
		// Encoding T1 / A1 Advanced SIMD
		// vmovl<c>.<dt> <Qd>, <Dm>
		// must precede thumb2_vshll__various in table
		new OpcodeARM(Index.thumb2_vmovl, "vmovl", "111x11111xxxx000xxxx101000x1xxxx"),
		// A8.6.384 VSHLL
		// Encoding T1 / A1 Advanced SIMD
		// vshll<c>.<type><size> <Qd>, <Dm>, #<imm> (0 < <imm> < <size>)
		// must follow thumb2_vmovl in table
		new OpcodeARM(Index.thumb2_vshll__various, "vshll", "111x11111xxxxxxxxxxx101000x1xxxx"),
		// A8.6.334 VMOVN
		// Encoding T1 / A1 Advanced SIMD
		// vmovn<c>.<dt> <Dd>, <Qm>
		// must precede thumb2_vqmov in table
		new OpcodeARM(Index.thumb2_vmovn, "vmovn", "111111111x11xx10xxxx001000x0xxxx"),
		// A8.6.335 VMRS
		// Encoding T1 / A1 VFPv2, VFPv3, Advanced SIMD
		// vmrs<c> <Rt>, fpscr
		// Unpredictable if (0) is 1: 1110111011110001xxxx10100(0)(0)1(0)(0)(0)(0)
		// B6.1.14 VMRS
		// Encoding T1 /A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// vmrs<c> <Rt>,<spec_reg>
		// Unpredictable if (0) is 1: 111011101111xxxxxxxx1010(0)(0)(0)1(0)(0)(0)(0)
		//
		new OpcodeARM(Index.thumb2_vmrs, "vmrs", "111011101111xxxxxxxx10100xx1xxxx"),
		// A8.6.336 VMSR
		// Encoding T1 / A1 VFPv2, VFPv3, Advanced SIMD
		// vmsr<c> fpscr, <Rt>
		// Unpredictable if (0) is 1: 1110111011100001xxxx10100(0)(0)1(0)(0)(0)(0)
		// B6.1.15 VMSR
		// Encoding T1 /A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// vmsr<c> <spec_reg>,<Rt>
		// Unpredictable if (0) is 1: 111011101110xxxxxxxx1010(0)(0)(0)1(0)(0)(0)(0)
		//
		new OpcodeARM(Index.thumb2_vmsr, "vmsr", "111011101110xxxxxxxx10100xx1xxxx"),
		// A8.6.337 VMUL, VMULL (integer and polynomial)
		// Encoding T1 / A1 Advanced SIMD
		// vmul<c>.<dt> <Qd>, <Qn>, <Qm>	vmul<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vmul_1, "vmul", "111x11110xxxxxxxxxxx1001xxx1xxxx"),
		// A8.6.337 VMUL, VMULL (integer and polynomial)
		// Encoding T2 / A2 Advanced SIMD
		// vmull<c>.<dt> <Qd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vmull, "vmull", "111x11111xxxxxxxxxxx11x0x0x0xxxx"),
		// A8.6.338 VMUL (floating-point)
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vmul<c>.f32 <Qd>, <Qn>, <Qm>	vmul<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vmul__f32, "vmul", "111111110x0xxxxxxxxx1101xxx1xxxx"),
		// A8.6.338 VMUL (floating-point)
		// Encoding T2 / A2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vmul<c>.f64 <Dd>, <Dn>, <Dm>	vmul<c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.thumb2_vmul__fp_2, "vmul", "111011100x10xxxxxxxx101xx0x0xxxx"),
		// A8.6.360 VQDMULL
		// Encoding T2 / A2 Advanced SIMD
		// vqdmull<c>.<dt> <Qd>,<Dn>,<Dm[x]>
		// must precede thumb2_vmul__scalar in table
		new OpcodeARM(Index.thumb2_vqdmull__scalar, "vqdmul", "111011111xxxxxxxxxxx1011x1x0xxxx"),
		// A8.6.339 VMUL, VMULL (by scalar)
		// Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vmul<c>.<dt> <Qd>, <Qn>, <Dm[x]>	vmul<c>.<dt> <Dd>, <Dn>, <Dm[x]>
		// A8.6.339 VMUL, VMULL (by scalar)
		// Encoding T2 / A2 Advanced SIMD
		// vmull<c>.<dt> <Qd>, <Dn>, <Dm[x]>
		// must follow thumb2_vqdmull__scalar in table
		//
		new OpcodeARM(Index.thumb2_vmul__scalar, "vmul", "111x11111xxxxxxxxxxx10xxx1x0xxxx"),
		// A8.6.341 VMVN (register)
		// Encoding T1 / A1 Advanced SIMD
		// vmvn<c> <Qd>, <Qm>	vmvn<c> <Dd>, <Dm>
		new OpcodeARM(Index.thumb2_vmvn, "vmvn", "111111111x11xx00xxxx01011xx0xxxx"),
		// A8.6.342 VNEG
		// Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vneg<c>.<dt> <Qd>, <Qm>	vneg<c>.<dt> <Dd>, <Dm>
		new OpcodeARM(Index.thumb2_vneg, "vneg", "111111111x11xx01xxxx0x111xx0xxxx"),
		// A8.6.342 VNEG
		// Encoding T2 / A2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vneg<c>.f64 <Dd>, <Dm>	vneg<c>.f32 <Sd>, <Sm>
		new OpcodeARM(Index.thumb2_vneg__f, "vneg", "111011101x110001xxxx101x01x0xxxx"),
		// A8.6.343 VNMLA, VNMLS, VNMUL
		// Encoding T1 / A1 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vnmla<c>.f64 <Dd>, <Dn>, <Dm>	vnmla<c>.f32 <Sd>, <Sn>, <Sm>
		// vnmls<c>.f64 <Dd>, <Dn>, <Dm>	vnmls<c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.thumb2_vnml, "vnml", "111011100x01xxxxxxxx101xxxx0xxxx"),
		// A8.6.343 VNMLA, VNMLS, VNMUL
		// Encoding T2 / A2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vnmul<c>.f64 <Dd>, <Dn>, <Dm>	vnmul<c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.thumb2_vnmul, "vnmul", "111011100x10xxxxxxxx101xx1x0xxxx"),
		// A8.6.345 VORN (register)
		// Encoding T1 / A1 Advanced SIMD
		// vorn<c> <Qd>, <Qn>, <Qm>	vorn<c> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vorn, "vorn", "111011110x11xxxxxxxx0001xxx1xxxx"),
		// A8.6.348 VPADAL
		// Encoding T1 / A1 Advanced SIMD
		// vpadal<c>.<dt> <Qd>, <Qm>	vpadal<c>.<dt> <Dd>, <Dm>
		new OpcodeARM(Index.thumb2_vpadal, "vpadal", "111111111x11xx00xxxx0110xxx0xxxx"),
		// A8.6.349 VPADD (integer)
		// Encoding T1 / A1 Advanced SIMD
		// vpadd<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vpadd__int, "vpadd", "111011110xxxxxxxxxxx1011xxx1xxxx"),
		// A8.6.350 VPADD (floating-point)
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vpadd<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vpadd__f32, "vpadd", "111111110x0xxxxxxxxx1101xxx0xxxx"),
		// A8.6.351 VPADDL
		// Encoding T1 / A1 Advanced SIMD
		// vpaddl<c>.<dt> <Qd>, <Qm>	vpaddl<c>.<dt> <Dd>, <Dm>
		new OpcodeARM(Index.thumb2_vpaddl, "vpaddl", "111111111x11xx00xxxx0010xxx0xxxx"),
		// A8.6.352 VPMAX, VPMIN (integer)
		// Encoding T1 / A1 Advanced SIMD
		// vp<op><c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vpmax_vpmin__int, "vp", "111x11110xxxxxxxxxxx1010xxxxxxxx"),
		// A8.6.353 VPMAX, VPMIN (floating-point)
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vp<op><c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vpmax_vpmin__fp, "vp", "111111110xxxxxxxxxxx1111xxx0xxxx"),
		// A8.6.355 VPUSH
		// Encoding T1 / A1 VFPv2, VFPv3, Advanced SIMD
		// vpush<c> <list> <list> is consecutive 64-bit registers
		// A8.6.355 VPUSH
		// Encoding T2 / A2 VFPv2, VFPv3
		// vpush<c> <list> <list> is consecutive 32-bit registers
		// must precede thumb2_vstm_1 in table
		new OpcodeARM(Index.thumb2_vpush, "vpush", "111011010x101101xxxx101xxxxxxxxx"),
		// A8.6.356 VQABS
		// Encoding T1 / A1 Advanced SIMD
		// vqabs<c>.<dt> <Qd>,<Qm>	vqabs<c>.<dt> <Dd>,<Dm>
		// must precede thumb2_vstm_2 in table
		// must precede thumb2_vabdl in table
		new OpcodeARM(Index.thumb2_vqabs, "vqabs", "111111111x11xx00xxxx01110xx0xxxx"),
		// A8.6.362 VQNEG
		// Encoding T1 / A1 Advanced SIMD
		// vqneg<c>.<dt> <Qd>,<Qm>	vqneg<c>.<dt> <Dd>,<Dm>
		// must precede thumb2_vabdl in table
		new OpcodeARM(Index.thumb2_vqneg, "vqneg", "111111111x11xx00xxxx01111xx0xxxx"),
		// A8.6.267 VABD, VABDL (integer)
		// Encoding T2 / A2 Advanced SIMD
		// vabdl<c>.<dt> <Qd>, <Dn>, <Dm>
		// must follow thumb2_vabs in table
		// must follow thumb2_vqabs in table
		// must follow thumb2_vqneg in table
		new OpcodeARM(Index.thumb2_vabdl, "vabdl", "111x11111xxxxxxxxxxx0111x0x0xxxx"),
		// A8.6.357 VQADD
		// Encoding T1 / A1 Advanced SIMD
		// vqadd<c>.<dt> <Qd>,<Qn>,<Qm>	vqadd<c>.<dt> <Dd>,<Dn>,<Dm>
		new OpcodeARM(Index.thumb2_vqadd, "vqadd", "111x11110xxxxxxxxxxx0000xxx1xxxx"),
		// A8.6.358 VQDMLAL, VQDMLSL
		// Encoding T1 / A1 Advanced SIMD
		// vqd<op><c>.<dt> <Qd>,<Dn>,<Dm>
		new OpcodeARM(Index.thumb2_vqdml__vec, "vqdml", "111011111xxxxxxxxxxx10x1x0x0xxxx"),
		// A8.6.358 VQDMLAL, VQDMLSL
		// Encoding T2 / A2 Advanced SIMD
		// vqd<op><c>.<dt> <Qd>,<Dn>,<Dm[x]>
		new OpcodeARM(Index.thumb2_vqdml__scalar, "vqdml", "111011111xxxxxxxxxxx0x11x1x0xxxx"),
		// A8.6.359 VQDMULH
		// Encoding T1 / A1 Advanced SIMD
		// vqdmulh<c>.<dt> <Qd>,<Qn>,<Qm>	vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm>
		new OpcodeARM(Index.thumb2_vqdmulh__vec, "vqdmulh", "111011110xxxxxxxxxxx1011xxx0xxxx"),
		// A8.6.359 VQDMULH
		// Encoding T2 / A2 Advanced SIMD
		// vqdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>	vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>
		new OpcodeARM(Index.thumb2_vqdmulh__scalar, "vqdmulh", "111x11111xxxxxxxxxxx1100x1x0xxxx"),
		// A8.6.360 VQDMULL
		// Encoding T1 / A1 Advanced SIMD
		// vqdmull<c>.<dt> <Qd>,<Dn>,<Dm>
		new OpcodeARM(Index.thumb2_vqdmull__vec, "vqdmull", "111011111xxxxxxxxxxx1101x0x0xxxx"),
		// A8.6.361 VQMOVN, VQMOVUN
		// Encoding T1 / A1 Advanced SIMD
		// vqmov{u}n<c>.<type><size> <Dd>, <Qm>
		// must follow thumb2_vmovn in table
		new OpcodeARM(Index.thumb2_vqmov, "vqmov", "111111111x11xx10xxxx0010xxx0xxxx"),
		// A8.6.363 VQRDMULH
		// Encoding T1 / A1 Advanced SIMD
		// vqrdmulh<c>.<dt> <Qd>,<Qn>,<Qm>	vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm>
		new OpcodeARM(Index.thumb2_vqrdmulh__vec, "vqrdmulh", "111111110xxxxxxxxxxx1011xxx0xxxx"),
		// A8.6.363 VQRDMULH
		// Encoding T2 / A2 Advanced SIMD
		// vqrdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>	vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>
		new OpcodeARM(Index.thumb2_vqrdmulh__scalar, "vqrdmulh", "111x11111xxxxxxxxxxx1101x1x0xxxx"),
		// A8.6.364 VQRSHL
		// Encoding T1 / A1 Advanced SIMD
		// vqrshl<c>.<type><size> <Qd>,<Qm>,<Qn>	vqrshl<c>.<type><size> <Dd>,<Dm>,<Dn>
		new OpcodeARM(Index.thumb2_vqrshl, "vqrshl", "111x11110xxxxxxxxxxx0101xxx1xxxx"),
		// A8.6.377 VRSHRN
		// Encoding T1 / A1 Advanced SIMD
		// vrshrn<c>.i<size> <Dd>, <Qm>, #<imm>
		// must precede thumb2_vqrshr in table
		new OpcodeARM(Index.thumb2_vrshrn, "vrshrn", "111011111xxxxxxxxxxx100001x1xxxx"),
		// A8.6.365 VQRSHRN, VQRSHRUN
		// Encoding T1 / A1 Advanced SIMD
		// vqrshr{u}n<c>.<type><size> <Dd>,<Qm>,#<imm>
		// must follow thumb2_vrshrn in table
		new OpcodeARM(Index.thumb2_vqrshr, "vqrshr", "111x11111xxxxxxxxxxx100x01x1xxxx"),
		// A8.6.366 VQSHL (register)
		// Encoding T1 / A1 Advanced SIMD
		// vqshl<c>.<type><size> <Qd>,<Qm>,<Qn>	vqshl<c>.<type><size> <Dd>,<Dm>,<Dn>
		new OpcodeARM(Index.thumb2_vqshl__reg, "vqshl", "111x11110xxxxxxxxxxx0100xxx1xxxx"),
		// A8.6.367 VQSHL, VQSHLU (immediate)
		// Encoding T1 / A1 Advanced SIMD
		// vqshl{u}<c>.<type><size> <Qd>,<Qm>,#<imm>	vqshl{u}<c>.<type><size> <Dd>,<Dm>,#<imm>
		new OpcodeARM(Index.thumb2_vqshl__imm, "vqshl", "111x11111xxxxxxxxxxx011xxxx1xxxx"),
		// A8.6.386 VSHRN
		// Encoding T1 / A1 Advanced SIMD
		// vshrn<c>.i<size> <Dd>, <Qm>, #<imm>
		// must precede thumb2_vqshr in table
		new OpcodeARM(Index.thumb2_vshrn, "vshrn", "111011111xxxxxxxxxxx100000x1xxxx"),
		// A8.6.368 VQSHRN, VQSHRUN
		// Encoding T1 / A1 Advanced SIMD
		// vqshr{u}n<c>.<type><size> <Dd>,<Qm>,#<imm>
		// must follow thumb2_vshrn in table
		new OpcodeARM(Index.thumb2_vqshr, "vqshr", "111x11111xxxxxxxxxxx100x00x1xxxx"),
		// A8.6.369 VQSUB
		// Encoding T1 / A1 Advanced SIMD
		// vqsub<c>.<type><size> <Qd>, <Qn>, <Qm>	vqsub<c>.<type><size> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vqsub, "vqsub", "111x11110xxxxxxxxxxx0010xxx1xxxx"),
		// A8.6.371 VRECPE
		// Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vrecpe<c>.<dt> <Qd>, <Qm>	vrecpe<c>.<dt> <Dd>, <Dm>
		// must precede thumb2_vraddhn in table
		new OpcodeARM(Index.thumb2_vrecpe, "vrecpe", "111111111x11xx11xxxx010x0xx0xxxx"),
		// A8.6.378 VRSQRTE
		// Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// vrsqrte<c>.<dt> <Qd>, <Qm>	vrsqrte<c>.<dt> <Dd>, <Dm>
		// must precede thumb2_vabal in table
		// must precede thumb2_vraddhn in table
		new OpcodeARM(Index.thumb2_vrsqrte, "vrsqrte", "111111111x11xx11xxxx010x1xx0xxxx"),
		// A8.6.370 VRADDHN
		// Encoding T1 / A1 Advanced SIMD
		// vraddhn<c>.<dt> <Dd>, <Qn>, <Qm>
		// must follow thumb2_vrecpe in table
		// must follow thumb2_vrsqrte in table
		new OpcodeARM(Index.thumb2_vraddhn, "vraddhn", "111111111xxxxxxxxxxx0100x0x0xxxx"),
		// A8.6.372 VRECPS
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vrecps<c>.f32 <Qd>, <Qn>, <Qm>	vrecps<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vrecps, "vrecps", "111011110x0xxxxxxxxx1111xxx1xxxx"),
		// A8.6.373 VREV16, VREV32, VREV64
		// Encoding T1 / A1 Advanced SIMD
		// vrev<n><c>.<size> <Qd>, <Qm>	vrev<n><c>.<size> <Dd>, <Dm>
		new OpcodeARM(Index.thumb2_vrev, "vrev", "111111111x11xx00xxxx000xxxx0xxxx"),
		// A8.6.374 VRHADD
		// Encoding T1 / A1 Advanced SIMD
		// vrhadd<c> <Qd>, <Qn>, <Qm>	vrhadd<c> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vrhadd, "vrhadd", "111x11110xxxxxxxxxxx0001xxx0xxxx"),
		// A8.6.375 VRSHL
		// Encoding T1 / A1 Advanced SIMD
		// vrshl<c>.<type><size> <Qd>, <Qm>, <Qn>	vrshl<c>.<type><size> <Dd>, <Dm>, <Dn>
		new OpcodeARM(Index.thumb2_vrshl, "vrshl", "111x11110xxxxxxxxxxx0101xxx0xxxx"),
		// A8.6.376 VRSHR
		// Encoding T1 / A1 Advanced SIMD
		// vrshr<c>.<type><size> <Qd>, <Qm>, #<imm>	vrshr<c>.<type><size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.thumb2_vrshr, "vrshr", "111x11111xxxxxxxxxxx0010xxx1xxxx"),
		// A8.6.266 VABA, VABAL
		// Encoding T2 / A2 Advanced SIMD
		// vabal<c>.<dt> <Qd>, <Dn>, <Dm>
		// must follow thumb2_vceq__imm0 in table
		// must follow thumb2_vrsqrte in table
		new OpcodeARM(Index.thumb2_vabal, "vabal", "111x11111xxxxxxxxxxx0101x0x0xxxx"),
		// A8.6.379 VRSQRTS
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vrsqrts<c>.f32 <Qd>, <Qn>, <Qm>	vrsqrts<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vrsqrts, "vrsqrts", "111011110x1xxxxxxxxx1111xxx1xxxx"),
		// A8.6.380 VRSRA
		// Encoding T1 / A1 Advanced SIMD
		// vrsra<c>.<type><size> <Qd>, <Qm>, #<imm>	vrsra<c>.<type><size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.thumb2_vrsra, "vrsra", "111x11111xxxxxxxxxxx0011xxx1xxxx"),
		// A8.6.381 VRSUBHN
		// Encoding T1 / A1 Advanced SIMD
		// vrsubhn<c>.<dt> <Dd>, <Qn>, <Qm>
		new OpcodeARM(Index.thumb2_vrsubhn, "vrsubhn", "111111111xxxxxxxxxxx0110x0x0xxxx"),
		// A8.6.382 VSHL (immediate)
		// Encoding T1 / A1 Advanced SIMD
		// vshl<c>.i<size> <Qd>, <Qm>, #<imm>	vshl<c>.i<size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.thumb2_vshl__imm, "vshl.i", "111011111xxxxxxxxxxx0101xxx1xxxx"),
		// A8.6.383 VSHL (register)
		// Encoding T1 / A1 Advanced SIMD
		// vshl<c>.i<size> <Qd>, <Qm>, <Qn>	vshl<c>.i<size> <Dd>, <Dm>, <Dn>
		new OpcodeARM(Index.thumb2_vshl__reg, "vshl", "111x11110xxxxxxxxxxx0100xxx0xxxx"),
		// A8.6.384 VSHLL
		// Encoding T2 / A2 Advanced SIMD
		// vshll<c>.<type><size> <Qd>, <Dm>, #<imm> (<imm> == <size>)
		new OpcodeARM(Index.thumb2_vshll__max, "vshll", "111111111x11xx10xxxx001100x0xxxx"),
		// A8.6.385 VSHR
		// Encoding T1 / A1 Advanced SIMD
		// vshr<c>.<type><size> <Qd>, <Qm>, #<imm>	vshr<c>.<type><size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.thumb2_vshr, "vshr", "111x11111xxxxxxxxxxx0000xxx1xxxx"),
		// A8.6.387 VSLI
		// Encoding T1 / A1 Advanced SIMD
		// vsli<c>.<size> <Qd>, <Qm>, #<imm>	vsli<c>.<size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.thumb2_vsli, "vsli.", "111111111xxxxxxxxxxx0101xxx1xxxx"),
		// A8.6.388 VSQRT
		// Encoding T1 / A1 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vsqrt<c>.f64 <Dd>, <Dm>	vsqrt<c>.f32 <Sd>, <Sm>
		new OpcodeARM(Index.thumb2_vsqrt, "vsqrt", "111011101x110001xxxx101x11x0xxxx"),
		// A8.6.389 VSRA
		// Encoding T1 / A1 Advanced SIMD
		// vsra<c>.<type><size> <Qd>, <Qm>, #<imm>	vsra<c>.<type><size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.thumb2_vsra, "vsra", "111x11111xxxxxxxxxxx0001xxx1xxxx"),
		// A8.6.390 VSRI
		// Encoding T1 / A1 Advanced SIMD
		// vsri<c>.<size> <Qd>, <Qm>, #<imm>	vsri<c>.<size> <Dd>, <Dm>, #<imm>
		new OpcodeARM(Index.thumb2_vsri, "vsri.", "111111111xxxxxxxxxxx0100xxx1xxxx"),
		// A8.6.400 VSTR
		// Encoding T1 / A1 VFPv2, VFPv3, Advanced SIMD
		// vstr<c> <Dd>, [<Rn>{, #+/-<imm>}]
		// must precede thumb2_vstm_1 in table
		new OpcodeARM(Index.thumb2_vstr__64, "vstr", "11101101xx00xxxxxxxx1011xxxxxxxx"),
		// A8.6.399 VSTM
		// Encoding T1 / A1 VFPv2, VFPv3, Advanced SIMD
		// vstm{mode}<c> <Rn>{!}, <list> <list> is consecutive 64-bit registers
		// must follow thumb2_vstr_1 in table
		new OpcodeARM(Index.thumb2_vstm__64, "vstm", "1110110xxxx0xxxxxxxx1011xxxxxxxx"),
		// A8.6.400 VSTR
		// Encoding T2 / A2 VFPv2, VFPv3
		// vstr<c> <Sd>, [<Rn>{, #+/-<imm>}]
		// must precede thumb2_vstm_2 in table
		new OpcodeARM(Index.thumb2_vstr__32, "vstr", "11101101xx00xxxxxxxx1010xxxxxxxx"),
		// A8.6.399 VSTM
		// Encoding T2 / A2 VFPv2, VFPv3
		// vstm{mode}<c> <Rn>{!}, <list> <list> is consecutive 32-bit registers
		// must follow thumb2_vstr_1 in table
		new OpcodeARM(Index.thumb2_vstm__32, "vstm", "1110110xxxx0xxxxxxxx1010xxxxxxxx"),
		// A8.6.401 VSUB (integer)
		// Encoding T1 / A1 Advanced SIMD
		// vsub<c>.<dt> <Qd>, <Qn>, <Qm>	vsub<c>.<dt> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vsub__int, "vsub", "111111110xxxxxxxxxxx1000xxx0xxxx"),
		// A8.6.402 VSUB (floating-point)
		// Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// vsub<c>.f32 <Qd>, <Qn>, <Qm>	vsub<c>.f32 <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vsub__f32, "vsub", "111011110x1xxxxxxxxx1101xxx0xxxx"),
		// A8.6.402 VSUB (floating-point)
		// Encoding T2 / A2 VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// vsub<c>.f64 <Dd>, <Dn>, <Dm>	vsub<c>.f32 <Sd>, <Sn>, <Sm>
		new OpcodeARM(Index.thumb2_vsub__fp_f, "vsub", "111011100x11xxxxxxxx101xx1x0xxxx"),
		// A8.6.403 VSUBHN
		// Encoding T1 / A1 Advanced SIMD
		// vsubhn<c>.<dt> <Dd>, <Qn>, <Qm>
		new OpcodeARM(Index.thumb2_vsubhn, "vsubhn", "111011111xxxxxxxxxxx0110x0x0xxxx"),
		// A8.6.404 VSUBL, VSUBW
		// Encoding T1 / A1 Advanced SIMD
		// vsubl<c>.<dt> <Qd>, <Dn>, <Dm>	vsubw<c>.<dt> {<Qd>,} <Qn>, <Dm>
		new OpcodeARM(Index.thumb2_vsubl_vsubw, "vsub", "111x11111xxxxxxxxxxx001xx0x0xxxx"),
		// A8.6.405 VSWP
		// Encoding T1 / A1 Advanced SIMD
		// vswp<c> <Qd>, <Qm>	vswp<c> <Dd>, <Dm>
		new OpcodeARM(Index.thumb2_vswp, "vswp", "111111111x11xx10xxxx00000xx0xxxx"),
		// A8.6.407 VTRN
		// Encoding T1 / A1 Advanced SIMD
		// vtrn<c>.<size> <Qd>, <Qm>	vtrn<c>.<size> <Dd>, <Dm>
		// must precede thumb2_vaddl_vaddw in table
		// must precede thumb2_vml__scalar in table
		new OpcodeARM(Index.thumb2_vtrn, "vtrn", "111111111x11xx10xxxx00001xx0xxxx"),
		// A8.6.408 VTST
		// Encoding T1 / A1 Advanced SIMD
		// vtst<c>.<size> <Qd>, <Qn>, <Qm>	vtst<c>.<size> <Dd>, <Dn>, <Dm>
		new OpcodeARM(Index.thumb2_vtst, "vtst", "111011110xxxxxxxxxxx1000xxx1xxxx"),
		// A8.6.409 VUZP
		// Encoding T1 / A1 Advanced SIMD
		// vuzp<c>.<size> <Qd>, <Qm>	vuzp<c>.<size> <Dd>, <Dm>
		// must precede thumb2_vaddl_vaddw in table
		// must precede thumb2_vml__scalar in table
		new OpcodeARM(Index.thumb2_vuzp, "vuzp", "111111111x11xx10xxxx00010xx0xxxx"),
		// A8.6.410 VZIP
		// Encoding T1 / A1 Advanced SIMD
		// vzip<c>.<size> <Qd>, <Qm>	vzip<c>.<size> <Dd>, <Dm>
		// must precede thumb2_vaddl_vaddw in table
		// must precede thumb2_vml__scalar in table
		new OpcodeARM(Index.thumb2_vzip, "vzip", "111111111x11xx10xxxx00011xx0xxxx"),
		// A8.6.325 VMLA, VMLAL, VMLS, VMLSL (by scalar)
		// Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// v<op><c>.<dt> <Qd>, <Qn>, <Dm[x]>	v<op><c>.<dt> <Dd>, <Dn>, <Dm[x]>
		// v<op>l<c>.<dt> <Qd>, <Dn>, <Dm[x]>
		// must follow thumb2_vtrn in table
		// must follow thumb2_vuzp in table
		// must follow thumb2_vzip in table
		new OpcodeARM(Index.thumb2_vml__scalar, "vml", "111x11111xxxxxxxxxxx0xxxx1x0xxxx"),
		// A8.6.274 VADDL, VADDW
		// Encoding T1 / A1 Advanced SIMD
		// vaddl<c>.<dt> <Qd>, <Dn>, <Dm>	vaddw<c>.<dt> <Qd>, <Qn>, <Dm>
		// must follow thumb2_vceq__imm0 in table
		// must follow thumb2_vcge__imm0 in table
		// must follow thumb2_vcgt__imm0 in table
		// must follow thumb2_vcle in table
		// must follow thumb2_vtrn in table
		// must follow thumb2_vuzp in table
		// must follow thumb2_vzip in table
		new OpcodeARM(Index.thumb2_vaddl_vaddw, "vadd", "111x11111xxxxxxxxxxx000xx0x0xxxx"),

		// Coprocessor Data Processing instructions
		// must follow VFP data processing instructions in table
		// (VMLA, VMLS, VNMLA, VNMLS, VNMUL, VMUL, VDIV, VMOV, VABS, VNEG
		//  VSQRT, VCVTB, VCVIT, VCMP, VCMPE, VCVT, VCVTR)

		// A8.6.28 CDP, CDP2
		// Encoding T1 / A1 ARMv6T2, ARMv7
		// cdp<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>
		new OpcodeARM(Index.thumb2_cdp, "cdp", "11101110xxxxxxxxxxxxxxxxxxx0xxxx"),
		// A8.6.28 CDP, CDP2
		// Encoding T2 /A2 ARMv6T2, ARMv7
		// cdp2<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>
		new OpcodeARM(Index.thumb2_cdp2, "cdp2", "11111110xxxxxxxxxxxxxxxxxxx0xxxx"),

		// must follow VMOV 64-bit transfer between ARM core and extension registers in table

		// A8.6.92 MCR, MCR2
		// Encoding T1 / A1 ARMv6T2, ARMv7
		// mcr<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// A8.6.92 MCR, MCR2
		// Encoding T2 / A2 ARMv6T2, ARMv7
		// mcr2<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		//
		new OpcodeARM(Index.thumb2_mcr, "mcr", "111x1110xxx0xxxxxxxxxxxxxxx1xxxx"),
		// A8.6.93 MCRR, MCRR2
		// Encoding T1 / A1 ARMv6T2, ARMv7
		// mcrr<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
		// A8.6.93 MCRR, MCRR2
		// Encoding T2 / A2 ARMv6T2, ARMv7
		// mcrr2<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
		// must precede thumb2_stc in table
		//
		new OpcodeARM(Index.thumb2_mcrr, "mcrr", "111x11000100xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.188 STC, STC2
		// Encoding T1 / A1 ARMv6T2, ARMv7
		// stc{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	stc{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>
		// stc{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// A8.6.188 STC, STC2
		// Encoding T2 / A2 ARMv6T2, ARMv7
		// stc2{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	stc2{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>
		// stc2{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		//
		// must follow thumb2_mcrr in table
		// must follow thumb2_mcrr2 in table
		// must follow thumb2_vstm__32 in table
		new OpcodeARM(Index.thumb2_stc, "stc", "111x110xxxx0xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.100 MRC, MRC2
		// Encoding T1 / A1 ARMv6T2, ARMv7
		// mrc<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// A8.6.100 MRC, MRC2
		// Encoding T2 / A2 ARMv6T2, ARMv7
		// mrc2<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		//
		new OpcodeARM(Index.thumb2_mrc, "mrc", "111x1110xxx1xxxxxxxxxxxxxxx1xxxx"),
		// A8.6.101 MRRC, MRRC2
		// Encoding T1 / A1 ARMv6T2, ARMv7
		// mrrc<c> <coproc>,<opc>,<Rt>,<Rt2>,<CRm>
		// A8.6.101 MRRC, MRRC2
		// Encoding T2 / A2 ARMv6T2, ARMv7
		// mrrc2<c> <coproc>,<opc>,<Rt>,<Rt2>,<CRm>
		//
		// must precede thumb2_ldc in table
		new OpcodeARM(Index.thumb2_mrrc, "mrrc", "111x11000101xxxxxxxxxxxxxxxxxxxx"),
		// A8.6.51 LDC, LDC2 (immediate)
		// Encoding T1 / A1 ARMv6T2, ARMv7
		// ldc{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	ldc{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>
		// ldc{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// A8.6.52 LDC, LDC2 (literal)
		// Encoding T1 /A1 (cond) ARMv6T2, ARMv7
		// ldc{l}<c> <coproc>,<CRd>,<label>	ldc{l}<c> <coproc>,<CRd>,[pc,#-0]
		// Special case	ldc{l}<c> <coproc>,<CRd>,[pc],<option>
		// A8.6.52 LDC, LDC2 (literal)
		// Encoding T2 / A2 ARMv6T2, ARMv7
		// ldc2{l}<c> <coproc>,<CRd>,<label>	ldc2{l}<c> <coproc>,<CRd>,[pc,#-0]
		// Special case	ldc2{l}<c> <coproc>,<CRd>,[pc],<option>
		// A8.6.51 LDC, LDC2 (immediate)
		// Encoding T2 / A1 ARMv6T2, ARMv7
		// ldc2{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	ldc2{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>
		// ldc2{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		//
		// must follow thumb_mrrc in table
		new OpcodeARM(Index.thumb2_ldc, "ldc", "111x110xxxx1xxxxxxxxxxxxxxxxxxxx"),
		// B6.1.8 RFE
		// Encoding T1 ARMv6T2, ARMv7
		// rfedb<c> <Rn>{!} Outside or last in IT block
		// Unpredictable if (1) is 0 or (0) is 1: 1110100000x1xxxx(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)
		// B6.1.8 RFE
		// Encoding T2 ARMv6T2, ARMv7
		// rfe{ia}<c> <Rn>{!} Outside or last in IT block
		// Unpredictable if (1) is 0 or (0) is 1: 1110100110x1xxxx(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)
		//
		new OpcodeARM(Index.thumb2_rfe, "rfe", "1110100xx0x1xxxxxxxxxxxxxxxxxxxx"),
		// B6.1.10 SRS
		// Encoding T1 ARMv6T2, ARMv7
		// srsdb<c> sp{!},#<mode>
		// Unpredictable if (1) is 0 or (0) is 1: 1110100000x0(1)(1)(0)(1)(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)(0)xxxxx
		// B6.1.10 SRS
		// Encoding T2 ARMv6T2, ARMv7
		// srs{ia}<c> sp{!},#<mode>
		// Unpredictable if (1) is 0 or (0) is 1: 1110100110x0(1)(1)(0)(1)(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)(0)xxxxx
		//
		new OpcodeARM(Index.thumb2_srs, "srs", "1110100xx0x0xxxxxxxxxxxxxxxxxxxx"),
		// B6.1.13 SUBS PC, LR and related instructions
		// Encoding T1 ARMv6T2, ARMv7
		// subs<c> pc,lr,#<imm8> Outside or last in IT block
		// Unpredictable if (1) is 0 or (0) is 1: 111100111101(1)(1)(1)(0)10(0)0(1)(1)(1)(1)xxxxxxxx
		new OpcodeARM(Index.thumb2_subs, "subs", "111100111101xxxx10x0xxxxxxxxxxxx"),
	};

	 // Reference manual citations (e.g., "A9.4.1") refer to sections in the ARM Architecture
	 // Reference Manual ARMv7-A and ARMv7-R Edition, Errata markup
	public static final OpcodeARM thumbEE_opcode_table[] = {
		// A9.4.1 LDR (register)
		// Encoding T1 ThumbEE
		// ldr<c> <Rt>,[<Rn>,<Rm>, lsl #2]
		// 0 1 0 1 1 0 0 Rm Rn Rt
		new OpcodeARM(Index.thumbEE_ldr_1, "ldr", "0101100xxxxxxxxx"),
		// A9.4.2 LDRH (register)
		// Encoding T1 ThumbEE
		// ldrh<c> <Rt>,[<Rn>,<Rm>, lsl #1]
		// 0 1 0 1 1 0 1 Rm Rn Rt
		new OpcodeARM(Index.thumbEE_ldrh, "ldrh", "0101101xxxxxxxxx"),
		// A9.4.3 LDRSH (register)
		// Encoding T1 ThumbEE
		// ldrsh<c> <Rt>,[<Rn>,<Rm>, lsl #1]
		// 0 1 0 1 1 1 1 Rm Rn Rt
		new OpcodeARM(Index.thumbEE_ldrsh, "ldrsh", "0101111xxxxxxxxx"),
		// A9.4.4 STR (register)
		// Encoding T1 ThumbEE
		// str<c> <Rt>,[<Rn>,<Rm>, lsl #2]
		// 0 1 0 1 0 0 0 Rm Rn Rt
		new OpcodeARM(Index.thumbEE_str_1, "str", "0101000xxxxxxxxx"),
		// A9.4.5 STRH (register)
		// Encoding T1 ThumbEE
		// strh<c> <Rt>,[<Rn>,<Rm>, lsl #1]
		// 0 1 0 1 0 0 1 Rm Rn Rt
		new OpcodeARM(Index.thumbEE_strh, "strh", "0101001xxxxxxxxx"),
		// A9.5.1 CHKA
		// Encoding E1 ThumbEE
		// chka<c> <Rn>,<Rm>
		// 1 1 0 0 1 0 1 0 N Rm Rn
		new OpcodeARM(Index.thumbEE_chka, "chka", "11001010xxxxxxxx"),
		// A9.5.2 HB, HBL
		// Encoding E1 ThumbEE
		// hb{l}<c> #<HandlerID>
		// 1 1 0 0 0 0 1 L handler
		new OpcodeARM(Index.thumbEE_hb, "hb", "1100001xxxxxxxxx"),
		// A9.5.3 HBLP
		// Encoding E1 ThumbEE
		// hblp<c> #<imm>, #<HandlerID>
		// 1 1 0 0 0 1 imm5 handler
		new OpcodeARM(Index.thumbEE_hblp, "hblp", "110001xxxxxxxxxx"),
		// A9.5.4 HBP
		// Encoding E1 ThumbEE
		// hbp<c> #<imm>, #<HandlerID>
		// 1 1 0 0 0 0 0 0 imm3 handler
		new OpcodeARM(Index.thumbEE_hbp, "hbp", "11000000xxxxxxxx"),
		// A9.5.5 LDR (immediate)
		// Encoding E1 ThumbEE
		// ldr<c> <Rt>,[R9{, #<imm>}]
		// 1 1 0 0 1 1 0 imm6 Rt
		new OpcodeARM(Index.thumbEE_ldr_2, "ldr", "1100110xxxxxxxxx"),
		// A9.5.5 LDR (immediate)
		// Encoding E2 ThumbEE
		// ldr<c> <Rt>,[R10{, #<imm>}]
		// 1 1 0 0 1 0 1 1 imm5 Rt
		new OpcodeARM(Index.thumbEE_ldr_3, "ldr", "11001011xxxxxxxx"),
		// A9.5.5 LDR (immediate)
		// Encoding E3 ThumbEE
		// ldr<c> <Rt>,[<Rn>{, #-<imm>}]
		// 1 1 0 0 1 0 0 imm3 Rn Rt
		new OpcodeARM(Index.thumbEE_ldr_4, "ldr", "1100100xxxxxxxxx"),
		// A9.5.6 STR (immediate)
		// Encoding E1 ThumbEE
		// str<c> <Rt>, [R9, #<imm>]
		// 1 1 0 0 1 1 1 imm6 Rt
		new OpcodeARM(Index.thumbEE_str_2, "str", "1100111xxxxxxxxx"),
	};

	 // Reference manual citations (e.g., "A9.3.1") refer to sections in the ARM Architecture
	 // Reference Manual ARMv7-A and ARMv7-R Edition, Errata markup
	public static final OpcodeARM thumbEE_thumb2_opcode_table[] = {
		// A9.3.1 ENTERX, LEAVEX
		// Encoding T1 ThumbEE
		// enterx  Not permitted in IT block.	leavex  Not permitted in IT block.
		// Unpredictable if (1) is 0 or (0) is 1: 111100111011(1)(1)(1)(1)10(0)0(1)(1)(1)(1)000x(1)(1)(1)(1)
		new OpcodeARM(Index.thumb2_enterx_leavex, "enterx", "111100111011xxxx10x0xxxx000xxxxx"),
	};

	// private members
	private final Index index; // Opcode index
	private final String mnemonic; // Mnemonic
	private final String opcodePattern; // Opcode pattern of '1', '0', 'x'
	private int opcodeMask; // Mask of bits to check in opcode pattern
	private int opcodeResult; // expected result after applying masked bits

	/**
	 * ARM opcode constructor.
	 *
	 * @param index
	 *            opcode index
	 * @param mnemonic
	 *            mnemonic
	 * @param pattern
	 *            opcode pattern of '1', '0', 'x'
	 */
	public OpcodeARM(Index index, String mnemonic, String pattern) {
		this.index = index;
		this.opcodePattern = pattern;
		this.mnemonic = mnemonic;
		initOpcode();
	}

	/**
	 * Get the index of an opcode entry.
	 *
	 * @return opcode index
	 */
	public Index getIndex() {
		return index;
	}

	/**
	 * Get the mnemonic of an opcode entry.
	 *
	 * @return mnemonic
	 */
	public String getMnemonic() {
		return mnemonic;
	}

	/**
	 * Get the mask of bits to check for an opcode entry.
	 *
	 * @return mask of bits to check
	 */
	public int getOpcodeMask() {
		return opcodeMask;
	}

	/**
	 * Get the bit mask pattern of '1', '0', 'x' for an opcode entry.
	 *
	 * @return opcode pattern
	 */
	public String getOpcodePattern() {
		return opcodePattern;
	}

	/**
	 * Get the expected result after applying masked bits of an opcode entry.
	 *
	 * @return expected result after applying masked bits
	 */
	public int getOpcodeResult() {
		return opcodeResult;
	}

	/**
	 * Initialize an opcode entry by converting the opcode pattern into a bit
	 * mask and figure out the expected result after applying the mask.
	 */
	protected void initOpcode() {
		char[] pattern = opcodePattern.toCharArray();
		int size = opcodePattern.length();
		opcodeMask = opcodeResult = 0;
		for (int i = 0; i < size; i++) {
			opcodeMask = opcodeMask << 1;
			opcodeResult = opcodeResult << 1;
			switch (pattern[i]) {
			case '0':
				opcodeMask |= 1;
				opcodeResult |= 0;
				break;

			case '1':
				opcodeMask |= 1;
				opcodeResult |= 1;
				break;

			default: // case 'x'
				break;
			}
		}
	}

}
