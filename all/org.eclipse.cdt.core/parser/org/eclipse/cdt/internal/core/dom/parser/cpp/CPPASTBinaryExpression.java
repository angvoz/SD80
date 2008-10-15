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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * @author jcamelon
 */
public class CPPASTBinaryExpression extends ASTNode implements
        ICPPASTBinaryExpression, IASTAmbiguityParent {

    private int op;
    private IASTExpression operand1;
    private IASTExpression operand2;

    public CPPASTBinaryExpression() {
	}

	public CPPASTBinaryExpression(int op, IASTExpression operand1, IASTExpression operand2) {
		this.op = op;
		setOperand1(operand1);
		setOperand2(operand2);
	}

	public int getOperator() {
        return op;
    }

    public IASTExpression getOperand1() {
        return operand1;
    }

    public IASTExpression getOperand2() {
        return operand2;
    }

    public void setOperator(int op) {
        this.op = op;
    }

    public void setOperand1(IASTExpression expression) {
        operand1 = expression;   
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND_ONE);
		}
    }

    public void setOperand2(IASTExpression expression) {
        operand2 = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND_TWO);
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( operand1 != null ) if( !operand1.accept( action ) ) return false;
        if( operand2 != null ) if( !operand2.accept( action ) ) return false;
        
        if(action.shouldVisitExpressions ){
        	switch( action.leave( this ) ){
        		case ASTVisitor.PROCESS_ABORT : return false;
        		case ASTVisitor.PROCESS_SKIP  : return true;
        		default : break;
        	}
        }
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == operand1 )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            operand1  = (IASTExpression) other;
        }
        if( child == operand2 )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            operand2  = (IASTExpression) other;
        }
    }

    public IType getExpressionType() {
    	return CPPVisitor.getExpressionType(this);
    }
    
}
