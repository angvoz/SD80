/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.extension.IScannerExtension;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.ast.ASTCompletionNode;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;
import org.eclipse.cdt.internal.core.parser.token.ImagedExpansionToken;
import org.eclipse.cdt.internal.core.parser.token.ImagedToken;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.cdt.internal.core.parser.token.SimpleToken;

/**
 * @author Doug Schaefer
 *
 */
public class Scanner2 implements IScanner, IScannerData {

	/**
	 * @author jcamelon
	 *
	 */
	private static class InclusionData {

		public final IASTInclusion inclusion;
		public final CodeReader reader;
		

		/**
		 * @param reader
		 * @param inclusion
		 */
		public InclusionData(CodeReader reader, IASTInclusion inclusion ) {
			this.reader = reader; 
			this.inclusion = inclusion;
		}
	}
	
	private ISourceElementRequestor requestor;
	
	private ParserLanguage language;
	private IParserLogService log;
	private IScannerExtension scannerExtension;
	
	private CharArrayObjectMap definitions = new CharArrayObjectMap(512);
	private String[] includePaths;
	int count;
	
	private ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator();
	private final CharArrayObjectMap fileCache = new CharArrayObjectMap(100);
	
	// The context stack
	private static final int bufferInitialSize = 8;
	int bufferStackPos = -1;
	private char[][] bufferStack = new char[bufferInitialSize][];
	private Object[] bufferData = new Object[bufferInitialSize];
	private int[] bufferPos = new int[bufferInitialSize];
	private int[] bufferLimit = new int[bufferInitialSize];
	int[] lineNumbers = new int[bufferInitialSize];
	private int[] lineOffsets = new int[bufferInitialSize];
	
	//inclusion stack
	private Object[] callbackStack = new Object[bufferInitialSize];
	private int callbackPos = -1;
	
	//branch tracking
	private int branchStackPos = -1;
	private int[] branches = new int[bufferInitialSize];
		//states
		final static private int BRANCH_IF    = 1;
		final static private int BRANCH_ELIF  = 2;
		final static private int BRANCH_ELSE  = 3;
		final static private int BRANCH_END   = 4;
		
	
	// Utility
	private static String[] emptyStringArray = new String[0];
	private static char[] emptyCharArray = new char[0];
	private static EndOfFileException EOF = new EndOfFileException();
	
	PrintStream dlog;

	private ParserMode parserMode;

	private List workingCopies;
	
	{
//		try {
//			dlog = new PrintStream(new FileOutputStream("C:/dlog.txt"));
//		} catch (FileNotFoundException e) {
//		}
	}

	public Scanner2(CodeReader reader,
					IScannerInfo info,
					ISourceElementRequestor requestor,
					ParserMode parserMode,
					ParserLanguage language,
					IParserLogService log,
					IScannerExtension extension,
					List workingCopies) {

		this.scannerExtension = extension;
		this.requestor = requestor;
		this.parserMode = parserMode;
		this.language = language;
		this.log = log;
		this.workingCopies = workingCopies;

		if (reader.filename != null)
			fileCache.put(reader.filename, reader);
		
		pushContext(reader.buffer, reader);

		setupBuiltInMacros();
		
		if (info.getDefinedSymbols() != null) {
			Map symbols = info.getDefinedSymbols();
			String[] keys = (String[])symbols.keySet().toArray(emptyStringArray);
			for (int i = 0; i < keys.length; ++i) {
				String symbolName = keys[i];
				Object value = symbols.get(symbolName);

				if( value instanceof String ) {	
					//TODO add in check here for '(' and ')'
					addDefinition( symbolName.toCharArray(), scannerExtension.initializeMacroValue(this, ((String)value).toCharArray()));
				} 
			}
		}
		
		includePaths = info.getIncludePaths();

	}

	private void pushContext(char[] buffer) {
		if (++bufferStackPos == bufferStack.length) {
			int size = bufferStack.length * 2;
			
			char[][] oldBufferStack = bufferStack;
			bufferStack = new char[size][];
			System.arraycopy(oldBufferStack, 0, bufferStack, 0, oldBufferStack.length);
			
			Object[] oldBufferData = bufferData;
			bufferData = new Object[size];
			System.arraycopy(oldBufferData, 0, bufferData, 0, oldBufferData.length);
			
			int[] oldBufferPos = bufferPos;
			bufferPos = new int[size];
			System.arraycopy(oldBufferPos, 0, bufferPos, 0, oldBufferPos.length);
			
			int[] oldBufferLimit = bufferLimit;
			bufferLimit = new int[size];
			System.arraycopy(oldBufferLimit, 0, bufferLimit, 0, oldBufferLimit.length);
			
			int [] oldLineNumbers = lineNumbers;
			lineNumbers  = new int[size];
			System.arraycopy( oldLineNumbers, 0, lineNumbers, 0, oldLineNumbers.length);
			
			int [] oldLineOffsets = lineOffsets;
			lineOffsets  = new int[size];
			System.arraycopy( oldLineOffsets, 0, lineOffsets, 0, oldLineOffsets.length);
			
			
		}
		
		bufferStack[bufferStackPos] = buffer;
		bufferPos[bufferStackPos] = -1;
		lineNumbers[ bufferStackPos ] = 1;
		lineOffsets[ bufferStackPos ] = 0;
		bufferLimit[bufferStackPos] = buffer.length;
	}
	
	private void pushContext(char[] buffer, Object data) {
		if( data instanceof InclusionData )
		{
			boolean isCircular = false;
			for( int i = 0; i < bufferStackPos; ++i )
			{
				if( bufferData[i] instanceof CodeReader && 
					CharArrayUtils.equals( ((CodeReader)bufferData[i]).filename, ((InclusionData)data).reader.filename ) )
				{
					isCircular = true;
					break;
				}
				else if( bufferData[i] instanceof InclusionData &&
					CharArrayUtils.equals( ((InclusionData)bufferData[i]).reader.filename, ((InclusionData)data).reader.filename ) )
				{
					isCircular = true;
					break;
				}
			}
			if( isCircular )
			{
				handleProblem( IProblem.PREPROCESSOR_CIRCULAR_INCLUSION, ((InclusionData)data).inclusion.getStartingOffset(), ((InclusionData)data).inclusion.getFilename() );
				return;
			}
		}
		pushContext(buffer);
		bufferData[bufferStackPos] = data;
		if( data instanceof InclusionData )
		{
			pushCallback( data );
			if( log.isTracing() )
			{
				StringBuffer b = new StringBuffer( "Entering inclusion "); //$NON-NLS-1$
				b.append( ((InclusionData)data).reader.filename ); 
				log.traceLog( b.toString() );
			}

		}
	}
	
	private void popContext() {
		bufferStack[bufferStackPos] = null;
		if( bufferData[bufferStackPos] instanceof InclusionData )
		{
			if( log.isTracing() )
			{
				StringBuffer buffer = new StringBuffer( "Exiting inclusion "); //$NON-NLS-1$
				buffer.append( ((InclusionData)bufferData[bufferStackPos]).reader.filename ); 
				log.traceLog( buffer.toString() );
			}
					    pushCallback( ((InclusionData) bufferData[bufferStackPos]).inclusion );
		}
		bufferData[bufferStackPos] = null;
		--bufferStackPos;
	}
	
	private void pushCallback( Object obj ){
	    if( ++callbackPos == callbackStack.length ){
	        Object[] temp = new Object[ callbackStack.length << 1 ];
	        System.arraycopy( callbackStack, 0, temp, 0, callbackStack.length );
	        callbackStack = temp;
	    }
	    callbackStack[ callbackPos ] = obj;
	}
	
