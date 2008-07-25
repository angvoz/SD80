/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation 
 *    Markus Schorn (Wind River Systems)
 *    Bryan Wilkinson (QNX)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
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
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
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
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CVisitor {
	public static class ClearBindingAction extends CASTVisitor {
		{
			shouldVisitNames = true;
		}
		@Override
		public int visit(IASTName name) {
			if (name.getBinding() != null) {
                try {
                    IScope scope = name.resolveBinding().getScope();
                    if (scope != null) 
                    	ASTInternal.removeBinding(scope, name.resolveBinding());
                } catch (DOMException e) {
                }
				name.setBinding(null);
			}
			
			return PROCESS_CONTINUE;
		}
	}
	
	public static class CollectProblemsAction extends CASTVisitor {
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

	public static class CollectDeclarationsAction extends CASTVisitor {
		{
			shouldVisitDeclarators = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitEnumerators = true;
			shouldVisitStatements = true;
		}
		
		private static final int DEFAULT_CHILDREN_LIST_SIZE = 8;
		private IASTName[] declsFound = null;
		int numFound = 0;
		IBinding binding = null;
		boolean compositeTypeDeclared = false;
		
		private void addName(IASTName name) {
			if (declsFound.length == numFound) // if the found array is full, then double the array
	        {
	            IASTName[] old = declsFound;
	            declsFound = new IASTName[old.length * 2];
	            for (int j = 0; j < old.length; ++j)
	                declsFound[j] = old[j];
	        }
			declsFound[numFound++] = name;
		}
		
	    private IASTName[] removeNullFromNames() {
	    	if (declsFound[declsFound.length-1] != null) { // if the last element in the list is not null then return the list
				return declsFound;			
			} else if (declsFound[0] == null) { // if the first element in the list is null, then return empty list
				return new IASTName[0];
			}
			
			IASTName[] results = new IASTName[numFound];
			for (int i=0; i<results.length; i++)
				results[i] = declsFound[i];
				
			return results;
	    }
		
		public IASTName[] getDeclarationNames() {
			return removeNullFromNames();
		}
		
		public CollectDeclarationsAction(IBinding binding) {
			declsFound = new IASTName[DEFAULT_CHILDREN_LIST_SIZE];
			this.binding = binding;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
		 */
		@Override
		public int visit(IASTDeclarator declarator) {
			//GCC allows declarations in expressions, so we have to continue from the 
			//declarator in case there is something in the initializer expression
			if (declarator == null || declarator.getName() == null || declarator.getName().toCharArray().length == 0) return PROCESS_CONTINUE;
			
			//if the binding is something not declared in a declarator, continue
			if (binding instanceof ICompositeType) return PROCESS_CONTINUE;
			if (binding instanceof IEnumeration) return PROCESS_CONTINUE;
			
			IASTNode parent = declarator.getParent();
			while (parent != null && !(parent instanceof IASTDeclaration || parent instanceof IASTParameterDeclaration))
				parent = parent.getParent();

			if (parent instanceof IASTDeclaration) {
				if (parent instanceof IASTFunctionDefinition) {
					if (declarator.getName() != null && declarator.getName().resolveBinding() == binding) {
						addName(declarator.getName());
					}
				} else if (parent instanceof IASTSimpleDeclaration) {
					// prototype parameter with no identifier isn't a declaration of the K&R C parameter 
//					if (binding instanceof CKnRParameter && declarator.getName().toCharArray().length == 0)
//						return PROCESS_CONTINUE;
					
					if ((declarator.getName() != null && declarator.getName().resolveBinding() == binding)) {
						addName(declarator.getName());
					}
				} 
			} else if (parent instanceof IASTParameterDeclaration) {
				if (declarator.getName() != null && declarator.getName().resolveBinding() == binding) {
					addName(declarator.getName());
				}
			}
			
			return PROCESS_CONTINUE;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
		 */
		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			if (compositeTypeDeclared && declSpec instanceof ICASTTypedefNameSpecifier)  
				return PROCESS_CONTINUE;
			
			//if the binding isn't declared in a decl spec, skip it
			if (!(binding instanceof ICompositeType) &&	!(binding instanceof IEnumeration))
				return PROCESS_CONTINUE;
			
			if (binding instanceof ICompositeType && declSpec instanceof IASTCompositeTypeSpecifier) {
			    if (((IASTCompositeTypeSpecifier)declSpec).getName().resolveBinding() == binding) { 
					compositeTypeDeclared = true;
					addName(((IASTCompositeTypeSpecifier)declSpec).getName());
				}
			} else if (binding instanceof IEnumeration && declSpec instanceof IASTEnumerationSpecifier) {
				if (((IASTEnumerationSpecifier)declSpec).getName().resolveBinding() == binding) {
					compositeTypeDeclared = true;
					addName(((IASTEnumerationSpecifier)declSpec).getName());
				}
			} else if (declSpec instanceof IASTElaboratedTypeSpecifier) {
			    if (compositeTypeDeclared) {
			        IASTNode parent = declSpec.getParent();
			        if (!(parent instanceof IASTSimpleDeclaration) || ((IASTSimpleDeclaration)parent).getDeclarators().length > 0) {
			            return PROCESS_CONTINUE;
			        }
			    }
				if (((IASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding() == binding) { 
					compositeTypeDeclared = true;
					addName(((IASTElaboratedTypeSpecifier)declSpec).getName());
				}
			}
			
			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processEnumerator(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
		 */
		@Override
		public int visit(IASTEnumerator enumerator) {
			if (binding instanceof IEnumerator && enumerator.getName().resolveBinding() == binding) {
				addName(enumerator.getName());
			}
			
			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
		 */
		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTLabelStatement && binding instanceof ILabel) {
				if (((IASTLabelStatement)statement).getName().resolveBinding() == binding) 
					addName(((IASTLabelStatement)statement).getName());
				return PROCESS_SKIP;
			}

			return PROCESS_CONTINUE;
		}
	}

	public static class CollectReferencesAction extends CASTVisitor {
		private static final int DEFAULT_LIST_SIZE = 8;
		private IASTName[] refs;
		private IBinding binding;
		private int idx = 0;
		private int kind;
		
		private static final int KIND_LABEL  = 1;
		private static final int KIND_OBJ_FN = 2;
		private static final int KIND_TYPE   = 3;
		
		
		public CollectReferencesAction(IBinding binding) {
			this.binding = binding;
			this.refs = new IASTName[DEFAULT_LIST_SIZE];
			
			shouldVisitNames = true;
			if (binding instanceof ILabel)
				kind = KIND_LABEL;
			else if (binding instanceof ICompositeType || 
					 binding instanceof ITypedef || 
					 binding instanceof IEnumeration)
			{
				kind = KIND_TYPE;
			} else 
				kind = KIND_OBJ_FN;
		}
		
		@Override
		public int visit(IASTName name) {
			ASTNodeProperty prop = name.getPropertyInParent();
			switch(kind) {
				case KIND_LABEL:
					if (prop == IASTGotoStatement.NAME)
						break;
					return PROCESS_CONTINUE;
				case KIND_TYPE:
					if (prop == IASTNamedTypeSpecifier.NAME)
						break;
					else if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
						IASTNode p = name.getParent().getParent();
						if (!(p instanceof IASTSimpleDeclaration) ||
							((IASTSimpleDeclaration)p).getDeclarators().length > 0)
						{
							break;
						}
					}
					return PROCESS_CONTINUE;
				case KIND_OBJ_FN:
					if (prop == IASTIdExpression.ID_NAME || 
						prop == IASTFieldReference.FIELD_NAME || 
						prop == ICASTFieldDesignator.FIELD_NAME)
					{
						break;
					}
					return PROCESS_CONTINUE;
			}
			
			if (CharArrayUtils.equals(name.toCharArray(), binding.getNameCharArray()))
				if (sameBinding(name.resolveBinding(), binding)) {
					if (refs.length == idx) {
						IASTName[] temp = new IASTName[refs.length * 2];
						System.arraycopy(refs, 0, temp, 0, refs.length);
						refs = temp;
					}
					refs[idx++] = name;
				}
			return PROCESS_CONTINUE;
		}
		
		private boolean sameBinding(IBinding binding1, IBinding binding2) {
			if (binding1 == binding2)
				return true;
			if (binding1 != null && binding1.equals(binding2))
				return true;
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
	
	protected static final ASTNodeProperty STRING_LOOKUP_PROPERTY = new ASTNodeProperty("CVisitor.STRING_LOOKUP_PROPERTY - STRING_LOOKUP"); //$NON-NLS-1$
	protected static final ASTNodeProperty STRING_LOOKUP_TAGS_PROPERTY = new ASTNodeProperty("CVisitor.STRING_LOOKUP_TAGS_PROPERTY - STRING_LOOKUP"); //$NON-NLS-1$
	private static final String SIZE_T = "size_t"; //$NON-NLS-1$
	private static final String PTRDIFF_T = "ptrdiff_t"; //$NON-NLS-1$
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$
	public static final char[] EMPTY_CHAR_ARRAY = "".toCharArray(); //$NON-NLS-1$
	//lookup bits
	private static final int COMPLETE 			= 0;		
	private static final int CURRENT_SCOPE 		= 1;
	private static final int TAGS 				= 1 << 1;
	private static final int INCLUDE_BLOCK_ITEM = 1 << 2;
	private static final int PREFIX_LOOKUP       = 1 << 3;
	
	//definition lookup start loc
	protected static final int AT_BEGINNING = 1;
	protected static final int AT_NEXT = 2; 

	static protected void createBinding(IASTName name) {
		IBinding binding = null;
		IASTNode parent = name.getParent();
		
		if (parent instanceof CASTIdExpression) {
			binding = resolveBinding(parent, COMPLETE | INCLUDE_BLOCK_ITEM);
		} else if (parent instanceof ICASTTypedefNameSpecifier) {
			binding = resolveBinding(parent);
		} else if (parent instanceof IASTFieldReference) {
			binding = (IBinding) findBinding((IASTFieldReference) parent, false);
			if (binding == null) {
				binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, name.toCharArray());
			}
		} else if (parent instanceof IASTDeclarator) {
			binding = createBinding((IASTDeclarator) parent, name);
		} else if (parent instanceof ICASTCompositeTypeSpecifier) {
			binding = createBinding((ICASTCompositeTypeSpecifier) parent);
		} else if (parent instanceof ICASTElaboratedTypeSpecifier) {
			binding = createBinding((ICASTElaboratedTypeSpecifier) parent);
		} else if (parent instanceof IASTStatement) {
		    binding = createBinding ((IASTStatement) parent);
		} else if (parent instanceof ICASTEnumerationSpecifier) {
		    binding = createBinding((ICASTEnumerationSpecifier) parent);
		} else if (parent instanceof IASTEnumerator) {
		    binding = createBinding((IASTEnumerator) parent);
		} else if (parent instanceof ICASTFieldDesignator) {
			binding = resolveBinding(parent);
		}
		name.setBinding(binding);
	}

	private static IBinding createBinding(ICASTEnumerationSpecifier enumeration) {
	    IASTName name = enumeration.getName();
	    IScope scope =  getContainingScope(enumeration);
	    IBinding binding= null;
	    if (scope != null) {
	    	try {
	    		binding = scope.getBinding(name, false);
	    	} catch (DOMException e) {
	    	}
	    }
        if (binding != null && !(binding instanceof IIndexBinding)) {
        	if (binding instanceof IEnumeration) {
            	if (binding instanceof CEnumeration) {
            	    ((CEnumeration)binding).addDefinition(name);
            	}
        	} else {
        		return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_OVERLOAD, name.toCharArray());	
        	}
	    } else {
	        binding = new CEnumeration(name);
	        try {
	        	ASTInternal.addName(scope, name);
            } catch (DOMException e1) {
            }
	    } 
        return binding; 
	}
	private static IBinding createBinding(IASTEnumerator enumerator) {
	    IEnumerator binding = new CEnumerator(enumerator); 
	    try {
	    	ASTInternal.addName(binding.getScope(), enumerator.getName());
        } catch (DOMException e) {
        }
	    return binding;
	}
	private static IBinding createBinding(IASTStatement statement) {
	    if (statement instanceof IASTGotoStatement) {
	        char[] gotoName = ((IASTGotoStatement)statement).getName().toCharArray();
	        IScope scope = getContainingScope(statement);
	        if (scope != null && scope instanceof ICFunctionScope) {
	            CFunctionScope functionScope = (CFunctionScope) scope;
	            ILabel[] labels = functionScope.getLabels();
	            for (ILabel label : labels) {
	                if (CharArrayUtils.equals(label.getNameCharArray(), gotoName)) {
	                    return label;
	                }
	            }
	            //label not found
	            return new CLabel.CLabelProblem(((IASTGotoStatement)statement).getName(), IProblemBinding.SEMANTIC_LABEL_STATEMENT_NOT_FOUND, gotoName);
	        }
	    } else if (statement instanceof IASTLabelStatement) {
	        IASTName name = ((IASTLabelStatement)statement).getName();
	        IBinding binding = new CLabel(name);
	        try {
	        	IScope scope = binding.getScope();
	        	if (scope instanceof ICFunctionScope)
	        		ASTInternal.addName(binding.getScope(), name);
            } catch (DOMException e) {
            }
	        return binding;
	    }
	    return null;
	}
	private static IBinding createBinding(ICASTElaboratedTypeSpecifier elabTypeSpec) {
		IASTNode parent = elabTypeSpec.getParent();
		if (parent instanceof IASTDeclaration) {
			int bits = TAGS;
			if (parent instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration)parent).getDeclarators().length == 0) {
				bits |= CURRENT_SCOPE;
			}
			IASTName name = elabTypeSpec.getName();
			IBinding binding = resolveBinding(elabTypeSpec, bits);
			if (binding != null) {
				if (binding instanceof CEnumeration) {
			        ((CEnumeration)binding).addDeclaration(name);
			    }
			} else {
				if (elabTypeSpec.getKind() == IASTElaboratedTypeSpecifier.k_enum) {
			        binding = new CEnumeration(name);
			    } else {
			        binding = new CStructure(name);    
			    }
				
				try {
					ASTInternal.addName(binding.getScope(), name);
                } catch (DOMException e) {
                }
			}
			
			return binding;
		} else if (parent instanceof IASTTypeId || parent instanceof IASTParameterDeclaration) {
			IASTNode blockItem = getContainingBlockItem(parent);
			try {
                return (IBinding) findBinding(blockItem, elabTypeSpec.getName(), COMPLETE | TAGS);
            } catch (DOMException e) {
                return null;
            }
		}
		return null;
	}
	
	/**
	 * if prefix == false, return an IBinding or null
	 * if prefix == true, return an IBinding[] or null
	 * @param fieldReference
	 * @param prefix
	 * @return
	 */
	private static Object findBinding(IASTFieldReference fieldReference, boolean prefix) {
		IASTExpression fieldOwner = fieldReference.getFieldOwner();
		IType type = null;
		if (fieldOwner instanceof IASTArraySubscriptExpression) {
		    type = getExpressionType(((IASTArraySubscriptExpression) fieldOwner).getArrayExpression());
		} else {
		    type = getExpressionType(fieldOwner);
		}
	    while (type != null && type instanceof ITypeContainer) {
    		try {
                type = ((ITypeContainer)type).getType();
            } catch (DOMException e) {
                return e.getProblem();
            }
	    }
		
		if (type != null && type instanceof ICompositeType) {
		    if (prefix) {
		        IBinding[] result = null;
		        try {
		            char[] p = fieldReference.getFieldName().toCharArray();
                    IField[] fields = ((ICompositeType) type).getFields();
                    for (IField field : fields) {
                        if (CharArrayUtils.equals(field.getNameCharArray(), 0, p.length, p, true)) {
                            result = (IBinding[]) ArrayUtil.append(IBinding.class, result, field);
                        }
                    }
                    return ArrayUtil.trim(IBinding.class, result);
                } catch (DOMException e) {
                    return new IBinding[] { e.getProblem() };
                }
		    } 
			try {
                return ((ICompositeType) type).findField(fieldReference.getFieldName().toString());
            } catch (DOMException e) {
                return e.getProblem();
            }
		}
		return null;
	}
	
	public static IType getExpressionType(IASTExpression expression) {
	    try{ 
		    if (expression instanceof IASTIdExpression) {
		        IBinding binding = ((IASTIdExpression)expression).getName().resolveBinding();
				if (binding instanceof IVariable) {
					return ((IVariable)binding).getType();
				}
				else if (binding instanceof IFunction) {
					return ((IFunction)binding).getType();
				}
		    } else if (expression instanceof IASTCastExpression) {
		        IASTTypeId id = ((IASTCastExpression)expression).getTypeId();
		        return createType(id.getAbstractDeclarator());
		    } else if (expression instanceof IASTFieldReference) { 
		        IBinding binding = ((IASTFieldReference)expression).getFieldName().resolveBinding();
				if (binding instanceof IVariable) {
					return ((IVariable)binding).getType();
				}
		    } else if (expression instanceof IASTFunctionCallExpression) {
		        IType type = getExpressionType(((IASTFunctionCallExpression)expression).getFunctionNameExpression());
		        while (type instanceof ITypeContainer)
	                type = ((ITypeContainer)type).getType();
	            if (type instanceof IFunctionType)
	                return ((IFunctionType)type).getReturnType();
		    } else if (expression instanceof IASTUnaryExpression) {
		        IType type = getExpressionType(((IASTUnaryExpression)expression).getOperand());
		        int op = ((IASTUnaryExpression)expression).getOperator(); 
		        if (op == IASTUnaryExpression.op_star && (type instanceof IPointerType || type instanceof IArrayType)) {
		            return ((ITypeContainer)type).getType();
		        } else if (op == IASTUnaryExpression.op_amper) {
		            return new CPointerType(type, 0);
		        }
		        return type;
		    } else if (expression instanceof IASTLiteralExpression) {
		    	switch(((IASTLiteralExpression) expression).getKind()) {
		    		case IASTLiteralExpression.lk_char_constant:
		    			return new CBasicType(IBasicType.t_char, 0, expression);
		    		case IASTLiteralExpression.lk_float_constant:
		    			return new CBasicType(IBasicType.t_float, 0, expression);
		    		case IASTLiteralExpression.lk_integer_constant:
		    			return new CBasicType(IBasicType.t_int, 0, expression);
		    		case IASTLiteralExpression.lk_string_literal:
		    			IType type = new CBasicType(IBasicType.t_char, 0, expression);
		    			type = new CQualifierType(type, true, false, false);
		    			return new CPointerType(type, 0);
		    	}
	    	} else if (expression instanceof IASTBinaryExpression) {
		        IASTBinaryExpression binary = (IASTBinaryExpression) expression;
		        int op = binary.getOperator();
				switch(op) {
					case IASTBinaryExpression.op_lessEqual:
					case IASTBinaryExpression.op_lessThan:
					case IASTBinaryExpression.op_greaterEqual:
					case IASTBinaryExpression.op_greaterThan:
					case IASTBinaryExpression.op_logicalAnd:
					case IASTBinaryExpression.op_logicalOr:
					case IASTBinaryExpression.op_equals:
					case IASTBinaryExpression.op_notequals:
						CBasicType basicType = new CBasicType(IBasicType.t_int, 0);
			        	basicType.setValue(expression);
			        	return basicType;
					case IASTBinaryExpression.op_plus:
						IType t2 = getExpressionType(binary.getOperand2());
						if (unwrapTypedefs(t2) instanceof IPointerType) {
							return t2;
						}
						break;

					case IASTBinaryExpression.op_minus:
						t2= getExpressionType(binary.getOperand2());
						if (unwrapTypedefs(t2) instanceof IPointerType) {
							IType t1 = getExpressionType(binary.getOperand1());
							if (unwrapTypedefs(t1) instanceof IPointerType) {
			        			IScope scope = getContainingScope(expression);
			        			try {
			        				IBinding[] bs = scope.find(PTRDIFF_T);
			        				if (bs.length > 0) {
			        					for (IBinding b : bs) {
			        						if (b instanceof IType) {
			        							if (b instanceof ICInternalBinding == false || 
			        									CVisitor.declaredBefore(((ICInternalBinding) b).getPhysicalNode(), binary)) {
			        								return (IType) b;
			        							}
			        						}
			        					}
			        				}
			        			} catch (DOMException e) {
			        			}

								basicType = new CBasicType(IBasicType.t_int, CBasicType.IS_UNSIGNED | CBasicType.IS_LONG);
					        	basicType.setValue(expression);
					        	return basicType;
							}
							return t1;
						}
						break;
				}
				return getExpressionType(binary.getOperand1());
		    } else if (expression instanceof IASTUnaryExpression) {
				int op = ((IASTUnaryExpression)expression).getOperator(); 
				if (op == IASTUnaryExpression.op_sizeof) {
					IScope scope = getContainingScope(expression);
					if (scope != null) {
						IBinding[] bs = scope.find(SIZE_T);
						if (bs.length > 0 && bs[0] instanceof IType) {
							return (IType) bs[0];
						}
					}
					return new CBasicType(IBasicType.t_int, CBasicType.IS_LONG | CBasicType.IS_UNSIGNED, expression);	
				}
				IType type = getExpressionType(((IASTUnaryExpression)expression).getOperand());
				
				if (op == IASTUnaryExpression.op_star && (type instanceof IPointerType || type instanceof IArrayType)) {
				    try {
						return ((ITypeContainer)type).getType();
					} catch (DOMException e) {
						return e.getProblem();
					}
				} else if (op == IASTUnaryExpression.op_amper) {
				    return new CPointerType(type, 0);
				} else if (type instanceof CBasicType) {
					((CBasicType)type).setValue(expression);
				}
				return type;
		    }  else if (expression instanceof IASTFieldReference) {
				IBinding binding = (IBinding) findBinding((IASTFieldReference) expression, false);
			    if (binding instanceof IVariable)
                    return ((IVariable)binding).getType();
                else if (binding instanceof IFunction)
				    return ((IFunction)binding).getType();
                else if (binding instanceof IEnumerator)
                	return ((IEnumerator)binding).getType();
			} else if (expression instanceof IASTExpressionList) {
				IASTExpression[] exps = ((IASTExpressionList)expression).getExpressions();
				return getExpressionType(exps[exps.length - 1]);
			} else if (expression instanceof IASTTypeIdExpression) {
			    IASTTypeIdExpression typeidExp = (IASTTypeIdExpression) expression;
				if (typeidExp.getOperator() == IASTTypeIdExpression.op_sizeof) {
					IScope scope = getContainingScope(typeidExp);
					IBinding[] bs = scope.find(SIZE_T);
					if (bs.length > 0 && bs[0] instanceof IType) {
						return (IType) bs[0];
					}
					return new CBasicType(IBasicType.t_int, CBasicType.IS_LONG | CBasicType.IS_UNSIGNED);
				}
			    return createType(typeidExp.getTypeId().getAbstractDeclarator());
			} else if (expression instanceof IASTArraySubscriptExpression) {
				IType t = getExpressionType(((IASTArraySubscriptExpression) expression).getArrayExpression());
				if (t instanceof IPointerType)
					return ((IPointerType)t).getType();
				else if (t instanceof IArrayType)
					return ((IArrayType)t).getType();
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
			}
	    } catch(DOMException e) {
	        return e.getProblem();
	    }
	    return null;
	}

	private static IType unwrapTypedefs(IType type) throws DOMException {
		while (type instanceof ITypedef) {
			type= ((ITypedef) type).getType();
		}
		return type;
	}

	/**
	 * @param parent
	 * @return
	 */
	private static IBinding createBinding(IASTDeclarator declarator, IASTName name) {
		IBinding binding = null;
		if (declarator instanceof ICASTKnRFunctionDeclarator) {
		    IASTNode parent = declarator.getParent();
			if (CharArrayUtils.equals(declarator.getName().toCharArray(), name.toCharArray())) {
				binding = resolveBinding(parent, CURRENT_SCOPE);
				if (binding != null && binding instanceof IIndexBinding == false) {
				    if (binding instanceof ICInternalFunction)
				        ((ICInternalFunction)binding).addDeclarator((ICASTKnRFunctionDeclarator) declarator);
				    else 
				        binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_OVERLOAD, name.toCharArray());
				} else { 
					binding = createBinding(declarator);
				}
			} else { // createBinding for one of the ICASTKnRFunctionDeclarator's parameterNames
			    IBinding f = declarator.getName().resolveBinding();
			    if (f instanceof CFunction) {
			        binding = ((CFunction) f).resolveParameter(name);
			    }

				if (declarator.getParent() instanceof IASTFunctionDefinition) {
					IScope scope =  ((IASTCompoundStatement)((IASTFunctionDefinition)declarator.getParent()).getBody()).getScope();
					if (scope != null && binding != null)
                        try {
                            ASTInternal.addName(scope, name);
                        } catch (DOMException e) {
                        }
				}
			}
		} else {
		    binding = createBinding(declarator);
		}
		return binding;
	}

	private static IBinding createBinding(IASTDeclarator declarator) {
		IASTNode parent = declarator.getParent();

		while (parent instanceof IASTDeclarator) {
			parent = parent.getParent();
		}

		while (declarator.getNestedDeclarator() != null)
			declarator = declarator.getNestedDeclarator();
		
		IASTFunctionDeclarator funcDeclarator= null;
		IASTNode node= declarator;
		do {
			if (node instanceof IASTFunctionDeclarator) {
				funcDeclarator= (IASTFunctionDeclarator) node;
				break;
			}
			if (((IASTDeclarator) node).getPointerOperators().length > 0 ||
					node.getPropertyInParent() != IASTDeclarator.NESTED_DECLARATOR) {
				break;
			}
			node= node.getParent();
		}
		while (node instanceof IASTDeclarator)
			;
			
		IScope scope =  getContainingScope(parent);
		
		ASTNodeProperty prop = parent.getPropertyInParent();
		if (prop == IASTDeclarationStatement.DECLARATION) {
		    //implicit scope, see 6.8.4-3
		    prop = parent.getParent().getPropertyInParent();
		    if (prop != IASTCompoundStatement.NESTED_STATEMENT)
		    	scope = null;
		}
		
		IASTName name = declarator.getName();
		
		IBinding binding = null;
		try {
            binding = (scope != null) ? scope.getBinding(name, false) : null;
        } catch (DOMException e1) {
        }  
		
        if (parent instanceof IASTParameterDeclaration || parent.getPropertyInParent() == ICASTKnRFunctionDeclarator.FUNCTION_PARAMETER) {
        	IASTDeclarator fdtor = (IASTDeclarator) parent.getParent();
        	IASTDeclarator nested= fdtor.getNestedDeclarator();
        	while (nested != null && nested.getPointerOperators().length == 0) {
        		fdtor= nested;
        		nested= nested.getNestedDeclarator();
        	}
		    IBinding temp = fdtor.getName().resolveBinding();
		    if (temp != null && temp instanceof CFunction) {
		        binding = ((CFunction) temp).resolveParameter(name);
		    } else if (temp instanceof IFunction) {
		    	 //problems with the function, still create binding for the parameter
			    binding = new CParameter(name);
		    }
		    try {
				if (scope != null && ASTInternal.getPhysicalNodeOfScope(scope) instanceof IASTTranslationUnit) {
					return binding;
				}
			} catch (DOMException e) {
			}
		} else if (funcDeclarator != null) {
			if (binding != null && !(binding instanceof IIndexBinding)) {
			    if (binding instanceof IFunction) {
			        IFunction function = (IFunction) binding;
			        if (function instanceof CFunction) {
			        	((CFunction)function).addDeclarator(funcDeclarator);
			        }
			        return function;
			    }
		        binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_OVERLOAD, name.toCharArray());
			} else if (parent instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration) parent).getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef)
				binding = new CTypedef(name);
			else
				binding = new CFunction(funcDeclarator);
		} else if (parent instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) parent;			
			if (simpleDecl.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef) {
				binding = new CTypedef(name);
			} else {
			    IType t1 = null, t2 = null;
			    if (binding != null && !(binding instanceof IIndexBinding)) {
			        if (binding instanceof IParameter) {
			            return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION, name.toCharArray());
			        } else if (binding instanceof IVariable) {
				        t1 = createType(declarator);
				        try {
	                        t2 = ((IVariable)binding).getType();
	                    } catch (DOMException e1) {
	                    }
	                    if (t1 != null && t2 != null && t1.isSameType(t2)) {
	    			        if (binding instanceof CVariable)
	    			            ((CVariable)binding).addDeclaration(name);
	    			    } else {
	    			        return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION, name.toCharArray());
	    			    }
			    	}
			    } else if (simpleDecl.getParent() instanceof ICASTCompositeTypeSpecifier) {
					binding = new CField(name);
				} else {
					binding = new CVariable(name);
				}
			}
		}

		if (scope != null && binding != null)
            try {
                ASTInternal.addName(scope,  name);
            } catch (DOMException e) {
            }
		return binding;
	}

	
	private static IBinding createBinding(ICASTCompositeTypeSpecifier compositeTypeSpec) {
		IScope scope = null;
		IBinding binding = null;
		IASTName name = compositeTypeSpec.getName();
		try {
			scope =  getContainingScope(compositeTypeSpec);
			while (scope instanceof ICCompositeTypeScope)
				scope =  scope.getParent();
				
			if (scope != null) {
				binding = scope.getBinding(name, false);
				if (binding != null && !(binding instanceof IIndexBinding)) {
					if (binding instanceof CStructure)
						((CStructure)binding).addDefinition(compositeTypeSpec);
					return binding;
				}
			}
		} catch (DOMException e2) {
		}
		
	    binding = new CStructure(name);
	    
        try {
            scope= binding.getScope();
            ASTInternal.addName(scope, name);
        } catch (DOMException e) {
        }
        
		return binding;
	}
	
	protected static IBinding resolveBinding(IASTNode node) {
		return resolveBinding(node, COMPLETE);
	}

	protected static IBinding resolveBinding(IASTNode node, int bits) {
		if (node instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) node;
			IASTFunctionDeclarator functionDeclartor = functionDef.getDeclarator();
			IASTName name = findInnermostDeclarator(functionDeclartor).getName();
			IASTNode blockItem = getContainingBlockItem(node);
			try {
                return (IBinding) findBinding(blockItem, name, bits);
            } catch (DOMException e) {
                return null;
            }
		} else if (node instanceof IASTIdExpression) {
			IASTNode blockItem = getContainingBlockItem(node);
			try {
				IBinding binding = (IBinding) findBinding(blockItem, ((IASTIdExpression)node).getName(), bits);
				if (binding instanceof IType && !(binding instanceof IProblemBinding) ) {
					return new ProblemBinding(node, IProblemBinding.SEMANTIC_INVALID_TYPE, binding.getNameCharArray());
				}
                return binding; 
            } catch (DOMException e) {
                return null;
            }
		} else if (node instanceof ICASTTypedefNameSpecifier) {
			IASTNode blockItem = getContainingBlockItem(node);
			try {
				IASTName name= ((ICASTTypedefNameSpecifier)node).getName();
				IBinding binding = (IBinding) findBinding(blockItem, name, bits);
                if (binding == null)
                	return new ProblemBinding(node, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, name.toCharArray());
				if (binding instanceof IType)
					return binding;
				return new ProblemBinding(node, IProblemBinding.SEMANTIC_INVALID_TYPE, binding.getNameCharArray());
            } catch (DOMException e) {
                return null;
            }
		} else if (node instanceof ICASTElaboratedTypeSpecifier) {
			IASTNode blockItem = getContainingBlockItem(node);
			try {
                return (IBinding) findBinding(blockItem, ((ICASTElaboratedTypeSpecifier)node).getName(), bits);
            } catch (DOMException e) {
                return null;
            }
		} else if (node instanceof ICASTCompositeTypeSpecifier) {
			IASTNode blockItem = getContainingBlockItem(node);
			try {
                return (IBinding) findBinding(blockItem, ((ICASTCompositeTypeSpecifier)node).getName(), bits);
            } catch (DOMException e) {
                return null;
            }
		} else if (node instanceof IASTTypeId) {
			IASTTypeId typeId = (IASTTypeId) node;
			IASTDeclSpecifier declSpec = typeId.getDeclSpecifier();
			IASTName name = null;
			if (declSpec instanceof ICASTElaboratedTypeSpecifier) {
				name = ((ICASTElaboratedTypeSpecifier)declSpec).getName();
			} else if (declSpec instanceof ICASTCompositeTypeSpecifier) {
				name = ((ICASTCompositeTypeSpecifier)declSpec).getName();
			} else if (declSpec instanceof ICASTTypedefNameSpecifier) {
				name = ((ICASTTypedefNameSpecifier)declSpec).getName();
			}
			if (name != null) {
				IBinding binding = name.resolveBinding();
				if (binding instanceof IType)
					return binding;
                else if (binding != null)
					return new ProblemBinding(node, IProblemBinding.SEMANTIC_INVALID_TYPE, binding.getNameCharArray());
				return null;
			}
		} else if (node instanceof ICASTFieldDesignator) {
			IASTNode blockItem = getContainingBlockItem(node);
			
			if ((blockItem instanceof IASTSimpleDeclaration ||
					(blockItem instanceof IASTDeclarationStatement && ((IASTDeclarationStatement)blockItem).getDeclaration() instanceof IASTSimpleDeclaration))) {
				
				IASTSimpleDeclaration simpleDecl = null;
				if (blockItem instanceof IASTDeclarationStatement &&
					((IASTDeclarationStatement)blockItem).getDeclaration() instanceof IASTSimpleDeclaration)
					simpleDecl = (IASTSimpleDeclaration)((IASTDeclarationStatement)blockItem).getDeclaration();
				else if (blockItem instanceof IASTSimpleDeclaration)
					simpleDecl = (IASTSimpleDeclaration)blockItem;
		
				if (simpleDecl != null) {
					IBinding struct = null;
					if (simpleDecl.getDeclSpecifier() instanceof IASTNamedTypeSpecifier)
						struct = ((IASTNamedTypeSpecifier)simpleDecl.getDeclSpecifier()).getName().resolveBinding();
					else if (simpleDecl.getDeclSpecifier() instanceof IASTElaboratedTypeSpecifier)
						struct = ((IASTElaboratedTypeSpecifier)simpleDecl.getDeclSpecifier()).getName().resolveBinding();
					else if (simpleDecl.getDeclSpecifier() instanceof IASTCompositeTypeSpecifier)
						struct = ((IASTCompositeTypeSpecifier)simpleDecl.getDeclSpecifier()).getName().resolveBinding();
					
					if (struct instanceof CStructure) {
						try {
                            return ((CStructure)struct).findField(((ICASTFieldDesignator)node).getName().toString());
                        } catch (DOMException e) {
                            return e.getProblem();
                        }
					} else if (struct instanceof ITypeContainer) {
						IType type;
                        try {
                            type = ((ITypeContainer)struct).getType();
                            while (type instanceof ITypeContainer && !(type instanceof CStructure)) {
    							type = ((ITypeContainer)type).getType();
    						}
                        } catch (DOMException e) {
                            return e.getProblem();
                        }
                        
						
						if (type instanceof CStructure)
                            try {
                                return ((CStructure)type).findField(((ICASTFieldDesignator)node).getName().toString());
                            } catch (DOMException e1) {
                                return e1.getProblem();
                            }
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * May return <code>null</code>, e.g. for parameter names in function-prototypes.
	 */
	public static IScope getContainingScope(IASTNode node) {
	    if (node == null)
			return null;
		while (node != null) {
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
				} else if (parent instanceof ICASTKnRFunctionDeclarator) {
					parent = ((IASTDeclarator)parent).getParent();
					if (parent instanceof IASTFunctionDefinition) {
						return ((IASTCompoundStatement)((IASTFunctionDefinition)parent).getBody()).getScope();
					}
				}
		    } else if (node instanceof IASTStatement)
		        return getContainingScope((IASTStatement) node);
		    else if (node instanceof IASTParameterDeclaration) {
				IASTNode parent = node.getParent();
				if (parent instanceof IASTStandardFunctionDeclarator) {
					parent = ((IASTDeclarator)parent).getParent();
					if (parent instanceof IASTFunctionDefinition)
						return ((IASTCompoundStatement)((IASTFunctionDefinition)parent).getBody()).getScope();
					return null;	// parameter name in function declarations
				}
		    }
		    else if (node instanceof IASTEnumerator) {
		        //put the enumerators in the same scope as the enumeration
		        node = node.getParent();
		    }
		    
		    node = node.getParent();
		}
	    return null;
	}
	
	public static IScope getContainingScope(IASTStatement statement) {
		IASTNode parent = statement.getParent();
		IScope scope = null;
		if (parent instanceof IASTCompoundStatement) {
		    IASTCompoundStatement compound = (IASTCompoundStatement) parent;
		    scope = compound.getScope();
		} else if (parent instanceof IASTStatement) {
			scope = getContainingScope((IASTStatement)parent);
		} else if (parent instanceof IASTFunctionDefinition) {
			IASTFunctionDeclarator fnDeclarator = ((IASTFunctionDefinition) parent).getDeclarator();
			IBinding function = CVisitor.findInnermostDeclarator(fnDeclarator).getName().resolveBinding();
			try {
				if (function instanceof IFunction) {
					scope = ((IFunction)function).getFunctionScope();
				} else if (function instanceof ProblemBinding) {
					return (IScope) function;
				}
            } catch (DOMException e) {
                return e.getProblem();
            }
		}
		
		if (statement instanceof IASTGotoStatement || statement instanceof IASTLabelStatement) {
		    //labels have function scope
		    while (scope != null && !(scope instanceof ICFunctionScope)) {
		        try {
                    scope = scope.getParent();
                } catch (DOMException e) {
                    scope = e.getProblem();
                    break;
                }
		    }
		}
		
		return scope;
	}
	
	private static IASTNode getContainingBlockItem(IASTNode node) {
		IASTNode parent = node.getParent();
		if (parent instanceof IASTDeclaration) {
			IASTNode p = parent.getParent();
			if (p instanceof IASTDeclarationStatement)
				return p;
			return parent;
		}
		//if parent is something that can contain a declaration
		else if (parent instanceof IASTCompoundStatement || 
				  parent instanceof IASTTranslationUnit   ||
				  parent instanceof IASTForStatement  ||
				  parent instanceof IASTFunctionDeclarator)
		{
			return node;
		}
		
		return getContainingBlockItem(parent);
	}
	
	/**
	 * if (bits & PREFIX_LOOKUP) then returns IBinding[]
	 * otherwise returns IBinding
	 */
	protected static Object findBinding(IASTNode blockItem, IASTName name, int bits) throws DOMException{
		IIndexFileSet fileSet= IIndexFileSet.EMPTY;
		if (blockItem != null) {
			final IASTTranslationUnit tu= blockItem.getTranslationUnit();
			if (tu != null) {
				final IIndexFileSet fs= (IIndexFileSet) tu.getAdapter(IIndexFileSet.class);
				if (fs != null) {
					fileSet= fs;
				}
			}
		}
		
	    boolean prefix = (bits & PREFIX_LOOKUP) != 0;
	    @SuppressWarnings("unchecked")
		Object binding =  prefix ? new ObjectSet(2) : null;
		IIndexBinding foundIndexBinding= null;
		CharArrayObjectMap prefixMap = prefix ? new CharArrayObjectMap(2) : null;

		while (blockItem != null) {
			IASTNode parent = blockItem.getParent();
			IASTNode[] nodes = null;
			IScope scope = null;
			if (parent instanceof IASTCompoundStatement) {
				IASTCompoundStatement compound = (IASTCompoundStatement) parent;
				scope =  compound.getScope();
				
				if (parent.getParent() instanceof IASTFunctionDefinition) {
			        IASTFunctionDeclarator dtor = ((IASTFunctionDefinition)parent.getParent()).getDeclarator();
			        if (dtor instanceof IASTStandardFunctionDeclarator)
			            nodes = ((IASTStandardFunctionDeclarator)dtor).getParameters();
			        else if (dtor instanceof ICASTKnRFunctionDeclarator)
			            nodes = ((ICASTKnRFunctionDeclarator)dtor).getParameterDeclarations();
			    } 
				if (nodes == null || nodes.length == 0) {
					nodes = compound.getStatements();
			    }
			} else if (parent instanceof IASTTranslationUnit) {
				IASTTranslationUnit translation = (IASTTranslationUnit) parent;
				if (!prefix) {
					nodes = translation.getDeclarations();
					scope =  translation.getScope();
				} else {
					// The index will be search later, still we need to look at the declarations found in
					// the AST, bug 180883
					nodes = translation.getDeclarations();
				}
			} else if (parent instanceof IASTStandardFunctionDeclarator) {
			    IASTStandardFunctionDeclarator dtor = (IASTStandardFunctionDeclarator) parent;
				nodes = dtor.getParameters();
				scope =  getContainingScope(blockItem);
			} else if (parent instanceof ICASTKnRFunctionDeclarator) {
			    ICASTKnRFunctionDeclarator dtor = (ICASTKnRFunctionDeclarator) parent;
				nodes = dtor.getParameterDeclarations();
				scope =  getContainingScope(blockItem);
			}
			
			boolean typesOnly = (bits & TAGS) != 0;
			boolean includeBlockItem = (bits & INCLUDE_BLOCK_ITEM) != 0;
			if (prefix)
			    scope = null;
			
			if (scope != null && ASTInternal.isFullyCached(scope)) {
			    try {
                    binding = scope.getBinding(name, true, fileSet);
                } catch (DOMException e) {
                    binding = null;
                }
			    if (binding != null)
			        return binding;
			} else {
				if (!prefix && scope != null  && scope.getParent() == null) {
					binding= scope.getBinding(name, false, fileSet);
					if (binding != null) {
						if (binding instanceof IIndexBinding) {
							foundIndexBinding= (IIndexBinding) binding;
						}
						else {
							return binding;
						}
					}
				}
					
				Object result = null;
				boolean reachedBlockItem = false;
				if (nodes != null) {
				    int idx = -1;
					IASTNode node = nodes.length > 0 ? nodes[++idx] : null;
					while (node != null) {
						Object candidate = null;
	                    try {
	                        candidate = checkForBinding(scope, node, name, typesOnly, prefixMap);
	                    } catch (DOMException e) {
	                        continue;
	                    }
				        
						if (result == null && !reachedBlockItem && 
							(includeBlockItem || (node != blockItem)))
						{
						    result = candidate;
						}
						if (node == blockItem) {
	                    	reachedBlockItem = true;
	                    }
						
						if (idx > -1 && ++idx < nodes.length) {
							node = nodes[idx];
						} else {
						    node = null;
						    if (nodes[0].getPropertyInParent() == ICASTKnRFunctionDeclarator.FUNCTION_PARAMETER ||
						        nodes[0].getPropertyInParent() == IASTStandardFunctionDeclarator.FUNCTION_PARAMETER) 
						    {
						    	//function body, we were looking at parameters, now check the body itself
						    	IASTCompoundStatement compound = null;
						    	if (parent instanceof IASTCompoundStatement) {
						    		compound = (IASTCompoundStatement) parent;
						    	} else if (parent instanceof IASTFunctionDeclarator) {
						    		IASTNode n = parent.getParent();
						    		while (n instanceof IASTDeclarator)
						    			n = n.getParent();
						    		if (n instanceof IASTFunctionDefinition) {
						    			compound = (IASTCompoundStatement) ((IASTFunctionDefinition)n).getBody();
						    		}
						    	}
						    	if (compound != null) {
									nodes = compound.getStatements(); 
									if (nodes.length > 0) {
								        idx = 0;
								        node = nodes[0];
								    }	
						    	}
						    }
						}
					}
					
				} else {
				    try {
	                    result = checkForBinding(scope, parent, name, typesOnly, prefixMap);
	                } catch (DOMException e) {
	                }
				}
				if (scope != null) {
	                try {
	                	ASTInternal.setFullyCached(scope, true);
	                } catch (DOMException e) {
	                }
				}
				if (result != null) {
					if (CVisitor.declaredBefore((IASTName)result, name)) {
						return ((IASTName)result).resolveBinding();
					}
				}
			}
			if ((bits & CURRENT_SCOPE) == 0)
				blockItem = parent;
			else 
				blockItem = null;
			
			if (blockItem instanceof IASTTranslationUnit)
			    break;
		}
		if (foundIndexBinding != null) {
			return foundIndexBinding;
		}
		if (prefixMap != null) {
		    IBinding[] result = null;
		    Object[] vals = prefixMap.valueArray();
		    for (Object val : vals) {
                result = (IBinding[]) ArrayUtil.append(IBinding.class, result, ((IASTName) val).resolveBinding());
            }
		    
		    IASTTranslationUnit tu = (IASTTranslationUnit)blockItem;
			IIndex index = tu.getIndex();
			if (index != null) {
				try {
					IndexFilter filter = IndexFilter.C_DECLARED_OR_IMPLICIT;
					IBinding[] bindings= prefix 
						? index.findBindingsForPrefix(name.toCharArray(), true, filter, null) 
						: index.findBindings(name.toCharArray(), filter, null);
					bindings= fileSet.filterFileLocalBindings(bindings);
					result = (IBinding[]) ArrayUtil.addAll(IBinding.class, result, bindings);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		    
		    return ArrayUtil.trim(IBinding.class, result);
		}
		if (blockItem != null) {
			if (binding == null)
				return externalBinding((IASTTranslationUnit) blockItem, name);
			return binding;
		}
		return null;
	}
	
	private static IBinding externalBinding(IASTTranslationUnit tu, IASTName name) {
	    IASTNode parent = name.getParent();
	    IBinding external = null;
	    if (parent instanceof IASTIdExpression) {
	        if (parent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME) {
	            //external function
	            external = new CExternalFunction(tu, name);
	            ((CScope)tu.getScope()).addName(name);
	        } 
	        else {
	            //external variable
	            //external = new CExternalVariable(tu, name);
       	        //((CScope)tu.getScope()).addName(name);
	        	external = new ProblemBinding(name, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, name.toCharArray());
	        }
	    }
	    return external;
	}
	
	private static IASTName checkForBinding(IScope scope, IASTDeclSpecifier declSpec, IASTName name, boolean typesOnly, CharArrayObjectMap prefixMap) throws DOMException{
		IASTName tempName = null;
		IASTName resultName = null;
		char[] n = name.toCharArray();
		if (declSpec instanceof ICASTElaboratedTypeSpecifier) {
			tempName = ((ICASTElaboratedTypeSpecifier)declSpec).getName();
			
			// Don't include the query name in the results
			if (tempName == name) {
				return null;
			}
			
			if (scope != null)
			    ASTInternal.addName(scope,  tempName);
			if (typesOnly) {
			    if (prefixMap != null) 
	                prefixMap = (CharArrayObjectMap) collectResult(tempName, n, prefixMap);
	            else if (collectResult(tempName, n, prefixMap) != null)
	                resultName = tempName;
			}
		} else if (declSpec instanceof ICASTCompositeTypeSpecifier) {
			tempName = ((ICASTCompositeTypeSpecifier)declSpec).getName();
			if (scope != null)
			    ASTInternal.addName(scope,  tempName);
			
			if (typesOnly) {
			    if (prefixMap != null) 
	                prefixMap = (CharArrayObjectMap) collectResult(tempName, n, prefixMap);
	            else if (collectResult(tempName, n, prefixMap) != null)
	                resultName = tempName;
			}
			//also have to check for any nested structs
			IASTDeclaration[] nested = ((ICASTCompositeTypeSpecifier)declSpec).getMembers();
			for (IASTDeclaration element : nested) {
				if (element instanceof IASTSimpleDeclaration) {
					IASTDeclSpecifier d = ((IASTSimpleDeclaration)element).getDeclSpecifier();
					if (d instanceof ICASTCompositeTypeSpecifier || d instanceof IASTEnumerationSpecifier) {
						Object obj = checkForBinding(scope, d, name, typesOnly, prefixMap);
					    if (prefixMap == null && resultName == null) {
						    resultName = (IASTName) obj;
						}
					}
				}
			}
		} else if (declSpec instanceof ICASTEnumerationSpecifier) {
		    ICASTEnumerationSpecifier enumeration = (ICASTEnumerationSpecifier) declSpec;
		    tempName = enumeration.getName();
		    if (scope != null)
		        ASTInternal.addName(scope,  tempName);
		    if (typesOnly) {
	            if (prefixMap != null) 
	                prefixMap = (CharArrayObjectMap) collectResult(tempName, n, prefixMap);
	            else if (collectResult(tempName, n, prefixMap) != null)
	                resultName = tempName;
			}
		    //check enumerators
		    IASTEnumerator[] list = ((ICASTEnumerationSpecifier) declSpec).getEnumerators();
		    for (IASTEnumerator enumerator : list) {
		        if (enumerator == null) break;
		        tempName = enumerator.getName();
		        if (scope != null)
		            ASTInternal.addName(scope,  tempName);
		        if (!typesOnly) {
		            if (prefixMap != null) 
		                prefixMap = (CharArrayObjectMap) collectResult(tempName, n, prefixMap);
		            else if (collectResult(tempName, n, prefixMap) != null)
		                resultName = tempName;
				}
		    }
		}
		return resultName;
	}
	
	private static Object collectResult(IASTName candidate, char[] name, CharArrayObjectMap prefixMap) {
	    char[] c = candidate.toCharArray();
        if (prefixMap == null && CharArrayUtils.equals(c, name)) {
            return candidate;
        } else if (prefixMap != null && CharArrayUtils.equals(c, 0, name.length, name, true) && !prefixMap.containsKey(c)) {
	        prefixMap.put(c, candidate);
	    }
        return prefixMap;
	}
	
	private static IASTName checkForBinding(IScope scope, IASTParameterDeclaration paramDecl, IASTName name, boolean typesOnly, CharArrayObjectMap prefixMap) throws DOMException{
	    if (paramDecl == null) return null;
	    
	    IASTDeclarator dtor = paramDecl.getDeclarator();
		while (dtor.getNestedDeclarator() != null) {
		    dtor = dtor.getNestedDeclarator();
		}
		IASTName tempName = dtor.getName();
		if (scope != null)
		    ASTInternal.addName(scope,  tempName);
		
		if (!typesOnly) {
		    char[] c = tempName.toCharArray();
		    char[] n = name.toCharArray();
		    if (prefixMap == null && CharArrayUtils.equals(c, n))
		        return tempName;
		    else if (prefixMap != null && CharArrayUtils.equals(c, 0, n.length, n, true) && !prefixMap.containsKey(c))
		        prefixMap.put(c, tempName);
		} else {
		    return checkForBinding(scope, paramDecl.getDeclSpecifier(), name, typesOnly, prefixMap);
		}
		return null;
	}
	
	/**
	 * if not a prefix lookup, returns IASTName
	 * if doing prefix lookup, results are in prefixMap, returns null
	 */
	private static IASTName checkForBinding(IScope scope, IASTNode node, IASTName name, boolean typesOnly, CharArrayObjectMap prefixMap) throws DOMException{
	    if (node instanceof IASTDeclaration) {
	        return checkForBinding(scope, (IASTDeclaration) node, name, typesOnly, prefixMap);
	    } else if (node instanceof IASTParameterDeclaration) {
	        return checkForBinding(scope, (IASTParameterDeclaration) node, name, typesOnly, prefixMap);
	    } else if (node instanceof IASTDeclarationStatement) {
			return checkForBinding(scope, ((IASTDeclarationStatement)node).getDeclaration(), name, typesOnly, prefixMap);
		} else if (node instanceof IASTForStatement) {
			IASTForStatement forStatement = (IASTForStatement) node;
			if (forStatement.getInitializerStatement() instanceof IASTDeclarationStatement) {
				return checkForBinding(scope, ((IASTDeclarationStatement)forStatement.getInitializerStatement()).getDeclaration(), name, typesOnly, prefixMap);
			}
		}
	    return null;
	}

	private static IASTName checkForBinding(IScope scope, IASTDeclaration declaration, IASTName name, boolean typesOnly, CharArrayObjectMap prefixMap) throws DOMException{
	    char[] n = name.toCharArray();
		IASTName tempName = null;
		IASTName resultName = null;
		if (declaration instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
			IASTDeclarator[] declarators = simpleDeclaration.getDeclarators();
			for (IASTDeclarator declarator : declarators) {
				declarator= CVisitor.findInnermostDeclarator(declarator);
				tempName = declarator.getName();
				if (scope != null)
				    ASTInternal.addName(scope,  tempName);
				
				if (!typesOnly) {
		            if (prefixMap != null) 
		                prefixMap = (CharArrayObjectMap) collectResult(tempName, n, prefixMap);
		            else if (collectResult(tempName, n, prefixMap) != null)
		                resultName = tempName;
		            
				}
			}
			tempName = checkForBinding(scope, simpleDeclaration.getDeclSpecifier(), name, typesOnly, prefixMap);
		    if (prefixMap == null && tempName != null) {
			    resultName = tempName;
			}
		} else if (!typesOnly && declaration instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) declaration;

			IASTDeclarator dtor = CVisitor.findInnermostDeclarator(functionDef.getDeclarator());
			tempName = dtor.getName();
			if (scope != null)
			    ASTInternal.addName(scope,  tempName);

			if (!typesOnly) {
	            if (prefixMap != null) 
	                prefixMap = (CharArrayObjectMap) collectResult(tempName, n, prefixMap);
	            else if (collectResult(tempName, n, prefixMap) != null)
	                resultName = tempName;
			}
			
			tempName = checkForBinding(scope, functionDef.getDeclSpecifier(), name, typesOnly, prefixMap); 
		    if (prefixMap == null && tempName != null) {
			    resultName = tempName;
			}
		}
		
		return resultName;
	}
	
	protected static IASTDeclarator findDefinition(IASTDeclarator declarator, int beginAtLoc) {
	    return (IASTDeclarator) findDefinition(declarator, declarator.getName().toCharArray(), beginAtLoc);
	}

	protected static IASTFunctionDeclarator findDefinition(IASTFunctionDeclarator declarator) {
		return (IASTFunctionDeclarator) findDefinition(declarator, declarator.getName().toCharArray(), AT_NEXT);
	}

	protected static IASTDeclSpecifier findDefinition(ICASTElaboratedTypeSpecifier declSpec) {
		return (IASTDeclSpecifier) findDefinition(declSpec, declSpec.getName().toCharArray(), AT_BEGINNING);
	}

	private static IASTNode findDefinition(IASTNode decl, char[] declName, int beginAtLoc) {
		IASTNode blockItem = getContainingBlockItem(decl);
		IASTNode parent = blockItem.getParent();
		IASTNode[] list = null;
		if (parent instanceof IASTCompoundStatement) {
			IASTCompoundStatement compound = (IASTCompoundStatement) parent;
			list = compound.getStatements();
		} else if (parent instanceof IASTTranslationUnit) {
			IASTTranslationUnit translation = (IASTTranslationUnit) parent;
			list = translation.getDeclarations();
		}
		boolean begun = (beginAtLoc == AT_BEGINNING);
		if (list != null) {
			for (IASTNode node : list) {
				if (node == blockItem) {
				    begun = true;
					continue;
				}
				
				if (begun) {
					if (node instanceof IASTDeclarationStatement) {
						node = ((IASTDeclarationStatement) node).getDeclaration();
					}
					
					if (node instanceof IASTFunctionDefinition && decl instanceof IASTFunctionDeclarator) {
						IASTFunctionDeclarator dtor = ((IASTFunctionDefinition) node).getDeclarator();
						IASTName name = CVisitor.findInnermostDeclarator(dtor).getName();
						if (name.toString().equals(declName)) {
							return dtor;
						}
					} else if (node instanceof IASTSimpleDeclaration && decl instanceof ICASTElaboratedTypeSpecifier) {
						IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) node;
						IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();
						IASTName name = null;
						
						if (declSpec instanceof ICASTCompositeTypeSpecifier) {
						    name = ((ICASTCompositeTypeSpecifier)declSpec).getName();
						} else if (declSpec instanceof ICASTEnumerationSpecifier) {
						    name = ((ICASTEnumerationSpecifier)declSpec).getName();
						}
						if (name !=  null) {
						    if (CharArrayUtils.equals(name.toCharArray(), declName)) {
								return declSpec;
							}
						}
					} else if (node instanceof IASTSimpleDeclaration && decl instanceof IASTDeclarator) {
					    IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) node;
					    IASTDeclarator[] dtors = simpleDecl.getDeclarators();
					    for (int j = 0; dtors != null && j < dtors.length; j++) {
					        if (CharArrayUtils.equals(dtors[j].getName().toCharArray(), declName)) {
					            return dtors[j];
					        }
					    }
					}
				}
			}
		}
		return null;
	}
	
	public static void clearBindings(IASTTranslationUnit tu) {
		tu.accept(new ClearBindingAction());
	}
	
	/**
	 * Create an IType for an IASTDeclarator.
	 * 
	 * @param declarator the IASTDeclarator whose IType will be created
	 * @return the IType of the IASTDeclarator parameter
	 */
	public static IType createType(IASTDeclarator declarator) {
	    IASTDeclSpecifier declSpec = null;
		
		IASTNode node = declarator.getParent();
		while (node instanceof IASTDeclarator) {
			declarator = (IASTDeclarator) node;
			node = node.getParent();
		}
		
		if (node instanceof IASTParameterDeclaration)
			declSpec = ((IASTParameterDeclaration) node).getDeclSpecifier();
		else if (node instanceof IASTSimpleDeclaration)
			declSpec = ((IASTSimpleDeclaration)node).getDeclSpecifier();
		else if (node instanceof IASTFunctionDefinition)
			declSpec = ((IASTFunctionDefinition)node).getDeclSpecifier();
		else if (node instanceof IASTTypeId)
		    declSpec = ((IASTTypeId)node).getDeclSpecifier();
	
		boolean isParameter = (node instanceof IASTParameterDeclaration || node.getParent() instanceof ICASTKnRFunctionDeclarator); 
		
		IType type = null;
		
		//C99 6.7.5.3-12 The storage class specifier for a parameter declaration is ignored unless the declared parameter is one of the 
		//members of the parameter type list for a function definition.
		if (isParameter && node.getParent().getParent() instanceof IASTFunctionDefinition) {
		    type = createBaseType(declSpec);
		} else {
		    type = createType((ICASTDeclSpecifier) declSpec);
		}
		
		type = createType(type, declarator);
		
		
        if (isParameter) {
            //C99: 6.7.5.3-7 a declaration of a parameter as "array of type" shall be adjusted to "qualified pointer to type", where the
    		//type qualifiers (if any) are those specified within the[and] of the array type derivation
            if (type instanceof IArrayType) {
	            CArrayType at = (CArrayType) type;
	            type = new CQualifiedPointerType(at.getType(), at.getModifier());
	        } else if (type instanceof IFunctionType) {
	            //-8 A declaration of a parameter as "function returning type" shall be adjusted to "pointer to function returning type"
	            type = new CPointerType(type, 0);
	        }
        }
        
		return type;
	}
	
	public static IType createType(IType baseType, IASTDeclarator declarator) {
	    if (declarator instanceof IASTFunctionDeclarator)
	        return createType(baseType, (IASTFunctionDeclarator)declarator);
		
		IType type = baseType;
		type = setupPointerChain(declarator.getPointerOperators(), type);
		type = setupArrayChain(declarator, type);
		
	    IASTDeclarator nested = declarator.getNestedDeclarator();
	    if (nested != null) {
	    	return createType(type, nested);
	    }
	    return type;
	}
	
	public static IType createType(IType returnType, IASTFunctionDeclarator declarator) {

	    IType[] pTypes = getParmTypes(declarator);
	    returnType = setupPointerChain(declarator.getPointerOperators(), returnType);
	    
	    IType type = new CFunctionType(returnType, pTypes);
	    
	    IASTDeclarator nested = declarator.getNestedDeclarator();
	    if (nested != null) {
	    	return createType(type, nested);
	    }
	    return type;
	}

	/**
	 * This is used to create a base IType corresponding to an IASTDeclarator and the IASTDeclSpecifier.  This method doesn't have any recursive
	 * behaviour and is used as the foundation of the ITypes being created.  
	 * The parameter isParm is used to specify whether the declarator is a parameter or not.  
	 * 
	 * @param declSpec the IASTDeclSpecifier used to determine if the base type is a CQualifierType or not
	 * @return the base IType
	 */
	public static IType createBaseType(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof IGCCASTSimpleDeclSpecifier) {
			IASTExpression exp = ((IGCCASTSimpleDeclSpecifier)declSpec).getTypeofExpression();
			if (exp != null)
				return getExpressionType(exp);
			return new CBasicType((ICASTSimpleDeclSpecifier) declSpec);
		} else if (declSpec instanceof ICASTSimpleDeclSpecifier) {
		    return new CBasicType((ICASTSimpleDeclSpecifier)declSpec);
		} 
		IBinding binding = null;
		IASTName name = null;
		if (declSpec instanceof ICASTTypedefNameSpecifier) {
			name = ((ICASTTypedefNameSpecifier) declSpec).getName();
		} else if (declSpec instanceof IASTElaboratedTypeSpecifier) {
			name = ((IASTElaboratedTypeSpecifier) declSpec).getName();
		} else if (declSpec instanceof IASTCompositeTypeSpecifier) {
			name = ((IASTCompositeTypeSpecifier) declSpec).getName();		
		} else if (declSpec instanceof IASTEnumerationSpecifier) {
			name = ((IASTEnumerationSpecifier)declSpec).getName();
		} else {
			return new ProblemBinding(declSpec, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, declSpec.getRawSignature().toCharArray());
		}
		
		binding = name.resolveBinding();
		if (binding instanceof IType)
		    return (IType) binding;
		
		if (binding != null)
			return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_TYPE, name.toCharArray());
		return new ProblemBinding(name, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, name.toCharArray());
	}

	public static IType createType(ICASTDeclSpecifier declSpec) {
	    if (declSpec.isConst() || declSpec.isVolatile() || declSpec.isRestrict()) {
			return new CQualifierType(declSpec);
		}
	    
	    return createBaseType(declSpec);
	}

	/**
	 * Returns an IType[] corresponding to the parameter types of the IASTFunctionDeclarator parameter.
	 * 
	 * @param decltor the IASTFunctionDeclarator to create an IType[] for its parameters
	 * @return IType[] corresponding to the IASTFunctionDeclarator parameters
	 */
	private static IType[] getParmTypes(IASTFunctionDeclarator decltor) {
		if (decltor instanceof IASTStandardFunctionDeclarator) {
			IASTParameterDeclaration parms[] = ((IASTStandardFunctionDeclarator)decltor).getParameters();
			IType parmTypes[] = new IType[parms.length];
			
		    for (int i = 0; i < parms.length; i++) {
		    	parmTypes[i] = createType(parms[i].getDeclarator());
		    }
		    return parmTypes;
		} else if (decltor instanceof ICASTKnRFunctionDeclarator) {
			IASTName parms[] = ((ICASTKnRFunctionDeclarator)decltor).getParameterNames();
			IType parmTypes[] = new IType[parms.length];
			
		    for (int i = 0; i < parms.length; i++) {
		        IASTDeclarator dtor = getKnRParameterDeclarator((ICASTKnRFunctionDeclarator) decltor, parms[i]);
                if (dtor != null)
                    parmTypes[i] = createType(dtor);
		    }
		    return parmTypes;
		} else {
			return null;
		}
	}
	
    protected static IASTDeclarator getKnRParameterDeclarator(ICASTKnRFunctionDeclarator fKnRDtor, IASTName name) {
        IASTDeclaration[] decls = fKnRDtor.getParameterDeclarations();
        char[] n = name.toCharArray();
        for (int i = 0; i < decls.length; i++) {
            if (!(decls[i] instanceof IASTSimpleDeclaration))
                continue;
            
            IASTDeclarator[] dtors = ((IASTSimpleDeclaration)decls[i]).getDeclarators();
            for (IASTDeclarator dtor : dtors) {
                if (CharArrayUtils.equals(dtor.getName().toCharArray(), n)) {
                    return dtor; 
                }
            }
        }
        return null;
    }
	
	/**
	 * Traverse through an array of IASTArrayModifier[] corresponding to the IASTDeclarator decl parameter.
	 * For each IASTArrayModifier in the array, create a corresponding CArrayType object and 
	 * link it in a chain.  The returned IType is the start of the CArrayType chain that represents
	 * the types of the IASTArrayModifier objects in the declarator.
	 * 
	 * @param decl the IASTDeclarator containing the IASTArrayModifier[] array to create a CArrayType chain for
	 * @param lastType the IType that the end of the CArrayType chain points to 
	 * @return the starting CArrayType at the beginning of the CArrayType chain
	 */
	private static IType setupArrayChain(IASTDeclarator decl, IType lastType) {
		if (decl instanceof IASTArrayDeclarator) {
			int i=0;
			IASTArrayModifier[] mods = ((IASTArrayDeclarator)decl).getArrayModifiers();
			
			CArrayType arrayType = new CArrayType(lastType); 
			if (mods[i] instanceof ICASTArrayModifier) {
				arrayType.setModifiedArrayModifier((ICASTArrayModifier)mods[i++]);
			}
			for (; i < ((IASTArrayDeclarator)decl).getArrayModifiers().length - 1; i++) {
				arrayType = new CArrayType(arrayType);
				if (mods[i] instanceof ICASTArrayModifier) {
					arrayType.setModifiedArrayModifier((ICASTArrayModifier)mods[i]);
				}
			}
			return arrayType;
		}
		
		return lastType;
	}

	/**
	 * Traverse through an array of IASTPointerOperator[] pointers and set up a pointer chain 
	 * corresponding to the types of the IASTPointerOperator[].
	 * 
	 * @param ptrs an array of IASTPointerOperator[] used to setup the pointer chain
	 * @param lastType the IType that the end of the CPointerType chain points to
	 * @return the starting CPointerType at the beginning of the CPointerType chain
	 */
	private static IType setupPointerChain(IASTPointerOperator[] ptrs, IType lastType) {
		CPointerType pointerType = null;
		
		if (ptrs != null && ptrs.length > 0) {
			pointerType = new CPointerType();
											
			if (ptrs.length == 1) {
				pointerType.setType(lastType);
				pointerType.setQualifiers(
						(((ICASTPointer)ptrs[0]).isConst() ? CPointerType.IS_CONST : 0) |
						(((ICASTPointer)ptrs[0]).isRestrict() ? CPointerType.IS_RESTRICT : 0) |
						(((ICASTPointer)ptrs[0]).isVolatile() ? CPointerType.IS_VOLATILE : 0));				
			} else {
				CPointerType tempType = new CPointerType();
				pointerType.setType(tempType);
				pointerType.setQualifiers(
						(((ICASTPointer)ptrs[ptrs.length - 1]).isConst() ? CPointerType.IS_CONST : 0) |
						(((ICASTPointer)ptrs[ptrs.length - 1]).isRestrict() ? CPointerType.IS_RESTRICT : 0) |
						(((ICASTPointer)ptrs[ptrs.length - 1]).isVolatile() ? CPointerType.IS_VOLATILE : 0));
				int i = ptrs.length - 2;
				for (; i > 0; i--) {
					tempType.setType(new CPointerType());
					tempType.setQualifiers(
							(((ICASTPointer)ptrs[i]).isConst() ? CPointerType.IS_CONST : 0) |
							(((ICASTPointer)ptrs[i]).isRestrict() ? CPointerType.IS_RESTRICT : 0) |
							(((ICASTPointer)ptrs[i]).isVolatile() ? CPointerType.IS_VOLATILE : 0));
					tempType = (CPointerType)tempType.getType();
				}					
				tempType.setType(lastType);
				tempType.setQualifiers(
						(((ICASTPointer)ptrs[i]).isConst() ? CPointerType.IS_CONST : 0) |
						(((ICASTPointer)ptrs[i]).isRestrict() ? CPointerType.IS_RESTRICT : 0) |
						(((ICASTPointer)ptrs[i]).isVolatile() ? CPointerType.IS_VOLATILE : 0));
			}
			
			return pointerType;
		}
		
		return lastType;
	}
	
	public static IASTProblem[] getProblems(IASTTranslationUnit tu) {
		CollectProblemsAction action = new CollectProblemsAction();
		tu.accept(action);
		
		return action.getProblems();
	}
	
	public static IASTName[] getDeclarations(IASTTranslationUnit tu, IBinding binding) {
		CollectDeclarationsAction action = new CollectDeclarationsAction(binding);
		tu.accept(action);

		return action.getDeclarationNames();
	}

	public static IASTName[] getReferences(IASTTranslationUnit tu, IBinding binding) {
		CollectReferencesAction action = new CollectReferencesAction(binding);
		tu.accept(action);
		return action.getReferences();
	}

    public static IBinding findTypeBinding(IASTNode startingPoint, IASTName name) throws DOMException {
        if (startingPoint instanceof IASTTranslationUnit) {
            IASTDeclaration[] declarations = ((IASTTranslationUnit)startingPoint).getDeclarations();
            if (declarations.length > 0)
               return (IBinding) findBinding(declarations[declarations.length - 1], name, COMPLETE | INCLUDE_BLOCK_ITEM );
        }
        if (startingPoint instanceof IASTCompoundStatement) {
            IASTStatement[] statements = ((IASTCompoundStatement)startingPoint).getStatements();
            if (statements.length > 0)
                return (IBinding) findBinding(statements[statements.length - 1], name, COMPLETE | INCLUDE_BLOCK_ITEM);
        }
        return null;
    }
    
    public static IBinding[] findBindingsForContentAssist(IASTName name, boolean isPrefix) {
        ASTNodeProperty prop = name.getPropertyInParent();
        
        IBinding[] result = null; 
        
        if (prop == IASTFieldReference.FIELD_NAME) {
            result = (IBinding[]) findBinding((IASTFieldReference) name.getParent(), isPrefix);
        } else {
	        int bits = isPrefix ? PREFIX_LOOKUP : COMPLETE;
	        if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
	            bits |= TAGS;
	        } else if (prop == IASTIdExpression.ID_NAME) {
	            bits |= INCLUDE_BLOCK_ITEM;
	        }
	        
	        IASTNode blockItem = getContainingBlockItem(name);
	        try {
	            result = isPrefix ? (IBinding[]) findBinding(blockItem, name, bits) :
	            	new IBinding[] { (IBinding) findBinding(blockItem, name, bits) };
	        } catch (DOMException e) {
	        }
        }
        
        return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
    }
    
    public static IBinding[] findBindings(IScope scope, String name, boolean prefixLookup) throws DOMException {
        IASTNode node = ASTInternal.getPhysicalNodeOfScope(scope);
        if (node instanceof IASTFunctionDefinition)
            node = ((IASTFunctionDefinition)node).getBody();
        
        CASTName astName = new CASTName(name.toCharArray());
	    astName.setParent(node);
	    
	    //normal names
	    astName.setPropertyInParent(STRING_LOOKUP_PROPERTY);
	    int flags = prefixLookup ? COMPLETE | PREFIX_LOOKUP : COMPLETE;
	    Object o1 = findBinding(astName, astName, flags);
        
	    IBinding[] b1 = null;
	    if (o1 instanceof IBinding) {
	    	b1 = new IBinding[] { (IBinding) o1 };
	    } else {
	    	b1 = (IBinding[]) o1;
	    }
	    
	    //structure names
        astName.setPropertyInParent(STRING_LOOKUP_TAGS_PROPERTY);
        flags = prefixLookup ? COMPLETE | TAGS | PREFIX_LOOKUP : COMPLETE | TAGS;
        Object o2 = findBinding(astName, astName, flags);

	    IBinding[] b2 = null;
	    if (o2 instanceof IBinding) {
	    	b2 = new IBinding[] { (IBinding) o2 };
	    } else {
	    	b2 = (IBinding[]) o2;
	    }
        
        //label names
        List<ILabel> b3 = new ArrayList<ILabel>();
        do {
            char[] n = name.toCharArray();
            if (scope instanceof ICFunctionScope) {
                ILabel[] labels = ((CFunctionScope)scope).getLabels();
                for (ILabel label : labels) {
	                if (prefixLookup) {
	                	if (CharArrayUtils.equals(label.getNameCharArray(),
	                			0, n.length, n, true)) {
	                		b3.add(label);
	                	}
	                } else {
	                	if (CharArrayUtils.equals(label.getNameCharArray(), n)) {
	                		b3.add(label);
	                		break;
	                	}
	                }
	            }
                if (!prefixLookup) break;
            }
            scope = scope.getParent();
        } while (scope != null);
        
        int c = (b1 == null ? 0 : b1.length) + (b2 == null ? 0 : b2.length) + b3.size();

        IBinding[] result = new IBinding[c];
        
        if (b1 != null)
        	ArrayUtil.addAll(IBinding.class, result, b1);
        
        if (b2 != null)
        	ArrayUtil.addAll(IBinding.class, result, b2);
       
        ArrayUtil.addAll(IBinding.class, result, b3.toArray(new IBinding[b3.size()]));
        
        return result;
    }
    
	static public boolean declaredBefore(IASTNode nodeA, IASTNode nodeB) {
	    if (nodeB == null) return true;
	    if (nodeB.getPropertyInParent() == STRING_LOOKUP_PROPERTY) return true;
	    
	    if (nodeA instanceof ASTNode) {
	    	ASTNode nd= (ASTNode) nodeA;
	        int pointOfDecl = 0;
	        
            ASTNodeProperty prop = nd.getPropertyInParent();
            //point of declaration for a name is immediately after its complete declarator and before its initializer
            if (prop == IASTDeclarator.DECLARATOR_NAME || nd instanceof IASTDeclarator) {
                IASTDeclarator dtor = (IASTDeclarator)((nd instanceof IASTDeclarator) ? nd : nd.getParent());
                while (dtor.getParent() instanceof IASTDeclarator)
                    dtor = (IASTDeclarator) dtor.getParent();
                IASTInitializer init = dtor.getInitializer();
                if (init != null)
                    pointOfDecl = ((ASTNode)init).getOffset() - 1;
                else
                    pointOfDecl = ((ASTNode)dtor).getOffset() + ((ASTNode)dtor).getLength();
            } 
            //point of declaration for an enumerator is immediately after it enumerator-definition
            else if (prop == IASTEnumerator.ENUMERATOR_NAME) {
                IASTEnumerator enumtor = (IASTEnumerator) nd.getParent();
                if (enumtor.getValue() != null) {
                    ASTNode exp = (ASTNode) enumtor.getValue();
                    pointOfDecl = exp.getOffset() + exp.getLength();
                } else {
                    pointOfDecl = nd.getOffset() + nd.getLength();
                }
            } else {
                pointOfDecl = nd.getOffset() + nd.getLength();
            }
            
            return (pointOfDecl < ((ASTNode)nodeB).getOffset());
	    }
	    
	    return true; 
	}


	/** 
	 * Returns the innermost declarator nested within the given <code>declarator</code>, or
	 * <code>declarator</code> itself.
	 * @since 5.0
	 */
	public static IASTDeclarator findInnermostDeclarator(IASTDeclarator declarator) {
		IASTDeclarator innermost= null;
		while(declarator != null) {
			innermost= declarator;
			declarator= declarator.getNestedDeclarator();
		}
		return innermost;
	}
	
	/** 
	 * Returns the outermost declarator the given <code>declarator</code> nests within, or
	 * <code>declarator</code> itself.
	 * @since 5.0
	 */
	public static IASTDeclarator findOutermostDeclarator(IASTDeclarator declarator) {
		IASTDeclarator outermost= null;
		IASTNode candidate= declarator;
		while(candidate instanceof IASTDeclarator) {
			outermost= (IASTDeclarator) candidate;
			candidate= outermost.getParent();
		}
		return outermost;
	}

	/**
	 * Searches for the innermost declarator that contributes the the type declared.
	 * @since 5.0
	 */
	public static IASTDeclarator findTypeRelevantDeclarator(IASTDeclarator declarator) {
		IASTDeclarator result= findInnermostDeclarator(declarator);
		while (result.getPointerOperators().length == 0 
				&& result instanceof IASTFieldDeclarator == false
				&& result instanceof IASTFunctionDeclarator == false
				&& result instanceof IASTArrayModifier == false) {
			final IASTNode parent= result.getParent();
			if (parent instanceof IASTDeclarator) {
				result= (IASTDeclarator) parent;
			} else {
				return result;
			}
		}
		return result;
	}

	
	/**
	 * Searches for the function enclosing the given node. May return <code>null</code>.
	 */
	public static IBinding findEnclosingFunction(IASTNode node) {
		while(node != null && node instanceof IASTFunctionDefinition == false) {
			node= node.getParent();
		}
		if (node == null)
			return null;
		
		IASTDeclarator dtor= findInnermostDeclarator(((IASTFunctionDefinition) node).getDeclarator());
		if (dtor != null) {
			IASTName name= dtor.getName();
			if (name != null) {
				return name.resolveBinding();
			}
		}
		return null;
	}

	/**
	 * Searches for the first function, struct or union enclosing the declaration the provided
	 * node belongs to and returns the binding for it. Returns <code>null</code>, if the declaration is not
	 * enclosed by any of the above constructs.
	 */
	public static IBinding findDeclarationOwner(IASTNode node, boolean allowFunction) {
		// search for declaration
		while (node instanceof IASTDeclaration == false) {
			if (node == null)
				return null;
			
			node= node.getParent();
		}
				
		// search for enclosing binding
		IASTName name= null;
		node= node.getParent();
		for (; node != null; node= node.getParent()) {
			if (node instanceof IASTFunctionDefinition) {
				if (!allowFunction) 
					continue;

				IASTDeclarator dtor= findInnermostDeclarator(((IASTFunctionDefinition) node).getDeclarator());
				if (dtor != null) {
					name= dtor.getName();
				}
				break;
			} 
			if (node instanceof IASTCompositeTypeSpecifier) {
				name= ((IASTCompositeTypeSpecifier) node).getName();
				break;
			}
		}
		if (name == null) 
			return null;
		
		return name.resolveBinding();
	}
}
