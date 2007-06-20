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
package org.eclipse.cdt.core.dom.parser.c99;

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
import org.eclipse.cdt.core.dom.c99.IASTNodeFactory;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTArrayDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTArrayDesignator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBreakStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCaseStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCastExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTConditionalExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTContinueStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDefaultStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDesignatedInitializer;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDoStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTEnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionList;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFieldDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFieldDesignator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTGotoStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTInitializerList;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTLabelStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTModifiedArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTName;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTNullStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTProblem;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTProblemDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTProblemExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTProblemStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeIdInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypedefNameSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.parser.c99.ASTCompletionNode;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99ASTComment;

/**
 * Methods that create new AST nodes.
 * These can be overridden in subclasses to chage the 
 * implementations of the nodes.
 * 
 */
public class C99ASTNodeFactory implements IASTNodeFactory {

	public IASTName newName(char[] name) {
		return new CASTName(name);
	}
	
	public IASTName newName() {
		return new CASTName();
	}
	
	/**
	 * TODO: this should return IASTCompletionNode
	 */
	public ASTCompletionNode newCompletionNode(String prefix) {
		return new ASTCompletionNode((prefix == null || prefix.length() == 0) ? null : prefix);
	}
	

	public IASTComment newComment() {
		return new C99ASTComment();
	}
	
	public IASTLiteralExpression newLiteralExpression() {
		return new CASTLiteralExpression();
	}
	
	
	public IASTIdExpression newIdExpression() {
		return new CASTIdExpression();
	}
	
	public IASTBinaryExpression newBinaryExpression() {
		return new CASTBinaryExpression();
	}
	
	public IASTConditionalExpression newConditionalExpession() {
		return new CASTConditionalExpression();
	}
	
	public IASTArraySubscriptExpression newArraySubscriptExpression() {
		return new CASTArraySubscriptExpression();
	}
	
	public IASTFunctionCallExpression newFunctionCallExpression() {
		return new CASTFunctionCallExpression();
	}
	
	public IASTExpressionList newExpressionList() {
		return new CASTExpressionList();
	}
	
	public IASTFieldReference newFieldReference() {
		return new CASTFieldReference();
	}
	
	public IASTUnaryExpression newUnaryExpression() {
		return new CASTUnaryExpression();
	}
	
	public IASTTypeIdExpression newTypeIdExpression() {
		return new CASTTypeIdExpression();
	}
	
	public ICASTTypeIdInitializerExpression newCTypeIdInitializerExpression() {
		return new CASTTypeIdInitializerExpression();
	}
	
	public IASTCastExpression newCastExpression() {
		return new CASTCastExpression();
	}
	
	public IASTTypeId newTypeId() {
		return new CASTTypeId();
	}
	
	public IASTDeclarator newDeclarator() {
		return new CASTDeclarator();
	}
	
	public IASTArrayDeclarator newArrayDeclarator() {
		return new CASTArrayDeclarator();
	}
	
	public ICASTArrayModifier newCArrayModifier() {
		return new CASTModifiedArrayModifier();
	}
	
	public IASTArrayModifier newArrayModifier() {
        return new CASTArrayModifier();
    }
	
	public IASTStandardFunctionDeclarator newFunctionDeclarator() {
		return new CASTFunctionDeclarator();
	}
	
	public ICASTKnRFunctionDeclarator newCKnRFunctionDeclarator() {
		return new CASTKnRFunctionDeclarator();
	}
	
	public ICASTPointer newCPointer() {
		return new CASTPointer();
	}
	
	public IASTParameterDeclaration newParameterDeclaration() {
		return new CASTParameterDeclaration();
	}
	
	public IASTInitializerExpression newInitializerExpression() {
		return new CASTInitializerExpression();
	}
	
	public IASTInitializerList newInitializerList() {
		return new CASTInitializerList();
	}
	
