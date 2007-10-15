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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.c99.IKeywordMap;
import org.eclipse.cdt.core.dom.c99.ILexer;
import org.eclipse.cdt.core.dom.c99.ILexerFactory;
import org.eclipse.cdt.core.dom.c99.IPreprocessorTokenCollector;
import org.eclipse.cdt.core.dom.parser.c99.C99KeywordMap;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99ExprEvaluator;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99LexerFactory;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99PPTokenComparator;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parserprs;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.C99Preprocessor;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.LocationResolver;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.ObjectTagger;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.SynthesizedToken;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.Token;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.TokenList;

public class C99PreprocessorTests extends TestCase {

	// TODO: assert that no problems are generated 
	 
	public C99PreprocessorTests() { }
	public C99PreprocessorTests(String name) { super(name); }

	
	private List<IToken> scanAndPreprocess(String input) {
		return scanAndPreprocess(input, null);
	}
	
	private static class TokenCollector implements IPreprocessorTokenCollector<IToken> {
		public List<IToken> tokens = new ArrayList<IToken>();
		private static IKeywordMap keywordMap = new C99KeywordMap();
		
		public void addToken(IToken token) {
			if(token.getKind() == C99Parserprs.TK_identifier) {
				Integer keywordKind = keywordMap.getKeywordKind(token.toString());
				if(keywordKind != null) {
					token.setKind(keywordKind.intValue());
				}
			}
			
			tokens.add(token);
		}
		
		public void addCommentToken(IToken token) {
			// don't care
		}
	}
	
	
	private List<IToken> scanAndPreprocess(String input, ICodeReaderFactory fileCreator) {
		CodeReader reader = new CodeReader(input.toCharArray());
		
		ILexerFactory lexerFactory = new C99LexerFactory();
		
		IScannerInfo scanInfo = null;
		C99Preprocessor<IToken> preprocessor = new C99Preprocessor<IToken>(lexerFactory, new C99PPTokenComparator(), reader, scanInfo, fileCreator, 0);
		
		TokenCollector tokenCollector = new TokenCollector();
		// the preprocessor injects tokens into the parser
		preprocessor.preprocess(tokenCollector, new LocationResolver(), null);
		
		return tokenCollector.tokens;
	}
	
	
	private void assertExpressionValue(long val, String expr) {
		TokenList tokens = scan(expr);
		if(tokens == null)
			fail("Lexer failed on input: " + expr);//$NON-NLS-1$
		
		C99ExprEvaluator evaluator = new C99ExprEvaluator(tokens, new C99PPTokenComparator());
		Long value = evaluator.evaluate();
		
		if(value == null)
			fail("evaluation of expression failed");//$NON-NLS-1$
		
		assertEquals(val, value.longValue());
	}
	
	
	
	private void assertInvalidToken(String expr) {
		TokenList tokens = scan(expr);
		if(tokens == null)
			fail("Lexer failed on input: " + expr);//$NON-NLS-1$
		
		for(Iterator iter = tokens.iterator(); iter.hasNext();) {
			IToken token = (IToken)iter.next();
			if(token.getKind() == C99Parsersym.TK_Invalid)
				return;
		}
		fail("did not find an invalid token");//$NON-NLS-1$
	}
	
	
	private TokenList scan(String input) {
		CodeReader reader = new CodeReader(input.toCharArray());
		ILexerFactory lexerFactory = new C99LexerFactory();
		ILexer lexer = lexerFactory.createLexer(reader);
		return lexer.lex(0);
	}
	
	
	private static void assertToken(int kind, IToken t) {
		assertEquals(kind, t.getKind());
	}
	
	
	public static void assertIdentifier(String ident, IToken t) {
		assertToken(C99Parsersym.TK_identifier, t);
		assertEquals(ident, t.toString());
	}
	
