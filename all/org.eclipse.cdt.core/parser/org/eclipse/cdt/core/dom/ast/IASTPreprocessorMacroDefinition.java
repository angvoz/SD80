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
 * This represents the definition of a macro.
 * 
 * @author Doug Schaefer
 */
public interface IASTPreprocessorMacroDefinition extends
		IASTPreprocessorStatement, IASTNameOwner {

	/**
	 * <code>MACRO_NAME</code> describes the relationship between a macro
	 * definition and it's name.
	 */
	public static final ASTNodeProperty MACRO_NAME = new ASTNodeProperty(
			"IASTPreprocessorMacroDefinition.MACRO_NAME - Macro Name"); //$NON-NLS-1$

	/**
	 * Get the macro name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * Set the macro name.
	 * 
	 * @param name
	 */
	public void setName(IASTName name);

	/**
	 * Get the macro expansion.
	 * 
	 * @return String
	 */
	public String getExpansion();

	/**
	 * Set the macro expansion.
	 * 
	 * @param exp
	 *            String
	 */
	public void setExpansion(String exp);
}
