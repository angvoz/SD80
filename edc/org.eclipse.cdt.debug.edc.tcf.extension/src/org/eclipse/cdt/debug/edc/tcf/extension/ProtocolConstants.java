/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
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
	// This is supplement to properties in IRunControl.
	static final String PROP_THREAD_ID = "ThreadID"; // value type: decimal string
	
	/*
	 * These are properties that a TCF agent should report with module-load or
	 * module-unload events. They are all optional. But if relocation does
	 * happen (e.g. for a Windows DLL), the agent must report at least
	 * PROP_IMAGE_BASE_ADDRESS or PROP_CODE_ADDRESS. 
	 */
	public static interface IModuleProperty {
		public static final String 
			PROP_FILE = "File", // module file name
			PROP_TIME = "Time",
			PROP_MODULE_LOADED = "Loaded",	// Boolean, whether loaded or unloaded.
			
			PROP_CODE_SIZE = "CodeSize", 		// value type: Number.
			PROP_DATA_SIZE = "DataSize", 		// value type: Number.
			PROP_BSS_SIZE = "BssSize", 			// value type: Number.

			// image base address for modules (PE file) from Windows
			PROP_IMAGE_BASE_ADDRESS = "ImageBaseAddress", // value type: Number (Java "Number"). 

			// following are for systems that can give us such info.
			// Note these are mutually exclusive with PROP_IMAGE_BASE_ADDRESS.
			PROP_CODE_ADDRESS = "CodeAddress", 	// value type: Number. 
			PROP_DATA_ADDRESS = "DataAddress", 	// value type: Number.
			PROP_BSS_ADDRESS = "BssAddress"; 	// value type: Number.
	};

}
