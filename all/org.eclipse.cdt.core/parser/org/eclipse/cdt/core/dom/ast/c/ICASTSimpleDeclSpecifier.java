/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;

/**
 * This interface represents a built-in type in C.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTSimpleDeclSpecifier extends IASTSimpleDeclSpecifier,
		ICASTDeclSpecifier {

	// Extra types in C
	/**
	 * <code>t_Bool</code> boolean. e.g. _Bool x;
	 */
	public static final int t_Bool = IASTSimpleDeclSpecifier.t_last + 1;

	/**
	 * <code>t_last</code> is defined for sub-interfaces.
	 */
	public static final int t_last = t_Bool;
	
	/**
	 * Is complex number? e.g. _Complex t;
	 * @return true if it is a complex number, false otherwise
	 */
	public boolean isComplex();
	
	/**
	 * Set the number to be complex.
	 * @param value true if it is a complex number, false otherwise
	 */
	public void setComplex(boolean value);
	
	/**
	 * Is imaginary number? e.g. _Imaginr
	 * @return true if it is an imaginary number, false otherwise
	 */
	public boolean isImaginary();
	
	/**
	 * Set the number to be imaginary.
	 * @param value true if it is an imaginary number, false otherwise
	 */
	public void setImaginary(boolean value);
	
	// allow for long long's
	/**
	 * Is long long?
	 * 
	 * @return boolean
	 */
	public boolean isLongLong();

	/**
	 * Set long long to be 'value'.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setLongLong(boolean value);
	
	/**
	 * @since 5.1
	 */
	public ICASTSimpleDeclSpecifier copy();

}
