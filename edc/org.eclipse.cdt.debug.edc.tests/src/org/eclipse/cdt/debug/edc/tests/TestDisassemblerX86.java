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
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IJumpToAddress;
import org.eclipse.cdt.debug.edc.JumpToAddress;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler.IDisassemblerOptions;
import org.eclipse.cdt.debug.edc.x86.disassembler.DisassemblerX86;
import org.eclipse.cdt.debug.edc.x86.disassembler.InstructionParserX86;
import org.eclipse.cdt.debug.edc.x86.disassembler.OpcodeX86;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author LWang
 * 
 */
public class TestDisassemblerX86 {

	static Map<String, Object> sOptions = null;
	static DisassemblerX86 sDisassembler;

	@BeforeClass
	public static void beforeClass() {
		/*
		 * set up common disassembler options.
		 */
		sOptions = new HashMap<String, Object>();
		sOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_ADDRESS, true);
		sOptions.put(IDisassemblerOptions.MNEMONICS_SHOW_BYTES, true);

		sDisassembler = new DisassemblerX86();
	}

//	@Before
//	public void setUp() throws Exception {
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}
	
	@Test
	public void testAdd() {
		
		/*
		 * This test can essentially cover such similar instructions: adc, and,
		 * xor, or, sbb, sub, cmp
		 */
		System.out.println("\n===================== add ========================\n");

		String[] insts = { "00 00", "add    %al,(%eax)", "01 f6", "add    %esi,%esi", "02 14 0e",
				"add    (%esi,%ecx,1),%dl", "03 5d 0c", "add    0xc(%ebp),%ebx", "04 01", "add    $0x1,%al",
				"05 14 08 00 00", "add    $0x814,%eax", "81 c3 58 ad 29 00", "add    $0x29ad58,%ebx",
				// find case for 0x82 ?
				"83 c4 04", "add    $0x4,%esp", "83 85 d0 fe ff ff 01", "addl   $0x1,-0x130(%ebp)", };

		disassembleInstArray(insts);
	}

	@Test
	public void testCall() {
		
		System.out.println("\n===================== call ========================\n");

		disassembleInst(0x80a4577, "e8 a4 f8 fb ff", new JumpToAddress(0x8063e20, true, true),
				"80a4577:       e8 a4 f8 fb ff          call   8063e20", null);

		disassembleInst(0x80834e8, "ff 14 85 e8 ce 31 08", new JumpToAddress("*0x831cee8(,%eax,4)", true, true),
				"80834e8:       ff 14 85 e8 ce 31 08    call   *0x831cee8(,%eax,4)", null);

		disassembleInst(0x8085f00, "ff d0", new JumpToAddress("*%eax", true, true),
				"8085f00:       ff d0                   call   *%eax", null);

		disassembleInst(0x100000, "ff 52 03", new JumpToAddress("*0x3(%edx)", true, true),
				"100000: 		ff 52 03                call   *0x3(%edx)", null);

		// Note: "lcall" and "call" are both acceptable by gnu as.
		//
		disassembleInst(0x100000, "9a 45 23 01 00 00 10", new JumpToAddress("$0x1000,$0x12345", true, true),
				"100000: 9a 45 23 01 00 00 10    call  $0x1000,$0x12345", null);
	}

	/**
	 * This is used to debug disassembling of a single instruction.
	 */
	@Test
	public void testDebugBench() {
		
		System.out.println("\n===================== Debug bench ========================\n");

		disassembleInst(0x100000, "66 90", null, // don't care
				"100000:        66 90                   xchg   %ax,%ax", null);
	}

	@Test
	public void testErrorCase() {
		
		System.out.println("\n===================== Error Cases ========================\n");

		// Case: not enough data, buffer underflow.
		//
		String code = "ea ea";
		try {
			disassembleInst(0x1000000, code, null, // don't check
													// jump-to-address.
					null, null);
			Assert.fail("Fail to detect error on disassembling: " + code);
		} catch (AssertionError e) {
			System.out.println("Error found: " + e.getMessage());
		}

		// Case: two-byte code: not supported yet.
		//
		code = "0f 12";
		try {
			disassembleInst(0x1000000, code, null, // don't check
													// jump-to-address.
					null, null);
			Assert.fail("Wow, two-byte opcode already supported !" + code);
		} catch (AssertionError e) {
			System.out.println("Error found: " + e.getMessage());
		}

		// case:
		// Case: three-byte code: not supported yet.
		//
		code = "0x0f 38 12";
		try {
			disassembleInst(0x1000000, code, null, // don't check
													// jump-to-address.
					null, null);
			Assert.fail("Wow, three-byte opcode already supported !" + code);
		} catch (AssertionError e) {
			System.out.println("Error found: " + e.getMessage());
		}
	}

	@Test
	public void testFpu() {
		
		System.out.println("\n===================== fpu instructions ===========\n");

		String[] insts = { "d8 c0", "fadd   %st(0),%st", "d8 05 00 ac 2b 08", "fadds  0x82bac00", "d8 1d 00 00 01 00",
				"fcomps 0x10000", "d8 f1", "fdiv   %st(1),%st", "d8 3d 00 00 01 00", "fdivrs 0x10000",
				"da 05 00 00 01 00", "fiaddl 0x10000", "d9 cc", "fxch   %st(4)", "d9 6d f4", "fldcw  -0xc(%ebp)",
				"d9 25 00 00 01 00", "fldenv 0x10000", "d9 35 00 00 01 00", "fnstenv 0x10000", "d9 f1", "fyl2x",
				"d9 e9", "fldl2t", "da c2", "fcmovb %st(2),%st", "da e9", "fucompp", "db 2d 00 00 01 00",
				"fldt   0x10000", "dc 35 00 00 01 00", "fdivl  0x10000", "dc f9",
				"fdiv  %st,%st(1)", // GNU objdump bug: displays this as
									// "fdivr".
				"dd 35 00 00 01 00", "fnsave 0x10000", "dd c2", "ffree  %st(2)", "dd e9", "fucomp %st(1)",
				"de 05 00 00 01 00", "fiadd  0x10000", "de f1", "fdivrp  %st,%st(1)", // another
																						// objdump
																						// bug:
																						// mistaken
																						// as
																						// "fdivp"
				"de e9", "fsubp %st,%st(1)",// another objdump bug: mistaken as
											// "fsubrp"
				"df 25 00 00 01 00", "fbld   0x10000", "df 2c 24", "fildll (%esp)", "df e0", "fnstsw  %ax", };

		disassembleInstArray(insts);
	}

	// disassemble instructions in one buffer.
	//
	@Test
	public void testInstructionBlock() {
		
		System.out.println("\n===================== instruction block ========================\n");

		String code = " eb 20" + " e9 e0 ff ff ff" + " 74 05" + " 72 e7" + " ea 78 56 34 12 bc fe";

		disassembleBlock(0x100000, code, null);
	}

	@Test
	public void testInc() {
		
		System.out.println("\n===================== inc ========================\n");

		String[] insts = { "40", "inc %eax", "66 40", "inc %ax", "fe 05 00 00 01 00", "incb   0x10000",
				"66 ff 05 00 00 01 00", "incw   0x10000", "ff 05 00 00 01 00", "incl   0x10000", };

		disassembleInstArray(insts);
	}

	@Test
	public void testIn_out() {
		
		System.out.println("\n===================== in & out ========================\n");

		String[] insts = { "e4 10", "in     $0x10,%al", "66 e5 10", "in     $0x10,%ax", "ec", "in     (%dx),%al",
				"e6 10", "out    %al,$0x10", "66 e7 10", "out    %ax,$0x10", "ee", "out    %al,(%dx)", "66 ef",
				"out    %ax,(%dx)", };

		disassembleInstArray(insts);
	}

	@Test
	public void testIns_outs() {
		
		System.out.println("\n===================== ins & outs ========================\n");

		String[] insts = { "6c", "insb   (%dx),%es:(%edi)", "66 6d", "insw   (%dx),%es:(%edi)", "6d",
				"insl   (%dx),%es:(%edi)", "6e", "outsb  %ds:(%esi),(%dx)", "66 6f", "outsw  %ds:(%esi),(%dx)", };

		disassembleInstArray(insts);
	}

	/**
	 * invalid instructions.
	 */
	@Test
	public void testInvalid() {
		
		System.out.println("\n===================== Invalid Opcode ========================\n");

		// case: invalid opcode "F6 /1"
		// 80abe02: f6 4c
		disassembleInst(0x80abe02, "f6 4c", null, // don't check
													// jump-to-address.
				"80abe02:       f6 4c " + OpcodeX86.sInvalidOpcode.getName(), null);

		disassembleInst(0x80abe02, "0f 36", null, // don't check
													// jump-to-address.
				"80abe02:       0f 36 " + OpcodeX86.sInvalidOpcode.getName(), null);
	}

	@Test
	public void testJcc() {
		
		System.out.println("\n===================== Jcc ========================\n");

		disassembleInst(0x804828f, "74 05", new JumpToAddress(0x8048296, false, false),
				"804828f:       74 05                   je     8048296", null);

		disassembleInst(0x8048357, "72 e7", new JumpToAddress(0x8048340, false, false),
				"8048357:       72 e7                   jb     8048340", null);

		disassembleInst(0x8088b52, "0f 88 f0 00 00 00", new JumpToAddress(0x8088c48, false, false),
				"8088b52:       0f 88 f0 00 00 00       js     8088c48", null);

		disassembleInst(0x8083c53, "0f 8e cf fc ff ff", new JumpToAddress(0x8083928, false, false),
				"8083c53:       0f 8e cf fc ff ff       jle    8083928", null);

	}

	@Test
	public void testJump() {
		
		System.out.println("\n===================== Jump ========================\n");

		disassembleInst(0x1000000, "eb 20", new JumpToAddress(0x1000022, true, false),
				"1000000:	eb 20 		jmp	0x1000022", null);

		disassembleInst(0x80482bf, "e9 e0 ff ff ff", new JumpToAddress(0x80482a4, true, false),
				"80482bf:       e9 e0 ff ff ff          jmp    0x80482a4", null);

		disassembleInst(0x100000, "ea 78 56 34 12 bc fe", new JumpToAddress("$0xfebc,$0x12345678", true, false),
				"100000: ea 78 56 34 12 bc fe    ljmp   $0xfebc,$0x12345678", null);

		disassembleInst(0x80822c6, "ff 25 fc cf 31 08", new JumpToAddress("*0x831cffc", true, false),
				"80822c6:       ff 25 fc cf 31 08       jmp    *0x831cffc", null);

		disassembleInst(0x80861c0, "ff e1", new JumpToAddress("*%ecx", true, false),
				"80861c0:       ff e1                   jmp    *%ecx", null);
	}

	@Test
	public void testLoop() {
		
		System.out.println("\n===================== loop ========================\n");

		disassembleInst(0x52, "e1 0f", new JumpToAddress(0x63, false, false),
				"52:   e1 0f                   loope  0x63", null);
	}

	@Test
	public void testMisc() {
		
		System.out.println("\n===================== misc ========================\n");

		String[] insts = {
				// Put in alphabetic order of instruction names
				//
				"37", "aaa", "d4 03", "aam    $0x3", "d5 03", "aad    $0x3", "3f", "aas", "63 c3", "arpl   %ax,%bx",
				"62 05 00 10 00 00", "bound  %eax,0x1000", "0f c8", "bswap  %eax", "0f a3 05 00 01 00 00",
				"bt     %eax,0x100", "0f ba 25 00 00 02 00 10", "btl    $0x10,0x20000", "80 7f 13 2e",
				"cmpb   $0x2e,0x13(%edi)", "81 7d e8 aa aa aa 0a", "cmpl   $0xaaaaaaa,-0x18(%ebp)", "98", "cwtl", "27",
				"daa", "2f", "das", "f6 75 da", "divb   -0x26(%ebp)", "c8 00 02 10", "enter  $0x200,$0x10", "9b",
				"fwait", "f7 ea", "imul   %edx", "69 ca 40 42 0f 00", "imul   $0xf4240,%edx,%ecx", "0f af 5c b8 fc",
				"imul   -0x4(%eax,%edi,4),%ebx", "c5 84 3e 00 00 01 00", "lds    0x10000(%esi,%edi,1),%eax", "8d 36",
				"lea    (%esi),%esi", "8d 34 3e", "lea    (%esi,%edi,1),%esi", "8d b4 3e 00 00 01 00",
				"lea    0x10000(%esi,%edi,1),%esi", "c9", "leave", "90", "nop", "f3 90", "pause", "07", "pop %es",
				"5b", "pop %ebx", "8f 05 00 00 01 00", "pop   0x10000", "06", "push %es", "0f a0", "push   %fs", "16",
				"push %ss", "55", "push %ebp", "9c", "pushf", "f3 a6", "repz cmpsb %es:(%edi),%ds:(%esi)", "66 a7",
				"cmpsw  %es:(%edi),%ds:(%esi)", "0f 31", "rdtsc", "c2 04 00", "ret    $0x4", "9e", "sahf",
				"c1 bd dc fc ff ff 1f", "sarl   $0x1f,-0x324(%ebp)", "0f 94 c0", "sete   %al", "0f a4 c2 04",
				"shld   $0x4,%eax,%edx", "0f a5 c2", "shld   %cl,%eax,%edx", "0f ac fe 04", "shrd   $0x4,%edi,%esi",
				"0f 00 05 00 00 01 00", "sldt   0x10000", "0f 00 c0", "sldt   %eax", "0f 01 25 00 00 01 00",
				"smsw   0x10000", "f9", "stc", "fd", "std",
				"f3 ab",
				"rep stosl %eax,%es:(%edi)", // with prefix
				"66 ab", "stosw   %ax,%es:(%edi)", "83 ec 28", "sub    $0x28,%esp", "0f 34", "sysenter", "0f 01 c1",
				"vmcall", "0f 01 c2", "vmlaunch", "0f c1 05 00 00 01 00", "xadd   %eax,0x10000", "d7",
				"xlat   %ds:(%ebx)", "31 d2", "xor    %edx,%edx", };

		disassembleInstArray(insts);
	}

	@Test
	public void testMov() {
		
		System.out.println("\n===================== mov ========================\n");

		String[] insts = {
				"66 8b 1d 00 01 00 00",
				"mov    0x100,%bx", // with prefix
				"88 02", "mov    %al,(%edx)", "89 04 24", "mov    %eax,(%esp)", "89 15 20 8c 33 08",
				"mov    %edx,0x8338c20", "8a 1d 10 00 00 00", "mov    0x10,%bl", "8b 15 38 8c 33 08",
				"mov    0x8338c38,%edx", "65 8b 0d 14 00 00 00", "mov    %gs:0x14,%ecx",
				"8c 1d 00 10 00 00",
				"mov    %ds,0x1000",
				"8e 1d 00 10 00 00",
				"mov    0x1000,%ds",
				"a1 54 ce 33 08",
				"mov    0x833ce54,%eax",
				"65 a1 14 00 00 00",
				"mov    %gs:0x14,%eax", // with prefix
				"a2 6c 82 32 08", "mov    %al,0x832826c", "a3 28 82 32 08", "mov    %eax,0x8328228", "a4",
				"movsb  %ds:(%esi),%es:(%edi)", "b1 16", "mov    $0x16,%cl", "b3 04", "mov    $0x4,%bl", "b7 00",
				"mov    $0x0,%bh", "b8 4a 6a 27 08", "mov    $0x8276a4a,%eax", "be 01 00 00 00", "mov    $0x1,%esi",
				"c6 04 30 00", "movb   $0x0,(%eax,%esi,1)", "c7 45 ec 00 00 00 00", "movl   $0x0,-0x14(%ebp)",
				"f3 a5",
				"rep movsl %ds:(%esi),%es:(%edi)",

				// two-byte opcode
				"0f 20 c0", "mov    %cr0,%eax", "0f 21 f8", "mov    %db7,%eax",

				"0f 48 05 00 00 01 00", "cmovs  0x10000,%eax", "0f be c2", "movsbl %dl,%eax", "66 0f be c2",
				"movsbw %dl,%ax", "66 0f be 82 00 b0 2c 08", "movsbw 0x82cb000(%edx),%ax", "0f bf 49 76",
				"movswl 0x76(%ecx),%ecx", "0f b6 c0", "movzbl %al,%eax", "0f b7 4a 1a", "movzwl 0x1a(%edx),%ecx",

				// three-byte opcode
				"0f 38 f1 05 00 00 01 00", "movbe  %eax,0x10000", };

		disassembleInstArray(insts);
	}

	@Test
	public void testOptions() {
		
		System.out.println("\n===================== Disassembler Options ========================\n");

		// An imaginary case: jump instruction
		//
		IAddress addr = new Addr64("0x1000000");
		ByteBuffer codeBuf = ByteBuffer.wrap(new byte[] { (byte) 0xeb, 0x20 });

		InstructionParserX86 disa = new InstructionParserX86(addr, codeBuf);

		Map<String, Object> options = new HashMap<String, Object>();

		IDisassembledInstruction output = null;
		try {
			output = disa.disassemble(options);
		} catch (CoreException e) {
			Assert.fail(e.getLocalizedMessage());
		}

		Assert.assertEquals(2, output.getSize());
		Assert.assertNotNull(output.getJumpToAddress());

		System.out.println(output);

		// Disassemble the same instruction with different options.
		//
		try {
			options.put(IDisassemblerOptions.MNEMONICS_SHOW_ADDRESS, true);
			output = disa.disassemble(options);
		} catch (CoreException e) {
			Assert.fail(e.getLocalizedMessage());
		}

		System.out.println(output.getMnemonics());

		try {
			options.put(IDisassemblerOptions.MNEMONICS_SHOW_BYTES, true);
			output = disa.disassemble(options);
		} catch (CoreException e) {
			Assert.fail(e.getLocalizedMessage());
		}

		System.out.println(output.getMnemonics());
	}

	@Test
	public void testRet() {
		
		System.out.println("\n===================== ret ========================\n");

		disassembleInst(0x8124ffc, "c2 04 00", new JumpToAddress(JumpToAddress.EXPRESSION_RETURN_NEAR, true, false),
				"8124ffc:       c2 04 00                ret    $0x4", null);

		disassembleInst(0x8124ffc, "c3", new JumpToAddress(JumpToAddress.EXPRESSION_RETURN_NEAR, true, false),
				"8124ffc:       c3                      ret", null);

		disassembleInst(0x10000, "ca 0 10", new JumpToAddress(JumpToAddress.EXPRESSION_RETURN_FAR, true, false),
				"10000:       ca 00 10                  lret   $0x1000", null);

		disassembleInst(0x10000, "cb", new JumpToAddress(JumpToAddress.EXPRESSION_RETURN_FAR, true, false),
				"10000:       cb		                lret", null);
	}

	@Test
	public void testShift() {
		
		System.out.println("\n===================== shift ========================\n");

		String[] insts = { "c0 c0 02", "rol    $0x2,%al", "c0 e8 03", "shr    $0x3,%al", "c1 e2 04",
				"shl    $0x4,%edx", "c1 ff 1f", "sar    $0x1f,%edi", "d0 c0", "rol    %al", "d0 e8", "shr    %al",
				"d1 25 f0 02 06 08", "shll   0x80602f0", "d1 e9", "shr    %ecx", "d3 e0", "shl    %cl,%eax", "d3 ea",
				"shr    %cl,%edx", };

		disassembleInstArray(insts);
	}

	@Test
	public void testTest() {
		
		System.out.println("\n===================== test ========================\n");

		String[] insts = { "84 c0", "test   %al,%al", "85 c0", "test   %eax,%eax", "a8 05", "test   $0x5,%al",
				"a9 00 00 00 20", "test   $0x20000000,%eax", "f6 44 50 01 08", "testb  $0x8,0x1(%eax,%edx,2)",
				"f7 44 50 01 08 00 00 00", "testl  $0x8,0x1(%eax,%edx,2)", "66 f7 44 50 01 08 00",
				"testw  $0x8,0x1(%eax,%edx,2)", };

		disassembleInstArray(insts);
	}

	@Test
	public void testXchg() {
		
		System.out.println("\n===================== xchg ========================\n");

		String[] insts = { "87 05 00 10 00 00", "xchg   %eax,0x1000", "66 87 1d 00 10 00 00", "xchg   %bx,0x1000",
				"91", "xchg   %eax,%ecx", "66 91", "xchg   %ax,%cx", "66 90", "xchg   %ax,%ax", "93",
				"xchg   %eax,%ebx", };

		disassembleInstArray(insts);
	}

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

	private void disassembleInst(long address, String code, IJumpToAddress expectedJumpAddr, String expectedMnemonics,
			Map<String, Object> options) {

		// Use global option.
		if (options == null)
			options = sOptions;

		IAddress addr = new Addr64(Long.toString(address));
		ByteBuffer codeBuf = ByteBuffer.wrap(getByteArray(code));

		InstructionParserX86 disa = new InstructionParserX86(addr, codeBuf);

		IDisassembledInstruction output = null;
		try {
			output = disa.disassemble(options);
		} catch (CoreException e) {
			Assert.fail(e.getLocalizedMessage());
		}

		Assert.assertEquals(codeBuf.capacity(), output.getSize());
		Assert.assertEquals(address, output.getAddress().getValue().longValue());

		if (expectedJumpAddr != null) {
			Assert.assertNotNull(output.getJumpToAddress());
			Assert.assertEquals(expectedJumpAddr, output.getJumpToAddress());
		}

		if (expectedMnemonics != null) {
			String msg = "Mnemonics\n " + output.getMnemonics() + "\n not match expected\n " + expectedMnemonics;
			Assert
					.assertTrue(msg, TestUtils.stringCompare(expectedMnemonics, output.getMnemonics(), false, true,
							true));
		}

		System.out.println(output.getMnemonics());
	}

	/**
	 * Disassemble a block of code.
	 * 
	 * @param address
	 * @param code
	 * @param expectedAsm
	 */
	private void disassembleBlock(long address, String code, String expectedAsm) {
		IAddress addr = new Addr64(Long.toString(address));
		ByteBuffer codeBuf = ByteBuffer.wrap(getByteArray(code));

		List<IDisassembledInstruction> output = null;
		try {
			output = sDisassembler.disassembleInstructions(addr, addr.add(codeBuf.capacity()), codeBuf, sOptions);
		} catch (CoreException e) {
			Assert.fail(e.getLocalizedMessage());
		}

		int instSize = 0;
		StringBuffer asm = new StringBuffer();

		for (IDisassembledInstruction inst : output) {
			instSize += inst.getSize();

			if (expectedAsm != null)
				asm.append(inst.getMnemonics());

			System.out.println(inst.getMnemonics());
		}

		if (expectedAsm != null)
			Assert.assertEquals(expectedAsm, asm.toString());

		Assert.assertEquals(codeBuf.capacity(), instSize);
	}

	/**
	 * disassemble an array of instructions and verify the output.
	 * 
	 * @param insts
	 *            - array of strings in pairs of (bytes, asmCode) where the
	 *            "bytes" is code bytes and "asmCode" is the expected asm code
	 *            from the disassembler.
	 */
	private void disassembleInstArray(String[] insts) {
		if (insts.length % 2 != 0)
			throw new IllegalArgumentException();

		// Don't show address nor bytes
		Map<String, Object> options = new HashMap<String, Object>();

		int cnt = insts.length;

		for (int i = 0; i < cnt; i += 2) {
			disassembleInst(0 /* don't care */, insts[i], null /* don't care */, insts[i + 1], options);
		}
	}
}
