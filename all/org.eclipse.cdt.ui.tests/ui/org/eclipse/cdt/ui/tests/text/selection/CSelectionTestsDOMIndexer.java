/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.selection;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerProjectSICollector;

import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Test Ctrl_F3/F3 with the DOM Indexer for a C project.
 * 
 * @author dsteffle
 */
public class CSelectionTestsDOMIndexer extends BaseSelectionTestsIndexer {
	private static final String INDEX_TAG = "1161844423.index"; //$NON-NLS-1$
	IFile 					file;
	NullProgressMonitor		monitor;

	static final String sourceIndexerID = IPDOMManager.ID_FULL_INDEXER; 
	
	public CSelectionTestsDOMIndexer(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		//Create temp project
		project = createProject("CSelectionTestsDOMIndexerProject"); //$NON-NLS-1$
		IPath pathLoc = CCorePlugin.getDefault().getStateLocation();
		
		File indexFile = new File(pathLoc.append(INDEX_TAG).toOSString());
		if (indexFile.exists())
			indexFile.delete();
		
		//Set the id of the source indexer extension point as a session property to allow
		//index manager to instantiate it
		
		//Enable indexing on test project

		if (project==null) fail("Unable to create project");	 //$NON-NLS-1$
		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());
		MakeProjectNature.addNature(project, new NullProgressMonitor());
		ScannerConfigNature.addScannerConfigNature(project);
		PerProjectSICollector.calculateCompilerBuiltins(project);
		
		resetIndexer(sourceIndexerID); // set indexer
		
