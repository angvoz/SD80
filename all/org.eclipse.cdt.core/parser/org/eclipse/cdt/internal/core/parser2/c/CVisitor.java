
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
package org.eclipse.cdt.internal.core.parser2.c;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CVisitor {
	public static abstract class BaseVisitorAction {
		public boolean processNames          = false;
		public boolean processDeclarations   = false;
		public boolean processParameterDeclarations = false;
		public boolean processDeclarators    = false;
		public boolean processDeclSpecifiers = false;
		public boolean processExpressions    = false;
		public boolean processStatements     = false;
		public boolean processTypeIds        = false;
		
		/**
		 * @return true to continue visiting, return false to stop
		 */
		public boolean processName( IASTName name ) 					{ return true; }
		public boolean processDeclaration( IASTDeclaration declaration ){ return true; }
		public boolean processParameterDeclaration( IASTParameterDeclaration parameterDeclaration ) { return true; }
		public boolean processDeclarator( IASTDeclarator declarator )   { return true; }
		public boolean processDeclSpecifier( IASTDeclSpecifier declSpec ){return true; }
		public boolean processExpression( IASTExpression expression )   { return true; }
		public boolean processStatement( IASTStatement statement )      { return true; }
		public boolean processTypeId( IASTTypeId typeId )               { return true; }
	}
	
	public static class ClearBindingAction extends BaseVisitorAction {
		{
			processNames = true;
		}
		public boolean processName(IASTName name) {
			((CASTName) name ).setBinding( null );
			return true;
		}
	}
	
	//Scopes
	private static final int COMPLETE = 1;
	private static final int CURRENT_SCOPE = 2;
	
	static protected void createBinding( CASTName name ){
		IBinding binding = null;
		IASTNode parent = name.getParent();
		
		if( parent instanceof CASTIdExpression ){
			binding = resolveBinding( parent );
		} else if( parent instanceof ICASTTypedefNameSpecifier ){
			binding = resolveBinding( parent );
		} else if( parent instanceof IASTFieldReference ){
			binding = findBinding( (IASTFieldReference) parent );
		} else if( parent instanceof IASTDeclarator ){
			binding = createBinding( (IASTDeclarator) parent, name );
		} else if( parent instanceof ICASTCompositeTypeSpecifier ){
			binding = createBinding( (ICASTCompositeTypeSpecifier) parent );
		} else if( parent instanceof ICASTElaboratedTypeSpecifier ){
			binding = createBinding( (ICASTElaboratedTypeSpecifier) parent );
		} else if( parent instanceof IASTStatement ){
		    binding = createBinding ( (IASTStatement) parent );
		}
		name.setBinding( binding );
	}

	private static IBinding createBinding( IASTStatement statement ){
	    if( statement instanceof IASTGotoStatement ){
	        IScope scope = getContainingScope( statement );
	        while( scope != null && !( scope instanceof ICFunctionScope) ){
	            scope = scope.getParent();
	        }
	        if( scope != null && scope instanceof ICFunctionScope ){
	            CFunctionScope functionScope = (CFunctionScope) scope;
	            List labels = functionScope.getLabels();
	            for( int i = 0; i < labels.size(); i++ ){
	                ILabel label = (ILabel) labels.get(i);
	                if( label.getName().equals( ((IASTGotoStatement)statement).getName().toString() ) ){
	                    return label;
	                }
	            }
	        }
	    } else if( statement instanceof IASTLabelStatement ){
	        return new CLabel( (IASTLabelStatement) statement );
	    }
	    return null;
	}
	private static IBinding createBinding( ICASTElaboratedTypeSpecifier elabTypeSpec ){
		IASTNode parent = elabTypeSpec.getParent();
		if( parent instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) parent;
			if( declaration.getDeclarators().size() == 0 ){
				//forward declaration
				IBinding binding = resolveBinding( elabTypeSpec, CURRENT_SCOPE );
				if( binding == null )
					binding = new CStructure( elabTypeSpec );
				return binding;
			} 
			return resolveBinding( elabTypeSpec );
		} else if( parent instanceof IASTTypeId ){
			IASTNode blockItem = getContainingBlockItem( parent );
			return findBinding( blockItem, (CASTName) elabTypeSpec.getName(), COMPLETE );
		}
		return null;
	}
	private static IBinding findBinding( IASTFieldReference fieldReference ){
		IASTExpression fieldOwner = fieldReference.getFieldOwner();
		ICompositeType compositeType = null;
		if( fieldOwner instanceof IASTIdExpression ){
			IBinding binding = resolveBinding( fieldOwner );
			if( binding instanceof IVariable ){
				binding = ((IVariable)binding).getType();
				while( binding != null && binding instanceof ITypedef )
					binding = ((ITypedef)binding).getType();
			}
			if( binding instanceof ICompositeType )
				compositeType = (ICompositeType) binding;
		} else if( fieldOwner instanceof IASTUnaryTypeIdExpression ){
			IASTTypeId id = ((IASTUnaryTypeIdExpression)fieldOwner).getTypeId();
			IBinding binding = resolveBinding( id );
			if( binding != null && binding instanceof ICompositeType ){
				compositeType = (ICompositeType) binding;
			}
		}

		IBinding binding = null;
		if( compositeType != null ){
			binding = compositeType.findField( fieldReference.getFieldName().toString() );
		}
		return binding;
	}
	
	/**
	 * @param parent
	 * @return
	 */
	private static IBinding createBinding(IASTDeclarator declarator, CASTName name) {
		IBinding binding = null;
		IASTNode parent = declarator.getParent();
		if( declarator instanceof IASTFunctionDeclarator ){
			binding = resolveBinding( parent, CURRENT_SCOPE );
			if( binding == null )
				binding = new CFunction( (IASTFunctionDeclarator) declarator );
		} else if( parent instanceof IASTSimpleDeclaration ){
			binding = createBinding( (IASTSimpleDeclaration) parent, name );
		} else if( parent instanceof IASTParameterDeclaration ){
			binding = createBinding( (IASTParameterDeclaration ) parent );
		}
		
		return binding;
	}

	private static IBinding createBinding( ICASTCompositeTypeSpecifier compositeTypeSpec ){
		return new CStructure( compositeTypeSpec );
	}
	

	/**
	 * @param parent
	 * @return
	 */
	private static IBinding createBinding(IASTSimpleDeclaration simpleDeclaration, IASTName name) {
		IBinding binding = null;
		if( simpleDeclaration.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef ){
			binding = new CTypeDef( name );
		} else if( simpleDeclaration.getParent() instanceof ICASTCompositeTypeSpecifier ){
			binding = new CField( name );
		} else {
			binding = new CVariable( name );
		}
 
		return binding;
	}

	private static IBinding createBinding( IASTParameterDeclaration parameterDeclaration ){
		IBinding binding = resolveBinding( parameterDeclaration, CURRENT_SCOPE );
		if( binding == null )
			binding = new CParameter( parameterDeclaration );
		return binding;
	}
	
	protected static IBinding resolveBinding( IASTNode node ){
		return resolveBinding( node, COMPLETE );
	}
	protected static IBinding resolveBinding( IASTNode node, int scopeDepth ){
		if( node instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) node;
			IASTFunctionDeclarator functionDeclartor = functionDef.getDeclarator();
			IASTName name = functionDeclartor.getName();
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, (CASTName) name, scopeDepth );
		} else if( node instanceof IASTIdExpression ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, (CASTName) ((IASTIdExpression)node).getName(), scopeDepth );
		} else if( node instanceof ICASTTypedefNameSpecifier ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, (CASTName) ((ICASTTypedefNameSpecifier)node).getName(), scopeDepth );
		} else if( node instanceof ICASTElaboratedTypeSpecifier ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, (CASTName) ((ICASTElaboratedTypeSpecifier)node).getName(), scopeDepth );
		} else if( node instanceof ICASTCompositeTypeSpecifier ){
			IASTNode blockItem = getContainingBlockItem( node );
			return findBinding( blockItem, (CASTName) ((ICASTCompositeTypeSpecifier)node).getName(), scopeDepth );
		} else if( node instanceof IASTParameterDeclaration ){
			IASTParameterDeclaration param = (IASTParameterDeclaration) node;
			IASTFunctionDeclarator fDtor = (IASTFunctionDeclarator) param.getParent();
			if( fDtor.getParent() instanceof IASTFunctionDefinition ){
				return null;
			}
			IASTFunctionDeclarator fdef = findDefinition( fDtor );
			if( fdef != null ){
				int index = fDtor.getParameters().indexOf( param );
				if( index >= 0 && index < fdef.getParameters().size() ){
					IASTParameterDeclaration pdef = (IASTParameterDeclaration) fdef.getParameters().get( index );
					return pdef.getDeclarator().getName().resolveBinding();
				}
			}
		} else if( node instanceof IASTTypeId ){
			IASTTypeId typeId = (IASTTypeId) node;
			IASTDeclSpecifier declSpec = typeId.getDeclSpecifier();
			IASTName name = null;
			if( declSpec instanceof ICASTElaboratedTypeSpecifier ){
				name = ((ICASTElaboratedTypeSpecifier)declSpec).getName();
			} else if( declSpec instanceof ICASTCompositeTypeSpecifier ){
				name = ((ICASTCompositeTypeSpecifier)declSpec).getName();
			}
			if( name != null ){
				return name.resolveBinding();
			}
		}
		return null;
	}
	
	/**
	 * @param declaration
	 * @return
	 */
	public static IScope getContainingScope(IASTDeclaration declaration) {
		IASTNode parent = declaration.getParent();
		if( parent instanceof IASTTranslationUnit ){
			return ((IASTTranslationUnit)parent).getScope();
		} else if( parent instanceof IASTDeclarationStatement ){
			return getContainingScope( (IASTStatement) parent );
		}
		
		return null;
	}
	
	public static IScope getContainingScope( IASTStatement statement ){
		IASTNode parent = statement.getParent();
		if( parent instanceof IASTStatement ){
			return getContainingScope( (IASTStatement)parent );
		} else if( parent instanceof IASTFunctionDefinition ){
			IASTFunctionDeclarator fnDeclarator = ((IASTFunctionDefinition) parent ).getDeclarator();
			IFunction function = (IFunction) fnDeclarator.getName().resolveBinding();
			return function.getFunctionScope();
		}
		
		return null;
	}
	
	public static IScope getContainingScope( IASTDeclSpecifier compTypeSpec ){
		return null;
	}

	/**
	 * @param parameterDeclaration
	 * @return
	 */
	public static IScope getContainingScope(IASTParameterDeclaration parameterDeclaration) {
		IASTNode parent = parameterDeclaration.getParent();
		if( parent instanceof IASTFunctionDeclarator ){
			IASTFunctionDeclarator functionDeclarator = (IASTFunctionDeclarator) parent;
			IASTName fnName = functionDeclarator.getName();
			IFunction function = (IFunction) fnName.resolveBinding();
			return function.getFunctionScope();
		}
		
		return null;
	}
	
	private static IASTNode getContainingBlockItem( IASTNode node ){
		IASTNode parent = node.getParent();
		if( parent instanceof IASTDeclaration ){
			IASTNode p = parent.getParent();
			if( p instanceof IASTDeclarationStatement )
				return p;
			return parent;
		}
		//if parent is something that can contain a declaration
		else if ( parent instanceof IASTCompoundStatement || 
				  parent instanceof IASTTranslationUnit   ||
				  parent instanceof IASTForStatement )
		{
			return node;
		}
		
		return getContainingBlockItem( parent );
	}
	
	protected static IBinding findBinding( IASTNode blockItem, CASTName name, int scopeDepth ){
		IBinding binding = null;
		while( blockItem != null ){
			
			IASTNode parent = blockItem.getParent();
			List list = null;
			if( parent instanceof IASTCompoundStatement ){
				IASTCompoundStatement compound = (IASTCompoundStatement) parent;
				list = compound.getStatements();
			} else if ( parent instanceof IASTTranslationUnit ){
				IASTTranslationUnit translation = (IASTTranslationUnit) parent;
				list = translation.getDeclarations();
			}
			
			if( list != null ){
				for( int i = 0; i < list.size(); i++ ){
					IASTNode node = (IASTNode) list.get(i);
					if( node == blockItem )
						break;
					if( node instanceof IASTDeclarationStatement ){
						IASTDeclarationStatement declStatement = (IASTDeclarationStatement) node;
						binding = checkForBinding( declStatement.getDeclaration(), name );
					} else if( node instanceof IASTDeclaration ){
						binding = checkForBinding( (IASTDeclaration) node, name );
					}
					if( binding != null )
						return binding;
				}
			} else {
				//check the parent
				if( parent instanceof IASTDeclaration ){
					binding = checkForBinding( (IASTDeclaration) parent, name );
					if( binding != null )
						return binding;
				} else if( parent instanceof IASTStatement ){
					binding = checkForBinding( (IASTStatement) parent, name );
					if( binding != null )
						return binding;
				}
			}
			if( scopeDepth == COMPLETE )
				blockItem = parent;
			else 
				blockItem = null;
		}
		
		return null;
	}
	
	private static IBinding checkForBinding( IASTDeclaration declaration, CASTName name ){
		if( declaration instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
			List declarators = simpleDeclaration.getDeclarators();
			int size = declarators.size();

			for( int i = 0; i < size; i++ ){
				IASTDeclarator declarator = (IASTDeclarator) declarators.get(i);
				CASTName declaratorName = (CASTName) declarator.getName();
				if( CharArrayUtils.equals( declaratorName.toCharArray(), name.toCharArray() ) ){
					return declaratorName.resolveBinding();
				}
			}

			//decl spec 
			IASTDeclSpecifier declSpec = simpleDeclaration.getDeclSpecifier();
			if( declSpec instanceof ICASTElaboratedTypeSpecifier ){
				CASTName elabName = (CASTName) ((ICASTElaboratedTypeSpecifier)declSpec).getName();
				if( CharArrayUtils.equals( elabName.toCharArray(), name.toCharArray() ) ){
					return elabName.resolveBinding();
				}
			} else if( declSpec instanceof ICASTCompositeTypeSpecifier ){
				CASTName compName = (CASTName) ((ICASTCompositeTypeSpecifier)declSpec).getName();
				if( CharArrayUtils.equals( compName.toCharArray(), name.toCharArray() ) ){
					return compName.resolveBinding();
				}
			}
		} else if( declaration instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) declaration;
			IASTFunctionDeclarator declarator = functionDef.getDeclarator();
			
			//check the function itself
			CASTName declName = (CASTName) declarator.getName();
			if( CharArrayUtils.equals( declName.toCharArray(), name.toCharArray() ) ){
				return declName.resolveBinding();
			}
			//check the parameters
			List parameters = declarator.getParameters();
			for( int i = 0; i < parameters.size(); i++ ){
				IASTParameterDeclaration parameterDeclaration = (IASTParameterDeclaration) parameters.get(i);
				declName = (CASTName) parameterDeclaration.getDeclarator().getName();
				if( CharArrayUtils.equals( declName.toCharArray(), name.toCharArray() ) ){
					return declName.resolveBinding();
				}
			}
		}
		return null;
	}
	
	private static IBinding checkForBinding( IASTStatement statement, CASTName name ){
		if( statement instanceof IASTDeclarationStatement ){
			return checkForBinding( ((IASTDeclarationStatement)statement).getDeclaration(), name );
		} else if( statement instanceof IASTForStatement ){
			IASTForStatement forStatement = (IASTForStatement) statement;
			if( forStatement.getInitDeclaration() != null ){
				return checkForBinding( forStatement.getInitDeclaration(), name );
			}
		}
		return null;
	}
	
	protected static IASTFunctionDeclarator findDefinition( IASTFunctionDeclarator declarator ){
		return (IASTFunctionDeclarator) findDefinition( declarator, declarator.getName().toString() );
	}
	protected static IASTDeclSpecifier findDefinition( ICASTElaboratedTypeSpecifier declSpec ){
		String elabName = declSpec.getName().toString();
		return (IASTDeclSpecifier) findDefinition(declSpec, elabName);
	}

	private static IASTNode findDefinition(IASTNode decl, String declName) {
		IASTNode blockItem = getContainingBlockItem( decl );
		IASTNode parent = blockItem.getParent();
		List list = null;
		if( parent instanceof IASTCompoundStatement ){
			IASTCompoundStatement compound = (IASTCompoundStatement) parent;
			list = compound.getStatements();
		} else if ( parent instanceof IASTTranslationUnit ){
			IASTTranslationUnit translation = (IASTTranslationUnit) parent;
			list = translation.getDeclarations();
		}
		if( list != null ){
			for( int i = 0; i < list.size(); i++ ){
				IASTNode node = (IASTNode) list.get(i);
				if( node == blockItem )
					continue;
				if( node instanceof IASTDeclarationStatement ){
					node = ((IASTDeclarationStatement) node).getDeclaration();
				}
				
				if( node instanceof IASTFunctionDefinition && decl instanceof IASTFunctionDeclarator ){
					IASTFunctionDeclarator dtor = ((IASTFunctionDefinition) node).getDeclarator();
					IASTName name = dtor.getName();
					if( name.toString().equals( declName )){
						return dtor;
					}
				} else if( node instanceof IASTSimpleDeclaration && decl instanceof ICASTElaboratedTypeSpecifier){
					IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) node;
					if( simpleDecl.getDeclSpecifier() instanceof ICASTCompositeTypeSpecifier ){
						ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) simpleDecl.getDeclSpecifier();
						IASTName name = compTypeSpec.getName();
						if( name.toString().equals( declName ) ){
							return compTypeSpec;
						}
					}
				}
			}
		}
		return null;
	}
	
	public static void clearBindings( IASTTranslationUnit tu ){
		visitTranslationUnit( tu, new ClearBindingAction() ); 
	}
	
	public static void visitTranslationUnit( IASTTranslationUnit tu, BaseVisitorAction action ){
		List decls = tu.getDeclarations();
		for( int i = 0; i < decls.size(); i++ ){
			if( !visitDeclaration( (IASTDeclaration) decls.get(i), action ) ) return;
		}
	}
	
	public static boolean visitName( IASTName name, BaseVisitorAction action ){
		if( action.processNames )
			return action.processName( name );
		return true;
	}
	
	public static boolean visitDeclaration( IASTDeclaration declaration, BaseVisitorAction action ){
		if( action.processDeclarations )
			if( !action.processDeclaration( declaration ) ) return false;
		
		if( declaration instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
			if( !visitDeclSpecifier( simpleDecl.getDeclSpecifier(), action ) ) return false;
			List list = simpleDecl.getDeclarators();
			for( int i = 0; i < list.size(); i++ ){
				if( !visitDeclarator( (IASTDeclarator) list.get(i), action ) ) return false;
			}
		} else if( declaration instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition fnDef = (IASTFunctionDefinition) declaration;
			if( !visitDeclSpecifier( fnDef.getDeclSpecifier(), action ) ) return false;
			if( !visitDeclarator( fnDef.getDeclarator(), action ) ) return false;
			if( !visitStatement( fnDef.getBody(), action ) ) return false;
		}
		return true;
	}
	public static boolean visitDeclarator( IASTDeclarator declarator, BaseVisitorAction action ){
		if( action.processDeclarators )
			if( !action.processDeclarator( declarator ) ) return false;
		
		if( !visitName( declarator.getName(), action ) ) return false;
		
		if( declarator.getNestedDeclarator() != null )
			if( !visitDeclarator( declarator.getNestedDeclarator(), action ) ) return false;
		
		//TODO: if( declarator.getInitializer() != null )
		
		if( declarator instanceof IASTFunctionDeclarator ){
			List list = ((IASTFunctionDeclarator)declarator).getParameters();
			for( int i = 0; i < list.size(); i++ ){
				IASTParameterDeclaration param = (IASTParameterDeclaration) list.get(i);
				if( !visitDeclarator( param.getDeclarator(), action ) ) return false;
			}
		}
		return true;
	}
	public static boolean visitParameterDeclaration( IASTParameterDeclaration parameterDeclaration, BaseVisitorAction action ){
	    if( action.processParameterDeclarations )
	        if( !action.processParameterDeclaration( parameterDeclaration ) ) return false;
	    
	    if( !visitDeclSpecifier( parameterDeclaration.getDeclSpecifier(), action ) ) return false;
	    if( !visitDeclarator( parameterDeclaration.getDeclarator(), action ) ) return false;
	    return true;
	}
	
	public static boolean visitDeclSpecifier( IASTDeclSpecifier declSpec, BaseVisitorAction action ){
		if( action.processDeclSpecifiers )
			if( !action.processDeclSpecifier( declSpec ) ) return false;
		
		if( declSpec instanceof ICASTCompositeTypeSpecifier ){
			ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) declSpec;
			if( !visitName( compTypeSpec.getName(), action ) ) return false;
			
			List list = compTypeSpec.getMembers();
			for( int i = 0; i < list.size(); i++ ){
				if( !visitDeclaration( (IASTDeclaration) list.get(i), action ) ) return false;
			}
		} else if( declSpec instanceof ICASTElaboratedTypeSpecifier ){
			if( !visitName( ((ICASTElaboratedTypeSpecifier) declSpec).getName(), action ) ) return false;
		} else if( declSpec instanceof ICASTTypedefNameSpecifier ){
			if( !visitName( ((ICASTTypedefNameSpecifier) declSpec).getName(), action ) ) return false;
		}
		return true;
	}
	public static boolean visitStatement( IASTStatement statement, BaseVisitorAction action ){
		if( action.processStatements )
			if( !action.processStatement( statement ) ) return false;
		
		if( statement instanceof IASTCompoundStatement ){
			List list = ((IASTCompoundStatement) statement).getStatements();
			for( int i = 0; i < list.size(); i++ ){
				if( !visitStatement( (IASTStatement) list.get(i), action ) ) return false;
			}
		} else if( statement instanceof IASTDeclarationStatement ){
			if( !visitDeclaration( ((IASTDeclarationStatement)statement).getDeclaration(), action ) ) return false;
		} else if( statement instanceof IASTExpressionStatement ){
		    if( !visitExpression( ((IASTExpressionStatement)statement).getExpression(), action ) ) return false;
		} else if( statement instanceof IASTCaseStatement ){
		    if( !visitExpression( ((IASTCaseStatement)statement).getExpression(), action ) ) return false;
		} else if( statement instanceof IASTDoStatement ){
		    if( !visitStatement( ((IASTDoStatement)statement).getBody(), action ) ) return false;
		} else if( statement instanceof IASTGotoStatement ){
		    if( !visitName( ((IASTGotoStatement)statement).getName(), action ) ) return false;
		} else if( statement instanceof IASTIfStatement ){
		    if( !visitExpression( ((IASTIfStatement) statement ).getCondition(), action ) ) return false;
		    if( !visitStatement( ((IASTIfStatement) statement ).getThenClause(), action ) ) return false;
		    if( !visitStatement( ((IASTIfStatement) statement ).getElseClause(), action ) ) return false;
		} else if( statement instanceof IASTLabelStatement ){
		    if( !visitName( ((IASTLabelStatement)statement).getName(), action ) ) return false;
		} else if( statement instanceof IASTReturnStatement ){
		    if( !visitExpression( ((IASTReturnStatement) statement ).getReturnValue(), action ) ) return false;
		} else if( statement instanceof IASTSwitchStatement ){
		    if( !visitExpression( ((IASTSwitchStatement) statement ).getController(), action ) ) return false;
		    if( !visitStatement( ((IASTSwitchStatement) statement ).getBody(), action ) ) return false;
		} else if( statement instanceof IASTWhileStatement ){
		    if( !visitExpression( ((IASTWhileStatement) statement ).getCondition(), action ) ) return false;
		    if( !visitStatement( ((IASTWhileStatement) statement ).getBody(), action ) ) return false;
		}
		return true;
	}
	public static boolean visitTypeId( IASTTypeId typeId, BaseVisitorAction action ){
		if( action.processTypeIds )
			if( !action.processTypeId( typeId ) ) return false;
		
		if( !visitDeclarator( typeId.getAbstractDeclarator(), action ) ) return false;
		if( !visitDeclSpecifier( typeId.getDeclSpecifier(), action ) ) return false;
		return true;
	}
	public static boolean visitExpression( IASTExpression expression, BaseVisitorAction action ){
		if( action.processExpressions )
		    if( !action.processExpression( expression ) ) return false;
		
		if( expression instanceof IASTArraySubscriptExpression ){
		    if( !visitExpression( ((IASTArraySubscriptExpression)expression).getArrayExpression(), action ) ) return false;
		    if( !visitExpression( ((IASTArraySubscriptExpression)expression).getSubscriptExpression(), action ) ) return false;
		} else if( expression instanceof IASTBinaryExpression ){
		    if( !visitExpression( ((IASTBinaryExpression)expression).getOperand1(), action ) ) return false;
		    if( !visitExpression( ((IASTBinaryExpression)expression).getOperand2(), action ) ) return false;
		} else if( expression instanceof IASTConditionalExpression){
		    if( !visitExpression( ((IASTConditionalExpression)expression).getLogicalConditionExpression(), action ) ) return false;
		    if( !visitExpression( ((IASTConditionalExpression)expression).getNegativeResultExpression(), action ) ) return false;
		    if( !visitExpression( ((IASTConditionalExpression)expression).getPositiveResultExpression(), action ) ) return false;
		} else if( expression instanceof IASTExpressionList ){
			List list = ((IASTExpressionList)expression).getExpressions();
			for( int i = 0; i < list.size(); i++){
			    if( !visitExpression( (IASTExpression) list.get(i), action ) ) return false;
			}
		} else if( expression instanceof IASTFieldReference ){
		    if( !visitExpression( ((IASTFieldReference)expression).getFieldOwner(), action ) ) return false;
		    if( !visitName( ((IASTFieldReference)expression).getFieldName(), action ) ) return false;
		} else if( expression instanceof IASTFunctionCallExpression ){
		    if( !visitExpression( ((IASTFunctionCallExpression)expression).getFunctionNameExpression(), action ) ) return false;
		    if( !visitExpression( ((IASTFunctionCallExpression)expression).getParameterExpression(), action ) ) return false;
		} else if( expression instanceof IASTIdExpression ){
		    if( !visitName( ((IASTIdExpression)expression).getName(), action ) ) return false;
		} else if( expression instanceof IASTTypeIdExpression ){
		    if( !visitTypeId( ((IASTTypeIdExpression)expression).getTypeId(), action ) ) return false;
		} else if( expression instanceof IASTUnaryExpression ){
		    if( !visitExpression( ((IASTUnaryExpression)expression).getOperand(), action ) ) return false;
		} else if( expression instanceof IASTUnaryTypeIdExpression ){
		    if( !visitExpression( ((IASTUnaryTypeIdExpression)expression).getOperand(), action ) ) return false;
		    if( !visitTypeId( ((IASTUnaryTypeIdExpression)expression).getTypeId(), action ) ) return false;
		} else if( expression instanceof ICASTTypeIdInitializerExpression ){
		    if( !visitTypeId( ((ICASTTypeIdInitializerExpression)expression).getTypeId(), action ) ) return false;
			//TODO: ((ICASTTypeIdInitializerExpression)expression).getInitializer();
		} else if( expression instanceof IGNUASTCompoundStatementExpression ){
		    if( !visitStatement( ((IGNUASTCompoundStatementExpression)expression).getCompoundStatement(), action ) ) return false;
		}
		return true;
	}
	
}
