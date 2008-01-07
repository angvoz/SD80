/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestSuite;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.internal.formatter.DefaultCodeFormatterOptions;

/**
 * Tests for the CodeFormatter.
 *
 * @since 4.0
 */
public class CodeFormatterTest extends BaseUITestCase {

	private Map fOptions;
	private Map fDefaultOptions;

	public static TestSuite suite() {
		return suite(CodeFormatterTest.class, "_");
	}

	protected void setUp() throws Exception {
		super.setUp();
		fDefaultOptions= DefaultCodeFormatterOptions.getDefaultSettings().getMap();
		fOptions= new HashMap(fDefaultOptions);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected void assertFormatterResult() throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		String before= contents[0].toString();
		IDocument document= new Document(before);
		String expected= contents[1].toString();
		TextEdit edit= CodeFormatterUtil.format(CodeFormatter.K_COMPILATION_UNIT, before, 0, TextUtilities.getDefaultLineDelimiter(document), fOptions);
		assertNotNull(edit);
		edit.apply(document);
		assertEquals(expected, document.get());
	}
	
	//void foo(int arg);
	//void foo(int arg){}
	
	//void foo (int arg);
	//void foo (int arg) {
	//}
	public void testInsertSpaceBeforeOpeningParen_Bug190184() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_DECLARATION, CCorePlugin.INSERT);
		assertFormatterResult();
	}

	//void FailSwitchFormatting(void)
	//{
	//        switch (confusefomatter)
	//        {
	//
	//        case START_CONFUSION:
	//                SomeFunctionCallWithTypecast(( castConfusion_t)myvar1,
	//                (castNoAdditionalConfusion_t) myvar2);
	//                break;
	//
	//                case REVEAL_CONFUSION:
	//                if (myBlockIndentIsOk)
	//                {
	//                        myBlockstuff();
	//                }
	//                break;
	//
	//                case CONTINUE_CONFUSION:
	//                {
	//                        //the indentation problem continues...
	//                }
	//                default://....still not right
	//        }
	//}

	//void FailSwitchFormatting(void) {
	//	switch (confusefomatter) {
	//
	//	case START_CONFUSION:
	//		SomeFunctionCallWithTypecast(( castConfusion_t)myvar1,
	//				(castNoAdditionalConfusion_t) myvar2);
	//		break;
	//
	//	case REVEAL_CONFUSION:
	//		if (myBlockIndentIsOk) {
	//			myBlockstuff();
	//		}
	//		break;
	//
	//	case CONTINUE_CONFUSION: {
	//		//the indentation problem continues...
	//	}
	//	default://....still not right
	//	}
	//}
	public void testIndentConfusionByCastExpression_Bug191021() throws Exception {
		assertFormatterResult();
	}
	
	//int
	//var;
	//int*
	//pvar;
	
	//int var;
	//int* pvar;
	public void testSpaceBetweenTypeAndIdentifier_Bug194603() throws Exception {
		assertFormatterResult();
	}

	//int a = sizeof(     int)    ;
	
	//int a = sizeof(int);
	public void testSizeofExpression_Bug195246() throws Exception {
		assertFormatterResult();
	}

	//int x;
	//int a = sizeof     x    ;
	
	//int x;
	//int a = sizeof x;
	public void testSizeofExpression_Bug201330() throws Exception {
		assertFormatterResult();
	}

	//void foo(){
	//for(;;){
	//int a=0;
	//switch(a){
	//case 0:
	//++a;
	//break;
	//case 1:
	//--a;
	//break;
	//}
	//}
	//}
	//int main(void){
	//foo();
	//return 1;
	//}

	//void foo() {
	//	for (;;) {
	//		int a=0;
	//		switch (a) {
	//		case 0:
	//			++a;
	//			break;
	//		case 1:
	//			--a;
	//			break;
	//		}
	//	}
	//}
	//int main(void) {
	//	foo();
	//	return 1;
	//}
	public void testForWithEmptyExpression_Bug195942() throws Exception {
		assertFormatterResult();
	}

	//#define MY private:
	//
	//class ClassA
	//{
	//MY ClassA() {}
	//};

	//#define MY private:
	//
	//class ClassA {
	//MY
	//	ClassA() {
	//	}
	//};
	public void testAccessSpecifierAsMacro_Bug197494() throws Exception {
		assertFormatterResult();
	}

	//int verylooooooooooooooooooooooooooooooooooongname = 0000000000000000000000000000000;
	
	//int verylooooooooooooooooooooooooooooooooooongname = 
	//		0000000000000000000000000000000;
	public void testLineWrappingOfInitializerExpression_Bug200961() throws Exception {
		assertFormatterResult();
	}
	
	//void functionWithLooooooooooooooooooooooooooooooooooooooooooooooooongName() throw(float);
	
	//void functionWithLooooooooooooooooooooooooooooooooooooooooooooooooongName()
	//		throw(float);
	public void testLineWrappingOfThrowSpecification_Bug200959() throws Exception {
		assertFormatterResult();
	}

	//class A {
	//public:
	//A();
	//};
	
	//class A
	//    {
	//public:
	//    A();
	//    };
	public void testWhiteSmithsAccessSpecifierIndentation1_Bug204575() throws Exception {
		fOptions= DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap();
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER, DefaultCodeFormatterConstants.FALSE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER, DefaultCodeFormatterConstants.TRUE);
		assertFormatterResult();
	}

	//class A {
	//public:
	//A();
	//};
	
	//class A
	//    {
	//    public:
	//    A();
	//    };
	public void testWhiteSmithsAccessSpecifierIndentation2_Bug204575() throws Exception {
		fOptions= DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap();
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER, DefaultCodeFormatterConstants.TRUE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER, DefaultCodeFormatterConstants.FALSE);
		assertFormatterResult();
	}

	//class A {
	//public:
	//A();
	//};
	
	//class A
	//    {
	//    public:
	//	A();
	//    };
	public void testWhiteSmithsAccessSpecifierIndentation3_Bug204575() throws Exception {
		fOptions= DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap();
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER, DefaultCodeFormatterConstants.TRUE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER, DefaultCodeFormatterConstants.TRUE);
		assertFormatterResult();
	}

	//template<typename T> class B {};
	//template<typename T1,typename T2=B<T1> > class A {};

	//template<typename T> class B {
	//};
	//template<typename T1, typename T2=B<T1> > class A {
	//};
	public void testNestedTemplateParameters_Bug206801() throws Exception {
		assertFormatterResult();
	}

	//main
	//(
	//    int           argc,
	//    char const *  argv[]
	//)
	//try
	//{
	//    for ( int i = 1 ; i < argc ; ++i )
	//    {
	//    }
	//    return 0;
	//}
	//catch ( float e )
	//{
	//    return 1;
	//}
	//catch ( ... )
	//{
	//	return 2;
	//}

	//main(int argc, char const * argv[])
	//try {
	//	for (int i = 1; i < argc; ++i) {
	//	}
	//	return 0;
	//}
	//catch (float e) {
	//	return 1;
	//}
	//catch (...) {
	//	return 2;
	//}
	public void testFunctionTryCatchBlock() throws Exception {
		assertFormatterResult();
	}

	//main(int argc, char const * argv[]) { try { for (int i = 1; i < argc; ++i) { } return 0; } catch (float e) { return 1; } catch (...) { return 2; } }
	
	//main(int argc, char const * argv[]) {
	//	try {
	//		for (int i = 1; i < argc; ++i) {
	//		}
	//		return 0;
	//	} catch (float e) {
	//		return 1;
	//	} catch (...) {
	//		return 2;
	//	}
	//}
	public void testTryCatchBlock() throws Exception {
		assertFormatterResult();
	}

	//void f() {
	//#define I 0
	//    int i = I;
	//}

	//void f() {
	//#define I 0
	//	int i = I;
	//}
	public void testMacroAsInitializer_Bug214354() throws Exception {
		assertFormatterResult();
	}
	
}
