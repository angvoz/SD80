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

import org.eclipse.cdt.debug.edc.internal.symbols.ILexicalBlockScope;

/**
 * Interface representing a variable or parameter.
 */
public interface IVariable {

	/**
	 * Get the name of the variable
	 * 
	 * @return the variable name
	 */
	String getName();

	/**
	 * Get the scope that the variable belongs to
	 * 
	 * @return the variable scope
	 */
	IScope getScope();

	/**
	 * Get the type of the variable
	 * 
	 * @return the variable type
	 */
	IType getType();

	/**
	 * Get the location provider for the variable
	 * 
	 * @return the location provider
	 */
	ILocationProvider getLocationProvider();

	/**
	 * A variable's lifetime may start somewhere inside its parent scope (without being
	 * inside an {@link ILexicalBlockScope}).  This provides the offset from the
	 * start address of the parent scope at which time the variable is considered
	 * live.
	 * <p>
	 * This scope may be narrower than the scope implied by {@link ILocationProvider#isLocationKnown(org.eclipse.cdt.core.IAddress)}.
	 * <p>
	 * Note: a variable is always considered to be live until the end of the parent scope.
	 * @return offset in bytes (0 means the lifetime is the same as the parent scope)
	 */
	long getStartScope();

	/**
	 * 
	 */
	void dispose();
}
