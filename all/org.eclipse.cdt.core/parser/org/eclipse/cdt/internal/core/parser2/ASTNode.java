/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser2;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;

/**
 * @author jcamelon
 */
public abstract class ASTNode implements IASTNode {

    private int length;
    private int offset;

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setLength(int length) {
        this.length = length;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getNodeLocations()
     */
    public IASTNodeLocation[] getNodeLocations() {
        return getTranslationUnit().getLocationInfo( offset, length );
    }
}
