/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Sergey Prigogin (Google)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
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
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.core.runtime.PlatformObject;

/**
 * 
 * @author aniefer
 */
public class CPPClassType extends PlatformObject implements ICPPInternalClassTypeMixinHost {
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
		public ICPPMethod[] getDeclaredConversionOperators() throws DOMException {
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
		@Override
		public IScope getParent() throws DOMException {
			throw new DOMException( this );
		}
		@Override
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
		public ICPPClassType[] getNestedClasses() throws DOMException {
			throw new DOMException( this );
		}
	}

	private class FindDefinitionAction extends CPPASTVisitor {
		private char [] nameArray = CPPClassType.this.getNameCharArray();
		public IASTName result = null;

		{
			shouldVisitNames          = true;
			shouldVisitDeclarations   = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitDeclarators    = true;
		}

		@Override
		public int visit( IASTName name ){
			if( name instanceof ICPPASTTemplateId )
				return PROCESS_SKIP;
			if( name instanceof ICPPASTQualifiedName )
				return PROCESS_CONTINUE;
			char [] c = name.toCharArray();

			if( name.getParent() instanceof ICPPASTQualifiedName ){
				IASTName [] ns = ((ICPPASTQualifiedName)name.getParent()).getNames();
				if( ns[ ns.length - 1 ] != name )
					return PROCESS_CONTINUE;
				name = (IASTName) name.getParent();
			}

			if( name.getParent() instanceof ICPPASTCompositeTypeSpecifier &&
					CharArrayUtils.equals( c, nameArray ) ) 
			{
				IBinding binding = name.resolveBinding();
				if( binding == CPPClassType.this ){
					if( name instanceof ICPPASTQualifiedName ){
						IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
						name = ns[ ns.length - 1 ];
					}
					result = name;
					return PROCESS_ABORT;
				}
			}
			return PROCESS_CONTINUE; 
		}

		@Override
		public int visit( IASTDeclaration declaration ){ 
			if(declaration instanceof IASTSimpleDeclaration || declaration instanceof ICPPASTTemplateDeclaration)
				return PROCESS_CONTINUE;
			return PROCESS_SKIP; 
		}
		@Override
		public int visit( IASTDeclSpecifier declSpec ){
			return (declSpec instanceof ICPPASTCompositeTypeSpecifier ) ? PROCESS_CONTINUE : PROCESS_SKIP; 
		}
		@Override
		public int visit( IASTDeclarator declarator ) 			{ return PROCESS_SKIP; }
	}

	private IASTName definition;
	private IASTName [] declarations;
	private boolean checked = false;
	private ICPPClassType typeInIndex;
	private ClassTypeMixin mixin;

	public CPPClassType( IASTName name, IBinding indexBinding ){
		if( name instanceof ICPPASTQualifiedName ){
			IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
			name = ns[ ns.length - 1 ];
		}
		IASTNode parent = name.getParent();
		while( parent instanceof IASTName )
			parent = parent.getParent();

		if( parent instanceof IASTCompositeTypeSpecifier )
			definition = name;
		else 
			declarations = new IASTName[] { name };
		name.setBinding( this );
		if (indexBinding instanceof ICPPClassType && indexBinding instanceof IIndexBinding) {
			typeInIndex= (ICPPClassType) indexBinding;
		}
		mixin= new ClassTypeMixin(this);
	}

	public IASTNode[] getDeclarations() {
		return declarations;
	}

	public IASTNode getDefinition() {
		return definition;
	}

	public void checkForDefinition(){
		if( !checked ) {
			FindDefinitionAction action = new FindDefinitionAction();
			IASTNode node = CPPVisitor.getContainingBlockItem( getPhysicalNode() ).getParent();

			if( node instanceof ICPPASTCompositeTypeSpecifier )
				node = CPPVisitor.getContainingBlockItem( node.getParent() );
			while( node instanceof ICPPASTTemplateDeclaration )
				node = node.getParent();
			node.accept( action );
			definition = action.result;

			if( definition == null ){
				node.getTranslationUnit().accept( action );
				definition = action.result;
			}
			checked = true;
		}
		return;
	}

