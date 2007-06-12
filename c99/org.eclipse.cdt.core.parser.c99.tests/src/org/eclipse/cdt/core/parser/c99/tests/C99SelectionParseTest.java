/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.c99.tests;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.c99.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.c99.C99Language;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2SelectionParseTest;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.core.resources.IFile;

public class C99SelectionParseTest extends AST2SelectionParseTest {
	
	public C99SelectionParseTest() {
	}

	public C99SelectionParseTest(String name, Class className) {
		super(name, className);
	}

	public C99SelectionParseTest(String name) {
		super(name);
	}

	protected IASTNode parse(String code, ParserLanguage lang, int offset, int length) throws ParserException {
		if(lang == ParserLanguage.C)
			return parse(code, lang, false, false, offset, length);
		else
			return super.parse(code, lang, offset, length);
	}
	
	protected IASTNode parse(IFile file, ParserLanguage lang, int offset, int length) throws ParserException {
		if(lang == ParserLanguage.C) {
			IASTTranslationUnit tu = parse(file, lang, false, false);
			return tu.selectNodeForLocation(tu.getFilePath(), offset, length);
		}
		else
			return super.parse(file, lang, offset, length);
	}
	
	protected IASTNode parse(String code, ParserLanguage lang, int offset, int length, boolean expectedToPass) throws ParserException {
		if(lang == ParserLanguage.C)
			return parse(code, lang, false, expectedToPass, offset, length);
		else
			return super.parse(code, lang, offset, length, expectedToPass);
	}
	
	protected IASTNode parse(String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems, int offset, int length) throws ParserException {
		if(lang == ParserLanguage.C) {
			IASTTranslationUnit tu = ParseHelper.parse(code, getLanguage(), useGNUExtensions, expectNoProblems, 0);
			return tu.selectNodeForLocation(tu.getFilePath(), offset, length);
		}
		else
			return super.parse(code, lang, useGNUExtensions, expectNoProblems, offset, length);
	}	
	
	protected IASTTranslationUnit parse( IFile file, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
		if(lang == ParserLanguage.C)
			throw new RuntimeException("file parsing not supported yet");//$NON-NLS-1$
		
		return super.parse(file, lang, useGNUExtensions, expectNoProblems);
	}

	
	protected BaseExtensibleLanguage getLanguage() {
		return C99Language.getDefault();
	}
	
	
	// The following three tests fail because they require access to include files
	
	public void testBug96702() {
		try {
			super.testBug96702();
		} catch(Exception _) {  // catch error
			return;
		}
		
		fail();
	}
	
	
	// The following three tests fail because they require access to include files
	
	public void testBug86126() {
		try {
			super.testBug86126();
		} catch(Exception _) {  // catch error
			return;
		}
		
		fail();
	}
	
	
}
