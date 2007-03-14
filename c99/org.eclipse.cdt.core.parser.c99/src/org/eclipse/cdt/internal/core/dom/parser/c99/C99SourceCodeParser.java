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

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.c99.ILexerFactory;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.C99BaseKeywordMap;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.C99Preprocessor;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.KeywordMap;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.LocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver;

/**
 * Provides an easy way to invoke the parser.
 * 
 * @author Mike Kucera
 *
 */
public class C99SourceCodeParser {

	private boolean encounteredError = false;
	
	public C99SourceCodeParser() {
		
	}
	
	public IASTTranslationUnit parse(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index) {
		
		ILexerFactory lexerFactory = new C99LexerFactory();
		KeywordMap keywordMap = new C99BaseKeywordMap();
		
		C99Preprocessor preprocessor = new C99Preprocessor(lexerFactory, reader, scanInfo, fileCreator, keywordMap);
		
		C99Parser parser = new C99Parser();
		
		// the preprocessor injects tokens into the parser
		ILocationResolver resolver = preprocessor.preprocess(parser);
		
		parser.prepareToParse();
		IASTTranslationUnit ast = parser.parse(resolver, index);
		
		encounteredError = parser.encounteredError();
		
		return ast;
	}
	
	
	public boolean encounteredError() {
		return encounteredError;
	}

}
