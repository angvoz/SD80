/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Implementation for C composite specifiers.
 */
public class CASTCompositeTypeSpecifier extends CASTBaseDeclSpecifier implements
        ICASTCompositeTypeSpecifier, IASTAmbiguityParent {

    private int fKey;
    private IASTName fName;
    private IASTDeclaration[] fActiveDeclarations= null;
    private IASTDeclaration [] fAllDeclarations = null;
    private int fDeclarationsPos=-1;
    private IScope fScope = null;
    
    public CASTCompositeTypeSpecifier() {
	}

	public CASTCompositeTypeSpecifier(int key, IASTName name) {
		this.fKey = key;
		setName(name);
	}
    
	public CASTCompositeTypeSpecifier copy() {
		CASTCompositeTypeSpecifier copy = new CASTCompositeTypeSpecifier();
		copyCompositeTypeSpecifier(copy);
		return copy;
	}
	
	protected void copyCompositeTypeSpecifier(CASTCompositeTypeSpecifier copy) {
		copyBaseDeclSpec(copy);
		copy.setKey(fKey);
		copy.setName(fName == null ? null : fName.copy());
		for(IASTDeclaration member : getMembers())
			copy.addMemberDeclaration(member == null ? null : member.copy());	
	}
	
    public int getKey() {
        return fKey;
    }

    public void setKey(int key) {
        assertNotFrozen();
        this.fKey = key;
    }

    public IASTName getName() {
        return fName;
    }
    
    public void setName(IASTName name) {
        assertNotFrozen();
        this.fName = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(TYPE_NAME);
		}
    }

	public IASTDeclaration[] getMembers() {
		IASTDeclaration[] active= fActiveDeclarations;
		if (active == null) {
			active = ASTQueries.extractActiveDeclarations(fAllDeclarations, fDeclarationsPos+1);
			fActiveDeclarations= active;
		}
		return active;
	}

	public final IASTDeclaration[] getDeclarations(boolean includeInactive) {
		if (includeInactive) {
			fAllDeclarations= (IASTDeclaration[]) ArrayUtil.removeNullsAfter(IASTDeclaration.class, fAllDeclarations, fDeclarationsPos);
			return fAllDeclarations;
		}
		return getMembers();
	}

    public void addMemberDeclaration(IASTDeclaration declaration) {
        assertNotFrozen();
    	if (declaration != null) {
    		declaration.setParent(this);
    		declaration.setPropertyInParent(MEMBER_DECLARATION);
			fAllDeclarations = (IASTDeclaration[]) ArrayUtil.append(IASTDeclaration.class, fAllDeclarations,
					++fDeclarationsPos, declaration);
			fActiveDeclarations= null;
    	}
    }
    
    public void addDeclaration(IASTDeclaration declaration) {
    	addMemberDeclaration(declaration);
    }
    
    public IScope getScope() {
        if( fScope == null )
            fScope = new CCompositeTypeScope( this );
        return fScope;
    }

    @Override
	public boolean accept( ASTVisitor action ){
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		if (fName != null && !fName.accept(action))
			return false;
           
		IASTDeclaration[] decls= getDeclarations(action.includeInactiveNodes);
		for (int i = 0; i < decls.length; i++) {
			if (!decls[i].accept(action)) return false;
		}
        
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.leave(this)) {
			    case ASTVisitor.PROCESS_ABORT: return false;
			    case ASTVisitor.PROCESS_SKIP: return true;
			    default: break;
			}
		}
        return true;
    }

	public int getRoleForName(IASTName n) {
		if( n == this.fName )
			return r_definition;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
		assert child.isActive() == other.isActive();
		for (int i = 0; i <= fDeclarationsPos; ++i) {
			if (fAllDeclarations[i] == child) {
				other.setParent(child.getParent());
				other.setPropertyInParent(child.getPropertyInParent());
				fAllDeclarations[i] = (IASTDeclaration) other;
				fActiveDeclarations= null;
				return;
			}
		}
    }
}
