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
package org.eclipse.cdt.core.parser.c99.tests;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2Tests;
import org.eclipse.cdt.internal.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c99.ASTPrinter;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99SourceParser;
import org.eclipse.cdt.internal.core.parser.ParserException;






public class C99Tests extends AST2Tests {

	
	public C99Tests() {
	}
	
	public C99Tests(String name) {
		super(name);
	}
	
	
	/**
	 * TODO: override the other parser methods
	 */
	protected IASTTranslationUnit parse(String code, ParserLanguage lang) throws ParserException {
 
		ISourceCodeParser parser = new C99SourceParser(code);
		
		IASTTranslationUnit ast = parser.parse();
		
		System.out.println("Original Code");
		System.out.println(code);
		System.out.println();
		System.out.println("AST: " + ast);
		System.out.println();
		ASTPrinter.printAST(ast);
		System.out.println();
		
		return ast;
	}


	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		
		suite.addTest(new C99Tests("testBug75189"));
		suite.addTest(new C99Tests("testBug75340"));
		suite.addTest(new C99Tests("testBug78103"));
		suite.addTest(new C99Tests("testBug43241"));
		suite.addTest(new C99Tests("testBug40768"));
		suite.addTest(new C99Tests("testBasicFunction"));
		suite.addTest(new C99Tests("testSimpleStruct"));
		//suite.addTest(new C99Tests("testCExpressions")); // ambiguity
		suite.addTest(new C99Tests("testMultipleDeclarators"));
		suite.addTest(new C99Tests("testStructureTagScoping_1"));
		suite.addTest(new C99Tests("testStructureTagScoping_2"));
		suite.addTest(new C99Tests("testStructureDef"));
		suite.addTest(new C99Tests("testStructureNamespace"));
		suite.addTest(new C99Tests("testFunctionParameters"));
		suite.addTest(new C99Tests("testSimpleFunction"));
		suite.addTest(new C99Tests("testSimpleFunctionCall"));
		suite.addTest(new C99Tests("testForLoop"));
		suite.addTest(new C99Tests("testExpressionFieldReference"));
		suite.addTest(new C99Tests("testLabels"));
		suite.addTest(new C99Tests("testAnonStruct"));
		suite.addTest(new C99Tests("testLongLong"));
		suite.addTest(new C99Tests("testEnumerations"));
		suite.addTest(new C99Tests("testPointerToFunction"));
		suite.addTest(new C99Tests("testBasicTypes"));
		suite.addTest(new C99Tests("testCompositeTypes"));
		suite.addTest(new C99Tests("testArrayTypes"));
		suite.addTest(new C99Tests("testFunctionTypes"));
		suite.addTest(new C99Tests("testDesignatedInitializers"));
		suite.addTest(new C99Tests("testMoregetDeclarationsInAST1"));
		suite.addTest(new C99Tests("testMoregetDeclarationsInAST2"));
		suite.addTest(new C99Tests("testMoregetDeclarationsInAST3"));
		suite.addTest(new C99Tests("testFnReturningPtrToFn"));
		suite.addTest(new C99Tests("testArrayTypeToQualifiedPointerTypeParm"));
		suite.addTest(new C99Tests("testFunctionDefTypes"));
		suite.addTest(new C99Tests("testParmToFunction"));
		suite.addTest(new C99Tests("testArrayPointerFunction"));
		suite.addTest(new C99Tests("testTypedefExample4a"));
		suite.addTest(new C99Tests("testTypedefExample4b"));
		suite.addTest(new C99Tests("testTypedefExample4c"));
		suite.addTest(new C99Tests("testBug80992"));
		suite.addTest(new C99Tests("testBug80978"));
		suite.addTest(new C99Tests("testExternalDefs"));
		suite.addTest(new C99Tests("testFieldDesignators"));
		//suite.addTest(new C99Tests("testArrayDesignator"));  // I have no idea what the problem is
		suite.addTest(new C99Tests("testBug83737"));
		suite.addTest(new C99Tests("testBug84090_LabelReferences"));
		suite.addTest(new C99Tests("testBug84092_EnumReferences"));
		suite.addTest(new C99Tests("testBug84096_FieldDesignatorRef"));
		suite.addTest(new C99Tests("testProblems"));
		suite.addTest(new C99Tests("testEnumerationForwards"));
		suite.addTest(new C99Tests("testBug84185"));
		suite.addTest(new C99Tests("testBug84185_2"));
		suite.addTest(new C99Tests("testBug84176"));
		suite.addTest(new C99Tests("testBug84266"));
		suite.addTest(new C99Tests("testBug84266_2"));
		suite.addTest(new C99Tests("testBug84250"));
		suite.addTest(new C99Tests("testBug84186"));
		suite.addTest(new C99Tests("testBug84267"));
		suite.addTest(new C99Tests("testBug84228"));
		suite.addTest(new C99Tests("testBug84236"));
		suite.addTest(new C99Tests("testBug85049"));
		suite.addTest(new C99Tests("testBug86766"));
		suite.addTest(new C99Tests("testBug88338_C"));
		suite.addTest(new C99Tests("test88460"));
		suite.addTest(new C99Tests("testBug90253"));
		suite.addTest(new C99Tests("testFind"));
		//suite.addTest(new C99Tests("test92791")); 
		suite.addTest(new C99Tests("testBug85786"));
		//suite.addTest(new C99Tests("testBug95720")); // cast ambiguity
		//suite.addTest(new C99Tests("testBug94365"));  // preprocessor
		//suite.addTest(new C99Tests("testBug95119"));  // preprocessor
		suite.addTest(new C99Tests("testBug81739"));
		suite.addTest(new C99Tests("testBug95757"));
		suite.addTest(new C99Tests("testBug93980"));
		suite.addTest(new C99Tests("testBug95866"));
		suite.addTest(new C99Tests("testBug98502"));
		suite.addTest(new C99Tests("testBug98365"));
		suite.addTest(new C99Tests("testBug99262"));
		suite.addTest(new C99Tests("testBug99262B"));
		suite.addTest(new C99Tests("testBug98960"));
		//suite.addTest(new C99Tests("testBug100408"));  // ambiguity
		suite.addTest(new C99Tests("testBug98760"));
		//suite.addTest(new C99Tests("testBug79650"));  // preprocessor
		suite.addTest(new C99Tests("testBug80171"));
		suite.addTest(new C99Tests("testBug79067"));
		//suite.addTest(new C99Tests("testBug84759"));  // preprocessor: location resolver
		suite.addTest(new C99Tests("test1043290"));
		suite.addTest(new C99Tests("testBug104390_2"));
		suite.addTest(new C99Tests("testBug104800"));
		//suite.addTest(new C99Tests("testBug107150")); // preprocessor
		//suite.addTest(new C99Tests("testBug107150b")); // preprocessor
		suite.addTest(new C99Tests("testBug143502"));
		//suite.addTest(new C99Tests("testBracketAroundIdentifier_168924")); // ambiguity
		
		
		return suite;
	}
	
}

