/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;


import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author hamer
 * 
 * Testing Single_Name_Reference in parameter passing
 * Bug#
 *
 */
public class CompletionTest_FunctionReference_Prefix  extends CompletionProposalsBaseTest{
	private final String fileName = "CompletionTestStart36.cpp";
	private final String fileFullPath ="resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath ="resources/contentassist/" + headerFileName;
	private final String expectedPrefix = "x";
	private final String[] expectedResults = {
			"xLocal : int",
			"xAClassField : float",
			"xVariable : int",
			"xAClassMethod(int x) : void",
			"xFunction(void) : bool",
			"xOtherFunction(void) : void",
			"xNamespace",
			"xOtherClass",
			"xFirstEnum",
			"xSecondEnum",
			"xThirdEnum",
			"xEnumeration",
			"XMacro(x, y)",
			"XStruct"
	};
	
	public CompletionTest_FunctionReference_Prefix(String name) {
		super(name);
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=109724
		//setExpectFailure(109724);
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite(CompletionTest_FunctionReference_Prefix.class.getName());
		suite.addTest(new CompletionTest_FunctionReference_Prefix("testCompletionProposals"));
		return suite;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	protected int getCompletionPosition() {
		return getBuffer().indexOf(" x ") + 2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getExpectedPrefix()
	 */
	protected String getExpectedPrefix() {
		return expectedPrefix;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getExpectedResultsValues()
	 */
	protected String[] getExpectedResultsValues() {
		return expectedResults;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getFileName()
	 */
	protected String getFileName() {
		return fileName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getFileFullPath()
	 */
	protected String getFileFullPath() {
		return fileFullPath;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getHeaderFileFullPath()
	 */
	protected String getHeaderFileFullPath() {
		return headerFileFullPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getHeaderFileName()
	 */
	protected String getHeaderFileName() {
		return headerFileName;
	}

	
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.tests.text.contentassist.CompletionProposalsBaseTest#getFunctionOrConstructorName()
	 */
	protected String getFunctionOrConstructorName() {
		return "xAClassMethod";
	}
}
