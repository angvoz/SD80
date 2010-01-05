/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Ed Swartz (Nokia)
 *    Mike Kucera (IBM) - bug #206952
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationListOwner;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.parser.AbstractParserLogService;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IInactiveCodeToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;

/**
 * Base class for the c- and c++ parser.
 */
public abstract class AbstractGNUSourceCodeParser implements ISourceCodeParser {
	protected static class FoundAggregateInitializer extends Exception {
		public final IASTDeclarator fDeclarator;
		public IASTDeclSpecifier fDeclSpec;
		public FoundAggregateInitializer(IASTDeclarator d) {
			fDeclarator= d;
		}
	}
    protected static class FoundDeclaratorException extends Exception {
    	private static final long serialVersionUID = 0;
    	
        public IASTDeclSpecifier declSpec;
        public IASTDeclarator declarator;

		public IASTDeclSpecifier altSpec;
		public IASTDeclarator altDeclarator;
        
        public IToken currToken;

        public FoundDeclaratorException(IASTDeclarator d, IToken t) {
            this.declarator = d;
            this.currToken =t;
        }
    }
    
	private static final ASTVisitor MARK_INACTIVE = new ASTGenericVisitor(true) {
		{
			shouldVisitAmbiguousNodes= true;
		}
		@Override
		protected int genericVisit(IASTNode node) {
			((ASTNode) node).setInactive();
			return PROCESS_CONTINUE;
		}
		
		@Override
		public int visit(ASTAmbiguousNode node) {
			node.setInactive();
			IASTNode[] alternatives= node.getNodes();
			for (IASTNode alt : alternatives) {
				if (!alt.accept(this))
					return PROCESS_ABORT;
			}
			return PROCESS_CONTINUE;
		}
	};

	protected static final int DEFAULT_DESIGNATOR_LIST_SIZE = 4;
    protected static int parseCount = 0;

	protected final AbstractParserLogService log;
    protected final IScanner scanner;
    protected final ParserMode mode;

    protected final boolean supportStatementsInExpressions;
    protected final boolean supportTypeOfUnaries;
    protected final boolean supportAlignOfUnaries;
    protected final boolean supportKnRC;
    protected final boolean supportAttributeSpecifiers;
    protected final boolean supportDeclspecSpecifiers;
    protected boolean supportParameterInfoBlock;
    protected boolean supportFunctionStyleAsm;
    protected boolean supportExtendedSizeofOperator; 
    protected final IBuiltinBindingsProvider builtinBindingsProvider;
    
    protected boolean functionCallCanBeLValue= false;
	protected boolean skipTrivialExpressionsInAggregateInitializers= false; 

    
    /**
     *  Marks the beginning of the current declaration. It is important to clear the mark whenever we
     *  enter a nested declaration, in order to avoid holding on to all the tokens.
     */
    protected IToken declarationMark;
    protected IToken nextToken;
    protected IToken lastTokenFromScanner;
    protected boolean onTopInTemplateArgs= false;
    protected boolean inBinaryExpression= true;

    protected boolean isCancelled = false;
	protected boolean parsePassed = true;
    protected int backtrackCount = 0;
    protected BacktrackException backtrack = new BacktrackException();
    
    protected ASTCompletionNode completionNode;
    protected IASTTypeId fTypeIdForCastAmbiguity;
    
    private final INodeFactory nodeFactory;
	private boolean fActiveCode= true;
	
    protected AbstractGNUSourceCodeParser(IScanner scanner,
            IParserLogService logService, ParserMode parserMode,
            INodeFactory nodeFactory,
            boolean supportStatementsInExpressions,
            boolean supportTypeOfUnaries, boolean supportAlignOfUnaries,
            boolean supportKnRC, boolean supportAttributeSpecifiers,
    		boolean supportDeclspecSpecifiers, 
    		IBuiltinBindingsProvider builtinBindingsProvider) {
        this.scanner = scanner;
        this.log = wrapLogService(logService);
        this.mode = parserMode;
        this.supportStatementsInExpressions = supportStatementsInExpressions;
        this.supportTypeOfUnaries = supportTypeOfUnaries;
        this.supportAlignOfUnaries = supportAlignOfUnaries;
        this.supportKnRC = supportKnRC;
        this.supportAttributeSpecifiers = supportAttributeSpecifiers;
        this.supportDeclspecSpecifiers = supportDeclspecSpecifiers;
        this.builtinBindingsProvider= builtinBindingsProvider;
        this.nodeFactory = nodeFactory;
    }
    
	/**
	 * Instructs the parser not to create ast nodes for expressions within aggregate initializers
	 * when they do not contain names.
	 */
	public void setSkipTrivialExpressionsInAggregateInitializers(boolean val) {
		skipTrivialExpressionsInAggregateInitializers= val;
	}

    private AbstractParserLogService wrapLogService(IParserLogService logService) {
		if (logService instanceof AbstractParserLogService) {
			return (AbstractParserLogService) logService;
		}
		return new ParserLogServiceWrapper(logService);
	}

    protected final void throwBacktrack(int offset, int length) throws BacktrackException {
        ++backtrackCount;
        backtrack.initialize(offset, (length < 0) ? 0 : length);
        throw backtrack;
    }

    public IASTCompletionNode getCompletionNode() {
        return completionNode;
    }

    // Use to create the completion node
    protected ASTCompletionNode createCompletionNode(IToken token) {
    	// the preprocessor may deliver tokens for literals or header-names.
        if(completionNode == null && token != null && token.getType() == IToken.tCOMPLETION) {
        	completionNode = new ASTCompletionNode(token, getTranslationUnit());
        }
        return completionNode;
    }

    /**
     * Fetches the next token from the scanner.
     */
    private final IToken fetchToken(boolean skipInactive) throws EndOfFileException {
        try {
        	IToken t= scanner.nextToken();
        	if (skipInactive) {
        		while (t.getType() == IToken.tINACTIVE_CODE_START) {
        			scanner.skipInactiveCode();
        			t= scanner.nextToken();
        		}
        	}
        	if (lastTokenFromScanner != null)
        		lastTokenFromScanner.setNext(t);
        	lastTokenFromScanner= t;
        	return t;
        } catch (OffsetLimitReachedException olre) {
        	if (mode != ParserMode.COMPLETION_PARSE)
			    throw new EndOfFileException();
			createCompletionNode(olre.getFinalToken());
			throw olre;
        }
    }
    
    private final IToken nextToken(boolean skipInactive) throws EndOfFileException {
    	IToken t= nextToken;
    	if (t == null) {
    		t= fetchToken(skipInactive);
    	}
    	nextToken= t;
    	return t;
    }

    private final IToken lookaheadToken(int i, boolean skipInactive) throws EndOfFileException {
    	assert i >= 0;
        if (isCancelled) {
            throw new ParseError(ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);
        }
        IToken t= nextToken(skipInactive);
        for (; i > 1; --i) {
            t = t.getNext();
            if (t == null) 
                t = fetchToken(skipInactive);
        }
        return t;
    }
    
    /**
     * Returns the next token without advancing. Same as {@code LA(1)}.
     */
    protected final IToken LA() throws EndOfFileException {
    	IToken t= nextToken(true);
    	checkForEOI(t);
    	return t;
    }

    /**
     * Returns one of the next tokens. With {@code i == 1}, the next token is returned.
     * @param i number of tokens to look ahead, must be greater than 0.
     */
    protected final IToken LA(int i) throws EndOfFileException {
        IToken t= lookaheadToken(i, true);
        checkForEOI(t);
        return t;
    }

    /**
     * Consumes and returns the next token available.
     */
    protected final IToken consume() throws EndOfFileException {
        IToken t= nextToken(true);
		checkForEOI(t);
		
        nextToken= t.getNext();
        return t;
    }

	/**
	 * Tests whether we are looking at a change from active to inactive code at this point. If so, the change
	 * is accepted.
	 * 
	 * @param nesting
	 *            the nesting level of the code branch we have to stay within
	 * @return <code>false</code> if an inactive code branch was rejected because of its nesting level,
	 *         <code>true</code>, otherwise.
	 */
    protected final boolean acceptInactiveCodeBoundary(int nesting) {
        try {
        	while (true) {
        		IToken t= nextToken(false);
        		switch (t.getType()) {
        		case IToken.tINACTIVE_CODE_START:
        		case IToken.tINACTIVE_CODE_SEPARATOR:
        			IInactiveCodeToken it = (IInactiveCodeToken) t;
					if (it.getNewNesting() < nesting || (it.getNewNesting() == nesting && it.getOldNesting() == nesting)) {
        				return false;
        			}        			
        			fActiveCode= false;
        			nextToken= t.getNext(); // consume the token
        			continue;
        		case IToken.tINACTIVE_CODE_END:
        			it = (IInactiveCodeToken) t;
					if (it.getNewNesting() < nesting || (it.getNewNesting() == nesting && it.getOldNesting() == nesting)) {
        				return false;
        			}        			
        			fActiveCode= true;
        			nextToken= t.getNext(); // consume the token
        			continue;
        		default:
        			return true;
        		}
        	}
        } catch (EndOfFileException e) {
        }
        return true;
    }
    
    protected final void skipInactiveCode() throws OffsetLimitReachedException {
    	IToken t= nextToken;
    	if (fActiveCode && (t == null || t.getType() != IToken.tINACTIVE_CODE_START))
    		return;
    	try {
        	fActiveCode= true;
        	while (t != null && t.getType() != IToken.tINACTIVE_CODE_END)
        		t= t.getNext();
        	
        	if (t != null) {
        		nextToken= t.getNext();
        	} else {
        		nextToken= null;
        		scanner.skipInactiveCode();
        	}
		} catch (OffsetLimitReachedException olre) {
			if (mode == ParserMode.COMPLETION_PARSE) {
				createCompletionNode(olre.getFinalToken());
				throw olre;
			}
		} 
    }
    
