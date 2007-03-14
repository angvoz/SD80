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

package org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor;



import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.c99.ILexer;
import org.eclipse.cdt.core.dom.c99.ILexerFactory;
import org.eclipse.cdt.core.dom.c99.IPreprocessorTokenOuput;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99ExprEvaluator;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.ast.ASTFileLocation;
import org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap;
import org.eclipse.cdt.internal.core.parser.scanner2.ScannerUtility;

// TODO: prove that all loops terminate!
/**
 * The C99 preprocessor.
 * 
 * @author Mike Kucera
 */
public class C99Preprocessor implements C99Parsersym {

	
	private static final String DEFINED_OPERATOR = "defined";
	
	
	private final KeywordMap keywordMap; // used to recognize certain identifiers as keywords 
	private final CodeReader codeReader; // The code reader that stores the source character stream of the file
	private final IScannerInfo scanInfo; // Used to resolve includes
	private final ILexerFactory lexerFactory; // Used to create lexers for included files
	private final ICodeReaderFactory codeReaderFactory; // Used to get source character buffers of included files
	
	
	private MacroEnvironment env; // Stores macro definitions created by #define
	private TokenList argumentOutputStream; // Used to collect the output when a macro argument is processed
	private InputTokenStream inputTokenStream; // stores tokens that have yet to be processed
	private IPreprocessorTokenOuput parser; // tokens are injected directly into the parser
	private IPreprocessorLog log; // resolves source offset location information
	private TokenExpansionMap tokenExpansionMap; // tracks certain tokens that are the result of macro expansion
	private C99Token lastTokenOutput = null; // used to detect adjacent string literals for concatenation
	
	
	private static class ParseException extends RuntimeException {}
	
	
	
	/**
	 * Creates a preprocessor using the given keywords.
	 */
	public C99Preprocessor(ILexerFactory lexerFactory, 
			CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, KeywordMap keywordMap) {
		
		this.keywordMap = keywordMap;
		this.codeReader = reader;
		this.scanInfo = scanInfo;
		this.lexerFactory = lexerFactory;
		this.codeReaderFactory = fileCreator;
	}

	
	/**
	 * Creates a preprocessor using the standard C99 keywords.
	 */
	public C99Preprocessor(ILexerFactory lexerFactory, 
			CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator) {
		
		this(lexerFactory, reader, scanInfo, fileCreator, new C99BaseKeywordMap());
	}
	
	
	/**
	 * Run the preprocessor, the resulting token stream will be injected directly
	 * into the given parser.
	 */
	public synchronized ILocationResolver preprocess(IPreprocessorTokenOuput parser) {
		LocationResolver locationResolver = new LocationResolver();
		
		this.parser = parser;
		this.inputTokenStream = new InputTokenStream();
		this.log = locationResolver;
		this.env = new MacroEnvironment();
		this.tokenExpansionMap = new TokenExpansionMap();

		// LPG requires that the parse stream must start with a dummy token
		parser.addToken(C99Token.DUMMY_TOKEN); 

		log.startTranslationUnit(codeReader);
		
		startProcess(codeReader);
		preprocessingFile();
		
		int tuSize = inputTokenStream.getTranslationUnitSize();
		parser.addToken(new C99Token(tuSize, tuSize, TK_EOF_TOKEN, "<EOF>"));
		
		log.endTranslationUnit(tuSize);
		
		// clean up
		this.parser = null;
		this.inputTokenStream = null;
		this.log = null;
		this.env = null;
		this.lastTokenOutput = null;
		this.tokenExpansionMap = null;
		
		return locationResolver;
	}
	
	
	private void startProcess(CodeReader reader) {
		ILexer lexer = lexerFactory.createLexer(reader);
		TokenList tokens = lexer.lex();
		inputTokenStream.pushIncludeContext(tokens, reader, 0, null);
	}
	

