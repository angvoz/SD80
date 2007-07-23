/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author dsteffle
 */
public class AST2CSpecFailingTest extends AST2SpecBaseTest {

	public AST2CSpecFailingTest() {
	}

	public AST2CSpecFailingTest(String name) {
		super(name);
	}

	/**
	 [--Start Example(C 6.7.7-6):
	typedef signed int t;
	typedef int plain;
	struct tag {
	unsigned t:4;
	const t:5;
	plain r:5;
	};
	t f(t (t));
	long t;
	 --End Example]
	 */
	public void test6_7_7s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef signed int t;\n"); //$NON-NLS-1$
		buffer.append("typedef int plain;\n"); //$NON-NLS-1$
		buffer.append("struct tag {\n"); //$NON-NLS-1$
		buffer.append("unsigned t:4;\n"); //$NON-NLS-1$
		buffer.append("const t:5;\n"); //$NON-NLS-1$
		buffer.append("plain r:5;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("t f(t (t));\n"); //$NON-NLS-1$
		buffer.append("long t;\n"); //$NON-NLS-1$
		try {
			parse(buffer.toString(), ParserLanguage.C, true, 0);
			assertTrue(false);
		} catch (Exception e) {}
	}
	
	/**
	 [--Start Example(C 6.10.3.5-5):
	#define x 3
	#define f(a) f(x * (a))
	#undef x
	#define x 2
	#define g f
	#define z z[0]
	#define h g(~
	#define m(a) a(w)
	#define w 0,1
	#define t(a) a
	#define p() int
	#define q(x) x
	#define r(x,y) x ## y
	#define str(x) # x
	int foo() {
	p() i[q()] = { q(1), r(2,3), r(4,), r(,5), r(,) };
	char c[2][6] = { str(hello), str() };
	}
	 --End Example]
	 */
	public void test6_10_3_5s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define x 3\n"); //$NON-NLS-1$
		buffer.append("#define f(a) f(x * (a))\n"); //$NON-NLS-1$
		buffer.append("#undef x\n"); //$NON-NLS-1$
		buffer.append("#define x 2\n"); //$NON-NLS-1$
		buffer.append("#define g f\n"); //$NON-NLS-1$
		buffer.append("#define z z[0]\n"); //$NON-NLS-1$
		buffer.append("#define h g(~\n"); //$NON-NLS-1$
		buffer.append("#define m(a) a(w)\n"); //$NON-NLS-1$
		buffer.append("#define w 0,1\n"); //$NON-NLS-1$
		buffer.append("#define t(a) a\n"); //$NON-NLS-1$
		buffer.append("#define p() int\n"); //$NON-NLS-1$
		buffer.append("#define q(x) x\n"); //$NON-NLS-1$
		buffer.append("#define r(x,y) x ## y\n"); //$NON-NLS-1$
		buffer.append("#define str(x) # x\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("p() i[q()] = { q(1), r(2,3), r(4,), r(,5), r(,) };\n"); //$NON-NLS-1$
		buffer.append("char c[2][6] = { str(hello), str() };\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parseCandCPP(buffer.toString(), true, 0);
		assertTrue(false);
		} catch (Exception e) {}
	}
}
