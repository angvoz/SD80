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
package org.eclipse.cdt.debug.edc.internal.symbols;

import java.util.Map;

public class FieldType extends Type implements IField {

	private final ICompositeType compositeType;
	private final long fieldOffset;
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
	
	@Override
	public String toString() {
		return name + " offset = " + fieldOffset
				+ ", byteSize = " +	getByteSize()
				+ (bitOffset != 0 ? ", bitOffset = " + bitOffset : "")
				+ (bitSize != 0 ? ", bitSize = " + bitSize : "")
				+ ", accessibility = " +
					(accessibility == ICompositeType.ACCESS_PRIVATE ?
							"private"
					: (accessibility == ICompositeType.ACCESS_PROTECTED ?
							"protected"
							: "public"));
	}

}
