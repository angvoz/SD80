/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Mar 28, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;

/**
 * @author aniefer
 */
public class CPPClassInstanceScope implements ICPPClassScope {
	private CharArrayObjectMap bindings;
	private ObjectMap instanceMap = ObjectMap.EMPTY_MAP;
	
	private ICPPInstance instance;
	private boolean isFullyCached = false;
	/**
	 * @param instance
	 */
	public CPPClassInstanceScope(CPPClassInstance instance ) {
		this.instance = instance;
	}

	private ICPPClassType getOriginalClass(){
		return (ICPPClassType) instance.getOriginalBinding();
	}
	public boolean isFullyCached(){
		if( !isFullyCached ){
			CPPSemantics.LookupData data = new CPPSemantics.LookupData( CPPSemantics.EMPTY_NAME_ARRAY );
			try {
				CPPSemantics.lookupInScope( data, this, null );
			} catch (DOMException e) {
			}
		}
		return true;
	}
	
	public IBinding getBinding( IASTName name, boolean forceResolve ) {
		//ICPPClassScope scope = (ICPPClassScope) getOriginalClass().getCompositeScope();
		char [] c = name.toCharArray();
	    if( bindings == null )
	        return null;
	    
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
		    			if( binding != null ){
		    				binding = CPPTemplates.createInstance( n, this, binding, instance.getArgumentMap() );
		    				if( instanceMap == ObjectMap.EMPTY_MAP )
		    					instanceMap = new ObjectMap(2);
			        		instanceMap.put( n, binding );
		    			}
	    			}
	    		} else if( obj instanceof IBinding ){
	    			if( instanceMap.containsKey( obj ) ){
	    				binding = (IBinding) instanceMap.get( obj );
	    			} else {
	    				binding = CPPTemplates.createInstance( null, this, (IBinding) obj, instance.getArgumentMap() );
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
		// TODO Auto-generated method stub
		return null;
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
		if( bindings == null )
			bindings = new CharArrayObjectMap(1);
		char [] c = name.toCharArray();
		Object o = bindings.get( c );
		if( o != null ){
		    if( o instanceof ObjectSet ){
		    	((ObjectSet)o).put( name );
		        //bindings.put( c, ArrayUtil.append( Object.class, (Object[]) o, name ) );
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#setFullyCached(boolean)
	 */
	public void setFullyCached(boolean b) {
		isFullyCached = b;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
	 */
	public IScope getParent() {
		// TODO Auto-generated method stub
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
		ICPPClassScope scope = (ICPPClassScope) getOriginalClass().getCompositeScope();
		return scope.getPhysicalNode();
	}
}
