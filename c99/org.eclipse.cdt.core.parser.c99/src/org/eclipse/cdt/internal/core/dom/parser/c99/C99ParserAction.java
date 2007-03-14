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

import java.util.Iterator;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
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
import org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver;


/**
 * Semantic actions called by the parser to build an AST.
 * 
 * @author Mike Kucera
 */
public class C99ParserAction {

	private static final char[] EMPTY_CHAR_ARRAY = {};
	
	// Stack that holds the intermediate nodes as the AST is being built
	private final ASTStack astStack = new ASTStack();
	
	private final C99Parser parser;
	private boolean encounteredRecoverableProblem = false;
	

	/**
	 * The LPG runtime will automatically calculate the offset and length
	 * of each grammar rule when it is reduced.
	 */
	protected int ruleOffset, ruleLength;
	
	
	private ILocationResolver resolver;
	private IIndex index;
	
	
	public C99ParserAction(C99Parser parser) {
		this.parser = parser;
	}
	
	
	
	public void setResolver(ILocationResolver resolver) {
		this.resolver = resolver;
	}



	public void setIndex(IIndex index) {
		this.index = index;
	}



	/**
	 * Returns an AST after a successful parse, null otherwise.
	 */
	public IASTTranslationUnit getAST() {
		if(astStack.isEmpty())
			return null;
		
		CASTTranslationUnit ast = (CASTTranslationUnit) astStack.peek();
		if(index != null)
			ast.setIndex(index);
		if(resolver != null)
			ast.setLocationResolver(resolver);
		
		return ast;
	}
	
	
	/**
	 * Returns true iff a syntax error was encountered during the parse.
	 */
	public boolean encounteredError() {
		// if the astStack is empty then an unrecoverable syntax error was encountered
		return encounteredRecoverableProblem || astStack.isEmpty();
	}
	
	
	/**
	 * Method that is called by the special <openscope> production
	 * in order to create a new scope in the AST stack.
	 * 
	 */
	protected void openASTScope() {
		astStack.openASTScope();
	}
	
	
	
	/**
	 * Creates a IASTName node from an identifier token.
	 * 
	 */
	private static CASTName createName(IToken token) {
		CASTName name = new CASTName(token.toString().toCharArray());
		name.setOffsetAndLength(offset(token), length(token)); 
		return name;
	}
	
	
	
	private static int offset(IToken token) {
		return token.getStartOffset();
	}
	
	private static int offset(IASTNode node) {
		return ((ASTNode)node).getOffset();
	}
	
	private static int length(IToken token) {
		return endOffset(token) - offset(token);
	}
	
	private static int length(IASTNode node) {
		return ((ASTNode)node).getLength();
	}
	
	private static int endOffset(IASTNode node) {
		return offset(node) + length(node);
	}
	
	private static int endOffset(IToken token) {
		return token.getEndOffset() + 1;
		//return token.getTokenIndex();
	}
	
	/********************************************************************
	 * Start of semantic actions.
	 ********************************************************************/
	
	
	
