/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Mike Kucera (IBM) - implicit names
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpressionList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;


public class CPPASTExpressionList extends ASTNode implements ICPPASTExpressionList, IASTAmbiguityParent {


	/**
	 * Caution: may contain nulls. 
	 * @see CPPASTExpressionList#computeImplicitNames
	 */
	private IASTImplicitName[] implicitNames;

	private ICPPFunction[] overloads = null;
	
	
	public CPPASTExpressionList copy() {
		CPPASTExpressionList copy = new CPPASTExpressionList();
		for(IASTExpression expr : getExpressions())
			copy.addExpression(expr == null ? null : expr.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTExpression [] getExpressions() {
        if( expressions == null ) return IASTExpression.EMPTY_EXPRESSION_ARRAY;
        return (IASTExpression[]) ArrayUtil.trim( IASTExpression.class, expressions );
    }

    public void addExpression(IASTExpression expression) {
        assertNotFrozen();
        expressions = (IASTExpression [])ArrayUtil.append( IASTExpression.class, expressions, expression );
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(NESTED_EXPRESSION);
		}
    }

    private IASTExpression [] expressions = new IASTExpression[2];
    
    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
      
        IASTExpression[] exps = getExpressions();
        IASTImplicitName[] implicits = action.shouldVisitImplicitNames ? computeImplicitNames() : null;

        for(int i = 0, n = exps.length; i < n; i++) {
        	if(!exps[i].accept(action)) { 
        		return false;
        	}
        	if(i < n-1 && implicits != null && implicits[i] != null) {
        		if(!implicits[i].accept(action)) {
        			return false;
        		}
        	}
        }
        
        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		} 
        return true;
    }
    
    
    /**
     * Returns an array of implicit names where each element of the array
     * represents a comma between the expression in the same index and the
     * next expression. This array contains null elements as placeholders
     * for commas that do not resolve to overloaded operators.
     */
    private IASTImplicitName[] computeImplicitNames() {
		if(implicitNames == null) {
			IASTExpression[] exprs = getExpressions(); // has to be at least two
			if(exprs.length < 2)
				return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			
			implicitNames = new IASTImplicitName[exprs.length-1];
			
			ICPPFunction[] overloads = getOverloads();
			for(int i = 0; i < overloads.length; i++) {
				ICPPFunction overload = overloads[i];
				if(overload != null) {
					CPPASTImplicitName operatorName = new CPPASTImplicitName(OverloadableOperator.COMMA, this);
					operatorName.setBinding(overload);
					operatorName.computeOperatorOffsets(exprs[i], true);
					implicitNames[i] = operatorName;
				}
			}
		}
		
		return implicitNames;
	}
    
    
    public IASTImplicitName[] getImplicitNames() {
    	return (IASTImplicitName[])ArrayUtil.removeNulls(IASTImplicitName.class, computeImplicitNames());
    }
    
    
    private ICPPFunction[] getOverloads() {
    	if(overloads == null) {
	    	IASTExpression[] exprs = getExpressions();
	    	if(exprs.length < 2 || getPropertyInParent() == IASTFunctionCallExpression.PARAMETERS)
	    		return overloads = new ICPPFunction[0];
	    	
	    	overloads = new ICPPFunction[exprs.length-1];
	    	IType lookupType = exprs[0].getExpressionType();
	    	
	    	for(int i = 1; i < exprs.length; i++) {
	    		IASTExpression e1 = exprs[i-1], e2 = exprs[i];	
	    		ICPPFunction overload = CPPSemantics.findOverloadedOperatorComma(e1, e2, lookupType);
	    		if(overload == null) {
	    			lookupType = e2.getExpressionType();
	    		}
	    		else {
	    			overloads[i-1] = overload;
	    			try {
						lookupType = overload.getType().getReturnType();
					} catch (DOMException e) {
						lookupType = e2.getExpressionType();
					}
	    		}
			}
    	}
    	
    	return overloads;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( expressions == null ) return;
        for (int i = 0; i < expressions.length; ++i) {
            if (child == expressions[i]) {
                other.setPropertyInParent(child.getPropertyInParent());
                other.setParent(child.getParent());
                expressions[i] = (IASTExpression) other;
            }
        }
    }
    
    public IType getExpressionType() {
    	ICPPFunction[] overloads = getOverloads();
    	if(overloads.length > 0) {
    		ICPPFunction last = overloads[overloads.length-1];
    		if(last != null) {
    			try {
					return last.getType().getReturnType();
				} catch (DOMException e) { }
    		}
    	}
    	
    	for (int i = expressions.length-1; i >= 0 ; i--) {
			IASTExpression expr= expressions[i];
			if (expr != null)
				return expr.getExpressionType();
		}
    	return null;
    }
}
