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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * @author jcamelon
 */
public class CPPASTUsingDirective extends ASTNode implements
        ICPPASTUsingDirective, IASTCompletionContext {

    private IASTName name;

    public CPPASTUsingDirective() {
	}

	public CPPASTUsingDirective(IASTName name) {
		setQualifiedName(name);
	}

	public CPPASTUsingDirective copy() {
		CPPASTUsingDirective copy = new CPPASTUsingDirective(name == null ? null : name.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTName getQualifiedName() {
        return name;
    }

    public void setQualifiedName(IASTName qualifiedName) {
        assertNotFrozen();
        this.name = qualifiedName;
        if (qualifiedName != null) {
			qualifiedName.setParent(this);
			qualifiedName.setPropertyInParent(QUALIFIED_NAME);
		}

    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}

        if( name != null ) if( !name.accept( action ) ) return false;

        if( action.shouldVisitDeclarations ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }


	public int getRoleForName(IASTName n) {
		if( n == name )
			return r_reference;
		return r_unclear;
	}

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);
		List<IBinding> filtered = new ArrayList<IBinding>();

		for (int i = 0;i < bindings.length; i++) {
			if (bindings[i] instanceof ICPPNamespace) {
				filtered.add(bindings[i]);
			}
		}

		return filtered.toArray(new IBinding[filtered.size()]);
	}
}
