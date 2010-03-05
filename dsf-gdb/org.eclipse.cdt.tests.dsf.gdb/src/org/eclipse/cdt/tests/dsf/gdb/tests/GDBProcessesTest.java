/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class GDBProcessesTest extends BaseTestCase {
	/*
	 * Path to executable
	 */
	private static final String EXEC_PATH = "data/launch/bin/";
	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "MultiThread.exe";
	
	
	private DsfSession fSession;
    private DsfServicesTracker fServicesTracker;	
	
    private IGDBControl fGdbCtrl; 
	private IMIProcesses fProcService; 
	
	/*
     *  Create a waiter and a generic completion object. They will be used to 
     *  wait for  asynchronous call completion.
     */
    private final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
    
	@Before
	public void init() throws Exception {
	    fSession = getGDBLaunch().getSession();
		fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
        /*
         *  Get the GDBProcesses & MIRunControl service.
         */
		fProcService = fServicesTracker.getService(IMIProcesses.class);
        fGdbCtrl = fServicesTracker.getService(IGDBControl.class);
	}

	@After
	public void tearDown() {
		fProcService = null;
		fServicesTracker.dispose();
	}
	
	@BeforeClass
	public static void beforeClassMethod() {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, 
				           EXEC_PATH + EXEC_NAME);
	}

	@Test
    /*
     *  Get the process data for the current program. Process is executable name in case of GDB back end
     */
	public void getProcessData() throws InterruptedException{
		
		/*
		 * Create a request monitor 
		 */
        final DataRequestMonitor<IThreadDMData> rm = 
        	new DataRequestMonitor<IThreadDMData>(fSession.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (isSuccess()) {
                    fWait.setReturnInfo(getData());
                }
                fWait.waitFinished(getStatus());
            }
        };
        
        /*
         * Ask the service to get model data for the process. 
         * There is only one process in case of GDB back end. 
         */
		final IProcessDMContext processContext = getProcessContext();
        fSession.getExecutor().submit(new Runnable() {
            public void run() {
				fProcService.getExecutionData(processContext, rm);
            }
        });
        /*
         * Wait for the operation to complete and validate success.
         */
        fWait.waitUntilDone(TestsPlugin.massageTimeout(2000));
        Assert.assertTrue(fWait.getMessage(), fWait.isOK());

        /*
         * Get process data. Name of the process is the executable name in case of GDB back-end. 
         */
        IThreadDMData processData = (IThreadDMData)fWait.getReturnInfo();
        Assert.assertNotNull("No process data is returned for Process DMC", processData);
        Assert.assertTrue("Process data should be executable name " + EXEC_NAME, processData.getName().contains(EXEC_NAME));
	}
	
	/* 
	 * getThreadData() for multiple threads
	 */
	@Test
	public void getThreadData() throws InterruptedException{
		
		final String THREAD_ID = "1";
        final DataRequestMonitor<IThreadDMData> rm = 
        	new DataRequestMonitor<IThreadDMData>(fSession.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (isSuccess()) {
                    fWait.setReturnInfo(getData());
                }
                fWait.waitFinished(getStatus());
            }
        };


		final IProcessDMContext processContext = getProcessContext();
        fProcService.getExecutor().submit(new Runnable() {
            public void run() {
            	IThreadDMContext threadDmc = fProcService.createThreadContext(processContext, THREAD_ID);
				fProcService.getExecutionData(threadDmc, rm);
            }
        });

        // Wait for the operation to complete and validate success.
        fWait.waitUntilDone(TestsPlugin.massageTimeout(2000));
        assertTrue(fWait.getMessage(), fWait.isOK());
        
        IThreadDMData threadData = (IThreadDMData)fWait.getReturnInfo();
        Assert.assertNotNull("Thread data not returned for thread id = " + THREAD_ID, threadData);

        // Thread id is only a series of numbers
    	Pattern pattern = Pattern.compile("\\d*",  Pattern.MULTILINE); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(threadData.getId());
		assertTrue("Thread ID is a series of number", matcher.find());
    	// Name is blank in case of GDB back end
    	assertEquals("Thread name is should have been blank for GDB Back end", "", threadData.getName());
    	
    	fWait.waitReset(); 
	}

	/**
	 * Utility method to return the process DM context. These tests launch a
	 * single process, thus only one such context is available.
	 * 
	 * <p>
	 * This must not be called from the DSF executor.
	 * 
	 * @return the process context
	 * @throws InterruptedException
	 */
	@ThreadSafeAndProhibitedFromDsfExecutor("fSession.getExecutor()")
	private IProcessDMContext getProcessContext() throws InterruptedException {
		assert !fSession.getExecutor().isInExecutorThread();
		
		final AsyncCompletionWaitor waitor = new AsyncCompletionWaitor();

		fProcService.getProcessesBeingDebugged(fGdbCtrl.getContext(), new DataRequestMonitor<IDMContext[]>(fSession.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
               if (isSuccess()) {
            	   IDMContext[] contexts = getData();
            	   Assert.assertNotNull("invalid return value from service", contexts);
            	   Assert.assertEquals("unexpected number of processes", contexts.length, 1);
            	   IDMContext context = contexts[0];    
            	   IProcessDMContext processContext = DMContexts.getAncestorOfType(context, IProcessDMContext.class);
                   Assert.assertNotNull("unexpected process context type ", processContext);
            	   waitor.setReturnInfo(processContext);
                }
            }
    		
    	});
    	
    	waitor.waitUntilDone(TestsPlugin.massageTimeout(2000));
    	Assert.assertTrue(fWait.getMessage(), fWait.isOK());
    	return (IProcessDMContext) waitor.getReturnInfo();
	}
}