	private void popCallbacks(){
	    Object obj = null;
	    for( int i = 0; i <= callbackPos; i++ ){
	        obj = callbackStack[i];
	        //on the stack, InclusionData means enter, IASTInclusion means exit
	        if( obj instanceof InclusionData )
	            requestor.enterInclusion( ((InclusionData)obj).inclusion );
	        else if( obj instanceof IASTInclusion )
	            requestor.exitInclusion( (IASTInclusion) obj );
	        else if( obj instanceof IASTMacro )
	            requestor.acceptMacro( (IASTMacro) obj );
	        else if( obj instanceof IProblem )
	    		requestor.acceptProblem( (IProblem) obj );
	    }
	    callbackPos = -1;   
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#addDefinition(java.lang.String, java.lang.String)
	 */
	public void addDefinition(char[] key, char[] value) {
		definitions.put(key, new ObjectStyleMacro(key, value ));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#getCount()
	 */
	public int getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#getDefinitions()
	 */
	public Map getDefinitions() {
	    CharArrayObjectMap objMap = getRealDefinitions();
	    int size = objMap.size();
	    Map hashMap = new HashMap( size );
	    for( int i = 0; i < size; i ++ ){
	        hashMap.put( String.valueOf( objMap.keyAt( i ) ), objMap.getAt( i ) );
	    }
	    
		return hashMap;
	}

	public CharArrayObjectMap getRealDefinitions() {
		return definitions;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#getIncludePaths()
	 */
	public String[] getIncludePaths() {
		return includePaths;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#isOnTopContext()
	 */
	public boolean isOnTopContext() {
		return bufferStackPos <= 0;
	}

	private IToken lastToken;
	private IToken nextToken;
	private boolean finished = false;

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final char[] EMPTY_STRING_CHAR_ARRAY = new char[0];

	

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#nextToken()
	 */
	public IToken nextToken() throws EndOfFileException {
	    boolean exception = false;
		if (nextToken == null && !finished ) {
			try
			{
				nextToken = fetchToken();
			}
			catch( Exception e )
			{
			    exception = true;
				errorHandle();
			}
			if (nextToken == null && !exception)
			{
			    finished = true;
			}
		}

		if( callbackPos != -1 ){
		    popCallbacks();
		}
		
		if (finished)
		{
			if( offsetBoundary == -1 )
				throw EOF;			
			throwOLRE();
		}
		
		if (lastToken != null)
			lastToken.setNext(nextToken);
		IToken oldToken = lastToken;
		lastToken = nextToken;
		
		try
		{
			nextToken = fetchToken();
		}
		catch( Exception e )
		{
		    nextToken = null;
		    exception = true;
			errorHandle();
		}
		
		if (nextToken == null ){
		    if(!exception)
		        finished = true;
		}
		else if (nextToken.getType() == IToken.tPOUNDPOUND) {
			// time for a pasting
			IToken token2 = fetchToken();
			if (token2 == null) {
				nextToken = null;
				finished = true;
			} else {
				char[] pb = CharArrayUtils.concat( lastToken.getCharImage(), token2.getCharImage() );
				pushContext(pb);
				lastToken = oldToken;
				nextToken = null;
				return nextToken();
			}
		} else if (lastToken.getType() == IToken.tSTRING || lastToken.getType() ==IToken.tLSTRING ) {
			while (nextToken != null && ( nextToken.getType() == IToken.tSTRING || nextToken.getType() == IToken.tLSTRING )) {
				// Concatenate the adjacent strings
				int tokenType = IToken.tSTRING; 
				if( lastToken.getType() == IToken.tLSTRING || nextToken.getType() == IToken.tLSTRING )
					tokenType = IToken.tLSTRING;
				lastToken = newToken(tokenType, CharArrayUtils.concat( lastToken.getCharImage(), nextToken.getCharImage() ) );
				if (oldToken != null)
					oldToken.setNext(lastToken);
				nextToken = fetchToken();
			}
		}
		
		return lastToken;
	}
	
	/**
	 * 
	 */
	protected void errorHandle() {
		++bufferPos[bufferStackPos];
	}

	/**
	 * 
	 */
	private void throwOLRE() throws OffsetLimitReachedException {
		if( lastToken != null && lastToken.getEndOffset() != offsetBoundary )
			throw new OffsetLimitReachedException( (IToken)null );
		throw new OffsetLimitReachedException( lastToken );
	}

	// Return null to signify end of file
	private IToken fetchToken() throws EndOfFileException{
		++count;
		contextLoop:
		while (bufferStackPos >= 0) {

			// Find the first thing we would care about
			skipOverWhiteSpace();
			
			while (++bufferPos[bufferStackPos] >= bufferLimit[bufferStackPos]) {
				// We're at the end of a context, pop it off and try again
				popContext();
				continue contextLoop;
			}
 
			// Tokens don't span buffers, stick to our current one
			char[] buffer = bufferStack[bufferStackPos];
			int limit = bufferLimit[bufferStackPos];
			int pos = bufferPos[bufferStackPos];
	
			switch (buffer[pos]) {
				case '\n':
					continue;
					
				case 'L':
					if (pos + 1 < limit && buffer[pos + 1] == '"')
						return scanString();
					if (pos + 1 < limit && buffer[pos + 1] == '\'')
						return scanCharLiteral(true);
					
					IToken t = scanIdentifier();
					if (t instanceof MacroExpansionToken)
						continue;
					return t;
					
				
				case '"':
					return scanString();
					
				case '\'':
					return scanCharLiteral(false);

				case 'a':
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 'f':
				case 'g':
				case 'h':
				case 'i':
				case 'j':
				case 'k':
				case 'l':
				case 'm':
				case 'n':
				case 'o':
				case 'p':
				case 'q':
				case 'r':
				case 's':
				case 't':
				case 'u':
				case 'v':
				case 'w':
				case 'x':
				case 'y':
				case 'z':
				case 'A':
				case 'B':
				case 'C':
				case 'D':
				case 'E':
				case 'F':
				case 'G':
				case 'H':
				case 'I':
				case 'J':
				case 'K':
				case 'M':
				case 'N':
				case 'O':
				case 'P':
				case 'Q':
				case 'R':
				case 'S':
				case 'T':
				case 'U':
				case 'V':
				case 'W':
				case 'X':
				case 'Y':
				case 'Z':
				case '_':
					t = scanIdentifier();
					if (t instanceof MacroExpansionToken)
						continue;
					return t;
				
				case '\\':
					if (pos + 1 < limit && ( buffer[pos + 1] == 'u' || buffer[pos+1] == 'U' ) )
					{
						t = scanIdentifier();
						if( t instanceof MacroExpansionToken )
							continue;
						return t;
					}
					handleProblem( IProblem.SCANNER_BAD_CHARACTER, bufferPos[ bufferStackPos ], new char[] { '\\' } );
					continue;
					
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					return scanNumber();

				case '.':
					if (pos + 1 < limit) {
						switch (buffer[pos + 1]) {
							case '0':
							case '1':
							case '2':
							case '3':
							case '4':
							case '5':
							case '6':
							case '7':
							case '8':
							case '9':
								return scanNumber();
							
							case '.':
								if (pos + 2 < limit) {
									if (buffer[pos + 2] == '.') {
										bufferPos[bufferStackPos] += 2;
										return newToken(IToken.tELLIPSIS );
									}
								}
							case '*':
								++bufferPos[bufferStackPos];
								return newToken(IToken.tDOTSTAR );
						}
					}
					return newToken(IToken.tDOT);
					
				case '#':
					if (pos + 1 < limit && buffer[pos + 1] == '#') {
						++bufferPos[bufferStackPos];
						return newToken(IToken.tPOUNDPOUND);
					}
					
					// Should really check to make sure this is the first
					// non whitespace character on the line
					handlePPDirective(pos);
					continue;
				
				case '{':
					return newToken(IToken.tLBRACE );
				
				case '}':
					return newToken(IToken.tRBRACE );
				
				case '[':
					return newToken(IToken.tLBRACKET );
				
				case ']':
					return newToken(IToken.tRBRACKET );
				
				case '(':
					return newToken(IToken.tLPAREN );
				
				case ')':
					return newToken(IToken.tRPAREN );

				case ';':
					return newToken(IToken.tSEMI );
				
				case ':':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == ':') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tCOLONCOLON );
						}
					}
					return newToken(IToken.tCOLON );
					
				case '?':
					return newToken(IToken.tQUESTION );
				
				case '+':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '+') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tINCR);
						} else if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tPLUSASSIGN );
						}
					}
					return newToken(IToken.tPLUS );
				
				case '-':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '>') {
							if (pos + 2 < limit) {
								if (buffer[pos + 2] == '*') {
									bufferPos[bufferStackPos] += 2;
									return newToken(IToken.tARROWSTAR );
								}
							}
							++bufferPos[bufferStackPos];
							return newToken(IToken.tARROW);
						} else if (buffer[pos + 1] == '-') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tDECR );
						} else if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tMINUSASSIGN );
						}
					}
					return newToken(IToken.tMINUS );
				
				case '*':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tSTARASSIGN );
						}
					}
					return newToken(IToken.tSTAR);
				
				case '/':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tDIVASSIGN );
						}
					}
					return newToken(IToken.tDIV );
				
				case '%':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tMODASSIGN );
						}
					}
					return newToken(IToken.tMOD );
				
				case '^':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tXORASSIGN );
						}
					}
					return newToken(IToken.tXOR );
				
				case '&':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '&') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tAND );
						} else if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tAMPERASSIGN );
						}
					}
					return newToken(IToken.tAMPER );
				
				case '|':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '|') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tOR );
						} else if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tBITORASSIGN );
						}
					}
					return newToken(IToken.tBITOR );
				
				case '~':
					return newToken(IToken.tCOMPL );
				
				case '!':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tNOTEQUAL );
						}
					}
					return newToken(IToken.tNOT );
				
				case '=':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tEQUAL );
						}
					}
					return newToken(IToken.tASSIGN );
				
				case '<':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tLTEQUAL );
						} else if (buffer[pos + 1] == '<') {
							if (pos + 2 < limit) {
								if (buffer[pos + 2] == '=') {
									bufferPos[bufferStackPos] += 2;
									return newToken(IToken.tSHIFTLASSIGN );
								}
							}
							++bufferPos[bufferStackPos];
							return newToken(IToken.tSHIFTL );
						}
						else
						{
							char [] queryCharArray  = CharArrayUtils.extract( buffer, pos, 2 ); 
							if( scannerExtension.isExtensionOperator( language, queryCharArray  ) )
							{
								++bufferPos[ bufferStackPos ];
								return scannerExtension.createExtensionToken( this, queryCharArray  );
							}
								
						}
					}
					return newToken(IToken.tLT );
				
				case '>':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '=') {
							++bufferPos[bufferStackPos];
							return newToken(IToken.tGTEQUAL  );
						} else if (buffer[pos + 1] == '>') {
							if (pos + 2 < limit) {
								if (buffer[pos + 2] == '=') {
									bufferPos[bufferStackPos] += 2;
									return newToken(IToken.tSHIFTRASSIGN );
								}
							}
							++bufferPos[bufferStackPos];
							return newToken(IToken.tSHIFTR);
						}
						else
						{
							char [] queryCharArray = CharArrayUtils.extract( buffer, pos, 2 ); 
							if( scannerExtension.isExtensionOperator( language, queryCharArray ) )
							{
								++bufferPos[ bufferStackPos ];
								return scannerExtension.createExtensionToken( this, queryCharArray );
							}
						}
					}
					return newToken(IToken.tGT );
				
				case ',':
					return newToken(IToken.tCOMMA );

				default:
				    if( Character.isLetter( buffer[pos] ) || scannerExtension.isValidIdentifierStartCharacter( buffer[pos ]) ){
				        t = scanIdentifier();
						if (t instanceof MacroExpansionToken)
							continue;
						return t;
				    }
					// skip over anything we don't handle
			}
		}

	// We've run out of contexts, our work is done here
		return null;
	}

	/**
	 * @return
	 */
	private IToken newToken( int signal ) {
		return new SimpleToken(signal,  bufferPos[bufferStackPos] + 1 , getCurrentFilename(), getLineNumber( bufferPos[bufferStackPos] + 1)  );
	}

	private IToken newToken( int signal, char [] buffer )
	{
		if( bufferData[bufferStackPos] instanceof IMacro )
		{
			int mostRelevant;
			for( mostRelevant = bufferStackPos; mostRelevant >= 0; --mostRelevant )
				if( bufferData[mostRelevant] instanceof InclusionData || bufferData[mostRelevant] instanceof CodeReader )
					break;
			return new ImagedExpansionToken( signal, buffer, bufferPos[mostRelevant], ((IMacro)bufferData[bufferStackPos]).getName().length, getCurrentFilename(), getLineNumber( bufferPos[bufferStackPos] + 1));
		}
		IToken i = new ImagedToken(signal, buffer, bufferPos[bufferStackPos] + 1 , getCurrentFilename(), getLineNumber( bufferPos[bufferStackPos] + 1));
		if( buffer != null && buffer.length == 0 )
			bufferPos[bufferStackPos] += 1; //ensure we don't hit infinite loops
		return i;
	}
	
	private IToken scanIdentifier() {
		char[] buffer = bufferStack[bufferStackPos];
		boolean escapedNewline = false;
		int start = bufferPos[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		int len = 1;
		
		while (++bufferPos[bufferStackPos] < limit) {
			char c = buffer[bufferPos[bufferStackPos]];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
				|| c == '_' || (c >= '0' && c <= '9') || Character.isUnicodeIdentifierPart(c) ) {
				++len;
				continue;
			}
			else if( c == '\\' && bufferPos[bufferStackPos] + 1 < limit && buffer[ bufferPos[bufferStackPos] + 1 ] == '\n')
			{
				// escaped newline
				++bufferPos[bufferStackPos];
				len += 2;
				escapedNewline = true;
				continue;
			}
			else if( c == '\\' && ( bufferPos[bufferStackPos] + 1 < limit ) )  
			{
				if (( buffer[ bufferPos[bufferStackPos] + 1 ] == 'u') || buffer[ bufferPos[bufferStackPos] + 1 ] == 'U')
				{
					++bufferPos[bufferStackPos];
					len += 2;
					continue;
				}
			}
			else if( scannerExtension.isValidIdentifierCharacter( c ))
			{
				++len;
				continue;
			}
			break;
		}

		--bufferPos[bufferStackPos];
		
		// Check for macro expansion
		Object expObject = definitions.get(buffer, start, len);
			
		if (expObject != null && !isLimitReached() && shouldExpandMacro( (IMacro) expObject)) {
			if (expObject instanceof FunctionStyleMacro) {
				handleFunctionStyleMacro((FunctionStyleMacro)expObject, true);
			} else if (expObject instanceof ObjectStyleMacro) {
				ObjectStyleMacro expMacro = (ObjectStyleMacro)expObject;
				char[] expText = expMacro.expansion;
				if (expText.length > 0)
					pushContext(expText, expMacro);
			}
			else if( expObject instanceof DynamicStyleMacro )
			{
				DynamicStyleMacro expMacro = (DynamicStyleMacro) expObject;
				char[] expText = expMacro.execute();
				if (expText.length > 0)
					pushContext(expText, expMacro);
				
			} else if (expObject instanceof char[]) {
				char[] expText = (char[])expObject;
				if (expText.length > 0)
					pushContext(expText);
			}
			return new MacroExpansionToken();
		}
		
		
		
		char [] result = escapedNewline ? removedEscapedNewline( buffer, start, len ) : CharArrayUtils.extract( buffer, start, len );
		if( scannerExtension.isExtensionKeyword( language,  result))
			return scannerExtension.createExtensionToken( this, result );
		int tokenType = escapedNewline ? keywords.get(result, 0, result.length) 
		                               : keywords.get(buffer, start, len );
		
		if (tokenType == keywords.undefined){
		    result = (result != null) ? result : CharArrayUtils.extract( buffer, start, len );
			return newToken(IToken.tIDENTIFIER, result );
		}
		return newToken(tokenType);
	}
	
	/**
     * @param buffer
     * @param start
     * @param len
     * @param expObject
     * @return
     */
    private boolean shouldExpandMacro( IMacro macro ) {
        // but not if it has been expanded on the stack already
		// i.e. recursion avoidance
		if( macro != null && !isLimitReached() )
			for (int stackPos = bufferStackPos; stackPos >= 0; --stackPos)
				if( bufferData[stackPos] != null && bufferData[stackPos] instanceof IMacro && 
				    CharArrayUtils.equals(macro.getName(), ((IMacro)bufferData[stackPos]).getName()) ) 
				{
					return false;
				}
        return true;
    }

    /**
	 * @return
	 */
	private final boolean isLimitReached() {
		if( offsetBoundary == -1 || bufferStackPos != 0 ) return false;
		if( bufferPos[bufferStackPos] == offsetBoundary - 1 ) return true;
		if( bufferPos[bufferStackPos] == offsetBoundary )
		{
			int c = bufferStack[bufferStackPos][bufferPos[bufferStackPos]];
			if( c == '\n' || c == ' ' || c == '\t' || c == '\r')
				return true;
		}
		return false;
	}

	private IToken scanString() {
		char[] buffer = bufferStack[bufferStackPos];
		
		int tokenType = IToken.tSTRING;
		if (buffer[bufferPos[bufferStackPos]] == 'L') {
			++bufferPos[bufferStackPos];
			tokenType = IToken.tLSTRING;
		}
		
		int stringStart = bufferPos[bufferStackPos] + 1;
		int stringLen = 0;
		boolean escaped = false;
		boolean foundClosingQuote = false;
		loop:
		while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
			++stringLen;
			char c = buffer[bufferPos[bufferStackPos]];
			if (c == '"') {
				if (!escaped){
				    foundClosingQuote = true;
					break;
				}
			}
			else if (c == '\\') {
				escaped = !escaped;
				continue;
			} else if(c == '\n'){
			    //unescaped end of line before end of string
			    if( !escaped )
			        break;
			}
			else if ( c == '\r')
			{
				if( bufferPos[bufferStackPos] + 1 < bufferLimit[bufferStackPos] && 
				    buffer[bufferPos[bufferStackPos] + 1 ] == '\n' )
				{
					++bufferPos[bufferStackPos];
					if( !escaped)
						break;
				}
			}
			escaped = false;
		}
		--stringLen;

		// We should really throw an exception if we didn't get the terminating
		// quote before the end of buffer
		char[] result = CharArrayUtils.extract(buffer, stringStart, stringLen);
		if( !foundClosingQuote ){
		    handleProblem( IProblem.SCANNER_UNBOUNDED_STRING, stringStart, result );
		}
		return newToken(tokenType, result);
	}

	private IToken scanCharLiteral(boolean b) {
		char[] buffer = bufferStack[bufferStackPos];
		int start = bufferPos[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];

		int tokenType = IToken.tCHAR;
		int length = 1;
		if (buffer[bufferPos[bufferStackPos]] == 'L') {
			++bufferPos[bufferStackPos];
			tokenType = IToken.tLCHAR;
			++length;
		}

		if (start >= limit) {
			return newToken(tokenType, emptyCharArray );
		}

		
		boolean escaped = false;
		while (++bufferPos[bufferStackPos] < limit) {
			++length;
			int pos = bufferPos[bufferStackPos];
			if (buffer[pos] == '\'') {
				if (!escaped)
					break;
			} else if (buffer[pos] == '\\') {
				escaped = !escaped;
				continue;
			}
			escaped = false;
		}
		
		if( bufferPos[ bufferStackPos ] == limit )
		{
			handleProblem( IProblem.SCANNER_BAD_CHARACTER, start,  CharArrayUtils.extract(buffer, start, length) );
			return newToken( tokenType, emptyCharArray );
		}
		
		char[] image = length > 0
			? CharArrayUtils.extract(buffer, start, length)
			: emptyCharArray;

		return newToken(tokenType, image );
	}
	
	private static final ScannerProblemFactory spf = new ScannerProblemFactory();
	/**
	 * @param scanner_bad_character
	 */
	private void handleProblem(int id, int startOffset, char [] arg ) {
		if( parserMode == ParserMode.COMPLETION_PARSE ) return;
		IProblem p = spf.createProblem( id, startOffset, bufferPos[bufferStackPos], getLineNumber( bufferPos[bufferStackPos] ), getCurrentFilename(), arg != null ? arg : emptyCharArray, false, true );
		pushCallback( p );
	}

	
	/**
	 * @param i
	 * @return
	 */
	public int getLineNumber(int offset) {
		if( parserMode == ParserMode.COMPLETION_PARSE ) return -1;
		int index = getCurrentFileIndex();
		if( offset >= bufferLimit[ index ]) return -1;
		
		int lineNum = lineNumbers[ index ]; 
		int startingPoint = lineOffsets[ index ]; 
		
		for( int i = startingPoint; i < offset; ++i )
		{
			if( bufferStack[ index ][i] == '\n')
				++lineNum;
		}
		if( startingPoint < offset )
		{
			lineNumbers[ index ] = lineNum;
			lineOffsets[ index ] = offset;
		}
		return lineNum;
	}

	private IToken scanNumber() {
		char[] buffer = bufferStack[bufferStackPos];
		int start = bufferPos[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		boolean isFloat = buffer[start] == '.';
		boolean hasExponent = false;
		
		boolean isHex = false;
		if (buffer[start] == '0' && start + 1 < limit) {
			switch (buffer[start + 1]) {
				case 'x':
				case 'X':
					isHex = true;
					++bufferPos[bufferStackPos];
			}
		}
		
		while (++bufferPos[bufferStackPos] < limit) {
			int pos = bufferPos[bufferStackPos];
			switch (buffer[pos]) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					continue;
				
				case '.':
					if (isFloat){
						// second dot
					    handleProblem( IProblem.SCANNER_BAD_FLOATING_POINT, start, null );
						break;
					}
					
					isFloat = true;
					continue;
				
				case 'E':
				case 'e':
					if (isHex)
						// a hex 'e'
						continue;
					
					if (hasExponent)
						// second e
						break;
				
					if (pos + 1 >= limit)
						// ending on the e?
						break;
					
					switch (buffer[pos + 1]) {
						case '+':
						case '-':
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							// looks like a good exponent
							isFloat = true;
							hasExponent = true;
							++bufferPos[bufferStackPos];
							continue;
						default:
							// no exponent number?
							break;
					}
					break;
				
				case 'a':
				case 'A':
				case 'b':
				case 'B':
				case 'c':
				case 'C':
				case 'd':
				case 'D':
					if (isHex)
						continue;
					
					// not ours
					--bufferPos[bufferStackPos];
					break;
					
				case 'f':
				case 'F':
					if (isHex)
						continue;
					
					// must be float suffix
					++bufferPos[bufferStackPos];
					break;

				case 'p':
				case 'P':
					// Hex float exponent prefix
					if (!isFloat || !isHex) {
						--bufferPos[bufferStackPos];
						break;
					}
					
					if (hasExponent)
						// second p
						break;
				
					if (pos + 1 >= limit)
						// ending on the p?
						break;
					
					switch (buffer[pos + 1]) {
						case '+':
						case '-':
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							// looks like a good exponent
							isFloat = true;
							hasExponent = true;
							++bufferPos[bufferStackPos];
							continue;
						default:
							// no exponent number?
							break;
					}
					break;
 
				case 'u':
				case 'U':
				case 'L':
				case 'l':
					// unsigned suffix
					suffixLoop: 
					while(++bufferPos[bufferStackPos]  < limit) {
						switch (buffer[bufferPos[bufferStackPos]]) {
							case 'U':
							case 'u':
							case 'l':
							case 'L':
								break;
							default:
								
								break suffixLoop;
						}
					}
					break;
					
				default:
					// not part of our number
			}
			
			// If we didn't continue in the switch, we're done
			break;
		}
		
		--bufferPos[bufferStackPos];
		
		char[] result = CharArrayUtils.extract( buffer, start, bufferPos[bufferStackPos] - start + 1);
		int tokenType = isFloat ? IToken.tFLOATINGPT : IToken.tINTEGER;
		if( tokenType == IToken.tINTEGER && isHex && result.length == 2 ){
		    handleProblem( IProblem.SCANNER_BAD_HEX_FORMAT, start, result );
		}
		
		return newToken( tokenType, result );
	}
	
	private boolean branchState( int state ){
	    if( state != BRANCH_IF && branchStackPos == -1 )
	        return false;
	    
	    switch( state ){
        case BRANCH_IF:
            if( ++branchStackPos == branches.length ){
                int [] temp = new int [ branches.length << 1 ];
                System.arraycopy( branches, 0, temp, 0, branches.length );
                branches = temp;
            }
            branches[branchStackPos] = BRANCH_IF;
            return true;
        case BRANCH_ELIF:
        case BRANCH_ELSE:
            switch( branches[branchStackPos] ){
                case BRANCH_IF:
                case BRANCH_ELIF:
                    branches[branchStackPos] = state;
                    return true;
                default:
                    return false;
            }
        case BRANCH_END:
            switch( branches[branchStackPos] ){
                case BRANCH_IF:
                case BRANCH_ELSE:
                case BRANCH_ELIF:
                    --branchStackPos;
                    return true;
                default:
                    return false;
            }
	    }
	    return false;
	}
	
	private void handlePPDirective(int pos) throws EndOfFileException {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		int startingLineNumber = getLineNumber( pos );
		skipOverWhiteSpace();
		if( isLimitReached() ) 
			handleCompletionOnPreprocessorDirective( "#" ); //$NON-NLS-1$
		
		// find the directive
		int start = ++bufferPos[bufferStackPos];
		
		// if new line or end of buffer, we're done
		if (start >= limit || buffer[start] == '\n')
			return;

		boolean problem = false;
		char c = buffer[start];
		if ((c >= 'a' && c <= 'z')) {
			while (++bufferPos[bufferStackPos] < limit) {
				c = buffer[bufferPos[bufferStackPos]];
				if ((c >= 'a' && c <= 'z') || c == '_')
					continue;
				break;
			}
			--bufferPos[bufferStackPos];
			int len = bufferPos[bufferStackPos] - start + 1;
			if( isLimitReached() ) 
				handleCompletionOnPreprocessorDirective( new String( buffer, pos, len + 1 ));
			
			int type = ppKeywords.get(buffer, start, len);
			if (type != ppKeywords.undefined) {
				switch (type) {
					case ppInclude:
						handlePPInclude(pos,false, startingLineNumber);
						return;
					case ppInclude_next:
						handlePPInclude(pos, true, startingLineNumber);
						return;
					case ppDefine:
						handlePPDefine(pos, startingLineNumber );
						return;
					case ppUndef:
						handlePPUndef();
						return;
					case ppIfdef:
						handlePPIfdef(true);
						return;
					case ppIfndef:
						handlePPIfdef(false);
						return;
					case ppIf:
						start = bufferPos[bufferStackPos];
						skipToNewLine();
						len = bufferPos[bufferStackPos] - start;
						if( isLimitReached() )
							handleCompletionOnExpression( CharArrayUtils.extract( buffer, start, len ) );
						branchState( BRANCH_IF );
						if (expressionEvaluator.evaluate(buffer, start, len, definitions) == 0) {
							if (dlog != null) dlog.println("#if <FALSE> " + new String(buffer,start+1,len-1)); //$NON-NLS-1$
							skipOverConditionalCode(true);
							if( isLimitReached() )
								handleInvalidCompletion();
						} else
							if (dlog != null) dlog.println("#if <TRUE> " + new String(buffer,start+1,len-1)); //$NON-NLS-1$
						return;
					case ppElse:
					case ppElif:
						// Condition must have been true, skip over the rest
						
						if( branchState( type == ppElse ? BRANCH_ELSE : BRANCH_ELIF) ){
						    skipToNewLine();
							skipOverConditionalCode(false);
						} else {
						    handleProblem( IProblem.PREPROCESSOR_UNBALANCE_CONDITION, start, ppKeywords.findKey( buffer, start, len ) );
						    skipToNewLine();
						}
						
						if( isLimitReached() )
							handleInvalidCompletion();
						return;
					case ppError:
						start = bufferPos[bufferStackPos];
						skipToNewLine();
						len = bufferPos[bufferStackPos] - start;
						handleProblem( IProblem.PREPROCESSOR_POUND_ERROR, start, CharArrayUtils.extract( buffer, start, len ));
						break;
					case ppEndif:
					    if( !branchState( BRANCH_END ) )
					        handleProblem( IProblem.PREPROCESSOR_UNBALANCE_CONDITION, start, ppKeywords.findKey( buffer, start, len ) );
					    break;
					default:
					    problem = true;
					    break;
				}
			}
		} else 
		    problem = true;

		if( problem )
		    handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, start, null );
		
		// don't know, chew up to the end of line
		// includes endif which is immatereal at this point
		skipToNewLine();
	}		

	private void handlePPInclude(int pos2, boolean next, int startingLineNumber) {
 		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		skipOverWhiteSpace();
		int startOffset = pos2;
		int pos = ++bufferPos[bufferStackPos];
		if (pos >= limit)
			return;

		boolean local = false;
		String filename = null;
		
		int endOffset = startOffset;
		int nameOffset = 0;
		int nameEndOffset = 0;
		
		int nameLine= 0, endLine = 0; 
		char c = buffer[pos];
		if( c == '\n') return;
		if (c == '"') {
			nameLine = getLineNumber( bufferPos[bufferStackPos] );
			local = true;
			int start = bufferPos[bufferStackPos] + 1;
			int length = 0;
			boolean escaped = false;
			while (++bufferPos[bufferStackPos] < limit) {
				++length;
				c = buffer[bufferPos[bufferStackPos]];
				if (c == '"') {
					if (!escaped)
						break;
				} else if (c == '\\') {
					escaped = !escaped;
					continue;
				}
				escaped = false;
			}
			--length;
			
			filename = new String(buffer, start, length);
			nameOffset = start;
			nameEndOffset = start + length;
			endOffset = start + length + 1;
		} else if (c == '<') {
			nameLine = getLineNumber( bufferPos[ bufferStackPos ] );
			local = false;
			int start = bufferPos[bufferStackPos] + 1;
			int length = 0;

			while (++bufferPos[bufferStackPos] < limit &&
					buffer[bufferPos[bufferStackPos]] != '>')
				++length;
			endOffset = start + length + 1;
			nameOffset = start;
			nameEndOffset = start + length;

			filename = new String(buffer, start, length);
		}
		else
		{
			// handle macro expansions
			int startPos = pos;
			int len = 1;
			while (++bufferPos[bufferStackPos] < limit) {
				c = buffer[bufferPos[bufferStackPos]];
				if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
						|| c == '_' || (c >= '0' && c <= '9') || Character.isUnicodeIdentifierPart(c)) {
					++len;
					continue;
				}
				else if( c == '\\' && bufferPos[bufferStackPos] + 1 < buffer.length && buffer[ bufferPos[bufferStackPos] + 1 ] == '\n')
				{
					// escaped newline
					++bufferPos[bufferStackPos];
					len += 2;
					continue;
				}
				break;
			}
			
			
			Object expObject = definitions.get(buffer, startPos, len );
			
			if (expObject != null) {
				--bufferPos[bufferStackPos];
				char [] t = null;
				if (expObject instanceof FunctionStyleMacro) 
				{
					t = handleFunctionStyleMacro((FunctionStyleMacro)expObject, false);
				}
				else if ( expObject instanceof ObjectStyleMacro )
				{
					t = ((ObjectStyleMacro)expObject).expansion;
				}
				if( t != null ) 
				{
					t = replaceArgumentMacros( t );
					if( (t[ t.length - 1 ] ==  t[0] ) && ( t[0] == '\"') )
					{
						local = true;
						filename = new String( t, 1, t.length - 2 );
					}
					else if( t[0] == '<' && t[t.length - 1] == '>' )
					{
						local = false;
						filename = new String( t, 1, t.length - 2 );						
					}
				}
			}
		}
		 
		if( filename == null || filename == EMPTY_STRING )
		{
			handleProblem( IProblem.PREPROCESSOR_INVALID_DIRECTIVE, startOffset, null );
			return;
		}
		char [] fileNameArray = filename.toCharArray();
		// TODO else we need to do macro processing on the rest of the line
		endLine = getLineNumber( bufferPos[ bufferStackPos ] );
		skipToNewLine();

		if( parserMode == ParserMode.QUICK_PARSE )
		{
			IASTInclusion inclusion = getASTFactory().createInclusion( fileNameArray, EMPTY_STRING_CHAR_ARRAY, local, startOffset, startingLineNumber, nameOffset, nameEndOffset, nameLine, endOffset, endLine, getCurrentFilename() );
			pushCallback( new InclusionData( null, inclusion ) );
		    pushCallback( inclusion );
		}
		else
		{
			CodeReader reader = null;
			
			if (local) {
				// create an include path reconciled to the current directory
				File file = new File( String.valueOf( getCurrentFilename() ) );
				File parentFile = file.getParentFile();
				if( parentFile != null )
				{
					String absolutePath = parentFile.getAbsolutePath();
					String finalPath = ScannerUtility.createReconciledPath( absolutePath, filename );
					reader = (CodeReader)fileCache.get(finalPath.toCharArray());
					if (reader == null){
						reader = ScannerUtility.createReaderDuple( finalPath, requestor, getWorkingCopies() );
						if (reader != null && reader.filename != null) 
							fileCache.put(reader.filename, reader);
					}
					if (reader != null) {
						if (dlog != null) dlog.println("#include \"" + finalPath + "\""); //$NON-NLS-1$ //$NON-NLS-2$
						IASTInclusion inclusion = getASTFactory().createInclusion( fileNameArray, reader.filename, local, startOffset, startingLineNumber, nameOffset, nameEndOffset, nameLine, endOffset, endLine, getCurrentFilename() );
						pushContext(reader.buffer, new InclusionData( reader, inclusion ));
						return;
					}
				}
			}
			
			// iterate through the include paths
			// foundme has odd logic but if we're not include_next, then we are looking for the
			// first occurance, otherwise, we're looking for the one after us
			boolean foundme = !next;
			if (includePaths != null)
				for (int i = 0; i < includePaths.length; ++i) {
					String finalPath = ScannerUtility.createReconciledPath(includePaths[i], filename);
					if (!foundme) {
						if (finalPath.equals(((InclusionData)bufferData[bufferStackPos]).reader.filename)) {
							foundme = true;
							continue;
						}
					} else {
						reader = (CodeReader)fileCache.get(finalPath.toCharArray());
						if (reader == null){
							reader = ScannerUtility.createReaderDuple( finalPath, requestor, getWorkingCopies() );
							if (reader != null && reader.filename != null)
								fileCache.put(reader.filename, reader);
						}
						if (reader != null) {
							if (dlog != null) dlog.println("#include <" + finalPath + ">"); //$NON-NLS-1$ //$NON-NLS-2$
							IASTInclusion inclusion = getASTFactory().createInclusion( fileNameArray, reader.filename, local, startOffset, startingLineNumber, nameOffset, nameEndOffset, nameLine, endOffset, endLine, getCurrentFilename() );
							pushContext(reader.buffer, new InclusionData( reader, inclusion ));
							return;
						}
					}
				}
		
			
			if (reader == null)
				handleProblem( IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, startOffset, fileNameArray);
		}
		
	}
	
	private void handlePPDefine(int pos2, int startingLineNumber) {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		int startingOffset = pos2;
		int endingLine = 0, nameLine = 0;
		skipOverWhiteSpace();
		
		// get the Identifier
		int idstart = ++bufferPos[bufferStackPos];
		if (idstart >= limit)
			return;
		
		char c = buffer[idstart];
		if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || Character.isUnicodeIdentifierPart(c))) {
		    handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, idstart, null );
			skipToNewLine();
			return;
		}

		int idlen = 1;
		while (++bufferPos[bufferStackPos] < limit) {
			c = buffer[bufferPos[bufferStackPos]];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| c == '_' || (c >= '0' && c <= '9') || Character.isUnicodeIdentifierPart(c)) {
				++idlen;
				continue;
			}  
			break;
		}
		--bufferPos[bufferStackPos];
		nameLine = getLineNumber( bufferPos[ bufferStackPos ] );
		char[] name = new char[idlen];
		System.arraycopy(buffer, idstart, name, 0, idlen);
		if (dlog != null) dlog.println("#define " + new String(buffer, idstart, idlen)); //$NON-NLS-1$
		
		// Now check for function style macro to store the arguments
		char[][] arglist = null;
		int pos = bufferPos[bufferStackPos];
		if (pos + 1 < limit && buffer[pos + 1] == '(') {
			++bufferPos[bufferStackPos];
			arglist = new char[4][];
			int currarg = -1;
			while (bufferPos[bufferStackPos] < limit) {
				pos = bufferPos[bufferStackPos];
				skipOverWhiteSpace();
				if (++bufferPos[bufferStackPos] >= limit)
					return;
				c = buffer[bufferPos[bufferStackPos]];
				if (c == ')') {
					break;
				} else if (c == ',') {
					continue;
				} else if (c == '.'
						&& pos + 1 < limit && buffer[pos + 1] == '.'
						&& pos + 2 < limit && buffer[pos + 2] == '.') {
					// varargs
					// TODO - something better
					bufferPos[bufferStackPos] += 2;
					arglist[++currarg] = "...".toCharArray(); //$NON-NLS-1$
					continue;
				} else if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || Character.isUnicodeIdentifierPart(c))) {
				    handleProblem( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, idstart, name );
					// yuck
					skipToNewLine();
					return;
				}
				int argstart = bufferPos[bufferStackPos];
				skipOverIdentifier();
				if (++currarg == arglist.length) {
					char[][] oldarglist = arglist;
					arglist = new char[oldarglist.length * 2][];
					System.arraycopy(oldarglist, 0, arglist, 0, oldarglist.length);
				}
				int arglen = bufferPos[bufferStackPos] - argstart + 1;
				char[] arg = new char[arglen];
				System.arraycopy(buffer, argstart, arg, 0, arglen);
				arglist[currarg] = arg;
			}
		}

		// Capture the replacement text
		skipOverWhiteSpace();
		int textstart = bufferPos[bufferStackPos] + 1;
		int textend = textstart - 1;
		
		boolean encounteredMultilineComment = false;
		while (bufferPos[bufferStackPos] + 1 < limit
				&& buffer[bufferPos[bufferStackPos] + 1] != '\n') {
		    //16.3.2-1 Each # preprocessing token in the replacement list for a function-like-macro shall
		    //be followed by a parameter as the next preprocessing token 
			if( arglist != null && !skipOverNonWhiteSpace( true ) ){
			    ++bufferPos[bufferStackPos];  //advances us to the #
			    if( skipOverWhiteSpace() )
			        encounteredMultilineComment = true;
			    
			    boolean isArg = false;
			    if( bufferPos[bufferStackPos] + 1 < limit )
			    {
				    ++bufferPos[bufferStackPos]; //advances us past the # (or last whitespace) 
				    for( int i = 0; i < arglist.length && arglist[i] != null; i++ )
				    {
				        if( bufferPos[bufferStackPos] + arglist[i].length - 1 < limit )
				        {
					        if( CharArrayUtils.equals( buffer, bufferPos[bufferStackPos], arglist[i].length, arglist[i] ) )
					        {
					            isArg = true;
					            //advance us to the end of the arg
					            bufferPos[bufferStackPos] += arglist[i].length - 1;
					            break;
					        }
				        }
				    }
			    }
			    if( !isArg )
			        handleProblem( IProblem.PREPROCESSOR_MACRO_PASTING_ERROR, bufferPos[bufferStackPos], null );
			} else {
			    skipOverNonWhiteSpace();
			}
			textend = bufferPos[bufferStackPos];
			if( skipOverWhiteSpace() )
				encounteredMultilineComment = true;
		}

		int textlen = textend - textstart + 1;
		endingLine = getLineNumber( bufferPos[ bufferStackPos ] );
		char[] text = emptyCharArray;
		if (textlen > 0) {
			text = new char[textlen];
			System.arraycopy(buffer, textstart, text, 0, textlen);
		}
		
		if( encounteredMultilineComment )
			text = removeMultilineCommentFromBuffer( text );
		text = removedEscapedNewline( text, 0, text.length );
			
		// Throw it in
		definitions.put(name, 	arglist == null
				? new ObjectStyleMacro(name, text)
						: new FunctionStyleMacro(name, text, arglist) );
		 
		pushCallback( getASTFactory().createMacro( name, startingOffset, startingLineNumber, idstart, idstart + idlen, nameLine, textstart + textlen, endingLine, getCurrentFilename() ) );
	}
	
	
	/**
	 * @param text
	 * @return
	 */
	private char[] removedEscapedNewline(char[] text, int start, int len ) {
		if( CharArrayUtils.indexOf( '\n', text, start, len ) == -1 )
			return text;
		char [] result = new char[ text.length ];
		Arrays.fill( result, ' ');
		int counter = 0;
		for( int i = 0;  i < text.length; ++i )
		{
			if( text[i] == '\\' && i+ 1 < text.length && text[i+1] == '\n' )
				++i;
			else
				result[ counter++ ] = text[i];
		}
		return CharArrayUtils.trim( result );
	}

	/**
	 * @param text
	 * @return
	 */
	private char[] removeMultilineCommentFromBuffer(char[] text) {
		char [] result = new char[ text.length ];
		Arrays.fill( result, ' ');
		int resultCount = 0;
		for( int i = 0; i < text.length; ++i )
		{
			if( text[i] == '/' && ( i+1 < text.length ) && text[i+1] == '*')
			{
				i += 2;
				while( i < text.length && text[i] != '*' && i+1 < text.length && text[i+1] != '/')
					++i;
				++i;
			}
			else
				result[resultCount++] = text[i];
				
		}
		return CharArrayUtils.trim( result );
	}

	private void handlePPUndef() throws EndOfFileException {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		skipOverWhiteSpace();
		
		// get the Identifier
		int idstart = ++bufferPos[bufferStackPos];
		if (idstart >= limit)
			return;
		
		char c = buffer[idstart];
		if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || Character.isUnicodeIdentifierPart(c))) {
			skipToNewLine();
			return;
		}

		int idlen = 1;
		while (++bufferPos[bufferStackPos] < limit) {
			c = buffer[bufferPos[bufferStackPos]];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| c == '_' || (c >= '0' && c <= '9' || Character.isUnicodeIdentifierPart(c))) {
				++idlen;
				continue;
			} 
			break;
			
		}
		--bufferPos[bufferStackPos];

		if( isLimitReached() )
			handleCompletionOnDefinition( new String( buffer, idstart, idlen ));

		skipToNewLine();
		
		definitions.remove(buffer, idstart, idlen);
		if (dlog != null) dlog.println("#undef " + new String(buffer, idstart, idlen)); //$NON-NLS-1$
	}
	
	private void handlePPIfdef(boolean positive) throws EndOfFileException {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];

		if( isLimitReached() )
			handleCompletionOnDefinition( EMPTY_STRING );

		skipOverWhiteSpace();

		if( isLimitReached() )
			handleCompletionOnDefinition( EMPTY_STRING );

		// get the Identifier
		int idstart = ++bufferPos[bufferStackPos];
		if (idstart >= limit)
			return;
		
		char c = buffer[idstart];
		if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || Character.isUnicodeIdentifierPart(c))) {
			skipToNewLine();
			return;
		}

		int idlen = 1;
		while (++bufferPos[bufferStackPos] < limit) {
			c = buffer[bufferPos[bufferStackPos]];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| c == '_' || (c >= '0' && c <= '9' || Character.isUnicodeIdentifierPart(c))) {
				++idlen;
				continue;
			} 
			break;
			
		}
		--bufferPos[bufferStackPos];

		if( isLimitReached() )
			handleCompletionOnDefinition( new String( buffer, idstart, idlen ));
		
		skipToNewLine();

		branchState( BRANCH_IF );
		
		if ((definitions.get(buffer, idstart, idlen) != null) == positive) {
			if (dlog != null) dlog.println((positive ? "#ifdef" : "#ifndef") //$NON-NLS-1$ //$NON-NLS-2$
					+ " <TRUE> " + new String(buffer, idstart, idlen)); //$NON-NLS-1$
			// continue on
			return;
		}
		
		if (dlog != null) dlog.println((positive ? "#ifdef" : "#ifndef") //$NON-NLS-1$ //$NON-NLS-2$
				+ " <FALSE> " + new String(buffer, idstart, idlen)); //$NON-NLS-1$
		// skip over this group
		skipOverConditionalCode(true);
		if( isLimitReached() )
			handleInvalidCompletion();
	}

	// checkelse - if potential for more, otherwise skip to endif
	private void skipOverConditionalCode(boolean checkelse) {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		int nesting = 0;
		
		while (bufferPos[bufferStackPos] < limit) {
				
			skipOverWhiteSpace();
			
			if (++bufferPos[bufferStackPos] >= limit)
				return;
			
			char c = buffer[bufferPos[bufferStackPos]];
			if (c == '#') {
				skipOverWhiteSpace();
				
				// find the directive
				int start = ++bufferPos[bufferStackPos];
				
				// if new line or end of buffer, we're done
				if (start >= limit || buffer[start] == '\n')
					continue;
				
				c = buffer[start];
				if ((c >= 'a' && c <= 'z')) {
					while (++bufferPos[bufferStackPos] < limit) {
						c = buffer[bufferPos[bufferStackPos]];
						if ((c >= 'a' && c <= 'z'))
							continue;
						break;
					}
					--bufferPos[bufferStackPos];
					int len = bufferPos[bufferStackPos] - start + 1;
					int type = ppKeywords.get(buffer, start, len);
					if (type != ppKeywords.undefined) {
						switch (type) {
							case ppIfdef:
							case ppIfndef:
							case ppIf:
								++nesting;
								branchState( BRANCH_IF );
								break;
							case ppElse:
							    if( branchState( BRANCH_ELSE ) ){
									if (checkelse && nesting == 0) {
										skipToNewLine();
										return;
									}
							    } else {
							        //problem, ignore this one. 
							        handleProblem( IProblem.PREPROCESSOR_UNBALANCE_CONDITION, start, ppKeywords.findKey( buffer, start, len ) );
							        skipToNewLine();
							    }
								break;
							case ppElif:
							    if( branchState( BRANCH_ELIF ) ){
									if (checkelse && nesting == 0) {
										// check the condition
										start = bufferPos[bufferStackPos];
										skipToNewLine();
										len = bufferPos[bufferStackPos] - start;
										if (expressionEvaluator.evaluate(buffer, start, len, definitions) != 0)
											// condition passed, we're good
											return;
									}
							    } else {
							        //problem, ignore this one. 
							        handleProblem( IProblem.PREPROCESSOR_UNBALANCE_CONDITION, start, ppKeywords.findKey( buffer, start, len ) );
							        skipToNewLine();
							    }
								break;
							case ppEndif:
							    if( branchState( BRANCH_END ) ){
									if (nesting > 0) {
										--nesting;
									} else {
										skipToNewLine();
										return;
									}
							    } else {
							        //problem, ignore this one. 
							        handleProblem( IProblem.PREPROCESSOR_UNBALANCE_CONDITION, start, ppKeywords.findKey( buffer, start, len ) );
							        skipToNewLine();
							    }
								break;
						}
					}
				}
			} else if (c != '\n')
				skipToNewLine();
		}
	}
	
	private boolean skipOverWhiteSpace() {
		
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		int pos = bufferPos[bufferStackPos];
//		if( pos > 0 && pos < limit && buffer[pos] == '\n')
//			return false;
		
		boolean encounteredMultiLineComment = false;
		while (++bufferPos[bufferStackPos] < limit) {
			pos = bufferPos[bufferStackPos];
			switch (buffer[pos]) {
				case ' ':
				case '\t':
				case '\r':
					continue;
				case '/':
					if (pos + 1 < limit) {
						if (buffer[pos + 1] == '/') {
							// C++ comment, skip rest of line
							skipToNewLine();
							// leave the new line there
							--bufferPos[bufferStackPos];
							return false;
						} else if (buffer[pos + 1] == '*') {
							// C comment, find closing */
							for (bufferPos[bufferStackPos] += 2;
									bufferPos[bufferStackPos] < limit;
									++bufferPos[bufferStackPos]) {
								pos = bufferPos[bufferStackPos];
								if (buffer[pos] == '*'
										&& pos + 1 < limit
										&& buffer[pos + 1] == '/') {
									++bufferPos[bufferStackPos];
									encounteredMultiLineComment = true;
									break;
								}
							}
							continue;
						}
					}
					break;
				case '\\':
					if (pos + 1 < limit && buffer[pos + 1] == '\n') {
						// \n is a whitespace
						++bufferPos[bufferStackPos];
						continue;
					}
					if( pos + 1 < limit && buffer[ pos + 1 ] == '\r')
					{
						if( pos + 2 < limit && buffer[ pos + 2] == '\n' )
						{
							bufferPos[bufferStackPos] +=2;
							continue;
						}
					}
					break;
			}
			
			// fell out of switch without continuing, we're done
			--bufferPos[bufferStackPos];
			return encounteredMultiLineComment;
		}
		--bufferPos[bufferStackPos];
		return encounteredMultiLineComment;
	}
	
	private void skipOverNonWhiteSpace(){
	    skipOverNonWhiteSpace( false );
	}
	private boolean skipOverNonWhiteSpace( boolean stopAtPound ) {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		while (++bufferPos[bufferStackPos] < limit) {
			switch (buffer[bufferPos[bufferStackPos]]) {
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					--bufferPos[bufferStackPos];
					return true;
				case '/':
					int pos = bufferPos[bufferStackPos];
					if( pos +1 < limit && ( buffer[pos+1] == '/' ) || ( buffer[pos+1] == '*') )
					{
						--bufferPos[bufferStackPos];
						return true;
					}
					break;
													
				case '\\':
					pos = bufferPos[bufferStackPos];
					if (pos + 1 < limit && buffer[pos + 1] == '\n') {
						// \n is whitespace
						--bufferPos[bufferStackPos];
						return true;
					}
					break;
				case '"':
					boolean escaped = false; 
					if( bufferPos[bufferStackPos] -1  > 0 && buffer[bufferPos[bufferStackPos] -1 ] == '\\' )
						escaped = true;
					loop:
					while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
						switch (buffer[bufferPos[bufferStackPos]]) {
							case '\\':
								escaped = !escaped;
								continue;
							case '"':
								if (escaped) {
									escaped = false;
									continue;
								}
								break loop;
							case '\n':
								if( !escaped )
									break loop;
							case '/': 
								if( escaped && ( bufferPos[bufferStackPos] +1 < limit ) && 
										( buffer[bufferPos[ bufferStackPos ] + 1] == '/' ||
										  buffer[bufferPos[ bufferStackPos ] + 1] == '*' ) )
								{
									--bufferPos[bufferStackPos];
									return true;
								}
						
							default:
								escaped = false;
						}
					}
					//if we hit the limit here, then the outer while loop will advance
					//us 2 past the end and we'll back up one and still be past the end,
					//so back up here as well to leave us at the last char.
					if( bufferPos[bufferStackPos] == bufferLimit[bufferStackPos] )
					    bufferPos[bufferStackPos]--;
					break;
				case '\'':
					escaped = false;
					loop:
					while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
						switch (buffer[bufferPos[bufferStackPos]]) {
							case '\\':
								escaped = !escaped;
								continue;
							case '\'':
								if (escaped) {
									escaped = false;
									continue;
								}
								break loop;
							default:
								escaped = false;
						}
					}
					if( bufferPos[bufferStackPos] == bufferLimit[bufferStackPos] )
					    bufferPos[bufferStackPos]--;
					
					break;
				case '#' :
				    if( stopAtPound ){
						if( bufferPos[bufferStackPos] + 1 >= limit || buffer[ bufferPos[bufferStackPos] + 1] != '#' ){
						    --bufferPos[bufferStackPos];
						    return false;
						} 
						++bufferPos[ bufferStackPos ];
				    }
				    break;
			}
		}
		--bufferPos[bufferStackPos];
		return true;
	}

	private int skipOverMacroArg(){
	    char [] buffer = bufferStack[bufferStackPos];
	    int limit = bufferLimit[bufferStackPos];
	    int argEnd = bufferPos[bufferStackPos]--;
	    int nesting = 0;
	    while (++bufferPos[bufferStackPos] < limit) {
			switch (buffer[bufferPos[bufferStackPos]]) {
			    case '(':	
			        ++nesting; 
			        break;
			    case ')':
			        if( nesting == 0 ){
			            --bufferPos[bufferStackPos];
			            return argEnd;
			        } 
			        --nesting;
			        break;
				case ',':
				    if( nesting == 0 ){
				        --bufferPos[bufferStackPos];
			            return argEnd;
				    }
				    break;
				case '"':
					boolean escaped = false;
					loop:
					while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
						switch (buffer[bufferPos[bufferStackPos]]) {
							case '\\':
								escaped = !escaped;
								continue;
							case '"':
								if (escaped) {
									escaped = false;
									continue;
								}
								break loop;
							default:
								escaped = false;
						}
					}
					break;
			}
			argEnd = bufferPos[bufferStackPos];
			skipOverWhiteSpace();
		}
		--bufferPos[bufferStackPos];
		return argEnd;
	}

	private void skipOverIdentifier() {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		
		while (++bufferPos[bufferStackPos] < limit) {
			char c = buffer[bufferPos[bufferStackPos]];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| c == '_' || (c >= '0' && c <= '9') || Character.isUnicodeIdentifierPart(c)) {
				continue;
			} 
			break;

		}
		--bufferPos[bufferStackPos];
	}

	private void skipToNewLine() {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];
		int pos = ++bufferPos[bufferStackPos];
		
		if (pos < limit && buffer[pos] == '\n')
			return;
		
		boolean escaped = false;
		while (++bufferPos[bufferStackPos] < limit) {
			switch (buffer[bufferPos[bufferStackPos]]) {
				case '/':
					pos = bufferPos[bufferStackPos];
					if (pos + 1 < limit && buffer[pos + 1] == '*') {
						++bufferPos[bufferStackPos];
						while (++bufferPos[bufferStackPos] < limit) {
							pos = bufferPos[bufferStackPos];
							if (buffer[pos] == '*'
									&& pos + 1 < limit
									&& buffer[pos + 1] == '/') {
								++bufferPos[bufferStackPos];
								break;
							}
						}
					}
					break;
				case '\\':
					escaped = !escaped;
					continue;
				case '\n':
					if (escaped) {
						escaped = false;
						break;
					} 
					return;
					
			}
			escaped = false;
		}
	}
	
	private char[] handleFunctionStyleMacro(FunctionStyleMacro macro, boolean pushContext) {
		char[] buffer = bufferStack[bufferStackPos];
		int limit = bufferLimit[bufferStackPos];

		skipOverWhiteSpace();
		while( 	bufferPos[bufferStackPos] < limit && 
				buffer[bufferPos[bufferStackPos]] == '\\' && 
				bufferPos[bufferStackPos] + 1 < buffer.length && 
				buffer[bufferPos[bufferStackPos]+1] == '\n' ) 
		{
			bufferPos[bufferStackPos] += 2;
			skipOverWhiteSpace();
		}

		if (++bufferPos[bufferStackPos] >= limit
				|| buffer[bufferPos[bufferStackPos]] != '(' )
			return emptyCharArray;
		
		char[][] arglist = macro.arglist;
		int currarg = -1;
		CharArrayObjectMap argmap = new CharArrayObjectMap(arglist.length);
		
		while (bufferPos[bufferStackPos] < limit) {
			skipOverWhiteSpace();
			
			if (buffer[++bufferPos[bufferStackPos]] == ')') {
				// end of macro
				break;
			} else if( buffer[bufferPos[bufferStackPos]] == ',' ){
			    continue;
			}
			
			if (++currarg >= arglist.length || arglist[currarg] == null){
				// too many args
			    handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, bufferPos[bufferStackPos], macro.name );
				break;
			}
			
		    int argstart = bufferPos[bufferStackPos];
			int argend = skipOverMacroArg(); 
			
			char[] arg = emptyCharArray;
			int arglen = argend - argstart + 1;
			if (arglen > 0) {
				arg = new char[arglen];
				System.arraycopy(buffer, argstart, arg, 0, arglen);
			}
			
			//16.3.1 completely macro replace the arguments before substituting them in
			arg = replaceArgumentMacros( arg );
			argmap.put(arglist[currarg], arg);
		}
		
		int numArgs = arglist.length;
		for( int i = 0; i < arglist.length; i++ ){
		    if( arglist[i] == null ){
		        numArgs = i;
		        break;
		    }
		}
		if( argmap.size() < numArgs ){
		    handleProblem( IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, bufferPos[bufferStackPos], macro.name );
		}
		
		int size = expandFunctionStyleMacro(macro.expansion, argmap, null);
		char[] result = new char[size];
		expandFunctionStyleMacro(macro.expansion, argmap, result);
		if( pushContext )
			pushContext(result, macro);
		return result;
	}

	private char[] replaceArgumentMacros( char [] arg ){
		int limit = arg.length;
		int start = -1, end = -1;
		Object expObject = null;
		for( int pos = 0; pos < limit; pos++ ){
		    char c = arg[pos];
		    if( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || 
		        Character.isLetter( c ) || scannerExtension.isValidIdentifierStartCharacter( c ) )
		    {
		        start = pos;
		        while (++pos < limit) {
					c = arg[pos];
					if( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || (c >= '0' && c <= '9') || 
					    scannerExtension.isValidIdentifierCharacter(c) || Character.isUnicodeIdentifierPart(c) ) 
					{
						continue;
					} 
					break;
				}    
		        end = pos - 1; 
		    }

		    if( start != -1 && end >= start ){
		        //Check for macro expansion
		        expObject = definitions.get(arg, start, ( end - start + 1 ) );
		        if( expObject == null || !shouldExpandMacro( (IMacro) expObject) ){
				    expObject = null;
				    start = -1;
				    continue;
				}
				//else, break and expand macro
				break;
		    }
		}
		
		if( expObject == null )
		    return arg;
		
		char [] expansion = null;
		if( expObject instanceof FunctionStyleMacro ){
		    FunctionStyleMacro expMacro = (FunctionStyleMacro) expObject;
		    pushContext( ( start == 0 ) ? arg : CharArrayUtils.extract( arg, start, arg.length - start + 1 ) );
		    bufferPos[bufferStackPos] += end - start + 1;
		    expansion = handleFunctionStyleMacro( expMacro, false );
		    end = bufferPos[bufferStackPos];
		    popContext();
		} else if (expObject instanceof ObjectStyleMacro) {
			ObjectStyleMacro expMacro = (ObjectStyleMacro)expObject;
			expansion = expMacro.expansion;
		} else if (expObject instanceof char[]) {
			expansion = (char[])expObject;
		} else if( expObject instanceof DynamicStyleMacro ){
			DynamicStyleMacro expMacro = (DynamicStyleMacro) expObject;
			expansion = expMacro.execute(); 
		}

		if( expansion != null ){
			int newlength = start + expansion.length + ( limit - end - 1 );
			char [] result = new char [ newlength ];
			System.arraycopy( arg, 0, result, 0, start);
			System.arraycopy( expansion, 0, result, start, expansion.length );
			if( arg.length > end + 1 )
			    System.arraycopy( arg, end + 1, result, start + expansion.length, limit - end - 1 );
			
			//we need to put the macro on the context stack in order to detect recursive macros
			pushContext( emptyCharArray, expObject );
			arg = replaceArgumentMacros( result );  //rescan for more macros
			popContext();
		}
		return arg;
	}
	
	private int expandFunctionStyleMacro(
			char[] expansion,
			CharArrayObjectMap argmap,
			char[] result) {

		// The current position in the expansion string that we are looking at
		int pos = -1;
		// The last position in the expansion string that was copied over
		int lastcopy = -1;
		// The current write offset in the result string - also tells us the length of the result string
		int outpos = 0;
		// The first character in the current block of white space - there are times when we don't
		// want to copy over the whitespace
		int wsstart = -1;
		
		int limit = expansion.length;
		
		while (++pos < limit) {
			char c = expansion[pos];
			
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || Character.isUnicodeIdentifierPart(c)) {

				wsstart = -1;
				int idstart = pos;
				while (++pos < limit) {
					c = expansion[pos];
					if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
							|| (c >= '0' && c <= '9') || c == '_' || Character.isUnicodeIdentifierPart(c))) {
						break;
					}
				}
				--pos;
				
				Object repObject = argmap.get(expansion, idstart, pos - idstart + 1);
				if (repObject != null) {
					// copy what we haven't so far
					if (++lastcopy < idstart) {
						int n = idstart - lastcopy;
						if (result != null)
							System.arraycopy(expansion, lastcopy, result, outpos, n);
						outpos += n;
					}
					
					// copy the argument replacement value
					char[] rep = (char[]) repObject;
					if (result != null)
						System.arraycopy(rep, 0, result, outpos, rep.length);
					outpos += rep.length;

					lastcopy = pos;
				}
				
			} else if (c >= '0' && c < '9') {
				
				// skip over numbers - note the expanded definition of a number
				// to include alphanumeric characters - gcc seems to operate this way
				wsstart = -1;
				while (++pos < limit) {
					c = expansion[pos];
					if (!((c >= '0' && c <= '9') || c == '.' || c == '_') 
							|| (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
						break;
				}

			} else if (c == '"') {
				
				// skip over strings
				wsstart = -1;
				boolean escaped = false;
				while (++pos < limit) {
					c = expansion[pos];
					if (c == '"') {
						if (!escaped)
							break;
					} else if (c == '\\') {
						escaped = !escaped;
					}
					escaped = false;
				}
				
			} else if (c == '\'') {

				// skip over character literals
				wsstart = -1;
				boolean escaped = false;
				while (++pos < limit) {
					c = expansion[pos];
					if (c == '\'') {
						if (!escaped)
							break;
					} else if (c == '\\') {
						escaped = !escaped;
					}
					escaped = false;
				}
				
			} else if (c == ' ' || c == '\t') {

				// obvious whitespace
				if (wsstart < 0)
					wsstart = pos;
				
			} else if (c == '/' && pos + 1 < limit) {

				// less than obvious, comments are whitespace
				c = expansion[++pos];
				if (c == '/') {
					// copy up to here or before the last whitespace
					++lastcopy;
					int n = wsstart < 0
						? pos - 1 - lastcopy
						: wsstart - lastcopy;
					if (result != null)
						System.arraycopy(expansion, lastcopy, result, outpos, n);
					outpos += n;

					// skip the rest
					lastcopy = expansion.length - 1;
				} else if (c == '*') {
					if (wsstart < 1)
						wsstart = pos - 1;
					while (++pos < limit) {
						if (expansion[pos] == '*'
								&& pos + 1 < limit
								&& expansion[pos + 1] == '/') {
							++pos;
							break;
						}
					}
				} else 
					wsstart = -1;

				
			} else if (c == '\\' && pos + 1 < limit
					&& expansion[pos + 1] == 'n') {
				// skip over this
				++pos;
				
			} else if (c == '#') {
				
				if (pos + 1 < limit && expansion[pos + 1] == '#') {
					++pos;
					// skip whitespace
					if (wsstart < 0)
						wsstart = pos - 1;
					while (++pos < limit) {
						switch (expansion[pos]) {
							case ' ':
							case '\t':
								continue;
							
							case '/':
								if (pos + 1 < limit) {
									c = expansion[pos + 1];
									if (c == '/')
										// skip over everything
										pos = expansion.length;
									else if (c == '*') {
										++pos;
										while (++pos < limit) {
											if (expansion[pos] == '*'
													&& pos + 1 < limit
													&& expansion[pos + 1] == '/') {
												++pos;
												break;
											}
										}
										continue;
									}
								}
						}
						break;
					}

					// copy everything up to the whitespace
					int n = wsstart - (++lastcopy);
					if (n > 0 && result != null)
						System.arraycopy(expansion, lastcopy, result, outpos, n);
					outpos += n;
					
					// skip over the ## and the whitespace around it
					lastcopy = --pos;
					wsstart = -1;

				} else {
					// stringify
					
					// copy what we haven't so far
					if (++lastcopy < pos) {
						int n = pos - lastcopy;
						if (result != null)
							System.arraycopy(expansion, lastcopy, result, outpos, n);
						outpos += n;
					}

					// skip whitespace
					while (++pos < limit) {
						switch (expansion[pos]) {
							case ' ':
							case '\t':
								continue;
							case '/':
								if (pos + 1 < limit) {
									c = expansion[pos + 1];
									if (c == '/')
										// skip over everything
										pos = expansion.length;
									else if (c == '*') {
										++pos;
										while (++pos < limit) {
											if (expansion[pos] == '*'
													&& pos + 1 < limit
													&& expansion[pos + 1] == '/') {
												++pos;
												break;
											}
										}
										continue;
									}
								}
							//TODO handle comments
						}
						break;
					}
				
					// grab the identifier
					c = expansion[pos];
					int idstart = pos;
					if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'X') || c == '_' || Character.isUnicodeIdentifierPart(c)) {
						while (++pos < limit) {
						    c = expansion[pos];
							if( !((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'X')
									|| (c >= '0' && c <= '9') || c == '_' || Character.isUnicodeIdentifierPart(c)) )
								break;
						}
					} // else TODO something
					--pos;
					int idlen = pos - idstart + 1;
					char[] argvalue = (char[])argmap.get(expansion, idstart, idlen);
					if (argvalue != null) {
					    //16.3.2-2 ... a \ character is inserted before each " and \ character
					    //of a character literal or string literal
					    
					    //technically, we are also supposed to replace each occurence of whitespace 
					    //(including comments) in  the argument with a single space. But, at this time
					    //we don't really care what the contents of the string are, just that we get the string
					    //so we won't bother doing that
						if (result != null) {
							result[outpos++] = '"';
							for( int i = 0; i < argvalue.length; i++ ){
							    if( argvalue[i] == '"' || argvalue[i] == '\\' )
							        result[outpos++] = '\\';
							    if( argvalue[i] == '\r' || argvalue[i] == '\n' )
							        result[outpos++] = ' ';
							    else
							        result[outpos++] = argvalue[i];
							}
							result[outpos++] = '"';
						} else {
						    for( int i = 0; i < argvalue.length; i++ ){
						        if( argvalue[i] == '"' || argvalue[i] == '\\' )
						            ++outpos;
						        ++outpos;
						    }
						    outpos += 2;
						}
					}
					lastcopy = pos;
				}
			} else {
				
				// not sure what it is but it sure ain't whitespace
				wsstart = -1;
			}
			
		}

		if (wsstart < 0 && ++lastcopy < expansion.length) {
			int n = expansion.length - lastcopy;
			if (result != null)
				System.arraycopy(expansion, lastcopy, result, outpos, n);
			outpos += n;
		}
		
		return outpos;
	}

	// standard built-ins
	private static final ObjectStyleMacro __cplusplus
		= new ObjectStyleMacro("__cplusplus".toCharArray(), "1".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __STDC__
		= new ObjectStyleMacro("__STDC__".toCharArray(), "1".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __STDC_HOSTED__
	= new ObjectStyleMacro("__STDC_HOSTED_".toCharArray(), "1".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __STDC_VERSION__
	= new ObjectStyleMacro("__STDC_VERSION_".toCharArray(), "199901L".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private final DynamicStyleMacro __FILE__ = 
		new DynamicStyleMacro( "__FILE__".toCharArray()) { //$NON-NLS-1$

			public char[] execute() {
				StringBuffer buffer = new StringBuffer( "\""); //$NON-NLS-1$
				buffer.append( getCurrentFilename() );
				buffer.append( '\"');
				return buffer.toString().toCharArray();
			} };
	private final DynamicStyleMacro __DATE__ = 
		new DynamicStyleMacro( "__DATE__".toCharArray()) { //$NON-NLS-1$

		private final void append( StringBuffer buffer, int value )
		{
			if( value < 10 )
				buffer.append( "0" ); //$NON-NLS-1$
			buffer.append( value );
		}

		public char[] execute() {
			StringBuffer buffer = new StringBuffer( "\""); //$NON-NLS-1$
			Calendar cal = Calendar.getInstance();
			buffer.append( cal.get( Calendar.MONTH ));
			buffer.append( " " ); //$NON-NLS-1$
			append( buffer, cal.get( Calendar.DAY_OF_MONTH ));
			buffer.append( " " ); //$NON-NLS-1$
			buffer.append( cal.get(Calendar.YEAR));
			buffer.append( "\""); //$NON-NLS-1$
			return buffer.toString().toCharArray();
		} 
	};
	private final DynamicStyleMacro __TIME__ = 
		new DynamicStyleMacro( "__TIME__".toCharArray()) { //$NON-NLS-1$

			private final void append( StringBuffer buffer, int value )
			{
				if( value < 10 )
					buffer.append( "0" ); //$NON-NLS-1$
				buffer.append( value );
			}
			public char[] execute() {
				StringBuffer buffer = new StringBuffer( "\""); //$NON-NLS-1$
				Calendar cal = Calendar.getInstance();
				append( buffer, cal.get( Calendar.HOUR ));
				buffer.append( ":"); //$NON-NLS-1$
				append( buffer, cal.get( Calendar.MINUTE));
				buffer.append( ":"); //$NON-NLS-1$
				append( buffer, cal.get( Calendar.SECOND));
				buffer.append( "\""); //$NON-NLS-1$
				return buffer.toString().toCharArray();
			} 
	};
	private final DynamicStyleMacro __LINE__ = 
		new DynamicStyleMacro( "__LINE__".toCharArray() ) { //$NON-NLS-1$

			public char[] execute() {
				int lineNumber = lineNumbers[ bufferStackPos ];
				return Long.toString( lineNumber ).toCharArray();
			} 
	};

	
	

	private IASTFactory astFactory;

	private int offsetBoundary = -1;
	
	protected void setupBuiltInMacros() {

		definitions.put(__STDC__.name, __STDC__);
		definitions.put(__FILE__.name, __FILE__);
		definitions.put(__DATE__.name, __DATE__ );
		definitions.put(__TIME__.name, __TIME__ );
		definitions.put(__LINE__.name, __LINE__ );
		
		if( language == ParserLanguage.CPP )
			definitions.put(__cplusplus.name, __cplusplus);
		else
		{
			definitions.put( __STDC_HOSTED__.name, __STDC_HOSTED__ );
			definitions.put( __STDC_VERSION__.name, __STDC_VERSION__ );
		}
		
		scannerExtension.setupBuiltInMacros( this );		
		
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#setASTFactory(org.eclipse.cdt.core.parser.ast.IASTFactory)
	 */
	public final void setASTFactory(IASTFactory f) {
		astFactory = f;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#setOffsetBoundary(int)
	 */
	public final void setOffsetBoundary(int offset) {
		offsetBoundary = offset;
		bufferLimit[0] = offset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getASTFactory()
	 */
	public final IASTFactory getASTFactory() {
		if( astFactory == null )
			astFactory = ParserFactory.createASTFactory( parserMode, language );
		return astFactory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getClientRequestor()
	 */
	public ISourceElementRequestor getClientRequestor() {
		return requestor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getLanguage()
	 */
	public ParserLanguage getLanguage() {
		return language;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getLogService()
	 */
	public IParserLogService getLogService() {
		return log;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getParserMode()
	 */
	public ParserMode getParserMode() {
		return parserMode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getProblemFactory()
	 */
	public IProblemFactory getProblemFactory() {
		return spf;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getWorkingCopies()
	 */
	public Iterator getWorkingCopies() {
		if( workingCopies == null ) return EmptyIterator.EMPTY_ITERATOR;
		return workingCopies.iterator();
	}

	
	public final char[] getCurrentFilename() {
		for( int i = bufferStackPos; i >= 0; --i )
		{
			if( bufferData[i] instanceof InclusionData )
				return ((InclusionData)bufferData[i]).reader.filename;
			if( bufferData[i] instanceof CodeReader )
				return ((CodeReader)bufferData[i]).filename;
		}
		return emptyCharArray;
	}

	private final int getCurrentFileIndex() {
		for( int i = bufferStackPos; i >= 0; --i )
		{
			if( bufferData[i] instanceof InclusionData || bufferData[i] instanceof CodeReader ) 
				return i;
		}
		return 0;
	}
	
	private static CharArrayIntMap keywords;
	private static CharArrayIntMap ppKeywords;
	private static final int ppIf		= 0;
	private static final int ppIfdef	= 1;
	private static final int ppIfndef	= 2;
	private static final int ppElif		= 3;
	private static final int ppElse		= 4;
	private static final int ppEndif	= 5;
	private static final int ppInclude	= 6;
	private static final int ppDefine	= 7;
	private static final int ppUndef	= 8;
	private static final int ppError	= 9;
	private static final int ppInclude_next = 10;

	private static final char[] TAB = { '\t' };
	private static final char[] SPACE = { ' ' };
	
	static {
		keywords = new CharArrayIntMap(IToken.tLAST, -1);
		
		// Common keywords
		keywords.put("auto".toCharArray(), IToken.t_auto); //$NON-NLS-1$
		keywords.put("break".toCharArray(), IToken.t_break); //$NON-NLS-1$
		keywords.put("case".toCharArray(), IToken.t_case); //$NON-NLS-1$
		keywords.put("char".toCharArray(), IToken.t_char); //$NON-NLS-1$
		keywords.put("const".toCharArray(), IToken.t_const); //$NON-NLS-1$
		keywords.put("continue".toCharArray(), IToken.t_continue); //$NON-NLS-1$
		keywords.put("default".toCharArray(), IToken.t_default); //$NON-NLS-1$
		keywords.put("do".toCharArray(), IToken.t_do); //$NON-NLS-1$
		keywords.put("double".toCharArray(), IToken.t_double); //$NON-NLS-1$
		keywords.put("else".toCharArray(), IToken.t_else); //$NON-NLS-1$
		keywords.put("enum".toCharArray(), IToken.t_enum); //$NON-NLS-1$
		keywords.put("extern".toCharArray(), IToken.t_extern); //$NON-NLS-1$
		keywords.put("float".toCharArray(), IToken.t_float); //$NON-NLS-1$
		keywords.put("for".toCharArray(), IToken.t_for); //$NON-NLS-1$
		keywords.put("goto".toCharArray(), IToken.t_goto); //$NON-NLS-1$
		keywords.put("if".toCharArray(), IToken.t_if); //$NON-NLS-1$
		keywords.put("inline".toCharArray(), IToken.t_inline); //$NON-NLS-1$
		keywords.put("int".toCharArray(), IToken.t_int); //$NON-NLS-1$
		keywords.put("long".toCharArray(), IToken.t_long); //$NON-NLS-1$
		keywords.put("register".toCharArray(), IToken.t_register); //$NON-NLS-1$
		keywords.put("return".toCharArray(), IToken.t_return); //$NON-NLS-1$
		keywords.put("short".toCharArray(), IToken.t_short); //$NON-NLS-1$
		keywords.put("signed".toCharArray(), IToken.t_signed); //$NON-NLS-1$
		keywords.put("sizeof".toCharArray(), IToken.t_sizeof); //$NON-NLS-1$
		keywords.put("static".toCharArray(), IToken.t_static); //$NON-NLS-1$
		keywords.put("struct".toCharArray(), IToken.t_struct); //$NON-NLS-1$
		keywords.put("switch".toCharArray(), IToken.t_switch); //$NON-NLS-1$
		keywords.put("typedef".toCharArray(), IToken.t_typedef); //$NON-NLS-1$
		keywords.put("union".toCharArray(), IToken.t_union); //$NON-NLS-1$
		keywords.put("unsigned".toCharArray(), IToken.t_unsigned); //$NON-NLS-1$
		keywords.put("void".toCharArray(), IToken.t_void); //$NON-NLS-1$
		keywords.put("volatile".toCharArray(), IToken.t_volatile); //$NON-NLS-1$
		keywords.put("while".toCharArray(), IToken.t_while); //$NON-NLS-1$

		// ANSI C keywords
		keywords.put("restrict".toCharArray(), IToken.t_restrict); //$NON-NLS-1$
		keywords.put("_Bool".toCharArray(), IToken.t__Bool); //$NON-NLS-1$
		keywords.put("_Complex".toCharArray(), IToken.t__Complex); //$NON-NLS-1$
		keywords.put("_Imaginary".toCharArray(), IToken.t__Imaginary); //$NON-NLS-1$

		// C++ Keywords
		keywords.put("asm".toCharArray(), IToken.t_asm); //$NON-NLS-1$
		keywords.put("bool".toCharArray(), IToken.t_bool); //$NON-NLS-1$
		keywords.put("catch".toCharArray(), IToken.t_catch); //$NON-NLS-1$
		keywords.put("class".toCharArray(), IToken.t_class); //$NON-NLS-1$
		keywords.put("const_cast".toCharArray(), IToken.t_const_cast); //$NON-NLS-1$
		keywords.put("delete".toCharArray(), IToken.t_delete); //$NON-NLS-1$
		keywords.put("dynamic_cast".toCharArray(), IToken.t_dynamic_cast); //$NON-NLS-1$
		keywords.put("explicit".toCharArray(), IToken.t_explicit); //$NON-NLS-1$
		keywords.put("export".toCharArray(), IToken.t_export); //$NON-NLS-1$
		keywords.put("false".toCharArray(), IToken.t_false); //$NON-NLS-1$
		keywords.put("friend".toCharArray(), IToken.t_friend); //$NON-NLS-1$
		keywords.put("mutable".toCharArray(), IToken.t_mutable); //$NON-NLS-1$
		keywords.put("namespace".toCharArray(), IToken.t_namespace); //$NON-NLS-1$
		keywords.put("new".toCharArray(), IToken.t_new); //$NON-NLS-1$
		keywords.put("operator".toCharArray(), IToken.t_operator); //$NON-NLS-1$
		keywords.put("private".toCharArray(), IToken.t_private); //$NON-NLS-1$
		keywords.put("protected".toCharArray(), IToken.t_protected); //$NON-NLS-1$
		keywords.put("public".toCharArray(), IToken.t_public); //$NON-NLS-1$
		keywords.put("reinterpret_cast".toCharArray(), IToken.t_reinterpret_cast); //$NON-NLS-1$
		keywords.put("static_cast".toCharArray(), IToken.t_static_cast); //$NON-NLS-1$
		keywords.put("template".toCharArray(), IToken.t_template); //$NON-NLS-1$
		keywords.put("this".toCharArray(), IToken.t_this); //$NON-NLS-1$
		keywords.put("throw".toCharArray(), IToken.t_throw); //$NON-NLS-1$
		keywords.put("true".toCharArray(), IToken.t_true); //$NON-NLS-1$
		keywords.put("try".toCharArray(), IToken.t_try); //$NON-NLS-1$
		keywords.put("typeid".toCharArray(), IToken.t_typeid); //$NON-NLS-1$
		keywords.put("typename".toCharArray(), IToken.t_typename); //$NON-NLS-1$
		keywords.put("using".toCharArray(), IToken.t_using); //$NON-NLS-1$
		keywords.put("virtual".toCharArray(), IToken.t_virtual); //$NON-NLS-1$
		keywords.put("wchar_t".toCharArray(), IToken.t_wchar_t); //$NON-NLS-1$

		// C++ operator alternative
		keywords.put("and".toCharArray(), IToken.t_and); //$NON-NLS-1$
		keywords.put("and_eq".toCharArray(), IToken.t_and_eq); //$NON-NLS-1$
		keywords.put("bitand".toCharArray(), IToken.t_bitand); //$NON-NLS-1$
		keywords.put("bitor".toCharArray(), IToken.t_bitor); //$NON-NLS-1$
		keywords.put("compl".toCharArray(), IToken.t_compl); //$NON-NLS-1$
		keywords.put("not".toCharArray(), IToken.t_not); //$NON-NLS-1$
		keywords.put("not_eq".toCharArray(), IToken.t_not_eq); //$NON-NLS-1$
		keywords.put("or".toCharArray(), IToken.t_or); //$NON-NLS-1$
		keywords.put("or_eq".toCharArray(), IToken.t_or_eq); //$NON-NLS-1$
		keywords.put("xor".toCharArray(), IToken.t_xor); //$NON-NLS-1$
		keywords.put("xor_eq".toCharArray(), IToken.t_xor_eq); //$NON-NLS-1$
		
		// Preprocessor keywords
		ppKeywords = new CharArrayIntMap(16, -1);
		ppKeywords.put("if".toCharArray(), ppIf); //$NON-NLS-1$
		ppKeywords.put("ifdef".toCharArray(), ppIfdef); //$NON-NLS-1$
		ppKeywords.put("ifndef".toCharArray(), ppIfndef); //$NON-NLS-1$
		ppKeywords.put("elif".toCharArray(), ppElif); //$NON-NLS-1$
		ppKeywords.put("else".toCharArray(), ppElse); //$NON-NLS-1$
		ppKeywords.put("endif".toCharArray(), ppEndif); //$NON-NLS-1$
		ppKeywords.put("include".toCharArray(), ppInclude); //$NON-NLS-1$
		ppKeywords.put("define".toCharArray(), ppDefine); //$NON-NLS-1$
		ppKeywords.put("undef".toCharArray(), ppUndef); //$NON-NLS-1$
		ppKeywords.put("error".toCharArray(), ppError); //$NON-NLS-1$
		ppKeywords.put("include_next".toCharArray(), ppInclude_next); //$NON-NLS-1$
	}
	
	/**
	 * @param definition
	 */
	protected void handleCompletionOnDefinition(String definition) throws EndOfFileException {
		IASTCompletionNode node = new ASTCompletionNode( IASTCompletionNode.CompletionKind.MACRO_REFERENCE, 
				null, null, definition, KeywordSets.getKeywords(KeywordSetKey.EMPTY, language), EMPTY_STRING, null );
		
		throw new OffsetLimitReachedException( node ); 
	}

	/**
	 * @param expression2
	 */
	protected void handleCompletionOnExpression(char [] buffer ) throws EndOfFileException {
		
		IASTCompletionNode.CompletionKind kind = IASTCompletionNode.CompletionKind.MACRO_REFERENCE;
		int lastSpace = CharArrayUtils.lastIndexOf( SPACE, buffer );
		int lastTab = CharArrayUtils.lastIndexOf( TAB, buffer );
		int max = lastSpace > lastTab ? lastSpace : lastTab;
		
		char [] prefix = CharArrayUtils.trim( CharArrayUtils.extract( buffer, max, buffer.length - max ) );
		for( int i = 0; i < prefix.length; ++i )
		{
			char c = prefix[i];
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| c == '_' || (c >= '0' && c <= '9') || Character.isUnicodeIdentifierPart(c) ) 
				continue;
			handleInvalidCompletion();
		}
		IASTCompletionNode node = new ASTCompletionNode( kind, 
				null, null, new String( prefix ), 
				KeywordSets.getKeywords(((kind == IASTCompletionNode.CompletionKind.NO_SUCH_KIND )? KeywordSetKey.EMPTY : KeywordSetKey.MACRO), language), EMPTY_STRING, null );
		
		throw new OffsetLimitReachedException( node );
	}

	
	protected void handleInvalidCompletion() throws EndOfFileException
	{
		throw new OffsetLimitReachedException( new ASTCompletionNode( IASTCompletionNode.CompletionKind.UNREACHABLE_CODE, null, null, EMPTY_STRING, KeywordSets.getKeywords(KeywordSetKey.EMPTY, language ) , EMPTY_STRING, null)); 
	}
	
	protected void handleCompletionOnPreprocessorDirective( String prefix ) throws EndOfFileException 
	{
		throw new OffsetLimitReachedException( new ASTCompletionNode( IASTCompletionNode.CompletionKind.PREPROCESSOR_DIRECTIVE, null, null, prefix, KeywordSets.getKeywords(KeywordSetKey.PP_DIRECTIVE, language ), EMPTY_STRING, null));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerData#getCurrentOffset()
	 */
	public int getCurrentOffset() {
		return bufferPos[ bufferStackPos ];
	}

}
