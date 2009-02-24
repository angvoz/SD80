/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems) 
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Represents a function.
 */
public class CFunction extends PlatformObject implements IFunction, ICInternalFunction {
	private IASTStandardFunctionDeclarator [] declarators = null;
	private IASTFunctionDeclarator definition;
	
	private static final int FULLY_RESOLVED         = 1;
	private static final int RESOLUTION_IN_PROGRESS = 1 << 1;
	private int bits = 0;
	
	protected IFunctionType type = null;
	
	public CFunction( IASTFunctionDeclarator declarator ){
		if( declarator != null ) {
		    if( declarator.getParent() instanceof IASTFunctionDefinition || declarator instanceof ICASTKnRFunctionDeclarator )
		        definition = declarator;
		    else {
		        declarators = new IASTStandardFunctionDeclarator [] { (IASTStandardFunctionDeclarator) declarator };
		    }
		}
	}
	
    public IASTNode getPhysicalNode(){
    	if( definition != null )
    		return definition;
    	else if( declarators != null && declarators.length > 0 )
    		return declarators[0];
    	return null;
    }
    public void addDeclarator( IASTFunctionDeclarator fnDeclarator ){
    	if (!fnDeclarator.isActive())
    		return;
    	
        if( fnDeclarator.getParent() instanceof IASTFunctionDefinition || fnDeclarator instanceof ICASTKnRFunctionDeclarator ) {
        	if (definition == fnDeclarator) {
        		// recursion?
        		return;
        	}
            updateParameterBindings( fnDeclarator );
            definition = fnDeclarator;
            return;
        }
		updateParameterBindings( fnDeclarator );
		if( declarators == null ){
		    declarators = new IASTStandardFunctionDeclarator[] { (IASTStandardFunctionDeclarator) fnDeclarator };
			return;
		}
		for( int i = 0; i < declarators.length; i++ ){
		    if( declarators[i] == null ){
		        declarators[i] = (IASTStandardFunctionDeclarator) fnDeclarator;
		        return;
		    }
		}
		IASTStandardFunctionDeclarator tmp [] = new IASTStandardFunctionDeclarator [ declarators.length * 2 ];
		System.arraycopy( declarators, 0, tmp, 0, declarators.length );
		tmp[ declarators.length ] = (IASTStandardFunctionDeclarator) fnDeclarator;
		declarators = tmp;
    }
	
    protected IASTTranslationUnit getTranslationUnit() {
		if( definition != null )
            return definition.getTranslationUnit();
        else if( declarators != null )
            return declarators[0].getTranslationUnit();
		return null;
    }
    
