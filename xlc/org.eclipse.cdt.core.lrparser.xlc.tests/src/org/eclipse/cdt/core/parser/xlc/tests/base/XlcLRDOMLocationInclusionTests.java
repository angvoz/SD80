/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.xlc.tests.base;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.lrparser.tests.LRDOMLocationInclusionTests;
import org.eclipse.cdt.core.lrparser.xlc.XlcCLanguage;
import org.eclipse.cdt.core.lrparser.xlc.XlcCPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;

public class XlcLRDOMLocationInclusionTests extends LRDOMLocationInclusionTests {

	public XlcLRDOMLocationInclusionTests() {
		super();
	}

	public XlcLRDOMLocationInclusionTests(String name, Class<Object> className) {
		super(name, className);
	}

	public XlcLRDOMLocationInclusionTests(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(XlcLRDOMLocationInclusionTests.class);
	}
	
	protected ILanguage getCLanguage() {
		return XlcCLanguage.getDefault();
	}
	
	protected ILanguage getCPPLanguage() {
		return XlcCPPLanguage.getDefault();
	}
}