	/**
	 * Recursively processes an included file.
	 */
	private void processIncludedFile(final CodeReader reader, int directiveStartOffset, int directiveEndOffset, 
			                         int nameStartOffset, int nameEndOffset, String name, boolean systemInclude) {
		// Lex the file into tokens
		// TODO properly deal with invalid tokens
		ILexer lexer = lexerFactory.createLexer(reader);
		TokenList tokens = lexer.lex();
		
		final String fileName = new String(reader.filename);
		
		//log.encounterPoundInclude(directiveStartOffset, nameStartOffset, nameEndOffset, 
		//		                  directiveEndOffset, fileName.toCharArray(), systemInclude, false);
		
		log.startInclusion(reader, directiveStartOffset, directiveEndOffset, nameStartOffset, 
				           nameEndOffset, name.toCharArray(), systemInclude);
		
		// This code gets fired when the tokens that make up the included file get used up
		InputTokenStream.IIncludeContextCallback callback = new InputTokenStream.IIncludeContextCallback() {
			public void contextClosed() {
				log.endInclusion(reader, inputTokenStream.adjust(reader.buffer.length));
			}
		};
		
		inputTokenStream.pushIncludeContext(tokens, reader, directiveEndOffset, callback);
	}
	
	
	public String toString() {
		return inputTokenStream.toString();
	}
	
	
	
	/**
	 * Injects the given token into the parser, or int the case that a macro argument
	 * is being processed the token is collected in the argumentOutputStream instance variable.
	 */
	private void addToOutputStream(IToken t) {
		
		// If this token was the last token in a macro expansion then fire the end macro event
		Macro[] macros = tokenExpansionMap.getMacros(t);
		if(macros != null) {
			for(int i = 0; i < macros.length; i++) {
				log.endMacroExpansion(macros[i], t.getEndOffset() + 1);
			}
		}
	
		if(argumentOutputStream != null) {
			argumentOutputStream.add(t);
			return;
		}
		
		
		// Filter what tokens the parser sees, for example newline tokens are removed
		int newKind;
		switch(t.getKind()) {
			case TK_NewLine:       // the parser does not want to see newline or placemarker tokens
			case TK_PlaceMarker:
				return;
			case TK_identifier:    // convert the identifier to a keyword, if it is a keyword
			case TK_DisabledMacroName: // disabled macro names should be treated as regular identifiers 
				newKind = convertToKeyword(t.toString());
				break;
			case TK_Parameter:
				assert false; //all macro parameters should be replaced
			default:
				newKind = t.getKind();
		}
		
		t.setKind(newKind);
		C99Token toOutput = new C99Token(t);
		
		// concatenate adjacent string literals
		// TODO: make sure this is efficient
		if(lastTokenOutput != null && 
		   newKind == TK_stringlit &&
		   lastTokenOutput.getKind() == TK_stringlit) {
			
			String s1 = lastTokenOutput.toString();
			String s2 = toOutput.toString();
			String rep = s1.substring(0, s1.length()-1) + s2.substring(1);
			
			lastTokenOutput.setRepresentation(rep);
			lastTokenOutput.setEndOffset(toOutput.getEndOffset());
			// don't send the result to the parser
		}
		else {
			lastTokenOutput = toOutput;
			//System.out.println("Token: (" + toOutput.getKind() + ", " + toOutput.getStartOffset() + ", " + toOutput.getEndOffset() + ") " + toOutput);
			parser.addToken(toOutput);
		}
	}
	
	
	/**
	 * Uses the keyword map to convert the identifier into a keyword if it is one.
	 */
	private int convertToKeyword(String identifier) {
		Integer keywordKind = keywordMap.getKeywordKind(identifier);
		return keywordKind == null ? TK_identifier : keywordKind.intValue();
	}
	
	
	
	public static void printTokens(TokenList tl) {
		for(Iterator iter = tl.iterator(); iter.hasNext();) {
			System.out.print("(" + iter.next().toString() + ")->");
		}
		System.out.println();
	}
	
	
	/***************************************************************************************
	 * Preprocessor tokens
	 ***************************************************************************************/
	

