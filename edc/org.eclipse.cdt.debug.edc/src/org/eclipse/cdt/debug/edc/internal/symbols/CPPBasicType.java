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

	public int getQualifierBits() {
		return this.qualifierBits;
	}

	public int getBaseType() {
		return this.baseType;
	}

	public boolean isLong() {
		return (this.qualifierBits & ICPPBasicType.IS_LONG) != 0;
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

	@Override
	public IType getType() {
		return null;
	}

}
