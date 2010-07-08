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

	public static String DwarfInfoReader_DumpFileOpenOrCreateFailed;
	
	public static String DwarfInfoReader_FrameIndicesReadFailed;
	
	public static String DwarfInfoReader_ParseDebugInfoSectionFailed1;
	public static String DwarfInfoReader_ParseDebugInfoSectionFailed2;
	
	public static String DwarfInfoReader_ParseSectionSourceFilesFailed1;
	public static String DwarfInfoReader_ParseSectionSourceFilesFailed2;
	
	public static String DwarfInfoReader_ParseTraceInfoSectionFailed1;
	public static String DwarfInfoReader_ParseTraceInfoSectionFailed2;
	
	public static String DwarfInfoReader_RangeReadFailed;
	
	public static String DwarfInfoReader_ReadDebugInfo;
	
	public static String DwarfInfoReader_ReadingSymbolInfo;
	
	public static String DwarfInfoReader_ReadType;
	
	public static String DwarfInfoReader_SubprogramNameNotFound1;
	public static String DwarfInfoReader_SubprogramNameNotFound2;
	
	public static String DwarfInfoReader_TraceAddressParse1;
	public static String DwarfInfoReader_TraceAddressParse2;
	
	public static String DwarfInfoReader_TraceAddressParseFor;
	
	public static String DwarfInfoReader_TraceFinishedAddressParse;
	
	public static String DwarfInfoReader_TraceFinishedInitialParse;
	
	public static String DwarfInfoReader_TraceFinishedQuickParse;
	
	public static String DwarfInfoReader_TraceInitialParseFor;
	
	public static String DwarfInfoReader_TraceParseTypes1;
	public static String DwarfInfoReader_TraceParseTypes2;
	
	public static String DwarfInfoReader_TraceQuickParse;
	
	public static String DwarfInfoReader_TraceScopeAddressParse1;
	public static String DwarfInfoReader_TraceScopeAddressParse2;
	
	public static String DwarfInfoReader_TraceTypeParse1;
	public static String DwarfInfoReader_TraceTypeParse2;

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
