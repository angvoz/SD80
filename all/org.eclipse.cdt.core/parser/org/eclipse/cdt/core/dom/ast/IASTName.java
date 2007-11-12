/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;

/**
 * This class represents a name in the program that represents a semantic object
 * in the program.
 * 
 * The toString method produces a string representation of the name as
 * appropriate for the language.
 * 
 * @author Doug Schaefer
 */
public interface IASTName extends IASTNode, IName {

	/**
	 * Constant sentinel.
	 */
	public static final IASTName[] EMPTY_NAME_ARRAY = new IASTName[0];

	/**
	 * Get the semantic object attached to this name.  May be null if this name
	 * has not yet been semantically resolved (@see resolveBinding)
	 * @return <code>IBinding</code> if it has been resolved, otherwise null 
	 */
	public IBinding getBinding();
		
	/** 
	 * Set the semantic object for this name to be the given binding
	 * @param binding
	 */
	public void setBinding( IBinding binding );
	
	/**
	 * Resolve the semantic object this name is referring to.
	 * 
	 * @return <code>IBinding</code> binding
	 */
	public IBinding resolveBinding();

	/**
	 * Return the completion context for this name.
	 * 
	 * @return <code>IASTCompletionContext</code> the context for completion
	 */
	public IASTCompletionContext getCompletionContext();
	
	/**
	 * Determines the current linkage in which the name has to be resolved.
	 */
	public ILinkage getLinkage();
	
	/**
	 * Returns the image location for this name or <code>null</code> if the information is not available.
	 * <p>
	 * An image location can be computed when the name is either found directly in the code, is (part of) 
	 * an argument to a macro expansion or is (part of) a macro definition found in the source code.
	 * <p>
	 * The image location is <code>null</code>, when the name consists of multiple tokens (qualified names)
	 * and the tokens are not found side by side in the code or if 
	 * the name is the result of a token-paste operation or the name is found in the definition of a 
	 * built-in macro.
	 * @since 5.0
	 */
	public IASTImageLocation getImageLocation();
}
