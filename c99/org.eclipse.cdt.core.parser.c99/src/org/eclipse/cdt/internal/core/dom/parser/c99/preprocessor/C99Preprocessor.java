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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IMacroCollector;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.c99.ILexer;
import org.eclipse.cdt.core.dom.c99.ILexerFactory;
import org.eclipse.cdt.core.dom.c99.IPreprocessorTokenCollector;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99ExprEvaluator;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;
import org.eclipse.cdt.internal.core.parser.scanner2.ScannerASTProblem;
import org.eclipse.cdt.internal.core.parser.scanner2.ScannerUtility;

// TODO: prove that all loops terminate!
/**
 * The C99 preprocessor.
 * 
 */
public class C99Preprocessor implements C99Parsersym {

	public static final int OPTION_GENERATE_COMMENTS_FOR_ACTIVE_CODE = 1;
	public static final int OPTION_GENERATE_ALL_COMMENTS = 2;
	
	private static final String DEFINED_OPERATOR = "defined"; //$NON-NLS-1$
	private static final String DIRECTIVE_COMPLETION_TOKEN_PREFIX = "#"; //$NON-NLS-1$
	
	
	// The number of EndOfCompletion tokens to generate after
	// generating a Completion token.
	private static final int NUM_EOC_TOKENS = 20;
	
	//private final IKeywordMap keywordMap; // used to recognize certain identifiers as keywords 
	private final CodeReader codeReader; // The code reader that stores the source character stream of the file
	private final IScannerInfo scanInfo; // Used to resolve includes and get access to macro definitions
	private final ILexerFactory lexerFactory; // Used to create lexers for included files
	private final ICodeReaderFactory codeReaderFactory; // Used to get source character buffers of included files
	
	private final boolean generateAllComments;
	private final boolean generateActiveComments;
	
	private MacroEnvironment env; // Stores macro definitions created by #define
	private TokenList argumentOutputStream; // Used to collect the output when a macro argument is processed
	private InputTokenStream inputTokenStream; // stores tokens that have yet to be processed
	private IPreprocessorTokenCollector parser; // tokens are injected directly into the parser
	private IPreprocessorLog log; // resolves source offset location information
	private IToken lastTokenOutput = null; // used to detect adjacent string literals for concatenation
	private int lastKindOutput; // used to detect adjacent string literals for concatenation
	
	private boolean encounteredError = false;
	
	private static class PreprocessorInternalParseException extends RuntimeException {
		public PreprocessorInternalParseException() {}
		public PreprocessorInternalParseException(String message) { super(message); }
	}
	
	private static class PreprocessorAbortParseException extends RuntimeException {}
	
	// Used to keep log events from firing when processing macro arguments
	private int macroInvokationDepth = 0;
	
	
	/**
	 * Creates a preprocessor using the given keywords.
	 */
	public C99Preprocessor(ILexerFactory lexerFactory, 
			CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, int options) {
		
		this.codeReader = reader;
		this.scanInfo = scanInfo;
		this.lexerFactory = lexerFactory;
		this.codeReaderFactory = fileCreator;
		
		this.generateActiveComments = (options & OPTION_GENERATE_COMMENTS_FOR_ACTIVE_CODE) != 0;
		this.generateAllComments    = (options & OPTION_GENERATE_ALL_COMMENTS) != 0;
	}
	
	
	/**
	 * Returns true iff an error was encountered during preprocessing.
	 */
	public boolean encounteredError() {
		return encounteredError;
	}
	
	
	/**
	 * Run the preprocessor, the resulting token stream will be injected directly
	 * into the given parser.
	 * 
	 * @return null if preprocessing fails
	 * @throws IllegalArgumentException if parser is null
	 */
	public synchronized void preprocess(IPreprocessorTokenCollector parser, IPreprocessorLog log) {
		if(parser == null)
			throw new IllegalArgumentException(Messages.getString("C99Preprocessor.0")); //$NON-NLS-1$
		
		preprocess(parser, log, new InputTokenStream(parser));
	}
	
	
	/**
	 * Run the preprocessor in contentAssistMode, the resulting token stream will be injected directly
	 * into the given parser.
	 * 
	 * @return null if preprocessing fails
	 * @throws IllegalArgumentException if contentAssistOffset < 0
	 * @throws IllegalArgumentException if parser is null
	 */
	public synchronized void preprocess(IPreprocessorTokenCollector parser, IPreprocessorLog log, int contentAssistOffset) {
		if(parser == null)
			throw new IllegalArgumentException(Messages.getString("C99Preprocessor.0")); //$NON-NLS-1$
		if(contentAssistOffset < 0)
			throw new IllegalArgumentException(Messages.getString("C99Preprocessor.2")); //$NON-NLS-1$
		
		InputTokenStream inputTokenStream = new InputTokenStream(parser);
		inputTokenStream.setContentAssistOffset(contentAssistOffset);
		preprocess(parser, log, inputTokenStream);
		
	}
	
	
	private synchronized void preprocess(IPreprocessorTokenCollector parser, IPreprocessorLog log, InputTokenStream inputTokenStream) {
		assert inputTokenStream != null;
		
		this.log = log;
		this.parser = parser;
		this.env = new MacroEnvironment();
		this.inputTokenStream = inputTokenStream;
		
		try {
			inputTokenStream.setCollectCommentTokens(generateActiveComments | generateAllComments);
			
			// add external macro definitions given by IScannerInfo.getDefinedSymbols()
			addMacroDefinitions();
			// LPG requires that the parse stream must start with a dummy token
			parser.addToken(C99Token.DUMMY_TOKEN); 
	
			if(log != null)
				log.startTranslationUnit(codeReader);
			
			
			TokenList tokenList = lex(codeReader);
			inputTokenStream.pushIncludeContext(tokenList, codeReader, 0, false, null);
			
			processExtendedScannerInfoMacrosAndIncludes(); // add any files that should be manually included for tests
			
			process(); // throws PreprocessorAbortParseException
			
			int tuSize = inputTokenStream.getTranslationUnitSize();
			parser.addToken(new C99Token(tuSize, tuSize, TK_EOF_TOKEN, "<EOF>")); //$NON-NLS-1$
			
			if(log != null)
				log.endTranslationUnit(tuSize);
		}
		catch(PreprocessorAbortParseException e) {
			encounteredError = true;
		}
		finally {
			// clean up
			this.parser = null;
			this.inputTokenStream = null;
			this.log = null;
			this.env = null;
			this.lastTokenOutput = null;
			this.macroInvokationDepth = 0;
		}
	}
	
	
	/**
	 * I believe that IExtendedScannerInfo is mainly used by the tests
	 * to manually include files for testing.
	 */
	private void processExtendedScannerInfoMacrosAndIncludes() {
		if(scanInfo instanceof IExtendedScannerInfo) {
			IExtendedScannerInfo einfo = (IExtendedScannerInfo) scanInfo;
			addExtendedScannerInfoIncludes(einfo.getMacroFiles());
			addExtendedScannerInfoIncludes(einfo.getIncludeFiles());
		}
	}

