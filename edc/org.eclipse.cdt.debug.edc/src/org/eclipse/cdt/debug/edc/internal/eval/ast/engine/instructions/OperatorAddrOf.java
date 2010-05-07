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

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
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
		OperandValue operand = popValue();

		// only allow address of an lvalue
		if (operand.getValueLocation() == null) {
			throw EDCDebugger.newCoreException(ASTEvalMessages.OperatorAddrOf_RequiresVariable);
		}

		IType subType = operand.getValueType();

		// do not allow a variable that is in a register
		if (operand.getValueLocation() instanceof RegisterVariableLocation) {
			throw EDCDebugger.newCoreException(ASTEvalMessages.OperatorAddrOf_NoRegister);
		}

		// do not allow a bit-field
		if (operand.isBitField()) {
			throw EDCDebugger.newCoreException(ASTEvalMessages.OperatorAddrOf_NoBitField);
		}

		PointerType pointer = new PointerType();
		pointer.setType(subType);

		OperandValue addr = new OperandValue(pointer);
		addr.setValueLocation(operand.getValueLocation());
		addr.setValue(operand.getValueLocationAddress());
		push(addr);
	}

}