	static final int 
		PPTOKEN   = 0, 
		HASH      = 1,
		HASHHASH  = 2,
		LPAREN    = 3,
		NEWLINE   = 4,
		IDENT     = 5,
		COMMA     = 6,
		RPAREN    = 7,
		DOTDOTDOT = 8,
		EOF       = 99;


	static final String 
		IF      = "if",
		IFDEF   = "ifdef",
		IFNDEF  = "ifndef",
		ELIF    = "elif",
		ELSE    = "else",
		ENDIF   = "endif",
		DEFINE  = "define",
		UNDEF   = "undef",
		INCLUDE = "include",
		PRAGMA  = "pragma",
		ERROR   = "error",
		WARNING = "warning";
	

	private static int toPPToken(IToken token) {
		if(token == null)
			return EOF;
		
		switch(token.getKind()) {
			case TK_Hash:       return HASH;
			case TK_HashHash:   return HASHHASH;
			case TK_LeftParen:  return LPAREN;
			case TK_NewLine:    return NEWLINE;
			case TK_identifier: return IDENT;
			case TK_Comma:      return COMMA;
			case TK_RightParen: return RPAREN;
			case TK_DotDotDot:  return DOTDOTDOT;
			case TK_EOF_TOKEN:  return EOF;
			default:            return PPTOKEN;
		}
	}
	
	
	private boolean check(int pptoken) {
		IToken token = inputTokenStream.peek();
		if(token == null)
			return false;
		
		return pptoken == toPPToken(token);
	}
	
	private boolean check(String directive) {
		IToken token = inputTokenStream.peek();
		if(token == null)
			return false;
		
		return directive.equals(token.toString()); 
	}
	
	
	private boolean done() {
		IToken token = inputTokenStream.peek();
		return token == null || token.getKind() == TK_EOF_TOKEN;
	}
	
	
	private IToken expect(int pptoken) {
		if(check(pptoken))
			return next();
		else 
			throw new ParseException();
	}
	
	private IToken expect(String directive) {
		if(check(directive))
			return next();
		else 
			throw new ParseException();
	}
	
	
	private IToken next() {
		return inputTokenStream.next();
	}

	
	/***************************************************************************************
	 * Grammar rules
	 ***************************************************************************************/
	
	
	private void preprocessingFile() {
		while(!done()) {
			try {
				
				if(check(HASH)) {
					System.out.println("Contol line");
					boolean stop = controlLine();
					
					// TODO: if processing of the file was stopped prematurely
					// by improperly nested #ifs then create error;
					// TODO: what if an eof occurs before an if is done,
					// probably detected by ifSection() as it will want to consume an #endif
					if(stop)
						return;
				}
				else
					textLine();
				
			} catch(ParseException e) { 
				// panic mode recovery, syncronize on next newline
				while(!(check(NEWLINE) || done())) {
					next();
				}
				if(check(NEWLINE))
					next();
				
				// TODO: generate a problem
			}
		}
	}
	
	
	private void skipGroup() {
		while(!done()) {
			if(check(HASH)) {
				IToken hash = next();
				if(check(ELIF) || check(ELSE) || check(ENDIF)) {
					inputTokenStream.addTokenToFront(hash);
					return;
				}
			}
			skipLine();
		}
	}
	
	
	/**
	 * Skips input until a newline is encountered, the 
	 * newline is also skipped.
	 */
	private void skipLine() {
		while(!(check(NEWLINE) || done())) {
			next();
		}
		next(); // skip the newline
	}
	
	
	private void textLine() { textLine(false); }
	
