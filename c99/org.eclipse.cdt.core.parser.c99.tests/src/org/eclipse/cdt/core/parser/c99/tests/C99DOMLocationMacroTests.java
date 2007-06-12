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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.c99.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.c99.C99Language;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.DOMLocationMacroTests;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class C99DOMLocationMacroTests extends DOMLocationMacroTests {

	protected IASTTranslationUnit parse( String code, ParserLanguage lang ) throws ParserException {
	    return parse(code, lang, false, true );
	}
	    
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions ) throws ParserException {
	    return parse( code, lang, useGNUExtensions, true );
	}
	 
    protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
    	if(lang == ParserLanguage.C)
    		return ParseHelper.parse(code, getLanguage(), expectNoProblems);
    	else
    		return super.parse(code, lang, useGNUExtensions, expectNoProblems);
    }
    
    
    protected BaseExtensibleLanguage getLanguage() {
    	return C99Language.getDefault();
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
    
    
    private void assertMacroLocation(IASTDeclaration decl, int index, int length) {
    	IASTSimpleDeclaration var = (IASTSimpleDeclaration) decl;
        IASTInitializerExpression initializer= (IASTInitializerExpression)var.getDeclarators()[0].getInitializer();
        IASTExpression expr= initializer.getExpression();
        assertNotNull(expr.getFileLocation()); 
        IASTNodeLocation [] locations = expr.getNodeLocations();
        assertEquals(1, locations.length);
        IASTMacroExpansion macroExpansion = (IASTMacroExpansion) locations[0];
        IASTNodeLocation[] expLocations= macroExpansion.getExpansionLocations();
        assertEquals(1, expLocations.length);
        IASTFileLocation fileLocation = expLocations[0].asFileLocation();
        assertEquals(index, fileLocation.getNodeOffset());
        assertEquals(length, fileLocation.getNodeLength());
    }
    
    private void assertExpressionLocation(IASTDeclaration decl, int index, int length) {
    	IASTSimpleDeclaration var = (IASTSimpleDeclaration) decl;
    	IASTInitializerExpression initializer= (IASTInitializerExpression)var.getDeclarators()[0].getInitializer();
        IASTExpression expr= initializer.getExpression();
        IASTFileLocation fileLocation = expr.getFileLocation();
        assertNotNull(fileLocation); 
        assertEquals(index, fileLocation.getNodeOffset());
        assertEquals(length, fileLocation.getNodeLength());
    }
    
    
    public void testBug186257() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("#define Nullstr Null(STR*) \n"); //$NON-NLS-1$
        sb.append("#define Null(x) ((x)NULL) \n"); //$NON-NLS-1$
        sb.append("int x = Nullstr; \n"); //$NON-NLS-1$
        sb.append("int y = whatever; \n"); //$NON-NLS-1$
        String code = sb.toString();

        IASTTranslationUnit tu = parse(code, ParserLanguage.C);
        IASTDeclaration[] decls = tu.getDeclarations();
        
        assertMacroLocation(decls[0], code.indexOf("Nullstr;"), "Nullstr".length()); //$NON-NLS-1$ //$NON-NLS-2$
        assertExpressionLocation(decls[1], code.indexOf("whatever;"), "whatever".length()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    
    public void testArgumentExpansion() throws Exception {
    	StringBuffer sb = new StringBuffer();
    	sb.append("#define ADD(a,b, c) (a) + (b) + (c) \n"); //$NON-NLS-1$
    	sb.append("#define ONEYONENOE 111111   \n"); //$NON-NLS-1$
    	sb.append("#define TWO 2 \n"); //$NON-NLS-1$
    	sb.append("#define THREE 3 \n"); //$NON-NLS-1$
    	sb.append("int x = ADD(ONEYONENOE,TWO,  THREE); \n"); //$NON-NLS-1$
    	sb.append("int y = whatever; \n"); //$NON-NLS-1$
    	String code = sb.toString();
    	
    	IASTTranslationUnit tu = parse(code, ParserLanguage.C);
        IASTDeclaration[] decls = tu.getDeclarations();
        
        assertMacroLocation(decls[0], code.indexOf("ADD(ONEYONENOE,TWO,  THREE)"), "ADD(ONEYONENOE,TWO,  THREE)".length()); //$NON-NLS-1$ //$NON-NLS-2$
        assertExpressionLocation(decls[1], code.indexOf("whatever;"), "whatever".length()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    
    public void testArgumentCapture() throws Exception {
    	StringBuffer sb = new StringBuffer();
    	sb.append("#define add(x,y) x + y \n"); //$NON-NLS-1$
    	sb.append("#define add2 add(x,   \n"); //$NON-NLS-1$
    	sb.append("int x = add2 z); \n"); //$NON-NLS-1$
    	sb.append("int y = whatever; \n"); //$NON-NLS-1$
    	String code = sb.toString();
    	
    	IASTTranslationUnit tu = parse(code, ParserLanguage.C);
        IASTDeclaration[] decls = tu.getDeclarations();
        
        assertMacroLocation(decls[0], code.indexOf("add2 z);"), "add2 z)".length()); //$NON-NLS-1$ //$NON-NLS-2$
        assertExpressionLocation(decls[1], code.indexOf("whatever;"), "whatever".length()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    
    public void testFunctionMacroNotCalled() throws Exception {
    	StringBuffer sb = new StringBuffer();
    	sb.append("#define FUNCTION(x) x \n"); //$NON-NLS-1$
    	sb.append("#define YO FUNCTION \n"); //$NON-NLS-1$
    	sb.append("int x = YO; \n"); //$NON-NLS-1$
    	sb.append("int y = whatever; \n"); //$NON-NLS-1$
    	String code = sb.toString();
    	
    	IASTTranslationUnit tu = parse(code, ParserLanguage.C);
        IASTDeclaration[] decls = tu.getDeclarations();
        
        assertMacroLocation(decls[0], code.indexOf("YO;"), "YO".length()); //$NON-NLS-1$ //$NON-NLS-2$
        assertExpressionLocation(decls[1], code.indexOf("whatever;"), "whatever".length()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public void testBuildFunctionMacroName() throws Exception {
    	StringBuffer sb = new StringBuffer();
    	sb.append("#define FUN1(x) x \n"); //$NON-NLS-1$
    	sb.append("#define FUN1(x) x \n"); //$NON-NLS-1$
    	sb.append("#define MAKEFUN(num) FUN ## num \n"); //$NON-NLS-1$
    	sb.append("int x = MAKEFUN(1)(z); \n"); //$NON-NLS-1$
    	sb.append("int y = whatever; \n"); //$NON-NLS-1$
    	String code = sb.toString();
    	
    	IASTTranslationUnit tu = parse(code, ParserLanguage.C);
        IASTDeclaration[] decls = tu.getDeclarations();
        
        assertMacroLocation(decls[0], code.indexOf("MAKEFUN(1)(z);"), "MAKEFUN(1)(z)".length()); //$NON-NLS-1$ //$NON-NLS-2$
        assertExpressionLocation(decls[1], code.indexOf("whatever;"), "whatever".length()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
}
