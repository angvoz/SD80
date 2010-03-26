/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;

/**
 * Catching by reference is recommended by C++ experts, for example Herb Sutter/Andrei Alexandresscu "C++ Coding Standards", Rule 73 "Throw by value, catch by reference". 
 * For one thing, this avoids copying and potentially slicing the exception.
 *
 */
public class CatchUsesReference extends AbstractIndexAstChecker {
	private static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.CatchUsesReference";

	public void processAst(IASTTranslationUnit ast) {
		// traverse the ast using the visitor pattern.
		ast.accept(new OnCatch());
	}

	class OnCatch extends ASTVisitor {
		OnCatch() {
			shouldVisitStatements = true;
		}

		public int visit(IASTStatement stmt) {
			if (stmt instanceof ICPPASTTryBlockStatement) {
				ICPPASTTryBlockStatement tblock = (ICPPASTTryBlockStatement) stmt;
				ICPPASTCatchHandler[] catchHandlers = tblock.getCatchHandlers();
				next: for (int i = 0; i < catchHandlers.length; i++) {
					ICPPASTCatchHandler catchHandler = catchHandlers[i];
					IASTDeclaration decl = catchHandler.getDeclaration();
					if (decl instanceof IASTSimpleDeclaration) {
						IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) decl;
						IASTDeclSpecifier spec = sdecl.getDeclSpecifier();
						if (!usesReference(catchHandler)) {
							if (spec instanceof ICPPASTNamedTypeSpecifier) {
								IBinding typeName = ((ICPPASTNamedTypeSpecifier) spec).getName().getBinding();
								// unwind typedef chain
								while (typeName instanceof ITypedef) {
									IType t = ((ITypedef) typeName).getType();
									if (t instanceof IBasicType) continue next;
									if (t instanceof IBinding) typeName = (IBinding) t;
									else break;
								}

								reportProblem(ER_ID, decl);
							}
						}
					}
				}

				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		/**
		 * @param catchHandler
		 * @return
		 */
		private boolean usesReference(ICPPASTCatchHandler catchHandler) {
			IASTDeclaration declaration = catchHandler.getDeclaration();
			if (declaration instanceof IASTSimpleDeclaration) {
				IASTDeclarator[] declarators = ((IASTSimpleDeclaration) declaration).getDeclarators();
				for (int i = 0; i < declarators.length; i++) {
					IASTDeclarator d = declarators[i];
					IASTPointerOperator[] pointerOperators = d.getPointerOperators();
					for (int j = 0; j < pointerOperators.length; j++) {
						IASTPointerOperator po = pointerOperators[j];
						if (po instanceof ICPPASTReferenceOperator) { return true; }

					}
				}
			}
			return false;
		}
	}

}
