/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * @author aniefer
 */
public class CPPClassScope extends CPPScope implements ICPPClassScope {
    private CharArrayObjectMap bindings = CharArrayObjectMap.EMPTY_MAP;
    private ICPPConstructor [] constructors = null;
    
	public CPPClassScope( ICPPASTCompositeTypeSpecifier physicalNode ) {
		super( physicalNode );
		
		createImplicitMembers();
	}

	// 12.1 The default constructor, copy constructor, copy assignment operator, and destructor are
	//special member functions.  The implementation will implicitly declare these member functions
	//for a class type when the program does not declare them.
	private void createImplicitMembers(){
	    //create bindings for the implicit members, if the user declared them then those declarations
	    //will resolve to these bindings.
	    ICPPASTCompositeTypeSpecifier compTypeSpec = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    
	    //default constructor: A()
	    addBinding( new CPPImplicitConstructor( this, IParameter.EMPTY_PARAMETER_ARRAY ) );
	    
	    ICPPClassType clsType = (ICPPClassType) compTypeSpec.getName().resolveBinding();

	    //copy constructor: A( const A & )
	    IType pType = new CPPReferenceType( new CPPQualifierType( clsType, true, false ) );
	    IParameter [] ps = new IParameter [] { new CPPParameter( pType ) };
	    addBinding( new CPPImplicitConstructor( this, ps ) );
	    
	    //copy assignment operator: A& operator = ( const A & ) 
	    IType refType = new CPPReferenceType( clsType );
	    addBinding( new CPPImplicitMethod( this, "operator =".toCharArray(), refType, ps ) ); //$NON-NLS-1$
	    
	    //destructor: ~A()
	    char [] dtorName = CharArrayUtils.concat( "~".toCharArray(), compTypeSpec.getName().toCharArray() );  //$NON-NLS-1$
	    addBinding( new CPPImplicitMethod( this, dtorName, new CPPBasicType( IBasicType.t_unspecified, 0 ), IParameter.EMPTY_PARAMETER_ARRAY ) );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#addBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void addBinding(IBinding binding) {
	    if( binding instanceof ICPPConstructor ){
	        addConstructor( (ICPPConstructor) binding );
	        return;
	    }
		if( bindings == CharArrayObjectMap.EMPTY_MAP )
			bindings = new CharArrayObjectMap(1);
		char [] c = binding.getNameCharArray();
		Object o = bindings.get( c );
		if( o != null ){
		    if( o instanceof List ){
		        ((List)o).add( binding );
		    } else {
		        List list = new ArrayList(2);
		        list.add( o );
		        list.add( binding );
		        bindings.put( c, list );
		    }
		} else {
		    bindings.put( c, binding );
		}
	}

	private void addConstructor( ICPPConstructor constructor ){
	    if( constructors == null )
	        constructors = new ICPPConstructor[ 2 ];
	    
	    int i = 0;
	    for( ; i < constructors.length; i++ ){
	        if( constructors[i] == null ){
	            constructors[i] = constructor;
	            return;
	        }
	    }
	    ICPPConstructor [] temp = new ICPPConstructor[ constructors.length * 2 ];
	    System.arraycopy( constructors, 0, temp, 0, constructors.length );
	    temp[ constructors.length ] = constructor;
	    constructors = temp;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getBinding(int, char[])
	 */
	public IBinding getBinding( IASTName name ) {
	    char [] c = name.toCharArray();
	
	    ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    if( CharArrayUtils.equals( c, compType.getName().toCharArray() ) ){
	        if( isConstructorReference( name ) ){
	            if( constructors == null )
	                return null;
	            return CPPSemantics.resolveAmbiguities( name, Arrays.asList( constructors ) );
	        }
            //9.2 ... The class-name is also inserted into the scope of the class itself
            return compType.getName().resolveBinding();
	    }
	        
	    Object obj = bindings.get( c );
	    if( obj != null ){
	        if( obj instanceof List ){
	            obj = CPPSemantics.resolveAmbiguities( name, (List) obj );
	        }
	    }
		return (IBinding) obj;
	}

	protected ICPPConstructor [] getConstructors(){
	    if( constructors == null ){
	        constructors = new ICPPConstructor[0];
	        return constructors;
	    }
	    
	    int i = 0;
	    for( ; i < constructors.length; i++ )
	        if( constructors[i] == null )
	            break;
	    if( i < constructors.length ){
	        ICPPConstructor[] temp = new ICPPConstructor[ i ];
	        System.arraycopy( constructors, 0, temp, 0, i );
	        constructors = temp;
	    }
	        
	    return constructors;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public List find(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean isConstructorReference( IASTName name ){
	    IASTNode node = name.getParent();
	    if( node instanceof ICPPASTQualifiedName ){
	    	IASTName [] ns = ((ICPPASTQualifiedName)node).getNames();
	    	if( ns[ ns.length - 1 ] == name )
	    		node = node.getParent();
	    }
	    if( node instanceof IASTDeclSpecifier ){
	        IASTNode parent = node.getParent();
	        if( parent instanceof IASTTypeId && parent.getParent() instanceof ICPPASTNewExpression )
	            return true;
	        return false;
	    }
	    return true;
	}

}
