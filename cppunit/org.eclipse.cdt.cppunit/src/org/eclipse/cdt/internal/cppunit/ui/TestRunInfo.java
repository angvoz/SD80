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
 * Store information about an executed test.
 */
public class TestRunInfo extends Object {
	public String fTestName;
	public String fTrace;
	public int fStatus;

	public TestRunInfo(String testName){
		fTestName= testName;
	}	
	
	/*
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return fTestName.hashCode();
	}

	/*
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		return fTestName.equals(obj);
	}
}