    protected final boolean isActiveCode() {
    	return fActiveCode;
    }
    
    protected final int getCodeBranchNesting() {
    	return scanner.getCodeBranchNesting();
    }

    /**
     * Returns the next token, which can be used to reset the input back to
     * this point in the stream.
     */
    protected final IToken mark() throws EndOfFileException {
    	return LA();
    }

    /**
     * Roll back to a previous point, reseting the queue of tokens.
     * @param mark a token previously obtained via {@link #mark()}.
     */
    protected final void backup(IToken mark) {
        nextToken = mark;
    }

	private final void checkForEOI(IToken t) throws EndOfFileException {
		final int lt= t.getType();
    	if (lt == IToken.tINACTIVE_CODE_SEPARATOR || lt == IToken.tINACTIVE_CODE_END)
    		throw new EndOfFileException(true);
	}

    /**
     * Same as {@link #LA(int)}, but returns <code>null</code> when eof is reached.
     */
    protected final IToken LAcatchEOF(int i) {
    	try {
    		return LA(i);
    	} catch (EndOfFileException e) {
    		return null;
    	}
    }
    
    /**
     * Look ahead in the token list and return the token type.
     * @param i number of tokens to look ahead, must be greater or equal to 0.
     * @return The type of that token
     */
    protected final int LT(int i) throws EndOfFileException {
        return LA(i).getType();
    }
    
    /**
     * Same as {@link #LT(int)}, but returns <code>0</code> when eof is reached.
     */
    protected final int LTcatchEOF(int i) {
    	try {
    		return LT(i);
    	} catch (EndOfFileException e) {
    		return 0;
    	}
    }
    
    /**
     * If the type of the next token matches, it is consumed and returned. Otherwise a
     * {@link BacktrackException} will be thrown. 
     * @param type the expected type of the next token.
     */
    protected final IToken consume(int type) throws EndOfFileException, BacktrackException {
    	final IToken result= consume();
        if (result.getType() != type)
            throwBacktrack(result);
        return result;
    }

	/**
	 * Consume the next token available only if the type is as specified. In case we reached the end of
	 * completion, no token is consumed and the eoc-token returned.
	 * 
	 * @param type
	 *            The type of token that you are expecting.
	 * @return the token that was consumed and removed from our buffer.
	 * @throws BacktrackException
	 *             if LT(1) != type
	 */
    protected IToken consumeOrEOC(int type) throws EndOfFileException, BacktrackException {
    	final IToken la1= LA(1);
        final int lt1 = la1.getType();
		if (lt1 != type) {
        	if (lt1 == IToken.tEOC)
        		return la1;
            throwBacktrack(la1);
        }        	
        return consume();
    }

	protected final boolean isOnSameLine(int offset1, int offset2) {
		ILocationResolver lr= (ILocationResolver) getTranslationUnit().getAdapter(ILocationResolver.class);
		IASTFileLocation floc= lr.getMappedFileLocation(offset1, offset2-offset1+1);
		return floc.getFileName().equals(lr.getContainingFilePath(offset1)) &&
			floc.getStartingLineNumber() == floc.getEndingLineNumber();
	}

    protected final int calculateEndOffset(IASTNode n) {
        ASTNode node = (ASTNode) n;
        return node.getOffset() + node.getLength();
    }

    protected final void setRange(IASTNode n, IASTNode from) {
    	((ASTNode) n).setOffsetAndLength((ASTNode) from);
    }

    protected final void setRange(IASTNode n, int offset, int endOffset) {
    	((ASTNode) n).setOffsetAndLength(offset, endOffset-offset);
    }
    
    protected final void adjustLength(IASTNode n, IASTNode endNode) {
        final int endOffset= calculateEndOffset(endNode);
        adjustEndOffset(n, endOffset);
    }

	protected final void adjustEndOffset(IASTNode n, final int endOffset) {
		final ASTNode node = (ASTNode) n;
        node.setLength(endOffset-node.getOffset());
	}

    protected final int getEndOffset() {
    	if (lastTokenFromScanner == null)
    		return 0;
    	return lastTokenFromScanner.getEndOffset();
    }

    /**
     * This is the single entry point for setting parsePassed to false
     */
    protected void failParse() {
        parsePassed = false;
    }

    public synchronized void cancel() {
        isCancelled = true;
    }

    /**
     * Parse an identifier.
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected abstract IASTName identifier() throws EndOfFileException, BacktrackException;

    /**
     * @return Returns the backtrackCount.
     */
    public final int getBacktrackCount() {
        return backtrackCount;
    }

    protected IASTProblem createProblem(BacktrackException bt) {
        IASTProblem result= bt.getProblem();
        if (result == null) {
            result= createProblem(IProblem.SYNTAX_ERROR, bt.getOffset(), bt.getLength());
        }
        return result;
    }


	protected final IASTProblem createProblem(int signal, int offset, int length) {
        IASTProblem result = nodeFactory.newProblem(signal, CharArrayUtils.EMPTY, true);
        ((ASTNode) result).setOffsetAndLength(offset, length);
        return result;
    }
    

    protected void logThrowable(String methodName, Throwable e) {
        if (e != null) {
        	if (log.isTracing()) {
        		String message = 
        			String.format("Parser: Unexpected throwable in %s:%s::%s. w/%s", //$NON-NLS-1$
        				          methodName, e.getClass().getName(), e.getMessage(), scanner);
        		log.traceLog(message);
        	}
        	log.traceException(e);
        }
    }

    
    
    @Override
	public String toString() {
        return scanner.toString(); 
    }

    /**
     * @param methodName
     * @param e
     */
    protected void logException(String methodName, Exception e) {
        if (!(e instanceof EndOfFileException) && e != null) {
        	if (log.isTracing()) {
        		String message = 
        			String.format("Parser: Unexpected exception in %s:%s::%s. w/%s", //$NON-NLS-1$
        				          methodName, e.getClass().getName(), e.getMessage(), scanner);
        		log.traceLog(message);
        	}
        	log.traceException(e);
        }
    }

    protected final void throwBacktrack(IASTProblem problem, IASTNode node) throws BacktrackException {
        ++backtrackCount;
        backtrack.initialize(problem, node);
        throw backtrack;
    }
    
    protected final void throwBacktrack(IASTProblem problem) throws BacktrackException {
        ++backtrackCount;
        backtrack.initialize(problem);
        throw backtrack;
    }

    public IASTTranslationUnit parse() {
        long startTime = System.currentTimeMillis();
        translationUnit();
        log.traceLog("Parse " //$NON-NLS-1$
                + (++parseCount) + ": " //$NON-NLS-1$
                + (System.currentTimeMillis() - startTime) + "ms" //$NON-NLS-1$
                + (parsePassed ? "" : " - parse failure")); //$NON-NLS-1$ //$NON-NLS-2$
        startTime = System.currentTimeMillis();
        resolveAmbiguities();
        log.traceLog("Ambiguity resolution : " //$NON-NLS-1$
                + (System.currentTimeMillis() - startTime) + "ms" //$NON-NLS-1$
        ); 
        IASTTranslationUnit result = getTranslationUnit();
        nullifyTranslationUnit();
        result.freeze(); // make the AST immutable
        return result;
    }

    protected void resolveAmbiguities() {
        final IASTTranslationUnit translationUnit = getTranslationUnit();
        if (translationUnit instanceof ASTTranslationUnit) {
        	((ASTTranslationUnit) translationUnit).resolveAmbiguities();
        }
    }

    protected abstract ASTVisitor createAmbiguityNodeVisitor();

    protected abstract void nullifyTranslationUnit();

	protected IToken skipOverCompoundStatement() throws BacktrackException, EndOfFileException {
        // speed up the parser by skipping the body, simply look for matching brace and return
        final boolean isActive = isActiveCode();
		final int codeBranchNesting= getCodeBranchNesting();
		
        consume(IToken.tLBRACE);
        IToken result = null;
        int depth = 1;
        while (depth > 0) {
            if (!isActive) {
                IToken t= lookaheadToken(1, false);
    			final int lt= t.getType();
				if (lt == IToken.tINACTIVE_CODE_SEPARATOR || lt == IToken.tINACTIVE_CODE_END || lt == IToken.tINACTIVE_CODE_START) {
					if (!acceptInactiveCodeBoundary(codeBranchNesting))
						throw new EndOfFileException(true);
				} 
            }
			result = consume();
            switch (result.getType()) {
            case IToken.tRBRACE:
                --depth;
                break;
            case IToken.tLBRACE:
                ++depth;
                break;
            case IToken.tEOC:
                throw new EndOfFileException();
            }
        }
        return result;
    }

    protected IASTProblemDeclaration skipProblemDeclaration(int offset) {
		failParse();
		declarationMark= null;
    	int endOffset = skipToSemiOrClosingBrace(offset, false);
		IASTProblem problem= createProblem(IProblem.SYNTAX_ERROR, offset, endOffset-offset);
		return buildProblemDeclaration(problem);
    }
    
    protected IASTProblemStatement skipProblemStatement(int offset) {
		failParse();
		declarationMark= null;
    	int endOffset = skipToSemiOrClosingBrace(offset, false);
		IASTProblem problem= createProblem(IProblem.SYNTAX_ERROR, offset, endOffset-offset);
		return buildProblemStatement(problem);
    }

    private IASTProblem skipProblemEnumerator(int offset) {
    	failParse();
    	final int endOffset= skipToSemiOrClosingBrace(offset, true);
    	return createProblem(IProblem.SYNTAX_ERROR, offset, endOffset-offset);
    }
    
