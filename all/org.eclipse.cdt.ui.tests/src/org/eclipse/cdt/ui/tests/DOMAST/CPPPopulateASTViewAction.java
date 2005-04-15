/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTInclusionStatement;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author dsteffle
 */
public class CPPPopulateASTViewAction extends CPPASTVisitor implements IPopulateDOMASTAction {
	private static final int INITIAL_PROBLEM_SIZE = 4;
	private static final int INITIAL_INCLUDE_STATEMENT_SIZE = 8;
	{
		shouldVisitNames          = true;
		shouldVisitDeclarations   = true;
		shouldVisitInitializers   = true;
		shouldVisitParameterDeclarations = true;
		shouldVisitDeclarators    = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitExpressions    = true;
		shouldVisitStatements     = true;
		shouldVisitTypeIds        = true;
		shouldVisitEnumerators    = true;
		shouldVisitBaseSpecifiers = true;
		shouldVisitNamespaces     = true;
	}

	DOMASTNodeParent root = null;
	IProgressMonitor monitor = null;
	IASTProblem[] astProblems = new IASTProblem[INITIAL_PROBLEM_SIZE];
	
	public CPPPopulateASTViewAction(IASTTranslationUnit tu, IProgressMonitor monitor) {
		root = new DOMASTNodeParent(tu);
		this.monitor = monitor;
	}
	
	private int addRoot(IASTNode node) {
        if (monitor != null && monitor.isCanceled()) return PROCESS_ABORT;
        if (node == null) return PROCESS_CONTINUE;
        
        // only do length check for ASTNode (getNodeLocations on PreprocessorStatements is very expensive)
        if (!(node instanceof ICPPASTLinkageSpecification) && 
        	node instanceof ASTNode && ((ASTNode)node).getLength() <= 0)
            return PROCESS_CONTINUE;
        
        DOMASTNodeParent parent = null;
        
        // if it's a preprocessor statement being merged then do a special search for parent (no search)
        if (node instanceof IASTPreprocessorStatement) {
            parent = root;  
        } else {
            IASTNode tempParent = node.getParent();
            if (tempParent instanceof IASTPreprocessorStatement) {
                parent = root.findTreeParentForMergedNode(node);
            } else {
                parent = root.findTreeParentForNode(node);              
            }
        }
        
        if (parent == null)
            parent = root;
        
        createNode(parent, node);
        
        return PROCESS_CONTINUE;
	}
    
