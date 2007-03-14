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
		return parse(code, lang, false, false, offset, length);
	}
	
	protected IASTNode parse(IFile file, ParserLanguage lang, int offset, int length) throws ParserException {
		IASTTranslationUnit tu = parse(file, lang, false, false);
		return tu.selectNodeForLocation(tu.getFilePath(), offset, length);
	}
	
	protected IASTNode parse(String code, ParserLanguage lang, int offset, int length, boolean expectedToPass) throws ParserException {
		return parse(code, lang, false, expectedToPass, offset, length);
	}
	
	protected IASTNode parse(String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems, int offset, int length) throws ParserException {
		IASTTranslationUnit tu = ParseHelper.parse(code, lang, useGNUExtensions, expectNoProblems, 0);
		return tu.selectNodeForLocation(tu.getFilePath(), offset, length);
	}	
	
	protected IASTTranslationUnit parse( IFile file, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
		throw new RuntimeException("file parsing not supported yet");
	}

}
