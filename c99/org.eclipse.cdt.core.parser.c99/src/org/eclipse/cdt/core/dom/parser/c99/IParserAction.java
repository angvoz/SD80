package org.eclipse.cdt.core.dom.parser.c99;

import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;

public interface IParserAction {

	
	void openASTScope();
	
	void beforeConsume();
	
	void consumeToken();
	
	/**
	 * Consumes a name from an identifier.
	 * Used by several grammar rules.
	 */
	void consumeName();

	/**
	 * constant ::= 'integer' | 'floating' | 'charconst' | 'stringlit'
	 * 
	 * @param kind One of the kind flags from IASTLiteralExpression
	 * @see IASTLiteralExpression
	 */
	void consumeExpressionConstant(int kind);

	/**
	 * primary_expression ::= ident
	 */
	void consumeExpressionID();

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
	void consumeExpressionBinaryOperator(int op);

	/**
	 * conditional_expression ::= logical_OR_expression '?' expression ':' conditional_expression
	 */
	void consumeExpressionConditional();

	/**
	 * postfix_expression ::= postfix_expression '[' expression ']'
	 */
	void consumeExpressionArraySubscript();

	/**
	 * postfix_expression ::= postfix_expression '(' argument_expression_list ')'
	 * postfix_expression ::= postfix_expression '(' ')'
	 */
	void consumeExpressionFunctionCall(boolean hasArgs);

	/**
	 * postfix_expression ::= postfix_expression '.' ident
	 * postfix_expression ::= postfix_expression '->' ident
	 */
	void consumeExpressionFieldReference(boolean isPointerDereference);

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
	void consumeExpressionUnaryOperator(int operator);

	/**
	 * unary_operation ::= 'sizeof' '(' type_name ')'
	 * 
	 * @see consumeExpressionUnaryOperator For the other use of sizeof
	 */
	void consumeExpressionUnarySizeofTypeName();

	/**
	 * postfix_expression ::= '(' type_name ')' '{' <openscope> initializer_list '}'
	 * postfix_expression ::= '(' type_name ')' '{' <openscope> initializer_list ',' '}'            
	 */
	void consumeExpressionTypeIdInitializer();

	/**
	 * cast_expression ::= '(' type_name ')' cast_expression
	 */
	void consumeExpressionCast();

	/**
	 * primary_expression ::= '(' expression ')'
	 * 
	 * TODO: should bracketed expressions cause a new node in the AST? whats the point?
	 */
	void consumeExpressionBracketed();

	/**
	 * expression ::= expression_list
	 * 
	 * In the case that an expression list consists of a single expression
	 * then discard the list.
	 */
	void consumeExpression();

	/**
	 * expression_list
	 *     ::= assignment_expression
	 *       | expression_list ',' assignment_expression 
	 */
	void consumeExpressionList(boolean baseCase);

	/**
	 * type_name ::= specifier_qualifier_list
	 *             | specifier_qualifier_list abstract_declarator
	 */
	void consumeTypeId(boolean hasDeclarator);

	/**
	 * declarator ::= <openscope> pointer direct_declarator
	 *              
	 * abstract_declarator  -- a declarator that does not include an identifier
	 *     ::= <openscope> pointer
	 *       | <openscope> pointer direct_abstract_declarator 
	 */
	void consumeDeclaratorWithPointer(boolean hasDeclarator);

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
	void consumeDirectDeclaratorModifiedArrayModifier(boolean isStatic,
			boolean isVarSized, boolean hasTypeQualifierList,
			boolean hasAssignmentExpr);

	/**
	 *  array_modifier 
	 *      ::= '[' ']' 
	 *        | '[' assignment_expression ']'
	 */
	void consumeDirectDeclaratorArrayModifier(boolean hasAssignmentExpr);

	/**
	 * direct_declarator ::= direct_declarator array_modifier
	 * 
	 * consume the direct_declarator part and add the array modifier
	 */
	void consumeDirectDeclaratorArrayDeclarator();

	/**
	 * direct_declarator ::= '(' declarator ')'
	 */
	void consumeDirectDeclaratorBracketed();

	/**
	 * init_declarator ::= declarator '=' initializer
	 */
	void consumeDeclaratorWithInitializer();

	/**
	 * direct_declarator ::= 'identifier'
	 */
	void consumeDirectDeclaratorIdentifier();

	/**
	 * direct_declarator ::= direct_declarator '(' <openscope> parameter_type_list ')'
	 * direct_declarator ::= direct_declarator '(' ')'
	 */
	void consumeDirectDeclaratorFunctionDeclarator(boolean hasParameters);

