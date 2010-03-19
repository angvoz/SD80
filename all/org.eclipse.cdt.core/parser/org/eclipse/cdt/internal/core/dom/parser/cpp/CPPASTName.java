/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon(IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Bryan Wilkinson (QNX)
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Unqualified name, also base class for operator and conversion name.
 */
public class CPPASTName extends CPPASTNameBase implements IASTCompletionContext {
	public static IASTName NOT_INITIALIZED= new CPPASTName(null);
	
	private char[] name;
    public CPPASTName(char[] name) {
        this.name = name;
    }

    public CPPASTName() {
        name = CharArrayUtils.EMPTY;
    }

    public CPPASTName copy() {
		CPPASTName copy = new CPPASTName(name == null ? null : name.clone());
		copy.setOffsetAndLength(this);
		return copy;
	}
    
	@Override
	protected IBinding createIntermediateBinding() {
		return CPPVisitor.createBinding(this);
	}

    @Override
	public IASTCompletionContext getCompletionContext() {
        IASTNode node = getParent();
    	while (node != null) {
    		if (node instanceof IASTCompletionContext) {
    			return (IASTCompletionContext) node;
    		}
    		node = node.getParent();
    	}
    	if (getLength() > 0) {
    		return this;
    	}
    	return null;
    }

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IASTNode parent = getParent();
		if (parent instanceof ICPPASTElaboratedTypeSpecifier) {
			ICPPASTElaboratedTypeSpecifier specifier = (ICPPASTElaboratedTypeSpecifier) parent;
			int kind = specifier.getKind();
			switch (kind) {
			case ICompositeType.k_struct:
			case ICompositeType.k_union:
			case ICPPASTElaboratedTypeSpecifier.k_class:
				break;
			default:
				return null;
			}
			IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);
			return filterByElaboratedTypeSpecifier(kind, bindings);
		}
		else if (parent instanceof IASTDeclarator) {
			IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);
			for (int i = 0; i < bindings.length; i++) {
				if (bindings[i] instanceof ICPPNamespace || bindings[i] instanceof ICPPClassType) {
				} else {
					bindings[i] = null;
				}
			}
			return (IBinding[])ArrayUtil.removeNulls(IBinding.class, bindings);
		}
		return null;
	}

    private IBinding[] filterByElaboratedTypeSpecifier(int kind, IBinding[] bindings) {
		for (int i = 0; i < bindings.length; i++) {
			if (bindings[i] instanceof ICPPNamespace) {
			} else if (bindings[i] instanceof ICPPClassType) {
				ICPPClassType type = (ICPPClassType) bindings[i];
				try {
					switch (type.getKey()) {
					case ICompositeType.k_struct:
						if (kind != ICompositeType.k_struct) {
							bindings[i] = null;
						}
						break;
					case ICompositeType.k_union:
						if (kind != ICompositeType.k_union) {
							bindings[i] = null;
						}
						break;
					case ICPPClassType.k_class:
						if (kind != ICPPASTElaboratedTypeSpecifier.k_class) {
							bindings[i] = null;
						}
						break;
					}
				} catch (DOMException e) {
					bindings[i] = null;
					CCorePlugin.log(e);
				}
			} else {
				bindings[i]= null;
			}
		}
		return (IBinding[])ArrayUtil.removeNulls(IBinding.class, bindings);
	}

    public char[] toCharArray() {
        return name;
    }

    public char[] getSimpleID() {
		return name;
	}

	public char[] getLookupKey() {
		return name;
	}
	
	public void setName(char[] name) {
        assertNotFrozen();
        this.name = name;
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitNames) {
            switch (action.visit(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }
        
                
        if (action.shouldVisitNames) {
            switch (action.leave(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }
        return true;
    }
}
