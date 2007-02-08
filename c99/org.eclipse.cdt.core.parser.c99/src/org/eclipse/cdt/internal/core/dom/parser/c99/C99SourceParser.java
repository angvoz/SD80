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

package org.eclipse.cdt.internal.core.dom.parser.c99;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ISourceCodeParser;


/**
 * This class represents the interface to the C99 parser.
 * 
 * @author Mike Kucera
 */
public class C99SourceParser implements ISourceCodeParser {

	private C99Lexer lexer;
	private C99Parser parser;
	
	
	/**
	 * For now just pass it the code.
	 */
	public C99SourceParser(String code) {
		lexer = new C99Lexer(code.toCharArray(), "fakeFileName");
		parser = new C99Parser(lexer);
	}
	
	
	public void cancel() {
		
		// does nothing
	}

	public boolean encounteredError() {
		// TODO Auto-generated method stub
		return false;
	}

	public ASTCompletionNode getCompletionNode() {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTTranslationUnit parse() {
		// TODO: this will have to change
		lexer.lexer(parser);
		parser.parser(-1);
		IASTTranslationUnit ast = parser.getAST();
		return ast;
	}

}