	/**
	 * direct_declarator ::= direct_declarator '(' <openscope> identifier_list ')'
	 */
	void consumeDirectDeclaratorFunctionDeclaratorKnR();

	/**
	 * pointer ::= '*'
	 *           | pointer '*' 
	 */
	void consumePointer();

	/**
	 * pointer ::= '*' <openscope> type_qualifier_list
	 *           | pointer '*' <openscope> type_qualifier_list
	 */
	void consumePointerTypeQualifierList();

	/**
	 * parameter_declaration ::= declaration_specifiers declarator
	 *                         | declaration_specifiers   
	 *                         | declaration_specifiers abstract_declarator
	 */
	void consumeParameterDeclaration(boolean hasDeclarator);

	/**
	 * direct_abstract_declarator   
	 *     ::= array_modifier
	 *       | direct_abstract_declarator array_modifier
	 */
	void consumeAbstractDeclaratorArrayModifier(boolean hasDeclarator);

	/**
	 * direct_abstract_declarator  
	 *     ::= '(' ')'
	 *       | direct_abstract_declarator '(' ')'
	 *       | '(' <openscope> parameter_type_list ')'
	 *       | direct_abstract_declarator '(' <openscope> parameter_type_list ')'
	 */
	void consumeAbstractDeclaratorFunctionDeclarator(boolean hasDeclarator,
			boolean hasParameters);

	/**
	 * initializer ::= assignment_expression
	 */
	void consumeInitializer();

	/**
	 * initializer ::= '{' <openscope> initializer_list '}'
	 *               | '{' <openscope> initializer_list ',' '}'
	 */
	void consumeInitializerList();

	/**
	 * designated_initializer ::= <openscope> designation initializer
	 */
	void consumeInitializerDesignated();

	/**
	 * designator ::= '[' constant_expression ']'
	 */
	void consumeDesignatorArrayDesignator();

	/**
	 *  designator ::= '.' 'identifier'
	 */
	void consumeDesignatorFieldDesignator();

	/**
	 * declaration_specifiers ::= <openscope> simple_declaration_specifiers
	 */
	void consumeDeclarationSpecifiersSimple();

	/**
	 * declaration_specifiers ::= <openscope> struct_or_union_declaration_specifiers
	 * declaration_specifiers ::= <openscope> enum_declaration_specifiers
	 */
	void consumeDeclarationSpecifiersStructUnionEnum();

	/**
	 * declaration_specifiers ::=  <openscope> typdef_name_declaration_specifiers
	 */
	void consumeDeclarationSpecifiersTypedefName();

	/**
	 * declaration ::= declaration_specifiers <openscope> init_declarator_list ';'
	 * declaration ::= declaration_specifiers  ';'
	 */
	void consumeDeclaration(boolean hasDeclaratorList);

	/**
	 * external_declaration ::= ';'
	 */
	void consumeDeclarationEmpty();

	/**
	 * a declaration inside of a struct
	 * 
	 * struct_declaration ::= specifier_qualifier_list <openscope> struct_declarator_list ';'
	 * 
	 * specifier_qualifier_list is a subset of declaration_specifiers,
	 * struct_declarators are declarators that are allowed inside a struct,
	 * a struct declarator is a regular declarator plus bit fields
	 */
	void consumeStructDeclaration(boolean hasDeclaration);

	/**
	 * struct_declarator
	 *     ::= ':' constant_expression  
	 *       | declarator ':' constant_expression		
	 */
	void consumeStructBitField(boolean hasDeclarator);

	/**
	 * struct_or_union_specifier
	 *     ::= 'struct' '{' <openscope> struct_declaration_list_opt '}'
	 *       | 'union'  '{' <openscope> struct_declaration_list_opt '}'
	 *       | 'struct' struct_or_union_identifier '{' <openscope> struct_declaration_list_opt '}'
	 *       | 'union'  struct_or_union_identifier '{' <openscope> struct_declaration_list_opt '}'
	 * 
	 * @param key either k_struct or k_union from IASTCompositeTypeSpecifier
	 */
	void consumeTypeSpecifierComposite(boolean hasName, int key);

	/**
	 * struct_or_union_specifier
	 *     ::= 'struct' struct_or_union_identifier
	 *       | 'union'  struct_or_union_identifier
	 *       
	 * enum_specifier ::= 'enum' enum_identifier     
	 */
	void consumeTypeSpecifierElaborated(int kind);

	/**
	 * enum_specifier ::= 'enum' '{' <openscope> enumerator_list_opt '}'
	 *                  | 'enum' enum_identifier '{' <openscope> enumerator_list_opt '}'
	 *                  | 'enum' '{' <openscope> enumerator_list_opt ',' '}'
	 *                  | 'enum' enum_identifier '{' <openscope> enumerator_list_opt ',' '}'
	 */
	void consumeTypeSpecifierEnumeration(boolean hasIdentifier);

