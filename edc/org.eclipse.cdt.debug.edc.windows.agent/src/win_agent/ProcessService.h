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

#include <map>
#include <string>
#include "TCFService.h"
#include "RunControlContext.h"

struct Protocol;
struct Channel;

class ProcessService: public TCFService {
public:
	ProcessService(Protocol * proto);
	~ProcessService(void);

	const char* GetName();

	static void command_get_context(const char *, Channel *);
	static void command_get_children(const char *, Channel *);
	static void command_attach(const char *, Channel *);
	static void command_detach(const char *, Channel *);
	static void command_terminate(const char *, Channel *);
	static void command_signal(const char *, Channel *);
	static void command_get_environment(const char *, Channel *);
	static void command_start(const char *, Channel *);
};
