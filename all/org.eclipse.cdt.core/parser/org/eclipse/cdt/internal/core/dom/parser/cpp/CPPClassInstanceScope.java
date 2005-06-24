/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
/*
 * Created on Mar 28, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;

/**
 * @author aniefer
 */
public class CPPClassInstanceScope implements ICPPClassScope {
	private static final char[] CONSTRUCTOR_KEY = "!!!CTOR!!!".toCharArray();  //$NON-NLS-1$

	private CharArrayObjectMap bindings;
	private ObjectMap instanceMap = ObjectMap.EMPTY_MAP;
	
	private ICPPTemplateInstance instance;
	private boolean isFullyCached = false;
	private boolean doneConstructors = false;
	
	/**
	 * @param instance
	 */
	public CPPClassInstanceScope(CPPClassInstance instance ) {
		this.instance = instance;
	}

	private ICPPClassType getOriginalClass(){
		return (ICPPClassType) instance.getTemplateDefinition();
	}
	public boolean isFullyCached(){
		if( !isFullyCached ){
			CPPSemantics.LookupData data = new CPPSemantics.LookupData();
			try {
				CPPSemantics.lookupInScope( data, this, null );
			} catch (DOMException e) {
			}
		}
		return true;
	}
	
	public IBinding getBinding( IASTName name, boolean forceResolve ) {
		char [] c = name.toCharArray();
	    if( bindings == null )
	        return null;
	    
	    if( CharArrayUtils.equals( c, instance.getNameCharArray() ) && CPPClassScope.isConstructorReference( name ) ){
            c = CONSTRUCTOR_KEY;
        }
	        
	    Object cache = bindings.get( c );
	    if( cache != null ){
	    	int i = ( cache instanceof ObjectSet ) ? 0 : -1;
	    	ObjectSet set = ( cache instanceof ObjectSet ) ? (ObjectSet) cache : null;
	    	Object obj = ( set != null ) ? set.keyAt( i ) : cache;
	    	IBinding [] bs = null;
	    	IBinding binding = null;
	    	while( obj != null ){
	    		if( obj instanceof IASTName ){
	    			IASTName n = (IASTName) obj;
	    			if( n instanceof ICPPASTQualifiedName ){
    					IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
    					n = ns[ ns.length - 1 ];
    				}
	    			if( instanceMap.containsKey( n ) ){
	    				binding = (IBinding) instanceMap.get( n );
	    			} else {
		    			binding = forceResolve ? n.resolveBinding() : n.getBinding();
		    			if (binding instanceof ICPPClassTemplatePartialSpecialization ){
		    			    binding = null;
		    			}
		    			if( binding != null ){
		    				binding = CPPTemplates.createSpecialization( this, binding, instance.getArgumentMap() );
		    				if( instanceMap == ObjectMap.EMPTY_MAP )
		    					instanceMap = new ObjectMap(2);
			        		instanceMap.put( n, binding );
		    			}
	    			}
	    		} else if( obj instanceof IBinding ){
	    			if( instanceMap.containsKey( obj ) ){
	    				binding = (IBinding) instanceMap.get( obj );
	    			} else {
	    				binding = CPPTemplates.createSpecialization( this, (IBinding) obj, instance.getArgumentMap()  );
	    				if( instanceMap == ObjectMap.EMPTY_MAP )
	    					instanceMap = new ObjectMap(2);
		        		instanceMap.put( obj, binding );
	    			}
	    		}
	    		if( binding != null ){
	    			if( i == -1 )
	    				return binding;
    				bs = (IBinding[]) ArrayUtil.append( IBinding.class, bs, binding );
    				binding = null;
	    		}
	    		if( i != -1 && ++i < set.size() ){
	    			obj = set.keyAt( i );
	    		} else {
	    			obj = null;
	    		}
	    	}
	    	bs = (IBinding[]) ArrayUtil.trim( IBinding.class, bs );
	    	if( bs.length  == 1 )
	    		return bs[0];
	    	return CPPSemantics.resolveAmbiguities( name, bs );
	    }
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope#getClassType()
	 */
	public ICPPClassType getClassType() {
		return (ICPPClassType) instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope#getImplicitMethods()
	 */
	public ICPPMethod[] getImplicitMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getScopeName()
	 */
	public IASTName getScopeName() {
		return (IASTName) ((ICPPInternalBinding)instance).getDefinition();
	}

	public void addName(IASTName name) {
		if( name instanceof ICPPASTQualifiedName )
			return;
		
		if( bindings == null )
			bindings = new CharArrayObjectMap(1);		
		char [] c = name.toCharArray();
		
		IASTNode parent = name.getParent();
		if( parent instanceof IASTDeclarator && CPPVisitor.isConstructor( this, (IASTDeclarator) parent ) ){
			c = CONSTRUCTOR_KEY;
		}
		Object o = bindings.get( c );
		if( o != null ){
		    if( o instanceof ObjectSet ){
		    	((ObjectSet)o).put( name );
		    } else {
		    	ObjectSet temp = new ObjectSet( 2 );
		    	temp.put( o );
		    	temp.put( name );
		        bindings.put( c, temp );
		    }
		} else {
		    bindings.put( c, name );
		}
	}

	protected ICPPConstructor [] getConstructors( ){
		if( bindings == null ) 
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		
		if( !doneConstructors ){
			ICPPConstructor[] ctors;
			try {
				ctors = ((ICPPClassType)instance.getTemplateDefinition()).getConstructors();
				for (int i = 0; i < ctors.length; i++) {
					addBinding( ctors[i] );
				}
				doneConstructors = true;
			} catch (DOMException e) {
			}
		}
		ICPPConstructor[] ctors =  CPPClassScope.getConstructors( bindings, true );
		for (int i = 0; i < ctors.length; i++) {
			if( instanceMap.containsKey( ctors[i] ) ){
				ctors[i] = (ICPPConstructor) instanceMap.get( ctors[i] );
			} else {
				IBinding b = CPPTemplates.createSpecialization( this, ctors[i], instance.getArgumentMap() ); 
				if( instanceMap == ObjectMap.EMPTY_MAP )
					instanceMap = new ObjectMap(2);
        		instanceMap.put( ctors[i], b );
        		ctors[i] = (ICPPConstructor) b;
			}
		}
		return ctors;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#setFullyCached(boolean)
	 */
	public void setFullyCached(boolean b) {
		isFullyCached = b;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
	 */
	public IScope getParent() throws DOMException {
		ICPPClassType cls = getOriginalClass();
		ICPPClassScope scope = (ICPPClassScope)cls.getCompositeScope();
		if( scope != null )
			return scope.getParent();
		if( cls instanceof ICPPInternalBinding ){
			IASTNode [] nds = ((ICPPInternalBinding)cls).getDeclarations();
			if( nds != null && nds.length > 0 )
				return CPPVisitor.getContainingScope( nds[0] );
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public IBinding[] find(String name) {
		if( name != null ) {}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() throws DOMException {
		ICPPClassType cls = getOriginalClass();
		ICPPClassScope scope = (ICPPClassScope)cls.getCompositeScope();
		if( scope != null )
			return scope.getPhysicalNode();
		
		if( cls instanceof ICPPInternalBinding ){
			IASTNode [] nds = ((ICPPInternalBinding)cls).getDeclarations();
			if( nds != null && nds.length > 0 )
				return nds[0];
		}
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#removeBinding(org.eclipse.cdt.core.dom.ast.IBinding)
     */
	public void removeBinding(IBinding binding) {
	    char [] name = binding.getNameCharArray();
	    if( ! bindings.containsKey( name ) )
	        return;
	    
	    Object obj = bindings.get( name );
	    if( obj instanceof ObjectSet ){
	        ObjectSet set = (ObjectSet) obj;
	        set.remove( binding );
	        if( set.size() == 0 )
	            bindings.remove( name, 0, name.length );
	    } else {
	        bindings.remove( name, 0, name.length );
	    }
	
		if( instanceMap != null && instanceMap.containsKey( binding ) )
			instanceMap.remove( binding );
		isFullyCached = false;
	}

	public void flushCache() {
		if( bindings != null )
			bindings.clear();
		isFullyCached = false;
	}

	public void addBinding(IBinding binding) {
        if( bindings == null )
            bindings = new CharArrayObjectMap(1);
        char [] c = (binding instanceof ICPPConstructor) ? CONSTRUCTOR_KEY : binding.getNameCharArray();
        Object o = bindings.get( c );
        if( o != null ){
            if( o instanceof ObjectSet ){
                ((ObjectSet)o).put( binding );
            } else {
                ObjectSet set = new ObjectSet(2);
                set.put( o );
                set.put( binding );
                bindings.put( c, set );
            }
        } else {
            bindings.put( c, binding );
        }
	}
	
	public IBinding getInstance( IBinding binding ){
		if( instanceMap != null && instanceMap.containsKey( binding ) )
			return (IBinding) instanceMap.get( binding );
		return null;
	}
}
