/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * If statement in c++
 */
public class CPPASTIfStatement extends ASTNode implements ICPPASTIfStatement, IASTAmbiguityParent {
	
    private IASTExpression condition;
    private IASTStatement thenClause;
    private IASTStatement elseClause;
    private IASTDeclaration condDecl;
    private IScope scope;
    
    
    public CPPASTIfStatement() {
	}

	public CPPASTIfStatement(IASTDeclaration condition, IASTStatement thenClause, IASTStatement elseClause) {
		setConditionDeclaration(condition);
		setThenClause(thenClause);
		setElseClause(elseClause);
	}
    
    public CPPASTIfStatement(IASTExpression condition, IASTStatement thenClause, IASTStatement elseClause) {
		setConditionExpression(condition);
		setThenClause(thenClause);
		setElseClause(elseClause);
	}
    
    public CPPASTIfStatement copy() {
		CPPASTIfStatement copy = new CPPASTIfStatement();
		copy.setConditionDeclaration(condDecl == null ? null : condDecl.copy());
		copy.setConditionExpression(condition == null ? null : condition.copy());
		copy.setThenClause(thenClause == null ? null : thenClause.copy());
		copy.setElseClause(elseClause == null ? null : elseClause.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}

    
	public IASTExpression getConditionExpression() {
        return condition;
    }

    public void setConditionExpression(IASTExpression condition) {
        assertNotFrozen();
        this.condition = condition;
        if (condition != null) {
			condition.setParent(this);
			condition.setPropertyInParent(CONDITION);
			condDecl= null;
		}
    }

    public IASTStatement getThenClause() {
        return thenClause;
    }

    public void setThenClause(IASTStatement thenClause) {
        assertNotFrozen();
        this.thenClause = thenClause;
        if (thenClause != null) {
			thenClause.setParent(this);
			thenClause.setPropertyInParent(THEN);
		}
    }

    public IASTStatement getElseClause() {
        return elseClause;
    }

    public void setElseClause(IASTStatement elseClause) {
        assertNotFrozen();
        this.elseClause = elseClause;
        if (elseClause != null) {
			elseClause.setParent(this);
			elseClause.setPropertyInParent(ELSE);
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
        if( condDecl != null )  if( !condDecl.accept( action )) return false;
        if( thenClause != null ) if( !thenClause.accept( action ) ) return false;
        if( elseClause != null ) if( !elseClause.accept( action ) ) return false;
        
        if( action.shouldVisitStatements ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        return true;
    }
    
	public void replace(IASTNode child, IASTNode other) {
		if (thenClause == child) {
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
			thenClause = (IASTStatement) other;
		} else if (elseClause == child) {
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
			elseClause = (IASTStatement) other;
		} else if (condition == child || condDecl == child) {
			if (other instanceof IASTExpression) {
				setConditionExpression((IASTExpression) other);
			} else if (other instanceof IASTDeclaration) {
				setConditionDeclaration((IASTDeclaration) other);
			}
		}
	}

    public IASTDeclaration getConditionDeclaration() {
        return condDecl;
    }

    public void setConditionDeclaration(IASTDeclaration d) {
        assertNotFrozen();
        condDecl = d;
        if (d != null) {
			d.setParent(this);
			d.setPropertyInParent(CONDITION);
			condition= null;
		}
    }
    
	public IScope getScope() {
		if( scope == null )
            scope = new CPPBlockScope( this );
        return scope;	
    }
}
