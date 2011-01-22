/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.debugger.tests;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.internal.services.dsf.INoop;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.Stack;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.tests.TestUtils;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.After;
import org.junit.Test;

public class Concurrent extends BaseLaunchTest {

	private EDCLaunch launch;
	private DsfSession session;
	private boolean testDidShutdown;

	/**
	 * This test validates that the EDC service threads are given a chance to
	 * complete before the DSF session proceeds with its shutdown. Otherwise,
	 * the logic running on those threads would likely encounter all sorts of
	 * problems as it tries to operate within a session that has been shut down.
	 */
	@Test
	public void testShutdown() throws Throwable {
		TestUtils.showPerspective("org.eclipse.debug.ui.DebugPerspective");	
		launch = createLaunch();
		assertNotNull(launch);
		session = TestUtils.waitForSession(launch);
		assertNotNull(session);
		final ExecutionDMC threadDMC = TestUtils.waitForSuspendedThread(session);
		assertNotNull(threadDMC);
		TestUtils.waitForStackFrame(session, threadDMC);
		final INoop service = TestUtils.getService(session, INoop.class);
		final String semaphore = new String();
		final AtomicBoolean successful = new AtomicBoolean(false);
		final Throwable[] exception = new Throwable[1];
		
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					RequestMonitor rm = new RequestMonitor(service.getExecutor(), null);
					service.longNoopUsingServiceTracker(5, rm);
					successful.set(true);
					synchronized (semaphore) {
						semaphore.notify();
					}
				}
				catch (Throwable exc) {
					synchronized (exception) {
						exception[0] = exc;
					}
					synchronized (semaphore) {
						semaphore.notify();
					}
				}
			}
		};

		// Kick off some work on an EDC service thread. Its logic will encounter
		// an exception if the DSF session shuts down while they're running
		EDCLaunch.getThreadPool(service.getSession().getId()).execute(runnable);

		// Tell the DSF session to shut down
		TestUtils.shutdownDebugSession(launch, session);
		
		// Avoid a redundant shutdown by the @After method 
		testDidShutdown = true;

		// Wait until the EDC service thread either completes or encounters an
		// exception
		synchronized (semaphore) {
			semaphore.wait(10 * 1000);
		}

		// If it ran into an exception, fail the test
		synchronized (exception) {
			if (exception[0] != null) {
				throw exception[0];
			}
		}
		
		// A sanity check that the thread completed successfully
		assertTrue(successful.get());
	}
	
	/**
	 * Basic test. See {@link #testStackTraces()}
	 */
	@Test
	public void testStackTraces1() throws Throwable {
		testStackTraces();
	}

	/**
	 * Variation that uses a thread pool with only one thread. Will be slower,
	 * but should be able to handle the load.
	 */
	/**
	 * @throws Throwable
	 */
	@Test
	public void testStackTraces2() throws Throwable {
		System.setProperty("org.eclipse.cdt.edc.poolthread.coreThreadCount", "1");		
		testStackTraces();
	}

	/**
	 * Variation that uses a larger thread pool. Should be more effective in
	 * flushing out concurrenc issues since more threads are running the same
	 * code simultaneously
	 */
	/**
	 * @throws Throwable
	 */
	@Test
	public void testStackTraces3() throws Throwable {
		System.setProperty("org.eclipse.cdt.edc.poolthread.coreThreadCount", "10");		
		testStackTraces();
	}

	/**
	 * Test that an overwhelmed thread pool throws the expected exception.
	 * There's no way three threads can handle 10,000 requests with a maximum
	 * backlog of five requests.
	 */
	/**
	 * @throws Throwable
	 */
	@Test
	public void testStackTraces4() throws Throwable {
		System.setProperty("org.eclipse.cdt.edc.poolthread.queueLimit", "5");
		try {
			testStackTraces();
			Assert.fail(); // RejectedExecutionException should have been thrown
		}
		catch (RejectedExecutionException exc) {}
	}

	/**
	 * @throws Throwable
	 */
	public void testStackTraces() throws Throwable {
		TestUtils.showPerspective("org.eclipse.debug.ui.DebugPerspective");	
		launch = createLaunch();
		assertNotNull(launch);
		session = TestUtils.waitForSession(launch);
		assertNotNull(session);
		final ExecutionDMC threadDMC = TestUtils.waitForSuspendedThread(session);
		assertNotNull(threadDMC);
		TestUtils.waitForStackFrame(session, threadDMC);		
		final AtomicInteger completed = new AtomicInteger();

		final Stack stackService = TestUtils.getService(session, Stack.class);
		final int testCount = 10000;
		
		final IFrameDMContext[][] referenceStackCrawl = new IFrameDMContext[1][];
		final Throwable[] exception = new Throwable[1];
		
		Runnable getFrames = new Runnable() {
			public void run() {
				try {
					// Don't bother if another thread has already encountered an
					// exception
					synchronized (exception) {
						if (exception[0] != null) {
							return;
						}
					}
					
					IFrameDMContext[] frames = stackService.getFramesForDMC((ExecutionDMC)threadDMC, 0, IStack.ALL_FRAMES);
					
					// The first stack crawl we get is the one we compare all
					// subsequent ones to
					synchronized (referenceStackCrawl) {
						if (referenceStackCrawl[0] == null) {
							referenceStackCrawl[0] = frames;
						}
					}
					if (frames != referenceStackCrawl[0]) {
						Assert.assertEquals(referenceStackCrawl[0].length, frames.length);
						for (int i = 0; i < frames.length; i++) {
							Assert.assertEquals(frames[i].getLevel(), referenceStackCrawl[0][i].getLevel());
							compareStackFrames((StackFrameDMC)frames[i], ((StackFrameDMC)referenceStackCrawl[0][i]));
						}
					}
					if (completed.incrementAndGet() == testCount) {
						synchronized (completed) {
							completed.notify();
						}
					}
				} catch (Throwable e) {
					synchronized (exception) {
						if (exception[0] == null) {
							exception[0] = e;
						}
					}
					synchronized (completed) {
						completed.notify();
					}
				}
			}
		};
		
		for (int i = 0; i < testCount; i++) {
			EDCLaunch.getThreadPool(stackService.getSession().getId()).execute(getFrames);
		}
		
		synchronized (completed) {
			completed.wait(30*1000);
		}

		// See if the threads encountered an exception. If so, then throw it from this thread (the test thread) so the test fails accordingly 
		synchronized (exception) {
			if (exception[0] != null) {
				throw exception[0];
			}
		}
		
		Assert.assertEquals(testCount, completed.get());

	}

	/**
	 * Validate two stack frames are equal
	 */
	private static void compareStackFrames(StackFrameDMC f1, StackFrameDMC f2) throws Exception {
		Assert.assertEquals(f1.getFunctionName(), f2.getFunctionName());
		Assert.assertEquals(f1.getLineNumber(), f2.getLineNumber()); 
		Assert.assertEquals(f1.getModuleName(), f2.getModuleName());
		Assert.assertEquals(f1.getName(), f2.getName());
		Assert.assertEquals(f1.getSourceFile(), f2.getSourceFile());
		Assert.assertEquals(f1.getBaseAddress(), f2.getBaseAddress());
		Assert.assertEquals(f1.getCalledFrame(), f2.getCalledFrame()); 
	}


	@After
	public void shutdown() {
		if (!testDidShutdown) {
			TestUtils.shutdownDebugSession(launch, session);
		}
	}

	@Override
	protected void configureLaunchConfiguration(
			ILaunchConfigurationWorkingCopy configuration) {

		// Make sure it stop at main
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "main");
	}

}
