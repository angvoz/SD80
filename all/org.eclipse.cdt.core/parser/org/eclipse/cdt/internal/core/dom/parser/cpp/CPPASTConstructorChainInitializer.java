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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTConstructorChainInitializer extends CPPASTNode implements
        ICPPASTConstructorChainInitializer, IASTAmbiguityParent {

    private IASTName name;
    private IASTExpression value;

    
    public CPPASTConstructorChainInitializer() {
	}

	public CPPASTConstructorChainInitializer(IASTName name, IASTExpression value) {
		setMemberInitializerId(name);
		setInitializerValue(value);
	}

	public IASTName getMemberInitializerId() {
        return name;
    }

    public void setMemberInitializerId(IASTName name) {
        this.name = name;
        if(name != null) {
			name.setParent(this);
			name.setPropertyInParent(MEMBER_ID);
		}
    }

    public IASTExpression getInitializerValue() {
        return value;
    }


    public void setInitializerValue(IASTExpression expression) {
        value = expression;
        if(expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(INITIALIZER);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (name != null)
            if (!name.accept(action))
                return false;
        if (value != null)
            if (!value.accept(action))
                return false;
        return true;
    }

    public int getRoleForName(IASTName n) {
        if (name == n)
            return r_reference;
        return r_unclear;
    }

    public void replace(IASTNode child, IASTNode other) {
        if (child == value) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            value = (IASTExpression) other;
        }
    }
}
