/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *	  Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPBaseClause implements ICPPBase, ICPPInternalBase {
    static public class CPPBaseProblem extends ProblemBinding implements ICPPBase, ICPPInternalBase {
    	private ICPPClassType classProblem;

        public CPPBaseProblem(IASTNode node, int id, char[] arg) {
            super(node, id, arg);
        }

        public IBinding getBaseClass() {
        	if (classProblem == null) {
        		classProblem = new CPPClassType.CPPClassTypeProblem(node, id, arg);
        	}
        	return classProblem;
        }

        public int getVisibility() throws DOMException {
            throw new DOMException(this);
        }

        public boolean isVirtual() throws DOMException {
            throw new DOMException(this);
        }

		public IName getBaseClassSpecifierName() {
			return (IName) node;
		}

		public void setBaseClass(IBinding binding) {
		}
		
		@Override
		public ICPPBase clone() {
			return this;
		}
    }

    private ICPPASTBaseSpecifier base;
	private IBinding baseClass;
    
    public CPPBaseClause(ICPPASTBaseSpecifier base) {
        this.base = base;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBase#getBaseClass()
     */
    public IBinding getBaseClass() throws DOMException {
		if (baseClass == null) {
	    	IBinding b = base.getName().resolveBinding();
	    	while (b instanceof ITypedef && ((ITypedef) b).getType() instanceof IBinding) {
				b = (IBinding) ((ITypedef) b).getType();
	    	}
	    	if (b instanceof ICPPClassType || b instanceof ICPPTemplateParameter) {
	    		baseClass = b;
	    	} else if (b instanceof IProblemBinding) {
	    		baseClass =  new CPPClassType.CPPClassTypeProblem(base.getName(), ((IProblemBinding) b).getID());
	    	} else {
				baseClass = new CPPClassType.CPPClassTypeProblem(base.getName(), IProblemBinding.SEMANTIC_NAME_NOT_FOUND);
	    	}
		}
		return baseClass;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBase#getVisibility()
     */
    public int getVisibility() {
		int vis = base.getVisibility();
		
		if (vis == 0) {
			ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) base.getParent();
			int key = compSpec.getKey();
			if (key == ICPPClassType.k_class)
				vis = ICPPBase.v_private;
			else
				vis = ICPPBase.v_public;
		}
        return vis;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBase#isVirtual()
     */
    public boolean isVirtual() {
        return base.isVirtual();
    }

	public void setBaseClass(IBinding cls) {
		baseClass = cls;
	}

	public IName getBaseClassSpecifierName() {
		return base.getName();
	}

    @Override
	public ICPPBase clone() {
        ICPPBase t = null;
   		try {
            t = (ICPPBase) super.clone();
        } catch (CloneNotSupportedException e) {
            //not going to happen
        }
        return t;
    }
}
