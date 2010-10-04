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

import java.util.Map;

import org.eclipse.cdt.debug.edc.symbols.IScope;

public class FieldType extends Type implements IField {

	private final ICompositeType compositeType;
	private long fieldOffset;
	private final int bitSize;
	private final int bitOffset;
	private final int accessibility;

	public FieldType(String name, IScope scope, ICompositeType compositeType, long fieldOffset, int bitSize,
			int bitOffset, int byteSize, int accessibility, Map<Object, Object> properties) {
		super(name, scope, byteSize, properties);

		this.compositeType = compositeType;
		this.fieldOffset = fieldOffset;
		this.bitSize = bitSize;
		this.bitOffset = bitOffset;
		this.accessibility = accessibility;
	}

	public long getFieldOffset() {
		return this.fieldOffset;
	}

	public int getBitSize() {
		return this.bitSize;
	}

	public int getBitOffset() {
		return this.bitOffset;
	}
	
	public int getAccessibility() {
		return this.accessibility;
	}

	public ICompositeType getCompositeTypeOwner() {
		return this.compositeType;
	}

	public void setFieldOffset(long offset) {
		this.fieldOffset = offset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Type#getByteSize()
	 */
	@Override
	public int getByteSize() {
		return updateByteSizeFromSubType();
	}

	@Override
	public String toString() {
		return name + " offset = " + fieldOffset //$NON-NLS-1$
				+ ", byteSize = " +	getByteSize() //$NON-NLS-1$
				+ (bitOffset != 0 ? ", bitOffset = " + bitOffset : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (bitSize != 0 ? ", bitSize = " + bitSize : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ", accessibility = " + //$NON-NLS-1$
					(accessibility == ICompositeType.ACCESS_PRIVATE ?
							"private" //$NON-NLS-1$
					: (accessibility == ICompositeType.ACCESS_PROTECTED ?
							"protected" //$NON-NLS-1$
							: "public")); //$NON-NLS-1$
	}

}
