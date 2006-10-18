/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jan 31, 2005
 */
package org.eclipse.cdt.core.dom.ast;


/**
 * This is the general purpose exception that is thrown for resolving semantic
 * aspects of an illegal binding.
 * 
 * @author aniefer
 */
public class DOMException extends Exception {
	
	private static final long serialVersionUID = 0;
	
	IProblemBinding problemBinding;

	/**
	 * @param problem
	 *            binding for throwing
	 * 
	 */
	public DOMException(IProblemBinding problem) {
		problemBinding = problem;
	}

	/**
	 * Get the problem associated w/this exception.
	 * 
	 * @return problem
	 */
	public IProblemBinding getProblem() {
		return problemBinding;
	}
}
