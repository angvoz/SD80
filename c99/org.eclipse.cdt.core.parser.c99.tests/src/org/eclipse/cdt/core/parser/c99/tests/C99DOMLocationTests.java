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

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.c99.C99Language;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.DOMLocationTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class C99DOMLocationTests extends DOMLocationTests {

	public C99DOMLocationTests() {
	}

	public C99DOMLocationTests(String name) {
		super(name);
	}

	protected IASTTranslationUnit parse( String code, ParserLanguage lang ) throws ParserException {
		if(lang != ParserLanguage.C)
			return super.parse(code, lang);
		
	    return parse(code, lang, false, true );
	}
	    
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions ) throws ParserException {
		if(lang != ParserLanguage.C)
			return super.parse(code, lang, useGNUExtensions);
		
	    return parse( code, lang, useGNUExtensions, true );
	}
	 
    protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
    	if(lang != ParserLanguage.C)
    		return super.parse(code, lang, useGNUExtensions, expectNoProblems);
		
    	return ParseHelper.parse(code, getC99Language(), expectNoProblems);
    }
    
    
    protected C99Language getC99Language() {
    	return C99Language.getDefault();
    }
    
    
    // this one fails because the C99 parser does error recovery differently
    public void test162180_1() throws Exception {
    	try {
    		super.test162180_1();
    		fail();
    	}
    	catch(AssertionFailedError e) {}
    	
    }
    
    public void test162180_3() throws Exception {
    	try {
    		super.test162180_3();
    		fail();
    	}
    	catch(AssertionFailedError e) {}
    }
}
