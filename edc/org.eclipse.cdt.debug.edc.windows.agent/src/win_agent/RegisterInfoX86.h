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
#ifndef REGISTERINFOX86_H_
#define REGISTERINFOX86_H_

#include "RegisterInAgent.h"
#include "RegisterInfo.h"

/**
 * Static class holding static info of X86 registers supported by
 * EDC Windows agent.
 */
class RegisterInfoX86 {

public:
	static std::list<RegisterGroupInfo*>& getRegisterGroupInfo();

private:
	static std::list<RegisterGroupInfo*> sRegisterGroupInfos;
	
	static RegisterInfo* setupRegister(Properties& commProps, const char* name, const char* role);
	
	static RegisterGroupInfo* setupRegGroup_Basic();
	/*
	 * Set up static info about all registers and register groups supported.
	 * Note this should be called only once.
	 */
	static void setupRegisterGroupInfo();
};

#endif // REGISTERINFOX86_H_
