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

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.c99.ILexer;
import org.eclipse.cdt.core.dom.c99.ILexerFactory;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99ExprEvaluator;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99LexerFactory;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parser;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99TokenMap;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.C99Preprocessor;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.TokenList;

public class C99PreprocessorTests extends TestCase {

	// TODO: assert that no probems are generated 
	
	public C99PreprocessorTests() { }
	public C99PreprocessorTests(String name) { super(name); }

	
	private List scanAndPreprocess(String input) {
		return scanAndPreprocess(input, null);
	}
	
	private List scanAndPreprocess(String input, ICodeReaderFactory fileCreator) {
		CodeReader reader = new CodeReader(input.toCharArray());
		
		ILexerFactory lexerFactory = new C99LexerFactory();
		
		IScannerInfo scanInfo = null;
		C99Preprocessor preprocessor = new C99Preprocessor(lexerFactory, new C99TokenMap(), reader, scanInfo, fileCreator, 0);
		
		C99Parser parser = new C99Parser();
		
		// the preprocessor injects tokens into the parser
		preprocessor.preprocess(parser, null, null);
		
		return parser.getTokens();
	}
	
	
	private void assertExpressionValue(long val, String expr) {
		TokenList tokens = scan(expr);
		if(tokens == null)
			fail("Lexer failed on input: " + expr);
		
		C99ExprEvaluator evaluator = new C99ExprEvaluator(tokens, new C99TokenMap());
		Long value = evaluator.evaluate();
		
		if(value == null)
			fail("evaluation of expression failed");
		
		assertEquals(val, value.longValue());
	}
	
	
	
	private void assertInvalidToken(String expr) {
		TokenList tokens = scan(expr);
		if(tokens == null)
			fail("Lexer failed on input: " + expr);
		
		for(Iterator iter = tokens.iterator(); iter.hasNext();) {
			IToken token = (IToken)iter.next();
			if(token.getKind() == C99Parsersym.TK_Invalid)
				return;
		}
		fail("did not find an invalid token");
	}
	
	
	private TokenList scan(String input) {
		CodeReader reader = new CodeReader(input.toCharArray());
		ILexerFactory lexerFactory = new C99LexerFactory();
		ILexer lexer = lexerFactory.createLexer(reader);
		return lexer.lex(0);
	}
	
