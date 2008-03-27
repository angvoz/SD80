/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *
 *******************************************************************************/
/*
 * Created on Apr 29, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public abstract class CPPSpecialization extends PlatformObject
		implements ICPPSpecialization, ICPPInternalBinding {
	private IBinding specialized;
	private ICPPScope scope;
	protected ObjectMap argumentMap;

	private IASTNode definition;
	private IASTNode[] declarations;

	public CPPSpecialization(IBinding specialized, ICPPScope scope, ObjectMap argumentMap) {
		this.specialized = specialized;
		this.scope = scope;
		this.argumentMap = argumentMap;

		if (specialized instanceof ICPPInternalBinding) {
			definition = ((ICPPInternalBinding) specialized).getDefinition();
			IASTNode[] decls = ((ICPPInternalBinding) specialized).getDeclarations();
			if (decls != null && decls.length > 0)
				declarations = new IASTNode[] { decls[0] };
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization#getSpecializedBinding()
	 */
	public IBinding getSpecializedBinding() {
		return specialized;
	}

	public IASTNode[] getDeclarations() {
		return declarations;
	}

	public IASTNode getDefinition() {
		return definition;
	}

	public void addDefinition(IASTNode node) {
		definition = node;
	}

	public void addDeclaration(IASTNode node) {
		if (declarations == null) {
	        declarations = new IASTNode[] { node };
		} else {
	        // keep the lowest offset declaration in [0]
			if (declarations.length > 0 &&
					((ASTNode) node).getOffset() < ((ASTNode) declarations[0]).getOffset()) {
				declarations = (IASTNode[]) ArrayUtil.prepend(IASTNode.class, declarations, node);
			} else {
				declarations = (IASTNode[]) ArrayUtil.append(IASTNode.class, declarations, node);
			}
	    }
	}

	public void removeDeclaration(IASTNode node) {
		if (node == definition) {
			definition = null;
			return;
		}
		ArrayUtil.remove(declarations, node);
	}

	public String getName() {
		return specialized.getName();
	}

	public char[] getNameCharArray() {
		return specialized.getNameCharArray();
	}

	public IScope getScope() {
		return scope;
	}

	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName(this);
	}

	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	public boolean isGloballyQualified() throws DOMException {
		return ((ICPPInternalBinding) specialized).isGloballyQualified();
	}

	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	public ObjectMap getArgumentMap() {
		return argumentMap;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(getName());
		if (argumentMap != null) {
			result.append(" "); //$NON-NLS-1$
			result.append(argumentMap.toString());
		}
		return result.toString();
	}
}
