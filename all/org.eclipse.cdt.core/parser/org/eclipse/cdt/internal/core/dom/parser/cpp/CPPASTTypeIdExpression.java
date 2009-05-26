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
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * @author jcamelon
 */
public class CPPASTTypeIdExpression extends ASTNode implements ICPPASTTypeIdExpression {

    private int op;
    private IASTTypeId typeId;

    public CPPASTTypeIdExpression() {
	}

	public CPPASTTypeIdExpression(int op, IASTTypeId typeId) {
		this.op = op;
		setTypeId(typeId);
	}

	public CPPASTTypeIdExpression copy() {
		CPPASTTypeIdExpression copy = new CPPASTTypeIdExpression(op, typeId == null ? null : typeId.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public int getOperator() {
        return op;
    }

    public void setOperator(int value) {
        assertNotFrozen();
        this.op = value;
    }

    public void setTypeId(IASTTypeId typeId) {
        assertNotFrozen();
       this.typeId = typeId;
       if (typeId != null) {
    	   typeId.setParent(this);
    	   typeId.setPropertyInParent(TYPE_ID);
       } 
    }

    public IASTTypeId getTypeId() {
        return typeId;
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
      
        if( typeId != null ) if( !typeId.accept( action ) ) return false;
        
        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
    
	public IType getExpressionType() {
		switch (getOperator()) {
		case IASTTypeIdExpression.op_sizeof:
			return CPPVisitor.get_SIZE_T(this);
		case IASTTypeIdExpression.op_typeid:
			return CPPVisitor.get_type_info(this);
		}
		return CPPVisitor.createType(getTypeId());
	}
}
