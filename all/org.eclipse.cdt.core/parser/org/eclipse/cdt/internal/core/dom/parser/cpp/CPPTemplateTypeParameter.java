/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.index.IIndexType;

/**
 * @author aniefer
 */
public class CPPTemplateTypeParameter extends CPPTemplateParameter implements
		ICPPTemplateTypeParameter, IType, ICPPInternalUnknown {
	private ICPPScope unknownScope;

	public CPPTemplateTypeParameter(IASTName name) {
		super(name);
	}

	public ICPPScope getUnknownScope() {
	    if (unknownScope == null) {
	    	IASTName n = null;
	    	IASTNode[] nodes = getDeclarations();
	    	if (nodes != null && nodes.length > 0)
	    		n = (IASTName) nodes[0];
	        unknownScope = new CPPUnknownScope(this, n);
	    }
	    return unknownScope;
	}

	public IType getDefault() {
		IASTNode[] nds = getDeclarations();
		if (nds == null || nds.length == 0)
		    return null;
		IASTName name = (IASTName) nds[0];
		ICPPASTSimpleTypeTemplateParameter simple = (ICPPASTSimpleTypeTemplateParameter) name.getParent();
		IASTTypeId typeId = simple.getDefaultType();
		if (typeId != null)
		    return CPPVisitor.createType(typeId);
		return null;
	}

    public boolean isSameType(IType type) {
        if (type == this)
            return true;
        if (type instanceof ITypedef || type instanceof IIndexType)
            return type.isSameType(this);
        return false;
    }

    public IBinding resolveUnknown(ObjectMap argMap) {
    	// Cannot do resolution here since the result is not necessarily a binding.
		return null;
    }
}
