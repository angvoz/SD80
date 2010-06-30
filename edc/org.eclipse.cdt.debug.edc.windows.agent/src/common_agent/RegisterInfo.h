/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nokia - Initial API and implementation.  March, 2010
 *******************************************************************************/

#ifndef REGISTERINFO_H_
#define REGISTERINFO_H_

#include <map>

#include "PropertyValue.h"

/**
 * Place holder for static info about register or register group.
 * Dynamic info about a register or register group include register
 * value and TCF context ID. All other info are static.
 */
class RegisterInfo {

private:
	Properties	properties;

public:
	/**
	 * @param name
	 *            name of the register, must be unique in its group.
	 * @param commonProps
	 *            properties common to all registers in the same group. Can be empty.
	 * @param specificProps
	 *            properties specific to the register.
	 */
	RegisterInfo(Properties& commonProps, Properties& specificProps) {
		properties = commonProps;
		properties.insert(specificProps.begin(), specificProps.end());
	}

	Properties& getProperties() {
		return properties;
	}
};

class RegisterGroupInfo {
private:
	/**
	 * Group specific properties
	 */
	Properties	properties;
	
	/**
	 * Registers in the group.
	 */
	std::list<RegisterInfo*>	registers;


public:
	RegisterGroupInfo(Properties& props, std::list<RegisterInfo*>& regs) {
		properties = props;
		registers = regs;
	}

	/**
	 * Group specific properties
	 */
	Properties& getProperties() {
		return properties;
	}

	std::list<RegisterInfo*>& getRegisters() {
		return registers;
	}
};

#endif // REGISTERINFO_H_
