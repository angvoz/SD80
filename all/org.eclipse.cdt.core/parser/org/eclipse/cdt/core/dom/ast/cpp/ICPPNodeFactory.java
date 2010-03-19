/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;
import org.eclipse.cdt.core.parser.IScanner;

/**
 * Factory for AST nodes for the C++ programming language.
 * 
 * @since 5.1
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPNodeFactory extends INodeFactory {
	
	/**
	 * @since 5.2
	 */
	public ICPPASTArrayDeclarator newArrayDeclarator(IASTName name);
	
	public ICPPASTArraySubscriptExpression newArraySubscriptExpression(IASTExpression arrayExpr, IASTExpression subscript);
	
	/**
	 * @since 5.2
	 */
	public ICPPASTArraySubscriptExpression newArraySubscriptExpression(IASTExpression arrayExpr, IASTInitializerClause subscript);
	
	public ICPPASTBaseSpecifier newBaseSpecifier(IASTName name, int visibility, boolean isVirtual);
	
	public ICPPASTBinaryExpression newBinaryExpression(int op, IASTExpression expr1, IASTExpression expr2);
	
	/**
	 * @since 5.2
	 */
	public ICPPASTBinaryExpression newBinaryExpression(int op, IASTExpression expr1, IASTInitializerClause expr2);

	public ICPPASTCastExpression newCastExpression(int operator, IASTTypeId typeId, IASTExpression operand);
	
	public ICPPASTCatchHandler newCatchHandler(IASTDeclaration decl, IASTStatement body);
	
	public ICPPASTCompositeTypeSpecifier newCompositeTypeSpecifier(int key, IASTName name);

	/**
	 * @deprecated Replaced by {@link #newConstructorChainInitializer(IASTName, IASTInitializer)}
	 */
	@Deprecated
	public ICPPASTConstructorChainInitializer newConstructorChainInitializer(IASTName memberInitializerId, IASTExpression initializerValue);
	
	/**
	 * @since 5.2
	 */
	public ICPPASTConstructorChainInitializer newConstructorChainInitializer(IASTName id, IASTInitializer initializer);

	/**
	 * @deprecated Replaced by {@link #newConstructorInitializer(IASTInitializerClause[])}.
	 */
	@Deprecated
	public ICPPASTConstructorInitializer newConstructorInitializer(IASTExpression exp);

	/**
	 * @since 5.2
	 */
	public ICPPASTConstructorInitializer newConstructorInitializer(IASTInitializerClause[] args);
	
	public ICPPASTConversionName newConversionName(IASTTypeId typeId);

	/**
	 * @since 5.2
	 */
	public ICPPASTDeclarator newDeclarator(IASTName name);

	public ICPPASTDeleteExpression newDeleteExpression(IASTExpression operand);
	
	public ICPPASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name);

	/**
	 * @since 5.2
	 */
	public ICPPASTEnumerationSpecifier newEnumerationSpecifier(boolean isScoped, IASTName name, ICPPASTDeclSpecifier baseType);

	public ICPPASTExplicitTemplateInstantiation newExplicitTemplateInstantiation(IASTDeclaration declaration);
	
	/**
	 * @deprecated Replaced by {@link #newExplicitTemplateInstantiation(IASTDeclaration)}.
	 */
	@Deprecated
	public org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation newExplicitTemplateInstantiationGPP(IASTDeclaration declaration);

	public ICPPASTExpressionList newExpressionList();

	/**
	 * @since 5.2
	 */
	public ICPPASTFieldDeclarator newFieldDeclarator(IASTName name, IASTExpression bitFieldSize);
	
	public ICPPASTFieldReference newFieldReference(IASTName name, IASTExpression owner);
	
	public ICPPASTForStatement newForStatement();
	
	public ICPPASTForStatement newForStatement(IASTStatement init, IASTDeclaration condition,
			IASTExpression iterationExpression, IASTStatement body);

	public ICPPASTForStatement newForStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body);
	
	/**
	 * @deprecated Replaced by {@link #newFunctionCallExpression(IASTExpression, IASTInitializerClause[])}.
	 */
	@Deprecated
	public ICPPASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr, IASTExpression argList);
	
	/**
	 * @since 5.2
	 */
	public ICPPASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr, IASTInitializerClause[] arguments);
	
	public ICPPASTFunctionDeclarator newFunctionDeclarator(IASTName name);
	
	public ICPPASTFunctionDefinition newFunctionDefinition(IASTDeclSpecifier declSpecifier,
			IASTFunctionDeclarator declarator, IASTStatement bodyStatement);
	
	public ICPPASTFunctionWithTryBlock newFunctionTryBlock(IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator,
			IASTStatement bodyStatement);
	
	public ICPPASTIfStatement newIfStatement();
	
	public ICPPASTIfStatement newIfStatement(IASTDeclaration condition, IASTStatement then, IASTStatement elseClause);
	
	public ICPPASTIfStatement newIfStatement(IASTExpression condition, IASTStatement then, IASTStatement elseClause);

	/**
	 * @since 5.2
	 */
	public ICPPASTInitializerList newInitializerList();

	public ICPPASTLinkageSpecification newLinkageSpecification(String literal);
	
	public ICPPASTLiteralExpression newLiteralExpression(int kind, String rep);

	public ICPPASTNamespaceAlias newNamespaceAlias(IASTName alias, IASTName qualifiedName);

	public ICPPASTNamespaceDefinition newNamespaceDefinition(IASTName name);
	
	/**
	 * @deprecated Replaced by {@link #newNewExpression(IASTInitializerClause[], IASTInitializer, IASTTypeId)}
	 */
	@Deprecated
	public ICPPASTNewExpression newNewExpression(IASTExpression placement, IASTExpression initializer, IASTTypeId typeId);
	
	/**
	 * @since 5.2
	 */
	public ICPPASTNewExpression newNewExpression(IASTInitializerClause[] placement, IASTInitializer initializer, IASTTypeId typeId);

	public ICPPASTOperatorName newOperatorName(char[] name);
	
	/**
	 * Creates a new pack expansion expression for the given pattern.
	 * @since 5.2
	 */
	public ICPPASTPackExpansionExpression newPackExpansionExpression(IASTExpression pattern);

	public ICPPASTParameterDeclaration newParameterDeclaration(IASTDeclSpecifier declSpec, IASTDeclarator declarator);

	public IGPPASTPointer newPointerGPP();
	
	public ICPPASTPointerToMember newPointerToMember(IASTName name);

	public IGPPASTPointerToMember newPointerToMemberGPP(IASTName name);

	public IASTProblemTypeId newProblemTypeId(IASTProblem problem);
	
	public ICPPASTQualifiedName newQualifiedName();
	
	/**
	 * @deprecated Replaced by {@link #newReferenceOperator(boolean)}.
	 */
	@Deprecated	public ICPPASTReferenceOperator newReferenceOperator();
	
	/**
	 * Creates an lvalue or rvalue reference operator.
	 * @since 5.2
	 */
	public ICPPASTReferenceOperator newReferenceOperator(boolean isRValueReference);
	
	/**
	 * @since 5.2
	 */
	public IASTReturnStatement newReturnStatement(IASTInitializerClause retValue);
	
	public ICPPASTSimpleDeclSpecifier newSimpleDeclSpecifier();
	
	/**
	 * @deprecated Replaced by {@link #newSimpleDeclSpecifier()}
	 */
	@Deprecated
	public org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier newSimpleDeclSpecifierGPP();

	/**
	 * @deprecated Replaced by {@link #newSimpleTypeConstructorExpression(ICPPASTDeclSpecifier, IASTInitializer)}
     */
	@Deprecated
	public ICPPASTSimpleTypeConstructorExpression newSimpleTypeConstructorExpression(int type, IASTExpression expression);

	/**
	 * @since 5.2
	 */
	public ICPPASTSimpleTypeConstructorExpression newSimpleTypeConstructorExpression(ICPPASTDeclSpecifier declSpec, IASTInitializer initializer);
	
	public ICPPASTSimpleTypeTemplateParameter newSimpleTypeTemplateParameter(int type, IASTName name, IASTTypeId typeId);
	
	/**
	 * Creates a new static assertion declaration with the given condition and message.
	 * @since 5.2
	 */
	public ICPPASTStaticAssertDeclaration newStaticAssertion(IASTExpression condition, ICPPASTLiteralExpression message);

	public ICPPASTSwitchStatement newSwitchStatement();
	
	public ICPPASTSwitchStatement newSwitchStatement(IASTDeclaration controller, IASTStatement body);

	public ICPPASTSwitchStatement newSwitchStatement(IASTExpression controlloer, IASTStatement body);

	public ICPPASTTemplateDeclaration newTemplateDeclaration(IASTDeclaration declaration);

	public ICPPASTTemplatedTypeTemplateParameter newTemplatedTypeTemplateParameter(IASTName name, IASTExpression defaultValue);
	
	public ICPPASTTemplateId newTemplateId(IASTName templateName);
	
	public ICPPASTTemplateSpecialization newTemplateSpecialization(IASTDeclaration declaration);
	
	/**
	 * @deprecated Replaced by {@link #newTranslationUnit(IScanner)}.
	 */
	@Deprecated
	public ICPPASTTranslationUnit newTranslationUnit();

	/**
	 * Creates a new translation unit that cooperates with the given scanner in order
	 * to track macro-expansions and location information.
	 * @scanner the preprocessor the translation unit interacts with.
	 * @since 5.2
	 */
	public ICPPASTTranslationUnit newTranslationUnit(IScanner scanner);

	public ICPPASTTryBlockStatement newTryBlockStatement(IASTStatement body);

	public ICPPASTNamedTypeSpecifier newTypedefNameSpecifier(IASTName name);
	
	/**
	 * @since 5.2
	 */
	public ICPPASTTypeId newTypeId(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator);
	
	public ICPPASTTypeIdExpression newTypeIdExpression(int operator, IASTTypeId typeId);

	/**
	 * @deprecated Replaced by {@link #newSimpleTypeConstructorExpression(ICPPASTDeclSpecifier, IASTInitializer)}
	 */
	@Deprecated
	public ICPPASTTypenameExpression newTypenameExpression(IASTName qualifiedName, IASTExpression expr, boolean isTemplate);
	
	public ICPPASTUnaryExpression newUnaryExpression(int operator, IASTExpression operand);

	public ICPPASTUsingDeclaration newUsingDeclaration(IASTName name);

	public ICPPASTUsingDirective newUsingDirective(IASTName name);

	public ICPPASTVisibilityLabel newVisibilityLabel(int visibility);

	public ICPPASTWhileStatement newWhileStatement();

	public ICPPASTWhileStatement newWhileStatement(IASTDeclaration condition, IASTStatement body);
	
	public ICPPASTWhileStatement newWhileStatement(IASTExpression condition, IASTStatement body);

}
