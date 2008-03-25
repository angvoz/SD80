/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public class CPPUnknownBinding extends PlatformObject implements ICPPInternalUnknown, Cloneable {
    private ICPPScope unknownScope;
    protected ICPPInternalUnknown scopeBinding;
    protected IASTName name;

    public CPPUnknownBinding(ICPPInternalUnknown scopeBinding, IASTName name) {
        super();
        this.name = name;
        this.scopeBinding = scopeBinding;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown#getUnknownScope()
     */
    public ICPPScope getUnknownScope() {
        if (unknownScope == null) {
            unknownScope = new CPPUnknownScope(this, name);
        }
        return unknownScope;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
     */
    public void addDefinition(IASTNode node) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
     */
    public void addDeclaration(IASTNode node) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#removeDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
     */
    public void removeDeclaration(IASTNode node) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
     */
    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
    	return CPPVisitor.getQualifiedNameCharArray(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return name.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return name.toCharArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return scopeBinding.getUnknownScope();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown#resolveUnknown(org.eclipse.cdt.core.parser.util.ObjectMap)
     */
    public IBinding resolveUnknown(ObjectMap argMap) throws DOMException {
        IBinding result = this;
        IType t = (IType) argMap.get(scopeBinding);
        if (t == null && scopeBinding instanceof CPPUnknownBinding) {
        	IBinding binding = ((CPPUnknownBinding) scopeBinding).resolveUnknown(argMap);
        	if (binding instanceof IType) {
                t = (IType) binding;
            }
        }
        if (t != null) {
            t = CPPSemantics.getUltimateType(t, false);
	        if (t instanceof ICPPClassType) {
	            IScope s = ((ICPPClassType) t).getCompositeScope();
	            if (s != null && ASTInternal.isFullyCached(s))
	            	result = s.getBinding(name, true);
	        } else if (t instanceof ICPPInternalUnknown) {
	            CPPUnknownBinding res = clone();
	            res.scopeBinding = (ICPPInternalUnknown) t;
	            res.unknownScope = null;
	            result = res;
	        }
        }
        return result;
    }

    
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	@Override
	public CPPUnknownBinding clone() {
		try {
			return (CPPUnknownBinding) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;  // Never happens
		}
	}

	/* (non-Javadoc)
	 * For debug purposes only
	 */
	@Override
	public String toString() {
		return getName();
	}
}
