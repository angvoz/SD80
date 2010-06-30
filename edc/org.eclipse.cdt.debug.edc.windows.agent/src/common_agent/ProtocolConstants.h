/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

/**
 * Constants shared by TCF agents and clients that are not covered by service
 * interfaces. These are part of the communication protocol between agents and
 * clients. <br>
 * The content here must be in sync with that of file ProtocolConstants.java in 
 * host debugger of EDC. 
 */

// This is supplement to properties in IRunControl.
// ID of a context in hosting OS (e.g. process id, thread id).
// Supposed to replace IRunControl.PROCESS_ID
#define PROP_OS_ID 	"OSID"  // value type: string

/*
 * These are properties that a TCF agent should report with module-load or
 * module-unload events. They are all optional. But if relocation does
 * happen (e.g. for a Windows DLL), the agent must report at least
 * PROP_IMAGE_BASE_ADDRESS or PROP_CODE_ADDRESS. 
 */
#define PROP_NAME			"Name" // module file name (base)
#define PROP_FILE     		"File" // module file name (for breakpoints)
#define PROP_TIME     		"Time"
#define PROP_MODULE_LOADED     "Loaded"	// Boolean, whether loaded or unloaded.
#define PROP_CODE_SIZE     "CodeSize" 		// value type: Number (c++ int or long).
#define PROP_DATA_SIZE     "DataSize" 		// value type: Number.
#define PROP_BSS_SIZE     "BssSize"			// value type: Number.
// image base address for modules (PE file) from Windows
#define PROP_IMAGE_BASE_ADDRESS     "ImageBaseAddress" // value type: Number.
// following are for systems that can give us such info.
// Note these are mutually exclusive with PROP_IMAGE_BASE_ADDRESS.
#define PROP_CODE_ADDRESS     "CodeAddress" 	// value type: Number. 
#define PROP_DATA_ADDRESS     "DataAddress" 	// value type: Number.
#define PROP_BSS_ADDRESS     "BssAddress" 	// value type: Number.

#define PROP_REQUIRED_RESUME  "RequireResume"  // if true, the process should be resumed after setting breakpoints on the module
#define PROP_UID             "UID"	// the UID of the module (Symbian)

/**
 * State change reason of a context.
 * Reason can be any text, but if it is one of predefined strings,
 * a generic client might be able to handle it better.
 */
#define REASON_USER_REQUEST "Suspended"
#define REASON_STEP 		"Step"
#define REASON_BREAKPOINT 	"Breakpoint"
#define REASON_EXCEPTION 	"Exception"
#define REASON_CONTAINER 	"Container"
#define REASON_WATCHPOINT 	"Watchpoint"
#define REASON_SIGNAL 		"Signal"
#define REASON_SHAREDLIB	"Shared Library"
#define REASON_ERROR 		"Error"
