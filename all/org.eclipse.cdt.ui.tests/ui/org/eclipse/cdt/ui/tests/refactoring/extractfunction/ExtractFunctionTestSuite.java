/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.extractfunction;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTester;

/**
 * @author Emanuel Graf
 * 
 */
public class ExtractFunctionTestSuite extends TestSuite {

	@SuppressWarnings("nls")
	public static Test suite() throws Exception {
		TestSuite suite = new ExtractFunctionTestSuite();
		suite.addTest(RefactoringTester.suite("ExtractMethodRefactoringTests", "resources/refactoring/ExtractMethod.rts"));
		suite.addTest(RefactoringTester.suite("Extract Expression", "resources/refactoring/ExtractExpression.rts"));
		suite.addTest(RefactoringTester.suite("ExtractMethodPreprocessorRefactoringTests", "resources/refactoring/ExtractMethodPreprocessor.rts"));
		suite.addTest(RefactoringTester.suite("Extract Function Templates Tests", "resources/refactoring/ExtractFunctionTemplates.rts"));
		suite.addTest(RefactoringTester.suite("Extract Method History Test", "resources/refactoring/ExtractMethodHistory.rts"));
		suite.addTest(RefactoringTester.suite("Extract Function Dublicates Test", "resources/refactoring/ExtractMethodDuplicates.rts"));
		return suite;
	}

}
