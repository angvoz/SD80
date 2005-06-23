/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @author Doug Schaefer
 */
public interface IBinding {
    public static final IBinding[] EMPTY_BINDING_ARRAY = new IBinding[0];
	/**
	 * The name of the binding.
	 * 
	 * @return name
	 */
	public String getName();
    
    /**
     * The name of the binding.
     * 
     * @return name
     */
	public char[] getNameCharArray();
	
	/**
	 * Every name has a scope.
	 * 
	 * @return the scope of this name
	 */
	public IScope getScope() throws DOMException;

}
