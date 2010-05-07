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

package org.eclipse.cdt.debug.edc.internal.arm.disassembler;

/**
 * ARM Opcode map.
 */
public class OpcodeARM {

	public enum Index {
		arm_adc, arm_add, arm_and, arm_b, arm_bl, arm_bic, arm_bkpt, arm_blx1, arm_blx2, arm_bx, arm_cdp, arm_cdp2, arm_clz, arm_cmn, arm_cmp, arm_eor, arm_ldc, arm_ldc2, arm_ldm1, arm_ldm2, arm_ldm3, arm_ldr, arm_ldrb, arm_ldrbt, arm_ldrd, arm_ldrh, arm_ldrsb, arm_ldrsh, arm_ldrt, arm_mcr, arm_mcr2, arm_mcrr, arm_mcrr2, arm_mla, arm_mov, arm_mrc, arm_mrc2, arm_mrrc, arm_mrrc2, arm_mrs, arm_msr, arm_msr2, arm_mul, arm_mvn, arm_orr, arm_pld, arm_qadd, arm_qdadd, arm_qdsub, arm_qsub, arm_rsb, arm_rsc, arm_sbc, arm_smla, arm_smlal, arm_smlalxy, arm_smlaw, arm_smul, arm_smull, arm_smulw, arm_stc, arm_stc2, arm_stm1, arm_stm2, arm_str, arm_strb, arm_strbt, arm_strd, arm_strh, arm_strt, arm_sub, arm_swi, arm_swp, arm_swpb, arm_teq, arm_tst, arm_umlal, arm_umull, arm_bxj, arm_cps, arm_cpsid, arm_cpsie, arm_cpy, arm_pkhbt, arm_pkhtb, arm_rev, arm_rev16, arm_revsh, arm_rfeia, arm_rfeib, arm_rfeda, arm_rfedb, arm_setend, arm_smmul, arm_smmulr, arm_srsia, arm_srsib, arm_srsda, arm_srsdb, arm_ssat, arm_ssat16, arm_sxtb, arm_sxtb16, arm_sxth, arm_usat, arm_usat16, arm_uxtb, arm_uxtb16, arm_uxth, arm_qadd16, arm_qadd8, arm_qaddsubx, arm_qsub16, arm_qsub8, arm_qsubaddx, arm_sadd16, arm_sadd8, arm_saddsubx, arm_sel, arm_shadd16, arm_shadd8, arm_shaddsubx, arm_shsub16, arm_shsub8, arm_shsubaddx, arm_smlald, arm_smlaldx, arm_smlsld, arm_smlsldx, arm_smmls, arm_smmlsr, arm_smuad, arm_smuadx, arm_smusd, arm_smusdx, arm_ssub16, arm_ssub8, arm_ssubaddx, arm_uadd16, arm_uadd8, arm_uaddsubx, arm_uhadd16, arm_uhadd8, arm_uhaddsubx, arm_uhsub16, arm_uhsub8, arm_uhsubaddx, arm_uqadd16, arm_uqadd8, arm_uqaddsubx, arm_uqsub16, arm_uqsub8, arm_uqsubaddx, arm_usad8, arm_usada8, arm_usub16, arm_usub8, arm_usubaddx, arm_smmla, arm_smmlar, arm_smlsd, arm_smlsdx, arm_smlad, arm_smladx, arm_uxtab, arm_uxtab16, arm_uxtah, arm_sxtab, arm_sxtab16, arm_sxtah, arm_fabsd, arm_fabss, arm_faddd, arm_fadds, arm_fcmpd, arm_fcmped, arm_fcmpes, arm_fcmpezd, arm_fcmpezs, arm_fcmps, arm_fcmpzd, arm_fcmpzs, arm_fcpyd, arm_fcpys, arm_fcvtds, arm_fcvtsd, arm_fdivd, arm_fdivs, arm_fldd, arm_fldmd, arm_fldms, arm_flds, arm_fmacd, arm_fmacs, arm_fmdhr, arm_fmdlr, arm_fmdrr, arm_fmrdh, arm_fmrdl, arm_fmrrd, arm_fmrrs, arm_fmrs, arm_fmrx, arm_fmscd, arm_fmscs, arm_fmsr, arm_fmsrr, arm_fmstat, arm_fmuld, arm_fmuls, arm_fmxr, arm_fnegd, arm_fnegs, arm_fnmacd, arm_fnmacs, arm_fnmscd, arm_fnmscs, arm_fnmuld, arm_fnmuls, arm_fsitod, arm_fsitos, arm_fsqrtd, arm_fsqrts, arm_fstd, arm_fstmd, arm_fstms, arm_fsts, arm_fsubd, arm_fsubs, arm_ftosid, arm_ftosis, arm_ftouid, arm_ftouis, arm_fuitod, arm_fuitos, thumb_adc, thumb_add1, thumb_add2, thumb_add3, thumb_add4, thumb_add5, thumb_add6, thumb_add7, thumb_and, thumb_asr1, thumb_asr2, thumb_b1, thumb_b2, thumb_bic, thumb_bkpt, thumb_blx1, thumb_blx2, thumb_bx, thumb_cmn, thumb_cmp1, thumb_cmp2, thumb_cmp3, thumb_cps, thumb_cpy, thumb_eor, thumb_ldmia, thumb_ldr1, thumb_ldr2, thumb_ldr3, thumb_ldr4, thumb_ldrb1, thumb_ldrb2, thumb_ldrh1, thumb_ldrh2, thumb_ldrsb, thumb_ldrsh, thumb_lsl1, thumb_lsl2, thumb_lsr1, thumb_lsr2, thumb_mov1, thumb_mov2, thumb_mul, thumb_mvn, thumb_neg, thumb_orr, thumb_pop, thumb_push, thumb_rev, thumb_rev16, thumb_revsh, thumb_ror, thumb_sbc, thumb_setend, thumb_stmia, thumb_str1, thumb_str2, thumb_str3, thumb_strb1, thumb_strb2, thumb_strh1, thumb_strh2, thumb_sub1, thumb_sub2, thumb_sub3, thumb_sub4, thumb_swi, thumb_sxtb, thumb_sxth, thumb_tst, thumb_uxtb, thumb_uxth, invalid
	};