	private void textLine(boolean handleDefined) {
		while(!(check(NEWLINE) || done())) {
			IToken currentToken = inputTokenStream.peek();
			if(handleDefined && check(IDENT) && DEFINED_OPERATOR.equals(currentToken.toString())) {
				handleDefined();
			}
			if(check(IDENT) && env.hasMacro(currentToken.toString())) {
				Macro macro = env.get(currentToken.toString());
				macroCall(macro);
			}
			else {
				addToOutputStream(next());
			}
		}
		
		if(check(NEWLINE))
			next(); // skip the newline
	}
	
	
	private void handleDefined() {
		next(); // skip the defined keyword
		
		IToken ident;
		if(check(RPAREN)) {
			next();
			ident = expect(IDENT);
			expect(LPAREN);
		}
		else if(check(IDENT)) {
			ident = next();
		}
		else {
			// TODO: is this the right thing to do?
			throw new ParseException();
		}
		
		String val = env.hasMacro(ident.toString()) ? "1" : "0";
		// TODO: what about the offsets (they probably don't matter)
		addToOutputStream(new C99Token(0, 0, TK_integer, val));
	}


	/**
	 * Parses a macro invocation, either object like of function like.
	 */
	private void macroCall(Macro macro) {
		IToken macroId = next();
		
		int expansionLocationOffset;
		TokenList replacement;
		
		if(macro.isObjectLike()) {
			replacement = macro.invoke();
			expansionLocationOffset = macroId.getEndOffset();
		}
		else { // function like
			// any number of newlines are allowed between the macro name and the args
			// if there is a preprocessing directive then the result is undefined
			while(check(NEWLINE))
				next();
			
			if(!check(LPAREN)) { // then its not actually a call to the macro
				addToOutputStream(macroId);
				return;
			}
			
			replacement = macroCallFunctionLike(macro);
			IToken rparen = next(); // the right paren will not be consumed by macroCallFunctionLike(), because we need it here
			expansionLocationOffset = rparen.getEndOffset();
		}
		
		expansionLocationOffset += 1;
		
		if(replacement == null || replacement.isEmpty()) {
			log.startMacroExpansion(macro, macroId.getStartOffset(), expansionLocationOffset);
			log.endMacroExpansion(macro, expansionLocationOffset);
		}
		else {
			// Add the result of the macro expansion to the input so it can be rescanned with the rest of the file
			inputTokenStream.pushMacroExpansionContext(replacement, expansionLocationOffset);
			// Record the offset of the expansion so that locations are resolved properly
			log.startMacroExpansion(macro, macroId.getStartOffset(), expansionLocationOffset);
			// log.endMacroExpansion() will be call when the last token in the replacement is output, see addToOutputStream()
			tokenExpansionMap.putMacro(replacement.last(), macroId, macro);
		}
	}

	
	
