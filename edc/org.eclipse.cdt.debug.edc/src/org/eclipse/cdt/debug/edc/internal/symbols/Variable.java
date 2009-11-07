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

public class Variable implements IVariable {

	protected String name;
	protected IScope scope;
	protected IType type;
	protected ILocationProvider locationProvider;

	public Variable(String name, IScope scope, IType type, ILocationProvider locationProvider) {
		this.name = name;
		this.scope = scope;
		this.type = type;
		this.locationProvider = locationProvider;
	}

	public String getName() {
		return name;
	}

	public IScope getScope() {
		return scope;
	}

	public IType getType() {
		return type;
	}

	public ILocationProvider getLocationProvider() {
		return locationProvider;
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
