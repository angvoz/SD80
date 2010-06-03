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
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;

import java.text.MessageFormat;

import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.IField;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IReferenceType;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.debug.edc.symbols.VariableLocationFactory;
import org.eclipse.core.runtime.CoreException;

/*
 * Field reference instruction, such as "." or "->" 
 */
public class FieldReference extends CompoundInstruction {

	private final IASTFieldReference refExpression;

	/**
	 * Constructor for field reference instruction
	 * 
	 * @param expression
	 *            - field reference expression
	 * @param start
	 *            - instruction start
	 */
	public FieldReference(IASTFieldReference expression, int start) {
		super(start);
		this.refExpression = expression;
	}

	/**
	 * Resolve a field reference operator, such as "." or "->"
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() throws CoreException {
		// pop the structure variable at the start of the field references
		OperandValue operand = popValue();

		if (operand == null)
			return;

		IType variableType = TypeUtils.getStrippedType(operand.getValueType());

		IVariableLocation location = null;
		boolean referenceType = variableType instanceof IReferenceType;

		if (refExpression.isPointerDereference()) {
			// '->' operator requires a pointer type
			boolean validPointerType = variableType instanceof IPointerType;
			
			if (!validPointerType) {
				throw EDCDebugger.newCoreException(ASTEvalMessages.FieldReference_InvalidPointerDeref);
			}

			IPointerType pointer = (IPointerType) variableType;

			IType pointedTo = pointer.getType();
			variableType = TypeUtils.getStrippedType(pointedTo);
		} else if (referenceType) {
			// '.' may be used with a reference "&" type
			IReferenceType pointer = (IReferenceType) variableType;

			IType pointedTo = pointer.getType();
			variableType = TypeUtils.getStrippedType(pointedTo);
		}

		if (!TypeUtils.isCompositeType(variableType)) {
			throw EDCDebugger.newCoreException(ASTEvalMessages.FieldReference_InvalidDotDeref);
		}

		// get the field/member
		ICompositeType compositeType = (ICompositeType) variableType;
		String fieldName  = refExpression.getFieldName().toString();
		IField[] fields = compositeType.findFields(fieldName);

		if (fields == null) {
			throw EDCDebugger.newCoreException(
					MessageFormat.format(ASTEvalMessages.FieldReference_InvalidMember, fieldName));
		}
		
		if (fields.length > 1) {
			throw EDCDebugger.newCoreException(
					MessageFormat.format(ASTEvalMessages.FieldReference_AmbiguousMember, fieldName,
						operand.getValueType().getName()));
		}
		
		// type and address of the field
		IField field = fields[0];
		IType typeOfField = field.getType();

		if (   refExpression.isPointerDereference()
			|| (!refExpression.isPointerDereference() && referenceType)) {
			// pointer with '->' operator, or reference with '.' 
			location = VariableLocationFactory.createMemoryVariableLocation(
					fInterpreter.getServicesTracker(), fInterpreter.getContext(),
					operand.getValue());
		} else {
			// '.' operator
			location = operand.getValueLocation();
		}

		location = location.addOffset(field.getFieldOffset());
		
		OperandValue varValue = new OperandValue(typeOfField, field.getBitSize() > 0);

		typeOfField = TypeUtils.getStrippedType(typeOfField);

		// for lvalues (base arithmetic types, enums, and pointers), read the
		// value and cast it to the right type
		if (typeOfField instanceof ICPPBasicType || typeOfField instanceof IPointerType
				|| typeOfField instanceof IEnumeration) {
			int byteSize = typeOfField.getByteSize();

			// TODO support 12-byte long double
			if (byteSize != 1 && byteSize != 2 && byteSize != 4 && byteSize != 8 &&
				!(typeOfField instanceof IPointerType && byteSize == 0)) {
				throw EDCDebugger.newCoreException(ASTEvalMessages.FieldReference_UnhandledOperandSize + byteSize);
			}

			// read the value pointed to
			Number newValue = varValue.getValueByType(typeOfField, location);

			// if this is a bit-field, then mask and/or extend the value
			// appropriately
			// Note: only unnamed bit-fields have a 0 bit size, so a named field
			// with a 0 bit size is not a bit-field
			if (field.getBitSize() > 0) {
				int bitSize = field.getBitSize();
				int bitOffset = field.getBitOffset();
				boolean isSignedInt = false;

				if (typeOfField instanceof ICPPBasicType)
					isSignedInt = ((ICPPBasicType) typeOfField).isSigned();
				else if (typeOfField instanceof IEnumeration)
					isSignedInt = true;

				newValue = TypeUtils.extractBitField(newValue, byteSize, bitSize, bitOffset, isSignedInt);
			}
			varValue.setValue(newValue);
			varValue.setValueLocation(location);

		} else if (typeOfField instanceof IAggregate) {
			// for aggregates, the address of the aggregate is the value
			// returned
			varValue.setAddressValue(location);
			varValue.setValueLocation(location);

		} else {
			throw EDCDebugger.newCoreException(ASTEvalMessages.FieldReference_CannotDereferenceType);
		}

		push(varValue);
	}
}
