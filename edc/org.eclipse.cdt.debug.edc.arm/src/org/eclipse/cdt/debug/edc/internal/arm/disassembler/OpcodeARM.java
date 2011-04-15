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
		arm_vmov_vbitwise, arm_vmov__imm, arm_vmov__reg, arm_vmov__reg_f,
		arm_vmov_5,	arm_vmov_6, arm_vmov_7, arm_vmov_8, arm_vmov_9,
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
		arm_vorr,
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
		thumb_add__imm, thumb_add__imm_to_sp, thumb_add__reg, thumb_add__reg_imm, thumb_add__reg_reg,
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
		thumb2_vcvt__fp_i_vec, thumb2_vcvt__fp_i_reg, thumb2_vcvt__fp_fix_vec, thumb2_vcvt__fp_fix_reg, thumb2_vcvt__dp_sp, thumb2_vcvt__hp_sp_vec, thumb2_vcvt__hp_sp_reg,
		thumb2_vdiv,
		thumb2_vdup__scalar, thumb2_vdup__reg,
		thumb2_vext,
		thumb2_vhadd_vhsub,
		thumb2_vld__multi,
		thumb2_vld__xlane,
		thumb2_vldm__64, thumb2_vldm__32,
		thumb2_vldr__64, thumb2_vldr__32,
		thumb2_vmax_vmin__int, thumb2_vmax_vmin__fp,
		thumb2_vml__int, thumb2_vml__int_long, thumb2_vml__f32, thumb2_vml__fp, thumb2_vml__scalar,
		thumb2_vmov_vbitwise, thumb2_vmov__imm, thumb2_vmov__reg, thumb2_vmov__reg_f, thumb2_vmov_5, thumb2_vmov_6, thumb2_vmov_7, thumb2_vmov_8, thumb2_vmov_9,
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
		thumb2_vorr__reg,
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


	public static final OpcodeARM arm_opcode_table[] = {
		// NEW - Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.67 LDRD (literal)
		// ldrd<c> <Rt>,<Rt2>,<label>	ldrd<c> <Rt>,<Rt2>,[pc,#-0] Special case
		// xxxx000(1)x1(0)01111xxxxxxxx1101xxxx
		// must precede arm_ldrd__imm in search table
		new OpcodeARM(Index.arm_ldrd__lit, "ldrd", "xxxx000xx1x01111xxxxxxxx1101xxxx"),
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.66 LDRD (immediate)
		// ldrd<c> <Rt>,<Rt2>,[<Rn>{,#+/-<imm8>}]	ldrd<c> <Rt>,<Rt2>,[<Rn>],#+/-<imm8>	ldrd<c> <Rt>,<Rt2>,[<Rn>,#+/-<imm8>]!
		// xxxx000xx1x0xxxxxxxxxxxx1101xxxx
		// must follow arm_ldrd__lit in search table
		new OpcodeARM(Index.arm_ldrd__imm, "ldrd", "xxxx000xx1x0xxxxxxxxxxxx1101xxxx"),
		// NEW - Encoding A1 ARMv6T2, ARMv7
		// A8.6.77 LDRHT
		// ldrht<c> <Rt>, [<Rn>] {, #+/-<imm8>}
		// xxxx0000x111xxxxxxxxxxxx1011xxxx
		// must precede arm_ldrh__imm in search table
		new OpcodeARM(Index.arm_ldrht__imm, "ldrht", "xxxx0000x111xxxxxxxxxxxx1011xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.75 LDRH (literal)
		// ldrh<c> <Rt>,<label>	ldrh<c> <Rt>,[pc,#-0] Special case
		// xxxx000(1)x1(0)11111xxxxxxxx1011xxxx
		// must precede arm_ldrh__imm in search table
		new OpcodeARM(Index.arm_ldrh__lit, "ldrh", "xxxx000xx1x11111xxxxxxxx1011xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.74 LDRH (immediate, ARM)
		// ldrh<c> <Rt>,[<Rn>{,#+/-<imm8>}]	ldrh<c> <Rt>,[<Rn>],#+/-<imm8>	ldrh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// xxxx000xx1x1xxxxxxxxxxxx1011xxxx
		// must follow arm_ldrh__lit in search table
		// must follow arm_ldrht__imm in search table
		new OpcodeARM(Index.arm_ldrh__imm, "ldrh", "xxxx000xx1x1xxxxxxxxxxxx1011xxxx"),
		// NEW - Encoding A1 ARMv6T2, ARMv7
		// A8.6.81 LDRSBT
		// ldrsbt<c> <Rt>, [<Rn>] {, #+/-<imm8>}
		// xxxx0000x111xxxxxxxxxxxx1101xxxx
		// must precede arm_ldrsb__imm in search table
		new OpcodeARM(Index.arm_ldrsbt__imm, "ldrsbt", "xxxx0000x111xxxxxxxxxxxx1101xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.79 LDRSB (literal)
		// ldrsb<c> <Rt>,<label>	ldrsb<c> <Rt>,[pc,#-0] Special case
		// xxxx000(1)x1(0)11111xxxxxxxx1101xxxx
		// must precede arm_ldrsb__imm in search table
		new OpcodeARM(Index.arm_ldrsb__lit, "ldrsb", "xxxx000xx1x11111xxxxxxxx1101xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.78 LDRSB (immediate)
		// ldrsb<c> <Rt>,[<Rn>{,#+/-<imm8>}]	ldrsb<c> <Rt>,[<Rn>],#+/-<imm8>	ldrsb<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// xxxx000xx1x1xxxxxxxxxxxx1101xxxx
		// must follow arm_ldrsb__lit in search table
		// must follow arm_ldrsbt__imm in search table
		new OpcodeARM(Index.arm_ldrsb__imm, "ldrsb", "xxxx000xx1x1xxxxxxxxxxxx1101xxxx"),
		// NEW - Encoding A1 ARMv6T2, ARMv7
		// A8.6.85 LDRSHT
		// ldrsht<c> <Rt>, [<Rn>] {, #+/-<imm8>}
		// xxxx0000x111xxxxxxxxxxxx1111xxxx
		// must precede arm_ldrsh__imm in search table
		new OpcodeARM(Index.arm_ldrsht__imm, "ldrsht", "xxxx0000x111xxxxxxxxxxxx1111xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.83 LDRSH (literal)
		// ldrsh<c> <Rt>,<label>	ldrsh<c> <Rt>,[pc,#-0] Special case
		// xxxx000(1)x1(0)11111xxxxxxxx1111xxxx
		// must precede arm_ldrsh__imm in search table
		new OpcodeARM(Index.arm_ldrsh__lit, "ldrsh", "xxxx000xx1x11111xxxxxxxx1111xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.82 LDRSH (immediate)
		// ldrsh<c> <Rt>,[<Rn>{,#+/-<imm8>}]	ldrsh<c> <Rt>,[<Rn>],#+/-<imm8>	ldrsh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// xxxx000xx1x1xxxxxxxxxxxx1111xxxx
		// must follow arm_ldrsh__lit in search table
		// must follow arm_ldrsht__imm in search table
		new OpcodeARM(Index.arm_ldrsh__imm, "ldrsh", "xxxx000xx1x1xxxxxxxxxxxx1111xxxx"),
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.200 STRD (immediate)
		// strd<c> <Rt>,<Rt2>,[<Rn>{,#+/-<imm8>}]	strd<c> <Rt>,<Rt2>,[<Rn>],#+/-<imm8>	strd<c> <Rt>,<Rt2>,[<Rn>,#+/-<imm8>]!
		// xxxx000xx1x0xxxxxxxxxxxx1111xxxx
		new OpcodeARM(Index.arm_strd__imm, "strd", "xxxx000xx1x0xxxxxxxxxxxx1111xxxx"),
		// NEW - Encoding A1 ARMv6T2, ARMv7
		// A8.6.209 STRHT
		// strht<c> <Rt>, [<Rn>] {, #+/-<imm8>}
		// xxxx0000x110xxxxxxxxxxxx1011xxxx
		// must precede arm_strh__imm in search table
		new OpcodeARM(Index.arm_strht__imm, "strht", "xxxx0000x110xxxxxxxxxxxx1011xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.207 STRH (immediate, ARM)
		// strh<c> <Rt>,[<Rn>{,#+/-<imm8>}]	strh<c> <Rt>,[<Rn>],#+/-<imm8>	strh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// xxxx000xx1x0xxxxxxxxxxxx1011xxxx
		// must follow arm_strht__imm in search table
		new OpcodeARM(Index.arm_strh__imm, "strh", "xxxx000xx1x0xxxxxxxxxxxx1011xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.1 ADC (immediate)
		// adc{s}<c> <Rd>,<Rn>,#<const>
		// xxxx0010101xxxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_adc__imm, "adc", "xxxx0010101xxxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.2 ADC (register)
		// adc{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// xxxx0000101xxxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.arm_adc__reg, "adc", "xxxx0000101xxxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.3 ADC (register-shifted register)
		// adc{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		// xxxx0000101xxxxxxxxxxxxx0xx1xxxx
		new OpcodeARM(Index.arm_adc__rsr, "adc", "xxxx0000101xxxxxxxxxxxxx0xx1xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.10 ADR
		// adr<c> <Rd>,<label>	add<c> <Rd>,pc,#<const>	Alternative form
		// xxxx001010001111xxxxxxxxxxxxxxxx
		// must precede arm_add__imm in search table
		new OpcodeARM(Index.arm_adr__higher, "add", "xxxx001010001111xxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.5 ADD (immediate, ARM)
		// add{s}<c> <Rd>,<Rn>,#<const>
		// xxxx0010100xxxxxxxxxxxxxxxxxxxxx
		// must follow arm_adr__higher in search table
		new OpcodeARM(Index.arm_add__imm, "add", "xxxx0010100xxxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.6 ADD (register)
		// add{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// xxxx0000100xxxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.arm_add__reg, "add", "xxxx0000100xxxxxxxxxxxxxxxx0xxxx"),
//		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
//		// A8.6.8 ADD (SP plus immediate)
//		// add{s}<c> <Rd>,sp,#<const>
//		// xxxx0010100x1101xxxxxxxxxxxxxxxx
// SEE arm_add__imm
//		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
//		// A8.6.9 ADD (SP plus register)
//		// add{s}<c> <Rd>,sp,<Rm>{,<shift>}
//		// xxxx0000100x1101xxxxxxxxxxx0xxxx
// SEE arm_add__reg
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.7 ADD (register-shifted register)
		// add{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		// xxxx0000100xxxxxxxxxxxxx0xx1xxxx
		new OpcodeARM(Index.arm_add__rsr, "add", "xxxx0000100xxxxxxxxxxxxx0xx1xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.11 AND (immediate)
		// and{s}<c> <Rd>,<Rn>,#<const>
		// xxxx0010000xxxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_and__imm, "and", "xxxx0010000xxxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.12 AND (register)
		// and{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// xxxx0000000xxxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.arm_and__reg, "and", "xxxx0000000xxxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.13 AND (register-shifted register)
		// and{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		// xxxx0000000xxxxxxxxxxxxx0xx1xxxx
		new OpcodeARM(Index.arm_and__rsr, "and", "xxxx0000000xxxxxxxxxxxxx0xx1xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.14 ASR (immediate)
		// asr{s}<c> <Rd>,<Rm>,#<imm>
		// xxxx0001101x(0)(0)(0)(0)xxxxxxxxx100xxxx
		new OpcodeARM(Index.arm_asr__imm, "asr", "xxxx0001101xxxxxxxxxxxxxx100xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.15 ASR (register)
		// asr{s}<c> <Rd>,<Rn>,<Rm>
		// xxxx0001101x(0)(0)(0)(0)xxxxxxxx0101xxxx
		new OpcodeARM(Index.arm_asr__reg, "asr", "xxxx0001101xxxxxxxxxxxxx0101xxxx"),
		// Encoding A2 ARMv5T*, ARMv6*, ARMv7
		// A8.6.23 BL, BLX (immediate)
		// blx <label>
		// 1111101xxxxxxxxxxxxxxxxxxxxxxxxx
		// must precede arm_b in search table
		new OpcodeARM(Index.arm_blx__imm, "blx", "1111101xxxxxxxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.16 B
		// b<c> <label>
		// xxxx1010xxxxxxxxxxxxxxxxxxxxxxxx
		//must follow arm_blx__imm in search table
		new OpcodeARM(Index.arm_b, "b", "xxxx1010xxxxxxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv6T2, ARMv7
		// A8.6.17 BFC
		// bfc<c> <Rd>,#<lsb>,#<width>
		// xxxx0111110xxxxxxxxxxxxxx0011111
		// must precede arm_bfi in search table
		new OpcodeARM(Index.arm_bfc, "bfc", "xxxx0111110xxxxxxxxxxxxxx0011111"),
		// NEW - Encoding A1 ARMv6T2, ARMv7
		// A8.6.18 BFI
		// bfi<c> <Rd>,<Rn>,#<lsb>,#<width>
		// xxxx0111110xxxxxxxxxxxxxx001xxxx
		// must follow arm_bfc in search table
		new OpcodeARM(Index.arm_bfi, "bfi", "xxxx0111110xxxxxxxxxxxxxx001xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.20 BIC (register)
		// bic{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// xxxx0001110xxxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.arm_bic__reg, "bic", "xxxx0001110xxxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.21 BIC (register-shifted register)
		// bic{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		// xxxx0001110xxxxxxxxxxxxx0xx1xxxx
		new OpcodeARM(Index.arm_bic__rsr, "bic", "xxxx0001110xxxxxxxxxxxxx0xx1xxxx"),
		// Encoding A1 ARMv5T*, ARMv6*, ARMv7
		// A8.6.22 BKPT
		// bkpt #<imm16>
		// xxxx00010010xxxxxxxxxxxx0111xxxx
		new OpcodeARM(Index.arm_bkpt, "bkpt", "xxxx00010010xxxxxxxxxxxx0111xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.23 BL, BLX (immediate)
		// bl<c> <label>
		// xxxx1011xxxxxxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_bl, "bl", "xxxx1011xxxxxxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv5T*, ARMv6*, ARMv7
		// A8.6.24 BLX (register)
		// blx<c> <Rm>
		// xxxx00010010(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)0011xxxx
		new OpcodeARM(Index.arm_blx__reg, "blx", "xxxx00010010xxxxxxxxxxxx0011xxxx"),
		// Encoding A1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.25 BX
		// bx<c> Rm
		// xxxx00010010(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)0001xxxx
		new OpcodeARM(Index.arm_bx, "bx", "xxxx00010010xxxxxxxxxxxx0001xxxx"),
		// Encoding A1 ARMv5TEJ, ARMv6*, ARMv7
		// A8.6.26 BXJ
		// bxj<c> <Rm>
		// xxxx00010010(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)(1)0010xxxx
		new OpcodeARM(Index.arm_bxj, "bxj", "xxxx00010010xxxxxxxxxxxx0010xxxx"),
		// NEW - Encoding A1 ARMv6K, ARMv7
		// A8.6.30 CLREX
		// clrex
		// 111101010111(1)(1)(1)(1)(1)(1)(1)(1)(0)(0)(0)(0)0001(1)(1)(1)(1)
		new OpcodeARM(Index.arm_clrex, "clrex", "111101010111xxxxxxxxxxxx0001xxxx"),
		// Encoding A1 ARMv5T*, ARMv6*, ARMv7
		// A8.6.31 CLZ
		// clz<c> <Rd>,<Rm>
		// xxxx00010110(1)(1)(1)(1)xxxx(1)(1)(1)(1)0001xxxx
		new OpcodeARM(Index.arm_clz, "clz", "xxxx00010110xxxxxxxxxxxx0001xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.33 CMN (register)
		// cmn<c> <Rn>,<Rm>{,<shift>}
		// xxxx00010111xxxx(0)(0)(0)(0)xxxxxxx0xxxx
		new OpcodeARM(Index.arm_cmn__reg, "cmn", "xxxx00010111xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.34 CMN (register-shifted register)
		// cmn<c> <Rn>,<Rm>,<type> <Rs>
		// xxxx00010111xxxx(0)(0)(0)(0)xxxx0xx1xxxx
		new OpcodeARM(Index.arm_cmn__rsr, "cmn", "xxxx00010111xxxxxxxxxxxx0xx1xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.36 CMP (register)
		// cmp<c> <Rn>,<Rm>{,<shift>}
		// xxxx00010101xxxx(0)(0)(0)(0)xxxxxxx0xxxx
		new OpcodeARM(Index.arm_cmp__reg, "cmp", "xxxx00010101xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.37 CMP (register-shifted register)
		// cmp<c> <Rn>,<Rm>,<type> <Rs>
		// xxxx00010101xxxx(0)(0)(0)(0)xxxx0xx1xxxx
		new OpcodeARM(Index.arm_cmp__rsr, "cmp", "xxxx00010101xxxxxxxxxxxx0xx1xxxx"),
		// NEW - Encoding A1 ARMv7 (executes as NOP in ARMv6Kand ARMv6T2)
		// A8.6.40 DBG
		// dbg<c> #<option>
		// xxxx001100100000(1)(1)(1)(1)(0)(0)(0)(0)1111xxxx
		new OpcodeARM(Index.arm_dbg, "dbg", "xxxx001100100000xxxxxxxx1111xxxx"),
		// NEW - Encoding A1 ARMv7
		// A8.6.41 DMB
		// dmb #<option>
		// 111101010111(1)(1)(1)(1)(1)(1)(1)(1)(0)(0)(0)(0)0101xxxx
		new OpcodeARM(Index.arm_dmb, "dmb", "111101010111xxxxxxxxxxxx0101xxxx"),
		// NEW - Encoding A1 ARMv7
		// A8.6.42 DSB
		// dsb #<option>
		// 111101010111(1)(1)(1)(1)(1)(1)(1)(1)(0)(0)(0)(0)0100xxxx
		new OpcodeARM(Index.arm_dsb, "dsb", "111101010111xxxxxxxxxxxx0100xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.44 EOR (immediate)
		// eor{s}<c> <Rd>,<Rn>,#<const>
		// xxxx0010001xxxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_eor__imm, "eor", "xxxx0010001xxxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.45 EOR (register)
		// eor{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// xxxx0000001xxxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.arm_eor__reg, "eor", "xxxx0000001xxxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.46 EOR (register-shifted register)
		// eor{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		// xxxx0000001xxxxxxxxxxxxx0xx1xxxx
		new OpcodeARM(Index.arm_eor__rsr, "eor", "xxxx0000001xxxxxxxxxxxxx0xx1xxxx"),
		// NEW - Encoding A1 ARMv7
		// A8.6.49 ISB
		// isb #<option>
		// 111101010111(1)(1)(1)(1)(1)(1)(1)(1)(0)(0)(0)(0)0110xxxx
		new OpcodeARM(Index.arm_isb, "isb", "111101010111xxxxxxxxxxxx0110xxxx"),
		// NEW - Encoding A1 ARMv6*, ARMv7
		// B6.1.8 RFE
		// rfe{<amode>} <Rn>{!}
		// 1111100xx0x1xxxx(0)(0)(0)(0)(1)(0)(1)(0)(0)(0)(0)(0)(0)(0)(0)(0)
		// must precede arm_ldm__exc_ret in search table
		new OpcodeARM(Index.arm_rfe, "rfe", "1111100xx0x1xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// B6.1.2 LDM (exception return)
		// ldm{<amode>}<c> <Rn>{!},<registers_with_pc>^
		// xxxx100xx1x1xxxx1xxxxxxxxxxxxxxx
		// must follow arm_rfe in search table
		new OpcodeARM(Index.arm_ldm__exc_ret, "ldm", "xxxx100xx1x1xxxx1xxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// B6.1.3 LDM (user registers)
		// ldm{<amode>}<c> <Rn>,<registers_without_pc>^
		// xxxx100xx1(0)1xxxx0xxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_ldm__user_reg, "ldm", "xxxx100xx1x1xxxx0xxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.122 POP
		// pop<c> <registers> <registers> contains more than one register
		// xxxx100010111101xxxxxxxxxxxxxxxx
		// must precede arm_ldm in search table
		new OpcodeARM(Index.arm_pop__regs, "pop", "xxxx100010111101xxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.53 LDM / LDMIA / LDMFD
		// ldm<c> <Rn>{!},<registers>
		// xxxx100010x1xxxxxxxxxxxxxxxxxxxx
		// must follow arm_pop__regs in search table
		new OpcodeARM(Index.arm_ldm, "ldm", "xxxx100010x1xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.54 LDMDA / LDMFA
		// ldmda<c> <Rn>{!},<registers>
		// xxxx100000x1xxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_ldmda, "ldmda", "xxxx100000x1xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.55 LDMDB / LDMEA
		// ldmdb<c> <Rn>{!},<registers>
		// xxxx100100x1xxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_ldmdb, "ldmdb", "xxxx100100x1xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.56 LDMIB / LDMED
		// ldmib<c> <Rn>{!},<registers>
		// xxxx100110x1xxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_ldmib, "ldmib", "xxxx100110x1xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.122 POP
		// pop<c> <registers> <registers> contains one register, <Rt>
		// xxxx010010011101xxxx000000000100
		// must precede arm_ldr__imm in search table
		new OpcodeARM(Index.arm_pop__reg, "pop", "xxxx010010011101xxxx000000000100"),
		// NEW - Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.118 PLD (literal)
		// pld <label>	pld [pc,#-0] Special case
		// 11110101x(1)011111(1)(1)(1)(1)xxxxxxxxxxxx
		// must precede arm_ldr__imm in search table
		// must precede arm_pld__imm in search table
		new OpcodeARM(Index.arm_pld__lit, "pld", "11110101xx011111xxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7 for PLD; ARMv7 with MP Extensions for PLDW
		// A8.6.117 PLD, PLDW (immediate)
		// pld{w} [<Rn>,#+/-<imm12>]
		// 11110101xx01xxxx(1)(1)(1)(1)xxxxxxxxxxxx
		// must follow arm_pld__lit in search table
		// must precede arm_ldr__imm in search table
		// must precede arm_ldrbt__imm in search table
		// must precede arm_ldrb__lit in search table
		// must precede arm_ldrb__imm in search table
		new OpcodeARM(Index.arm_pld__imm, "pld", "11110101xx01xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv7
		// A8.6.120 PLI (immediate, literal)
		// pli [<Rn>,#+/-<imm12>]	pli <label>	pli [pc,#-0] Special case
		// 11110100x101xxxx(1)(1)(1)(1)xxxxxxxxxxxx
		// must precede arm_ldr__imm in search table
		// must precede arm_ldrbt__imm in search table
		// must precede arm_ldrb__lit in search table
		// must precede arm_ldrb__imm in search table
		new OpcodeARM(Index.arm_pli__imm_lit, "pli", "11110100x101xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.86 LDRT
		// ldrt<c> <Rt>, [<Rn>] {, #+/-<imm12>}
		// xxxx0100x011xxxxxxxxxxxxxxxxxxxx
		// must precede arm_ldr__lit in search table
		// must follow arm_pld__lit in search table
		// must follow arm_pld__imm in search table
		// must follow arm_pli__imm_lit in search table
		new OpcodeARM(Index.arm_ldrt__imm, "ldrt", "xxxx0100x011xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.59 LDR (literal)
		// ldr<c> <Rt>,<label>	ldr<c> <Rt>,[pc,#-0] Special case
		// xxxx010(1)x0(0)11111xxxxxxxxxxxxxxxx
		// must precede arm_ldr__imm in search table
		// must follow arm_ldrt__imm i n search table
		// must follow arm_pld__lit in search table
		// must follow arm_pld__imm in search table
		// must follow arm_pli__imm_lit in search table
		new OpcodeARM(Index.arm_ldr__lit, "ldr", "xxxx010xx0x11111xxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.58 LDR (immediate, ARM)
		// ldr<c> <Rt>,[<Rn>{,#+/-<imm12>}]	ldr<c> <Rt>,[<Rn>],#+/-<imm12>	ldr<c> <Rt>,[<Rn>,#+/-<imm12>]!
		// xxxx010xx0x1xxxxxxxxxxxxxxxxxxxx
		// must follow arm_ldr__lit in search table
		// must follow arm_pop__reg in search table
		// must follow arm_pld__lit in search table
		// must follow arm_pld__imm in search table
		// must follow arm_pli__imm_lit in search table
		new OpcodeARM(Index.arm_ldr__imm, "ldr", "xxxx010xx0x1xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.86 LDRT
		// ldrt<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// xxxx0110x011xxxxxxxxxxxxxxx0xxxx
		// must precede arm_ldr__reg in search table
		new OpcodeARM(Index.arm_ldrt__reg, "ldrt", "xxxx0110x011xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv5TE*, ARMv6*, ARMv7 for PLD; ARMv7 with MP Extensions for PLDW
		// A8.6.119 PLD, PLDW (register)
		// pld{w}<c> [<Rn>,+/-<Rm>{, <shift>}]
		// 11110111xx01xxxx(1)(1)(1)(1)xxxxxxx0xxxx
		// must precede arm_ldrb__reg in search table
		// must precede arm_ldrt__reg in search table
		new OpcodeARM(Index.arm_pld__reg, "pld", "11110111xx01xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv7
		// A8.6.121 PLI (register)
		// pli [<Rn>,+/-<Rm>{, <shift>}]
		// 11110110x101xxxx(1)(1)(1)(1)xxxxxxx0xxxx
		// must precede arm_ldrb__reg in search table
		// must precede arm_ldrt__reg in search table
		new OpcodeARM(Index.arm_pli__reg, "pli", "11110110x101xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.60 LDR (register)
		// ldr<c> <Rt>,[<Rn>,+/-<Rm>{, <shift>}]{!}	ldr<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// xxxx011xx0x1xxxxxxxxxxxxxxx0xxxx
		// must follow arm_ldrt__reg in search table
		// must follow arm_pld__reg in search table
		// must follow arm_pli__reg in search table
		new OpcodeARM(Index.arm_ldr__reg, "ldr", "xxxx011xx0x1xxxxxxxxxxxxxxx0xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.65 LDRBT
		// ldrbt<c> <Rt>,[<Rn>],#+/-<imm12>
		// xxxx0100x111xxxxxxxxxxxxxxxxxxxx
		// must precede arm_ldrb__imm in search table
		// must follow arm_pld__imm in search table
		new OpcodeARM(Index.arm_ldrbt__imm, "ldrbt", "xxxx0100x111xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.63 LDRB (literal)
		// ldrb<c> <Rt>,<label>	ldrb<c> <Rt>,[pc,#-0] Special case
		// xxxx010(1)x1(0)11111xxxxxxxxxxxxxxxx
		// must precede arm_ldrb__imm in search table
		// must follow arm_pld__imm in search table
		new OpcodeARM(Index.arm_ldrb__lit, "ldrb", "xxxx010xx1x11111xxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.62 LDRB (immediate, ARM)
		// ldrb<c> <Rt>,[<Rn>{,#+/-<imm12>}]	ldrb<c> <Rt>,[<Rn>],#+/-<imm12>	ldrb<c> <Rt>,[<Rn>,#+/-<imm12>]!
		// xxxx010xx1x1xxxxxxxxxxxxxxxxxxxx
		// must follow arm_ldrb__lit in search table
		// must follow arm_ldrbt__imm in search table
		// must follow arm_pld__imm in search table
		new OpcodeARM(Index.arm_ldrb__imm, "ldrb", "xxxx010xx1x1xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.65 LDRBT
		// ldrbt<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// xxxx0110x111xxxxxxxxxxxxxxx0xxxx
		// must precede arm_ldrb__reg in search table
		new OpcodeARM(Index.arm_ldrbt__reg, "ldrbt", "xxxx0110x111xxxxxxxxxxxxxxx0xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.64 LDRB (register)
		// ldrb<c> <Rt>,[<Rn>,+/-<Rm>{, <shift>}]{!}	ldrb<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// xxxx011xx1x1xxxxxxxxxxxxxxx0xxxx
		// must follow arm_ldrbt__reg in search table
		// must follow arm_pld__reg in search table
		new OpcodeARM(Index.arm_ldrb__reg, "ldrb", "xxxx011xx1x1xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv6*, ARMv7
		// B6.1.1 CPS
		// cps<effect> <iflags>{,#<mode>}	cps #<mode>
		// 111100010000xxx0(0)(0)(0)(0)(0)(0)(0)xxx0xxxxx
		// must precede arm_mrs in search table
		// must precede arm_ldrd__reg in search table
		new OpcodeARM(Index.arm_cps, "cps", "111100010000xxx0xxxxxxxxxx0xxxxx"),
		// NEW - Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.68 LDRD (register)
		// ldrd<c> <Rt>,<Rt2>,[<Rn>,+/-<Rm>]{!}	ldrd<c> <Rt>,<Rt2>,[<Rn>],+/-<Rm>
		// xxxx000xx0x0xxxxxxxx(0)(0)(0)(0)1101xxxx
		//must follow arm_cps in search table
		new OpcodeARM(Index.arm_ldrd__reg, "ldrd", "xxxx000xx0x0xxxxxxxxxxxx1101xxxx"),
		// NEW - Encoding A1 ARMv6*, ARMv7
		// A8.6.69 LDREX
		// ldrex<c> <Rt>,[<Rn>]
		// xxxx00011001xxxxxxxx(1)(1)(1)(1)1001(1)(1)(1)(1)
		new OpcodeARM(Index.arm_ldrex, "ldrex", "xxxx00011001xxxxxxxxxxxx1001xxxx"),
		// NEW - Encoding A1 ARMv6K, ARMv7
		// A8.6.70 LDREXB
		// ldrexb<c> <Rt>, [<Rn>]
		// xxxx00011101xxxxxxxx(1)(1)(1)(1)1001(1)(1)(1)(1)
		new OpcodeARM(Index.arm_ldrexb, "ldrexb", "xxxx00011101xxxxxxxxxxxx1001xxxx"),
		// NEW - Encoding A1 ARMv6K, ARMv7
		// A8.6.71 LDREXD
		// ldrexd<c> <Rt>,<Rt2>,[<Rn>]
		// xxxx00011011xxxxxxxx(1)(1)(1)(1)1001(1)(1)(1)(1)
		new OpcodeARM(Index.arm_ldrexd, "ldrexd", "xxxx00011011xxxxxxxxxxxx1001xxxx"),
		// NEW - Encoding A1 ARMv6K, ARMv7
		// A8.6.72 LDREXH
		// ldrexh<c> <Rt>, [<Rn>]
		// xxxx00011111xxxxxxxx(1)(1)(1)(1)1001(1)(1)(1)(1)
		new OpcodeARM(Index.arm_ldrexh, "ldrexh", "xxxx00011111xxxxxxxxxxxx1001xxxx"),
		// NEW - Encoding A2 ARMv6T2, ARMv7
		// A8.6.77 LDRHT
		// ldrht<c> <Rt>, [<Rn>], +/-<Rm>
		// xxxx0000x011xxxxxxxx(0)(0)(0)(0)1011xxxx
		// must precede arm_ldrh__reg in search table
		new OpcodeARM(Index.arm_ldrht__reg, "ldrht", "xxxx0000x011xxxxxxxxxxxx1011xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.76 LDRH (register)
		// ldrh<c> <Rt>,[<Rn>,+/-<Rm>]{!}	ldrh<c> <Rt>,[<Rn>],+/-<Rm>
		// xxxx000xx0x1xxxxxxxx(0)(0)(0)(0)1011xxxx
		// must follow arm_ldrht__reg in search table
		new OpcodeARM(Index.arm_ldrh__reg, "ldrh", "xxxx000xx0x1xxxxxxxxxxxx1011xxxx"),
		// NEW - Encoding A2 ARMv6T2, ARMv7
		// A8.6.81 LDRSBT
		// ldrsbt<c> <Rt>, [<Rn>], +/-<Rm>
		// xxxx0000x011xxxxxxxx(0)(0)(0)(0)1101xxxx
		// must precede arm_ldrsb__reg in search table
		new OpcodeARM(Index.arm_ldrsbt__reg, "ldrsbt", "xxxx0000x011xxxxxxxxxxxx1101xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.80 LDRSB (register)
		// ldrsb<c> <Rt>,[<Rn>,+/-<Rm>]{!}	ldrsb<c> <Rt>,[<Rn>],+/-<Rm>
		// xxxx000xx0x1xxxxxxxx(0)(0)(0)(0)1101xxxx
		// must follow arm_ldrsbt__reg in search table
		new OpcodeARM(Index.arm_ldrsb__reg, "ldrsb", "xxxx000xx0x1xxxxxxxxxxxx1101xxxx"),
		// NEW - Encoding A2 ARMv6T2, ARMv7
		// A8.6.85 LDRSHT
		// ldrsht<c> <Rt>, [<Rn>], +/-<Rm>
		// xxxx0000x011xxxxxxxx(0)(0)(0)(0)1111xxxx
		// must precede arm_ldrsh__reg in search table
		new OpcodeARM(Index.arm_ldrsht__reg, "ldrsht", "xxxx0000x011xxxxxxxxxxxx1111xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.84 LDRSH (register)
		// ldrsh<c> <Rt>,[<Rn>,+/-<Rm>]{!}	ldrsh<c> <Rt>,[<Rn>],+/-<Rm>
		// xxxx000xx0x1xxxxxxxx(0)(0)(0)(0)1111xxxx
		// must follow arm_ldrsht__reg in search table
		new OpcodeARM(Index.arm_ldrsh__reg, "ldrsh", "xxxx000xx0x1xxxxxxxxxxxx1111xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.89 LSL (register)
		// lsl{s}<c> <Rd>,<Rn>,<Rm>
		// xxxx0001101x(0)(0)(0)(0)xxxxxxxx0001xxxx
		new OpcodeARM(Index.arm_lsl__reg, "lsl", "xxxx0001101xxxxxxxxxxxxx0001xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.90 LSR (immediate)
		// lsr{s}<c> <Rd>,<Rm>,#<imm>
		// xxxx0001101x(0)(0)(0)(0)xxxxxxxxx010xxxx
		new OpcodeARM(Index.arm_lsr__imm, "lsr", "xxxx0001101xxxxxxxxxxxxxx010xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.91 LSR (register)
		// lsr{s}<c> <Rd>,<Rn>,<Rm>
		// xxxx0001101x(0)(0)(0)(0)xxxxxxxx0011xxxx
		new OpcodeARM(Index.arm_lsr__reg, "lsr", "xxxx0001101xxxxxxxxxxxxx0011xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.94 MLA
		// mla{s}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// xxxx0000001xxxxxxxxxxxxx1001xxxx
		new OpcodeARM(Index.arm_mla, "mla", "xxxx0000001xxxxxxxxxxxxx1001xxxx"),
		// NEW - Encoding A1 ARMv6T2, ARMv7
		// A8.6.95 MLS
		// mls<c> <Rd>,<Rn>,<Rm>,<Ra>
		// xxxx00000110xxxxxxxxxxxx1001xxxx
		new OpcodeARM(Index.arm_mls, "mls", "xxxx00000110xxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.96 MOV (immediate)
		// mov{s}<c> <Rd>,#<const>
		// xxxx0011101x(0)(0)(0)(0)xxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_mov__imm, "mov", "xxxx0011101xxxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.97 MOV (register)
		// mov{s}<c> <Rd>,<Rm>
		// xxxx0001101x(0)(0)(0)(0)xxxx00000000xxxx
		// must precede arm_lsl__imm in search table
		new OpcodeARM(Index.arm_mov__reg, "mov", "xxxx0001101xxxxxxxxx00000000xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.88 LSL (immediate)
		// lsl{s}<c> <Rd>,<Rm>,#<imm>
		// xxxx0001101x(0)(0)(0)(0)xxxxxxxxx000xxxx
		// must follow arm_mov__reg in search table
		new OpcodeARM(Index.arm_lsl__imm, "lsl", "xxxx0001101xxxxxxxxxxxxxx000xxxx"),
		// NEW - Encoding A2 ARMv6T2, ARMv7
		// A8.6.96 MOV (immediate)
		// movw<c> <Rd>,#<imm16>
		// xxxx00110000xxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_movw, "movw", "xxxx00110000xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.157 SETEND
		// setend <endian_specifier> Cannot be conditional
		// 111100010000(0)(0)(0)1(0)(0)(0)(0)(0)(0)x(0)0000(0)(0)(0)(0)
		//must precede arm_mrs in search table
		new OpcodeARM(Index.arm_setend, "setend", "111100010000xxx1xxxxxxxx0000xxxx"),
//		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
//		// B6.1.5 MRS
//		// mrs<c> <Rd>,<spec_reg>
//		// xxxx00010x00(1)(1)(1)(1)xxxx(0)(0)(0)(0)0000(0)(0)(0)(0)
// SEE arm_mrs
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.102 MRS
		// mrs<c> <Rd>,<spec_reg>
		// xxxx00010000(1)(1)(1)(1)xxxx(0)(0)(0)(0)0000(0)(0)(0)(0)
		// combined A8.6.102 MRS and B6.1.5 MRS, using the bit pattern of B6.1.5 MRS
		// must follow arm_cps in search table
		// must follow arm_setend in search table
		new OpcodeARM(Index.arm_mrs, "mrs", "xxxx00010x00xxxxxxxxxxxx0000xxxx"),
		// NEW - Encoding A1 ARMv6K, ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.158 SEV
		// sev<c>
		// xxxx001100100000(1)(1)(1)(1)(0)(0)(0)(0)00000100
		// must precede arm_msr__imm in search table
		new OpcodeARM(Index.arm_sev, "sev", "xxxx001100100000xxxxxxxx00000100"),
		// NEW - Encoding A1 ARMv6K, ARMv6T2, ARMv7
		// A8.6.110 NOP
		// nop<c>
		// xxxx001100100000(1)(1)(1)(1)(0)(0)(0)(0)00000000
		// must precede arm_msr__imm in search table
		new OpcodeARM(Index.arm_nop, "nop", "xxxx001100100000xxxxxxxx00000000"),
		// NEW - Encoding A1 ARMv6K, ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.411 WFE
		// wfe<c>
		// xxxx001100100000(1)(1)(1)(1)(0)(0)(0)(0)00000010
		// must precede arm_msr__imm in search table
		new OpcodeARM(Index.arm_wfe, "wfe", "xxxx001100100000xxxxxxxx00000010"),
		// NEW - Encoding A1 ARMv6K, ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.412 WFI
		// wfi<c>
		// xxxx001100100000(1)(1)(1)(1)(0)(0)(0)(0)00000011
		// must precede arm_msr__imm in search table
		new OpcodeARM(Index.arm_wfi, "wfi", "xxxx001100100000xxxxxxxx00000011"),
		// NEW - Encoding A1 ARMv6K, ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.413 YIELD
		// yield<c>
		// xxxx001100100000(1)(1)(1)(1)(0)(0)(0)(0)00000001
		// must precede arm_msr__imm in search table
		new OpcodeARM(Index.arm_yield, "yield", "xxxx001100100000xxxxxxxx00000001"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.104 MSR (register)
		// msr<c> <spec_reg>,<Rn>
		// xxxx00010010xx00(1)(1)(1)(1)(0)(0)(0)(0)0000xxxx
		// must precede arm_msr__sys_reg in search table
		new OpcodeARM(Index.arm_msr__reg, "msr", "xxxx00010010xx00xxxxxxxx0000xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// B6.1.7 MSR (register)
		// msr<c> <spec_reg>,<Rn>
		// xxxx00010x10xxxx(1)(1)(1)(1)(0)(0)(0)(0)0000xxxx
		// must follow arm_msr__reg in table
		new OpcodeARM(Index.arm_msr__sys_reg, "msr", "xxxx00010x10xxxxxxxxxxxx0000xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.105 MUL
		// mul{s}<c> <Rd>,<Rn>,<Rm>
		// xxxx0000000xxxxx(0)(0)(0)(0)xxxx1001xxxx
		new OpcodeARM(Index.arm_mul, "mul", "xxxx0000000xxxxxxxxxxxxx1001xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.107 MVN (register)
		// mvn{s}<c> <Rd>,<Rm>{,<shift>}
		// xxxx0001111x(0)(0)(0)(0)xxxxxxxxxxx0xxxx
		new OpcodeARM(Index.arm_mvn__reg, "mvn", "xxxx0001111xxxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.108 MVN (register-shifted register)
		// mvn{s}<c> <Rd>,<Rm>,<type> <Rs>
		// xxxx0001111x(0)(0)(0)(0)xxxxxxxx0xx1xxxx
		new OpcodeARM(Index.arm_mvn__rsr, "mvn", "xxxx0001111xxxxxxxxxxxxx0xx1xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.113 ORR (immediate)
		// orr{s}<c> <Rd>,<Rn>,#<const>
		// xxxx0011100xxxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_orr__imm, "orr", "xxxx0011100xxxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.114 ORR (register)
		// orr{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// xxxx0001100xxxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.arm_orr__reg, "orr", "xxxx0001100xxxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.115 ORR (register-shifted register)
		// orr{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		// xxxx0001100xxxxxxxxxxxxx0xx1xxxx
		new OpcodeARM(Index.arm_orr__rsr, "orr", "xxxx0001100xxxxxxxxxxxxx0xx1xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.116 PKH
		// pkhbt<c> <Rd>,<Rn>,<Rm>{,lsl #<imm>}	pkhtb<c> <Rd>,<Rn>,<Rm>{,asr #<imm>}
		// xxxx01101000xxxxxxxxxxxxxx01xxxx
		new OpcodeARM(Index.arm_pkh, "pkh", "xxxx01101000xxxxxxxxxxxxxx01xxxx"),
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.124 QADD
		// qadd<c> <Rd>,<Rm>,<Rn>
		// xxxx00010000xxxxxxxx(0)(0)(0)(0)0101xxxx
		new OpcodeARM(Index.arm_qadd, "qadd", "xxxx00010000xxxxxxxxxxxx0101xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.125 QADD16
		// qadd16<c> <Rd>,<Rn>,<Rm>
		// xxxx01100010xxxxxxxx(1)(1)(1)(1)0001xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_qadd16, "qadd16", "xxxx01100010xxxxxxxxxxxx0001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.126 QADD8
		// qadd8<c> <Rd>,<Rn>,<Rm>
		// xxxx01100010xxxxxxxx(1)(1)(1)(1)1001xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_qadd8, "qadd8", "xxxx01100010xxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.127 QASX
		// qasx<c> <Rd>,<Rn>,<Rm>
		// xxxx01100010xxxxxxxx(1)(1)(1)(1)0011xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_qasx, "qasx", "xxxx01100010xxxxxxxxxxxx0011xxxx"),
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.128 QDADD
		// qdadd<c> <Rd>,<Rm>,<Rn>
		// xxxx00010100xxxxxxxx(0)(0)(0)(0)0101xxxx
		new OpcodeARM(Index.arm_qdadd, "qdadd", "xxxx00010100xxxxxxxxxxxx0101xxxx"),
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.129 QDSUB
		// qdsub<c> <Rd>,<Rm>,<Rn>
		// xxxx00010110xxxxxxxx(0)(0)(0)(0)0101xxxx
		new OpcodeARM(Index.arm_qdsub, "qdsub", "xxxx00010110xxxxxxxxxxxx0101xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.130 QSAX
		// qsax<c> <Rd>,<Rn>,<Rm>
		// xxxx01100010xxxxxxxx(1)(1)(1)(1)0101xxxx
// see arm__r_dnm_math
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.131 QSUB
		// qsub<c> <Rd>,<Rm>,<Rn>
		// xxxx00010010xxxxxxxx(0)(0)(0)(0)0101xxxx
		new OpcodeARM(Index.arm_qsub, "qsub", "xxxx00010010xxxxxxxxxxxx0101xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.132 QSUB16
		// qsub16<c> <Rd>,<Rn>,<Rm>
		// xxxx01100010xxxxxxxx(1)(1)(1)(1)0111xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_qsub16, "qsub16", "xxxx01100010xxxxxxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.133 QSUB8
		// qsub8<c> <Rd>,<Rn>,<Rm>
		// xxxx01100010xxxxxxxx(1)(1)(1)(1)1111xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_qsub8, "qsub8", "xxxx01100010xxxxxxxxxxxx1111xxxx"),

		// {s|u|}{h|q|}{{add|sub}{8|16}|asx|sax}<c> <Rd>,<Rn>,<Rm>
		// cond_31_28 0 1 1 0 0 hqsu_22_20 Rn_19_16 Rd_15_12 (1)(1)(1)(1) op_7_5 1 Rm_3_0
		// xxxx01100010xxxxxxxx(1)(1)(1)(1)0001xxxx
		new OpcodeARM(Index.arm__r_dnm_math, null, "xxxx01100xxxxxxxxxxxxxxxxxx1xxxx"),

		// NEW - Encoding A1 ARMv6T2, ARMv7
		// A8.6.134 RBIT
		// rbit<c> <Rd>,<Rm>
		// xxxx01101111(1)(1)(1)(1)xxxx(1)(1)(1)(1)0011xxxx
		new OpcodeARM(Index.arm_rbit, "rbit", "xxxx01101111xxxxxxxxxxxx0011xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.135 REV
		// rev<c> <Rd>,<Rm>
		// xxxx01101011(1)(1)(1)(1)xxxx(1)(1)(1)(1)0011xxxx
		new OpcodeARM(Index.arm_rev, "rev", "xxxx01101011xxxxxxxxxxxx0011xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.136 REV16
		// rev16<c> <Rd>,<Rm>
		// xxxx01101011(1)(1)(1)(1)xxxx(1)(1)(1)(1)1011xxxx
		new OpcodeARM(Index.arm_rev16, "rev16", "xxxx01101011xxxxxxxxxxxx1011xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.137 REVSH
		// revsh<c> <Rd>,<Rm>
		// xxxx01101111(1)(1)(1)(1)xxxx(1)(1)(1)(1)1011xxxx
		new OpcodeARM(Index.arm_revsh, "revsh", "xxxx01101111xxxxxxxxxxxx1011xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.141 RRX
		// rrx{s}<c> <Rd>,<Rm>
		// xxxx0001101x(0)(0)(0)(0)xxxx00000110xxxx
		// must precede arm_ror__imm in search table
		new OpcodeARM(Index.arm_rrx, "rrx", "xxxx0001101xxxxxxxxx00000110xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.139 ROR (immediate)
		// ror{s}<c> <Rd>,<Rm>,#<imm>
		// xxxx0001101x(0)(0)(0)(0)xxxxxxxxx110xxxx
		// must follow arm_rrx in search table
		new OpcodeARM(Index.arm_ror__imm, "ror", "xxxx0001101xxxxxxxxxxxxxx110xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.140 ROR (register)
		// ror{s}<c> <Rd>,<Rn>,<Rm>
		// xxxx0001101x(0)(0)(0)(0)xxxxxxxx0111xxxx
		new OpcodeARM(Index.arm_ror__reg, "ror", "xxxx0001101xxxxxxxxxxxxx0111xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.143 RSB (register)
		// rsb{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// xxxx0000011xxxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.arm_rsb__reg, "rsb", "xxxx0000011xxxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.144 RSB (register-shifted register)
		// rsb{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		// xxxx0000011xxxxxxxxxxxxx0xx1xxxx
		new OpcodeARM(Index.arm_rsb__rsr, "rsb", "xxxx0000011xxxxxxxxxxxxx0xx1xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.146 RSC (register)
		// rsc{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// xxxx0000111xxxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.arm_rsc__reg, "rsc", "xxxx0000111xxxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.147 RSC (register-shifted register)
		// rsc{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		// xxxx0000111xxxxxxxxxxxxx0xx1xxxx
		new OpcodeARM(Index.arm_rsc__rsr, "rsc", "xxxx0000111xxxxxxxxxxxxx0xx1xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.148 SADD16
		// sadd16<c> <Rd>,<Rn>,<Rm>
		// xxxx01100001xxxxxxxx(1)(1)(1)(1)0001xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_sadd16, "sadd16", "xxxx01100001xxxxxxxxxxxx0001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.149 SADD8
		// sadd8<c> <Rd>,<Rn>,<Rm>
		// xxxx01100001xxxxxxxx(1)(1)(1)(1)1001xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_sadd8, "sadd8", "xxxx01100001xxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.150 SASX
		// sasx<c> <Rd>,<Rn>,<Rm>
		// xxxx01100001xxxxxxxx(1)(1)(1)(1)0011xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_sasx, "sasx", "xxxx01100001xxxxxxxxxxxx0011xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.152 SBC (register)
		// sbc{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// xxxx0000110xxxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.arm_sbc__reg, "sbc", "xxxx0000110xxxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.153 SBC (register-shifted register)
		// sbc{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		// xxxx0000110xxxxxxxxxxxxx0xx1xxxx
		new OpcodeARM(Index.arm_sbc__rsr, "sbc", "xxxx0000110xxxxxxxxxxxxx0xx1xxxx"),
		// NEW - Encoding A1 ARMv6T2, ARMv7
		// A8.6.154 SBFX
		// sbfx<c> <Rd>,<Rn>,#<lsb>,#<width>
		// xxxx0111101xxxxxxxxxxxxxx101xxxx
		new OpcodeARM(Index.arm_sbfx, "sbfx", "xxxx0111101xxxxxxxxxxxxxx101xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.156 SEL
		// sel<c> <Rd>,<Rn>,<Rm>
		// xxxx01101000xxxxxxxx(1)(1)(1)(1)1011xxxx
		new OpcodeARM(Index.arm_sel, "sel", "xxxx01101000xxxxxxxxxxxx1011xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.159 SHADD16
		// shadd16<c> <Rd>,<Rn>,<Rm>
		// xxxx01100011xxxxxxxx(1)(1)(1)(1)0001xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_shadd16, "shadd16", "xxxx01100011xxxxxxxxxxxx0001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.160 SHADD8
		// shadd8<c> <Rd>,<Rn>,<Rm>
		// xxxx01100011xxxxxxxx(1)(1)(1)(1)1001xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_shadd8, "shadd8", "xxxx01100011xxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.161 SHASX
		// shasx<c> <Rd>,<Rn>,<Rm>
		// xxxx01100011xxxxxxxx(1)(1)(1)(1)0011xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_shasx, "shasx", "xxxx01100011xxxxxxxxxxxx0011xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.162 SHSAX
		// shsax<c> <Rd>,<Rn>,<Rm>
		// xxxx01100011xxxxxxxx(1)(1)(1)(1)0101xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_shsax, "shsax", "xxxx01100011xxxxxxxxxxxx0101xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.163 SHSUB16
		// shsub16<c> <Rd>,<Rn>,<Rm>
		// xxxx01100011xxxxxxxx(1)(1)(1)(1)0111xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_shsub16, "shsub16", "xxxx01100011xxxxxxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.164 SHSUB8
		// shsub8<c> <Rd>,<Rn>,<Rm>
		// xxxx01100011xxxxxxxx(1)(1)(1)(1)1111xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_shsub8, "shsub8", "xxxx01100011xxxxxxxxxxxx1111xxxx"),
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.166 SMLABB, SMLABT, SMLATB, SMLATT
		// smla<x><y><c> <Rd>,<Rn>,<Rm>,<Ra>
		// xxxx00010000xxxxxxxxxxxx1xx0xxxx
		new OpcodeARM(Index.arm_smla, "smla", "xxxx00010000xxxxxxxxxxxx1xx0xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.177 SMUAD
		// smuad{x}<c> <Rd>,<Rn>,<Rm>
		// xxxx01110000xxxx1111xxxx00x1xxxx
		// must precede arm_smlad in search table
		new OpcodeARM(Index.arm_smuad, "smuad", "xxxx01110000xxxx1111xxxx00x1xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.167 SMLAD
		// smlad{x}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// xxxx01110000xxxxxxxxxxxx00x1xxxx
		// must follow arm_smuad in search table
		new OpcodeARM(Index.arm_smlad, "smlad", "xxxx01110000xxxxxxxxxxxx00x1xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.168 SMLAL
		// smlal{s}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// xxxx0000111xxxxxxxxxxxxx1001xxxx
		new OpcodeARM(Index.arm_smlal, "smlal", "xxxx0000111xxxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.169 SMLALBB, SMLALBT, SMLALTB, SMLALTT
		// smlal<x><y><c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// xxxx00010100xxxxxxxxxxxx1xx0xxxx
		new OpcodeARM(Index.arm_smlalxy, "smlal", "xxxx00010100xxxxxxxxxxxx1xx0xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.170 SMLALD
		// smlald{x}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// xxxx01110100xxxxxxxxxxxx00x1xxxx
		new OpcodeARM(Index.arm_smlald, "smlald", "xxxx01110100xxxxxxxxxxxx00x1xxxx"),
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.171 SMLAWB, SMLAWT
		// smlaw<y><c> <Rd>,<Rn>,<Rm>,<Ra>
		// xxxx00010010xxxxxxxxxxxx1x00xxxx
		new OpcodeARM(Index.arm_smlaw, "smlaw", "xxxx00010010xxxxxxxxxxxx1x00xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.181 SMUSD
		// smusd{x}<c> <Rd>,<Rn>,<Rm>
		// xxxx01110000xxxx1111xxxx01x1xxxx
		// must precede arm_smlsd in search table
		new OpcodeARM(Index.arm_smusd, "smusd", "xxxx01110000xxxx1111xxxx01x1xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.172 SMLSD
		// smlsd{x}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// xxxx01110000xxxxxxxxxxxx01x1xxxx
		// must follow arm_smusd in search table
		new OpcodeARM(Index.arm_smlsd, "smlsd", "xxxx01110000xxxxxxxxxxxx01x1xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.173 SMLSLD
		// smlsld{x}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// xxxx01110100xxxxxxxxxxxx01x1xxxx
		new OpcodeARM(Index.arm_smlsld, "smlsld", "xxxx01110100xxxxxxxxxxxx01x1xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.176 SMMUL
		// smmul{r}<c> <Rd>,<Rn>,<Rm>
		// xxxx01110101xxxx1111xxxx00x1xxxx
		// must precede arm_smmla in search table
		new OpcodeARM(Index.arm_smmul, "smmul", "xxxx01110101xxxx1111xxxx00x1xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.174 SMMLA
		// smmla{r}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// xxxx01110101xxxxxxxxxxxx00x1xxxx
		// must follow arm_smmul in search table
		new OpcodeARM(Index.arm_smmla, "smmla", "xxxx01110101xxxxxxxxxxxx00x1xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.175 SMMLS
		// smmls{r}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// xxxx01110101xxxxxxxxxxxx11x1xxxx
		new OpcodeARM(Index.arm_smmls, "smmls", "xxxx01110101xxxxxxxxxxxx11x1xxxx"),
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.178 SMULBB, SMULBT, SMULTB, SMULTT
		// smul<x><y><c> <Rd>,<Rn>,<Rm>
		// xxxx00010110xxxxxxxxxxxx1xx0xxxx
		new OpcodeARM(Index.arm_smul, "smul", "xxxx00010110xxxxxxxxxxxx1xx0xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.179 SMULL
		// smull{s}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// xxxx0000110xxxxxxxxxxxxx1001xxxx
		new OpcodeARM(Index.arm_smull, "smull", "xxxx0000110xxxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.180 SMULWB, SMULWT
		// smulw<y><c> <Rd>,<Rn>,<Rm>
		// xxxx00010010xxxxxxxxxxxx1x10xxxx
		new OpcodeARM(Index.arm_smulw, "smulw", "xxxx00010010xxxxxxxxxxxx1x10xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.183 SSAT
		// ssat<c> <Rd>,#<imm>,<Rn>{,<shift>}
		// xxxx0110101xxxxxxxxxxxxxxx01xxxx
		new OpcodeARM(Index.arm_ssat, "ssat", "xxxx0110101xxxxxxxxxxxxxxx01xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.184 SSAT16
		// ssat16<c> <Rd>,#<imm>,<Rn>
		// xxxx01101010xxxxxxxx(1)(1)(1)(1)0011xxxx
		new OpcodeARM(Index.arm_ssat16, "ssat16", "xxxx01101010xxxxxxxxxxxx0011xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.185 SSAX
		// ssax<c> <Rd>,<Rn>,<Rm>
		// xxxx01100001xxxxxxxx(1)(1)(1)(1)0101xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_ssax,     "ssax",     "xxxx01100001xxxxxxxxxxxx0101xxxx"),
//was	new OpcodeARM(Index.arm_ssubaddx, "ssubaddx", "xxxx01100001xxxxxxxx11110101xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.186 SSUB16
		// ssub16<c> <Rd>,<Rn>,<Rm>
		// xxxx01100001xxxxxxxx(1)(1)(1)(1)0111xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_ssub16, "ssub16", "xxxx01100001xxxxxxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.187 SSUB8
		// ssub8<c> <Rd>,<Rn>,<Rm>
		// xxxx01100001xxxxxxxx(1)(1)(1)(1)1111xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_ssub8, "ssub8", "xxxx01100001xxxxxxxxxxxx1111xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.189 STM / STMIA / STMEA
		// stm<c> <Rn>{!},<registers>
		// must precede arm_stm__usr_regs in search table
		// xxxx100010x0xxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_stm__regs, "stm", "xxxx100010x0xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.190 STMDA / STMED
		// stmda<c> <Rn>{!},<registers>
		// xxxx100000x0xxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_stmda, "stmda", "xxxx100000x0xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.123 PUSH
		// push<c> <registers> <registers> contains more than one register
		// xxxx100100101101xxxxxxxxxxxxxxxx
		// must precede arm_stmdb in search table
		new OpcodeARM(Index.arm_push__regs, "push", "xxxx100100101101xxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.191 STMDB / STMFD
		// stmdb<c> <Rn>{!},<registers>
		// xxxx100100x0xxxxxxxxxxxxxxxxxxxx
		// must follow arm_push__regs in search table
		new OpcodeARM(Index.arm_stmdb, "stmdb", "xxxx100100x0xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.192 STMIB / STMFA
		// stmib<c> <Rn>{!},<registers>
		// xxxx100110x0xxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_stmib, "stmib", "xxxx100110x0xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.210 STRT
		// strt<c> <Rt>, [<Rn>] {, +/-<imm12>}
		// xxxx0100x010xxxxxxxxxxxxxxxxxxxx
		// must precede arm_str__imm in search table
		new OpcodeARM(Index.arm_strt__imm, "strt", "xxxx0100x010xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.123 PUSH
		// push<c> <registers> <registers> contains one register, <Rt>
		// xxxx010100101101xxxx000000000100
		// must precede arm_str__imm in search table
		new OpcodeARM(Index.arm_push__reg, "push", "xxxx010100101101xxxx000000000100"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.194 STR (immediate, ARM)
		// str<c> <Rt>,[<Rn>{,#+/-<imm12>}]	str<c> <Rt>,[<Rn>],#+/-<imm12>	str<c> <Rt>,[<Rn>,#+/-<imm12>]!
		// xxxx010xx0x0xxxxxxxxxxxxxxxxxxxx
		// must follow arm_strt__imm in search table
		// must follow arm_push__reg in search table
		new OpcodeARM(Index.arm_str__imm, "str", "xxxx010xx0x0xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.210 STRT
		// strt<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// xxxx0110x010xxxxxxxxxxxxxxx0xxxx
		// must precede arm_str__reg in search table
		new OpcodeARM(Index.arm_strt__reg, "strt", "xxxx0110x010xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.195 STR (register)
		// str<c> <Rt>,[<Rn>,+/-<Rm>{, <shift>}]{!}	str<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// xxxx011xx0x0xxxxxxxxxxxxxxx0xxxx
		// must follow arm_strt__reg in search table
		new OpcodeARM(Index.arm_str__reg, "str", "xxxx011xx0x0xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.199 STRBT
		// strbt<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// xxxx0110x110xxxxxxxxxxxxxxx0xxxx
		// must precede arm_strb__reg in search table
		new OpcodeARM(Index.arm_strbt__reg, "strbt", "xxxx0110x110xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.198 STRB (register)
		// strb<c> <Rt>,[<Rn>,+/-<Rm>{, <shift>}]{!}	strb<c> <Rt>,[<Rn>],+/-<Rm>{, <shift>}
		// xxxx011xx1x0xxxxxxxxxxxxxxx0xxxx
		// must follow arm_strbt__reg in search table
		new OpcodeARM(Index.arm_strb__reg, "strb", "xxxx011xx1x0xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.201 STRD (register)
		// strd<c> <Rt>,<Rt2>,[<Rn>,+/-<Rm>]{!}	strd<c> <Rt>,<Rt2>,[<Rn>],+/-<Rm>
		// xxxx000xx0x0xxxxxxxx(0)(0)(0)(0)1111xxxx
		new OpcodeARM(Index.arm_strd__reg, "strd", "xxxx000xx0x0xxxxxxxxxxxx1111xxxx"),
		// NEW - Encoding A1 ARMv6*, ARMv7
		// A8.6.202 STREX
		// strex<c> <Rd>,<Rt>,[<Rn>]
		// xxxx00011000xxxxxxxx(1)(1)(1)(1)1001xxxx
		new OpcodeARM(Index.arm_strex, "strex", "xxxx00011000xxxxxxxxxxxx1001xxxx"),
		// NEW - Encoding A1 ARMv6K, ARMv7
		// A8.6.203 STREXB
		// strexb<c> <Rd>,<Rt>,[<Rn>]
		// xxxx00011100xxxxxxxx(1)(1)(1)(1)1001xxxx
		new OpcodeARM(Index.arm_strexb, "strexb", "xxxx00011100xxxxxxxxxxxx1001xxxx"),
		// NEW - Encoding A1 ARMv6K, ARMv7
		// A8.6.204 STREXD
		// strexd<c> <Rd>,<Rt>,<Rt2>,[<Rn>]
		// xxxx00011010xxxxxxxx(1)(1)(1)(1)1001xxxx
		new OpcodeARM(Index.arm_strexd, "strexd", "xxxx00011010xxxxxxxxxxxx1001xxxx"),
		// NEW - Encoding A1 ARMv6K, ARMv7
		// A8.6.205 STREXH
		// strexh<c> <Rd>,<Rt>,[<Rn>]
		// xxxx00011110xxxxxxxx(1)(1)(1)(1)1001xxxx
		new OpcodeARM(Index.arm_strexh, "strexh", "xxxx00011110xxxxxxxxxxxx1001xxxx"),
		// NEW - Encoding A2 ARMv6T2, ARMv7
		// A8.6.209 STRHT
		// strht<c> <Rt>, [<Rn>], +/-<Rm>
		// xxxx0000x010xxxxxxxx(0)(0)(0)(0)1011xxxx
		// must precede arm_strh__reg in search table
		new OpcodeARM(Index.arm_strht__reg, "strht", "xxxx0000x010xxxxxxxxxxxx1011xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.208 STRH (register)
		// strh<c> <Rt>,[<Rn>,+/-<Rm>]{!}	strh<c> <Rt>,[<Rn>],+/-<Rm>
		// xxxx000xx0x0xxxxxxxx(0)(0)(0)(0)1011xxxx
		// must follow arm_strht__reg in search table
		new OpcodeARM(Index.arm_strh__reg, "strh", "xxxx000xx0x0xxxxxxxxxxxx1011xxxx"),
//		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
//		// A8.6.215 SUB (SP minus immediate)
//		// sub{s}<c> <Rd>,sp,#<const>
//		// xxxx0010010x1101xxxxxxxxxxxxxxxx
// SEE arm_sub__imm
//		new OpcodeARM(Index.arm_sub__sp_imm, "sub", "xxxx0010010x1101xxxxxxxxxxxxxxxx"),
//		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
//		// A8.6.216 SUB (SP minus register)
//		// sub{s}<c> <Rd>,sp,<Rm>{,<shift>}
//		// xxxx0000010x1101xxxxxxxxxxx0xxxx
// SEE arm_sub__reg
//		new OpcodeARM(Index.arm_sub__sp_reg, "sub", "xxxx0000010x1101xxxxxxxxxxx0xxxx"),
		// NEW - Encoding A2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.10 ADR
		// adr<c> <Rd>,<label>	sub<c> <Rd>,pc,#<const>	Alternative form
		// xxxx001001001111xxxxxxxxxxxxxxxx
		// must precede arm_sub__imm in search table
		new OpcodeARM(Index.arm_adr__lower, "sub", "xxxx001001001111xxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.213 SUB (register)
		// sub{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// xxxx0000010xxxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.arm_sub__reg, "sub", "xxxx0000010xxxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.214 SUB (register-shifted register)
		// sub{s}<c> <Rd>,<Rn>,<Rm>,<type> <Rs>
		// xxxx0000010xxxxxxxxxxxxx0xx1xxxx
		new OpcodeARM(Index.arm_sub__rsr, "sub", "xxxx0000010xxxxxxxxxxxxx0xx1xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.218 SVC (previously SWI)
		// svc<c> #<imm24>
		// xxxx1111xxxxxxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_svc, "svc", "xxxx1111xxxxxxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6* (deprecated), ARMv7 (deprecated)
		// A8.6.219 SWP, SWPB
		// swp{b}<c> <Rt>,<Rt2>,[<Rn>]
		// xxxx00010x00xxxxxxxx(0)(0)(0)(0)1001xxxx
		new OpcodeARM(Index.arm_swp, "swp", "xxxx00010x00xxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.223 SXTB
		// sxtb<c> <Rd>,<Rm>{,<rotation>}
		// xxxx011010101111xxxxxx(0)(0)0111xxxx
		// must precede arm_sxtab in search table
		new OpcodeARM(Index.arm_sxtb, "sxtb", "xxxx011010101111xxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.220 SXTAB
		// sxtab<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// xxxx01101010xxxxxxxxxx(0)(0)0111xxxx
		// must follow arm_sxtb in search table
		new OpcodeARM(Index.arm_sxtab, "sxtab", "xxxx01101010xxxxxxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.224 SXTB16
		// sxtb16<c> <Rd>,<Rm>{,<rotation>}
		// xxxx011010001111xxxxxx(0)(0)0111xxxx
		// must precede arm_sxtab16 in search table
		new OpcodeARM(Index.arm_sxtb16, "sxtb16", "xxxx011010001111xxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.221 SXTAB16
		// sxtab16<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// xxxx01101000xxxxxxxxxx(0)(0)0111xxxx
		// must follow arm_sxtb16 in search table
		new OpcodeARM(Index.arm_sxtab16, "sxtab16", "xxxx01101000xxxxxxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.225 SXTH
		// sxth<c> <Rd>,<Rm>{,<rotation>}
		// xxxx011010111111xxxxxx(0)(0)0111xxxx
		// must precede arm_sxtah in search table
		new OpcodeARM(Index.arm_sxth, "sxth", "xxxx011010111111xxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.222 SXTAH
		// sxtah<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// xxxx01101011xxxxxxxxxx(0)(0)0111xxxx
		// must follow arm_sxth in search table
		new OpcodeARM(Index.arm_sxtah, "sxtah", "xxxx01101011xxxxxxxxxxxx0111xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.227 TEQ (immediate)
		// teq<c> <Rn>,#<const>
		// xxxx00110011xxxx(0)(0)(0)(0)xxxxxxxxxxxx
		new OpcodeARM(Index.arm_teq__imm, "teq", "xxxx00110011xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.228 TEQ (register)
		// teq<c> <Rn>,<Rm>{,<shift>}
		// xxxx00010011xxxx(0)(0)(0)(0)xxxxxxx0xxxx
		new OpcodeARM(Index.arm_teq__reg, "teq", "xxxx00010011xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.229 TEQ (register-shifted register)
		// teq<c> <Rn>,<Rm>,<type> <Rs>
		// xxxx00010011xxxx(0)(0)(0)(0)xxxx0xx1xxxx
		new OpcodeARM(Index.arm_teq__rsr, "teq", "xxxx00010011xxxxxxxxxxxx0xx1xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.230 TST (immediate)
		// tst<c> <Rn>,#<const>
		// xxxx00110001xxxx(0)(0)(0)(0)xxxxxxxxxxxx
		new OpcodeARM(Index.arm_tst__imm, "tst", "xxxx00110001xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.231 TST (register)
		// tst<c> <Rn>,<Rm>{,<shift>}
		// xxxx00010001xxxx(0)(0)(0)(0)xxxxxxx0xxxx
		new OpcodeARM(Index.arm_tst__reg, "tst", "xxxx00010001xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.232 TST (register-shifted register)
		// tst<c> <Rn>,<Rm>,<type> <Rs>
		// xxxx00010001xxxx(0)(0)(0)(0)xxxx0xx1xxxx
		new OpcodeARM(Index.arm_tst__rsr, "tst", "xxxx00010001xxxxxxxxxxxx0xx1xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.233 UADD16
		// uadd16<c> <Rd>,<Rn>,<Rm>
		// xxxx01100101xxxxxxxx(1)(1)(1)(1)0001xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uadd16, "uadd16", "xxxx01100101xxxxxxxxxxxx0001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.234 UADD8
		// uadd8<c> <Rd>,<Rn>,<Rm>
		// xxxx01100101xxxxxxxx(1)(1)(1)(1)1001xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uadd8, "uadd8", "xxxx01100101xxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.235 UASX
		// uasx<c> <Rd>,<Rn>,<Rm>
		// xxxx01100101xxxxxxxx(1)(1)(1)(1)0011xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uasx,     "uasx",     "xxxx01100101xxxxxxxxxxxx0011xxxx"),
//was	new OpcodeARM(Index.arm_uaddsubx, "uaddsubx", "xxxx01100101xxxxxxxx11110011xxxx"),
		// NEW - Encoding A1 ARMv6T2, ARMv7
		// A8.6.236 UBFX
		// ubfx<c> <Rd>,<Rn>,#<lsb>,#<width>
		// xxxx0111111xxxxxxxxxxxxxx101xxxx
		new OpcodeARM(Index.arm_ubfx, "ubfx", "xxxx0111111xxxxxxxxxxxxxx101xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.238 UHADD16
		// uhadd16<c> <Rd>,<Rn>,<Rm>
		// xxxx01100111xxxxxxxx(1)(1)(1)(1)0001xxxx
// see arm__r_dnm_math		
//		new OpcodeARM(Index.arm_uhadd16, "uhadd16", "xxxx01100111xxxxxxxxxxxx0001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.239 UHADD8
		// uhadd8<c> <Rd>,<Rn>,<Rm>
		// xxxx01100111xxxxxxxx(1)(1)(1)(1)1001xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uhadd8, "uhadd8", "xxxx01100111xxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.240 UHASX
		// uhasx<c> <Rd>,<Rn>,<Rm>
		// xxxx01100111xxxxxxxx(1)(1)(1)(1)0011xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uhasx,     "uhasx",     "xxxx01100111xxxxxxxxxxxx0011xxxx"),
//was	new OpcodeARM(Index.arm_uhaddsubx, "uhaddsubx", "xxxx01100111xxxxxxxx11110011xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.241 UHSAX
		// uhsax<c> <Rd>,<Rn>,<Rm>
		// xxxx01100111xxxxxxxx(1)(1)(1)(1)0101xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uhsax,     "uhsax",     "xxxx01100111xxxxxxxxxxxx0101xxxx"),
//was	new OpcodeARM(Index.arm_uhsubaddx, "uhsubaddx", "xxxx01100111xxxxxxxx11110101xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.242 UHSUB16
		// uhsub16<c> <Rd>,<Rn>,<Rm>
		// xxxx01100111xxxxxxxx(1)(1)(1)(1)0111xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uhsub16, "uhsub16", "xxxx01100111xxxxxxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.243 UHSUB8
		// uhsub8<c> <Rd>,<Rn>,<Rm>
		// xxxx01100111xxxxxxxx(1)(1)(1)(1)1111xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uhsub8, "uhsub8", "xxxx01100111xxxxxxxxxxxx1111xxxx"),
		// NEW - Encoding A1 ARMv6*, ARMv7
		// A8.6.244 UMAAL
		// umaal<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// xxxx00000100xxxxxxxxxxxx1001xxxx
		new OpcodeARM(Index.arm_umaal, "umaal", "xxxx00000100xxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.245 UMLAL
		// umlal{s}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// xxxx0000101xxxxxxxxxxxxx1001xxxx
		new OpcodeARM(Index.arm_umlal, "umlal", "xxxx0000101xxxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.246 UMULL
		// umull{s}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// xxxx0000100xxxxxxxxxxxxx1001xxxx
		new OpcodeARM(Index.arm_umull, "umull", "xxxx0000100xxxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.247 UQADD16
		// uqadd16<c> <Rd>,<Rn>,<Rm>
		// xxxx01100110xxxxxxxx(1)(1)(1)(1)0001xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uqadd16, "uqadd16", "xxxx01100110xxxxxxxxxxxx0001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.248 UQADD8
		// uqadd8<c> <Rd>,<Rn>,<Rm>
		// xxxx01100110xxxxxxxx(1)(1)(1)(1)1001xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uqadd8, "uqadd8", "xxxx01100110xxxxxxxxxxxx1001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.249 UQASX
		// uqasx<c> <Rd>,<Rn>,<Rm>
		// xxxx01100110xxxxxxxx(1)(1)(1)(1)0011xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uqasx,     "uqasx",     "xxxx01100110xxxxxxxxxxxx0011xxxx"),
//was	new OpcodeARM(Index.arm_uqaddsubx, "uqaddsubx", "xxxx01100110xxxxxxxx11110011xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.250 UQSAX
		// uqsax<c> <Rd>,<Rn>,<Rm>
		// xxxx01100110xxxxxxxx(1)(1)(1)(1)0101xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uqsax,     "uqsax",     "xxxx01100110xxxxxxxxxxxx0101xxxx"),
//was	new OpcodeARM(Index.arm_uqsubaddx, "uqsubaddx", "xxxx01100110xxxxxxxx11110101xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.251 UQSUB16
		// uqsub16<c> <Rd>,<Rn>,<Rm>
		// xxxx01100110xxxxxxxx(1)(1)(1)(1)0111xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uqsub16, "uqsub16", "xxxx01100110xxxxxxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.252 UQSUB8
		// uqsub8<c> <Rd>,<Rn>,<Rm>
		// xxxx01100110xxxxxxxx(1)(1)(1)(1)1111xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_uqsub8, "uqsub8", "xxxx01100110xxxxxxxxxxxx1111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.253 USAD8
		// usad8<c> <Rd>,<Rn>,<Rm>
		// xxxx01111000xxxx1111xxxx0001xxxx
		// must precede arm_usada8 in search table
		new OpcodeARM(Index.arm_usad8, "usad8", "xxxx01111000xxxx1111xxxx0001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.254 USADA8
		// usada8<c> <Rd>,<Rn>,<Rm>,<Ra>
		// xxxx01111000xxxxxxxxxxxx0001xxxx
		// must follow arm_usad8 in search table
		new OpcodeARM(Index.arm_usada8, "usada8", "xxxx01111000xxxxxxxxxxxx0001xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.255 USAT
		// usat<c> <Rd>,#<imm5>,<Rn>{,<shift>}
		// xxxx0110111xxxxxxxxxxxxxxx01xxxx
		new OpcodeARM(Index.arm_usat, "usat", "xxxx0110111xxxxxxxxxxxxxxx01xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.256 USAT16
		// usat16<c> <Rd>,#<imm4>,<Rn>
		// xxxx01101110xxxxxxxx(1)(1)(1)(1)0011xxxx
		new OpcodeARM(Index.arm_usat16, "usat16", "xxxx01101110xxxxxxxxxxxx0011xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.257 USAX
		// usax<c> <Rd>,<Rn>,<Rm>
		// xxxx01100101xxxxxxxx(1)(1)(1)(1)0101xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_usax,     "usax",     "xxxx01100101xxxxxxxxxxxx0101xxxx"),
//was	new OpcodeARM(Index.arm_usubaddx, "usubaddx", "xxxx01100101xxxxxxxx11110101xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.258 USUB16
		// usub16<c> <Rd>,<Rn>,<Rm>
		// xxxx01100101xxxxxxxx(1)(1)(1)(1)0111xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_usub16, "usub16", "xxxx01100101xxxxxxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.259 USUB8
		// usub8<c> <Rd>,<Rn>,<Rm>
		// xxxx01100101xxxxxxxx(1)(1)(1)(1)1111xxxx
// see arm__r_dnm_math
//		new OpcodeARM(Index.arm_usub8, "usub8", "xxxx01100101xxxxxxxxxxxx1111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.263 UXTB
		// uxtb<c> <Rd>,<Rm>{,<rotation>}
		// xxxx011011101111xxxxxx(0)(0)0111xxxx
		// must precede arm_uxtab in search table
		new OpcodeARM(Index.arm_uxtb, "uxtb", "xxxx011011101111xxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.260 UXTAB
		// uxtab<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// xxxx01101110xxxxxxxxxx(0)(0)0111xxxx
		// must follow arm_uxtb in search table
		new OpcodeARM(Index.arm_uxtab, "uxtab", "xxxx01101110xxxxxxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.264 UXTB16
		// uxtb16<c> <Rd>,<Rm>{,<rotation>}
		// xxxx011011001111xxxxxx(0)(0)0111xxxx
		// must precede arm_uxtab16 in search table
		new OpcodeARM(Index.arm_uxtb16, "uxtb16", "xxxx011011001111xxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.261 UXTAB16
		// uxtab16<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// xxxx01101100xxxxxxxxxx(0)(0)0111xxxx
		// must follow arm_uxtb16 in search table
		new OpcodeARM(Index.arm_uxtab16, "uxtab16", "xxxx01101100xxxxxxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.265 UXTH
		// uxth<c> <Rd>,<Rm>{,<rotation>}
		// xxxx011011111111xxxxxx(0)(0)0111xxxx
		// must precede arm_uxtah in search table
		new OpcodeARM(Index.arm_uxth, "uxth", "xxxx011011111111xxxxxxxx0111xxxx"),
		// Encoding A1 ARMv6*, ARMv7
		// A8.6.262 UXTAH
		// uxtah<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// xxxx01101111xxxxxxxxxx(0)(0)0111xxxx
		// must follow arm_uxth in search table
		new OpcodeARM(Index.arm_uxtah, "uxtah", "xxxx01101111xxxxxxxxxxxx0111xxxx"),
		// NEW - Encoding A1 Security Extensions
		// B6.1.9 SMC (previously SMI)
		// smc<c> #<imm4>
		// xxxx00010110(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)0111xxxx
		new OpcodeARM(Index.arm_smc, "smc", "xxxx00010110xxxxxxxxxxxx0111xxxx"),
		// NEW - Encoding A1 ARMv6*, ARMv7
		// B6.1.10 SRS
		// srs{<amode>} sp{!},#<mode>
		// 1111100xx1x0(1)(1)(0)(1)(0)(0)(0)(0)(0)(1)(0)(1)(0)(0)(0)xxxxx
		new OpcodeARM(Index.arm_srs, "srs", "1111100xx1x0xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// B6.1.11 STM (user registers)
		// stm{amode}<c> <Rn>,<registers>^
		// xxxx100xx1(0)0xxxxxxxxxxxxxxxxxxxx
		// must follow arm_stm__regs in search table
		new OpcodeARM(Index.arm_stm__usr_regs, "stm", "xxxx100xx1x0xxxxxxxxxxxxxxxxxxxx"),

		// VFP and Advanced SIMD instructions

		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.266 VABA, VABAL
		// vaba<c>.<dt> <Qd>, <Qn>, <Qm>	vaba<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1111001x0xxxxxxxxxxx0111xxx1xxxx
		new OpcodeARM(Index.arm_vaba, "vaba", "1111001x0xxxxxxxxxxx0111xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.281 VCEQ (immediate #0)
		// vceq<c>.<dt> <Qd>, <Qm>, #0	vceq<c>.<dt> <Dd>, <Dm>, #0
		// 111100111x11xx01xxxx0x010xx0xxxx
		// must precede arm_vabal
		// must precede arm_vaddl_vaddw
		new OpcodeARM(Index.arm_vceq__imm0, "vceq", "111100111x11xx01xxxx0x010xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.287 VCLE (immediate #0)
		// vcle<c>.<dt> <Qd>, <Qm>, #0	vcle<c>.<dt> <Dd>, <Dm>, #0
		// 111100111x11xx01xxxx0x011xx0xxxx
		// must precede arm_vabal
		// must precede arm_vaddl_vaddw
		new OpcodeARM(Index.arm_vcle, "vcle", "111100111x11xx01xxxx0x011xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.293 VCNT
		// vcnt<c>.8 <Qd>, <Qm>	vcnt<c>.8 <Dd>, <Dm>
		// 111100111x11xx00xxxx01010xx0xxxx
		// must precede arm_vabal
		new OpcodeARM(Index.arm_vcnt, "vcnt", "111100111x11xx00xxxx01010xx0xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.267 VABD, VABDL (integer)
		// vabd<c>.<dt> <Qd>, <Qn>, <Qm>	vabd<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1111001x0xxxxxxxxxxx0111xxx0xxxx
		new OpcodeARM(Index.arm_vabd__int, "vabd", "1111001x0xxxxxxxxxxx0111xxx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variant)
		// A8.6.269 VABS
		// vabs<c>.<dt> <Qd>, <Qm>	vabs<c>.<dt> <Dd>, <Dm>
		// 111100111x11xx01xxxx0x110xx0xxxx
		// must precede arm_vabdl
		new OpcodeARM(Index.arm_vabs, "vabs", "111100111x11xx01xxxx0x110xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.294 VCVT (between floating-point and integer, Advanced SIMD)
		// vcvt<c>.<Td>.<Tm> <Qd>, <Qm>	vcvt<c>.<Td>.<Tm> <Dd>, <Dm>
		// 111100111x11xx11xxxx011xxxx0xxxx
		// must precede arm_vabdl
		new OpcodeARM(Index.arm_vcvt__fp_i_vec, "vcvt", "111100111x11xx11xxxx011xxxx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD with half-precision extensions (UNDEFINED in integer-only variant)
		// A8.6.299 VCVT (between half-precision and single-precision, Advanced SIMD)
		// vcvt<c>.f32.f16 <Qd>, <Dm>	vcvt<c>.f16.f32 <Dd>, <Qm>
		// 111100111x11xx10xxxx011x00x0xxxx
		// must precede arm_vabdl
		new OpcodeARM(Index.arm_vcvt__hp_sp_vec, "vcvt", "111100111x11xx10xxxx011x00x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.342 VNEG
		// vneg<c>.<dt> <Qd>, <Qm>	vneg<c>.<dt> <Dd>, <Dm>
		// 111100111x11xx01xxxx0x111xx0xxxx
		// must precede arm_vabdl
		// must precede arm_vmlal
		// must precede arm_vmlsl
		new OpcodeARM(Index.arm_vneg, "vneg", "111100111x11xx01xxxx0x111xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.356 VQABS
		// vqabs<c>.<dt> <Qd>,<Qm>	vqabs<c>.<dt> <Dd>,<Dm>
		// 111100111x11xx00xxxx01110xx0xxxx
		// must precede arm_vabdl
		// must precede arm_vmlal
		new OpcodeARM(Index.arm_vqabs, "vqabs", "111100111x11xx00xxxx01110xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.362 VQNEG
		// vqneg<c>.<dt> <Qd>,<Qm>	vqneg<c>.<dt> <Dd>,<Dm>
		// 111100111x11xx00xxxx01111xx0xxxx
		// must precede arm_vabdl
		// must precede arm_vmlsl
		new OpcodeARM(Index.arm_vqneg, "vqneg", "111100111x11xx00xxxx01111xx0xxxx"),
		// NEW - Encoding A2 Advanced SIMD
		// A8.6.267 VABD, VABDL (integer)
		// vabdl<c>.<dt> <Qd>, <Dn>, <Dm>
		// 1111001x1xxxxxxxxxxx0111x0x0xxxx
		// must follow arm_vabs
		// must follow arm_vcvt__fp_i_vec
		// must follow arm_vcvt__hp_sp_vec
		// must follow arm_vneg
		// must follow arm_vqabs
		// must follow arm_vqneg
		new OpcodeARM(Index.arm_vabdl, "vabdl", "1111001x1xxxxxxxxxxx0111x0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.268 VABD (floating-point)
		// vabd<c>.f32 <Qd>, <Qn>, <Qm>	vabd<c>.f32 <Dd>, <Dn>, <Dm>
		// 111100110x1xxxxxxxxx1101xxx0xxxx
		new OpcodeARM(Index.arm_vabd__f32, "vabd", "111100110x1xxxxxxxxx1101xxx0xxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.269 VABS
		// vabs<c>.f64 <Dd>, <Dm>	vabs<c>.f32 <Sd>, <Sm>
		// xxxx11101x110000xxxx101x11x0xxxx
		new OpcodeARM(Index.arm_vabs__f, "vabs", "xxxx11101x110000xxxx101x11x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.270 VACGE, VACGT, VACLE, VACLT
		// vacge<c>.f32 <Qd>, <Qn>, <Qm>	vacge<c>.f32 <Dd>, <Dn>, <Dm>
		// vacgt<c>.f32 <Qd>, <Qn>, <Qm>	vacgt<c>.f32 <Dd>, <Dn>, <Dm>
		// 111100110xxxxxxxxxxx1110xxx1xxxx
		new OpcodeARM(Index.arm_vacge_vacgt, "vac", "111100110xxxxxxxxxxx1110xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.271 VADD (integer)
		// vadd<c>.<dt> <Qd>, <Qn>, <Qm>	vadd<c>.<dt> <Dd>, <Dn>, <Dm>
		// 111100100xxxxxxxxxxx1000xxx0xxxx
		new OpcodeARM(Index.arm_vadd__int, "vadd", "111100100xxxxxxxxxxx1000xxx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variants)
		// A8.6.272 VADD (floating-point)
		// vadd<c>.f32 <Qd>, <Qn>, <Qm>	vadd<c>.f32 <Dd>, <Dn>, <Dm>
		// 111100100x0xxxxxxxxx1101xxx0xxxx
		new OpcodeARM(Index.arm_vadd__f32, "vadd", "111100100x0xxxxxxxxx1101xxx0xxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.272 VADD (floating-point)
		// vadd<c>.f64 <Dd>, <Dn>, <Dm>	vadd<c>.f32 <Sd>, <Sn>, <Sm>
		// xxxx11100x11xxxxxxxx101xx0x0xxxx
		new OpcodeARM(Index.arm_vadd__fp_f, "vadd", "xxxx11100x11xxxxxxxx101xx0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.305 VEXT
		// vext<c>.8 <Qd>, <Qn>, <Qm>, #<imm>	vext<c>.8 <Dd>, <Dn>, <Dm>, #<imm>
		// 111100101x11xxxxxxxxxxxxxxx0xxxx
		// must precede arm_vabal
		// must precede arm_vaddhn
		new OpcodeARM(Index.arm_vext, "vext", "111100101x11xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.273 VADDHN
		// vaddhn<c>.<dt> <Dd>, <Qn>, <Qm>
		// 111100101xxxxxxxxxxx0100x0x0xxxx
		// must follow arm_vext
		new OpcodeARM(Index.arm_vaddhn, "vaddhn", "111100101xxxxxxxxxxx0100x0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.283 VCGE (immediate #0)
		// vcge<c>.<dt> <Qd>, <Qm>, #0	vcge<c>.<dt> <Dd>, <Dm>, #0
		// 111100111x11xx01xxxx0x001xx0xxxx
		// must precede arm_vaddl_vaddw
		new OpcodeARM(Index.arm_vcge__imm0, "vcge", "111100111x11xx01xxxx0x001xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.285 VCGT (immediate #0)
		// vcgt<c>.<dt> <Qd>, <Qm>, #0	vcgt<c>.<dt> <Dd>, <Dm>, #0
		// 111100111x11xx01xxxx0x000xx0xxxx
		// must precede arm_vaddl_vaddw
		new OpcodeARM(Index.arm_vcgt__imm0, "vcgt", "111100111x11xx01xxxx0x000xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.373 VREV16, VREV32, VREV64
		// vrev<n><c>.<size> <Qd>, <Qm>	vrev<n><c>.<size> <Dd>, <Dm>
		// 111100111x11xx00xxxx000xxxx0xxxx
		// must precede arm_vaddl_vaddw
		new OpcodeARM(Index.arm_vrev, "vrev", "111100111x11xx00xxxx000xxxx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.405 VSWP
		// vswp<c> <Qd>, <Qm>	vswp<c> <Dd>, <Dm>
		// 111100111x11xx10xxxx00000xx0xxxx
		// must precede arm_vaddl_vaddw
		new OpcodeARM(Index.arm_vswp, "vswp", "111100111x11xx10xxxx00000xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.409 VUZP
		// vuzp<c>.<size> <Qd>, <Qm>	vuzp<c>.<size> <Dd>, <Dm>
		// 111100111x11xx10xxxx00010xx0xxxx
		// must precede arm_vaddl_vaddw
		// must precede arm_vtrn
		new OpcodeARM(Index.arm_vuzp, "vuzp", "111100111x11xx10xxxx00010xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.410 VZIP
		// vzip<c>.<size> <Qd>, <Qm>	vzip<c>.<size> <Dd>, <Dm>
		// 111100111x11xx10xxxx00011xx0xxxx
		// must precede arm_vaddl_vaddw
		// must precede arm_vtrn
		new OpcodeARM(Index.arm_vzip, "vzip", "111100111x11xx10xxxx00011xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.407 VTRN
		// vtrn<c>.<size> <Qd>, <Qm>	vtrn<c>.<size> <Dd>, <Dm>
		// 111100111x11xx10xxxx00001xx0xxxx
		// must precede arm_vaddl_vaddw
		// must follow arm_vuzp
		// must follow arm_vzip
		new OpcodeARM(Index.arm_vtrn, "vtrn", "111100111x11xx10xxxx00001xx0xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.274 VADDL, VADDW
		// vaddl<c>.<dt> <Qd>, <Dn>, <Dm>	vaddw<c>.<dt> <Qd>, <Qn>, <Dm>
		// 1111001x1xxxxxxxxxxx000xx0x0xxxx
		// must follow arm_vceq__imm0
		// must follow arm_vcge__imm0
		// must follow arm_vcgt__imm0
		// must follow arm_vcle
		// must follow arm_vrev
		// must follow arm_vswp
		// must follow arm_vtrn
		// must follow arm_vuzp
		// must follow arm_vzip
		new OpcodeARM(Index.arm_vaddl_vaddw, "vadd", "1111001x1xxxxxxxxxxx000xx0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.276 VAND (register)
		// vand<c> <Qd>, <Qn>, <Qm>	vand<c> <Dd>, <Dn>, <Dm>
		// 111100100x00xxxxxxxx0001xxx1xxxx
		new OpcodeARM(Index.arm_vand, "vand", "111100100x00xxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.278 VBIC (register)
		// vbic<c> <Qd>, <Qn>, <Qm>	vbic<c> <Dd>, <Dn>, <Dm>
		// 111100100x01xxxxxxxx0001xxx1xxxx
		new OpcodeARM(Index.arm_vbic, "vbic", "111100100x01xxxxxxxx0001xxx1xxxx"),
//		// NEW - Encoding A1 / T1 Advanced SIMD
//		// A8.6.304 VEOR
//		// veor<c> <Qd>, <Qn>, <Qm>	veor<c> <Dd>, <Dn>, <Dm>
//		// 111100110x00xxxxxxxx0001xxx1xxxx
// SEE arm_vbif_vbit_vbsl_veor
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.279 VBIF, VBIT, VBSL
		// vbif<c> <Qd>, <Qn>, <Qm>	vbif<c> <Dd>, <Dn>, <Dm>
		// vbit<c> <Qd>, <Qn>, <Qm>	vbit<c> <Dd>, <Dn>, <Dm>
		// vbsl<c> <Qd>, <Qn>, <Qm>	vbsl<c> <Dd>, <Dn>, <Dm>
		// 111100110xxxxxxxxxxx0001xxx1xxxx
		//
		new OpcodeARM(Index.arm_vbif_vbit_vbsl_veor, "v", "111100110xxxxxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.280 VCEQ (register)
		// vceq<c>.<dt> <Qd>, <Qn>, <Qm>	vceq<c>.<dt> <Dd>, <Dn>, <Dm>
		// 111100110xxxxxxxxxxx1000xxx1xxxx
		new OpcodeARM(Index.arm_vceq__reg_int, "vceq", "111100110xxxxxxxxxxx1000xxx1xxxx"),
		// NEW - Encoding A2 / T2 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.280 VCEQ (register)
		// vceq<c>.f32 <Qd>, <Qn>, <Qm>	vceq<c>.f32 <Dd>, <Dn>, <Dm>
		// 111100100x0xxxxxxxxx1110xxx0xxxx
		new OpcodeARM(Index.arm_vceq__reg_f32, "vceq", "111100100x0xxxxxxxxx1110xxx0xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.282 VCGE (register)
		// vcge<c>.<dt> <Qd>, <Qn>, <Qm>	vcge<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1111001x0xxxxxxxxxxx0011xxx1xxxx
		new OpcodeARM(Index.arm_vcge__reg_int, "vcge", "1111001x0xxxxxxxxxxx0011xxx1xxxx"),
		// NEW - Encoding A2 / T2 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.282 VCGE (register)
		// vcge<c>.f32 <Qd>, <Qn>, <Qm>	vcge<c>.f32 <Dd>, <Dn>, <Dm>
		// 111100110x0xxxxxxxxx1110xxx0xxxx
		new OpcodeARM(Index.arm_vcge__reg_f32, "vcge", "111100110x0xxxxxxxxx1110xxx0xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.284 VCGT (register)
		// vcgt<c>.<dt> <Qd>, <Qn>, <Qm>	vcgt<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1111001x0xxxxxxxxxxx0011xxx0xxxx
		new OpcodeARM(Index.arm_vcgt__reg_int, "vcgt", "1111001x0xxxxxxxxxxx0011xxx0xxxx"),
		// NEW - Encoding A2 / T2 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.284 VCGT (register)
		// vcgt<c>.f32 <Qd>, <Qn>, <Qm>	vcgt<c>.f32 <Dd>, <Dn>, <Dm>
		// 111100110x1xxxxxxxxx1110xxx0xxxx
		new OpcodeARM(Index.arm_vcgt__reg_f32, "vcgt", "111100110x1xxxxxxxxx1110xxx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.288 VCLS
		// vcls<c>.<dt> <Qd>, <Qm>	vcls<c>.<dt> <Dd>, <Dm>
		// 111100111x11xx00xxxx01000xx0xxxx
		new OpcodeARM(Index.arm_vcls, "vcls", "111100111x11xx00xxxx01000xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.290 VCLT (immediate #0)
		// vclt<c>.<dt> <Qd>, <Qm>, #0	vclt<c>.<dt> <Dd>, <Dm>, #0
		// 111100111x11xx01xxxx0x100xx0xxxx
		new OpcodeARM(Index.arm_vclt, "vclt", "111100111x11xx01xxxx0x100xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.291 VCLZ
		// vclz<c>.<dt> <Qd>, <Qm>	vclz<c>.<dt> <Dd>, <Dm>
		// 111100111x11xx00xxxx01001xx0xxxx
		new OpcodeARM(Index.arm_vclz, "vclz", "111100111x11xx00xxxx01001xx0xxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.292 VCMP, VCMPE
		// vcmp{e}<c>.f64 <Dd>, <Dm>	vcmp{e}<c>.f32 <Sd>, <Sm>
		// xxxx11101x110100xxxx101xx1x0xxxx
		new OpcodeARM(Index.arm_vcmp__reg, "vcmp", "xxxx11101x110100xxxx101xx1x0xxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.292 VCMP, VCMPE
		// vcmp{e}<c>.f64 <Dd>, #0.0	vcmp{e}<c>.f32 <Sd>, #0.0
		// xxxx11101x110101xxxx101xx1(0)0(0)(0)(0)(0)
		new OpcodeARM(Index.arm_vcmp__to_0, "vcmp", "xxxx11101x110101xxxx101xx1x0xxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv3 (sf = 1 UNDEFINED in single-precision only variants)
		// A8.6.297 VCVT (between floating-point and fixed-point, VFP)
		// vcvt<c>.<Td>.f64 <Dd>, <Dd>, #<fbits>	vcvt<c>.<Td>.f32 <Sd>, <Sd>, #<fbits>
		// vcvt<c>.f64.<Td> <Dd>, <Dd>, #<fbits>	vcvt<c>.f32.<Td> <Sd>, <Sd>, #<fbits>
		// xxxx11101x111x1xxxxx101xx1x0xxxx
		// must precede arm_vcvt__fp_i_reg in search table
		new OpcodeARM(Index.arm_vcvt__fp_fix_reg, "vcvt", "xxxx11101x111x1xxxxx101xx1x0xxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.295 VCVT, VCVTR (between floating-point and integer, VFP)
		// vcvt{r}<c>.s32.f64 <Sd>, <Dm>	vcvt{r}<c>.s32.f32 <Sd>, <Sm>	vcvt{r}<c>.u32.f64 <Sd>, <Dm>
		// vcvt{r}<c>.u32.f32 <Sd>, <Sm>	vcvt<c>.f64.<Tm> <Dd>, <Sm>	vcvt<c>.f32.<Tm> <Sd>, <Sm>
		// xxxx11101x111xxxxxxx101xx1x0xxxx
		// must follow arm_vcvt__fp_fix_reg in search table
		new OpcodeARM(Index.arm_vcvt__fp_i_reg, "vcvt", "xxxx11101x111xxxxxxx101xx1x0xxxx"),
		// NEW - Encoding A1 / T1 (i) Advanced SIMD
		// A8.6.277 VBIC (immediate)
		// vbic<c>.<dt> <Qd>, #<imm>	vbic<c>.<dt> <Dd>, #<imm>
// SEE arm_vmov_vbitwise
		// NEW - Encoding A1 / T1 (i) Advanced SIMD
		// A8.6.346 VORR (immediate)
		// vorr<c>.<dt> <Qd>, #<imm>	vorr<c>.<dt> <Dd>, #<imm>
// SEE arm_vmov_vbitwise
		// NEW - Encoding A1 / T1 (i) Advanced SIMD
		// A8.6.340 VMVN (immediate)
		// vmvn<c>.<dt> <Qd>, #<imm>	vmvn<c>.<dt> <Dd>, #<imm>
// SEE arm_vmov_vbitwise
		// NEW - Encoding A1 / T1 (i) Advanced SIMD
		// A8.6.326 VMOV (immediate)
		// vmov<c>.<dt> <Qd>, #<imm>	vmov<c>.<dt> <Dd>, #<imm>
		//
		// 1111001x1x000xxxxxxxxxxx0x11xxxx
		// 1111001x1x000xxxxxxxxxxx0x01xxxx
		// 1111001x1x000xxxxxxxxxxx0x11xxxx
		// 1111001x1x000xxxxxxxxxxx0xx1xxxx
		// must precede arm_vcvt__fp_fix_vec
		new OpcodeARM(Index.arm_vmov_vbitwise, "_", "1111001x1x000xxxxxxxxxxx0xx1xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.296 VCVT (between floating-point and fixed-point, Advanced SIMD)
		// vcvt<c>.<Td>.<Tm> <Qd>, <Qm>, #<fbits>	vcvt<c>.<Td>.<Tm> <Dd>, <Dm>, #<fbits>
		// 1111001x1xxxxxxxxxxx111x0xx1xxxx
		// must follow arm_vmov_vbitwise
		new OpcodeARM(Index.arm_vcvt__fp_fix_vec, "vcvt", "1111001x1xxxxxxxxxxx111x0xx1xxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3 (UNDEFINED in single-precision only variants)
		// A8.6.298 VCVT (between double-precision and single-precision)
		// vcvt<c>.f64.f32 <Dd>, <Sm>	vcvt<c>.f32.f64 <Sd>, <Dm>
		// xxxx11101x110111xxxx101x11x0xxxx
		new OpcodeARM(Index.arm_vcvt__dp_sp, "vcvt", "xxxx11101x110111xxxx101x11x0xxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv3 half-precision extensions
		// A8.6.300 VCVTB, VCVTT (between half-precision and single-precision, VFP)
		// vcvt<y><c>.f32.f16 <Sd>, <Sm>	vcvt<y><c>.f16.f32 <Sd>, <Sm>
		// xxxx11101x11001xxxxx1010x1x0xxxx
		new OpcodeARM(Index.arm_vcvt__hp_sp_reg, "vcvt", "xxxx11101x11001xxxxx1010x1x0xxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.301 VDIV
		// vdiv<c>.f64 <Dd>, <Dn>, <Dm>	vdiv<c>.f32 <Sd>, <Sn>, <Sm>
		// xxxx11101x00xxxxxxxx101xx0x0xxxx
		new OpcodeARM(Index.arm_vdiv, "vdiv", "xxxx11101x00xxxxxxxx101xx0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.302 VDUP (scalar)
		// vdup<c>.<size> <Qd>, <Dm[x]>	vdup<c>.<size> <Dd>, <Dm[x]>
		// 111100111x11xxxxxxxx11000xx0xxxx
		new OpcodeARM(Index.arm_vdup__scalar, "vdup", "111100111x11xxxxxxxx11000xx0xxxx"),
		// NEW - Encoding A1 / T1 (cond) Advanced SIMD
		// A8.6.303 VDUP (ARM core register)
		// vdup<c>.<size> <Qd>, <Rt>	vdup<c>.<size> <Dd>, <Rt>
		// xxxx11101xx0xxxxxxxx1011x0x1(0)(0)(0)(0)
		new OpcodeARM(Index.arm_vdup__reg, "vdup", "xxxx11101xx0xxxxxxxx1011x0x1xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.306 VHADD, VHSUB
		// vh<op><c> <Qd>, <Qn>, <Qm>	vh<op><c> <Dd>, <Dn>, <Dm>
		// 1111001x0xxxxxxxxxxx00x0xxx0xxxx
		new OpcodeARM(Index.arm_vhadd_vhsub, "vh", "1111001x0xxxxxxxxxxx00x0xxx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.307 VLD1 (multiple single elements)
		// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101000x10xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.310 VLD2 (multiple 2-element structures)
		// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101000x10xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.313 VLD3 (multiple 3-element structures)
		// vld3<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld3<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101000x10xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.316 VLD4 (multiple 4-element structures)
		// vld4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101000x10xxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_vld__multi, "vld", "111101000x10xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.308 VLD1 (single element to one lane)
		// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101001x10xxxxxxxxxx00xxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.309 VLD1 (single element to all lanes)
		// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101001x10xxxxxxxx1100xxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.311 VLD2 (single 2-element structure to one lane)
		// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101001x10xxxxxxxxxx01xxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.312 VLD2 (single 2-element structure to all lanes)
		// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101001x10xxxxxxxx1101xxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.314 VLD3 (single 3-element structure to one lane)
		// vld3<c>.<size> <list>, [<Rn>]{!}	vld3<c>.<size> <list>, [<Rn>], <Rm>
		// 111101001x10xxxxxxxxxx10xxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.315 VLD3 (single 3-element structure to all lanes)
		// vld3<c>.<size> <list>, [<Rn>]{!}	vld3<c>.<size> <list>, [<Rn>], <Rm>
		// 111101001x10xxxxxxxx1110xxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.317 VLD4 (single 4-element structure to one lane)
		// vld4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101001x10xxxxxxxxxx11xxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.318 VLD4 (single 4-element structure to all lanes)
		// vld4<c>.<size> <list>, [<Rn>{ @<align>}]{!}	vld4<c>.<size> <list>, [<Rn>{ @<align>}], <Rm>
		// 111101001x10xxxxxxxx1111xxxxxxxx
		// 
		new OpcodeARM(Index.arm_vld__xlane, "vld", "111101001x10xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.320 VLDR
		// vldr<c> <Dd>, [<Rn>{, #+/-<imm>}]	vldr<c> <Dd>, <label>	vldr<c> <Dd>, [pc,#-0] Special case
		// xxxx1101xx01xxxxxxxx1011xxxxxxxx
		// must precede arm_vldm_1 in search table
		new OpcodeARM(Index.arm_vldr__64, "vldr", "xxxx1101xx01xxxxxxxx1011xxxxxxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.332 VMOV (between two ARM core registers and a doubleword extension register)
		// vmov<c> <Dm>, <Rt>, <Rt2>	vmov<c> <Rt>, <Rt2>, <Dm>
		// xxxx1100010xxxxxxxxx101100x1xxxx
		// must precede arm_vldm__64
		new OpcodeARM(Index.arm_vmov_9, "vmov", "xxxx1100010xxxxxxxxx101100x1xxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.354 VPOP
		// vpop <list> <list> is consecutive 64-bit registers
		// xxxx11001x111101xxxx1011xxxxxxxx
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3
		// A8.6.354 VPOP
		// vpop <list> <list> is consecutive 32-bit registers
		// xxxx11001x111101xxxx1010xxxxxxxx
		// must precede arm_vldm_1 in search table
		// must precede arm_vldm_2 in search table
		// must precede arm_vldm__64
		new OpcodeARM(Index.arm_vpop, "vpop", "xxxx11001x111101xxxx101xxxxxxxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.319 VLDM
		// vldm{mode}<c> <Rn>{!}, <list> <list> is consecutive 64-bit registers
		// xxxx110xxxx1xxxxxxxx1011xxxxxxxx
		// must follow arm_vldr_1 in search table
		// must follow arm_vmov_9
		// must follow arm_vpop in search table
		new OpcodeARM(Index.arm_vldm__64, "vldm", "xxxx110xxxx1xxxxxxxx1011xxxxxxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3
		// A8.6.331 VMOV (between two ARM core registers and two single-precision registers)
		// vmov<c> <Sm>, <Sm1>, <Rt>, <Rt2>	vmov<c> <Rt>, <Rt2>, <Sm>, <Sm1>
		// xxxx1100010xxxxxxxxx101000x1xxxx
		// must precede arm_vldr__32
		new OpcodeARM(Index.arm_vmov_8, "vmov", "xxxx1100010xxxxxxxxx101000x1xxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3
		// A8.6.320 VLDR
		// vldr<c> <Sd>, [<Rn>{, #+/-<imm>}]	vldr<c> <Sd>, <label>	vldr<c> <Sd>, [pc,#-0] Special case
		// xxxx1101xx01xxxxxxxx1010xxxxxxxx
		// must precede arm_vldm_2 in search table
		// must follow arm_vmov_8
		new OpcodeARM(Index.arm_vldr__32, "vldr", "xxxx1101xx01xxxxxxxx1010xxxxxxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3
		// A8.6.319 VLDM
		// vldm{mode}<c> <Rn>{!}, <list> <list> is consecutive 32-bit registers
		// xxxx110xxxx1xxxxxxxx1010xxxxxxxx
		// must follow arm_vldr_2 in search table
		// must follow arm_vpop in search table
		new OpcodeARM(Index.arm_vldm__32, "vldm", "xxxx110xxxx1xxxxxxxx1010xxxxxxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.321 VMAX, VMIN (integer)
		// vmax<c>.<dt> <Qd>, <Qn>, <Qm>	vmax<c>.<dt> <Dd>, <Dn>, <Dm>
		// vmin<c>.<dt> <Qd>, <Qn>, <Qm>	vmin<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1111001x0xxxxxxxxxxx0110xxxxxxxx
		new OpcodeARM(Index.arm_vmax_vmin__int, "v", "1111001x0xxxxxxxxxxx0110xxxxxxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.322 VMAX, VMIN (floating-point)
		// vmax<c>.f32 <Qd>, <Qn>, <Qm>	vmax<c>.f32 <Dd>, <Dn>, <Dm>
		// vmin<c>.f32 <Qd>, <Qn>, <Qm>	vmin<c>.f32 <Dd>, <Dn>, <Dm>
		// 111100100xxxxxxxxxxx1111xxx0xxxx
		new OpcodeARM(Index.arm_vmax_vmin__fp, "v", "111100100xxxxxxxxxxx1111xxx0xxxx"),
		// NEW - Encoding A1 / T1 (op) Advanced SIMD
		// A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
		// v<op><c>.<dt> <Qd>, <Qn>, <Qm>	v<op><c>.<dt> <Dd>, <Dn>, <Dm>
		// 1111001x0xxxxxxxxxxx1001xxx0xxxx
		new OpcodeARM(Index.arm_vml__int, "vml", "1111001x0xxxxxxxxxxx1001xxx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.406 VTBL, VTBX
		// v<op><c>.8 <Dd>, <list>, <Dm>
		// 111100111x11xxxxxxxx10xxxxx0xxxx
		// must precede arm_vml__int_long
		new OpcodeARM(Index.arm_vtb, "vtb", "111100111x11xxxxxxxx10xxxxx0xxxx"),
		// NEW - Encoding A2 / T2 (U) Advanced SIMD
		// A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
		// v<op>l<c>.<dt> <Qd>, <Dn>, <Dm>
		// 1111001x1xxxxxxxxxxx10x0x0x0xxxx
		// must follow arm_vtb
		new OpcodeARM(Index.arm_vml__int_long, "vml", "1111001x1xxxxxxxxxxx10x0x0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.324 VMLA, VMLS (floating-point)
		// v<op><c>.f32 <Qd>, <Qn>, <Qm>	v<op><c>.f32 <Dd>, <Dn>, <Dm>
		// 111100100xxxxxxxxxxx1101xxx1xxxx
		new OpcodeARM(Index.arm_vml__f32, "vml", "111100100xxxxxxxxxxx1101xxx1xxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.324 VMLA, VMLS (floating-point)
		// v<op><c>.f64 <Dd>, <Dn>, <Dm>	v<op><c>.f32 <Sd>, <Sn>, <Sm>
		// xxxx11100x00xxxxxxxx101xxxx0xxxx
		new OpcodeARM(Index.arm_vml__fp, "vml", "xxxx11100x00xxxxxxxx101xxxx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.341 VMVN (register)
		// vmvn<c> <Qd>, <Qm>	vmvn<c> <Dd>, <Dm>
		// 111100111x11xx00xxxx01011xx0xxxx
		// must precede arm_vml__scalar
		new OpcodeARM(Index.arm_vmvn, "vmvn", "111100111x11xx00xxxx01011xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.348 VPADAL
		// vpadal<c>.<dt> <Qd>, <Qm>	vpadal<c>.<dt> <Dd>, <Dm>
		// 111100111x11xx00xxxx0110xxx0xxxx
		// must precede arm_vml__scalar
		new OpcodeARM(Index.arm_vpadal, "vpadal", "111100111x11xx00xxxx0110xxx0xxxx"),
		// NEW - Encoding A2 / T2 Advanced SIMD
		// A8.6.358 VQDMLAL, VQDMLSL
		// vqd<op><c>.<dt> <Qd>,<Dn>,<Dm[x]>
		// 111100101xxxxxxxxxxx0x11x1x0xxxx
		// must precede arm_vml__scalar
		new OpcodeARM(Index.arm_vqdml__scalar, "vqdml", "111100101xxxxxxxxxxx0x11x1x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.334 VMOVN
		// vmovn<c>.<dt> <Dd>, <Qm>
		// 111100111x11xx10xxxx001000x0xxxx
		// must precede arm_vqmov in search table
		new OpcodeARM(Index.arm_vmovn, "vmovn", "111100111x11xx10xxxx001000x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.351 VPADDL
		// vpaddl<c>.<dt> <Qd>, <Qm>	vpaddl<c>.<dt> <Dd>, <Dm>
		// 111100111x11xx00xxxx0010xxx0xxxx
		// must precede arm_vml__scalar
		new OpcodeARM(Index.arm_vpaddl, "vpaddl", "111100111x11xx00xxxx0010xxx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.361 VQMOVN, VQMOVUN
		// vqmov{u}n<c>.<type><size> <Dd>, <Qm>
		// 111100111x11xx10xxxx0010xxx0xxxx
		// must follow arm_vmovn in search table
		// must precede arm_vml__scalar
		new OpcodeARM(Index.arm_vqmov, "vqmov", "111100111x11xx10xxxx0010xxx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.371 VRECPE
		// vrecpe<c>.<dt> <Qd>, <Qm>	vrecpe<c>.<dt> <Dd>, <Dm>
		// 111100111x11xx11xxxx010x0xx0xxxx
		// must precede arm_vml__scalar
		new OpcodeARM(Index.arm_vrecpe, "vrecpe", "111100111x11xx11xxxx010x0xx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.378 VRSQRTE
		// vrsqrte<c>.<dt> <Qd>, <Qm>	vrsqrte<c>.<dt> <Dd>, <Dm>
		// 111100111x11xx11xxxx010x1xx0xxxx
		// must precede arm_vabal
		// must precede arm_vml__scalar
		new OpcodeARM(Index.arm_vrsqrte, "vrsqrte", "111100111x11xx11xxxx010x1xx0xxxx"),
		// NEW - Encoding A1 / T1 (Q) Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.325 VMLA, VMLAL, VMLS, VMLSL (by scalar)
		// v<op><c>.<dt> <Qd>, <Qn>, <Dm[x]>	v<op><c>.<dt> <Dd>, <Dn>, <Dm[x]>
		// NEW - Encoding A2 / T2 (U) Advanced SIMD
		// A8.6.325 VMLA, VMLAL, VMLS, VMLSL (by scalar)
		// v<op>l<c>.<dt> <Qd>, <Dn>, <Dm[x]>
		// 1111001x1xxxxxxxxxxx0xxxx1x0xxxx
		// must follow arm_vmvn
		// must follow arm_vpadal
		// must follow arm_vpaddl
		// must follow arm_vqdml__scalar
		// must follow arm_vqmov
		// must follow arm_vrecpe
		// must follow arm_vrsqrte
		new OpcodeARM(Index.arm_vml__scalar, "vml", "1111001x1xxxxxxxxxxx0xxxx1x0xxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.326 VMOV (immediate)
		// vmov<c>.f64 <Dd>, #<imm>	vmov<c>.f32 <Sd>, #<imm>
		// xxxx11101x11xxxxxxxx101x(0)0(0)0xxxx
		new OpcodeARM(Index.arm_vmov__imm, "vmov", "xxxx11101x11xxxxxxxx101xx0x0xxxx"),
//		// NEW - Encoding A1 / T1 Advanced SIMD
//		// A8.6.327 VMOV (register)
//		// vmov<c> <Qd>, <Qm>	vmov<c> <Dd>, <Dm>
//		// 111100100x10xxxxxxxx0001xxx1xxxx
// SEE arm_vorr_2 parse
//		new OpcodeARM(Index.arm_vmov__reg, "vmov", "111100100x10xxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.327 VMOV (register)
		// vmov<c>.f64 <Dd>, <Dm>	vmov<c>.f32 <Sd>, <Sm>
		// xxxx11101x110000xxxx101x01x0xxxx
		new OpcodeARM(Index.arm_vmov__reg_f, "vmov", "xxxx11101x110000xxxx101x01x0xxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3, Advanced SIMD if opc1 == '0x' && opc2 == '00'; Advanced SIMD otherwise
		// A8.6.328 VMOV (ARM core register to scalar)
		// vmov<c>.<size> <Dd[x]>, <Rt>
		// xxxx11100xx0xxxxxxxx1011xxx1(0)(0)(0)(0)
		new OpcodeARM(Index.arm_vmov_5, "vmov", "xxxx11100xx0xxxxxxxx1011xxx1xxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3, Advanced SIMD if opc1 == '0x' && opc2 == '00';Advanced SIMD otherwise
		// A8.6.329 VMOV (scalar to ARM core register)
		// vmov<c>.<dt> <Rt>, <Dn[x]>
		// xxxx1110xxx1xxxxxxxx1011xxx1(0)(0)(0)(0)
		new OpcodeARM(Index.arm_vmov_6, "vmov", "xxxx1110xxx1xxxxxxxx1011xxx1xxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3
		// A8.6.330 VMOV (between ARM core register and single-precision register)
		// vmov<c> <Sn>, <Rt>	vmov<c> <Sn>, <Rt>	vmov<c> <Rt>, <Sn>
		// xxxx1110000xxxxxxxxx1010x(0)(0)1(0)(0)(0)(0)
		new OpcodeARM(Index.arm_vmov_7, "vmov", "xxxx1110000xxxxxxxxx1010xxx1xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.333 VMOVL
		// vmovl<c>.<dt> <Qd>, <Dm>
		// 1111001x1xxxx000xxxx101000x1xxxx
		// must precede arm_vshll_1 in search table
		new OpcodeARM(Index.arm_vmovl, "vmovl", "1111001x1xxxx000xxxx101000x1xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.384 VSHLL
		// vshll<c>.<type><size> <Qd>, <Dm>, #<imm> (0 < <imm> < <size>)
		// 1111001x1xxxxxxxxxxx101000x1xxxx
		// must follow arm_vmovl in search table
		new OpcodeARM(Index.arm_vshll__various, "vshll", "1111001x1xxxxxxxxxxx101000x1xxxx"),
//		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3, Advanced SIMD
//		// A8.6.335 VMRS
//		// vmrs<c> <Rt>, fpscr
//		// xxxx111011110001xxxx10100(0)(0)1(0)(0)(0)(0)
// SEE arm_vmrs
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3, Advanced SIMD
		// B6.1.14 VMRS
		// vmrs<c> <Rt>,<spec_reg>
		// xxxx11101111xxxxxxxx1010(0)(0)(0)1(0)(0)(0)(0)
		new OpcodeARM(Index.arm_vmrs, "vmrs", "xxxx11101111xxxxxxxx1010xxx1xxxx"),
//		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3, Advanced SIMD
//		// A8.6.336 VMSR
//		// vmsr<c> fpscr, <Rt>
//		// xxxx111011100001xxxx10100(0)(0)1(0)(0)(0)(0)
// SEE arm_vmsr
//		new OpcodeARM(Index.arm_vmsr_1, "vmsr", "xxxx111011100001xxxx10100xx1xxxx"),
		// NEW - Encoding A1 / T2 (cond) VFPv2, VFPv3, Advanced SIMD
		// B6.1.15 VMSR
		// vmsr<c> <spec_reg>,<Rt>
		// xxxx11101110xxxxxxxx1010(0)(0)(0)1(0)(0)(0)(0)
		new OpcodeARM(Index.arm_vmsr, "vmsr", "xxxx11101110xxxxxxxx1010xxx1xxxx"),
		// NEW - Encoding A1 / T1 (op) Advanced SIMD
		// A8.6.337 VMUL, VMULL (integer and polynomial)
		// vmul<c>.<dt> <Qd>, <Qn>, <Qm>	vmul<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1111001x0xxxxxxxxxxx1001xxx1xxxx
		new OpcodeARM(Index.arm_vmul_1, "vmul", "1111001x0xxxxxxxxxxx1001xxx1xxxx"),
		// NEW - Encoding A2 / T2 (U) Advanced SIMD
		// A8.6.337 VMUL, VMULL (integer and polynomial)
		// vmull<c>.<dt> <Qd>, <Dn>, <Dm>
		// 1111001x1xxxxxxxxxxx11x0x0x0xxxx
		new OpcodeARM(Index.arm_vmull, "vmull", "1111001x1xxxxxxxxxxx11x0x0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.338 VMUL (floating-point)
		// vmul<c>.f32 <Qd>, <Qn>, <Qm>	vmul<c>.f32 <Dd>, <Dn>, <Dm>
		// 111100110x0xxxxxxxxx1101xxx1xxxx
		new OpcodeARM(Index.arm_vmul_f32, "vmul", "111100110x0xxxxxxxxx1101xxx1xxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.338 VMUL (floating-point)
		// vmul<c>.f64 <Dd>, <Dn>, <Dm>	vmul<c>.f32 <Sd>, <Sn>, <Sm>
		// xxxx11100x10xxxxxxxx101xx0x0xxxx
		new OpcodeARM(Index.arm_vmul__fp_2, "vmul", "xxxx11100x10xxxxxxxx101xx0x0xxxx"),
		// NEW - Encoding A2 / T2 Advanced SIMD
		// A8.6.360 VQDMULL
		// vqdmull<c>.<dt> <Qd>,<Dn>,<Dm[x]>
		// 111100101xxxxxxxxxxx1011x1x0xxxx
		// must precede arm_vmul__scalar
		new OpcodeARM(Index.arm_vqdmull__scalar, "vqdmul", "111100101xxxxxxxxxxx1011x1x0xxxx"),
		// NEW - Encoding A1 / T1 (Q) Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.339 VMUL, VMULL (by scalar)
		// vmul<c>.<dt> <Qd>, <Qn>, <Dm[x]>	vmul<c>.<dt> <Dd>, <Dn>, <Dm[x]>
		// 1111001x1xxxxxxxxxxx100xx1x0xxxx
		// NEW - Encoding A2 / T2 (U) Advanced SIMD
		// A8.6.339 VMUL, VMULL (by scalar)
		// vmull<c>.<dt> <Qd>, <Dn>, <Dm[x]>
		// 1111001x1xxxxxxxxxxx1010x1x0xxxx
		// must follow arm_vqdmull__scalar
		new OpcodeARM(Index.arm_vmul__scalar, "vmul", "1111001x1xxxxxxxxxxx10xxx1x0xxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.342 VNEG
		// vneg<c>.f64 <Dd>, <Dm>	vneg<c>.f32 <Sd>, <Sm>
		// xxxx11101x110001xxxx101x01x0xxxx
		new OpcodeARM(Index.arm_vneg__f, "vneg", "xxxx11101x110001xxxx101x01x0xxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.343 VNMLA, VNMLS, VNMUL
		// vnmla<c>.f64 <Dd>, <Dn>, <Dm>	vnmla<c>.f32 <Sd>, <Sn>, <Sm>
		// vnmls<c>.f64 <Dd>, <Dn>, <Dm>	vnmls<c>.f32 <Sd>, <Sn>, <Sm>
		// xxxx11100x01xxxxxxxx101xxxx0xxxx
		new OpcodeARM(Index.arm_vnml, "vnml", "xxxx11100x01xxxxxxxx101xxxx0xxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.343 VNMLA, VNMLS, VNMUL
		// vnmul<c>.f64 <Dd>, <Dn>, <Dm>	vnmul<c>.f32 <Sd>, <Sn>, <Sm>
		// xxxx11100x10xxxxxxxx101xx1x0xxxx
		new OpcodeARM(Index.arm_vnmul, "vnmul", "xxxx11100x10xxxxxxxx101xx1x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.347 VORR (register)
		// vorr<c> <Qd>, <Qn>, <Qm>	vorr<c> <Dd>, <Dn>, <Dm>
		// 111100100x10xxxxxxxx0001xxx1xxxx
		new OpcodeARM(Index.arm_vorr, "vorr", "111100100x10xxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.345 VORN (register)
		// vorn<c> <Qd>, <Qn>, <Qm>	vorn<c> <Dd>, <Dn>, <Dm>
		// 111100100x11xxxxxxxx0001xxx1xxxx
		new OpcodeARM(Index.arm_vorn, "vorn", "111100100x11xxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.349 VPADD (integer)
		// vpadd<c>.<dt> <Dd>, <Dn>, <Dm>
		// 111100100xxxxxxxxxxx1011xxx1xxxx
		new OpcodeARM(Index.arm_vpadd__int, "vpadd", "111100100xxxxxxxxxxx1011xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.350 VPADD (floating-point)
		// vpadd<c>.f32 <Dd>, <Dn>, <Dm>
		// 111100110x0xxxxxxxxx1101xxx0xxxx
		new OpcodeARM(Index.arm_vpadd__f32, "vpadd", "111100110x0xxxxxxxxx1101xxx0xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.352 VPMAX, VPMIN (integer)
		// vp<op><c>.<dt> <Dd>, <Dn>, <Dm>
		// 1111001x0xxxxxxxxxxx1010xxxxxxxx
		new OpcodeARM(Index.arm_vpmax_vpmin__int, "vp", "1111001x0xxxxxxxxxxx1010xxxxxxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.353 VPMAX, VPMIN (floating-point)
		// vp<op><c>.f32 <Dd>, <Dn>, <Dm>
		// 111100110xxxxxxxxxxx1111xxx0xxxx
		new OpcodeARM(Index.arm_vpmax_vpmin__fp, "vp", "111100110xxxxxxxxxxx1111xxx0xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.357 VQADD
		// vqadd<c>.<dt> <Qd>,<Qn>,<Qm>	vqadd<c>.<dt> <Dd>,<Dn>,<Dm>
		// 1111001x0xxxxxxxxxxx0000xxx1xxxx
		new OpcodeARM(Index.arm_vqadd, "vqadd", "1111001x0xxxxxxxxxxx0000xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.358 VQDMLAL, VQDMLSL
		// vqd<op><c>.<dt> <Qd>,<Dn>,<Dm>
		// 111100101xxxxxxxxxxx10x1x0x0xxxx
		new OpcodeARM(Index.arm_vqdml__vec, "vqdml", "111100101xxxxxxxxxxx10x1x0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.359 VQDMULH
		// vqdmulh<c>.<dt> <Qd>,<Qn>,<Qm>	vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm>
		// 111100100xxxxxxxxxxx1011xxx0xxxx
		new OpcodeARM(Index.arm_vqdmulh__vec, "vqdmulh", "111100100xxxxxxxxxxx1011xxx0xxxx"),
		// NEW - Encoding A2 / T2 (Q) Advanced SIMD
		// A8.6.359 VQDMULH
		// vqdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>	vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>
		// 1111001x1xxxxxxxxxxx1100x1x0xxxx
		new OpcodeARM(Index.arm_vqdmulh__scalar, "vqdmulh", "1111001x1xxxxxxxxxxx1100x1x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.360 VQDMULL
		// vqdmull<c>.<dt> <Qd>,<Dn>,<Dm>	vqdmull<c>.<dt> <Qd>,<Dn>,<Dm>
		// 111100101xxxxxxxxxxx1101x0x0xxxx
		new OpcodeARM(Index.arm_vqdmull__vec, "vqdmull", "111100101xxxxxxxxxxx1101x0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.363 VQRDMULH
		// vqrdmulh<c>.<dt> <Qd>,<Qn>,<Qm>	vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm>
		// 111100110xxxxxxxxxxx1011xxx0xxxx
		new OpcodeARM(Index.arm_vqrdmulh__vec, "vqrdmulh", "111100110xxxxxxxxxxx1011xxx0xxxx"),
		// NEW - Encoding A2 / T2 (Q) Advanced SIMD
		// A8.6.363 VQRDMULH
		// vqrdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>	vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>
		// 1111001x1xxxxxxxxxxx1101x1x0xxxx
		new OpcodeARM(Index.arm_vqrdmulh__scalar, "vqrdmulh", "1111001x1xxxxxxxxxxx1101x1x0xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.364 VQRSHL
		// vqrshl<c>.<type><size> <Qd>,<Qm>,<Qn>	vqrshl<c>.<type><size> <Dd>,<Dm>,<Dn>
		// 1111001x0xxxxxxxxxxx0101xxx1xxxx
		new OpcodeARM(Index.arm_vqrshl, "vqrshl", "1111001x0xxxxxxxxxxx0101xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.377 VRSHRN
		// vrshrn<c>.i<size> <Dd>, <Qm>, #<imm>
		// 111100101xxxxxxxxxxx100001x1xxxx
		// must precede arm_vqrshr in search table
		new OpcodeARM(Index.arm_vrshrn, "vrshrn", "111100101xxxxxxxxxxx100001x1xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.365 VQRSHRN, VQRSHRUN
		// vqrshr{u}n<c>.<type><size> <Dd>,<Qm>,#<imm>
		// 1111001x1xxxxxxxxxxx100x01x1xxxx
		// must follow arm_vrshrn in search table
		new OpcodeARM(Index.arm_vqrshr, "vqrshr", "1111001x1xxxxxxxxxxx100x01x1xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.366 VQSHL (register)
		// vqshl<c>.<type><size> <Qd>,<Qm>,<Qn>	vqshl<c>.<type><size> <Dd>,<Dm>,<Dn>
		// 1111001x0xxxxxxxxxxx0100xxx1xxxx
		new OpcodeARM(Index.arm_vqshl__reg, "vqshl", "1111001x0xxxxxxxxxxx0100xxx1xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.367 VQSHL, VQSHLU (immediate)
		// vqshl{u}<c>.<type><size> <Qd>,<Qm>,#<imm>	vqshl{u}<c>.<type><size> <Dd>,<Dm>,#<imm>
		// 1111001x1xxxxxxxxxxx011xxxx1xxxx
		new OpcodeARM(Index.arm_vqshl__imm, "vqshl", "1111001x1xxxxxxxxxxx011xxxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.386 VSHRN
		// vshrn<c>.i<size> <Dd>, <Qm>, #<imm>
		// 111100101xxxxxxxxxxx100000x1xxxx
		// must precede arm_vqshr in search table
		new OpcodeARM(Index.arm_vshrn, "vshrn", "111100101xxxxxxxxxxx100000x1xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.368 VQSHRN, VQSHRUN
		// vqshr{u}n<c>.<type><size> <Dd>,<Qm>,#<imm>
		// 1111001x1xxxxxxxxxxx100x00x1xxxx
		// must follow arm_vshrn in search table
		new OpcodeARM(Index.arm_vqshr, "vqshr", "1111001x1xxxxxxxxxxx100x00x1xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.369 VQSUB
		// vqsub<c>.<type><size> <Qd>, <Qn>, <Qm>	vqsub<c>.<type><size> <Dd>, <Dn>, <Dm>
		// 1111001x0xxxxxxxxxxx0010xxx1xxxx
		new OpcodeARM(Index.arm_vqsub, "vqsub", "1111001x0xxxxxxxxxxx0010xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.370 VRADDHN
		// vraddhn<c>.<dt> <Dd>, <Qn>, <Qm>
		// 111100111xxxxxxxxxxx0100x0x0xxxx
		new OpcodeARM(Index.arm_vraddhn, "vraddhn", "111100111xxxxxxxxxxx0100x0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.372 VRECPS
		// vrecps<c>.f32 <Qd>, <Qn>, <Qm>	vrecps<c>.f32 <Dd>, <Dn>, <Dm>
		// 111100100x0xxxxxxxxx1111xxx1xxxx
		new OpcodeARM(Index.arm_vrecps, "vrecps", "111100100x0xxxxxxxxx1111xxx1xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.374 VRHADD
		// vrhadd<c> <Qd>, <Qn>, <Qm>	vrhadd<c> <Dd>, <Dn>, <Dm>
		// 1111001x0xxxxxxxxxxx0001xxx0xxxx
		new OpcodeARM(Index.arm_vrhadd, "vrhadd", "1111001x0xxxxxxxxxxx0001xxx0xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.375 VRSHL
		// vrshl<c>.<type><size> <Qd>, <Qm>, <Qn>	vrshl<c>.<type><size> <Dd>, <Dm>, <Dn>
		// 1111001x0xxxxxxxxxxx0101xxx0xxxx
		new OpcodeARM(Index.arm_vrshl, "vrshl", "1111001x0xxxxxxxxxxx0101xxx0xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.376 VRSHR
		// vrshr<c>.<type><size> <Qd>, <Qm>, #<imm>	vrshr<c>.<type><size> <Dd>, <Dm>, #<imm>
		// 1111001x1xxxxxxxxxxx0010xxx1xxxx
		new OpcodeARM(Index.arm_vrshr, "vrshr", "1111001x1xxxxxxxxxxx0010xxx1xxxx"),
		// NEW - Encoding A2 / T2 (U) Advanced SIMD
		// A8.6.266 VABA, VABAL
		// vabal<c>.<dt> <Qd>, <Dn>, <Dm>
		// 1111001x1xxxxxxxxxxx0101x0x0xxxx
		// must follow arm_vceq__imm0
		// must follow arm_vcle
		// must follow arm_vcnt
		// must follow arm_vext
		// must follow arm_vrsqrte
		new OpcodeARM(Index.arm_vabal, "vabal", "1111001x1xxxxxxxxxxx0101x0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.379 VRSQRTS
		// vrsqrts<c>.f32 <Qd>, <Qn>, <Qm>	vrsqrts<c>.f32 <Dd>, <Dn>, <Dm>
		// 111100100x1xxxxxxxxx1111xxx1xxxx
		new OpcodeARM(Index.arm_vrsqrts, "vrsqrts", "111100100x1xxxxxxxxx1111xxx1xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.380 VRSRA
		// vrsra<c>.<type><size> <Qd>, <Qm>, #<imm>	vrsra<c>.<type><size> <Dd>, <Dm>, #<imm>
		// 1111001x1xxxxxxxxxxx0011xxx1xxxx
		new OpcodeARM(Index.arm_vrsra, "vrsra", "1111001x1xxxxxxxxxxx0011xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.381 VRSUBHN
		// vrsubhn<c>.<dt> <Dd>, <Qn>, <Qm>
		// 111100111xxxxxxxxxxx0110x0x0xxxx
		new OpcodeARM(Index.arm_vrsubhn, "vrsubhn", "111100111xxxxxxxxxxx0110x0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.382 VSHL (immediate)
		// vshl<c>.i<size> <Qd>, <Qm>, #<imm>	vshl<c>.i<size> <Dd>, <Dm>, #<imm>
		// 111100101xxxxxxxxxxx0101xxx1xxxx
		new OpcodeARM(Index.arm_vshl__imm, "vshl.i", "111100101xxxxxxxxxxx0101xxx1xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.383 VSHL (register)
		// vshl<c>.i<size> <Qd>, <Qm>, <Qn>	vshl<c>.i<size> <Dd>, <Dm>, <Dn>
		// 1111001x0xxxxxxxxxxx0100xxx0xxxx
		new OpcodeARM(Index.arm_vshl__reg, "vshl", "1111001x0xxxxxxxxxxx0100xxx0xxxx"),
		// NEW - Encoding A2 / T2 Advanced SIMD
		// A8.6.384 VSHLL
		// vshll<c>.<type><size> <Qd>, <Dm>, #<imm> (<imm> == <size>)
		// 111100111x11xx10xxxx001100x0xxxx
		new OpcodeARM(Index.arm_vshll__max, "vshll", "111100111x11xx10xxxx001100x0xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.385 VSHR
		// vshr<c>.<type><size> <Qd>, <Qm>, #<imm>	vshr<c>.<type><size> <Dd>, <Dm>, #<imm>
		// 1111001x1xxxxxxxxxxx0000xxx1xxxx
		new OpcodeARM(Index.arm_vshr, "vshr", "1111001x1xxxxxxxxxxx0000xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.387 VSLI
		// vsli<c>.<size> <Qd>, <Qm>, #<imm>	vsli<c>.<size> <Dd>, <Dm>, #<imm>
		// 111100111xxxxxxxxxxx0101xxx1xxxx
		new OpcodeARM(Index.arm_vsli, "vsli.", "111100111xxxxxxxxxxx0101xxx1xxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.388 VSQRT
		// vsqrt<c>.f64 <Dd>, <Dm>	vsqrt<c>.f32 <Sd>, <Sm>
		// xxxx11101x110001xxxx101x11x0xxxx
		new OpcodeARM(Index.arm_vsqrt, "vsqrt", "xxxx11101x110001xxxx101x11x0xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.389 VSRA
		// vsra<c>.<type><size> <Qd>, <Qm>, #<imm>	vsra<c>.<type><size> <Dd>, <Dm>, #<imm>
		// 1111001x1xxxxxxxxxxx0001xxx1xxxx
		new OpcodeARM(Index.arm_vsra, "vsra", "1111001x1xxxxxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.390 VSRI
		// vsri<c>.<size> <Qd>, <Qm>, #<imm>	vsri<c>.<size> <Dd>, <Dm>, #<imm>
		// 111100111xxxxxxxxxxx0100xxx1xxxx
		new OpcodeARM(Index.arm_vsri, "vsri.", "111100111xxxxxxxxxxx0100xxx1xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.391 VST1 (multiple single elements)
		// vst1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101000x00xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.393 VST2 (multiple 2-element structures)
		// vst2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101000x00xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.395 VST3 (multiple 3-element structures)
		// vst3<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst3<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101000x00xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.397 VST4 (multiple 4-element structures)
		// vst4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101000x00xxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_vst__multi, "vst", "111101000x00xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.392 VST1 (single element from one lane)
		// vst1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101001x00xxxxxxxxxx00xxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.394 VST2 (single 2-element structure from one lane)
		// vst2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101001x00xxxxxxxxxx01xxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.396 VST3 (single 3-element structure from one lane)
		// vst3<c>.<size> <list>, [<Rn>]{!}	vst3<c>.<size> <list>, [<Rn>], <Rm>
		// 111101001x00xxxxxxxxxx10xxxxxxxx
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.398 VST4 (single 4-element structure from one lane)
		// vst4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 111101001x00xxxxxxxxxx11xxxxxxxx
		// 
		new OpcodeARM(Index.arm_vst__xlane, "vst", "111101001x00xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.355 VPUSH
		// vpush<c> <list> <list> is consecutive 64-bit registers
		// xxxx11010x101101xxxx1011xxxxxxxx
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3
		// A8.6.355 VPUSH
		// vpush<c> <list> <list> is consecutive 32-bit registers
		// xxxx11010x101101xxxx1010xxxxxxxx
		// must precede arm_vstm_1 in search table
		// must precede arm_vstm_2 in search table
		new OpcodeARM(Index.arm_vpush, "vpush", "xxxx11010x101101xxxx101xxxxxxxxx"),
		// NEW - Encoding A1 / T1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.400 VSTR
		// vstr<c> <Dd>, [<Rn>{, #+/-<imm>}]
		// xxxx1101xx00xxxxxxxx1011xxxxxxxx
		// must precede arm_vstm_1 in search table
		new OpcodeARM(Index.arm_vstr__64, "vstr", "xxxx1101xx00xxxxxxxx1011xxxxxxxx"),
		// NEW - Encoding A1 / T1 (cond) Advanced SIMD
		// A8.6.399 VSTM
		// vstm{mode}<c> <Rn>{!}, <list> <list> is consecutive 64-bit registers
		// xxxx110xxxx0xxxxxxxx1011xxxxxxxx
		// must follow arm_vpush in search table
		// must follow arm_vstr_1 in search table
		new OpcodeARM(Index.arm_vstm__64, "vstm", "xxxx110xxxx0xxxxxxxx1011xxxxxxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3
		// A8.6.400 VSTR
		// vstr<c> <Sd>, [<Rn>{, #+/-<imm>}]
		// xxxx1101xx00xxxxxxxx1010xxxxxxxx
		// must precede arm_vstm_2 in search table
		new OpcodeARM(Index.arm_vstr__32, "vstr", "xxxx1101xx00xxxxxxxx1010xxxxxxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3
		// A8.6.399 VSTM
		// vstm{mode}<c> <Rn>{!}, <list> <list> is consecutive 32-bit registers
		// xxxx110xxxx0xxxxxxxx1010xxxxxxxx
		// must follow arm_vpush in search table
		// must follow arm_vstr_2 in search table
		new OpcodeARM(Index.arm_vstm__32, "vstm", "xxxx110xxxx0xxxxxxxx1010xxxxxxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.401 VSUB (integer)
		// vsub<c>.<dt> <Qd>, <Qn>, <Qm>	vsub<c>.<dt> <Dd>, <Dn>, <Dm>
		// 111100110xxxxxxxxxxx1000xxx0xxxx
		new OpcodeARM(Index.arm_vsub__int, "vsub", "111100110xxxxxxxxxxx1000xxx0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.402 VSUB (floating-point)
		// vsub<c>.f32 <Qd>, <Qn>, <Qm>	vsub<c>.f32 <Dd>, <Dn>, <Dm>
		// 111100100x1xxxxxxxxx1101xxx0xxxx
		new OpcodeARM(Index.arm_vsub__f32, "vsub", "111100100x1xxxxxxxxx1101xxx0xxxx"),
		// NEW - Encoding A2 / T2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.402 VSUB (floating-point)
		// vsub<c>.f64 <Dd>, <Dn>, <Dm>	vsub<c>.f32 <Sd>, <Sn>, <Sm>
		// xxxx11100x11xxxxxxxx101xx1x0xxxx
		new OpcodeARM(Index.arm_vsub__fp_f, "vsub", "xxxx11100x11xxxxxxxx101xx1x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.403 VSUBHN
		// vsubhn<c>.<dt> <Dd>, <Qn>, <Qm>
		// 111100101xxxxxxxxxxx0110x0x0xxxx
		new OpcodeARM(Index.arm_vsubhn, "vsubhn", "111100101xxxxxxxxxxx0110x0x0xxxx"),
		// NEW - Encoding A1 / T1 (U) Advanced SIMD
		// A8.6.404 VSUBL, VSUBW
		// vsubl<c>.<dt> <Qd>, <Dn>, <Dm>	vsubw<c>.<dt> {<Qd>,} <Qn>, <Dm>
		// 1111001x1xxxxxxxxxxx001xx0x0xxxx
		new OpcodeARM(Index.arm_vsubl_vsubw, "vsub", "1111001x1xxxxxxxxxxx001xx0x0xxxx"),
		// NEW - Encoding A1 / T1 Advanced SIMD
		// A8.6.408 VTST
		// vtst<c>.<size> <Qd>, <Qn>, <Qm>	vtst<c>.<size> <Dd>, <Dn>, <Dm>
		// 111100100xxxxxxxxxxx1000xxx1xxxx
		new OpcodeARM(Index.arm_vtst, "vtst", "111100100xxxxxxxxxxx1000xxx1xxxx"),

		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.19 BIC (immediate)
		// bic{s}<c> <Rd>,<Rn>,#<const>
		// xxxx0011110xxxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_bic__imm, "bic", "xxxx0011110xxxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.32 CMN (immediate)
		// cmn<c> <Rn>,#<const>
		// xxxx00110111xxxx(0)(0)(0)(0)xxxxxxxxxxxx
		new OpcodeARM(Index.arm_cmn__imm, "cmn", "xxxx00110111xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.35 CMP (immediate)
		// cmp<c> <Rn>,#<const>
		// xxxx00110101xxxx(0)(0)(0)(0)xxxxxxxxxxxx
		new OpcodeARM(Index.arm_cmp__imm, "cmp", "xxxx00110101xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv6T2, ARMv7
		// A8.6.99 MOVT
		// movt<c> <Rd>,#<imm16>
		// xxxx00110100xxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_movt, "movt", "xxxx00110100xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.103 MSR (immediate)
		// msr<c> <spec_reg>,#<const>
		// xxxx00110010xx00(1)(1)(1)(1)xxxxxxxxxxxx
		// must precede arm_msr__sys_imm in search table
		// must follow arm_nop in search table
		// must follow arm_sev in search table
		// must follow arm_wfe in search table
		// must follow arm_wfi in search table
		// must follow arm_yield in search table
		new OpcodeARM(Index.arm_msr__imm, "msr", "xxxx00110010xx00xxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// B6.1.6 MSR (immediate)
		// msr<c> <spec_reg>,#<const>
		// xxxx00110x10xxxx(1)(1)(1)(1)xxxxxxxxxxxx
		// must follow arm_msr__imm in table
		new OpcodeARM(Index.arm_msr__sys_imm, "msr", "xxxx00110x10xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.106 MVN (immediate)
		// mvn{s}<c> <Rd>,#<const>
		// xxxx0011111x(0)(0)(0)(0)xxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_mvn__imm, "mvn", "xxxx0011111xxxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.142 RSB (immediate)
		// rsb{s}<c> <Rd>,<Rn>,#<const>
		// xxxx0010011xxxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_rsb__imm, "rsb", "xxxx0010011xxxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.145 RSC (immediate)
		// rsc{s}<c> <Rd>,<Rn>,#<const>
		// xxxx0010111xxxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_rsc__imm, "rsc", "xxxx0010111xxxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.151 SBC (immediate)
		// sbc{s}<c> <Rd>,<Rn>,#<const>
		// xxxx0010110xxxxxxxxxxxxxxxxxxxxx
		new OpcodeARM(Index.arm_sbc__imm, "sbc", "xxxx0010110xxxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.199 STRBT
		// strbt<c> <Rt>,[<Rn>],#+/-<imm12>
		// xxxx0100x110xxxxxxxxxxxxxxxxxxxx
		// must precede arm_strb__imm in search table
		new OpcodeARM(Index.arm_strbt__imm, "strbt", "xxxx0100x110xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.197 STRB (immediate, ARM)
		// strb<c> <Rt>,[<Rn>{,#+/-<imm12>}]	strb<c> <Rt>,[<Rn>],#+/-<imm12>	strb<c> <Rt>,[<Rn>,#+/-<imm12>]!
		// xxxx010xx1x0xxxxxxxxxxxxxxxxxxxx
		// must follow arm_strbt__imm in search table
		new OpcodeARM(Index.arm_strb__imm, "strb", "xxxx010xx1x0xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.212 SUB (immediate, ARM)
		// sub{s}<c> <Rd>,<Rn>,#<const>
		// xxxx0010010xxxxxxxxxxxxxxxxxxxxx
		// must follow arm_adr__lower in search table
		new OpcodeARM(Index.arm_sub__imm, "sub", "xxxx0010010xxxxxxxxxxxxxxxxxxxxx"),

		// Coprocessor instructions
		
		// Encoding A2 / T2 ARMv5T*, ARMv6*, ARMv7
		// A8.6.28 CDP, CDP2
		// cdp2<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>
		// 11111110xxxxxxxxxxxxxxxxxxx0xxxx
		// must precede arm_cdp in the search table
		new OpcodeARM(Index.arm_cdp2, "cdp2", "11111110xxxxxxxxxxxxxxxxxxx0xxxx"),
		// Encoding A1 / T1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.28 CDP, CDP2
		// cdp<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>
		// xxxx1110xxxxxxxxxxxxxxxxxxx0xxxx
		// must follow arm_cdp2 in the search table
		new OpcodeARM(Index.arm_cdp, "cdp", "xxxx1110xxxxxxxxxxxxxxxxxxx0xxxx"),
		// Encoding A2 / T2 ARMv5T*, ARMv6*, ARMv7
		// A8.6.92 MCR, MCR2
		// mcr2<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// 11111110xxx0xxxxxxxxxxxxxxx1xxxx
		// must precede arm_mcr in the search table
		new OpcodeARM(Index.arm_mcr2, "mcr2", "11111110xxx0xxxxxxxxxxxxxxx1xxxx"),
		// Encoding A1 / T2 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.92 MCR, MCR2
		// mcr<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// xxxx1110xxx0xxxxxxxxxxxxxxx1xxxx
		// must follow arm_mcr2 in the search table
		new OpcodeARM(Index.arm_mcr, "mcr", "xxxx1110xxx0xxxxxxxxxxxxxxx1xxxx"),
		// Encoding A2 / T2 ARMv5T*, ARMv6*, ARMv7
		// A8.6.100 MRC, MRC2
		// mrc2<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// 11111110xxx1xxxxxxxxxxxxxxx1xxxx
		// must precede arm_mrc in the search table
		new OpcodeARM(Index.arm_mrc2, "mrc2", "11111110xxx1xxxxxxxxxxxxxxx1xxxx"),
		// Encoding A1 / T1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.100 MRC, MRC2
		// mrc<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// xxxx1110xxx1xxxxxxxxxxxxxxx1xxxx
		// must follow arm_mrc2 in the search table
		new OpcodeARM(Index.arm_mrc, "mrc", "xxxx1110xxx1xxxxxxxxxxxxxxx1xxxx"),
		// Encoding A2 / T1 ARMv6*, ARMv7
		// A8.6.101 MRRC, MRRC2
		// mrrc2<c> <coproc>,<opc>,<Rt>,<Rt2>,<CRm>
		// 111111000101xxxxxxxxxxxxxxxxxxxx
		// must precede arm_mrrc in the search table
		// must precede arm_ldc__lit in the search table
		// must precede arm_ldc__imm in the search table
		new OpcodeARM(Index.arm_mrrc2, "mrrc2", "111111000101xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 / T1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.101 MRRC, MRRC2
		// mrrc<c> <coproc>,<opc>,<Rt>,<Rt2>,<CRm>
		// xxxx11000101xxxxxxxxxxxxxxxxxxxx
		// must follow arm_mrrc2 in the search table
		// must precede arm_ldc__lit in the search table
		// must precede arm_ldc__imm in the search table
		new OpcodeARM(Index.arm_mrrc, "mrrc", "xxxx11000101xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding A2 / T2 ARMv5T*, ARMv6*, ARMv7
		// A8.6.52 LDC, LDC2 (literal)
		// ldc2{l}<c> <coproc>,<CRd>,<label>	ldc2{l}<c> <coproc>,<CRd>,[pc,#-0] Special case	ldc2{l}<c> <coproc>,<CRd>,[pc],<option>
		// 1111110xxxx11111xxxxxxxxxxxxxxxx
		// must precede arm_ldc__imm in search table
		// must follow arm_mrrc in search table
		// must follow arm_mrrc2 in search table
		new OpcodeARM(Index.arm_ldc2__lit, "ldc2", "1111110xxxx11111xxxxxxxxxxxxxxxx"),
		// NEW - Encoding A1 / T1 (cond) ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.52 LDC, LDC2 (literal)
		// ldc{l}<c> <coproc>,<CRd>,<label>	ldc{l}<c> <coproc>,<CRd>,[pc,#-0] Special case	ldc{l}<c> <coproc>,<CRd>,[pc],<option>
		// xxxx110xxxx11111xxxxxxxxxxxxxxxx
		// must follow arm_ldc2__lit in search table
		// must precede arm_ldc2__imm in search table
		new OpcodeARM(Index.arm_ldc__lit, "ldc", "xxxx110xxxx11111xxxxxxxxxxxxxxxx"),
		// Encoding A2 / T2 ARMv5T*, ARMv6*, ARMv7
		// A8.6.51 LDC, LDC2 (immediate)
		// ldc2{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	ldc2{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>	ldc2{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// 1111110xxxx1xxxxxxxxxxxxxxxxxxxx
		// must precede arm_ldc__lit in search table
		new OpcodeARM(Index.arm_ldc2__imm, "ldc2", "1111110xxxx1xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 / T1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.51 LDC, LDC2 (immediate)
		// ldc{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	ldc{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>	ldc{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// xxxx110xxxx1xxxxxxxxxxxxxxxxxxxx
		// must follow arm_ldc2__imm in search table
		// must follow arm_mrrc in search table
		// must follow arm_mrrc2 in search table
		new OpcodeARM(Index.arm_ldc__imm, "ldc", "xxxx110xxxx1xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A2 / T2 ARMv6*, ARMv7
		// A8.6.93 MCRR, MCRR2
		// mcrr2<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
		// 111111000100xxxxxxxxxxxxxxxxxxxx
		// must precede arm_mcrr in search table
		// must precede arm_stc in search table
		// must precede arm_stc2 in search table
		new OpcodeARM(Index.arm_mcrr2, "mcrr2", "111111000100xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 / T1 ARMv5TE*, ARMv6*, ARMv7
		// A8.6.93 MCRR, MCRR2
		// mcrr<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
		// xxxx11000100xxxxxxxxxxxxxxxxxxxx
		// must follow arm_mcrr2 in the search table
		// must precede arm_stc in search table
		// must precede arm_stc2 in search table
		new OpcodeARM(Index.arm_mcrr, "mcrr", "xxxx11000100xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A2 / T2 ARMv5T*, ARMv6*, ARMv7
		// A8.6.188 STC, STC2
		// stc2{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	stc2{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>	stc2{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// 1111110xxxx0xxxxxxxxxxxxxxxxxxxx
		// must precede stc in search table
		// must follow arm_ldc2__lit in search table
		// must follow arm_mcrr in search table
		// must follow arm_mcrr2 in search table
		new OpcodeARM(Index.arm_stc2, "stc2", "1111110xxxx0xxxxxxxxxxxxxxxxxxxx"),
		// Encoding A1 / T1 ARMv4*, ARMv5T*, ARMv6*, ARMv7
		// A8.6.188 STC, STC2
		// stc{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	stc{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>	stc{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// xxxx110xxxx0xxxxxxxxxxxxxxxxxxxx
		// must follow arm_ldc2__lit in search table
		// must follow arm_stc2 in search table
		// must follow arm_mcrr in search table
		// must follow arm_mcrr2 in search table
		new OpcodeARM(Index.arm_stc, "stc", "xxxx110xxxx0xxxxxxxxxxxxxxxxxxxx"),

		new OpcodeARM(Index.arm_undefined, "undefined", "111001111111xxxxxxxxxxxxxxx1xxxx"),
	};

	public static final OpcodeARM thumb_opcode_table[] = {

		new OpcodeARM(Index.thumb_undefined, "undefined", "11011110xxxxxxxx"),	// needs to precede 'b' since it overloads 'b' with invalid condition

		// Encoding T1 ARMv6*, ARMv7 if <Rd> and <Rm> both from R0-R7; ARMv4T, ARMv5T*, ARMv6*, ARMv7 otherwise
		// A8.6.97 MOV (register)
		// mov<c> <Rd>,<Rm> If <Rd> is the PC, must be outside or last in IT block.
		// 01000110xxxxxxxx
		new OpcodeARM(Index.thumb_mov__reg, "mov", "01000110xxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.2 ADC (register)
		// adcs <Rdn>,<Rm> Outside IT block.	adc<c> <Rdn>,<Rm> Inside IT block.
		// 0100000101xxxxxx
		new OpcodeARM(Index.thumb_adc, "adc", "0100000101xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.4 ADD (immediate, Thumb)
		// adds <Rd>,<Rn>,#<imm3> Outside IT block.	add<c> <Rd>,<Rn>,#<imm3> Inside IT block.
		// 0001110xxxxxxxxx
		new OpcodeARM(Index.thumb_add__reg_imm, "add", "0001110xxxxxxxxx"),
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.8 ADD (SP plus immediate)
		// add<c> sp,sp,#<imm>
		// 101100000xxxxxxx
		new OpcodeARM(Index.thumb_add__imm_to_sp, "add", "101100000xxxxxxx"),
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.4 ADD (immediate, Thumb)
		// adds <Rdn>,#<imm8> Outside IT block.	add<c> <Rdn>,#<imm8> Inside IT block.
		// 00110xxxxxxxxxxx
		new OpcodeARM(Index.thumb_add__imm, "add", "00110xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.6 ADD (register)
		// adds <Rd>,<Rn>,<Rm> Outside IT block.	add<c> <Rd>,<Rn>,<Rm> Inside IT block.
		// 0001100xxxxxxxxx
		new OpcodeARM(Index.thumb_add__reg_reg, "add", "0001100xxxxxxxxx"),
//		// NEW - Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
//		// A8.6.9 ADD (SP plus register)
//		// add<c> <Rdm>, sp, <Rdm>
//		// 01000100x1101xxx
// SEE thumb_add__reg
//		new OpcodeARM(Index.thumb_add__sp_reg, "add", "01000100x1101xxx"),
//		// NEW - Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
//		// A8.6.9 ADD (SP plus register)
//		// add<c> sp,<Rm>
//		// 010001001xxxx101
// SEE thumb_add__reg
//		new OpcodeARM(Index.thumb_add__reg_to_sp, "add", "010001001xxxx101"),
		// Encoding T2 ARMv6T2, ARMv7 if <Rdn> and <Rm> are both from R0-R7; ARMv4T, ARMv5T*, ARMv6*, ARMv7 otherwise
		// A8.6.6 ADD (register)
		// add<c> <Rdn>,<Rm> If <Rdn> is the PC, must be outside or last in IT block.
		// 01000100xxxxxxxx
		new OpcodeARM(Index.thumb_add__reg, "add", "01000100xxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.8 ADD (SP plus immediate)
		// add<c> <Rd>,sp,#<imm>
		// 10101xxxxxxxxxxx
		new OpcodeARM(Index.thumb_add__sp_imm, "add", "10101xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.10 ADR
		// adr<c> <Rd>,<label>	add <Rd>,pc,imm8		Alternative form
		// 10100xxxxxxxxxxx
		new OpcodeARM(Index.thumb_adr, "add", "10100xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.12 AND (register)
		// ands <Rdn>,<Rm> Outside IT block.	and<c> <Rdn>,<Rm> Inside IT block.
		// 0100000000xxxxxx
		new OpcodeARM(Index.thumb_and, "and", "0100000000xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.14 ASR (immediate)
		// asrs <Rd>,<Rm>,#<imm> Outside IT block.	asr<c> <Rd>,<Rm>,#<imm> Inside IT block.
		// 00010xxxxxxxxxxx
		new OpcodeARM(Index.thumb_asr__imm, "asr", "00010xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.15 ASR (register)
		// asrs <Rdn>,<Rm> Outside IT block.	asr<c> <Rdn>,<Rm> Inside IT block.
		// 0100000100xxxxxx
		new OpcodeARM(Index.thumb_asr__reg, "asr", "0100000100xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.218 SVC (previously SWI)
		// svc<c> #<imm8>
		// 11011111xxxxxxxx
		// must precede thumb_b_1 in search table
		new OpcodeARM(Index.thumb_svc, "svc", "11011111xxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.16 B
		// b<c> <label> Not permitted in IT block.
		// 1101xxxxxxxxxxxx
		// must follow thumb_b_1 in search table
		new OpcodeARM(Index.thumb_b_1, "b", "1101xxxxxxxxxxxx"),
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.16 B
		// b<c> <label> Outside or last in IT block
		// 11100xxxxxxxxxxx
		new OpcodeARM(Index.thumb_b_2, "b", "11100xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.20 BIC (register)
		// bics <Rdn>,<Rm> Outside IT block.	bic<c> <Rdn>,<Rm> Inside IT block.
		// 0100001110xxxxxx
		new OpcodeARM(Index.thumb_bic, "bic", "0100001110xxxxxx"),
		// Encoding T1 ARMv5T*, ARMv6*, ARMv7
		// A8.6.22 BKPT
		// bkpt #<imm8>
		// 10111110xxxxxxxx
		new OpcodeARM(Index.thumb_bkpt, "bkpt", "10111110xxxxxxxx"),
		// Encoding T1 ARMv5T*, ARMv6*, ARMv7
		// A8.6.24 BLX (register)
		// blx<c> <Rm> Outside or last in IT block
		// 010001111xxxx(0)(0)(0)
		new OpcodeARM(Index.thumb_blx, "blx", "010001111xxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.25 BX
		// bx<c> <Rm> Outside or last in IT block
		// 010001110xxxx(0)(0)(0)
		new OpcodeARM(Index.thumb_bx, "bx", "010001110xxxxxxx"),
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.27 CBNZ, CBZ
		// cb{n}z <Rn>,<label> Not permitted in IT block.
		// 1011x0x1xxxxxxxx
		new OpcodeARM(Index.thumb_cbnz_cbz, "cb", "1011x0x1xxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.33 CMN (register)
		// cmn<c> <Rn>,<Rm>
		// 0100001011xxxxxx
		new OpcodeARM(Index.thumb_cmn, "cmn", "0100001011xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.35 CMP (immediate)
		// cmp<c> <Rn>,#<imm8>
		// 00101xxxxxxxxxxx
		new OpcodeARM(Index.thumb_cmp__imm, "cmp", "00101xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.36 CMP (register)
		// cmp<c> <Rn>,<Rm> <Rn> and <Rm> both from R0-R7
		// 0100001010xxxxxx
		new OpcodeARM(Index.thumb_cmp__reg, "cmp", "0100001010xxxxxx"),
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.36 CMP (register)
		// cmp<c> <Rn>,<Rm> <Rn> and <Rm> not both from R0-R7
		// 01000101xxxxxxxx
		new OpcodeARM(Index.thumb_cmp__reg_hi, "cmp", "01000101xxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.45 EOR (register)
		// eors <Rdn>,<Rm> Outside IT block.	eor<c> <Rdn>,<Rm> Inside IT block.
		// 0100000001xxxxxx
		new OpcodeARM(Index.thumb_eor, "eor", "0100000001xxxxxx"),
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.110 NOP
		// nop<c>
		// 1011111100000000
		// must precede thumb_it in search table
		new OpcodeARM(Index.thumb_nop, "nop", "1011111100000000"),
		// NEW - Encoding T1 ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.158 SEV
		// sev<c>
		// 1011111101000000
		// must precede thumb_it in search table
		new OpcodeARM(Index.thumb_sev, "sev", "1011111101000000"),
		// NEW - Encoding T1 ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.411 WFE
		// wfe<c>
		// 1011111100100000
		// must precede thumb_it in search table
		new OpcodeARM(Index.thumb_wfe, "wfe", "1011111100100000"),
		// NEW - Encoding T1 ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.412 WFI
		// wfi<c>
		// 1011111100110000
		// must precede thumb_it in search table
		new OpcodeARM(Index.thumb_wfi, "wfi", "1011111100110000"),
		// NEW - Encoding T1 ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.413 YIELD
		// yield<c>
		// 1011111100010000
		// must precede thumb_it in search table
		new OpcodeARM(Index.thumb_yield, "yield", "1011111100010000"),
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.50 IT
		// it{x{y{z}}} <firstcond> Not permitted in IT block
		// 10111111xxxxxxxx
		// must follow thumb_nop in search table 
		// must follow thumb_sev in search table 
		// must follow thumb_wfe in search table 
		// must follow thumb_wfi in search table 
		// must follow thumb_yield in search table 
		new OpcodeARM(Index.thumb_it, "it", "10111111xxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7 (not in ThumbEE)
		// A8.6.53 LDM / LDMIA / LDMFD
		// ldm<c> <Rn>!,<registers> <Rn> not included in <registers>	ldm<c> <Rn>,<registers> <Rn> included in <registers>
		// 11001xxxxxxxxxxx
		new OpcodeARM(Index.thumb_ldm, "ldm", "11001xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.57 LDR (immediate, Thumb)
		// ldr<c> <Rt>, [<Rn>{,#<imm>}]
		// 01101xxxxxxxxxxx
		new OpcodeARM(Index.thumb_ldr__imm, "ldr", "01101xxxxxxxxxxx"),
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.57 LDR (immediate, Thumb)
		// ldr<c> <Rt>,[sp{,#<imm>}]
		// 10011xxxxxxxxxxx
		new OpcodeARM(Index.thumb_ldr__imm_sp, "ldr", "10011xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.59 LDR (literal)
		// ldr<c> <Rt>,<label>
		// 01001xxxxxxxxxxx
		new OpcodeARM(Index.thumb_ldr__lit, "ldr", "01001xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.60 LDR (register)
		// ldr<c> <Rt>,[<Rn>,<Rm>]
		// 0101100xxxxxxxxx
		new OpcodeARM(Index.thumb_ldr__reg, "ldr", "0101100xxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.61 LDRB (immediate, Thumb)
		// ldrb<c> <Rt>,[<Rn>{,#<imm5>}]
		// 01111xxxxxxxxxxx
		new OpcodeARM(Index.thumb_ldrb__imm, "ldrb", "01111xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.64 LDRB (register)
		// ldrb<c> <Rt>,[<Rn>,<Rm>]
		// 0101110xxxxxxxxx
		new OpcodeARM(Index.thumb_ldrb__reg, "ldrb", "0101110xxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.73 LDRH (immediate, Thumb)
		// ldrh<c> <Rt>,[<Rn>{,#<imm>}]
		// 10001xxxxxxxxxxx
		new OpcodeARM(Index.thumb_ldrh__imm, "ldrh", "10001xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.76 LDRH (register)
		// ldrh<c> <Rt>,[<Rn>,<Rm>]
		// 0101101xxxxxxxxx
		new OpcodeARM(Index.thumb_ldrh__reg, "ldrh", "0101101xxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.80 LDRSB (register)
		// ldrsb<c> <Rt>,[<Rn>,<Rm>]
		// 0101011xxxxxxxxx
		new OpcodeARM(Index.thumb_ldrsb, "ldrsb", "0101011xxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.84 LDRSH (register)
		// ldrsh<c> <Rt>,[<Rn>,<Rm>]
		// 0101111xxxxxxxxx
		new OpcodeARM(Index.thumb_ldrsh, "ldrsh", "0101111xxxxxxxxx"),
		// NEW - Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.97 MOV (register)
		// movs <Rd>,<Rm> Not permitted in IT block
		// 0000000000xxxxxx
		// must precede thumb_lsl__imm in search table
		new OpcodeARM(Index.thumb_movs, "movs", "0000000000xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.88 LSL (immediate)
		// lsls <Rd>,<Rm>,#<imm5> Outside IT block.	lsl<c> <Rd>,<Rm>,#<imm5> Inside IT block.
		// 00000xxxxxxxxxxx
		// must follow thumb_movs in search table
		new OpcodeARM(Index.thumb_lsl__imm, "lsl", "00000xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.89 LSL (register)
		// lsls <Rdn>,<Rm> Outside IT block.	lsl<c> <Rdn>,<Rm> Inside IT block.
		// 0100000010xxxxxx
		new OpcodeARM(Index.thumb_lsl__reg, "lsl", "0100000010xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.90 LSR (immediate)
		// lsrs <Rd>,<Rm>,#<imm> Outside IT block.	lsr<c> <Rd>,<Rm>,#<imm> Inside IT block.
		// 00001xxxxxxxxxxx
		new OpcodeARM(Index.thumb_lsr__imm, "lsr", "00001xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.91 LSR (register)
		// lsrs <Rdn>,<Rm> Outside IT block.	lsr<c> <Rdn>,<Rm> Inside IT block.
		// 0100000011xxxxxx
		new OpcodeARM(Index.thumb_lsr__reg, "lsr", "0100000011xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.96 MOV (immediate)
		// movs <Rd>,#<imm8> Outside IT block.	mov<c> <Rd>,#<imm8> Inside IT block.
		// 00100xxxxxxxxxxx
		new OpcodeARM(Index.thumb_mov__imm, "mov", "00100xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.105 MUL
		// muls <Rdm>,<Rn>,<Rdm> Outside IT block.	mul<c> <Rdm>,<Rn>,<Rdm> Inside IT block.
		// 0100001101xxxxxx
		new OpcodeARM(Index.thumb_mul, "mul", "0100001101xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.107 MVN (register)
		// mvns <Rd>,<Rm> Outside IT block.	mvn<c> <Rd>,<Rm> Inside IT block.
		// 0100001111xxxxxx
		new OpcodeARM(Index.thumb_mvn, "mvn", "0100001111xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.114 ORR (register)
		// orrs <Rdn>,<Rm> Outside IT block.	orr<c> <Rdn>,<Rm> Inside IT block.
		// 0100001100xxxxxx
		new OpcodeARM(Index.thumb_orr, "orr", "0100001100xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.122 POP
		// pop<c> <registers>
		// 1011110xxxxxxxxx
		new OpcodeARM(Index.thumb_pop, "pop", "1011110xxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.123 PUSH
		// push<c> <registers>
		// 1011010xxxxxxxxx
		new OpcodeARM(Index.thumb_push, "push", "1011010xxxxxxxxx"),
		// Encoding T1 ARMv6*, ARMv7
		// A8.6.135 REV
		// rev<c> <Rd>,<Rm>
		// 1011101000xxxxxx
		new OpcodeARM(Index.thumb_rev, "rev", "1011101000xxxxxx"),
		// Encoding T1 ARMv6*, ARMv7
		// A8.6.136 REV16
		// rev16<c> <Rd>,<Rm>
		// 1011101001xxxxxx
		new OpcodeARM(Index.thumb_rev16, "rev16", "1011101001xxxxxx"),
		// Encoding T1 ARMv6*, ARMv7
		// A8.6.137 REVSH
		// revsh<c> <Rd>,<Rm>
		// 1011101011xxxxxx
		new OpcodeARM(Index.thumb_revsh, "revsh", "1011101011xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.140 ROR (register)
		// rors <Rdn>,<Rm> Outside IT block.	ror<c> <Rdn>,<Rm> Inside IT block.
		// 0100000111xxxxxx
		new OpcodeARM(Index.thumb_ror, "ror", "0100000111xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.142 RSB (immediate)
		// rsbs <Rd>,<Rn>,#0 Outside IT block.	rsb<c> <Rd>,<Rn>,#0 Inside IT block.
		// 0100001001xxxxxx
		new OpcodeARM(Index.thumb_rsb, "rsb", "0100001001xxxxxx"),
		// NEW - Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.152 SBC (register)
		// sbcs <Rdn>,<Rm> Outside IT block.	sbc<c> <Rdn>,<Rm> Inside IT block.
		// 0100000110xxxxxx
		new OpcodeARM(Index.thumb_sbc, "sbc", "0100000110xxxxxx"),
		// Encoding T1 ARMv6*, ARMv7
		// A8.6.157 SETEND
		// setend <endian_specifier> Not permitted in IT block
		// 10110110010(1)x(0)(0)(0)
		new OpcodeARM(Index.thumb_setend, "setend", "10110110010xxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7 (not in ThumbEE)
		// A8.6.189 STM / STMIA / STMEA
		// stm<c> <Rn>!,<registers>
		// 11000xxxxxxxxxxx
		new OpcodeARM(Index.thumb_stm, "stm", "11000xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.193 STR (immediate, Thumb)
		// str<c> <Rt>, [<Rn>{,#<imm>}]
		// 01100xxxxxxxxxxx
		new OpcodeARM(Index.thumb_str__imm, "str", "01100xxxxxxxxxxx"),
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.193 STR (immediate, Thumb)
		// str<c> <Rt>,[sp,#<imm>]
		// 10010xxxxxxxxxxx
		new OpcodeARM(Index.thumb_str__imm_sp, "str", "10010xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.195 STR (register)
		// str<c> <Rt>,[<Rn>,<Rm>]
		// 0101000xxxxxxxxx
		new OpcodeARM(Index.thumb_str__reg, "str", "0101000xxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.196 STRB (immediate, Thumb)
		// strb<c> <Rt>,[<Rn>,#<imm5>]
		// 01110xxxxxxxxxxx
		new OpcodeARM(Index.thumb_strb__imm, "strb", "01110xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.198 STRB (register)
		// strb<c> <Rt>,[<Rn>,<Rm>]
		// 0101010xxxxxxxxx
		new OpcodeARM(Index.thumb_strb__reg, "strb", "0101010xxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.206 STRH (immediate, Thumb)
		// strh<c> <Rt>,[<Rn>{,#<imm>}]
		// 10000xxxxxxxxxxx
		new OpcodeARM(Index.thumb_strh__imm, "strh", "10000xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.208 STRH (register)
		// strh<c> <Rt>,[<Rn>,<Rm>]
		// 0101001xxxxxxxxx
		new OpcodeARM(Index.thumb_strh__reg, "strh", "0101001xxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.211 SUB (immediate, Thumb)
		// subs <Rd>,<Rn>,#<imm3> Outside IT block.	sub<c> <Rd>,<Rn>,#<imm3> Inside IT block.
		// 0001111xxxxxxxxx
		new OpcodeARM(Index.thumb_sub__reg_imm, "sub", "0001111xxxxxxxxx"),
		// Encoding T2 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.211 SUB (immediate, Thumb)
		// subs <Rdn>,#<imm8> Outside IT block.	sub<c> <Rdn>,#<imm8> Inside IT block.
		// 00111xxxxxxxxxxx
		new OpcodeARM(Index.thumb_sub__imm, "sub", "00111xxxxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.213 SUB (register)
		// subs <Rd>,<Rn>,<Rm> Outside IT block.	sub<c> <Rd>,<Rn>,<Rm> Inside IT block.
		// 0001101xxxxxxxxx
		new OpcodeARM(Index.thumb_sub__reg_reg, "sub", "0001101xxxxxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.215 SUB (SP minus immediate)
		// sub<c> sp,sp,#<imm>
		// 101100001xxxxxxx
		new OpcodeARM(Index.thumb_sub__imm_from_sp, "sub", "101100001xxxxxxx"),
		// Encoding T1 ARMv6*, ARMv7
		// A8.6.223 SXTB
		// sxtb<c> <Rd>,<Rm>
		// 1011001001xxxxxx
		new OpcodeARM(Index.thumb_sxtb, "sxtb", "1011001001xxxxxx"),
		// Encoding T1 ARMv6*, ARMv7
		// A8.6.225 SXTH
		// sxth<c> <Rd>,<Rm>
		// 1011001000xxxxxx
		new OpcodeARM(Index.thumb_sxth, "sxth", "1011001000xxxxxx"),
		// Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7
		// A8.6.231 TST (register)
		// tst<c> <Rn>,<Rm>
		// 0100001000xxxxxx
		new OpcodeARM(Index.thumb_tst, "tst", "0100001000xxxxxx"),
		// Encoding T1 ARMv6*, ARMv7
		// A8.6.263 UXTB
		// uxtb<c> <Rd>,<Rm>
		// 1011001011xxxxxx
		new OpcodeARM(Index.thumb_uxtb, "uxtb", "1011001011xxxxxx"),
		// Encoding T1 ARMv6*, ARMv7
		// A8.6.265 UXTH
		// uxth<c> <Rd>,<Rm>
		// 1011001010xxxxxx
		new OpcodeARM(Index.thumb_uxth, "uxth", "1011001010xxxxxx"),
		// Encoding T1 ARMv6*, ARMv7
		// B6.1.1 CPS
		// cps<effect> <iflags> Not permitted in IT block.
		// 10110110011x(0)xxx
		new OpcodeARM(Index.thumb_cps, "cps", "10110110011xxxxx"),
	};

	public static final OpcodeARM thumb2_opcode_table[] = {

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.1 ADC (immediate)
		// adc{s}<c> <Rd>,<Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 1 0 1 0 S_1_4_4 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x01010xxxxx0xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_adc__imm, "adc", "11110x01010xxxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.2 ADC (register)
		// adc{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 1 0 1 0 S_1_4_4 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101011010xxxxx(0)xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_adc__reg, "adc", "11101011010xxxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.10 ADR
		// adr<c>.w <Rd>,<label>
		// 1 1 1 1 0 i_1_10_10 1 0 0 0 0 0 1 1 1 1 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x10000011110xxxxxxxxxxxxxxx
		// must precede thumb2_addw in search table
		new OpcodeARM(Index.thumb2_adr__add, "add", "11110x10000011110xxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.32 CMN (immediate)
		// cmn<c> <Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 1 0 0 0 1 Rn_1_3_0 0 imm3_0_14_12 1 1 1 1 imm8_0_7_0
		// 11110x010001xxxx0xxx1111xxxxxxxx
		// must precede thumb2_add__imm in search table
		// must precede thumb2_addw__imm in search table
		new OpcodeARM(Index.thumb2_cmn__imm, "cmn", "11110x010001xxxx0xxx1111xxxxxxxx"),

		// NEW - Encoding T4 ARMv6T2, ARMv7
		// A8.6.4 ADD (immediate, Thumb)
		// addw<c> <Rd>,<Rn>,#<imm12>
		// 1 1 1 1 0 i_1_10_10 1 0 0 0 0 0 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x100000xxxx0xxxxxxxxxxxxxxx
		// NEW - Encoding T4 ARMv6T2, ARMv7
		// A8.6.8 ADD (SP plus immediate)
		// addw <Rd>,sp,#<imm12>
		// 1 1 1 1 0 i_1_10_10 1 0 0 0 0 0 1 1 0 1 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x10000011010xxxxxxxxxxxxxxx
		//
		// must follow thumb2_adr__add in search table
		// must follow thumb2_cmn__imm in search table
		// must precede thumb2_add__imm in search table
		new OpcodeARM(Index.thumb2_addw, "addw", "11110x100000xxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.4 ADD (immediate, Thumb)
		// add{s}<c>.w <Rd>,<Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 1 0 0 0 S_1_4_4 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x01000xxxxx0xxxxxxxxxxxxxxx
		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.8 ADD (SP plus immediate)
		// add{s}.w <Rd>,sp,#<const>
		// 1 1 1 1 0 i_1_10_10 0 1 0 0 0 S_1_4_4 1 1 0 1 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x01000x11010xxxxxxxxxxxxxxx
		//
		// must follow thumb2_addw__imm in search table
		// must follow thumb2_cmn__imm in search table
		new OpcodeARM(Index.thumb2_add__imm, "add", "11110x01000xxxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.33 CMN (register)
		// cmn<c>.w <Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 1 0 0 0 1 Rn_1_3_0 (0) imm3_0_14_12 1 1 1 1 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 111010110001xxxx(0)xxx1111xxxxxxxx
		// must precede thumb2_add__reg in search table
		new OpcodeARM(Index.thumb2_cmn__reg, "cmn.w", "111010110001xxxxxxxx1111xxxxxxxx"),

		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.6 ADD (register)
		// add{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 1 0 0 0 S_1_4_4 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101011000xxxxx(0)xxxxxxxxxxxxxxx
		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.9 ADD (SP plus register)
		// add{s}<c>.w <Rd>,sp,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 1 0 0 0 S_1_4_4 1 1 0 1 0 imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101011000x11010xxxxxxxxxxxxxxx
		//
		// must follow thumb2_cmn__reg in search table
		new OpcodeARM(Index.thumb2_add__reg, "add", "11101011000xxxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.10 ADR
		// adr<c>.w <Rd>,<label>	sub <Rd>,pc,#0 Special case for subtraction of zero
		// 1 1 1 1 0 i_1_10_10 1 0 1 0 1 0 1 1 1 1 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x10101011110xxxxxxxxxxxxxxx
		// must precede thumb2_subw in search table
		new OpcodeARM(Index.thumb2_adr__sub, "sub", "11110x10101011110xxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.230 TST (immediate)
		// tst<c> <Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 0 0 0 0 1 Rn_1_3_0 0 imm3_0_14_12 1 1 1 1 imm8_0_7_0
		// 11110x000001xxxx0xxx1111xxxxxxxx
		// must precede thumb2_and__imm in search table
		new OpcodeARM(Index.thumb2_tst__imm, "tst", "11110x000001xxxx0xxx1111xxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.11 AND (immediate)
		// and{s}<c> <Rd>,<Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 0 0 0 0 S_1_4_4 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x00000xxxxx0xxxxxxxxxxxxxxx
		// must follow thumb2_and__imm in search table
		new OpcodeARM(Index.thumb2_and__imm, "and", "11110x00000xxxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.231 TST (register)
		// tst<c>.w <Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 0 0 0 0 1 Rn_1_3_0 (0) imm3_0_14_12 1 1 1 1 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 111010100001xxxx(0)xxx1111xxxxxxxx
		// must precede thumb2_and__reg in search table
		new OpcodeARM(Index.thumb2_tst__reg, "tst.w", "111010100001xxxxxxxx1111xxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.12 AND (register)
		// and{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 0 0 0 0 S_1_4_4 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101010000xxxxx(0)xxxxxxxxxxxxxxx
		// must follow thumb2_and__reg in search table
		new OpcodeARM(Index.thumb2_and__reg, "and", "11101010000xxxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.14 ASR (immediate)
		// asr{s}<c>.w <Rd>,<Rm>,#<imm>
		// 1 1 1 0 1 0 1 0 0 1 0 S_1_4_4 1 1 1 1 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 1 0 Rm_0_3_0
		// 11101010010x1111(0)xxxxxxxxx10xxxx
		new OpcodeARM(Index.thumb2_asr__imm, "asr", "11101010010x1111xxxxxxxxxx10xxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.15 ASR (register)
		// asr{s}<c>.w <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 0 1 0 S_1_4_4 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 11111010010xxxxx1111xxxx0000xxxx
		new OpcodeARM(Index.thumb2_asr__reg, "asr", "11111010010xxxxx1111xxxx0000xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.26 BXJ
		// bxj<c> <Rm> Outside or last in IT block
		// 1 1 1 1 0 0 1 1 1 1 0 0 Rm_1_3_0 1 0 (0) 0 (1)(1)(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)
		// 111100111100xxxx10(0)0(1)(1)(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)
		// must precede thumb2_b__cond in search table
		new OpcodeARM(Index.thumb2_bxj, "bxj", "111100111100xxxx10x0xxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv7
		// A8.6.30 CLREX
		// clrex<c>
		// 1 1 1 1 0 0 1 1 1 0 1 1 (1)(1)(1) (1) 1 0 (0) 0 (1)(1)(1)(1) 0 0 1 0 (1)(1)(1)(1)
		// 111100111011(1)(1)(1)(1)10(0)0(1)(1)(1)(1)0010(1)(1)(1)(1)
		// must precede thumb2_b__cond in search table
		new OpcodeARM(Index.thumb2_clrex, "clrex", "111100111011xxxx10x0xxxx0010xxxx"),

		// NEW - Encoding T1 ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.40 DBG
		// dbg<c> #<option>
		// 1 1 1 1 0 0 1 1 1 0 1 0 (1)(1)(1) (1) 1 0 (0) 0 (0) 0 0 0 1 1 1 1 option
		// 111100111010(1)(1)(1)(1)10(0)0(0)0001111xxxx
		// must precede thumb2_b__cond in search table
		// must precede thumb2_cps in search table
		new OpcodeARM(Index.thumb2_dbg, "dbg", "111100111010xxxx10x0x0001111xxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.110 NOP
		// nop<c>.w
		// 1 1 1 1 0 0 1 1 1 0 1 0 (1)(1)(1) (1) 1 0 (0) 0 (0) 0 0 0 0 0 0 0 0 0 0 0
		// 111100111010(1)(1)(1)(1)10(0)0(0)00000000000
		// must precede thumb2_b__cond in search table
		// must precede thumb2_cps in search table
		new OpcodeARM(Index.thumb2_nop, "nop.w", "111100111010xxxx10x0x00000000000"),

		// NEW - Encoding T2 ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.158 SEV
		// sev<c>.w
		// 1 1 1 1 0 0 1 1 1 0 1 0 (1)(1)(1) (1) 1 0 (0) 0 (0) 0 0 0 0 0 0 0 0 1 0 0
		// 111100111010(1)(1)(1)(1)10(0)0(0)00000000100
		// must precede thumb2_b__cond in search table
		// must precede thumb2_cps in search table
		new OpcodeARM(Index.thumb2_sev, "sev.w", "111100111010xxxx10x0x00000000100"),

		// NEW - Encoding T1 Security Extensions (not in ARMv6K)
		// B6.1.9 SMC (previously SMI)
		// smc<c> #<imm4>
		// 1 1 1 1 0 1 1 1 1 1 1 1 imm4_1_3_0 1 0 0 0 (0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)
		// 111101111111xxxx1000(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_smc, "smc", "111101111111xxxx1000xxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.411 WFE
		// wfe<c>.w
		// 1 1 1 1 0 0 1 1 1 0 1 0 (1)(1)(1) (1) 1 0 (0) 0 (0) 0 0 0 0 0 0 0 0 0 1 0
		// 111100111010(1)(1)(1)(1)10(0)0(0)00000000010
		// must precede thumbw_b__cond in search table
		// must precede thumb2_cps in search table
		new OpcodeARM(Index.thumb2_wfe, "wfe.w", "111100111010xxxx10x0x00000000010"),

		// NEW - Encoding T2 ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.412 WFI
		// wfi<c>.w
		// 1 1 1 1 0 0 1 1 1 0 1 0 (1)(1)(1) (1) 1 0 (0) 0 (0) 0 0 0 0 0 0 0 0 0 1 1
		// 111100111010(1)(1)(1)(1)10(0)0(0)00000000011
		// must precede thumbw_b__cond in search table
		// must precede thumb2_cps in search table
		new OpcodeARM(Index.thumb2_wfi, "wfi.w", "111100111010xxxx10x0x00000000011"),

		// NEW - Encoding T2 ARMv7 (executes as NOP in ARMv6T2)
		// A8.6.413 YIELD
		// yield<c>.w
		// 1 1 1 1 0 0 1 1 1 0 1 0 (1)(1)(1) (1) 1 0 (0) 0 (0) 0 0 0 0 0 0 0 0 0 0 1
		// 111100111010(1)(1)(1)(1)10(0)0(0)00000000001
		// must precede thumbw_b__cond in search table
		// must precede thumb2_cps in search table
		new OpcodeARM(Index.thumb2_yield, "yield.w", "111100111010xxxx10x0x00000000001"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// B6.1.1 CPS
		// cps<effect>.w <iflags>{,#<mode>} Not permitted in IT block.	cps #<mode> Not permitted in IT block.
		// 1 1 1 1 0 0 1 1 1 0 1 0 (1)(1)(1) (1) 1 0 (0) 0 (0) imod_0_10_9 M_0_8_8 A_0_7_7 I_0_6_6 F_0_5_5 mode_0_4_0
		// 111100111010(1)(1)(1)(1)10(0)0(0)xxxxxxxxxxx
		// must follow thumb2_dbg in search table
		// must follow thumb2_nop in search table
		// must follow thumb2_sev in search table
		// must follow thumb2_wfe in search table
		// must follow thumb2_wfi in search table
		// must follow thumb2_yield in search table
		// must precede thumb2_b__cond in search table
		new OpcodeARM(Index.thumb2_cps, "cps", "111100111010xxxx10x0xxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv7
		// A8.6.41 DMB
		// dmb<c> #<option>
		// 1 1 1 1 0 0 1 1 1 0 1 1 (1)(1)(1) (1) 1 0 (0) 0 (1)(1)(1)(1) 0 1 0 1 option_0_3_0
		// 111100111011(1)(1)(1)(1)10(0)0(1)(1)(1)(1)0101xxxx
		// must precede thumb2_b__cond in search table
		new OpcodeARM(Index.thumb2_dmb, "dmb", "111100111011xxxx10x0xxxx0101xxxx"),

		// NEW - Encoding T1 ARMv7
		// A8.6.42 DSB
		// dsb<c> #<option>
		// 1 1 1 1 0 0 1 1 1 0 1 1 (1)(1)(1) (1) 1 0 (0) 0 (1)(1)(1)(1) 0 1 0 0 option_0_3_0
		// 111100111011(1)(1)(1)(1)10(0)0(1)(1)(1)(1)0100xxxx
		// must precede thumb2_b__cond in search table
		new OpcodeARM(Index.thumb2_dsb, "dsb", "111100111011xxxx10x0xxxx0100xxxx"),

		// NEW - Encoding T1 ARMv7
		// A8.6.49 ISB
		// isb<c> #<option>
		// 1 1 1 1 0 0 1 1 1 0 1 1 (1)(1)(1) (1) 1 0 (0) 0 (1)(1)(1)(1) 0 1 1 0 option_0_3_0
		// 111100111011(1)(1)(1)(1)10(0)0(1)(1)(1)(1)0110xxxx
		// must precede thumb2_b__cond in search table
		new OpcodeARM(Index.thumb2_isb, "isb", "111100111011xxxx10x0xxxx0110xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.102 MRS
		// mrs<c> <Rd>,<spec_reg>
		// 1 1 1 1 0 0 1 1 1 1 1 0 (1)(1)(1) (1) 1 0 (0) 0 Rd_0_11_8 (0)(0)(0)(0)(0)(0)(0)(0)
		// 111100111110(1)(1)(1)(1)10(0)0xxxx(0)(0)(0)(0)(0)(0)(0)(0)
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// B6.1.5 MRS
		// mrs<c> <Rd>,<spec_reg>
		// 1 1 1 1 0 0 1 1 1 1 1 R_1_4_4 (1)(1)(1) (1) 1 0 (0) 0 Rd_0_11_8 (0)(0)(0)(0)(0)(0)(0)(0)
		// 11110011111x(1)(1)(1)(1)10(0)0xxxx(0)(0)(0)(0)(0)(0)(0)(0)
		// must precede thumb2_b__cond in search table
		new OpcodeARM(Index.thumb2_mrs, "mrs", "11110011111xxxxx10x0xxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.104 MSR (register)
		// msr<c> <spec_reg>,<Rn>
		// 1 1 1 1 0 0 1 1 1 0 0 0 Rn_1_3_0 1 0 (0) 0 mask_0_11_10 0 0 (0)(0)(0)(0)(0)(0)(0)(0)
		// 111100111000xxxx10(0)0xx00(0)(0)(0)(0)(0)(0)(0)(0)
		// must precede thumb2_b__cond in search table
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// B6.1.7 MSR (register)
		// msr<c> <spec_reg>,<Rn>
		// 1 1 1 1 0 0 1 1 1 0 0 R_1_4_4 Rn_1_3_0 1 0 (0) 0 mask_0_11_8 (0)(0)(0)(0)(0)(0)(0)(0)
		// 11110011100xxxxx10(0)0xxxx(0)(0)(0)(0)(0)(0)(0)(0)
		// must precede thumb2_b__cond in search table
		new OpcodeARM(Index.thumb2_msr, "msr", "11110011100xxxxx10x0xxxxxxxxxxxx"),

		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.16 B
		// b<c>.w <label> Not permitted in IT block.
		// 1 1 1 1 0 S_1_10_10 cond_1_9_6 imm6_1_5_0 1 0 J1_0_13_13 0 J2_0_11_11 imm11_0_10_0
		// 11110xxxxxxxxxxx10x0xxxxxxxxxxxx
		// must follow thumb2_bxj in search table
		// must follow thumb2_clrex in search table
		// must follow thumb2_cps in search table
		// must follow thumb2_dbg in search table
		// must follow thumb2_dmb in search table
		// must follow thumb2_dsb in search table
		// must follow thumb2_isb in search table
		// must follow thumb2_mrs in search table
		// must follow thumb2_msr in search table
		// must follow thumb2_nop in search table
		// must follow thumb2_sev in search table
		// must follow thumb2_wfe in search table
		// must follow thumb2_wfi in search table
		// must follow thumb2_yield in search table
		new OpcodeARM(Index.thumb2_b__cond, "b", "11110xxxxxxxxxxx10x0xxxxxxxxxxxx"),

		// NEW - Encoding T4 ARMv6T2, ARMv7
		// A8.6.16 B
		// b<c>.w <label> Outside or last in IT block
		// 1 1 1 1 0 S_1_10_10 imm10_1_9_0 1 0 J1_0_13_13 1 J2_0_11_11 imm11_0_10_0
		// 11110xxxxxxxxxxx10x1xxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_b__uncond, "b.w", "11110xxxxxxxxxxx10x1xxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.17 BFC
		// bfc<c> <Rd>,#<lsb>,#<width>
		// 1 1 1 1 0 (0) 1 1 0 1 1 0 1 1 1 1 0 imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 (0) msb_0_4_0
		// 11110(0)11011011110xxxxxxxxx(0)xxxxx
		// must precede thumb2_bfi in search table
		new OpcodeARM(Index.thumb2_bfc, "bfc", "11110x11011011110xxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.18 BFI
		// bfi<c> <Rd>,<Rn>,#<lsb>,#<width>
		// 1 1 1 1 0 (0) 1 1 0 1 1 0 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 (0) msb_0_4_0
		// 11110(0)110110xxxx0xxxxxxxxx(0)xxxxx
		// must follow thumb2_bfc in search table
		new OpcodeARM(Index.thumb2_bfi, "bfi", "11110x110110xxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.154 SBFX
		// sbfx<c> <Rd>,<Rn>,#<lsb>,#<width>
		// 1 1 1 1 0 (0) 1 1 0 1 0 0 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 (0) widthm1
		// 11110(0)110100xxxx0xxxxxxxxx(0)xxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.236 UBFX
		// ubfx<c> <Rd>,<Rn>,#<lsb>,#<width>
		// 1 1 1 1 0 (0) 1 1 1 1 0 0 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 (0) widthm1
		// 11110(0)111100xxxx0xxxxxxxxx(0)xxxxx
		//
		new OpcodeARM(Index.thumb2_bfx, "bfx", "11110x11x100xxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.19 BIC (immediate)
		// bic{s}<c> <Rd>,<Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 0 0 0 1 S_1_4_4 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x00001xxxxx0xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_bic__imm, "bic", "11110x00001xxxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.20 BIC (register)
		// bic{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 0 0 0 1 S_1_4_4 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101010001xxxxx(0)xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_bic__reg, "bic", "11101010001xxxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv4T, ARMv5T*, ARMv6*, ARMv7 if J1 == J2 == 1; ARMv6T2, ARMv7 otherwise
		// A8.6.23 BL, BLX (immediate)
		// bl<c> <label> Outside or last in IT block
		// 1 1 1 1 0 S_1_10_10 imm10_1_9_0 1 1 J1_0_13_13 1 J2_0_11_11 imm11_0_10_0
		// 11110xxxxxxxxxxx11x1xxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_bl, "bl", "11110xxxxxxxxxxx11x1xxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv5T*, ARMv6*, ARMv7 if J1 == J2 == 1; ARMv6T2, ARMv7 otherwise
		// A8.6.23 BL, BLX (immediate)
		// blx<c> <label> Outside or last in IT block
		// 1 1 1 1 0 S_1_10_10 imm10H_1_9_0 1 1 J1_0_13_13 0 J2_0_11_11 imm10L_0_10_1 0
		// 11110xxxxxxxxxxx11x0xxxxxxxxxxx0
		new OpcodeARM(Index.thumb2_blx, "blx", "11110xxxxxxxxxxx11x0xxxxxxxxxxx0"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.31 CLZ
		// clz<c> <Rd>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 1 1 Rm_1_3_0 1 1 1 1 Rd_0_11_8 1 0 0 0 Rm_0_3_0
		// 111110101011xxxx1111xxxx1000xxxx
		new OpcodeARM(Index.thumb2_clz, "clz", "111110101011xxxx1111xxxx1000xxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.35 CMP (immediate)
		// cmp<c>.w <Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 1 1 0 1 1 Rn_1_3_0 0 imm3_0_14_12 1 1 1 1 imm8_0_7_0
		// 11110x011011xxxx0xxx1111xxxxxxxx
		// must precede thumb2_sub__imm in search table
		// must precede thumb2_subw in search table
		new OpcodeARM(Index.thumb2_cmp__imm, "cmp.w", "11110x011011xxxx0xxx1111xxxxxxxx"),

		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.36 CMP (register)
		// cmp<c>.w <Rn>, <Rm> {,<shift>}
		// 1 1 1 0 1 0 1 1 1 0 1 1 Rn_1_3_0 (0) imm3_0_14_12 1 1 1 1 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 111010111011xxxx(0)xxx1111xxxxxxxx
		// must precede thumb2_sub__reg in search table
		// must precede thumb2_subw in search table
		new OpcodeARM(Index.thumb2_cmp__reg, "cmp.w", "111010111011xxxxxxxx1111xxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.227 TEQ (immediate)
		// teq<c> <Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 0 1 0 0 1 Rn_1_3_0 0 imm3_0_14_12 1 1 1 1 imm8_0_7_0
		// 11110x001001xxxx0xxx1111xxxxxxxx
		// must precede thumb2_eor__imm in search table
		new OpcodeARM(Index.thumb2_teq__imm, "teq", "11110x001001xxxx0xxx1111xxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.44 EOR (immediate)
		// eor{s}<c> <Rd>,<Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 0 1 0 0 S_1_4_4 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x00100xxxxx0xxxxxxxxxxxxxxx
		// must follow thumb2_teq__imm in search table
		new OpcodeARM(Index.thumb2_eor__imm, "eor", "11110x00100xxxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.228 TEQ (register)
		// teq<c> <Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 0 1 0 0 1 Rn_1_3_0 (0) imm3_0_14_12 1 1 1 1 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 111010101001xxxx(0)xxx1111xxxxxxxx
		// must precede thumb2_eor__reg in search table
		new OpcodeARM(Index.thumb2_teq__reg, "teq", "111010101001xxxxxxxx1111xxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.45 EOR (register)
		// eor{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 0 1 0 0 S_1_4_4 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101010100xxxxx(0)xxxxxxxxxxxxxxx
		// must follow thumb2_teq__reg in search table
		new OpcodeARM(Index.thumb2_eor__reg, "eor", "11101010100xxxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.122 POP
		// pop<c>.w <registers> <registers> contains more than one register
		// 1 1 1 0 1 0 0 0 1 0 1 1 1 1 0 1 P_0_15_15 M_0_14_14 (0) register_list_0_12_0
		// 1110100010111101xx(0)xxxxxxxxxxxxx
		// must precede thumb2_ldm in search table
		new OpcodeARM(Index.thumb2_pop__regs, "pop.w", "1110100010111101xxxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.53 LDM / LDMIA / LDMFD
		// ldm<c>.w <Rn>{!},<registers>
		// 1 1 1 0 1 0 0 0 1 0 W_1_5_5 1 Rn_1_3_0 P_0_15_15 M_0_14_14 (0) register_list_0_12_0
		// 1110100010x1xxxxxx(0)xxxxxxxxxxxxx
		// must follow thumb2_pop__regs in search table
		new OpcodeARM(Index.thumb2_ldm, "ldm.w", "1110100010x1xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7; if ony 1 register, instead assemble to LDR<c><q> <Rt>,[<Rn>,#-4]{!} instruction
		// A8.6.55 LDMDB / LDMEA
		// ldmdb<c> <Rn>{!},<registers>
		// 1 1 1 0 1 0 0 1 0 0 W_1_5_5 1 Rn_1_3_0 P_0_15_15 M_0_14_14 (0) register_list_0_12_0
		// 1110100100x1xxxxxx(0)xxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_ldmdb, "ldmdb", "1110100100x1xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7 for PLD; ARMv7 with MP Extensions for PLDW
		// A8.6.117 PLD, PLDW (immediate)
		// pld{w}<c> [<Rn>,#<imm12>]
		// 1 1 1 1 1 0 0 0 1 0 W_1_5_5 1 Rn_1_3_0 1 1 1 1 imm12_0_11_0
		// 1111100010x1xxxx1111xxxxxxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7 for PLD; ARMv7 with MP Extensions for PLDW
		// A8.6.117 PLD, PLDW (immediate)
		// pld{w}<c> [<Rn>,#-<imm8>]
		// 1 1 1 1 1 0 0 0 0 0 W_1_5_5 1 Rn_1_3_0 1 1 1 1 1 1 0 0 imm8_0_7_0
		// 1111100000x1xxxx11111100xxxxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.118 PLD (literal)
		// pld<c> <label>	pld<c> [pc,#-0] Special case
		// 1 1 1 1 1 0 0 0 U_1_7_7 0 (0) 1 1 1 1 1 1 1 1 1 imm12_0_11_0
		// 11111000x0(0)111111111xxxxxxxxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7 for PLD; ARMv7 with MP Extensions for PLDW
		// A8.6.119 PLD, PLDW (register)
		// pld{w}<c> [<Rn>,<Rm>{,lsl #<imm2>}]
		// 1 1 1 1 1 0 0 0 0 0 W_1_5_5 1 Rn_1_3_0 1 1 1 1 0 0 0 0 0 0 imm2_0_5_4 Rm_0_3_0
		// 1111100000x1xxxx1111000000xxxxxx
		//
		// must precede thumb2_ldr in search table
		new OpcodeARM(Index.thumb2_pld,        "pld", "11111000x0x1xxxx1111xxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv7
		// A8.6.120 PLI (immediate, literal)
		// pli<c> [<Rn>,#<imm12>]
		// 1 1 1 1 1 0 0 1 1 0 0 1 Rn_1_3_0 1 1 1 1 imm12_0_11_0
		// 111110011001xxxx1111xxxxxxxxxxxx
		// NEW - Encoding T2 ARMv7
		// A8.6.120 PLI (immediate, literal)
		// pli<c> [<Rn>,#-<imm8>]
		// 1 1 1 1 1 0 0 1 0 0 0 1 Rn_1_3_0 1 1 1 1 1 1 0 0 imm8_0_7_0
		// 111110010001xxxx11111100xxxxxxxx
		// NEW - Encoding T3 ARMv7
		// A8.6.120 PLI (immediate, literal)
		// pli<c> <label>	pli<c> [pc,#-0] Special case
		// 1 1 1 1 1 0 0 1 U_1_7_7 0 0 1 1 1 1 1 1 1 1 1 imm12_0_11_0
		// 11111001x00111111111xxxxxxxxxxxx
		// must precede thumb2_ldr__imm in search table
		// NEW - Encoding T1 ARMv7
		// A8.6.121 PLI (register)
		// pli<c> [<Rn>,<Rm>{,lsl #<imm2>}]
		// 1 1 1 1 1 0 0 1 0 0 0 1 Rn_1_3_0 1 1 1 1 0 0 0 0 0 0 imm2_0_5_4 Rm_0_3_0
		// 111110010001xxxx1111000000xxxxxx
		// must precede thumb2_ldr in search table
		new OpcodeARM(Index.thumb2_pli,      "pli", "11111001x001xxxx1111xxxxxxxxxxxx"),

		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.122 POP
		// pop<c>.w <registers> <registers> contains one register, <Rt>
		// 1 1 1 1 1 0 0 0 0 1 0 1 1 1 0 1 Rt_0_15_12 1 0 1 1 0 0 0 0 0 1 0 0
		// 1111100001011101xxxx101100000100
		// must precede thumb2_ldr in search table
		new OpcodeARM(Index.thumb2_pop__reg, "pop.w", "1111100001011101xxxx101100000100"),

		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.57 LDR (immediate, Thumb)
		// ldr<c>.w <Rt>,[<Rn>{,#<imm12>}]
		// 1 1 1 1 1 0 0 0 1 1 0 1 Rn_1_3_0 Rt_0_15_12 imm12_0_11_0
		// 111110001101xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding T4 ARMv6T2, ARMv7
		// A8.6.57 LDR (immediate, Thumb)
		// ldr<c> <Rt>,[<Rn>,#-<imm8>]	ldr<c> <Rt>,[<Rn>],#+/-<imm8>	ldr<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// 1 1 1 1 1 0 0 0 0 1 0 1 Rn_1_3_0 Rt_0_15_12 1 P_0_10_10 U_0_9_9 W_0_8_8 imm8_0_7_0
		// 111110000101xxxxxxxx1xxxxxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.59 LDR (literal)
		// ldr<c>.w <Rt>,<label>	ldr<c>.w <Rt>,[pc,#-0] Special case
		// 1 1 1 1 1 0 0 0 U_1_7_7 1 0 1 1 1 1 1 Rt_0_15_12 imm12_0_11_0
		// 11111000x1011111xxxxxxxxxxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.60 LDR (register)
		// ldr<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// 1 1 1 1 1 0 0 0 0 1 0 1 Rn_1_3_0 Rt_0_15_12 0 0 0 0 0 0 imm2_0_5_4 Rm_0_3_0
		// 111110000101xxxxxxxx000000xxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.61 LDRB (immediate, Thumb)
		// ldrb<c>.w <Rt>,[<Rn>{,#<imm12>}]
		// 1 1 1 1 1 0 0 0 1 0 0 1 Rn_1_3_0 Rt_0_15_12 imm12_0_11_0
		// 111110001001xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.61 LDRB (immediate, Thumb)
		// ldrb<c> <Rt>,[<Rn>,#-<imm8>]	ldrb<c> <Rt>,[<Rn>],#+/-<imm8>	ldrb<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// 1 1 1 1 1 0 0 0 0 0 0 1 Rn_1_3_0 Rt_0_15_12 1 P_0_10_10 U_0_9_9 W_0_8_8 imm8_0_7_0
		// 111110000001xxxxxxxx1xxxxxxxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.63 LDRB (literal)
		// ldrb<c> <Rt>,<label>	ldrb<c> <Rt>,[pc,#-0] Special case
		// 1 1 1 1 1 0 0 0 U_1_7_7 0 0 1 1 1 1 1 Rt_0_15_12 imm12_0_11_0
		// 11111000x0011111xxxxxxxxxxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.64 LDRB (register)
		// ldrb<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// 1 1 1 1 1 0 0 0 0 0 0 1 Rn_1_3_0 Rt_0_15_12 0 0 0 0 0 0 imm2_0_5_4 Rm_0_3_0
		// 111110000001xxxxxxxx000000xxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.65 LDRBT
		// ldrbt<c> <Rt>,[<Rn>,#<imm8>]
		// 1 1 1 1 1 0 0 0 0 0 0 1 Rn_1_3_0 Rt_0_15_12 1 1 1 0 imm8_0_7_0
		// 111110000001xxxxxxxx1110xxxxxxxx
		// A8.6.73 LDRH (immediate, Thumb)
		// ldrh<c>.w <Rt>,[<Rn>{,#<imm12>}]
		// 1 1 1 1 1 0 0 0 1 0 1 1 Rn_1_3_0 Rt_0_15_12 imm12_0_11_0
		// 111110001011xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.73 LDRH (immediate, Thumb)
		// ldrh<c> <Rt>,[<Rn>,#-<imm8>]	ldrh<c> <Rt>,[<Rn>],#+/-<imm8>	ldrh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// 1 1 1 1 1 0 0 0 0 0 1 1 Rn_1_3_0 Rt_0_15_12 1 P_0_10_10 U_0_9_9 W_0_8_8 imm8_0_7_0
		// 111110000011xxxxxxxx1xxxxxxxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.75 LDRH (literal)
		// ldrh<c> <Rt>,<label>	ldrh<c> <Rt>,[pc,#-0] Special case
		// 1 1 1 1 1 0 0 0 U_1_7_7 0 1 1 1 1 1 1 Rt_0_15_12 imm12_0_11_0
		// 11111000x0111111xxxxxxxxxxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.76 LDRH (register)
		// ldrh<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// 1 1 1 1 1 0 0 0 0 0 1 1 Rn_1_3_0 Rt_0_15_12 0 0 0 0 0 0 imm2_0_5_4 Rm_0_3_0
		// 111110000011xxxxxxxx000000xxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.77 LDRHT
		// ldrht<c> <Rt>,[<Rn>,#<imm8>]
		// 1 1 1 1 1 0 0 0 0 0 1 1 Rn_1_3_0 Rt_0_15_12 1 1 1 0 imm8_0_7_0
		// 111110000011xxxxxxxx1110xxxxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.78 LDRSB (immediate)
		// ldrsb<c> <Rt>,[<Rn>,#<imm12>]
		// 1 1 1 1 1 0 0 1 1 0 0 1 Rn_1_3_0 Rt_0_15_12 imm12_0_11_0
		// 111110011001xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.78 LDRSB (immediate)
		// ldrsb<c> <Rt>,[<Rn>,#-<imm8>]	ldrsb<c> <Rt>,[<Rn>],#+/-<imm8>	ldrsb<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// 1 1 1 1 1 0 0 1 0 0 0 1 Rn_1_3_0 Rt_0_15_12 1 P_0_10_10 U_0_9_9 W_0_8_8 imm8_0_7_0
		// 111110010001xxxxxxxx1xxxxxxxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.79 LDRSB (literal)
		// ldrsb<c> <Rt>,<label>	ldrsb<c> <Rt>,[pc,#-0] Special case
		// 1 1 1 1 1 0 0 1 U_1_7_7 0 0 1 1 1 1 1 Rt_0_15_12 imm12_0_11_0
		// 11111001x0011111xxxxxxxxxxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.80 LDRSB (register)
		// ldrsb<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// 1 1 1 1 1 0 0 1 0 0 0 1 Rn_1_3_0 Rt_0_15_12 0 0 0 0 0 0 imm2_0_5_4 Rm_0_3_0
		// 111110010001xxxxxxxx000000xxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.81 LDRSBT
		// ldrsbt<c> <Rt>,[<Rn>,#<imm8>]
		// 1 1 1 1 1 0 0 1 0 0 0 1 Rn_1_3_0 Rt_0_15_12 1 1 1 0 imm8_0_7_0
		// 111110010001xxxxxxxx1110xxxxxxxx
		// A8.6.82 LDRSH (immediate)
		// ldrsh<c> <Rt>,[<Rn>,#<imm12>]
		// 1 1 1 1 1 0 0 1 1 0 1 1 Rn_1_3_0 Rt_0_15_12 imm12_0_11_0
		// 111110011011xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.82 LDRSH (immediate)
		// ldrsh<c> <Rt>,[<Rn>,#-<imm8>]	ldrsh<c> <Rt>,[<Rn>],#+/-<imm8>	ldrsh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// 1 1 1 1 1 0 0 1 0 0 1 1 Rn_1_3_0 Rt_0_15_12 1 P_0_10_10 U_0_9_9 W_0_8_8 imm8_0_7_0
		// 111110010011xxxxxxxx1xxxxxxxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.83 LDRSH (literal)
		// ldrsh<c> <Rt>,<label>	ldrsh<c> <Rt>,[pc,#-0] Special case
		// 1 1 1 1 1 0 0 1 U_1_7_7 0 1 1 1 1 1 1 Rt_0_15_12 imm12_0_11_0
		// 11111001x0111111xxxxxxxxxxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.84 LDRSH (register)
		// ldrsh<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// 1 1 1 1 1 0 0 1 0 0 1 1 Rn_1_3_0 Rt_0_15_12 0 0 0 0 0 0 imm2_0_5_4 Rm_0_3_0
		// 111110010011xxxxxxxx000000xxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.85 LDRSHT
		// ldrsht<c> <Rt>,[<Rn>,#<imm8>]
		// 1 1 1 1 1 0 0 1 0 0 1 1 Rn_1_3_0 Rt_0_15_12 1 1 1 0 imm8_0_7_0
		// 111110010011xxxxxxxx1110xxxxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.86 LDRT
		// ldrt<c> <Rt>,[<Rn>,#<imm8>]
		// 1 1 1 1 1 0 0 0 0 1 0 1 Rn_1_3_0 Rt_0_15_12 1 1 1 0 imm8_0_7_0
		// 111110000101xxxxxxxx1110xxxxxxxx
		//
		// must follow thumb2_pld in search table
		// must follow thumb2_pli in search table
		// must follow thumb2_pop_reg in search table
		// 1 1 1 1 1 0 0 s_1_8_8 u_1_7_7 op_1_6_6 bh_1_5_5 1 Rn_1_3_0 Rt_0_15_12 imm12_0_11_0
		new OpcodeARM(Index.thumb2_ldr, "ldr", "1111100xxxx1xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.67 LDRD (literal)
		// ldrd<c> <Rt>,<Rt2>,<label>	ldrd<c> <Rt>,<Rt2>,[pc,#-0] Special case
		// 1 1 1 0 1 0 0 P_1_8_8 U_1_7_7 1 (0) 1 1 1 1 1 Rt_0_15_12 Rt2_0_11_8 imm8_0_7_0
		// 1110100xx1(0)11111xxxxxxxxxxxxxxxx
		// must precede thumb2_ldrex in search table
		// must precede thumb2_ldrd__imm in search table
		new OpcodeARM(Index.thumb2_ldrd__lit, "ldrd", "1110100xx1x11111xxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.69 LDREX
		// ldrex<c> <Rt>,[<Rn>{,#<imm>}]
		// 1 1 1 0 1 0 0 0 0 1 0 1 Rn_1_3_0 Rt_0_15_12 (1)(1)(1)(1) imm8_0_7_0
		// 111010000101xxxxxxxx(1)(1)(1)(1)xxxxxxxx
		// must follow thumb2_ldrd__lit in search table
		// must encode (1)(1)(1)(1) and precede thumb2_ldrd__imm in search table
		new OpcodeARM(Index.thumb2_ldrex, "ldrex", "111010000101xxxxxxxx1111xxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.226 TBB, TBH
		// tbb<c> [<Rn>,<Rm>] Outside or last in IT block	tbh<c> [<Rn>,<Rm>,LSL #1] Outside or last in IT block
		// 1 1 1 0 1 0 0 0 1 1 0 1 Rn_1_3_0 (1) (1)(1)(1)(0)(0)(0)(0) 0 0 0 H_0_4_4 Rm_0_3_0
		// 111010001101xxxx(1)(1)(1)(1)(0)(0)(0)(0)000xxxxx
		// must precede thumb2_ldrexx in search table
		new OpcodeARM(Index.thumb2_tb, "tb", "111010001101xxxxxxxxxxxx000xxxxx"),

		// NEW - Encoding T1 ARMv7
		// A8.6.70 LDREXB
		// ldrexb<c> <Rt>, [<Rn>]
		// 1 1 1 0 1 0 0 0 1 1 0 1 Rn_1_3_0 Rt_0_15_12 (1)(1)(1)(1) 0 1 0 0 (1)(1)(1)(1)
		// 111010001101xxxxxxxx(1)(1)(1)(1)0100(1)(1)(1)(1)
		// NEW - Encoding T1 ARMv7
		// A8.6.71 LDREXD
		// ldrexd<c> <Rt>,<Rt2>,[<Rn>]
		// 1 1 1 0 1 0 0 0 1 1 0 1 Rn_1_3_0 Rt_0_15_12 Rt2_0_11_8 0 1 1 1 (1)(1)(1)(1)
		// 111010001101xxxxxxxxxxxx0111(1)(1)(1)(1)
		// NEW - Encoding T1 ARMv7
		// A8.6.72 LDREXH
		// ldrexh<c> <Rt>, [<Rn>]
		// 1 1 1 0 1 0 0 0 1 1 0 1 Rn_1_3_0 Rt_0_15_12 (1)(1)(1)(1) 0 1 0 1 (1)(1)(1)(1)
		// 111010001101xxxxxxxx(1)(1)(1)(1)0101(1)(1)(1)(1)
		// must follow thumb2_ldrd__lit in search table
		// must follow thumb2_tb in search table
		// must encode Rt2/(1)(1)(1)(1) and precede thumb2_ldrd__imm in search table
		new OpcodeARM(Index.thumb2_ldrexx, "ldrex", "111010001101xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.66 LDRD (immediate)
		// ldrd<c> <Rt>,<Rt2>,[<Rn>{,#+/-<imm>}]	ldrd<c> <Rt>,<Rt2>,[<Rn>],#+/-<imm>	ldrd<c> <Rt>,<Rt2>,[<Rn>,#+/-<imm>]!
		// 1 1 1 0 1 0 0 P_1_8_8 U_1_7_7 1 W_1_5_5 1 Rn_1_3_0 Rt_0_15_12 Rt2_0_11_8 imm8_0_7_0
		// 1110100xx1x1xxxxxxxxxxxxxxxxxxxx
		// must follow thumb2_ldrd__lit in search table
		// must follow thumb2_ldrex in search table		
		new OpcodeARM(Index.thumb2_ldrd__imm, "ldrd", "1110100xx1x1xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.97 MOV (register)
		// mov{s}<c>.w <Rd>,<Rm>
		// 1 1 1 0 1 0 1 0 0 1 0 S_1_4_4 1 1 1 1 (0) 0 0 0 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 11101010010x1111(0)000xxxx0000xxxx
		// must precede thumb2_lsl__imm in search table
		// must precede thumb2_orr__reg in search table
		new OpcodeARM(Index.thumb2_mov__reg, "mov", "11101010010x1111x000xxxx0000xxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.88 LSL (immediate)
		// lsl{s}<c>.w <Rd>,<Rm>,#<imm5>
		// 1 1 1 0 1 0 1 0 0 1 0 S_1_4_4 1 1 1 1 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 0 0 Rm_0_3_0
		// 11101010010x1111(0)xxxxxxxxx00xxxx
		// must follow thumb2_mov_reg in search table
		new OpcodeARM(Index.thumb2_lsl__imm, "lsl", "11101010010x1111xxxxxxxxxx00xxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.89 LSL (register)
		// lsl{s}<c>.w <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 0 0 0 S_1_4_4 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 11111010000xxxxx1111xxxx0000xxxx
		new OpcodeARM(Index.thumb2_lsl__reg, "lsl", "11111010000xxxxx1111xxxx0000xxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.90 LSR (immediate)
		// lsr{s}<c>.w <Rd>,<Rm>,#<imm>
		// 1 1 1 0 1 0 1 0 0 1 0 S_1_4_4 1 1 1 1 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 0 1 Rm_0_3_0
		// 11101010010x1111(0)xxxxxxxxx01xxxx
		new OpcodeARM(Index.thumb2_lsr__imm, "lsr", "11101010010x1111xxxxxxxxxx01xxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.91 LSR (register)
		// lsr{s}<c>.w <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 0 0 1 S_1_4_4 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 11111010001xxxxx1111xxxx0000xxxx
		new OpcodeARM(Index.thumb2_lsr__reg, "lsr", "11111010001xxxxx1111xxxx0000xxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.105 MUL
		// mul<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 0 0 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110110000xxxx1111xxxx0000xxxx
		// must precede thumb2_ml in search table
		new OpcodeARM(Index.thumb2_mul, "mul", "111110110000xxxx1111xxxx0000xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.94 MLA
		// mla<c> <Rd>,<Rn>,<Rm>,<Ra>
		// 1 1 1 1 1 0 1 1 0 0 0 0 Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110110000xxxxxxxxxxxx0000xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.95 MLS
		// mls<c> <Rd>,<Rn>,<Rm>,<Ra>
		// 1 1 1 1 1 0 1 1 0 0 0 0 Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 0 0 0 1 Rm_0_3_0
		// 111110110000xxxxxxxxxxxx0001xxxx
		//
		// must follow thumb2_mul in search table
		new OpcodeARM(Index.thumb2_ml, "ml", "111110110000xxxxxxxxxxxx000xxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.96 MOV (immediate)
		// mov{s}<c>.w <Rd>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 0 0 1 0 S_1_4_4 1 1 1 1 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x00010x11110xxxxxxxxxxxxxxx
		// must precede thumb2_orr__imm in search table
		new OpcodeARM(Index.thumb2_mov__imm, "mov", "11110x00010x11110xxxxxxxxxxxxxxx"),

		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.96 MOV (immediate)
		// movw<c> <Rd>,#<imm16>
		// 1 1 1 1 0 i_1_10_10 1 0 0 1 0 0 imm4_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x100100xxxx0xxxxxxxxxxxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.99 MOVT
		// movt<c> <Rd>,#<imm16>
		// 1 1 1 1 0 i_1_10_10 1 0 1 1 0 0 imm4_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x101100xxxx0xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_movx, "mov", "11110x10x100xxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.106 MVN (immediate)
		// mvn{s}<c> <Rd>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 0 0 1 1 S_1_4_4 1 1 1 1 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x00011x11110xxxxxxxxxxxxxxx
		// must precede thumb2_orn__imm in search table
		new OpcodeARM(Index.thumb2_mvn__imm, "mvn", "11110x00011x11110xxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.107 MVN (register)
		// mvn{s}<c>.w <Rd>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 0 0 1 1 S_1_4_4 1 1 1 1 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101010011x1111(0)xxxxxxxxxxxxxxx
		// must precede thumb2_orn__reg in search table
		new OpcodeARM(Index.thumb2_mvn__reg, "mvn", "11101010011x1111xxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.111 ORN (immediate)
		// orn{s}<c> <Rd>,<Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 0 0 1 1 S_1_4_4 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x00011xxxxx0xxxxxxxxxxxxxxx
		// must follow thumb2_mvn__imm in search table
		new OpcodeARM(Index.thumb2_orn__imm, "orn", "11110x00011xxxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.112 ORN (register)
		// orn{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 0 0 1 1 S_1_4_4 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101010011xxxxx(0)xxxxxxxxxxxxxxx
		// must follow thumb2_mvn__reg in search table
		new OpcodeARM(Index.thumb2_orn__reg, "orn", "11101010011xxxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.113 ORR (immediate)
		// orr{s}<c> <Rd>,<Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 0 0 1 0 S_1_4_4 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x00010xxxxx0xxxxxxxxxxxxxxx
		// must follow thumb2_mov__imm in search table
		new OpcodeARM(Index.thumb2_orr__imm, "orr", "11110x00010xxxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.141 RRX
		// rrx{s}<c> <Rd>,<Rm>
		// 1 1 1 0 1 0 1 0 0 1 0 S_1_4_4 1 1 1 1 (0) 0 0 0 Rd_0_11_8 0 0 1 1 Rm_0_3_0
		// 11101010010x1111(0)000xxxx0011xxxx
		// must precede thumb2_ror__imm in search table
		// must precede thumb2_orr__reg in search table
		new OpcodeARM(Index.thumb2_rrx, "rrx", "11101010010x1111x000xxxx0011xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.139 ROR (immediate)
		// ror{s}<c> <Rd>,<Rm>,#<imm>
		// 1 1 1 0 1 0 1 0 0 1 0 S_1_4_4 1 1 1 1 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 1 1 Rm_0_3_0
		// 11101010010x1111(0)xxxxxxxxx11xxxx
		// must precede thumb2_orr__reg in search table
		// must follow thumb2_rrx in search table
		new OpcodeARM(Index.thumb2_ror__imm, "ror", "11101010010x1111xxxxxxxxxx11xxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.114 ORR (register)
		// orr{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 0 0 1 0 S_1_4_4 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101010010xxxxx(0)xxxxxxxxxxxxxxx
		// must follow thumb2_mov__reg in search table
		// must follow thumb2_ror__imm in search table
		// must follow thumb2_rrx in search table
		new OpcodeARM(Index.thumb2_orr__reg, "orr", "11101010010xxxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.116 PKH
		// pkhbt<c> <Rd>,<Rn>,<Rm>{,lsl #<imm>}	pkhtb<c> <Rd>,<Rn>,<Rm>{,asr #<imm>}
		// 1 1 1 0 1 0 1 0 1 1 0 0 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 tb_0_5_5 0 Rm_0_3_0
		// 111010101100xxxx(0)xxxxxxxxxx0xxxx
		new OpcodeARM(Index.thumb2_pkh, "pkh", "111010101100xxxxxxxxxxxxxxx0xxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.123 PUSH
		// push<c>.w <registers> <registers> contains more than one register
		// 1 1 1 0 1 0 0 0 1 0 1 0 1 1 0 1 (0) M_0_14_14 (0) register_list_0_12_0
		// 1110100010101101(0)x(0)xxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_push__regs, "push.w", "1110100010101101xxxxxxxxxxxxxxxx"),

		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.123 PUSH
		// push<c>.w <registers> <registers> contains one register, <Rt>
		// 1 1 1 1 1 0 0 0 0 1 0 0 1 1 0 1 Rt_0_15_12 1 1 0 1 0 0 0 0 0 1 0 0
		// 1111100001001101xxxx110100000100
		// must precede thumb2_str in search table
		new OpcodeARM(Index.thumb2_push__reg, "push.w", "1111100001001101xxxx110100000100"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.124 QADD
		// qadd<c> <Rd>,<Rm>,<Rn>
		// 1 1 1 1 1 0 1 0 1 0 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 1 0 0 0 Rm_0_3_0
		// 111110101000xxxx1111xxxx1000xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.128 QDADD
		// qdadd<c> <Rd>,<Rm>,<Rn>
		// 1 1 1 1 1 0 1 0 1 0 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 1 0 0 1 Rm_0_3_0
		// 111110101000xxxx1111xxxx1001xxxx
		//
		new OpcodeARM(Index.thumb2_qadd, "add", "111110101000xxxx1111xxxx100xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.129 QDSUB
		// qdsub<c> <Rd>,<Rm>,<Rn>
		// 1 1 1 1 1 0 1 0 1 0 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 1 0 1 1 Rm_0_3_0
		// 111110101000xxxx1111xxxx1011xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.131 QSUB
		// qsub<c> <Rd>,<Rm>,<Rn>
		// 1 1 1 1 1 0 1 0 1 0 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 1 0 1 0 Rm_0_3_0
		// 111110101000xxxx1111xxxx1010xxxx
		//
		new OpcodeARM(Index.thumb2_qsub, "sub", "111110101000xxxx1111xxxx101xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.125 QADD16
		// qadd16<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 1 Rm_0_3_0
		// 111110101001xxxx1111xxxx0001xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.126 QADD8
		// qadd8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 1 Rm_0_3_0
		// 111110101000xxxx1111xxxx0001xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.127 QASX
		// qasx<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 1 Rm_0_3_0
		// 111110101010xxxx1111xxxx0001xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.130 QSAX
		// qsax<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 1 Rm_0_3_0
		// 111110101110xxxx1111xxxx0001xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.132 QSUB16
		// qsub16<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 1 Rm_0_3_0
		// 111110101101xxxx1111xxxx0001xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.133 QSUB8
		// qsub8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 1 Rm_0_3_0
		// 111110101100xxxx1111xxxx0001xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.148 SADD16
		// sadd16<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110101001xxxx1111xxxx0000xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.149 SADD8
		// sadd8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110101000xxxx1111xxxx0000xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.150 SASX
		// sasx<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110101010xxxx1111xxxx0000xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.159 SHADD16
		// shadd16<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 1 0 Rm_0_3_0
		// 111110101001xxxx1111xxxx0010xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.160 SHADD8
		// shadd8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 1 0 Rm_0_3_0
		// 111110101000xxxx1111xxxx0010xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.161 SHASX
		// shasx<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 1 0 Rm_0_3_0
		// 111110101010xxxx1111xxxx0010xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.162 SHSAX
		// shsax<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 1 0 Rm_0_3_0
		// 111110101110xxxx1111xxxx0010xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.163 SHSUB16
		// shsub16<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 1 0 Rm_0_3_0
		// 111110101101xxxx1111xxxx0010xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.164 SHSUB8
		// shsub8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 1 0 Rm_0_3_0
		// 111110101100xxxx1111xxxx0010xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.185 SSAX
		// ssax<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110101110xxxx1111xxxx0000xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.186 SSUB16
		// ssub16<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110101101xxxx1111xxxx0000xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.187 SSUB8
		// ssub8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110101100xxxx1111xxxx0000xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.233 UADD16
		// uadd16<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 0 0 Rm_0_3_0
		// 111110101001xxxx1111xxxx0100xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.234 UADD8
		// uadd8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 0 0 Rm_0_3_0
		// 111110101000xxxx1111xxxx0100xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.235 UASX
		// uasx<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 0 0 Rm_0_3_0
		// 111110101010xxxx1111xxxx0100xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.238 UHADD16
		// uhadd16<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 1 0 Rm_0_3_0
		// 111110101001xxxx1111xxxx0110xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.239 UHADD8
		// uhadd8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 1 0 Rm_0_3_0
		// 111110101000xxxx1111xxxx0110xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.240 UHASX
		// uhasx<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 1 0 Rm_0_3_0
		// 111110101010xxxx1111xxxx0110xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.241 UHSAX
		// uhsax<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 1 0 Rm_0_3_0
		// 111110101110xxxx1111xxxx0110xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.242 UHSUB16
		// uhsub16<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 1 0 Rm_0_3_0
		// 111110101101xxxx1111xxxx0110xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.243 UHSUB8
		// uhsub8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 1 0 Rm_0_3_0
		// 111110101100xxxx1111xxxx0110xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.247 UQADD16
		// uqadd16<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 0 1 Rm_0_3_0
		// 111110101001xxxx1111xxxx0101xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.248 UQADD8
		// uqadd8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 0 1 Rm_0_3_0
		// 111110101000xxxx1111xxxx0101xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.249 UQASX
		// uqasx<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 0 1 Rm_0_3_0
		// 111110101010xxxx1111xxxx0101xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.250 UQSAX
		// uqsax<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 0 1 Rm_0_3_0
		// 111110101110xxxx1111xxxx0101xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.251 UQSUB16
		// uqsub16<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 0 1 Rm_0_3_0
		// 111110101101xxxx1111xxxx0101xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.252 UQSUB8
		// uqsub8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 0 1 Rm_0_3_0
		// 111110101100xxxx1111xxxx0101xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.257 USAX
		// usax<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 0 0 Rm_0_3_0
		// 111110101110xxxx1111xxxx0100xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.258 USUB16
		// usub16<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 0 0 Rm_0_3_0
		// 111110101101xxxx1111xxxx0100xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.259 USUB8
		// usub8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 1 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 1 0 0 Rm_0_3_0
		// 111110101100xxxx1111xxxx0100xxxx
		//
		// {s|u|}{h|q|}{asx|sax|{add|sub}{8|16}<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 op_1_6_4 Rn_1_3_0 1 1 1 1 Rd_0_11_8 u_0_6_0 h_0_5_0 q_0_4_0 Rm_0_3_0
		new OpcodeARM(Index.thumb2__r_dnm_math, null, "111110101xxxxxxx1111xxxx0xxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.134 RBIT
		// rbit<c> <Rd>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 1 Rm_1_3_0 1 1 1 1 Rd_0_11_8 1 0 1 0 Rm_0_3_0
		// 111110101001xxxx1111xxxx1010xxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.135 REV
		// rev<c>.w <Rd>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 1 Rm_1_3_0 1 1 1 1 Rd_0_11_8 1 0 0 0 Rm_0_3_0
		// 111110101001xxxx1111xxxx1000xxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.136 REV16
		// rev16<c>.w <Rd>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 1 Rm_1_3_0 1 1 1 1 Rd_0_11_8 1 0 0 1 Rm_0_3_0
		// 111110101001xxxx1111xxxx1001xxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.137 REVSH
		// revsh<c>.w <Rd>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 0 1 Rm_1_3_0 1 1 1 1 Rd_0_11_8 1 0 1 1 Rm_0_3_0
		// 111110101001xxxx1111xxxx1011xxxx
		//
		new OpcodeARM(Index.thumb2_reverse, "r", "111110101001xxxx1111xxxx10xxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.140 ROR (register)
		// ror{s}<c>.w <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 0 1 1 S_1_4_4 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 11111010011xxxxx1111xxxx0000xxxx
		new OpcodeARM(Index.thumb2_ror__reg, "ror", "11111010011xxxxx1111xxxx0000xxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.142 RSB (immediate)
		// rsb{s}<c>.w <Rd>,<Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 1 1 1 0 S_1_4_4 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x01110xxxxx0xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_rsb__imm, "rsb", "11110x01110xxxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.143 RSB (register)
		// rsb{s}<c> <Rd>,<Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 1 1 1 0 S_1_4_4 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101011110xxxxx(0)xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_rsb__reg, "rsb", "11101011110xxxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.151 SBC (immediate)
		// sbc{s}<c> <Rd>,<Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 1 0 1 1 S_1_4_4 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x01011xxxxx0xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_sbc__imm, "sbc", "11110x01011xxxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.152 SBC (register)
		// sbc{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 1 0 1 1 S_1_4_4 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101011011xxxxx(0)xxxxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_sbc__reg, "sbc", "11101011011xxxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv7-R
		// A8.6.155 SDIV
		// sdiv<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 1 0 0 1 Rn_1_3_0 (1) (1)(1)(1) Rd_0_11_8 1 1 1 1 Rm_0_3_0
		// 111110111001xxxx(1)(1)(1)(1)xxxx1111xxxx
		new OpcodeARM(Index.thumb2_sdiv, "sdiv", "111110111001xxxxxxxxxxxx1111xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.156 SEL
		// sel<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 0 1 0 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 1 0 0 0 Rm_0_3_0
		// 111110101010xxxx1111xxxx1000xxxx
		new OpcodeARM(Index.thumb2_sel, "sel", "111110101010xxxx1111xxxx1000xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.178 SMULBB, SMULBT, SMULTB, SMULTT
		// smul<x><y><c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 0 0 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 N_0_5_5 M_0_4_4 Rm_0_3_0
		// 111110110001xxxx1111xxxx00xxxxxx
		// must preced thumb2_smla in search table
		new OpcodeARM(Index.thumb2_smul, "smul", "111110110001xxxx1111xxxx00xxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.166 SMLABB, SMLABT, SMLATB, SMLATT
		// smla<x><y><c> <Rd>,<Rn>,<Rm>,<Ra>
		// 1 1 1 1 1 0 1 1 0 0 0 1 Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 0 0 N_0_5_5 M_0_4_4 Rm_0_3_0
		// 111110110001xxxxxxxxxxxx00xxxxxx
		// must follow thumb2_smul in search table
		new OpcodeARM(Index.thumb2_smla, "smla", "111110110001xxxxxxxxxxxx00xxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.177 SMUAD
		// smuad{x}<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 0 0 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 M_0_4_4 Rm_0_3_0
		// 111110110010xxxx1111xxxx000xxxxx
		// must preced thumb2_smlad in search table
		new OpcodeARM(Index.thumb2_smuad, "smuad", "111110110010xxxx1111xxxx000xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.167 SMLAD
		// smlad{x}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// 1 1 1 1 1 0 1 1 0 0 1 0 Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 0 0 0 M_0_4_4 Rm_0_3_0
		// 111110110010xxxxxxxxxxxx000xxxxx
		// must follow thumb2_smuad in search table
		new OpcodeARM(Index.thumb2_smlad, "smlad", "111110110010xxxxxxxxxxxx000xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.168 SMLAL
		// smlal<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 1 1 0 0 Rn_1_3_0 RdLo_0_15_12 RdHi_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110111100xxxxxxxxxxxx0000xxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.169 SMLALBB, SMLALBT, SMLALTB, SMLALTT
		// smlal<x><y><c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 1 1 0 0 Rn_1_3_0 RdLo_0_15_12 RdHi_0_11_8 1 0 N_0_5_5 M_0_4_4 Rm_0_3_0
		// 111110111100xxxxxxxxxxxx10xxxxxx
		//
		new OpcodeARM(Index.thumb2_smlal, "smlal", "111110111100xxxxxxxxxxxxx0xxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.170 SMLALD
		// smlald{x}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 1 1 0 0 Rn_1_3_0 RdLo_0_15_12 RdHi_0_11_8 1 1 0 M_0_4_4 Rm_0_3_0
		// 111110111100xxxxxxxxxxxx110xxxxx
		new OpcodeARM(Index.thumb2_smlald, "smlald", "111110111100xxxxxxxxxxxx110xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.180 SMULWB, SMULWT
		// smulw<y><c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 0 0 1 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 M_0_4_4 Rm_0_3_0
		// 111110110011xxxx1111xxxx000xxxxx
		// must precede thumb2_smlaw in search table
		new OpcodeARM(Index.thumb2_smulw, "smulw", "111110110011xxxx1111xxxx000xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.171 SMLAWB, SMLAWT
		// smlaw<y><c> <Rd>,<Rn>,<Rm>,<Ra>
		// 1 1 1 1 1 0 1 1 0 0 1 1 Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 0 0 0 M_0_4_4 Rm_0_3_0
		// 111110110011xxxxxxxxxxxx000xxxxx
		// must follow thumb2_smulw in search table
		new OpcodeARM(Index.thumb2_smlaw, "smlaw", "111110110011xxxxxxxxxxxx000xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.181 SMUSD
		// smusd{x}<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 0 1 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 M_0_4_4 Rm_0_3_0
		// 111110110100xxxx1111xxxx000xxxxx
		new OpcodeARM(Index.thumb2_smusd, "smusd", "111110110100xxxx1111xxxx000xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.172 SMLSD
		// smlsd{x}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// 1 1 1 1 1 0 1 1 0 1 0 0 Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 0 0 0 M_0_4_4 Rm_0_3_0
		// 111110110100xxxxxxxxxxxx000xxxxx
		// must follow thumb2_smusd in search table
		new OpcodeARM(Index.thumb2_smlsd, "smlsd", "111110110100xxxxxxxxxxxx000xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.173 SMLSLD
		// smlsld{x}<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 1 1 0 1 Rn_1_3_0 RdLo_0_15_12 RdHi_0_11_8 1 1 0 M_0_4_4 Rm_0_3_0
		// 111110111101xxxxxxxxxxxx110xxxxx
		new OpcodeARM(Index.thumb2_smlsld, "smlsld", "111110111101xxxxxxxxxxxx110xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.176 SMMUL
		// smmul{r}<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 0 1 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 R_0_4_4 Rm_0_3_0
		// 111110110101xxxx1111xxxx000xxxxx
		// must precede thumb2_smmla in search table
		new OpcodeARM(Index.thumb2_smmul, "smmul", "111110110101xxxx1111xxxx000xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.174 SMMLA
		// smmla{r}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// 1 1 1 1 1 0 1 1 0 1 0 1 Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 0 0 0 R_0_4_4 Rm_0_3_0
		// 111110110101xxxxxxxxxxxx000xxxxx
		// must follow thumb2_smmul in search table
		new OpcodeARM(Index.thumb2_smmla, "smmla", "111110110101xxxxxxxxxxxx000xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.175 SMMLS
		// smmls{r}<c> <Rd>,<Rn>,<Rm>,<Ra>
		// 1 1 1 1 1 0 1 1 0 1 1 0 Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 0 0 0 R_0_4_4 Rm_0_3_0
		// 111110110110xxxxxxxxxxxx000xxxxx
		new OpcodeARM(Index.thumb2_smmls, "smmls", "111110110110xxxxxxxxxxxx000xxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.179 SMULL
		// smull<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 1 0 0 0 Rn_1_3_0 RdLo_0_15_12 RdHi_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110111000xxxxxxxxxxxx0000xxxx
		new OpcodeARM(Index.thumb2_smull, "smull", "111110111000xxxxxxxxxxxx0000xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.184 SSAT16
		// ssat16<c> <Rd>,#<imm>,<Rn>
		// 1 1 1 1 0 (0) 1 1 0 0 1 0 Rn_1_3_0 0 0 0 0 Rd_0_11_8 0 0 (0)(0) sat_imm_0_3_0
		// 11110(0)110010xxxx0000xxxx00(0)(0)xxxx
		// must precede thumb2_ssat in search table
		new OpcodeARM(Index.thumb2_ssat16, "ssat16", "11110x110010xxxx0000xxxx00xxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.183 SSAT
		// ssat<c> <Rd>,#<imm>,<Rn>{,<shift>}
		// 1 1 1 1 0 (0) 1 1 0 0 sh_1_5_5 0 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 (0) sat_imm_0_4_0
		// 11110(0)1100x0xxxx0xxxxxxxxx(0)xxxxx
		// must follow thumb2_ssat16 in search table
		new OpcodeARM(Index.thumb2_ssat,   "ssat",   "11110x1100x0xxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.189 STM / STMIA / STMEA
		// stm<c>.w <Rn>{!},<registers>
		// 1 1 1 0 1 0 0 0 1 0 W_1_5_5 0 Rn_1_3_0 (0) M_0_14_14 (0) register_list_0_12_0
		// 1110100010x0xxxx(0)x(0)xxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_stm, "stm.w", "1110100010x0xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.191 STMDB / STMFD
		// stmdb<c> <Rn>{!},<registers>
		// 1 1 1 0 1 0 0 1 0 0 W_1_5_5 0 Rn_1_3_0 (0) M_0_14_14 (0) register_list_0_12_0
		// 1110100100x0xxxx(0)x(0)xxxxxxxxxxxxx
		new OpcodeARM(Index.thumb2_stmdb, "stmdb", "1110100100x0xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.307 VLD1 (multiple single elements)
		// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.310 VLD2 (multiple 2-element structures)
		// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.313 VLD3 (multiple 3-element structures)
		// vld3<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld3<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.316 VLD4 (multiple 4-element structures)
		// vld4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 1 1 1 1 1 0 0 1 0 D_1_6_6 1 0 Rn_1_3_0 Vd_0_15_12 type_0_11_8 size_0_7_6 align_0_5_4 Rm_0_3_0
		// 111110010x10xxxxxxxxxxxxxxxxxxxx
		//
		// must precede thumb2_str
		new OpcodeARM(Index.thumb2_vld__multi, "vld", "111110010x10xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.309 VLD1 (single element to all lanes)
		// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 1 1 1 1 1 0 0 1 1 D_1_6_6 1 0 Rn_1_3_0 Vd_0_15_12 1 1 0 0 size_0_7_6 T_0_5_5 a_0_4_4 Rm_0_3_0
		// 111110011x10xxxxxxxx1100xxxxxxxx
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.308 VLD1 (single element to one lane)
		// vld1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 1 1 1 1 1 0 0 1 1 D_1_6_6 1 0 Rn_1_3_0 Vd_0_15_12 size_0_11_10 0 0 index_align_0_7_4 Rm_0_3_0
		// 111110011x10xxxxxxxxxx00xxxxxxxx
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.312 VLD2 (single 2-element structure to all lanes)
		// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 1 1 1 1 1 0 0 1 1 D_1_6_6 1 0 Rn_1_3_0 Vd_0_15_12 1 1 0 1 size_0_7_6 T_0_5_5 a_0_4_4 Rm_0_3_0
		// 111110011x10xxxxxxxx1101xxxxxxxx
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.311 VLD2 (single 2-element structure to one lane)
		// vld2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 1 1 1 1 1 0 0 1 1 D_1_6_6 1 0 Rn_1_3_0 Vd_0_15_12 size_0_11_10 0 1 index_align_0_7_4 Rm_0_3_0
		// 111110011x10xxxxxxxxxx01xxxxxxxx
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.315 VLD3 (single 3-element structure to all lanes)
		// vld3<c>.<size> <list>, [<Rn>]{!}	vld3<c>.<size> <list>, [<Rn>], <Rm>
		// 1 1 1 1 1 0 0 1 1 D_1_6_6 1 0 Rn_1_3_0 Vd_0_15_12 1 1 1 0 size_0_7_6 T_0_5_5 a_0_4_4 Rm_0_3_0
		// 111110011x10xxxxxxxx1110xxxxxxxx
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.314 VLD3 (single 3-element structure to one lane)
		// vld3<c>.<size> <list>, [<Rn>]{!}	vld3<c>.<size> <list>, [<Rn>], <Rm>
		// 1 1 1 1 1 0 0 1 1 D_1_6_6 1 0 Rn_1_3_0 Vd_0_15_12 size_0_11_10 1 0 index_align_0_7_4 Rm_0_3_0
		// 111110011x10xxxxxxxxxx10xxxxxxxx
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.318 VLD4 (single 4-element structure to all lanes)
		// vld4<c>.<size> <list>, [<Rn>{ @<align>}]{!}	vld4<c>.<size> <list>, [<Rn>{ @<align>}], <Rm>
		// 1 1 1 1 1 0 0 1 1 D_1_6_6 1 0 Rn_1_3_0 Vd_0_15_12 1 1 1 1 size_0_7_6 T_0_5_5 a_0_4_4 Rm_0_3_0
		// 111110011x10xxxxxxxx1111xxxxxxxx
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.317 VLD4 (single 4-element structure to one lane)
		// vld4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vld4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 1 1 1 1 1 0 0 1 1 D_1_6_6 1 0 Rn_1_3_0 Vd_0_15_12 size_0_11_10 1 1 index_align_0_7_4 Rm_0_3_0
		// 111110011x10xxxxxxxxxx11xxxxxxxx
		// 
		// must precede thumb2_str							
		new OpcodeARM(Index.thumb2_vld__xlane, "vld", "111110011x10xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.391 VST1 (multiple single elements)
		// vst1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.393 VST2 (multiple 2-element structures)
		// vst2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.395 VST3 (multiple 3-element structures)
		// vst3<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst3<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.397 VST4 (multiple 4-element structures)
		// vst4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 1 1 1 1 1 0 0 1 0 D_1_6_6 0 0 Rn_1_3_0 Vd_0_15_12 type_0_11_8 size_0_7_6 align_0_5_4 Rm_0_3_0
		// 111110010x00xxxxxxxxxxxxxxxxxxxx
		//
		// must precede thumb2_str
		new OpcodeARM(Index.thumb2_vst__multi, "vst", "111110010x00xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.392 VST1 (single element from one lane)
		// vst1<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst1<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 1 1 1 1 1 0 0 1 1 D_1_6_6 0 0 Rn_1_3_0 Vd_0_15_12 size_0_11_10 0 0 index_align_0_7_4 Rm_0_3_0
		// 111110011x00xxxxxxxxxx00xxxxxxxx
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.394 VST2 (single 2-element structure from one lane)
		// vst2<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst2<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 1 1 1 1 1 0 0 1 1 D_1_6_6 0 0 Rn_1_3_0 Vd_0_15_12 size_0_11_10 0 1 index_align_0_7_4 Rm_0_3_0
		// 111110011x00xxxxxxxxxx01xxxxxxxx
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.396 VST3 (single 3-element structure from one lane)
		// vst3<c>.<size> <list>, [<Rn>]{!}	vst3<c>.<size> <list>, [<Rn>], <Rm>
		// 1 1 1 1 1 0 0 1 1 D_1_6_6 0 0 Rn_1_3_0 Vd_0_15_12 size_0_11_10 1 0 index_align_0_7_4 Rm_0_3_0
		// 111110011x00xxxxxxxxxx10xxxxxxxx
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.398 VST4 (single 4-element structure from one lane)
		// vst4<c>.<size> <list>, [<Rn>{@<align>}]{!}	vst4<c>.<size> <list>, [<Rn>{@<align>}], <Rm>
		// 1 1 1 1 1 0 0 1 1 D_1_6_6 0 0 Rn_1_3_0 Vd_0_15_12 size_0_11_10 1 1 index_align_0_7_4 Rm_0_3_0
		// 111110011x00xxxxxxxxxx11xxxxxxxx
		//								
		// must precede thumb2_str in search table
		new OpcodeARM(Index.thumb2_vst__xlane, "vst", "111110011x00xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.193 STR (immediate, Thumb)
		// str<c>.w <Rt>,[<Rn>,#<imm12>]
		// 1 1 1 1 1 0 0 0 1 1 0 0 Rn_1_3_0 Rt_0_15_12 imm12_0_11_0
		// 111110001100xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding T4 ARMv6T2, ARMv7
		// A8.6.193 STR (immediate, Thumb)
		// str<c> <Rt>,[<Rn>,#-<imm8>]	str<c> <Rt>,[<Rn>],#+/-<imm8>	str<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// 1 1 1 1 1 0 0 0 0 1 0 0 Rn_1_3_0 Rt_0_15_12 1 P_0_10_10 U_0_9_9 W_0_8_8 imm8_0_7_0
		// 111110000100xxxxxxxx1xxxxxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.195 STR (register)
		// str<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// 1 1 1 1 1 0 0 0 0 1 0 0 Rn_1_3_0 Rt_0_15_12 0 0 0 0 0 0 imm2_0_5_4 Rm_0_3_0
		// 111110000100xxxxxxxx000000xxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.196 STRB (immediate, Thumb)
		// strb<c>.w <Rt>,[<Rn>,#<imm12>]
		// 1 1 1 1 1 0 0 0 1 0 0 0 Rn_1_3_0 Rt_0_15_12 imm12_0_11_0
		// 111110001000xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.196 STRB (immediate, Thumb)
		// strb<c> <Rt>,[<Rn>,#-<imm8>]	strb<c> <Rt>,[<Rn>],#+/-<imm8>	strb<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// 1 1 1 1 1 0 0 0 0 0 0 0 Rn_1_3_0 Rt_0_15_12 1 P_0_10_10 U_0_9_9 W_0_8_8 imm8_0_7_0
		// 111110000000xxxxxxxx1xxxxxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.198 STRB (register)
		// strb<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// 1 1 1 1 1 0 0 0 0 0 0 0 Rn_1_3_0 Rt_0_15_12 0 0 0 0 0 0 imm2_0_5_4 Rm_0_3_0
		// 111110000000xxxxxxxx000000xxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.199 STRBT
		// strbt<c> <Rt>,[<Rn>,#<imm8>]
		// 1 1 1 1 1 0 0 0 0 0 0 0 Rn_1_3_0 Rt_0_15_12 1 1 1 0 imm8_0_7_0
		// 111110000000xxxxxxxx1110xxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.206 STRH (immediate, Thumb)
		// strh<c>.w <Rt>,[<Rn>{,#<imm12>}]
		// 1 1 1 1 1 0 0 0 1 0 1 0 Rn_1_3_0 Rt_0_15_12 imm12_0_11_0
		// 111110001010xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.206 STRH (immediate, Thumb)
		// strh<c> <Rt>,[<Rn>,#-<imm8>]	strh<c> <Rt>,[<Rn>],#+/-<imm8>	strh<c> <Rt>,[<Rn>,#+/-<imm8>]!
		// 1 1 1 1 1 0 0 0 0 0 1 0 Rn_1_3_0 Rt_0_15_12 1 P_0_10_10 U_0_9_9 W_0_8_8 imm8_0_7_0
		// 111110000010xxxxxxxx1xxxxxxxxxxx
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.208 STRH (register)
		// strh<c>.w <Rt>,[<Rn>,<Rm>{,lsl #<imm2>}]
		// 1 1 1 1 1 0 0 0 0 0 1 0 Rn_1_3_0 Rt_0_15_12 0 0 0 0 0 0 imm2_0_5_4 Rm_0_3_0
		// 111110000010xxxxxxxx000000xxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.209 STRHT
		// strht<c> <Rt>,[<Rn>,#<imm8>]
		// 1 1 1 1 1 0 0 0 0 0 1 0 Rn_1_3_0 Rt_0_15_12 1 1 1 0 imm8_0_7_0
		// 111110000010xxxxxxxx1110xxxxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.210 STRT
		// strt<c> <Rt>,[<Rn>,#<imm8>]
		// 1 1 1 1 1 0 0 0 0 1 0 0 Rn_1_3_0 Rt_0_15_12 1 1 1 0 imm8_0_7_0
		// 111110000100xxxxxxxx1110xxxxxxxx
		//
		// must follow thumb2_push__reg in search table
		// must follow thumb2_vld__multi in search table
		// must follow thumb2_vld__xlane in search table
		// must follow thumb2_vst__multi in search table
		// must follow thumb2_vst__xlane in search table
		new OpcodeARM(Index.thumb2_str, "str",        "1111100xxxx0xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.202 STREX
		// strex<c> <Rd>,<Rt>,[<Rn>{,#<imm>}]
		// 1 1 1 0 1 0 0 0 0 1 0 0 Rn_1_3_0 Rt_0_15_12 Rd_0_11_8 imm8_0_7_0
		// 111010000100xxxxxxxxxxxxxxxxxxxx
		// must precede thumb2_strd in search table
		new OpcodeARM(Index.thumb2_strex,  "strex", "111010000100xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv7
		// A8.6.203 STREXB
		// strexb<c> <Rd>,<Rt>,[<Rn>]
		// 1 1 1 0 1 0 0 0 1 1 0 0 Rn_1_3_0 Rt_0_15_12 (1)(1)(1)(1) 0 1 0 0 Rd
		// 111010001100xxxxxxxx(1)(1)(1)(1)0100xxxx
		// must precede thumb2_strd in search table
		// NEW - Encoding T1 ARMv7
		// A8.6.204 STREXD
		// strexd<c> <Rd>,<Rt>,<Rt2>,[<Rn>]
		// 1 1 1 0 1 0 0 0 1 1 0 0 Rn_1_3_0 Rt_0_15_12 Rt2_0_11_8 0 1 1 1 Rd_0_3_0
		// 111010001100xxxxxxxxxxxx0111xxxx
		// NEW - Encoding T1 ARMv7
		// A8.6.205 STREXH
		// strexh<c> <Rd>,<Rt>,[<Rn>]
		// 1 1 1 0 1 0 0 0 1 1 0 0 Rn_1_3_0 Rt_0_15_12 (1)(1)(1)(1) 0 1 0 1 Rd
		// 111010001100xxxxxxxx(1)(1)(1)(1)0101xxxx
		//
		// must precede thumb2_strd in search table
		new OpcodeARM(Index.thumb2_strexx, "strex", "111010001100xxxxxxxxxxxx01xxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.200 STRD (immediate)
		// strd<c> <Rt>,<Rt2>,[<Rn>{,#+/-<imm>}]	strd<c> <Rt>,<Rt2>,[<Rn>],#+/-<imm>	strd<c> <Rt>,<Rt2>,[<Rn>,#+/-<imm>]!
		// 1 1 1 0 1 0 0 P_1_8_8 U_1_7_7 1 W_1_5_5 0 Rn_1_3_0 Rt_0_15_12 Rt2_0_11_8 imm8_0_7_0
		// 1110100xx1x0xxxxxxxxxxxxxxxxxxxx
		// must follow thumb2_strex in search table
		// must follow thumb2_strexx in search table
		new OpcodeARM(Index.thumb2_strd, "strd",  "1110100xx1x0xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T4 ARMv6T2, ARMv7
		// A8.6.211 SUB (immediate, Thumb)
		// subw<c> <Rd>,<Rn>,#<imm12>
		// 1 1 1 1 0 i_1_10_10 1 0 1 0 1 0 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x101010xxxx0xxxxxxxxxxxxxxx
		// must follow thumb2_adr__sub in search table
		// must follow thumb2_cmp__imm in search table
		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.215 SUB (SP minus immediate)
		// subw <Rd>,sp,#<imm12>
		// 1 1 1 1 0 i_1_10_10 1 0 1 0 1 0 1 1 0 1 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x10101011010xxxxxxxxxxxxxxx
		//
		new OpcodeARM(Index.thumb2_subw, "subw", "11110x101010xxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T3 ARMv6T2, ARMv7
		// A8.6.211 SUB (immediate, Thumb)
		// sub{s}<c>.w <Rd>,<Rn>,#<const>
		// 1 1 1 1 0 i_1_10_10 0 1 1 0 1 S_1_4_4 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x01101xxxxx0xxxxxxxxxxxxxxx
		// must follow thumb2_cmp__imm in search table
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.215 SUB (SP minus immediate)
		// sub{s}.w <Rd>,sp,#<const>
		// 1 1 1 1 0 i_1_10_10 0 1 1 0 1 S_1_4_4 1 1 0 1 0 imm3_0_14_12 Rd_0_11_8 imm8_0_7_0
		// 11110x01101x11010xxxxxxxxxxxxxxx
		// 
		new OpcodeARM(Index.thumb2_sub__imm, "sub", "11110x01101xxxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.213 SUB (register)
		// sub{s}<c>.w <Rd>,<Rn>,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 1 1 0 1 S_1_4_4 Rn_1_3_0 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101011101xxxxx(0)xxxxxxxxxxxxxxx
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.216 SUB (SP minus register)
		// sub{s} <Rd>,sp,<Rm>{,<shift>}
		// 1 1 1 0 1 0 1 1 1 0 1 S_1_4_4 1 1 0 1 (0) imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 type_0_5_4 Rm_0_3_0
		// 11101011101x1101(0)xxxxxxxxxxxxxxx
		//
		// must follow thumb2_cmp__reg in search table
		new OpcodeARM(Index.thumb2_sub__reg, "sub", "11101011101xxxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.223 SXTB
		// sxtb<c>.w <Rd>,<Rm>{,<rotation>}
		// 1 1 1 1 1 0 1 0 0 1 0 0 1 1 1 1 1 1 1 1 Rd_0_11_8 1 (0) rotate_0_5_4 Rm_0_3_0
		// 11111010010011111111xxxx1(0)xxxxxx
		// must precede thumb2_sxtab in search table
		new OpcodeARM(Index.thumb2_sxtb, "sxtb.w", "11111010010011111111xxxx1xxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.220 SXTAB
		// sxtab<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// 1 1 1 1 1 0 1 0 0 1 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 1 (0) rotate_0_5_4 Rm_0_3_0
		// 111110100100xxxx1111xxxx1(0)xxxxxx
		// must follow thumb2_sxtb in search table
		new OpcodeARM(Index.thumb2_sxtab, "sxtab", "111110100100xxxx1111xxxx1xxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.224 SXTB16
		// sxtb16<c> <Rd>,<Rm>{,<rotation>}
		// 1 1 1 1 1 0 1 0 0 0 1 0 1 1 1 1 1 1 1 1 Rd_0_11_8 1 (0) rotate_0_5_4 Rm_0_3_0
		// 11111010001011111111xxxx1(0)xxxxxx
		// must precede thumb2_sxtab16 in search table
		new OpcodeARM(Index.thumb2_sxtb16, "sxtb16", "11111010001011111111xxxx1xxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.221 SXTAB16
		// sxtab16<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// 1 1 1 1 1 0 1 0 0 0 1 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 1 (0) rotate_0_5_4 Rm_0_3_0
		// 111110100010xxxx1111xxxx1(0)xxxxxx
		// must follow thumb2_sxtb16 in search table
		new OpcodeARM(Index.thumb2_sxtab16, "sxtab16", "111110100010xxxx1111xxxx1xxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.225 SXTH
		// sxth<c>.w <Rd>,<Rm>{,<rotation>}
		// 1 1 1 1 1 0 1 0 0 0 0 0 1 1 1 1 1 1 1 1 Rd_0_11_8 1 (0) rotate_0_5_4 Rm_0_3_0
		// 11111010000011111111xxxx1(0)xxxxxx
		// must precede thumb2_sxtah in search table
		new OpcodeARM(Index.thumb2_sxth, "sxth.w", "11111010000011111111xxxx1xxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.222 SXTAH
		// sxtah<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// 1 1 1 1 1 0 1 0 0 0 0 0 Rn_1_3_0 1 1 1 1 Rd_0_11_8 1 (0) rotate_0_5_4 Rm_0_3_0
		// 111110100000xxxx1111xxxx1(0)xxxxxx
		// must follow thumb2_sxth in search table
		new OpcodeARM(Index.thumb2_sxtah, "sxtah", "111110100000xxxx1111xxxx1xxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.226 TBB, TBH
		// tbb<c> [<Rn>,<Rm>] Outside or last in IT block	tbh<c> [<Rn>,<Rm>,LSL #1] Outside or last in IT block
		// 1 1 1 0 1 0 0 0 1 1 0 1 Rn_1_3_0 (1) (1)(1)(1)(0)(0)(0)(0) 0 0 0 H_0_4_4 Rm_0_3_0
		// 111010001101xxxx(1)(1)(1)(1)(0)(0)(0)(0)000xxxxx
		new OpcodeARM(Index.thumb2_tb, "tb", "111010001101xxxxxxxxxxxx000xxxxx"),

		// NEW - Encoding T1 ARMv7-R
		// A8.6.237 UDIV
		// udiv<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 1 0 1 1 Rn_1_3_0 (1) (1)(1)(1) Rd_0_11_8 1 1 1 1 Rm_0_3_0
		// 111110111011xxxx(1)(1)(1)(1)xxxx1111xxxx
		new OpcodeARM(Index.thumb2_udiv, "udiv", "111110111011xxxxxxxxxxxx1111xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.244 UMAAL
		// umaal<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 1 1 1 0 Rn_1_3_0 RdLo_0_15_12 RdHi_0_11_8 0 1 1 0 Rm_0_3_0
		// 111110111110xxxxxxxxxxxx0110xxxx
		new OpcodeARM(Index.thumb2_umaal, "umaal", "111110111110xxxxxxxxxxxx0110xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.245 UMLAL
		// umlal<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 1 1 1 0 Rn_1_3_0 RdLo_0_15_12 RdHi_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110111110xxxxxxxxxxxx0000xxxx
		new OpcodeARM(Index.thumb2_umlal, "umlal", "111110111110xxxxxxxxxxxx0000xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.246 UMULL
		// umull<c> <RdLo>,<RdHi>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 1 0 1 0 Rn_1_3_0 RdLo_0_15_12 RdHi_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110111010xxxxxxxxxxxx0000xxxx
		new OpcodeARM(Index.thumb2_umull, "umull", "111110111010xxxxxxxxxxxx0000xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.253 USAD8
		// usad8<c> <Rd>,<Rn>,<Rm>
		// 1 1 1 1 1 0 1 1 0 1 1 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110110111xxxx1111xxxx0000xxxx
		new OpcodeARM(Index.thumb2_usad8, "usad8", "111110110111xxxx1111xxxx0000xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.254 USADA8
		// usada8<c> <Rd>,<Rn>,<Rm>,<Ra>
		// 1 1 1 1 1 0 1 1 0 1 1 1 Rn_1_3_0 Ra_0_15_12 Rd_0_11_8 0 0 0 0 Rm_0_3_0
		// 111110110111xxxxxxxxxxxx0000xxxx
		new OpcodeARM(Index.thumb2_usada8, "usada8", "111110110111xxxxxxxxxxxx0000xxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.256 USAT16
		// usat16<c> <Rd>,#<imm4>,<Rn>
		// 1 1 1 1 0 (0) 1 1 1 0 1 0 Rn_1_3_0 0 0 0 0 Rd_0_11_8 0 0 (0)(0) sat_imm_0_3_0
		// 11110(0)111010xxxx0000xxxx00(0)(0)xxxx
		// must precede thumb2_usat in search table
		new OpcodeARM(Index.thumb2_usat16, "usat16", "11110x111010xxxx0000xxxx00xxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.255 USAT
		// usat<c> <Rd>,#<imm5>,<Rn>{,<shift>}
		// 1 1 1 1 0 (0) 1 1 1 0 sh_1_5_5 0 Rn_1_3_0 0 imm3_0_14_12 Rd_0_11_8 imm2_0_7_6 (0) sat_imm_0_4_0
		// 11110(0)1110x0xxxx0xxxxxxxxx(0)xxxxx
		// must follow thumb2_usat16 in search table
		new OpcodeARM(Index.thumb2_usat, "usat", "11110x1110x0xxxx0xxxxxxxxxxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.263 UXTB
		// uxtb<c>.w <Rd>,<Rm>{,<rotation>}
		// 1 1 1 1 1 0 1 0 0 1 0 1 1 1 1 1 1 1 1 1 Rd_0_11_8 1 (0) rotate_0_5_4 Rm_0_3_0
		// 11111010010111111111xxxx1(0)xxxxxx
		// must precede thumb2_uxtab in search table
		new OpcodeARM(Index.thumb2_uxtb, "uxtb.w", "11111010010111111111xxxx1xxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.260 UXTAB
		// uxtab<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// 1 1 1 1 1 0 1 0 0 1 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 1 (0) rotate_0_5_4 Rm_0_3_0
		// 111110100101xxxx1111xxxx1(0)xxxxxx
		// must follow thumb2_uxtb in search table
		new OpcodeARM(Index.thumb2_uxtab, "uxtab", "111110100101xxxx1111xxxx1xxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.264 UXTB16
		// uxtb16<c> <Rd>,<Rm>{,<rotation>}
		// 1 1 1 1 1 0 1 0 0 0 1 1 1 1 1 1 1 1 1 1 Rd_0_11_8 1 (0) rotate_0_5_4 Rm_0_3_0
		// 11111010001111111111xxxx1(0)xxxxxx
		// must precede thumb2_uxtab16 in search table
		new OpcodeARM(Index.thumb2_uxtb16, "uxtb16", "11111010001111111111xxxx1xxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.261 UXTAB16
		// uxtab16<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// 1 1 1 1 1 0 1 0 0 0 1 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 1 (0) rotate_0_5_4 Rm_0_3_0
		// 111110100011xxxx1111xxxx1(0)xxxxxx
		// must follow thumb2_uxtb16 in search table
		new OpcodeARM(Index.thumb2_uxtab16, "uxtab16", "111110100011xxxx1111xxxx1xxxxxxx"),

		// NEW - Encoding T2 ARMv6T2, ARMv7
		// A8.6.265 UXTH
		// uxth<c>.w <Rd>,<Rm>{,<rotation>}
		// 1 1 1 1 1 0 1 0 0 0 0 1 1 1 1 1 1 1 1 1 Rd_0_11_8 1 (0) rotate_0_5_4 Rm_0_3_0
		// 11111010000111111111xxxx1(0)xxxxxx
		// must precede thumb2_uxtabh in search table
		new OpcodeARM(Index.thumb2_uxth, "uxth.w", "11111010000111111111xxxx1xxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// A8.6.262 UXTAH
		// uxtah<c> <Rd>,<Rn>,<Rm>{,<rotation>}
		// 1 1 1 1 1 0 1 0 0 0 0 1 Rn_1_3_0 1 1 1 1 Rd_0_11_8 1 (0) rotate_0_5_4 Rm_0_3_0
		// 111110100001xxxx1111xxxx1(0)xxxxxx
		// must follow thumb2_uxth in search table
		new OpcodeARM(Index.thumb2_uxtah, "uxtah", "111110100001xxxx1111xxxx1xxxxxxx"),



		// VFP and Advanced SIMD instructions

		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.266 VABA, VABAL
		// vaba<c>.<dt> <Qd>, <Qn>, <Qm>	vaba<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 1 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11110xxxxxxxxxxx0111xxx1xxxx
		new OpcodeARM(Index.thumb2_vaba, "vaba", "111x11110xxxxxxxxxxx0111xxx1xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.267 VABD, VABDL (integer)
		// vabd<c>.<dt> <Qd>, <Qn>, <Qm>	vabd<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 1 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111x11110xxxxxxxxxxx0111xxx0xxxx
		new OpcodeARM(Index.thumb2_vabd__int, "vabd", "111x11110xxxxxxxxxxx0111xxx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.268 VABD (floating-point)
		// vabd<c>.f32 <Qd>, <Qn>, <Qm>	vabd<c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 1 1 1 1 1 0 D_1_6_6 1 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111110x1xxxxxxxxx1101xxx0xxxx
		new OpcodeARM(Index.thumb2_vabd__f32, "vabd", "111111110x1xxxxxxxxx1101xxx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variant)
		// A8.6.269 VABS
		// vabs<c>.<dt> <Qd>, <Qm>	vabs<c>.<dt> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 1 Vd_0_15_12 0 F_0_10_10 1 1 0 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx01xxxx0x110xx0xxxx
		// must precede thumb2_vabdl
		new OpcodeARM(Index.thumb2_vabs, "vabs", "111111111x11xx01xxxx0x110xx0xxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.269 VABS
		// vabs<c>.f64 <Dd>, <Dm>	vabs<c>.f32 <Sd>, <Sm>
		// 1 1 1 0 1 1 1 0 1 D_1_6_6 1 1 0 0 0 0 Vd_0_15_12 1 0 1 sz_0_8_8 1 1 M_0_5_5 0 Vm_0_3_0
		// 111011101x110000xxxx101x11x0xxxx
		new OpcodeARM(Index.thumb2_vabs__f, "vabs", "111011101x110000xxxx101x11x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.270 VACGE, VACGT, VACLE, VACLT
		// vacge<c>.f32 <Qd>, <Qn>, <Qm>	vacge<c>.f32 <Dd>, <Dn>, <Dm>	vacgt<c>.f32 <Qd>, <Qn>, <Qm>	vacgt<c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 1 1 1 1 1 0 D_1_6_6 op_1_5_5 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 1 0 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111111110xxxxxxxxxxx1110xxx1xxxx
		new OpcodeARM(Index.thumb2_vacge_vacgt, "vac", "111111110xxxxxxxxxxx1110xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.271 VADD (integer)
		// vadd<c>.<dt> <Qd>, <Qn>, <Qm>	vadd<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 0 0 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111011110xxxxxxxxxxx1000xxx0xxxx
		new OpcodeARM(Index.thumb2_vadd__int, "vadd", "111011110xxxxxxxxxxx1000xxx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variants)
		// A8.6.272 VADD (floating-point)
		// vadd<c>.f32 <Qd>, <Qn>, <Qm>	vadd<c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 0 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111011110x0xxxxxxxxx1101xxx0xxxx
		new OpcodeARM(Index.thumb2_vadd__f32, "vadd", "111011110x0xxxxxxxxx1101xxx0xxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.272 VADD (floating-point)
		// vadd<c>.f64 <Dd>, <Dn>, <Dm>	vadd<c>.f32 <Sd>, <Sn>, <Sm>
		// 1 1 1 0 1 1 1 0 0 D_1_6_6 1 1 Vn_1_3_0 Vd_0_15_12 1 0 1 sz_0_8_8 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111011100x11xxxxxxxx101xx0x0xxxx
		new OpcodeARM(Index.thumb2_vadd__fp_f, "vadd", "111011100x11xxxxxxxx101xx0x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.273 VADDHN
		// vaddhn<c>.<dt> <Dd>, <Qn>, <Qm>
		// 1 1 1 0 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 0 0 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111011111xxxxxxxxxxx0100x0x0xxxx
		new OpcodeARM(Index.thumb2_vaddhn, "vaddhn", "111011111xxxxxxxxxxx0100x0x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.276 VAND (register)
		// vand<c> <Qd>, <Qn>, <Qm>	vand<c> <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 0 0 Vn_1_3_0 Vd_0_15_12 0 0 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111011110x00xxxxxxxx0001xxx1xxxx
		new OpcodeARM(Index.thumb2_vand, "vand", "111011110x00xxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.278 VBIC (register)
		// vbic<c> <Qd>, <Qn>, <Qm>	vbic<c> <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 0 1 Vn_1_3_0 Vd_0_15_12 0 0 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111011110x01xxxxxxxx0001xxx1xxxx
		new OpcodeARM(Index.thumb2_vbic__reg, "vbic", "111011110x01xxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.304 VEOR - MUST PRECEDE VBIF/VBIT/VBSL
		// veor<c> <Qd>, <Qn>, <Qm>	veor<c> <Dd>, <Dn>, <Dm>
		// 1 1 1 1 1 1 1 1 0 D_1_6_6 0 0 Vn_1_3_0 Vd_0_15_12 0 0 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
// see thumb2_vbif_vbit_vbsl_veor
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.279 VBIF, VBIT, VBSL
		// vbif<c> <Qd>, <Qn>, <Qm>	vbif<c> <Dd>, <Dn>, <Dm>	vbit<c> <Qd>, <Qn>, <Qm>	vbit<c> <Dd>, <Dn>, <Dm>	vbsl<c> <Qd>, <Qn>, <Qm>	vbsl<c> <Dd>, <Dn>, <Dm>
		// 1 1 1 1 1 1 1 1 0 D_1_6_6 op_1_5_4 Vn_1_3_0 Vd_0_15_12 0 0 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		//
		// 111111110x00xxxxxxxx0001xxx1xxxx
		// 111111110xxxxxxxxxxx0001xxx1xxxx
		new OpcodeARM(Index.thumb2_vbif_vbit_vbsl_veor, "v", "111111110xxxxxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.280 VCEQ (register)
		// vceq<c>.<dt> <Qd>, <Qn>, <Qm>	vceq<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1 1 1 1 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 0 0 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111111110xxxxxxxxxxx1000xxx1xxxx
		new OpcodeARM(Index.thumb2_vceq__reg_int, "vceq", "111111110xxxxxxxxxxx1000xxx1xxxx"),
		// NEW - Encoding T2 / A2 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.280 VCEQ (register)
		// vceq<c>.f32 <Qd>, <Qn>, <Qm>	vceq<c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 0 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 1 0 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111011110x0xxxxxxxxx1110xxx0xxxx
		new OpcodeARM(Index.thumb2_vceq__reg_f32, "vceq", "111011110x0xxxxxxxxx1110xxx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.281 VCEQ (immediate #0)
		// vceq<c>.<dt> <Qd>, <Qm>, #0	vceq<c>.<dt> <Dd>, <Dm>, #0
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 1 Vd_0_15_12 0 F_0_10_10 0 1 0 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx01xxxx0x010xx0xxxx
		// must precede thumb2_vaddl_vaddw
		// must precede thumb2_vabal
		new OpcodeARM(Index.thumb2_vceq__imm0, "vceq", "111111111x11xx01xxxx0x010xx0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.282 VCGE (register)
		// vcge<c>.<dt> <Qd>, <Qn>, <Qm>	vcge<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 0 1 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11110xxxxxxxxxxx0011xxx1xxxx
		new OpcodeARM(Index.thumb2_vcge__reg_int, "vcge", "111x11110xxxxxxxxxxx0011xxx1xxxx"),
		// NEW - Encoding T2 / A2 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.282 VCGE (register)
		// vcge<c>.f32 <Qd>, <Qn>, <Qm>	vcge<c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 1 1 1 1 1 0 D_1_6_6 0 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 1 0 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111110x0xxxxxxxxx1110xxx0xxxx
		new OpcodeARM(Index.thumb2_vcge__reg_f32, "vcge", "111111110x0xxxxxxxxx1110xxx0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.284 VCGT (register)
		// vcgt<c>.<dt> <Qd>, <Qn>, <Qm>	vcgt<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 0 1 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111x11110xxxxxxxxxxx0011xxx0xxxx
		new OpcodeARM(Index.thumb2_vcgt__reg_int, "vcgt", "111x11110xxxxxxxxxxx0011xxx0xxxx"),
		// NEW - Encoding T2 / A2 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.284 VCGT (register)
		// vcgt<c>.f32 <Qd>, <Qn>, <Qm>	vcgt<c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 1 1 1 1 1 0 D_1_6_6 1 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 1 0 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111110x1xxxxxxxxx1110xxx0xxxx
		new OpcodeARM(Index.thumb2_vcgt__reg_f32, "vcgt", "111111110x1xxxxxxxxx1110xxx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.283 VCGE (immediate #0)
		// vcge<c>.<dt> <Qd>, <Qm>, #0	vcge<c>.<dt> <Dd>, <Dm>, #0
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 1 Vd_0_15_12 0 F_0_10_10 0 0 1 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx01xxxx0x001xx0xxxx
		// must precede thumb2_vaddl_vaddw
		new OpcodeARM(Index.thumb2_vcge__imm0, "vcge", "111111111x11xx01xxxx0x001xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.285 VCGT (immediate #0)
		// vcgt<c>.<dt> <Qd>, <Qm>, #0	vcgt<c>.<dt> <Dd>, <Dm>, #0
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 1 Vd_0_15_12 0 F_0_10_10 0 0 0 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx01xxxx0x000xx0xxxx
		// must precede thumb2_vaddl_vaddw
		new OpcodeARM(Index.thumb2_vcgt__imm0, "vcgt", "111111111x11xx01xxxx0x000xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.287 VCLE (immediate #0)
		// vcle<c>.<dt> <Qd>, <Qm>, #0	vcle<c>.<dt> <Dd>, <Dm>, #0
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 1 Vd_0_15_12 0 F_0_10_10 0 1 1 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx01xxxx0x011xx0xxxx
		// must precede thumb2_vaddl_vaddw
		new OpcodeARM(Index.thumb2_vcle, "vcle", "111111111x11xx01xxxx0x011xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.288 VCLS
		// vcls<c>.<dt> <Qd>, <Qm>	vcls<c>.<dt> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 0 Vd_0_15_12 0 1 0 0 0 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx00xxxx01000xx0xxxx
		new OpcodeARM(Index.thumb2_vcls, "vcls", "111111111x11xx00xxxx01000xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.290 VCLT (immediate #0)
		// vclt<c>.<dt> <Qd>, <Qm>, #0	vclt<c>.<dt> <Dd>, <Dm>, #0
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 1 Vd_0_15_12 0 F_0_10_10 1 0 0 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx01xxxx0x100xx0xxxx
		new OpcodeARM(Index.thumb2_vclt, "vclt", "111111111x11xx01xxxx0x100xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.291 VCLZ
		// vclz<c>.<dt> <Qd>, <Qm>	vclz<c>.<dt> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 0 Vd_0_15_12 0 1 0 0 1 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx00xxxx01001xx0xxxx
		new OpcodeARM(Index.thumb2_vclz, "vclz", "111111111x11xx00xxxx01001xx0xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.292 VCMP, VCMPE
		// vcmp{e}<c>.f64 <Dd>, <Dm>	vcmp{e}<c>.f32 <Sd>, <Sm>
		// 1 1 1 0 1 1 1 0 1 D_1_6_6 1 1 0 1 0 0 Vd_0_15_12 1 0 1 sz_0_8_8 E_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111011101x110100xxxx101xx1x0xxxx
		new OpcodeARM(Index.thumb2_vcmp__reg, "vcmp", "111011101x110100xxxx101xx1x0xxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.292 VCMP, VCMPE
		// vcmp{e}<c>.f64 <Dd>, #0.0	vcmp{e}<c>.f32 <Sd>, #0.0
		// 1 1 1 0 1 1 1 0 1 D_1_6_6 1 1 0 1 0 1 Vd_0_15_12 1 0 1 sz_0_8_8 E_0_7_7 1 (0) 0 (0)(0)(0)(0)
		// 111011101x110101xxxx101xx1(0)0(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_vcmp__to_0, "vcmp", "111011101x110101xxxx101xx1x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.293 VCNT
		// vcnt<c>.8 <Qd>, <Qm>	vcnt<c>.8 <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 0 Vd_0_15_12 0 1 0 1 0 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx00xxxx01010xx0xxxx
		new OpcodeARM(Index.thumb2_vcnt, "vcnt", "111111111x11xx00xxxx01010xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.294 VCVT (between floating-point and integer, Advanced SIMD)
		// vcvt<c>.<Td>.<Tm> <Qd>, <Qm>	vcvt<c>.<Td>.<Tm> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 1 Vd_0_15_12 0 1 1 op_0_8_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx11xxxx011xxxx0xxxx
		new OpcodeARM(Index.thumb2_vcvt__fp_i_vec, "vcvt", "111111111x11xx11xxxx011xxxx0xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv3 (sf = 1 UNDEFINED in single-precision only variants)
		// A8.6.297 VCVT (between floating-point and fixed-point, VFP)
		// vcvt<c>.<Td>.f64 <Dd>, <Dd>, #<fbits>	vcvt<c>.<Td>.f32 <Sd>, <Sd>, #<fbits>	vcvt<c>.f64.<Td> <Dd>, <Dd>, #<fbits>	vcvt<c>.f32.<Td> <Sd>, <Sd>, #<fbits>
		// 1 1 1 0 1 1 1 0 1 D_1_6_6 1 1 1 op_1_2_2 1 U_1_0_0 Vd_0_15_12 1 0 1 sf_0_8_8 sx_0_7_7 1 i_0_5_5 0 imm4_0_3_0
		// 111011101x111x1xxxxx101xx1x0xxxx
		// must precede thumb2_vcvt__fp_i_reg
		new OpcodeARM(Index.thumb2_vcvt__fp_fix_reg, "vcvt", "111011101x111x1xxxxx101xx1x0xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.295 VCVT, VCVTR (between floating-point and integer, VFP)
		// vcvt{r}<c>.s32.f64 <Sd>, <Dm>	vcvt{r}<c>.s32.f32 <Sd>, <Sm>	vcvt{r}<c>.u32.f64 <Sd>, <Dm>	vcvt{r}<c>.u32.f32 <Sd>, <Sm>	vcvt<c>.f64.<Tm> <Dd>, <Sm>	vcvt<c>.f32.<Tm> <Sd>, <Sm>
		// 1 1 1 0 1 1 1 0 1 D_1_6_6 1 1 1 opc2_1_2_0 Vd_0_15_12 1 0 1 sz_0_8_8 op_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111011101x111xxxxxxx101xx1x0xxxx
		// must follow thumb2_vcvt__fp_fix_reg
		new OpcodeARM(Index.thumb2_vcvt__fp_i_reg, "vcvt", "111011101x111xxxxxxx101xx1x0xxxx"),
		// NEW - Encoding T2 / A2 (U) Advanced SIMD
		// A8.6.325 VMLA, VMLAL, VMLS, VMLSL (by scalar)
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 op_0_10_10 1 0 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx0x10x1x0xxxx
		// NEW - Encoding T1 / A1 (i) Advanced SIMD
		// A8.6.277 VBIC (immediate)
		// vbic<c>.<dt> <Qd>, #<imm>	vbic<c>.<dt> <Dd>, #<imm>
		// 1 1 1 i_1_12_12 1 1 1 1 1 D_1_6_6 0 0 0 imm3_1_2_0 Vd_0_15_12 cmode_0_11_8 0 Q_0_6_6 1 1 imm4_0_3_0
// see thumb2_vmov_vbitwise
		// NEW - Encoding T1 / A1 (i) Advanced SIMD
		// A8.6.346 VORR (immediate)
		// vorr<c>.<dt> <Qd>, #<imm>	vorr<c>.<dt> <Dd>, #<imm>
		// 1 1 1 i_1_12_12 1 1 1 1 1 D_1_6_6 0 0 0 imm3_1_2_0 Vd_0_15_12 cmode_0_11_8 0 Q_0_6_6 0 1 imm4_0_3_0
// see thumb2_vmov_vbitwise
		// NEW - Encoding T1 / A1 (i) Advanced SIMD
		// A8.6.340 VMVN (immediate)
		// vmvn<c>.<dt> <Qd>, #<imm>	vmvn<c>.<dt> <Dd>, #<imm>
		// 1 1 1 i_1_12_12 1 1 1 1 1 D_1_6_6 0 0 0 imm3_1_2_0 Vd_0_15_12 cmode_0_11_8 0 Q_0_6_6 1 1 imm4_0_3_0
// see thumb2_vmov_vbitwise
		// NEW - Encoding T1 / A1 (i) Advanced SIMD
		// A8.6.326 VMOV (immediate)
		// vmov<c>.<dt> <Qd>, #<imm>	vmov<c>.<dt> <Dd>, #<imm>
		// 1 1 1 i_1_12_12 1 1 1 1 1 D_1_6_6 0 0 0 imm3_1_2_0 Vd_0_15_12 cmode_0_11_8 0 Q_0_6_6 op_0_5_5 1 imm4_0_3_0
		//
		// 111x11111x000xxxxxxxxxxx0x11xxxx
		// 111x11111x000xxxxxxxxxxx0x01xxxx
		// 111x11111x000xxxxxxxxxxx0x11xxxx
		// 111x11111x000xxxxxxxxxxx0xx1xxxx
		// must precede thumb2_vcvt__fp_fix_vec
		new OpcodeARM(Index.thumb2_vmov_vbitwise, "_", "111x11111x000xxxxxxxxxxx0xx1xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.296 VCVT (between floating-point and fixed-point, Advanced SIMD)
		// vcvt<c>.<Td>.<Tm> <Qd>, <Qm>, #<fbits>	vcvt<c>.<Td>.<Tm> <Dd>, <Dm>, #<fbits>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 1 1 1 op_0_8_8 0 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11111xxxxxxxxxxx111x0xx1xxxx
		// must follow thumb2_vmov_vbitwise
		new OpcodeARM(Index.thumb2_vcvt__fp_fix_vec, "vcvt", "111x11111xxxxxxxxxxx111x0xx1xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3 (UNDEFINED in single-precision only variants)
		// A8.6.298 VCVT (between double-precision and single-precision)
		// vcvt<c>.f64.f32 <Dd>, <Sm>	vcvt<c>.f32.f64 <Sd>, <Dm>
		// 1 1 1 0 1 1 1 0 1 D_1_6_6 1 1 0 1 1 1 Vd_0_15_12 1 0 1 sz_0_8_8 1 1 M_0_5_5 0 Vm_0_3_0
		// 111011101x110111xxxx101x11x0xxxx
		new OpcodeARM(Index.thumb2_vcvt__dp_sp, "vcvt", "111011101x110111xxxx101x11x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD with half-precision extensions (UNDEFINED in integer-only variant)
		// A8.6.299 VCVT (between half-precision and single-precision, Advanced SIMD)
		// vcvt<c>.f32.f16 <Qd>, <Dm>	vcvt<c>.f16.f32 <Dd>, <Qm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 0 Vd_0_15_12 0 1 1 op_0_8_8 0 0 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx10xxxx011x00x0xxxx
		new OpcodeARM(Index.thumb2_vcvt__hp_sp_vec, "vcvt", "111111111x11xx10xxxx011x00x0xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv3 half-precision extensions
		// A8.6.300 VCVTB, VCVTT (between half-precision and single-precision, VFP)
		// vcvt<y><c>.f32.f16 <Sd>, <Sm>	vcvt<y><c>.f16.f32 <Sd>, <Sm>
		// 1 1 1 0 1 1 1 0 1 D_1_6_6 1 1 0 0 1 op_1_0_0 Vd_0_15_12 1 0 1 (0) T_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111011101x11001xxxxx1010x1x0xxxx
		new OpcodeARM(Index.thumb2_vcvt__hp_sp_reg, "vcvt", "111011101x11001xxxxx1010x1x0xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.301 VDIV
		// vdiv<c>.f64 <Dd>, <Dn>, <Dm>	vdiv<c>.f32 <Sd>, <Sn>, <Sm>
		// 1 1 1 0 1 1 1 0 1 D_1_6_6 0 0 Vn_1_3_0 Vd_0_15_12 1 0 1 sz_0_8_8 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111011101x00xxxxxxxx101xx0x0xxxx
		new OpcodeARM(Index.thumb2_vdiv, "vdiv", "111011101x00xxxxxxxx101xx0x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.302 VDUP (scalar)
		// vdup<c>.<size> <Qd>, <Dm[x]>	vdup<c>.<size> <Dd>, <Dm[x]>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 imm4_1_3_0 Vd_0_15_12 1 1 0 0 0 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xxxxxxxx11000xx0xxxx
		new OpcodeARM(Index.thumb2_vdup__scalar, "vdup", "111111111x11xxxxxxxx11000xx0xxxx"),
		// NEW - Encoding T1 / A1 (cond) Advanced SIMD
		// A8.6.303 VDUP (ARM core register)
		// vdup<c>.<size> <Qd>, <Rt>	vdup<c>.<size> <Dd>, <Rt>
		// 1 1 1 0 1 1 1 0 1 b_1_6_6 Q_1_5_5 0 Vd_1_3_0 Rt_0_15_12 1 0 1 1 D_1_6_6 0 e_0_5_5 1 (0)(0)(0)(0)
		// 111011101xx0xxxxxxxx1011x0x1(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_vdup__reg, "vdup", "111011101xx0xxxxxxxx1011x0x1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.305 VEXT
		// vext<c>.8 <Qd>, <Qn>, <Qm>, #<imm>	vext<c>.8 <Dd>, <Dn>, <Dm>, #<imm>
		// 1 1 1 0 1 1 1 1 1 D_1_6_6 1 1 Vn_1_3_0 Vd_0_15_12 imm4_0_11_8 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111011111x11xxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.thumb2_vext, "vext.8", "111011111x11xxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.306 VHADD, VHSUB
		// vh<op><c> <Qd>, <Qn>, <Qm>	vh<op><c> <Dd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 0 op_0_9_9 0 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111x11110xxxxxxxxxxx00x0xxx0xxxx
		new OpcodeARM(Index.thumb2_vhadd_vhsub, "vh", "111x11110xxxxxxxxxxx00x0xxx0xxxx"),

		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.320 VLDR
		// vldr<c> <Dd>, [<Rn>{, #+/-<imm>}]	vldr<c> <Dd>, <label>	vldr<c> <Dd>, [pc,#-0] Special case
		// 1 1 1 0 1 1 0 1 U_1_7_7 D_1_6_6 0 1 Rn_1_3_0 Vd_0_15_12 1 0 1 1 imm8_0_7_0
		// 11101101xx01xxxxxxxx1011xxxxxxxx
		// must precede thumb2_vldm__64 in search table
		new OpcodeARM(Index.thumb2_vldr__64, "vldr", "11101101xx01xxxxxxxx1011xxxxxxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.354 VPOP
		// vpop <list> <list> is consecutive 64-bit registers
		// 1 1 1 0 1 1 0 0 1 D_1_6_6 1 1 1 1 0 1 Vd_0_15_12 1 0 1 1 imm8_0_7_0
		// 111011001x111101xxxx1011xxxxxxxx
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3
		// A8.6.354 VPOP
		// vpop <list> <list> is consecutive 32-bit registers
		// 1 1 1 0 1 1 0 0 1 D_1_6_6 1 1 1 1 0 1 Vd_0_15_12 1 0 1 0 imm8_0_7_0
		// 111011001x111101xxxx1010xxxxxxxx
		// must precede thumb2_vldm__32 in search table
		// must precede thumb2_vldm__64 in search table
//		new OpcodeARM(Index.thumb2_vpop_1, "vpop", "111011001x111101xxxx1011xxxxxxxx"),
//		new OpcodeARM(Index.thumb2_vpop_2, "vpop", "111011001x111101xxxx1010xxxxxxxx"),
		new OpcodeARM(Index.thumb2_vpop,   "vpop", "111011001x111101xxxx101xxxxxxxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.332 VMOV (between two ARM core registers and a doubleword extension register)
		// vmov<c> <Dm>, <Rt>, <Rt2>	vmov<c> <Rt>, <Rt2>, <Dm>
		// 1 1 1 0 1 1 0 0 0 1 0 op_1_4_4 Rt2_1_3_0 Rt_0_15_12 1 0 1 1 0 0 M_0_5_5 1 Vm_0_3_0
		// 11101100010xxxxxxxxx101100x1xxxx
		// must precede thumb2_vldm_32 in search table
		// must precede thumb2_vldm_64 in search table
		new OpcodeARM(Index.thumb2_vmov_9, "vmov", "11101100010xxxxxxxxx101100x1xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.319 VLDM
		// vldm{mode}<c> <Rn>{!}, <list> <list> is consecutive 64-bit registers
		// 1 1 1 0 1 1 0 P_1_8_8 U_1_7_7 D_1_6_6 W_1_5_5 1 Rn_1_3_0 Vd_0_15_12 1 0 1 1 imm8_0_7_0
		// 1110110xxxx1xxxxxxxx1011xxxxxxxx
		// must follow thumb2_vldr_1 in search table
		// must follow thumb2_vpop in search table
		// must follow thumb2_vmov_9
		new OpcodeARM(Index.thumb2_vldm__64, "vldm", "1110110xxxx1xxxxxxxx1011xxxxxxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3
		// A8.6.320 VLDR
		// vldr<c> <Sd>, [<Rn>{, #+/-<imm>}]	vldr<c> <Sd>, <label>	vldr<c> <Sd>, [pc,#-0] Special case
		// 1 1 1 0 1 1 0 1 U_1_7_7 D_1_6_6 0 1 Rn_1_3_0 Vd_0_15_12 1 0 1 0 imm8_0_7_0
		// 11101101xx01xxxxxxxx1010xxxxxxxx
		// must precede thumb2_vldm__32 in search table
		new OpcodeARM(Index.thumb2_vldr__32, "vldr", "11101101xx01xxxxxxxx1010xxxxxxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3
		// A8.6.331 VMOV (between two ARM core registers and two single-precision registers)
		// vmov<c> <Sm>, <Sm1>, <Rt>, <Rt2>	vmov<c> <Rt>, <Rt2>, <Sm>, <Sm1>
		// 1 1 1 0 1 1 0 0 0 1 0 op_1_4_4 Rt2_1_3_0 Rt_0_15_12 1 0 1 0 0 0 M_0_5_5 1 Vm_0_3_0
		// 11101100010xxxxxxxxx101000x1xxxx
		// must precede thumb2_vldm__32 in search table
		new OpcodeARM(Index.thumb2_vmov_8, "vmov", "11101100010xxxxxxxxx101000x1xxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3
		// A8.6.319 VLDM
		// vldm{mode}<c> <Rn>{!}, <list> <list> is consecutive 32-bit registers
		// 1 1 1 0 1 1 0 P_1_8_8 U_1_7_7 D_1_6_6 W_1_5_5 1 Rn_1_3_0 Vd_0_15_12 1 0 1 0 imm8_0_7_0
		// 1110110xxxx1xxxxxxxx1010xxxxxxxx
		// must follow thumb2_vldr__32 in search table
		// must follow thumb2_vpop in search table
		// must follow thumb2_vmov_8
		new OpcodeARM(Index.thumb2_vldm__32, "vldm", "1110110xxxx1xxxxxxxx1010xxxxxxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.321 VMAX, VMIN (integer)
		// vmax<c>.<dt> <Qd>, <Qn>, <Qm>	vmax<c>.<dt> <Dd>, <Dn>, <Dm>	vmin<c>.<dt> <Qd>, <Qn>, <Qm>	vmin<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 1 0 N_0_7_7 Q_0_6_6 M_0_5_5 op_0_4_4 Vm_0_3_0
		// 111x11110xxxxxxxxxxx0110xxxxxxxx
		new OpcodeARM(Index.thumb2_vmax_vmin__int, "v", "111x11110xxxxxxxxxxx0110xxxxxxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.322 VMAX, VMIN (floating-point)
		// vmax<c>.f32 <Qd>, <Qn>, <Qm>	vmax<c>.f32 <Dd>, <Dn>, <Dm>	vmin<c>.f32 <Qd>, <Qn>, <Qm>	vmin<c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 op_1_5_5 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 1 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111011110xxxxxxxxxxx1111xxx0xxxx
		new OpcodeARM(Index.thumb2_vmax_vmin__fp, "v", "111011110xxxxxxxxxxx1111xxx0xxxx"),
		// NEW - Encoding T1 / A1 (op) Advanced SIMD
		// A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
		// v<op><c>.<dt> <Qd>, <Qn>, <Qm>	v<op><c>.<dt> <Dd>, <Dn>, <Dm>
		// 1 1 1 op_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111x11110xxxxxxxxxxx1001xxx0xxxx
		new OpcodeARM(Index.thumb2_vml__int, "vml", "111x11110xxxxxxxxxxx1001xxx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.406 VTBL, VTBX
		// v<op><c>.8 <Dd>, <list>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 Vn_1_3_0 Vd_0_15_12 1 0 len_0_9_8 N_0_7_7 op_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xxxxxxxx10xxxxx0xxxx
		// must precede thumb2_vml__int_long
		new OpcodeARM(Index.thumb2_vtb, "vtb", "111111111x11xxxxxxxx10xxxxx0xxxx"),
		// NEW - Encoding T2 / A2 (U) Advanced SIMD
		// A8.6.323 VMLA, VMLAL, VMLS, VMLSL (integer)
		// v<op>l<c>.<dt> <Qd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 op_0_9_9 0 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx10x0x0x0xxxx
		// must follow thumb2_vtb
		new OpcodeARM(Index.thumb2_vml__int_long, "vml", "111x11111xxxxxxxxxxx10x0x0x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.324 VMLA, VMLS (floating-point)
		// v<op><c>.f32 <Qd>, <Qn>, <Qm>	v<op><c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 op_1_5_5 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111011110xxxxxxxxxxx1101xxx1xxxx
		new OpcodeARM(Index.thumb2_vml__f32, "vml", "111011110xxxxxxxxxxx1101xxx1xxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.324 VMLA, VMLS (floating-point)
		// v<op><c>.f64 <Dd>, <Dn>, <Dm>	v<op><c>.f32 <Sd>, <Sn>, <Sm>
		// 1 1 1 0 1 1 1 0 0 D_1_6_6 0 0 Vn_1_3_0 Vd_0_15_12 1 0 1 sz_0_8_8 N_0_7_7 op_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111011100x00xxxxxxxx101xxxx0xxxx
		new OpcodeARM(Index.thumb2_vml__fp, "vml", "111011100x00xxxxxxxx101xxxx0xxxx"),

		// NEW - Encoding T2 / A2 (U) Advanced SIMD
		// A8.6.325 VMLA, VMLAL, VMLS, VMLSL (by scalar)
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 op_0_10_10 1 0 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx0x10x1x0xxxx
		// NEW - Encoding T1 / A1 (i) Advanced SIMD
		// A8.6.277 VBIC (immediate)
		// vbic<c>.<dt> <Qd>, #<imm>	vbic<c>.<dt> <Dd>, #<imm>
		// 1 1 1 i_1_12_12 1 1 1 1 1 D_1_6_6 0 0 0 imm3_1_2_0 Vd_0_15_12 cmode_0_11_8 0 Q_0_6_6 1 1 imm4_0_3_0
		// NEW - Encoding T1 / A1 (i) Advanced SIMD
		// A8.6.326 VMOV (immediate)
		// vmov<c>.<dt> <Qd>, #<imm>	vmov<c>.<dt> <Dd>, #<imm>
		// 1 1 1 i_1_12_12 1 1 1 1 1 D_1_6_6 0 0 0 imm3_1_2_0 Vd_0_15_12 cmode_0_11_8 0 Q_0_6_6 op_0_5_5 1 imm4_0_3_0
		// NEW - Encoding T1 / A1 (i) Advanced SIMD
		// A8.6.346 VORR (immediate)
		// vorr<c>.<dt> <Qd>, #<imm>	vorr<c>.<dt> <Dd>, #<imm>
		// 1 1 1 i_1_12_12 1 1 1 1 1 D_1_6_6 0 0 0 imm3_1_2_0 Vd_0_15_12 cmode_0_11_8 0 Q_0_6_6 0 1 imm4_0_3_0
		// NEW - Encoding T1 / A1 (i) Advanced SIMD
		// A8.6.340 VMVN (immediate)
		// vmvn<c>.<dt> <Qd>, #<imm>	vmvn<c>.<dt> <Dd>, #<imm>
		// 1 1 1 i_1_12_12 1 1 1 1 1 D_1_6_6 0 0 0 imm3_1_2_0 Vd_0_15_12 cmode_0_11_8 0 Q_0_6_6 1 1 imm4_0_3_0
		//
		// 111x11111x000xxxxxxxxxxx0x11xxxx
		// 111x11111x000xxxxxxxxxxx0x01xxxx
		// 111x11111x000xxxxxxxxxxx0x11xxxx
		// 111x11111x000xxxxxxxxxxx0xx1xxxx
		// must follow thumb2_vorr__imm in search table
		new OpcodeARM(Index.thumb2_vmov_vbitwise, null, "111x11111x000xxxxxxxxxxx0xx1xxxx"),

		// NEW - Encoding T2 / A2 (cond) VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.326 VMOV (immediate)
		// vmov<c>.f64 <Dd>, #<imm>	vmov<c>.f32 <Sd>, #<imm>
		// 1 1 1 0 1 1 1 0 1 D_1_6_6 1 1 imm4H_1_3_0 Vd_0_15_12 1 0 1 sz_0_8_8 (0) 0 (0) 0 imm4L_0_3_0
		// 111011101x11xxxxxxxx101x(0)0(0)0xxxx
		new OpcodeARM(Index.thumb2_vmov__imm, "vmov", "111011101x11xxxxxxxx101xx0x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.347 VORR (register)
		// vorr<c> <Qd>, <Qn>, <Qm>	vorr<c> <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 1 0 Vn_1_3_0 Vd_0_15_12 0 0 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111011110x10xxxxxxxx0001xxx1xxxx
		// must precede thumb2_vmov__reg
		new OpcodeARM(Index.thumb2_vorr__reg, "vorr", "111011110x10xxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.327 VMOV (register)
		// vmov<c> <Qd>, <Qm>	vmov<c> <Dd>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 1 0 Vm_1_3_0 Vd_0_15_12 0 0 0 1 M_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111011110x10xxxxxxxx0001xxx1xxxx
		// must follow thumb2_vorr__reg
		new OpcodeARM(Index.thumb2_vmov__reg, "vmov", "111011110x10xxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.327 VMOV (register)
		// vmov<c>.f64 <Dd>, <Dm>	vmov<c>.f32 <Sd>, <Sm>
		// 1 1 1 0 1 1 1 0 1 D_1_6_6 1 1 0 0 0 0 Vd_0_15_12 1 0 1 sz_0_8_8 0 1 M_0_5_5 0 Vm_0_3_0
		// 111011101x110000xxxx101x01x0xxxx
		new OpcodeARM(Index.thumb2_vmov__reg_f, "vmov", "111011101x110000xxxx101x01x0xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3, Advanced SIMD if opc1 == ’0x’ && opc2 == '00'; Advanced SIMD otherwise
		// A8.6.328 VMOV (ARM core register to scalar)
		// vmov<c>.<size> <Dd[x]>, <Rt>
		// 1 1 1 0 1 1 1 0 0 opc1_1_6_5 0 Vd_1_3_0 Rt_0_15_12 1 0 1 1 D_0_7_7 opc2_0_6_5 1 (0)(0)(0)(0)
		// 111011100xx0xxxxxxxx1011xxx1(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_vmov_5, "vmov", "111011100xx0xxxxxxxx1011xxx1xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3, Advanced SIMD if opc1 == '0x' && opc2 == '00';Advanced SIMD otherwise
		// A8.6.329 VMOV (scalar to ARM core register)
		// vmov<c>.<dt> <Rt>, <Dn[x]>
		// 1 1 1 0 1 1 1 0 U_1_7_7 opc1_1_6_5 1 Vn_1_3_0 Rt_0_15_12 1 0 1 1 N_0_7_7 opc2_0_6_5 1 (0)(0)(0)(0)
		// 11101110xxx1xxxxxxxx1011xxx1(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_vmov_6, "vmov", "11101110xxx1xxxxxxxx1011xxx1xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3
		// A8.6.330 VMOV (between ARM core register and single-precision register)
		// vmov<c> <Sn>, <Rt>	vmov<c> <Rt>, <Sn>
		// 1 1 1 0 1 1 1 0 0 0 0 op_1_4_4 Vn_1_3_0 Rt_0_15_12 1 0 1 0 N_0_7_7 (0)(0) 1 (0)(0)(0)(0)
		// 11101110000xxxxxxxxx1010x(0)(0)1(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_vmov_7, "vmov", "11101110000xxxxxxxxx1010xxx1xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.333 VMOVL
		// vmovl<c>.<dt> <Qd>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 imm3_5_3 0 0 0 Vd_0_15_12 1 0 1 0 0 0 M_0_5_5 1 Vm_0_3_0
		// 111x11111xxxx000xxxx101000x1xxxx
		// must precede thumb2_vshll__various  in search table
		new OpcodeARM(Index.thumb2_vmovl, "vmovl", "111x11111xxxx000xxxx101000x1xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.384 VSHLL
		// vshll<c>.<type><size> <Qd>, <Dm>, #<imm> (0 < <imm> < <size>)
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 1 0 1 0 0 0 M_0_5_5 1 Vm_0_3_0
		// 111x11111xxxxxxxxxxx101000x1xxxx
		// must follow thumb2_vmovl in search table
		new OpcodeARM(Index.thumb2_vshll__various, "vshll", "111x11111xxxxxxxxxxx101000x1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.334 VMOVN
		// vmovn<c>.<dt> <Dd>, <Qm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 0 Vd_0_15_12 0 0 1 0 0 0 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx10xxxx001000x0xxxx
		// must precede thumb2_vqmov in search table
		new OpcodeARM(Index.thumb2_vmovn, "vmovn", "111111111x11xx10xxxx001000x0xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.335 VMRS
		// vmrs<c> <Rt>, fpscr
		// 1 1 1 0 1 1 1 0 1 1 1 1 0 0 0 1 Rt_0_15_12 1 0 1 0 0 (0)(0) 1 (0)(0)(0)(0)
		// 1110111011110001xxxx10100(0)(0)1(0)(0)(0)(0)
		// NEW - Encoding T1 /A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// B6.1.14 VMRS
		// vmrs<c> <Rt>,<spec_reg>
		// 1 1 1 0 1 1 1 0 1 1 1 1 reg_1_3_0 Rt_0_15_12 1 0 1 0 (0)(0)(0) 1 (0)(0)(0)(0)
		// 111011101111xxxxxxxx1010(0)(0)(0)1(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_vmrs, "vmrs", "111011101111xxxxxxxx10100xx1xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.336 VMSR
		// vmsr<c> fpscr, <Rt>
		// 1 1 1 0 1 1 1 0 1 1 1 0 0 0 0 1 Rt_0_15_12 1 0 1 0 0 (0)(0) 1 (0)(0)(0)(0)
		// 1110111011100001xxxx10100(0)(0)1(0)(0)(0)(0)
		// NEW - Encoding T1 /A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// B6.1.15 VMSR
		// vmsr<c> <spec_reg>,<Rt>
		// 1 1 1 0 1 1 1 0 1 1 1 0 reg_1_3_0 Rt_0_15_12 1 0 1 0 (0)(0)(0) 1 (0)(0)(0)(0)
		// 111011101110xxxxxxxx1010(0)(0)(0)1(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_vmsr, "vmsr", "111011101110xxxxxxxx10100xx1xxxx"),
		// NEW - Encoding T1 / A1 (op) Advanced SIMD
		// A8.6.337 VMUL, VMULL (integer and polynomial)
		// vmul<c>.<dt> <Qd>, <Qn>, <Qm>	vmul<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1 1 1 op_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11110xxxxxxxxxxx1001xxx1xxxx
		new OpcodeARM(Index.thumb2_vmul_1, "vmul", "111x11110xxxxxxxxxxx1001xxx1xxxx"),
		// NEW - Encoding T2 / A2 (U) Advanced SIMD
		// A8.6.337 VMUL, VMULL (integer and polynomial)
		// vmull<c>.<dt> <Qd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 1 op_0_9_9 0 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx11x0x0x0xxxx
		new OpcodeARM(Index.thumb2_vmull, "vmull", "111x11111xxxxxxxxxxx11x0x0x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.338 VMUL (floating-point)
		// vmul<c>.f32 <Qd>, <Qn>, <Qm>	vmul<c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 1 1 1 1 1 0 D_1_6_6 0 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111111110x0xxxxxxxxx1101xxx1xxxx
		new OpcodeARM(Index.thumb2_vmul__f32, "vmul", "111111110x0xxxxxxxxx1101xxx1xxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.338 VMUL (floating-point)
		// vmul<c>.f64 <Dd>, <Dn>, <Dm>	vmul<c>.f32 <Sd>, <Sn>, <Sm>
		// 1 1 1 0 1 1 1 0 0 D_1_6_6 1 0 Vn_1_3_0 Vd_0_15_12 1 0 1 sz_0_8_8 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111011100x10xxxxxxxx101xx0x0xxxx
		new OpcodeARM(Index.thumb2_vmul__fp_2, "vmul", "111011100x10xxxxxxxx101xx0x0xxxx"),
		// NEW - Encoding T2 / A2 Advanced SIMD
		// A8.6.360 VQDMULL
		// vqdmull<c>.<dt> <Qd>,<Dn>,<Dm[x]>
		// 1 1 1 0 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 1 1 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111011111xxxxxxxxxxx1011x1x0xxxx
		// must precede thumb2_vmul__scalar
		new OpcodeARM(Index.thumb2_vqdmull__scalar, "vqdmul", "111011111xxxxxxxxxxx1011x1x0xxxx"),
		// NEW - Encoding T1 / A1 (Q) Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.339 VMUL, VMULL (by scalar)
		// vmul<c>.<dt> <Qd>, <Qn>, <Dm[x]>	vmul<c>.<dt> <Dd>, <Dn>, <Dm[x]>
		// 1 1 1 Q_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 0 F_0_8_8 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx100xx1x0xxxx
		// NEW - Encoding T2 / A2 (U) Advanced SIMD
		// A8.6.339 VMUL, VMULL (by scalar)
		// vmull<c>.<dt> <Qd>, <Dn>, <Dm[x]>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 1 0 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx1010x1x0xxxx
		// must follow thumb2_vqdmull__scalar
		new OpcodeARM(Index.thumb2_vmul__scalar,  "vmul",  "111x11111xxxxxxxxxxx10xxx1x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.341 VMVN (register)
		// vmvn<c> <Qd>, <Qm>	vmvn<c> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 0 Vd_0_15_12 0 1 0 1 1 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx00xxxx01011xx0xxxx
		new OpcodeARM(Index.thumb2_vmvn, "vmvn", "111111111x11xx00xxxx01011xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.342 VNEG
		// vneg<c>.<dt> <Qd>, <Qm>	vneg<c>.<dt> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 1 Vd_0_15_12 0 F_0_10_10 1 1 1 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx01xxxx0x111xx0xxxx
		new OpcodeARM(Index.thumb2_vneg, "vneg", "111111111x11xx01xxxx0x111xx0xxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.342 VNEG
		// vneg<c>.f64 <Dd>, <Dm>	vneg<c>.f32 <Sd>, <Sm>
		// 1 1 1 0 1 1 1 0 1 D_1_6_6 1 1 0 0 0 1 Vd_0_15_12 1 0 1 sz_0_8_8 0 1 M_0_5_5 0 Vm_0_3_0
		// 111011101x110001xxxx101x01x0xxxx
		new OpcodeARM(Index.thumb2_vneg__f, "vneg", "111011101x110001xxxx101x01x0xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.343 VNMLA, VNMLS, VNMUL
		// vnmla<c>.f64 <Dd>, <Dn>, <Dm>	vnmla<c>.f32 <Sd>, <Sn>, <Sm>	vnmls<c>.f64 <Dd>, <Dn>, <Dm>	vnmls<c>.f32 <Sd>, <Sn>, <Sm>
		// 1 1 1 0 1 1 1 0 0 D_1_6_6 0 1 Vn_1_3_0 Vd_0_15_12 1 0 1 sz_0_8_8 N_0_7_7 op_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111011100x01xxxxxxxx101xxxx0xxxx
		new OpcodeARM(Index.thumb2_vnml, "vnml", "111011100x01xxxxxxxx101xxxx0xxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.343 VNMLA, VNMLS, VNMUL
		// vnmul<c>.f64 <Dd>, <Dn>, <Dm>	vnmul<c>.f32 <Sd>, <Sn>, <Sm>
		// 1 1 1 0 1 1 1 0 0 D_1_6_6 1 0 Vn_1_3_0 Vd_0_15_12 1 0 1 sz_0_8_8 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111011100x10xxxxxxxx101xx1x0xxxx
		new OpcodeARM(Index.thumb2_vnmul, "vnmul", "111011100x10xxxxxxxx101xx1x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.345 VORN (register)
		// vorn<c> <Qd>, <Qn>, <Qm>	vorn<c> <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 1 1 Vn_1_3_0 Vd_0_15_12 0 0 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111011110x11xxxxxxxx0001xxx1xxxx
		new OpcodeARM(Index.thumb2_vorn, "vorn", "111011110x11xxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.348 VPADAL
		// vpadal<c>.<dt> <Qd>, <Qm>	vpadal<c>.<dt> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 0 Vd_0_15_12 0 1 1 0 op_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx00xxxx0110xxx0xxxx
		new OpcodeARM(Index.thumb2_vpadal, "vpadal", "111111111x11xx00xxxx0110xxx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.349 VPADD (integer)
		// vpadd<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 1 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111011110xxxxxxxxxxx1011xxx1xxxx
		new OpcodeARM(Index.thumb2_vpadd__int, "vpadd", "111011110xxxxxxxxxxx1011xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.350 VPADD (floating-point)
		// vpadd<c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 1 1 1 1 1 0 D_1_6_6 0 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111110x0xxxxxxxxx1101xxx0xxxx
		new OpcodeARM(Index.thumb2_vpadd__f32, "vpadd", "111111110x0xxxxxxxxx1101xxx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.351 VPADDL
		// vpaddl<c>.<dt> <Qd>, <Qm>	vpaddl<c>.<dt> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 0 Vd_0_15_12 0 0 1 0 op_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx00xxxx0010xxx0xxxx
		new OpcodeARM(Index.thumb2_vpaddl, "vpaddl", "111111111x11xx00xxxx0010xxx0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.352 VPMAX, VPMIN (integer)
		// vp<op><c>.<dt> <Dd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 1 0 N_0_7_7 Q_0_6_6 M_0_5_5 op_0_4_4 Vm_0_3_0
		// 111x11110xxxxxxxxxxx1010xxxxxxxx
		new OpcodeARM(Index.thumb2_vpmax_vpmin__int, "vp", "111x11110xxxxxxxxxxx1010xxxxxxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.353 VPMAX, VPMIN (floating-point)
		// vp<op><c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 1 1 1 1 1 0 D_1_6_6 op_1_5_5 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 1 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111110xxxxxxxxxxx1111xxx0xxxx
		new OpcodeARM(Index.thumb2_vpmax_vpmin__fp, "vp", "111111110xxxxxxxxxxx1111xxx0xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.355 VPUSH
		// vpush<c> <list> <list> is consecutive 64-bit registers
		// 1 1 1 0 1 1 0 1 0 D_1_6_6 1 0 1 1 0 1 Vd_0_15_12 1 0 1 1 imm8_0_7_0
		// 111011010x101101xxxx1011xxxxxxxx
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3
		// A8.6.355 VPUSH
		// vpush<c> <list> <list> is consecutive 32-bit registers
		// 1 1 1 0 1 1 0 1 0 D_1_6_6 1 0 1 1 0 1 Vd_0_15_12 1 0 1 0 imm8_0_7_0
		// 111011010x101101xxxx1010xxxxxxxx
		// must precede thumb2_vstm_1 in search table
//		new OpcodeARM(Index.thumb2_vpush_1, "vpush", "111011010x101101xxxx1011xxxxxxxx"),
//		new OpcodeARM(Index.thumb2_vpush_2, "vpush", "111011010x101101xxxx1010xxxxxxxx"),
		new OpcodeARM(Index.thumb2_vpush,   "vpush", "111011010x101101xxxx101xxxxxxxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.356 VQABS
		// vqabs<c>.<dt> <Qd>,<Qm>	vqabs<c>.<dt> <Dd>,<Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 0 Vd_0_15_12 0 1 1 1 0 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx00xxxx01110xx0xxxx
		// must precede thumb2_vstm_2 in search table
		// must precede thumb2_vabdl
		new OpcodeARM(Index.thumb2_vqabs, "vqabs", "111111111x11xx00xxxx01110xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.362 VQNEG
		// vqneg<c>.<dt> <Qd>,<Qm>	vqneg<c>.<dt> <Dd>,<Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 0 Vd_0_15_12 0 1 1 1 1 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx00xxxx01111xx0xxxx
		// must precede thumb2_vabdl
		new OpcodeARM(Index.thumb2_vqneg, "vqneg", "111111111x11xx00xxxx01111xx0xxxx"),
		// NEW - Encoding T2 / A2 (U) Advanced SIMD
		// A8.6.267 VABD, VABDL (integer)
		// vabdl<c>.<dt> <Qd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 1 1 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx0111x0x0xxxx
		// must follow thumb2_vabs
		// must follow thumb2_vqabs
		// must follow thumb2_vqneg
		new OpcodeARM(Index.thumb2_vabdl, "vabdl", "111x11111xxxxxxxxxxx0111x0x0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.357 VQADD
		// vqadd<c>.<dt> <Qd>,<Qn>,<Qm>	vqadd<c>.<dt> <Dd>,<Dn>,<Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 0 0 0 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11110xxxxxxxxxxx0000xxx1xxxx
		new OpcodeARM(Index.thumb2_vqadd, "vqadd", "111x11110xxxxxxxxxxx0000xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.358 VQDMLAL, VQDMLSL
		// vqd<op><c>.<dt> <Qd>,<Dn>,<Dm>
		// 1 1 1 0 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 op_0_9_9 1 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111011111xxxxxxxxxxx10x1x0x0xxxx
		new OpcodeARM(Index.thumb2_vqdml__vec, "vqdml", "111011111xxxxxxxxxxx10x1x0x0xxxx"),
		// NEW - Encoding T2 / A2 Advanced SIMD
		// A8.6.358 VQDMLAL, VQDMLSL
		// vqd<op><c>.<dt> <Qd>,<Dn>,<Dm[x]>
		// 1 1 1 0 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 op_0_10_10 1 1 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111011111xxxxxxxxxxx0x11x1x0xxxx
		new OpcodeARM(Index.thumb2_vqdml__scalar, "vqdml", "111011111xxxxxxxxxxx0x11x1x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.359 VQDMULH
		// vqdmulh<c>.<dt> <Qd>,<Qn>,<Qm>	vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 1 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111011110xxxxxxxxxxx1011xxx0xxxx
		new OpcodeARM(Index.thumb2_vqdmulh__vec, "vqdmulh", "111011110xxxxxxxxxxx1011xxx0xxxx"),
		// NEW - Encoding T2 / A2 (Q) Advanced SIMD
		// A8.6.359 VQDMULH
		// vqdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>	vqdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>
		// 1 1 1 Q_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 1 0 0 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx1100x1x0xxxx
		new OpcodeARM(Index.thumb2_vqdmulh__scalar, "vqdmulh", "111x11111xxxxxxxxxxx1100x1x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.360 VQDMULL
		// vqdmull<c>.<dt> <Qd>,<Dn>,<Dm>
		// 1 1 1 0 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 1 0 1 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111011111xxxxxxxxxxx1101x0x0xxxx
		new OpcodeARM(Index.thumb2_vqdmull__vec, "vqdmull", "111011111xxxxxxxxxxx1101x0x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.361 VQMOVN, VQMOVUN
		// vqmov{u}n<c>.<type><size> <Dd>, <Qm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 0 Vd_0_15_12 0 0 1 0 op_0_7_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx10xxxx0010xxx0xxxx
		// must follow thumb2_vmovn in search table
		new OpcodeARM(Index.thumb2_vqmov, "vqmov", "111111111x11xx10xxxx0010xxx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.363 VQRDMULH
		// vqrdmulh<c>.<dt> <Qd>,<Qn>,<Qm>	vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm>
		// 1 1 1 1 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 1 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111110xxxxxxxxxxx1011xxx0xxxx
		new OpcodeARM(Index.thumb2_vqrdmulh__vec, "vqrdmulh", "111111110xxxxxxxxxxx1011xxx0xxxx"),
		// NEW - Encoding T2 / A2 (Q) Advanced SIMD
		// A8.6.363 VQRDMULH
		// vqrdmulh<c>.<dt> <Qd>,<Qn>,<Dm[x]>	vqrdmulh<c>.<dt> <Dd>,<Dn>,<Dm[x]>
		// 1 1 1 Q_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 1 0 1 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx1101x1x0xxxx
		new OpcodeARM(Index.thumb2_vqrdmulh__scalar, "vqrdmulh", "111x11111xxxxxxxxxxx1101x1x0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.364 VQRSHL
		// vqrshl<c>.<type><size> <Qd>,<Qm>,<Qn>	vqrshl<c>.<type><size> <Dd>,<Dm>,<Dn>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11110xxxxxxxxxxx0101xxx1xxxx
		new OpcodeARM(Index.thumb2_vqrshl, "vqrshl", "111x11110xxxxxxxxxxx0101xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.377 VRSHRN
		// vrshrn<c>.i<size> <Dd>, <Qm>, #<imm>
		// 1 1 1 0 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 1 0 0 0 0 1 M_0_5_5 1 Vm_0_3_0
		// 111011111xxxxxxxxxxx100001x1xxxx
		// must precede thumb2_vqrshr in search table
		new OpcodeARM(Index.thumb2_vrshrn, "vrshrn", "111011111xxxxxxxxxxx100001x1xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.365 VQRSHRN, VQRSHRUN
		// vqrshr{u}n<c>.<type><size> <Dd>,<Qm>,#<imm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 1 0 0 op_0_8_8 0 1 M_0_5_5 1 Vm_0_3_0
		// 111x11111xxxxxxxxxxx100x01x1xxxx
		// must follow thumb2_vrshrn in search table
		new OpcodeARM(Index.thumb2_vqrshr, "vqrshr", "111x11111xxxxxxxxxxx100x01x1xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.366 VQSHL (register)
		// vqshl<c>.<type><size> <Qd>,<Qm>,<Qn>	vqshl<c>.<type><size> <Dd>,<Dm>,<Dn>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 0 0 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11110xxxxxxxxxxx0100xxx1xxxx
		new OpcodeARM(Index.thumb2_vqshl__reg, "vqshl", "111x11110xxxxxxxxxxx0100xxx1xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.367 VQSHL, VQSHLU (immediate)
		// vqshl{u}<c>.<type><size> <Qd>,<Qm>,#<imm>	vqshl{u}<c>.<type><size> <Dd>,<Dm>,#<imm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 0 1 1 op_0_8_8 L_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11111xxxxxxxxxxx011xxxx1xxxx
		new OpcodeARM(Index.thumb2_vqshl__imm, "vqshl", "111x11111xxxxxxxxxxx011xxxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.386 VSHRN
		// vshrn<c>.i<size> <Dd>, <Qm>, #<imm>
		// 1 1 1 0 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 1 0 0 0 0 0 M_0_5_5 1 Vm_0_3_0
		// 111011111xxxxxxxxxxx100000x1xxxx
		// must precede thumb2_vqshr in search table
		new OpcodeARM(Index.thumb2_vshrn, "vshrn", "111011111xxxxxxxxxxx100000x1xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.368 VQSHRN, VQSHRUN
		// vqshr{u}n<c>.<type><size> <Dd>,<Qm>,#<imm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 1 0 0 op_0_8_8 0 0 M_0_5_5 1 Vm_0_3_0
		// 111x11111xxxxxxxxxxx100x00x1xxxx
		// must follow thumb2_vshrn in search table
		new OpcodeARM(Index.thumb2_vqshr, "vqshr", "111x11111xxxxxxxxxxx100x00x1xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.369 VQSUB
		// vqsub<c>.<type><size> <Qd>, <Qn>, <Qm>	vqsub<c>.<type><size> <Dd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 0 1 0 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11110xxxxxxxxxxx0010xxx1xxxx
		new OpcodeARM(Index.thumb2_vqsub, "vqsub", "111x11110xxxxxxxxxxx0010xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.371 VRECPE
		// vrecpe<c>.<dt> <Qd>, <Qm>	vrecpe<c>.<dt> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 1 Vd_0_15_12 0 1 0 F_0_8_8 0 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx11xxxx010x0xx0xxxx
		// must precede thumb2_vraddhn
		new OpcodeARM(Index.thumb2_vrecpe, "vrecpe", "111111111x11xx11xxxx010x0xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.378 VRSQRTE
		// vrsqrte<c>.<dt> <Qd>, <Qm>	vrsqrte<c>.<dt> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 1 Vd_0_15_12 0 1 0 F_0_8_8 1 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx11xxxx010x1xx0xxxx
		// must precede thumb2_vabal
		// must precede thumb2_vraddhn
		new OpcodeARM(Index.thumb2_vrsqrte, "vrsqrte", "111111111x11xx11xxxx010x1xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.370 VRADDHN
		// vraddhn<c>.<dt> <Dd>, <Qn>, <Qm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 0 0 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111111111xxxxxxxxxxx0100x0x0xxxx
		// must follow thumb2_vrecpe
		// must follow thumb2_vrsqrte
		new OpcodeARM(Index.thumb2_vraddhn, "vraddhn", "111111111xxxxxxxxxxx0100x0x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.372 VRECPS
		// vrecps<c>.f32 <Qd>, <Qn>, <Qm>	vrecps<c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 0 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 1 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111011110x0xxxxxxxxx1111xxx1xxxx
		new OpcodeARM(Index.thumb2_vrecps, "vrecps", "111011110x0xxxxxxxxx1111xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.373 VREV16, VREV32, VREV64
		// vrev<n><c>.<size> <Qd>, <Qm>	vrev<n><c>.<size> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 0 0 Vd_0_15_12 0 0 0 op_0_8_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx00xxxx000xxxx0xxxx
		new OpcodeARM(Index.thumb2_vrev, "vrev", "111111111x11xx00xxxx000xxxx0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.374 VRHADD
		// vrhadd<c> <Qd>, <Qn>, <Qm>	vrhadd<c> <Dd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 0 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111x11110xxxxxxxxxxx0001xxx0xxxx
		new OpcodeARM(Index.thumb2_vrhadd, "vrhadd", "111x11110xxxxxxxxxxx0001xxx0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.375 VRSHL
		// vrshl<c>.<type><size> <Qd>, <Qm>, <Qn>	vrshl<c>.<type><size> <Dd>, <Dm>, <Dn>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111x11110xxxxxxxxxxx0101xxx0xxxx
		new OpcodeARM(Index.thumb2_vrshl, "vrshl", "111x11110xxxxxxxxxxx0101xxx0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.376 VRSHR
		// vrshr<c>.<type><size> <Qd>, <Qm>, #<imm>	vrshr<c>.<type><size> <Dd>, <Dm>, #<imm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 0 0 1 0 L_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11111xxxxxxxxxxx0010xxx1xxxx
		new OpcodeARM(Index.thumb2_vrshr, "vrshr", "111x11111xxxxxxxxxxx0010xxx1xxxx"),
		// NEW - Encoding T2 / A2 (U) Advanced SIMD
		// A8.6.266 VABA, VABAL
		// vabal<c>.<dt> <Qd>, <Dn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 0 1 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx0101x0x0xxxx
		// must follow thumb2_vceq__imm0
		// must follow thumb2_vrsqrte
		new OpcodeARM(Index.thumb2_vabal, "vabal", "111x11111xxxxxxxxxxx0101x0x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.379 VRSQRTS
		// vrsqrts<c>.f32 <Qd>, <Qn>, <Qm>	vrsqrts<c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 1 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 1 1 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111011110x1xxxxxxxxx1111xxx1xxxx
		new OpcodeARM(Index.thumb2_vrsqrts, "vrsqrts", "111011110x1xxxxxxxxx1111xxx1xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.380 VRSRA
		// vrsra<c>.<type><size> <Qd>, <Qm>, #<imm>	vrsra<c>.<type><size> <Dd>, <Dm>, #<imm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 0 0 1 1 L_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11111xxxxxxxxxxx0011xxx1xxxx
		new OpcodeARM(Index.thumb2_vrsra, "vrsra", "111x11111xxxxxxxxxxx0011xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.381 VRSUBHN
		// vrsubhn<c>.<dt> <Dd>, <Qn>, <Qm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 1 0 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111111111xxxxxxxxxxx0110x0x0xxxx
		new OpcodeARM(Index.thumb2_vrsubhn, "vrsubhn", "111111111xxxxxxxxxxx0110x0x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.382 VSHL (immediate)
		// vshl<c>.i<size> <Qd>, <Qm>, #<imm>	vshl<c>.i<size> <Dd>, <Dm>, #<imm>
		// 1 1 1 0 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 0 1 0 1 L_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111011111xxxxxxxxxxx0101xxx1xxxx
		new OpcodeARM(Index.thumb2_vshl__imm, "vshl.i", "111011111xxxxxxxxxxx0101xxx1xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.383 VSHL (register)
		// vshl<c>.i<size> <Qd>, <Qm>, <Qn>	vshl<c>.i<size> <Dd>, <Dm>, <Dn>
		// 1 1 1 U_1_12_12 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 0 0 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111x11110xxxxxxxxxxx0100xxx0xxxx
		new OpcodeARM(Index.thumb2_vshl__reg, "vshl", "111x11110xxxxxxxxxxx0100xxx0xxxx"),
		// NEW - Encoding T2 / A2 Advanced SIMD
		// A8.6.384 VSHLL
		// vshll<c>.<type><size> <Qd>, <Dm>, #<imm> (<imm> == <size>)
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 0 Vd_0_15_12 0 0 1 1 0 0 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx10xxxx001100x0xxxx
		new OpcodeARM(Index.thumb2_vshll__max, "vshll", "111111111x11xx10xxxx001100x0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.385 VSHR
		// vshr<c>.<type><size> <Qd>, <Qm>, #<imm>	vshr<c>.<type><size> <Dd>, <Dm>, #<imm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 0 0 0 0 L_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11111xxxxxxxxxxx0000xxx1xxxx
		new OpcodeARM(Index.thumb2_vshr, "vshr", "111x11111xxxxxxxxxxx0000xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.387 VSLI
		// vsli<c>.<size> <Qd>, <Qm>, #<imm>	vsli<c>.<size> <Dd>, <Dm>, #<imm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 0 1 0 1 L_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111111111xxxxxxxxxxx0101xxx1xxxx
		new OpcodeARM(Index.thumb2_vsli, "vsli.", "111111111xxxxxxxxxxx0101xxx1xxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.388 VSQRT
		// vsqrt<c>.f64 <Dd>, <Dm>	vsqrt<c>.f32 <Sd>, <Sm>
		// 1 1 1 0 1 1 1 0 1 D_1_6_6 1 1 0 0 0 1 Vd_0_15_12 1 0 1 sz_0_8_8 1 1 M_0_5_5 0 Vm_0_3_0
		// 111011101x110001xxxx101x11x0xxxx
		new OpcodeARM(Index.thumb2_vsqrt, "vsqrt", "111011101x110001xxxx101x11x0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.389 VSRA
		// vsra<c>.<type><size> <Qd>, <Qm>, #<imm>	vsra<c>.<type><size> <Dd>, <Dm>, #<imm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 0 0 0 1 L_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111x11111xxxxxxxxxxx0001xxx1xxxx
		new OpcodeARM(Index.thumb2_vsra, "vsra", "111x11111xxxxxxxxxxx0001xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.390 VSRI
		// vsri<c>.<size> <Qd>, <Qm>, #<imm>	vsri<c>.<size> <Dd>, <Dm>, #<imm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 imm6_1_5_0 Vd_0_15_12 0 1 0 0 L_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111111111xxxxxxxxxxx0100xxx1xxxx
		new OpcodeARM(Index.thumb2_vsri, "vsri.", "111111111xxxxxxxxxxx0100xxx1xxxx"),

		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.400 VSTR
		// vstr<c> <Dd>, [<Rn>{, #+/-<imm>}]
		// 1 1 1 0 1 1 0 1 U_1_7_7 D_1_6_6 0 0 Rn_1_3_0 Vd_0_15_12 1 0 1 1 imm8_0_7_0
		// 11101101xx00xxxxxxxx1011xxxxxxxx
		// must precede thumb2_vstm_1 in search table
		new OpcodeARM(Index.thumb2_vstr__64, "vstr", "11101101xx00xxxxxxxx1011xxxxxxxx"),
		// NEW - Encoding T1 / A1 (cond) VFPv2, VFPv3, Advanced SIMD
		// A8.6.399 VSTM
		// vstm{mode}<c> <Rn>{!}, <list> <list> is consecutive 64-bit registers
		// 1 1 1 0 1 1 0 P_1_8_8 U_1_7_7 D_1_6_6 W_1_5_5 0 Rn_1_3_0 Vd_0_15_12 1 0 1 1 imm8_0_7_0
		// 1110110xxxx0xxxxxxxx1011xxxxxxxx
		// must follow thumb2_vstr_1 in search table
		new OpcodeARM(Index.thumb2_vstm__64, "vstm", "1110110xxxx0xxxxxxxx1011xxxxxxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3
		// A8.6.400 VSTR
		// vstr<c> <Sd>, [<Rn>{, #+/-<imm>}]
		// 1 1 1 0 1 1 0 1 U_1_7_7 D_1_6_6 0 0 Rn_1_3_0 Vd_0_15_12 1 0 1 0 imm8_0_7_0
		// 11101101xx00xxxxxxxx1010xxxxxxxx
		// must precede thumb2_vstm_2 in search table
		new OpcodeARM(Index.thumb2_vstr__32, "vstr", "11101101xx00xxxxxxxx1010xxxxxxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3
		// A8.6.399 VSTM
		// vstm{mode}<c> <Rn>{!}, <list> <list> is consecutive 32-bit registers
		// 1 1 1 0 1 1 0 P_1_8_8 U_1_7_7 D_1_6_6 W_1_5_5 0 Rn_1_3_0 Vd_0_15_12 1 0 1 0 imm8_0_7_0
		// 1110110xxxx0xxxxxxxx1010xxxxxxxx
		// must follow thumb2_vstr_1 in search table
		new OpcodeARM(Index.thumb2_vstm__32, "vstm", "1110110xxxx0xxxxxxxx1010xxxxxxxx"),

		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.401 VSUB (integer)
		// vsub<c>.<dt> <Qd>, <Qn>, <Qm>	vsub<c>.<dt> <Dd>, <Dn>, <Dm>
		// 1 1 1 1 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 0 0 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111110xxxxxxxxxxx1000xxx0xxxx
		new OpcodeARM(Index.thumb2_vsub__int, "vsub", "111111110xxxxxxxxxxx1000xxx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD (UNDEFINED in integer-only variant)
		// A8.6.402 VSUB (floating-point)
		// vsub<c>.f32 <Qd>, <Qn>, <Qm>	vsub<c>.f32 <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 1 sz_1_4_4 Vn_1_3_0 Vd_0_15_12 1 1 0 1 N_0_7_7 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111011110x1xxxxxxxxx1101xxx0xxxx
		new OpcodeARM(Index.thumb2_vsub__f32, "vsub", "111011110x1xxxxxxxxx1101xxx0xxxx"),
		// NEW - Encoding T2 / A2 (cond) VFPv2, VFPv3 (sz = 1 UNDEFINED in single-precision only variants)
		// A8.6.402 VSUB (floating-point)
		// vsub<c>.f64 <Dd>, <Dn>, <Dm>	vsub<c>.f32 <Sd>, <Sn>, <Sm>
		// 1 1 1 0 1 1 1 0 0 D_1_6_6 1 1 Vn_1_3_0 Vd_0_15_12 1 0 1 sz_0_8_8 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111011100x11xxxxxxxx101xx1x0xxxx
		new OpcodeARM(Index.thumb2_vsub__fp_f, "vsub", "111011100x11xxxxxxxx101xx1x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.403 VSUBHN
		// vsubhn<c>.<dt> <Dd>, <Qn>, <Qm>
		// 1 1 1 0 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 1 1 0 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111011111xxxxxxxxxxx0110x0x0xxxx
		new OpcodeARM(Index.thumb2_vsubhn, "vsubhn", "111011111xxxxxxxxxxx0110x0x0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.404 VSUBL, VSUBW
		// vsubl<c>.<dt> <Qd>, <Dn>, <Dm>	vsubw<c>.<dt> {<Qd>,} <Qn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 0 1 op_0_8_8 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx001xx0x0xxxx
		new OpcodeARM(Index.thumb2_vsubl_vsubw, "vsub", "111x11111xxxxxxxxxxx001xx0x0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.405 VSWP
		// vswp<c> <Qd>, <Qm>	vswp<c> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 0 Vd_0_15_12 0 0 0 0 0 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx10xxxx00000xx0xxxx
		new OpcodeARM(Index.thumb2_vswp, "vswp", "111111111x11xx10xxxx00000xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.407 VTRN
		// vtrn<c>.<size> <Qd>, <Qm>	vtrn<c>.<size> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 0 Vd_0_15_12 0 0 0 0 1 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx10xxxx00001xx0xxxx
		// must precede thumb2_vaddl_vaddw
		// must precede thumb2_vml__scalar
		new OpcodeARM(Index.thumb2_vtrn, "vtrn", "111111111x11xx10xxxx00001xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.408 VTST
		// vtst<c>.<size> <Qd>, <Qn>, <Qm>	vtst<c>.<size> <Dd>, <Dn>, <Dm>
		// 1 1 1 0 1 1 1 1 0 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 1 0 0 0 N_0_7_7 Q_0_6_6 M_0_5_5 1 Vm_0_3_0
		// 111011110xxxxxxxxxxx1000xxx1xxxx
		new OpcodeARM(Index.thumb2_vtst, "vtst", "111011110xxxxxxxxxxx1000xxx1xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.409 VUZP
		// vuzp<c>.<size> <Qd>, <Qm>	vuzp<c>.<size> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 0 Vd_0_15_12 0 0 0 1 0 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx10xxxx00010xx0xxxx
		// must precede thumb2_vaddl_vaddw
		// must precede thumb2_vml__scalar
		new OpcodeARM(Index.thumb2_vuzp, "vuzp", "111111111x11xx10xxxx00010xx0xxxx"),
		// NEW - Encoding T1 / A1 Advanced SIMD
		// A8.6.410 VZIP
		// vzip<c>.<size> <Qd>, <Qm>	vzip<c>.<size> <Dd>, <Dm>
		// 1 1 1 1 1 1 1 1 1 D_1_6_6 1 1 size_1_3_2 1 0 Vd_0_15_12 0 0 0 1 1 Q_0_6_6 M_0_5_5 0 Vm_0_3_0
		// 111111111x11xx10xxxx00011xx0xxxx
		// must precede thumb2_vaddl_vaddw
		// must precede thumb2_vml__scalar
		new OpcodeARM(Index.thumb2_vzip, "vzip", "111111111x11xx10xxxx00011xx0xxxx"),
		// NEW - Encoding T1 / A1 (Q) Advanced SIMD (F = 1 UNDEFINED in integer-only variants)
		// A8.6.325 VMLA, VMLAL, VMLS, VMLSL (by scalar)
		// v<op><c>.<dt> <Qd>, <Qn>, <Dm[x]>	v<op><c>.<dt> <Dd>, <Dn>, <Dm[x]>
		// 1 1 1 Q_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 op_0_10_10 0 F_0_8_8 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// v<op>l<c>.<dt> <Qd>, <Dn>, <Dm[x]>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 op_0_10_10 1 0 N_0_7_7 1 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx0xxxx1x0xxxx
		// must follow thumb2_vtrn
		// must follow thumb2_vuzp
		// must follow thumb2_vzip
		new OpcodeARM(Index.thumb2_vml__scalar, "vml", "111x11111xxxxxxxxxxx0xxxx1x0xxxx"),
		// NEW - Encoding T1 / A1 (U) Advanced SIMD
		// A8.6.274 VADDL, VADDW
		// vaddl<c>.<dt> <Qd>, <Dn>, <Dm>	vaddw<c>.<dt> <Qd>, <Qn>, <Dm>
		// 1 1 1 U_1_12_12 1 1 1 1 1 D_1_6_6 size_1_5_4 Vn_1_3_0 Vd_0_15_12 0 0 0 op_0_8_8 N_0_7_7 0 M_0_5_5 0 Vm_0_3_0
		// 111x11111xxxxxxxxxxx000xx0x0xxxx
		// must follow thumb2_vceq__imm0
		// must follow thumb2_vcge__imm0
		// must follow thumb2_vcgt__imm0
		// must follow thumb2_vcle
		// must follow thumb2_vtrn
		// must follow thumb2_vuzp
		// must follow thumb2_vzip
		new OpcodeARM(Index.thumb2_vaddl_vaddw, "vadd", "111x11111xxxxxxxxxxx000xx0x0xxxx"),


		// Coprocessor Data Processing instructions
		// must follow VFP data processing instructions in search table
		// (VMLA, VMLS, VNMLA, VNMLS, VNMUL, VMUL, VDIV, VMOV, VABS, VNEG
		//  VSQRT, VCVTB, VCVIT, VCMP, VCMPE, VCVT, VCVTR)

		// NEW - Encoding T1 / A1 (cond) ARMv6T2, ARMv7
		// A8.6.28 CDP, CDP2
		// cdp<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>
		// 1 1 1 0 1 1 1 0 opc1_1_7_4 CRn_1_3_0 CRd_0_15_12 coproc_0_11_8 opc2_0_7_5 0 CRm_0_3_0
		// 11101110xxxxxxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.thumb2_cdp, "cdp", "11101110xxxxxxxxxxxxxxxxxxx0xxxx"),
		// NEW - Encoding T2 /A2 ARMv6T2, ARMv7
		// A8.6.28 CDP, CDP2
		// cdp2<c> <coproc>,<opc1>,<CRd>,<CRn>,<CRm>,<opc2>
		// 1 1 1 1 1 1 1 0 opc1_1_7_4 CRn_1_3_0 CRd_0_15_12 coproc_0_11_8 opc2_0_7_5 0 CRm_0_3_0
		// 11111110xxxxxxxxxxxxxxxxxxx0xxxx
		new OpcodeARM(Index.thumb2_cdp2, "cdp2", "11111110xxxxxxxxxxxxxxxxxxx0xxxx"),

		// must follow VMOV 64-bit transfer between ARM core and extension registers

		// NEW - Encoding T1 / A1 (cond) ARMv6T2, ARMv7
		// A8.6.92 MCR, MCR2
		// mcr<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// 1 1 1 0 1 1 1 0 opc1_1_7_5 0 CRn_1_3_0 Rt_0_15_12 coproc_0_11_8 opc2_0_7_5 1 CRm_0_3_0
		// 11101110xxx0xxxxxxxxxxxxxxx1xxxx
		// NEW - Encoding T2 / A2 ARMv6T2, ARMv7
		// A8.6.92 MCR, MCR2
		// mcr2<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// 1 1 1 1 1 1 1 0 opc1_1_7_5 0 CRn_1_3_0 Rt_0_15_12 coproc_0_11_8 opc2_0_7_5 1 CRm_0_3_0
		// 11111110xxx0xxxxxxxxxxxxxxx1xxxx
		new OpcodeARM(Index.thumb2_mcr, "mcr", "111x1110xxx0xxxxxxxxxxxxxxx1xxxx"),

		// NEW - Encoding T1 / A1 (cond) ARMv6T2, ARMv7
		// A8.6.93 MCRR, MCRR2
		// mcrr<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
		// 1 1 1 0 1 1 0 0 0 1 0 0 Rt2_1_3_0 Rt_0_15_12 coproc_0_11_8 opc1_0_7_4 CRm_0_3_0
		// 111011000100xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding T2 / A2 ARMv6T2, ARMv7
		// A8.6.93 MCRR, MCRR2
		// mcrr2<c> <coproc>,<opc1>,<Rt>,<Rt2>,<CRm>
		// 1 1 1 1 1 1 0 0 0 1 0 0 Rt2_1_3_0 Rt_0_15_12 coproc_0_11_8 opc1_0_7_4 CRm_0_3_0
		// 111111000100xxxxxxxxxxxxxxxxxxxx
		// must precede thumb2_stc
		new OpcodeARM(Index.thumb2_mcrr, "mcrr", "111x11000100xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 / A1 (cond) ARMv6T2, ARMv7
		// A8.6.188 STC, STC2
		// stc{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	stc{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>	stc{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// 1 1 1 0 1 1 0 P_1_8_8 U_1_7_7 D_1_6_6 W_1_5_5 0 Rn_1_3_0 CRd_0_15_12 coproc_0_11_8 imm8_0_7_0
		// 1110110xxxx0xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding T2 / A2 ARMv6T2, ARMv7
		// A8.6.188 STC, STC2
		// stc2{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	stc2{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>	stc2{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// 1 1 1 1 1 1 0 P_1_8_8 U_1_7_7 D_1_6_6 W_1_5_5 0 Rn_1_3_0 CRd_0_15_12 coproc_0_11_8 imm8_0_7_0
		// 1111110xxxx0xxxxxxxxxxxxxxxxxxxx
		// must follow thumb2_mcrr
		// must follow thumb2_mcrr2
		// must follow thumb2_vstm__32
		new OpcodeARM(Index.thumb2_stc, "stc", "111x110xxxx0xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 / A1 (cond) ARMv6T2, ARMv7
		// A8.6.100 MRC, MRC2
		// mrc<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// 1 1 1 0 1 1 1 0 opc1_1_7_5 1 CRn_1_3_0 Rt_0_15_12 coproc_0_11_8 opc2_0_7_5 1 CRm_0_3_0
		// 11101110xxx1xxxxxxxxxxxxxxx1xxxx
		// NEW - Encoding T2 / A2 ARMv6T2, ARMv7
		// A8.6.100 MRC, MRC2
		// mrc2<c> <coproc>,<opc1>,<Rt>,<CRn>,<CRm>{,<opc2>}
		// 1 1 1 1 1 1 1 0 opc1_1_7_5 1 CRn_1_3_0 Rt_0_15_12 coproc_0_11_8 opc2_0_7_5 1 CRm_0_3_0
		// 11111110xxx1xxxxxxxxxxxxxxx1xxxx
		new OpcodeARM(Index.thumb2_mrc, "mrc", "111x1110xxx1xxxxxxxxxxxxxxx1xxxx"),

		// NEW - Encoding T1 / A1 (cond) ARMv6T2, ARMv7
		// A8.6.101 MRRC, MRRC2
		// mrrc<c> <coproc>,<opc>,<Rt>,<Rt2>,<CRm>
		// 1 1 1 0 1 1 0 0 0 1 0 1 Rt2_1_3_0 Rt_0_15_12 coproc_0_11_8 opc1_0_7_4 CRm_0_3_0
		// 111011000101xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding T2 / A2 ARMv6T2, ARMv7
		// A8.6.101 MRRC, MRRC2
		// mrrc2<c> <coproc>,<opc>,<Rt>,<Rt2>,<CRm>
		// 1 1 1 1 1 1 0 0 0 1 0 1 Rt2_1_3_0 Rt_0_15_12 coproc_0_11_8 opc1_0_7_4 CRm_0_3_0
		// 111111000101xxxxxxxxxxxxxxxxxxxx
		// must precede thumb2_ldc in search table
		// must precede thumb2_ldc2 in search table
		new OpcodeARM(Index.thumb2_mrrc, "mrrc", "111x11000101xxxxxxxxxxxxxxxxxxxx"),

		// must follow thumb_mrrc in search table
		// NEW - Encoding T1 / A1 (cond) ARMv6T2, ARMv7
		// A8.6.51 LDC, LDC2 (immediate)
		// ldc{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	ldc{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>	ldc{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// 1 1 1 0 1 1 0 P_1_8_8 U_1_7_7 D_1_6_6 W_1_5_5 1 Rn_1_3_0 CRd_0_15_12 coproc_0_11_8 imm8_0_7_0
		// 1110110xxxx1xxxxxxxxxxxxxxxxxxxx
		// NEW - Encoding T1 /A1 (cond) ARMv6T2, ARMv7
		// A8.6.52 LDC, LDC2 (literal)
		// ldc{l}<c> <coproc>,<CRd>,<label>	ldc{l}<c> <coproc>,<CRd>,[pc,#-0] Special case	ldc{l}<c> <coproc>,<CRd>,[pc],<option>
		// 1 1 1 0 1 1 0 P_1_8_8 U_1_7_7 D_1_6_6 W_1_5_5 1 1 1 1 1 CRd_0_15_12 coproc_0_11_8 imm8_0_7_0
		// 1110110xxxx11111xxxxxxxxxxxxxxxx
		// NEW - Encoding T2 / A2 ARMv6T2, ARMv7
		// A8.6.52 LDC, LDC2 (literal)
		// ldc2{l}<c> <coproc>,<CRd>,<label>	ldc2{l}<c> <coproc>,<CRd>,[pc,#-0] Special case	ldc2{l}<c> <coproc>,<CRd>,[pc],<option>
		// 1 1 1 1 1 1 0 P_1_8_8 U_1_7_7 D_1_6_6 W_1_5_5 1 1 1 1 1 CRd_0_15_12 coproc_0_11_8 imm8_0_7_0
		// 1111110xxxx11111xxxxxxxxxxxxxxxx
		// NEW - Encoding T2 / A1 ARMv6T2, ARMv7
		// A8.6.51 LDC, LDC2 (immediate)
		// ldc2{l}<c> <coproc>,<CRd>,[<Rn>,#+/-<imm>]{!}	ldc2{l}<c> <coproc>,<CRd>,[<Rn>],#+/-<imm>	ldc2{l}<c> <coproc>,<CRd>,[<Rn>],<option>
		// 1 1 1 1 1 1 0 P_1_8_8 U_1_7_7 D_1_6_6 W_1_5_5 1 Rn_1_3_0 CRd_0_15_12 coproc_0_11_8 imm8_0_7_0
		// 1111110xxxx1xxxxxxxxxxxxxxxxxxxx
		//
		// must follow thumb_mrrc2 in search table
		new OpcodeARM(Index.thumb2_ldc, "ldc",   "111x110xxxx1xxxxxxxxxxxxxxxxxxxx"),

		// NEW - Encoding T1 ARMv6T2, ARMv7
		// B6.1.8 RFE
		// rfedb<c> <Rn>{!} Outside or last in IT block
		// 1 1 1 0 1 0 0 0 0 0 W_1_5_5 1 Rn_1_3_0 (1) (1)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)
		// 1110100000x1xxxx(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// B6.1.8 RFE
		// rfe{ia}<c> <Rn>{!} Outside or last in IT block
		// 1 1 1 0 1 0 0 1 1 0 W_1_5_5 1 Rn_1_3_0 (1) (1)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)
		// 1110100000x1xxxx(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)
		// 1110100110x1xxxx(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)(0)
		new OpcodeARM(Index.thumb2_rfe, "rfe", "1110100xx0x1xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// B6.1.10 SRS
		// srsdb<c> sp{!},#<mode>
		// 1 1 1 0 1 0 0 0 0 0 W_1_5_5 0 (1)(1)(0) (1) (1) (1)(0)(0)(0)(0)(0)(0)(0)(0)(0) mode_0_4_0
		// 1110100000x0(1)(1)(0)(1)(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)(0)xxxxx
// see thumb2_srs
		// NEW - Encoding T2 ARMv6T2, ARMv7
		// B6.1.10 SRS
		// srs{ia}<c> sp{!},#<mode>
		// 1 1 1 0 1 0 0 1 1 0 W_1_5_5 0 (1) (1) (0) (1) (1) (1) (0) (0) (0) (0) (0) (0) (0) (0) (0) mode_0_4_0
		// 1110100110x0(1)(1)(0)(1)(1)(1)(0)(0)(0)(0)(0)(0)(0)(0)(0)xxxxx
		new OpcodeARM(Index.thumb2_srs, "srs", "1110100xx0x0xxxxxxxxxxxxxxxxxxxx"),
		// NEW - Encoding T1 ARMv6T2, ARMv7
		// B6.1.13 SUBS PC, LR and related instructions
		// subs<c> pc,lr,#<imm8> Outside or last in IT block
		// 1 1 1 1 0 0 1 1 1 1 0 1 (1)(1)(1) (0) 1 0 (0) 0 (1)(1)(1)(1) imm8_0_7_0
		// 111100111101(1)(1)(1)(0)10(0)0(1)(1)(1)(1)xxxxxxxx
		new OpcodeARM(Index.thumb2_subs, "subs", "111100111101xxxx10x0xxxxxxxxxxxx"),
	};

	public static final OpcodeARM thumbEE_opcode_table[] = {
		// NEW - Encoding T1 ThumbEE
		// A9.4.1 LDR (register)
		// ldr<c> <Rt>,[<Rn>,<Rm>, lsl #2]
		// 0 1 0 1 1 0 0 Rm Rn Rt
		// 0101100xxxxxxxxx
		new OpcodeARM(Index.thumbEE_ldr_1, "ldr", "0101100xxxxxxxxx"),
		// NEW - Encoding T1 ThumbEE
		// A9.4.2 LDRH (register)
		// ldrh<c> <Rt>,[<Rn>,<Rm>, lsl #1]
		// 0 1 0 1 1 0 1 Rm Rn Rt
		// 0101101xxxxxxxxx
		new OpcodeARM(Index.thumbEE_ldrh, "ldrh", "0101101xxxxxxxxx"),
		// NEW - Encoding T1 ThumbEE
		// A9.4.3 LDRSH (register)
		// ldrsh<c> <Rt>,[<Rn>,<Rm>, lsl #1]
		// 0 1 0 1 1 1 1 Rm Rn Rt
		// 0101111xxxxxxxxx
		new OpcodeARM(Index.thumbEE_ldrsh, "ldrsh", "0101111xxxxxxxxx"),
		// NEW - Encoding T1 ThumbEE
		// A9.4.4 STR (register)
		// str<c> <Rt>,[<Rn>,<Rm>, lsl #2]
		// 0 1 0 1 0 0 0 Rm Rn Rt
		// 0101000xxxxxxxxx
		new OpcodeARM(Index.thumbEE_str_1, "str", "0101000xxxxxxxxx"),
		// NEW - Encoding T1 ThumbEE
		// A9.4.5 STRH (register)
		// strh<c> <Rt>,[<Rn>,<Rm>, lsl #1]
		// 0 1 0 1 0 0 1 Rm Rn Rt
		// 0101001xxxxxxxxx
		new OpcodeARM(Index.thumbEE_strh, "strh", "0101001xxxxxxxxx"),
		// NEW - Encoding E1 ThumbEE
		// A9.5.1 CHKA
		// chka<c> <Rn>,<Rm>
		// 1 1 0 0 1 0 1 0 N Rm Rn
		// 11001010xxxxxxxx
		new OpcodeARM(Index.thumbEE_chka, "chka", "11001010xxxxxxxx"),
		// NEW - Encoding E1 ThumbEE
		// A9.5.2 HB, HBL
		// hb{l}<c> #<HandlerID>
		// 1 1 0 0 0 0 1 L handler
		// 1100001xxxxxxxxx
		new OpcodeARM(Index.thumbEE_hb, "hb", "1100001xxxxxxxxx"),
		// NEW - Encoding E1 ThumbEE
		// A9.5.3 HBLP
		// hblp<c> #<imm>, #<HandlerID>
		// 1 1 0 0 0 1 imm5 handler
		// 110001xxxxxxxxxx
		new OpcodeARM(Index.thumbEE_hblp, "hblp", "110001xxxxxxxxxx"),
		// NEW - Encoding E1 ThumbEE
		// A9.5.4 HBP
		// hbp<c> #<imm>, #<HandlerID>
		// 1 1 0 0 0 0 0 0 imm3 handler
		// 11000000xxxxxxxx
		new OpcodeARM(Index.thumbEE_hbp, "hbp", "11000000xxxxxxxx"),
		// NEW - Encoding E1 ThumbEE
		// A9.5.5 LDR (immediate)
		// ldr<c> <Rt>,[R9{, #<imm>}]
		// 1 1 0 0 1 1 0 imm6 Rt
		// 1100110xxxxxxxxx
		new OpcodeARM(Index.thumbEE_ldr_2, "ldr", "1100110xxxxxxxxx"),
		// NEW - Encoding E2 ThumbEE
		// A9.5.5 LDR (immediate)
		// ldr<c> <Rt>,[R10{, #<imm>}]
		// 1 1 0 0 1 0 1 1 imm5 Rt
		// 11001011xxxxxxxx
		new OpcodeARM(Index.thumbEE_ldr_3, "ldr", "11001011xxxxxxxx"),
		// NEW - Encoding E3 ThumbEE
		// A9.5.5 LDR (immediate)
		// ldr<c> <Rt>,[<Rn>{, #-<imm>}]
		// 1 1 0 0 1 0 0 imm3 Rn Rt
		// 1100100xxxxxxxxx
		new OpcodeARM(Index.thumbEE_ldr_4, "ldr", "1100100xxxxxxxxx"),
		// NEW - Encoding E1 ThumbEE
		// A9.5.6 STR (immediate)
		// str<c> <Rt>, [R9, #<imm>]
		// 1 1 0 0 1 1 1 imm6 Rt
		// 1100111xxxxxxxxx
		new OpcodeARM(Index.thumbEE_str_2, "str", "1100111xxxxxxxxx"),
	};

	public static final OpcodeARM thumbEE_thumb2_opcode_table[] = {
		// NEW - Encoding T1 ThumbEE
		// A9.3.1 ENTERX, LEAVEX
		// enterx  Not permitted in IT block.	leavex  Not permitted in IT block.
		// 1 1 1 1 0 0 1 1 1 0 1 1 (1)(1)(1) (1) 1 0 (0) 0 (1)(1)(1)(1) 0 0 0 J_0_4_4 (1)(1)(1)(1)
		// 111100111011(1)(1)(1)(1)10(0)0(1)(1)(1)(1)000x(1)(1)(1)(1)
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
