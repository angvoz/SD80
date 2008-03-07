/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.insertbefore;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;

public class CtorChainInitializerTest extends ChangeGeneratorTest {

	public CtorChainInitializerTest(){
		super("Insert Before Ctor Initializer"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "TestClass::TestClass(int a, int b):beta(b){\n}\n\n"; //$NON-NLS-1$
		expectedSource = "TestClass::TestClass(int a, int b):alpha(a), beta(b){\n}\n\n"; //$NON-NLS-1$
		super.setUp();
	}

	@Override
	protected CPPASTVisitor createModificator(
			final ASTModificationStore modStore) {
		return new CPPASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}
			
			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof CPPASTFunctionDeclarator) {
					CPPASTFunctionDeclarator functionDeclarator = (CPPASTFunctionDeclarator)declarator;
					ICPPASTConstructorChainInitializer ctorInitializer = functionDeclarator.getConstructorChain()[0];
					CPPASTIdExpression initExpr = new CPPASTIdExpression(new CPPASTName("a".toCharArray())); //$NON-NLS-1$
					CPPASTName initName = new CPPASTName("alpha".toCharArray()); //$NON-NLS-1$
					ICPPASTConstructorChainInitializer newInitializer = new CPPASTConstructorChainInitializer(initName, initExpr);
					ASTModification modification = new ASTModification(ModificationKind.INSERT_BEFORE, ctorInitializer, newInitializer, null);
					modStore.storeModification(null, modification);
					
				}
				return PROCESS_CONTINUE;
			}
		};
	}
	
	public static Test suite() {
		return new CtorChainInitializerTest();
		
	}
}
