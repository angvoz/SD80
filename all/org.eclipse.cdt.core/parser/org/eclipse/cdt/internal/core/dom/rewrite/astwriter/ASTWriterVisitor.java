/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *    Institute for Software - initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTLiteralNode;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * 
 * Visits all nodes, prints leading comments and handles macro expansions. The
 * source code generation is delegated to severals <code>NodeWriters</code>.
 * 
 * @see NodeWriter
 * @see MacroExpansionHandler
 * 
 * @author Emanuel Graf IFS
 * 
 */
public class ASTWriterVisitor extends CPPASTVisitor {
	
	protected Scribe scribe = new Scribe();
	protected NodeCommentMap commentMap;
	protected ExpressionWriter expWriter;
	protected DeclSpecWriter declSpecWriter;
	protected StatementWriter statementWriter;
	protected DeclaratorWriter declaratorWriter;
	protected DeclarationWriter declarationWriter;
	protected InitializerWriter initializerWriter;
	protected NameWriter nameWriter;
	protected TemplateParameterWriter tempParameterWriter;
	protected MacroExpansionHandler macroHandler;
	{
		shouldVisitExpressions = true;
		
		shouldVisitStatements = true;
		
		shouldVisitNames = true;
		
		shouldVisitDeclarations = true;
		
		shouldVisitDeclSpecifiers = true;
		
		shouldVisitDeclarators = true;
		
		shouldVisitArrayModifiers= true;
		
		shouldVisitInitializers = true;
		
		shouldVisitBaseSpecifiers = true;

		shouldVisitNamespaces = true;

		shouldVisitTemplateParameters = true;
		
		shouldVisitParameterDeclarations = true;
		
		shouldVisitTranslationUnit = true;
	}
	
	
	
	public ASTWriterVisitor(NodeCommentMap commentMap) {
		this("", commentMap); //$NON-NLS-1$
	}



	public ASTWriterVisitor(String givenIndentation, NodeCommentMap commentMap) {
		super();
		scribe.setGivenIndentation(givenIndentation);
		init(commentMap);
		this.commentMap = commentMap;

	}

	private void init(NodeCommentMap commentMap) {
		macroHandler = new MacroExpansionHandler(scribe);
		statementWriter = new StatementWriter(scribe,this, commentMap);
		declaratorWriter = new DeclaratorWriter(scribe,this, commentMap);
		declarationWriter = new DeclarationWriter(scribe,this, commentMap);
		declSpecWriter = new DeclSpecWriter(scribe,this, commentMap);
		expWriter = new ExpressionWriter(scribe,this, macroHandler, commentMap);
		initializerWriter = new InitializerWriter (scribe,this, commentMap);
//		ppStmtWriter = new PreprocessorStatementWriter(scribe, this, commentMap);
		nameWriter = new NameWriter(scribe,this, commentMap);
		tempParameterWriter = new TemplateParameterWriter(scribe, this, commentMap);
	}
	
	@Override
	public String toString(){
		return scribe.toString();
	}

	@Override
	public int leave(IASTTranslationUnit tu) {
		for(IASTComment comment : commentMap.getFreestandingCommentsForNode(tu)) {
			scribe.print(comment.getComment());
			scribe.newLine();
		}	
		return super.leave(tu);
	}

	private void writeLeadingComments(IASTNode node) {
		for(IASTComment comment : commentMap.getLeadingCommentsForNode(node)) {
			scribe.print(comment.getComment());
			scribe.newLine();
		}		
	}
	
	public void visit(ASTLiteralNode lit) {
		scribe.print(lit.getRawSignature());
	}
	
	@Override
	public int visit(IASTName name) {
		writeLeadingComments(name);
		if(!macroHandler.checkisMacroExpansionNode(name)) {
			nameWriter.writeName(name);
		}
		return ASTVisitor.PROCESS_SKIP;
	}



	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		writeLeadingComments(declSpec);
		declSpecWriter.writeDelcSpec(declSpec);			
		return ASTVisitor.PROCESS_SKIP;
	}



	@Override
	public int visit(IASTExpression expression) {
		writeLeadingComments(expression);		
		if(!macroHandler.checkisMacroExpansionNode(expression)) {
			if (expression instanceof IGNUASTCompoundStatementExpression) {
				IGNUASTCompoundStatementExpression gnuCompStmtExp = (IGNUASTCompoundStatementExpression) expression;
				gnuCompStmtExp.getCompoundStatement().accept(this);
			}else {
				expWriter.writeExpression(expression);
			}
		}
		return ASTVisitor.PROCESS_SKIP;
	}



	@Override
	public int visit(IASTStatement statement) {
		writeLeadingComments(statement);
		if(macroHandler.isStatementWithMixedLocation(statement) && !(statement instanceof IASTCompoundStatement)){
			return statementWriter.writeMixedStatement(statement);
		}
		if(macroHandler.checkisMacroExpansionNode(statement)) {
			return ASTVisitor.PROCESS_SKIP;
		}
		return statementWriter.writeStatement(statement, true);
	}


	@Override
	public int visit(IASTDeclaration declaration) {
		writeLeadingComments(declaration);
		if(!macroHandler.checkisMacroExpansionNode(declaration)) {
			declarationWriter.writeDeclaration(declaration);
		}
		return  ASTVisitor.PROCESS_SKIP;
	}



	@Override
	public int visit(IASTDeclarator declarator) {
		writeLeadingComments(declarator);
		if(!macroHandler.checkisMacroExpansionNode(declarator)) {
			declaratorWriter.writeDeclarator(declarator);
		}
		return ASTVisitor.PROCESS_SKIP;
	}

	@Override
	public int visit(IASTArrayModifier amod) {
		if(!macroHandler.checkisMacroExpansionNode(amod)) {
			declaratorWriter.writeArrayModifier(amod);
		}
		return ASTVisitor.PROCESS_SKIP;
	}


	@Override
	public int visit(IASTInitializer initializer) {
		writeLeadingComments(initializer);
		if(!macroHandler.checkisMacroExpansionNode(initializer)) {
			initializerWriter.writeInitializer(initializer);
		}
		return ASTVisitor.PROCESS_SKIP;
	}



	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		writeLeadingComments(parameterDeclaration);
		if(!macroHandler.checkisMacroExpansionNode(parameterDeclaration)) {
			parameterDeclaration.getDeclSpecifier().accept(this);
			IASTDeclarator declarator = getParameterDeclarator(parameterDeclaration);
			
			if(getParameterName(declarator).toString().length() != 0){
				scribe.printSpaces(1);
			}
			declarator.accept(this);
		}
		return ASTVisitor.PROCESS_SKIP;
	}



	protected IASTName getParameterName(IASTDeclarator declarator) {
		return declarator.getName();
	}


	protected IASTDeclarator getParameterDeclarator(
			IASTParameterDeclaration parameterDeclaration) {
		return parameterDeclaration.getDeclarator();
	}
	
	@Override
	public int visit(ICPPASTNamespaceDefinition namespace) {
		writeLeadingComments(namespace);
		if(!macroHandler.checkisMacroExpansionNode(namespace)) {
			declarationWriter.writeDeclaration(namespace);
		}
		return ASTVisitor.PROCESS_SKIP;
	}

	@Override
	public int visit(ICPPASTTemplateParameter parameter) {
		writeLeadingComments(parameter);
		if(!macroHandler.checkisMacroExpansionNode(parameter)) {
			tempParameterWriter.writeTemplateParameter(parameter);
		}
		return ASTVisitor.PROCESS_SKIP;
	}


	public void cleanCache() {
		scribe.cleanCache();
		macroHandler.reset();
	}

}
