package org.eclipse.cdt.core.model.failedTests;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import java.io.FileInputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;


/**
 * @author vhirsl
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CModelElementsFailedTests extends TestCase {
	
    private ICProject fCProject;
	private IFile headerFile;
	private NullProgressMonitor monitor;
		
	public static Test suite() {
		TestSuite suite= new TestSuite(CModelElementsFailedTests.class.getName());
		suite.addTest(new CModelElementsFailedTests("testBug36379"));
		return suite;
	}		
		
	public CModelElementsFailedTests(String name) {
		super(name);
	}
		
	protected void setUp() throws Exception {
		monitor = new NullProgressMonitor();
		String pluginRoot=org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile();
	
		fCProject= CProjectHelper.createCCProject("TestProject1", "bin");
		headerFile = fCProject.getProject().getFile("CModelElementsTest.h");
		if (!headerFile.exists()) {
			try{
				FileInputStream fileIn = new FileInputStream(pluginRoot+ "resources/cfiles/CModelElementsTestStart.h"); 
				headerFile.create(fileIn,false, monitor);        
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void tearDown()  {
		CProjectHelper.delete(fCProject);
	}	
}
