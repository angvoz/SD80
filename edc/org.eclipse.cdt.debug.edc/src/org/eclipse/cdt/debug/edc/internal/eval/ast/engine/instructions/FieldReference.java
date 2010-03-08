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

import java.math.BigInteger;
import java.text.MessageFormat;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompositeType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.IField;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IReferenceType;
import org.eclipse.cdt.debug.edc.internal.symbols.Variable;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.utils.Addr64;
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
		Object operand = popValue();

		if (operand == null)
			return;

		if (operand instanceof InvalidExpression) {
			push(operand);
			return;
		}

		if (!(operand instanceof VariableWithValue) || ((VariableWithValue) operand).getVariable() == null) {
			push(new Long(0));
			return;
		}

		VariableWithValue variableWithValue = (VariableWithValue) operand;
		IType variableType = TypeUtils.getStrippedType(variableWithValue.getVariable().getType());

		Object location = null;

		boolean referenceType = variableType instanceof IReferenceType;

		if (refExpression.isPointerDereference()) {
			// '->' operator requires a pointer type
			boolean validPointerType = variableType instanceof IPointerType;
			
			if (validPointerType) {
				Object value = variableWithValue.getValue();
				
				if (value instanceof Long)
					variableWithValue.setValue(new BigInteger(((Long) value).toString()));
				else if (value instanceof Integer)
					variableWithValue.setValue(new BigInteger(((Integer) value).toString()));
			
				validPointerType = variableWithValue.getValue() instanceof BigInteger ||
								variableWithValue.getValue() instanceof IAddress;
			}
			
			if (!validPointerType) {
				IInvalidExpression invalidExpression = null;
				if (variableWithValue.getValue() instanceof IInvalidExpression)
					invalidExpression = (IInvalidExpression) variableWithValue.getValue();
				else
					invalidExpression = new InvalidExpression(
						ASTEvalMessages.FieldReference_InvalidPointerDeref);
				push(invalidExpression);
				setLastValue(invalidExpression);
				setValueLocation(""); //$NON-NLS-1$
				setValueType(""); //$NON-NLS-1$
				return;
			}

			IPointerType pointer = (IPointerType) variableType;

			IType pointedTo = pointer.getType();
			variableType = TypeUtils.getStrippedType(pointedTo);
		} else if (referenceType) {
			// '.' may be used with a reference "&" type
			Object value = variableWithValue.getValue();
			
			if (value instanceof Long)
				variableWithValue.setValue(new BigInteger(((Long) value).toString()));
			else if (value instanceof Integer)
				variableWithValue.setValue(new BigInteger(((Integer) value).toString()));
			
			if (   !(variableWithValue.getValue() instanceof BigInteger)
				&& !(variableWithValue.getValue() instanceof IAddress)) {
				IInvalidExpression invalidExpression = null;
				if (variableWithValue.getValue() instanceof IInvalidExpression)
					invalidExpression = (IInvalidExpression) variableWithValue.getValue();
				else
					invalidExpression = new InvalidExpression(
						ASTEvalMessages.FieldReference_InvalidDotDeref);
				push(invalidExpression);
				setLastValue(invalidExpression);
				setValueLocation(""); //$NON-NLS-1$
				setValueType(""); //$NON-NLS-1$
				return;
			}

			IReferenceType pointer = (IReferenceType) variableType;

			IType pointedTo = pointer.getType();
			variableType = TypeUtils.getStrippedType(pointedTo);
		}

		if (!TypeUtils.isCompositeType(variableType)) {
			InvalidExpression invalidExpression = new InvalidExpression(
					ASTEvalMessages.FieldReference_InvalidDotDeref);
			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return;
		}

		// get the field/member
		ICompositeType compositeType = (ICompositeType) variableType;
		String fieldName  = refExpression.getFieldName().toString();
		IField[] fields = compositeType.findFields(fieldName);

		if (fields == null) {
			InvalidExpression invalidExpression =
				new InvalidExpression(MessageFormat.format(ASTEvalMessages.FieldReference_InvalidMember, fieldName));
			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return;
		}
		
		if (fields.length > 1) {
			InvalidExpression invalidExpression =
				new InvalidExpression(MessageFormat.format(
						ASTEvalMessages.FieldReference_AmbiguousMember, fieldName,
						variableWithValue.getVariable().getName()));
			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return;
		}
		
		// type and address of the field
		IField field = fields[0];
		IType typeOfField = field.getType();

		if (   refExpression.isPointerDereference()
			|| (!refExpression.isPointerDereference() && referenceType)) {
			// pointer with '->' operator, or reference with '.' 
			if (variableWithValue.getValue() instanceof BigInteger) {
				location = new Addr64((BigInteger) variableWithValue.getValue());
			} else {
				location = variableWithValue.getValue();
			}
		} else {
			// '.' operator
			location = variableWithValue.getValueLocation();
		}

		if (location instanceof IAddress)
			location = ((IAddress) location).add(field.getFieldOffset());
		else if (location instanceof IVariableLocation)
			location = ((IVariableLocation) location).addOffset(field.getFieldOffset());
		else
			assert(false);
		
		setValueType(typeOfField);
		setValueLocation(location);

		// create a skeletal VariableWithValue for the result
		Variable variable = new Variable(field.getName(), variableWithValue.getVariable().getScope(), typeOfField, null);
		VariableWithValue varValue = new VariableWithValue(variableWithValue.getServicesTracker(), variableWithValue
				.getFrame(), variable, field.getBitSize() > 0);

		typeOfField = TypeUtils.getStrippedType(typeOfField);

		// for lvalues (base arithmetic types, enums, and pointers), read the
		// value and cast it to the right type
		if (typeOfField instanceof ICPPBasicType || typeOfField instanceof IPointerType
				|| typeOfField instanceof IEnumeration) {
			int byteSize = typeOfField.getByteSize();

			// TODO support 12-byte long double
			if (byteSize != 1 && byteSize != 2 && byteSize != 4 && byteSize != 8 &&
				!(typeOfField instanceof IPointerType && byteSize == 0)) {
				pushNewValue(new Long(0));
				return;
			}

			// read the value pointed to
			Object newValue = varValue.getValueByType(typeOfField, location);

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
			varValue.setValue(location);
			varValue.setValueLocation(location);

		} else {
			pushNewValue(new Long(0));
			return;
		}

		push(varValue);
	}
}
