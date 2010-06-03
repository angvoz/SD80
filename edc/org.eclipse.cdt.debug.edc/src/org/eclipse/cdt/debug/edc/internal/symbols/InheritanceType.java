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

public class InheritanceType extends Type implements IInheritance {

	// access type of inheritance
	private final int accessibility;

	// offset in inherited class to the inherited fields 
	private final long fieldsOffset;
	
	public InheritanceType(IScope scope, int accessibility, long inheritanceOffset, Map<Object, Object> properties) {
		super("", scope, 0, properties); //$NON-NLS-1$
		this.accessibility = accessibility;
		this.fieldsOffset = inheritanceOffset;
	}
	
	public int getAccessibility() {
		return this.accessibility;
	}
	
	public long getFieldsOffset() {
		return this.fieldsOffset;
	}
	
	@Override
	public String getName() {
		if (getType() instanceof ICompositeType)
			return ((ICompositeType) getType()).getBaseName();

		if (getType() == null)
			return "$"; //$NON-NLS-1$  // do not confuse expression formatter
		else
			return getType().getName();
	}
}
