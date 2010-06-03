/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import org.eclipse.osgi.util.NLS;

public class DwarfMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfMessages"; //$NON-NLS-1$

	public static String DwarfDebugInfoProvider_FailedToReadCIE;

	public static String DwarfDebugInfoProvider_DwarfProviderFor;

	public static String DwarfDebugInfoProvider_NotParsingType1;
	public static String DwarfDebugInfoProvider_NotParsingType2;

	public static String DwarfDebugInfoProvider_CannotResolveCompUnit1;
	public static String DwarfDebugInfoProvider_CannotResolveCompUnit2;

	public static String DwarfDebugInfoProvider_UnhandledType;

	public static String DwarfFrameRegisters_CannotReadRegister;

	public static String DwarfFrameRegisters_CannotWriteRegister;

	public static String DwarfFrameRegisters_ErrorCalculatingLocation;

	public static String DwarfFrameRegisters_NoCommonInfoEntry;

	public static String UnknownVariableAddress;
	public static String NotImplementedFormat;
	public static String InternalErrorFormat;

	public static String LocationExpression_BadStackSize;

	public static String LocationExpression_DW_OP;

	public static String LocationExpression_MultiRegisterVariable;

	public static String LocationExpression_UnexpectedOperand;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, DwarfMessages.class);
	}

	private DwarfMessages() {
	}
}
