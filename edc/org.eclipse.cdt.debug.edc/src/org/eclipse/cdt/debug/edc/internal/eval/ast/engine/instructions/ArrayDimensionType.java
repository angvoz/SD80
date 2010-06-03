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

import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.Type;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;

// Internal expression type to hold all dimensions of a multidimensional array except the smallest.
// E.g., for "int a[6][7][8];", this type might hold info about "a[2]" or "a[2][4]", but not "a[2][4][3]".
public class ArrayDimensionType extends Type implements IArrayDimensionType {

	private final OperandValue value; // needed for scope,
														// frame, services
														// tracker, etc.
	private final IArrayType arrayType;
	private IVariableLocation location;
	private int dimensionCount; // number of dimensions processed so far

	public ArrayDimensionType(String name, OperandValue value, IArrayType arrayType, IVariableLocation location) {
		super(name, null, 0, null);
		this.value = value;
		this.arrayType = arrayType;
		this.location = location;
		this.dimensionCount = 1;
	}

	public OperandValue getOperandValue() {
		return this.value;
	}

	public IArrayType getArrayType() {
		return this.arrayType;
	}

	public IVariableLocation getLocation() {
		return this.location;
	}

	public int getDimensionCount() {
		return this.dimensionCount;
	}

	public void addDimension(long subscript, long increase) {
		this.name += "[" + subscript + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		this.location = location.addOffset(increase);
		this.dimensionCount++;
	}

	@Override
	public IType getType() {
		return null;
	}

}