	private int skipToSemiOrClosingBrace(int offset, boolean eatBrace) {
		failParse();
		declarationMark= null;
    	int depth= 0;
    	int endOffset;
    	loop: try {
    		endOffset= LA(1).getOffset();
			while(true) {
				switch (LT(1)) {
				case IToken.tEOC:
					endOffset= getEndOffset();
					break loop;
				case IToken.tSEMI:
					if (depth == 0) {
						endOffset= consume().getEndOffset();  
						break loop;
					}
					break;
				case IToken.tLBRACE:
					++depth;
					break;
				case IToken.tRBRACE:
					if (--depth <= 0) {
						if (depth == 0 || offset == endOffset || eatBrace) {
							endOffset= consume().getEndOffset(); // consume closing brace
						}
						if (LTcatchEOF(1) == IToken.tSEMI) {
							endOffset= consume().getEndOffset();
						}
						break loop;
					}
					break;
				}
				endOffset= consume().getEndOffset();
			}
		} catch (EndOfFileException e) {
			endOffset= getEndOffset();
		}
		return endOffset;
	}

    protected IASTProblemExpression skipProblemConditionInParenthesis(int offset) {
		failParse();
		int compExpr= 0;
		int depth= 0;
		int endOffset= offset;
		loop: try {
			while(true) {
				switch (LT(1)) {
				case IToken.tEOC:
					endOffset= getEndOffset();
					break loop;
				case IToken.tSEMI:
				case IToken.tLBRACE:
					if (compExpr == 0) {
						break loop;
					}
					break;
				case IToken.tLPAREN:
					depth++;
					if (LTcatchEOF(2) == IToken.tLBRACE) {
						if (compExpr == 0) {
							compExpr= depth;
						}
						consume();
					} 
					break;
				case IToken.tRPAREN:
					if (--depth < 0) {
						break loop;
					}
					if (depth < compExpr) {
						compExpr= 0;
					}
					break;
				}
				endOffset= consume().getEndOffset();
			}
		} catch (EndOfFileException e) {
			endOffset= getEndOffset();
		}
		IASTProblem problem= createProblem(IProblem.SYNTAX_ERROR, offset, endOffset-offset);
		return buildProblemExpression(problem);
    }

    /**
     * @return TODO
     * @throws BacktrackException
     */
    protected IASTCompoundStatement compoundStatement() throws EndOfFileException, BacktrackException {
        IASTCompoundStatement result = nodeFactory.newCompoundStatement();
        if (LT(1) == IToken.tEOC)
            return result;

        final int offset= LA(1).getOffset();
        int endOffset= consume(IToken.tLBRACE).getOffset();

        int stmtOffset= -1;
        while(true) {
        	IToken next= LAcatchEOF(1);
        	if (next == null) {
        		((ASTNode) result).setOffsetAndLength(offset, endOffset-offset);
        		throwBacktrack(createProblem(IProblem.SYNTAX_ERROR, endOffset, 0), result);
        		return null; // hint for java-compiler
        	}
            try {
            	if (next.getType() == IToken.tEOC)
            		break;
            	
            	if (next.getType() == IToken.tRBRACE) {
            		endOffset= consume().getEndOffset();
            		break;
            	}
            		    
            	final int nextOffset = next.getOffset();
        		declarationMark= next;
        		next= null; // don't hold on to the token while parsing namespaces, class bodies, etc.

        		IASTStatement stmt;
        		if (stmtOffset == nextOffset) {
        			// no progress
        			stmt= skipProblemStatement(stmtOffset);
            	} else {
            		stmtOffset= nextOffset;
            		stmt= statement();
            	}
        		result.addStatement(stmt);
        		endOffset= calculateEndOffset(stmt);
            } catch (BacktrackException bt) {
            	IASTStatement stmt= skipProblemStatement(stmtOffset);
        		result.addStatement(stmt);
        		endOffset= calculateEndOffset(stmt);
            } catch (EndOfFileException e) {
            	IASTStatement stmt= skipProblemStatement(stmtOffset);
        		result.addStatement(stmt);
        		endOffset= calculateEndOffset(stmt);
        		break;
            } finally {
            	declarationMark= null;
            }
        }
        ((ASTNode) result).setOffsetAndLength(offset, endOffset-offset);
        return result;
    }


	private IASTProblemDeclaration buildProblemDeclaration(IASTProblem problem) {
		IASTProblemDeclaration pd = nodeFactory.newProblemDeclaration(problem);
		((ASTNode) pd).setOffsetAndLength(((ASTNode) problem));
		return pd;
	}

	private IASTProblemStatement buildProblemStatement(IASTProblem problem) {
		IASTProblemStatement pstmt = nodeFactory.newProblemStatement(problem);
		((ASTNode) pstmt).setOffsetAndLength(((ASTNode) problem));
		return pstmt;
	}

	private IASTProblemExpression buildProblemExpression(IASTProblem problem) {
		IASTProblemExpression pexpr = nodeFactory.newProblemExpression(problem);
		((ASTNode) pexpr).setOffsetAndLength(((ASTNode) problem));
		return pexpr;
	}

    protected IASTExpression compoundStatementExpression() throws EndOfFileException, BacktrackException {
        int startingOffset = consume().getOffset(); // tLPAREN always
        IASTCompoundStatement compoundStatement = null;
        if (mode == ParserMode.QUICK_PARSE || mode == ParserMode.STRUCTURAL_PARSE || !isActiveCode())
            skipOverCompoundStatement();
        else if (mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE) {
            if (scanner.isOnTopContext())
                compoundStatement();
            else
                skipOverCompoundStatement();
        } else if (mode == ParserMode.COMPLETE_PARSE)
            compoundStatement = compoundStatement();

        int lastOffset = consume(IToken.tRPAREN).getEndOffset();
        IGNUASTCompoundStatementExpression resultExpression = nodeFactory.newGNUCompoundStatementExpression(compoundStatement);
        ((ASTNode) resultExpression).setOffsetAndLength(startingOffset, lastOffset - startingOffset);
        return resultExpression;
    }


    protected IASTExpression possiblyEmptyExpressionList(int endToken) throws BacktrackException, EndOfFileException {
    	IToken la1= LA(1);
    	if (la1.getType() == endToken) {
            IASTExpressionList expressionList = nodeFactory.newExpressionList();
            ((ASTNode) expressionList).setOffsetAndLength(la1.getOffset(), 0);
            return expressionList;
    	}
    	return expression();
    }
    
    protected IASTExpression expression() throws BacktrackException, EndOfFileException {
    	IToken la = LA(1);
    	int startingOffset = la.getOffset();
    	IASTExpression assignmentExpression = assignmentExpression();
    	if (LT(1) != IToken.tCOMMA)
    		return assignmentExpression;

    	IASTExpressionList expressionList = nodeFactory.newExpressionList();
    	((ASTNode) expressionList).setOffset(startingOffset);
    	expressionList.addExpression(assignmentExpression);

    	int lastOffset = 0;
    	while (LT(1) == IToken.tCOMMA) {
    		consume();
    		IASTExpression secondExpression = assignmentExpression();
    		expressionList.addExpression(secondExpression);
    		lastOffset = calculateEndOffset(secondExpression);
    	}
    	((ASTNode) expressionList).setLength(lastOffset - startingOffset);
    	return expressionList;
    }


    protected abstract IASTExpression assignmentExpression()
            throws BacktrackException, EndOfFileException;

	protected IASTExpression relationalExpression() throws BacktrackException, EndOfFileException {
        IASTExpression result= shiftExpression();
        for (;;) {
        	int operator;
            switch (LT(1)) {
            case IToken.tGT:
            	if (onTopInTemplateArgs)
            		return result;
            	operator= IASTBinaryExpression.op_greaterThan;
            	break;
            case IToken.tLT:
                operator = IASTBinaryExpression.op_lessThan;
                break;
            case IToken.tLTEQUAL:
                operator = IASTBinaryExpression.op_lessEqual;
                break;
            case IToken.tGTEQUAL:
                operator = IASTBinaryExpression.op_greaterEqual;
                break;
            case IGCCToken.tMAX:
                operator = IASTBinaryExpression.op_max;
                break;
            case IGCCToken.tMIN:
                operator = IASTBinaryExpression.op_min;
                break;
            default:
            	return result;
            }
            consume();
            IASTExpression rhs= shiftExpression();
            result = buildBinaryExpression(operator, result, rhs, calculateEndOffset(rhs));
        }
    }

    protected abstract IASTTypeId typeId(DeclarationOptions option) throws EndOfFileException;

	protected IASTExpression castExpression() throws EndOfFileException, BacktrackException {
		if (LT(1) == IToken.tLPAREN) {
			final IToken mark= mark();
			final int startingOffset= mark.getOffset();
			consume();
			IASTTypeId typeId = typeId(DeclarationOptions.TYPEID);
			if (typeId != null && LT(1) == IToken.tRPAREN) {
				consume();
				boolean unaryFailed= false;
				if (inBinaryExpression) {
    				switch (LT(1)){
    				// ambiguity with unary operator
    				case IToken.tPLUS: case IToken.tMINUS: 
    				case IToken.tSTAR: case IToken.tAMPER:
    					IToken markEnd= mark();
    					backup(mark);
    					try {
    						IASTExpression unary= unaryExpression();
    						fTypeIdForCastAmbiguity= typeId;
    						return unary;
    					} catch (BacktrackException bt) {
    						backup(markEnd);
    						unaryFailed= true;
    					}
    				} 
				}
				try {
					boolean couldBeFunctionCall= LT(1) == IToken.tLPAREN;
					IASTExpression rhs= castExpression();
					IASTCastExpression result= buildCastExpression(IASTCastExpression.op_cast, typeId, rhs, startingOffset, calculateEndOffset(rhs));
					if (!unaryFailed && couldBeFunctionCall && rhs instanceof IASTCastExpression == false) {
						IToken markEnd= mark();
						final IASTTypeId typeidForCastAmbiguity= fTypeIdForCastAmbiguity;
						backup(mark);
						try {
							IASTExpression expr= primaryExpression();
							IASTFunctionCallExpression fcall = nodeFactory.newFunctionCallExpression(expr, null);
							IASTAmbiguousExpression ambiguity = createAmbiguousCastVsFunctionCallExpression(result, fcall);
							((ASTNode) ambiguity).setOffsetAndLength((ASTNode) result);
							return ambiguity;
						} catch (BacktrackException bt) {
						} finally {
							backup(markEnd);
							fTypeIdForCastAmbiguity= typeidForCastAmbiguity;
						}
					}
					return result;
				} catch (BacktrackException b) {
					if (unaryFailed)
						throw b;
				}
			}
			backup(mark);
		}
		return unaryExpression();
    }

