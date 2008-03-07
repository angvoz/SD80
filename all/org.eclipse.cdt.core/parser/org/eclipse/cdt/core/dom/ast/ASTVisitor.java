/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisitor;

/**
 * Abstract base class for all visitors to traverse ast nodes.  <br>
 * visit() methods implement a top-down traversal, and <br>
 * leave() methods implement a bottom-up traversal. <br>
 * 
 * To visit c- or c++-specific nodes you need to implement {@link ICASTVisitor} 
 * and/or {@link ICPPASTVisitor} in addition to deriving from this class. 
 */
public abstract class ASTVisitor {
	/**
	 * Set this flag to visit names.
	 */
	public boolean shouldVisitNames = false;
	/**
	 * Set this flag to visit declarations.
	 */
	public boolean shouldVisitDeclarations = false;
	/**
	 * Set this flag to visit initializers.
	 */
	public boolean shouldVisitInitializers = false;
	/**
	 * Set this flag to visit parameter declarations.
	 */
	public boolean shouldVisitParameterDeclarations = false;
	/**
	 * Set this flag to visit declarators.
	 */
	public boolean shouldVisitDeclarators = false;
	/**
	 * Set this flag to visit declaration specifiers.
	 */
	public boolean shouldVisitDeclSpecifiers = false;
	/**
	 * Set this flag to visit expressions.
	 */
	public boolean shouldVisitExpressions = false;
	/**
	 * Set this flag to visit statements.
	 */
	public boolean shouldVisitStatements = false;
	/**
	 * Set this flag to visit typeids.
	 */
	public boolean shouldVisitTypeIds = false;
	/**
	 * Set this flag to visit enumerators.
	 */
	public boolean shouldVisitEnumerators = false;
	/**
	 * Set this flag to visit translation units.
	 */
	public boolean shouldVisitTranslationUnit = false;
	/**
	 * Set this flag to visit problem nodes.
	 */
	public boolean shouldVisitProblems = false;

	/**
	 * Set this flag to visit designators of initializers.
	 * Your visitor needs to implement {@link ICASTVisitor} to make this work.
	 */
	public boolean shouldVisitDesignators = false;
	
	/**
	 * Set this flag to visit base specifiers off composite types.
	 * Your visitor needs to implement {@link ICPPASTVisitor} to make this work.
	 */
	public boolean shouldVisitBaseSpecifiers = false;

	/**
	 * Set this flag to visit to visit namespaces.
	 * Your visitor needs to implement {@link ICPPASTVisitor} to make this work.
	 */
	public boolean shouldVisitNamespaces = false;

	/**
	 * Set this flag to visit template parameters.
	 * Your visitor needs to implement {@link ICPPASTVisitor} to make this work.
	 */
	public boolean shouldVisitTemplateParameters = false;


	/**
	 * Skip the traversal of children of this node, don't call leave on this node. 
	 */
	public final static int PROCESS_SKIP = 1;

	/**
	 * Abort the entire traversal.
	 */
	public final static int PROCESS_ABORT = 2;

	/**
	 * Continue with traversing the children of this node.
	 */
	public final static int PROCESS_CONTINUE = 3;

	// visit methods
	public int visit(IASTTranslationUnit tu) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTName name) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTDeclaration declaration) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTInitializer initializer) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTParameterDeclaration parameterDeclaration) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTDeclarator declarator) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTDeclSpecifier declSpec) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTExpression expression) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTStatement statement) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTTypeId typeId) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTEnumerator enumerator) {
		return PROCESS_CONTINUE;
	}
	
	public int visit( IASTProblem problem ){
		return PROCESS_CONTINUE;
	}
	
	@Deprecated
	public int visit( IASTComment comment){
		return PROCESS_CONTINUE;
	}

	// leave methods
	public int leave(IASTTranslationUnit tu) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTName name) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTDeclaration declaration) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTInitializer initializer) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTParameterDeclaration parameterDeclaration) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTDeclarator declarator) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTDeclSpecifier declSpec) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTExpression expression) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTStatement statement) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTTypeId typeId) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTEnumerator enumerator) {
		return PROCESS_CONTINUE;
	}
	
	public int leave(IASTProblem problem){
		return PROCESS_CONTINUE;
	}
	
	public int leave( IASTComment comment){
		return PROCESS_CONTINUE;
	}
}
