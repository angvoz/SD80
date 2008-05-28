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
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateTypeUptoPointers;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
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
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArrayType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPEnumeration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespaceAlias;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedef;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;

/**
 * @author aniefer
 */
public class CPPVisitor {
	public static final String SIZE_T = "size_t"; //$NON-NLS-1$
	public static final String PTRDIFF_T = "ptrdiff_t"; //$NON-NLS-1$
	
	/**
	 * @param name
	 */
	public static IBinding createBinding(IASTName name) {
		IASTNode parent = name.getParent();
		IBinding binding = null;
		if (parent instanceof IASTNamedTypeSpecifier ||
			    parent instanceof ICPPASTQualifiedName ||
				parent instanceof ICPPASTBaseSpecifier ||
				parent instanceof ICPPASTConstructorChainInitializer ||
				name.getPropertyInParent() == ICPPASTNamespaceAlias.MAPPING_NAME ||
				parent instanceof ICPPASTTemplateId) {
			binding = CPPSemantics.resolveBinding(name); 
			if (binding instanceof IProblemBinding && parent instanceof ICPPASTQualifiedName && 
					!(parent.getParent() instanceof ICPPASTNamespaceAlias)) {
				final ICPPASTQualifiedName qname = (ICPPASTQualifiedName)parent;
			    final IASTName[] ns = qname.getNames();
			    if (ns[ns.length - 1] != name) 
			    	return binding;
				
				parent = parent.getParent();
			    if (((IProblemBinding)binding).getID() == IProblemBinding.SEMANTIC_MEMBER_DECLARATION_NOT_FOUND) {
					IASTNode node = getContainingBlockItem(name.getParent());
					ASTNodeProperty prop= node.getPropertyInParent();
					if (prop != IASTCompositeTypeSpecifier.MEMBER_DECLARATION &&
							prop != ICPPASTNamespaceDefinition.OWNED_DECLARATION) {
						return binding;
					}
					
				    if (getContainingScope(qname) != getContainingScope(name))
				        return binding;
				}
			} else {
				return binding;
			}
		} 
		if (parent instanceof IASTIdExpression) {
			return resolveBinding(parent);
		} else if (parent instanceof ICPPASTFieldReference) {
			return resolveBinding(parent);
		} else if (parent instanceof ICPPASTCompositeTypeSpecifier) {
			return createBinding((ICPPASTCompositeTypeSpecifier) parent);
		} else if (parent instanceof IASTDeclarator) {
			return createBinding((IASTDeclarator) parent);
		} else if (parent instanceof ICPPASTElaboratedTypeSpecifier) {
			return createBinding((ICPPASTElaboratedTypeSpecifier) parent);
		} else if (parent instanceof IASTDeclaration) {
			return createBinding((IASTDeclaration) parent);
		} else if (parent instanceof IASTEnumerationSpecifier) {
		    return createBinding((IASTEnumerationSpecifier) parent);
		} else if (parent instanceof IASTEnumerator) {
		    return createBinding((IASTEnumerator) parent);
		} else if (parent instanceof IASTGotoStatement) {
		    return createBinding((IASTGotoStatement) parent);
		} else if (parent instanceof IASTLabelStatement) {
		    return createBinding((IASTLabelStatement) parent);
		} else if (parent instanceof ICPPASTTemplateParameter) {
			return CPPTemplates.createBinding((ICPPASTTemplateParameter) parent);
		}
		
		if (name.toCharArray().length > 0)
			return binding;
		return null;
	}
	
	private static IBinding createBinding(IASTGotoStatement gotoStatement) {
	    ICPPFunctionScope functionScope = (ICPPFunctionScope) getContainingScope(gotoStatement.getName());
	    IASTName name = gotoStatement.getName();
	    IBinding binding;
        try {
            binding = functionScope.getBinding(name, false);
            if (binding == null || !(binding instanceof ILabel)) {
    	        binding = new CPPLabel(name);
    	        ASTInternal.addName(functionScope,  name);
    	    }
        } catch (DOMException e) {
            binding = e.getProblem();
        }
        
	    return binding;
	}
	
	private static IBinding createBinding(IASTLabelStatement labelStatement) {
	    ICPPFunctionScope functionScope = (ICPPFunctionScope) getContainingScope(labelStatement.getName());
	    IASTName name = labelStatement.getName();
	    IBinding binding;
        try {
            binding = functionScope.getBinding(name, false);
            if (binding == null || !(binding instanceof ILabel)) {
    	        binding = new CPPLabel(name);
    	        ASTInternal.addName(functionScope,  name);
    	    } else {
    	        ((CPPLabel)binding).setLabelStatement(name);
    	    }
        } catch (DOMException e) {
            binding = e.getProblem();
        }
        
	    return binding;
	}
	
    private static IBinding createBinding(IASTEnumerator enumerator) {
        ICPPScope scope = (ICPPScope) getContainingScope(enumerator);
        IBinding enumtor;
        try {
            enumtor = scope.getBinding(enumerator.getName(), false);
            if (enumtor == null || !(enumtor instanceof IEnumerator)) {
                enumtor = new CPPEnumerator(enumerator.getName());
                ASTInternal.addName(scope,  enumerator.getName());
            }
        } catch (DOMException e) {
            enumtor = e.getProblem();
        }
        
        return enumtor;
    }


    private static IBinding createBinding(IASTEnumerationSpecifier specifier) {
        ICPPScope scope = (ICPPScope) getContainingScope(specifier);
        IBinding enumeration;
        try {
            enumeration = scope.getBinding(specifier.getName(), false);
            if (enumeration == null || !(enumeration instanceof IEnumeration)) {
                enumeration = new CPPEnumeration(specifier.getName());
                ASTInternal.addName(scope,  specifier.getName());
            }
        } catch (DOMException e) {
            enumeration = e.getProblem();
        }
        
        return enumeration;
    }

    private static IBinding createBinding(final ICPPASTElaboratedTypeSpecifier elabType) {
	    final IASTNode parent = elabType.getParent();
	    IBinding binding = null;
	    boolean mustBeSimple = true;
	    boolean isFriend = false;
	    boolean qualified = false;
	    IASTName name = elabType.getName();
	    if (name instanceof ICPPASTQualifiedName) {
	        qualified = true;
	        IASTName[] ns = ((ICPPASTQualifiedName)name).getNames();
	        name = ns[ns.length - 1];
	    }
	    if (parent instanceof IASTSimpleDeclaration) {
	        IASTDeclarator[] dtors = ((IASTSimpleDeclaration)parent).getDeclarators();
	        ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	        isFriend = declSpec.isFriend() && dtors.length == 0;
	        if (dtors.length > 0 || isFriend) {
	        	binding = CPPSemantics.resolveBinding(name);
	        	mustBeSimple = !isFriend;
	        } else {
	        	mustBeSimple = false;
	        }
	    } else if (parent instanceof IASTParameterDeclaration || 
	    		   parent instanceof IASTDeclaration ||
				   parent instanceof IASTTypeId) {
	    	binding = CPPSemantics.resolveBinding(elabType.getName());
	    }
	    
		if (binding != null && 
				(!(binding instanceof IProblemBinding) ||
				((IProblemBinding)binding).getID() != IProblemBinding.SEMANTIC_NAME_NOT_FOUND))	{
			return binding;
    	}
		
		//7.1.5.3-2 ... If name lookup does not find a declaration for the name, the elaborated-type-specifier is ill-formed
		//unless it is of the simple form class-key identifier
	    if (mustBeSimple && elabType.getName() instanceof ICPPASTQualifiedName)
	    	return binding;
	    
		boolean template = false;
		ICPPScope scope = (ICPPScope) getContainingScope(name);
		if (scope instanceof ICPPTemplateScope) {
			ICPPScope parentScope = null;
			try {
				template = true;
				parentScope = (ICPPScope) getParentScope(scope, elabType.getTranslationUnit());
			} catch (DOMException e1) {
			}
			scope = parentScope;
		}
		
		if (mustBeSimple) {
			//3.3.1-5 ... the identifier is declared in the smallest non-class non-function-prototype scope that contains
			//the declaration
			while (scope instanceof ICPPClassScope || scope instanceof ICPPFunctionScope) {
				try {
					scope = (ICPPScope) getParentScope(scope, elabType.getTranslationUnit());
				} catch (DOMException e1) {
				}
			}
		}
		if (scope instanceof ICPPClassScope && isFriend && !qualified) {
	        try {
	            while (scope instanceof ICPPClassScope)
	                scope = (ICPPScope) getParentScope(scope, elabType.getTranslationUnit());
            } catch (DOMException e1) {
		    }
		}
        try {
        	if (scope != null) {
        		binding = scope.getBinding(elabType.getName(), false);
        	}
            if (!(binding instanceof ICPPInternalBinding) || !(binding instanceof ICPPClassType)) {
    			if (elabType.getKind() != IASTElaboratedTypeSpecifier.k_enum) {
					if (template)
	            		binding = new CPPClassTemplate(name);
	            	else
						binding = new CPPClassType(name, binding);
    				ASTInternal.addName(scope,  elabType.getName());
    			}
    		} else {
    			((ICPPInternalBinding)binding).addDeclaration(elabType);
    		}
        } catch (DOMException e) {
            binding = e.getProblem();
        }
        
		return binding;
	}

