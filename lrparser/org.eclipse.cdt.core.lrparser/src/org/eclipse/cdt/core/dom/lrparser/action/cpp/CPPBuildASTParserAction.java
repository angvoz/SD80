/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action.cpp;

import static org.eclipse.cdt.core.parser.util.CollectionUtils.findFirstAndRemove;
import static org.eclipse.cdt.core.parser.util.CollectionUtils.reverseIterable;
import static org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPParsersym.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAmbiguousTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
import org.eclipse.cdt.core.dom.lrparser.LPGTokenAdapter;
import org.eclipse.cdt.core.dom.lrparser.action.BuildASTParserAction;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenMap;
import org.eclipse.cdt.core.dom.lrparser.action.TokenMap;
import org.eclipse.cdt.core.parser.util.DebugUtil;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPNoCastExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPNoFunctionDeclaratorParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPParsersym;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPSizeofExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPTemplateTypeParameterParser;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTAmbiguousTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

/**
 * Semantic actions that build the AST during the parse. 
 * These are the actions that are specific to the C++ parser, the superclass
 * contains actions that can be shared with the C99 parser.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public class CPPBuildASTParserAction extends BuildASTParserAction {

	/** Allows code in this class to refer to the token kinds in CPPParsersym */
	private final ITokenMap tokenMap;
	
	/** Used to create the AST node objects */
	protected final ICPPASTNodeFactory nodeFactory;
	
	/**
	 * @param parser
	 * @param orderedTerminalSymbols When an instance of this class is created for a parser
	 * that parsers token kinds will be mapped back to the base C99 parser's token kinds.
	 */
	public CPPBuildASTParserAction(ICPPASTNodeFactory nodeFactory, IParserActionTokenProvider parser, IASTTranslationUnit tu) {
		super(nodeFactory, parser, tu);
		this.nodeFactory = nodeFactory;
		this.tokenMap = new TokenMap(CPPParsersym.orderedTerminalSymbols, parser.getOrderedTerminalSymbols());
	}
	
	
	private int baseKind(IToken token) {
		return tokenMap.mapKind(token.getKind());
	}
	
	@Override 
	protected boolean isCompletionToken(IToken token) {
		return baseKind(token) == TK_Completion;
	}
	
	
	
	@Override
	protected IParser getExpressionParser() {
		return new CPPExpressionParser(parser.getOrderedTerminalSymbols()); 
	}
	
	@Override
	protected IParser getNoCastExpressionParser() {
		return new CPPNoCastExpressionParser(parser.getOrderedTerminalSymbols());
	}
	
	@Override
	protected IParser getSizeofExpressionParser() {
		return new CPPSizeofExpressionParser(parser.getOrderedTerminalSymbols());
	}
	
	protected IParser getTemplateTypeParameterParser() {
		return new CPPTemplateTypeParameterParser(parser.getOrderedTerminalSymbols());
	}
	
	protected IParser getNoFunctionDeclaratorParser() {
		return new CPPNoFunctionDeclaratorParser(parser.getOrderedTerminalSymbols()); 
	}

	
	
	
	/**
	 * new_expression
     *     ::= dcolon_opt 'new' new_placement_opt new_type_id <openscope-ast> new_array_expressions_op new_initializer_opt
     *       | dcolon_opt 'new' new_placement_opt '(' type_id ')' <openscope-ast> new_array_expressions_op new_initializer_opt
	 */
	public void consumeExpressionNew(boolean isNewTypeId) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();

		IASTExpression initializer = (IASTExpression) astStack.pop(); // may be null
		List<Object> arrayExpressions = astStack.closeScope();
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		IASTExpression placement = (IASTExpression) astStack.pop(); // may be null
		boolean hasDoubleColon = astStack.pop() != null;
		
		ICPPASTNewExpression newExpression = nodeFactory.newCPPNewExpression(placement, initializer, typeId);
		newExpression.setIsGlobal(hasDoubleColon);
		newExpression.setIsNewTypeId(isNewTypeId);
		
		for(Object expr : arrayExpressions)
			newExpression.addNewTypeIdArrayExpression((IASTExpression)expr);
		
		setOffsetAndLength(newExpression);
		astStack.push(newExpression);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * new_declarator -- pointer operators are part of the type id, held in an empty declarator
     *     ::= <openscope-ast> new_pointer_operators
	 */
	public void consumeNewDeclarator() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = nodeFactory.newName();
		IASTDeclarator declarator = nodeFactory.newDeclarator(name);
		
		for(Object pointer : astStack.closeScope())
			declarator.addPointerOperator((IASTPointerOperator)pointer);
		
		setOffsetAndLength(declarator);
		astStack.push(declarator);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * throw_expression
     *     ::= 'throw'
     *       | 'throw' assignment_expression
	 */
	public void consumeExpressionThrow(boolean hasExpr) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression operand = hasExpr ? (IASTExpression) astStack.pop() : null;
		IASTUnaryExpression expr = nodeFactory.newUnaryExpression(ICPPASTUnaryExpression.op_throw, operand);
		setOffsetAndLength(expr);
		astStack.push(expr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * delete_expression
     *     ::= dcolon_opt 'delete' cast_expression
     *       | dcolon_opt 'delete' '[' ']' cast_expression
	 * @param isVectorized
	 */
	public void consumeExpressionDelete(boolean isVectorized) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression operand = (IASTExpression) astStack.pop();
		boolean hasDoubleColon = astStack.pop() != null;
		
		ICPPASTDeleteExpression deleteExpr = nodeFactory.newDeleteExpression(operand);
		deleteExpr.setIsGlobal(hasDoubleColon);
		deleteExpr.setIsVectored(isVectorized);
		
		setOffsetAndLength(deleteExpr);
		astStack.push(deleteExpr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * 
	 */
	public void consumeExpressionFieldReference(boolean isPointerDereference, boolean hasTemplateKeyword) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = (IASTName) astStack.pop();
		IASTExpression owner = (IASTExpression) astStack.pop();
		IASTFieldReference expr = nodeFactory.newFieldReference(name, owner, isPointerDereference, hasTemplateKeyword);
		
		setOffsetAndLength(expr);
		astStack.push(expr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	/**
	 * postfix_expression
	 *     ::= simple_type_specifier '(' expression_list_opt ')'
	 */
	public void consumeExpressionSimpleTypeConstructor() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression expression = (IASTExpression) astStack.pop();
		IToken token = (IToken) astStack.pop();
		
		int type = asICPPASTSimpleTypeConstructorExpressionType(token);
		ICPPASTSimpleTypeConstructorExpression typeConstructor = nodeFactory.newCPPSimpleTypeConstructorExpression(type, expression);
		
		setOffsetAndLength(typeConstructor);
		astStack.push(typeConstructor);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	private int asICPPASTSimpleTypeConstructorExpressionType(IToken token) {
		assert token != null;
		
		switch(baseKind(token)) {
			case TK_char     : return ICPPASTSimpleTypeConstructorExpression.t_char;
			case TK_wchar_t  : return ICPPASTSimpleTypeConstructorExpression.t_wchar_t;
			case TK_bool     : return ICPPASTSimpleTypeConstructorExpression.t_bool;
			case TK_short    : return ICPPASTSimpleTypeConstructorExpression.t_short;
			case TK_int      : return ICPPASTSimpleTypeConstructorExpression.t_int;
			case TK_long     : return ICPPASTSimpleTypeConstructorExpression.t_long;
			case TK_signed   : return ICPPASTSimpleTypeConstructorExpression.t_signed;
			case TK_unsigned : return ICPPASTSimpleTypeConstructorExpression.t_unsigned;
			case TK_float    : return ICPPASTSimpleTypeConstructorExpression.t_float;
			case TK_double   : return ICPPASTSimpleTypeConstructorExpression.t_double;
			case TK_void     : return ICPPASTSimpleTypeConstructorExpression.t_void;
		
			default:
				assert false : "type parsed wrong"; //$NON-NLS-1$
				return ICPPASTSimpleTypeConstructorExpression.t_unspecified;
		}
	}
	
	
	/**
	 * postfix_expression
	 *     ::= 'typename' dcolon_opt nested_name_specifier <empty>  identifier_name '(' expression_list_opt ')'
     *       | 'typename' dcolon_opt nested_name_specifier template_opt template_id '(' expression_list_opt ')'
	 */
	@SuppressWarnings("unchecked")
	public void consumeExpressionTypeName() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression expr = (IASTExpression) astStack.pop();
		IASTName name = (IASTName) astStack.pop();
		boolean isTemplate = astStack.pop() == PLACE_HOLDER;
		LinkedList<IASTName> nestedNames = (LinkedList<IASTName>) astStack.pop();
		IToken dColon = (IToken) astStack.pop();

		nestedNames.addFirst(name);
		
		int startOffset = dColon == null ? offset(nestedNames.getLast()) : offset(dColon);
		int endOffset   = endOffset(name);
		IASTName qualifiedName = createQualifiedName(nestedNames, startOffset, endOffset, dColon != null);
		
		ICPPASTTypenameExpression typenameExpr = nodeFactory.newCPPTypenameExpression(qualifiedName, expr, isTemplate);
		
		setOffsetAndLength(typenameExpr);
		astStack.push(typenameExpr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * condition
     *     ::= type_specifier_seq declarator '=' assignment_expression
	 */
	public void consumeConditionDeclaration() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression expr = (IASTExpression) astStack.pop();
		IASTDeclarator declarator = (IASTDeclarator) astStack.pop();
		IASTDeclSpecifier declSpec = (IASTDeclSpecifier) astStack.pop();
		
		IASTInitializerExpression initializer = nodeFactory.newInitializerExpression(expr);
		setOffsetAndLength(initializer, offset(expr), length(expr));
		declarator.setInitializer(initializer);
		
		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpec);
		declaration.addDeclarator(declarator);
		
		setOffsetAndLength(declaration);
		astStack.push(declaration);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * template_id
     *     ::= identifier_name '<' <openscope-ast> template_argument_list_opt '>'
     *     
     * operator_function_id
     *     ::= operator_id '<' <openscope-ast> template_argument_list_opt '>'
	 */
	public void consumeTemplateId() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		List<Object> templateArguments = astStack.closeScope();
		IASTName name = (IASTName) astStack.pop();
		
		ICPPASTTemplateId templateId = nodeFactory.newCPPTemplateId(name);
		
		for(Object arg : templateArguments) {
			if(arg instanceof IASTExpression)
				templateId.addTemplateArgument((IASTExpression)arg);
			else if(arg instanceof IASTTypeId)
				templateId.addTemplateArgument((IASTTypeId)arg);
			else if(arg instanceof ICPPASTAmbiguousTemplateArgument)
				templateId.addTemplateArgument((ICPPASTAmbiguousTemplateArgument)arg);
		}
		
		setOffsetAndLength(templateId);
		astStack.push(templateId);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	

	/**
	 * Disambiguates template arguments.
	 */
	public void consumeTemplateArgumentTypeId() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IParser secondaryParser = getExpressionParser();
		IASTNode result = runSecondaryParser(secondaryParser);
		
		// The grammar rule allows assignment_expression, but the ambiguity
		// only arises with id_expressions.
		if(!(result instanceof IASTIdExpression))
			return;
		
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		IASTIdExpression idExpression = (IASTIdExpression) result;
		
		ICPPASTAmbiguousTemplateArgument ambiguityNode = nodeFactory.newAmbiguousTemplateArgument(typeId, idExpression);
		//setOffsetAndLength(ambiguityNode);
		
		astStack.push(ambiguityNode);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * operator_id
     *     ::= 'operator' overloadable_operator
	 */
	public void consumeOperatorName() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		List<IToken> tokens = parser.getRuleTokens();
		tokens = tokens.subList(1, tokens.size());
		OverloadableOperator operator = getOverloadableOperator(tokens);
		
		ICPPASTOperatorName name = nodeFactory.newCPPOperatorName(operator);
		setOffsetAndLength(name);
		astStack.push(name); 
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}

	

	private OverloadableOperator getOverloadableOperator(List<IToken> tokens) {
		if(tokens.size() == 1) {
			// TODO this is a hack that I did to save time
			LPGTokenAdapter coreToken = (LPGTokenAdapter) tokens.get(0);
			return OverloadableOperator.valueOf(coreToken.getWrappedToken());
		}
		else if(matchTokens(tokens, tokenMap, TK_new, TK_LeftBracket, TK_RightBracket)) {
			return OverloadableOperator.NEW_ARRAY;
		}
		else if(matchTokens(tokens, tokenMap, TK_delete, TK_LeftBracket, TK_RightBracket)) {
			return OverloadableOperator.DELETE_ARRAY;
		}
		else if(matchTokens(tokens, tokenMap, TK_LeftBracket, TK_RightBracket)) {
			return OverloadableOperator.BRACKET;
		}
		else if(matchTokens(tokens, tokenMap, TK_LeftParen, TK_RightParen)) {
			return OverloadableOperator.PAREN;
		}
		
		return null;
	}
	
	
	/**
	 * conversion_function_id
     *     ::= 'operator' conversion_type_id
	 */
	public void consumeConversionName() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		String rep = createStringRepresentation(parser.getRuleTokens());
		char[] chars = rep.toCharArray();
		
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		ICPPASTConversionName name = nodeFactory.newCPPConversionName(chars, typeId);
		setOffsetAndLength(name);
		astStack.push(name);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	
    /**
     * unqualified_id
     *     ::= '~' identifier_token
     */
  	public void consumeDestructorName() {
  		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
  		
  		char[] chars = ("~" + parser.getRightIToken()).toCharArray(); //$NON-NLS-1$
  		
  		IASTName name = nodeFactory.newName(chars);
  		setOffsetAndLength(name);
  		astStack.push(name);
  		
  		if(TRACE_AST_STACK) System.out.println(astStack);
  	}
  	
  	
  	/**
  	 * destructor_type_name
     *     ::= '~' template_id_name
  	 */
  	public void consumeDestructorNameTemplateId() {
  		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
  		
  		ICPPASTTemplateId templateId = (ICPPASTTemplateId) astStack.peek();
  		
  		IASTName oldName = templateId.getTemplateName();
  		char[] newChars = ("~" + oldName).toCharArray(); //$NON-NLS-1$
  		
  		IASTName newName = nodeFactory.newName(newChars);
  		
  		int offset = offset(parser.getLeftIToken());
  		int length = offset - endOffset(oldName);
  		setOffsetAndLength(newName, offset, length);
  		
  		templateId.setTemplateName(newName);
  		
  		if(TRACE_AST_STACK) System.out.println(astStack);
  	}
  	
  	
  	
  	/**
  	 * qualified_id
     *     ::= '::' identifier_name
     *       | '::' operator_function_id
     *       | '::' template_id
  	 */

	public void consumeGlobalQualifiedId() {
  		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();

  		IASTName name = (IASTName) astStack.pop();
  		
  		ICPPASTQualifiedName qualifiedName = nodeFactory.newCPPQualifiedName();
  		qualifiedName.addName(name);
  		qualifiedName.setFullyQualified(true);
  		((CPPASTQualifiedName)qualifiedName).setSignature("::" + name.toString()); //$NON-NLS-1$
  		
  		setOffsetAndLength(qualifiedName);
  		astStack.push(qualifiedName);
  		
  		if(TRACE_AST_STACK) System.out.println(astStack);
  	}
  	
  	
  	/**
	 * selection_statement ::=  switch '(' condition ')' statement
	 */
	public void consumeStatementSwitch() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTStatement body  = (IASTStatement)  astStack.pop();
		
		Object condition = astStack.pop();
		
		IASTSwitchStatement stat;
		if(condition instanceof IASTExpression)
			stat = nodeFactory.newSwitchStatment((IASTExpression)condition, body);
		else
			stat = nodeFactory.newSwitchStatment((IASTDeclaration)condition, body);
		
		
		setOffsetAndLength(stat);
		astStack.push(stat);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	

	public void consumeStatementIf(boolean hasElse) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTStatement elseClause = hasElse ? (IASTStatement)astStack.pop() : null;		
		IASTStatement thenClause = (IASTStatement) astStack.pop();
		
		Object condition = astStack.pop();
		
		IASTIfStatement ifStatement;
		if(condition instanceof IASTExpression)
			ifStatement = nodeFactory.newIfStatement((IASTExpression)condition, thenClause, elseClause);
		else
			ifStatement = nodeFactory.newIfStatement((IASTDeclaration)condition, thenClause, elseClause);
		
		setOffsetAndLength(ifStatement);
		astStack.push(ifStatement);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * iteration_statement ::= 'while' '(' condition ')' statement
	 */
	public void consumeStatementWhileLoop() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTStatement body = (IASTStatement) astStack.pop();
		
		Object condition = astStack.pop();
		
		IASTWhileStatement whileStatement;
		if(condition instanceof IASTExpression)
			whileStatement = nodeFactory.newWhileStatement((IASTExpression)condition, body);
		else
			whileStatement = nodeFactory.newWhileStatement((IASTDeclaration)condition, body);
		
		setOffsetAndLength(whileStatement);
		astStack.push(whileStatement);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 */
	public void consumeStatementForLoop() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTExpression expr = (IASTExpression) astStack.pop();
		Object condition = astStack.pop(); // can be an expression or a declaration
		IASTStatement initializer = (IASTStatement) astStack.pop();
		
		// bug 234463, fix for content assist to work in this case
		int TK_EOC = TK_EndOfCompletion; // TODO: change this in the grammar file
		List<IToken> tokens = parser.getRuleTokens();
		if(matchTokens(tokens, tokenMap, 
				TK_for, TK_LeftParen, TK_Completion, TK_EOC, TK_EOC, TK_EOC, TK_EOC)) {
			IASTName name = createName(tokens.get(2));
			IASTIdExpression idExpression = nodeFactory.newIdExpression(name);
			setOffsetAndLength(idExpression, offset(name), length(name));
			initializer = nodeFactory.newExpressionStatement(idExpression);
			setOffsetAndLength(initializer, offset(name), length(name));
		}
		
		
		IASTForStatement forStat;
		if(condition instanceof IASTExpression)
			forStat = nodeFactory.newForStatement(initializer, (IASTExpression)condition, expr, body);
		else // its a declaration or its null
			forStat = nodeFactory.newForStatement(initializer, (IASTDeclaration)condition, expr, body);
		
		setOffsetAndLength(forStat);
		astStack.push(forStat);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * try_block
     *     ::= 'try' compound_statement <openscope-ast> handler_seq
     */
	public void consumeStatementTryBlock() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		List<Object> handlerSeq = astStack.closeScope();
		IASTStatement body = (IASTStatement) astStack.pop();
		
		ICPPASTTryBlockStatement tryStatement = nodeFactory.newTryBlockStatement(body);
		
		for(Object handler : handlerSeq)
			tryStatement.addCatchHandler((ICPPASTCatchHandler)handler);
		
		setOffsetAndLength(tryStatement);
		astStack.push(tryStatement);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * handler
     *     ::= 'catch' '(' exception_declaration ')' compound_statement
     *       | 'catch' '(' '...' ')' compound_statement
	 */
	 public void consumeStatementCatchHandler(boolean hasEllipsis) {
		 if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		 
		 IASTStatement body = (IASTStatement) astStack.pop();
		 IASTDeclaration decl = hasEllipsis ? null : (IASTDeclaration) astStack.pop();
		 
		 ICPPASTCatchHandler catchHandler = nodeFactory.newCatchHandler(decl, body);
		 catchHandler.setIsCatchAll(hasEllipsis);

		 setOffsetAndLength(catchHandler);
	     astStack.push(catchHandler);
		 
		 if(TRACE_AST_STACK) System.out.println(astStack);
	 }
	 
	
	/**
	 * nested_name_specifier
     *     ::= class_or_namespace_name '::' nested_name_specifier_with_template
     *       | class_or_namespace_name '::' 
     *
     * nested_name_specifier_with_template
     *     ::= class_or_namespace_name_with_template '::' nested_name_specifier_with_template
     *       | class_or_namespace_name_with_template '::'
     *       
     *        
     * Creates and updates a list of the nested names on the stack.
     * Important: the names in the list are in *reverse* order,
     * this is because the actions fire in reverse order.
	 */
	@SuppressWarnings("unchecked")
	public void consumeNestedNameSpecifier(final boolean hasNested) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		LinkedList<IASTName> names;
		if(hasNested)
			names = (LinkedList<IASTName>) astStack.pop();
		else
			names = new LinkedList<IASTName>();
		
		IASTName name = (IASTName) astStack.pop();
		names.add(name);
		
		astStack.push(names);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}

	
	public void consumeNestedNameSpecifierEmpty() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		// can't use Collections.EMPTY_LIST because we need a list thats mutable
		astStack.push(new LinkedList<IASTName>());
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	
	/**
	 * The template keyword is optional but must be the leftmost token.
	 * 
	 * This just throws away the template keyword.
	 */
	public void consumeNameWithTemplateKeyword() { 
		if(TRACE_ACTIONS) 
			DebugUtil.printMethodTrace();
		
		IASTName name = (IASTName) astStack.pop();
		
		astStack.pop(); // pop the template keyword

		astStack.push(name);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}

	
	
	
	/**
	 * qualified_id
     *     ::= dcolon_opt nested_name_specifier any_name
	 */
	public void consumeQualifiedId(boolean hasTemplateKeyword) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();

		IASTName qualifiedName = subRuleQualifiedName(hasTemplateKeyword);
		astStack.push(qualifiedName);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	private IASTName createQualifiedName(LinkedList<IASTName> nestedNames, int startOffset, int endOffset, boolean startsWithColonColon) {
		return createQualifiedName(nestedNames, startOffset, endOffset, startsWithColonColon, false);
	}
	
	
	/**
	 * Creates a qualified name from a list of names (that must be in reverse order).
	 * 
	 * @param names List of name nodes in reverse order
	 */
	private IASTName createQualifiedName(LinkedList<IASTName> names, int startOffset, int endOffset, boolean startsWithColonColon, boolean endsWithColonColon) {
		if(!endsWithColonColon && !startsWithColonColon && names.size() == 1) 
			return names.getFirst(); // its actually an unqualified name

		ICPPASTQualifiedName qualifiedName = nodeFactory.newCPPQualifiedName();
		qualifiedName.setFullyQualified(startsWithColonColon);
		setOffsetAndLength(qualifiedName, startOffset, endOffset - startOffset);
		for(IASTName name : reverseIterable(names))
			qualifiedName.addName(name);
		
		if(qualifiedName instanceof CPPASTQualifiedName) {
			// compute the signature, find the tokens that make up the name
			List<IToken> nameTokens = tokenOffsetSubList(parser.getRuleTokens(), startOffset, endOffset);
			String signature = createStringRepresentation(nameTokens);
			((CPPASTQualifiedName)qualifiedName).setSignature(signature);
		}
	
		// there must be a dummy name in the AST after the last double colon, this happens with pointer to member names
		if(endsWithColonColon) {
			IASTName dummyName = nodeFactory.newName();
			setOffsetAndLength(dummyName, endOffset, 0);
			qualifiedName.addName(dummyName);
		}
		
		return qualifiedName;
	}
	
	
	private String createStringRepresentation(List<IToken> nameTokens) {
		StringBuilder sb = new StringBuilder();
		IToken prev = null;
		for(IToken t : nameTokens) {
			if(needSpaceBetween(prev, t))
				sb.append(' ');
			sb.append(t.toString());
			prev = t;
		}
		return sb.toString();
	}
	
	
	private boolean needSpaceBetween(IToken prev, IToken iter) {
		// this logic was copied from BasicTokenDuple.createCharArrayRepresentation()
		if(prev == null)
			return false;
		
		int prevKind = baseKind(prev);
		int iterKind = baseKind(iter);
		
		return  prevKind != TK_ColonColon && 
				prevKind != TK_identifier && 
				prevKind != TK_LT &&
				prevKind != TK_Tilde &&
				iterKind != TK_GT && 
				prevKind != TK_LeftBracket && 
				iterKind != TK_RightBracket && 
				iterKind != TK_ColonColon;
	}
	
	/**
	 * Consumes grammar sub-rules of the following form:
	 * 
	 * dcolon_opt nested_name_specifier_opt keyword_opt name
	 * 
	 * Where name is any rule that produces an IASTName node on the stack.
	 * Does not place the resulting node on the stack, returns it instead.
	 */
	@SuppressWarnings("unchecked")
	private IASTName subRuleQualifiedName(boolean hasOptionalKeyword) {
		IASTName lastName = (IASTName) astStack.pop();
		
		if(hasOptionalKeyword) // this is usually a template keyword and can be ignored
			astStack.pop();
		
		LinkedList<IASTName> nestedNames = (LinkedList<IASTName>) astStack.pop();
		IToken dColon = (IToken) astStack.pop();
		
		if(nestedNames.isEmpty() && dColon == null) { // then its not a qualified name
			return lastName;
		}

		nestedNames.addFirst(lastName); // the list of names is in reverse order
		
		int startOffset = dColon == null ? offset(nestedNames.getLast()) : offset(dColon);
		int endOffset = endOffset(lastName);
		
		return createQualifiedName(nestedNames, startOffset, endOffset, dColon != null);
	}
	
	
	
	/**
	 * pseudo_destructor_name
     *     ::= dcolon_opt nested_name_specifier_opt type_name '::' destructor_type_name
     *       | dcolon_opt nested_name_specifier 'template' template_id '::' destructor_type_name
     *       | dcolon_opt nested_name_specifier_opt destructor_type_name
     */
	@SuppressWarnings("unchecked")
	public void consumePsudoDestructorName(boolean hasExtraTypeName) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName destructorTypeName = (IASTName) astStack.pop();
		IASTName extraName = hasExtraTypeName ? (IASTName) astStack.pop() : null;
		LinkedList<IASTName> nestedNames = (LinkedList<IASTName>) astStack.pop();
		IToken dColon = (IToken) astStack.pop();
		
		if(hasExtraTypeName)
			nestedNames.addFirst(extraName);
		
		nestedNames.addFirst(destructorTypeName);
		
		int startOffset = dColon == null ? offset(nestedNames.getLast()) : offset(dColon);
		int endOffset = endOffset(destructorTypeName);
		IASTName qualifiedName = createQualifiedName(nestedNames, startOffset, endOffset, dColon != null);
		
		setOffsetAndLength(qualifiedName);
		astStack.push(qualifiedName);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * asm_definition
     *     ::= 'asm' '(' 'stringlit' ')' ';'
	 */
	public void consumeDeclarationASM() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		String s = parser.getRuleTokens().get(2).toString();
		IASTASMDeclaration asm = nodeFactory.newASMDeclaration(s);
		
		setOffsetAndLength(asm);
		astStack.push(asm);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	/**
	 * namespace_alias_definition
     *     ::= 'namespace' 'identifier' '=' dcolon_opt nested_name_specifier_opt namespace_name ';'
     */
	public void consumeNamespaceAliasDefinition() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();

		IASTName qualifiedName = subRuleQualifiedName(false);
		
		IASTName alias = createName(parser.getRuleTokens().get(1));
		ICPPASTNamespaceAlias namespaceAlias = nodeFactory.newNamespaceAlias(alias, qualifiedName);
		
		setOffsetAndLength(namespaceAlias);
		astStack.push(namespaceAlias);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * using_declaration
     *     ::= 'using' typename_opt dcolon_opt nested_name_specifier_opt unqualified_id ';'
	 */
	public void consumeUsingDeclaration() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName qualifiedName = subRuleQualifiedName(false);
		boolean hasTypenameKeyword = astStack.pop() == PLACE_HOLDER;
		
		ICPPASTUsingDeclaration usingDeclaration = nodeFactory.newUsingDeclaration(qualifiedName);
		usingDeclaration.setIsTypename(hasTypenameKeyword);
		
		setOffsetAndLength(usingDeclaration);
		astStack.push(usingDeclaration);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * using_directive
     *     ::= 'using' 'namespace' dcolon_opt nested_name_specifier_opt namespace_name ';'
	 */
	public void consumeUsingDirective() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName qualifiedName = subRuleQualifiedName(false);
		
		ICPPASTUsingDirective usingDirective = nodeFactory.newUsingDirective(qualifiedName);
		setOffsetAndLength(usingDirective);
		astStack.push(usingDirective);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * linkage_specification
     *     ::= 'extern' 'stringlit' '{' <openscope-ast> declaration_seq_opt '}'
     *       | 'extern' 'stringlit' <openscope-ast> declaration
	 */
	public void consumeLinkageSpecification() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		String name = parser.getRuleTokens().get(1).toString();
		ICPPASTLinkageSpecification linkageSpec = nodeFactory.newLinkageSpecification(name);
		
		for(Object declaration : astStack.closeScope())
			linkageSpec.addDeclaration((IASTDeclaration)declaration);
			
		setOffsetAndLength(linkageSpec);
		astStack.push(linkageSpec);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	/**
	 * original_namespace_definition
     *     ::= 'namespace' identifier_name '{' <openscope-ast> declaration_seq_opt '}'
     *    
     * extension_namespace_definition
     *     ::= 'namespace' original_namespace_name '{' <openscope-ast> declaration_seq_opt '}'
     *
     * unnamed_namespace_definition
     *     ::= 'namespace' '{' <openscope-ast> declaration_seq_opt '}'
	 */
	public void consumeNamespaceDefinition(boolean hasName) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		List<Object> declarations = astStack.closeScope();
		IASTName namespaceName = hasName ? (IASTName)astStack.pop() : nodeFactory.newName();
		
		ICPPASTNamespaceDefinition definition = nodeFactory.newNamespaceDefinition(namespaceName);
		
		for(Object declaration : declarations)
			definition.addDeclaration((IASTDeclaration)declaration);
		
		setOffsetAndLength(definition);
		astStack.push(definition);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * template_declaration
     *     ::= export_opt 'template' '<' <openscope-ast> template_parameter_list '>' declaration
	 */
	public void consumeTemplateDeclaration() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTDeclaration declaration = (IASTDeclaration) astStack.pop();
		
		// For some reason ambiguous declarators cause bugs when they are a part of a template declaration.
		// But it shouldn't be ambiguous anyway, so just throw away the ambiguity node.
		resolveAmbiguousDeclaratorsToFunction(declaration);
		
		ICPPASTTemplateDeclaration templateDeclaration = nodeFactory.newTemplateDeclaration(declaration);
		
		for(Object param : astStack.closeScope())
			templateDeclaration.addTemplateParamter((ICPPASTTemplateParameter)param);

		boolean hasExportKeyword = astStack.pop() == PLACE_HOLDER;
		templateDeclaration.setExported(hasExportKeyword);
		
		setOffsetAndLength(templateDeclaration);
		astStack.push(templateDeclaration);

		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * If we know that a declarator must be a function declarator then we can resolve
	 * the ambiguity without resorting to binding resolution.
	 */
	private static void resolveAmbiguousDeclaratorsToFunction(IASTDeclaration declaration) {
		if(declaration instanceof IASTSimpleDeclaration) {
			for(IASTDeclarator declarator : ((IASTSimpleDeclaration)declaration).getDeclarators()) {
				if(declarator instanceof CPPASTAmbiguousDeclarator) {
					IASTAmbiguityParent owner = (IASTAmbiguityParent) declaration;
					CPPASTAmbiguousDeclarator ambiguity = (CPPASTAmbiguousDeclarator)declarator;
					owner.replace(ambiguity, ambiguity.getNodes()[0]);
				}
			}
		}
	}
	
	
    /**
     * explicit_instantiation
     *    ::= 'template' declaration
     */
	public void consumeTemplateExplicitInstantiation() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTDeclaration declaration = (IASTDeclaration) astStack.pop();
		ICPPASTExplicitTemplateInstantiation instantiation = nodeFactory.newExplicitTemplateInstantiation(declaration);
		
		setOffsetAndLength(instantiation);
		astStack.push(instantiation);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * explicit_specialization
     *     ::= 'template' '<' '>' declaration
     */
	public void consumeTemplateExplicitSpecialization() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTDeclaration declaration = (IASTDeclaration) astStack.pop();
		ICPPASTTemplateSpecialization specialization = nodeFactory.newTemplateSpecialization(declaration);
		
		setOffsetAndLength(specialization);
		astStack.push(specialization);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * Sets a token specifier.
	 * Needs to be overrideable for new decl spec keywords.
	 * 
	 * @param token Allows subclasses to override this method and use any
	 * object to determine how to set a specifier.
	 */
	protected void setSpecifier(ICPPASTDeclSpecifier node, IToken token) {
		int kind = baseKind(token);
		switch(kind){
			case TK_typedef:  node.setStorageClass(IASTDeclSpecifier.sc_typedef);    return;
			case TK_extern:   node.setStorageClass(IASTDeclSpecifier.sc_extern);     return;
			case TK_static:   node.setStorageClass(IASTDeclSpecifier.sc_static);     return;
			case TK_auto:     node.setStorageClass(IASTDeclSpecifier.sc_auto);       return;
			case TK_register: node.setStorageClass(IASTDeclSpecifier.sc_register);   return;
			case TK_mutable:  node.setStorageClass(ICPPASTDeclSpecifier.sc_mutable); return;
			
			case TK_inline:   node.setInline(true);   return;
			case TK_const:    node.setConst(true);    return;
			case TK_friend:   node.setFriend(true);   return;
			case TK_virtual:  node.setVirtual(true);  return;
			case TK_volatile: node.setVolatile(true); return;
			case TK_explicit: node.setExplicit(true); return;
		}
		
		if(node instanceof ICPPASTSimpleDeclSpecifier) {
			ICPPASTSimpleDeclSpecifier n = (ICPPASTSimpleDeclSpecifier) node;
			switch(kind) {
				case TK_void:     n.setType(IASTSimpleDeclSpecifier.t_void);       return;
				case TK_char:     n.setType(IASTSimpleDeclSpecifier.t_char);       return;
				case TK_int:      n.setType(IASTSimpleDeclSpecifier.t_int);        return;
				case TK_float:    n.setType(IASTSimpleDeclSpecifier.t_float);      return;
				case TK_double:   n.setType(IASTSimpleDeclSpecifier.t_double);     return;
				case TK_bool:     n.setType(ICPPASTSimpleDeclSpecifier.t_bool);    return;
				case TK_wchar_t:  n.setType(ICPPASTSimpleDeclSpecifier.t_wchar_t); return;
				
				case TK_signed:   n.setSigned(true);   return;
				case TK_unsigned: n.setUnsigned(true); return;
				case TK_long:     n.setLong(true);     return;
				case TK_short:    n.setShort(true);    return;
			}
		}
		
	}
	
	
	public void consumeDeclarationSpecifiersSimple() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		ICPPASTDeclSpecifier declSpec = nodeFactory.newCPPSimpleDeclSpecifier();
		
		for(Object token : astStack.closeScope())
			setSpecifier(declSpec, (IToken)token);
		
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}

	
	/**
	 * TODO: maybe move this into the superclass
	 */
	public void consumeDeclarationSpecifiersComposite() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		List<Object> topScope = astStack.closeScope();
		
		// There's already a composite or elaborated or enum type specifier somewhere on the stack, find it.
		ICPPASTDeclSpecifier declSpec = findFirstAndRemove(topScope, ICPPASTDeclSpecifier.class);
		
		// now apply the rest of the specifiers
		for(Object token : topScope)
			setSpecifier(declSpec, (IToken)token);
		
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	
//	/**
//	 * declaration_specifiers ::=  <openscope> type_name_declaration_specifiers
//	 */
	public void consumeDeclarationSpecifiersTypeName() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		List<Object> topScope = astStack.closeScope();
		// There's a name somewhere on the stack, find it		
		IASTName typeName = findFirstAndRemove(topScope, IASTName.class);
		
		// TODO what does the second argument mean?
		ICPPASTNamedTypeSpecifier declSpec = nodeFactory.newCPPNamedTypeSpecifier(typeName, false);
		
		// now apply the rest of the specifiers
		for(Object token : topScope)
			setSpecifier(declSpec, (IToken)token);

		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * elaborated_type_specifier
     *     ::= class_keyword dcolon_opt nested_name_specifier_opt identifier_name
     *       | class_keyword dcolon_opt nested_name_specifier_opt template_opt template_id_name
     *       | 'enum' dcolon_opt nested_name_specifier_opt identifier_name      
	 */
	public void consumeTypeSpecifierElaborated(boolean hasOptionalTemplateKeyword) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = subRuleQualifiedName(hasOptionalTemplateKeyword);
		int kind = getElaboratedTypeSpecifier(parser.getLeftIToken());
		
		IASTElaboratedTypeSpecifier typeSpecifier = nodeFactory.newElaboratedTypeSpecifier(kind, name);
		
		setOffsetAndLength(typeSpecifier);
		astStack.push(typeSpecifier);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	private int getElaboratedTypeSpecifier(IToken token) {
		int kind = baseKind(token);
		switch(kind) {
			default: assert false : "wrong token kind: " + kind; //$NON-NLS-1$
			case TK_struct: return IASTElaboratedTypeSpecifier.k_struct;
			case TK_union:  return IASTElaboratedTypeSpecifier.k_union;
			case TK_enum:   return IASTElaboratedTypeSpecifier.k_enum;
			case TK_class:  return ICPPASTElaboratedTypeSpecifier.k_class;   
		}
	}
	
	
	
	/**
	 * simple_declaration
     *     ::= declaration_specifiers_opt <openscope-ast> init_declarator_list_opt ';'
	 */
	public void consumeDeclarationSimple(boolean hasDeclaratorList) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		List<Object> declarators = hasDeclaratorList ? astStack.closeScope() : Collections.emptyList();
		ICPPASTDeclSpecifier declSpecifier = (ICPPASTDeclSpecifier) astStack.pop(); // may be null
		List<IToken> ruleTokens = parser.getRuleTokens();
		IToken nameToken = null;
		
		
		// do not generate nodes for extra EOC tokens
		if(matchTokens(ruleTokens, tokenMap, TK_EndOfCompletion)) {
			return;
		}
		
		// In the case that a single completion token is parsed then it needs
		// to be interpreted as a named type specifier for content assist to work.
		else if(matchTokens(ruleTokens, tokenMap, TK_Completion, TK_EndOfCompletion)) {
			IASTName name = createName(parser.getLeftIToken());
			declSpecifier = nodeFactory.newCPPNamedTypeSpecifier(name, false);
			setOffsetAndLength(declSpecifier, offset(name), length(name));
			declarators = Collections.emptyList(); // throw away the bogus declarator
		}
		
		// can happen if implicit int is used
		else if(declSpecifier == null) { 
			declSpecifier = nodeFactory.newCPPSimpleDeclSpecifier();
			setOffsetAndLength(declSpecifier, parser.getLeftIToken().getStartOffset(), 0);
		}
		
		// bug 80171, check for situation similar to: static var;
		// this will get parsed wrong, the following is a hack to rebuild the AST as it should have been parsed
		else if(declarators.isEmpty() && 
		   declSpecifier instanceof ICPPASTNamedTypeSpecifier &&
		   ruleTokens.size() >= 2 &&
		   baseKind(nameToken = ruleTokens.get(ruleTokens.size() - 2)) == TK_identifier) {
			
			declSpecifier = nodeFactory.newCPPSimpleDeclSpecifier();
			for(IToken t : ruleTokens.subList(0, ruleTokens.size()-1))
				setSpecifier(declSpecifier, t);
			
			int offset = offset(parser.getLeftIToken());
			int length = endOffset(ruleTokens.get(ruleTokens.size()-2)) - offset;
			setOffsetAndLength(declSpecifier, offset, length);
			
			IASTName name = createName(nameToken);
			IASTDeclarator declarator = nodeFactory.newDeclarator(name);
			setOffsetAndLength(declarator, nameToken);
			declarators.add(declarator);
		}
		

		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpecifier);
		setOffsetAndLength(declaration);
		for(Object declarator : declarators)
			declaration.addDeclarator((IASTDeclarator)declarator);
		
		
		// simple ambiguity resolutions