    protected abstract IASTExpression unaryExpression() throws BacktrackException, EndOfFileException;
    protected abstract IASTExpression primaryExpression() throws BacktrackException, EndOfFileException;

    protected abstract IASTTranslationUnit getTranslationUnit();

	protected abstract void setupTranslationUnit() throws Exception;

	protected void translationUnit() {
        try {
            setupTranslationUnit();
        } catch (Exception e2) {
            logException("translationUnit::createCompilationUnit()", e2); //$NON-NLS-1$
            return;
        }
        parseTranslationUnit();
    }

	protected void parseTranslationUnit() {
		final IASTTranslationUnit tu= getTranslationUnit();
		declarationList(tu, DeclarationOptions.GLOBAL, false, 0);
        ((ASTNode) tu).setLength(getEndOffset());
	}

	protected final void declarationListInBraces(final IASTDeclarationListOwner tu, int offset, DeclarationOptions options) throws EndOfFileException, BacktrackException {
		// consume brace, if requested
		int codeBranchNesting= getCodeBranchNesting();
		consume(IToken.tLBRACE);
		declarationList(tu, options, true, codeBranchNesting);

		final int lt1 = LTcatchEOF(1);
		if (lt1 == IToken.tRBRACE) {
			int endOffset= consume().getEndOffset();
			setRange(tu, offset, endOffset);
			return;
		}
		
		final int endOffset = getEndOffset();
		setRange(tu, offset, endOffset);
		if (lt1 == IToken.tEOC || (lt1 == 0 && tu instanceof IASTCompositeTypeSpecifier)) {
			return;
		}
		throwBacktrack(createProblem(IProblem.SYNTAX_ERROR, endOffset, 0), tu);
	}
	
	private final void declarationList(final IASTDeclarationListOwner tu, DeclarationOptions options, boolean upToBrace, int codeBranchNesting) {
		final boolean wasActive= isActiveCode();
		while (true) {
			final boolean ok= acceptInactiveCodeBoundary(codeBranchNesting);
			if (!ok) {
				// we left to an enclosing code branch. If we started in inactive code, it's time to leave.
				if (!wasActive) 
					return;
				
				// if we started in active code, we need to skip the outer and therefore unrelated 
				// inactive branches until we hit active code again.
				try {
					skipInactiveCode();
				} catch (OffsetLimitReachedException e) {
					return;
				}
				codeBranchNesting= Math.min(getCodeBranchNesting()+1, codeBranchNesting);
				
				// we could be at the start of inactive code so restart the loop
				continue; 
			}
			
			final boolean active= isActiveCode();
			IToken next= LAcatchEOF(1);
			if (next == null || next.getType() == IToken.tEOC)
				return;

			if (upToBrace && next.getType() == IToken.tRBRACE && active == wasActive) {
				return;
			}
			
			final int offset = next.getOffset();
			declarationMark= next;
			next= null; // don't hold on to the token while parsing namespaces, class bodies, etc.
			try {
				IASTDeclaration declaration= declaration(options);
				if (((ASTNode) declaration).getLength() == 0 && LTcatchEOF(1) != IToken.tEOC) {
					declaration= skipProblemDeclaration(offset);
				}
				addDeclaration(tu, declaration, active);
			} catch (BacktrackException bt) {
				IASTDeclaration[] decls= problemDeclaration(offset, bt, options);
				for (IASTDeclaration declaration : decls) {
					addDeclaration(tu, declaration, active);
				}
			} catch (EndOfFileException e) {
				IASTDeclaration declaration= skipProblemDeclaration(offset);
				addDeclaration(tu, declaration, active);
				if (!e.endsInactiveCode()) {
					break;
				}
			} finally {
				declarationMark= null;
			}
		}
	}

	private void addDeclaration(final IASTDeclarationListOwner parent, IASTDeclaration declaration,
			final boolean active) {
		if (!active) {
			declaration.accept(MARK_INACTIVE);
		}
		parent.addDeclaration(declaration);
	}

    protected IASTExpression assignmentOperatorExpression(int kind,
            IASTExpression lhs) throws EndOfFileException, BacktrackException {
        consume();
        IASTExpression rhs = assignmentExpression();
        return buildBinaryExpression(kind, lhs, rhs, calculateEndOffset(rhs));
    }

    protected IASTExpression constantExpression() throws BacktrackException, EndOfFileException {
    	return conditionalExpression();
    }

