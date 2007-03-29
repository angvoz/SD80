/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.Arrays;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Tests PDOM class template related bindings
 */
public class CPPClassTemplateTests extends PDOMTestBase {
	protected PDOM pdom;
	protected ICProject cproject;
	
	public static Test suite() {
		return suite(CPPClassTemplateTests.class);
	}
	
	public void setUp() throws Exception {
		cproject= CProjectHelper.createCCProject("classTemplateTests"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);		
	}
	
	protected void setUpSpecifics(int sections) throws Exception {
		StringBuffer[] contents= TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), sections);
		for(int i=0; i<contents.length; i++) {
			IFile file= TestSourceReader.createFile(cproject.getProject(), new Path("refs.cpp"), contents[i].toString());
		}
		IndexerPreferences.set(cproject.getProject(), IndexerPreferences.KEY_INDEX_ALL_FILES, "true");
		IndexerPreferences.set(cproject.getProject(), IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(cproject);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
		pdom= (PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject);
	}
	
	protected void tearDown() throws Exception {
		if(pdom!=null) {
			pdom.releaseWriteLock();
		}
		cproject.getProject().delete(true, NPM);
	}
	
	/*************************************************************************/
	
	// template<typename C>
	// class D {
	// public:
	// int foo(C c) {return 1};
	// };
	public void testSimpleDefinition() throws Exception {
		setUpSpecifics(1);
		assertDeclarationCount(pdom, "D", 1);
		IIndexFragmentBinding[] b= pdom.findBindings(new char[][] {{'D'}}, IndexFilter.ALL, NPM);
		assertEquals(1, b.length);
		assertTrue(b[0] instanceof ICPPClassTemplate);
		ICPPClassTemplate ct= (ICPPClassTemplate) b[0];
		ICPPTemplateParameter[] tp= ct.getTemplateParameters();
		assertEquals(1, tp.length);
		assertTrue(tp[0] instanceof ICPPTemplateTypeParameter);
		ICPPTemplateTypeParameter ctp= (ICPPTemplateTypeParameter) tp[0];
		assertNull(ctp.getDefault());
		assertEquals(0, ct.getPartialSpecializations().length);
	}
	
	// template<class C=char> /* typename and class are equivalent in template parameter context */
	// class D {
	// public:
	// int foo(C c) {return 1};
	// };
	public void testDefinition() throws Exception {
		setUpSpecifics(1);
		assertDeclarationCount(pdom, "D", 1);
		IIndexFragmentBinding[] b= pdom.findBindings(new char[][] {{'D'}}, IndexFilter.ALL, NPM);
		assertEquals(1, b.length);
		assertTrue(b[0] instanceof ICPPClassTemplate);
		ICPPClassTemplate ct= (ICPPClassTemplate) b[0];
		ICPPTemplateParameter[] tp= ct.getTemplateParameters();
		assertEquals(1, tp.length);
		assertTrue(tp[0] instanceof ICPPTemplateTypeParameter);
		assertEquals("C", tp[0].getName());
		assertEquals(new String[] {"D","C"}, tp[0].getQualifiedName());
		assertEquals(new char[][] {{'D'},{'C'}}, tp[0].getQualifiedNameCharArray());
		ICPPTemplateTypeParameter ctp= (ICPPTemplateTypeParameter) tp[0];
		IType def= ctp.getDefault();
		assertTrue(def instanceof IBasicType);
		assertEquals(0, ct.getPartialSpecializations().length);
	}
	
	// class TA {};
	// class TC {};
	//
	// template<typename A= TA, typename B, typename C=TC>
	// class E {
	// public:
	// int foo(C c, B b, A a) {return 1};
	// };
	public void testDefinition2() throws Exception {
		setUpSpecifics(1);
		assertDeclarationCount(pdom, "E", 1);
		IIndexFragmentBinding[] b= pdom.findBindings(new char[][] {{'E'}}, IndexFilter.ALL, NPM);
		assertEquals(1, b.length);
		assertTrue(b[0] instanceof ICPPClassTemplate);
		ICPPClassTemplate ct= (ICPPClassTemplate) b[0];
		ICPPTemplateParameter[] tp= ct.getTemplateParameters();
		assertEquals(3, tp.length);
		
		assertTrue(tp[0] instanceof ICPPTemplateTypeParameter);
		assertEquals("A", tp[0].getName());
		assertEquals(new String[] {"E","A"}, tp[0].getQualifiedName());
		assertEquals(new char[][] {{'E'},{'A'}}, tp[0].getQualifiedNameCharArray());
		ICPPTemplateTypeParameter ctpa= (ICPPTemplateTypeParameter) tp[0];
		IType defa= ctpa.getDefault();
		assertTrue(defa instanceof ICPPClassType);
		ICPPClassType ctdefa= (ICPPClassType) defa;
		assertEquals(new char[][] {{'T','A'}}, ctdefa.getQualifiedNameCharArray());
		
		assertTrue(tp[1] instanceof ICPPTemplateTypeParameter);
		assertEquals("B", tp[1].getName());
		assertEquals(new String[] {"E","B"}, tp[1].getQualifiedName());
		assertEquals(new char[][] {{'E'},{'B'}}, tp[1].getQualifiedNameCharArray());
		ICPPTemplateTypeParameter ctpb= (ICPPTemplateTypeParameter) tp[1];
		IType defb= ctpb.getDefault();
		assertNull(defb);
		
		assertTrue(tp[2] instanceof ICPPTemplateTypeParameter);
		assertEquals("C", tp[2].getName());
		assertEquals(new String[] {"E","C"}, tp[2].getQualifiedName());
		assertEquals(new char[][] {{'E'},{'C'}}, tp[2].getQualifiedNameCharArray());
		ICPPTemplateTypeParameter ctpc= (ICPPTemplateTypeParameter) tp[2];
		IType defc= ctpc.getDefault();
		assertTrue(defc instanceof ICPPClassType);
		ICPPClassType ctdefc= (ICPPClassType) defc;
		assertEquals(new char[][] {{'T','C'}}, ctdefc.getQualifiedNameCharArray());
		
		assertEquals(0, ct.getPartialSpecializations().length);
	}
	
	
	// template<typename C>
	// class D {
	// public:
	// int foo(C c) {return 1};
	// };
	//
	// class N {};
	//
	// template<>
	// class D<N> {
	// public:
	// int foo(N n) {return 2;}
	// };
	//
	// D<N> dn;
	// D<int> dint;
	public void testExplicitInstantiation() throws Exception {
		setUpSpecifics(1);
		
		{
			// template
			IIndexFragmentBinding[] b= pdom.findBindings(new char[][] {{'D'}}, IndexFilter.ALL, NPM);
			assertEquals(2, b.length);
			assertTrue(!(b[0] instanceof ICPPClassTemplate) || !(b[1] instanceof ICPPClassTemplate));
			int i= b[0] instanceof ICPPClassTemplate ? 0 : 1;
			
			assertInstance(b[i], ICPPClassTemplate.class);
			ICPPClassTemplate ct= (ICPPClassTemplate) b[i];
			ICPPTemplateParameter[] tp= ct.getTemplateParameters();
			assertEquals(1, tp.length);
			assertInstance(tp[i], ICPPTemplateTypeParameter.class);
			ICPPTemplateTypeParameter ctp= (ICPPTemplateTypeParameter) tp[i];
			assertNull(ctp.getDefault());
		}

		{
			assertDeclarationCount(pdom, "dn", 1);
			IIndexFragmentBinding[] b= pdom.findBindings(new char[][] {"dn".toCharArray()}, IndexFilter.ALL, NPM);
			assertEquals(1, b.length);
			assertInstance(b[0], ICPPVariable.class);
			ICPPVariable var= (ICPPVariable) b[0];
			assertInstance(var.getType(), ICPPClassType.class);
			assertInstance(var.getType(), ICPPSpecialization.class);
			ICPPSpecialization cp= (ICPPSpecialization) var.getType();
			ObjectMap m= cp.getArgumentMap();
			assertEquals(1, m.size());
			Object key= m.keyAt(0), val= m.get(key);
			assertInstance(key, ICPPTemplateTypeParameter.class);
			assertInstance(val, ICPPClassType.class);
			assertEquals(new String[] {"D","C"}, ((ICPPTemplateTypeParameter)key).getQualifiedName());
			assertEquals(new String[] {"N"}, ((ICPPClassType)val).getQualifiedName());
		}
		
		{
			assertDeclarationCount(pdom, "dint", 1);
			IIndexFragmentBinding[] b= pdom.findBindings(new char[][] {"dint".toCharArray()}, IndexFilter.ALL, NPM);
			assertEquals(1, b.length);
			assertTrue(b[0] instanceof ICPPVariable);
			ICPPVariable var= (ICPPVariable) b[0];
			assertInstance(var.getType(), ICPPClassType.class);
			assertInstance(var.getType(), ICPPSpecialization.class);
			ICPPSpecialization cp= (ICPPSpecialization) var.getType();
			ObjectMap m= cp.getArgumentMap();
			assertEquals(1, m.size());
			Object key= m.keyAt(0), val= m.get(key);
			assertInstance(key, ICPPTemplateTypeParameter.class);
			assertInstance(val, IBasicType.class);
			assertEquals(new String[] {"D","C"}, ((ICPPTemplateTypeParameter)key).getQualifiedName());
			assertEquals(IBasicType.t_int, ((IBasicType)val).getType());

		}
	}
	
	protected void assertInstance(Object o, Class c) {
		assertNotNull(o);
		assertTrue("Expected "+c.getName()+" but got "+o.getClass().getName(), c.isInstance(o));
	}
	
	protected void assertEquals(char[] c1, char[] c2) {
		assertTrue(Arrays.equals(c1, c2));
	}
	
	protected void assertEquals(String[] s1, String[] s2) {
		assertTrue(Arrays.equals(s1, s2));
	}
	
	protected void assertEquals(char[][] c1, char[][] c2) {
		if(c1==null || c2==null) {
			assertTrue(c1==c2);
			return;
		}
		
		assertEquals(c1.length, c2.length);
		for(int i=0; i<c1.length; i++) {
			assertEquals(c1[i], c2[i]);
		}
	}
}
