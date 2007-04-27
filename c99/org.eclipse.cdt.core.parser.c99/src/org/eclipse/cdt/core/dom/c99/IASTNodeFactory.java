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
package org.eclipse.cdt.core.dom.c99;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c99.ASTCompletionNode;

/**
 * Factory for AST nodes, used by the semantic actions 
 * in C99ParserAction to build the AST.
 * 
 * @author Mike Kucera
 */
public interface IASTNodeFactory {

	public IASTName newName(char[] name);

	public IASTName newName();

	// TODO: this should return IASTCompletionNode
	public ASTCompletionNode newCompletionNode(String prefix);

	public IASTLiteralExpression newLiteralExpression();

	public IASTIdExpression newIdExpression();

	public IASTBinaryExpression newBinaryExpression();

	public IASTConditionalExpression newConditionalExpession();

	public IASTArraySubscriptExpression newArraySubscriptExpression();

	public IASTFunctionCallExpression newFunctionCallExpression();

	public IASTExpressionList newExpressionList();

	public IASTFieldReference newFieldReference();

	public IASTUnaryExpression newUnaryExpression();

	public IASTTypeIdExpression newTypeIdExpression();

	public ICASTTypeIdInitializerExpression newCTypeIdInitializerExpression();

	public IASTCastExpression newCastExpression();

	public IASTTypeId newTypeId();

	public IASTDeclarator newDeclarator();

	public IASTArrayDeclarator newArrayDeclarator();

	public ICASTArrayModifier newCArrayModifier();

	public IASTArrayModifier newArrayModifier();

	public IASTStandardFunctionDeclarator newFunctionDeclarator();

	public ICASTKnRFunctionDeclarator newCKnRFunctionDeclarator();

	public ICASTPointer newCPointer();

	public IASTParameterDeclaration newParameterDeclaration();

	public IASTInitializerExpression newInitializerExpression();

	public IASTInitializerList newInitializerList();

	public ICASTDesignatedInitializer newCDesignatedInitializer();

	public ICASTArrayDesignator newCArrayDesignator();

	public ICASTFieldDesignator newCFieldDesignator();

	public ICASTSimpleDeclSpecifier newCSimpleDeclSpecifier();

	public ICASTTypedefNameSpecifier newCTypedefNameSpecifier();

	public IASTSimpleDeclaration newSimpleDeclaration();

	public IASTFieldDeclarator newFieldDeclarator();

	public ICASTCompositeTypeSpecifier newCCompositeTypeSpecifier();

	public ICASTElaboratedTypeSpecifier newCElaboratedTypeSpecifier();

	public ICASTEnumerationSpecifier newCEnumerationSpecifier();

	public IASTEnumerator newEnumerator();

	public IASTCompoundStatement newCompoundStatement();

	public IASTForStatement newForStatement();

	public IASTExpressionStatement newExpressionStatement();

	public IASTDeclarationStatement newDeclarationStatement();

	public IASTNullStatement newNullStatement();

	public IASTWhileStatement newWhileStatement();

	public IASTDoStatement newDoStatement();

	public IASTGotoStatement newGotoStatement();

	public IASTContinueStatement newContinueStatement();

	public IASTBreakStatement newBreakStatement();

	public IASTReturnStatement newReturnStatement();

	public IASTLabelStatement newLabelStatement();

	public IASTCaseStatement newCaseStatement();

	public IASTDefaultStatement newDefaultStatement();

	public IASTSwitchStatement newSwitchStatment();

	public IASTIfStatement newIfStatement();

	public IASTTranslationUnit newTranslationUnit();

	public IASTFunctionDefinition newFunctionDefinition();

	public IASTProblemDeclaration newProblemDeclaration();

	public IASTProblemStatement newProblemStatement();

	public IASTProblemExpression newProblemExpression();

	public IASTProblem newProblem(int id, char[] arg, boolean warn, boolean error);

	public IASTComment newComment();

}
