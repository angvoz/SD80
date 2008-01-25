/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.dom.parser.c.ICInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.c.ICInternalFunction;
import org.eclipse.cdt.internal.core.dom.parser.c.ICInternalVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalVariable;

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

	public static void addBinding(IScope scope, IBinding binding) throws DOMException {
		if (scope instanceof IASTInternalScope) {
			((IASTInternalScope) scope).addBinding(binding);
		}		
	}

	public static void removeBinding(IScope scope, IBinding binding) throws DOMException {
		if (scope instanceof IASTInternalScope) {
			((IASTInternalScope) scope).removeBinding(binding);
		}		
	}
	
	public static void addName(IScope scope, IASTName name) throws DOMException {
		if (scope instanceof IASTInternalScope) {
			((IASTInternalScope) scope).addName(name);
		}		
	}		
	
	public static boolean isStatic(IFunction func, boolean resolveAll) throws DOMException {
		if (func instanceof ICPPInternalFunction) {
			return ((ICPPInternalFunction)func).isStatic(resolveAll);
		}
		if (func instanceof ICInternalFunction) {
			return ((ICInternalFunction) func).isStatic(resolveAll);
		}
		return func.isStatic();
	}

	public static boolean isStatic(IVariable var) throws DOMException {
		if (var instanceof ICPPInternalVariable) {
			return ((ICPPInternalVariable)var).isStatic();
		}
		if (var instanceof ICInternalVariable) {
			return ((ICInternalVariable)var).isStatic();
		}
		return var.isStatic();
	}

	public static void setFullyResolved(IBinding binding, boolean val) {
		if (binding instanceof ICInternalFunction) {
			((ICInternalFunction) binding).setFullyResolved(true);
		}
	}

	public static String getDeclaredInSourceFileOnly(IBinding binding) {
		IASTNode[] decls;
		IASTNode def;
		if (binding instanceof ICPPInternalBinding) {
			ICPPInternalBinding ib= (ICPPInternalBinding) binding;
			decls= ib.getDeclarations();
			def= ib.getDefinition();
		}
		else if (binding instanceof ICInternalBinding) {
			ICInternalBinding ib= (ICInternalBinding) binding;
			decls= ib.getDeclarations();
			def= ib.getDefinition();
		}
		else {
			return null;
		}
		String filePath= null;
		if (def != null) {
			if ( (filePath= isPartOfSource(filePath, def)) == null) {
				return null;
			}
		}
		if (decls != null) {
			for (int i = 0; i < decls.length; i++) {
				final IASTNode node= decls[i];
				if (node != null) {
					if ( (filePath= isPartOfSource(filePath, node)) == null) {
						return null;
					}
				}
			}
		}
		return filePath;
	}

	private static String isPartOfSource(String filePath, IASTNode decl) {
		IASTTranslationUnit tu;
		if (filePath == null) {
			tu= decl.getTranslationUnit();
			if (tu.isHeaderUnit()) {
				return null;
			}
			filePath= tu.getFilePath();
		}
		if (!filePath.equals(decl.getContainingFilename())) {
			return null;
		}
		return filePath;
	}
}
