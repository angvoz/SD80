/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * @author jcamelon
 */
public class CPPASTNamedTypeSpecifier extends CPPASTBaseDeclSpecifier implements
        ICPPASTNamedTypeSpecifier, IASTCompletionContext {

    private boolean typename;
    private IASTName name;

    
    public CPPASTNamedTypeSpecifier() {
	}

	public CPPASTNamedTypeSpecifier(IASTName name) {
		setName(name);
	}

	public CPPASTNamedTypeSpecifier copy() {
		CPPASTNamedTypeSpecifier copy = new CPPASTNamedTypeSpecifier(name == null ? null : name.copy());
		copyBaseDeclSpec(copy);
		copy.typename = typename;
		return copy;
	}
	
	public boolean isTypename() {
        return typename;
    }

    public void setIsTypename(boolean value) {
        assertNotFrozen();
        typename = value;
    }

    public IASTName getName() {
        return name;
    }


    public void setName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(NAME);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitDeclSpecifiers) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT:
	            	return false;
	            case ASTVisitor.PROCESS_SKIP:
	            	return true;
	            default:
	            	break;
	        }
		}
        if (name != null && !name.accept(action))
        	return false;
        
        if (action.shouldVisitDeclSpecifiers ){
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT:
	            	return false;
	            case ASTVisitor.PROCESS_SKIP:
	            	return true;
	            default:
	            	break;
	        }
		}
        return true;
    }
	
	public int getRoleForName(IASTName n) {
		if (n == name)
			return r_reference;
		return r_unclear;
	}

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);
		List<IBinding> filtered = new ArrayList<IBinding>();

		for (int i = 0; i < bindings.length; i++) {
			if (bindings[i] instanceof ICPPClassType
					|| bindings[i] instanceof IEnumeration
					|| bindings[i] instanceof ICPPNamespace
					|| bindings[i] instanceof ITypedef
					|| bindings[i] instanceof ICPPTemplateTypeParameter) {
				filtered.add(bindings[i]);
			}
		}

		return filtered.toArray(new IBinding[filtered.size()]);
	}
}
