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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c99.ASTPrinter;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99LexerFactory;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parser;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99SourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.C99Preprocessor;

/**
 * Utility methods for parsing test code using the C99 LPG parser.
 * 
 * @author Mike Kucera
 */
public class ParseHelper {
	
	static int testsRun = 0;
	
	private static class C99NameResolver extends CASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings=0;
		public int numNullBindings=0;
		
		public int visit( IASTName name ){
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}
	}
	
	
	public static IASTTranslationUnit parse(String code, ParserLanguage lang, boolean expectNoProblems, boolean checkBindings, int expectedProblemBindings) {
		CodeReader codeReader = new CodeReader(code.toCharArray());
		return parse(codeReader, lang, expectNoProblems, checkBindings, expectedProblemBindings);
	}
	
	
	public static IASTTranslationUnit parse(String code, ParserLanguage lang, boolean expectNoProblems) {
		return parse(code, lang, expectNoProblems, false, 0);
	}
	
	
	public static IASTTranslationUnit parse(CodeReader codeReader, ParserLanguage lang, boolean expectNoProblems, boolean checkBindings, int expectedProblemBindings) {
		testsRun++;
		
		if(lang != ParserLanguage.C) {
			System.err.println("Warning, parsing of C++ not supported yet, will parse file as C99");
		}
		

		C99SourceCodeParser parser = new C99SourceCodeParser();
		IASTTranslationUnit tu = parser.parse(codeReader, null, null, null);

		// resolve all bindings
		if (checkBindings) {

			C99NameResolver res = new C99NameResolver();
	        tu.accept( res );
			if (res.numProblemBindings != expectedProblemBindings )
				throw new AssertionFailedError("Expected " + expectedProblemBindings + " problems, encountered " + res.numProblemBindings ); //$NON-NLS-1$ //$NON-NLS-2$
			
		}

        if(parser.encounteredError() && expectNoProblems )
        	throw new AssertionFailedError("Parse Error"); //$NON-NLS-1$
         
        if(expectNoProblems )
        {
			if (CVisitor.getProblems(tu).length != 0) {
				throw new AssertionFailedError(" CVisitor has AST Problems " ); //$NON-NLS-1$
			}
			
			// TODO: actually collect preprocessor problems
			if (tu.getPreprocessorProblems().length != 0) {
				throw new AssertionFailedError(" C TranslationUnit has Preprocessor Problems " ); //$NON-NLS-1$
			}
        }

		
		return tu;
	}

}
