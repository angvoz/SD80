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

import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.IField;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IType;
import org.eclipse.cdt.debug.edc.internal.symbols.ITypedef;
import org.eclipse.cdt.debug.edc.internal.symbols.TypeUtils;
import org.eclipse.cdt.debug.edc.internal.symbols.Variable;
import org.eclipse.cdt.utils.Addr64;
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
		Object operand = popValue();

		if (operand == null)
			return;

		if (operand instanceof InvalidExpression) {
			push(operand);
			return;
		}

		// only allow indirection of pointers to base types, pointers, and
		// structured types
		if (!(operand instanceof VariableWithValue) || ((VariableWithValue) operand).getVariable() == null) {
			InvalidExpression invalidExpression = new InvalidExpression(
					ASTEvalMessages.OperatorIndirection_RequiresPointer);
			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return;
		}

		VariableWithValue variableWithValue = (VariableWithValue) operand;

		Object opType = TypeUtils.getStrippedType(variableWithValue.getVariable().getType());

		if (!TypeUtils.isPointerType(opType) || !(variableWithValue.getValue() instanceof BigInteger)) {
			IInvalidExpression invalidExpression = null;
			if (variableWithValue.getValue() instanceof IInvalidExpression)
				invalidExpression = (IInvalidExpression) variableWithValue.getValue();
			else
				invalidExpression = new InvalidExpression(
					ASTEvalMessages.OperatorIndirection_RequiresPointer);

			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return;
		}

		IPointerType pointer = (IPointerType) opType;

		IType pointedTo = pointer.getType();
		IType unqualifiedPointedTo = TypeUtils.getStrippedType(pointedTo);

		// do not allow a pointer to a bit-field
		if ((unqualifiedPointedTo instanceof IField) && (((IField) unqualifiedPointedTo).getBitSize() != 0)) {
			InvalidExpression invalidExpression = new InvalidExpression(ASTEvalMessages.OperatorIndirection_NoBitField);
			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return;
		}

		setValueType(unqualifiedPointedTo);
		setValueLocation(new Long(0));

		// create a skeletal VariableWithValue for the result
		Variable variable = new Variable("", variableWithValue.getVariable().getScope(), unqualifiedPointedTo, null); //$NON-NLS-1$
		VariableWithValue varValue = new VariableWithValue(variableWithValue.getServicesTracker(), variableWithValue
				.getFrame(), variable);

		if (unqualifiedPointedTo instanceof ITypedef)
			unqualifiedPointedTo = TypeUtils.getStrippedType(unqualifiedPointedTo.getType());

		// for a lvalues (base arithmetic types, enums, and pointers), read the
		// value and cast it to the right type
		if (unqualifiedPointedTo instanceof ICPPBasicType || unqualifiedPointedTo instanceof IPointerType
				|| unqualifiedPointedTo instanceof IEnumeration) {
			int byteSize = unqualifiedPointedTo.getByteSize();

			if (byteSize != 1 && byteSize != 2 && byteSize != 4 && byteSize != 8) {
				pushNewValue(new Long(0));
				return;
			}

			// read the value pointed to
			Addr64 location = new Addr64((BigInteger) variableWithValue.getValue());
			Object newValue = variableWithValue.getValueByType(unqualifiedPointedTo, location);
			varValue.setValue(newValue);
			varValue.setValueLocation(location);
			setValueLocation(location);
			push(varValue);

		} else if (unqualifiedPointedTo instanceof IAggregate) {
			// for aggregates, the address of the aggregate is the value
			// returned
			Addr64 location = new Addr64((BigInteger) variableWithValue.getValue());
			varValue.setValue(location);
			varValue.setValueLocation(location);
			setValueLocation(location);
			push(varValue);

		} else {
			pushNewValue(new Long(0));
		}
	}

	/**
	 * Convert certain operand types to expected types (e.g., BigInteger to
	 * long)
	 * 
	 * @param operand
	 *            - original operand
	 * @return result operand type
	 */

	@Override
	protected Object convertForPromotion(Object operand) {
		if (operand instanceof VariableWithValue) {
			VariableWithValue variableWithValue = (VariableWithValue) operand;
			IType nextType = null;
			for (nextType = variableWithValue.getVariable().getType(); nextType != null; nextType = nextType.getType()) {
				if (nextType instanceof IPointerType)
					return nextType;
			}

			if (TypeUtils.isAggregateType(variableWithValue.getVariable().getType())) {
				operand = variableWithValue.getValueLocation();
				if (operand instanceof Addr64)
					operand = ((Addr64) operand).getValue().longValue();
			} else
				operand = variableWithValue.getValue();
		}

		if (operand instanceof BigInteger)
			operand = ((BigInteger) operand).longValue();

		return operand;
	}

}
