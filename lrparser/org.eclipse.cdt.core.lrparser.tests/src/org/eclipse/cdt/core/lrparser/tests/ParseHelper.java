/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Utility methods for parsing test code using the C99 LPG parser.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings({"restriction", "nls"})
public class ParseHelper {
	
	static int testsRun = 0;
	
	
	
	static protected class NameResolver extends ASTVisitor {
		{
			shouldVisitNames = true;
		}
		
		public List<IASTName> nameList = new ArrayList<IASTName>();
		public List<String> problemBindings = new ArrayList<String>();
		public int numNullBindings = 0;
		
		
		@Override
		public int visit(IASTName name) {
			nameList.add(name);
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				problemBindings.add(name.toString());
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}
		
		public IASTName getName(int idx) {
			if(idx < 0 || idx >= nameList.size())
				return null;
			return nameList.get(idx);
		}
		
		public int size() { 
			return nameList.size(); 
		}
	}
	
	

	public static IASTTranslationUnit parse(char[] code, ILanguage lang, boolean expectNoProblems, boolean checkBindings, int expectedProblemBindings) {
		CodeReader codeReader = new CodeReader(code);
		return parse(codeReader, lang, new ScannerInfo(), null, expectNoProblems, checkBindings, expectedProblemBindings, null);
	}
	
	public static IASTTranslationUnit parse(String code, ILanguage lang, boolean expectNoProblems, boolean checkBindings, int expectedProblemBindings) {
		return parse(code.toCharArray(), lang, expectNoProblems, checkBindings, expectedProblemBindings);
	}
	
	public static IASTTranslationUnit parse(String code, ILanguage lang, boolean expectNoProblems) {
		return parse(code, lang, expectNoProblems, false, 0);
	}

	public static IASTTranslationUnit parse(String code, ILanguage lang, String[] problems) {
		CodeReader codeReader = new CodeReader(code.toCharArray());
    	return parse(codeReader, lang, new ScannerInfo(), null, true, true, problems.length, problems);
	}

	
	public static IASTTranslationUnit parse(CodeReader codeReader, ILanguage language, IScannerInfo scanInfo, 
			                                ICodeReaderFactory fileCreator, boolean expectNoProblems, 
			                                boolean checkBindings, int expectedProblemBindings, String[] problems) {
		testsRun++;
		
		IASTTranslationUnit tu;
		try {
			tu = language.getASTTranslationUnit(codeReader, scanInfo, fileCreator, null, ParserUtil.getParserLogService());
		} catch (CoreException e) {
			throw new AssertionFailedError(e.toString());
		}

		// should parse correctly first before we look at the bindings
        if(expectNoProblems) {
        	
        	// this should work for C++ also, CVisitor.getProblems() and CPPVisitor.getProblems() are exactly the same code!
			if (CVisitor.getProblems(tu).length != 0) { 
				throw new AssertionFailedError(" CVisitor has AST Problems " ); 
			}
			if (tu.getPreprocessorProblems().length != 0) {
				throw new AssertionFailedError(" C TranslationUnit has Preprocessor Problems " );
			}
        }

        // resolve all bindings
		if (checkBindings) {
			NameResolver res = new NameResolver();
	        tu.accept( res );
			if(res.problemBindings.size() != expectedProblemBindings)
				throw new AssertionFailedError("Expected " + expectedProblemBindings + " problem(s), encountered " + res.problemBindings.size());
			
			if(problems != null) {
				for(int i = 0; i < problems.length; i++) {
					String expected = problems[i];
					String actual = res.problemBindings.get(i);
					if(!expected.equals(actual))
						throw new AssertionFailedError(String.format("Problem binding not equal, expected: %s, got: %s", expected, actual));
				}
			}
		}
		
		return tu;
	}

	
	public static IASTTranslationUnit commentParse(String code, ILanguage language) {
		CodeReader codeReader = new CodeReader(code.toCharArray());
		IASTTranslationUnit tu;
		try {
			tu = language.getASTTranslationUnit(codeReader, new ScannerInfo(), null, null, ILanguage.OPTION_ADD_COMMENTS, ParserUtil.getParserLogService());
		} catch (CoreException e) {
			throw new AssertionFailedError(e.toString());
		}
		return tu;
	}
	
	public static IASTCompletionNode getCompletionNode(String code, ILanguage lang) {
		return getCompletionNode(code, lang, code.length());
	}
	
	
	public static IASTCompletionNode getCompletionNode(String code, ILanguage language, int offset) {
		CodeReader reader = new CodeReader(code.toCharArray());
		try {
			return language.getCompletionNode(reader, new ScannerInfo(), null, null, ParserUtil.getParserLogService(), offset);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

}