	private TokenList macroCallFunctionLike(Macro macro) {
		next(); // discard the LPAREN
		
		// arguments to the macro
		List arguments = new Vector(); // List<TokenList> 
		int numParams = macro.getNumParams(); // number of parameters not including '...'
		System.out.println("numParams: " + numParams);
		TokenList arg = new TokenList();
		int parenLevel = 0;
		
		if(check(RPAREN)) {
			//next();
			// no arguments to a macro that takes a parameter, create an empty argument
			if(macro.getNumParams() > 0) {
				arguments.add(arg);
			}
		}
		else {
			while(true) {
				if(done()) {
					// TODO: error
					return null;
				}
				if(check(HASH) || check(HASHHASH)) {
					// error: unterminated argument list
					return null;
				}
				if(check(NEWLINE)) {
					// newlines are allowed within a macro invocation, they are ignored
					// TODO: is a newline allowed between the macro name and the (
					next();
				}
				else if(check(COMMA)) {
					if(parenLevel == 0 && arguments.size() < numParams) {
						arguments.add(arg); // arg may be empty, thats ok
						arg = new TokenList();
						next();
					}
					else { // if nested brackets or varargs then collect the comma
						arg.add(next());
					}
				}
				else if(check(LPAREN)) {
					parenLevel++;
					arg.add(next());
				}
				else if(check(RPAREN)) {
					if(parenLevel == 0) {
						arguments.add(arg);
						//next();  // when this method returns the RPAREN will not be consumed
						break;
					}
					else {
						parenLevel--;
						arg.add(next());
					}
				}
				else {
					arg.add(next());
				}
			}
		}
		
		// TODO: rename this from Closure to something else, or maybe just get rid of Closure and
		// resort to a more imperative (read crappy) approach
		final Closure process = new Closure() { public void call() { preprocessingFile(); } };
		
		List macroArguments = new Vector(arguments.size());
		
		for(int i = 0; i < arguments.size(); i++) {
			TokenList its = (TokenList) arguments.get(i);
			
			macroArguments.add(new MacroArgument(its,
				new MacroArgument.IProcessCallback() {
					public TokenList process(TokenList tokens) {
						return pushContextAndProcess(tokens, process);
					}
				}
			));
		}
		
		System.out.println("macroArguments: " + macroArguments);
		if(macro.isCorrectNumberOfArguments(macroArguments)) {
			TokenList expansion = macro.invoke(macroArguments);
			//inputTokenStream.pushMacroExpansionContext(expansion, expansionLocationOffset);
			return expansion;
		}
		else {
			// TODO: handle this problem
			System.out.println("isCorrectNumberOfArguments failed");
			return null;
		}
	}
	
	
	private boolean controlLine() {
		IToken hash = next();
		int directiveStartOffset = hash.getStartOffset();
		
		if(check(NEWLINE)) {
			next();
		}
		else if(check(DEFINE)) {
			next();
			defineDirective(hash.getStartOffset());
		}
		else if(check(UNDEF)) {
			next();
			IToken macroName = expect(IDENT);
			log.undefineMacro(directiveStartOffset, macroName.getEndOffset(), macroName.toString(), macroName.getStartOffset());
			env.removeMacro(macroName.toString());
			expect(NEWLINE);
		}
		else if(check(INCLUDE)) {
			next();
			includeDirective(directiveStartOffset);
		}
		else if(check(IF) || check(IFDEF) || check(IFNDEF)) {
			ifSection(directiveStartOffset);
		}
		else if(check(ELIF) || check(ELSE) || check(ENDIF)) {
			inputTokenStream.addTokenToFront(hash);
			return true;
		}
		else if(check(PRAGMA)) {
			IToken pragma = next();
			TokenList tokens = collectTokensUntilNewline();
			tokens.addFirst(pragma); // just in case tokens is empty
			log.encounterPoundPragma(directiveStartOffset, tokens.last().getEndOffset());
			expect(NEWLINE);
		}
		else if(check(ERROR)) {
			IToken error = next();
			TokenList tokens = collectTokensUntilNewline();
			tokens.addFirst(error); // just in case tokens is empty
			log.encounterPoundError(directiveStartOffset, tokens.last().getEndOffset());
			expect(NEWLINE);
		}
		else if(check(WARNING)) { // not in the spec, but hell, why not?
			IToken error = next();
			TokenList tokens = collectTokensUntilNewline();
			tokens.addFirst(error); // just in case tokens is empty
			log.encounterPoundError(directiveStartOffset, tokens.last().getEndOffset());
			expect(NEWLINE);
		}
		else { 
			// all other preprocessing directives and non directives are ignored.
			skipLine();
		}
		return false;
	}
	
