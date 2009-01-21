/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

/** 
 * Interface for methods on scopes that are internal to the AST.
 */
public interface IASTInternalScope extends IScope {
    /**
     * Return the physical IASTNode that this scope was created for
     */
    public IASTNode getPhysicalNode();
		
	/**
	 * This adds an IBinding to the scope.  It is primarily used by the parser to add
	 * implicit IBindings to the scope (such as GCC built-in functions).
	 * 
	 * @param binding
	 * @throws DOMException
	 */
	public void addBinding(IBinding binding) throws DOMException;

	/**
	 * Add an IASTName to be cached in this scope
	 * 
	 * @param name
	 * @throws DOMException
	 */
	public void addName(IASTName name) throws DOMException;
	
	/**
	 * Can be called during ambiguity resolution to populate a scope without considering
	 * the ambiguous branches. The rest of the names has to be cached one by one after
	 * the ambiguities have been resolved.
	 */
	public void populateCache();
}
