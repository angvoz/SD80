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

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.c99.C99Language;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.CommentTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class C99CommentTests extends CommentTests {

	protected IASTTranslationUnit parse( String code, ParserLanguage lang ) 
		throws ParserException {
		if(lang != ParserLanguage.C)
			return super.parse(code, lang);
		
	    return parse(code, lang, false, true );
	}
	    
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions ) 
		throws ParserException {
		if(lang != ParserLanguage.C)
			return super.parse(code, lang, useGNUExtensions);
		
	    return parse( code, lang, useGNUExtensions, true );
	}
	 
    protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) 
    	throws ParserException {
    	if(lang != ParserLanguage.C)
    		return super.parse(code, lang, useGNUExtensions, expectNoProblems);
    	
    	return ParseHelper.parse(code, getC99Language(), expectNoProblems);
    }
    
    
    protected IASTTranslationUnit parse(String code, ParserLanguage lang,
			boolean useGNUExtensions, boolean expectNoProblems,
			boolean parseComments) throws ParserException {
		
    	
    	if(lang != ParserLanguage.C)
    		return super.parse(code, lang, useGNUExtensions, expectNoProblems, parseComments);
    	
    	return ParseHelper.commentParse(code, getC99Language());
    }

	protected C99Language getC99Language() {
    	return new C99Language();
    }
}
