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
 * This represents an initializer for a declarator.
 * 
 * @author Doug Schaefer
 */
public interface IASTInitializer extends IASTNode {

	/**
	 * Constant.
	 */
	public final static IASTInitializer[] EMPTY_INITIALIZER_ARRAY = new IASTInitializer[0];

}
