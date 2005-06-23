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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;

/**
 * Elaborated types in C++ include classes.
 * 
 * @author jcamelon
 */
public interface ICPPASTElaboratedTypeSpecifier extends
		IASTElaboratedTypeSpecifier, ICPPASTDeclSpecifier {

	/**
	 * <code>k_class</code> represents elaborated class declaration
	 */
	public static final int k_class = IASTElaboratedTypeSpecifier.k_last + 1;

	/**
	 * <code>k_last</code> is defined for subinterfaces.
	 */
	public static final int k_last = k_class;

}
