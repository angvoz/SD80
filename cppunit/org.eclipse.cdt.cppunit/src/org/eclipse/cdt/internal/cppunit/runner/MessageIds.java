/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 f�vr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.runner;

/**
 * Message identifiers for messages sent by the
 * RemoteTestRunner. 
 * 
 * @see RemoteTestRunner
 */
public class MessageIds { 
	/**
	 * The header length of a message, all messages
	 * have a fixed header length
	 */
	public static final int MSG_HEADER_LENGTH= 8;
	
	/**
	 * Notification that a test trace has started.
	 * The end of the trace is signaled by a Trace_END
	 * message. In between the TRACE_START and TRACE_END
	 * the stack trace is submitted as multiple lines.
	 */
	public static final String TRACE_START= "%TRACES "; //$NON-NLS-1$
	/**
	 * Notification that a trace for a reran test has started.
	 * The end of the trace is signaled by a RTrace_END
	 * message.
	 */
	public static final String RTRACE_START= "%RTRACES"; //$NON-NLS-1$
	/**
	 * Notification that a trace ends.
	 */
	public static final String TRACE_END=   "%TRACEE "; //$NON-NLS-1$
	/**
	 * Notification that a trace of a reran trace ends.
	 */
	public static final String RTRACE_END=  "%RTRACEE"; //$NON-NLS-1$
	/**
	 * Notification that a test run has started. 
	 * MessageIds.TEST_RUN_START+testCount.toString
	 */
	public static final String TEST_RUN_START=  "%TESTC  "; //$NON-NLS-1$
	/**
	 * Notification that a test has started.
	 * MessageIds.TEST_START + testName
	 */
	public static final String TEST_START=  "%TESTS  ";		 //$NON-NLS-1$
	/**
	 * Notification that a test has started.
	 * TEST_END + testName
	 */
	public static final String TEST_END=    "%TESTE  ";		 //$NON-NLS-1$
	/**
	 * Notification that a test had a error.
	 * TEST_ERROR + testName.
	 * After the notification follows the stack trace.
	 */
	public static final String TEST_ERROR=  "%ERROR  ";		 //$NON-NLS-1$
	/**
	 * Notification that a test had a failure.
	 * TEST_FAILED + testName.
	 * After the notification follows the stack trace.
	 */
	public static final String TEST_FAILED= "%FAILED ";	 //$NON-NLS-1$
	/**
	 * Notification that a test run has ended.
	 * TEST_RUN_END+elapsedTime.toString().
	 */ 	
	public static final String TEST_RUN_END="%RUNTIME";	 //$NON-NLS-1$
	/**
	 * Notification that a test run was successfully stopped.
	 */ 
	public static final String TEST_STOPPED="%TSTSTP "; //$NON-NLS-1$
	/**
	 * Notification that a test was reran.
	 * TEST_RERAN+testClass+" "+testName+STATUS.
	 * Status = "OK" or "FAILURE".
	 */ 
	public static final String TEST_RERAN=  "%TSTRERN"; //$NON-NLS-1$
	/**
	 * Notification about a test inside the test suite.
	 * TEST_TREE+testName","isSuite","testcount
	 * isSuite = "true" or "false"
	 */ 
	public static final String TEST_TREE="%TSTTREE"; //$NON-NLS-1$
	/**
	 * Request to stop the current test run.
	 */
	public static final String TEST_STOP=	">STOP   "; //$NON-NLS-1$
	/**
	 * Request to rerun a test.
	 * TEST_RERUN+ClassName+" "+testName
	 */
	public static final String TEST_RERUN=	">RERUN  "; //$NON-NLS-1$
}


