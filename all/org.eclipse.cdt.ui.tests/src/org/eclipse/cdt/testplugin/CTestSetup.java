/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.cdt.testplugin;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.eclipse.core.resources.IWorkspaceRoot;


public class CTestSetup extends TestSetup {
	
	/**
	 * @deprecated
	 * Not needed anymore. No added value
	 */
	public CTestSetup(Test test) {
		super(test);
	}	
	
	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}
	

	
	
}