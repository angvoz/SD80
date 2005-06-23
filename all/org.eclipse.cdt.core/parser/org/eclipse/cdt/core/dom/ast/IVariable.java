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
 * @author Doug Schaefer
 */
public interface IVariable extends IBinding {

	/**
	 * @return the type of the variable
	 */
	public IType getType() throws DOMException;
	
	
	/**
	 * Does this function have the static storage-class specifier
	 * similarily for extern, auto, register
	 * @return
	 * @throws DOMException
	 */
	public boolean isStatic() throws DOMException;
	public boolean isExtern() throws DOMException;
	public boolean isAuto() throws DOMException;
	public boolean isRegister() throws DOMException;
}