    private void resolveAllDeclarations(){
	    if( (bits & (FULLY_RESOLVED | RESOLUTION_IN_PROGRESS)) == 0 ){
	        bits |= RESOLUTION_IN_PROGRESS;
		    IASTTranslationUnit tu = getTranslationUnit();
	        if( tu != null ){
	            CVisitor.getDeclarations( tu, this );
	        }
	        declarators = (IASTStandardFunctionDeclarator[]) ArrayUtil.trim( IASTStandardFunctionDeclarator.class, declarators );
	        bits |= FULLY_RESOLVED;
	        bits &= ~RESOLUTION_IN_PROGRESS;
	    }
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getParameters()
	 */
	public IParameter[] getParameters() {
		IParameter [] result = IParameter.EMPTY_PARAMETER_ARRAY;
		
	    IASTFunctionDeclarator dtor = (IASTFunctionDeclarator) getPhysicalNode();
	    if( dtor == null && (bits & FULLY_RESOLVED) == 0){
            resolveAllDeclarations();
            dtor = (IASTFunctionDeclarator) getPhysicalNode();
    	}
	    
		if (dtor instanceof IASTStandardFunctionDeclarator) {
			IASTParameterDeclaration[] params = ((IASTStandardFunctionDeclarator)dtor).getParameters();
			int size = params.length;
			result = new IParameter[ size ];
			if( size > 0 ){
				for( int i = 0; i < size; i++ ){
					IASTParameterDeclaration p = params[i];
					result[i] = (IParameter) ASTQueries.findInnermostDeclarator(p.getDeclarator()).getName().resolveBinding();
				}
			}
		} else if (dtor instanceof ICASTKnRFunctionDeclarator) {
			IASTName[] names = ((ICASTKnRFunctionDeclarator)dtor).getParameterNames();
			result = new IParameter[ names.length ];
			if( names.length > 0 ){
				// ensures that the List of parameters is created in the same order as the K&R C parameter names
				for( int i=0; i<names.length; i++ ) {
				    IASTDeclarator decl = CVisitor.getKnRParameterDeclarator( (ICASTKnRFunctionDeclarator) dtor, names[i] );
				    if( decl != null ) {
				        result[i] = (IParameter) decl.getName().resolveBinding();
				    } else {
				        result[i] = new CParameter.CParameterProblem( names[i], IProblemBinding.SEMANTIC_KNR_PARAMETER_DECLARATION_NOT_FOUND, names[i].toCharArray() );
				    }
				}
			}
		}
		
		return result;	    
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return getASTName().toString();
	}
	
	public char[] getNameCharArray(){
		return getASTName().toCharArray();
	}

	private IASTName getASTName() {
		IASTDeclarator dtor = ( definition != null ) ? definition : declarators[0];
	    IASTDeclarator nested= dtor.getNestedDeclarator();
	    while (nested != null && nested.getPointerOperators().length == 0) {
	    	dtor= nested;
	    	nested= nested.getNestedDeclarator();
	    }
	    return dtor.getName();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
	    IASTFunctionDeclarator dtor = (IASTFunctionDeclarator) getPhysicalNode();
	    if( dtor != null )
	    	return CVisitor.getContainingScope( dtor.getParent() );
	    return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
	 */
	public IScope getFunctionScope() {
		if( definition != null ){
			IASTFunctionDefinition def = (IASTFunctionDefinition) definition.getParent();
			return def.getScope();
		}
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#getType()
     */
    public IFunctionType getType() {
        if( type == null ) {
        	IASTDeclarator functionDtor = (IASTDeclarator) getPhysicalNode();
        	if( functionDtor == null && (bits & FULLY_RESOLVED) == 0){
                resolveAllDeclarations();
                functionDtor = (IASTDeclarator) getPhysicalNode();
        	}
        	if( functionDtor != null ) {
	        	while (functionDtor.getNestedDeclarator() != null)
	        		functionDtor = functionDtor.getNestedDeclarator();
	        	
	        	IType tempType = CVisitor.createType( functionDtor );
	        	if (tempType instanceof IFunctionType)
	        		type = (IFunctionType)tempType;
        	}
        }
        
        return type;
    }
	
    public IBinding resolveParameter( IASTName paramName ){
    	if( paramName.getBinding() != null )
    	    return paramName.getBinding();

    	IBinding binding = null;
    	int idx = 0;
    	IASTNode parent = paramName.getParent();
    	while( parent instanceof IASTDeclarator && !(parent instanceof ICASTKnRFunctionDeclarator ) )
    	    parent = parent.getParent();
    	
    	ICASTKnRFunctionDeclarator fKnRDtor = null;
    	IASTDeclarator knrParamDtor = null;
    	if( parent instanceof IASTParameterDeclaration ){
    	    IASTStandardFunctionDeclarator fdtor = (IASTStandardFunctionDeclarator) parent.getParent();
    	    IASTParameterDeclaration [] ps = fdtor.getParameters();
        	for( ; idx < ps.length; idx++ ){
        		if( parent == ps[idx] )
        			break;
        	}
    	} else if( parent instanceof IASTSimpleDeclaration ){ 
    	    //KnR: name in declaration list
    	    fKnRDtor = (ICASTKnRFunctionDeclarator) parent.getParent();
    	    IASTName [] ps = fKnRDtor.getParameterNames();
    	    char [] n = paramName.toCharArray();
        	for( ; idx < ps.length; idx++ ){
        		if( CharArrayUtils.equals( ps[idx].toCharArray(), n ) )
        			break;
        	}    	    
    	} else {
    	    //KnR: name in name list
    	    fKnRDtor = (ICASTKnRFunctionDeclarator) parent;
    	    IASTName [] ps = fKnRDtor.getParameterNames();
        	for( ; idx < ps.length; idx++ ){
        		if( ps[idx] == paramName)
        			break;
        	}
        	knrParamDtor = CVisitor.getKnRParameterDeclarator( fKnRDtor, paramName );
            if( knrParamDtor != null )
                paramName = knrParamDtor.getName();
    	}
    	
    	//create a new binding and set it for the corresponding parameter in all known defns and decls
    	binding = new CParameter( paramName );
    	IASTParameterDeclaration temp = null;
    	if( definition != null ){
    	    if( definition instanceof IASTStandardFunctionDeclarator ){
    	    	IASTParameterDeclaration [] parameters = ((IASTStandardFunctionDeclarator)definition).getParameters();
    	    	if( parameters.length > idx ) {
	    	        temp = parameters[idx];
	    	        ASTQueries.findInnermostDeclarator(temp.getDeclarator()).getName().setBinding( binding );
    	    	}
    	    } else if( definition instanceof ICASTKnRFunctionDeclarator ){
    	    	fKnRDtor = (ICASTKnRFunctionDeclarator) definition;
    	    	IASTName [] parameterNames = fKnRDtor.getParameterNames();
    	    	if( parameterNames.length > idx ) {
	    	        IASTName n = parameterNames[idx];
	    	        n.setBinding( binding );
	    	        IASTDeclarator dtor = CVisitor.getKnRParameterDeclarator( fKnRDtor, n );
	    	        if( dtor != null ){
	    	            dtor.getName().setBinding( binding );
	    	        }
    	    	}
    	    }
    	}
    	if( declarators != null ){
    		for( int j = 0; j < declarators.length && declarators[j] != null; j++ ){
    		    if( declarators[j].getParameters().length > idx ){
					temp = declarators[j].getParameters()[idx];
					ASTQueries.findInnermostDeclarator(temp.getDeclarator()).getName().setBinding( binding );
    		    }
    		}
    	}
    	return binding;
    }
    

    
    protected void updateParameterBindings( IASTFunctionDeclarator fdtor ){
        IParameter [] params = getParameters();
        if( fdtor instanceof IASTStandardFunctionDeclarator ){
        	IASTParameterDeclaration [] nps = ((IASTStandardFunctionDeclarator)fdtor).getParameters();
        	if(params.length < nps.length )
        	    return; 
        	for( int i = 0; i < nps.length; i++ ){
        		IASTName name = ASTQueries.findInnermostDeclarator(nps[i].getDeclarator()).getName();
        		name.setBinding( params[i] );
        		if( params[i] instanceof CParameter )
        			((CParameter)params[i]).addDeclaration( name );
        	}
        } else {
            IASTName [] ns = ((ICASTKnRFunctionDeclarator)fdtor).getParameterNames();
            if( params.length > 0 && params.length != ns.length )
                return; //problem
            
            for( int i = 0; i < params.length; i++ ){
            	IASTName name = ns[i];
            	name.setBinding( params[i] );
            	IASTDeclarator dtor = CVisitor.getKnRParameterDeclarator( (ICASTKnRFunctionDeclarator) fdtor, name );
    			if( dtor != null ){
    			    dtor.getName().setBinding( params[i] );
    			    if( params[i] instanceof CParameter )
    			    	((CParameter)params[i]).addDeclaration( dtor.getName() );
    			}
        	}
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#isStatic()
     */
    public boolean isStatic() {
    	return isStatic(true);
    }
    
    public boolean isStatic(boolean resolveAll) {
        if( resolveAll && (bits & FULLY_RESOLVED) == 0 ){
            resolveAllDeclarations();
        }
		return hasStorageClass( IASTDeclSpecifier.sc_static );
    }

	public boolean hasStorageClass( int storage){
	    IASTDeclarator dtor = definition;
	    IASTDeclarator[] ds = declarators;

        int i = -1;
        do{ 
            if( dtor != null ){
	            IASTNode parent = dtor.getParent();
	            while( !(parent instanceof IASTDeclaration) )
	                parent = parent.getParent();
	            
	            IASTDeclSpecifier declSpec = null;
	            if( parent instanceof IASTSimpleDeclaration ){
	                declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	            } else if( parent instanceof IASTFunctionDefinition )
	                declSpec = ((IASTFunctionDefinition)parent).getDeclSpecifier();
	            
	            if( declSpec != null && declSpec.getStorageClass() == storage ) {
	            	return true;
	            }
            }
            
            if( ds != null && ++i < ds.length )
                dtor = ds[i];
            else
            	break;
        } while( dtor != null );
        return false;
	}
	

    public boolean isExtern() {
    	return isExtern(true);
    }
    
    public boolean isExtern(boolean resolveAll) {
        if( resolveAll && (bits & FULLY_RESOLVED) == 0 ){
            resolveAllDeclarations();
        }
        return hasStorageClass( IASTDeclSpecifier.sc_extern);
    }


    public boolean isAuto() {
        if( (bits & FULLY_RESOLVED) == 0 ){
            resolveAllDeclarations();
        }
        return hasStorageClass( IASTDeclSpecifier.sc_auto);
    }

  
    public boolean isRegister() {
        if( (bits & FULLY_RESOLVED) == 0 ){
            resolveAllDeclarations();
        }
        return hasStorageClass( IASTDeclSpecifier.sc_register);
    }

 
    public boolean isInline() {
        if( (bits & FULLY_RESOLVED) == 0 ){
            resolveAllDeclarations();
        }
	    IASTDeclarator dtor = definition;
	    IASTDeclarator[] ds = declarators;
        int i = -1;
        do{
            if( dtor != null ){
	            IASTNode parent = dtor.getParent();
	            while( !(parent instanceof IASTDeclaration) )
	                parent = parent.getParent();
	            
	            IASTDeclSpecifier declSpec = null;
	            if( parent instanceof IASTSimpleDeclaration ){
	                declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	            } else if( parent instanceof IASTFunctionDefinition )
	                declSpec = ((IASTFunctionDefinition)parent).getDeclSpecifier();
	
	            if( declSpec != null && declSpec.isInline() )
	                return true;
            }
            if( ds != null && ++i < ds.length )
                dtor = ds[i];
            else
                break;
        } while( dtor != null );
        
        return false;
    }


    public boolean takesVarArgs() {
        if( (bits & FULLY_RESOLVED) == 0 ){
            resolveAllDeclarations();
        }
           
        if( definition != null ){
            if( definition instanceof IASTStandardFunctionDeclarator )
                return ((IASTStandardFunctionDeclarator)definition).takesVarArgs();
            return false;
        }

        if( declarators != null && declarators.length > 0 ){
            return declarators[0].takesVarArgs();
        }
        return false;
    }

	public void setFullyResolved(boolean resolved) {
		if( resolved )
			bits |= FULLY_RESOLVED;
		else 
			bits &= ~FULLY_RESOLVED;
	}

	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	public IASTNode[] getDeclarations() {
		return declarators;
	}

	public IASTNode getDefinition() {
		return definition;
	}
	
	public IBinding getOwner() throws DOMException {
		return null;
	}
}
