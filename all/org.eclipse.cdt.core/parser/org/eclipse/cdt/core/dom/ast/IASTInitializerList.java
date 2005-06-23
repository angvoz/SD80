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
 * This is an an initializer that is a list of initializers.
 * 
 * @author Doug Schaefer
 */
public interface IASTInitializerList extends IASTInitializer {

	/**
	 * <code>NESTED_INITIALIZER</code> describes the relationship between an
	 * <code>IASTInitializerList</code> and its sub-<code>IASTInitializer</code>s.
	 */
	public static final ASTNodeProperty NESTED_INITIALIZER = new ASTNodeProperty(
			"IASTInitializerList.NESTED_INITIALIZER - sub-IASTInitializer for IASTInitializerList"); //$NON-NLS-1$

	/**
	 * Get the list of initializers.
	 * 
	 * @return <code>IASTInitializer[]</code> array of initializers
	 */
	public IASTInitializer[] getInitializers();

	/**
	 * Add an initializer to the initializer list.
	 * 
	 * @param initializer
	 *            <code>IASTInitializer</code>
	 */
	public void addInitializer(IASTInitializer initializer);
}
