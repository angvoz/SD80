/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.symbols;

import org.eclipse.osgi.util.NLS;

public class SymbolsMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.edc.symbols.SymbolsMessages"; //$NON-NLS-1$

	public static String TypeEngine_CannotResolveBaseType;
	public static String TypeEngine_CannotResolveType;
	public static String TypeEngine_ExpectedIntegerConstant;
	public static String TypeEngine_NoDecltypeSupport;
	public static String TypeEngine_NoTypeToCast;
	public static String TypeEngine_UnhandledType;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, SymbolsMessages.class);
	}

	private SymbolsMessages() {
	}
}
