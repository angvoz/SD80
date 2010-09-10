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

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;

public class ArrayType extends MayBeQualifiedType implements IArrayType {

	protected ArrayList<IArrayBoundType> bounds = new ArrayList<IArrayBoundType>();

	public ArrayType(String name, IScope scope, int byteSize, Map<Object, Object> properties) {
		super(name, scope, byteSize, properties);
	}

	public int getBoundsCount() {
		return bounds.size();
	}

	public void addBound(IArrayBoundType bound) {
		// existing array dimensions now represent bound.getBoundCount() times
		// as many array elements,
		// and have a higher dimensional position
		for (IArrayBoundType existingBound : this.bounds) {
			existingBound.multiplyElementCount(bound.getBoundCount());
			existingBound.incDimensionIndex();
		}
		bounds.add(bound);
		byteSize = 0;	// recalculate
	}

	public IArrayBoundType[] getBounds() {
		IArrayBoundType[] boundsArray = new IArrayBoundType[bounds.size()];
		for (int i = 0; i < bounds.size(); i++) {
			boundsArray[i] = bounds.get(i);
		}
		return boundsArray;
	}

	// get the Nth bound. E.g., for "a[X][Y]", getBound(0) returns info for
	// "[X]"
	public IArrayBoundType getBound(int index) {
		if (index >= 0 && index < bounds.size())
			return bounds.get(index);

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Type#getByteSize()
	 */
	@Override
	public int getByteSize() {
		if (byteSize == 0) {
			if (bounds.size() > 0) {
				updateByteSizeFromSubType();
				IType subtype = TypeUtils.getStrippedType(getType());
				if (subtype instanceof IArrayType) {
					for (IArrayBoundType bound : ((IArrayType)subtype).getBounds())
						byteSize *= bound.getBoundCount();
				}
			}
		}
		return byteSize;
	}
}
