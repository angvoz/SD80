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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.DOMLocationInclusionTests;
import org.eclipse.cdt.internal.core.dom.parser.c99.ASTPrinter;
import org.eclipse.core.resources.IFile;

public class C99DOMLocationInclusionTests extends DOMLocationInclusionTests {
	
	public C99DOMLocationInclusionTests() {
	}

	public C99DOMLocationInclusionTests(String name, Class className) {
		super(name, className);
	}

	public C99DOMLocationInclusionTests(String name) {
		super(name);
	}

	

}
