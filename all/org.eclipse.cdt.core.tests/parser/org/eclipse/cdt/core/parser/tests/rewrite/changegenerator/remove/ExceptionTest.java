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
package org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.remove;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.parser.tests.rewrite.changegenerator.ChangeGeneratorTest;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;

public class ExceptionTest extends ChangeGeneratorTest {

	public ExceptionTest(){
		super("Remove Exception Declaration"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		source = "void foo(int parameter) throw (int){\n}\n\n"; //$NON-NLS-1$
		expectedSource = "void foo(int parameter) throw (){\n}\n\n"; //$NON-NLS-1$
		super.setUp();
	}

	public static Test suite() {
		return new ExceptionTest();
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
					IASTTypeId[] exceptions = functionDeclarator.getExceptionSpecification();
					for(IASTTypeId curException : exceptions){		
							ASTModification modification = new ASTModification(ModificationKind.REPLACE, curException, null, null);
							modStore.storeModification(null, modification);
					}
				}
				return PROCESS_CONTINUE;
			}
		};
	}
}
