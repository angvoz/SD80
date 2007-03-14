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
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.DOMLocationMacroTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class C99DOMLocationMacroTests extends DOMLocationMacroTests {

	protected IASTTranslationUnit parse( String code, ParserLanguage lang ) {
	    return parse(code, lang, false, true );
	}
	    
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions ) {
	    return parse( code, lang, useGNUExtensions, true );
	}
	 
    protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) {
    	return ParseHelper.parse(code, lang, expectNoProblems);
    }
    
    /**
     * Tests GCC specific stuff, not applicable at this point
     */
    public void testStdioBug() throws ParserException {
    	try {
    		super.testStdioBug();
    		fail();
    	}
    	catch(AssertionFailedError e) {
    		
    	}
    }
    
    public void testFunctionMacroExpansionWithNameSubstitution_Bug173637() throws Exception {
    	try {
    		super.testFunctionMacroExpansionWithNameSubstitution_Bug173637();
    		fail();
    	}
    	catch(AssertionFailedError e) {
    	}
    }
}
