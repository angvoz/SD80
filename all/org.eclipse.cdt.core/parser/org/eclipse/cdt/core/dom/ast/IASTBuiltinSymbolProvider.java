/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is used to IASTName implementations to determine if they are bound to a Built-in Symbol
 * provided by a Built-in Symbol Provider that implements this interface.
 * 
 * @author dsteffle
 * 
 * @deprecated Use {@link org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider} instead
 */
@Deprecated
public interface IASTBuiltinSymbolProvider {
	
	/**
	 * Returns all of the IBindings corresponding to the IASTBuiltinSymbolProvider.
	 */
	public IBinding[] getBuiltinBindings();
	
}