	public static final OpcodeARM arm_opcode_table[] = {
			new OpcodeARM(Index.arm_ldrd, "ldrd", "xxxx000xxxx0xxxxxxxxxxxx1101xxxx"),
			new OpcodeARM(Index.arm_ldrh, "ldrh", "xxxx000xxxx1xxxxxxxxxxxx1011xxxx"),
			new OpcodeARM(Index.arm_ldrsb, "ldrsb", "xxxx000xxxx1xxxxxxxxxxxx1101xxxx"),
			new OpcodeARM(Index.arm_ldrsh, "ldrsh", "xxxx000xxxx1xxxxxxxxxxxx1111xxxx"),
			new OpcodeARM(Index.arm_strd, "strd", "xxxx000xxxx0xxxxxxxxxxxx1111xxxx"),
			new OpcodeARM(Index.arm_strh, "strh", "xxxx000xxxx0xxxxxxxxxxxx1011xxxx"),
			new OpcodeARM(Index.arm_adc, "adc", "xxxx00x0101xxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_add, "add", "xxxx00x0100xxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_and, "and", "xxxx00x0000xxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_blx1, "blx", "1111101xxxxxxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_b, "b", "xxxx1010xxxxxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_bl, "bl", "xxxx1011xxxxxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_bic, "bic", "xxxx00x1110xxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_bkpt, "bkpt", "111000010010xxxxxxxxxxxx0111xxxx"),
			new OpcodeARM(Index.arm_blx2, "blx", "xxxx000100101111111111110011xxxx"),
			new OpcodeARM(Index.arm_bx, "bx", "xxxx000100101111111111110001xxxx"),
			new OpcodeARM(Index.arm_clz, "clz", "xxxx000101101111xxxx11110001xxxx"),
			new OpcodeARM(Index.arm_cmn, "cmn", "xxxx00x10111xxxx0000xxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_cmp, "cmp", "xxxx00x10101xxxx0000xxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_eor, "eor", "xxxx00x0001xxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_ldm1, "ldm", "xxxx100xx0x1xxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_ldm2, "ldm", "xxxx100xx101xxxx0xxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_ldm3, "ldm", "xxxx100xx1x1xxxx0xxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_ldr, "ldr", "xxxx01xxx0x1xxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_ldrb, "ldrb", "xxxx01xxx1x1xxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_ldrbt, "ldrbt", "xxxx01x0x111xxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_ldrt, "ldrt", "xxxx01x0x011xxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_mla, "mla", "xxxx0000001xxxxxxxxxxxxx1001xxxx"),
			new OpcodeARM(Index.arm_mov, "mov", "xxxx00x1101x0000xxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_mrs, "mrs", "xxxx00010x001111xxxx000000000000"),
			new OpcodeARM(Index.arm_msr, "msr", "xxxx00110x10xxxx1111xxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_msr2, "msr", "xxxx00010x10xxxx111100000000xxxx"),
			new OpcodeARM(Index.arm_mul, "mul", "xxxx0000000xxxxx0000xxxx1001xxxx"),
			new OpcodeARM(Index.arm_mvn, "mvn", "xxxx00x1111x0000xxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_orr, "orr", "xxxx00x1100xxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_pld, "pld", "111101x1x101xxxx1111xxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_qadd, "qadd", "xxxx00010000xxxxxxxx00000101xxxx"),
			new OpcodeARM(Index.arm_qdadd, "qdadd", "xxxx00010100xxxxxxxx00000101xxxx"),
			new OpcodeARM(Index.arm_qdsub, "qdsub", "xxxx00010110xxxxxxxx00000101xxxx"),
			new OpcodeARM(Index.arm_qsub, "qsub", "xxxx00010010xxxxxxxx00000101xxxx"),
			new OpcodeARM(Index.arm_rsb, "rsb", "xxxx00x0011xxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_rsc, "rsc", "xxxx00x0111xxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_sbc, "sbc", "xxxx00x0110xxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_smla, "smla", "xxxx00010000xxxxxxxxxxxx1xx0xxxx"),
			new OpcodeARM(Index.arm_smlal, "smlal", "xxxx0000111xxxxxxxxxxxxx1001xxxx"),
			new OpcodeARM(Index.arm_smlalxy, "smlal", "xxxx00010100xxxxxxxxxxxx1xx0xxxx"),
			new OpcodeARM(Index.arm_smlaw, "smlaw", "xxxx00010010xxxxxxxxxxxx1x00xxxx"),
			new OpcodeARM(Index.arm_smul, "smul", "xxxx00010110xxxx0000xxxx1xx0xxxx"),
			new OpcodeARM(Index.arm_smull, "smull", "xxxx0000110xxxxxxxxxxxxx1001xxxx"),
			new OpcodeARM(Index.arm_smulw, "smulw", "xxxx00010010xxxx0000xxxx1x10xxxx"),
			new OpcodeARM(Index.arm_stm1, "stm", "xxxx100xx0x0xxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_stm2, "stm", "xxxx100xx100xxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_str, "str", "xxxx01xxx0x0xxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_strb, "strb", "xxxx01xxx1x0xxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_strbt, "strbt", "xxxx01x0x110xxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_strt, "strt", "xxxx01x0x010xxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_sub, "sub", "xxxx00x0010xxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_swi, "swi", "xxxx1111xxxxxxxxxxxxxxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_swp, "swp", "xxxx00010000xxxxxxxx00001001xxxx"),
			new OpcodeARM(Index.arm_swpb, "swpb", "xxxx00010100xxxxxxxx00001001xxxx"),
			new OpcodeARM(Index.arm_teq, "teq", "xxxx00x10011xxxx0000xxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_tst, "tst", "xxxx00x10001xxxx0000xxxxxxxxxxxx"),
			new OpcodeARM(Index.arm_umlal, "umlal", "xxxx0000101xxxxxxxxxxxxx1001xxxx"),
			new OpcodeARM(Index.arm_umull, "umull", "xxxx0000100xxxxxxxxxxxxx1001xxxx"),
			// ARMv6 instructions
			new OpcodeARM(Index.arm_bxj, "bxj", "xxxx000100101111111111110010xxxx"),
			new OpcodeARM(Index.arm_cps, "cps", "111100010000001000000000000xxxxx"),
			new OpcodeARM(Index.arm_cpsid, "cpsid", "11110001000011x00000000xxx0xxxxx"),
			new OpcodeARM(Index.arm_cpsie, "cpsie", "11110001000010x00000000xxx0xxxxx"),
			new OpcodeARM(Index.arm_cpy, "cpy", "xxxx000110100000xxxx00000000xxxx"),
			new OpcodeARM(Index.arm_pkhbt, "pkhbt", "xxxx01101000xxxxxxxxxxxxx001xxxx"),
			new OpcodeARM(Index.arm_pkhtb, "pkhtb", "xxxx01101000xxxxxxxxxxxxx101xxxx"),
			new OpcodeARM(Index.arm_rev, "rev", "xxxx011010111111xxxx11110011xxxx"),
			new OpcodeARM(Index.arm_rev16, "rev16", "xxxx011010111111xxxx11111011xxxx"),
			new OpcodeARM(Index.arm_revsh, "revsh", "xxxx011011111111xxxx11111011xxxx"),
			new OpcodeARM(Index.arm_rfeia, "rfeia", "xxxx100010x1xxxx0000101000000000"),
			new OpcodeARM(Index.arm_rfeib, "rfeib", "xxxx100110x1xxxx0000101000000000"),
			new OpcodeARM(Index.arm_rfeda, "rfeda", "xxxx100000x1xxxx0000101000000000"),
			new OpcodeARM(Index.arm_rfedb, "rfedb", "xxxx100100x1xxxx0000101000000000"),
			new OpcodeARM(Index.arm_setend, "setend", "1111000100000001000000x000000000"),
			new OpcodeARM(Index.arm_smmul, "smmul", "xxxx01110101xxxx1111xxxx0001xxxx"),
			new OpcodeARM(Index.arm_smmulr, "smmulr", "xxxx01110101xxxx1111xxxx0011xxxx"),
			new OpcodeARM(Index.arm_srsia, "srsia", "1111100011x0110100000101000xxxxx"),
			new OpcodeARM(Index.arm_srsib, "srsib", "1111100111x0110100000101000xxxxx"),
			new OpcodeARM(Index.arm_srsda, "srsda", "1111100001x0110100000101000xxxxx"),
			new OpcodeARM(Index.arm_srsdb, "srsdb", "1111100101x0110100000101000xxxxx"),
			new OpcodeARM(Index.arm_ssat, "ssat", "xxxx0110101xxxxxxxxxxxxxxx01xxxx"),
			new OpcodeARM(Index.arm_ssat16, "ssat16", "xxxx01101010xxxxxxxx11110011xxxx"),
			new OpcodeARM(Index.arm_sxtb, "sxtb", "xxxx01101010xxxxxxxx11110011xxxx"),
			new OpcodeARM(Index.arm_sxtb16, "sxtb16", "xxxx011010001111xxxxxx000111xxxx"),
			new OpcodeARM(Index.arm_sxth, "sxth", "xxxx011010111111xxxxxx000111xxxx"),
			new OpcodeARM(Index.arm_usat, "usat", "xxxx0110111xxxxxxxxxxxxxxx01xxxx"),
			new OpcodeARM(Index.arm_usat16, "usat16", "xxxx01101110xxxxxxxx11110011xxxx"),
			new OpcodeARM(Index.arm_uxtb, "uxtb", "xxxx011011101111xxxxxx000111xxxx"),
			new OpcodeARM(Index.arm_uxtb16, "uxtb16", "xxxx011011001111xxxxxx000111xxxx"),
			new OpcodeARM(Index.arm_uxth, "uxth", "xxxx011011111111xxxxxx000111xxxx"),
			new OpcodeARM(Index.arm_qadd16, "qadd16", "xxxx01100010xxxxxxxx11110001xxxx"),
			new OpcodeARM(Index.arm_qadd8, "qadd8", "xxxx01100010xxxxxxxx11111001xxxx"),
			new OpcodeARM(Index.arm_qaddsubx, "qaddsubx", "xxxx01100010xxxxxxxx11110011xxxx"),
			new OpcodeARM(Index.arm_qsub16, "qsub16", "xxxx01100010xxxxxxxx11110111xxxx"),
			new OpcodeARM(Index.arm_qsub8, "qsub8", "xxxx01100010xxxxxxxx11111111xxxx"),
			new OpcodeARM(Index.arm_qsubaddx, "qsubaddx", "xxxx01100010xxxxxxxx11110101xxxx"),
			new OpcodeARM(Index.arm_sadd16, "sadd16", "xxxx01100001xxxxxxxx11110001xxxx"),
			new OpcodeARM(Index.arm_sadd8, "sadd8", "xxxx01100001xxxxxxxx11111001xxxx"),
			new OpcodeARM(Index.arm_saddsubx, "saddsubx", "xxxx01100001xxxxxxxx11110011xxxx"),
			new OpcodeARM(Index.arm_sel, "sel", "xxxx01101000xxxxxxxx11111011xxxx"),
			new OpcodeARM(Index.arm_shadd16, "shadd16", "xxxx01100011xxxxxxxx11110001xxxx"),
			new OpcodeARM(Index.arm_shadd8, "shadd8", "xxxx01100011xxxxxxxx11111001xxxx"),
			new OpcodeARM(Index.arm_shaddsubx, "shaddsubx", "xxxx01100011xxxxxxxx11110011xxxx"),
			new OpcodeARM(Index.arm_shsub16, "shsub16", "xxxx01100011xxxxxxxx11110111xxxx"),
			new OpcodeARM(Index.arm_shsub8, "shsub8", "xxxx01100011xxxxxxxx11111111xxxx"),
			new OpcodeARM(Index.arm_shsubaddx, "shsubaddx", "xxxx01100011xxxxxxxx11110101xxxx"),
			new OpcodeARM(Index.arm_smlald, "smlald", "xxxx01110100xxxxxxxxxxxx0001xxxx"),
			new OpcodeARM(Index.arm_smlaldx, "smlaldx", "xxxx01110100xxxxxxxxxxxx0011xxxx"),
			new OpcodeARM(Index.arm_smlsld, "smlsld", "xxxx01110100xxxxxxxxxxxx0101xxxx"),
			new OpcodeARM(Index.arm_smlsldx, "smlsldx", "xxxx01110100xxxxxxxxxxxx0111xxxx"),
			new OpcodeARM(Index.arm_smmls, "smmls", "xxxx01110101xxxxxxxxxxxx1101xxxx"),
			new OpcodeARM(Index.arm_smmlsr, "smmlsr", "xxxx01110101xxxxxxxxxxxx1111xxxx"),
			new OpcodeARM(Index.arm_smuad, "smuad", "xxxx01110000xxxx1111xxxx0001xxxx"),
			new OpcodeARM(Index.arm_smuadx, "smuadx", "xxxx01110000xxxx1111xxxx0011xxxx"),
			new OpcodeARM(Index.arm_smusd, "smusd", "xxxx01110000xxxx1111xxxx0101xxxx"),
			new OpcodeARM(Index.arm_smusdx, "smusdx", "xxxx01110000xxxx1111xxxx0111xxxx"),
			new OpcodeARM(Index.arm_ssub16, "ssub16", "xxxx01100001xxxxxxxx11110111xxxx"),
			new OpcodeARM(Index.arm_ssub8, "ssub8", "xxxx01100001xxxxxxxx11111111xxxx"),
			new OpcodeARM(Index.arm_ssubaddx, "ssubaddx", "xxxx01100001xxxxxxxx11110101xxxx"),
			new OpcodeARM(Index.arm_uadd16, "uadd16", "xxxx01100101xxxxxxxx11110001xxxx"),
			new OpcodeARM(Index.arm_uadd8, "uadd8", "xxxx01100101xxxxxxxx11111001xxxx"),
			new OpcodeARM(Index.arm_uaddsubx, "uaddsubx", "xxxx01100101xxxxxxxx11110011xxxx"),
			new OpcodeARM(Index.arm_uhadd16, "uhadd16", "xxxx01100111xxxxxxxx11110001xxxx"),
			new OpcodeARM(Index.arm_uhadd8, "uhadd8", "xxxx01100111xxxxxxxx11111001xxxx"),
			new OpcodeARM(Index.arm_uhaddsubx, "uhaddsubx", "xxxx01100111xxxxxxxx11110011xxxx"),
			new OpcodeARM(Index.arm_uhsub16, "uhsub16", "xxxx01100111xxxxxxxx11110111xxxx"),
			new OpcodeARM(Index.arm_uhsub8, "uhsub8", "xxxx01100111xxxxxxxx11111111xxxx"),
			new OpcodeARM(Index.arm_uhsubaddx, "uhsubaddx", "xxxx01100111xxxxxxxx11110101xxxx"),
			new OpcodeARM(Index.arm_uqadd16, "uqadd16", "xxxx01100110xxxxxxxx11110001xxxx"),
			new OpcodeARM(Index.arm_uqadd8, "uqadd8", "xxxx01100110xxxxxxxx11111001xxxx"),
			new OpcodeARM(Index.arm_uqaddsubx, "uqaddsubx", "xxxx01100110xxxxxxxx11110011xxxx"),
			new OpcodeARM(Index.arm_uqsub16, "uqsub16", "xxxx01100110xxxxxxxx11110111xxxx"),
			new OpcodeARM(Index.arm_uqsub8, "uqsub8", "xxxx01100110xxxxxxxx11111111xxxx"),
			new OpcodeARM(Index.arm_uqsubaddx, "uqsubaddx", "xxxx01100110xxxxxxxx11110101xxxx"),
			new OpcodeARM(Index.arm_usad8, "usad8", "xxxx01111000xxxx1111xxxx0001xxxx"),
			new OpcodeARM(Index.arm_usada8, "usada8", "xxxx01111000xxxxxxxxxxxx0001xxxx"),
			new OpcodeARM(Index.arm_usub16, "usub16", "xxxx01100101xxxxxxxx11110111xxxx"),
			new OpcodeARM(Index.arm_usub8, "usub8", "xxxx01100101xxxxxxxx11111111xxxx"),
			new OpcodeARM(Index.arm_usubaddx, "usubaddx", "xxxx01100101xxxxxxxx11110101xxxx"),
			new OpcodeARM(Index.arm_smmla, "smmla", "xxxx01110101xxxxxxxxxxxx0001xxxx"),
			new OpcodeARM(Index.arm_smmlar, "smmlar", "xxxx01110101xxxxxxxxxxxx0011xxxx"),
			new OpcodeARM(Index.arm_smlsd, "smlsd", "xxxx01110000xxxxxxxxxxxx0101xxxx"),
			new OpcodeARM(Index.arm_smlsdx, "smlsdx", "xxxx01110000xxxxxxxxxxxx0111xxxx"),
			new OpcodeARM(Index.arm_smlad, "smlad", "xxxx01110000xxxxxxxxxxxx0001xxxx"),
			new OpcodeARM(Index.arm_smladx, "smladx", "xxxx01110000xxxxxxxxxxxx0011xxxx"),
			new OpcodeARM(Index.arm_uxtab, "uxtab", "xxxx01101110xxxxxxxxxx000111xxxx"),
			new OpcodeARM(Index.arm_uxtab16, "uxtab16", "xxxx01101100xxxxxxxxxx000111xxxx"),
			new OpcodeARM(Index.arm_uxtah, "uxtah", "xxxx01101111xxxxxxxxxx000111xxxx"),
			new OpcodeARM(Index.arm_sxtab, "sxtab", "xxxx01101010xxxxxxxxxx000111xxxx"),
			new OpcodeARM(Index.arm_sxtab16, "sxtab16", "xxxx01101000xxxxxxxxxx000111xxxx"),
			new OpcodeARM(Index.arm_sxtah, "sxtah", "xxxx01101011xxxxxxxxxx000111xxxx"),
			// VFP instructions
			new OpcodeARM(Index.arm_fabsd, "fabsd", "cccc111010110000dddd10111100dddd"),
			new OpcodeARM(Index.arm_fabss, "fabss", "cccc11101d110000ssss101011m0ssss"),
			new OpcodeARM(Index.arm_faddd, "faddd", "cccc11100011dddddddd10110000dddd"),
			new OpcodeARM(Index.arm_fadds, "fadds", "cccc11100d11ssssssss1010n0m0ssss"),
			new OpcodeARM(Index.arm_fcmpd, "fcmpd", "cccc111010110100dddd10110100dddd"),
			new OpcodeARM(Index.arm_fcmps, "fcmps", "cccc11101d110100ssss101001m0ssss"),
			new OpcodeARM(Index.arm_fcmped, "fcmped", "cccc111010110100dddd10111100dddd"),
			new OpcodeARM(Index.arm_fcmpes, "fcmpes", "cccc11101d110100ssss101011m0ssss"),
			new OpcodeARM(Index.arm_fcmpezd, "fcmpezd", "cccc111010110101dddd101111000000"),
			new OpcodeARM(Index.arm_fcmpezs, "fcmpezs", "cccc11101d110101ssss101011000000"),
			new OpcodeARM(Index.arm_fcmpzd, "fcmpzd", "cccc111010110101dddd101101000000"),
			new OpcodeARM(Index.arm_fcmpzs, "fcmpzs", "cccc11101d110101ssss101001000000"),
			new OpcodeARM(Index.arm_fcpyd, "fcpyd", "cccc111010110000dddd10110100dddd"),
			new OpcodeARM(Index.arm_fcpys, "fcpys", "cccc11101d110000ssss101001m0ssss"),
			new OpcodeARM(Index.arm_fcvtds, "fcvtds", "cccc111010110111dddd101011m0ssss"),
			new OpcodeARM(Index.arm_fcvtsd, "fcvtsd", "cccc11101d110111ssss10111100dddd"),
			new OpcodeARM(Index.arm_fdivd, "fdivd", "cccc11101000dddddddd10110000dddd"),
			new OpcodeARM(Index.arm_fdivs, "fdivs", "cccc11101d00ssssssss1010n0m0ssss"),
			new OpcodeARM(Index.arm_fldd, "fldd", "cccc1101u001rrrrdddd1011iiiiiiii"),
			new OpcodeARM(Index.arm_flds, "flds", "cccc1101ud01rrrrssss1010iiiiiiii"),
			new OpcodeARM(Index.arm_fmacd, "fmacd", "cccc11100000dddddddd10110000dddd"),
			new OpcodeARM(Index.arm_fmacs, "fmacs", "cccc11100d00ssssssss1010n0m0ssss"),
			new OpcodeARM(Index.arm_fmdhr, "fmdhr", "cccc11100010ddddrrrr101100010000"),
			new OpcodeARM(Index.arm_fmdlr, "fmdlr", "cccc11100000ddddrrrr101100010000"),
			new OpcodeARM(Index.arm_fmdrr, "fmdrr", "cccc11000100rrrrrrrr10110001dddd"),
			new OpcodeARM(Index.arm_fmrdh, "fmrdh", "cccc11100011ddddrrrr101100010000"),
			new OpcodeARM(Index.arm_fmrdl, "fmrdl", "cccc11100001ddddrrrr101100010000"),
			new OpcodeARM(Index.arm_fmrrd, "fmrrd", "cccc11000101rrrrrrrr10110001dddd"),
			new OpcodeARM(Index.arm_fmrrs, "fmrrs", "cccc11000101rrrrrrrr101000m1ssss"),
			new OpcodeARM(Index.arm_fmrs, "fmrs", "cccc11100001ssssrrrr1010n0010000"),
			new OpcodeARM(Index.arm_fmstat, "fmstat", "cccc1110111100011111101000010000"),
			new OpcodeARM(Index.arm_fmrx, "fmrx", "cccc11101111ffffrrrr101000010000"),
			new OpcodeARM(Index.arm_fmscd, "fmscd", "cccc11100001dddddddd10110000dddd"),
			new OpcodeARM(Index.arm_fmscs, "fmscs", "cccc11100d01ssssssss1010n0m0ssss"),
			new OpcodeARM(Index.arm_fmsr, "fmsr", "cccc11100000ssssrrrr1010n0010000"),
			new OpcodeARM(Index.arm_fmsrr, "fmsrr", "cccc11000100rrrrrrrr101000m1ssss"),
			new OpcodeARM(Index.arm_fmuld, "fmuld", "cccc11100010dddddddd10110000dddd"),
			new OpcodeARM(Index.arm_fmuls, "fmuls", "cccc11100d10ssssssss1010n0m0ssss"),
			new OpcodeARM(Index.arm_fmxr, "fmxr", "cccc11101110ffffrrrr101000010000"),
			new OpcodeARM(Index.arm_fnegd, "fnegd", "cccc111010110001dddd10110100dddd"),
			new OpcodeARM(Index.arm_fnegs, "fnegs", "cccc11101d110001ssss101001m0ssss"),
			new OpcodeARM(Index.arm_fnmacd, "fnmacd", "cccc11100000dddddddd10110100dddd"),
			new OpcodeARM(Index.arm_fnmacs, "fnmacs", "cccc11100d00ssssssss1010n1m0ssss"),
			new OpcodeARM(Index.arm_fnmscd, "fnmscd", "cccc11100001dddddddd10110100dddd"),
			new OpcodeARM(Index.arm_fnmscs, "fnmscs", "cccc11100d01ssssssss1010n1m0ssss"),
			new OpcodeARM(Index.arm_fnmuld, "fnmuld", "cccc11100010dddddddd10110100dddd"),
			new OpcodeARM(Index.arm_fnmuls, "fnmuls", "cccc11100d10ssssssss1010n1m0ssss"),
			new OpcodeARM(Index.arm_fsitod, "fsitod", "cccc111010111000dddd101111m0ssss"),
			new OpcodeARM(Index.arm_fsitos, "fsitos", "cccc11101d111000ssss101011m0ssss"),
			new OpcodeARM(Index.arm_fsqrtd, "fsqrtd", "cccc111010110001dddd10111100dddd"),
			new OpcodeARM(Index.arm_fsqrts, "fsqrts", "cccc11101d110001ssss101011m0ssss"),
			new OpcodeARM(Index.arm_fstd, "fstd", "cccc1101u000rrrrdddd1011iiiiiiii"),
			new OpcodeARM(Index.arm_fsts, "fsts", "cccc1101ud00rrrrssss1010iiiiiiii"),
			new OpcodeARM(Index.arm_fstmd, "fstmd", "cccc110pu0w0rrrrdddd1011iiiiiiii"),
			new OpcodeARM(Index.arm_fstms, "fstms", "cccc110pudw0rrrrssss1010iiiiiiii"),
			new OpcodeARM(Index.arm_fsubd, "fsubd", "cccc11100011dddddddd10110100dddd"),
			new OpcodeARM(Index.arm_fsubs, "fsubs", "cccc11100d11ssssssss1010n1m0ssss"),
			new OpcodeARM(Index.arm_ftosid, "ftosid", "cccc11101d111101ssss1011z100dddd"),
			new OpcodeARM(Index.arm_ftosis, "ftosis", "cccc11101d111101ssss1010z1m0ssss"),
			new OpcodeARM(Index.arm_ftouid, "ftouid", "cccc11101d111100ssss1011z100dddd"),
			new OpcodeARM(Index.arm_ftouis, "ftouis", "cccc11101d111100ssss1010z1m0ssss"),
			new OpcodeARM(Index.arm_fuitod, "fuitod", "cccc111010111000dddd101101m0ssss"),
			new OpcodeARM(Index.arm_fuitos, "fuitos", "cccc11101d111000ssss101001m0ssss"),
			new OpcodeARM(Index.arm_fldmd, "fldmd", "cccc110pu0w1rrrrdddd1011iiiiiiii"),
			new OpcodeARM(Index.arm_fldms, "fldms", "cccc110pudw1rrrrssss1010iiiiiiii"),
			// Coprocessor instructions
			new OpcodeARM(Index.arm_cdp, "cdp", "xxxx1110xxxxxxxxxxxxxxxxxxx0xxxx"), // coprocessor
																						// instruction
			new OpcodeARM(Index.arm_cdp2, "cdp2", "11111110xxxxxxxxxxxxxxxxxxx0xxxx"), // coprocessor
																						// instruction
			new OpcodeARM(Index.arm_mcr, "mcr", "xxxx1110xxx0xxxxxxxxxxxxxxx1xxxx"), // coprocessor
																						// instruction
			new OpcodeARM(Index.arm_mcr2, "mcr2", "11111110xxx0xxxxxxxxxxxxxxx1xxxx"), // coprocessor
																						// instruction
			new OpcodeARM(Index.arm_mcrr, "mcrr", "xxxx11000100xxxxxxxxxxxxxxxxxxxx"), // coprocessor
																						// instruction
			new OpcodeARM(Index.arm_mcrr2, "mcrr2", "111111000100xxxxxxxxxxxxxxxxxxxx"), // coprocessor
																							// instruction
			new OpcodeARM(Index.arm_mrc, "mrc", "xxxx1110xxx1xxxxxxxxxxxxxxx1xxxx"), // coprocessor
																						// instruction
			new OpcodeARM(Index.arm_mrc2, "mrc2", "11111110xxx1xxxxxxxxxxxxxxx1xxxx"), // coprocessor
																						// instruction
			new OpcodeARM(Index.arm_mrrc, "mrrc", "xxxx11000101xxxxxxxxxxxxxxxxxxxx"), // coprocessor
																						// instruction
			new OpcodeARM(Index.arm_mrrc2, "mrrc2", "111111000101xxxxxxxxxxxxxxxxxxxx"), // coprocessor
																							// instruction
			new OpcodeARM(Index.arm_ldc, "ldc", "xxxx110xxxx1xxxxxxxxxxxxxxxxxxxx"), // coprocessor
																						// instruction
			new OpcodeARM(Index.arm_ldc2, "ldc2", "1111110xxxx1xxxxxxxxxxxxxxxxxxxx"), // coprocessor
																						// instruction
			new OpcodeARM(Index.arm_stc, "stc", "xxxx110xxxx0xxxxxxxxxxxxxxxxxxxx"), // coprocessor
																						// instruction
			new OpcodeARM(Index.arm_stc2, "stc2", "1111110xxxx0xxxxxxxxxxxxxxxxxxxx"), // coprocessor
																						// instruction
	};

	public static final OpcodeARM thumb_opcode_table[] = { new OpcodeARM(Index.thumb_mov2, "mov", "0001110000xxxxxx"),
			new OpcodeARM(Index.thumb_swi, "swi", "11011111xxxxxxxx"),
			new OpcodeARM(Index.thumb_adc, "adc", "0100000101xxxxxx"),
			new OpcodeARM(Index.thumb_add1, "add", "0001110xxxxxxxxx"),
			new OpcodeARM(Index.thumb_add2, "add", "00110xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_add3, "add", "0001100xxxxxxxxx"),
			new OpcodeARM(Index.thumb_add4, "add", "01000100xxxxxxxx"),
			new OpcodeARM(Index.thumb_add5, "add", "10100xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_add6, "add", "10101xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_add7, "add", "101100000xxxxxxx"),
			new OpcodeARM(Index.thumb_and, "and", "0100000000xxxxxx"),
			new OpcodeARM(Index.thumb_asr1, "asr", "00010xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_asr2, "asr", "0100000100xxxxxx"),
			new OpcodeARM(Index.thumb_b1, "b", "1101xxxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_b2, "b", "11100xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_bic, "bic", "0100001110xxxxxx"),
			new OpcodeARM(Index.thumb_bkpt, "bkpt", "10111110xxxxxxxx"),
			new OpcodeARM(Index.thumb_blx1, "blx", "11110xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_blx2, "blx", "010001111xxxx000"),
			new OpcodeARM(Index.thumb_bx, "bx", "010001110xxxx000"),
			new OpcodeARM(Index.thumb_cmn, "cmn", "0100001011xxxxxx"),
			new OpcodeARM(Index.thumb_cmp1, "cmp", "00101xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_cmp2, "cmp", "0100001010xxxxxx"),
			new OpcodeARM(Index.thumb_cmp3, "cmp", "01000101xxxxxxxx"),
			new OpcodeARM(Index.thumb_cps, "cps", "10110110011x0xxx"),
			new OpcodeARM(Index.thumb_cpy, "cpy", "01000110xxxxxxxx"),
			new OpcodeARM(Index.thumb_eor, "eor", "0100000001xxxxxx"),
			new OpcodeARM(Index.thumb_ldmia, "ldmia", "11001xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_ldr1, "ldr", "01101xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_ldr2, "ldr", "0101100xxxxxxxxx"),
			new OpcodeARM(Index.thumb_ldr3, "ldr", "01001xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_ldr4, "ldr", "10011xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_ldrb1, "ldrb", "01111xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_ldrb2, "ldrb", "0101110xxxxxxxxx"),
			new OpcodeARM(Index.thumb_ldrh1, "ldrh", "10001xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_ldrh2, "ldrh", "0101101xxxxxxxxx"),
			new OpcodeARM(Index.thumb_ldrsb, "ldrsb", "0101011xxxxxxxxx"),
			new OpcodeARM(Index.thumb_ldrsh, "ldrsh", "0101111xxxxxxxxx"),
			new OpcodeARM(Index.thumb_lsl1, "lsl", "00000xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_lsl2, "lsl", "0100000010xxxxxx"),
			new OpcodeARM(Index.thumb_lsr1, "lsr", "00001xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_lsr2, "lsr", "0100000011xxxxxx"),
			new OpcodeARM(Index.thumb_mov1, "mov", "00100xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_mul, "mul", "0100001101xxxxxx"),
			new OpcodeARM(Index.thumb_mvn, "mvn", "0100001111xxxxxx"),
			new OpcodeARM(Index.thumb_neg, "neg", "0100001001xxxxxx"),
			new OpcodeARM(Index.thumb_orr, "orr", "0100001100xxxxxx"),
			new OpcodeARM(Index.thumb_pop, "pop", "1011110xxxxxxxxx"),
			new OpcodeARM(Index.thumb_push, "push", "1011010xxxxxxxxx"),
			new OpcodeARM(Index.thumb_rev, "rev", "1011101000xxxxxx"),
			new OpcodeARM(Index.thumb_rev16, "rev16", "1011101001xxxxxx"),
			new OpcodeARM(Index.thumb_revsh, "revsh", "1011101011xxxxxx"),
			new OpcodeARM(Index.thumb_ror, "ror", "0100000111xxxxxx"),
			new OpcodeARM(Index.thumb_sbc, "sbc", "0100000110xxxxxx"),
			new OpcodeARM(Index.thumb_setend, "setend", "101101100101x000"),
			new OpcodeARM(Index.thumb_stmia, "stmia", "11000xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_str1, "str", "01100xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_str2, "str", "0101000xxxxxxxxx"),
			new OpcodeARM(Index.thumb_str3, "str", "10010xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_strb1, "strb", "01110xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_strb2, "strb", "0101010xxxxxxxxx"),
			new OpcodeARM(Index.thumb_strh1, "strh", "10000xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_strh2, "strh", "0101001xxxxxxxxx"),
			new OpcodeARM(Index.thumb_sub1, "sub", "0001111xxxxxxxxx"),
			new OpcodeARM(Index.thumb_sub2, "sub", "00111xxxxxxxxxxx"),
			new OpcodeARM(Index.thumb_sub3, "sub", "0001101xxxxxxxxx"),
			new OpcodeARM(Index.thumb_sub4, "sub", "101100001xxxxxxx"),
			new OpcodeARM(Index.thumb_sxtb, "sxtb", "1011001001xxxxxx"),
			new OpcodeARM(Index.thumb_sxth, "sxth", "1011001000xxxxxx"),
			new OpcodeARM(Index.thumb_tst, "tst", "0100001000xxxxxx"),
			new OpcodeARM(Index.thumb_uxtb, "uxtb", "1011001011xxxxxx"),
			new OpcodeARM(Index.thumb_uxth, "uxth", "1011001010xxxxxx"), };

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
