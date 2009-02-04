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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;

/**
 * This interface represents a built-in type in C++.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTSimpleDeclSpecifier extends IASTSimpleDeclSpecifier,
		ICPPASTDeclSpecifier {
	// Extra types
	/**
	 * <code>t_bool</code> bool
	 */
	public static final int t_bool = IASTSimpleDeclSpecifier.t_last + 1;

	/**
	 * <code>t_wchar_t</code> wchar_t
	 */
	public static final int t_wchar_t = IASTSimpleDeclSpecifier.t_last + 2;

	/**
	 * <code>t_last</code> is specified for subinterfaces.
	 */
	public static final int t_last = t_wchar_t;
	
	/**
	 * @since 5.1
	 */
	public ICPPASTSimpleDeclSpecifier copy();

}
