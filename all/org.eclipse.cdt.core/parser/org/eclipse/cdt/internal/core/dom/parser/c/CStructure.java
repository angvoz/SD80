
/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class CStructure implements ICompositeType, ICInternalBinding {
	private IASTName [] declarations = null;
	private IASTName definition;
	
	public CStructure( IASTName name ){
	    if( name.getPropertyInParent() == IASTCompositeTypeSpecifier.TYPE_NAME )
	        definition = name;
	    else {
	        declarations = new IASTName[] { name };
	    }
	    ((CASTName) name).setBinding( this );
	}
	
    public IASTNode getPhysicalNode(){
        return ( definition != null ) ? (IASTNode)definition : (IASTNode)declarations[0];
    }
	private ICASTCompositeTypeSpecifier checkForDefinition( IASTElaboratedTypeSpecifier declSpec ){
		IASTDeclSpecifier spec = CVisitor.findDefinition( (ICASTElaboratedTypeSpecifier) declSpec );
		if( spec != null && spec instanceof ICASTCompositeTypeSpecifier ){
			ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) spec;
			((CASTName)compTypeSpec.getName()).setBinding( this );
			return compTypeSpec;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		if( definition != null )
			return definition.toString();

		return declarations[0].toString();
	}
	public char[] getNameCharArray() {
		if( definition != null )
			return definition.toCharArray();

		return declarations[0].toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() throws DOMException {
	    IASTDeclSpecifier declSpec = (IASTDeclSpecifier) ( ( definition != null ) ? (IASTNode)definition.getParent() : declarations[0].getParent() );
		IScope scope = CVisitor.getContainingScope( declSpec );
		while( scope instanceof ICCompositeTypeScope ){
			scope = scope.getParent();
		}
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() throws DOMException {
	    if( definition == null ){
	        ICASTCompositeTypeSpecifier temp = checkForDefinition( (IASTElaboratedTypeSpecifier) declarations[0].getParent() );
	        if( temp == null )
	            return new IField [] { new CField.CFieldProblem( IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
	        definition = temp.getName();
	    }
	    ICASTCompositeTypeSpecifier compSpec = (ICASTCompositeTypeSpecifier) definition.getParent();
		IASTDeclaration[] members = compSpec.getMembers();
		int size = members.length;
		IField[] fields = new IField[ size ];
		if( size > 0 ){
		    ICCompositeTypeScope scope = (ICCompositeTypeScope) getCompositeScope();
		    if( scope.isFullyCached() )
		        scope = null;
			for( int i = 0; i < size; i++ ){
				IASTNode node = members[i];
				if( node instanceof IASTSimpleDeclaration ){
					IASTDeclarator[] declarators = ((IASTSimpleDeclaration)node).getDeclarators();
					for( int j = 0; j < declarators.length; j++ ){
						IASTDeclarator declarator = declarators[j];
						IASTName name = declarator.getName();
						IBinding binding = name.resolveBinding();
						if( scope != null )
						    scope.addName( name );
						if( binding != null )
							fields[i] =  (IField) binding;
					}
				}
			}
			if( scope != null )
			    scope.setFullyCached( true );
		}
		return fields;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#findField(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public IField findField(String name) throws DOMException {
	    if( definition == null ){
	        ICASTCompositeTypeSpecifier temp = checkForDefinition( (IASTElaboratedTypeSpecifier) declarations[0].getParent() );
	        if( temp == null )
	            return new CField.CFieldProblem( IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() );
	        definition = temp.getName();
	    }
	    
	    ICCompositeTypeScope scope = (ICCompositeTypeScope) getCompositeScope();
	    if( scope != null && scope.isFullyCached() ){
	        IBinding binding = scope.getBinding( name.toCharArray() );
	        if( binding instanceof IField )
	            return (IField) binding;
	    } else {
	        ICASTCompositeTypeSpecifier compSpec = (ICASTCompositeTypeSpecifier) definition.getParent();
	    	IASTDeclaration[] members = compSpec.getMembers();
	    	int size = members.length;
	    	if( size > 0 ){
	    	    IField found = null;
	    		for( int i = 0; i < size; i++ ){
	    			IASTNode node = members[i];
	    			if( node instanceof IASTSimpleDeclaration ){
	    				IASTDeclarator[] declarators = ((IASTSimpleDeclaration)node).getDeclarators();
	    				for( int j = 0; j < declarators.length; j++ ){
	    					IASTDeclarator declarator = declarators[j];
	    					IASTName dtorName = declarator.getName();
	    					if( scope != null )
	    					    scope.addName( dtorName );
	    					if( name.equals( dtorName.toString() ) ){
	    						IBinding binding = dtorName.resolveBinding();
	    						if( binding instanceof IField )
	    							found = (IField) binding;
	    					}
	    				}
	    			}
	    		}
	    		if( scope != null )
	    		    scope.setFullyCached( true );
	    		if( found != null )
	    		    return found;
	    	}
	    }
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() {
		return ( definition != null ) ? ((IASTCompositeTypeSpecifier)definition.getParent()).getKey() 
		        					  : ((IASTElaboratedTypeSpecifier)declarations[0].getParent()).getKind();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() {
		return (definition != null ) ? ((IASTCompositeTypeSpecifier)definition.getParent()).getScope() : null;
	}
	
    public Object clone(){
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //not going to happen
        }
        return t;
    }

	/**
	 * @param compositeTypeSpec
	 */
	public void addDefinition(ICASTCompositeTypeSpecifier compositeTypeSpec) {
		definition = compositeTypeSpec.getName();
		((CASTName)compositeTypeSpec.getName()).setBinding( this );
	}
}
