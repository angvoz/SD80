/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTCompositeTypeSpecifier extends CPPASTBaseDeclSpecifier
        implements ICPPASTCompositeTypeSpecifier, IASTAmbiguityParent {

    private int k;
    private IASTName n;
    private IScope scope;


    public CPPASTCompositeTypeSpecifier() {
	}

	public CPPASTCompositeTypeSpecifier(int k, IASTName n) {
		this.k = k;
		setName(n);
	}

	@Override
	public String getRawSignature() {
       return getName().toString() == null ? "" : getName().toString(); //$NON-NLS-1$
    }

    public ICPPASTBaseSpecifier[] getBaseSpecifiers() {
        if( baseSpecs == null ) return ICPPASTBaseSpecifier.EMPTY_BASESPECIFIER_ARRAY;
        baseSpecs = (ICPPASTBaseSpecifier[]) ArrayUtil.removeNullsAfter( ICPPASTBaseSpecifier.class, baseSpecs, baseSpecsPos );
        return baseSpecs;
    }

    public void addBaseSpecifier(ICPPASTBaseSpecifier baseSpec) {
    	if (baseSpec != null) {
    		baseSpec.setParent(this);
			baseSpec.setPropertyInParent(BASE_SPECIFIER);
    		baseSpecs = (ICPPASTBaseSpecifier[]) ArrayUtil.append( ICPPASTBaseSpecifier.class, baseSpecs, ++baseSpecsPos, baseSpec );
    	}
    }

    public int getKey() {
        return k;
    }

    public void setKey(int key) {
        k = key;
    }

    public IASTName getName() {
        return n;
    }

    public void setName(IASTName name) {
        this.n = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(TYPE_NAME);
		}
    }

    public IASTDeclaration[] getMembers() {
        if( declarations == null ) return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
        return (IASTDeclaration[]) ArrayUtil.trim( IASTDeclaration.class, declarations );

    }

    public void addMemberDeclaration(IASTDeclaration decl) {
        declarations = (IASTDeclaration[]) ArrayUtil.append( IASTDeclaration.class, declarations, decl );
        if(decl != null) {
        	decl.setParent(this);
        	decl.setPropertyInParent(decl instanceof ICPPASTVisiblityLabel ? VISIBILITY_LABEL : MEMBER_DECLARATION);
        }
    }


    private IASTDeclaration [] declarations = new IASTDeclaration[4];
    private ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier [] baseSpecs = null;
    private int baseSpecsPos=-1;

    public IScope getScope() {
    	if( scope == null )
    		scope = new CPPClassScope( this );
    	
        return scope;
    }
    
    public void setScope( IScope scope ){
        this.scope = scope;
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( n != null ) if( !n.accept( action ) ) return false;
        ICPPASTBaseSpecifier[] bases = getBaseSpecifiers();
        for( int i = 0; i < bases.length; i++ )   
            if( !bases[i].accept( action ) ) return false;
           
        IASTDeclaration [] decls = getMembers();
        for( int i = 0; i < decls.length; i++ )
            if( !decls[i].accept( action ) ) return false;
         
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
	
	public int getRoleForName(IASTName name) {
		if( name == this.n )
			return r_definition;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
        if( declarations == null ) return;
        for( int i = 0; i < declarations.length; ++i )
        {
           if( declarations[i] == null ) {
        	   break;
           }
           if( declarations[i] == child )
           {
               other.setParent( child.getParent() );
               other.setPropertyInParent( child.getPropertyInParent() );
               declarations[i] = (IASTDeclaration) other;
           }
        }
    }
}
