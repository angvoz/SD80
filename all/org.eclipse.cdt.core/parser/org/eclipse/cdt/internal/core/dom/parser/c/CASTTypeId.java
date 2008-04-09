/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * @author jcamelon
 */
public class CASTTypeId extends CASTNode implements IASTTypeId {

    private IASTDeclSpecifier declSpecifier;
    private IASTDeclarator declarator;

    public CASTTypeId() {
	}

	public CASTTypeId(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator) {
		setDeclSpecifier(declSpecifier);
		setAbstractDeclarator(declarator);
	}

	public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }

    public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        this.declSpecifier = declSpec;
        if (declSpec != null) {
			declSpec.setParent(this);
			declSpec.setPropertyInParent(DECL_SPECIFIER);
		}
    }

    public IASTDeclarator getAbstractDeclarator() {
        return declarator;
    }

    public void setAbstractDeclarator(IASTDeclarator abstractDeclarator) {
        declarator = abstractDeclarator;
        if (abstractDeclarator != null) {
			abstractDeclarator.setParent(this);
			abstractDeclarator.setPropertyInParent(ABSTRACT_DECLARATOR);
		}

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
        
        if( declSpecifier != null ) if( !declSpecifier.accept( action ) ) return false;
        if( declarator != null ) if( !declarator.accept( action ) ) return false;

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
