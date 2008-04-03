/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ken Ryall (Nokia) - 207675
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

/**
 * Constant definitions for C/C++ debug plug-in.
 */
public interface ICDebugConstants {

	/**
	 * C/C++ debug plug-in identifier (value
	 * <code>"org.eclipse.cdt.debug.core"</code>).
	 */
	public static final String PLUGIN_ID = CDebugCorePlugin.getUniqueIdentifier();

	/**
	 * The identifier of the default variable format to use in the variables
	 * view
	 */
	public static final String PREF_DEFAULT_VARIABLE_FORMAT = PLUGIN_ID + "cDebug.default_variable_format"; //$NON-NLS-1$

	/**
	 * The identifier of the default register format to use in the registers
	 * view
	 */
	public static final String PREF_DEFAULT_REGISTER_FORMAT = PLUGIN_ID + "cDebug.default_register_format"; //$NON-NLS-1$
	
	/**
	 * The identifier of the character set to use with unicode types
	 * view
	 */
	public static final String PREF_CHARSET = PLUGIN_ID + "cDebug.character_set"; //$NON-NLS-1$

	/**
	 * The identifier of the default expression format to use in the expressions
	 * views
	 */
	public static final String PREF_DEFAULT_EXPRESSION_FORMAT = PLUGIN_ID + "cDebug.default_expression_format"; //$NON-NLS-1$

	/**
	 * The identifier of the maximum number of instructions displayed in
	 * disassembly.
	 */
	public static final String PREF_MAX_NUMBER_OF_INSTRUCTIONS = PLUGIN_ID + "cDebug.max_number_of_instructions"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether the search for duplicate source
	 * files will be performed by debugger.
	 */
	public static final String PREF_SEARCH_DUPLICATE_FILES = PLUGIN_ID + "cDebug.Source.search_duplicate_files"; //$NON-NLS-1$

	/**
	 * The identifier of the common source locations list
	 */
	public static final String PREF_SOURCE_LOCATIONS = PLUGIN_ID + "cDebug.Source.source_locations"; //$NON-NLS-1$

	/**
	 * The default number of instructions displayed in disassembly.
	 */
	public static final int DEF_NUMBER_OF_INSTRUCTIONS = 100;

	/**
	 * The minimal valid number of instructions displayed in disassembly.
	 */
	public static final int MIN_NUMBER_OF_INSTRUCTIONS = 1;

	/**
	 * The maximal valid number of instructions displayed in disassembly.
	 */
	public static final int MAX_NUMBER_OF_INSTRUCTIONS = 999;

    /**
     * Preference that saves the default debugger type
     * @since 3.1
     */
    public static final String PREF_DEFAULT_DEBUGGER_TYPE = PLUGIN_ID + ".cDebug.defaultDebugger"; //$NON-NLS-1$

    /**
     * Preference that saves the deactivated debugger types
     * @since 3.1
     */
    public static final String PREF_FILTERED_DEBUGGERS = PLUGIN_ID + ".cDebug.filteredDebuggers"; //$NON-NLS-1$

    /**
	 * Boolean preference controlling whether the instruction stepping mode should be activated.
	 * 
	 * Temporary. See bugs 79872 and 80323.
	 */
	public static final String PREF_INSTRUCTION_STEP_MODE_ON = PLUGIN_ID + "cDebug.Disassembly.instructionStepOn"; //$NON-NLS-1$

	/**
	 * The default character set to use with unicode strings.
	 */
	public static final String DEF_CHARSET = "UTF-16"; //$NON-NLS-1$

    /**
     * Specifies the stepping mode (context/source/instruction)
     */
    public static final String PREF_STEP_MODE = PLUGIN_ID + ".steppingMode"; //$NON-NLS-1$

    public static final String PREF_VALUE_STEP_MODE_CONTEXT = "context"; //$NON-NLS-1$
    public static final String PREF_VALUE_STEP_MODE_SOURCE = "source"; //$NON-NLS-1$
    public static final String PREF_VALUE_STEP_MODE_INSTRUCTION = "instruction"; //$NON-NLS-1$
}
