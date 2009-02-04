/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents a function call expression. f( x ) : f is the
 * function name expression, x is the parameter expression.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTFunctionCallExpression extends IASTExpression {

	/**
	 * <code>FUNCTION_NAME</code> represents the relationship between a
	 * <code>IASTFunctionCallExpression</code> and its
	 * <code>IASTExpression</code> (function name).
	 */
	public static final ASTNodeProperty FUNCTION_NAME = new ASTNodeProperty(
			"IASTFunctionCallExpression.FUNCTION_Name - IASTExpression (name) for IASTFunctionCallExpression"); //$NON-NLS-1$

	/**
	 * Set the function name expression.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code> representing the function name
	 */
	public void setFunctionNameExpression(IASTExpression expression);

	/**
	 * Get the function name expression.
	 * 
	 * @return <code>IASTExpression</code> representing the function name
	 */
	public IASTExpression getFunctionNameExpression();

	/**
	 * <code>PARAMETERS</code> represents the relationship between a
	 * <code>IASTFunctionCallExpression</code> and its
	 * <code>IASTExpression</code> (parameters).
	 */
	public static final ASTNodeProperty PARAMETERS = new ASTNodeProperty(
			"IASTFunctionCallExpression.PARAMETERS - IASTExpression (parameters) for IASTFunctionCallExpression"); //$NON-NLS-1$

	/**
	 * Set the parameters expression.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code> representing the parameters
	 */
	public void setParameterExpression(IASTExpression expression);

	/**
	 * Get the parameter expression.
	 * 
	 * @return <code>IASTExpression</code> representing the parameters
	 */
	public IASTExpression getParameterExpression();
	
	/**
	 * @since 5.1
	 */
	public IASTFunctionCallExpression copy();

}
