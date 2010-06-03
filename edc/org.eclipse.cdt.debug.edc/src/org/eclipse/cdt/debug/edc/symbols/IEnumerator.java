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

/**
 * Interface representing an enumerator (what's inside enum {}). E.g.,
*  enum {Red, Green, Blue} has enumerators Red, Green, Blue
 */
public interface IEnumerator {

	/**
	 * Get the name of the enumerator
	 * 
	 * @return the enumerator name
	 */
	String getName();

	/**
	 * Get the value of the enumerator.
	 * 
	 * @return the enumerator value
	 */
	long getValue();
}
