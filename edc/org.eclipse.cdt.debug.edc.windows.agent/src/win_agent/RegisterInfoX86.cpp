/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nokia - Initial API and implementation. March, 2010
 *******************************************************************************/

#include "RegisterInfoX86.h"

std::list<RegisterGroupInfo*> RegisterInfoX86::sRegisterGroupInfos;

std::list<RegisterGroupInfo*>& RegisterInfoX86::getRegisterGroupInfo() {
	if (sRegisterGroupInfos.size() == 0)
		setupRegisterGroupInfo();
	
	return sRegisterGroupInfos;
}
	
RegisterInfo* RegisterInfoX86::setupRegister(Properties& commProps, const char* name, const char* role) {
	Properties props;
	props[PROP_NAME] = new PropertyValue(name);
	if (role != NULL)
		props[PROP_ROLE] = new PropertyValue(role);
	
	return new RegisterInfo(commProps, props);
}

/*
 * Set up static info for x86 basic program execution registers
 * which includes GPR, Segment Registers and EFLAGS & EIP.
 * See "Intel® 64 and IA-32 Architectures Software Developer’s Manual" Vol 1.
 */
RegisterGroupInfo* RegisterInfoX86::setupRegGroup_Basic() {
	// Properties common to all registers in the group
	//
	Properties commonProps;

	commonProps[PROP_SIZE] = new PropertyValue(4);
	commonProps[PROP_READABLE] = new PropertyValue(true);
	commonProps[PROP_WRITEABLE] = new PropertyValue(true);
	// pass value in big-endian string.
	// Currently EDC host side does not honor this yet, instead
	// it always assume big-endian....02/28/10
	commonProps[PROP_BIG_ENDIAN] = new PropertyValue(true);
	// See TCF RegistersProxy.Context for default values of other properties.

	std::list<RegisterInfo*> regs;

	regs.push_back(setupRegister(commonProps, "EAX", NULL));
	regs.push_back(setupRegister(commonProps, "ECX", NULL));
	regs.push_back(setupRegister(commonProps, "EDX", NULL));
	regs.push_back(setupRegister(commonProps, "EBX", NULL));
	regs.push_back(setupRegister(commonProps, "ESP", ROLE_SP));
	regs.push_back(setupRegister(commonProps, "EBP", ROLE_FP));
	regs.push_back(setupRegister(commonProps, "ESI", NULL));
	regs.push_back(setupRegister(commonProps, "EDI", NULL));
	regs.push_back(setupRegister(commonProps, "GS", NULL));
	regs.push_back(setupRegister(commonProps, "FS", NULL));
	regs.push_back(setupRegister(commonProps, "ES", NULL));
	regs.push_back(setupRegister(commonProps, "DS", NULL));
	regs.push_back(setupRegister(commonProps, "EIP", ROLE_PC));
	regs.push_back(setupRegister(commonProps, "CS", NULL));
	regs.push_back(setupRegister(commonProps, "EFL", NULL));
	regs.push_back(setupRegister(commonProps, "SS", NULL));

	Properties groupProps;
	groupProps[PROP_NAME] =	new PropertyValue("Basic");
	groupProps[PROP_DESCRIPTION] = new PropertyValue("Basic Program Execution Registers of x86");
	groupProps[PROP_ROLE] =	new PropertyValue(ROLE_CORE);

	return new RegisterGroupInfo(groupProps, regs);
}

/*
 * Set up static info about all registers and register groups supported.
 * Note this should be called only once.
 */
void RegisterInfoX86::setupRegisterGroupInfo() {
	sRegisterGroupInfos.push_back(setupRegGroup_Basic());
}
