/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

/**
 * Catch handler serves as a standalone stage.
 * 
 * @author jcamelon
 */
public interface ICPPASTCatchHandler extends IASTStatement {

	/**
	 * Constant
	 */
	public static final ICPPASTCatchHandler[] EMPTY_CATCHHANDLER_ARRAY = new ICPPASTCatchHandler[0];

	/**
	 * <code>DECLARATION</code> represnts the nested declaration within the
	 * catch handler.
	 */
	public static final ASTNodeProperty DECLARATION = new ASTNodeProperty(
			"Declaration"); //$NON-NLS-1$

	/**
	 * <code>CATCH_BODY</code> represents the nested (compound) statement.
	 */
	public static final ASTNodeProperty CATCH_BODY = new ASTNodeProperty(
			"Catch Body"); //$NON-NLS-1$

	/**
	 * Set is catch all handler.
	 * 
	 * @param isEllipsis
	 *            boolean
	 */
	public void setIsCatchAll(boolean isEllipsis);

	/**
	 * Is this catch handler for all exceptions?
	 * 
	 * @return boolean
	 */
	public boolean isCatchAll();

	/**
	 * Set the catch body.
	 * 
	 * @param compoundStatement
	 *            <code>IASTStatement</code>
	 */
	public void setCatchBody(IASTStatement compoundStatement);

	/**
	 * Get the cathc body.
	 * 
	 * @return <code>IASTStatement</code>
	 */
	public IASTStatement getCatchBody();

	/**
	 * Set the declaration.
	 * 
	 * @param decl
	 *            <code>IASTDeclaration</code>
	 */
	public void setDeclaration(IASTDeclaration decl);

	/**
	 * Get the declaration.
	 * 
	 * @return <code>IASTDeclaration</code>
	 */
	public IASTDeclaration getDeclaration();

}
