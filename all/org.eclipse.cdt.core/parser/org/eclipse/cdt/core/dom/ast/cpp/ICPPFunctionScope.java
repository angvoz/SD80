/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * @author aniefer
 */
public interface ICPPFunctionScope extends ICPPScope {

	/**
	 * Get the scope representing the function body. returns null if there is no
	 * function definition
	 * 
	 * @return
	 * @throws DOMException
	 */
	public IScope getBodyScope() throws DOMException;
}