	public ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier(){
		if( definition != null ){
			IASTNode node = definition;
			while( node instanceof IASTName )
				node = node.getParent();
			if( node instanceof ICPPASTCompositeTypeSpecifier )
				return (ICPPASTCompositeTypeSpecifier)node;
		}
		return null;
	}
	
	private ICPPASTElaboratedTypeSpecifier getElaboratedTypeSpecifier() {
		if( declarations != null ){
			IASTNode node = declarations[0];
			while( node instanceof IASTName )
				node = node.getParent();
			if( node instanceof ICPPASTElaboratedTypeSpecifier )
				return (ICPPASTElaboratedTypeSpecifier)node;
		}
		return null;
	}

	public String getName() {
		return ( definition != null ) ? definition.toString() : declarations[0].toString();
	}

	public char[] getNameCharArray() {
		return ( definition != null ) ? definition.toCharArray() : declarations[0].toCharArray();
	}

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
						scope = scope.getParent();
					} catch (DOMException e1) {
					}
				}
			}
		}
		return scope;
	}

	public IScope getCompositeScope() {
		if (definition == null) {
			checkForDefinition();
		}
		if (definition != null) {
			return getCompositeTypeSpecifier().getScope();
		}
		// fwd-declarations must be backed up from the index
		if (typeInIndex != null) {
			try {
				return typeInIndex.getCompositeScope();
			} catch (DOMException e) {
				// index bindings don't throw DOMExeptions.
			}
		}
		return null;
	}

	public IASTNode getPhysicalNode() {
		return (definition != null ) ? (IASTNode) definition : declarations[0];
	}

	public int getKey() {
		if( definition != null )
			return getCompositeTypeSpecifier().getKey();

		return getElaboratedTypeSpecifier().getKind();
	}

	public void addDefinition( IASTNode node ){
		if( node instanceof ICPPASTCompositeTypeSpecifier )
			definition = ((ICPPASTCompositeTypeSpecifier)node).getName();
	}
	
	public void addDeclaration( IASTNode node ) {
		if( !(node instanceof ICPPASTElaboratedTypeSpecifier) )
			return;

		IASTName name = ((ICPPASTElaboratedTypeSpecifier) node).getName();

		if( declarations == null ){
			declarations = new IASTName[] { name };
			return;
		}

		//keep the lowest offset declaration in [0]
		if( declarations.length > 0 && ((ASTNode)node).getOffset() < ((ASTNode)declarations[0]).getOffset() ){
			declarations = (IASTName[]) ArrayUtil.prepend( IASTName.class, declarations, name );
		} else {
			declarations = (IASTName[]) ArrayUtil.append( IASTName.class, declarations, name );
		}
	}

	public void removeDeclaration(IASTNode node) {
		if( definition == node ){
			definition = null;
			return;
		}
		ArrayUtil.remove(declarations, node);
	}

	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName( this );
	}

	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray( this );
	}

	public boolean isGloballyQualified() throws DOMException {
		IScope scope = getScope();
		while( scope != null ){
			if( scope instanceof ICPPBlockScope )
				return false;
			scope = scope.getParent();
		}
		return true;
	}

	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	/*   */
	
	public boolean isSameType( IType type ) {
		if (type == this)
			return true;
		if (type instanceof ITypedef || type instanceof IIndexType)
			return type.isSameType(this);
		return false;
	}
	
	public ICPPBase [] getBases() {
		return mixin.getBases();
	}

	public IField[] getFields() throws DOMException {
		return mixin.getFields();
	}

	public ICPPField[] getDeclaredFields() throws DOMException {
		return mixin.getDeclaredFields();
	}

	public ICPPMethod[] getMethods() throws DOMException {
		return mixin.getMethods();
	}

	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		return mixin.getAllDeclaredMethods();
	}

	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		return mixin.getDeclaredMethods();
	}

	public ICPPConstructor[] getConstructors() throws DOMException {
		return mixin.getConstructors();
	}

	public IBinding[] getFriends() {
		return mixin.getFriends();
	}
	
	public ICPPClassType[] getNestedClasses() {
		return mixin.getNestedClasses();
	}

	public IField findField(String name) throws DOMException {
		return mixin.findField(name);
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
	
	/**
	 * For debugging purposes, only.
	 */
	@Override
	public String toString() {
		return getName(); 
	}
}
