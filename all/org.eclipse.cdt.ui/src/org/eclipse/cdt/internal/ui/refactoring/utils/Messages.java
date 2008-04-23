/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.refactoring.utils.messages";//$NON-NLS-1$

	private Messages() {
		// Do not instantiate
	}

	public static String IdentifierHelper_isKeyword;
	public static String IdentifierHelper_isValid;
	public static String IdentifierHelper_leadingDigit;
	public static String IdentifierHelper_emptyIdentifier;
	public static String IdentifierHelper_illegalCharacter;
	public static String IdentifierHelper_unidentifiedMistake;
	public static String VisibilityEnum_public;
	public static String VisibilityEnum_protected;
	public static String VisibilityEnum_private;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