	/**
	 * Adds the included files to the input stream. Does not fire
	 * log callbacks and treats each file as if it had a length
	 * of 0. This should only be used for testing.
	 */
	private void addExtendedScannerInfoIncludes(String[] paths) {
		if(paths == null)
			return;
		
		for(int i = 0; i < paths.length; i++) {
			CodeReader cr = codeReaderFactory.createCodeReaderForInclusion(null, paths[i]);
			if(cr != null) {
				int offset = inputTokenStream.getCurrentOffset();
				
				// The tokens from the code reader will be isolated on the inputTokenStream, 
				// this means that preprocessingFile() will return when the tokens are consumed.
				addIncludedFileToInputStream(cr, offset, offset, offset, offset, "", true, true);//$NON-NLS-1$
				process();
				inputTokenStream.resume();
			}
		}
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
	
	
	/**
	 * Lexes and saves an external macro definition (given by the toolchain) into
	 * the macro environment.
	 */
	private void registerMacro(String signature, String expansion) {
		String define = signature + " " + expansion; //$NON-NLS-1$
		TokenList tokenList = lex(new CodeReader(define.toCharArray()));
		
		inputTokenStream.pushIsolatedContext(tokenList, null);
		
		// parse the macro as if it were created with a #define and add it to the macro environment
		Macro macro = defineDirective(0, false); // false means don't log the define with the LocationResolver
		if(macro != null)
			log.registerBuiltinMacro(macro);
		
		inputTokenStream.resume();
	}
	
	
	
	/**
	 * Convert the buffer in the CodeReader into a list of tokens.
	 */
	private TokenList lex(CodeReader codeReader) {
		ILexer lexer = lexerFactory.createLexer(codeReader);
		boolean generateComments = generateActiveComments | generateAllComments;
		int lexerOptions = generateComments ? ILexer.OPTION_GENERATE_COMMENT_TOKENS : 0;
		return lexer.lex(lexerOptions);
	}
	
	
	/**
	 * Creates a CodeReader object for an #include directive.
	 */
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
	 * Recursively processes a file included by an #include directive.
	 */
	private void addIncludedFileToInputStream(final CodeReader reader, int directiveStartOffset, int directiveEndOffset, 
			                         int nameStartOffset, int nameEndOffset, String name, boolean systemInclude, boolean isolated) {
		
		TokenList tokens = lex(reader);
		
		if(log != null)
			log.startInclusion(reader, directiveStartOffset, directiveEndOffset, nameStartOffset, 
				           nameEndOffset, name.toCharArray(), systemInclude);
		
		// This code gets fired when the tokens that make up the included file get used up
		InputTokenStream.IIncludeContextCallback callback = null;
		if(log != null) {
			callback = new InputTokenStream.IIncludeContextCallback() {
				public void contextClosed() {
					log.endInclusion(reader, inputTokenStream.adjust(reader.buffer.length));
				}
			};
		}
		
		inputTokenStream.pushIncludeContext(tokens, reader, directiveEndOffset, isolated, callback);
	}
	
	
	public String toString() {
		return inputTokenStream.toString();
	}
	
	
	
	/**
	 * Injects the given token into the parser, or int the case that a macro argument
	 * is being processed the token is collected in the argumentOutputStream instance variable.
	 */
	private void addToOutputStream(IToken t) {
		if(argumentOutputStream != null) {
			argumentOutputStream.add(t);
			return;
		}
		
		// Filter what tokens the parser sees, for example newline tokens are removed
		switch(t.getKind()) {
			case TK_NewLine:       // the parser does not want to see newline or placemarker tokens
			case TK_PlaceMarker:
				return;
			case TK_DisabledMacroName: // disabled macro names should be treated as regular identifiers 
				t.setKind(TK_identifier);
				break;
			case TK_Parameter:
				assert false; //all macro parameters should have already been replaced
		}
		
		//C99Token toOutput = new C99Token(t); // why create a new object?
		IToken toOutput = t;
		
		// TODO lastTokenOutput probably isn't the best way of handling this
		// the parser may have already altered lastTokenOutput
		
		// concatenate adjacent string literals
		if(lastTokenOutput != null &&  t.getKind() == TK_stringlit && lastKindOutput == TK_stringlit) {
			String s1 = lastTokenOutput.toString();
			String s2 = toOutput.toString();
			assert s1.length() >=2 && s2.length() != 2; // smallest string literal is ""
			
			String rep = s1.substring(0, s1.length()-1) + s2.substring(1);
			((C99Token)lastTokenOutput).setRepresentation(rep);
			lastTokenOutput.setEndOffset(toOutput.getEndOffset());
			// don't send the result to the parser
		}
		else {
			lastTokenOutput = toOutput;
			lastKindOutput = toOutput.getKind();
			//System.out.println("Token: (" + toOutput.getKind() + ", " + toOutput.getStartOffset() + ", " + toOutput.getEndOffset() + ") " + toOutput);
			parser.addToken(toOutput);
		}
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
		IF      = "if",      //$NON-NLS-1$
		IFDEF   = "ifdef",   //$NON-NLS-1$
		IFNDEF  = "ifndef",  //$NON-NLS-1$
		ELIF    = "elif",    //$NON-NLS-1$
		ELSE    = "else",    //$NON-NLS-1$
		ENDIF   = "endif",   //$NON-NLS-1$
		DEFINE  = "define",  //$NON-NLS-1$
		UNDEF   = "undef",   //$NON-NLS-1$
		INCLUDE = "include", //$NON-NLS-1$
		PRAGMA  = "pragma",  //$NON-NLS-1$
		ERROR   = "error",   //$NON-NLS-1$
		WARNING = "warning"; //$NON-NLS-1$
	


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
			throw new PreprocessorInternalParseException(inputTokenStream.getCurrentFileName());
	}
	

	private IToken expect(String directive) {
		if(check(directive))
			return next();
		else 
			throw new PreprocessorInternalParseException(inputTokenStream.getCurrentFileName());
	}
	
	
	/**
	 * Moves to the next token on the input stream.
	 */
	private IToken next() {
		return inputTokenStream.next(true);
	}

	private IToken next(boolean adjust) {
		return inputTokenStream.next(adjust);
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
		problem.setOffsetAndLength(startOffset, (endOffset - startOffset) + 1);
		if(log != null)
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
	private void process() {
		while(!done()) {
			try {
				if(check(HASH)) {
					boolean encounteredPoundElse = controlLine();
					if(encounteredPoundElse) {
						// improperly nested #else
						
						throw new PreprocessorInternalParseException(inputTokenStream.getCurrentFileName());
					}
				}
				else {
					textLine();
				}
				
			} catch(PreprocessorInternalParseException e) {
				//e.printStackTrace();
				if(inputTokenStream.isContentAssistMode())
					throw new PreprocessorAbortParseException();
				
				
				// log the error, and try to synchronize on the next newline
				if(done()) {
					int offset = inputTokenStream.getCurrentOffset();
					encounterProblem(IASTProblem.SYNTAX_ERROR, offset, offset);
				}
				else {
					if(check(NEWLINE)) { 
						IToken token = next();
						encounterProblem(IASTProblem.SYNTAX_ERROR, token);
						return;
					}
					
					IToken token = next();
					encounterProblem(IASTProblem.SYNTAX_ERROR, token);
					
					while(!(check(NEWLINE) || done())) {
						token = next();
					}
					if(check(NEWLINE))
						token = next();
				}
			}
		}
	}
	
	/**
	 * The difference is this one stops when it hits an #else or equivalent.
	 */
	private int processBranch() {
		while(!done()) {
			if(check(HASH)) {
				int hashOffset = inputTokenStream.adjust(inputTokenStream.peek().getStartOffset());
				
				// Returns when an #else, #elif or #endif is encountered.
				// This is because ifSection() calls processBranch() to handle a branch of an #if
				boolean encounteredPoundElse = controlLine();
				if(encounteredPoundElse)
					return hashOffset;
			}
			else {
				textLine();
			}
		}
		throw new PreprocessorInternalParseException(inputTokenStream.getCurrentFileName()); // caused by improperly nested #ifs
	}
	
	
	/**
	 * Skips a branch of a conditional compiliation construct
	 */
	private int skipBranch() {
		if(!generateAllComments)
			inputTokenStream.setCollectCommentTokens(false);
		 
		int depth = 0; // for proper nesting
		
		while(!done()) {
			if(check(HASH)) {
				IToken hash = next();
				int directiveStartOffset = hash.getStartOffset();
				if(check(INCLUDE)) { // show inactive includes in the outline view
					next();
					includeDirective(directiveStartOffset, false);
				}
				else if(check(IF)) {
					handleIf(directiveStartOffset, false);
					depth++;
				}
				else if(check(IFDEF) || check(IFNDEF)) {
					handleIfDef(directiveStartOffset);
					depth++;
				}
				else if (check(ELIF)) {	
					if(depth == 0)
						return directiveStartOffset;
					handleIf(directiveStartOffset, false);
				}
				else if(check(ELSE)) {
					if(depth == 0)
						return directiveStartOffset;
					handleElse(directiveStartOffset, false);
				}
				else if(check(ENDIF)) {
					if(depth == 0) {
						inputTokenStream.setCollectCommentTokens(generateActiveComments || generateAllComments);
						return directiveStartOffset;
					}
					else {
						depth--;
						handleEndif(directiveStartOffset);
					}
				}
			}
			else {
				skipLine();
			}
		}
		
		throw new PreprocessorInternalParseException(inputTokenStream.getCurrentFileName()); // caused by improperly nested #ifs
	}
	
	String spaces(int x) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < x; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}
	
	/**
	 * Skips input until a newline is encountered, the newline is also skipped.
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
			
			completionToken = new C99Token(newStartOffset, newEndOffset, TK_Completion, prefix);
			//completionToken.setRepresentation(prefix);
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
		inputTokenStream = new InputTokenStream(parser);
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
		if(check(LPAREN)) {
			next();
			ident = expect(IDENT);
			expect(RPAREN);
		}
		else if(check(IDENT)) {
			ident = next();
		}
		else {
			throw new PreprocessorInternalParseException(inputTokenStream.getCurrentFileName());
		}
		
		String val = env.hasMacro(ident.toString()) ? "1" : "0"; //$NON-NLS-1$ //$NON-NLS-2$
		// TODO: what about the offsets (they probably don't matter)
		addToOutputStream(new C99Token(0, 0, TK_integer, val));
	}

	
	/**
	 *  A MacroExpansionCallback will be stored on the InputTokenStream context stack
	 *  for the result of a macro expansion. This object contains information that 
	 *  is necessary to support the case when macro invokations overlap.
	 *  Example:
	 *  #define PLUS5(x) (x+5) 
     *  #define FUNCTION PLUS5 
     *  int var = FUNCTION(1)
	 *  
	 *  In order to support "overlapping macros" we need a way to detect
	 *  a macro boundary cross, meaning that a macro context gets popped
	 *  while collecting the tokens that make up the next macro's arguments.
	 */
	private class MacroExpansionCallback implements InputTokenStream.IIncludeContextCallback {
		boolean popped = false; // we just need to know if the context was popped
		boolean disabled = false; // when set to true calling the contextClosed() event does not affect the log
		int directiveStartOffset;
		int directiveEndOffset;
		int replacementLength;
		int expansionEndOffset;
		Macro macro;

		MacroExpansionCallback(Macro macro, int directiveStartOffset, int directiveEndOffset, int replacementLength) {
			this.directiveStartOffset = directiveStartOffset;
			this.directiveEndOffset = directiveEndOffset;
			this.replacementLength = replacementLength;
			this.macro = macro;
		}
		
		public void startExpansion(char[][] args) {
			if(log != null && macroInvokationDepth == 0)
				log.startMacroExpansion(macro, directiveStartOffset, directiveEndOffset, args);
		}
		
		public void contextClosed() {
			if(!popped)
				expansionEndOffset = inputTokenStream.adjust(replacementLength) + 1;
			popped = true;
			
			if(disabled)
				return;
			
			if(log != null && macroInvokationDepth == 0)
				log.endMacroExpansion(macro, expansionEndOffset);
		}
		
		/**
		 * Should only be called when a macro overlap occurs.
		 */
		public void macroOverlapClose() {
			if(log != null && macroInvokationDepth == 0)
				log.endMacroExpansion(macro, directiveEndOffset);
		}
		
		public int getSourceLength() {
			return (directiveEndOffset - directiveStartOffset) + 1;
		}
	}
	

	/**
	 * Parses a macro invocation, either object like of function like.
	 */
	private void macroCall(final Macro macro) {
		IToken macroId = next();
		int startOffset = macroId.getStartOffset();
		int expansionLocationOffset;
		TokenList replacement;
		char[][] argsAsChar = null;
		
		if(macro.isObjectLike()) {
			replacement = invoke(macro, null);			
			if(replacement == null)
				encounterProblem(IASTProblem.PREPROCESSOR_MACRO_USAGE_ERROR, macroId);
			expansionLocationOffset = macroId.getEndOffset() + 1;
		}
		else { // function like
			MacroExpansionCallback callback = null;
			if(inputTokenStream.inMacroExpansionContext()) { 
				// get the callback that is already on the context stack
				callback = (MacroExpansionCallback)inputTokenStream.getCurrentContextCallback();
				// we don't want contextClosed() to fire automatically
				callback.disabled = true;
			}
			
			// Any number of newlines are allowed between the macro name and the args.
			// If there is a preprocessing directive then the result is undefined.
			while(check(NEWLINE))
				next();
			
			// We need to check for the macro's arguments, but it is legal for the name of
			// a function-like macro to exist in the source without any arguments being passed,
			// in this case it is not actually a call to the macro. Therefore, in this case,
			// we must explicity call contextClosed() because the callback was disabled above.
			if(check(LPAREN)) { 
				next();
			}
			else {
				
				// the event should have fired, we blocked it just in case, but it should have fired
				if(callback != null && callback.popped && callback.disabled) {
					callback.disabled = false;
					callback.contextClosed();
				}
				addToOutputStream(macroId);
				return;
			}
			
			List/*<MacroArgument>*/ arguments = collectArgumentsToFunctionLikeMacro(macroId, macro);
			if(arguments == null) {
				encounterProblem(IASTProblem.PREPROCESSOR_MACRO_USAGE_ERROR, macroId);
				return;
			}
				
			argsAsChar = convertArgsToCharArrays(arguments);
			replacement = invoke(macro, arguments);
			
			if(replacement == null) {
				encounterProblem(IASTProblem.PREPROCESSOR_MACRO_USAGE_ERROR, macroId);
				return;
			}
			
			// the right paren will not be consumed by collectArgumentsToFunctionLikeMacro(), because we need it here
			IToken rparen = next(); 
			expansionLocationOffset = rparen.getEndOffset() + 1;
			
			if(callback != null)
				callback.disabled = false;
			
			// test if a boundary was crossed, and if so make an adjustment to the current offsets
			if(callback != null && callback.popped) {
				callback.macroOverlapClose(); 
				int replacementLength = callback.expansionEndOffset - callback.directiveEndOffset;
				startOffset = callback.directiveStartOffset;
				expansionLocationOffset -= replacementLength;
				inputTokenStream.adjustGlobalOffsets(-replacementLength);
			}
		}
		
		
		if(replacement == null || replacement.isEmpty()) {
			if(log != null) {
				log.startMacroExpansion(macro, startOffset, expansionLocationOffset, argsAsChar);
				log.endMacroExpansion(macro, expansionLocationOffset);
			}
		}
		else {
			int replacementLength = replacement.last().getEndOffset();
			MacroExpansionCallback callback = 
				new MacroExpansionCallback(macro, startOffset, expansionLocationOffset, replacementLength);
			callback.startExpansion(argsAsChar);
			
			// Add the result of the macro expansion to the input so it can be rescanned with the rest of the file
			inputTokenStream.pushMacroExpansionContext(replacement, expansionLocationOffset, callback, macroInvokationDepth == 0);
		}
	}

	
	/**
	 * A counter is updated every time a macro is invoked.
	 * If macroInvokationDepth > 0 then we are expanding a macro while a macro is already
	 * expanding. This occurs when macro arguments are recursively processed.
	 * The counter is used to make sure that log events are not fired while processing
	 * macro arguments.
	 */
	private TokenList invoke(Macro macro, List/*<MacroArgument>*/ arguments) {
		macroInvokationDepth++;
		TokenList result = macro.invoke(arguments);
		macroInvokationDepth--;
		return result;
	}
	
	
	/**
	 * Converts a list of MacroArguments to char[][] representing the actual
	 * source text of the arguments. 
	 */
	private static char[][] convertArgsToCharArrays(List arguments) {
		char[][] chars = new char[arguments.size()][];
		
		for(int i = 0; i < arguments.size(); i++) {
			MacroArgument arg = (MacroArgument) arguments.get(i);
			chars[i] = arg.getRawTokens().toString().toCharArray();
		}
		return chars;
	}
	
	
	/**
	 * Parses the arguments to a function-like macro and returns
	 * them as a List of MacroArgument objects.
	 */
	private List collectArgumentsToFunctionLikeMacro(IToken macroName, Macro macro) {
		// arguments to the macro
		List arguments = new ArrayList(); 
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
					return null;
				}
				else if(check(HASH) || check(HASHHASH)) {
					return null;
				}
				else if(check(NEWLINE)) {
					// newlines are allowed within a macro invocation, they are ignored
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
		
		List macroArguments = new ArrayList(arguments.size());
		
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
			return macroArguments;
		}
		else {
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
		else if(check(DEFINE)) { // TODO: check the rules of macro redefinition?
			next();
			defineDirective(directiveStartOffset, true);
		}
		else if(check(UNDEF)) {
			next();
			IToken macroName = expect(IDENT);
			String name = macroName.toString();
			if(log != null)
				log.undefineMacro(directiveStartOffset, macroName.getEndOffset() + 1, name, macroName.getStartOffset());
			env.removeMacro(name);
			if(!done())
				expect(NEWLINE);
		}
		else if(check(INCLUDE)) {
			next();
			includeDirective(directiveStartOffset, true);
		}
		else if(check(IF) || check(IFDEF) || check(IFNDEF)) {
			ifSection(directiveStartOffset);
		}
		else if(check(ELIF) || check(ELSE) || check(ENDIF)) {
			return true;
		}
		else if(check(PRAGMA) || check(ERROR) || check(WARNING)) {
			boolean isPragma  = check(PRAGMA);
			boolean isError   = check(ERROR);
			
			IToken token = next();
			TokenList tokens = collectTokensUntilNewlineOrDone();
			int endOffset = 1 + (tokens.isEmpty() ? token.getEndOffset() : tokens.last().getEndOffset());
			char[] body = tokens.toString().toCharArray();
			
			if(isPragma) {
				if(log != null) log.encounterPoundPragma(directiveStartOffset, endOffset, body);
			}
			else if(isError) {
				if(log != null) log.encounterPoundError(directiveStartOffset, endOffset, body);
				encounterProblem(IASTProblem.PREPROCESSOR_POUND_ERROR, tokens);
			}
			else {
				if(log != null) log.encounterPoundError(directiveStartOffset, endOffset, body);
				encounterProblem(IASTProblem.PREPROCESSOR_POUND_WARNING, tokens);
			}
			
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
	
	
	/**
	 * Evaluates an #if, #ifdef or #ifndef.
	 * Can't return from this until the entire if-group is processed.
	 */
	private void ifSection(int directiveStartOffset) {
		// Determine if the branch should be followed
		boolean takeIfBranch;
		if(check(IFDEF) || check(IFNDEF))
			takeIfBranch = handleIfDef(directiveStartOffset);
		else // must be an if
			takeIfBranch = handleIf(directiveStartOffset, true);
		
		// processBranch() will return when it hits an elif, else or endif
		int hashOffset = takeIfBranch ? processBranch() : skipBranch();
		elseGroups(takeIfBranch, hashOffset);
	}
	
	
	
	private void elseGroups(boolean skipRest, int hashOffset) {
		while(!done()) {
			if(check(ENDIF)) {
				handleEndif(hashOffset);
				return;
			}
			else if(check(ELIF)) {
				boolean followBranch = handleIf(hashOffset, !skipRest);
				if(followBranch) {
					skipRest = true;
					hashOffset = processBranch();
				} 
				else {
					hashOffset = skipBranch();
				}
			}
			else if(check(ELSE)) {
				handleElse(hashOffset, !skipRest);
				if(skipRest) 
					hashOffset = skipBranch(); 
				else 
					hashOffset = processBranch();
			}
			else {
				throw new PreprocessorInternalParseException(inputTokenStream.getCurrentFileName());
			}
		}
	}
	

	/**
	 * Determines if the first brach of an #ifdef or #ifndef should be followed.
	 */
	private boolean handleIfDef(int directiveStartOffset) {
		boolean isIfdef = check(IFDEF);
		next();
		IToken ident = expect(IDENT);
		if(!done())
			expect(NEWLINE);
		
		boolean takeIfBranch = isIfdef == env.hasMacro(ident.toString());

		if(log != null) {
			int directiveEndOffset = ident.getEndOffset() + 1;
			char[] condition = ident.toString().toCharArray();
			if(isIfdef)
				log.encounterPoundIfdef(directiveStartOffset, directiveEndOffset, takeIfBranch, condition);
			else
				log.encounterPoundIfndef(directiveStartOffset, directiveEndOffset, takeIfBranch, condition);
		}
		
		
		return takeIfBranch;
	}
	
	
	/**
	 * Determines if the first branch of an #if should be followed.
	 * @param evaluate If true will evaluate the condition in the #if
	 */
	private boolean handleIf(int directiveStartOffset, boolean evaluate) {
		boolean isIf = check(IF); // are we in an #if or an #elif
		
		boolean followBranch;
		IToken ifToken = next();
		TokenList expressionTokens = collectTokensUntilNewlineOrDone();
		char[] expressionChars = expressionTokens.toString().toCharArray(); // if empty toString() will return ""
		int endOffset = 1 + (expressionTokens.isEmpty() ? ifToken.getEndOffset() : expressionTokens.last().getEndOffset());
		
		Long value;
		if(evaluate)
			value = evaluateConstantExpression(expressionTokens); // returns null if invalid expression
		else
			value = new Long(0L);

		if(value == null)
			encounterProblem(IASTProblem.PREPROCESSOR_CONDITIONAL_EVAL_ERROR, directiveStartOffset, endOffset);
		
		if(!done())
			expect(NEWLINE);
		
		// will be false if evaluate is false
		followBranch = value != null && value.longValue() != 0;
		
		if(log != null) {
			if(isIf)
				log.encounterPoundIf(directiveStartOffset, endOffset, followBranch, expressionChars);
			else
				log.encounterPoundElif(directiveStartOffset, endOffset, followBranch, expressionChars);
		}
		
		return followBranch;
	}
	
	
	/**
	 * Evaluates the expression in an #if or #elif.
	 * @return null if the expression could not be evaluated
	 */
	private Long evaluateConstantExpression(TokenList expressionTokens) {
		if(expressionTokens == null || expressionTokens.isEmpty())
			return null;
		
		TokenList constantExpression;
		try {
			// macro expansions that occur during the processing of the #if's body don't matter
			if(log != null)
				log.setIgnoreMacroExpansions(true);
			
			// First expand macros and handle the 'defined' operator
			constantExpression = pushContextAndProcess(expressionTokens, true, true);
			
			// macro expansions that occur during the processing of the #include's body don't matter
			if(log != null)
				log.setIgnoreMacroExpansions(false);
			
		} catch(PreprocessorInternalParseException e) {
			return null;
		}
		
		C99ExprEvaluator evaluator = new C99ExprEvaluator(constantExpression);
		// if there is a problem during evaluation then evaluate() will return null
		return evaluator.evaluate();
	}
	
	private void handleElse(int directiveStartOffset, boolean taken) {
		IToken els = next();
		if(log != null)
			log.encounterPoundElse(directiveStartOffset, els.getEndOffset()+1, taken);
	}
	
	
	private void handleEndif(int directiveStartOffset) {
		IToken endif = next();
		if(log != null)
			log.encounterPoundEndIf(directiveStartOffset, endif.getEndOffset()+1);
		if(!done())
			expect(NEWLINE);
	}
		
	
	


	
	/**
	 * Retrieve the source code for the file that is referenced by the #include directive and process it.
	 */
	private void includeDirective(int directiveStart, boolean active) {	
		// The include directive can contain a macro invocations
		TokenList tokens = collectTokensUntilNewlineOrDone();
		
		if(tokens.isEmpty()) {
			int code = IASTProblem.PREPROCESSOR_INCLUSION_NOT_FOUND;
			encounterProblemInclude(code, directiveStart, directiveStart, directiveStart, directiveStart, null, false);
			return;
		}
		
		// calculate the offsets of the include directive in the source
		int fileNameStart, fileNameEnd, directiveEnd;
		
		// if its a regular include like <filename.h> or "filename.h" then ignore the < > and " in the offsets
		if((tokens.first().getKind() == TK_LT && tokens.last().getKind() == TK_GT) ||
		   (tokens.size() == 1 && tokens.first().getKind() == TK_stringlit)) {
			
			fileNameStart = tokens.first().getStartOffset() + 1;
			fileNameEnd   = tokens.last().getEndOffset();
			directiveEnd  = fileNameEnd + 1;
		}
		else { // otherwise its probably a macro invocation in the include body
			fileNameStart = tokens.first().getStartOffset() ;
			fileNameEnd   = tokens.last().getEndOffset() + 1;
			directiveEnd  = fileNameEnd;
		}
		
		// macro expansions that occur during the processing of the #include's body don't matter
		if(log != null)
			log.setIgnoreMacroExpansions(true);
		
		TokenList includeBody = pushContextAndProcess(tokens, true, false);
		String fileName = computeIncludeFileName(includeBody);
		
		if(log != null)
			log.setIgnoreMacroExpansions(false);
		
		if(fileName == null) {
			int code = IASTProblem.PREPROCESSOR_INCLUSION_NOT_FOUND;
			encounterProblemInclude(code, directiveStart, directiveEnd, fileNameStart, fileNameEnd, fileName, false);
			return;
		}
		
		assert fileName.length() >= 2;
		boolean local = fileName.startsWith("\""); //$NON-NLS-1$
		fileName = fileName.substring(1, fileName.length() - 1); // remove the double quotes or double angle brackets
		
		
		if(!active) { // the include statement is inactive, just log its presence and return
			if(log != null) {
				log.encounterPoundInclude(directiveStart, fileNameStart, fileNameEnd, directiveEnd, 
					                      fileName.toCharArray(), !local, false);
			}
			return;
		}
		
		
		CodeReader reader = computeCodeReaderForInclusion(fileName, local);
		
		if(reader == null || inputTokenStream.isCircularInclusion(reader)) {
			int problemCode = (reader == null) ? IASTProblem.PREPROCESSOR_INCLUSION_NOT_FOUND : IASTProblem.PREPROCESSOR_CIRCULAR_INCLUSION;
			encounterProblemInclude(problemCode, directiveStart, directiveEnd, fileNameStart, fileNameEnd, fileName, !local);
		}
		else {
			addIncludedFileToInputStream(reader, directiveStart, directiveEnd, fileNameStart, fileNameEnd, fileName, !local, false);
		}
	}


	/**
	 * Creates a CodeReader object for the given file name.
	 */
	private CodeReader computeCodeReaderForInclusion(String fileName, boolean local) {
		CodeReader reader = null;
		
		// attempt to find the file to include
		if(new File(fileName).isAbsolute() || fileName.startsWith("/")) { //$NON-NLS-1$
			reader = createCodeReader("", fileName); //$NON-NLS-1$
		}
		else if(local) {
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
		
		return reader;
	}
	
	
	/**
	 * Called when there is a problem with an #include directive.
	 */
	private void encounterProblemInclude(int problemCode, int directiveStart, int directiveEnd, int nameStart, int nameEnd,
			                             String fileName, boolean systemInclude) {
		
		encounterProblem(problemCode, directiveStart, directiveEnd - 1);
		
		// Log the fact that an include was encountered
		if(log != null) {
			char[] chars = fileName == null ? null : fileName.toCharArray();
			log.encounterPoundInclude(directiveStart, nameStart, nameEnd, directiveEnd, chars, systemInclude, true);
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
		
		if(includeBody.size() == 1) {
			IToken token = includeBody.first();
			if(token.getKind() == TK_stringlit) // local include
				return token.toString();
			else
				return null;
		}

		// at this point the size must be at least 3
		IToken first = includeBody.first();
		IToken last  = includeBody.last();
		if(first.getKind() != TK_LT || last.getKind() != TK_GT) {
			return null;
		}

		return includeBody.toString();
	}
	
	
	/**
	 * Does not collect or discard the end of line token.
	 * @return Does not return null, will return empty TokenList if no tokens are collected.
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
			process();
		
		// The following 3 lines of code aren't really necessary, they are just a sanity check
		inputTokenStream.peek();
		if(!inputTokenStream.isStuck())
			throw new PreprocessorInternalParseException(inputTokenStream.getCurrentFileName()); // the context was not fully consumed
		
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
			IToken badToken = next(); // fix for testcase DOMLocationTests.test162180_2()
			encounterProblem(IASTProblem.PREPROCESSOR_INVALID_MACRO_DEFN, badToken.getStartOffset(), badToken.getEndOffset());
			skipLine();
			return null;
		}

		IToken macroName = next();
		Macro macro;

		// function like macro
		// There must not be any space between the macro name and the left paren.
		// This actually isn't in the spec but its how gcc behaves in C99 mode
		if(check(LPAREN) && inputTokenStream.getCurrentOffset() == macroName.getEndOffset() + 1) { 
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
			
			// replacementList will contain the newline
			TokenList replacementList = replacementListTokens(paramNames, varArgParamName);
			
			if(startsOrEndsWithHashHash(replacementList)) {
				encounterProblem(IASTProblem.PREPROCESSOR_MACRO_PASTING_ERROR, startOffset, inputTokenStream.getCurrentOffset());
				skipLine();
				return null;
			}

			int endOffset = calculateMacroDefinitionEndOffset(rparen, replacementList);
			macro = new Macro(macroName, replacementList, startOffset, endOffset, paramNames, varArgParamName);
		}
		
		// object like macro
		else { 
			TokenList replacementList = replacementListTokens(null, null);
			int endOffset = calculateMacroDefinitionEndOffset(macroName, replacementList);
			macro = new Macro(macroName, replacementList, startOffset, endOffset);
		}
		
		env.addMacro(macro);
		if(log != null && logMacro)
			log.defineMacro(macro);
		
		return macro;
	}
	
	
	private static boolean startsOrEndsWithHashHash(TokenList tokens) {
		if(tokens.isEmpty()) 
			return false;
		
		return tokens.first().getKind() == TK_HashHash || tokens.last().getKind() == TK_HashHash;
	}
	
	/**
	 * If the replacement list contains a newline then the start offset of the newline will be
	 * returned. Otherwise the end ofsset of the last token will be returned.
	 */
	private int calculateMacroDefinitionEndOffset(IToken tokenBeforeReplacementList, TokenList replacementList) {
		if(replacementList == null || replacementList.isEmpty())
			return tokenBeforeReplacementList.getEndOffset() + 1;
		else {
			IToken last = replacementList.last();
			if(last.getKind() == TK_NewLine) {
				return last.getStartOffset();
			}
			else {
				return replacementList.last().getEndOffset() + 1;
			}
		}
	}
	
	
	private TokenList replacementListTokens(Set paramNames, String varArgParamName) {
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
