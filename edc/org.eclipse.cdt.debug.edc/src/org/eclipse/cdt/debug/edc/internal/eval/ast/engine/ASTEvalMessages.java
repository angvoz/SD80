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
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine;

import org.eclipse.osgi.util.NLS;

public class ASTEvalMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages"; //$NON-NLS-1$

	public static String DivideByZero;
	public static String ASTInstructionCompiler_InvalidNumber;
	public static String ArraySubscript_MustSubscriptArray;
	public static String ArraySubscript_SubscriptMustBeInteger;
	public static String EvaluateID_VariableNotFound;
	public static String FieldReference_InvalidPointerDeref;
	public static String FieldReference_InvalidDotDeref;
	public static String FieldReference_InvalidMember;
	public static String OperatorAddrOf_RequiresVariable;
	public static String OperatorAddrOf_NoRegister;
	public static String OperatorAddrOf_NoBitField;
	public static String OperatorIndirection_RequiresPointer;
	public static String OperatorIndirection_NoBitField;
	public static String OperatorMinus_NonPtrMinusPtr;
	public static String OperatorPlus_PtrPlusPtr;
	public static String VariableWithValue_ErrorReadingMemory; 
	public static String VariableWithValue_InvalidRegisterID; 
	public static String VariableWithValue_UnknownLocation;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ASTEvalMessages.class);
	}

	private ASTEvalMessages() {
	}
}
