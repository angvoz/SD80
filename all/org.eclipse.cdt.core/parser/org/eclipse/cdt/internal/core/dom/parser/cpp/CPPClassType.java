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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPClassType implements ICPPClassType, ICPPInternalBinding {
    public static class CPPClassTypeDelegate extends CPPDelegate implements ICPPClassType {
        public CPPClassTypeDelegate( IASTName name, ICPPClassType cls ){
            super( name, cls );
        }
        public ICPPBase[] getBases() throws DOMException {
            return ((ICPPClassType)getBinding()).getBases();
        }
        public IField[] getFields() throws DOMException {
            return ((ICPPClassType)getBinding()).getFields();
        }
        public IField findField( String name ) throws DOMException {
            return ((ICPPClassType)getBinding()).findField( name );
        }
        public ICPPField[] getDeclaredFields() throws DOMException {
            return ((ICPPClassType)getBinding()).getDeclaredFields();
        }
        public ICPPMethod[] getMethods() throws DOMException {
            return ((ICPPClassType)getBinding()).getMethods();
        }
        public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
            return ((ICPPClassType)getBinding()).getAllDeclaredMethods();
        }
        public ICPPMethod[] getDeclaredMethods() throws DOMException {
            return ((ICPPClassType)getBinding()).getDeclaredMethods();
        }
        public ICPPConstructor[] getConstructors() throws DOMException {
            return ((ICPPClassType)getBinding()).getConstructors();
        }
        public IBinding[] getFriends() throws DOMException {
            return ((ICPPClassType)getBinding()).getFriends();
        }
        public int getKey() throws DOMException {
            return ((ICPPClassType)getBinding()).getKey();
        }
        public IScope getCompositeScope() throws DOMException {
            return ((ICPPClassType)getBinding()).getCompositeScope();
        }
        public Object clone() {
            return ((ICPPClassType)getBinding()).clone();
        }
    }
	public static class CPPClassTypeProblem extends ProblemBinding implements ICPPClassType{
        public CPPClassTypeProblem( IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
        }

		public ICPPBase[] getBases() throws DOMException {
			throw new DOMException( this );
		}
		public IField[] getFields() throws DOMException {
			throw new DOMException( this );
		}
		public ICPPField[] getDeclaredFields() throws DOMException {
			throw new DOMException( this );
		}
		public ICPPMethod[] getMethods() throws DOMException {
			throw new DOMException( this );
		}
		public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
			throw new DOMException( this );
		}
		public ICPPMethod[] getDeclaredMethods() throws DOMException {
			throw new DOMException( this );
		}
		public ICPPConstructor[] getConstructors() throws DOMException {
			throw new DOMException( this );
		}
		public int getKey() throws DOMException {
			throw new DOMException( this );
		}
		public IField findField(String name) throws DOMException {
			throw new DOMException( this );
		}
		public IScope getCompositeScope() throws DOMException {
			throw new DOMException( this );
		}
		public IScope getParent() throws DOMException {
			throw new DOMException( this );
		}
		public IBinding[] find(String name) throws DOMException {
			throw new DOMException( this );
		}
        public IBinding[] getFriends() throws DOMException {
			throw new DOMException( this );
        }
        public String[] getQualifiedName() throws DOMException {
            throw new DOMException( this );
        }
        public char[][] getQualifiedNameCharArray() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isGloballyQualified() throws DOMException {
            throw new DOMException( this );
        }
    }
	
	private IASTName definition;
	private IASTName [] declarations;
	
	public CPPClassType( IASTName name ){
	    ASTNodeProperty prop = name.getPropertyInParent();
	    if( name instanceof ICPPASTQualifiedName ){
	        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
	        name = ns[ ns.length - 1 ];
	    }
	    
	    if( prop == IASTCompositeTypeSpecifier.TYPE_NAME )
			definition = name;
		else 
			declarations = new IASTName[] { name };
		name.setBinding( this );
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return declarations;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return definition;
    }
    
	private class FindDefinitionAction extends CPPASTVisitor {
	    private char [] nameArray = CPPClassType.this.getNameCharArray();
	    public IASTName result = null;
	    
	    {
	        shouldVisitNames          = true;
			shouldVisitDeclarations   = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitDeclarators    = true;
			shouldVisitNamespaces     = true;
	    }
	    
	    public int visit( IASTName name ){
	        if( name.getParent() instanceof ICPPASTCompositeTypeSpecifier &&
	            CharArrayUtils.equals( name.toCharArray(), nameArray ) ) 
	        {
	            IBinding binding = name.resolveBinding();
	            if( binding == CPPClassType.this ){
	                result = name;
	                return PROCESS_ABORT;
	            }
	        }
	        return PROCESS_CONTINUE; 
	    }
	    
		public int visit( IASTDeclaration declaration ){ 
		    return (declaration instanceof IASTSimpleDeclaration ) ? PROCESS_CONTINUE : PROCESS_SKIP; 
		}
		public int visit( IASTDeclSpecifier declSpec ){
		    return (declSpec instanceof ICPPASTCompositeTypeSpecifier ) ? PROCESS_CONTINUE : PROCESS_SKIP; 
		}
		public int processDeclarators( IASTDeclarator declarator ) 			{ return PROCESS_SKIP; }
	}
	
	private void checkForDefinition(){
		FindDefinitionAction action = new FindDefinitionAction();
		IASTNode node = CPPVisitor.getContainingBlockItem( getPhysicalNode() ).getParent();

		node.accept( action );
	    definition = action.result;
		
		if( definition == null ){
			node.getTranslationUnit().accept( action );
		    definition = action.result;
		}
		
		return;
	}
	
	private ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier(){
	    if( definition != null ){
	        return (ICPPASTCompositeTypeSpecifier) definition.getParent();
	    }
	    return null;
	}
	private ICPPASTElaboratedTypeSpecifier getElaboratedTypeSpecifier() {
	    if( declarations != null )
	        return (ICPPASTElaboratedTypeSpecifier) declarations[0].getParent();
	    return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() throws DOMException {
	    if( definition == null ){
	        checkForDefinition();
	        if( definition == null ){
	            IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
	            return new IField [] { new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
	        }
	    }

		IField[] fields = getDeclaredFields();
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
            fields = (IField[]) ArrayUtil.addAll( IField.class, fields, bases[i].getBaseClass().getFields() );
        }
		return (IField[]) ArrayUtil.trim( IField.class, fields );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#findField(java.lang.String)
	 */
	public IField findField(String name) throws DOMException {
		IBinding [] bindings = CPPSemantics.findBindings( getCompositeScope(), name, true );
		IField field = null;
		for ( int i = 0; i < bindings.length; i++ ) {
            if( bindings[i] instanceof IField ){
                if( field == null )
                    field = (IField) bindings[i];
                else {
                    IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                    return new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, name.toCharArray() );
                }
            }
        }
		return field;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return ( definition != null ) ? definition.toString() : declarations[0].toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return ( definition != null ) ? definition.toCharArray() : declarations[0].toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		IASTName name = definition != null ? definition : declarations[0];

		IScope scope = CPPVisitor.getContainingScope( name );
		if( definition == null && name.getPropertyInParent() != ICPPASTQualifiedName.SEGMENT_NAME ){
		    IASTNode node = declarations[0].getParent().getParent();
		    if( node instanceof IASTFunctionDefinition || node instanceof IASTParameterDeclaration ||
		        ( node instanceof IASTSimpleDeclaration && 
		          ( ((IASTSimpleDeclaration) node).getDeclarators().length > 0 || getElaboratedTypeSpecifier().isFriend() ) ) )
		    {
	            while( scope instanceof ICPPClassScope || scope instanceof ICPPFunctionScope ){
					try {
						scope = (ICPPScope) scope.getParent();
					} catch (DOMException e1) {
					}
				}
		    }
		}
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() {
		return (definition != null ) ? getCompositeTypeSpecifier().getScope() : null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() {
		return (definition != null ) ? (IASTNode) definition : declarations[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() {
	    if( definition != null )
	        return getCompositeTypeSpecifier().getKey();
	    
		return getElaboratedTypeSpecifier().getKind();
	}

	public void addDefinition( ICPPASTCompositeTypeSpecifier compSpec ){
		definition = compSpec.getName();
	}
	public void addDeclaration( ICPPASTElaboratedTypeSpecifier elabSpec ) {
		if( declarations == null ){
			declarations = new IASTName[] { elabSpec.getName() };
			return;
		}

        for( int i = 0; i < declarations.length; i++ ){
            if( declarations[i] == null ){
                declarations[i] = elabSpec.getName();
                return;
            }
        }
        IASTName tmp [] = new IASTName[ declarations.length * 2 ];
        System.arraycopy( declarations, 0, tmp, 0, declarations.length );
        tmp[ declarations.length ] = elabSpec.getName();
        declarations = tmp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase [] getBases() {
		if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPBase [] { new CPPBaseClause.CPPBaseProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
		ICPPASTBaseSpecifier [] bases = getCompositeTypeSpecifier().getBaseSpecifiers();
		if( bases.length == 0 )
		    return ICPPBase.EMPTY_BASE_ARRAY;
		
		ICPPBase [] bindings = new ICPPBase[ bases.length ];
		for( int i = 0; i < bases.length; i++ ){
		    bindings[i] = new CPPBaseClause( bases[i] );
		}
		
		return bindings; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredFields()
	 */
	public ICPPField[] getDeclaredFields() throws DOMException {
	    if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPField[] { new CPPField.CPPFieldProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
	    IBinding binding = null;
	    ICPPField [] result = null;
	    
	    IASTDeclaration [] decls = getCompositeTypeSpecifier().getMembers();
	    for ( int i = 0; i < decls.length; i++ ) {
            if( decls[i] instanceof IASTSimpleDeclaration ){
                IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decls[i]).getDeclarators();
                for ( int j = 0; j < dtors.length; j++ ) {
                    binding = dtors[j].getName().resolveBinding();
                    if( binding instanceof ICPPField )
                        result = (ICPPField[]) ArrayUtil.append( ICPPField.class, result, binding );
                }
            } else if( decls[i] instanceof ICPPASTUsingDeclaration ){
                IASTName n = ((ICPPASTUsingDeclaration)decls[i]).getName();
                binding = n.resolveBinding();
                if( binding instanceof ICPPUsingDeclaration ){
                    IBinding [] bs = ((ICPPUsingDeclaration)binding).getDelegates();
                    for ( int j = 0; j < bs.length; j++ ) {
                        if( bs[j] instanceof ICPPField )
                            result = (ICPPField[]) ArrayUtil.append( ICPPField.class, result, bs[j] );
                    }
                } else if( binding instanceof ICPPField ) {
                    result = (ICPPField[]) ArrayUtil.append( ICPPField.class, result, binding );
                }
            }
        }
		return (ICPPField[]) ArrayUtil.trim( ICPPField.class, result );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getMethods()
	 */
	public ICPPMethod[] getMethods() throws DOMException {
		ObjectSet set = new ObjectSet(2);
		ICPPMethod [] ms = getDeclaredMethods();
		set.addAll( ms );
		ICPPClassScope scope = (ICPPClassScope) getCompositeScope();
		set.addAll( scope.getImplicitMethods() );
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
			set.addAll( bases[i].getBaseClass().getMethods() );
        }
		return (ICPPMethod[]) ArrayUtil.trim( ICPPMethod.class, set.keyArray(), true );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getAllDeclaredMethods()
	 */
	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		if( definition == null ){
	        checkForDefinition();
	        if( definition == null ){
	            IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
	            return new ICPPMethod [] { new CPPMethod.CPPMethodProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
	        }
	    }

		ICPPMethod[] methods = getDeclaredMethods();
		ICPPBase [] bases = getBases();
		for ( int i = 0; i < bases.length; i++ ) {
            methods = (ICPPMethod[]) ArrayUtil.addAll( ICPPMethod.class, methods, bases[i].getBaseClass().getAllDeclaredMethods() );
        }
		return (ICPPMethod[]) ArrayUtil.trim( ICPPMethod.class, methods );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredMethods()
	 */
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
	    if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPMethod[] { new CPPMethod.CPPMethodProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
	    IBinding binding = null;
	    ICPPMethod [] result = null;
	    
	    IASTDeclaration [] decls = getCompositeTypeSpecifier().getMembers();
	    for ( int i = 0; i < decls.length; i++ ) {
            if( decls[i] instanceof IASTSimpleDeclaration ){
                IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decls[i]).getDeclarators();
                for ( int j = 0; j < dtors.length; j++ ) {
                    binding = dtors[j].getName().resolveBinding();
                    if( binding instanceof ICPPMethod)
                        result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
                }
            } else if( decls[i] instanceof IASTFunctionDefinition ){
                IASTDeclarator dtor = ((IASTFunctionDefinition)decls[i]).getDeclarator();
                dtor = CPPVisitor.getMostNestedDeclarator( dtor );
                binding = dtor.getName().resolveBinding();
                if( binding instanceof ICPPMethod ){
                    result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
                }
            } else if( decls[i] instanceof ICPPASTUsingDeclaration ){
                IASTName n = ((ICPPASTUsingDeclaration)decls[i]).getName();
                binding = n.resolveBinding();
                if( binding instanceof ICPPUsingDeclaration ){
                    IBinding [] bs = ((ICPPUsingDeclaration)binding).getDelegates();
                    for ( int j = 0; j < bs.length; j++ ) {
                        if( bs[j] instanceof ICPPMethod )
                            result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, bs[j] );
                    }
                } else if( binding instanceof ICPPMethod ) {
                    result = (ICPPMethod[]) ArrayUtil.append( ICPPMethod.class, result, binding );
                }
            }
        }
		return (ICPPMethod[]) ArrayUtil.trim( ICPPMethod.class, result );
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getConstructors()
     */
    public ICPPConstructor[] getConstructors() throws DOMException {
        if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new ICPPConstructor [] { new CPPConstructor.CPPConstructorProblem( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
        
        ICPPClassScope scope = (ICPPClassScope) getCompositeScope();
        if( scope.isFullyCached() )
        	return ((CPPClassScope)scope).getConstructors( true );
        	
        IASTDeclaration [] members = getCompositeTypeSpecifier().getMembers();
        for( int i = 0; i < members.length; i++ ){
			if( members[i] instanceof IASTSimpleDeclaration ){
			    IASTDeclarator [] dtors = ((IASTSimpleDeclaration)members[i]).getDeclarators();
			    for( int j = 0; j < dtors.length; j++ ){
			        if( dtors[j] == null ) break;
		            scope.addName( dtors[j].getName() );
			    }
			} else if( members[i] instanceof IASTFunctionDefinition ){
			    IASTDeclarator dtor = ((IASTFunctionDefinition)members[i]).getDeclarator();
			    scope.addName( dtor.getName() );
			}
        }
        
        return ((CPPClassScope)scope).getConstructors( true );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getFriends()
     */
    public IBinding[] getFriends() {
        if( definition == null ){
            checkForDefinition();
            if( definition == null ){
                IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
                return new IBinding [] { new ProblemBinding( node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
            }
        }
        ObjectSet resultSet = new ObjectSet(2);
        IASTDeclaration [] members = getCompositeTypeSpecifier().getMembers();
        for( int i = 0; i < members.length; i++ ){
			if( members[i] instanceof IASTSimpleDeclaration ){
			    ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)members[i]).getDeclSpecifier();
			    if( declSpec.isFriend() ){
			        IASTDeclarator [] dtors = ((IASTSimpleDeclaration)members[i]).getDeclarators();
			        if( declSpec instanceof ICPPASTElaboratedTypeSpecifier && dtors.length == 0 ){
			        	resultSet.put( ((ICPPASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding() );
			        } else {
					    for( int j = 0; j < dtors.length; j++ ){
					        if( dtors[j] == null ) break;
					        resultSet.put( dtors[j].getName().resolveBinding() );
					    }    
			        }
			    }
			} else if( members[i] instanceof IASTFunctionDefinition ){
			    ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTFunctionDefinition)members[i]).getDeclSpecifier();
			    if( declSpec.isFriend() ){
			        IASTDeclarator dtor = ((IASTFunctionDefinition)members[i]).getDeclarator();
			        resultSet.put( dtor.getName().resolveBinding() );
			    }
			    
			}
        }
        
        return (IBinding[]) ArrayUtil.trim( IBinding.class, resultSet.keyArray(), true );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedName()
     */
    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#getQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while( scope != null ){
            if( scope instanceof ICPPBlockScope )
                return false;
            scope = scope.getParent();
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate( IASTName name ) {
        return new CPPClassTypeDelegate( name, this );
    }
}
