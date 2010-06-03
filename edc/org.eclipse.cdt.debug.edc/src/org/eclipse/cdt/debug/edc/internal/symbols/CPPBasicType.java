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
import org.eclipse.cdt.debug.edc.symbols.IType;

public class CPPBasicType extends MayBeQualifiedType implements ICPPBasicType {

	private final int baseType;
	private final int qualifierBits;

	public CPPBasicType(String name, IScope scope, int baseType, int qualifierBits, int byteSize,
			Map<Object, Object> properties) {
		super(name, scope, byteSize, properties);
		this.baseType = baseType;
		this.qualifierBits = qualifierBits;
	}

	public CPPBasicType(String name, int baseType, int qualifierBits, int byteSize) {
		super(name, null, byteSize, null);
		this.baseType = baseType;
		this.qualifierBits = qualifierBits;
	}

	
	/**
	 * WARNING: this only works for CPPBasicType itself.  No other types in
	 * the hierarchy implement this correctly.
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 12345;		// super.hashCode();
		result = prime * result + baseType;
		result = prime * result + qualifierBits;
		return result;
	}

	/**
	 * WARNING: this only works for CPPBasicType itself.  No other types in
	 * the hierarchy implement this correctly.
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		//if (!super.equals(obj))
		//	return false;
		if (getClass() != obj.getClass())
			return false;
		// do not test name, since it's essentially random
		CPPBasicType other = (CPPBasicType) obj;
		if (baseType != other.baseType)
			return false;
		if (qualifierBits != other.qualifierBits)
			return false;
		return true;
	}

	public int getQualifierBits() {
		return this.qualifierBits;
	}

	public int getBaseType() {
		return this.baseType;
	}

	public boolean isLong() {
		return (this.qualifierBits & ICPPBasicType.IS_LONG) != 0;
	}
	
	public boolean isLongLong() {
		return (this.qualifierBits & ICPPBasicType.IS_LONG_LONG) != 0;
	}

	public boolean isShort() {
		return (this.qualifierBits & ICPPBasicType.IS_SHORT) != 0;
	}

	public boolean isSigned() {
		return (this.qualifierBits & ICPPBasicType.IS_SIGNED) != 0;
	}

	public boolean isUnsigned() {
		return (this.qualifierBits & ICPPBasicType.IS_UNSIGNED) != 0;
	}
	
	public boolean isComplex() {
		return (this.qualifierBits & ICPPBasicType.IS_COMPLEX) != 0;
	}

	@Override
	public IType getType() {
		return null;
	}

}
