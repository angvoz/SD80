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
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;

import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.PointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.RegisterVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.core.runtime.CoreException;

/*
 * Unary address of operation "&"
 */
public class OperatorAddrOf extends CompoundInstruction {

	/**
	 * Constructor for address of operation "&"
	 * 
	 * @param start
	 *            - instruction start
	 */
	public OperatorAddrOf(int start) {
		super(start);
	}

	/**
	 * Resolve an address of operation "&"
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() throws CoreException {
		Object operand = popValue();

		if (operand == null)
			return;

		if (operand instanceof InvalidExpression) {
			push(operand);
			return;
		}

		// only allow address of a variable
		if (!(operand instanceof VariableWithValue) || ((VariableWithValue) operand).getVariable() == null) {
			InvalidExpression invalidExpression = new InvalidExpression(ASTEvalMessages.OperatorAddrOf_RequiresVariable);
			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return;
		}

		VariableWithValue variableWithValue = (VariableWithValue) operand;
		operand = variableWithValue.getValue();
		IType subType = variableWithValue.getVariable().getType();

		// do not allow a variable that is in a register
		if (variableWithValue.getValueLocation() instanceof RegisterVariableLocation) {
			InvalidExpression invalidExpression = new InvalidExpression(ASTEvalMessages.OperatorAddrOf_NoRegister);
			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return;
		}

		// do not allow a bit-field
		if (variableWithValue.isBitField()) {
			InvalidExpression invalidExpression = new InvalidExpression(ASTEvalMessages.OperatorAddrOf_NoBitField);
			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return;
		}

		PointerType pointer = new PointerType();
		pointer.setType(subType);

		setValueLocation(variableWithValue.getValueLocation());
		setValueType(pointer);
		push(operand);
	}

}