//		if(declSpecifier.isFriend())
//			resolveAmbiguousDeclaratorsToFunction(declaration);
//		
//		if(declSpecifier instanceof IASTSimpleDeclSpecifier) {
//			IASTSimpleDeclSpecifier simple = (IASTSimpleDeclSpecifier) declSpecifier;
//			if(simple.getType() == IASTSimpleDeclSpecifier.t_void && declaration.getDeclarators()[0].getPointerOperators().length == 0)
//				resolveAmbiguousDeclaratorsToFunction(declaration);
//			
//		}
		
		
		
		astStack.push(declaration);

		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	

	
	
	public void consumeInitDeclaratorComplete() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();

		// Don't do disambiguation when parsing for content assist,
		// trust me this makes things work out a lot better.
		if(completionNode != null)
			return;
		
		IASTDeclarator declarator = (IASTDeclarator) astStack.peek();
		if(!(declarator instanceof IASTFunctionDeclarator))
			return;
		
		IParser secondaryParser = getNoFunctionDeclaratorParser(); 
		IASTNode alternateDeclarator = runSecondaryParser(secondaryParser);
	
		if(alternateDeclarator == null || alternateDeclarator instanceof IASTProblemDeclaration)
			return;
		
		astStack.pop();
		// TODO create node factory method for this
		IASTNode ambiguityNode = new CPPASTAmbiguousDeclarator(declarator, (IASTDeclarator)alternateDeclarator);

		setOffsetAndLength(ambiguityNode);
		astStack.push(ambiguityNode); 
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	/**
	 * visibility_label
     *     ::= access_specifier_keyword ':'
	 */
	public void consumeVisibilityLabel() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();

		IToken specifier = (IToken)astStack.pop();
		int visibility = getAccessSpecifier(specifier);
		ICPPASTVisibilityLabel visibilityLabel = nodeFactory.newVisibilityLabel(visibility);
		setOffsetAndLength(visibilityLabel);
		astStack.push(visibilityLabel);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	private int getAccessSpecifier(IToken token) {
		int kind = baseKind(token);
		switch(kind) {
			default: assert false : "wrong token kind: " + kind; //$NON-NLS-1$
			case TK_private:   return ICPPASTVisibilityLabel.v_private;
			case TK_public:    return ICPPASTVisibilityLabel.v_public;    
			case TK_protected: return ICPPASTVisibilityLabel.v_protected;
		}
	}
	
	
	/**
	 * base_specifier
     *     ::= dcolon_opt nested_name_specifier_opt class_name
     *       | 'virtual' access_specifier_keyword_opt dcolon_opt nested_name_specifier_opt class_name
     *       | access_specifier_keyword 'virtual' dcolon_opt nested_name_specifier_opt class_name
     *       | access_specifier_keyword dcolon_opt nested_name_specifier_opt class_name
	 */
	public void consumeBaseSpecifier(boolean hasAccessSpecifier, boolean isVirtual) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = subRuleQualifiedName(false);
		
		int visibility = 0; // this is the default value that the DOM parser uses
		if(hasAccessSpecifier) {
			IToken accessSpecifierToken = (IToken) astStack.pop();
			if(accessSpecifierToken != null)
				visibility = getAccessSpecifier(accessSpecifierToken);
		}
		
		ICPPASTBaseSpecifier baseSpecifier = nodeFactory.newBaseSpecifier(name, visibility, isVirtual);
		setOffsetAndLength(baseSpecifier);
		astStack.push(baseSpecifier);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
		
	
	/**
	 * Gets the current token and places it on the stack for later consumption.
	 */
	public void consumeAccessKeywordToken() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		astStack.push(parser.getRightIToken());
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * class_specifier
     *     ::= class_head '{' <openscope-ast> member_declaration_list_opt '}'
     */
	public void consumeClassSpecifier() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		List<Object> declarations = astStack.closeScope();
		
		// the class specifier is created by the rule for class_head
		IASTCompositeTypeSpecifier classSpecifier = (IASTCompositeTypeSpecifier) astStack.pop();
		
		for(Object declaration : declarations) {
			classSpecifier.addMemberDeclaration((IASTDeclaration)declaration);
		}
		
		setOffsetAndLength(classSpecifier);
		astStack.push(classSpecifier);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	/**
     * class_head
     *     ::= class_keyword identifier_name_opt <openscope-ast> base_clause_opt
     *       | class_keyword template_id <openscope-ast> base_clause_opt
     *       | class_keyword nested_name_specifier identifier_name <openscope-ast> base_clause_opt
     *       | class_keyword nested_name_specifier template_id <openscope-ast> base_clause_opt
	 */
	@SuppressWarnings("unchecked")
	public void consumeClassHead(boolean hasNestedNameSpecifier) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		int key = getCompositeTypeSpecifier(parser.getLeftIToken());
		List<Object> baseSpecifiers = astStack.closeScope();
		// may be null, but if it is then hasNestedNameSpecifier == false
		IASTName className = (IASTName) astStack.pop();
		
		if(hasNestedNameSpecifier) {
			LinkedList<IASTName> nestedNames = (LinkedList<IASTName>) astStack.pop();
			nestedNames.addFirst(className);
			int startOffset = offset(nestedNames.getLast());
			int endOffset = endOffset(className);
			className = createQualifiedName(nestedNames, startOffset, endOffset, false);
		}

		if(className == null)
			className = nodeFactory.newName();
		
		ICPPASTCompositeTypeSpecifier classSpecifier = nodeFactory.newCPPCompositeTypeSpecifier(key, className);
		
		for(Object base : baseSpecifiers)
			classSpecifier.addBaseSpecifier((ICPPASTBaseSpecifier)base);
		
		// the offset and length are set in consumeClassSpecifier()
		astStack.push(classSpecifier);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	private int getCompositeTypeSpecifier(IToken token) {
		final int kind = baseKind(token);
		switch(kind) {
			default: assert false : "wrong token kind: " + kind; //$NON-NLS-1$
			case TK_struct: return IASTCompositeTypeSpecifier.k_struct;
			case TK_union:  return IASTCompositeTypeSpecifier.k_union;
			case TK_class:  return ICPPASTCompositeTypeSpecifier.k_class;   
		}
	}
	
	
	/**
	 * ptr_operator
     *     ::= '*' <openscope-ast> cv_qualifier_seq_opt
     */
    public void consumePointer() {
    	if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
    	
    	IASTPointer pointer = nodeFactory.newCPPPointer();
    	List<Object> tokens = astStack.closeScope();
    	addCVQualifiersToPointer(pointer, tokens);
		setOffsetAndLength(pointer);
		astStack.push(pointer);
    	
    	if(TRACE_AST_STACK) System.out.println(astStack);
    }
    
    
    private void addCVQualifiersToPointer(IASTPointer pointer, List<Object> tokens) {
    	for(Object t : tokens) {
    		IToken token = (IToken) t;
			int kind = baseKind(token);
			switch(kind) {
				default : assert false;
				case TK_const:    pointer.setConst(true);    break;
				case TK_volatile: pointer.setVolatile(true); break;
			}
		}
    }
    
    /**
	 * ptr_operator
     *     ::= '&'
     */ 
    public void consumeReferenceOperator() {
        if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
    	
        ICPPASTReferenceOperator referenceOperator = nodeFactory.newReferenceOperator();
        setOffsetAndLength(referenceOperator);
		astStack.push(referenceOperator);
		
    	if(TRACE_AST_STACK) System.out.println(astStack);
    }
    
    
    /**
	 * ptr_operator
     *     ::= dcolon_opt nested_name_specifier '*' <openscope-ast> cv_qualifier_seq_opt
     */
     @SuppressWarnings("unchecked")
	public void consumePointerToMember() {
    	 if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
     	
    	 List<Object> qualifiers = astStack.closeScope();
    	 LinkedList<IASTName> nestedNames = (LinkedList<IASTName>) astStack.pop();
    	 IToken dColon = (IToken) astStack.pop();
    	 
    	 int startOffset = dColon == null ? offset(nestedNames.getLast()) : offset(dColon);
    	 int endOffset   = endOffset(nestedNames.getFirst()); // temporary
    	 
    	 // find the last double colon by searching for it
    	 for(IToken t : reverseIterable(parser.getRuleTokens())) {
    		 if(baseKind(t) == TK_ColonColon) {
    			 endOffset = endOffset(t);
    			 break;
    		 }
    	 }
    	 
    	 IASTName name = createQualifiedName(nestedNames, startOffset, endOffset, dColon != null, true);
    	 
     	 ICPPASTPointerToMember pointer = nodeFactory.newPointerToMember(name);
     	 addCVQualifiersToPointer(pointer, qualifiers);
     	 setOffsetAndLength(pointer);
		 astStack.push(pointer);
     	
     	 if(TRACE_AST_STACK) System.out.println(astStack);
     }

     
     
     /**
      * initializer
      *     ::= '(' expression_list ')'
      */
     public void consumeInitializerConstructor() {
    	 if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
    	 
    	 IASTExpression expression = (IASTExpression) astStack.pop();
    	 ICPPASTConstructorInitializer initializer = nodeFactory.newConstructorInitializer(expression);
    	 setOffsetAndLength(initializer);
		 astStack.push(initializer);
		 
    	 if(TRACE_AST_STACK) System.out.println(astStack);
     }
     
     
    /**
 	 * function_direct_declarator
     *     ::= basic_direct_declarator '(' <openscope-ast> parameter_declaration_clause ')' 
     *         <openscope-ast> cv_qualifier_seq_opt <openscope-ast> exception_specification_opt
 	 */
 	public void consumeDirectDeclaratorFunctionDeclarator(boolean hasDeclarator) {
 		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
 		
 		IASTName name = nodeFactory.newName();
 		ICPPASTFunctionDeclarator declarator = nodeFactory.newCPPFunctionDeclarator(name);
 		
 		for(Object typeId : astStack.closeScope()) {
 			declarator.addExceptionSpecificationTypeId((IASTTypeId) typeId);
 		}
 		
 		for(Object token : astStack.closeScope()) {
 			int kind = baseKind((IToken)token);
 			switch(kind) {
 				default: assert false : "wrong token kind: " + kind; //$NON-NLS-1$
 				case TK_const:    declarator.setConst(true); break;
 				case TK_volatile: declarator.setVolatile(true); break;
 			}
 		}
 		
 		boolean isVarArgs = astStack.pop() == PLACE_HOLDER;
 		declarator.setVarArgs(isVarArgs);
 			
 		for(Object o : astStack.closeScope()) {
 			declarator.addParameterDeclaration((IASTParameterDeclaration)o);
 		}
 		
 		if(hasDeclarator) {
 			int endOffset = endOffset(parser.getRightIToken());
 			addFunctionModifier(declarator, endOffset);
 		}
 		else {
 			setOffsetAndLength(declarator);
			astStack.push(declarator);
 		}
 	}
 
 	
 	/**
 	 * Consume an empty bracketed abstract declarator.
 	 */
 	public void consumeAbstractDeclaratorEmpty() {
 		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
 		
 		IASTName name = nodeFactory.newName();
 		setOffsetAndLength(name, offset(parser.getLeftIToken())+1, 0);
 		IASTDeclarator declarator = nodeFactory.newDeclarator(name);
 		setOffsetAndLength(declarator);
 		astStack.push(declarator);
 		
 		if(TRACE_AST_STACK) System.out.println(astStack);
 	}
 	
 	
 	/**
 	 * mem_initializer
     *     ::= mem_initializer_id '(' expression_list_opt ')'
 	 */
 	public void consumeConstructorChainInitializer() {
 		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
 		
 		IASTExpression expr = (IASTExpression) astStack.pop();
 		IASTName name = (IASTName) astStack.pop();
 		ICPPASTConstructorChainInitializer initializer = nodeFactory.newConstructorChainInitializer(name, expr);
 		setOffsetAndLength(initializer);
		astStack.push(initializer);
 		
 		if(TRACE_AST_STACK) System.out.println(astStack);
 	}
 	
 	
 	
 	/**
 	 * function_definition
     *     ::= declaration_specifiers_opt function_direct_declarator 
     *         <openscope-ast> ctor_initializer_list_opt function_body
     *         
     *       | declaration_specifiers_opt function_direct_declarator 
     *         'try' <openscope-ast> ctor_initializer_list_opt function_body <openscope-ast> handler_seq
     *         
 	 */
 	public void consumeFunctionDefinition(boolean isTryBlockDeclarator) {
 		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();

 		List<Object> handlers = isTryBlockDeclarator ? astStack.closeScope() : Collections.emptyList();
 		IASTCompoundStatement body = (IASTCompoundStatement) astStack.pop();
 		List<Object> initializers = astStack.closeScope();
 		ICPPASTFunctionDeclarator declarator = (ICPPASTFunctionDeclarator) astStack.pop();
 		IASTDeclSpecifier declSpec = (IASTDeclSpecifier) astStack.pop(); // may be null
 		
 		if(declSpec == null) { // can happen if implicit int is used
 			declSpec = nodeFactory.newSimpleDeclSpecifier();
			setOffsetAndLength(declSpec, parser.getLeftIToken().getStartOffset(), 0);
		}
 		
 		if(isTryBlockDeclarator) {
 		    // perform a shallow copy 
 			ICPPASTFunctionTryBlockDeclarator tryBlockDeclarator = nodeFactory.newFunctionTryBlockDeclarator(declarator.getName());
 			tryBlockDeclarator.setConst(declarator.isConst());
 			tryBlockDeclarator.setVolatile(declarator.isVolatile());
 			tryBlockDeclarator.setPureVirtual(declarator.isPureVirtual());
 			tryBlockDeclarator.setVarArgs(declarator.takesVarArgs());
 			for(IASTParameterDeclaration parameter : declarator.getParameters()) {
 				tryBlockDeclarator.addParameterDeclaration(parameter);
 			}
 			for(IASTTypeId exception : declarator.getExceptionSpecification()) {
 				tryBlockDeclarator.addExceptionSpecificationTypeId(exception);
 			}
 			for(Object handler : handlers) {
 				tryBlockDeclarator.addCatchHandler((ICPPASTCatchHandler)handler);
 	 		}
 			
 			declarator = tryBlockDeclarator;
 		}
 		
 		
 		if(initializers != null && !initializers.isEmpty()) {
 			for(Object initializer : initializers)
 	 			declarator.addConstructorToChain((ICPPASTConstructorChainInitializer)initializer);
 			
 			// recalculate the length of the declarator to include the initializers
 			IASTNode lastInitializer = (IASTNode)initializers.get(initializers.size()-1);
 			int offset = offset(declarator);
 			int length = endOffset(lastInitializer) - offset;
 			setOffsetAndLength(declarator, offset, length);
 		}

 		IASTFunctionDefinition definition = nodeFactory.newFunctionDefinition(declSpec, declarator, body);
 		
 		setOffsetAndLength(definition);
		astStack.push(definition);
 			
 		if(TRACE_AST_STACK) System.out.println(astStack);
 	}
 	
 	
 	/**
 	 * member_declaration
 	 *     ::= dcolon_opt nested_name_specifier template_opt unqualified_id_name ';'
 	 */
 	public void consumeMemberDeclarationQualifiedId() {
 		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
 		
 		IASTName qualifiedId = subRuleQualifiedName(true);
 		IASTDeclarator declarator = nodeFactory.newDeclarator(qualifiedId);
 		setOffsetAndLength(declarator);
 		// there has to be an empty specifier or... kaboom!
 		IASTDeclSpecifier emptySpecifier = nodeFactory.newSimpleDeclSpecifier();
		setOffsetAndLength(emptySpecifier, parser.getLeftIToken().getStartOffset(), 0);
 		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(emptySpecifier);
 		setOffsetAndLength(declaration);
 		declaration.addDeclarator(declarator);
 		astStack.push(declaration);
 		
 		if(TRACE_AST_STACK) System.out.println(astStack);
 	}
 	
 	
 	/**
 	 * member_declarator
     *     ::= declarator constant_initializer
 	 */
 	
    public void consumeMemberDeclaratorWithInitializer() {
    	if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
    	
    	IASTInitializerExpression initializer = (IASTInitializerExpression) astStack.pop();
    	IASTDeclarator declarator = (IASTDeclarator) astStack.peek();
    	setOffsetAndLength(declarator);
    	
    	if(declarator instanceof ICPPASTFunctionDeclarator) {
    		IASTExpression expr = initializer.getExpression();
    		if(expr instanceof IASTLiteralExpression && "0".equals(expr.toString())) { //$NON-NLS-1$
    			((ICPPASTFunctionDeclarator)declarator).setPureVirtual(true);
    			return;
    		}
    	}
    	
    	declarator.setInitializer(initializer);
    	
    	if(TRACE_AST_STACK) System.out.println(astStack);
    }

    
    /**
     * type_parameter
     *     ::= 'class' identifier_name_opt -- simple type template parameter     
     *       | 'class' identifier_name_opt '=' type_id
     *       | 'typename' identifier_name_opt
     *       | 'typename' identifier_name_opt '=' type_id
     */
    public void consumeSimpleTypeTemplateParameter(boolean hasTypeId) {
    	if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
    	
    	IASTTypeId typeId = hasTypeId ? (IASTTypeId)astStack.pop() : null;
    	
    	IASTName name = (IASTName)astStack.pop();
    	if(name == null)
    		name = nodeFactory.newName();
    	
    	int type = getTemplateParameterType(parser.getLeftIToken()); 
    	
    	ICPPASTSimpleTypeTemplateParameter templateParameter = nodeFactory.newSimpleTypeTemplateParameter(type, name, typeId);
    	
    	setOffsetAndLength(templateParameter);
		astStack.push(templateParameter);
		
    	if(TRACE_AST_STACK) System.out.println(astStack);
    }
    
    private int getTemplateParameterType(IToken token) {
    	int kind = baseKind(token);
    	switch(kind) {
    		default: assert false : "wrong token kind: " + kind; //$NON-NLS-1$
    		case TK_class:    return ICPPASTSimpleTypeTemplateParameter.st_class;
    		case TK_typename: return ICPPASTSimpleTypeTemplateParameter.st_typename;
    	}
    }
    
    
    /**
     * Simple type template parameters using the 'class' keyword are being parsed
     * wrong due to an ambiguity between type_parameter and parameter_declaration.
     * 
     * eg) template <class T>
     * 
     * The 'class T' part is being parsed as an elaborated type specifier instead
     * of a simple type template parameter.
     * 
     * This method detects the incorrect parse, throws away the incorrect AST fragment,
     * and replaces it with the correct AST fragment.
     * 
     * Yes its a hack.
     */
    public void consumeTemplateParamterDeclaration() {
    	if(TRACE_ACTIONS) DebugUtil.printMethodTrace();

    	IParser typeParameterParser = getTemplateTypeParameterParser();
    	IASTNode alternate = runSecondaryParser(typeParameterParser);
    	
		if(alternate == null || alternate instanceof IASTProblemDeclaration)
			return;
		
		astStack.pop(); // throw away the incorrect AST
		astStack.push(alternate);  // replace it with the correct AST
    	
    	if(TRACE_AST_STACK) System.out.println(astStack);
    }

    
    
    /**
     * type_parameter
     *     ::= 'template' '<' <openscope-ast> template_parameter_list '>' 'class' identifier_name_opt
     *       | 'template' '<' <openscope-ast> template_parameter_list '>' 'class' identifier_name_opt '=' id_expression
     * @param hasIdExpr
     */
    public void consumeTemplatedTypeTemplateParameter(boolean hasIdExpr) {
    	if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
    	
    	IASTExpression idExpression = hasIdExpr ? (IASTExpression)astStack.pop() : null;
    	IASTName name = (IASTName) astStack.pop();
    	
    	ICPPASTTemplatedTypeTemplateParameter templateParameter = nodeFactory.newTemplatedTypeTemplateParameter(name, idExpression);
    	
    	for(Object param : astStack.closeScope())
    		templateParameter.addTemplateParamter((ICPPASTTemplateParameter)param);
    	
    	setOffsetAndLength(templateParameter);
		astStack.push(templateParameter);
    	
    	if(TRACE_AST_STACK) System.out.println(astStack);
    }


	
    
    
}




























