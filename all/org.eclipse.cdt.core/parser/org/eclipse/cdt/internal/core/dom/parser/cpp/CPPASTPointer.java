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
import org.eclipse.cdt.core.dom.ast.IASTPointer;

/**
 * @author jcamelon
 */
public class CPPASTPointer extends CPPASTNode implements IASTPointer {

    private boolean isConst;

    private boolean isVolatile;

    public boolean isConst() {
        return isConst;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    public void setConst(boolean value) {
        isConst = value;
    }

    public void setVolatile(boolean value) {
        isVolatile = value;
    }

    public boolean accept(ASTVisitor action) {
        return true;
    }
}
