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
import org.eclipse.cdt.core.dom.c99.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.c99.C99Language;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.CompleteParser2Tests;

public class C99CompleteParser2Tests extends CompleteParser2Tests {

	protected IASTTranslationUnit parse(String code, boolean expectedToPass,
			ParserLanguage lang, boolean gcc) throws Exception {
		
		if(lang != ParserLanguage.C)
			return super.parse(code, expectedToPass, lang, gcc);
		
		return ParseHelper.parse(code, getLanguage(), expectedToPass);
	}

	protected BaseExtensibleLanguage getLanguage() {
    	return C99Language.getDefault();
    }
	
	
	// Tests that are failing at this point
    
	public void testBug39676_tough() { // is this C99?
		try {
			super.testBug39676_tough();
		} catch(AssertionFailedError _) {
			return;
		} catch(Exception _) {
			return;
		}
		
		fail();
	} 
	
	
	public void testBug39551B() { // is this C99?
		try {
			super.testBug39551B();
		} catch(AssertionFailedError _) {
			return;
		} catch(Exception _) {
			return;
		}
		
		fail();
	} 
}
