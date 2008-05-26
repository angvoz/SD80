-----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2008 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
-----------------------------------------------------------------------------------

%options la=2
%options package=org.eclipse.cdt.internal.core.dom.parser.upc
%options template=FixedBtParserTemplateD.g


$Import
	C99SizeofExpressionParser.g
$End

$Import
    UPCGrammarExtensions.g
$DropRules

unary_expression
    ::= 'upc_localsizeof' '(' type_name ')'
      | 'upc_blocksizeof' '(' type_name ')'
      | 'upc_elemsizeof'  '(' type_name ')'
          
$End