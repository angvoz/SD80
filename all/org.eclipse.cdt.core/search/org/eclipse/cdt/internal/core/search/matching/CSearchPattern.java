/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 13, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IQuickParseCallback;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.impl.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.impl.IndexInput;
import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.cdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * @author aniefer
 */
public abstract class CSearchPattern implements ICSearchConstants, ICSearchPattern, IIndexConstants {
	
	public static final int IMPOSSIBLE_MATCH = 0;
	public static final int POSSIBLE_MATCH   = 1;
	public static final int ACCURATE_MATCH   = 2;
	public static final int INACCURATE_MATCH = 3;
	
	protected static class Requestor extends NullSourceElementRequestor
	{
		public Requestor( ParserMode mode )
		{
			super( mode );
		}
		
		public boolean acceptProblem( IProblem problem )
		{
			if( problem.getID() == IProblem.SCANNER_BAD_CHARACTER ) return false;
			return super.acceptProblem( problem );
		}
	}
	
	private static Requestor callback = new Requestor( ParserMode.COMPLETE_PARSE );
	/**
	 * @param matchMode
	 * @param caseSensitive
	 */
	public CSearchPattern(int matchMode, boolean caseSensitive, LimitTo limitTo ) {
		_matchMode = matchMode;
		_caseSensitive = caseSensitive;
		_limitTo = limitTo;
	}

	public CSearchPattern() {
		super();
	}

	public LimitTo getLimitTo(){
		return _limitTo;
	}

	public boolean canAccept(LimitTo limit) {
		return ( limit == getLimitTo() );
	}

