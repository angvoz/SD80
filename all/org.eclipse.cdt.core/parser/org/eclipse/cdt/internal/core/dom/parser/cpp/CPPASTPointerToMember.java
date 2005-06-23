/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;

/**
 * @author jcamelon
 */
public class CPPASTPointerToMember extends CPPASTPointer implements
        ICPPASTPointerToMember {

    private IASTName n;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        n = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember#getName()
     */
    public IASTName getName() {
        return n;
    }

    public boolean accept( ASTVisitor action ){
        if( n != null ) if( !n.accept( action ) ) return false;
        return true;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName name ) {
		if( name  == this.n )
			return r_reference;
		return r_unclear;
	}
}
