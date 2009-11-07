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

	@Override
	public IType getType() {
		if (!qualifiersChecked)
			checkQualifiers();
		return type;
	}

	/**
	 * Check whether the type is qualified. If so, set the proper booleans and
	 * have getType() return the unqualified type.
	 */
	private void checkQualifiers() {
		IType typeChecked = this.type;
		while (typeChecked != null && (typeChecked instanceof IQualifierType)) {
			if (typeChecked instanceof ConstType)
				this.isConst = true;
			else
				this.isVolatile = true; // assumes the only qualifiers are const
										// and volatile
		}
		this.type = typeChecked;
		this.qualifiersChecked = true;
	}

}
