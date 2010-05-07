/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation.  Mar, 2010
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.debugger.tests;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Breakpoints;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.debug.edc.tests.TestUtils.Condition;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test run-to-line, move-to-line and resume-at-line.
 *
 */
public class RunAndMoveToLine extends BaseLaunchTest {

	protected DsfSession session;
	protected ExecutionDMC threadDMC;
	protected IFrameDMContext frame;
	protected EDCLaunch launch;
	protected IStack stackService; 
	protected Breakpoints breakpointsService;
	protected IRunControl2 runControlService;
	
	@Override
	protected String getExeFileName() {
		// This is the executable built by Cygwin gcc 3.4.4
		// All source files are built from this foler:
		//   "C:\\myprog\\BlackFlagWascana\\src\\"
		// Note we don't need any source file to perform the test.
		//
		return "BlackFlagMinGW_NoHardcodedBreak.exe";
	}

	@Override
	protected void configureLaunchConfiguration(
			ILaunchConfigurationWorkingCopy configuration) {

		// Make sure it stop at main
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "main");
	}

	@After
	public void shutdown() {
		TestUtils.shutdownDebugSession(launch, session);
	}
	
	/**
	 * test run-to-line, move-to-line and resume-at-line.
	 * 
     * @throws Exception
	 */
	@Test
	public void testRunAndMoveToLine() throws Exception {
		TestUtils.disableDebugPerspectiveSwitchPrompt();

		launch = createLaunch();
		assertNotNull(launch);
		session = TestUtils.waitForSession(launch);
		Assert.assertNotNull(session);
		getDsfServices();
		IEDCExecutionDMC executionDMC = TestUtils.waitForExecutionDMC(session);
		Assert.assertNotNull(executionDMC);
		
		updateSuspendedThreadAndFrame(2000);
		
		assertControlIsAt("BlackFlagWascana.cpp", "main", 15);
		
		waitForUIUpdate(2000);

		/*
		 * Now we test control in structs() function of 
		 * dbg_derived_type.cpp in Blackflag.
		 * Here's the snippet of the source lines in structs():
		 *    
		     52         lstruct.achar = '1';
		     53         lstruct.auchar = 2;
		     54         lstruct.aschar = '3';
		     55         lstruct.ashort = 4;
		     56         lstruct.aushort = 5;
		     57         lstruct.asshort = 6;
		     58         lstruct.aint = 7;
		     59         lstruct.auint = 8;
		 *
		 * Here's what's done below:
		 * 1) Run to line 53.
		 * 2) Move to line 56.
		 * 3) Run to line 57.
		 * 4) Set temp breakpoint at line 56
		 * 5) resume from line 54, which should stop at line 56.
		 */
		// run to line 53, so "lstruct.achar = '1'" is executed. 
		waitRunToLine("C:\\myprog\\BlackFlagWascana\\src\\dbg_derived_types.cpp", 53);

		assertControlIsAt("dbg_derived_types.cpp", "structs", 53);

		// move to line 56, namely skip line 53, 54 & 55
		waitMoveToLine("C:\\myprog\\BlackFlagWascana\\src\\dbg_derived_types.cpp", 56, false);	

		assertControlIsAt("dbg_derived_types.cpp", "structs", 56);

		// run to line 57, namely line 56 is executed, "aushort" is assigned.  
		waitRunToLine("C:\\myprog\\BlackFlagWascana\\src\\dbg_derived_types.cpp", 57);

		assertControlIsAt("dbg_derived_types.cpp", "structs", 57);

		// Now check which lines are executed by checking which struct fields are set.
		assertEquals("49 ('1')", TestUtils.getExpressionValue(session, frame, "lstruct.achar"));  // 52
		assertEquals("0 ('\\0')", TestUtils.getExpressionValue(session, frame, "lstruct.auchar"));// 53
		assertEquals("0 ('\\0')", TestUtils.getExpressionValue(session, frame, "lstruct.aschar"));// 54
		assertEquals("0", TestUtils.getExpressionValue(session, frame, "lstruct.ashort"));        // 55
		assertEquals("5", TestUtils.getExpressionValue(session, frame, "lstruct.aushort"));       // 56
		assertEquals("0", TestUtils.getExpressionValue(session, frame, "lstruct.asshort"));       // 57

		// Set temp breakpoint at line 56. 
		setTempBreakpoint(threadDMC, breakpointsService, "C:\\myprog\\BlackFlagWascana\\src\\dbg_derived_types.cpp", 56);

		// Resume from line 54, namely execute line 54, 55.
		waitMoveToLine("C:\\myprog\\BlackFlagWascana\\src\\dbg_derived_types.cpp", 54, true);

		assertControlIsAt("dbg_derived_types.cpp", "structs", 56);

		assertEquals("0 ('\\0')", TestUtils.getExpressionValue(session, frame, "lstruct.auchar"));// 53
		assertEquals("51 ('3')", TestUtils.getExpressionValue(session, frame, "lstruct.aschar"));// 54
		assertEquals("4", TestUtils.getExpressionValue(session, frame, "lstruct.ashort"));        // 55
		assertEquals("5", TestUtils.getExpressionValue(session, frame, "lstruct.aushort"));       // 56
		assertEquals("0", TestUtils.getExpressionValue(session, frame, "lstruct.asshort"));       // 57
		
		waitForUIUpdate(2000);
	}

	private void getDsfServices() throws Exception {
		assertNotNull(session);	// this must be initialized already.
		
		TestUtils.waitOnExecutorThread(session, new Condition() {
			
			public boolean isConditionValid() {
				DsfServicesTracker servicesTracker = getDsfServicesTracker(session);

				stackService = servicesTracker.getService(IStack.class);
				breakpointsService = servicesTracker.getService(Breakpoints.class);
				runControlService = servicesTracker.getService(IRunControl2.class);
				return true;
			}
		});
	}

	/**
	 * Wait for UI to update so that we can see the debugger work in 
	 * test workbench. 
	 * For headless mode, this just does nothing and return immediately.
	 *  
	 * @param timeout
	 */
	private void waitForUIUpdate(int timeout) {
		long limit = System.currentTimeMillis() + timeout;
		Display display = Display.getCurrent();
		if (display == null || null == display.getActiveShell())
			return;
		
		while (true) {
			while (display.readAndDispatch());
			if (System.currentTimeMillis() > limit)
				break;
		}
	}

	/**
	 * Perform run to line and wait till it's done. Underlying members "thread" and "frame" 
	 * will be updated.
	 *  
	 * @param fileName
	 * @param lineNo
	 * @throws Exception
	 */
	private void waitRunToLine(final String fileName, final int lineNo) throws Exception {
		Query<IStatus> query = new Query<IStatus>() {
			@Override
			protected void execute(final DataRequestMonitor<IStatus> drm) {
				runControlService.runToLine(threadDMC, fileName, lineNo, false, new RequestMonitor(session.getExecutor(), drm) {
					@Override
					protected void handleCompleted() {
						drm.setData(getStatus());
						drm.done();
					}});
				
			}
		};
		
		session.getExecutor().execute(query);

		IStatus status = query.get(5, TimeUnit.SECONDS);
		
		if (status == null || ! status.isOK())
			fail("Error in run-to-line: " + (status == null ? "Exception happened." : status.getMessage()));

		updateSuspendedThreadAndFrame(500);
	}

	/**
	 * Perform run to line and wait till it's done. Underlying members "thread" and "frame" 
	 * will be updated.
	 *  
	 * @param fileName
	 * @param lineNo
	 * @param resume TODO
	 * @throws Exception
	 */
	private void waitMoveToLine(final String fileName, final int lineNo, final boolean resume) throws Exception {
		Query<IStatus> query = new Query<IStatus>() {
			@Override
			protected void execute(final DataRequestMonitor<IStatus> drm) {
				runControlService.moveToLine(threadDMC, fileName, lineNo, resume, new RequestMonitor(session.getExecutor(), drm) {
					@Override
					protected void handleCompleted() {
						drm.setData(getStatus());
						drm.done();
					}});
				
			}
		};
		
		session.getExecutor().execute(query);

		IStatus status = query.get(5, TimeUnit.SECONDS);
		
		if (status == null || ! status.isOK())
			fail("Error in move-to-line: " + (status == null ? "Exception happened." : status.getMessage()));

		updateSuspendedThreadAndFrame(500);
	}

	/**
	 * Wait for a suspend and update suspended thread and frame.
	 * @param waitForSuspend time to wait for suspend event to be broadcasted.
	 * 
	 * @throws Exception
	 */
	private void updateSuspendedThreadAndFrame(int waitForSuspend) throws Exception {
		threadDMC = TestUtils.waitForSuspendedThread(session);
		Assert.assertNotNull(threadDMC);
		// Wait some time for the suspend event to get broadcasted.
		Thread.sleep(waitForSuspend);
		
		frame = TestUtils.waitForStackFrame(session, threadDMC, 0);
	}

	private void assertControlIsAt(String fileName,
			String functionName, int lineNo) throws Exception {
		assertFrameMatches(frame, fileName, functionName, lineNo);
	}

	private void assertFrameMatches(final IFrameDMContext frame2, String fileName,
			String functionName, int lineNo) throws Exception {
		
		Query<IFrameDMData> query = new Query<IFrameDMData>() {
			@Override
			protected void execute(final DataRequestMonitor<IFrameDMData> drm) {
				stackService.getFrameData(frame2, drm);
			}
		};
		session.getExecutor().execute(query);

		IFrameDMData fdata = null;
		fdata = query.get(5, TimeUnit.SECONDS);
		
		if (fdata == null)
			fail("Error getting stack frame data.");
		
		assertNotNull(fdata);
		assertNotNull(fdata.getFile());
		assertTrue(
			"Expected source file is [" + fileName + "] but got [" + fdata.getFile() + "].",
			fdata.getFile().contains(fileName));
		assertEquals(functionName, fdata.getFunction());
		assertEquals(lineNo, fdata.getLine());
	}

	private void setTempBreakpoint(final ExecutionDMC executionDMC, final Breakpoints bpService,
			final String srcFile, final int lineNo) throws Exception {
		Query<IBreakpointDMContext> query = new Query<IBreakpoints.IBreakpointDMContext>() {

			@Override
			protected void execute(
					final DataRequestMonitor<IBreakpoints.IBreakpointDMContext> drm) {
				
				Modules modulesService = getDsfServicesTracker(session).getService(Modules.class);

				modulesService.getLineAddress(executionDMC, srcFile, lineNo, new DataRequestMonitor<List<IAddress>>(session.getExecutor(), drm) {

					@Override
					protected void handleSuccess() {
						List<IAddress> addrs = getData();

						// IBreakpointsTargetDMContext bt_dmc = DMContexts.getAncestorOfType(executionDMC, IBreakpointsTargetDMContext.class);
						bpService.setTempBreakpoint(executionDMC, addrs.get(0), new RequestMonitor(session.getExecutor(), drm));
					}});
				
			}
		};
		
		session.getExecutor().execute(query);
		
		query.get(5, TimeUnit.SECONDS);
	}
}
