/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllSD80LanguageSettingsTests {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllSD80LanguageSettingsTests.class.getName());

		suite.addTest(LanguageSettingsManagerTests.suite());
		suite.addTest(LanguageSettingsPersistenceTests.suite());
		suite.addTest(LanguageSettingsUnderConstructionTests.suite());
		return suite;
	}
}