	public static CSearchPattern createPattern( String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive ){
		if( patternString == null || patternString.length() == 0 ){
			return null;
		}
		
		CSearchPattern pattern = null;
		if( searchFor == TYPE || searchFor == CLASS || searchFor == STRUCT || 
			searchFor == ENUM || searchFor == UNION || searchFor == CLASS_STRUCT  ||
			searchFor == TYPEDEF )
		{
			pattern = createClassPattern( patternString, searchFor, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == METHOD || searchFor == FUNCTION ){
			pattern = createMethodPattern( patternString, searchFor, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == FIELD || searchFor == VAR ){
			pattern = createFieldPattern( patternString, searchFor, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == NAMESPACE ){
			pattern = createNamespacePattern( patternString, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == MACRO ){
			pattern = createMacroPattern( patternString, limitTo, matchMode, caseSensitive );
		} else if ( searchFor == INCLUDE){
			pattern = createIncludePattern( patternString, limitTo, matchMode, caseSensitive);
		}
	
		return pattern;
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createIncludePattern(String patternString, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		if( limitTo != REFERENCES )
			return null;
			
		return new IncludePattern ( patternString.toCharArray(), matchMode, limitTo, caseSensitive );	
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createMacroPattern(String patternString, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		if( limitTo != DECLARATIONS && limitTo != ALL_OCCURRENCES )
			return null;
			
		return new MacroDeclarationPattern( patternString.toCharArray(), matchMode, DECLARATIONS, caseSensitive );	
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createNamespacePattern(String patternString, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		if( limitTo == ALL_OCCURRENCES ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createNamespacePattern( patternString, DECLARATIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createNamespacePattern( patternString, DEFINITIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createNamespacePattern( patternString, REFERENCES, matchMode, caseSensitive ) );
			return orPattern;
		}
		
		IScanner scanner = null;
		try {
			scanner =
				ParserFactory.createScanner(
					new StringReader(patternString),
					"TEXT",
					new ScannerInfo(),
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP,
					callback, 
					nullLog);
		} catch (ParserFactoryError e) {

		}
		LinkedList list = scanForNames( scanner, null );
		
		char [] name = (char []) list.removeLast();
		char [][] qualifications = new char [0][];
		
		return new NamespaceDeclarationPattern( name, (char[][]) list.toArray( qualifications ), matchMode, limitTo, caseSensitive );
	}

//	/**
//	 * @param patternString
//	 * @param limitTo
//	 * @param matchMode
//	 * @param caseSensitive
//	 * @return
//	 */
//	private static CSearchPattern createFunctionPattern(String patternString, LimitTo limitTo, int matchMode, boolean caseSensitive) {
//		if( limitTo == ALL_OCCURRENCES ){
//			OrPattern orPattern = new OrPattern();
//			orPattern.addPattern( createFunctionPattern( patternString, DECLARATIONS, matchMode, caseSensitive ) );
//			orPattern.addPattern( createFunctionPattern( patternString, REFERENCES, matchMode, caseSensitive ) );
//			orPattern.addPattern( createFunctionPattern( patternString, DEFINITIONS, matchMode, caseSensitive ) );
//			return orPattern;
//		}
//		
//		int index = patternString.indexOf( '(' );
//		
//		String paramString = ( index == -1 ) ? "" : patternString.substring( index );
//		
//		String nameString = ( index == -1 ) ? patternString : patternString.substring( 0, index );
//				
//		IScanner scanner = ParserFactory.createScanner( new StringReader( paramString ), "TEXT", new ScannerInfo(), ParserMode.QUICK_PARSE, null );
//				
//		LinkedList params = scanForParameters( scanner );
//				
//		char [] name = nameString.toCharArray();
//		char [][] parameters = new char [0][];
//		parameters = (char[][])params.toArray( parameters );
//				
//		return new MethodDeclarationPattern( name, parameters, matchMode, FUNCTION, limitTo, caseSensitive );
//	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createFieldPattern(String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		if( limitTo == ALL_OCCURRENCES ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createFieldPattern( patternString, searchFor, DECLARATIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createFieldPattern( patternString, searchFor, REFERENCES, matchMode, caseSensitive ) );
			orPattern.addPattern( createFieldPattern( patternString, searchFor, DEFINITIONS, matchMode, caseSensitive ) );
			return orPattern;
		}
		
		IScanner scanner=null;
		try {
			scanner =
				ParserFactory.createScanner(
					new StringReader(patternString),
					"TEXT",
					new ScannerInfo(),
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP,
					callback, nullLog);
		} catch (ParserFactoryError e) {

		}
		LinkedList list = scanForNames( scanner, null );
		
		char [] name = (char []) list.removeLast();
		char [][] qualifications = new char[0][];
		
		return new FieldDeclarationPattern( name, (char[][]) list.toArray( qualifications ), matchMode, searchFor, limitTo, caseSensitive );
	}

	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createMethodPattern(String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive) {

		if( limitTo == ALL_OCCURRENCES ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createMethodPattern( patternString, searchFor, DECLARATIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createMethodPattern( patternString, searchFor, REFERENCES, matchMode, caseSensitive ) );
			orPattern.addPattern( createMethodPattern( patternString, searchFor, DEFINITIONS, matchMode, caseSensitive ) );
			return orPattern;
		}
				
		int index = patternString.indexOf( '(' );
		String paramString = ( index == -1 ) ? "" : patternString.substring( index );
		String nameString = ( index == -1 ) ? patternString : patternString.substring( 0, index );
		
		IScanner scanner=null;
		try {
			scanner =
				ParserFactory.createScanner(
					new StringReader(nameString),
					"TEXT",
					new ScannerInfo(),
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP,
					callback, nullLog);
		} catch (ParserFactoryError e) {
		}
		
		LinkedList names = scanForNames( scanner, null );

		LinkedList params = scanForParameters( paramString );
		
		char [] name = (char [])names.removeLast();
		char [][] qualifications = new char[0][];
		qualifications = (char[][])names.toArray( qualifications );
		char [][] parameters = new char [0][];
		parameters = (char[][])params.toArray( parameters );
		
		return new MethodDeclarationPattern( name, qualifications, parameters, matchMode, searchFor, limitTo, caseSensitive );
	}

	private static final IParserLogService nullLog = new NullLogService();
	/**
	 * @param patternString
	 * @param limitTo
	 * @param matchMode
	 * @param caseSensitive
	 * @return
	 */
	private static CSearchPattern createClassPattern(String patternString, SearchFor searchFor, LimitTo limitTo, int matchMode, boolean caseSensitive) {
		
		if( limitTo == ALL_OCCURRENCES ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createClassPattern( patternString, searchFor, DECLARATIONS, matchMode, caseSensitive ) );
			orPattern.addPattern( createClassPattern( patternString, searchFor, REFERENCES, matchMode, caseSensitive ) );
			return orPattern;
		}
		
		if( searchFor == CLASS_STRUCT ){
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern( createClassPattern( patternString, CLASS, limitTo, matchMode, caseSensitive ) );
			orPattern.addPattern( createClassPattern( patternString, STRUCT, limitTo, matchMode, caseSensitive ) );
			return orPattern;
		}
//		 else if( searchFor == TYPE ){
//			OrPattern orPattern = new OrPattern();
//			orPattern.addPattern( createClassPattern( patternString, CLASS, limitTo, matchMode, caseSensitive ) );
//			orPattern.addPattern( createClassPattern( patternString, STRUCT, limitTo, matchMode, caseSensitive ) );
//			orPattern.addPattern( createClassPattern( patternString, UNION, limitTo, matchMode, caseSensitive ) );
//			orPattern.addPattern( createClassPattern( patternString, ENUM, limitTo, matchMode, caseSensitive ) );
//			orPattern.addPattern( createClassPattern( patternString, TYPEDEF, limitTo, matchMode, caseSensitive ) );
//			return orPattern;
//		}
		
		IScanner scanner =null;
		try {
			scanner =
				ParserFactory.createScanner(
					new StringReader(patternString),
					"TEXT",
					new ScannerInfo(),
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP,
					callback, nullLog );
		} catch (ParserFactoryError e1) {
		}
		
		IToken token = null;
		
		try {
			token = scanner.nextToken();
		} catch (EndOfFileException e) {
		} catch (ScannerException e) {
		}
		
		if( token != null ){
			boolean nullifyToken = true;
			if( token.getType() == IToken.t_class ){
				searchFor = CLASS;
			} else if ( token.getType() == IToken.t_struct ){
				searchFor = STRUCT;
			} else if ( token.getType() == IToken.t_union ){
				searchFor = UNION;
			} else if ( token.getType() == IToken.t_enum ){
				searchFor = ENUM;
			} else if ( token.getType() == IToken.t_typedef ){
				searchFor = TYPEDEF;
			} else {
				nullifyToken = false;
			}
			if( nullifyToken )
				token = null;
		}
			
		LinkedList list = scanForNames( scanner, token );
		
		char[] name = (char [])list.removeLast();
		char [][] qualifications = new char[0][];
		
		return new ClassDeclarationPattern( name, (char[][])list.toArray( qualifications ), searchFor, limitTo, matchMode, caseSensitive );
	}



	/**
	 * @param scanner
	 * @param object
	 * @return
	 */
	private static LinkedList scanForParameters( String paramString ) {
		LinkedList list = new LinkedList();
		
		if( paramString == null || paramString.equals("") )
			return list;
		
		String functionString = "void f " + paramString + ";";
				
		IScanner scanner=null;
		try {
			scanner =
				ParserFactory.createScanner(
					new StringReader(functionString),
					"TEXT",
					new ScannerInfo(),
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP,
					callback,new NullLogService());
		} catch (ParserFactoryError e1) {
		}
		IQuickParseCallback callback = ParserFactory.createQuickParseCallback();			   
		IParser parser=null;
		try {
			parser =
				ParserFactory.createParser(
					scanner,
					callback,
					ParserMode.QUICK_PARSE,
					ParserLanguage.CPP, ParserUtil.getParserLogService());
		} catch (ParserFactoryError e2) {
		} 

		if( parser.parse() ){
			IASTCompilationUnit compUnit = callback.getCompilationUnit();
			Iterator declarations = null;
			try {
				declarations = compUnit.getDeclarations();
			} catch (ASTNotImplementedException e) {
			}
			
			if( declarations == null || ! declarations.hasNext() )
				return null;
			IASTFunction function = (IASTFunction) declarations.next();
			
			Iterator parameters = function.getParameters();
			char [] param = null;
			while( parameters.hasNext() ){
				param = getParamString( (IASTParameterDeclaration)parameters.next() );
				list.add( param );
			}
			
			if (param == null){
				//This means that no params have been added (i.e. empty brackets - void case)
				param = "void ".toCharArray();
				list.add (param); 
			}
		}
		
		return list;
	}
		
	static public char [] getParamString( IASTParameterDeclaration param ){
		if( param == null ) return null;
		 
		String signature = "";
		
		IASTTypeSpecifier typeSpec = param.getTypeSpecifier();
		if( typeSpec instanceof IASTSimpleTypeSpecifier ){
			IASTSimpleTypeSpecifier simple = (IASTSimpleTypeSpecifier)typeSpec;
			signature += simple.getTypename();
		} else if( typeSpec instanceof IASTElaboratedTypeSpecifier ){
			IASTElaboratedTypeSpecifier elaborated = (IASTElaboratedTypeSpecifier)typeSpec;
			if( elaborated.getClassKind() == ASTClassKind.CLASS ){
				signature += "class ";
			} else if( elaborated.getClassKind() == ASTClassKind.ENUM ) {
				signature += "enum ";
			} else if( elaborated.getClassKind() == ASTClassKind.STRUCT ) {
				signature += "struct ";
			} else if( elaborated.getClassKind() == ASTClassKind.UNION ) {
				signature += "union";
			}

			signature += elaborated.getName();
		} else if( typeSpec instanceof IASTClassSpecifier ){
			IASTClassSpecifier classSpec = (IASTClassSpecifier)typeSpec;
			signature += classSpec.getName();
		} else if( typeSpec instanceof IASTEnumerationSpecifier ){
			IASTEnumerationSpecifier enumSpec = (IASTEnumerationSpecifier)typeSpec;
			signature += enumSpec.getName();
		}
		
		signature += " ";
		
		if( param.isConst() ) signature += "const ";
		if( param.isVolatile() ) signature += "volatile ";

		Iterator ptrs = param.getPointerOperators();
		while( ptrs.hasNext() ){
			ASTPointerOperator ptrOp = (ASTPointerOperator) ptrs.next();
			if( ptrOp == ASTPointerOperator.POINTER ){
				signature += " * ";
			} else if( ptrOp == ASTPointerOperator.REFERENCE ){
				signature += " & ";
			} else if( ptrOp == ASTPointerOperator.CONST_POINTER ){
				signature += " const * ";
			} else if( ptrOp == ASTPointerOperator.VOLATILE_POINTER ){
				signature += " volatile * ";
			}
		}

		Iterator arrayModifiers = param.getArrayModifiers();
		while( arrayModifiers.hasNext() ){
			arrayModifiers.next();
			signature += " [] ";
		}

		return signature.toCharArray();
	}
	
	static private LinkedList scanForNames( IScanner scanner, IToken unusedToken ){
		LinkedList list = new LinkedList();
		
		String name  = new String("");
		
		try {
			IToken token = ( unusedToken != null ) ? unusedToken : scanner.nextToken();
			IToken prev = null;
			
			scanner.setThrowExceptionOnBadCharacterRead( true );
			
			boolean encounteredWild = false;
			boolean lastTokenWasOperator = false;
			
			while( true ){
				switch( token.getType() ){
					case IToken.tCOLONCOLON :
						list.addLast( name.toCharArray() );
						name = new String("");
						lastTokenWasOperator = false;
						break;
					
					case IToken.t_operator :
						name += token.getImage() + " ";
						lastTokenWasOperator = true;
						break;
					
					default:
						if( token.getType() == IToken.tSTAR || 
						    token.getType() == IToken.tQUESTION 
						    ){
							encounteredWild = true;
						} else if( !encounteredWild && !lastTokenWasOperator && name.length() > 0 &&
									prev.getType() != IToken.tIDENTIFIER &&
									prev.getType() != IToken.tLT &&
									prev.getType() != IToken.tCOMPL &&
									prev.getType() != IToken.tLBRACKET && 
									token.getType() != IToken.tRBRACKET &&
									token.getType()!= IToken.tGT
								 ){
							name += " ";
						} else {
							encounteredWild = false;
						}
						
						name += token.getImage();

						lastTokenWasOperator = false;
						break;
				}
				prev = token;
				
				token = null;
				while( token == null ){
					try{
						token = scanner.nextToken();
					} catch ( ScannerException e ){
						if( e.getProblem().getID() == IProblem.SCANNER_BAD_CHARACTER ){
							//TODO : This may not be \\, it could be another bad character
							if( !encounteredWild && !lastTokenWasOperator ) name += " ";
							name += "\\";
							encounteredWild = true;
							lastTokenWasOperator = false;
							prev = null;
						}
					}
				}
			}
		} catch (EndOfFileException e) {	
			list.addLast( name.toCharArray() );
		} catch (ScannerException e) {
		}
		
		return list;	
	}
	
	protected boolean matchesName( char[] pattern, char[] name ){
		if( pattern == null ){
			return true;  //treat null as "*"
		}
		
		if( name != null ){
			switch( _matchMode ){
				case EXACT_MATCH:
					return CharOperation.equals( pattern, name, _caseSensitive );
				case PREFIX_MATCH:
					return CharOperation.prefixEquals( pattern, name, _caseSensitive );
				case PATTERN_MATCH:
					if( !_caseSensitive ){
						pattern = CharOperation.toLowerCase( pattern );
					}
					
					return CharOperation.match( pattern, name, _caseSensitive );
			}
		}
		return false;
	}
	
	protected boolean matchQualifications( char[][] qualifications, char[][] candidate ){
		
		int qualLength = qualifications != null ? qualifications.length : 0;
		int candidateLength = candidate != null ? candidate.length : 0;
		
		if( qualLength == 0 ){
			return true;
		}
		
		int root = ( qualifications[0].length == 0 ) ? 1 : 0;
		
		if( (root == 1 && candidateLength != qualLength - 1 ) ||
			(root == 0 && candidateLength < qualLength ) )
		{
			return false;
		}
		
		for( int i = 1; i <= qualLength - root; i++ ){
			if( !matchesName( qualifications[ qualLength - i ], candidate[ candidateLength - i ] ) ){
				return false;		
			}
		}
		
		return true;
	}

    /**
	* Query a given index for matching entries. 
	*/
   public void findIndexMatches(IIndex index, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, ICSearchScope scope) throws IOException {

	   if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	   IndexInput input = new BlocksIndexInput(index.getIndexFile());
	   try {
		   input.open();
		   findIndexMatches(input, requestor, detailLevel, progressMonitor,scope);
	   } finally {
		   input.close();
	   }
   }
   /**
	* Query a given index for matching entries. 
	*/
   public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, ICSearchScope scope) throws IOException {

	   if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();
	
	   /* narrow down a set of entries using prefix criteria */
		char [] prefix = indexEntryPrefix();
		if( prefix == null ) return;
		
	   IEntryResult[] entries = input.queryEntriesPrefixedBy( prefix );
	   if (entries == null) return;
	
	   /* only select entries which actually match the entire search pattern */
	   for (int i = 0, max = entries.length; i < max; i++){

		   if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

		   /* retrieve and decode entry */	
		   IEntryResult entry = entries[i];
		   resetIndexInfo();
		   decodeIndexEntry(entry);
		   if (matchIndexEntry()){
			   feedIndexRequestor(requestor, detailLevel, entry.getFileReferences(), input, scope);
		   }
	   }
   }

   /**
   * Feed the requestor according to the current search pattern
   */
   public abstract void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, ICSearchScope scope)  throws IOException ;
   
   /**
    * Called to reset any variables used in the decoding of index entries, 
    * this ensures that the matchIndexEntry is not polluted by index info
    * from previous entries.
    */
   protected abstract void resetIndexInfo();
   
   /**
   * Decodes the index entry
   */
   protected abstract void decodeIndexEntry(IEntryResult entryResult);
   /**
	* Answers the suitable prefix that should be used in order
	* to query indexes for the corresponding item.
	* The more accurate the prefix and the less false hits will have
	* to be eliminated later on.
	*/
   public abstract char[] indexEntryPrefix();
   /**
	* Checks whether an entry matches the current search pattern
	*/
   protected abstract boolean matchIndexEntry();
   
	protected int 		_matchMode;
	protected boolean 	_caseSensitive;
	protected LimitTo   _limitTo;
}