	// can't return from this until the entire if-group is processed
	private void ifSection(int directiveStartOffset) {
		boolean takeIfBranch;
		
		// Determine if the branch should be followed
		if(check(IFDEF) || check(IFNDEF)) {
			boolean isIfdef = check(IFDEF);
			next();
			IToken ident = expect(IDENT);
			expect(NEWLINE);
			takeIfBranch = isIfdef == env.hasMacro(ident.toString());
			
			int directiveEndOffset = ident.getEndOffset();
			if(isIfdef)
				log.encounterPoundIfdef(directiveStartOffset, directiveEndOffset, takeIfBranch);
			else
				log.encounterPoundIfndef(directiveStartOffset, directiveEndOffset, takeIfBranch);
		}
		else { // its an if
			next();
			TokenList expressionTokens = collectTokensUntilNewline(); // TODO: what if expressionTokens is empty?
			int endOffset = expressionTokens.last().getEndOffset();
			int value = evaluateConstantExpression(expressionTokens); // throws ParseException if invalid expression
			expect(NEWLINE);
			takeIfBranch = value != 0;
			
			log.encounterPoundIf(directiveStartOffset, endOffset, takeIfBranch);
		}
		
		if(takeIfBranch) {
			preprocessingFile(); // will return when it hits an elif, else or endif
			if(check(HASH))
				elseGroups(true);
			else if(done())
				return;
			else
				return; // TODO: is this a problem
		}
		else { // skip the branch
			skipGroup();
			elseGroups(false);
		}
	}
	
	
	private int evaluateConstantExpression(TokenList expressionTokens) {
		// First expand macros and handle the 'defined' operator
		Closure processIfCondition = new Closure() { public void call() { textLine(true); } };
		TokenList constantExpression = pushContextAndProcess(expressionTokens, processIfCondition);
		
		// then evaluate the expression
		C99ExprEvaluator evaluator = new C99ExprEvaluator(constantExpression);
		Integer value = evaluator.evaluate();
		
		if(value == null) {
			// TODO, also generate a problem or something
			System.err.println("Error parsing conditional expression");
			throw new ParseException();
		}
		return value.intValue();
	}
	
	
	private void elseGroups(boolean skipRest) {
		// TODO: check for wierd cases
		while(true) {
			IToken hash = expect(HASH);
			int directiveStartOffset = hash.getStartOffset();
			
			if(check(ENDIF)) {
				IToken endif = next();
				log.encounterPoundEndIf(directiveStartOffset, endif.getEndOffset());
				expect(NEWLINE);
				return;
			}
			else if(check(ELIF)) {
				IToken elif = next();
				TokenList expressionTokens = collectTokensUntilNewline();
				// TODO: maybe just create a problem instead
				int endOffset = expressionTokens.isEmpty() ? elif.getEndOffset() : expressionTokens.last().getEndOffset();
				
				if(skipRest) {
					log.encounterPoundElif(directiveStartOffset, endOffset, false);
					skipGroup();
				}
				else if (expressionTokens == null || expressionTokens.isEmpty()) {
					// TODO: error
				}
				else {
					
					int value = evaluateConstantExpression(expressionTokens);
					expect(NEWLINE);
					log.encounterPoundElif(directiveStartOffset, endOffset, value != 0);
					if(value != 0) {
						skipRest = true;
						preprocessingFile();
					}
					else {
						skipGroup();
					}
				}
			}
			else if(check(ELSE)) {
				IToken els = next();
				log.encounterPoundElse(directiveStartOffset, els.getEndOffset(), !skipRest);
				if(skipRest)
					skipGroup();
				else 
					preprocessingFile();
			}
			else {
				// TODO: error!, problem
				return;
			}
		}
	}


