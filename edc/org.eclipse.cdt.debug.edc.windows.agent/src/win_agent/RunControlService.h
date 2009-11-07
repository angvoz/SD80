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
#pragma once

#include "TCFService.h"

struct Channel;
struct Protocol;

class RunControlService: public TCFService {
public:
	RunControlService(Protocol * proto);
	~RunControlService(void);

	const char* GetName();

	static void command_get_context(char * token, Channel * c);
	static void command_get_children(char * token, Channel * c);
	static void command_get_state(char * token, Channel * c);
	static void command_resume(char * token, Channel * c);
	static void command_suspend(char * token, Channel * c);
	static void command_terminate(char * token, Channel * c);
};
