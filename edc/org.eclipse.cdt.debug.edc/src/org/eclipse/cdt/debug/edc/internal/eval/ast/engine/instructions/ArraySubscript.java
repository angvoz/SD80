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

import java.math.BigInteger;
import java.text.MessageFormat;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvalMessages;
import org.eclipse.cdt.debug.edc.internal.symbols.IAggregate;
import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.IBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.IEnumeration;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.internal.symbols.ITypedef;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.debug.edc.symbols.VariableLocationFactory;
import org.eclipse.core.runtime.CoreException;

/*
 * Array subscript instruction
 */
public class ArraySubscript extends CompoundInstruction {

	/**
	 * Constructor for array subscript instruction
	 * 
	 * @param expression
	 *            - array subscript expression
	 * @param start
	 *            - instruction start
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
		OperandValue subscriptOperand = popValue();
		OperandValue variableOperand = popValue();

		long subscript = 0;

		if (subscriptOperand.isFloating())
			throw EDCDebugger.newCoreException(ASTEvalMessages.ArraySubscript_SubscriptMustBeInteger);
		
		subscript = subscriptOperand.getLongValue();

		IType variableType = TypeUtils.getStrippedType(variableOperand.getValueType());

		IArrayType arrayType;
		IVariableLocation location = null;
		IType arrayElementType;
		int byteSize;

		if (variableType instanceof IArrayDimensionType)
			arrayElementType = TypeUtils.getStrippedType(((IArrayDimensionType) variableType).getArrayType().getType());
		else
			arrayElementType = TypeUtils.getStrippedType(variableType.getType());
		if (arrayElementType == null)
			throw EDCDebugger.newCoreException(ASTEvalMessages.ArraySubscript_MustSubscriptArray);
		
		if (variableType instanceof IArrayType)
			byteSize = variableType.getByteSize();
		else
			byteSize = arrayElementType.getByteSize();
			

		IVariableLocation varLocation = variableOperand.getValueLocation();
		if (varLocation == null) {
			// may be a string...
			String stringValue = variableOperand.getStringValue();
			if (stringValue != null) {
				if (subscript < stringValue.length()) {
					pushNewValue(arrayElementType, (int) stringValue.charAt((int) subscript));
				} else if (subscript == stringValue.length()) {
					pushNewValue(arrayElementType, 0);
				} else {
					throw EDCDebugger.newCoreException(ASTEvalMessages.ArraySubscript_ReadingPastEndOfString);
				}
				return;
			}
			throw EDCDebugger.newCoreException(ASTEvalMessages.ArraySubscript_CannotIndirectTemporary);
		}

		// If the variable type is just a pointer, then add the pointer base type's size
		//
		// *(ptr+element)
		if (variableType instanceof IPointerType) {
			IPointerType pointerType = (IPointerType) variableType;
			
			// dereference ptr
			BigInteger ptrValue = varLocation.readValue(pointerType.getByteSize());
			
			// point into array
			location = VariableLocationFactory.createMemoryVariableLocation(
					fInterpreter.getServicesTracker(), fInterpreter.getContext(), 
					ptrValue.add(BigInteger.valueOf(byteSize * subscript)));

			// dereference to fetch offset 
			OperandValue op = new OperandValue(pointerType.getType());
			
			op.setValueLocation(VariableLocationFactory.createMemoryVariableLocation(
					fInterpreter.getServicesTracker(), fInterpreter.getContext(), 
					location.getAddress().getValue()));
			
			// read actual value
			Number newValue = op.getValueByType(op.getValueType(), op.getValueLocation());
			op.setValue(newValue);
			push(op);
			
			return;

		}
		
		// if the variable is an IArrayType, there are two cases:
		//   we're accessing a single element of a one dimensional array
		//   we're accessing an entire dimension of a multidimensional array
		if (variableType instanceof IArrayType) {

			arrayType = (IArrayType) variableType;

			if (arrayType.getBoundsCount() == 0) {
				throw EDCDebugger.newCoreException(ASTEvalMessages.ArraySubscript_ArrayHasNoBounds);
			}

			// find the location of indexed 1st dimension element, or of entire
			// dimension
			location = varLocation.addOffset(arrayType.getBounds()[0].getElementCount() * byteSize
							* subscript);

			if (arrayType.getBoundsCount() == 1) {
				// we're accessing a single element of a one dimensional array
				pushArrayElement(variableOperand, location, arrayElementType);
			} else {
				String name = variableOperand.getValueType().getName() + "[" + subscript + "]"; //$NON-NLS-1$ //$NON-NLS-2$
				ArrayDimensionType arrayDimensionType = new ArrayDimensionType(name, variableOperand, arrayType,
						location);
				OperandValue opValue = new OperandValue(arrayDimensionType);

				opValue.setAddressValue(location);
				opValue.setValueLocation(location);
				push(opValue);
			}
			return;
		}

		if (!(variableType instanceof IArrayDimensionType)) {
			throw EDCDebugger.newCoreException(ASTEvalMessages.ArraySubscript_MustSubscriptArray);
		}

		// if the variable is an ArrayDimensionType, there are two cases:
		//   we're accessing a single element of a multidimensional array
		//   we're accessing another entire dimension of a multidimensional array
		ArrayDimensionType arrayDimension = (ArrayDimensionType) variableType;
		arrayType = arrayDimension.getArrayType();
		arrayElementType = TypeUtils.getStrippedType(arrayType.getType());

		byteSize = arrayElementType.getByteSize();
		if (arrayElementType instanceof ITypedef) {
			byteSize = TypeUtils.getStrippedType(arrayElementType.getType()).getByteSize();
		}

		arrayDimension.addDimension(subscript, arrayType.getBound(arrayDimension.getDimensionCount()).getElementCount()
				* byteSize * subscript);
		location = arrayDimension.getLocation();

		if (arrayDimension.getDimensionCount() >= arrayType.getBoundsCount()) {
			// we're accessing a single element of a multidimensional array
			pushArrayElement(arrayDimension.getOperandValue(), location, arrayElementType);
		} else {
			// we're accessing another entire dimension of a multidimensional
			// array
			variableOperand.setAddressValue(location);
			variableOperand.setValueLocation(location);
			push(variableOperand);
		}
	}

	private void pushArrayElement(OperandValue originalVariableValue, IVariableLocation location, IType arrayElementType) throws CoreException {
		OperandValue varValue = new OperandValue(arrayElementType);

		varValue.setValueLocation(location);

		// for a lvalues (base arithmetic types, enums, and pointers), read the
		// value and cast it to the right type
		if (arrayElementType instanceof IBasicType || arrayElementType instanceof IEnumeration
				|| arrayElementType instanceof IPointerType) {
			int byteSize = arrayElementType.getByteSize();

			// TODO support 12-byte long double
			if (byteSize != 1 && byteSize != 2 && byteSize != 4 && byteSize != 8) {
				throw EDCDebugger.newCoreException(MessageFormat.format(ASTEvalMessages.UnhandledSize, byteSize));
			}

			// read the value pointed to
			Number newValue = originalVariableValue.getValueByType(arrayElementType, location);
			varValue.setValue(newValue);
			push(varValue);

		} else if (arrayElementType instanceof IAggregate) {
			// for aggregates, the address of the aggregate is the value
			// returned
			varValue.setAddressValue(location);
			push(varValue);

		} else {
			throw EDCDebugger.newCoreException(ASTEvalMessages.ArraySubscript_ErrorDereferencingArray);
		}
	}

}
