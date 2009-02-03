/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Emanuel Graf IFS - Bug 198269
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * For statement in c++
 */
public class CPPASTForStatement extends ASTNode implements ICPPASTForStatement, IASTAmbiguityParent {
    private IScope scope = null;
    
    private IASTStatement  init;
    private IASTExpression condition;
    private IASTDeclaration condDeclaration;
    private IASTExpression iterationExpression;
    private IASTStatement body;


    
    public CPPASTForStatement() {
	}

	public CPPASTForStatement(IASTStatement init,  IASTDeclaration condDeclaration,
			IASTExpression iterationExpression, IASTStatement body) {
    	setInitializerStatement(init);
    	setConditionDeclaration(condDeclaration);
    	setIterationExpression(iterationExpression);
    	setBody(body);
	}

    public CPPASTForStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body) {
    	setInitializerStatement(init);
    	setConditionExpression(condition);
    	setIterationExpression(iterationExpression);
    	setBody(body);
	}

    public CPPASTForStatement copy() {
		CPPASTForStatement copy = new CPPASTForStatement();
		copy.setInitializerStatement(init == null ? null : init.copy());
		copy.setConditionDeclaration(condDeclaration == null ? null : condDeclaration.copy());
		copy.setConditionExpression(condition == null ? null : condition.copy());
		copy.setIterationExpression(iterationExpression == null ? null : iterationExpression.copy());
		copy.setBody(body == null ? null : body.copy());
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
			condDeclaration= null;
		}
    }

    public IASTExpression getIterationExpression() {
        return iterationExpression;
    }

    public void setIterationExpression(IASTExpression iterator) {
        assertNotFrozen();
        this.iterationExpression = iterator;
        if (iterator != null) {
			iterator.setParent(this);
			iterator.setPropertyInParent(ITERATION);
		}
    }

    public IASTStatement getBody() {
        return body;
    }

    public void setBody(IASTStatement statement) {
        assertNotFrozen();
        body = statement;
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(BODY);
		}
    }

    public IScope getScope() {
        if( scope == null )
            scope = new CPPBlockScope( this );
        return scope;
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
        if( init != null ) if( !init.accept( action ) ) return false;
        if( condition != null ) if( !condition.accept( action ) ) return false;
        if( condDeclaration != null ) if( !condDeclaration.accept( action ) ) return false;
        if( iterationExpression != null ) if( !iterationExpression.accept( action ) ) return false;
        if( body != null ) if( !body.accept( action ) ) return false;
        
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
		if (body == child) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			body = (IASTStatement) other;
		} else if (child == condition || child == condDeclaration) {
			if (other instanceof IASTExpression) {
				setConditionExpression((IASTExpression) other);
			} else if (other instanceof IASTDeclaration) {
				setConditionDeclaration((IASTDeclaration) other);
			}
		} else if (child == iterationExpression) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			iterationExpression = (IASTExpression) other;
		} else if (child == init) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			init = (IASTStatement) other;
		}
	}

    public IASTStatement getInitializerStatement() {
        return init;
    }

    public void setInitializerStatement(IASTStatement statement) {
        assertNotFrozen();
        init = statement;
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(INITIALIZER);
		}
    }

    public void setConditionDeclaration(IASTDeclaration d) {
        assertNotFrozen();
        condDeclaration = d;
        if (d != null) {
			d.setParent(this);
			d.setPropertyInParent(CONDITION_DECLARATION);
			condition= null;
		}
    }

    public IASTDeclaration getConditionDeclaration() {
        return condDeclaration;
    }
}