    protected IASTExpression logicalOrExpression() throws BacktrackException, EndOfFileException {
        IASTExpression firstExpression = logicalAndExpression();
        while (LT(1) == IToken.tOR) {
        	if (shallRejectLogicalOperator()) {
        		throwBacktrack(LA(1));
        	}

            consume();
            IASTExpression secondExpression = logicalAndExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_logicalOr, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

	protected boolean shallRejectLogicalOperator() {
		return false;
	}

    protected IASTExpression logicalAndExpression() throws BacktrackException, EndOfFileException {
        IASTExpression firstExpression = inclusiveOrExpression();
        while (LT(1) == IToken.tAND) {
        	if (shallRejectLogicalOperator()) {
        		throwBacktrack(LA(1));
        	}

            consume();
            IASTExpression secondExpression = inclusiveOrExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_logicalAnd, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    protected IASTExpression inclusiveOrExpression() throws BacktrackException, EndOfFileException {
        IASTExpression firstExpression = exclusiveOrExpression();
        while (LT(1) == IToken.tBITOR) {
            consume();
            IASTExpression secondExpression = exclusiveOrExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_binaryOr, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    protected IASTExpression exclusiveOrExpression() throws BacktrackException, EndOfFileException {
        IASTExpression firstExpression = andExpression();
        while (LT(1) == IToken.tXOR) {
            consume();
            IASTExpression secondExpression = andExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_binaryXor, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    protected IASTExpression andExpression() throws EndOfFileException, BacktrackException {
        IASTExpression firstExpression = equalityExpression();
        while (LT(1) == IToken.tAMPER) {
            final int offset= consume().getOffset();
            final IASTTypeId typeid= fTypeIdForCastAmbiguity; fTypeIdForCastAmbiguity= null;
            IASTExpression secondExpression = equalityExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_binaryAnd, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
            if (typeid != null) {
            	firstExpression = createCastVsBinaryExpressionAmbiguity((IASTBinaryExpression) firstExpression, typeid, IASTUnaryExpression.op_amper, offset);
            }
        }
        return firstExpression;
    }

    protected IASTExpression equalityExpression() throws EndOfFileException, BacktrackException {
        IASTExpression firstExpression = relationalExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tEQUAL:
            case IToken.tNOTEQUAL:
                IToken t = consume();
                int operator = ((t.getType() == IToken.tEQUAL) ? IASTBinaryExpression.op_equals : IASTBinaryExpression.op_notequals);
                IASTExpression secondExpression = relationalExpression();
                firstExpression = buildBinaryExpression(operator,
                        firstExpression, secondExpression,
                        calculateEndOffset(secondExpression));
                break;
            default:
                return firstExpression;
            }
        }
    }

    protected IASTExpression buildBinaryExpression(int operator, IASTExpression expr1, IASTExpression expr2, int lastOffset) {
        IASTBinaryExpression result = nodeFactory.newBinaryExpression(operator, expr1, expr2);
        int o = ((ASTNode) expr1).getOffset();
        ((ASTNode) result).setOffsetAndLength(o, lastOffset - o);
        return result;
    }

    protected IASTExpression shiftExpression() throws BacktrackException, EndOfFileException {
        IASTExpression firstExpression = additiveExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tSHIFTL:
            case IToken.tSHIFTR:
                IToken t = consume();
                int operator = t.getType() == IToken.tSHIFTL ? IASTBinaryExpression.op_shiftLeft : IASTBinaryExpression.op_shiftRight;
                IASTExpression secondExpression = additiveExpression();
                firstExpression = buildBinaryExpression(operator,
                        firstExpression, secondExpression,
                        calculateEndOffset(secondExpression));
                break;
            default:
                return firstExpression;
            }
        }
    }

    protected IASTExpression additiveExpression() throws BacktrackException, EndOfFileException {
        IASTExpression result= multiplicativeExpression();
        for (;;) {
        	int operator;
        	int unaryOperator;
            switch (LT(1)) {
            case IToken.tPLUS:
            	operator= IASTBinaryExpression.op_plus;
            	unaryOperator= IASTUnaryExpression.op_plus;
            	break;
            case IToken.tMINUS:
            	operator= IASTBinaryExpression.op_minus;
            	unaryOperator= IASTUnaryExpression.op_minus;
            	break;
            default:
            	return result;
            }
            final int offset= consume().getOffset();
            final IASTTypeId typeid= fTypeIdForCastAmbiguity; fTypeIdForCastAmbiguity= null;
            IASTExpression secondExpression = multiplicativeExpression();
            result = buildBinaryExpression(operator, result, secondExpression, calculateEndOffset(secondExpression));
            if (typeid != null) {
            	result = createCastVsBinaryExpressionAmbiguity((IASTBinaryExpression) result, typeid, unaryOperator, offset);
            }
        }
    }
    
    protected IASTExpression multiplicativeExpression() throws BacktrackException, EndOfFileException {
    	inBinaryExpression= true;
    	fTypeIdForCastAmbiguity= null;
        IASTExpression result= pmExpression();
        for (;;) {
        	int operator;
            switch (LT(1)) {
            case IToken.tSTAR:
                operator = IASTBinaryExpression.op_multiply;
                break;
            case IToken.tDIV:
                operator = IASTBinaryExpression.op_divide;
                break;
            case IToken.tMOD:
                operator = IASTBinaryExpression.op_modulo;
                break;
            default:
            	return result;
            }
            final int offset= consume().getOffset();
            final IASTTypeId typeid= fTypeIdForCastAmbiguity; fTypeIdForCastAmbiguity= null;
            IASTExpression secondExpression= pmExpression();
            result= buildBinaryExpression(operator,	result, secondExpression, calculateEndOffset(secondExpression));
            if (typeid != null) {
            	result = createCastVsBinaryExpressionAmbiguity((IASTBinaryExpression) result, typeid, IASTUnaryExpression.op_star, offset);
            }
        }
    }
    
    protected abstract IASTExpression pmExpression() throws EndOfFileException, BacktrackException;


	private IASTExpression createCastVsBinaryExpressionAmbiguity(IASTBinaryExpression expr, final IASTTypeId typeid, int unaryOperator, int unaryOpOffset) {
		IASTUnaryExpression unary= nodeFactory.newUnaryExpression(unaryOperator, null);
		((ASTNode) unary).setOffset(unaryOpOffset);
		IASTCastExpression castExpr = buildCastExpression(IASTCastExpression.op_cast, typeid, unary, 0, 0);
		IASTExpression result= createAmbiguousBinaryVsCastExpression(expr, castExpr);
		((ASTNode) result).setOffsetAndLength((ASTNode) expr);
		return result;
	}

    protected IASTExpression conditionalExpression() throws BacktrackException, EndOfFileException {
        IASTExpression firstExpression = logicalOrExpression();
        if (LT(1) == IToken.tQUESTION) {
        	if (shallRejectLogicalOperator()) {
        		throwBacktrack(LA(1));
        	}
        		
            consume();
            IASTExpression secondExpression= null;
            if (LT(1) != IToken.tCOLON) {
            	secondExpression = expression();
            }
            IASTExpression thirdExpression = null;
            
            if (LT(1) != IToken.tEOC) {
                consume(IToken.tCOLON);
                thirdExpression = assignmentExpression();
            }
            
            IASTConditionalExpression result = nodeFactory.newConditionalExpession(firstExpression, secondExpression, thirdExpression);
            if (thirdExpression != null) {
                ((ASTNode) result).setOffsetAndLength(((ASTNode) firstExpression)
                        .getOffset(), calculateEndOffset(thirdExpression)
                        - ((ASTNode) firstExpression).getOffset());
            }
            
            return result;
        }
        return firstExpression;
    }

    protected IASTExpression unarayExpression(int operator) throws EndOfFileException, BacktrackException {
    	final IToken operatorToken= consume();
        final IASTExpression operand= castExpression();
        
        if (operator == IASTUnaryExpression.op_star && operand instanceof IASTLiteralExpression) {
        	IASTLiteralExpression lit= (IASTLiteralExpression) operand;
        	switch(lit.getKind()) {
        	case IASTLiteralExpression.lk_char_constant:
        	case IASTLiteralExpression.lk_float_constant:
        	case IASTLiteralExpression.lk_integer_constant:
        	case IASTLiteralExpression.lk_true:
        	case IASTLiteralExpression.lk_false:
				throwBacktrack(operatorToken);
        	}
        }
        
        return buildUnaryExpression(operator, operand, operatorToken.getOffset(), calculateEndOffset(operand));
    }

    protected IASTExpression buildUnaryExpression(int operator, IASTExpression operand, int offset, int lastOffset) {
        IASTUnaryExpression result = nodeFactory.newUnaryExpression(operator, operand); 
        setRange(result, offset, lastOffset);
        return result;
    }


    protected IASTStatement handleFunctionBody() throws BacktrackException, EndOfFileException {
    	declarationMark= null; 
        if (mode == ParserMode.QUICK_PARSE || mode == ParserMode.STRUCTURAL_PARSE || !isActiveCode()) {
            int offset = LA(1).getOffset();
            IToken last = skipOverCompoundStatement();
            IASTCompoundStatement cs = nodeFactory.newCompoundStatement();
            setRange(cs, offset, last.getEndOffset());
            return cs;
        } else if (mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE) {
            if (scanner.isOnTopContext())
                return functionBody();
            int offset = LA(1).getOffset();
            IToken last = skipOverCompoundStatement();
            IASTCompoundStatement cs = nodeFactory.newCompoundStatement();
            setRange(cs, offset, last.getEndOffset());
            return cs;
        } 

        // full parse
        return functionBody();
    }

    /**
     * Parses a function body.
     * 
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTStatement functionBody() throws EndOfFileException, BacktrackException {
        return compoundStatement();
    }

    protected abstract IASTDeclarator initDeclarator(DeclarationOptions option) 
    		throws EndOfFileException, BacktrackException, FoundAggregateInitializer;

    /**
     * @param option the options with which to parse the declaration
     * @throws FoundDeclaratorException encountered EOF while looking ahead
     * @throws FoundAggregateInitializer found aggregate initializer, needs special treatment
     *   because of scalability.
     */
    protected void lookAheadForDeclarator(final DeclarationOptions option) 
    		throws FoundDeclaratorException, FoundAggregateInitializer {
        IToken mark = null;
        try {
            mark = mark();
            final IASTDeclarator dtor= initDeclarator(option);
            final IToken la = LA(1);
            backup(mark);
            if (la == null || la == mark)
            	return;

            if (verifyLookaheadDeclarator(option, dtor, la))
            	throw new FoundDeclaratorException(dtor, la);
        } catch (BacktrackException bte) {
        	if (mark != null)
        		backup(mark);
        } catch (EndOfFileException e) {
        	if (mark != null)
        		backup(mark);
        } 
    }

	protected abstract boolean verifyLookaheadDeclarator(DeclarationOptions option, IASTDeclarator d, IToken nextToken);
    
    /**
     * Parse an enumeration specifier, as according to the ANSI specs in C &
     * C++. enumSpecifier: "enum" (name)? "{" (enumerator-list) "}"
     * enumerator-list: enumerator-definition enumerator-list ,
     * enumerator-definition enumerator-definition: enumerator enumerator =
     * constant-expression enumerator: identifier
     * 
     * @throws BacktrackException request a backtrack
     */
    protected IASTEnumerationSpecifier enumSpecifier() throws BacktrackException, EndOfFileException {
        final IToken mark= mark();
        final int offset= consume().getOffset();

        // if __attribute__ or __declspec occurs after struct/union/class and before the identifier        
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);

        IASTName name;
        if (LT(1) == IToken.tIDENTIFIER) {
            name= identifier();
        } else {
            name= nodeFactory.newName();
        }
        
        if (LT(1) != IToken.tLBRACE) {
        	backup(mark);
        	throwBacktrack(mark);
        }

        final IASTEnumerationSpecifier result= nodeFactory.newEnumerationSpecifier(name);

        boolean needComma= false;
        int endOffset= consume().getEndOffset(); // IToken.tLBRACE
        int problemOffset= endOffset;
        try {
        	loop: while (true) {
        		switch (LTcatchEOF(1)) {
        		case 0: // eof
        			endOffset= getEndOffset();
        			break loop;
        		case IToken.tRBRACE:
        			endOffset= consume().getEndOffset();
        			break loop;
        		case IToken.tEOC:
        			break loop;
        		case IToken.tCOMMA:
        			if (!needComma) {
        				problemOffset= LA(1).getOffset();
        				throw backtrack;
        			}
        			endOffset= consume().getEndOffset();
        			needComma= false;
        			continue loop;
        		case IToken.tIDENTIFIER:
        		case IToken.tCOMPLETION:
        			problemOffset= LA(1).getOffset();
        			if (needComma)
        				throw backtrack;
        			
        			final IASTName etorName= identifier();
        			final IASTEnumerator enumerator= nodeFactory.newEnumerator(etorName, null);
        			endOffset= calculateEndOffset(etorName);
        			setRange(enumerator, problemOffset, endOffset);
        			result.addEnumerator(enumerator);
        			if (LTcatchEOF(1) == IToken.tASSIGN) {
        				problemOffset= consume().getOffset();
        				final IASTExpression value= constantExpression();
        				enumerator.setValue(value);
        				adjustLength(enumerator, value);
        				endOffset= calculateEndOffset(value);
        			} 
        			needComma= true;
        			continue loop;
        		default:
        			problemOffset= LA(1).getOffset();
                	throw backtrack;
        		}
        	}
        } catch (EndOfFileException eof) {
        	throwBacktrack(createProblem(IProblem.SYNTAX_ERROR, problemOffset, getEndOffset()-problemOffset), result);
        } catch (BacktrackException bt) {
        	IASTProblem problem= skipProblemEnumerator(problemOffset);
        	throwBacktrack(problem, result);
        }
        setRange(result, offset, endOffset);
        return result;
    }

    protected abstract IASTStatement statement() throws EndOfFileException, BacktrackException;

    protected IASTExpression condition(boolean followedByParenthesis) throws BacktrackException, EndOfFileException {
    	IToken mark= mark();
    	try {
    		IASTExpression expr= expression();
    		if (!followedByParenthesis)
    			return expr;
    		
    		switch (LT(1)) {
    		case IToken.tEOC:
    		case IToken.tRPAREN:
    			return expr;
    		}
    	} catch (BacktrackException b) {
    		if (!followedByParenthesis)
    			throw b;
    	}
		backup(mark);
		return skipProblemConditionInParenthesis(mark.getOffset());
    }

    public boolean encounteredError() {
        return !parsePassed;
    }


    protected abstract IASTDeclaration declaration(DeclarationOptions option) throws BacktrackException, EndOfFileException;
    protected abstract IASTDeclSpecifier declSpecifierSeq(DeclarationOptions option) throws BacktrackException, EndOfFileException, FoundDeclaratorException, FoundAggregateInitializer;

    protected IASTDeclaration[] problemDeclaration(int offset, BacktrackException bt, DeclarationOptions option) {
    	failParse();
    	IASTProblem origProblem= createProblem(bt);
    	
    	// a node was detected by assuming additional tokens (e.g. missing semicolon)
    	IASTNode n= bt.getNodeBeforeProblem();
    	if (n instanceof IASTDeclaration) {
    		IToken la1= LAcatchEOF(1);
    		if (la1 == null || la1.getOffset() > offset) {
    			declarationMark= null;
    			return new IASTDeclaration[] {(IASTDeclaration) n, buildProblemDeclaration(origProblem)};
    		}
    	} 
    	
    	if (declarationMark != null && isActiveCode()) {
    		IASTDeclaration trailingProblem= null;
    		offset= declarationMark.getOffset();
    		
    		// try to skip identifiers (undefined macros?)
    		IASTDeclaration decl= null;
    		int endOffset= 0;
    		loop: while (declarationMark != null && declarationMark.getType() == IToken.tIDENTIFIER) {
    			endOffset= declarationMark.getEndOffset();
    			declarationMark= declarationMark.getNext();
    			if (declarationMark != null) {
    				backup(declarationMark);
    				// avoid creating an empty declaration
    				switch(LTcatchEOF(1)) {
    				case 0: // eof
    				case IToken.tEOC:
    				case IToken.tSEMI:
    				 	break loop;
    				}
    				try {
    					decl= declaration(option);
    					break;
    				} catch (BacktrackException bt2) {
    			    	n= bt2.getNodeBeforeProblem();
    			    	if (n instanceof IASTDeclaration) {
    			    		decl= (IASTDeclaration) n;
    			    		trailingProblem= buildProblemDeclaration(bt2.getProblem());
    			    		break;
    			    	} 
    				} catch (EndOfFileException e) {
    					endOffset= getEndOffset();
    					break;
    				}
    			}
    		}
    		declarationMark= null;
    		
    		if (decl != null) {
    			IASTProblem problem= createProblem(IProblem.SYNTAX_ERROR, offset, endOffset-offset);
    			IASTDeclaration pd= buildProblemDeclaration(problem);
    			if (trailingProblem != null)
    				return new IASTDeclaration[] {pd, decl, trailingProblem};
    			return new IASTDeclaration[] {pd, decl};
    		}
    	}

    	return new IASTDeclaration[] {skipProblemDeclaration(offset)};
    }

	protected IASTDeclaration asmDeclaration() throws EndOfFileException, BacktrackException {
        final int offset= consume().getOffset(); // t_asm
        if (LT(1) == IToken.t_volatile) {
        	consume();
        }
        
        if (supportFunctionStyleAsm && LT(1) != IToken.tLPAREN) {
        	return functionStyleAsmDeclaration();
        }
        
        StringBuilder buffer= new StringBuilder();
        asmExpression(buffer);
        int lastOffset = consume(IToken.tSEMI).getEndOffset();

        return buildASMDirective(offset, buffer.toString(), lastOffset);
    }

	
	protected IASTDeclaration functionStyleAsmDeclaration() throws BacktrackException, EndOfFileException {

		final int offset= LA(1).getOffset();
		IASTDeclSpecifier declSpec= null;
		IASTDeclarator dtor;
    	try {
			declSpec = declSpecifierSeq(DeclarationOptions.FUNCTION_STYLE_ASM);
    		dtor = initDeclarator(DeclarationOptions.FUNCTION_STYLE_ASM);
    	} catch (FoundDeclaratorException e) {
        	if (e.altSpec != null) {
        		declSpec= e.altSpec;
        		dtor= e.altDeclarator;
        	} else {
        		declSpec = e.declSpec;
        		dtor= e.declarator;
        	}
            backup( e.currToken );
        } catch (FoundAggregateInitializer lie) {
        	if (declSpec == null)
        		declSpec= lie.fDeclSpec;
        	dtor= addInitializer(lie, DeclarationOptions.FUNCTION_STYLE_ASM);
    	}

    	if (LT(1) != IToken.tLBRACE)
    		throwBacktrack(LA(1));

    	final IASTDeclarator fdtor= ASTQueries.findTypeRelevantDeclarator(dtor);
    	if (dtor instanceof IASTFunctionDeclarator == false)
    		throwBacktrack(offset, LA(1).getEndOffset() - offset);

    	final int compoundOffset= LA(1).getOffset();
    	final int endOffset= skipOverCompoundStatement().getEndOffset();
    	IASTCompoundStatement cs = nodeFactory.newCompoundStatement(); //createCompoundStatement();
    	((ASTNode)cs).setOffsetAndLength(compoundOffset, endOffset - compoundOffset);

    	IASTFunctionDefinition funcDefinition = nodeFactory.newFunctionDefinition(declSpec, (IASTFunctionDeclarator)fdtor, cs);
    	((ASTNode) funcDefinition).setOffsetAndLength(offset, endOffset - offset);

    	return funcDefinition;
	}

	protected abstract IASTDeclarator addInitializer(FoundAggregateInitializer lie, DeclarationOptions option) throws EndOfFileException;

	protected IToken asmExpression(StringBuilder content) throws EndOfFileException, BacktrackException {
		IToken t= consume(IToken.tLPAREN);
    	boolean needspace= false;
        int open= 1;
        while (open > 0) {
        	t= consume();
			switch(t.getType()) {
			case IToken.tLPAREN:
				open++;
				break;
        	case IToken.tRPAREN:
        		open--;
        		break;
        	case IToken.tEOC:
        		throw new EndOfFileException();
        	
        	default:
        		if (content != null) {
        			if (needspace) {
        				content.append(' ');
        			}
        			content.append(t.getCharImage());
        			needspace= true;
        		}
        		break;
			}
        }
		return t;
	}

    protected IASTASMDeclaration buildASMDirective(int offset, String assembly, int lastOffset) {
        IASTASMDeclaration result = nodeFactory.newASMDeclaration(assembly);
        ((ASTNode) result).setOffsetAndLength(offset, lastOffset - offset);
        return result;
    }


    protected IASTCastExpression buildCastExpression(int op, IASTTypeId typeId, IASTExpression operand, int offset, int endOffset) {
        IASTCastExpression result = nodeFactory.newCastExpression(op, typeId, operand);
        ((ASTNode) result).setOffsetAndLength(offset, endOffset - offset);
        return result;
    }

    
    /**
     * There are many ambiguities in C and C++ between expressions and declarations.
     * This method will attempt to parse a statement as both an expression and a declaration,
     * if both parses succeed then an ambiguity node is returned.
     */
    protected IASTStatement parseDeclarationOrExpressionStatement(DeclarationOptions option) throws EndOfFileException, BacktrackException {
        // First attempt to parse an expressionStatement
        // Note: the function style cast ambiguity is handled in expression
        // Since it only happens when we are in a statement
        IToken mark = mark();
        IASTExpressionStatement expressionStatement = null;
        IToken lastTokenOfExpression = null;
        try {
            IASTExpression expression = expression();
            if (LT(1) == IToken.tEOC)
                lastTokenOfExpression = consume();
            else
                lastTokenOfExpression = consume(IToken.tSEMI);
            expressionStatement = nodeFactory.newExpressionStatement(expression);
            ((ASTNode) expressionStatement).setOffsetAndLength(mark.getOffset(), lastTokenOfExpression.getEndOffset() - mark.getOffset());
        } catch (BacktrackException b) {
        }

        backup(mark);

        // Now attempt to parse a declarationStatement
        IASTDeclarationStatement ds = null;
        try {
            IASTDeclaration d = declaration(option);
            ds = nodeFactory.newDeclarationStatement(d);
            ((ASTNode) ds).setOffsetAndLength(((ASTNode) d).getOffset(), ((ASTNode) d).getLength());
        } catch (BacktrackException b) {
            if (expressionStatement == null) {
            	throw b;
            }
        }

        if (expressionStatement == null) {
        	return ds;
        }
        if (ds == null) {
        	backup(lastTokenOfExpression); consume();
            return expressionStatement;
        }
        
        // At this point we know we have an ambiguity.
        // Attempt to resolve some ambiguities that are easy to detect.
        
        // A * B = C;  // A*B cannot be a lvalue.
        // foo() = x;  // foo() cannot be a lvalue in c, in c++ it can.
        if (expressionStatement.getExpression() instanceof IASTBinaryExpression) {
            IASTBinaryExpression exp = (IASTBinaryExpression) expressionStatement.getExpression();
            if (exp.getOperator() == IASTBinaryExpression.op_assign) {
                IASTExpression lhs = exp.getOperand1();
                if (lhs instanceof IASTBinaryExpression
                        && ((IASTBinaryExpression) lhs).getOperator() == IASTBinaryExpression.op_multiply) {
                    return ds;
                }
                if (lhs instanceof IASTFunctionCallExpression && !functionCallCanBeLValue) {
                    return ds;
                }
            }
        }

        final IASTDeclaration declaration = ds.getDeclaration();
		if (declaration instanceof IASTSimpleDeclaration) {
	        final IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
	        IASTDeclSpecifier declspec= simpleDecl.getDeclSpecifier();
	        if (declspec instanceof IASTNamedTypeSpecifier) {
	        	final IASTDeclarator[] declarators = simpleDecl.getDeclarators();

	            // x; 
	            // can be parsed as a named declaration specifier without a declarator
	        	if (declarators.length == 0) {
					backup(lastTokenOfExpression); consume();
					return expressionStatement;
	        	}
	        }
		}

        // create and return ambiguity node
        IASTAmbiguousStatement statement = createAmbiguousStatement();
        statement.addStatement(expressionStatement);
        statement.addStatement(ds);
        ((ASTNode) statement).setOffsetAndLength((ASTNode) ds);
        return statement;
    }
    
    
    /**
     * Returns true if the given declaration has unspecified type,
     * in this case the type defaults to int and is know as "implicit int".
     */
    protected static boolean isImplicitInt(IASTDeclaration declaration) {
    	if(declaration instanceof IASTSimpleDeclaration) {
    		IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration)declaration).getDeclSpecifier();
    		if(declSpec instanceof IASTSimpleDeclSpecifier && 
    		   ((IASTSimpleDeclSpecifier)declSpec).getType() == IASTSimpleDeclSpecifier.t_unspecified) {
    			return true;
    		}
    	}
    	return false;
    }

    
    protected abstract IASTAmbiguousStatement createAmbiguousStatement();

    protected IASTStatement parseLabelStatement() throws EndOfFileException, BacktrackException {
    	int offset= LA(1).getOffset();
    	IASTName name = identifier(); // tIDENTIFIER
        consume(IToken.tCOLON); // tCOLON
        IASTStatement nestedStatement = statement();
        int lastOffset = calculateEndOffset( nestedStatement );
        
        
        IASTLabelStatement label_statement = nodeFactory.newLabelStatement(name, nestedStatement);
        setRange(label_statement, offset, lastOffset);
        return label_statement;
    }

    protected IASTStatement parseNullStatement() throws EndOfFileException, BacktrackException {
        IToken t = consume(); // tSEMI

        IASTNullStatement null_statement = nodeFactory.newNullStatement();
        ((ASTNode) null_statement).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
        return null_statement;
    }

    protected IASTStatement parseGotoStatement() throws EndOfFileException, BacktrackException {
        int startOffset = consume().getOffset(); // t_goto
        IASTName goto_label_name = identifier();
        int lastOffset = consume(IToken.tSEMI).getEndOffset();

        IASTGotoStatement goto_statement = nodeFactory.newGotoStatement(goto_label_name);
        ((ASTNode) goto_statement).setOffsetAndLength(startOffset, lastOffset - startOffset);
        return goto_statement;
    }

    protected IASTStatement parseBreakStatement() throws EndOfFileException, BacktrackException {
        int startOffset = consume().getOffset(); // t_break
        int lastOffset = consume(IToken.tSEMI).getEndOffset();

        IASTBreakStatement break_statement = nodeFactory.newBreakStatement();
        ((ASTNode) break_statement).setOffsetAndLength(startOffset, lastOffset - startOffset);
        return break_statement;
    }

    protected IASTStatement parseSwitchBody() throws EndOfFileException, BacktrackException {
		IASTStatement stmt= null;
        if (LT(1) != IToken.tEOC)
        	stmt= statement();
    
        if (stmt instanceof IASTCaseStatement == false) 
        	return stmt;
        
        // bug 105334, switch without compound statement
        IASTCompoundStatement comp= nodeFactory.newCompoundStatement();
        ((ASTNode) comp).setOffsetAndLength((ASTNode) stmt);
        comp.addStatement(stmt);

        while (LT(1) != IToken.tEOC && stmt instanceof IASTCaseStatement) {
        	stmt= statement();
        	comp.addStatement(stmt);
        }
        adjustLength(comp, stmt);
		return comp;
	}

    protected IASTStatement parseContinueStatement() throws EndOfFileException, BacktrackException {
        int startOffset = consume().getOffset(); // t_continue
        int lastOffset = consume(IToken.tSEMI).getEndOffset();

        IASTContinueStatement continue_statement = nodeFactory.newContinueStatement();
        ((ASTNode) continue_statement).setOffsetAndLength(startOffset, lastOffset - startOffset);
        return continue_statement;
    }

    protected IASTStatement parseReturnStatement() throws EndOfFileException, BacktrackException {
        int startOffset;
        startOffset = consume().getOffset(); // t_return
        IASTExpression result = null;

        // See if there is a return expression
        switch (LT(1)) {
        case IToken.tEOC:
            // We're trying to start one
            IASTName name = identifier();
            IASTIdExpression idExpr = nodeFactory.newIdExpression(name);
            result = idExpr;
            break;
        case IToken.tSEMI:
            // None
            break;
        default:
            // Yes
            result = expression();
            break;
        }

        int lastOffset = 0;
        switch (LT(1)) {
        case IToken.tSEMI:
        case IToken.tEOC:
            lastOffset = consume().getEndOffset();
            break;
        default:
            throwBacktrack(LA(1));
        }

        IASTReturnStatement return_statement = nodeFactory.newReturnStatement(result);
        ((ASTNode) return_statement).setOffsetAndLength(startOffset, lastOffset - startOffset);
        return return_statement;
    }

    protected IASTStatement parseDoStatement() throws EndOfFileException, BacktrackException {
        int startOffset;
        startOffset = consume().getOffset(); // t_do
        IASTStatement do_body = statement();

        IASTExpression do_condition = null;
        if (LT(1) != IToken.tEOC) {
            consume(IToken.t_while);
            consume(IToken.tLPAREN);
            do_condition = condition(true);
        }
        
        int lastOffset;
        switch (LT(1)) {
        case IToken.tRPAREN:
        case IToken.tEOC:
            consume();
            break;
        default:
            throw backtrack;
        }
        
        switch (LT(1)) {
        case IToken.tSEMI:
        case IToken.tEOC:
            lastOffset = consume().getEndOffset();
            break;
        default:
            throw backtrack;
        }
        
        IASTDoStatement do_statement = nodeFactory.newDoStatement(do_body, do_condition);
        ((ASTNode) do_statement).setOffsetAndLength(startOffset, lastOffset - startOffset);
        return do_statement;
    }

    protected IASTStatement parseWhileStatement() throws EndOfFileException, BacktrackException {
        int startOffset = consume().getOffset();
        consume(IToken.tLPAREN);
        IASTExpression while_condition = condition(true);
        switch (LT(1)) {
        case IToken.tRPAREN:
            consume();
            break;
        case IToken.tEOC:
            break;
        default:
            throwBacktrack(LA(1));
        }
        IASTStatement while_body = null;
        if (LT(1) != IToken.tEOC)
            while_body = statement();

        IASTWhileStatement while_statement = nodeFactory.newWhileStatement(while_condition, while_body);
        ((ASTNode) while_statement).setOffsetAndLength(startOffset,
                (while_body != null ? calculateEndOffset(while_body) : LA(1).getEndOffset()) - startOffset);

        return while_statement;
    }

    /**
     * @param result
     */
    protected void reconcileLengths(IASTIfStatement result) {
        if (result == null)
            return;
        IASTIfStatement current = result;
        while (current.getElseClause() instanceof IASTIfStatement)
            current = (IASTIfStatement) current.getElseClause();

        while (current != null) {
            ASTNode r = ((ASTNode) current);
            if (current.getElseClause() != null) {
                ASTNode else_clause = ((ASTNode) current.getElseClause());
                r.setLength(else_clause.getOffset() + else_clause.getLength() - r.getOffset());
            } else {
                ASTNode then_clause = (ASTNode) current.getThenClause();
                if (then_clause != null)
                    r.setLength(then_clause.getOffset() + then_clause.getLength() - r.getOffset());
            }
            if (current.getParent() != null && current.getParent() instanceof IASTIfStatement)
                current = (IASTIfStatement) current.getParent();
            else
                current = null;
        }
    }


    protected IASTStatement parseCompoundStatement() throws EndOfFileException, BacktrackException {
        IASTCompoundStatement compound = compoundStatement();
        return compound;
    }

    protected IASTStatement parseDefaultStatement() throws EndOfFileException, BacktrackException {
        int startOffset = consume().getOffset(); // t_default
        int lastOffset = consume(IToken.tCOLON).getEndOffset();

        IASTDefaultStatement df = nodeFactory.newDefaultStatement();
        ((ASTNode) df).setOffsetAndLength(startOffset, lastOffset - startOffset);
        return df;
    }

    protected IASTStatement parseCaseStatement() throws EndOfFileException, BacktrackException {
        int startOffset = consume().getOffset(); // t_case
        IASTExpression caseExpression = constantExpression();
        int lt1 = LT(1);
		if (lt1 == IToken.tELLIPSIS) {
			consume();
        	IASTExpression upperBoundExpression= constantExpression();
        	caseExpression = buildBinaryExpression(IASTBinaryExpression.op_ellipses,
        			caseExpression, upperBoundExpression, calculateEndOffset(upperBoundExpression));
        	lt1= LT(1);
		}
        int lastOffset = 0;
        switch (lt1) {
        case IToken.tCOLON:
        case IToken.tEOC:
            lastOffset = consume().getEndOffset();
            break;
        default:
            throwBacktrack(LA(1));
        }

        IASTCaseStatement cs = nodeFactory.newCaseStatement(caseExpression);
        ((ASTNode) cs).setOffsetAndLength(startOffset, lastOffset - startOffset);
        return cs;
    }

 
    protected int figureEndOffset(IASTDeclSpecifier declSpec, IASTDeclarator[] declarators) {
        if (declarators.length == 0)
            return calculateEndOffset(declSpec);
        return calculateEndOffset(declarators[declarators.length - 1]);
    }


    protected int figureEndOffset(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator) {
        if (declarator == null || ((ASTNode) declarator).getLength() == 0)
            return calculateEndOffset(declSpecifier);
        return calculateEndOffset(declarator);
    }


    protected void throwBacktrack(IToken token) throws BacktrackException {
        throwBacktrack(token.getOffset(), token.getLength());
    }

    protected IASTExpression parseTypeidInParenthesisOrUnaryExpression(boolean exprIsLimitedToParenthesis, 
    		int offset, int typeExprKind, int unaryExprKind) throws BacktrackException, EndOfFileException {
    	IASTTypeId typeid;
    	IASTExpression expr= null;
        IToken typeidLA= null;
    	IToken mark = mark();
    	int endOffset1= -1;
    	int endOffset2= -1;

        try {
        	consume(IToken.tLPAREN);
        	int typeidOffset= LA(1).getOffset();
            typeid= typeId(DeclarationOptions.TYPEID);
            if (!isValidTypeIDForUnaryExpression(unaryExprKind, typeid)) {
            	typeid= null;
            } else {
            	switch(LT(1)) {
            	case IToken.tRPAREN:
            	case IToken.tEOC:
            		endOffset1= consume().getEndOffset();
                	typeidLA= LA(1);
            		break;
            	case IToken.tCOMMA:
            		if (supportExtendedSizeofOperator && typeExprKind == IASTTypeIdExpression.op_sizeof) {
            			consume();
            			IASTExpression expr2= expression();
            			endOffset1= consumeOrEOC(IToken.tRPAREN).getEndOffset();
						expr= nodeFactory.newTypeIdExpression(IASTTypeIdExpression.op_typeof, typeid);
						setRange(expr, typeidOffset, calculateEndOffset(typeid));

            	        IASTExpressionList expressionList = nodeFactory.newExpressionList();
            	        ((ASTNode) expressionList).setOffsetAndLength(typeidOffset, calculateEndOffset(expr2)-typeidOffset);
            	        expressionList.addExpression(expr);
            	        if (expr2 instanceof IASTExpressionList) {
            	        	for (IASTExpression e : ((IASTExpressionList) expr2).getExpressions()) {
								expressionList.addExpression(e);
							}
            	        } else {
            	        	expressionList.addExpression(expr2);
            	        }
            	        
            	        return buildUnaryExpression(unaryExprKind, expressionList, offset, endOffset1);
            		}
            		typeid= null;
            		break;
            	default:
            		typeid= null;
            		break;
            	}
            }
        } catch (BacktrackException e) { 
        	typeid= null;
        }
        
        backup(mark);
        try {
        	if (exprIsLimitedToParenthesis) {
        		consume(IToken.tLPAREN);
        		expr= expression();
        		endOffset2= consumeOrEOC(IToken.tRPAREN).getEndOffset();
        	} else {
        		expr= unaryExpression(); 
            	endOffset2= calculateEndOffset(expr);
            }
        } catch (BacktrackException bte) { 
        	if (typeid == null)
        		throw bte;
        }

        IASTExpression result1= null;
        if (typeid != null && endOffset1 >= endOffset2) {
			IASTTypeIdExpression typeIdExpression = nodeFactory.newTypeIdExpression(typeExprKind, typeid);
			setRange(typeIdExpression, offset, endOffset1);
			result1= typeIdExpression;
        	backup(typeidLA);
        	
        	if (expr == null || endOffset1 > endOffset2)
        		return result1;
        }
        
        IASTExpression result2= buildUnaryExpression(unaryExprKind, expr, offset, endOffset2);
        if (result1 == null)
        	return result2;


        IASTAmbiguousExpression ambExpr = createAmbiguousExpression();
        ambExpr.addExpression(result1);
        ambExpr.addExpression(result2);
        ((ASTNode) ambExpr).setOffsetAndLength((ASTNode) result1);
        return ambExpr;
    }

	private boolean isValidTypeIDForUnaryExpression(int unaryExprKind, IASTTypeId typeid) {
		if (typeid == null)
			return false;
		if (unaryExprKind == IASTUnaryExpression.op_sizeof) {
			// 5.3.3.1
			if (ASTQueries.findTypeRelevantDeclarator(typeid.getAbstractDeclarator()) instanceof IASTFunctionDeclarator)
				return false;
		}
		return true;
	}

	protected abstract IASTAmbiguousExpression createAmbiguousExpression();
    protected abstract IASTAmbiguousExpression createAmbiguousBinaryVsCastExpression(IASTBinaryExpression binary, IASTCastExpression castExpr);
    protected abstract IASTAmbiguousExpression createAmbiguousCastVsFunctionCallExpression(IASTCastExpression castExpr, IASTFunctionCallExpression funcCall);

    protected IASTStatement forInitStatement(DeclarationOptions option) throws BacktrackException, EndOfFileException {
        if( LT(1) == IToken.tSEMI )
            return parseNullStatement();
        return parseDeclarationOrExpressionStatement(option);
    }

    /**
     * Accept a sequence of __attribute__ or __declspec
     * 
     * @param allowAttrib if true accept any number of __attribute__ 
     * @param allowDeclspec if true accept any number of __declspec
     * @throws BacktrackException
     * @throws EndOfFileException
     */
    protected void __attribute_decl_seq(boolean allowAttrib, boolean allowDeclspec) throws BacktrackException, EndOfFileException {
        while (true) {
        	final int lt = LTcatchEOF(1);
        	if ( allowAttrib && (lt == IGCCToken.t__attribute__)) {
        		__attribute__();
        	} else if (allowDeclspec && (lt == IGCCToken.t__declspec)) {
        		__declspec();
        	} else {
        		break;
        	}
        }
    }

    protected void __attribute__() throws BacktrackException, EndOfFileException {    	
    	if (LT(1) != IGCCToken.t__attribute__) 
    		return;
    	
    	consume();
    	if (LT(1) == IToken.tLPAREN) {
    		consume(); 
    		consume(IToken.tLPAREN);

    		for (;;) {
    			final int lt1= LT(1);				
    			if (lt1 == IToken.tRPAREN || lt1 == IToken.tEOC) 
    				break;
    			
    			// Allow empty attribute
    			if (lt1 != IToken.tCOMMA) {
    				singelAttribute();
    			}

				// Require comma
    			if (LT(1) != IToken.tCOMMA) 
    				break;
    			consume();
    		}

    		consumeOrEOC(IToken.tRPAREN);
    		consumeOrEOC(IToken.tRPAREN);
    	}
    }

	private void singelAttribute() throws EndOfFileException, BacktrackException {
		// Check if we have an identifier including keywords
		if (!isIdentifier(LA(1)))
			throw backtrack;					
		consume();

		// Check for parameters
		if (LT(1) == IToken.tLPAREN) {
			consume();
			for(;;) {
				final int lt2= LT(1);				
				if (lt2 == IToken.tRPAREN || lt2 == IToken.tEOC) 
					break;

				// Allow empty parameter
				if (lt2 != IToken.tCOMMA) {
					expression();
				}
				// Require comma
				if (LT(1) != IToken.tCOMMA) 
					break;
				consume();
			}
			consumeOrEOC(IToken.tRPAREN);
		}
	}
    
	private boolean isIdentifier(IToken t) {
		char[] image= t.getCharImage();
		if (image.length == 0)
			return false;
		char firstChar= image[0];
		return Character.isLetter(firstChar) || firstChar == '_'; 
	}

	protected void __declspec() throws BacktrackException, EndOfFileException {
    	IToken token = LA(1);
    	if (token.getType() == IGCCToken.t__declspec) {
    		consume();
    		if (LT(1) == IToken.tLPAREN) {
    	    	skipBrackets(IToken.tLPAREN, IToken.tRPAREN);
    		}
    	}
    }    

    /**
	 * Hook method to support (skip) additional declspec modifiers.
	 * @throws BacktrackException
     * @throws EndOfFileException 
	 */
	protected void handleOtherDeclSpecModifier() throws BacktrackException, EndOfFileException {
		// default action: consume keyword plus optional parenthesised "something"
		consume();

		IToken token = LA(1);

		if (token.getType() == IToken.tLPAREN) {
			consume();
			int openParen= 1;
			while(true) {
				token = LA(1);
				consume();
				if (token.getType() == IToken.tLPAREN) {
					++openParen;
				} else if (token.getType() == IToken.tRPAREN) {
					--openParen;
					if (openParen == 0) {
						break;
					}
				}
			}
		}
	}

    /**
     * In case a cast expression is followed by +/- or & we should avoid it:
     * (a)+1 vs. (int)+1;
     * @since 4.0
     */
	protected boolean avoidCastExpressionByHeuristics() throws EndOfFileException {
		if (LT(1) == IToken.tIDENTIFIER) {
			if (LT(2) == IToken.tRPAREN) {
				switch (LT(3)) {
				case IToken.tPLUS:
				case IToken.tMINUS:
				case IToken.tAMPER:
				case IToken.tSTAR:
		    		return true;
		    	}
			}
		}
		return false;
	}
	
	protected boolean canBeTypeSpecifier() throws EndOfFileException {

		switch (LT(1)) {
		// simple type specifiers:
		case IToken.tIDENTIFIER:
		case IToken.tCOLONCOLON:
        case IToken.t_void:
		case IToken.t_char:
		case IToken.t_wchar_t:
		case IToken.t_bool:
		case IToken.t_short:
        case IToken.t_int:
        case IToken.t_long:
        case IToken.t_float:
        case IToken.t_double:
        case IToken.t__Bool:
        case IToken.t__Complex:
        case IToken.t__Imaginary:
        case IToken.t_signed:
        case IToken.t_unsigned:

        // class-specifier:
        case IToken.t_class:
        case IToken.t_struct:
        case IToken.t_union:

        // enum-specifier:
        case IToken.t_enum:

        // elaborated type specifier: (together with class, struct, union, enum
        case IToken.t_typename:
        	
        // cq-qualifiers
		case IToken.t_const:
        case IToken.t_volatile:
        case IToken.t_restrict:

        // gcc-special
        case IGCCToken.t_typeof:
        case IGCCToken.t__attribute__:

        // content assist
        case IToken.tCOMPLETION:
        	return true;
        	
        default:
        	return false;
		}
	}
	
	protected void skipBrackets(int left, int right) throws EndOfFileException, BacktrackException {
		consume(left);
		int nesting= 0;
		while(true) {
			final int lt1= LT(1);
			if (lt1 == IToken.tEOC)
				throwBacktrack(LA(1));

			consume();
			if (lt1 == left) {
				nesting++;
			} else if (lt1 == right) {
				if (--nesting < 0) {
					return;
				}
			}
		}
	}
}
