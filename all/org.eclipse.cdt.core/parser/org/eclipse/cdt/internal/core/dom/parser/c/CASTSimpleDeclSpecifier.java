/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public class CASTSimpleDeclSpecifier extends CASTBaseDeclSpecifier implements ICASTSimpleDeclSpecifier,
		IASTAmbiguityParent {
    
    private int simpleType;
    private boolean isSigned;
    private boolean isUnsigned;
    private boolean isShort;
    private boolean isLong;
    private boolean longlong;
    private boolean complex=false;
    private boolean imaginary=false;
	private IASTExpression fDeclTypeExpression;

    public CASTSimpleDeclSpecifier copy() {
		CASTSimpleDeclSpecifier copy = new CASTSimpleDeclSpecifier();
		copySimpleDeclSpec(copy);
		return copy;
	}
    
    protected void copySimpleDeclSpec(CASTSimpleDeclSpecifier copy) {
    	copyBaseDeclSpec(copy);
    	copy.simpleType = simpleType;
    	copy.isSigned = isSigned;
    	copy.isUnsigned = isUnsigned;
    	copy.isShort = isShort;
    	copy.isLong = isLong;
    	copy.longlong = longlong;
    	copy.complex = complex;
    	copy.imaginary = imaginary;
    	if (fDeclTypeExpression != null)
    		copy.setDeclTypeExpression(fDeclTypeExpression.copy());
    }
    
    public int getType() {
        return simpleType;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public boolean isUnsigned() {
        return isUnsigned;
    }

    public boolean isShort() {
        return isShort;
    }

    public boolean isLong() {
        return isLong;
    }
    
    public void setType(int type) {
        assertNotFrozen();
        simpleType = type;
    }
    
    public void setType(Kind kind) {
    	setType(getType(kind));
    }
    
    private int getType(Kind kind) {
    	switch(kind) {
    	case eBoolean:
    		return t_bool;
		case eChar:
		case eWChar:
		case eChar16:
		case eChar32:
			return t_char;
		case eDouble:
			return t_double;
		case eFloat:
			return t_float;
		case eInt:
			return t_int;
		case eUnspecified:
			return t_unspecified;
		case eVoid:
			return t_void;
    	}
    	return t_unspecified;
    }
    
    public void setShort(boolean value) {
        assertNotFrozen();
        isShort = value;
    }
    
    public void setLong(boolean value) {
        assertNotFrozen();
        isLong = value;
    }
    
    public void setUnsigned(boolean value) {
        assertNotFrozen();
        isUnsigned = value;
    }
    
    public void setSigned(boolean value) {
        assertNotFrozen();
        isSigned = value;
    }

    public boolean isLongLong() {
        return longlong;
    }

    public void setLongLong(boolean value) {
        assertNotFrozen();
        longlong = value;
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}

        if (fDeclTypeExpression != null && !fDeclTypeExpression.accept(action))
			return false;

        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

	public boolean isComplex() {
		return complex;
	}

	public void setComplex(boolean value) {
        assertNotFrozen();
		this.complex = value;
	}

	public boolean isImaginary() {
		return imaginary;
	}

	public void setImaginary(boolean value) {
        assertNotFrozen();
		this.imaginary = value;		
	}

	public IASTExpression getDeclTypeExpression() {
		return fDeclTypeExpression;
	}

	public void setDeclTypeExpression(IASTExpression expression) {
        assertNotFrozen();
        fDeclTypeExpression= expression;
        if (expression != null) {
        	expression.setPropertyInParent(DECLTYPE_EXPRESSION);
        	expression.setParent(this);
        }
	}
	
	public void replace(IASTNode child, IASTNode other) {
		if (child == fDeclTypeExpression) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fDeclTypeExpression= (IASTExpression) other;
		}
	}
}
