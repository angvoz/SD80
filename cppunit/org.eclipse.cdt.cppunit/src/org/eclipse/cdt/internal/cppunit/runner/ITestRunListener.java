/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 févr. 03
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
 * A listener interface for observing the
 * execution of a test run.
 */
 public interface ITestRunListener {
 	/**
 	 * Test passed.
 	 */
 	public static final int STATUS_OK= 0;
 	/**
 	 * Test had an error.
 	 */
 	public static final int STATUS_ERROR= 1;
 	/**
 	 * Test had a failure.
 	 */
 	public static final int STATUS_FAILURE= 2;
 	/**
 	 * A test run has started
 	 */
	public void testRunStarted(int testCount);
	/**
	 * A test run ended.
	 */
	public void testRunEnded(long elapsedTime);
	/**
	 * A test run was stopped before it ended
	 */
	public void testRunStopped(long elapsedTime);
	/**
	 * A test started
	 */
	public void testStarted(String testName);
	/**
	 * A test ended
	 */
	public void testEnded(String testName);
	/**
	 * A test failed.
	 */
	public void testFailed(int status, String testName, String trace);		
	/**
	 * Add an entry to the tree.
	 * The format of the string is: 
	 * testName","isSuite","testcount
	 */ 
	public void testTreeEntry(String entry);
	/**
	 * The test runner VM has terminated
	 */
	public void testRunTerminated();
	
	/**
	 * A test was reran.
	 */
	public void testReran(String testClass, String testName, int status, String trace);
}