	private static IBinding createBinding(ICPPASTCompositeTypeSpecifier compType) {
		IASTName name = compType.getName();
		if (name instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName)name).getNames();
			name = ns[ns.length - 1];
		}
		ICPPScope scope = (ICPPScope) getContainingScope(name);
		boolean template = false;
		if (scope instanceof ICPPTemplateScope) {
			template = true;
			ICPPScope parentScope = null;
			try {
				parentScope = (ICPPScope) getParentScope(scope, compType.getTranslationUnit());
			} catch (DOMException e1) {
			}
			scope = parentScope;
		}
		IBinding binding = null;
		if (name instanceof ICPPASTTemplateId) {
			return CPPTemplates.createClassSpecialization(compType);
		} 
        try {
        	if (name.toCharArray().length > 0 && scope != null) //can't lookup anonymous things
        		binding = scope.getBinding(name, false);
            if (!(binding instanceof ICPPInternalBinding) || !(binding instanceof ICPPClassType)) {
            	if (template) {
            		binding = new CPPClassTemplate(name);
            	} else {
            		binding = new CPPClassType(name, binding);
            	}
				if (scope != null) {
					ASTInternal.addName(scope, compType.getName());
				}
    		} else {
    			ICPPInternalBinding internal = (ICPPInternalBinding) binding;
    			if (internal.getDefinition() == null) {
    				internal.addDefinition(compType);
    		    } else {
    				binding = new ProblemBinding(name,
    						IProblemBinding.SEMANTIC_INVALID_REDEFINITION, name.toCharArray());
    			}
    		}
        } catch (DOMException e) {
            binding = e.getProblem();
        }
		return binding;
	}
	private static IBinding createBinding(IASTDeclaration declaration) {
		if (declaration instanceof ICPPASTNamespaceDefinition) {
			ICPPASTNamespaceDefinition namespaceDef = (ICPPASTNamespaceDefinition) declaration;
			ICPPScope scope = (ICPPScope) getContainingScope(namespaceDef);
			IBinding binding;
            try {
                binding = scope.getBinding(namespaceDef.getName(), false);
                if (!(binding instanceof ICPPInternalBinding) || binding instanceof IProblemBinding 
                		|| !(binding instanceof ICPPNamespace)) {
    				binding = new CPPNamespace(namespaceDef);
    				ASTInternal.addName(scope,  namespaceDef.getName());
    			}
            } catch (DOMException e) {
                binding = e.getProblem();
            }
			return binding;
		} else if (declaration instanceof ICPPASTUsingDirective) {
			return CPPSemantics.resolveBinding(((ICPPASTUsingDirective) declaration).getQualifiedName());
		} else if (declaration instanceof ICPPASTNamespaceAlias) {
		    ICPPASTNamespaceAlias alias = (ICPPASTNamespaceAlias) declaration;
		    ICPPScope scope = (ICPPScope) getContainingScope(declaration);
		    IBinding binding;
		    try {
		        binding = scope.getBinding(alias.getAlias(), false);
		        if (!(binding instanceof ICPPInternalBinding)) {
		            IBinding namespace = alias.getMappingName().resolveBinding();
		            if (namespace instanceof IProblemBinding) {
		            	IProblemBinding problem = (IProblemBinding) namespace;
		            	namespace = new CPPNamespace.CPPNamespaceProblem(problem.getASTNode(),
		            			problem.getID(), alias.getMappingName().toCharArray());
		            }
		            if (namespace instanceof ICPPNamespace) { 
		                binding = new CPPNamespaceAlias(alias.getAlias(), (ICPPNamespace) namespace);
		                ASTInternal.addName(scope,  alias.getAlias());
		            } else {
		                binding = new ProblemBinding(alias.getAlias(),
		                		IProblemBinding.SEMANTIC_NAME_NOT_FOUND, alias.getAlias().toCharArray());
		            }
		        }
		    } catch (DOMException e) {
		        binding = e.getProblem();
		    }
			
			return binding;
		}

		return null;
	}
	private static IBinding createBinding(IASTDeclarator declarator) {
		IASTNode parent = declarator.getParent();
		while (parent instanceof IASTDeclarator) {
			parent = parent.getParent();
		}
		
		while (declarator.getNestedDeclarator() != null)
			declarator = declarator.getNestedDeclarator();

		IASTFunctionDeclarator funcDeclarator= null;
		IASTNode tmpNode= declarator;
		do {
			if (tmpNode instanceof IASTFunctionDeclarator) {
				funcDeclarator= (IASTFunctionDeclarator) tmpNode;
				break;
			}
			if (((IASTDeclarator) tmpNode).getPointerOperators().length > 0 ||
					tmpNode.getPropertyInParent() != IASTDeclarator.NESTED_DECLARATOR) {
				break;
			}
			tmpNode= tmpNode.getParent();
		}
		while (tmpNode instanceof IASTDeclarator);
			
		IASTName name= declarator.getName();
		if (name instanceof ICPPASTQualifiedName) {
			name= ((ICPPASTQualifiedName)name).getLastName();
		}
		
		ASTNodeProperty prop = parent.getPropertyInParent();
		if (parent instanceof IASTTypeId) {
		    return CPPSemantics.resolveBinding(name);
		} else if (prop == ICPPASTTemplateSpecialization.OWNED_DECLARATION ||
		         prop == ICPPASTExplicitTemplateInstantiation.OWNED_DECLARATION) {
			return CPPTemplates.createFunctionSpecialization(name);
		} else if (prop == ICPPASTTemplateDeclaration.PARAMETER) {
			return CPPTemplates.createBinding((ICPPASTTemplateParameter) parent);
		}
		
		boolean template = false;
		ICPPScope scope = (ICPPScope) getContainingScope((IASTNode) name);
		if (scope instanceof ICPPTemplateScope) {
			ICPPScope parentScope = null;
			try {
				template = true;
				parentScope = (ICPPScope) getParentScope(scope, name.getTranslationUnit());
			} catch (DOMException e1) {
			}
			scope = parentScope;
		}
		if (parent instanceof IASTSimpleDeclaration && scope instanceof ICPPClassScope) {
		    ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)parent).getDeclSpecifier();
		    if (declSpec.isFriend()) {
		        try {
                    scope = (ICPPScope) getParentScope(scope, name.getTranslationUnit());
                } catch (DOMException e1) {
                }
		    }
		}
		
		IBinding binding;
        try {
            binding = (scope != null) ? scope.getBinding(name, false) : null;
        } catch (DOMException e) {
            binding = null;
        }
        
        IASTSimpleDeclaration simpleDecl = (parent instanceof IASTSimpleDeclaration) ?
        		(IASTSimpleDeclaration)parent : null;
        if (parent instanceof ICPPASTParameterDeclaration) {
			ICPPASTParameterDeclaration param = (ICPPASTParameterDeclaration) parent;
			parent = param.getParent();
			if (parent instanceof IASTStandardFunctionDeclarator) {
				IASTStandardFunctionDeclarator fDtor = (IASTStandardFunctionDeclarator) param.getParent();
				if (hasNestedPointerOperator(fDtor)) 
				    return null;
				IBinding temp = fDtor.getName().resolveBinding();
				if (temp instanceof ICPPInternalFunction) {
					binding = ((ICPPInternalFunction) temp).resolveParameter(param);
				} else if (temp instanceof IProblemBinding) {
				    //problems with the function, still create binding for the parameter
				    binding = new CPPParameter(name);
				} else if (temp instanceof IIndexBinding) {
					binding= new CPPParameter(name);
				}
			} else if (parent instanceof ICPPASTTemplateDeclaration) {
				return CPPTemplates.createBinding(param);
			}
		} else if (simpleDecl != null &&
				simpleDecl.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef) {
		    if (binding instanceof ICPPInternalBinding && binding instanceof ITypedef) {
		        try {
                    IType t1 = ((ITypedef)binding).getType();
                    IType t2 = createType(declarator);
                    if (t1 != null && t2 != null && t1.isSameType(t2)) {
        		        ICPPInternalBinding internal = (ICPPInternalBinding) binding;
                        internal.addDeclaration(name);
                        return binding;
                    }
                } catch (DOMException e1) {
                	return e1.getProblem();
                }
                return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION, name.toCharArray());
		    }
		    // if we don't resolve the target type first, we get a problem binding in case the typedef
		    // redeclares the target type:
		    // typedef struct S S;
		    IType targetType= CPPVisitor.createType(declarator);
		    CPPTypedef td= new CPPTypedef(name);
		    td.setType(targetType);
			binding = td;
		} else if (funcDeclarator != null) {
			if (binding instanceof ICPPInternalBinding && binding instanceof IFunction) {
			    IFunction function = (IFunction) binding;
			    if (CPPSemantics.isSameFunction(function, funcDeclarator)) {
			        ICPPInternalBinding internal = (ICPPInternalBinding) function;
			        if (parent instanceof IASTSimpleDeclaration) {
			            internal.addDeclaration(name);
			        } else if (internal.getDefinition() == null) {
			            internal.addDefinition(name);
			        } else {
		                IASTNode def = internal.getDefinition();
		                if (def instanceof IASTDeclarator)
		                    def = ((IASTDeclarator)def).getName();
		                if (def != name) {
		                    return new ProblemBinding(name,
		                    		IProblemBinding.SEMANTIC_INVALID_REDEFINITION, name.toCharArray());
		                }
		            }
			        
			        return function;
			    }
			} 
			
			if (binding instanceof IIndexBinding) {
				ICPPASTTemplateDeclaration templateDecl = CPPTemplates.getTemplateDeclaration(name);
				if (templateDecl != null) {
					ICPPASTTemplateParameter[] params = templateDecl.getTemplateParameters();
					for (ICPPASTTemplateParameter param : params) {
						IASTName paramName = CPPTemplates.getTemplateParameterName(param);
						paramName.setBinding(null);
						//unsetting the index bindings so that they
						//can be re-resolved with normal bindings
					}
				}
			}
			
			if (scope instanceof ICPPClassScope) {
				if (isConstructor(scope, funcDeclarator)) {
					binding = template ? (ICPPConstructor)  new CPPConstructorTemplate(name)
									   : new CPPConstructor((ICPPASTFunctionDeclarator) funcDeclarator);
				} else {
					binding = template ? (ICPPMethod) new CPPMethodTemplate(name)
							           : new CPPMethod((ICPPASTFunctionDeclarator) funcDeclarator);
				}
			} else {
				binding = template ? (ICPPFunction) new CPPFunctionTemplate(name)
								   : new CPPFunction((ICPPASTFunctionDeclarator) funcDeclarator);
			}
		} else if (parent instanceof IASTSimpleDeclaration) {
    	    IType t1 = null, t2 = null;
		    if (binding != null && binding instanceof IVariable && !(binding instanceof IIndexBinding)) {
		        t1 = createType(declarator);
		        try {
                    t2 = ((IVariable)binding).getType();
                } catch (DOMException e1) {
                }
		    }
		    if (t1 != null && t2 != null) {
		    	if (t1.isSameType(t2)) {
		    		if (binding instanceof ICPPInternalBinding)
		    			((ICPPInternalBinding)binding).addDeclaration(name);
		    	} else {
		    		binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION, declarator.getName().toCharArray());
		    	}
		    } else if (simpleDecl != null && simpleDecl.getParent() instanceof ICPPASTCompositeTypeSpecifier) {
				binding = new CPPField(name); 
		    } else {
		        binding = new CPPVariable(name);
		    }
		} 

		if (scope != null && binding != null) {
            try {
                ASTInternal.addName(scope,  name);
            } catch (DOMException e1) {
            }
		}
		
		return binding;
	}

	private static boolean hasNestedPointerOperator(IASTDeclarator decl) {
		decl= decl.getNestedDeclarator();
		while (decl != null) {
			if (decl.getPointerOperators().length > 0) {
				return true;
			}
			decl= decl.getNestedDeclarator();
		}
		return false;
	}

	public static boolean isConstructor(IScope containingScope, IASTDeclarator declarator) {
	    if (containingScope == null || !(containingScope instanceof ICPPClassScope))
	        return false;
	    
	    ICPPASTCompositeTypeSpecifier clsTypeSpec;
        try {
        	IASTNode node = ASTInternal.getPhysicalNodeOfScope(containingScope);
        	if (node instanceof ICPPASTCompositeTypeSpecifier)
        		clsTypeSpec = (ICPPASTCompositeTypeSpecifier)node;
        	else
        		return false;
        } catch (DOMException e) {
            return false;
        }
        IASTName clsName = clsTypeSpec.getName();
        if (clsName instanceof ICPPASTQualifiedName) {
	        IASTName[] names = ((ICPPASTQualifiedName)clsName).getNames(); 
	        clsName = names[names.length - 1];
	    }
        return isConstructor(clsName, declarator);
	}

	public static boolean isConstructor(IASTName parentName, IASTDeclarator declarator) {
	    if (declarator == null      || !(declarator instanceof IASTFunctionDeclarator))
	        return false;
        
	    IASTName name = declarator.getName();
	    if (name instanceof ICPPASTQualifiedName) {
	        IASTName[] names = ((ICPPASTQualifiedName)name).getNames(); 
	        name = names[names.length - 1];
	    }
	    if (!CharArrayUtils.equals(name.toCharArray(), parentName.toCharArray()))
	        return false;
	    
	    IASTDeclSpecifier declSpec = null;
	    IASTNode parent = declarator.getParent();
	    if (parent instanceof IASTSimpleDeclaration) {
	        declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	    } else if (parent instanceof IASTFunctionDefinition) {
	        declSpec = ((IASTFunctionDefinition)parent).getDeclSpecifier();
	    }
	    if (declSpec != null && declSpec instanceof IASTSimpleDeclSpecifier) {
	        return (((IASTSimpleDeclSpecifier)declSpec).getType() == IASTSimpleDeclSpecifier.t_unspecified); 
	    }
	    
	    return false;
	    
	}
	
	public static IScope getContainingScope(final IASTNode inputNode) {
		if (inputNode == null || inputNode instanceof IASTTranslationUnit)
			return null;
		IASTNode node= inputNode;
		while (node != null) {
		    if (node instanceof IASTName && !(node instanceof ICPPASTQualifiedName)) {
				return getContainingScope((IASTName) node);
			} 
		    if (node instanceof IASTDeclaration) {
				IASTNode parent = node.getParent();
				if (parent instanceof IASTTranslationUnit) {
					return ((IASTTranslationUnit)parent).getScope();
				} else if (parent instanceof IASTDeclarationStatement) {
					return getContainingScope((IASTStatement) parent);
				} else if (parent instanceof IASTForStatement) {
				    return ((IASTForStatement)parent).getScope();
				} else if (parent instanceof IASTCompositeTypeSpecifier) {
				    return ((IASTCompositeTypeSpecifier)parent).getScope();
				} else if (parent instanceof ICPPASTNamespaceDefinition) {
					return ((ICPPASTNamespaceDefinition)parent).getScope();
				} else if (parent instanceof ICPPASTSwitchStatement) {
					return ((ICPPASTSwitchStatement)parent).getScope();
				} else if (parent instanceof ICPPASTIfStatement) {
					return ((ICPPASTIfStatement)parent).getScope();
				} else if (parent instanceof ICPPASTWhileStatement) {
					return ((ICPPASTWhileStatement)parent).getScope();
				} else if (parent instanceof ICPPASTTemplateDeclaration) {
					return ((ICPPASTTemplateDeclaration)parent).getScope();
				}
			} else if (node instanceof IASTStatement) {
		        return getContainingScope((IASTStatement) node); 
			} else if (node instanceof IASTTypeId) {
				if (node.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT) {
				    ICPPASTTemplateDeclaration decl = CPPTemplates.getTemplateDeclaration((IASTName) node.getParent());
			        if (decl == null) {
			            node = node.getParent();
			            while (node instanceof IASTName)
			                node = node.getParent();
			            continue;
			        }
				}
			} else if (node instanceof IASTParameterDeclaration) {
			    IASTNode parent = node.getParent();
			    if (parent instanceof ICPPASTFunctionDeclarator) {
					ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) parent;
					if (dtor.getNestedDeclarator() == null || dtor.getNestedDeclarator().getPointerOperators().length == 0) {
						while (parent.getParent() instanceof IASTDeclarator)
						    parent = parent.getParent();
						ASTNodeProperty prop = parent.getPropertyInParent();
						if (prop == IASTSimpleDeclaration.DECLARATOR)
						    return dtor.getFunctionScope();
						else if (prop == IASTFunctionDefinition.DECLARATOR)
						    return ((IASTCompoundStatement)((IASTFunctionDefinition)parent.getParent()).getBody()).getScope();
					}
			    } else if (parent instanceof ICPPASTTemplateDeclaration) {
			    	return CPPTemplates.getContainingScope(node);
			    }
			} else if (node instanceof IASTInitializerExpression) {
			    IASTNode parent = node.getParent();
			    while (!(parent instanceof IASTDeclarator))
			        parent = parent.getParent();
	    	    IASTDeclarator dtor = (IASTDeclarator) parent;
	    	    IASTName name = dtor.getName();
	    	    if (name instanceof ICPPASTQualifiedName) {
	    	        IASTName[] ns = ((ICPPASTQualifiedName)name).getNames();
	    	        return getContainingScope(ns[ns.length - 1]);
	    	    }
			} else if (node instanceof IASTExpression) {
		    	IASTNode parent = node.getParent();
			    if (parent instanceof IASTForStatement) {
			        return ((IASTForStatement)parent).getScope();
			    } else if (parent instanceof ICPPASTIfStatement) {
			    	return ((ICPPASTIfStatement)parent).getScope();
			    } else if (parent instanceof ICPPASTSwitchStatement) {
			    	return ((ICPPASTSwitchStatement)parent).getScope();
			    } else if (parent instanceof ICPPASTWhileStatement) {
			    	return ((ICPPASTWhileStatement)parent).getScope();
			    } else if (parent instanceof IASTCompoundStatement) {
			        return ((IASTCompoundStatement)parent).getScope();
			    } else if (parent instanceof ICPPASTConstructorChainInitializer) {
			    	IASTNode temp = getContainingBlockItem(parent.getParent());
			    	if (temp instanceof IASTFunctionDefinition) {
			    		IASTCompoundStatement body = (IASTCompoundStatement) ((IASTFunctionDefinition)temp).getBody();
			    		return body.getScope();
			    	}
			    } else if (parent instanceof IASTArrayModifier || parent instanceof IASTInitializer) {
			        IASTNode d = parent.getParent();
			        while (!(d instanceof IASTDeclarator))
			            d = d.getParent();
			        IASTDeclarator dtor = (IASTDeclarator) d;
			        while (dtor.getNestedDeclarator() != null)
			            dtor = dtor.getNestedDeclarator();
			        IASTName name = dtor.getName();
			        if (name instanceof ICPPASTQualifiedName) {
			            IASTName[] ns = ((ICPPASTQualifiedName)name).getNames();
			            return getContainingScope(ns[ns.length - 1]);
			        }
			    }
		    } else if (node instanceof ICPPASTTemplateParameter) {
		    	return CPPTemplates.getContainingScope(node);
		    } else if (node instanceof ICPPASTBaseSpecifier) {
	    	    ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) node.getParent();
	    	    IASTName n = compSpec.getName();
	    	    if (n instanceof ICPPASTQualifiedName) {
	    	        IASTName[] ns = ((ICPPASTQualifiedName)n).getNames();
	    	        n = ns[ns.length - 1];
	    	    }
	    	    
		        return CPPVisitor.getContainingScope(n);
		    }
		    node = node.getParent();
		}
	    return new CPPScope.CPPScopeProblem(inputNode, IProblemBinding.SEMANTIC_BAD_SCOPE, 
	    		inputNode.getRawSignature().toCharArray());
	}
	
	public static IScope getContainingScope(IASTName name) {
		IScope scope= getContainingScopeOrNull(name);
		if (scope == null) {
			return new CPPScope.CPPScopeProblem(name, IProblemBinding.SEMANTIC_BAD_SCOPE,
					name == null ? CharArrayUtils.EMPTY : name.toCharArray());
		}

		return scope;
	}
	
	private static IScope getContainingScopeOrNull(IASTName name) {
		if (name == null) {
			return null;
		}
		IASTNode parent = name.getParent();
		try {
		    if (parent instanceof ICPPASTTemplateId) {
		        name = (IASTName) parent;
		        parent = name.getParent();
		    }
	        ICPPASTTemplateDeclaration tmplDecl = CPPTemplates.getTemplateDeclaration(name);
	        if (tmplDecl != null)
	            return tmplDecl.getScope();
	            
			if (parent instanceof ICPPASTQualifiedName) {
				final ICPPASTQualifiedName qname= (ICPPASTQualifiedName) parent;
				final IASTName[] names = qname.getNames();
				int i = 0;
				for (; i < names.length; i++) {
					if (names[i] == name) break;
				}
				if (i == 0) {
					if (qname.isFullyQualified()) {
						return parent.getTranslationUnit().getScope();
					} 
					for (int j=1; j < names.length; j++) {
						tmplDecl = CPPTemplates.getTemplateDeclaration(names[j]);
						if (tmplDecl != null) {
							return getContainingScope(tmplDecl);
						}
					}
				}
				if (i > 0) {
					IBinding binding = names[i-1].resolveBinding();
					while (binding instanceof ITypedef) {
						IType t = ((ITypedef)binding).getType();
						if (t instanceof IBinding)
							binding = (IBinding) t;
						else break;
					}
					boolean done= true;
					IScope scope= null;
					if (binding instanceof ICPPClassType) {
						scope= ((ICPPClassType)binding).getCompositeScope();
					} else if (binding instanceof ICPPNamespace) {
						scope= ((ICPPNamespace)binding).getNamespaceScope();
					} else if (binding instanceof ICPPUnknownBinding) {
					    scope= ((ICPPUnknownBinding)binding).getUnknownScope();
					} else if (binding instanceof IProblemBinding) {
						if (binding instanceof ICPPScope)
							scope= (IScope) binding;
					} else {
						done= false;
					}
					if (done) {
						if (scope == null) {
							return new CPPScope.CPPScopeProblem(names[i - 1],
									IProblemBinding.SEMANTIC_BAD_SCOPE, names[i-1].toCharArray());
						}
						return scope;
					}
				} 
			} else if (parent instanceof ICPPASTFieldReference) {
				final ICPPASTFieldReference fieldReference = (ICPPASTFieldReference)parent;
				IASTExpression owner = fieldReference.getFieldOwner();
				IType type = getExpressionType(owner);
				if (fieldReference.isPointerDereference()) {
					type= getUltimateTypeUptoPointers(type);
					// bug 205964: as long as the type is a class type, recurse. 
					// Be defensive and allow a max of 10 levels.
					for (int j = 0; j < 10; j++) {
						if (type instanceof ICPPClassType) {
							ICPPFunction op = CPPSemantics.findOperator(fieldReference, (ICPPClassType) type);
							if (op != null) {
								type = op.getType().getReturnType();
							}
						} else {
							break;
						}
					}
				} 
				type =  getUltimateType(type, false);
				if (type instanceof ICPPClassType) {
					return ((ICPPClassType) type).getCompositeScope();
				}
			} else if (parent instanceof IASTGotoStatement || parent instanceof IASTLabelStatement) {
			    while (!(parent instanceof IASTFunctionDefinition)) {
			        parent = parent.getParent();
			    }
			    IASTFunctionDefinition fdef = (IASTFunctionDefinition) parent;
			    return ((ICPPASTFunctionDeclarator)fdef.getDeclarator()).getFunctionScope();
			}
		} catch (DOMException e) {
			IProblemBinding problem = e.getProblem();
			if (problem instanceof ICPPScope)
				return problem;
			return new CPPScope.CPPScopeProblem(problem.getASTNode(), problem.getID(), problem.getNameCharArray()); 
		}
		return getContainingScope(parent);
	}

	public static IScope getContainingScope(IASTStatement statement) {
		IASTNode parent = statement.getParent();
		IScope scope = null;
		if (parent instanceof IASTCompoundStatement) {
		    IASTCompoundStatement compound = (IASTCompoundStatement) parent;
		    scope = compound.getScope();
		} else if (parent instanceof IASTForStatement) {
		    scope = ((IASTForStatement)parent).getScope();
		} else if (parent instanceof ICPPASTSwitchStatement) {
			scope = ((ICPPASTSwitchStatement)parent).getScope();
		} else if (parent instanceof ICPPASTIfStatement) {
			scope = ((ICPPASTIfStatement)parent).getScope();
		} else if (parent instanceof ICPPASTWhileStatement) {
			scope = ((ICPPASTWhileStatement)parent).getScope();
		} else if (parent instanceof IASTStatement) {
			scope = getContainingScope((IASTStatement)parent);
		} else if (parent instanceof IASTFunctionDefinition) {
		    IASTFunctionDeclarator fnDeclarator = ((IASTFunctionDefinition) parent).getDeclarator();
		    IASTName name = fnDeclarator.getName();
		    if (name instanceof ICPPASTQualifiedName) {
		        IASTName[] ns = ((ICPPASTQualifiedName)name).getNames();
		        name = ns[ns.length -1];
		    }
		    return getContainingScope(name);
		}
		
		if (scope == null)
			return getContainingScope(parent);
		return scope;
	}
	
	public static IASTNode getContainingBlockItem(IASTNode node) {
	    if (node == null) return null;
	    if (node.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return null;
		IASTNode parent = node.getParent();
		if (parent == null)
		    return null;
		while (parent != null) {
			if (parent instanceof IASTDeclaration) {
				IASTNode p = parent.getParent();
				if (p instanceof IASTDeclarationStatement)
					return p;
				return parent;
			} else if (parent instanceof IASTExpression) {
				IASTNode p = parent.getParent();
				if (p instanceof IASTForStatement)
				    return parent;
				if (p instanceof IASTStatement)
					return p;
			} else if (parent instanceof IASTStatement || parent instanceof IASTTranslationUnit) {
				return parent;
			} else if (parent instanceof IASTFunctionDeclarator && node.getPropertyInParent() == IASTStandardFunctionDeclarator.FUNCTION_PARAMETER) {
			    return node;
			} else if (parent instanceof IASTEnumerationSpecifier.IASTEnumerator) {
			    return parent;
			}
			node = parent;
			parent = node.getParent();
		}
		return null;
	}
	
	static private IBinding resolveBinding(IASTNode node) {
		IASTName name = null;
		while (node != null) {
			if (node instanceof IASTIdExpression) {
				name = ((IASTIdExpression) node).getName();
				break;
			} else if (node instanceof ICPPASTFieldReference) {
				name = ((ICPPASTFieldReference)node).getFieldName();
				break;
			} else if (node instanceof IASTFunctionCallExpression) {
				node = ((IASTFunctionCallExpression)node).getFunctionNameExpression();
			} else if (node instanceof IASTUnaryExpression) {
				node = ((IASTUnaryExpression)node).getOperand();
			} else if (node instanceof IASTBinaryExpression) {
				node = ((IASTBinaryExpression)node).getOperand2();
			} else {
				node = null;
			}
		}
		if (name != null) {
			if (name instanceof ICPPASTQualifiedName) {
				IASTName ns[] = ((ICPPASTQualifiedName)name).getNames();
				name = ns[ns.length - 1];
			}
			if (name instanceof CPPASTName) {
				((CPPASTName) name).incResolutionDepth();
			}
			else if (name instanceof CPPASTTemplateId) {
				((CPPASTTemplateId) name).incResolutionDepth();
			}
			IBinding binding = name.getBinding();
			if (binding == null) {
				binding = CPPSemantics.resolveBinding(name);
				name.setBinding(binding);
				if (name instanceof ICPPASTTemplateId && binding instanceof ICPPSpecialization) {
					((ICPPASTTemplateId)name).getTemplateName().setBinding(((ICPPSpecialization)binding).getSpecializedBinding());
				}
			}
			return binding;
		}
		return null;
	}
	
	public static class CollectProblemsAction extends CPPASTVisitor {
		{
			shouldVisitDeclarations = true;
			shouldVisitExpressions = true;
			shouldVisitStatements = true;
			shouldVisitTypeIds = true;
		}
		
		private static final int DEFAULT_CHILDREN_LIST_SIZE = 8;
		private IASTProblem[] problems = null;
		int numFound = 0;

		public CollectProblemsAction() {
			problems = new IASTProblem[DEFAULT_CHILDREN_LIST_SIZE];
		}
		
		private void addProblem(IASTProblem problem) {
			if (problems.length == numFound) { // if the found array is full, then double the array
	            IASTProblem[] old = problems;
	            problems = new IASTProblem[old.length * 2];
	            for (int j = 0; j < old.length; ++j)
	                problems[j] = old[j];
	        }
			problems[numFound++] = problem;
		}
		
	    private IASTProblem[] removeNullFromProblems() {
	    	if (problems[problems.length-1] != null) { // if the last element in the list is not null then return the list
				return problems;			
			} else if (problems[0] == null) { // if the first element in the list is null, then return empty list
				return new IASTProblem[0];
			}
			
			IASTProblem[] results = new IASTProblem[numFound];
			for (int i=0; i<results.length; i++)
				results[i] = problems[i];
				
			return results;
	    }
		
		public IASTProblem[] getProblems() {
			return removeNullFromProblems();
		}
	    
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
		 */
		@Override
		public int visit(IASTDeclaration declaration) {
			if (declaration instanceof IASTProblemHolder)
				addProblem(((IASTProblemHolder)declaration).getProblem());

			return PROCESS_CONTINUE;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
		 */
		@Override
		public int visit(IASTExpression expression) {
			if (expression instanceof IASTProblemHolder)
				addProblem(((IASTProblemHolder)expression).getProblem());

			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
		 */
		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTProblemHolder)
				addProblem(((IASTProblemHolder)statement).getProblem());

			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
		 */
		@Override
		public int visit(IASTTypeId typeId) {
			if (typeId instanceof IASTProblemHolder)
				addProblem(((IASTProblemHolder)typeId).getProblem());

			return PROCESS_CONTINUE;
		}
	}
	
	public static class CollectDeclarationsAction extends CPPASTVisitor {
	    private static final int DEFAULT_LIST_SIZE = 8;
		private IASTName[] decls;
		private IBinding[] bindings;
		private int idx = 0;
		private int kind;
		
		private static final int KIND_LABEL  = 1;
		private static final int KIND_OBJ_FN = 2;
		private static final int KIND_TYPE   = 3;
		private static final int KIND_NAMESPACE   = 4;
		private static final int KIND_COMPOSITE = 5;
		private static final int KIND_TEMPLATE_PARAMETER = 6;
		
		
		public CollectDeclarationsAction(IBinding binding) {
			shouldVisitNames = true;
			this.decls = new IASTName[DEFAULT_LIST_SIZE];
			
			this.bindings = new IBinding[] {binding};
			if (binding instanceof ICPPUsingDeclaration) {
				this.bindings= ((ICPPUsingDeclaration) binding).getDelegates();
				kind= KIND_COMPOSITE;
			} else if (binding instanceof ILabel) {
				kind = KIND_LABEL;
			} else if (binding instanceof ICPPTemplateParameter) {
				kind = KIND_TEMPLATE_PARAMETER;
			} else if (binding instanceof ICompositeType || 
					 binding instanceof ITypedef || 
					 binding instanceof IEnumeration) {
				kind = KIND_TYPE;
			} else if (binding instanceof ICPPNamespace) {
				kind = KIND_NAMESPACE;
			} else { 
				kind = KIND_OBJ_FN;
			}
		}
		
		@SuppressWarnings("fallthrough")
		@Override
		public int visit(IASTName name) {
			if (name instanceof ICPPASTQualifiedName) return PROCESS_CONTINUE;
			
			ASTNodeProperty prop = name.getPropertyInParent();
			if (prop == ICPPASTQualifiedName.SEGMENT_NAME)
				prop = name.getParent().getPropertyInParent();
			
			switch(kind) {
				case KIND_TEMPLATE_PARAMETER:
					if (prop == ICPPASTSimpleTypeTemplateParameter.PARAMETER_NAME ||
							prop == ICPPASTTemplatedTypeTemplateParameter.PARAMETER_NAME) {
						break;
					} else if (prop == IASTDeclarator.DECLARATOR_NAME) {
						IASTNode d = name.getParent();
						while (d.getParent() instanceof IASTDeclarator)
							d = d.getParent();
						if (d.getPropertyInParent() == IASTParameterDeclaration.DECLARATOR) {
							break;
						}
					}
					return PROCESS_CONTINUE;

				case KIND_LABEL:
					if (prop == IASTLabelStatement.NAME)
						break;
					return PROCESS_CONTINUE;

				case KIND_TYPE:
				case KIND_COMPOSITE:
				    if (prop == IASTCompositeTypeSpecifier.TYPE_NAME ||
					        prop == IASTEnumerationSpecifier.ENUMERATION_NAME ||
							prop == ICPPASTUsingDeclaration.NAME) {
				        break;
				    } else if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
						IASTNode p = name.getParent().getParent();
						if (p instanceof IASTSimpleDeclaration &&
								((IASTSimpleDeclaration) p).getDeclarators().length == 0) {
							break;
						}
					} else if (prop == IASTDeclarator.DECLARATOR_NAME) {
					    IASTNode p = name.getParent();
					    while (p instanceof IASTDeclarator) {
					    	p= p.getParent();
					    }
					    if (p instanceof IASTSimpleDeclaration) {
					        IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration)p).getDeclSpecifier();
					        if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_typedef)
					            break;
					    }
					}
        
					if (kind == KIND_TYPE)
					    return PROCESS_CONTINUE;
					// fall through
					
				case KIND_OBJ_FN:
					if (prop == IASTDeclarator.DECLARATOR_NAME ||
						    prop == IASTEnumerationSpecifier.IASTEnumerator.ENUMERATOR_NAME ||
							prop == ICPPASTUsingDeclaration.NAME) {
						break;
					}
					return PROCESS_CONTINUE;

				case KIND_NAMESPACE:
					if (prop == ICPPASTNamespaceDefinition.NAMESPACE_NAME ||
							prop == ICPPASTNamespaceAlias.ALIAS_NAME) {
						break;
					}					
					return PROCESS_CONTINUE;
			}
			
			if (bindings != null) {
				if (isDeclarationsBinding(name.resolveBinding())) {
					if (decls.length == idx) {
						IASTName[] temp = new IASTName[decls.length * 2];
						System.arraycopy(decls, 0, temp, 0, decls.length);
						decls = temp;
					}
					decls[idx++] = name;
			    }   
			}
			return PROCESS_CONTINUE;
		}

		private boolean isDeclarationsBinding(IBinding nameBinding) {
			nameBinding= unwindBinding(nameBinding);
			if (nameBinding != null) {
				for (IBinding binding : bindings) {
					if (nameBinding.equals(unwindBinding(binding))) {
						return true;
					}
					// a using declaration is a declaration for the references of its delegates
					if (nameBinding instanceof ICPPUsingDeclaration) {
						if (ArrayUtil.contains(((ICPPUsingDeclaration) nameBinding).getDelegates(), binding)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		public IASTName[] getDeclarations() {
			if (idx < decls.length) {
				IASTName[] temp = new IASTName[idx];
				System.arraycopy(decls, 0, temp, 0, idx);
				decls = temp;
			}
			return decls;
		}

	}

	protected static IBinding unwindBinding(IBinding binding) {
		while (true) {
			if (binding instanceof ICPPSpecialization) {
				binding= ((ICPPSpecialization) binding).getSpecializedBinding();
			} else {
				break;
			}
		}
		return binding;
	}

	public static class CollectReferencesAction extends CPPASTVisitor {
		private static final int DEFAULT_LIST_SIZE = 8;
		private IASTName[] refs;
		private IBinding[] bindings;
		private int idx = 0;
		private int kind;
		
		private static final int KIND_LABEL  = 1;
		private static final int KIND_OBJ_FN = 2;
		private static final int KIND_TYPE   = 3;
		private static final int KIND_NAMESPACE   = 4;
		private static final int KIND_COMPOSITE = 5;
		
		
		public CollectReferencesAction(IBinding binding) {
			shouldVisitNames = true;
			this.refs = new IASTName[DEFAULT_LIST_SIZE];

			binding = unwindBinding(binding);
			this.bindings = new IBinding[] {binding};
			
			if (binding instanceof ICPPUsingDeclaration) {
				this.bindings= ((ICPPUsingDeclaration) binding).getDelegates();
				kind= KIND_COMPOSITE;
			} else if (binding instanceof ILabel) {
				kind = KIND_LABEL;
			} else if (binding instanceof ICompositeType || 
					 binding instanceof ITypedef || 
					 binding instanceof IEnumeration) {
				kind = KIND_TYPE;
			} else if (binding instanceof ICPPNamespace) {
				kind = KIND_NAMESPACE;
			} else if (binding instanceof ICPPTemplateParameter) {
			    kind = KIND_COMPOSITE;
			} else { 
				kind = KIND_OBJ_FN;
			}
		}
		
		@SuppressWarnings("fallthrough")
		@Override
		public int visit(IASTName name) {
			if (name instanceof ICPPASTQualifiedName || name instanceof ICPPASTTemplateId)
				return PROCESS_CONTINUE;
			
			ASTNodeProperty prop = name.getPropertyInParent();
			ASTNodeProperty p2 = null;
			if (prop == ICPPASTQualifiedName.SEGMENT_NAME) {
			    p2 = prop;
				prop = name.getParent().getPropertyInParent();
			}
			
			switch(kind) {
				case KIND_LABEL:
					if (prop == IASTGotoStatement.NAME)
						break;
					return PROCESS_CONTINUE;
				case KIND_TYPE:
				case KIND_COMPOSITE:
					if (prop == IASTNamedTypeSpecifier.NAME || 
							prop == ICPPASTPointerToMember.NAME ||
							prop == ICPPASTTypenameExpression.TYPENAME ||
							prop == ICPPASTUsingDeclaration.NAME ||
							prop == ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.NAME ||
							prop == ICPPASTTemplateId.TEMPLATE_NAME ||
							p2 == ICPPASTQualifiedName.SEGMENT_NAME) {
						break;
					} else if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME)	{
						IASTNode p = name.getParent().getParent();
						if (!(p instanceof IASTSimpleDeclaration) ||
							((IASTSimpleDeclaration)p).getDeclarators().length > 0)
						{
							break;
						}
					}
					if (kind == KIND_TYPE)
					    return PROCESS_CONTINUE;
					// fall through

				case KIND_OBJ_FN:
					if (prop == IASTIdExpression.ID_NAME || 
							prop == IASTFieldReference.FIELD_NAME || 
							prop == ICPPASTUsingDirective.QUALIFIED_NAME ||
							prop == ICPPASTUsingDeclaration.NAME ||
							prop == IASTFunctionCallExpression.FUNCTION_NAME ||
							prop == ICPPASTUsingDeclaration.NAME ||
							prop == IASTNamedTypeSpecifier.NAME ||
							prop == ICPPASTConstructorChainInitializer.MEMBER_ID ||
							prop == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT)	{
						break;
					}
					return PROCESS_CONTINUE;
				case KIND_NAMESPACE:
					if (prop == ICPPASTUsingDirective.QUALIFIED_NAME ||
							prop == ICPPASTNamespaceAlias.MAPPING_NAME ||
							prop == ICPPASTUsingDeclaration.NAME ||
							p2 == ICPPASTQualifiedName.SEGMENT_NAME) {
						break;
					}
					return PROCESS_CONTINUE;
			}
			
			if (bindings != null) {
			    if (isReferenceBinding(name.resolveBinding())) {
			    	if (refs.length == idx) {
			    		IASTName[] temp = new IASTName[refs.length * 2];
			    		System.arraycopy(refs, 0, temp, 0, refs.length);
			    		refs = temp;
			    	}
			    	refs[idx++] = name;
			    }
			}
			return PROCESS_CONTINUE;
		}

		private boolean isReferenceBinding(IBinding nameBinding) {
			nameBinding= unwindBinding(nameBinding);
			if (nameBinding != null) {
				for (IBinding binding : bindings) {
					if (nameBinding.equals(binding)) {
						return true;
					}
				}
				if (nameBinding instanceof ICPPUsingDeclaration) {
					IBinding[] delegates= ((ICPPUsingDeclaration) nameBinding).getDelegates();
					for (IBinding delegate : delegates) {
						if (isReferenceBinding(delegate)) {
							return true;
						}
					}
					return false;
				} else {
					return false;
				}
			}
			return false;
		}
		
		public IASTName[] getReferences() {
			if (idx < refs.length) {
				IASTName[] temp = new IASTName[idx];
				System.arraycopy(refs, 0, temp, 0, idx);
				refs = temp;
			}
			return refs;
		}
	}

	/**
	 * Generate a function type for an implicit function.
	 * NOTE: This does not correctly handle parameters with typedef types.
	 */
	public static IFunctionType createImplicitFunctionType(IType returnType, IParameter[] parameters, IPointerType thisType) {
	    IType[] pTypes = new IType[parameters.length];
	    IType pt = null;
	    
	    for (int i = 0; i < parameters.length; i++) {
	        try {
                pt = parameters[i].getType();
            
                // remove qualifiers
                if (pt instanceof IQualifierType) {
                	pt= ((IQualifierType) pt).getType();
                }

                if (pt instanceof IArrayType) {
                	pt = new CPPPointerType(((IArrayType) pt).getType());
                } else if (pt instanceof IFunctionType) {
                	pt = new CPPPointerType(pt);
                }
            } catch (DOMException e) {
                pt = e.getProblem();
            }
	        
	        pTypes[i] = pt; 
	    }
	    
	    return new CPPFunctionType(returnType, pTypes, thisType);
	}
	
	private static IType createType(IType returnType, ICPPASTFunctionDeclarator fnDtor) {
	    IASTParameterDeclaration[] params = fnDtor.getParameters();
	    IType[] pTypes = new IType[params.length];
	    IType pt = null;
	    
	    for (int i = 0; i < params.length; i++) {
	        IASTDeclSpecifier pDeclSpec = params[i].getDeclSpecifier();
	        IASTDeclarator pDtor = params[i].getDeclarator();
	        pt = createType(pDeclSpec);
	        pt = createType(pt, pDtor);

	        //8.3.5-3 
	        //Any cv-qualifier modifying a parameter type is deleted.
	        //so only create the base type from the declspec and not the qualifiers
	        try {
	        	if (pt instanceof IQualifierType) {
	        		pt= ((IQualifierType) pt).getType();
	        	}
	        	if (pt instanceof CPPPointerType) {
	        		pt= ((CPPPointerType) pt).stripQualifiers();
	        	}
	        	//any parameter of type array of T is adjusted to be pointer to T
	        	if (pt instanceof IArrayType) {
	        		IArrayType at = (IArrayType) pt;
                    pt = new CPPPointerType(at.getType());
	        	}
            } catch (DOMException e) {
                pt = e.getProblem();
            }

	        //any parameter to type function returning T is adjusted to be pointer to function
	        if (pt instanceof IFunctionType) {
	            pt = new CPPPointerType(pt);
	        }
	        
	        pTypes[i] = pt;
	    }
	     
	    IASTName name = fnDtor.getName();
		if (name instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
			name = ns[ns.length - 1];
		}
	    if (name instanceof ICPPASTConversionName) {
	    	returnType = createType(((ICPPASTConversionName) name).getTypeId());
	    } else {
	    	returnType = getPointerTypes(returnType, fnDtor);
	    }
	    
	    IScope scope = fnDtor.getFunctionScope();
	    IType thisType = getThisType(scope);
	    if (thisType instanceof IPointerType) {
			try {
				IType classType = ((IPointerType) thisType).getType();
		    	thisType = new CPPPointerType(classType, fnDtor.isConst(), fnDtor.isVolatile());
			} catch (DOMException e) {
			}
	    } else {
	    	thisType = null;
	    }
	    IType type = new CPPFunctionType(returnType, pTypes, (IPointerType) thisType);
	    IASTDeclarator nested = fnDtor.getNestedDeclarator();
	    if (nested != null) {
	    	return createType(type, nested);
	    }
	    return type;
	}
	
	/**
	 * @param declarator
	 * @return
	 */
	private static IType createType(IType baseType, IASTDeclarator declarator) {
	    if (declarator instanceof ICPPASTFunctionDeclarator)
	        return createType(baseType, (ICPPASTFunctionDeclarator)declarator);
		
		IType type = baseType;
		type = getPointerTypes(type, declarator);
		if (declarator instanceof IASTArrayDeclarator)
		    type = getArrayTypes(type, (IASTArrayDeclarator) declarator);

	    IASTDeclarator nested = declarator.getNestedDeclarator();
	    if (nested != null) {
	    	return createType(type, nested);
	    }
	    return type;
	}

	private static IType getPointerTypes(IType type, IASTDeclarator declarator) {
	    IASTPointerOperator[] ptrOps = declarator.getPointerOperators();
		for (IASTPointerOperator ptrOp : ptrOps) {
		    if (ptrOp instanceof IGPPASTPointerToMember) {
		        type = new GPPPointerToMemberType(type, (IGPPASTPointerToMember) ptrOp);
		    } else if (ptrOp instanceof ICPPASTPointerToMember) {
				type = new CPPPointerToMemberType(type, (ICPPASTPointerToMember) ptrOp);
		    } else if (ptrOp instanceof IGPPASTPointer) {
			    type = new GPPPointerType(type, (IGPPASTPointer) ptrOp);
		    } else if (ptrOp instanceof IASTPointer) {
		        type = new CPPPointerType(type, (IASTPointer) ptrOp);
		    } else if (ptrOp instanceof ICPPASTReferenceOperator) {
		        type = new CPPReferenceType(type);
		    }
		}
		return type;
	}

	private static IType getArrayTypes(IType type, IASTArrayDeclarator declarator) {
	    IASTArrayModifier[] mods = declarator.getArrayModifiers();
	    for (IASTArrayModifier mod : mods) {
	        type = new CPPArrayType(type, mod.getConstantExpression());
	    }
	    return type;
	}
	
	public static IType createType(IASTNode node) {
	    if (node == null)
	        return null;
		if (node instanceof IASTExpression)
			return getExpressionType((IASTExpression) node);
		if (node instanceof IASTTypeId)
			return createType(((IASTTypeId) node).getAbstractDeclarator());
		if (node instanceof IASTParameterDeclaration)
			return createType(((IASTParameterDeclaration)node).getDeclarator());
		return null;
	}

	public static IType createType(IASTDeclarator declarator) {
		IASTDeclSpecifier declSpec = null;
		
		IASTNode node = declarator.getParent();
		while (node instanceof IASTDeclarator) {
			declarator = (IASTDeclarator) node;
			node = node.getParent();
		}
		
		if (node instanceof IASTParameterDeclaration) {
			declSpec = ((IASTParameterDeclaration) node).getDeclSpecifier();
		} else if (node instanceof IASTSimpleDeclaration) {
			declSpec = ((IASTSimpleDeclaration)node).getDeclSpecifier();
		} else if (node instanceof IASTFunctionDefinition) {
			declSpec = ((IASTFunctionDefinition)node).getDeclSpecifier();
		} else if (node instanceof IASTTypeId) {
			declSpec = ((IASTTypeId)node).getDeclSpecifier();
		}
	
		IType type = createType(declSpec);
		type = createType(type, declarator);
		return type;
	}

	public static IType createType(IASTDeclSpecifier declSpec) {
	    IType type = getBaseType(declSpec);
		
		if (type != null && (declSpec.isConst() || declSpec.isVolatile())) {
		    type = new CPPQualifierType(type, declSpec.isConst(), declSpec.isVolatile());
		}
		return type;
	}

	private static IType getBaseType(IASTDeclSpecifier declSpec) {
	    IType type = null;
	    IASTName name = null;
	    if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			name = ((ICPPASTCompositeTypeSpecifier) declSpec).getName();
	    } else if (declSpec instanceof ICPPASTNamedTypeSpecifier) {
	    	name = ((ICPPASTNamedTypeSpecifier)declSpec).getName();
		} else if (declSpec instanceof ICPPASTElaboratedTypeSpecifier) {
			name = ((ICPPASTElaboratedTypeSpecifier)declSpec).getName();
		} else if (declSpec instanceof IASTEnumerationSpecifier) {
			name = ((IASTEnumerationSpecifier)declSpec).getName();
		} else if (declSpec instanceof ICPPASTSimpleDeclSpecifier) {
			ICPPASTSimpleDeclSpecifier spec = (ICPPASTSimpleDeclSpecifier) declSpec;
			int bits = (spec.isLong()     ? ICPPBasicType.IS_LONG  : 0) |
					   (spec.isShort()    ? ICPPBasicType.IS_SHORT : 0) |
					   (spec.isSigned()   ? ICPPBasicType.IS_SIGNED: 0) |
					   (spec.isUnsigned() ? ICPPBasicType.IS_UNSIGNED : 0);
			if (spec instanceof IGPPASTSimpleDeclSpecifier) {
				IGPPASTSimpleDeclSpecifier gspec = (IGPPASTSimpleDeclSpecifier) spec;
				if (gspec.getTypeofExpression() != null) {
					type = getExpressionType(gspec.getTypeofExpression());
				} else {
					bits |= (gspec.isLongLong() ? ICPPBasicType.IS_LONG_LONG : 0);
					type = new GPPBasicType(spec.getType(), bits, getExpressionType(gspec.getTypeofExpression()));
				}
			} else {
			    type = new CPPBasicType(spec.getType(), bits);
			}
		}
		if (name != null) {
			IBinding binding = name.resolveBinding();
			if (binding instanceof ICPPConstructor) {
				try {
					ICPPClassScope scope = (ICPPClassScope) binding.getScope();
					type = scope.getClassType();
					type = new CPPPointerType(type);
				} catch (DOMException e) {
					type = e.getProblem();
				}
			} else if (binding instanceof IType) {
				type = (IType) binding;
			} else if (binding instanceof ICPPTemplateNonTypeParameter) {
				//TODO workaround... is there anything better? 
				try {
					type = ((ICPPTemplateNonTypeParameter) binding).getType();
				} catch (DOMException e) {
					type = e.getProblem();
				}
			} else if (binding instanceof IVariable) {
				//this is to help with the ambiguity between typeid & idexpression in template arguments
				try {
					type = ((IVariable)binding).getType();
				} catch (DOMException e) {
					type = e.getProblem();
				}
			}
		}
		return type;
	}

	public static IType getThisType(IScope scope) {
	    try {
			IASTNode node = null;
			while (scope != null) {
				if (scope instanceof ICPPBlockScope || scope instanceof ICPPFunctionScope) {
					node = ASTInternal.getPhysicalNodeOfScope(scope);
					if (node instanceof IASTFunctionDeclarator)
						break;
					if (node.getParent() instanceof IASTFunctionDefinition)
						break;
				}
				scope = scope.getParent();
			}
			if (node != null) {
				if (node.getParent() instanceof IASTFunctionDefinition) {
					IASTFunctionDefinition def = (IASTFunctionDefinition) node.getParent();
					node = def.getDeclarator();
				}
				if (node instanceof IASTFunctionDeclarator) {
					ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) node;
					IASTName fName = dtor.getName();
					if (fName instanceof ICPPASTQualifiedName) {
					    IASTName[] ns = ((ICPPASTQualifiedName)fName).getNames();
					    fName = ns[ns.length - 1];
					}
					IScope s = getContainingScope(fName);
					if (s instanceof ICPPTemplateScope)
						s = getParentScope(s, fName.getTranslationUnit());
					if (s instanceof ICPPClassScope) {
						ICPPClassScope cScope = (ICPPClassScope) s;
						IType type = cScope.getClassType();
						if (type instanceof ICPPClassTemplate) {
						    type = (IType) CPPTemplates.instantiateWithinClassTemplate((ICPPClassTemplate) type);
						}
						if (dtor.isConst() || dtor.isVolatile())
							type = new CPPQualifierType(type, dtor.isConst(), dtor.isVolatile());
						type = new CPPPointerType(type);
						return type;
					}
				}
			}
		} catch (DOMException e) {
		    return e.getProblem();
		}
		return null;
	}
	
	public static IType getExpressionType(IASTExpression expression) {
		if (expression == null)
			return null;
	    if (expression instanceof IASTIdExpression) {
	        IBinding binding = resolveBinding(expression);
	        try {
				if (binding instanceof IVariable) {
                    return ((IVariable) binding).getType();
				} else if (binding instanceof IEnumerator) {
					return ((IEnumerator) binding).getType();
				} else if (binding instanceof IProblemBinding) {
					return (IType) binding;
				} else if (binding instanceof IFunction) {
					return ((IFunction) binding).getType();
				} else if (binding instanceof ICPPTemplateNonTypeParameter) {
					return ((ICPPTemplateNonTypeParameter) binding).getType();
				} else if (binding instanceof ICPPClassType) {
					return ((ICPPClassType) binding);
				}
			} catch (DOMException e) {
				return e.getProblem();
			}
	    } else if (expression instanceof IASTCastExpression) {
	        IASTTypeId id = ((IASTCastExpression)expression).getTypeId();
	        IType type = createType(id.getDeclSpecifier());
	        return createType(type, id.getAbstractDeclarator());
	    } else if (expression instanceof ICPPASTLiteralExpression) {
	    	ICPPASTLiteralExpression lit= (ICPPASTLiteralExpression) expression;
	    	switch(lit.getKind()) {
	    		case ICPPASTLiteralExpression.lk_this : {
	    			IScope scope = getContainingScope(expression);
	    			return getThisType(scope);
	    		}
	    		case ICPPASTLiteralExpression.lk_true :
	    		case ICPPASTLiteralExpression.lk_false:
	    			return new CPPBasicType(ICPPBasicType.t_bool, 0, expression);
	    		case IASTLiteralExpression.lk_char_constant:
	    			return new CPPBasicType(IBasicType.t_char, 0, expression);
	    		case IASTLiteralExpression.lk_float_constant: 
	    			return classifyTypeOfFloatLiteral(lit);
	    		case IASTLiteralExpression.lk_integer_constant: 
	    			return classifyTypeOfIntLiteral(lit);
	    		case IASTLiteralExpression.lk_string_literal:
	    			IType type = new CPPBasicType(IBasicType.t_char, 0, expression);
	    			type = new CPPQualifierType(type, true, false);
	    			return new CPPPointerType(type);
	    	}
	    	
	    } else if (expression instanceof IASTFunctionCallExpression) {
	        IBinding binding = resolveBinding(expression);
	        if (binding instanceof ICPPConstructor) {
				try {
					IScope scope= binding.getScope();
					if (scope instanceof ICPPTemplateScope && ! (scope instanceof ICPPClassScope)) {
						scope= scope.getParent();
					}
					if (scope instanceof ICPPClassScope) {
						return ((ICPPClassScope) scope).getClassType();
					}
				} catch (DOMException e) {
					return e.getProblem();
				}
				return new ProblemBinding(expression, IProblemBinding.SEMANTIC_BAD_SCOPE,
						binding.getName().toCharArray());
	        } else if (binding instanceof IFunction) {
	            IFunctionType fType;
                try {
                    fType = ((IFunction)binding).getType();
                    if (fType != null)
    	                return fType.getReturnType();
                } catch (DOMException e) {
                    return e.getProblem();
                }
	        } else if (binding instanceof IVariable) {
	        	try {
		        	IType t = ((IVariable)binding).getType();
		        	while (t instanceof ITypedef) {
		        		t = ((ITypedef)t).getType();
		        	}
		        	if (t instanceof IPointerType && ((IPointerType)t).getType() instanceof IFunctionType) {
		        		IFunctionType ftype = (IFunctionType) ((IPointerType)t).getType();
		        		if (ftype != null)
		        			return ftype.getReturnType();
		        	}
					t= getUltimateTypeUptoPointers(t);
					if (t instanceof ICPPClassType) {
						ICPPFunction op = CPPSemantics.findOperator(expression, (ICPPClassType) t);
						if (op != null) {
							return op.getType().getReturnType();
						}
					}
	        	} catch (DOMException e) {
	        		return e.getProblem();
	        	} 
	        } else if (binding instanceof ITypedef) {
	        	try {
					IType type = ((ITypedef)binding).getType();
					while (type instanceof ITypedef)
						type = ((ITypedef)type).getType();
					if (type instanceof IFunctionType) {
						return ((IFunctionType)type).getReturnType();
					}
					return type;
				} catch (DOMException e) {
					return e.getProblem();
				}
	        } else if (binding instanceof IProblemBinding) {
	        	return (IType) binding;
	        }
	    } else if (expression instanceof IASTBinaryExpression) {
	    	final IASTBinaryExpression binary = (IASTBinaryExpression) expression;
	        final int op = binary.getOperator();
	        switch(op) {
	        case IASTBinaryExpression.op_lessEqual:
	        case IASTBinaryExpression.op_lessThan:
	        case IASTBinaryExpression.op_greaterEqual:
	        case IASTBinaryExpression.op_greaterThan:
	        case IASTBinaryExpression.op_logicalAnd:
	        case IASTBinaryExpression.op_logicalOr:
	        case IASTBinaryExpression.op_equals:
	        case IASTBinaryExpression.op_notequals:
	        	CPPBasicType basicType= new CPPBasicType(ICPPBasicType.t_bool, 0);
	        	basicType.setValue(expression);
	        	return basicType;
	        case IASTBinaryExpression.op_plus:
	        	IType t2 = getExpressionType(binary.getOperand2());
	        	if (SemanticUtil.getUltimateTypeViaTypedefs(t2) instanceof IPointerType) {
	        		return t2;
	        	}
	        	break;
	        case IASTBinaryExpression.op_minus:
	        	t2= getExpressionType(binary.getOperand2());
	        	if (SemanticUtil.getUltimateTypeViaTypedefs(t2) instanceof IPointerType) {
	        		IType t1 = getExpressionType(binary.getOperand1());
	        		if (SemanticUtil.getUltimateTypeViaTypedefs(t1) instanceof IPointerType) {
	        			IScope scope = getContainingScope(expression);
	        			try {
	        				IBinding[] bs = scope.find(PTRDIFF_T);
	        				if (bs.length > 0) {
	        					for (IBinding b : bs) {
	        						if (b instanceof IType && CPPSemantics.declaredBefore(b, binary, false)) {
	        							return (IType) b;
	        						}
								}
	        				}
	        			} catch (DOMException e) {
	        			}
	        			basicType= new CPPBasicType(IBasicType.t_int, ICPPBasicType.IS_LONG | ICPPBasicType.IS_UNSIGNED);
	        			basicType.setValue(expression);
	        			return basicType;
	        		}
	        		return t1;
	        	}
	        	break;
	        case ICPPASTBinaryExpression.op_pmarrow:
	        case ICPPASTBinaryExpression.op_pmdot:
	        	IType type = getExpressionType(((IASTBinaryExpression) expression).getOperand2());
	        	if (type instanceof ICPPPointerToMemberType) {
	        		try {
	        			return ((ICPPPointerToMemberType)type).getType();
	        		} catch (DOMException e) {
	        			return e.getProblem();
	        		}
	        	} 
	        	return new ProblemBinding(binary, IProblemBinding.SEMANTIC_INVALID_TYPE, new char[0]); 
	        }
			return getExpressionType(((IASTBinaryExpression) expression).getOperand1());
	    } else if (expression instanceof IASTUnaryExpression) {
			int op = ((IASTUnaryExpression)expression).getOperator();
			if (op == IASTUnaryExpression.op_sizeof) {
				IScope scope = getContainingScope(expression);
				try {
					IBinding[] bs = scope.find(SIZE_T);
					if (bs.length > 0 && bs[0] instanceof IType) {
						return (IType) bs[0];
					}
				} catch (DOMException e) {
				}
				return new CPPBasicType(IBasicType.t_int, ICPPBasicType.IS_LONG | ICPPBasicType.IS_UNSIGNED);
			}
			
			IType type = getExpressionType(((IASTUnaryExpression)expression).getOperand());
			while (type instanceof ITypedef) {
				try {
					type = ((ITypedef) type).getType();
				} catch (DOMException e) {
					break;
				}
			}
			if (op == IASTUnaryExpression.op_star && type instanceof ICPPClassType) {
				try {
					ICPPFunction operator= CPPSemantics.findOperator(expression, (ICPPClassType) type);
					if (operator != null) {
						return operator.getType().getReturnType();
					}
				} catch (DOMException de) {
					return de.getProblem();
				}
			}
			if (op == IASTUnaryExpression.op_star && (type instanceof IPointerType || type instanceof IArrayType)) {
			    try {
					return ((ITypeContainer) type).getType();
				} catch (DOMException e) {
					return e.getProblem();
				}
			} else if (op == IASTUnaryExpression.op_amper) {
				if (type instanceof ICPPReferenceType) {
					try {
						type = ((ICPPReferenceType) type).getType();
					} catch (DOMException e) {
					}
				}
				if (type instanceof ICPPFunctionType) {
					ICPPFunctionType functionType = (ICPPFunctionType) type;
					IPointerType thisType = functionType.getThisType();
					if (thisType != null) {
						IType nestedType;
						try {
							nestedType = thisType.getType();
							while (nestedType instanceof ITypeContainer) {
								nestedType = ((ITypeContainer) nestedType).getType();
							}
						} catch (DOMException e) {
							return e.getProblem();
						}
						return new CPPPointerToMemberType(type, (ICPPClassType) nestedType,
								thisType.isConst(), thisType.isVolatile());
					}
				}
				return new CPPPointerType(type);
			} else if (type instanceof CPPBasicType) {
				((CPPBasicType) type).setValue(expression);
			}
			return type;
	    } else if (expression instanceof ICPPASTFieldReference) {
			IASTName name = ((ICPPASTFieldReference)expression).getFieldName();
			IBinding binding = name.resolveBinding();
			try {
			    if (binding instanceof IVariable)
                    return ((IVariable)binding).getType();
                else if (binding instanceof IFunction)
				    return ((IFunction)binding).getType();
                else if (binding instanceof IEnumerator)
                	return ((IEnumerator)binding).getType();
		    } catch (DOMException e) {
		        return e.getProblem();
            }
		} else if (expression instanceof IASTExpressionList) {
			IASTExpression[] exps = ((IASTExpressionList)expression).getExpressions();
			return getExpressionType(exps[exps.length - 1]);
		} else if (expression instanceof ICPPASTTypeIdExpression) {
		    ICPPASTTypeIdExpression typeidExp = (ICPPASTTypeIdExpression) expression;
			if (typeidExp.getOperator() == IASTTypeIdExpression.op_sizeof) {
				IScope scope = getContainingScope(typeidExp);
				try {
					IBinding[] bs = scope.find(SIZE_T);
					if (bs.length > 0 && bs[0] instanceof IType) {
						return (IType) bs[0];
					}
				} catch (DOMException e) {
				}
				return new CPPBasicType(IBasicType.t_int, ICPPBasicType.IS_LONG | ICPPBasicType.IS_UNSIGNED);
			}
		    return createType(typeidExp.getTypeId());
		} else if (expression instanceof IASTArraySubscriptExpression) {
			IType t = getExpressionType(((IASTArraySubscriptExpression) expression).getArrayExpression());
			try {
				if (t instanceof ICPPReferenceType)
					t = ((ICPPReferenceType)t).getType();
				while (t instanceof ITypedef) {
					t = ((ITypedef)t).getType();
				}
				if (t instanceof ICPPClassType) {
					ICPPFunction op = CPPSemantics.findOperator(expression, (ICPPClassType) t);
					if (op != null) {
						return op.getType().getReturnType();
					}
				}
				if (t instanceof IPointerType)
					return ((IPointerType)t).getType();
				else if (t instanceof IArrayType)
					return ((IArrayType)t).getType();
			} catch (DOMException e) {
			}
		} else if (expression instanceof IGNUASTCompoundStatementExpression) {
			IASTCompoundStatement compound = ((IGNUASTCompoundStatementExpression)expression).getCompoundStatement();
			IASTStatement[] statements = compound.getStatements();
			if (statements.length > 0) {
				IASTStatement st = statements[statements.length - 1];
				if (st instanceof IASTExpressionStatement)
					return getExpressionType(((IASTExpressionStatement)st).getExpression());
			}
		} else if (expression instanceof IASTConditionalExpression) {
			final IASTConditionalExpression conditional = (IASTConditionalExpression) expression;
			IASTExpression positiveExpression = conditional.getPositiveResultExpression();
			if (positiveExpression == null) {
				positiveExpression= conditional.getLogicalConditionExpression();
			}
			IType t2 = getExpressionType(positiveExpression);
			IType t3 = getExpressionType(conditional.getNegativeResultExpression());
			if (t3 instanceof IPointerType || t2 == null)
				return t3;
			return t2;
		} else if (expression instanceof ICPPASTDeleteExpression) {
			return CPPSemantics.VOID_TYPE;
		} else if (expression instanceof ICPPASTTypenameExpression) {
			IBinding binding = ((ICPPASTTypenameExpression)expression).getName().resolveBinding();
			if (binding instanceof IType)
				return (IType) binding;
		} else if (expression instanceof ICPPASTNewExpression) {
			ICPPASTNewExpression newExp = (ICPPASTNewExpression) expression;
			return createType(newExp.getTypeId());
		}
	    return null;
	}
	
	private static IType classifyTypeOfFloatLiteral(final IASTLiteralExpression expr) {
		final String lit= expr.toString();
		final int len= lit.length();
		int kind= IBasicType.t_double;
		int flags= 0;
		if (len > 0) {
			switch(lit.charAt(len-1)) {
			case 'f': case 'F':
				kind= IBasicType.t_float;
				break;
			case 'l': case 'L':
				flags |= ICPPBasicType.IS_LONG;
				break;
			}
		}
		return new CPPBasicType(kind, flags, expr);
	}

	private static IType classifyTypeOfIntLiteral(IASTLiteralExpression expression) {
		int makelong= 0;
		boolean unsigned= false;
	
		final String lit= expression.toString();
		for (int i=lit.length()-1; i >=0; i--) {
			final char c= lit.charAt(i);
			if (!(c > 'f' && c <= 'z') && !(c > 'F' && c <= 'Z')) {
				break;
			}
			switch (lit.charAt(i)) {
			case 'u':
			case 'U':
				unsigned = true;
				break;
			case 'l':
			case 'L':
				makelong++;
				break;
			}
		}

		int flags= 0;
		if (unsigned) {
			flags |= ICPPBasicType.IS_UNSIGNED;
		}
		
		if (makelong > 1) {
			flags |= ICPPBasicType.IS_LONG_LONG;
			GPPBasicType result = new GPPBasicType(IBasicType.t_int, flags, null);
			result.setValue(expression);
			return result;
		} 
		
		if (makelong == 1) {
			flags |= ICPPBasicType.IS_LONG;
		} 
		return new CPPBasicType(IBasicType.t_int, flags, expression);
	}

	public static IASTDeclarator getMostNestedDeclarator(IASTDeclarator dtor) {
	    if (dtor == null) return null;
	    IASTDeclarator nested = null;
	    while ((nested = dtor.getNestedDeclarator()) != null) {
	        dtor = nested;
	    }
	    return dtor;
	}
	
	public static IASTProblem[] getProblems(IASTTranslationUnit tu) {
		CollectProblemsAction action = new CollectProblemsAction();
		tu.accept(action);
		
		return action.getProblems();
	}

	public static IASTName[] getReferences(IASTTranslationUnit tu, IBinding binding) {
		CollectReferencesAction action = new CollectReferencesAction(binding);
		tu.accept(action);
		return action.getReferences();
	}
	
	public static IASTName[] getDeclarations(IASTTranslationUnit tu, IBinding binding) {
	    CollectDeclarationsAction action = new CollectDeclarationsAction(binding);
	    tu.accept(action);
	    
		IASTName[] found = action.getDeclarations();
		if (found.length == 0 && binding instanceof ICPPSpecialization && binding instanceof ICPPInternalBinding) {
			IASTNode node = ((ICPPInternalBinding)binding).getDefinition();
			if (node == null) {
				IASTNode[] nds = ((ICPPInternalBinding)binding).getDeclarations();
				if (nds != null && nds.length > 0)
					node = nds[0]; 
			}
			if (node != null) {
				IASTName name = null;
				if (node instanceof IASTDeclarator)
					name = ((IASTDeclarator)node).getName();
				else if (node instanceof IASTName)
					name = (IASTName) node;
				if (name != null)
					found = new IASTName[] { name };
			}
		}
		
		return found;
	}
	
	/**
	 * Return the qualified name by concatenating component names with the 
	 * Scope resolution operator ::
	 * @param qn the component names
	 * @return the qualified name
	 */
	public static String renderQualifiedName(String[] qn) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < qn.length; i++) {
			result.append(qn[i] + (i + 1 < qn.length ? "::" : ""));  //$NON-NLS-1$//$NON-NLS-2$
		}
		return result.toString();
	}
	
	public static String[] getQualifiedName(IBinding binding) {
		IName[] ns = null;
	    try {
            ICPPScope scope = (ICPPScope) binding.getScope();
            while (scope != null) {
            	if (scope instanceof ICPPTemplateScope)
            		scope = (ICPPScope) scope.getParent();
            	
            	if (scope == null) 
            		break;
            	
            	IName n = scope.getScopeName();
            	if (n == null)
            		break;
                if (scope instanceof ICPPBlockScope || scope instanceof ICPPFunctionScope) 
                    break;
                if (scope instanceof ICPPNamespaceScope && scope.getScopeName().toCharArray().length == 0)
                    break;
            
                ns = (IName[]) ArrayUtil.append(IName.class, ns, n);
                scope = (ICPPScope) scope.getParent();
            }
        } catch (DOMException e) {
        }
        ns = (IName[]) ArrayUtil.trim(IName.class, ns);
        String[] result = new String[ns.length + 1];
        for (int i = ns.length - 1; i >= 0; i--) {
            result[ns.length - i - 1] = ns[i].toString();
        }
        result[ns.length] = binding.getName();
	    return result;
	}
	
	public static char[][] getQualifiedNameCharArray(IBinding binding) {
		IName[] ns = null;
	    try {
            ICPPScope scope = (ICPPScope) binding.getScope();
            while (scope != null) {
            	if (scope instanceof ICPPTemplateScope)
            		scope = (ICPPScope) scope.getParent();
            	
            	if (scope == null) 
            		break;
            	IName n = scope.getScopeName();
            	if (n == null)
            		break;
                if (scope instanceof ICPPBlockScope || scope instanceof ICPPFunctionScope) 
                    break;
                if (scope instanceof ICPPNamespaceScope && scope.getScopeName().toCharArray().length == 0)
                    break;
                
                ns = (IName[]) ArrayUtil.append(IName.class, ns, n);
                scope = (ICPPScope) scope.getParent();
            }
        } catch (DOMException e) {
        }
        ns = (IName[]) ArrayUtil.trim(IName.class, ns);
        char[][] result = new char[ns.length + 1][];
        for (int i = ns.length - 1; i >= 0; i--) {
            result[ns.length - i - 1] = ns[i].toCharArray();
        }
        result[ns.length] = binding.getNameCharArray();
	    return result;
	}

	private static IScope getParentScope(IScope scope, IASTTranslationUnit unit) throws DOMException {
		IScope parentScope= scope.getParent();
		// the index cannot return the translation unit as parent scope
		if (parentScope == null && scope instanceof IIndexScope && unit != null) {
			parentScope= unit.getScope();
		}
		return parentScope;
	}

	public static boolean isExternC(IASTNode node) {
		while (node != null) {
			node= node.getParent();
			if (node instanceof ICPPASTLinkageSpecification) {
				if ("\"C\"".equals(((ICPPASTLinkageSpecification) node).getLiteral())) { //$NON-NLS-1$
					return true;
				}
			}
		}
		return false;
	}
	
	/**
     * [3.10] Lvalues and Rvalues
	 * @param exp
	 * @return whether the specified expression is an rvalue
	 */
	static boolean isRValue(IASTExpression exp) {
		if (exp instanceof IASTUnaryExpression) {
			IASTUnaryExpression ue= (IASTUnaryExpression) exp;
			if (ue.getOperator() == IASTUnaryExpression.op_amper) {
				return true;
			}
		}
		if (exp instanceof IASTLiteralExpression)
			return true;
		if (exp instanceof IASTFunctionCallExpression) {
			try {
				IASTFunctionCallExpression fc= (IASTFunctionCallExpression) exp;
				IASTExpression fne= fc.getFunctionNameExpression();
				if (fne instanceof IASTIdExpression) {
					IASTIdExpression ide= (IASTIdExpression) fne;
					IBinding b= ide.getName().resolveBinding();
					if (b instanceof IFunction) {
						IFunctionType tp= ((IFunction)b).getType();
						return !(tp.getReturnType() instanceof ICPPReferenceType);
					}
				}
			} catch (DOMException de) {
				// fall-through
			}
		}
		return false;
	}
}
