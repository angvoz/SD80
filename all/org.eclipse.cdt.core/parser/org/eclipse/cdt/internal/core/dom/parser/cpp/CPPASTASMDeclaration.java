/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author jcamelon
 */
public class CPPASTASMDeclaration extends ASTNode implements IASTASMDeclaration {
    char [] assembly = null;

    public CPPASTASMDeclaration() {
	}

	public CPPASTASMDeclaration(String assembly) {
		setAssembly(assembly);
	}

	public CPPASTASMDeclaration copy() {
		CPPASTASMDeclaration copy = new CPPASTASMDeclaration();
		copy.assembly = assembly.clone();
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public String getAssembly() {
        if( assembly == null ) 
        	return ""; //$NON-NLS-1$
        return new String( assembly );
    }

    public void setAssembly(String assembly) {
        assertNotFrozen();
        this.assembly = assembly.toCharArray();
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
        if( action.shouldVisitDeclarations ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

}
