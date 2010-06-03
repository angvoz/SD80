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

public class MayBeQualifiedType extends Type implements IMayBeQualifedType {
	boolean qualifiersChecked = false;
	boolean isConst = false;
	boolean isVolatile = false;

	public MayBeQualifiedType(String name, IScope scope, int byteSize, Map<Object, Object> properties) {
		super(name, scope, byteSize, properties);
	}

	public boolean isConst() {
		if (!qualifiersChecked)
			checkQualifiers();
		return isConst;
	}

	public boolean isVolatile() {
		if (!qualifiersChecked)
			checkQualifiers();
		return isVolatile;
	}

	/**
	 * Check whether the type is qualified. If so, set the proper booleans.
	 */
	private void checkQualifiers() {
		IType checkedType = getType();	// so it will be resolved
		while (checkedType != null && (checkedType instanceof IQualifierType)) {
			isConst    |= checkedType instanceof ConstType;
			isVolatile |= checkedType instanceof VolatileType;
			checkedType = checkedType.getType();
		}
		qualifiersChecked = true;
	}

}
