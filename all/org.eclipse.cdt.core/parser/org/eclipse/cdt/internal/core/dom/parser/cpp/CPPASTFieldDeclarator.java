/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Field declarator for c++.
 */
public class CPPASTFieldDeclarator extends CPPASTDeclarator implements
        ICPPASTFieldDeclarator, IASTAmbiguityParent {

    private IASTExpression bitField;
 
    public CPPASTFieldDeclarator() {
	}
    
    public CPPASTFieldDeclarator(IASTName name) {
		super(name);
	}

	public CPPASTFieldDeclarator(IASTName name, IASTExpression bitField) {
		super(name);
		setBitFieldSize(bitField);
	}

	@Override
	public CPPASTFieldDeclarator copy() {
		CPPASTFieldDeclarator copy = new CPPASTFieldDeclarator();
		copyBaseDeclarator(copy);
		copy.setBitFieldSize(bitField == null ? null : bitField.copy());
		return copy;
	}
	 
	public IASTExpression getBitFieldSize() {
        return bitField;
    }

    public void setBitFieldSize(IASTExpression size) {
        assertNotFrozen();
        this.bitField = size;
        if (size != null) {
			size.setParent(this);
			size.setPropertyInParent(FIELD_SIZE);
		}
    }

    @Override
	protected boolean postAccept(ASTVisitor action) {
		if (bitField != null && !bitField.accept(action))
			return false;

		return super.postAccept(action);
	}

    public void replace(IASTNode child, IASTNode other) {
        if( child == bitField )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            bitField  = (IASTExpression) other;
        }
        
    }

}