	private static void assertToken(int kind, Object t) {
		assertEquals(kind, ((IToken)t).getKind());
	}
	
	
	public void testSimpleObjectLike1() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define YO 5 \n");//$NON-NLS-1$
		sb.append("# define MAMA 10 \n");//$NON-NLS-1$
		sb.append("int x = YO + MAMA; \n");//$NON-NLS-1$
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(9, tokens.size());
		assertToken(0, tokens.get(0));
		assertToken(C99Parsersym.TK_int,        tokens.get(1));
		assertToken(C99Parsersym.TK_identifier, tokens.get(2));
		assertToken(C99Parsersym.TK_Assign,     tokens.get(3));
		assertToken(C99Parsersym.TK_integer,    tokens.get(4));
		assertToken(C99Parsersym.TK_Plus,       tokens.get(5));
		assertToken(C99Parsersym.TK_integer,    tokens.get(6));
		assertToken(C99Parsersym.TK_SemiColon,  tokens.get(7));
		assertToken(C99Parsersym.TK_EOF_TOKEN,  tokens.get(8));
	}
	
	public void testSimpleObjectLike2() {
		// test rescan
		StringBuffer sb = new StringBuffer();
		sb.append("#define YO MAMA \n");//$NON-NLS-1$
		sb.append("# define MAMA 10 \n");//$NON-NLS-1$
		sb.append("int x = MAMA; \n");//$NON-NLS-1$
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(7, tokens.size());
		assertToken(0, tokens.get(0));
		assertToken(C99Parsersym.TK_int,        tokens.get(1));
		assertToken(C99Parsersym.TK_identifier, tokens.get(2));
		assertToken(C99Parsersym.TK_Assign,     tokens.get(3));
		assertToken(C99Parsersym.TK_integer,    tokens.get(4));
		assertToken(C99Parsersym.TK_SemiColon,  tokens.get(5));
		assertToken(C99Parsersym.TK_EOF_TOKEN,  tokens.get(6));
		
		String replaced = tokens.get(4).toString();
		assertEquals("10", replaced);
	}

	
	public void testSimpleFunctionLike1() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define MAX(a, b) (a) > (b) ? (a) : (b) \n");//$NON-NLS-1$
		sb.append("int max = MAX(x, y); \n");//$NON-NLS-1$
		
		// int max = (x) > (y) ? (x) : (y);
		
		List tokens = scanAndPreprocess(sb.toString());
		System.err.println(tokens);
		
		assertNotNull(tokens);
		assertEquals(21, tokens.size());
		assertToken(0, tokens.get(0));
		
		assertToken(C99Parsersym.TK_int,        tokens.get(1));
		assertToken(C99Parsersym.TK_identifier, tokens.get(2));
		assertToken(C99Parsersym.TK_Assign,     tokens.get(3));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(4));
		assertToken(C99Parsersym.TK_identifier, tokens.get(5));
		assertEquals("x", tokens.get(5).toString());
		assertToken(C99Parsersym.TK_RightParen, tokens.get(6));
		assertToken(C99Parsersym.TK_GT,         tokens.get(7));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(8));
		assertToken(C99Parsersym.TK_identifier, tokens.get(9));
		assertEquals("y", tokens.get(9).toString());
		assertToken(C99Parsersym.TK_RightParen, tokens.get(10));
		assertToken(C99Parsersym.TK_Question,   tokens.get(11));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(12));
		assertToken(C99Parsersym.TK_identifier, tokens.get(13));
		assertEquals("x", tokens.get(13).toString());
		assertToken(C99Parsersym.TK_RightParen, tokens.get(14));
		assertToken(C99Parsersym.TK_Colon,      tokens.get(15));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(16));
		assertToken(C99Parsersym.TK_identifier, tokens.get(17));
		assertEquals("y", tokens.get(17).toString());
		assertToken(C99Parsersym.TK_RightParen, tokens.get(18));
		assertToken(C99Parsersym.TK_SemiColon,  tokens.get(19));
		assertToken(C99Parsersym.TK_EOF_TOKEN,  tokens.get(20));
	}
	
	
	public void testSimpleFunctionLike2() {
		// test rescan
		StringBuffer sb = new StringBuffer();
		sb.append("#define ADD(a, b) (a) + (b) \n");//$NON-NLS-1$
		sb.append("#define ADDPART(a) ADD(a \n");//$NON-NLS-1$
		sb.append("int sum = ADDPART (x) , y); ");//$NON-NLS-1$
		
		// int sum = (x) + (y) ;
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(13, tokens.size());
		assertToken(0, tokens.get(0));
		
		assertToken(C99Parsersym.TK_int,        tokens.get(1));
		assertToken(C99Parsersym.TK_identifier, tokens.get(2));
		assertToken(C99Parsersym.TK_Assign,     tokens.get(3));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(4));
		assertToken(C99Parsersym.TK_identifier, tokens.get(5));
		assertEquals("x", tokens.get(5).toString());
		assertToken(C99Parsersym.TK_RightParen, tokens.get(6));
		assertToken(C99Parsersym.TK_Plus,       tokens.get(7));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(8));
		assertToken(C99Parsersym.TK_identifier, tokens.get(9));
		assertEquals("y", tokens.get(9).toString());
		assertToken(C99Parsersym.TK_RightParen, tokens.get(10));
		assertToken(C99Parsersym.TK_SemiColon,  tokens.get(11));
		assertToken(C99Parsersym.TK_EOF_TOKEN,  tokens.get(12));
	}
	
	public void testSimpleFunctionLike3() {
		// test longer args
		StringBuffer sb = new StringBuffer();
		sb.append("#define ADD(a, b) (a) + (b) \n");//$NON-NLS-1$
		sb.append("int sum = ADD(x+1,y+1); ");//$NON-NLS-1$
		
		// int sum = (x+1) + (y+1) ;
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(17, tokens.size());
		assertToken(0, tokens.get(0));
		
		assertToken(C99Parsersym.TK_int,        tokens.get(1));
		assertToken(C99Parsersym.TK_identifier, tokens.get(2));
		assertToken(C99Parsersym.TK_Assign,     tokens.get(3));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(4));
		assertToken(C99Parsersym.TK_identifier, tokens.get(5));
		assertEquals("x", tokens.get(5).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Plus,       tokens.get(6));
		assertToken(C99Parsersym.TK_integer,    tokens.get(7));
		assertToken(C99Parsersym.TK_RightParen, tokens.get(8));
		assertToken(C99Parsersym.TK_Plus,       tokens.get(9));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(10));
		assertToken(C99Parsersym.TK_identifier, tokens.get(11));
		assertEquals("y", tokens.get(11).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Plus,       tokens.get(12));
		assertToken(C99Parsersym.TK_integer,    tokens.get(13));
		assertToken(C99Parsersym.TK_RightParen, tokens.get(14));
		assertToken(C99Parsersym.TK_SemiColon,  tokens.get(15));
		assertToken(C99Parsersym.TK_EOF_TOKEN,  tokens.get(16));
	}
	
	
	public void testSimpleFunctionLike4() {
		// parenthesis
		StringBuffer sb = new StringBuffer();
		sb.append("#define ADD(a, b) (a) + (b) \n");//$NON-NLS-1$
		sb.append("int sum = ADD(f(x,y),z+1); ");//$NON-NLS-1$
		
		// int sum = (f(x,y)) + (z+1) ;
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(20, tokens.size());
		assertToken(0, tokens.get(0));
		
		assertToken(C99Parsersym.TK_int,        tokens.get(1));
		assertToken(C99Parsersym.TK_identifier, tokens.get(2));
		assertToken(C99Parsersym.TK_Assign,     tokens.get(3));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(4));
		assertToken(C99Parsersym.TK_identifier, tokens.get(5));
		assertEquals("f", tokens.get(5).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(6));
		assertToken(C99Parsersym.TK_identifier, tokens.get(7));
		assertEquals("x", tokens.get(7).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma,      tokens.get(8));
		assertToken(C99Parsersym.TK_identifier, tokens.get(9));
		assertEquals("y", tokens.get(9).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_RightParen, tokens.get(10));
		assertToken(C99Parsersym.TK_RightParen, tokens.get(11));
		assertToken(C99Parsersym.TK_Plus,       tokens.get(12));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(13));
		assertToken(C99Parsersym.TK_identifier, tokens.get(14));
		assertEquals("z", tokens.get(14).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Plus,       tokens.get(15));
		assertToken(C99Parsersym.TK_integer,    tokens.get(16));
		assertToken(C99Parsersym.TK_RightParen, tokens.get(17));
		assertToken(C99Parsersym.TK_SemiColon,  tokens.get(18));
		assertToken(C99Parsersym.TK_EOF_TOKEN,  tokens.get(19));
	}
	
	
	public void testSpecHashHashExample() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define hash_hash # ## # \n ");//$NON-NLS-1$
		sb.append("#define mkstr(a) # a\n ");//$NON-NLS-1$
		sb.append("#define in_between(a) mkstr(a)\n ");//$NON-NLS-1$
		sb.append("#define join(c, d) in_between(c hash_hash d)\n ");//$NON-NLS-1$
		sb.append("char p[] = join(x, y); \n ");//$NON-NLS-1$
		
		// char p[] = "x ## y" ;
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(9, tokens.size());
		assertToken(0, tokens.get(0));
		
		assertToken(C99Parsersym.TK_char, tokens.get(1));
		assertToken(C99Parsersym.TK_identifier, tokens.get(2));
		assertToken(C99Parsersym.TK_LeftBracket, tokens.get(3));
		assertToken(C99Parsersym.TK_RightBracket, tokens.get(4));
		assertToken(C99Parsersym.TK_Assign, tokens.get(5));
		assertToken(C99Parsersym.TK_stringlit, tokens.get(6));
		assertEquals("\"x ## y\"", tokens.get(6).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_SemiColon, tokens.get(7));
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
		
		// just the dummy token and EOF
		assertEquals(2, tokens.size());
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
		
		// f(2 * (y+1)) + f(2 * (f(2 * (z[0])))) % f(2 * (0)) + t(1); //44
		
		List tokens = scanAndPreprocess(sb.toString());

		assertNotNull(tokens);
		assertEquals(46, tokens.size());
		assertToken(0, tokens.get(0));
		
		Iterator iter = tokens.iterator();
		iter.next();
		
		assertToken(C99Parsersym.TK_identifier, iter.next()); // f
		assertEquals("f", tokens.get(1).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Star, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next()); // y
		assertEquals("y", tokens.get(6).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Plus, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Plus, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next()); //f
		assertEquals("f", tokens.get(12).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Star, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next()); // f
		assertEquals("f", tokens.get(17).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Star, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next()); // z
		assertEquals("z", tokens.get(22).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftBracket, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightBracket, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Percent, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next()); // f
		assertEquals("f", tokens.get(31).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_Star, iter.next());
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_Plus, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next()); // t
		assertEquals("t", tokens.get(40).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_LeftParen, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertToken(C99Parsersym.TK_RightParen, iter.next());
		assertToken(C99Parsersym.TK_SemiColon, iter.next());
	}
	
	
	public void testSpecExample3_2() {
		StringBuffer sb = getExample3Defines();
		sb.append("g(x+(3,4)-w) | h 5) & m (f)^m(m); \n");//$NON-NLS-1$
		
		// f(2 * (2+(3,4)-0,1)) | f(2 * (~ 5)) & f(2 * (0,1))^m(0,1); //47
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(49, tokens.size());
		assertToken(0, tokens.get(0));
		
		Iterator iter = tokens.iterator();
		iter.next();
		
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
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(18, tokens.size());
		assertToken(0, tokens.get(0));
		
		
		
		Iterator iter = tokens.iterator();
		iter.next();
		
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
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(17, tokens.size());
		assertToken(0, tokens.get(0));
		
		Iterator iter = tokens.iterator();
		iter.next();
		
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
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(11, tokens.size());
		assertToken(0, tokens.get(0));
		
		Iterator iter = tokens.iterator();
		iter.next();
		
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
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(9, tokens.size());
		assertToken(0, tokens.get(0));
		
		Iterator iter = tokens.iterator();
		iter.next(); // skip the dummy token
		
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
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(3, tokens.size());
		assertToken(0, tokens.get(0));
		
		assertToken(C99Parsersym.TK_stringlit, tokens.get(1));
		assertEquals("\"hello, world\"", tokens.get(1).toString());//$NON-NLS-1$
	}
	
	public void testSpecExample4_4() {
		StringBuffer sb = getExample4Defines();
		sb.append("glue(HIGH, LOW); \n");//$NON-NLS-1$
		
		// "hello";
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(4, tokens.size());
		assertToken(0, tokens.get(0));
		
		assertToken(C99Parsersym.TK_stringlit, tokens.get(1));
		assertEquals("\"hello\"", tokens.get(1).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_SemiColon, tokens.get(2));
	}
	
	
	public void testSpecExample5() {
		StringBuffer sb = new StringBuffer();
		sb.append("#define t(x,y,z) x ## y ## z \n");//$NON-NLS-1$
		sb.append("int j[] = { t(1,2,3), t(,4,5), t(6,,7), t(8,9,), " +//$NON-NLS-1$
				  "t(10,,), t(,11,), t(,,12), t(,,) }; \n");//$NON-NLS-1$
		
		// results in
		// int j[] = {123, 45, 67, 89, 10, 11, 12, };
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(24, tokens.size());
		assertToken(0, tokens.get(0));
		
		Iterator iter = tokens.iterator();
		iter.next();
		
		
		assertToken(C99Parsersym.TK_int, iter.next());
		assertToken(C99Parsersym.TK_identifier, iter.next());
		assertToken(C99Parsersym.TK_LeftBracket, iter.next());
		assertToken(C99Parsersym.TK_RightBracket, iter.next());
		assertToken(C99Parsersym.TK_Assign, iter.next());
		assertToken(C99Parsersym.TK_LeftBrace, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertEquals("123", tokens.get(7).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertEquals("45", tokens.get(9).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertEquals("67", tokens.get(11).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertEquals("89", tokens.get(13).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertEquals("10", tokens.get(15).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertEquals("11", tokens.get(17).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_Comma, iter.next());
		assertToken(C99Parsersym.TK_integer, iter.next());
		assertEquals("12", tokens.get(19).toString());//$NON-NLS-1$
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
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(9, tokens.size());
		assertToken(0, tokens.get(0));
		
		assertToken(C99Parsersym.TK_identifier, tokens.get(1));
		assertToken(C99Parsersym.TK_LeftParen, tokens.get(2));
		assertToken(C99Parsersym.TK_identifier, tokens.get(3));
		assertToken(C99Parsersym.TK_Comma,      tokens.get(4));
		assertToken(C99Parsersym.TK_stringlit,  tokens.get(5));
		assertToken(C99Parsersym.TK_RightParen,  tokens.get(6));
		assertToken(C99Parsersym.TK_SemiColon,  tokens.get(7));		
	}
	
	public void testSpecExample7_2() {
		StringBuffer sb = getExample7Defines();
		sb.append("debug(\"X = %d\\n\", x); \n");//$NON-NLS-1$
		// fprintf(stderr, "X = %d\n", x ); //9
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(11, tokens.size());
		assertToken(0, tokens.get(0));
		
		assertToken(C99Parsersym.TK_identifier, tokens.get(1));
		assertToken(C99Parsersym.TK_LeftParen, tokens.get(2));
		assertToken(C99Parsersym.TK_identifier, tokens.get(3));
		assertToken(C99Parsersym.TK_Comma,      tokens.get(4));
		assertToken(C99Parsersym.TK_stringlit,  tokens.get(5));
		assertToken(C99Parsersym.TK_Comma,      tokens.get(6));
		assertToken(C99Parsersym.TK_identifier, tokens.get(7));
		assertToken(C99Parsersym.TK_RightParen,  tokens.get(8));
		assertToken(C99Parsersym.TK_SemiColon,  tokens.get(9));		
	}
	
	
	public void testSpecExample7_3() {
		StringBuffer sb = getExample7Defines();
		sb.append("showlist(The first, second, and third items.); \n");//$NON-NLS-1$
		// puts( "The first, second, and third items." ); //5
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(7, tokens.size());
		assertToken(0, tokens.get(0));
		
		assertToken(C99Parsersym.TK_identifier, tokens.get(1));
		assertToken(C99Parsersym.TK_LeftParen, tokens.get(2));
		assertToken(C99Parsersym.TK_stringlit,  tokens.get(3));
		// TODO: assertEquals("\"The first, second, and third items.\"", tokens.get(3).toString());
		assertToken(C99Parsersym.TK_RightParen,  tokens.get(4));
		assertToken(C99Parsersym.TK_SemiColon,  tokens.get(5));
	}
	
	
	
	public void testSpecExample7_4() {
		StringBuffer sb = getExample7Defines();
		sb.append("report(x>y, \"x is %d but y is %d\", x, y); \n");//$NON-NLS-1$
		// ( (x>y) ? puts("x>y") : printf("x is %d but y is %d", x, y) ); //22
		
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(24, tokens.size());
		assertToken(0, tokens.get(0));
		
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(1));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(2));
		assertToken(C99Parsersym.TK_identifier, tokens.get(3));
		assertToken(C99Parsersym.TK_GT,         tokens.get(4));
		assertToken(C99Parsersym.TK_identifier, tokens.get(5));
		assertToken(C99Parsersym.TK_RightParen,  tokens.get(6));
		assertToken(C99Parsersym.TK_Question,   tokens.get(7));
		assertToken(C99Parsersym.TK_identifier, tokens.get(8));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(9));
		assertToken(C99Parsersym.TK_stringlit,  tokens.get(10));
		// TODO : assertEquals("\"x>y\"", tokens.get(10).toString());
		assertToken(C99Parsersym.TK_RightParen, tokens.get(11));
		assertToken(C99Parsersym.TK_Colon,      tokens.get(12));
		assertToken(C99Parsersym.TK_identifier, tokens.get(13));
		assertToken(C99Parsersym.TK_LeftParen,  tokens.get(14));
		assertToken(C99Parsersym.TK_stringlit,  tokens.get(15));
		assertToken(C99Parsersym.TK_Comma,      tokens.get(16));
		assertToken(C99Parsersym.TK_identifier, tokens.get(17));
		assertToken(C99Parsersym.TK_Comma,      tokens.get(18));
		assertToken(C99Parsersym.TK_identifier, tokens.get(19));
		assertToken(C99Parsersym.TK_RightParen, tokens.get(20));
		assertToken(C99Parsersym.TK_RightParen, tokens.get(21));
		assertToken(C99Parsersym.TK_SemiColon,  tokens.get(22));
	}
	
	
	public void testConditionalExpressions() {
		// test hex and octal
		assertExpressionValue(0x1234, " 0x1234 ");
		assertExpressionValue(0x99,  "0x99");
		assertExpressionValue(0xAB + 0xFE, "0xABu + 0xFEUll");
		assertExpressionValue(020, "020");
		assertExpressionValue(077 + 0x22, " 077 + 0x22 ");
		assertExpressionValue(145, "145");
		
		// test char constants
		assertExpressionValue(97, " 'a' ");
		assertExpressionValue(97, " L'a' ");
		assertExpressionValue(24930, " 'ab' ");
		assertExpressionValue(6379864, " '\\x61YX' ");
		assertExpressionValue(2567, " '\\n\\a' ");
		assertExpressionValue(4660, " '\\u1234' ");
		assertExpressionValue(305419896, " '\\u12345678' ");
		
		// test that invalid escape sequences are actually caught
		// by the scanner
		assertInvalidToken(" '\\x' ");
		assertInvalidToken(" '\\u' ");
		assertInvalidToken(" '\\u123' "); // not a hex quad
		assertInvalidToken(" '\\q' "); // invalid escape sequence
	}
	
	public void testBug186047() {
		StringBuffer sb = getExample7Defines();
		sb.append("#define D \n");//$NON-NLS-1$
		sb.append("#if defined D \n");
		sb.append("    x; \n");
		sb.append("#endif \n");
		sb.append("#if defined(D) \n");
		sb.append("    y; \n");
		sb.append("#endif \n");
			
		List tokens = scanAndPreprocess(sb.toString());
		
		assertNotNull(tokens);
		assertEquals(6, tokens.size());
		assertToken(0, tokens.get(0));
		
		assertToken(C99Parsersym.TK_identifier,   tokens.get(1));
		assertEquals("x", tokens.get(1).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_SemiColon,    tokens.get(2));
		assertToken(C99Parsersym.TK_identifier,   tokens.get(3));
		assertEquals("y", tokens.get(3).toString());//$NON-NLS-1$
		assertToken(C99Parsersym.TK_SemiColon,    tokens.get(4));
	}
	
}
