/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.text.CAutoIndentTest;
import org.eclipse.cdt.ui.tests.text.CBreakIteratorTest;
import org.eclipse.cdt.ui.tests.text.CPartitionerTest;
import org.eclipse.cdt.ui.tests.text.CWordIteratorTest;
import org.eclipse.cdt.ui.tests.text.InactiveCodeHighlightingTest;
import org.eclipse.cdt.ui.tests.text.NumberRuleTest;
import org.eclipse.cdt.ui.tests.text.SemanticHighlightingTest;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionFailedTest_MemberReference_Arrow_Prefix2;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_ArgumentType_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_ArgumentType_NoPrefix2;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_ArgumentType_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_ClassReference_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_ClassReference_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_ConstructorReference;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_ExceptionReference_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_ExceptionReference_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_FieldType_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_FieldType_NoPrefix2;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_FieldType_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_FunctionReference_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_FunctionReference_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_MacroRef_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_MacroRef_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_MemberReference_Arrow_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_MemberReference_Arrow_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_MemberReference_Dot_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_MemberReference_Dot_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_NamespaceRef_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_NamespaceRef_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_NewTypeReference_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_NewTypeReference_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_ScopedReference_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_ScopedReference_NonCodeScope;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_ScopedReference_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_SingleName_Method_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_SingleName_Method_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_SingleName_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_SingleName_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_SingleName_Prefix2;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_TypeDef_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_TypeRef_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_TypeRef_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_VariableType_NestedPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_VariableType_NoPrefix;
import org.eclipse.cdt.ui.tests.text.contentassist.CompletionTest_VariableType_Prefix;
import org.eclipse.cdt.ui.tests.text.contentassist.ContentAssistTests;
import org.eclipse.cdt.ui.tests.text.selectiontests.CPPSelectionTestsCTagsIndexer;
import org.eclipse.cdt.ui.tests.text.selectiontests.CPPSelectionTestsDOMIndexer;
import org.eclipse.cdt.ui.tests.text.selectiontests.CPPSelectionTestsNoIndexer;
import org.eclipse.cdt.ui.tests.text.selectiontests.CSelectionTestsCTagsIndexer;
import org.eclipse.cdt.ui.tests.text.selectiontests.CSelectionTestsDOMIndexer;
import org.eclipse.cdt.ui.tests.text.selectiontests.CSelectionTestsNoIndexer;
import org.eclipse.cdt.ui.tests.viewsupport.ViewSupportTestSuite;

/**
 * Test all areas of the UI.
 */
public class AutomatedSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new AutomatedSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public AutomatedSuite() {
		
		// Success Tests
		//addTest(PartitionTokenScannerTest.suite());
		addTest(NumberRuleTest.suite());
		addTest(CAutoIndentTest.suite());
		addTest(CPartitionerTest.suite());
		
		// completion tests
		addTest(CompletionTest_FieldType_Prefix.suite());
		addTest(CompletionTest_FieldType_NoPrefix.suite());
		addTest(CompletionTest_FieldType_NoPrefix2.suite());
		addTest(CompletionTest_VariableType_Prefix.suite());
		addTest(CompletionTest_VariableType_NoPrefix.suite());
		addTest(CompletionTest_ArgumentType_NoPrefix.suite());
		addTest(CompletionTest_ArgumentType_NoPrefix2.suite());
		addTest(CompletionTest_ArgumentType_Prefix.suite());
		addTest(CompletionTest_ArgumentType_NoPrefix2.suite());
		addTest(CompletionTest_SingleName_Method_Prefix.suite());
		addTest(CompletionTest_SingleName_Method_NoPrefix.suite());
		addTest(CompletionTest_SingleName_Prefix.suite());
		addTest(CompletionTest_SingleName_Prefix2.suite());
		addTest(CompletionTest_SingleName_NoPrefix.suite());
		addTest(CompletionTest_MemberReference_Dot_Prefix.suite());
		addTest(CompletionTest_MemberReference_Dot_NoPrefix.suite());
		addTest(CompletionTest_MemberReference_Arrow_Prefix.suite());
		addTest(CompletionTest_MemberReference_Arrow_NoPrefix.suite());
		addTest(CompletionTest_NamespaceRef_Prefix.suite());
		addTest(CompletionTest_NamespaceRef_NoPrefix.suite());
		addTest(CompletionTest_TypeRef_NoPrefix.suite());		
		addTest(CompletionTest_TypeRef_Prefix.suite());		
		addTest(CompletionTest_ClassReference_NoPrefix.suite());
		addTest(CompletionTest_ClassReference_Prefix.suite());
		addTest(CompletionTest_NewTypeReference_NoPrefix.suite());
		addTest(CompletionTest_NewTypeReference_Prefix.suite());
		addTest(CompletionTest_ExceptionReference_NoPrefix.suite());
		addTest(CompletionTest_ExceptionReference_Prefix.suite());
		addTest(CompletionTest_FunctionReference_Prefix.suite());
		addTest(CompletionTest_ScopedReference_NoPrefix.suite());
		addTest(CompletionTest_ScopedReference_Prefix.suite());
		addTest(CompletionTest_ScopedReference_NonCodeScope.suite());
		addTest(CompletionTest_MacroRef_NoPrefix.suite());		
		addTest(CompletionTest_MacroRef_Prefix.suite());
		addTest(CompletionTest_FunctionReference_NoPrefix.suite());
		addTest(CompletionTest_ConstructorReference.suite());
		addTest(CompletionTest_TypeDef_NoPrefix.suite());
		addTest(CompletionTest_VariableType_NestedPrefix.suite());
		
		addTest( ContentAssistTests.suite() );
        
		// Failed Tests
		addTest(CompletionFailedTest_MemberReference_Arrow_Prefix2.suite());
		
        // selection tests
        addTest( CPPSelectionTestsNoIndexer.suite() );
		addTest( CSelectionTestsNoIndexer.suite() );
		addTest( CPPSelectionTestsDOMIndexer.suite() );
		addTest( CSelectionTestsDOMIndexer.suite() );
		addTest( CPPSelectionTestsCTagsIndexer.suite() );
		addTest( CSelectionTestsCTagsIndexer.suite() );

		// Break iterator tests.
		addTest(CBreakIteratorTest.suite());
		addTest(CWordIteratorTest.suite());

		// highlighting tests
		addTest(SemanticHighlightingTest.suite());
		addTest(InactiveCodeHighlightingTest.suite());
		
		// tests for package viewsupport
		addTest(ViewSupportTestSuite.suite());
	}
	
}
