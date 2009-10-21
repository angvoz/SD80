-----------------------------------------------------------------------------------
-- Copyright (c) 2009 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
-----------------------------------------------------------------------------------

%options la=2
%options package=org.eclipse.cdt.internal.core.dom.lrparser.gcc
%options template=LRSecondaryParserTemplate.g


$Import
	GCCGrammar.g
$DropRules

	unary_expression
	    ::= 'sizeof' '(' type_id ')'
	      | '__alignof__' '(' type_id ')'
	      | 'typeof' '(' type_id ')'
    
$End

$Define
    $ast_class /. IASTExpression ./
$End

$Start
    no_sizeof_type_name_start
$End

$Rules 

	no_sizeof_type_name_start
	    ::= expression
	      | ERROR_TOKEN
	          /. $Build  consumeEmpty();  $EndBuild ./
          
$End