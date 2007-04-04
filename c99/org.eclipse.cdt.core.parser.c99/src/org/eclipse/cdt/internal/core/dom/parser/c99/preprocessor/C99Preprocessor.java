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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IMacroCollector;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.c99.ILexer;
import org.eclipse.cdt.core.dom.c99.ILexerFactory;
import org.eclipse.cdt.core.dom.c99.IPreprocessorTokenOuput;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99ExprEvaluator;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;
import org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner2.ScannerASTProblem;
import org.eclipse.cdt.internal.core.parser.scanner2.ScannerUtility;

// TODO: prove that all loops terminate!
/**
 * The C99 preprocessor.
 * 
 */
public class C99Preprocessor implements C99Parsersym {

	
	private static final String DEFINED_OPERATOR = "defined"; //$NON-NLS-1$
	private static final String DIRECTIVE_COMPLETION_TOKEN_PREFIX = "#"; //$NON-NLS-1$
	
	
	// The number of EndOfCompletion tokens to generate after
	// generating a Completion token.
	private static final int NUM_EOC_TOKENS = 20;
	
	private final IKeywordMap keywordMap; // used to recognize certain identifiers as keywords 
	private final CodeReader codeReader; // The code reader that stores the source character stream of the file
	private final IScannerInfo scanInfo; // Used to resolve includes and get access to macro definitions
	private final ILexerFactory lexerFactory; // Used to create lexers for included files
	private final ICodeReaderFactory codeReaderFactory; // Used to get source character buffers of included files
	
	
	private MacroEnvironment env; // Stores macro definitions created by #define
	private TokenList argumentOutputStream; // Used to collect the output when a macro argument is processed
	private InputTokenStream inputTokenStream; // stores tokens that have yet to be processed
	private IPreprocessorTokenOuput parser; // tokens are injected directly into the parser
	private IPreprocessorLog log; // resolves source offset location information
	private TokenExpansionMap tokenExpansionMap; // tracks certain tokens that are the result of macro expansion
	private C99Token lastTokenOutput = null; // used to detect adjacent string literals for concatenation
	
	
	private static class PreprocessorInternalParseException extends RuntimeException {}
	private static class PreprocessorAbortParseException extends RuntimeException {}
	
	
	
	/**
	 * Creates a preprocessor using the given keywords.
	 */
	public C99Preprocessor(ILexerFactory lexerFactory, 
			CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IKeywordMap keywordMap) {
		
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
		
		this(lexerFactory, reader, scanInfo, fileCreator, new C99KeywordMap());
	}
	
	
	/**
	 * Run the preprocessor, the resulting token stream will be injected directly
	 * into the given parser.
	 * 
	 * @return null if preprocessing fails
	 * @throws IllegalArgumentException if parser is null
	 */
	public synchronized ILocationResolver preprocess(IPreprocessorTokenOuput parser) {
		if(parser == null)
			throw new IllegalArgumentException(Messages.getString("C99Preprocessor.0")); //$NON-NLS-1$
		
		return preprocess(parser, new InputTokenStream());
	}
	
	
	/**
	 * Run the preprocessor in contentAssistMode, the resulting token stream will be injected directly
	 * into the given parser.
	 * 
	 * @return null if preprocessing fails
	 * @throws IllegalArgumentException if contentAssistOffset < 0
	 * @throws IllegalArgumentException if parser is null
	 */
	public synchronized ILocationResolver preprocess(IPreprocessorTokenOuput parser, int contentAssistOffset) {
		if(parser == null)
			throw new IllegalArgumentException(Messages.getString("C99Preprocessor.0")); //$NON-NLS-1$
		if(contentAssistOffset < 0)
			throw new IllegalArgumentException(Messages.getString("C99Preprocessor.2")); //$NON-NLS-1$
		
		InputTokenStream inputTokenStream = new InputTokenStream();
		inputTokenStream.setContentAssistOffset(contentAssistOffset);
		return preprocess(parser, inputTokenStream);
		
	}
	
	
	private synchronized ILocationResolver preprocess(IPreprocessorTokenOuput parser, InputTokenStream inputTokenStream) {
		assert inputTokenStream != null;
		
		LocationResolver locationResolver = new LocationResolver();
		
		this.parser = parser;
		this.log = locationResolver;
		this.env = new MacroEnvironment();
		this.tokenExpansionMap = new TokenExpansionMap();
		this.inputTokenStream = inputTokenStream;
		
		try {
			// add external macro definitions given by IScannerInfo.getDefinedSymbols()
			addMacroDefinitions();
			
			// LPG requires that the parse stream must start with a dummy token
			parser.addToken(C99Token.DUMMY_TOKEN); 
	
			log.startTranslationUnit(codeReader);
			
			ILexer lexer = lexerFactory.createLexer(codeReader);
			TokenList tokens = lexer.lex();
		
			inputTokenStream.pushIncludeContext(tokens, codeReader, 0, null);
			
			preprocessingFile(); // throws AbortParseException
			
			int tuSize = inputTokenStream.getTranslationUnitSize();
			parser.addToken(new C99Token(tuSize, tuSize, TK_EOF_TOKEN, "<EOF>")); //$NON-NLS-1$
			
			log.endTranslationUnit(tuSize);
		}
		catch(PreprocessorAbortParseException e) {
			return null;
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			// clean up
			this.parser = null;
			this.inputTokenStream = null;
			this.log = null;
			this.env = null;
			this.lastTokenOutput = null;
			this.tokenExpansionMap = null;
		}
		
		return locationResolver;
	}
	
	

	

