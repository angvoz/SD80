/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nokia - Initial API and implementation  Feb, 2010
 *******************************************************************************/

#ifndef REGISTERINAGENT_H_
#define REGISTERINAGENT_H_

#include "TCFContext.h"

/*
 * From TCF IRegisters.java
 */
// These are already define in Context.h
// PROP_ID
// PROP_PARENT_ID
// PROP_NAME
#define PROP_DESCRIPTION    "Description"       /** String, context description */
#define PROP_SIZE    "Size"                     /** Number, context size in bytes. Byte arrays in get/set commands should be same size */
#define PROP_READABLE    "Readable"             /** Boolean, true if context value can be read */
#define PROP_READ_ONCE    "ReadOnce"            /** Boolean, true if reading the context (register) destroys its current value */
#define PROP_WRITEABLE    "Writeable"           /** Boolean, true if context value can be written */
#define PROP_WRITE_ONCE    "WriteOnce"          /** Boolean, true if register value can not be overwritten - every write counts */
#define PROP_SIDE_EFFECTS    "SideEffects"      /** Boolean, true if writing the context can change values of other registers */
#define PROP_VOLATILE    "Volatile"             /** Boolean, true if the register value can change even when target is stopped */
#define PROP_FLOAT    "Float"                   /** Boolean, true if the register value is a floating-point value */
#define PROP_BIG_ENDIAN    "BigEndian"          /** Boolean, true if big endian */
#define PROP_LEFT_TO_RIGHT    "LeftToRight"     /** Boolean, true if the lowest numbered bit should be shown to user as the left-most bit */
#define PROP_FIST_BIT    "FirstBit"             /** Number, bit numbering base (0 or 1) to use when showing bits to user */
#define PROP_BITS    "Bits"                     /** Number, if context is a bit field, contains the field bit numbers in the parent context */
#define PROP_VALUES    "Values"                 /** Array of Map, predefined names (mnemonics) for some of context values */
#define PROP_MEMORY_ADDRESS    "MemoryAddress"  /** Number, the address of a memory mapped register */
#define PROP_MEMORY_CONTEXT    "MemoryContext"  /** String, the context ID of a memory context in which a memory mapped register is located */
#define PROP_CAN_SEARCH    "CanSearch"          /** Array of String, a list of attribute names which can be searched for starting on this context */
#define PROP_ROLE    "Role"                    /** String, the role the register plays in a program execution */

/**
 * Values of context property "Role".
 */
#define ROLE_PC   "PC"                         /** Program counter. Defines instruction to execute next */
#define ROLE_SP   "SP"                         /** Register defining the current stack pointer location */
#define ROLE_FP   "FP"                         /** Register defining the current frame pointer location */
#define ROLE_RET   "RET"                       /** Register used to store the return address for calls */
#define ROLE_CORE   "CORE"                     /** Indicates register or register groups which belong to the core state */

/**
 * Register context in a TCF agent.
 *
 */
class RegisterInAgent : public Context {

public:
	RegisterInAgent(const std::string& name, const ContextID& parentID, Properties& props);

	static std::string& createInternalID(const std::string& name, const std::string& parentID);
};

#endif // REGISTERINAGENT_H_
