/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTCatchHandler extends CPPASTNode implements
        ICPPASTCatchHandler, IASTAmbiguityParent {

    private boolean isCatchAll;
    private IASTStatement body;
    private IASTDeclaration declaration;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler#setIsCatchAll(boolean)
     */
    public void setIsCatchAll(boolean isEllipsis) {
        isCatchAll = isEllipsis;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler#isCatchAll()
     */
    public boolean isCatchAll() {
        return isCatchAll;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler#setCatchBody(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setCatchBody(IASTStatement compoundStatement) {
        body = compoundStatement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler#getCatchBody()
     */
    public IASTStatement getCatchBody() {
        return body;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler#setDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public void setDeclaration(IASTDeclaration decl) {
        declaration = decl;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler#getDeclaration()
     */
    public IASTDeclaration getDeclaration() {
        return declaration;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitStatements ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( declaration != null ) if( !declaration.accept( action ) ) return false;
        if( body != null ) if( !body.accept( action ) ) return false;
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( body == child )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            body = (IASTStatement) other;
        }
        if( declaration == child )
        {
            other.setParent( child.getParent() );
            other.setPropertyInParent( child.getPropertyInParent() );
            declaration = (IASTDeclaration) other;
        }

    }
}
