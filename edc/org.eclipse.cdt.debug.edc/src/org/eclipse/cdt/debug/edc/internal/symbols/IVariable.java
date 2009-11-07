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

}
