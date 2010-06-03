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

/**
 * Generic symbolic scope interface.
 */
public interface IScope extends Comparable<Object> {

	/**
	 * Get the name of the scope
	 * 
	 * @return the name
	 */
	String getName();

	/**
	 * Tell whether the scope has an empty address range (either unset or unspecified)
	 */
	boolean hasEmptyRange();

	/**
	 * Get the low link address of this scope (absolute or synthesized from a range list)
	 * 
	 * @return low address, or null if unknown
	 */
	IAddress getLowAddress();

	/**
	 * Get the high link address of this scope (absolute or synthesized from a range list)
	 * 
	 * @return high address, or null if unknown
	 */
	IAddress getHighAddress();

	/**
	 * Get the list of non-consecutive ranges for the scope.
	 * @return list or <code>null</code> if the low and high addresses specify a contiguous range
	 */
	IRangeList getRangeList();
	
	/**
	 * Get the parent of this scope
	 * 
	 * @return the parent scope, or null if the highest level scope
	 */
	IScope getParent();

	/**
	 * Gets the list of child scopes (if any)
	 * 
	 * @return unmodifiable list of child scopes which may be empty
	 */
	Collection<IScope> getChildren();

	/**
	 * Gets the list of variables in this scope only 
	 * 
	 * @return unmodifiable list of variables which may be empty
	 */
	Collection<IVariable> getVariables();

	/**
	 * Gets the list of enumerators in this scope (if any)
	 * 
	 * @return unmodifiable list of enumerators which may be empty
	 */
	Collection<IEnumerator> getEnumerators();

	/**
	 * Find the smallest scope at the given link address
	 * 
	 * @param address
	 *            the link address
	 * @return the smallest scope containing the given address, or null if none
	 *         found
	 */
	IScope getScopeAtAddress(IAddress linkAddress);

	/**
	 * 
	 */
	void dispose();

}
