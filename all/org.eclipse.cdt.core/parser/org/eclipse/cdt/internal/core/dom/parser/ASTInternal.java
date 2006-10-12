/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;

/**
 * Access to methods on scopes and bindings internal to the parser.
 * @since 4.0
 */
public class ASTInternal {

	public static IASTNode[] getDeclarationsOfBinding(IBinding binding) {
		if( binding instanceof ICPPInternalBinding ) {
			return ((ICPPInternalBinding)binding).getDeclarations();
		}
		assert false;
		return IASTNode.EMPTY_NODE_ARRAY;
	}

	public static IASTNode getPhysicalNodeOfScope(IScope scope) throws DOMException {
		if (scope instanceof IASTInternalScope) {
			return ((IASTInternalScope) scope).getPhysicalNode();
		}
		assert false;
		return null;
	}

	public static void flushCache(IScope scope) throws DOMException {
		if (scope instanceof IASTInternalScope) {
			((IASTInternalScope) scope).flushCache();
		}
	}

	public static boolean isFullyCached(IScope scope) throws DOMException {
		if (scope instanceof IASTInternalScope) {
			return ((IASTInternalScope) scope).isFullyCached();
		}
		return true;
	}

	public static void setFullyCached(IScope scope, boolean val) throws DOMException {
		if (scope instanceof IASTInternalScope) {
			((IASTInternalScope) scope).setFullyCached(val);
		}
	}
}