	/**
	 * enumerator ::= enum_identifier
	 *              | enum_identifier '=' constant_expression
	 */
	void consumeEnumerator(boolean hasInitializer);

	/**
	 * compound_statement ::= <openscope> '{' block_item_list '}'
	 * 
	 * block_item_list ::= block_item | block_item_list block_item
	 */
	void consumeStatementCompoundStatement();

	/**
	 * compound_statement ::= '{' '}' 
	 */
	void consumeStatementEmptyCompoundStatement();

	/**
	 * iteration_statement_matched
	 *     ::= 'for' '(' expression ';' expression ';' expression ')' statement
	 *       | 'for' '(' expression ';' expression ';'            ')' statement
	 *       | 'for' '(' expression ';'            ';' expression ')' statement
	 *       | 'for' '(' expression ';'            ';'            ')' statement
	 *       | 'for' '('            ';' expression ';' expression ')' statement
	 *       | 'for' '('            ';' expression ';'            ')' statement
	 *       | 'for' '('            ';'            ';' expression ')' statement
	 *       | 'for' '('            ';'            ';'            ')' statement
	 *       | 'for' '(' declaration expression ';' expression ')' statement
	 *       | 'for' '(' declaration expression ';'            ')' statement
	 *       | 'for' '(' declaration            ';' expression ')' statement
	 *       | 'for' '(' declaration            ';'            ')' statement
	 *       
	 */
	void consumeStatementForLoop(boolean hasExpr1, boolean hasExpr2,
			boolean hasExpr3);

	/**
	 * iteration_statement_matched
	 *     ::= 'while' '(' expression ')' matched_statement
	 */
	void consumeStatementWhileLoop();

	/**
	 * iteration_statement_matched
	 *     ::= 'do' statement 'while' '(' expression ')' ';'
	 */
	void consumeStatementDoLoop();

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
	void consumeStatementDeclaration();

	/**
	 * jump_statement ::= goto goto_identifier ';'
	 */
	void consumeStatementGoto();

	/**
	 * jump_statement ::= continue ';'
	 */
	void consumeStatementContinue();

	/**
	 * jump_statement ::= break ';'
	 */
	void consumeStatementBreak();

	/**
	 * jump_statement ::= return ';'
	 * jump_statement ::= return expression ';'
	 */
	void consumeStatementReturn(boolean hasExpression);

	/**
	 * expression_statement ::= ';'
	 */
	void consumeStatementNull();

	/**
	 * expression_statement ::= expression ';'
	 */
	void consumeStatementExpression();

	/**
	 * labeled_statement ::= label_identifier ':' statement
	 * label_identifier ::= identifier 
	 */
	void consumeStatementLabeled();

	/**
	 * labeled_statement ::= case constant_expression ':'
	 */
	void consumeStatementCase();

	/**
	 * labeled_statement ::= default ':'
	 */
	void consumeStatementDefault();

	/**
	 * selection_statement ::=  switch '(' expression ')' statement
	 */
	void consumeStatementSwitch();

	/**
	 * if_then_statement ::= if '(' expression ')' statement
	 */
	void consumeStatementIfThen();

	/**
	 * if_then_else_matched_statement
	 *     ::= if '(' expression ')' statement_no_short_if else statement_no_short_if
	 *     
	 * if_then_else_unmatched_statement
	 *     ::= if '(' expression ')' statement_no_short_if else statement
	 */
	void consumeStatementIfThenElse();

	/**
	 * translation_unit ::= external_declaration_list
	 *
	 * external_declaration_list
	 *    ::= external_declaration
	 *      | external_declaration_list external_declaration
	 */
	void consumeTranslationUnit();

	/**
	 * function_definition
	 *    ::= declaration_specifiers <openscope> declarator compound_statement
	 *      | function_declarator compound_statement
	 */
	void consumeFunctionDefinition(boolean hasDeclSpecifiers);

	/**
	 * function_definition
	 *     ::= declaration_specifiers <openscope> declarator 
	 *         <openscope> declaration_list compound_statement
	 */
	void consumeFunctionDefinitionKnR();

	/**
	 * statement ::= ERROR_TOKEN
	 */
	void consumeStatementProblem();

	/**
	 * assignment_expression ::= ERROR_TOKEN
	 * constant_expression ::= ERROR_TOKEN
	 */
	void consumeExpressionProblem();

	/**
	 * external_declaration ::= ERROR_TOKEN
	 */
	void consumeDeclarationProblem();

}
