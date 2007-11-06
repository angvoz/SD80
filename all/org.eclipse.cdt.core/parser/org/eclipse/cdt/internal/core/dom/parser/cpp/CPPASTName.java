/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Bryan Wilkinson (QNX)
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author jcamelon
 */
public class CPPASTName extends CPPASTNode implements IASTName, IASTCompletionContext {
	final static class RecursionResolvingBinding extends ProblemBinding {
		public RecursionResolvingBinding() {
			super(null, IProblemBinding.SEMANTIC_RECURSION_IN_LOOKUP, CharArrayUtils.EMPTY);
		}
		public RecursionResolvingBinding(IASTName node) {
			super(node, IProblemBinding.SEMANTIC_RECURSION_IN_LOOKUP, node.toCharArray());
		}
	}

	private char[] name;

    private static final char[] EMPTY_CHAR_ARRAY = {};
	private static final String EMPTY_STRING = "";  //$NON-NLS-1$

	static final int MAX_RESOLUTION_DEPTH = 5;

    private IBinding binding = null;
    private int fResolutionDepth= 0;

    public CPPASTName(char[] name) {
        this.name = name;
    }

    public CPPASTName() {
        name = EMPTY_CHAR_ARRAY;
    }

    public IBinding resolveBinding() {
        if (binding == null) {
        	if (++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
        		binding= new RecursionResolvingBinding(this);
        	}
        	else {
        		binding = CPPVisitor.createBinding(this);
        	}
        }
        return binding;
    }

	public void incResolutionDepth() {
		if (binding == null && ++fResolutionDepth > MAX_RESOLUTION_DEPTH) {
			binding= new RecursionResolvingBinding(this);
		}
	}

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
		if (getParent() instanceof IASTDeclarator) {
			IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);
			for (int i = 0; i < bindings.length; i++) {
				if (bindings[i] instanceof ICPPNamespace || bindings[i] instanceof ICPPClassType) {
				} else {
					bindings[i]= null;
				}
			}
			return (IBinding[])ArrayUtil.removeNulls(IBinding.class, bindings);
		}
		return null;
	}

    public void setBinding(IBinding binding) {
        this.binding = binding;
        fResolutionDepth= 0;
    }

    public IBinding getBinding() {
        return binding;
    }

    public String toString() {
        if (name == EMPTY_CHAR_ARRAY)
            return EMPTY_STRING;
        return new String(name);
    }

    public char[] toCharArray() {
        return name;
    }

    public void setName(char[] name) {
        this.name = name;
    }

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

    public boolean isDeclaration() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_reference:
            case IASTNameOwner.r_unclear:
                return false;
            default:
                return true;
            }
        }
        return false;
    }

    public boolean isReference() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_reference:
                return true;
            default:
                return false;
            }
        }
        return false;
    }

    public boolean isDefinition() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_definition:
                return true;
            default:
                return false;
            }
        }
        return false;
    }
}
