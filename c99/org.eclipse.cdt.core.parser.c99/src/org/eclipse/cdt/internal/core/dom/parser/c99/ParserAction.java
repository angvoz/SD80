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
package org.eclipse.cdt.internal.core.dom.parser.c99;

import java.util.List;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.c99.IASTNodeFactory;
import org.eclipse.cdt.core.dom.c99.IParserActionTokenProvider;
import org.eclipse.cdt.core.dom.parser.c99.ASTStack;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;



public abstract class ParserAction {

	public static final char[] EMPTY_CHAR_ARRAY = {};
	
	
	/** Stack that holds the intermediate nodes as the AST is being built */
	// TODO refine the type to ASTNode, the problem is that tokens are also being stored on the stack
	// they would have to be stored somewhere else or wrapped
	protected final ASTStack<Object> astStack = new ASTStack<Object>();
	
	/** Used to create the AST node objects */
	protected final IASTNodeFactory nodeFactory;
	
	/** Provides an interface to the token stream */
	protected final IParserActionTokenProvider parser;

	
	
	/** set to true if a problem is encountered */
	private boolean encounteredRecoverableProblem = false;
	
	/** The completion node generated during a completion parse */
	private ASTCompletionNode completionNode;

	/** The offset and length of each grammar rule will be automatically calculated. */
	private int ruleOffset;
	private int ruleLength;
	
	
	
	protected abstract IASTNodeFactory createNodeFactory();
	
	
	/**
	 * @param parser
	 * @param orderedTerminalSymbols When an instance of this class is created for a parser
	 * that parsers token kinds will be mapped back to the base C99 parser's token kinds.
	 */
	public ParserAction(IParserActionTokenProvider parser) {
		this.parser = parser;
		this.nodeFactory = createNodeFactory();
	}
	
	
	protected IASTNodeFactory getNodeFactory() {
		return nodeFactory;
	}

	
	protected ASTStack getASTStack() {
		return astStack;
	}
	
	
	protected void setEncounteredRecoverableProblem(boolean problem) {
		this.encounteredRecoverableProblem = problem;
	}
	
	
	/**
	 * Returns an AST after a successful parse, null otherwise.
	 */
	public IASTTranslationUnit getAST() {
		if(astStack.isEmpty())
			return null;
		
		IASTTranslationUnit tu = (IASTTranslationUnit) astStack.peek();
		generateCommentNodes(tu);
		forceAmbiguityResolution(tu);
		return tu;
	}
	

	
	
	/**
	 * Forces resolution of ambiguity nodes by applying a visitor to the AST.
	 * The visitor itself doesn't do anything, however ambiguity nodes will resolve 
	 * themselves when their accept() method is called.
	 */
	private void forceAmbiguityResolution(IASTTranslationUnit tu) {
		CASTVisitor emptyVisitor = new CASTVisitor() { { shouldVisitStatements = true; } };
		tu.accept(emptyVisitor);
	}
	
	
	/**
	 * Generates a comment node for each comment token.
	 */
	private void generateCommentNodes(IASTTranslationUnit tu) {
		List<IToken> commentTokens = parser.getCommentTokens();
		if(commentTokens == null || commentTokens.isEmpty())
			return;
		
		IASTComment[] commentNodes = new IASTComment[commentTokens.size()];
		
		for(int i = 0; i < commentNodes.length; i++) {
			IToken token = commentTokens.get(i);
			IASTComment comment = nodeFactory.newComment();
			comment.setParent(tu);
			comment.setComment(token.toString().toCharArray());
			setOffsetAndLength(comment, token);
			commentNodes[i] = comment;
		}
		
		tu.setComments(commentNodes);
	}
	
	
	/**
	 * Returns true if a syntax error was encountered during the parse.
	 */
	public boolean encounteredError() {
		// if the astStack is empty then an unrecoverable syntax error was encountered
		return encounteredRecoverableProblem || astStack.isEmpty();
	}

	/**
	 * Method that is called by the special <openscope> production
	 * in order to create a new scope in the AST stack.
	 */
	public void openASTScope() {
		astStack.openASTScope();
	}
	
	
	/**
	 * Returns the completion node if this is a completion parse.
	 */
	public IASTCompletionNode getASTCompletionNode() {
		if(completionNode != null)
			completionNode.setTranslationUnit(getAST());
		return completionNode;
	}
	
	
	protected void addNameToCompletionNode(IASTName name, String prefix) {
		if(completionNode == null)
			completionNode = nodeFactory.newCompletionNode(prefix.length() == 0 ? null : prefix);
		
		completionNode.addName(name);
	}
	
	
	
	// convenience methods for setting offsets and lengths on nodes
	
	protected static int offset(IToken token) {
		return token.getStartOffset();
	}
	
	protected static int offset(IASTNode node) {
		return ((ASTNode)node).getOffset();
	}
	
	protected static int length(IToken token) {
		return endOffset(token) - offset(token);
	}
	
	protected static int length(IASTNode node) {
		return ((ASTNode)node).getLength();
	}
	
	protected static int endOffset(IASTNode node) {
		return offset(node) + length(node);
	}
	
	protected static int endOffset(IToken token) {
		return token.getEndOffset() + 1;
	}
	
	protected void setOffsetAndLength(IASTNode node) {
		((ASTNode)node).setOffsetAndLength(ruleOffset, ruleLength);
	}
	
	protected void setOffsetAndLength(IASTNode node, IToken token) {
		((ASTNode)node).setOffsetAndLength(offset(token), length(token));
	}
	
	protected void setOffsetAndLength(IASTNode node, int offset, int length) {
		((ASTNode)node).setOffsetAndLength(offset, length);
	}
	

	/**
	 * Special action that is always called before every consume action.
	 * Calculates AST node offsets automatically.
	 * 
	 * TODO: If all the references to ruleOffset and ruleLength are removed then
	 * this code can be inlined into setOffsetAndLength() and this method
	 * can be removed
	 */
	public void beforeConsume() {
		ruleOffset = parser.getLeftIToken().getStartOffset();
		ruleLength = parser.getRightIToken().getEndOffset() + 1 - ruleOffset;
	}
	
	
	protected int getRuleOffset() {
		return ruleOffset;
	}
	
	
	protected int getRuleLength() {
		return ruleLength;
	}
	
	
	/**
	 * Gets the current token and places it on the stack for later consumption.
	 */
	public void consumeToken() {
		astStack.push(parser.getRightIToken());
	}
	
	
}
