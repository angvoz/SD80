/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTSimpleTypeConstructorExpression extends ASTNode implements
        ICPPASTSimpleTypeConstructorExpression, IASTAmbiguityParent {

    private int st;
    private IASTExpression init;

    public CPPASTSimpleTypeConstructorExpression() {
	}

	public CPPASTSimpleTypeConstructorExpression(int st, IASTExpression init) {
		this.st = st;
		setInitialValue(init);
	}

	public CPPASTSimpleTypeConstructorExpression copy() {
		CPPASTSimpleTypeConstructorExpression copy = new CPPASTSimpleTypeConstructorExpression();
		copy.st = st;
		copy.setInitialValue(init == null ? null : init.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public int getSimpleType() {
        return st;
    }

    public void setSimpleType(int value) {
        assertNotFrozen();
        st = value;
    }

    public IASTExpression getInitialValue() {
        return init;
    }

    public void setInitialValue(IASTExpression expression) {
        assertNotFrozen();
        init = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(INITIALIZER_VALUE);
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
        
        if( init != null ) if( !init.accept( action ) ) return false;
        
        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == init )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            init  = (IASTExpression) other;
        }        
    }
    
    public IType getExpressionType() {
    	return new CPPBasicType(st, 0);
    }
}
