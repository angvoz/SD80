/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;

/**
 * Represents the binding for a dependent name within a template declaration.
 */
public interface ICPPUnknownBinding extends ICPPBinding {
	ICPPUnknownBinding[] EMPTY_UNKNOWN_BINDING_ARRAY = {};

	/**
	 * Returns the scope this binding represents.
	 * @throws DOMException 
	 */
    public ICPPScope asScope() throws DOMException;

	/**
	 * Returns a the name of the unknown binding that has to be searched in the parent scope.
	 * The ast-node may not be rooted in an ast-tree. May be <code>null</code>.
	 */
	public IASTName getUnknownName();
}
