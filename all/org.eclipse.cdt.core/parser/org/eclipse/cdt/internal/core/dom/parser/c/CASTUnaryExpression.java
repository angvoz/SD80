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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTUnaryExpression extends CASTNode implements
        IASTUnaryExpression, IASTAmbiguityParent {

    private int operator;
    private IASTExpression operand;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTUnaryExpression#getOperator()
     */
    public int getOperator() {
        return operator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTUnaryExpression#setOperator(int)
     */
    public void setOperator(int value) {
        this.operator = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTUnaryExpression#getOperand()
     */
    public IASTExpression getOperand() {
        return operand;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTUnaryExpression#setOperand(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setOperand(IASTExpression expression) {
        operand = expression;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
      
        if( operand != null ) if( !operand.accept( action ) ) return false;
        return true;
    }
    
    public void replace(IASTNode child, IASTNode other) {
        if( child == operand )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            operand = (IASTExpression) other;
        }
    }
}