	// TODO: handle circular inclusions
	// TODO: how does inclusions affect source locations
	// TODO: empty include directive?
	private void includeDirective(int directiveStartOffset) {	
		System.err.println("include directive");
		// The include body can contain a macro invocations
		Closure processIncludeBody = new Closure() { public void call() { textLine(false); } };
		TokenList includeBody = pushContextAndProcess(collectTokensUntilNewline(), processIncludeBody);
		
		String fileName = computeIncludeFileName(includeBody);
		
		int directiveEndOffset = includeBody.last().getEndOffset() + 1;
		int nameStartOffset    = includeBody.first().getStartOffset() + 1;
		int nameEndOffset      = includeBody.last().getEndOffset();
		
		
		if(fileName == null) {
			// TODO: problem
			return;
		}

		boolean local = fileName.startsWith("\"");
		fileName = fileName.substring(1, fileName.length() - 1); // remove the double quotes or double angle brackets
		
		if (new File(fileName).isAbsolute() || fileName.startsWith("/")) {
			CodeReader reader = createCodeReader("", fileName);
			if(reader != null) { 
				processIncludedFile(reader, directiveStartOffset, directiveEndOffset, nameStartOffset, nameEndOffset, fileName, false);
			}
			else {
				// TODO: problem, inclusion not found
			}
			return;  
		}

		if(local) { // local
			File currentDirectory = inputTokenStream.getCurrentDirectory();
			if(currentDirectory != null) { 
				String absolutePath = currentDirectory.getAbsolutePath();
				CodeReader reader = createCodeReader(absolutePath, fileName);
				if(reader != null) {
					processIncludedFile(reader, directiveStartOffset, directiveEndOffset, nameStartOffset, nameEndOffset, fileName, false);
					return;
				}
			}
		}
		
		if(scanInfo != null) {
			String[] standardIncludePaths = scanInfo.getIncludePaths();
			if(standardIncludePaths != null) {
				for(int i = 0; i < standardIncludePaths.length; i++) {
					CodeReader reader = createCodeReader(standardIncludePaths[i], fileName);
					if(reader != null) {
						processIncludedFile(reader, directiveStartOffset, directiveEndOffset, nameStartOffset, nameEndOffset, fileName, true);
						return;
					}
				}
			}
		}
		
		// TODO: create a problem
	}
	
	
	