    private void createNode(DOMASTNodeParent parent, IASTNode node) {
        DOMASTNodeParent tree = new DOMASTNodeParent(node);
        parent.addChild(tree);
        
        // set filter flags
        if (node instanceof IASTProblemHolder || node instanceof IASTProblem) { 
            tree.setFiltersFlag(DOMASTNodeLeaf.FLAG_PROBLEM);
            
            if (node instanceof IASTProblemHolder)
                astProblems = (IASTProblem[])ArrayUtil.append(IASTProblem.class, astProblems, ((IASTProblemHolder)node).getProblem());
            else
                astProblems = (IASTProblem[])ArrayUtil.append(IASTProblem.class, astProblems, node);
        }
        if (node instanceof IASTPreprocessorStatement)
            tree.setFiltersFlag(DOMASTNodeLeaf.FLAG_PREPROCESSOR);
        if (node instanceof IASTPreprocessorIncludeStatement)
            tree.setFiltersFlag(DOMASTNodeLeaf.FLAG_INCLUDE_STATEMENTS);
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
	 */
	public int visit(IASTDeclaration declaration) {
		return addRoot(declaration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
	 */
	public int visit(IASTDeclarator declarator) {
		int ret = addRoot(declarator);
		
		IASTPointerOperator[] ops = declarator.getPointerOperators();
		for(int i=0; i<ops.length; i++)
			addRoot(ops[i]);
		
		if (declarator instanceof IASTArrayDeclarator) {
			IASTArrayModifier[] mods = ((IASTArrayDeclarator)declarator).getArrayModifiers();
			for(int i=0; i<mods.length; i++)
				addRoot(mods[i]);	
		}
		
		if (declarator instanceof ICPPASTFunctionDeclarator) {
			ICPPASTConstructorChainInitializer[] chainInit = ((ICPPASTFunctionDeclarator)declarator).getConstructorChain();
			for(int i=0; i<chainInit.length; i++) {
				addRoot(chainInit[i]);
			}
			
			if( declarator instanceof ICPPASTFunctionTryBlockDeclarator ){
				ICPPASTCatchHandler [] catchHandlers = ((ICPPASTFunctionTryBlockDeclarator)declarator).getCatchHandlers();
				for( int i = 0; i < catchHandlers.length; i++ ){
					addRoot(catchHandlers[i]);
				}
			}	
		}
		
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processBaseSpecifier(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier)
	 */
	public int visit(ICPPASTBaseSpecifier specifier) {
		return addRoot(specifier);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
	 */
	public int visit(IASTDeclSpecifier declSpec) {
		return addRoot(declSpec);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processEnumerator(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
	 */
	public int visit(IASTEnumerator enumerator) {
		return addRoot(enumerator);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	public int visit(IASTExpression expression) {
		return addRoot(expression);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processInitializer(org.eclipse.cdt.core.dom.ast.IASTInitializer)
	 */
	public int visit(IASTInitializer initializer) {
		return addRoot(initializer);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int visit(IASTName name) {
		if (name.toString() != null)
			return addRoot(name);
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processNamespace(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition)
	 */
	public int visit(ICPPASTNamespaceDefinition namespace) {
		return addRoot(namespace);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processParameterDeclaration(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
	 */
	public int visit(
			IASTParameterDeclaration parameterDeclaration) {
		return addRoot(parameterDeclaration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
	 */
	public int visit(IASTStatement statement) {
		return addRoot(statement);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
	 */
	public int visit(IASTTypeId typeId) {
		return addRoot(typeId);
	}

	private void mergeNode(ASTNode node) {
		addRoot(node);
		
		if (node instanceof IASTPreprocessorMacroDefinition)
			addRoot(((IASTPreprocessorMacroDefinition)node).getName());
	}
	
	public void mergePreprocessorStatements(IASTPreprocessorStatement[] statements) {
		for(int i=0; i<statements.length; i++) {
			if (monitor != null && monitor.isCanceled()) return;
			
			if (statements[i] instanceof ASTNode)
				mergeNode((ASTNode)statements[i]);
		}
	}
	
	public void mergePreprocessorProblems(IASTProblem[] problems) {
		for(int i=0; i<problems.length; i++) {
			if (monitor != null && monitor.isCanceled()) return;
			
			if (problems[i] instanceof ASTNode)
			   mergeNode((ASTNode)problems[i]);
		}
	}
	
	public DOMASTNodeParent getTree() {
		return root;
	}
	
	public void groupIncludes(IASTPreprocessorStatement[] statements) {
		// get all of the includes from the preprocessor statements (need the object since .equals isn't implemented)
		IASTPreprocessorIncludeStatement[] includes = new IASTPreprocessorIncludeStatement[INITIAL_INCLUDE_STATEMENT_SIZE];
		int index = 0;
		for(int i=0; i<statements.length; i++) {
			if (monitor != null && monitor.isCanceled()) return;
			if (statements[i] instanceof IASTPreprocessorIncludeStatement) {
				if (index == includes.length) {
					includes = (IASTPreprocessorIncludeStatement[])ArrayUtil.append(IASTPreprocessorIncludeStatement.class, includes, statements[i]);
					index++;
				} else {
					includes[index++] = (IASTPreprocessorIncludeStatement)statements[i];	
				}
			}
		}
		
		// get the tree model elements corresponding to the includes
		DOMASTNodeParent[] treeIncludes = new DOMASTNodeParent[index];
		for (int i=0; i<treeIncludes.length; i++) {
			if (monitor != null && monitor.isCanceled()) return;
			treeIncludes[i] = root.findTreeObject(includes[i], false);
		}
		
		// loop through the includes and make sure that all of the nodes 
		// that are children of the TU are in the proper include (based on offset)
		DOMASTNodeLeaf child = null;
		outerLoop: for (int i=treeIncludes.length-1; i>=0; i--) {
			if (treeIncludes[i] == null) continue;

			for(int j=root.getChildren(false).length-1; j>=0; j--) {
				if (monitor != null && monitor.isCanceled()) return;
				child = root.getChildren(false)[j];
				
				if (child != null && treeIncludes[i] != child &&
						includes[i] instanceof ASTInclusionStatement &&
						((ASTNode)child.getNode()).getOffset() >= ((ASTInclusionStatement)includes[i]).startOffset &&
						((ASTNode)child.getNode()).getOffset() <= ((ASTInclusionStatement)includes[i]).endOffset) {
					root.removeChild(child);
					treeIncludes[i].addChild(child);
				}
			}
		}
	}
	
	public IASTProblem[] getASTProblems() {
		return astProblems;
	}
}
