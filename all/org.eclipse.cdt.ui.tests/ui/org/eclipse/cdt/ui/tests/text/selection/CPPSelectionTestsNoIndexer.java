/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;

import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.parser.ParserException;

import org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsAction;

/**
 * It is required to test the selection performance independent of the indexer to make sure that the DOM is functioning properly.
 * 
 * Indexer bugs can drastically influence the correctness of these tests so the indexer has to be off when performing them.
 * 
 * @author dsteffle
 */
public class CPPSelectionTestsNoIndexer extends BaseTestCase {
    
    private static final String INDEX_FILE_ID = "2946365241"; //$NON-NLS-1$
	static NullProgressMonitor      monitor;
    static IWorkspace               workspace;
    static IProject                 project;
	static ICProject				cPrj;
    static FileManager              fileManager;
    static boolean                  disabledHelpContributions = false;
    {
        //(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();
        monitor = new NullProgressMonitor();
        
        workspace = ResourcesPlugin.getWorkspace();
        
        try {
            cPrj = CProjectHelper.createCCProject("CPPSelectionTestsNoIndexer", "bin", IPDOMManager.ID_NO_INDEXER); //$NON-NLS-1$ //$NON-NLS-2$
        
            project = cPrj.getProject();
			
			IPath pathLoc = CCorePlugin.getDefault().getStateLocation();
			File indexFile = new File(pathLoc.append(INDEX_FILE_ID + ".index").toOSString()); //$NON-NLS-1$
			if (indexFile.exists())
				indexFile.delete();
        } catch ( CoreException e ) {
            /*boo*/
        }
        if (project == null)
            fail("Unable to create project"); //$NON-NLS-1$

        //Create file manager
        fileManager = new FileManager();
    }
    public CPPSelectionTestsNoIndexer()
    {
        super();
    }
    /**
     * @param name
     */
    public CPPSelectionTestsNoIndexer(String name)
    {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite= suite(CPPSelectionTestsNoIndexer.class, "_");
        suite.addTest( new CPPSelectionTestsNoIndexer("cleanupProject") );    //$NON-NLS-1$
        return suite;
    }
    
    public void cleanupProject() throws Exception {
        try{
            project.delete( true, false, monitor );
            project = null;
        } catch( Throwable e ){
            /*boo*/
        }
    }
    