	/**
	 * Special action that is always called before every consume action.
	 * Allows AST node offsets to be calculated automatically.
	 */
	protected void beforeConsume() {
		ruleOffset = parser.getLeftIToken().getStartOffset();
		ruleLength = parser.getRightIToken().getEndOffset() + 1 - ruleOffset;
	}
	
	
	/**
	 * Consumes a name from an identifer.
	 * Used by several grammar rules.
	 */
	protected void consumeName() {
		IASTName name = createName( parser.getRightIToken() );
		astStack.push(name);
	}
	
	
	/**
	 * Gets the current token and places it on the stack for later consumption.
	 */
	protected void consumeToken() {
		astStack.push(parser.getRightIToken());
	}
	
	
	/**
	 * constant ::= 'integer' | 'floating' | 'charconst' | 'stringlit'
	 * 
	 * @param kind One of the kind flags from IASTLiteralExpression
	 * @see IASTLiteralExpression
	 */
	protected void consumeExpressionConstant(int kind) {
		CASTLiteralExpression expr = new CASTLiteralExpression();
		IToken token = parser.getRightIToken();
		expr.setKind(kind);
		expr.setValue(token.toString());
		expr.setOffsetAndLength(offset(token), length(token));
		astStack.push(expr);
	}
	
	
	/**
	 * primary_expression ::= 'identifier'
	 */
	protected void consumeExpressionID() {
		CASTIdExpression expr = new CASTIdExpression();
		CASTName name = createName(parser.getRightIToken());
		expr.setName(name);
		name.setParent(expr);
		name.setPropertyInParent(IASTIdExpression.ID_NAME);
        expr.setOffsetAndLength(name);
        astStack.push(expr);
	}
	
	
	/**
	 * multiplicative_expression ::= multiplicative_expression '*' cast_expression
	 * multiplicative_expression ::= multiplicative_expression '/' cast_expression
	 * multiplicative_expression ::= multiplicative_expression '%' cast_expression
	 * 
	 * additive_expression ::= additive_expression '+' multiplicative_expression
	 * additive_expression ::= additive_expression '_' multiplicative_expression
	 * 
	 * shift_expression ::= shift_expression '<<' additive_expression
	 * shift_expression ::= shift_expression '>>' additive_expression
	 * 
	 * relational_expression ::= relational_expression '<' shift_expression
	 * relational_expression ::= relational_expression '>' shift_expression
	 * relational_expression ::= relational_expression '<=' shift_expression
	 * relational_expression ::= relational_expression '>=' shift_expression
	 * 
	 * equality_expression ::= equality_expression '==' relational_expression
	 * equality_expression ::= equality_expression '!=' relational_expression
	 * 
	 * AND_expression ::= AND_expression '&' equality_expression
	 * 
	 * exclusive_OR_expression ::= exclusive_OR_expression '^' AND_expression
	 * 
	 * inclusive_OR_expression ::= inclusive_OR_expression '|' exclusive_OR_expression
	 * 
	 * logical_AND_expression ::= logical_AND_expression '&&' inclusive_OR_expression
	 * 
	 * logical_OR_expression ::= logical_OR_expression '||' logical_AND_expression
	 * 
	 * assignment_expression ::= unary_expression '='   assignment_expression
	 * assignment_expression ::= unary_expression '*='  assignment_expression
	 * assignment_expression ::= unary_expression '/='  assignment_expression
	 * assignment_expression ::= unary_expression '%='  assignment_expression
	 * assignment_expression ::= unary_expression '+='  assignment_expression
	 * assignment_expression ::= unary_expression '_='  assignment_expression
	 * assignment_expression ::= unary_expression '<<=' assignment_expression
	 * assignment_expression ::= unary_expression '>>=' assignment_expression
	 * assignment_expression ::= unary_expression '&='  assignment_expression
	 * assignment_expression ::= unary_expression '^='  assignment_expression
	 * assignment_expression ::= unary_expression '|='  assignment_expression
	 * 
	 * 
	 * @param op Field from IASTBinaryExpression
	 */
	protected void consumeExpressionBinaryOperator(int op) {
		IASTExpression expr2 = (IASTExpression) astStack.pop();
		IASTExpression expr1 = (IASTExpression) astStack.pop();
		
		CASTBinaryExpression binExpr = new CASTBinaryExpression();
		binExpr.setOperator(op);
		
		binExpr.setOperand1(expr1);
		expr1.setParent(binExpr);
		expr1.setPropertyInParent(IASTBinaryExpression.OPERAND_ONE);
		
		binExpr.setOperand2(expr2);
		expr2.setParent(binExpr);
		expr2.setPropertyInParent(IASTBinaryExpression.OPERAND_TWO);
		
		binExpr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(binExpr);
	}
	
	
	/**
	 * conditional_expression ::= logical_OR_expression '?' expression ':' conditional_expression
	 */
	protected void consumeExpressionConditional() {
		IASTExpression expr3 = (IASTExpression) astStack.pop();
		IASTExpression expr2 = (IASTExpression) astStack.pop();
		IASTExpression expr1 = (IASTExpression) astStack.pop();
		
		CASTConditionalExpression condExpr = new CASTConditionalExpression();
		
		condExpr.setLogicalConditionExpression(expr1);
		expr1.setParent(condExpr);
		expr1.setPropertyInParent(IASTConditionalExpression.LOGICAL_CONDITION);
		
		condExpr.setPositiveResultExpression(expr2);
		expr2.setParent(condExpr);
		expr2.setPropertyInParent(IASTConditionalExpression.POSITIVE_RESULT);
		
		condExpr.setNegativeResultExpression(expr3);
		expr3.setParent(condExpr);
		expr3.setPropertyInParent(IASTConditionalExpression.NEGATIVE_RESULT);
		
		condExpr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(condExpr);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '[' expression ']'
	 */
	protected void consumeExpressionArraySubscript() {
		CASTArraySubscriptExpression expr = new CASTArraySubscriptExpression();
		
		IASTExpression subscript = (IASTExpression) astStack.pop();
		IASTExpression arrayExpr = (IASTExpression) astStack.pop();
		
		expr.setArrayExpression(arrayExpr);
		arrayExpr.setParent(expr);
		arrayExpr.setPropertyInParent(IASTArraySubscriptExpression.ARRAY);
		
		expr.setSubscriptExpression(subscript);
		subscript.setParent(expr);
		arrayExpr.setPropertyInParent(IASTArraySubscriptExpression.SUBSCRIPT);
		
		expr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(expr);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '(' argument_expression_list ')'
	 * postfix_expression ::= postfix_expression '(' ')'
	 */
	protected void consumeExpressionFunctionCall(boolean hasArgs) {
		CASTFunctionCallExpression expr = new CASTFunctionCallExpression();
		
		if(hasArgs) {
			CASTExpressionList argList = (CASTExpressionList) astStack.pop();
			
			expr.setParameterExpression(argList);
			argList.setParent(expr);
			argList.setPropertyInParent(IASTFunctionCallExpression.PARAMETERS);
		}
		
		IASTExpression idExpr  = (IASTExpression) astStack.pop();
		
		expr.setFunctionNameExpression(idExpr);
		idExpr.setParent(expr);
		idExpr.setPropertyInParent(IASTFunctionCallExpression.FUNCTION_NAME);

		expr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(expr);
	}
	
	
	/**
	 * argument_expression_list
     *     ::= assignment_expression  -- base case
     *       | argument_expression_list ',' assignment_expression
	 * 
	 */
	protected void consumeExpressionArgumentExpressionList(boolean baseCase) {
		IASTExpression argumentExpression = (IASTExpression) astStack.pop();
		
		CASTExpressionList argList;
		if(baseCase) {
			argList = new CASTExpressionList();
			argList.setOffset(ruleOffset);
			astStack.push(argList);
		}
		else {
			argList = (CASTExpressionList) astStack.peek();
		}
		
		argList.addExpression(argumentExpression);
		argumentExpression.setParent(argList);
		argumentExpression.setPropertyInParent(IASTExpressionList.NESTED_EXPRESSION);
		argList.setLength(ruleLength);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '.' 'identifier'
	 * postfix_expression ::= postfix_expression '->' 'identifier'
	 */
	protected void consumeExpressionFieldReference(boolean isPointerDereference) {
		IASTExpression idExpression = (IASTExpression) astStack.pop();
		
		CASTFieldReference expr = new CASTFieldReference();
		expr.setIsPointerDereference(isPointerDereference);
		
		IToken identifier = parser.getRightIToken();
		IASTName name = createName(identifier);
		
		expr.setFieldName(name);
		name.setParent(expr);
		name.setPropertyInParent(IASTFieldReference.FIELD_NAME);
		
		expr.setFieldOwner(idExpression);
		idExpression.setParent(expr);
		idExpression.setPropertyInParent(IASTFieldReference.FIELD_OWNER);
		
		expr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(expr);
	}
	
	
	
	/**
	 * postfix_expression ::= postfix_expression '++'
	 * postfix_expression ::= postfix_expression '__'
	 * 
	 * unary_expression ::= '++' unary_expression
	 * unary_expression ::= '__' unary_expression
	 * unary_expression ::= '&' cast_expression
	 * unary_expression ::= '*' cast_expression
	 * unary_expression ::= '+' cast_expression
	 * unary_expression ::= '_' cast_expression
	 * unary_expression ::= '~' cast_expression
	 * unary_expression ::= '!' cast_expression
	 * unary_expression ::= 'sizeof' unary_expression
	 * 
	 * @param operator From IASTUnaryExpression
	 */
	protected void consumeExpressionUnaryOperator(int operator) {
		IASTExpression operand = (IASTExpression) astStack.pop();
		
		CASTUnaryExpression expr = new CASTUnaryExpression();
		expr.setOperator(operator);
		
		expr.setOperand(operand);
		operand.setParent(expr);
		operand.setPropertyInParent(IASTUnaryExpression.OPERAND);
		
		expr.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(expr);
	}
	
	
	/**
	 * unary_operation ::= 'sizeof' '(' type_name ')'
	 * 
	 * @see consumeExpressionUnaryOperator For the other use of sizeof
	 */
	protected void consumeExpressionUnarySizeofTypeName() {
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		CASTTypeIdExpression expr = new CASTTypeIdExpression();
		
		expr.setTypeId(typeId);
		typeId.setParent(expr);
		typeId.setPropertyInParent(IASTTypeIdExpression.TYPE_ID);
		
		expr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(expr);
	}
	
	
	
	/**
	 * postfix_expression ::= '(' type_name ')' '{' <openscope> initializer_list '}'
	 * postfix_expression ::= '(' type_name ')' '{' <openscope> initializer_list ',' '}'            
	 */
	protected void consumeExpressionTypeIdInitializer() {
		consumeInitializerList(); // closes the scope
		
		IASTInitializerList list = (IASTInitializerList) astStack.pop();
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		
		CASTTypeIdInitializerExpression expr = new CASTTypeIdInitializerExpression();
		
		expr.setInitializer(list);
		list.setParent(expr);
		list.setPropertyInParent(ICASTTypeIdInitializerExpression.INITIALIZER);
		
		expr.setTypeId(typeId);
		typeId.setParent(expr);
		typeId.setPropertyInParent(ICASTTypeIdInitializerExpression.TYPE_ID);
	
		expr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(expr);
	}
	
	
	
	/**
	 * cast_expression ::= '(' type_name ')' cast_expression
	 */
	protected void consumeExpressionCast() {
		CASTCastExpression expr = new CASTCastExpression();
		expr.setOperator(IASTCastExpression.op_cast);
		
		IASTExpression operand = (IASTExpression) astStack.pop();
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		
		expr.setTypeId(typeId);
		typeId.setParent(expr);
		typeId.setPropertyInParent(IASTCastExpression.TYPE_ID);
		
		expr.setOperand(operand);
		operand.setParent(expr);
		operand.setPropertyInParent(IASTCastExpression.OPERAND);
		
		expr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(expr);
	}
	
	
	/**
	 * primary_expression ::= '(' expression ')'
	 * 
	 * TODO: should bracketed expressions cause a new node in the AST? whats the point?
	 */
	protected void consumeExpressionBracketed() {
		CASTUnaryExpression expr = new CASTUnaryExpression();
		expr.setOperator(IASTUnaryExpression.op_bracketedPrimary);
		
		IASTExpression operand = (IASTExpression) astStack.pop();
		
		expr.setOperand(operand);
		operand.setParent(expr);
        operand.setPropertyInParent(IASTUnaryExpression.OPERAND);

        expr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(expr);
	}
	
	
	
	/**
	 * expression ::= expression_list
	 * 
	 * In the case that an expression list consists of a single expression
	 * then discard the list.
	 */
	protected void consumeExpression() {
		IASTExpressionList exprList = (IASTExpressionList) astStack.pop();
		IASTExpression[] expressions = exprList.getExpressions();
		if(expressions.length == 1) {
			astStack.push(expressions[0]);
		}
		else {
			astStack.push(exprList);
		}
	}
	
	
	
	/**
	 * expression_list
     *     ::= assignment_expression
     *       | expression_list ',' assignment_expression 
	 */
	protected void consumeExpressionList(boolean baseCase) {
		IASTExpression expr = (IASTExpression) astStack.pop();
		
		CASTExpressionList exprList;
		if(baseCase) {
			exprList = new CASTExpressionList();
			exprList.setOffset(ruleOffset);
			astStack.push(exprList);
		}
		else {
			exprList = (CASTExpressionList) astStack.peek();
		}
		
		exprList.addExpression(expr);
		exprList.setLength(ruleLength);
		expr.setParent(exprList);
		expr.setPropertyInParent(IASTExpressionList.NESTED_EXPRESSION);
	}
	
	
	
	/**
	 * Sets a token specifier.
	 */
	private void setSpecifier(ICASTDeclSpecifier node, IToken token) {
		switch(token.getKind()){
			// storage_class_specifier
			case C99Parsersym.TK_typedef: 
				node.setStorageClass(IASTDeclSpecifier.sc_typedef); 
				return;
			case C99Parsersym.TK_extern: 
				node.setStorageClass(IASTDeclSpecifier.sc_extern); 
				return;
			case C99Parsersym.TK_static:
				node.setStorageClass(IASTDeclSpecifier.sc_static);
				return;
			case C99Parsersym.TK_auto:
				node.setStorageClass(IASTDeclSpecifier.sc_auto);
				return;
			case C99Parsersym.TK_register:
				node.setStorageClass(IASTDeclSpecifier.sc_register);
				return;
			// function_specifier
			case C99Parsersym.TK_inline:
				node.setInline(true);
				return;
			// type_qualifier
			case C99Parsersym.TK_const:
				node.setConst(true);
				return;
			case C99Parsersym.TK_restrict:
				node.setRestrict(true);
				return;
			case C99Parsersym.TK_volatile:
				node.setVolatile(true);
				return;
		}
		
		// type_specifier
		if(node instanceof ICASTSimpleDeclSpecifier)
		{
			ICASTSimpleDeclSpecifier n = (ICASTSimpleDeclSpecifier) node;
			switch(token.getKind()) {
				case C99Parsersym.TK_void:
					n.setType(IASTSimpleDeclSpecifier.t_void);
					break;
				case C99Parsersym.TK_char:
					n.setType(IASTSimpleDeclSpecifier.t_char);
					break;
				case C99Parsersym.TK_short:
					n.setShort(true);
					break;
				case C99Parsersym.TK_int:
					n.setType(IASTSimpleDeclSpecifier.t_int);
					break;
				case C99Parsersym.TK_long:
					boolean isLong = n.isLong();
					n.setLongLong(isLong);
					n.setLong(!isLong);
					break;
				case C99Parsersym.TK_float:
					n.setType(IASTSimpleDeclSpecifier.t_float);
					break;
				case C99Parsersym.TK_double:
					n.setType(IASTSimpleDeclSpecifier.t_double);
					break;
				case C99Parsersym.TK_signed:
					n.setSigned(true);
					break;
				case C99Parsersym.TK_unsigned:
					n.setUnsigned(true);
					break;
				case C99Parsersym.TK__Bool:
					n.setType(ICASTSimpleDeclSpecifier.t_Bool);
					break;
				case C99Parsersym.TK__Complex:
					n.setComplex(true);
					break;
				default:
					return;
			}
			//declSpecStack.setEncounteredType(true);
		}
	}
	
	
	
	/**
	 * type_name ::= specifier_qualifier_list
     *             | specifier_qualifier_list abstract_declarator
	 */
	protected void consumeTypeId(boolean hasDeclarator) {
		CASTTypeId typeId = new CASTTypeId();
		
		if(hasDeclarator) {
			IASTDeclarator declarator = (IASTDeclarator) astStack.pop();
			typeId.setAbstractDeclarator(declarator);
			declarator.setParent(typeId);
			declarator.setPropertyInParent(IASTTypeId.ABSTRACT_DECLARATOR);
		}
		
		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop();
		
		typeId.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(typeId);
		declSpecifier.setPropertyInParent(IASTTypeId.DECL_SPECIFIER);
		
		typeId.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(typeId);
	}
	

	
	/**
	 * declarator ::= <openscope> pointer direct_declarator
     *              
     * abstract_declarator  -- a declarator that does not include an identifier
     *     ::= <openscope> pointer
     *       | <openscope> pointer direct_abstract_declarator 
	 */
	protected void consumeDeclaratorWithPointer(boolean hasDeclarator) {
		CASTDeclarator decl;
		if(hasDeclarator) {	
			decl = (CASTDeclarator)astStack.pop();
		}
		else {
			decl = new CASTDeclarator();
			IASTName name = new CASTName();
			decl.setName(name);
			name.setParent(decl);
			name.setPropertyInParent(IASTDeclarator.DECLARATOR_NAME);
		}
		
		// add all the pointers to the declarator
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			ICASTPointer pointer = (ICASTPointer) iter.next();
			decl.addPointerOperator(pointer);
			pointer.setParent(decl);
			pointer.setPropertyInParent(IASTDeclarator.POINTER_OPERATOR);
			
		}
		astStack.closeASTScope();

		decl.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(decl);
	}
	
	
	
	/**
	 * Used by rules of the form:  direct_declarator ::= direct_declarator '[' < something > ']'
	 * Consumes the direct_declarator part, the array modifier (the square bracket part) must be provided.
	 * Returns true if there is no problem.
	 * There will be an IASTArrayDeclarator on the stack if this method returns true.
	 *  
	 * __ 5 possibilities
     *    __ identifier
     *       __ create new array declarator
     *    __ nested declarator
     *       __ create new array declarator
     *    __ array declarator
     *       __ add this modifier to existing declarator
     *    __ function declarator
     *       __ problem
     *    __ problem
     *       __ problem
	 */
	private void consumeDeclaratorArray(IASTArrayModifier arrayModifier) {
		ASTNode node = (ASTNode) astStack.pop();
		
		// Its a nested declarator so create an new ArrayDeclarator
		if(node.getPropertyInParent() == IASTDeclarator.NESTED_DECLARATOR) {
			CASTArrayDeclarator declarator = new CASTArrayDeclarator();
			
			IASTName name = new CASTName();
			declarator.setName(name);
			name.setParent(declarator);
			name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
			
			CASTDeclarator nested = (CASTDeclarator) node;
			declarator.setNestedDeclarator(nested);
			nested.setParent(declarator);
			//nested.setPropertyInParent(IASTArrayDeclarator.NESTED_DECLARATOR);
			
			int offset = nested.getOffset();
			int length = endOffset(arrayModifier) - offset;
			declarator.setOffsetAndLength(offset, length);
			
			addArrayModifier(declarator, arrayModifier);
			astStack.push(declarator);
		}
		// There is already an array declarator so just add the modifier to it
		else if(node instanceof IASTArrayDeclarator) {
			CASTArrayDeclarator decl = (CASTArrayDeclarator) node;
			decl.setLength(endOffset(arrayModifier) - decl.getOffset());
			
			addArrayModifier(decl, arrayModifier);
			astStack.push(decl);
		}
		// The declarator is an identifier so create a new array declarator
		else if(node instanceof CASTDeclarator) {
			CASTArrayDeclarator decl = new CASTArrayDeclarator();
			
			CASTName name = (CASTName)((CASTDeclarator)node).getName();
			decl.setName(name);
			name.setParent(decl);
			name.setPropertyInParent(IASTArrayDeclarator.DECLARATOR_NAME);
		
			int offset = name.getOffset();
			int length = endOffset(arrayModifier) - offset;
			decl.setOffsetAndLength(offset, length);
			
			addArrayModifier(decl, arrayModifier);
			astStack.push(decl);
		}
		else {
			astStack.push(new CASTProblemDeclaration());
			encounteredRecoverableProblem = true;
		}
	}
	
	
	private void addArrayModifier(IASTArrayDeclarator decl, IASTArrayModifier modifier) {
		decl.addArrayModifier(modifier);
		modifier.setParent(decl);
		modifier.setPropertyInParent(IASTArrayDeclarator.ARRAY_MODIFIER);
	}
	
	
	/**
	 * type_qualifier ::= const | restrict | volatile
	 */
	private void collectArrayModifierTypeQualifiers(CASTModifiedArrayModifier arrayModifier) {		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IToken token = (IToken) iter.next();
			
			switch(token.getKind()) {
				case C99Parsersym.TK_const:
					arrayModifier.setConst(true);
					break;
				case C99Parsersym.TK_restrict:
					arrayModifier.setRestrict(true);
					break;
				case C99Parsersym.TK_volatile:
					arrayModifier.setVolatile(true);
					break;
			}
		}
		astStack.closeASTScope();
	}
	
	
	/**
	 *  array_modifier 
     *      ::= '[' <openscope> type_qualifier_list ']'
     *        | '[' <openscope> type_qualifier_list assignment_expression ']'
     *        | '[' 'static' assignment_expression ']'
     *        | '[' 'static' <openscope> type_qualifier_list assignment_expression ']'
     *        | '[' <openscope> type_qualifier_list 'static' assignment_expression ']'
     *        | '[' '*' ']'
     *        | '[' <openscope> type_qualifier_list '*' ']'
     *        
     * The main reason to separate array_modifier into its own rule is to
     * make calculating the offset and length much easier.
	 */
	protected void consumeDirectDeclaratorModifiedArrayModifier(boolean isStatic, 
			 boolean isVarSized, boolean hasTypeQualifierList, boolean hasAssignmentExpr) {
		
		assert isStatic || isVarSized || hasTypeQualifierList;
		
		CASTModifiedArrayModifier arrayModifier = new CASTModifiedArrayModifier();
		
		// consume all the stuff between the square brackets into an array modifier
		arrayModifier.setStatic(isStatic);
		arrayModifier.setVariableSized(isVarSized);
		
		if(hasAssignmentExpr) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			arrayModifier.setConstantExpression(expr);
			expr.setParent(arrayModifier);
			expr.setPropertyInParent(ICASTArrayModifier.CONSTANT_EXPRESSION);
		}
		
		if(hasTypeQualifierList) {
			collectArrayModifierTypeQualifiers(arrayModifier);
		}

		arrayModifier.setOffsetAndLength(ruleOffset, ruleLength); // snap!
		astStack.push(arrayModifier);
	}
	
	
	/**
	 *  array_modifier 
	 *      ::= '[' ']' 
     *        | '[' assignment_expression ']'
     */        
	protected void consumeDirectDeclaratorArrayModifier(boolean hasAssignmentExpr) {
		CASTArrayModifier arrayModifier = new CASTArrayModifier();
		
		if(hasAssignmentExpr) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			arrayModifier.setConstantExpression(expr);
			expr.setParent(arrayModifier);
			expr.setPropertyInParent(ICASTArrayModifier.CONSTANT_EXPRESSION);
		}
		