	/**
	 * Recursively processes an included file.
	 */
	private void processIncludedFile(final CodeReader reader, int directiveStartOffset, int directiveEndOffset, 
			                         int nameStartOffset, int nameEndOffset, String name, boolean systemInclude) {
		// Lex the file into tokens
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
		if(lastTokenOutput != null && 
		   newKind == TK_stringlit &&
		   lastTokenOutput.getKind() == TK_stringlit) {
			
			String s1 = lastTokenOutput.toString();
			String s2 = toOutput.toString();
			assert s1.length() >=2 && s2.length() != 2; // smallest string literal is ""
			
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
			System.out.print("(" + iter.next().toString() + ")->"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		System.out.println();
	}
	
	
	/***************************************************************************************
	 * Preprocessor tokens
	 ***************************************************************************************/
	

	// Tokens that the preprocessor recongizes
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


	// Identifier tokens regognized as reprocessor directives
	static final String 
		IF      = "if", //$NON-NLS-1$
		IFDEF   = "ifdef", //$NON-NLS-1$
		IFNDEF  = "ifndef", //$NON-NLS-1$
		ELIF    = "elif", //$NON-NLS-1$
		ELSE    = "else", //$NON-NLS-1$
		ENDIF   = "endif", //$NON-NLS-1$
		DEFINE  = "define", //$NON-NLS-1$
		UNDEF   = "undef", //$NON-NLS-1$
		INCLUDE = "include", //$NON-NLS-1$
		PRAGMA  = "pragma", //$NON-NLS-1$
		ERROR   = "error", //$NON-NLS-1$
		WARNING = "warning"; //$NON-NLS-1$
	

	
	

	/**
	 * Checks the type of the current token.
	 */
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
	
	
	/**
	 * Returns true when the end of the input has been reached.
	 */
	private boolean done() {
		IToken token = inputTokenStream.peek();
		return token == null || token.getKind() == TK_EOF_TOKEN;
	}
	

	/**
	 * Checks for the given token type, if not present
	 * then an exception is thrown.
	 */
	private IToken expect(int pptoken) {
		if(check(pptoken))
			return next();
		else 
			throw new PreprocessorInternalParseException();
	}
	

	private IToken expect(String directive) {
		if(check(directive))
			return next();
		else 
			throw new PreprocessorInternalParseException();
	}
	
	
	/**
	 * Moves to the next token on the input stream.
	 */
	private IToken next() {
		return inputTokenStream.next();
	}

	private IToken next(boolean adjust) {
		return inputTokenStream.next(adjust);
	}
	
	
	/**
	 * Converts a regular token type into one of the
	 * token types that are recognized by the preprocessor.
	 */
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
	
	
	/***************************************************************************************
	 * Problem handling
	 ***************************************************************************************/
	
	//Generates a problem node in the AST.
	private void encounterProblem(int id, IToken token) {
		if(token != null) {
			encounterProblem(id, token.getStartOffset(), token.getEndOffset(), null);
		}
		else {
			int offset = inputTokenStream.getCurrentOffset();
			encounterProblem(id, offset, offset, null);
		}
	}
	
	//Generates a problem node in the AST.
	private void encounterProblem(int id, int startOffset, int endOffset) {
		encounterProblem(id, startOffset, endOffset, null);
	}
	
	//Generates a problem node in the AST.
	private void encounterProblem(int id, TokenList tokenList) {
		int start = tokenList.first().getStartOffset();
		int end = tokenList.last().getEndOffset();
		encounterProblem(id, start, end, createProblemArg(tokenList));
	}
	
	//Generates a problem node in the AST.
	private void encounterProblem(int id, int startOffset, int endOffset, char[] arg) {
		ScannerASTProblem problem = new ScannerASTProblem(id, arg, true, false);
		problem.setOffsetAndLength(startOffset, inputTokenStream.getCurrentOffset());
		log.encounterProblem(problem);
	}
	
	/**
	 * Converts a list of tokens into a char array, used by #error and others
	 */
	private static char[] createProblemArg(TokenList tokenList) {
		StringBuffer sb = new StringBuffer();
		
		Iterator iter = tokenList.iterator();
		while(iter.hasNext()) {
			IToken token = (IToken) iter.next();
			sb.append(token.toString());
			if(iter.hasNext())
				sb.append(' ');
		}
		
		return sb.toString().toCharArray();
	}
	
	/***************************************************************************************
	 * Grammar rules
	 ***************************************************************************************/
	
	// The main entry point to start preprocessing
	private void preprocessingFile() { // TODO: handles more than just files, rename
		while(!done()) {
			try {
				if(check(HASH)) {
					boolean encounteredPoundElse = controlLine();
					
					// Returns when an #else, #elif or #endif is encountered.
					// This is because ifSection() calls preprocessingFile() to handle a branch of an #if
					if(encounteredPoundElse)
						return;
				}
				else {
					textLine();
				}
			} catch(PreprocessorInternalParseException e) {
				if(inputTokenStream.isContentAssistMode())
					throw new PreprocessorAbortParseException();
				
				IToken token = null;
				while(!(check(NEWLINE) || done())) {
					token = next();
				}
				if(check(NEWLINE))
					token = next();
				
				encounterProblem(IASTProblem.SYNTAX_ERROR, token);
			}
		}
	}
	
	
	/**
	 * Skips a branch of a conditional compiliation construct
	 */
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
	
	
	/**
	 * Called when the cursor in the working copy is reached during a completion parse.
	 * Generates a completion token followed by several "end of completion" tokens.
	 * Allows the parser to create an ASTCompletionNode and successfully complete the parse.
	 */
	private void handleBasicContentAssistOffsetReached() {
		assert inputTokenStream.isContentAssistOffsetReached();

		if(inputTokenStream.isEmpty()) {
			int endOffset = inputTokenStream.getTranslationUnitSize();
			C99Token completionToken = new C99Token(endOffset, endOffset, TK_Completion, ""); //$NON-NLS-1$
			addCompletionTokenToOutputAndQuit(completionToken);
			return;
		}
		
		// The current token, offsets are not adjusted, therefore the offsets reflect the editor source.
		IToken token = inputTokenStream.peek();
		// The offset of the cursor in the editor.
		// The cursor sits between characters.
		int cursorOffset   = inputTokenStream.getContentAssistOffset();
		int adjustedOffset = inputTokenStream.adjust(cursorOffset) - 1;
		
		C99Token completionToken;
		
		if(token.getStartOffset() >= cursorOffset) {
			// we are in between tokens, create an empty completion token
			completionToken = new C99Token(adjustedOffset, adjustedOffset, TK_Completion, ""); //$NON-NLS-1$
		}
		else if(token.getKind() == TK_identifier) {
			// at this point we know the cursor is in the middle or at the end of an identifier token
			int startOffset = token.getStartOffset();

			// calculate the prefix
			int prefixLength = cursorOffset - startOffset;
			String prefix = token.toString().substring(0, prefixLength);
			
			// not sure if calculating these offsets is even necessary
			int newStartOffset = inputTokenStream.adjust(startOffset);
			int newEndOffset   = inputTokenStream.adjust(startOffset + prefixLength);
			
			completionToken = new C99Token(newStartOffset, newEndOffset, TK_Completion);
			completionToken.setRepresentation(prefix);
		}
		else if(token.getEndOffset() == cursorOffset - 1) {
			// its not an identifier, and we are at the end of the token
			completionToken = new C99Token(adjustedOffset, adjustedOffset, TK_Completion, ""); //$NON-NLS-1$
			addToOutputStream(next()); // very important
		}	
		else { 
			// we are in the middle of a number or a string literal or something, abort parse
			throw new PreprocessorAbortParseException();
		}
		
		addCompletionTokenToOutputAndQuit(completionToken);
	}
	
	
	
	/**
	 * Generates a bunch of EOC tokens then stops processing.
	 * There is no way to predict how many EOC tokens the parser
	 * will need in order to be able to successfully finish the parse,
	 * therefore we choose an arbitrary number NUM_EOC_TOKENS and
	 * hope its enough.
	 */
	private void addCompletionTokenToOutputAndQuit(IToken completionToken) {
		assert completionToken.getKind() == TK_Completion;
		
		addToOutputStream(completionToken);
		
		int offset = completionToken.getEndOffset() + 1;
		// Generate a bunch of eoc tokens
		for(int i = 0; i < NUM_EOC_TOKENS; i++) {
			addToOutputStream(new C99Token(offset, offset, TK_EndOfCompletion, "")); //$NON-NLS-1$
		}		
		// discard the rest of the input tokens on the input
		inputTokenStream = new InputTokenStream();
	}
	
	
	private void addCompletionTokenToOutputAndQuit(int offset, String prefix) {
		addCompletionTokenToOutputAndQuit(new C99Token(offset, offset, TK_Completion, prefix));
	}
	
	
	
	/**
	 * Handles a single line of text. Can also be used to process macro arguments
	 * or the body of an #include or #if directive.
	 * @param handleDefined Used for #if directives, when true it will process the 'defined' operator.
	 */
	private void textLine(boolean handleDefined) {
		while(true) {
			if(inputTokenStream.isContentAssistOffsetReached() && !handleDefined) {
				// TODO: what if we aren't adding tokens to the output, what if we
				// are processing a macro argument or something?
				handleBasicContentAssistOffsetReached();
				return;
			}
			
			if(check(NEWLINE) || done()) {
				break;
			}
			
			IToken currentToken = inputTokenStream.peek();
			if(handleDefined && check(IDENT) && DEFINED_OPERATOR.equals(currentToken.toString())) {
				next(); // skip the defined keyword
				handleDefinedOperator();
			}
			else if(check(IDENT) && env.hasMacro(currentToken.toString())) {
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
	
	private void textLine() { textLine(false); }
	
	
	
	private void handleDefinedOperator() {
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
			throw new PreprocessorInternalParseException();
		}
		
		String val = env.hasMacro(ident.toString()) ? "1" : "0"; //$NON-NLS-1$ //$NON-NLS-2$
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
			
			if(check(LPAREN)) { 
				next();
			}
			else { // then its not actually a call to the macro
				addToOutputStream(macroId);
				return;
			}
			
			replacement = macroCallFunctionLike(macroId, macro);
			if(replacement == null) // there was a problem parsing the macro invocation, abort
				return;
			
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
			inputTokenStream.pushMacroExpansionContext(replacement, expansionLocationOffset, null);
			// Record the offset of the expansion so that locations are resolved properly
			log.startMacroExpansion(macro, macroId.getStartOffset(), expansionLocationOffset);
			// log.endMacroExpansion() will be call when the last token in the replacement is output, see addToOutputStream()
			tokenExpansionMap.putMacro(replacement.last(), macroId, macro);
		}
	}

	
	/**
	 * Invokes a macro.
	 */
	private TokenList macroCallFunctionLike(IToken macroName, Macro macro) {
		// arguments to the macro
		List arguments = new Vector(); // List<TokenList> 
		int numParams = macro.getNumParams(); // number of parameters not including '...'
		TokenList arg = new TokenList();
		int parenLevel = 0; // used to track nested parenthesis
		
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
					encounterProblem(IASTProblem.PREPROCESSOR_MACRO_USAGE_ERROR, macroName);
					return null;
				}
				else if(check(HASH) || check(HASHHASH)) {
					encounterProblem(IASTProblem.PREPROCESSOR_MACRO_USAGE_ERROR, macroName);
					return null;
				}
				else if(check(NEWLINE)) {
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
		//final Callable process = new Callable() { public void call() { preprocessingFile(); } };
		List macroArguments = new Vector(arguments.size());
		
		for(int i = 0; i < arguments.size(); i++) {
			TokenList its = (TokenList) arguments.get(i);
			
			macroArguments.add(new MacroArgument(its,
				new MacroArgument.IProcessCallback() {
					public TokenList process(TokenList tokens) {
						return pushContextAndProcess(tokens, false, false);
					}
				}
			));
		}
		
		if(macro.isCorrectNumberOfArguments(macroArguments.size())) {
			TokenList expansion = macro.invoke(macroArguments);
			return expansion;
		}
		else {
			encounterProblem(IASTProblem.PREPROCESSOR_MACRO_USAGE_ERROR, macroName);
			return null;
		}
	}
	
	
	/**
	 * When the content assist offset is in a preprocessor directive
	 * this method is used to compute the prefix that will be used
	 * to create the completion token.
	 */
	private IToken findPrefixTokenOnCurrentLine() {
		int contentAssistOffset = inputTokenStream.getContentAssistOffset() - 1;
		while(!check(NEWLINE) && !done()) {
			
			// Do not adjust the offsets of the token,
			// this way we get the original offset in the working copy.
			IToken token = next(false);
			
			int endOffset = token.getEndOffset();
			
			if(token.getKind() == TK_identifier && endOffset == contentAssistOffset) {
				return token;
			}
			if(endOffset > contentAssistOffset) {
				return null;
			}
		}
		return null;
	}
	
	
	
	/**
	 * Handles preprocessor directives.
	 */
	private boolean controlLine() {
		IToken hash = next();
		int directiveStartOffset = hash.getStartOffset();
		
		if(inputTokenStream.isContentAssistOffsetReached()) {
			// handle completions on the hash #
			addCompletionTokenToOutputAndQuit(directiveStartOffset, DIRECTIVE_COMPLETION_TOKEN_PREFIX);
		}
		else if(inputTokenStream.isContentAssistOffsetOnCurrentLine()) {
			IToken token = findPrefixTokenOnCurrentLine();
			addCompletionTokenToOutputAndQuit(directiveStartOffset, token == null ? "" : token.toString()); //$NON-NLS-1$
		}
		else if(check(NEWLINE)) {
			next();
		}
		else if(check(DEFINE)) {
			next();
			defineDirective(hash.getStartOffset(), true);
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
			TokenList tokens = collectTokensUntilNewlineOrDone();
			tokens.addFirst(pragma); // just in case tokens is empty
			log.encounterPoundPragma(directiveStartOffset, tokens.last().getEndOffset() + 1);
			if(!done())
				expect(NEWLINE); 
		}
		else if(check(ERROR)) {
			IToken error = next();
			TokenList tokens = collectTokensUntilNewlineOrDone();
			tokens.addFirst(error); // just in case tokens is empty
			log.encounterPoundError(directiveStartOffset, tokens.last().getEndOffset() + 1);
			encounterProblem(IASTProblem.PREPROCESSOR_POUND_ERROR, tokens);
			if(!done())
				expect(NEWLINE);
		}
		else if(check(WARNING)) { // not in the spec, but why not?
			IToken error = next();
			TokenList tokens = collectTokensUntilNewlineOrDone();
			tokens.addFirst(error); // just in case tokens is empty
			log.encounterPoundError(directiveStartOffset, tokens.last().getEndOffset() + 1);
			encounterProblem(IASTProblem.PREPROCESSOR_POUND_WARNING, tokens);
			if(!done())
				expect(NEWLINE);
		}
		else { 
			IToken invalidDirective = next();
			encounterProblem(IASTProblem.PREPROCESSOR_INVALID_DIRECTIVE, invalidDirective);
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
			if(!done())
				expect(NEWLINE);
			
			takeIfBranch = isIfdef == env.hasMacro(ident.toString());
			
			int directiveEndOffset = ident.getEndOffset() + 1;
			if(isIfdef)
				log.encounterPoundIfdef(directiveStartOffset, directiveEndOffset, takeIfBranch);
			else
				log.encounterPoundIfndef(directiveStartOffset, directiveEndOffset, takeIfBranch);
		}
		else { // its an if
			IToken ifToken = next();
			TokenList expressionTokens = collectTokensUntilNewlineOrDone(); // TODO: what if expressionTokens is empty?
			int endOffset = 1 + (expressionTokens.isEmpty() ? ifToken.getEndOffset() : expressionTokens.last().getEndOffset());
			Integer value = evaluateConstantExpression(expressionTokens); // returns null if invalid expression
			
			if(value == null)
				encounterProblem(IASTProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR, directiveStartOffset, endOffset);
			
			if(!done())
				expect(NEWLINE);
			
			takeIfBranch = value != null && value.intValue() != 0;
			
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
	
	
	/**
	 * Evaluates the expression in an #if or #elif.
	 */
	private Integer evaluateConstantExpression(TokenList expressionTokens) {
		TokenList constantExpression;
		try {
			// First expand macros and handle the 'defined' operator
			constantExpression = pushContextAndProcess(expressionTokens, true, true);
		} catch(PreprocessorInternalParseException e) {
			return null;
		}
		
		C99ExprEvaluator evaluator = new C99ExprEvaluator(constantExpression);
		// if there is a problem during evaluation then evaluate() will return null
		return evaluator.evaluate();
	}
	
	
	private void elseGroups(boolean skipRest) {
		// TODO: check for wierd cases
		while(true) {
			IToken hash = expect(HASH);
			int directiveStartOffset = hash.getStartOffset();
			
			if(check(ENDIF)) {
				IToken endif = next();
				log.encounterPoundEndIf(directiveStartOffset, endif.getEndOffset()+1);
				expect(NEWLINE);
				return;
			}
			else if(check(ELIF)) {
				IToken elif = next();
				TokenList expressionTokens = collectTokensUntilNewlineOrDone();
				// TODO: maybe just create a problem instead
				int endOffset = 1 + (expressionTokens.isEmpty() ? elif.getEndOffset() : expressionTokens.last().getEndOffset());
				
				if(skipRest) {
					log.encounterPoundElif(directiveStartOffset, endOffset, false);
					skipGroup();
				}
				else if (expressionTokens == null || expressionTokens.isEmpty()) {
					// TODO: error
				}
				else {
					
					Integer value = evaluateConstantExpression(expressionTokens);
					if(value == null)
						encounterProblem(IASTProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR, elif.getStartOffset(), endOffset);
						
					boolean followElif = value != null && value.intValue() != 0;
					expect(NEWLINE);
					log.encounterPoundElif(directiveStartOffset, endOffset, followElif);
					if(followElif) {
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
				log.encounterPoundElse(directiveStartOffset, els.getEndOffset() + 1, !skipRest);
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


	private void includeDirective(int directiveStartOffset) {	
		// The include directive can contain a macro invocations
		//Callable processIncludeDirectiveBody = new Callable() { public void call() { textLine(false); } };
		TokenList includeBody = pushContextAndProcess(collectTokensUntilNewlineOrDone(), true, false);
		
		int directiveEndOffset = includeBody.last().getEndOffset() + 1;
		int nameStartOffset    = includeBody.first().getStartOffset() + 1;
		int nameEndOffset      = includeBody.last().getEndOffset();
		
		String fileName = computeIncludeFileName(includeBody);
		
		if(fileName == null) {
			encounterProblem(IASTProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, directiveStartOffset, directiveEndOffset);
			return;
		}
		assert fileName.length() >= 2;

		boolean local = fileName.startsWith("\""); //$NON-NLS-1$
		fileName = fileName.substring(1, fileName.length() - 1); // remove the double quotes or double angle brackets
		
		CodeReader reader = null;
		
		// attempt to find the file to include
		if(new File(fileName).isAbsolute() || fileName.startsWith("/")) { //$NON-NLS-1$
			reader = createCodeReader("", fileName); //$NON-NLS-1$
		}
		else if(local) { // local
			File currentDirectory = inputTokenStream.getCurrentDirectory();
			if(currentDirectory != null)
				reader = createCodeReader(currentDirectory.getAbsolutePath(), fileName);
		}
		else if(scanInfo != null) {
			String[] standardIncludePaths = scanInfo.getIncludePaths();
			if(standardIncludePaths != null) {
				for(int i = 0; i < standardIncludePaths.length; i++) {
					reader = createCodeReader(standardIncludePaths[i], fileName);
					if(reader != null)
						break;
				}
			}
		}
		
		if(reader == null) {
			encounterProblem(IASTProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, directiveStartOffset, directiveEndOffset);
		}
		else if(inputTokenStream.isCircularInclusion(reader)) {
			encounterProblem(IASTProblem.PREPROCESSOR_CIRCULAR_INCLUSION, directiveStartOffset, directiveEndOffset);
		}
		else {
			processIncludedFile(reader, directiveStartOffset, directiveEndOffset, nameStartOffset, nameEndOffset, fileName, false);
		}
	}
	
	
	/**
	 * Takes the token list that occurs as part of an #include directive
	 * and converts it into a string that should represent the file name	
	 * of the file that is to be included.
	 * 
	 * postcondition: result == null ^ result.length() >= 2
	 */
	private static String computeIncludeFileName(TokenList includeBody) {
		if(includeBody == null || includeBody.isEmpty() || includeBody.size() == 2) {
			return null;
		}
		// at this point the size == 1 or size >= 3
		
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


	/**
	 * Add external macro definitions given by IScannerInfo.getDefinedSymbols()
	 */
	private void addMacroDefinitions() {
		if(scanInfo == null)
			return;
		
		Map definedSymbols = scanInfo.getDefinedSymbols();
		if(definedSymbols == null)
			return;
		
		for(Iterator iter = definedSymbols.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			registerMacro((String)entry.getKey(), (String)entry.getValue());
		}
	}
	
	
	private void registerMacro(String signature, String expansion) {
		String define = signature + " " + expansion; //$NON-NLS-1$
		ILexer lexer = lexerFactory.createLexer(new CodeReader(define.toCharArray()));
		TokenList tokenList = lexer.lex();
		
		inputTokenStream.pushIsolatedContext(tokenList, null);
		
		// parse the macro as if it were created with a #define and add it to the macro environment
		Macro macro = defineDirective(0, false); // false means don't log the define with the LocationResolver
		
		System.out.println(Messages.getString("C99Preprocessor.3") + macro.getName()); //$NON-NLS-1$
		log.registerBuiltinMacro(macro);
		
		inputTokenStream.resume();
	}
	
	
	private CodeReader createCodeReader(String path, String fileName) {
		String finalPath = ScannerUtility.createReconciledPath(path, fileName);
		
		IMacroCollector indexMacroCollector = new IMacroCollector() {
			public void addDefinition(IMacro macro) {
				registerMacro(new String(macro.getSignature()), new String(macro.getExpansion()));
			}
		};
		
		return codeReaderFactory.createCodeReaderForInclusion(indexMacroCollector, finalPath);
	}
	
	/**
	 * Does not collect or discard the end of line token.
	 */
	private TokenList collectTokensUntilNewlineOrDone() {
		TokenList result = new TokenList();
		while(!check(NEWLINE) && !done()) {
			result.add(next());
		}
		return result;
	}
	
	
	/**
	 * Used to process include files and macro arguments and such.
	 * The Closure allows the actuall process to be provided, for example
	 * an include file will require that preprocessingFile() be called
	 * but a constant expression just requires that textLine() be called.
	 * 
	 * precondition:
	 *     singleLine == true implies that newInput does not contain any newline tokens
	 */
	private TokenList pushContextAndProcess(TokenList newInput, boolean singleLine, boolean handleDefined) {
		if(newInput == null || newInput.isEmpty())
			return new TokenList();
		
		TokenList savedArgumentOutputStream = argumentOutputStream;
		
		inputTokenStream.pushIsolatedContext(newInput, null); 
		argumentOutputStream = new TokenList(); // collect the output
		
		// textLine might return before the context is fully consumed
		// if textLine is called then newInput should not have any newlines that way the
		// entire context is consumed
		if(singleLine)
			textLine(handleDefined);
		else
			preprocessingFile();
		
		if(!inputTokenStream.isStuck())
			throw new PreprocessorInternalParseException(); // the context was not fully consumed
		
		// the token stream will get stuck, so unstick it
		inputTokenStream.resume();
		
		TokenList result = argumentOutputStream;
		argumentOutputStream = savedArgumentOutputStream;
		return result;
	}
	


	/**
	 * Creates a Macro object and stores it in the environment.
	 */
	private Macro defineDirective(int startOffset, boolean logMacro) {
		if(!check(IDENT)) {
			skipLine();
			encounterProblem(IASTProblem.PREPROCESSOR_INVALID_MACRO_DEFN, startOffset, inputTokenStream.getCurrentOffset());
			return null;
		}

		IToken macroName = next();
		Macro macro;

		// function like macro
		// There must not be any space between the macro name and the left paren.
		// This actually isn't in the spec but its how gcc behaves in C99 mode
		if(check(LPAREN) && inputTokenStream.peek().getStartOffset() == macroName.getEndOffset() + 1) { 
			next();

			String varArgParamName = null;
			LinkedHashSet paramNames = new LinkedHashSet(); // no duplicates, maintains insertion order
			IToken rparen = null; 
			boolean problem = false;
			
			if(check(RPAREN)) {
				rparen = next();
			}
			else {
				// parse the parameters
				while(true) { // proof of loop termination: each brach has a next() or break 
					if(check(IDENT)) {
						String paramName = next().toString();
						
						if(check(DOTDOTDOT)) {
							next();
							varArgParamName = paramName;
							if(check(RPAREN))
								rparen = next();
							else
								problem = true;
							break;
						}
						
						paramNames.add(paramName); // TODO: check for duplicate parameter name 
						
						if(check(COMMA)) {
							next();
						}
						else if(check(RPAREN)) {
							rparen = next();
							break;
						}
						else {
							problem = true;
							break;
						}
					}
					else if(check(DOTDOTDOT)) {
						next();
						varArgParamName = Macro.__VA_ARGS__;
						if(check(RPAREN))
							rparen = next();
						else
							problem = true;
						break;
					}
					else { // should handle the done() case
						problem = true;
						break;
					}
				}
			}
			
			if(problem) {
				encounterProblem(IASTProblem.PREPROCESSOR_INVALID_MACRO_DEFN, startOffset, inputTokenStream.getCurrentOffset());
				skipLine();
				return null;
			}
			
			// TODO replacement list cannot start or end with ##
			TokenList replacementList = replacementList(paramNames, varArgParamName);
			int endOffset = calculateMacroDefinitionEndOffset(rparen, replacementList);
			macro = new Macro(macroName, replacementList, startOffset, endOffset, paramNames, varArgParamName);
		}
		
		// object like macro
		else { 
			TokenList replacementList = replacementList(null, null);
			int endOffset = calculateMacroDefinitionEndOffset(macroName, replacementList);
			macro = new Macro(macroName, replacementList, startOffset, endOffset);
		}
		
		env.addMacro(macro);
		if(logMacro)
			log.defineMacro(macro);
		
		return macro;
	}
	
	
	private static int calculateMacroDefinitionEndOffset(IToken tokenBeforeReplacementList, TokenList replacementList) {
		if(replacementList == null || replacementList.isEmpty())
			return tokenBeforeReplacementList.getEndOffset() + 1;
		else
			return replacementList.last().getEndOffset() + 1;
	}
	
	
	private TokenList replacementList(Set paramNames, String varArgParamName) {
		TokenList tokens = new TokenList();
		
		while(!check(NEWLINE) && !done()) {
			IToken token = next();
			
			// Avoid accedental name capture by changing the token kind of parameters.
			// The token kind will be changed back to TK_identifier (or a keyword) before 
			// passed to the parser.
			if(isParamName(token, paramNames, varArgParamName)) {
				token.setKind(TK_Parameter);
			}
			
			tokens.add(token);
		}
		
		if(!done())
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
