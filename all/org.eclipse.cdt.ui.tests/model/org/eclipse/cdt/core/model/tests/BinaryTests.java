package org.eclipse.cdt.core.model.tests;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import java.io.FileInputStream;
import java.io.FileNotFoundException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.testplugin.CProjectHelper;
import org.eclipse.cdt.testplugin.util.ExpectedStrings;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;



/**
 * @author Peter Graves
 *
 * This file contains a set of generic tests for the core C model's Binary
 * class. There is nothing exotic here, mostly just sanity type tests
 *
 */
public class BinaryTests extends TestCase {
    IWorkspace workspace;
    IWorkspaceRoot root;
    ICProject testProject;
    IFile cfile, exefile, libfile, archfile, objfile, bigexe, ppcexefile, ndexe;
    Path cpath, exepath, libpath, archpath, objpath;
    NullProgressMonitor monitor;



    /**
     * Constructor for BinaryTests
     * @param name
     */
    public BinaryTests(String name) {
        super(name);
        
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        super.finalize();

        /**
         * Make sure we leave the workspace clean for the next set of tests
         */
        CProjectHelper.delete(testProject);

        
    }

    
    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     * 
     * Example code test the packages in the project 
     *  "com.qnx.tools.ide.cdt.core"
     */
    protected void setUp()  throws CoreException,FileNotFoundException  {
        String pluginRoot;
        /***
         * The tests assume that they have a working workspace
         * and workspace root object to use to create projects/files in, 
         * so we need to get them setup first.
         */
        workspace= ResourcesPlugin.getWorkspace();
        root= workspace.getRoot();
        monitor = new NullProgressMonitor();
        if (workspace==null) 
            fail("Workspace was not setup");
        if (root==null)
            fail("Workspace root was not setup");
            
            
        /***
         * Setup the various files, paths and projects that are needed by the
         * tests
         */
            
        testProject=CProjectHelper.createCProject("filetest", "none");
        if (testProject==null)
            fail("Unable to create project");

        pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.ui.tests").find(new Path("/")).getFile();

        cfile = testProject.getProject().getFile("exetest.c");
        if (!cfile.exists()) {
            cfile.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/exe/main.c"),false, monitor);
        
        }
        cpath=new Path(workspace.getRoot().getLocation()+"/filetest/main.c");

        objfile = testProject.getProject().getFile("exetest.o");
        if (!objfile.exists()) {
            objfile.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/exe/x86/o.g/main.o"),false, monitor);
        
        }
        objpath=new Path(workspace.getRoot().getLocation()+"/filetest/exetest.o");
        
        exefile = testProject.getProject().getFile("test_g");
        if (!exefile.exists()) {
            exefile.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/exe/x86/o.g/exe_g"),false, monitor);
        
        }
        exepath=new Path(workspace.getRoot().getLocation()+"/filetest/exe_g");
        ppcexefile = testProject.getProject().getFile("ppctest_g");
        if (!ppcexefile.exists()) {
            ppcexefile.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/exe/ppc/be.g/exe_g"),false, monitor);
        
        }
        ndexe = testProject.getProject().getFile("exetest");
        if (!ndexe.exists()) {
            ndexe.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/exe/x86/o/exe"),false, monitor);
        
        }



        bigexe = testProject.getProject().getFile("exebig_g");
        if (!bigexe.exists()) {
            bigexe.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/exebig/x86/o.g/exebig_g"),false, monitor);
        
        }
        
        archfile = testProject.getProject().getFile("libtestlib_g.a");
        if (!archfile.exists()) {
            archfile.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/testlib/x86/a.g/libtestlib_g.a"),false, monitor);
        
        }
        libpath=new Path(workspace.getRoot().getLocation()+"/filetest/libtestlib_g.so");
        
        libfile = testProject.getProject().getFile("libtestlib_g.so");
        if (!libfile.exists()) {
            libfile.create(new FileInputStream(pluginRoot+"model/org/eclipse/cdt/core/model/tests/resources/testlib/x86/so.g/libtestlib_g.so"),false, monitor);
        
        }
        archpath=new Path(workspace.getRoot().getLocation()+"/filetest/libtestlib_g.a");
        



    }
    
     /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown() throws CoreException {
       // release resources here and clean-up
        testProject.getProject().close(null);
        testProject.getProject().open(null);

        CProjectHelper.delete(testProject);
        
    }
    
    public static TestSuite suite() {
        return new TestSuite(BinaryTests.class);
    }
    
    public static void main (String[] args){
        junit.textui.TestRunner.run(suite());
    }



