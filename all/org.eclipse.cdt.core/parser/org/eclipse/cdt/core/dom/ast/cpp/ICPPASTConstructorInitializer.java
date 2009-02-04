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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;

/**
 * This is an initializer that is a call to the constructor for the declarator.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTConstructorInitializer extends IASTInitializer {

	/**
	 * <code>EXPRESSION</code> represents the expression being conusmed in a
	 * constructor.
	 */
	public static final ASTNodeProperty EXPRESSION = new ASTNodeProperty(
			"ICPPASTConstructorInitializer.EXPRESSION - Expression consumed in constructor"); //$NON-NLS-1$

	/**
	 * Get the arguments to the constructor.
	 * 
	 * @return IASTExpression
	 */
	public IASTExpression getExpression();

	/**
	 * Set the arguments to the constructor.
	 * 
	 * @param expression
	 */
	public void setExpression(IASTExpression expression);

	/**
	 * @since 5.1
	 */
	public ICPPASTConstructorInitializer copy();
}
