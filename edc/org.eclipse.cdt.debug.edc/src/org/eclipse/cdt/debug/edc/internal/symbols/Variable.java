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

import org.eclipse.cdt.debug.edc.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariable;


public class Variable implements IVariable {

	protected String name;
	protected IScope scope;
	protected IType type;
	protected ILocationProvider locationProvider;
	protected long startScope;
	
	public Variable(String name, IScope scope, IType type, ILocationProvider locationProvider) {
		this.name = name;
		this.scope = scope;
		this.type = type;
		this.locationProvider = locationProvider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IVariable#dispose()
	 */
	public void dispose() {
		type = null;
		locationProvider = null;
		scope = null;
	}
	
	public String getName() {
		return name;
	}

	public IScope getScope() {
		return scope;
	}

	public IType getType() {
		if (type instanceof IForwardTypeReference)
			type = ((IForwardTypeReference) type).getReferencedType();
		
		return type;
	}

	public ILocationProvider getLocationProvider() {
		return locationProvider;
	}

	public void setStartScope(long start) {
		this.startScope = start;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IVariable#getStartScope()
	 */
	public long getStartScope() {
		return startScope;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Var ["); //$NON-NLS-1$
		if (name != null) {
			builder.append(name);
		}
		if (scope != null) {
			builder.append(", "); //$NON-NLS-1$
			builder.append("scope="); //$NON-NLS-1$
			builder.append(scope.getName());
		}
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}
}
