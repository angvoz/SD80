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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTWhileStatement extends CPPASTNode implements
        ICPPASTWhileStatement, IASTAmbiguityParent {
	
    private IASTExpression condition;
    private IASTStatement body;
    private IASTDeclaration condition2;
    private IScope scope;

    public CPPASTWhileStatement() {
	}

	public CPPASTWhileStatement(IASTDeclaration condition, IASTStatement body) {
    	setConditionDeclaration(condition);
		setBody(body);
	}
    
    public CPPASTWhileStatement(IASTExpression condition, IASTStatement body) {
		setCondition(condition);
		setBody(body);
	}

	public IASTExpression getCondition() {
        return condition;
    }

    public void setCondition(IASTExpression condition) {
        this.condition = condition;
        if (condition != null) {
			condition.setParent(this);
			condition.setPropertyInParent(CONDITIONEXPRESSION);
		}
    }

    public IASTStatement getBody() {
        return body;
    }

    public void setBody(IASTStatement body) {
        this.body = body;
        if (body != null) {
			body.setParent(this);
			body.setPropertyInParent(BODY);
		}
    }

	public IASTDeclaration getConditionDeclaration() {
		return condition2;
	}

	public void setConditionDeclaration(IASTDeclaration declaration) {
		condition2 = declaration;
		if (declaration != null) {
			declaration.setParent(this);
			declaration.setPropertyInParent(CONDITIONDECLARATION);
		}
	}

	@Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitStatements ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( condition != null ) if( !condition.accept( action ) ) return false;
        if( condition2 != null ) if( !condition2.accept( action ) ) return false;
        if( body != null ) if( !body.accept( action ) ) return false;
        
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
        if( body == child )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            body = (IASTStatement) other;
        }
        if( child == condition )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            condition  = (IASTExpression) other;
        }
        if( condition2 == child )
        {
            other.setParent( child.getParent() );
            other.setPropertyInParent( child.getPropertyInParent() );
            condition2 = (IASTDeclaration) other;
        }
    }

	public IScope getScope() {
		if( scope == null )
            scope = new CPPBlockScope( this );
        return scope;	
    }

}
