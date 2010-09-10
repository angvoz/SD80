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
package org.eclipse.cdt.debug.edc.internal.symbols;

import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IType;

public class ArrayBoundType extends Type implements IArrayBoundType {

	// bound of this array dimension. E.g., for "int a[7][8]", this would be
	// either 7 or 8.
	private final long bound;

	// number of array elements associated with each index of this array
	// dimension.
	// E.g., for "int a[7][8]", "a[1]" comprises 8 elements, but "a[1][2]"
	// comprises 1 element.
	private long elements;

	// array dimension ordinal. E.g., for "int a[7][8]", "[7]" is index 1 and
	// "[8]" is index 0;
	private long dimensionIndex = 0;

	public ArrayBoundType(IScope scope, long arrayBound) {
		super("", scope, 0, null); //$NON-NLS-1$

		if (arrayBound < 1) {
			this.bound = 0;
			this.elements = 0;
		} else {
			this.bound = arrayBound;
			this.elements = 1;
		}
	}

	public long getBoundCount() {
		return this.bound;
	}

	public long getElementCount() {
		return this.elements;
	}

	public long getDimensionIndex() {
		return this.dimensionIndex;
	}

	public void multiplyElementCount(long multiply) {
		this.elements *= multiply;
	}

	public void incDimensionIndex() {
		this.dimensionIndex++;
	}

	@Override
	public IType getType() {
		return null;
	}

}
