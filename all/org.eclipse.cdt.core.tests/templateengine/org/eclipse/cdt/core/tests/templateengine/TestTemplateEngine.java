/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.tests.templateengine;

import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.ui.PlatformUI;

/**
 * Test the functionality of TemplateEngine.
 */
public class TestTemplateEngine extends BaseTestCase {

    
    TemplateEngine templateEngine = null;
    
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		templateEngine = TemplateEngine.getDefault();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Constructor for TestTemplateEngine.
	 * @param name
	 */
	public TestTemplateEngine(String name) {
		super(name);
	}
	
	/**
	 * check for non null SharedDefaults
	 *
	 */
	public void testSharedDefaults(){
		// when running the testcase in head-less mode, the  TestExtraPagesProvider class cannot be loaded,
		// which is logged.
		if (!PlatformUI.isWorkbenchRunning()) {
			setExpectedNumberOfLoggedNonOKStatusObjects(1);
		}
	    assertNotNull(TemplateEngine.getSharedDefaults());
	}
	
	/**
	 * check that the instace is created once(Singleton).
	 */
	public void testSingleton(){    
	    assertSame(templateEngine, TemplateEngine.getDefault());
	}

}
