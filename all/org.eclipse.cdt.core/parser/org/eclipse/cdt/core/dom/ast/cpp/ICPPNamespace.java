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
 * Created on Dec 1, 2004
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * This interface represents a C++ namespace
 * 
 * @author aniefer
 */
public interface ICPPNamespace extends ICPPBinding {
	/**
	 * get the scope object associated with this namespace
	 * 
	 * @return
	 * @throws DOMException
	 */
	public ICPPNamespaceScope getNamespaceScope() throws DOMException;
	
	/**
	 * get an array of the all the bindings declared in this namespace.
	 * @return
	 * @throws DOMException
	 */
	public IBinding [] getMemberBindings() throws DOMException;
}
