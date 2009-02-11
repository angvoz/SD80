/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * Pointers in c++
 */
public class CPPPointerType implements IPointerType, ITypeContainer {
	protected IType type = null;
	private boolean isConst = false;
	private boolean isVolatile = false;
	
	public CPPPointerType(IType type, IASTPointer operator) {
		this.type = type;
		this.isConst = operator.isConst();
		this.isVolatile = operator.isVolatile();
	}

	public CPPPointerType(IType type, boolean isConst, boolean isVolatile) {
		this.type = type;
		this.isConst = isConst;
		this.isVolatile = isVolatile;
	}
	
	public CPPPointerType(IType type) {
	    this.type = type;
	}

	public IType stripQualifiers() {
		CPPPointerType result = this;
		if (isConst || isVolatile) {
			result = (CPPPointerType) clone();
			result.isConst = false;
			result.isVolatile = false;
		}
		return result;
	}
	
	public boolean isSameType(IType o) {
	    if (o == this)
            return true;
        if (o instanceof ITypedef)
            return o.isSameType(this);
        
        if (!(o instanceof IPointerType))
        	return false;
        
	    if (this instanceof ICPPPointerToMemberType != o instanceof ICPPPointerToMemberType) 
	        return false;
	    
	    if (type == null)
	        return false;
	    
	    IPointerType pt = (IPointerType) o;
	    if (isConst == pt.isConst() && isVolatile == pt.isVolatile()) {
			try {
				return type.isSameType(pt.getType());
			} catch (DOMException e) {
			}
	    }
	    return false;
	}

	public IType getType() {
		return type;
	}
	
	public void setType(IType t) {
	    type = t;
	}

	public boolean isConst() {
		return isConst;
	}

	public boolean isVolatile() {
		return isVolatile;
	}
	
    @Override
	public Object clone() {
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch (CloneNotSupportedException e) {
            //not going to happen
        }
        return t;
    }

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