	private static String computeIncludeFileName(TokenList includeBody) {
		if(includeBody == null || includeBody.isEmpty() || includeBody.size() == 2) {
			return null;
		}

		// local include
		if(includeBody.size() == 1 && includeBody.first().getKind() == TK_stringlit) {
			return includeBody.first().toString();
		}
		
		// at this point the size must be at least 3
		IToken first = includeBody.removeFirst();
		IToken last  = includeBody.removeLast();
		
		if(first.getKind() != TK_LT || last.getKind() != TK_GT) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append('<');
		sb.append(spaceBetween(first, includeBody.first()));
		
		IToken prevToken = first;
		IToken token = null; // the iterator must contain at least one element
		for(Iterator iter = includeBody.iterator(); iter.hasNext();) {
			token = (IToken) iter.next();
			sb.append(spaceBetween(prevToken, token));
			sb.append(token.toString());
			prevToken = token;
		}
		
		sb.append(spaceBetween(token, last));
		sb.append('>');
		return sb.toString();
	}
	
	
	/**
	 * Returns the number of characters of whitespace between the two tokens.
	 */
	private static StringBuffer spaceBetween(IToken t1, IToken t2) {
		int numSpaces = t2.getStartOffset() - (t1.getEndOffset() + 1);
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < numSpaces; i++) {
			sb.append(' ');
		}
		return sb;
	}


	private CodeReader createCodeReader(String path, String fileName) {
		// Processes wierd path cases like .. and slashes and stuff.
		// TODO: Can ScannerUtility.createReconciledPath be replaced by some regular expressions?
		String finalPath = ScannerUtility.createReconciledPath(path, fileName);
		return codeReaderFactory.createCodeReaderForInclusion(null, finalPath);
	}
	
	/**
	 * Does not collect or discard the end of line token.
	 */
	private TokenList collectTokensUntilNewline() {
		TokenList result = new TokenList();
		while(!check(NEWLINE)) {
			result.add(next());
		}
		return result;
	}
	
	
	// A little bit of functional programming in Java...
	private interface Closure {
		void call();
	}
	
	
	/**
	 * Used to process include files and macro arguments and such.
	 * The Closure allows the actuall process to be provided, for example
	 * an include file will require that preprocessingFile() be called
	 * but a constant expression just requires that textLine() be called.
	 */
	private TokenList pushContextAndProcess(TokenList newInput, Closure process) {
		//InputTokenStream savedInputTokenStream = inputTokenStream;
		TokenList savedArgumentOutputStream = argumentOutputStream;
		inputTokenStream.pushStopContext(newInput);
		argumentOutputStream = new TokenList(); // collect the output
		
		process.call();
		
		// the token stream will get stuck, so unstick it
		inputTokenStream.resume();
		
		TokenList result = argumentOutputStream;
		argumentOutputStream = savedArgumentOutputStream;
		return result;
	}
	


	/**
	 * Creates a Macro object and stores it in the environment.
	 */
	private void defineDirective(int startOffset) {
		if(check(IDENT)) {
			IToken macroName = next();
			Macro macro;
			
			if(check(LPAREN)) { // function like macro
				next();
							
				String varArgParamName = null;

				// no duplicates, maintains insertion order
				// TODO: each call to paramNames.add should be checked for duplicate
				// TODO: the varArgParamName should not be a duplicate
				LinkedHashSet paramNames = new LinkedHashSet();
				
				IToken rparen; // the data-flow analyzer will make sure rparen has a value
				
				if(check(RPAREN)) {
					rparen = next();
				}
				else {
					// parse the parameters
					while(true) {
						if(check(IDENT)) {
							String name = next().toString();
							
							if(check(DOTDOTDOT)) {
								varArgParamName = name;
								next();
								rparen = expect(RPAREN);
								break;
							}
							
							if(!paramNames.add(name)) {
								// TODO: error duplicate parameter
							}
							
							if(check(COMMA)) {
								next();
							}
							else if(check(RPAREN)) {
								rparen = next();
								break;
							}
							else {
								throw new ParseException();
							}
						}
						else if(check(DOTDOTDOT)) {
							varArgParamName = Macro.__VA_ARGS__;
							next();
							rparen = expect(RPAREN);
							break;
						}
						else {
							throw new ParseException();
						}
					}
				}
				
				// TODO replacement list cannot start or end with ##
				TokenList replacementList = replacementList(paramNames, varArgParamName);
				int endOffset = calculateMacroDefinitionEndOffset(rparen, replacementList);
				macro = new Macro(macroName, replacementList, startOffset, endOffset, paramNames, varArgParamName);
				System.out.println("Adding FL macro: " + macroName);
			}
			else { // object like macro
				TokenList replacementList = replacementList(null, null);
				int endOffset = calculateMacroDefinitionEndOffset(macroName, replacementList);
				macro = new Macro(macroName, replacementList, startOffset, endOffset);
				System.out.println("Adding OL macro: " + macroName);
			}
			
			env.addMacro(macro);
			log.defineMacro(macro);
		}
		else {
			throw new ParseException();
		}
	}
	
	
	private static int calculateMacroDefinitionEndOffset(IToken tokenBeforeReplacementList, TokenList replacementList) {
		if(replacementList == null || replacementList.isEmpty())
			return tokenBeforeReplacementList.getEndOffset() + 1;
		else
			return replacementList.last().getEndOffset() + 1;
	}
	
	
	private TokenList replacementList(Set paramNames, String varArgParamName) {
		TokenList tokens = new TokenList();
		
		while(!check(NEWLINE) && !done()) { // TODO: done is an error
			
			IToken token = next();
			
			// avoid accedental name capture by changing the token kind of parameters
			if(isParamName(token, paramNames, varArgParamName)) {
				token.setKind(TK_Parameter);
			}
			
			tokens.add(token);
		}
		next(); // consume the newline
		return tokens;
	}
	
	
	private static boolean isParamName(IToken token, Set paramNames, String varArgParamName) {
		if(token.getKind() != TK_identifier)
			return false;
		if(paramNames == null)
			return false;
		String ident = token.toString();
		return paramNames.contains(ident) || ident.equals(varArgParamName);
	}
	
}