	public static void assertInteger(String ident, IToken t) {
		assertToken(C99Parsersym.TK_integer, t);
		assertEquals(ident, t.toString());
	}
	
	
	public void testSimpleObjectLike1() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define YO 5 \n");//$NON-NLS-1$
		sb.append("# define MAMA 10 \n");//$NON-NLS-1$
		sb.append("int x = YO + MAMA; \n");//$NON-NLS-1$
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(7, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_int,        iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_Assign,     iter.next());
		assertToken(C99Parsersym.TK_integer,    iter.next());
		assertToken(C99Parsersym.TK_Plus,       iter.next());
		assertToken(C99Parsersym.TK_integer,    iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());
	}
	
	public void testSimpleObjectLike2() {
		// test rescan
		StringBuffer sb = new StringBuffer();
		sb.append("#define YO MAMA \n");//$NON-NLS-1$
		sb.append("# define MAMA 10 \n");//$NON-NLS-1$
		sb.append("int x = MAMA; \n");//$NON-NLS-1$
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(5, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_int,        iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_Assign,     iter.next());
		assertToken(C99Parsersym.TK_integer,    iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());
		
		String replaced = tokens.get(3).toString();
		assertEquals("10", replaced);//$NON-NLS-1$
	}

	
	public void testSimpleFunctionLike1() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define MAX(a, b) (a) > (b) ? (a) : (b) \n");//$NON-NLS-1$
		sb.append("int max = MAX(x, y); \n");//$NON-NLS-1$
		
		// int max = (x) > (y) ? (x) : (y);
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_int,        iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_Assign,     iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("x", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_GT,         iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("y", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Question,   iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("x", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Colon,      iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("y", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());
	}
	
	
	public void testSimpleFunctionLike2() {
		// test rescan
		StringBuffer sb = new StringBuffer();
		sb.append("#define ADD(a, b) (a) + (b) \n");//$NON-NLS-1$
		sb.append("#define ADDPART(a) ADD(a \n");//$NON-NLS-1$
		sb.append("int sum = ADDPART (x) , y); ");//$NON-NLS-1$
		
		// int sum = (x) + (y) ;
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(11, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_int,        iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_Assign,     iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("x", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Plus,       iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("y", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());
	}
	
	
	public void testSimpleFunctionLike3() {
		// test longer args
		StringBuffer sb = new StringBuffer();
		sb.append("#define ADD(a, b) (a) + (b) \n");//$NON-NLS-1$
		sb.append("int sum = ADD(x+1,y+1); ");//$NON-NLS-1$
		
		// int sum = (x+1) + (y+1) ;
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(15, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_int,        iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_Assign,     iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("x", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Plus,       iter.next());
		assertToken(C99Parsersym.TK_integer,    iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Plus,       iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("y", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Plus,       iter.next());
		assertToken(C99Parsersym.TK_integer,    iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());
	}
	
	
	public void testSimpleFunctionLike4() {
		// parenthesis
		StringBuffer sb = new StringBuffer();
		sb.append("#define ADD(a, b) (a) + (b) \n");//$NON-NLS-1$
		sb.append("int sum = ADD(f(x,y),z+1); ");//$NON-NLS-1$
		
		// int sum = (f(x,y)) + (z+1) ;
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(18, tokens.size());

		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_int,        iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_Assign,     iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("f", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("x", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma,      iter.next());
		assertIdentifier("y", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Plus,       iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("z", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Plus,       iter.next());
		assertToken(C99Parsersym.TK_integer,    iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());
	}
	
	
	public void testSpecHashHashExample() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define hash_hash # ## # \n ");//$NON-NLS-1$
		sb.append("#define mkstr(a) # a\n ");//$NON-NLS-1$
		sb.append("#define in_between(a) mkstr(a)\n ");//$NON-NLS-1$
		sb.append("#define join(c, d) in_between(c hash_hash d)\n ");//$NON-NLS-1$
		sb.append("char p[] = join(x, y); \n ");//$NON-NLS-1$
		
		// char p[] = "x ## y" ;
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(7, tokens.size());
		
		assertEquals("\"x ## y\"", tokens.get(5).toString());//$NON-NLS-1$
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_char,         iter.next());
		assertToken(C99Parsersym.TK_identifier,   iter.next());
		assertToken(C99Parsersym.TK_LeftBracket,  iter.next());
		assertToken(C99Parsersym.TK_RightBracket, iter.next());
		assertToken(C99Parsersym.TK_Assign,       iter.next());
		assertToken(C99Parsersym.TK_stringlit,    iter.next());
		assertToken(C99Parsersym.TK_SemiColon,    iter.next());
	}
	
	
	public void testSpecExample2() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define VERSION 2 \n");//$NON-NLS-1$
		sb.append("#if VERSION == 1 \n");//$NON-NLS-1$
		sb.append("#define INCFILE \"vers1.h\" \n");//$NON-NLS-1$
		sb.append("#elif VERSION == 2 \n");//$NON-NLS-1$
		sb.append("#define INCFILE \"vers2.h\" \n");//$NON-NLS-1$
		sb.append("#else \n");//$NON-NLS-1$
		sb.append("#define INCFILE \"versN.h\" \n");//$NON-NLS-1$
		sb.append("#endif \n");//$NON-NLS-1$
		sb.append("#include INCFILE \n");//$NON-NLS-1$
		
		List tokens = scanAndPreprocess(sb.toString());
		
		// should be empty
		assertEquals(0, tokens.size());
	}
	
	
	private static StringBuffer getExample3Defines() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define x 3 \n");//$NON-NLS-1$
		sb.append("#define f(a) f(x * (a)) \n");//$NON-NLS-1$
		sb.append("#undef x \n");//$NON-NLS-1$
		sb.append("#define x 2 \n");//$NON-NLS-1$
		sb.append("#define g f \n");//$NON-NLS-1$
		sb.append("#define z z[0] \n");//$NON-NLS-1$
		sb.append("#define h g(~ \n");//$NON-NLS-1$
		sb.append("#define m(a) a(w) \n");//$NON-NLS-1$
		sb.append("#define w 0,1 \n");//$NON-NLS-1$
		sb.append("#define t(a) a \n");//$NON-NLS-1$
		sb.append("#define p() int \n");//$NON-NLS-1$
		sb.append("#define q(x) x \n");//$NON-NLS-1$
		sb.append("#define r(x,y) x ## y \n");//$NON-NLS-1$
		sb.append("#define str(x) # x \n");//$NON-NLS-1$
		return sb;
		
		//sb.append("f(y+1) + f(f(z)) % t(t(g)(0) + t)(1); \n");
		//sb.append("g(x+(3,4)-w) | h 5) & m (f)^m(m); \n");
		//sb.append("p() i[q()] = { q(1), r(2,3), r(4,), r(,5), r(,) }; \n");
		//sb.append("char c[2][6] = { str(hello), str() }; \n");
		
		// results in:
		// f(2 * (y+1)) + f(2 * (f(2 * (z[0])))) % f(2 * (0)) + t(1);
		// f(2 * (2+(3,4)-0,1)) | f(2 * (~ 5)) & f(2 * (0,1))^m(0,1);
		// int i[] = { 1, 23, 4, 5, };
		// char c[2][6] = { "hello", "" };
	}
	
	

	
	public void testSpecExample3_1() {
		StringBuffer sb = getExample3Defines();
		sb.append("f(y+1) + f(f(z)) % t(t(g)(0) + t)(1); \n"); //$NON-NLS-1$ //31
		
		//sb.append("z");
		
		// f(2 * (y+1)) + f(2 * (f(2 * (z[0])))) % f(2 * (0)) + t(1); //44
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(44, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertIdentifier("f", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Star, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertIdentifier("y", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Plus, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Plus, iter.next());
		assertIdentifier("f", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Star, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertIdentifier("f", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Star, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertIdentifier("z", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftBracket, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightBracket, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Percent, iter.next());
		assertIdentifier("f", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Star, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Plus, iter.next());
		assertIdentifier("t", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon, iter.next());
	}
	
	
	public void _testSpecExample3_2() {
		StringBuffer sb = getExample3Defines();
		sb.append("g(x+(3,4)-w) | h 5) & m (f)^m(m); \n");//$NON-NLS-1$
		
		// f(2 * (2+(3,4)-0,1)) | f(2 * (~ 5)) & f(2 * (0,1))^m(0,1); //47
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(47, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Star, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Plus, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Minus, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Or, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Star, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_Tilde, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_And, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Star, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Caret, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon, iter.next());
	}
	
	
	public void testSpecExample3_3() {
		
		StringBuffer sb = getExample3Defines();
		sb.append("p() i[q()] = { q(1), r(2,3), r(4,), r(,5), r(,) }; \n");//$NON-NLS-1$
		
		// int i[] = { 1, 23, 4, 5, };
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(16, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_int, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftBracket, iter.next());
		assertToken(C99Parsersym.TK_RightBracket, iter.next());
		assertToken(C99Parsersym.TK_Assign, iter.next());
		assertToken(C99Parsersym.TK_LeftBrace, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_RightBrace, iter.next());
		assertToken(C99Parsersym.TK_SemiColon, iter.next());	
	}
	
	
	public void testSpecExample3_4() {
		StringBuffer sb = getExample3Defines();
		sb.append("char c[2][6] = { str(hello), str() }; \n"); //$NON-NLS-1$  //31
		
		// char c[2][6] = { "hello", "" }; //15
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(15, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();

		assertToken(C99Parsersym.TK_char, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftBracket, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightBracket, iter.next());
		assertToken(C99Parsersym.TK_LeftBracket, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightBracket, iter.next());
		assertToken(C99Parsersym.TK_Assign, iter.next());
		assertToken(C99Parsersym.TK_LeftBrace, iter.next());
		assertToken(C99Parsersym.TK_stringlit, iter.next());
		//TODO assertEquals("\"hello\"", tokens.get(11).toString());
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_stringlit, iter.next());
		//assertEquals("\"\"", tokens.get(13).toString());
		assertToken(C99Parsersym.TK_RightBrace, iter.next());
		assertToken(C99Parsersym.TK_SemiColon, iter.next());
	}
	
	
	private static StringBuffer getExample4Defines() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define str(s) # s \n");//$NON-NLS-1$
		sb.append("#define xstr(s) str(s) \n");//$NON-NLS-1$
		sb.append("#define debug(s, t) printf(\"x\" # s \"= %d, x\" # t \"= %s\", \\ \n");//$NON-NLS-1$
		sb.append("x ## s, x ## t) \n");//$NON-NLS-1$
		sb.append("#define INCFILE(n) vers ## n \n");//$NON-NLS-1$
		sb.append("#define glue(a, b) a ## b \n");//$NON-NLS-1$
		sb.append("#define xglue(a, b) glue(a, b) \n");//$NON-NLS-1$
		sb.append("#define HIGHLOW \"hello\" \n");//$NON-NLS-1$
		sb.append("#define LOW LOW \", world\" \n");//$NON-NLS-1$
		return sb;
		
//		sb.append("debug(1, 2); \n");
//		sb.append("fputs(str(strncmp(\"abc\0d\", \"abc\", '\\4') // this goes away   \n");
//		sb.append("== 0) str(: @\n), s); \n");
//		sb.append("glue(HIGH, LOW); \n");
//		sb.append("xglue(HIGH, LOW) \n");
	}
	
	
	public void testSpecExample4_1() {
		StringBuffer sb = getExample4Defines();
		sb.append("debug(1, 2); \n"); //$NON-NLS-1$ //31
		
		// printf("x1= %d, x2= %s", x1, x2); // 9
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(9, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertToken(C99Parsersym.TK_stringlit,  iter.next());
		assertToken(C99Parsersym.TK_Comma,      iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_Comma,      iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());
	}
	
	
	public void testSpecExample4_2() {
		StringBuffer sb = getExample4Defines();
		sb.append("fputs(str(strncmp(\"abc\\0d\", \"abc\", '\\4') // this goes away   \n");//$NON-NLS-1$
		sb.append("== 0) str(: @\\n), s); \n");//$NON-NLS-1$
		
		// fputs( "strncmp(\"abc\\0d\", \"abc\", '\\4') == 0: @\n", s); // 7
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(7, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertToken(C99Parsersym.TK_stringlit,  iter.next());
		assertToken(C99Parsersym.TK_Comma,      iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());
	}
	
	
	public void testSpecExample4_3() {
		StringBuffer sb = getExample4Defines();
		sb.append("xglue(HIGH, LOW) \n");//$NON-NLS-1$
		
		// "hello, world"
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(1, tokens.size());
		
		assertToken(C99Parsersym.TK_stringlit, tokens.get(0));
		// TODO implement string concatenation properly
		//assertEquals("\"hello, world\"", tokens.get(1).toString());//$NON-NLS-1$
	}
	
	public void testSpecExample4_4() {
		StringBuffer sb = getExample4Defines();
		sb.append("glue(HIGH, LOW); \n");//$NON-NLS-1$
		
		// "hello";
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(2, tokens.size());

		assertToken(C99Parsersym.TK_stringlit, tokens.get(0));
		assertEquals("\"hello\"", tokens.get(0).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_SemiColon, tokens.get(1));
	}
	
	
	public void testSpecExample5() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define t(x,y,z) x ## y ## z \n");//$NON-NLS-1$
		sb.append("int j[] = { t(1,2,3), t(,4,5), t(6,,7), t(8,9,), " +//$NON-NLS-1$
				  "t(10,,), t(,11,), t(,,12), t(,,) }; \n");//$NON-NLS-1$
		
		// results in
		// int j[] = {123, 45, 67, 89, 10, 11, 12, };
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());

		assertNotNull(tokens);
		assertEquals(22, tokens.size());

		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_int, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftBracket, iter.next());
		assertToken(C99Parsersym.TK_RightBracket, iter.next());
		assertToken(C99Parsersym.TK_Assign, iter.next());
		assertToken(C99Parsersym.TK_LeftBrace, iter.next());
		assertInteger("123", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertInteger("45", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertInteger("67", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertInteger("89", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertInteger("10", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertInteger("11", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertInteger("12", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_RightBrace, iter.next());
		assertToken(C99Parsersym.TK_SemiColon, iter.next());
	}
	
	
	public StringBuffer getExample7Defines() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define debug(...) fprintf(stderr, __VA_ARGS__) \n ");//$NON-NLS-1$
		sb.append("#define showlist(...) puts(#__VA_ARGS__)\n ");//$NON-NLS-1$
		sb.append("#define report(test, ...) ((test)?puts(#test):\\ \n ");//$NON-NLS-1$
		sb.append("printf(__VA_ARGS__))  \n ");//$NON-NLS-1$
		return sb;
	}
	
	
	public void testSpecExample7_1() {
		StringBuffer sb = getExample7Defines();
		sb.append("debug(\"Flag\"); \n");//$NON-NLS-1$
		// fprintf(stderr, "Flag" ); //7
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(7, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_Comma,      iter.next());
		assertToken(C99Parsersym.TK_stringlit,  iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());		
	}
	
	
	public void testSpecExample7_2() {
		StringBuffer sb = getExample7Defines();
		sb.append("debug(\"X = %d\\n\", x); \n");//$NON-NLS-1$
		// fprintf(stderr, "X = %d\n", x ); //9
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(9, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_Comma,      iter.next());
		assertToken(C99Parsersym.TK_stringlit,  iter.next());
		assertToken(C99Parsersym.TK_Comma,      iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());		
	}
	
	
	public void testSpecExample7_3() {
		StringBuffer sb = getExample7Defines();
		sb.append("showlist(The first, second, and third items.); \n");//$NON-NLS-1$
		// puts( "The first, second, and third items." ); //5
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(5, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertToken(C99Parsersym.TK_stringlit,  iter.next());
		// TODO: assertEquals("\"The first, second, and third items.\"", tokens.get(3).toString());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());
	}
	
	
	
	public void testSpecExample7_4() {
		StringBuffer sb = getExample7Defines();
		sb.append("report(x>y, \"x is %d but y is %d\", x, y); \n");//$NON-NLS-1$
		// ( (x>y) ? puts("x>y") : printf("x is %d but y is %d", x, y) ); //22
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(22, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_GT,         iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Question,   iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertToken(C99Parsersym.TK_stringlit,  iter.next());
		// TODO : assertEquals("\"x>y\"", tokens.get(10).toString());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Colon,      iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertToken(C99Parsersym.TK_stringlit,  iter.next());
		assertToken(C99Parsersym.TK_Comma,      iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_Comma,      iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());
	}
	
	
	public void testConditionalExpressions() {
		// test hex and octal
		assertExpressionValue(0x1234, " 0x1234 ");//$NON-NLS-1$
		assertExpressionValue(0x99,  "0x99");//$NON-NLS-1$
		assertExpressionValue(0xAB + 0xFE, "0xABu + 0xFEUll");//$NON-NLS-1$
		assertExpressionValue(020, "020");//$NON-NLS-1$
		assertExpressionValue(077 + 0x22, " 077 + 0x22 ");//$NON-NLS-1$
		assertExpressionValue(145, "145");//$NON-NLS-1$
		
		// test char constants
		assertExpressionValue(97, " 'a' ");//$NON-NLS-1$
		assertExpressionValue(97, " L'a' ");//$NON-NLS-1$
		assertExpressionValue(24930, " 'ab' ");//$NON-NLS-1$
		assertExpressionValue(6379864, " '\\x61YX' ");//$NON-NLS-1$
		assertExpressionValue(2567, " '\\n\\a' ");//$NON-NLS-1$
		assertExpressionValue(4660, " '\\u1234' ");//$NON-NLS-1$
		assertExpressionValue(305419896, " '\\u12345678' ");//$NON-NLS-1$
		
		// test that invalid escape sequences are actually caught
		// by the scanner
		assertInvalidToken(" '\\x' ");//$NON-NLS-1$
		assertInvalidToken(" '\\u' ");//$NON-NLS-1$
		assertInvalidToken(" '\\u123' ");//$NON-NLS-1$ // not a hex quad
		assertInvalidToken(" '\\q' ");//$NON-NLS-1$ // invalid escape sequence
	}
	
	
	public void testBug186047() {
		StringBuffer sb = getExample7Defines();
		sb.append("#define D \n");//$NON-NLS-1$
		sb.append("#if defined D \n");//$NON-NLS-1$
		sb.append("    x; \n");//$NON-NLS-1$
		sb.append("#endif \n");//$NON-NLS-1$
		sb.append("#if defined(D) \n");//$NON-NLS-1$
		sb.append("    y; \n");//$NON-NLS-1$
		sb.append("#endif \n");//$NON-NLS-1$
			
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(4, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertIdentifier("x", iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());
		assertIdentifier("y", iter.next());
		assertToken(C99Parsersym.TK_SemiColon,  iter.next());
	}
	
	
	public void testTokenEqualsHashcode() {
		char[] chars = "one two three".toCharArray();//$NON-NLS-1$
		
		Token[] source = {
			new Token(0, 2, 0, chars),
			new Token(4, 6, 0, chars),
			new Token(8, 12, 0, chars) 
		};
		
		assertEquals("one", source[0].toString());//$NON-NLS-1$
		assertEquals("two", source[1].toString());//$NON-NLS-1$
		assertEquals("three", source[2].toString());//$NON-NLS-1$
		
		Token[] synth = {
			new SynthesizedToken(0, 2, 0, "one"),//$NON-NLS-1$
			new SynthesizedToken(4, 6, 0, "two"),//$NON-NLS-1$
			new SynthesizedToken(8, 12, 0, "three")//$NON-NLS-1$
		};
		
		assertEquals("one", synth[0].toString());//$NON-NLS-1$
		assertEquals("two", synth[1].toString());//$NON-NLS-1$
		assertEquals("three", synth[2].toString());//$NON-NLS-1$
		
		for(int i = 0; i < 3; i++) {
			assertTrue(source[i].equals(synth[i]));
			assertTrue(synth[i].equals(source[i]));
			assertEquals(source[2].hashCode(), synth[2].hashCode());
		}
	}
	
	
	public void _testRecursiveExpansion() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define foo g g g \n");//$NON-NLS-1$
		sb.append("#define g f##oo \n");//$NON-NLS-1$
		sb.append("foo \n");//$NON-NLS-1$
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(3, tokens.size());
		
		assertIdentifier("foo", tokens.get(0));//$NON-NLS-1$
		assertIdentifier("foo", tokens.get(1));//$NON-NLS-1$
		assertIdentifier("foo", tokens.get(2));//$NON-NLS-1$
	}
	
	
	public void testRecursiveExpansion2() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define m !(m)+n \n");//$NON-NLS-1$
		sb.append("#define n(n) n(m) \n");//$NON-NLS-1$
		sb.append("m(m)\n"); //$NON-NLS-1$
		
		// !(m)+ !(m)+n(!(m)+n)
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		
		Iterator<IToken> iter = tokens.iterator();
		
		assertToken(C99Parsersym.TK_Bang,       iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("m", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Plus,       iter.next());
		assertToken(C99Parsersym.TK_Bang,       iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("m", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Plus,       iter.next());
		assertIdentifier("n", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertToken(C99Parsersym.TK_Bang,       iter.next());
		assertToken(C99Parsersym.TK_LeftParen,  iter.next());
		assertIdentifier("m", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Plus,       iter.next());
		assertIdentifier("n", iter.next());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_RightParen, iter.next());
	}
	
	
	public void testRecursiveExpansion3() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define f g\n");//$NON-NLS-1$
		sb.append("#define cat(a,b) a ## b\n");//$NON-NLS-1$
		sb.append("#define g bad\n");//$NON-NLS-1$
		sb.append("cat(f, f)\n");//$NON-NLS-1$
		
		 // ff
		
		List<IToken> tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(1, tokens.size());
		
		assertIdentifier("ff", tokens.get(0));//$NON-NLS-1$
	}
	
	
	public void testObjectTagger() {
		ObjectTagger<Integer,String> tagger = new ObjectTagger<Integer,String>();
		
		Integer obj1 = new Integer(10);
		Integer obj2 = new Integer(10);
		
		// according to equals() and hashCode() in the integer class these objects should be equal
		assertTrue(obj1.equals(obj2));
		assertEquals(obj1.hashCode(), obj2.hashCode());
		// but they are not the same object
		assertFalse(obj1 == obj2);
		
		final String TAG_1A = "tag1a", TAG_1B = "tag1b";
		final String TAG_2A = "tag2a", TAG_2B = "tag2b";
		
		// now tag the objects, it should use object equality and not the overridden equals() method
		tagger.tag(obj1, TAG_1A);
		tagger.tag(obj2, TAG_2A);
		
		assertTrue(tagger.hasTag(obj1, TAG_1A));
		assertFalse(tagger.hasTag(obj1, TAG_2A));
		assertTrue(tagger.hasTag(obj2, TAG_2A));
		assertFalse(tagger.hasTag(obj2, TAG_1A));
		
		// add some more tags
		tagger.tag(obj1, TAG_1B);
		tagger.tag(obj2, TAG_2B);
		
		assertTrue(tagger.hasTag(obj1, TAG_1A));
		assertFalse(tagger.hasTag(obj1, TAG_2A));
		assertTrue(tagger.hasTag(obj2, TAG_2A));
		assertFalse(tagger.hasTag(obj2, TAG_1A));
		
		assertTrue(tagger.hasTag(obj1, TAG_1B));
		assertFalse(tagger.hasTag(obj1, TAG_2B));
		assertTrue(tagger.hasTag(obj2, TAG_2B));
		assertFalse(tagger.hasTag(obj2, TAG_1B));
		
		tagger.removeAllTags(obj2); 
		
		assertTrue(tagger.hasTag(obj1, TAG_1A));
		assertFalse(tagger.hasTag(obj1, TAG_2A));
		assertFalse(tagger.hasTag(obj2, TAG_2A));
		assertFalse(tagger.hasTag(obj2, TAG_1A));
		
		assertTrue(tagger.hasTag(obj1, TAG_1B));
		assertFalse(tagger.hasTag(obj1, TAG_2B));
		assertFalse(tagger.hasTag(obj2, TAG_2B));
		assertFalse(tagger.hasTag(obj2, TAG_1B));
	}
}





