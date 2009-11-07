/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.internal;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.testing.TestableObject;

/**
 * Miscellaneous general utilities.
 * 
 */
public class GeneralUtils {
	/**
	 * Return true if running JUnit tests, false if normal interactive run
	 */
	public static boolean isJUnitRunning() {
		boolean result = false;
		TestableObject testableObject = PlatformUI.getTestableObject();
		if (testableObject != null) {
			result = testableObject.getTestHarness() != null;
		}
		return result;
	}
}
