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

import org.eclipse.cdt.core.dom.ast.ASTSignatureUtil;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;

/**
 * @author jcamelon
 */
public abstract class CPPASTBaseDeclSpecifier extends CPPASTNode implements ICPPASTDeclSpecifier {

    private boolean friend;
    private boolean inline;
    private boolean volatil;
    private boolean isConst;
    private int sc;
    private boolean virtual;
    private boolean explicit;

    public boolean isFriend() {
        return friend;
    }

    public int getStorageClass() {
        return sc;
    }

    public void setStorageClass(int storageClass) {
        sc = storageClass;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean value) {
        isConst = value;
    }

    public boolean isVolatile() {
        return volatil;
    }

    public void setVolatile(boolean value) {
        volatil = value;
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean value) {
        this.inline = value;
    }

    public void setFriend(boolean value) {
        friend = value;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(boolean value) {
        virtual = value;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean value) {
        this.explicit = value;
    }

    @Override
	public String toString() {
    	return ASTSignatureUtil.getSignature(this);
    }
}
