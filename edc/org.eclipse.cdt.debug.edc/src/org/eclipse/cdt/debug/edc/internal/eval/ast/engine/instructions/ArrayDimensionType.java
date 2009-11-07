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

import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.IType;
import org.eclipse.cdt.debug.edc.internal.symbols.Type;
import org.eclipse.cdt.utils.Addr64;

// Internal expression type to hold all dimensions of a multidimensional array except the smallest.
// E.g., for "int a[6][7][8];", this type might hold info about "a[2]" or "a[2][4]", but not "a[2][4][3]".
public class ArrayDimensionType extends Type implements IArrayDimensionType {

	private final VariableWithValue variableWithValue; // needed for scope,
														// frame, services
														// tracker, etc.
	private final IArrayType arrayType;
	private Addr64 location;
	private int dimensionCount; // number of dimensions processed so far

	public ArrayDimensionType(String name, VariableWithValue variableWithValue, IArrayType arrayType, Addr64 location) {
		super(name, null, 0, null);
		this.variableWithValue = variableWithValue;
		this.arrayType = arrayType;
		this.location = location;
		this.dimensionCount = 1;
	}

	public VariableWithValue getVariableWithValue() {
		return this.variableWithValue;
	}

	public IArrayType getArrayType() {
		return this.arrayType;
	}

	public Addr64 getLocation() {
		return this.location;
	}

	public int getDimensionCount() {
		return this.dimensionCount;
	}

	public void addDimension(long subscript, long increase) {
		this.name += "[" + subscript + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		this.location = (Addr64) this.location.add(increase);
		this.dimensionCount++;
	}

	@Override
	public IType getType() {
		return null;
	}

}