    /****
     * Simple tests to make sure we can get all of a binarys children
     */
    public void testGetChildren() throws CoreException,FileNotFoundException {
        IBinary myBinary;
        ICElement[] elements;
        ExpectedStrings expSyms;
        String[] myStrings = {"atexit", "exit", "_init_libc", "printf", "_fini",
            "test.c", "_init","main.c", "_start", "test2.c", "_btext", "errno"};
        
        expSyms=new ExpectedStrings(myStrings);

        /***
         * Grab the IBinary we want to test, and find all the elements in all 
         * the binarie and make sure we get everything we expect.
         */
        myBinary=CProjectHelper.findBinary(testProject, "test_g");
        elements=myBinary.getChildren();
        for (int i=0;i<elements.length;i++) {
            expSyms.foundString(elements[i].getElementName());
        }

        assertTrue(expSyms.getMissingString(), expSyms.gotAll());
        assertTrue(expSyms.getExtraString(), !expSyms.gotExtra());
    }
    
    /***
     * A quick check to make sure the getBSS function works as expected.
     */
    public void testGetBss(){
        IBinary bigBinary,littleBinary;
        bigBinary=CProjectHelper.findBinary(testProject, "exebig_g");
        littleBinary=CProjectHelper.findBinary(testProject, "test_g");

        assertTrue("Expected 432, Got: " + bigBinary.getBSS(), bigBinary.getBSS()==432);
        assertTrue("Expected 4, Got: " + littleBinary.getBSS(), littleBinary.getBSS()==4);                
    }
    /***
     * A quick check to make sure the getBSS function works as expected.
     */
    public void testGetData(){
        IBinary bigBinary,littleBinary;
        bigBinary=CProjectHelper.findBinary(testProject, "exebig_g");
        littleBinary=CProjectHelper.findBinary(testProject, "test_g");
        if (false) {
            /****
             * Since there is no comment on this function, I have no idea what 
             * it is ment to do.  Once I find out what it's ment to do, I will
             * actually write some tests.
             * PR23602 
             */
            assertTrue("Expected 76 Got: " + bigBinary.getData(), bigBinary.getData()==76);
            assertTrue("Expected 8, Got: " + littleBinary.getData(), littleBinary.getData()==8);                
        } else
            fail("PR:23602  No docs, can't test");
    }

    /***
     * A very small set of tests to make usre Binary.getCPU() seems to return 
     * something sane for the most common exe type (x86) and one other (ppc)
     * This is not a in depth test at all.
     */
    public void testGetCpu() {
        IBinary myBinary;
        myBinary=CProjectHelper.findBinary(testProject, "exebig_g");

        assertTrue("Expected: x86  Got: " + myBinary.getCPU(),myBinary.getCPU().equals("x86"));
        myBinary=CProjectHelper.findBinary(testProject, ppcexefile.toString());
        assertTrue("Expected: ppcbe  Got: " + myBinary.getCPU(),myBinary.getCPU().equals("ppcbe"));

    }
    
    /****
     * A set of simple tests to make sute getNeededSharedLibs seems to be sane
     */
    public void testGetNeededSharedLibs() {
        IBinary myBinary;
        String[] exelibs={"libsocket.so.2", "libc.so.2"};
        String[] bigexelibs={"libc.so.2"};
        String[] gotlibs;
        ExpectedStrings exp;
        int x;
        
        exp=new ExpectedStrings(exelibs);
        myBinary=CProjectHelper.findBinary(testProject, "test_g");
        gotlibs=myBinary.getNeededSharedLibs();
        for (x=0;x<gotlibs.length;x++) {
            exp.foundString(gotlibs[x]);
        }
        assertTrue(exp.getMissingString(), exp.gotAll());
        assertTrue(exp.getExtraString(), !exp.gotExtra());
        
        exp=new ExpectedStrings(bigexelibs);
        myBinary=CProjectHelper.findBinary(testProject,"exebig_g");
        gotlibs=myBinary.getNeededSharedLibs();
        for (x=0;x<gotlibs.length;x++) {
            exp.foundString(gotlibs[x]);
        }
        assertTrue(exp.getMissingString(), exp.gotAll());
        assertTrue(exp.getExtraString(), !exp.gotExtra());
        
        exp=new ExpectedStrings(bigexelibs);
        myBinary=CProjectHelper.findBinary(testProject, "libtestlib_g.so");
        gotlibs=myBinary.getNeededSharedLibs();
        for (x=0;x<gotlibs.length;x++) {
            exp.foundString(gotlibs[x]);
        }
        assertTrue(exp.getMissingString(), exp.gotAll());
        assertTrue(exp.getExtraString(), !exp.gotExtra());
        
    }
    
