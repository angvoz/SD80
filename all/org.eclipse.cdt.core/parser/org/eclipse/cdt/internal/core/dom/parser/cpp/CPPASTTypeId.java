/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Type id for c++
 */
public class CPPASTTypeId extends ASTNode implements ICPPASTTypeId {

    private IASTDeclSpecifier declSpec;
    private IASTDeclarator absDecl;
    private boolean isPackExpansion;

    
    public CPPASTTypeId() {
	}

	public CPPASTTypeId(IASTDeclSpecifier declSpec, IASTDeclarator absDecl) {
		setDeclSpecifier(declSpec);
		setAbstractDeclarator(absDecl);
	}
	
	public CPPASTTypeId copy() {
		CPPASTTypeId copy = new CPPASTTypeId();
		copy.setDeclSpecifier(declSpec == null ? null : declSpec.copy());
		copy.setAbstractDeclarator(absDecl == null ? null : absDecl.copy());
		copy.setOffsetAndLength(this);
		copy.isPackExpansion= isPackExpansion;
		return copy;
	}

	public IASTDeclSpecifier getDeclSpecifier() {
        return declSpec;
    }

    public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        assertNotFrozen();
        this.declSpec = declSpec;
        if (declSpec != null) {
			declSpec.setParent(this);
			declSpec.setPropertyInParent(DECL_SPECIFIER);
		}
    }

    public IASTDeclarator getAbstractDeclarator() {
        return absDecl;
    }


    public void setAbstractDeclarator(IASTDeclarator abstractDeclarator) {
        assertNotFrozen();
        this.absDecl = abstractDeclarator;
        if (abstractDeclarator != null) {
			abstractDeclarator.setParent(this);
			abstractDeclarator.setPropertyInParent(ABSTRACT_DECLARATOR);
		}
    }

    public boolean isPackExpansion() {
		return isPackExpansion;
	}

	public void setIsPackExpansion(boolean val) {
		isPackExpansion= val;
	}

	@Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitTypeIds ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( declSpec != null ) if( !declSpec.accept( action ) ) return false;
        if( absDecl != null ) if( !absDecl.accept( action ) ) return false;

        if( action.shouldVisitTypeIds ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
}
