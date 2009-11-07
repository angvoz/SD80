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

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.IBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.IType;
import org.eclipse.cdt.debug.edc.internal.symbols.ITypedef;
import org.eclipse.cdt.debug.edc.internal.symbols.TypeUtils;
import org.eclipse.cdt.debug.edc.internal.symbols.Variable;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;

/*
 * Array subscript instruction
 */
public class ArraySubscript extends CompoundInstruction {

	static final Addr64 Addr64Zero = new Addr64("0"); //$NON-NLS-1$

	/**
	 * Constructor for array subscript instruction
	 * 
	 * @param expression
	 *            - array subscript expression
	 * @param start
	 */
	public ArraySubscript(int start) {
		super(start);
	}

	/**
	 * Resolve an array subscript expression
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() throws CoreException {
		Object subscriptOperand = popValue();
		Object variableOperand = popValue();

		if (subscriptOperand == null || variableOperand == null)
			return;

		if (subscriptOperand instanceof InvalidExpression) {
			push(subscriptOperand);
			return;
		}

		if (variableOperand instanceof InvalidExpression) {
			push(variableOperand);
			return;
		}

		if (!(variableOperand instanceof VariableWithValue)) {
			InvalidExpression invalidExpression = new InvalidExpression(
					ASTEvalMessages.ArraySubscript_MustSubscriptArray);
			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return;
		}

		long subscript = 0;
		boolean integerSubscript = true;

		if (subscriptOperand instanceof VariableWithValue) {
			subscriptOperand = ((VariableWithValue) subscriptOperand).getValue();
		}

		if (subscriptOperand instanceof BigInteger)
			subscript = ((BigInteger) subscriptOperand).longValue();
		else if (subscriptOperand instanceof Long)
			subscript = ((Long) subscriptOperand);
		else if (subscriptOperand instanceof Integer)
			subscript = ((Integer) subscriptOperand);
		else if (subscriptOperand instanceof Short)
			subscript = ((Short) subscriptOperand);
		else if (subscriptOperand instanceof Character)
			subscript = ((Character) subscriptOperand);
		else
			integerSubscript = false;

		if (!integerSubscript) {
			InvalidExpression invalidExpression = new InvalidExpression(
					ASTEvalMessages.ArraySubscript_SubscriptMustBeInteger);
			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return;
		}

		VariableWithValue variableWithValue = (VariableWithValue) variableOperand;
		IType variableType = TypeUtils.getUnqualifiedType(variableWithValue.getVariable().getType());

		IArrayType arrayType;
		Addr64 location = null;
		IType arrayElementType;
		int byteSize;

		// if the variable is an IArrayType, there are two cases:
		// we're accessing a single element of a one dimensional array
		// we're accessing an entire dimension of a multidimensional array
		if (variableType instanceof IArrayType) {

			arrayType = (IArrayType) variableType;

			if (arrayType.getBoundsCount() == 0) {
				push(new Long(0));
				return;
			}

			// find the location of indexed 1st dimension element, or of entire
			// dimension
			arrayElementType = TypeUtils.getUnqualifiedType(arrayType.getType());

			byteSize = arrayElementType.getByteSize();
			if (arrayElementType instanceof ITypedef) {
				byteSize = TypeUtils.getUnqualifiedType(arrayElementType.getType()).getByteSize();
			}

			if (variableWithValue.getValueLocation() instanceof IAddress) {
				IAddress varLocation = (IAddress) variableWithValue.getValueLocation();

				if (varLocation instanceof Addr64) {
					location = (Addr64) variableWithValue.getValueLocation();

					if (location instanceof Addr64)
						location = (Addr64) location.add(arrayType.getBounds()[0].getElementCount() * byteSize
								* subscript);
					else
						location = Addr64Zero;
				} else {
					location = Addr64Zero;
				}
			}

			if (arrayType.getBoundsCount() == 1) {
				// we're accessing a single element of a one dimensional array
				pushArrayElement(variableWithValue, location, arrayElementType);
			} else {
				String name = variableWithValue.getVariable().getName() + "[" + subscript + "]"; //$NON-NLS-1$ //$NON-NLS-2$
				ArrayDimensionType arrayDimensionType = new ArrayDimensionType(name, variableWithValue, arrayType,
						location);
				Variable variable = new Variable(name, variableWithValue.getVariable().getScope(), arrayDimensionType,
						null);
				VariableWithValue varValue = new VariableWithValue(variableWithValue.getServicesTracker(),
						variableWithValue.getFrame(), variable);

				varValue.setValue(location);
				varValue.setValueLocation(location);
				setValueLocation(location);
				setValueType(arrayDimensionType);
				push(varValue);
			}
			return;
		}

		if (!(variableType instanceof IArrayDimensionType)) {
			InvalidExpression invalidExpression = new InvalidExpression(
					ASTEvalMessages.ArraySubscript_MustSubscriptArray);
			push(invalidExpression);
			setLastValue(invalidExpression);
			setValueLocation(""); //$NON-NLS-1$
			setValueType(""); //$NON-NLS-1$
			return;
		}

		// if the variable is an ArrayDimensionType, there are two cases:
		// we're accessing a single element of a multidimensional array
		// we're accessing another entire dimension of a multidimensional array
		ArrayDimensionType arrayDimension = (ArrayDimensionType) variableType;
		arrayType = arrayDimension.getArrayType();
		arrayElementType = TypeUtils.getUnqualifiedType(arrayType.getType());

		byteSize = arrayElementType.getByteSize();
		if (arrayElementType instanceof ITypedef) {
			byteSize = TypeUtils.getUnqualifiedType(arrayElementType.getType()).getByteSize();
		}

		arrayDimension.addDimension(subscript, arrayType.getBound(arrayDimension.getDimensionCount()).getElementCount()
				* byteSize * subscript);
		location = arrayDimension.getLocation();

		if (arrayDimension.getDimensionCount() >= arrayType.getBoundsCount()) {
			// we're accessing a single element of a multidimensional array
			pushArrayElement(arrayDimension.getVariableWithValue(), location, arrayElementType);
		} else {
			// we're accessing another entire dimension of a multidimensional
			// array
			variableWithValue.setValue(location);
			variableWithValue.setValueLocation(location);
			setValueLocation(location);
			push(variableWithValue);
		}
	}

	private void pushArrayElement(VariableWithValue originalVariableValue, Addr64 location, IType arrayElementType) {
		// create a skeletal VariableWithValue for the result
		Variable variable = new Variable("", originalVariableValue.getVariable().getScope(), arrayElementType, null); //$NON-NLS-1$
		VariableWithValue varValue = new VariableWithValue(originalVariableValue.getServicesTracker(),
				originalVariableValue.getFrame(), variable);

		varValue.setValueLocation(location);
		setValueLocation(location);
		setValueType(arrayElementType);

		if (arrayElementType instanceof ITypedef)
			arrayElementType = TypeUtils.getUnqualifiedType(arrayElementType.getType());

		// for a lvalues (base arithmetic types, enums, and pointers), read the
		// value and cast it to the right type
		if (arrayElementType instanceof IBasicType || arrayElementType instanceof IEnumeration
				|| arrayElementType instanceof IPointerType) {
			int byteSize = arrayElementType.getByteSize();

			// TODO support 12-byte long double
			if (byteSize != 1 && byteSize != 2 && byteSize != 4 && byteSize != 8) {
				pushNewValue(new Long(0));
				return;
			}

			// read the value pointed to
			Object newValue = originalVariableValue.getValueByType(arrayElementType, location);
			varValue.setValue(newValue);
			push(varValue);

		} else if (arrayElementType instanceof IAggregate) {
			// for aggregates, the address of the aggregate is the value
			// returned
			varValue.setValue(location);
			push(varValue);

		} else {
			pushNewValue(new Long(0));
		}
	}

}
