package org.eclipse.cdt.debug.core.tests;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.testplugin.CDebugHelper;
import org.eclipse.cdt.debug.testplugin.CProjectHelper;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Peter Graves
 *
 * This file contains a set of generic tests for the CDI Location interfaces.
 * It will currenly use the mi implementation.
 *
 */
public class LocationTests extends TestCase {
    IWorkspace workspace;
    IWorkspaceRoot root;
    ICProject testProject;
    NullProgressMonitor monitor;
	ICDISession session;
    

    /**
     * Constructor for LocationTests
     * @param name
     */
    public LocationTests(String name) {
        super(name);
     /***
     * The assume that they have a working workspace
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

    }
    
    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     * 
     * Example code test the packages in the project 
     *  "com.qnx.tools.ide.cdt.core"
     */
    protected void setUp() throws CoreException, InvocationTargetException, IOException {
		ResourcesPlugin.getWorkspace().getDescription().setAutoBuilding(false);
		/***
		 * Create a new project and import the test source.
		 */
		IPath importFile = new Path("resources/debugTest.zip");
		testProject=CProjectHelper.createCProjectWithImport("filetest", importFile);
		if (testProject==null)
			fail("Unable to create project");
		/* Build the test project.. */

		testProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
            
    }
    
     /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown() throws CoreException, CDIException {
    	if (session!=null) {
    		session.terminate();
    		session=null;
    	}
        CProjectHelper.delete(testProject);
    }
    
    public static TestSuite suite() {
    return new TestSuite(LocationTests.class);
    }
    
    public static void main (String[] args){
    junit.textui.TestRunner.run(suite());
    }


    /***
     * A couple tests to make sure comparing Locations works as expected.
     */
    public void testIsEquals() throws CoreException, MIException, IOException, CDIException {
        ICDITarget cdiTarget;
        ICDILocation location, location2;
        ICDIBreakpoint[] breakpoints;
        ICDILocationBreakpoint curbreak;
        session=CDebugHelper.createSession("main",testProject);
        assertNotNull(session);
		ICDITarget[] targets = session.getTargets();
		assertNotNull(targets);
		assertTrue(targets.length > 0);
		cdiTarget = targets[0];
        assertNotNull(cdiTarget);
        
        /**********************************************************************
         *  Simple test.. this should work.
         **********************************************************************/
        location=cdiTarget.createLocation("main.c", "func1", 0);
        location2=cdiTarget.createLocation("main.c", "func1", 0);
        assertTrue(location.equals(location2));
        /**********************************************************************
         *  Simple test.. this should work.
         **********************************************************************/
        location=cdiTarget.createLocation("main.c", null, 10);
        location2=cdiTarget.createLocation("main.c", null, 10);
        assertTrue(location.equals(location2));

        /**********************************************************************
         * make sure that the location returned from getLocation on the 
         * ICDILocationBreakpoint.getLocation that is returned from 
         * setLocationBreakpoint is the same as the breakpoint returned from
         * BreakpointManager.getBreakpoints.getLocation()
         **********************************************************************/
        location=cdiTarget.createLocation("main.c", "func1", 0);
        assertNotNull(location);
        location2=cdiTarget.setLocationBreakpoint(0, location, null, false).getLocation();
        
        breakpoints=cdiTarget.getBreakpoints();
        assertNotNull(breakpoints);
        assertTrue(breakpoints.length==1);
        if (breakpoints[0] instanceof ICDILocationBreakpoint) {
            curbreak=(ICDILocationBreakpoint) breakpoints[0];
        } else
            curbreak=null;
        assertNotNull(curbreak);
        
        assertTrue(curbreak.getLocation().equals(location2));
        cdiTarget.deleteAllBreakpoints();
        /* Create a break point on a generic function with a file name that 
         * gdb will change to the relitive path of the source file.  This
         * should work, but at the time of writing (Sept 25, 2002) does not.
         */
        location=cdiTarget.createLocation("main.c", "func1", 0);
        assertNotNull(location);
        cdiTarget.setLocationBreakpoint(0, location, null, false);
        
        breakpoints=cdiTarget.getBreakpoints();
        assertNotNull(breakpoints);
        assertTrue(breakpoints.length==1);
        if (breakpoints[0] instanceof ICDILocationBreakpoint) {
            curbreak=(ICDILocationBreakpoint) breakpoints[0];
        } else
            curbreak=null;
        assertNotNull(curbreak);
        
        assertTrue("PR:23879",curbreak.getLocation().equals(location));

        
        /* clean up the session */
	    session.terminate();
		session=null;
    
   }
       

}