		arrayModifier.setOffsetAndLength(ruleOffset, ruleLength); // snap!
		astStack.push(arrayModifier);
	}
	
	
	/**
	 * direct_declarator ::= direct_declarator array_modifier
	 * 
	 * consume the direct_declarator part and add the array modifier
	 */
	protected void consumeDirectDeclaratorArrayDeclarator() {
		IASTArrayModifier arrayModifier = (IASTArrayModifier) astStack.pop();
		consumeDeclaratorArray(arrayModifier);
	}
	
	
	/**
	 * direct_declarator ::= '(' declarator ')'
	 */
	protected void consumeDirectDeclaratorBracketed() {
		IASTDeclarator decl = (IASTDeclarator) astStack.peek();
		decl.setPropertyInParent(IASTDeclarator.NESTED_DECLARATOR);
	}
	
	
	/**
	 * init_declarator ::= declarator '=' initializer
	 */
	protected void consumeDeclaratorWithInitializer() {
		IASTInitializer expr = (IASTInitializer) astStack.pop();
		CASTDeclarator declarator = (CASTDeclarator) astStack.peek();
		
		declarator.setInitializer(expr);
		expr.setParent(declarator);
		expr.setPropertyInParent(IASTDeclarator.INITIALIZER);
		
		declarator.setLength(ruleLength);
	}
	
	
	/**
	 * direct_declarator ::= 'identifier'
	 */
	protected void consumeDirectDeclaratorIdentifier() {
		CASTName name = createName(parser.getRightIToken());
		
		CASTDeclarator declarator = new CASTDeclarator();
		declarator.setName(name);
		name.setParent(declarator);
		name.setPropertyInParent(IASTDeclarator.DECLARATOR_NAME);
		
		declarator.setOffsetAndLength(name);
		
		astStack.push(declarator);
	}
	
	
	/**
	 * direct_declarator ::= direct_declarator '(' <openscope> parameter_type_list ')'
	 * direct_declarator ::= direct_declarator '(' ')'
	 */
	protected void consumeDirectDeclaratorFunctionDeclarator(boolean hasParameters) {
		System.out.println("consumeDirectDeclaratorFunctionDeclarator " + hasParameters);
		System.out.println("offset " + ruleOffset + " length " + ruleLength);
		CASTFunctionDeclarator declarator = new CASTFunctionDeclarator();
		
		if(hasParameters) {
			for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
				CASTParameterDeclaration parameter = (CASTParameterDeclaration) iter.next();
				declarator.addParameterDeclaration(parameter);
				parameter.setParent(declarator);
				parameter.setPropertyInParent(IASTStandardFunctionDeclarator.FUNCTION_PARAMETER);
			}
			astStack.closeASTScope();
		}
		
		int endOffset = endOffset(parser.getRightIToken());
		System.out.println("endOffset? " + endOffset );
		
		consumeDirectDeclaratorFunctionDeclarator(declarator, endOffset);
	}
	
	
	/**
	 * direct_declarator ::= direct_declarator '(' <openscope> identifier_list ')'
	 */
	protected void consumeDirectDeclaratorFunctionDeclaratorKnR() {
		System.out.println("consumeDirectDeclaratorFunctionDeclaratorKnR");
		CASTKnRFunctionDeclarator declarator = new CASTKnRFunctionDeclarator();
		
		IASTName[] names = (IASTName[])astStack.topScopeArray(new IASTName[]{});
		declarator.setParameterNames(names);
		for(int i = 0; i < names.length; i++) {
			names[i].setParent(declarator);
			names[i].setPropertyInParent(ICASTKnRFunctionDeclarator.FUNCTION_PARAMETER);
		}
		astStack.closeASTScope();
		
		int endOffset = endOffset(parser.getRightIToken());
		consumeDirectDeclaratorFunctionDeclarator(declarator, endOffset);
	}
	
	
	/**
	 * Pops a simple declarator from the stack, converts it into 
	 * a FunctionDeclator, then pushes it.
	 * 
	 * TODO: is this the best way of doing this?
	 * 
	 */
	private void consumeDirectDeclaratorFunctionDeclarator(IASTFunctionDeclarator declarator, int endOffset) {
		IASTDeclarator decl = (IASTDeclarator) astStack.pop();
		 
		if(decl.getPropertyInParent() == IASTDeclarator.NESTED_DECLARATOR) {
			declarator.setNestedDeclarator(decl);
			decl.setParent(declarator);
			
			IASTName name = new CASTName();
			declarator.setName(name);
			name.setParent(declarator);
			name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
			
			int offset = ((ASTNode)decl).getOffset();
			((ASTNode)declarator).setOffsetAndLength(offset, endOffset - offset);
			astStack.push(declarator);
		}
		else if(decl instanceof CASTDeclarator) {
			CASTName name = (CASTName)((CASTDeclarator)decl).getName();
			declarator.setName(name);
			name.setParent(declarator);
			name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
			
			IASTPointerOperator[] pointers = decl.getPointerOperators();
			for(int i = 0; i < pointers.length; i++) {
				IASTPointerOperator pointer = pointers[i];
				declarator.addPointerOperator(pointer);
				pointer.setParent(declarator);
				pointer.setPropertyInParent(IASTFunctionDeclarator.POINTER_OPERATOR);
			}
			
			int offset = name.getOffset();
			((ASTNode)declarator).setOffsetAndLength(offset, endOffset - offset);
			
			astStack.push(declarator);
		}
		else {
			astStack.push(new CASTProblemDeclaration());
			encounteredRecoverableProblem = true;
		}
	}
	
	
	/**
	 * pointer ::= '*'
     *           | pointer '*' 
     */ 
	protected void consumePointer() {
		CASTPointer pointer = new CASTPointer();
		IToken star = parser.getRightIToken();
		pointer.setOffsetAndLength(offset(star), length(star));
		astStack.push(pointer);
	}
	
	
	/**
	 * pointer ::= '*' <openscope> type_qualifier_list
     *           | pointer '*' <openscope> type_qualifier_list
	 */
	protected void consumePointerTypeQualifierList() {
		CASTPointer pointer = new CASTPointer();

		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IToken token = (IToken) iter.next();			
			switch(token.getKind()) {
				case C99Parsersym.TK_const:
					pointer.setConst(true);
					break;
				case C99Parsersym.TK_restrict:
					pointer.setRestrict(true);
					break;
				case C99Parsersym.TK_volatile:
					pointer.setVolatile(true);
					break;
			}
		}
		astStack.closeASTScope();

		pointer.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(pointer);
	}
	
	
	/**
	 * parameter_declaration ::= declaration_specifiers declarator
     *                         | declaration_specifiers   
     *                         | declaration_specifiers abstract_declarator
	 */
	protected void consumeParameterDeclaration(boolean hasDeclarator) {
		CASTParameterDeclaration declaration = new CASTParameterDeclaration();
		
		IASTDeclarator declarator;
		if(hasDeclarator) {
			declarator = (IASTDeclarator) astStack.pop();
		}
		else { // it appears that a declarator is always required in the AST here
			declarator = new CASTDeclarator();
			((ASTNode)declarator).setOffsetAndLength(ruleOffset + ruleLength, 0);
			CASTName name = new CASTName();
			name.setOffsetAndLength(ruleOffset + ruleLength, 0);
			declarator.setName(name);
			name.setParent(declarator);
			name.setPropertyInParent(IASTDeclarator.DECLARATOR_NAME);
		}
		
		declaration.setDeclarator(declarator);
		declarator.setParent(declaration);
		declarator.setPropertyInParent(IASTParameterDeclaration.DECLARATOR);
		
		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop();
		declaration.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(declaration);
		declSpecifier.setPropertyInParent(IASTParameterDeclaration.DECL_SPECIFIER);
		
		declaration.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(declaration);
	}
	
	
	
	/**
	 * direct_abstract_declarator   
     *     ::= array_modifier
     *       | direct_abstract_declarator array_modifier
	 */
	protected void consumeAbstractDeclaratorArrayModifier(boolean hasDeclarator) {
		IASTArrayModifier arrayModifier = (IASTArrayModifier) astStack.pop();
		
		if(hasDeclarator) {
			consumeDeclaratorArray(arrayModifier);
		}
		else {
			CASTArrayDeclarator decl = new CASTArrayDeclarator();
			decl.addArrayModifier(arrayModifier);
			arrayModifier.setParent(decl);
			arrayModifier.setPropertyInParent(IASTArrayDeclarator.ARRAY_MODIFIER);
			
			decl.setOffsetAndLength(ruleOffset, ruleLength);
			astStack.push(decl);
		}
	}
	
	
	/**
	 * direct_abstract_declarator  
	 *     ::= '(' ')'
     *       | direct_abstract_declarator '(' ')'
     *       | '(' <openscope> parameter_type_list ')'
     *       | direct_abstract_declarator '(' <openscope> parameter_type_list ')'
	 */
	protected void consumeAbstractDeclaratorFunctionDeclarator(boolean hasDeclarator, boolean hasParameters) {
		CASTFunctionDeclarator declarator = new CASTFunctionDeclarator();
		
		if(hasParameters) {
			for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
				CASTParameterDeclaration parameter = (CASTParameterDeclaration) iter.next();
				declarator.addParameterDeclaration(parameter);
				parameter.setParent(declarator);
				parameter.setPropertyInParent(IASTStandardFunctionDeclarator.FUNCTION_PARAMETER);
			}
			astStack.closeASTScope();
		}
		
		IASTName name = new CASTName();
		declarator.setName(name);
		name.setParent(declarator);
		name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
		
		if(hasDeclarator) {
			consumeDirectDeclaratorFunctionDeclarator(declarator, endOffset(parser.getRightIToken()));
		}
		else {
			declarator.setOffsetAndLength(ruleOffset, ruleLength);
			astStack.push(declarator);
		}
	}
	
	
	/**
	 * initializer ::= assignment_expression
	 */
	protected void consumeInitializer() {
		IASTExpression assignmentExpression = (IASTExpression) astStack.pop();
		
		CASTInitializerExpression expr = new CASTInitializerExpression();
		
		expr.setExpression(assignmentExpression);
		assignmentExpression.setParent(expr);
        assignmentExpression.setPropertyInParent(IASTInitializerExpression.INITIALIZER_EXPRESSION);
        
        expr.setOffsetAndLength((ASTNode)assignmentExpression);
        
        astStack.push(expr);
	}
	
	
	/**
	 * initializer ::= '{' <openscope> initializer_list '}'
     *               | '{' <openscope> initializer_list ',' '}'
	 */
	protected void consumeInitializerList() {
		CASTInitializerList list = new CASTInitializerList();
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTInitializer initializer = (IASTInitializer) iter.next();
			list.addInitializer(initializer);
			initializer.setParent(list);
			initializer.setPropertyInParent(IASTInitializerList.NESTED_INITIALIZER);
		}
		astStack.closeASTScope();
		
		list.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(list);
	}
	
	
	/**
	 * designated_initializer ::= <openscope> designation initializer
	 */
	protected void consumeInitializerDesignated() {
		CASTDesignatedInitializer result = new CASTDesignatedInitializer();
		
		IASTInitializer initializer = (IASTInitializer)astStack.pop();
		result.setOperandInitializer(initializer);
		initializer.setParent(result);
		initializer.setPropertyInParent(ICASTDesignatedInitializer.OPERAND);
		
		// consume the designation which is a list of designators
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			ICASTDesignator designator  = (ICASTDesignator)iter.next();
			result.addDesignator(designator);
			designator.setParent(result);
			designator.setPropertyInParent(ICASTDesignatedInitializer.DESIGNATOR);
		}
		astStack.closeASTScope();
		
		result.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(result);
	}
	
	
	/**
	 * designator ::= '[' constant_expression ']'
	 */
	protected void consumeDesignatorArrayDesignator() {
		IASTExpression expr = (IASTExpression) astStack.pop();
		
		CASTArrayDesignator designator = new CASTArrayDesignator();
		designator.setSubscriptExpression(expr);
		expr.setParent(designator);
		expr.setPropertyInParent(ICASTArrayDesignator.SUBSCRIPT_EXPRESSION);
		
		designator.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(designator);
	}
	
	
	/**
	 *  designator ::= '.' 'identifier'
	 */
	protected void consumeDesignatorFieldDesignator() {		
		CASTFieldDesignator designator = new CASTFieldDesignator();
		IASTName name = createName( parser.getRightIToken() );
		designator.setName(name);
		name.setParent(designator);
		name.setPropertyInParent(ICASTFieldDesignator.FIELD_NAME);
		
		designator.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(designator);
	}
	
	
	/**
	 * declaration_specifiers ::= <openscope> simple_declaration_specifiers
	 */
	protected void consumeDeclarationSpecifiersSimple() {
		CASTSimpleDeclSpecifier declSpec = new CASTSimpleDeclSpecifier();
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IToken token = (IToken) iter.next();
			setSpecifier(declSpec, token);
		}
		astStack.closeASTScope();
		
		declSpec.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(declSpec);
	}
	
	
	/**
	 * declaration_specifiers ::= <openscope> struct_or_union_declaration_specifiers
	 * declaration_specifiers ::= <openscope> enum_declaration_specifiers
	 */
	protected void consumeDeclarationSpecifiersStructUnionEnum() {
		// There's already a composite type specifier somewhere on the stack, find it.
		ICASTDeclSpecifier declSpec = null;
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			Object o = iter.next();
			if(o instanceof ICASTDeclSpecifier) {
				declSpec = (ICASTDeclSpecifier) o;
				iter.remove();
				break;
			}
		}
		
		// now apply the rest of the specifiers
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IToken token = (IToken) iter.next();
			setSpecifier(declSpec, token);
		}
		astStack.closeASTScope();
		
		((ASTNode)declSpec).setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(declSpec);
	}
	
	
	/**
	 * declaration_specifiers ::=  <openscope> typdef_name_declaration_specifiers
	 */
	protected void consumeDeclarationSpecifiersTypedefName() {
		CASTTypedefNameSpecifier declSpec = new CASTTypedefNameSpecifier();
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IToken token = (IToken) iter.next();
			// There is one identifier token on the stack
			if(token.getKind() == C99Parsersym.TK_identifier) {
				IASTName name = createName(token);
				declSpec.setName(name);
				name.setParent(declSpec);
				name.setPropertyInParent(IASTNamedTypeSpecifier.NAME);
			}
			else
				setSpecifier(declSpec, token);
		}
		astStack.closeASTScope();
		declSpec.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(declSpec);
	}
	
	
	/**
	 * declaration ::= declaration_specifiers <openscope> init_declarator_list ';'
	 * declaration ::= declaration_specifiers  ';'
	 */
	protected void consumeDeclaration(boolean hasDeclaratorList) {
		CASTSimpleDeclaration declaration = new CASTSimpleDeclaration();
		
		if(hasDeclaratorList) {
			for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
				IASTDeclarator declarator = (IASTDeclarator)iter.next();
				declaration.addDeclarator(declarator);
				declarator.setParent(declaration);
				declarator.setPropertyInParent(IASTSimpleDeclaration.DECLARATOR);
			}
			astStack.closeASTScope();
		}
		
		ICASTDeclSpecifier declSpecifier = (ICASTDeclSpecifier) astStack.pop();
		
		declaration.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(declaration);
		declSpecifier.setPropertyInParent(IASTSimpleDeclaration.DECL_SPECIFIER);
		
		declaration.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(declaration);
	}
	
	
	/**
	 * external_declaration ::= ';'
	 */
	protected void consumeDeclarationEmpty() {
		CASTSimpleDeclaration declaration = new CASTSimpleDeclaration();
		declaration.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(declaration);
	}
	
	
	/**
	 * struct_declaration ::= specifier_qualifier_list <openscope> struct_declarator_list ';'
	 * 
	 * specifier_qualifier_list is a subset of declaration_specifiers,
	 * struct_declarators are declarators that are allowed inside a struct,
	 * a struct declarator is a regular declarator plus bit fields
	 */
	protected void consumeStructDeclaration(boolean hasDeclaration) {
		consumeDeclaration(hasDeclaration); // TODO this is ok as long as bit fields implement IASTDeclarator (see consumeDeclaration())
	} 
	
	
	/**
	 * struct_declarator
     *     ::= ':' constant_expression  
     *       | declarator ':' constant_expression		
	 */
	protected void consumeStructBitField(boolean hasDeclarator) {
		IASTExpression expr = (IASTExpression)astStack.pop();
		
		CASTFieldDeclarator fieldDecl = new CASTFieldDeclarator();
		fieldDecl.setBitFieldSize(expr);
		expr.setParent(fieldDecl);
		expr.setPropertyInParent(IASTFieldDeclarator.FIELD_SIZE);
		
		IASTName name;
		if(hasDeclarator) { // it should have been parsed into a regular declarator
			IASTDeclarator decl = (IASTDeclarator) astStack.pop();
			name = decl.getName();
		}
		else {
			name = new CASTName();
		}
		
		fieldDecl.setName(name);
		name.setParent(fieldDecl);
		name.setPropertyInParent(IASTFieldDeclarator.DECLARATOR_NAME);
		
		
		fieldDecl.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(fieldDecl);
	}
	
	
	
	/**
	 * struct_or_union_specifier
     *     ::= 'struct' '{' <openscope> struct_declaration_list_opt '}'
     *       | 'union'  '{' <openscope> struct_declaration_list_opt '}'
     *       | 'struct' struct_or_union_identifier '{' <openscope> struct_declaration_list_opt '}'
     *       | 'union'  struct_or_union_identifier '{' <openscope> struct_declaration_list_opt '}'
	 * 
	 * @param key either k_struct or k_union from IASTCompositeTypeSpecifier
	 */
	protected void consumeTypeSpecifierComposite(boolean hasName, int key) {
		CASTCompositeTypeSpecifier typeSpec = new CASTCompositeTypeSpecifier();
		typeSpec.setKey(key); // struct or union
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTDeclaration declaration = (IASTDeclaration)iter.next();
			typeSpec.addMemberDeclaration(declaration);
			declaration.setParent(typeSpec);
			declaration.setPropertyInParent(IASTCompositeTypeSpecifier.MEMBER_DECLARATION);
		}
		astStack.closeASTScope();
		
		IASTName name = (hasName) ? (IASTName)astStack.pop() : new CASTName();
		typeSpec.setName(name);
		name.setParent(typeSpec);
		name.setPropertyInParent(IASTCompositeTypeSpecifier.TYPE_NAME);
		
		typeSpec.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(typeSpec);
	}
	
	
	/**
	 * struct_or_union_specifier
     *     ::= 'struct' struct_or_union_identifier
     *       | 'union'  struct_or_union_identifier
     *       
     * enum_specifier ::= 'enum' enum_identifier     
	 */
	protected void consumeTypeSpecifierElaborated(int kind) {
		CASTElaboratedTypeSpecifier typeSpec = new CASTElaboratedTypeSpecifier();
		typeSpec.setKind(kind);
		
		IASTName name = (IASTName)astStack.pop();
		typeSpec.setName(name);
		name.setParent(typeSpec);
		name.setPropertyInParent(IASTElaboratedTypeSpecifier.TYPE_NAME);
		
		typeSpec.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(typeSpec);
	}
	
	
	
	/**
	 * enum_specifier ::= 'enum' '{' <openscope> enumerator_list_opt '}'
     *                  | 'enum' enum_identifier '{' <openscope> enumerator_list_opt '}'
     *                  | 'enum' '{' <openscope> enumerator_list_opt ',' '}'
     *                  | 'enum' enum_identifier '{' <openscope> enumerator_list_opt ',' '}'
	 */
	protected void consumeTypeSpecifierEnumeration(boolean hasIdentifier) {
		CASTEnumerationSpecifier enumSpec = new CASTEnumerationSpecifier();

		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTEnumerator enumerator = (IASTEnumerator)iter.next();
			enumSpec.addEnumerator(enumerator);
			enumerator.setParent(enumSpec);
			enumerator.setPropertyInParent(ICASTEnumerationSpecifier.ENUMERATOR);
		}
		astStack.closeASTScope();
		
		if(hasIdentifier) {
			IASTName name = (IASTName)astStack.pop();
			enumSpec.setName(name);
			name.setParent(enumSpec);
			name.setPropertyInParent(ICASTEnumerationSpecifier.ENUMERATION_NAME);
		}
		
		enumSpec.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(enumSpec);
	}
	
	
	
	/**
	 * enumerator ::= enum_identifier
     *              | enum_identifier '=' constant_expression
	 */
	protected void consumeEnumerator(boolean hasInitializer) {
		CASTEnumerator enumerator = new CASTEnumerator();
		
		if(hasInitializer) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			enumerator.setValue(expr);
			expr.setParent(enumerator);
			expr.setPropertyInParent(IASTEnumerator.ENUMERATOR_VALUE);
		}
		
		IASTName name = (IASTName)astStack.pop();
		enumerator.setName(name);
		name.setParent(enumerator);
		name.setPropertyInParent(IASTEnumerator.ENUMERATOR_NAME);
		
		enumerator.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(enumerator);
	}
		
	
	/**
	 * compound_statement ::= <openscope> '{' block_item_list '}'
	 * 
	 * block_item_list ::= block_item | block_item_list block_item
	 */
	protected void consumeStatementCompoundStatement() {
		CASTCompoundStatement block = new CASTCompoundStatement();
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTStatement statement = (IASTStatement)iter.next();
			block.addStatement(statement);
			statement.setParent(block);
			statement.setPropertyInParent(IASTCompoundStatement.NESTED_STATEMENT);
		}
		astStack.closeASTScope();
		
		block.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(block);
	}
	
	
	
	/**
	 * compound_statement ::= '{' '}' 
	 */
	protected void consumeStatementEmptyCompoundStatement() {
		CASTCompoundStatement statement = new CASTCompoundStatement();
		statement.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(statement);
	}
	
	
	
	
	/**
	 * iteration_statement_matched
	 *     ::= 'for' '(' expression ';' expression ';' expression ')' matched_statement
     *       | 'for' '(' expression ';' expression ';'            ')' matched_statement
     *       | 'for' '(' expression ';'            ';' expression ')' matched_statement
     *       | 'for' '(' expression ';'            ';'            ')' matched_statement
     *       | 'for' '('            ';' expression ';' expression ')' matched_statement
     *       | 'for' '('            ';' expression ';'            ')' matched_statement
     *       | 'for' '('            ';'            ';' expression ')' matched_statement
     *       | 'for' '('            ';'            ';'            ')' matched_statement
     *       | 'for' '(' declaration expression ';' expression ')' matched_statement
     *       | 'for' '(' declaration expression ';'            ')' matched_statement
     *       | 'for' '(' declaration            ';' expression ')' matched_statement
     *       | 'for' '(' declaration            ';'            ')' matched_statement
     *       
	 */
	protected void consumeStatementForLoop(boolean hasExpr1, boolean hasExpr2, boolean hasExpr3) {
		CASTForStatement forStat = new CASTForStatement();
		
		IASTStatement body = (IASTStatement) astStack.pop();
		forStat.setBody(body);
		body.setParent(forStat);
		body.setPropertyInParent(IASTForStatement.BODY);
		
		if(hasExpr3) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			forStat.setIterationExpression(expr);
			expr.setParent(forStat);
			expr.setPropertyInParent(IASTForStatement.ITERATION);
		}
		
		if(hasExpr2) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			forStat.setConditionExpression(expr);
			expr.setParent(forStat);
			expr.setPropertyInParent(IASTForStatement.CONDITION);
		}
		
		if(hasExpr1) { // may be an expression or a declaration
			IASTNode node = (IASTNode) astStack.pop();
			
			if(node instanceof IASTExpression) {
				IASTExpressionStatement stat = new CASTExpressionStatement();
				IASTExpression expr = (IASTExpression)node;
				stat.setExpression(expr);
				expr.setParent(stat);
				expr.setPropertyInParent(IASTExpressionStatement.EXPFRESSION);
				
				forStat.setInitializerStatement(stat);
				stat.setParent(forStat);
				stat.setPropertyInParent(IASTForStatement.INITIALIZER);
			}
			else if(node instanceof IASTDeclaration) {
				IASTDeclarationStatement stat = new CASTDeclarationStatement();
				IASTDeclaration declaration = (IASTDeclaration)node;
				stat.setDeclaration(declaration);
				declaration.setParent(stat);
				declaration.setPropertyInParent(IASTDeclarationStatement.DECLARATION);
				
				forStat.setInitializerStatement(stat);
				stat.setParent(forStat);
				stat.setPropertyInParent(IASTForStatement.INITIALIZER);
			}
		}
		else {
			forStat.setInitializerStatement(new CASTNullStatement());
		}
		
		forStat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(forStat);
	}
	
	
	/**
	 * iteration_statement_matched
	 *     ::= 'while' '(' expression ')' matched_statement
	 */
	protected void consumeStatementWhileLoop() {
		CASTWhileStatement stat = new CASTWhileStatement();
		
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTExpression condition = (IASTExpression) astStack.pop();
		
		stat.setBody(body);
		body.setParent(stat);
		body.setPropertyInParent(IASTWhileStatement.BODY);
		
		stat.setCondition(condition);
		condition.setParent(stat);
		condition.setPropertyInParent(IASTWhileStatement.CONDITIONEXPRESSION);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	
	/**
	 * iteration_statement_matched
	 *     ::= 'do' statement 'while' '(' expression ')' ';'
	 */
	protected void consumeStatementDoLoop() {
		CASTDoStatement stat = new CASTDoStatement();
		
		IASTExpression condition = (IASTExpression) astStack.pop();
		IASTStatement body = (IASTStatement) astStack.pop();
		
		stat.setCondition(condition);
		condition.setParent(stat);
		condition.setPropertyInParent(IASTDoStatement.CONDITION);
		
		stat.setBody(body);
		body.setParent(stat);
		body.setPropertyInParent(IASTDoStatement.BODY);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * block_item ::= declaration | statement 
	 * 
	 * Wrap a declaration in a DeclarationStatement.
	 * 
	 * Disambiguation:
	 * 
	 * x; // should be an expression statement
	 * 
	 */
	protected void consumeStatementDeclaration() {
		IASTDeclaration decl = (IASTDeclaration) astStack.pop();
		
		// Kludgy way to disambiguate a certain case.
		// An identifier alone on a line will be parsed as a declaration
		// but it probably should be an expression.
		// eg) i;
		if(decl instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) decl;
			if(declaration.getDeclarators() == IASTDeclarator.EMPTY_DECLARATOR_ARRAY) {
				IASTDeclSpecifier declSpec = declaration.getDeclSpecifier();
				if(declSpec instanceof ICASTTypedefNameSpecifier) {
					CASTTypedefNameSpecifier typedefNameSpec = (CASTTypedefNameSpecifier) declSpec;
					CASTName name = (CASTName)typedefNameSpec.getName();
					
					if(name.getOffset() == typedefNameSpec.getOffset() &&
				       name.getLength() == typedefNameSpec.getLength()) {
						
						CASTExpressionStatement stat = new CASTExpressionStatement();
						CASTIdExpression idExpr = new CASTIdExpression();
						idExpr.setName(name);
						name.setParent(idExpr);
						name.setPropertyInParent(IASTIdExpression.ID_NAME);
						
						stat.setExpression(idExpr);
						idExpr.setParent(stat);
						idExpr.setPropertyInParent(IASTExpressionStatement.EXPFRESSION);
						
						stat.setOffsetAndLength(ruleOffset, ruleLength);
						astStack.push(stat);
						return;
					}
				}
			}
		}

		CASTDeclarationStatement stat = new CASTDeclarationStatement();
		
		stat.setDeclaration(decl);
		decl.setParent(stat);
		decl.setPropertyInParent(IASTDeclarationStatement.DECLARATION);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * jump_statement ::= goto 'identifier' ';'
	 */
	protected void consumeStatementGoto() {
		IASTName name = createName(parser.getRhsIToken(2));
		CASTGotoStatement gotoStat = new CASTGotoStatement();
		
		gotoStat.setName(name);
		name.setParent(gotoStat);
		name.setPropertyInParent(IASTGotoStatement.NAME);
		
		gotoStat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(gotoStat);
	}
	
	
	/**
	 * jump_statement ::= continue ';'
	 */
	protected void consumeStatementContinue() {  
		CASTContinueStatement stat = new CASTContinueStatement();
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * jump_statement ::= break ';'
	 */
	protected void consumeStatementBreak() {   
		CASTBreakStatement stat = new CASTBreakStatement();
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * jump_statement ::= return ';'
	 * jump_statement ::= return expression ';'
	 */
	protected void consumeStatementReturn(boolean hasExpression) {
		CASTReturnStatement returnStat = new CASTReturnStatement();
		
		if(hasExpression) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			returnStat.setReturnValue(expr);
			expr.setParent(returnStat);
			expr.setPropertyInParent(IASTReturnStatement.RETURNVALUE);
		}
		
		returnStat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(returnStat);
	}
	
	
	/**
	 * expression_statement ::= ';'
	 */
	protected void consumeStatementNull() {
		CASTNullStatement stat = new CASTNullStatement();
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * expression_statement ::= expression ';'
	 */
	protected void consumeStatementExpression() {
		IASTExpression expr = (IASTExpression) astStack.pop();
		CASTExpressionStatement stat = new CASTExpressionStatement();
		stat.setExpression(expr);
		expr.setParent(stat);
		expr.setPropertyInParent(IASTExpressionStatement.EXPFRESSION);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * labeled_statement ::= label_identifier ':' statement
	 * label_identifier ::= identifier 
	 */
	protected void consumeStatementLabeled() {
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTName label = (IASTName) astStack.pop();
		
		CASTLabelStatement stat = new CASTLabelStatement();
		
		stat.setNestedStatement(body);
		body.setParent(stat);
		body.setPropertyInParent(IASTLabelStatement.NESTED_STATEMENT);
		
		stat.setName(label);
		label.setParent(stat);
		label.setPropertyInParent(IASTLabelStatement.NAME);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * labeled_statement ::= case constant_expression ':' statement
	 */
	protected void consumeStatementCase() { 
		IASTStatement body  = (IASTStatement)  astStack.pop();
		IASTExpression expr = (IASTExpression) astStack.pop();
		
		CASTCaseStatement stat = new CASTCaseStatement();
		
		stat.setExpression(expr);
		expr.setParent(stat);
		expr.setPropertyInParent(IASTCaseStatement.EXPRESSION);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
		astStack.push(body);
	}
	
	
	/**
	 * labeled_statement ::= default ':' statement
	 */
	protected void consumeStatementDefault() {
		IASTStatement body = (IASTStatement) astStack.pop();
		
		CASTDefaultStatement stat = new CASTDefaultStatement();
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(stat);
		astStack.push(body);
	}
	
	
	/**
	 * selection_statement ::=  switch '(' expression ')' statement
	 */
	protected void consumeStatementSwitch() {
		IASTStatement body  = (IASTStatement)  astStack.pop();
		IASTExpression expr = (IASTExpression) astStack.pop();
		
		CASTSwitchStatement stat = new CASTSwitchStatement();
		
		stat.setBody(body);
		body.setParent(stat);
		body.setPropertyInParent(IASTSwitchStatement.BODY);
		
		stat.setControllerExpression(expr);
		expr.setParent(stat);
		expr.setPropertyInParent(IASTSwitchStatement.CONTROLLER_EXP);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * if_then_statement ::= if '(' expression ')' statement
	 */
	protected void consumeStatementIfThen() {
		IASTStatement thenClause = (IASTStatement) astStack.pop();
		IASTExpression condition = (IASTExpression) astStack.pop();
		
		CASTIfStatement stat = new CASTIfStatement();
		
		stat.setConditionExpression(condition);
		condition.setParent(stat);
		condition.setPropertyInParent(IASTIfStatement.CONDITION);
		
		stat.setThenClause(thenClause);
		thenClause.setParent(stat);
		thenClause.setPropertyInParent(IASTIfStatement.THEN);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * if_then_else_matched_statement
     *     ::= if '(' expression ')' statement_no_short_if else statement_no_short_if
     *     
     * if_then_else_unmatched_statement
     *     ::= if '(' expression ')' statement_no_short_if else statement
	 */
	protected void consumeStatementIfThenElse() { 
		IASTStatement elseClause = (IASTStatement) astStack.pop();
		
		consumeStatementIfThen();
		CASTIfStatement stat = (CASTIfStatement) astStack.pop();
		
		stat.setElseClause(elseClause);
		elseClause.setParent(stat);
		elseClause.setPropertyInParent(IASTIfStatement.ELSE);
		
		// the offset and length is set in consumeStatementIfThen()
		astStack.push(stat);
	}
	

	/**
	 * translation_unit ::= external_declaration_list
     *
     * external_declaration_list
     *    ::= external_declaration
     *      | external_declaration_list external_declaration
	 */
	protected void consumeTranslationUnit() {
		CASTTranslationUnit tu = new CASTTranslationUnit();
		tu.setParent(null);
		tu.setPropertyInParent(null);
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTDeclaration declaration = (IASTDeclaration) iter.next();
			tu.addDeclaration(declaration);
			declaration.setParent(tu);
			declaration.setPropertyInParent(IASTTranslationUnit.OWNED_DECLARATION);
		}
		
		
		//tu.setOffsetAndLength(ruleOffset, ruleLength);
		
		IToken eof = (IToken) parser.getTokens().get(parser.getTokens().size() - 1);
		System.out.println("Is this EOF?: " + eof);
		tu.setOffsetAndLength(0, eof.getEndOffset());
		astStack.push(tu); 
	}
	
	
	/**
	 * function_definition
     *    ::= declaration_specifiers <openscope> declarator compound_statement
     */
	protected void consumeFunctionDefinition() {
		CASTFunctionDefinition def = new CASTFunctionDefinition();
		
		IASTCompoundStatement  body = (IASTCompoundStatement)  astStack.pop();
		IASTFunctionDeclarator decl = (IASTFunctionDeclarator) astStack.pop();
		// The seemingly pointless <openscope> is just there to 
		// prevent a shift/reduce conflict in the grammar.
		astStack.closeASTScope();
		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop();
		
		def.setBody(body);
		body.setParent(def);
		body.setPropertyInParent(IASTFunctionDefinition.FUNCTION_BODY);
		
		def.setDeclarator(decl);
		decl.setParent(def);
		decl.setPropertyInParent(IASTFunctionDefinition.DECLARATOR);
		
		def.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(def);
		declSpecifier.setPropertyInParent(IASTFunctionDefinition.DECL_SPECIFIER);
		
		def.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(def);
	}
	
	
    
    /**
     * function_definition
     *     ::= declaration_specifiers <openscope> declarator 
     *         <openscope> declaration_list compound_statement
     */
	protected void consumeFunctionDefinitionKnR() {
    	CASTFunctionDefinition def = new CASTFunctionDefinition();
    	
    	// compound_statement
    	IASTCompoundStatement  body = (IASTCompoundStatement) astStack.pop();
    	
    	// declaration_list, parameters
    	IASTDeclaration[] declarations = (IASTDeclaration[]) astStack.topScopeArray(new IASTDeclaration[]{});
    	astStack.closeASTScope();
    	
    	// declarator
    	CASTKnRFunctionDeclarator decl = (CASTKnRFunctionDeclarator) astStack.pop();
    	astStack.closeASTScope();

    	ICASTSimpleDeclSpecifier declSpecifier = (CASTSimpleDeclSpecifier) astStack.pop();
    	
    	decl.setParameterDeclarations(declarations);
		for(int i = 0; i < declarations.length; i++) {
			declarations[i].setParent(decl);
			declarations[i].setPropertyInParent(ICASTKnRFunctionDeclarator.FUNCTION_PARAMETER);
		}
		
		def.setBody(body);
		body.setParent(def);
		body.setPropertyInParent(IASTFunctionDefinition.FUNCTION_BODY);
		
		def.setDeclarator(decl);
		decl.setParent(def);
		decl.setPropertyInParent(IASTFunctionDefinition.DECLARATOR);
		
		def.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(def);
		declSpecifier.setPropertyInParent(IASTFunctionDefinition.DECL_SPECIFIER);
	
		def.setOffsetAndLength(ruleOffset, ruleLength);
    	astStack.push(def);
    }
	
	
	/**
	 * statement ::= ERROR_TOKEN
	 */
	protected void consumeStatementProblem() {
		consumeProblem(new CASTProblemStatement());
	}
	
	
	/**
	 * assignment_expression ::= ERROR_TOKEN
	 * constant_expression ::= ERROR_TOKEN
	 */
	protected void consumeExpressionProblem() {
		consumeProblem(new CASTProblemExpression());
	}
	
	
	/**
	 * external_declaration ::= ERROR_TOKEN
	 */
	protected void consumeDeclarationProblem() {
		consumeProblem(new CASTProblemDeclaration());
	}
	
	
	private void consumeProblem(IASTProblemHolder problemHolder) {
		encounteredRecoverableProblem = true;
		
		CASTProblem problem = new CASTProblem(IASTProblem.SYNTAX_ERROR, EMPTY_CHAR_ARRAY, false, true);
		
		problemHolder.setProblem(problem);
		problem.setParent((IASTNode)problemHolder);
		problem.setPropertyInParent(IASTProblemStatement.PROBLEM);
		
		problem.setOffsetAndLength(ruleOffset, ruleLength);
		((ASTNode)problemHolder).setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(problemHolder);
	}


	
}
