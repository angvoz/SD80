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
package org.eclipse.cdt.debug.edc.debugger.tests;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.services.dsf.EDCServicesMessages;
import org.junit.Assert;
import org.junit.Test;

public class ExpressionsInvalidExpressions extends SimpleDebuggerTest {

	@Test
	public void testExpressionsInvalid() throws Exception {
		openSnapshotAndWaitForSuspendedContext(0);

		// badly formed expressions
		Assert.assertEquals(getExpressionValue("1 + 6)"), EDCServicesMessages.Expressions_SyntaxError);
		Assert.assertEquals(getExpressionValue("(1 + 7 * 9)) + 6"), EDCServicesMessages.Expressions_SyntaxError);
		Assert.assertEquals(getExpressionValue("78 +/ 87"), EDCServicesMessages.Expressions_SyntaxError);
		// invalid number formats
		Assert.assertEquals(getExpressionValue("10 + 7g6"),
				ASTEvalMessages.ASTInstructionCompiler_InvalidNumber);
		Assert.assertEquals(getExpressionValue("0x67t9"),
				ASTEvalMessages.ASTInstructionCompiler_InvalidNumber);
		Assert.assertEquals(getExpressionValue("6 + 10E"),
				ASTEvalMessages.ASTInstructionCompiler_InvalidNumber);
		Assert.assertEquals(getExpressionValue("1.6E.1"),
				ASTEvalMessages.ASTInstructionCompiler_InvalidNumber);
		Assert.assertEquals(getExpressionValue("'\\u09T'"),
				ASTEvalMessages.ASTInstructionCompiler_InvalidNumber);
		Assert.assertEquals(getExpressionValue("'\\768'"),
				ASTEvalMessages.ASTInstructionCompiler_InvalidNumber);
		Assert.assertEquals(getExpressionValue("'\\1234'"),
				ASTEvalMessages.ASTInstructionCompiler_InvalidNumber);
		// non-existent variables
		Assert.assertEquals(getExpressionValue("10 * 65 + jajaja - 12 * 76"),
				MessageFormat.format(ASTEvalMessages.EvaluateID_VariableNotFound, "jajaja"));
	}

	/*
	 *	Note: This assumes you are at a breakpoint where:
	 *	typedef struct {
	 *		char achar;
	 *		UCHAR auchar;
	 *		SCHAR aschar;
	 *		short ashort;
	 *		USHORT aushort;
	 *		SSHORT asshort;
	 *		int aint;
	 *		UINT auint;
	 *		SINT asint;
	 *		long along;
	 *		ULONG aulong;
	 *		SLONG aslong;
	 *		ULONGLONG aulonglong;
	 *		SLONGLONG aslonglong;
	 *		float afloat;
	 *		double adouble;
	 *	} struct_type;
	 *
	 *	struct_type lstruct;
	 *
	 *	lstruct.achar = '1';
	 *	lstruct.auchar = 2;
	 *	lstruct.aschar = '3';
	 *	lstruct.ashort = 4;
	 *	lstruct.aushort = 5;
	 *	lstruct.asshort = 6;
	 *	lstruct.aint = 7;
	 *	lstruct.auint = 8;
	 *	lstruct.asint = 9;
	 *	lstruct.along = 10;
	 *	lstruct.aulong = 11;
	 *	lstruct.aslong = 12;
	 *	lstruct.aulonglong = 13;
	 *	lstruct.aslonglong = 14;
	 *	lstruct.afloat = 15.0;
	 *	lstruct.adouble = 16.0;
	 */
	@Test
	public void testExpressionsInvalidStructs() throws Exception {
		openSnapshotAndWaitForSuspendedContext(0);

		// trying to use struct as composite pointer
		Assert.assertEquals(getExpressionValue("lstruct->achar"),
				ASTEvalMessages.FieldReference_InvalidPointerDeref);
		// non-existent members
		Assert.assertEquals(getExpressionValue("lstruct.bad"),
				MessageFormat.format(ASTEvalMessages.FieldReference_InvalidMember, "bad"));
	}

	/*
	 * Note: This assumes you are at a breakpoint where:
	 * 
	 * int larray[40];
	 * 
	 * larray[0] = 40;
	 * larray[1] = 39;
	 * larray[2] = 38;
	 * ....
	 * larray[37] = 3;
	 * larray[38] = 2;
	 * larray[39] = 1;
	 */
	@Test
	public void testExpressionsInvalidArrays() throws Exception {
		openSnapshotAndWaitForSuspendedContext(1);

		// trying to use array as struct/class
		Assert.assertEquals(getExpressionValue("larray.member"),
				ASTEvalMessages.FieldReference_InvalidDotDeref);
		Assert.assertEquals(getExpressionValue("larray[1].member"),
				ASTEvalMessages.FieldReference_InvalidDotDeref);
		// subscripting
		Assert.assertEquals(getExpressionValue("larray[1.0]"),
				ASTEvalMessages.ArraySubscript_SubscriptMustBeInteger);
	}

	/*
	 *	Note: This assumes you are at a breakpoint where:
	 *
	 *	typedef union {
	 *		volatile int x;
	 *		volatile long y;
	 *	} union_type;
	 *
	 *	union_type lunion;
	 *
	 *	lunion.x = 2;
	 *	lunion.y = 2;
	 */
	@Test
	public void testExpressionsInvalidUnions() throws Exception {
		openSnapshotAndWaitForSuspendedContext(2);

		// non-existent members
		Assert.assertEquals(getExpressionValue("lunion.bad"),
				MessageFormat.format(ASTEvalMessages.FieldReference_InvalidMember, "bad"));
		// subscripting
		Assert.assertEquals(getExpressionValue("(lunion.x)[6]"),
				ASTEvalMessages.ArraySubscript_MustSubscriptArray);
		// unary '*'
		Assert.assertEquals(getExpressionValue("*(lunion.x)"),
				ASTEvalMessages.OperatorIndirection_RequiresPointer);
	}

	/*
	 *	Note: This assumes you are at a breakpoint where:
	 *
	 *	typedef struct {
	 *		volatile unsigned x:1;
	 *		volatile unsigned y:2;
	 *		volatile unsigned z:3;
	 *		volatile unsigned w:16;
	 *	} bitfield_type;
	 *
	 *	bitfield_type lbitfield;
	 *
	 *	lbitfield.x = 1;
	 *	lbitfield.y = 2;
	 *	lbitfield.z = 3;
	 *	lbitfield.w = 26;
	 */
	@Test
	public void testExpressionsInvalidBitfields() throws Exception {
		openSnapshotAndWaitForSuspendedContext(3);

		// non-existent members
		Assert.assertEquals(getExpressionValue("lbitfield.bad"),
				MessageFormat.format(ASTEvalMessages.FieldReference_InvalidMember, "bad"));
		Assert.assertEquals(getExpressionValue("lbitfield.x.bad"),
				ASTEvalMessages.FieldReference_InvalidDotDeref);
		// address of & applied to a non-variable or bit-field
		Assert.assertEquals(getExpressionValue("&0x123456"),
				ASTEvalMessages.OperatorAddrOf_RequiresVariable);
		Assert.assertEquals(getExpressionValue("&(lbitfield.x)"),
				ASTEvalMessages.OperatorAddrOf_NoBitField);
	}

	@Override
	public String getAlbumName() {
		return "ExpressionsAggregatesAndEnums.dsa";
	}

}