    protected void tearDown() throws Exception {
        if( project == null || !project.exists() ) 
            return;
        
        IResource [] members = project.members();
        for( int i = 0; i < members.length; i++ ){
            if( members[i].getName().equals( ".project" ) || members[i].getName().equals( ".cdtproject" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            if (members[i].getName().equals(".settings")) 
            	continue;
            try{
                members[i].delete( false, monitor );
            } catch( Throwable e ){
                /*boo*/
            }
        }
    }
    
    protected IFile importFile(String fileName, String contents ) throws Exception{
        //Obtain file handle
        IFile file = project.getProject().getFile(fileName);
        
        InputStream stream = new ByteArrayInputStream( contents.getBytes() ); 
        //Create file input stream
        if( file.exists() )
            file.setContents( stream, false, false, monitor );
        else
            file.create( stream, false, monitor );
        
        fileManager.addFile(file);
        
        return file;
    }
    
    protected IFile importFileWithLink(String fileName, String contents) throws Exception{
        //Obtain file handle
        IFile file = project.getProject().getFile(fileName);
        
        IPath location = new Path(project.getLocation().removeLastSegments(1).toOSString() + File.separator + fileName); //$NON-NLS-1$
        
        File linkFile = new File(location.toOSString());
        if (!linkFile.exists()) {
        	linkFile.createNewFile();
        }
        
        file.createLink(location, IResource.ALLOW_MISSING_LOCAL, null);
        
        InputStream stream = new ByteArrayInputStream( contents.getBytes() ); 
        //Create file input stream
        if( file.exists() )
            file.setContents( stream, false, false, monitor );
        else
            file.create( stream, false, monitor );
        
        fileManager.addFile(file);
        
        return file;
    }
    
    protected IFile importFileInsideLinkedFolder(String fileName, String contents, String folderName ) throws Exception{
    	IFolder linkedFolder = project.getFolder(folderName);
    	IPath folderLocation = new Path(project.getLocation().toOSString() + File.separator + folderName + "_this_is_linked"); //$NON-NLS-1$
    	IFolder actualFolder = project.getFolder(folderName + "_this_is_linked"); //$NON-NLS-1$
    	if (!actualFolder.exists())
    		actualFolder.create(true, true, monitor);
    	
    	linkedFolder.createLink(folderLocation, IResource.NONE, monitor);
    	
    	actualFolder.delete(true, false, monitor);
    	
    	IFile file = linkedFolder.getFile(fileName);
    	
        InputStream stream = new ByteArrayInputStream( contents.getBytes() ); 
        //Create file input stream
        if( file.exists() )
            file.setContents( stream, false, false, monitor );
        else
            file.create( stream, false, monitor );
            	
        fileManager.addFile(file);
    	
        return file;
    }
    
	protected IASTNode testF3(IFile file, int offset) throws ParserException, CoreException {
		return testF3(file, offset, 0);
	}
	
    protected IASTNode testF3(IFile file, int offset, int length) throws ParserException, CoreException {
		if (offset < 0)
			throw new ParserException("offset can not be less than 0 and was " + offset); //$NON-NLS-1$
		
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart part = null;
        try {
            part = page.openEditor(new FileEditorInput(file), "org.eclipse.cdt.ui.editor.CEditor"); //$NON-NLS-1$
        } catch (PartInitException e) {
            assertFalse(true);
        }
        
        if (part instanceof AbstractTextEditor) {
            ((AbstractTextEditor)part).getSelectionProvider().setSelection(new TextSelection(offset,length));
            
            final OpenDeclarationsAction action = (OpenDeclarationsAction) ((AbstractTextEditor)part).getAction("OpenDeclarations"); //$NON-NLS-1$
            action.runSync();
        
            // the action above should highlight the declaration, so now retrieve it and use that selection to get the IASTName selected on the TU
            ISelection sel = ((AbstractTextEditor)part).getSelectionProvider().getSelection();
            
            if (sel instanceof TextSelection) {
            	ITextSelection textSel = (ITextSelection)sel;
            	ITranslationUnit tu = (ITranslationUnit)CoreModel.getDefault().create(file);
            	IASTTranslationUnit ast = tu.getAST();
                IASTName[] names = tu.getLanguage().getSelectedNames(ast, textSel.getOffset(), textSel.getLength());
                
                if (names.length == 0) {
                    assertFalse(true);
                } else {
                    return names[0];
                }
            }
        }
        
        return null;
    }
    	
    public void _testBug93281() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class Point{                         \n"); //$NON-NLS-1$
        buffer.append("public:                              \n"); //$NON-NLS-1$
        buffer.append("Point(): xCoord(0){}                 \n"); //$NON-NLS-1$
        buffer.append("Point& operator=(const Point &rhs){return *this;}    // line A\n"); //$NON-NLS-1$
        buffer.append("void* operator new [ ] (unsigned int);\n"); //$NON-NLS-1$
        buffer.append("private:                             \n"); //$NON-NLS-1$
        buffer.append("int xCoord;                          \n"); //$NON-NLS-1$
        buffer.append("};                                   \n"); //$NON-NLS-1$
        buffer.append("static const Point zero;\n"); //$NON-NLS-1$
        buffer.append("int main(int argc, char **argv) {        \n"); //$NON-NLS-1$
        buffer.append("Point *p2 = new Point();         \n"); //$NON-NLS-1$
        buffer.append("p2->    operator // /* operator */ // F3 in the middle \n"); //$NON-NLS-1$
        buffer.append("//of \"operator\" should work\n"); //$NON-NLS-1$
        buffer.append("// \\n"); //$NON-NLS-1$
        buffer.append("/* */\n"); //$NON-NLS-1$
        buffer.append("=(zero);           // line B\n"); //$NON-NLS-1$
        buffer.append("p2->operator /* oh yeah */ new // F3 in the middle of \"operator\"\n"); //$NON-NLS-1$
        buffer.append("// should work\n"); //$NON-NLS-1$
        buffer.append("//\n"); //$NON-NLS-1$
        buffer.append("[ /* sweet */ ] //\n"); //$NON-NLS-1$
        buffer.append("(2);\n"); //$NON-NLS-1$
        buffer.append("return (0);                          \n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        
        String code = buffer.toString();
        IFile file = importFile("test93281.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("p2->operator") + 6; //$NON-NLS-1$
        IASTNode node = testF3(file, offset);
        
        assertTrue(node instanceof IASTName);
        assertEquals(((IASTName)node).toString(), "operator new[]"); //$NON-NLS-1$
        assertEquals(((ASTNode)node).getOffset(), 183);
        assertEquals(((ASTNode)node).getLength(), 16);
        
        offset = code.indexOf("p2->    operator") + 11; //$NON-NLS-1$
        node = testF3(file, offset);
        
        assertTrue(node instanceof IASTName);
        assertEquals(((IASTName)node).toString(), "operator ="); //$NON-NLS-1$
        assertEquals(((ASTNode)node).getOffset(), 121);
        assertEquals(((ASTNode)node).getLength(), 9);
        
    }
        
    public void testBasicDefinition() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("extern int MyInt;       // def is in another file  \n"); //$NON-NLS-1$
        buffer.append("extern const int MyConst;   // def is in another file    \n"); //$NON-NLS-1$
        buffer.append("void MyFunc(int);       // often used in header files\n"); //$NON-NLS-1$
        buffer.append("struct MyStruct;        // often used in header files\n"); //$NON-NLS-1$
        buffer.append("typedef int NewInt;     // a normal typedef statement\n"); //$NON-NLS-1$
        buffer.append("class MyClass;          // often used in header files\n"); //$NON-NLS-1$
        buffer.append("int MyInt;\n"); //$NON-NLS-1$
        buffer.append("extern const int MyConst = 42;\n"); //$NON-NLS-1$
        buffer.append("void MyFunc(int a) { cout << a << endl; }\n"); //$NON-NLS-1$
        buffer.append("struct MyStruct { int Member1; int Member2; };\n"); //$NON-NLS-1$
        buffer.append("class MyClass { int MemberVar; };\n"); //$NON-NLS-1$
        
        String code = buffer.toString();
        IFile file = importFile("testBasicDefinition.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("MyInt") + 2; //$NON-NLS-1$
        int defOffset = code.indexOf("MyInt", offset) + 2; //$NON-NLS-1$
        IASTNode def = testF3(file, offset);
        IASTNode decl = testF3(file, defOffset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyInt"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 11);
        assertEquals(((ASTNode)decl).getLength(), 5);
        assertEquals(((IASTName)def).toString(), "MyInt"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 330);
        assertEquals(((ASTNode)def).getLength(), 5);
        
        offset= code.indexOf("MyConst") + 2; 
        defOffset= code.indexOf("MyConst", offset) + 2;
        def = testF3(file, offset);
        decl = testF3(file, defOffset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyConst"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 69);
        assertEquals(((ASTNode)decl).getLength(), 7);
        assertEquals(((IASTName)def).toString(), "MyConst"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 354);
        assertEquals(((ASTNode)def).getLength(), 7);
        
        offset= code.indexOf("MyFunc") + 2; 
        defOffset= code.indexOf("MyFunc", offset) + 2;
        def = testF3(file, offset);
        decl = testF3(file, defOffset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyFunc"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 115);
        assertEquals(((ASTNode)decl).getLength(), 6);
        assertEquals(((IASTName)def).toString(), "MyFunc"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 373);
        assertEquals(((ASTNode)def).getLength(), 6);
        
        offset= code.indexOf("MyStruct") + 2; 
        defOffset= code.indexOf("MyStruct", offset) + 2;
        def = testF3(file, offset);
        decl = testF3(file, defOffset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyStruct"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 171);
        assertEquals(((ASTNode)decl).getLength(), 8);
        assertEquals(((IASTName)def).toString(), "MyStruct"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 417);
        assertEquals(((ASTNode)def).getLength(), 8);
        
        offset= code.indexOf("MyClass") + 2; 
        defOffset= code.indexOf("MyClass", offset) + 2;
        def = testF3(file, offset);
        decl = testF3(file, defOffset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyClass"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 278);
        assertEquals(((ASTNode)decl).getLength(), 7);
        assertEquals(((IASTName)def).toString(), "MyClass"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 463);
        assertEquals(((ASTNode)def).getLength(), 7);
    }
    
	public void testBug95224() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "class A{\n"); //$NON-NLS-1$
	    writer.write( "A();\n"); //$NON-NLS-1$
	    writer.write( "A(const A&); // open definition on A finds class A\n"); //$NON-NLS-1$
	    writer.write( "~A(); // open definition on A finds nothing\n"); //$NON-NLS-1$
	    writer.write( "};\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		IFile file = importFile("testBug95224.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("A(); // open definition "); //$NON-NLS-1$
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "~A"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 65);
        assertEquals(((ASTNode)decl).getLength(), 2);
	}
	
	public void testBasicTemplateInstance() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "namespace N{                               \n"); //$NON-NLS-1$
	    writer.write( "   template < class T > class AAA { T _t; };\n"); //$NON-NLS-1$
	    writer.write( "};                                         \n"); //$NON-NLS-1$
	    writer.write( "N::AAA<int> a;                             \n"); //$NON-NLS-1$
	    
		String code = writer.toString();
		IFile file = importFile("testBasicTemplateInstance.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("AAA<int>"); //$NON-NLS-1$
        IASTNode decl1 = testF3(file, offset, 3);
        assertTrue(decl1 instanceof IASTName);
        assertEquals(((IASTName)decl1).toString(), "AAA"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl1).getOffset(), 74);
        assertEquals(((ASTNode)decl1).getLength(), 3);
		
        IASTNode decl2 = testF3(file, offset, 8);
        assertTrue(decl2 instanceof IASTName);
        assertEquals(((IASTName)decl2).toString(), "AAA"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl2).getOffset(), 74);
        assertEquals(((ASTNode)decl2).getLength(), 3);
	}
	
	public void testBug86829A() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class X {\n"); //$NON-NLS-1$
        buffer.append("public:\n"); //$NON-NLS-1$
        buffer.append("X(int); // openReferences fails to find the constructor in g()\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("X f(X);\n"); //$NON-NLS-1$
        buffer.append("void g()\n"); //$NON-NLS-1$
        buffer.append("{\n"); //$NON-NLS-1$
        buffer.append("X b = f(X(2)); // openDeclarations on X(int) finds the class and not \n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$

		String code = buffer.toString();
        IFile file = importFile("testBug86829A.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("X(2)"); //$NON-NLS-1$
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 18);
        assertEquals(((ASTNode)decl).getLength(), 1);
	}
	
	public void _testBug86829B() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class X {\n"); //$NON-NLS-1$
        buffer.append("public:\n"); //$NON-NLS-1$
        buffer.append("operator int();\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("class Y {\n"); //$NON-NLS-1$
        buffer.append("public:\n"); //$NON-NLS-1$
        buffer.append("operator X();\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("Y a;\n"); //$NON-NLS-1$
        buffer.append("int c = X(a); // OK: a.operator X().operator int()\n"); //$NON-NLS-1$
		
        String code = buffer.toString();
        IFile file = importFile("testBug86829B.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("X(a);"); //$NON-NLS-1$
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 6);
        assertEquals(((ASTNode)decl).getLength(), 1);
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
		static int y; // declares static data member y
		X(): x(0) { } // defines a constructor of X
	};
	int X::y = 1; // defines X::y
	enum { up, down }; // defines up and down
	namespace N { int d; } // defines N and N::d
	namespace N1 = N; // defines N1
	X anX; // defines anX
	// whereas these are just declarations:
	extern int a; // declares a
	extern const int c; // declares c
	int f(int); // declares f
	struct S; // declares S
	typedef int Int; // declares Int
	extern X anotherX; // declares anotherX
	using N::d; // declares N::d
	*/
	public void testCPPSpecDeclsDefs() throws Exception {
		StringBuffer buffer = new StringBuffer();
        buffer.append("int a; // defines a\n"); //$NON-NLS-1$
        buffer.append("extern const int c = 1; // defines c\n"); //$NON-NLS-1$
        buffer.append("int f(int x) { return x+a; } // defines f and defines x\n"); //$NON-NLS-1$
        buffer.append("struct S { int a; int b; }; // defines S, S::a, and S::b\n"); //$NON-NLS-1$
        buffer.append("struct X { // defines X\n"); //$NON-NLS-1$
        buffer.append("int x; // defines nonstatic data member x\n"); //$NON-NLS-1$
        buffer.append("static int y; // declares static data member y\n"); //$NON-NLS-1$
        buffer.append("X(): x(0) { } // defines a constructor of X\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("int X::y = 1; // defines X::y\n"); //$NON-NLS-1$
        buffer.append("enum { up, down }; // defines up and down\n"); //$NON-NLS-1$
        buffer.append("namespace N { int d; } // defines N and N::d\n"); //$NON-NLS-1$
        buffer.append("namespace N1 = N; // defines N1\n"); //$NON-NLS-1$
        buffer.append("X anX; // defines anX\n"); //$NON-NLS-1$
        buffer.append("extern int a; // declares a\n"); //$NON-NLS-1$
        buffer.append("extern const int c; // declares c\n"); //$NON-NLS-1$
        buffer.append("int f(int y); // declar f\n"); //$NON-NLS-1$
        buffer.append("struct S; // declares S\n"); //$NON-NLS-1$
        buffer.append("typedef int Int; // declares Int\n"); //$NON-NLS-1$
        buffer.append("extern X anotherX; // declares anotherX\n"); //$NON-NLS-1$
        buffer.append("using N::d; // declares N::d\n"); //$NON-NLS-1$
		
        String code = buffer.toString();
        IFile file = importFile("testCPPSpecDeclsDefs.cpp", code); //$NON-NLS-1$
        int offset = code.indexOf("a; // defines a"); //$NON-NLS-1$
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 512);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("c = 1; // defines c"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "c"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 546);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("f(int x) { return x+a; } // defines f and defines x"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "f"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 567);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("x) { return x+a; } // defines f and defines x"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 67);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("x+a; } // defines f and defines x"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 67);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("x+a; } // defines f and defines x"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 67);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("a; } // defines f and defines x"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("S { int a; int b; }; // defines S, S::a, and S::b"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "S"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 596);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("a; int b; }; // defines S, S::a, and S::b"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 128);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("b; }; // defines S, S::a, and S::b"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "b"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 135);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("X { // defines X"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 177);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("x; // defines nonstatic data member x"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 198);
        assertEquals(((ASTNode)decl).getLength(), 1);
        
        IASTNode def;
		offset = code.indexOf("y; // declares static data member y"); //$NON-NLS-1$
        def = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertEquals(((IASTName)def).toString(), "y"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 337);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("X(): x(0) { } // defines a constructor of X"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 283);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("x(0) { } // defines a constructor of X"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 198);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("X::y = 1; // defines X::y"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 177);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("y = 1; // defines X::y"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "y"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 247);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("up, down }; // defines up and down"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "up"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 367);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("down }; // defines up and down"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "down"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 371);
        assertEquals(((ASTNode)decl).getLength(), 4);
		
		offset = code.indexOf("N { int d; } // defines N and N::d"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "N"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 412);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("d; } // defines N and N::d"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "d"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 420);
        assertEquals(((ASTNode)decl).getLength(), 1);
		
		offset = code.indexOf("N1 = N; // defines N1"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "N1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 457);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("N; // defines N1"); //$NON-NLS-1$
        def = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertEquals(((IASTName)def).toString(), "N"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 412);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("X anX; // defines anX"); //$NON-NLS-1$
        def = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertEquals(((IASTName)def).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 177);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("anX; // defines anX"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "anX"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 481);
        assertEquals(((ASTNode)decl).getLength(), 3);
		
		offset = code.indexOf("a; // declares a"); //$NON-NLS-1$
        def = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertEquals(((IASTName)def).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 4);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("c; // declares c"); //$NON-NLS-1$
        def = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertEquals(((IASTName)def).toString(), "c"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 37);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("f(int y); // declar f"); //$NON-NLS-1$
        def = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertEquals(((IASTName)def).toString(), "f"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 61);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("S; // declares S"); //$NON-NLS-1$
        def = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertEquals(((IASTName)def).toString(), "S"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 120);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("Int; // declares Int"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "Int"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 625);
        assertEquals(((ASTNode)decl).getLength(), 3);
        
		offset = code.indexOf("X anotherX; // declares anotherX"); //$NON-NLS-1$
        def = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertEquals(((IASTName)def).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 177);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("anotherX; // declares anotherX"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "anotherX"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 655);
        assertEquals(((ASTNode)decl).getLength(), 8);
        		
		offset = code.indexOf("N::d; // declares N::d"); //$NON-NLS-1$
        def = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertEquals(((IASTName)def).toString(), "N"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 412);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("d; // declares N::d"); //$NON-NLS-1$
        def = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertEquals(((IASTName)def).toString(), "d"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 420);
        assertEquals(((ASTNode)def).getLength(), 1);
	}
	
	public void testBug95225() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class Overflow {\n"); //$NON-NLS-1$
        buffer.append("public:\n"); //$NON-NLS-1$
        buffer.append("Overflow(char,double,double);\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("void f(double x)\n"); //$NON-NLS-1$
        buffer.append("{\n"); //$NON-NLS-1$
        buffer.append("throw Overflow('+',x,3.45e107);\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        buffer.append("int foo() {\n"); //$NON-NLS-1$
        buffer.append("try {\n"); //$NON-NLS-1$
        buffer.append("f(1.2);\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        buffer.append("catch(Overflow& oo) {\n"); //$NON-NLS-1$
        buffer.append("				// handle exceptions of type Overflow here\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
		
        String code = buffer.toString();
        IFile file = importFile("testBug95225.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("rflow('+',x,3.45e107);"); //$NON-NLS-1$
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "Overflow"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 25);
        assertEquals(((ASTNode)decl).getLength(), 8);
        
		offset = code.indexOf("x,3.45e107);"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 72);
        assertEquals(((ASTNode)decl).getLength(), 1);
    }
	
	public void testNoDefinitions() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern int a1; // declares a\n"); //$NON-NLS-1$
		buffer.append("extern const int c1; // declares c\n"); //$NON-NLS-1$
		buffer.append("int f1(int); // declares f\n"); //$NON-NLS-1$
		buffer.append("struct S1; // declares S\n"); //$NON-NLS-1$
		buffer.append("typedef int Int; // declares Int\n"); //$NON-NLS-1$
		
        String code = buffer.toString();
        IFile file = importFile("testNoDefinitions.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("a1; // declares a"); //$NON-NLS-1$
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 11);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("c1; // declares c"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "c1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 46);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("f1(int); // declares f"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "f1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 68);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("S1; // declares S"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "S1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 98);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("Int; // declares Int"); //$NON-NLS-1$
        decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "Int"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 128);
        assertEquals(((ASTNode)decl).getLength(), 3);
	}
    
    public void testBug95202() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A { }; // implicitlydeclared A::operator=\n"); //$NON-NLS-1$
        buffer.append("struct B : A {\n"); //$NON-NLS-1$
        buffer.append("B& operator=(const B &);\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("B& B::operator=(const B& s) {\n"); //$NON-NLS-1$
        buffer.append("this->B::operator=(s); // wellformed\n"); //$NON-NLS-1$
        buffer.append("return *this;\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        
        String code = buffer.toString();
        IFile file = importFile("testBug95202.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("s); // wellformed"); //$NON-NLS-1$
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "s"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 117);
        assertEquals(((ASTNode)decl).getLength(), 1);
    }
    
    public void _testBug95229() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A {\n"); //$NON-NLS-1$
        buffer.append("operator short(); // F3 on operator causes an infinite loop\n"); //$NON-NLS-1$
        buffer.append("} a;\n"); //$NON-NLS-1$
        buffer.append("int f(int);\n"); //$NON-NLS-1$
        buffer.append("int f(float);\n"); //$NON-NLS-1$
        buffer.append("int i = f(a); // Calls f(int), because short -> int is\n"); //$NON-NLS-1$
                
        String code = buffer.toString();
        IFile file = importFile("testBug95229.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("rator short(); // F3"); //$NON-NLS-1$
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "operator short"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 11);
        assertEquals(((ASTNode)decl).getLength(), 14);
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
    
    public void testBug103697() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("int x;\n"); //$NON-NLS-1$
        buffer.append("int foo() {\n"); //$NON-NLS-1$
        buffer.append(" return x;\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
                
        String code = buffer.toString();
        IFile file = importFileWithLink("testBug103697.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("return x;\n") + "return ".length(); //$NON-NLS-1$ //$NON-NLS-2$
        IASTNode decl = testF3(file, offset);
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
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 1);
    }
}
