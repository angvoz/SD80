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

package org.eclipse.cdt.internal.cppunit.ui;

/**
 * A TestRunView is shown as a page in a tabbed folder.
 * It contributes the page contents and can return
 * the currently selected test.
 */
interface ITestRunView {
	/**
	 * Returns the name of the currently selected Test in the View
	 */
	public String getTestName();

	/**
	 * Activates the TestRunView
	 */
	public void activate();
	
	/**
	 * Sets the focus in the TestRunView
	 */
	public void setFocus();
	
	/**
	 * Informs that the suite is about to start 
	 */
	public void aboutToStart();

	/**
	 * Returns the name of the RunView
	 */
	public String getName();
	
	/**
	 * Sets the current Test in the View
	 */
	public void setSelectedTest(String testName);
	
	/**
	 * A test has ended
	 */
	public void endTest(String testName);
	
	/**
	 * The status of a test has changed
	 */
	public void testStatusChanged(TestRunInfo newInfo);
	/**
	 * A new tree entry got posted.
	 */
	public void newTreeEntry(String treeEntry);

}
