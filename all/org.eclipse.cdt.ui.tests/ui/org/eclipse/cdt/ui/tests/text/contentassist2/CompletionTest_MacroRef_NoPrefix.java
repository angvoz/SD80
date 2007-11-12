/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 * Testing Macro_Reference, with no prefix
 * Bug#50487 :Wrong completion kind and prefix after "#ifdef"
 *
 */
public class CompletionTest_MacroRef_NoPrefix  extends CompletionProposalsBaseTest{
	
	private final String fileName = "CompletionTestStart26.cpp";
	private final String fileFullPath ="resources/contentassist/" + fileName;
	private final String headerFileName = "CompletionTestStart.h";
	private final String headerFileFullPath ="resources/contentassist/" + headerFileName;
	private final String expectedPrefix = ""; 
	private final String[] expectedResults = {
			"AMacro(x)",
			"DEBUG",
			"XMacro(x, y)",
			"__DATE__",
			"__FILE__",
			"__LINE__",
			"__STDC__",
			"__TIME__",
			"__asm__",
			"__builtin_constant_p(exp)",
			"__builtin_va_arg(ap, type)",
			"__complex__",
			"__const",
			"__const__",
			"__cplusplus",
			"__extension__",
			"__imag__",
			"__inline__",
			"__null",
			"__real__",
			"__restrict",
			"__restrict__",
			"__signed__",
			"__stdcall",
			"__volatile__"
	};
	
	public CompletionTest_MacroRef_NoPrefix(String name) {
		super(name);
		setExpectFailure(0); // no bugnumber, the test fails because I added additional macros that
		// are reported by the CPreprocessor, but not by Scanner2. As soon as we switch over to the
		// CPreprocessor, the test-case works again.
	}

	public static Test suite() {
		TestSuite suite= new TestSuite(CompletionTest_MacroRef_NoPrefix.class.getName());
		suite.addTest(new CompletionTest_MacroRef_NoPrefix("testCompletionProposals"));
		return suite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.codeassist.tests.CompletionProposalsTest#getCompletionPosition()
	 */
	protected int getCompletionPosition() {
		return getBuffer().indexOf("#ifdef ") + 7;
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

}
