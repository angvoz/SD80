/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.osgi.util.NLS;

public final class DisassemblyMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyMessages";//$NON-NLS-1$

	private DisassemblyMessages() {
		// Do not instantiate
	}

	public static String Disassembly_action_ShowAddresses_label;
	public static String Disassembly_action_ShowFunctionOffsets_label;
	public static String Disassembly_action_ShowDisassembly_label;
	public static String Disassembly_action_ShowSource_label;
	public static String Disassembly_action_ShowSymbols_label;
	public static String Disassembly_action_ShowSimplified_label;
	public static String Disassembly_action_SourceSteppingMode_error;
	public static String Disassembly_action_SourceSteppingMode_label;
	public static String Disassembly_action_AssemblySteppingMode_label;
	public static String Disassembly_action_RunToHere_label;
	public static String Disassembly_action_SetPCToHere_label;
	public static String Disassembly_action_GotoPC_label;
	public static String Disassembly_action_GotoPC_tooltip;
	public static String Disassembly_action_GotoAddress_label;
	public static String Disassembly_action_GotoSymbol_label;
	public static String Disassembly_action_Copy_label;
	public static String Disassembly_action_SelectAll_label;
	public static String Disassembly_action_BreakpointProperties_label;
	public static String Disassembly_action_RemoveBreakpoint_label;
	public static String Disassembly_action_AddBreakpoint_label;
	public static String Disassembly_action_AddHWBreakpoint_label;
	public static String Disassembly_action_AddTracepoint_label;
	public static String Disassembly_action_DisableBreakpoint_label;
	public static String Disassembly_action_EnableBreakpoint_label;
	public static String Disassembly_action_WatchExpression_label;
	public static String Disassembly_action_ShowInMemory_label;
	public static String Disassembly_action_RefreshView_label;
	public static String Disassembly_action_OpenPreferences_label;
	public static String Disassembly_GotoAddressDialog_title;
	public static String Disassembly_GotoAddressDialog_label;
	public static String Disassembly_GotoAddressDialog_error_invalid_address;
	public static String Disassembly_GotoAddressDialog_error_not_a_number;
	public static String Disassembly_GotoSymbolDialog_title;
	public static String Disassembly_GotoSymbolDialog_label;
	public static String Disassembly_message_notConnected;
	public static String Disassembly_log_error_retrieveFrameAddress;
	public static String Disassembly_log_error_locateFile;
	public static String Disassembly_log_error_accessLineInfo;
	public static String Disassembly_log_error_noFileInfo;
	public static String Disassembly_log_error_fileTooLarge;
	public static String Disassembly_log_error_readFile;
	public static String Disassembly_log_error_createVersion;
	public static String Disassembly_log_error_retrieveDisassembly;
	public static String Disassembly_log_error_showDisassembly;
	public static String Disassembly_log_error_invalidSymbol;
	public static String DisassemblyPreferencePage_startAddress;
	public static String DisassemblyPreferencePage_endAddress;
	public static String DisassemblyPreferencePage_addressRadix;
	public static String DisassemblyPreferencePage_instructionRadix;
	public static String DisassemblyPreferencePage_showAddressRadix;
	public static String DisassemblyPreferencePage_showSource;
	public static String DisassemblyPreferencePage_showSymbols;
	public static String DisassemblyPreferencePage_simplifiedMnemonics;
	public static String DisassemblyPreferencePage_error_not_a_number;
	public static String DisassemblyPreferencePage_error_negative_number;
	public static String DisassemblyPreferencePage_radix_octal;
	public static String DisassemblyPreferencePage_radix_decimal;
	public static String DisassemblyPreferencePage_radix_hexadecimal;
	public static String DisassemblyPreferencePage_showFunctionOffsets;
	public static String DisassemblyPreferencePage_showAddress;
	public static String DisassemblyPreferencePage_useSourceOnlyMode;
	public static String DisassemblyPreferencePage_useSourceOnlyMode_noteTtitle;
	public static String DisassemblyPreferencePage_useSourceOnlyMode_noteMessage;
	public static String DisassemblyPreferencePage_avoidReadBeforePC;
	public static String DisassemblyIPAnnotation_primary;
	public static String DisassemblyIPAnnotation_secondary;
	public static String SourceReadingJob_name;
	public static String SourceColorerJob_name;
	public static String EditionFinderJob_name;
	public static String EditionFinderJob_task_get_timestamp;
	public static String EditionFinderJob_task_search_history;

	static {
		NLS.initializeMessages(BUNDLE_NAME, DisassemblyMessages.class);
	}
}