	public ICASTDesignatedInitializer newCDesignatedInitializer() {
		return new CASTDesignatedInitializer();
	}
	
	public ICASTArrayDesignator newCArrayDesignator() {
		return new CASTArrayDesignator();
	}
	
	public ICASTFieldDesignator newCFieldDesignator() {
		return new CASTFieldDesignator();
	}
	
	public ICASTSimpleDeclSpecifier newCSimpleDeclSpecifier() {
		return new CASTSimpleDeclSpecifier();
	}
	
	public ICASTTypedefNameSpecifier newCTypedefNameSpecifier() {
		return new CASTTypedefNameSpecifier();
	}
	
	public IASTSimpleDeclaration newSimpleDeclaration() {
		return new CASTSimpleDeclaration();
	}
	
	public IASTFieldDeclarator newFieldDeclarator() {
		return new CASTFieldDeclarator();
	}
	
	public ICASTCompositeTypeSpecifier newCCompositeTypeSpecifier() {
		return new CASTCompositeTypeSpecifier();
	}
	
	public ICASTElaboratedTypeSpecifier newCElaboratedTypeSpecifier() {
		return new CASTElaboratedTypeSpecifier();
	}
	
	public ICASTEnumerationSpecifier newCEnumerationSpecifier() {
		return new CASTEnumerationSpecifier();
	}
	
	public IASTEnumerator newEnumerator() {
		return new CASTEnumerator();
	}
	
	public IASTCompoundStatement newCompoundStatement() {
		return new CASTCompoundStatement();
	}
	
	public IASTForStatement newForStatement() {
		return new CASTForStatement();
	}
	
	public IASTExpressionStatement newExpressionStatement() {
		return new CASTExpressionStatement();
	}
	
	public IASTDeclarationStatement newDeclarationStatement() {
		return new CASTDeclarationStatement();
	}
	
	public IASTNullStatement newNullStatement() {
		return new CASTNullStatement();
	}
	
	public IASTWhileStatement newWhileStatement() {
		return new CASTWhileStatement();
	}
	
	public IASTDoStatement newDoStatement() {
		return new CASTDoStatement();
	}
	
	public IASTGotoStatement newGotoStatement() {
		return new CASTGotoStatement();
	}
	
	public IASTContinueStatement newContinueStatement() {
		return new CASTContinueStatement();
	}
	
	public IASTBreakStatement newBreakStatement() {
		return new CASTBreakStatement();
	}
	
	public IASTReturnStatement newReturnStatement() {
		return new CASTReturnStatement();
	}
	
	public IASTLabelStatement newLabelStatement() {
		return new CASTLabelStatement();
	}
	
	public IASTCaseStatement newCaseStatement() {
		return new CASTCaseStatement();
	}
	
	public IASTDefaultStatement newDefaultStatement() {
		return new CASTDefaultStatement();
	}
	
	public IASTSwitchStatement newSwitchStatment() {
		return new CASTSwitchStatement();
	}
	
	public IASTIfStatement newIfStatement() {
		return new CASTIfStatement();
	}
	
	public IASTTranslationUnit newTranslationUnit() {
		return new CASTTranslationUnit();
	}
	
	public IASTFunctionDefinition newFunctionDefinition() {
		return new CASTFunctionDefinition();
	}
	
	public IASTProblemDeclaration newProblemDeclaration() {
		return new CASTProblemDeclaration();
	}
	
	public IASTProblemStatement newProblemStatement() {
		return new CASTProblemStatement();
	}
	
	public IASTProblemExpression newProblemExpression() {
		return new CASTProblemExpression();
	}
	
	public IASTProblem newProblem(int id, char[] arg, boolean warn, boolean error) {
		return new CASTProblem(id, arg, warn, error);
	}

	public IASTAmbiguousExpression newAmbiguousExpression() {
		return new CASTAmbiguousExpression();
	}

	public IASTAmbiguousStatement newAmbiguousStatement() {
		return new CASTAmbiguousStatement();
	}
	
} 

















