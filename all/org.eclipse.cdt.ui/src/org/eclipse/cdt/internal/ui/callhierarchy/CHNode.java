/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IVariableDeclaration;

import org.eclipse.cdt.internal.ui.util.CoreUtility;

/**
 * Represents a node in the include browser
 */
public class CHNode implements IAdaptable {
	private CHNode fParent;
	private ICElement fRepresentedDecl;
	private ITranslationUnit fFileOfReferences;
    private List fReferences;
    
    private int fHashCode;
    private boolean fIsRecursive;
    private long fTimestamp;

    /**
     * Creates a new node for the include browser
     */
    public CHNode(CHNode parent, ITranslationUnit fileOfReferences, long timestamp, ICElement decl) {
        fParent= parent;
        fFileOfReferences= fileOfReferences;
        fReferences= Collections.EMPTY_LIST;
        fRepresentedDecl= decl;
        fIsRecursive= computeIsRecursive(fParent, decl);
        fHashCode= computeHashCode();
        fTimestamp= timestamp;
    }
    
	private int computeHashCode() {
        int hashCode= 0;
        if (fParent != null) {
            hashCode= fParent.hashCode() * 31;
        }
        if (fRepresentedDecl != null) {
        	hashCode+= fRepresentedDecl.hashCode();
        }
        return hashCode;
    }   

    public int hashCode() {
        return fHashCode;
    }
    
    public boolean equals(Object o) {
		if (!(o instanceof CHNode)) {
			return false;
		}

		CHNode rhs = (CHNode) o;
		if (fHashCode != rhs.fHashCode) {
			return false;
		}

		return CoreUtility.safeEquals(fRepresentedDecl, rhs.fRepresentedDecl);
    }
    
    private boolean computeIsRecursive(CHNode parent, ICElement decl) {
        if (parent == null || decl == null) {
            return false;
        }
        if (decl.equals(parent.fRepresentedDecl)) {
            return true;
        }
        return computeIsRecursive(parent.fParent, decl);
    }

	/**
     * Returns the parent node or <code>null</code> for the root node.
     */
    public CHNode getParent() {
        return fParent;
    }


	public boolean isRecursive() {
        return fIsRecursive;
    }

	public int getReferenceCount() {
		return fReferences.size();
	}
	
	public CHReferenceInfo getReference(int idx) {
		return (CHReferenceInfo) fReferences.get(idx);
	}
	
	public ICElement getRepresentedDeclaration() {
		return fRepresentedDecl;
	}

	public long getTimestamp() {
		return fTimestamp;
	}

	public boolean isMacro() {
		return fRepresentedDecl instanceof IMacro;
	}

	public boolean isVariable() {
		return fRepresentedDecl instanceof IVariableDeclaration;
	}
	
	public int getFirstReferenceOffset() {
		return fReferences.isEmpty() ? -1 : getReference(0).getOffset();
	}
	
	public void addReference(CHReferenceInfo info) {
		switch (fReferences.size()) {
		case 0:
			fReferences= Collections.singletonList(info);
			return;
		case 1:
			fReferences= new ArrayList(fReferences);
			break;
		}
		fReferences.add(info);
	}

	public ITranslationUnit getFileOfReferences() {
		return fFileOfReferences;
	}

	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(ICElement.class)) {
			return getRepresentedDeclaration();
		}
		return null;
	}
	
	public boolean isMultiDef() {
		return false;
	}

	public ICElement getOneRepresentedDeclaration() {
		return getRepresentedDeclaration();
	}
}
