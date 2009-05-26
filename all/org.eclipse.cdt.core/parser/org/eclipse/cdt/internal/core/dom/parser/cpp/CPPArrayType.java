/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 13, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author aniefer
 */
public class CPPArrayType implements IArrayType, ITypeContainer {
    private IType type;
    private IASTExpression sizeExpression;
    
    public CPPArrayType(IType type) {
        this.type = type;
    }
    
    public CPPArrayType(IType type, IASTExpression sizeExp) {
        this.type = type;
        this.sizeExpression = sizeExp;
    }
    
    public IType getType() {
        return type;
    }
    
    public void setType(IType t) {
        this.type = t;
    }
    
    public boolean isSameType(IType obj) {
        if (obj == this)
            return true;
        if (obj instanceof ITypedef)
            return ((ITypedef) obj).isSameType(this);
        
        if (obj instanceof IArrayType) {
            try {
            	IType objType = ((IArrayType) obj).getType();
            	if (objType != null)
            		return objType.isSameType(type);
            } catch (DOMException e) {
                return false;
            }
        }
    	return false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IArrayType#getArraySizeExpression()
     */
    public IASTExpression getArraySizeExpression() {
        return sizeExpression;
    }

    @Override
	public Object clone() {
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            // Not going to happen
        }
        return t;
    }

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
