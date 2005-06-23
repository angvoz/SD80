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
 * Represents a goto statement.
 * 
 * @author Doug Schaefer
 */
public interface IASTGotoStatement extends IASTStatement, IASTNameOwner {

	public static final ASTNodeProperty NAME = new ASTNodeProperty("IASTGotoStatement.NAME - Name for IASTGotoStatement"); //$NON-NLS-1$

	/**
	 * Returns the name of the label. The name resolves to a ILabel binding.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * Set the name for a goto statement label.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setName(IASTName name);

}
