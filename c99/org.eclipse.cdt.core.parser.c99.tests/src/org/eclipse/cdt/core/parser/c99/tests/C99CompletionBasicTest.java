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

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.prefix.BasicCompletionTest;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class C99CompletionBasicTest extends BasicCompletionTest {

	protected IASTCompletionNode getCompletionNode(String code,
			ParserLanguage lang, boolean useGNUExtensions)
			throws ParserException {
		
		return ParseHelper.getCompletionNode(code, lang);
	}
	
	// The C99 parser currently doesn't support ambiguity nodes.
	// Therefore calling IASTCompletionNode.getNames() will
	// never return more than one name.
	
	
	public void testFunction() throws Exception {
		StringBuffer code = new StringBuffer();
		code.append("void func(int x) { }");
		code.append("void func2() { fu");

		// C
		IASTCompletionNode node = getGCCCompletionNode(code.toString());
		IASTName[] names = node.getNames();

		// There is only one name, for now
		assertEquals(1, names.length);
		// The expression points to our functions
		IBinding[] bindings = sortBindings(names[0].getCompletionContext().findBindings(
				names[0], true));
		// There should be two since they both start with fu
		assertEquals(2, bindings.length);
		assertEquals("func", ((IFunction)bindings[0]).getName());
		assertEquals("func2", ((IFunction)bindings[1]).getName());
		
	}

	public void testTypedef() throws Exception {
		StringBuffer code = new StringBuffer();
		code.append("typedef int blah;");
		code.append("bl");
		
		// C
		IASTCompletionNode node = getGCCCompletionNode(code.toString());
		IASTName[] names = node.getNames();
		assertEquals(1, names.length);
		IBinding[] bindings = names[0].getCompletionContext().findBindings(names[0], true);
		assertEquals(1, bindings.length);
		assertEquals("blah", ((ITypedef)bindings[0]).getName());
	}
	
}