    /****
     * Simple tests for the getSoname method;
     */
    public void testGetSoname() {
        IBinary myBinary;
        String name;
        myBinary=CProjectHelper.findBinary(testProject, "test_g");
        assertTrue(myBinary.getSoname().equals(""));
        
        myBinary=CProjectHelper.findBinary(testProject, "libtestlib_g.so");
        name=myBinary.getSoname();
        assertNotNull(name);
        assertTrue("Expected: libtestlib_g.so.1  Got: " + name, 
        name.equals("libtestlib_g.so.1"));
        
    }
    
    /*** 
     * Simple tests for getText
     */
    public void testGetText() {
        IBinary bigBinary,littleBinary;
        bigBinary=CProjectHelper.findBinary(testProject, bigexe.toString());
        littleBinary=CProjectHelper.findBinary(testProject, exefile.toString());
        if (false) {
            /****
             * Since there is no comment on this function, I have no idea what 
             * it is ment to do.  Once I find out what it's ment to do, I will
             * actually write some tests.
             * PR23602 
             */

            assertTrue("Expected 296, Got: " + bigBinary.getText(), bigBinary.getText()==296);
            assertTrue("Expected 296, Got: " + littleBinary.getText(), littleBinary.getText()==296);                
        } else
            fail("PR:23602  No docs, can't test");
    }
    
    /***
     * Simple tests for the hadDebug call
     */
    public void testHasDebug() {
        IBinary myBinary;
        myBinary = CProjectHelper.findBinary(testProject, "test_g");
        assertTrue(myBinary.hasDebug());
        myBinary = CProjectHelper.findBinary(testProject, "libtestlib_g.so");
        assertTrue(myBinary.hasDebug());
        myBinary = CProjectHelper.findBinary(testProject, "exetest");
        assertTrue(!myBinary.hasDebug());
    }
    
    /***
     * Sanity - isBinary and isReadonly should always return true;
     */
    public void testisBinRead() {
        IBinary myBinary;
        myBinary =CProjectHelper.findBinary(testProject, "test_g");
        assertTrue(myBinary != null);
        assertTrue(myBinary.isReadOnly());

    }
    
    /***
     * Quick tests to make sure isObject works as expected.
     */
    public void testIsObject() {
        IBinary myBinary;
        myBinary=CProjectHelper.findObject(testProject, "exetest.o");
        assertTrue(myBinary.isObject());

        
        myBinary= CProjectHelper.findBinary(testProject, "test_g");
        assertTrue(!myBinary.isObject());
        
        myBinary= CProjectHelper.findBinary(testProject, "libtestlib_g.so");
        assertTrue(!myBinary.isObject());

        myBinary= CProjectHelper.findBinary(testProject, "exetest");
        assertTrue(!myBinary.isObject());

    }
    
    /***
     * Quick tests to make sure isSharedLib works as expected.
     */
    public void testIsSharedLib() {
        IBinary myBinary;

        myBinary=CProjectHelper.findObject(testProject, "exetest.o");
        assertTrue(!myBinary.isSharedLib());

        myBinary= CProjectHelper.findBinary(testProject, "libtestlib_g.so");
        assertTrue(myBinary.isSharedLib());
        
        myBinary= CProjectHelper.findBinary(testProject, "test_g");
        assertTrue(!myBinary.isSharedLib());
        

        myBinary= CProjectHelper.findBinary(testProject, "exetest");
        assertTrue(!myBinary.isSharedLib());

    }
    
    /***
     * Quick tests to make sure isExecutable works as expected.
     */
    public void testIsExecutable() throws InterruptedException {
        IBinary myBinary;
        myBinary=CProjectHelper.findObject(testProject, "exetest.o");
        assertTrue(!myBinary.isExecutable());
        
        myBinary=CProjectHelper.findBinary(testProject, "test_g");
        assertTrue(myBinary.isExecutable());
        
        myBinary= CProjectHelper.findBinary(testProject, "libtestlib_g.so");
        assertTrue(!myBinary.isExecutable());


        myBinary= CProjectHelper.findBinary(testProject, "exetest");
        assertTrue(myBinary.isExecutable());

    }

    /***
     *  Simple sanity test to make sure Binary.isBinary returns true
     *  
     */
    public void testIsBinary() throws CoreException,FileNotFoundException,Exception {
        IBinary myBinary;

        myBinary=CProjectHelper.findBinary(testProject, "exebig_g");
        assertTrue("A Binary", myBinary != null);
    }



    
}
