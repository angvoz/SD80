/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine;

import org.eclipse.osgi.util.NLS;

public class ASTEvalMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages"; //$NON-NLS-1$

	public static String DivideByZero;
	public static String UnhandledTypeCode;
	public static String UnhandledSize;
	public static String UnsupportedStringOperation;

	public static String ASTEvaluationEngine_DidNotDetectType;
	
	public static String ASTInstructionCompiler_InvalidNumber;

	public static String ArraySubscript_ArrayHasNoBounds;
	public static String ArraySubscript_CannotIndirectTemporary;
	public static String ArraySubscript_ErrorDereferencingArray;
	public static String ArraySubscript_MustSubscriptArray;
	public static String ArraySubscript_ReadingPastEndOfString;
	public static String ArraySubscript_SubscriptMustBeInteger;

	public static String EvaluateID_CannotResolveName;
	public static String EvaluateID_NameHasNoLocation;
	public static String EvaluateID_VariableNotFound;

	public static String FieldReference_InvalidPointerDeref;
	public static String FieldReference_InvalidDotDeref;
	public static String FieldReference_InvalidMember;
	public static String FieldReference_AmbiguousMember;
	public static String FieldReference_CannotDereferenceType;
	public static String FieldReference_UnhandledOperandSize;

	public static String GetValue_TypePromotionError;

	public static String Instruction_CannotUseCompositeType;
	public static String Instruction_EmptyStack;
	public static String Instruction_UnhandledTypeCombination;

	public static String OperandValue_CannotGetAddress;
	public static String OperandValue_CannotReadUnspecifiedType;
	public static String OperandValue_CannotReadVoid;
	public static String OperandValue_UnhandledType;
	public static String OperandValue_VariableNoAddress;

	public static String OperatorAddrOf_RequiresVariable;
	public static String OperatorAddrOf_NoRegister;
	public static String OperatorAddrOf_NoBitField;

	public static String OperatorCast_CannotCastString;

	public static String OperatorIndirection_RequiresPointer;
	public static String OperatorIndirection_NoBitField;
	public static String OperatorIndirection_NoFunction;
	public static String OperatorIndirection_UnhandledType;

	public static String OperatorMinus_NonPtrMinusPtr;
	public static String OperatorPlus_PtrPlusPtr;

	public static String VariableWithValue_CannotLocateVariable;
	public static String VariableWithValue_NoTwelveByteLongDouble; 
	public static String VariableWithValue_UnhandledType;
	public static String VariableWithValue_UnknownLocation;
	public static String VariableWithValue_VariableHasNoType;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ASTEvalMessages.class);
	}

	private ASTEvalMessages() {
	}
}
