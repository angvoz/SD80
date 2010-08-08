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
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IVariable;
import org.eclipse.core.runtime.IPath;

public class FunctionScope extends Scope implements IFunctionScope {

	protected ILocationProvider frameBaseLocationProvider;
	protected List<IVariable> parameters = new ArrayList<IVariable>();
	private int declLine;
	private int declColumn;
	private IPath declFile;

	public FunctionScope(String name, IScope parent, IAddress lowAddress, IAddress highAddress,
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IFunctionScope#getScopedVariables(org.eclipse.cdt.core.IAddress)
	 */
	public Collection<IVariable> getScopedVariables(IAddress linkAddress) {
		// Unfortunately, lexical blocks and inlined functions may span several scopes or use ranges;
		// don't #getScopeAtAddress() and go up the parent chain, but iterate them top-down.
		
		List<IVariable> scoped = new ArrayList<IVariable>();

		recurseGetScopedVariables(this, scoped, linkAddress);

		return scoped;
	}
		
	protected static void recurseGetScopedVariables(IScope scope, List<IVariable> scoped, IAddress linkAddress) {
		long scopeOffset = linkAddress.add(scope.getLowAddress().getValue().negate()).getValue().longValue();

		for (IVariable var : scope.getVariables()) {
			if (scopeOffset >= var.getStartScope() && var.getLocationProvider().isLocationKnown(linkAddress)) {
				scoped.add(var);
			}
		}

		boolean isFunctionScope = (scope instanceof IFunctionScope); 
		if (isFunctionScope) {
			for (IVariable var : ((FunctionScope) scope).getParameters()) {
				if (scopeOffset >= var.getStartScope() && var.getLocationProvider().isLocationKnown(linkAddress)) {
					scoped.add(var);
				}
			}
		}

		for (IScope kid : scope.getChildren()) {
			// notice this is > instead of >= like caller getScopedVariables() ...
			// thus stepping out of scope to next instr won't result in scoped variables still being seen
			if (kid.getLowAddress().compareTo(linkAddress) <= 0 && kid.getHighAddress().compareTo(linkAddress) > 0)
				recurseGetScopedVariables(kid, scoped, linkAddress);
			else if (isFunctionScope && linkAddress.compareTo(kid.getHighAddress()) == 0)
				recurseGetScopedVariables(kid, scoped, kid.getHighAddress());
		}
	}

	public void addParameter(IVariable parameter) {
		parameters.add(parameter);
	}

	public IPath getDeclFile() {
		return declFile;
	}
	
	public void setDeclFile(IPath declFile) {
		this.declFile = declFile;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IFunctionScope#getDeclLine()
	 */
	public int getDeclLine() {
		return declLine;
	}
	
	public void setDeclLine(int declLine) {
		this.declLine = declLine;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IFunctionScope#getDeclColumn()
	 */
	public int getDeclColumn() {
		return declColumn;
	}
	
	public void setDeclColumn(int declColumn) {
		this.declColumn = declColumn;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Scope#addChild(org.eclipse.cdt.debug.edc.internal.symbols.IScope)
	 */
	@Override
	public void addChild(IScope scope) {
		super.addChild(scope);
		
		if (scope instanceof IFunctionScope)
			addLineInfoToParent(scope);
	}
	

	public void setLocationProvider(ILocationProvider locationProvider) {
		this.frameBaseLocationProvider = locationProvider;
	}

}