		//indexManager.reset();
		//Get the indexer used for the test project
	}

	protected void tearDown() {
		try {
			super.tearDown();
		} catch (Exception e1) {
		}
		//Delete project
		if (project.exists()) {
			try {
				System.gc();
				System.runFinalization();
				project.delete(true, monitor);
			} catch (CoreException e) {
				fail(getMessage(e.getStatus()));
			}
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CSelectionTestsDOMIndexer.class.getName());

		suite.addTest(new CSelectionTestsDOMIndexer("testBug78354")); //$NON-NLS-1$
		suite.addTest(new CSelectionTestsDOMIndexer("testSimpleOpenDeclaration")); //$NON-NLS-1$
		suite.addTest(new CSelectionTestsDOMIndexer("testSimpleOpenDeclaration2")); //$NON-NLS-1$
		suite.addTest(new CSelectionTestsDOMIndexer("testBasicDefinition")); //$NON-NLS-1$
		suite.addTest(new CSelectionTestsDOMIndexer("testCPPSpecDeclsDefs")); //$NON-NLS-1$
		suite.addTest(new CSelectionTestsDOMIndexer("testNoDefinitions")); //$NON-NLS-1$
		suite.addTest(new CSelectionTestsDOMIndexer("testOpenFileDiffDir")); //$NON-NLS-1$
		suite.addTest(new CSelectionTestsDOMIndexer("testBug101287")); //$NON-NLS-1$
		suite.addTest(new CSelectionTestsDOMIndexer("testBug103697")); //$NON-NLS-1$
		suite.addTest(new CSelectionTestsDOMIndexer("testBug76043")); //$NON-NLS-1$
		
		return suite;
	}

	private IProject createProject(String projectName) throws CoreException {
		ICProject cPrj = CProjectHelper.createCCProject(projectName, "bin"); //$NON-NLS-1$
		return cPrj.getProject();
	}
	
	public void testSimpleOpenDeclaration() throws Exception {
		String header = "int x() { return 1; }"; //$NON-NLS-1$
		importFile("test.h", header); //$NON-NLS-1$
		String code = "int foo() { \n return x();\n}\n"; //$NON-NLS-1$
		IFile file = importFile("test.c", code);
		
		int offset = code.indexOf("x();\n}\n");
		IASTNode def = testCtrl_F3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals(((ASTNode)def).getOffset(), header.indexOf("x")); //$NON-NLS-1$
		assertEquals(((ASTNode)def).getLength(), "x".length()); //$NON-NLS-1$
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals(((ASTNode)decl).getOffset(), header.indexOf("x")); //$NON-NLS-1$
		assertEquals(((ASTNode)decl).getLength(), "x".length()); //$NON-NLS-1$
		
	}

	public void testSimpleOpenDeclaration2() throws Exception {
		String header = "int x;\r\n // comment \r\n int y() { return 1; } /* comment */ \r\n int z; \r\n"; //$NON-NLS-1$
		importFile("testSimpleOpenDeclaration2.h", header); //$NON-NLS-1$
		String code = "int foo() { \n return y();\n}\n"; //$NON-NLS-1$
		IFile file = importFile("testSimpleOpenDeclaration2.c", code);
		
		int offset = code.indexOf("y();\n}\n");
		IASTNode def = testCtrl_F3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals(((ASTNode)def).getOffset(), header.indexOf("y")); //$NON-NLS-1$
		assertEquals(((ASTNode)def).getLength(), "y".length()); //$NON-NLS-1$
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals(((ASTNode)decl).getOffset(), header.indexOf("y")); //$NON-NLS-1$
		assertEquals(((ASTNode)decl).getLength(), "y".length()); //$NON-NLS-1$
		
	}
	
	// perform the tests from CSelectionTestsNoIndexer and make sure they work with an indexer as well:
    public void testBasicDefinition() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("extern int MyInt;       // MyInt is in another file\n"); //$NON-NLS-1$
        buffer.append("extern const int MyConst;   // MyConst is in another file\n"); //$NON-NLS-1$
        buffer.append("void MyFunc(int);       // often used in header files\n"); //$NON-NLS-1$
        buffer.append("struct MyStruct;        // often used in header files\n"); //$NON-NLS-1$
        buffer.append("typedef int NewInt;     // a normal typedef statement\n"); //$NON-NLS-1$
        buffer.append("int MyInt;\n"); //$NON-NLS-1$
        buffer.append("extern const int MyConst = 42;\n"); //$NON-NLS-1$
        buffer.append("void MyFunc(int a) { cout << a << endl; }\n"); //$NON-NLS-1$
        buffer.append("struct MyStruct { int Member1; int Member2; };\n"); //$NON-NLS-1$
        
        String code = buffer.toString();
        IFile file = importFile("testBasicDefinition.c", code); //$NON-NLS-1$
        
        int offset = code.indexOf("MyInt;\n") + 2; //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyInt"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 11);
        assertEquals(((ASTNode)decl).getLength(), 5);
        assertEquals(((IASTName)def).toString(), "MyInt"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 276);
        assertEquals(((ASTNode)def).getLength(), 5);
        
        offset = code.indexOf("MyConst = 42") + 2; //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyConst"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 69);
        assertEquals(((ASTNode)decl).getLength(), 7);
        assertEquals(((IASTName)def).toString(), "MyConst"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 300);
        assertEquals(((ASTNode)def).getLength(), 7);
        
        offset = code.indexOf("MyFunc(int a)") + 2; //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyFunc"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 115);
        assertEquals(((ASTNode)decl).getLength(), 6);
        assertEquals(((IASTName)def).toString(), "MyFunc"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 319);
        assertEquals(((ASTNode)def).getLength(), 6);
        
        offset = code.indexOf("MyStruct {") + 2; //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyStruct"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 171);
        assertEquals(((ASTNode)decl).getLength(), 8);
        assertEquals(((IASTName)def).toString(), "MyStruct"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 363);
        assertEquals(((ASTNode)def).getLength(), 8);
    }
    
	// taken from C++ spec 3.1-3:
	/*
	// all but one of the following are definitions:
	int a; // defines a
	extern const int c = 1; // defines c
	int f(int x) { return x+a; } // defines f and defines x
	struct S { int a; int b; }; // defines S, S::a, and S::b
	struct X { // defines X
		int x; // defines nonstatic data member x
	};
	enum { up, down }; // defines up and down
	struct X anX; // defines anX
	// whereas these are just declarations:
	extern int a; // declares a
	extern const int c; // declares c
	int f(int); // declares f
	struct S; // declares S
	typedef int Int; // declares Int
	extern struct X anotherX; // declares anotherX
	*/
	public void testCPPSpecDeclsDefs() throws Exception {
		StringBuffer buffer = new StringBuffer();
        buffer.append("int a; // defines a\n"); //$NON-NLS-1$
        buffer.append("extern const int c = 1; // defines c\n"); //$NON-NLS-1$
        buffer.append("int f(int x) { return x+a; } // defines f and defines x\n"); //$NON-NLS-1$
        buffer.append("struct S { int a; int b; }; // defines S, S::a, and S::b\n"); //$NON-NLS-1$
        buffer.append("struct X { // defines X\n"); //$NON-NLS-1$
        buffer.append("int x; // defines nonstatic data member x\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("enum { up, down }; // defines up and down\n"); //$NON-NLS-1$
        buffer.append("struct X anX; // defines anX\n"); //$NON-NLS-1$
        buffer.append("extern int a; // declares a\n"); //$NON-NLS-1$
        buffer.append("extern const int c; // declares c\n"); //$NON-NLS-1$
        buffer.append("int f(int); // declares f\n"); //$NON-NLS-1$
        buffer.append("struct S; // declares S\n"); //$NON-NLS-1$
        buffer.append("typedef int Int; // declares Int\n"); //$NON-NLS-1$
        buffer.append("extern struct X anotherX; // declares anotherX\n"); //$NON-NLS-1$
		
        String code = buffer.toString();
        IFile file = importFile("testCPPSpecDeclsDefs.c", code); //$NON-NLS-1$
        
        int offset = code.indexOf("a; // defines a"); //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 4);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("c = 1; // defines c"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "c"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 37);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "c"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 37);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("f(int x) { return x+a; } // defines f and defines x"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "f"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 61);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "f"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 61);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("x) { return x+a; } // defines f and defines x"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 67);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 67);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("x+a; } // defines f and defines x"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 67);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 67);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("x+a; } // defines f and defines x"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 67);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 67);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("a; } // defines f and defines x"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 4);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("S { int a; int b; }; // defines S, S::a, and S::b"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "S"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 120);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "S"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 120);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("a; int b; }; // defines S, S::a, and S::b"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 128);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 128);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("b; }; // defines S, S::a, and S::b"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "b"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 135);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "b"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 135);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("X { // defines X"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 177);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 177);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("x; // defines nonstatic data member x"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 198);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 198);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("up, down }; // defines up and down"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "up"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 246);
        assertEquals(((ASTNode)decl).getLength(), 2);
        assertEquals(((IASTName)def).toString(), "up"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 246);
        assertEquals(((ASTNode)def).getLength(), 2);
		
		offset = code.indexOf("down }; // defines up and down"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "down"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 250);
        assertEquals(((ASTNode)decl).getLength(), 4);
        assertEquals(((IASTName)def).toString(), "down"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 250);
        assertEquals(((ASTNode)def).getLength(), 4);
		
		offset = code.indexOf("X anX; // defines anX"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 177);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 177);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("anX; // defines anX"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "anX"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 290);
        assertEquals(((ASTNode)decl).getLength(), 3);
        assertEquals(((IASTName)def).toString(), "anX"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 290);
        assertEquals(((ASTNode)def).getLength(), 3);
		
		offset = code.indexOf("a; // declares a"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 4);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("c; // declares c"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "c"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 37);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "c"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 37);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("f(int); // declares f"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "f"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 61);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "f"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 61);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("S; // declares S"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "S"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 120);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "S"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 120);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("Int; // declares Int"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "Int"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 434);
        assertEquals(((ASTNode)decl).getLength(), 3);
		assertEquals(((IASTName)def).toString(), "Int"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 434);
        assertEquals(((ASTNode)def).getLength(), 3);
        
		offset = code.indexOf("X anotherX; // declares anotherX"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 177);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 177);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("anotherX; // declares anotherX"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
       	assertNull(def);

        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "anotherX"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 471);
        assertEquals(((ASTNode)decl).getLength(), 8);
	}
	
	public void testNoDefinitions() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern int a1; // declares a\n"); //$NON-NLS-1$
		buffer.append("extern const int c1; // declares c\n"); //$NON-NLS-1$
		buffer.append("int f1(int); // declares f\n"); //$NON-NLS-1$
		buffer.append("struct S1; // declares S\n"); //$NON-NLS-1$
		buffer.append("typedef int Int; // declares Int\n"); //$NON-NLS-1$
		
        String code = buffer.toString();
        IFile file = importFile("testNoDefinitions.c", code); //$NON-NLS-1$
        
        int offset = code.indexOf("a1; // declares a"); //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
       	assertNull(def);

        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 11);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("c1; // declares c"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
       	assertNull(def);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "c1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 46);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("f1(int); // declares f"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertNull(def);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "f1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 68);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("S1; // declares S"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
       	assertNull(def);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "S1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 98);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("Int; // declares Int"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "Int"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 128);
        assertEquals(((ASTNode)decl).getLength(), 3);
		assertEquals(((IASTName)def).toString(), "Int"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 128);
        assertEquals(((ASTNode)def).getLength(), 3);
	}
	
	public void testOpenFileDiffDir() throws Exception {
		importFolder("test"); //$NON-NLS-1$
		String header = "int x;\r\n // comment \r\n int y(){ return 1; } /* comment */ \r\n int z; \r\n"; //$NON-NLS-1$
		importFile("test/test.h", header); //$NON-NLS-1$
		String code = "int foo() { \n return y();\n}\n"; //$NON-NLS-1$
		IFile file = importFile("test.c", code);
		
		int offset = code.indexOf("y();\n}\n");
		IASTNode def = testCtrl_F3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals(((ASTNode)def).getOffset(), header.indexOf("y")); //$NON-NLS-1$
		assertEquals(((ASTNode)def).getLength(), "y".length()); //$NON-NLS-1$
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals(((ASTNode)decl).getOffset(), header.indexOf("y")); //$NON-NLS-1$
		assertEquals(((ASTNode)decl).getLength(), "y".length()); //$NON-NLS-1$
		
	}

	public void testBug101287() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int abc;\n"); //$NON-NLS-1$
		buffer.append("int main(int argc, char **argv) {\n"); //$NON-NLS-1$
		buffer.append("abc\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		
        String code = buffer.toString();
        IFile file = importFile("testBug101287.c", code); //$NON-NLS-1$
        
        int offset = code.indexOf("abc\n"); //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "abc"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 3);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "abc"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 3);
	}
	
    public void testBug103697() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("int x;\n"); //$NON-NLS-1$
        buffer.append("int foo() {\n"); //$NON-NLS-1$
        buffer.append(" return x;\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
                
        String code = buffer.toString();
        IFile file = importFileWithLink("testBug103697.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("return x;\n") + "return ".length(); //$NON-NLS-1$ //$NON-NLS-2$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertEquals(((IASTName)def).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 4);
        assertEquals(((ASTNode)def).getLength(), 1);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 1);
    }
    
    public void testBug76043() throws Exception {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("int x;\n"); //$NON-NLS-1$
    	buffer.append("int foo() {\n"); //$NON-NLS-1$
    	buffer.append(" return x;\n"); //$NON-NLS-1$
    	buffer.append("}\n"); //$NON-NLS-1$
    	String code = buffer.toString(); 
    	
    	IFile file = importFileInsideLinkedFolder("testBug76043.c", code, "folder"); //$NON-NLS-1$ //$NON-NLS-2$
    	
    	assertFalse(file.isLinked()); // I'm not sure why the IResource#isLinked() returns false if it's contained within a linked folder
    	
        int offset = code.indexOf("return x;\n") + "return ".length(); //$NON-NLS-1$ //$NON-NLS-2$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertEquals(((IASTName)def).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 4);
        assertEquals(((ASTNode)def).getLength(), 1);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 1);
    }

    public void testBug78354() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("typedef int TestTypeOne;\n"); //$NON-NLS-1$
        buffer.append("typedef int TestTypeTwo;\n"); //$NON-NLS-1$
        buffer.append("main()\n"); //$NON-NLS-1$
        buffer.append("{\n"); //$NON-NLS-1$
        buffer.append("TestTypeOne myFirstLink = 5;\n"); //$NON-NLS-1$
        buffer.append("TestTypeTwo mySecondLink = 6;\n"); //$NON-NLS-1$
        buffer.append("return 0;\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        
        String code = buffer.toString();
        IFile file = importFileWithLink("testBug78354.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("TestTypeOne myFirstLink = 5;"); //$NON-NLS-1$ //$NON-NLS-2$
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "TestTypeOne"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 12);
        assertEquals(((ASTNode)decl).getLength(), 11);
    }  
    
    // REMINDER: see CSelectionTestsDomIndexer#suite() when appending new tests to this suite
}
