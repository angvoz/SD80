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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IVariable;

public class LexicalBlockScope extends Scope implements ILexicalBlockScope {

	public LexicalBlockScope(String name, IScope parent, IAddress lowAddress, IAddress highAddress) {
		super(name, lowAddress, highAddress, parent);
	}

	public Collection<IVariable> getVariablesInTree() {
		List<IVariable> variables = new ArrayList<IVariable>();
		variables.addAll(super.getVariables());

		// check for variables in children as well
		for (IScope child : children) {
			if (child instanceof IFunctionScope)
				variables.addAll(((IFunctionScope) child).getVariablesInTree());
			else if (child instanceof ILexicalBlockScope)
				variables.addAll(((ILexicalBlockScope) child).getVariablesInTree());
			else
				variables.addAll(child.getVariables());
		}

		return variables;
	}
}
