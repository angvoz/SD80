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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.IAddress;

public class FunctionScope extends Scope implements IFunctionScope {

	protected ILocationProvider frameBaseLocationProvider;
	protected List<IVariable> parameters = new ArrayList<IVariable>();

	public FunctionScope(String name, ICompileUnitScope parent, IAddress lowAddress, IAddress highAddress,
			ILocationProvider frameBaseLocationProvider) {
		super(name, lowAddress, highAddress, parent);

		this.frameBaseLocationProvider = frameBaseLocationProvider;
	}

	public Collection<IVariable> getParameters() {
		return Collections.unmodifiableCollection(parameters);
	}

	public ILocationProvider getFrameBaseLocation() {
		return frameBaseLocationProvider;
	}

	@Override
	public Collection<IVariable> getVariables() {
		List<IVariable> variables = new ArrayList<IVariable>();
		variables.addAll(super.getVariables());

		// check for variables in child lexical blocks as well
		for (IScope child : children) {
			variables.addAll(child.getVariables());
		}

		return variables;
	}

	public void addParameter(IVariable parameter) {
		parameters.add(parameter);
	}

}
