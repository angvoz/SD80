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
 * Defines constants which are used to refer to values in the plugin's preference store.
 */
public interface ICppUnitPreferencesConstants {
	/**
	 * Boolean preference controlling whether the CppUnit view should be shown on
	 * errors only.
	 */	
	public static String SHOW_ON_ERROR_ONLY= CppUnitPlugin.PLUGIN_ID + ".show_on_error"; //$NON-NLS-1$
}
