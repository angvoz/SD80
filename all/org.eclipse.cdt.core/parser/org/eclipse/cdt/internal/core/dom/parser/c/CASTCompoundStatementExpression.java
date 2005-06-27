/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;

/**
 * @author jcamelon
 */
public class CASTCompoundStatementExpression extends CASTNode implements
        IGNUASTCompoundStatementExpression {

    private IASTCompoundStatement statement;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.gcc.IGCCASTCompoundStatementExpression#getCompoundStatement()
     */
    public IASTCompoundStatement getCompoundStatement() {
        return statement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.gcc.IGCCASTCompoundStatementExpression#setCompoundStatement(org.eclipse.cdt.core.dom.ast.IASTCompoundStatement)
     */
    public void setCompoundStatement(IASTCompoundStatement statement) {
        this.statement = statement;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
     
        if( statement != null ) if( !statement.accept( action ) ) return false;
        return true;
    }
}
