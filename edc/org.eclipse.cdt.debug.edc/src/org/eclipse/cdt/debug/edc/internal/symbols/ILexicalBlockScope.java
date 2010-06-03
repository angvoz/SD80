/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols;

import java.util.Collection;

import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IVariable;

/**
 * Interface representing a lexical block scope. A lexical block is a block of
 * code inside of a function. A lexical block may contain other lexical blocks
 * as children.
 */
public interface ILexicalBlockScope extends IScope {

	/**
	 * Gets the list of variables in this scope and any child scopes
	 * 
	 * @return unmodifiable list of variables which may be empty
	 */
	Collection<IVariable> getVariablesInTree();
	
}
