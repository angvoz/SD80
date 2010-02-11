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
package org.eclipse.cdt.debug.edc.internal.symbols.newdwarf;

import org.eclipse.cdt.debug.edc.internal.symbols.IForwardTypeReference;
import org.eclipse.cdt.debug.edc.internal.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.IScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IType;
import org.eclipse.cdt.debug.edc.internal.symbols.Variable;

public class DwarfVariable extends Variable {

	public DwarfVariable(String name, IScope scope, ILocationProvider locationProvider, 
			IType type) {
		super(name, scope, null, locationProvider);

		this.type = type;
	}

	@Override
	public IType getType() {
		if (type instanceof IForwardTypeReference) {
			return ((IForwardTypeReference) type).getReferencedType();
		}
		return type;
	}

}
