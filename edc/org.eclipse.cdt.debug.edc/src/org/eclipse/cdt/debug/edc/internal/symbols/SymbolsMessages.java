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
package org.eclipse.cdt.debug.edc.internal.symbols;

import org.eclipse.osgi.util.NLS;

public class SymbolsMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.edc.internal.symbols.SymbolsMessages"; //$NON-NLS-1$

	public static String InvalidVariableLocation_CannotWriteInvalidLocation;

	public static String MemoryVariableLocation_CannotFindFrame;
	public static String MemoryVariableLocation_CannotReadAddrFormat;
	public static String MemoryVariableLocation_CannotWriteAddrFormat;
	public static String MemoryVariableLocation_Hex;
	public static String MemoryVariableLocation_LinkTime;

	public static String RegisterOffsetVariableLocation_Hex;
	public static String RegisterOffsetVariableLocation_Negative;
	public static String RegisterOffsetVariableLocation_Positive;
	public static String RegisterVariableLocation_CannotReadFramelessRegister;
	public static String RegisterVariableLocation_CannotWriteFramelessRegister;

	public static String ValueVariableLocation_CannotModifyDerivedValue;
	public static String ValueVariableLocation_NoValueAvailable;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, SymbolsMessages.class);
	}

	private SymbolsMessages() {
	}
}
