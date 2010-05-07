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

import java.text.MessageFormat;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.IField;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.symbols.IMemoryVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.debug.edc.symbols.VariableLocationFactory;
import org.eclipse.core.runtime.CoreException;

/*
 * Unary indirection operation "*"
 */
public class OperatorIndirection extends CompoundInstruction {

	/**
	 * Constructor for a unary indirection operation "*"
	 * 
	 * @param start
	 *            - instruction start
	 */
	public OperatorIndirection(int start) {
		super(start);
	}

	/**
	 * Resolve a unary indirection expression
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() throws CoreException {
		OperandValue operand = popValue();

		IType opType = TypeUtils.getStrippedType(operand.getValueType());
		
		if (operand.getStringValue() != null) {
			// read first char of a string constant
			pushNewValue(opType.getType(), (int) operand.getStringValue().charAt(0));
			return;
		}
		
		if (!TypeUtils.isPointerType(opType)) {
			throw EDCDebugger.newCoreException(ASTEvalMessages.OperatorIndirection_RequiresPointer);
		}

		IPointerType pointer = (IPointerType) opType;
		IType pointedTo = pointer.getType();
		IType unqualifiedPointedTo = TypeUtils.getStrippedType(pointedTo);

		// do not allow a pointer to a bit-field
		if ((unqualifiedPointedTo instanceof IField) && (((IField) unqualifiedPointedTo).getBitSize() != 0)) {
			throw EDCDebugger.newCoreException(ASTEvalMessages.OperatorIndirection_NoBitField);
		}

		OperandValue opValue = new OperandValue(unqualifiedPointedTo);
		
		// for a lvalues (base arithmetic types, enums, and pointers), read the
		// value and cast it to the right type
		IMemoryVariableLocation location = VariableLocationFactory.createMemoryVariableLocation(
				fInterpreter.getServicesTracker(), fInterpreter.getContext(),
				operand.getValue());
		
		if (unqualifiedPointedTo instanceof ICPPBasicType || unqualifiedPointedTo instanceof IPointerType
				|| unqualifiedPointedTo instanceof IEnumeration) {
			int byteSize = unqualifiedPointedTo.getByteSize();
			
			// treat ICPPBasicType of byte size 0 as a void pointer (size 4)
			if (unqualifiedPointedTo instanceof ICPPBasicType && byteSize == 0)
				byteSize = 4;

			if (byteSize != 1 && byteSize != 2 && byteSize != 4 && byteSize != 8) {
				throw EDCDebugger.newCoreException(MessageFormat.format(ASTEvalMessages.UnhandledSize, byteSize));
			}

			// read the value pointed to
			Number newValue = operand.getValueByType(unqualifiedPointedTo, location);
			opValue.setValue(newValue);
			opValue.setValueLocation(location);
			push(opValue);

		} else if (unqualifiedPointedTo instanceof IAggregate) {
			// for aggregates, the address of the aggregate is the value
			// returned
			opValue.setAddressValue(location);
			opValue.setValueLocation(location);
			push(opValue);

		} else {
			throw EDCDebugger.newCoreException(MessageFormat.format(ASTEvalMessages.OperatorIndirection_UnhandledType, 
					unqualifiedPointedTo != null ? unqualifiedPointedTo.getName() : "null"));
		}
	}
}
