/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nokia - Initial API and implementation  Sep 17, 2009
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.tcf.extension;

/**
 * Constants shared by TCF agents and clients that are not covered by service
 * interfaces. These are part of the communication protocol between agents and
 * clients. <br>
 * This file is supposed to be accessed by both Java-based agents and clients.
 * For C/C++ or other language based agents, a similar file should be created.
 */
public interface ProtocolConstants {
	/**
	 * OS defined ID of a context in the debugged OS. This is supplement to
	 * properties in IRunControl. It is similar to IRunControl.PROCESS_ID but
	 * different in that this applies to any OS context (process, thread, etc)
	 * while PROCESS_ID only applies to process context.
	 */
	static final String PROP_OS_ID = "OSID"; // value type: string

	/**
	 * Extra detail an agent wants host debugger to display for a suspended
	 * context.
	 * @since 2.0
	 */
	static final String PROP_SUSPEND_DETAIL = "message"; // value type: string

	/**
	 * Is the context (process, thread, etc) the foreground (a.k.a current) one in the
	 * debugged OS.
	 * @since 2.0
	 */
	static final String PROP_IS_FOREGROUND = "is_foreground"; // value type: boolean

	/*
	 * These are properties that a TCF agent should report with module (a
	 * library or executable) load or unload events. They are not all required.
	 * But if relocation does happen (e.g. for a Windows DLL), the agent must
	 * report at least PROP_IMAGE_BASE_ADDRESS or PROP_CODE_ADDRESS.
	 */
	public static interface IModuleProperty {
		public static final String 
				PROP_FILE = "File", // module file name
				
				PROP_TIME = "Time", 

				// whether the module is loaded or unloaded.
				// Value type: boolean
				PROP_MODULE_LOADED = "Loaded", 

				PROP_CODE_SIZE = "CodeSize", // value type: Number.
				PROP_DATA_SIZE = "DataSize", // value type: Number.
				PROP_BSS_SIZE = "BssSize", // value type: Number.

				// image base address for modules (PE file) from Windows
				// value type: Number (Java "Number").
				PROP_IMAGE_BASE_ADDRESS = "ImageBaseAddress", 

				// following are for systems that can give us such info.
				// Note these are mutually exclusive with
				// PROP_IMAGE_BASE_ADDRESS.
				PROP_CODE_ADDRESS = "CodeAddress", // value type: Number.
				PROP_DATA_ADDRESS = "DataAddress", // value type: Number.
				PROP_BSS_ADDRESS = "BssAddress", // value type: Number.
				
				// This property indicates whether host should send a resume
				// command after handling a module load/unload event.
				// Usually agent knows if the debugged process is suspended
				// when the load/unload event happens, thus it's agent's duty
				// to set this property. 
				// By default, host assumes that resume is needed.
				PROP_RESUME = "RequireResume"; // value type: Boolean
	};

}
