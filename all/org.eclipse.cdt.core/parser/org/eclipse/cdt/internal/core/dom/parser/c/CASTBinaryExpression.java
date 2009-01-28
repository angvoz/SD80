/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Binary expression for c
 */
public class CASTBinaryExpression extends ASTNode implements
        IASTBinaryExpression, IASTAmbiguityParent {

    private int op;
    private IASTExpression operand1;
    private IASTExpression operand2;

    public CASTBinaryExpression() {
	}

	public CASTBinaryExpression(int op, IASTExpression operand1, IASTExpression operand2) {
		this.op = op;
		setOperand1(operand1);
		setOperand2(operand2);
	}
	
	public CASTBinaryExpression copy() {
		CASTBinaryExpression copy = new CASTBinaryExpression();
		copy.op = op;
		copy.setOperand1(operand1 == null ? null : operand1.copy());
		copy.setOperand2(operand2 == null ? null : operand2.copy());
		copy.setOffsetAndLength(this);
		return copy;
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

    /**
     * @param op An op_X field from {@link IASTBinaryExpression}
     */
    public void setOperator(int op) {
        assertNotFrozen();
        this.op = op;
    }

    public void setOperand1(IASTExpression expression) {
        assertNotFrozen();
        operand1 = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND_ONE);
		}
    }

    public void setOperand2(IASTExpression expression) {
        assertNotFrozen();
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
            operand1 = (IASTExpression) other;
        }
        if( child == operand2)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            operand2 = (IASTExpression) other;
        }
    }
    
    public IType getExpressionType() {
        int op = getOperator();
		try {
			switch(op) {
				case op_lessEqual:
				case op_lessThan:
				case op_greaterEqual:
				case op_greaterThan:
				case op_logicalAnd:
				case op_logicalOr:
				case op_equals:
				case op_notequals:
					CBasicType basicType = new CBasicType(IBasicType.t_int, 0);
			    	basicType.setValue(this);
			    	return basicType;
				case IASTBinaryExpression.op_plus:
					IType t2 = getOperand2().getExpressionType();
					if (CVisitor.unwrapTypedefs(t2) instanceof IPointerType) {
						return t2;
					}
					break;

				case IASTBinaryExpression.op_minus:
					t2= getOperand2().getExpressionType();
					if (CVisitor.unwrapTypedefs(t2) instanceof IPointerType) {
						IType t1 = getOperand1().getExpressionType();
						if (CVisitor.unwrapTypedefs(t1) instanceof IPointerType) {
			    			return CVisitor.getPtrDiffType(this);
						}
						return t1;
					}
					break;
			}
		} catch (DOMException e) {
			return e.getProblem();
		}
		return getOperand1().getExpressionType();
    }

}
