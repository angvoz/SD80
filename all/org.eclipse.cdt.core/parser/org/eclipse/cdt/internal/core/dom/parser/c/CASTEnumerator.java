/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTEnumerator extends CASTNode implements IASTEnumerator, IASTAmbiguityParent {

    private IASTName name;
    private IASTExpression value;


    public CASTEnumerator() {
	}

	public CASTEnumerator(IASTName name, IASTExpression value) {
		setName(name);
		setValue(value);
	}

	public void setName(IASTName name) {
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ENUMERATOR_NAME);
		}
    }

    public IASTName getName() {
        return name;
    }

    public void setValue(IASTExpression expression) {
        this.value = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(ENUMERATOR_VALUE);
		}
    }

    public IASTExpression getValue() {
        return value;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitEnumerators ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( name != null ) if( !name.accept( action ) ) return false;
        if( value != null ) if( !value.accept( action ) ) return false;
        if( action.shouldVisitEnumerators ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }


	public int getRoleForName(IASTName n) {
		if( n == name )return r_definition;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
        if( child == value)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            value  = (IASTExpression) other;
        }
    }

}
