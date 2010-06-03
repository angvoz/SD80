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
package org.eclipse.cdt.debug.edc.symbols;

import java.util.Collection;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.runtime.IPath;

/**
 * Interface representing a function scope. A function is a block of code
 * (function/method) which may have parameters and lexical blocks.
 * <p>
 * TODO: Note: a function's scope is either {@link IFunctionScope} or {@link ICompileUnitScope}.
 * This does <b>not</b> reflect language scoping.  Thus, there is currently no way to
 * find out if a function is a method of a given class (except, perhaps, by looking
 * at a 'this' parameter and introspecting its type).  (But nothing currently handles
 * namespaces.)
 */
public interface IFunctionScope extends IScope {

	/**
	 * Gets the list of variables in this scope and any child scopes
	 * (for functions and lexical blocks)
	 * 
	 * @return unmodifiable list of variables which may be empty
	 */
	Collection<IVariable> getVariablesInTree();
	
	
	/**
	 * Get function parameters
	 * 
	 * @return unmodifiable list of parameters which may be empty
	 */
	Collection<IVariable> getParameters();
	
	/**
	 * Get the variables live at the given address, by using ILexicalBlockScope
	 * and embedded IFunctionScopes, as well as using {@link IVariable#getLocationProvider()}
	 * information.
	 * @param linkAddress the link-time address
	 * @return unmodifiable list of locals which may be empty
	 */
	Collection<IVariable> getScopedVariables(IAddress linkAddress);
	
	/**
	 * Get the location provider for the frame base.
	 * 
	 * @return the location provider, or null if none
	 */
	ILocationProvider getFrameBaseLocation();

	/**
	 * Get the file where the function was declared, if known.
	 * @return IPath or <code>null</code>
	 */
	IPath getDeclFile();

	/**
	 * Get the line number where the function was declared, if known.
	 * @return line number, 1-based (0 if unknown)
	 */
	int getDeclLine();
	
	/**
	 * Get the column number where the function was declared, if known.
	 * @return column number, 1-based (0 if unknown)
	 */
	int getDeclColumn();
}
